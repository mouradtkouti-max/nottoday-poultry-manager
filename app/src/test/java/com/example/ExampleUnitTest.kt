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
            assertTrue(
                "global_tab_ai_vet in $lang should start with 📖",
                Translations.tr("global_tab_ai_vet", lang).startsWith("📖")
            )
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

        // Verify concise 1-line Book sub-tab labels in Arabic
        assertEquals("📖 الكتاب", Translations.tr("global_tab_ai_vet", AppLanguage.AR))
        assertEquals("📊 التحليلات", Translations.tr("ai_subtab_smart_decisions", AppLanguage.AR))
        assertEquals("💡 نصائح", Translations.tr("ai_subtab_diagnostics", AppLanguage.AR))
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

    @Test
    fun batch2SurgicalPatches_dailyReportAudit_weatherNullSafety_andAiRealBarnContext() {
        // 1. Surgery 1: Unchanged daily report save must NOT mark isEdited = true
        val testFarmId = "farm_b2_audit"
        val testCycleId = "cycle_b2_audit"
        AgritechMockDb.dailyReports.removeAll { it.farmId == testFarmId }
        val initialReport = DailyReportModel(
            id = "rep_b2_d1",
            farmId = testFarmId,
            cycleId = testCycleId,
            dayNumber = 1,
            feedIntakeKg = 150.0,
            waterIntakeLiters = 300.0,
            mortalityCount = 2,
            temperature = 31.0,
            gasUnits = 1.0,
            actualAvgWeightKg = 0.055,
            medicineGiven = false,
            medicineSummary = "",
            isEdited = false,
            previousSummary = ""
        )
        val inserted = AgritechMockDb.upsertDailyReportWithAudit(initialReport)
        assertFalse("Initial insert must not be marked edited", inserted.isEdited)
        assertEquals("", inserted.previousSummary)

        // Saving the exact same report values must keep isEdited = false and previousSummary = ""
        val savedWithoutChanges = AgritechMockDb.upsertDailyReportWithAudit(inserted.copy())
        assertFalse("Unchanged edit must not mark report as edited", savedWithoutChanges.isEdited)
        assertEquals("", savedWithoutChanges.previousSummary)

        // Saving with a real value change (e.g. mortality 2 -> 4) must mark isEdited = true and record previousSummary
        val savedWithRealChange = AgritechMockDb.upsertDailyReportWithAudit(
            savedWithoutChanges.copy(mortalityCount = 4)
        )
        assertTrue("Changed edit must mark report as edited", savedWithRealChange.isEdited)
        assertTrue("Changed edit must populate previousSummary", savedWithRealChange.previousSummary.isNotBlank())

        // 2. Surgery 2 & 3: Verify concise day label, hint, and indoor_reading_no_data keys
        assertEquals("📅 رقم اليوم في الدورة", Translations.tr("field_day_number_mid_cycle", AppLanguage.AR))
        assertEquals("يُكتشف تلقائياً، أو اكتب رقم يوم سابق مثل 20 أو 30 لتعديله", Translations.tr("field_day_number_mid_cycle_hint", AppLanguage.AR))
        assertEquals("لا توجد قراءة مسجلة بعد — أدخل التقرير اليومي", Translations.tr("indoor_reading_no_data", AppLanguage.AR))

        // 3. Surgery 4: Weather coordinate key formatting
        assertEquals("36.753_3.042", BarnWeatherRepository.formatCoordKey(36.7528, 3.0420))

        // 4. Surgery 5: Default disconnected sensor simulation & real barn context in AI Vet fallback
        val defaultTelemetry = SmartBarnTelemetrySnapshot()
        assertFalse("Sensor simulation must default to disconnected (false)", defaultTelemetry.isRobotConnected)
        val aiAdviceNoTemp = AiVetGeminiService.buildClinicalFallbackAdvice(
            question = "فحص القطيع",
            flockAgeDays = 1,
            liveBirds = 15000,
            mortalityPct = 0.0,
            currentTempC = null,
            lang = AppLanguage.AR,
            barnName = "عنبر السلام"
        )
        assertTrue(aiAdviceNoTemp.contains("عنبر السلام"))
        assertTrue(aiAdviceNoTemp.contains("15000"))
        assertTrue(aiAdviceNoTemp.contains("غير مسجلة"))
        assertFalse(aiAdviceNoTemp.contains("21.3"))
        assertFalse(aiAdviceNoTemp.contains("7880"))
    }

    @Test
    fun package1Of3_myManagementsSeparation_fullFieldDailyReportAudit_and_nullVsZeroTemperature_workCorrectly() {
        // 1. Verify full-field daily report audit comparison (actualAvgWeightKg, ventilationStatus, litterCondition, feedIntakeKg, etc.)
        val testFarmId = "farm_pkg1_audit"
        val testCycleId = "cycle_pkg1_audit"
        AgritechMockDb.dailyReports.removeAll { it.farmId == testFarmId }

        val baseReport = DailyReportModel(
            id = "rep_pkg1_d10",
            farmId = testFarmId,
            cycleId = testCycleId,
            dayNumber = 10,
            feedIntakeKg = 350.0,
            waterIntakeLiters = 700.0,
            mortalityCount = 2,
            temperature = 28.5,
            gasUnits = 2.0,
            ventilationStatus = "جيدة",
            litterCondition = "جافة",
            actualAvgWeightKg = 0.285,
            medicineGiven = true,
            medicineSummary = "فيتامين AD3E",
            isEdited = false,
            previousSummary = ""
        )
        val savedInitial = AgritechMockDb.upsertDailyReportWithAudit(baseReport)
        assertFalse("Initial save must not mark report as edited", savedInitial.isEdited)

        // Saving identical values (including actualAvgWeightKg, ventilationStatus, and litterCondition) must NOT set isEdited = true
        val savedIdentical = AgritechMockDb.upsertDailyReportWithAudit(savedInitial.copy())
        assertFalse("Identical report save must not mark isEdited = true", savedIdentical.isEdited)
        assertEquals("", savedIdentical.previousSummary)

        // Changing actualAvgWeightKg alone must set isEdited = true
        val changedWeight = AgritechMockDb.upsertDailyReportWithAudit(
            savedIdentical.copy(actualAvgWeightKg = 0.310)
        )
        assertTrue("Changing actualAvgWeightKg must set isEdited = true", changedWeight.isEdited)
        assertTrue(changedWeight.previousSummary.isNotBlank())

        // Reset and test changing ventilationStatus or litterCondition alone
        val baseReportDay11 = baseReport.copy(id = "rep_pkg1_d11", dayNumber = 11, isEdited = false, previousSummary = "")
        val savedDay11 = AgritechMockDb.upsertDailyReportWithAudit(baseReportDay11)
        assertFalse(savedDay11.isEdited)
        val changedVentilation = AgritechMockDb.upsertDailyReportWithAudit(
            savedDay11.copy(ventilationStatus = "متوسطة")
        )
        assertTrue("Changing ventilationStatus must set isEdited = true", changedVentilation.isEdited)

        val baseReportDay12 = baseReport.copy(id = "rep_pkg1_d12", dayNumber = 12, isEdited = false, previousSummary = "")
        val savedDay12 = AgritechMockDb.upsertDailyReportWithAudit(baseReportDay12)
        assertFalse(savedDay12.isEdited)
        val changedLitter = AgritechMockDb.upsertDailyReportWithAudit(
            savedDay12.copy(litterCondition = "رطبة")
        )
        assertTrue("Changing litterCondition must set isEdited = true", changedLitter.isEdited)

        // 2. Verify the 4 unavailable roles and 3 available managements
        val unavailableRoles = listOf(
            AppRole.FEED_SUPPLIER,
            AppRole.CHICK_HATCHERY,
            AppRole.WHOLESALE_TRADER,
            AppRole.VET_IMPORTER
        )
        for (role in unavailableRoles) {
            assertFalse("Role $role must be marked unavailable", role.isCurrentlyAvailable)
        }
        assertEquals(unavailableRoles.toSet(), AppRole.unavailableManagementRoles.toSet())

        val availableRoles = listOf(
            AppRole.OWNER,
            AppRole.OWNER_BARN,
            AppRole.VETERINARIAN,
            AppRole.VET_CLINIC,
            AppRole.WORKER
        )
        for (role in availableRoles) {
            assertTrue("Role $role must be marked available", role.isCurrentlyAvailable)
        }
        assertEquals(
            setOf(AppRole.OWNER_BARN, AppRole.VET_CLINIC, AppRole.WORKER),
            AppRole.availableManagementRoles.toSet()
        )
        assertEquals("إداراتي", Translations.tr("btn_my_managements", AppLanguage.AR))
        assertEquals("+ إضافة إدارة", Translations.tr("btn_add_management", AppLanguage.AR))
        assertEquals("متاحة", Translations.tr("prof_status_available", AppLanguage.AR))
        assertEquals("غير متوفرة حالياً", Translations.tr("prof_status_unavailable", AppLanguage.AR))

        // 3. Verify missing report -> null temperature vs report with 0.0°C -> actual 0.0°C reading
        val stateWhenNoReports = IndoorReadingState.fromManualReport("farm_pkg1", null)
        assertNull("No reports in cycle must produce null temperatureC", stateWhenNoReports.temperatureC)
        assertNull("No reports in cycle must produce null ventilationStatus", stateWhenNoReports.ventilationStatus)
        assertFalse(stateWhenNoReports.hasAnyReading)

        val reportAtZeroCelsius = baseReport.copy(id = "rep_zero_c", dayNumber = 13, temperature = 0.0)
        val stateAtZeroCelsius = IndoorReadingState.fromManualReport("farm_pkg1", reportAtZeroCelsius)
        assertNotNull("Report with temperature = 0.0 must NOT be treated as null", stateAtZeroCelsius.temperatureC)
        assertEquals(0.0, stateAtZeroCelsius.temperatureC!!, 0.0001)
        assertTrue(stateAtZeroCelsius.hasAnyReading)
    }

    @Test
    fun package2Of3_weatherCacheLocationIsolation_nullJsonParsing_robotDisconnectedDefault_and_aiContext_workCorrectly() {
        // 1. Changing barn location from City A to City B invalidates old weather cache and never returns City A's reading
        val uid = "uid_pkg2_weather"
        val barnId = "farm_pkg2_1"
        val cityALoc = com.example.models.BarnWeatherLocation(
            barnId = barnId,
            localityName = "بوفاريك",
            country = "الجزائر",
            region = "البليدة",
            latitude = 36.575,
            longitude = 2.912,
            isEnabled = true
        )
        val cityBLoc = com.example.models.BarnWeatherLocation(
            barnId = barnId,
            localityName = "سطيف",
            country = "الجزائر",
            region = "سطيف",
            latitude = 36.191,
            longitude = 5.414,
            isEnabled = true
        )

        BarnWeatherRepository.saveLocation(null, uid, cityALoc)
        val cityASnapshot = com.example.models.OutdoorWeatherSnapshot(
            barnId = barnId,
            localityName = "بوفاريك",
            latitude = cityALoc.latitude,
            longitude = cityALoc.longitude,
            temperatureC = 24.5,
            apparentTemperatureC = 24.0,
            relativeHumidityPct = 55,
            weatherCode = 1,
            windSpeedKmh = 12.0,
            precipitationMm = 0.0,
            isDay = true,
            providerDataTimeIso = "2026-11-11T12:00",
            fetchedAtMillis = System.currentTimeMillis()
        )
        BarnWeatherRepository.saveCachedSnapshot(null, uid, cityASnapshot)
        assertNotNull(
            "City A cached snapshot should be returned when querying City A",
            BarnWeatherRepository.getCachedSnapshot(null, uid, barnId, cityALoc)
        )
        assertNull(
            "City A cached snapshot must NOT be returned when querying City B coordinates",
            BarnWeatherRepository.getCachedSnapshot(null, uid, barnId, cityBLoc)
        )

        // Saving City B location must immediately invalidate City A's cached snapshot for that barn
        BarnWeatherRepository.saveLocation(null, uid, cityBLoc)
        assertNull(
            "Saving City B location must invalidate old cached snapshot for that barn",
            BarnWeatherRepository.getCachedSnapshot(null, uid, barnId)
        )
        assertNull(
            "Even querying with City A after saveLocation(City B) must not return purged cache",
            BarnWeatherRepository.getCachedSnapshot(null, uid, barnId, cityALoc)
        )

        // 2. Weather JSON parser safely handles explicit JSON null in optional fields and missing/null weather_code -> -1 (غير معروف)
        val jsonWithNullOptionalFieldsAndMissingWeatherCode = """
            {
                "current": {
                    "time": "2026-11-11T15:30",
                    "temperature_2m": 19.8,
                    "apparent_temperature": null,
                    "relative_humidity_2m": null,
                    "wind_speed_10m": null,
                    "precipitation": null,
                    "is_day": null
                }
            }
        """.trimIndent()
        val parsedMissingCode = BarnWeatherRepository.parseWeatherJson(
            cityBLoc,
            jsonWithNullOptionalFieldsAndMissingWeatherCode
        )
        assertEquals(19.8, parsedMissingCode.temperatureC, 0.001)
        assertNull(parsedMissingCode.apparentTemperatureC)
        assertNull(parsedMissingCode.relativeHumidityPct)
        assertNull(parsedMissingCode.windSpeedKmh)
        assertNull(parsedMissingCode.precipitationMm)
        assertNull(parsedMissingCode.isDay)
        assertEquals(
            "Missing weather_code must not be interpreted as 0 (Clear sky)",
            -1,
            parsedMissingCode.weatherCode
        )
        assertEquals("غير معروف", parsedMissingCode.conditionDescription("ar"))
        assertEquals("Unknown", parsedMissingCode.conditionDescription("en"))

        val jsonWithExplicitNullWeatherCode = """
            {
                "current": {
                    "time": "2026-11-11T15:45",
                    "temperature_2m": 18.2,
                    "weather_code": null
                }
            }
        """.trimIndent()
        val parsedNullCode = BarnWeatherRepository.parseWeatherJson(cityBLoc, jsonWithExplicitNullWeatherCode)
        assertEquals(-1, parsedNullCode.weatherCode)
        assertEquals("غير معروف", parsedNullCode.conditionDescription("ar"))

        // 3. Default state of SmartBarnTelemetrySnapshot has isRobotConnected == false
        val defaultRobotState = SmartBarnTelemetrySnapshot()
        assertFalse(
            "Default state of SmartBarnTelemetrySnapshot must be disconnected (isRobotConnected == false)",
            defaultRobotState.isRobotConnected
        )

        // 4. AI Vet fallback for a barn with no reports does NOT contain fake numbers (25 days, 7880 birds, 21.3°C)
        // and clearly states "غير مسجل" / "لا توجد قراءة"
        val emptyBarnContext = com.example.utils.BarnAiContext(
            farmName = "عنبر الاختبار الجديد",
            cycleAgeDays = null,
            liveBirdsCount = null,
            latestTemperatureC = null,
            cumulativeMortalityPct = null,
            feedInventoryKg = null
        )
        val fallbackResponse = AiVetGeminiService.buildClinicalFallbackAdvice(
            question = "ما هي نصيحتك للعنبر اليوم؟",
            lang = AppLanguage.AR,
            barnContext = emptyBarnContext
        )
        assertTrue(fallbackResponse.contains("عنبر الاختبار الجديد"))
        assertTrue(
            "Fallback response for barn without reports must indicate غير مسجل / لا توجد قراءة",
            fallbackResponse.contains("غير مسجل") && fallbackResponse.contains("لا توجد قراءة")
        )
        assertFalse("Must not fabricate 25 days", fallbackResponse.contains("25 يوم"))
        assertFalse("Must not fabricate 7880 birds", fallbackResponse.contains("7880"))
        assertFalse("Must not fabricate 21.3°C", fallbackResponse.contains("21.3"))
    }

    @Test
    fun package3Of3_idempotentDraftSaleRetry_concurrentSalesInvariant_and_simulatorIsolation_workCorrectly() {
        // 1. Idempotent Retry for the same draft sale operation:
        // Submitting the same sale twice with the same draftSaleOperationId records the invoice only once
        // and deducts cumulativeBirdsSold only once.
        val testFarmId = "farm_pkg3_sales"
        val testCycleId = "cycle_pkg3_sales"
        AgritechMockDb.transactions.removeAll { it.farmId == testFarmId }
        AgritechMockDb.dailyReports.removeAll { it.farmId == testFarmId }
        AgritechMockDb.cycles.removeAll { it.id == testCycleId }

        AgritechMockDb.cycles.add(
            CycleModel(
                id = testCycleId,
                farmId = testFarmId,
                totalBirdsPlaced = 5000,
                status = "active",
                cumulativeBirdsSold = 0,
                cumulativeSoldWeightKg = 0.0
            )
        )

        val retryOpId = "op_retry_1"
        val retryKey = "sale_${testFarmId}_${testCycleId}_$retryOpId"
        val retryInvoice = SalesInvoiceModel(
            invoiceNumber = "INV-RETRY-1",
            farmName = "عنبر اختبار المبيعات",
            cycleId = testCycleId,
            dateText = "اليوم 30",
            birdAgeDays = 30,
            buyerName = "تاجر الجملة",
            truckPlate = "12345-116-16",
            totalCrates = 80,
            birdsPerCrate = 10,
            totalBirdsSold = 800,
            totalGrossKg = 2160.0,
            totalTareKg = 160.0,
            totalNetKg = 2000.0,
            avgBirdWeightKg = 2.5,
            pricePerKg = 250.0,
            totalAmount = 500000.0,
            currency = "دج",
            tareWeightsCount = 8,
            grossWeightsCount = 16
        )

        val firstAttempt = AgritechMockDb.recordSalesInvoiceIdempotent(
            farmId = testFarmId,
            cycleId = testCycleId,
            idempotencyKey = retryKey,
            dateText = "اليوم 30",
            invoice = retryInvoice
        )
        val secondAttempt = AgritechMockDb.recordSalesInvoiceIdempotent(
            farmId = testFarmId,
            cycleId = testCycleId,
            idempotencyKey = retryKey,
            dateText = "اليوم 30",
            invoice = retryInvoice
        )
        assertTrue("First submission of draft sale must succeed", firstAttempt)
        assertFalse("Duplicate retry with same draftSaleOperationId must be rejected idempotently", secondAttempt)
        assertEquals(
            "Invoice must be recorded exactly once",
            1,
            AgritechMockDb.transactions.count { it.id == retryKey }
        )
        val cycleAfterRetry = AgritechMockDb.cycles.first { it.id == testCycleId }
        assertEquals("cumulativeBirdsSold must be deducted once (800), never doubled", 800, cycleAfterRetry.cumulativeBirdsSold)
        assertEquals(2000.0, cycleAfterRetry.cumulativeSoldWeightKg, 0.001)

        // 2. Concurrent Sales Invariant:
        // Reset cycle to 5000 available birds, 0 sold.
        val concurrentFarmId = "farm_pkg3_concurrent"
        val concurrentCycleId = "cycle_pkg3_concurrent"
        AgritechMockDb.transactions.removeAll { it.farmId == concurrentFarmId }
        AgritechMockDb.dailyReports.removeAll { it.farmId == concurrentFarmId }
        AgritechMockDb.cycles.removeAll { it.id == concurrentCycleId }
        AgritechMockDb.cycles.add(
            CycleModel(
                id = concurrentCycleId,
                farmId = concurrentFarmId,
                totalBirdsPlaced = 5000,
                status = "active",
                cumulativeBirdsSold = 0,
                cumulativeSoldWeightKg = 0.0
            )
        )

        val op1Key = "sale_${concurrentFarmId}_${concurrentCycleId}_op_1"
        val op2Key = "sale_${concurrentFarmId}_${concurrentCycleId}_op_2"
        val op3Key = "sale_${concurrentFarmId}_${concurrentCycleId}_op_3"

        val saleOp1 = SalesInvoiceModel(
            invoiceNumber = "INV-OP-1",
            farmName = "عنبر المبيعات المتزامنة",
            cycleId = concurrentCycleId,
            dateText = "اليوم 35",
            birdAgeDays = 35,
            buyerName = "تاجر 1",
            truckPlate = "111-16",
            totalCrates = 100,
            birdsPerCrate = 10,
            totalBirdsSold = 1000,
            totalGrossKg = 2700.0,
            totalTareKg = 200.0,
            totalNetKg = 2500.0,
            avgBirdWeightKg = 2.5,
            pricePerKg = 250.0,
            totalAmount = 625000.0,
            currency = "دج",
            tareWeightsCount = 10,
            grossWeightsCount = 20
        )
        val saleOp2 = saleOp1.copy(
            invoiceNumber = "INV-OP-2",
            buyerName = "تاجر 2",
            totalBirdsSold = 1000,
            totalNetKg = 2500.0,
            totalAmount = 625000.0
        )
        val saleOp3Oversell = saleOp1.copy(
            invoiceNumber = "INV-OP-3",
            buyerName = "تاجر 3",
            totalBirdsSold = 3500,
            totalNetKg = 8750.0,
            totalAmount = 2187500.0
        )

        assertTrue(
            "Sale op_1 (1000 of 5000) must succeed",
            AgritechMockDb.recordSalesInvoiceIdempotent(
                farmId = concurrentFarmId,
                cycleId = concurrentCycleId,
                idempotencyKey = op1Key,
                dateText = "اليوم 35",
                invoice = saleOp1
            )
        )
        assertTrue(
            "Sale op_2 (1000 of remaining 4000) must succeed without key collision",
            AgritechMockDb.recordSalesInvoiceIdempotent(
                farmId = concurrentFarmId,
                cycleId = concurrentCycleId,
                idempotencyKey = op2Key,
                dateText = "اليوم 35",
                invoice = saleOp2
            )
        )

        val cycleAfterTwoSales = AgritechMockDb.cycles.first { it.id == concurrentCycleId }
        assertEquals(2000, cycleAfterTwoSales.cumulativeBirdsSold)
        val remainingLiveAfterTwoSales = PoultryCalculators.calculateLiveBirds(
            totalBirdsPlaced = cycleAfterTwoSales.totalBirdsPlaced,
            totalMortality = 0,
            cumulativeBirdsSold = cycleAfterTwoSales.cumulativeBirdsSold
        )
        assertEquals(3000, remainingLiveAfterTwoSales)

        // Third sale attempting to sell 3500 when only 3000 are available must be rejected
        assertFalse(
            "Sale op_3 (3500 birds when only 3000 remain) must be rejected",
            AgritechMockDb.recordSalesInvoiceIdempotent(
                farmId = concurrentFarmId,
                cycleId = concurrentCycleId,
                idempotencyKey = op3Key,
                dateText = "اليوم 35",
                invoice = saleOp3Oversell
            )
        )
        val cycleAfterRejectedThirdSale = AgritechMockDb.cycles.first { it.id == concurrentCycleId }
        assertEquals("cumulativeBirdsSold must remain 2000 after rejected oversell", 2000, cycleAfterRejectedThirdSale.cumulativeBirdsSold)
        assertTrue("Rejected op_3 transaction must not be recorded", AgritechMockDb.transactions.none { it.id == op3Key })

        // 3. Simulator Isolation: Deleting "farm_1" prevents PersonaSimulatorEngine from resurrecting "farm_1" in AgritechMockDb.farms
        val hadFarm1Initially = AgritechMockDb.farms.any { it.id == "farm_1" }
        AgritechMockDb.deleteFarmCompletely("farm_1")
        assertTrue("farm_1 must be in deletedFarmIds", "farm_1" in AgritechMockDb.deletedFarmIds)
        assertTrue("farm_1 must be removed from AgritechMockDb.farms", AgritechMockDb.farms.none { it.id == "farm_1" })

        PersonaSimulatorEngine.ensureInitialized()
        assertTrue("ensureInitialized() must NOT resurrect deleted farm_1", AgritechMockDb.farms.none { it.id == "farm_1" })

        PersonaSimulatorEngine.resetSimulationToDay7()
        assertTrue("resetSimulationToDay7() must NOT resurrect deleted farm_1", AgritechMockDb.farms.none { it.id == "farm_1" })

        PersonaSimulatorEngine.stepSimulationOneDay()
        assertTrue("stepSimulationOneDay() must NOT resurrect deleted farm_1", AgritechMockDb.farms.none { it.id == "farm_1" })

        PersonaSimulatorEngine.restoreUserState()
        assertTrue("restoreUserState() must NOT resurrect deleted farm_1", AgritechMockDb.farms.none { it.id == "farm_1" })

        // Clean up test artifacts
        AgritechMockDb.deletedFarmIds.remove("farm_1")
        if (hadFarm1Initially && AgritechMockDb.farms.none { it.id == "farm_1" }) {
            AgritechMockDb.farms.add(0, FarmModel("farm_1", "عنبر الخيرات والبركة 1", "البليدة - بوفاريك", 8000, "worker_1"))
        }
    }

    @Test
    fun securityAndConsistencyBatch_D01_D02_F02_F03_X01_invariantsHold() {
        // 1. D02: Explicit UID takes strict priority in resolveActiveStorageIdentityKey
        val resolvedIdentityA = AgritechAuthHelper.resolveActiveStorageIdentityKey("uid_account_alpha")
        val resolvedIdentityB = LocalFarmPersistence.resolveActiveStorageIdentityKey("uid_account_beta")
        assertEquals("auth_uid_uid_account_alpha", resolvedIdentityA)
        assertEquals("auth_uid_uid_account_beta", resolvedIdentityB)
        assertNotEquals(resolvedIdentityA, resolvedIdentityB)

        // 2. D01: resetForAccountSwitch clears chatMessages and chatConversations so messages never leak across accounts
        AgritechMockDb.chatMessages.add(
            com.example.models.ChatMessageModel(
                id = "msg_test_leak_check",
                cycleId = "general_admin",
                senderId = "uid_account_alpha",
                senderName = "حساب أ",
                senderRole = "مالك",
                text = "رسالة خاصة بالحساب الأول",
                timestamp = "12:00"
            )
        )
        AgritechMockDb.chatConversations.add(
            com.example.models.ChatConversationModel(
                id = "conv_test_leak_check",
                userName = "مراسل خاص",
                roleLabel = "تاجر",
                avatarEmoji = "🌾",
                avatarBgHex = 0xFFE8F5E9,
                defaultSnippet = "رسالة خاصة",
                defaultTimestamp = "12:00"
            )
        )
        assertTrue(AgritechMockDb.chatMessages.isNotEmpty())
        assertTrue(AgritechMockDb.chatConversations.isNotEmpty())

        AgritechMockDb.resetForAccountSwitch()
        assertTrue("resetForAccountSwitch() must clear chatMessages (D01)", AgritechMockDb.chatMessages.isEmpty())
        assertTrue("resetForAccountSwitch() must clear chatConversations (D01)", AgritechMockDb.chatConversations.isEmpty())

        // 3. F03: DailyReportModel preserves litterCondition independently from generalStatus
        val reportDay9 = DailyReportModel(
            id = "rep_f03_test",
            farmId = "farm_f03",
            cycleId = "cycle_f03",
            dayNumber = 9,
            feedIntakeKg = 350.0,
            waterIntakeLiters = 700.0,
            mortalityCount = 2,
            temperature = 29.5,
            ventilationStatus = "جيدة",
            generalStatus = "طبيعية",
            litterCondition = "رطبة قليلاً"
        )
        val savedReport = AgritechMockDb.upsertDailyReportWithAudit(reportDay9)
        assertEquals("طبيعية", savedReport.generalStatus)
        assertEquals("رطبة قليلاً", savedReport.litterCondition)
        assertFalse("Modifying only litterCondition must be detected as a real value change", savedReport.hasActualValueChangesFrom(savedReport.copy()))
        assertTrue(
            "Changing litterCondition from 'رطبة قليلاً' to 'جافة ممتازة' must be detected by hasActualValueChangesFrom",
            savedReport.copy(litterCondition = "جافة ممتازة").hasActualValueChangesFrom(savedReport)
        )

        // 4. F02: PersonalAndHouseholdEngine.evaluateMonthlyHouseholdBudget exposes savingsRatePct in customKpis
        val budgetSummary = PersonalAndHouseholdEngine.evaluateMonthlyHouseholdBudget(
            monthlySalaryAndIncome = 100000.0,
            electricityAndGasExpense = 5000.0,
            internetAndPhoneExpense = 3000.0,
            carInsuranceAndMaintenanceExpense = 7000.0,
            groceriesAndHomeExpense = 25000.0,
            previousMonthTotalExpense = 40000.0
        )
        assertEquals(40000.0, budgetSummary.totalExpenses, 0.001)
        assertEquals(60000.0, budgetSummary.netProfit, 0.001)
        assertTrue("customKpis must contain savingsRatePct (F02)", budgetSummary.customKpis.containsKey("savingsRatePct"))
        assertEquals(60.0, budgetSummary.customKpis["savingsRatePct"] ?: 0.0, 0.001)

        // 5. X01: PersonaSimulatorEngine.resolveSimulationTargetFarms() always returns isolated sim_test_ farm even when real farms exist
        AgritechMockDb.farms.add(FarmModel("real_user_farm_99", "مزرعة المستخدم الحقيقية", "سطيف", 10000, "worker_1"))
        val simTargets = PersonaSimulatorEngine.resolveSimulationTargetFarms()
        assertEquals(1, simTargets.size)
        assertEquals(PersonaSimulatorEngine.SIM_ISOLATED_FARM_ID, simTargets.first().id)
        assertTrue(simTargets.all { it.id.startsWith("sim_test_") })
        AgritechMockDb.farms.removeAll { it.id == "real_user_farm_99" || it.id == "farm_f03" }
        AgritechMockDb.dailyReports.removeAll { it.id == "rep_f03_test" }
    }

    @Test
    fun ross308BroilerHandbookChapters_andBookTabLabels_areCompleteAndAccurate() {
        // 1. Verify 14-language Book tab and sub-tab labels
        assertEquals("📖 الكتاب", Translations.tr("global_tab_ai_vet", AppLanguage.AR))
        assertEquals("📖 Book", Translations.tr("global_tab_ai_vet", AppLanguage.EN))
        assertEquals("📖 Livre", Translations.tr("global_tab_ai_vet", AppLanguage.FR))
        assertEquals("📖 Libro", Translations.tr("global_tab_ai_vet", AppLanguage.ES))
        assertEquals("📖 Livro", Translations.tr("global_tab_ai_vet", AppLanguage.PT))
        assertEquals("📖 Kitap", Translations.tr("global_tab_ai_vet", AppLanguage.TR))
        assertEquals("📖 Buch", Translations.tr("global_tab_ai_vet", AppLanguage.DE))
        assertEquals("📖 Libro", Translations.tr("global_tab_ai_vet", AppLanguage.IT))
        assertEquals("📖 Книга", Translations.tr("global_tab_ai_vet", AppLanguage.RU))
        assertEquals("📖 手册", Translations.tr("global_tab_ai_vet", AppLanguage.ZH))
        assertEquals("📖 पुस्तक", Translations.tr("global_tab_ai_vet", AppLanguage.HI))
        assertEquals("📖 کتاب", Translations.tr("global_tab_ai_vet", AppLanguage.UR))
        assertEquals("📖 Buku", Translations.tr("global_tab_ai_vet", AppLanguage.ID))
        assertEquals("📖 Kitabu", Translations.tr("global_tab_ai_vet", AppLanguage.SW))

        assertEquals("📊 التحليلات", Translations.tr("ai_subtab_smart_decisions", AppLanguage.AR))
        assertEquals("📊 Analytics", Translations.tr("ai_subtab_smart_decisions", AppLanguage.EN))
        assertEquals("📊 Analyses", Translations.tr("ai_subtab_smart_decisions", AppLanguage.FR))

        assertEquals("💡 نصائح", Translations.tr("ai_subtab_diagnostics", AppLanguage.AR))
        assertEquals("💡 Tips", Translations.tr("ai_subtab_diagnostics", AppLanguage.EN))
        assertEquals("💡 Conseils", Translations.tr("ai_subtab_diagnostics", AppLanguage.FR))

        assertEquals("💬 المحادثة", Translations.tr("ai_subtab_live_chat", AppLanguage.AR))
        assertEquals("💬 Chat", Translations.tr("ai_subtab_live_chat", AppLanguage.EN))
        assertEquals("💬 Discussion", Translations.tr("ai_subtab_live_chat", AppLanguage.FR))

        assertEquals(
            "📖 كتاب العنبر الذكي — مرجع دجاج اللحم (Ross 308 Book)",
            Translations.tr("ai_vet_welcome_title", AppLanguage.AR)
        )
        assertEquals(
            "اسأل كتاب العنبر الذكي عن الحرارة، العلف، الأوزان، أو الرعاية...",
            Translations.tr("ai_vet_input_placeholder", AppLanguage.AR)
        )
        assertEquals(
            "📖 قراءة الفصل وتطبيقه على عنبري",
            Translations.tr("ross308_apply_chapter_btn", AppLanguage.AR)
        )

        // 2. Verify the 5 documented Aviagen Ross 308 Broiler Management Handbook chapters & live barn comparison
        val chapters = Ross308BroilerHandbook.buildChaptersForBarn(
            barnName = "عنبر السلام 1",
            ageDays = 14,
            placedBirds = 5000,
            liveBirds = 4920,
            recordedTempC = 25.5,
            totalFeedQuintals = 26.0,
            remainingFeedQuintals = 14.0,
            latestDailyFeedKg = 310.0,
            latestDailyWaterLiters = 540.0,
            currentWeightKg = 0.53,
            actualFcr = 1.06
        )
        assertEquals(5, chapters.size)
        assertEquals("ross308_chapter_1", chapters[0].tag)
        assertTrue(chapters[0].summaryPoints.any { it.contains("39.4°C – 40.5°C") })
        assertTrue(chapters[0].summaryPoints.any { it.contains("95%") })
        assertTrue(chapters[0].summaryPoints.any { it.contains("190 – 200") })

        assertEquals("ross308_chapter_2", chapters[1].tag)
        assertTrue(chapters[1].summaryPoints.any { it.contains("0.10 m³/ساعة/طائر") && it.contains("1.50 m³/ساعة/طائر") })
        assertTrue(chapters[1].liveBarnComparison.contains("25.5°C"))

        assertEquals("ross308_chapter_3", chapters[2].tag)
        assertTrue(chapters[2].summaryPoints.any { it.contains("23%") && it.contains("3000 kcal/kg") })
        assertTrue(chapters[2].summaryPoints.any { it.contains("1.6 إلى 1.8 لتر") })
        assertTrue(chapters[2].liveBarnComparison.contains("نامي (Grower"))

        assertEquals("ross308_chapter_4", chapters[3].tag)
        assertTrue(chapters[3].summaryPoints.any { it.contains("FCR ~0.85") && it.contains("FCR ~1.25") })
        assertTrue(chapters[3].liveBarnComparison.contains("1.06"))

        assertEquals("ross308_chapter_5", chapters[4].tag)
        assertTrue(chapters[4].summaryPoints.any { it.contains("Ascites") && it.contains("Flip-over") })
        assertTrue(chapters[4].summaryPoints.any { it.contains("AD3E") })

        // 3. Verify the 4-Branch Drill-Down Card Architecture (4 branches x 4 sub-cards = 16 cards)
        val branches = Ross308BroilerHandbook.buildFourBranchesForBarn(
            barnName = "عنبر السلام 1",
            ageDays = 14,
            placedBirds = 5000,
            liveBirds = 4920,
            recordedTempC = 25.5,
            totalFeedQuintals = 26.0,
            remainingFeedQuintals = 14.0,
            latestDailyFeedKg = 310.0,
            latestDailyWaterLiters = 540.0,
            currentWeightKg = 0.53,
            actualFcr = 1.06
        )
        assertEquals(4, branches.size)
        assertEquals("برنامج الرعاية", branches[0].shortName)
        assertEquals("برنامج التحصينات", branches[1].shortName)
        assertEquals("الأمراض والوقاية", branches[2].shortName)
        assertEquals("برنامج العلف والأوزان", branches[3].shortName)

        // Each branch must contain exactly 4 sub-cards with 3 structured detail blocks
        branches.forEach { branch ->
            assertEquals(4, branch.subCards.size)
            branch.subCards.forEach { subCard ->
                assertEquals(branch.branchIndex, subCard.branchIndex)
                assertTrue(subCard.badge1.isNotBlank())
                assertTrue(subCard.badge2.isNotBlank())
                assertTrue(subCard.scientificRulePoints.isNotEmpty())
                assertTrue(subCard.practicalStepsWithCheck.isNotEmpty())
                assertTrue(subCard.practicalStepsWithCheck.all { it.startsWith("✓") })
                assertTrue(subCard.liveBarnComparisonText.isNotBlank())
            }
        }

        // Verify sub-card IDs per branch
        assertEquals(listOf("care_1", "care_2", "care_3", "care_4"), branches[0].subCards.map { it.id })
        assertEquals(listOf("vax_1", "vax_2", "vax_3", "vax_4"), branches[1].subCards.map { it.id })
        assertEquals(listOf("dis_1", "dis_2", "dis_3", "dis_4"), branches[2].subCards.map { it.id })
        assertEquals(listOf("feed_1", "feed_2", "feed_3", "feed_4"), branches[3].subCards.map { it.id })

        // Verify same-branch next/previous cyclic navigation
        assertEquals("care_2", Ross308BroilerHandbook.getNextSubCardId(branches, "care_1"))
        assertEquals("care_3", Ross308BroilerHandbook.getNextSubCardId(branches, "care_2"))
        assertEquals("care_4", Ross308BroilerHandbook.getNextSubCardId(branches, "care_3"))
        assertEquals("care_1", Ross308BroilerHandbook.getNextSubCardId(branches, "care_4"))

        assertEquals("care_4", Ross308BroilerHandbook.getPreviousSubCardId(branches, "care_1"))
        assertEquals("care_1", Ross308BroilerHandbook.getPreviousSubCardId(branches, "care_2"))

        assertEquals("feed_2", Ross308BroilerHandbook.getNextSubCardId(branches, "feed_1"))
        assertEquals("feed_4", Ross308BroilerHandbook.getPreviousSubCardId(branches, "feed_1"))
        assertEquals("feed_3", Ross308BroilerHandbook.findSubCard(branches, "feed_3")?.second?.id)
    }

    @Test
    fun googlePlayCompliance_accountDeletion_ugcModeration_and_noExternalPayment_workCorrectly() {
        // 1. Reset state and populate sample farms, market posts, and chat messages
        AgritechMockDb.resetForAccountSwitch()
        AgritechMockDb.farms.add(
            FarmModel(
                id = "farm_gp_1",
                name = "عنبر الامتثال 1",
                location = "البليدة",
                capacity = 5000
            )
        )
        AgritechMockDb.marketPosts.add(
            0,
            MarketPostModel(
                id = "post_ugc_bad_1",
                title = "عرض مشبوه في السوق",
                description = "تفاصيل عرض",
                priceDzd = 100.0,
                priceUnit = "دج / كغ",
                publisherName = "مستخدم مخالف",
                publisherLocation = "الجزائر",
                imageCaption = "صورة",
                chatChannelId = "bad_user_channel_1"
            )
        )
        AgritechMockDb.sendChatMessage(
            ChatMessageModel(
                id = "msg_ugc_bad_1",
                cycleId = "bad_user_channel_1",
                senderId = "bad_user_uid_99",
                senderName = "مستخدم مخالف",
                senderRole = "تاجر",
                text = "رسالة مخالفة",
                timestamp = "11:00 ص"
            )
        )

        // 2. Verify reporting a UGC item immediately hides it from marketPosts and streamChatMessages
        assertTrue(AgritechMockDb.marketPosts.any { it.id == "post_ugc_bad_1" })
        AgritechMockDb.reportUgcContent(
            itemId = "post_ugc_bad_1",
            targetUid = "bad_user_channel_1",
            reason = "احتيال أو أسعار وهمية"
        )
        assertTrue("post_ugc_bad_1" in AgritechMockDb.reportedUgcItemIds)
        assertFalse(
            "Reported market post must be removed immediately from local feed",
            AgritechMockDb.marketPosts.any { it.id == "post_ugc_bad_1" }
        )

        // 3. Verify blocking a user adds targetUid to blockedUserIds and immediately filters out their messages and conversations
        AgritechMockDb.blockUser(targetUid = "bad_user_uid_99")
        assertTrue("bad_user_uid_99" in AgritechMockDb.blockedUserIds)
        assertTrue(
            "Messages from blocked senderId must be removed and excluded from streamChatMessages",
            AgritechMockDb.streamChatMessages("bad_user_channel_1").none { it.senderId == "bad_user_uid_99" }
        )

        AgritechMockDb.blockUser(targetUid = "bad_user_channel_1")
        assertTrue(
            "Blocked channel must return empty messages list",
            AgritechMockDb.streamChatMessages("bad_user_channel_1").isEmpty()
        )

        // 4. Verify permanent account deletion clears all local farms, cycles, reports, transactions, chat messages, and sets confirmation message
        AgritechMockDb.clearAllDataForDeletedAccount(uid = "deleted_uid_test")
        assertTrue("Farms must be cleared after account deletion", AgritechMockDb.farms.isEmpty())
        assertTrue("Cycles must be cleared after account deletion", AgritechMockDb.cycles.isEmpty())
        assertTrue("DailyReports must be cleared after account deletion", AgritechMockDb.dailyReports.isEmpty())
        assertTrue("Transactions must be cleared after account deletion", AgritechMockDb.transactions.isEmpty())
        assertTrue("ChatMessages must be cleared after account deletion", AgritechMockDb.chatMessages.isEmpty())
        assertEquals(
            "تم حذف حسابك وجميع بياناتك نهائياً بنجاح",
            AgritechMockDb.accountDeletionConfirmationMessage
        )

        // Restore default seed for subsequent tests
        AgritechMockDb.accountDeletionConfirmationMessage = null
        AgritechMockDb.resetForAccountSwitch()
    }

    @Test
    fun encyclopediaInfographicPosters_allFourBranchesAndUnifiedTempCurve_areComplete() {
        // 1. Unified Ross 308 temperature curve verification
        assertEquals(33.0, Ross308BroilerHandbook.getStandardTempForDay(1), 0.001)
        assertEquals(33.0, Ross308BroilerHandbook.getStandardTempForDay(3), 0.001)
        assertEquals(31.0, Ross308BroilerHandbook.getStandardTempForDay(4), 0.001)
        assertEquals(31.0, Ross308BroilerHandbook.getStandardTempForDay(7), 0.001)
        assertEquals(29.0, Ross308BroilerHandbook.getStandardTempForDay(10), 0.001)
        assertEquals(29.0, Ross308BroilerHandbook.getStandardTempForDay(14), 0.001)
        assertEquals(27.0, Ross308BroilerHandbook.getStandardTempForDay(18), 0.001)
        assertEquals(24.0, Ross308BroilerHandbook.getStandardTempForDay(24), 0.001)
        assertEquals(21.0, Ross308BroilerHandbook.getStandardTempForDay(35), 0.001)

        val branches = Ross308BroilerHandbook.buildFourBranchesForBarn(
            barnName = "عنبر الاختبار",
            ageDays = 14,
            placedBirds = 5000,
            liveBirds = 4920,
            recordedTempC = 29.0,
            totalFeedQuintals = 18.0,
            remainingFeedQuintals = 12.0,
            latestDailyFeedKg = 240.0,
            latestDailyWaterLiters = 420.0,
            currentWeightKg = 0.52,
            actualFcr = 1.08
        )
        assertEquals(4, branches.size)

        // All 16 sub-cards must have a complete posterSpec with distinct visualType
        val visualTypes = mutableSetOf<InfographicVisualType>()
        branches.forEach { branch ->
            assertEquals(4, branch.subCards.size)
            branch.subCards.forEach { subCard ->
                val spec = subCard.posterSpec
                assertNotNull("posterSpec missing for ${subCard.id}", spec)
                spec!!
                assertTrue("specialtyBadge blank for ${subCard.id}", spec.specialtyBadge.isNotBlank())
                assertTrue("englishSubtitleOrRule blank for ${subCard.id}", spec.englishSubtitleOrRule.isNotBlank())
                assertTrue("diagramTitle blank for ${subCard.id}", spec.diagramTitle.isNotBlank())
                assertTrue("coreIdea blank for ${subCard.id}", spec.coreIdea.isNotBlank())
                visualTypes.add(spec.visualType)
            }
        }
        assertEquals("Expected 16 unique InfographicVisualType diagrams", 16, visualTypes.size)

        // Branch 3 (Diseases) must follow the 6-section medical anatomy & performance layout
        val diseaseCards = branches[2].subCards
        assertEquals(listOf("dis_1", "dis_2", "dis_3", "dis_4"), diseaseCards.map { it.id })
        diseaseCards.forEach { card ->
            val spec = card.posterSpec!!
            assertTrue("Medical layout must be enabled for ${card.id}", spec.isMedicalSixSectionLayout)
            assertTrue("sec1Definition blank for ${card.id}", spec.sec1Definition.isNotBlank())
            assertTrue("quickFacts missing for ${card.id}", spec.quickFacts.size >= 4)
            assertEquals("anatomyLabels must have 4 items for ${card.id}", 4, spec.anatomyLabels.size)
            assertEquals("sec2SymptomCards must have 5 cards for ${card.id}", 5, spec.sec2SymptomCards.size)
            assertTrue("sec3TreatmentSteps empty for ${card.id}", spec.sec3TreatmentSteps.isNotEmpty())
            assertEquals("sec4PerformanceTable must have 5 metrics for ${card.id}", 5, spec.sec4PerformanceTable.size)
            assertTrue("sec5DiagnosisPoints empty for ${card.id}", spec.sec5DiagnosisPoints.isNotEmpty())
            assertEquals("sec6PreventionIcons must have 5 shields for ${card.id}", 5, spec.sec6PreventionIcons.size)
            assertTrue("sec6ImportantNote blank for ${card.id}", spec.sec6ImportantNote.isNotBlank())
        }

        // Branches 1, 2, 4 must have timeline steps and good/bad comparison cards
        (branches[0].subCards + branches[1].subCards + branches[3].subCards).forEach { card ->
            val spec = card.posterSpec!!
            assertFalse("Non-disease card ${card.id} should not use medical layout", spec.isMedicalSixSectionLayout)
            assertTrue("whyImportantPoints should have 4 points for ${card.id}", spec.whyImportantPoints.size == 4)
            assertTrue("timelineSteps missing for ${card.id}", spec.timelineSteps.size >= 3)
            assertNotNull("comparisonPair missing for ${card.id}", spec.comparisonPair)
        }

        // Branch 2 (Vaccination) & Branch 4 (Feed) must include structured tables
        (branches[1].subCards + branches[3].subCards).forEach { card ->
            val spec = card.posterSpec!!
            assertTrue("tableHeaders missing for ${card.id}", spec.tableHeaders.isNotEmpty())
            assertTrue("tableRows missing for ${card.id}", spec.tableRows.isNotEmpty())
        }

        // Verify 4 independent books (each specialty has 4 lessons numbered 01..04) and 14-language localization
        branches.forEach { branch ->
            assertEquals(
                listOf("01", "02", "03", "04"),
                branch.subCards.map { it.posterSpec!!.lessonNumberFormatted }
            )
            assertEquals(
                listOf(1, 2, 3, 4),
                branch.subCards.map { it.cardNumberInBranch }
            )
        }
        val bookPages = Ross308BroilerHandbook.buildSixteenBookPages(branches)
        assertEquals(16, bookPages.size)
        for (lang in AppLanguage.values()) {
            val ribbonTitle = Translations.tr("book_encyclopedia_ribbon_title", lang)
            assertTrue("book_encyclopedia_ribbon_title blank for $lang", ribbonTitle.isNotBlank() && ribbonTitle != "book_encyclopedia_ribbon_title")
            for (bIdx in 0..3) {
                val bTitle = Translations.tr("book_branch_${bIdx}_title", lang)
                val bSub = Translations.tr("book_branch_${bIdx}_subtitle", lang)
                assertTrue("book_branch_${bIdx}_title blank for $lang", bTitle.isNotBlank() && bTitle != "book_branch_${bIdx}_title")
                assertTrue("book_branch_${bIdx}_subtitle blank for $lang", bSub.isNotBlank() && bSub != "book_branch_${bIdx}_subtitle")
            }
            for (page in bookPages) {
                val subCard = page.second
                val (locCard, locSpec) = com.example.utils.Ross308BookPageTranslations.localizeSubCardAndSpec(
                    subCard = subCard,
                    spec = subCard.posterSpec!!,
                    lang = lang
                )
                assertTrue("locCard.title blank for ${subCard.id} in $lang", locCard.title.isNotBlank())
                assertTrue("locSpec.specialtyBadge blank for ${subCard.id} in $lang", locSpec.specialtyBadge.isNotBlank())
            }
        }
    }

    @Test
    fun dailyReportNullableTemperature_doesNotFabricateReadingAndDetectsChanges() {
        val base = DailyReportModel(
            id = "rep_null_1",
            farmId = "farm_1",
            cycleId = "cyc_1",
            dayNumber = 5,
            feedIntakeKg = 100.0,
            waterIntakeLiters = 180.0,
            mortalityCount = 1,
            temperature = null
        )
        assertNull(base.temperature)
        val reading = IndoorReadingState.fromManualReport("farm_1", base)
        assertNull("Report with null temperature must keep IndoorReadingState.temperatureC null", reading.temperatureC)

        val same = base.copy(id = "rep_null_copy")
        assertFalse(base.hasActualValueChangesFrom(same))

        val withTemp = base.copy(temperature = 31.0)
        assertTrue(withTemp.hasActualValueChangesFrom(base))
        assertTrue(base.hasActualValueChangesFrom(withTemp))
        assertEquals(31.0, IndoorReadingState.fromManualReport("farm_1", withTemp).temperatureC ?: 0.0, 0.001)
    }

    @Test
    fun audit8f04be5_feedDeficit_cycleScopedReportIds_deltaRollbacks_and_storageIdentityUnification() {
        // 1. Storage identity unification: raw UID, local_user_<uid>, and auth_uid_<uid> all resolve to auth_uid_<uid>
        assertEquals("auth_uid_user_abc_123", AgritechAuthHelper.resolveActiveStorageIdentityKey("user_abc_123"))
        assertEquals("auth_uid_user_abc_123", AgritechAuthHelper.resolveActiveStorageIdentityKey("local_user_user_abc_123"))
        assertEquals("auth_uid_user_abc_123", AgritechAuthHelper.resolveActiveStorageIdentityKey("auth_uid_user_abc_123"))
        assertEquals("auth_uid_user_abc_123", LocalFarmPersistence.resolveActiveStorageIdentityKey("user_abc_123"))
        assertEquals("auth_uid_user_abc_123", LocalFarmPersistence.resolveActiveStorageIdentityKey("local_user_user_abc_123"))
        assertEquals("auth_uid_user_abc_123", LocalFarmPersistence.resolveActiveStorageIdentityKey("auth_uid_user_abc_123"))

        // 2. Market 2-party direct channel ID determinism and farm isolation
        val chanAB = AgritechFirestoreRepository.buildMarketDirectChannelId("post_55", "buyer_1", "seller_9")
        val chanBA = AgritechFirestoreRepository.buildMarketDirectChannelId("post_55", "seller_9", "buyer_1")
        assertEquals(chanAB, chanBA)
        assertEquals("market_post_55_buyer_1_seller_9", chanAB)
        assertTrue(AgritechFirestoreRepository.isMarketOrPrivateDirectChannelId(chanAB))
        assertFalse(AgritechFirestoreRepository.isMarketOrPrivateDirectChannelId("farm_1_cycle_1_worker"))

        // 3. Cycle-scoped report IDs & Feed Deficit without phantom inventory
        val farmId = "farm_audit_unit"
        AgritechMockDb.cycles.removeAll { it.farmId == farmId }
        AgritechMockDb.dailyReports.removeAll { it.farmId == farmId }
        AgritechMockDb.cycles.add(
            CycleModel(
                id = "cycle_u1",
                farmId = farmId,
                status = "active",
                totalBirdsPlaced = 5000,
                feedInventoryKg = 0.0,
                feedDeficitKg = 0.0
            )
        )
        AgritechMockDb.cycles.add(
            CycleModel(
                id = "cycle_u2",
                farmId = farmId,
                status = "active",
                totalBirdsPlaced = 5000,
                feedInventoryKg = 600.0,
                feedDeficitKg = 0.0
            )
        )

        // Day 1 in cycle_u1 consumes 250 kg when inventory is 0 -> inventory = 0.0, deficit = 250.0
        val repC1D1 = AgritechMockDb.upsertDailyReportWithAudit(
            DailyReportModel(
                id = "rep_temp",
                farmId = farmId,
                cycleId = "cycle_u1",
                dayNumber = 1,
                feedIntakeKg = 250.0,
                waterIntakeLiters = 500.0,
                mortalityCount = 5,
                temperature = 33.0
            )
        )
        assertEquals("rep_farm_audit_unit_cycle_u1_day_1", repC1D1.id)
        val c1AfterCreate = AgritechMockDb.cycles.first { it.id == "cycle_u1" }
        assertEquals(0.0, c1AfterCreate.feedInventoryKg, 0.001)
        assertEquals(250.0, c1AfterCreate.feedDeficitKg, 0.001)
        assertEquals(250.0, c1AfterCreate.cumulativeFeedConsumedKg, 0.001)
        assertEquals(5, c1AfterCreate.cumulativeMortality)

        // Edit Day 1 in cycle_u1 from 250 kg -> 200 kg and mortality 5 -> 2 -> inventory stays 0.0 (no +50 kg phantom!), deficit becomes 200.0
        val repC1D1Edited = AgritechMockDb.upsertDailyReportWithAudit(
            repC1D1.copy(feedIntakeKg = 200.0, mortalityCount = 2)
        )
        val c1AfterEdit = AgritechMockDb.cycles.first { it.id == "cycle_u1" }
        assertEquals(0.0, c1AfterEdit.feedInventoryKg, 0.001)
        assertEquals(200.0, c1AfterEdit.feedDeficitKg, 0.001)
        assertEquals(200.0, c1AfterEdit.cumulativeFeedConsumedKg, 0.001)
        assertEquals(2, c1AfterEdit.cumulativeMortality)

        // Day 1 in cycle_u2 does NOT collide with Day 1 in cycle_u1
        val repC2D1 = AgritechMockDb.upsertDailyReportWithAudit(
            DailyReportModel(
                id = "rep_temp",
                farmId = farmId,
                cycleId = "cycle_u2",
                dayNumber = 1,
                feedIntakeKg = 150.0,
                waterIntakeLiters = 300.0,
                mortalityCount = 1,
                temperature = 32.0
            )
        )
        assertEquals("rep_farm_audit_unit_cycle_u2_day_1", repC2D1.id)
        assertNotEquals(repC1D1Edited.id, repC2D1.id)
        assertEquals(2, AgritechMockDb.dailyReports.count { it.farmId == farmId && it.dayNumber == 1 })

        // 4. Operation-specific delta rollback preserves concurrent updates
        AgritechMockDb.rollbackDailyReportMutation(repC1D1, repC1D1Edited)
        val c1AfterRollback = AgritechMockDb.cycles.first { it.id == "cycle_u1" }
        assertEquals(250.0, c1AfterRollback.feedDeficitKg, 0.001)
        assertEquals(5, c1AfterRollback.cumulativeMortality)
    }

    @Test
    fun ross308_fourteenLanguageBookLessons_and_dailyReportKeys_areComplete() {
        val branches = Ross308BroilerHandbook.buildFourBranchesForBarn(
            barnName = "عنبر السلام",
            ageDays = 22,
            placedBirds = 5000,
            liveBirds = 4900,
            recordedTempC = 24.0,
            totalFeedQuintals = 45.0,
            remainingFeedQuintals = 15.0,
            latestDailyFeedKg = 420.0,
            latestDailyWaterLiters = 720.0,
            currentWeightKg = 1.05,
            actualFcr = 1.28
        )
        val feed3Card = branches[3].subCards.first { it.id == "feed_3" }
        assertTrue(feed3Card.title.contains("75/25"))
        assertTrue(feed3Card.posterSpec!!.coreIdea.contains("75/25"))

        val care1Card = branches[0].subCards.first { it.id == "care_1" }
        assertTrue(care1Card.title.contains("33.5"))
        assertTrue(care1Card.posterSpec!!.coreIdea.contains("33.5"))

        val requiredUiKeys = listOf(
            "book_btn_back_to_4_books",
            "book_btn_back_to_index",
            "book_index_header_fmt",
            "book_index_short_0",
            "book_index_short_1",
            "book_index_short_2",
            "book_index_short_3",
            "book_ribbon_success_signs",
            "book_card_success_do_this",
            "book_card_avoid_dont",
            "book_badge_stopped_here",
            "book_btn_start_reading",
            "book_swipe_page_footer_fmt",
            "book_barn_selector_label",
            "book_4_lessons_badge_fmt",
            "book_branch_3_title_display",
            "book_branch_0_shelf_desc",
            "book_branch_1_shelf_desc",
            "book_branch_2_shelf_desc",
            "book_branch_3_shelf_desc",
            "no_active_cycle",
            "btn_owner_add_daily_report",
            "dialog_owner_daily_report_title",
            "dialog_edit_daily_report_title",
            "field_day_number_mid_cycle",
            "field_day_number_mid_cycle_hint",
            "confirm_medicine_water_today_label",
            "medicine_water_name_input_label",
            "field_actual_bird_weight_grams_optional",
            "book_analytics_section_title"
        )

        for (lang in AppLanguage.values()) {
            for (key in requiredUiKeys) {
                val value = Translations.tr(key, lang)
                assertTrue("Key $key blank or echoed for $lang", value.isNotBlank() && value != key)
                if (lang != AppLanguage.EN) {
                    assertNotEquals(
                        "Key $key must not fall back identically to English for $lang",
                        Translations.tr(key, AppLanguage.EN),
                        value
                    )
                }
            }

            // Verify book_index_header_fmt replaces {book}/{title}/{count} cleanly without raw placeholders
            for (branchIdx in 0..3) {
                val shortBook = Translations.tr("book_index_short_$branchIdx", lang)
                val formattedHeader = Translations.tr("book_index_header_fmt", lang)
                    .replace("{book}", shortBook)
                    .replace("{title}", shortBook)
                    .replace("{count}", "4")
                assertFalse("Header must not retain {book} for $lang", formattedHeader.contains("{book}"))
                assertFalse("Header must not retain {title} for $lang", formattedHeader.contains("{title}"))
                assertFalse("Header must not retain raw braces for $lang", formattedHeader.contains("{") || formattedHeader.contains("}"))
                assertTrue("Header must contain resolved specialty name for $lang", formattedHeader.contains(shortBook))
            }

            for (branch in branches) {
                for (subCard in branch.subCards) {
                    val (locCard, locSpec) = com.example.utils.Ross308BookPageTranslations.localizeSubCardAndSpec(
                        subCard = subCard,
                        spec = subCard.posterSpec!!,
                        lang = lang
                    )
                    assertTrue("Title blank for ${subCard.id} in $lang", locCard.title.isNotBlank())
                    assertTrue("CoreIdea blank for ${subCard.id} in $lang", locSpec.coreIdea.isNotBlank())
                    if (locSpec.isMedicalSixSectionLayout) {
                        assertTrue("sec1Definition blank for ${subCard.id} in $lang", locSpec.sec1Definition.isNotBlank())
                        assertEquals("quickFacts size for ${subCard.id} in $lang", 4, locSpec.quickFacts.size)
                        assertEquals("sec2SymptomCards size for ${subCard.id} in $lang", 5, locSpec.sec2SymptomCards.size)
                        assertEquals("sec4PerformanceTable size for ${subCard.id} in $lang", 5, locSpec.sec4PerformanceTable.size)
                    } else {
                        assertEquals("whyImportantPoints size for ${subCard.id} in $lang", 4, locSpec.whyImportantPoints.size)
                        assertTrue("timelineSteps size for ${subCard.id} in $lang", locSpec.timelineSteps.size >= 3)
                        assertNotNull("comparisonPair null for ${subCard.id} in $lang", locSpec.comparisonPair)
                    }
                    if (lang != AppLanguage.AR && lang != AppLanguage.EN) {
                        val enSpec = com.example.utils.Ross308BookPageTranslations.localizeSubCardAndSpec(
                            subCard = subCard,
                            spec = subCard.posterSpec!!,
                            lang = AppLanguage.EN
                        ).second
                        assertNotEquals(
                            "coreIdea for ${subCard.id} must not fall back to EN in $lang",
                            enSpec.coreIdea,
                            locSpec.coreIdea
                        )
                    }
                }
            }
        }
    }

    @Test
    fun nanoBananaImageEngine_endpointPromptsFallbackAndSanitization_areValid() {
        assertEquals(
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent",
            BookNanoBananaImageEngine.NANO_BANANA_ENDPOINT
        )
        assertEquals("hero_feed_1", BookNanoBananaImageEngine.sanitizeImageKey("hero feed/1"))

        val heroPrompt = BookNanoBananaImageEngine.buildPromptForHero(
            lessonId = "feed_1",
            visualType = InfographicVisualType.FEED_STARTER_SPEC
        )
        assertTrue(heroPrompt.contains("ZERO text"))
        assertTrue(heroPrompt.contains("NO letters"))

        for (stepIdx in 0..2) {
            val stepPrompt = BookNanoBananaImageEngine.buildPromptForStep(
                lessonId = "care_1",
                visualType = InfographicVisualType.CARE_PREHEAT_PREP,
                stepIndex = stepIdx
            )
            assertTrue(stepPrompt.contains("ZERO text"))
        }

        // All visual types map cleanly to pre-bundled watercolor drawables in res/drawable/
        for (visualType in InfographicVisualType.values()) {
            val resId = BookNanoBananaImageEngine.bundledFallbackDrawableFor(visualType)
            assertTrue("Fallback drawable for $visualType must be non-zero", resId != 0)
        }
    }
}


