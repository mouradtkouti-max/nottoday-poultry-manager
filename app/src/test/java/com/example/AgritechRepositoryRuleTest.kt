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
import org.robolectric.annotation.Config
import java.util.UUID

@Config(sdk = [36])
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

        // 0. Before cycle exists: sales invoice and feed purchase MUST fail rather than creating a phantom 12000-bird cycle
        val missingCycleSale = withTimeout(5000L) {
            repo.saveSalesInvoiceAndMigrateIdempotent(
                farmId = farmId,
                cycleId = "cycle_missing_999",
                idempotencyKey = "inv_missing_${UUID.randomUUID()}",
                dateText = "اليوم 5",
                invoice = sampleInvoice.copy(cycleId = "cycle_missing_999")
            )
        }
        assertTrue("Sale on missing cycle must fail without creating phantom cycle", missingCycleSale.isFailure)

        val missingCycleFeed = withTimeout(5000L) {
            repo.recordFeedPurchaseAndExpenseIdempotent(
                idempotencyKey = "feed_missing_${UUID.randomUUID()}",
                tx = TransactionModel(
                    id = "tx_feed_missing",
                    farmId = farmId,
                    cycleId = "cycle_missing_999",
                    type = TransactionType.EXPENSE,
                    category = "🌾 علف",
                    amount = 80000.0,
                    currency = "دج",
                    date = "اليوم 1",
                    quantityQuintals = 10.0
                ),
                addedFeedKg = 1000.0
            )
        }
        assertTrue("Feed purchase on missing cycle must fail without creating phantom cycle", missingCycleFeed.isFailure)

        // Create the real active cycle with 1000 birds placed and 100 cumulative mortality (900 available birds)
        withTimeout(5000L) {
            val cycleRes = repo.saveCycleRecord(
                CycleModel(
                    id = "cycle_2026_01",
                    farmId = farmId,
                    status = "active",
                    totalBirdsPlaced = 1000,
                    targetWeight = 2.3,
                    cumulativeMortality = 100,
                    cumulativeBirdsSold = 0,
                    feedInventoryKg = 1500.0
                )
            )
            assertTrue(cycleRes.isSuccess)
        }

        // First submission (500 birds <= 900 available) succeeds and returns true
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

        // Attempting to sell 500 more birds when only 400 remain (1000 placed - 100 mortality - 500 sold = 400) MUST fail
        val overSale = withTimeout(5000L) {
            repo.saveSalesInvoiceAndMigrateIdempotent(
                farmId = farmId,
                cycleId = "cycle_2026_01",
                idempotencyKey = "inv_over_${UUID.randomUUID()}",
                dateText = "اليوم 6",
                invoice = sampleInvoice.copy(invoiceNumber = "INV-1-0002", totalBirdsSold = 500)
            )
        }
        assertTrue("Selling more birds than available must fail", overSale.isFailure)

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

        // Verify single source of truth balance and preserved farmName on shared farm
        val txs = withTimeout(5000L) {
            repo.observeTransactions(farmId).first { it.size == 2 }
        }
        val saleTx = txs.first { it.type == TransactionType.INCOME }
        assertEquals("عنبر البركة 1", saleTx.invoiceData?.farmName)

        val summary = repo.calculateNetBalanceFromTransactions(txs)
        assertEquals(380000.0, summary.totalIncome, 0.01)
        assertEquals(80000.0, summary.totalExpenses, 0.01)
        assertEquals(300000.0, summary.netBalance, 0.01)
    }

    @Test
    fun sharedFarmCollaboration_verifiedInviteJoin_unifiedChat_vetMedicationAndWorkerDailyReport() = runBlocking {
        val farmId = "farm_${UUID.randomUUID().toString().take(8)}"

        // 1. Owner initializes farm, active cycle, and creates verified invites for VET and WORKER
        val ownerUid = signInTestUser("owner_${UUID.randomUUID()}@farm.com")
        val repo = AgritechFirestoreRepository(firestore)
        val vetInvite: FarmInviteModel
        val workerInvite: FarmInviteModel
        withTimeout(5000L) {
            repo.setUserRoleAndFarm(AppRole.OWNER, farmId, "Owner").getOrThrow()
            repo.saveFarmRecord(
                FarmModel(
                    id = farmId,
                    name = "عنبر التعاون المشترك",
                    location = "البليدة",
                    capacity = 8000,
                    assignedWorkerId = "worker_1",
                    members = mapOf(ownerUid to "OWNER")
                ),
                AppRole.OWNER
            ).getOrThrow()
            repo.saveCycleRecord(
                CycleModel(
                    id = "cycle_2026_01",
                    farmId = farmId,
                    status = "active",
                    totalBirdsPlaced = 8000,
                    targetWeight = 2.3,
                    feedInventoryKg = 2000.0
                )
            ).getOrThrow()
            vetInvite = repo.createFarmInvite(
                farmId = farmId,
                role = AppRole.VETERINARIAN,
                customInviteId = "vet_inv_1"
            ).getOrThrow()
            workerInvite = repo.createFarmInvite(
                farmId = farmId,
                role = AppRole.WORKER,
                customInviteId = "worker_inv_1"
            ).getOrThrow()
        }

        // 2. Vet signs in: setUserRoleAndFarm alone does NOT add Vet to farm.members; acceptFarmInvite does!
        val vetUid = signInTestUser("vet_${UUID.randomUUID()}@farm.com")
        withTimeout(5000L) {
            repo.setUserRoleAndFarm(AppRole.VETERINARIAN, farmId, "Dr. Vet").getOrThrow()
            // Before accepting invite, Vet is not in farm.members so writing a vetNote fails
            val preInviteWrite = repo.saveVetNote(
                VetNoteModel(
                    id = "vn_pre_invite",
                    farmId = farmId,
                    cycleId = "cycle_2026_01",
                    dayNumber = 4,
                    medicineName = "PreInviteTest",
                    dosage = "1ml/L",
                    note = "Should fail before invite acceptance",
                    isUrgent = false
                )
            )
            assertTrue("Vet cannot write to farm before accepting verified invite", preInviteWrite.isFailure)

            // Accept verified invite
            val acceptVetRes = repo.acceptFarmInvite(farmId, vetInvite.inviteId, AppRole.VETERINARIAN)
            assertTrue(acceptVetRes.isSuccess)
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

        // Vet sends a unified financial request chat message and observes it back with full fields
        val channelId = "${farmId}_cycle_2026_01_vet"
        val sentChatMsg = ChatMessageModel(
            id = "msg_vet_${UUID.randomUUID()}",
            cycleId = channelId,
            senderId = vetUid,
            senderName = "Dr. Vet",
            senderRole = "VETERINARIAN",
            text = "فاتورة دواء إنروفلوكساسين",
            timestamp = "09:45 ص",
            linkedTransactionId = vetInvKey,
            linkedVetNoteId = "vn_chat_1",
            isFinancialRequest = true,
            requestCategory = "Enrofloxacin 20%",
            requestAmount = 14500.0,
            requestCurrency = "دج",
            status = "PENDING",
            requestStatus = "PENDING"
        )
        withTimeout(5000L) {
            repo.sendChatMessage(sentChatMsg, farmIdHint = farmId)
            val observedMessages = repo.observeChatMessages(channelId).first { list ->
                list.any { it.id == sentChatMsg.id }
            }
            val observed = observedMessages.first { it.id == sentChatMsg.id }
            assertEquals(vetInvKey, observed.linkedTransactionId)
            assertEquals("vn_chat_1", observed.linkedVetNoteId)
            assertTrue(observed.isFinancialRequest)
            assertEquals("Enrofloxacin 20%", observed.requestCategory)
            assertEquals(14500.0, observed.requestAmount, 0.01)
            assertEquals("دج", observed.requestCurrency)
            assertEquals("PENDING", observed.status)
            assertEquals("PENDING", observed.requestStatus)
            assertEquals("SYNCED", observed.deliveryState)
        }

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

        // 3. Worker joins the same shared farm via verified invite, reads Vet's note, and writes DailyReport
        signInTestUser("worker_${UUID.randomUUID()}@farm.com")
        withTimeout(5000L) {
            repo.setUserRoleAndFarm(AppRole.WORKER, farmId, "Barn Worker").getOrThrow()
            repo.acceptFarmInvite(farmId, workerInvite.inviteId, AppRole.WORKER).getOrThrow()
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
                    mortalityCount = 5,
                    temperature = 32.0,
                    gasUnits = 3.5,
                    ventilationStatus = "جيدة",
                    generalStatus = "طبيعية"
                )
            )
        }
        assertTrue(repRes.isSuccess)

        // Worker is blocked from writing vetNotes and recording non-PENDING or illegal farm treasury expenses
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

        val workerTxRes = withTimeout(5000L) {
            repo.recordTransactionIdempotent(
                idempotencyKey = "tx_worker_blocked_${UUID.randomUUID()}",
                tx = TransactionModel(
                    id = "tx_worker_blocked",
                    farmId = farmId,
                    cycleId = "cycle_2026_01",
                    type = TransactionType.EXPENSE,
                    category = "MAINTENANCE",
                    amount = 3500.0,
                    currency = "دج",
                    date = "اليوم 4",
                    status = "APPROVED"
                )
            )
        }
        assertTrue(workerTxRes.isFailure)
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

    @Test
    fun googlePlayCompliance_cloudAccountDeletion_and_ugcReportSubmission() = runBlocking {
        val userUid = signInTestUser("gp_compliance_${UUID.randomUUID()}@farm.com")
        val repo = AgritechFirestoreRepository(firestore)
        val farmId = "farm_gp_${UUID.randomUUID().toString().take(8)}"

        withTimeout(5000L) {
            repo.setUserRoleAndFarm(AppRole.OWNER, farmId, "GP Owner").getOrThrow()
            repo.saveFarmRecord(
                FarmModel(
                    id = farmId,
                    name = "عنبر اختبار المتجر",
                    location = "المدية",
                    capacity = 6000,
                    members = mapOf(userUid to "OWNER")
                ),
                AppRole.OWNER
            ).getOrThrow()
            repo.saveUserFarmMembershipToCloud(farmId, AppRole.OWNER, displayName = "GP Owner").getOrThrow()

            // Submit UGC report to /ugc_reports/{reportId}
            val reportRes = repo.submitUgcReportToCloud(
                itemId = "post_1",
                targetUid = "conv_feed_trader",
                reason = "محتوى مسيء أو غير لائق",
                contentType = "MARKET_POST"
            )
            assertTrue("Submitting UGC report to cloud must succeed", reportRes.isSuccess)

            // Save blocked user IDs to /users/{uid}.blockedUserIds
            val blockRes = repo.saveBlockedUserToCloud(setOf("conv_feed_trader"))
            assertTrue("Saving blockedUserIds to cloud profile must succeed", blockRes.isSuccess)

            // Delete user account and all cloud data completely
            val deleteRes = repo.deleteUserAccountAndDataCompletely(userUid)
            assertTrue("Deleting account and cloud data must succeed: ${deleteRes.exceptionOrNull()?.message}", deleteRes.isSuccess)
        }
    }
}

