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
    fun testSurgical7UpdatesVerification() {
        // 1. Verify all new translation keys for the 7 surgical updates across AR, FR, EN, ES, PT, HI
        val surgical7Keys = listOf(
            "ai_vet_coming_soon_title",
            "ai_vet_coming_soon_subtitle",
            "personal_qr_sheet_title",
            "personal_qr_tab_my_code",
            "personal_qr_tab_scan",
            "personal_qr_whatsapp_subtitle",
            "invoice_sig_dialog_title",
            "invoice_sig_deduction_notice",
            "invoice_sig_pad_label",
            "invoice_sig_clear_btn",
            "invoice_sig_confirm_btn",
            "sig_clear_short",
            "sig_breeder_signed_badge",
            "sig_buyer_signed_badge",
            "med_drinking_badge_prefix"
        )
        for (key in surgical7Keys) {
            assertTrue("Key '$key' must be registered in Translations", Translations.hasKey(key))
            for (lang in listOf(AppLanguage.AR, AppLanguage.FR, AppLanguage.EN, AppLanguage.ES, AppLanguage.PT, AppLanguage.HI)) {
                val translated = Translations.tr(key, lang)
                assertTrue("Key '$key' in ${lang.name} must not be blank or raw", translated.isNotBlank() && translated != key)
            }
        }

        // 2. Verify WhatsApp-style Personal Contact QR parsing
        val contactPayload = "MYBUSINESS_CONTACT|name=د. مراد تكوتي|role=طبيب بيطري|phone=0550112233"
        val parsedContact = parsePersonalContactQr(contactPayload)
        assertTrue("Personal contact QR payload must parse cleanly", parsedContact != null)
        assertEquals("د. مراد تكوتي", parsedContact!!.name)
        assertEquals("طبيب بيطري", parsedContact.roleLabel)
        assertEquals("0550112233", parsedContact.phone)

        // 3. Verify Shipment Info {days} placeholder replacement leaves no raw {days} token
        for (lang in listOf(AppLanguage.AR, AppLanguage.FR, AppLanguage.EN, AppLanguage.ES, AppLanguage.PT)) {
            val formattedAge = Translations.tr("bird_age_auto_calculated_title", lang).replace("{days}", "36")
            assertTrue("Formatted age in ${lang.name} must contain 36 and no {days}: $formattedAge", formattedAge.contains("36") && !formattedAge.contains("{days}"))
        }

        // 4. Verify CycleModel default feedInventoryKg starts at 0.0 (0 ق)
        val newCycle = CycleModel(
            id = "cycle_zero_feed_test",
            farmId = "farm_zero_feed_test",
            startDate = "2025-10-26",
            totalBirdsPlaced = 5000
        )
        assertEquals(0.0, newCycle.feedInventoryKg, 0.001)

        // 5. Verify DailyReportModel medicineGiven & medicineSummary confirmation and MedicineOrdersLedger keys
        val medLedgerKeys = listOf(
            "confirm_medicine_water_today_label",
            "medicine_water_name_input_label",
            "badge_day_med_pill",
            "medicine_orders_ledger_title",
            "total_medicine_expenses_label",
            "total_prescriptions_count_label",
            "th_med_day",
            "th_med_prescription_dosage",
            "th_med_status",
            "th_med_total_price",
            "med_status_approved_short",
            "med_status_pending_short",
            "no_medicine_orders_yet",
            "prescriptions_unit_short"
        )
        for (key in medLedgerKeys) {
            assertTrue("Key '$key' must be registered in Translations", Translations.hasKey(key))
            for (lang in listOf(AppLanguage.AR, AppLanguage.FR, AppLanguage.EN, AppLanguage.ES, AppLanguage.PT, AppLanguage.HI)) {
                val translated = Translations.tr(key, lang)
                assertTrue("Key '$key' in ${lang.name} must not be blank or raw", translated.isNotBlank() && translated != key)
            }
        }

        val medConfirmedReport = DailyReportModel(
            id = "rep_med_given_d10",
            farmId = "farm_1",
            cycleId = "cycle_2025_1",
            dayNumber = 10,
            feedIntakeKg = 350.0,
            waterIntakeLiters = 700.0,
            mortalityCount = 1,
            temperature = 29.0,
            medicineGiven = true,
            medicineSummary = "إنروفلوكساسين 10% + فيتامين C"
        )
        val savedMedReport = AgritechMockDb.upsertDailyReportWithAudit(medConfirmedReport)
        assertTrue(savedMedReport.medicineGiven)
        assertEquals("إنروفلوكساسين 10% + فيتامين C", savedMedReport.medicineSummary)
    }

    @Test
    fun testSurgical4ReliabilityFixesVerification() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Verify Accurate Feed Quintal Parsing (No fabricated amount / 8000 fallback)
        val txExplicitQuintals = TransactionModel(
            id = "tx_feed_explicit_q",
            farmId = "farm_1",
            cycleId = "cycle_2026_01",
            type = TransactionType.EXPENSE,
            category = "🌾 علف ناهي",
            amount = 500000.0,
            currency = "دج",
            date = "اليوم 10",
            quantityQuintals = 18.5
        )
        assertEquals(1850.0, parseFeedTransaction(txExplicitQuintals, AppLanguage.AR).estimatedKg, 0.001)

        val txNoQuantity = TransactionModel(
            id = "tx_feed_no_qty",
            farmId = "farm_1",
            cycleId = "cycle_2026_01",
            type = TransactionType.EXPENSE,
            category = "🌾 فاتورة علف بدون وزن",
            amount = 320000.0,
            currency = "دج",
            date = "اليوم 11",
            quantityQuintals = 0.0
        )
        assertEquals(0.0, parseFeedTransaction(txNoQuantity, AppLanguage.AR).estimatedKg, 0.001)

        // 2. Verify Chat Financial Request Signed Approval + Non-Duplicating Feed Stock Sync
        val testCycle = CycleModel(
            id = "cycle_chat_feed_sync",
            farmId = "farm_chat_sync",
            totalBirdsPlaced = 6000,
            feedInventoryKg = 500.0
        )
        AgritechMockDb.cycles.add(testCycle)

        val chatFeedReqTx = TransactionModel(
            id = "tx_chat_feed_req_1",
            farmId = "farm_chat_sync",
            cycleId = "cycle_chat_feed_sync",
            type = TransactionType.EXPENSE,
            category = "🌾 فاتورة علف نامي (20 ق)",
            amount = 160000.0,
            currency = "دج",
            date = "اليوم 8",
            status = "PENDING",
            quantityQuintals = 20.0
        )
        AgritechMockDb.recordUniversalFinancialRequest(
            tx = chatFeedReqTx,
            noteText = "شحنة 20 قنطار علف نامي",
            senderId = "trader_1",
            senderName = "كمال - تاجر أعلاف",
            senderRole = "تاجر أعلاف 🌾",
            chatChannelId = "conv_feed_trader"
        )

        val sigValue = "Signed_By_Owner_1730000000000"
        AgritechMockDb.approveVetInvoice(
            transactionId = "tx_chat_feed_req_1",
            signatureTag = sigValue
        )
        val approvedChatTx = AgritechMockDb.transactions.first { it.id == "tx_chat_feed_req_1" }
        val approvedChatMsg = AgritechMockDb.chatMessages.first { it.linkedTransactionId == "tx_chat_feed_req_1" }
        val updatedCycleAfterFirstApprove = AgritechMockDb.cycles.first { it.id == "cycle_chat_feed_sync" }

        assertEquals("APPROVED", approvedChatTx.status)
        assertEquals(sigValue, approvedChatTx.approvedBySignature)
        assertEquals("APPROVED", approvedChatMsg.status)
        assertEquals(2500.0, updatedCycleAfterFirstApprove.feedInventoryKg, 0.001)

        // Calling approveVetInvoice a second time for the same transaction MUST NOT duplicate feedInventoryKg
        AgritechMockDb.approveVetInvoice(
            transactionId = "tx_chat_feed_req_1",
            signatureTag = sigValue
        )
        val updatedCycleAfterSecondApprove = AgritechMockDb.cycles.first { it.id == "cycle_chat_feed_sync" }
        assertEquals(2500.0, updatedCycleAfterSecondApprove.feedInventoryKg, 0.001)

        // 3. Verify LocalFarmPersistence saveAll / loadIntoMockDb / clear on explicit logout
        LocalFarmPersistence.saveAll(context)
        assertTrue(LocalFarmPersistence.hasSavedData(context))

        val session = AgritechAppSessionState(
            initialOnboardingCompleted = true,
            initialTheme = AppThemeOption.ICE_BLUE,
            initialLanguage = AppLanguage.AR,
            initialCountryCode = "DZ",
            initialCurrencyCode = "DZD",
            initialIsLoggedIn = true,
            initialRole = AppRole.OWNER,
            initialHasLinkedBarns = true,
            initialActiveFarmId = "farm_chat_sync",
            initialUserUid = "owner_1"
        )
        session.performExplicitLogout(context)
        assertEquals(0, AgritechMockDb.farms.size)
        assertEquals(0, AgritechMockDb.cycles.size)
        assertEquals(false, LocalFarmPersistence.hasSavedData(context))

        // 4. Verify gram_unit translation key across AR, FR, EN, ES, PT, HI
        assertTrue(Translations.hasKey("gram_unit"))
        assertEquals("غرام", Translations.tr("gram_unit", AppLanguage.AR))
        assertEquals("g", Translations.tr("gram_unit", AppLanguage.FR))
        assertEquals("g", Translations.tr("gram_unit", AppLanguage.EN))
        assertEquals("g", Translations.tr("gram_unit", AppLanguage.ES))
        assertEquals("g", Translations.tr("gram_unit", AppLanguage.PT))
    }

    @Test
    fun cycleDayAbove60_actualBirdWeightPersistence_andRealZxingQrEncoding_workCorrectly() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Verify calculateCycleDay works accurately above day 60 (no 60-day cap)
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val seventyFourDaysAgoMillis = System.currentTimeMillis() - (74L * 86_400_000L)
        val startDate75 = sdf.format(java.util.Date(seventyFourDaysAgoMillis))
        val calculatedDayFromDate = com.example.utils.PoultryCalculators.calculateCycleDay(startDate75, maxReportDay = 1)
        assertTrue("calculateCycleDay must count above 60 days (got $calculatedDayFromDate)", calculatedDayFromDate >= 74)

        val calculatedDayFromReport = com.example.utils.PoultryCalculators.calculateCycleDay("2026-10-29", maxReportDay = 78)
        assertEquals(78, calculatedDayFromReport)

        // 2. Verify actualAvgWeightKg (grams -> kg) persistence and resolution
        val d12Report = DailyReportModel(
            id = "rep_weight_d12",
            farmId = "farm_w_1",
            cycleId = "cycle_w_1",
            dayNumber = 12,
            feedIntakeKg = 300.0,
            waterIntakeLiters = 600.0,
            mortalityCount = 2,
            temperature = 28.0,
            actualAvgWeightKg = 0.420 // 420g
        )
        val d25Report = DailyReportModel(
            id = "rep_weight_d25",
            farmId = "farm_w_1",
            cycleId = "cycle_w_1",
            dayNumber = 25,
            feedIntakeKg = 650.0,
            waterIntakeLiters = 1300.0,
            mortalityCount = 1,
            temperature = 25.0,
            actualAvgWeightKg = 1.250 // 1250g
        )
        AgritechMockDb.upsertDailyReportWithAudit(d12Report)
        AgritechMockDb.upsertDailyReportWithAudit(d25Report)
        LocalFarmPersistence.saveAll(context)
        LocalFarmPersistence.loadIntoMockDb(context)

        val reloadedReports = AgritechMockDb.dailyReports.filter { it.cycleId == "cycle_w_1" }
        val resolvedActualWeightKg = com.example.utils.PoultryCalculators.resolveActualAvgBirdWeightKg(
            reports = reloadedReports,
            cycleTransactions = emptyList(),
            cycleId = "cycle_w_1"
        )
        assertEquals(1.250, resolvedActualWeightKg ?: 0.0, 0.001)

        // 3. Verify real ISO/IEC 18004 ZXing QR BitMatrix generation for personal and farm invite payloads
        val personalPayload = "MYBUSINESS_CONTACT|name=مراد|role=مالك المزرعة|phone=0550123456"
        val farmInvitePayload = "AGRITECH_JOIN|farmId=farm_1|farmName=عنبر النور|role=WORKER"

        val personalMatrix = com.example.utils.PoultryCalculators.encodeQrBitMatrix(personalPayload, size = 1)
        val farmMatrix = com.example.utils.PoultryCalculators.encodeQrBitMatrix(farmInvitePayload, size = 1)

        assertTrue("Personal QR matrix must have valid dimensions (> 21x21)", personalMatrix.width >= 21 && personalMatrix.height >= 21)
        assertTrue("Farm Invite QR matrix must have valid dimensions (> 21x21)", farmMatrix.width >= 21 && farmMatrix.height >= 21)

        var personalBlackModules = 0
        for (y in 0 until personalMatrix.height) {
            for (x in 0 until personalMatrix.width) {
                if (personalMatrix.get(x, y)) personalBlackModules++
            }
        }
        assertTrue("Personal QR matrix must contain encoded dark modules", personalBlackModules > 50)

        // 4. Verify new localization keys for weight input & live birds sales cap warning
        for (lang in listOf(AppLanguage.AR, AppLanguage.FR, AppLanguage.EN, AppLanguage.ES, AppLanguage.PT)) {
            val weightLabel = Translations.tr("field_actual_bird_weight_grams_optional", lang)
            val salesWarn = Translations.tr("sales_exceeds_live_birds_warning", lang)
            assertTrue(weightLabel.isNotBlank() && weightLabel != "field_actual_bird_weight_grams_optional")
            assertTrue(salesWarn.isNotBlank() && salesWarn != "sales_exceeds_live_birds_warning")
        }
    }
}

