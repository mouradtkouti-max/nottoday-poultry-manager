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

    fun darwinPlateRomanFor(visualType: InfographicVisualType): String {
        return when (visualType) {
            InfographicVisualType.CARE_PREHEAT_PREP -> "I"
            InfographicVisualType.CARE_BROODING_BEHAVIOR -> "II"
            InfographicVisualType.CARE_VENTILATION_AIRFLOW -> "III"
            InfographicVisualType.CARE_LIGHTING_PRESALE -> "IV"
            InfographicVisualType.VAX_RECEPTION_WATER -> "V"
            InfographicVisualType.VAX_NEWCASTLE_EYE_SPRAY -> "VI"
            InfographicVisualType.VAX_GUMBORO_DRINKING -> "VII"
            InfographicVisualType.VAX_BOOSTER_DETOX -> "VIII"
            InfographicVisualType.DIS_IB_BRONCHITIS -> "IX"
            InfographicVisualType.DIS_CRD_MYCOPLASMA -> "X"
            InfographicVisualType.DIS_COCCIDIOSIS_GUT -> "XI"
            InfographicVisualType.DIS_ND_IBD_HEAT -> "XII"
            InfographicVisualType.FEED_STARTER_SPEC -> "XIII"
            InfographicVisualType.FEED_GROWER_HEIGHT -> "XIV"
            InfographicVisualType.FEED_FINISHER_PELLETS -> "XV"
            InfographicVisualType.FEED_WEIGHT_WATER_CURVE -> "XVI"
        }
    }

    fun buildPromptForHero(lessonId: String, visualType: InfographicVisualType): String {
        val sceneSubject = when (visualType) {
            InfographicVisualType.CARE_PREHEAT_PREP ->
                "Darwin-era copperplate engraving and vintage watercolor of a disinfected poultry barn floor with 5-8 cm golden wood shavings, radiant brass brooder canopy, and glass mercury thermometer at 33.5C"
            InfographicVisualType.CARE_BROODING_BEHAVIOR ->
                "Darwin-era scientific naturalist diagram comparing three brooding distribution rings: chicks evenly dispersed in comfort zone vs huddled under heater vs pushed to outer walls, plus anatomical crop cross-section filled with water and feed"
            InfographicVisualType.CARE_VENTILATION_AIRFLOW ->
                "Darwin-era architectural cross-section copperplate engraving of a broiler house showing negative-pressure upper inlet air currents, moisture removal, and dry litter bed"
            InfographicVisualType.CARE_LIGHTING_PRESALE ->
                "Darwin-era naturalist illustration of mature white Ross 308 broilers resting under calm photoperiod lighting with raised feeder lines during pre-market fasting"
            InfographicVisualType.VAX_RECEPTION_WATER ->
                "Darwin-era apothecary and veterinary watercolor plate showing day-old chick crop hydration, glass electrolyte and AD3E tonic flasks, and bell drinker cross-section (Day 1-3)"
            InfographicVisualType.VAX_NEWCASTLE_EYE_SPRAY ->
                "Darwin-era anatomical copperplate illustration of avian ocular Harderian gland immunization: precise eye-dropper pipette delivering blue vaccine drop and fine 150-micron spray droplets over chicks (Day 7)"
            InfographicVisualType.VAX_GUMBORO_DRINKING ->
                "Darwin-era scientific veterinary plate showing Bursa of Fabricius lymphoid protection, cold-chain vaccine vial in ice bath, and chlorine-free drinking water line immunization (Day 12-14)"
            InfographicVisualType.VAX_BOOSTER_DETOX ->
                "Darwin-era anatomical and botanical watercolor plate showing avian tracheal mucosal immunity (LaSota Day 18-20) alongside hepatorenal botanical tonic and Selenium-Vitamin E flasks"
            InfographicVisualType.DIS_IB_BRONCHITIS ->
                "Darwin-era veterinary pathology copperplate engraving of avian trachea and bronchi cross-section comparing healthy airway vs Infectious Bronchitis mucus plug and cold draft arrows"
            InfographicVisualType.DIS_CRD_MYCOPLASMA ->
                "Darwin-era scientific anatomical illustration of avian thoracic air sacs and lungs showing ammonia gas (>20 ppm) irritation vs clean ventilated air sac membrane"
            InfographicVisualType.DIS_COCCIDIOSIS_GUT ->
                "Darwin-era naturalist parasitology plate showing avian intestinal ceca cross-section, Eimeria oocyst lifecycle in wet litter (>70% humidity), and dry wood shavings barrier"
            InfographicVisualType.DIS_ND_IBD_HEAT ->
                "Darwin-era scientific field plate illustrating avian thermoregulation under heat stress, evaporative cooling pad airflow, and biosecurity footbath barrier at barn entrance"
            InfographicVisualType.FEED_STARTER_SPEC ->
                "Darwin-era comparative zoological plate showing three broiler growth stages (Day 1-10 Starter crumble 23% protein, Day 11-24 Grower pellet 21.5%, Day 25+ Finisher pellet 19.5%)"
            InfographicVisualType.FEED_GROWER_HEIGHT ->
                "Darwin-era mechanical and anatomical diagram showing suspended pan feeder adjusted precisely to the horizontal plane of the broiler's dorsal back line to prevent feed spillage"
            InfographicVisualType.FEED_FINISHER_PELLETS ->
                "Darwin-era agricultural study of 3-day progressive feed transition blending (75/25, 50/50, 25/75) and dry elevated wooden pallet storage protecting grain from Aspergillus mycotoxins"
            InfographicVisualType.FEED_WEIGHT_WATER_CURVE ->
                "Darwin-era precision brass balance scale weighing a 2.8 kg Ross 308 broiler alongside a graduated glass cylinder measuring the 1.8:1 water-to-feed intake ratio"
        }
        return "19th-century Charles Darwin naturalist manuscript copperplate engraving with delicate hand-tinted sepia and botanical watercolor wash on aged parchment (#F3E9D2). " +
            "Plate ${darwinPlateRomanFor(visualType)} ($lessonId): $sceneSubject. " +
            "Museum-grade scientific clarity for farmers. " +
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
