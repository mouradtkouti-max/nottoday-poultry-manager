package com.example.models

import java.io.Serializable

enum class IndoorReadingSource {
    MANUAL_REPORT,      // Sourced from latest farmer-entered daily report
    CONNECTED_SENSOR,   // Sourced from a genuine connected IoT hardware sensor (currently none verified)
    SIMULATION          // Explicit demo / simulation mode
}

/**
 * Single typed state representing indoor telemetry for a selected barn.
 * Eliminates contradictions between cards and avoids fabricated default temperatures or ammonia.
 */
data class IndoorReadingState(
    val barnId: String,
    val temperatureC: Double?,
    val humidityPct: Double?,
    val ammoniaPpm: Double?,
    val source: IndoorReadingSource,
    val measuredAtMillis: Long?,
    val isVerifiedSensorConnected: Boolean = false,
    val ventilationStatus: String? = null
) : Serializable {
    val hasAnyReading: Boolean
        get() = temperatureC != null || humidityPct != null || ammoniaPpm != null

    val isFresh: Boolean
        get() {
            val ts = measuredAtMillis ?: return false
            return (System.currentTimeMillis() - ts) < 24 * 60 * 60 * 1000L
        }

    companion object {
        fun noReading(barnId: String): IndoorReadingState = IndoorReadingState(
            barnId = barnId,
            temperatureC = null,
            humidityPct = null,
            ammoniaPpm = null,
            source = IndoorReadingSource.MANUAL_REPORT,
            measuredAtMillis = null,
            isVerifiedSensorConnected = false,
            ventilationStatus = null
        )

        fun fromManualReport(
            barnId: String,
            report: DailyReportModel?
        ): IndoorReadingState {
            if (report == null) return noReading(barnId)
            val temp: Double? = report.temperature
            // Note: report.gasUnits represents recorded heating gas / heater bottles or burner usage,
            // NOT sensor-measured ammonia NH3 ppm. We do not fabricate NH3 from gas usage.
            return IndoorReadingState(
                barnId = barnId,
                temperatureC = temp,
                humidityPct = null,
                ammoniaPpm = null,
                source = IndoorReadingSource.MANUAL_REPORT,
                measuredAtMillis = report.updatedAtMillis,
                isVerifiedSensorConnected = false,
                ventilationStatus = report.ventilationStatus.takeIf { it.isNotBlank() }
            )
        }

        fun simulation(
            barnId: String,
            temperatureC: Double = 21.3,
            humidityPct: Double = 58.0,
            ammoniaPpm: Double = 12.0
        ): IndoorReadingState = IndoorReadingState(
            barnId = barnId,
            temperatureC = temperatureC,
            humidityPct = humidityPct,
            ammoniaPpm = ammoniaPpm,
            source = IndoorReadingSource.SIMULATION,
            measuredAtMillis = System.currentTimeMillis(),
            isVerifiedSensorConnected = false,
            ventilationStatus = "محاكاة"
        )
    }
}

/**
 * Confirmed locality/coordinates for weather lookups on a per-barn, per-account basis.
 */
data class BarnWeatherLocation(
    val barnId: String,
    val localityName: String,
    val country: String,
    val region: String,
    val latitude: Double,
    val longitude: Double,
    val isEnabled: Boolean = false,
    val updatedAtMillis: Long = System.currentTimeMillis()
) : Serializable

/**
 * Outdoor weather data fetched via Open-Meteo HTTPS API.
 * Provider/model-based outdoor estimate, NOT on-site sensor telemetry.
 */
data class OutdoorWeatherSnapshot(
    val barnId: String,
    val localityName: String,
    val latitude: Double,
    val longitude: Double,
    val temperatureC: Double,
    val apparentTemperatureC: Double?,
    val relativeHumidityPct: Int?,
    val weatherCode: Int,
    val windSpeedKmh: Double?,
    val precipitationMm: Double?,
    val isDay: Boolean?,
    val providerDataTimeIso: String,
    val fetchedAtMillis: Long,
    val providerAttribution: String = "Open-Meteo (open-meteo.com)",
    val isCached: Boolean = false
) : Serializable {
    val isFresh: Boolean
        get() = (System.currentTimeMillis() - fetchedAtMillis) < 30 * 60 * 1000L // 30 minutes fresh cache

    fun conditionDescription(lang: String = "ar"): String {
        return when (weatherCode) {
            0 -> if (lang == "ar") "سماء صافية ☀️" else "Clear sky ☀️"
            1, 2, 3 -> if (lang == "ar") "غائم جزئياً ⛅" else "Partly cloudy ⛅"
            45, 48 -> if (lang == "ar") "ضباب 🌫️" else "Fog 🌫️"
            51, 53, 55 -> if (lang == "ar") "رذاذ مطر 🌦️" else "Drizzle 🌦️"
            61, 63, 65 -> if (lang == "ar") "أمطار 🌧️" else "Rain 🌧️"
            71, 73, 75 -> if (lang == "ar") "ثلوج ❄️" else "Snow ❄️"
            80, 81, 82 -> if (lang == "ar") "زخات مطرية 🌧️" else "Rain showers 🌧️"
            95, 96, 99 -> if (lang == "ar") "عواصف رعدية ⛈️" else "Thunderstorm ⛈️"
            else -> if (lang == "ar") "طقس معتدل 🌤️" else "Moderate 🌤️"
        }
    }
}
