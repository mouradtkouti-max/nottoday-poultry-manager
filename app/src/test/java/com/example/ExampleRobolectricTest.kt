package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.models.*
import com.example.utils.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Poultry Manager", appName)
    }

    @Test
    fun `test accurate FCR and EPEF calculations`() {
        // Realistic Field Standard anchors check:
        // Day 0: 40g, Day 1: 49g, Day 7: 187g, Day 14: 430g, Day 21: 1000g,
        // Day 28: 1550g, Day 35: 2050g (2.05 kg), Day 42: 2570g, Day 45: 2800g
        assertEquals(40.0, PoultryCalculators.getRealisticFieldWeightGrams(0), 0.001)
        assertEquals(49.0, PoultryCalculators.getRealisticFieldWeightGrams(1), 0.001)
        assertEquals(187.0, PoultryCalculators.getRealisticFieldWeightGrams(7), 0.001)
        assertEquals(1000.0, PoultryCalculators.getRealisticFieldWeightGrams(21), 0.001)
        assertEquals(2800.0, PoultryCalculators.getRealisticFieldWeightGrams(45), 0.001)

        val standardWeightDay35 = PoultryCalculators.getRoss308StandardWeightKg(35)
        assertEquals(2.05, standardWeightDay35, 0.001)

        val fcr = PoultryCalculators.calculateFcr(
            totalFeedKg = 32800.0,
            initialBirds = 10500,
            totalMortality = 500,
            birdsSold = 0,
            ageInDays = 35
        )
        assertEquals(1.6, fcr, 0.001)

        // Partial sales (Thinning) FCR & Cost distortion verification:
        // Initial: 10,500 birds, Mortality: 500, Sold: 2,000 birds @ 4,000 kg total sold weight
        // Remaining liveBirds = 8,000 @ 2.05 kg (Day 35) = 16,400 kg remaining live biomass
        // Total Produced Biomass = 16,400 + 4,000 = 20,400 kg
        val thinningLiveBirds = PoultryCalculators.calculateLiveBirds(10500, 500, 2000)
        assertEquals(8000, thinningLiveBirds)

        val totalProducedBiomassKg = PoultryCalculators.calculateTotalProducedBiomassKg(
            liveBirds = thinningLiveBirds,
            avgLiveWeightKg = 2.05,
            cumulativeSoldWeightKg = 4000.0
        )
        assertEquals(20400.0, totalProducedBiomassKg, 0.001)

        val thinningFcr = PoultryCalculators.calculateFcr(
            totalFeedKg = 32640.0,
            initialBirds = 10500,
            totalMortality = 500,
            birdsSold = 2000,
            ageInDays = 35,
            cumulativeSoldWeightKg = 4000.0
        )
        assertEquals(1.6, thinningFcr, 0.001)

        val costPerBird = PoultryCalculators.calculateCostPerBird(
            totalExpenses = 4000000.0,
            liveBirds = thinningLiveBirds,
            cumulativeBirdsSold = 2000
        )
        assertEquals(400.0, costPerBird, 0.001)

        val costPerKg = PoultryCalculators.calculateCostPerKg(
            totalExpenses = 4080000.0,
            totalProducedBiomassKg = totalProducedBiomassKg
        )
        assertEquals(200.0, costPerKg, 0.001)

        val epef = PoultryCalculators.calculateEpef(
            livabilityPct = 95.238,
            avgLiveWeightKg = 2.05,
            ageInDays = 35,
            fcr = fcr
        )
        assertTrue(epef > 340.0)

        // Division-by-zero safety check
        assertEquals(0.0, PoultryCalculators.calculateEpef(95.0, 2.05, 0, 1.6), 0.001)
        assertEquals(0.0, PoultryCalculators.calculateEpef(95.0, 2.05, 35, 0.0), 0.001)
    }

    @Test
    fun `test universal quintal feed unit formatting and atomic vet medicine invoicing`() {
        // 1. Universal Quintal (ق) formatting checks: 15 kg -> 0.15 ق, 50 kg -> 0.5 ق, 740 kg -> 7.4 ق, 4000 kg -> 40 ق
        assertEquals("0.15", Translations.formatQuintalNumber(15.0 / 100.0))
        assertEquals("0.5", Translations.formatQuintalNumber(50.0 / 100.0))
        assertEquals("7.4", Translations.formatQuintalNumber(740.0 / 100.0))
        assertEquals("40", Translations.formatQuintalNumber(4000.0 / 100.0))

        val badge15kg = Translations.formatDualFeedInventoryBadge(15.0, AppLanguage.AR)
        assertEquals("مخزون: 0.15 ق 🌾", badge15kg)

        val bulk15kg = Translations.formatBulkFeedPurchaseQuantity(15.0, AppLanguage.AR)
        assertEquals("0.15 ق", bulk15kg)

        val badge50kg = Translations.formatDualFeedInventoryBadge(50.0, AppLanguage.AR)
        assertEquals("مخزون: 0.5 ق 🌾", badge50kg)

        val bulk4000kg = Translations.formatBulkFeedPurchaseQuantity(4000.0, AppLanguage.AR)
        assertEquals("40 ق", bulk4000kg)

        // 2. Atomic Vet Medicine Invoice & Prescription check
        val initialNotesCount = AgritechMockDb.vetNotes.size
        val initialTxCount = AgritechMockDb.transactions.size

        val testNote = VetNoteModel(
            id = "vn_test_atomic_1",
            farmId = "farm_1",
            cycleId = "cycle_2025_1",
            dayNumber = 5,
            medicineName = "مضاد حيوي إنروفلوكساسين",
            dosage = "1 مل لكل لتر ماء لمدة 4 أيام",
            note = "وصفة طبية معتمدة",
            isUrgent = true
        )
        val testTx = TransactionModel(
            id = "tx_test_atomic_1",
            farmId = "farm_1",
            cycleId = "cycle_2025_1",
            type = TransactionType.EXPENSE,
            category = "💊 الأدوية واللقاحات - مضاد حيوي إنروفلوكساسين",
            amount = 14500.0,
            currency = "دج",
            date = "اليوم 5"
        )

        AgritechMockDb.recordVetInvoiceAndPrescription(testTx, testNote)

        assertEquals(initialNotesCount + 1, AgritechMockDb.vetNotes.size)
        assertEquals(initialTxCount + 1, AgritechMockDb.transactions.size)
        assertTrue(AgritechMockDb.vetNotes.any { it.id == "vn_test_atomic_1" && it.medicineName == "مضاد حيوي إنروفلوكساسين" })
        assertTrue(AgritechMockDb.transactions.any { it.id == "tx_test_atomic_1" && it.amount == 14500.0 && it.type == TransactionType.EXPENSE })
    }

    @Test
    fun parseAgritechQrInvite_parsesStructuredAndFallbackQrPayloads() {
        val vetQr = parseAgritechQrInvite("AGRITECH_JOIN|farmId=farm_1|farmName=عنبر البركة النموذجي|role=VETERINARIAN")!!
        assertEquals("farm_1", vetQr.farmId)
        assertEquals(AppRole.VETERINARIAN, vetQr.role)

        val workerQr = parseAgritechQrInvite("AGRITECH_JOIN|farmId=farm_2|farmName=مزرعة الوفاق|role=WORKER")!!
        assertEquals("farm_2", workerQr.farmId)
        assertEquals(AppRole.WORKER, workerQr.role)
    }

    @Test
    fun aiVetGeminiService_providesClinicalAdviceForSymptoms() {
        val respiratoryReply = AiVetGeminiService.buildClinicalFallbackAdvice("حشرجة وعطس تنفسي في اليوم 18")
        assertTrue(respiratoryReply.contains("تنفسية") || respiratoryReply.contains("التيلوزين"))
    }

    @Test
    fun testGlobalOnboardingPortugueseAndCurrencyPropagation() {
        val previousCurrency = Translations.globalCurrencyCode
        try {
            // Verify Portuguese (AppLanguage.PT) support
            assertEquals("Galpões e Instalações Avícolas", Translations.tr("farms_title", AppLanguage.PT))
            assertEquals("Gestão Pro Galpão Adicional - $4.99 / 2 Meses (Ciclo Completo)", Translations.tr("paywall_offer_title", AppLanguage.PT))

            // Apply COP (Colombian Peso) globally and verify financial field labels update
            Translations.globalCurrencyCode = "COP"
            assertEquals("COP", Translations.tr("currency", AppLanguage.ES))
            assertTrue(Translations.tr("amount_label", AppLanguage.AR).contains("(COP)"))
            assertTrue(Translations.tr("field_chick_price", AppLanguage.PT).contains("(COP)"))
            assertTrue(Translations.tr("vet_invoice_amount_label", AppLanguage.EN).contains("(COP)"))
        } finally {
            Translations.globalCurrencyCode = previousCurrency
        }
    }

    @Test
    fun testFreemiumPaywallLogic() {
        // 1st Barn (existingOwnedCount == 0) is Free -> bypasses paywall
        assertEquals(false, shouldInterceptNewBarnWithPaywall(existingOwnedCount = 0, unlockedAdditionalBarnsCount = 0))

        // 2nd+ Barn (existingOwnedCount >= 1) intercepts and triggers SubscriptionPaywallBottomSheet
        assertEquals(true, shouldInterceptNewBarnWithPaywall(existingOwnedCount = 1, unlockedAdditionalBarnsCount = 0))
        assertEquals(true, shouldInterceptNewBarnWithPaywall(existingOwnedCount = 2, unlockedAdditionalBarnsCount = 0))

        // Once subscribed via Google Play / Stripe, additional barn slot unlocks
        assertEquals(false, shouldInterceptNewBarnWithPaywall(existingOwnedCount = 1, unlockedAdditionalBarnsCount = 1))
    }

    @Test
    fun testSessionStatePreservesAuthAcrossThemeAndLanguageChanges() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val session = AgritechAppSessionState(
            initialOnboardingCompleted = true,
            initialTheme = AppThemeOption.ICE_BLUE,
            initialLanguage = AppLanguage.AR,
            initialCountryCode = "DZ",
            initialCurrencyCode = "DZD",
            initialIsLoggedIn = false,
            initialRole = null,
            initialHasLinkedBarns = false,
            initialActiveFarmId = "farm_1",
            initialUserUid = null
        )

        // Authenticate user as OWNER
        session.markAuthenticated(
            context = context,
            role = AppRole.OWNER,
            linkedBarns = true,
            farmId = "farm_1",
            uid = "uid_owner_1"
        )
        session.selectedFarmForOwner = FarmModel("farm_1", "عنبر البركة النموذجي", "المدية", 12000, "worker_1")

        // Switch theme to NIGHT_MODE and language to PT + currency to COP
        session.updateTheme(context, AppThemeOption.NIGHT_MODE)
        session.updateGlobalSettings(
            context = context,
            newLanguage = AppLanguage.PT,
            newCountryCode = "CO",
            newCurrencyCode = "COP"
        )

        // Auth state and active navigation MUST remain intact
        assertEquals(true, session.isLoggedIn)
        assertEquals(AppRole.OWNER, session.currentRole)
        assertEquals("uid_owner_1", session.currentUserUid)
        assertEquals(AppThemeOption.NIGHT_MODE, session.currentTheme)
        assertEquals(AppLanguage.PT, session.currentLanguage)
        assertEquals("COP", session.currentCurrencyCode)
        assertEquals("farm_1", session.selectedFarmForOwner?.id)

        // Only explicit logout clears auth state
        session.performExplicitLogout(context)
        assertEquals(false, session.isLoggedIn)
        assertEquals(null, session.currentRole)
        assertEquals(null, session.selectedFarmForOwner)
    }

    @Test
    fun testSurgicalStrike1TranslationsAndZeroState() {
        // 1. Verify all previously exposed vet_invoice_* and btn_cancel keys are translated across AR, FR, EN, ES, PT
        val requiredKeys = listOf(
            "vet_invoic",
            "vet_invoice_btn",
            "vet_invoice_dialog_subtitle",
            "vet_med_name_label",
            "vet_dosage_instructions_label",
            "vet_med_total_price_label",
            "vet_clinical_note_label",
            "vet_invoice_submit_btn",
            "vet_invoice_saved_banner",
            "btn_cancel",
            "zero_barns_title",
            "zero_barns_subtitle",
            "zero_barns_create_btn"
        )
        val supportedLanguages = listOf(AppLanguage.AR, AppLanguage.FR, AppLanguage.EN, AppLanguage.ES, AppLanguage.PT)
        for (key in requiredKeys) {
            for (lang in supportedLanguages) {
                val translated = Translations.tr(key, lang)
                assertTrue("Key '$key' should not return raw key for $lang", translated != key && translated.isNotBlank())
            }
        }

        // 2. Verify WhatsApp-style neutral language labels contain zero country flags
        assertEquals("العربية", AppLanguage.AR.label)
        assertEquals("Français", AppLanguage.FR.label)
        assertEquals("English", AppLanguage.EN.label)
        assertEquals("Español", AppLanguage.ES.label)
        assertEquals("Português", AppLanguage.PT.label)

        // 3. Verify True Zero-State for new users (0 pre-populated barns -> 1st barn is 100% free)
        assertEquals(0, AgritechMockDb.farms.size)
        assertEquals(false, shouldInterceptNewBarnWithPaywall(AgritechMockDb.farms.size, 0))
    }

    @Test
    fun testSurgicalStrike2FieldOperationsAuditAndVetApprovalWorkflow() {
        // 1. Verify all new Surgical Strike 2 translation keys across AR, FR, EN, ES, PT
        val strike2Keys = listOf(
            "btn_owner_add_daily_report",
            "dialog_owner_daily_report_title",
            "dialog_edit_daily_report_title",
            "field_day_number_mid_cycle",
            "th_edit",
            "badge_report_edited",
            "audit_previous_values_prefix",
            "vet_invoice_pending_badge",
            "vet_invoice_approved_badge",
            "btn_approve_vet_invoice",
            "vet_pending_banner_title",
            "vet_pending_banner_subtitle",
            "btn_save"
        )
        val supportedLanguages = listOf(AppLanguage.AR, AppLanguage.FR, AppLanguage.EN, AppLanguage.ES, AppLanguage.PT)
        for (key in strike2Keys) {
            for (lang in supportedLanguages) {
                val translated = Translations.tr(key, lang)
                assertTrue("Key '$key' should be translated for $lang", translated != key && translated.isNotBlank())
            }
        }

        // 2. Test Owner Daily Report Entry (Mid-Cycle Day 25) & Edit Audit Trail (`isEdited = true`)
        val midCycleReport = DailyReportModel(
            id = "rep_audit_test_d25",
            farmId = "farm_audit_1",
            cycleId = "cycle_audit_1",
            dayNumber = 25,
            feedIntakeKg = 220.0,
            waterIntakeLiters = 410.0,
            mortalityCount = 2,
            temperature = 28.0,
            gasUnits = 1.4
        )
        AgritechMockDb.upsertDailyReportWithAudit(midCycleReport)
        val createdReport = AgritechMockDb.dailyReports.first { it.id == "rep_audit_test_d25" }
        assertEquals(false, createdReport.isEdited)

        // Now modify the Day 25 report -> must set isEdited = true and record previousSummary
        val modifiedReport = createdReport.copy(
            feedIntakeKg = 250.0,
            mortalityCount = 4
        )
        AgritechMockDb.upsertDailyReportWithAudit(modifiedReport)
        val auditedReport = AgritechMockDb.dailyReports.first { it.id == "rep_audit_test_d25" }
        assertEquals(true, auditedReport.isEdited)
        assertTrue(auditedReport.previousSummary.isNotBlank())
        assertEquals(250.0, auditedReport.feedIntakeKg, 0.001)
        assertEquals(4, auditedReport.mortalityCount)

        // 3. Test Vet Invoice Approval Workflow (PENDING -> excluded from Treasury -> Owner Approves -> APPROVED & deducted)
        val pendingVetNote = VetNoteModel(
            id = "vn_pending_test_1",
            farmId = "farm_audit_1",
            cycleId = "cycle_audit_1",
            dayNumber = 25,
            medicineName = "لقاح نيوكاسل + مضاد تنفسي",
            dosage = "1 مل / لتر",
            note = "وصفة بيطرية",
            isUrgent = true
        )
        val pendingVetTx = TransactionModel(
            id = "tx_pending_test_1",
            farmId = "farm_audit_1",
            cycleId = "cycle_audit_1",
            type = TransactionType.EXPENSE,
            category = "MEDICATION",
            amount = 18000.0,
            currency = "DZD",
            date = "اليوم 25"
        )
        AgritechMockDb.recordVetInvoiceAndPrescription(pendingVetTx, pendingVetNote)

        val savedPendingTx = AgritechMockDb.transactions.first { it.id == "tx_pending_test_1" }
        val savedPendingNote = AgritechMockDb.vetNotes.first { it.id == "vn_pending_test_1" }
        assertEquals("PENDING", savedPendingTx.status)
        assertEquals("PENDING", savedPendingNote.status)

        // While PENDING, it must NOT be deducted from Treasury netBalance
        val summaryBeforeApproval = AgritechFirestoreRepository.calculateNetBalanceFromTransactions(listOf(savedPendingTx))
        assertEquals(0.0, summaryBeforeApproval.totalExpenses, 0.001)
        assertEquals(0.0, summaryBeforeApproval.netBalance, 0.001)
        assertEquals(18000.0, summaryBeforeApproval.pendingExpenses, 0.001)

        // Owner approves the Vet Invoice
        AgritechMockDb.approveVetInvoice("tx_pending_test_1", "vn_pending_test_1")
        val approvedTx = AgritechMockDb.transactions.first { it.id == "tx_pending_test_1" }
        val approvedNote = AgritechMockDb.vetNotes.first { it.id == "vn_pending_test_1" }
        assertEquals("APPROVED", approvedTx.status)
        assertEquals("APPROVED", approvedNote.status)

        // Once APPROVED, it is deducted from Treasury netBalance
        val summaryAfterApproval = AgritechFirestoreRepository.calculateNetBalanceFromTransactions(listOf(approvedTx))
        assertEquals(18000.0, summaryAfterApproval.totalExpenses, 0.001)
        assertEquals(-18000.0, summaryAfterApproval.netBalance, 0.001)
        assertEquals(0.0, summaryAfterApproval.pendingExpenses, 0.001)
    }

    @Test
    fun testPhase1AutoLocaleDetectionAppearanceStudioAndPaywall499() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Smart Auto-Detection of Country, Currency, and Language on First Launch
        val dzDetected = detectInitialLocaleAndCurrency(context, overrideCountryIso = "DZ", overrideLanguageIso = "ar")
        assertEquals("DZ", dzDetected.countryCode)
        assertEquals("DZD", dzDetected.currencyCode)
        assertEquals(AppLanguage.AR, dzDetected.language)

        val brDetected = detectInitialLocaleAndCurrency(context, overrideCountryIso = "BR", overrideLanguageIso = "pt")
        assertEquals("BR", brDetected.countryCode)
        assertEquals("BRL", brDetected.currencyCode)
        assertEquals(AppLanguage.PT, brDetected.language)

        val coDetected = detectInitialLocaleAndCurrency(context, overrideCountryIso = "CO", overrideLanguageIso = "es")
        assertEquals("CO", coDetected.countryCode)
        assertEquals("COP", coDetected.currencyCode)
        assertEquals(AppLanguage.ES, coDetected.language)

        val usDetected = detectInitialLocaleAndCurrency(context, overrideCountryIso = "US", overrideLanguageIso = "en")
        assertEquals("US", usDetected.countryCode)
        assertEquals("USD", usDetected.currencyCode)
        assertEquals(AppLanguage.EN, usDetected.language)

        // 2. Verify all 14 Global Languages have greetings, native scripts, zero flags, and safe translation fallback
        assertEquals(14, AppLanguage.values().size)
        for (lang in AppLanguage.values()) {
            assertTrue("Language ${lang.name} greeting should not be blank", lang.greeting.isNotBlank())
            assertTrue("Language ${lang.name} subtitleLabel should not be blank", lang.subtitleLabel.isNotBlank())
            val titleTranslated = Translations.tr("settings_dialog_title", lang)
            assertTrue("Language ${lang.name} must safely resolve translations without raw keys", titleTranslated != "settings_dialog_title" && titleTranslated.isNotBlank())
        }

        // 3. Verify Appearance Studio options (6 Accent Colors, 4 Text Scales, 3 Corner Styles)
        assertEquals(6, AppAccentColorOption.values().size)
        assertEquals(4, AppTextScaleOption.values().size)
        assertEquals(3, AppCornerStyleOption.values().size)
        assertEquals(20, AppCornerStyleOption.ROUNDED.cornerRadiusDp)
        assertEquals(12, AppCornerStyleOption.SOFT.cornerRadiusDp)
        assertEquals(4, AppCornerStyleOption.CRISP.cornerRadiusDp)

        // 4. Verify $4.99 / 2 Months Paywall Pricing across AR, EN, FR, ES, PT
        assertTrue(Translations.tr("paywall_price_badge", AppLanguage.AR).contains("4.99$"))
        assertTrue(Translations.tr("paywall_price_badge", AppLanguage.EN).contains("$4.99"))
        assertTrue(Translations.tr("paywall_price_badge", AppLanguage.FR).contains("4.99$"))
        assertTrue(Translations.tr("paywall_price_badge", AppLanguage.ES).contains("$4.99"))
        assertTrue(Translations.tr("paywall_price_badge", AppLanguage.PT).contains("$4.99"))
    }

    @Test
    fun testPhase2GlobalThemeWhatsAppSettingsClimateBannerAndLocalizations() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Verify Global AppAppearanceState propagation in AgritechAppSessionState
        val sessionState = AgritechAppSessionState(
            initialOnboardingCompleted = true,
            initialTheme = AppThemeOption.NIGHT_MODE,
            initialAccentColor = AppAccentColorOption.VIOLET,
            initialTextScale = AppTextScaleOption.EXTRA_LARGE,
            initialCornerStyle = AppCornerStyleOption.CRISP,
            initialLanguage = AppLanguage.FR,
            initialCountryCode = "FR",
            initialCurrencyCode = "EUR",
            initialIsLoggedIn = true,
            initialRole = AppRole.OWNER,
            initialHasLinkedBarns = true,
            initialActiveFarmId = "farm_1",
            initialUserUid = "owner_1"
        )
        val appearance = sessionState.appearanceState
        assertTrue(appearance.isDark)
        assertEquals(AppAccentColorOption.VIOLET, appearance.accentOption)
        assertEquals(1.25f, appearance.fontScale, 0.001f)
        assertEquals(4, appearance.cornerRadiusDp)

        sessionState.updateAccentColor(context, AppAccentColorOption.OCEAN_BLUE)
        sessionState.updateCornerStyle(context, AppCornerStyleOption.ROUNDED)
        assertEquals(AppAccentColorOption.OCEAN_BLUE, sessionState.appearanceState.accentOption)
        assertEquals(20, sessionState.appearanceState.cornerRadiusDp)

        // 2. Verify WhatsApp-Style Horizontal Settings Hub tabs include MAIN_HUB
        assertEquals(4, SettingsStudioTab.values().size)
        assertEquals(SettingsStudioTab.MAIN_HUB, SettingsStudioTab.values().first())

        // 3. Verify Audit Summary Localization from Arabic to French & English
        val rawArabicAudit = "العلف: 150.0 ق | النافق: 5 | الحرارة: 28.5°"
        val frenchAudit = Translations.localizeAuditSummary(rawArabicAudit, AppLanguage.FR)
        assertTrue("French audit summary must translate Arabic labels: $frenchAudit", frenchAudit.contains("Aliment") && frenchAudit.contains("Mortalité") && !frenchAudit.contains("العلف"))

        val englishAudit = Translations.localizeAuditSummary(rawArabicAudit, AppLanguage.EN)
        assertTrue("English audit summary must translate Arabic labels: $englishAudit", englishAudit.contains("Feed") && englishAudit.contains("Mortality") && !englishAudit.contains("العلف"))

        // 4. Verify Phase 2 translation keys resolve cleanly with zero raw keys across AR, FR, EN, ES, PT, HI
        val phase2Keys = listOf(
            "settings_hub_row_appearance_title",
            "settings_hub_row_language_title",
            "settings_hub_row_region_title",
            "settings_hub_row_subscription_title",
            "climate_banner_title",
            "climate_temp_label",
            "climate_ammonia_label",
            "climate_vent_label",
            "climate_status_optimal",
            "climate_status_warning",
            "climate_status_alert",
            "quick_stat_live_birds",
            "quick_stat_feed_stock",
            "quick_stat_fcr_weight",
            "quick_stat_net_treasury",
            "buyer_name_placeholder",
            "bird_age_auto_calculated_title",
            "bird_age_auto_calculated_desc",
            "tare_mode_detailed_desc",
            "tare_mode_direct_desc",
            "weigh_net_weight_label",
            "weigh_total_birds_label",
            "weigh_avg_bird_label",
            "weigh_total_sales_label",
            "weigh_batch_entry_title",
            "weigh_price_per_kg_badge",
            "weigh_full_crates_label",
            "weigh_tare_input_label",
            "weigh_gross_input_label",
            "weigh_save_btn",
            "weigh_empty_table_msg",
            "crates_short_unit",
            "weigh_total_row_label",
            "weigh_delete_dialog_title",
            "climate_header_btn_label",
            "climate_modal_title",
            "climate_modal_subtitle",
            "treasury_expenses",
            "btn_invite_team_short"
        )
        for (lang in listOf(AppLanguage.AR, AppLanguage.FR, AppLanguage.EN, AppLanguage.ES, AppLanguage.PT, AppLanguage.HI)) {
            for (key in phase2Keys) {
                val translated = Translations.tr(key, lang)
                assertTrue("Key '$key' in ${lang.name} must not return raw key", translated != key && translated.isNotBlank())
            }
        }
    }

    @Test
    fun testPhase3PhoneContactsFinancialRequestCardsMultiFarmSwitcherAndLiveBiomassFcr() {
        // 1. Verify BUG A & BUG C translations across all languages (AR, FR, EN, ES, PT, HI)
        assertEquals("المخزون", Translations.tr("feed_stock_short", AppLanguage.AR))
        assertEquals("Stock", Translations.tr("feed_stock_short", AppLanguage.FR))
        assertEquals("Stock", Translations.tr("feed_stock_short", AppLanguage.EN))
        assertEquals("Stock", Translations.tr("feed_stock_short", AppLanguage.ES))
        assertEquals("Estoque", Translations.tr("feed_stock_short", AppLanguage.PT))
        assertEquals("إغلاق ✓", Translations.tr("btn_close", AppLanguage.AR))

        val phase3Keys = listOf(
            "feed_stock_short",
            "btn_close",
            "btn_add_phone_contact",
            "add_contact_dialog_title",
            "btn_send_financial_request",
            "fin_req_dialog_title",
            "fin_req_cat_worker_advance",
            "fin_req_cat_gas_maint",
            "fin_req_cat_feed_invoice",
            "fin_req_cat_chick_invoice",
            "fin_req_cat_vet_invoice",
            "fin_req_status_pending",
            "fin_req_status_approved",
            "multi_farm_switcher_label"
        )
        for (lang in listOf(AppLanguage.AR, AppLanguage.FR, AppLanguage.EN, AppLanguage.ES, AppLanguage.PT, AppLanguage.HI)) {
            for (key in phase3Keys) {
                val translated = Translations.tr(key, lang)
                assertTrue("Key '$key' in ${lang.name} must not return raw key", translated != key && translated.isNotBlank())
            }
        }

        // 2. Verify Phone-Number Contact System (`addPhoneContact`)
        val addedContact = AgritechMockDb.addPhoneContact(
            name = "مؤسسة أعلاف الهضاب",
            phone = "0559123456",
            roleLabel = "🌾 مورد أعلاف",
            avatarEmoji = "🌾"
        )
        assertEquals("0559123456", addedContact.phoneNumber)
        assertTrue(AgritechMockDb.chatConversations.any { it.id == addedContact.id && it.phoneNumber == "0559123456" })

        // 3. Verify Universal Financial Request Card & PENDING -> APPROVED Treasury Flow
        val initialApprovedExpenses = AgritechMockDb.transactions
            .filter { it.type == com.example.models.TransactionType.EXPENSE && it.status == "APPROVED" }
            .sumOf { it.amount }

        val reqTx = com.example.models.TransactionModel(
            id = "tx_req_test_worker_advance_1",
            farmId = "farm_1",
            cycleId = "cycle_2025_1",
            type = com.example.models.TransactionType.EXPENSE,
            category = "👷 أجرة عامل / سلفة",
            amount = 25000.0,
            currency = "دج",
            date = "اليوم 3",
            status = "PENDING"
        )
        AgritechMockDb.recordUniversalFinancialRequest(
            tx = reqTx,
            noteText = "سلفة شهر أكتوبر",
            senderId = "worker_1",
            senderName = "أحمد (مشرف العنبر)",
            senderRole = "مشرف العنبر 👷",
            chatChannelId = addedContact.id
        )

        // While PENDING, it must NOT be deducted from approved Treasury expenses
        val expensesWhilePending = AgritechMockDb.transactions
            .filter { it.type == com.example.models.TransactionType.EXPENSE && it.status == "APPROVED" }
            .sumOf { it.amount }
        assertEquals(initialApprovedExpenses, expensesWhilePending, 0.01)

        val chatMsgs = AgritechMockDb.streamChatMessages(addedContact.id)
        val postedCardMsg = chatMsgs.firstOrNull { it.linkedTransactionId == reqTx.id }
        assertTrue("Chat must contain the interactive financial request message", postedCardMsg != null)
        assertEquals(25000.0, postedCardMsg!!.requestAmount, 0.01)

        // When Owner approves ("تأكيد واعتماد الصرف ✓"), status becomes APPROVED and deducts from Treasury
        AgritechMockDb.approveVetInvoice(reqTx.id)
        val expensesAfterApproval = AgritechMockDb.transactions
            .filter { it.type == com.example.models.TransactionType.EXPENSE && it.status == "APPROVED" }
            .sumOf { it.amount }
        assertEquals(initialApprovedExpenses + 25000.0, expensesAfterApproval, 0.01)

        // 4. Verify Live Biomass FCR resolution (`resolveActualAvgBirdWeightKg`)
        val reportWithActualWeight = com.example.models.DailyReportModel(
            id = "rep_measured_weight",
            farmId = "farm_1",
            cycleId = "cycle_2025_1",
            dayNumber = 21,
            feedIntakeKg = 450.0,
            waterIntakeLiters = 900.0,
            mortalityCount = 2,
            temperature = 26.0,
            gasUnits = 6.0,
            ventilationStatus = "Good",
            generalStatus = "Normal",
            actualAvgWeightKg = 1.05
        )
        val resolvedWeight = com.example.utils.PoultryCalculators.resolveActualAvgBirdWeightKg(
            reports = listOf(reportWithActualWeight),
            cycleTransactions = emptyList(),
            cycleId = "cycle_2025_1"
        )
        assertEquals(1.05, resolvedWeight ?: 0.0, 0.001)
    }

    @Test
    fun testPhase4RoleBasedManagementHubAndCompleteLocalizationSweep() {
        // 1. Verify all 7 professional hub roles & backward-compatible role helpers
        assertEquals(7, AppRole.professionalHubRoles.size)
        assertTrue(AppRole.OWNER.isOwnerRole && AppRole.OWNER_BARN.isOwnerRole)
        assertTrue(AppRole.VETERINARIAN.isVetRole && AppRole.VET_CLINIC.isVetRole)
        assertEquals(AppRole.OWNER_BARN, AppRole.OWNER.normalizedHubRole)
        assertEquals(AppRole.VET_CLINIC, AppRole.VETERINARIAN.normalizedHubRole)

        // 2. Verify 100% genuine dictionary registration (hasKey == true) and exact translations
        assertEquals("إضافة جهة اتصال جديدة", Translations.tr("add_contact_dialog_title", AppLanguage.AR))
        assertEquals("Ajouter un contact", Translations.tr("add_contact_dialog_title", AppLanguage.FR))
        assertEquals("أدخل بيانات الشريك أو العامل أو المورد", Translations.tr("add_contact_dialog_subtitle", AppLanguage.AR))
        assertEquals("Entrez les coordonnées", Translations.tr("add_contact_dialog_subtitle", AppLanguage.FR))
        assertEquals("مورد أعلاف وذرة وصويا", Translations.tr("contact_role_feed_supplier", AppLanguage.AR))
        assertEquals("Fournisseur d'aliments", Translations.tr("contact_role_feed_supplier", AppLanguage.FR))
        assertEquals("مفرخة / بائع صيصان", Translations.tr("contact_role_chick_seller", AppLanguage.AR))
        assertEquals("Couvoir / Vendeur de poussins", Translations.tr("contact_role_chick_seller", AppLanguage.FR))
        assertEquals("حفظ جهة الاتصال ✓", Translations.tr("btn_save_contact", AppLanguage.AR))
        assertEquals("Enregistrer ✓", Translations.tr("btn_save_contact", AppLanguage.FR))
        assertEquals("🧾 إرسال طلب مالي / فاتورة", Translations.tr("btn_send_financial_request", AppLanguage.AR))
        assertEquals("🧾 Envoyer demande financière", Translations.tr("btn_send_financial_request", AppLanguage.FR))
        assertEquals("إرسال طلب مالي إلى المالك", Translations.tr("fin_req_dialog_title", AppLanguage.AR))
        assertEquals("Demande financière au propriétaire", Translations.tr("fin_req_dialog_title", AppLanguage.FR))

        val requiredHubAndDialogKeys = listOf(
            "add_contact_dialog_title",
            "add_contact_dialog_subtitle",
            "contact_role_feed_supplier",
            "contact_role_chick_seller",
            "contact_role_vet_importer",
            "btn_save_contact",
            "btn_send_financial_request",
            "fin_req_dialog_title",
            "fin_req_dialog_subtitle",
            "fin_req_type_label",
            "fin_req_cat_worker_advance",
            "fin_req_cat_gas_maint",
            "fin_req_cat_feed_invoice",
            "fin_req_cat_chick_invoice",
            "fin_req_cat_vet_invoice",
            "fin_req_amount_label",
            "fin_req_submit_btn",
            "btn_approve_vet_invoice",
            "hub_title_main",
            "hub_subtitle_select_role",
            "role_badge_owner_barn",
            "role_badge_vet_clinic",
            "role_badge_worker",
            "role_badge_feed_supplier",
            "role_badge_chick_hatchery",
            "role_badge_wholesale_trader",
            "role_badge_vet_importer",
            "vet_hub_tab_supervised_barns",
            "vet_hub_tab_pharmacy",
            "vet_hub_tab_wallet",
            "vet_hub_tab_epidemics"
        )
        for (key in requiredHubAndDialogKeys) {
            assertTrue("Key '$key' must be explicitly registered in Translations dictionary", Translations.hasKey(key))
            for (lang in listOf(AppLanguage.AR, AppLanguage.FR, AppLanguage.EN, AppLanguage.ES, AppLanguage.PT, AppLanguage.HI)) {
                val translated = Translations.tr(key, lang)
                assertTrue("Key '$key' in ${lang.name} must not be blank", translated.isNotBlank() && translated != key)
            }
        }

        // 3. Verify Vet Clinic Pharmacy Inventory stock adjustment & Epidemic Alerts
        assertTrue(AgritechMockDb.supervisedVetFarms.size >= 2)
        assertTrue(AgritechMockDb.vetPharmacyInventory.size >= 4)
        assertTrue(AgritechMockDb.vetEpidemicAlerts.size >= 3)

        val firstMedId = AgritechMockDb.vetPharmacyInventory.first().id
        val initialStock = AgritechMockDb.vetPharmacyInventory.first().stockUnits
        AgritechMockDb.adjustVetPharmacyStock(firstMedId, 5)
        assertEquals(initialStock + 5, AgritechMockDb.vetPharmacyInventory.first { it.id == firstMedId }.stockUnits)
        AgritechMockDb.adjustVetPharmacyStock(firstMedId, -5)
        assertEquals(initialStock, AgritechMockDb.vetPharmacyInventory.first { it.id == firstMedId }.stockUnits)
    }

    @Test
    fun testPhase5SurgicalFieldUiRefactoringAndInteractiveMedicalTable() {
        // 1. Verify tab_management exact translations across AR, FR, EN, ES, PT
        assertEquals("الإدارة", Translations.tr("tab_management", AppLanguage.AR))
        assertEquals("Gestion", Translations.tr("tab_management", AppLanguage.FR))
        assertEquals("Management", Translations.tr("tab_management", AppLanguage.EN))
        assertEquals("Gestión", Translations.tr("tab_management", AppLanguage.ES))
        assertEquals("Gestão", Translations.tr("tab_management", AppLanguage.PT))

        // 2. Verify all Phase 5 surgical refactoring translation keys across AR, FR, EN, ES, PT, HI
        val phase5Keys = listOf(
            "tab_management",
            "settings_profession_section_title",
            "vet_bottom_tab_barns",
            "vet_bottom_tab_treasury",
            "vet_bottom_tab_epidemics",
            "vet_bottom_tab_pharmacy",
            "vet_interactive_table_title",
            "vet_day_details_title",
            "vet_day_scheduled_vaccines_title",
            "vet_day_prescribed_meds_title",
            "vet_day_no_meds_yet",
            "vet_day_add_note_btn",
            "vet_day_save_note_btn"
        )
        for (key in phase5Keys) {
            assertTrue("Key '$key' must be explicitly registered in Translations dictionary", Translations.hasKey(key))
            for (lang in listOf(AppLanguage.AR, AppLanguage.FR, AppLanguage.EN, AppLanguage.ES, AppLanguage.PT, AppLanguage.HI)) {
                val translated = Translations.tr(key, lang)
                assertTrue("Key '$key' in ${lang.name} must not be blank or raw", translated.isNotBlank() && translated != key)
            }
        }
    }

    @Test
    fun testPhase6VetWorkspaceOnboardingAndSettingsProfessionRow() {
        // 1. Verify all Phase 6 translation keys (Vet Clinic ID Card, Onboarding, Settings Profession Row)
        val phase6Keys = listOf(
            "vet_clinic_id_card_title",
            "vet_clinic_id_card_subtitle",
            "vet_clinic_stat_barns",
            "vet_clinic_stat_treasury",
            "vet_clinic_stat_alerts",
            "open_vet_clinic_workspace_btn",
            "vet_workspace_title",
            "profession_onboarding_title",
            "profession_onboarding_subtitle",
            "profession_onboarding_confirm_btn",
            "profession_card_owner_title",
            "profession_card_owner_desc",
            "profession_card_vet_title",
            "profession_card_vet_desc",
            "profession_card_worker_title",
            "profession_card_worker_desc",
            "profession_card_feed_title",
            "profession_card_feed_desc",
            "profession_card_hatchery_title",
            "profession_card_hatchery_desc",
            "profession_card_trader_title",
            "profession_card_trader_desc",
            "profession_card_importer_title",
            "profession_card_importer_desc",
            "settings_hub_row_profession_title"
        )
        for (key in phase6Keys) {
            assertTrue("Key '$key' must be explicitly registered in Translations dictionary", Translations.hasKey(key))
            for (lang in listOf(AppLanguage.AR, AppLanguage.FR, AppLanguage.EN, AppLanguage.ES, AppLanguage.PT, AppLanguage.HI)) {
                val translated = Translations.tr(key, lang)
                assertTrue("Key '$key' in ${lang.name} must not be blank or raw", translated.isNotBlank() && translated != key)
            }
        }

        // 2. Verify sevenProfessionCatalog has all 7 distinct roles
        assertEquals(7, sevenProfessionCatalog.size)
        assertEquals(
            setOf(
                AppRole.OWNER_BARN,
                AppRole.VET_CLINIC,
                AppRole.WORKER,
                AppRole.FEED_SUPPLIER,
                AppRole.CHICK_HATCHERY,
                AppRole.WHOLESALE_TRADER,
                AppRole.VET_IMPORTER
            ),
            sevenProfessionCatalog.map { it.role }.toSet()
        )

        // 3. Verify First-Launch Profession Onboarding persistence via SaasPreferencesManager & AgritechAppSessionState
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertTrue(!SaasPreferencesManager.hasSelectedInitialProfession(context))

        val sessionState = AgritechAppSessionState(
            initialOnboardingCompleted = true,
            initialTheme = AppThemeOption.ICE_BLUE,
            initialLanguage = AppLanguage.AR,
            initialCountryCode = "DZ",
            initialCurrencyCode = "DZD",
            initialIsLoggedIn = true,
            initialRole = AppRole.OWNER_BARN,
            initialHasLinkedBarns = true,
            initialActiveFarmId = "farm_1",
            initialUserUid = "owner_1",
            initialProfessionSelected = SaasPreferencesManager.hasSelectedInitialProfession(context)
        )
        assertTrue(!sessionState.hasSelectedInitialProfession)

        sessionState.completeInitialProfessionSelection(context, AppRole.VET_CLINIC)
        assertTrue(sessionState.hasSelectedInitialProfession)
        assertEquals(AppRole.VET_CLINIC, sessionState.currentRole)
        assertTrue(SaasPreferencesManager.hasSelectedInitialProfession(context))
        assertEquals(AppRole.VET_CLINIC, SaasPreferencesManager.getSavedUserRole(context))
    }

    @Test
    fun testPhase7RealHumanPersonaInspectorAndFiveFieldGaps() {
        // 1. Verify all Persona Inspector & 5 Field Gap translation keys across all languages (Zero Raw Keys)
        val phase7Keys = listOf(
            "persona_inspector_btn",
            "persona_inspector_title",
            "persona_inspector_subtitle",
            "persona_mode_a_btn",
            "persona_mode_b_btn",
            "persona_sim_start_btn",
            "persona_sim_pause_btn",
            "persona_sim_speed_1x",
            "persona_sim_speed_10x",
            "persona_sim_step_next_day_btn",
            "persona_sim_run_all_40_btn",
            "persona_sim_reset_btn",
            "persona_sim_day_progress",
            "persona_math_check_title",
            "persona_math_feed_match",
            "persona_math_birds_match",
            "persona_math_treasury_match",
            "persona_math_pharmacy_match",
            "persona_critique_section_title",
            "remaining_feed_stock_quintal_label",
            "remaining_feed_stock_low_warning",
            "btn_quick_restock_feed_40q",
            "vet_withdrawal_period_badge",
            "vet_withdrawal_period_desc",
            "vet_pharmacy_auto_deduct_note",
            "trader_transport_mortality_label",
            "trader_transport_mortality_hint",
            "trader_net_payable_after_mortality",
            "hatchery_bonus_2pct_label",
            "importer_restock_vet_pharmacy_btn"
        )
        for (key in phase7Keys) {
            assertTrue("Key '$key' must be explicitly registered in Translations dictionary", Translations.hasKey(key))
            for (lang in listOf(AppLanguage.AR, AppLanguage.FR, AppLanguage.EN, AppLanguage.ES, AppLanguage.PT, AppLanguage.HI)) {
                val translated = Translations.tr(key, lang)
                assertTrue("Key '$key' in ${lang.name} must not be blank or raw", translated.isNotBlank() && translated != key)
            }
        }

        // 2. Verify Vet Pharmacy automatic stock deduction when issuing a medicine invoice
        val enroBefore = AgritechMockDb.vetPharmacyInventory.first { it.id == "pharm_3" }.stockUnits
        val testTx = com.example.models.TransactionModel(
            id = "tx_vet_test_auto_deduct",
            farmId = "farm_1",
            cycleId = "cycle_2026_01",
            type = com.example.models.TransactionType.EXPENSE,
            category = "💊 فاتورة دواء: إنروفلوكساسين",
            amount = 15000.0,
            currency = "دج",
            date = "اليوم 8",
            status = "PENDING"
        )
        val testVetNote = com.example.models.VetNoteModel(
            id = "vn_test_auto_deduct",
            farmId = "farm_1",
            cycleId = "cycle_2026_01",
            dayNumber = 8,
            medicineName = "إنروفلوكساسين 20% + كولستين سائل",
            dosage = "1 مل / لتر",
            note = "فترة السحب الطبية: 5 أيام قبل البيع",
            isUrgent = true
        )
        AgritechMockDb.recordVetInvoiceAndPrescription(testTx, testVetNote)
        val enroAfter = AgritechMockDb.vetPharmacyInventory.first { it.id == "pharm_3" }.stockUnits
        assertEquals(enroBefore - 1, enroAfter)

        // 3. Verify 40-Day Live Persona Simulator & Math Reconciliation Engine
        com.example.utils.PersonaSimulatorEngine.ensureInitialized()
        val initialLogsCount = com.example.utils.PersonaSimulatorEngine.critiqueLogs.size
        assertTrue("Initial human persona critique logs must include all 7 professions", initialLogsCount >= 7)

        // Advance simulation to Day 40 and verify math reconciliation & all 7 roles represented
        com.example.utils.PersonaSimulatorEngine.runFull40DaysInstant()
        assertEquals(40, com.example.utils.PersonaSimulatorEngine.currentSimDay)
        val mathStatus = com.example.utils.PersonaSimulatorEngine.evaluateMathReconciliation()
        assertTrue("Feed math reconciliation must match", mathStatus.feedMatches)
        assertTrue("Live birds math reconciliation must match", mathStatus.liveBirdsMatches)
        assertTrue("Treasury expenses reconciliation must match", mathStatus.treasuryInvoicesMatch)
        assertTrue("Vet pharmacy stock deduction must be tracked", mathStatus.pharmacyDeductionActive)

        val rolesInCritiques = com.example.utils.PersonaSimulatorEngine.critiqueLogs.map { it.role }.toSet()
        assertTrue(rolesInCritiques.containsAll(
            listOf(
                AppRole.OWNER_BARN,
                AppRole.VET_CLINIC,
                AppRole.WORKER,
                AppRole.FEED_SUPPLIER,
                AppRole.CHICK_HATCHERY,
                AppRole.WHOLESALE_TRADER,
                AppRole.VET_IMPORTER
            )
        ))

        // Restore zero-state for other tests
        com.example.utils.PersonaSimulatorEngine.resetSimulationToDay7()
        AgritechMockDb.farms.clear()
        AgritechMockDb.cycles.clear()
    }

    @Test
    fun testPhase8SixSurgicalFieldFixes() {
        // 1. Reactive Feed Stock Counter: Baseline 80 Q (8000 kg), 7 Q consumed -> 73 Q remaining; +40 Q purchase -> 113 Q remaining & 120 Q total purchased
        assertEquals(8000.0, PoultryCalculators.INITIAL_BASELINE_FEED_KG, 0.001)
        val defaultCycle = CycleModel(id = "cycle_test_80q", farmId = "farm_1", totalBirdsPlaced = 10000)
        assertEquals(8000.0, defaultCycle.feedInventoryKg, 0.001)

        val feedPurchaseTx = TransactionModel(
            id = "tx_feed_40q_test",
            farmId = "farm_1",
            cycleId = "cycle_test_80q",
            type = TransactionType.EXPENSE,
            category = "🌾 شراء الأعلاف (40 ق)",
            amount = 320000.0,
            currency = "دج",
            date = "اليوم 7",
            notes = "40 قنطار"
        )
        assertTrue(PoultryCalculators.isFeedTransaction(feedPurchaseTx))
        assertEquals(4000.0, PoultryCalculators.extractFeedKgFromTransaction(feedPurchaseTx), 0.001)

        val consumedKg = 700.0 // 7 Q consumed
        val remainingBeforePurchaseQ = (PoultryCalculators.INITIAL_BASELINE_FEED_KG - consumedKg) / 100.0
        assertEquals(73.0, remainingBeforePurchaseQ, 0.001)
        val remainingAfterPurchaseQ = (PoultryCalculators.INITIAL_BASELINE_FEED_KG + PoultryCalculators.extractFeedKgFromTransaction(feedPurchaseTx) - consumedKg) / 100.0
        assertEquals(113.0, remainingAfterPurchaseQ, 0.001)

        // 2. Verify all new weighing, crate-count transformation, delete dialog, and withdrawal translation keys across AR, FR, EN, ES, PT, HI
        val phase8Keys = listOf(
            "days_unit",
            "weigh_delete_dialog_msg",
            "weigh_delete_full_row",
            "weigh_crate_count_input_label",
            "weigh_clear_table_btn",
            "weigh_toggle_tare_crate_hint",
            "withdrawal_safety_alert_banner",
            "feed_initial_baseline_designation"
        )
        for (key in phase8Keys) {
            assertTrue("Key '$key' must be explicitly registered in Translations dictionary", Translations.hasKey(key))
            for (lang in listOf(AppLanguage.AR, AppLanguage.FR, AppLanguage.EN, AppLanguage.ES, AppLanguage.PT, AppLanguage.HI)) {
                val translated = Translations.tr(key, lang)
                assertTrue("Key '$key' in ${lang.name} must not return raw key", translated.isNotBlank() && translated != key)
            }
        }

        // 3. Verify {days} interpolation in bird_age_auto_calculated_title and withdrawal_safety_alert_banner
        for (lang in listOf(AppLanguage.AR, AppLanguage.FR, AppLanguage.EN, AppLanguage.ES, AppLanguage.PT)) {
            val ageTitle = Translations.tr("bird_age_auto_calculated_title", lang).replace("{days}", "35")
            assertTrue("Age title in ${lang.name} must replace {days} with 35: $ageTitle", ageTitle.contains("35") && !ageTitle.contains("{days}"))

            val withdrawalBanner = Translations.tr("withdrawal_safety_alert_banner", lang).replace("{days}", "5")
            assertTrue("Withdrawal banner in ${lang.name} must replace {days} with 5: $withdrawalBanner", withdrawalBanner.contains("5") && !withdrawalBanner.contains("{days}"))
        }

        // 4. Verify Antibiotic Withdrawal Period Safety Calculator
        val recentAntibioticNote = VetNoteModel(
            id = "vn_withdrawal_test",
            farmId = "farm_1",
            cycleId = "cycle_test_80q",
            dayNumber = 32,
            medicineName = "مضاد حيوي إنروفلوكساسين 20%",
            dosage = "1 مل / لتر",
            note = "علاج تنفسي",
            isUrgent = true
        )
        val remainingDaysAtDay34 = PoultryCalculators.calculateWithdrawalDaysRemaining(
            vetNotes = listOf(recentAntibioticNote),
            currentCycleDay = 34,
            maxReportDay = 34
        )
        assertEquals(5, remainingDaysAtDay34)

        val remainingDaysAtDay41 = PoultryCalculators.calculateWithdrawalDaysRemaining(
            vetNotes = listOf(recentAntibioticNote),
            currentCycleDay = 41,
            maxReportDay = 41
        )
        assertEquals(0, remainingDaysAtDay41)

        // 5. Verify RTL-safe greetings and localized mock strings
        assertEquals("Hola", AppLanguage.ES.greeting)
        assertEquals("Olá", AppLanguage.PT.greeting)
        assertEquals(Translations.tr("farm_1_name", AppLanguage.EN), Translations.getLocalizedFarmName("farm_custom", AppLanguage.EN, "عنبر البركة النموذجي"))
        assertEquals("Galpão Al-Baraka 1", Translations.getLocalizedFarmName("farm_custom", AppLanguage.PT, "عنبر البركة 1"))
        assertEquals("Aliment Croissance", Translations.localizeFeedDesignation("علف نامي", AppLanguage.FR))
        assertEquals(Translations.tr("tx_feed", AppLanguage.EN), Translations.localizeFeedDesignation("شراء علف", AppLanguage.EN))
    }

    @Test
    fun testPhase9Calibrated40DayAlgerianBroilerEconomics() {
        try {
            // 1. Verify daily feed curve anchors & 40-day cumulative sum = 390.0 Quintals
            assertEquals(1.8, PoultryCalculators.getCalibratedDailyFeedQuintals(1), 0.001)
            assertEquals(5.5, PoultryCalculators.getCalibratedDailyFeedQuintals(10), 0.001)
            assertEquals(9.5, PoultryCalculators.getCalibratedDailyFeedQuintals(20), 0.001)
            assertEquals(13.5, PoultryCalculators.getCalibratedDailyFeedQuintals(30), 0.001)
            assertEquals(16.5, PoultryCalculators.getCalibratedDailyFeedQuintals(35), 0.001)
            assertEquals(16.5, PoultryCalculators.getCalibratedDailyFeedQuintals(40), 0.001)

            val sum40DayFeedQ = (1..40).sumOf { PoultryCalculators.getCalibratedDailyFeedQuintals(it) }
            assertEquals(390.0, sum40DayFeedQ, 0.01)

            // 2. Verify daily water curve anchors & 40-day cumulative sum = 190,000 Liters
            assertEquals(1200.0, PoultryCalculators.getCalibratedDailyWaterLiters(1), 0.001)
            assertEquals(4500.0, PoultryCalculators.getCalibratedDailyWaterLiters(20), 0.001)
            assertEquals(7500.0, PoultryCalculators.getCalibratedDailyWaterLiters(40), 0.001)

            val sum40DayWaterLiters = (1..40).sumOf { PoultryCalculators.getCalibratedDailyWaterLiters(it) }
            assertEquals(190000.0, sum40DayWaterLiters, 1.0)

            // 3. Run full 40-day simulation and verify barn analytics & Algerian broiler economics
            PersonaSimulatorEngine.runFull40DaysInstant()
            assertEquals(40, PersonaSimulatorEngine.currentSimDay)

            val primaryFarm = AgritechMockDb.farms.first()
            val primaryCycle = AgritechMockDb.cycles.first { it.farmId == primaryFarm.id }
            val farmReports = AgritechMockDb.dailyReports.filter { it.farmId == primaryFarm.id }
            val farmExpenses = AgritechMockDb.transactions.filter { it.farmId == primaryFarm.id && it.type == TransactionType.EXPENSE }

            assertEquals(40, farmReports.size)

            // Total feed consumed = 390 Q (39,000 kg), remaining feed stock = 20 Q (within 10-30 Q)
            val totalFeedKg = farmReports.sumOf { it.feedIntakeKg }
            assertEquals(39000.0, totalFeedKg, 1.0)
            val remainingStockQ = primaryCycle.feedInventoryKg / 100.0
            assertTrue("Remaining feed stock ($remainingStockQ Q) must be between 10 Q and 30 Q", remainingStockQ in 10.0..30.0)
            assertEquals(20.0, remainingStockQ, 0.1)

            // Mortality = 380 birds (4.75%, within 3%..8%), Live birds = 7,620 (95.25% livability)
            val totalMortality = farmReports.sumOf { it.mortalityCount }
            assertEquals(380, totalMortality)
            val liveBirds = PoultryCalculators.calculateLiveBirds(primaryCycle.totalBirdsPlaced, totalMortality, primaryCycle.cumulativeBirdsSold)
            assertEquals(7620, liveBirds)

            // Final bird weight at Day 40 = 2.92 kg (within 2.85..2.95 kg), FCR = ~1.75 (within 1.72..1.76)
            val finalAvgWeightKg = PoultryCalculators.resolveActualAvgBirdWeightKg(farmReports, emptyList(), primaryCycle.id) ?: 0.0
            assertTrue("Final bird weight ($finalAvgWeightKg kg) must be in 2.85..2.95 kg", finalAvgWeightKg in 2.85..2.95)
            val fcr = PoultryCalculators.calculateFcr(
                totalFeedKg = totalFeedKg,
                initialBirds = primaryCycle.totalBirdsPlaced,
                totalMortality = totalMortality,
                birdsSold = 0,
                ageInDays = 40,
                actualAvgWeightKg = finalAvgWeightKg
            )
            assertTrue("40-day FCR ($fcr) must be between 1.72 and 1.76", fcr in 1.72..1.76)

            // Total 40-day expenses = 4,190,000 DZD -> Cost per bird = ~550 DZD (549.87 DZD), Cost per kg = ~188.3 DZD
            val totalExpensesDzd = farmExpenses.sumOf { it.amount }
            assertEquals(4190000.0, totalExpensesDzd, 1.0)
            val costPerBird = PoultryCalculators.calculateCostPerBird(totalExpensesDzd, liveBirds, 0)
            assertEquals(550.0, costPerBird, 1.0)
            val totalBiomassKg = PoultryCalculators.calculateTotalProducedBiomassKg(liveBirds, finalAvgWeightKg, 0.0)
            val costPerKg = PoultryCalculators.calculateCostPerKg(totalExpensesDzd, totalBiomassKg)
            assertEquals(189.0, costPerKg, 2.0)

            // Verify Wholesale Trader Yassine chat message & market post request 2.8 - 3.0 kg
            val yassineMsg = AgritechMockDb.chatMessages.first { it.id == "msg_wb1" }
            assertTrue(yassineMsg.text.contains("2.8 - 3.0 كغ"))
        } finally {
            PersonaSimulatorEngine.resetSimulationToDay7()
            AgritechMockDb.farms.clear()
            AgritechMockDb.cycles.clear()
        }
    }

    @Test
    fun testPhase10FiveSurgicalRefinementsAndWlioVetClinicalProfile() {
        try {
            // 1. Verify "total_feed" and all new Wlio-style Vet Clinical Profile & Weighing keys across AR, FR, EN, ES, PT, HI
            assertEquals("إجمالي المستهلك", Translations.tr("total_feed", AppLanguage.AR))
            assertEquals("Total consommé", Translations.tr("total_feed", AppLanguage.FR))
            assertEquals("Total Consumed", Translations.tr("total_feed", AppLanguage.EN))
            assertEquals("Total consumido", Translations.tr("total_feed", AppLanguage.ES))
            assertEquals("Total consumido", Translations.tr("total_feed", AppLanguage.PT))

            val phase10Keys = listOf(
                "total_feed",
                "vet_filter_all",
                "vet_filter_urgent",
                "vet_filter_vaccines_due",
                "vet_filter_withdrawal",
                "vet_clinical_profile_title",
                "vet_status_stable",
                "vet_status_monitoring",
                "vet_metric_breed",
                "vet_metric_age",
                "vet_metric_livability",
                "vet_clinical_summary_title",
                "vet_btn_message_owner",
                "vet_btn_issue_rx_invoice",
                "weigh_toggle_chip_to_crates",
                "weigh_toggle_chip_to_tare"
            )
            for (key in phase10Keys) {
                assertTrue("Key '$key' must be explicitly registered in Translations dictionary", Translations.hasKey(key))
                for (lang in listOf(AppLanguage.AR, AppLanguage.FR, AppLanguage.EN, AppLanguage.ES, AppLanguage.PT, AppLanguage.HI)) {
                    val translated = Translations.tr(key, lang)
                    assertTrue("Key '$key' in ${lang.name} must not return raw key", translated.isNotBlank() && translated != key)
                }
            }

            // 2. Simulate creating a custom barn first (which inserts tx_chicks_cycle_custom for 640,000 DZD)
            // and then running Mode A step-by-step from Day 7 to Day 40 -> must reconcile to 4,190,000 DZD and 20 Q remaining feed stock!
            AgritechMockDb.farms.clear()
            AgritechMockDb.cycles.clear()
            val customFarm = FarmModel("farm_custom_999", "عنبر النور", "المدية", 8000, "worker_1")
            val customCycle = CycleModel(
                id = "cycle_custom_999",
                farmId = customFarm.id,
                startDate = "2026-10-01",
                totalBirdsPlaced = 8000,
                chickPrice = 80.0,
                feedInventoryKg = 11000.0
            )
            AgritechMockDb.farms.add(customFarm)
            AgritechMockDb.cycles.add(customCycle)
            AgritechMockDb.transactions.add(
                TransactionModel(
                    id = "tx_chicks_cycle_custom_999",
                    farmId = customFarm.id,
                    cycleId = customCycle.id,
                    type = TransactionType.EXPENSE,
                    category = "شراء الصيصان",
                    amount = 640000.0,
                    currency = "DZD",
                    date = "اليوم 1"
                )
            )

            // Reset & step through Mode A day-by-day up to Day 40
            PersonaSimulatorEngine.resetSimulationToDay7()
            while (PersonaSimulatorEngine.currentSimDay < 40) {
                PersonaSimulatorEngine.stepSimulationOneDay()
            }
            assertEquals(40, PersonaSimulatorEngine.currentSimDay)

            val farmExpenses = AgritechMockDb.transactions.filter {
                it.farmId == customFarm.id && it.type == TransactionType.EXPENSE && it.status == "APPROVED"
            }
            val totalExpensesDzd = farmExpenses.sumOf { it.amount }
            assertEquals(4190000.0, totalExpensesDzd, 1.0)

            val updatedCycle = AgritechMockDb.cycles.first { it.id == customCycle.id }
            assertEquals(20.0, updatedCycle.feedInventoryKg / 100.0, 0.1)
        } finally {
            PersonaSimulatorEngine.resetSimulationToDay7()
            AgritechMockDb.farms.clear()
            AgritechMockDb.cycles.clear()
        }
    }

    @Test
    fun testPhase11QuickWeighingRestorationTraderSlaughterhouseDoaAndRbacAudit() {
        try {
            // 1. Verify QuickWeighingScreen single checkmark ("✓ حفظ") and non-duplicated (كغ) labels
            assertEquals("✓ حفظ", Translations.tr("weigh_save_btn", AppLanguage.AR))
            assertEquals("✓ Save", Translations.tr("weigh_save_btn", AppLanguage.EN))
            assertEquals("✓ Valider", Translations.tr("weigh_save_btn", AppLanguage.FR))
            assertEquals("✓ Guardar", Translations.tr("weigh_save_btn", AppLanguage.ES))
            assertEquals("✓ Salvar", Translations.tr("weigh_save_btn", AppLanguage.PT))

            val tareHeaderAr = "🧺 ${Translations.tr("weigh_tare_input_label", AppLanguage.AR)}"
            val grossHeaderAr = "📦 ${Translations.tr("weigh_gross_input_label", AppLanguage.AR)}"
            assertEquals("🧺 الفارغ (كغ)", tareHeaderAr)
            assertEquals("📦 الممتلئ (كغ)", grossHeaderAr)

            val phase11Keys = listOf(
                "weigh_save_btn",
                "weigh_save_btn_short",
                "trader_transport_mortality_label",
                "trader_transport_mortality_hint",
                "trader_net_payable_after_mortality",
                "trader_notify_farmer_slaughterhouse_doa_btn",
                "trader_doa_notification_sent_banner",
                "trader_doa_settlement_prefix"
            )
            for (key in phase11Keys) {
                assertTrue("Key '$key' must be registered in Translations", Translations.hasKey(key))
                for (lang in listOf(AppLanguage.AR, AppLanguage.FR, AppLanguage.EN, AppLanguage.ES, AppLanguage.PT, AppLanguage.HI)) {
                    val translated = Translations.tr(key, lang)
                    assertTrue("Key '$key' in ${lang.name} must not return raw key", translated.isNotBlank() && translated != key)
                }
            }

            // 2. Verify Wholesale Trader Slaughterhouse Arrival DOA Notification workflow
            val doaTx = AgritechMockDb.recordSlaughterhouseDoaSettlementNotification(
                deadBirdsCount = 5,
                farmId = "farm_1",
                cycleId = "cycle_2026_01",
                avgBirdWeightKg = 2.35,
                pricePerKgDzd = 380.0,
                currency = "دج"
            )
            assertTrue(doaTx != null)
            assertEquals("PENDING", doaTx!!.status)
            assertTrue(doaTx.category.contains("إشعار من التاجر: تسجيل 5 طير نافق عند الوصول للمذبح"))
            assertTrue(
                AgritechMockDb.chatMessages.any {
                    it.cycleId == "conv_wholesale_buyer" &&
                        it.text.contains("إشعار من التاجر: تسجيل 5 طير نافق عند الوصول للمذبح")
                }
            )

            // 3. Verify OWASP MASVS Role-Based Access Control (RBAC), Immutable Sale Invoices, Idempotency & Input Sanitization
            assertEquals(true, AgritechFirestoreRepository.canDeleteOrModifyApprovedTreasuryRecord(AppRole.OWNER))
            assertEquals(true, AgritechFirestoreRepository.canDeleteOrModifyApprovedTreasuryRecord(AppRole.OWNER_BARN))
            assertEquals(false, AgritechFirestoreRepository.canDeleteOrModifyApprovedTreasuryRecord(AppRole.WORKER))
            assertEquals(false, AgritechFirestoreRepository.canDeleteOrModifyApprovedTreasuryRecord(AppRole.VETERINARIAN))
            assertEquals(false, AgritechFirestoreRepository.canDeleteOrModifyApprovedTreasuryRecord(AppRole.VET_CLINIC))
            assertEquals(false, AgritechFirestoreRepository.canDeleteOrModifyApprovedTreasuryRecord(AppRole.WHOLESALE_TRADER))

            // Verify finalized sale invoice (INV-...) immutability and RBAC protection
            val finalizedInvoice = SalesInvoiceModel(
                invoiceNumber = "INV-1-0099",
                farmName = "عنبر الخيرات والبركة 1",
                cycleId = "cycle_2026_01",
                dateText = "اليوم 40",
                birdAgeDays = 40,
                buyerName = "ياسين",
                truckPlate = "01234-116-16",
                totalCrates = 50,
                birdsPerCrate = 10,
                totalBirdsSold = 500,
                totalGrossKg = 1500.0,
                totalTareKg = 100.0,
                totalNetKg = 1400.0,
                avgBirdWeightKg = 2.8,
                pricePerKg = 380.0,
                totalAmount = 532000.0,
                currency = "دج",
                tareWeightsCount = 10,
                grossWeightsCount = 10
            )
            val finalizedSaleTx = TransactionModel(
                id = "tx_sale_immutable_99",
                farmId = "farm_1",
                cycleId = "cycle_2026_01",
                type = TransactionType.INCOME,
                category = "بيع الدواجن",
                amount = 532000.0,
                currency = "دج",
                date = "اليوم 40",
                invoiceData = finalizedInvoice,
                status = "APPROVED"
            )
            AgritechMockDb.transactions.add(finalizedSaleTx)

            assertEquals(false, AgritechMockDb.deleteTransactionWithRbac("tx_sale_immutable_99", AppRole.WORKER))
            assertEquals(false, AgritechMockDb.deleteTransactionWithRbac("tx_sale_immutable_99", AppRole.VETERINARIAN))
            assertEquals(false, AgritechMockDb.deleteTransactionWithRbac("tx_sale_immutable_99", AppRole.OWNER))
            assertEquals(false, AgritechMockDb.modifySalesInvoiceWithRbac("tx_sale_immutable_99", finalizedInvoice.copy(totalAmount = 100.0), AppRole.WORKER))
            assertEquals(false, AgritechMockDb.modifySalesInvoiceWithRbac("tx_sale_immutable_99", finalizedInvoice.copy(totalAmount = 100.0), AppRole.OWNER))

            // Verify Worker/Vet cannot approve pending transactions, and Owner approval is idempotent
            assertEquals(false, AgritechMockDb.approveVetInvoice(doaTx.id, actorRole = AppRole.WORKER))
            assertEquals(false, AgritechMockDb.approveVetInvoice(doaTx.id, actorRole = AppRole.VETERINARIAN))
            assertEquals(true, AgritechMockDb.approveVetInvoice(doaTx.id, actorRole = AppRole.OWNER))
            assertEquals(false, AgritechMockDb.approveVetInvoice(doaTx.id, actorRole = AppRole.OWNER))

            // Verify numeric input sanitization against negative, NaN, and Infinity
            assertEquals(0.0, AgritechFirestoreRepository.sanitizeNonNegativeDouble(-45.0), 0.001)
            assertEquals(0.0, AgritechFirestoreRepository.sanitizeNonNegativeDouble(Double.NaN), 0.001)
            assertEquals(0.0, AgritechFirestoreRepository.sanitizeNonNegativeDouble(Double.POSITIVE_INFINITY), 0.001)
            assertEquals(0, AgritechFirestoreRepository.sanitizeNonNegativeInt(-12))
        } finally {
            PersonaSimulatorEngine.resetSimulationToDay7()
            AgritechMockDb.farms.clear()
            AgritechMockDb.cycles.clear()
        }
    }
}
