package com.example.utils

import android.content.Context
import com.example.models.BarnWeatherLocation
import com.example.models.OutdoorWeatherSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Result state sealed hierarchy for Weather operations.
 */
sealed class WeatherResult<out T> {
    data class Success<out T>(val data: T, val isStale: Boolean = false) : WeatherResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : WeatherResult<Nothing>()
    object NoLocation : WeatherResult<Nothing>()
    object Disabled : WeatherResult<Nothing>()
    object Loading : WeatherResult<Nothing>()
}

/**
 * Service and Repository for real outdoor weather via Open-Meteo.
 * Supports geocoding/locality search, coordinate input, account+barn local caching,
 * 30-minute throttling, timeouts, cancellation, and clear provider attribution.
 */
object BarnWeatherRepository {

    private const val PREFS_NAME = "agritech_weather_prefs"
    private const val KEY_LOCATION_PREFIX = "weather_loc_"
    private const val KEY_SNAPSHOT_PREFIX = "weather_snap_"
    private const val CONNECT_TIMEOUT_MS = 8000
    private const val READ_TIMEOUT_MS = 10000
    private const val CACHE_FRESHNESS_MS = 30 * 60 * 1000L // 30 minutes

    /**
     * Local storage for user-confirmed barn weather locations per account + barn.
     */
    fun getSavedLocation(context: Context, accountUid: String, barnId: String): BarnWeatherLocation? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(locKey(accountUid, barnId), null) ?: return null
        return try {
            val json = JSONObject(raw)
            BarnWeatherLocation(
                barnId = json.getString("barnId"),
                localityName = json.getString("localityName"),
                country = json.optString("country", ""),
                region = json.optString("region", ""),
                latitude = json.getDouble("latitude"),
                longitude = json.getDouble("longitude"),
                isEnabled = json.optBoolean("isEnabled", false),
                updatedAtMillis = json.optLong("updatedAtMillis", System.currentTimeMillis())
            )
        } catch (_: Exception) {
            null
        }
    }

    fun saveLocation(context: Context, accountUid: String, location: BarnWeatherLocation) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = JSONObject().apply {
            put("barnId", location.barnId)
            put("localityName", location.localityName)
            put("country", location.country)
            put("region", location.region)
            put("latitude", location.latitude)
            put("longitude", location.longitude)
            put("isEnabled", location.isEnabled)
            put("updatedAtMillis", location.updatedAtMillis)
        }
        prefs.edit().putString(locKey(accountUid, location.barnId), json.toString()).apply()
    }

    fun getCachedSnapshot(context: Context, accountUid: String, barnId: String): OutdoorWeatherSnapshot? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(snapKey(accountUid, barnId), null) ?: return null
        return try {
            val json = JSONObject(raw)
            OutdoorWeatherSnapshot(
                barnId = json.getString("barnId"),
                localityName = json.getString("localityName"),
                latitude = json.getDouble("latitude"),
                longitude = json.getDouble("longitude"),
                temperatureC = json.getDouble("temperatureC"),
                apparentTemperatureC = if (json.has("apparentTemperatureC")) json.getDouble("apparentTemperatureC") else null,
                relativeHumidityPct = if (json.has("relativeHumidityPct")) json.getInt("relativeHumidityPct") else null,
                weatherCode = json.optInt("weatherCode", 0),
                windSpeedKmh = if (json.has("windSpeedKmh")) json.getDouble("windSpeedKmh") else null,
                precipitationMm = if (json.has("precipitationMm")) json.getDouble("precipitationMm") else null,
                isDay = if (json.has("isDay")) json.getBoolean("isDay") else null,
                providerDataTimeIso = json.optString("providerDataTimeIso", ""),
                fetchedAtMillis = json.optLong("fetchedAtMillis", 0L),
                providerAttribution = json.optString("providerAttribution", "Open-Meteo (open-meteo.com)"),
                isCached = true
            )
        } catch (_: Exception) {
            null
        }
    }

    fun saveCachedSnapshot(context: Context, accountUid: String, snapshot: OutdoorWeatherSnapshot) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = JSONObject().apply {
            put("barnId", snapshot.barnId)
            put("localityName", snapshot.localityName)
            put("latitude", snapshot.latitude)
            put("longitude", snapshot.longitude)
            put("temperatureC", snapshot.temperatureC)
            snapshot.apparentTemperatureC?.let { put("apparentTemperatureC", it) }
            snapshot.relativeHumidityPct?.let { put("relativeHumidityPct", it) }
            put("weatherCode", snapshot.weatherCode)
            snapshot.windSpeedKmh?.let { put("windSpeedKmh", it) }
            snapshot.precipitationMm?.let { put("precipitationMm", it) }
            snapshot.isDay?.let { put("isDay", it) }
            put("providerDataTimeIso", snapshot.providerDataTimeIso)
            put("fetchedAtMillis", snapshot.fetchedAtMillis)
            put("providerAttribution", snapshot.providerAttribution)
        }
        prefs.edit().putString(snapKey(accountUid, snapshot.barnId), json.toString()).apply()
    }

    /**
     * Searches Open-Meteo Geocoding API for localities matching query.
     */
    suspend fun searchLocalities(query: String, count: Int = 5): List<BarnWeatherLocation> {
        val clean = query.trim()
        if (clean.length < 2) return emptyList()
        return withContext(Dispatchers.IO) {
            try {
                val encoded = URLEncoder.encode(clean, "UTF-8")
                val urlStr = "https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=$count&language=ar&format=json"
                val url = URL(urlStr)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = CONNECT_TIMEOUT_MS
                    readTimeout = READ_TIMEOUT_MS
                    setRequestProperty("Accept", "application/json")
                }
                if (conn.responseCode != 200) return@withContext emptyList()
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val responseText = reader.readText()
                reader.close()
                parseGeocodingJson(responseText)
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    fun parseGeocodingJson(jsonStr: String): List<BarnWeatherLocation> {
        val results = mutableListOf<BarnWeatherLocation>()
        try {
            val root = JSONObject(jsonStr)
            val items = root.optJSONArray("results") ?: return emptyList()
            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val name = item.getString("name")
                val country = item.optString("country", "")
                val admin1 = item.optString("admin1", "")
                val lat = item.getDouble("latitude")
                val lon = item.getDouble("longitude")
                results.add(
                    BarnWeatherLocation(
                        barnId = "",
                        localityName = name,
                        country = country,
                        region = admin1,
                        latitude = lat,
                        longitude = lon,
                        isEnabled = true
                    )
                )
            }
        } catch (_: Exception) {}
        return results
    }

    /**
     * Fetches current outdoor weather from Open-Meteo for the confirmed barn location.
     */
    suspend fun fetchWeather(
        context: Context,
        accountUid: String,
        location: BarnWeatherLocation,
        forceRefresh: Boolean = false
    ): WeatherResult<OutdoorWeatherSnapshot> {
        if (!location.isEnabled) return WeatherResult.Disabled

        val cached = getCachedSnapshot(context, accountUid, location.barnId)
        if (!forceRefresh && cached != null && cached.isFresh) {
            return WeatherResult.Success(cached, isStale = false)
        }

        return withContext(Dispatchers.IO) {
            try {
                val lat = location.latitude
                val lon = location.longitude
                val urlStr = "https://api.open-meteo.com/v1/forecast?" +
                    "latitude=$lat&longitude=$lon" +
                    "&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,wind_speed_10m" +
                    "&timezone=auto"

                val url = URL(urlStr)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = CONNECT_TIMEOUT_MS
                    readTimeout = READ_TIMEOUT_MS
                    setRequestProperty("Accept", "application/json")
                }

                if (conn.responseCode != 200) {
                    return@withContext if (cached != null) {
                        WeatherResult.Success(cached, isStale = true)
                    } else {
                        WeatherResult.Error("HTTP ${conn.responseCode} from weather provider")
                    }
                }

                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val jsonStr = reader.readText()
                reader.close()

                val parsed = parseWeatherJson(location, jsonStr)
                saveCachedSnapshot(context, accountUid, parsed)
                WeatherResult.Success(parsed, isStale = false)
            } catch (e: Exception) {
                if (cached != null) {
                    WeatherResult.Success(cached, isStale = true)
                } else {
                    WeatherResult.Error(e.localizedMessage ?: "Failed to reach weather provider", e)
                }
            }
        }
    }

    fun parseWeatherJson(location: BarnWeatherLocation, jsonStr: String): OutdoorWeatherSnapshot {
        val root = JSONObject(jsonStr)
        val current = root.getJSONObject("current")
        val temp = current.getDouble("temperature_2m")
        val apparent = if (current.has("apparent_temperature")) current.getDouble("apparent_temperature") else null
        val humidity = if (current.has("relative_humidity_2m")) current.getInt("relative_humidity_2m") else null
        val code = current.optInt("weather_code", 0)
        val wind = if (current.has("wind_speed_10m")) current.getDouble("wind_speed_10m") else null
        val precip = if (current.has("precipitation")) current.getDouble("precipitation") else null
        val isDayInt = current.optInt("is_day", -1)
        val isDay = if (isDayInt >= 0) isDayInt == 1 else null
        val timeIso = current.optString("time", "")

        return OutdoorWeatherSnapshot(
            barnId = location.barnId,
            localityName = location.localityName,
            latitude = location.latitude,
            longitude = location.longitude,
            temperatureC = temp,
            apparentTemperatureC = apparent,
            relativeHumidityPct = humidity,
            weatherCode = code,
            windSpeedKmh = wind,
            precipitationMm = precip,
            isDay = isDay,
            providerDataTimeIso = timeIso,
            fetchedAtMillis = System.currentTimeMillis(),
            providerAttribution = "Open-Meteo (open-meteo.com)",
            isCached = false
        )
    }

    private fun locKey(uid: String, barnId: String) = "${KEY_LOCATION_PREFIX}${uid}_$barnId"
    private fun snapKey(uid: String, barnId: String) = "${KEY_SNAPSHOT_PREFIX}${uid}_$barnId"
}
