package com.example

import com.example.models.*
import com.example.utils.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun poultryCalculators_fcr_epef_and_thinningBiomass_areAccurate() {
        assertEquals(40.0, PoultryCalculators.getRealisticFieldWeightGrams(0), 0.001)
        assertEquals(187.0, PoultryCalculators.getRealisticFieldWeightGrams(7), 0.001)
        assertEquals(2050.0, PoultryCalculators.getRealisticFieldWeightGrams(35), 0.001)
        assertEquals(2.05, PoultryCalculators.getRoss308StandardWeightKg(35), 0.001)

        val liveBirds = PoultryCalculators.calculateLiveBirds(
            totalBirdsPlaced = 10500,
            totalMortality = 500,
            cumulativeBirdsSold = 2000
        )
        assertEquals(8000, liveBirds)

        val totalProducedBiomassKg = PoultryCalculators.calculateTotalProducedBiomassKg(
            liveBirds = liveBirds,
            avgLiveWeightKg = 2.05,
            cumulativeSoldWeightKg = 4000.0
        )
        assertEquals(20400.0, totalProducedBiomassKg, 0.001)

        val fcr = PoultryCalculators.calculateFcr(
            totalFeedKg = 32640.0,
            initialBirds = 10500,
            totalMortality = 500,
            birdsSold = 2000,
            ageInDays = 35,
            cumulativeSoldWeightKg = 4000.0
        )
        assertEquals(1.6, fcr, 0.001)

        val epef = PoultryCalculators.calculateEpef(
            livabilityPct = 95.238,
            avgLiveWeightKg = 2.05,
            ageInDays = 35,
            fcr = fcr
        )
        assertTrue(epef > 340.0)
        assertEquals(0.0, PoultryCalculators.calculateEpef(95.0, 2.05, 0, 1.6), 0.001)
        assertEquals(0.0, PoultryCalculators.calculateEpef(95.0, 2.05, 35, 0.0), 0.001)
    }

    @Test
    fun withdrawalPeriod_blocksSalesDuringActiveAntibiotic_andClearsAfterExpiry() {
        val vitaminNote = VetNoteModel(
            id = "vn_vit_1",
            farmId = "farm_1",
            cycleId = "cycle_1",
            dayNumber = 15,
            medicineName = "فيتامين AD3E",
            dosage = "1 مل / لتر",
            note = "رفع مناعة",
            isUrgent = false,
            withdrawalDays = 0
        )
        assertEquals(
            0,
            PoultryCalculators.calculateWithdrawalDaysRemaining(
                vetNotes = listOf(vitaminNote),
                currentCycleDay = 17
            )
        )

        val antibioticNote = VetNoteModel(
            id = "vn_abx_1",
            farmId = "farm_1",
            cycleId = "cycle_1",
            dayNumber = 28,
            medicineName = "إنروفلوكساسين 20%",
            dosage = "1 مل / لتر",
            note = "علاج تنفسي",
            isUrgent = true,
            withdrawalDays = 7
        )
        assertEquals(
            5,
            PoultryCalculators.calculateWithdrawalDaysRemaining(
                vetNotes = listOf(antibioticNote),
                currentCycleDay = 30
            )
        )
        assertEquals(
            0,
            PoultryCalculators.calculateWithdrawalDaysRemaining(
                vetNotes = listOf(antibioticNote),
                currentCycleDay = 35
            )
        )
    }

    @Test
    fun unmeasuredWeight_returnsNull_untilActualMeasurementOrInvoiceExists() {
        val unmeasuredReport = DailyReportModel(
            id = "rep_d10",
            farmId = "farm_1",
            cycleId = "cycle_1",
            dayNumber = 10,
            feedIntakeKg = 250.0,
            waterIntakeLiters = 500.0,
            mortalityCount = 2,
            temperature = 29.0,
            actualAvgWeightKg = 0.0
        )
        assertNull(
            PoultryCalculators.resolveActualAvgBirdWeightKg(
                reports = listOf(unmeasuredReport),
                cycleTransactions = emptyList(),
                cycleId = "cycle_1"
            )
        )

        val measuredReport = unmeasuredReport.copy(id = "rep_d14", dayNumber = 14, actualAvgWeightKg = 0.445)
        val resolved = PoultryCalculators.resolveActualAvgBirdWeightKg(
            reports = listOf(unmeasuredReport, measuredReport),
            cycleTransactions = emptyList(),
            cycleId = "cycle_1"
        )
        assertNotNull(resolved)
        assertEquals(0.445, resolved!!, 0.001)
    }

    @Test
    fun cloudSyncMerge_protectsLocalEditsFromZeroTimestampCloudOverwrite_andPreservesCycleInventory() {
        val localEditedReport = DailyReportModel(
            id = "rep_sync_d5",
            farmId = "farm_1",
            cycleId = "cycle_1",
            dayNumber = 5,
            feedIntakeKg = 190.0,
            waterIntakeLiters = 380.0,
            mortalityCount = 3,
            temperature = 31.0,
            isEdited = true,
            updatedAtMillis = 1730000500000L,
            actualAvgWeightKg = 0.135
        )
        // Cloud report has updatedAtMillis = 0L and older feedIntakeKg = 120.0
        val staleCloudReportWithZeroTimestamp = DailyReportModel(
            id = "rep_sync_d5",
            farmId = "farm_1",
            cycleId = "cycle_1",
            dayNumber = 5,
            feedIntakeKg = 120.0,
            waterIntakeLiters = 240.0,
            mortalityCount = 1,
            temperature = 31.0,
            isEdited = false,
            updatedAtMillis = 0L,
            actualAvgWeightKg = 0.0
        )

        val mergedReport = AgritechMockDb.mergeCloudReportWithLocal(
            cloudReport = staleCloudReportWithZeroTimestamp,
            existingLocal = localEditedReport
        )
        assertEquals(190.0, mergedReport.feedIntakeKg, 0.001)
        assertEquals(3, mergedReport.mortalityCount)
        assertEquals(0.135, mergedReport.actualAvgWeightKg, 0.001)
        assertTrue(mergedReport.isEdited)

        // Cycle merge must preserve local feedInventoryKg, cumulativeBirdsSold, and cumulativeSoldWeightKg
        val localCycle = CycleModel(
            id = "cycle_sync_1",
            farmId = "farm_1",
            totalBirdsPlaced = 8000,
            feedInventoryKg = 2400.0,
            cumulativeBirdsSold = 1500,
            cumulativeSoldWeightKg = 3300.0
        )
        val cloudCycleWithZeroInventory = CycleModel(
            id = "cycle_sync_1",
            farmId = "farm_1",
            totalBirdsPlaced = 8000,
            feedInventoryKg = 0.0,
            cumulativeBirdsSold = 0,
            cumulativeSoldWeightKg = 0.0
        )
        val mergedCycle = AgritechMockDb.mergeCloudCycleWithLocal(
            cloudCycle = cloudCycleWithZeroInventory,
            localCycle = localCycle
        )
        assertEquals(2400.0, mergedCycle.feedInventoryKg, 0.001)
        assertEquals(1500, mergedCycle.cumulativeBirdsSold)
        assertEquals(3300.0, mergedCycle.cumulativeSoldWeightKg, 0.001)

        // Crucial check: When daily feed consumption deducts local feedInventoryKg (e.g. 4000 kg -> 3500 kg),
        // an older cloudCycle snapshot with 4000.0 kg must NOT overwrite the local deduction back to 4000.0 kg!
        val localCycleAfterConsumption = CycleModel(
            id = "cycle_sync_deduction",
            farmId = "farm_1",
            totalBirdsPlaced = 8000,
            feedInventoryKg = 3500.0,
            cumulativeBirdsSold = 500,
            cumulativeSoldWeightKg = 1150.0
        )
        val staleCloudCycleBeforeConsumption = CycleModel(
            id = "cycle_sync_deduction",
            farmId = "farm_1",
            totalBirdsPlaced = 8000,
            feedInventoryKg = 4000.0,
            cumulativeBirdsSold = 0,
            cumulativeSoldWeightKg = 0.0
        )
        val mergedAfterDeduction = AgritechMockDb.mergeCloudCycleWithLocal(
            cloudCycle = staleCloudCycleBeforeConsumption,
            localCycle = localCycleAfterConsumption
        )
        assertEquals(3500.0, mergedAfterDeduction.feedInventoryKg, 0.001)
        assertEquals(500, mergedAfterDeduction.cumulativeBirdsSold)
        assertEquals(1150.0, mergedAfterDeduction.cumulativeSoldWeightKg, 0.001)
    }

    @Test
    fun freemiumPaywall_and_chatInvoiceReturnModels_workCorrectly() {
        // 0 existing barns -> free (maxAllowedBarns = 1 + 0 = 1)
        assertFalse(shouldInterceptNewBarnWithPaywall(existingOwnedCount = 0, unlockedAdditionalBarnsCount = 0))
        // 1 existing barn, 0 unlocked -> triggers paywall
        assertTrue(shouldInterceptNewBarnWithPaywall(existingOwnedCount = 1, unlockedAdditionalBarnsCount = 0))
        // 1 existing barn, 1 unlocked via subscription -> allowed without re-triggering paywall!
        assertFalse(shouldInterceptNewBarnWithPaywall(existingOwnedCount = 1, unlockedAdditionalBarnsCount = 1))

        // Verify recordVetInvoiceAndPrescription and recordUniversalFinancialRequest return the ChatMessageModel for cloud sync
        val vetTx = TransactionModel(
            id = "tx_unit_vet_1",
            farmId = "farm_1",
            cycleId = "cycle_1",
            type = TransactionType.EXPENSE,
            category = "MEDICATION",
            amount = 12000.0,
            currency = "DZD",
            date = "اليوم 6"
        )
        val vetNote = VetNoteModel(
            id = "vn_unit_vet_1",
            farmId = "farm_1",
            cycleId = "cycle_1",
            dayNumber = 6,
            medicineName = "تيلوزين",
            dosage = "0.5 غ / لتر",
            note = "علاج تنفسي",
            isUrgent = true,
            withdrawalDays = 5
        )
        val vetChatMsg = AgritechMockDb.recordVetInvoiceAndPrescription(vetTx, vetNote, "cycle_1_vet")
        assertEquals("msg_vet_inv_tx_unit_vet_1", vetChatMsg.id)
        assertEquals("tx_unit_vet_1", vetChatMsg.linkedTransactionId)
        assertEquals(12000.0, vetChatMsg.requestAmount, 0.001)
    }
}
