package com.example.utils

import android.content.Context
import android.telephony.TelephonyManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.AgritechMockDb
import com.example.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

// ============================================================================
// GLOBAL SAAS ONBOARDING MODELS & CATALOGS (LANGUAGE, COUNTRY, CURRENCY)
// ============================================================================

data class SaasCountryOption(
    val code: String,
    val flag: String,
    val nameAr: String,
    val nameFr: String,
    val nameEn: String,
    val nameEs: String,
    val namePt: String,
    val defaultCurrencyCode: String,
    val defaultLanguage: AppLanguage = AppLanguage.EN
) {
    fun localizedName(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR, AppLanguage.UR -> nameAr
        AppLanguage.FR -> nameFr
        AppLanguage.EN -> nameEn
        AppLanguage.ES -> nameEs
        AppLanguage.PT -> namePt
        else -> nameEn
    }
}

data class SaasCurrencyOption(
    val code: String,
    val symbol: String,
    val flag: String,
    val nameAr: String,
    val nameFr: String,
    val nameEn: String,
    val nameEs: String,
    val namePt: String
) {
    fun localizedName(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR, AppLanguage.UR -> nameAr
        AppLanguage.FR -> nameFr
        AppLanguage.EN -> nameEn
        AppLanguage.ES -> nameEs
        AppLanguage.PT -> namePt
        else -> nameEn
    }
}

data class DetectedInitialLocale(
    val language: AppLanguage,
    val countryCode: String,
    val currencyCode: String
)

object SaasGlobalCatalog {
    // Full 14 Global Languages (Strictly NO country flags on languages)
    val onboardingLanguages: List<AppLanguage> = AppLanguage.values().toList()

    val countries: List<SaasCountryOption> = listOf(
        SaasCountryOption("DZ", "🇩🇿", "الجزائر", "Algérie", "Algeria", "Argelia", "Argélia", "DZD", AppLanguage.AR),
        SaasCountryOption("MA", "🇲🇦", "المغرب", "Maroc", "Morocco", "Marruecos", "Marrocos", "MAD", AppLanguage.AR),
        SaasCountryOption("TN", "🇹🇳", "تونس", "Tunisie", "Tunisia", "Túnez", "Tunísia", "TND", AppLanguage.AR),
        SaasCountryOption("SA", "🇸🇦", "المملكة العربية السعودية", "Arabie Saoudite", "Saudi Arabia", "Arabia Saudita", "Arábia Saudita", "SAR", AppLanguage.AR),
        SaasCountryOption("EG", "🇪🇬", "مصر", "Égypte", "Egypt", "Egipto", "Egito", "EGP", AppLanguage.AR),
        SaasCountryOption("US", "🇺🇸", "الولايات المتحدة", "États-Unis", "United States", "Estados Unidos", "Estados Unidos", "USD", AppLanguage.EN),
        SaasCountryOption("CA", "🇨🇦", "كندا", "Canada", "Canada", "Canadá", "Canadá", "CAD", AppLanguage.EN),
        SaasCountryOption("CO", "🇨🇴", "كولومبيا", "Colombie", "Colombia", "Colombia", "Colômbia", "COP", AppLanguage.ES),
        SaasCountryOption("BR", "🇧🇷", "البرازيل", "Brésil", "Brazil", "Brasil", "Brasil", "BRL", AppLanguage.PT),
        SaasCountryOption("MX", "🇲🇽", "المكسيك", "Mexique", "Mexico", "México", "México", "MXN", AppLanguage.ES),
        SaasCountryOption("AR", "🇦🇷", "الأرجنتين", "Argentine", "Argentina", "Argentina", "Argentina", "ARS", AppLanguage.ES),
        SaasCountryOption("FR", "🇫🇷", "فرنسا", "France", "France", "Francia", "França", "EUR", AppLanguage.FR),
        SaasCountryOption("ES", "🇪🇸", "إسبانيا", "Espagne", "Spain", "España", "Espanha", "EUR", AppLanguage.ES),
        SaasCountryOption("PT", "🇵🇹", "البرتغال", "Portugal", "Portugal", "Portugal", "Portugal", "EUR", AppLanguage.PT),
        SaasCountryOption("TR", "🇹🇷", "تركيا", "Turquie", "Turkey", "Turquía", "Turquia", "TRY", AppLanguage.TR),
        SaasCountryOption("IN", "🇮🇳", "الهند", "Inde", "India", "India", "Índia", "INR", AppLanguage.HI),
        SaasCountryOption("ID", "🇮🇩", "إندونيسيا", "Indonésie", "Indonesia", "Indonesia", "Indonésia", "IDR", AppLanguage.ID),
        SaasCountryOption("PK", "🇵🇰", "باكستان", "Pakistan", "Pakistan", "Pakistán", "Paquistão", "PKR", AppLanguage.UR),
        SaasCountryOption("KE", "🇰🇪", "كينيا", "Kenya", "Kenya", "Kenia", "Quênia", "KES", AppLanguage.SW),
        SaasCountryOption("RU", "🇷🇺", "روسيا", "Russie", "Russia", "Rusia", "Rússia", "RUB", AppLanguage.RU),
        SaasCountryOption("CN", "🇨🇳", "الصين", "Chine", "China", "China", "China", "CNY", AppLanguage.ZH),
        SaasCountryOption("DE", "🇩🇪", "ألمانيا", "Allemagne", "Germany", "Alemania", "Alemanha", "EUR", AppLanguage.DE),
        SaasCountryOption("IT", "🇮🇹", "إيطاليا", "Italie", "Italy", "Italia", "Itália", "EUR", AppLanguage.IT)
    )

    val currencies: List<SaasCurrencyOption> = listOf(
        SaasCurrencyOption("DZD", "دج", "🇩🇿", "دينار جزائري (DZD)", "Dinar Algérien (DZD)", "Algerian Dinar (DZD)", "Dinar Argelino (DZD)", "Dinar Argelino (DZD)"),
        SaasCurrencyOption("MAD", "DH", "🇲🇦", "درهم مغربي (MAD)", "Dirham Marocain (MAD)", "Moroccan Dirham (MAD)", "Dírham Marroquí (MAD)", "Dirham Marroquino (MAD)"),
        SaasCurrencyOption("TND", "DT", "🇹🇳", "دينار تونسي (TND)", "Dinar Tunisien (TND)", "Tunisian Dinar (TND)", "Dinar Tunecino (TND)", "Dinar Tunisiano (TND)"),
        SaasCurrencyOption("SAR", "ر.س", "🇸🇦", "ريال سعودي (SAR)", "Riyal Saoudien (SAR)", "Saudi Riyal (SAR)", "Riyal Saudí (SAR)", "Riyal Saudita (SAR)"),
        SaasCurrencyOption("EGP", "ج.م", "🇪🇬", "جنيه مصري (EGP)", "Livre Égyptienne (EGP)", "Egyptian Pound (EGP)", "Libra Egipcia (EGP)", "Libra Egípcia (EGP)"),
        SaasCurrencyOption("USD", "$", "🇺🇸", "دولار أمريكي (USD)", "Dollar Américain (USD)", "US Dollar (USD)", "Dólar Estadounidense (USD)", "Dólar Americano (USD)"),
        SaasCurrencyOption("CAD", "CA$", "🇨🇦", "دولار كندي (CAD)", "Dollar Canadien (CAD)", "Canadian Dollar (CAD)", "Dólar Canadiense (CAD)", "Dólar Canadense (CAD)"),
        SaasCurrencyOption("COP", "COL$", "🇨🇴", "بيزو كولومبي (COP)", "Peso Colombien (COP)", "Colombian Peso (COP)", "Peso Colombiano (COP)", "Peso Colombiano (COP)"),
        SaasCurrencyOption("BRL", "R$", "🇧🇷", "ريال برازيلي (BRL)", "Réal Brésilien (BRL)", "Brazilian Real (BRL)", "Real Brasileño (BRL)", "Real Brasileiro (BRL)"),
        SaasCurrencyOption("MXN", "MX$", "🇲🇽", "بيزو مكسيكي (MXN)", "Peso Mexicain (MXN)", "Mexican Peso (MXN)", "Peso Mexicano (MXN)", "Peso Mexicano (MXN)"),
        SaasCurrencyOption("ARS", "ARS$", "🇦🇷", "بيزو أرجنتيني (ARS)", "Peso Argentin (ARS)", "Argentine Peso (ARS)", "Peso Argentino (ARS)", "Peso Argentino (ARS)"),
        SaasCurrencyOption("EUR", "€", "🇪🇺", "يورو أوروبي (EUR)", "Euro (EUR)", "Euro (EUR)", "Euro (EUR)", "Euro (EUR)"),
        SaasCurrencyOption("TRY", "₺", "🇹🇷", "ليرة تركية (TRY)", "Livre Turque (TRY)", "Turkish Lira (TRY)", "Lira Turca (TRY)", "Lira Turca (TRY)"),
        SaasCurrencyOption("INR", "₹", "🇮🇳", "روبية هندية (INR)", "Roupie Indienne (INR)", "Indian Rupee (INR)", "Rupia India (INR)", "Rupia Indiana (INR)"),
        SaasCurrencyOption("IDR", "Rp", "🇮🇩", "روبية إندونيسية (IDR)", "Roupie Indonésienne (IDR)", "Indonesian Rupiah (IDR)", "Rupia Indonesia (IDR)", "Rupia Indonésia (IDR)"),
        SaasCurrencyOption("PKR", "Rs", "🇵🇰", "روبية باكستانية (PKR)", "Roupie Pakistanaise (PKR)", "Pakistani Rupee (PKR)", "Rupia Paquistaní (PKR)", "Rupia Paquistanesa (PKR)"),
        SaasCurrencyOption("KES", "KSh", "🇰🇪", "شلن كيني (KES)", "Shilling Kényan (KES)", "Kenyan Shilling (KES)", "Chelín Keniano (KES)", "Xelim Queniano (KES)"),
        SaasCurrencyOption("RUB", "₽", "🇷🇺", "روبل روسي (RUB)", "Rouble Russe (RUB)", "Russian Ruble (RUB)", "Rublo Ruso (RUB)", "Rublo Russo (RUB)"),
        SaasCurrencyOption("CNY", "¥", "🇨🇳", "يوان صيني (CNY)", "Yuan Chinois (CNY)", "Chinese Yuan (CNY)", "Yuan Chino (CNY)", "Yuan Chinês (CNY)")
    )
}

/**
 * Smart Auto-Detection of Country, Currency, and Language on First Launch:
 * Uses `TelephonyManager.networkCountryIso` / `simCountryIso` and `Locale.getDefault()`
 * to automatically map the user's region to Country (`DZ`, `MA`, `TN`, `SA`, `EG`, `US`, `CA`,
 * `CO`, `BR`, `MX`, `AR`, `FR`, `ES`, `PT`, etc.) and real Currency (`DZD`, `MAD`, `TND`, `SAR`,
 * `EGP`, `USD`, `CAD`, `COP`, `BRL`, `MXN`, `ARS`, `EUR`, etc.).
 */
fun detectInitialLocaleAndCurrency(
    context: Context? = null,
    overrideCountryIso: String? = null,
    overrideLanguageIso: String? = null
): DetectedInitialLocale {
    val telephonyCountry = try {
        val tm = context?.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val netIso = tm?.networkCountryIso?.trim()?.uppercase(Locale.US)
        val simIso = tm?.simCountryIso?.trim()?.uppercase(Locale.US)
        when {
            !netIso.isNullOrBlank() && netIso.length == 2 -> netIso
            !simIso.isNullOrBlank() && simIso.length == 2 -> simIso
            else -> null
        }
    } catch (_: Exception) {
        null
    }

    val defaultLocale = try {
        Locale.getDefault()
    } catch (_: Exception) {
        Locale("ar", "DZ")
    }

    val rawCountryIso = (overrideCountryIso?.trim()?.uppercase(Locale.US)?.takeIf { it.length == 2 }
        ?: telephonyCountry
        ?: defaultLocale.country.trim().uppercase(Locale.US).takeIf { it.length == 2 }
        ?: "DZ")

    val matchedCountry = SaasGlobalCatalog.countries.firstOrNull {
        it.code.equals(rawCountryIso, ignoreCase = true)
    }

    val resolvedCountryCode = matchedCountry?.code ?: when (rawCountryIso) {
        "DZ", "MA", "TN", "SA", "EG", "US", "CA", "CO", "BR", "MX", "AR", "FR", "ES", "PT",
        "TR", "IN", "ID", "PK", "KE", "RU", "CN", "DE", "IT" -> rawCountryIso
        else -> "DZ"
    }

    val resolvedCurrencyCode = matchedCountry?.defaultCurrencyCode ?: when (resolvedCountryCode) {
        "DZ" -> "DZD"
        "MA" -> "MAD"
        "TN" -> "TND"
        "SA" -> "SAR"
        "EG" -> "EGP"
        "US" -> "USD"
        "CA" -> "CAD"
        "CO" -> "COP"
        "BR" -> "BRL"
        "MX" -> "MXN"
        "AR" -> "ARS"
        "FR", "ES", "PT", "DE", "IT" -> "EUR"
        "TR" -> "TRY"
        "IN" -> "INR"
        "ID" -> "IDR"
        "PK" -> "PKR"
        "KE" -> "KES"
        "RU" -> "RUB"
        "CN" -> "CNY"
        else -> "DZD"
    }

    val rawLangIso = (overrideLanguageIso?.trim()?.lowercase(Locale.US)?.takeIf { it.isNotBlank() }
        ?: defaultLocale.language.trim().lowercase(Locale.US))

    val resolvedLanguage = AppLanguage.values().firstOrNull {
        it.code.equals(rawLangIso, ignoreCase = true)
    } ?: matchedCountry?.defaultLanguage ?: AppLanguage.AR

    return DetectedInitialLocale(
        language = resolvedLanguage,
        countryCode = resolvedCountryCode,
        currencyCode = resolvedCurrencyCode
    )
}

// ============================================================================
// LOCAL PREFERENCES & PERSISTENT AUTH/THEME/APPEARANCE SESSION ENGINE
// ============================================================================

object SaasPreferencesManager {
    private const val PREFS_NAME = "agritech_saas_prefs"
    private const val KEY_ONBOARDING_DONE = "onboarding_completed"
    private const val KEY_LANGUAGE = "selected_language"
    private const val KEY_COUNTRY = "selected_country"
    private const val KEY_CURRENCY = "selected_currency"
    private const val KEY_THEME = "selected_theme"
    private const val KEY_ACCENT_COLOR = "selected_accent_color"
    private const val KEY_TEXT_SCALE = "selected_text_scale"
    private const val KEY_CORNER_STYLE = "selected_corner_style"
    private const val KEY_UNLOCKED_ADDITIONAL_BARNS = "unlocked_additional_barns"
    private const val KEY_IS_LOGGED_IN = "session_is_logged_in"
    private const val KEY_USER_ROLE = "session_user_role"
    private const val KEY_HAS_LINKED_BARNS = "session_has_linked_barns"
    private const val KEY_ACTIVE_FARM_ID = "session_active_farm_id"
    private const val KEY_CURRENT_USER_UID = "session_current_user_uid"
    private const val KEY_PROFESSION_ONBOARDING_DONE = "profession_onboarding_completed"

    fun hasCompletedOnboarding(context: Context): Boolean {
        return try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_ONBOARDING_DONE, false)
        } catch (_: Exception) {
            false
        }
    }

    fun hasSelectedInitialProfession(context: Context): Boolean {
        return try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_PROFESSION_ONBOARDING_DONE, false)
        } catch (_: Exception) {
            false
        }
    }

    fun saveInitialProfessionSelected(context: Context, role: AppRole) {
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_PROFESSION_ONBOARDING_DONE, true)
                .putString(KEY_USER_ROLE, role.name)
                .apply()
        } catch (_: Exception) {}
    }

    fun getSavedLanguage(context: Context): AppLanguage {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedCode = prefs.getString(KEY_LANGUAGE, null)
            if (!savedCode.isNullOrBlank()) {
                AppLanguage.values().firstOrNull { it.code == savedCode } ?: AppLanguage.AR
            } else {
                AppLanguage.AR
            }
        } catch (_: Exception) {
            AppLanguage.AR
        }
    }

    fun getSavedTheme(context: Context): AppThemeOption {
        return try {
            val name = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_THEME, AppThemeOption.ICE_BLUE.name) ?: AppThemeOption.ICE_BLUE.name
            AppThemeOption.values().firstOrNull { it.name == name } ?: AppThemeOption.ICE_BLUE
        } catch (_: Exception) {
            AppThemeOption.ICE_BLUE
        }
    }

    fun saveTheme(context: Context, theme: AppThemeOption) {
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_THEME, theme.name)
                .apply()
        } catch (_: Exception) {}
    }

    fun getSavedAccentColor(context: Context): AppAccentColorOption {
        return try {
            val name = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_ACCENT_COLOR, AppAccentColorOption.FOREST_GREEN.name)
                ?: AppAccentColorOption.FOREST_GREEN.name
            AppAccentColorOption.values().firstOrNull { it.name == name } ?: AppAccentColorOption.FOREST_GREEN
        } catch (_: Exception) {
            AppAccentColorOption.FOREST_GREEN
        }
    }

    fun getSavedTextScale(context: Context): AppTextScaleOption {
        return try {
            val name = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_TEXT_SCALE, AppTextScaleOption.DEFAULT.name)
                ?: AppTextScaleOption.DEFAULT.name
            AppTextScaleOption.values().firstOrNull { it.name == name } ?: AppTextScaleOption.DEFAULT
        } catch (_: Exception) {
            AppTextScaleOption.DEFAULT
        }
    }

    fun getSavedCornerStyle(context: Context): AppCornerStyleOption {
        return try {
            val name = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_CORNER_STYLE, AppCornerStyleOption.ROUNDED.name)
                ?: AppCornerStyleOption.ROUNDED.name
            AppCornerStyleOption.values().firstOrNull { it.name == name } ?: AppCornerStyleOption.ROUNDED
        } catch (_: Exception) {
            AppCornerStyleOption.ROUNDED
        }
    }

    fun saveAppearanceStudioSettings(
        context: Context,
        theme: AppThemeOption,
        accentColor: AppAccentColorOption,
        textScale: AppTextScaleOption,
        cornerStyle: AppCornerStyleOption
    ) {
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_THEME, theme.name)
                .putString(KEY_ACCENT_COLOR, accentColor.name)
                .putString(KEY_TEXT_SCALE, textScale.name)
                .putString(KEY_CORNER_STYLE, cornerStyle.name)
                .apply()
        } catch (_: Exception) {}
    }

    fun getSavedCountryCode(context: Context): String {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val saved = prefs.getString(KEY_COUNTRY, null)
            if (!saved.isNullOrBlank()) saved else detectInitialLocaleAndCurrency(context).countryCode
        } catch (_: Exception) {
            "DZ"
        }
    }

    fun getSavedCurrencyCode(context: Context): String {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val saved = prefs.getString(KEY_CURRENCY, null)
            if (!saved.isNullOrBlank()) saved else detectInitialLocaleAndCurrency(context).currencyCode
        } catch (_: Exception) {
            "DZD"
        }
    }

    fun getUnlockedAdditionalBarns(context: Context): Int {
        return try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_UNLOCKED_ADDITIONAL_BARNS, 0)
        } catch (_: Exception) {
            0
        }
    }

    fun saveUnlockedAdditionalBarns(context: Context, unlockedCount: Int) {
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_UNLOCKED_ADDITIONAL_BARNS, unlockedCount)
                .apply()
        } catch (_: Exception) {}
    }

    fun getSavedIsLoggedIn(context: Context): Boolean {
        return try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_IS_LOGGED_IN, false)
        } catch (_: Exception) {
            false
        }
    }

    fun getSavedUserRole(context: Context): AppRole? {
        return try {
            val roleName = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USER_ROLE, null) ?: return null
            AppRole.values().firstOrNull { it.name == roleName }
        } catch (_: Exception) {
            null
        }
    }

    fun getSavedHasLinkedBarns(context: Context): Boolean {
        return try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_HAS_LINKED_BARNS, false)
        } catch (_: Exception) {
            false
        }
    }

    fun getSavedActiveFarmId(context: Context): String {
        return try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_ACTIVE_FARM_ID, "") ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    fun getSavedUserUid(context: Context): String? {
        return try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_CURRENT_USER_UID, null)
        } catch (_: Exception) {
            null
        }
    }

    fun saveAuthSession(
        context: Context,
        isLoggedIn: Boolean,
        role: AppRole?,
        hasLinkedBarns: Boolean,
        activeFarmId: String,
        userUid: String?
    ) {
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
                .putString(KEY_USER_ROLE, role?.name)
                .putBoolean(KEY_HAS_LINKED_BARNS, hasLinkedBarns)
                .putString(KEY_ACTIVE_FARM_ID, activeFarmId)
                .putString(KEY_CURRENT_USER_UID, userUid)
                .apply()
        } catch (_: Exception) {}
    }

    fun clearAuthSession(context: Context) {
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .remove(KEY_USER_ROLE)
                .putBoolean(KEY_HAS_LINKED_BARNS, false)
                .remove(KEY_CURRENT_USER_UID)
                .apply()
        } catch (_: Exception) {}
    }

    fun saveOnboardingPreferences(
        context: Context,
        language: AppLanguage,
        countryCode: String,
        currencyCode: String
    ) {
        Translations.globalCountryCode = countryCode
        Translations.globalCurrencyCode = currencyCode
        applyCurrencyToMockTransactions(currencyCode)
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_ONBOARDING_DONE, true)
                .putString(KEY_LANGUAGE, language.code)
                .putString(KEY_COUNTRY, countryCode)
                .putString(KEY_CURRENCY, currencyCode)
                .apply()
        } catch (_: Exception) {}
    }

    fun applyCurrencyToMockTransactions(currencyCode: String) {
        if (currencyCode.isBlank()) return
        for (i in AgritechMockDb.transactions.indices) {
            val tx = AgritechMockDb.transactions[i]
            AgritechMockDb.transactions[i] = tx.copy(currency = currencyCode)
        }
    }
}

// ============================================================================
// HOISTED APP SESSION & GLOBAL SETTINGS STATE HOLDER (CompositionLocal)
// ============================================================================

@Stable
class AgritechAppSessionState(
    initialOnboardingCompleted: Boolean,
    initialTheme: AppThemeOption,
    initialLanguage: AppLanguage,
    initialCountryCode: String,
    initialCurrencyCode: String,
    initialIsLoggedIn: Boolean,
    initialRole: AppRole?,
    initialHasLinkedBarns: Boolean,
    initialActiveFarmId: String,
    initialUserUid: String?,
    initialAccentColor: AppAccentColorOption = AppAccentColorOption.FOREST_GREEN,
    initialTextScale: AppTextScaleOption = AppTextScaleOption.DEFAULT,
    initialCornerStyle: AppCornerStyleOption = AppCornerStyleOption.ROUNDED,
    initialProfessionSelected: Boolean = true
) {
    var hasCompletedOnboarding by mutableStateOf(initialOnboardingCompleted)
    var hasSelectedInitialProfession by mutableStateOf(initialProfessionSelected)
    var currentTheme by mutableStateOf(initialTheme)
    var currentLanguage by mutableStateOf(initialLanguage)
    var currentCountryCode by mutableStateOf(initialCountryCode)
    var currentCurrencyCode by mutableStateOf(initialCurrencyCode)

    // Appearance Studio Live Preferences
    var currentAccentColor by mutableStateOf(initialAccentColor)
    var currentTextScale by mutableStateOf(initialTextScale)
    var currentCornerStyle by mutableStateOf(initialCornerStyle)

    // Hoisted Auth & Navigation State — NEVER resets on recomposition or theme/language changes
    var isLoggedIn by mutableStateOf(initialIsLoggedIn)
    var currentUserUid by mutableStateOf(initialUserUid)
    var currentRole by mutableStateOf(initialRole)
    var hasLinkedBarns by mutableStateOf(initialHasLinkedBarns)
    var activeFarmId by mutableStateOf(initialActiveFarmId)

    // Guard flag ensuring auto Google Sign-In is attempted AT MOST ONCE per cold start when unauthenticated
    var hasAttemptedInitialAutoSignIn by mutableStateOf(false)

    // Hoisted Selected Farm Navigation Stack so switching Theme/Language/Currency never kicks user out of open notebook
    var selectedFarmForOwner by mutableStateOf<FarmModel?>(null)
    var selectedFarmForVet by mutableStateOf<FarmModel?>(null)
    var selectedFarmForWorker by mutableStateOf<FarmModel?>(null)

    fun completeInitialProfessionSelection(context: Context, chosenRole: AppRole) {
        hasSelectedInitialProfession = true
        currentRole = chosenRole
        selectedFarmForOwner = null
        selectedFarmForVet = null
        selectedFarmForWorker = null
        SaasPreferencesManager.saveInitialProfessionSelected(context, chosenRole)
        if (isLoggedIn) {
            SaasPreferencesManager.saveAuthSession(
                context = context,
                isLoggedIn = true,
                role = chosenRole,
                hasLinkedBarns = hasLinkedBarns,
                activeFarmId = activeFarmId,
                userUid = currentUserUid
            )
        }
    }

    fun markAuthenticated(
        context: Context,
        role: AppRole,
        linkedBarns: Boolean,
        farmId: String = activeFarmId,
        uid: String? = currentUserUid
    ) {
        isLoggedIn = true
        currentRole = role
        hasLinkedBarns = linkedBarns
        activeFarmId = farmId
        if (uid != null) {
            currentUserUid = uid
        }
        SaasPreferencesManager.saveAuthSession(
            context = context,
            isLoggedIn = true,
            role = role,
            hasLinkedBarns = linkedBarns,
            activeFarmId = farmId,
            userUid = currentUserUid
        )
    }

    fun performExplicitLogout(context: Context) {
        isLoggedIn = false
        currentRole = null
        hasLinkedBarns = false
        currentUserUid = null
        selectedFarmForOwner = null
        selectedFarmForVet = null
        selectedFarmForWorker = null
        SaasPreferencesManager.clearAuthSession(context)
        AgritechMockDb.clearOnExplicitLogout()
        LocalFarmPersistence.clear(context)
    }

    fun updateTheme(context: Context, newTheme: AppThemeOption) {
        currentTheme = newTheme
        SaasPreferencesManager.saveTheme(context, newTheme)
    }

    fun updateAppearanceStudio(
        context: Context,
        newTheme: AppThemeOption = currentTheme,
        newAccentColor: AppAccentColorOption = currentAccentColor,
        newTextScale: AppTextScaleOption = currentTextScale,
        newCornerStyle: AppCornerStyleOption = currentCornerStyle
    ) {
        currentTheme = newTheme
        currentAccentColor = newAccentColor
        currentTextScale = newTextScale
        currentCornerStyle = newCornerStyle
        SaasPreferencesManager.saveAppearanceStudioSettings(
            context = context,
            theme = newTheme,
            accentColor = newAccentColor,
            textScale = newTextScale,
            cornerStyle = newCornerStyle
        )
    }

    fun updateGlobalSettings(
        context: Context,
        newLanguage: AppLanguage = currentLanguage,
        newCountryCode: String = currentCountryCode,
        newCurrencyCode: String = currentCurrencyCode
    ) {
        currentLanguage = newLanguage
        currentCountryCode = newCountryCode
        currentCurrencyCode = newCurrencyCode
        hasCompletedOnboarding = true
        SaasPreferencesManager.saveOnboardingPreferences(
            context = context,
            language = newLanguage,
            countryCode = newCountryCode,
            currencyCode = newCurrencyCode
        )
    }

    fun updateAccentColor(context: Context, newAccentColor: AppAccentColorOption) {
        updateAppearanceStudio(context = context, newAccentColor = newAccentColor)
    }

    fun updateCornerStyle(context: Context, newCornerStyle: AppCornerStyleOption) {
        updateAppearanceStudio(context = context, newCornerStyle = newCornerStyle)
    }

    val appearanceState: AppAppearanceState
        get() = AppAppearanceState(
            theme = currentTheme,
            isDark = currentTheme.isDark,
            accentOption = currentAccentColor,
            primaryAccent = currentAccentColor.primaryColor,
            darkModeAccent = currentAccentColor.darkModeColor,
            activeAccent = if (currentTheme.isDark) currentAccentColor.darkModeColor else currentAccentColor.primaryColor,
            activeAccentContainer = if (currentTheme.isDark) currentAccentColor.darkContainerColor else currentAccentColor.softContainerColor,
            textScale = currentTextScale,
            cornerStyle = currentCornerStyle,
            cornerRadiusDp = currentCornerStyle.cornerRadiusDp,
            cardShape = RoundedCornerShape(currentCornerStyle.cornerRadiusDp.dp),
            buttonShape = RoundedCornerShape((currentCornerStyle.cornerRadiusDp - 4).coerceAtLeast(4).dp)
        )
}

data class AppAppearanceState(
    val theme: AppThemeOption = AppThemeOption.ICE_BLUE,
    val isDark: Boolean = theme.isDark,
    val accentOption: AppAccentColorOption = AppAccentColorOption.FOREST_GREEN,
    val primaryAccent: Color = accentOption.primaryColor,
    val darkModeAccent: Color = accentOption.darkModeColor,
    val activeAccent: Color = if (isDark) darkModeAccent else primaryAccent,
    val activeAccentContainer: Color = if (isDark) accentOption.darkContainerColor else accentOption.softContainerColor,
    val textScale: AppTextScaleOption = AppTextScaleOption.DEFAULT,
    val cornerStyle: AppCornerStyleOption = AppCornerStyleOption.ROUNDED,
    val cornerRadiusDp: Int = cornerStyle.cornerRadiusDp,
    val cardShape: RoundedCornerShape = RoundedCornerShape(cornerStyle.cornerRadiusDp.dp),
    val buttonShape: RoundedCornerShape = RoundedCornerShape((cornerStyle.cornerRadiusDp - 4).coerceAtLeast(4).dp)
) {
    val accentColor: Color get() = activeAccent
    val accentContainerColor: Color get() = activeAccentContainer
    val fontScale: Float get() = textScale.scaleFactor
}

val LocalAppAppearanceState = compositionLocalOf { AppAppearanceState() }

val LocalAgritechAppState = compositionLocalOf<AgritechAppSessionState> {
    AgritechAppSessionState(
        initialOnboardingCompleted = true,
        initialTheme = AppThemeOption.ICE_BLUE,
        initialLanguage = AppLanguage.AR,
        initialCountryCode = "DZ",
        initialCurrencyCode = "DZD",
        initialIsLoggedIn = false,
        initialRole = AppRole.OWNER,
        initialHasLinkedBarns = true,
        initialActiveFarmId = "",
        initialUserUid = null
    )
}

/**
 * Freemium Paywall Rule (1st Barn Free, 2nd+ Barn is $4.99 / 2 Months):
 * - If existingOwnedCount == 0: Free Tier (bypass paywall -> returns false).
 * - If existingOwnedCount >= 1: Intercept and show SubscriptionPaywallBottomSheet (returns true),
 *   unless the user has unlocked an additional barn slot via subscription.
 */
fun shouldInterceptNewBarnWithPaywall(
    existingOwnedCount: Int,
    unlockedAdditionalBarnsCount: Int = 0
): Boolean {
    if (existingOwnedCount <= 0) return false
    return existingOwnedCount >= (1 + unlockedAdditionalBarnsCount)
}

// ============================================================================
// 1. GLOBAL ONBOARDING SCREEN (AUTO-DETECTED LOCALE + MINIMALIST SELECTORS)
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalOnboardingScreen(
    initialLanguage: AppLanguage = AppLanguage.AR,
    initialCountryCode: String = "DZ",
    initialCurrencyCode: String = "DZD",
    currentTheme: AppThemeOption = AppThemeOption.ICE_BLUE,
    onCompleteOnboarding: (AppLanguage, String, String) -> Unit
) {
    var selectedLang by remember(initialLanguage) { mutableStateOf(initialLanguage) }
    var selectedCountry by remember(initialCountryCode) {
        mutableStateOf(
            SaasGlobalCatalog.countries.firstOrNull { it.code == initialCountryCode }
                ?: SaasGlobalCatalog.countries.first()
        )
    }
    var selectedCurrency by remember(initialCurrencyCode) {
        mutableStateOf(
            SaasGlobalCatalog.currencies.firstOrNull { it.code == initialCurrencyCode }
                ?: SaasGlobalCatalog.currencies.first()
        )
    }

    var languageMenuExpanded by remember { mutableStateOf(false) }
    var countryMenuExpanded by remember { mutableStateOf(false) }
    var showCurrencyBottomSheet by remember { mutableStateOf(false) }

    val isDarkTheme = currentTheme.isDark
    val fieldContainerColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val fieldBorderColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)
    val primaryTextColor = if (isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val secondaryTextColor = if (isDarkTheme) Color(0xFFCBD5E1) else Color(0xFF64748B)
    val accentGreenText = if (isDarkTheme) Color(0xFF34D399) else TextForestGreen
    val accentGreenSurface = if (isDarkTheme) Color(0xFF064E3B) else LightGreenContainer

    Scaffold(
        containerColor = currentTheme.bgColor,
        modifier = Modifier
            .fillMaxSize()
            .testTag("global_onboarding_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated Greeting Hero Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, fieldBorderColor.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = accentGreenSurface
                            ) {
                                Text(
                                    text = Translations.tr("onboarding_badge", selectedLang),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentGreenText
                                )
                            }

                            AnimatedContent(
                                targetState = selectedLang.greeting,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                                },
                                label = "OnboardingGreetingAnim"
                            ) { greetingText ->
                                Text(
                                    text = "$greetingText 👋",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = accentGreenText
                                )
                            }
                        }

                        Text(
                            text = Translations.tr("onboarding_title", selectedLang),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = primaryTextColor
                        )

                        Text(
                            text = Translations.tr("onboarding_subtitle", selectedLang),
                            fontSize = 12.5.sp,
                            color = secondaryTextColor,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Minimalist Single Form Card: 3 Compact Selectors (Language, Country, Currency)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, fieldBorderColor.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. Compact Language Dropdown (Flag-Free Native Names + Subtitle + Checkmark)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = Translations.tr("onboarding_lang_label", selectedLang),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp,
                                color = secondaryTextColor
                            )

                            ExposedDropdownMenuBox(
                                expanded = languageMenuExpanded,
                                onExpandedChange = { languageMenuExpanded = !languageMenuExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedLang.label,
                                    onValueChange = {},
                                    readOnly = true,
                                    singleLine = true,
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Language,
                                            contentDescription = null,
                                            tint = accentGreenText,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageMenuExpanded)
                                    },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                        .testTag("onboarding_language_dropdown"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = accentGreenText,
                                        unfocusedBorderColor = fieldBorderColor,
                                        focusedTextColor = primaryTextColor,
                                        unfocusedTextColor = primaryTextColor,
                                        focusedContainerColor = fieldContainerColor,
                                        unfocusedContainerColor = fieldContainerColor
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = languageMenuExpanded,
                                    onDismissRequest = { languageMenuExpanded = false },
                                    modifier = Modifier.background(currentTheme.cardColor)
                                ) {
                                    SaasGlobalCatalog.onboardingLanguages.forEach { langOption ->
                                        val isSelected = langOption == selectedLang
                                        DropdownMenuItem(
                                            leadingIcon = {
                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = null,
                                                    colors = RadioButtonDefaults.colors(
                                                        selectedColor = accentGreenText,
                                                        unselectedColor = secondaryTextColor
                                                    )
                                                )
                                            },
                                            text = {
                                                Column {
                                                    Text(
                                                        text = langOption.label,
                                                        color = primaryTextColor,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        fontSize = 13.5.sp
                                                    )
                                                    Text(
                                                        text = langOption.subtitleLabel,
                                                        color = secondaryTextColor,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            },
                                            trailingIcon = {
                                                if (isSelected) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = accentGreenText,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedLang = langOption
                                                languageMenuExpanded = false
                                            },
                                            modifier = Modifier.testTag("onboarding_lang_option_${langOption.code}")
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Compact Country Dropdown (Strictly with Country Flags)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = Translations.tr("onboarding_country_label", selectedLang),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp,
                                color = secondaryTextColor
                            )

                            ExposedDropdownMenuBox(
                                expanded = countryMenuExpanded,
                                onExpandedChange = { countryMenuExpanded = !countryMenuExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = "${selectedCountry.flag}  ${selectedCountry.localizedName(selectedLang)} (${selectedCountry.code})",
                                    onValueChange = {},
                                    readOnly = true,
                                    singleLine = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryMenuExpanded)
                                    },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                        .testTag("onboarding_country_dropdown"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = accentGreenText,
                                        unfocusedBorderColor = fieldBorderColor,
                                        focusedTextColor = primaryTextColor,
                                        unfocusedTextColor = primaryTextColor,
                                        focusedContainerColor = fieldContainerColor,
                                        unfocusedContainerColor = fieldContainerColor
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = countryMenuExpanded,
                                    onDismissRequest = { countryMenuExpanded = false },
                                    modifier = Modifier.background(currentTheme.cardColor)
                                ) {
                                    SaasGlobalCatalog.countries.forEach { country ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Text(country.flag, fontSize = 17.sp)
                                                    Text(
                                                        "${country.localizedName(selectedLang)} (${country.code})",
                                                        color = primaryTextColor,
                                                        fontWeight = if (country.code == selectedCountry.code) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            },
                                            trailingIcon = {
                                                if (country.code == selectedCountry.code) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = accentGreenText,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedCountry = country
                                                SaasGlobalCatalog.currencies.firstOrNull { it.code == country.defaultCurrencyCode }?.let {
                                                    selectedCurrency = it
                                                }
                                                countryMenuExpanded = false
                                            },
                                            modifier = Modifier.testTag("onboarding_country_option_${country.code}")
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Compact Currency Trigger Row (Opens Searchable ModalBottomSheet ONLY when tapped)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = Translations.tr("onboarding_currency_label", selectedLang),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp,
                                color = secondaryTextColor
                            )

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = fieldContainerColor,
                                border = BorderStroke(1.dp, fieldBorderColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { showCurrencyBottomSheet = true }
                                    .testTag("onboarding_currency_selector_card")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(selectedCurrency.flag, fontSize = 19.sp)
                                        Column {
                                            Text(
                                                text = selectedCurrency.localizedName(selectedLang),
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = primaryTextColor
                                            )
                                            Text(
                                                text = "${selectedCurrency.code} • ${selectedCurrency.symbol}",
                                                fontSize = 11.5.sp,
                                                color = secondaryTextColor
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = accentGreenSurface
                                        ) {
                                            Text(
                                                text = selectedCurrency.code,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = accentGreenText
                                            )
                                        }
                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = secondaryTextColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Confirm & Save Global Preferences Button
                Button(
                    onClick = {
                        onCompleteOnboarding(
                            selectedLang,
                            selectedCountry.code,
                            selectedCurrency.code
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_complete_onboarding"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TextForestGreen)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Translations.tr("onboarding_start_btn", selectedLang),
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // Searchable Currency Selector ModalBottomSheet (Opened ONLY when tapped)
    if (showCurrencyBottomSheet) {
        CurrencySelectorBottomSheet(
            selectedCurrencyCode = selectedCurrency.code,
            lang = selectedLang,
            currentTheme = currentTheme,
            onSelectCurrency = { chosen ->
                selectedCurrency = chosen
                showCurrencyBottomSheet = false
            },
            onDismiss = { showCurrencyBottomSheet = false }
        )
    }
}

// ============================================================================
// 2. ULTRA-SMOOTH FULL-SCREEN SETTINGS & APPEARANCE STUDIO (PremiumSettingsModal)
// ============================================================================

enum class SettingsStudioTab {
    MAIN_HUB,
    APPEARANCE,
    LANGUAGE,
    REGION_CURRENCY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumSettingsModal(
    currentTheme: AppThemeOption,
    onSelectTheme: (AppThemeOption) -> Unit,
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    currentCountryCode: String = Translations.globalCountryCode,
    currentCurrencyCode: String = Translations.globalCurrencyCode ?: "DZD",
    onUpdateCountryAndCurrency: (String, String) -> Unit = { _, _ -> },
    onOpenGlobalOnboarding: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sessionState = LocalAgritechAppState.current

    val committedTheme = sessionState.currentTheme
    val committedAccent = sessionState.currentAccentColor
    val committedTextScale = sessionState.currentTextScale
    val committedCornerStyle = sessionState.currentCornerStyle

    var draftTheme by remember(committedTheme) { mutableStateOf(committedTheme) }
    var draftAccent by remember(committedAccent) { mutableStateOf(committedAccent) }
    var draftTextScale by remember(committedTextScale) { mutableStateOf(committedTextScale) }
    var draftCornerStyle by remember(committedCornerStyle) { mutableStateOf(committedCornerStyle) }

    var selectedLang by remember(currentLanguage) { mutableStateOf(currentLanguage) }
    var selectedCountry by remember(currentCountryCode) {
        mutableStateOf(
            SaasGlobalCatalog.countries.firstOrNull { it.code == currentCountryCode }
                ?: SaasGlobalCatalog.countries.first()
        )
    }
    var selectedCurrency by remember(currentCurrencyCode) {
        mutableStateOf(
            SaasGlobalCatalog.currencies.firstOrNull { it.code == currentCurrencyCode }
                ?: SaasGlobalCatalog.currencies.first()
        )
    }

    var activeStudioTab by remember { mutableStateOf(SettingsStudioTab.MAIN_HUB) }
    var showProfessionSelectorSubScreen by remember { mutableStateOf(false) }
    var showCurrencyBottomSheet by remember { mutableStateOf(false) }
    var showPaywallFromSettings by remember { mutableStateOf(false) }
    var showQrPreviewBanner by remember { mutableStateOf(false) }

    // Count pending appearance changes
    val pendingChangesCount = remember(
        draftTheme, committedTheme,
        draftAccent, committedAccent,
        draftTextScale, committedTextScale,
        draftCornerStyle, committedCornerStyle
    ) {
        var count = 0
        if (draftTheme != committedTheme) count++
        if (draftAccent != committedAccent) count++
        if (draftTextScale != committedTextScale) count++
        if (draftCornerStyle != committedCornerStyle) count++
        count
    }

    val isDark = draftTheme.isDark
    val screenBg = if (isDark) Color(0xFF0F172A) else draftTheme.bgColor
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val subtleSurfaceBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val borderCol = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    // Strict High-Contrast Legibility (#FFFFFF primary and #CBD5E1 secondary on dark surfaces)
    val textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFFCBD5E1) else Color(0xFF64748B)
    val activeAccent = if (isDark) draftAccent.darkModeColor else draftAccent.primaryColor
    val activeAccentContainer = if (isDark) draftAccent.darkContainerColor else draftAccent.softContainerColor
    val previewShape = RoundedCornerShape(draftCornerStyle.cornerRadiusDp.dp)

    Dialog(
        onDismissRequest = {
            if (showProfessionSelectorSubScreen) {
                showProfessionSelectorSubScreen = false
            } else if (activeStudioTab != SettingsStudioTab.MAIN_HUB) {
                activeStudioTab = SettingsStudioTab.MAIN_HUB
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        androidx.activity.compose.BackHandler {
            if (showProfessionSelectorSubScreen) {
                showProfessionSelectorSubScreen = false
            } else if (activeStudioTab != SettingsStudioTab.MAIN_HUB) {
                activeStudioTab = SettingsStudioTab.MAIN_HUB
            } else {
                onDismiss()
            }
        }

        Scaffold(
            containerColor = screenBg,
            modifier = Modifier
                .fillMaxSize()
                .testTag("premium_settings_modal"),
            topBar = {
                Surface(
                    color = cardBg,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Top Title Bar (with Back arrow when inside a sub-view)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (showProfessionSelectorSubScreen || activeStudioTab != SettingsStudioTab.MAIN_HUB) {
                                    IconButton(
                                        onClick = {
                                            showProfessionSelectorSubScreen = false
                                            activeStudioTab = SettingsStudioTab.MAIN_HUB
                                        },
                                        modifier = Modifier
                                            .size(38.dp)
                                            .testTag("btn_back_to_settings_hub")
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowBack,
                                            contentDescription = Translations.tr("settings_hub_back_to_main", selectedLang),
                                            tint = activeAccent
                                        )
                                    }
                                } else {
                                    Surface(
                                        shape = CircleShape,
                                        color = activeAccentContainer,
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Tune,
                                                contentDescription = null,
                                                tint = activeAccent,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                Column {
                                    Text(
                                        text = if (showProfessionSelectorSubScreen) {
                                            Translations.tr("settings_hub_row_profession_title", selectedLang)
                                        } else {
                                            when (activeStudioTab) {
                                                SettingsStudioTab.MAIN_HUB -> Translations.tr("settings_dialog_title", selectedLang)
                                                SettingsStudioTab.APPEARANCE -> Translations.tr("settings_hub_row_appearance_title", selectedLang)
                                                SettingsStudioTab.LANGUAGE -> Translations.tr("settings_hub_row_language_title", selectedLang)
                                                SettingsStudioTab.REGION_CURRENCY -> Translations.tr("settings_hub_row_region_title", selectedLang)
                                            }
                                        },
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = if (showProfessionSelectorSubScreen) {
                                            Translations.tr("hub_subtitle_select_role", selectedLang)
                                        } else {
                                            Translations.tr("settings_studio_subtitle", selectedLang)
                                        },
                                        fontSize = 11.sp,
                                        color = textSecondary,
                                        maxLines = 1
                                    )
                                }
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("btn_close_settings_modal")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = textSecondary)
                            }
                        }

                        // Sub-Screen Quick Navigation Tabs (visible in sub-views or for quick jumping)
                        if (activeStudioTab != SettingsStudioTab.MAIN_HUB) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val tabs = listOf(
                                    Triple(SettingsStudioTab.APPEARANCE, "🎨 ${Translations.tr("settings_tab_appearance", selectedLang)}", "settings_nav_appearance"),
                                    Triple(SettingsStudioTab.LANGUAGE, "🌐 ${Translations.tr("settings_tab_language", selectedLang)} (${selectedLang.label})", "settings_nav_language"),
                                    Triple(SettingsStudioTab.REGION_CURRENCY, "${selectedCountry.flag} ${Translations.tr("settings_tab_region_currency", selectedLang)} (${selectedCurrency.code})", "settings_nav_region_currency")
                                )
                                tabs.forEach { (tab, title, tag) ->
                                    val selected = activeStudioTab == tab
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selected) activeAccent else subtleSurfaceBg,
                                        border = BorderStroke(1.dp, if (selected) activeAccent else borderCol),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { activeStudioTab = tab }
                                            .testTag(tag)
                                    ) {
                                        Text(
                                            text = title,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) Color.White else textPrimary
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isDark) Color(0xFF78350F).copy(alpha = 0.45f) else Color(0xFFFEF3C7),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { showPaywallFromSettings = true }
                                        .testTag("settings_nav_subscription")
                                ) {
                                    Text(
                                        text = "👑 ${Translations.tr("settings_tab_subscription", selectedLang)}",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isDark) Color(0xFFFDE68A) else Color(0xFFB45309)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                // Bottom Floating Action Bar: Shows pending changes count with "Discard" and "Apply / Save" buttons
                Surface(
                    color = cardBg,
                    shadowElevation = 10.dp,
                    border = BorderStroke(1.dp, borderCol),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .testTag("appearance_floating_action_bar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (pendingChangesCount > 0) {
                                    "• $pendingChangesCount ${Translations.tr("appearance_pending_changes", selectedLang).removePrefix("•").trim()}"
                                } else {
                                    "✓ ${Translations.getLocalizedThemeTitle(draftTheme, selectedLang)} • ${draftAccent.hexCode}"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (pendingChangesCount > 0) activeAccent else textSecondary,
                                modifier = Modifier.testTag("appearance_pending_changes_text")
                            )
                            Text(
                                text = "${draftTextScale.defaultLabel} (${(draftTextScale.scaleFactor * 100).toInt()}%) • ${draftCornerStyle.defaultLabel} (${draftCornerStyle.cornerRadiusDp}dp)",
                                fontSize = 10.5.sp,
                                color = textSecondary
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (pendingChangesCount > 0) {
                                OutlinedButton(
                                    onClick = {
                                        draftTheme = committedTheme
                                        draftAccent = committedAccent
                                        draftTextScale = committedTextScale
                                        draftCornerStyle = committedCornerStyle
                                        sessionState.updateAppearanceStudio(
                                            context = context,
                                            newTheme = committedTheme,
                                            newAccentColor = committedAccent,
                                            newTextScale = committedTextScale,
                                            newCornerStyle = committedCornerStyle
                                        )
                                        onSelectTheme(committedTheme)
                                    },
                                    shape = previewShape,
                                    border = BorderStroke(1.dp, borderCol),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    modifier = Modifier
                                        .height(42.dp)
                                        .testTag("btn_appearance_discard")
                                ) {
                                    Text(
                                        text = Translations.tr("appearance_btn_discard", selectedLang),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    sessionState.updateAppearanceStudio(
                                        context = context,
                                        newTheme = draftTheme,
                                        newAccentColor = draftAccent,
                                        newTextScale = draftTextScale,
                                        newCornerStyle = draftCornerStyle
                                    )
                                    onSelectTheme(draftTheme)
                                    onSelectLanguage(selectedLang)
                                    onUpdateCountryAndCurrency(selectedCountry.code, selectedCurrency.code)
                                    onDismiss()
                                },
                                shape = previewShape,
                                colors = ButtonDefaults.buttonColors(containerColor = draftAccent.primaryColor),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .height(42.dp)
                                    .testTag("btn_appearance_apply")
                            ) {
                                Text(
                                    text = Translations.tr("appearance_btn_apply", selectedLang),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            if (showProfessionSelectorSubScreen) {
                val activeHubRole = (sessionState.currentRole ?: AppRole.OWNER_BARN).normalizedHubRole
                ProfessionSelectorSubScreen(
                    activeRole = activeHubRole,
                    lang = selectedLang,
                    isDark = isDark,
                    cardBg = cardBg,
                    subtleSurfaceBg = subtleSurfaceBg,
                    borderCol = borderCol,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    activeAccent = activeAccent,
                    activeAccentContainer = activeAccentContainer,
                    previewShape = previewShape,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onSelectProfession = { chosenRole ->
                        sessionState.selectedFarmForOwner = null
                        sessionState.selectedFarmForVet = null
                        sessionState.selectedFarmForWorker = null
                        sessionState.completeInitialProfessionSelection(context, chosenRole)
                        sessionState.markAuthenticated(
                            context = context,
                            role = chosenRole,
                            linkedBarns = true
                        )
                        showProfessionSelectorSubScreen = false
                    }
                )
            } else {
                AnimatedContent(
                    targetState = activeStudioTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    label = "SettingsStudioTabTransition"
                ) { currentTab ->
                    when (currentTab) {
                        SettingsStudioTab.MAIN_HUB -> {
                            // WhatsApp-Style Clean Vertical List of Full-Width Horizontal Rows
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 18.dp, vertical = 14.dp)
                                    .testTag("whatsapp_settings_main_hub"),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                val activeHubRole = (sessionState.currentRole ?: AppRole.OWNER_BARN).normalizedHubRole
                                val roleOptions = listOf(
                                    AppRole.OWNER_BARN to Translations.tr("role_badge_owner_barn", selectedLang),
                                    AppRole.VET_CLINIC to Translations.tr("role_badge_vet_clinic", selectedLang),
                                    AppRole.WORKER to Translations.tr("role_badge_worker", selectedLang),
                                    AppRole.FEED_SUPPLIER to Translations.tr("role_badge_feed_supplier", selectedLang),
                                    AppRole.CHICK_HATCHERY to Translations.tr("role_badge_chick_hatchery", selectedLang),
                                    AppRole.WHOLESALE_TRADER to Translations.tr("role_badge_wholesale_trader", selectedLang),
                                    AppRole.VET_IMPORTER to Translations.tr("role_badge_vet_importer", selectedLang)
                                )
                                val activeRoleEmoji = when (activeHubRole) {
                                    AppRole.VET_CLINIC -> "🩺"
                                    AppRole.WORKER -> "👷"
                                    AppRole.FEED_SUPPLIER -> "🌾"
                                    AppRole.CHICK_HATCHERY -> "🐣"
                                    AppRole.WHOLESALE_TRADER -> "🚚"
                                    AppRole.VET_IMPORTER -> "🧬"
                                    else -> "👨‍🌾"
                                }
                                val activeRoleTitle = roleOptions.firstOrNull { it.first == activeHubRole }?.second
                                    ?: Translations.tr("role_owner", selectedLang)

                                // 1) Top Profile Header Card: Avatar, Active Profession/Role, Email, and quick QR Invite icon button
                                Surface(
                                    shape = previewShape,
                                    color = cardBg,
                                    border = BorderStroke(1.dp, borderCol),
                                    shadowElevation = 1.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("settings_profile_header_card")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(52.dp)
                                                    .clip(CircleShape)
                                                    .background(activeAccentContainer)
                                                    .border(2.dp, activeAccent, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(activeRoleEmoji, fontSize = 24.sp)
                                            }

                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text(
                                                    text = activeRoleTitle,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 14.5.sp,
                                                    color = textPrimary,
                                                    maxLines = 1
                                                )
                                                val activeProfileIdentifier = remember(selectedLang) {
                                                    try {
                                                        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.let { u ->
                                                            u.email?.takeIf { it.isNotBlank() }
                                                                ?: u.phoneNumber?.takeIf { it.isNotBlank() }
                                                        }
                                                    } catch (_: Exception) {
                                                        null
                                                    } ?: Translations.tr("account_local_profile", selectedLang)
                                                }
                                                Text(
                                                    text = "$activeProfileIdentifier • ${selectedCountry.flag} ${selectedCurrency.code}",
                                                    fontSize = 11.5.sp,
                                                    color = textSecondary,
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = activeAccentContainer,
                                            border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.5f)),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { showQrPreviewBanner = !showQrPreviewBanner }
                                                .testTag("btn_settings_qr_invite")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.QrCode2,
                                                    contentDescription = "QR Invite",
                                                    tint = activeAccent,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "QR",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = activeAccent
                                                )
                                            }
                                        }
                                    }
                                }

                                if (showQrPreviewBanner) {
                                    Surface(
                                        shape = previewShape,
                                        color = activeAccentContainer,
                                        border = BorderStroke(1.dp, activeAccent),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "📲 AGRITECH_JOIN|farmId=${sessionState.activeFarmId.ifBlank { "farm_1" }}|role=WORKER",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textPrimary,
                                                modifier = Modifier.weight(1f)
                                            )
                                            TextButton(onClick = { showQrPreviewBanner = false }) {
                                                Text("✓", color = activeAccent, fontWeight = FontWeight.ExtraBold)
                                            }
                                        }
                                    }
                                }

                                // 2) Direct Dark/Light Mode Toggle Row with animated Sun ☀️ / Moon 🌙 icon
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(previewShape)
                                        .clickable {
                                            val toggled = if (draftTheme.isDark) AppThemeOption.ICE_BLUE else AppThemeOption.NIGHT_MODE
                                            draftTheme = toggled
                                            sessionState.updateTheme(context, toggled)
                                            onSelectTheme(toggled)
                                        }
                                        .testTag("card_toggle_dark_light_mode"),
                                    shape = previewShape,
                                    colors = CardDefaults.cardColors(containerColor = cardBg),
                                    border = BorderStroke(1.5.dp, activeAccent.copy(alpha = 0.55f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            AnimatedContent(
                                                targetState = isDark,
                                                transitionSpec = {
                                                    fadeIn(tween(220)) togetherWith fadeOut(tween(180))
                                                },
                                                label = "HubSunMoonIconTransition"
                                            ) { darkActive ->
                                                Surface(
                                                    shape = CircleShape,
                                                    color = if (darkActive) Color(0xFF312E81) else Color(0xFFFEF3C7),
                                                    modifier = Modifier.size(44.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = if (darkActive) "🌙" else "☀️",
                                                            fontSize = 22.sp
                                                        )
                                                    }
                                                }
                                            }

                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text(
                                                    text = if (isDark) {
                                                        Translations.tr("appearance_dark_mode_title", selectedLang)
                                                    } else {
                                                        Translations.tr("appearance_light_mode_title", selectedLang)
                                                    },
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 14.5.sp,
                                                    color = textPrimary
                                                )
                                                Text(
                                                    text = if (isDark) {
                                                        Translations.tr("appearance_dark_mode_subtitle", selectedLang)
                                                    } else {
                                                        Translations.tr("appearance_light_mode_subtitle", selectedLang)
                                                    },
                                                    fontSize = 11.5.sp,
                                                    color = textSecondary
                                                )
                                            }
                                        }

                                        Switch(
                                            checked = isDark,
                                            onCheckedChange = { checked ->
                                                val nextTheme = if (checked) AppThemeOption.NIGHT_MODE else AppThemeOption.ICE_BLUE
                                                draftTheme = nextTheme
                                                sessionState.updateTheme(context, nextTheme)
                                                onSelectTheme(nextTheme)
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = activeAccent
                                            )
                                        )
                                    }
                                }

                                // 3) WhatsApp-Style Full-Width Horizontal Navigation Rows Card
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = previewShape,
                                    colors = CardDefaults.cardColors(containerColor = cardBg),
                                    border = BorderStroke(1.dp, borderCol),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        // Row 0: 💼 الصفة المهنية ونوع الحساب (Clean WhatsApp-style horizontal row)
                                        WhatsAppSettingsHubRow(
                                            iconEmoji = "💼",
                                            iconBg = activeAccentContainer,
                                            title = Translations.tr("settings_hub_row_profession_title", selectedLang),
                                            subtitle = activeRoleTitle,
                                            textPrimary = textPrimary,
                                            textSecondary = textSecondary,
                                            accentColor = activeAccent,
                                            testTag = "settings_row_profession",
                                            secondaryTestTag = "settings_nav_profession",
                                            onClick = { showProfessionSelectorSubScreen = true }
                                        )

                                        HorizontalDivider(color = borderCol, thickness = 0.8.dp, modifier = Modifier.padding(horizontal = 16.dp))

                                        // Row 1: 🎨 الشكل والمظهر (Appearance & Theme)
                                        val textScaleLabelKey = when (draftTextScale) {
                                            AppTextScaleOption.SMALL -> "text_size_small"
                                            AppTextScaleOption.DEFAULT -> "text_size_default"
                                            AppTextScaleOption.LARGE -> "text_size_large"
                                            AppTextScaleOption.EXTRA_LARGE -> "text_size_extra_large"
                                        }
                                        val cornerStyleLabelKey = when (draftCornerStyle) {
                                            AppCornerStyleOption.ROUNDED -> "corner_style_rounded"
                                            AppCornerStyleOption.SOFT -> "corner_style_soft"
                                            AppCornerStyleOption.CRISP -> "corner_style_crisp"
                                        }
                                        WhatsAppSettingsHubRow(
                                            iconEmoji = "🎨",
                                            iconBg = activeAccentContainer,
                                            title = Translations.tr("settings_hub_row_appearance_title", selectedLang),
                                            subtitle = "${draftAccent.hexCode} • ${Translations.tr(textScaleLabelKey, selectedLang)} • ${Translations.tr(cornerStyleLabelKey, selectedLang)}",
                                            textPrimary = textPrimary,
                                            textSecondary = textSecondary,
                                            accentColor = activeAccent,
                                            testTag = "settings_row_appearance",
                                            secondaryTestTag = "settings_nav_appearance",
                                            onClick = { activeStudioTab = SettingsStudioTab.APPEARANCE }
                                        )

                                        HorizontalDivider(color = borderCol, thickness = 0.8.dp, modifier = Modifier.padding(horizontal = 16.dp))

                                        // Row 2: 🌐 لغة التطبيق (App Language)
                                        WhatsAppSettingsHubRow(
                                            iconEmoji = "🌐",
                                            iconBg = if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.4f) else Color(0xFFDBEAFE),
                                            title = Translations.tr("settings_hub_row_language_title", selectedLang),
                                            subtitle = "${selectedLang.label} • ${selectedLang.greeting}",
                                            textPrimary = textPrimary,
                                            textSecondary = textSecondary,
                                            accentColor = activeAccent,
                                            testTag = "settings_row_language",
                                            secondaryTestTag = "settings_nav_language",
                                            onClick = { activeStudioTab = SettingsStudioTab.LANGUAGE }
                                        )

                                        HorizontalDivider(color = borderCol, thickness = 0.8.dp, modifier = Modifier.padding(horizontal = 16.dp))

                                        // Row 3: 💱 الدولة والعملة المعتمدة (Country & Currency)
                                        WhatsAppSettingsHubRow(
                                            iconEmoji = "💱",
                                            iconBg = if (isDark) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFFD1FAE5),
                                            title = Translations.tr("settings_hub_row_region_title", selectedLang),
                                            subtitle = "${selectedCountry.flag} ${selectedCountry.localizedName(selectedLang)} • ${selectedCurrency.code} (${selectedCurrency.localizedName(selectedLang)})",
                                            textPrimary = textPrimary,
                                            textSecondary = textSecondary,
                                            accentColor = activeAccent,
                                            testTag = "settings_row_region_currency",
                                            secondaryTestTag = "settings_nav_region_currency",
                                            onClick = { activeStudioTab = SettingsStudioTab.REGION_CURRENCY }
                                        )

                                        HorizontalDivider(color = borderCol, thickness = 0.8.dp, modifier = Modifier.padding(horizontal = 16.dp))

                                        // Row 4: 💎 باقة الاشتراك (SaaS Pro - $4.99 / 2 Months)
                                        WhatsAppSettingsHubRow(
                                            iconEmoji = "💎",
                                            iconBg = if (isDark) Color(0xFF78350F).copy(alpha = 0.5f) else Color(0xFFFEF3C7),
                                            title = Translations.tr("settings_hub_row_subscription_title", selectedLang),
                                            subtitle = Translations.tr("paywall_price_badge", selectedLang),
                                            textPrimary = textPrimary,
                                            textSecondary = if (isDark) Color(0xFFFDE68A) else Color(0xFFB45309),
                                            accentColor = Color(0xFFF59E0B),
                                            testTag = "settings_row_subscription",
                                            secondaryTestTag = "settings_nav_subscription",
                                            onClick = { showPaywallFromSettings = true }
                                        )
                                    }
                                }
                            }
                        }

                    SettingsStudioTab.APPEARANCE -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // b) Animated Light/Dark Mode Card (Sun ☀️ / Moon 🌙 transition)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(previewShape)
                                    .clickable {
                                        val toggled = if (draftTheme.isDark) AppThemeOption.ICE_BLUE else AppThemeOption.NIGHT_MODE
                                        draftTheme = toggled
                                        sessionState.updateTheme(context, toggled)
                                        onSelectTheme(toggled)
                                    }
                                    .testTag("card_toggle_dark_light_mode"),
                                shape = previewShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) Color(0xFF1E293B) else Color.White
                                ),
                                border = BorderStroke(1.5.dp, activeAccent.copy(alpha = 0.55f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        AnimatedContent(
                                            targetState = isDark,
                                            transitionSpec = {
                                                fadeIn(tween(220)) togetherWith fadeOut(tween(180))
                                            },
                                            label = "SunMoonIconTransition"
                                        ) { darkActive ->
                                            Surface(
                                                shape = CircleShape,
                                                color = if (darkActive) Color(0xFF312E81) else Color(0xFFFEF3C7),
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = if (darkActive) "🌙" else "☀️",
                                                        fontSize = 24.sp
                                                    )
                                                }
                                            }
                                        }

                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = if (isDark) {
                                                    Translations.tr("appearance_dark_mode_title", selectedLang)
                                                } else {
                                                    Translations.tr("appearance_light_mode_title", selectedLang)
                                                },
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp,
                                                color = textPrimary
                                            )
                                            Text(
                                                text = if (isDark) {
                                                    Translations.tr("appearance_dark_mode_subtitle", selectedLang)
                                                } else {
                                                    Translations.tr("appearance_light_mode_subtitle", selectedLang)
                                                },
                                                fontSize = 12.sp,
                                                color = textSecondary
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = isDark,
                                        onCheckedChange = { checked ->
                                            val nextTheme = if (checked) AppThemeOption.NIGHT_MODE else AppThemeOption.ICE_BLUE
                                            draftTheme = nextTheme
                                            sessionState.updateTheme(context, nextTheme)
                                            onSelectTheme(nextTheme)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = activeAccent
                                        )
                                    )
                                }
                            }

                            // c) THEME COLOR Selector (6 Circular Swatches with Scale Animation & Checkmark ✓)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = previewShape,
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = BorderStroke(1.dp, borderCol)
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = Translations.tr("appearance_theme_color_label", selectedLang),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.5.sp,
                                            color = textSecondary
                                        )
                                        Text(
                                            text = draftAccent.hexCode,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = activeAccent
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AppAccentColorOption.values().forEach { swatch ->
                                            val isSelected = draftAccent == swatch
                                            val animatedScale by animateFloatAsState(
                                                targetValue = if (isSelected) 1.15f else 1.0f,
                                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                                label = "SwatchScale_${swatch.name}"
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .scale(animatedScale)
                                                    .clip(CircleShape)
                                                    .background(swatch.primaryColor)
                                                    .border(
                                                        width = if (isSelected) 3.dp else 1.dp,
                                                        color = if (isSelected) textPrimary else Color.White.copy(alpha = 0.4f),
                                                        shape = CircleShape
                                                    )
                                                    .clickable {
                                                        draftAccent = swatch
                                                        sessionState.updateAppearanceStudio(
                                                            context = context,
                                                            newTheme = draftTheme,
                                                            newAccentColor = swatch,
                                                            newTextScale = draftTextScale,
                                                            newCornerStyle = draftCornerStyle
                                                        )
                                                    }
                                                    .testTag("swatch_theme_color_${swatch.name}"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    Text(
                                                        text = "✓",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 16.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // d) TEXT SIZE Selector (`Aa`: Small, Default, Large, Extra Large)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = previewShape,
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = BorderStroke(1.dp, borderCol)
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = Translations.tr("appearance_text_size_label", selectedLang),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.5.sp,
                                        color = textSecondary
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        AppTextScaleOption.values().forEach { scaleOpt ->
                                            val isSelected = draftTextScale == scaleOpt
                                            val labelKey = when (scaleOpt) {
                                                AppTextScaleOption.SMALL -> "text_size_small"
                                                AppTextScaleOption.DEFAULT -> "text_size_default"
                                                AppTextScaleOption.LARGE -> "text_size_large"
                                                AppTextScaleOption.EXTRA_LARGE -> "text_size_extra_large"
                                            }
                                            Surface(
                                                shape = previewShape,
                                                color = if (isSelected) activeAccentContainer else subtleSurfaceBg,
                                                border = BorderStroke(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) activeAccent else borderCol
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(previewShape)
                                                    .clickable {
                                                        draftTextScale = scaleOpt
                                                        sessionState.updateAppearanceStudio(
                                                            context = context,
                                                            newTheme = draftTheme,
                                                            newAccentColor = draftAccent,
                                                            newTextScale = scaleOpt,
                                                            newCornerStyle = draftCornerStyle
                                                        )
                                                    }
                                                    .testTag("card_text_scale_${scaleOpt.name}")
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = "Aa",
                                                        fontSize = scaleOpt.previewSp.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = if (isSelected) activeAccent else textPrimary
                                                    )
                                                    Text(
                                                        text = Translations.tr(labelKey, selectedLang),
                                                        fontSize = 10.5.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) activeAccent else textSecondary,
                                                        textAlign = TextAlign.Center,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // e) UI STYLE (Corner Radius Selector: Rounded 20.dp, Soft 12.dp, Crisp 4.dp)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = previewShape,
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = BorderStroke(1.dp, borderCol)
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = Translations.tr("appearance_ui_style_label", selectedLang),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.5.sp,
                                        color = textSecondary
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        AppCornerStyleOption.values().forEach { cornerOpt ->
                                            val isSelected = draftCornerStyle == cornerOpt
                                            val labelKey = when (cornerOpt) {
                                                AppCornerStyleOption.ROUNDED -> "corner_style_rounded"
                                                AppCornerStyleOption.SOFT -> "corner_style_soft"
                                                AppCornerStyleOption.CRISP -> "corner_style_crisp"
                                            }
                                            val sampleShape = RoundedCornerShape(cornerOpt.cornerRadiusDp.dp)
                                            Surface(
                                                shape = sampleShape,
                                                color = if (isSelected) activeAccentContainer else subtleSurfaceBg,
                                                border = BorderStroke(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) activeAccent else borderCol
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(sampleShape)
                                                    .clickable {
                                                        draftCornerStyle = cornerOpt
                                                        sessionState.updateAppearanceStudio(
                                                            context = context,
                                                            newTheme = draftTheme,
                                                            newAccentColor = draftAccent,
                                                            newTextScale = draftTextScale,
                                                            newCornerStyle = cornerOpt
                                                        )
                                                    }
                                                    .testTag("card_corner_style_${cornerOpt.name}")
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    // Visual Corner Preview Box
                                                    Box(
                                                        modifier = Modifier
                                                            .size(width = 48.dp, height = 28.dp)
                                                            .clip(sampleShape)
                                                            .background(if (isSelected) activeAccent.copy(alpha = 0.2f) else cardBg)
                                                            .border(2.dp, if (isSelected) activeAccent else textSecondary, sampleShape)
                                                    )
                                                    Text(
                                                        text = Translations.tr(labelKey, selectedLang),
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) activeAccent else textPrimary,
                                                        textAlign = TextAlign.Center,
                                                        maxLines = 1
                                                    )
                                                    Text(
                                                        text = "${cornerOpt.cornerRadiusDp}dp",
                                                        fontSize = 10.sp,
                                                        color = textSecondary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    SettingsStudioTab.LANGUAGE -> {
                        // 3. Animated Greeting Language Screen & Global Safe-Fallback List
                        AnimatedLanguageSelectorScreen(
                            selectedLanguage = selectedLang,
                            isDark = isDark,
                            cardBg = cardBg,
                            subtleSurfaceBg = subtleSurfaceBg,
                            borderCol = borderCol,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            activeAccent = activeAccent,
                            activeAccentContainer = activeAccentContainer,
                            previewShape = previewShape,
                            onSelectLanguage = { newLang ->
                                selectedLang = newLang
                                onSelectLanguage(newLang)
                            }
                        )
                    }

                    SettingsStudioTab.REGION_CURRENCY -> {
                        // 4. Searchable Country & Real Currency Selector Sub-Screen
                        CountryAndCurrencySelectorScreen(
                            selectedCountry = selectedCountry,
                            selectedCurrency = selectedCurrency,
                            lang = selectedLang,
                            isDark = isDark,
                            cardBg = cardBg,
                            subtleSurfaceBg = subtleSurfaceBg,
                            borderCol = borderCol,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            activeAccent = activeAccent,
                            activeAccentContainer = activeAccentContainer,
                            previewShape = previewShape,
                            onSelectCountryAndCurrency = { country, currency ->
                                selectedCountry = country
                                selectedCurrency = currency
                                onUpdateCountryAndCurrency(country.code, currency.code)
                            }
                        )
                    }
                }
            }
            }
        }
    }

    if (showCurrencyBottomSheet) {
        CurrencySelectorBottomSheet(
            selectedCurrencyCode = selectedCurrency.code,
            lang = selectedLang,
            currentTheme = draftTheme,
            onSelectCurrency = { chosen ->
                selectedCurrency = chosen
                onUpdateCountryAndCurrency(selectedCountry.code, chosen.code)
                showCurrencyBottomSheet = false
            },
            onDismiss = { showCurrencyBottomSheet = false }
        )
    }

    if (showPaywallFromSettings) {
        SubscriptionPaywallBottomSheet(
            currentTheme = draftTheme,
            lang = selectedLang,
            existingBarnsCount = AgritechMockDb.farms.size.coerceAtLeast(1),
            onDismiss = { showPaywallFromSettings = false },
            onSubscriptionSuccess = { _ ->
                val currentUnlocked = SaasPreferencesManager.getUnlockedAdditionalBarns(context)
                SaasPreferencesManager.saveUnlockedAdditionalBarns(context, currentUnlocked + 1)
                showPaywallFromSettings = false
            }
        )
    }
}

@Composable
fun WhatsAppSettingsHubRow(
    iconEmoji: String,
    iconBg: Color,
    title: String,
    subtitle: String,
    textPrimary: Color,
    textSecondary: Color,
    accentColor: Color,
    testTag: String,
    secondaryTestTag: String = testTag,
    onClick: () -> Unit
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(44.dp)
                .testTag(secondaryTestTag),
            shape = CircleShape,
            color = iconBg
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = iconEmoji, fontSize = 19.sp)
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                color = textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = if (isRtl) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ============================================================================
// 3. ANIMATED GREETING LANGUAGE HUB (14 GLOBAL LANGUAGES, NO COUNTRY FLAGS)
// ============================================================================

@Composable
fun AnimatedLanguageSelectorScreen(
    selectedLanguage: AppLanguage,
    isDark: Boolean,
    cardBg: Color,
    subtleSurfaceBg: Color,
    borderCol: Color,
    textPrimary: Color,
    textSecondary: Color,
    activeAccent: Color,
    activeAccentContainer: Color,
    previewShape: RoundedCornerShape,
    onSelectLanguage: (AppLanguage) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .testTag("animated_language_selector_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Sleek Top Hero Banner with AnimatedContent Greeting
        Surface(
            shape = previewShape,
            color = activeAccentContainer,
            border = BorderStroke(1.5.dp, activeAccent.copy(alpha = 0.65f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("language_greeting_hero_banner")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AnimatedContent(
                    targetState = selectedLanguage,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(240)) togetherWith fadeOut(animationSpec = tween(180))
                    },
                    label = "HeroGreetingTextTransition"
                ) { targetLang ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = targetLang.greeting,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = activeAccent,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("language_hero_greeting_text")
                        )
                        Text(
                            text = targetLang.greetingSubtitle,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = textPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Scrollable Global Language List (14 Languages, Native Script + Subtitle + Glowing Checkmark ✓, ZERO Flags)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("global_languages_lazy_column"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(SaasGlobalCatalog.onboardingLanguages, key = { it.code }) { langOption ->
                val isSelected = langOption == selectedLanguage
                Surface(
                    shape = previewShape,
                    color = if (isSelected) activeAccentContainer else cardBg,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) activeAccent else borderCol
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(previewShape)
                        .clickable { onSelectLanguage(langOption) }
                        .testTag("global_lang_row_${langOption.code}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = langOption.label,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = textPrimary
                            )
                            Text(
                                text = "${langOption.subtitleLabel} • ${langOption.greeting}",
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(activeAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✓",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, borderCol, CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 4. SEARCHABLE COUNTRY & REAL CURRENCY SELECTOR SUB-SCREEN
// ============================================================================

@Composable
fun CountryAndCurrencySelectorScreen(
    selectedCountry: SaasCountryOption,
    selectedCurrency: SaasCurrencyOption,
    lang: AppLanguage,
    isDark: Boolean,
    cardBg: Color,
    subtleSurfaceBg: Color,
    borderCol: Color,
    textPrimary: Color,
    textSecondary: Color,
    activeAccent: Color,
    activeAccentContainer: Color,
    previewShape: RoundedCornerShape,
    onSelectCountryAndCurrency: (SaasCountryOption, SaasCurrencyOption) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredCountries = remember(searchQuery, lang) {
        val q = searchQuery.trim()
        if (q.isEmpty()) {
            SaasGlobalCatalog.countries
        } else {
            SaasGlobalCatalog.countries.filter { c ->
                c.code.contains(q, ignoreCase = true) ||
                    c.defaultCurrencyCode.contains(q, ignoreCase = true) ||
                    c.localizedName(lang).contains(q, ignoreCase = true) ||
                    c.nameEn.contains(q, ignoreCase = true) ||
                    c.nameAr.contains(q, ignoreCase = true)
            }
        }
    }

    val filteredCurrencies = remember(searchQuery, lang) {
        val q = searchQuery.trim()
        if (q.isEmpty()) {
            SaasGlobalCatalog.currencies
        } else {
            SaasGlobalCatalog.currencies.filter { curr ->
                curr.code.contains(q, ignoreCase = true) ||
                    curr.symbol.contains(q, ignoreCase = true) ||
                    curr.localizedName(lang).contains(q, ignoreCase = true) ||
                    curr.nameEn.contains(q, ignoreCase = true) ||
                    curr.nameAr.contains(q, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .testTag("country_currency_selector_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    Translations.tr("onboarding_currency_search_hint", lang),
                    fontSize = 12.5.sp,
                    color = textSecondary
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = textSecondary)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = null, tint = textSecondary)
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("region_currency_search_input"),
            shape = previewShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = activeAccent,
                unfocusedBorderColor = borderCol,
                focusedTextColor = textPrimary,
                unfocusedTextColor = textPrimary,
                focusedContainerColor = cardBg,
                unfocusedContainerColor = cardBg
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = Translations.tr("onboarding_country_label", lang),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textSecondary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(filteredCountries, key = { "country_${it.code}" }) { country ->
                val isSelected = country.code == selectedCountry.code
                val matchingCurrency = SaasGlobalCatalog.currencies.firstOrNull {
                    it.code == country.defaultCurrencyCode
                } ?: selectedCurrency
                Surface(
                    shape = previewShape,
                    color = if (isSelected) activeAccentContainer else cardBg,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) activeAccent else borderCol
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(previewShape)
                        .clickable {
                            onSelectCountryAndCurrency(country, matchingCurrency)
                        }
                        .testTag("region_country_item_${country.code}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(country.flag, fontSize = 22.sp)
                            Column {
                                Text(
                                    text = "${country.localizedName(lang)} (${country.code})",
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    color = textPrimary
                                )
                                Text(
                                    text = "${matchingCurrency.code} - ${matchingCurrency.localizedName(lang)} (${matchingCurrency.symbol})",
                                    fontSize = 11.5.sp,
                                    color = textSecondary
                                )
                            }
                        }

                        if (isSelected) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = activeAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = Translations.tr("onboarding_currency_label", lang),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textSecondary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(filteredCurrencies, key = { "curr_${it.code}" }) { curr ->
                val isSelected = curr.code == selectedCurrency.code
                Surface(
                    shape = previewShape,
                    color = if (isSelected) activeAccentContainer else cardBg,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) activeAccent else borderCol
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(previewShape)
                        .clickable {
                            onSelectCountryAndCurrency(selectedCountry, curr)
                        }
                        .testTag("region_currency_item_${curr.code}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(curr.flag, fontSize = 20.sp)
                            Column {
                                Text(
                                    text = "${curr.code} - ${curr.localizedName(lang)}",
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = textPrimary
                                )
                                Text(
                                    text = "${curr.code} (${curr.symbol})",
                                    fontSize = 11.5.sp,
                                    color = textSecondary
                                )
                            }
                        }
                        if (isSelected) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = activeAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// SLEEK SEARCHABLE CURRENCY MODAL BOTTOM SHEET (DARK / LIGHT PREMIUM UI)
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelectorBottomSheet(
    selectedCurrencyCode: String,
    lang: AppLanguage,
    currentTheme: AppThemeOption,
    onSelectCurrency: (SaasCurrencyOption) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    var isDarkSheet by remember { mutableStateOf(currentTheme.isDark) }

    val sheetBg = if (isDarkSheet) Color(0xFF0F172A) else Color.White
    val cardBg = if (isDarkSheet) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val textPrimary = if (isDarkSheet) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val textSecondary = if (isDarkSheet) Color(0xFFCBD5E1) else Color(0xFF64748B)
    val borderColor = if (isDarkSheet) Color(0xFF334155) else Color(0xFFE2E8F0)

    val filteredCurrencies = remember(searchQuery, lang) {
        val q = searchQuery.trim()
        if (q.isEmpty()) {
            SaasGlobalCatalog.currencies
        } else {
            SaasGlobalCatalog.currencies.filter { curr ->
                curr.code.contains(q, ignoreCase = true) ||
                    curr.symbol.contains(q, ignoreCase = true) ||
                    curr.localizedName(lang).contains(q, ignoreCase = true) ||
                    curr.nameEn.contains(q, ignoreCase = true) ||
                    curr.nameAr.contains(q, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        modifier = Modifier.testTag("currency_selector_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sheet Header + Dark/Light Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Translations.tr("onboarding_currency_label", lang),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = textPrimary,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { isDarkSheet = !isDarkSheet }
                        .testTag("btn_toggle_currency_panel_theme")
                ) {
                    Text(
                        text = if (isDarkSheet) "🌙 Dark" else "☀️ Light",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                }
            }

            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        Translations.tr("onboarding_currency_search_hint", lang),
                        fontSize = 12.5.sp,
                        color = textSecondary
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = textSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = textSecondary)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_currency_search_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = borderColor,
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary,
                    focusedContainerColor = cardBg,
                    unfocusedContainerColor = cardBg
                )
            )

            // Compact Scrollable Currency List inside BottomSheet
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredCurrencies, key = { it.code }) { curr ->
                    val isSelected = curr.code == selectedCurrencyCode
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) {
                            if (isDarkSheet) Color(0xFF064E3B) else LightGreenContainer
                        } else {
                            cardBg
                        },
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) Color(0xFF10B981) else borderColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectCurrency(curr) }
                            .testTag("onboarding_currency_item_${curr.code}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(curr.flag, fontSize = 20.sp)
                                Column {
                                    Text(
                                        text = "${curr.code} - ${curr.localizedName(lang)}",
                                        fontSize = 13.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "${curr.code} (${curr.symbol})",
                                        fontSize = 11.5.sp,
                                        color = textSecondary
                                    )
                                }
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 5. FREEMIUM SUBSCRIPTION PAYWALL BOTTOM SHEET ($4.99 / 2 MONTHS FOR 2ND+ BARN)
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionPaywallBottomSheet(
    currentTheme: AppThemeOption,
    lang: AppLanguage,
    existingBarnsCount: Int,
    onDismiss: () -> Unit,
    onSubscriptionSuccess: (paymentProvider: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var isProcessingPayment by remember { mutableStateOf(false) }
    var activePaymentProvider by remember { mutableStateOf<String?>(null) }

    val isDark = currentTheme.isDark
    val primaryText = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val secondaryText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    val accentEmerald = if (isDark) Color(0xFF34D399) else TextForestGreen

    ModalBottomSheet(
        onDismissRequest = { if (!isProcessingPayment) onDismiss() },
        sheetState = sheetState,
        containerColor = currentTheme.cardColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("subscription_paywall_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pro SaaS Badge & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDark) Color(0xFF78350F).copy(alpha = 0.45f) else Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B))
                ) {
                    Text(
                        text = Translations.tr("paywall_badge", lang),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color(0xFFFDE68A) else Color(0xFFB45309)
                    )
                }

                IconButton(
                    onClick = { if (!isProcessingPayment) onDismiss() },
                    modifier = Modifier.testTag("btn_close_paywall_sheet")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = secondaryText)
                }
            }

            // Main Offer Banner Card ("إدارة احترافية للعنبر الإضافي - 4.99$ / لشهرين (دورة إنتاج كاملة)")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🏠", fontSize = 22.sp)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Translations.tr("paywall_offer_title", lang),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.testTag("paywall_offer_title_text")
                            )
                            Text(
                                text = "Freemium Tier: 1st Barn 100% FREE ($existingBarnsCount Active) • $4.99 / 2 Months",
                                fontSize = 11.5.sp,
                                color = Color(0xFF6EE7B7)
                            )
                        }
                    }

                    Text(
                        text = Translations.tr("paywall_subtitle", lang),
                        fontSize = 12.5.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF064E3B),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("paywall_no_annual_lock_badge")
                    ) {
                        Text(
                            text = Translations.tr("paywall_no_annual_lock", lang),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6EE7B7)
                        )
                    }
                }
            }

            // Included Pro Features List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("paywall_feat_1", "paywall_feat_2", "paywall_feat_3").forEach { featKey ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = null,
                            tint = accentEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = Translations.tr(featKey, lang),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = primaryText
                        )
                    }
                }
            }

            HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFBEB),
                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.55f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("paywall_sandbox_banner")
            ) {
                Text(
                    text = Translations.tr("paywall_sandbox_notice", lang),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            // Payment Buttons: Google Play & Stripe ($4.99 / 2 Months)
            Button(
                onClick = {
                    if (!isProcessingPayment) {
                        isProcessingPayment = true
                        activePaymentProvider = "Google Play"
                        scope.launch {
                            delay(250)
                            isProcessingPayment = false
                            onSubscriptionSuccess("Google Play")
                        }
                    }
                },
                enabled = !isProcessingPayment,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .testTag("btn_subscribe_google_play"),
                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TextForestGreen)
            ) {
                if (isProcessingPayment && activePaymentProvider == "Google Play") {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Icon(Icons.Default.Shop, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = Translations.tr("paywall_btn_google_play", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            OutlinedButton(
                onClick = {
                    if (!isProcessingPayment) {
                        isProcessingPayment = true
                        activePaymentProvider = "Stripe"
                        scope.launch {
                            delay(250)
                            isProcessingPayment = false
                            onSubscriptionSuccess("Stripe")
                        }
                    }
                },
                enabled = !isProcessingPayment,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .testTag("btn_subscribe_stripe"),
                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, Color(0xFF635BFF)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF635BFF))
            ) {
                if (isProcessingPayment && activePaymentProvider == "Stripe") {
                    CircularProgressIndicator(
                        color = Color(0xFF635BFF),
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color(0xFF635BFF))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = Translations.tr("paywall_btn_stripe", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isDark) Color(0xFFA5B4FC) else Color(0xFF635BFF)
                )
            }
        }
    }
}

// ============================================================================
// 5. LIVE BARN MICRO-CLIMATE ANIMATED BANNER & 2x2 QUICK STATS GRID
// ============================================================================

@Composable
fun LiveBarnClimateBanner(
    temperatureC: Double,
    ammoniaPpm: Double,
    ventilationStatus: String,
    cycleDay: Int,
    lang: AppLanguage,
    currentTheme: AppThemeOption,
    modifier: Modifier = Modifier
) {
    val appearance = LocalAppAppearanceState.current
    val isDark = appearance.isDark
    val cornerShape = RoundedCornerShape(appearance.cornerRadiusDp.dp)

    // Ideal Ross 308 temperature band shifts by cycle age; evaluate micro-climate status:
    // Optimal (Emerald), Warning (Amber), Alert (Red)
    val isAlert = temperatureC > 34.5 || temperatureC < 17.0 || ammoniaPpm >= 25.0
    val isWarning = !isAlert && (temperatureC > 32.5 || temperatureC < 19.5 || ammoniaPpm >= 16.0)

    val statusColor = when {
        isAlert -> Color(0xFFEF4444)
        isWarning -> Color(0xFFF59E0B)
        else -> appearance.accentColor
    }

    val statusBadgeBg = when {
        isAlert -> if (isDark) Color(0xFF450A0A) else Color(0xFFFEE2E2)
        isWarning -> if (isDark) Color(0xFF451A03) else Color(0xFFFEF3C7)
        else -> appearance.accentContainerColor
    }

    val statusTextKey = when {
        isAlert -> "climate_status_alert"
        isWarning -> "climate_status_warning"
        else -> "climate_status_optimal"
    }

    var fanBoost by remember { mutableStateOf(false) }
    val fanRotation by animateFloatAsState(
        targetValue = if (fanBoost) 360f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "climate_fan_rotation"
    )
    LaunchedEffect(temperatureC, ammoniaPpm) {
        fanBoost = true
    }

    val cardBg = if (isDark) Color(0xFF1E293B) else currentTheme.cardColor
    val textPrimary = if (isDark) Color.White else currentTheme.headingColor
    val textSecondary = if (isDark) Color(0xFFCBD5E1) else Color(0xFF64748B)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_barn_climate_banner"),
        shape = cornerShape,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.2.dp, statusColor.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Subtle animated airflow stream lines on Canvas
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .heightIn(min = 110.dp)
            ) {
                val w = size.width
                val h = size.height
                val lineColor = statusColor.copy(alpha = if (isDark) 0.14f else 0.10f)
                drawLine(
                    color = lineColor,
                    start = Offset(w * 0.08f, h * 0.28f),
                    end = Offset(w * 0.92f, h * 0.28f),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = lineColor,
                    start = Offset(w * 0.15f, h * 0.56f),
                    end = Offset(w * 0.85f, h * 0.56f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = statusColor.copy(alpha = 0.18f),
                    radius = 5.dp.toPx(),
                    center = Offset(w * 0.24f, h * 0.28f)
                )
                drawCircle(
                    color = statusColor.copy(alpha = 0.18f),
                    radius = 4.dp.toPx(),
                    center = Offset(w * 0.72f, h * 0.56f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(38.dp)
                                .clickable { fanBoost = !fanBoost },
                            shape = CircleShape,
                            color = statusBadgeBg
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Air,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .rotate(fanRotation)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = Translations.tr("climate_banner_title", lang),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${Translations.tr("day_prefix", lang)} $cycleDay • Ross 308",
                                fontSize = 11.sp,
                                color = textSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = statusBadgeBg,
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("climate_status_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(statusColor, CircleShape)
                            )
                            Text(
                                text = Translations.tr(statusTextKey, lang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = statusColor,
                                maxLines = 1
                            )
                        }
                    }
                }

                // 3 Micro-Climate Telemetry Pills: Temp (°C), Ammonia (ppm), Ventilation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClimateMetricPill(
                        emoji = "🌡️",
                        label = Translations.tr("climate_temp_label", lang),
                        value = String.format(Locale.US, "%.1f °C", temperatureC),
                        accentColor = statusColor,
                        isDark = isDark,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("climate_pill_temp")
                    )
                    ClimateMetricPill(
                        emoji = "🧪",
                        label = Translations.tr("climate_ammonia_label", lang),
                        value = String.format(Locale.US, "%.1f ppm", ammoniaPpm),
                        accentColor = if (ammoniaPpm >= 20.0) Color(0xFFEF4444) else appearance.accentColor,
                        isDark = isDark,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("climate_pill_ammonia")
                    )
                    ClimateMetricPill(
                        emoji = "🌀",
                        label = Translations.tr("climate_vent_label", lang),
                        value = Translations.getLocalizedVentilation(ventilationStatus, lang),
                        accentColor = appearance.accentColor,
                        isDark = isDark,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("climate_pill_vent")
                    )
                }
            }
        }
    }
}

@Composable
private fun ClimateMetricPill(
    emoji: String,
    label: String,
    value: String,
    accentColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val pillBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val pillBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFFCBD5E1) else Color(0xFF64748B)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = pillBg,
        border = BorderStroke(1.dp, pillBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = emoji, fontSize = 12.sp)
                Text(
                    text = label,
                    fontSize = 10.5.sp,
                    color = textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = if (accentColor == Color(0xFFEF4444)) accentColor else textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun QuickStatsGrid(
    liveBirds: Int,
    livabilityPct: Double,
    feedInventoryKg: Double,
    fcr: Double,
    estimatedWeightKg: Double,
    netBalance: Double,
    totalExpenses: Double,
    currencyCode: String,
    lang: AppLanguage,
    currentTheme: AppThemeOption,
    modifier: Modifier = Modifier
) {
    val appearance = LocalAppAppearanceState.current
    val isDark = appearance.isDark
    val cornerShape = RoundedCornerShape(appearance.cornerRadiusDp.dp)
    val cardBg = if (isDark) Color(0xFF1E293B) else currentTheme.cardColor
    val borderCol = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color.White else currentTheme.headingColor
    val textSecondary = if (isDark) Color(0xFFCBD5E1) else Color(0xFF64748B)
    val accent = appearance.accentColor
    val qShort = Translations.tr("quintal_short", lang)
    val kgUnit = Translations.tr("kg_unit", lang)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quick_stats_grid_2x2"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: (1) Active Live Birds & Livability %, (2) Remaining Feed Stock in Quintals (ق)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickStatCardItem(
                emoji = "🐥",
                title = Translations.tr("quick_stat_live_birds", lang),
                primaryValue = "${String.format(Locale.US, "%,d", liveBirds)} ${Translations.tr("birds_unit_short", lang)}",
                subtitle = "${Translations.tr("livability_label", lang)}: ${String.format(Locale.US, "%.1f%%", livabilityPct)}",
                accentColor = accent,
                cardBg = cardBg,
                borderCol = borderCol,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                shape = cornerShape,
                testTag = "quick_stat_card_birds",
                modifier = Modifier.weight(1f)
            )

            val feedQuintals = feedInventoryKg / 100.0
            val isLowFeed = feedInventoryKg < 500.0
            QuickStatCardItem(
                emoji = "🌾",
                title = Translations.tr("quick_stat_feed_stock", lang),
                primaryValue = "${Translations.formatQuintalNumber(feedQuintals)} $qShort",
                subtitle = "${String.format(Locale.US, "%,.0f", feedInventoryKg)} $kgUnit",
                accentColor = if (isLowFeed) Color(0xFFEF4444) else accent,
                cardBg = cardBg,
                borderCol = borderCol,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                shape = cornerShape,
                testTag = "quick_stat_card_feed",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: (3) Live FCR & Estimated Weight (kg), (4) Net Treasury / Cycle Expenses
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickStatCardItem(
                emoji = "⚖️",
                title = Translations.tr("quick_stat_fcr_weight", lang),
                primaryValue = "FCR ${String.format(Locale.US, "%.2f", fcr)}",
                subtitle = "${Translations.tr("badge_estimated_weight", lang)}: ${String.format(Locale.US, "%.3f", estimatedWeightKg)} $kgUnit",
                accentColor = accent,
                cardBg = cardBg,
                borderCol = borderCol,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                shape = cornerShape,
                testTag = "quick_stat_card_fcr",
                modifier = Modifier.weight(1f)
            )

            val netFormatted = String.format(Locale.US, "%,.0f", netBalance)
            val expFormatted = String.format(Locale.US, "%,.0f", totalExpenses)
            QuickStatCardItem(
                emoji = "💰",
                title = Translations.tr("quick_stat_net_treasury", lang),
                primaryValue = "$netFormatted $currencyCode",
                subtitle = "${Translations.tr("treasury_expenses", lang)}: $expFormatted $currencyCode",
                accentColor = if (netBalance >= 0) accent else Color(0xFFF59E0B),
                cardBg = cardBg,
                borderCol = borderCol,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                shape = cornerShape,
                testTag = "quick_stat_card_treasury",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickStatCardItem(
    emoji: String,
    title: String,
    primaryValue: String,
    subtitle: String,
    accentColor: Color,
    cardBg: Color,
    borderCol: Color,
    textPrimary: Color,
    textSecondary: Color,
    shape: RoundedCornerShape,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag(testTag),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderCol),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = emoji, fontSize = 14.sp)
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = primaryValue,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.5.sp,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = textPrimary.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Interactive Modal Dialog triggered by the Top Header Climate Icon (`🌡️`) in `AgritechFarmNotebookScreen`.
 * Displays the live Barn Micro-Climate (`LiveBarnClimateBanner`) and the 4 Quick Stats (`QuickStatsGrid`)
 * on demand so the Daily Ledger table remains immediately visible at the top of the screen.
 */
@Composable
fun BarnClimateAndStatsDialog(
    farmName: String,
    cycleDay: Int,
    temperatureC: Double,
    ammoniaPpm: Double,
    ventilationStatus: String,
    liveBirds: Int,
    livabilityPct: Double,
    feedInventoryKg: Double,
    fcr: Double,
    estimatedWeightKg: Double,
    netBalance: Double,
    totalExpenses: Double,
    currencyCode: String,
    lang: AppLanguage,
    currentTheme: AppThemeOption,
    onDismiss: () -> Unit
) {
    val appearance = LocalAppAppearanceState.current
    val isDark = appearance.isDark
    val dialogBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val headerCardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val borderCol = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color.White else currentTheme.headingColor
    val textSecondary = if (isDark) Color(0xFFCBD5E1) else Color(0xFF64748B)
    val accent = appearance.accentColor
    val cornerShape = RoundedCornerShape(appearance.cornerRadiusDp.dp)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 520.dp)
                .testTag("barn_climate_and_stats_dialog"),
            shape = RoundedCornerShape((appearance.cornerRadiusDp + 4).dp),
            color = dialogBg,
            border = BorderStroke(1.dp, borderCol),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Dialog Header
                Surface(
                    shape = cornerShape,
                    color = headerCardBg,
                    border = BorderStroke(1.dp, borderCol),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = appearance.accentContainerColor,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🌡️", fontSize = 18.sp)
                                }
                            }
                            Column {
                                Text(
                                    text = Translations.tr("climate_modal_title", lang),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.5.sp,
                                    color = textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$farmName • ${Translations.tr("day_prefix", lang)} $cycleDay",
                                    fontSize = 11.5.sp,
                                    color = textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("btn_close_climate_dialog")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = Translations.tr("btn_cancel", lang),
                                tint = textSecondary
                            )
                        }
                    }
                }

                // 0. Smart Barn Robot & Gas/Climate IoT Telemetry Card ("🤖 روبوت وحساسات العنبر الذكية")
                SmartBarnRobotIoTCard(
                    flockAgeDays = cycleDay,
                    liveBirds = liveBirds,
                    mortalityPct = (100.0 - livabilityPct).coerceIn(0.0, 100.0),
                    dailyFeedQuintals = 14.5,
                    temperatureC = temperatureC,
                    lang = lang,
                    currentTheme = currentTheme
                )

                // 1. Live Barn Micro-Climate Animated Banner
                LiveBarnClimateBanner(
                    temperatureC = temperatureC,
                    ammoniaPpm = ammoniaPpm,
                    ventilationStatus = ventilationStatus,
                    cycleDay = cycleDay,
                    lang = lang,
                    currentTheme = currentTheme
                )

                // 2. 2x2 Quick Stats Grid
                QuickStatsGrid(
                    liveBirds = liveBirds,
                    livabilityPct = livabilityPct,
                    feedInventoryKg = feedInventoryKg,
                    fcr = fcr,
                    estimatedWeightKg = estimatedWeightKg,
                    netBalance = netBalance,
                    totalExpenses = totalExpenses,
                    currencyCode = currencyCode,
                    lang = lang,
                    currentTheme = currentTheme
                )

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_confirm_close_climate_dialog"),
                    shape = cornerShape,
                    colors = ButtonDefaults.buttonColors(containerColor = appearance.accentOption.primaryColor)
                ) {
                    Text(
                        text = Translations.tr("btn_close", lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Apple-HIG-styled "🤖 روبوت وحساسات العنبر الذكية (Smart Barn IoT)" card:
 * - Mode toggle: `[✋ إدخال يدوي]` | `[🤖 متصل بالروبوت]`
 * - 4 compact live sensor pills:
 *   1) `🧪 الأمونيا (NH3): 12 ppm (🟢 آمن)` (tap-to-simulate alert toggle `26 ppm 🔴 خطر تنفسي`)
 *   2) `💨 الأكسجين وCO2: 1,800 ppm (🟢 جيد)`
 *   3) `🔥 ضغط الغاز: 85% (🟢 مستقر)`
 *   4) `🌡️ الحرارة والرطوبة: 21.3°C • 58%`
 * - One-tap button `"🤖 تحليل قراءات الروبوت والقطيع الآن"` that feeds live telemetry into `AiVetGeminiService`.
 */
@Composable
fun SmartBarnRobotIoTCard(
    flockAgeDays: Int,
    liveBirds: Int,
    mortalityPct: Double,
    dailyFeedQuintals: Double = 14.5,
    temperatureC: Double = 21.3,
    lang: AppLanguage,
    currentTheme: AppThemeOption,
    onAnalysisResult: ((String) -> Unit)? = null
) {
    val appearance = LocalAppAppearanceState.current
    val isDark = currentTheme.isDark
    val cardBg = if (isDark) Color(0xFF161B26) else currentTheme.cardColor
    val pillSurfaceBg = if (isDark) Color(0xFF0E121A) else Color(0xFFF8FAFC)
    val subtleBorder = if (isDark) Color.White.copy(alpha = 0.08f) else appearance.accentColor.copy(alpha = 0.22f)
    val primaryText = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val secondaryText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    val emeraldGreen = Color(0xFF10B981)
    val alertRed = Color(0xFFEF4444)
    val cornerShape = RoundedCornerShape(appearance.cornerRadiusDp.dp)
    val pillShape = RoundedCornerShape((appearance.cornerRadiusDp - 6).coerceAtLeast(6).dp)

    var isRobotConnected by remember { mutableStateOf(true) }
    var isSimulatedNh3Alert by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var inlineAiReport by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val currentNh3Ppm = if (isSimulatedNh3Alert) 26 else 12
    val telemetrySnapshot = remember(isRobotConnected, currentNh3Ppm, temperatureC) {
        SmartBarnTelemetrySnapshot(
            isRobotConnected = isRobotConnected,
            ammoniaNh3Ppm = currentNh3Ppm,
            co2Ppm = 1800,
            gasPressurePct = 85,
            temperatureC = 21.3,
            humidityPct = 58
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("smart_barn_robot_iot_card"),
        shape = cornerShape,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, subtleBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header + Mode Toggle (`[✋ إدخال يدوي]` | `[🤖 متصل بالروبوت]`)
            Text(
                text = Translations.tr("smart_barn_iot_title", lang),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.5.sp,
                color = primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = { isRobotConnected = false },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .testTag("btn_iot_mode_manual"),
                    shape = pillShape,
                    color = if (!isRobotConnected) appearance.accentOption.primaryColor else pillSurfaceBg,
                    border = BorderStroke(1.dp, if (!isRobotConnected) appearance.accentOption.primaryColor else subtleBorder)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                        Text(
                            text = Translations.tr("smart_barn_mode_manual", lang),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isRobotConnected) Color.White else secondaryText,
                            maxLines = 1
                        )
                    }
                }
                Surface(
                    onClick = { isRobotConnected = true },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .testTag("btn_iot_mode_robot"),
                    shape = pillShape,
                    color = if (isRobotConnected) appearance.accentOption.primaryColor else pillSurfaceBg,
                    border = BorderStroke(1.dp, if (isRobotConnected) appearance.accentOption.primaryColor else subtleBorder)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                        Text(
                            text = Translations.tr("smart_barn_mode_robot", lang),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRobotConnected) Color.White else secondaryText,
                            maxLines = 1
                        )
                    }
                }
            }

            // 4 Compact Live Sensor Pills (2x2 clean grid)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pill 1: 🧪 الأمونيا (NH3): 12 ppm (🟢 آمن) / 26 ppm (🔴 خطر تنفسي)
                Surface(
                    onClick = { isSimulatedNh3Alert = !isSimulatedNh3Alert },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .testTag("iot_pill_nh3_toggle"),
                    shape = pillShape,
                    color = if (isSimulatedNh3Alert) alertRed.copy(alpha = 0.14f) else pillSurfaceBg,
                    border = BorderStroke(
                        1.dp,
                        if (isSimulatedNh3Alert) alertRed else emeraldGreen.copy(alpha = 0.45f)
                    )
                ) {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (isSimulatedNh3Alert) {
                                Translations.tr("smart_barn_sensor_nh3_alert", lang)
                            } else {
                                Translations.tr("smart_barn_sensor_nh3_safe", lang)
                            },
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSimulatedNh3Alert) (if (isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C)) else primaryText,
                            maxLines = 1
                        )
                    }
                }

                // Pill 2: 💨 الأكسجين وCO2: 1,800 ppm (🟢 جيد)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .testTag("iot_pill_o2_co2"),
                    shape = pillShape,
                    color = pillSurfaceBg,
                    border = BorderStroke(1.dp, emeraldGreen.copy(alpha = 0.35f))
                ) {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = Translations.tr("smart_barn_sensor_o2_co2", lang),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText,
                            maxLines = 1
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pill 3: 🔥 ضغط الغاز: 85% (🟢 مستقر)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .testTag("iot_pill_gas_pressure"),
                    shape = pillShape,
                    color = pillSurfaceBg,
                    border = BorderStroke(1.dp, emeraldGreen.copy(alpha = 0.35f))
                ) {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = Translations.tr("smart_barn_sensor_gas", lang),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText,
                            maxLines = 1
                        )
                    }
                }

                // Pill 4: 🌡️ الحرارة والرطوبة: 21.3°C • 58%
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .testTag("iot_pill_temp_humidity"),
                    shape = pillShape,
                    color = pillSurfaceBg,
                    border = BorderStroke(1.dp, emeraldGreen.copy(alpha = 0.35f))
                ) {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = Translations.tr("smart_barn_sensor_temp_hum", lang),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText,
                            maxLines = 1
                        )
                    }
                }
            }

            Text(
                text = Translations.tr("smart_barn_tap_nh3_hint", lang),
                fontSize = 10.sp,
                color = secondaryText,
                maxLines = 1
            )

            // One-tap button: "🤖 تحليل قراءات الروبوت والقطيع الآن"
            Button(
                onClick = {
                    if (!isAnalyzing) {
                        isAnalyzing = true
                        scope.launch {
                            val result = AiVetGeminiService.analyzeSmartBarnTelemetry(
                                flockAgeDays = flockAgeDays,
                                liveBirds = liveBirds,
                                mortalityPct = mortalityPct,
                                dailyFeedQuintals = dailyFeedQuintals,
                                telemetry = telemetrySnapshot,
                                lang = lang
                            )
                            inlineAiReport = result
                            onAnalysisResult?.invoke(result)
                            isAnalyzing = false
                        }
                    }
                },
                enabled = !isAnalyzing,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 46.dp)
                    .testTag("btn_analyze_robot_telemetry"),
                shape = pillShape,
                colors = ButtonDefaults.buttonColors(containerColor = appearance.accentOption.primaryColor)
            ) {
                Text(
                    text = Translations.tr("smart_barn_analyze_btn", lang),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
            }

            if (inlineAiReport != null && onAnalysisResult == null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("smart_barn_inline_ai_report"),
                    shape = pillShape,
                    color = pillSurfaceBg,
                    border = BorderStroke(1.dp, subtleBorder)
                ) {
                    Text(
                        text = inlineAiReport.orEmpty(),
                        fontSize = 11.5.sp,
                        color = primaryText,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

// ============================================================================
// PROFESSION SELECTOR SUB-SCREEN & FIRST-LAUNCH PROFESSION ONBOARDING MODAL
// ============================================================================

data class ProfessionCardItem(
    val role: AppRole,
    val emoji: String,
    val titleKey: String,
    val descKey: String
)

val sevenProfessionCatalog: List<ProfessionCardItem> = listOf(
    ProfessionCardItem(
        role = AppRole.OWNER_BARN,
        emoji = "🏡",
        titleKey = "profession_card_owner_title",
        descKey = "profession_card_owner_desc"
    ),
    ProfessionCardItem(
        role = AppRole.VET_CLINIC,
        emoji = "🩺",
        titleKey = "profession_card_vet_title",
        descKey = "profession_card_vet_desc"
    ),
    ProfessionCardItem(
        role = AppRole.WORKER,
        emoji = "👷",
        titleKey = "profession_card_worker_title",
        descKey = "profession_card_worker_desc"
    ),
    ProfessionCardItem(
        role = AppRole.FEED_SUPPLIER,
        emoji = "🌾",
        titleKey = "profession_card_feed_title",
        descKey = "profession_card_feed_desc"
    ),
    ProfessionCardItem(
        role = AppRole.CHICK_HATCHERY,
        emoji = "🐣",
        titleKey = "profession_card_hatchery_title",
        descKey = "profession_card_hatchery_desc"
    ),
    ProfessionCardItem(
        role = AppRole.WHOLESALE_TRADER,
        emoji = "🚚",
        titleKey = "profession_card_trader_title",
        descKey = "profession_card_trader_desc"
    ),
    ProfessionCardItem(
        role = AppRole.VET_IMPORTER,
        emoji = "📦",
        titleKey = "profession_card_importer_title",
        descKey = "profession_card_importer_desc"
    )
)

@Composable
fun ProfessionSelectorSubScreen(
    activeRole: AppRole,
    lang: AppLanguage,
    isDark: Boolean,
    cardBg: Color,
    subtleSurfaceBg: Color,
    borderCol: Color,
    textPrimary: Color,
    textSecondary: Color,
    activeAccent: Color,
    activeAccentContainer: Color,
    previewShape: RoundedCornerShape,
    modifier: Modifier = Modifier,
    onSelectProfession: (AppRole) -> Unit
) {
    val normalizedActive = activeRole.normalizedHubRole
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .testTag("profession_selector_sub_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        sevenProfessionCatalog.forEach { item ->
            val isSelected = normalizedActive == item.role
            Surface(
                shape = previewShape,
                color = if (isSelected) activeAccentContainer else cardBg,
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) activeAccent else borderCol
                ),
                shadowElevation = if (isSelected) 2.dp else 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(previewShape)
                    .clickable { onSelectProfession(item.role) }
                    .testTag("role_chip_${item.role.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) activeAccent.copy(alpha = 0.2f) else subtleSurfaceBg,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(item.emoji, fontSize = 22.sp)
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = Translations.tr(item.titleKey, lang),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = if (isSelected) activeAccent else textPrimary
                            )
                            Text(
                                text = Translations.tr(item.descKey, lang),
                                fontSize = 11.5.sp,
                                color = textSecondary
                            )
                        }
                    }
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectProfession(item.role) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = activeAccent,
                            unselectedColor = textSecondary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ProfessionOnboardingDialog(
    lang: AppLanguage,
    currentTheme: AppThemeOption,
    initialRole: AppRole = AppRole.OWNER_BARN,
    onConfirmProfession: (AppRole) -> Unit
) {
    val appearance = LocalAppAppearanceState.current
    val isDark = currentTheme.isDark
    val screenBg = if (isDark) Color(0xFF0F172A) else currentTheme.bgColor
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val subtleSurfaceBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val borderCol = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    val activeAccent = appearance.accentColor
    val primaryButtonAccent = appearance.accentOption.primaryColor
    val activeAccentContainer = appearance.accentContainerColor
    val cardShape = RoundedCornerShape(appearance.cornerRadiusDp.dp)

    var selectedRole by remember(initialRole) { mutableStateOf(initialRole.normalizedHubRole) }

    Dialog(
        onDismissRequest = { onConfirmProfession(selectedRole) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            containerColor = screenBg,
            modifier = Modifier
                .fillMaxSize()
                .testTag("profession_onboarding_dialog"),
            bottomBar = {
                Surface(
                    color = cardBg,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, borderCol),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Button(
                            onClick = { onConfirmProfession(selectedRole) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_confirm_initial_profession"),
                            shape = cardShape,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryButtonAccent)
                        ) {
                            Text(
                                text = Translations.tr("profession_onboarding_confirm_btn", lang),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = cardShape,
                    color = cardBg,
                    border = BorderStroke(1.dp, borderCol),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = Translations.tr("profession_onboarding_title", lang),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.5.sp,
                            color = textPrimary
                        )
                        Text(
                            text = Translations.tr("profession_onboarding_subtitle", lang),
                            fontSize = 12.sp,
                            color = textSecondary
                        )
                    }
                }

                sevenProfessionCatalog.forEach { item ->
                    val isSelected = selectedRole == item.role
                    Surface(
                        shape = cardShape,
                        color = if (isSelected) activeAccentContainer else cardBg,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) activeAccent else borderCol
                        ),
                        shadowElevation = if (isSelected) 2.dp else 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(cardShape)
                            .clickable { selectedRole = item.role }
                            .testTag("onboarding_profession_card_${item.role.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) activeAccent.copy(alpha = 0.2f) else subtleSurfaceBg,
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(item.emoji, fontSize = 22.sp)
                                    }
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        text = Translations.tr(item.titleKey, lang),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.5.sp,
                                        color = if (isSelected) activeAccent else textPrimary
                                    )
                                    Text(
                                        text = Translations.tr(item.descKey, lang),
                                        fontSize = 11.5.sp,
                                        color = textSecondary
                                    )
                                }
                            }
                                RadioButton(
                                selected = isSelected,
                                onClick = { selectedRole = item.role },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = activeAccent,
                                    unselectedColor = textSecondary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Ironclad Local (`SharedPreferences` JSON) Persistence for Farms, Cycles, Daily Reports,
 * Transactions, and Vet Notes (`"agritech_local_state_v1"`).
 */
object LocalFarmPersistence {
    private const val PREF_NAME = "agritech_local_state_v1"
    private const val KEY_SAVED = "has_saved_state"
    private const val KEY_FARMS = "farms_json"
    private const val KEY_CYCLES = "cycles_json"
    private const val KEY_REPORTS = "daily_reports_json"
    private const val KEY_TRANSACTIONS = "transactions_json"
    private const val KEY_VET_NOTES = "vet_notes_json"

    fun hasSavedData(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SAVED, false)
    }

    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    fun saveAll(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val farmsArr = org.json.JSONArray()
            for (f in AgritechMockDb.farms) {
                val obj = org.json.JSONObject()
                obj.put("id", f.id)
                obj.put("name", f.name)
                obj.put("location", f.location)
                obj.put("capacity", f.capacity)
                obj.put("assignedWorkerId", f.assignedWorkerId)
                obj.put("currencyCode", f.currencyCode)
                farmsArr.put(obj)
            }

            val cyclesArr = org.json.JSONArray()
            for (c in AgritechMockDb.cycles) {
                val obj = org.json.JSONObject()
                obj.put("id", c.id)
                obj.put("farmId", c.farmId)
                obj.put("totalBirdsPlaced", c.totalBirdsPlaced)
                obj.put("targetWeight", c.targetWeight)
                obj.put("status", c.status)
                obj.put("startDate", c.startDate)
                obj.put("chickPrice", c.chickPrice)
                obj.put("feedInventoryKg", c.feedInventoryKg)
                obj.put("cumulativeBirdsSold", c.cumulativeBirdsSold)
                obj.put("cumulativeSoldWeightKg", c.cumulativeSoldWeightKg)
                obj.put("currencyCode", c.currencyCode)
                cyclesArr.put(obj)
            }

            val reportsArr = org.json.JSONArray()
            for (r in AgritechMockDb.dailyReports) {
                val obj = org.json.JSONObject()
                obj.put("id", r.id)
                obj.put("farmId", r.farmId)
                obj.put("cycleId", r.cycleId)
                obj.put("dayNumber", r.dayNumber)
                obj.put("feedIntakeKg", r.feedIntakeKg)
                obj.put("waterIntakeLiters", r.waterIntakeLiters)
                obj.put("mortalityCount", r.mortalityCount)
                obj.put("temperature", r.temperature)
                obj.put("gasUnits", r.gasUnits)
                obj.put("ventilationStatus", r.ventilationStatus)
                obj.put("generalStatus", r.generalStatus)
                obj.put("isLocked", r.isLocked)
                obj.put("isEdited", r.isEdited)
                obj.put("previousSummary", r.previousSummary)
                obj.put("actualAvgWeightKg", r.actualAvgWeightKg)
                obj.put("medicineGiven", r.medicineGiven)
                obj.put("medicineSummary", r.medicineSummary)
                obj.put("withdrawalDays", r.withdrawalDays)
                obj.put("updatedAtMillis", r.updatedAtMillis)
                reportsArr.put(obj)
            }

            val txArr = org.json.JSONArray()
            for (t in AgritechMockDb.transactions) {
                val obj = org.json.JSONObject()
                obj.put("id", t.id)
                obj.put("farmId", t.farmId)
                obj.put("cycleId", t.cycleId)
                obj.put("type", t.type.name)
                obj.put("category", t.category)
                obj.put("amount", t.amount)
                obj.put("currency", t.currency)
                obj.put("date", t.date)
                obj.put("status", t.status)
                obj.put("notes", t.notes)
                obj.put("quantityQuintals", t.quantityQuintals)
                obj.put("approvedBySignature", t.approvedBySignature)
                t.invoiceData?.let { inv ->
                    val invObj = org.json.JSONObject()
                    invObj.put("invoiceNumber", inv.invoiceNumber)
                    invObj.put("farmName", inv.farmName)
                    invObj.put("cycleId", inv.cycleId)
                    invObj.put("dateText", inv.dateText)
                    invObj.put("birdAgeDays", inv.birdAgeDays)
                    invObj.put("buyerName", inv.buyerName)
                    invObj.put("truckPlate", inv.truckPlate)
                    invObj.put("totalCrates", inv.totalCrates)
                    invObj.put("birdsPerCrate", inv.birdsPerCrate)
                    invObj.put("totalBirdsSold", inv.totalBirdsSold)
                    invObj.put("totalGrossKg", inv.totalGrossKg)
                    invObj.put("totalTareKg", inv.totalTareKg)
                    invObj.put("totalNetKg", inv.totalNetKg)
                    invObj.put("avgBirdWeightKg", inv.avgBirdWeightKg)
                    invObj.put("pricePerKg", inv.pricePerKg)
                    invObj.put("totalAmount", inv.totalAmount)
                    invObj.put("currency", inv.currency)
                    invObj.put("tareWeightsCount", inv.tareWeightsCount)
                    invObj.put("grossWeightsCount", inv.grossWeightsCount)
                    obj.put("invoiceData", invObj)
                }
                txArr.put(obj)
            }

            val notesArr = org.json.JSONArray()
            for (vn in AgritechMockDb.vetNotes) {
                val obj = org.json.JSONObject()
                obj.put("id", vn.id)
                obj.put("farmId", vn.farmId)
                obj.put("cycleId", vn.cycleId)
                obj.put("dayNumber", vn.dayNumber)
                obj.put("medicineName", vn.medicineName)
                obj.put("dosage", vn.dosage)
                obj.put("note", vn.note)
                obj.put("isUrgent", vn.isUrgent)
                obj.put("status", vn.status)
                obj.put("linkedTransactionId", vn.linkedTransactionId)
                obj.put("invoiceAmount", vn.invoiceAmount)
                obj.put("currency", vn.currency)
                obj.put("withdrawalDays", vn.withdrawalDays)
                obj.put("updatedAtMillis", vn.updatedAtMillis)
                notesArr.put(obj)
            }

            prefs.edit()
                .putBoolean(KEY_SAVED, true)
                .putString(KEY_FARMS, farmsArr.toString())
                .putString(KEY_CYCLES, cyclesArr.toString())
                .putString(KEY_REPORTS, reportsArr.toString())
                .putString(KEY_TRANSACTIONS, txArr.toString())
                .putString(KEY_VET_NOTES, notesArr.toString())
                .apply()
        } catch (_: Exception) {
        }
    }

    fun loadIntoMockDb(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            if (!prefs.getBoolean(KEY_SAVED, false)) return

            val farmsStr = prefs.getString(KEY_FARMS, null)
            if (!farmsStr.isNullOrBlank()) {
                val arr = org.json.JSONArray(farmsStr)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val farm = FarmModel(
                        id = obj.optString("id"),
                        name = obj.optString("name"),
                        location = obj.optString("location"),
                        capacity = obj.optInt("capacity", 5000),
                        assignedWorkerId = obj.optString("assignedWorkerId", "worker_1"),
                        currencyCode = obj.optString("currencyCode", "DZD")
                    )
                    if (farm.id.isNotBlank()) {
                        val idx = AgritechMockDb.farms.indexOfFirst { it.id == farm.id }
                        if (idx >= 0) AgritechMockDb.farms[idx] = farm else AgritechMockDb.farms.add(farm)
                    }
                }
            }

            val cyclesStr = prefs.getString(KEY_CYCLES, null)
            if (!cyclesStr.isNullOrBlank()) {
                val arr = org.json.JSONArray(cyclesStr)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val cycle = CycleModel(
                        id = obj.optString("id"),
                        farmId = obj.optString("farmId"),
                        totalBirdsPlaced = obj.optInt("totalBirdsPlaced", 5000),
                        targetWeight = obj.optDouble("targetWeight", 2.3),
                        status = obj.optString("status", "active"),
                        startDate = obj.optString("startDate", "2026-03-01"),
                        chickPrice = obj.optDouble("chickPrice", 120.0),
                        feedInventoryKg = obj.optDouble("feedInventoryKg", 0.0),
                        cumulativeBirdsSold = obj.optInt("cumulativeBirdsSold", 0),
                        cumulativeSoldWeightKg = obj.optDouble("cumulativeSoldWeightKg", 0.0),
                        currencyCode = obj.optString("currencyCode", "DZD")
                    )
                    if (cycle.id.isNotBlank()) {
                        val idx = AgritechMockDb.cycles.indexOfFirst { it.id == cycle.id }
                        if (idx >= 0) AgritechMockDb.cycles[idx] = cycle else AgritechMockDb.cycles.add(cycle)
                    }
                }
            }

            val reportsStr = prefs.getString(KEY_REPORTS, null)
            if (!reportsStr.isNullOrBlank()) {
                val arr = org.json.JSONArray(reportsStr)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val rep = DailyReportModel(
                        id = obj.optString("id"),
                        farmId = obj.optString("farmId"),
                        cycleId = obj.optString("cycleId"),
                        dayNumber = obj.optInt("dayNumber", 1),
                        feedIntakeKg = obj.optDouble("feedIntakeKg", 0.0),
                        waterIntakeLiters = obj.optDouble("waterIntakeLiters", 0.0),
                        mortalityCount = obj.optInt("mortalityCount", 0),
                        temperature = obj.optDouble("temperature", 30.0),
                        gasUnits = obj.optDouble("gasUnits", 0.0),
                        ventilationStatus = obj.optString("ventilationStatus", "جيدة"),
                        generalStatus = obj.optString("generalStatus", "طبيعية"),
                        isLocked = obj.optBoolean("isLocked", true),
                        isEdited = obj.optBoolean("isEdited", false),
                        previousSummary = obj.optString("previousSummary", ""),
                        actualAvgWeightKg = obj.optDouble("actualAvgWeightKg", 0.0),
                        medicineGiven = obj.optBoolean("medicineGiven", false),
                        medicineSummary = obj.optString("medicineSummary", ""),
                        withdrawalDays = obj.optInt("withdrawalDays", 0),
                        updatedAtMillis = obj.optLong("updatedAtMillis", 0L)
                    )
                    if (rep.id.isNotBlank()) {
                        val idx = AgritechMockDb.dailyReports.indexOfFirst {
                            it.id == rep.id || (it.farmId == rep.farmId && it.cycleId == rep.cycleId && it.dayNumber == rep.dayNumber)
                        }
                        if (idx >= 0) AgritechMockDb.dailyReports[idx] = rep else AgritechMockDb.dailyReports.add(rep)
                    }
                }
            }

            val txStr = prefs.getString(KEY_TRANSACTIONS, null)
            if (!txStr.isNullOrBlank()) {
                val arr = org.json.JSONArray(txStr)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val type = try {
                        TransactionType.valueOf(obj.optString("type", "EXPENSE"))
                    } catch (_: Exception) {
                        TransactionType.EXPENSE
                    }
                    val invObj = obj.optJSONObject("invoiceData")
                    val parsedInvoice = if (invObj != null) {
                        SalesInvoiceModel(
                            invoiceNumber = invObj.optString("invoiceNumber"),
                            farmName = invObj.optString("farmName"),
                            cycleId = invObj.optString("cycleId"),
                            dateText = invObj.optString("dateText"),
                            birdAgeDays = invObj.optInt("birdAgeDays", 1),
                            buyerName = invObj.optString("buyerName"),
                            truckPlate = invObj.optString("truckPlate"),
                            totalCrates = invObj.optInt("totalCrates", 0),
                            birdsPerCrate = invObj.optInt("birdsPerCrate", 10),
                            totalBirdsSold = invObj.optInt("totalBirdsSold", 0),
                            totalGrossKg = invObj.optDouble("totalGrossKg", 0.0),
                            totalTareKg = invObj.optDouble("totalTareKg", 0.0),
                            totalNetKg = invObj.optDouble("totalNetKg", 0.0),
                            avgBirdWeightKg = invObj.optDouble("avgBirdWeightKg", 0.0),
                            pricePerKg = invObj.optDouble("pricePerKg", 0.0),
                            totalAmount = invObj.optDouble("totalAmount", 0.0),
                            currency = invObj.optString("currency", "دج"),
                            tareWeightsCount = invObj.optInt("tareWeightsCount", 0),
                            grossWeightsCount = invObj.optInt("grossWeightsCount", 0)
                        )
                    } else null
                    val tx = TransactionModel(
                        id = obj.optString("id"),
                        farmId = obj.optString("farmId"),
                        cycleId = obj.optString("cycleId"),
                        type = type,
                        category = obj.optString("category"),
                        amount = obj.optDouble("amount", 0.0),
                        currency = obj.optString("currency", "دج"),
                        date = obj.optString("date", "اليوم 1"),
                        invoiceData = parsedInvoice,
                        status = obj.optString("status", "APPROVED"),
                        notes = obj.optString("notes", ""),
                        quantityQuintals = obj.optDouble("quantityQuintals", 0.0),
                        approvedBySignature = obj.optString("approvedBySignature", "")
                    )
                    if (tx.id.isNotBlank()) {
                        val idx = AgritechMockDb.transactions.indexOfFirst { it.id == tx.id }
                        if (idx >= 0) {
                            val existing = AgritechMockDb.transactions[idx]
                            AgritechMockDb.transactions[idx] = tx.copy(invoiceData = tx.invoiceData ?: existing.invoiceData)
                        } else {
                            AgritechMockDb.transactions.add(tx)
                        }
                    }
                }
            }

            val notesStr = prefs.getString(KEY_VET_NOTES, null)
            if (!notesStr.isNullOrBlank()) {
                val arr = org.json.JSONArray(notesStr)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val vn = VetNoteModel(
                        id = obj.optString("id"),
                        farmId = obj.optString("farmId"),
                        cycleId = obj.optString("cycleId"),
                        dayNumber = obj.optInt("dayNumber", 1),
                        medicineName = obj.optString("medicineName"),
                        dosage = obj.optString("dosage"),
                        note = obj.optString("note"),
                        isUrgent = obj.optBoolean("isUrgent", false),
                        status = obj.optString("status", "APPROVED"),
                        linkedTransactionId = obj.optString("linkedTransactionId", ""),
                        invoiceAmount = obj.optDouble("invoiceAmount", 0.0),
                        currency = obj.optString("currency", "دج"),
                        withdrawalDays = obj.optInt("withdrawalDays", 0),
                        updatedAtMillis = obj.optLong("updatedAtMillis", 0L)
                    )
                    if (vn.id.isNotBlank()) {
                        val idx = AgritechMockDb.vetNotes.indexOfFirst { it.id == vn.id }
                        if (idx >= 0) AgritechMockDb.vetNotes[idx] = vn else AgritechMockDb.vetNotes.add(vn)
                    }
                }
            }
        } catch (_: Exception) {
        }
    }
}



