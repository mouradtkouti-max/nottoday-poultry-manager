package com.example.utils

import android.content.Context
import com.example.models.CycleModel
import com.example.models.FarmModel
import com.example.models.TransactionModel
import com.example.models.TransactionType
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * ملخص مالي موحّد لأي فرع أو نشاط (زراعي، حيواني، تجاري، أو حرفي/حر).
 * يلتزم بتصنيف جميع التدفقات الخارجة تحت "المصاريف" (Expenses) دون استخدام مصطلح "ديون".
 */
data class BranchFinancialSummary(
    val sectorId: String = "AGRICULTURE",
    val branchId: String = "BROILER",
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netProfit: Double = 0.0,
    val profitMarginPct: Double = 0.0,
    val isProfitable: Boolean = false,
    val unitCost: Double = 0.0,
    val customKpis: Map<String, Double> = emptyMap()
)

/**
 * المحرك المركزي لدفتر الأعمال المتعدد القطاعات (BusinessNotebookEngine).
 * يعزل منطق الحسابات والتحقق من المدخلات لكل قطاع عن واجهة المستخدم (MainActivity).
 */
object BusinessNotebookEngine {

    const val SECTOR_AGRICULTURE = "AGRICULTURE"
    const val SECTOR_COMMERCIAL = "COMMERCIAL"
    const val SECTOR_FREELANCE = "FREELANCE"
    const val SECTOR_PERSONAL_HOME = "PERSONAL_HOME"

    const val BRANCH_BROILER = "BROILER"
    const val BRANCH_LAYER = "LAYER"
    const val BRANCH_TURKEY = "TURKEY"
    const val BRANCH_CATTLE_SHEEP = "CATTLE_SHEEP"
    const val BRANCH_AQUACULTURE = "AQUACULTURE"
    const val BRANCH_BEEKEEPING = "BEEKEEPING"
    const val BRANCH_GREENHOUSE = "GREENHOUSE"
    const val BRANCH_ORCHARD = "ORCHARD"
    const val BRANCH_FIELD_CROPS = "FIELD_CROPS"
    const val BRANCH_RETAIL_WHOLESALE = "RETAIL_WHOLESALE"
    const val BRANCH_WORKSHOP_SERVICE = "WORKSHOP_SERVICE"
    const val BRANCH_HOUSEHOLD_LEDGER = "HOUSEHOLD_LEDGER"

    /**
     * التحقق الصارم من المبالغ والمدخلات الرقمية لمنع القيم السالبة أو غير الصالحة.
     */
    fun validateNonNegativeAmount(value: Double): Boolean {
        return !value.isNaN() && !value.isInfinite() && value >= 0.0
    }

    fun validatePositiveAmount(value: Double): Boolean {
        return !value.isNaN() && !value.isInfinite() && value > 0.0
    }

    fun sanitizeNumericInput(raw: String, fallback: Double = 0.0): Double {
        val cleaned = raw.trim().replace(",", ".")
        val parsed = cleaned.toDoubleOrNull() ?: return fallback
        return if (validateNonNegativeAmount(parsed)) parsed else fallback
    }

    /**
     * حساب الملخص المالي الموحد لأي فرع انطلاقاً من قائمة المعاملات المالية.
     */
    fun summarizeBranchFinances(
        sectorId: String,
        branchId: String,
        transactions: List<TransactionModel>,
        producedUnitsCount: Double = 0.0,
        customKpis: Map<String, Double> = emptyMap()
    ): BranchFinancialSummary {
        val validIncome = transactions
            .filter { it.type == TransactionType.INCOME && validateNonNegativeAmount(it.amount) }
            .sumOf { it.amount }
        val validExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE && validateNonNegativeAmount(it.amount) }
            .sumOf { it.amount }

        val netProfit = validIncome - validExpenses
        val profitMarginPct = if (validIncome > 0.0) {
            (netProfit / validIncome) * 100.0
        } else {
            0.0
        }
        val safeUnits = if (validatePositiveAmount(producedUnitsCount)) producedUnitsCount else 0.0
        val unitCost = if (safeUnits > 0.0) validExpenses / safeUnits else 0.0

        return BranchFinancialSummary(
            sectorId = sectorId.ifBlank { SECTOR_AGRICULTURE },
            branchId = branchId.ifBlank { BRANCH_BROILER },
            totalIncome = validIncome,
            totalExpenses = validExpenses,
            netProfit = netProfit,
            profitMarginPct = profitMarginPct,
            isProfitable = netProfit > 0.0,
            unitCost = unitCost,
            customKpis = customKpis
        )
    }
}

/**
 * محرك حسابات الإنتاج الحيواني والداجني والسمكي والنحل (LivestockAndPoultryEngine).
 * يحافظ على معيار القنطار (1 قنطار = 100 كغ) ويتكامل مع PoultryCalculators.
 */
object LivestockAndPoultryEngine {

    fun quintalsToKg(quintals: Double): Double {
        if (!BusinessNotebookEngine.validateNonNegativeAmount(quintals)) return 0.0
        return quintals * 100.0
    }

    fun kgToQuintals(kg: Double): Double {
        if (!BusinessNotebookEngine.validateNonNegativeAmount(kg)) return 0.0
        return kg / 100.0
    }

    /**
     * حساب مؤشرات كفاءة القطيع أو الدواجن (نسبة النفوق، معامل التحويل FCR، مؤشر الكفاءة الأوروبي EPEF).
     */
    fun evaluateFlockOrHerdMetrics(
        initialHeads: Int,
        cumulativeMortality: Int,
        culledOrSoldHeads: Int = 0,
        totalFeedQuintals: Double,
        averageLiveWeightKg: Double,
        ageDays: Int
    ): Map<String, Double> {
        val safeInitial = max(0, initialHeads)
        val safeMortality = cumulativeMortality.coerceIn(0, safeInitial)
        val safeCulled = culledOrSoldHeads.coerceIn(0, safeInitial - safeMortality)
        val remainingLiveHeads = max(0, safeInitial - safeMortality - safeCulled)

        val mortalityPct = if (safeInitial > 0) {
            (safeMortality.toDouble() / safeInitial.toDouble()) * 100.0
        } else {
            0.0
        }
        val livabilityPct = (100.0 - mortalityPct).coerceIn(0.0, 100.0)

        val totalFeedKg = quintalsToKg(totalFeedQuintals)
        val safeAvgWeightKg = if (BusinessNotebookEngine.validatePositiveAmount(averageLiveWeightKg)) {
            averageLiveWeightKg
        } else {
            0.0
        }
        val totalLiveBiomassKg = remainingLiveHeads * safeAvgWeightKg

        val fcr = if (totalLiveBiomassKg > 0.0 && totalFeedKg > 0.0) {
            totalFeedKg / totalLiveBiomassKg
        } else {
            0.0
        }

        val safeAge = max(0, ageDays)
        val epef = if (safeAge > 0 && fcr > 0.0 && safeAvgWeightKg > 0.0) {
            ((livabilityPct * safeAvgWeightKg) / (safeAge.toDouble() * fcr)) * 100.0
        } else {
            0.0
        }

        return mapOf(
            "remainingLiveHeads" to remainingLiveHeads.toDouble(),
            "mortalityPct" to mortalityPct,
            "livabilityPct" to livabilityPct,
            "totalFeedQuintals" to max(0.0, totalFeedQuintals),
            "totalFeedKg" to totalFeedKg,
            "totalLiveBiomassKg" to totalLiveBiomassKg,
            "fcr" to fcr,
            "epef" to epef
        )
    }

    /**
     * حساب إنتاج البيض أو الحليب أو العسل مع التكلفة لكل وحدة.
     */
    fun calculateDailyYieldMetrics(
        producingUnits: Int,
        dailyOutputQuantity: Double,
        dailyFeedAndCareExpense: Double
    ): Map<String, Double> {
        val safeUnits = max(0, producingUnits)
        val safeOutput = if (BusinessNotebookEngine.validateNonNegativeAmount(dailyOutputQuantity)) dailyOutputQuantity else 0.0
        val safeExpense = if (BusinessNotebookEngine.validateNonNegativeAmount(dailyFeedAndCareExpense)) dailyFeedAndCareExpense else 0.0

        val yieldPerUnit = if (safeUnits > 0) safeOutput / safeUnits.toDouble() else 0.0
        val productionRatePct = if (safeUnits > 0) ((safeOutput / safeUnits.toDouble()) * 100.0).coerceAtMost(100.0) else 0.0
        val costPerOutputUnit = if (safeOutput > 0.0) safeExpense / safeOutput else 0.0

        return mapOf(
            "yieldPerUnit" to yieldPerUnit,
            "productionRatePct" to productionRatePct,
            "costPerOutputUnit" to costPerOutputUnit
        )
    }
}

/**
 * محرك حسابات الإنتاج النباتي، البيوت البلاستيكية، والأشجار المثمرة (PlantAndOrchardEngine).
 */
object PlantAndOrchardEngine {

    /**
     * تقييم إنتاجية المحصول أو البستان أو البيت البلاستيكي والعائد الصافي للهكتار أو الشجرة.
     */
    fun evaluateCropOrOrchardSeason(
        areaHectaresOrTreesCount: Double,
        harvestedQuintals: Double,
        sellingPricePerQuintal: Double,
        seedAndSaplingExpense: Double,
        fertilizerAndPesticideExpense: Double,
        irrigationAndLaborExpense: Double
    ): BranchFinancialSummary {
        val safeAreaOrTrees = if (BusinessNotebookEngine.validatePositiveAmount(areaHectaresOrTreesCount)) areaHectaresOrTreesCount else 0.0
        val safeHarvestQuintals = if (BusinessNotebookEngine.validateNonNegativeAmount(harvestedQuintals)) harvestedQuintals else 0.0
        val safePricePerQuintal = if (BusinessNotebookEngine.validateNonNegativeAmount(sellingPricePerQuintal)) sellingPricePerQuintal else 0.0

        val safeSeeds = if (BusinessNotebookEngine.validateNonNegativeAmount(seedAndSaplingExpense)) seedAndSaplingExpense else 0.0
        val safeFertilizer = if (BusinessNotebookEngine.validateNonNegativeAmount(fertilizerAndPesticideExpense)) fertilizerAndPesticideExpense else 0.0
        val safeIrrigation = if (BusinessNotebookEngine.validateNonNegativeAmount(irrigationAndLaborExpense)) irrigationAndLaborExpense else 0.0

        val totalIncome = safeHarvestQuintals * safePricePerQuintal
        val totalExpenses = safeSeeds + safeFertilizer + safeIrrigation
        val netProfit = totalIncome - totalExpenses

        val yieldQuintalsPerAreaUnit = if (safeAreaOrTrees > 0.0) safeHarvestQuintals / safeAreaOrTrees else 0.0
        val expensePerAreaUnit = if (safeAreaOrTrees > 0.0) totalExpenses / safeAreaOrTrees else 0.0
        val costPerHarvestedQuintal = if (safeHarvestQuintals > 0.0) totalExpenses / safeHarvestQuintals else 0.0
        val profitMarginPct = if (totalIncome > 0.0) (netProfit / totalIncome) * 100.0 else 0.0

        return BranchFinancialSummary(
            sectorId = BusinessNotebookEngine.SECTOR_AGRICULTURE,
            branchId = BusinessNotebookEngine.BRANCH_ORCHARD,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netProfit = netProfit,
            profitMarginPct = profitMarginPct,
            isProfitable = netProfit > 0.0,
            unitCost = costPerHarvestedQuintal,
            customKpis = mapOf(
                "harvestedQuintals" to safeHarvestQuintals,
                "harvestedKg" to (safeHarvestQuintals * 100.0),
                "yieldQuintalsPerAreaUnit" to yieldQuintalsPerAreaUnit,
                "expensePerAreaUnit" to expensePerAreaUnit,
                "costPerHarvestedQuintal" to costPerHarvestedQuintal
            )
        )
    }
}

/**
 * محرك حسابات الأنشطة التجارية، الورشات، والمهن الحرة (CommercialAndFreelanceEngine).
 * يصنف جميع التدفقات النقدية الخارجة تحت "المصاريف" (Expenses) دون استخدام لفظ "ديون".
 */
object CommercialAndFreelanceEngine {

    /**
     * تقييم ربحية النشاط التجاري أو الحرفي مع حساب هامش الربح الإجمالي والصافي والعائد على المصاريف (ROI).
     */
    fun evaluateCommercialOrFreelancePeriod(
        branchId: String = BusinessNotebookEngine.BRANCH_RETAIL_WHOLESALE,
        grossSalesAndServiceIncome: Double,
        merchandiseOrMaterialExpense: Double,
        operatingAndOverheadExpense: Double,
        completedOrdersOrProjectsCount: Int = 0
    ): BranchFinancialSummary {
        val safeIncome = if (BusinessNotebookEngine.validateNonNegativeAmount(grossSalesAndServiceIncome)) grossSalesAndServiceIncome else 0.0
        val safeMaterials = if (BusinessNotebookEngine.validateNonNegativeAmount(merchandiseOrMaterialExpense)) merchandiseOrMaterialExpense else 0.0
        val safeOperating = if (BusinessNotebookEngine.validateNonNegativeAmount(operatingAndOverheadExpense)) operatingAndOverheadExpense else 0.0
        val safeOrders = max(0, completedOrdersOrProjectsCount)

        val totalExpenses = safeMaterials + safeOperating
        val grossProfit = safeIncome - safeMaterials
        val netProfit = safeIncome - totalExpenses

        val profitMarginPct = if (safeIncome > 0.0) (netProfit / safeIncome) * 100.0 else 0.0
        val roiPct = if (totalExpenses > 0.0) (netProfit / totalExpenses) * 100.0 else 0.0
        val expensePerOrder = if (safeOrders > 0) totalExpenses / safeOrders.toDouble() else 0.0
        val averageOrderRevenue = if (safeOrders > 0) safeIncome / safeOrders.toDouble() else 0.0

        return BranchFinancialSummary(
            sectorId = BusinessNotebookEngine.SECTOR_COMMERCIAL,
            branchId = branchId,
            totalIncome = safeIncome,
            totalExpenses = totalExpenses,
            netProfit = netProfit,
            profitMarginPct = profitMarginPct,
            isProfitable = netProfit > 0.0,
            unitCost = expensePerOrder,
            customKpis = mapOf(
                "grossProfit" to grossProfit,
                "roiPct" to roiPct,
                "averageOrderRevenue" to averageOrderRevenue,
                "completedOrdersCount" to safeOrders.toDouble()
            )
        )
    }
}

/**
 * محرك إدارة المنزل والحساب الشخصي للعامل والمشرف والمستخدم الفردي (PersonalAndHouseholdEngine).
 * يتتبع الفواتير والمصاريف الشهرية المتكررة (كهرباء وغاز، إنترنت وجوال، تأمين وصيانة سيارة، مقاضي البيت)
 * مقابل الراتب والدخل الشهري، ويحسب نسبة التغير مقارنة بالشهر السابق.
 */
object PersonalAndHouseholdEngine {

    val standardExpenseCategoryKeys: List<String> = listOf(
        "home_cat_electricity_gas",
        "home_cat_internet_phone",
        "home_cat_car_insurance",
        "home_cat_groceries"
    )

    const val salaryIncomeCategoryKey: String = "home_cat_salary_income"

    fun evaluateMonthlyHouseholdBudget(
        monthlySalaryAndIncome: Double,
        electricityAndGasExpense: Double,
        internetAndPhoneExpense: Double,
        carInsuranceAndMaintenanceExpense: Double,
        groceriesAndHomeExpense: Double,
        previousMonthTotalExpense: Double = 0.0
    ): BranchFinancialSummary {
        val safeIncome = if (BusinessNotebookEngine.validateNonNegativeAmount(monthlySalaryAndIncome)) monthlySalaryAndIncome else 0.0
        val safeElecGas = if (BusinessNotebookEngine.validateNonNegativeAmount(electricityAndGasExpense)) electricityAndGasExpense else 0.0
        val safeInternet = if (BusinessNotebookEngine.validateNonNegativeAmount(internetAndPhoneExpense)) internetAndPhoneExpense else 0.0
        val safeCar = if (BusinessNotebookEngine.validateNonNegativeAmount(carInsuranceAndMaintenanceExpense)) carInsuranceAndMaintenanceExpense else 0.0
        val safeGroceries = if (BusinessNotebookEngine.validateNonNegativeAmount(groceriesAndHomeExpense)) groceriesAndHomeExpense else 0.0
        val safePrevExpense = if (BusinessNotebookEngine.validateNonNegativeAmount(previousMonthTotalExpense)) previousMonthTotalExpense else 0.0

        val totalExpenses = safeElecGas + safeInternet + safeCar + safeGroceries
        val netSavings = safeIncome - totalExpenses
        val savingsRatePct = if (safeIncome > 0.0) (netSavings / safeIncome) * 100.0 else 0.0
        val monthOverMonthDelta = totalExpenses - safePrevExpense
        val monthOverMonthPct = if (safePrevExpense > 0.0) (monthOverMonthDelta / safePrevExpense) * 100.0 else 0.0

        return BranchFinancialSummary(
            sectorId = BusinessNotebookEngine.SECTOR_PERSONAL_HOME,
            branchId = BusinessNotebookEngine.BRANCH_HOUSEHOLD_LEDGER,
            totalIncome = safeIncome,
            totalExpenses = totalExpenses,
            netProfit = netSavings,
            profitMarginPct = savingsRatePct,
            isProfitable = netSavings >= 0.0,
            unitCost = totalExpenses / 30.0,
            customKpis = mapOf(
                "electricityAndGasExpense" to safeElecGas,
                "internetAndPhoneExpense" to safeInternet,
                "carInsuranceAndMaintenanceExpense" to safeCar,
                "groceriesAndHomeExpense" to safeGroceries,
                "monthOverMonthDelta" to monthOverMonthDelta,
                "monthOverMonthPct" to monthOverMonthPct
            )
        )
    }
}

/**
 * مخزن تفضيلات القطاع والفرع للمستخدم (UserSectorPreferenceStore).
 * يحفظ القطاع والفرع المختار محلياً ويرقي بيانات الوحدات القديمة بأمان إلى الإصدار 2 دون كسر التوافقية.
 */
object UserSectorPreferenceStore {
    private const val PREFS_NAME = "agritech_sector_preferences"
    private const val KEY_SELECTED_SECTOR = "selected_sector_id"
    private const val KEY_SELECTED_BRANCH = "selected_branch_id"

    fun saveSelectedSectorAndBranch(
        context: Context,
        sectorId: String,
        branchId: String
    ) {
        val safeSector = sectorId.trim().ifEmpty { BusinessNotebookEngine.SECTOR_AGRICULTURE }
        val safeBranch = branchId.trim().ifEmpty { BusinessNotebookEngine.BRANCH_BROILER }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SELECTED_SECTOR, safeSector)
            .putString(KEY_SELECTED_BRANCH, safeBranch)
            .apply()
    }

    fun getSelectedSectorId(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SELECTED_SECTOR, BusinessNotebookEngine.SECTOR_AGRICULTURE)
            ?.ifBlank { BusinessNotebookEngine.SECTOR_AGRICULTURE }
            ?: BusinessNotebookEngine.SECTOR_AGRICULTURE
    }

    fun getSelectedBranchId(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SELECTED_BRANCH, BusinessNotebookEngine.BRANCH_BROILER)
            ?.ifBlank { BusinessNotebookEngine.BRANCH_BROILER }
            ?: BusinessNotebookEngine.BRANCH_BROILER
    }

    /**
     * ترقية آمنة لنموذج المزرعة/الوحدة من الإصدارات السابقة إلى schemaVersion = 2 مع الحفاظ على جميع البيانات.
     */
    fun upgradeFarmToLatestSchema(farm: FarmModel): FarmModel {
        return farm.copy(
            schemaVersion = max(2, farm.schemaVersion),
            sectorId = farm.sectorId.ifBlank { BusinessNotebookEngine.SECTOR_AGRICULTURE },
            branchId = farm.branchId.ifBlank { BusinessNotebookEngine.BRANCH_BROILER }
        )
    }

    /**
     * ترقية آمنة لنموذج الدورة من الإصدارات السابقة إلى schemaVersion = 2 مع الحفاظ على جميع البيانات.
     */
    fun upgradeCycleToLatestSchema(cycle: CycleModel): CycleModel {
        return cycle.copy(
            schemaVersion = max(2, cycle.schemaVersion),
            sectorId = cycle.sectorId.ifBlank { BusinessNotebookEngine.SECTOR_AGRICULTURE },
            branchId = cycle.branchId.ifBlank { BusinessNotebookEngine.BRANCH_BROILER }
        )
    }
}
