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

    @Test
    fun futureProofUnitModel_and_smartBarnLocalization_workCorrectly() {
        val defaultUnit = FutureProofUnitModel()
        assertEquals(2, defaultUnit.schemaVersion)
        assertEquals("AGRICULTURE", defaultUnit.sectorId)
        assertEquals("BROILER", defaultUnit.branchId)
        assertTrue(defaultUnit.customMetrics.isEmpty())

        val customCycle = CycleModel(
            id = "cycle_fp_1",
            farmId = "farm_fp_1",
            totalBirdsPlaced = 5000,
            customMetrics = mapOf("targetFcr" to "1.55")
        )
        assertEquals(2, customCycle.schemaVersion)
        assertEquals("AGRICULTURE", customCycle.sectorId)
        assertEquals("BROILER", customCycle.branchId)
        assertEquals("1.55", customCycle.customMetrics["targetFcr"])

        assertTrue(Translations.hasKey("smart_barn_iot_title"))
        assertTrue(Translations.hasKey("smart_barn_mode"))
        assertTrue(Translations.hasKey("smart_barn_sensor"))
        assertTrue(Translations.hasKey("smart_barn_tap_nh3_hint"))
        assertTrue(Translations.hasKey("smart_barn_analyze_btn"))

        assertFalse(Translations.tr("tx_feed", AppLanguage.AR).contains("25"))
        assertFalse(Translations.tr("tx_feed", AppLanguage.EN).contains("25"))
        assertFalse(Translations.tr("tx_feed", AppLanguage.PT).contains("25"))
    }

    @Test
    fun modularSectorEngines_calculateAndValidateCorrectly() {
        // 1. Input validation & sanitization in BusinessNotebookEngine
        assertTrue(BusinessNotebookEngine.validatePositiveAmount(150.0))
        assertFalse(BusinessNotebookEngine.validatePositiveAmount(0.0))
        assertFalse(BusinessNotebookEngine.validatePositiveAmount(-20.0))
        assertEquals(42.5, BusinessNotebookEngine.sanitizeNumericInput(" 42,5 "), 0.001)
        assertEquals(0.0, BusinessNotebookEngine.sanitizeNumericInput("-99"), 0.001)
        assertEquals(0.0, BusinessNotebookEngine.sanitizeNumericInput("abc"), 0.001)

        // 2. LivestockAndPoultryEngine (Quintals = 100kg, FCR, EPEF, mortality, daily yield)
        assertEquals(2500.0, LivestockAndPoultryEngine.quintalsToKg(25.0), 0.001)
        assertEquals(0.0, LivestockAndPoultryEngine.quintalsToKg(-5.0), 0.001)
        val flockMetrics = LivestockAndPoultryEngine.evaluateFlockOrHerdMetrics(
            initialHeads = 1000,
            cumulativeMortality = 50,
            culledOrSoldHeads = 0,
            totalFeedQuintals = 38.0, // 3,800 kg
            averageLiveWeightKg = 2.0, // 950 * 2.0 = 1,900 kg biomass -> FCR = 2.0
            ageDays = 38
        )
        assertEquals(950.0, flockMetrics["remainingLiveHeads"] ?: 0.0, 0.001)
        assertEquals(5.0, flockMetrics["mortalityPct"] ?: 0.0, 0.001)
        assertEquals(2.0, flockMetrics["fcr"] ?: 0.0, 0.001)
        assertTrue((flockMetrics["epef"] ?: 0.0) > 200.0)

        val yieldMetrics = LivestockAndPoultryEngine.calculateDailyYieldMetrics(
            producingUnits = 500,
            dailyOutputQuantity = 450.0,
            dailyFeedAndCareExpense = 9000.0
        )
        assertEquals(90.0, yieldMetrics["productionRatePct"] ?: 0.0, 0.001)
        assertEquals(20.0, yieldMetrics["costPerOutputUnit"] ?: 0.0, 0.001)

        // 3. PlantAndOrchardEngine calculations & negative input protection
        val orchardSummary = PlantAndOrchardEngine.evaluateCropOrOrchardSeason(
            areaHectaresOrTreesCount = 5.0,
            harvestedQuintals = 200.0,
            sellingPricePerQuintal = 6000.0, // 1,200,000 income
            seedAndSaplingExpense = 100000.0,
            fertilizerAndPesticideExpense = 150000.0,
            irrigationAndLaborExpense = 150000.0 // 400,000 total expenses
        )
        assertEquals(1200000.0, orchardSummary.totalIncome, 0.001)
        assertEquals(400000.0, orchardSummary.totalExpenses, 0.001)
        assertEquals(800000.0, orchardSummary.netProfit, 0.001)
        assertTrue(orchardSummary.isProfitable)
        assertEquals(40.0, orchardSummary.customKpis["yieldQuintalsPerAreaUnit"] ?: 0.0, 0.001)
        assertEquals(2000.0, orchardSummary.unitCost, 0.001)

        // 4. CommercialAndFreelanceEngine calculations & ROI
        val commercialSummary = CommercialAndFreelanceEngine.evaluateCommercialOrFreelancePeriod(
            branchId = BusinessNotebookEngine.BRANCH_RETAIL_WHOLESALE,
            grossSalesAndServiceIncome = 500000.0,
            merchandiseOrMaterialExpense = 300000.0,
            operatingAndOverheadExpense = 50000.0,
            completedOrdersOrProjectsCount = 100
        )
        assertEquals(500000.0, commercialSummary.totalIncome, 0.001)
        assertEquals(350000.0, commercialSummary.totalExpenses, 0.001)
        assertEquals(150000.0, commercialSummary.netProfit, 0.001)
        assertEquals(200000.0, commercialSummary.customKpis["grossProfit"] ?: 0.0, 0.001)
        assertEquals(3500.0, commercialSummary.unitCost, 0.001)
    }

    @Test
    fun resetCycleAndClearBarn_clearsDailyReportsTransactionsVetNotesAndResetsCycleToDayOne() {
        val testFarmId = "farm_reset_test"
        val testCycleId = "cycle_reset_test"
        AgritechMockDb.cycles.removeAll { it.id == testCycleId }
        AgritechMockDb.dailyReports.removeAll { it.farmId == testFarmId }
        AgritechMockDb.transactions.removeAll { it.farmId == testFarmId }
        AgritechMockDb.vetNotes.removeAll { it.farmId == testFarmId }

        AgritechMockDb.cycles.add(
            CycleModel(
                id = testCycleId,
                farmId = testFarmId,
                startDate = "2026-01-01",
                totalBirdsPlaced = 5000,
                feedInventoryKg = 1200.0,
                cumulativeBirdsSold = 400,
                cumulativeSoldWeightKg = 920.0,
                status = "active"
            )
        )
        AgritechMockDb.dailyReports.add(
            DailyReportModel(
                id = "r_test_1",
                farmId = testFarmId,
                cycleId = testCycleId,
                dayNumber = 15,
                feedIntakeKg = 300.0,
                waterIntakeLiters = 550.0,
                mortalityCount = 12,
                temperature = 28.5
            )
        )
        AgritechMockDb.transactions.add(
            TransactionModel(
                id = "tx_test_1",
                farmId = testFarmId,
                cycleId = testCycleId,
                type = TransactionType.EXPENSE,
                category = "علف",
                amount = 240000.0,
                date = "اليوم 15"
            )
        )
        AgritechMockDb.vetNotes.add(
            VetNoteModel(
                id = "vn_test_1",
                farmId = testFarmId,
                cycleId = testCycleId,
                dayNumber = 15,
                medicineName = "Enrofloxacin",
                dosage = "1 ml/L",
                note = "Test note",
                isUrgent = false,
                withdrawalDays = 5
            )
        )

        val resetCycle = AgritechMockDb.resetCycleAndClearBarn(testFarmId, testCycleId)
        assertTrue(resetCycle != null)
        assertEquals(java.time.LocalDate.now().toString(), resetCycle!!.startDate)
        assertEquals(0.0, resetCycle.feedInventoryKg, 0.001)
        assertEquals(0, resetCycle.cumulativeBirdsSold)
        assertEquals(0.0, resetCycle.cumulativeSoldWeightKg, 0.001)
        assertTrue(AgritechMockDb.dailyReports.none { it.farmId == testFarmId })
        assertTrue(AgritechMockDb.transactions.none { it.farmId == testFarmId })
        assertTrue(AgritechMockDb.vetNotes.none { it.farmId == testFarmId })

        // Verify new localization keys
        assertEquals("🔄 إعادة الدورة", Translations.tr("btn_reset_cycle", AppLanguage.AR))
        assertEquals("إجمالي النفوق", Translations.tr("sum_mortality", AppLanguage.AR))
        assertEquals("قيد القياس", Translations.tr("weight_not_measured_yet", AppLanguage.AR))
        assertEquals("فتح الدفتر", Translations.tr("open_notebook", AppLanguage.AR))
        assertEquals("💰 تقدير المبيعات وصافي ربح الدورة", Translations.tr("sales_profit_simulator_title", AppLanguage.AR))

        // Verify Sales & Net Profit Simulator formula with cull bird deduction (5,786 - 286 = 5,500 * 2.8 = 15,400 kg * 250 = 3,850,000 - 2,940,000 = +910,000)
        val liveBirds = 5786
        val cullBirds = 286
        val marketableBirds = (liveBirds - cullBirds).coerceAtLeast(0)
        val estWeightKg = 2.8
        val expectedPricePerKg = 250.0
        val actualExpenses = 2940000.0
        val expectedTotalWeightKg = marketableBirds * estWeightKg
        val expectedRevenue = expectedTotalWeightKg * expectedPricePerKg
        val estimatedNetProfit = expectedRevenue - actualExpenses
        assertEquals(5500, marketableBirds)
        assertEquals(15400.0, expectedTotalWeightKg, 0.001)
        assertEquals(3850000.0, expectedRevenue, 0.001)
        assertEquals(910000.0, estimatedNetProfit, 0.001)
    }
}

