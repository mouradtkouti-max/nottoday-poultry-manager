package com.example.utils

import com.example.models.CycleModel
import com.example.models.DailyReportModel
import com.example.models.TransactionModel
import com.example.models.TransactionType
import com.example.models.VetNoteModel

// ============================================================================
// CALCULATION LOGIC (Ross 308 Standards & Partial Sales / Thinning Strictness)
// ============================================================================

object PoultryCalculators {
    const val INITIAL_BASELINE_FEED_KG = 8000.0 // 80 Quintals baseline feed per barn cycle

    fun isFeedTransaction(tx: TransactionModel): Boolean {
        if (tx.type != TransactionType.EXPENSE) return false
        return tx.category.contains("علف") ||
            tx.category.contains("أعلاف") ||
            tx.category.equals("feed", ignoreCase = true) ||
            tx.category.contains("Aliment", ignoreCase = true) ||
            tx.category.contains("Feed", ignoreCase = true) ||
            tx.category.contains("Pienso", ignoreCase = true) ||
            tx.category.contains("Ração", ignoreCase = true) ||
            tx.category.contains("चारा") ||
            tx.notes.contains("علف") ||
            tx.notes.contains("قنطار")
    }

    fun extractFeedKgFromTransaction(tx: TransactionModel): Double {
        if (!isFeedTransaction(tx)) return 0.0
        val regexQ = Regex("""(\d+(?:\.\d+)?)\s*(?:قنطار|ق|Q|q|Quintal|Quintaux)""", RegexOption.IGNORE_CASE)
        val matchQ = regexQ.find(tx.notes) ?: regexQ.find(tx.category)
        if (matchQ != null) {
            val qVal = matchQ.groupValues[1].toDoubleOrNull()
            if (qVal != null && qVal > 0.0) return qVal * 100.0
        }
        val regexKg = Regex("""(\d+(?:\.\d+)?)\s*(?:كغ|كجم|kg)""", RegexOption.IGNORE_CASE)
        val matchKg = regexKg.find(tx.notes) ?: regexKg.find(tx.category)
        if (matchKg != null) {
            val kgVal = matchKg.groupValues[1].toDoubleOrNull()
            if (kgVal != null && kgVal > 0.0) return kgVal
        }
        return if (tx.amount >= 800.0) (tx.amount / 8000.0) * 100.0 else 0.0
    }

    /**
     * Checks if the barn has any active medication/antibiotic administered within the last 7 days
     * and returns the remaining withdrawal days (> 0 if active, 0 if safe).
     */
    fun calculateWithdrawalDaysRemaining(
        vetNotes: List<VetNoteModel>,
        currentCycleDay: Int,
        maxReportDay: Int = currentCycleDay
    ): Int {
        if (vetNotes.isEmpty()) return 0
        val refDay = maxOf(currentCycleDay, maxReportDay).coerceAtLeast(1)
        val recentMedNotes = vetNotes.filter { vn ->
            val text = "${vn.medicineName} ${vn.dosage} ${vn.note}"
            val hasMedication = vn.medicineName.isNotBlank() ||
                vn.invoiceAmount > 0.0 ||
                text.contains("مضاد") ||
                text.contains("دواء") ||
                text.contains("أوكسي") ||
                text.contains("إنروفلوكساسين") ||
                text.contains("تيلوزين") ||
                text.contains("كوليستين") ||
                text.contains("أموكسيسيلين") ||
                text.contains("سحب") ||
                text.contains("Antibiotic", ignoreCase = true) ||
                text.contains("Enro", ignoreCase = true) ||
                text.contains("Tylosin", ignoreCase = true)
            val daysSince = (refDay - vn.dayNumber).coerceAtLeast(0)
            hasMedication && daysSince < 7
        }
        if (recentMedNotes.isEmpty()) return 0
        val maxRemaining = recentMedNotes.maxOf { vn ->
            val daysSince = (refDay - vn.dayNumber).coerceAtLeast(0)
            (7 - daysSince).coerceIn(1, 7)
        }
        return maxRemaining
    }

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
     * Calibrated 40-day broiler live weight curve (kg) for an 8,000-bird Algerian commercial barn,
     * reaching ~2.92 kg at Day 40 (within the 2.85–2.95 kg market target).
     */
    fun getCalibratedBroilerWeightKg(day: Int): Double {
        val grams = when {
            day <= 0 -> 42.0
            day == 1 -> 49.0
            day in 2..6 -> 49.0 + (day - 1) * ((190.0 - 49.0) / 6.0)
            day == 7 -> 190.0
            day in 8..13 -> 190.0 + (day - 7) * ((480.0 - 190.0) / 7.0)
            day == 14 -> 480.0
            day in 15..20 -> 480.0 + (day - 14) * ((1050.0 - 480.0) / 7.0)
            day == 21 -> 1050.0
            day in 22..27 -> 1050.0 + (day - 21) * ((1700.0 - 1050.0) / 7.0)
            day == 28 -> 1700.0
            day in 29..34 -> 1700.0 + (day - 28) * ((2400.0 - 1700.0) / 7.0)
            day == 35 -> 2400.0
            day in 36..39 -> 2400.0 + (day - 35) * ((2920.0 - 2400.0) / 5.0)
            day == 40 -> 2920.0
            else -> 2920.0 + (day - 40) * 65.0
        }
        return kotlin.math.round(grams) / 1000.0
    }

    /**
     * Calibrated daily feed intake in Quintals (1 ق = 100 kg) for an 8,000-bird flock:
     * Day 1 = 1.8 ق, Day 10 = 5.5 ق, Day 20 = 9.5 ق, Day 30 = 13.5 ق, Days 35–40 = 16.5 ق,
     * summing to exactly 390.0 Quintals (39,000 kg, ~5.0–5.1 kg per surviving bird) over 40 days.
     */
    fun getCalibratedDailyFeedQuintals(day: Int): Double {
        return when (day) {
            1 -> 1.8
            2 -> 2.2
            3 -> 2.6
            4 -> 3.1
            5 -> 3.5
            6 -> 4.0
            7 -> 4.4
            8 -> 4.8
            9 -> 5.1
            10 -> 5.5
            in 11..20 -> kotlin.math.round((5.5 + (day - 10) * 0.4) * 10.0) / 10.0
            in 21..30 -> kotlin.math.round((9.5 + (day - 20) * 0.4) * 10.0) / 10.0
            in 31..34 -> kotlin.math.round((13.5 + (day - 30) * 0.6) * 10.0) / 10.0
            else -> 16.5
        }
    }

    /**
     * Calibrated daily water consumption in Liters for an 8,000-bird flock:
     * Starts at 1,200 L/day on Day 1, reaches 4,500 L/day on Day 20, and 7,500 L/day on Days 35–40,
     * summing to 190,000 Liters (~24.9 L/bird) across the 40-day cycle.
     */
    fun getCalibratedDailyWaterLiters(day: Int): Double {
        return when (day) {
            1 -> 1200.0
            2 -> 1400.0
            3 -> 1600.0
            4 -> 1800.0
            5 -> 2000.0
            6 -> 2200.0
            7 -> 2400.0
            8 -> 2550.0
            9 -> 2700.0
            10 -> 2850.0
            11 -> 3000.0
            12 -> 3200.0
            13 -> 3400.0
            14 -> 3600.0
            15 -> 3800.0
            16 -> 4000.0
            17 -> 4150.0
            18 -> 4300.0
            19 -> 4400.0
            20 -> 4500.0
            21 -> 4800.0
            22 -> 5000.0
            23 -> 5200.0
            24 -> 5400.0
            25 -> 5550.0
            26 -> 5800.0
            27 -> 6050.0
            28 -> 6300.0
            29 -> 6500.0
            30 -> 6700.0
            31 -> 6900.0
            32 -> 7100.0
            33 -> 7250.0
            34 -> 7400.0
            else -> 7500.0
        }
    }

    /**
     * Calibrated daily mortality for an 8,000-bird flock:
     * Averages 9.5 birds/day (10 on odd days, 9 on even days) to reach 380 total mortality (4.75%)
     * over 40 days, leaving 7,620 live birds (95.25% livability).
     */
    fun getCalibratedDailyMortality(day: Int): Int {
        return if (day % 2 == 1) 10 else 9
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
