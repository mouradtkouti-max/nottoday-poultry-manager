package com.example.models

import androidx.compose.ui.graphics.Color

// ============================================================================
// THEME OPTIONS & COLOR CONSTANTS
// ============================================================================

enum class AppThemeOption(
    val bgColor: Color,
    val cardColor: Color,
    val headingColor: Color,
    val isDark: Boolean
) {
    ICE_BLUE(Color(0xFFE8F4FD), Color.White, Color(0xFF1B5E20), false),
    NATURE_GREEN(Color(0xFFE8F5E9), Color.White, Color(0xFF1B5E20), false),
    CRISP_WHITE(Color(0xFFF8FAFC), Color.White, Color(0xFF1B5E20), false),
    NIGHT_MODE(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFFF8FAFC), true)
}

enum class AppAccentColorOption(
    val hexCode: String,
    val primaryColor: Color,
    val darkModeColor: Color,
    val softContainerColor: Color,
    val darkContainerColor: Color
) {
    FOREST_GREEN("#1B5E20", Color(0xFF1B5E20), Color(0xFF34D399), Color(0xFFE8F5E9), Color(0xFF064E3B)),
    VIOLET("#7C3AED", Color(0xFF7C3AED), Color(0xFFA78BFA), Color(0xFFEDE9FE), Color(0xFF2E1065)),
    OCEAN_BLUE("#2563EB", Color(0xFF2563EB), Color(0xFF60A5FA), Color(0xFFDBEAFE), Color(0xFF1E3A8A)),
    MINT_EMERALD("#10B981", Color(0xFF10B981), Color(0xFF34D399), Color(0xFFD1FAE5), Color(0xFF064E3B)),
    AMBER_GOLD("#D97706", Color(0xFFD97706), Color(0xFFFBBF24), Color(0xFFFEF3C7), Color(0xFF78350F)),
    ROSE("#E11D48", Color(0xFFE11D48), Color(0xFFFB7185), Color(0xFFFFE4E6), Color(0xFF881337))
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
    VET_IMPORTER;

    val isOwnerRole: Boolean
        get() = this == OWNER || this == OWNER_BARN

    val isVetRole: Boolean
        get() = this == VETERINARIAN || this == VET_CLINIC

    val normalizedHubRole: AppRole
        get() = when (this) {
            OWNER -> OWNER_BARN
            VETERINARIAN -> VET_CLINIC
            else -> this
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
    val id: String,
    val name: String,
    val location: String,
    val capacity: Int,
    val assignedWorkerId: String,
    val members: Map<String, String> = emptyMap(),
    val currencyCode: String = "DZD"
)

data class CycleModel(
    val id: String,
    val farmId: String,
    val totalBirdsPlaced: Int,
    val targetWeight: Double = 2.3,
    val status: String = "active",
    val startDate: String = "2026-03-01",
    val chickPrice: Double = 120.0,
    val feedInventoryKg: Double = 0.0,
    val cumulativeBirdsSold: Int = 0,
    val cumulativeSoldWeightKg: Double = 0.0,
    val currencyCode: String = "DZD"
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
    val temperature: Double,
    val gasUnits: Double = 0.0,
    val ventilationStatus: String = "جيدة",
    val generalStatus: String = "طبيعية",
    val isLocked: Boolean = true,
    val isEdited: Boolean = false,
    val previousSummary: String = "",
    val actualAvgWeightKg: Double = 0.0
)

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
    val currency: String = "دج"
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
    val requestCategory: String = "",
    val requestAmount: Double = 0.0,
    val requestCurrency: String = "دج"
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
    val phoneNumber: String = "+213 550 10 20 30",
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
    val publisherPhone: String = "+213 550 10 20 30",
    val isPhoneContact: Boolean = true
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

data class TransactionModel(
    val id: String,
    val farmId: String,
    val cycleId: String,
    val type: TransactionType,
    val category: String,
    val amount: Double,
    val currency: String = "دج",
    val date: String,
    val invoiceData: SalesInvoiceModel? = null,
    val status: String = "APPROVED"
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
 * Data model for a parsed QR invitation payload (`AGRITECH_JOIN|farmId=...|role=...`).
 */
data class QrFarmInviteData(
    val farmId: String,
    val role: AppRole,
    val rawPayload: String
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
