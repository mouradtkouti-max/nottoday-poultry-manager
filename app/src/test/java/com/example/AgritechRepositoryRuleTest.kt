package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.models.*
import com.example.utils.*
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class AgritechRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun ownerRole_savesFarmAndIdempotentInvoiceAndExpense_preventsDuplicatesAndComputesBalance() = runBlocking {
        val ownerUid = signInTestUser("owner_${UUID.randomUUID()}@farm.com")
        val repo = AgritechFirestoreRepository(firestore)
        val farmId = "farm_${UUID.randomUUID().toString().take(8)}"

        withTimeout(5000L) {
            val roleRes = repo.setUserRoleAndFarm(AppRole.OWNER, farmId, "Farm Owner")
            assertTrue(roleRes.isSuccess)
            val farmRes = repo.saveFarmRecord(
                FarmModel(
                    id = farmId,
                    name = "عنبر البركة 1",
                    location = "المدية",
                    capacity = 12000,
                    assignedWorkerId = "worker_1",
                    members = mapOf(ownerUid to "OWNER")
                ),
                AppRole.OWNER
            )
            assertTrue(farmRes.isSuccess)
        }

        val idempotencyKey = "inv_${UUID.randomUUID()}"
        val sampleInvoice = SalesInvoiceModel(
            invoiceNumber = "INV-1-0001",
            farmName = "عنبر البركة 1",
            cycleId = "cycle_2026_01",
            dateText = "اليوم 5",
            birdAgeDays = 5,
            buyerName = "كمال رابحي",
            truckPlate = "04521-122-16",
            totalCrates = 50,
            birdsPerCrate = 10,
            totalBirdsSold = 500,
            totalGrossKg = 1200.0,
            totalTareKg = 200.0,
            totalNetKg = 1000.0,
            avgBirdWeightKg = 2.0,
            pricePerKg = 380.0,
            totalAmount = 380000.0,
            currency = "دج",
            tareWeightsCount = 4,
            grossWeightsCount = 10
        )

        // First submission succeeds and returns true
        val firstSave = withTimeout(5000L) {
            repo.saveSalesInvoiceAndMigrateIdempotent(
                farmId = farmId,
                cycleId = "cycle_2026_01",
                idempotencyKey = idempotencyKey,
                dateText = "اليوم 5 - كمال رابحي",
                invoice = sampleInvoice
            )
        }
        assertTrue(firstSave.isSuccess)
        assertTrue(firstSave.getOrThrow())

        // Duplicate submission with same idempotencyKey returns false (idempotent no-op)
        val duplicateSave = withTimeout(5000L) {
            repo.saveSalesInvoiceAndMigrateIdempotent(
                farmId = farmId,
                cycleId = "cycle_2026_01",
                idempotencyKey = idempotencyKey,
                dateText = "اليوم 5 - كمال رابحي",
                invoice = sampleInvoice
            )
        }
        assertTrue(duplicateSave.isSuccess)
        assertFalse(duplicateSave.getOrThrow())

        // Record an expense
        val expKey = "exp_${UUID.randomUUID()}"
        val expSave = withTimeout(5000L) {
            repo.recordTransactionIdempotent(
                idempotencyKey = expKey,
                tx = TransactionModel(
                    id = expKey,
                    farmId = farmId,
                    cycleId = "cycle_2026_01",
                    type = TransactionType.EXPENSE,
                    category = "tx_feed",
                    amount = 80000.0,
                    currency = "دج",
                    date = "اليوم 5"
                )
            )
        }
        assertTrue(expSave.isSuccess)
        assertTrue(expSave.getOrThrow())

        // Verify single source of truth balance on shared farm
        val txs = withTimeout(5000L) {
            repo.observeTransactions(farmId).first { it.size == 2 }
        }
        val summary = repo.calculateNetBalanceFromTransactions(txs)
        assertEquals(380000.0, summary.totalIncome, 0.01)
        assertEquals(80000.0, summary.totalExpenses, 0.01)
        assertEquals(300000.0, summary.netBalance, 0.01)
    }

    @Test
    fun sharedFarmCollaboration_vetCanCreateMedicationExpense_andWorkerCanWriteDailyReport() = runBlocking {
        val farmId = "farm_${UUID.randomUUID().toString().take(8)}"

        // 1. Owner initializes farm
        signInTestUser("owner_${UUID.randomUUID()}@farm.com")
        val repo = AgritechFirestoreRepository(firestore)
        withTimeout(5000L) {
            repo.setUserRoleAndFarm(AppRole.OWNER, farmId, "Owner").getOrThrow()
        }

        // 2. Vet joins the same shared farm and submits a Medication Invoice + Prescription
        signInTestUser("vet_${UUID.randomUUID()}@farm.com")
        withTimeout(5000L) {
            repo.setUserRoleAndFarm(AppRole.VETERINARIAN, farmId, "Dr. Vet").getOrThrow()
        }

        val vetInvKey = "tx_vet_${UUID.randomUUID()}"
        val vetInvRes = withTimeout(5000L) {
            repo.recordVetInvoiceAndPrescriptionIdempotent(
                idempotencyKey = vetInvKey,
                tx = TransactionModel(
                    id = vetInvKey,
                    farmId = farmId,
                    cycleId = "cycle_2026_01",
                    type = TransactionType.EXPENSE,
                    category = "MEDICATION",
                    amount = 14500.0,
                    currency = "دج",
                    date = "اليوم 4"
                ),
                vetNote = VetNoteModel(
                    id = "vn_${UUID.randomUUID()}",
                    farmId = farmId,
                    cycleId = "cycle_2026_01",
                    dayNumber = 4,
                    medicineName = "Enrofloxacin 20%",
                    dosage = "1ml/L",
                    note = "Approved respiratory treatment",
                    isUrgent = true
                )
            )
        }
        assertTrue(vetInvRes.isSuccess)
        assertTrue(vetInvRes.getOrThrow())

        // Vet is blocked from recording non-MEDICATION expenses (e.g. FEED)
        val illegalVetTx = withTimeout(5000L) {
            repo.recordTransactionIdempotent(
                idempotencyKey = "tx_illegal_${UUID.randomUUID()}",
                tx = TransactionModel(
                    id = "tx_illegal",
                    farmId = farmId,
                    cycleId = "cycle_2026_01",
                    type = TransactionType.EXPENSE,
                    category = "FEED",
                    amount = 50000.0,
                    currency = "دج",
                    date = "اليوم 4"
                )
            )
        }
        assertTrue(illegalVetTx.isFailure)

        // 3. Worker joins the same shared farm, reads Vet's note, and writes DailyReport
        signInTestUser("worker_${UUID.randomUUID()}@farm.com")
        withTimeout(5000L) {
            repo.setUserRoleAndFarm(AppRole.WORKER, farmId, "Barn Worker").getOrThrow()
        }

        val sharedNotes = withTimeout(5000L) {
            repo.observeVetNotes(farmId).first { it.isNotEmpty() }
        }
        assertEquals("Enrofloxacin 20%", sharedNotes.first().medicineName)

        val repRes = withTimeout(5000L) {
            repo.saveDailyReport(
                DailyReportModel(
                    id = "rep_${UUID.randomUUID()}",
                    farmId = farmId,
                    cycleId = "cycle_2026_01",
                    dayNumber = 4,
                    feedIntakeKg = 340.0,
                    waterIntakeLiters = 680.0,
                    mortalityCount = 2,
                    temperature = 32.0,
                    gasUnits = 3.5,
                    ventilationStatus = "جيدة",
                    generalStatus = "طبيعية"
                )
            )
        }
        assertTrue(repRes.isSuccess)

        // Worker is blocked from writing vetNotes and recording expenses
        val workerVetRes = withTimeout(5000L) {
            repo.saveVetNote(
                VetNoteModel(
                    id = "vet_${UUID.randomUUID()}",
                    farmId = farmId,
                    cycleId = "cycle_2026_01",
                    dayNumber = 4,
                    medicineName = "Tylosin",
                    dosage = "1g/L",
                    note = "Test",
                    isUrgent = false
                )
            )
        }
        assertTrue(workerVetRes.isFailure)
    }

    @Test
    fun unauthenticatedCaller_rejectedWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = AgritechFirestoreRepository(firestore)
        var failed = false
        try {
            withTimeout(3000L) {
                repo.observeDailyReports("farm_1").first()
            }
        } catch (e: Throwable) {
            failed = true
            var curr: Throwable? = e
            var firestoreEx: FirebaseFirestoreException? = null
            while (curr != null) {
                if (curr is FirebaseFirestoreException) {
                    firestoreEx = curr
                    break
                }
                curr = curr.cause
            }
            if (firestoreEx != null) {
                assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx.code)
            }
        }
        assertTrue(failed)
    }
}
