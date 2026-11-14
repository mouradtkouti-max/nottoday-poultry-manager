package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.annotation.DrawableRes
import com.example.R
import com.example.models.InfographicVisualType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/**
 * Smart Nano Banana (`gemini-2.5-flash-image`) agricultural encyclopedia illustration generator
 * with persistent disk caching in `File(context.filesDir, "nano_banana_book_cache/{imageKey}.png")`.
 *
 * - Reads API key safely via [AiVetGeminiService.resolveConfiguredApiKey] (uses BuildConfig.GEMINI_API_KEY
 *   when provided via Secrets Gradle Plugin without crashing when absent).
 * - Decodes returned Base64 image parts directly to PNG files on disk for instant 0ms offline loading.
 * - Provides pre-bundled watercolor encyclopedia fallback drawables in `res/drawable/` so every page
 *   always renders a crystal-clear illustration immediately while offline or prior to network fetch.
 */
object BookNanoBananaImageEngine {

    const val MODEL_NAME = "gemini-2.5-flash-image"
    const val NANO_BANANA_ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent"
    const val CACHE_FOLDER_NAME = "nano_banana_book_cache"

    private val memoryCache = ConcurrentHashMap<String, Bitmap>()
    private val inFlightLocks = ConcurrentHashMap<String, Mutex>()

    fun sanitizeImageKey(imageKey: String): String {
        val cleaned = imageKey.trim().replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        return if (cleaned.isNotEmpty()) cleaned else "default_book_image"
    }

    fun cacheDir(context: Context): File = File(context.filesDir, CACHE_FOLDER_NAME)

    fun cacheFileForKey(context: Context, imageKey: String): File {
        val dir = cacheDir(context)
        if (!dir.exists()) {
            runCatching { dir.mkdirs() }
        }
        return File(dir, "${sanitizeImageKey(imageKey)}.png")
    }

    fun loadCachedBitmap(context: Context, imageKey: String): Bitmap? {
        val safeKey = sanitizeImageKey(imageKey)
        memoryCache[safeKey]?.let { return it }
        return runCatching {
            val file = cacheFileForKey(context, safeKey)
            if (file.exists() && file.length() > 0L) {
                BitmapFactory.decodeFile(file.absolutePath)?.also { bmp ->
                    memoryCache[safeKey] = bmp
                }
            } else {
                null
            }
        }.getOrNull()
    }

    fun saveBitmapToCache(context: Context, imageKey: String, bitmap: Bitmap): File? {
        val safeKey = sanitizeImageKey(imageKey)
        return runCatching {
            val target = cacheFileForKey(context, safeKey)
            val tmpFile = File(target.parentFile, "$safeKey.tmp.png")
            FileOutputStream(tmpFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 95, fos)
                fos.flush()
            }
            if (target.exists()) {
                target.delete()
            }
            tmpFile.renameTo(target)
            memoryCache[safeKey] = bitmap
            target
        }.getOrNull()
    }

    fun buildRequestJson(prompt: String): String {
        val part = JSONObject().put("text", prompt)
        val content = JSONObject().put("parts", JSONArray().put(part))
        val generationConfig = JSONObject()
            .put("responseModalities", JSONArray().put("IMAGE"))
        return JSONObject()
            .put("contents", JSONArray().put(content))
            .put("generationConfig", generationConfig)
            .toString()
    }

    /**
     * Extracts Base64 image bytes cleanly from Gemini `generateContent` JSON responses
     * handling both camelCase (`inlineData`) and snake_case (`inline_data`).
     */
    fun parseImageBytesFromResponse(rawJson: String): ByteArray? {
        return runCatching {
            val root = JSONObject(rawJson)
            val candidates = root.optJSONArray("candidates") ?: return@runCatching null
            for (cIdx in 0 until candidates.length()) {
                val cand = candidates.optJSONObject(cIdx) ?: continue
                val content = cand.optJSONObject("content") ?: continue
                val parts = content.optJSONArray("parts") ?: continue
                for (pIdx in 0 until parts.length()) {
                    val part = parts.optJSONObject(pIdx) ?: continue
                    val inlineObj = part.optJSONObject("inlineData")
                        ?: part.optJSONObject("inline_data")
                        ?: continue
                    val b64Data = inlineObj.optString("data", "").trim()
                    if (b64Data.isNotEmpty()) {
                        val decoded = Base64.decode(b64Data, Base64.DEFAULT)
                        if (decoded.isNotEmpty()) return@runCatching decoded
                    }
                }
            }
            null
        }.getOrNull()
    }

    @DrawableRes
    fun bundledFallbackDrawableFor(visualType: InfographicVisualType): Int {
        return when {
            visualType.name.startsWith("CARE_") -> R.drawable.img_book_care_hero
            visualType.name.startsWith("VAX_") -> R.drawable.img_book_vax_hero
            visualType.name.startsWith("DIS_") -> R.drawable.img_book_dis_hero
            else -> R.drawable.img_book_feed_hero
        }
    }

    fun buildPromptForHero(lessonId: String, visualType: InfographicVisualType): String {
        val sceneSubject = when (visualType) {
            InfographicVisualType.CARE_PREHEAT_PREP ->
                "Clean disinfected broiler barn floor covered with golden dry wood shavings under a warm glowing circular brooder heater lamp, small clean red chick waterers and trays ready for chick placement"
            InfographicVisualType.CARE_BROODING_BEHAVIOR ->
                "Happy fluffy yellow day-old broiler chicks evenly spread in a peaceful circle under a warm radiant brooder canopy on golden wood shavings drinking and eating calmly"
            InfographicVisualType.CARE_VENTILATION_AIRFLOW ->
                "Cross section of a bright modern broiler poultry barn with healthy white chickens on dry straw and curved fresh blue air currents entering high upper wall vents gently"
            InfographicVisualType.CARE_LIGHTING_PRESALE ->
                "Healthy mature white broiler chickens resting peacefully under dimmed barn lights beside raised silver suspended pan feeders and water nipple line"
            InfographicVisualType.VAX_RECEPTION_WATER ->
                "Fresh chilled electrolyte drink flowing into clean red bell chick drinkers with healthy yellow day-old chicks drinking eagerly"
            InfographicVisualType.VAX_NEWCASTLE_EYE_SPRAY ->
                "Poultry farmer hands gently holding a yellow chick while applying a single blue droplet eye vaccine near an ice-cooled glass vaccine vial"
            InfographicVisualType.VAX_GUMBORO_DRINKING ->
                "Overhead water nipple line delivering clean drinking water mixed with vaccine stabilizer to eager young broiler chickens on dry straw"
            InfographicVisualType.VAX_BOOSTER_DETOX ->
                "Healthy vigorous grower broiler flock drinking vitamin-rich water in a clean sunlit poultry house"
            InfographicVisualType.DIS_IB_BRONCHITIS ->
                "Poultry veterinarian inspecting clean warm broiler flock inside a well-ventilated poultry barn with crisp fresh air circulation"
            InfographicVisualType.DIS_CRD_MYCOPLASMA ->
                "Airflow management in a bright clean poultry house with open ceiling vents removing dust and keeping white broiler flock healthy"
            InfographicVisualType.DIS_COCCIDIOSIS_GUT ->
                "Dry clean golden poultry litter under raised water nipple drinkers with healthy white broiler chickens free of wet patches"
            InfographicVisualType.DIS_ND_IBD_HEAT ->
                "Cooling tunnel fan misting and shaded cool water lines keeping broiler chickens relaxed and comfortable in warm summer weather"
            InfographicVisualType.FEED_STARTER_SPEC ->
                "Three broiler growth stages left to right: yellow day-old chick, medium grower chicken, and plump white Ross 308 broiler beside suspended red pan feeders filled with golden crumble and pellets"
            InfographicVisualType.FEED_GROWER_HEIGHT ->
                "Broiler chickens eating cleanly from a suspended silver and red pan feeder adjusted precisely to the height of the chicken's back"
            InfographicVisualType.FEED_FINISHER_PELLETS ->
                "Farmer hands gently blending two bowls of golden feed crumble and coarse pellets together on a burlap feed sack"
            InfographicVisualType.FEED_WEIGHT_WATER_CURVE ->
                "Healthy mature white Ross 308 broiler standing next to a classic farm brass dial weighing scale and clean water trough"
        }
        return "Vintage agricultural encyclopedia watercolor and ink illustration on warm cream parchment paper (#F5E6C8). " +
            "Subject ($lessonId): $sceneSubject. " +
            "Crystal clear and immediately understandable for a simple farmer. " +
            "Strictly ZERO text, NO letters, NO numbers, NO words, NO watermark anywhere inside the image."
    }

    fun buildPromptForStep(
        lessonId: String,
        visualType: InfographicVisualType,
        stepIndex: Int
    ): String {
        val stepSubject = when {
            visualType == InfographicVisualType.FEED_STARTER_SPEC -> when (stepIndex) {
                0 -> "Open burlap feed sack filled with fine golden yellow chick starter crumble feed"
                1 -> "Open burlap feed sack filled with medium sized broiler grower pellets"
                else -> "Open burlap feed sack filled with thick coarse golden broiler finisher pellets"
            }
            visualType == InfographicVisualType.CARE_PREHEAT_PREP -> when (stepIndex) {
                0 -> "High-pressure water sprayer washing and disinfecting a clean poultry barn floor"
                1 -> "Farmer spreading thick clean golden wood shavings bedding evenly on a barn floor beside a wooden measuring stick"
                else -> "Glowing radiant brooder heater canopy warming golden litter beside a classic glass wall thermometer"
            }
            visualType == InfographicVisualType.CARE_BROODING_BEHAVIOR -> when (stepIndex) {
                0 -> "Fluffy yellow chicks huddled tightly together shivering directly under heater lamp due to cold floor"
                1 -> "Fluffy yellow chicks spread happily and evenly around feeder and waterer under warm brooder"
                else -> "Yellow chicks pushed far away to outer barn corners panting from excessive heat"
            }
            visualType.name.startsWith("VAX_") -> when (stepIndex) {
                0 -> "Small chilled glass vaccine vials nestled neatly on blue ice packs inside an insulated cooler"
                1 -> "Clean blue eye-dropper pipette placing one clear drop next to an open water drinker"
                else -> "Healthy energetic young chicks drinking vitamin-enriched water calmly"
            }
            visualType.name.startsWith("DIS_") -> when (stepIndex) {
                0 -> "Poultry farmer gently observing an alert white broiler chicken up close in clean barn"
                1 -> "Clean farm veterinary checkup scene with dry clean litter and open ventilated poultry house"
                else -> "Farmer administering vitamin solution into clean drinking water tank for flock recovery"
            }
            else -> when (stepIndex) {
                0 -> "Clean red suspended chick feeder tray filled with fresh golden feed"
                1 -> "Farmer hand adjusting hanging chain height of a broiler pan feeder to bird back level"
                else -> "Plump healthy white broiler standing calmly beside a vintage brass farm scale"
            }
        }
        return "Vintage agricultural encyclopedia watercolor spot illustration on cream parchment paper (#F5E6C8). " +
            "Iconic single subject for farmer ($lessonId step ${stepIndex + 1}): $stepSubject. " +
            "Strictly ZERO text, NO numbers, NO labels anywhere in the drawing."
    }

    private suspend fun executeHttpImageCall(apiKey: String, prompt: String): ByteArray? =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = URL(NANO_BANANA_ENDPOINT)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 15000
                    readTimeout = 45000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    setRequestProperty("x-goog-api-key", apiKey)
                }
                try {
                    OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                        writer.write(buildRequestJson(prompt))
                        writer.flush()
                    }
                    val status = conn.responseCode
                    if (status !in 200..299) {
                        return@runCatching null
                    }
                    val responseBody = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use {
                        it.readText()
                    }
                    parseImageBytesFromResponse(responseBody)
                } finally {
                    conn.disconnect()
                }
            }.getOrNull()
        }

    /**
     * Returns a permanently cached [Bitmap] from `context.filesDir/nano_banana_book_cache/{imageKey}.png`
     * if already generated, or fetches it from Nano Banana (`gemini-2.5-flash-image`), decodes it,
     * and persists it to disk for offline 0ms reuse. Never throws on offline/missing key.
     */
    suspend fun getOrFetchLessonImage(
        context: Context,
        imageKey: String,
        prompt: String,
        apiKeyProvider: () -> String = { AiVetGeminiService.resolveConfiguredApiKey() },
        remoteFetcher: (suspend (apiKey: String, prompt: String) -> ByteArray?)? = null
    ): Bitmap? = withContext(Dispatchers.IO) {
        val safeKey = sanitizeImageKey(imageKey)
        loadCachedBitmap(context, safeKey)?.let { return@withContext it }

        val mutex = inFlightLocks.getOrPut(safeKey) { Mutex() }
        mutex.withLock {
            loadCachedBitmap(context, safeKey)?.let { return@withLock it }

            val apiKey = runCatching { apiKeyProvider().trim() }.getOrDefault("")
            if (apiKey.isEmpty() && remoteFetcher == null) {
                return@withLock null
            }

            val rawBytes = runCatching {
                if (remoteFetcher != null) {
                    remoteFetcher(apiKey, prompt)
                } else {
                    executeHttpImageCall(apiKey, prompt)
                }
            }.getOrNull() ?: return@withLock null

            val bitmap = runCatching {
                BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size)
            }.getOrNull() ?: return@withLock null

            saveBitmapToCache(context, safeKey, bitmap)
            bitmap
        }
    }
}
