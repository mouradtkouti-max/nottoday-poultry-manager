package com.example.models

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// ============================================================================
// THEME OPTIONS, PAPER STYLES & COLOR CONSTANTS
// ============================================================================

enum class AppPaperStyleOption(
    val id: String,
    val emoji: String,
    val defaultLabel: String
) {
    DARWIN_PARCHMENT("darwin_parchment", "📜", "Darwin Parchment"),
    RULED_NOTEBOOK("ruled_notebook", "📓", "Ruled Notebook"),
    ENGINEERING_GRID("engineering_grid", "📐", "Engineering Grid"),
    FROSTED_GLASS("frosted_glass", "📄", "Translucent Paper"),
    DEFAULT_PURE("default_pure", "💎", "Default Pure");

    companion object {
        val RULED_LEDGER: AppPaperStyleOption get() = RULED_NOTEBOOK
        val BLUEPRINT_GRID: AppPaperStyleOption get() = ENGINEERING_GRID
        val CLEAN_DEFAULT: AppPaperStyleOption get() = DEFAULT_PURE

        fun fromStoredValue(raw: String?): AppPaperStyleOption {
            return when (raw?.trim()) {
                "DARWIN_PARCHMENT", "darwin_parchment" -> DARWIN_PARCHMENT
                "RULED_NOTEBOOK", "ruled_notebook", "RULED_LEDGER", "ruled_ledger" -> RULED_NOTEBOOK
                "ENGINEERING_GRID", "engineering_grid", "BLUEPRINT_GRID", "blueprint_grid" -> ENGINEERING_GRID
                "FROSTED_GLASS", "frosted_glass", "TRANSLUCENT_PAPER", "translucent_paper" -> FROSTED_GLASS
                "DEFAULT_PURE", "default_pure", "CLEAN_DEFAULT", "clean_default" -> DEFAULT_PURE
                else -> DARWIN_PARCHMENT
            }
        }
    }
}

@Stable
object AppPaperStyleRuntime {
    var activePaperStyle by mutableStateOf(AppPaperStyleOption.DARWIN_PARCHMENT)
}

enum class AppThemeOption(
    val baseBgColor: Color,
    val baseCardColor: Color,
    val baseHeadingColor: Color,
    val isDark: Boolean
) {
    ICE_BLUE(Color(0xFFE8F4FD), Color.White, Color(0xFF1E293B), false),
    NATURE_GREEN(Color(0xFFE8F5E9), Color.White, Color(0xFF1E293B), false),
    CRISP_WHITE(Color(0xFFF8FAFC), Color.White, Color(0xFF1E293B), false),
    NIGHT_MODE(Color(0xFF0E121A), Color(0xFF161B26), Color(0xFFFFFFFF), true);

    val bgColor: Color
        get() = when (AppPaperStyleRuntime.activePaperStyle) {
            AppPaperStyleOption.DARWIN_PARCHMENT ->
                if (isDark) Color(0xFF18120E) else Color(0xFFF4EBD6)
            AppPaperStyleOption.RULED_NOTEBOOK ->
                if (isDark) Color(0xFF1A1814) else Color(0xFFF6F0DD)
            AppPaperStyleOption.ENGINEERING_GRID ->
                if (isDark) Color(0xFF0F171E) else Color(0xFFEBF3F9)
            AppPaperStyleOption.FROSTED_GLASS ->
                if (isDark) Color(0xFF0E1116) else Color(0xFFE0F2FE)
            AppPaperStyleOption.DEFAULT_PURE -> baseBgColor
        }

    val cardColor: Color
        get() = when (AppPaperStyleRuntime.activePaperStyle) {
            AppPaperStyleOption.DARWIN_PARCHMENT ->
                if (isDark) Color(0xFF2A2018) else Color(0xFFFAF3E4)
            AppPaperStyleOption.RULED_NOTEBOOK ->
                if (isDark) Color(0xFF24211B) else Color(0xFFFFFDF4)
            AppPaperStyleOption.ENGINEERING_GRID ->
                if (isDark) Color(0xFF17222D) else Color(0xFFF4F9FD)
            AppPaperStyleOption.FROSTED_GLASS ->
                if (isDark) Color(0xFF1C212B).copy(alpha = 0.78f) else Color.White.copy(alpha = 0.58f)
            AppPaperStyleOption.DEFAULT_PURE -> baseCardColor
        }

    val dialogCardColor: Color
        get() = when (AppPaperStyleRuntime.activePaperStyle) {
            AppPaperStyleOption.DARWIN_PARCHMENT ->
                if (isDark) Color(0xFF2A2018) else Color(0xFFFAF3E4)
            AppPaperStyleOption.RULED_NOTEBOOK ->
                if (isDark) Color(0xFF24211B) else Color(0xFFFFFDF4)
            AppPaperStyleOption.ENGINEERING_GRID ->
                if (isDark) Color(0xFF17222D) else Color(0xFFFAFDFF)
            AppPaperStyleOption.FROSTED_GLASS ->
                if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
            AppPaperStyleOption.DEFAULT_PURE -> baseCardColor
        }

    val innerStatBoxColor: Color
        get() = when (AppPaperStyleRuntime.activePaperStyle) {
            AppPaperStyleOption.DARWIN_PARCHMENT ->
                if (isDark) Color(0xFF1E1712) else Color(0xFFF2E5C9)
            AppPaperStyleOption.RULED_NOTEBOOK ->
                if (isDark) Color(0xFF1C1915) else Color(0xFFF6F0DF)
            AppPaperStyleOption.ENGINEERING_GRID ->
                if (isDark) Color(0xFF111A22) else Color(0xFFE6F0F8)
            AppPaperStyleOption.FROSTED_GLASS ->
                if (isDark) Color(0xFF334155).copy(alpha = 0.52f) else Color.White.copy(alpha = 0.45f)
            AppPaperStyleOption.DEFAULT_PURE ->
                if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
        }

    val headingColor: Color
        get() = when (AppPaperStyleRuntime.activePaperStyle) {
            AppPaperStyleOption.DARWIN_PARCHMENT ->
                if (isDark) Color(0xFFF5ECD8) else Color(0xFF26160C)
            AppPaperStyleOption.RULED_NOTEBOOK ->
                if (isDark) Color(0xFFFDF8ED) else Color(0xFF1E293B)
            AppPaperStyleOption.ENGINEERING_GRID ->
                if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
            else -> if (isDark) Color(0xFFFFFFFF) else baseHeadingColor
        }

    val secondaryTextColor: Color
        get() = when (AppPaperStyleRuntime.activePaperStyle) {
            AppPaperStyleOption.DARWIN_PARCHMENT ->
                if (isDark) Color(0xFFD7C4A3) else Color(0xFF5C4028)
            AppPaperStyleOption.RULED_NOTEBOOK ->
                if (isDark) Color(0xFFD6CFC2) else Color(0xFF475569)
            AppPaperStyleOption.ENGINEERING_GRID ->
                if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
            else -> if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
        }
}

enum class AppAccentColorOption(
    val hexCode: String,
    val primaryColor: Color,
    val darkModeColor: Color,
    val softContainerColor: Color,
    val darkContainerColor: Color,
    val labelKey: String = "theme_color_forest_green"
) {
    CRYSTAL_WHITE_GLASS("#FFFFFF", Color(0xFF1E293B), Color(0xFFF8FAFC), Color(0xFFFFFFFF), Color(0xFF1E293B), "theme_color_crystal_white_glass"),
    SMOKED_OBSIDIAN("#090D16", Color(0xFF090D16), Color(0xFF94A3B8), Color(0xFFE2E8F0), Color(0xFF090D16), "theme_color_smoked_obsidian"),
    DARWIN_BRONZE("#784B1C", Color(0xFF784B1C), Color(0xFFD4AF37), Color(0xFFF3E5CE), Color(0xFF3A2616), "theme_color_darwin_bronze"),
    OAK_WOOD("#5D3A1A", Color(0xFF5D3A1A), Color(0xFFD7CCC8), Color(0xFFEFEBE9), Color(0xFF3E2723), "theme_color_oak_wood"),
    FOREST_GREEN("#1B5E20", Color(0xFF1B5E20), Color(0xFF34D399), Color(0xFFE8F5E9), Color(0xFF064E3B), "theme_color_forest_green"),
    DEEP_TEAL("#0F766E", Color(0xFF0F766E), Color(0xFF2DD4BF), Color(0xFFCCFBF1), Color(0xFF134E4A), "theme_color_deep_teal"),
    AMBER_GOLD("#D97706", Color(0xFFD97706), Color(0xFFFBBF24), Color(0xFFFEF3C7), Color(0xFF78350F), "theme_color_amber_gold"),
    ROSE("#E11D48", Color(0xFFE11D48), Color(0xFFFB7185), Color(0xFFFFE4E6), Color(0xFF881337), "theme_color_rose"),
    VIOLET("#7C3AED", Color(0xFF7C3AED), Color(0xFFA78BFA), Color(0xFFEDE9FE), Color(0xFF2E1065), "theme_color_violet"),
    OCEAN_BLUE("#2563EB", Color(0xFF2563EB), Color(0xFF60A5FA), Color(0xFFDBEAFE), Color(0xFF1E3A8A), "theme_color_ocean_blue"),
    MINT_EMERALD("#10B981", Color(0xFF10B981), Color(0xFF34D399), Color(0xFFD1FAE5), Color(0xFF064E3B), "theme_color_mint_emerald"),
    SADDLE_LEATHER("#8B4513", Color(0xFF8B4513), Color(0xFFDEB887), Color(0xFFF5E6D3), Color(0xFF4A250A), "theme_color_saddle_leather"),
    TERRACOTTA_COPPER("#C2410C", Color(0xFFC2410C), Color(0xFFFB923C), Color(0xFFFFEDD5), Color(0xFF7C2D12), "theme_color_terracotta_copper"),
    OLIVE_GROVE("#4D7C0F", Color(0xFF4D7C0F), Color(0xFFA3E635), Color(0xFFECFCCB), Color(0xFF1A2E05), "theme_color_olive_grove"),
    DEEP_NAVY("#1E3A8A", Color(0xFF1E3A8A), Color(0xFF60A5FA), Color(0xFFDBEAFE), Color(0xFF172554), "theme_color_deep_navy"),
    ICE_SKY("#0284C7", Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFFE0F2FE), Color(0xFF0C4A6E), "theme_color_ice_sky"),
    STEEL_SLATE("#475569", Color(0xFF475569), Color(0xFF94A3B8), Color(0xFFE2E8F0), Color(0xFF1E293B), "theme_color_steel_slate");

    val swatchDisplayColor: Color
        get() = if (this == CRYSTAL_WHITE_GLASS) Color(0xFFFFFFFF) else primaryColor

    companion object {
        val curatedThemeColors: List<AppAccentColorOption> = listOf(
            CRYSTAL_WHITE_GLASS,
            SMOKED_OBSIDIAN,
            DARWIN_BRONZE,
            OAK_WOOD,
            FOREST_GREEN,
            DEEP_TEAL,
            AMBER_GOLD,
            ROSE,
            VIOLET,
            OCEAN_BLUE,
            DEEP_NAVY,
            ICE_SKY,
            OLIVE_GROVE,
            STEEL_SLATE
        )

        val curatedEightColors: List<AppAccentColorOption>
            get() = curatedThemeColors
    }
}

enum class AppTabBarMotionStyle(
    val badgeNumber: String,
    val labelKey: String,
    val defaultLabel: String
) {
    SLIDING_PILL("01", "tab_motion_sliding_pill", "01 — الكبسولة المنزلقة (Sliding Pill)"),
    LIQUID_BULGE("02", "tab_motion_liquid_bulge", "02 — التموج السائل (Liquid Bulge)"),
    FLOATING_NOTCH("03", "tab_motion_floating_notch", "03 — الفقاعة البارزة (Floating Notch)"),
    ICON_FILL("04", "tab_motion_icon_fill", "04 — التعبئة المضيئة (Icon Fill)"),
    EXPANDING_LABEL("05", "tab_motion_expanding_label", "05 — الكبسولة المتمددة (Expanding Label)"),
    BUBBLE_FLOAT("06", "tab_motion_bubble_float", "06 — الفقاعات الطافية (Bubble Float)");

    companion object {
        fun fromStoredValue(raw: String?): AppTabBarMotionStyle {
            return entries.firstOrNull { it.name == raw } ?: EXPANDING_LABEL
        }
    }
}

enum class AppIconStyleOption(
    val badgeNumber: String,
    val labelKey: String,
    val defaultLabel: String
) {
    TACTILE_3D("01", "icon_style_tactile_3d", "01 — أيقونات ثلاثية الأبعاد بارزة (3D Tactile)"),
    CLASSIC_INK("02", "icon_style_classic_ink", "02 — أيقونات حبرية كلاسيكية (Classic Ink)"),
    FLAT_MINIMAL("03", "icon_style_flat_minimal", "03 — أيقونات مسطحة بسيطة (Flat Minimal)");

    companion object {
        fun fromStoredValue(raw: String?): AppIconStyleOption {
            return entries.firstOrNull { it.name == raw } ?: TACTILE_3D
        }
    }
}

@Stable
object AppIconStyleRuntime {
    var activeIconStyle by mutableStateOf(AppIconStyleOption.TACTILE_3D)
}

enum class AppTextScaleOption(
    val scaleFactor: Float,
    val defaultLabel: String,
    val previewSp: Int
) {
    SMALL(0.9f, "Small", 12),
    DEFAULT(1.0f, "Default", 14),
    LARGE(1.12f, "Large", 16),
    EXTRA_LARGE(1.25f, "Extra Large", 19)
}

enum class AppCornerStyleOption(
    val cornerRadiusDp: Int,
    val defaultLabel: String
) {
    ROUNDED(20, "Rounded"),
    SOFT(12, "Soft"),
    CRISP(4, "Crisp")
}

val TextForestGreen = Color(0xFF1B5E20)
val DarkTextColor = Color(0xDD000000)
val LightGreenContainer = Color(0xFFE8F5E9)
val ActionRed = Color(0xFFC62828)

// ============================================================================
// CORE AGRITECH DATA MODELS
// ============================================================================

enum class AppRole {
    OWNER,
    OWNER_BARN,
    VETERINARIAN,
    VET_CLINIC,
    WORKER,
    FEED_SUPPLIER,
    CHICK_HATCHERY,
    WHOLESALE_TRADER,
    VET_IMPORTER,
    HOME_PERSONAL;

    val isOwnerRole: Boolean
        get() = this == OWNER || this == OWNER_BARN

    val isVetRole: Boolean
        get() = this == VETERINARIAN || this == VET_CLINIC

    val isHomePersonalRole: Boolean
        get() = this == HOME_PERSONAL

    val normalizedHubRole: AppRole
        get() = when (this) {
            OWNER -> OWNER_BARN
            VETERINARIAN -> VET_CLINIC
            HOME_PERSONAL -> WORKER
            else -> this
        }

    val isCurrentlyAvailable: Boolean
        get() = when (this) {
            OWNER,
            OWNER_BARN,
            VETERINARIAN,
            VET_CLINIC,
            WORKER,
            HOME_PERSONAL -> true
            FEED_SUPPLIER,
            CHICK_HATCHERY,
            WHOLESALE_TRADER,
            VET_IMPORTER -> false
        }

    companion object {
        val professionalHubRoles: List<AppRole> = listOf(
            OWNER_BARN,
            VET_CLINIC,
            WORKER,
            FEED_SUPPLIER,
            CHICK_HATCHERY,
            WHOLESALE_TRADER,
            VET_IMPORTER
        )

        val availableManagementRoles: List<AppRole> = listOf(
            OWNER_BARN,
            VET_CLINIC,
            WORKER
        )

        val unavailableManagementRoles: List<AppRole> = listOf(
            FEED_SUPPLIER,
            CHICK_HATCHERY,
            WHOLESALE_TRADER,
            VET_IMPORTER
        )
    }
}

data class VetPharmacyItemModel(
    val id: String,
    val name: String,
    val category: String,
    val stockUnits: Int,
    val unitLabel: String,
    val importerName: String,
    val importerPhone: String,
    val unitCostDzd: Double
)

data class VetEpidemicAlertModel(
    val id: String,
    val virusName: String,
    val regionName: String,
    val riskLevel: String,
    val recommendedVaccine: String,
    val scheduleDayText: String,
    val clinicalGuidance: String
)

data class RoleBusinessOrderModel(
    val id: String,
    val role: AppRole,
    val clientName: String,
    val clientPhone: String,
    val itemSummary: String,
    val quantityText: String,
    val totalAmountDzd: Double,
    val status: String = "PENDING"
)

data class FarmModel(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val capacity: Int = 0,
    val assignedWorkerId: String = "",
    val members: Map<String, String> = emptyMap(),
    val currencyCode: String = "DZD",
    val schemaVersion: Int = 2,
    val sectorId: String = "AGRICULTURE",
    val branchId: String = "BROILER",
    val customMetrics: Map<String, String> = emptyMap()
)

typealias FutureProofUnitModel = FarmModel

data class CycleModel(
    val id: String = "",
    val farmId: String = "",
    val totalBirdsPlaced: Int = 0,
    val targetWeight: Double = 2.3,
    val status: String = "active",
    val startDate: String = "2026-03-01",
    val chickPrice: Double = 120.0,
    val feedInventoryKg: Double = 0.0,
    val feedDeficitKg: Double = 0.0,
    val cumulativeFeedConsumedKg: Double = 0.0,
    val lastDailyReportId: String = "",
    val cumulativeBirdsSold: Int = 0,
    val cumulativeSoldWeightKg: Double = 0.0,
    val cumulativeMortality: Int = 0,
    val currencyCode: String = "DZD",
    val schemaVersion: Int = 2,
    val sectorId: String = "AGRICULTURE",
    val branchId: String = "BROILER",
    val customMetrics: Map<String, String> = emptyMap(),
    val updatedAtMillis: Long = 0L
)

typealias ProductionCycle = CycleModel

data class DailyReportModel(
    val id: String,
    val farmId: String,
    val cycleId: String,
    val dayNumber: Int,
    val feedIntakeKg: Double,
    val waterIntakeLiters: Double,
    val mortalityCount: Int,
    val temperature: Double? = null,
    val gasUnits: Double = 0.0,
    val ventilationStatus: String = "جيدة",
    val generalStatus: String = "طبيعية",
    val isLocked: Boolean = true,
    val isEdited: Boolean = false,
    val previousSummary: String = "",
    val actualAvgWeightKg: Double = 0.0,
    val medicineGiven: Boolean = false,
    val medicineSummary: String = "",
    val withdrawalDays: Int = 0,
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val litterCondition: String = ""
) {
    fun hasActualValueChangesFrom(existingReport: DailyReportModel): Boolean {
        val oldWeight = existingReport.actualAvgWeightKg.takeIf { it > 0.0 }
        val newWeight = this.actualAvgWeightKg.takeIf { it > 0.0 }
        val weightChanged = if (oldWeight == null && newWeight == null) {
            false
        } else if (oldWeight == null || newWeight == null) {
            true
        } else {
            kotlin.math.abs(newWeight - oldWeight) > 0.0001
        }
        val oldTemp = existingReport.temperature
        val newTemp = this.temperature
        val tempChanged = if (oldTemp == null && newTemp == null) {
            false
        } else if (oldTemp == null || newTemp == null) {
            true
        } else {
            kotlin.math.abs(newTemp - oldTemp) > 0.01
        }
        return kotlin.math.abs(this.feedIntakeKg - existingReport.feedIntakeKg) > 0.001 ||
            kotlin.math.abs(this.waterIntakeLiters - existingReport.waterIntakeLiters) > 0.001 ||
            this.mortalityCount != existingReport.mortalityCount ||
            tempChanged ||
            kotlin.math.abs(this.gasUnits - existingReport.gasUnits) > 0.01 ||
            this.ventilationStatus.trim() != existingReport.ventilationStatus.trim() ||
            this.litterCondition.trim() != existingReport.litterCondition.trim() ||
            this.medicineGiven != existingReport.medicineGiven ||
            this.medicineSummary.trim() != existingReport.medicineSummary.trim() ||
            weightChanged
    }
}

data class VetNoteModel(
    val id: String,
    val farmId: String,
    val cycleId: String,
    val dayNumber: Int,
    val medicineName: String,
    val dosage: String,
    val note: String,
    val isUrgent: Boolean,
    val status: String = "APPROVED",
    val linkedTransactionId: String = "",
    val invoiceAmount: Double = 0.0,
    val currency: String = "دج",
    val withdrawalDays: Int = 0,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

data class ChatMessageModel(
    val id: String,
    val cycleId: String,
    val senderId: String,
    val senderName: String,
    val senderRole: String,
    val text: String,
    val timestamp: String,
    val linkedTransactionId: String = "",
    val linkedVetNoteId: String = "",
    val isFinancialRequest: Boolean = false,
    val requestCategory: String = "",
    val requestAmount: Double = 0.0,
    val requestCurrency: String = "دج",
    val status: String = "PENDING",
    val requestStatus: String = status,
    val userId: String = senderId,
    val timestampText: String = timestamp,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val deliveryState: String = "LOCAL_ONLY"
)

data class ChatConversationModel(
    val id: String,
    val userName: String,
    val roleLabel: String,
    val avatarEmoji: String,
    val avatarBgHex: Long,
    val defaultSnippet: String,
    val defaultTimestamp: String,
    val unreadCount: Int = 0,
    val phoneNumber: String = "",
    val isPhoneContact: Boolean = true
)

data class MarketPostModel(
    val id: String,
    val title: String,
    val description: String,
    val priceDzd: Double,
    val priceUnit: String = "دج",
    val publisherName: String,
    val publisherLocation: String,
    val imageCaption: String = "صورة مرفقة للإعلان",
    val chatChannelId: String = "conv_feed_trader",
    val imageUri: String? = null,
    val publisherPhone: String = "",
    val isPhoneContact: Boolean = true,
    val publisherId: String = chatChannelId
)

typealias MarketPostItem = MarketPostModel

enum class TransactionType { EXPENSE, INCOME }

data class SalesInvoiceModel(
    val invoiceNumber: String,
    val farmName: String,
    val cycleId: String,
    val dateText: String,
    val birdAgeDays: Int,
    val buyerName: String,
    val truckPlate: String,
    val totalCrates: Int,
    val birdsPerCrate: Int,
    val totalBirdsSold: Int,
    val totalGrossKg: Double,
    val totalTareKg: Double,
    val totalNetKg: Double,
    val avgBirdWeightKg: Double,
    val pricePerKg: Double,
    val totalAmount: Double,
    val currency: String = "دج",
    val tareWeightsCount: Int = 0,
    val grossWeightsCount: Int = 0
)

object AppCurrency {
    const val EUR = "EUR"
    const val USD = "USD"
    const val DZD = "DZD"
}

data class TransactionModel(
    val id: String,
    val farmId: String = "",
    val cycleId: String,
    val type: TransactionType,
    val category: String,
    val amount: Double,
    val currency: String = "دج",
    val date: String,
    val invoiceData: SalesInvoiceModel? = null,
    val status: String = "APPROVED",
    val notes: String = "",
    val quantityQuintals: Double = 0.0,
    val approvedBySignature: String = "",
    val invoiceNumber: Int? = null,
    val updatedAtMillis: Long = 0L,
    val deliveryState: String = "SYNCED"
)

data class WeighingRecord(
    val id: String,
    val grossWeight: Double,
    val tareWeight: Double,
    val birdsCount: Int
) {
    val netWeight: Double get() = (grossWeight - tareWeight).coerceAtLeast(0.0)
}

enum class SalesFlowStep {
    NONE,
    STAGE_1_SHIPMENT_INFO,
    STAGE_2_WEIGHING_TABLE
}

enum class TareMode {
    DETAILED,    // الخيار (أ): تسجيل وزنات الصناديق الفارغة بالتفصيل (وزنة بوزنة)
    DIRECT_TOTAL // الخيار (ب): إدخال إجمالي وزن الصناديق الفارغة مباشرة كرقم واحد
}

enum class QuickWeighingPhase {
    TARE_PHASE,  // المرحلة 1: إدخال وزنات الصناديق الفارغة 📦
    GROSS_PHASE  // المرحلة 2: إدخال وزنات الصناديق الممتلئة بالدجاج 🐔
}

data class WeighEntry(
    val cratesCount: Int,
    val weightKg: Double
)

enum class GlobalDashboardTab {
    BARNS,
    CHAT,
    MARKET,
    AI_VET
}

/**
 * Data model for a parsed QR invitation payload (`AGRITECH_JOIN|farmId=...|role=...|inviteId=...`).
 */
data class QrFarmInviteData(
    val farmId: String,
    val role: AppRole,
    val rawPayload: String,
    val inviteId: String = ""
)

data class FarmInviteModel(
    val inviteId: String,
    val farmId: String,
    val role: String,
    val createdBy: String,
    val status: String = "ACTIVE",
    val expiresAtMillis: Long,
    val consumedBy: String = "",
    val consumedAtMillis: Long = 0L
)

/**
 * Data model for the AI Vet Chat interface (`OwnerGlobalAiVetPlaceholderScreen`).
 */
data class AiVetChatMessage(
    val id: String,
    val senderName: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: String
)

/**
 * Data model for the Worker Personal Ledger (`personal_worker_ledger`).
 */
data class WorkerPersonalEntryModel(
    val id: String,
    val farmId: String = "personal_worker_ledger",
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val dateLabel: String,
    val isBillOrObligation: Boolean = true
)
