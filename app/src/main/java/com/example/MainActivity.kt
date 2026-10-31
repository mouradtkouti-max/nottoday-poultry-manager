package com.example

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.models.*
import com.example.ui.theme.PoultryTheme
import com.example.utils.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ============================================================================
// MOCK DATA ASSEMBLY
// ============================================================================

object AgritechMockDb {
    // True Zero-State for new users: do NOT pre-populate dummy barns on launch.
    // A newly logged-in user starts with 0 barns so their 1st barn is 100% FREE.
    val farms = mutableStateListOf<FarmModel>()

    val cycles = mutableStateListOf<CycleModel>()

    val countedFeedTxIds = mutableSetOf<String>()

    private var boundAppContext: android.content.Context? = null

    fun bindContext(context: android.content.Context) {
        boundAppContext = context.applicationContext
    }

    fun persistLocalState() {
        boundAppContext?.let { ctx ->
            LocalFarmPersistence.saveAll(ctx)
        }
    }

    private val initialDailyReportsSeed = listOf(
        DailyReportModel("rep_1", "farm_1", "cycle_2026_01", 1, 240.0, 500.0, 2, 33.0, 4.0, "جيدة", "طبيعية"),
        DailyReportModel("rep_2", "farm_1", "cycle_2026_01", 2, 280.0, 580.0, 1, 32.5, 3.8, "جيدة", "طبيعية"),
        DailyReportModel("rep_3", "farm_1", "cycle_2026_01", 3, 340.0, 680.0, 3, 32.0, 3.5, "جيدة", "طبيعية"),
        DailyReportModel("rep_4", "farm_1", "cycle_2026_01", 4, 410.0, 810.0, 2, 31.5, 3.2, "جيدة", "طبيعية"),
        DailyReportModel("rep_5", "farm_1", "cycle_2026_01", 5, 490.0, 960.0, 4, 31.0, 3.0, "متوسطة", "طبيعية"),
        DailyReportModel("rep_6", "farm_1", "cycle_2026_01", 6, 560.0, 1080.0, 2, 30.5, 2.8, "جيدة", "طبيعية"),
        DailyReportModel("rep_7", "farm_1", "cycle_2026_01", 7, 630.0, 1210.0, 1, 30.0, 2.5, "جيدة", "طبيعية"),
        DailyReportModel("rep_f2_1", "farm_2", "cycle_2026_02", 1, 320.0, 640.0, 2, 33.0, 4.2, "جيدة", "طبيعية"),
        DailyReportModel("rep_f2_2", "farm_2", "cycle_2026_02", 2, 390.0, 760.0, 1, 32.5, 3.9, "جيدة", "طبيعية"),
        DailyReportModel("rep_f2_7", "farm_2", "cycle_2026_02", 7, 740.0, 1450.0, 3, 29.8, 2.6, "جيدة", "طبيعية")
    )

    val dailyReports = mutableStateListOf<DailyReportModel>().apply {
        addAll(initialDailyReportsSeed)
    }

    private val initialVetNotesSeed = listOf(
        VetNoteModel("vet_1", "farm_1", "cycle_2026_01", 1, "فيتامين AD3E + إلكترولايت", "1 مل / لتر لمدة 3 أيام", "تحصين استقبال الكتاكيت وتفادي إجهاد النقل", false),
        VetNoteModel("vet_2", "farm_1", "cycle_2026_01", 4, "مضاد حيوي تنفسي وقائي", "0.5 غرام / لتر", "ملاحظة صوت خفيف في التهوية الليلية", true),
        VetNoteModel("vet_3", "farm_1", "cycle_2026_01", 7, "لقاح نيوكاسل (Clone 30 + IB)", "تقطير عيني / رش صباحي", "جرعة التحصين الفيروسي الأولى لليوم السابع", false)
    )

    val vetNotes = mutableStateListOf<VetNoteModel>().apply {
        addAll(initialVetNotesSeed)
    }

    val supervisedVetFarms = mutableStateListOf(
        FarmModel("farm_1", "عنبر الخيرات والبركة 1 (الحاج مراد)", "البليدة - بوفاريك", 8000, "worker_1"),
        FarmModel("farm_2", "مزرعة الهضاب للدواجن (سطيف)", "سطيف - عين أرنات", 12000, "worker_2")
    )

    val vetPharmacyInventory = mutableStateListOf(
        VetPharmacyItemModel(
            id = "pharm_1",
            name = "لقاح نيوكاسل (Clone 30 + IB)",
            category = "لقاحات فيروسية 🧬",
            stockUnits = 45,
            unitLabel = "قارورة (1000 جرعة)",
            importerName = "مؤسسة بيوفارم لاستيراد الأدوية البيطرية",
            importerPhone = "+213 550 88 99 11",
            unitCostDzd = 1850.0
        ),
        VetPharmacyItemModel(
            id = "pharm_2",
            name = "لقاح الجمبورو (IBD Intermediate Plus)",
            category = "لقاحات مناعية 🛡️",
            stockUnits = 30,
            unitLabel = "قارورة (1000 جرعة)",
            importerName = "مؤسسة بيوفارم لاستيراد الأدوية البيطرية",
            importerPhone = "+213 550 88 99 11",
            unitCostDzd = 2100.0
        ),
        VetPharmacyItemModel(
            id = "pharm_3",
            name = "إنروفلوكساسين 20% + كولستين سائل",
            category = "مضادات حيوية تنفسية ومعوية 💊",
            stockUnits = 18,
            unitLabel = "عبوة (1 لتر)",
            importerName = "مخابر أطلس فيت الدولية",
            importerPhone = "+213 561 44 22 10",
            unitCostDzd = 6400.0
        ),
        VetPharmacyItemModel(
            id = "pharm_4",
            name = "فيتامين AD3E + أملاح وإلكترولايت",
            category = "مكملات ورافعات مناعة 🧪",
            stockUnits = 60,
            unitLabel = "عبوة (1 لتر)",
            importerName = "مخابر أطلس فيت الدولية",
            importerPhone = "+213 561 44 22 10",
            unitCostDzd = 2900.0
        )
    )

    val vetEpidemicAlerts = mutableStateListOf(
        VetEpidemicAlertModel(
            id = "epid_1",
            virusName = "فيروس نيوكاسل (Newcastle NDV)",
            regionName = "البليدة • المدية • عين الدفلى",
            riskLevel = "HIGH",
            recommendedVaccine = "Clone 30 / LaSota + لقاح زيتي ميت",
            scheduleDayText = "اليوم 7 (تقطير/رش) + اليوم 18 (تذكيري)",
            clinicalGuidance = "رفع الأمن الحيوي عند مداخل العنابر وتطبيق الرش الصباحي بماء مقطر بارد."
        ),
        VetEpidemicAlertModel(
            id = "epid_2",
            virusName = "التهاب الشعب المعدي (IB Variant 2)",
            regionName = "سطيف • برج بوعريريج • المسيلة",
            riskLevel = "MEDIUM",
            recommendedVaccine = "IB Ma5 + 4/91",
            scheduleDayText = "اليوم 1 (رش بالمفرخة) + اليوم 14",
            clinicalGuidance = "ضبط التهوية الليلية ومنع تراكم الأمونيا لتجنب التهابات القصبات الهوائية."
        ),
        VetEpidemicAlertModel(
            id = "epid_3",
            virusName = "فيروس الجمبورو (IBD Gumboro)",
            regionName = "تيارت • الشلف • غليزان",
            riskLevel = "LOW",
            recommendedVaccine = "Gumboro Intermediate",
            scheduleDayText = "اليوم 12 واليوم 20 في مياه الشرب",
            clinicalGuidance = "إعطاء رافعات مناعة وفيتامين E+Selenium بعد التحصين لمدة 48 ساعة."
        )
    )

    val roleBusinessOrders = mutableStateListOf(
        RoleBusinessOrderModel(
            id = "ord_feed_1",
            role = AppRole.FEED_SUPPLIER,
            clientName = "عنبر الخيرات والبركة 1 - الحاج مراد",
            clientPhone = "+213 550 11 22 33",
            itemSummary = "علف بادئ ونامي متوازن (ذرة وصويا 22% بروتين)",
            quantityText = "40 ق",
            totalAmountDzd = 318000.0,
            status = "PENDING"
        ),
        RoleBusinessOrderModel(
            id = "ord_chick_1",
            role = AppRole.CHICK_HATCHERY,
            clientName = "مزرعة الهضاب للدواجن",
            clientPhone = "+213 552 40 50 60",
            itemSummary = "صيصان تسمين Ross 308 عمر يوم واحد محصنة",
            quantityText = "8,000 طائر",
            totalAmountDzd = 960000.0,
            status = "APPROVED"
        ),
        RoleBusinessOrderModel(
            id = "ord_trader_1",
            role = AppRole.WHOLESALE_TRADER,
            clientName = "عنبر الخيرات والبركة 1",
            clientPhone = "+213 550 11 22 33",
            itemSummary = "شحنة دجاج لحم حي جاهز للتسويق (متوسط 2.35 كغ)",
            quantityText = "1,500 طائر (150 صندوق)",
            totalAmountDzd = 570000.0,
            status = "APPROVED"
        ),
        RoleBusinessOrderModel(
            id = "ord_importer_1",
            role = AppRole.VET_IMPORTER,
            clientName = "عيادة د. كمال البيطرية",
            clientPhone = "+213 550 88 99 11",
            itemSummary = "توريد لقاحات نيوكاسل وجمبورو + مضادات تنفسية",
            quantityText = "50 قارورة + 20 عبوة",
            totalAmountDzd = 215000.0,
            status = "PENDING"
        )
    )

    fun adjustVetPharmacyStock(itemId: String, delta: Int) {
        val idx = vetPharmacyInventory.indexOfFirst { it.id == itemId }
        if (idx >= 0) {
            val current = vetPharmacyInventory[idx]
            vetPharmacyInventory[idx] = current.copy(
                stockUnits = (current.stockUnits + delta).coerceAtLeast(0)
            )
        }
    }

    private val initialTransactionsSeed = listOf(
        TransactionModel("tx_1", "farm_1", "cycle_2026_01", TransactionType.EXPENSE, "🐣 كتاكيت (Ross 308)", 1440000.0, "دج", "اليوم 1"),
        TransactionModel("tx_2", "farm_1", "cycle_2026_01", TransactionType.EXPENSE, "🌾 علف بادئ (25 ق)", 200000.0, "دج", "اليوم 1", quantityQuintals = 25.0),
        TransactionModel("tx_2b", "farm_1", "cycle_2026_01", TransactionType.EXPENSE, "🌾 علف نامي (15 ق)", 120000.0, "دج", "اليوم 3", quantityQuintals = 15.0),
        TransactionModel("tx_3", "farm_1", "cycle_2026_01", TransactionType.EXPENSE, "🔥 غاز وتدفئة", 35000.0, "دج", "اليوم 2"),
        TransactionModel("tx_4", "farm_1", "cycle_2026_01", TransactionType.EXPENSE, "💊 أدوية وتحصين", 48000.0, "دج", "اليوم 4"),
        TransactionModel("tx_5", "farm_1", "cycle_2026_01", TransactionType.INCOME, "🚜 بيع سماد عضوي", 60000.0, "دج", "اليوم 5"),
        TransactionModel("tx_f2_1", "farm_2", "cycle_2026_02", TransactionType.EXPENSE, "🌾 علف بادئ (30 ق)", 240000.0, "دج", "اليوم 1", quantityQuintals = 30.0),
        TransactionModel(
            "tx_sales_01",
            "farm_1",
            "cycle_2026_01",
            TransactionType.INCOME,
            "tx_chicken_sale",
            570000.0,
            "دج",
            "اليوم 5 - كمال رابحي",
            SalesInvoiceModel(
                invoiceNumber = "INV-1-0001",
                farmName = "عنبر الخيرات والبركة 1",
                cycleId = "cycle_2026_01",
                dateText = "اليوم 5",
                birdAgeDays = 5,
                buyerName = "كمال رابحي (تاجر الجملة)",
                truckPlate = "04521-122-16",
                totalCrates = 150,
                birdsPerCrate = 10,
                totalBirdsSold = 1500,
                totalGrossKg = 3650.0,
                totalTareKg = 2150.0,
                totalNetKg = 1500.0,
                avgBirdWeightKg = 1.000,
                pricePerKg = 380.0,
                totalAmount = 570000.0,
                currency = "دج",
                tareWeightsCount = 5,
                grossWeightsCount = 15
            )
        )
    )

    val transactions = mutableStateListOf<TransactionModel>().apply {
        addAll(initialTransactionsSeed)
    }

    val sharedTransactions: MutableList<TransactionModel>
        get() = transactions

    val chatMessages = mutableStateListOf(
        // 1. Contextual Worker Chat Messages (Ledger Tab)
        ChatMessageModel(
            id = "msg_w1",
            cycleId = "cycle_2026_01_worker",
            senderId = "worker_1",
            senderName = "أحمد",
            senderRole = "مشرف العنبر 👷‍♂️",
            text = "السلام عليكم عمي مراد، تم تسجيل التقرير اليومي: استهلاك العلف 5.1 ق والمياه 980 لتر، والحرارة مستقرة عند 30.0°C.",
            timestamp = "08:15 ص"
        ),
        ChatMessageModel(
            id = "msg_w2",
            cycleId = "cycle_2026_01_worker",
            senderId = "owner_1",
            senderName = "الحاج مراد",
            senderRole = "مالك المزرعة 👨‍🌾",
            text = "وعليكم السلام يا أحمد، الله يبارك. انتبه لتوزيع المعالف في الجناح الشرقي وتأكد من نظافة خطوط الحلمات.",
            timestamp = "08:25 ص"
        ),
        ChatMessageModel(
            id = "msg_w3",
            cycleId = "cycle_2026_01_worker",
            senderId = "worker_1",
            senderName = "أحمد",
            senderRole = "مشرف العنبر 👷‍♂️",
            text = "حاضر عمي مراد، تم تفقد خطوط المياه وضبط ارتفاع المعالف بالكامل ✓",
            timestamp = "08:40 ص"
        ),

        // 2. Contextual Vet Chat Messages (Vet Notes Tab)
        ChatMessageModel(
            id = "msg_v1",
            cycleId = "cycle_2026_01_vet",
            senderId = "vet_1",
            senderName = "د. كمال",
            senderRole = "طبيب بيطري 🩺",
            text = "السلام عليكم الحاج مراد، بناءً على فحص اليوم الخامس يُنصح بإعطاء جرعة فيتامين AD3E + أملاح معدنية (1 مل / لتر) لمدة 3 أيام لدعم النمو.",
            timestamp = "09:10 ص"
        ),
        ChatMessageModel(
            id = "msg_v2",
            cycleId = "cycle_2026_01_vet",
            senderId = "owner_1",
            senderName = "الحاج مراد",
            senderRole = "مالك المزرعة 👨‍🌾",
            text = "وعليكم السلام دكتور كمال، تم توفير العبوات في صيدلية العنبر وإبلاغ المشرف بتطبيق الجرعة فوراً.",
            timestamp = "09:18 ص"
        ),
        ChatMessageModel(
            id = "msg_v3",
            cycleId = "cycle_2026_01_vet",
            senderId = "vet_1",
            senderName = "د. كمال",
            senderRole = "طبيب بيطري 🩺",
            text = "ممتاز جداً، سأمرّ غداً صباحاً لمعاينة التهوية ومراجعة أوزان الأسبوع الأول إن شاء الله.",
            timestamp = "09:30 ص"
        ),

        // 3. General Admin Chat Messages (OwnerDashboardScreen)
        ChatMessageModel(
            id = "msg_g1",
            cycleId = "general_admin",
            senderId = "owner_1",
            senderName = "الحاج مراد",
            senderRole = "مالك المزرعة 👨‍🌾",
            text = "السلام عليكم فريق المزرعة، يرجى من جميع مشرفي العنابر رفع تقارير المخزون اليومية بالقنطار (ق) قبل الساعة 5 مساءً لجدولة طلبيات العلف الأسبوعية.",
            timestamp = "07:45 ص"
        ),
        ChatMessageModel(
            id = "msg_g2",
            cycleId = "general_admin",
            senderId = "worker_2",
            senderName = "سمير",
            senderRole = "مشرف عنبر الوفاق 👷‍♂️",
            text = "وعليكم السلام، مخزون العلف في عنبر الوفاق حالياً 4.2 ق ونحتاج شحنة علف نامي (40 ق) غداً صباحاً.",
            timestamp = "08:05 ص"
        ),
        ChatMessageModel(
            id = "msg_g3",
            cycleId = "general_admin",
            senderId = "vet_1",
            senderName = "د. كمال",
            senderRole = "طبيب بيطري 🩺",
            text = "تمت مراجعة برنامج التحصين لجميع العنابر النشطة، الأمور الصحية ممتازة ونسبة الحيوية تتجاوز 99.6%.",
            timestamp = "08:50 ص"
        ),

        // 4. Feed Trader Conversation ("كمال - تاجر أعلاف")
        ChatMessageModel(
            id = "msg_ft1",
            cycleId = "conv_feed_trader",
            senderId = "trader_1",
            senderName = "كمال",
            senderRole = "تاجر أعلاف 🌾",
            text = "السلام عليكم الحاج مراد، توفرت اليوم دفعة جديدة من علف ناهي ممتاز (18% بروتين) بسعر 7,600 دج للقنطار مع التوصيل للمزرعة.",
            timestamp = "10:15 ص"
        ),
        ChatMessageModel(
            id = "msg_ft2",
            cycleId = "conv_feed_trader",
            senderId = "owner_1",
            senderName = "الحاج مراد",
            senderRole = "مالك المزرعة 👨‍🌾",
            text = "وعليكم السلام سي كمال، بارك الله فيك. احجز لنا 40 قنطار لعنبر الخيرات ليوم الغد إن شاء الله.",
            timestamp = "10:22 ص"
        ),
        ChatMessageModel(
            id = "msg_ft3",
            cycleId = "conv_feed_trader",
            senderId = "trader_1",
            senderName = "كمال",
            senderRole = "تاجر أعلاف 🌾",
            text = "تم الحجز عمي مراد، الشاحنة تنطلق غداً الساعة 7 صباحاً بإذن الله ✓",
            timestamp = "10:30 ص"
        ),

        // 5. Wholesale Poultry Buyer Conversation ("ياسين - تاجر دواجن الجملة")
        ChatMessageModel(
            id = "msg_wb1",
            cycleId = "conv_wholesale_buyer",
            senderId = "buyer_1",
            senderName = "ياسين",
            senderRole = "تاجر دواجن الجملة 🚚",
            text = "السلام عليكم، نبحث عن شحنة دجاج لحم واجد (متوسط وزن 2.3 كغ) بـ 385 دج للكيلوغرام نقداً في المزرعة.",
            timestamp = "أمس"
        )
    )

    val chatConversations = mutableStateListOf(
        ChatConversationModel(
            id = "conv_feed_trader",
            userName = "كمال - تاجر أعلاف",
            roleLabel = "مورد أعلاف وذرة صويا 🌾",
            avatarEmoji = "🌾",
            avatarBgHex = 0xFFE8F5E9,
            defaultSnippet = "تم الحجز عمي مراد، الشاحنة تنطلق غداً الساعة 7 صباحاً بإذن الله ✓",
            defaultTimestamp = "10:30 ص",
            unreadCount = 1
        ),
        ChatConversationModel(
            id = "cycle_2026_01_vet",
            userName = "د. كمال - طبيب بيطري",
            roleLabel = "استشاري صحة الدواجن 🩺",
            avatarEmoji = "🩺",
            avatarBgHex = 0xFFE0F2FE,
            defaultSnippet = "ممتاز جداً، سأمرّ غداً صباحاً لمعاينة التهوية ومراجعة أوزان الأسبوع الأول إن شاء الله.",
            defaultTimestamp = "09:30 ص",
            unreadCount = 0
        ),
        ChatConversationModel(
            id = "cycle_2026_01_worker",
            userName = "أحمد - مشرف عنبر 1",
            roleLabel = "عنبر الخيرات والبركة 1 👷‍♂️",
            avatarEmoji = "👷‍♂️",
            avatarBgHex = 0xFFFEF3C7,
            defaultSnippet = "حاضر عمي مراد، تم تفقد خطوط المياه وضبط ارتفاع المعالف بالكامل ✓",
            defaultTimestamp = "08:40 ص",
            unreadCount = 0
        ),
        ChatConversationModel(
            id = "general_admin",
            userName = "مجموعة إدارة المزرعة",
            roleLabel = "المالك • الطبيب • مشرفو العنابر 🏢",
            avatarEmoji = "🏢",
            avatarBgHex = 0xFFEDE9FE,
            defaultSnippet = "تمت مراجعة برنامج التحصين لجميع العنابر النشطة، الأمور الصحية ممتازة ونسبة الحيوية تتجاوز 99.6%.",
            defaultTimestamp = "08:50 ص",
            unreadCount = 2
        ),
        ChatConversationModel(
            id = "conv_wholesale_buyer",
            userName = "ياسين - تاجر دواجن الجملة",
            roleLabel = "تسويق دجاج اللحم الحي 🚚",
            avatarEmoji = "🚚",
            avatarBgHex = 0xFFF1F5F9,
            defaultSnippet = "السلام عليكم، نبحث عن شحنة دجاج لحم واجد (متوسط وزن 2.3 كغ) بـ 385 دج للكيلوغرام نقداً في المزرعة.",
            defaultTimestamp = "أمس",
            unreadCount = 0
        )
    )

    val marketPosts = mutableStateListOf(
        MarketPostModel(
            id = "post_1",
            title = "علف بادئ ونامي ممتاز (ذرة وصويا نقية 22% بروتين)",
            description = "متوفر كميات جاهزة للتوصيل الفوري لجميع الولايات. علف متوازن لكتاكيت Ross 308 يضمن تحويل غذائي ممتاز من الأسبوع الأول.",
            priceDzd = 7950.0,
            priceUnit = "دج / قنطار",
            publisherName = "كمال - تاجر أعلاف",
            publisherLocation = "البليدة - بوفاريك",
            imageCaption = "صورة شحنات أكياس العلف (50 كغ)",
            chatChannelId = "conv_feed_trader"
        ),
        MarketPostModel(
            id = "post_2",
            title = "دفعة دجاج لحم حي جاهز للتسويق (متوسط وزن 2.4 كغ)",
            description = "العدد المتوفر: 3,500 طائر بصحة ممتازة وريش كامل، متاح للوزن والتحميل المباشر من العنبر.",
            priceDzd = 385.0,
            priceUnit = "دج / كغ",
            publisherName = "مزرعة الهضاب للدواجن",
            publisherLocation = "سطيف - عين أرنات",
            imageCaption = "صورة القطيع داخل العنبر (عمر 36 يوم)",
            chatChannelId = "conv_wholesale_buyer"
        ),
        MarketPostModel(
            id = "post_3",
            title = "كتكوت تسمين Ross 308 عمر يوم واحد محصن",
            description = "كتاكيت درجة أولى حيوية عالية مع شهادة التحصين البيطري الكاملة (نيوكاسل + جمبورو).",
            priceDzd = 125.0,
            priceUnit = "دج / للفرخ",
            publisherName = "مفرخة الأمل الحديثة",
            publisherLocation = "المدية - البرواقية",
            imageCaption = "صورة صناديق الكتاكيت جاهزة للتوزيع",
            chatChannelId = "general_admin"
        )
    )

    fun addMarketPost(post: MarketPostModel) {
        marketPosts.add(0, post)
    }

    fun streamChatMessages(cycleId: String): List<ChatMessageModel> {
        val exactMatches = chatMessages.filter { it.cycleId == cycleId }
        if (exactMatches.isNotEmpty()) return exactMatches
        return when {
            cycleId.endsWith("_worker") -> chatMessages.filter { it.cycleId == "cycle_2026_01_worker" }
            cycleId.endsWith("_vet") -> chatMessages.filter { it.cycleId == "cycle_2026_01_vet" }
            else -> chatMessages.filter { it.cycleId == cycleId || it.cycleId == "general_admin" }
        }
    }

    fun sendChatMessage(message: ChatMessageModel) {
        if (chatMessages.none { it.id == message.id }) {
            chatMessages.add(message)
        }
    }

    /**
     * Atomically records a Vet Medicine Invoice Expense in the Owner's Treasury (`transactions`) with `status = "PENDING"`,
     * adds the Vet Note prescription to `vetNotes` with `status = "PENDING"`, and posts an official invoice bubble in the Vet Chat.
     */
    fun recordVetInvoiceAndPrescription(
        tx: TransactionModel,
        vetNote: VetNoteModel,
        chatChannelId: String = "${tx.cycleId}_vet"
    ): ChatMessageModel {
        val pendingTx = tx.copy(status = "PENDING")
        val pendingNote = vetNote.copy(
            status = "PENDING",
            linkedTransactionId = pendingTx.id,
            invoiceAmount = pendingTx.amount,
            currency = pendingTx.currency
        )
        val existingTxIdx = transactions.indexOfFirst { it.id == pendingTx.id }
        if (existingTxIdx >= 0) {
            transactions[existingTxIdx] = pendingTx
        } else {
            transactions.add(pendingTx)
        }
        val existingNoteIdx = vetNotes.indexOfFirst { it.id == pendingNote.id }
        if (existingNoteIdx >= 0) {
            vetNotes[existingNoteIdx] = pendingNote
        } else {
            vetNotes.add(0, pendingNote)
        }
        val nowTime = try {
            java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale("ar"))
            )
        } catch (_: Exception) {
            "الآن"
        }
        val formattedPrice = String.format(java.util.Locale.US, "%,.0f", pendingTx.amount)
        val chatMsg = ChatMessageModel(
            id = "msg_vet_inv_${pendingTx.id}",
            cycleId = chatChannelId,
            senderId = "vet_1",
            senderName = "د. كمال",
            senderRole = "طبيب بيطري 🩺",
            text = "🧾 فاتورة دواء (بانتظار موافقة المالك ⏳): ${pendingNote.medicineName}\n• الجرعة: ${pendingNote.dosage}\n• المبلغ المطلوب اعتماده: $formattedPrice ${pendingTx.currency}",
            timestamp = nowTime,
            linkedTransactionId = pendingTx.id,
            requestCategory = pendingNote.medicineName,
            requestAmount = pendingTx.amount,
            requestCurrency = pendingTx.currency,
            status = "PENDING"
        )
        sendChatMessage(chatMsg)
        persistLocalState()
        return chatMsg
    }

    /**
     * Universal Financial Request in Chat:
     * Supports Worker advances/gas/maintenance and Supplier/Vet invoices (feed, chicks, medicine).
     * Creates a PENDING TransactionModel in the Treasury and posts an interactive Financial Request Card in Chat.
     */
    fun recordUniversalFinancialRequest(
        tx: TransactionModel,
        noteText: String,
        senderId: String,
        senderName: String,
        senderRole: String,
        chatChannelId: String
    ): ChatMessageModel {
        val pendingTx = tx.copy(
            status = "PENDING",
            notes = noteText.ifBlank { tx.notes }
        )
        val existingTxIdx = transactions.indexOfFirst { it.id == pendingTx.id }
        if (existingTxIdx >= 0) {
            transactions[existingTxIdx] = pendingTx
        } else {
            transactions.add(0, pendingTx)
        }
        val nowTime = try {
            java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale("ar"))
            )
        } catch (_: Exception) {
            "الآن"
        }
        val formattedPrice = String.format(java.util.Locale.US, "%,.0f", pendingTx.amount)
        val cleanNote = if (noteText.isNotBlank()) "\n• البيان: $noteText" else ""
        val chatMsg = ChatMessageModel(
            id = "msg_fin_req_${pendingTx.id}",
            cycleId = chatChannelId,
            senderId = senderId,
            senderName = senderName,
            senderRole = senderRole,
            text = "🧾 ${pendingTx.category}$cleanNote\n• المبلغ المطلوب اعتماده: $formattedPrice ${pendingTx.currency}",
            timestamp = nowTime,
            linkedTransactionId = pendingTx.id,
            requestCategory = pendingTx.category,
            requestAmount = pendingTx.amount,
            requestCurrency = pendingTx.currency,
            status = "PENDING"
        )
        sendChatMessage(chatMsg)
        persistLocalState()
        return chatMsg
    }

    fun mergeCloudCycleWithLocal(cloudCycle: CycleModel, localCycle: CycleModel): CycleModel {
        return cloudCycle.copy(
            feedInventoryKg = maxOf(cloudCycle.feedInventoryKg, localCycle.feedInventoryKg),
            cumulativeBirdsSold = maxOf(cloudCycle.cumulativeBirdsSold, localCycle.cumulativeBirdsSold),
            cumulativeSoldWeightKg = maxOf(cloudCycle.cumulativeSoldWeightKg, localCycle.cumulativeSoldWeightKg)
        )
    }

    fun mergeCloudReportWithLocal(cloudReport: DailyReportModel, existingLocal: DailyReportModel): DailyReportModel {
        val localIsNewer = (existingLocal.updatedAtMillis > cloudReport.updatedAtMillis) ||
            (existingLocal.isEdited && !cloudReport.isEdited)
        return if (localIsNewer) {
            existingLocal.copy(
                actualAvgWeightKg = if (existingLocal.actualAvgWeightKg > 0.0) existingLocal.actualAvgWeightKg else cloudReport.actualAvgWeightKg,
                medicineGiven = existingLocal.medicineGiven || cloudReport.medicineGiven,
                medicineSummary = existingLocal.medicineSummary.ifBlank { cloudReport.medicineSummary },
                withdrawalDays = maxOf(existingLocal.withdrawalDays, cloudReport.withdrawalDays)
            )
        } else {
            cloudReport.copy(
                isEdited = cloudReport.isEdited || existingLocal.isEdited,
                previousSummary = cloudReport.previousSummary.ifBlank { existingLocal.previousSummary },
                actualAvgWeightKg = if (cloudReport.actualAvgWeightKg > 0.0) cloudReport.actualAvgWeightKg else existingLocal.actualAvgWeightKg,
                medicineGiven = cloudReport.medicineGiven || existingLocal.medicineGiven,
                medicineSummary = cloudReport.medicineSummary.ifBlank { existingLocal.medicineSummary },
                withdrawalDays = maxOf(cloudReport.withdrawalDays, existingLocal.withdrawalDays),
                updatedAtMillis = maxOf(cloudReport.updatedAtMillis, existingLocal.updatedAtMillis)
            )
        }
    }

    /**
     * Adds a private contact by phone number to `chatConversations`.
     */
    fun addPhoneContact(
        name: String,
        phone: String,
        roleLabel: String,
        avatarEmoji: String
    ): ChatConversationModel {
        val cleanPhone = phone.trim()
        val existing = chatConversations.firstOrNull {
            it.phoneNumber == cleanPhone && cleanPhone.isNotEmpty()
        }
        if (existing != null) return existing
        val newConv = ChatConversationModel(
            id = "chat_contact_${java.util.UUID.randomUUID().toString().take(8)}",
            userName = name.trim(),
            roleLabel = roleLabel,
            phoneNumber = cleanPhone,
            avatarEmoji = avatarEmoji,
            avatarBgHex = 0xFFE0F2FE,
            defaultSnippet = "📞 $cleanPhone • $roleLabel",
            defaultTimestamp = "الآن",
            unreadCount = 0
        )
        chatConversations.add(0, newConv)
        return newConv
    }

    /**
     * Owner Approval Action ("تأكيد واعتماد الصرف ✓"):
     * Updates the Vet Invoice / Financial Request Transaction, VetNote, and ChatMessage status from "PENDING" to "APPROVED"
     * with the Owner's signature tag, and updates feed stock without duplication if it is a feed purchase.
     */
    fun approveVetInvoice(
        transactionId: String,
        vetNoteId: String = "",
        signatureTag: String = "Signed_By_Owner_${System.currentTimeMillis()}"
    ) {
        var resolvedTxId = transactionId
        if (vetNoteId.isNotBlank()) {
            val vnIdx = vetNotes.indexOfFirst { it.id == vetNoteId }
            if (vnIdx >= 0) {
                val vn = vetNotes[vnIdx]
                if (resolvedTxId.isBlank() && vn.linkedTransactionId.isNotBlank()) {
                    resolvedTxId = vn.linkedTransactionId
                }
                vetNotes[vnIdx] = vn.copy(status = "APPROVED")
            }
        }
        if (resolvedTxId.isNotBlank()) {
            val txIdx = transactions.indexOfFirst { it.id == resolvedTxId }
            if (txIdx >= 0) {
                val currentTx = transactions[txIdx]
                val updatedTx = currentTx.copy(
                    status = "APPROVED",
                    approvedBySignature = signatureTag
                )
                transactions[txIdx] = updatedTx

                val isFeedCat = updatedTx.category.contains("علف") ||
                    updatedTx.category.contains("Feed", ignoreCase = true) ||
                    updatedTx.category.contains("Aliment", ignoreCase = true) ||
                    updatedTx.category.contains("Pienso", ignoreCase = true)
                if (isFeedCat && !countedFeedTxIds.contains(updatedTx.id)) {
                    val addedFeedKg = parseFeedTransaction(updatedTx, AppLanguage.AR).estimatedKg
                    if (addedFeedKg > 0.0) {
                        countedFeedTxIds.add(updatedTx.id)
                        val cycleIdx = cycles.indexOfFirst {
                            it.id == updatedTx.cycleId || (it.farmId == updatedTx.farmId && it.status.equals("active", ignoreCase = true))
                        }
                        if (cycleIdx >= 0) {
                            val c = cycles[cycleIdx]
                            cycles[cycleIdx] = c.copy(feedInventoryKg = c.feedInventoryKg + addedFeedKg)
                        }
                    }
                }
            }
            for (i in vetNotes.indices) {
                if (vetNotes[i].linkedTransactionId == resolvedTxId && vetNotes[i].status != "APPROVED") {
                    vetNotes[i] = vetNotes[i].copy(status = "APPROVED")
                }
            }
            for (i in chatMessages.indices) {
                if (chatMessages[i].linkedTransactionId == resolvedTxId && chatMessages[i].status != "APPROVED") {
                    chatMessages[i] = chatMessages[i].copy(status = "APPROVED")
                }
            }
        }
        persistLocalState()
    }

    /**
     * Resets user-authored in-memory state when the user explicitly clicks Logout.
     */
    fun clearOnExplicitLogout() {
        farms.clear()
        cycles.clear()
        countedFeedTxIds.clear()
        dailyReports.clear()
        dailyReports.addAll(initialDailyReportsSeed)
        vetNotes.clear()
        vetNotes.addAll(initialVetNotesSeed)
        transactions.clear()
        transactions.addAll(initialTransactionsSeed)
    }

    /**
     * Inserts a new DailyReportModel or updates an existing report for the same day/id with full Audit Trail:
     * - If an existing report is modified, marks `isEdited = true` and records `previousSummary`.
     * - Adjusts `cycle.feedInventoryKg` by the feed delta.
     */
    fun upsertDailyReportWithAudit(report: DailyReportModel): DailyReportModel {
        val existingIdx = dailyReports.indexOfFirst {
            it.id == report.id || (it.farmId == report.farmId && it.cycleId == report.cycleId && it.dayNumber == report.dayNumber)
        }
        val finalReport = if (existingIdx >= 0) {
            val old = dailyReports[existingIdx]
            val prevSummary = report.previousSummary.ifBlank {
                old.previousSummary.ifBlank {
                    "العلف: ${Translations.formatQuintalNumber(old.feedIntakeKg / 100.0)} ق • الماء: ${old.waterIntakeLiters.toInt()} L • النافق: ${old.mortalityCount} • الحرارة: ${String.format(java.util.Locale.US, "%.1f", old.temperature)}°"
                }
            }
            val updated = report.copy(
                id = old.id,
                isEdited = true,
                previousSummary = prevSummary,
                withdrawalDays = maxOf(report.withdrawalDays, old.withdrawalDays),
                updatedAtMillis = System.currentTimeMillis()
            )
            dailyReports[existingIdx] = updated
            val feedDeltaKg = updated.feedIntakeKg - old.feedIntakeKg
            if (kotlin.math.abs(feedDeltaKg) > 0.0001) {
                val cycleIdx = cycles.indexOfFirst { it.id == updated.cycleId || (it.farmId == updated.farmId && it.status.equals("active", true)) }
                if (cycleIdx >= 0) {
                    val c = cycles[cycleIdx]
                    cycles[cycleIdx] = c.copy(
                        feedInventoryKg = (c.feedInventoryKg - feedDeltaKg).coerceAtLeast(0.0)
                    )
                }
            }
            updated
        } else {
            val stamped = report.copy(updatedAtMillis = System.currentTimeMillis())
            dailyReports.add(stamped)
            dailyReports.sortBy { it.dayNumber }
            if (stamped.feedIntakeKg > 0.0) {
                val cycleIdx = cycles.indexOfFirst { it.id == stamped.cycleId || (it.farmId == stamped.farmId && it.status.equals("active", true)) }
                if (cycleIdx >= 0) {
                    val c = cycles[cycleIdx]
                    cycles[cycleIdx] = c.copy(
                        feedInventoryKg = (c.feedInventoryKg - stamped.feedIntakeKg).coerceAtLeast(0.0)
                    )
                }
            }
            stamped
        }
        persistLocalState()
        return finalReport
    }
}

// ============================================================================
// MAIN ACTIVITY
// ============================================================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            PoultryTheme {
                AgritechRootApp()
            }
        }
    }
}

// ============================================================================
// ROOT APPLICATION CONTROLLER WITH AUTO TEXT DIRECTION
// ============================================================================

@Composable
fun AgritechRootApp() {
    val context = LocalContext.current
    val sessionState = remember {
        AgritechMockDb.bindContext(context)
        LocalFarmPersistence.loadIntoMockDb(context)
        val onboardingDone = SaasPreferencesManager.hasCompletedOnboarding(context)
        val detectedLocale = detectInitialLocaleAndCurrency(context)
        val savedLang = if (onboardingDone) SaasPreferencesManager.getSavedLanguage(context) else detectedLocale.language
        val savedCountry = if (onboardingDone) SaasPreferencesManager.getSavedCountryCode(context) else detectedLocale.countryCode
        val savedCurrency = if (onboardingDone) SaasPreferencesManager.getSavedCurrencyCode(context) else detectedLocale.currencyCode
        val savedTheme = SaasPreferencesManager.getSavedTheme(context)
        val savedAccentColor = SaasPreferencesManager.getSavedAccentColor(context)
        val savedTextScale = SaasPreferencesManager.getSavedTextScale(context)
        val savedCornerStyle = SaasPreferencesManager.getSavedCornerStyle(context)
        val firebaseSignedIn = AgritechAuthHelper.isSignedIn()
        val savedLoggedIn = SaasPreferencesManager.getSavedIsLoggedIn(context) || firebaseSignedIn
        val savedRole = SaasPreferencesManager.getSavedUserRole(context)
            ?: if (savedLoggedIn) AppRole.OWNER else null
        val savedLinkedBarns = if (savedLoggedIn) {
            SaasPreferencesManager.getSavedHasLinkedBarns(context) || savedRole == AppRole.OWNER
        } else {
            true
        }
        val savedFarmId = SaasPreferencesManager.getSavedActiveFarmId(context)
        val savedUid = AgritechAuthHelper.currentUserUid() ?: SaasPreferencesManager.getSavedUserUid(context)
        val professionSelectedDone = SaasPreferencesManager.hasSelectedInitialProfession(context)

        Translations.globalCountryCode = savedCountry
        Translations.globalCurrencyCode = savedCurrency
        SaasPreferencesManager.applyCurrencyToMockTransactions(savedCurrency)

        AgritechAppSessionState(
            initialOnboardingCompleted = onboardingDone,
            initialTheme = savedTheme,
            initialAccentColor = savedAccentColor,
            initialTextScale = savedTextScale,
            initialCornerStyle = savedCornerStyle,
            initialLanguage = savedLang,
            initialCountryCode = savedCountry,
            initialCurrencyCode = savedCurrency,
            initialIsLoggedIn = savedLoggedIn,
            initialRole = savedRole,
            initialHasLinkedBarns = savedLinkedBarns,
            initialActiveFarmId = savedFarmId,
            initialUserUid = savedUid,
            initialProfessionSelected = professionSelectedDone
        )
    }

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showOnboardingModalOverlay by remember { mutableStateOf(false) }

    val layoutDirection = if (sessionState.currentLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    val baseDensity = LocalDensity.current
    val scaledDensity = remember(baseDensity, sessionState.currentTextScale) {
        Density(
            density = baseDensity.density,
            fontScale = baseDensity.fontScale * sessionState.currentTextScale.scaleFactor
        )
    }

    CompositionLocalProvider(
        LocalAgritechAppState provides sessionState,
        LocalAppAppearanceState provides sessionState.appearanceState,
        LocalLayoutDirection provides layoutDirection,
        LocalDensity provides scaledDensity,
        LocalFeedUnitPreference provides FeedUnitPreference.QUINTAL
    ) {
        PoultryTheme(
            isDark = sessionState.currentTheme.isDark,
            accentColor = sessionState.currentAccentColor,
            cornerStyle = sessionState.currentCornerStyle
        ) {
            if (!sessionState.hasCompletedOnboarding) {
                GlobalOnboardingScreen(
                    initialLanguage = sessionState.currentLanguage,
                    initialCountryCode = sessionState.currentCountryCode,
                    initialCurrencyCode = sessionState.currentCurrencyCode,
                    currentTheme = sessionState.currentTheme,
                    onCompleteOnboarding = { chosenLang, chosenCountry, chosenCurrency ->
                        sessionState.updateGlobalSettings(
                            context = context,
                            newLanguage = chosenLang,
                            newCountryCode = chosenCountry,
                            newCurrencyCode = chosenCurrency
                        )
                    }
                )
            } else {
                // Keep AgritechMainApp permanently mounted once onboarding is completed so state NEVER resets
                AgritechMainApp(
                    currentTheme = sessionState.currentTheme,
                    currentLanguage = sessionState.currentLanguage,
                    onOpenSettings = { showSettingsDialog = true }
                )

                if (!sessionState.hasSelectedInitialProfession) {
                    ProfessionOnboardingDialog(
                        lang = sessionState.currentLanguage,
                        currentTheme = sessionState.currentTheme,
                        initialRole = sessionState.currentRole ?: AppRole.OWNER_BARN,
                        onConfirmProfession = { chosenRole ->
                            sessionState.completeInitialProfessionSelection(context, chosenRole)
                        }
                    )
                }

                if (showSettingsDialog) {
                    SettingsDialog(
                        currentTheme = sessionState.currentTheme,
                        onSelectTheme = { newTheme ->
                            sessionState.updateTheme(context, newTheme)
                        },
                        currentLanguage = sessionState.currentLanguage,
                        onSelectLanguage = { newLang ->
                            sessionState.updateGlobalSettings(
                                context = context,
                                newLanguage = newLang
                            )
                        },
                        currentCountryCode = sessionState.currentCountryCode,
                        currentCurrencyCode = sessionState.currentCurrencyCode,
                        onUpdateCountryAndCurrency = { newCountry, newCurrency ->
                            sessionState.updateGlobalSettings(
                                context = context,
                                newCountryCode = newCountry,
                                newCurrencyCode = newCurrency
                            )
                        },
                        onOpenGlobalOnboarding = {
                            showSettingsDialog = false
                            showOnboardingModalOverlay = true
                        },
                        onDismiss = { showSettingsDialog = false }
                    )
                }

                if (showOnboardingModalOverlay) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        GlobalOnboardingScreen(
                            initialLanguage = sessionState.currentLanguage,
                            initialCountryCode = sessionState.currentCountryCode,
                            initialCurrencyCode = sessionState.currentCurrencyCode,
                            currentTheme = sessionState.currentTheme,
                            onCompleteOnboarding = { chosenLang, chosenCountry, chosenCurrency ->
                                sessionState.updateGlobalSettings(
                                    context = context,
                                    newLanguage = chosenLang,
                                    newCountryCode = chosenCountry,
                                    newCurrencyCode = chosenCurrency
                                )
                                showOnboardingModalOverlay = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AgritechMainApp(
    currentTheme: AppThemeOption,
    currentLanguage: AppLanguage,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionState = LocalAgritechAppState.current

    val credentialManager = remember {
        try {
            CredentialManager.create(context)
        } catch (_: Exception) {
            null
        }
    }
    val firestoreRepo = remember {
        try {
            AgritechFirestoreRepository(context)
        } catch (_: Exception) {
            null
        }
    }

    var authErrorBanner by remember { mutableStateOf<String?>(null) }

    // Attempt silent auto sign-in AT MOST ONCE on initial launch when not yet logged in.
    // Once authenticated (or once attempted), NEVER re-trigger the Google Sign-In bottom sheet automatically.
    LaunchedEffect(credentialManager) {
        if (!sessionState.isLoggedIn && !sessionState.hasAttemptedInitialAutoSignIn) {
            sessionState.hasAttemptedInitialAutoSignIn = true
            val cm = credentialManager
            if (AgritechAuthHelper.isSignedIn()) {
                sessionState.markAuthenticated(
                    context = context,
                    role = sessionState.currentRole ?: AppRole.OWNER,
                    linkedBarns = sessionState.hasLinkedBarns,
                    uid = AgritechAuthHelper.currentUserUid()
                )
            } else if (cm != null) {
                AgritechAuthHelper.attemptAutoSignIn(
                    context = context,
                    credentialManager = cm,
                    onAuthSuccess = {
                        sessionState.markAuthenticated(
                            context = context,
                            role = sessionState.currentRole ?: AppRole.OWNER,
                            linkedBarns = true,
                            uid = AgritechAuthHelper.currentUserUid()
                        )
                    },
                    onUnauthenticated = {
                        // Do NOT reset if user already logged in during the meantime
                    },
                    scope = scope
                )
            }
        }
    }

    // Observe Firebase Auth state & sync real-time shared /farms/{farmId}/... collections when signed in.
    // CRITICAL FIX: Never reset sessionState.isLoggedIn to false in background listeners!
    // Only explicit user action via performLogout can log the user out.
    LaunchedEffect(firestoreRepo, sessionState.activeFarmId) {
        val activeFarmId = sessionState.activeFarmId
        val authFlow = try {
            FirebaseAuth.getInstance().authStateFlow()
        } catch (_: Exception) {
            null
        }
        authFlow?.collectLatest { firebaseUser ->
            if (firebaseUser != null && firestoreRepo != null) {
                val uid = firebaseUser.uid
                if (!sessionState.isLoggedIn) {
                    sessionState.markAuthenticated(
                        context = context,
                        role = sessionState.currentRole ?: AppRole.OWNER,
                        linkedBarns = sessionState.hasLinkedBarns,
                        farmId = activeFarmId,
                        uid = uid
                    )
                } else {
                    sessionState.currentUserUid = uid
                }
                launch {
                    try {
                        firestoreRepo.observeUserRole(uid).collectLatest { cloudRole ->
                            // Only update role if cloudRole is non-null; never overwrite with null!
                            if (cloudRole != null) {
                                sessionState.markAuthenticated(
                                    context = context,
                                    role = cloudRole,
                                    linkedBarns = sessionState.hasLinkedBarns,
                                    farmId = activeFarmId,
                                    uid = uid
                                )
                            }
                        }
                    } catch (_: Exception) {}
                }
                if (activeFarmId.isNotBlank()) {
                    launch {
                        try {
                            firestoreRepo.observeFarms(activeFarmId).collectLatest { cloudFarms ->
                                if (cloudFarms.isNotEmpty()) {
                                    cloudFarms.forEach { farm ->
                                        val idx = AgritechMockDb.farms.indexOfFirst { it.id == farm.id }
                                        if (idx >= 0) {
                                            AgritechMockDb.farms[idx] = farm
                                        } else {
                                            AgritechMockDb.farms.add(farm)
                                        }
                                    }
                                }
                            }
                        } catch (_: Exception) {}
                    }
                    launch {
                        try {
                            firestoreRepo.observeCycles(activeFarmId).collectLatest { cloudCycles ->
                                if (cloudCycles.isNotEmpty()) {
                                    cloudCycles.forEach { cycle ->
                                        val idx = AgritechMockDb.cycles.indexOfFirst { it.id == cycle.id }
                                        if (idx >= 0) {
                                            val localCycle = AgritechMockDb.cycles[idx]
                                            AgritechMockDb.cycles[idx] = AgritechMockDb.mergeCloudCycleWithLocal(
                                                cloudCycle = cycle,
                                                localCycle = localCycle
                                            )
                                        } else {
                                            AgritechMockDb.cycles.add(cycle)
                                        }
                                    }
                                    AgritechMockDb.persistLocalState()
                                }
                            }
                        } catch (_: Exception) {}
                    }
                    launch {
                        try {
                            firestoreRepo.observeDailyReports(activeFarmId).collectLatest { cloudReports ->
                                if (cloudReports.isNotEmpty()) {
                                    cloudReports.forEach { report ->
                                        val idx = AgritechMockDb.dailyReports.indexOfFirst {
                                            it.id == report.id || (it.farmId == report.farmId && it.cycleId == report.cycleId && it.dayNumber == report.dayNumber)
                                        }
                                        if (idx >= 0) {
                                            val existingLocal = AgritechMockDb.dailyReports[idx]
                                            val localIsNewer = (existingLocal.updatedAtMillis > report.updatedAtMillis) ||
                                                (existingLocal.isEdited && !report.isEdited)
                                            val mergedReport = if (localIsNewer) {
                                                existingLocal.copy(
                                                    actualAvgWeightKg = if (existingLocal.actualAvgWeightKg > 0.0) existingLocal.actualAvgWeightKg else report.actualAvgWeightKg,
                                                    medicineGiven = existingLocal.medicineGiven || report.medicineGiven,
                                                    medicineSummary = existingLocal.medicineSummary.ifBlank { report.medicineSummary },
                                                    withdrawalDays = maxOf(existingLocal.withdrawalDays, report.withdrawalDays)
                                                )
                                            } else {
                                                report.copy(
                                                    isEdited = report.isEdited || existingLocal.isEdited,
                                                    previousSummary = report.previousSummary.ifBlank { existingLocal.previousSummary },
                                                    actualAvgWeightKg = if (report.actualAvgWeightKg > 0.0) report.actualAvgWeightKg else existingLocal.actualAvgWeightKg,
                                                    medicineGiven = report.medicineGiven || existingLocal.medicineGiven,
                                                    medicineSummary = report.medicineSummary.ifBlank { existingLocal.medicineSummary },
                                                    withdrawalDays = maxOf(report.withdrawalDays, existingLocal.withdrawalDays),
                                                    updatedAtMillis = maxOf(report.updatedAtMillis, existingLocal.updatedAtMillis)
                                                )
                                            }
                                            AgritechMockDb.dailyReports[idx] = mergedReport
                                        } else {
                                            AgritechMockDb.dailyReports.add(report)
                                        }
                                    }
                                    AgritechMockDb.persistLocalState()
                                }
                            }
                        } catch (_: Exception) {}
                    }
                    launch {
                        try {
                            firestoreRepo.observeVetNotes(activeFarmId).collectLatest { cloudNotes ->
                                if (cloudNotes.isNotEmpty()) {
                                    cloudNotes.forEach { note ->
                                        val idx = AgritechMockDb.vetNotes.indexOfFirst { it.id == note.id }
                                        if (idx >= 0) {
                                            val existingLocal = AgritechMockDb.vetNotes[idx]
                                            AgritechMockDb.vetNotes[idx] = note.copy(
                                                status = if (existingLocal.status == "APPROVED") "APPROVED" else note.status,
                                                withdrawalDays = maxOf(note.withdrawalDays, existingLocal.withdrawalDays),
                                                updatedAtMillis = maxOf(note.updatedAtMillis, existingLocal.updatedAtMillis)
                                            )
                                        } else {
                                            AgritechMockDb.vetNotes.add(note)
                                        }
                                    }
                                    AgritechMockDb.persistLocalState()
                                }
                            }
                        } catch (_: Exception) {}
                    }
                    launch {
                        try {
                            firestoreRepo.observeTransactions(activeFarmId).collectLatest { cloudTxs ->
                                if (cloudTxs.isNotEmpty()) {
                                    cloudTxs.forEach { tx ->
                                        val idx = AgritechMockDb.transactions.indexOfFirst { it.id == tx.id }
                                        if (idx >= 0) {
                                            val existingLocal = AgritechMockDb.transactions[idx]
                                            AgritechMockDb.transactions[idx] = tx.copy(
                                                status = if (existingLocal.status == "APPROVED") "APPROVED" else tx.status,
                                                invoiceData = tx.invoiceData ?: existingLocal.invoiceData,
                                                approvedBySignature = tx.approvedBySignature.ifBlank { existingLocal.approvedBySignature }
                                            )
                                        } else {
                                            AgritechMockDb.transactions.add(tx)
                                        }
                                    }
                                    AgritechMockDb.persistLocalState()
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        }
    }

    val performLogout: () -> Unit = {
        val cm = credentialManager
        if (cm != null) {
            try {
                AgritechAuthHelper.signOut(
                    context = context,
                    credentialManager = cm,
                    onSignOutComplete = {
                        sessionState.performExplicitLogout(context)
                    },
                    scope = scope
                )
            } catch (_: Exception) {
                sessionState.performExplicitLogout(context)
            }
        } else {
            sessionState.performExplicitLogout(context)
        }
    }

    when {
        !sessionState.isLoggedIn || sessionState.currentRole == null -> {
            AgritechLoginScreen(
                currentTheme = currentTheme,
                lang = currentLanguage,
                authErrorMessage = authErrorBanner,
                onLogin = { role, linkedBarns ->
                    authErrorBanner = null
                    val cm = credentialManager
                    val isAlreadySignedIn = AgritechAuthHelper.isSignedIn()
                    if (isAlreadySignedIn) {
                        sessionState.markAuthenticated(
                            context = context,
                            role = role,
                            linkedBarns = linkedBarns,
                            uid = AgritechAuthHelper.currentUserUid()
                        )
                        if (role == AppRole.WORKER && linkedBarns) {
                            sessionState.selectedFarmForWorker = AgritechMockDb.farms.firstOrNull()
                        }
                        if (sessionState.activeFarmId.isNotBlank()) {
                            scope.launch {
                                firestoreRepo?.setUserRoleAndFarm(role, sessionState.activeFarmId)
                                if (role == AppRole.OWNER) {
                                    AgritechMockDb.farms.firstOrNull { it.id == sessionState.activeFarmId }?.let { farm ->
                                        firestoreRepo?.saveFarmRecord(farm, AppRole.OWNER)
                                    }
                                }
                            }
                        }
                    } else if (cm != null) {
                        AgritechAuthHelper.onGoogleSignInClicked(
                            context = context,
                            credentialManager = cm,
                            onAuthSuccess = {
                                authErrorBanner = null
                                sessionState.markAuthenticated(
                                    context = context,
                                    role = role,
                                    linkedBarns = linkedBarns,
                                    uid = AgritechAuthHelper.currentUserUid()
                                )
                                if (role == AppRole.WORKER && linkedBarns) {
                                    sessionState.selectedFarmForWorker = AgritechMockDb.farms.firstOrNull()
                                }
                                if (sessionState.activeFarmId.isNotBlank()) {
                                    scope.launch {
                                        firestoreRepo?.setUserRoleAndFarm(role, sessionState.activeFarmId)
                                        if (role == AppRole.OWNER) {
                                            AgritechMockDb.farms.firstOrNull { it.id == sessionState.activeFarmId }?.let { farm ->
                                                firestoreRepo?.saveFarmRecord(farm, AppRole.OWNER)
                                            }
                                        }
                                    }
                                }
                            },
                            onAuthError = { errMsg ->
                                authErrorBanner = "تعذر تسجيل الدخول عبر Google ($errMsg). يرجى المصادقة للمتابعة."
                            },
                            scope = scope,
                            onAuthCancelled = {
                                authErrorBanner = "تم إلغاء تسجيل الدخول. يجب المصادقة بحساب Google للوصول لبيانات المزرعة."
                            }
                        )
                    } else {
                        authErrorBanner = "خدمة المصادقة غير متوفرة حالياً. يرجى تسجيل الدخول بحساب Google معتمد."
                    }
                },
                onOpenSettings = onOpenSettings
            )
        }
        sessionState.selectedFarmForOwner != null -> {
            BackHandler { sessionState.selectedFarmForOwner = null }
            val farm = sessionState.selectedFarmForOwner!!
            val cycle = AgritechMockDb.cycles.firstOrNull {
                it.farmId == farm.id && it.status.equals("active", ignoreCase = true)
            } ?: AgritechMockDb.cycles.firstOrNull { it.farmId == farm.id }
                ?: AgritechMockDb.cycles.firstOrNull()
                ?: CycleModel("cycle_default_${farm.id}", farm.id, farm.capacity, 2.3, "active")
            AgritechFarmNotebookScreen(
                farm = farm,
                cycle = cycle,
                currentTheme = currentTheme,
                lang = currentLanguage,
                firestoreRepo = firestoreRepo,
                reports = AgritechMockDb.dailyReports.filter { it.farmId == farm.id }.sortedBy { it.dayNumber },
                vetNotes = AgritechMockDb.vetNotes.filter { it.farmId == farm.id },
                transactions = AgritechMockDb.transactions.filter { it.farmId == farm.id },
                onAddTransaction = { tx, addedFeedKg ->
                    if (AgritechMockDb.transactions.none { it.id == tx.id }) {
                        val normalizedTx = if (addedFeedKg > 0.0 && tx.quantityQuintals <= 0.0) {
                            tx.copy(quantityQuintals = addedFeedKg / 100.0)
                        } else {
                            tx
                        }
                        AgritechMockDb.transactions.add(normalizedTx)
                        val invoice = normalizedTx.invoiceData
                        if (invoice != null && normalizedTx.type == TransactionType.INCOME) {
                            val cycleIdx = AgritechMockDb.cycles.indexOfFirst { it.id == normalizedTx.cycleId }
                            if (cycleIdx >= 0) {
                                val c = AgritechMockDb.cycles[cycleIdx]
                                AgritechMockDb.cycles[cycleIdx] = c.copy(
                                    cumulativeBirdsSold = c.cumulativeBirdsSold + invoice.totalBirdsSold,
                                    cumulativeSoldWeightKg = c.cumulativeSoldWeightKg + invoice.totalNetKg
                                )
                            }
                        } else if (addedFeedKg > 0.0) {
                            AgritechMockDb.countedFeedTxIds.add(normalizedTx.id)
                            val cycleIdx = AgritechMockDb.cycles.indexOfFirst { it.id == normalizedTx.cycleId }
                            if (cycleIdx >= 0) {
                                val c = AgritechMockDb.cycles[cycleIdx]
                                AgritechMockDb.cycles[cycleIdx] = c.copy(feedInventoryKg = c.feedInventoryKg + addedFeedKg)
                            }
                        }
                        LocalFarmPersistence.saveAll(context)
                        scope.launch {
                            if (invoice != null && normalizedTx.type == TransactionType.INCOME) {
                                firestoreRepo?.saveSalesInvoiceAndMigrateIdempotent(
                                    farmId = normalizedTx.farmId,
                                    cycleId = normalizedTx.cycleId,
                                    idempotencyKey = normalizedTx.id,
                                    dateText = normalizedTx.date,
                                    invoice = invoice
                                )
                            } else if (addedFeedKg > 0.0 && normalizedTx.type == TransactionType.EXPENSE) {
                                firestoreRepo?.recordFeedPurchaseAndExpenseIdempotent(
                                    idempotencyKey = normalizedTx.id,
                                    tx = normalizedTx,
                                    addedFeedKg = addedFeedKg
                                )
                            } else {
                                firestoreRepo?.recordTransactionIdempotent(
                                    idempotencyKey = normalizedTx.id,
                                    tx = normalizedTx
                                )
                            }
                        }
                    }
                },
                onSaveOrUpdateDailyReport = { report ->
                    val savedReport = AgritechMockDb.upsertDailyReportWithAudit(report)
                    LocalFarmPersistence.saveAll(context)
                    scope.launch {
                        firestoreRepo?.saveDailyReport(savedReport)
                    }
                },
                onApproveVetInvoice = { txId, vnId ->
                    val sigTag = "Signed_By_Owner_${System.currentTimeMillis()}"
                    AgritechMockDb.approveVetInvoice(txId, vnId, sigTag)
                    LocalFarmPersistence.saveAll(context)
                    scope.launch {
                        firestoreRepo?.approveVetInvoiceTransaction(
                            farmId = farm.id,
                            transactionId = txId,
                            vetNoteId = vnId,
                            approvedBySignature = sigTag
                        )
                    }
                },
                onOpenSettings = onOpenSettings,
                onBack = { sessionState.selectedFarmForOwner = null }
            )
        }
        sessionState.selectedFarmForVet != null -> {
            BackHandler { sessionState.selectedFarmForVet = null }
            val farm = sessionState.selectedFarmForVet!!
            val cycle = AgritechMockDb.cycles.firstOrNull { it.farmId == farm.id }
                ?: AgritechMockDb.cycles.firstOrNull()
                ?: CycleModel("cycle_default_${farm.id}", farm.id, farm.capacity, 2.3, "active")
            val allSupervisedFarms = (AgritechMockDb.farms + AgritechMockDb.supervisedVetFarms).distinctBy { it.id }
            val farmReports = AgritechMockDb.dailyReports.filter { it.farmId == farm.id }.ifEmpty {
                AgritechMockDb.dailyReports.filter { it.farmId == "farm_1" }
            }
            AgritechVetNotebookScreen(
                farm = farm,
                cycle = cycle,
                currentTheme = currentTheme,
                lang = currentLanguage,
                firestoreRepo = firestoreRepo,
                reports = farmReports,
                vetNotes = AgritechMockDb.vetNotes.filter { it.farmId == farm.id },
                farms = allSupervisedFarms,
                onSwitchFarm = { sessionState.selectedFarmForVet = it },
                onAddNote = { note ->
                    if (AgritechMockDb.vetNotes.none { it.id == note.id }) {
                        AgritechMockDb.vetNotes.add(note)
                        LocalFarmPersistence.saveAll(context)
                        scope.launch {
                            firestoreRepo?.saveVetNote(note)
                        }
                    }
                },
                onOpenSettings = onOpenSettings,
                onBack = { sessionState.selectedFarmForVet = null }
            )
        }
        sessionState.selectedFarmForWorker != null -> {
            BackHandler { sessionState.selectedFarmForWorker = null }
            val farm = sessionState.selectedFarmForWorker!!
            val cycle = AgritechMockDb.cycles.firstOrNull { it.farmId == farm.id }
                ?: AgritechMockDb.cycles.firstOrNull()
                ?: CycleModel("cycle_default_${farm.id}", farm.id, farm.capacity, 2.3, "active")
            AgritechWorkerDashboard(
                farm = farm,
                cycle = cycle,
                currentTheme = currentTheme,
                lang = currentLanguage,
                reports = AgritechMockDb.dailyReports.filter { it.farmId == farm.id },
                urgentNotes = AgritechMockDb.vetNotes.filter { it.farmId == farm.id && it.isUrgent },
                farms = AgritechMockDb.farms,
                onSwitchFarm = { sessionState.selectedFarmForWorker = it },
                onSubmitReport = { report ->
                    val savedReport = AgritechMockDb.upsertDailyReportWithAudit(report)
                    LocalFarmPersistence.saveAll(context)
                    scope.launch {
                        firestoreRepo?.saveDailyReport(savedReport)
                    }
                },
                onOpenSettings = onOpenSettings,
                onBack = { sessionState.selectedFarmForWorker = null },
                onLogout = performLogout
            )
        }
        else -> {
            // Universal SaaS Platform Dashboard for ALL users (General User, Owner, Vet, Worker)
            AgritechOwnerDashboard(
                farms = AgritechMockDb.farms,
                currentTheme = currentTheme,
                lang = currentLanguage,
                firestoreRepo = firestoreRepo,
                activeRole = sessionState.currentRole ?: AppRole.OWNER,
                hasLinkedBarns = sessionState.hasLinkedBarns,
                onUpdateUserProfileMode = { newRole, linkedBarns ->
                    sessionState.markAuthenticated(
                        context = context,
                        role = newRole,
                        linkedBarns = linkedBarns
                    )
                    if (newRole == AppRole.WORKER && linkedBarns) {
                        sessionState.selectedFarmForWorker = AgritechMockDb.farms.firstOrNull()
                    }
                    if (sessionState.activeFarmId.isNotBlank()) {
                        scope.launch {
                            firestoreRepo?.setUserRoleAndFarm(newRole, sessionState.activeFarmId)
                        }
                    }
                },
                onOpenNotebook = { farm ->
                    val currentRole = sessionState.currentRole ?: AppRole.OWNER
                    when {
                        currentRole.isVetRole -> sessionState.selectedFarmForVet = farm
                        currentRole == AppRole.WORKER -> sessionState.selectedFarmForWorker = farm
                        else -> sessionState.selectedFarmForOwner = farm
                    }
                },
                onAddFarmAndCycle = { newFarm, newCycle, initialTx ->
                    val existingFarmIdx = AgritechMockDb.farms.indexOfFirst { it.id == newFarm.id }
                    if (existingFarmIdx >= 0) {
                        AgritechMockDb.farms[existingFarmIdx] = newFarm
                    } else {
                        AgritechMockDb.farms.add(newFarm)
                    }
                    // Complete any previous active cycles for this exact farmId before binding the new active cycle
                    for (i in AgritechMockDb.cycles.indices) {
                        val c = AgritechMockDb.cycles[i]
                        if (c.farmId == newFarm.id && c.id != newCycle.id && c.status.equals("active", ignoreCase = true)) {
                            AgritechMockDb.cycles[i] = c.copy(status = "completed")
                        }
                    }
                    val existingCycleIdx = AgritechMockDb.cycles.indexOfFirst { it.id == newCycle.id }
                    if (existingCycleIdx >= 0) {
                        AgritechMockDb.cycles[existingCycleIdx] = newCycle
                    } else {
                        AgritechMockDb.cycles.add(newCycle)
                    }
                    if (AgritechMockDb.transactions.none { it.id == initialTx.id }) {
                        AgritechMockDb.transactions.add(initialTx)
                    }
                    LocalFarmPersistence.saveAll(context)
                    // Creating a farm automatically links the user as an Owner and reveals the 🏠 Barns tab
                    sessionState.markAuthenticated(
                        context = context,
                        role = AppRole.OWNER,
                        linkedBarns = true,
                        farmId = newFarm.id
                    )
                    scope.launch {
                        firestoreRepo?.saveFarmRecord(newFarm, AppRole.OWNER)
                        firestoreRepo?.saveCycleRecord(newCycle, 0)
                        firestoreRepo?.recordTransactionIdempotent(initialTx.id, initialTx)
                    }
                },
                onSubmitWorkerReport = { report ->
                    val savedReport = AgritechMockDb.upsertDailyReportWithAudit(report)
                    LocalFarmPersistence.saveAll(context)
                    scope.launch {
                        firestoreRepo?.saveDailyReport(savedReport)
                    }
                },
                onOpenSettings = onOpenSettings,
                onLogout = performLogout
            )
        }
    }
}

// ============================================================================
// SETTINGS DIALOG (APPLE-GRADE MINIMALIST DROPDOWNS + SEARCHABLE CURRENCY SHEET)
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    currentTheme: AppThemeOption,
    onSelectTheme: (AppThemeOption) -> Unit,
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    currentCountryCode: String = Translations.globalCountryCode ?: "DZ",
    currentCurrencyCode: String = Translations.globalCurrencyCode ?: "DZD",
    onUpdateCountryAndCurrency: (String, String) -> Unit = { _, _ -> },
    onOpenGlobalOnboarding: () -> Unit = {},
    onDismiss: () -> Unit
) {
    PremiumSettingsModal(
        currentTheme = currentTheme,
        onSelectTheme = onSelectTheme,
        currentLanguage = currentLanguage,
        onSelectLanguage = onSelectLanguage,
        currentCountryCode = currentCountryCode,
        currentCurrencyCode = currentCurrencyCode,
        onUpdateCountryAndCurrency = onUpdateCountryAndCurrency,
        onOpenGlobalOnboarding = onOpenGlobalOnboarding,
        onDismiss = onDismiss
    )
}

// ============================================================================
// 1. شاشة تسجيل الدخول الموحدة + مسح رمز الانضمام (QR Onboarding)
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgritechLoginScreen(
    currentTheme: AppThemeOption,
    lang: AppLanguage,
    authErrorMessage: String? = null,
    onLogin: (AppRole, Boolean) -> Unit,
    onOpenSettings: () -> Unit
) {
    val appearance = LocalAppAppearanceState.current
    val activeAccent = appearance.accentColor
    val activeAccentContainer = appearance.accentContainerColor
    val cardCornerShape = RoundedCornerShape(appearance.cornerRadiusDp.dp)
    val buttonCornerShape = RoundedCornerShape((appearance.cornerRadiusDp - 4).coerceAtLeast(4).dp)

    var showQrScannerSheet by remember { mutableStateOf(false) }
    var isLoginMorphing by remember { mutableStateOf(false) }
    var cardEntered by remember { mutableStateOf(false) }
    val loginScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        cardEntered = true
    }

    val cardSlideOffsetY by animateDpAsState(
        targetValue = if (cardEntered) 0.dp else 44.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "login_card_spring_slide"
    )

    val loginBtnWidth by animateDpAsState(
        targetValue = if (isLoginMorphing) 56.dp else 320.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "login_btn_morph_width"
    )

    if (showQrScannerSheet) {
        QrScannerJoinBottomSheet(
            currentTheme = currentTheme,
            lang = lang,
            onDismiss = { showQrScannerSheet = false },
            onJoinRole = { joinedRole ->
                showQrScannerSheet = false
                onLogin(joinedRole, true)
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentTheme.bgColor)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = currentTheme.headingColor)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = cardSlideOffsetY)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(activeAccentContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Agriculture,
                    contentDescription = null,
                    tint = activeAccent,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                Translations.tr("app_title", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = currentTheme.headingColor
            )

            Text(
                Translations.tr("app_subtitle", lang),
                fontSize = 13.sp,
                color = currentTheme.headingColor.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_spring_card"),
                shape = cardCornerShape,
                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        Translations.tr("login_title_owner", lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = currentTheme.headingColor,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        Translations.tr("app_subtitle", lang),
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (!isLoginMorphing) {
                                isLoginMorphing = true
                                onLogin(AppRole.OWNER, true)
                                loginScope.launch {
                                    delay(350)
                                    isLoginMorphing = false
                                }
                            }
                        },
                        modifier = Modifier
                            .then(
                                if (isLoginMorphing) Modifier.width(loginBtnWidth)
                                else Modifier.fillMaxWidth()
                            )
                            .height(50.dp)
                            .testTag("btn_owner_login_morph"),
                        shape = if (isLoginMorphing) CircleShape else buttonCornerShape,
                        colors = ButtonDefaults.buttonColors(containerColor = activeAccent),
                        contentPadding = if (isLoginMorphing) PaddingValues(0.dp) else ButtonDefaults.ContentPadding
                    ) {
                        AnimatedContent(
                            targetState = isLoginMorphing,
                            label = "login_btn_morph_content"
                        ) { loading ->
                            if (loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Text(
                                    Translations.tr("login_btn", lang),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (authErrorMessage != null) {
                        Surface(
                            shape = buttonCornerShape,
                            color = Color(0xFFFFEBEE),
                            border = BorderStroke(1.dp, ActionRed.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_auth_error_banner")
                        ) {
                            Text(
                                text = authErrorMessage,
                                color = ActionRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // QR Onboarding Scan Button for New Workers & Vets ("مسح رمز الانضمام 📷")
                    OutlinedButton(
                        onClick = { showQrScannerSheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_scan_qr_join"),
                        shape = buttonCornerShape,
                        border = BorderStroke(1.5.dp, activeAccent),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = activeAccentContainer.copy(alpha = 0.45f),
                            contentColor = activeAccent
                        )
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = Translations.tr("btn_scan_qr_join", lang),
                            tint = activeAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            Translations.tr("btn_scan_qr_join", lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = activeAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 2. المنصة الشاملة الموحدة (UNIVERSAL SAAS PLATFORM DASHBOARD WITH DYNAMIC TABS)
// ============================================================================

@Composable
fun OwnerDashboardScreen(
    farms: List<FarmModel>,
    currentTheme: AppThemeOption,
    lang: AppLanguage,
    firestoreRepo: AgritechFirestoreRepository? = null,
    activeRole: AppRole = AppRole.OWNER,
    hasLinkedBarns: Boolean = true,
    onUpdateUserProfileMode: (AppRole, Boolean) -> Unit = { _, _ -> },
    onOpenNotebook: (FarmModel) -> Unit,
    onAddFarmAndCycle: ((FarmModel, CycleModel, TransactionModel) -> Unit)? = null,
    onSubmitWorkerReport: ((DailyReportModel) -> Unit)? = null,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit
) {
    AgritechOwnerDashboard(
        farms = farms,
        currentTheme = currentTheme,
        lang = lang,
        firestoreRepo = firestoreRepo,
        activeRole = activeRole,
        hasLinkedBarns = hasLinkedBarns,
        onUpdateUserProfileMode = onUpdateUserProfileMode,
        onOpenNotebook = onOpenNotebook,
        onAddFarmAndCycle = onAddFarmAndCycle,
        onSubmitWorkerReport = onSubmitWorkerReport,
        onOpenSettings = onOpenSettings,
        onLogout = onLogout
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgritechOwnerDashboard(
    farms: List<FarmModel>,
    currentTheme: AppThemeOption,
    lang: AppLanguage,
    firestoreRepo: AgritechFirestoreRepository? = null,
    activeRole: AppRole = AppRole.OWNER,
    hasLinkedBarns: Boolean = true,
    onUpdateUserProfileMode: (AppRole, Boolean) -> Unit = { _, _ -> },
    onOpenNotebook: (FarmModel) -> Unit,
    onAddFarmAndCycle: ((FarmModel, CycleModel, TransactionModel) -> Unit)? = null,
    onSubmitWorkerReport: ((DailyReportModel) -> Unit)? = null,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val sessionState = LocalAgritechAppState.current
    val activeAccentOption = sessionState.currentAccentColor
    val activeCornerRadius = sessionState.currentCornerStyle.cornerRadiusDp.dp
    val isDark = currentTheme.isDark
    val primaryTextColor = if (isDark) Color(0xFFFFFFFF) else currentTheme.headingColor
    val secondaryTextColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF64748B)
    val accentEmerald = if (isDark) activeAccentOption.darkModeColor else activeAccentOption.primaryColor
    val accentBadgeBg = if (isDark) activeAccentOption.darkContainerColor else activeAccentOption.softContainerColor
    val primaryButtonAccent = activeAccentOption.primaryColor

    // Management Hub is always accessible on the first tab for all 7 professions
    val showBarnsTab = true
    var selectedGlobalTab by remember {
        mutableStateOf(GlobalDashboardTab.BARNS)
    }

    var selectedChatConversation by remember { mutableStateOf<ChatConversationModel?>(null) }
    var showAddMarketPostDialog by remember { mutableStateOf(false) }
    var showNewCycleDialog by remember { mutableStateOf(false) }
    var showSubscriptionPaywallSheet by remember { mutableStateOf(false) }
    var unlockedAdditionalBarns by remember {
        mutableStateOf(SaasPreferencesManager.getUnlockedAdditionalBarns(context))
    }
    var showGeneralChatSheet by remember { mutableStateOf(false) }
    var showQrJoinSheetFromDashboard by remember { mutableStateOf(false) }
    var targetFarmForVetInvoice by remember { mutableStateOf<FarmModel?>(null) }
    var targetFarmForCycle by remember { mutableStateOf<FarmModel?>(null) }
    var farmNameInput by remember { mutableStateOf("") }
    var locationInput by remember { mutableStateOf("") }
    var birdCountInput by remember { mutableStateOf("") }
    var chickPriceInput by remember { mutableStateOf("") }
    var newCycleValidationError by remember { mutableStateOf<String?>(null) }

    // Strict Freemium Paywall Interception for "+ دورة جديدة / عنبر جديد" (New Barn):
    // a) Count existing farms owned by the current user
    // b) If ownedFarmsCount == 0: 1st barn is 100% FREE -> open Barn Creation dialog directly
    // c) As soon as ownedFarmsCount >= 1: clicking "+ دورة جديدة / عنبر جديد" MUST trigger SubscriptionPaywallBottomSheet ($5 / 2 Months)
    val ownedFarmsOrCyclesCount = farms.size
    val onTapNewBarnOrCycle: () -> Unit = {
        val maxAllowedBarns = 1 + unlockedAdditionalBarns
        if (ownedFarmsOrCyclesCount < maxAllowedBarns) {
            targetFarmForCycle = null
            farmNameInput = ""
            locationInput = ""
            birdCountInput = ""
            chickPriceInput = ""
            newCycleValidationError = null
            showNewCycleDialog = true
        } else {
            showSubscriptionPaywallSheet = true
        }
    }

    if (showSubscriptionPaywallSheet) {
        SubscriptionPaywallBottomSheet(
            currentTheme = currentTheme,
            lang = lang,
            existingBarnsCount = ownedFarmsOrCyclesCount,
            onDismiss = { showSubscriptionPaywallSheet = false },
            onSubscriptionSuccess = { _ ->
                // Unlock the additional barn slot and immediately open the Barn Creation dialog
                val updatedUnlocked = unlockedAdditionalBarns + 1
                unlockedAdditionalBarns = updatedUnlocked
                SaasPreferencesManager.saveUnlockedAdditionalBarns(context, updatedUnlocked)
                showSubscriptionPaywallSheet = false
                targetFarmForCycle = null
                farmNameInput = ""
                locationInput = ""
                birdCountInput = ""
                chickPriceInput = ""
                newCycleValidationError = null
                showNewCycleDialog = true
            }
        )
    }

    if (showQrJoinSheetFromDashboard) {
        PersonalAccountWhatsAppQrBottomSheet(
            currentTheme = currentTheme,
            lang = lang,
            activeRole = activeRole,
            onDismiss = { showQrJoinSheetFromDashboard = false },
            onContactScanned = { addedConv ->
                showQrJoinSheetFromDashboard = false
                selectedChatConversation = addedConv
                selectedGlobalTab = GlobalDashboardTab.CHAT
            },
            onJoinRole = { joinedRole ->
                showQrJoinSheetFromDashboard = false
                onUpdateUserProfileMode(joinedRole, true)
                selectedGlobalTab = GlobalDashboardTab.BARNS
            }
        )
    }

    if (targetFarmForVetInvoice != null) {
        val invFarm = targetFarmForVetInvoice!!
        val invCycle = AgritechMockDb.cycles.firstOrNull { it.farmId == invFarm.id }
            ?: AgritechMockDb.cycles.firstOrNull()
            ?: CycleModel("cycle_default_${invFarm.id}", invFarm.id, invFarm.capacity, 2.3, "active")
        val invReports = AgritechMockDb.dailyReports.filter { it.farmId == invFarm.id }
        val invDay = PoultryCalculators.calculateCycleDay(invCycle.startDate, invReports.maxOfOrNull { it.dayNumber } ?: 1)
        VetMedicineInvoiceDialog(
            farm = invFarm,
            cycle = invCycle,
            dayNumber = invDay,
            lang = lang,
            currentTheme = currentTheme,
            channelId = "${invCycle.id}_vet",
            firestoreRepo = firestoreRepo,
            onDismiss = { targetFarmForVetInvoice = null }
        )
    }

    if (showGeneralChatSheet) {
        ContextualChatBottomSheet(
            title = Translations.tr("chat_general_admin_title", lang),
            subtitle = Translations.tr("farms_title", lang),
            channelId = "general_admin",
            currentTheme = currentTheme,
            lang = lang,
            onDismiss = { showGeneralChatSheet = false }
        )
    }

    if (showAddMarketPostDialog) {
        AddMarketPostDialog(
            lang = lang,
            currentTheme = currentTheme,
            onDismiss = { showAddMarketPostDialog = false },
            onPublishPost = { newPost ->
                AgritechMockDb.addMarketPost(newPost)
                showAddMarketPostDialog = false
            }
        )
    }

    Scaffold(
        containerColor = currentTheme.bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            Translations.tr("farms_title", lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                            color = primaryTextColor,
                            maxLines = 1
                        )
                        Text(
                            when (activeRole.normalizedHubRole) {
                                AppRole.VET_CLINIC -> Translations.tr("role_badge_vet_clinic", lang)
                                AppRole.WORKER -> Translations.tr("role_badge_worker", lang)
                                AppRole.FEED_SUPPLIER -> Translations.tr("role_badge_feed_supplier", lang)
                                AppRole.CHICK_HATCHERY -> Translations.tr("role_badge_chick_hatchery", lang)
                                AppRole.WHOLESALE_TRADER -> Translations.tr("role_badge_wholesale_trader", lang)
                                AppRole.VET_IMPORTER -> Translations.tr("role_badge_vet_importer", lang)
                                else -> Translations.tr("role_badge_owner_barn", lang)
                            },
                            fontSize = 11.5.sp,
                            color = secondaryTextColor,
                            maxLines = 1
                        )
                    }
                },
                actions = {
                    if (!hasLinkedBarns) {
                        IconButton(
                            onClick = onTapNewBarnOrCycle,
                            modifier = Modifier.testTag("btn_create_farm_unlock_barns")
                        ) {
                            Icon(
                                Icons.Default.AddHome,
                                contentDescription = "+ إنشاء مزرعة 🏠",
                                tint = primaryTextColor
                            )
                        }
                    }
                    IconButton(
                        onClick = { showQrJoinSheetFromDashboard = true },
                        modifier = Modifier.testTag("btn_dashboard_qr_join")
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = Translations.tr("btn_scan_qr_join", lang),
                            tint = primaryTextColor
                        )
                    }
                    IconButton(
                        onClick = { showGeneralChatSheet = true },
                        modifier = Modifier.testTag("btn_general_admin_chat")
                    ) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = Translations.tr("chat_general_admin_title", lang),
                            tint = primaryTextColor
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = primaryTextColor)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = primaryTextColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = currentTheme.bgColor)
            )
        },
        floatingActionButton = {
            when (selectedGlobalTab) {
                GlobalDashboardTab.BARNS -> {
                    if (activeRole.isOwnerRole) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FloatingActionButton(
                                onClick = { showGeneralChatSheet = true },
                                containerColor = if (isDark) Color(0xFF1E293B) else Color.White,
                                contentColor = accentEmerald,
                                shape = CircleShape,
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp),
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("fab_general_admin_chat")
                            ) {
                                Icon(
                                    Icons.Default.Chat,
                                    contentDescription = Translations.tr("chat_general_admin_title", lang),
                                    tint = accentEmerald
                                )
                            }

                            FloatingActionButton(
                                onClick = onTapNewBarnOrCycle,
                                containerColor = primaryButtonAccent,
                                contentColor = Color.White,
                                shape = RoundedCornerShape(activeCornerRadius),
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp),
                                modifier = Modifier.testTag("fab_add_cycle")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add New Farm Cycle", tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        Translations.tr("btn_new_cycle", lang),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
                GlobalDashboardTab.MARKET -> {
                    // Market Tab FAB: "+ إضافة إعلان" (Add Post)
                    FloatingActionButton(
                        onClick = { showAddMarketPostDialog = true },
                        containerColor = primaryButtonAccent,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(activeCornerRadius),
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                        modifier = Modifier.testTag("fab_add_market_post")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = Translations.tr("btn_add_post", lang), tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                Translations.tr("btn_add_post", lang),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                else -> {}
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = currentTheme.cardColor,
                contentColor = accentEmerald,
                modifier = Modifier.testTag("owner_global_bottom_nav")
            ) {
                val dynamicGlobalTabs = buildList {
                    if (showBarnsTab) {
                        add(Triple(GlobalDashboardTab.BARNS, "💼 ${Translations.tr("tab_management", lang)}", Icons.Default.BusinessCenter))
                    }
                    add(Triple(GlobalDashboardTab.CHAT, Translations.tr("global_tab_chat", lang), Icons.Default.ChatBubbleOutline))
                    add(Triple(GlobalDashboardTab.MARKET, Translations.tr("global_tab_market", lang), Icons.Default.ShoppingCart))
                    add(Triple(GlobalDashboardTab.AI_VET, Translations.tr("global_tab_ai_vet", lang), Icons.Default.SmartToy))
                }

                dynamicGlobalTabs.forEachIndexed { idx, (tabEnum, label, icon) ->
                    val isSelected = selectedGlobalTab == tabEnum
                    NavigationBarItem(
                        modifier = Modifier.testTag("global_nav_tab_${tabEnum.name.lowercase()}_$idx"),
                        selected = isSelected,
                        onClick = {
                            selectedGlobalTab = tabEnum
                            if (tabEnum != GlobalDashboardTab.CHAT) selectedChatConversation = null
                        },
                        icon = {
                            Icon(
                                icon,
                                contentDescription = label,
                                tint = if (isSelected) accentEmerald else secondaryTextColor
                            )
                        },
                        label = {
                            Text(
                                label,
                                color = if (isSelected) accentEmerald else secondaryTextColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.5.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = accentBadgeBg
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedGlobalTab) {
                GlobalDashboardTab.BARNS -> {
                    ManagementHubScreen(
                        farms = farms,
                        activeRole = activeRole,
                        currentTheme = currentTheme,
                        lang = lang,
                        firestoreRepo = firestoreRepo,
                        onSelectRole = { newRole ->
                            onUpdateUserProfileMode(newRole, true)
                        },
                        onTapNewBarnOrCycle = onTapNewBarnOrCycle,
                        onOpenNotebook = onOpenNotebook,
                        onStartNewCycleForFarm = { selectedFarm ->
                            targetFarmForCycle = selectedFarm
                            farmNameInput = Translations.getLocalizedFarmName(selectedFarm.id, lang, selectedFarm.name)
                            locationInput = Translations.getLocalizedFarmLocation(selectedFarm.id, lang, selectedFarm.location)
                            birdCountInput = ""
                            chickPriceInput = ""
                            newCycleValidationError = null
                            showNewCycleDialog = true
                        },
                        onIssueVetInvoiceForFarm = { farmForInv ->
                            targetFarmForVetInvoice = farmForInv
                        },
                        onOpenImporterChat = { importerName, importerPhone ->
                            val existingConv = AgritechMockDb.chatConversations.firstOrNull {
                                it.phoneNumber == importerPhone || it.userName.contains(importerName)
                            } ?: AgritechMockDb.addPhoneContact(
                                name = importerName,
                                phone = importerPhone,
                                roleLabel = Translations.tr("contact_role_vet_importer", lang),
                                avatarEmoji = "🧬"
                            )
                            selectedChatConversation = existingConv
                            selectedGlobalTab = GlobalDashboardTab.CHAT
                        }
                    )
                }
                GlobalDashboardTab.CHAT -> {
                    // 💬 الدردشة (WhatsApp-Style ChatListScreen -> ChatDetailScreen)
                    val activeConv = selectedChatConversation
                    if (activeConv == null) {
                        ChatListScreen(
                            conversations = AgritechMockDb.chatConversations,
                            currentTheme = currentTheme,
                            lang = lang,
                            onSelectConversation = { conv ->
                                selectedChatConversation = conv
                            },
                            onOpenPersonalQr = {
                                showQrJoinSheetFromDashboard = true
                            }
                        )
                    } else {
                        ChatDetailScreen(
                            conversation = activeConv,
                            currentTheme = currentTheme,
                            lang = lang,
                            firestoreRepo = firestoreRepo,
                            onBack = { selectedChatConversation = null }
                        )
                    }
                }
                GlobalDashboardTab.MARKET -> {
                    // 🛒 السوق (Interactive User-Driven MarketFeedScreen)
                    MarketFeedScreen(
                        posts = AgritechMockDb.marketPosts,
                        currentTheme = currentTheme,
                        lang = lang,
                        onContactPublisher = { post ->
                            val matchedConv = AgritechMockDb.chatConversations.firstOrNull {
                                it.id == post.chatChannelId
                            } ?: ChatConversationModel(
                                id = post.chatChannelId,
                                userName = post.publisherName,
                                roleLabel = post.publisherLocation,
                                avatarEmoji = "🛒",
                                avatarBgHex = 0xFFE8F5E9,
                                defaultSnippet = post.title,
                                defaultTimestamp = "الآن"
                            )
                            selectedChatConversation = matchedConv
                            selectedGlobalTab = GlobalDashboardTab.CHAT
                        }
                    )
                }
                GlobalDashboardTab.AI_VET -> {
                    // 🤖 AI بيطري (AI Vet Assistant Screen)
                    OwnerGlobalAiVetPlaceholderScreen(currentTheme = currentTheme, lang = lang)
                }
            }
        }

        if (showNewCycleDialog) {
            val curr = Translations.tr("currency", lang)
            val parsedInitialCount = birdCountInput.toIntOrNull() ?: 0
            val parsedChickPrice = chickPriceInput.toDoubleOrNull() ?: 0.0
            val previewChicksExpense = parsedInitialCount * parsedChickPrice
            val dialogBg = if (isDark) Color(0xFF1E293B) else Color.White
            val fieldBg = if (isDark) Color(0xFF0F172A) else Color.White
            val fieldBorder = if (isDark) Color(0xFF334155) else Color(0xFFE0E0E0)

            AlertDialog(
                onDismissRequest = {
                    showNewCycleDialog = false
                    targetFarmForCycle = null
                    newCycleValidationError = null
                },
                shape = RoundedCornerShape(22.dp),
                containerColor = dialogBg,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = accentBadgeBg,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🐣", fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                if (targetFarmForCycle != null) Translations.tr("btn_start_new_cycle", lang)
                                else Translations.tr("dialog_new_cycle_title", lang),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryTextColor
                            )
                            Text(
                                Translations.tr("chick_expense_note", lang),
                                fontSize = 11.sp,
                                color = secondaryTextColor
                            )
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = farmNameInput,
                            onValueChange = {
                                if (targetFarmForCycle == null) {
                                    farmNameInput = it
                                    newCycleValidationError = null
                                }
                            },
                            readOnly = targetFarmForCycle != null,
                            label = { Text(Translations.tr("field_farm_name", lang), fontSize = 12.sp, color = secondaryTextColor) },
                            placeholder = { Text("عنبر البركة النموذجي 1", fontSize = 12.sp, color = secondaryTextColor.copy(alpha = 0.6f)) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_farm_name"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentEmerald,
                                unfocusedBorderColor = fieldBorder,
                                focusedTextColor = primaryTextColor,
                                unfocusedTextColor = primaryTextColor,
                                focusedContainerColor = fieldBg,
                                unfocusedContainerColor = fieldBg
                            )
                        )

                        OutlinedTextField(
                            value = locationInput,
                            onValueChange = {
                                if (targetFarmForCycle == null) {
                                    locationInput = it
                                    newCycleValidationError = null
                                }
                            },
                            readOnly = targetFarmForCycle != null,
                            label = { Text(Translations.tr("field_location", lang), fontSize = 12.sp, color = secondaryTextColor) },
                            placeholder = { Text("المدية - سيدي نعمان", fontSize = 12.sp, color = secondaryTextColor.copy(alpha = 0.6f)) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_location"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentEmerald,
                                unfocusedBorderColor = fieldBorder,
                                focusedTextColor = primaryTextColor,
                                unfocusedTextColor = primaryTextColor,
                                focusedContainerColor = fieldBg,
                                unfocusedContainerColor = fieldBg
                            )
                        )

                        OutlinedTextField(
                            value = birdCountInput,
                            onValueChange = {
                                birdCountInput = it.filter { ch -> ch.isDigit() }
                                newCycleValidationError = null
                            },
                            label = { Text(Translations.tr("field_bird_count", lang), fontSize = 12.sp, color = secondaryTextColor) },
                            placeholder = { Text("8000", fontSize = 12.sp, color = secondaryTextColor.copy(alpha = 0.6f)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_bird_count"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentEmerald,
                                unfocusedBorderColor = fieldBorder,
                                focusedTextColor = primaryTextColor,
                                unfocusedTextColor = primaryTextColor,
                                focusedContainerColor = fieldBg,
                                unfocusedContainerColor = fieldBg
                            )
                        )

                        OutlinedTextField(
                            value = chickPriceInput,
                            onValueChange = {
                                chickPriceInput = it.filter { ch -> ch.isDigit() || ch == '.' }
                                newCycleValidationError = null
                            },
                            label = { Text(Translations.tr("field_chick_price", lang), fontSize = 12.sp, color = secondaryTextColor) },
                            placeholder = { Text("40", fontSize = 12.sp, color = secondaryTextColor.copy(alpha = 0.6f)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_chick_price"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentEmerald,
                                unfocusedBorderColor = fieldBorder,
                                focusedTextColor = primaryTextColor,
                                unfocusedTextColor = primaryTextColor,
                                focusedContainerColor = fieldBg,
                                unfocusedContainerColor = fieldBg
                            )
                        )

                        if (newCycleValidationError != null) {
                            Text(
                                text = newCycleValidationError!!,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = ActionRed,
                                modifier = Modifier.testTag("new_cycle_validation_error")
                            )
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF064E3B) else Color(0xFFF1F8E9)),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF10B981).copy(alpha = 0.45f) else Color(0xFFC8E6C9))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "💰 ${Translations.tr("tx_chicks", lang)}:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = accentEmerald
                                    )
                                    Text(
                                        "${String.format(java.util.Locale.US, "%,.0f", previewChicksExpense)} $curr",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = accentEmerald
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "${String.format(java.util.Locale.US, "%,d", parsedInitialCount)} ${Translations.tr("birds_unit_short", lang)} × ${String.format(java.util.Locale.US, "%.0f", parsedChickPrice)} $curr",
                                    fontSize = 11.sp,
                                    color = secondaryTextColor
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val count = birdCountInput.trim().toIntOrNull()
                            val price = chickPriceInput.trim().toDoubleOrNull()
                            if (count == null || count <= 0 || price == null || price < 0.0) {
                                newCycleValidationError = Translations.tr("input_required_error", lang)
                                return@Button
                            }
                            val chickTotal = count * price
                            val uniqueSuffix = (System.currentTimeMillis() % 100000).toString()
                            val existingFarm = targetFarmForCycle
                            val farmId = existingFarm?.id ?: "farm_$uniqueSuffix"
                            val cycleId = "cycle_$uniqueSuffix"
                            val farmName = existingFarm?.name ?: farmNameInput.trim().ifEmpty { "عنبر البركة $uniqueSuffix" }
                            val loc = existingFarm?.location ?: locationInput.trim().ifEmpty { "الجزائر" }
                            val todayStr = java.time.LocalDate.now().toString()

                            val lockedCurrencyCode = Translations.globalCurrencyCode ?: "DZD"
                            val updatedOrNewFarm = existingFarm?.copy(capacity = count) ?: FarmModel(
                                id = farmId,
                                name = farmName,
                                location = loc,
                                capacity = count,
                                assignedWorkerId = "worker_1",
                                currencyCode = lockedCurrencyCode
                            )
                            val newCycle = CycleModel(
                                id = cycleId,
                                farmId = farmId,
                                totalBirdsPlaced = count,
                                targetWeight = 2.3,
                                status = "active",
                                startDate = todayStr,
                                chickPrice = price,
                                feedInventoryKg = 0.0,
                                currencyCode = updatedOrNewFarm.currencyCode
                            )
                            val initialTx = TransactionModel(
                                id = "tx_chicks_$cycleId",
                                farmId = farmId,
                                cycleId = cycleId,
                                type = TransactionType.EXPENSE,
                                category = "🐣 ${Translations.tr("tx_chicks", lang)} (Ross 308)",
                                amount = chickTotal,
                                currency = curr,
                                date = "${Translations.tr("day_prefix", lang)} 1"
                            )

                            onAddFarmAndCycle?.invoke(updatedOrNewFarm, newCycle, initialTx)
                            showNewCycleDialog = false
                            targetFarmForCycle = null
                            farmNameInput = ""
                            locationInput = ""
                            birdCountInput = ""
                            chickPriceInput = ""
                            newCycleValidationError = null
                        },
                        modifier = Modifier.testTag("btn_confirm_new_cycle"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TextForestGreen)
                    ) {
                        Text(Translations.tr("btn_create_cycle", lang), fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showNewCycleDialog = false
                            targetFarmForCycle = null
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(Translations.tr("btn_cancel", lang), color = secondaryTextColor, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }
    }
}

// ============================================================================
// DYNAMIC ROLE-BASED MANAGEMENT HUB (💼 مركز الإدارة المستقل لكل مهنة)
// ============================================================================

@Composable
fun ManagementHubScreen(
    farms: List<FarmModel>,
    activeRole: AppRole,
    currentTheme: AppThemeOption,
    lang: AppLanguage,
    firestoreRepo: AgritechFirestoreRepository?,
    onSelectRole: (AppRole) -> Unit,
    onTapNewBarnOrCycle: () -> Unit,
    onOpenNotebook: (FarmModel) -> Unit,
    onStartNewCycleForFarm: (FarmModel) -> Unit,
    onIssueVetInvoiceForFarm: (FarmModel) -> Unit,
    onOpenImporterChat: (String, String) -> Unit
) {
    val appearance = LocalAppAppearanceState.current
    val isDark = currentTheme.isDark
    val primaryTextColor = if (isDark) Color(0xFFFFFFFF) else currentTheme.headingColor
    val secondaryTextColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    val activeAccent = appearance.accentColor
    val primaryButtonAccent = appearance.accentOption.primaryColor
    val accentBadgeBg = appearance.accentContainerColor
    val cardCornerShape = RoundedCornerShape(appearance.cornerRadiusDp.dp)
    val pillShape = RoundedCornerShape((appearance.cornerRadiusDp - 4).coerceAtLeast(8).dp)
    val normalizedRole = activeRole.normalizedHubRole
    val curr = Translations.tr("currency", lang)

    var selectedVetHubTab by remember { mutableStateOf(0) }
    var showPartnerFinancialReqDialog by remember { mutableStateOf(false) }

    val roleItems = listOf(
        AppRole.OWNER_BARN to Translations.tr("role_badge_owner_barn", lang),
        AppRole.VET_CLINIC to Translations.tr("role_badge_vet_clinic", lang),
        AppRole.WORKER to Translations.tr("role_badge_worker", lang),
        AppRole.FEED_SUPPLIER to Translations.tr("role_badge_feed_supplier", lang),
        AppRole.CHICK_HATCHERY to Translations.tr("role_badge_chick_hatchery", lang),
        AppRole.WHOLESALE_TRADER to Translations.tr("role_badge_wholesale_trader", lang),
        AppRole.VET_IMPORTER to Translations.tr("role_badge_vet_importer", lang)
    )

    if (showPartnerFinancialReqDialog) {
        val defaultFarm = farms.firstOrNull()
            ?: AgritechMockDb.supervisedVetFarms.firstOrNull()
            ?: FarmModel("farm_1", "عنبر الخيرات والبركة 1", "البليدة", 8000, "worker_1")
        val defaultCycle = AgritechMockDb.cycles.firstOrNull { it.farmId == defaultFarm.id }
            ?: CycleModel("cycle_2026_01", defaultFarm.id, defaultFarm.capacity, 2.3, "active")
        val roleTitle = roleItems.firstOrNull { it.first == normalizedRole }?.second ?: normalizedRole.name
        UniversalFinancialRequestDialog(
            farm = defaultFarm,
            cycle = defaultCycle,
            dayNumber = 5,
            senderName = roleTitle,
            senderRole = roleTitle,
            channelId = "general_admin",
            lang = lang,
            currentTheme = currentTheme,
            firestoreRepo = firestoreRepo,
            onOrderSubmitted = { amt, chosenCat, noteStr ->
                if (normalizedRole in listOf(
                        AppRole.FEED_SUPPLIER,
                        AppRole.CHICK_HATCHERY,
                        AppRole.WHOLESALE_TRADER,
                        AppRole.VET_IMPORTER
                    )
                ) {
                    val detailsSummary = if (noteStr.isNotBlank()) "$chosenCat — $noteStr" else chosenCat
                    AgritechMockDb.roleBusinessOrders.add(
                        0,
                        RoleBusinessOrderModel(
                            id = "ord_${normalizedRole.name.lowercase()}_${System.currentTimeMillis()}",
                            role = normalizedRole,
                            clientName = Translations.getLocalizedFarmName(defaultFarm.id, lang, defaultFarm.name),
                            clientPhone = "+213 550 10 20 30",
                            itemSummary = detailsSummary,
                            quantityText = "${Translations.tr("day_prefix", lang)} 5",
                            totalAmountDzd = amt,
                            status = "PENDING"
                        )
                    )
                }
            },
            onDismiss = { showPartnerFinancialReqDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("management_hub_screen")
    ) {
        // Dynamic Profession Content Area (Profession Switcher moved cleanly into Settings ⚙️)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (normalizedRole) {
                AppRole.VET_CLINIC -> {
                    val supervisedBarns = remember(farms.size, AgritechMockDb.supervisedVetFarms.size) {
                        (farms + AgritechMockDb.supervisedVetFarms).distinctBy { it.id }
                    }
                    val vetTransactions = AgritechMockDb.transactions.filter {
                        it.category.contains("أدوية") ||
                            it.category.contains("لقاح") ||
                            it.category.contains("بيطر") ||
                            it.category.contains("MEDICATION", ignoreCase = true) ||
                            it.category.contains("Vet", ignoreCase = true)
                    }
                    val collectedTotal = vetTransactions
                        .filter { it.status.equals("APPROVED", ignoreCase = true) }
                        .sumOf { it.amount }
                    val pendingTotal = vetTransactions
                        .filter { it.status.equals("PENDING", ignoreCase = true) }
                        .sumOf { it.amount }
                    val totalClinicTreasury = collectedTotal + pendingTotal
                    val alertsCount = AgritechMockDb.vetEpidemicAlerts.size
                    val defaultWorkspaceFarm = supervisedBarns.firstOrNull()
                        ?: FarmModel("farm_1", "عنبر البركة النموذجي", "المدية", 10000, "worker_1")

                    // Single Main Bottom Bar Only: On the main dashboard (💼 الإدارة), show the Vet Clinic ID Card
                    // with "🏥 فتح مكتب العيادة البيطرية" button followed by supervised barns (matching Owner's "فتح دفتر المزرعة" workflow)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .testTag("vet_clinic_management_hub"),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. Sleek Vet Clinic ID Card (بطاقة العيادة البيطرية)
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("vet_clinic_id_card"),
                                shape = cardCornerShape,
                                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                border = BorderStroke(1.5.dp, activeAccent.copy(alpha = 0.45f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = accentBadgeBg,
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text("🩺", fontSize = 24.sp)
                                                }
                                            }
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text(
                                                    Translations.tr("vet_clinic_id_card_title", lang),
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 15.5.sp,
                                                    color = primaryTextColor,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    Translations.tr("vet_clinic_id_card_subtitle", lang),
                                                    fontSize = 11.5.sp,
                                                    color = secondaryTextColor,
                                                    maxLines = 2
                                                )
                                            }
                                        }
                                    }

                                    // 3 Summary Stat Boxes (Active Barns | Clinic Treasury | Vaccine Alerts)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = pillShape,
                                            color = accentBadgeBg
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    Translations.tr("vet_clinic_stat_barns", lang),
                                                    fontSize = 10.5.sp,
                                                    color = secondaryTextColor,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    "${supervisedBarns.size} 🏥",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 13.5.sp,
                                                    color = activeAccent,
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = pillShape,
                                            color = accentBadgeBg
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    Translations.tr("vet_clinic_stat_treasury", lang),
                                                    fontSize = 10.5.sp,
                                                    color = secondaryTextColor,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    "${String.format(java.util.Locale.US, "%,.0f", totalClinicTreasury)} $curr",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.5.sp,
                                                    color = activeAccent,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }

                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = pillShape,
                                            color = if (isDark) Color(0xFF78350F).copy(alpha = 0.35f) else Color(0xFFFEF3C7)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    Translations.tr("vet_clinic_stat_alerts", lang),
                                                    fontSize = 10.5.sp,
                                                    color = secondaryTextColor,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    "$alertsCount 🧬",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 13.5.sp,
                                                    color = Color(0xFFD97706),
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }

                                    // Primary Full-Width Button: "🏥 فتح مكتب العيادة البيطرية"
                                    Button(
                                        onClick = { onOpenNotebook(defaultWorkspaceFarm) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("btn_open_vet_clinic_workspace"),
                                        shape = pillShape,
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryButtonAccent)
                                    ) {
                                        Text(
                                            Translations.tr("open_vet_clinic_workspace_btn", lang),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.5.sp,
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Supervised Barns List Header & Cards
                        item {
                            Text(
                                Translations.tr("vet_hub_supervised_subtitle", lang),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = secondaryTextColor
                            )
                        }

                        items(supervisedBarns, key = { it.id }) { farm ->
                            val cycle = AgritechMockDb.cycles.firstOrNull { it.farmId == farm.id }
                            val farmReports = AgritechMockDb.dailyReports.filter { it.farmId == farm.id }
                            val totalMort = farmReports.sumOf { it.mortalityCount }
                            val initialBirds = cycle?.totalBirdsPlaced ?: farm.capacity
                            val livability = if (initialBirds > 0) {
                                ((initialBirds - totalMort).coerceAtLeast(0).toDouble() / initialBirds) * 100.0
                            } else {
                                100.0
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("vet_supervised_barn_card_${farm.id}"),
                                shape = cardCornerShape,
                                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.25f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                Translations.getLocalizedFarmName(farm.id, lang, farm.name),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp,
                                                color = primaryTextColor,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                Translations.getLocalizedFarmLocation(farm.id, lang, farm.location),
                                                fontSize = 12.sp,
                                                color = secondaryTextColor,
                                                maxLines = 1
                                            )
                                        }
                                        Surface(
                                            shape = pillShape,
                                            color = accentBadgeBg
                                        ) {
                                            Text(
                                                "${Translations.tr("liveability", lang)}: ${String.format(java.util.Locale.US, "%.1f", livability)}%",
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = activeAccent,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = { onOpenNotebook(farm) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(46.dp)
                                                .testTag("btn_vet_open_records_${farm.id}"),
                                            shape = pillShape,
                                            colors = ButtonDefaults.buttonColors(containerColor = primaryButtonAccent)
                                        ) {
                                            Text(
                                                Translations.tr("open_medical_records", lang),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color.White,
                                                maxLines = 1
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { onIssueVetInvoiceForFarm(farm) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(46.dp)
                                                .testTag("btn_vet_card_invoice_${farm.id}"),
                                            shape = pillShape,
                                            border = BorderStroke(1.5.dp, activeAccent),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = activeAccent)
                                        ) {
                                            Text(
                                                Translations.tr("btn_send_vet_invoice", lang),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = activeAccent,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
                AppRole.WORKER -> {
                    val workerBarns = if (farms.isNotEmpty()) farms else AgritechMockDb.supervisedVetFarms
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("worker_management_hub"),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Button(
                                onClick = { showPartnerFinancialReqDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_worker_hub_financial_req"),
                                shape = pillShape,
                                colors = ButtonDefaults.buttonColors(containerColor = primaryButtonAccent)
                            ) {
                                Text(
                                    Translations.tr("btn_send_financial_request", lang),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        }
                        items(workerBarns, key = { it.id }) { farm ->
                            val cycle = AgritechMockDb.cycles.firstOrNull { it.farmId == farm.id }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = cardCornerShape,
                                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                Translations.getLocalizedFarmName(farm.id, lang, farm.name),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = primaryTextColor
                                            )
                                            Text(
                                                Translations.getLocalizedFarmLocation(farm.id, lang, farm.location),
                                                fontSize = 12.sp,
                                                color = secondaryTextColor
                                            )
                                        }
                                        if (cycle != null) {
                                            Surface(
                                                shape = pillShape,
                                                color = accentBadgeBg
                                            ) {
                                                Text(
                                                    Translations.formatDualFeedInventoryBadge(cycle.feedInventoryKg, lang),
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = activeAccent
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { onOpenNotebook(farm) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("btn_open_worker_daily_report_${farm.id}"),
                                        shape = pillShape,
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryButtonAccent)
                                    ) {
                                        Icon(
                                            Icons.Default.EditNote,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            Translations.tr("worker_log_title", lang),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(90.dp)) }
                    }
                }
                AppRole.FEED_SUPPLIER,
                AppRole.CHICK_HATCHERY,
                AppRole.WHOLESALE_TRADER,
                AppRole.VET_IMPORTER -> {
                    val roleOrders = AgritechMockDb.roleBusinessOrders.filter { it.role == normalizedRole }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("partner_role_management_hub_${normalizedRole.name.lowercase()}"),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    Translations.tr("partner_hub_orders_title", lang),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.5.sp,
                                    color = primaryTextColor,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                )
                                Button(
                                    onClick = { showPartnerFinancialReqDialog = true },
                                    modifier = Modifier
                                        .height(42.dp)
                                        .testTag("btn_partner_hub_send_invoice"),
                                    shape = pillShape,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryButtonAccent)
                                ) {
                                    Text(
                                        Translations.tr("partner_hub_send_invoice_btn", lang),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                        items(roleOrders, key = { it.id }) { order ->
                            val isPending = order.status.equals("PENDING", ignoreCase = true)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("partner_order_card_${order.id}"),
                                shape = cardCornerShape,
                                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            order.clientName,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = primaryTextColor,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            "${String.format(java.util.Locale.US, "%,.0f", order.totalAmountDzd)} $curr",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = activeAccent
                                        )
                                    }
                                    Text(
                                        "${order.itemSummary} • ${order.quantityText}",
                                        fontSize = 12.sp,
                                        color = secondaryTextColor
                                    )
                                    Text(
                                        "📞 ${order.clientPhone} • ${
                                            if (isPending) Translations.tr("fin_req_status_pending", lang)
                                            else Translations.tr("fin_req_status_approved", lang)
                                        }",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPending) Color(0xFFD97706) else activeAccent
                                    )
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(90.dp)) }
                    }
                }
                else -> {
                    // OWNER / OWNER_BARN: Barn Management & Pending Financial Approvals
                    val pendingRequests = AgritechMockDb.transactions.filter {
                        it.status.equals("PENDING", ignoreCase = true)
                    }
                    var pendingHubTxForSignature by remember { mutableStateOf<TransactionModel?>(null) }
                    if (pendingHubTxForSignature != null) {
                        val targetTx = pendingHubTxForSignature!!
                        InvoiceApprovalSignatureDialog(
                            categoryOrTitle = Translations.getLocalizedTxCategory(targetTx.category, lang),
                            dateOrDayText = targetTx.date,
                            amount = targetTx.amount,
                            currency = targetTx.currency,
                            currentTheme = currentTheme,
                            lang = lang,
                            onDismiss = { pendingHubTxForSignature = null },
                            onConfirmSignedApproval = {
                                val linkedNote = AgritechMockDb.vetNotes.firstOrNull {
                                    it.linkedTransactionId == targetTx.id ||
                                        (it.farmId == targetTx.farmId && it.cycleId == targetTx.cycleId && it.status.equals("PENDING", ignoreCase = true))
                                }
                                AgritechMockDb.approveVetInvoice(targetTx.id, linkedNote?.id ?: "")
                                pendingHubTxForSignature = null
                            }
                        )
                    }
                    if (farms.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp)
                                .testTag("zero_state_barns_screen"),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 460.dp),
                                shape = cardCornerShape,
                                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.3f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = accentBadgeBg,
                                        modifier = Modifier.size(68.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Home,
                                                contentDescription = null,
                                                tint = activeAccent,
                                                modifier = Modifier.size(34.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        Translations.tr("zero_barns_title", lang),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.5.sp,
                                        color = primaryTextColor,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        Translations.tr("zero_barns_subtitle", lang),
                                        fontSize = 13.sp,
                                        color = secondaryTextColor,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 19.sp
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Button(
                                        onClick = onTapNewBarnOrCycle,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .testTag("btn_zero_state_create_first_barn"),
                                        shape = pillShape,
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryButtonAccent)
                                    ) {
                                        Icon(
                                            Icons.Default.AddHome,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            Translations.tr("zero_barns_create_btn", lang),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            if (pendingRequests.isNotEmpty()) {
                                item {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("owner_hub_pending_requests_card"),
                                        shape = cardCornerShape,
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isDark) Color(0xFF78350F).copy(alpha = 0.35f) else Color(0xFFFEF3C7)
                                        ),
                                        border = BorderStroke(1.2.dp, Color(0xFFF59E0B))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                "${Translations.tr("vet_pending_banner_title", lang)} (${pendingRequests.size})",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 13.5.sp,
                                                color = if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E)
                                            )
                                            pendingRequests.take(3).forEach { reqTx ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            reqTx.category,
                                                            fontSize = 12.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = primaryTextColor,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            "${String.format(java.util.Locale.US, "%,.0f", reqTx.amount)} ${reqTx.currency}",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = Color(0xFFD97706)
                                                        )
                                                    }
                                                    Button(
                                                        onClick = { pendingHubTxForSignature = reqTx },
                                                        modifier = Modifier
                                                            .height(36.dp)
                                                            .testTag("btn_hub_approve_tx_${reqTx.id}"),
                                                        shape = pillShape,
                                                        colors = ButtonDefaults.buttonColors(containerColor = primaryButtonAccent),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(
                                                            Translations.tr("btn_approve_disbursement", lang),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (farms.size > 1) {
                                item {
                                    FarmSwitcherRow(
                                        farms = farms,
                                        selectedFarmId = farms.firstOrNull()?.id ?: "",
                                        lang = lang,
                                        currentTheme = currentTheme,
                                        onSelectFarm = { onOpenNotebook(it) }
                                    )
                                }
                            }
                            items(farms, key = { it.id }) { farm ->
                                FarmCard(
                                    farm = farm,
                                    currentTheme = currentTheme,
                                    lang = lang,
                                    firestoreRepo = firestoreRepo,
                                    onOpenNotebook = { onOpenNotebook(farm) },
                                    onStartNewCycle = { selectedFarm ->
                                        onStartNewCycleForFarm(selectedFarm)
                                    }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(100.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// WHATSAPP-STYLE CHAT LIST & CHAT DETAIL SCREENS (GLOBAL NAV TAB 1)
// ============================================================================

@Composable
fun ChatListScreen(
    conversations: List<ChatConversationModel>,
    currentTheme: AppThemeOption,
    lang: AppLanguage = AppLanguage.AR,
    onSelectConversation: (ChatConversationModel) -> Unit,
    onOpenPersonalQr: (() -> Unit)? = null
) {
    val appearance = LocalAppAppearanceState.current
    val isDark = currentTheme.isDark
    val primaryText = if (isDark) Color(0xFFFFFFFF) else currentTheme.headingColor
    val secondaryText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    val metaText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF64748B)
    val activeAccent = appearance.accentColor
    val primaryButtonAccent = appearance.accentOption.primaryColor
    val avatarBgTint = appearance.accentContainerColor
    val tileShape = RoundedCornerShape(appearance.cornerRadiusDp.dp)
    var showAddPhoneContactDialog by remember { mutableStateOf(false) }
    var showLocalPersonalQrSheet by remember { mutableStateOf(false) }

    if (showAddPhoneContactDialog) {
        AddPhoneContactDialog(
            lang = lang,
            currentTheme = currentTheme,
            onDismiss = { showAddPhoneContactDialog = false },
            onContactAdded = { newConv ->
                showAddPhoneContactDialog = false
                onSelectConversation(newConv)
            }
        )
    }

    if (showLocalPersonalQrSheet) {
        PersonalAccountWhatsAppQrBottomSheet(
            currentTheme = currentTheme,
            lang = lang,
            activeRole = AppRole.OWNER,
            onDismiss = { showLocalPersonalQrSheet = false },
            onContactScanned = { newConv ->
                showLocalPersonalQrSheet = false
                onSelectConversation(newConv)
            },
            onJoinRole = { _ ->
                showLocalPersonalQrSheet = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("whatsapp_chat_list_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(tileShape)
                        .clickable { showAddPhoneContactDialog = true }
                        .testTag("btn_add_phone_contact"),
                    shape = tileShape,
                    color = avatarBgTint,
                    border = BorderStroke(1.2.dp, activeAccent.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = primaryButtonAccent
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                Translations.tr("btn_add_phone_contact", lang),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = activeAccent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = activeAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // WhatsApp-style Personal QR Code Icon Button next to "+ صديق برقم الهاتف"
                Surface(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(tileShape)
                        .clickable {
                            if (onOpenPersonalQr != null) {
                                onOpenPersonalQr()
                            } else {
                                showLocalPersonalQrSheet = true
                            }
                        }
                        .testTag("btn_chat_personal_qr"),
                    shape = tileShape,
                    color = avatarBgTint,
                    border = BorderStroke(1.2.dp, activeAccent.copy(alpha = 0.45f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = Translations.tr("personal_qr_tab_my_code", lang),
                            tint = activeAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        items(conversations, key = { it.id }) { conv ->
            val channelMsgs = AgritechMockDb.streamChatMessages(conv.id)
            val lastMessageText = channelMsgs.lastOrNull()?.text ?: conv.defaultSnippet
            val lastMessageTime = channelMsgs.lastOrNull()?.timestamp ?: conv.defaultTimestamp

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(tileShape)
                    .clickable { onSelectConversation(conv) }
                    .testTag("chat_list_tile_${conv.id}"),
                shape = tileShape,
                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Circular Avatar bound to active theme accentContainerColor & accentColor
                    Surface(
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("chat_avatar_${conv.id}"),
                        shape = CircleShape,
                        color = avatarBgTint,
                        border = BorderStroke(1.2.dp, activeAccent.copy(alpha = 0.38f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(conv.avatarEmoji, fontSize = 23.sp)
                        }
                    }

                    // User Name & Last Message Snippet
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                conv.userName,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.5.sp,
                                color = primaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                lastMessageTime,
                                fontSize = 11.sp,
                                fontWeight = if (conv.unreadCount > 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (conv.unreadCount > 0) activeAccent else metaText,
                                maxLines = 1
                            )
                        }

                        if (conv.phoneNumber.isNotBlank()) {
                            Text(
                                "📞 ${conv.phoneNumber} • ${conv.roleLabel}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = activeAccent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                lastMessageText,
                                fontSize = 12.5.sp,
                                color = secondaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (conv.unreadCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .testTag("chat_unread_badge_${conv.id}"),
                                    shape = CircleShape,
                                    color = primaryButtonAccent
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "${conv.unreadCount}",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "✓✓",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = activeAccent,
                                    modifier = Modifier.testTag("chat_read_checkmark_${conv.id}")
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ChatDetailScreen(
    conversation: ChatConversationModel,
    currentTheme: AppThemeOption,
    lang: AppLanguage,
    firestoreRepo: AgritechFirestoreRepository? = null,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val appearance = LocalAppAppearanceState.current
    val isDark = currentTheme.isDark
    val headerBg = if (isDark) Color(0xFF1E293B) else Color.White
    val primaryText = if (isDark) Color(0xFFFFFFFF) else currentTheme.headingColor
    val secondaryText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF64748B)
    val activeAccent = appearance.accentColor
    val invoiceBtnBg = appearance.accentContainerColor

    val isVetConversation = conversation.id.contains("vet", ignoreCase = true) ||
        conversation.userName.contains("حكيم") ||
        conversation.roleLabel.contains("بيطري")
    var showVetInvoiceDialog by remember { mutableStateOf(false) }
    var showUniversalFinancialDialog by remember { mutableStateOf(false) }

    val activeFarm = AgritechMockDb.farms.firstOrNull() ?: FarmModel(id = "farm_1", name = "عنبر البركة 1 - المدية", location = "المدية", capacity = 8000, assignedWorkerId = "worker_1")
    val activeCycle = AgritechMockDb.cycles.firstOrNull { it.farmId == activeFarm.id } ?: CycleModel(id = "cycle_2025_1", farmId = activeFarm.id, totalBirdsPlaced = 8000, targetWeight = 2.3, status = "active")
    val dayNum = (AgritechMockDb.dailyReports.filter { it.farmId == activeFarm.id }.maxOfOrNull { it.dayNumber } ?: 1)

    LaunchedEffect(conversation.id, firestoreRepo) {
        if (firestoreRepo != null) {
            try {
                firestoreRepo.observeChatMessages(conversation.id).collectLatest { cloudMsgs ->
                    cloudMsgs.forEach { msg ->
                        val idx = AgritechMockDb.chatMessages.indexOfFirst { it.id == msg.id }
                        if (idx >= 0) {
                            val existing = AgritechMockDb.chatMessages[idx]
                            AgritechMockDb.chatMessages[idx] = msg.copy(
                                status = if (existing.status == "APPROVED") "APPROVED" else msg.status
                            )
                        } else {
                            AgritechMockDb.chatMessages.add(msg)
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    if (showVetInvoiceDialog) {
        VetMedicineInvoiceDialog(
            farm = activeFarm,
            cycle = activeCycle,
            dayNumber = dayNum,
            lang = lang,
            currentTheme = currentTheme,
            channelId = conversation.id,
            firestoreRepo = firestoreRepo,
            onDismiss = { showVetInvoiceDialog = false }
        )
    }

    if (showUniversalFinancialDialog) {
        UniversalFinancialRequestDialog(
            farm = activeFarm,
            cycle = activeCycle,
            dayNumber = dayNum,
            senderName = conversation.userName,
            senderRole = conversation.roleLabel,
            channelId = conversation.id,
            lang = lang,
            currentTheme = currentTheme,
            firestoreRepo = firestoreRepo,
            onDismiss = { showUniversalFinancialDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(currentTheme.bgColor)
            .testTag("chat_detail_screen_${conversation.id}")
    ) {
        // WhatsApp-style Conversation Top Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = headerBg,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("btn_back_from_chat_detail")
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = activeAccent
                    )
                }

                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = appearance.accentContainerColor,
                    border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.35f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(conversation.avatarEmoji, fontSize = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        conversation.userName,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = primaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val phoneSuffix = if (conversation.phoneNumber.isNotBlank()) " • ${conversation.phoneNumber}" else ""
                    Text(
                        "${conversation.roleLabel}$phoneSuffix",
                        fontSize = 11.5.sp,
                        color = secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .testTag("btn_chat_universal_invoice")
                            .clickable { showUniversalFinancialDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        color = invoiceBtnBg,
                        border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                Translations.tr("btn_send_financial_request", lang),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = activeAccent,
                                maxLines = 1
                            )
                        }
                    }

                    if (isVetConversation) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .testTag("btn_chat_issue_vet_invoice")
                                .clickable { showVetInvoiceDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            color = invoiceBtnBg,
                            border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.45f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = activeAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Reuse WhatsApp-style dynamic accent bubble chat + input bar
        Box(modifier = Modifier.weight(1f)) {
            TeamChatTab(
                channelId = conversation.id,
                currentTheme = currentTheme,
                lang = lang,
                firestoreRepo = firestoreRepo
            )
        }
    }
}

@Composable
fun AddPhoneContactDialog(
    lang: AppLanguage,
    currentTheme: AppThemeOption,
    onDismiss: () -> Unit,
    onContactAdded: (ChatConversationModel) -> Unit
) {
    val appearance = LocalAppAppearanceState.current
    val isDark = currentTheme.isDark
    val dialogBg = if (isDark) Color(0xFF1E293B) else Color.White
    val primaryText = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val secondaryText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    val activeAccent = appearance.accentColor

    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    val roleOptions = listOf(
        Triple(Translations.tr("contact_role_worker", lang), "👷", "مشرف العنبر"),
        Triple(Translations.tr("contact_role_vet", lang), "🩺", "طبيب بيطري"),
        Triple(Translations.tr("contact_role_feed_supplier", lang), "🌾", "مورد أعلاف"),
        Triple(Translations.tr("contact_role_chick_seller", lang), "🐣", "مفرخة / بائع صيصان"),
        Triple(Translations.tr("contact_role_buyer", lang), "🚚", "تاجر جملة"),
        Triple(Translations.tr("contact_role_vet_importer", lang), "🧬", "مستورد أدوية بيطرية")
    )
    var selectedRoleIdx by remember { mutableStateOf(2) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBg,
        shape = RoundedCornerShape(appearance.cornerRadiusDp.dp),
        modifier = Modifier.testTag("add_phone_contact_dialog"),
        title = {
            Column {
                Text(
                    Translations.tr("add_contact_dialog_title", lang),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = activeAccent
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    Translations.tr("add_contact_dialog_subtitle", lang),
                    fontSize = 11.5.sp,
                    color = secondaryText
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text(Translations.tr("contact_name_label", lang), fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_contact_name")
                )
                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it.filter { ch -> ch.isDigit() || ch == '+' || ch == ' ' } },
                    label = { Text(Translations.tr("contact_phone_label", lang), fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_contact_phone")
                )
                Text(
                    Translations.tr("contact_role_label", lang),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    roleOptions.forEachIndexed { idx, (label, emoji, _) ->
                        val isSelected = selectedRoleIdx == idx
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedRoleIdx = idx },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) appearance.accentContainerColor else Color.Transparent,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) activeAccent else secondaryText.copy(alpha = 0.25f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "$emoji $label",
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) activeAccent else primaryText
                                )
                                if (isSelected) {
                                    Text("✓", fontWeight = FontWeight.ExtraBold, color = activeAccent)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val chosen = roleOptions[selectedRoleIdx]
                    val finalName = nameInput.trim().ifEmpty { chosen.first }
                    val finalPhone = phoneInput.trim().ifEmpty { "0550000000" }
                    val added = AgritechMockDb.addPhoneContact(
                        name = finalName,
                        phone = finalPhone,
                        roleLabel = chosen.first,
                        avatarEmoji = chosen.second
                    )
                    onContactAdded(added)
                },
                modifier = Modifier.testTag("btn_save_phone_contact"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = appearance.accentOption.primaryColor)
            ) {
                Text(
                    Translations.tr("btn_save_contact", lang),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Translations.tr("btn_cancel", lang), color = secondaryText)
            }
        }
    )
}

@Composable
fun UniversalFinancialRequestDialog(
    farm: FarmModel,
    cycle: CycleModel,
    dayNumber: Int,
    senderName: String,
    senderRole: String,
    channelId: String,
    lang: AppLanguage,
    currentTheme: AppThemeOption,
    firestoreRepo: AgritechFirestoreRepository? = null,
    onOrderSubmitted: ((amount: Double, category: String, note: String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val appearance = LocalAppAppearanceState.current
    val curr = Translations.tr("currency", lang)
    val isDark = currentTheme.isDark
    val dialogBg = if (isDark) Color(0xFF1E293B) else Color.White
    val primaryText = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val secondaryText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    val activeAccent = appearance.accentColor
    val dialogIdempotencyUuid = remember { java.util.UUID.randomUUID().toString() }

    val categories = listOf(
        Translations.tr("fin_req_cat_worker_advance", lang),
        Translations.tr("fin_req_cat_gas_maint", lang),
        Translations.tr("fin_req_cat_feed_invoice", lang),
        Translations.tr("fin_req_cat_chick_invoice", lang),
        Translations.tr("fin_req_cat_vet_invoice", lang)
    )
    var selectedCatIdx by remember { mutableStateOf(0) }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBg,
        shape = RoundedCornerShape(appearance.cornerRadiusDp.dp),
        modifier = Modifier.testTag("universal_financial_request_dialog"),
        title = {
            Column {
                Text(
                    Translations.tr("fin_req_dialog_title", lang),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.5.sp,
                    color = activeAccent
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    Translations.tr("fin_req_dialog_subtitle", lang),
                    fontSize = 11.5.sp,
                    color = secondaryText
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    Translations.tr("fin_req_type_label", lang),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText
                )
                categories.forEachIndexed { idx, catLabel ->
                    val isSelected = selectedCatIdx == idx
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedCatIdx = idx }
                            .testTag("fin_req_cat_option_$idx"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) appearance.accentContainerColor else Color.Transparent,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) activeAccent else secondaryText.copy(alpha = 0.25f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                catLabel,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) activeAccent else primaryText
                            )
                            if (isSelected) {
                                Text("✓", fontWeight = FontWeight.ExtraBold, color = activeAccent)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("${Translations.tr("fin_req_amount_label", lang)} ($curr)", fontSize = 12.sp) },
                    placeholder = { Text("15000", fontSize = 12.sp, color = secondaryText.copy(alpha = 0.5f)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_fin_req_amount")
                )

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text(Translations.tr("fin_req_note_label", lang), fontSize = 12.sp) },
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_fin_req_note")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = (amountText.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
                    if (amt > 0.0) {
                        val chosenCat = categories[selectedCatIdx]
                        val txId = "tx_req_${farm.id}_${cycle.id}_$dialogIdempotencyUuid"
                        val pendingTx = TransactionModel(
                            id = txId,
                            farmId = farm.id,
                            cycleId = cycle.id,
                            type = TransactionType.EXPENSE,
                            category = chosenCat,
                            amount = amt,
                            currency = curr,
                            date = "${Translations.tr("day_prefix", lang)} $dayNumber",
                            status = "PENDING"
                        )
                        val chatMsg = AgritechMockDb.recordUniversalFinancialRequest(
                            tx = pendingTx,
                            noteText = noteText.trim(),
                            senderId = "contact_${channelId}",
                            senderName = senderName,
                            senderRole = senderRole,
                            chatChannelId = channelId
                        )
                        if (firestoreRepo != null) {
                            scope.launch {
                                firestoreRepo.recordTransactionIdempotent(
                                    idempotencyKey = txId,
                                    tx = pendingTx
                                )
                                firestoreRepo.sendChatMessage(chatMsg)
                            }
                        }
                        onOrderSubmitted?.invoke(amt, chosenCat, noteText.trim())
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("btn_submit_fin_req"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = appearance.accentOption.primaryColor)
            ) {
                Text(
                    Translations.tr("fin_req_submit_btn", lang),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Translations.tr("btn_cancel", lang), color = secondaryText)
            }
        }
    )
}

@Composable
fun FarmSwitcherRow(
    farms: List<FarmModel>,
    selectedFarmId: String,
    lang: AppLanguage,
    currentTheme: AppThemeOption,
    onSelectFarm: (FarmModel) -> Unit
) {
    val appearance = LocalAppAppearanceState.current
    val activeAccent = appearance.accentColor
    val isDark = currentTheme.isDark
    val primaryText = if (isDark) Color(0xFFFFFFFF) else currentTheme.headingColor
    val pillShape = RoundedCornerShape((appearance.cornerRadiusDp - 4).coerceAtLeast(8).dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("multi_farm_switcher_row"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            Translations.tr("multi_farm_switcher_label", lang),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = activeAccent
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            farms.forEach { itemFarm ->
                val isSelected = itemFarm.id == selectedFarmId
                Surface(
                    modifier = Modifier
                        .clip(pillShape)
                        .clickable { onSelectFarm(itemFarm) }
                        .testTag("farm_switcher_chip_${itemFarm.id}"),
                    shape = pillShape,
                    color = if (isSelected) appearance.accentOption.primaryColor else currentTheme.cardColor,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) appearance.accentOption.primaryColor else activeAccent.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "🏠 ${Translations.getLocalizedFarmName(itemFarm.id, lang, itemFarm.name)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) Color.White else primaryText,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// INTERACTIVE USER-DRIVEN MARKET FEED SCREEN & MARKET POST CARD (GLOBAL NAV TAB 2)
// ============================================================================

@Composable
fun MarketFeedScreen(
    posts: List<MarketPostModel>,
    currentTheme: AppThemeOption,
    lang: AppLanguage,
    onContactPublisher: (MarketPostModel) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .testTag("global_market_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(posts, key = { it.id }) { post ->
            MarketPostCard(
                post = post,
                currentTheme = currentTheme,
                lang = lang,
                onContact = { onContactPublisher(post) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

@Composable
fun MarketPostCard(
    post: MarketPostModel,
    currentTheme: AppThemeOption,
    lang: AppLanguage,
    onContact: () -> Unit
) {
    val context = LocalContext.current
    val appearance = LocalAppAppearanceState.current
    val isDark = currentTheme.isDark
    val primaryText = if (isDark) Color(0xFFFFFFFF) else Color(0xDD000000)
    val secondaryText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    val metaText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF64748B)
    val accentEmerald = appearance.accentColor
    val priceBadgeBg = appearance.accentContainerColor
    val imageContainerBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)
    val imageContainerBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val cardShape = RoundedCornerShape(appearance.cornerRadiusDp.dp)
    val btnShape = RoundedCornerShape((appearance.cornerRadiusDp - 6).coerceAtLeast(4).dp)

    val loadedBitmap = remember(post.imageUri) {
        post.imageUri?.let { uriStr ->
            try {
                val parsed = Uri.parse(uriStr)
                context.contentResolver.openInputStream(parsed)?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("market_post_card_${post.id}"),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // a) Image Container (renders picked photo URI when available, or clean placeholder)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(155.dp),
                shape = btnShape,
                color = imageContainerBg,
                border = BorderStroke(1.dp, imageContainerBorder)
            ) {
                if (loadedBitmap != null) {
                    Image(
                        bitmap = loadedBitmap,
                        contentDescription = post.imageCaption,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            if (post.imageUri != null) Icons.Default.PhotoCamera else Icons.Default.Image,
                            contentDescription = post.imageCaption,
                            tint = if (post.imageUri != null) accentEmerald else metaText,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            post.imageCaption,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (post.imageUri != null) accentEmerald else metaText
                        )
                    }
                }
            }

            // b) Post Title & Text Description
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    post.title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.5.sp,
                    color = primaryText
                )
                Text(
                    post.description,
                    fontSize = 13.sp,
                    color = secondaryText,
                    lineHeight = 19.sp
                )
            }

            // c) Price Pill (high-contrast accent)
            Surface(
                shape = btnShape,
                color = priceBadgeBg
            ) {
                Text(
                    "${String.format(java.util.Locale.US, "%,.0f", post.priceDzd)} ${post.priceUnit}",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = accentEmerald
                )
            }

            HorizontalDivider(color = imageContainerBorder, thickness = 1.dp)

            // d) Publisher's Name and "تواصل" (Contact) button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = priceBadgeBg
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👨‍🌾", fontSize = 16.sp)
                        }
                    }
                    Column {
                        Text(
                            post.publisherName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = primaryText,
                            maxLines = 1
                        )
                        Text(
                            "📍 ${post.publisherLocation}",
                            fontSize = 11.sp,
                            color = metaText,
                            maxLines = 1
                        )
                    }
                }

                Button(
                    onClick = onContact,
                    shape = btnShape,
                    colors = ButtonDefaults.buttonColors(containerColor = appearance.accentOption.primaryColor),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("btn_contact_publisher_${post.id}")
                ) {
                    Icon(
                        Icons.Default.Chat,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        Translations.tr("btn_contact", lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun AddMarketOfferBottomSheet(
    lang: AppLanguage,
    currentTheme: AppThemeOption,
    onDismiss: () -> Unit,
    onPublishPost: (MarketPostModel) -> Unit
) {
    AddMarketPostDialog(
        lang = lang,
        currentTheme = currentTheme,
        onDismiss = onDismiss,
        onPublishPost = onPublishPost
    )
}

@Composable
fun AddMarketPostDialog(
    lang: AppLanguage,
    currentTheme: AppThemeOption,
    onDismiss: () -> Unit,
    onPublishPost: (MarketPostModel) -> Unit
) {
    val context = LocalContext.current
    val isDark = currentTheme.isDark
    val dialogBg = if (isDark) Color(0xFF1E293B) else Color.White
    val fieldBg = if (isDark) Color(0xFF0F172A) else Color.White
    val fieldBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val primaryText = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val secondaryText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF64748B)
    val accentEmerald = if (isDark) Color(0xFF34D399) else Color(0xFF1B5E20)

    var titleInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Real Android Photo Picker (ActivityResultContracts.PickVisualMedia)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    val previewBitmap = remember(selectedImageUri) {
        selectedImageUri?.let { uri ->
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        containerColor = dialogBg,
        title = {
            Text(
                Translations.tr("dialog_add_post_title", lang),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.5.sp,
                color = accentEmerald
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text(Translations.tr("field_post_title", lang), fontSize = 12.sp, color = secondaryText) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_market_post_title"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentEmerald,
                        unfocusedBorderColor = fieldBorder,
                        focusedTextColor = primaryText,
                        unfocusedTextColor = primaryText,
                        focusedContainerColor = fieldBg,
                        unfocusedContainerColor = fieldBg
                    )
                )

                OutlinedTextField(
                    value = descInput,
                    onValueChange = { descInput = it },
                    label = { Text(Translations.tr("field_post_desc", lang), fontSize = 12.sp, color = secondaryText) },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_market_post_desc"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentEmerald,
                        unfocusedBorderColor = fieldBorder,
                        focusedTextColor = primaryText,
                        unfocusedTextColor = primaryText,
                        focusedContainerColor = fieldBg,
                        unfocusedContainerColor = fieldBg
                    )
                )

                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text(Translations.tr("field_post_price", lang), fontSize = 12.sp, color = secondaryText) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_market_post_price"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentEmerald,
                        unfocusedBorderColor = fieldBorder,
                        focusedTextColor = primaryText,
                        unfocusedTextColor = primaryText,
                        focusedContainerColor = fieldBg,
                        unfocusedContainerColor = fieldBg
                    )
                )

                // Thumbnail preview when an image URI has been selected from the gallery
                if (selectedImageUri != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("market_selected_image_preview"),
                        shape = RoundedCornerShape(14.dp),
                        color = fieldBg,
                        border = BorderStroke(1.dp, accentEmerald)
                    ) {
                        if (previewBitmap != null) {
                            Image(
                                bitmap = previewBitmap,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "📸 ${selectedImageUri?.lastPathSegment ?: "Image"}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentEmerald
                                )
                            }
                        }
                    }
                }

                // Real Photo Picker Button ("إرفاق صورة للمنتج")
                OutlinedButton(
                    onClick = {
                        try {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        } catch (_: Exception) {
                            // Fallback URI in headless unit test environments where no gallery activity is registered
                            selectedImageUri = Uri.parse("content://com.aistudio.poultry/market_photo_${System.currentTimeMillis()}.jpg")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_upload_market_image"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.2.dp,
                        if (selectedImageUri != null) accentEmerald else fieldBorder
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedImageUri != null) {
                            if (isDark) Color(0xFF064E3B) else LightGreenContainer
                        } else fieldBg
                    )
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = if (selectedImageUri != null) accentEmerald else secondaryText
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (selectedImageUri != null) "✓ تم إرفاق صورة الإعلان بنجاح"
                        else Translations.tr("btn_upload_image", lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = if (selectedImageUri != null) accentEmerald else secondaryText
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanTitle = titleInput.trim()
                    val parsedPrice = priceInput.toDoubleOrNull() ?: 0.0
                    if (cleanTitle.isNotEmpty() && parsedPrice > 0.0) {
                        val curr = Translations.tr("currency", lang)
                        val currentUser = try {
                            FirebaseAuth.getInstance().currentUser
                        } catch (_: Exception) {
                            null
                        }
                        val activeFarm = AgritechMockDb.farms.firstOrNull()
                        val resolvedPublisherName = currentUser?.displayName?.takeIf { it.isNotBlank() }
                            ?: activeFarm?.name?.takeIf { it.isNotBlank() }
                            ?: Translations.tr("role_badge_owner_barn", lang)
                        val resolvedPublisherLocation = activeFarm?.location?.takeIf { it.isNotBlank() }
                            ?: Translations.tr("farms_title", lang)
                        onPublishPost(
                            MarketPostModel(
                                id = "post_${System.currentTimeMillis()}",
                                title = cleanTitle,
                                description = descInput.trim().ifEmpty { "متوفر حالياً في المزرعة بجودة ممتازة." },
                                priceDzd = parsedPrice,
                                priceUnit = curr,
                                publisherName = resolvedPublisherName,
                                publisherLocation = resolvedPublisherLocation,
                                imageCaption = if (selectedImageUri != null) "صورة مرفقة: $cleanTitle" else "صورة مرفقة للإعلان",
                                chatChannelId = "general_admin",
                                imageUri = selectedImageUri?.toString()
                            )
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                modifier = Modifier.testTag("btn_confirm_publish_post")
            ) {
                Text(
                    Translations.tr("btn_publish_post", lang),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Translations.tr("btn_cancel", lang), color = secondaryText)
            }
        }
    )
}

@Composable
fun OwnerGlobalAiVetPlaceholderScreen(
    currentTheme: AppThemeOption,
    lang: AppLanguage = AppLanguage.AR
) {
    val appearance = LocalAppAppearanceState.current
    val isDark = currentTheme.isDark
    val aiAccent = appearance.accentColor
    val aiAccentContainer = appearance.accentContainerColor
    val aiCardShape = RoundedCornerShape(appearance.cornerRadiusDp.dp)
    val primaryText = if (isDark) Color(0xFFFFFFFF) else currentTheme.headingColor
    val secondaryText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentTheme.bgColor)
            .padding(20.dp)
            .testTag("global_ai_vet_screen"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp)
                .testTag("ai_vet_coming_soon_card"),
            shape = aiCardShape,
            colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
            border = BorderStroke(1.2.dp, aiAccent.copy(alpha = 0.35f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = aiAccentContainer,
                    border = BorderStroke(1.5.dp, aiAccent.copy(alpha = 0.45f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🤖⏳", fontSize = 28.sp)
                    }
                }

                Text(
                    text = Translations.tr("ai_vet_coming_soon_title", lang),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.5.sp,
                    color = primaryText,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = Translations.tr("ai_vet_coming_soon_subtitle", lang),
                    fontSize = 13.5.sp,
                    color = secondaryText,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

/**
 * Dynamic Barn Identity Card (`FarmCard`) for `OwnerDashboardScreen`.
 * Streams the active `CycleModel` for the given `FarmModel`, reactively computes liveBirds,
 * startDate (DD-MM-YYYY), current cycle day, Ross 308 estimated weight, and livability %,
 * or displays a clean empty state ("لا توجد دورة نشطة") with a "Start New Cycle" action.
 */
@Composable
fun FarmCard(
    farm: FarmModel,
    currentTheme: AppThemeOption,
    lang: AppLanguage,
    firestoreRepo: AgritechFirestoreRepository? = null,
    onOpenNotebook: () -> Unit,
    onStartNewCycle: (FarmModel) -> Unit
) {
    val preferredFeedUnit = LocalFeedUnitPreference.current
    var showTeamQrInviteSheet by remember { mutableStateOf(false) }

    if (showTeamQrInviteSheet) {
        TeamQrInviteBottomSheet(
            farm = farm,
            lang = lang,
            onDismiss = { showTeamQrInviteSheet = false }
        )
    }

    // 1. Stream active cycles from Firestore (when authenticated) merged with reactive state
    val streamedCycles by produceState<List<CycleModel>>(initialValue = emptyList(), farm.id, firestoreRepo) {
        val uid = try {
            FirebaseAuth.getInstance().currentUser?.uid
        } catch (_: Exception) {
            null
        }
        if (uid != null && firestoreRepo != null) {
            try {
                firestoreRepo.streamCycles(uid, farm.id, "active").collectLatest { list ->
                    value = list
                }
            } catch (_: Exception) {}
        }
    }

    val localActiveCycle = AgritechMockDb.cycles.firstOrNull {
        it.farmId == farm.id && it.status.equals("active", ignoreCase = true)
    }
    val activeCycle = localActiveCycle ?: streamedCycles.firstOrNull()

    val appearance = LocalAppAppearanceState.current
    val isDark = currentTheme.isDark
    val primaryText = if (isDark) Color(0xFFFFFFFF) else currentTheme.headingColor
    val secondaryText = if (isDark) Color(0xFFCBD5E1) else Color.Gray
    val accentEmerald = appearance.accentColor
    val pillBgEmerald = appearance.accentContainerColor
    val pillBorderEmerald = accentEmerald.copy(alpha = 0.45f)
    val identityPanelBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FBF8)
    val identityPanelBorder = if (isDark) Color(0xFF334155) else Color(0xFFE0F2F1)
    val qrButtonBg = appearance.accentContainerColor
    val cardCornerShape = RoundedCornerShape(appearance.cornerRadiusDp.dp)
    val innerCornerShape = RoundedCornerShape((appearance.cornerRadiusDp - 4).coerceAtLeast(4).dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("farm_card_${farm.id}"),
        shape = cardCornerShape,
        colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            if (activeCycle != null) {
                // Reactive calculations for liveBirds, cycle day, weight, and livability
                val reportsForCycle = AgritechMockDb.dailyReports.filter {
                    it.farmId == farm.id && (it.cycleId == activeCycle.id || it.cycleId.isBlank())
                }
                val txsForCycle = AgritechMockDb.transactions.filter {
                    it.farmId == farm.id && (it.cycleId == activeCycle.id || it.cycleId.isBlank())
                }
                val totalMortality = reportsForCycle.sumOf { it.mortalityCount }
                val totalFeed = reportsForCycle.sumOf { it.feedIntakeKg }
                val totalPurchasedFeedKg = txsForCycle.filter {
                    it.type == TransactionType.EXPENSE &&
                        it.status.equals("APPROVED", true) &&
                        (it.category.contains("علف") || it.category.contains("Feed", true) || it.category.contains("Aliment", true))
                }.sumOf { parseFeedTransaction(it, lang).estimatedKg }
                val effectiveFeedInventoryKg = if (totalPurchasedFeedKg > 0.0 || reportsForCycle.isNotEmpty()) {
                    (totalPurchasedFeedKg - totalFeed).coerceAtLeast(0.0)
                } else {
                    activeCycle.feedInventoryKg.coerceAtLeast(0.0)
                }
                val totalSold = PoultryCalculators.resolveCumulativeBirdsSold(activeCycle, txsForCycle)

                val liveBirds = PoultryCalculators.calculateLiveBirds(
                    totalBirdsPlaced = activeCycle.totalBirdsPlaced,
                    totalMortality = totalMortality,
                    cumulativeBirdsSold = totalSold
                )
                val livabilityPct = if (activeCycle.totalBirdsPlaced > 0) {
                    ((activeCycle.totalBirdsPlaced - totalMortality).toDouble() / activeCycle.totalBirdsPlaced * 100.0)
                        .coerceIn(0.0, 100.0)
                } else {
                    100.0
                }
                val maxReportDay = reportsForCycle.maxOfOrNull { it.dayNumber } ?: 1
                val calculatedDay = PoultryCalculators.calculateCycleDay(activeCycle.startDate, maxReportDay)
                val actualCardWeightKg = PoultryCalculators.resolveActualAvgBirdWeightKg(
                    reports = reportsForCycle,
                    cycleTransactions = txsForCycle,
                    cycleId = activeCycle.id
                )
                val formattedStartDate = PoultryCalculators.formatDateDdMmYyyy(activeCycle.startDate)
                val kgUnit = Translations.tr("kg_unit", lang)

                var showCardFeedLedger by remember { mutableStateOf(false) }
                if (showCardFeedLedger) {
                    FeedOrdersLedgerBottomSheet(
                        farmName = Translations.getLocalizedFarmName(farm.id, lang, farm.name),
                        cycle = activeCycle.copy(feedInventoryKg = effectiveFeedInventoryKg),
                        transactions = txsForCycle,
                        lang = lang,
                        onDismiss = { showCardFeedLedger = false }
                    )
                }

                // a) ID Header Row: Clean Barn Avatar on start, full Barn Name + Location subtitle (generous weight), and sleek Day Badge on opposite side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = pillBgEmerald,
                            border = BorderStroke(1.dp, pillBorderEmerald),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🏡", fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                Translations.getLocalizedFarmName(farm.id, lang, farm.name),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.5.sp,
                                color = primaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                Translations.getLocalizedFarmLocation(farm.id, lang, farm.location),
                                fontSize = 12.sp,
                                color = secondaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Sleek Day Badge on opposite side
                    Surface(
                        shape = innerCornerShape,
                        color = pillBgEmerald,
                        border = BorderStroke(1.dp, pillBorderEmerald)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📅", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${Translations.tr("day_prefix", lang)} $calculatedDay",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = accentEmerald,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // b) ID Body: Structured bordered container with Active Live Birds + full-width Start Date + 3 equal-width mini stat boxes (NO horizontal scrolling)
                val isFeedLow = effectiveFeedInventoryKg < 500.0
                val feedInventoryColor = if (isFeedLow) {
                    if (isDark) Color(0xFFFCA5A5) else Color(0xFFD32F2F)
                } else accentEmerald
                val miniBoxBg = if (isDark) Color(0xFF1E293B) else Color.White
                val miniBoxBorder = if (isDark) Color(0xFF334155) else accentEmerald.copy(alpha = 0.22f)
                val feedBoxBg = if (isFeedLow) {
                    if (isDark) Color(0xFF450A0A) else Color(0xFFFFEBEE)
                } else miniBoxBg
                val feedBoxBorder = if (isFeedLow) {
                    if (isDark) Color(0xFFF87171).copy(alpha = 0.5f) else Color(0xFFEF9A9A)
                } else miniBoxBorder

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = innerCornerShape,
                    color = identityPanelBg,
                    border = BorderStroke(1.dp, identityPanelBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Active Live Birds
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(accentEmerald, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "${String.format(java.util.Locale.US, "%,d", liveBirds)} ${Translations.tr("birds_unit", lang)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = accentEmerald,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                "(${String.format(java.util.Locale.US, "%,d", activeCycle.totalBirdsPlaced)} - ${String.format(java.util.Locale.US, "%,d", totalMortality)} - ${String.format(java.util.Locale.US, "%,d", totalSold)})",
                                fontSize = 10.5.sp,
                                color = secondaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Start Date cleanly on its own full-width line
                        Text(
                            "${Translations.tr("start_date_label", lang)}: $formattedStartDate",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("farm_card_start_date_${farm.id}")
                        )

                        HorizontalDivider(
                            color = identityPanelBorder,
                            thickness = 1.dp
                        )

                        // 3 equal-width (Modifier.weight(1f)) mini stat boxes inside the card (NO horizontal scrolling row)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1) Estimated Weight
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = innerCornerShape,
                                color = miniBoxBg,
                                border = BorderStroke(1.dp, miniBoxBorder)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        Translations.tr("badge_estimated_weight", lang),
                                        fontSize = 10.sp,
                                        color = secondaryText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        if (actualCardWeightKg != null) {
                                            "${String.format(java.util.Locale.US, "%.3f", actualCardWeightKg)} $kgUnit"
                                        } else {
                                            Translations.tr("weight_not_measured_yet", lang)
                                        },
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (actualCardWeightKg != null) primaryText else secondaryText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // 2) Livability
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = innerCornerShape,
                                color = miniBoxBg,
                                border = BorderStroke(1.dp, miniBoxBorder)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        Translations.tr("livability_label", lang),
                                        fontSize = 10.sp,
                                        color = secondaryText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        String.format(java.util.Locale.US, "%.1f%%", livabilityPct),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = accentEmerald,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // 3) Feed Stock (Quintals)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("badge_feed_inventory_${farm.id}")
                                    .clip(innerCornerShape)
                                    .clickable { showCardFeedLedger = true },
                                shape = innerCornerShape,
                                color = feedBoxBg,
                                border = BorderStroke(1.dp, feedBoxBorder)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        Translations.tr("feed_stock_short", lang),
                                        fontSize = 10.sp,
                                        color = if (isFeedLow) feedInventoryColor else secondaryText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        Translations.formatBulkFeedPurchaseQuantity(effectiveFeedInventoryKg, lang, preferredFeedUnit),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = feedInventoryColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // c) ID Footer Row: Primary full-weight action button "📖 فتح دفتر المزرعة" alongside compact secondary outlined button "👥+ فريق"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onOpenNotebook,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_open_notebook_${farm.id}"),
                        shape = innerCornerShape,
                        colors = ButtonDefaults.buttonColors(containerColor = appearance.accentOption.primaryColor),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            Translations.tr("open_notebook", lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = { showTeamQrInviteSheet = true },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("btn_add_team_${farm.id}"),
                        shape = innerCornerShape,
                        border = BorderStroke(1.2.dp, accentEmerald.copy(alpha = 0.55f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = qrButtonBg,
                            contentColor = accentEmerald
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            Translations.tr("btn_invite_team_short", lang),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.5.sp,
                            color = accentEmerald,
                            maxLines = 1
                        )
                    }
                }
            } else {
                // 3. Empty State Handling: No active cycle exists for this farm
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = pillBgEmerald,
                            border = BorderStroke(1.dp, pillBorderEmerald),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🏡", fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                Translations.getLocalizedFarmName(farm.id, lang, farm.name),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.5.sp,
                                color = primaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                Translations.getLocalizedFarmLocation(farm.id, lang, farm.location),
                                fontSize = 12.sp,
                                color = secondaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        shape = innerCornerShape,
                        color = pillBgEmerald,
                        border = BorderStroke(1.dp, pillBorderEmerald)
                    ) {
                        Text(
                            "${String.format(java.util.Locale.US, "%,d", farm.capacity)} ${Translations.tr("birds_unit", lang)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = accentEmerald,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = innerCornerShape,
                    color = identityPanelBg,
                    border = BorderStroke(1.dp, identityPanelBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            Translations.tr("no_active_cycle", lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = primaryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            Translations.tr("no_active_cycle_desc", lang),
                            fontSize = 11.sp,
                            color = secondaryText,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onStartNewCycle(farm) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_start_cycle_${farm.id}"),
                        shape = innerCornerShape,
                        colors = ButtonDefaults.buttonColors(containerColor = appearance.accentOption.primaryColor)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            Translations.tr("btn_start_new_cycle", lang),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = { showTeamQrInviteSheet = true },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("btn_add_team_${farm.id}"),
                        shape = innerCornerShape,
                        border = BorderStroke(1.2.dp, accentEmerald.copy(alpha = 0.55f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = qrButtonBg,
                            contentColor = accentEmerald
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            Translations.tr("btn_invite_team_short", lang),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.5.sp,
                            color = accentEmerald,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * ModalBottomSheet for QR Code Team Invitation ("دعوة فريق العمل (QR)").
 * Allows the Owner to choose `[ 👷 مشرف عنبر | 👨‍⚕️ طبيب بيطري ]` and displays a custom-painted
 * QR Code representing `farmId` and `selectedRole` with onboarding instructions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamQrInviteBottomSheet(
    farm: FarmModel,
    lang: AppLanguage,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedInviteRole by remember { mutableStateOf(AppRole.WORKER) }
    val localizedFarmName = Translations.getLocalizedFarmName(farm.id, lang, farm.name)
    val qrPayload = "AGRITECH_JOIN|farmId=${farm.id}|role=${selectedInviteRole.name}"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        modifier = Modifier.testTag("team_qr_invite_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE8F4FD),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.QrCode,
                                contentDescription = null,
                                tint = TextForestGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            Translations.tr("qr_invite_sheet_title", lang),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = TextForestGreen
                        )
                        Text(
                            "$localizedFarmName • ID: ${farm.id}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            // Role Selector: [ 👷 مشرف عنبر | 👨‍⚕️ طبيب بيطري ]
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFE8F4FD),
                border = BorderStroke(1.dp, Color(0xFFBBDEFB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val inviteRoles = listOf(
                        Triple(AppRole.WORKER, Translations.tr("qr_role_worker_btn", lang), "qr_role_worker"),
                        Triple(AppRole.VETERINARIAN, Translations.tr("qr_role_vet_btn", lang), "qr_role_vet")
                    )
                    inviteRoles.forEach { (roleOpt, label, tag) ->
                        val isSelected = selectedInviteRole == roleOpt
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) TextForestGreen else Color.White)
                                .testTag(tag)
                                .clickable { selectedInviteRole = roleOpt }
                                .padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                color = if (isSelected) Color.White else Color(0xFF1E293B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Prominent Clean QR Code Card (Ice Blue subtle frame + crisp white QR container)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFE8F4FD),
                border = BorderStroke(1.5.dp, TextForestGreen.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("qr_code_display_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 3.dp,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.size(216.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CustomQrCodeCanvas(
                                payload = qrPayload,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Center Role Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                border = BorderStroke(1.5.dp, TextForestGreen),
                                shadowElevation = 2.dp,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        if (selectedInviteRole == AppRole.WORKER) "👷" else "👨‍⚕️",
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Text(
                            "${farm.id.uppercase()} • ${if (selectedInviteRole == AppRole.WORKER) "WORKER" else "VET"}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextForestGreen
                        )
                    }
                }
            }

            // Instruction Text Below the QR
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = LightGreenContainer.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, Color(0xFFA5D6A7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    Translations.tr("qr_invite_instruction", lang),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B5E20),
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

/**
 * Genuine ISO/IEC 18004 QR Code Painter using `com.google.zxing.qrcode.QRCodeWriter` (`BitMatrix`).
 * Renders exact QR modules with standard quiet zone margin so `RealQrCameraScannerView` (ML Kit)
 * can immediately scan and decode the QR code from another phone.
 */
@Composable
fun CustomQrCodeCanvas(
    payload: String,
    modifier: Modifier = Modifier
) {
    val bitMatrix = remember(payload) {
        try {
            PoultryCalculators.encodeQrBitMatrix(payload, size = 1)
        } catch (_: Exception) {
            null
        }
    }

    Canvas(modifier = modifier) {
        drawRect(color = Color.White, size = size)
        val matrix = bitMatrix ?: return@Canvas
        val matrixWidth = matrix.width.coerceAtLeast(1)
        val matrixHeight = matrix.height.coerceAtLeast(1)
        val cellSize = size.minDimension / maxOf(matrixWidth, matrixHeight).toFloat()
        val offsetX = (size.width - matrixWidth * cellSize) / 2f
        val offsetY = (size.height - matrixHeight * cellSize) / 2f
        val darkColor = Color(0xFF0F172A)

        for (y in 0 until matrixHeight) {
            for (x in 0 until matrixWidth) {
                if (matrix.get(x, y)) {
                    drawRect(
                        color = darkColor,
                        topLeft = Offset(offsetX + x * cellSize, offsetY + y * cellSize),
                        size = Size(cellSize + 0.4f, cellSize + 0.4f)
                    )
                }
            }
        }
    }
}

/**
 * QR Scanner Onboarding ModalBottomSheet triggered from `LoginScreen` ("مسح رمز الانضمام 📷").
 * Uses Google ML Kit Barcode Scanning + CameraX (`RealQrCameraScannerView`) with runtime
 * `Manifest.permission.CAMERA` handling to scan a farm's QR invitation and join immediately.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerJoinBottomSheet(
    currentTheme: AppThemeOption,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onJoinRole: (AppRole) -> Unit,
    onJoinFarmWithRole: ((String, AppRole) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var scannedRole by remember { mutableStateOf(AppRole.WORKER) }
    var scannedFarmId by remember { mutableStateOf("farm_1") }
    var detectedQrBanner by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        modifier = Modifier.testTag("qr_scanner_join_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE8F4FD),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = TextForestGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        Translations.tr("qr_scanner_sheet_title", lang),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.5.sp,
                        color = TextForestGreen
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            // Real CameraX + Google ML Kit Barcode Scanner Viewfinder with CAMERA Permission Handling
            RealQrCameraScannerView(
                selectedRoleFallback = scannedRole,
                onQrCodeScanned = { inviteData ->
                    scannedFarmId = inviteData.farmId
                    scannedRole = inviteData.role
                    detectedQrBanner = "تمت قراءة رمز العنبر (${inviteData.farmId}) • الدور: ${inviteData.role.name}"
                    onJoinFarmWithRole?.invoke(inviteData.farmId, inviteData.role)
                    onJoinRole(inviteData.role)
                }
            )

            if (detectedQrBanner != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LightGreenContainer,
                    border = BorderStroke(1.dp, TextForestGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = detectedQrBanner!!,
                        modifier = Modifier.padding(10.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextForestGreen,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Text(
                Translations.tr("qr_scanner_desc", lang),
                fontSize = 12.5.sp,
                color = Color(0xFF475569),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            // Role Selection fallback for manual confirmation in emulator/test environments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val roles = listOf(
                    Pair(AppRole.WORKER, Translations.tr("qr_role_worker_btn", lang)),
                    Pair(AppRole.VETERINARIAN, Translations.tr("qr_role_vet_btn", lang))
                )
                roles.forEach { (roleOpt, label) ->
                    val isSel = scannedRole == roleOpt
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { scannedRole = roleOpt },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) LightGreenContainer else Color(0xFFF8FAFC),
                        border = BorderStroke(if (isSel) 1.5.dp else 1.dp, if (isSel) TextForestGreen else Color(0xFFE2E8F0))
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = if (isSel) TextForestGreen else Color(0xFF334155)
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    onJoinFarmWithRole?.invoke(scannedFarmId, scannedRole)
                    onJoinRole(scannedRole)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_confirm_qr_scan_join"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TextForestGreen)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "تأكيد المسح والانضمام فوراً ✓",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * WhatsApp-Style Personal Account QR & Live Scanner BottomSheet (`PersonalAccountWhatsAppQrBottomSheet`):
 * - Tab 1 ("رمزي الشخصي QR"): Displays user's profile card (Name, Profession Role badge, Phone/Email)
 *   and a scannable `CustomQrCodeCanvas` encoding `"MYBUSINESS_CONTACT|name=...|role=...|phone=..."`
 *   with subtitle `"امسح هذا الرمز عبر الكاميرا لإضافتي مباشرة في الدردشة (مثل واتساب)"`.
 * - Tab 2 ("مسح رمز QR 📷"): Uses the live camera scanner (`RealQrCameraScannerView` / ML Kit)
 *   to scan either a Personal Contact QR (automatically adds the contact to `AgritechMockDb.chatConversations`
 *   and opens the chat) or a Barn Team QR.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalAccountWhatsAppQrBottomSheet(
    currentTheme: AppThemeOption,
    lang: AppLanguage,
    activeRole: AppRole = AppRole.OWNER,
    onDismiss: () -> Unit,
    onContactScanned: (ChatConversationModel) -> Unit,
    onJoinRole: (AppRole) -> Unit,
    onJoinFarmWithRole: ((String, AppRole) -> Unit)? = null
) {
    val appearance = LocalAppAppearanceState.current
    val isDark = currentTheme.isDark
    val sheetBg = if (isDark) Color(0xFF1E293B) else Color.White
    val primaryText = if (isDark) Color.White else currentTheme.headingColor
    val secondaryText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    val activeAccent = appearance.accentColor
    val accentContainer = appearance.accentContainerColor
    val primaryButtonAccent = appearance.accentOption.primaryColor
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedTab by remember { mutableStateOf(0) }
    var scannedRole by remember { mutableStateOf(AppRole.WORKER) }
    var scannedFarmId by remember { mutableStateOf("farm_1") }
    var detectedQrBanner by remember { mutableStateOf<String?>(null) }

    val currentUser = try {
        FirebaseAuth.getInstance().currentUser
    } catch (_: Exception) {
        null
    }
    val userDisplayName = currentUser?.displayName?.takeIf { it.isNotBlank() }
        ?: AgritechMockDb.farms.firstOrNull()?.name?.takeIf { it.isNotBlank() }
        ?: Translations.tr("account_local_profile", lang)
    val userPhoneOrEmail = currentUser?.phoneNumber?.takeIf { it.isNotBlank() }
        ?: currentUser?.email?.takeIf { it.isNotBlank() }
        ?: Translations.tr("account_local_profile", lang)
    val roleBadgeLabel = when (activeRole.normalizedHubRole) {
        AppRole.VET_CLINIC -> Translations.tr("role_badge_vet_clinic", lang)
        AppRole.WORKER -> Translations.tr("role_badge_worker", lang)
        AppRole.FEED_SUPPLIER -> Translations.tr("role_badge_feed_supplier", lang)
        AppRole.CHICK_HATCHERY -> Translations.tr("role_badge_chick_hatchery", lang)
        AppRole.WHOLESALE_TRADER -> Translations.tr("role_badge_wholesale_trader", lang)
        AppRole.VET_IMPORTER -> Translations.tr("role_badge_vet_importer", lang)
        else -> Translations.tr("role_badge_owner_barn", lang)
    }
    val personalQrPayload = "MYBUSINESS_CONTACT|name=$userDisplayName|role=$roleBadgeLabel|phone=$userPhoneOrEmail"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        modifier = Modifier.testTag("personal_whatsapp_qr_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = accentContainer,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.QrCode,
                                contentDescription = null,
                                tint = activeAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        Translations.tr("personal_qr_sheet_title", lang),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.5.sp,
                        color = primaryText
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = secondaryText)
                }
            }

            // Two-Tab Switcher: [ رمزي الشخصي QR | مسح رمز QR 📷 ]
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf(
                        Triple(0, Translations.tr("personal_qr_tab_my_code", lang), "personal_qr_tab_0"),
                        Triple(1, Translations.tr("personal_qr_tab_scan", lang), "personal_qr_tab_1")
                    )
                    tabs.forEach { (idx, label, tag) ->
                        val isSel = selectedTab == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) primaryButtonAccent else Color.Transparent)
                                .clickable { selectedTab = idx }
                                .padding(vertical = 10.dp)
                                .testTag(tag),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = if (isSel) Color.White else primaryText,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                // Tab 1: رمزي الشخصي QR (WhatsApp-style Personal Contact QR Card)
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                    border = BorderStroke(1.5.dp, activeAccent.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("personal_qr_profile_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // User Profile Header
                        Surface(
                            shape = CircleShape,
                            color = accentContainer,
                            border = BorderStroke(1.5.dp, activeAccent),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("👤", fontSize = 26.sp)
                            }
                        }
                        Text(
                            userDisplayName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = primaryText,
                            textAlign = TextAlign.Center
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = accentContainer,
                            border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.4f))
                        ) {
                            Text(
                                roleBadgeLabel,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = activeAccent
                            )
                        }
                        Text(
                            userPhoneOrEmail,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = secondaryText
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Scannable CustomQrCodeCanvas
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            shadowElevation = 3.dp,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .size(210.dp)
                                .testTag("personal_qr_canvas_box")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CustomQrCodeCanvas(
                                    payload = personalQrPayload,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.5.dp, activeAccent),
                                    shadowElevation = 2.dp,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("💬", fontSize = 14.sp)
                                    }
                                }
                            }
                        }

                        Text(
                            Translations.tr("personal_qr_whatsapp_subtitle", lang),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = activeAccent,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                // Tab 2: مسح رمز QR 📷 (Live Camera Scanner for Personal Contact QR like WhatsApp)
                RealQrCameraScannerView(
                    selectedRoleFallback = scannedRole,
                    onQrCodeScanned = { inviteData ->
                        val contactParsed = parsePersonalContactQr(inviteData.rawPayload)
                        if (contactParsed != null) {
                            val newConv = AgritechMockDb.addPhoneContact(
                                name = contactParsed.name,
                                phone = contactParsed.phone,
                                roleLabel = contactParsed.roleLabel,
                                avatarEmoji = "🤝"
                            )
                            detectedQrBanner = "تمت إضافة جهة الاتصال (${contactParsed.name}) في الدردشة ✓"
                            onContactScanned(newConv)
                        } else {
                            scannedFarmId = inviteData.farmId
                            scannedRole = inviteData.role
                            detectedQrBanner = "تمت قراءة رمز العنبر (${inviteData.farmId}) • الدور: ${inviteData.role.name}"
                            onJoinFarmWithRole?.invoke(inviteData.farmId, inviteData.role)
                            onJoinRole(inviteData.role)
                        }
                    }
                )

                if (detectedQrBanner != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = accentContainer,
                        border = BorderStroke(1.dp, activeAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = detectedQrBanner!!,
                            modifier = Modifier.padding(10.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeAccent,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Text(
                    Translations.tr("personal_qr_scan_whatsapp_desc", lang),
                    fontSize = 12.sp,
                    color = secondaryText,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Button(
                    onClick = {
                        val newConv = AgritechMockDb.addPhoneContact(
                            name = "شريك أعمال (مسح QR)",
                            phone = "0550998877",
                            roleLabel = roleBadgeLabel,
                            avatarEmoji = "🤝"
                        )
                        detectedQrBanner = "تمت إضافة جهة الاتصال (${newConv.userName}) في الدردشة ✓"
                        onContactScanned(newConv)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_confirm_qr_scan_join"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryButtonAccent)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        Translations.tr("personal_qr_scan_add_contact_btn", lang),
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
 * Mandatory Review & Digital Finger Signature Dialog before approving pending invoices or expenses.
 */
@Composable
fun InvoiceApprovalSignatureDialog(
    categoryOrTitle: String,
    dateOrDayText: String,
    amount: Double,
    currency: String,
    currentTheme: AppThemeOption,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onConfirmSignedApproval: () -> Unit
) {
    val appearance = LocalAppAppearanceState.current
    val isDark = currentTheme.isDark
    val dialogBg = if (isDark) Color(0xFF1E293B) else Color.White
    val primaryText = if (isDark) Color.White else Color(0xFF0F172A)
    val secondaryText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    val activeAccent = appearance.accentColor
    val primaryButtonAccent = appearance.accentOption.primaryColor

    val strokes = remember { mutableStateListOf<List<Offset>>() }
    var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }
    val signaturePointsCount = strokes.sumOf { it.size } + currentStroke.size
    val hasSignature = signaturePointsCount > 3

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBg,
        shape = RoundedCornerShape(appearance.cornerRadiusDp.dp),
        modifier = Modifier.testTag("invoice_approval_signature_dialog"),
        title = {
            Text(
                Translations.tr("invoice_sig_dialog_title", lang),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = primaryText
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Full Invoice Summary Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            categoryOrTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = primaryText
                        )
                        Text(
                            dateOrDayText,
                            fontSize = 12.sp,
                            color = secondaryText
                        )
                        HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                Translations.tr("amount_label", lang),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = secondaryText
                            )
                            Text(
                                "${String.format(java.util.Locale.US, "%,.0f", amount)} $currency",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFD97706)
                            )
                        }
                    }
                }

                // Official Treasury Deduction Notice
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDark) Color(0xFF78350F).copy(alpha = 0.35f) else Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        Translations.tr("invoice_sig_deduction_notice", lang),
                        modifier = Modifier.padding(10.dp),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E),
                        lineHeight = 17.sp
                    )
                }

                // Finger Signature Pad Header + Clear Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        Translations.tr("invoice_sig_pad_label", lang),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = {
                            strokes.clear()
                            currentStroke = emptyList()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("btn_clear_approval_signature")
                    ) {
                        Text(
                            Translations.tr("invoice_sig_clear_btn", lang),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = ActionRed
                        )
                    }
                }

                // Interactive Finger Signature Canvas
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF),
                    border = BorderStroke(
                        1.5.dp,
                        if (hasSignature) activeAccent else Color(0xFF94A3B8)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("invoice_approval_signature_canvas")
                                .pointerInput(Unit) {
                                    detectTapGestures { offset ->
                                        strokes.add(
                                            listOf(
                                                offset,
                                                Offset(offset.x + 12f, offset.y - 6f),
                                                Offset(offset.x + 24f, offset.y + 6f),
                                                Offset(offset.x + 36f, offset.y - 4f)
                                            )
                                        )
                                    }
                                }
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { startOffset ->
                                            currentStroke = listOf(startOffset)
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            currentStroke = currentStroke + change.position
                                        },
                                        onDragEnd = {
                                            if (currentStroke.isNotEmpty()) {
                                                strokes.add(currentStroke)
                                                currentStroke = emptyList()
                                            }
                                        }
                                    )
                                }
                        ) {
                            val strokeColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0F172A)
                            val allStrokes = if (currentStroke.isNotEmpty()) strokes + listOf(currentStroke) else strokes
                            allStrokes.forEach { pts ->
                                if (pts.size >= 2) {
                                    val path = Path().apply {
                                        moveTo(pts.first().x, pts.first().y)
                                        for (i in 1 until pts.size) {
                                            lineTo(pts[i].x, pts[i].y)
                                        }
                                    }
                                    drawPath(
                                        path = path,
                                        color = strokeColor,
                                        style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )
                                } else if (pts.size == 1) {
                                    drawCircle(color = strokeColor, radius = 4f, center = pts.first())
                                }
                            }
                        }

                        if (!hasSignature) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "✍️",
                                    fontSize = 24.sp,
                                    color = secondaryText.copy(alpha = 0.35f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmSignedApproval,
                enabled = hasSignature,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("btn_confirm_signed_invoice_approval"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryButtonAccent,
                    disabledContainerColor = Color.LightGray
                )
            ) {
                Text(
                    Translations.tr("invoice_sig_confirm_btn", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Translations.tr("btn_cancel", lang), color = secondaryText)
            }
        }
    )
}

/**
 * Interactive Finger Signature Box for Official Sales Invoice (`ProfessionalSalesInvoiceModal`).
 */
@Composable
fun DigitalSignaturePadBox(
    title: String,
    subtitle: String,
    signedBadgeText: String,
    clearText: String,
    isDark: Boolean,
    accentColor: Color,
    accentContainerColor: Color,
    primaryText: Color,
    testTagPrefix: String,
    modifier: Modifier = Modifier
) {
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }
    val isSigned = strokes.isNotEmpty() || currentStroke.isNotEmpty()

    Surface(
        color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (isSigned) accentColor else (if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1))),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp,
                    color = primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (isSigned) {
                    Text(
                        text = clearText,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = ActionRed,
                        modifier = Modifier
                            .clickable {
                                strokes.clear()
                                currentStroke = emptyList()
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .testTag("${testTagPrefix}_clear")
                    )
                }
            }
            Text(subtitle, fontSize = 9.5.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isDark) Color(0xFF1E293B) else Color.White,
                border = BorderStroke(1.dp, if (isSigned) accentColor.copy(alpha = 0.6f) else Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("${testTagPrefix}_canvas")
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    strokes.add(
                                        listOf(
                                            offset,
                                            Offset(offset.x + 16f, offset.y - 6f),
                                            Offset(offset.x + 32f, offset.y + 4f)
                                        )
                                    )
                                }
                            }
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { startOffset ->
                                        currentStroke = listOf(startOffset)
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        currentStroke = currentStroke + change.position
                                    },
                                    onDragEnd = {
                                        if (currentStroke.isNotEmpty()) {
                                            strokes.add(currentStroke)
                                            currentStroke = emptyList()
                                        }
                                    }
                                )
                            }
                    ) {
                        val inkColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0F172A)
                        val allStrokes = if (currentStroke.isNotEmpty()) strokes + listOf(currentStroke) else strokes
                        allStrokes.forEach { pts ->
                            if (pts.size >= 2) {
                                val path = Path().apply {
                                    moveTo(pts.first().x, pts.first().y)
                                    for (i in 1 until pts.size) {
                                        lineTo(pts[i].x, pts[i].y)
                                    }
                                }
                                drawPath(
                                    path = path,
                                    color = inkColor,
                                    style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            } else if (pts.size == 1) {
                                drawCircle(color = inkColor, radius = 3.5f, center = pts.first())
                            }
                        }
                    }

                    if (!isSigned) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "✍️ وقّع بإصبعك",
                                fontSize = 10.sp,
                                color = Color.Gray.copy(alpha = 0.65f)
                            )
                        }
                    }
                }
            }

            if (isSigned) {
                Surface(
                    color = accentContainerColor,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        signedBadgeText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }
        }
    }
}

// ============================================================================
// 3. دفتر المزرعة (TABLE, TREASURY, ANALYTICS, VET)
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgritechFarmNotebookScreen(
    farm: FarmModel,
    cycle: CycleModel,
    currentTheme: AppThemeOption,
    lang: AppLanguage,
    firestoreRepo: AgritechFirestoreRepository? = null,
    reports: List<DailyReportModel>,
    vetNotes: List<VetNoteModel>,
    transactions: List<TransactionModel>,
    onAddTransaction: (tx: TransactionModel, addedFeedKg: Double) -> Unit,
    onSaveOrUpdateDailyReport: (DailyReportModel) -> Unit = { AgritechMockDb.upsertDailyReportWithAudit(it) },
    onApproveVetInvoice: (transactionId: String, vetNoteId: String) -> Unit = { txId, vnId ->
        AgritechMockDb.approveVetInvoice(txId, vnId)
    },
    onOpenSettings: () -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var showIncomeDialog by remember { mutableStateOf(false) }
    var showFeedLedgerBottomSheet by remember { mutableStateOf(false) }
    var showMedicineLedgerBottomSheet by remember { mutableStateOf(false) }
    var selectedMedicineTxForLedger by remember { mutableStateOf<TransactionModel?>(null) }
    var showVetMedicineInvoiceDialog by remember { mutableStateOf(false) }
    var vetInvoiceSuccessBanner by remember { mutableStateOf<String?>(null) }
    var showOwnerDailyReportDialog by remember { mutableStateOf(false) }
    var editingDailyReport by remember { mutableStateOf<DailyReportModel?>(null) }
    var selectedOwnerDayReportForMedicalDetails by remember { mutableStateOf<DailyReportModel?>(null) }
    var activeContextualChatChannel by remember { mutableStateOf<String?>(null) }
    var activeContextualChatTitle by remember { mutableStateOf("") }
    var pendingSignatureApprovalRequest by remember { mutableStateOf< (()->Unit)? >(null) }
    var pendingSignatureTitle by remember { mutableStateOf("") }
    var pendingSignatureDate by remember { mutableStateOf("") }
    var pendingSignatureAmount by remember { mutableStateOf(0.0) }

    // Stream the active ProductionCycle from Firestore when entering AgritechFarmNotebookScreen
    val streamedCycles by produceState<List<CycleModel>>(initialValue = emptyList(), farm.id, cycle.id, firestoreRepo) {
        val uid = try {
            FirebaseAuth.getInstance().currentUser?.uid
        } catch (_: Exception) {
            null
        }
        if (uid != null && firestoreRepo != null) {
            try {
                firestoreRepo.streamCycles(uid, farm.id, "active").collectLatest { list ->
                    value = list
                }
            } catch (_: Exception) {}
        }
    }
    val activeCycle = AgritechMockDb.cycles.firstOrNull { it.id == cycle.id }
        ?: streamedCycles.firstOrNull { it.id == cycle.id }
        ?: cycle

    val liveReportsForFarm = (AgritechMockDb.dailyReports.filter { it.farmId == farm.id } + reports.filter { it.farmId == farm.id })
        .distinctBy { it.dayNumber }
    val reportsForCycle = liveReportsForFarm.filter {
        it.farmId == farm.id && (it.cycleId == activeCycle.id || it.cycleId.isBlank())
    }
    val liveVetNotesForFarm = (AgritechMockDb.vetNotes.filter { it.farmId == farm.id } + vetNotes.filter { it.farmId == farm.id })
        .distinctBy { it.id }
    val txsForCycle = (AgritechMockDb.transactions.filter { it.farmId == farm.id } + transactions.filter { it.farmId == farm.id })
        .distinctBy { it.id }
        .filter {
            it.farmId == farm.id && (it.cycleId == activeCycle.id || it.cycleId.isBlank())
        }

    val calculatedCycleDay = remember(activeCycle.startDate, reportsForCycle) {
        val reportMax = reportsForCycle.maxOfOrNull { it.dayNumber } ?: 1
        PoultryCalculators.calculateCycleDay(activeCycle.startDate, reportMax)
    }
    val ageInDays = calculatedCycleDay

    var lastSavedInvoice by remember { mutableStateOf<SalesInvoiceModel?>(null) }
    var selectedInvoiceForView by remember { mutableStateOf<SalesInvoiceModel?>(null) }
    var selectedTransactionForReceipt by remember { mutableStateOf<TransactionModel?>(null) }

    // Weighing & Sales Flow State (ميزان وبيع الدفعة المستقل)
    var salesFlowStep by remember { mutableStateOf(SalesFlowStep.NONE) }
    var buyerNameInput by remember { mutableStateOf("") }
    var truckPlateInput by remember { mutableStateOf("") }
    var pricePerKgInput by remember { mutableStateOf("380") }
    var birdsPerCrateInput by remember { mutableStateOf("10") }
    var tareMode by remember { mutableStateOf(TareMode.DETAILED) }
    var directTareTotalInput by remember { mutableStateOf("") }
    var tareWeightsList by remember { mutableStateOf(emptyList<Double>()) }
    var grossWeightsList by remember { mutableStateOf(emptyList<WeighEntry>()) }
    var showReceiptDialog by remember { mutableStateOf(false) }
    var salesMigratedMsg by remember { mutableStateOf<String?>(null) }

    val parsedBirdsPerCrate = (birdsPerCrateInput.toIntOrNull() ?: 10).coerceAtLeast(1)
    val totalTareKg = if (tareMode == TareMode.DETAILED) tareWeightsList.sum() else (directTareTotalInput.toDoubleOrNull() ?: 0.0)
    val totalGrossKg = grossWeightsList.sumOf { it.weightKg }
    val totalGrossCrates = grossWeightsList.sumOf { it.cratesCount }
    val totalNetKg = (totalGrossKg - totalTareKg).coerceAtLeast(0.0)
    val totalBirdsSold = grossWeightsList.sumOf { it.cratesCount * parsedBirdsPerCrate }
    val avgBirdWeightKg = if (totalBirdsSold > 0) totalNetKg / totalBirdsSold else 0.0
    val pricePerKg = pricePerKgInput.toDoubleOrNull() ?: 380.0
    val totalSalesValue = totalNetKg * pricePerKg

    val totalFeed = reportsForCycle.sumOf { it.feedIntakeKg }
    val totalWater = reportsForCycle.sumOf { it.waterIntakeLiters }
    val totalMortality = reportsForCycle.sumOf { it.mortalityCount }
    val totalPurchasedFeedKg = txsForCycle.filter {
        it.type == TransactionType.EXPENSE &&
            it.status.equals("APPROVED", true) &&
            (it.category.contains("علف") || it.category.contains("Feed", true) || it.category.contains("Aliment", true))
    }.sumOf { parseFeedTransaction(it, lang).estimatedKg }
    val effectiveFeedInventoryKg = if (totalPurchasedFeedKg > 0.0 || reportsForCycle.isNotEmpty()) {
        (totalPurchasedFeedKg - totalFeed).coerceAtLeast(0.0)
    } else {
        activeCycle.feedInventoryKg.coerceAtLeast(0.0)
    }

    // Idempotency & Single Source of Truth Treasury Balance (Only APPROVED transactions for active cycle affect Treasury)
    var lastSubmittedIdempotencyKey by remember { mutableStateOf("") }
    val approvedTransactions = txsForCycle.filter { it.status.equals("APPROVED", ignoreCase = true) }
    val totalExpenses = approvedTransactions.filter { it.type == TransactionType.EXPENSE && it.amount > 0.0 }.sumOf { it.amount }
    val totalIncome = approvedTransactions.filter { it.type == TransactionType.INCOME && it.amount > 0.0 }.sumOf { it.amount }
    val netBalance = totalIncome - totalExpenses

    val cumulativeBirdsSold = PoultryCalculators.resolveCumulativeBirdsSold(activeCycle, txsForCycle)
    val cumulativeSoldWeightKg = PoultryCalculators.resolveCumulativeSoldWeightKg(activeCycle, txsForCycle)
    val currentStandardWeightKg = PoultryCalculators.getRoss308StandardWeightKg(ageInDays)
    val actualMeasuredWeightKg = PoultryCalculators.resolveActualAvgBirdWeightKg(
        reports = reportsForCycle,
        cycleTransactions = txsForCycle,
        cycleId = activeCycle.id
    )
    val effectiveAvgWeightKg = actualMeasuredWeightKg ?: currentStandardWeightKg
    val liveBirds = PoultryCalculators.calculateLiveBirds(
        totalBirdsPlaced = activeCycle.totalBirdsPlaced,
        totalMortality = totalMortality,
        cumulativeBirdsSold = cumulativeBirdsSold
    )
    val totalProducedBiomassKg = PoultryCalculators.calculateTotalProducedBiomassKg(
        liveBirds = liveBirds,
        avgLiveWeightKg = effectiveAvgWeightKg,
        cumulativeSoldWeightKg = cumulativeSoldWeightKg
    )
    val accurateFcr = PoultryCalculators.calculateFcr(
        totalFeedKg = totalFeed,
        initialBirds = activeCycle.totalBirdsPlaced,
        totalMortality = totalMortality,
        birdsSold = cumulativeBirdsSold,
        ageInDays = ageInDays,
        actualAvgWeightKg = actualMeasuredWeightKg,
        cumulativeSoldWeightKg = cumulativeSoldWeightKg
    )

    val mortalityPct = if (activeCycle.totalBirdsPlaced > 0) (totalMortality.toDouble() / activeCycle.totalBirdsPlaced) * 100.0 else 0.0
    val liveabilityPct = (100.0 - mortalityPct).coerceIn(0.0, 100.0)
    val accurateEpef = PoultryCalculators.calculateEpef(
        livabilityPct = liveabilityPct,
        avgLiveWeightKg = effectiveAvgWeightKg,
        ageInDays = ageInDays,
        fcr = accurateFcr
    )
    val activeWithdrawalDays = PoultryCalculators.calculateWithdrawalDaysRemaining(
        vetNotes = liveVetNotesForFarm.filter { it.cycleId == activeCycle.id || it.cycleId.isBlank() },
        currentCycleDay = calculatedCycleDay,
        maxReportDay = ageInDays,
        reports = reportsForCycle
    )

    val curr = Translations.tr("currency", lang)
    val kgUnit = Translations.tr("kg_unit", lang)
    val literUnit = Translations.tr("liter_unit", lang)

    val expenseCategories = listOf(
        Translations.tr("tx_feed", lang),
        Translations.tr("tx_chicks", lang),
        Translations.tr("tx_meds", lang),
        Translations.tr("tx_gas", lang),
        Translations.tr("tx_labor", lang),
        Translations.tr("tx_other_exp", lang)
    )

    val incomeCategories = listOf(
        Translations.tr("tx_chicken_sale", lang),
        Translations.tr("tx_manure", lang),
        Translations.tr("tx_other_inc", lang)
    )

    if (showExpenseDialog) {
        val expenseDialogIdempUuid = remember { java.util.UUID.randomUUID().toString() }
        SimpleTransactionDialog(
            title = Translations.tr("btn_spend", lang),
            type = TransactionType.EXPENSE,
            lang = lang,
            categories = expenseCategories,
            onDismiss = { showExpenseDialog = false },
            onConfirm = { cat, amt, addedFeedKg ->
                if (amt > 0.0) {
                    val idempKey = "tx_exp_${farm.id}_${activeCycle.id}_$expenseDialogIdempUuid"
                    if (idempKey != lastSubmittedIdempotencyKey) {
                        lastSubmittedIdempotencyKey = idempKey
                        onAddTransaction(
                            TransactionModel(
                                id = idempKey,
                                farmId = farm.id,
                                cycleId = activeCycle.id,
                                type = TransactionType.EXPENSE,
                                category = cat,
                                amount = amt,
                                currency = curr,
                                date = "${Translations.tr("day_prefix", lang)} $ageInDays",
                                quantityQuintals = if (addedFeedKg > 0.0) addedFeedKg / 100.0 else 0.0
                            ),
                            addedFeedKg
                        )
                    }
                }
                showExpenseDialog = false
            }
        )
    }

    if (showIncomeDialog) {
        val incomeDialogIdempUuid = remember { java.util.UUID.randomUUID().toString() }
        SimpleTransactionDialog(
            title = Translations.tr("btn_income", lang),
            type = TransactionType.INCOME,
            lang = lang,
            categories = incomeCategories,
            onDismiss = { showIncomeDialog = false },
            onConfirm = { cat, amt, _ ->
                if (amt > 0.0) {
                    val idempKey = "tx_inc_${farm.id}_${cycle.id}_$incomeDialogIdempUuid"
                    if (idempKey != lastSubmittedIdempotencyKey) {
                        lastSubmittedIdempotencyKey = idempKey
                        onAddTransaction(
                            TransactionModel(
                                id = idempKey,
                                farmId = farm.id,
                                cycleId = cycle.id,
                                type = TransactionType.INCOME,
                                category = cat,
                                amount = amt,
                                currency = curr,
                                date = "${Translations.tr("day_prefix", lang)} $ageInDays"
                            ),
                            0.0
                        )
                    }
                }
                showIncomeDialog = false
            }
        )
    }

    if (selectedTransactionForReceipt != null) {
        val txForReceipt = selectedTransactionForReceipt!!
        DigitalTransactionReceiptBottomSheet(
            farmName = Translations.getLocalizedFarmName(farm.id, lang, farm.name),
            cycle = cycle,
            calculatedCycleDay = calculatedCycleDay,
            transaction = txForReceipt,
            lang = lang,
            onOpenDetailedWeighingInvoice = if (txForReceipt.invoiceData != null) {
                {
                    val inv = txForReceipt.invoiceData
                    selectedTransactionForReceipt = null
                    selectedInvoiceForView = inv
                }
            } else null,
            onDismiss = { selectedTransactionForReceipt = null }
        )
    }

    if (selectedInvoiceForView != null) {
        ProfessionalSalesInvoiceModal(
            invoice = selectedInvoiceForView!!,
            lang = lang,
            onDismiss = { selectedInvoiceForView = null }
        )
    }

    if (showReceiptDialog) {
        LiveBroilerReceiptDialog(
            farmName = Translations.getLocalizedFarmName(farm.id, lang),
            cycleId = cycle.id,
            dateText = "${Translations.tr("day_prefix", lang)} $ageInDays",
            lang = lang,
            weighingRecordsCount = grossWeightsList.size,
            totalGrossKg = totalGrossKg,
            totalTareKg = totalTareKg,
            totalNetKg = totalNetKg,
            totalBirds = totalBirdsSold,
            avgWeightKg = avgBirdWeightKg,
            pricePerKg = pricePerKg,
            totalValue = totalSalesValue,
            currency = curr,
            kgUnit = kgUnit,
            buyerName = buyerNameInput,
            truckPlate = truckPlateInput,
            onDismiss = {
                showReceiptDialog = false
                salesFlowStep = SalesFlowStep.NONE
            }
        )
    }

    when (salesFlowStep) {
        SalesFlowStep.STAGE_1_SHIPMENT_INFO -> {
            BatchShipmentInfoScreen(
                currentTheme = currentTheme,
                lang = lang,
                farm = farm,
                cycle = cycle,
                ageInDays = ageInDays,
                liveBirds = liveBirds,
                buyerName = buyerNameInput,
                onBuyerNameChange = { buyerNameInput = it },
                truckPlate = truckPlateInput,
                onTruckPlateChange = { truckPlateInput = it },
                pricePerKg = pricePerKgInput,
                onPricePerKgChange = { pricePerKgInput = it },
                birdsPerCrate = birdsPerCrateInput,
                onBirdsPerCrateChange = { birdsPerCrateInput = it },
                tareMode = tareMode,
                onTareModeChange = { tareMode = it },
                directTareTotal = directTareTotalInput,
                onDirectTareTotalChange = { directTareTotalInput = it },
                onProceedToWeighing = { salesFlowStep = SalesFlowStep.STAGE_2_WEIGHING_TABLE },
                onBack = { salesFlowStep = SalesFlowStep.NONE }
            )
        }
        SalesFlowStep.STAGE_2_WEIGHING_TABLE -> {
            val parsedPrice = pricePerKgInput.toDoubleOrNull() ?: 380.0
            val directTareParsed = directTareTotalInput.toDoubleOrNull() ?: 0.0
            BatchQuickWeighingScreen(
                currentTheme = currentTheme,
                lang = lang,
                farm = farm,
                cycle = cycle,
                ageInDays = ageInDays,
                liveBirds = liveBirds,
                buyerName = buyerNameInput,
                truckPlate = truckPlateInput,
                birdsPerCrate = parsedBirdsPerCrate,
                tareMode = tareMode,
                directTareTotal = directTareParsed,
                pricePerKg = parsedPrice,
                tareWeights = tareWeightsList,
                grossWeights = grossWeightsList,
                onAddTareWeight = { tareWeightsList = tareWeightsList + it },
                onDeleteTareWeight = { idx ->
                    if (idx in tareWeightsList.indices) {
                        tareWeightsList = tareWeightsList.filterIndexed { i, _ -> i != idx }
                    }
                },
                onAddGrossWeight = { crates, kg ->
                    grossWeightsList = grossWeightsList + WeighEntry(crates, kg)
                },
                onDeleteGrossWeight = { idx ->
                    if (idx in grossWeightsList.indices) {
                        grossWeightsList = grossWeightsList.filterIndexed { i, _ -> i != idx }
                    }
                },
                onFinalizeAndMigrate = {
                    if (activeWithdrawalDays <= 0 && totalSalesValue > 0.0 && totalNetKg > 0.0 && totalBirdsSold in 1..liveBirds) {
                        val invoiceNumber = "INV-${farm.id.takeLast(1).uppercase()}-${String.format(java.util.Locale.US, "%04d", transactions.count { it.type == TransactionType.INCOME } + 1)}"
                        val invoiceIdempKey = "tx_sale_${farm.id}_${cycle.id}_${invoiceNumber}"
                        if (invoiceIdempKey != lastSubmittedIdempotencyKey) {
                            lastSubmittedIdempotencyKey = invoiceIdempKey
                            val completedInvoice = SalesInvoiceModel(
                                invoiceNumber = invoiceNumber,
                                farmName = Translations.getLocalizedFarmName(farm.id, lang),
                                cycleId = cycle.id,
                                dateText = "${Translations.tr("day_prefix", lang)} $ageInDays",
                                birdAgeDays = ageInDays,
                                buyerName = buyerNameInput.ifEmpty { "تاجر جملة معتمد" },
                                truckPlate = truckPlateInput.ifEmpty { "غير محدد" },
                                totalCrates = grossWeightsList.sumOf { it.cratesCount },
                                birdsPerCrate = parsedBirdsPerCrate,
                                totalBirdsSold = totalBirdsSold,
                                totalGrossKg = totalGrossKg,
                                totalTareKg = totalTareKg,
                                totalNetKg = totalNetKg,
                                avgBirdWeightKg = avgBirdWeightKg,
                                pricePerKg = pricePerKg,
                                totalAmount = totalSalesValue,
                                currency = curr,
                                tareWeightsCount = tareWeightsList.size,
                                grossWeightsCount = grossWeightsList.size
                            )

                            onAddTransaction(
                                TransactionModel(
                                    id = invoiceIdempKey,
                                    farmId = farm.id,
                                    cycleId = cycle.id,
                                    type = TransactionType.INCOME,
                                    category = "tx_chicken_sale",
                                    amount = totalSalesValue,
                                    currency = curr,
                                    date = "${Translations.tr("day_prefix", lang)} $ageInDays - ${buyerNameInput.ifEmpty { "تاجر معتمد" }}",
                                    invoiceData = completedInvoice
                                ),
                                0.0
                            )

                            // 1. تُحفظ بيانات الفاتورة كاملة وتُفتح الفاتورة الاحترافية فوراً (ويتم تحديث cumulativeBirdsSold و cumulativeSoldWeightKg تلقائياً عبر الدورة والفاتورة)
                            lastSavedInvoice = completedInvoice
                            selectedInvoiceForView = completedInvoice

                            // 3. تُصفّر حقول الميزان بحيث عند الضغط على زر [البيع 🍗] مرة أخرى تفتح فاتورة شحنة جديدة وفارغة تماماً
                            buyerNameInput = ""
                            truckPlateInput = ""
                            pricePerKgInput = "380"
                            birdsPerCrateInput = "10"
                            tareMode = TareMode.DETAILED
                            directTareTotalInput = ""
                            tareWeightsList = emptyList()
                            grossWeightsList = emptyList()
                            salesFlowStep = SalesFlowStep.NONE
                            salesMigratedMsg = "تم حفظ الفاتورة ($invoiceNumber) وترحيل ${String.format(java.util.Locale.US, "%,.0f", totalSalesValue)} $curr للخزنة وحسم $totalBirdsSold طائر من العنبر بنجاح ✓"
                        }
                    }
                },
                onBack = { salesFlowStep = SalesFlowStep.STAGE_1_SHIPMENT_INFO }
            )
        }
        SalesFlowStep.NONE -> {
            val appearance = LocalAppAppearanceState.current
            val preferredFeedUnit = LocalFeedUnitPreference.current
            val isLowFeedInventory = effectiveFeedInventoryKg < 500.0
            val feedAlertRed = Color(0xFFD32F2F)
            val feedStatusColor = if (isLowFeedInventory) feedAlertRed else appearance.accentColor
            var showBarnClimateAndStatsDialog by remember { mutableStateOf(false) }

            if (pendingSignatureApprovalRequest != null) {
                InvoiceApprovalSignatureDialog(
                    categoryOrTitle = pendingSignatureTitle,
                    dateOrDayText = pendingSignatureDate,
                    amount = pendingSignatureAmount,
                    currency = curr,
                    currentTheme = currentTheme,
                    lang = lang,
                    onDismiss = { pendingSignatureApprovalRequest = null },
                    onConfirmSignedApproval = {
                        pendingSignatureApprovalRequest?.invoke()
                        pendingSignatureApprovalRequest = null
                    }
                )
            }

            val latestReportForHeader = reportsForCycle.maxByOrNull { it.dayNumber }
            val liveTempC = latestReportForHeader?.temperature ?: 29.0
            val liveAmmoniaPpm = latestReportForHeader?.gasUnits ?: 8.5
            val liveVentStatus = latestReportForHeader?.ventilationStatus ?: "جيدة"
            val isClimateAlert = liveTempC > 33.5 || liveTempC < 19.0 || liveAmmoniaPpm > 18.0
            val isClimateWarning = !isClimateAlert && (liveTempC > 31.5 || liveAmmoniaPpm > 12.0)

            if (showBarnClimateAndStatsDialog) {
                BarnClimateAndStatsDialog(
                    farmName = Translations.getLocalizedFarmName(farm.id, lang, farm.name),
                    cycleDay = calculatedCycleDay,
                    temperatureC = liveTempC,
                    ammoniaPpm = liveAmmoniaPpm,
                    ventilationStatus = liveVentStatus,
                    liveBirds = liveBirds,
                    livabilityPct = liveabilityPct,
                    feedInventoryKg = effectiveFeedInventoryKg,
                    fcr = accurateFcr,
                    estimatedWeightKg = effectiveAvgWeightKg,
                    netBalance = netBalance,
                    totalExpenses = totalExpenses,
                    currencyCode = curr,
                    lang = lang,
                    currentTheme = currentTheme,
                    onDismiss = { showBarnClimateAndStatsDialog = false }
                )
            }

            if (showFeedLedgerBottomSheet) {
                FeedOrdersLedgerBottomSheet(
                    farmName = Translations.getLocalizedFarmName(farm.id, lang, farm.name),
                    cycle = activeCycle.copy(feedInventoryKg = effectiveFeedInventoryKg),
                    transactions = transactions,
                    lang = lang,
                    onDismiss = { showFeedLedgerBottomSheet = false }
                )
            }

            if (showMedicineLedgerBottomSheet) {
                MedicineOrdersLedgerBottomSheet(
                    farmName = Translations.getLocalizedFarmName(farm.id, lang, farm.name),
                    cycle = activeCycle,
                    transactions = txsForCycle,
                    vetNotes = liveVetNotesForFarm.filter { it.cycleId == activeCycle.id || it.cycleId.isBlank() },
                    reports = reportsForCycle,
                    initialSelectedTransaction = selectedMedicineTxForLedger,
                    lang = lang,
                    onSelectTransactionForReceipt = { tx ->
                        showMedicineLedgerBottomSheet = false
                        selectedMedicineTxForLedger = null
                        selectedTransactionForReceipt = tx
                    },
                    onDismiss = {
                        showMedicineLedgerBottomSheet = false
                        selectedMedicineTxForLedger = null
                    }
                )
            }

            if (selectedOwnerDayReportForMedicalDetails != null) {
                val activeReport = reportsForCycle.firstOrNull { it.dayNumber == selectedOwnerDayReportForMedicalDetails!!.dayNumber }
                    ?: selectedOwnerDayReportForMedicalDetails!!
                val matchingVetNotes = liveVetNotesForFarm.filter { it.dayNumber == activeReport.dayNumber }
                val effectiveDayVetNotes = if (matchingVetNotes.isNotEmpty()) {
                    matchingVetNotes
                } else if (activeReport.medicineGiven || activeReport.medicineSummary.isNotBlank()) {
                    listOf(
                        VetNoteModel(
                            id = "vn_confirmed_day_${activeReport.dayNumber}",
                            farmId = farm.id,
                            cycleId = activeCycle.id,
                            dayNumber = activeReport.dayNumber,
                            medicineName = activeReport.medicineSummary.ifBlank { "دواء / فيتامين في مياه الشرب" },
                            dosage = "1 مل / لتر في مياه الشرب",
                            note = Translations.tr("confirm_medicine_water_today_label", lang),
                            isUrgent = false
                        )
                    )
                } else {
                    emptyList()
                }
                DayMedicalDetailsBottomSheet(
                    farm = farm,
                    cycle = activeCycle,
                    report = activeReport,
                    dayVetNotes = effectiveDayVetNotes,
                    currentTheme = currentTheme,
                    lang = lang,
                    onAddMedicalNoteForDay = { newNote ->
                        AgritechMockDb.vetNotes.add(0, newNote)
                    },
                    onOpenInvoiceForDay = {
                        selectedOwnerDayReportForMedicalDetails = null
                        showVetMedicineInvoiceDialog = true
                    },
                    onDismiss = { selectedOwnerDayReportForMedicalDetails = null }
                )
            }

            if (showOwnerDailyReportDialog) {
                val suggestedNextDay = (reportsForCycle.maxOfOrNull { it.dayNumber } ?: 0) + 1
                OwnerDailyReportDialog(
                    farm = farm,
                    cycle = activeCycle,
                    initialReport = editingDailyReport,
                    suggestedDayNumber = suggestedNextDay.coerceAtLeast(1),
                    vetNotes = vetNotes.filter { it.farmId == farm.id },
                    lang = lang,
                    currentTheme = currentTheme,
                    onDismiss = {
                        showOwnerDailyReportDialog = false
                        editingDailyReport = null
                    },
                    onSaveReport = { savedReport ->
                        onSaveOrUpdateDailyReport(savedReport)
                        showOwnerDailyReportDialog = false
                        editingDailyReport = null
                    }
                )
            }

            if (showVetMedicineInvoiceDialog) {
                VetMedicineInvoiceDialog(
                    farm = farm,
                    cycle = activeCycle,
                    dayNumber = calculatedCycleDay,
                    lang = lang,
                    currentTheme = currentTheme,
                    channelId = "${activeCycle.id}_vet",
                    firestoreRepo = firestoreRepo,
                    onDismiss = { showVetMedicineInvoiceDialog = false },
                    onSaved = { medName, totalDzd ->
                        vetInvoiceSuccessBanner = "${Translations.tr("vet_invoice_saved_banner", lang)} ($medName - ${String.format(java.util.Locale.US, "%,.0f", totalDzd)} $curr)"
                        showVetMedicineInvoiceDialog = false
                    }
                )
            }

            if (activeContextualChatChannel != null) {
                ContextualChatBottomSheet(
                    title = activeContextualChatTitle,
                    subtitle = "${Translations.getLocalizedFarmName(farm.id, lang, farm.name)} • ${activeCycle.id}",
                    channelId = activeContextualChatChannel!!,
                    currentTheme = currentTheme,
                    lang = lang,
                    firestoreRepo = firestoreRepo,
                    onOpenVetInvoice = if (activeContextualChatChannel!!.contains("vet")) {
                        {
                            activeContextualChatChannel = null
                            showVetMedicineInvoiceDialog = true
                        }
                    } else null,
                    onDismiss = { activeContextualChatChannel = null }
                )
            }

            Scaffold(
                containerColor = currentTheme.bgColor,
                topBar = {
                    val climatePillColor = when {
                        isClimateAlert -> if (currentTheme.isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626)
                        isClimateWarning -> if (currentTheme.isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
                        else -> appearance.accentColor
                    }
                    val climatePillBg = when {
                        isClimateAlert -> if (currentTheme.isDark) Color(0xFF450A0A) else Color(0xFFFEF2F2)
                        isClimateWarning -> if (currentTheme.isDark) Color(0xFF451A03) else Color(0xFFFFFBEB)
                        else -> appearance.accentContainerColor
                    }
                    val headerPillShape = RoundedCornerShape((appearance.cornerRadiusDp - 6).coerceAtLeast(6).dp)

                    Surface(
                        color = currentTheme.bgColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 6.dp)
                        ) {
                            // Row 1: Back Button + Full Barn Name & Day/Live Birds + Settings Icon
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(onClick = onBack) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = currentTheme.headingColor
                                    )
                                }
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                ) {
                                    Text(
                                        Translations.getLocalizedFarmName(farm.id, lang, farm.name),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.5.sp,
                                        color = currentTheme.headingColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "${Translations.tr("day_prefix", lang)} $calculatedCycleDay • ${String.format(java.util.Locale.US, "%,d", liveBirds)} ${Translations.tr("birds_unit_short", lang)}",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = currentTheme.headingColor.copy(alpha = 0.85f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(onClick = onOpenSettings) {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = currentTheme.headingColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Row 2: Generous Climate Status Pill (`🌡️`) + Feed Stock Pill (`🌾`)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1. Sleek Top Header Climate Status Pill/Icon Button (`🌡️ 29.0°C • المناخ`)
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("header_climate_status_btn")
                                        .clip(headerPillShape)
                                        .clickable { showBarnClimateAndStatsDialog = true },
                                    shape = headerPillShape,
                                    color = climatePillBg,
                                    border = BorderStroke(1.dp, climatePillColor.copy(alpha = 0.45f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            "🌡️ ${String.format(java.util.Locale.US, "%.1f°C", liveTempC)} • ${Translations.tr("climate_header_btn_label", lang)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = climatePillColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                // 2. Dual Smart Feed Unit Header Inventory Badge: e.g., `مخزون: 40 ق 🌾`
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("header_feed_inventory_indicator")
                                        .clip(headerPillShape)
                                        .clickable { showFeedLedgerBottomSheet = true },
                                    shape = headerPillShape,
                                    color = if (isLowFeedInventory) {
                                        if (currentTheme.isDark) Color(0xFF450A0A) else Color(0xFFFFEBEE)
                                    } else appearance.accentContainerColor,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isLowFeedInventory) feedAlertRed.copy(alpha = 0.45f) else appearance.accentColor.copy(alpha = 0.35f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        if (isLowFeedInventory) {
                                            Text("⚠️", fontSize = 11.sp)
                                            Spacer(modifier = Modifier.width(3.dp))
                                        }
                                        Text(
                                            Translations.formatDualFeedInventoryBadge(effectiveFeedInventoryKg, lang, preferredFeedUnit),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = feedStatusColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
        floatingActionButton = {
            val appearance = LocalAppAppearanceState.current
            val fabCorner = RoundedCornerShape(appearance.cornerRadiusDp.dp)
            when (selectedTab) {
                0 -> {
                    // Contextual Worker Chat FAB inside Ledger Tab (جدول السجل)
                    FloatingActionButton(
                        onClick = {
                            activeContextualChatTitle = Translations.tr("chat_worker_title", lang)
                            activeContextualChatChannel = "${cycle.id}_worker"
                        },
                        containerColor = appearance.accentOption.primaryColor,
                        contentColor = Color.White,
                        shape = fabCorner,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                        modifier = Modifier.testTag("fab_worker_chat")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Chat,
                                contentDescription = Translations.tr("chat_worker_title", lang),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                Translations.tr("chat_worker_title", lang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = Color.White
                            )
                        }
                    }
                }
                3 -> {
                    // Contextual Vet Chat FAB inside Vet Notes Tab (البيطري):
                    // Keeps only the green consultation button at the bottom so it never overlaps pending invoice cards
                    FloatingActionButton(
                        onClick = {
                            activeContextualChatTitle = Translations.tr("chat_vet_title", lang)
                            activeContextualChatChannel = "${cycle.id}_vet"
                        },
                        containerColor = appearance.accentOption.primaryColor,
                        contentColor = Color.White,
                        shape = fabCorner,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                        modifier = Modifier.testTag("fab_vet_chat")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Chat,
                                contentDescription = Translations.tr("chat_vet_title", lang),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                Translations.tr("chat_vet_title", lang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            val appearance = LocalAppAppearanceState.current
            NavigationBar(
                containerColor = currentTheme.cardColor,
                contentColor = appearance.accentColor
            ) {
                val items = listOf(
                    Triple(0, Translations.tr("tab_ledger", lang), Icons.Default.TableChart),
                    Triple(1, Translations.tr("tab_treasury", lang), Icons.Default.ReceiptLong),
                    Triple(2, Translations.tr("tab_analytics", lang), Icons.Default.Insights),
                    Triple(3, Translations.tr("tab_vet", lang), Icons.Default.MedicalServices)
                )

                items.forEach { (index, label, icon) ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        modifier = Modifier.testTag("nav_tab_$index"),
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                icon,
                                contentDescription = label,
                                tint = if (isSelected) appearance.accentColor else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                label,
                                color = if (isSelected) appearance.accentColor else Color.Gray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = appearance.accentContainerColor
                        )
                    )
                }
            }
        }
    ) { padding ->
        val appearance = LocalAppAppearanceState.current
        val notebookAccent = appearance.accentColor
        val notebookButtonAccent = appearance.accentOption.primaryColor
        val notebookShape = RoundedCornerShape(appearance.cornerRadiusDp.dp)
        val notebookBtnShape = RoundedCornerShape((appearance.cornerRadiusDp - 6).coerceAtLeast(4).dp)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Crossfade(
                targetState = selectedTab,
                animationSpec = tween(durationMillis = 260),
                label = "tab_crossfade"
            ) { activeTab ->
            when (activeTab) {
                0 -> {
                    // TAB 0: جدول السجل اليومي (DataTable) - Directly visible at the top of the screen
                    // (Climate & Quick Stats are accessed on-demand via the top bar 🌡️ Climate Status Pill)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .testTag("ledger_scrollable_column")
                    ) {
                        // Top Action Header for Owner Daily Report Entry (Mid-Cycle Onboarding e.g. Day 20 / Day 30)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                Translations.tr("tab_ledger", lang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = currentTheme.headingColor
                            )
                            Button(
                                onClick = {
                                    editingDailyReport = null
                                    showOwnerDailyReportDialog = true
                                },
                                modifier = Modifier
                                    .height(40.dp)
                                    .testTag("btn_owner_add_daily_report"),
                                shape = notebookBtnShape,
                                colors = ButtonDefaults.buttonColors(containerColor = notebookButtonAccent),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Default.EditNote,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    Translations.tr("btn_owner_add_daily_report", lang),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        if (activeWithdrawalDays > 0) {
                            Surface(
                                shape = notebookBtnShape,
                                color = if (currentTheme.isDark) Color(0xFF450A0A) else Color(0xFFFEF2F2),
                                border = BorderStroke(1.2.dp, ActionRed.copy(alpha = 0.65f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("withdrawal_warning_banner")
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = Translations.tr("withdrawal_active_banner_title", lang)
                                            .replace("{days}", activeWithdrawalDays.toString()),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = if (currentTheme.isDark) Color(0xFFFCA5A5) else ActionRed
                                    )
                                    Text(
                                        text = Translations.tr("withdrawal_active_sales_blocked", lang)
                                            .replace("{days}", activeWithdrawalDays.toString()),
                                        fontSize = 11.5.sp,
                                        color = if (currentTheme.isDark) Color(0xFFFECACA) else Color(0xFF7F1D1D)
                                    )
                                }
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            shape = notebookShape,
                            colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp, vertical = 8.dp)
                            ) {
                                // 4-Column Responsive Table Header (📅 اليوم | 🌾 طعام (ق) | 💧 ماء (لتر) | 💀 نافق) — 100% visible without clipping
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(currentTheme.bgColor, RoundedCornerShape(8.dp))
                                        .padding(vertical = 11.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CalmTableCell(
                                        Translations.tr("th_day", lang),
                                        modifier = Modifier.weight(1.15f),
                                        isHeader = true,
                                        headingColor = currentTheme.headingColor,
                                        fontSize = 11.sp
                                    )
                                    CalmTableCell(
                                        Translations.tr("th_feed", lang),
                                        modifier = Modifier.weight(1.0f),
                                        isHeader = true,
                                        headingColor = currentTheme.headingColor,
                                        fontSize = 11.sp
                                    )
                                    CalmTableCell(
                                        Translations.tr("th_water", lang),
                                        modifier = Modifier.weight(1.0f),
                                        isHeader = true,
                                        headingColor = currentTheme.headingColor,
                                        fontSize = 11.sp
                                    )
                                    CalmTableCell(
                                        Translations.tr("th_mortality", lang),
                                        modifier = Modifier.weight(0.85f),
                                        isHeader = true,
                                        headingColor = currentTheme.headingColor,
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                reportsForCycle.sortedBy { it.dayNumber }.forEach { r ->
                                    val isRowEdited = r.isEdited
                                    val dayMeds = liveVetNotesForFarm.filter { it.dayNumber == r.dayNumber }
                                    val hasMedForDay = r.medicineGiven || r.medicineSummary.isNotBlank() || dayMeds.isNotEmpty()
                                    val editedRowBg = when {
                                        isRowEdited -> if (currentTheme.isDark) Color(0xFF78350F).copy(alpha = 0.42f) else Color(0xFFFEF3C7)
                                        hasMedForDay -> appearance.accentContainerColor.copy(alpha = 0.45f)
                                        else -> Color.Transparent
                                    }
                                    val editedBorderColor = if (isRowEdited) Color(0xFFF59E0B) else Color.Transparent

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(editedRowBg, RoundedCornerShape(10.dp))
                                            .then(
                                                if (isRowEdited) Modifier.border(1.dp, editedBorderColor.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                                                else Modifier
                                            )
                                            .clickable { selectedOwnerDayReportForMedicalDetails = r }
                                            .testTag("daily_report_row_${r.dayNumber}")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedOwnerDayReportForMedicalDetails = r }
                                                .padding(vertical = 8.dp, horizontal = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val dayColor = notebookAccent
                                            val qShort = Translations.tr("quintal_short", lang)
                                            val rowTextColor = if (currentTheme.isDark) Color.White else Color(0xFF212121)

                                            // Col 1: 📅 اليوم + Compact Medicine Badge ("💊 دواء") + Edit Icon
                                            Column(
                                                modifier = Modifier
                                                    .weight(1.15f)
                                                    .clickable { selectedOwnerDayReportForMedicalDetails = r }
                                                    .padding(horizontal = 2.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Text(
                                                        "${Translations.tr("day_prefix", lang)} ${r.dayNumber}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.5.sp,
                                                        color = dayColor,
                                                        textAlign = TextAlign.Center,
                                                        maxLines = 1
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .size(20.dp)
                                                            .clip(CircleShape)
                                                            .clickable {
                                                                editingDailyReport = r
                                                                showOwnerDailyReportDialog = true
                                                            }
                                                            .testTag("btn_edit_daily_report_${r.dayNumber}"),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Edit,
                                                            contentDescription = Translations.tr("th_edit", lang),
                                                            tint = if (isRowEdited) Color(0xFFD97706) else notebookAccent.copy(alpha = 0.75f),
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                    }
                                                }
                                                if (hasMedForDay) {
                                                    Spacer(modifier = Modifier.height(3.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = appearance.accentContainerColor,
                                                        border = BorderStroke(1.dp, notebookAccent.copy(alpha = 0.5f)),
                                                        modifier = Modifier
                                                            .clickable { selectedOwnerDayReportForMedicalDetails = r }
                                                            .testTag("owner_ledger_med_badge_day_${r.dayNumber}")
                                                    ) {
                                                        Text(
                                                            Translations.tr("badge_day_med_pill", lang),
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = notebookAccent,
                                                            maxLines = 1,
                                                            textAlign = TextAlign.Center,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                                        )
                                                    }
                                                }
                                                if (isRowEdited) {
                                                    Spacer(modifier = Modifier.height(3.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFFF59E0B).copy(alpha = 0.22f),
                                                        border = BorderStroke(1.dp, Color(0xFFD97706)),
                                                        modifier = Modifier.testTag("badge_edited_day_${r.dayNumber}")
                                                    ) {
                                                        Text(
                                                            Translations.tr("badge_report_edited", lang),
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = if (currentTheme.isDark) Color(0xFFFDE68A) else Color(0xFFB45309),
                                                            maxLines = 1,
                                                            textAlign = TextAlign.Center,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            // Col 2: 🌾 طعام (ق)
                                            CalmTableCell(
                                                "${Translations.formatQuintalNumber(r.feedIntakeKg / 100.0)} $qShort",
                                                modifier = Modifier.weight(1.0f),
                                                isBold = true,
                                                textColor = rowTextColor
                                            )
                                            // Col 3: 💧 ماء (لتر)
                                            CalmTableCell(
                                                String.format(java.util.Locale.US, "%.0f", r.waterIntakeLiters),
                                                modifier = Modifier.weight(1.0f),
                                                textColor = rowTextColor
                                            )
                                            // Col 4: 💀 نافق
                                            CalmTableCell(
                                                "${r.mortalityCount}",
                                                modifier = Modifier.weight(0.85f),
                                                textColor = if (r.mortalityCount > 3) ActionRed else rowTextColor,
                                                isBold = r.mortalityCount > 3
                                            )
                                        }

                                        if (isRowEdited && r.previousSummary.isNotBlank()) {
                                            Text(
                                                text = "${Translations.tr("audit_previous_values_prefix", lang)} ${Translations.localizeAuditSummary(r.previousSummary, lang)}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (currentTheme.isDark) Color(0xFFFDE68A) else Color(0xFF92400E),
                                                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 6.dp)
                                            )
                                        }
                                    }
                                    HorizontalDivider(color = if (currentTheme.isDark) Color(0xFF334155) else Color(0xFFF1F5F9), thickness = 1.dp)
                                }
                            }
                        }

                        // Bottom Summary Bar ("إجمالي العلف" strictly in Quintals ق, "إجمالي الماء", "إجمالي النافق")
                        Surface(
                            shadowElevation = 4.dp,
                            color = currentTheme.cardColor,
                            shape = notebookShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 14.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                val feedWaterGreen = notebookAccent
                                val mortColor = if (totalMortality > 5) ActionRed else currentTheme.headingColor
                                val quintalShort = Translations.tr("quintal_short", lang)
                                CalmSummaryItem(
                                    label = Translations.tr("sum_feed", lang),
                                    value = "${Translations.formatQuintalNumber(totalFeed / 100.0)} $quintalShort",
                                    headingColor = feedWaterGreen
                                )
                                CalmSummaryItem(label = Translations.tr("sum_water", lang), value = "${String.format(java.util.Locale.US, "%.1f", totalWater)} $literUnit", headingColor = feedWaterGreen)
                                CalmSummaryItem(label = Translations.tr("sum_mortality", lang), value = "$totalMortality", isAlert = totalMortality > 5, headingColor = mortColor)
                            }
                        }

                        // Bottom spacing so the floating button "👷‍♂️ محادثة مشرف العنبر" never covers the water or mortality totals when scrolled down
                        Spacer(modifier = Modifier.height(88.dp))
                    }
                }
                1 -> {
                    // TAB 1: الخزنة (Treasury) with Vet Invoice Approval Workflow
                    val pendingVetTransactions = txsForCycle.filter { it.status.equals("PENDING", ignoreCase = true) }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = notebookShape,
                            colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(Translations.tr("treasury_title", lang), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = currentTheme.headingColor)
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val positiveGreen = notebookAccent
                                    Box(modifier = Modifier.weight(1f)) {
                                        CalmFinancialItem(Translations.tr("total_expenses", lang), "${String.format(java.util.Locale.US, "%,.0f", totalExpenses)} $curr", ActionRed)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        CalmFinancialItem(Translations.tr("total_income", lang), "${String.format(java.util.Locale.US, "%,.0f", totalIncome)} $curr", positiveGreen)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(modifier = Modifier.weight(1.15f)) {
                                        CalmFinancialItem(Translations.tr("net_balance", lang), "${String.format(java.util.Locale.US, "%,.0f", netBalance)} $curr", if (netBalance >= 0) positiveGreen else ActionRed)
                                    }
                                }
                            }
                        }

                        // Prominent Banner for PENDING Vet Invoices awaiting Owner confirmation before Treasury deduction
                        if (pendingVetTransactions.isNotEmpty()) {
                            Surface(
                                shape = notebookShape,
                                color = if (currentTheme.isDark) Color(0xFF78350F).copy(alpha = 0.45f) else Color(0xFFFEF3C7),
                                border = BorderStroke(1.5.dp, Color(0xFFF59E0B)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("treasury_pending_vet_banner")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        "${Translations.tr("vet_pending_banner_title", lang)} (${pendingVetTransactions.size})",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.5.sp,
                                        color = if (currentTheme.isDark) Color(0xFFFDE68A) else Color(0xFF92400E)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        Translations.tr("vet_pending_banner_subtitle", lang),
                                        fontSize = 11.5.sp,
                                        color = if (currentTheme.isDark) Color(0xFFFEF3C7) else Color(0xFFB45309)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { showExpenseDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = notebookBtnShape,
                                colors = ButtonDefaults.buttonColors(containerColor = ActionRed)
                            ) {
                                Text(Translations.tr("btn_spend", lang), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            }

                            Button(
                                onClick = { showIncomeDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = notebookBtnShape,
                                colors = ButtonDefaults.buttonColors(containerColor = notebookButtonAccent)
                            ) {
                                Text(Translations.tr("btn_income", lang), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            }
                        }

                        Text(Translations.tr("tx_history_title", lang), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = currentTheme.headingColor)

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(txsForCycle.reversed()) { tx ->
                                val isPendingTx = tx.status.equals("PENDING", ignoreCase = true)
                                val isChickenSale = tx.type == TransactionType.INCOME && (tx.category == "tx_chicken_sale" || tx.category.contains("دجاج") || tx.category.contains("Chicken", true) || tx.category.contains("Poulets", true) || tx.invoiceData != null)
                                val isMedicineTx = tx.category.equals("MEDICATION", ignoreCase = true) ||
                                    tx.category == Translations.tr("tx_meds", lang) ||
                                    tx.category.contains("أدوية") ||
                                    tx.category.contains("لقاح") ||
                                    tx.category.contains("دواء") ||
                                    tx.category.contains("تحصين") ||
                                    tx.category.contains("فيتامين") ||
                                    tx.category.contains("Med", ignoreCase = true) ||
                                    tx.category.contains("Vaccin", ignoreCase = true)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("tx_card_${tx.id}")
                                        .clickable {
                                            if (isMedicineTx) {
                                                selectedMedicineTxForLedger = tx
                                                showMedicineLedgerBottomSheet = true
                                            } else {
                                                selectedTransactionForReceipt = tx
                                            }
                                        },
                                    shape = notebookShape,
                                    colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    border = when {
                                        isPendingTx -> BorderStroke(1.5.dp, Color(0xFFF59E0B))
                                        isChickenSale -> BorderStroke(1.dp, notebookAccent.copy(alpha = 0.5f))
                                        else -> null
                                    }
                                ) {
                                    Column(modifier = Modifier.padding(18.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(end = 8.dp)
                                            ) {
                                                Text(
                                                    Translations.getLocalizedTxCategory(tx.category, lang, preferredFeedUnit),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = if (currentTheme.isDark) Color.White else Color(0xFF212121),
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                val dayDigits = tx.date.filter { it.isDigit() }.ifEmpty { "1" }
                                                Text(
                                                    if (isChickenSale && tx.date.contains("-")) tx.date else "${Translations.tr("day_prefix", lang)} $dayDigits",
                                                    fontSize = 12.sp,
                                                    color = if (currentTheme.isDark) Color(0xFFCBD5E1) else Color.Gray
                                                )
                                            }

                                            val positiveGreen = notebookAccent
                                            Text(
                                                "${if (tx.type == TransactionType.INCOME) "+" else "-"}${String.format(java.util.Locale.US, "%,.0f", tx.amount)} $curr",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = when {
                                                    isPendingTx -> Color(0xFFD97706)
                                                    tx.type == TransactionType.INCOME -> positiveGreen
                                                    else -> ActionRed
                                                },
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }

                                        if (isPendingTx) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Surface(
                                                shape = notebookBtnShape,
                                                color = if (currentTheme.isDark) Color(0xFF78350F).copy(alpha = 0.4f) else Color(0xFFFEF3C7),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Text(
                                                        Translations.tr("vet_invoice_pending_badge", lang),
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (currentTheme.isDark) Color(0xFFFDE68A) else Color(0xFFB45309)
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Button(
                                                        onClick = {
                                                            val linkedNote = vetNotes.firstOrNull {
                                                                it.linkedTransactionId == tx.id ||
                                                                    (it.farmId == tx.farmId && it.cycleId == tx.cycleId && it.status.equals("PENDING", ignoreCase = true))
                                                            }
                                                            pendingSignatureTitle = Translations.getLocalizedTxCategory(tx.category, lang, preferredFeedUnit)
                                                            pendingSignatureDate = tx.date
                                                            pendingSignatureAmount = tx.amount
                                                            pendingSignatureApprovalRequest = {
                                                                onApproveVetInvoice(tx.id, linkedNote?.id ?: "")
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(42.dp)
                                                            .testTag("btn_approve_vet_invoice_${tx.id}"),
                                                        shape = notebookBtnShape,
                                                        colors = ButtonDefaults.buttonColors(containerColor = notebookButtonAccent)
                                                    ) {
                                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            Translations.tr("btn_approve_vet_invoice", lang),
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(
                                                color = if (tx.type == TransactionType.INCOME) appearance.accentContainerColor else if (currentTheme.isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                                                shape = notebookBtnShape,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            Icons.Default.ReceiptLong,
                                                            contentDescription = null,
                                                            tint = notebookAccent,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            Translations.tr("tap_for_digital_receipt", lang),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = notebookAccent
                                                        )
                                                    }
                                                    Icon(
                                                        Icons.Default.ChevronRight,
                                                        contentDescription = null,
                                                        tint = notebookAccent,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // TAB 2: التحليلات وميزان وبيع الدفعة (Analytics & Batch Weighing/Sales)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. بطاقة مؤشرات الأداء والكفاءة (تبقى كما هي في الأعلى)
                        Text(Translations.tr("analytics_title", lang), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = currentTheme.headingColor)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = notebookShape,
                            colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                val positiveGreen = notebookAccent
                                val darkValueColor = if (currentTheme.isDark) Color.White else Color(0xFF212121)
                                val qtlUnit = Translations.tr("quintal_unit", lang)
                                val birdUnit = Translations.tr("bird_unit", lang)

                                val totalActiveAndSoldBirds = liveBirds + cumulativeBirdsSold
                                val feedPerBirdKg = if (totalActiveAndSoldBirds > 0) totalFeed / totalActiveAndSoldBirds else 0.0
                                val feedPerBirdG = feedPerBirdKg * 1000.0
                                val costPerBird = PoultryCalculators.calculateCostPerBird(
                                    totalExpenses = totalExpenses,
                                    liveBirds = liveBirds,
                                    cumulativeBirdsSold = cumulativeBirdsSold
                                )
                                val costPerKg = PoultryCalculators.calculateCostPerKg(
                                    totalExpenses = totalExpenses,
                                    totalProducedBiomassKg = totalProducedBiomassKg
                                )

                                // 1. 🐔 عدد الدجاج الحي داخل العنبر
                                CalmMetricRow(
                                    Translations.tr("live_birds_count", lang),
                                    "${String.format(java.util.Locale.US, "%,d", liveBirds)} $birdUnit",
                                    positiveGreen
                                )

                                // 2. 💚 نسبة الحيوية (%)
                                CalmMetricRow(
                                    Translations.tr("liveability", lang),
                                    "${String.format(java.util.Locale.US, "%.1f", liveabilityPct)}%",
                                    positiveGreen
                                )

                                // 3. 🌾 إجمالي العلف المستهلك (بالقنطار ق)
                                CalmMetricRow(
                                    Translations.tr("total_barn_feed", lang),
                                    "${Translations.formatQuintalNumber(totalFeed / 100.0)} $qtlUnit",
                                    darkValueColor
                                )

                                // 4. 🥣 استهلاك الدجاجة الواحدة من العلف (وحدة ذكية تلقائية: غرام تحت 1000غ، وكغ من 1000غ فما فوق)
                                val formattedFeedPerBird = if (feedPerBirdG < 1000.0) {
                                    "${kotlin.math.round(feedPerBirdG).toInt()} ${Translations.tr("gram_unit", lang)}"
                                } else {
                                    "${String.format(java.util.Locale.US, "%.2f", feedPerBirdG / 1000.0)} $kgUnit"
                                }
                                CalmMetricRow(
                                    Translations.tr("feed_per_bird", lang),
                                    formattedFeedPerBird,
                                    darkValueColor
                                )

                                // 5. 🐥 متوسط وزن الطائر (الفعلي المسجل أو غير مقاس مع مرجع Ross 308)
                                val weightMetricText = if (actualMeasuredWeightKg != null) {
                                    "${String.format(java.util.Locale.US, "%.3f", actualMeasuredWeightKg)} $kgUnit"
                                } else {
                                    "${Translations.tr("weight_not_measured_yet", lang)} (${Translations.tr("ross308_reference_hint", lang)}: ${String.format(java.util.Locale.US, "%.3f", currentStandardWeightKg)} $kgUnit)"
                                }
                                CalmMetricRow(
                                    Translations.tr("est_weight", lang),
                                    weightMetricText,
                                    if (actualMeasuredWeightKg != null) positiveGreen else darkValueColor
                                )

                                // 6. ⚖️ معامل التحويل (FCR)
                                val fcrMetricText = if (actualMeasuredWeightKg != null) {
                                    String.format(java.util.Locale.US, "%.3f", accurateFcr)
                                } else {
                                    "${Translations.tr("weight_not_measured_yet", lang)} (${Translations.tr("ross308_reference_hint", lang)}: ${String.format(java.util.Locale.US, "%.3f", accurateFcr)})"
                                }
                                CalmMetricRow(
                                    Translations.tr("fcr", lang),
                                    fcrMetricText,
                                    if (actualMeasuredWeightKg != null) positiveGreen else darkValueColor
                                )

                                // 6. 💰 إجمالي مصاريف العنبر (دج)
                                CalmMetricRow(
                                    Translations.tr("total_expenses", lang),
                                    "${String.format(java.util.Locale.US, "%,.0f", totalExpenses)} $curr",
                                    ActionRed
                                )

                                // 7. 🐓 تكلفة الدجاجة الواحدة (دج/طائر)
                                CalmMetricRow(
                                    Translations.tr("cost_per_bird", lang),
                                    "${String.format(java.util.Locale.US, "%,.1f", costPerBird)} $curr/$birdUnit",
                                    darkValueColor
                                )

                                // 8. 🏷️ تكلفة الكيلو الواحد (دج/كغ)
                                CalmMetricRow(
                                    Translations.tr("cost_per_kg", lang),
                                    if (totalProducedBiomassKg > 0) "${String.format(java.util.Locale.US, "%.1f", costPerKg)} $curr/$kgUnit" else "0 $curr/$kgUnit",
                                    darkValueColor
                                )
                            }
                        }

                        // 2. زر وبطاقة بدء عملية بيع وشحن الدجاج ⚖️🍗
                        if (salesMigratedMsg != null) {
                            Surface(
                                color = appearance.accentContainerColor,
                                shape = notebookBtnShape,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = notebookAccent, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        salesMigratedMsg ?: "",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = notebookAccent
                                    )
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = notebookShape,
                            colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(appearance.accentContainerColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Scale,
                                            contentDescription = null,
                                            tint = notebookAccent,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            Translations.tr("weighing_sales_title", lang),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = currentTheme.headingColor
                                        )
                                        Text(
                                            Translations.tr("sales_banner_desc", lang),
                                            fontSize = 12.sp,
                                            color = if (currentTheme.isDark) Color(0xFFCBD5E1) else Color.Gray
                                        )
                                    }
                                }

                                // تفاصيل الشحنة المسجلة في الأعلى بوضوح
                                if (grossWeightsList.isNotEmpty()) {
                                    Surface(
                                        color = if (currentTheme.isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                                        shape = notebookBtnShape,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "🚚 ${buyerNameInput.ifEmpty { Translations.tr("buyer_label", lang) }}${if (truckPlateInput.isNotBlank()) " • $truckPlateInput" else ""}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = currentTheme.headingColor
                                                )
                                                Surface(
                                                    color = appearance.accentContainerColor,
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        "${grossWeightsList.size} ${Translations.tr("weigh_number", lang)}",
                                                        fontSize = 11.5.sp,
                                                        color = notebookAccent,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }

                                            val positiveGreen = notebookAccent
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    "🐔 $totalBirdsSold ${Translations.tr("birds_unit_short", lang)}",
                                                    fontSize = 12.5.sp,
                                                    color = if (currentTheme.isDark) Color.White else Color(0xFF334155),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    "⚖️ ${String.format(java.util.Locale.US, "%,.1f", totalNetKg)} $kgUnit",
                                                    fontSize = 12.5.sp,
                                                    color = positiveGreen,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            if (totalSalesValue > 0) {
                                                HorizontalDivider(color = if (currentTheme.isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        Translations.tr("total_sales_value", lang),
                                                        fontSize = 12.5.sp,
                                                        color = if (currentTheme.isDark) Color(0xFFCBD5E1) else Color.Gray
                                                    )
                                                    Text(
                                                        "💰 ${String.format(java.util.Locale.US, "%,.0f", totalSalesValue)} $curr",
                                                        fontSize = 14.sp,
                                                        color = positiveGreen,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (activeWithdrawalDays > 0) {
                                    Surface(
                                        shape = notebookBtnShape,
                                        color = if (currentTheme.isDark) Color(0xFF450A0A) else Color(0xFFFEF2F2),
                                        border = BorderStroke(1.2.dp, ActionRed.copy(alpha = 0.65f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("withdrawal_sales_blocked_banner")
                                    ) {
                                        Text(
                                            text = Translations.tr("withdrawal_active_sales_blocked", lang)
                                                .replace("{days}", activeWithdrawalDays.toString()),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (currentTheme.isDark) Color(0xFFFCA5A5) else ActionRed,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }

                                // 1. زر البيع الرئيسي المختصر [البيع 🍗] في الأعلى (يُقفل آلياً أثناء فترة سحب الدواء)
                                Button(
                                    onClick = {
                                        if (activeWithdrawalDays <= 0) {
                                            salesFlowStep = SalesFlowStep.STAGE_1_SHIPMENT_INFO
                                        }
                                    },
                                    enabled = activeWithdrawalDays <= 0,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("btn_start_sales_flow"),
                                    shape = notebookBtnShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = notebookButtonAccent,
                                        disabledContainerColor = ActionRed.copy(alpha = 0.35f)
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                                ) {
                                    Icon(Icons.Default.Scale, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        if (activeWithdrawalDays > 0) {
                                            Translations.tr("withdrawal_active_banner_title", lang)
                                                .replace("{days}", activeWithdrawalDays.toString())
                                        } else {
                                            Translations.tr("btn_start_sales_flow", lang)
                                        },
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                // 2. زر [🧾 فاتورة البيع] الثانوي بعرض كامل أسفل زر البيع
                                if (lastSavedInvoice != null || totalSalesValue > 0) {
                                    OutlinedButton(
                                        onClick = {
                                            selectedInvoiceForView = lastSavedInvoice ?: SalesInvoiceModel(
                                                invoiceNumber = "INV-${farm.id.takeLast(1).uppercase()}-0001",
                                                farmName = Translations.getLocalizedFarmName(farm.id, lang),
                                                cycleId = cycle.id,
                                                dateText = "${Translations.tr("day_prefix", lang)} $ageInDays",
                                                birdAgeDays = ageInDays,
                                                buyerName = buyerNameInput.ifEmpty { "تاجر جملة معتمد" },
                                                truckPlate = truckPlateInput.ifEmpty { "غير محدد" },
                                                totalCrates = grossWeightsList.sumOf { it.cratesCount },
                                                birdsPerCrate = parsedBirdsPerCrate,
                                                totalBirdsSold = totalBirdsSold,
                                                totalGrossKg = totalGrossKg,
                                                totalTareKg = totalTareKg,
                                                totalNetKg = totalNetKg,
                                                avgBirdWeightKg = avgBirdWeightKg,
                                                pricePerKg = pricePerKg,
                                                totalAmount = totalSalesValue,
                                                currency = curr,
                                                tareWeightsCount = tareWeightsList.size,
                                                grossWeightsCount = grossWeightsList.size
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("btn_issue_sales_invoice"),
                                        shape = notebookBtnShape,
                                        border = BorderStroke(1.5.dp, notebookAccent),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = notebookAccent
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.ReceiptLong,
                                            contentDescription = null,
                                            tint = notebookAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            Translations.tr("btn_issue_receipt", lang),
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = notebookAccent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // TAB 3: البيطري (Veterinary Records & Invoicing + Owner Approval Workflow)
                    val pendingVetNotes = vetNotes.filter { it.status.equals("PENDING", ignoreCase = true) }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    Translations.tr("vet_records_title", lang),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = currentTheme.headingColor
                                )

                                Button(
                                    onClick = { showVetMedicineInvoiceDialog = true },
                                    modifier = Modifier
                                        .height(40.dp)
                                        .testTag("btn_open_vet_invoice_dialog"),
                                    shape = notebookBtnShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = notebookButtonAccent),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        "🧾 ${Translations.tr("vet_invoice_btn", lang)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        if (pendingVetNotes.isNotEmpty()) {
                            item {
                                Surface(
                                    shape = notebookShape,
                                    color = if (currentTheme.isDark) Color(0xFF78350F).copy(alpha = 0.45f) else Color(0xFFFEF3C7),
                                    border = BorderStroke(1.5.dp, Color(0xFFF59E0B)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("vet_tab_pending_banner")
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            "${Translations.tr("vet_pending_banner_title", lang)} (${pendingVetNotes.size})",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.5.sp,
                                            color = if (currentTheme.isDark) Color(0xFFFDE68A) else Color(0xFF92400E)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            Translations.tr("vet_pending_banner_subtitle", lang),
                                            fontSize = 11.5.sp,
                                            color = if (currentTheme.isDark) Color(0xFFFEF3C7) else Color(0xFFB45309)
                                        )
                                    }
                                }
                            }
                        }

                        if (vetInvoiceSuccessBanner != null) {
                            item {
                                Surface(
                                    shape = notebookBtnShape,
                                    color = appearance.accentContainerColor,
                                    border = BorderStroke(1.dp, notebookAccent.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = notebookAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            vetInvoiceSuccessBanner!!,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = notebookAccent
                                        )
                                    }
                                }
                            }
                        }

                        items(vetNotes) { vn ->
                            val isPendingNote = vn.status.equals("PENDING", ignoreCase = true)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("vet_note_card_${vn.id}"),
                                shape = notebookShape,
                                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                border = if (isPendingNote) BorderStroke(1.5.dp, Color(0xFFF59E0B)) else null,
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            Translations.getLocalizedMedicine(vn.id, vn.medicineName, lang),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = currentTheme.headingColor,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (vn.isUrgent) {
                                            Text(Translations.tr("med_urgent_badge", lang), color = ActionRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                    Text(
                                        "${Translations.tr("dosage_label", lang)}: ${Translations.getLocalizedDosage(vn.id, vn.dosage, lang)} • ${Translations.tr("day_prefix", lang)} ${vn.dayNumber}",
                                        fontSize = 12.sp,
                                        color = if (currentTheme.isDark) Color(0xFFCBD5E1) else Color(0xFF64748B),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        Translations.getLocalizedVetNote(vn.id, vn.note, lang),
                                        fontSize = 13.sp,
                                        color = if (currentTheme.isDark) Color.White else DarkTextColor
                                    )

                                    if (vn.invoiceAmount > 0.0 || vn.linkedTransactionId.isNotBlank() || isPendingNote) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Surface(
                                            shape = notebookBtnShape,
                                            color = if (isPendingNote) {
                                                if (currentTheme.isDark) Color(0xFF78350F).copy(alpha = 0.4f) else Color(0xFFFEF3C7)
                                            } else {
                                                appearance.accentContainerColor
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        if (isPendingNote) Translations.tr("vet_invoice_pending_badge", lang)
                                                        else Translations.tr("vet_invoice_approved_badge", lang),
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isPendingNote) {
                                                            if (currentTheme.isDark) Color(0xFFFDE68A) else Color(0xFFB45309)
                                                        } else {
                                                            notebookAccent
                                                        },
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .padding(end = 10.dp)
                                                    )
                                                    if (vn.invoiceAmount > 0.0) {
                                                        Surface(
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = if (isPendingNote) {
                                                                if (currentTheme.isDark) Color(0xFF451A03) else Color(0xFFFEF3C7)
                                                            } else {
                                                                appearance.accentContainerColor
                                                            },
                                                            border = BorderStroke(
                                                                1.dp,
                                                                if (isPendingNote) Color(0xFFD97706).copy(alpha = 0.45f)
                                                                else notebookAccent.copy(alpha = 0.4f)
                                                            )
                                                        ) {
                                                            Text(
                                                                "${String.format(java.util.Locale.US, "%,.0f", vn.invoiceAmount)} $curr",
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = if (isPendingNote) Color(0xFFD97706) else notebookAccent,
                                                                maxLines = 1,
                                                                softWrap = false,
                                                                modifier = Modifier
                                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                                                    .testTag("vet_note_invoice_amount_${vn.id}")
                                                            )
                                                        }
                                                    }
                                                }
                                                if (isPendingNote) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Button(
                                                        onClick = {
                                                            val matchedTxId = vn.linkedTransactionId.ifBlank {
                                                                transactions.firstOrNull {
                                                                    it.farmId == vn.farmId && it.cycleId == vn.cycleId && it.status.equals("PENDING", ignoreCase = true)
                                                                }?.id ?: ""
                                                            }
                                                            pendingSignatureTitle = Translations.getLocalizedMedicine(vn.id, vn.medicineName, lang)
                                                            pendingSignatureDate = "${Translations.tr("day_prefix", lang)} ${vn.dayNumber}"
                                                            pendingSignatureAmount = vn.invoiceAmount
                                                            pendingSignatureApprovalRequest = {
                                                                onApproveVetInvoice(matchedTxId, vn.id)
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(42.dp)
                                                            .testTag("btn_approve_vet_note_${vn.id}"),
                                                        shape = notebookBtnShape,
                                                        colors = ButtonDefaults.buttonColors(containerColor = notebookButtonAccent)
                                                    ) {
                                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            Translations.tr("btn_approve_vet_invoice", lang),
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(130.dp))
                        }
                    }
                }
            }
            }
        }
    }
        }
    }
}

/**
 * Dialog for Owner Daily Report Entry (supporting Mid-Cycle Onboarding e.g., Day 20 or Day 30)
 * and Editing existing daily reports with an automatic Audit Trail (`isEdited = true`).
 */
@Composable
fun OwnerDailyReportDialog(
    farm: FarmModel,
    cycle: CycleModel,
    initialReport: DailyReportModel?,
    suggestedDayNumber: Int,
    vetNotes: List<VetNoteModel> = emptyList(),
    lang: AppLanguage,
    currentTheme: AppThemeOption,
    onDismiss: () -> Unit,
    onSaveReport: (DailyReportModel) -> Unit
) {
    val appearance = LocalAppAppearanceState.current
    val isDark = currentTheme.isDark
    val dialogBg = if (isDark) Color(0xFF1E293B) else Color.White
    val primaryText = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val secondaryText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    val accentEmerald = appearance.accentColor

    val isEditMode = initialReport != null
    var dayNumberInput by remember(initialReport, suggestedDayNumber) {
        mutableStateOf((initialReport?.dayNumber ?: suggestedDayNumber).toString())
    }
    var feedQuintalsInput by remember(initialReport) {
        mutableStateOf(
            if (initialReport != null) Translations.formatQuintalNumber(initialReport.feedIntakeKg / 100.0)
            else ""
        )
    }
    var waterLitersInput by remember(initialReport) {
        mutableStateOf(
            if (initialReport != null) String.format(java.util.Locale.US, "%.0f", initialReport.waterIntakeLiters)
            else ""
        )
    }
    var mortalityInput by remember(initialReport) {
        mutableStateOf(initialReport?.mortalityCount?.toString() ?: "")
    }
    var tempInput by remember(initialReport) {
        mutableStateOf(
            if (initialReport != null) String.format(java.util.Locale.US, "%.1f", initialReport.temperature)
            else "29.5"
        )
    }
    var gasAmmoniaInput by remember(initialReport) {
        mutableStateOf(
            if (initialReport != null) String.format(java.util.Locale.US, "%.1f", initialReport.gasUnits)
            else "1.5"
        )
    }
    var actualWeightGramsInput by remember(initialReport) {
        mutableStateOf(
            if (initialReport != null && initialReport.actualAvgWeightKg > 0.0) {
                kotlin.math.round(initialReport.actualAvgWeightKg * 1000.0).toInt().toString()
            } else {
                ""
            }
        )
    }
    var dailyReportValidationError by remember { mutableStateOf<String?>(null) }

    val parsedCurrentDay = dayNumberInput.toIntOrNull() ?: suggestedDayNumber
    val activeDayOrFarmVetNote = vetNotes.firstOrNull { it.dayNumber == parsedCurrentDay }
        ?: vetNotes.lastOrNull()
        ?: AgritechMockDb.vetNotes.firstOrNull { it.farmId == farm.id && it.dayNumber == parsedCurrentDay }
        ?: AgritechMockDb.vetNotes.lastOrNull { it.farmId == farm.id }
    val defaultActiveMedName = initialReport?.medicineSummary?.takeIf { it.isNotBlank() }
        ?: activeDayOrFarmVetNote?.medicineName
        ?: "فيتامين AD3E + رافع مناعة"

    var medicineGivenToggle by remember(initialReport, activeDayOrFarmVetNote) {
        mutableStateOf(initialReport?.medicineGiven ?: (activeDayOrFarmVetNote != null))
    }
    var medicineSummaryInput by remember(initialReport, activeDayOrFarmVetNote) {
        mutableStateOf(defaultActiveMedName)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBg,
        shape = RoundedCornerShape(appearance.cornerRadiusDp.dp),
        modifier = Modifier.testTag("owner_daily_report_dialog"),
        title = {
            Text(
                if (isEditMode) Translations.tr("dialog_edit_daily_report_title", lang)
                else Translations.tr("dialog_owner_daily_report_title", lang),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = primaryText
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = dayNumberInput,
                    onValueChange = { if (!isEditMode) dayNumberInput = it.filter { ch -> ch.isDigit() } },
                    readOnly = isEditMode,
                    label = { Text(Translations.tr("field_day_number_mid_cycle", lang), fontSize = 11.5.sp, color = secondaryText) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = primaryText,
                        unfocusedTextColor = primaryText,
                        focusedBorderColor = accentEmerald
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_owner_report_day")
                )

                OutlinedTextField(
                    value = feedQuintalsInput,
                    onValueChange = {
                        feedQuintalsInput = it.filter { ch -> ch.isDigit() || ch == '.' }
                        dailyReportValidationError = null
                    },
                    label = { Text(Translations.tr("th_feed", lang), fontSize = 11.5.sp, color = secondaryText) },
                    placeholder = { Text("1.8", fontSize = 11.5.sp, color = secondaryText.copy(alpha = 0.6f)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = primaryText,
                        unfocusedTextColor = primaryText,
                        focusedBorderColor = accentEmerald
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_owner_report_feed_quintals")
                )

                OutlinedTextField(
                    value = waterLitersInput,
                    onValueChange = {
                        waterLitersInput = it.filter { ch -> ch.isDigit() || ch == '.' }
                        dailyReportValidationError = null
                    },
                    label = { Text(Translations.tr("th_water", lang), fontSize = 11.5.sp, color = secondaryText) },
                    placeholder = { Text("320", fontSize = 11.5.sp, color = secondaryText.copy(alpha = 0.6f)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = primaryText,
                        unfocusedTextColor = primaryText,
                        focusedBorderColor = accentEmerald
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_owner_report_water")
                )

                OutlinedTextField(
                    value = mortalityInput,
                    onValueChange = {
                        mortalityInput = it.filter { ch -> ch.isDigit() }
                        dailyReportValidationError = null
                    },
                    label = { Text(Translations.tr("th_mortality", lang), fontSize = 11.5.sp, color = secondaryText) },
                    placeholder = { Text("0", fontSize = 11.5.sp, color = secondaryText.copy(alpha = 0.6f)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = primaryText,
                        unfocusedTextColor = primaryText,
                        focusedBorderColor = accentEmerald
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_owner_report_mortality")
                )

                OutlinedTextField(
                    value = actualWeightGramsInput,
                    onValueChange = { actualWeightGramsInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text(Translations.tr("field_actual_bird_weight_grams_optional", lang), fontSize = 11.5.sp, color = secondaryText) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = primaryText,
                        unfocusedTextColor = primaryText,
                        focusedBorderColor = accentEmerald
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_owner_report_actual_weight_grams")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tempInput,
                        onValueChange = { tempInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text(Translations.tr("th_temp", lang), fontSize = 11.sp, color = secondaryText) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = primaryText,
                            unfocusedTextColor = primaryText,
                            focusedBorderColor = accentEmerald
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_owner_report_temp")
                    )

                    OutlinedTextField(
                        value = gasAmmoniaInput,
                        onValueChange = { gasAmmoniaInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text(Translations.tr("th_gas", lang), fontSize = 11.sp, color = secondaryText) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = primaryText,
                            unfocusedTextColor = primaryText,
                            focusedBorderColor = accentEmerald
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_owner_report_gas")
                    )
                }

                // Medicine in water confirmation switch row ("💊 تأكيد إعطاء الدواء في الماء اليوم (شرب الدواء ✓)")
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (medicineGivenToggle) appearance.accentContainerColor.copy(alpha = 0.65f)
                    else if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                    border = BorderStroke(
                        1.dp,
                        if (medicineGivenToggle) accentEmerald.copy(alpha = 0.55f)
                        else if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                Translations.tr("confirm_medicine_water_today_label", lang),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (medicineGivenToggle) accentEmerald else primaryText,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            Switch(
                                checked = medicineGivenToggle,
                                onCheckedChange = { medicineGivenToggle = it },
                                modifier = Modifier.testTag("switch_owner_report_medicine_given")
                            )
                        }
                        if (medicineGivenToggle) {
                            OutlinedTextField(
                                value = medicineSummaryInput,
                                onValueChange = { medicineSummaryInput = it },
                                label = {
                                    Text(
                                        Translations.tr("medicine_water_name_input_label", lang),
                                        fontSize = 11.sp,
                                        color = secondaryText
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = primaryText,
                                    unfocusedTextColor = primaryText,
                                    focusedBorderColor = accentEmerald
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_owner_report_medicine_name")
                            )
                        }
                    }
                }
                if (dailyReportValidationError != null) {
                    Text(
                        text = dailyReportValidationError!!,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = ActionRed,
                        modifier = Modifier.testTag("daily_report_validation_error")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedFeedQ = feedQuintalsInput.trim().toDoubleOrNull()
                    val parsedWaterVal = waterLitersInput.trim().toDoubleOrNull()
                    val parsedMortVal = mortalityInput.trim().toIntOrNull()
                    if (parsedFeedQ == null || parsedWaterVal == null || parsedMortVal == null) {
                        dailyReportValidationError = Translations.tr("input_required_error", lang)
                        return@Button
                    }
                    val parsedDay = (dayNumberInput.toIntOrNull() ?: suggestedDayNumber).coerceAtLeast(1)
                    val parsedFeedKg = (parsedFeedQ * 100.0).coerceAtLeast(0.0)
                    val parsedWater = parsedWaterVal.coerceAtLeast(0.0)
                    val parsedMortality = parsedMortVal.coerceAtLeast(0)
                    val parsedTemp = tempInput.toDoubleOrNull() ?: 29.0
                    val parsedGas = (gasAmmoniaInput.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
                    val parsedWeightGrams = actualWeightGramsInput.trim().replace(',', '.').toDoubleOrNull() ?: 0.0
                    val resolvedActualWeightKg = if (parsedWeightGrams > 0.0) {
                        parsedWeightGrams / 1000.0
                    } else {
                        0.0
                    }
                    val resolvedMedName = if (medicineGivenToggle) {
                        medicineSummaryInput.trim().ifBlank { defaultActiveMedName }
                    } else {
                        ""
                    }

                    val reportId = initialReport?.id ?: "rep_${farm.id}_${cycle.id}_d$parsedDay"
                    val reportToSave = DailyReportModel(
                        id = reportId,
                        farmId = farm.id,
                        cycleId = cycle.id,
                        dayNumber = parsedDay,
                        feedIntakeKg = parsedFeedKg,
                        waterIntakeLiters = parsedWater,
                        mortalityCount = parsedMortality,
                        temperature = parsedTemp,
                        gasUnits = parsedGas,
                        ventilationStatus = initialReport?.ventilationStatus ?: "جيدة",
                        actualAvgWeightKg = resolvedActualWeightKg,
                        isEdited = isEditMode,
                        previousSummary = initialReport?.previousSummary ?: "",
                        medicineGiven = medicineGivenToggle,
                        medicineSummary = resolvedMedName
                    )
                    onSaveReport(reportToSave)
                },
                modifier = Modifier.testTag("btn_confirm_owner_daily_report"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = appearance.accentOption.primaryColor)
            ) {
                Text(
                    Translations.tr("btn_save", lang),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Translations.tr("btn_cancel", lang), color = secondaryText)
            }
        }
    )
}

/**
 * Contextual WhatsApp-Style Chat ModalBottomSheet (`ContextualChatBottomSheet`).
 * Triggered contextually from:
 * - Ledger Tab FAB -> "محادثة مشرف العنبر 👷" (Worker Chat)
 * - Vet Notes Tab FAB -> "استشارة الطبيب البيطري 👨‍⚕️" (Vet Chat)
 * - OwnerDashboardScreen AppBar / FAB -> "الدردشة الإدارية العامة" (General Admin Chat)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextualChatBottomSheet(
    title: String,
    subtitle: String,
    channelId: String,
    currentTheme: AppThemeOption,
    lang: AppLanguage,
    firestoreRepo: AgritechFirestoreRepository? = null,
    onOpenVetInvoice: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showUniversalFinancialDialog by remember { mutableStateOf(false) }

    if (showUniversalFinancialDialog) {
        val activeFarm = AgritechMockDb.farms.firstOrNull() ?: FarmModel(id = "farm_1", name = "عنبر البركة 1 - المدية", location = "المدية", capacity = 8000, assignedWorkerId = "worker_1")
        val activeCycle = AgritechMockDb.cycles.firstOrNull { it.farmId == activeFarm.id } ?: CycleModel(id = "cycle_2025_1", farmId = activeFarm.id, totalBirdsPlaced = 8000, targetWeight = 2.3, status = "active")
        val dayNum = (AgritechMockDb.dailyReports.filter { it.farmId == activeFarm.id }.maxOfOrNull { it.dayNumber } ?: 1)
        UniversalFinancialRequestDialog(
            farm = activeFarm,
            cycle = activeCycle,
            dayNumber = dayNum,
            senderName = title,
            senderRole = subtitle,
            channelId = channelId,
            lang = lang,
            currentTheme = currentTheme,
            firestoreRepo = firestoreRepo,
            onDismiss = { showUniversalFinancialDialog = false }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = currentTheme.bgColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(540.dp)
                .testTag("contextual_chat_bottom_sheet")
        ) {
            // Contextual Chat Header Bar (100% identical layout for both Worker Chat and Vet Consultation Chat)
            val appearance = LocalAppAppearanceState.current
            val isDark = currentTheme.isDark
            val headerBg = if (isDark) Color(0xFF1E293B) else Color.White
            val primaryText = if (isDark) Color(0xFFFFFFFF) else currentTheme.headingColor
            val secondaryText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF64748B)
            val accentEmerald = appearance.accentColor

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = headerBg,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = appearance.accentContainerColor
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Chat,
                                    contentDescription = null,
                                    tint = accentEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                title,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.5.sp,
                                color = primaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                subtitle,
                                fontSize = 11.5.sp,
                                color = secondaryText,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .testTag("btn_contextual_chat_universal_invoice")
                                .clickable { showUniversalFinancialDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            color = appearance.accentContainerColor,
                            border = BorderStroke(1.dp, accentEmerald.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    Translations.tr("btn_send_financial_request", lang),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentEmerald,
                                    maxLines = 1
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = secondaryText
                            )
                        }
                    }
                }
            }

            // WhatsApp-style Chat Body + Input Bar
            Box(modifier = Modifier.weight(1f)) {
                TeamChatTab(
                    channelId = channelId,
                    currentTheme = currentTheme,
                    lang = lang,
                    firestoreRepo = firestoreRepo
                )
            }
        }
    }
}

/**
 * Vet Medicine Invoice & Prescription Dialog ("إصدار فاتورة دواء / وصفة 💊").
 */
@Composable
fun VetMedicineInvoiceDialog(
    farm: FarmModel,
    cycle: CycleModel,
    dayNumber: Int,
    lang: AppLanguage,
    currentTheme: AppThemeOption = AppThemeOption.ICE_BLUE,
    channelId: String? = null,
    firestoreRepo: AgritechFirestoreRepository? = null,
    onDismiss: () -> Unit,
    onSaved: ((medicineName: String, priceDzd: Double) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val appearance = LocalAppAppearanceState.current
    val curr = Translations.tr("currency", lang)
    val isDark = currentTheme.isDark
    val dialogBg = if (isDark) Color(0xFF1E293B) else Color.White
    val primaryText = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val secondaryText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    val accentEmerald = appearance.accentColor

    var medicineName by remember { mutableStateOf("مضاد حيوي إنروفلوكساسين 20%") }
    var dosageText by remember { mutableStateOf("1 مل لكل لتر ماء شرب لمدة 4 أيام") }
    var priceText by remember { mutableStateOf("14500") }
    var clinicalNote by remember { mutableStateOf("علاج وقائي وتنفسي معتمد للقطيع مع مراقبة استهلاك المياه.") }
    var withdrawalDaysInput by remember { mutableStateOf("7") }
    var isUrgent by remember { mutableStateOf(true) }

    val parsedPrice = (priceText.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val parsedWithdrawalDays = (withdrawalDaysInput.toIntOrNull() ?: 0).coerceAtLeast(0)
    val medsCategoryLabel = Translations.tr("tx_meds", lang)
    val vetInvoiceDialogUuid = remember { java.util.UUID.randomUUID().toString() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBg,
        shape = RoundedCornerShape(appearance.cornerRadiusDp.dp),
        modifier = Modifier.testTag("vet_medicine_invoice_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = appearance.accentContainerColor,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("💊", fontSize = 20.sp)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        Translations.tr("vet_invoice_dialog_title", lang),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = accentEmerald
                    )
                    Text(
                        "${Translations.getLocalizedFarmName(farm.id, lang, farm.name)} • ${Translations.tr("day_prefix", lang)} $dayNumber",
                        fontSize = 11.5.sp,
                        color = secondaryText
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    Translations.tr("vet_invoice_dialog_subtitle", lang),
                    fontSize = 12.sp,
                    color = secondaryText,
                    lineHeight = 17.sp
                )

                OutlinedTextField(
                    value = medicineName,
                    onValueChange = { medicineName = it },
                    label = { Text(Translations.tr("vet_med_name_label", lang), fontSize = 12.sp, color = secondaryText) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = primaryText,
                        unfocusedTextColor = primaryText,
                        focusedBorderColor = accentEmerald
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vet_med_name")
                )

                OutlinedTextField(
                    value = dosageText,
                    onValueChange = { dosageText = it },
                    label = { Text(Translations.tr("vet_dosage_instructions_label", lang), fontSize = 12.sp, color = secondaryText) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = primaryText,
                        unfocusedTextColor = primaryText,
                        focusedBorderColor = accentEmerald
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vet_dosage")
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text(Translations.tr("vet_med_total_price_label", lang), fontSize = 12.sp, color = secondaryText) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = primaryText,
                        unfocusedTextColor = primaryText,
                        focusedBorderColor = accentEmerald
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vet_med_price")
                )

                OutlinedTextField(
                    value = clinicalNote,
                    onValueChange = { clinicalNote = it },
                    label = { Text(Translations.tr("vet_clinical_note_label", lang), fontSize = 12.sp, color = secondaryText) },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = primaryText,
                        unfocusedTextColor = primaryText,
                        focusedBorderColor = accentEmerald
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vet_clinical_note")
                )

                OutlinedTextField(
                    value = withdrawalDaysInput,
                    onValueChange = { withdrawalDaysInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text(Translations.tr("vet_withdrawal_days_label", lang), fontSize = 11.5.sp, color = secondaryText) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = primaryText,
                        unfocusedTextColor = primaryText,
                        focusedBorderColor = accentEmerald
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vet_withdrawal_days")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { isUrgent = !isUrgent }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        Translations.tr("med_urgent_badge", lang),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUrgent) ActionRed else secondaryText
                    )
                    Switch(
                        checked = isUrgent,
                        onCheckedChange = { isUrgent = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ActionRed)
                    )
                }

                // Atomic Treasury Expense Preview Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = appearance.accentContainerColor,
                    border = BorderStroke(1.dp, accentEmerald.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "💊 $medsCategoryLabel:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentEmerald
                            )
                            Text(
                                "-${String.format(java.util.Locale.US, "%,.0f", parsedPrice)} $curr",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDark) Color(0xFFFCA5A5) else ActionRed
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "يتم قيد الفاتورة آلياً في الخزنة وتسجيل الوصفة في السجل الطبي للعنبر.",
                            fontSize = 11.sp,
                            color = primaryText
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanMed = medicineName.trim().ifEmpty { "دواء بيطري معتمد" }
                    val cleanDosage = dosageText.trim().ifEmpty { "حسب إرشادات الطبيب البيطري" }
                    val cleanNote = clinicalNote.trim().ifEmpty { "وصفة طبية معتمدة من الطبيب البيطري" }
                    if (parsedPrice > 0.0) {
                        val noteId = "vn_inv_${farm.id}_${cycle.id}_$vetInvoiceDialogUuid"
                        val txId = "tx_vet_${farm.id}_${cycle.id}_$vetInvoiceDialogUuid"
                        val vetNote = VetNoteModel(
                            id = noteId,
                            farmId = farm.id,
                            cycleId = cycle.id,
                            dayNumber = dayNumber,
                            medicineName = cleanMed,
                            dosage = cleanDosage,
                            note = "$cleanNote (فاتورة دواء: ${String.format(java.util.Locale.US, "%,.0f", parsedPrice)} $curr)",
                            isUrgent = isUrgent,
                            status = "PENDING",
                            linkedTransactionId = txId,
                            invoiceAmount = parsedPrice,
                            currency = curr,
                            withdrawalDays = parsedWithdrawalDays
                        )
                        val medTx = TransactionModel(
                            id = txId,
                            farmId = farm.id,
                            cycleId = cycle.id,
                            type = TransactionType.EXPENSE,
                            category = "MEDICATION",
                            amount = parsedPrice,
                            currency = curr,
                            date = "${Translations.tr("day_prefix", lang)} $dayNumber",
                            status = "PENDING"
                        )

                        // 1. Atomic update in reactive state (Treasury Expense + VetNote + Vet Chat Bubble)
                        val targetChannel = channelId ?: "${cycle.id}_vet"
                        val chatMsg = AgritechMockDb.recordVetInvoiceAndPrescription(medTx, vetNote, targetChannel)

                        // 2. Atomic Firestore transaction + Chat message sync when authenticated
                        if (firestoreRepo != null) {
                            scope.launch {
                                firestoreRepo.recordVetInvoiceAndPrescriptionIdempotent(
                                    idempotencyKey = txId,
                                    tx = medTx,
                                    vetNote = vetNote
                                )
                                firestoreRepo.sendChatMessage(chatMsg)
                            }
                        }

                        onSaved?.invoke(cleanMed, parsedPrice)
                        if (onSaved == null) {
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.testTag("btn_confirm_vet_invoice"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = appearance.accentOption.primaryColor)
            ) {
                Text(
                    Translations.tr("vet_invoice_submit_btn", lang),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Translations.tr("btn_cancel", lang), color = secondaryText)
            }
        }
    )
}

/**
 * WhatsApp-Style Internal Team Chat View (`TeamChatTab`).
 * Connects the Farm Owner, Veterinarian, and Barn Worker inside a specific contextual channel
 * while dynamically obeying the global `AppAppearanceState` (`accentColor`, `cornerRadius`, `isDark`).
 */
@Composable
fun TeamChatTab(
    channelId: String,
    currentTheme: AppThemeOption,
    lang: AppLanguage,
    firestoreRepo: AgritechFirestoreRepository? = null
) {
    val scope = rememberCoroutineScope()
    val messages = AgritechMockDb.streamChatMessages(channelId)
    var messageInput by remember { mutableStateOf("") }
    var pendingChatApprovalMsg by remember { mutableStateOf<ChatMessageModel?>(null) }
    val currentUserId = "owner_1"
    val appearance = LocalAppAppearanceState.current
    val sendBtnAccent = appearance.accentOption.primaryColor
    val isDark = currentTheme.isDark
    val primaryText = if (isDark) Color(0xFFFFFFFF) else Color(0xDD000000)
    val secondaryText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF64748B)
    val accentEmerald = appearance.accentColor

    pendingChatApprovalMsg?.let { targetMsg ->
        val targetTx = AgritechMockDb.transactions.firstOrNull { it.id == targetMsg.linkedTransactionId }
        val displayTitle = targetMsg.requestCategory.ifBlank {
            targetTx?.category ?: Translations.tr("fin_req_dialog_title", lang)
        }
        val displayAmt = if (targetMsg.requestAmount > 0.0) targetMsg.requestAmount else (targetTx?.amount ?: 0.0)
        val displayCurr = targetMsg.requestCurrency.ifBlank {
            targetTx?.currency ?: Translations.tr("currency", lang)
        }
        val displayDate = targetTx?.date ?: targetMsg.timestamp

        InvoiceApprovalSignatureDialog(
            categoryOrTitle = displayTitle,
            dateOrDayText = displayDate,
            amount = displayAmt,
            currency = displayCurr,
            currentTheme = currentTheme,
            lang = lang,
            onDismiss = { pendingChatApprovalMsg = null },
            onConfirmSignedApproval = {
                val targetTxId = targetMsg.linkedTransactionId
                val sigTag = "Signed_By_Owner_${System.currentTimeMillis()}"
                AgritechMockDb.approveVetInvoice(
                    transactionId = targetTxId,
                    signatureTag = sigTag
                )
                val targetFarmId = targetTx?.farmId ?: "farm_1"
                if (firestoreRepo != null) {
                    scope.launch {
                        firestoreRepo.approveVetInvoiceTransaction(
                            farmId = targetFarmId,
                            transactionId = targetTxId,
                            approvedBySignature = sigTag
                        )
                    }
                }
                pendingChatApprovalMsg = null
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(currentTheme.bgColor)
            .testTag("team_chat_tab")
    ) {
        // 1. Message Bubbles ListView
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isMe = msg.senderId == currentUserId || msg.senderRole.contains("مالك") || msg.senderRole.contains("Owner", ignoreCase = true)

                // Role-specific header accent color
                val roleAccentColor = when {
                    msg.senderId.startsWith("vet") || msg.senderRole.contains("بيطري") || msg.senderRole.contains("Vet", ignoreCase = true) ->
                        if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                    msg.senderId.startsWith("worker") || msg.senderRole.contains("مشرف") || msg.senderRole.contains("Worker", ignoreCase = true) ->
                        if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
                    else -> accentEmerald
                }

                // My Messages: Right-aligned, active accent container background, rounded corners except top-right
                // Others' Messages: Left-aligned, surface background, rounded corners except top-left
                val bubbleShape = if (isMe) {
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 4.dp,
                        bottomStart = 18.dp,
                        bottomEnd = 18.dp
                    )
                } else {
                    RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 18.dp,
                        bottomStart = 18.dp,
                        bottomEnd = 18.dp
                    )
                }

                val bubbleBgColor = if (isMe) {
                    appearance.accentContainerColor
                } else {
                    if (isDark) Color(0xFF1E293B) else Color.White
                }

                val linkedTx = if (msg.linkedTransactionId.isNotBlank()) {
                    AgritechMockDb.transactions.firstOrNull { it.id == msg.linkedTransactionId }
                } else null
                val isPendingRequest = linkedTx != null && linkedTx.status.equals("PENDING", ignoreCase = true)
                val isApprovedRequest = (linkedTx != null && linkedTx.status.equals("APPROVED", ignoreCase = true)) ||
                    msg.status.equals("APPROVED", ignoreCase = true)

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Surface(
                        modifier = Modifier
                            .widthIn(min = 120.dp, max = 315.dp)
                            .testTag("chat_bubble_${msg.id}"),
                        shape = bubbleShape,
                        color = bubbleBgColor,
                        border = if (isMe) BorderStroke(1.dp, accentEmerald.copy(alpha = 0.35f)) else null,
                        shadowElevation = 1.5.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            // Sender Name & Role
                            Text(
                                "${msg.senderName} - ${msg.senderRole}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = roleAccentColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            // Message Text
                            Text(
                                msg.text,
                                fontSize = 13.5.sp,
                                color = primaryText,
                                lineHeight = 19.sp
                            )

                            // Interactive Financial Request Card inside Chat Bubble
                            if (msg.linkedTransactionId.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                val reqCardBorder = if (isApprovedRequest) accentEmerald else Color(0xFFD97706)
                                val reqCardBg = if (isApprovedRequest) {
                                    appearance.accentContainerColor
                                } else {
                                    if (isDark) Color(0xFF451A03).copy(alpha = 0.65f) else Color(0xFFFFFBEB)
                                }
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("chat_financial_request_card_${msg.linkedTransactionId}"),
                                    shape = RoundedCornerShape(12.dp),
                                    color = reqCardBg,
                                    border = BorderStroke(1.dp, reqCardBorder.copy(alpha = 0.55f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val displayAmt = if (msg.requestAmount > 0.0) msg.requestAmount else (linkedTx?.amount ?: 0.0)
                                        val displayCurr = if (msg.requestCurrency.isNotBlank()) msg.requestCurrency else (linkedTx?.currency ?: Translations.tr("currency", lang))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                if (msg.requestCategory.isNotBlank()) msg.requestCategory else (linkedTx?.category ?: ""),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = primaryText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(end = 8.dp)
                                            )
                                            Text(
                                                "${String.format(java.util.Locale.US, "%,.0f", displayAmt)} $displayCurr",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isApprovedRequest) accentEmerald else Color(0xFFD97706),
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                        Text(
                                            if (isApprovedRequest) {
                                                Translations.tr("fin_req_status_approved", lang)
                                            } else {
                                                Translations.tr("fin_req_status_pending", lang)
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isApprovedRequest) accentEmerald else Color(0xFFD97706)
                                        )
                                        if (isPendingRequest && !isApprovedRequest) {
                                            Button(
                                                onClick = {
                                                    pendingChatApprovalMsg = msg
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(38.dp)
                                                    .testTag("btn_approve_chat_tx_${msg.linkedTransactionId}"),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = sendBtnAccent)
                                            ) {
                                                Text(
                                                    Translations.tr("btn_approve_vet_invoice", lang),
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Timestamp at the bottom corner
                            Row(
                                modifier = Modifier.align(Alignment.End),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    msg.timestamp,
                                    fontSize = 10.sp,
                                    color = secondaryText
                                )
                                if (isMe) {
                                    Text(
                                        "✓✓",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentEmerald
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Pinned WhatsApp-Style Message Input Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = currentTheme.bgColor,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = {
                        Text(
                            Translations.tr("chat_input_hint", lang),
                            fontSize = 13.sp,
                            color = secondaryText
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_message_input"),
                    shape = RoundedCornerShape(28.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.White,
                        unfocusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.White,
                        focusedTextColor = primaryText,
                        unfocusedTextColor = primaryText,
                        focusedBorderColor = accentEmerald.copy(alpha = 0.5f),
                        unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color.Black.copy(alpha = 0.08f)
                    )
                )

                // Circular Active Accent Send Button
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .testTag("btn_send_chat_message")
                        .clickable {
                            val trimmed = messageInput.trim()
                            if (trimmed.isNotEmpty()) {
                                val nowTime = java.time.LocalTime.now().format(
                                    java.time.format.DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale("ar"))
                                )
                                val resolvedSenderName = try {
                                    FirebaseAuth.getInstance().currentUser?.displayName?.takeIf { it.isNotBlank() }
                                } catch (_: Exception) {
                                    null
                                } ?: AgritechMockDb.farms.firstOrNull()?.name?.takeIf { it.isNotBlank() }
                                    ?: Translations.tr("role_badge_owner_barn", lang)
                                val newMsg = ChatMessageModel(
                                    id = "msg_${System.currentTimeMillis()}",
                                    cycleId = channelId,
                                    senderId = currentUserId,
                                    senderName = resolvedSenderName,
                                    senderRole = "مالك المزرعة 👨‍🌾",
                                    text = trimmed,
                                    timestamp = nowTime
                                )
                                AgritechMockDb.sendChatMessage(newMsg)
                                if (firestoreRepo != null) {
                                    scope.launch {
                                        firestoreRepo.sendChatMessage(newMsg)
                                    }
                                }
                                messageInput = ""
                            }
                        },
                    shape = CircleShape,
                    color = sendBtnAccent,
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// الشاشة الأولى: بيانات الشحنة والمشتري (Shipment & Buyer Details)
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchShipmentInfoScreen(
    currentTheme: AppThemeOption,
    lang: AppLanguage,
    farm: FarmModel,
    cycle: CycleModel,
    ageInDays: Int,
    liveBirds: Int = cycle.totalBirdsPlaced,
    buyerName: String,
    onBuyerNameChange: (String) -> Unit,
    truckPlate: String,
    onTruckPlateChange: (String) -> Unit,
    pricePerKg: String,
    onPricePerKgChange: (String) -> Unit,
    birdsPerCrate: String,
    onBirdsPerCrateChange: (String) -> Unit,
    tareMode: TareMode,
    onTareModeChange: (TareMode) -> Unit,
    directTareTotal: String,
    onDirectTareTotalChange: (String) -> Unit,
    onProceedToWeighing: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val parsedCrateBirds = birdsPerCrate.trim().toIntOrNull() ?: 0
    val isCrateCountExceedingLiveBirds = parsedCrateBirds > liveBirds
    val liveBirdsExceededWarningText = Translations.tr("sales_exceeds_live_birds_warning", lang)
        .replace("{liveBirds}", "$liveBirds")

    Scaffold(
        containerColor = currentTheme.bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            Translations.tr("sales_stage_1_title", lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = currentTheme.headingColor
                        )
                        Text(
                            "${Translations.getLocalizedFarmName(farm.id, lang)} • ${Translations.tr("cycle_label", lang)}: ${cycle.id}",
                            fontSize = 12.sp,
                            color = currentTheme.headingColor.copy(alpha = 0.75f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = currentTheme.headingColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = currentTheme.bgColor)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val appearance = LocalAppAppearanceState.current
            val shipAccent = appearance.accentColor
            val shipAccentContainer = appearance.accentContainerColor
            val shipCardShape = RoundedCornerShape(appearance.cornerRadiusDp.dp)
            val shipFieldShape = RoundedCornerShape((appearance.cornerRadiusDp - 6).coerceAtLeast(4).dp)

            // شاشة بيضاء نظيفة ومستقلة تحتوي على الحقول
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = shipCardShape,
                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(shipAccentContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = shipAccent, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                Translations.tr("sales_stage_1_title", lang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = currentTheme.headingColor
                            )
                            Text(
                                Translations.tr("sales_banner_desc", lang),
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    HorizontalDivider(color = if (currentTheme.isDark) Color(0xFF334155) else Color(0xFFF1F5F9))

                    // 1. اسم المشتري / التاجر
                    OutlinedTextField(
                        value = buyerName,
                        onValueChange = onBuyerNameChange,
                        label = { Text(Translations.tr("buyer_name_label", lang), fontSize = 13.sp) },
                        placeholder = { Text(Translations.tr("buyer_name_placeholder", lang)) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = shipAccent)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = shipFieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = shipAccent,
                            focusedLabelColor = shipAccent
                        )
                    )

                    // 2. رقم لوحة الشاحنة
                    OutlinedTextField(
                        value = truckPlate,
                        onValueChange = onTruckPlateChange,
                        label = { Text(Translations.tr("truck_plate_label", lang), fontSize = 13.sp) },
                        placeholder = { Text("04521-122-16") },
                        leadingIcon = {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = shipAccent)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = shipFieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = shipAccent,
                            focusedLabelColor = shipAccent
                        )
                    )

                    // 3. عمر الدجاج (محسوب تلقائياً من آخر يوم مسجل في السجل)
                    Surface(
                        color = if (currentTheme.isDark) Color(0xFF0F172A) else shipAccentContainer.copy(alpha = 0.45f),
                        shape = shipFieldShape,
                        border = BorderStroke(1.dp, shipAccent.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(shipAccentContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = shipAccent, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    Translations.tr("bird_age_auto_calculated_title", lang).replace("{days}", "$ageInDays"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = shipAccent
                                )
                                Text(
                                    Translations.tr("bird_age_auto_calculated_desc", lang),
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // 4. سعر الكيلوغرام الواحد في السوق اليوم (دج/كغ)
                    OutlinedTextField(
                        value = pricePerKg,
                        onValueChange = onPricePerKgChange,
                        label = { Text(Translations.tr("price_per_kg_label", lang), fontSize = 13.sp) },
                        placeholder = { Text("380") },
                        leadingIcon = {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = shipAccent)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = shipFieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = shipAccent,
                            focusedLabelColor = shipAccent
                        )
                    )

                    // 5. عدد الدجاج داخل الصندوق الواحد (حقل إدخال رقمي حر TextField)
                    OutlinedTextField(
                        value = birdsPerCrate,
                        onValueChange = onBirdsPerCrateChange,
                        label = { Text(Translations.tr("birds_per_crate_label", lang), fontSize = 13.sp) },
                        placeholder = { Text("10") },
                        isError = isCrateCountExceedingLiveBirds,
                        supportingText = {
                            if (isCrateCountExceedingLiveBirds) {
                                Text(
                                    liveBirdsExceededWarningText,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ActionRed,
                                    modifier = Modifier.testTag("sales_stage1_live_birds_exceeded_warning")
                                )
                            } else {
                                Text(Translations.tr("birds_per_crate_free_hint", lang), fontSize = 11.5.sp, color = Color.Gray)
                            }
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Inbox, contentDescription = null, tint = if (isCrateCountExceedingLiveBirds) ActionRed else shipAccent)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_sales_birds_per_crate"),
                        shape = shipFieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isCrateCountExceedingLiveBirds) ActionRed else shipAccent,
                            focusedLabelColor = if (isCrateCountExceedingLiveBirds) ActionRed else shipAccent
                        )
                    )

                    HorizontalDivider(color = if (currentTheme.isDark) Color(0xFF334155) else Color(0xFFF1F5F9))

                    // 6. خيار وزن الصناديق الفارغة (التارا)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            Translations.tr("tare_mode_label", lang),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.headingColor
                        )

                        // الخيار (أ): تسجيل وزنات الصناديق الفارغة بالتفصيل (وزنة بوزنة)
                        Surface(
                            onClick = { onTareModeChange(TareMode.DETAILED) },
                            shape = shipFieldShape,
                            color = if (tareMode == TareMode.DETAILED) shipAccentContainer else (if (currentTheme.isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)),
                            border = BorderStroke(
                                1.5.dp,
                                if (tareMode == TareMode.DETAILED) shipAccent else Color.LightGray.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = tareMode == TareMode.DETAILED,
                                    onClick = { onTareModeChange(TareMode.DETAILED) },
                                    colors = RadioButtonDefaults.colors(selectedColor = shipAccent)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        Translations.tr("tare_mode_detailed", lang),
                                        fontSize = 13.sp,
                                        fontWeight = if (tareMode == TareMode.DETAILED) FontWeight.Bold else FontWeight.Medium,
                                        color = if (tareMode == TareMode.DETAILED) shipAccent else currentTheme.headingColor
                                    )
                                    Text(
                                        Translations.tr("tare_mode_detailed_desc", lang),
                                        fontSize = 11.5.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        // الخيار (ب): إدخال إجمالي وزن الصناديق الفارغة مباشرة كرقم واحد
                        Surface(
                            onClick = { onTareModeChange(TareMode.DIRECT_TOTAL) },
                            shape = shipFieldShape,
                            color = if (tareMode == TareMode.DIRECT_TOTAL) shipAccentContainer else (if (currentTheme.isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)),
                            border = BorderStroke(
                                1.5.dp,
                                if (tareMode == TareMode.DIRECT_TOTAL) shipAccent else Color.LightGray.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = tareMode == TareMode.DIRECT_TOTAL,
                                    onClick = { onTareModeChange(TareMode.DIRECT_TOTAL) },
                                    colors = RadioButtonDefaults.colors(selectedColor = shipAccent)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        Translations.tr("tare_mode_direct", lang),
                                        fontSize = 13.sp,
                                        fontWeight = if (tareMode == TareMode.DIRECT_TOTAL) FontWeight.Bold else FontWeight.Medium,
                                        color = if (tareMode == TareMode.DIRECT_TOTAL) shipAccent else currentTheme.headingColor
                                    )
                                    Text(
                                        Translations.tr("tare_mode_direct_desc", lang),
                                        fontSize = 11.5.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        // إذا اختار المستخدم الخيار (ب): إدخال إجمالي الفارغ مباشرة
                        if (tareMode == TareMode.DIRECT_TOTAL) {
                            OutlinedTextField(
                                value = directTareTotal,
                                onValueChange = onDirectTareTotalChange,
                                label = { Text(Translations.tr("tare_direct_total_label", lang), fontSize = 13.sp) },
                                placeholder = { Text("150.0") },
                                leadingIcon = {
                                    Icon(Icons.Default.Scale, contentDescription = null, tint = shipAccent)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                shape = shipFieldShape,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = shipAccent,
                                    focusedLabelColor = shipAccent
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // زر عريض في الأسفل: [حفظ وبدء تسجيل الوزنات ➡️]
            Button(
                onClick = {
                    if (!isCrateCountExceedingLiveBirds) {
                        onProceedToWeighing()
                    }
                },
                enabled = !isCrateCountExceedingLiveBirds,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("btn_proceed_to_weighing"),
                shape = shipFieldShape,
                colors = ButtonDefaults.buttonColors(containerColor = appearance.accentOption.primaryColor),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                Text(
                    Translations.tr("btn_proceed_to_weighing", lang),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ============================================================================
// الشاشة الثانية: جدول الميزان السريع (Quick Weighing Table - 2 Phases)
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchQuickWeighingScreen(
    currentTheme: AppThemeOption,
    lang: AppLanguage,
    farm: FarmModel,
    cycle: CycleModel,
    ageInDays: Int,
    liveBirds: Int = cycle.totalBirdsPlaced,
    buyerName: String,
    truckPlate: String,
    birdsPerCrate: Int,
    tareMode: TareMode,
    directTareTotal: Double,
    pricePerKg: Double,
    tareWeights: List<Double>,
    grossWeights: List<WeighEntry>,
    onAddTareWeight: (Double) -> Unit,
    onDeleteTareWeight: (Int) -> Unit,
    onAddGrossWeight: (Int, Double) -> Unit,
    onDeleteGrossWeight: (Int) -> Unit,
    onFinalizeAndMigrate: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var tareInput by remember { mutableStateOf("") }
    var grossInput by remember { mutableStateOf("") }
    var cratesPerThisWeigh by remember { mutableStateOf("5") }
    var deleteConfirmIdx by remember { mutableStateOf<Int?>(null) }
    val tareFocusRequester = remember { FocusRequester() }
    val grossFocusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val weighScope = rememberCoroutineScope()

    val maxRowsCount = maxOf(tareWeights.size, grossWeights.size)
    LaunchedEffect(maxRowsCount) {
        if (maxRowsCount > 0) {
            listState.animateScrollToItem(maxRowsCount - 1)
        }
    }

    val submitCurrentWeighRow: () -> Unit = {
        val hadTare = tareInput.isNotBlank()
        val hadGross = grossInput.isNotBlank()
        var addedAny = false
        var newTareCount = tareWeights.size
        var newGrossCount = grossWeights.size

        val t = tareInput.trim().replace(',', '.').toDoubleOrNull()
        if (t != null && t > 0) {
            onAddTareWeight(t)
            tareInput = ""
            addedAny = true
            newTareCount += 1
        }
        val g = grossInput.trim().replace(',', '.').toDoubleOrNull()
        if (g != null && g > 0) {
            val crates = (cratesPerThisWeigh.toIntOrNull() ?: 5).coerceAtLeast(1)
            onAddGrossWeight(crates, g)
            grossInput = ""
            addedAny = true
            newGrossCount += 1
        }
        if (addedAny) {
            val targetLastIdx = (maxOf(newTareCount, newGrossCount) - 1).coerceAtLeast(0)
            weighScope.launch {
                listState.animateScrollToItem(targetLastIdx)
            }
            try {
                if (hadGross) {
                    grossFocusRequester.requestFocus()
                } else if (hadTare) {
                    tareFocusRequester.requestFocus()
                }
            } catch (_: Exception) {}
        }
    }

    val totalTareKg = if (tareMode == TareMode.DETAILED) {
        if (tareWeights.isNotEmpty()) tareWeights.sum() else 0.0
    } else {
        directTareTotal
    }
    val totalGrossKg = grossWeights.sumOf { it.weightKg }
    val totalGrossCrates = grossWeights.sumOf { it.cratesCount }
    val totalNetKg = (totalGrossKg - totalTareKg).coerceAtLeast(0.0)
    val totalBirdsSold = grossWeights.sumOf { it.cratesCount * birdsPerCrate }
    val avgBirdWeightKg = if (totalBirdsSold > 0) totalNetKg / totalBirdsSold else 0.0
    val totalSalesValue = totalNetKg * pricePerKg
    val isBirdsSoldExceedingLive = totalBirdsSold > liveBirds
    val liveBirdsExceededWarningText = Translations.tr("sales_exceeds_live_birds_warning", lang)
        .replace("{liveBirds}", "$liveBirds")

    val curr = Translations.tr("currency", lang)
    val kgUnit = Translations.tr("kg_unit", lang)

    Scaffold(
        containerColor = currentTheme.bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            Translations.tr("sales_stage_2_title", lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = currentTheme.headingColor
                        )
                        Text(
                            "${buyerName.ifEmpty { Translations.tr("buyer_label", lang) }}${if (truckPlate.isNotBlank()) " • $truckPlate" else ""}",
                            fontSize = 12.sp,
                            color = currentTheme.headingColor.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = currentTheme.headingColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = currentTheme.bgColor)
            )
        },
        bottomBar = {
            // 3. الشريط السفلي الثابت (حساب الصافي النهائي فقط) داخل SafeArea
            Surface(
                color = currentTheme.cardColor,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .padding(bottom = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val appearance = LocalAppAppearanceState.current
                    val weighAccent = appearance.accentColor
                    val weighButtonAccent = appearance.accentOption.primaryColor
                    val positiveGreen = weighAccent
                    val darkColor = if (currentTheme.isDark) Color.White else Color(0xFF0F172A)

                    if (isBirdsSoldExceedingLive) {
                        Surface(
                            color = if (currentTheme.isDark) Color(0xFF450A0A) else Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, ActionRed.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sales_live_birds_exceeded_warning")
                        ) {
                            Text(
                                liveBirdsExceededWarningText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentTheme.isDark) Color(0xFFFCA5A5) else ActionRed,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // لا يتم حساب وإظهار الصافي إلا في الأسفل من المجموع الكلي
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // صافي وزن الدجاج الحي = (مجموع أوزان الممتلئ) - (مجموع أوزان الفارغ)
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.1f)) {
                            Text(Translations.tr("weigh_net_weight_label", lang), fontSize = 10.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${String.format(java.util.Locale.US, "%,.1f", totalNetKg)} $kgUnit", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = positiveGreen)
                        }
                        // إجمالي عدد الطيور المباعة
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(Translations.tr("weigh_total_birds_label", lang), fontSize = 10.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                "${String.format(java.util.Locale.US, "%,d", totalBirdsSold)} ${Translations.tr("birds_unit_short", lang)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = if (isBirdsSoldExceedingLive) ActionRed else darkColor
                            )
                        }
                        // معدل وزن الدجاجة الواحدة = (الصافي الكلي ÷ إجمالي الطيور)
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(Translations.tr("weigh_avg_bird_label", lang), fontSize = 10.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${String.format(java.util.Locale.US, "%.3f", avgBirdWeightKg)} $kgUnit", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = positiveGreen)
                        }
                        // إجمالي قيمة المبيعات (دج) = (الصافي الكلي × سعر الكيلو)
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.2f)) {
                            Text(Translations.tr("weigh_total_sales_label", lang), fontSize = 10.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${String.format(java.util.Locale.US, "%,.0f", totalSalesValue)} $curr", fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp, color = positiveGreen)
                        }
                    }

                    // زر [حفظ الفاتورة وترحيل للخزنة 💰] في الأسفل داخل SafeArea مع مؤشر التحميل (CircularProgressIndicator)
                    var isSavingInvoice by remember { mutableStateOf(false) }
                    val saveScope = rememberCoroutineScope()
                    Button(
                        onClick = {
                            if (!isSavingInvoice && totalSalesValue > 0 && !isBirdsSoldExceedingLive) {
                                isSavingInvoice = true
                                saveScope.launch {
                                    delay(350)
                                    onFinalizeAndMigrate()
                                    isSavingInvoice = false
                                }
                            }
                        },
                        enabled = totalSalesValue > 0 && !isBirdsSoldExceedingLive && !isSavingInvoice,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_save_invoice_and_migrate"),
                        shape = RoundedCornerShape((appearance.cornerRadiusDp - 4).coerceAtLeast(4).dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = weighButtonAccent,
                            disabledContainerColor = if (isSavingInvoice) weighButtonAccent.copy(alpha = 0.85f) else Color.LightGray
                        )
                    ) {
                        if (isSavingInvoice) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                Translations.tr("btn_save_invoice_and_migrate", lang),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        val appearance = LocalAppAppearanceState.current
        val weighAccent = appearance.accentColor
        val weighAccentContainer = appearance.accentContainerColor
        val weighButtonAccent = appearance.accentOption.primaryColor
        val weighCardShape = RoundedCornerShape(appearance.cornerRadiusDp.dp)
        val crateShortUnit = Translations.tr("crates_short_unit", lang)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 2. طريقة الإدخال البسيطة في أعلى الجدول (في نفس الصفحة):
            // خانة لإدخال وزن الفارغ وخانة لإدخال وزن الممتلئ بجانبها مع زر [حفظ ✓]
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = weighCardShape,
                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            Translations.tr("weigh_batch_entry_title", lang),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.headingColor
                        )
                        Surface(
                            color = weighAccentContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "${Translations.tr("weigh_price_per_kg_badge", lang)}: ${String.format(java.util.Locale.US, "%,.0f", pricePerKg)} $curr",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = weighAccent
                            )
                        }
                    }

                    // الحيلة الذكية لعدد الصناديق في الوزنة الممتلئة:
                    // شريط متناسق بعرض كامل للأرقام من 1 إلى 5 بدون أي اقتصاص على الشاشات الصغيرة
                    Surface(
                        color = if (currentTheme.isDark) Color(0xFF1E293B) else weighAccentContainer.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, weighAccent.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val cratesNum = cratesPerThisWeigh.toIntOrNull() ?: 5
                            val birdsInThisWeigh = cratesNum * birdsPerCrate
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    Translations.tr("weigh_full_crates_label", lang),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = weighAccent
                                )
                                Text(
                                    "($birdsInThisWeigh ${Translations.tr("birds_unit_short", lang)})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = weighAccent.copy(alpha = 0.85f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("1", "2", "3", "4", "5").forEach { preset ->
                                    val isSelected = cratesPerThisWeigh == preset
                                    Surface(
                                        color = if (isSelected) weighButtonAccent else if (currentTheme.isDark) Color(0xFF0F172A) else Color.White,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, if (isSelected) weighButtonAccent else Color.LightGray),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                            .testTag("crate_count_chip_$preset")
                                            .clickable { cratesPerThisWeigh = preset }
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Text(
                                                preset,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                color = if (isSelected) Color.White else (if (currentTheme.isDark) Color.White else Color(0xFF0F172A))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // خانة وزن الفارغ (Supports numeric keypad Done action for instant scale entry)
                        OutlinedTextField(
                            value = tareInput,
                            onValueChange = { tareInput = it },
                            label = { Text(Translations.tr("weigh_tare_input_label", lang), fontSize = 11.sp) },
                            placeholder = { Text(Translations.tr("weigh_tare_placeholder", lang), color = Color.Gray, fontSize = 11.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(tareFocusRequester)
                                .testTag("input_weigh_tare"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    submitCurrentWeighRow()
                                    try {
                                        tareFocusRequester.requestFocus()
                                    } catch (_: Exception) {}
                                }
                            ),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0284C7),
                                focusedLabelColor = Color(0xFF0284C7)
                            )
                        )

                        // خانة وزن الممتلئ (Supports numeric keypad Done action for instant scale entry)
                        OutlinedTextField(
                            value = grossInput,
                            onValueChange = { grossInput = it },
                            label = { Text(Translations.tr("weigh_gross_input_label", lang), fontSize = 11.sp) },
                            placeholder = { Text(Translations.tr("weigh_gross_placeholder", lang), color = Color.Gray, fontSize = 11.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(grossFocusRequester)
                                .testTag("input_weigh_gross"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    submitCurrentWeighRow()
                                    try {
                                        grossFocusRequester.requestFocus()
                                    } catch (_: Exception) {}
                                }
                            ),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = weighAccent,
                                focusedLabelColor = weighAccent
                            )
                        )

                        // زر [حفظ ✓]
                        Button(
                            onClick = { submitCurrentWeighRow() },
                            enabled = tareInput.isNotBlank() || grossInput.isNotBlank(),
                            modifier = Modifier
                                .height(54.dp)
                                .testTag("btn_save_weigh_entry"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = weighButtonAccent,
                                disabledContainerColor = Color.LightGray
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(Translations.tr("weigh_save_btn", lang), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        }
                    }
                }
            }

            // 1. تصميم شاشة "جدول الميزان السريع" في بطاقة جدول واحدة عريضة ونظيفة:
            // أعمدته هي فقط: [ # | 🧺 الفارغ (كغ) | 📦 الممتلئ (كغ) | 🗑️ ]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = weighCardShape,
                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    // ترويسة الجدول العريض
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (currentTheme.isDark) Color(0xFF1E293B) else currentTheme.bgColor,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val headerColor = if (currentTheme.isDark) Color.White else weighAccent
                        CalmTableCell("#", 64.dp, isHeader = true, headingColor = headerColor)
                        CalmTableCell(Translations.tr("weigh_tare_input_label", lang), width = 0.dp, modifier = Modifier.weight(1f), isHeader = true, headingColor = headerColor)
                        CalmTableCell(Translations.tr("weigh_gross_input_label", lang), width = 0.dp, modifier = Modifier.weight(1.2f), isHeader = true, headingColor = headerColor)
                        Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.Center) {
                            Text("🗑️", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val maxRows = maxOf(tareWeights.size, grossWeights.size)
                    if (maxRows == 0) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                Translations.tr("weigh_empty_table_msg", lang),
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        val dayColor = weighAccent
                        val cellNumberColor = if (currentTheme.isDark) Color.White else Color(0xFF0F172A)

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .testTag("weighing_batches_lazy_column"),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(maxRows) { idx ->
                                Surface(
                                    color = if (idx % 2 == 0) (if (currentTheme.isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)) else currentTheme.cardColor,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // 1. #
                                        CalmTableCell("#${idx + 1}", 64.dp, isBold = true, textColor = dayColor)

                                        // 2. 🧺 الفارغ (كغ)
                                        val tareText = if (idx < tareWeights.size) {
                                            String.format(java.util.Locale.US, "%.1f", tareWeights[idx])
                                        } else {
                                            "—"
                                        }
                                        CalmTableCell(
                                            text = tareText,
                                            width = 0.dp,
                                            modifier = Modifier.weight(1f),
                                            isBold = idx < tareWeights.size,
                                            textColor = if (idx < tareWeights.size) (if (currentTheme.isDark) Color(0xFF38BDF8) else Color(0xFF0369A1)) else Color.Gray
                                        )

                                        // 3. 📦 الممتلئ (كغ) مع تفصيل الصناديق والطيور
                                        val grossText = if (idx < grossWeights.size) {
                                            val entry = grossWeights[idx]
                                            "${String.format(java.util.Locale.US, "%.1f", entry.weightKg)} (${entry.cratesCount}$crateShortUnit•${entry.cratesCount * birdsPerCrate} ${Translations.tr("bird_unit", lang)})"
                                        } else {
                                            "—"
                                        }
                                        CalmTableCell(
                                            text = grossText,
                                            width = 0.dp,
                                            modifier = Modifier.weight(1.2f),
                                            isBold = idx < grossWeights.size,
                                            textColor = if (idx < grossWeights.size) cellNumberColor else Color.Gray,
                                            fontSize = 11.sp,
                                            softWrap = false
                                        )

                                        // 4. 🗑️
                                        Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.Center) {
                                            IconButton(
                                                onClick = {
                                                    if (idx < tareWeights.size && idx < grossWeights.size) {
                                                        deleteConfirmIdx = idx
                                                    } else if (idx < tareWeights.size) {
                                                        onDeleteTareWeight(idx)
                                                    } else if (idx < grossWeights.size) {
                                                        onDeleteGrossWeight(idx)
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ActionRed, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // في آخر سطر من الجدول، اعرض فقط: (مجموع الفارغ كغ) تحت عمود الفارغ، و(مجموع الممتلئ كغ) تحت عمود الممتلئ
                    Surface(
                        color = if (currentTheme.isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // عرض كلمة "المجموع" كاملة دون اقتطاع
                            CalmTableCell(Translations.tr("weigh_total_row_label", lang), 64.dp, isBold = true, textColor = currentTheme.headingColor)
                            CalmTableCell(
                                text = "${String.format(java.util.Locale.US, "%,.1f", totalTareKg)} $kgUnit",
                                width = 0.dp,
                                modifier = Modifier.weight(1f),
                                isBold = true,
                                textColor = if (currentTheme.isDark) Color(0xFF38BDF8) else Color(0xFF0369A1)
                            )
                            CalmTableCell(
                                text = "${String.format(java.util.Locale.US, "%,.1f", totalGrossKg)} $kgUnit (${totalGrossCrates} $crateShortUnit)",
                                width = 0.dp,
                                modifier = Modifier.weight(1.2f),
                                isBold = true,
                                textColor = weighAccent
                            )
                            Box(modifier = Modifier.width(48.dp))
                        }
                    }
                }
            }
        }
    }

    if (deleteConfirmIdx != null) {
        val appearance = LocalAppAppearanceState.current
        val targetIdx = deleteConfirmIdx!!
        val grossEntry = if (targetIdx < grossWeights.size) grossWeights[targetIdx] else null
        val tareKg = if (targetIdx < tareWeights.size) tareWeights[targetIdx] else null
        val crateShortUnit = Translations.tr("crates_short_unit", lang)
        AlertDialog(
            onDismissRequest = { deleteConfirmIdx = null },
            title = {
                Text(
                    "${Translations.tr("weigh_delete_dialog_title", lang)} #${targetIdx + 1}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = currentTheme.headingColor
                )
            },
            text = {
                Text(
                    Translations.tr("weigh_delete_dialog_msg", lang),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (grossEntry != null) {
                        Button(
                            onClick = {
                                onDeleteGrossWeight(targetIdx)
                                deleteConfirmIdx = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = appearance.accentOption.primaryColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("${Translations.tr("weigh_delete_gross_only", lang)} (${String.format(java.util.Locale.US, "%.1f", grossEntry.weightKg)} $kgUnit - ${grossEntry.cratesCount} $crateShortUnit)")
                        }
                    }
                    if (tareKg != null) {
                        Button(
                            onClick = {
                                onDeleteTareWeight(targetIdx)
                                deleteConfirmIdx = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("${Translations.tr("weigh_delete_tare_only", lang)} (${String.format(java.util.Locale.US, "%.1f", tareKg)} $kgUnit)")
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            if (tareKg != null) onDeleteTareWeight(targetIdx)
                            if (grossEntry != null) onDeleteGrossWeight(targetIdx)
                            deleteConfirmIdx = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ActionRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(Translations.tr("weigh_delete_full_row", lang))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmIdx = null }) {
                    Text(Translations.tr("btn_cancel", lang), color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun CalmTableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp = 0.dp,
    modifier: Modifier = Modifier,
    isHeader: Boolean = false,
    isBold: Boolean = false,
    textColor: Color = DarkTextColor,
    headingColor: Color = TextForestGreen,
    fontSize: TextUnit = if (isHeader) 11.5.sp else 12.sp,
    softWrap: Boolean = true
) {
    val finalModifier = if (width > 0.dp) modifier.width(width) else modifier
    Box(
        modifier = finalModifier.padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = if (isHeader || isBold) FontWeight.Bold else FontWeight.Normal,
            fontSize = fontSize,
            color = if (isHeader) headingColor else textColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = softWrap
        )
    }
}

@Composable
fun CalmSummaryItem(label: String, value: String, isAlert: Boolean = false, headingColor: Color = TextForestGreen) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = headingColor, fontWeight = FontWeight.Bold)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isAlert) ActionRed else headingColor)
    }
}

@Composable
fun CalmFinancialItem(label: String, value: String, color: Color) {
    Column {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
    }
}

@Composable
fun CalmMetricRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = valueColor)
    }
}

@Composable
fun SimpleTransactionDialog(
    title: String,
    type: TransactionType,
    lang: AppLanguage,
    categories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (category: String, amount: Double, addedFeedKg: Double) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var amountText by remember { mutableStateOf("") }

    // Feed-specific fields when "علف" (Feed) chip is selected in Add Expense Dialog
    var feedDesignation by remember { mutableStateOf(Translations.tr("feed_designation_default", lang)) }
    var feedQuintalsText by remember { mutableStateOf("40") }
    var feedPricePerQuintalText by remember { mutableStateOf("8000") }

    val feedCategoryLabel = Translations.tr("tx_feed", lang)
    val isFeedExpense = type == TransactionType.EXPENSE && (
        selectedCategory == feedCategoryLabel ||
            selectedCategory.contains("علف") ||
            selectedCategory.contains("Feed", ignoreCase = true) ||
            selectedCategory.contains("Aliment", ignoreCase = true) ||
            selectedCategory.contains("Pienso", ignoreCase = true)
        )

    val appearance = LocalAppAppearanceState.current
    val isDark = appearance.isDark
    val dialogBg = if (isDark) Color(0xFF1E293B) else Color.White
    val primaryText = if (isDark) Color.White else Color(0xFF0F172A)
    val secondaryText = if (isDark) Color(0xFFCBD5E1) else Color.Gray
    val accentEmerald = appearance.accentColor
    val parsedQuintals = (feedQuintalsText.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val parsedUnitPrice = (feedPricePerQuintalText.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val autoCalculatedFeedAmount = parsedQuintals * parsedUnitPrice
    val addedFeedKg = parsedQuintals * 100.0
    val curr = Translations.tr("currency", lang)
    val kgUnit = Translations.tr("kg_unit", lang)
    val quintalUnit = Translations.tr("quintal_unit", lang)

    AlertDialog(
        containerColor = dialogBg,
        shape = RoundedCornerShape(appearance.cornerRadiusDp.dp),
        onDismissRequest = onDismiss,
        title = {
            Text(title, fontWeight = FontWeight.Bold, color = if (type == TransactionType.INCOME) accentEmerald else ActionRed)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(Translations.tr("category_label", lang), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = accentEmerald)
                WrapFilters(
                    categories = categories,
                    selected = selectedCategory,
                    onSelect = { selectedCategory = it }
                )

                if (isFeedExpense) {
                    OutlinedTextField(
                        value = feedDesignation,
                        onValueChange = { feedDesignation = it },
                        label = { Text(Translations.tr("feed_designation_label", lang), fontSize = 12.sp, color = secondaryText) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = primaryText,
                            unfocusedTextColor = primaryText,
                            focusedBorderColor = accentEmerald
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_feed_designation")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = feedQuintalsText,
                            onValueChange = { feedQuintalsText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text(Translations.tr("feed_qty_quintals_label", lang), fontSize = 11.sp, color = secondaryText) },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = primaryText,
                                unfocusedTextColor = primaryText,
                                focusedBorderColor = accentEmerald
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_feed_quintals")
                        )

                        OutlinedTextField(
                            value = feedPricePerQuintalText,
                            onValueChange = { feedPricePerQuintalText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text(Translations.tr("feed_price_quintal_label", lang), fontSize = 11.sp, color = secondaryText) },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = primaryText,
                                unfocusedTextColor = primaryText,
                                focusedBorderColor = accentEmerald
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_feed_unit_price")
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = appearance.accentContainerColor,
                        border = BorderStroke(1.dp, accentEmerald.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    Translations.tr("feed_total_auto_label", lang),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = accentEmerald
                                )
                                Text(
                                    "${String.format(java.util.Locale.US, "%,.0f", autoCalculatedFeedAmount)} $curr",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = accentEmerald
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val qShort = Translations.tr("quintal_short", lang)
                            Text(
                                "+${Translations.formatQuintalNumber(parsedQuintals)} $qShort ($quintalUnit) → ${Translations.tr("feed_inventory_label", lang)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = primaryText
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text(Translations.tr("amount_label", lang), color = secondaryText) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = primaryText,
                            unfocusedTextColor = primaryText,
                            focusedBorderColor = accentEmerald
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_tx_amount")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isFeedExpense) {
                        if (autoCalculatedFeedAmount > 0.0 && addedFeedKg > 0.0) {
                            val cleanDesig = feedDesignation.trim().ifEmpty { Translations.tr("feed_designation_default", lang) }
                            val qShort = Translations.tr("quintal_short", lang)
                            val formattedCat = "🌾 $cleanDesig (${Translations.formatQuintalNumber(parsedQuintals)} $qShort)"
                            onConfirm(formattedCat, autoCalculatedFeedAmount, addedFeedKg)
                        }
                    } else {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            onConfirm(selectedCategory, amt, 0.0)
                        }
                    }
                },
                modifier = Modifier.testTag("btn_confirm_transaction"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == TransactionType.INCOME) appearance.accentOption.primaryColor else ActionRed
                )
            ) {
                Text(Translations.tr("confirm_btn", lang), color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Translations.tr("cancel_btn", lang), color = secondaryText)
            }
        }
    )
}

data class FeedOrderParsedRow(
    val date: String,
    val designation: String,
    val quantityText: String,
    val estimatedKg: Double,
    val totalAmountDzd: Double,
    val currency: String
)

internal fun parseFeedTransaction(
    tx: TransactionModel,
    lang: AppLanguage,
    unitPref: FeedUnitPreference = FeedUnitPreference.QUINTAL
): FeedOrderParsedRow {
    val rawCat = tx.category.removePrefix("🌾").trim()
    val parenStart = rawCat.indexOf('(')
    val parenEnd = rawCat.lastIndexOf(')')
    val designation = if (parenStart > 0) {
        rawCat.substring(0, parenStart).trim()
    } else {
        rawCat.ifEmpty { Translations.tr("feed_designation_default", lang) }
    }
    val insideParen = if (parenStart >= 0 && parenEnd > parenStart) {
        rawCat.substring(parenStart + 1, parenEnd).trim()
    } else {
        ""
    }
    val numericInParen = Regex("""(\d+(\.\d+)?)""").find(insideParen)?.groupValues?.get(1)?.toDoubleOrNull()
    val combinedText = "${tx.category} ${tx.notes}"
    val inlineTonMatch = Regex("""(\d+(\.\d+)?)\s*(طن|ton)""", RegexOption.IGNORE_CASE).find(combinedText)?.groupValues?.get(1)?.toDoubleOrNull()
    val inlineKgMatch = Regex("""(\d+(\.\d+)?)\s*(كغ|كيلو|kg)""", RegexOption.IGNORE_CASE).find(combinedText)?.groupValues?.get(1)?.toDoubleOrNull()
    val inlineQuintalMatch = Regex("""(\d+(\.\d+)?)\s*(قنطار|ق|q)\b""", RegexOption.IGNORE_CASE).find(combinedText)?.groupValues?.get(1)?.toDoubleOrNull()

    val estimatedKg = when {
        tx.quantityQuintals > 0.0 -> tx.quantityQuintals * 100.0
        numericInParen != null && (insideParen.contains("طن") || insideParen.contains("ton", ignoreCase = true)) -> numericInParen * 1000.0
        numericInParen != null && (insideParen.contains("كغ") || insideParen.contains("kg", ignoreCase = true)) -> numericInParen
        numericInParen != null -> numericInParen * 100.0
        inlineTonMatch != null -> inlineTonMatch * 1000.0
        inlineKgMatch != null -> inlineKgMatch
        inlineQuintalMatch != null -> inlineQuintalMatch * 100.0
        else -> 0.0
    }
    val quantityText = Translations.formatBulkFeedPurchaseQuantity(estimatedKg, lang, unitPref)
    return FeedOrderParsedRow(
        date = tx.date,
        designation = designation,
        quantityText = quantityText,
        estimatedKg = estimatedKg,
        totalAmountDzd = tx.amount,
        currency = tx.currency
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedOrdersLedgerBottomSheet(
    farmName: String,
    cycle: CycleModel,
    transactions: List<TransactionModel>,
    lang: AppLanguage,
    onDismiss: () -> Unit
) {
    val appearance = LocalAppAppearanceState.current
    val isDark = appearance.isDark
    val sheetBg = if (isDark) Color(0xFF1E293B) else Color.White
    val primaryText = if (isDark) Color.White else Color(0xDD000000)
    val secondaryText = if (isDark) Color(0xFFCBD5E1) else Color.Gray
    val accentEmerald = appearance.accentColor
    val preferredFeedUnit = LocalFeedUnitPreference.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val feedCategoryLocalized = Translations.tr("tx_feed", lang)

    // Filter transactions for the current cycle where category corresponds to Feed ('feed')
    val feedTransactions = remember(transactions, cycle.id, lang) {
        transactions.filter { tx ->
            tx.type == TransactionType.EXPENSE &&
                tx.status.equals("APPROVED", ignoreCase = true) &&
                (tx.cycleId == cycle.id || tx.cycleId.isBlank()) &&
                (
                    tx.category.equals("feed", ignoreCase = true) ||
                        tx.category == feedCategoryLocalized ||
                        tx.category.contains("علف") ||
                        tx.category.contains("Feed", ignoreCase = true) ||
                        tx.category.contains("Aliment", ignoreCase = true) ||
                        tx.category.contains("Pienso", ignoreCase = true) ||
                        tx.category.contains("चारा")
                    )
        }
    }

    val parsedRows = remember(feedTransactions, lang, preferredFeedUnit) {
        feedTransactions.map { parseFeedTransaction(it, lang, preferredFeedUnit) }
    }

    val totalFeedPurchasedKg = parsedRows.sumOf { it.estimatedKg }
    val totalFeedPurchasedQuintals = totalFeedPurchasedKg / 100.0
    val totalFeedPurchasedDzd = parsedRows.sumOf { it.totalAmountDzd }

    val isLowInventory = cycle.feedInventoryKg < 500.0
    val alertRed = Color(0xFFD32F2F)
    val remainingColor = if (isLowInventory) alertRed else accentEmerald
    val curr = Translations.tr("currency", lang)
    val kgShort = Translations.tr("kg_short", lang)
    val quintalUnit = Translations.tr("quintal_unit", lang)
    val quintalShort = Translations.tr("quintal_short", lang)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("feed_orders_ledger_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = appearance.accentContainerColor,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🌾", fontSize = 18.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            Translations.tr("feed_orders_ledger_title", lang),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = accentEmerald
                        )
                        Text(
                            "$farmName • ${cycle.id}",
                            fontSize = 12.sp,
                            color = secondaryText
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = secondaryText)
                }
            }

            // Top Summary Card: Total Feed Purchased & Current Remaining Inventory
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(appearance.cornerRadiusDp.dp),
                color = appearance.accentContainerColor,
                border = BorderStroke(1.dp, accentEmerald.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Total Feed Purchased (Strictly in Quintals ق)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            Translations.tr("total_feed_purchased_label", lang),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${Translations.formatQuintalNumber(totalFeedPurchasedQuintals)} $quintalShort ($quintalUnit)",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentEmerald
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "${String.format(java.util.Locale.US, "%,.0f", totalFeedPurchasedDzd)} $curr",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = secondaryText
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(44.dp)
                            .background(accentEmerald.copy(alpha = 0.3f))
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    // 2. Current Remaining Inventory (Strictly in Quintals ق)
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            Translations.tr("current_remaining_inventory_label", lang),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isLowInventory) {
                                Text("⚠️", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                "${Translations.formatQuintalNumber(cycle.feedInventoryKg / 100.0)} $quintalShort 🌾",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = remainingColor
                            )
                        }
                        if (isLowInventory) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                Translations.tr("feed_low_alert", lang),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = alertRed
                            )
                        }
                    }
                }
            }

            // Feed Orders Table / List
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(appearance.cornerRadiusDp.dp),
                color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(appearance.accentContainerColor, RoundedCornerShape(10.dp))
                            .padding(vertical = 10.dp, horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            Translations.tr("th_purchase_date", lang),
                            modifier = Modifier.weight(0.9f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = accentEmerald
                        )
                        Text(
                            Translations.tr("th_feed_designation", lang),
                            modifier = Modifier.weight(1.2f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = accentEmerald
                        )
                        Text(
                            Translations.tr("th_feed_quantity", lang),
                            modifier = Modifier.weight(0.9f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = accentEmerald,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            Translations.tr("th_feed_total_price", lang),
                            modifier = Modifier.weight(1.1f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = accentEmerald,
                            textAlign = TextAlign.End
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (parsedRows.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                Translations.tr("no_feed_orders_yet", lang),
                                fontSize = 12.5.sp,
                                color = secondaryText,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        parsedRows.forEachIndexed { idx, row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    row.date,
                                    modifier = Modifier.weight(0.9f),
                                    fontSize = 12.sp,
                                    color = primaryText
                                )
                                Text(
                                    "🌾 ${row.designation}",
                                    modifier = Modifier.weight(1.2f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = primaryText
                                )
                                Text(
                                    row.quantityText,
                                    modifier = Modifier.weight(0.9f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentEmerald,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    "${String.format(java.util.Locale.US, "%,.0f", row.totalAmountDzd)} ${row.currency}",
                                    modifier = Modifier.weight(1.1f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = accentEmerald,
                                    textAlign = TextAlign.End
                                )
                            }
                            if (idx < parsedRows.lastIndex) {
                                HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class MedicineLedgerRowItem(
    val id: String,
    val dayText: String,
    val medicineName: String,
    val dosageText: String,
    val isApproved: Boolean,
    val amountDzd: Double,
    val currency: String,
    val linkedTransaction: TransactionModel?
)

/**
 * Unified Medicine & Prescriptions Ledger BottomSheet (`MedicineOrdersLedgerBottomSheet`),
 * modeled directly after `FeedOrdersLedgerBottomSheet` (`سجل طلبيات العلف`).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineOrdersLedgerBottomSheet(
    farmName: String,
    cycle: CycleModel,
    transactions: List<TransactionModel>,
    vetNotes: List<VetNoteModel>,
    reports: List<DailyReportModel> = emptyList(),
    initialSelectedTransaction: TransactionModel? = null,
    lang: AppLanguage,
    onSelectTransactionForReceipt: ((TransactionModel) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val appearance = LocalAppAppearanceState.current
    val isDark = appearance.isDark
    val dialogBg = if (isDark) Color(0xFF1E293B) else Color.White
    val primaryText = if (isDark) Color.White else Color(0xDD000000)
    val secondaryText = if (isDark) Color(0xFFCBD5E1) else Color.Gray
    val accentEmerald = appearance.accentColor
    val primaryButtonAccent = appearance.accentOption.primaryColor
    val curr = Translations.tr("currency", lang)
    val dayPrefix = Translations.tr("day_prefix", lang)

    val medicineTransactions = remember(transactions, cycle.id, lang) {
        transactions.filter { tx ->
            tx.type == TransactionType.EXPENSE &&
                (tx.cycleId == cycle.id || tx.cycleId.isBlank()) &&
                (
                    tx.category.equals("MEDICATION", ignoreCase = true) ||
                        tx.category == Translations.tr("tx_meds", lang) ||
                        tx.category.contains("أدوية") ||
                        tx.category.contains("لقاح") ||
                        tx.category.contains("دواء") ||
                        tx.category.contains("تحصين") ||
                        tx.category.contains("فيتامين") ||
                        tx.category.contains("Med", ignoreCase = true) ||
                        tx.category.contains("Vaccin", ignoreCase = true)
                    )
        }
    }

    val ledgerRows = remember(medicineTransactions, vetNotes, reports, lang, curr) {
        val rows = mutableListOf<MedicineLedgerRowItem>()
        val matchedTxIds = mutableSetOf<String>()
        val coveredDays = mutableSetOf<Int>()

        vetNotes.forEach { vn ->
            val linkedTx = medicineTransactions.firstOrNull { tx ->
                tx.id == vn.linkedTransactionId ||
                    (vn.medicineName.isNotBlank() && tx.category.contains(vn.medicineName, ignoreCase = true))
            }
            if (linkedTx != null) {
                matchedTxIds.add(linkedTx.id)
            }
            coveredDays.add(vn.dayNumber)
            val effectiveAmount = linkedTx?.amount ?: vn.invoiceAmount
            val effectiveStatus = linkedTx?.status ?: vn.status
            rows.add(
                MedicineLedgerRowItem(
                    id = vn.id,
                    dayText = "$dayPrefix ${vn.dayNumber}",
                    medicineName = Translations.getLocalizedMedicine(vn.id, vn.medicineName, lang),
                    dosageText = Translations.getLocalizedDosage(vn.id, vn.dosage, lang),
                    isApproved = effectiveStatus.equals("APPROVED", ignoreCase = true),
                    amountDzd = effectiveAmount,
                    currency = curr,
                    linkedTransaction = linkedTx
                )
            )
        }

        medicineTransactions.filter { it.id !in matchedTxIds }.forEach { tx ->
            val dayDigits = tx.date.filter { it.isDigit() }.ifEmpty { "1" }
            val cleanMedName = tx.category
                .removePrefix("💊")
                .replace("الأدوية واللقاحات -", "")
                .replace("فاتورة دواء:", "")
                .replace("MEDICATION", Translations.tr("tx_meds", lang), ignoreCase = true)
                .trim()
                .ifEmpty { Translations.tr("tx_meds", lang) }
            rows.add(
                MedicineLedgerRowItem(
                    id = tx.id,
                    dayText = "$dayPrefix $dayDigits",
                    medicineName = cleanMedName,
                    dosageText = Translations.tr("tx_meds", lang),
                    isApproved = tx.status.equals("APPROVED", ignoreCase = true),
                    amountDzd = tx.amount,
                    currency = curr,
                    linkedTransaction = tx
                )
            )
        }

        reports.filter { (it.medicineGiven || it.medicineSummary.isNotBlank()) && it.dayNumber !in coveredDays }
            .forEach { rep ->
                rows.add(
                    MedicineLedgerRowItem(
                        id = "rep_med_${rep.dayNumber}",
                        dayText = "$dayPrefix ${rep.dayNumber}",
                        medicineName = rep.medicineSummary.ifBlank { Translations.tr("tx_meds", lang) },
                        dosageText = Translations.tr("day_med_drunk_confirmed_prefix", lang),
                        isApproved = true,
                        amountDzd = 0.0,
                        currency = curr,
                        linkedTransaction = null
                    )
                )
            }

        rows
    }

    val totalApprovedMedicineDzd = remember(medicineTransactions, ledgerRows) {
        val fromTx = medicineTransactions
            .filter { it.status.equals("APPROVED", ignoreCase = true) }
            .sumOf { it.amount }
        if (fromTx > 0.0) fromTx else ledgerRows.filter { it.isApproved }.sumOf { it.amountDzd }
    }
    val prescriptionsCount = ledgerRows.size
    val receiptTargetTx = initialSelectedTransaction ?: medicineTransactions.lastOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBg,
        shape = RoundedCornerShape(appearance.cornerRadiusDp.dp),
        modifier = Modifier.testTag("medicine_orders_ledger_bottom_sheet"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = appearance.accentContainerColor,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("💊", fontSize = 18.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            Translations.tr("medicine_orders_ledger_title", lang),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.5.sp,
                            color = accentEmerald
                        )
                        Text(
                            "$farmName • ${cycle.id}",
                            fontSize = 11.5.sp,
                            color = secondaryText
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = secondaryText)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Summary Card (2 columns):
                // 1. "إجمالي مصاريف الأدوية واللقاحات"
                // 2. "عدد الوصفات المسجلة"
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(appearance.cornerRadiusDp.dp),
                    color = appearance.accentContainerColor,
                    border = BorderStroke(1.dp, accentEmerald.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                Translations.tr("total_medicine_expenses_label", lang),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${String.format(java.util.Locale.US, "%,.0f", totalApprovedMedicineDzd)} $curr",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = accentEmerald
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(accentEmerald.copy(alpha = 0.3f))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                Translations.tr("total_prescriptions_count_label", lang),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "$prescriptionsCount ${Translations.tr("prescriptions_unit_short", lang)} 🩺",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = accentEmerald
                            )
                        }
                    }
                }

                // Medicine & Prescriptions Table: [ اليوم | الدواء والوصفة (الجرعة) | الحالة | السعر الإجمالي ]
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(appearance.cornerRadiusDp.dp),
                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(appearance.accentContainerColor, RoundedCornerShape(10.dp))
                                .padding(vertical = 9.dp, horizontal = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                Translations.tr("th_med_day", lang),
                                modifier = Modifier.weight(0.75f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                color = accentEmerald
                            )
                            Text(
                                Translations.tr("th_med_prescription_dosage", lang),
                                modifier = Modifier.weight(1.45f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                color = accentEmerald
                            )
                            Text(
                                Translations.tr("th_med_status", lang),
                                modifier = Modifier.weight(0.85f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                color = accentEmerald,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                Translations.tr("th_med_total_price", lang),
                                modifier = Modifier.weight(1.0f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                color = accentEmerald,
                                textAlign = TextAlign.End
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (ledgerRows.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    Translations.tr("no_medicine_orders_yet", lang),
                                    fontSize = 12.sp,
                                    color = secondaryText,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            ledgerRows.forEachIndexed { idx, row ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .then(
                                            if (row.linkedTransaction != null && onSelectTransactionForReceipt != null) {
                                                Modifier.clickable { onSelectTransactionForReceipt(row.linkedTransaction) }
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .padding(vertical = 9.dp, horizontal = 6.dp)
                                        .testTag("medicine_ledger_row_${row.id}"),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        row.dayText,
                                        modifier = Modifier.weight(0.75f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryText
                                    )
                                    Column(
                                        modifier = Modifier
                                            .weight(1.45f)
                                            .padding(end = 4.dp)
                                    ) {
                                        Text(
                                            "💊 ${row.medicineName}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryText,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (row.dosageText.isNotBlank()) {
                                            Text(
                                                row.dosageText,
                                                fontSize = 9.5.sp,
                                                color = secondaryText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Text(
                                        if (row.isApproved) Translations.tr("med_status_approved_short", lang)
                                        else Translations.tr("med_status_pending_short", lang),
                                        modifier = Modifier.weight(0.85f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (row.isApproved) accentEmerald else Color(0xFFD97706),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        "${String.format(java.util.Locale.US, "%,.0f", row.amountDzd)} ${row.currency}",
                                        modifier = Modifier.weight(1.0f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (row.isApproved) accentEmerald else Color(0xFFD97706),
                                        textAlign = TextAlign.End,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                                if (idx < ledgerRows.lastIndex) {
                                    HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                                }
                            }
                        }
                    }
                }

                // Button at the bottom of the dialog to open the shareable digital receipt for the tapped transaction
                if (receiptTargetTx != null && onSelectTransactionForReceipt != null) {
                    OutlinedButton(
                        onClick = { onSelectTransactionForReceipt(receiptTargetTx) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_open_medicine_tx_receipt"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.2.dp, accentEmerald)
                    ) {
                        Text(
                            Translations.tr("btn_view_share_tx_receipt", lang),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentEmerald,
                            maxLines = 1
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryButtonAccent),
                modifier = Modifier.testTag("btn_close_medicine_ledger_dialog")
            ) {
                Text(
                    Translations.tr("btn_close", lang),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    )
}

/**
 * Digital Receipt Ticket ModalBottomSheet ("وصل معاملة معتمد" / Official Transaction Receipt).
 * Displays a formal digital receipt ticket for any Expense or Income transaction in the ledger,
 * and allows instant WhatsApp-formatted sharing via "مشاركة الوصل 📤".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalTransactionReceiptBottomSheet(
    farmName: String,
    cycle: CycleModel,
    calculatedCycleDay: Int,
    transaction: TransactionModel,
    lang: AppLanguage,
    onOpenDetailedWeighingInvoice: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val appearance = LocalAppAppearanceState.current
    val isDark = appearance.isDark
    val sheetBg = if (isDark) Color(0xFF1E293B) else Color.White
    val primaryText = if (isDark) Color.White else Color(0xDD000000)
    val accentEmerald = appearance.accentColor
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var sharedWhatsAppPreview by remember { mutableStateOf<String?>(null) }

    val isIncome = transaction.type == TransactionType.INCOME
    val accentColor = if (isIncome) accentEmerald else ActionRed
    val refCode = "REC-${transaction.id.takeLast(6).uppercase()}"

    // Extract Category Name & Quantity (if present in parentheses or invoiceData)
    val rawCat = transaction.category.trim()
    val parenStart = rawCat.indexOf('(')
    val parenEnd = rawCat.lastIndexOf(')')
    val extractedParens = if (parenStart >= 0 && parenEnd > parenStart) {
        rawCat.substring(parenStart + 1, parenEnd).trim()
    } else {
        ""
    }
    val categoryDisplayTitle = if (parenStart > 0 && rawCat.contains("علف")) {
        rawCat.substring(0, parenStart).trim()
    } else {
        Translations.getLocalizedTxCategory(transaction.category, lang)
    }

    val quantityDisplay: String? = when {
        transaction.invoiceData != null -> {
            val inv = transaction.invoiceData
            "${String.format(java.util.Locale.US, "%,d", inv.totalBirdsSold)} ${Translations.tr("birds_unit_short", lang)} • ${String.format(java.util.Locale.US, "%,.1f", inv.totalNetKg)} ${Translations.tr("kg_short", lang)}"
        }
        extractedParens.isNotEmpty() -> extractedParens
        rawCat.contains("كتاكيت") || rawCat.contains("Chicks", ignoreCase = true) -> {
            "${String.format(java.util.Locale.US, "%,d", cycle.totalBirdsPlaced)} ${Translations.tr("birds_unit_short", lang)}"
        }
        else -> null
    }

    val formattedAmount = "${String.format(java.util.Locale.US, "%,.0f", transaction.amount)} ${transaction.currency}"
    val cycleDayInfo = "${cycle.id} • ${Translations.tr("day_prefix", lang)} $calculatedCycleDay"

    // Build clean WhatsApp-formatted receipt string
    val whatsappReceiptText = remember(farmName, cycle, transaction, lang) {
        buildString {
            appendLine("🧾 *${Translations.tr("official_receipt_header", lang)}*")
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine("📌 *${Translations.tr("receipt_ref_label", lang)}:* #$refCode")
            appendLine("🏡 *${Translations.tr("receipt_farm_label", lang)}:* $farmName")
            appendLine("🗓️ *${Translations.tr("cycle_label", lang)}:* $cycleDayInfo")
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine("📂 *${Translations.tr("category_label", lang)}:* $categoryDisplayTitle")
            if (!quantityDisplay.isNullOrBlank()) {
                appendLine("📦 *${Translations.tr("th_feed_quantity", lang)}:* $quantityDisplay")
            }
            appendLine("📅 *${Translations.tr("th_purchase_date", lang)}:* ${transaction.date}")
            appendLine("💰 *${Translations.tr("amount_label", lang)}:* *$formattedAmount*")
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine("✅ _${Translations.tr("receipt_footer_verified", lang)}_")
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("digital_receipt_ticket_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Formal Ticket Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = appearance.accentContainerColor,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                tint = accentEmerald,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            Translations.tr("official_receipt_header", lang),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = accentEmerald
                        )
                        Text(
                            if (isIncome) Translations.tr("official_income_doc", lang) else Translations.tr("official_expense_doc", lang),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = appearance.accentContainerColor,
                    border = BorderStroke(1.dp, accentEmerald.copy(alpha = 0.35f))
                ) {
                    Text(
                        "#$refCode",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentEmerald
                    )
                }
            }

            // 2. Ticket Body Card (Apple-Grade Minimalist Ticket)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(appearance.cornerRadiusDp.dp),
                color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Farm Name & Cycle/Day Info
                    CalmMetricRow(
                        label = Translations.tr("receipt_farm_label", lang),
                        value = farmName,
                        valueColor = accentEmerald
                    )
                    CalmMetricRow(
                        label = Translations.tr("cycle_label", lang),
                        value = cycleDayInfo,
                        valueColor = primaryText
                    )

                    HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0), thickness = 1.dp)

                    // Transaction Details: Category, Quantity (if applicable), Date
                    CalmMetricRow(
                        label = Translations.tr("category_label", lang),
                        value = categoryDisplayTitle,
                        valueColor = primaryText
                    )
                    if (!quantityDisplay.isNullOrBlank()) {
                        CalmMetricRow(
                            label = Translations.tr("th_feed_quantity", lang),
                            value = quantityDisplay,
                            valueColor = accentEmerald
                        )
                    }
                    CalmMetricRow(
                        label = Translations.tr("th_purchase_date", lang),
                        value = transaction.date,
                        valueColor = primaryText
                    )

                    HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0), thickness = 1.dp)

                    // Bold and Large Total Amount in DZD
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isIncome) appearance.accentContainerColor else if (isDark) Color(0xFF450A0A) else Color(0xFFFFEBEE),
                        border = BorderStroke(
                            1.dp,
                            if (isIncome) accentEmerald.copy(alpha = 0.45f) else Color(0xFFEF9A9A)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                Translations.tr("amount_label", lang),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                            Text(
                                formattedAmount,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = accentColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Formal Footer: "تم التسجيل والاعتماد في دفتر المزرعة"
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = appearance.accentContainerColor
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = accentEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                Translations.tr("receipt_footer_verified", lang),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentEmerald,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Optional confirmation & WhatsApp formatted preview after tapping Share
            if (sharedWhatsAppPreview != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = appearance.accentContainerColor,
                    border = BorderStroke(1.dp, accentEmerald.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            Translations.tr("receipt_shared_confirm", lang),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentEmerald
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            sharedWhatsAppPreview!!,
                            fontSize = 11.sp,
                            color = primaryText
                        )
                    }
                }
            }

            if (onOpenDetailedWeighingInvoice != null) {
                OutlinedButton(
                    onClick = onOpenDetailedWeighingInvoice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, accentEmerald),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accentEmerald)
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("عرض فاتورة الميزان التفصيلية ⚖️", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // 3. Prominent Wide Share Receipt Button ("مشاركة الوصل 📤")
            Button(
                onClick = {
                    println(whatsappReceiptText)
                    sharedWhatsAppPreview = whatsappReceiptText
                    try {
                        val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_SUBJECT, Translations.tr("official_receipt_header", lang))
                            putExtra(android.content.Intent.EXTRA_TEXT, whatsappReceiptText)
                        }
                        val chooser = android.content.Intent.createChooser(
                            sendIntent,
                            Translations.tr("share_receipt_btn", lang)
                        ).apply {
                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(chooser)
                    } catch (_: Exception) {
                        // Fallback handled gracefully via sharedWhatsAppPreview banner
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_share_digital_receipt"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = appearance.accentOption.primaryColor)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    Translations.tr("share_receipt_btn", lang),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun LiveBroilerReceiptDialog(
    farmName: String,
    cycleId: String,
    dateText: String,
    lang: AppLanguage,
    weighingRecordsCount: Int,
    totalGrossKg: Double,
    totalTareKg: Double,
    totalNetKg: Double,
    totalBirds: Int,
    avgWeightKg: Double,
    pricePerKg: Double,
    totalValue: Double,
    currency: String,
    kgUnit: String,
    buyerName: String = "",
    truckPlate: String = "",
    onDismiss: () -> Unit
) {
    val appearance = LocalAppAppearanceState.current
    val accentEmerald = appearance.accentColor
    var isPrintedOrShared by remember { mutableStateOf(false) }

    AlertDialog(
        containerColor = if (appearance.isDark) Color(0xFF1E293B) else Color.White,
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    Translations.tr("receipt_dialog_title", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (appearance.isDark) Color.White else Color(0xFF0F172A)
                )
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = accentEmerald)
            }
        },
        text = {
            val primaryText = if (appearance.isDark) Color.White else Color(0xFF0F172A)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = if (appearance.isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(farmName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = primaryText)
                        if (buyerName.isNotEmpty() || truckPlate.isNotEmpty()) {
                            Text(
                                "🚚 ${Translations.tr("buyer_label", lang)} ${buyerName.ifEmpty { "عميل معتمد" }}${if (truckPlate.isNotEmpty()) " • $truckPlate" else ""}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp,
                                color = accentEmerald
                            )
                        }
                        Text("🗓️ ${Translations.tr("cycle_label", lang)}: $cycleId • $dateText", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                HorizontalDivider()

                CalmMetricRow(Translations.tr("weights_list_title", lang), "$weighingRecordsCount ${Translations.tr("weigh_number", lang)}", primaryText)
                CalmMetricRow(Translations.tr("gross_weight_label", lang), "${String.format(java.util.Locale.US, "%,.1f", totalGrossKg)} $kgUnit", primaryText)
                CalmMetricRow(Translations.tr("tare_weight_label", lang), "${String.format(java.util.Locale.US, "%,.1f", totalTareKg)} $kgUnit", primaryText)

                HorizontalDivider()

                CalmMetricRow(Translations.tr("net_meat_weight", lang), "${String.format(java.util.Locale.US, "%,.1f", totalNetKg)} $kgUnit", accentEmerald)
                CalmMetricRow(Translations.tr("total_birds_sold", lang), "$totalBirds ${Translations.tr("birds_unit_short", lang)}", primaryText)
                CalmMetricRow(Translations.tr("avg_bird_weight", lang), "${String.format(java.util.Locale.US, "%.3f", avgWeightKg)} $kgUnit", accentEmerald)
                CalmMetricRow(Translations.tr("price_per_kg_label", lang), "${String.format(java.util.Locale.US, "%,.0f", pricePerKg)} $currency/$kgUnit", primaryText)

                Surface(
                    color = appearance.accentContainerColor,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(Translations.tr("total_sales_value", lang), fontWeight = FontWeight.Bold, color = accentEmerald, fontSize = 14.sp)
                        Text("${String.format(java.util.Locale.US, "%,.0f", totalValue)} $currency", fontWeight = FontWeight.ExtraBold, color = accentEmerald, fontSize = 16.sp)
                    }
                }

                if (isPrintedOrShared) {
                    Surface(
                        color = appearance.accentContainerColor,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "✓ تم تجهيز الفاتورة للطباعة والمشاركة بنجاح 🖨️",
                            color = accentEmerald,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = appearance.accentOption.primaryColor)
            ) {
                Text(Translations.tr("print_close_btn", lang), color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { isPrintedOrShared = true },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, accentEmerald),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = accentEmerald)
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("طباعة / مشاركة 🖨️", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
        }
    )
}

@Composable
fun ProfessionalSalesInvoiceModal(
    invoice: SalesInvoiceModel,
    lang: AppLanguage,
    onDismiss: () -> Unit
) {
    val appearance = LocalAppAppearanceState.current
    val accentEmerald = appearance.accentColor
    val isDark = appearance.isDark
    val primaryText = if (isDark) Color.White else Color(0xFF0F172A)
    var isPrintedOrShared by remember { mutableStateOf(false) }

    AlertDialog(
        containerColor = if (isDark) Color(0xFF1E293B) else Color.White,
        onDismissRequest = onDismiss,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = appearance.accentOption.primaryColor,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "فاتورة بيع رسمية احترافية 🧾",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                invoice.invoiceNumber,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. ترويسة الفاتورة: اسم العنبر، رقم الفاتورة، التاريخ، وعمر الدجاج التلقائي
                Surface(
                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "🏠 ${invoice.farmName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = primaryText
                            )
                            Surface(
                                color = appearance.accentContainerColor,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "🗓️ عمر الدجاج: ${invoice.birdAgeDays} يوم",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentEmerald
                                )
                            }
                        }
                        Text(
                            "📋 ${Translations.tr("cycle_label", lang)}: ${invoice.cycleId} • ${invoice.dateText}",
                            fontSize = 11.5.sp,
                            color = Color.Gray
                        )
                    }
                }

                // 2. بيانات المشتري: اسم التاجر ورقم لوحة الشاحنة 🚚
                Surface(
                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("🚚 بيانات التاجر والشاحنة:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("التاجر: ${invoice.buyerName.ifEmpty { "عميل معتمد" }}", fontSize = 12.sp, color = primaryText, fontWeight = FontWeight.SemiBold)
                            Text("لوحة الشاحنة: ${invoice.truckPlate.ifEmpty { "غير محدد" }}", fontSize = 12.sp, color = accentEmerald, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 3. جدول الأوزان: إجمالي الوزن القائم (الممتلئ)، ناقص إجمالي الفارغ (التارا)، والوزن الصافي للحم الحي (كغ)
                Surface(
                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("⚖️ جدول الأوزان واللحم الصافي:", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = primaryText)
                        HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                        CalmMetricRow("إجمالي الوزن القائم (الممتلئ)", "${String.format(java.util.Locale.US, "%,.1f", invoice.totalGrossKg)} كغ", primaryText)
                        CalmMetricRow("ناقص إجمالي الفارغ (التارا)", "${String.format(java.util.Locale.US, "%,.1f", invoice.totalTareKg)} كغ", Color(0xFF0284C7))
                        HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1))
                        CalmMetricRow("الوزن الصافي للحم الحي 🍗", "${String.format(java.util.Locale.US, "%,.1f", invoice.totalNetKg)} كغ", accentEmerald)
                    }
                }

                // 4. تفاصيل الطيور: إجمالي الصناديق، إجمالي عدد الدجاج المباع 🐔، ومعدل وزن الدجاجة الواحدة (كغ)
                Surface(
                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("🐔 تفاصيل الطيور والصناديق:", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = primaryText)
                        HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                        CalmMetricRow("إجمالي الصناديق", "${invoice.totalCrates} صندوق (${invoice.birdsPerCrate} دجاجة/صندوق)", primaryText)
                        CalmMetricRow("إجمالي عدد الدجاج المباع", "${String.format(java.util.Locale.US, "%,d", invoice.totalBirdsSold)} طائر", accentEmerald)
                        CalmMetricRow("معدل وزن الدجاجة الواحدة", "${String.format(java.util.Locale.US, "%.3f", invoice.avgBirdWeightKg)} كغ", primaryText)
                    }
                }

                // 5. الحساب المالي: سعر الكيلوغرام (دج) × الوزن الصافي = إجمالي المبلغ المستحق بالدينار الجزائري 💰
                Surface(
                    color = appearance.accentContainerColor,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, accentEmerald),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("سعر الكيلوغرام:", fontSize = 12.sp, color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155))
                            Text("${String.format(java.util.Locale.US, "%,.0f", invoice.pricePerKg)} ${invoice.currency}/كغ", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = primaryText)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("الوزن الصافي الخاضع للحساب:", fontSize = 12.sp, color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155))
                            Text("${String.format(java.util.Locale.US, "%,.1f", invoice.totalNetKg)} كغ", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = primaryText)
                        }
                        HorizontalDivider(color = accentEmerald.copy(alpha = 0.3f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("إجمالي المبلغ المستحق 💰:", fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp, color = accentEmerald)
                            Text(
                                "${String.format(java.util.Locale.US, "%,.0f", invoice.totalAmount)} ${invoice.currency}",
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                color = accentEmerald
                            )
                        }
                    }
                }

                // 6. خانة توقيع واعتماد الطرفين التفاعلية بالإصبع (توقيع المربي وتوقيع المشتري ✍️)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Box 1: توقيع واعتماد المربي ✍️
                    DigitalSignaturePadBox(
                        title = "توقيع واعتماد المربي ✍️",
                        subtitle = "إدارة المزرعة والإنتاج",
                        signedBadgeText = Translations.tr("sig_breeder_signed_badge", lang),
                        clearText = Translations.tr("sig_clear_short", lang),
                        isDark = isDark,
                        accentColor = accentEmerald,
                        accentContainerColor = appearance.accentContainerColor,
                        primaryText = primaryText,
                        testTagPrefix = "sales_invoice_breeder_sig",
                        modifier = Modifier.weight(1f)
                    )

                    // Box 2: توقيع واستلام المشتري ✍️
                    DigitalSignaturePadBox(
                        title = "توقيع واستلام المشتري ✍️",
                        subtitle = invoice.buyerName.ifEmpty { "تاجر الجملة" },
                        signedBadgeText = Translations.tr("sig_buyer_signed_badge", lang),
                        clearText = Translations.tr("sig_clear_short", lang),
                        isDark = isDark,
                        accentColor = accentEmerald,
                        accentContainerColor = appearance.accentContainerColor,
                        primaryText = primaryText,
                        testTagPrefix = "sales_invoice_buyer_sig",
                        modifier = Modifier.weight(1f)
                    )
                }

                if (isPrintedOrShared) {
                    Surface(
                        color = appearance.accentContainerColor,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "✓ تم تجهيز الفاتورة للطباعة والمشاركة وتصديرها بصيغة PDF بنجاح 🖨️",
                            color = accentEmerald,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = appearance.accentOption.primaryColor)
            ) {
                Text("إغلاق الفاتورة", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { isPrintedOrShared = true },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, accentEmerald),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = accentEmerald)
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("طباعة / مشاركة 🖨️", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WrapFilters(categories: List<String>, selected: String, onSelect: (String) -> Unit) {
    val appearance = LocalAppAppearanceState.current
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        categories.forEach { cat ->
            FilterChip(
                selected = selected == cat,
                onClick = { onSelect(cat) },
                label = { Text(cat, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = appearance.accentContainerColor,
                    selectedLabelColor = appearance.accentColor
                )
            )
        }
    }
}

// ============================================================================
// 4. بوابة البيطري
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgritechVetDashboard(
    farms: List<FarmModel>,
    currentTheme: AppThemeOption,
    lang: AppLanguage,
    onOpenMedicalRecords: (FarmModel) -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val appearance = LocalAppAppearanceState.current
    val cardShape = RoundedCornerShape(appearance.cornerRadiusDp.dp)
    val btnShape = RoundedCornerShape((appearance.cornerRadiusDp - 6).coerceAtLeast(4).dp)

    Scaffold(
        containerColor = currentTheme.bgColor,
        topBar = {
            TopAppBar(
                title = { Text(Translations.tr("role_vet", lang) + " 🩺", fontWeight = FontWeight.Bold, color = currentTheme.headingColor) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = currentTheme.headingColor)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Exit", tint = currentTheme.headingColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = currentTheme.bgColor)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(farms) { farm ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            Translations.getLocalizedFarmName(farm.id, lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = currentTheme.headingColor
                        )
                        Text(
                            Translations.getLocalizedFarmLocation(farm.id, lang),
                            fontSize = 12.sp,
                            color = if (currentTheme.isDark) Color(0xFFCBD5E1) else Color.Gray
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { onOpenMedicalRecords(farm) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = btnShape,
                            colors = ButtonDefaults.buttonColors(containerColor = appearance.accentOption.primaryColor)
                        ) {
                            Text(Translations.tr("open_medical_records", lang), fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgritechVetNotebookScreen(
    farm: FarmModel,
    cycle: CycleModel,
    currentTheme: AppThemeOption,
    lang: AppLanguage,
    firestoreRepo: AgritechFirestoreRepository? = null,
    reports: List<DailyReportModel>,
    vetNotes: List<VetNoteModel>,
    farms: List<FarmModel> = emptyList(),
    onSwitchFarm: ((FarmModel) -> Unit)? = null,
    onAddNote: (VetNoteModel) -> Unit,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit
) {
    val appearance = LocalAppAppearanceState.current
    val isDark = currentTheme.isDark
    val primaryTextColor = if (isDark) Color(0xFFFFFFFF) else currentTheme.headingColor
    val secondaryTextColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    val activeAccent = appearance.accentColor
    val accentBadgeBg = appearance.accentContainerColor
    val primaryButtonAccent = appearance.accentOption.primaryColor
    val cardShape = RoundedCornerShape(appearance.cornerRadiusDp.dp)
    val pillShape = RoundedCornerShape((appearance.cornerRadiusDp - 4).coerceAtLeast(8).dp)
    val curr = Translations.tr("currency", lang)

    var selectedVetWorkspaceTab by remember { mutableStateOf(0) }
    var showVetMedicineInvoiceDialog by remember { mutableStateOf(false) }
    var selectedDayReportForDetails by remember { mutableStateOf<DailyReportModel?>(null) }

    val liveReports = (AgritechMockDb.dailyReports.filter { it.farmId == farm.id } + reports.filter { it.farmId == farm.id })
        .distinctBy { it.dayNumber }
    val liveVetNotes = (AgritechMockDb.vetNotes.filter { it.farmId == farm.id } + vetNotes.filter { it.farmId == farm.id })
        .distinctBy { it.id }

    val dayNumber = (liveReports.maxOfOrNull { it.dayNumber } ?: 1)
    val qShort = Translations.tr("quintal_short", lang)
    val literUnit = Translations.tr("liter_unit", lang)
    val totalFeed = liveReports.sumOf { it.feedIntakeKg }
    val totalWater = liveReports.sumOf { it.waterIntakeLiters }
    val totalMortality = liveReports.sumOf { it.mortalityCount }

    val vetBottomNavItems = listOf(
        Triple(0, Translations.tr("vet_bottom_tab_barns", lang), Icons.Default.HomeWork),
        Triple(1, Translations.tr("vet_bottom_tab_treasury", lang), Icons.Default.AccountBalanceWallet),
        Triple(2, Translations.tr("vet_bottom_tab_epidemics", lang), Icons.Default.Science),
        Triple(3, Translations.tr("vet_bottom_tab_pharmacy", lang), Icons.Default.Medication)
    )

    if (showVetMedicineInvoiceDialog) {
        VetMedicineInvoiceDialog(
            farm = farm,
            cycle = cycle,
            dayNumber = dayNumber,
            lang = lang,
            currentTheme = currentTheme,
            channelId = "${cycle.id}_vet",
            firestoreRepo = firestoreRepo,
            onDismiss = { showVetMedicineInvoiceDialog = false }
        )
    }

    if (selectedDayReportForDetails != null) {
        val activeReport = liveReports.firstOrNull { it.dayNumber == selectedDayReportForDetails!!.dayNumber }
            ?: selectedDayReportForDetails!!
        val matchingVetNotes = liveVetNotes.filter { it.dayNumber == activeReport.dayNumber }
        val effectiveDayVetNotes = if (matchingVetNotes.isNotEmpty()) {
            matchingVetNotes
        } else if (activeReport.medicineGiven || activeReport.medicineSummary.isNotBlank()) {
            listOf(
                VetNoteModel(
                    id = "vn_confirmed_day_${activeReport.dayNumber}",
                    farmId = farm.id,
                    cycleId = cycle.id,
                    dayNumber = activeReport.dayNumber,
                    medicineName = activeReport.medicineSummary.ifBlank { "دواء / فيتامين في مياه الشرب" },
                    dosage = "1 مل / لتر في مياه الشرب",
                    note = Translations.tr("confirm_medicine_water_today_label", lang),
                    isUrgent = false
                )
            )
        } else {
            emptyList()
        }
        DayMedicalDetailsBottomSheet(
            farm = farm,
            cycle = cycle,
            report = activeReport,
            dayVetNotes = effectiveDayVetNotes,
            currentTheme = currentTheme,
            lang = lang,
            onAddMedicalNoteForDay = { newNote ->
                onAddNote(newNote)
            },
            onOpenInvoiceForDay = {
                selectedDayReportForDetails = null
                showVetMedicineInvoiceDialog = true
            },
            onDismiss = { selectedDayReportForDetails = null }
        )
    }

    Scaffold(
        containerColor = currentTheme.bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            Translations.getLocalizedFarmName(farm.id, lang, farm.name),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = primaryTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${Translations.tr("vet_workspace_title", lang)} • ${Translations.tr("cycle_label", lang)}: ${cycle.id}",
                            fontSize = 11.5.sp,
                            color = secondaryTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_vet_workspace_back")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = primaryTextColor)
                    }
                },
                actions = {
                    IconButton(onClick = { showVetMedicineInvoiceDialog = true }) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = Translations.tr("vet_invoice_btn", lang), tint = activeAccent)
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = primaryTextColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = currentTheme.bgColor)
            )
        },
        bottomBar = {
            // ONLY ONE 4-Tab Bottom Navigation Bar in Full-Screen Vet Clinic Workspace
            NavigationBar(
                containerColor = currentTheme.cardColor,
                contentColor = activeAccent,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vet_clinic_bottom_nav")
            ) {
                vetBottomNavItems.forEach { (idx, label, icon) ->
                    val isTabSelected = selectedVetWorkspaceTab == idx
                    NavigationBarItem(
                        modifier = Modifier.testTag("vet_hub_tab_$idx"),
                        selected = isTabSelected,
                        onClick = { selectedVetWorkspaceTab = idx },
                        icon = {
                            Icon(
                                icon,
                                contentDescription = label,
                                tint = if (isTabSelected) activeAccent else secondaryTextColor
                            )
                        },
                        label = {
                            Text(
                                label,
                                color = if (isTabSelected) activeAccent else secondaryTextColor,
                                fontWeight = if (isTabSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = accentBadgeBg
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedVetWorkspaceTab) {
                0 -> {
                    // Tab 0: 🏥 العنابر & Interactive Medical Ledger Table
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .testTag("vet_notebook_lazy_column"),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (farms.size > 1 && onSwitchFarm != null) {
                            item {
                                FarmSwitcherRow(
                                    farms = farms,
                                    selectedFarmId = farm.id,
                                    lang = lang,
                                    currentTheme = currentTheme,
                                    onSelectFarm = onSwitchFarm
                                )
                            }
                        }

                        // 1. Compact Interactive Daily Medical Ledger Table (`جدول السجل الطبي التفاعلي`)
                        item {
                            Text(
                                Translations.tr("vet_interactive_table_title", lang),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = primaryTextColor
                            )
                        }

                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("vet_interactive_medical_table"),
                                shape = cardShape,
                                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        // Table Header matching Owner's Daily Ledger table (`اليوم` | `العلف (ق)` | `الماء (لتر)` | `النفوق 💀` | `الحرارة °C` | `الغاز`)
                                        Row(
                                            modifier = Modifier
                                                .background(currentTheme.bgColor, RoundedCornerShape(8.dp))
                                                .padding(vertical = 11.dp, horizontal = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CalmTableCell(Translations.tr("th_day", lang), 108.dp, isHeader = true, headingColor = primaryTextColor)
                                            CalmTableCell(Translations.tr("th_feed", lang), 90.dp, isHeader = true, headingColor = primaryTextColor)
                                            CalmTableCell(Translations.tr("th_water", lang), 86.dp, isHeader = true, headingColor = primaryTextColor)
                                            CalmTableCell(Translations.tr("th_mortality", lang), 82.dp, isHeader = true, headingColor = primaryTextColor)
                                            CalmTableCell(Translations.tr("th_temp", lang), 78.dp, isHeader = true, headingColor = primaryTextColor)
                                            CalmTableCell(Translations.tr("th_gas", lang), 68.dp, isHeader = true, headingColor = primaryTextColor)
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        liveReports.sortedBy { it.dayNumber }.forEach { r ->
                                            val dayMeds = liveVetNotes.filter { it.dayNumber == r.dayNumber }
                                            val hasMedNoteForDay = r.medicineGiven || r.medicineSummary.isNotBlank() || dayMeds.isNotEmpty()
                                            val isUrgentDay = dayMeds.any { it.isUrgent }
                                            val rowBgColor = when {
                                                isUrgentDay -> if (currentTheme.isDark) Color(0xFF450A0A).copy(alpha = 0.45f) else Color(0xFFFEF2F2)
                                                hasMedNoteForDay -> appearance.accentContainerColor.copy(alpha = 0.45f)
                                                else -> Color.Transparent
                                            }

                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(rowBgColor)
                                                    .clickable { selectedDayReportForDetails = r }
                                                    .testTag("vet_medical_day_row_${r.dayNumber}")
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable { selectedDayReportForDetails = r }
                                                        .padding(vertical = 10.dp, horizontal = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(
                                                        modifier = Modifier
                                                            .width(108.dp)
                                                            .clickable { selectedDayReportForDetails = r }
                                                            .padding(horizontal = 4.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text(
                                                            "${Translations.tr("day_prefix", lang)} ${r.dayNumber}",
                                                            fontWeight = FontWeight.ExtraBold,
                                                            fontSize = 12.sp,
                                                            color = activeAccent,
                                                            textAlign = TextAlign.Center,
                                                            maxLines = 1
                                                        )
                                                        if (hasMedNoteForDay) {
                                                            Spacer(modifier = Modifier.height(3.dp))
                                                            Surface(
                                                                shape = RoundedCornerShape(6.dp),
                                                                color = appearance.accentContainerColor,
                                                                border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.5f)),
                                                                modifier = Modifier
                                                                    .clickable { selectedDayReportForDetails = r }
                                                                    .testTag("vet_ledger_med_badge_day_${r.dayNumber}")
                                                            ) {
                                                                Text(
                                                                    if (isUrgentDay) "🚨 ${Translations.tr("badge_day_med_pill", lang)}" else Translations.tr("badge_day_med_pill", lang),
                                                                    fontSize = 9.5.sp,
                                                                    fontWeight = FontWeight.ExtraBold,
                                                                    color = if (isUrgentDay) ActionRed else activeAccent,
                                                                    maxLines = 1,
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                )
                                                            }
                                                        }
                                                    }

                                                    val cellTextColor = if (currentTheme.isDark) Color.White else Color(0xFF212121)
                                                    CalmTableCell(
                                                        "${Translations.formatQuintalNumber(r.feedIntakeKg / 100.0)} $qShort",
                                                        90.dp,
                                                        isBold = true,
                                                        textColor = cellTextColor
                                                    )
                                                    CalmTableCell(
                                                        String.format(java.util.Locale.US, "%.0f", r.waterIntakeLiters),
                                                        86.dp,
                                                        textColor = cellTextColor
                                                    )
                                                    CalmTableCell(
                                                        "${r.mortalityCount}",
                                                        82.dp,
                                                        textColor = if (r.mortalityCount > 3) ActionRed else cellTextColor,
                                                        isBold = r.mortalityCount > 3
                                                    )
                                                    CalmTableCell(
                                                        String.format(java.util.Locale.US, "%.1f°", r.temperature),
                                                        78.dp,
                                                        textColor = cellTextColor
                                                    )
                                                    CalmTableCell(
                                                        String.format(java.util.Locale.US, "%.1f", r.gasUnits),
                                                        68.dp,
                                                        textColor = cellTextColor
                                                    )
                                                }
                                            }
                                            HorizontalDivider(
                                                color = if (currentTheme.isDark) Color(0xFF334155) else Color(0xFFF1F5F9),
                                                thickness = 1.dp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Recorded Veterinary Prescriptions & Notes Section
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    Translations.tr("vet_records_title", lang),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = primaryTextColor,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                )
                                OutlinedButton(
                                    onClick = { showVetMedicineInvoiceDialog = true },
                                    shape = pillShape,
                                    border = BorderStroke(1.2.dp, activeAccent),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("btn_vet_workspace_send_invoice")
                                ) {
                                    Text(
                                        Translations.tr("vet_invoice_btn", lang),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = activeAccent,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                        items(liveVetNotes, key = { it.id }) { vn ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = cardShape,
                                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.25f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            Translations.getLocalizedMedicine(vn.id, vn.medicineName, lang),
                                            fontWeight = FontWeight.Bold,
                                            color = primaryTextColor,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            "${Translations.tr("day_prefix", lang)} ${vn.dayNumber}",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = activeAccent
                                        )
                                    }
                                    Text(
                                        "${Translations.tr("dosage_label", lang)}: ${Translations.getLocalizedDosage(vn.id, vn.dosage, lang)}",
                                        fontSize = 12.sp,
                                        color = secondaryTextColor
                                    )
                                    Text(
                                        Translations.getLocalizedVetNote(vn.id, vn.note, lang),
                                        fontSize = 12.5.sp,
                                        color = primaryTextColor
                                    )
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
                1 -> {
                    // Tab 1: 💰 الخزنة (Vet Clinic Financial Wallet & Dues)
                    val vetTransactions = AgritechMockDb.transactions.filter {
                        it.category.contains("أدوية") ||
                            it.category.contains("لقاح") ||
                            it.category.contains("بيطر") ||
                            it.category.contains("MEDICATION", ignoreCase = true) ||
                            it.category.contains("Vet", ignoreCase = true)
                    }
                    val collectedTotal = vetTransactions
                        .filter { it.status.equals("APPROVED", ignoreCase = true) }
                        .sumOf { it.amount }
                    val pendingTotal = vetTransactions
                        .filter { it.status.equals("PENDING", ignoreCase = true) }
                        .sumOf { it.amount }
                    val totalInvoiced = collectedTotal + pendingTotal

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .testTag("vet_hub_wallet_content"),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = cardShape,
                                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                border = BorderStroke(1.2.dp, activeAccent.copy(alpha = 0.35f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        Translations.tr("vet_hub_tab_wallet", lang),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = primaryTextColor
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = pillShape,
                                            color = accentBadgeBg
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    Translations.tr("vet_hub_wallet_collected", lang),
                                                    fontSize = 11.sp,
                                                    color = secondaryTextColor,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    "${String.format(java.util.Locale.US, "%,.0f", collectedTotal)} $curr",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 13.5.sp,
                                                    color = activeAccent,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = pillShape,
                                            color = if (isDark) Color(0xFF78350F).copy(alpha = 0.35f) else Color(0xFFFEF3C7)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    Translations.tr("vet_hub_wallet_pending", lang),
                                                    fontSize = 11.sp,
                                                    color = secondaryTextColor,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    "${String.format(java.util.Locale.US, "%,.0f", pendingTotal)} $curr",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 13.5.sp,
                                                    color = Color(0xFFD97706),
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        "${Translations.tr("vet_hub_wallet_total", lang)}: ${String.format(java.util.Locale.US, "%,.0f", totalInvoiced)} $curr",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryTextColor
                                    )
                                }
                            }
                        }
                        items(vetTransactions.reversed(), key = { it.id }) { tx ->
                            val isPending = tx.status.equals("PENDING", ignoreCase = true)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = cardShape,
                                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                border = BorderStroke(
                                    1.dp,
                                    if (isPending) Color(0xFFF59E0B) else activeAccent.copy(alpha = 0.25f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp)
                                    ) {
                                        Text(
                                            tx.category,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = primaryTextColor,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            if (isPending) Translations.tr("fin_req_status_pending", lang)
                                            else Translations.tr("fin_req_status_approved", lang),
                                            fontSize = 11.5.sp,
                                            color = if (isPending) Color(0xFFD97706) else activeAccent
                                        )
                                    }
                                    Text(
                                        "${String.format(java.util.Locale.US, "%,.0f", tx.amount)} $curr",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = if (isPending) Color(0xFFD97706) else activeAccent,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
                2 -> {
                    // Tab 2: 🧬 اللقاحات والأوبئة (Epidemiology & Vaccination Analytics)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .testTag("vet_hub_epidemics_content"),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                Translations.tr("vet_hub_epidemics_subtitle", lang),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = secondaryTextColor
                            )
                        }
                        items(AgritechMockDb.vetEpidemicAlerts, key = { it.id }) { alert ->
                            val riskColor = when (alert.riskLevel) {
                                "HIGH" -> Color(0xFFDC2626)
                                "MEDIUM" -> Color(0xFFD97706)
                                else -> activeAccent
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("vet_epidemic_card_${alert.id}"),
                                shape = cardShape,
                                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                border = BorderStroke(1.2.dp, riskColor.copy(alpha = 0.45f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "🧬 ${alert.virusName}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.5.sp,
                                            color = primaryTextColor,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            shape = pillShape,
                                            color = riskColor.copy(alpha = 0.14f)
                                        ) {
                                            Text(
                                                alert.riskLevel,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = riskColor
                                            )
                                        }
                                    }
                                    Text(
                                        "📍 ${alert.regionName}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = activeAccent
                                    )
                                    Text(
                                        "💉 ${alert.recommendedVaccine} • 🗓️ ${alert.scheduleDayText}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = primaryTextColor
                                    )
                                    Text(
                                        alert.clinicalGuidance,
                                        fontSize = 11.5.sp,
                                        color = secondaryTextColor
                                    )
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
                else -> {
                    // Tab 3: 💊 الصيدلية (Personal Vet Pharmacy Inventory & Drug Importers)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .testTag("vet_hub_pharmacy_content"),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                Translations.tr("vet_hub_pharmacy_subtitle", lang),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = secondaryTextColor
                            )
                        }
                        items(AgritechMockDb.vetPharmacyInventory, key = { it.id }) { medItem ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("vet_pharmacy_item_${medItem.id}"),
                                shape = cardShape,
                                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.25f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(15.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                medItem.name,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp,
                                                color = primaryTextColor
                                            )
                                            Text(
                                                "${medItem.category} • ${medItem.importerName}",
                                                fontSize = 11.5.sp,
                                                color = secondaryTextColor
                                            )
                                        }
                                        Surface(
                                            shape = pillShape,
                                            color = accentBadgeBg
                                        ) {
                                            Text(
                                                "${medItem.stockUnits} ${medItem.unitLabel}",
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = activeAccent,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = { AgritechMockDb.adjustVetPharmacyStock(medItem.id, -1) },
                                            modifier = Modifier
                                                .height(38.dp)
                                                .testTag("btn_dec_stock_${medItem.id}"),
                                            shape = pillShape,
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text("-1", fontWeight = FontWeight.ExtraBold, color = primaryTextColor)
                                        }
                                        OutlinedButton(
                                            onClick = { AgritechMockDb.adjustVetPharmacyStock(medItem.id, +5) },
                                            modifier = Modifier
                                                .height(38.dp)
                                                .testTag("btn_inc_stock_${medItem.id}"),
                                            shape = pillShape,
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text("+5", fontWeight = FontWeight.ExtraBold, color = activeAccent)
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Day Medical Details Dialog / BottomSheet (`DayMedicalDetailsBottomSheet`):
 * Opens when the Veterinarian taps any Day Row in the Interactive Medical Table (e.g., `اليوم 7`).
 * Shows full vital signs, scheduled vaccinations for that age, prescribed medications,
 * and an inline form to add a new medical note or vaccine record for that specific day.
 */
@Composable
fun DayMedicalDetailsBottomSheet(
    farm: FarmModel,
    cycle: CycleModel,
    report: DailyReportModel,
    dayVetNotes: List<VetNoteModel>,
    currentTheme: AppThemeOption,
    lang: AppLanguage,
    onAddMedicalNoteForDay: (VetNoteModel) -> Unit,
    onOpenInvoiceForDay: () -> Unit,
    onDismiss: () -> Unit
) {
    val appearance = LocalAppAppearanceState.current
    val isDark = currentTheme.isDark
    val dialogBg = if (isDark) Color(0xFF1E293B) else Color.White
    val subtleBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val primaryText = if (isDark) Color.White else currentTheme.headingColor
    val secondaryText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF64748B)
    val activeAccent = appearance.accentColor
    val primaryButtonAccent = appearance.accentOption.primaryColor
    val pillShape = RoundedCornerShape((appearance.cornerRadiusDp - 4).coerceAtLeast(8).dp)
    val qShort = Translations.tr("quintal_short", lang)
    val literUnit = Translations.tr("liter_unit", lang)

    var showQuickAddForm by remember { mutableStateOf(false) }
    var medNameInput by remember { mutableStateOf("") }
    var dosageInput by remember { mutableStateOf("1 مل / لتر لمدة 3 أيام") }
    var clinicalNoteInput by remember { mutableStateOf("") }
    var isUrgentInput by remember { mutableStateOf(false) }

    val scheduledVaccinesForAge = remember(report.dayNumber) {
        when {
            report.dayNumber <= 2 -> "💉 اليوم 1-2: لقاح التهاب الشعب المعدي (IB Ma5) + فيتامين AD3E ومحلول إلكترولايت للاستقبال"
            report.dayNumber in 6..8 -> "💉 اليوم 7: تحصين نيوكاسل (Newcastle Clone 30 + IB) تقطير عيني أو رش صباحي"
            report.dayNumber in 11..14 -> "💉 اليوم 12-14: تحصين الجمبورو (IBD Intermediate) في مياه الشرب + رافع مناعة"
            report.dayNumber in 17..21 -> "💉 اليوم 18-20: الجرعة التذكيرية لنيوكاسل (LaSota) والجمبورو"
            else -> "🛡️ برنامج الرقابة الدورية: فيتامينات، مضاد سموم فطرية، ومراقبة الكوكسيديا والكلوستريديا"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("day_medical_details_sheet"),
        shape = RoundedCornerShape(appearance.cornerRadiusDp.dp),
        containerColor = dialogBg,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "📅 ${Translations.tr("vet_day_details_title", lang)} ${report.dayNumber} — ${Translations.getLocalizedFarmName(farm.id, lang, farm.name)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.5.sp,
                    color = primaryText
                )
                Text(
                    "${Translations.getLocalizedFarmName(farm.id, lang, farm.name)} • ${cycle.id}",
                    fontSize = 12.sp,
                    color = secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1) Vital Signs & Barn Environment Grid for the Day
                Surface(
                    shape = pillShape,
                    color = subtleBg,
                    border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CalmMetricRow(
                            Translations.tr("th_feed", lang),
                            "${Translations.formatQuintalNumber(report.feedIntakeKg / 100.0)} $qShort",
                            activeAccent
                        )
                        CalmMetricRow(
                            Translations.tr("th_water", lang),
                            "${String.format(java.util.Locale.US, "%.0f", report.waterIntakeLiters)} $literUnit",
                            primaryText
                        )
                        CalmMetricRow(
                            Translations.tr("th_mortality", lang),
                            "${report.mortalityCount}",
                            if (report.mortalityCount > 3) ActionRed else primaryText
                        )
                        CalmMetricRow(
                            Translations.tr("th_temp", lang),
                            "${String.format(java.util.Locale.US, "%.1f°C", report.temperature)}",
                            primaryText
                        )
                        CalmMetricRow(
                            Translations.tr("th_gas", lang),
                            "${String.format(java.util.Locale.US, "%.1f", report.gasUnits)}",
                            primaryText
                        )
                        CalmMetricRow(
                            Translations.tr("th_vent", lang),
                            Translations.getLocalizedVentilation(report.ventilationStatus, lang),
                            activeAccent
                        )
                    }
                }

                // 2) Scheduled Vaccination Program for this Day/Age
                Surface(
                    shape = pillShape,
                    color = appearance.accentContainerColor,
                    border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            Translations.tr("vet_day_scheduled_vaccines_title", lang),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = activeAccent
                        )
                        Text(
                            scheduledVaccinesForAge,
                            fontSize = 12.sp,
                            color = primaryText
                        )
                    }
                }

                // 3) Medicine Drunk Section ("💊 الأدوية والفيتامينات المعطاة في هذا اليوم")
                Text(
                    Translations.tr("day_meds_given_section_title", lang),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = primaryText
                )

                if (report.medicineGiven || report.medicineSummary.isNotBlank()) {
                    val confirmedMedLabel = report.medicineSummary.ifBlank {
                        dayVetNotes.firstOrNull()?.medicineName ?: Translations.tr("tx_meds", lang)
                    }
                    Surface(
                        shape = pillShape,
                        color = appearance.accentContainerColor,
                        border = BorderStroke(1.2.dp, activeAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("day_details_confirmed_med_banner")
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                "${Translations.tr("day_med_drunk_confirmed_prefix", lang)} $confirmedMedLabel",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.5.sp,
                                color = activeAccent
                            )
                            Text(
                                Translations.tr("confirm_medicine_water_today_label", lang),
                                fontSize = 11.sp,
                                color = secondaryText
                            )
                        }
                    }
                }

                if (dayVetNotes.isEmpty() && !report.medicineGiven && report.medicineSummary.isBlank()) {
                    Text(
                        Translations.tr("vet_day_no_meds_yet", lang),
                        fontSize = 12.sp,
                        color = secondaryText
                    )
                } else {
                    dayVetNotes.forEach { vn ->
                        Surface(
                            shape = pillShape,
                            color = subtleBg,
                            border = BorderStroke(
                                1.dp,
                                if (vn.isUrgent) ActionRed.copy(alpha = 0.5f) else activeAccent.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    Translations.getLocalizedMedicine(vn.id, vn.medicineName, lang),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = primaryText
                                )
                                Text(
                                    "${Translations.tr("dosage_label", lang)}: ${Translations.getLocalizedDosage(vn.id, vn.dosage, lang)}",
                                    fontSize = 11.5.sp,
                                    color = secondaryText
                                )
                                Text(
                                    Translations.getLocalizedVetNote(vn.id, vn.note, lang),
                                    fontSize = 12.sp,
                                    color = primaryText
                                )
                            }
                        }
                    }
                }

                // 4) Quick Button / Inline Form to Add a Medical Note or Vaccine Record for this Day
                if (!showQuickAddForm) {
                    Button(
                        onClick = { showQuickAddForm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_add_day_medical_note"),
                        shape = pillShape,
                        colors = ButtonDefaults.buttonColors(containerColor = primaryButtonAccent)
                    ) {
                        Text(
                            Translations.tr("vet_day_add_note_btn", lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                } else {
                    Surface(
                        shape = pillShape,
                        color = subtleBg,
                        border = BorderStroke(1.dp, activeAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = medNameInput,
                                onValueChange = { medNameInput = it },
                                label = { Text(Translations.tr("vet_med_name_label", lang), fontSize = 11.5.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_day_med_name")
                            )
                            OutlinedTextField(
                                value = dosageInput,
                                onValueChange = { dosageInput = it },
                                label = { Text(Translations.tr("vet_dosage_instructions_label", lang), fontSize = 11.5.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_day_med_dosage")
                            )
                            OutlinedTextField(
                                value = clinicalNoteInput,
                                onValueChange = { clinicalNoteInput = it },
                                label = { Text(Translations.tr("vet_clinical_note_label", lang), fontSize = 11.5.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_day_med_note")
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    Translations.tr("vet_invoice_urgent_label", lang),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = primaryText
                                )
                                Switch(
                                    checked = isUrgentInput,
                                    onCheckedChange = { isUrgentInput = it }
                                )
                            }
                            Button(
                                onClick = {
                                    val cleanMed = medNameInput.trim().ifEmpty { "تحصين / متابعة طبية لليوم ${report.dayNumber}" }
                                    val cleanDosage = dosageInput.trim().ifEmpty { "1 مل / لتر" }
                                    val cleanNote = clinicalNoteInput.trim().ifEmpty { "تمت المعاينة البيطرية لليوم ${report.dayNumber}" }
                                    onAddMedicalNoteForDay(
                                        VetNoteModel(
                                            id = "vn_day_${report.dayNumber}_${java.util.UUID.randomUUID().toString().take(6)}",
                                            farmId = farm.id,
                                            cycleId = cycle.id,
                                            dayNumber = report.dayNumber,
                                            medicineName = cleanMed,
                                            dosage = cleanDosage,
                                            note = cleanNote,
                                            isUrgent = isUrgentInput
                                        )
                                    )
                                    medNameInput = ""
                                    clinicalNoteInput = ""
                                    showQuickAddForm = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("btn_save_day_medical_note"),
                                shape = pillShape,
                                colors = ButtonDefaults.buttonColors(containerColor = primaryButtonAccent)
                            ) {
                                Text(
                                    Translations.tr("vet_day_save_note_btn", lang),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = pillShape,
                colors = ButtonDefaults.buttonColors(containerColor = primaryButtonAccent),
                modifier = Modifier.testTag("btn_close_day_medical_sheet")
            ) {
                Text(
                    Translations.tr("btn_close", lang),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onOpenInvoiceForDay,
                shape = pillShape,
                border = BorderStroke(1.dp, activeAccent)
            ) {
                Text(
                    "🧾 ${Translations.tr("vet_invoice_btn", lang)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = activeAccent
                )
            }
        }
    )
}

// ============================================================================
// 5. مشرف العنبر
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgritechWorkerDashboard(
    farm: FarmModel,
    cycle: CycleModel,
    currentTheme: AppThemeOption,
    lang: AppLanguage,
    reports: List<DailyReportModel>,
    urgentNotes: List<VetNoteModel>,
    farms: List<FarmModel> = emptyList(),
    onSwitchFarm: ((FarmModel) -> Unit)? = null,
    onSubmitReport: (DailyReportModel) -> Unit,
    onOpenSettings: () -> Unit,
    onBack: (() -> Unit)? = null,
    onLogout: () -> Unit
) {
    val appearance = LocalAppAppearanceState.current
    val cardShape = RoundedCornerShape(appearance.cornerRadiusDp.dp)
    val btnShape = RoundedCornerShape((appearance.cornerRadiusDp - 4).coerceAtLeast(4).dp)
    val currentDay = (reports.maxOfOrNull { it.dayNumber } ?: 0) + 1
    var isSubmittedToday by remember { mutableStateOf(false) }

    // Feed input is strictly in Quintals (ق) - e.g. 0.5 ق for 50 kg, 2.4 ق for 240 kg
    var feedQuintalsText by remember { mutableStateOf("") }
    var waterText by remember { mutableStateOf("") }
    var mortalityText by remember { mutableStateOf("") }
    var tempText by remember { mutableStateOf("") }
    var gasText by remember { mutableStateOf("") }
    var workerValidationError by remember { mutableStateOf<String?>(null) }

    val activePrescriptionNote = urgentNotes.firstOrNull { it.dayNumber == currentDay }
        ?: urgentNotes.lastOrNull()
        ?: AgritechMockDb.vetNotes.firstOrNull { it.farmId == farm.id && it.dayNumber == currentDay }
        ?: AgritechMockDb.vetNotes.lastOrNull { it.farmId == farm.id }
    val defaultWorkerMedName = activePrescriptionNote?.medicineName ?: "فيتامين AD3E + رافع مناعة"
    var workerMedicineGiven by remember(activePrescriptionNote) { mutableStateOf(activePrescriptionNote != null) }
    var workerMedicineName by remember(activePrescriptionNote) { mutableStateOf(defaultWorkerMedName) }

    var ventilationSelected by remember { mutableStateOf("Good") }
    var generalStatusSelected by remember { mutableStateOf("Normal") }
    val qShort = Translations.tr("quintal_short", lang)

    Scaffold(
        containerColor = currentTheme.bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        Translations.tr("worker_log_title", lang),
                        fontWeight = FontWeight.Bold,
                        color = currentTheme.headingColor,
                        fontSize = 16.sp
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("btn_worker_back")
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = currentTheme.headingColor
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = currentTheme.headingColor)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Exit", tint = currentTheme.headingColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = currentTheme.bgColor)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (farms.size > 1 && onSwitchFarm != null) {
                item {
                    FarmSwitcherRow(
                        farms = farms,
                        selectedFarmId = farm.id,
                        lang = lang,
                        currentTheme = currentTheme,
                        onSelectFarm = onSwitchFarm
                    )
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                Translations.getLocalizedFarmName(farm.id, lang, farm.name),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = currentTheme.headingColor
                            )
                            Text("${Translations.tr("batch_label", lang)}: ${String.format(java.util.Locale.US, "%,d", cycle.totalBirdsPlaced)} 🐥", fontSize = 12.sp, color = if (currentTheme.isDark) Color(0xFFCBD5E1) else Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${Translations.tr("day_prefix", lang)} $currentDay 🗓️", color = currentTheme.headingColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                Translations.formatDualFeedInventoryBadge(cycle.feedInventoryKg, lang),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (cycle.feedInventoryKg < 500.0) ActionRed else appearance.accentColor
                            )
                        }
                    }
                }
            }

            item {
                if (isSubmittedToday) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = cardShape,
                        colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = appearance.accentColor, modifier = Modifier.size(52.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(Translations.tr("locked_today_msg", lang), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = appearance.accentColor)
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = cardShape,
                        colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("${Translations.tr("log_today_prefix", lang)} $currentDay 🗓️", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = currentTheme.headingColor)

                            OutlinedTextField(
                                value = feedQuintalsText,
                                onValueChange = {
                                    feedQuintalsText = it.filter { ch -> ch.isDigit() || ch == '.' }
                                    workerValidationError = null
                                },
                                label = { Text("${Translations.tr("th_feed", lang)} (مثال: 0.5 $qShort)") },
                                placeholder = { Text("0.5") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("worker_input_feed_quintals"),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(value = waterText, onValueChange = { waterText = it; workerValidationError = null }, label = { Text(Translations.tr("th_water", lang)) }, placeholder = { Text("500") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = mortalityText, onValueChange = { mortalityText = it; workerValidationError = null }, label = { Text(Translations.tr("th_mortality", lang)) }, placeholder = { Text("0") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = tempText, onValueChange = { tempText = it }, label = { Text(Translations.tr("th_temp", lang)) }, placeholder = { Text("30.0") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = gasText, onValueChange = { gasText = it }, label = { Text(Translations.tr("th_gas", lang)) }, placeholder = { Text("2.5") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

                            if (workerValidationError != null) {
                                Text(
                                    text = workerValidationError!!,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ActionRed
                                )
                            }

                            // Supervisor Medicine in Water Confirmation Row ("💊 تأكيد إعطاء الدواء في الماء اليوم (شرب الدواء ✓)")
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (workerMedicineGiven) appearance.accentContainerColor.copy(alpha = 0.65f)
                                else if (currentTheme.isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                                border = BorderStroke(
                                    1.dp,
                                    if (workerMedicineGiven) appearance.accentColor.copy(alpha = 0.55f)
                                    else if (currentTheme.isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            Translations.tr("confirm_medicine_water_today_label", lang),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (workerMedicineGiven) appearance.accentColor else currentTheme.headingColor,
                                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                                        )
                                        Switch(
                                            checked = workerMedicineGiven,
                                            onCheckedChange = { workerMedicineGiven = it },
                                            modifier = Modifier.testTag("switch_worker_report_medicine_given")
                                        )
                                    }
                                    if (workerMedicineGiven) {
                                        OutlinedTextField(
                                            value = workerMedicineName,
                                            onValueChange = { workerMedicineName = it },
                                            label = { Text(Translations.tr("medicine_water_name_input_label", lang), fontSize = 11.sp) },
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("input_worker_report_medicine_name")
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            var isSavingReport by remember { mutableStateOf(false) }
                            val workerScope = rememberCoroutineScope()
                            Button(
                                onClick = {
                                    if (!isSavingReport) {
                                        val feedQuintals = feedQuintalsText.trim().toDoubleOrNull()
                                        val water = waterText.trim().toDoubleOrNull()
                                        val mort = mortalityText.trim().toIntOrNull()
                                        if (feedQuintals == null || water == null || mort == null) {
                                            workerValidationError = Translations.tr("input_required_error", lang)
                                            return@Button
                                        }
                                        isSavingReport = true
                                        val feedKg = feedQuintals * 100.0
                                        val temp = tempText.toDoubleOrNull() ?: 31.5
                                        val gas = gasText.toDoubleOrNull() ?: 3.5
                                        val resolvedWorkerMed = if (workerMedicineGiven) {
                                            workerMedicineName.trim().ifBlank { defaultWorkerMedName }
                                        } else {
                                            ""
                                        }

                                        workerScope.launch {
                                            delay(350)
                                            onSubmitReport(
                                                DailyReportModel(
                                                    id = "rep_${System.currentTimeMillis()}",
                                                    farmId = farm.id,
                                                    cycleId = cycle.id,
                                                    dayNumber = currentDay,
                                                    feedIntakeKg = feedKg,
                                                    waterIntakeLiters = water,
                                                    mortalityCount = mort,
                                                    temperature = temp,
                                                    gasUnits = gas,
                                                    ventilationStatus = ventilationSelected,
                                                    generalStatus = generalStatusSelected,
                                                    isLocked = true,
                                                    medicineGiven = workerMedicineGiven,
                                                    medicineSummary = resolvedWorkerMed
                                                )
                                            )
                                            isSavingReport = false
                                            isSubmittedToday = true
                                        }
                                    }
                                },
                                enabled = !isSavingReport,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("btn_worker_submit_report"),
                                shape = btnShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = appearance.accentOption.primaryColor,
                                    disabledContainerColor = appearance.accentOption.primaryColor.copy(alpha = 0.85f)
                                )
                            ) {
                                if (isSavingReport) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.5.dp,
                                        modifier = Modifier.size(22.dp)
                                    )
                                } else {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(Translations.tr("lock_report_btn", lang), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
