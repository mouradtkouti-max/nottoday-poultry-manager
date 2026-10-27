package com.example.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "AppCheckInit"

// App Check contract keys defined by the Firebase SDK and runtime harness.
private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

/**
 * Configures Firebase App Check before any UI or Firebase AI Logic operations.
 * Uses synchronous `.commit()` in debug builds to ensure the token is written before
 * `DebugAppCheckProviderFactory` initializes.
 */
fun configureAppCheck(context: Context, intent: Intent?) {
    try {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }

        if (BuildConfig.DEBUG) {
            val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)

            if (!debugToken.isNullOrEmpty()) {
                val persistenceKey = FirebaseApp.getInstance().persistenceKey
                val prefs = context.getSharedPreferences(
                    "$DEBUG_STORE_PREFIX$persistenceKey",
                    Context.MODE_PRIVATE
                )
                prefs.edit()
                    .putString(DEBUG_SECRET_KEY, debugToken)
                    .commit()
                Log.d(TAG, "App Check debug secret configured successfully.")
            } else {
                Log.w(TAG, "No debug token found in Intent extra.")
            }

            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Log.d(TAG, "DebugAppCheckProviderFactory installed.")
        } else {
            val resId = context.resources.getIdentifier(RECAPTCHA_SITE_KEY_RES, "string", context.packageName)
            if (resId != 0) {
                val siteKey = context.getString(resId)
                if (siteKey.isNotBlank()) {
                    FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                        RecaptchaAppCheckProviderFactory.getInstance(siteKey)
                    )
                    Log.d(TAG, "RecaptchaAppCheckProviderFactory installed.")
                } else {
                    Log.e(TAG, "recaptcha_site_key resource string is blank.")
                }
            } else {
                Log.e(TAG, "recaptcha_site_key string resource not found in package.")
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize Firebase App Check", e)
    }
}

/**
 * Gemini AI Veterinary Service for `OwnerGlobalAiVetPlaceholderScreen`.
 *
 * ### Initialization Snippet (Firebase AI Logic SDK — Production Recommended):
 * ```kotlin
 * val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI())
 *     .generativeModel(
 *         modelName = "gemini-3.8-flash",
 *         systemInstruction = content {
 *             text("أنت طبيب بيطري خبير في إدارة مزارع دجاج التسمين (سلالة Ross 308)...")
 *         }
 *     )
 * ```
 *
 * ### Initialization Snippet (Standalone Google AI Client SDK with API Key):
 * ```kotlin
 * // build.gradle.kts: implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
 * val generativeModel = com.google.ai.client.generativeai.GenerativeModel(
 *     modelName = "gemini-2.5-flash",
 *     apiKey = BuildConfig.GEMINI_API_KEY, // Injected via Secrets Gradle Plugin (.env)
 *     systemInstruction = com.google.ai.client.generativeai.type.content {
 *         text("You are an expert poultry veterinarian specializing in Ross 308 broilers.")
 *     }
 * )
 * ```
 */
object AiVetGeminiService {

    private const val SYSTEM_PROMPT_AR =
        "أنت المساعد البيطري الذكي (AI Vet) المتخصص في مزارع دجاج اللحم (سلالة Ross 308). " +
            "قدّم إرشادات بيطرية عملية ومختصرة ودقيقة حول التهوية، الإجهاد الحراري، برامج التحصين (نيوكاسل، جمبورو، التهاب الشعب)، " +
            "معامل التحويل الغذائي (FCR)، ومؤشر الكفاءة الأوروبي (EPEF)، مع حسابات العلف بالقنطار (ق = 100 كغ)."

    private val generativeModel: GenerativeModel? by lazy {
        try {
            Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
                modelName = "gemini-3.8-flash",
                systemInstruction = content {
                    text(SYSTEM_PROMPT_AR)
                }
            )
        } catch (e: Exception) {
            Log.w("AiVetGeminiService", "GenerativeModel lazy init fallback: ${e.message}")
            null
        }
    }

    suspend fun generateVetResponse(userMessage: String, lang: AppLanguage = AppLanguage.AR): String {
        val cleanQuestion = userMessage.trim()
        if (cleanQuestion.isEmpty()) return ""

        return withContext(Dispatchers.IO) {
            try {
                val model = generativeModel
                if (model != null) {
                    val response = model.generateContent(cleanQuestion)
                    val reply = response.text?.trim()
                    if (!reply.isNullOrBlank()) {
                        return@withContext reply
                    }
                }
            } catch (e: Exception) {
                Log.w("AiVetGeminiService", "Gemini API call failed, using offline clinical knowledge base", e)
            }
            buildClinicalFallbackAdvice(cleanQuestion)
        }
    }

    /**
     * Provides structured Ross 308 clinical veterinary guidance when offline or before backend key/AppCheck provisioning completes.
     */
    fun buildClinicalFallbackAdvice(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("تنفس") || q.contains("حشرجة") || q.contains("تهوية") || q.contains("حرارة") || q.contains("عطس") -> {
                "🩺 تشخيص إرشادي (الأعراض التنفسية والتهوية - Ross 308):\n" +
                    "1. تفقد سرعة الهواء ومستوى الأمونيا فوراً داخل العنبر (يجب ألا يتجاوز 15 ppm).\n" +
                    "2. في حال سماع حشرجة ليلية، يُنصح بموسع شعب هوائية (منثول/أوكاليبتوس) في ماء الشرب لمدة 8 ساعات.\n" +
                    "3. بعد استشارة الطبيب البيطري المشرف، يمكن تطبيق مضاد حيوي تنفسي (مثل تايلوزين أو دوكسي سيكلين 0.5 غ/لتر لمدة 3 أيام)."
            }
            q.contains("تحصين") || q.contains("فيتامين") || q.contains("نيوكاسل") || q.contains("جمبورو") || q.contains("ad3e") -> {
                "💧 برنامج التحصين والفيتامينات القياسي (Ross 308):\n" +
                    "• اليوم 1–3: فيتامين AD3E + إلكترولايت (1 مل/لتر) لتفادي إجهاد النقل.\n" +
                    "• اليوم 7: تحصين نيوكاسل (Hitchner B1 أو Clone 30) + التهاب الشعب الهوائية (IB).\n" +
                    "• اليوم 14: تحصين الجمبورو (IBD) في مياه الشرب الخالية من الكلور مع حليب خالي الدسم.\n" +
                    "• اليوم 18–20: جرعة نيوكاسل المعززة (LaSota) متبوعة برافع مناعة وفيتامين E+سيلينيوم."
            }
            q.contains("تحويل") || q.contains("fcr") || q.contains("وزن") || q.contains("علف") || q.contains("epef") -> {
                "⚖️ تحسين معامل التحويل الغذائي (FCR) والكتلة الحية:\n" +
                    "• المعادلة المعتمدة: FCR = إجمالي العلف المستهلك (كغ) ÷ إجمالي الكتلة الحية المنتجة (شاملة المبيعات الجزئية).\n" +
                    "• عند عمر 35 يوماً، الوزن القياسي لسلالة Ross 308 هو 2.05 كغ ومعامل التحويل المثالي بين 1.55 و 1.65.\n" +
                    "• تأكد من ضبط ارتفاع المعالف بمستوى ظهر الطائر لتقليل الهدر، ومراقبة نسبة استهلاك الماء إلى العلف (حوالي 1.8 : 1)."
            }
            q.contains("اسهال") || q.contains("إسهال") || q.contains("كوكسيديا") || q.contains("فرشة") -> {
                "🧪 بروتوكول التعامل مع الإسهالات وصحة الأمعاء (Coccidiosis / Enteritis):\n" +
                    "• افحص رطوبة الفرشة أسفل خطوط الحلمات وتخلص من البقع المتكتلة فوراً.\n" +
                    "• الإسهال البني أو المدمم يشير غالباً للكوكسيديا (يُعالج بـ تولترازوريل أو أمبروليوم + فيتامين K3).\n" +
                    "• ادعم القطيع بالأحماض العضوية والسموم الفطرية في ماء الشرب لتحسين كفاءة الهضم."
            }
            else -> {
                "🤖 المساعد البيطري الذكي (Ross 308):\n" +
                    "تم استلام استفسارك: \"$query\".\n" +
                    "• يُنصح بمراجعة درجة حرارة العنبر حسب عمر الطائر (اليوم 1: 33°C وتنخفض تدريجياً إلى 21°C في الأسبوع الخامس).\n" +
                    "• راقب استهلاك العلف اليومي بالقنطار (ق) واستهلاك المياه، وفي حال ملاحظة ارتفاع في النفوق اليومي (> 0.1%) يرجى إرسال تنبيه عاجل للطبيب البيطري عبر تبويب (🩺 البيطري)."
            }
        }
    }
}
