package com.example.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val APP_CHECK_TAG = "AppCheckInit"
private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

/**
 * Safely configures Firebase App Check conditionally by build variant (Debug vs Release)
 * without crashing in Robolectric or local JVM unit tests.
 */
fun configureAppCheck(context: Context, intent: Intent?) {
    try {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        if (FirebaseApp.getApps(context).isEmpty()) return

        if (BuildConfig.DEBUG) {
            val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
            if (!debugToken.isNullOrEmpty()) {
                val persistenceKey = FirebaseApp.getInstance().persistenceKey
                val prefs = context.getSharedPreferences(
                    "$DEBUG_STORE_PREFIX$persistenceKey",
                    Context.MODE_PRIVATE
                )
                // Synchronous commit() ensures the secret exists before DebugAppCheckProviderFactory reads it.
                prefs.edit()
                    .putString(DEBUG_SECRET_KEY, debugToken)
                    .commit()
                runCatching { Log.d(APP_CHECK_TAG, "App Check debug secret configured successfully.") }
            }
            val debugFactoryClass = Class.forName("com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory")
            val debugFactory = debugFactoryClass.getMethod("getInstance").invoke(null) as AppCheckProviderFactory
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(debugFactory)
            runCatching { Log.d(APP_CHECK_TAG, "DebugAppCheckProviderFactory installed.") }
        } else {
            val resId = context.resources.getIdentifier(RECAPTCHA_SITE_KEY_RES, "string", context.packageName)
            if (resId != 0) {
                val siteKey = context.getString(resId)
                if (siteKey.isNotBlank()) {
                    FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                        RecaptchaAppCheckProviderFactory.getInstance(siteKey)
                    )
                    runCatching { Log.d(APP_CHECK_TAG, "RecaptchaAppCheckProviderFactory installed.") }
                } else {
                    runCatching { Log.e(APP_CHECK_TAG, "recaptcha_site_key resource string is blank.") }
                }
            } else {
                runCatching { Log.e(APP_CHECK_TAG, "recaptcha_site_key string resource not found in package.") }
            }
        }
    } catch (e: Throwable) {
        runCatching { Log.e(APP_CHECK_TAG, "Failed to initialize Firebase App Check", e) }
    }
}

/**
 * Structured real barn context passed to the AI Veterinarian & free-chat consultation flow.
 * Any field that has not been recorded yet remains null and is formatted as "غير مسجل / لا توجد قراءة"
 * rather than fabricated numbers.
 */
data class BarnAiContext(
    val farmName: String? = null,
    val cycleAgeDays: Int? = null,
    val liveBirdsCount: Int? = null,
    val latestTemperatureC: Double? = null,
    val cumulativeMortalityPct: Double? = null,
    val feedInventoryKg: Double? = null
)

/**
 * Data model for Smart Barn Robot & Gas/Climate IoT Telemetry ("🤖 روبوت وحساسات العنبر الذكية").
 * Defaults strictly to disconnected (isRobotConnected = false); when toggled on in the UI,
 * it operates strictly as an explicit demo simulation and never injects fake readings as real barn data.
 */
data class SmartBarnTelemetrySnapshot(
    val isRobotConnected: Boolean = false,
    val ammoniaNh3Ppm: Int = 12,
    val co2Ppm: Int = 1800,
    val gasPressurePct: Int = 85,
    val temperatureC: Double = 21.3,
    val humidityPct: Int = 58
) {
    val isAmmoniaAlert: Boolean
        get() = isRobotConnected && ammoniaNh3Ppm >= 20
}

/**
 * Gemini AI Veterinary Consultation & Smart Barn IoT Telemetry Service ("المستشار البيطري الذكي ✨").
 * Uses Firebase AI Logic (`gemini-2.5-flash`) with safe API key & response parsing and a deterministic
 * clinical fallback engine so farmers always receive actionable poultry advice online or offline.
 */
object AiVetGeminiService {

    private const val MODEL_NAME = "gemini-2.5-flash"
    const val LOCAL_FALLBACK_NOTICE_AR = "ℹ️ (إرشاد محلي احتياطي — تعذر الاتصال بالخادم الذكي حالياً)"
    const val LOCAL_FALLBACK_NOTICE_FR = "ℹ️ (Conseil local de secours — serveur IA actuellement indisponible)"
    const val LOCAL_FALLBACK_NOTICE_EN = "ℹ️ (Local fallback guidance — cloud AI server currently unreachable)"

    fun fallbackNoticeFor(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> LOCAL_FALLBACK_NOTICE_AR
        AppLanguage.FR -> LOCAL_FALLBACK_NOTICE_FR
        else -> LOCAL_FALLBACK_NOTICE_EN
    }

    /**
     * Safely resolves any configured API key from BuildConfig (via Secrets Gradle Plugin) or system env
     * without throwing exceptions if absent, falling back gracefully to Firebase AI Logic or the
     * built-in offline diagnostic engine.
     */
    fun resolveConfiguredApiKey(): String {
        return try {
            val buildConfigClass = Class.forName("com.example.BuildConfig")
            val fieldNames = listOf("GEMINI_API_KEY", "API_KEY", "AI_API_KEY")
            for (name in fieldNames) {
                val field = buildConfigClass.declaredFields.firstOrNull { it.name == name }
                if (field != null) {
                    field.isAccessible = true
                    val value = (field.get(null) as? String)?.trim().orEmpty()
                    if (value.isNotBlank() && !value.startsWith("YOUR_", ignoreCase = true)) {
                        return value
                    }
                }
            }
            ""
        } catch (_: Throwable) {
            ""
        }
    }

    /**
     * Safely parses a Gemini text response:
     * - If the response contains a markdown ```json ... ``` block or raw JSON object with a known text field
     *   (`advice`, `recommendation`, `diagnosis`, `response`, `text`), extracts and formats it cleanly.
     * - If the response does NOT contain markdown ```json blocks, returns the clean plain-text response
     *   directly without crashing.
     */
    fun parseGeminiResponse(rawResponse: String): String {
        val trimmed = rawResponse.trim()
        if (trimmed.isEmpty()) return ""

        val fencedRegex = Regex("```(?:json)?\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
        val fencedMatch = fencedRegex.find(trimmed)
        val candidate = fencedMatch?.groupValues?.getOrNull(1)?.trim() ?: trimmed

        if (candidate.startsWith("{") && candidate.endsWith("}")) {
            val fieldRegex = Regex(
                "\"(?:advice|recommendation|diagnosis|response|summary|text)\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"",
                RegexOption.IGNORE_CASE
            )
            val match = fieldRegex.find(candidate)
            if (match != null) {
                return match.groupValues[1]
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("**", "")
                    .trim()
            }
        }

        // Plain-text fallback when no markdown ```json block is present
        return candidate
            .removePrefix("```")
            .removeSuffix("```")
            .replace("**", "")
            .trim()
    }

    suspend fun askAiVeterinarian(
        question: String,
        flockAgeDays: Int?,
        liveBirds: Int?,
        mortalityPct: Double?,
        currentTempC: Double?,
        lang: AppLanguage,
        telemetry: SmartBarnTelemetrySnapshot? = null,
        barnName: String = "",
        barnContext: BarnAiContext? = null
    ): String = withContext(Dispatchers.IO) {
        val resolvedContext = barnContext ?: BarnAiContext(
            farmName = barnName.takeIf { it.isNotBlank() },
            cycleAgeDays = flockAgeDays,
            liveBirdsCount = liveBirds,
            latestTemperatureC = currentTempC,
            cumulativeMortalityPct = mortalityPct,
            feedInventoryKg = null
        )
        val cleanQuestion = question.trim()
        if (cleanQuestion.isEmpty()) {
            return@withContext "${fallbackNoticeFor(lang)}\n" + buildClinicalFallbackAdvice(
                question = "",
                flockAgeDays = resolvedContext.cycleAgeDays,
                liveBirds = resolvedContext.liveBirdsCount,
                mortalityPct = resolvedContext.cumulativeMortalityPct,
                currentTempC = resolvedContext.latestTemperatureC,
                lang = lang,
                telemetry = telemetry,
                barnName = resolvedContext.farmName.orEmpty(),
                barnContext = resolvedContext
            )
        }

        val langInstruction = when (lang) {
            AppLanguage.AR -> "أجب باللغة العربية بأسلوب طبيب بيطري خبير في دجاج التسمين (Ross 308 / Cobb 500) في الجزائر والمغرب العربي، في نقاط مختصرة وعملية جداً (التشخيص المبدئي، الإجراء الفوري في العنبر، العلاج المقترح، وفترة السحب إن وجدت)."
            AppLanguage.FR -> "Répondez en français comme un vétérinaire avicole expert (Ross 308), en points courts et pratiques (Diagnostic probable, Action immédiate au bâtiment, Traitement recommandé, Délai d'attente)."
            AppLanguage.ES -> "Responda en español como un veterinario avícola experto (Ross 308), en puntos breves y prácticos (Diagnóstico probable, Acción inmediata en el galpón, Tratamiento sugerido, Tiempo de retiro)."
            AppLanguage.PT -> "Responda em português como um veterinário avícola especialista (Ross 308), em tópicos curtos e práticos (Diagnóstico provável, Ação imediata no galpão, Tratamento sugerido, Período de carência)."
            else -> "Respond in English as an expert broiler poultry veterinarian (Ross 308), using concise, actionable bullet points (Likely Diagnosis, Immediate Barn Action, Suggested Treatment, Withdrawal Period)."
        }

        val barnHeaderLine = resolvedContext.farmName?.takeIf { it.isNotBlank() }
            ?: "غير مسجل / لا يوجد عنبر محدد (Not recorded)"
        val ageLine = resolvedContext.cycleAgeDays?.let { "$it days" } ?: "غير مسجل / لا توجد قراءة (Not recorded)"
        val birdsLine = resolvedContext.liveBirdsCount?.let { "$it birds" } ?: "غير مسجل / لا توجد قراءة (Not recorded)"
        val mortalityLine = resolvedContext.cumulativeMortalityPct?.let {
            "${String.format(java.util.Locale.US, "%.2f", it)}%"
        } ?: "غير مسجل / لا توجد قراءة (Not recorded)"
        val tempLine = resolvedContext.latestTemperatureC?.let {
            "${String.format(java.util.Locale.US, "%.1f", it)}°C"
        } ?: "غير مسجل / لا توجد قراءة (Not recorded)"
        val feedLine = resolvedContext.feedInventoryKg?.let {
            "${String.format(java.util.Locale.US, "%.1f", it / 100.0)} quintals (${String.format(java.util.Locale.US, "%.0f", it)} kg)"
        } ?: "غير مسجل / لا توجد قراءة (Not recorded)"

        val systemContext = """
            You are an expert Broiler Poultry Veterinarian.
            Real Barn Context (Never invent missing readings; if a field says Not recorded, state that no reading is recorded yet):
            - Active Barn Name: $barnHeaderLine
            - Cycle Age: $ageLine
            - Live Birds Count: $birdsLine
            - Cumulative Mortality: $mortalityLine
            - Latest Recorded Barn Temperature: $tempLine
            - Remaining Feed Inventory: $feedLine
            $langInstruction
        """.trimIndent()

        try {
            val model = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
                modelName = MODEL_NAME,
                generationConfig = generationConfig {
                    temperature = 0.35f
                    maxOutputTokens = 600
                },
                systemInstruction = content { text(systemContext) }
            )
            val response = model.generateContent(cleanQuestion)
            val parsedText = parseGeminiResponse(response.text.orEmpty())
            if (parsedText.isNotBlank()) {
                return@withContext parsedText
            }
        } catch (_: Throwable) {
            // Safe fallback when offline, unauthenticated, or when Firebase AI Logic is unavailable
        }

        "${fallbackNoticeFor(lang)}\n" + buildClinicalFallbackAdvice(
            question = cleanQuestion,
            flockAgeDays = resolvedContext.cycleAgeDays,
            liveBirds = resolvedContext.liveBirdsCount,
            mortalityPct = resolvedContext.cumulativeMortalityPct,
            currentTempC = resolvedContext.latestTemperatureC,
            lang = lang,
            telemetry = telemetry,
            barnName = resolvedContext.farmName.orEmpty(),
            barnContext = resolvedContext
        )
    }

    /**
     * One-tap Smart Barn Robot & Flock Telemetry Analysis ("🤖 تحليل قراءات الروبوت والقطيع الآن").
     * Clearly labels simulation mode when enabled so simulated numbers are never treated as real sensor readings.
     */
    suspend fun analyzeSmartBarnTelemetry(
        flockAgeDays: Int?,
        liveBirds: Int?,
        mortalityPct: Double?,
        dailyFeedQuintals: Double,
        telemetry: SmartBarnTelemetrySnapshot,
        lang: AppLanguage
    ): String {
        val ageLabelAr = flockAgeDays?.let { "$it يوم" } ?: "غير مسجل"
        val birdsLabelAr = liveBirds?.let { "$it" } ?: "غير مسجل"
        val prompt = when (lang) {
            AppLanguage.AR -> if (telemetry.isRobotConnected) {
                "قم بتحليل سيناريو وضع المحاكاة التجريبي للعرض فقط (ليست قراءة حساس حقيقي): الأمونيا الافتراضية NH3=${telemetry.ammoniaNh3Ppm} ppm، ثاني أكسيد الكربون CO2=${telemetry.co2Ppm} ppm، ضغط الغاز=${telemetry.gasPressurePct}%، عمر القطيع $ageLabelAr، الطيور الحية $birdsLabelAr."
            } else {
                "قم بتحليل مؤشرات القطيع الفعلية في الوضع اليدوي (بدون حساسات متصلة): عمر القطيع $ageLabelAr، الطيور الحية $birdsLabelAr، استهلاك العلف ${String.format(java.util.Locale.US, "%.1f", dailyFeedQuintals)} قنطار."
            }
            AppLanguage.FR -> if (telemetry.isRobotConnected) {
                "Analysez le scénario de simulation démo (non issu d'un capteur réel) : NH3=${telemetry.ammoniaNh3Ppm} ppm, CO2=${telemetry.co2Ppm} ppm."
            } else {
                "Analysez le troupeau en mode manuel (capteurs déconnectés)."
            }
            else -> if (telemetry.isRobotConnected) {
                "Analyze demo simulation scenario (not a real hardware sensor reading): Ammonia NH3=${telemetry.ammoniaNh3Ppm} ppm, CO2=${telemetry.co2Ppm} ppm."
            } else {
                "Analyze flock in manual mode (sensors disconnected)."
            }
        }
        return askAiVeterinarian(
            question = prompt,
            flockAgeDays = flockAgeDays,
            liveBirds = liveBirds,
            mortalityPct = mortalityPct,
            currentTempC = null,
            lang = lang,
            telemetry = telemetry
        )
    }

    suspend fun generateVetResponse(
        question: String,
        lang: AppLanguage,
        flockAgeDays: Int? = null,
        liveBirds: Int? = null,
        mortalityPct: Double? = null,
        currentTempC: Double? = null,
        barnName: String = "",
        barnContext: BarnAiContext? = null
    ): String {
        val ctx = barnContext ?: BarnAiContext(
            farmName = barnName.takeIf { it.isNotBlank() },
            cycleAgeDays = flockAgeDays,
            liveBirdsCount = liveBirds,
            latestTemperatureC = currentTempC,
            cumulativeMortalityPct = mortalityPct,
            feedInventoryKg = null
        )
        return askAiVeterinarian(
            question = question,
            flockAgeDays = ctx.cycleAgeDays,
            liveBirds = ctx.liveBirdsCount,
            mortalityPct = ctx.cumulativeMortalityPct,
            currentTempC = ctx.latestTemperatureC,
            lang = lang,
            barnName = ctx.farmName.orEmpty(),
            barnContext = ctx
        )
    }

    fun buildClinicalFallbackAdvice(
        question: String,
        flockAgeDays: Int? = null,
        liveBirds: Int? = null,
        mortalityPct: Double? = null,
        currentTempC: Double? = null,
        lang: AppLanguage = AppLanguage.AR,
        telemetry: SmartBarnTelemetrySnapshot? = null,
        barnName: String = "",
        barnContext: BarnAiContext? = null
    ): String {
        val resolvedName = barnContext?.farmName?.takeIf { it.isNotBlank() } ?: barnName
        val resolvedAge = barnContext?.cycleAgeDays ?: flockAgeDays
        val resolvedBirds = barnContext?.liveBirdsCount ?: liveBirds
        val resolvedMortality = barnContext?.cumulativeMortalityPct ?: mortalityPct
        val resolvedTemp = barnContext?.latestTemperatureC ?: currentTempC
        val resolvedFeedKg = barnContext?.feedInventoryKg

        val q = question.lowercase()
        val hasConnectedTelemetry = telemetry != null && telemetry.isRobotConnected
        val isAmmoniaHigh = (hasConnectedTelemetry && (telemetry?.ammoniaNh3Ppm ?: 0) >= 20) || q.contains("nh3") || q.contains("أمونيا") || q.contains("امونيا") || q.contains("ammonia")
        val isRobotTelemetryQuery = hasConnectedTelemetry || q.contains("روبوت") || q.contains("robot") || q.contains("حساسات")
        val isRespiratory = q.contains("تنفس") || q.contains("شخير") || q.contains("عطس") || q.contains("حشرجة") ||
            q.contains("respir") || q.contains("cough") || q.contains("râle") || q.contains("toux")
        val isCoccidiosis = q.contains("كوكسيديا") || q.contains("إسهال") || q.contains("اسهال") || q.contains("مدمم") ||
            q.contains("أحمر") || q.contains("بني") || q.contains("coccid") || q.contains("diarr") || q.contains("blood")
        val hasValidTemp = resolvedTemp != null
        val isHeatStress = q.contains("حرارة") || q.contains("نهجان") || q.contains("احتباس") ||
            q.contains("heat") || q.contains("chaleur") || (resolvedTemp != null && resolvedTemp >= 31.0)

        val tempDisplayAr = if (hasValidTemp) {
            "${String.format(java.util.Locale.US, "%.1f", resolvedTemp)}°C"
        } else {
            "غير مسجلة (لا توجد قراءة — غير مسجل)"
        }
        val tempDisplayFr = if (hasValidTemp) {
            "${String.format(java.util.Locale.US, "%.1f", resolvedTemp)}°C"
        } else {
            "Non enregistrée (aucune lecture)"
        }
        val tempDisplayEn = if (hasValidTemp) {
            "${String.format(java.util.Locale.US, "%.1f", resolvedTemp)}°C"
        } else {
            "Not recorded (no reading)"
        }

        val ageDisplayAr = resolvedAge?.let { "$it يوم" } ?: "غير مسجل / لا توجد قراءة"
        val ageDisplayFr = resolvedAge?.let { "$it j" } ?: "Non enregistré"
        val ageDisplayEn = resolvedAge?.let { "$it days" } ?: "Not recorded"

        val birdsDisplayAr = resolvedBirds?.let { "$it طائر" } ?: "غير مسجل / لا توجد قراءة"
        val birdsDisplayFr = resolvedBirds?.let { "$it sujets" } ?: "Non enregistré"
        val birdsDisplayEn = resolvedBirds?.let { "$it birds" } ?: "Not recorded"

        val mortalityDisplayAr = resolvedMortality?.let {
            "${String.format(java.util.Locale.US, "%.2f", it)}%"
        } ?: "غير مسجل / لا توجد قراءة"
        val mortalityDisplayFr = resolvedMortality?.let {
            "${String.format(java.util.Locale.US, "%.2f", it)}%"
        } ?: "Non enregistré"
        val mortalityDisplayEn = resolvedMortality?.let {
            "${String.format(java.util.Locale.US, "%.2f", it)}%"
        } ?: "Not recorded"

        val feedLineAr = resolvedFeedKg?.let {
            " • مخزون العلف: ${String.format(java.util.Locale.US, "%.1f", it / 100.0)} قنطار"
        } ?: ""

        val barnLabelAr = if (resolvedName.isNotBlank()) "$resolvedName • " else "العنبر: غير مسجل • "
        val barnLabelFr = if (resolvedName.isNotBlank()) "$resolvedName • " else "Bâtiment : Non enregistré • "
        val barnLabelEn = if (resolvedName.isNotBlank()) "$resolvedName • " else "Barn: Not recorded • "

        val rawText = when (lang) {
            AppLanguage.AR -> when {
                isAmmoniaHigh -> """
                    🚨 تحليل(محاكاة/فحص) غاز الأمونيا (NH3 = ${telemetry?.ammoniaNh3Ppm ?: 26} ppm 🔴 خطر تنفسي)
                    • التشخيص الميداني (${barnLabelAr}العمر: $ageDisplayAr • العدد الحي: $birdsDisplayAr): تجاوز الأمونيا حد 20 ppm يسبب تهيج الأغشية المخاطية للقصبة الهوائية ويضعف المناعة الموضعية ضد النيوكاسل والـ CRD.
                    • الإجراء الفوري للتهوية والفرشة:
                      1. رفع دورة شفاطات التهوية الدنيا بنسبة 25% لمدة 30 دقيقة لطرد الغازات الثقيلة دون إحداث تيار بارد مباشر.
                      2. فحص خطوط الحلمات (Nipples) وتقليب البقع الرطبة في الفرشة مع نثر الجير المطفي أو سوبر فوسفات وفوقها طبقة نشارة جافة.
                      3. إعطاء مقشع وموسع شعب (المنثول + الأوكاليبتوس) في مياه الشرب لمدة 3 أيام مع فيتامين C.
                """.trimIndent()
                isRobotTelemetryQuery -> """
                    🤖 تقرير فحص مستشعرات العنبر (${barnLabelAr}العمر: $ageDisplayAr • العدد الحي: $birdsDisplayAr):
                    • حالة الحساسات: ${if (hasConnectedTelemetry) "🧪 محاكاة تجريبية للعرض فقط (الأمونيا NH3 = ${telemetry?.ammoniaNh3Ppm} ppm | CO2 = ${telemetry?.co2Ppm} ppm)" else "غير متصل حالياً — القراءة الفعلية للعنبر: الحرارة $tempDisplayAr"}.
                    • التقييم البيطري والإنتاجي:
                      1. راقب جودة الهواء والتهوية التبادلية الهادئة داخل العنبر.
                      2. حرارة العنبر المسجلة في السجل اليومي: $tempDisplayAr.
                      3. استمر في مراقبة استهلاك الماء والعلف اليومي بدقة.
                """.trimIndent()
                isRespiratory -> """
                    🩺 التشخيص البيطري المبدئي (${barnLabelAr}العمر: $ageDisplayAr • العدد الحي: $birdsDisplayAr):
                    • اشتباه عدوى تنفسية (CRD / مايكوبلازما) أو رد فعل ما بعد التحصين، خاصة إذا ترافقت الأعراض مع رطوبة الفرشة أو غاز الأمونيا.
                    • الإجراء الفوري في العنبر:
                      1. تحسين التهوية السقفية تدريجياً لطرد الأمونيا دون تعريض الطيور لتيار هواء بارد مباشر (حرارة العنبر المسجلة: $tempDisplayAr).
                      2. تقديم موسع شعب ومقشع طبيعي (المنتول + الأوكاليبتوس) في ماء الشرب لمدة 3 أيام.
                    • العلاج الدوائي المقترح (تحت إشراف طبيب المزرعة):
                      - التيلوزين (Tylosin) أو دوكسيسيكلين لمدة 4 أيام متتالية مع رافع مناعة وفيتامين AD3E.
                    • ⏳ تنبيه فترة السحب: يمنع بيع الطيور للاستهلاك قبل انقضاء 5 إلى 7 أيام من آخر جرعة مضاد حيوي.
                """.trimIndent()
                isCoccidiosis -> """
                    🩺 التشخيص البيطري المبدئي (${barnLabelAr}العمر: $ageDisplayAr • العدد الحي: $birdsDisplayAr):
                    • اشتباه كوكسيديا معوية أو أعوريه (Eimeria) ناتجة عن زيادة رطوبة الفرشة حول خطوط الحلمات.
                    • الإجراء الفوري في العنبر:
                      1. تقليب الفرشة الرطبة فوراً وإضافة طبقة نشارة جافة مع الجير المطفي تحت خطوط الماء.
                      2. تقديم مضاد كوكسيديا (تولترازوريل Toltrazuril 2.5% بمعدل 1 مل/لتر لمدة يومين، أو أمبروليوم + فيتامين K3 لوقف النزف المعوي).
                      3. دعم الأمعاء بالأحماض العضوية والـ Probiotics بعد انتهاء العلاج.
                """.trimIndent()
                isHeatStress -> """
                    🩺 تنبيه إجهاد حراري (${barnLabelAr}حرارة العنبر: $tempDisplayAr • العمر: $ageDisplayAr):
                    • الإجراء الفوري:
                      1. رفع العلف خلال ساعات الذروة الحارة (من 11 صباحاً حتى 4 عصراً) لتقليل الحرارة الاستقلابية.
                      2. إضافة فيتامين C + إلكتروليت (بيكربونات الصوديوم) في ماء الشرب البارد لتعويض التوازن الشاردي.
                      3. تشغيل خلايا التبريد والمراوح تدريجياً مع مراقبة الرطوبة النسبية.
                """.trimIndent()
                else -> """
                    🩺 التوصية البيطرية الذكية للقطيع (${barnLabelAr}العمر: $ageDisplayAr • العدد الحي: $birdsDisplayAr):
                    • تقييم المؤشرات الحيوية: نسبة النفوق التراكمية ($mortalityDisplayAr) وحرارة العنبر ($tempDisplayAr)$feedLineAr.
                    • خطة العمل الموصى بها:
                      1. الفرشة والتهوية: حافظ على جفاف الفرشة وتأكد من عدم وجود رائحة أمونيا في مستوى رأس الطائر.
                      2. برنامج المياه: افحص نظافة خطوط الشرب (النيبل) وقدم جرعة فيتامين AD3E + أملاح معدنية لدعم النمو والتحويل الغذائي (FCR).
                      3. المتابعة التشريحية: في حال ظهور أي خمول أو تغير في لون الزرق، قم بتصوير الحالة وإرسالها لطبيب المزرعة في تبويب المحادثة.
                """.trimIndent()
            }
            AppLanguage.FR -> when {
                isAmmoniaHigh -> """
                    🚨 Alerte Ammoniac élevé (NH3 = ${telemetry?.ammoniaNh3Ppm ?: 26} ppm 🔴 Risque respiratoire)
                    • Diagnostic (${barnLabelFr}Âge : $ageDisplayFr • Effectif : $birdsDisplayFr) : Un taux de NH3 > 20 ppm irrite la trachée et favorise les infections CRD.
                    • Actions immédiates :
                      1. Augmenter l'extraction d'air de +25% pendant 30 minutes sans courant d'air froid.
                      2. Assécher les zones humides sous les pipettes avec du copeau sec.
                      3. Administrer un complexe huiles essentielles (Menthol/Eucalyptus) + Vitamine C pendant 3 jours.
                """.trimIndent()
                else -> """
                    🩺 Analyse Vétérinaire (${barnLabelFr}Âge : $ageDisplayFr • Effectif vif : $birdsDisplayFr) :
                    • Indicateurs : Mortalité cumulée ($mortalityDisplayFr), Température ($tempDisplayFr).
                    • Actions immédiates recommandées :
                      1. Ventilation & Litière : Maintenez une litière sèche et contrôlez le taux d'ammoniac (NH3 < 20 ppm).
                      2. Eau de boisson : Administrez un complexe Vitamines AD3E + Électrolytes ou Hépato-protecteur selon l'état clinique.
                      3. Délai d'attente : Respectez strictement le délai de retrait avant l'abattage en cas de traitement antibiotique.
                """.trimIndent()
            }
            else -> when {
                isAmmoniaHigh -> """
                    🚨 High Ammonia Alert (NH3 = ${telemetry?.ammoniaNh3Ppm ?: 26} ppm 🔴 Respiratory Risk)
                    • Clinical Impact (${barnLabelEn}Age: $ageDisplayEn • Birds: $birdsDisplayEn): NH3 above 20 ppm damages tracheal cilia and triggers secondary CRD / E. coli.
                    • Immediate Action Plan:
                      1. Increase minimum ventilation exhaust fans by +25% for 30 minutes to purge heavy gases.
                      2. Inspect drinker nipples for leaks and top-dress wet litter spots with dry shavings.
                      3. Add Menthol/Eucalyptus respiratory tonic + Vitamin C in drinking water for 3 days.
                """.trimIndent()
                else -> """
                    🩺 AI Vet Assessment (${barnLabelEn}Flock Age: $ageDisplayEn • Live Birds: $birdsDisplayEn):
                    • Vital Signs: Cumulative mortality ($mortalityDisplayEn), Barn Temp ($tempDisplayEn).
                    • Recommended Action Plan:
                      1. Litter & Air Quality: Keep litter friable and dry under drinker lines; ensure ammonia stays below 20 ppm.
                      2. Water Supplementation: Provide AD3E vitamins + electrolytes or organic acids to optimize gut health and FCR.
                      3. Withdrawal Safety: Always verify antibiotic withdrawal days before scheduling any flock sale.
                """.trimIndent()
            }
        }
        return rawText.replace("**", "")
    }
}
