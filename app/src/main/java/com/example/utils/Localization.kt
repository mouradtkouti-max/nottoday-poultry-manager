package com.example.utils

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.AgritechMockDb
import com.example.models.AppThemeOption

// ============================================================================
// LANGUAGES, SMART FEED UNITS & MULTI-LANGUAGE ENGINE (AR, FR, EN, ES, PT + HI)
// ============================================================================

enum class FeedUnitPreference {
    QUINTAL, // قنطار (100 كغ) - Default for bulk feed purchases
    KG       // كيلوغرام (كغ)
}

val LocalFeedUnitPreference = compositionLocalOf { FeedUnitPreference.QUINTAL }

enum class AppLanguage(
    val code: String,
    val label: String,
    val isRtl: Boolean,
    val subtitleLabel: String = label,
    val greeting: String = "Hello",
    val greetingSubtitle: String = "Choose your preferred language to continue"
) {
    AR("ar", "العربية", true, "Arabic", "مرحباً بك", "اختر لغتك المفضلة للمتابعة في إدارة المزرعة"),
    FR("fr", "Français", false, "French", "Bonjour", "Choisissez votre langue préférée pour continuer"),
    EN("en", "English", false, "English (International)", "Hello", "Choose your preferred language to continue"),
    ES("es", "Español", false, "Spanish (Latin America)", "¡Hola!", "Elige tu idioma preferido para continuar"),
    PT("pt", "Português", false, "Portuguese (Brazil)", "Olá!", "Escolha seu idioma preferido para continuar"),
    TR("tr", "Türkçe", false, "Turkish", "Merhaba", "Devam etmek için tercih ettiğiniz dili seçin"),
    HI("hi", "हिन्दी", false, "Hindi", "नमस्ते", "जारी रखने के लिए अपनी पसंदीदा भाषा चुनें"),
    ID("id", "Bahasa Indonesia", false, "Indonesian", "Halo", "Pilih bahasa pilihan Anda untuk melanjutkan"),
    UR("ur", "اردو", true, "Urdu", "خوش آمدید", "جاری رکھنے کے لیے اپنی پسندیدہ زبان منتخب کریں"),
    SW("sw", "Kiswahili", false, "Swahili", "Hujambo", "Chagua lugha unayopendelea ili kuendelea"),
    RU("ru", "Русский", false, "Russian", "Привет", "Выберите предпочитаемый язык для продолжения"),
    ZH("zh", "中文 (简体)", false, "Chinese (Simplified)", "你好", "请选择您的首选语言以继续"),
    DE("de", "Deutsch", false, "German", "Hallo", "Wählen Sie Ihre bevorzugte Sprache, um fortzufahren"),
    IT("it", "Italiano", false, "Italian", "Ciao", "Scegli la tua lingua preferita per continuare")
}

object Translations {
    // Global SaaS Currency & Country state applied across all financial text fields
    var globalCurrencyCode by mutableStateOf<String?>(null)
    var globalCountryCode by mutableStateOf("DZ")
    private val dict: Map<String, Map<String, String>> = mapOf(
        "app_title" to mapOf(
            "ar" to "دفتر إدارة الدواجن 🐥",
            "fr" to "Gestion Avicole 🐥",
            "en" to "Poultry Manager 🐥",
            "es" to "Gestión Avícola 🐥",
            "hi" to "पोल्ट्री प्रबंधन 🐥"
        ),
        "app_subtitle" to mapOf(
            "ar" to "نظام متابعة العنابر والإنتاج اليومي",
            "fr" to "Suivi des poulaillers et production journalière",
            "en" to "Broiler farm & daily production tracker",
            "es" to "Control de granjas y producción diaria",
            "hi" to "दैनिक पोल्ट्री उत्पादन एवं फार्म ट्रैकर"
        ),
        "role_owner" to mapOf(
            "ar" to "مدير المزرعة",
            "fr" to "Directeur",
            "en" to "Farm Owner",
            "es" to "Dueño",
            "hi" to "फार्म मालिक"
        ),
        "role_vet" to mapOf(
            "ar" to "طبيب بيطري",
            "fr" to "Vétérinaire",
            "en" to "Veterinarian",
            "es" to "Veterinario",
            "hi" to "पशु चिकित्सक"
        ),
        "role_worker" to mapOf(
            "ar" to "مشرف العنبر",
            "fr" to "Superviseur",
            "en" to "Worker",
            "es" to "Supervisor",
            "hi" to "कार्यकर्ता"
        ),
        "login_title_owner" to mapOf(
            "ar" to "تسجيل دخول المالك",
            "fr" to "Connexion Propriétaire",
            "en" to "Owner Login",
            "es" to "Acceso Dueño",
            "hi" to "मालिक लॉगिन"
        ),
        "login_title_vet" to mapOf(
            "ar" to "دخول الطبيب البيطري",
            "fr" to "Connexion Vétérinaire",
            "en" to "Veterinarian Login",
            "es" to "Acceso Veterinario",
            "hi" to "पशु चिकित्सक लॉगिन"
        ),
        "login_title_worker" to mapOf(
            "ar" to "تسجيل دخول مشرف العنبر",
            "fr" to "Connexion Superviseur",
            "en" to "Worker Login",
            "es" to "Acceso Supervisor",
            "hi" to "कार्यकर्ता लॉगिन"
        ),
        "username_label" to mapOf(
            "ar" to "اسم المستخدم أو البريد",
            "fr" to "Nom d'utilisateur ou e-mail",
            "en" to "Username or Email",
            "es" to "Usuario o Correo",
            "hi" to "उपयोगकर्ता नाम या ईमेल"
        ),
        "password_label" to mapOf(
            "ar" to "كلمة المرور",
            "fr" to "Mot de passe",
            "en" to "Password",
            "es" to "Contraseña",
            "hi" to "पासवर्ड"
        ),
        "login_btn" to mapOf(
            "ar" to "تسجيل الدخول",
            "fr" to "Connexion",
            "en" to "Log In",
            "es" to "Iniciar Sesión",
            "hi" to "लॉग इन करें"
        ),
        "farms_title" to mapOf(
            "ar" to "عنابر ومنشآت الدواجن",
            "fr" to "Bâtiments et Élevages",
            "en" to "Broiler Houses & Farms",
            "es" to "Galpones e Instalaciones",
            "hi" to "पोल्ट्री शेड एवं फार्म"
        ),
        "owner_supervision" to mapOf(
            "ar" to "إشراف المالك العام",
            "fr" to "Supervision Générale",
            "en" to "General Management",
            "es" to "Supervisión General",
            "hi" to "सामान्य पर्यवेक्षण"
        ),
        "open_notebook" to mapOf(
            "ar" to "فتح دفتر المزرعة",
            "fr" to "Ouvrir le carnet",
            "en" to "Open Farm Notebook",
            "es" to "Abrir Cuaderno",
            "hi" to "फार्म बही खोलें"
        ),
        "batch_label" to mapOf(
            "ar" to "الدفعة",
            "fr" to "Lot",
            "en" to "Flock",
            "es" to "Lote",
            "hi" to "बैच"
        ),
        "birds_unit" to mapOf(
            "ar" to "طائر 🐥",
            "fr" to "sujets 🐥",
            "en" to "birds 🐥",
            "es" to "aves 🐥",
            "hi" to "पक्षी 🐥"
        ),
        "day_prefix" to mapOf(
            "ar" to "اليوم",
            "fr" to "Jour",
            "en" to "Day",
            "es" to "Día",
            "hi" to "दिन"
        ),
        "tab_ledger" to mapOf(
            "ar" to "📋 جدول السجل",
            "fr" to "📋 Registre",
            "en" to "📋 Ledger",
            "es" to "📋 Registro",
            "hi" to "📋 दैनिक बही"
        ),
        "tab_treasury" to mapOf(
            "ar" to "💰 الخزنة",
            "fr" to "💰 Caisse",
            "en" to "💰 Treasury",
            "es" to "💰 Caja",
            "hi" to "💰 खजाना"
        ),
        "tab_analytics" to mapOf(
            "ar" to "📊 التحليلات",
            "fr" to "📊 Analyses",
            "en" to "📊 Analytics",
            "es" to "📊 Análisis",
            "hi" to "📊 विश्लेषण"
        ),
        "tab_vet" to mapOf(
            "ar" to "🩺 البيطري",
            "fr" to "🩺 Vétérinaire",
            "en" to "🩺 Vet",
            "es" to "🩺 Veterinario",
            "hi" to "🩺 पशु चिकित्सक"
        ),
        "th_day" to mapOf(
            "ar" to "🗓️ اليوم",
            "fr" to "🗓️ Jour",
            "en" to "🗓️ Day",
            "es" to "🗓️ Día",
            "hi" to "🗓️ दिन"
        ),
        "th_feed" to mapOf(
            "ar" to "🌾 طعام (ق)",
            "fr" to "🌾 Aliment (q)",
            "en" to "🌾 Feed (q)",
            "es" to "🌾 Alimento (q)",
            "hi" to "🌾 दाना (क्विं.)"
        ),
        "th_water" to mapOf(
            "ar" to "💧 ماء (لتر)",
            "fr" to "💧 Eau (L)",
            "en" to "💧 Water (L)",
            "es" to "💧 Agua (L)",
            "hi" to "💧 पानी (लीटर)"
        ),
        "th_mortality" to mapOf(
            "ar" to "💀 نفوق",
            "fr" to "💀 Mortalité",
            "en" to "💀 Mortality",
            "es" to "💀 Mortandad",
            "hi" to "💀 मृत्यु"
        ),
        "th_temp" to mapOf(
            "ar" to "حرارة (°C)",
            "fr" to "Temp (°C)",
            "en" to "Temp (°C)",
            "es" to "Temp (°C)",
            "hi" to "तापमान (°C)"
        ),
        "th_gas" to mapOf(
            "ar" to "غاز",
            "fr" to "Gaz",
            "en" to "Gas",
            "es" to "Gas",
            "hi" to "गैस"
        ),
        "th_vent" to mapOf(
            "ar" to "التهوية",
            "fr" to "Aération",
            "en" to "Ventilation",
            "es" to "Ventilación",
            "hi" to "वेंटिलेशन"
        ),
        "sum_feed" to mapOf(
            "ar" to "🌾 إجمالي العلف",
            "fr" to "🌾 Aliment Total",
            "en" to "🌾 Total Feed",
            "es" to "🌾 Alimento Total",
            "hi" to "🌾 कुल दाना"
        ),
        "sum_water" to mapOf(
            "ar" to "💧 إجمالي الماء",
            "fr" to "💧 Eau Totale",
            "en" to "💧 Total Water",
            "es" to "💧 Agua Total",
            "hi" to "💧 कुल पानी"
        ),
        "sum_mortality" to mapOf(
            "ar" to "💀 إجمالي النفوق",
            "fr" to "💀 Mortalité Totale",
            "en" to "💀 Total Mortality",
            "es" to "💀 Mortandad Total",
            "hi" to "💀 कुल मृत्यु"
        ),
        "currency" to mapOf(
            "ar" to "دج",
            "fr" to "DZD",
            "en" to "DZD",
            "es" to "DZD",
            "hi" to "DZD"
        ),
        "kg_unit" to mapOf(
            "ar" to "كغ",
            "fr" to "kg",
            "en" to "kg",
            "es" to "kg",
            "hi" to "किग्रा"
        ),
        "liter_unit" to mapOf(
            "ar" to "لتر",
            "fr" to "L",
            "en" to "L",
            "es" to "L",
            "hi" to "लीटर"
        ),
        "treasury_title" to mapOf(
            "ar" to "💰 خزنة الدورة المالية",
            "fr" to "💰 Caisse du cycle",
            "en" to "💰 Cycle Treasury",
            "es" to "💰 Caja del Ciclo",
            "hi" to "💰 चक्र खजाना"
        ),
        "total_expenses" to mapOf(
            "ar" to "إجمالي المصاريف",
            "fr" to "Dépenses Totales",
            "en" to "Total Expenses",
            "es" to "Gastos Totales",
            "hi" to "कुल खर्च"
        ),
        "total_income" to mapOf(
            "ar" to "إجمالي الإيرادات",
            "fr" to "Revenus Totaux",
            "en" to "Total Income",
            "es" to "Ingresos Totales",
            "hi" to "कुल आय"
        ),
        "net_balance" to mapOf(
            "ar" to "صافي الرصيد",
            "fr" to "Solde Net",
            "en" to "Net Balance",
            "es" to "Saldo Neto",
            "hi" to "शुद्ध शेष"
        ),
        "btn_spend" to mapOf(
            "ar" to "🔴 صرف مصروف 💸",
            "fr" to "🔴 Dépense 💸",
            "en" to "🔴 Record Expense 💸",
            "es" to "🔴 Registrar Gasto 💸",
            "hi" to "🔴 खर्च दर्ज करें 💸"
        ),
        "btn_income" to mapOf(
            "ar" to "🟢 دخل إيراد 💵",
            "fr" to "🟢 Revenu 💵",
            "en" to "🟢 Record Income 💵",
            "es" to "🟢 Registrar Ingreso 💵",
            "hi" to "🟢 आय दर्ज करें 💵"
        ),
        "tx_history_title" to mapOf(
            "ar" to "سجل العمليات المالية للدورة",
            "fr" to "Historique des transactions",
            "en" to "Financial Transactions Log",
            "es" to "Historial de Transacciones",
            "hi" to "वित्तीय लेनदेन इतिहास"
        ),
        "analytics_title" to mapOf(
            "ar" to "📊 مؤشرات الأداء والكفاءة",
            "fr" to "📊 Indicateurs de Performance",
            "en" to "📊 Performance & Efficiency KPIs",
            "es" to "📊 Indicadores de Rendimiento",
            "hi" to "📊 प्रदर्शन एवं दक्षता संकेतक"
        ),
        "liveability" to mapOf(
            "ar" to "💚 نسبة الحيوية (Liveability)",
            "fr" to "💚 Viabilité (Liveability)",
            "en" to "💚 Liveability",
            "es" to "💚 Viabilidad (Liveability)",
            "hi" to "💚 जीवन दर (Liveability)"
        ),
        "fcr" to mapOf(
            "ar" to "⚖️ معامل التحويل FCR",
            "fr" to "⚖️ Indice de Consommation (FCR)",
            "en" to "⚖️ Feed Conversion Ratio (FCR)",
            "es" to "⚖️ Conversión Alimenticia (FCR)",
            "hi" to "⚖️ फ़ीड रूपांतरण अनुपात (FCR)"
        ),
        "est_weight" to mapOf(
            "ar" to "🐥 الوزن التقديري الحالي",
            "fr" to "🐥 Poids Moyen Estimé",
            "en" to "🐥 Estimated Average Weight",
            "es" to "🐥 Peso Promedio Estimado",
            "hi" to "🐥 अनुमानित औसत वजन"
        ),
        "total_biomass" to mapOf(
            "ar" to "📊 إجمالي الكتلة الحية",
            "fr" to "📊 Biomasse Totale",
            "en" to "📊 Total Live Biomass",
            "es" to "📊 Biomasa Total",
            "hi" to "📊 कुल जीवित बायोमास"
        ),
        "epef" to mapOf(
            "ar" to "🏆 مؤشر الكفاءة EPEF",
            "fr" to "🏆 Facteur d'Efficacité (EPEF)",
            "en" to "🏆 Efficiency Factor (EPEF)",
            "es" to "🏆 Factor de Eficiencia (EPEF)",
            "hi" to "🏆 दक्षता कारक (EPEF)"
        ),
        "cost_per_kg" to mapOf(
            "ar" to "🏷️ تكلفة الكيلو",
            "fr" to "🏷️ Coût par kg",
            "en" to "🏷️ Cost per kg",
            "es" to "🏷️ Costo por kg",
            "hi" to "🏷️ प्रति किलो लागत"
        ),
        "worker_log_title" to mapOf(
            "ar" to "سجل العمليات اليومي للعامل",
            "fr" to "Registre journalier de l'ouvrier",
            "en" to "Worker Daily Log",
            "es" to "Registro Diario del Trabajador",
            "hi" to "कार्यकर्ता दैनिक लॉग"
        ),
        "log_today_prefix" to mapOf(
            "ar" to "تسجيل بيانات اليوم",
            "fr" to "Enregistrer les données du jour",
            "en" to "Log Today's Data",
            "es" to "Registrar Datos de Hoy",
            "hi" to "आज का डेटा दर्ज करें"
        ),
        "lock_report_btn" to mapOf(
            "ar" to "تأكيد وقفل التقرير 🔒",
            "fr" to "Valider et verrouiller 🔒",
            "en" to "Confirm & Lock Report 🔒",
            "es" to "Confirmar y Bloquear 🔒",
            "hi" to "पुष्टि करें और लॉक करें 🔒"
        ),
        "locked_today_msg" to mapOf(
            "ar" to "تم قفل وحفظ تقرير اليوم 🔒",
            "fr" to "Rapport du jour validé et verrouillé 🔒",
            "en" to "Today's report confirmed & locked 🔒",
            "es" to "Reporte de hoy confirmado y bloqueado 🔒",
            "hi" to "आज की रिपोर्ट सत्यापित और लॉक कर दी गई 🔒"
        ),
        "settings_dialog_title" to mapOf(
            "ar" to "إعدادات التطبيق والمظهر ⚙️",
            "fr" to "Paramètres et Apparence ⚙️",
            "en" to "Settings & Appearance ⚙️",
            "es" to "Ajustes y Apariencia ⚙️",
            "hi" to "सेटिंग्स और दिखावट ⚙️"
        ),
        "theme_section_title" to mapOf(
            "ar" to "🎨 مظهر الشاشة ولون الخلفية 🧊:",
            "fr" to "🎨 Apparence et couleur de fond 🧊:",
            "en" to "🎨 Screen Appearance & Background 🧊:",
            "es" to "🎨 Apariencia y Color de Fondo 🧊:",
            "hi" to "🎨 स्क्रीन का रूप और पृष्ठभूमि रंग 🧊:"
        ),
        "theme_ice_blue" to mapOf(
            "ar" to "أزرق جليدي بارد 🧊",
            "en" to "Cool Ice Blue 🧊",
            "fr" to "Bleu Glacé 🧊",
            "es" to "Azul Glacial 🧊",
            "hi" to "बर्फ़ीला नीला 🧊"
        ),
        "theme_farm_green" to mapOf(
            "ar" to "أخضر زراعي طبيعي 🌿",
            "en" to "Natural Farm Green 🌿",
            "fr" to "Vert Agricole 🌿",
            "es" to "Verde Granja 🌿",
            "hi" to "प्राकृतिक हरा 🌿"
        ),
        "theme_clean_white" to mapOf(
            "ar" to "أبيض ناصع مشرق ☀️",
            "en" to "Bright Clean White ☀️",
            "fr" to "Blanc Éclatant ☀️",
            "es" to "Blanco Brillante ☀️",
            "hi" to "चमकदार सफ़ेद ☀️"
        ),
        "theme_night_mode" to mapOf(
            "ar" to "وضع ليلي هادئ 🌙",
            "en" to "Calm Night Mode 🌙",
            "fr" to "Mode Nuit 🌙",
            "es" to "Modo Noche 🌙",
            "hi" to "रात का मोड 🌙"
        ),
        "language_section_title" to mapOf(
            "ar" to "🌐 اختيار اللغة (Language):",
            "fr" to "🌐 Choix de la langue (Language):",
            "en" to "🌐 Select Language:",
            "es" to "🌐 Seleccionar Idioma:",
            "hi" to "🌐 भाषा चुनें:"
        ),
        "farm_info_title" to mapOf(
            "ar" to "🏡 معلومات المنشأة:",
            "fr" to "🏡 Informations du bâtiment:",
            "en" to "🏡 Farm House Info:",
            "es" to "🏡 Información del Galpón:",
            "hi" to "🏡 फार्म की जानकारी:"
        ),
        "save_and_close" to mapOf(
            "ar" to "حفظ وإغلاق",
            "fr" to "Enregistrer et fermer",
            "en" to "Save & Close",
            "es" to "Guardar y Cerrar",
            "hi" to "सहेजें और बंद करें"
        ),
        "farm_1_name" to mapOf(
            "ar" to "عنبر البركة النموذجي",
            "fr" to "Bâtiment Avicole Al-Baraka",
            "en" to "Al-Baraka Model Farm (Barn A)",
            "es" to "Granja Modelo Al-Baraka (Galpón A)",
            "hi" to "अल-बरका मॉडल फार्म (शेड ए)"
        ),
        "farm_1_location" to mapOf(
            "ar" to "المدية - سيدي نعمان",
            "fr" to "Médéa - Sidi Naamane",
            "en" to "Medea - Sidi Naamane",
            "es" to "Medea - Sidi Naamane",
            "hi" to "मेडेया - सिदी नामने"
        ),
        "farm_2_name" to mapOf(
            "ar" to "مزرعة الوفاق للدواجن",
            "fr" to "Élevage Avicole Al-Wifaq",
            "en" to "Al-Wifaq Poultry Farm",
            "es" to "Granja Avícola Al-Wifaq",
            "hi" to "अल-विफ़ाक पोल्ट्री फार्म"
        ),
        "farm_2_location" to mapOf(
            "ar" to "البويرة - عين بسام",
            "fr" to "Bouira - Ain Bessem",
            "en" to "Bouira - Ain Bessem",
            "es" to "Bouira - Ain Bessem",
            "hi" to "बुइरा - ऐन बेसेम"
        ),
        "vent_good" to mapOf(
            "ar" to "جيدة",
            "fr" to "Bonne",
            "en" to "Good",
            "es" to "Buena",
            "hi" to "अच्छा"
        ),
        "vent_moderate" to mapOf(
            "ar" to "متوسطة",
            "fr" to "Moyenne",
            "en" to "Moderate",
            "es" to "Regular",
            "hi" to "मध्यम"
        ),
        "vent_issue" to mapOf(
            "ar" to "مشكلة",
            "fr" to "Problème",
            "en" to "Issue",
            "es" to "Problema",
            "hi" to "समस्या"
        ),
        "tx_chicks" to mapOf(
            "ar" to "🐣 كتاكيت (Ross 308)",
            "fr" to "🐣 Poussins (Ross 308)",
            "en" to "🐣 Chicks (Ross 308)",
            "es" to "🐣 Pollitos (Ross 308)",
            "hi" to "🐣 चूजे (Ross 308)"
        ),
        "tx_feed" to mapOf(
            "ar" to "🌾 علف بادئ (25 ق)",
            "fr" to "🌾 Aliment Démarrage (25 q)",
            "en" to "🌾 Starter Feed (25 q)",
            "es" to "🌾 Alimento Inicio (25 q)",
            "hi" to "🌾 स्टार्टर दाना (25 क्विं.)"
        ),
        "tx_gas" to mapOf(
            "ar" to "🔥 غاز وتدفئة",
            "fr" to "🔥 Gaz & Chauffage",
            "en" to "🔥 Gas & Heating",
            "es" to "🔥 Gas y Calefacción",
            "hi" to "🔥 गैस और हीटिंग"
        ),
        "tx_meds" to mapOf(
            "ar" to "💊 أدوية وتحصين",
            "fr" to "💊 Médicaments & Vaccins",
            "en" to "💊 Medicine & Vaccines",
            "es" to "💊 Medicinas y Vacunas",
            "hi" to "💊 दवाइयां और टीके"
        ),
        "tx_manure" to mapOf(
            "ar" to "🚜 بيع سماد عضوي",
            "fr" to "🚜 Vente Fumier Organique",
            "en" to "🚜 Organic Manure Sale",
            "es" to "🚜 Venta Abono Orgánico",
            "hi" to "🚜 जैविक खाद बिक्री"
        ),
        "tx_chicken_sale" to mapOf(
            "ar" to "🍗 بيع دجاج لحم",
            "fr" to "🍗 Vente Poulets de Chair",
            "en" to "🍗 Broiler Chicken Sale",
            "es" to "🍗 Venta Pollos de Engorde",
            "hi" to "🍗 ब्रायलर चिकन बिक्री"
        ),
        "tx_labor" to mapOf(
            "ar" to "🚚 عمالة ونقل",
            "fr" to "🚚 Main-d'œuvre & Transport",
            "en" to "🚚 Labor & Transport",
            "es" to "🚚 Mano de Obra y Transporte",
            "hi" to "🚚 श्रम और परिवहन"
        ),
        "tx_other_inc" to mapOf(
            "ar" to "إيرادات أخرى",
            "fr" to "Autres Revenus",
            "en" to "Other Income",
            "es" to "Otros Ingresos",
            "hi" to "अन्य आय"
        ),
        "tx_other_exp" to mapOf(
            "ar" to "مصاريف أخرى",
            "fr" to "Autres Dépenses",
            "en" to "Other Expenses",
            "es" to "Otros Gastos",
            "hi" to "अन्य खर्च"
        ),
        "med_vitamin" to mapOf(
            "ar" to "فيتامين AD3E + إلكترولايت",
            "fr" to "Électrolytes + Vitamine AD3E",
            "en" to "Electrolytes + Vitamin AD3E",
            "es" to "Electrolitos + Vitamina AD3E",
            "hi" to "इलेक्ट्रोलाइट्स + विटामिन AD3E"
        ),
        "med_antibiotic" to mapOf(
            "ar" to "مضاد حيوي تنفسي وقائي",
            "fr" to "Antibiotique Respiratoire Préventif",
            "en" to "Preventive Respiratory Antibiotic",
            "es" to "Antibiótico Respiratorio Preventivo",
            "hi" to "निवारक श्वसन एंटीबायोटिक"
        ),
        "med_1_dosage" to mapOf(
            "ar" to "1 مل / لتر لمدة 3 أيام",
            "fr" to "1 ml / L pendant 3 jours",
            "en" to "1 ml / L for 3 days",
            "es" to "1 ml / L por 3 días",
            "hi" to "1 मिली / लीटर 3 दिनों के लिए"
        ),
        "med_1_note" to mapOf(
            "ar" to "تحصين استقبال الكتاكيت وتفادي إجهاد النقل",
            "fr" to "Réception des poussins et anti-stress transport",
            "en" to "Chicks arrival protocol & transport stress prevention",
            "es" to "Protocolo de recepción y prevención de estrés de transporte",
            "hi" to "चूजों के आगमन का प्रोटोकॉल एवं परिवहन तनाव निवारण"
        ),
        "med_2_dosage" to mapOf(
            "ar" to "0.5 غرام / لتر لمدة يومين",
            "fr" to "0.5 g / L pendant 2 jours",
            "en" to "0.5 g / L for 2 days",
            "es" to "0.5 g / L por 2 días",
            "hi" to "0.5 ग्राम / लीटर 2 दिनों के लिए"
        ),
        "med_2_note" to mapOf(
            "ar" to "ملاحظة صوت خفيف أثناء التهوية الليلية",
            "fr" to "Léger râle respiratoire détecté en aération nocturne",
            "en" to "Mild respiratory snick noticed during night ventilation",
            "es" to "Leve estertor respiratorio notado durante ventilación nocturna",
            "hi" to "रात के वेंटिलेशन के दौरान हल्का श्वसन खर्राटे देखा गया"
        ),
        "vet_records_title" to mapOf(
            "ar" to "🩺 السجل والوصفات الطبية البيطرية",
            "fr" to "🩺 Registre et Prescriptions Vétérinaires",
            "en" to "🩺 Veterinary Medical Records",
            "es" to "🩺 Registros y Recetas Veterinarias",
            "hi" to "🩺 पशु चिकित्सा रिकॉर्ड एवं पर्चे"
        ),
        "dosage_label" to mapOf(
            "ar" to "الجرعة",
            "fr" to "Posologie",
            "en" to "Dosage",
            "es" to "Dosis",
            "hi" to "खुराक"
        ),
        "med_urgent_badge" to mapOf(
            "ar" to "⚠️ عاجل",
            "fr" to "⚠️ Urgent",
            "en" to "⚠️ Urgent",
            "es" to "⚠️ Urgente",
            "hi" to "⚠️ अत्यावश्यक"
        ),
        "open_medical_records" to mapOf(
            "ar" to "فتح السجل الطبي البيطري",
            "fr" to "Ouvrir le dossier médical",
            "en" to "Open Medical Records",
            "es" to "Abrir Expediente Médico",
            "hi" to "चिकित्सा रिकॉर्ड खोलें"
        ),
        "category_label" to mapOf(
            "ar" to "التصنيف:",
            "fr" to "Catégorie :",
            "en" to "Category:",
            "es" to "Categoría:",
            "hi" to "श्रेणी:"
        ),
        "amount_label" to mapOf(
            "ar" to "المبلغ (دج)",
            "fr" to "Montant (DZD)",
            "en" to "Amount (DZD)",
            "es" to "Monto (DZD)",
            "hi" to "राशि (DZD)"
        ),
        "confirm_btn" to mapOf(
            "ar" to "تأكيد",
            "fr" to "Confirmer",
            "en" to "Confirm",
            "es" to "Confirmar",
            "hi" to "पुष्टि करें"
        ),
        "cancel_btn" to mapOf(
            "ar" to "إلغاء",
            "fr" to "Annuler",
            "en" to "Cancel",
            "es" to "Cancelar",
            "hi" to "रद्द करें"
        ),
        "cycle_label" to mapOf(
            "ar" to "الدورة",
            "fr" to "Cycle",
            "en" to "Cycle",
            "es" to "Ciclo",
            "hi" to "चक्र"
        ),
        "temp_label" to mapOf(
            "ar" to "الحرارة",
            "fr" to "Temp",
            "en" to "Temp",
            "es" to "Temp",
            "hi" to "तापमान"
        ),
        "gas_label" to mapOf(
            "ar" to "الغاز",
            "fr" to "Gaz",
            "en" to "Gas",
            "es" to "Gas",
            "hi" to "गैस"
        ),
        "weighing_sales_title" to mapOf(
            "ar" to "ميزان وبيع الدفعة ⚖️🍗",
            "fr" to "Pesée & Vente du lot ⚖️🍗",
            "en" to "Batch Weighing & Sales ⚖️🍗",
            "es" to "Pesaje y Venta del Lote ⚖️🍗",
            "hi" to "वजन और बिक्री ⚖️🍗"
        ),
        "weighing_sales_subtitle" to mapOf(
            "ar" to "حاسبة الميزان السريع (الوزنة = 5 صناديق)",
            "fr" to "Calculateur rapide (Pesée = 5 caisses)",
            "en" to "Quick Weigh Calculator (Weigh = 5 crates)",
            "es" to "Calculadora rápida (Pesada = 5 cajas)",
            "hi" to "त्वरित वजन कैलकुलेटर (1 वजन = 5 क्रेट)"
        ),
        "gross_weight_label" to mapOf(
            "ar" to "الوزن القائم (5 صناديق ممتلئة) كغ",
            "fr" to "Poids brut (5 caisses pleines) kg",
            "en" to "Gross Weight (5 full crates) kg",
            "es" to "Peso bruto (5 cajas llenas) kg",
            "hi" to "सकल वजन (5 भरी क्रेट) किग्रा"
        ),
        "tare_weight_label" to mapOf(
            "ar" to "وزن الفارغ / التارا (كغ)",
            "fr" to "Tare des caisses vides (kg)",
            "en" to "Tare Weight (empty crates) kg",
            "es" to "Tara de cajas vacías (kg)",
            "hi" to "खाली वजन / टेयर (किग्रा)"
        ),
        "birds_per_crate_label" to mapOf(
            "ar" to "عدد الطيور بالصندوق:",
            "fr" to "Poulets par caisse :",
            "en" to "Birds per crate:",
            "es" to "Aves por caja:",
            "hi" to "प्रति क्रेट मुर्गियाँ:"
        ),
        "crate_8_birds" to mapOf(
            "ar" to "8 طيور (40 طائر)",
            "fr" to "8 poulets (40)",
            "en" to "8 birds (40)",
            "es" to "8 aves (40)",
            "hi" to "8 मुर्गियाँ (40)"
        ),
        "crate_10_birds" to mapOf(
            "ar" to "10 طيور (50 طائر)",
            "fr" to "10 poulets (50)",
            "en" to "10 birds (50)",
            "es" to "10 aves (50)",
            "hi" to "10 मुर्गियाँ (50)"
        ),
        "add_weight_btn" to mapOf(
            "ar" to "إضافة الوزنة ➕",
            "fr" to "Ajouter la pesée ➕",
            "en" to "Add Weighing ➕",
            "es" to "Añadir pesada ➕",
            "hi" to "वजन जोड़ें ➕"
        ),
        "weights_list_title" to mapOf(
            "ar" to "قائمة الوزنات المسجلة:",
            "fr" to "Liste des pesées enregistrées :",
            "en" to "Recorded Weighings List:",
            "es" to "Lista de pesadas registradas:",
            "hi" to "दर्ज वजन सूची:"
        ),
        "net_meat_weight" to mapOf(
            "ar" to "صافي وزن الدجاج الحي",
            "fr" to "Poids net vif total",
            "en" to "Net Live Weight",
            "es" to "Peso neto vivo",
            "hi" to "शुद्ध जीवित वजन"
        ),
        "total_birds_sold" to mapOf(
            "ar" to "إجمالي الطيور المباعة",
            "fr" to "Total poulets vendus",
            "en" to "Total Birds Sold",
            "es" to "Total aves vendidas",
            "hi" to "कुल बेची गई मुर्गियाँ"
        ),
        "avg_bird_weight" to mapOf(
            "ar" to "معدل وزن الدجاجة الواحدة",
            "fr" to "Poids moyen par poulet",
            "en" to "Average Bird Weight",
            "es" to "Peso medio por ave",
            "hi" to "औसत मुर्गी का वजन"
        ),
        "price_per_kg_label" to mapOf(
            "ar" to "سعر الكيلوغرام في السوق اليوم (دج/كغ)",
            "fr" to "Prix du kg aujourd'hui (DZD/kg)",
            "en" to "Market Price per kg today (DZD/kg)",
            "es" to "Precio por kg hoy (DZD/kg)",
            "hi" to "आज का प्रति किग्रा बाजार भाव (DZD/kg)"
        ),
        "total_sales_value" to mapOf(
            "ar" to "إجمالي قيمة المبيعات",
            "fr" to "Montant total des ventes",
            "en" to "Total Sales Value",
            "es" to "Valor total de ventas",
            "hi" to "कुल बिक्री मूल्य"
        ),
        "btn_issue_receipt" to mapOf(
            "ar" to "🧾 فاتورة البيع",
            "fr" to "🧾 Facture de vente",
            "en" to "🧾 Sales Invoice",
            "es" to "🧾 Factura de venta",
            "hi" to "🧾 बिक्री बिल"
        ),
        "btn_issue_invoice" to mapOf(
            "ar" to "🧾 فاتورة البيع",
            "fr" to "🧾 Facture de vente",
            "en" to "🧾 Sales Invoice",
            "es" to "🧾 Factura de venta",
            "hi" to "🧾 बिक्री बिल"
        ),
        "btn_migrate_treasury" to mapOf(
            "ar" to "ترحيل الإيراد للخزنة 💰",
            "fr" to "Transférer en trésorerie 💰",
            "en" to "Transfer to Treasury 💰",
            "es" to "Transferir a tesorería 💰",
            "hi" to "खजाने में स्थानांतरित करें 💰"
        ),
        "receipt_dialog_title" to mapOf(
            "ar" to "فاتورة بيع دجاج اللحم الحي 🧾",
            "fr" to "Facture de vente poulet de chair 🧾",
            "en" to "Live Broiler Sales Invoice 🧾",
            "es" to "Factura de venta de pollo vivo 🧾",
            "hi" to "ब्रायलर बिक्री चालान 🧾"
        ),
        "receipt_migrated_success" to mapOf(
            "ar" to "تم حفظ فاتورة البيع وترحيل الإيراد للخزنة بنجاح! 💰🎉",
            "fr" to "Facture enregistrée et transférée en trésorerie avec succès ! 💰🎉",
            "en" to "Invoice saved and transferred to treasury successfully! 💰🎉",
            "es" to "¡Factura guardada y transferida a tesorería exitosamente! 💰🎉",
            "hi" to "चालान सहेजा गया और खजाने में सफलतापूर्वक दर्ज किया गया! 💰🎉"
        ),
        "weigh_number" to mapOf(
            "ar" to "وزنة",
            "fr" to "Pesée",
            "en" to "Weigh",
            "es" to "Pesada",
            "hi" to "वजन"
        ),
        "gross_short" to mapOf(
            "ar" to "ممتلئ B",
            "fr" to "Brut B",
            "en" to "Gross B",
            "es" to "Bruto B",
            "hi" to "सकल B"
        ),
        "tare_short" to mapOf(
            "ar" to "فارغ A",
            "fr" to "Tare A",
            "en" to "Tare A",
            "es" to "Tara A",
            "hi" to "टेयर A"
        ),
        "net_short" to mapOf(
            "ar" to "الصافي",
            "fr" to "Net",
            "en" to "Net",
            "es" to "Neto",
            "hi" to "शुद्ध"
        ),
        "birds_unit_short" to mapOf(
            "ar" to "طائر",
            "fr" to "poulets",
            "en" to "birds",
            "es" to "aves",
            "hi" to "मुर्गियाँ"
        ),
        "buyer_label" to mapOf(
            "ar" to "التاجر / المشتري:",
            "fr" to "Acheteur / Commerçant :",
            "en" to "Buyer / Merchant:",
            "es" to "Comprador / Comerciante:",
            "hi" to "व्यापारी / खरीदार:"
        ),
        "print_close_btn" to mapOf(
            "ar" to "طباعة وإغلاق الفاتورة",
            "fr" to "Imprimer & Fermer",
            "en" to "Print & Close Invoice",
            "es" to "Imprimir y Cerrar",
            "hi" to "प्रिंट और बंद करें"
        ),
        "btn_start_sales_flow" to mapOf(
            "ar" to "البيع 🍗",
            "fr" to "Vendre 🍗",
            "en" to "Sell 🍗",
            "es" to "Venta 🍗",
            "hi" to "बिक्री 🍗"
        ),
        "sales_stage_1_title" to mapOf(
            "ar" to "بيانات الشحنة والمشتري 📋",
            "fr" to "Détails du chargement & acheteur 📋",
            "en" to "Shipment & Buyer Details 📋",
            "es" to "Detalles del Envío y Comprador 📋",
            "hi" to "शिपमेंट और खरीदार का विवरण 📋"
        ),
        "sales_stage_2_title" to mapOf(
            "ar" to "جدول الميزان السريع ⚖️",
            "fr" to "Tableau de pesée rapide ⚖️",
            "en" to "Quick Weighing Table ⚖️",
            "es" to "Tabla de Pesaje Rápido ⚖️",
            "hi" to "त्वरित वजन तालिका ⚖️"
        ),
        "buyer_name_label" to mapOf(
            "ar" to "اسم المشتري / التاجر",
            "fr" to "Nom de l'acheteur / commerçant",
            "en" to "Buyer / Merchant Name",
            "es" to "Nombre del comprador",
            "hi" to "खरीदार / व्यापारी का नाम"
        ),
        "truck_plate_label" to mapOf(
            "ar" to "رقم لوحة الشاحنة",
            "fr" to "Immatriculation du camion",
            "en" to "Truck License Plate",
            "es" to "Matrícula del camión",
            "hi" to "ट्रक नंबर प्लेट"
        ),
        "bird_age_days_label" to mapOf(
            "ar" to "عمر الدجاج (بالأيام)",
            "fr" to "Âge des poulets (jours)",
            "en" to "Bird Age (days)",
            "es" to "Edad del ave (días)",
            "hi" to "मुर्गियों की आयु (दिन)"
        ),
        "btn_proceed_to_weighing" to mapOf(
            "ar" to "حفظ وبدء تسجيل الوزنات ➡️",
            "fr" to "Enregistrer & peser ➡️",
            "en" to "Save & Start Weighing ➡️",
            "es" to "Guardar y empezar pesaje ➡️",
            "hi" to "सहेजें और वजन शुरू करें ➡️"
        ),
        "save_single_weigh_btn" to mapOf(
            "ar" to "حفظ وزنة ✓",
            "fr" to "Valider pesée ✓",
            "en" to "Save Weigh ✓",
            "es" to "Guardar pesada ✓",
            "hi" to "वजन सहेजें ✓"
        ),
        "save_tare_weigh_btn" to mapOf(
            "ar" to "حفظ وزنة فارغ ✓",
            "fr" to "Valider tare ✓",
            "en" to "Save Tare ✓",
            "es" to "Guardar tara ✓",
            "hi" to "खाली वजन सहेजें ✓"
        ),
        "save_gross_weigh_btn" to mapOf(
            "ar" to "حفظ وزنة ممتلئ ✓",
            "fr" to "Valider brut ✓",
            "en" to "Save Gross ✓",
            "es" to "Guardar bruto ✓",
            "hi" to "भरा हुआ वजन सहेजें ✓"
        ),
        "btn_finalize_and_migrate" to mapOf(
            "ar" to "حفظ الفاتورة وترحيل للخزنة 💰",
            "fr" to "Enregistrer la facture & transférer 💰",
            "en" to "Save Invoice & Transfer to Treasury 💰",
            "es" to "Guardar factura y transferir a tesorería 💰",
            "hi" to "चालान सहेजें और खजाने में भेजें 💰"
        ),
        "sales_banner_desc" to mapOf(
            "ar" to "شحن الدفعة والتسليم المباشر للتاجر مع توثيق الأوزان بدقة وسرعة",
            "fr" to "Chargement du lot et livraison directe avec pesée rapide",
            "en" to "Batch loading and direct merchant delivery with fast weighing",
            "es" to "Carga del lote y entrega directa con pesaje rápido",
            "hi" to "तेज वजन के साथ बैच लोडिंग और सीधी व्यापारी डिलीवरी"
        ),
        "live_birds_count" to mapOf(
            "ar" to "🐔 عدد الدجاج الحي داخل العنبر",
            "fr" to "🐔 Poulets vivants dans le bâtiment",
            "en" to "🐔 Live Birds in Barn",
            "es" to "🐔 Aves vivas en el galpón",
            "hi" to "🐔 शेड में जीवित मुर्गियाँ"
        ),
        "total_barn_feed" to mapOf(
            "ar" to "🌾 إجمالي العلف المستهلك",
            "fr" to "🌾 Total aliment consommé",
            "en" to "🌾 Total Feed Consumed",
            "es" to "🌾 Alimento total consumido",
            "hi" to "🌾 कुल खपत दाना"
        ),
        "feed_per_bird" to mapOf(
            "ar" to "🥣 استهلاك الدجاجة الواحدة من العلف",
            "fr" to "🥣 Conso. moyenne par poulet",
            "en" to "🥣 Avg Feed Consumption per Bird",
            "es" to "🥣 Consumo medio por ave",
            "hi" to "🥣 प्रति मुर्गी औसत दाना खपत"
        ),
        "cost_per_bird" to mapOf(
            "ar" to "🐓 تكلفة الدجاجة الواحدة",
            "fr" to "🐓 Coût par poulet",
            "en" to "🐓 Cost per Bird",
            "es" to "🐓 Costo por ave",
            "hi" to "🐓 प्रति मुर्गी लागत"
        ),
        "bird_unit" to mapOf(
            "ar" to "طائر",
            "fr" to "sujet",
            "en" to "bird",
            "es" to "ave",
            "hi" to "पक्षी"
        ),
        "tare_mode_label" to mapOf(
            "ar" to "طريقة وزن وتحديد الصناديق الفارغة (التارا):",
            "fr" to "Mode de pesée des caisses vides (Tare) :",
            "en" to "Empty Crates Tare Method:",
            "es" to "Método de pesaje de cajas vacías (Tara):",
            "hi" to "खाली बक्से का वजन (टेयर) तरीका:"
        ),
        "tare_mode_detailed" to mapOf(
            "ar" to "الخيار (أ): تسجيل وزنات الصناديق الفارغة بالتفصيل (وزنة بوزنة)",
            "fr" to "Option A : Pesée détaillée des caisses vides (pesée par pesée)",
            "en" to "Option A: Record Empty Crates in Detail (Weigh by Weigh)",
            "es" to "Opción A: Registrar cajas vacías en detalle (pesada por pesada)",
            "hi" to "विकल्प A: विस्तार से खाली बक्से का वजन दर्ज करें"
        ),
        "tare_mode_direct" to mapOf(
            "ar" to "الخيار (ب): إدخال إجمالي وزن الصناديق الفارغة مباشرة كرقم واحد",
            "fr" to "Option B : Saisir le poids total des caisses vides directement",
            "en" to "Option B: Enter Total Empty Tare Weight Directly as Single Value",
            "es" to "Opción B: Ingresar el peso total de cajas vacías directamente",
            "hi" to "विकल्प B: सीधे कुल खाली वजन एक संख्या के रूप में दर्ज करें"
        ),
        "tare_phase_title" to mapOf(
            "ar" to "المرحلة 1: إدخال وزنات الصناديق الفارغة 📦",
            "fr" to "Étape 1 : Saisie des caisses vides 📦",
            "en" to "Stage 1: Enter Empty Crates Weights 📦",
            "es" to "Fase 1: Entrada de cajas vacías 📦",
            "hi" to "चरण 1: खाली बक्से का वजन दर्ज करें 📦"
        ),
        "gross_phase_title" to mapOf(
            "ar" to "المرحلة 2: إدخال وزنات الصناديق الممتلئة بالدجاج 🐔",
            "fr" to "Étape 2 : Saisie des caisses pleines de poulets 🐔",
            "en" to "Stage 2: Enter Full Chicken Crates Weights 🐔",
            "es" to "Fase 2: Entrada de cajas llenas de pollos 🐔",
            "hi" to "चरण 2: भरे बक्सों का वजन दर्ज करें 🐔"
        ),
        "finish_tare_proceed_btn" to mapOf(
            "ar" to "تم الانتهاء من الفارغ ➡️ انتقل لوزن الصناديق الممتلئة",
            "fr" to "Tare terminée ➡️ Passer au chargement des poulets",
            "en" to "Tare Finished ➡️ Proceed to Full Crates Weighing",
            "es" to "Tara finalizada ➡️ Pasar al pesaje de pollos",
            "hi" to "खाली वजन पूरा ➡️ भरी हुई मुर्गियों के वजन पर जाएं"
        ),
        "birds_per_crate_free_hint" to mapOf(
            "ar" to "عدد الدجاج في الصندوق الواحد (مثلاً: 8 أو 9 أو 10 أو 12)",
            "fr" to "Poulets par caisse (ex: 8, 9, 10 ou 12)",
            "en" to "Birds per single crate (e.g.: 8, 9, 10 or 12)",
            "es" to "Aves por caja (ej: 8, 9, 10 o 12)",
            "hi" to "प्रति क्रेट मुर्गियाँ (उदा: 8, 9, 10 या 12)"
        ),
        "tare_direct_total_label" to mapOf(
            "ar" to "إجمالي وزن الصناديق الفارغة مباشرة (كغ)",
            "fr" to "Poids total des caisses vides directement (kg)",
            "en" to "Total Empty Crates Weight Directly (kg)",
            "es" to "Peso total de cajas vacías directamente (kg)",
            "hi" to "खाली बक्सों का कुल वजन सीधे (kg)"
        ),
        "accumulated_tare_label" to mapOf(
            "ar" to "إجمالي الفارغ (A)",
            "fr" to "Total Tare (A)",
            "en" to "Total Tare (A)",
            "es" to "Total Tara (A)",
            "hi" to "कुल टेयर (A)"
        ),
        "total_gross_b_label" to mapOf(
            "ar" to "إجمالي الممتلئ (B)",
            "fr" to "Total Brut (B)",
            "en" to "Total Gross (B)",
            "es" to "Total Bruto (B)",
            "hi" to "कुल सकल (B)"
        ),
        "actual_net_label" to mapOf(
            "ar" to "الصافي الفعلي (B - A)",
            "fr" to "Net Réel (B - A)",
            "en" to "Actual Net (B - A)",
            "es" to "Neto Real (B - A)",
            "hi" to "वास्तविक शुद्ध (B - A)"
        ),
        "tab_tare_a" to mapOf(
            "ar" to "📦 وزن الصناديق الفارغة (A)",
            "fr" to "📦 Pesée caisses vides (A)",
            "en" to "📦 Empty Crates Weighing (A)",
            "es" to "📦 Pesaje cajas vacías (A)",
            "hi" to "📦 खाली बक्सों का वजन (A)"
        ),
        "tab_gross_b" to mapOf(
            "ar" to "🐔 وزن الصناديق الممتلئة (B)",
            "fr" to "🐔 Pesée caisses pleines (B)",
            "en" to "🐔 Full Crates Weighing (B)",
            "es" to "🐔 Pesaje cajas llenas (B)",
            "hi" to "🐔 भरे बक्सों का वजन (B)"
        ),
        "save_tare_a_btn" to mapOf(
            "ar" to "حفظ وزنة فارغ A ✓",
            "fr" to "Enregistrer Tare A ✓",
            "en" to "Save Tare A ✓",
            "es" to "Guardar Tara A ✓",
            "hi" to "खाली वजन A सहेजें ✓"
        ),
        "save_gross_b_btn" to mapOf(
            "ar" to "حفظ وزنة ممتلئ B ✓",
            "fr" to "Enregistrer Brut B ✓",
            "en" to "Save Gross B ✓",
            "es" to "Guardar Bruto B ✓",
            "hi" to "सकल वजन B सहेजें ✓"
        ),
        "switch_to_gross_btn" to mapOf(
            "ar" to "انتهيت من الفارغ ➡️ انتقل لوزن الممتلئ B",
            "fr" to "Terminé avec la tare ➡️ Passer au plein B",
            "en" to "Done with Tare ➡️ Proceed to Full B",
            "es" to "Listo con tara ➡️ Pasar a llenas B",
            "hi" to "खाली पूरा ➡️ भरे वजन B पर जाएं"
        ),
        "btn_save_invoice_and_migrate" to mapOf(
            "ar" to "حفظ الفاتورة وترحيل للخزنة 💰",
            "fr" to "Enregistrer facture et migrer en caisse 💰",
            "en" to "Save Invoice & Transfer to Cash 💰",
            "es" to "Guardar factura y transferir a caja 💰",
            "hi" to "चालान सहेजें और नकद में स्थानांतरित करें 💰"
        ),
        "btn_new_cycle" to mapOf(
            "ar" to "دورة جديدة 🐣",
            "fr" to "Nouveau cycle 🐣",
            "en" to "New Cycle 🐣",
            "es" to "Nuevo Ciclo 🐣",
            "hi" to "नया चक्र 🐣"
        ),
        "dialog_new_cycle_title" to mapOf(
            "ar" to "إنشاء دورة وعنبر جديد 🐣",
            "fr" to "Créer un nouveau cycle 🐣",
            "en" to "Create New Farm Cycle 🐣",
            "es" to "Crear Nuevo Ciclo 🐣",
            "hi" to "नया फार्म चक्र बनाएं 🐣"
        ),
        "field_farm_name" to mapOf(
            "ar" to "اسم العنبر / المزرعة",
            "fr" to "Nom du bâtiment / ferme",
            "en" to "Farm / Barn Name",
            "es" to "Nombre de la granja",
            "hi" to "फार्म का नाम"
        ),
        "field_location" to mapOf(
            "ar" to "الموقع الجغرافي",
            "fr" to "Localisation",
            "en" to "Location",
            "es" to "Ubicación",
            "hi" to "स्थान"
        ),
        "field_bird_count" to mapOf(
            "ar" to "عدد الكتاكيت المستلمة (العدد)",
            "fr" to "Nombre de poussins",
            "en" to "Initial Bird Count",
            "es" to "Cantidad de aves",
            "hi" to "चूजों की संख्या"
        ),
        "field_chick_price" to mapOf(
            "ar" to "سعر الكتكوت الواحد (دج)",
            "fr" to "Prix par poussin (DZD)",
            "en" to "Price Per Chick (DZD)",
            "es" to "Precio por pollito (DZD)",
            "hi" to "प्रति चूजा मूल्य (DZD)"
        ),
        "start_date_label" to mapOf(
            "ar" to "تاريخ البدء",
            "fr" to "Date de début",
            "en" to "Start Date",
            "es" to "Fecha de inicio",
            "hi" to "आरंभ तिथि"
        ),
        "badge_estimated_weight" to mapOf(
            "ar" to "الوزن التقديري",
            "fr" to "Poids estimé",
            "en" to "Est. Weight",
            "es" to "Peso est.",
            "hi" to "अनुमानित वजन"
        ),
        "live_birds_label" to mapOf(
            "ar" to "الطيور الحية",
            "fr" to "Sujets vivants",
            "en" to "Live Birds",
            "es" to "Aves vivas",
            "hi" to "जीवित पक्षी"
        ),
        "btn_create_cycle" to mapOf(
            "ar" to "بدء وحفظ الدورة 🚀",
            "fr" to "Créer & Démarrer 🚀",
            "en" to "Create & Start Cycle 🚀",
            "es" to "Crear e Iniciar 🚀",
            "hi" to "शुरू करें 🚀"
        ),
        "chick_expense_note" to mapOf(
            "ar" to "توليد تلقائي لأول مصروف في الخزنة",
            "fr" to "Génération automatique de la 1ère dépense",
            "en" to "Auto-generates first treasury expense",
            "es" to "Genera automáticamente el 1er gasto",
            "hi" to "खजाने का पहला खर्च स्वतः दर्ज होगा"
        ),
        "no_active_cycle" to mapOf(
            "ar" to "لا توجد دورة نشطة",
            "fr" to "Aucun cycle actif",
            "en" to "No active cycle",
            "es" to "No hay ciclo activo",
            "hi" to "कोई सक्रिय चक्र नहीं"
        ),
        "no_active_cycle_desc" to mapOf(
            "ar" to "هذا العنبر جاهز ومطهر لاستقبال دفعة صيصان جديدة",
            "fr" to "Ce bâtiment est prêt à recevoir un nouveau lot",
            "en" to "This barn is sanitized and ready for a new flock",
            "es" to "Esta nave está lista para recibir un nuevo lote",
            "hi" to "यह बार्न नए झुंड के लिए तैयार है"
        ),
        "btn_start_new_cycle" to mapOf(
            "ar" to "بدء دورة جديدة 🐣",
            "fr" to "Démarrer un nouveau cycle 🐣",
            "en" to "Start New Cycle 🐣",
            "es" to "Iniciar Nuevo Ciclo 🐣",
            "hi" to "नया चक्र शुरू करें 🐣"
        ),
        "livability_label" to mapOf(
            "ar" to "الحيوية",
            "fr" to "Viabilité",
            "en" to "Livability",
            "es" to "Viabilidad",
            "hi" to "जीवित रहने की दर"
        ),
        "farm_3_name" to mapOf(
            "ar" to "عنبر الهضاب الحديث",
            "fr" to "Bâtiment Hauts-Plateaux",
            "en" to "Highlands Modern Barn",
            "es" to "Nave Moderna Altiplano",
            "hi" to "हाईलैंड्स आधुनिक बार्न"
        ),
        "farm_3_location" to mapOf(
            "ar" to "سطيف - عين أرنات",
            "fr" to "Sétif - Aïn Arnat",
            "en" to "Setif - Ain Arnat",
            "es" to "Sétif - Aïn Arnat",
            "hi" to "सेतिफ - ऐन अरनात"
        ),
        "feed_inventory_label" to mapOf(
            "ar" to "مخزون العلف",
            "fr" to "Stock d'aliment",
            "en" to "Feed Inventory",
            "es" to "Inventario de pienso",
            "hi" to "चारा स्टॉक"
        ),
        "feed_designation_label" to mapOf(
            "ar" to "تسمية / نوع العلف (مثلاً: علف بادئ)",
            "fr" to "Désignation de l'aliment (ex: Démarrage)",
            "en" to "Feed Designation (e.g., Starter Feed)",
            "es" to "Designación del pienso (ej: Inicio)",
            "hi" to "चारे का नाम (जैसे: स्टार्टर फ़ीड)"
        ),
        "feed_designation_default" to mapOf(
            "ar" to "علف بادئ",
            "fr" to "Aliment Démarrage",
            "en" to "Starter Feed",
            "es" to "Pienso de Inicio",
            "hi" to "स्टार्टर फ़ीड"
        ),
        "feed_qty_quintals_label" to mapOf(
            "ar" to "الكمية بالقنطار (مثلاً: 40)",
            "fr" to "Quantité en Quintaux (ex: 40)",
            "en" to "Quantity in Quintals (e.g., 40)",
            "es" to "Cantidad en Quintales (ej: 40)",
            "hi" to "मात्रा क्विंटल में (जैसे: 40)"
        ),
        "feed_price_quintal_label" to mapOf(
            "ar" to "سعر القنطار الواحد (مثلاً: 8000)",
            "fr" to "Prix unitaire par Quintal (ex: 8000)",
            "en" to "Unit Price per Quintal (e.g., 8000)",
            "es" to "Precio por Quintal (ej: 8000)",
            "hi" to "प्रति क्विंटल मूल्य (जैसे: 8000)"
        ),
        "feed_total_auto_label" to mapOf(
            "ar" to "المبلغ الإجمالي المحسوب تلقائياً",
            "fr" to "Montant total calculé automatiquement",
            "en" to "Auto-Calculated Total Amount",
            "es" to "Importe total calculado automáticamente",
            "hi" to "स्वतः गणना की गई कुल राशि"
        ),
        "quintal_unit" to mapOf(
            "ar" to "قنطار",
            "fr" to "q",
            "en" to "qtl",
            "es" to "q",
            "hi" to "क्विंटल"
        ),
        "feed_low_alert" to mapOf(
            "ar" to "تنبيه: مخزون العلف منخفض (< 500 كجم)",
            "fr" to "Alerte: Stock faible (< 500 kg)",
            "en" to "Alert: Low Feed Inventory (< 500 kg)",
            "es" to "Alerta: Inventario bajo (< 500 kg)",
            "hi" to "चेतावनी: कम चारा स्टॉक (< 500 किग्रा)"
        ),
        "feed_badge_short" to mapOf(
            "ar" to "مخزون",
            "fr" to "Stock",
            "en" to "Feed",
            "es" to "Stock",
            "hi" to "स्टॉक"
        ),
        "kg_short" to mapOf(
            "ar" to "كغ",
            "fr" to "kg",
            "en" to "kg",
            "es" to "kg",
            "hi" to "किग्रा"
        ),
        "feed_orders_ledger_title" to mapOf(
            "ar" to "سجل طلبيات العلف",
            "fr" to "Registre des Commandes d'Aliment",
            "en" to "Feed Orders Ledger",
            "es" to "Registro de Pedidos de Pienso",
            "hi" to "चारा ऑर्डर रजिस्टर"
        ),
        "total_feed_purchased_label" to mapOf(
            "ar" to "إجمالي العلف المشترى",
            "fr" to "Total Aliment Acheté",
            "en" to "Total Feed Purchased",
            "es" to "Total Pienso Comprado",
            "hi" to "कुल खरीदा गया चारा"
        ),
        "current_remaining_inventory_label" to mapOf(
            "ar" to "المخزون المتبقي الحالي",
            "fr" to "Stock Restant Actuel",
            "en" to "Current Remaining Inventory",
            "es" to "Inventario Restante Actual",
            "hi" to "वर्तमान शेष स्टॉक"
        ),
        "th_purchase_date" to mapOf(
            "ar" to "التاريخ",
            "fr" to "Date",
            "en" to "Date",
            "es" to "Fecha",
            "hi" to "तिथि"
        ),
        "th_feed_designation" to mapOf(
            "ar" to "نوع العلف",
            "fr" to "Aliment",
            "en" to "Feed Name",
            "es" to "Pienso",
            "hi" to "चारा नाम"
        ),
        "th_feed_quantity" to mapOf(
            "ar" to "الكمية",
            "fr" to "Quantité",
            "en" to "Quantity",
            "es" to "Cantidad",
            "hi" to "मात्रा"
        ),
        "th_feed_total_price" to mapOf(
            "ar" to "السعر الإجمالي",
            "fr" to "Prix Total",
            "en" to "Total Price",
            "es" to "Precio Total",
            "hi" to "कुल मूल्य"
        ),
        "no_feed_orders_yet" to mapOf(
            "ar" to "لا توجد طلبيات علف مسجلة في هذه الدورة بعد",
            "fr" to "Aucune commande d'aliment enregistrée pour ce cycle",
            "en" to "No feed orders recorded for this cycle yet",
            "es" to "No hay pedidos de pienso registrados en este ciclo",
            "hi" to "इस चक्र के लिए अभी तक कोई चारा ऑर्डर दर्ज नहीं है"
        ),
        "official_receipt_header" to mapOf(
            "ar" to "وصل معاملة معتمد",
            "fr" to "Reçu de Transaction Officiel",
            "en" to "Official Transaction Receipt",
            "es" to "Recibo Oficial de Transacción",
            "hi" to "आधिकारिक लेनदेन रसीद"
        ),
        "official_expense_doc" to mapOf(
            "ar" to "وثيقة مصروف معتمدة 💸",
            "fr" to "Document de Dépense Certifié 💸",
            "en" to "Verified Expense Document 💸",
            "es" to "Documento de Gasto Verificado 💸",
            "hi" to "प्रमाणित व्यय दस्तावेज़ 💸"
        ),
        "official_income_doc" to mapOf(
            "ar" to "وثيقة إيراد معتمدة 💰",
            "fr" to "Document de Revenu Certifié 💰",
            "en" to "Verified Income Document 💰",
            "es" to "Documento de Ingreso Verificado 💰",
            "hi" to "प्रमाणित आय दस्तावेज़ 💰"
        ),
        "receipt_footer_verified" to mapOf(
            "ar" to "تم التسجيل والاعتماد في دفتر المزرعة",
            "fr" to "Enregistré et vérifié dans le registre de la ferme",
            "en" to "Recorded & Verified in Farm Ledger",
            "es" to "Registrado y verificado en el libro de la granja",
            "hi" to "फ़ार्म लेज़र में दर्ज और सत्यापित"
        ),
        "share_receipt_btn" to mapOf(
            "ar" to "مشاركة الوصل 📤",
            "fr" to "Partager le Reçu 📤",
            "en" to "Share Receipt 📤",
            "es" to "Compartir Recibo 📤",
            "hi" to "रसीद साझा करें 📤"
        ),
        "receipt_ref_label" to mapOf(
            "ar" to "رقم الوصل المرجعي",
            "fr" to "Référence du reçu",
            "en" to "Receipt Reference",
            "es" to "Referencia del recibo",
            "hi" to "रसीद संदर्भ संख्या"
        ),
        "receipt_farm_label" to mapOf(
            "ar" to "المنشأة / العنبر",
            "fr" to "Bâtiment / Ferme",
            "en" to "Farm / Barn",
            "es" to "Granja / Nave",
            "hi" to "फ़ार्म / बार्न"
        ),
        "receipt_shared_confirm" to mapOf(
            "ar" to "✓ تم تجهيز ومشاركة الوصل الرقمي بنسيق واتساب بنجاح",
            "fr" to "✓ Reçu numérique formaté pour WhatsApp prêt à partager",
            "en" to "✓ Digital receipt formatted for WhatsApp shared",
            "es" to "✓ Recibo digital formateado para WhatsApp listo",
            "hi" to "✓ व्हाट्सएप के लिए डिजिटल रसीद तैयार है"
        ),
        "tap_for_digital_receipt" to mapOf(
            "ar" to "🧾 انقر لعرض ومشاركة وصل المعاملة المعتمد",
            "fr" to "🧾 Appuyez pour voir et partager le reçu officiel",
            "en" to "🧾 Tap to view & share official digital receipt",
            "es" to "🧾 Toque para ver y compartir el recibo oficial",
            "hi" to "🧾 आधिकारिक रसीद देखने और साझा करने के लिए टैप करें"
        ),
        "tab_chat" to mapOf(
            "ar" to "الدردشة",
            "fr" to "Discussion",
            "en" to "Chat",
            "es" to "Chat",
            "hi" to "चैट"
        ),
        "chat_input_hint" to mapOf(
            "ar" to "اكتب رسالة...",
            "fr" to "Écrire un message...",
            "en" to "Type a message...",
            "es" to "Escribe un mensaje...",
            "hi" to "संदेश लिखें..."
        ),
        "chat_role_owner" to mapOf(
            "ar" to "مالك المزرعة 👨‍🌾",
            "fr" to "Propriétaire de la Ferme 👨‍🌾",
            "en" to "Farm Owner 👨‍🌾",
            "es" to "Propietario de la Granja 👨‍🌾",
            "hi" to "फ़ार्म मालिक 👨‍🌾"
        ),
        "chat_role_vet" to mapOf(
            "ar" to "د. كمال - طبيب بيطري 🩺",
            "fr" to "Dr. Kamel - Vétérinaire 🩺",
            "en" to "Dr. Kamel - Veterinarian 🩺",
            "es" to "Dr. Kamel - Veterinario 🩺",
            "hi" to "डॉ. कमाल - पशु चिकित्सक 🩺"
        ),
        "chat_role_worker" to mapOf(
            "ar" to "أحمد - مشرف العنبر 👷‍♂️",
            "fr" to "Ahmed - Superviseur 👷‍♂️",
            "en" to "Ahmed - Barn Worker 👷‍♂️",
            "es" to "Ahmed - Supervisor 👷‍♂️",
            "hi" to "अहमद - बार्न पर्यवेक्षक 👷‍♂️"
        ),
        "chat_worker_title" to mapOf(
            "ar" to "محادثة مشرف العنبر 👷",
            "fr" to "Discussion Superviseur 👷",
            "en" to "Worker Chat 👷",
            "es" to "Chat del Supervisor 👷",
            "hi" to "पर्यवेक्षक चैट 👷"
        ),
        "chat_vet_title" to mapOf(
            "ar" to "استشارة الطبيب البيطري 👨‍⚕️",
            "fr" to "Consultation Vétérinaire 👨‍⚕️",
            "en" to "Vet Consultation 👨‍⚕️",
            "es" to "Consulta Veterinaria 👨‍⚕️",
            "hi" to "पशु चिकित्सक परामर्श 👨‍⚕️"
        ),
        "chat_general_admin_title" to mapOf(
            "ar" to "الدردشة الإدارية العامة",
            "fr" to "Discussion Administrative Générale",
            "en" to "General Admin Chat",
            "es" to "Chat Administrativo General",
            "hi" to "सामान्य प्रशासनिक चैट"
        ),
        "global_tab_barns" to mapOf(
            "ar" to "🏠 العنابر",
            "fr" to "🏠 Bâtiments",
            "en" to "🏠 Barns",
            "es" to "🏠 Naves",
            "hi" to "🏠 बार्न"
        ),
        "global_tab_chat" to mapOf(
            "ar" to "💬 الدردشة",
            "fr" to "💬 Discussion",
            "en" to "💬 Chat",
            "es" to "💬 Chat",
            "hi" to "💬 चैट"
        ),
        "global_tab_market" to mapOf(
            "ar" to "🛒 السوق",
            "fr" to "🛒 Marché",
            "en" to "🛒 Market",
            "es" to "🛒 Mercado",
            "hi" to "🛒 बाज़ार"
        ),
        "global_tab_ai_vet" to mapOf(
            "ar" to "🤖 AI بيطري",
            "fr" to "🤖 IA Vétérinaire",
            "en" to "🤖 AI Vet",
            "es" to "🤖 IA Veterinario",
            "hi" to "🤖 AI पशु चिकित्सक"
        ),
        "btn_add_post" to mapOf(
            "ar" to "إضافة إعلان",
            "fr" to "Ajouter une annonce",
            "en" to "Add Post",
            "es" to "Añadir anuncio",
            "hi" to "विज्ञापन जोड़ें"
        ),
        "btn_contact" to mapOf(
            "ar" to "تواصل",
            "fr" to "Contacter",
            "en" to "Contact",
            "es" to "Contactar",
            "hi" to "संपर्क करें"
        ),
        "dialog_add_post_title" to mapOf(
            "ar" to "إضافة إعلان جديد في السوق 🛒",
            "fr" to "Nouvelle annonce sur le marché 🛒",
            "en" to "Add New Market Post 🛒",
            "es" to "Nuevo anuncio en el mercado 🛒",
            "hi" to "बाज़ार में नया विज्ञापन जोड़ें 🛒"
        ),
        "field_post_title" to mapOf(
            "ar" to "عنوان الإعلان (مثال: دجاج لحم واجد للبيع)",
            "fr" to "Titre de l'annonce",
            "en" to "Post Title",
            "es" to "Título del anuncio",
            "hi" to "विज्ञापन शीर्षक"
        ),
        "field_post_desc" to mapOf(
            "ar" to "تفاصيل الإعلان (الكمية، الوزن، الموقع...)",
            "fr" to "Description de l'annonce",
            "en" to "Post Description",
            "es" to "Descripción del anuncio",
            "hi" to "विज्ञापन विवरण"
        ),
        "field_post_price" to mapOf(
            "ar" to "السعر (دج)",
            "fr" to "Prix (DZD)",
            "en" to "Price (DZD)",
            "es" to "Precio (DZD)",
            "hi" to "मूल्य (DZD)"
        ),
        "btn_upload_image" to mapOf(
            "ar" to "رفع صورة الإعلان 📷",
            "fr" to "Télécharger une image 📷",
            "en" to "Upload Image 📷",
            "es" to "Subir imagen 📷",
            "hi" to "छवि अपलोड करें 📷"
        ),
        "btn_publish_post" to mapOf(
            "ar" to "نشر الإعلان ✓",
            "fr" to "Publier l'annonce ✓",
            "en" to "Publish Post ✓",
            "es" to "Publicar anuncio ✓",
            "hi" to "विज्ञापन प्रकाशित करें ✓"
        ),
        "feed_unit_pref_title" to mapOf(
            "ar" to "وحدة قياس العلف المفضلة",
            "fr" to "Unité d'aliment préférée",
            "en" to "Preferred Feed Unit",
            "es" to "Unidad de pienso preferida",
            "hi" to "पसंदीदा चारा इकाई"
        ),
        "feed_unit_kg_option" to mapOf(
            "ar" to "كيلوغرام",
            "fr" to "Kilogramme",
            "en" to "Kilogram",
            "es" to "Kilogramo",
            "hi" to "किलोग्राम"
        ),
        "feed_unit_quintal_option" to mapOf(
            "ar" to "قنطار",
            "fr" to "Quintal",
            "en" to "Quintal",
            "es" to "Quintal",
            "hi" to "क्विंटल"
        ),
        "quintal_short" to mapOf(
            "ar" to "ق",
            "fr" to "q",
            "en" to "q",
            "es" to "q",
            "hi" to "क्विं."
        ),
        "btn_add_team" to mapOf(
            "ar" to "إضافة فريق",
            "fr" to "Ajouter équipe",
            "en" to "Add Team",
            "es" to "Añadir equipo",
            "hi" to "टीम जोड़ें"
        ),
        "qr_invite_sheet_title" to mapOf(
            "ar" to "دعوة فريق العمل (QR)",
            "fr" to "Inviter l'équipe (QR)",
            "en" to "Invite Team via QR",
            "es" to "Invitar equipo (QR)",
            "hi" to "QR द्वारा टीम को आमंत्रित करें"
        ),
        "qr_role_worker_btn" to mapOf(
            "ar" to "👷 مشرف عنبر",
            "fr" to "👷 Superviseur",
            "en" to "👷 Barn Worker",
            "es" to "👷 Supervisor",
            "hi" to "👷 बार्न पर्यवेक्षक"
        ),
        "qr_role_vet_btn" to mapOf(
            "ar" to "👨‍⚕️ طبيب بيطري",
            "fr" to "👨‍⚕️ Vétérinaire",
            "en" to "👨‍⚕️ Veterinarian",
            "es" to "👨‍⚕️ Veterinario",
            "hi" to "👨‍⚕️ पशु चिकित्सक"
        ),
        "qr_invite_instruction" to mapOf(
            "ar" to "اطلب من العامل أو البيطري مسح هذا الرمز من تطبيقه للانضمام فوراً للعنبر.",
            "fr" to "Demandez au superviseur ou au vétérinaire de scanner ce code depuis son application pour rejoindre immédiatement le bâtiment.",
            "en" to "Ask the worker or vet to scan this code from their app to immediately join the barn.",
            "es" to "Pida al trabajador o veterinario que escanee este código desde su aplicación para unirse inmediatamente a la nave.",
            "hi" to "कर्मचारी या पशु चिकित्सक से तुरंत बार्न में शामिल होने के लिए अपने ऐप से इस कोड को स्कैन करने के लिए कहें।"
        ),
        "btn_scan_qr_join" to mapOf(
            "ar" to "مسح رمز الانضمام 📷",
            "fr" to "Scanner le code QR 📷",
            "en" to "Scan QR to Join 📷",
            "es" to "Escanear QR para unirse 📷",
            "hi" to "शामिल होने के लिए QR स्कैन करें 📷"
        ),
        "qr_scanner_sheet_title" to mapOf(
            "ar" to "مسح رمز الانضمام للعنبر 📷",
            "fr" to "Scanner le code d'invitation 📷",
            "en" to "Scan Barn Invite QR 📷",
            "es" to "Escanear QR de invitación 📷",
            "hi" to "बार्न आमंत्रण QR स्कैन करें 📷"
        ),
        "qr_scanner_desc" to mapOf(
            "ar" to "وجّه الكاميرا نحو رمز (QR) في هاتف مالك المزرعة للانضمام الفوري وتفعيل الصلاحيات تلقائياً.",
            "fr" to "Pointez la caméra vers le code QR du propriétaire pour rejoindre la ferme instantanément.",
            "en" to "Point the camera at the Farm Owner's QR code for instant onboarding and role activation.",
            "es" to "Apunte la cámara al código QR del propietario para unirse al instante.",
            "hi" to "तुरंत जुड़ने के लिए फार्म मालिक के QR कोड की ओर कैमरा करें।"
        ),
        "btn_send_vet_invoice" to mapOf(
            "ar" to "إرسال فاتورة دواء 🧾",
            "fr" to "Envoyer facture médicament 🧾",
            "en" to "Send Medicine Invoice 🧾",
            "es" to "Enviar factura de medicina 🧾",
            "hi" to "दवा चालान भेजें 🧾"
        ),
        "vet_invoice_dialog_title" to mapOf(
            "ar" to "إرسال فاتورة دواء ووصفة بيطرية 💊🧾",
            "fr" to "Facture de Médicament & Ordonnance 💊🧾",
            "en" to "Medicine Invoice & Vet Prescription 💊🧾",
            "es" to "Factura de Medicamento y Receta 💊🧾",
            "hi" to "दवा चालान और पशु चिकित्सा पर्ची 💊🧾"
        ),
        "vet_invoice_med_label" to mapOf(
            "ar" to "اسم الدواء أو التحصين 💊",
            "fr" to "Nom du médicament / vaccin 💊",
            "en" to "Medicine / Vaccine Name 💊",
            "es" to "Nombre del medicamento 💊",
            "hi" to "दवा / टीके का नाम 💊"
        ),
        "vet_invoice_dosage_label" to mapOf(
            "ar" to "الجرعة وطريقة الاستعمال 🧪",
            "fr" to "Posologie et mode d'emploi 🧪",
            "en" to "Dosage & Instructions 🧪",
            "es" to "Dosis e instrucciones 🧪",
            "hi" to "खुराक और निर्देश 🧪"
        ),
        "vet_invoice_note_label" to mapOf(
            "ar" to "ملاحظة التشخيص البيطري 🩺",
            "fr" to "Diagnostic vétérinaire 🩺",
            "en" to "Clinical Diagnosis Note 🩺",
            "es" to "Nota de diagnóstico 🩺",
            "hi" to "पशु चिकित्सा निदान नोट 🩺"
        ),
        "vet_invoice_amount_label" to mapOf(
            "ar" to "مبلغ فاتورة الدواء (دج) 💰",
            "fr" to "Montant de la facture (DZD) 💰",
            "en" to "Invoice Amount (DZD) 💰",
            "es" to "Monto de la factura (DZD) 💰",
            "hi" to "चालान राशि (DZD) 💰"
        ),
        "vet_invoice_urgent_label" to mapOf(
            "ar" to "وصفة علاجية عاجلة 🚨",
            "fr" to "Ordonnance urgente 🚨",
            "en" to "Urgent Prescription 🚨",
            "es" to "Receta urgente 🚨",
            "hi" to "तत्काल पर्ची 🚨"
        ),
        "vet_invoice_confirm_btn" to mapOf(
            "ar" to "إرسال واعتماد الفاتورة 🧾",
            "fr" to "Envoyer et valider la facture 🧾",
            "en" to "Send & Record Invoice 🧾",
            "es" to "Enviar y registrar factura 🧾",
            "hi" to "चालान भेजें और दर्ज करें 🧾"
        ),
        "universal_user_mode_general" to mapOf(
            "ar" to "👤 مستخدم عام (بدون عنبر)",
            "fr" to "👤 Utilisateur (Sans bâtiment)",
            "en" to "👤 General User (No Barn)",
            "es" to "👤 Usuario General",
            "hi" to "👤 सामान्य उपयोगकर्ता"
        ),
        "universal_user_mode_owner" to mapOf(
            "ar" to "👨‍🌾 مالك المزرعة",
            "fr" to "👨‍🌾 Propriétaire",
            "en" to "👨‍🌾 Farm Owner",
            "es" to "👨‍🌾 Propietario",
            "hi" to "👨‍🌾 फार्म मालिक"
        ),
        "universal_user_mode_vet" to mapOf(
            "ar" to "👨‍⚕️ بيطري (QR)",
            "fr" to "👨‍⚕️ Vétérinaire (QR)",
            "en" to "👨‍⚕️ Vet (QR)",
            "es" to "👨‍⚕️ Veterinario (QR)",
            "hi" to "👨‍⚕️ पशु चिकित्सक (QR)"
        ),
        "universal_user_mode_worker" to mapOf(
            "ar" to "👷 مشرف (QR)",
            "fr" to "👷 Superviseur (QR)",
            "en" to "👷 Worker (QR)",
            "es" to "👷 Supervisor (QR)",
            "pt" to "👷 Supervisor (QR)",
            "hi" to "👷 पर्यवेक्षक (QR)"
        ),
        // Global SaaS Onboarding & Freemium Paywall Keys
        "onboarding_badge" to mapOf(
            "ar" to "🌍 إعداد المنصة العالمية",
            "fr" to "🌍 Configuration Globale SaaS",
            "en" to "🌍 Global SaaS Setup",
            "es" to "🌍 Configuración Global SaaS",
            "pt" to "🌍 Configuração Global SaaS",
            "hi" to "🌍 वैश्विक प्लेटफ़ॉर्म सेटअप"
        ),
        "onboarding_title" to mapOf(
            "ar" to "مرحباً بك في دفتر إدارة الدواجن العالمي 🐥",
            "fr" to "Bienvenue sur Gestion Avicole Globale 🐥",
            "en" to "Welcome to Global Poultry Manager 🐥",
            "es" to "Bienvenido a Gestión Avícola Global 🐥",
            "pt" to "Bem-vindo ao Gestor Avícola Global 🐥",
            "hi" to "ग्लोबल पोल्ट्री मैनेजर में आपका स्वागत है 🐥"
        ),
        "onboarding_subtitle" to mapOf(
            "ar" to "اختر لغتك، دولتك، وعملتك المحلية لضبط كافة السجلات المالية والفواتير تلقائياً.",
            "fr" to "Choisissez votre langue, pays et devise locale pour configurer tous vos registres financiers.",
            "en" to "Select your language, country, and local currency to configure all financial ledgers automatically.",
            "es" to "Seleccione su idioma, país y moneda local para configurar todos los registros financieros.",
            "pt" to "Selecione seu idioma, país e moeda local para configurar todos os registros financeiros automaticamente.",
            "hi" to "सभी वित्तीय खातों को स्वचालित रूप से कॉन्फ़िगर करने के लिए अपनी भाषा, देश और मुद्रा चुनें।"
        ),
        "onboarding_lang_label" to mapOf(
            "ar" to "🌐 لغة التطبيق (Language)",
            "fr" to "🌐 Langue de l'application",
            "en" to "🌐 Application Language",
            "es" to "🌐 Idioma de la Aplicación",
            "pt" to "🌐 Idioma do Aplicativo",
            "hi" to "🌐 ऐप की भाषा"
        ),
        "onboarding_country_label" to mapOf(
            "ar" to "🏳️ الدولة والإقليم (Country)",
            "fr" to "🏳️ Pays et Région",
            "en" to "🏳️ Country & Region",
            "es" to "🏳️ País y Región",
            "pt" to "🏳️ País e Região",
            "hi" to "🏳️ देश और क्षेत्र"
        ),
        "onboarding_currency_label" to mapOf(
            "ar" to "💱 العملة المعتمدة للخزنة والفواتير (Currency)",
            "fr" to "💱 Devise de la Trésorerie et des Factures",
            "en" to "💱 Treasury & Invoicing Currency",
            "es" to "💱 Moneda de Tesorería y Facturación",
            "pt" to "💱 Moeda de Tesouraria e Faturamento",
            "hi" to "💱 खजाना और चालान मुद्रा"
        ),
        "onboarding_currency_search_hint" to mapOf(
            "ar" to "ابحث عن العملة (DZD, USD, CAD, COP, BRL...)",
            "fr" to "Rechercher une devise (DZD, USD, CAD, COP, BRL...)",
            "en" to "Search currency (DZD, USD, CAD, COP, BRL...)",
            "es" to "Buscar moneda (DZD, USD, CAD, COP, BRL...)",
            "pt" to "Pesquisar moeda (DZD, USD, CAD, COP, BRL...)",
            "hi" to "मुद्रा खोजें (DZD, USD, CAD, COP, BRL...)"
        ),
        "onboarding_start_btn" to mapOf(
            "ar" to "حفظ الإعدادات وبدء الاستخدام 🚀",
            "fr" to "Enregistrer et Démarrer 🚀",
            "en" to "Save Preferences & Start 🚀",
            "es" to "Guardar y Comenzar 🚀",
            "pt" to "Salvar e Começar 🚀",
            "hi" to "सहेजें और शुरू करें 🚀"
        ),
        "paywall_badge" to mapOf(
            "ar" to "👑 باقة الأعمال الاحترافية (SaaS Pro)",
            "fr" to "👑 Offre Pro Multi-Bâtiments (SaaS Pro)",
            "en" to "👑 Multi-Barn Pro Plan (SaaS Pro)",
            "es" to "👑 Plan Pro Multi-Galpón (SaaS Pro)",
            "pt" to "👑 Plano Pro Multi-Galpão (SaaS Pro)",
            "hi" to "👑 मल्टी-बार्न प्रो प्लान (SaaS Pro)"
        ),
        "paywall_offer_title" to mapOf(
            "ar" to "إدارة احترافية للعنبر الإضافي - 4.99$ / لشهرين (دورة إنتاج كاملة)",
            "fr" to "Gestion Pro Bâtiment Supplémentaire - 4.99$ / 2 Mois (Cycle Complet)",
            "en" to "Premium Additional Barn - $4.99 / 2 Months (Full Production Cycle)",
            "es" to "Gestión Pro Galpón Adicional - $4.99 / 2 Meses (Ciclo Completo)",
            "pt" to "Gestão Pro Galpão Adicional - $4.99 / 2 Meses (Ciclo Completo)",
            "hi" to "प्रीमियम अतिरिक्त शेड प्रबंधन - $4.99 / 2 महीने (पूर्ण उत्पादन चक्र)"
        ),
        "paywall_price_badge" to mapOf(
            "ar" to "4.99$ / لشهرين (دورة إنتاج كاملة)",
            "fr" to "4.99$ / 2 Mois (Cycle Complet)",
            "en" to "$4.99 / 2 Months (Full Production Cycle)",
            "es" to "$4.99 / 2 Meses (Ciclo Completo)",
            "pt" to "$4.99 / 2 Meses (Ciclo Completo)",
            "hi" to "$4.99 / 2 महीने (पूर्ण उत्पादन चक्र)"
        ),
        "paywall_subtitle" to mapOf(
            "ar" to "العنبر الأول مجاني بالكامل (Free Tier). لإضافة عنبر إضافي لدورة إنتاج كاملة (شهرين) بدون أي التزام سنوي، فعّل الباقة الاحترافية.",
            "fr" to "Le 1er bâtiment est 100% gratuit. Pour ajouter un bâtiment supplémentaire pour un cycle complet (2 mois) sans engagement annuel, activez l'offre Pro.",
            "en" to "Your 1st Barn is 100% Free. Add an extra barn for a full 2-month production cycle at $4.99 with zero annual lock-in.",
            "es" to "El 1er galpón es 100% gratis. Agregue un galpón adicional por un ciclo completo de 2 meses ($4.99) sin contrato anual.",
            "pt" to "O 1º galpão é 100% gratuito. Adicione um galpão extra por um ciclo completo de 2 meses ($4.99) sem fidelidade anual.",
            "hi" to "आपका पहला शेड 100% मुफ़्त है। बिना किसी वार्षिक अनुबंध के 2 महीने के चक्र के लिए अतिरिक्त शेड जोड़ें।"
        ),
        "paywall_no_annual_lock" to mapOf(
            "ar" to "✓ بدون اشتراك سنوي إجباري • تدفع فقط للعنبر الإضافي (4.99$ / شهرين)",
            "fr" to "✓ Aucun abonnement annuel forcé • Payez par cycle de 2 mois (4.99$)",
            "en" to "✓ No forced annual plan • Pay only $4.99 per 2-month production cycle",
            "es" to "✓ Sin suscripción anual forzada • Solo $4.99 por ciclo de 2 meses",
            "pt" to "✓ Sem assinatura anual forçada • Apenas $4.99 por ciclo de 2 meses",
            "hi" to "✓ कोई वार्षिक अनुबंध नहीं • केवल $4.99 प्रति 2-महीने का चक्र"
        ),
        "paywall_feat_1" to mapOf(
            "ar" to "فتح عنبر إضافي كامل مع مزامنة سحابية فورية للفريق (QR)",
            "fr" to "Bâtiment supplémentaire complet avec synchronisation cloud & équipe QR",
            "en" to "Full additional barn with real-time cloud sync & QR team onboarding",
            "es" to "Galpón adicional completo con sincronización en la nube y equipo QR",
            "pt" to "Galpão adicional completo com sincronização na nuvem e equipe via QR",
            "hi" to "रीयल-टाइम क्लाउड सिंक और QR टीम के साथ अतिरिक्त शेड"
        ),
        "paywall_feat_2" to mapOf(
            "ar" to "حاسبات FCR و EPEF الدقيقة مع دعم البيع الجزئي (Thinning)",
            "fr" to "Calculateurs FCR & EPEF précis avec support des ventes partielles",
            "en" to "Precision FCR & EPEF analytics with partial sales (Thinning) support",
            "es" to "Calculadoras FCR y EPEF precisas con soporte de ventas parciales",
            "pt" to "Calculadoras FCR e EPEF precisas com suporte a vendas parciais (Desbaste)",
            "hi" to "आंशिक बिक्री समर्थन के साथ सटीक FCR और EPEF विश्लेषण"
        ),
        "paywall_feat_3" to mapOf(
            "ar" to "استشارات المساعد البيطري الذكي (Gemini AI Vet) بلا حدود",
            "fr" to "Consultations illimitées avec l'Assistant Vétérinaire IA (Gemini)",
            "en" to "Unlimited AI Veterinarian (Gemini) clinical diagnostics & prescriptions",
            "es" to "Consultas ilimitadas con el Asistente Veterinario IA (Gemini)",
            "pt" to "Consultas ilimitadas com o Assistente Veterinário IA (Gemini)",
            "hi" to "असीमित AI पशु चिकित्सक (Gemini) परामर्श"
        ),
        "paywall_btn_google_play" to mapOf(
            "ar" to "الاشتراك عبر Google Play (4.99$ / لشهرين)",
            "fr" to "S'abonner via Google Play (4.99$ / 2 Mois)",
            "en" to "Subscribe via Google Play ($4.99 / 2 Months)",
            "es" to "Suscribirse con Google Play ($4.99 / 2 Meses)",
            "pt" to "Assinar via Google Play ($4.99 / 2 Meses)",
            "hi" to "Google Play के माध्यम से सदस्यता लें ($4.99 / 2 महीने)"
        ),
        "paywall_btn_stripe" to mapOf(
            "ar" to "الدفع السريع عبر Stripe 💳 (4.99$ / لشهرين)",
            "fr" to "S'abonner via Stripe 💳 (4.99$ / 2 Mois)",
            "en" to "Subscribe via Stripe 💳 ($4.99 / 2 Months)",
            "es" to "Suscribirse con Stripe 💳 ($4.99 / 2 Meses)",
            "pt" to "Assinar via Stripe 💳 ($4.99 / 2 Meses)",
            "hi" to "Stripe 💳 के माध्यम से सदस्यता लें ($4.99 / 2 महीने)"
        ),
        "vet_invoic" to mapOf(
            "ar" to "إصدار فاتورة دواء",
            "fr" to "Facture Médicament",
            "en" to "Issue Med Invoice",
            "es" to "Factura de Medicamento",
            "pt" to "Fatura de Medicamento",
            "hi" to "दवा चालान जारी करें"
        ),
        "vet_invoice_btn" to mapOf(
            "ar" to "إصدار فاتورة دواء",
            "fr" to "Facture Médicament",
            "en" to "Issue Med Invoice",
            "es" to "Factura de Medicamento",
            "pt" to "Fatura de Medicamento",
            "hi" to "दवा चालान जारी करें"
        ),
        "vet_invoice_dialog_subtitle" to mapOf(
            "ar" to "قيد مالي مباشر ووصفة طبية في سجل العنبر",
            "fr" to "Entrée financière directe et ordonnance",
            "en" to "Direct financial entry & prescription",
            "es" to "Registro financiero directo y receta",
            "pt" to "Registro financeiro direto e receita",
            "hi" to "सीधी वित्तीय प्रविष्टि और पर्ची"
        ),
        "vet_med_name_label" to mapOf(
            "ar" to "اسم الدواء أو اللقاح",
            "fr" to "Nom du médicament / vaccin",
            "en" to "Medicine / Vaccine Name",
            "es" to "Nombre del medicamento / vacuna",
            "pt" to "Nome do medicamento / vacina",
            "hi" to "दवा / टीके का नाम"
        ),
        "vet_dosage_instructions_label" to mapOf(
            "ar" to "الجرعة وطريقة التقديم في الماء",
            "fr" to "Posologie et instructions",
            "en" to "Dosage & Water Instructions",
            "es" to "Dosis e instrucciones",
            "pt" to "Dosagem e instruções",
            "hi" to "खुराक और पानी के निर्देश"
        ),
        "vet_med_total_price_label" to mapOf(
            "ar" to "إجمالي سعر الدواء",
            "fr" to "Coût total du médicament",
            "en" to "Total Medicine Cost",
            "es" to "Costo total del medicamento",
            "pt" to "Custo total do medicamento",
            "hi" to "दवा की कुल लागत"
        ),
        "vet_clinical_note_label" to mapOf(
            "ar" to "الملاحظة العيادية والتشخيص",
            "fr" to "Note clinique et diagnostic",
            "en" to "Clinical Note & Diagnosis",
            "es" to "Nota clínica e diagnóstico",
            "pt" to "Nota clínica e diagnóstico",
            "hi" to "नैदानिक नोट और निदान"
        ),
        "vet_invoice_submit_btn" to mapOf(
            "ar" to "إرسال واعتماد الفاتورة",
            "fr" to "Soumettre la facture",
            "en" to "Submit Invoice",
            "es" to "Enviar factura",
            "pt" to "Enviar fatura",
            "hi" to "चालान जमा करें"
        ),
        "vet_invoice_saved_banner" to mapOf(
            "ar" to "فاتورة دواء ووصفة بيطرية",
            "fr" to "Facture et ordonnance",
            "en" to "Med Invoice & Prescription",
            "es" to "Factura y receta",
            "pt" to "Fatura e receita",
            "hi" to "दवा चालान और पर्ची"
        ),
        "btn_cancel" to mapOf(
            "ar" to "إلغاء",
            "fr" to "Annuler",
            "en" to "Cancel",
            "es" to "Cancelar",
            "pt" to "Cancelar",
            "hi" to "रद्द करें"
        ),
        "zero_barns_title" to mapOf(
            "ar" to "لا توجد عنابر مسجلة بعد",
            "fr" to "Aucun bâtiment enregistré",
            "en" to "No Barns Created Yet",
            "es" to "Aún no hay galpones registrados",
            "pt" to "Nenhum galpão registrado ainda",
            "hi" to "अभी तक कोई बार्न दर्ज नहीं है"
        ),
        "zero_barns_subtitle" to mapOf(
            "ar" to "ابدأ رحلتك الآن! العنبر الأول مجاني بالكامل (100% مجاناً) مع كافة ميزات المتابعة اليومية والخزنة والفريق.",
            "fr" to "Commencez maintenant ! Votre 1er bâtiment est 100% gratuit avec suivi quotidien, trésorerie et équipe.",
            "en" to "Start your journey now! Your 1st barn is 100% FREE with full daily ledger, treasury, and QR team features.",
            "es" to "¡Comience ahora! Su 1er galpón es 100% GRATIS con registro diario, tesorería y equipo QR.",
            "pt" to "Comece agora! Seu 1º galpão é 100% GRÁTIS com registro diário, tesouraria e equipe QR.",
            "hi" to "अभी शुरू करें! आपका पहला बार्न 100% मुफ़्त है।"
        ),
        "zero_barns_create_btn" to mapOf(
            "ar" to "+ إنشاء عنبرك الأول مجاناً",
            "fr" to "+ Créer votre 1er bâtiment gratuitement",
            "en" to "+ Create Your 1st Barn for Free",
            "es" to "+ Crear tu 1er Galpón Gratis",
            "pt" to "+ Criar seu 1º Galpão Grátis",
            "hi" to "+ अपना पहला बार्न मुफ़्त बनाएं"
        ),
        "btn_owner_add_daily_report" to mapOf(
            "ar" to "+ تسجيل / إدخال يومي",
            "fr" to "+ Saisie quotidienne",
            "en" to "+ Log Daily Entry",
            "es" to "+ Registro diario",
            "pt" to "+ Registro diário",
            "hi" to "+ दैनिक प्रविष्टि दर्ज करें"
        ),
        "dialog_owner_daily_report_title" to mapOf(
            "ar" to "تسجيل البيانات اليومية للعنبر 📋",
            "fr" to "Saisie des données quotidiennes 📋",
            "en" to "Log Barn Daily Data 📋",
            "es" to "Registrar datos diarios del galpón 📋",
            "pt" to "Registrar dados diários do galpão 📋",
            "hi" to "बार्न दैनिक डेटा दर्ज करें 📋"
        ),
        "dialog_edit_daily_report_title" to mapOf(
            "ar" to "تعديل السجل اليومي (مع أثر التدقيق ⚠️)",
            "fr" to "Modifier le rapport quotidien (Audit ⚠️)",
            "en" to "Edit Daily Report (Audit Trail ⚠️)",
            "es" to "Editar registro diario (Auditoría ⚠️)",
            "pt" to "Editar registro diário (Auditoria ⚠️)",
            "hi" to "दैनिक रिपोर्ट संपादित करें (ऑडिट ⚠️)"
        ),
        "field_day_number_mid_cycle" to mapOf(
            "ar" to "رقم اليوم في الدورة (يدعم البدء من منتصف الدورة مثل يوم 20 أو 30)",
            "fr" to "Numéro du jour (supporte mi-cycle ex: Jour 20 ou 30)",
            "en" to "Cycle Day Number (supports mid-cycle e.g. Day 20 or 30)",
            "es" to "Número de día del lote (soporta mitad de ciclo ej. Día 20 o 30)",
            "pt" to "Número do dia do lote (suporta meio de ciclo ex: Dia 20 ou 30)",
            "hi" to "चक्र दिन संख्या (मध्य-चक्र का समर्थन करता है जैसे दिन 20 या 30)"
        ),
        "th_edit" to mapOf(
            "ar" to "تعديل",
            "fr" to "Modif.",
            "en" to "Edit",
            "es" to "Editar",
            "pt" to "Editar",
            "hi" to "संपादित"
        ),
        "badge_report_edited" to mapOf(
            "ar" to "⚠️ معدّل",
            "fr" to "⚠️ Modifié",
            "en" to "⚠️ Edited",
            "es" to "⚠️ Editado",
            "pt" to "⚠️ Editado",
            "hi" to "⚠️ संशोधित"
        ),
        "audit_previous_values_prefix" to mapOf(
            "ar" to "القيم السابقة قبل التعديل:",
            "fr" to "Valeurs précédentes avant modification :",
            "en" to "Previous values before edit:",
            "es" to "Valores anteriores antes de editar:",
            "pt" to "Valores anteriores antes da edição:",
            "hi" to "संपादन से पहले के मान:"
        ),
        "vet_invoice_pending_badge" to mapOf(
            "ar" to "⏳ بانتظار موافقة المالك",
            "fr" to "⏳ En attente d'approbation",
            "en" to "⏳ Pending Owner Approval",
            "es" to "⏳ Pendiente de aprobación",
            "pt" to "⏳ Pendente de aprovação",
            "hi" to "⏳ मालिक की स्वीकृति लंबित"
        ),
        "vet_invoice_approved_badge" to mapOf(
            "ar" to "✓ معتمدة ومصروفة",
            "fr" to "✓ Approuvée et déduite",
            "en" to "✓ Approved & Deducted",
            "es" to "✓ Aprobada y descontada",
            "pt" to "✓ Aprovada e descontada",
            "hi" to "✓ स्वीकृत और कटौती की गई"
        ),
        "btn_approve_vet_invoice" to mapOf(
            "ar" to "تأكيد واعتماد الصرف ✓",
            "fr" to "Approuver et déduire ✓",
            "en" to "Confirm & Approve Expense ✓",
            "es" to "Confirmar y aprobar gasto ✓",
            "pt" to "Confirmar e aprovar despesa ✓",
            "hi" to "पुष्टि करें और खर्च स्वीकृत करें ✓"
        ),
        "vet_pending_banner_title" to mapOf(
            "ar" to "فواتير أدوية بانتظار اعتماد الصرف ⏳",
            "fr" to "Factures vétérinaires en attente d'approbation ⏳",
            "en" to "Vet Invoices Pending Owner Approval ⏳",
            "es" to "Facturas veterinarias pendientes de aprobación ⏳",
            "pt" to "Faturas veterinárias pendentes de aprovação ⏳",
            "hi" to "स्वीकृति के लिए लंबित पशु चिकित्सा चालान ⏳"
        ),
        "vet_pending_banner_subtitle" to mapOf(
            "ar" to "لن يتم خصم قيمة هذه الفواتير من الخزنة إلا بعد ضغط المالك على زر التأكيد والاعتماد",
            "fr" to "Ces montants ne seront déduits de la trésorerie qu'après validation du propriétaire",
            "en" to "These amounts are not deducted from the Treasury until confirmed by the Owner",
            "es" to "Estos montos no se descuentan de la tesorería hasta que el propietario los apruebe",
            "pt" to "Estes valores não são descontados da tesouraria até a confirmação do proprietário",
            "hi" to "जब तक मालिक द्वारा पुष्टि नहीं की जाती, ये राशियाँ खजाने से नहीं काटी जाती हैं"
        ),
        "btn_save" to mapOf(
            "ar" to "حفظ وتسجيل ✓",
            "fr" to "Enregistrer ✓",
            "en" to "Save & Record ✓",
            "es" to "Guardar ✓",
            "pt" to "Salvar ✓",
            "hi" to "सहेजें ✓"
        ),
        "settings_studio_subtitle" to mapOf(
            "ar" to "تخصيص المظهر، اللغات العالمية، العملة، واشتراك العنابر",
            "fr" to "Apparence, langues globales, devise et abonnement",
            "en" to "Appearance Studio, Global Languages, Currency & Barn Plan",
            "es" to "Estudio de apariencia, idiomas, moneda y plan de galpones",
            "pt" to "Estúdio de aparência, idiomas, moeda e plano de galpões",
            "hi" to "दिखावट स्टूडियो, वैश्विक भाषाएं, मुद्रा और शेड प्लान"
        ),
        "settings_tab_appearance" to mapOf(
            "ar" to "المظهر",
            "fr" to "Apparence",
            "en" to "Appearance",
            "es" to "Apariencia",
            "pt" to "Aparência",
            "hi" to "दिखावट"
        ),
        "settings_tab_language" to mapOf(
            "ar" to "اللغة",
            "fr" to "Langue",
            "en" to "Language",
            "es" to "Idioma",
            "pt" to "Idioma",
            "hi" to "भाषा"
        ),
        "settings_tab_region_currency" to mapOf(
            "ar" to "الدولة والعملة",
            "fr" to "Pays & Devise",
            "en" to "Country & Currency",
            "es" to "País y Moneda",
            "pt" to "País e Moeda",
            "hi" to "देश और मुद्रा"
        ),
        "settings_tab_subscription" to mapOf(
            "ar" to "الباقة (4.99$)",
            "fr" to "Offre Pro (4.99$)",
            "en" to "Pro Plan ($4.99)",
            "es" to "Plan Pro ($4.99)",
            "pt" to "Plano Pro ($4.99)",
            "hi" to "प्रो प्लान ($4.99)"
        ),
        "appearance_light_mode_title" to mapOf(
            "ar" to "الوضع النهاري (Light Mode)",
            "fr" to "Mode Clair (Light Mode)",
            "en" to "Light Mode",
            "es" to "Modo Claro (Light Mode)",
            "pt" to "Modo Claro (Light Mode)",
            "hi" to "लाइट मोड"
        ),
        "appearance_light_mode_subtitle" to mapOf(
            "ar" to "اضغط للتبديل إلى الوضع الليلي المريح للعين",
            "fr" to "Appuyez pour passer en mode sombre",
            "en" to "Tap to go dark",
            "es" to "Toca para activar el modo oscuro",
            "pt" to "Toque para ativar o modo escuro",
            "hi" to "डार्क मोड के लिए टैप करें"
        ),
        "appearance_dark_mode_title" to mapOf(
            "ar" to "الوضع الليلي (Dark Mode)",
            "fr" to "Mode Sombre (Dark Mode)",
            "en" to "Dark Mode",
            "es" to "Modo Oscuro (Dark Mode)",
            "pt" to "Modo Escuro (Dark Mode)",
            "hi" to "डार्क मोड"
        ),
        "appearance_dark_mode_subtitle" to mapOf(
            "ar" to "مريح للعين في الإضاءة المنخفضة وتباين عالي الوضوح",
            "fr" to "Confortable pour les yeux la nuit",
            "en" to "Easy on the eyes at night",
            "es" to "Cómodo para la vista por la noche",
            "pt" to "Confortável para os olhos à noite",
            "hi" to "रात में आँखों के लिए आरामदायक"
        ),
        "appearance_theme_color_label" to mapOf(
            "ar" to "لون السمة الرئيسي (THEME COLOR)",
            "fr" to "COULEUR DU THÈME (THEME COLOR)",
            "en" to "THEME COLOR",
            "es" to "COLOR DEL TEMA (THEME COLOR)",
            "pt" to "COR DO TEMA (THEME COLOR)",
            "hi" to "थीम का रंग (THEME COLOR)"
        ),
        "appearance_text_size_label" to mapOf(
            "ar" to "حجم الخط والقراءة الميدانية (TEXT SIZE)",
            "fr" to "TAILLE DU TEXTE (TEXT SIZE)",
            "en" to "TEXT SIZE",
            "es" to "TAMAÑO DE TEXTO (TEXT SIZE)",
            "pt" to "TAMANHO DO TEXTO (TEXT SIZE)",
            "hi" to "टेक्स्ट का आकार (TEXT SIZE)"
        ),
        "appearance_ui_style_label" to mapOf(
            "ar" to "نمط الحواف والبطاقات (UI STYLE)",
            "fr" to "STYLE D'INTERFACE (UI STYLE)",
            "en" to "UI STYLE",
            "es" to "ESTILO DE INTERFAZ (UI STYLE)",
            "pt" to "ESTILO DE INTERFACE (UI STYLE)",
            "hi" to "यूआई स्टाइल (UI STYLE)"
        ),
        "text_size_small" to mapOf(
            "ar" to "صغير",
            "fr" to "Petit",
            "en" to "Small",
            "es" to "Pequeño",
            "pt" to "Pequeno",
            "hi" to "छोटा"
        ),
        "text_size_default" to mapOf(
            "ar" to "افتراضي",
            "fr" to "Par défaut",
            "en" to "Default",
            "es" to "Predeterminado",
            "pt" to "Padrão",
            "hi" to "डिफ़ॉल्ट"
        ),
        "text_size_large" to mapOf(
            "ar" to "كبير",
            "fr" to "Grand",
            "en" to "Large",
            "es" to "Grande",
            "pt" to "Grande",
            "hi" to "बड़ा"
        ),
        "text_size_extra_large" to mapOf(
            "ar" to "كبير جداً",
            "fr" to "Très grand",
            "en" to "Extra Large",
            "es" to "Extra Grande",
            "pt" to "Extra Grande",
            "hi" to "बहुत बड़ा"
        ),
        "corner_style_rounded" to mapOf(
            "ar" to "دائري (Rounded)",
            "fr" to "Arrondi (Rounded)",
            "en" to "Rounded",
            "es" to "Redondeado (Rounded)",
            "pt" to "Arredondado (Rounded)",
            "hi" to "गोल (Rounded)"
        ),
        "corner_style_soft" to mapOf(
            "ar" to "ناعم (Soft)",
            "fr" to "Doux (Soft)",
            "en" to "Soft",
            "es" to "Suave (Soft)",
            "pt" to "Suave (Soft)",
            "hi" to "सॉफ्ट (Soft)"
        ),
        "corner_style_crisp" to mapOf(
            "ar" to "حاد (Crisp)",
            "fr" to "Net (Crisp)",
            "en" to "Crisp",
            "es" to "Nítido (Crisp)",
            "pt" to "Nítido (Crisp)",
            "hi" to "क्रिस्प (Crisp)"
        ),
        "appearance_pending_changes" to mapOf(
            "ar" to "• تغييرات غير محفوظة",
            "fr" to "• Modifications",
            "en" to "• Changes",
            "es" to "• Cambios",
            "pt" to "• Alterações",
            "hi" to "• बदलाव"
        ),
        "appearance_btn_discard" to mapOf(
            "ar" to "تراجع (Discard)",
            "fr" to "Annuler",
            "en" to "Discard",
            "es" to "Descartar",
            "pt" to "Descartar",
            "hi" to "रद्द करें"
        ),
        "appearance_btn_apply" to mapOf(
            "ar" to "تطبيق وحفظ ✓",
            "fr" to "Appliquer ✓",
            "en" to "Apply / Save ✓",
            "es" to "Aplicar / Guardar ✓",
            "pt" to "Aplicar / Salvar ✓",
            "hi" to "लागू करें ✓"
        ),
        "settings_hub_row_appearance_title" to mapOf(
            "ar" to "الشكل والمظهر",
            "fr" to "Apparence et Thème",
            "en" to "Appearance & Theme",
            "es" to "Apariencia y Tema",
            "pt" to "Aparência e Tema",
            "hi" to "दिखावट और थीम"
        ),
        "settings_hub_row_language_title" to mapOf(
            "ar" to "لغة التطبيق",
            "fr" to "Langue de l'application",
            "en" to "App Language",
            "es" to "Idioma de la aplicación",
            "pt" to "Idioma do aplicativo",
            "hi" to "ऐप की भाषा"
        ),
        "settings_hub_row_region_title" to mapOf(
            "ar" to "الدولة والعملة المعتمدة",
            "fr" to "Pays et Devise",
            "en" to "Country & Currency",
            "es" to "País y Moneda",
            "pt" to "País e Moeda",
            "hi" to "देश और मुद्रा"
        ),
        "settings_hub_row_subscription_title" to mapOf(
            "ar" to "باقة الاشتراك (SaaS Pro)",
            "fr" to "Abonnement (SaaS Pro)",
            "en" to "Subscription Plan (SaaS Pro)",
            "es" to "Plan de Suscripción (SaaS Pro)",
            "pt" to "Plano de Assinatura (SaaS Pro)",
            "hi" to "सदस्यता योजना (SaaS Pro)"
        ),
        "settings_hub_back_to_main" to mapOf(
            "ar" to "العودة لقائمة الإعدادات",
            "fr" to "Retour aux paramètres",
            "en" to "Back to Settings",
            "es" to "Volver a ajustes",
            "pt" to "Voltar às configurações",
            "hi" to "सेटिंग्स पर वापस जाएं"
        ),
        "climate_banner_title" to mapOf(
            "ar" to "المناخ الحي داخل العنبر",
            "fr" to "Micro-climat du bâtiment en direct",
            "en" to "Live Barn Micro-Climate",
            "es" to "Microclima del galpón en vivo",
            "pt" to "Microclima do galpão ao vivo",
            "hi" to "बार्न का लाइव माइक्रो-क्लाइमेट"
        ),
        "climate_header_btn_label" to mapOf(
            "ar" to "المناخ",
            "fr" to "Climat",
            "en" to "Climate",
            "es" to "Clima",
            "pt" to "Clima",
            "hi" to "जलवायु"
        ),
        "climate_modal_title" to mapOf(
            "ar" to "🌡️ مناخ العنبر والملخص",
            "fr" to "🌡️ Climat & Résumé",
            "en" to "🌡️ Barn Climate & Summary",
            "es" to "🌡️ Clima y Resumen",
            "pt" to "🌡️ Clima e Resumo",
            "hi" to "🌡️ जलवायु और सारांश"
        ),
        "climate_modal_subtitle" to mapOf(
            "ar" to "قراءات الحرارة والأمونيا والتهوية مع مؤشرات الدورة الحية",
            "fr" to "Température, ammoniac, ventilation et indicateurs clés du lot",
            "en" to "Real-time temperature, ammonia, ventilation & key flock metrics",
            "es" to "Temperatura, amoníaco, ventilación e indicadores clave del lote",
            "pt" to "Temperatura, amônia, ventilação e indicadores-chave do lote",
            "hi" to "तापमान, अमोनिया, वेंटिलेशन और प्रमुख झुंड संकेतक"
        ),
        "climate_status_optimal" to mapOf(
            "ar" to "مثالي ومستقر",
            "fr" to "Optimal & Stable",
            "en" to "Optimal & Stable",
            "es" to "Óptimo y Estable",
            "pt" to "Ideal e Estável",
            "hi" to "उत्तम और स्थिर"
        ),
        "climate_status_warning" to mapOf(
            "ar" to "تنبيه حرارة / أمونيا",
            "fr" to "Alerte Temp / Ammoniac",
            "en" to "Temp / Ammonia Warning",
            "es" to "Aviso Temp / Amoníaco",
            "pt" to "Aviso Temp / Amônia",
            "hi" to "तापमान / अमोनिया चेतावनी"
        ),
        "climate_status_alert" to mapOf(
            "ar" to "خطر حرج في المناخ",
            "fr" to "Alerte Critique Climat",
            "en" to "Critical Climate Alert",
            "es" to "Alerta Crítica de Clima",
            "pt" to "Alerta Crítico de Clima",
            "hi" to "गंभीर जलवायु चेतावनी"
        ),
        "climate_temp_label" to mapOf(
            "ar" to "الحرارة",
            "fr" to "Température",
            "en" to "Temperature",
            "es" to "Temperatura",
            "pt" to "Temperatura",
            "hi" to "तापमान"
        ),
        "climate_ammonia_label" to mapOf(
            "ar" to "الأمونيا",
            "fr" to "Ammoniac",
            "en" to "Ammonia",
            "es" to "Amoníaco",
            "pt" to "Amônia",
            "hi" to "अमोनिया"
        ),
        "climate_ventilation_label" to mapOf(
            "ar" to "التهوية",
            "fr" to "Ventilation",
            "en" to "Ventilation",
            "es" to "Ventilación",
            "pt" to "Ventilação",
            "hi" to "वेंटिलेशन"
        ),
        "climate_vent_label" to mapOf(
            "ar" to "التهوية",
            "fr" to "Ventilation",
            "en" to "Ventilation",
            "es" to "Ventilación",
            "pt" to "Ventilação",
            "hi" to "वेंटिलेशन"
        ),
        "quick_stat_birds_title" to mapOf(
            "ar" to "الطيور الحية",
            "fr" to "Sujets vivants",
            "en" to "Live Birds",
            "es" to "Aves vivas",
            "pt" to "Aves vivas",
            "hi" to "जीवित पक्षी"
        ),
        "quick_stat_live_birds" to mapOf(
            "ar" to "الطيور الحية",
            "fr" to "Sujets vivants",
            "en" to "Live Birds",
            "es" to "Aves vivas",
            "pt" to "Aves vivas",
            "hi" to "जीवित पक्षी"
        ),
        "quick_stat_feed_title" to mapOf(
            "ar" to "مخزون العلف",
            "fr" to "Stock d'aliment",
            "en" to "Feed Stock",
            "es" to "Stock de alimento",
            "pt" to "Estoque de ração",
            "hi" to "चारा स्टॉक"
        ),
        "quick_stat_feed_stock" to mapOf(
            "ar" to "مخزون العلف",
            "fr" to "Stock d'aliment",
            "en" to "Feed Stock",
            "es" to "Stock de alimento",
            "pt" to "Estoque de ração",
            "hi" to "चारा स्टॉक"
        ),
        "quick_stat_fcr_title" to mapOf(
            "ar" to "التحويل والوزن",
            "fr" to "Indice FCR & Poids",
            "en" to "FCR & Weight",
            "es" to "FCR y Peso",
            "pt" to "FCR e Peso",
            "hi" to "FCR और वजन"
        ),
        "quick_stat_fcr_weight" to mapOf(
            "ar" to "التحويل والوزن",
            "fr" to "Indice FCR & Poids",
            "en" to "FCR & Weight",
            "es" to "FCR y Peso",
            "pt" to "FCR e Peso",
            "hi" to "FCR और वजन"
        ),
        "quick_stat_treasury_title" to mapOf(
            "ar" to "صافي الخزنة",
            "fr" to "Trésorerie nette",
            "en" to "Net Treasury",
            "es" to "Tesorería neta",
            "pt" to "Tesouraria líquida",
            "hi" to "शुद्ध खजाना"
        ),
        "quick_stat_net_treasury" to mapOf(
            "ar" to "صافي الخزنة",
            "fr" to "Trésorerie nette",
            "en" to "Net Treasury",
            "es" to "Tesorería neta",
            "pt" to "Tesouraria líquida",
            "hi" to "शुद्ध खजाना"
        ),
        "treasury_expenses" to mapOf(
            "ar" to "المصاريف",
            "fr" to "Dépenses",
            "en" to "Expenses",
            "es" to "Gastos",
            "pt" to "Despesas",
            "hi" to "खर्च"
        ),
        "feed_stock_short" to mapOf(
            "ar" to "المخزون",
            "fr" to "Stock",
            "en" to "Stock",
            "es" to "Stock",
            "pt" to "Estoque",
            "hi" to "स्टॉक"
        ),
        "feed_stock_badge_prefix" to mapOf(
            "ar" to "المخزون",
            "fr" to "Stock",
            "en" to "Stock",
            "es" to "Stock",
            "pt" to "Estoque",
            "hi" to "स्टॉक"
        ),
        "btn_close" to mapOf(
            "ar" to "إغلاق ✓",
            "fr" to "Fermer ✓",
            "en" to "Close ✓",
            "es" to "Cerrar ✓",
            "pt" to "Fechar ✓",
            "hi" to "बंद करें ✓"
        ),
        "btn_add_phone_contact" to mapOf(
            "ar" to "+ إضافة جهة اتصال برقم الهاتف",
            "fr" to "+ Ajouter un contact par téléphone",
            "en" to "+ Add Contact by Phone Number",
            "es" to "+ Añadir contacto por teléfono",
            "pt" to "+ Adicionar contato por telefone",
            "hi" to "+ फ़ोन नंबर से संपर्क जोड़ें"
        ),
        "dialog_add_contact_title" to mapOf(
            "ar" to "إضافة جهة اتصال جديدة",
            "fr" to "Ajouter un contact",
            "en" to "Add New Contact",
            "es" to "Añadir nuevo contacto",
            "pt" to "Adicionar novo contato",
            "hi" to "नया संपर्क जोड़ें"
        ),
        "add_contact_dialog_title" to mapOf(
            "ar" to "إضافة جهة اتصال جديدة",
            "fr" to "Ajouter un contact",
            "en" to "Add New Contact",
            "es" to "Añadir nuevo contacto",
            "pt" to "Adicionar novo contato",
            "hi" to "नया संपर्क जोड़ें"
        ),
        "add_contact_dialog_subtitle" to mapOf(
            "ar" to "أدخل بيانات الشريك أو العامل أو المورد",
            "fr" to "Entrez les coordonnées",
            "en" to "Enter partner, worker, or supplier details",
            "es" to "Ingrese los datos del socio o proveedor",
            "pt" to "Insira os dados do parceiro ou fornecedor",
            "hi" to "साथी, कार्यकर्ता या आपूर्तिकर्ता का विवरण दर्ज करें"
        ),
        "contact_name_label" to mapOf(
            "ar" to "اسم جهة الاتصال",
            "fr" to "Nom du contact",
            "en" to "Contact Name",
            "es" to "Nombre del contacto",
            "pt" to "Nome do contato",
            "hi" to "संपर्क का नाम"
        ),
        "contact_phone_label" to mapOf(
            "ar" to "رقم الهاتف",
            "fr" to "Numéro de téléphone",
            "en" to "Phone Number",
            "es" to "Número de teléfono",
            "pt" to "Número de telefone",
            "hi" to "फ़ोन नंबर"
        ),
        "contact_role_label" to mapOf(
            "ar" to "الصفة / الدور",
            "fr" to "Rôle / Fonction",
            "en" to "Role / Category",
            "es" to "Rol / Categoría",
            "pt" to "Função / Categoria",
            "hi" to "भूमिका / श्रेणी"
        ),
        "contact_role_worker" to mapOf(
            "ar" to "مشرف عنبر 👷‍♂️",
            "fr" to "Ouvrier avicole 👷‍♂️",
            "en" to "Barn Worker 👷‍♂️",
            "es" to "Supervisor de galpón 👷‍♂️",
            "pt" to "Supervisor de galpão 👷‍♂️",
            "hi" to "बार्न सुपरवाइज़र 👷‍♂️"
        ),
        "contact_role_vet" to mapOf(
            "ar" to "طبيب بيطري 🩺",
            "fr" to "Vétérinaire 🩺",
            "en" to "Veterinarian 🩺",
            "es" to "Veterinario 🩺",
            "pt" to "Veterinário 🩺",
            "hi" to "पशु चिकित्सक 🩺"
        ),
        "contact_role_feed" to mapOf(
            "ar" to "مورد أعلاف وذرة وصويا",
            "fr" to "Fournisseur d'aliments",
            "en" to "Feed, Corn & Soy Supplier",
            "es" to "Proveedor de alimento, maíz y soya",
            "pt" to "Fornecedor de ração, milho e soja",
            "hi" to "चारा, मक्का और सोया आपूर्तिकर्ता"
        ),
        "contact_role_feed_supplier" to mapOf(
            "ar" to "مورد أعلاف وذرة وصويا",
            "fr" to "Fournisseur d'aliments",
            "en" to "Feed, Corn & Soy Supplier",
            "es" to "Proveedor de alimento, maíz y soya",
            "pt" to "Fornecedor de ração, milho e soja",
            "hi" to "चारा, मक्का और सोया आपूर्तिकर्ता"
        ),
        "contact_role_chicks" to mapOf(
            "ar" to "مفرخة / بائع صيصان",
            "fr" to "Couvoir / Vendeur de poussins",
            "en" to "Hatchery / Chick Seller",
            "es" to "Incubadora / Vendedor de pollitos",
            "pt" to "Incubatório / Vendedor de pintinhos",
            "hi" to "हैचरी / चूजा विक्रेता"
        ),
        "contact_role_chick_seller" to mapOf(
            "ar" to "مفرخة / بائع صيصان",
            "fr" to "Couvoir / Vendeur de poussins",
            "en" to "Hatchery / Chick Seller",
            "es" to "Incubadora / Vendedor de pollitos",
            "pt" to "Incubatório / Vendedor de pintinhos",
            "hi" to "हैचरी / चूजा विक्रेता"
        ),
        "contact_role_buyer" to mapOf(
            "ar" to "تاجر جملة 🚚",
            "fr" to "Acheteur en gros 🚚",
            "en" to "Wholesale Buyer 🚚",
            "es" to "Comprador mayorista 🚚",
            "pt" to "Comprador atacadista 🚚",
            "hi" to "थोक खरीदार 🚚"
        ),
        "contact_role_vet_importer" to mapOf(
            "ar" to "مستورد أدوية بيطرية 🧬",
            "fr" to "Importateur de médicaments vét. 🧬",
            "en" to "Vet Medicine Importer 🧬",
            "es" to "Importador de medicamentos vet. 🧬",
            "pt" to "Importador de medicamentos vet. 🧬",
            "hi" to "पशु दवा आयातक 🧬"
        ),
        "btn_save_contact" to mapOf(
            "ar" to "حفظ جهة الاتصال ✓",
            "fr" to "Enregistrer ✓",
            "en" to "Save Contact ✓",
            "es" to "Guardar contacto ✓",
            "pt" to "Salvar contato ✓",
            "hi" to "संपर्क सहेजें ✓"
        ),
        "btn_send_financial_request" to mapOf(
            "ar" to "🧾 إرسال طلب مالي / فاتورة",
            "fr" to "🧾 Envoyer demande financière",
            "en" to "🧾 Send Financial Request / Invoice",
            "es" to "🧾 Enviar solicitud financiera",
            "pt" to "🧾 Enviar solicitação financeira",
            "hi" to "🧾 वित्तीय अनुरोध / चालान भेजें"
        ),
        "btn_universal_financial_request" to mapOf(
            "ar" to "🧾 إرسال طلب مالي / فاتورة",
            "fr" to "🧾 Envoyer demande financière",
            "en" to "🧾 Send Financial Request / Invoice",
            "es" to "🧾 Enviar solicitud financiera",
            "pt" to "🧾 Enviar solicitação financeira",
            "hi" to "🧾 वित्तीय अनुरोध / चालान भेजें"
        ),
        "btn_universal_financial_short" to mapOf(
            "ar" to "🧾 فاتورة / طلب دفعة",
            "fr" to "🧾 Facture / Paiement",
            "en" to "🧾 Invoice / Payment",
            "es" to "🧾 Factura / Pago",
            "pt" to "🧾 Fatura / Pagamento",
            "hi" to "🧾 चालान / भुगतान"
        ),
        "fin_req_dialog_title" to mapOf(
            "ar" to "إرسال طلب مالي إلى المالك",
            "fr" to "Demande financière au propriétaire",
            "en" to "Send Financial Request to Owner",
            "es" to "Solicitud financiera al propietario",
            "pt" to "Solicitação financeira ao proprietário",
            "hi" to "मालिक को वित्तीय अनुरोध भेजें"
        ),
        "fin_req_dialog_subtitle" to mapOf(
            "ar" to "يرتبط الطلب بخزنة العنبر ليقوم المالك باعتماده وصرفه",
            "fr" to "La demande sera envoyée à la trésorerie pour approbation",
            "en" to "Linked to barn treasury for owner approval and disbursement",
            "es" to "Vinculado a la tesorería para aprobación del propietario",
            "pt" to "Vinculado à tesouraria para aprovação do proprietário",
            "hi" to "मालिक की स्वीकृति और भुगतान के लिए खजाने से जुड़ा"
        ),
        "fin_req_type_label" to mapOf(
            "ar" to "نوع الطلب المالي / الفاتورة",
            "fr" to "Type de demande / Facture",
            "en" to "Request / Invoice Type",
            "es" to "Tipo de solicitud / Factura",
            "pt" to "Tipo de solicitação / Fatura",
            "hi" to "अनुरोध / चालान का प्रकार"
        ),
        "fin_req_worker_wage" to mapOf(
            "ar" to "أجرة عامل / سلفة",
            "fr" to "Salaire ouvrier / Avance",
            "en" to "Worker Wage / Advance",
            "es" to "Salario / Anticipo",
            "pt" to "Salário / Adiantamento",
            "hi" to "मजदूरी / अग्रिम"
        ),
        "fin_req_cat_worker_advance" to mapOf(
            "ar" to "أجرة عامل / سلفة مالية",
            "fr" to "Salaire ouvrier / Avance",
            "en" to "Worker Wage / Financial Advance",
            "es" to "Salario de trabajador / Anticipo",
            "pt" to "Salário de trabalhador / Adiantamento",
            "hi" to "मजदूरी / वित्तीय अग्रिम"
        ),
        "fin_req_gas_maint" to mapOf(
            "ar" to "شراء غاز / صيانة طارئة",
            "fr" to "Achat gaz / Maintenance urgente",
            "en" to "Gas Purchase / Emergency Maint.",
            "es" to "Compra de gas / Mantenimiento",
            "pt" to "Compra de gás / Manutenção",
            "hi" to "गैस खरीद / आपातकालीन रखरखाव"
        ),
        "fin_req_cat_gas_maint" to mapOf(
            "ar" to "شراء غاز / صيانة طارئة",
            "fr" to "Achat gaz / Maintenance urgente",
            "en" to "Gas Purchase / Emergency Maint.",
            "es" to "Compra de gas / Mantenimiento urgente",
            "pt" to "Compra de gás / Manutenção urgente",
            "hi" to "गैस खरीद / आपातकालीन रखरखाव"
        ),
        "fin_req_feed_invoice" to mapOf(
            "ar" to "فاتورة أعلاف",
            "fr" to "Facture d'aliments",
            "en" to "Feed Invoice",
            "es" to "Factura de alimento",
            "pt" to "Fatura de ração",
            "hi" to "चारा चालान"
        ),
        "fin_req_cat_feed_invoice" to mapOf(
            "ar" to "فاتورة أعلاف (ذرة / صويا / مركب)",
            "fr" to "Facture d'aliments (Maïs / Soja)",
            "en" to "Feed Invoice (Corn / Soy / Compound)",
            "es" to "Factura de alimento (Maíz / Soya)",
            "pt" to "Fatura de ração (Milho / Soja)",
            "hi" to "चारा चालान (मक्का / सोया / मिश्रित)"
        ),
        "fin_req_chicks_invoice" to mapOf(
            "ar" to "فاتورة صيصان",
            "fr" to "Facture de poussins",
            "en" to "Chicks Invoice",
            "es" to "Factura de pollitos",
            "pt" to "Fatura de pintinhos",
            "hi" to "चूज़ा चालान"
        ),
        "fin_req_cat_chick_invoice" to mapOf(
            "ar" to "فاتورة صيصان (مفرخة)",
            "fr" to "Facture de poussins (Couvoir)",
            "en" to "Chicks Invoice (Hatchery)",
            "es" to "Factura de pollitos (Incubadora)",
            "pt" to "Fatura de pintinhos (Incubatório)",
            "hi" to "चूज़ा चालान (हैचरी)"
        ),
        "fin_req_vet_meds" to mapOf(
            "ar" to "فاتورة أدوية بيطرية",
            "fr" to "Facture médicaments vét.",
            "en" to "Veterinary Medicine Invoice",
            "es" to "Factura medicamentos vet.",
            "pt" to "Fatura medicamentos vet.",
            "hi" to "पशु चिकित्सा दवा चालान"
        ),
        "fin_req_cat_vet_invoice" to mapOf(
            "ar" to "فاتورة أدوية ولقاحات بيطرية",
            "fr" to "Facture médicaments & vaccins vét.",
            "en" to "Veterinary Medicines & Vaccines Invoice",
            "es" to "Factura de medicamentos y vacunas vet.",
            "pt" to "Fatura de medicamentos e vacinas vet.",
            "hi" to "पशु दवाएं और टीके चालान"
        ),
        "fin_req_amount_label" to mapOf(
            "ar" to "المبلغ المطلوب",
            "fr" to "Montant demandé",
            "en" to "Requested Amount",
            "es" to "Monto solicitado",
            "pt" to "Valor solicitado",
            "hi" to "अनुरोधित राशि"
        ),
        "fin_req_note_label" to mapOf(
            "ar" to "البيان / تفاصيل الطلب",
            "fr" to "Motif / Détails",
            "en" to "Description / Note",
            "es" to "Concepto / Detalles",
            "pt" to "Descrição / Detalhes",
            "hi" to "विवरण / नोट"
        ),
        "fin_req_submit_btn" to mapOf(
            "ar" to "إرسال الطلب المالي ✓",
            "fr" to "Envoyer la demande ✓",
            "en" to "Submit Financial Request ✓",
            "es" to "Enviar solicitud ✓",
            "pt" to "Enviar solicitação ✓",
            "hi" to "वित्तीय अनुरोध भेजें ✓"
        ),
        "fin_req_status_pending" to mapOf(
            "ar" to "⏳ بانتظار اعتماد المالك",
            "fr" to "⏳ En attente d'approbation",
            "en" to "⏳ Pending Owner Approval",
            "es" to "⏳ Pendiente de aprobación",
            "pt" to "⏳ Aguardando aprovação",
            "hi" to "⏳ मालिक की स्वीकृति लंबित"
        ),
        "fin_req_status_approved" to mapOf(
            "ar" to "✓ تم الاعتماد والصرف من الخزنة",
            "fr" to "✓ Approuvé & Déduit de la trésorerie",
            "en" to "✓ Approved & Deducted from Treasury",
            "es" to "✓ Aprobado y descontado",
            "pt" to "✓ Aprovado e deduzido",
            "hi" to "✓ स्वीकृत और खजाने से कटौती"
        ),
        "btn_approve_disbursement" to mapOf(
            "ar" to "تأكيد واعتماد الصرف ✓",
            "fr" to "Confirmer & Approuver ✓",
            "en" to "Confirm & Approve Payment ✓",
            "es" to "Confirmar y Aprobar Pago ✓",
            "pt" to "Confirmar e Aprovar Pagamento ✓",
            "hi" to "भुगतान की पुष्टि और स्वीकृति ✓"
        ),
        "btn_approve_vet_invoice" to mapOf(
            "ar" to "تأكيد واعتماد الصرف ✓",
            "fr" to "Confirmer & Approuver ✓",
            "en" to "Confirm & Approve Payment ✓",
            "es" to "Confirmar y Aprobar Pago ✓",
            "pt" to "Confirmar e Aprovar Pagamento ✓",
            "hi" to "भुगतान की पुष्टि और स्वीकृति ✓"
        ),
        "vet_pending_banner_title" to mapOf(
            "ar" to "⏳ طلبات مالية وفواتير بانتظار اعتمادك",
            "fr" to "⏳ Demandes financières en attente",
            "en" to "⏳ Financial Requests & Invoices Pending Approval",
            "es" to "⏳ Solicitudes financieras pendientes de aprobación",
            "pt" to "⏳ Solicitações financeiras aguardando aprovação",
            "hi" to "⏳ वित्तीय अनुरोध और चालान स्वीकृति हेतु लंबित"
        ),
        "vet_pending_banner_subtitle" to mapOf(
            "ar" to "راجع الطلبات المرسلة من الطبيب أو المشرف أو المورد واضغط اعتماد ليتم خصمها من الخزنة",
            "fr" to "Vérifiez les demandes envoyées et approuvez-les pour déduire de la trésorerie",
            "en" to "Review requests from vet, worker, or supplier and approve to deduct from treasury",
            "es" to "Revise las solicitudes enviadas y apruébelas para descontar de la tesorería",
            "pt" to "Revise as solicitações enviadas e aprove-as para descontar da tesouraria",
            "hi" to "पशु चिकित्सक, कार्यकर्ता या आपूर्तिकर्ता के अनुरोधों की समीक्षा करें और खजाने से कटौती हेतु स्वीकृत करें"
        ),
        "vet_invoice_pending_badge" to mapOf(
            "ar" to "⏳ طلب مالي معلق — لا يُخصم من الخزنة حتى تعتمده",
            "fr" to "⏳ En attente — Non déduit avant votre approbation",
            "en" to "⏳ Pending Request — Not deducted until you approve",
            "es" to "⏳ Solicitud pendiente — No descontada hasta aprobar",
            "pt" to "⏳ Solicitação pendente — Não descontada até aprovar",
            "hi" to "⏳ लंबित अनुरोध — आपकी स्वीकृति तक खजाने से नहीं काटा गया"
        ),
        "hub_title_main" to mapOf(
            "ar" to "💼 مركز الإدارة المستقل لكل مهنة",
            "fr" to "💼 Centre de Gestion par Profession",
            "en" to "💼 Role-Based Management Hub",
            "es" to "💼 Centro de Gestión por Profesión",
            "pt" to "💼 Centro de Gestão por Profissão",
            "hi" to "💼 पेशेवर प्रबंधन केंद्र"
        ),
        "hub_subtitle_select_role" to mapOf(
            "ar" to "اختر مهنتك ليتحول المركز إلى نظام إدارة متكامل خاص بنشاطك:",
            "fr" to "Choisissez votre profession pour adapter le tableau de bord :",
            "en" to "Select your profession to adapt the management workspace:",
            "es" to "Seleccione su profesión para adaptar el centro de gestión:",
            "pt" to "Selecione sua profissão para adaptar o centro de gestão:",
            "hi" to "प्रबंधन कार्यक्षेत्र को अनुकूलित करने के लिए अपना पेशा चुनें:"
        ),
        "role_badge_owner_barn" to mapOf(
            "ar" to "🏠 مالك المزرعة والعنابر",
            "fr" to "🏠 Propriétaire d'élevage",
            "en" to "🏠 Barn & Farm Owner",
            "es" to "🏠 Propietario de Granja",
            "pt" to "🏠 Proprietário de Granja",
            "hi" to "🏠 फार्म और बार्न मालिक"
        ),
        "role_badge_vet_clinic" to mapOf(
            "ar" to "🩺 الطبيب البيطري المستقل",
            "fr" to "🩺 Clinique Vétérinaire",
            "en" to "🩺 Independent Vet Clinic",
            "es" to "🩺 Clínica Veterinaria",
            "pt" to "🩺 Clínica Veterinária",
            "hi" to "🩺 स्वतंत्र पशु चिकित्सालय"
        ),
        "role_badge_worker" to mapOf(
            "ar" to "👷 مشرف العنبر الميداني",
            "fr" to "👷 Superviseur de Bâtiment",
            "en" to "👷 Field Barn Supervisor",
            "es" to "👷 Supervisor de Galpón",
            "pt" to "👷 Supervisor de Galpão",
            "hi" to "👷 बार्न पर्यवेक्षक"
        ),
        "role_badge_feed_supplier" to mapOf(
            "ar" to "🌾 مورد الأعلاف والذرة والصويا",
            "fr" to "🌾 Fournisseur d'Aliments",
            "en" to "🌾 Feed, Corn & Soy Supplier",
            "es" to "🌾 Proveedor de Alimento",
            "pt" to "🌾 Fornecedor de Ração",
            "hi" to "🌾 चारा, मक्का और सोया आपूर्तिकर्ता"
        ),
        "role_badge_chick_hatchery" to mapOf(
            "ar" to "🐣 مفرخة وبائع صيصان",
            "fr" to "🐣 Couvoir & Poussins",
            "en" to "🐣 Chick Hatchery",
            "es" to "🐣 Incubadora de Pollitos",
            "pt" to "🐣 Incubatório de Pintinhos",
            "hi" to "🐣 चूजा हैचरी"
        ),
        "role_badge_wholesale_trader" to mapOf(
            "ar" to "🚚 تاجر جملة ومشتري دجاج",
            "fr" to "🚚 Grossiste Volaille",
            "en" to "🚚 Wholesale Poultry Trader",
            "es" to "🚚 Mayorista Avícola",
            "pt" to "🚚 Atacadista de Aves",
            "hi" to "🚚 थोक पोल्ट्री व्यापारी"
        ),
        "role_badge_vet_importer" to mapOf(
            "ar" to "🧬 مستورد الأدوية واللقاحات",
            "fr" to "🧬 Importateur Médicaments Vét.",
            "en" to "🧬 Vet Drug & Vaccine Importer",
            "es" to "🧬 Importador Veterinario",
            "pt" to "🧬 Importador Veterinário",
            "hi" to "🧬 पशु दवा और टीका आयातक"
        ),
        "vet_hub_tab_supervised_barns" to mapOf(
            "ar" to "🏥 العنابر تحت الإشراف",
            "fr" to "🏥 Bâtiments supervisés",
            "en" to "🏥 Supervised Barns",
            "es" to "🏥 Galpones supervisados",
            "pt" to "🏥 Galpões supervisionados",
            "hi" to "🏥 पर्यवेक्षित बार्न"
        ),
        "vet_hub_tab_pharmacy" to mapOf(
            "ar" to "💊 مخزون الصيدلية والمستوردين",
            "fr" to "💊 Pharmacie & Importateurs",
            "en" to "💊 Pharmacy & Importers",
            "es" to "💊 Farmacia e Importadores",
            "pt" to "💊 Farmácia e Importadores",
            "hi" to "💊 फार्मेसी और आयातक"
        ),
        "vet_hub_tab_wallet" to mapOf(
            "ar" to "💰 خزنة العيادة البيطرية",
            "fr" to "💰 Trésorerie Clinique",
            "en" to "💰 Vet Clinic Wallet",
            "es" to "💰 Tesorería Clínica",
            "pt" to "💰 Tesouraria da Clínica",
            "hi" to "💰 क्लिनिक खजाना"
        ),
        "vet_hub_tab_epidemics" to mapOf(
            "ar" to "🧬 تحليلات الأوبئة واللقاحات",
            "fr" to "🧬 Épidémies & Vaccins",
            "en" to "🧬 Epidemics & Vaccines",
            "es" to "🧬 Epidemias y Vacunas",
            "pt" to "🧬 Epidemias e Vacinas",
            "hi" to "🧬 महामारी और टीके"
        ),
        "vet_hub_supervised_subtitle" to mapOf(
            "ar" to "متابعة الحالة الصحية والحيوية لجميع المزارع المتعاقدة مع عيادتك:",
            "fr" to "Suivi sanitaire de toutes les fermes sous contrat avec votre clinique :",
            "en" to "Monitor health & livability across all farms contracted with your clinic:",
            "es" to "Monitoreo sanitario de todas las granjas contratadas con su clínica:",
            "pt" to "Monitoramento sanitário de todas as granjas contratadas com sua clínica:",
            "hi" to "आपके क्लिनिक से जुड़े सभी फार्मों के स्वास्थ्य की निगरानी:"
        ),
        "vet_hub_pharmacy_subtitle" to mapOf(
            "ar" to "جرد اللقاحات والمضادات الحيوية المتوفرة في عيادتك والربط المباشر بالمستوردين:",
            "fr" to "Inventaire des vaccins et antibiotiques et lien direct avec les importateurs :",
            "en" to "Clinic vaccine/antibiotic inventory & direct ordering from importers:",
            "es" to "Inventario de vacunas y antibióticos y contacto con importadores:",
            "pt" to "Inventário de vacinas e antibióticos e contato com importadores:",
            "hi" to "क्लिनिक वैक्सीन/एंटीबायोटिक स्टॉक और आयातकों से सीधा संपर्क:"
        ),
        "vet_hub_contact_importer_btn" to mapOf(
            "ar" to "📦 طلب توريد من المستورد",
            "fr" to "📦 Commander chez l'importateur",
            "en" to "📦 Order from Importer",
            "es" to "📦 Pedir al importador",
            "pt" to "📦 Pedir ao importador",
            "hi" to "📦 आयातक से ऑर्डर करें"
        ),
        "vet_hub_wallet_collected" to mapOf(
            "ar" to "المبالغ المحصلة",
            "fr" to "Montants encaissés",
            "en" to "Collected Revenue",
            "es" to "Ingresos cobrados",
            "pt" to "Valores recebidos",
            "hi" to "प्राप्त राशि"
        ),
        "vet_hub_wallet_pending" to mapOf(
            "ar" to "مستحقات قيد التحصيل",
            "fr" to "Créances en attente",
            "en" to "Pending Receivables",
            "es" to "Cuentas por cobrar",
            "pt" to "Valores a receber",
            "hi" to "लंबित बकाया"
        ),
        "vet_hub_wallet_total" to mapOf(
            "ar" to "إجمالي فواتير العيادة",
            "fr" to "Total factures clinique",
            "en" to "Total Clinic Invoices",
            "es" to "Total facturas clínica",
            "pt" to "Total faturas da clínica",
            "hi" to "कुल क्लिनिक चालान"
        ),
        "vet_hub_epidemics_subtitle" to mapOf(
            "ar" to "الرصد الوبائي الإقليمي (نيوكاسل، الجمبورو، التهاب الشعب IB) وجدول التحصين:",
            "fr" to "Surveillance épidémiologique régionale (Newcastle, Gumboro, IB) et vaccination :",
            "en" to "Regional virus surveillance (Newcastle, Gumboro, IB) & vaccination schedule:",
            "es" to "Vigilancia epidemiológica regional (Newcastle, Gumboro, IB) y vacunación:",
            "pt" to "Vigilância epidemiológica regional (Newcastle, Gumboro, IB) e vacinação:",
            "hi" to "क्षेत्रीय वायरस निगरानी (न्यूकैसल, गम्बोरो, IB) और टीकाकरण अनुसूची:"
        ),
        "partner_hub_orders_title" to mapOf(
            "ar" to "📋 طلبيات التوريد والعقود النشطة",
            "fr" to "📋 Commandes & Contrats actifs",
            "en" to "📋 Active Supply Orders & Contracts",
            "es" to "📋 Pedidos de suministro y contratos",
            "pt" to "📋 Pedidos de fornecimento e contratos",
            "hi" to "📋 सक्रिय आपूर्ति ऑर्डर और अनुबंध"
        ),
        "partner_hub_send_invoice_btn" to mapOf(
            "ar" to "🧾 إرسال فاتورة توريد للمزرعة",
            "fr" to "🧾 Envoyer facture à la ferme",
            "en" to "🧾 Send Supply Invoice to Farm",
            "es" to "🧾 Enviar factura a la granja",
            "pt" to "🧾 Enviar fatura para a granja",
            "hi" to "🧾 फार्म को आपूर्ति चालान भेजें"
        ),
        "multi_farm_switcher_label" to mapOf(
            "ar" to "التنقل السريع بين العنابر:",
            "fr" to "Changement rapide de bâtiment :",
            "en" to "Quick Barn Switcher:",
            "es" to "Cambio rápido de galpón:",
            "pt" to "Troca rápida de galpão:",
            "hi" to "त्वरित बार्न स्विचर:"
        ),
        "tab_management" to mapOf(
            "ar" to "الإدارة",
            "fr" to "Gestion",
            "en" to "Management",
            "es" to "Gestión",
            "pt" to "Gestão",
            "hi" to "प्रबंधन"
        ),
        "settings_profession_section_title" to mapOf(
            "ar" to "💼 الصفة المهنية ونوع الحساب",
            "fr" to "💼 Profession et type de compte",
            "en" to "💼 Profession & Account Mode",
            "es" to "💼 Profesión y tipo de cuenta",
            "pt" to "💼 Profissão e tipo de conta",
            "hi" to "💼 पेशा और खाता प्रकार"
        ),
        "vet_bottom_tab_barns" to mapOf(
            "ar" to "🏥 العنابر",
            "fr" to "🏥 Bâtiments",
            "en" to "🏥 Barns",
            "es" to "🏥 Galpones",
            "pt" to "🏥 Galpões",
            "hi" to "🏥 बार्न"
        ),
        "vet_bottom_tab_treasury" to mapOf(
            "ar" to "💰 الخزنة",
            "fr" to "💰 Trésorerie",
            "en" to "💰 Treasury",
            "es" to "💰 Tesorería",
            "pt" to "💰 Tesouraria",
            "hi" to "💰 खजाना"
        ),
        "vet_bottom_tab_epidemics" to mapOf(
            "ar" to "🧬 اللقاحات والأوبئة",
            "fr" to "🧬 Vaccins & Épid.",
            "en" to "🧬 Vaccines",
            "es" to "🧬 Vacunas",
            "pt" to "🧬 Vacinas",
            "hi" to "🧬 टीके"
        ),
        "vet_bottom_tab_pharmacy" to mapOf(
            "ar" to "💊 الصيدلية",
            "fr" to "💊 Pharmacie",
            "en" to "💊 Pharmacy",
            "es" to "💊 Farmacia",
            "pt" to "💊 Farmácia",
            "hi" to "💊 फार्मेसी"
        ),
        "vet_interactive_table_title" to mapOf(
            "ar" to "📋 جدول السجل الطبي التفاعلي (اضغط على أي يوم للتفاصيل)",
            "fr" to "📋 Tableau médical interactif (Touchez un jour pour les détails)",
            "en" to "📋 Interactive Medical Ledger (Tap any day for details)",
            "es" to "📋 Tabla médica interactiva (Toque un día para ver detalles)",
            "pt" to "📋 Tabela médica interativa (Toque em um dia para detalhes)",
            "hi" to "📋 इंटरैक्टिव मेडिकल तालिका (विवरण के लिए किसी भी दिन पर टैप करें)"
        ),
        "vet_day_details_title" to mapOf(
            "ar" to "🩺 التفاصيل الحيوية والطبية - اليوم",
            "fr" to "🩺 Détails vitaux et médicaux - Jour",
            "en" to "🩺 Vital & Medical Details - Day",
            "es" to "🩺 Detalles vitales y médicos - Día",
            "pt" to "🩺 Detalhes vitais e médicos - Dia",
            "hi" to "🩺 महत्वपूर्ण और चिकित्सा विवरण - दिन"
        ),
        "vet_day_scheduled_vaccines_title" to mapOf(
            "ar" to "💉 برنامج التحصين واللقاحات المجدولة لهذا العمر",
            "fr" to "💉 Programme de vaccination prévu pour cet âge",
            "en" to "💉 Scheduled Vaccination Program for This Age",
            "es" to "💉 Programa de vacunación programado para esta edad",
            "pt" to "💉 Programa de vacinação programado para esta idade",
            "hi" to "💉 इस आयु के लिए निर्धारित टीकाकरण कार्यक्रम"
        ),
        "vet_day_prescribed_meds_title" to mapOf(
            "ar" to "💊 الأدوية والملاحظات البيطرية المسجلة",
            "fr" to "💊 Médicaments prescrits et notes vétérinaires",
            "en" to "💊 Prescribed Medications & Vet Notes",
            "es" to "💊 Medicamentos recetados y notas veterinarias",
            "pt" to "💊 Medicamentos prescritos e notas veterinárias",
            "hi" to "💊 निर्धारित दवाएं और पशु चिकित्सा नोट्स"
        ),
        "vet_day_no_meds_yet" to mapOf(
            "ar" to "لا توجد وصفات أو ملاحظات طبية مسجلة لهذا اليوم بعد.",
            "fr" to "Aucune prescription ou note médicale enregistrée pour ce jour.",
            "en" to "No prescriptions or medical notes recorded for this day yet.",
            "es" to "Aún no hay recetas ni notas médicas registradas para este día.",
            "pt" to "Nenhuma receita ou nota médica registrada para este dia ainda.",
            "hi" to "इस दिन के लिए अभी तक कोई दवा या मेडिकल नोट दर्ज नहीं है।"
        ),
        "vet_day_add_note_btn" to mapOf(
            "ar" to "+ إضافة ملاحظة طبية أو تحصين لهذا اليوم 🩺",
            "fr" to "+ Ajouter une note médicale ou vaccin pour ce jour 🩺",
            "en" to "+ Add Medical Note or Vaccine for This Day 🩺",
            "es" to "+ Añadir nota médica o vacuna para este día 🩺",
            "pt" to "+ Adicionar nota médica ou vacina para este dia 🩺",
            "hi" to "+ इस दिन के लिए मेडिकल नोट या टीका जोड़ें 🩺"
        ),
        "vet_day_save_note_btn" to mapOf(
            "ar" to "حفظ السجل الطبي لليوم ✓",
            "fr" to "Enregistrer la note médicale ✓",
            "en" to "Save Medical Record ✓",
            "es" to "Guardar registro médico ✓",
            "pt" to "Salvar registro médico ✓",
            "hi" to "मेडिकल रिकॉर्ड सहेजें ✓"
        ),
        "btn_invite_team_short" to mapOf(
            "ar" to "👥+ فريق",
            "fr" to "👥+ Équipe",
            "en" to "👥+ Team",
            "es" to "👥+ Equipo",
            "pt" to "👥+ Equipe",
            "hi" to "👥+ टीम"
        ),
        "buyer_name_placeholder" to mapOf(
            "ar" to "مثال: تاجر الجملة الحاج علي",
            "fr" to "Ex : Grossiste El Hadj Ali",
            "en" to "e.g., Wholesaler Al-Hajj Ali",
            "es" to "Ej: Mayorista El Hadj Ali",
            "pt" to "Ex: Atacadista Sr. Carlos",
            "hi" to "उदा: थोक व्यापारी हाजी अली"
        ),
        "bird_age_auto_calculated_title" to mapOf(
            "ar" to "🗓️ عمر الدجاج: {days} يوم (محسوب تلقائياً)",
            "fr" to "🗓️ Âge du lot : {days} jours (calculé auto.)",
            "en" to "🗓️ Bird Age: {days} days (Auto-calculated)",
            "es" to "🗓️ Edad del lote: {days} días (Calculado auto.)",
            "pt" to "🗓️ Idade do lote: {days} dias (Calculado auto.)",
            "hi" to "🗓️ मुर्गियों की आयु: {days} दिन (स्वतः गणना)"
        ),
        "bird_age_auto_calculated_desc" to mapOf(
            "ar" to "يُقرأ عمر الدجاج تلقائياً من آخر يوم مسجل في السجل ويُطبع في الفاتورة الرسمية.",
            "fr" to "L'âge est lu automatiquement depuis le dernier jour du registre et imprimé sur la facture officielle.",
            "en" to "Bird age is read automatically from the latest recorded cycle day and printed on the official invoice.",
            "es" to "La edad se lee automáticamente del último día registrado y se imprime en la factura oficial.",
            "pt" to "A idade é lida automaticamente do último dia registrado e impressa na fatura oficial.",
            "hi" to "आयु अंतिम दर्ज दिन से स्वतः पढ़ी जाती है और आधिकारिक चालान पर छपती है।"
        ),
        "tare_mode_detailed_desc" to mapOf(
            "ar" to "يبدأ بوزن الفارغ بالتفصيل ثم الانتقال للممتلئ",
            "fr" to "Commence par peser les caisses vides en détail puis les caisses pleines",
            "en" to "Starts by weighing empty crates in detail, then full crates",
            "es" to "Comienza pesando las cajas vacías en detalle y luego las llenas",
            "pt" to "Começa pesando as caixas vazias em detalhe e depois as cheias",
            "hi" to "पहले खाली बक्सों का विस्तार से वजन करें, फिर भरे बक्सों का"
        ),
        "tare_mode_direct_desc" to mapOf(
            "ar" to "إدخال رقم واحد لإجمالي وزن الفارغ والانتقال المباشر للقائم",
            "fr" to "Saisir un seul chiffre pour la tare totale et passer directement au brut",
            "en" to "Enter a single number for total empty tare and proceed directly to gross",
            "es" to "Ingresar un solo número para la tara total y pasar directo al peso bruto",
            "pt" to "Inserir um único valor para a tara total e ir direto ao peso bruto",
            "hi" to "कुल खाली वजन के लिए एक संख्या दर्ज करें और सीधे सकल वजन पर जाएं"
        ),
        "weigh_net_weight_short" to mapOf(
            "ar" to "صافي الوزن (B-A)",
            "fr" to "Poids Net (B-A)",
            "en" to "Net Weight (B-A)",
            "es" to "Peso Neto (B-A)",
            "pt" to "Peso Líquido (B-A)",
            "hi" to "शुद्ध वजन (B-A)"
        ),
        "weigh_net_weight_label" to mapOf(
            "ar" to "صافي الوزن (B-A)",
            "fr" to "Poids Net (B-A)",
            "en" to "Net Weight (B-A)",
            "es" to "Peso Neto (B-A)",
            "pt" to "Peso Líquido (B-A)",
            "hi" to "शुद्ध वजन (B-A)"
        ),
        "weigh_total_birds_short" to mapOf(
            "ar" to "إجمالي الطيور",
            "fr" to "Total Sujets",
            "en" to "Total Birds",
            "es" to "Total Aves",
            "pt" to "Total Aves",
            "hi" to "कुल पक्षी"
        ),
        "weigh_total_birds_label" to mapOf(
            "ar" to "إجمالي الطيور",
            "fr" to "Total Sujets",
            "en" to "Total Birds",
            "es" to "Total Aves",
            "pt" to "Total Aves",
            "hi" to "कुल पक्षी"
        ),
        "weigh_avg_bird_short" to mapOf(
            "ar" to "معدل الدجاجة",
            "fr" to "Poids Moyen",
            "en" to "Avg / Bird",
            "es" to "Promedio / Ave",
            "pt" to "Média / Ave",
            "hi" to "औसत / पक्षी"
        ),
        "weigh_avg_bird_label" to mapOf(
            "ar" to "معدل الدجاجة",
            "fr" to "Poids Moyen",
            "en" to "Avg / Bird",
            "es" to "Promedio / Ave",
            "pt" to "Média / Ave",
            "hi" to "औसत / पक्षी"
        ),
        "weigh_total_sales_short" to mapOf(
            "ar" to "إجمالي المبيعات",
            "fr" to "Total Ventes",
            "en" to "Total Sales",
            "es" to "Total Ventas",
            "pt" to "Total Vendas",
            "hi" to "कुल बिक्री"
        ),
        "weigh_total_sales_label" to mapOf(
            "ar" to "إجمالي المبيعات",
            "fr" to "Total Ventes",
            "en" to "Total Sales",
            "es" to "Total Ventas",
            "pt" to "Total Vendas",
            "hi" to "कुल बिक्री"
        ),
        "weigh_batch_entry_title" to mapOf(
            "ar" to "⚡ إدخال وزنات الدفعة",
            "fr" to "⚡ Saisie rapide des pesées",
            "en" to "⚡ Batch Weighing Entry",
            "es" to "⚡ Entrada rápida de pesadas",
            "pt" to "⚡ Entrada rápida de pesagens",
            "hi" to "⚡ बैच वजन प्रविष्टि"
        ),
        "weigh_price_per_kg_badge" to mapOf(
            "ar" to "سعر الكيلو",
            "fr" to "Prix / kg",
            "en" to "Price / kg",
            "es" to "Precio / kg",
            "pt" to "Preço / kg",
            "hi" to "मूल्य / किग्रा"
        ),
        "private_phone_network_title" to mapOf(
            "ar" to "🔒 شبكة خاصة برقم الهاتف (فريقك وأصدقاؤك فقط)",
            "fr" to "🔒 Réseau privé par N° de téléphone (Votre équipe & amis)",
            "en" to "🔒 Private Phone Network (Your Team & Trusted Contacts Only)",
            "es" to "🔒 Red privada por N° de teléfono (Solo tu equipo y amigos)",
            "pt" to "🔒 Rede privada por N° de telefone (Sua equipe e amigos)",
            "hi" to "🔒 निजी फोन नेटवर्क (केवल आपकी टीम और विश्वसनीय मित्र)"
        ),
        "private_phone_network_sub" to mapOf(
            "ar" to "مثل الواتساب: تواصل شركة صغيرة بين العمال، البيطري، وبائعي الصيصان والأعلاف المضافين برقم الهاتف فقط.",
            "fr" to "Comme WhatsApp : communication privée par numéro de téléphone avec vos ouvriers, vétérinaires et fournisseurs.",
            "en" to "Like WhatsApp: private small-company circle connected strictly by phone number (workers, vets & trusted suppliers).",
            "es" to "Como WhatsApp: círculo privado de pequeña empresa conectado solo por número de teléfono.",
            "pt" to "Como WhatsApp: círculo privado de pequena empresa conectado apenas por número de telefone.",
            "hi" to "व्हाट्सएप की तरह: केवल फोन नंबर से जुड़े छोटे उद्यम और मित्रों का निजी नेटवर्क।"
        ),
        "btn_add_phone_contact" to mapOf(
            "ar" to "📱+ صديق برقم الهاتف",
            "fr" to "📱+ Contact par N°",
            "en" to "📱+ Add by Phone",
            "es" to "📱+ Añadir por N°",
            "pt" to "📱+ Adicionar por N°",
            "hi" to "📱+ फोन नंबर जोड़ें"
        ),
        "dialog_add_phone_contact_title" to mapOf(
            "ar" to "إضافة صديق / شريك برقم الهاتف 📱",
            "fr" to "Ajouter un contact par N° de téléphone 📱",
            "en" to "Add Contact / Partner by Phone Number 📱",
            "es" to "Añadir contacto / socio por N° de teléfono 📱",
            "pt" to "Adicionar contato / parceiro por N° de telefone 📱",
            "hi" to "फोन नंबर द्वारा संपर्क / साथी जोड़ें 📱"
        ),
        "dialog_add_phone_contact_sub" to mapOf(
            "ar" to "أضف رقم هاتف العامل، البيطري، تاجر الجملة أو بائع الصيصان ليظهر في دردشتك وسوقك الخاص فقط.",
            "fr" to "Ajoutez le numéro d'un ouvrier, vétérinaire, vendeur de poussins ou grossiste à votre réseau privé.",
            "en" to "Add the phone number of a worker, vet, chick seller, or wholesaler to your private chat & market circle.",
            "es" to "Agrega el número de un trabajador, veterinario o proveedor a tu red privada.",
            "pt" to "Adicione o número de um trabalhador, veterinário ou fornecedor à sua rede privada.",
            "hi" to "अपने निजी नेटवर्क में कार्यकर्ता, पशु चिकित्सक या आपूर्तिकर्ता का फोन नंबर जोड़ें।"
        ),
        "field_contact_phone" to mapOf(
            "ar" to "رقم الهاتف (مثل الواتساب: +213 550...)",
            "fr" to "Numéro de téléphone (ex: +213 550...)",
            "en" to "Phone Number (WhatsApp style: +213 550...)",
            "es" to "Número de teléfono (ej: +213 550...)",
            "pt" to "Número de telefone (ex: +213 550...)",
            "hi" to "फ़ोन नंबर (उदा: +213 550...)"
        ),
        "field_contact_name" to mapOf(
            "ar" to "الاسم الكامل أو اسم المزرعة / المؤسسة",
            "fr" to "Nom complet ou nom de la ferme / société",
            "en" to "Full Name or Farm / Business Name",
            "es" to "Nombre completo o nombre de la granja",
            "pt" to "Nome completo ou nome da granja",
            "hi" to "पूरा नाम या फार्म / व्यवसाय का नाम"
        ),
        "field_contact_role_label" to mapOf(
            "ar" to "صفة الحساب الشخصي (دواجن / مواشي / فلاحة)",
            "fr" to "Rôle du compte personnel",
            "en" to "Personal Account Specialty / Role",
            "es" to "Especialidad / Rol de la cuenta personal",
            "pt" to "Especialidade / Função da conta pessoal",
            "hi" to "व्यक्तिगत खाता भूमिका"
        ),
        "contact_role_worker" to mapOf(
            "ar" to "👷‍♂️ عامل / مشرف عنبر",
            "fr" to "👷‍♂️ Ouvrier / Responsable bâtiment",
            "en" to "👷‍♂️ Barn Worker / Supervisor",
            "es" to "👷‍♂️ Trabajador / Supervisor de galpón",
            "pt" to "👷‍♂️ Trabalhador / Supervisor de galpão",
            "hi" to "👷‍♂️ बार्न कार्यकर्ता / पर्यवेक्षक"
        ),
        "contact_role_vet" to mapOf(
            "ar" to "🩺 طبيب بيطري (يدير عدة عنابر)",
            "fr" to "🩺 Vétérinaire (Multi-bâtiments)",
            "en" to "🩺 Veterinarian (Multi-barn)",
            "es" to "🩺 Veterinario (Multi-galpón)",
            "pt" to "🩺 Veterinário (Multi-galpão)",
            "hi" to "🩺 पशु चिकित्सक (मल्टी-बार्न)"
        ),
        "contact_role_chick_seller" to mapOf(
            "ar" to "مفرخة / بائع صيصان",
            "fr" to "Couvoir / Vendeur de poussins",
            "en" to "Hatchery / Chick Seller",
            "es" to "Incubadora / Vendedor de pollitos",
            "pt" to "Incubatório / Vendedor de pintinhos",
            "hi" to "हैचरी / चूजा विक्रेता"
        ),
        "contact_role_feed_seller" to mapOf(
            "ar" to "🌾 تاجر أعلاف جملة (بالقنطار)",
            "fr" to "🌾 Grossiste d'aliments (Quintal)",
            "en" to "🌾 Feed Wholesaler (Quintal)",
            "es" to "🌾 Mayorista de alimento (Quintal)",
            "pt" to "🌾 Atacadista de ração (Quintal)",
            "hi" to "🌾 चारा थोक व्यापारी (क्विंटल)"
        ),
        "contact_role_wholesale_friend" to mapOf(
            "ar" to "🚚 صديق تاجر جملة / مربي (دواجن・أغنام・أبقار・زراعة)",
            "fr" to "🚚 Ami Grossiste / Éleveur (Volaille・Ovins・Bovins・Agri)",
            "en" to "🚚 Trusted Wholesaler / Farmer (Poultry・Sheep・Cattle・Crops)",
            "es" to "🚚 Amigo Mayorista / Ganadero (Aves・Ovinos・Bovinos・Agro)",
            "pt" to "🚚 Amigo Atacadista / Produtor (Aves・Ovinos・Bovinos・Agro)",
            "hi" to "🚚 विश्वसनीय थोक व्यापारी / किसान (पोल्ट्री・भेड़・मवेशी・कृषि)"
        ),
        "badge_trusted_phone_contact" to mapOf(
            "ar" to "✓ مضاف برقم الهاتف",
            "fr" to "✓ Contact N° vérifié",
            "en" to "✓ Saved Phone Contact",
            "es" to "✓ Contacto por N°",
            "pt" to "✓ Contato por N°",
            "hi" to "✓ फोन संपर्क"
        ),
        "market_private_circle_banner" to mapOf(
            "ar" to "📱 سوق دائرة الأصدقاء والشركة الصغيرة (برقم الهاتف فقط)",
            "fr" to "📱 Marché privé de vos contacts téléphoniques",
            "en" to "📱 Private Phone-Book Market (Friends & Small Company Circle)",
            "es" to "📱 Mercado privado de tus contactos telefónicos",
            "pt" to "📱 Mercado privado dos seus contatos telefônicos",
            "hi" to "📱 आपके फोन संपर्कों का निजी बाजार"
        ),
        "market_filter_phone_contacts_only" to mapOf(
            "ar" to "🔒 أصدقائي برقم الهاتف فقط",
            "fr" to "🔒 Mes contacts N° uniquement",
            "en" to "🔒 My Phone Contacts Only",
            "es" to "🔒 Solo mis contactos telefónicos",
            "pt" to "🔒 Apenas meus contatos telefônicos",
            "hi" to "🔒 केवल मेरे फोन संपर्क"
        ),
        "market_filter_all_trusted" to mapOf(
            "ar" to "🤝 كل شركاء المزرعة المضافين",
            "fr" to "🤝 Tous les partenaires ajoutés",
            "en" to "🤝 All Saved Farm Partners",
            "es" to "🤝 Todos los socios guardados",
            "pt" to "🤝 Todos os parceiros salvos",
            "hi" to "🤝 सभी सहेजे गए फार्म भागीदार"
        ),
        "universal_invoice_btn" to mapOf(
            "ar" to "🧾 فاتورة / طلب مالي",
            "fr" to "🧾 Facture / Demande fonds",
            "en" to "🧾 Invoice / Request Money",
            "es" to "🧾 Factura / Solicitar pago",
            "pt" to "🧾 Fatura / Solicitar valor",
            "hi" to "🧾 चालान / धन अनुरोध"
        ),
        "universal_invoice_dialog_title" to mapOf(
            "ar" to "إرسال فاتورة أو طلب دفعة مالية للمالك 🧾",
            "fr" to "Envoyer une facture ou demande de paiement 🧾",
            "en" to "Send Invoice or Financial Request to Owner 🧾",
            "es" to "Enviar factura o solicitud de pago al propietario 🧾",
            "pt" to "Enviar fatura ou solicitação financeira ao proprietário 🧾",
            "hi" to "मालिक को चालान या भुगतान अनुरोध भेजें 🧾"
        ),
        "universal_invoice_dialog_sub" to mapOf(
            "ar" to "متاح لجميع الحسابات الشخصية (عامل يطلب مالاً أو سلفة، بائع صيصان، تاجر أعلاف، طبيب بيطري، أو مربي أغنام وأبقار وزراعة). يُقيد في خزنة المالك تحت بند المصاريف بعد الاعتماد.",
            "fr" to "Disponible pour tous les comptes (ouvrier demandant une avance, vendeur de poussins, grossiste d'aliments, vétérinaire). Enregistré dans les dépenses après validation.",
            "en" to "Available for all personal accounts (worker requesting cash/advance, chick seller, feed wholesaler, or vet). Recorded under Owner Expenses upon approval.",
            "es" to "Disponible para todas las cuentas personales (trabajador solicitando efectivo, vendedor de pollitos, alimento o veterinario). Se registra en Gastos al aprobarse.",
            "pt" to "Disponível para todas as contas pessoais (trabalhador pedindo adiantamento, vendedor de pintinhos, ração ou veterinário). Registrado em Despesas após aprovação.",
            "hi" to "सभी व्यक्तिगत खातों (कार्यकर्ता, चूजा विक्रेता, चारा व्यापारी या पशु चिकित्सक) के लिए उपलब्ध। स्वीकृति पर खर्च में दर्ज होता है।"
        ),
        "inv_category_label" to mapOf(
            "ar" to "نوع الفاتورة / الطلب المالي",
            "fr" to "Type de facture / demande",
            "en" to "Invoice / Financial Request Type",
            "es" to "Tipo de factura / solicitud",
            "pt" to "Tipo de fatura / solicitação",
            "hi" to "चालान / वित्तीय अनुरोध प्रकार"
        ),
        "inv_type_worker_cash" to mapOf(
            "ar" to "👷‍♂️ طلب مال / أجرة عامل أو مصروف عنبر",
            "fr" to "👷‍♂️ Demande d'argent / Salaire ouvrier",
            "en" to "👷‍♂️ Worker Cash Request / Wage / Barn Supply",
            "es" to "👷‍♂️ Solicitud de efectivo / Salario de trabajador",
            "pt" to "👷‍♂️ Pedido de dinheiro / Salário de trabalhador",
            "hi" to "👷‍♂️ कार्यकर्ता नकद अनुरोध / वेतन"
        ),
        "inv_type_chicks" to mapOf(
            "ar" to "🐣 فاتورة بائع صيصان / كتاكيت",
            "fr" to "🐣 Facture vendeur de poussins",
            "en" to "🐣 Chick Supplier Invoice",
            "es" to "🐣 Factura de proveedor de pollitos",
            "pt" to "🐣 Fatura de fornecedor de pintinhos",
            "hi" to "🐣 चूजा आपूर्तिकर्ता चालान"
        ),
        "inv_type_feed" to mapOf(
            "ar" to "🌾 فاتورة تاجر أعلاف (بالقنطار ق)",
            "fr" to "🌾 Facture grossiste d'aliment (Quintal q)",
            "en" to "🌾 Feed Wholesaler Invoice (Quintals q)",
            "es" to "🌾 Factura mayorista de alimento (Quintales q)",
            "pt" to "🌾 Fatura atacadista de ração (Quintais q)",
            "hi" to "🌾 चारा थोक व्यापारी चालान (क्विंटल q)"
        ),
        "inv_type_meds" to mapOf(
            "ar" to "💊 فاتورة دواء / إشراف بيطري",
            "fr" to "💊 Facture médicaments / Vétérinaire",
            "en" to "💊 Veterinary Medicine / Service Invoice",
            "es" to "💊 Factura de medicamentos / Veterinario",
            "pt" to "💊 Fatura de medicamentos / Veterinário",
            "hi" to "💊 पशु चिकित्सा दवा / सेवा चालान"
        ),
        "inv_type_general_agritech" to mapOf(
            "ar" to "🐑 مصاريف عامة (دواجن・أغنام・أبقار・زراعة)",
            "fr" to "🐑 Dépenses générales (Volaille・Ovins・Bovins・Agri)",
            "en" to "🐑 General Farm Expense (Poultry・Sheep・Cattle・Crops)",
            "es" to "🐑 Gastos generales (Aves・Ovinos・Bovinos・Cultivos)",
            "pt" to "🐑 Despesas gerais (Aves・Ovinos・Bovinos・Lavoura)",
            "hi" to "🐑 सामान्य फार्म खर्च (पोल्ट्री・भेड़・मवेशी・फसल)"
        ),
        "inv_item_title_label" to mapOf(
            "ar" to "البيان / سبب طلب المال أو اسم الصنف",
            "fr" to "Désignation / Motif de la demande",
            "en" to "Description / Reason for Payment or Item Name",
            "es" to "Descripción / Motivo del pago o artículo",
            "pt" to "Descrição / Motivo do pagamento ou item",
            "hi" to "विवरण / भुगतान का कारण या वस्तु का नाम"
        ),
        "inv_qty_details_label" to mapOf(
            "ar" to "الكمية أو التفاصيل (مثال: سلفة أسبوعية / 40 ق علف / جرعة دواء)",
            "fr" to "Quantité ou détails (ex: Avance / 40 q aliment / Posologie)",
            "en" to "Quantity or Details (e.g. Weekly advance / 40 q feed / Dosage)",
            "es" to "Cantidad o detalles (ej: Adelanto / 40 q alimento / Dosis)",
            "pt" to "Quantidade ou detalhes (ex: Adiantamento / 40 q ração / Dose)",
            "hi" to "मात्रा या विवरण (उदा: अग्रिम / 40 q चारा / खुराक)"
        ),
        "worker_request_money_card_title" to mapOf(
            "ar" to "طلب مال أو إرسال فاتورة لمالك العنبر 🧾",
            "fr" to "Demander de l'argent ou envoyer une facture au propriétaire 🧾",
            "en" to "Request Money or Send Invoice to Barn Owner 🧾",
            "es" to "Solicitar dinero o enviar factura al propietario 🧾",
            "pt" to "Solicitar dinheiro ou enviar fatura ao proprietário 🧾",
            "hi" to "बार्न मालिक से पैसे का अनुरोध करें या चालान भेजें 🧾"
        ),
        "worker_request_money_card_sub" to mapOf(
            "ar" to "هل تحتاج مالاً لمصاريف العنبر أو دفعة من أجرتك؟ أرسل طلب صرف رسمي يصل فوراً لدردشة وخزنة المالك.",
            "fr" to "Besoin d'argent pour le bâtiment ou d'une avance ? Envoyez une demande directe au propriétaire.",
            "en" to "Need cash for barn supplies or a wage advance? Send an official payment request directly to the Owner's chat & treasury.",
            "es" to "¿Necesitas efectivo para el galpón o un adelanto? Envía una solicitud oficial al propietario.",
            "pt" to "Precisa de dinheiro para o galpão ou adiantamento? Envie um pedido oficial ao proprietário.",
            "hi" to "बार्न खर्च या वेतन अग्रिम के लिए पैसे चाहिए? सीधे मालिक को आधिकारिक भुगतान अनुरोध भेजें।"
        ),
        "weigh_crates_per_gross_label" to mapOf(
            "ar" to "📦 صناديق وزنة الممتلئ:",
            "fr" to "📦 Caisses par pesée pleine :",
            "en" to "📦 Crates per gross weigh:",
            "es" to "📦 Cajas por pesada llena:",
            "pt" to "📦 Caixas por pesagem cheia:",
            "hi" to "📦 प्रति सकल वजन क्रेट:"
        ),
        "weigh_full_crates_label" to mapOf(
            "ar" to "📦 صناديق وزنة الممتلئ:",
            "fr" to "📦 Caisses par pesée pleine :",
            "en" to "📦 Crates per gross weigh:",
            "es" to "📦 Cajas por pesada llena:",
            "pt" to "📦 Caixas por pesagem cheia:",
            "hi" to "📦 प्रति सकल वजन क्रेट:"
        ),
        "weigh_tare_col" to mapOf(
            "ar" to "الفارغ",
            "fr" to "Tare",
            "en" to "Tare",
            "es" to "Tara",
            "pt" to "Tara",
            "hi" to "खाली"
        ),
        "weigh_tare_input_label" to mapOf(
            "ar" to "الفارغ (كغ)",
            "fr" to "Tare (kg)",
            "en" to "Tare (kg)",
            "es" to "Tara (kg)",
            "pt" to "Tara (kg)",
            "hi" to "खाली (किग्रा)"
        ),
        "weigh_gross_col" to mapOf(
            "ar" to "الممتلئ",
            "fr" to "Brut",
            "en" to "Gross",
            "es" to "Bruto",
            "pt" to "Bruto",
            "hi" to "भरा"
        ),
        "weigh_gross_input_label" to mapOf(
            "ar" to "الممتلئ (كغ)",
            "fr" to "Brut (kg)",
            "en" to "Gross (kg)",
            "es" to "Bruto (kg)",
            "pt" to "Bruto (kg)",
            "hi" to "भरा (किग्रा)"
        ),
        "weigh_tare_placeholder" to mapOf(
            "ar" to "مثال: 50",
            "fr" to "Ex: 50",
            "en" to "e.g. 50",
            "es" to "Ej: 50",
            "pt" to "Ex: 50",
            "hi" to "उदा: 50"
        ),
        "weigh_gross_placeholder" to mapOf(
            "ar" to "مثال: 170",
            "fr" to "Ex: 170",
            "en" to "e.g. 170",
            "es" to "Ej: 170",
            "pt" to "Ex: 170",
            "hi" to "उदा: 170"
        ),
        "weigh_save_btn_short" to mapOf(
            "ar" to "حفظ ✓",
            "fr" to "Valider ✓",
            "en" to "Save ✓",
            "es" to "Guardar ✓",
            "pt" to "Salvar ✓",
            "hi" to "सहेजें ✓"
        ),
        "weigh_save_btn" to mapOf(
            "ar" to "حفظ ✓",
            "fr" to "Valider ✓",
            "en" to "Save ✓",
            "es" to "Guardar ✓",
            "pt" to "Salvar ✓",
            "hi" to "सहेजें ✓"
        ),
        "weigh_empty_table_hint" to mapOf(
            "ar" to "لم يتم تسجيل أي وزنة بعد.\nأدخل أوزان الفارغ أو الممتلئ في الأعلى واضغط [حفظ ✓].",
            "fr" to "Aucune pesée enregistrée.\nSaisissez le poids tare ou brut ci-dessus et appuyez sur [Valider ✓].",
            "en" to "No weigh-ins recorded yet.\nEnter tare or gross weights above and tap [Save ✓].",
            "es" to "Aún no hay pesadas registradas.\nIngrese el peso arriba y pulse [Guardar ✓].",
            "pt" to "Nenhuma pesagem registrada ainda.\nInsira o peso acima e toque em [Salvar ✓].",
            "hi" to "अभी तक कोई वजन दर्ज नहीं किया गया है।\nऊपर खाली या भरा वजन दर्ज करें और [सहेजें ✓] दबाएं।"
        ),
        "weigh_empty_table_msg" to mapOf(
            "ar" to "لم يتم تسجيل أي وزنة بعد.\nأدخل أوزان الفارغ أو الممتلئ في الأعلى واضغط [حفظ ✓].",
            "fr" to "Aucune pesée enregistrée.\nSaisissez le poids tare ou brut ci-dessus et appuyez sur [Valider ✓].",
            "en" to "No weigh-ins recorded yet.\nEnter tare or gross weights above and tap [Save ✓].",
            "es" to "Aún no hay pesadas registradas.\nIngrese el peso arriba y pulse [Guardar ✓].",
            "pt" to "Nenhuma pesagem registrada ainda.\nInsira o peso acima e toque em [Salvar ✓].",
            "hi" to "अभी तक कोई वजन दर्ज नहीं किया गया है।\nऊपर खाली या भरा वजन दर्ज करें और [सहेजें ✓] दबाएं।"
        ),
        "weigh_total_row_label" to mapOf(
            "ar" to "المجموع",
            "fr" to "Total",
            "en" to "Total",
            "es" to "Total",
            "pt" to "Total",
            "hi" to "कुल"
        ),
        "crates_short_unit" to mapOf(
            "ar" to "ص",
            "fr" to "c",
            "en" to "cr",
            "es" to "cj",
            "pt" to "cx",
            "hi" to "क्रेट"
        ),
        "weigh_delete_row_title" to mapOf(
            "ar" to "حذف وزنة من السطر",
            "fr" to "Supprimer la pesée de la ligne",
            "en" to "Delete weigh-in from row",
            "es" to "Eliminar pesada de la fila",
            "pt" to "Excluir pesagem da linha",
            "hi" to "पंक्ति से वजन हटाएं"
        ),
        "weigh_delete_dialog_title" to mapOf(
            "ar" to "حذف وزنة من السطر",
            "fr" to "Supprimer la pesée de la ligne",
            "en" to "Delete weigh-in from row",
            "es" to "Eliminar pesada de la fila",
            "pt" to "Excluir pesagem da linha",
            "hi" to "पंक्ति से वजन हटाएं"
        ),
        "weigh_delete_row_prompt" to mapOf(
            "ar" to "يحتوي هذا السطر على وزنة فارغ وممتلئ. ما الذي ترغب بحذفه؟",
            "fr" to "Cette ligne contient une tare et un poids brut. Que souhaitez-vous supprimer ?",
            "en" to "This row contains both tare and gross weights. What would you like to delete?",
            "es" to "Esta fila contiene tara y peso bruto. ¿Qué desea eliminar?",
            "pt" to "Esta linha contém tara e peso bruto. O que deseja excluir?",
            "hi" to "इस पंक्ति में खाली और भरा दोनों वजन हैं। आप क्या हटाना चाहते हैं?"
        ),
        "weigh_delete_gross_only" to mapOf(
            "ar" to "حذف الممتلئ فقط",
            "fr" to "Supprimer uniquement le brut",
            "en" to "Delete Gross Only",
            "es" to "Eliminar solo bruto",
            "pt" to "Excluir apenas bruto",
            "hi" to "केवल भरा वजन हटाएं"
        ),
        "weigh_delete_tare_only" to mapOf(
            "ar" to "حذف الفارغ فقط",
            "fr" to "Supprimer uniquement la tare",
            "en" to "Delete Tare Only",
            "es" to "Eliminar solo tara",
            "pt" to "Excluir apenas tara",
            "hi" to "केवल खाली वजन हटाएं"
        ),
        "weigh_delete_both_row" to mapOf(
            "ar" to "حذف السطر كاملاً (الفارغ والممتلئ)",
            "fr" to "Supprimer toute la ligne (Tare & Brut)",
            "en" to "Delete Entire Row (Tare & Gross)",
            "es" to "Eliminar fila completa (Tara y Bruto)",
            "pt" to "Excluir linha inteira (Tara e Bruto)",
            "hi" to "पूरी पंक्ति हटाएं (खाली और भरा)"
        ),
        "audit_feed_short" to mapOf(
            "ar" to "العلف",
            "fr" to "Aliment",
            "en" to "Feed",
            "es" to "Alimento",
            "pt" to "Ração",
            "hi" to "चारा"
        ),
        "audit_water_short" to mapOf(
            "ar" to "الماء",
            "fr" to "Eau",
            "en" to "Water",
            "es" to "Agua",
            "pt" to "Água",
            "hi" to "पानी"
        ),
        "audit_mortality_short" to mapOf(
            "ar" to "النافق",
            "fr" to "Mortalité",
            "en" to "Mortality",
            "es" to "Mortalidad",
            "pt" to "Mortalidade",
            "hi" to "मृत्यु"
        ),
        "audit_temp_short" to mapOf(
            "ar" to "الحرارة",
            "fr" to "Temp",
            "en" to "Temp",
            "es" to "Temp",
            "pt" to "Temp",
            "hi" to "तापमान"
        ),
        "vet_clinic_id_card_title" to mapOf(
            "ar" to "🩺 بطاقة العيادة البيطرية",
            "fr" to "🩺 Carte de la Clinique Vétérinaire",
            "en" to "🩺 Vet Clinic ID Card",
            "es" to "🩺 Tarjeta de Clínica Veterinaria",
            "pt" to "🩺 Cartão da Clínica Veterinária",
            "hi" to "🩺 पशु चिकित्सा क्लिनिक कार्ड"
        ),
        "vet_clinic_id_card_subtitle" to mapOf(
            "ar" to "الإشراف البيطري الميداني، خزنة العيادة، وتنبيهات اللقاحات",
            "fr" to "Supervision des bâtiments, trésorerie clinique et alertes vaccins",
            "en" to "Supervised barns, clinic treasury & regional vaccine alerts",
            "es" to "Galpones supervisados, tesorería clínica y alertas de vacunas",
            "pt" to "Galpões supervisionados, tesouraria clínica e alertas de vacinas",
            "hi" to "पर्यवेक्षित बार्न, क्लिनिक खजाना और टीका अलर्ट"
        ),
        "vet_clinic_stat_barns" to mapOf(
            "ar" to "العنابر",
            "fr" to "Bâtiments",
            "en" to "Barns",
            "es" to "Galpones",
            "pt" to "Galpões",
            "hi" to "बार्न"
        ),
        "vet_clinic_stat_treasury" to mapOf(
            "ar" to "خزنة العيادة",
            "fr" to "Trésorerie",
            "en" to "Treasury",
            "es" to "Tesorería",
            "pt" to "Tesouraria",
            "hi" to "खजाना"
        ),
        "vet_clinic_stat_alerts" to mapOf(
            "ar" to "تنبيهات اللقاحات",
            "fr" to "Alertes Vaccins",
            "en" to "Vaccine Alerts",
            "es" to "Alertas Vacunas",
            "pt" to "Alertas Vacinas",
            "hi" to "टीका अलर्ट"
        ),
        "open_vet_clinic_workspace_btn" to mapOf(
            "ar" to "🏥 فتح مكتب العيادة البيطرية",
            "fr" to "🏥 Ouvrir le Cabinet Vétérinaire",
            "en" to "🏥 Open Vet Clinic Workspace",
            "es" to "🏥 Abrir Consultorio Veterinario",
            "pt" to "🏥 Abrir Consultório Veterinário",
            "hi" to "🏥 वेटरनरी क्लिनिक कार्यक्षेत्र खोलें"
        ),
        "vet_workspace_title" to mapOf(
            "ar" to "🏥 مكتب العيادة البيطرية",
            "fr" to "🏥 Cabinet Vétérinaire",
            "en" to "🏥 Vet Clinic Workspace",
            "es" to "🏥 Consultorio Veterinario",
            "pt" to "🏥 Consultório Veterinário",
            "hi" to "🏥 वेटरनरी क्लिनिक"
        ),
        "profession_onboarding_title" to mapOf(
            "ar" to "مرحباً بك! ما هو تخصصك أو مجال عملك؟",
            "fr" to "Bienvenue ! Quelle est votre profession ou spécialité ?",
            "en" to "Welcome! What is your profession or field of work?",
            "es" to "¡Bienvenido! ¿Cuál es su profesión o área de trabajo?",
            "pt" to "Bem-vindo! Qual é a sua profissão ou área de atuação?",
            "hi" to "स्वागत है! आपका पेशा या कार्यक्षेत्र क्या है?"
        ),
        "profession_onboarding_subtitle" to mapOf(
            "ar" to "اختر مجال عملك لتخصيص أدوات الإدارة والسجلات (يمكنك تغييره لاحقاً من الإعدادات ⚙️)",
            "fr" to "Choisissez votre spécialité pour adapter vos outils (modifiable dans Paramètres ⚙️)",
            "en" to "Select your specialty to tailor your management workspace (can be changed anytime in Settings ⚙️)",
            "es" to "Seleccione su especialidad para adaptar las herramientas (modificable en Ajustes ⚙️)",
            "pt" to "Selecione sua especialidade para personalizar as ferramentas (alterável em Configurações ⚙️)",
            "hi" to "अपने प्रबंधन उपकरणों को अनुकूलित करने के लिए अपना पेशा चुनें (सेटिंग्स ⚙️ में बदला जा सकता है)"
        ),
        "profession_onboarding_confirm_btn" to mapOf(
            "ar" to "تأكيد والبدء ✓",
            "fr" to "Confirmer et commencer ✓",
            "en" to "Confirm & Start ✓",
            "es" to "Confirmar y comenzar ✓",
            "pt" to "Confirmar e começar ✓",
            "hi" to "पुष्टि करें और शुरू करें ✓"
        ),
        "profession_card_owner_title" to mapOf(
            "ar" to "🏡 مالك مزرعة وعنابر",
            "fr" to "🏡 Propriétaire de ferme et bâtiments",
            "en" to "🏡 Farm & Barn Owner",
            "es" to "🏡 Propietario de granja y galpones",
            "pt" to "🏡 Proprietário de granja e galpões",
            "hi" to "🏡 फार्म और बार्न मालिक"
        ),
        "profession_card_owner_desc" to mapOf(
            "ar" to "إدارة العنابر، السجل اليومي، الخزنة، وحسابات FCR والوزن",
            "fr" to "Gestion des bâtiments, registre quotidien, trésorerie et FCR",
            "en" to "Manage barns, daily ledger, treasury, FCR & live weight",
            "es" to "Gestión de galpones, registro diario, tesorería y FCR",
            "pt" to "Gestão de galpões, registro diário, tesouraria e FCR",
            "hi" to "बार्न, दैनिक रजिस्टर, खजाना और FCR का प्रबंधन"
        ),
        "profession_card_vet_title" to mapOf(
            "ar" to "🩺 طبيب بيطري مستقل",
            "fr" to "🩺 Vétérinaire indépendant",
            "en" to "🩺 Independent Veterinarian",
            "es" to "🩺 Veterinario independiente",
            "pt" to "🩺 Veterinário independente",
            "hi" to "🩺 स्वतंत्र पशु चिकित्सक"
        ),
        "profession_card_vet_desc" to mapOf(
            "ar" to "الإشراف على العنابر، الوصفات الطبية، اللقاحات، وصيدلية العيادة",
            "fr" to "Supervision des bâtiments, ordonnances, vaccins et pharmacie",
            "en" to "Supervise barns, medical records, vaccines & clinic pharmacy",
            "es" to "Supervisión de galpones, recetas médicas, vacunas y farmacia",
            "pt" to "Supervisão de galpões, receitas médicas, vacinas e farmácia",
            "hi" to "बार्न पर्यवेक्षण, चिकित्सा नुस्खे, टीके और क्लिनिक फार्मेसी"
        ),
        "profession_card_worker_title" to mapOf(
            "ar" to "👷 مشرف عنبر ميداني",
            "fr" to "👷 Superviseur de bâtiment",
            "en" to "👷 Field Barn Supervisor",
            "es" to "👷 Supervisor de galpón",
            "pt" to "👷 Supervisor de galpão",
            "hi" to "👷 फील्ड बार्न पर्यवेक्षक"
        ),
        "profession_card_worker_desc" to mapOf(
            "ar" to "تسجيل العلف والماء والنفوق اليومي وطلب المصاريف من المالك",
            "fr" to "Saisie quotidienne d'aliment, eau, mortalité et demandes de fonds",
            "en" to "Log daily feed, water, mortality & submit expense requests",
            "es" to "Registro diario de alimento, agua, mortalidad y solicitudes",
            "pt" to "Registro diário de ração, água, mortalidade e solicitações",
            "hi" to "दैनिक चारा, पानी, मृत्यु दर दर्ज करें और खर्च का अनुरोध भेजें"
        ),
        "profession_card_feed_title" to mapOf(
            "ar" to "🌾 مورد أعلاف وذرة وصويا",
            "fr" to "🌾 Fournisseur d'aliments (Maïs / Soja)",
            "en" to "🌾 Feed, Corn & Soy Supplier",
            "es" to "🌾 Proveedor de alimento, maíz y soja",
            "pt" to "🌾 Fornecedor de ração, milho e soja",
            "hi" to "🌾 चारा, मक्का और सोया आपूर्तिकर्ता"
        ),
        "profession_card_feed_desc" to mapOf(
            "ar" to "توريد الأعلاف بالقنطار وإرسال فواتير التوريد للمزارع",
            "fr" to "Livraison d'aliments au quintal et facturation aux fermes",
            "en" to "Supply feed in quintals and send invoices directly to farms",
            "es" to "Suministro de alimento en quintales y facturación a granjas",
            "pt" to "Fornecimento de ração em quintais e faturamento para granjas",
            "hi" to "क्विंटल में चारा आपूर्ति और फार्मों को चालान भेजना"
        ),
        "profession_card_hatchery_title" to mapOf(
            "ar" to "🐣 مفرخة وبائع صيصان",
            "fr" to "🐣 Couvoir et vendeur de poussins",
            "en" to "🐣 Hatchery & Chick Seller",
            "es" to "🐣 Incubadora y vendedor de pollitos",
            "pt" to "🐣 Incubatório e vendedor de pintinhos",
            "hi" to "🐣 हैचरी और चूजा विक्रेता"
        ),
        "profession_card_hatchery_desc" to mapOf(
            "ar" to "حجوزات دفعات الكتاكيت وإصدار فواتير التسليم للمربين",
            "fr" to "Réservations de lots de poussins et factures de livraison",
            "en" to "Manage day-old chick bookings and issue delivery invoices",
            "es" to "Reservas de lotes de pollitos y facturas de entrega",
            "pt" to "Reservas de lotes de pintinhos e faturas de entrega",
            "hi" to "चूजों की बुकिंग और डिलीवरी चालान जारी करना"
        ),
        "profession_card_trader_title" to mapOf(
            "ar" to "🚚 تاجر دجاج جملة",
            "fr" to "🚚 Grossiste de volaille",
            "en" to "🚚 Wholesale Broiler Trader",
            "es" to "🚚 Comerciante mayorista de pollos",
            "pt" to "🚚 Comerciante atacadista de frangos",
            "hi" to "🚚 थोक ब्रायलर व्यापारी"
        ),
        "profession_card_trader_desc" to mapOf(
            "ar" to "شراء الدفعات الجاهزة، جدول القبان السريع، وحسابات الوزن الصافي",
            "fr" to "Achat de lots prêts, pesée rapide et calcul du poids net",
            "en" to "Buy finished flocks, batch weighing & net weight settlement",
            "es" to "Compra de lotes listos, pesaje rápido y liquidación de peso neto",
            "pt" to "Compra de lotes prontos, pesagem rápida e peso líquido",
            "hi" to "तैयार बैच खरीदें, त्वरित वजन और शुद्ध वजन हिसाब"
        ),
        "profession_card_importer_title" to mapOf(
            "ar" to "📦 مستورد أدوية بيطرية",
            "fr" to "📦 Importateur de médicaments vétérinaires",
            "en" to "📦 Veterinary Medicine Importer",
            "es" to "📦 Importador de medicamentos veterinarios",
            "pt" to "📦 Importador de medicamentos veterinários",
            "hi" to "📦 पशु चिकित्सा दवा आयातक"
        ),
        "profession_card_importer_desc" to mapOf(
            "ar" to "توزيع اللقاحات والأدوية البيطرية للعيادات والمزارع",
            "fr" to "Distribution de vaccins et médicaments aux cliniques et fermes",
            "en" to "Distribute vaccines & veterinary pharmaceuticals to clinics",
            "es" to "Distribución de vacunas y medicamentos a clínicas y granjas",
            "pt" to "Distribuição de vacinas e medicamentos para clínicas e granjas",
            "hi" to "क्लिनिकों और फार्मों को टीके और दवाएं वितरित करें"
        ),
        "settings_hub_row_profession_title" to mapOf(
            "ar" to "الصفة المهنية ونوع الحساب",
            "fr" to "Profession et type de compte",
            "en" to "Profession & Account Mode",
            "es" to "Profesión y tipo de cuenta",
            "pt" to "Profissão e tipo de conta",
            "hi" to "पेशा और खाता प्रकार"
        ),
        "ai_vet_coming_soon_title" to mapOf(
            "ar" to "المساعد البيطري الذكي (AI Vet) — قيد التطوير",
            "fr" to "Assistant Vétérinaire Intelligent (AI Vet) — En cours de développement",
            "en" to "Smart Veterinary Assistant (AI Vet) — Under Development",
            "es" to "Asistente Veterinario Inteligente (AI Vet) — En desarrollo",
            "pt" to "Assistente Veterinário Inteligente (AI Vet) — Em desenvolvimento",
            "hi" to "स्मार्ट पशु चिकित्सा सहायक (AI Vet) — विकास के अधीन"
        ),
        "ai_vet_coming_soon_subtitle" to mapOf(
            "ar" to "هذه الميزة ليست متوفرة حالياً. نعمل على تطويرها وتدريبها بيطرياً لتكون متاحة لك في التحديثات القادمة بإذن الله.",
            "fr" to "Cette fonctionnalité n'est pas encore disponible. Nous la développons et l'entraînons cliniquement pour les prochaines mises à jour.",
            "en" to "This feature is not available right now. We are actively developing and clinically training it for upcoming updates.",
            "es" to "Esta función no está disponible actualmente. Estamos desarrollándola y entrenándola clínicamente para próximas actualizaciones.",
            "pt" to "Este recurso não está disponível no momento. Estamos desenvolvendo e treinando clinicamente para as próximas atualizações.",
            "hi" to "यह सुविधा अभी उपलब्ध नहीं है। हम आगामी अपडेट के लिए इसे विकसित और प्रशिक्षित कर रहे हैं।"
        ),
        "personal_qr_sheet_title" to mapOf(
            "ar" to "رمز الحساب الشخصي والمسح السريع (QR)",
            "fr" to "Mon Code QR Personnel & Scanner",
            "en" to "Personal Account QR & Quick Scanner",
            "es" to "Código QR Personal y Escáner",
            "pt" to "Código QR Pessoal e Scanner",
            "hi" to "व्यक्तिगत खाता QR और स्कैनर"
        ),
        "personal_qr_tab_my_code" to mapOf(
            "ar" to "رمزي الشخصي QR",
            "fr" to "Mon QR Personnel",
            "en" to "My Personal QR",
            "es" to "Mi QR Personal",
            "pt" to "Meu QR Pessoal",
            "hi" to "मेरा व्यक्तिगत QR"
        ),
        "personal_qr_tab_scan" to mapOf(
            "ar" to "مسح رمز QR 📷",
            "fr" to "Scanner un QR 📷",
            "en" to "Scan QR Code 📷",
            "es" to "Escanear QR 📷",
            "pt" to "Escanear QR 📷",
            "hi" to "QR कोड स्कैन करें 📷"
        ),
        "personal_qr_whatsapp_subtitle" to mapOf(
            "ar" to "امسح هذا الرمز عبر الكاميرا لإضافتي مباشرة في الدردشة (مثل واتساب)",
            "fr" to "Scannez ce code avec la caméra pour m'ajouter directement au chat (comme WhatsApp)",
            "en" to "Scan this code with the camera to add me directly in chat (like WhatsApp)",
            "es" to "Escanea este código con la cámara para agregarme directamente al chat (como WhatsApp)",
            "pt" to "Escaneie este código com a câmera para me adicionar diretamente no chat (como WhatsApp)",
            "hi" to "मुझे सीधे चैट में जोड़ने के लिए कैमरे से इस कोड को स्कैन करें (व्हाट्सएप की तरह)"
        ),
        "invoice_sig_dialog_title" to mapOf(
            "ar" to "مراجعة وتوقيع اعتماد الصرف ✍️",
            "fr" to "Vérification et signature d'approbation ✍️",
            "en" to "Review & Sign Disbursement Approval ✍️",
            "es" to "Revisión y firma de aprobación ✍️",
            "pt" to "Revisão e assinatura de aprovação ✍️",
            "hi" to "भुगतान स्वीकृति की समीक्षा और हस्ताक्षर ✍️"
        ),
        "invoice_sig_deduction_notice" to mapOf(
            "ar" to "سيتم خصم هذا المبلغ رسمياً من خزنة المزرعة بعد توقيعك واعتمادك",
            "fr" to "Ce montant sera officiellement déduit de la trésorerie après votre signature et approbation",
            "en" to "This amount will be officially deducted from the farm treasury after your signature and approval",
            "es" to "Este monto se descontará oficialmente de la tesorería tras su firma y aprobación",
            "pt" to "Este valor será oficialmente deduzido da tesouraria após sua assinatura e aprovação",
            "hi" to "आपके हस्ताक्षर और स्वीकृति के बाद यह राशि आधिकारिक तौर पर फार्म के खजाने से काट ली जाएगी"
        ),
        "invoice_sig_pad_label" to mapOf(
            "ar" to "✍️ وقّع بإصبعك هنا لتأكيد القراءة والاعتماد",
            "fr" to "✍️ Signez avec votre doigt ici pour confirmer",
            "en" to "✍️ Sign with your finger here to confirm reading & approval",
            "es" to "✍️ Firme con su dedo aquí para confirmar lectura y aprobación",
            "pt" to "✍️ Assine com o dedo aqui para confirmar a leitura e aprovação",
            "hi" to "✍️ पढ़ने और स्वीकृति की पुष्टि करने के लिए यहाँ अपनी उंगली से हस्ताक्षर करें"
        ),
        "invoice_sig_clear_btn" to mapOf(
            "ar" to "مسح التوقيع",
            "fr" to "Effacer la signature",
            "en" to "Clear Signature",
            "es" to "Borrar firma",
            "pt" to "Limpar assinatura",
            "hi" to "हस्ताक्षर साफ़ करें"
        ),
        "invoice_sig_confirm_btn" to mapOf(
            "ar" to "✓ حفظ التوقيع واعتماد الصرف",
            "fr" to "✓ Enregistrer la signature et approuver",
            "en" to "✓ Save Signature & Approve Payment",
            "es" to "✓ Guardar firma y aprobar pago",
            "pt" to "✓ Salvar assinatura e aprovar pagamento",
            "hi" to "✓ हस्ताक्षर सहेजें और भुगतान स्वीकृत करें"
        ),
        "sig_clear_short" to mapOf(
            "ar" to "مسح",
            "fr" to "Effacer",
            "en" to "Clear",
            "es" to "Borrar",
            "pt" to "Limpar",
            "hi" to "साफ़ करें"
        ),
        "sig_breeder_signed_badge" to mapOf(
            "ar" to "✓ تم توقيع المربي",
            "fr" to "✓ Signé par l'éleveur",
            "en" to "✓ Breeder Signed",
            "es" to "✓ Firmado por el criador",
            "pt" to "✓ Assinado pelo produtor",
            "hi" to "✓ पालक द्वारा हस्ताक्षरित"
        ),
        "sig_buyer_signed_badge" to mapOf(
            "ar" to "✓ تم توقيع المشتري",
            "fr" to "✓ Signé par l'acheteur",
            "en" to "✓ Buyer Signed",
            "es" to "✓ Firmado por el comprador",
            "pt" to "✓ Assinado pelo comprador",
            "hi" to "✓ खरीदार द्वारा हस्ताक्षरित"
        ),
        "med_drinking_badge_prefix" to mapOf(
            "ar" to "💊 شرب الدواء:",
            "fr" to "💊 Médicament administré :",
            "en" to "💊 Medicine Administered:",
            "es" to "💊 Medicamento administrado:",
            "pt" to "💊 Medicamento administrado:",
            "hi" to "💊 दी गई दवा:"
        ),
        "confirm_medicine_water_today_label" to mapOf(
            "ar" to "💊 تأكيد إعطاء الدواء في الماء اليوم (شرب الدواء ✓)",
            "fr" to "💊 Confirmer l'administration du médicament dans l'eau aujourd'hui (✓)",
            "en" to "💊 Confirm giving medicine in water today (Medicine Given ✓)",
            "es" to "💊 Confirmar medicina en el agua hoy (Medicina dada ✓)",
            "pt" to "💊 Confirmar medicamento na água hoje (Medicamento dado ✓)",
            "hi" to "💊 आज पानी में दवा देने की पुष्टि करें (दवा दी गई ✓)"
        ),
        "medicine_water_name_input_label" to mapOf(
            "ar" to "اسم الدواء / الوصفة المُعطاة في الماء 💊",
            "fr" to "Nom du médicament / prescription dans l'eau 💊",
            "en" to "Medicine / Prescription given in water 💊",
            "es" to "Nombre del medicamento / receta en el agua 💊",
            "pt" to "Nome do medicamento / receita na água 💊",
            "hi" to "पानी में दी गई दवा / नुस्खे का नाम 💊"
        ),
        "badge_day_med_pill" to mapOf(
            "ar" to "💊 دواء",
            "fr" to "💊 Méd.",
            "en" to "💊 Med",
            "es" to "💊 Med",
            "pt" to "💊 Med",
            "hi" to "💊 दवा"
        ),
        "medicine_orders_ledger_title" to mapOf(
            "ar" to "💊 سجل الأدوية والوصفات البيطرية",
            "fr" to "💊 Registre des Médicaments & Ordonnances Vétérinaires",
            "en" to "💊 Medicines & Veterinary Prescriptions Ledger",
            "es" to "💊 Registro de Medicamentos y Recetas Veterinarias",
            "pt" to "💊 Registro de Medicamentos e Receitas Veterinárias",
            "hi" to "💊 दवाइयों और पशु चिकित्सा नुस्खों का रजिस्टर"
        ),
        "total_medicine_expenses_label" to mapOf(
            "ar" to "إجمالي مصاريف الأدوية واللقاحات",
            "fr" to "Total Dépenses Médicaments & Vaccins",
            "en" to "Total Medicine & Vaccine Expenses",
            "es" to "Total Gastos en Medicamentos y Vacunas",
            "pt" to "Total de Despesas com Medicamentos e Vacinas",
            "hi" to "कुल दवा और टीकाकरण खर्च"
        ),
        "total_prescriptions_count_label" to mapOf(
            "ar" to "عدد الوصفات المسجلة",
            "fr" to "Nombre d'ordonnances enregistrées",
            "en" to "Recorded Prescriptions Count",
            "es" to "Número de recetas registradas",
            "pt" to "Número de receitas registradas",
            "hi" to "दर्ज नुस्खों की संख्या"
        ),
        "th_med_day" to mapOf(
            "ar" to "اليوم",
            "fr" to "Jour",
            "en" to "Day",
            "es" to "Día",
            "pt" to "Dia",
            "hi" to "दिन"
        ),
        "th_med_prescription_dosage" to mapOf(
            "ar" to "الدواء والوصفة (الجرعة)",
            "fr" to "Médicament & Posologie",
            "en" to "Medicine & Prescription (Dosage)",
            "es" to "Medicamento y Dosis",
            "pt" to "Medicamento e Dosagem",
            "hi" to "दवा और खुराक"
        ),
        "th_med_status" to mapOf(
            "ar" to "الحالة",
            "fr" to "Statut",
            "en" to "Status",
            "es" to "Estado",
            "pt" to "Status",
            "hi" to "स्थिति"
        ),
        "th_med_total_price" to mapOf(
            "ar" to "السعر الإجمالي",
            "fr" to "Prix Total",
            "en" to "Total Price",
            "es" to "Precio Total",
            "pt" to "Preço Total",
            "hi" to "कुल कीमत"
        ),
        "med_status_approved_short" to mapOf(
            "ar" to "✓ معتمدة",
            "fr" to "✓ Approuvé",
            "en" to "✓ Approved",
            "es" to "✓ Aprobado",
            "pt" to "✓ Aprovado",
            "hi" to "✓ स्वीकृत"
        ),
        "med_status_pending_short" to mapOf(
            "ar" to "⏳ معلقة",
            "fr" to "⏳ En attente",
            "en" to "⏳ Pending",
            "es" to "⏳ Pendiente",
            "pt" to "⏳ Pendente",
            "hi" to "⏳ लंबित"
        ),
        "no_medicine_orders_yet" to mapOf(
            "ar" to "لا توجد وصفات أو فواتير أدوية مسجلة في هذه الدورة بعد.",
            "fr" to "Aucune ordonnance ou facture de médicament enregistrée pour ce lot.",
            "en" to "No medicine prescriptions or invoices recorded in this cycle yet.",
            "es" to "Aún no hay recetas ni facturas de medicamentos registradas en este lote.",
            "pt" to "Nenhuma receita ou fatura de medicamento registrada neste lote ainda.",
            "hi" to "इस चक्र में अभी तक कोई दवा नुस्खा या चालान दर्ज नहीं किया गया है।"
        ),
        "prescriptions_unit_short" to mapOf(
            "ar" to "وصفة",
            "fr" to "ordonnances",
            "en" to "prescriptions",
            "es" to "recetas",
            "pt" to "receitas",
            "hi" to "नुस्खे"
        ),
        "day_meds_given_section_title" to mapOf(
            "ar" to "💊 الأدوية والفيتامينات المعطاة في هذا اليوم",
            "fr" to "💊 Médicaments et vitamines administrés ce jour",
            "en" to "💊 Medicines & Vitamins Administered Today",
            "es" to "💊 Medicamentos y vitaminas administrados este día",
            "pt" to "💊 Medicamentos e vitaminas administrados neste dia",
            "hi" to "💊 इस दिन दी गई दवाएं और विटामिन"
        ),
        "day_med_drunk_confirmed_prefix" to mapOf(
            "ar" to "✓ تم شرب الدواء:",
            "fr" to "✓ Médicament administré dans l'eau :",
            "en" to "✓ Medicine drunk in water:",
            "es" to "✓ Medicamento administrado en agua:",
            "pt" to "✓ Medicamento administrado na água:",
            "hi" to "✓ पानी में दवा दी गई:"
        ),
        "btn_view_share_tx_receipt" to mapOf(
            "ar" to "🧾 عرض ومشاركة وصل المعاملة",
            "fr" to "🧾 Voir et partager le reçu de transaction",
            "en" to "🧾 View & Share Transaction Receipt",
            "es" to "🧾 Ver y compartir recibo de transacción",
            "pt" to "🧾 Ver e compartilhar recibo da transação",
            "hi" to "🧾 लेनदेन रसीद देखें और साझा करें"
        ),
        "personal_qr_scan_whatsapp_desc" to mapOf(
            "ar" to "وجه الكاميرا نحو رمز (QR) الحساب الشخصي لصديق أو تاجر أو طبيب بيطري لإضافته فوراً إلى قائمة الدردشة (مثل واتساب)",
            "fr" to "Dirigez la caméra vers le code QR personnel d'un ami, commerçant ou vétérinaire pour l'ajouter directement au chat (style WhatsApp)",
            "en" to "Point the camera at the personal QR code of a friend, trader, or vet to immediately add them to your chat list (like WhatsApp)",
            "es" to "Apunte la cámara al código QR personal de un amigo, comerciante o veterinario para agregarlo al instante al chat (como WhatsApp)",
            "pt" to "Aponte a câmera para o código QR pessoal de um amigo, comerciante ou veterinário para adicioná-lo imediatamente ao chat (como WhatsApp)",
            "hi" to "किसी मित्र, व्यापारी या पशु चिकित्सक को तुरंत चैट सूची में जोड़ने के लिए उनके व्यक्तिगत QR कोड पर कैमरा इंगित करें"
        ),
        "personal_qr_scan_add_contact_btn" to mapOf(
            "ar" to "✓ مسح وإضافة جهة الاتصال للدردشة",
            "fr" to "✓ Scanner et ajouter le contact au chat",
            "en" to "✓ Scan & Add Contact to Chat",
            "es" to "✓ Escanear y agregar contacto al chat",
            "pt" to "✓ Escanear e adicionar contato ao chat",
            "hi" to "✓ स्कैन करें और चैट में संपर्क जोड़ें"
        ),
        "gram_unit" to mapOf(
            "ar" to "غرام",
            "fr" to "g",
            "en" to "g",
            "es" to "g",
            "pt" to "g",
            "hi" to "ग्राम"
        ),
        "field_actual_bird_weight_grams_optional" to mapOf(
            "ar" to "⚖️ متوسط وزن الطائر الفعلي (غرام - اختياري)",
            "fr" to "⚖️ Poids moyen réel de l'oiseau (grammes - optionnel)",
            "en" to "⚖️ Actual Average Bird Weight (grams - optional)",
            "es" to "⚖️ Peso promedio real del ave (gramos - opcional)",
            "pt" to "⚖️ Peso médio real da ave (gramas - opcional)",
            "hi" to "⚖️ पक्षी का वास्तविक औसत वजन (ग्राम - वैकल्पिक)"
        ),
        "sales_exceeds_live_birds_warning" to mapOf(
            "ar" to "⚠️ العدد المدخل يتجاوز عدد الطيور الحية المتبقية في العنبر ({liveBirds} طائر)",
            "fr" to "⚠️ Le nombre saisi dépasse le nombre d'oiseaux vivants restants dans le bâtiment ({liveBirds} oiseaux)",
            "en" to "⚠️ Entered count exceeds remaining live birds in the barn ({liveBirds} birds)",
            "es" to "⚠️ La cantidad ingresada supera las aves vivas restantes en el galpón ({liveBirds} aves)",
            "pt" to "⚠️ A quantidade inserida excede as aves vivas restantes no galpão ({liveBirds} aves)",
            "hi" to "⚠️ दर्ज की गई संख्या बाड़े में शेष जीवित पक्षियों ({liveBirds} पक्षी) से अधिक है"
        ),
        "weight_not_measured_yet" to mapOf(
            "ar" to "غير مقاس بعد",
            "fr" to "Non mesuré",
            "en" to "Not measured yet",
            "es" to "Sin medir aún",
            "pt" to "Ainda não medido",
            "hi" to "अभी मापा नहीं गया"
        ),
        "ross308_reference_hint" to mapOf(
            "ar" to "مرجع Ross 308",
            "fr" to "Réf. Ross 308",
            "en" to "Ross 308 Ref.",
            "es" to "Ref. Ross 308",
            "pt" to "Ref. Ross 308",
            "hi" to "रॉस 308 संदर्भ"
        ),
        "withdrawal_active_banner_title" to mapOf(
            "ar" to "⏳ فترة سحب الدواء نشطة (متبقي {days} يوم)",
            "fr" to "⏳ Période d'attente médicamenteuse active ({days} j restants)",
            "en" to "⏳ Active Drug Withdrawal Period ({days} days left)",
            "es" to "⏳ Período de retiro de medicamento activo ({days} días restantes)",
            "pt" to "⏳ Período de carência ativo ({days} dias restantes)",
            "hi" to "⏳ दवा निकासी अवधि सक्रिय ({days} दिन शेष)"
        ),
        "withdrawal_active_sales_blocked" to mapOf(
            "ar" to "⛔ يُمنع بيع أو ذبح الطيور للاستهلاك خلال فترة سحب الدواء ({days} يوم متبقي لسلامة المستهلك).",
            "fr" to "⛔ Vente bloquée pendant la période d'attente médicamenteuse ({days} j restants).",
            "en" to "⛔ Bird sales are blocked during the active veterinary drug withdrawal period ({days} days remaining).",
            "es" to "⛔ Venta bloqueada durante el período de retiro del medicamento ({days} días restantes).",
            "pt" to "⛔ Venda bloqueada durante o período de carência do medicamento ({days} dias restantes).",
            "hi" to "⛔ सक्रिय दवा निकासी अवधि ({days} दिन शेष) के दौरान पक्षियों की बिक्री अवरुद्ध है।"
        ),
        "vet_withdrawal_days_label" to mapOf(
            "ar" to "⏳ فترة سحب الدواء بالأيام (0 للفيتامينات، 7 للمضادات)",
            "fr" to "⏳ Délai d'attente en jours (0 vitamines, 7 antibiotiques)",
            "en" to "⏳ Withdrawal Period in Days (0 vitamins, 7 antibiotics)",
            "es" to "⏳ Días de retiro (0 vitaminas, 7 antibióticos)",
            "pt" to "⏳ Período de carência em dias (0 vitaminas, 7 antibióticos)",
            "hi" to "⏳ दिनों में दवा निकासी अवधि (0 विटामिन, 7 एंटीबायोटिक)"
        ),
        "account_local_profile" to mapOf(
            "ar" to "حساب المزرعة المحلي",
            "fr" to "Profil local de la ferme",
            "en" to "Local Farm Profile",
            "es" to "Perfil local de la granja",
            "pt" to "Perfil local da granja",
            "hi" to "स्थानीय फार्म प्रोफ़ाइल"
        ),
        "paywall_sandbox_notice" to mapOf(
            "ar" to "🧪 وضع التفعيل التجريبي (Sandbox): تفعيل فوري بدون خصم بنكي حقيقي لأغراض الاختبار.",
            "fr" to "🧪 Mode test (Sandbox) : activation immédiate sans prélèvement bancaire réel.",
            "en" to "🧪 Sandbox / Demo Mode: Instant unlock without real billing charge.",
            "es" to "🧪 Modo de prueba (Sandbox): activación inmediata sin cargo bancario real.",
            "pt" to "🧪 Modo de teste (Sandbox): ativação imediata sem cobrança bancária real.",
            "hi" to "🧪 सैंडबॉक्स / डेमो मोड: वास्तविक शुल्क के बिना तत्काल अनलॉक।"
        ),
        "input_required_error" to mapOf(
            "ar" to "⚠️ يرجى إدخال القيم الإلزامية أولاً",
            "fr" to "⚠️ Veuillez saisir les champs obligatoires",
            "en" to "⚠️ Please enter mandatory values first",
            "es" to "⚠️ Por favor ingrese los valores obligatorios",
            "pt" to "⚠️ Insira os valores obrigatórios primeiro",
            "hi" to "⚠️ कृपया पहले अनिवार्य मान दर्ज करें"
        )
    )

    // Comprehensive Latin American Portuguese (AppLanguage.PT) dictionary for all app keys
    private val ptDict: Map<String, String> = mapOf(
        "start_date_label" to "Data de Início",
        "badge_estimated_weight" to "Peso Est.",
        "live_birds_label" to "Aves Vivas",
        "livability_label" to "Viabilidade",
        "treasury_expenses" to "Despesas",
        "btn_invite_team_short" to "👥+ Equipe",
        "climate_header_btn_label" to "Clima",
        "climate_modal_title" to "Clima ao Vivo e Resumo Rápido",
        "climate_modal_subtitle" to "Temperatura, amônia, ventilação e indicadores-chave do lote",
        "app_title" to "Gestor Avícola 🐥",
        "app_subtitle" to "Controle de galpões de frango de corte e produção diária",
        "role_owner" to "Produtor / Dono",
        "role_vet" to "Médico Veterinário",
        "role_worker" to "Supervisor de Galpão",
        "login_title_owner" to "Acesso do Produtor",
        "login_title_vet" to "Acesso do Veterinário",
        "login_title_worker" to "Acesso do Supervisor",
        "username_label" to "Usuário ou E-mail",
        "password_label" to "Senha",
        "login_btn" to "Entrar na Granja",
        "farms_title" to "Galpões e Instalações Avícolas",
        "owner_supervision" to "Supervisão Geral do Produtor",
        "open_notebook" to "Abrir Caderno do Galpão",
        "batch_label" to "Lote",
        "birds_unit" to "aves 🐥",
        "day_prefix" to "Dia",
        "tab_ledger" to "📋 Registro Diário",
        "tab_treasury" to "💰 Tesouraria",
        "tab_analytics" to "📊 Indicadores",
        "tab_vet" to "🩺 Veterinário",
        "th_day" to "🗓️ Dia",
        "th_feed" to "🌾 Ração (q)",
        "th_water" to "💧 Água (L)",
        "th_mortality" to "💀 Mortalidade",
        "th_temp" to "Temp (°C)",
        "th_gas" to "Gás",
        "th_vent" to "Ventilação",
        "sum_feed" to "🌾 Ração Total",
        "sum_water" to "💧 Água Total",
        "sum_mortality" to "💀 Mortalidade Total",
        "currency" to "BRL",
        "kg_unit" to "kg",
        "liter_unit" to "L",
        "treasury_title" to "💰 Tesouraria do Lote",
        "total_expenses" to "Despesas Totais",
        "total_income" to "Receitas Totais",
        "net_balance" to "Saldo Líquido",
        "btn_spend" to "🔴 Registrar Despesa 💸",
        "btn_income" to "🟢 Registrar Receita 💵",
        "tx_history_title" to "Histórico Financeiro do Lote",
        "analytics_title" to "📊 Indicadores de Desempenho (KPIs)",
        "liveability" to "💚 Viabilidade (Liveability)",
        "fcr" to "⚖️ Conversão Alimentar (FCR)",
        "est_weight" to "🐥 Peso Médio Estimado",
        "total_biomass" to "📊 Biomassa Viva Total",
        "epef" to "🏆 Fator de Eficiência (EPEF)",
        "cost_per_kg" to "🏷️ Custo por kg",
        "worker_log_title" to "Registro Diário do Trabalhador",
        "log_today_prefix" to "Registrar Dados de Hoje",
        "lock_report_btn" to "Confirmar e Bloquear Relatório 🔒",
        "locked_today_msg" to "Relatório de hoje confirmado e bloqueado 🔒",
        "settings_dialog_title" to "Configurações e Aparência ⚙️",
        "theme_section_title" to "🎨 Aparência da Tela e Fundo 🧊:",
        "theme_ice_blue" to "Azul Gelo Suave 🧊",
        "theme_farm_green" to "Verde Agrícola Natural 🌿",
        "theme_clean_white" to "Branco Puro Brilhante ☀️",
        "theme_night_mode" to "Modo Noturno Calmo 🌙",
        "language_section_title" to "🌐 Selecionar Idioma:",
        "farm_info_title" to "🏡 Informações do Galpão:",
        "save_and_close" to "Salvar e Fechar",
        "farm_1_name" to "Granja Modelo Al-Baraka (Galpão A)",
        "farm_1_location" to "Médéa - Sidi Naamane",
        "farm_2_name" to "Granja Avícola Al-Wifaq",
        "farm_2_location" to "Bouira - Ain Bessem",
        "vent_good" to "Boa",
        "vent_moderate" to "Moderada",
        "vent_issue" to "Alerta",
        "tx_chicks" to "🐣 Pintinhos (Ross 308)",
        "tx_feed" to "🌾 Ração Inicial (25 q)",
        "tx_gas" to "🔥 Gás e Aquecimento",
        "tx_meds" to "💊 Medicamentos e Vacinas",
        "tx_manure" to "🚜 Venda de Adubo Orgânico",
        "tx_chicken_sale" to "🍗 Venda de Frango de Corte",
        "tx_labor" to "🚚 Mão de Obra e Transporte",
        "tx_other_inc" to "Outras Receitas",
        "tx_other_exp" to "Outras Despesas",
        "med_vitamin" to "Eletrólitos + Vitamina AD3E",
        "med_antibiotic" to "Antibiótico Respiratório Preventivo",
        "med_1_dosage" to "1 ml / L por 3 dias",
        "med_1_note" to "Protocolo de recepção de pintinhos e prevenção de estresse",
        "med_2_dosage" to "0.5 g / L por 2 dias",
        "med_2_note" to "Leve ruído respiratório observado na ventilação noturna",
        "vet_records_title" to "🩺 Prontuário e Receitas Veterinárias",
        "dosage_label" to "Dosagem",
        "med_urgent_badge" to "⚠️ Urgente",
        "open_medical_records" to "Abrir Prontuário Médico",
        "category_label" to "Categoria:",
        "amount_label" to "Valor (BRL)",
        "confirm_btn" to "Confirmar",
        "cancel_btn" to "Cancelar",
        "cycle_label" to "Lote",
        "temp_label" to "Temp",
        "gas_label" to "Gás",
        "weighing_sales_title" to "Pesagem e Venda do Lote ⚖️🍗",
        "weighing_sales_subtitle" to "Calculadora Rápida (1 Pesagem = 5 Caixas)",
        "gross_weight_label" to "Peso Bruto (5 caixas cheias) kg",
        "tare_weight_label" to "Peso da Tara (caixas vazias) kg",
        "birds_per_crate_label" to "Aves por caixa:",
        "crate_8_birds" to "8 aves (40)",
        "crate_10_birds" to "10 aves (50)",
        "add_weight_btn" to "Adicionar Pesagem ➕",
        "weights_list_title" to "Lista de Pesagens Registradas:",
        "net_meat_weight" to "Peso Vivo Líquido",
        "total_birds_sold" to "Total de Aves Vendidas",
        "avg_bird_weight" to "Peso Médio por Ave",
        "price_per_kg_label" to "Preço de Mercado por kg hoje (BRL/kg)",
        "total_sales_value" to "Valor Total da Venda",
        "btn_issue_receipt" to "🧾 Fatura de Venda",
        "btn_issue_invoice" to "🧾 Fatura de Venda",
        "btn_migrate_treasury" to "Transferir para Tesouraria 💰",
        "receipt_dialog_title" to "Fatura de Venda de Frango Vivo 🧾",
        "receipt_migrated_success" to "Fatura salva e receita transferida para a tesouraria! 💰🎉",
        "weigh_number" to "Pesagem",
        "gross_short" to "Bruto B",
        "tare_short" to "Tara A",
        "net_short" to "Líquido",
        "birds_unit_short" to "aves",
        "buyer_label" to "Comprador / Comerciante:",
        "print_close_btn" to "Imprimir e Fechar Fatura",
        "btn_start_sales_flow" to "Vender 🍗",
        "sales_stage_1_title" to "Dados do Carregamento e Comprador 📋",
        "sales_stage_2_title" to "Tabela de Pesagem Rápida ⚖️",
        "buyer_name_label" to "Nome do Comprador / Comerciante",
        "truck_plate_label" to "Placa do Caminhão",
        "bird_age_days_label" to "Idade do Lote (dias)",
        "btn_proceed_to_weighing" to "Salvar e Iniciar Pesagem ➡️",
        "save_single_weigh_btn" to "Salvar Pesagem ✓",
        "save_tare_weigh_btn" to "Salvar Tara ✓",
        "save_gross_weigh_btn" to "Salvar Bruto ✓",
        "btn_finalize_and_migrate" to "Salvar Fatura e Transferir 💰",
        "sales_banner_desc" to "Carregamento do lote e entrega direta com registro rápido de pesos",
        "live_birds_count" to "🐔 Aves Vivas no Galpão",
        "total_barn_feed" to "🌾 Total de Ração Consumida",
        "feed_per_bird" to "🥣 Consumo Médio de Ração por Ave",
        "cost_per_bird" to "🐓 Custo por Ave",
        "bird_unit" to "ave",
        "tare_mode_label" to "Método de Tara das Caixas Vazias:",
        "tare_mode_detailed" to "Opção A: Registrar caixas vazias pesagem por pesagem",
        "tare_mode_direct" to "Opção B: Inserir peso total da tara em um único valor",
        "btn_new_cycle" to "+ Novo Galpão / Lote",
        "btn_start_new_cycle" to "Iniciar Novo Lote 🔄",
        "dialog_new_cycle_title" to "Criar Novo Galpão / Lote 🐣",
        "chick_expense_note" to "O custo dos pintinhos será deduzido automaticamente na tesouraria",
        "field_farm_name" to "Nome do Galpão / Granja",
        "field_location" to "Localização / Município",
        "field_bird_count" to "Quantidade de Pintinhos Alojados",
        "field_chick_price" to "Preço Unitário do Pintinho (BRL)",
        "btn_create_cycle" to "Criar e Iniciar Lote",
        "btn_cancel" to "Cancelar",
        "global_tab_barns" to "Galpões",
        "global_tab_chat" to "Chat",
        "global_tab_market" to "Mercado",
        "global_tab_ai_vet" to "IA Vet",
        "btn_add_post" to "+ Publicar Anúncio",
        "feed_badge_short" to "Estoque Ração",
        "quintal_short" to "q",
        "btn_add_team" to "Equipe QR",
        "qr_invite_sheet_title" to "Convidar Equipe (QR)",
        "qr_role_worker_btn" to "👷 Supervisor",
        "qr_role_vet_btn" to "👨‍⚕️ Veterinário",
        "qr_invite_instruction" to "Peça ao supervisor ou veterinário que escaneie este QR pelo aplicativo para ingressar imediatamente.",
        "btn_scan_qr_join" to "Escanear QR da Equipe 📷",
        "qr_scanner_sheet_title" to "Escanear Convite do Galpão 📷",
        "qr_scanner_desc" to "Aponte a câmera para o código QR do proprietário da granja para ativar seu acesso.",
        "btn_send_vet_invoice" to "Enviar Fatura de Medicamento 🧾",
        "vet_invoice_dialog_title" to "Fatura de Medicamento e Receita 💊🧾",
        "vet_invoice_med_label" to "Nome do Medicamento / Vacina 💊",
        "vet_invoice_dosage_label" to "Dosagem e Modo de Uso 🧪",
        "vet_invoice_note_label" to "Diagnóstico Clínico 🩺",
        "vet_invoice_amount_label" to "Valor da Fatura (BRL) 💰",
        "vet_invoice_urgent_label" to "Receita Urgente 🚨",
        "vet_invoice_confirm_btn" to "Enviar e Registrar Fatura 🧾",
        "universal_user_mode_general" to "👤 Usuário Geral (Sem Galpão)",
        "universal_user_mode_owner" to "👨‍🌾 Produtor / Dono",
        "vet_invoic" to "Fatura de Medicamento",
        "vet_invoice_btn" to "Fatura de Medicamento",
        "vet_invoice_dialog_subtitle" to "Registro financeiro direto e receita",
        "vet_med_name_label" to "Nome do medicamento / vacina",
        "vet_dosage_instructions_label" to "Dosagem e instruções",
        "vet_med_total_price_label" to "Custo total do medicamento",
        "vet_clinical_note_label" to "Nota clínica e diagnóstico",
        "vet_invoice_submit_btn" to "Enviar fatura",
        "vet_invoice_saved_banner" to "Fatura e receita",
        "add_contact_dialog_title" to "Adicionar novo contato",
        "add_contact_dialog_subtitle" to "Insira os dados do parceiro ou fornecedor",
        "contact_role_feed_supplier" to "Fornecedor de ração, milho e soja",
        "contact_role_chick_seller" to "Incubatório / Vendedor de pintinhos",
        "contact_role_vet_importer" to "Importador de medicamentos vet. 🧬",
        "btn_save_contact" to "Salvar contato ✓",
        "btn_send_financial_request" to "🧾 Enviar solicitação financeira",
        "fin_req_dialog_title" to "Solicitação financeira ao proprietário",
        "fin_req_dialog_subtitle" to "Vinculado à tesouraria para aprovação do proprietário",
        "fin_req_type_label" to "Tipo de solicitação / Fatura",
        "fin_req_cat_worker_advance" to "Salário de trabalhador / Adiantamento",
        "fin_req_cat_gas_maint" to "Compra de gás / Manutenção urgente",
        "fin_req_cat_feed_invoice" to "Fatura de ração (Milho / Soja)",
        "fin_req_cat_chick_invoice" to "Fatura de pintinhos (Incubatório)",
        "fin_req_cat_vet_invoice" to "Fatura de medicamentos e vacinas vet.",
        "fin_req_amount_label" to "Valor solicitado",
        "fin_req_submit_btn" to "Enviar solicitação ✓",
        "btn_approve_vet_invoice" to "Confirmar e Aprovar Pagamento ✓",
        "day_meds_given_section_title" to "💊 Medicamentos e vitaminas administrados neste dia",
        "day_med_drunk_confirmed_prefix" to "✓ Medicamento administrado na água:",
        "btn_view_share_tx_receipt" to "🧾 Ver e compartilhar recibo da transação",
        "personal_qr_scan_whatsapp_desc" to "Aponte a câmera para o código QR pessoal de um amigo, comerciante ou veterinário para adicioná-lo imediatamente ao chat (como WhatsApp)",
        "personal_qr_scan_add_contact_btn" to "✓ Escanear e adicionar contato ao chat",
        "gram_unit" to "g",
        "field_actual_bird_weight_grams_optional" to "⚖️ Peso médio real da ave (gramas - opcional)",
        "sales_exceeds_live_birds_warning" to "⚠️ A quantidade inserida excede as aves vivas restantes no galpão ({liveBirds} aves)",
        "weight_not_measured_yet" to "Ainda não medido",
        "ross308_reference_hint" to "Ref. Ross 308",
        "withdrawal_active_banner_title" to "⏳ Período de carência ativo ({days} dias restantes)",
        "withdrawal_active_sales_blocked" to "⛔ Venda bloqueada durante o período de carência do medicamento ({days} dias restantes).",
        "vet_withdrawal_days_label" to "⏳ Período de carência em dias (0 vitaminas, 7 antibióticos)",
        "account_local_profile" to "Perfil local da granja",
        "paywall_sandbox_notice" to "🧪 Modo de teste (Sandbox): ativação imediata sem cobrança bancária real.",
        "input_required_error" to "⚠️ Insira os valores obrigatórios primeiro"
    )

    fun hasKey(key: String): Boolean {
        return dict.containsKey(key) || ptDict.containsKey(key)
    }

    fun tr(key: String, lang: AppLanguage): String {
        val activeCurrency = globalCurrencyCode
        if (key == "currency" && !activeCurrency.isNullOrBlank()) {
            return activeCurrency
        }

        // Strict multi-language safe fallback chain:
        // 1) Requested language in dict
        // 2) Portuguese dedicated dictionary when PT
        // 3) English ("en") international fallback
        // 4) Arabic ("ar") primary fallback
        // 5) Sanitized human-readable string (NEVER expose raw underscore keys)
        val raw = dict[key]?.get(lang.code)
            ?: (if (lang == AppLanguage.PT) ptDict[key] else null)
            ?: dict[key]?.get("en")
            ?: dict[key]?.get("ar")
            ?: ptDict[key]
            ?: key.replace('_', ' ').replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        // Apply the user's globally chosen currency to all financial field labels
        if (!activeCurrency.isNullOrBlank() &&
            (key == "amount_label" || key == "price_per_kg_label" || key == "field_chick_price" || key == "vet_invoice_amount_label")
        ) {
            return raw
                .replace("(دج/كغ)", "($activeCurrency/${tr("kg_unit", lang)})")
                .replace("(DZD/kg)", "($activeCurrency/kg)")
                .replace("(BRL/kg)", "($activeCurrency/kg)")
                .replace("(دج)", "($activeCurrency)")
                .replace("(DZD)", "($activeCurrency)")
                .replace("(BRL)", "($activeCurrency)")
        }
        return raw
    }

    fun localizeAuditSummary(summary: String, lang: AppLanguage): String {
        if (summary.isBlank()) return summary
        val feedLabel = tr("audit_feed_short", lang)
        val waterLabel = tr("audit_water_short", lang)
        val mortLabel = tr("audit_mortality_short", lang)
        val tempLabel = tr("audit_temp_short", lang)
        val qShort = tr("quintal_short", lang)
        return summary
            .replace("العلف:", "$feedLabel:")
            .replace("الماء:", "$waterLabel:")
            .replace("النافق:", "$mortLabel:")
            .replace("الحرارة:", "$tempLabel:")
            .replace(" ق ", " $qShort ")
    }

    fun formatQuintalNumber(quintals: Double): String {
        if (kotlin.math.abs(quintals) < 0.0001) return "0"
        val formatted = String.format(java.util.Locale.US, "%.2f", quintals)
        return formatted.trimEnd('0').trimEnd('.')
    }

    /**
     * Universal Feed Unit ("ق" / Quintal) formatter for Header & Card Feed Inventory Badge:
     * Strictly displays feed inventory in Quintals by ALWAYS dividing Kg by 100.0 (e.g., 15 Kg -> `0.15 ق`, 50 Kg -> `0.5 ق`, 740 Kg -> `7.4 ق`).
     */
    fun formatDualFeedInventoryBadge(
        feedInventoryKg: Double,
        lang: AppLanguage,
        unitPref: FeedUnitPreference = FeedUnitPreference.QUINTAL
    ): String {
        val quintals = feedInventoryKg / 100.0
        val quintalsStr = formatQuintalNumber(quintals)
        val prefix = tr("feed_badge_short", lang)
        val qShort = tr("quintal_short", lang)
        return "$prefix: $quintalsStr $qShort 🌾"
    }

    /**
     * Formats feed purchase quantities strictly in Quintals "ق" by ALWAYS dividing Kg by 100.0 (e.g., "40 ق", "0.5 ق", "0.15 ق").
     */
    fun formatBulkFeedPurchaseQuantity(
        estimatedKg: Double,
        lang: AppLanguage,
        unitPref: FeedUnitPreference = FeedUnitPreference.QUINTAL
    ): String {
        val quintals = estimatedKg / 100.0
        val qStr = formatQuintalNumber(quintals)
        val qShort = tr("quintal_short", lang)
        return "$qStr $qShort"
    }

    fun getLocalizedFarmName(farmId: String, lang: AppLanguage, fallbackName: String = ""): String {
        return when (farmId) {
            "farm_1" -> tr("farm_1_name", lang)
            "farm_2" -> tr("farm_2_name", lang)
            "farm_3" -> tr("farm_3_name", lang)
            else -> fallbackName.ifEmpty {
                val farm = AgritechMockDb.farms.firstOrNull { it.id == farmId }
                farm?.name ?: farmId
            }
        }
    }

    fun getLocalizedFarmLocation(farmId: String, lang: AppLanguage, fallbackLocation: String = ""): String {
        return when (farmId) {
            "farm_1" -> tr("farm_1_location", lang)
            "farm_2" -> tr("farm_2_location", lang)
            "farm_3" -> tr("farm_3_location", lang)
            else -> fallbackLocation.ifEmpty {
                val farm = AgritechMockDb.farms.firstOrNull { it.id == farmId }
                farm?.location ?: "الجزائر"
            }
        }
    }

    fun getLocalizedVentilation(status: String, lang: AppLanguage): String {
        return when {
            status.contains("جيدة") || status.equals("Good", true) || status.equals("Bonne", true) -> tr("vent_good", lang)
            status.contains("متوسطة") || status.equals("Moderate", true) || status.equals("Moyenne", true) -> tr("vent_moderate", lang)
            else -> tr("vent_issue", lang)
        }
    }

    fun getLocalizedTxCategory(
        cat: String,
        lang: AppLanguage,
        unitPref: FeedUnitPreference = FeedUnitPreference.QUINTAL
    ): String {
        if (cat.contains("علف") || cat.contains("Feed", true) || cat.contains("Aliment", true) || cat.contains("Pienso", true)) {
            val rawCat = cat.removePrefix("🌾").trim()
            val parenStart = rawCat.indexOf('(')
            val parenEnd = rawCat.lastIndexOf(')')
            if (parenStart > 0 && parenEnd > parenStart) {
                val designation = rawCat.substring(0, parenStart).trim()
                val insideParen = rawCat.substring(parenStart + 1, parenEnd).trim()
                val numericVal = Regex("""(\d+(\.\d+)?)""").find(insideParen)?.groupValues?.get(1)?.toDoubleOrNull()
                if (numericVal != null) {
                    val estimatedKg = if (insideParen.contains("طن") || insideParen.contains("ton", ignoreCase = true)) {
                        numericVal * 1000.0
                    } else if (insideParen.contains("كغ") || insideParen.contains("kg", ignoreCase = true)) {
                        numericVal
                    } else {
                        numericVal * 100.0
                    }
                    val formattedQty = formatBulkFeedPurchaseQuantity(estimatedKg, lang, unitPref)
                    return "🌾 $designation ($formattedQty)"
                }
                return "🌾 $rawCat"
            }
            return tr("tx_feed", lang)
        }
        if (cat.contains("فاتورة دواء") || cat.contains("Medicine Invoice", ignoreCase = true)) {
            return cat
        }
        return when {
            cat.contains("كتاكيت") || cat.contains("Chicks", true) || cat.contains("Poussins", true) -> tr("tx_chicks", lang)
            cat.contains("غاز") || cat.contains("Gas", true) || cat.contains("Gaz", true) -> tr("tx_gas", lang)
            cat.equals("MEDICATION", true) || cat.contains("أدوية") || cat.contains("Medicine", true) || cat.contains("Médicaments", true) -> tr("tx_meds", lang)
            cat.contains("سماد") || cat.contains("Manure", true) || cat.contains("Fumier", true) -> tr("tx_manure", lang)
            cat.contains("دجاج") || cat.contains("Chicken", true) || cat.contains("Poulets", true) -> tr("tx_chicken_sale", lang)
            cat.contains("عمالة") || cat.contains("Labor", true) || cat.contains("Main-d'œuvre", true) -> tr("tx_labor", lang)
            cat.contains("إيرادات أخرى") || cat.contains("Other Income", true) -> tr("tx_other_inc", lang)
            cat.contains("مصاريف أخرى") || cat.contains("Other Expenses", true) || cat.contains("أخرى") || cat.contains("Other", true) -> tr("tx_other_exp", lang)
            else -> cat
        }
    }

    fun getLocalizedMedicine(id: String, fallback: String, lang: AppLanguage): String {
        return when {
            id == "vet_1" || fallback.contains("AD3E") || fallback.contains("Electrolyte", true) -> tr("med_vitamin", lang)
            id == "vet_2" || fallback.contains("مضاد") || fallback.contains("Antibiotic", true) -> tr("med_antibiotic", lang)
            else -> fallback
        }
    }

    fun getLocalizedDosage(id: String, fallback: String, lang: AppLanguage): String {
        return when (id) {
            "vet_1" -> tr("med_1_dosage", lang)
            "vet_2" -> tr("med_2_dosage", lang)
            else -> fallback
        }
    }

    fun getLocalizedVetNote(id: String, fallback: String, lang: AppLanguage): String {
        return when (id) {
            "vet_1" -> tr("med_1_note", lang)
            "vet_2" -> tr("med_2_note", lang)
            else -> fallback
        }
    }

    fun getLocalizedThemeTitle(theme: AppThemeOption, lang: AppLanguage): String {
        return when (theme) {
            AppThemeOption.ICE_BLUE -> tr("theme_ice_blue", lang)
            AppThemeOption.NATURE_GREEN -> tr("theme_farm_green", lang)
            AppThemeOption.CRISP_WHITE -> tr("theme_clean_white", lang)
            AppThemeOption.NIGHT_MODE -> tr("theme_night_mode", lang)
        }
    }
}
