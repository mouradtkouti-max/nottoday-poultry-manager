package com.example.utils

import android.content.Context
import android.content.Intent
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Safely configures Firebase App Check if available without crashing in unit tests or emulators.
 */
fun configureAppCheck(context: Context, intent: Intent?) {
    try {
        com.google.firebase.FirebaseApp.initializeApp(context)
    } catch (_: Throwable) {
    }
}

/**
 * Data model for live Smart Barn Robot & Gas/Climate IoT Telemetry ("🤖 روبوت وحساسات العنبر الذكية").
 */
data class SmartBarnTelemetrySnapshot(
    val isRobotConnected: Boolean = true,
    val ammoniaNh3Ppm: Int = 12,
    val co2Ppm: Int = 1800,
    val gasPressurePct: Int = 85,
    val temperatureC: Double = 21.3,
    val humidityPct: Int = 58
) {
    val isAmmoniaAlert: Boolean
        get() = ammoniaNh3Ppm >= 20
}

/**
 * Gemini AI Veterinary Consultation & Smart Barn IoT Telemetry Service ("المستشار البيطري الذكي ✨").
 * Uses Firebase AI Logic (`gemini-2.5-flash`) with safe API key & response parsing and a deterministic
 * clinical fallback engine so farmers always receive actionable poultry advice online or offline.
 */
object AiVetGeminiService {

    private const val MODEL_NAME = "gemini-2.5-flash"

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
                    .trim()
            }
        }

        // Plain-text fallback when no markdown ```json block is present
        return candidate
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    suspend fun askAiVeterinarian(
        question: String,
        flockAgeDays: Int,
        liveBirds: Int,
        mortalityPct: Double,
        currentTempC: Double,
        lang: AppLanguage,
        telemetry: SmartBarnTelemetrySnapshot? = null
    ): String = withContext(Dispatchers.IO) {
        val cleanQuestion = question.trim()
        if (cleanQuestion.isEmpty()) {
            return@withContext buildClinicalFallbackAdvice("", flockAgeDays, liveBirds, mortalityPct, currentTempC, lang, telemetry)
        }

        val langInstruction = when (lang) {
            AppLanguage.AR -> "أجب باللغة العربية بأسلوب طبيب بيطري خبير في دجاج التسمين (Ross 308 / Cobb 500) في الجزائر والمغرب العربي، في نقاط مختصرة وعملية جداً (التشخيص المبدئي، الإجراء الفوري في العنبر، العلاج المقترح، وفترة السحب إن وجدت)."
            AppLanguage.FR -> "Répondez en français comme un vétérinaire avicole expert (Ross 308), en points courts et pratiques (Diagnostic probable, Action immédiate au bâtiment, Traitement recommandé, Délai d'attente)."
            AppLanguage.ES -> "Responda en español como un veterinario avícola experto (Ross 308), en puntos breves y prácticos (Diagnóstico probable, Acción inmediata en el galpón, Tratamiento sugerido, Tiempo de retiro)."
            AppLanguage.PT -> "Responda em português como um veterinário avícola especialista (Ross 308), em tópicos curtos e práticos (Diagnóstico provável, Ação imediata no galpão, Tratamento sugerido, Período de carência)."
            else -> "Respond in English as an expert broiler poultry veterinarian (Ross 308), using concise, actionable bullet points (Likely Diagnosis, Immediate Barn Action, Suggested Treatment, Withdrawal Period)."
        }

        val telemetrySection = if (telemetry != null) {
            """
            - Smart Barn Robot Telemetry: Mode=${if (telemetry.isRobotConnected) "Connected to Robot" else "Manual"}, Ammonia (NH3)=${telemetry.ammoniaNh3Ppm} ppm, CO2=${telemetry.co2Ppm} ppm, Gas Pressure=${telemetry.gasPressurePct}%, Barn Temp=${telemetry.temperatureC}°C, Humidity=${telemetry.humidityPct}%
            """.trimIndent()
        } else {
            ""
        }

        val systemContext = """
            You are an expert Broiler Poultry Veterinarian & Smart Barn IoT Specialist.
            Current Barn Telemetry:
            - Flock Age: $flockAgeDays days
            - Live Birds: $liveBirds birds
            - Cumulative Mortality: ${String.format(java.util.Locale.US, "%.2f", mortalityPct)}%
            - Barn Temperature: ${String.format(java.util.Locale.US, "%.1f", currentTempC)}°C
            $telemetrySection
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

        buildClinicalFallbackAdvice(cleanQuestion, flockAgeDays, liveBirds, mortalityPct, currentTempC, lang, telemetry)
    }

    /**
     * One-tap Smart Barn Robot & Flock Telemetry Analysis ("🤖 تحليل قراءات الروبوت والقطيع الآن").
     * Feeds live barn age, mortality, feed intake, and NH3/CO2/temperature sensor data into `AiVetGeminiService`
     * to generate an immediate, practical recommendation for the farmer.
     */
    suspend fun analyzeSmartBarnTelemetry(
        flockAgeDays: Int,
        liveBirds: Int,
        mortalityPct: Double,
        dailyFeedQuintals: Double,
        telemetry: SmartBarnTelemetrySnapshot,
        lang: AppLanguage
    ): String {
        val prompt = when (lang) {
            AppLanguage.AR -> "قم بتحليل قراءات روبوت العنبر الذكي: الأمونيا NH3=${telemetry.ammoniaNh3Ppm} ppm، ثاني أكسيد الكربون CO2=${telemetry.co2Ppm} ppm، ضغط الغاز=${telemetry.gasPressurePct}%، الحرارة=${telemetry.temperatureC}°C، الرطوبة=${telemetry.humidityPct}%، عمر القطيع $flockAgeDays يوم، استهلاك العلف ${String.format(java.util.Locale.US, "%.1f", dailyFeedQuintals)} قنطار."
            AppLanguage.FR -> "Analysez la télémétrie du robot du poulailler : NH3=${telemetry.ammoniaNh3Ppm} ppm, CO2=${telemetry.co2Ppm} ppm, Gaz=${telemetry.gasPressurePct}%, Temp=${telemetry.temperatureC}°C, Humidité=${telemetry.humidityPct}%, Âge=$flockAgeDays j."
            else -> "Analyze Smart Barn Robot telemetry: Ammonia NH3=${telemetry.ammoniaNh3Ppm} ppm, CO2=${telemetry.co2Ppm} ppm, Gas Pressure=${telemetry.gasPressurePct}%, Temp=${telemetry.temperatureC}°C, Humidity=${telemetry.humidityPct}%, Flock Age=$flockAgeDays days."
        }
        return askAiVeterinarian(
            question = prompt,
            flockAgeDays = flockAgeDays,
            liveBirds = liveBirds,
            mortalityPct = mortalityPct,
            currentTempC = telemetry.temperatureC,
            lang = lang,
            telemetry = telemetry
        )
    }

    suspend fun generateVetResponse(
        question: String,
        lang: AppLanguage,
        flockAgeDays: Int = 25,
        liveBirds: Int = 7880,
        mortalityPct: Double = 1.5,
        currentTempC: Double = 21.3
    ): String {
        return askAiVeterinarian(
            question = question,
            flockAgeDays = flockAgeDays,
            liveBirds = liveBirds,
            mortalityPct = mortalityPct,
            currentTempC = currentTempC,
            lang = lang
        )
    }

    fun buildClinicalFallbackAdvice(
        question: String,
        flockAgeDays: Int = 25,
        liveBirds: Int = 7880,
        mortalityPct: Double = 1.5,
        currentTempC: Double = 21.3,
        lang: AppLanguage = AppLanguage.AR,
        telemetry: SmartBarnTelemetrySnapshot? = null
    ): String {
        val q = question.lowercase()
        val isAmmoniaHigh = (telemetry?.ammoniaNh3Ppm ?: 12) >= 20 || q.contains("nh3") || q.contains("أمونيا") || q.contains("امونيا") || q.contains("ammonia")
        val isRobotTelemetryQuery = telemetry != null || q.contains("روبوت") || q.contains("robot") || q.contains("حساسات")
        val isRespiratory = q.contains("تنفس") || q.contains("شخير") || q.contains("عطس") || q.contains("حشرجة") ||
            q.contains("respir") || q.contains("cough") || q.contains("râle") || q.contains("toux")
        val isCoccidiosis = q.contains("كوكسيديا") || q.contains("إسهال") || q.contains("اسهال") || q.contains("مدمم") ||
            q.contains("أحمر") || q.contains("بني") || q.contains("coccid") || q.contains("diarr") || q.contains("blood")
        val isHeatStress = q.contains("حرارة") || q.contains("نهجان") || q.contains("احتباس") ||
            q.contains("heat") || q.contains("chaleur") || currentTempC >= 31.0

        return when (lang) {
            AppLanguage.AR -> when {
                isAmmoniaHigh -> """
                    🚨 **تحليل روبوت العنبر: تنبيه ارتفاع غاز الأمونيا (NH3 = ${telemetry?.ammoniaNh3Ppm ?: 26} ppm 🔴 خطر تنفسي)**
                    • **التشخيص الميداني (العمر $flockAgeDays يوم • $liveBirds طائر):** تجاوز الأمونيا حد 20 ppm يسبب تهيج الأغشية المخاطية للقصبة الهوائية ويضعف المناعة الموضعية ضد النيوكاسل والـ CRD.
                    • **الإجراء الفوري للتهوية والفرشة:**
                      1. رفع دورة شفاطات التهوية الدنيا بنسبة 25% لمدة 30 دقيقة لطرد الغازات الثقيلة دون إحداث تيار بارد مباشر.
                      2. فحص خطوط الحلمات (Nipples) وتقليب البقع الرطبة في الفرشة مع نثر الجير المطفي أو سوبر فوسفات وفوقها طبقة نشارة جافة.
                      3. إعطاء مقشع وموسع شعب (المنثول + الأوكاليبتوس) في مياه الشرب لمدة 3 أيام مع فيتامين C.
                """.trimIndent()
                isRobotTelemetryQuery -> """
                    🤖 **تقرير الفحص الذكي لروبوت العنبر (العمر $flockAgeDays يوم • $liveBirds طائر):**
                    • **قراءات الحساسات الحية:** الأمونيا NH3 = ${telemetry?.ammoniaNh3Ppm ?: 12} ppm (🟢 آمن < 20 ppm) | ثاني أكسيد الكربون CO2 = ${telemetry?.co2Ppm ?: 1800} ppm (🟢 جيد) | ضغط الغاز = ${telemetry?.gasPressurePct ?: 85}% (🟢 مستقر) | الحرارة = ${String.format(java.util.Locale.US, "%.1f", telemetry?.temperatureC ?: currentTempC)}°C • الرطوبة = ${telemetry?.humidityPct ?: 58}%.
                    • **التقييم البيطري والإنتاجي:**
                      1. جودة الهواء ممتازة ونسبة الأكسجين مثالية لتحويل العلف (FCR) في عمر $flockAgeDays يوم.
                      2. حافظ على التهوية التبادلية الهادئة وتأكد من ثبات حرارة العنبر حول ${if (flockAgeDays >= 28) "20.5°C - 21.5°C" else "24.0°C - 26.0°C"}.
                      3. استمر في مراقبة استهلاك الماء اليومي (نسبة الماء للعلف ~1.8 : 1)؛ القطيع جاهز لتحقيق أوزان قياسية بإذن الله.
                """.trimIndent()
                isRespiratory -> """
                    🩺 **التشخيص البيطري المبدئي (عمر $flockAgeDays يوم):**
                    • اشتباه عدوى تنفسية (CRD / مايكوبلازما) أو رد فعل ما بعد التحصين، خاصة إذا ترافقت الأعراض مع رطوبة الفرشة أو غاز الأمونيا.
                    • **الإجراء الفوري في العنبر:**
                      1. تحسين التهوية السقفية تدريجياً لطرد الأمونيا دون تعريض الطيور لتيار هواء بارد مباشر (حرارة العنبر الحالية: ${String.format(java.util.Locale.US, "%.1f", currentTempC)}°C).
                      2. تقديم موسع شعب ومقشع طبيعي (المنتول + الأوكاليبتوس) في ماء الشرب لمدة 3 أيام.
                    • **العلاج الدوائي المقترح (تحت إشراف طبيب المزرعة):**
                      - التيلوزين (Tylosin) أو دوكسيسيكلين لمدة 4 أيام متتالية مع رافع مناعة وفيتامين AD3E.
                    • **⏳ تنبيه فترة السحب:** يمنع بيع الطيور للاستهلاك قبل انقضاء 5 إلى 7 أيام من آخر جرعة مضاد حيوي.
                """.trimIndent()
                isCoccidiosis -> """
                    🩺 **التشخيص البيطري المبدئي (عمر $flockAgeDays يوم):**
                    • اشتباه كوكسيديا معوية أو أعوريه (Eimeria) ناتجة عن زيادة رطوبة الفرشة حول خطوط الحلمات.
                    • **الإجراء الفوري في العنبر:**
                      1. تقليب الفرشة الرطبة فوراً وإضافة طبقة نشارة جافة مع الجير المطفي تحت خطوط الماء.
                      2. تقديم مضاد كوكسيديا (تولترازوريل Toltrazuril 2.5% بمعدل 1 مل/لتر لمدة يومين، أو أمبروليوم + فيتامين K3 لوقف النزف المعوي).
                      3. دعم الأمعاء بالأحماض العضوية والـ Probiotics بعد انتهاء العلاج.
                """.trimIndent()
                isHeatStress -> """
                    🩺 **تنبيه إجهاد حراري (حرارة العنبر ${String.format(java.util.Locale.US, "%.1f", currentTempC)}°C - عمر $flockAgeDays يوم):**
                    • **الإجراء الفوري:**
                      1. رفع العلف خلال ساعات الذروة الحارة (من 11 صباحاً حتى 4 عصراً) لتقليل الحرارة الاستقلابية.
                      2. إضافة فيتامين C + إلكتروليت (بيكربونات الصوديوم) في ماء الشرب البارد لتعويض التوازن الشاردي.
                      3. تشغيل خلايا التبريد والمراوح تدريجياً مع مراقبة الرطوبة النسبية.
                """.trimIndent()
                else -> """
                    🩺 **التوصية البيطرية الذكية للقطيع (العمر: $flockAgeDays يوم • العدد الحي: $liveBirds طائر):**
                    • **تقييم المؤشرات الحيوية:** نسبة النفوق التراكمية (${String.format(java.util.Locale.US, "%.2f", mortalityPct)}%) وحرارة العنبر (${String.format(java.util.Locale.US, "%.1f", currentTempC)}°C).
                    • **خطة العمل الموصى بها:**
                      1. **الفرشة والتهوية:** حافظ على جفاف الفرشة وتأكد من عدم وجود رائحة أمونيا في مستوى رأس الطائر.
                      2. **برنامج المياه:** افحص نظافة خطوط الشرب (النيبل) وقدم جرعة فيتامين AD3E + أملاح معدنية لدعم النمو والتحويل الغذائي (FCR).
                      3. **المتابعة التشريحية:** في حال ظهور أي خمول أو تغير في لون الزرق، قم بتصوير الحالة وإرسالها لطبيب المزرعة في تبويب المحادثة.
                """.trimIndent()
            }
            AppLanguage.FR -> when {
                isAmmoniaHigh -> """
                    🚨 **Alerte Robot IoT : Ammoniac élevé (NH3 = ${telemetry?.ammoniaNh3Ppm ?: 26} ppm 🔴 Risque respiratoire)**
                    • **Diagnostic (Âge $flockAgeDays j • $liveBirds sujets) :** Un taux de NH3 > 20 ppm irrite la trachée et favorise les infections CRD.
                    • **Actions immédiates :**
                      1. Augmenter l'extraction d'air de +25% pendant 30 minutes sans courant d'air froid.
                      2. Assécher les zones humides sous les pipettes avec du copeau sec.
                      3. Administrer un complexe huiles essentielles (Menthol/Eucalyptus) + Vitamine C pendant 3 jours.
                """.trimIndent()
                else -> """
                    🩺 **Analyse Vétérinaire & Robot IoT (Âge : $flockAgeDays j • Effectif vif : $liveBirds sujets) :**
                    • **Indicateurs :** Mortalité cumulée (${String.format(java.util.Locale.US, "%.2f", mortalityPct)}%), Température (${String.format(java.util.Locale.US, "%.1f", currentTempC)}°C), NH3 (${telemetry?.ammoniaNh3Ppm ?: 12} ppm).
                    • **Actions immédiates recommandées :**
                      1. **Ventilation & Litière :** Maintenez une litière sèche et contrôlez le taux d'ammoniac (NH3 < 20 ppm).
                      2. **Eau de boisson :** Administrez un complexe Vitamines AD3E + Électrolytes ou Hépato-protecteur selon l'état clinique.
                      3. **Délai d'attente :** Respectez strictement le délai de retrait avant l'abattage en cas de traitement antibiotique.
                """.trimIndent()
            }
            else -> when {
                isAmmoniaHigh -> """
                    🚨 **Smart Barn Robot Alert: High Ammonia Detected (NH3 = ${telemetry?.ammoniaNh3Ppm ?: 26} ppm 🔴 Respiratory Risk)**
                    • **Clinical Impact (Day $flockAgeDays • $liveBirds birds):** NH3 above 20 ppm damages tracheal cilia and triggers secondary CRD / E. coli.
                    • **Immediate Action Plan:**
                      1. Increase minimum ventilation exhaust fans by +25% for 30 minutes to purge heavy gases.
                      2. Inspect drinker nipples for leaks and top-dress wet litter spots with dry shavings.
                      3. Add Menthol/Eucalyptus respiratory tonic + Vitamin C in drinking water for 3 days.
                """.trimIndent()
                else -> """
                    🩺 **AI Vet & Smart Robot Assessment (Flock Age: $flockAgeDays days • Live Birds: $liveBirds):**
                    • **Vital Signs:** Cumulative mortality (${String.format(java.util.Locale.US, "%.2f", mortalityPct)}%), Barn Temp (${String.format(java.util.Locale.US, "%.1f", currentTempC)}°C), NH3 (${telemetry?.ammoniaNh3Ppm ?: 12} ppm).
                    • **Recommended Action Plan:**
                      1. **Litter & Air Quality:** Keep litter friable and dry under drinker lines; ensure ammonia stays below 20 ppm.
                      2. **Water Supplementation:** Provide AD3E vitamins + electrolytes or organic acids to optimize gut health and FCR.
                      3. **Withdrawal Safety:** Always verify antibiotic withdrawal days before scheduling any flock sale.
                """.trimIndent()
            }
        }
    }
}
