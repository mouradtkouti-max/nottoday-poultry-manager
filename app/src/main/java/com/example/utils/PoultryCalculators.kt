package com.example.utils

import com.example.models.CycleModel
import com.example.models.DailyReportModel
import com.example.models.TransactionModel
import com.example.models.TransactionType

// ============================================================================
// CALCULATION LOGIC (Ross 308 Standards & Partial Sales / Thinning Strictness)
// ============================================================================

object PoultryCalculators {
    fun getRealisticFieldWeightGrams(day: Int): Double {
        return when {
            day <= 0 -> 40.0
            day == 1 -> 49.0
            day in 2..6 -> 49.0 + (day - 1) * ((187.0 - 49.0) / 6.0)
            day == 7 -> 187.0
            day in 8..13 -> 187.0 + (day - 7) * ((430.0 - 187.0) / 7.0)
            day == 14 -> 430.0
            day in 15..20 -> 430.0 + (day - 14) * ((1000.0 - 1000.0 * 0.0 - 430.0) / 7.0)
            day == 21 -> 1000.0
            day in 22..27 -> 1000.0 + (day - 21) * ((1550.0 - 1000.0) / 7.0)
            day == 28 -> 1550.0
            day in 29..34 -> 1550.0 + (day - 28) * ((2050.0 - 1550.0) / 7.0)
            day == 35 -> 2050.0
            day in 36..41 -> 2050.0 + (day - 35) * ((2570.0 - 2050.0) / 7.0)
            day == 42 -> 2570.0
            day in 43..44 -> 2570.0 + (day - 42) * ((2800.0 - 2570.0) / 3.0)
            day == 45 -> 2800.0
            else -> 2800.0 + (day - 45) * 80.0
        }
    }

    fun getRoss308StandardWeightKg(day: Int): Double {
        return getRealisticFieldWeightGrams(day) / 1000.0
    }

    /**
     * Resolves the latest actual measured average bird weight (kg) from daily reports or weighing sales invoices,
     * falling back to null when no actual measurement has been recorded yet.
     */
    fun resolveActualAvgBirdWeightKg(
        reports: List<DailyReportModel>,
        cycleTransactions: List<TransactionModel> = emptyList(),
        cycleId: String = ""
    ): Double? {
        val latestReportMeasuredKg = reports
            .sortedByDescending { it.dayNumber }
            .firstOrNull { it.actualAvgWeightKg > 0.0 }
            ?.actualAvgWeightKg
        if (latestReportMeasuredKg != null && latestReportMeasuredKg > 0.0) {
            return latestReportMeasuredKg
        }
        val latestInvoiceAvgKg = cycleTransactions
            .lastOrNull {
                (cycleId.isBlank() || it.cycleId == cycleId || it.cycleId.isBlank()) &&
                    it.type == TransactionType.INCOME &&
                    (it.invoiceData?.avgBirdWeightKg ?: 0.0) > 0.0
            }?.invoiceData?.avgBirdWeightKg
        return latestInvoiceAvgKg?.takeIf { it > 0.0 }
    }

    fun calculateLiveBirds(
        totalBirdsPlaced: Int,
        totalMortality: Int,
        cumulativeBirdsSold: Int
    ): Int {
        return (totalBirdsPlaced - totalMortality - cumulativeBirdsSold).coerceAtLeast(0)
    }

    fun resolveCumulativeBirdsSold(
        cycle: CycleModel,
        cycleTransactions: List<TransactionModel>
    ): Int {
        val txSold = cycleTransactions
            .filter { (it.cycleId == cycle.id || it.cycleId.isBlank()) && it.type == TransactionType.INCOME && it.invoiceData != null }
            .sumOf { it.invoiceData?.totalBirdsSold ?: 0 }
        return maxOf(cycle.cumulativeBirdsSold, txSold).coerceAtLeast(0)
    }

    fun resolveCumulativeSoldWeightKg(
        cycle: CycleModel,
        cycleTransactions: List<TransactionModel>
    ): Double {
        val txSoldWeightKg = cycleTransactions
            .filter { (it.cycleId == cycle.id || it.cycleId.isBlank()) && it.type == TransactionType.INCOME && it.invoiceData != null }
            .sumOf { it.invoiceData?.totalNetKg ?: 0.0 }
        return maxOf(cycle.cumulativeSoldWeightKg, txSoldWeightKg).coerceAtLeast(0.0)
    }

    /**
     * Total Produced Biomass (kg) = (liveBirds * avgLiveWeightKg) + cumulativeSoldWeightKg
     */
    fun calculateTotalProducedBiomassKg(
        liveBirds: Int,
        avgLiveWeightKg: Double,
        cumulativeSoldWeightKg: Double = 0.0
    ): Double {
        val remainingLiveBiomassKg = liveBirds.coerceAtLeast(0) * avgLiveWeightKg.coerceAtLeast(0.0)
        return remainingLiveBiomassKg + cumulativeSoldWeightKg.coerceAtLeast(0.0)
    }

    /**
     * Calculates FCR = Total Feed Consumed (kg) / Total Produced Biomass (kg)
     * where Total Produced Biomass = (liveBirds * avgLiveWeightKg) + cumulativeSoldWeightKg.
     * Prevents FCR distortion during partial sales (thinning).
     */
    fun calculateFcr(
        totalFeedKg: Double,
        initialBirds: Int,
        totalMortality: Int,
        birdsSold: Int,
        ageInDays: Int,
        actualAvgWeightKg: Double? = null,
        cumulativeSoldWeightKg: Double = 0.0
    ): Double {
        val liveBirds = calculateLiveBirds(initialBirds, totalMortality, birdsSold)
        val avgWeight = actualAvgWeightKg?.takeIf { it > 0 } ?: getRoss308StandardWeightKg(ageInDays)
        val totalProducedBiomassKg = calculateTotalProducedBiomassKg(
            liveBirds = liveBirds,
            avgLiveWeightKg = avgWeight,
            cumulativeSoldWeightKg = cumulativeSoldWeightKg
        )
        return if (totalProducedBiomassKg > 0.001) totalFeedKg / totalProducedBiomassKg else 0.0
    }

    /**
     * Cost per Bird = totalExpenses / (liveBirds + cumulativeBirdsSold)
     */
    fun calculateCostPerBird(
        totalExpenses: Double,
        liveBirds: Int,
        cumulativeBirdsSold: Int
    ): Double {
        val totalProducedBirds = liveBirds.coerceAtLeast(0) + cumulativeBirdsSold.coerceAtLeast(0)
        return if (totalProducedBirds > 0) totalExpenses / totalProducedBirds else 0.0
    }

    /**
     * Cost per Kg = totalExpenses / totalProducedBiomassKg
     */
    fun calculateCostPerKg(
        totalExpenses: Double,
        totalProducedBiomassKg: Double
    ): Double {
        return if (totalProducedBiomassKg > 0.001) totalExpenses / totalProducedBiomassKg else 0.0
    }

    /**
     * European Production Efficiency Factor (EPEF):
     * EPEF = (Livability % * Average Live Weight kg) / (Age in days * FCR) * 100
     * Dynamically avoids division-by-zero risks based on daily active days and FCR.
     */
    fun calculateEpef(
        livabilityPct: Double,
        avgLiveWeightKg: Double,
        ageInDays: Int,
        fcr: Double
    ): Double {
        if (ageInDays <= 0 || fcr <= 0.0001 || avgLiveWeightKg <= 0.0) return 0.0
        val clampedLivability = livabilityPct.coerceIn(0.0, 100.0)
        return (clampedLivability * avgLiveWeightKg) / (ageInDays * fcr) * 100.0
    }

    fun formatDateDdMmYyyy(dateStr: String): String {
        return try {
            val parsed = java.time.LocalDate.parse(dateStr.trim())
            parsed.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy", java.util.Locale.US))
        } catch (_: Exception) {
            dateStr
        }
    }

    fun calculateCycleDay(startDateStr: String, maxReportDay: Int = 1): Int {
        return try {
            val start = java.time.LocalDate.parse(startDateStr.trim())
            val today = java.time.LocalDate.now()
            val days = java.time.temporal.ChronoUnit.DAYS.between(start, today).toInt() + 1
            if (days in 1..60) {
                maxOf(days, maxReportDay)
            } else {
                maxReportDay.coerceAtLeast(1)
            }
        } catch (_: Exception) {
            maxReportDay.coerceAtLeast(1)
        }
    }
}
