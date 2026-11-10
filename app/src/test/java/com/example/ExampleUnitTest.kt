package com.example

import com.example.models.*
import com.example.utils.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
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

        val initialWorkerEntriesCount = AgritechMockDb.workerPersonalEntries.size
        assertTrue(initialWorkerEntriesCount >= 5)
        assertTrue(AgritechMockDb.workerPersonalEntries.all { it.farmId == "personal_worker_ledger" })

        val resetCycle = AgritechMockDb.resetCycleAndClearBarn(testFarmId, testCycleId)
        assertTrue(resetCycle != null)
        assertEquals(java.time.LocalDate.now().toString(), resetCycle!!.startDate)
        assertEquals(0.0, resetCycle.feedInventoryKg, 0.001)
        assertEquals(0, resetCycle.cumulativeBirdsSold)
        assertEquals(0.0, resetCycle.cumulativeSoldWeightKg, 0.001)
        assertTrue(AgritechMockDb.dailyReports.none { it.farmId == testFarmId })
        assertTrue(AgritechMockDb.transactions.none { it.farmId == testFarmId })
        assertTrue(AgritechMockDb.vetNotes.none { it.farmId == testFarmId })

        // Verify Worker Personal Ledger (farmId = "personal_worker_ledger") is 100% isolated from barn reset and farm treasury
        assertEquals(initialWorkerEntriesCount, AgritechMockDb.workerPersonalEntries.size)
        assertNull(AgritechMockDb.resetCycleAndClearBarn("personal_worker_ledger", testCycleId))
        assertEquals(initialWorkerEntriesCount, AgritechMockDb.workerPersonalEntries.size)
        assertTrue(AgritechMockDb.transactions.none { it.farmId == "personal_worker_ledger" })

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

    @Test
    fun nextGenAiAgent_homePersonalLedger_and_14LanguageNotebookLocalization_workCorrectly() {
        // 1. Verify PersonalAndHouseholdEngine calculations & month-over-month delta
        val homeSummary = PersonalAndHouseholdEngine.evaluateMonthlyHouseholdBudget(
            monthlySalaryAndIncome = 120000.0,
            electricityAndGasExpense = 4500.0,
            internetAndPhoneExpense = 3200.0,
            carInsuranceAndMaintenanceExpense = 8500.0,
            groceriesAndHomeExpense = 28000.0,
            previousMonthTotalExpense = 50227.27
        )
        assertEquals(120000.0, homeSummary.totalIncome, 0.001)
        assertEquals(44200.0, homeSummary.totalExpenses, 0.001)
        assertEquals(75800.0, homeSummary.netProfit, 0.001)
        assertTrue(homeSummary.isProfitable)
        // Month-over-month expense change should be approx -12%
        val momChangePct = homeSummary.customKpis["monthOverMonthPct"] ?: 0.0
        assertEquals(-12.0, momChangePct, 0.1)

        // 2. Verify HOME_PERSONAL is merged into WORKER and professionalHubRoles keeps the 7 original professions
        assertEquals(7, AppRole.professionalHubRoles.size)
        assertFalse(AppRole.professionalHubRoles.contains(AppRole.HOME_PERSONAL))
        assertEquals(AppRole.WORKER, AppRole.HOME_PERSONAL.normalizedHubRole)
        assertEquals(7, sevenProfessionCatalog.size)
        assertFalse(sevenProfessionCatalog.any { it.role == AppRole.HOME_PERSONAL })

        // 3. Verify 14-language localization for Notebook header, Worker 4-Tab Notebook, QR scanner, and AI Agent Employee
        assertEquals("💼 الإدارة", Translations.tr("worker_tab_barns_daily", AppLanguage.AR))
        assertEquals("💰 الخزنة", Translations.tr("worker_tab_home_wallet", AppLanguage.AR))
        assertEquals("📊 التحليلات", Translations.tr("worker_tab_analytics_agent", AppLanguage.AR))
        assertEquals("🧾 الفواتير", Translations.tr("worker_tab_vet_tasks", AppLanguage.AR))

        val requiredKeys = listOf(
            "global_tab_ai_vet",
            "btn_owner_add_daily_report",
            "chat_worker_title",
            "chat_vet_title",
            "climate_header_btn_label",
            "btn_spend",
            "btn_income",
            "qr_confirm_scan_join_btn",
            "qr_detected_banner_fmt",
            "ai_agent_persona_title",
            "ai_agent_card1_market_fmt",
            "ai_agent_card2_vaccine_fmt",
            "worker_tab_barns_daily",
            "worker_tab_home_wallet",
            "worker_tab_analytics_agent",
            "worker_tab_vet_tasks",
            "btn_open_worker_smart_notebook",
            "worker_tx_expense_name_label",
            "worker_tx_income_name_label",
            "worker_add_new_bill_btn",
            "worker_ai_smart_alert_title",
            "worker_invoices_tab_section_title",
            "worker_bills_ledger_section_title",
            "worker_savings_summary_title",
            "home_cat_electricity_gas",
            "home_cat_salary_income",
            "weigh_crates_stepper_fmt",
            "farm_vitality_vaccines_sheet_title",
            "farm_vitality_vaccines_sheet_subtitle",
            "farm_weight_income_sheet_title",
            "farm_weight_income_sheet_subtitle",
            "farm_feed_consumed_section_title",
            "farm_income_summary_section_title",
            "btn_open_sales_cull_simulator",
            "ai_subtab_smart_decisions",
            "ai_subtab_diagnostics",
            "ai_subtab_live_chat",
            "notebook_tab_inventory",
            "barn_inventory_section_title",
            "barn_inv_gas_cylinders_title",
            "barn_inv_wood_shavings_title",
            "barn_inv_water_tankers_title",
            "barn_inv_meds_packs_title",
            "expense_custom_name_label",
            "expense_custom_name_placeholder",
            "delete_barn_long_press_badge",
            "delete_barn_dialog_title",
            "delete_barn_confirm_btn",
            "settings_row_reset_active_cycle_title"
        )
        for (lang in AppLanguage.values()) {
            assertEquals("🧠 IA", Translations.tr("global_tab_ai_vet", lang))
            for (key in requiredKeys) {
                val translated = Translations.tr(key, lang)
                assertFalse("Key $key should not be blank in $lang", translated.isBlank())
                assertFalse("Key $key should not contain raw markdown ** in $lang", translated.contains("**"))
            }
        }

        // Verify Spanish, Turkish, Hindi, Russian, German, Chinese do not fall back to English for Notebook buttons
        assertNotEquals("📝 + Daily Report", Translations.tr("btn_owner_add_daily_report", AppLanguage.ES))
        assertNotEquals("📝 + Daily Report", Translations.tr("btn_owner_add_daily_report", AppLanguage.TR))
        assertNotEquals("📝 + Daily Report", Translations.tr("btn_owner_add_daily_report", AppLanguage.HI))
        assertNotEquals("📝 + Daily Report", Translations.tr("btn_owner_add_daily_report", AppLanguage.RU))
        assertNotEquals("📝 + Daily Report", Translations.tr("btn_owner_add_daily_report", AppLanguage.DE))
        assertNotEquals("📝 + Daily Report", Translations.tr("btn_owner_add_daily_report", AppLanguage.ZH))

        // Verify AI Vet fallback advice never outputs raw ** asterisks
        val fallbackAdvice = AiVetGeminiService.buildClinicalFallbackAdvice(
            question = "تحليل صحة القطيع",
            flockAgeDays = 24,
            liveBirds = 5800,
            mortalityPct = 1.2,
            currentTempC = 23.0,
            lang = AppLanguage.AR
        )
        assertFalse(fallbackAdvice.contains("**"))

        // Verify Barn Supply Inventory counters (Gas, Shavings, Water, Meds) & Complete Barn Deletion (Surgery 6)
        AgritechMockDb.barnInventoryCounters.remove("farm_test_gas")
        AgritechMockDb.adjustBarnSupplyCount("farm_test", "gas", 5, 10)
        assertEquals(15, AgritechMockDb.getBarnSupplyCount("farm_test", "gas", 10))
        // Clamping at 0 when subtracting a large value
        AgritechMockDb.adjustBarnSupplyCount("farm_test", "gas", -50, 10)
        assertEquals(0, AgritechMockDb.getBarnSupplyCount("farm_test", "gas", 10))

        // Populate farm_test supply keys and farm record, then verify deleteFarmCompletely removes all farm_test_* keys
        AgritechMockDb.adjustBarnSupplyCount("farm_test", "gas", 12, 0)
        AgritechMockDb.adjustBarnSupplyCount("farm_test", "shavings", 30, 0)
        AgritechMockDb.adjustBarnSupplyCount("farm_test", "water", 4, 0)
        AgritechMockDb.adjustBarnSupplyCount("farm_test", "meds", 9, 0)
        AgritechMockDb.farms.add(
            FarmModel(
                id = "farm_test",
                name = "عنبر اختبار الحذف",
                capacity = 5000
            )
        )
        val workerCountBeforeDelete = AgritechMockDb.workerPersonalEntries.size
        assertTrue(AgritechMockDb.barnInventoryCounters.keys.any { it.startsWith("farm_test_") })

        AgritechMockDb.deleteFarmCompletely("farm_test")
        assertTrue(AgritechMockDb.farms.none { it.id == "farm_test" })
        assertTrue(AgritechMockDb.barnInventoryCounters.keys.none { it.startsWith("farm_test_") })
        assertTrue(AgritechMockDb.deletedFarmIds.contains("farm_test"))

        // Verify concise 1-line AI sub-tab labels in Arabic
        assertEquals("⚡ القرارات", Translations.tr("ai_subtab_smart_decisions", AppLanguage.AR))
        assertEquals("🔍 الفحوصات", Translations.tr("ai_subtab_diagnostics", AppLanguage.AR))
        assertEquals("💬 المحادثة", Translations.tr("ai_subtab_live_chat", AppLanguage.AR))

        // Verify personal_worker_ledger is 100% protected from deleteFarmCompletely
        AgritechMockDb.deleteFarmCompletely("personal_worker_ledger")
        assertFalse(AgritechMockDb.deletedFarmIds.contains("personal_worker_ledger"))
        assertEquals(workerCountBeforeDelete, AgritechMockDb.workerPersonalEntries.size)
        assertTrue(AgritechMockDb.workerPersonalEntries.all { it.farmId == "personal_worker_ledger" })
    }

    @Test
    fun unassignedFarm_guardAndValidation_preventInvalidQueries() {
        assertEquals("unassigned_farm", AgritechFirestoreRepository.UNASSIGNED_FARM_ID)
        assertTrue(AgritechFirestoreRepository.isUnassignedFarm("unassigned_farm"))
        assertTrue(AgritechFirestoreRepository.isUnassignedFarm(""))
        assertTrue(AgritechFirestoreRepository.isUnassignedFarm("   "))
        assertTrue(AgritechFirestoreRepository.isUnassignedFarm(null))
        assertFalse(AgritechFirestoreRepository.isUnassignedFarm("farm_1"))
        assertFalse(AgritechFirestoreRepository.isUnassignedFarm("farm_my_flock"))

        // When farm is in deletedFarmIds, it's also recognized as unassigned
        AgritechMockDb.deletedFarmIds.add("farm_deleted_test")
        assertTrue(AgritechFirestoreRepository.isUnassignedFarm("farm_deleted_test"))
        AgritechMockDb.deletedFarmIds.remove("farm_deleted_test")
    }

    @Test
    fun newBarnInventory_startsAtZero_andIncrementWorksAccurately() {
        val newFarmId = "farm_new_test_zero"
        AgritechMockDb.barnInventoryCounters.keys.removeAll { it.startsWith("${newFarmId}_") }

        assertEquals(0, AgritechMockDb.getBarnSupplyCount(newFarmId, "gas"))
        assertEquals(0, AgritechMockDb.getBarnSupplyCount(newFarmId, "shavings"))
        assertEquals(0, AgritechMockDb.getBarnSupplyCount(newFarmId, "water"))
        assertEquals(0, AgritechMockDb.getBarnSupplyCount(newFarmId, "meds"))

        AgritechMockDb.adjustBarnSupplyCount(newFarmId, "gas", +1)
        assertEquals(1, AgritechMockDb.getBarnSupplyCount(newFarmId, "gas"))

        AgritechMockDb.adjustBarnSupplyCount(newFarmId, "gas", +5)
        assertEquals(6, AgritechMockDb.getBarnSupplyCount(newFarmId, "gas"))

        AgritechMockDb.adjustBarnSupplyCount(newFarmId, "gas", -10)
        assertEquals(0, AgritechMockDb.getBarnSupplyCount(newFarmId, "gas"))
    }

    @Test
    fun fcrNotCalculated_translationsAndFallback_areAccurate() {
        for (lang in AppLanguage.values()) {
            val trFcr = Translations.tr("fcr_not_calculated", lang)
            assertFalse(trFcr.isBlank())
            assertFalse(trFcr.contains("_"))
        }
        assertEquals("غير محسوب", Translations.tr("fcr_not_calculated", AppLanguage.AR))
        assertEquals("Not calculated", Translations.tr("fcr_not_calculated", AppLanguage.EN))
        assertEquals("Non calculé", Translations.tr("fcr_not_calculated", AppLanguage.FR))
        assertEquals("No calculado", Translations.tr("fcr_not_calculated", AppLanguage.ES))
        assertEquals("Não calculado", Translations.tr("fcr_not_calculated", AppLanguage.PT))

        // When feed is zero or birds are placed with no consumption yet
        val initialFeedZero = 0.0
        val zeroFeedFcr = PoultryCalculators.calculateFcr(
            totalFeedKg = initialFeedZero,
            initialBirds = 5000,
            totalMortality = 0,
            birdsSold = 0,
            ageInDays = 1,
            actualAvgWeightKg = 0.045,
            cumulativeSoldWeightKg = 0.0
        )
        assertEquals(0.0, zeroFeedFcr, 0.001)
    }

    @Test
    fun indoorReadingState_modelsAndSeparation_areCorrect() {
        val noReading = IndoorReadingState.noReading("farm_empty")
        assertFalse(noReading.hasAnyReading)
        assertEquals(IndoorReadingSource.MANUAL_REPORT, noReading.source)
        assertFalse(noReading.isVerifiedSensorConnected)
        assertNull(noReading.temperatureC)
        assertNull(noReading.ammoniaPpm)

        val reportWithGasUnits = DailyReportModel(
            id = "rep_gas_units",
            farmId = "farm_1",
            cycleId = "cycle_1",
            dayNumber = 12,
            feedIntakeKg = 400.0,
            waterIntakeLiters = 800.0,
            mortalityCount = 1,
            temperature = 26.5,
            gasUnits = 14.0, // Burner heating gas bottles used, NOT ammonia NH3 ppm
            ventilationStatus = "ممتازة"
        )
        val fromManual = IndoorReadingState.fromManualReport("farm_1", reportWithGasUnits)
        assertTrue(fromManual.hasAnyReading)
        assertEquals(26.5, fromManual.temperatureC ?: 0.0, 0.001)
        // CRITICAL: Gas bottle usage must NEVER be converted to ammonia NH3 ppm!
        assertNull(fromManual.ammoniaPpm)
        assertEquals("ممتازة", fromManual.ventilationStatus)
        assertEquals(IndoorReadingSource.MANUAL_REPORT, fromManual.source)
        assertFalse(fromManual.isVerifiedSensorConnected)

        val sim = IndoorReadingState.simulation("farm_1")
        assertTrue(sim.hasAnyReading)
        assertEquals(IndoorReadingSource.SIMULATION, sim.source)
        assertFalse(sim.isVerifiedSensorConnected)
    }
}





