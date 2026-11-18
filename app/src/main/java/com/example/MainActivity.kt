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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.graphics.graphicsLayer
import com.example.models.*
import com.example.ui.BookAdviceNavigationFlow
import com.example.ui.BookBorderInner
import com.example.ui.BookBorderOuter
import com.example.ui.BookBurgundy
import com.example.ui.BookBurgundyDark
import com.example.ui.BookGoldAccent
import com.example.ui.BookParchmentLight
import com.example.ui.BookParchmentMid
import com.example.ui.BookSepiaInk
import com.example.ui.EncyclopediaInfographicPosterCard
import com.example.ui.VintageSpiralBookPageContainer
import com.example.ui.theme.PoultryTheme
import com.example.utils.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.coroutineScope
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
        FarmModel("farm_1", "عنبر الخيرات والبركة 1", "البليدة - بوفاريك", 8000, "worker_1"),
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
            clientName = "عنبر الخيرات والبركة 1",
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

    val initialWorkerPersonalEntriesSeed = listOf(
        WorkerPersonalEntryModel(
            id = "w_tx_salary_1",
            farmId = "personal_worker_ledger",
            title = "💵 الراتب والدخل الشهري",
            amount = 65000.0,
            type = TransactionType.INCOME,
            dateLabel = "2026-11-01",
            isBillOrObligation = false
        ),
        WorkerPersonalEntryModel(
            id = "w_tx_elec_1",
            farmId = "personal_worker_ledger",
            title = "⚡ كهرباء وغاز",
            amount = 4500.0,
            type = TransactionType.EXPENSE,
            dateLabel = "2026-11-01",
            isBillOrObligation = true
        ),
        WorkerPersonalEntryModel(
            id = "w_tx_net_1",
            farmId = "personal_worker_ledger",
            title = "🌐 إنترنت وجوال",
            amount = 2600.0,
            type = TransactionType.EXPENSE,
            dateLabel = "2026-11-02",
            isBillOrObligation = true
        ),
        WorkerPersonalEntryModel(
            id = "w_tx_car_1",
            farmId = "personal_worker_ledger",
            title = "🚗 تأمين وصيانة سيارة",
            amount = 6000.0,
            type = TransactionType.EXPENSE,
            dateLabel = "2026-11-02",
            isBillOrObligation = true
        ),
        WorkerPersonalEntryModel(
            id = "w_tx_groc_1",
            farmId = "personal_worker_ledger",
            title = "🛒 مقاضي ومصاريف البيت",
            amount = 18500.0,
            type = TransactionType.EXPENSE,
            dateLabel = "2026-11-03",
            isBillOrObligation = true
        )
    )

    val workerPersonalEntries = mutableStateListOf<WorkerPersonalEntryModel>().apply {
        addAll(initialWorkerPersonalEntriesSeed)
    }

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
            text = "السلام عليكم، تم تسجيل التقرير اليومي: استهلاك العلف 5.1 ق والمياه 980 لتر، والحرارة مستقرة عند 30.0°C.",
            timestamp = "08:15 ص"
        ),
        ChatMessageModel(
            id = "msg_w2",
            cycleId = "cycle_2026_01_worker",
            senderId = "owner_1",
            senderName = "مالك المزرعة",
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
            text = "حاضر، تم تفقد خطوط المياه وضبط ارتفاع المعالف بالكامل ✓",
            timestamp = "08:40 ص"
        ),

        // 2. Contextual Vet Chat Messages (Vet Notes Tab)
        ChatMessageModel(
            id = "msg_v1",
            cycleId = "cycle_2026_01_vet",
            senderId = "vet_1",
            senderName = "د. كمال",
            senderRole = "طبيب بيطري 🩺",
            text = "السلام عليكم، بناءً على فحص اليوم الخامس يُنصح بإعطاء جرعة فيتامين AD3E + أملاح معدنية (1 مل / لتر) لمدة 3 أيام لدعم النمو.",
            timestamp = "09:10 ص"
        ),
        ChatMessageModel(
            id = "msg_v2",
            cycleId = "cycle_2026_01_vet",
            senderId = "owner_1",
            senderName = "مالك المزرعة",
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
            senderName = "مالك المزرعة",
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
            text = "السلام عليكم، توفرت اليوم دفعة جديدة من علف ناهي ممتاز (18% بروتين) بسعر 7,600 دج للقنطار مع التوصيل للمزرعة.",
            timestamp = "10:15 ص"
        ),
        ChatMessageModel(
            id = "msg_ft2",
            cycleId = "conv_feed_trader",
            senderId = "owner_1",
            senderName = "مالك المزرعة",
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
            text = "تم الحجز، الشاحنة تنطلق غداً الساعة 7 صباحاً بإذن الله ✓",
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
            defaultSnippet = "تم الحجز، الشاحنة تنطلق غداً الساعة 7 صباحاً بإذن الله ✓",
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
            defaultSnippet = "حاضر، تم تفقد خطوط المياه وضبط ارتفاع المعالف بالكامل ✓",
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

    val blockedUserIds = mutableStateListOf<String>()
    val reportedUgcItemIds = mutableStateListOf<String>()
    var accountDeletionConfirmationMessage by mutableStateOf<String?>(null)

    fun addMarketPost(post: MarketPostModel) {
        marketPosts.add(0, post)
    }

    fun blockUser(
        targetUid: String,
        context: android.content.Context? = boundAppContext,
        firestoreRepo: AgritechFirestoreRepository? = null
    ) {
        val cleanUid = targetUid.trim()
        if (cleanUid.isBlank()) return
        if (cleanUid !in blockedUserIds) {
            blockedUserIds.add(cleanUid)
        }
        chatMessages.removeAll { it.senderId == cleanUid || it.cycleId == cleanUid }
        chatConversations.removeAll { it.id == cleanUid || it.userName == cleanUid }
        marketPosts.removeAll {
            it.publisherId == cleanUid ||
                it.chatChannelId == cleanUid ||
                it.publisherName == cleanUid ||
                it.id == cleanUid
        }
        context?.let { LocalFarmPersistence.saveAll(it) }
    }

    fun reportUgcContent(
        itemId: String,
        targetUid: String = "",
        reason: String = "محتوى مخالف",
        context: android.content.Context? = boundAppContext,
        firestoreRepo: AgritechFirestoreRepository? = null
    ) {
        val cleanId = itemId.trim()
        if (cleanId.isBlank()) return
        if (cleanId !in reportedUgcItemIds) {
            reportedUgcItemIds.add(cleanId)
        }
        marketPosts.removeAll { it.id == cleanId }
        chatMessages.removeAll { it.id == cleanId }
        context?.let { LocalFarmPersistence.saveAll(it) }
    }

    fun visibleMarketPosts(): List<MarketPostModel> {
        return marketPosts.filterNot { post ->
            post.id in reportedUgcItemIds ||
                post.publisherId in blockedUserIds ||
                post.chatChannelId in blockedUserIds ||
                post.publisherName in blockedUserIds
        }
    }

    fun visibleChatConversations(): List<ChatConversationModel> {
        return chatConversations.filterNot { conv ->
            conv.id in reportedUgcItemIds ||
                conv.id in blockedUserIds ||
                conv.userName in blockedUserIds ||
                (conv.phoneNumber.isNotBlank() && conv.phoneNumber in blockedUserIds)
        }
    }

    fun streamChatMessages(cycleId: String): List<ChatMessageModel> {
        if (cycleId in blockedUserIds || cycleId in reportedUgcItemIds) return emptyList()
        val allowed = chatMessages.filter {
            it.id !in reportedUgcItemIds &&
                it.senderId !in blockedUserIds &&
                it.senderName !in blockedUserIds &&
                it.cycleId !in blockedUserIds
        }
        val exactMatches = allowed.filter { it.cycleId == cycleId }
        if (exactMatches.isNotEmpty()) return exactMatches
        return when {
            cycleId.endsWith("_worker") -> allowed.filter { it.cycleId == "cycle_2026_01_worker" }
            cycleId.endsWith("_vet") -> allowed.filter { it.cycleId == "cycle_2026_01_vet" }
            else -> allowed.filter { it.cycleId == cycleId || it.cycleId == "general_admin" }
        }
    }

    fun sendChatMessage(message: ChatMessageModel) {
        val existingIdx = chatMessages.indexOfFirst { it.id == message.id }
        if (existingIdx >= 0) {
            chatMessages[existingIdx] = message
        } else {
            chatMessages.add(message)
        }
    }

    fun updateChatMessageDeliveryState(messageId: String, newState: String) {
        val idx = chatMessages.indexOfFirst { it.id == messageId }
        if (idx >= 0) {
            chatMessages[idx] = chatMessages[idx].copy(deliveryState = newState)
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
        chatChannelId: String,
        displayCategory: String = tx.category
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
        val chatLabel = displayCategory.ifBlank { pendingTx.category }
        val chatMsg = ChatMessageModel(
            id = "msg_fin_req_${pendingTx.id}",
            cycleId = chatChannelId,
            senderId = senderId,
            senderName = senderName,
            senderRole = senderRole,
            text = "🧾 $chatLabel$cleanNote\n• المبلغ المطلوب اعتماده: $formattedPrice ${pendingTx.currency}",
            timestamp = nowTime,
            linkedTransactionId = pendingTx.id,
            requestCategory = chatLabel,
            requestAmount = pendingTx.amount,
            requestCurrency = pendingTx.currency,
            status = "PENDING"
        )
        sendChatMessage(chatMsg)
        persistLocalState()
        return chatMsg
    }

    fun mergeCloudCycleWithLocal(cloudCycle: CycleModel, localCycle: CycleModel): CycleModel {
        val effectiveFarmId = cloudCycle.farmId.ifBlank { localCycle.farmId }
        val effectiveCycleId = cloudCycle.id.ifBlank { localCycle.id }
        val localIsStrictlyNewer = localCycle.updatedAtMillis > 0L &&
            cloudCycle.updatedAtMillis > 0L &&
            localCycle.updatedAtMillis > cloudCycle.updatedAtMillis

        val allRecordedDaysForCycle = resolveReportsForFarmAndCycle(effectiveFarmId, effectiveCycleId)
        val reportsConsumedFeedKg = allRecordedDaysForCycle.sumOf { it.feedIntakeKg }
        val reportsMortality = allRecordedDaysForCycle.sumOf { it.mortalityCount }

        val totalPurchasedFeedKg = transactions.filter {
            (it.cycleId == effectiveCycleId || it.cycleId == localCycle.id) &&
                it.type == TransactionType.EXPENSE &&
                it.status.equals("APPROVED", ignoreCase = true) &&
                (it.quantityQuintals > 0.0 ||
                    it.category.contains("علف") ||
                    it.category.contains("Feed", ignoreCase = true) ||
                    it.category.contains("Aliment", ignoreCase = true) ||
                    it.category.contains("Pienso", ignoreCase = true) ||
                    it.category.contains("Yem", ignoreCase = true) ||
                    it.category.contains("Ração", ignoreCase = true))
        }.sumOf { parseFeedTransaction(it, AppLanguage.AR).estimatedKg }

        val effectiveConsumedFeedKg = if (allRecordedDaysForCycle.isNotEmpty()) {
            reportsConsumedFeedKg
        } else {
            maxOf(cloudCycle.cumulativeFeedConsumedKg, localCycle.cumulativeFeedConsumedKg)
        }

        val (accurateFeedInventoryKg, accurateFeedDeficitKg) = when {
            totalPurchasedFeedKg > 0.0 ->
                AgritechFirestoreRepository.applyFeedDeltaWithDeficit(
                    currentInventoryKg = totalPurchasedFeedKg,
                    currentDeficitKg = 0.0,
                    feedDeltaToDeduct = effectiveConsumedFeedKg
                )
            allRecordedDaysForCycle.isNotEmpty() -> {
                val bestBaseFeed = if (localIsStrictlyNewer) localCycle.feedInventoryKg else cloudCycle.feedInventoryKg
                val bestBaseDeficit = if (localIsStrictlyNewer) localCycle.feedDeficitKg else cloudCycle.feedDeficitKg
                Pair(bestBaseFeed.coerceAtLeast(0.0), bestBaseDeficit.coerceAtLeast(0.0))
            }
            localIsStrictlyNewer ->
                Pair(localCycle.feedInventoryKg.coerceAtLeast(0.0), localCycle.feedDeficitKg.coerceAtLeast(0.0))
            localCycle.feedInventoryKg > 0.0 && cloudCycle.feedInventoryKg > 0.0 ->
                Pair(minOf(localCycle.feedInventoryKg, cloudCycle.feedInventoryKg), maxOf(localCycle.feedDeficitKg, cloudCycle.feedDeficitKg))
            localCycle.feedInventoryKg > 0.0 ->
                Pair(localCycle.feedInventoryKg, localCycle.feedDeficitKg)
            else -> Pair(
                cloudCycle.feedInventoryKg.coerceAtLeast(0.0),
                cloudCycle.feedDeficitKg.coerceAtLeast(0.0)
            )
        }

        val effectiveMortality = if (allRecordedDaysForCycle.isNotEmpty()) {
            reportsMortality
        } else {
            maxOf(cloudCycle.cumulativeMortality, localCycle.cumulativeMortality)
        }

        val farmSalesTxs = transactions.filter {
            it.farmId == effectiveFarmId && (it.cycleId == effectiveCycleId || it.cycleId.isBlank())
        }
        val effectiveBirdsPlaced = maxOf(localCycle.totalBirdsPlaced, cloudCycle.totalBirdsPlaced)
        val effectiveBirdsSold = maxOf(
            PoultryCalculators.resolveCumulativeBirdsSold(localCycle, farmSalesTxs),
            cloudCycle.cumulativeBirdsSold
        )
        val effectiveSoldWeightKg = maxOf(
            PoultryCalculators.resolveCumulativeSoldWeightKg(localCycle, farmSalesTxs),
            cloudCycle.cumulativeSoldWeightKg
        )

        val base = if (localIsStrictlyNewer) localCycle else cloudCycle
        return base.copy(
            id = effectiveCycleId,
            farmId = effectiveFarmId,
            totalBirdsPlaced = effectiveBirdsPlaced,
            targetWeight = if (localCycle.targetWeight > 0.0) localCycle.targetWeight else cloudCycle.targetWeight,
            startDate = localCycle.startDate.ifBlank { cloudCycle.startDate },
            currencyCode = localCycle.currencyCode.ifBlank { cloudCycle.currencyCode },
            feedInventoryKg = accurateFeedInventoryKg,
            feedDeficitKg = accurateFeedDeficitKg,
            cumulativeFeedConsumedKg = effectiveConsumedFeedKg,
            cumulativeMortality = effectiveMortality,
            cumulativeBirdsSold = effectiveBirdsSold,
            cumulativeSoldWeightKg = effectiveSoldWeightKg,
            updatedAtMillis = maxOf(cloudCycle.updatedAtMillis, localCycle.updatedAtMillis)
        )
    }

    fun mergeCloudReportWithLocal(cloudReport: DailyReportModel, existingLocal: DailyReportModel): DailyReportModel {
        val localIsNewer = existingLocal.updatedAtMillis > 0L &&
            existingLocal.updatedAtMillis > cloudReport.updatedAtMillis
        return if (localIsNewer) {
            existingLocal.copy(
                actualAvgWeightKg = if (existingLocal.actualAvgWeightKg > 0.0) existingLocal.actualAvgWeightKg else cloudReport.actualAvgWeightKg,
                medicineGiven = existingLocal.medicineGiven || cloudReport.medicineGiven,
                medicineSummary = existingLocal.medicineSummary.ifBlank { cloudReport.medicineSummary },
                withdrawalDays = existingLocal.withdrawalDays
            )
        } else {
            cloudReport.copy(
                isEdited = cloudReport.isEdited || existingLocal.isEdited,
                previousSummary = cloudReport.previousSummary.ifBlank { existingLocal.previousSummary },
                actualAvgWeightKg = if (cloudReport.actualAvgWeightKg > 0.0) cloudReport.actualAvgWeightKg else existingLocal.actualAvgWeightKg,
                medicineGiven = cloudReport.medicineGiven,
                medicineSummary = cloudReport.medicineSummary.ifBlank { existingLocal.medicineSummary },
                withdrawalDays = cloudReport.withdrawalDays,
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
        val nowMillis = System.currentTimeMillis()
        var resolvedTxId = transactionId
        if (vetNoteId.isNotBlank()) {
            val vnIdx = vetNotes.indexOfFirst { it.id == vetNoteId }
            if (vnIdx >= 0) {
                val vn = vetNotes[vnIdx]
                if (resolvedTxId.isBlank() && vn.linkedTransactionId.isNotBlank()) {
                    resolvedTxId = vn.linkedTransactionId
                }
                vetNotes[vnIdx] = vn.copy(status = "APPROVED", updatedAtMillis = nowMillis)
            }
        }
        if (resolvedTxId.isNotBlank()) {
            val txIdx = transactions.indexOfFirst { it.id == resolvedTxId }
            if (txIdx >= 0) {
                val currentTx = transactions[txIdx]
                val updatedTx = currentTx.copy(
                    status = "APPROVED",
                    approvedBySignature = signatureTag,
                    updatedAtMillis = nowMillis
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
                            val (newInv, newDeficit) = AgritechFirestoreRepository.applyFeedDeltaWithDeficit(
                                currentInventoryKg = c.feedInventoryKg,
                                currentDeficitKg = c.feedDeficitKg,
                                feedDeltaToDeduct = -addedFeedKg
                            )
                            cycles[cycleIdx] = c.copy(
                                feedInventoryKg = newInv,
                                feedDeficitKg = newDeficit,
                                updatedAtMillis = nowMillis
                            )
                        }
                    }
                }
            }
            for (i in vetNotes.indices) {
                if (vetNotes[i].linkedTransactionId == resolvedTxId && vetNotes[i].status != "APPROVED") {
                    vetNotes[i] = vetNotes[i].copy(status = "APPROVED", updatedAtMillis = nowMillis)
                }
            }
            for (i in chatMessages.indices) {
                if (chatMessages[i].linkedTransactionId == resolvedTxId && chatMessages[i].status != "APPROVED") {
                    chatMessages[i] = chatMessages[i].copy(
                        status = "APPROVED",
                        requestStatus = "APPROVED"
                    )
                }
            }
        }
        persistLocalState()
    }

    /**
     * Atomically and idempotently records a sales invoice in local state (`transactions` + `cycles`).
     * 1. First checks if a transaction with `idempotencyKey` is already recorded; if so, returns `false`
     *    immediately without adding the invoice a second time and without deducting `cumulativeBirdsSold` again.
     * 2. Verifies `invoice.totalBirdsSold > 0 && invoice.totalBirdsSold <= availableBirds` before recording.
     */
    fun recordSalesInvoiceIdempotent(
        farmId: String,
        cycleId: String,
        idempotencyKey: String,
        dateText: String,
        invoice: SalesInvoiceModel
    ): Boolean {
        val cleanKey = idempotencyKey.trim()
        if (cleanKey.isEmpty()) return false
        if (transactions.any { it.id == cleanKey }) {
            return false
        }
        val cycleIdx = cycles.indexOfFirst { it.id == cycleId && it.farmId == farmId }
            .takeIf { it >= 0 }
            ?: cycles.indexOfFirst { it.id == cycleId }
        if (cycleIdx < 0) return false

        val c = cycles[cycleIdx]
        val reportsMortality = resolveReportsForFarmAndCycle(c.farmId, c.id).sumOf { it.mortalityCount }
        val effectiveMortality = maxOf(c.cumulativeMortality, reportsMortality)
        val availableBirds = (c.totalBirdsPlaced - effectiveMortality - c.cumulativeBirdsSold).coerceAtLeast(0)
        if (!(invoice.totalBirdsSold > 0 && invoice.totalBirdsSold <= availableBirds) ||
            invoice.totalAmount <= 0.0 ||
            invoice.totalNetKg <= 0.0
        ) {
            return false
        }

        val nowMillis = System.currentTimeMillis()
        cycles[cycleIdx] = c.copy(
            cumulativeBirdsSold = c.cumulativeBirdsSold + invoice.totalBirdsSold,
            cumulativeSoldWeightKg = c.cumulativeSoldWeightKg + invoice.totalNetKg,
            updatedAtMillis = nowMillis
        )
        transactions.add(
            TransactionModel(
                id = cleanKey,
                farmId = farmId,
                cycleId = cycleId,
                type = TransactionType.INCOME,
                category = "tx_chicken_sale",
                amount = invoice.totalAmount,
                currency = invoice.currency,
                date = dateText,
                invoiceData = invoice,
                updatedAtMillis = nowMillis
            )
        )
        persistLocalState()
        return true
    }

    /**
     * Removes a member from `farm.members` locally and clears `assignedWorkerId` if the removed member was the assigned worker.
     */
    fun removeMemberFromFarmLocally(farmId: String, memberUid: String) {
        val cleanFarmId = farmId.trim()
        val cleanUid = memberUid.trim()
        if (cleanFarmId.isBlank() || cleanUid.isBlank()) return
        val idx = farms.indexOfFirst { it.id == cleanFarmId }
        if (idx >= 0) {
            val farm = farms[idx]
            val updatedMembers = farm.members.toMutableMap().apply { remove(cleanUid) }
            val updatedAssignedWorker = if (farm.assignedWorkerId == cleanUid || updatedMembers[farm.assignedWorkerId] != "WORKER") {
                updatedMembers.entries.firstOrNull { it.value == "WORKER" }?.key ?: ""
            } else {
                farm.assignedWorkerId
            }
            farms[idx] = farm.copy(
                members = updatedMembers,
                assignedWorkerId = updatedAssignedWorker
            )
            persistLocalState()
        }
    }

    /**
     * Operation-specific delta rollback for a failed cloud sales invoice write:
     * Reverses ONLY the sold birds and sold weight delta of this invoice without overwriting concurrent feed or mortality updates.
     */
    fun rollbackSalesInvoiceDelta(
        cycleId: String,
        txId: String,
        soldBirdsDelta: Int,
        soldWeightDeltaKg: Double
    ) {
        transactions.removeAll { it.id == txId }
        val cIdx = cycles.indexOfFirst { it.id == cycleId }
        if (cIdx >= 0) {
            val cur = cycles[cIdx]
            cycles[cIdx] = cur.copy(
                cumulativeBirdsSold = (cur.cumulativeBirdsSold - soldBirdsDelta).coerceAtLeast(0),
                cumulativeSoldWeightKg = (cur.cumulativeSoldWeightKg - soldWeightDeltaKg).coerceAtLeast(0.0),
                updatedAtMillis = System.currentTimeMillis()
            )
        }
        persistLocalState()
    }

    /**
     * Operation-specific delta rollback for a failed cloud feed purchase write:
     * Reverses ONLY the added feed delta (respecting `feedDeficitKg`) without overwriting concurrent sales or mortality updates.
     */
    fun rollbackFeedPurchaseDelta(
        cycleId: String,
        txId: String,
        addedFeedKg: Double
    ) {
        transactions.removeAll { it.id == txId }
        countedFeedTxIds.remove(txId)
        if (addedFeedKg > 0.0) {
            val cIdx = cycles.indexOfFirst { it.id == cycleId }
            if (cIdx >= 0) {
                val cur = cycles[cIdx]
                val (restoredInv, restoredDeficit) = AgritechFirestoreRepository.applyFeedDeltaWithDeficit(
                    currentInventoryKg = cur.feedInventoryKg,
                    currentDeficitKg = cur.feedDeficitKg,
                    feedDeltaToDeduct = addedFeedKg
                )
                cycles[cIdx] = cur.copy(
                    feedInventoryKg = restoredInv,
                    feedDeficitKg = restoredDeficit,
                    updatedAtMillis = System.currentTimeMillis()
                )
            }
        }
        persistLocalState()
    }

    /**
     * Operation-specific delta rollback for a failed cloud daily report write:
     * Restores the previous report state (or removes the newly created report) and reverses ONLY its feed and mortality delta on the cycle.
     */
    fun rollbackDailyReportMutation(
        previousReport: DailyReportModel?,
        attemptedReport: DailyReportModel
    ) {
        val feedDeltaToReverse = attemptedReport.feedIntakeKg - (previousReport?.feedIntakeKg ?: 0.0)
        val mortalityDeltaToReverse = attemptedReport.mortalityCount - (previousReport?.mortalityCount ?: 0)
        if (previousReport != null) {
            val rIdx = dailyReports.indexOfFirst {
                it.id == attemptedReport.id || it.id == previousReport.id ||
                    (it.farmId == attemptedReport.farmId && it.cycleId == attemptedReport.cycleId && it.dayNumber == attemptedReport.dayNumber)
            }
            if (rIdx >= 0) {
                dailyReports[rIdx] = previousReport
            } else {
                dailyReports.add(previousReport)
                dailyReports.sortBy { it.dayNumber }
            }
        } else {
            dailyReports.removeAll {
                it.id == attemptedReport.id ||
                    (it.farmId == attemptedReport.farmId && it.cycleId == attemptedReport.cycleId && it.dayNumber == attemptedReport.dayNumber)
            }
        }

        val cIdx = cycles.indexOfFirst {
            it.id == attemptedReport.cycleId || (it.farmId == attemptedReport.farmId && it.status.equals("active", ignoreCase = true))
        }
        if (cIdx >= 0 && (kotlin.math.abs(feedDeltaToReverse) > 0.0001 || mortalityDeltaToReverse != 0)) {
            val cur = cycles[cIdx]
            val (restoredInv, restoredDeficit) = AgritechFirestoreRepository.applyFeedDeltaWithDeficit(
                currentInventoryKg = cur.feedInventoryKg,
                currentDeficitKg = cur.feedDeficitKg,
                feedDeltaToDeduct = -feedDeltaToReverse
            )
            cycles[cIdx] = cur.copy(
                feedInventoryKg = restoredInv,
                feedDeficitKg = restoredDeficit,
                cumulativeFeedConsumedKg = (cur.cumulativeFeedConsumedKg - feedDeltaToReverse).coerceAtLeast(0.0),
                cumulativeMortality = (cur.cumulativeMortality - mortalityDeltaToReverse).coerceAtLeast(0),
                updatedAtMillis = System.currentTimeMillis()
            )
        }
        persistLocalState()
    }

    /**
     * Resolves the active or latest cycle for a specific `farm`, NEVER falling back to another farm's cycle.
     */
    fun resolveCycleForFarm(farm: FarmModel): CycleModel {
        return cycles.firstOrNull {
            it.farmId == farm.id && it.status.equals("active", ignoreCase = true)
        } ?: cycles.firstOrNull {
            it.farmId == farm.id
        } ?: CycleModel(
            id = "cycle_default_${farm.id}",
            farmId = farm.id,
            totalBirdsPlaced = farm.capacity,
            targetWeight = 2.3,
            status = "active"
        )
    }

    /**
     * Resolves and deduplicates daily reports strictly for `(farmId, cycleId)` by `(farmId, cycleId, dayNumber)`
     * keeping the latest `updatedAtMillis`, and NEVER borrowing reports from `farm_1`.
     */
    fun resolveReportsForFarmAndCycle(
        farmId: String,
        cycleId: String? = null,
        extraReports: List<DailyReportModel> = emptyList()
    ): List<DailyReportModel> {
        val targetCycleId = cycleId?.trim().orEmpty()
        val combined = (dailyReports.asSequence() + extraReports.asSequence())
            .filter {
                it.farmId == farmId &&
                    (targetCycleId.isBlank() || it.cycleId == targetCycleId || it.cycleId.isBlank())
            }
            .toList()
        return combined
            .groupBy { rep ->
                val resolvedCycle = if (targetCycleId.isNotBlank()) targetCycleId else rep.cycleId
                Triple(rep.farmId, resolvedCycle, rep.dayNumber)
            }
            .map { (_, group) ->
                group.maxByOrNull { it.updatedAtMillis } ?: group.last()
            }
            .let { deduped ->
                if (targetCycleId.isNotBlank()) deduped.distinctBy { it.dayNumber } else deduped
            }
            .sortedBy { it.dayNumber }
    }

    /**
     * Non-destructive merge for incoming Firestore `dailyReports` snapshots:
     * NEVER deletes local days when cloud snapshots arrive with fewer items or delayed writes.
     * Matches exclusively by `(farmId, cycleId, dayNumber)` or canonical `id`, preserving all recorded days.
     */
    fun mergeCloudDailyReportsNonDestructive(
        farmId: String,
        cloudReports: List<DailyReportModel>
    ) {
        if (cloudReports.isEmpty()) return
        var changed = false
        cloudReports.forEach { cloudReport ->
            if (cloudReport.farmId != farmId) return@forEach
            val canonicalId = AgritechFirestoreRepository.buildCanonicalDailyReportId(
                farmId = cloudReport.farmId,
                cycleId = cloudReport.cycleId,
                dayNumber = cloudReport.dayNumber
            )
            val normalizedCloud = cloudReport.copy(id = canonicalId)
            val idx = dailyReports.indexOfFirst { local ->
                local.id == normalizedCloud.id ||
                    local.id == cloudReport.id ||
                    (local.farmId == normalizedCloud.farmId &&
                        (local.cycleId == normalizedCloud.cycleId || local.cycleId.isBlank() || normalizedCloud.cycleId.isBlank()) &&
                        local.dayNumber == normalizedCloud.dayNumber)
            }
            if (idx >= 0) {
                val existingLocal = dailyReports[idx]
                val merged = mergeCloudReportWithLocal(
                    cloudReport = normalizedCloud,
                    existingLocal = existingLocal.copy(id = canonicalId)
                )
                dailyReports[idx] = merged
            } else {
                dailyReports.add(normalizedCloud)
            }
            changed = true
        }
        if (changed) {
            dailyReports.sortBy { it.dayNumber }
            persistLocalState()
        }
    }

    private val initialBarnInventorySeed = mapOf(
        "farm_1_gas" to 14,
        "farm_1_shavings" to 45,
        "farm_1_water" to 6,
        "farm_1_meds" to 12,
        "farm_2_gas" to 18,
        "farm_2_shavings" to 60,
        "farm_2_water" to 8,
        "farm_2_meds" to 15
    )

    /**
     * Resets all in-memory state when switching between identities or logging out so no farms,
     * deletedFarmIds tombstones, cycles, reports, transactions, or worker ledger items leak across accounts.
     */
    fun resetForAccountSwitch() {
        farms.clear()
        cycles.clear()
        deletedFarmIds.clear()
        countedFeedTxIds.clear()
        chatMessages.clear()
        chatConversations.clear()
        dailyReports.clear()
        vetNotes.clear()
        transactions.clear()
        supervisedVetFarms.clear()
        workerPersonalEntries.clear()
        workerPersonalEntries.addAll(initialWorkerPersonalEntriesSeed)
        barnInventoryCounters.clear()
        barnInventoryCounters.putAll(initialBarnInventorySeed)
        blockedUserIds.clear()
        reportedUgcItemIds.clear()
    }

    /**
     * Resets user-authored in-memory state when the user explicitly clicks Logout.
     */
    fun clearOnExplicitLogout() {
        resetForAccountSwitch()
    }

    /**
     * Completely wipes all user data from memory and SharedPreferences when the user deletes their account permanently.
     */
    fun clearAllDataForDeletedAccount(
        uid: String? = null,
        context: android.content.Context? = boundAppContext
    ) {
        farms.clear()
        cycles.clear()
        deletedFarmIds.clear()
        countedFeedTxIds.clear()
        chatMessages.clear()
        chatConversations.clear()
        dailyReports.clear()
        vetNotes.clear()
        transactions.clear()
        supervisedVetFarms.clear()
        workerPersonalEntries.clear()
        barnInventoryCounters.clear()
        blockedUserIds.clear()
        reportedUgcItemIds.clear()
        accountDeletionConfirmationMessage = "تم حذف حسابك وجميع بياناتك نهائياً بنجاح"
        val targetCtx: android.content.Context? = context
        if (targetCtx != null) {
            LocalFarmPersistence.clearAllForDeletedIdentity(targetCtx, uid)
        }
    }

    /**
     * Inserts a new DailyReportModel or updates an existing report for the same day/id with full Audit Trail:
     * - If an existing report is modified, marks `isEdited = true` and records `previousSummary`.
     * - Adjusts `cycle.feedInventoryKg` by the feed delta and `cycle.cumulativeMortality` by the mortality delta.
     */
    fun upsertDailyReportWithAudit(rawReport: DailyReportModel): DailyReportModel {
        val report = PoultryCalculators.sanitizeDailyReport(rawReport)
        val resolvedCycleId = report.cycleId.trim().ifBlank {
            cycles.firstOrNull { it.farmId == report.farmId && it.status.equals("active", ignoreCase = true) }?.id
                ?: cycles.firstOrNull { it.farmId == report.farmId }?.id
                ?: "cycle_default_${report.farmId}"
        }
        val canonicalId = AgritechFirestoreRepository.buildCanonicalDailyReportId(
            farmId = report.farmId,
            cycleId = resolvedCycleId,
            dayNumber = report.dayNumber
        )
        val normalizedReport = report.copy(
            id = canonicalId,
            cycleId = resolvedCycleId
        )

        val existingIdx = dailyReports.indexOfFirst {
            it.id == canonicalId || (it.farmId == normalizedReport.farmId && it.cycleId == resolvedCycleId && it.dayNumber == normalizedReport.dayNumber)
        }
        val nowMillis = System.currentTimeMillis()
        val finalReport = if (existingIdx >= 0) {
            val old = dailyReports[existingIdx]
            val hasActualValueChange = normalizedReport.hasActualValueChangesFrom(old)

            if (!hasActualValueChange) {
                val unchanged = normalizedReport.copy(
                    id = canonicalId,
                    cycleId = resolvedCycleId,
                    isEdited = old.isEdited,
                    previousSummary = old.previousSummary,
                    withdrawalDays = maxOf(normalizedReport.withdrawalDays, old.withdrawalDays),
                    updatedAtMillis = old.updatedAtMillis
                )
                dailyReports[existingIdx] = unchanged
                unchanged
            } else {
                val prevSummary = normalizedReport.previousSummary.ifBlank {
                    old.previousSummary.ifBlank {
                        val tempStr = old.temperature?.let { "${String.format(java.util.Locale.US, "%.1f", it)}°" } ?: "—"
                        "العلف: ${Translations.formatQuintalNumber(old.feedIntakeKg / 100.0)} ق • الماء: ${old.waterIntakeLiters.toInt()} L • النافق: ${old.mortalityCount} • الحرارة: $tempStr"
                    }
                }
                val updated = normalizedReport.copy(
                    id = canonicalId,
                    cycleId = resolvedCycleId,
                    isEdited = true,
                    previousSummary = prevSummary,
                    withdrawalDays = maxOf(normalizedReport.withdrawalDays, old.withdrawalDays),
                    updatedAtMillis = nowMillis
                )
                dailyReports[existingIdx] = updated
                val feedDeltaKg = updated.feedIntakeKg - old.feedIntakeKg
                val cycleIdx = cycles.indexOfFirst { it.id == resolvedCycleId }
                    .takeIf { it >= 0 }
                    ?: cycles.indexOfFirst { it.farmId == updated.farmId && it.status.equals("active", true) }
                if (cycleIdx >= 0) {
                    val c = cycles[cycleIdx]
                    val allDaysForCycle = resolveReportsForFarmAndCycle(updated.farmId, resolvedCycleId)
                    val cumulativeConsumedKg = allDaysForCycle.sumOf { it.feedIntakeKg }
                    val cumulativeMortCount = allDaysForCycle.sumOf { it.mortalityCount }
                    val purchasedFeedKg = transactions.filter {
                        it.farmId == updated.farmId &&
                            (it.cycleId == resolvedCycleId || it.cycleId.isBlank()) &&
                            it.type == TransactionType.EXPENSE &&
                            it.status.equals("APPROVED", ignoreCase = true) &&
                            (it.quantityQuintals > 0.0 ||
                                it.category.contains("علف") ||
                                it.category.contains("Feed", ignoreCase = true) ||
                                it.category.contains("Aliment", ignoreCase = true) ||
                                it.category.contains("Pienso", ignoreCase = true) ||
                                it.category.contains("Yem", ignoreCase = true) ||
                                it.category.contains("Ração", ignoreCase = true))
                    }.sumOf { parseFeedTransaction(it, AppLanguage.AR).estimatedKg }
                    val (newInv, newDeficit) = if (purchasedFeedKg > 0.0) {
                        AgritechFirestoreRepository.applyFeedDeltaWithDeficit(
                            currentInventoryKg = purchasedFeedKg,
                            currentDeficitKg = 0.0,
                            feedDeltaToDeduct = cumulativeConsumedKg
                        )
                    } else {
                        AgritechFirestoreRepository.applyFeedDeltaWithDeficit(
                            currentInventoryKg = c.feedInventoryKg,
                            currentDeficitKg = c.feedDeficitKg,
                            feedDeltaToDeduct = feedDeltaKg
                        )
                    }
                    cycles[cycleIdx] = c.copy(
                        feedInventoryKg = newInv,
                        feedDeficitKg = newDeficit,
                        cumulativeFeedConsumedKg = cumulativeConsumedKg,
                        cumulativeMortality = cumulativeMortCount,
                        lastDailyReportId = canonicalId,
                        updatedAtMillis = nowMillis
                    )
                }
                updated
            }
        } else {
            val stamped = normalizedReport.copy(
                id = canonicalId,
                cycleId = resolvedCycleId,
                updatedAtMillis = nowMillis
            )
            dailyReports.add(stamped)
            dailyReports.sortBy { it.dayNumber }
            var cycleIdx = cycles.indexOfFirst { it.id == resolvedCycleId }
                .takeIf { it >= 0 }
                ?: cycles.indexOfFirst { it.farmId == stamped.farmId && it.status.equals("active", true) }
            if (cycleIdx < 0 && stamped.farmId.isNotBlank()) {
                val targetFarm = farms.firstOrNull { it.id == stamped.farmId }
                cycles.add(
                    CycleModel(
                        id = resolvedCycleId,
                        farmId = stamped.farmId,
                        totalBirdsPlaced = targetFarm?.capacity ?: 0,
                        targetWeight = 2.3,
                        status = "active",
                        startDate = java.time.LocalDate.now().toString()
                    )
                )
                cycleIdx = cycles.lastIndex
            }
            if (cycleIdx >= 0) {
                val c = cycles[cycleIdx]
                val allDaysForCycle = resolveReportsForFarmAndCycle(stamped.farmId, resolvedCycleId)
                val cumulativeConsumedKg = allDaysForCycle.sumOf { it.feedIntakeKg }
                val cumulativeMortCount = allDaysForCycle.sumOf { it.mortalityCount }
                val purchasedFeedKg = transactions.filter {
                    it.farmId == stamped.farmId &&
                        (it.cycleId == resolvedCycleId || it.cycleId.isBlank()) &&
                        it.type == TransactionType.EXPENSE &&
                        it.status.equals("APPROVED", ignoreCase = true) &&
                        (it.quantityQuintals > 0.0 ||
                            it.category.contains("علف") ||
                            it.category.contains("Feed", ignoreCase = true) ||
                            it.category.contains("Aliment", ignoreCase = true) ||
                            it.category.contains("Pienso", ignoreCase = true) ||
                            it.category.contains("Yem", ignoreCase = true) ||
                            it.category.contains("Ração", ignoreCase = true))
                }.sumOf { parseFeedTransaction(it, AppLanguage.AR).estimatedKg }
                val (newInv, newDeficit) = if (purchasedFeedKg > 0.0) {
                    AgritechFirestoreRepository.applyFeedDeltaWithDeficit(
                        currentInventoryKg = purchasedFeedKg,
                        currentDeficitKg = 0.0,
                        feedDeltaToDeduct = cumulativeConsumedKg
                    )
                } else {
                    AgritechFirestoreRepository.applyFeedDeltaWithDeficit(
                        currentInventoryKg = c.feedInventoryKg,
                        currentDeficitKg = c.feedDeficitKg,
                        feedDeltaToDeduct = stamped.feedIntakeKg
                    )
                }
                cycles[cycleIdx] = c.copy(
                    feedInventoryKg = newInv,
                    feedDeficitKg = newDeficit,
                    cumulativeFeedConsumedKg = cumulativeConsumedKg,
                    cumulativeMortality = cumulativeMortCount,
                    lastDailyReportId = canonicalId,
                    updatedAtMillis = nowMillis
                )
            }
            stamped
        }
        persistLocalState()
        return finalReport
    }

    /**
     * Immediately registers a newly created barn, its active cycle, and optional chick-purchase transaction
     * into local state (`AgritechMockDb` + `LocalFarmPersistence`) with ZERO dependency on network availability.
     */
    fun registerFarmAndCycleLocally(
        newFarm: FarmModel,
        newCycle: CycleModel,
        initialTx: TransactionModel? = null
    ) {
        deletedFarmIds.remove(newFarm.id)
        val existingFarmIdx = farms.indexOfFirst { it.id == newFarm.id }
        if (existingFarmIdx >= 0) {
            farms[existingFarmIdx] = newFarm
        } else {
            farms.add(newFarm)
        }
        for (i in cycles.indices) {
            val c = cycles[i]
            if (c.farmId == newFarm.id && c.id != newCycle.id && c.status.equals("active", ignoreCase = true)) {
                cycles[i] = c.copy(status = "completed")
            }
        }
        val existingCycleIdx = cycles.indexOfFirst { it.id == newCycle.id }
        if (existingCycleIdx >= 0) {
            cycles[existingCycleIdx] = newCycle
        } else {
            cycles.add(newCycle)
        }
        if (initialTx != null && initialTx.amount > 0.0 && transactions.none { it.id == initialTx.id }) {
            transactions.add(
                initialTx.copy(
                    status = initialTx.status.ifBlank { "APPROVED" },
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
        }
        persistLocalState()
    }

    /**
     * Offline-First financial transaction + feed inventory update:
     * Immediately appends the APPROVED transaction to `transactions`, updates `cycle.feedInventoryKg`,
     * and persists locally. NEVER rolls back on cloud unavailability.
     */
    fun recordExpenseOrIncomeOfflineFirst(
        tx: TransactionModel,
        addedFeedKg: Double = 0.0
    ): Boolean {
        val safeAmount = PoultryCalculators.sanitizeFiniteDouble(tx.amount, 0.0, 1_000_000_000.0)
        val safeQuintals = PoultryCalculators.sanitizeFiniteDouble(tx.quantityQuintals, 0.0, 500_000.0)
        val safeAddedFeedKg = PoultryCalculators.sanitizeFiniteDouble(addedFeedKg, 0.0, 50_000_000.0)
        if (safeAmount <= 0.0 || tx.id.isBlank()) return false
        if (transactions.any { it.id == tx.id }) return false

        val resolvedAddedFeedKg = when {
            safeAddedFeedKg > 0.0 -> safeAddedFeedKg
            safeQuintals > 0.0 -> safeQuintals * 100.0
            else -> 0.0
        }
        val nowMillis = System.currentTimeMillis()
        val normalizedTx = tx.copy(
            amount = safeAmount,
            status = tx.status.ifBlank { "APPROVED" },
            quantityQuintals = if (resolvedAddedFeedKg > 0.0 && safeQuintals <= 0.0) {
                resolvedAddedFeedKg / 100.0
            } else {
                safeQuintals
            },
            updatedAtMillis = nowMillis
        )

        if (resolvedAddedFeedKg > 0.0 && normalizedTx.type == TransactionType.EXPENSE && countedFeedTxIds.add(normalizedTx.id)) {
            var cycleIdx = cycles.indexOfFirst { it.id == normalizedTx.cycleId }
                .takeIf { it >= 0 }
                ?: cycles.indexOfFirst { it.farmId == normalizedTx.farmId && it.status.equals("active", ignoreCase = true) }
            if (cycleIdx < 0 && normalizedTx.farmId.isNotBlank()) {
                val targetFarm = farms.firstOrNull { it.id == normalizedTx.farmId }
                cycles.add(
                    CycleModel(
                        id = normalizedTx.cycleId.ifBlank { "cycle_default_${normalizedTx.farmId}" },
                        farmId = normalizedTx.farmId,
                        totalBirdsPlaced = targetFarm?.capacity ?: 0,
                        targetWeight = 2.3,
                        status = "active",
                        startDate = java.time.LocalDate.now().toString()
                    )
                )
                cycleIdx = cycles.lastIndex
            }
            if (cycleIdx >= 0) {
                val c = cycles[cycleIdx]
                val (newInv, newDeficit) = AgritechFirestoreRepository.applyFeedDeltaWithDeficit(
                    currentInventoryKg = c.feedInventoryKg,
                    currentDeficitKg = c.feedDeficitKg,
                    feedDeltaToDeduct = -resolvedAddedFeedKg
                )
                cycles[cycleIdx] = c.copy(
                    feedInventoryKg = newInv,
                    feedDeficitKg = newDeficit,
                    updatedAtMillis = nowMillis
                )
            }
        }

        transactions.add(normalizedTx)
        persistLocalState()
        return true
    }

    fun resetCycleAndClearBarn(farmId: String, cycleId: String): CycleModel? {
        if (farmId == "personal_worker_ledger") return null
        dailyReports.removeAll { it.farmId == farmId && it.farmId != "personal_worker_ledger" && (it.cycleId == cycleId || it.cycleId.isBlank()) }
        transactions.removeAll { it.farmId == farmId && it.farmId != "personal_worker_ledger" && (it.cycleId == cycleId || it.cycleId.isBlank()) }
        vetNotes.removeAll { it.farmId == farmId && it.farmId != "personal_worker_ledger" && (it.cycleId == cycleId || it.cycleId.isBlank()) }
        val todayIso = java.time.LocalDate.now().toString()
        val idx = cycles.indexOfFirst { it.id == cycleId || (it.farmId == farmId && it.status.equals("active", ignoreCase = true)) }
        val updatedCycle = if (idx >= 0) {
            val existing = cycles[idx]
            val reset = existing.copy(
                startDate = todayIso,
                feedInventoryKg = 0.0,
                feedDeficitKg = 0.0,
                cumulativeFeedConsumedKg = 0.0,
                lastDailyReportId = "",
                updatedAtMillis = System.currentTimeMillis(),
                cumulativeMortality = 0,
                cumulativeBirdsSold = 0,
                cumulativeSoldWeightKg = 0.0,
                status = "active"
            )
            cycles[idx] = reset
            reset
        } else {
            null
        }
        persistLocalState()
        return updatedCycle
    }

    val deletedFarmIds = mutableSetOf<String>()

    /**
     * Permanently deletes a barn (`farmId`) and all its associated cycles, daily reports, transactions, vet notes,
     * and supply inventory keys from local state without touching `personal_worker_ledger`.
     */
    fun deleteFarmCompletely(farmId: String) {
        if (farmId.isBlank() || farmId == "personal_worker_ledger") return
        deletedFarmIds.add(farmId)
        farms.removeAll { it.id == farmId }
        cycles.removeAll { it.farmId == farmId }
        dailyReports.removeAll { it.farmId == farmId && it.farmId != "personal_worker_ledger" }
        transactions.removeAll { it.farmId == farmId && it.farmId != "personal_worker_ledger" }
        vetNotes.removeAll { it.farmId == farmId && it.farmId != "personal_worker_ledger" }
        barnInventoryCounters.keys.removeAll { it.startsWith("${farmId}_") }
        persistLocalState()
    }

    // Interactive Barn Supply Counters for Tab 4 ("📦 المخزون"): Gas Cylinders, Wood Shavings, Water Tankers, Medicine Packs
    val barnInventoryCounters = androidx.compose.runtime.mutableStateMapOf<String, Int>().apply {
        putAll(initialBarnInventorySeed)
    }

    fun getBarnSupplyCount(farmId: String, supplyKey: String, defaultVal: Int = 0): Int {
        return barnInventoryCounters["${farmId}_$supplyKey"] ?: defaultVal
    }

    fun adjustBarnSupplyCount(farmId: String, supplyKey: String, delta: Int, defaultVal: Int = 0) {
        val key = "${farmId}_$supplyKey"
        val current = barnInventoryCounters[key] ?: defaultVal
        barnInventoryCounters[key] = (current + delta).coerceAtLeast(0)
        persistLocalState()
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
        val savedUid = AgritechAuthHelper.currentUserUid() ?: SaasPreferencesManager.getSavedUserUid(context)
        LocalFarmPersistence.setActiveIdentity(AgritechAuthHelper.resolveActiveStorageIdentityKey(savedUid))
        LocalFarmPersistence.loadIntoMockDb(context)
        val onboardingDone = SaasPreferencesManager.hasCompletedOnboarding(context)
        val savedLang = SaasPreferencesManager.getSavedLanguage(context)
        val savedCountry = SaasPreferencesManager.getSavedCountryCode(context)
        val savedCurrency = SaasPreferencesManager.getSavedCurrencyCode(context)
        val savedTheme = SaasPreferencesManager.getSavedTheme(context)
        val savedAccentColor = SaasPreferencesManager.getSavedAccentColor(context)
        val savedTextScale = SaasPreferencesManager.getSavedTextScale(context)
        val savedCornerStyle = SaasPreferencesManager.getSavedCornerStyle(context)
        val savedPaperStyle = SaasPreferencesManager.getSavedPaperStyle(context)
        val savedTabBarMotion = SaasPreferencesManager.getSavedTabBarMotion(context)
        val savedIconStyle = SaasPreferencesManager.getSavedIconStyle(context)
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
            initialPaperStyle = savedPaperStyle,
            initialTabBarMotion = savedTabBarMotion,
            initialIconStyle = savedIconStyle,
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
        val activeTheme = sessionState.currentTheme
        PoultryTheme(
            isDark = activeTheme.isDark,
            accentColor = sessionState.currentAccentColor,
            cornerStyle = sessionState.currentCornerStyle,
            themeBgColor = activeTheme.bgColor,
            themeCardColor = activeTheme.cardColor,
            themeHeadingColor = activeTheme.headingColor,
            themeSecondaryTextColor = activeTheme.secondaryTextColor
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
                if (!sessionState.isLoggedIn || sessionState.currentUserUid != uid) {
                    sessionState.markAuthenticated(
                        context = context,
                        role = sessionState.currentRole ?: AppRole.OWNER,
                        linkedBarns = sessionState.hasLinkedBarns,
                        farmId = activeFarmId,
                        uid = uid
                    )
                }
                coroutineScope {
                    launch {
                        try {
                            firestoreRepo.observeUserRole(uid).collectLatest { cloudRole ->
                                // Update role without resetting already-loaded local workspace state
                                if (cloudRole != null) {
                                    if (sessionState.isLoggedIn && sessionState.currentUserUid == uid) {
                                        sessionState.syncCloudRoleSafely(context, cloudRole)
                                    } else {
                                        sessionState.markAuthenticated(
                                            context = context,
                                            role = cloudRole,
                                            linkedBarns = sessionState.hasLinkedBarns,
                                            farmId = activeFarmId,
                                            uid = uid
                                        )
                                    }
                                }
                            }
                        } catch (_: Exception) {}
                    }
                    launch {
                        try {
                            firestoreRepo.observeAllUserFarms(uid, activeFarmId).collectLatest { cloudFarms ->
                                val validCloudFarms = cloudFarms.filterNot {
                                    it.id in AgritechMockDb.deletedFarmIds || AgritechFirestoreRepository.isUnassignedFarm(it.id)
                                }
                                if (validCloudFarms.isNotEmpty()) {
                                    validCloudFarms.forEach { farm ->
                                        val idx = AgritechMockDb.farms.indexOfFirst { it.id == farm.id }
                                        if (idx >= 0) {
                                            val existing = AgritechMockDb.farms[idx]
                                            AgritechMockDb.farms[idx] = farm.copy(
                                                capacity = maxOf(farm.capacity, existing.capacity),
                                                customMetrics = existing.customMetrics + farm.customMetrics
                                            )
                                        } else {
                                            AgritechMockDb.farms.add(farm)
                                        }
                                    }
                                    if (sessionState.activeFarmId.isBlank() ||
                                        AgritechFirestoreRepository.isUnassignedFarm(sessionState.activeFarmId)
                                    ) {
                                        sessionState.activeFarmId = validCloudFarms.first().id
                                    }
                                    AgritechMockDb.persistLocalState()
                                }
                            }
                        } catch (_: Exception) {}
                    }
                    if (activeFarmId.isNotBlank()) {
                        launch {
                            try {
                                firestoreRepo.observeFarms(activeFarmId).collectLatest { cloudFarms ->
                                    val validCloudFarms = cloudFarms.filterNot { it.id in AgritechMockDb.deletedFarmIds }
                                    if (validCloudFarms.isNotEmpty()) {
                                        validCloudFarms.forEach { farm ->
                                            val idx = AgritechMockDb.farms.indexOfFirst { it.id == farm.id }
                                            if (idx >= 0) {
                                                val existing = AgritechMockDb.farms[idx]
                                                AgritechMockDb.farms[idx] = farm.copy(
                                                    capacity = maxOf(farm.capacity, existing.capacity),
                                                    customMetrics = existing.customMetrics + farm.customMetrics
                                                )
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
                                    AgritechMockDb.mergeCloudDailyReportsNonDestructive(
                                        farmId = activeFarmId,
                                        cloudReports = cloudReports
                                    )
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
                                                val localIsStrictlyNewer = existingLocal.updatedAtMillis > 0L &&
                                                    existingLocal.updatedAtMillis > note.updatedAtMillis
                                                AgritechMockDb.vetNotes[idx] = note.copy(
                                                    status = if (localIsStrictlyNewer) existingLocal.status else note.status,
                                                    withdrawalDays = if (localIsStrictlyNewer) existingLocal.withdrawalDays else note.withdrawalDays,
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
                                                val localIsStrictlyNewer = existingLocal.updatedAtMillis > 0L &&
                                                    existingLocal.updatedAtMillis > tx.updatedAtMillis
                                                AgritechMockDb.transactions[idx] = tx.copy(
                                                    status = if (localIsStrictlyNewer) existingLocal.status else tx.status,
                                                    invoiceData = tx.invoiceData ?: existingLocal.invoiceData,
                                                    approvedBySignature = tx.approvedBySignature.ifBlank { existingLocal.approvedBySignature },
                                                    updatedAtMillis = maxOf(tx.updatedAtMillis, existingLocal.updatedAtMillis)
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

    val selectedOwnerFarm = sessionState.selectedFarmForOwner
    val selectedVetFarm = sessionState.selectedFarmForVet
    val selectedWorkerFarm = sessionState.selectedFarmForWorker
    when {
        !sessionState.isLoggedIn || sessionState.currentRole == null -> {
            AgritechLoginScreen(
                currentTheme = currentTheme,
                lang = currentLanguage,
                authErrorMessage = authErrorBanner ?: AgritechMockDb.accountDeletionConfirmationMessage,
                onLogin = { role, linkedBarns ->
                    authErrorBanner = null
                    AgritechMockDb.accountDeletionConfirmationMessage = null
                    val cm = credentialManager
                    val isAlreadySignedIn = AgritechAuthHelper.isSignedIn()
                    if (isAlreadySignedIn) {
                        LocalFarmPersistence.restoreFromReloginArchiveIfNeeded(context)
                        LocalFarmPersistence.loadIntoMockDb(context)
                        // Do not automatically assign the first local farm to a user who doesn't own or belong to it:
                        // Only retain activeFarmId if it was already selected and valid for this role; otherwise start unassigned ("")
                        val currentActive = sessionState.activeFarmId.takeIf {
                            it.isNotBlank() && it !in AgritechMockDb.deletedFarmIds &&
                                (role != AppRole.OWNER || AgritechMockDb.farms.any { f -> f.id == it })
                        } ?: if (role == AppRole.OWNER) {
                            AgritechMockDb.farms.firstOrNull { it.id !in AgritechMockDb.deletedFarmIds }?.id.orEmpty()
                        } else {
                            ""
                        }
                        sessionState.markAuthenticated(
                            context = context,
                            role = role,
                            linkedBarns = linkedBarns,
                            farmId = currentActive,
                            uid = AgritechAuthHelper.currentUserUid()
                        )
                        if (role == AppRole.WORKER && linkedBarns) {
                            sessionState.selectedFarmForWorker = AgritechMockDb.farms.firstOrNull { it.id == currentActive && it.id !in AgritechMockDb.deletedFarmIds }
                        }
                        scope.launch {
                            val res = firestoreRepo?.setUserRoleAndFarm(role, currentActive)
                            if (res != null && res.isFailure) {
                                authErrorBanner = "تنبيه السحابة: تعذر حفظ الملف الشخصي (${res.exceptionOrNull()?.localizedMessage ?: "خطأ غير معروف"})"
                            }
                            if (role == AppRole.OWNER && currentActive.isNotBlank() && currentActive !in AgritechMockDb.deletedFarmIds) {
                                AgritechMockDb.farms.firstOrNull { it.id == currentActive }?.let { farm ->
                                    firestoreRepo?.saveFarmRecord(farm, AppRole.OWNER)
                                }
                            }
                        }
                    } else if (cm != null) {
                        AgritechAuthHelper.onGoogleSignInClicked(
                            context = context,
                            credentialManager = cm,
                            onAuthSuccess = {
                                authErrorBanner = null
                                LocalFarmPersistence.restoreFromReloginArchiveIfNeeded(context)
                                LocalFarmPersistence.loadIntoMockDb(context)
                                val currentActive = sessionState.activeFarmId.takeIf {
                                    it.isNotBlank() && it !in AgritechMockDb.deletedFarmIds &&
                                        (role != AppRole.OWNER || AgritechMockDb.farms.any { f -> f.id == it })
                                } ?: if (role == AppRole.OWNER) {
                                    AgritechMockDb.farms.firstOrNull { it.id !in AgritechMockDb.deletedFarmIds }?.id.orEmpty()
                                } else {
                                    ""
                                }
                                sessionState.markAuthenticated(
                                    context = context,
                                    role = role,
                                    linkedBarns = linkedBarns,
                                    farmId = currentActive,
                                    uid = AgritechAuthHelper.currentUserUid()
                                )
                                if (role == AppRole.WORKER && linkedBarns) {
                                    sessionState.selectedFarmForWorker = AgritechMockDb.farms.firstOrNull { it.id == currentActive && it.id !in AgritechMockDb.deletedFarmIds }
                                }
                                scope.launch {
                                    val res = firestoreRepo?.setUserRoleAndFarm(role, currentActive)
                                    if (res != null && res.isFailure) {
                                        authErrorBanner = "تنبيه السحابة: تعذر حفظ الملف الشخصي (${res.exceptionOrNull()?.localizedMessage ?: "خطأ غير معروف"})"
                                    }
                                    if (role == AppRole.OWNER && currentActive.isNotBlank() && currentActive !in AgritechMockDb.deletedFarmIds) {
                                        AgritechMockDb.farms.firstOrNull { it.id == currentActive }?.let { farm ->
                                            firestoreRepo?.saveFarmRecord(farm, AppRole.OWNER)
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
        selectedOwnerFarm != null -> {
            BackHandler { sessionState.selectedFarmForOwner = null }
            val farm = selectedOwnerFarm
            LaunchedEffect(farm.id) {
                if (farm.id.isNotBlank() && sessionState.activeFarmId != farm.id) {
                    sessionState.activeFarmId = farm.id
                }
            }
            val cycle = AgritechMockDb.resolveCycleForFarm(farm)
            AgritechFarmNotebookScreen(
                farm = farm,
                cycle = cycle,
                currentTheme = currentTheme,
                lang = currentLanguage,
                firestoreRepo = firestoreRepo,
                reports = AgritechMockDb.resolveReportsForFarmAndCycle(farm.id, cycle.id),
                vetNotes = AgritechMockDb.vetNotes.filter { it.farmId == farm.id },
                transactions = AgritechMockDb.transactions.filter { it.farmId == farm.id && it.farmId != "personal_worker_ledger" },
                onAddTransaction = { tx, addedFeedKg ->
                    val resolvedAddedFeedKg = when {
                        addedFeedKg > 0.0 -> addedFeedKg
                        tx.quantityQuintals > 0.0 -> tx.quantityQuintals * 100.0
                        else -> 0.0
                    }
                    val normalizedTx = if (resolvedAddedFeedKg > 0.0 && tx.quantityQuintals <= 0.0) {
                        tx.copy(quantityQuintals = resolvedAddedFeedKg / 100.0, status = tx.status.ifBlank { "APPROVED" })
                    } else {
                        tx.copy(status = tx.status.ifBlank { "APPROVED" })
                    }
                    val invoice = normalizedTx.invoiceData
                    if (invoice != null && normalizedTx.type == TransactionType.INCOME) {
                        val recordedLocally = AgritechMockDb.recordSalesInvoiceIdempotent(
                            farmId = normalizedTx.farmId,
                            cycleId = normalizedTx.cycleId,
                            idempotencyKey = normalizedTx.id,
                            dateText = normalizedTx.date,
                            invoice = invoice
                        )
                        if (!recordedLocally && AgritechMockDb.transactions.none { it.id == normalizedTx.id }) {
                            return@AgritechFarmNotebookScreen
                        }
                        LocalFarmPersistence.saveAll(context)
                        scope.launch {
                            val cloudRes = firestoreRepo?.saveSalesInvoiceAndMigrateIdempotent(
                                farmId = normalizedTx.farmId,
                                cycleId = normalizedTx.cycleId,
                                idempotencyKey = normalizedTx.id,
                                dateText = normalizedTx.date,
                                invoice = invoice
                            )
                            if (cloudRes != null && cloudRes.isFailure) {
                                android.util.Log.w("OfflineFirstSync", "Sales invoice saved locally; cloud sync deferred: ${cloudRes.exceptionOrNull()?.message}")
                            }
                        }
                    } else if (AgritechMockDb.recordExpenseOrIncomeOfflineFirst(normalizedTx, resolvedAddedFeedKg)) {
                        LocalFarmPersistence.saveAll(context)
                        scope.launch {
                            val cloudRes = if (resolvedAddedFeedKg > 0.0 && normalizedTx.type == TransactionType.EXPENSE) {
                                firestoreRepo?.recordFeedPurchaseAndExpenseIdempotent(
                                    idempotencyKey = normalizedTx.id,
                                    tx = normalizedTx,
                                    addedFeedKg = resolvedAddedFeedKg
                                )
                            } else {
                                firestoreRepo?.recordTransactionIdempotent(
                                    idempotencyKey = normalizedTx.id,
                                    tx = normalizedTx
                                )
                            }
                            if (cloudRes != null && cloudRes.isFailure) {
                                android.util.Log.w("OfflineFirstSync", "Transaction saved locally; cloud sync deferred: ${cloudRes.exceptionOrNull()?.message}")
                            }
                        }
                    }
                },
                onSaveOrUpdateDailyReport = { report ->
                    val savedReport = AgritechMockDb.upsertDailyReportWithAudit(report)
                    LocalFarmPersistence.saveAll(context)
                    scope.launch {
                        val cloudRes = firestoreRepo?.saveDailyReport(savedReport)
                        if (cloudRes != null && cloudRes.isFailure) {
                            android.util.Log.w("OfflineFirstSync", "Daily report saved locally; cloud sync deferred: ${cloudRes.exceptionOrNull()?.message}")
                        }
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
        selectedVetFarm != null -> {
            BackHandler { sessionState.selectedFarmForVet = null }
            val farm = selectedVetFarm
            LaunchedEffect(farm.id) {
                if (farm.id.isNotBlank() && sessionState.activeFarmId != farm.id) {
                    sessionState.activeFarmId = farm.id
                }
            }
            val cycle = AgritechMockDb.resolveCycleForFarm(farm)
            val allSupervisedFarms = (AgritechMockDb.farms + AgritechMockDb.supervisedVetFarms).distinctBy { it.id }
            val farmReports = AgritechMockDb.resolveReportsForFarmAndCycle(farm.id, cycle.id)
            AgritechVetNotebookScreen(
                farm = farm,
                cycle = cycle,
                currentTheme = currentTheme,
                lang = currentLanguage,
                firestoreRepo = firestoreRepo,
                reports = farmReports,
                vetNotes = AgritechMockDb.vetNotes.filter { it.farmId == farm.id },
                farms = allSupervisedFarms,
                onSwitchFarm = {
                    sessionState.activeFarmId = it.id
                    sessionState.selectedFarmForVet = it
                },
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
        selectedWorkerFarm != null -> {
            BackHandler { sessionState.selectedFarmForWorker = null }
            val farm = selectedWorkerFarm
            LaunchedEffect(farm.id) {
                if (farm.id.isNotBlank() && sessionState.activeFarmId != farm.id) {
                    sessionState.activeFarmId = farm.id
                }
            }
            val cycle = AgritechMockDb.resolveCycleForFarm(farm)
            AgritechWorkerDashboard(
                farm = farm,
                cycle = cycle,
                currentTheme = currentTheme,
                lang = currentLanguage,
                reports = AgritechMockDb.resolveReportsForFarmAndCycle(farm.id, cycle.id),
                urgentNotes = AgritechMockDb.vetNotes.filter { it.farmId == farm.id && it.isUrgent },
                farms = AgritechMockDb.farms,
                onSwitchFarm = {
                    sessionState.activeFarmId = it.id
                    sessionState.selectedFarmForWorker = it
                },
                onSubmitReport = { report ->
                    val savedReport = AgritechMockDb.upsertDailyReportWithAudit(report)
                    LocalFarmPersistence.saveAll(context)
                    scope.launch {
                        val cloudRes = firestoreRepo?.saveDailyReport(savedReport)
                        if (cloudRes != null && cloudRes.isFailure) {
                            android.util.Log.w("OfflineFirstSync", "Worker report saved locally; cloud sync deferred: ${cloudRes.exceptionOrNull()?.message}")
                        }
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
                    if (sessionState.activeFarmId.isNotBlank()) {
                        scope.launch {
                            firestoreRepo?.setUserRoleAndFarm(newRole, sessionState.activeFarmId)
                        }
                    }
                },
                onOpenNotebook = { farm ->
                    sessionState.activeFarmId = farm.id
                    val currentRole = sessionState.currentRole ?: AppRole.OWNER
                    when {
                        currentRole.isVetRole -> sessionState.selectedFarmForVet = farm
                        currentRole == AppRole.WORKER -> sessionState.selectedFarmForWorker = farm
                        else -> sessionState.selectedFarmForOwner = farm
                    }
                },
                onAddFarmAndCycle = { newFarm, newCycle, initialTx ->
                    AgritechMockDb.registerFarmAndCycleLocally(newFarm, newCycle, initialTx)
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
                        if (initialTx.amount > 0.0) {
                            firestoreRepo?.recordTransactionIdempotent(initialTx.id, initialTx)
                        }
                    }
                },
                onSubmitWorkerReport = { report ->
                    val savedReport = AgritechMockDb.upsertDailyReportWithAudit(report)
                    LocalFarmPersistence.saveAll(context)
                    scope.launch {
                        val cloudRes = firestoreRepo?.saveDailyReport(savedReport)
                        if (cloudRes != null && cloudRes.isFailure) {
                            android.util.Log.w("OfflineFirstSync", "Universal worker report saved locally; cloud sync deferred: ${cloudRes.exceptionOrNull()?.message}")
                        }
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
    val isDark = currentTheme.isDark
    val activeAccent = appearance.accentColor
    val pearlLoginBg = currentTheme.bgColor
    val loginCardShape = RoundedCornerShape(appearance.cornerRadiusDp.dp)
    val squircleShape = RoundedCornerShape(appearance.cornerRadiusDp.dp)

    var showQrScannerSheet by remember { mutableStateOf(false) }
    var showPrivacyPolicyModal by remember { mutableStateOf(false) }
    var isLoginMorphing by remember { mutableStateOf(false) }
    var cardEntered by remember { mutableStateOf(false) }
    val loginScope = rememberCoroutineScope()

    if (showPrivacyPolicyModal) {
        PrivacyPolicyAndTermsModal(
            lang = lang,
            currentTheme = currentTheme,
            onDismiss = { showPrivacyPolicyModal = false }
        )
    }

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
            .darwinPaperBackground(currentTheme, appearance.paperStyle)
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
            // 3D Neumorphic Squircle Badge for App Logo
            val squircleBorderBrush = if (isDark) {
                Brush.linearGradient(listOf(Color.White.copy(alpha = 0.35f), currentTheme.cardColor))
            } else {
                Brush.linearGradient(listOf(currentTheme.cardColor, Color(0xFFCBD5E1)))
            }
            val squircleSurfaceBrush = if (isDark) {
                Brush.linearGradient(listOf(currentTheme.cardColor, currentTheme.bgColor))
            } else {
                Brush.linearGradient(listOf(currentTheme.cardColor, currentTheme.bgColor))
            }

            Box(
                modifier = Modifier
                    .size(78.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = squircleShape,
                        spotColor = activeAccent.copy(alpha = 0.45f)
                    )
                    .clip(squircleShape)
                    .background(brush = squircleSurfaceBrush, shape = squircleShape)
                    .border(width = 1.5.dp, brush = squircleBorderBrush, shape = squircleShape)
                    .darwinManuscriptBorder(cornerRadius = appearance.cornerRadiusDp.dp, isDark = isDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Agriculture,
                    contentDescription = null,
                    tint = activeAccent,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                Translations.tr("app_title", lang),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = currentTheme.headingColor
            )

            Text(
                Translations.tr("app_subtitle", lang),
                fontSize = 13.sp,
                color = currentTheme.secondaryTextColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 12.dp,
                        shape = loginCardShape,
                        spotColor = Color(0xFF64748B).copy(alpha = 0.35f)
                    )
                    .darwinManuscriptBorder(cornerRadius = appearance.cornerRadiusDp.dp, isDark = isDark)
                    .testTag("login_spring_card"),
                shape = loginCardShape,
                colors = CardDefaults.cardColors(
                    containerColor = currentTheme.cardColor
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        Translations.tr("login_title_owner", lang),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = currentTheme.headingColor,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        Translations.tr("app_subtitle", lang),
                        fontSize = 13.sp,
                        color = currentTheme.secondaryTextColor,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 3D Luminous Capsule Login Button (Linear Gradient #43C6FF -> #6B46FF)
                    val loginCapsuleShape = if (isLoginMorphing) CircleShape else RoundedCornerShape(50)
                    val loginGradientBrush = Brush.linearGradient(
                        colors = listOf(Color(0xFF43C6FF), Color(0xFF6B46FF))
                    )
                    val loginTopHighlight = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.5f), Color.Transparent)
                    )

                    Box(
                        modifier = Modifier
                            .then(
                                if (isLoginMorphing) Modifier.width(loginBtnWidth)
                                else Modifier.fillMaxWidth()
                            )
                            .height(52.dp)
                            .shadow(
                                elevation = 9.dp,
                                shape = loginCapsuleShape,
                                spotColor = Color(0xFF6B46FF),
                                ambientColor = Color(0xFF43C6FF)
                            )
                            .clip(loginCapsuleShape)
                            .background(brush = loginGradientBrush, shape = loginCapsuleShape)
                            .border(width = 1.dp, brush = loginTopHighlight, shape = loginCapsuleShape)
                            .clickable {
                                if (!isLoginMorphing) {
                                    isLoginMorphing = true
                                    onLogin(AppRole.OWNER, true)
                                    loginScope.launch {
                                        delay(350)
                                        isLoginMorphing = false
                                    }
                                }
                            }
                            .testTag("btn_owner_login_morph"),
                        contentAlignment = Alignment.Center
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
                                    "${Translations.tr("login_btn", lang)} ➔",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }

                    if (authErrorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
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

                    // Soft Raised Neumorphic Capsule for QR Scan Join ("📷 مسح رمز الانضمام")
                    val qrCapsuleShape = RoundedCornerShape(50)
                    val qrRaisedBrush = if (isDark) {
                        Brush.verticalGradient(listOf(Color(0xFF26354D), Color(0xFF162032)))
                    } else {
                        Brush.verticalGradient(listOf(Color.White, Color(0xFFE6EEF8)))
                    }
                    val qrBevelBorder = if (isDark) {
                        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.25f), Color(0xFF0F172A)))
                    } else {
                        Brush.linearGradient(listOf(Color.White, Color(0xFFCBD5E1)))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .shadow(
                                elevation = 5.dp,
                                shape = qrCapsuleShape,
                                spotColor = Color(0xFF64748B)
                            )
                            .clip(qrCapsuleShape)
                            .background(brush = qrRaisedBrush, shape = qrCapsuleShape)
                            .border(width = 1.2.dp, brush = qrBevelBorder, shape = qrCapsuleShape)
                            .clickable { showQrScannerSheet = true }
                            .testTag("btn_scan_qr_join"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
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
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = if (isDark) Color.White else activeAccent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    TextButton(
                        onClick = { showPrivacyPolicyModal = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_login_privacy_policy")
                    ) {
                        Text(
                            "🔒 سياسة الخصوصية وشروط الاستخدام",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeAccent,
                            textAlign = TextAlign.Center
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
    val primaryTextColor = currentTheme.headingColor
    val secondaryTextColor = currentTheme.secondaryTextColor
    val accentEmerald = if (isDark) activeAccentOption.darkModeColor else activeAccentOption.primaryColor
    val accentBadgeBg = if (isDark) activeAccentOption.darkContainerColor else activeAccentOption.softContainerColor
    val primaryButtonAccent = activeAccentOption.primaryColor

    // Management Hub is always accessible on the first tab for all 7 professions
    val showBarnsTab = true
    var selectedGlobalTab by remember {
        mutableStateOf(GlobalDashboardTab.BARNS)
    }
    var isWorkerNotebookOpen by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(activeRole) {
        if (activeRole.normalizedHubRole != AppRole.WORKER) {
            isWorkerNotebookOpen = false
        }
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
        val qrJoinScope = rememberCoroutineScope()
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
            },
            onJoinInviteData = { inviteData ->
                if (firestoreRepo != null && inviteData.inviteId.isNotBlank()) {
                    qrJoinScope.launch {
                        firestoreRepo.acceptFarmInvite(
                            farmId = inviteData.farmId,
                            inviteId = inviteData.inviteId,
                            expectedRole = inviteData.role
                        )
                    }
                }
            }
        )
    }

    targetFarmForVetInvoice?.let { invFarm ->
        val invCycle = AgritechMockDb.resolveCycleForFarm(invFarm)
        val invReports = AgritechMockDb.resolveReportsForFarmAndCycle(invFarm.id, invCycle.id)
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
        QuickAiVetBottomSheet(
            selectedFarm = farms.firstOrNull { it.id == sessionState.activeFarmId } ?: farms.firstOrNull(),
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

    if (isWorkerNotebookOpen && activeRole.normalizedHubRole == AppRole.WORKER) {
        BackHandler { isWorkerNotebookOpen = false }
        WorkerSmartNotebookScreen(
            farms = farms,
            currentTheme = currentTheme,
            lang = lang,
            onOpenDailyReport = onOpenNotebook,
            onOpenSettings = onOpenSettings,
            onLogout = onLogout,
            onBack = { isWorkerNotebookOpen = false }
        )
        return
    }

    val appearance = LocalAppAppearanceState.current
    val isFrostedGlassMode = appearance.paperStyle == AppPaperStyleOption.FROSTED_GLASS
    val topBarGlassContainerColor = if (isFrostedGlassMode) currentTheme.cardColor else currentTheme.bgColor

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .darwinPaperBackground(currentTheme),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .statusBarsPadding()
                    .then(
                        if (isFrostedGlassMode) {
                            Modifier.darwinManuscriptBorder(cornerRadius = 0.dp, isDark = currentTheme.isDark)
                        } else {
                            Modifier
                        }
                    ),
                title = {
                    Column {
                        Text(
                            Translations.tr("farms_title", lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                            color = primaryTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            when (activeRole.normalizedHubRole) {
                                AppRole.VET_CLINIC -> Translations.tr("role_badge_vet_clinic", lang)
                                AppRole.WORKER -> Translations.tr("role_badge_worker", lang)
                                AppRole.HOME_PERSONAL -> Translations.tr("role_badge_home_personal", lang)
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
                        onClick = {
                            showGeneralChatSheet = true
                        },
                        modifier = Modifier.testTag("btn_general_admin_chat")
                    ) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = Translations.tr("quick_ai_vet_sheet_title", lang),
                            tint = primaryTextColor
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = primaryTextColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = topBarGlassContainerColor)
            )
        },
        floatingActionButton = {
            when (selectedGlobalTab) {
                GlobalDashboardTab.BARNS -> {
                    if (activeRole.isOwnerRole && farms.isNotEmpty()) {
                        FloatingActionButton(
                            onClick = onTapNewBarnOrCycle,
                            containerColor = primaryButtonAccent,
                            contentColor = Color.White,
                            shape = RoundedCornerShape(activeCornerRadius),
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                            modifier = Modifier
                                .tactile3DStyle(baseColor = primaryButtonAccent, cornerRadius = activeCornerRadius)
                                .testTag("fab_add_cycle")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add New Farm Cycle", tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "${Translations.tr("btn_new_cycle", lang)} 🐣",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
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
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .tactile3DStyle(baseColor = primaryButtonAccent, cornerRadius = activeCornerRadius)
                            .testTag("fab_add_market_post")
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
            val dynamicGlobalTabs = buildList {
                if (showBarnsTab) {
                    add(Triple(GlobalDashboardTab.BARNS, "💼 ${Translations.tr("tab_management", lang)}", Icons.Default.BusinessCenter))
                }
                add(Triple(GlobalDashboardTab.CHAT, Translations.tr("global_tab_chat", lang), Icons.Default.ChatBubbleOutline))
                add(Triple(GlobalDashboardTab.MARKET, Translations.tr("global_tab_market", lang), Icons.Default.ShoppingCart))
                add(Triple(GlobalDashboardTab.AI_VET, Translations.tr("global_tab_ai_vet", lang), Icons.Default.MenuBook))
            }
            val selectedGlobalIdx = dynamicGlobalTabs.indexOfFirst { it.first == selectedGlobalTab }.coerceAtLeast(0)

            AgritechAnimatedBottomBar(
                items = dynamicGlobalTabs.mapIndexed { idx, (tabEnum, label, icon) ->
                    AgritechTabBarItemSpec(
                        id = tabEnum.name,
                        label = label,
                        icon = if (tabEnum == GlobalDashboardTab.AI_VET) null else icon,
                        emoji = if (tabEnum == GlobalDashboardTab.AI_VET) "📖" else null,
                        testTag = "global_nav_tab_${tabEnum.name.lowercase()}_$idx"
                    )
                },
                selectedIndex = selectedGlobalIdx,
                onSelectIndex = { idx ->
                    val tabEnum = dynamicGlobalTabs.getOrNull(idx)?.first ?: GlobalDashboardTab.BARNS
                    selectedGlobalTab = tabEnum
                    if (tabEnum != GlobalDashboardTab.CHAT) selectedChatConversation = null
                },
                currentTheme = currentTheme,
                modifier = Modifier.testTag("owner_global_bottom_nav")
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .darwinPaperBackground(currentTheme)
                .padding(padding)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
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
                        onOpenWorkerSmartNotebook = { isWorkerNotebookOpen = true },
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
                    val visibleConversations = AgritechMockDb.visibleChatConversations()
                    if (activeConv == null || activeConv.id in AgritechMockDb.blockedUserIds || activeConv.id in AgritechMockDb.reportedUgcItemIds) {
                        ChatListScreen(
                            conversations = visibleConversations,
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
                    // 🛒 السوق (Interactive User-Driven MarketFeedScreen with Google Play UGC Reporting & Blocking)
                    MarketFeedScreen(
                        posts = AgritechMockDb.visibleMarketPosts(),
                        currentTheme = currentTheme,
                        lang = lang,
                        firestoreRepo = firestoreRepo,
                        onContactPublisher = { post ->
                            val currentBuyerUid = AgritechAuthHelper.currentUserUid()
                                ?: sessionState.currentUserUid.orEmpty()
                            val resolvedDirectChannelId = if (currentBuyerUid.isNotBlank() && post.publisherId.isNotBlank() && currentBuyerUid != post.publisherId) {
                                AgritechFirestoreRepository.buildMarketDirectChannelId(
                                    postId = post.id,
                                    uidA = currentBuyerUid,
                                    uidB = post.publisherId
                                )
                            } else {
                                post.chatChannelId
                            }
                            val matchedConv = AgritechMockDb.chatConversations.firstOrNull {
                                it.id == resolvedDirectChannelId
                            } ?: ChatConversationModel(
                                id = resolvedDirectChannelId,
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
            val dialogBg = currentTheme.dialogCardColor
            val dialogCornerDp = activeCornerRadius

            AlertDialog(
                onDismissRequest = {
                    showNewCycleDialog = false
                    targetFarmForCycle = null
                    newCycleValidationError = null
                },
                shape = RoundedCornerShape(dialogCornerDp),
                containerColor = dialogBg,
                modifier = Modifier.darwinManuscriptBorder(cornerRadius = dialogCornerDp, isDark = isDark),
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
                        LuminousOutlinedTextField(
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
                                .testTag("input_farm_name")
                        )

                        LuminousOutlinedTextField(
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
                                .testTag("input_location")
                        )

                        LuminousOutlinedTextField(
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
                                .testTag("input_bird_count")
                        )

                        LuminousOutlinedTextField(
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
                                .testTag("input_chick_price")
                        )

                        newCycleValidationError?.let { errMsg ->
                            Text(
                                text = errMsg,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = ActionRed,
                                modifier = Modifier.testTag("new_cycle_validation_error")
                            )
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = accentBadgeBg),
                            border = BorderStroke(1.dp, accentEmerald.copy(alpha = 0.45f))
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
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("btn_confirm_new_cycle"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryButtonAccent)
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
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp),
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
    onOpenWorkerSmartNotebook: () -> Unit = {},
    onStartNewCycleForFarm: (FarmModel) -> Unit,
    onIssueVetInvoiceForFarm: (FarmModel) -> Unit,
    onOpenImporterChat: (String, String) -> Unit
) {
    val context = LocalContext.current
    val sessionState = LocalAgritechAppState.current
    val appearance = LocalAppAppearanceState.current
    val isDark = currentTheme.isDark
    val primaryTextColor = currentTheme.headingColor
    val secondaryTextColor = currentTheme.secondaryTextColor
    val activeAccent = appearance.accentColor
    val primaryButtonAccent = appearance.accentOption.primaryColor
    val accentBadgeBg = appearance.accentContainerColor
    val cardCornerShape = RoundedCornerShape(appearance.cornerRadiusDp.dp)
    val pillShape = RoundedCornerShape((appearance.cornerRadiusDp - 4).coerceAtLeast(4).dp)
    val normalizedRole = activeRole.normalizedHubRole
    val curr = Translations.tr("currency", lang)

    var selectedVetHubTab by rememberSaveable { mutableStateOf(0) }
    var selectedWorkerHubTab by rememberSaveable { mutableStateOf(0) }
    var showPartnerFinancialReqDialog by remember { mutableStateOf(false) }
    var showMyManagementsDialog by remember { mutableStateOf(false) }
    var showAddManagementDialog by remember { mutableStateOf(false) }

    val enabledRolesForUser = remember(
        sessionState.currentUserUid,
        sessionState.enabledRolesForUser,
        sessionState.currentRole,
        sessionState.hasSelectedInitialProfession
    ) {
        val persisted = SaasPreferencesManager.getEnabledRolesForUser(context, sessionState.currentUserUid)
        when {
            persisted.isNotEmpty() -> persisted
            sessionState.enabledRolesForUser.isNotEmpty() -> sessionState.enabledRolesForUser
            sessionState.hasSelectedInitialProfession && activeRole.isCurrentlyAvailable -> setOf(normalizedRole)
            else -> emptySet()
        }
    }

    if (showMyManagementsDialog) {
        MyManagementsDialog(
            enabledRoles = enabledRolesForUser,
            activeRole = activeRole,
            lang = lang,
            currentTheme = currentTheme,
            onSelectEnabledRole = { chosenRole ->
                if (sessionState.trySwitchOrAddManagement(context, chosenRole)) {
                    onSelectRole(chosenRole)
                }
            },
            onOpenAddManagement = {
                showMyManagementsDialog = false
                showAddManagementDialog = true
            },
            onDismiss = { showMyManagementsDialog = false }
        )
    }

    if (showAddManagementDialog) {
        val hasAdditionalEntitlement = SaasPreferencesManager.hasAdditionalRoleEntitlement(
            context,
            sessionState.currentUserUid
        )
        AddManagementDialog(
            enabledRoles = enabledRolesForUser,
            activeRole = activeRole,
            hasAdditionalRoleEntitlement = hasAdditionalEntitlement,
            lang = lang,
            currentTheme = currentTheme,
            onEnableAndSwitchRole = { chosenRole ->
                if (sessionState.trySwitchOrAddManagement(context, chosenRole)) {
                    onSelectRole(chosenRole)
                }
            },
            onDismiss = { showAddManagementDialog = false }
        )
    }

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
        // Top Management Action Bar: Strict separation between «إداراتي» and «+ إضافة إدارة»
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val mgmtPillCornerDp = (appearance.cornerRadiusDp - 6).coerceAtLeast(4).dp
            Surface(
                shape = pillShape,
                color = if (appearance.paperStyle == AppPaperStyleOption.FROSTED_GLASS) currentTheme.cardColor else accentBadgeBg,
                border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.45f)),
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .darwinManuscriptBorder(cornerRadius = mgmtPillCornerDp, isDark = isDark)
                    .clip(pillShape)
                    .clickable { showMyManagementsDialog = true }
                    .testTag("btn_my_managements")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.BusinessCenter,
                        contentDescription = null,
                        tint = activeAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Translations.tr("btn_my_managements", lang),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.5.sp,
                        color = activeAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                shape = pillShape,
                color = currentTheme.cardColor,
                border = BorderStroke(1.2.dp, activeAccent.copy(alpha = 0.55f)),
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .darwinManuscriptBorder(cornerRadius = mgmtPillCornerDp, isDark = isDark)
                    .clip(pillShape)
                    .clickable { showAddManagementDialog = true }
                    .testTag("btn_add_management")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Translations.tr("btn_add_management", lang),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.5.sp,
                        color = primaryTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Dynamic Profession Content Area
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
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 8.dp)
                                        ) {
                                            Text(
                                                Translations.getLocalizedFarmName(farm.id, lang, farm.name),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.5.sp,
                                                color = primaryTextColor,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Text(
                                                Translations.getLocalizedFarmLocation(farm.id, lang, farm.location),
                                                fontSize = 12.sp,
                                                color = secondaryTextColor,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        val compactLivabilityText = if (kotlin.math.abs(livability - kotlin.math.round(livability)) < 0.05) {
                                            String.format(java.util.Locale.US, "%.0f%%", livability)
                                        } else {
                                            String.format(java.util.Locale.US, "%.1f%%", livability)
                                        }
                                        Surface(
                                            shape = pillShape,
                                            color = accentBadgeBg
                                        ) {
                                            Text(
                                                "${Translations.tr("liveability", lang)}: $compactLivabilityText",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontSize = 11.sp,
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
                AppRole.WORKER, AppRole.HOME_PERSONAL -> {
                    val workerBarns = if (farms.isNotEmpty()) farms else AgritechMockDb.supervisedVetFarms
                    val personalEntries = AgritechMockDb.workerPersonalEntries
                    val totalPersonalInc = personalEntries.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                    val totalPersonalExp = personalEntries.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                    val netPersonalBal = totalPersonalInc - totalPersonalExp

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("worker_management_hub"),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. بطاقة ترحيبية أنيقة للمشرف / العامل مع زر [📖 فتح الدفتر الذكي للمشرف / العامل]
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("worker_welcome_hub_card"),
                                shape = cardCornerShape,
                                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                border = BorderStroke(1.5.dp, activeAccent.copy(alpha = 0.55f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = accentBadgeBg,
                                            modifier = Modifier.size(46.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("👷", fontSize = 22.sp)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                Translations.tr("worker_smart_notebook_card_title", lang),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.5.sp,
                                                color = primaryTextColor
                                            )
                                            Text(
                                                Translations.tr("worker_smart_notebook_card_subtitle", lang),
                                                fontSize = 11.5.sp,
                                                color = secondaryTextColor,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }

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
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    Translations.tr("worker_tab_barns_daily", lang),
                                                    fontSize = 10.5.sp,
                                                    color = secondaryTextColor,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    "${workerBarns.size} 🏚️",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 13.sp,
                                                    color = activeAccent,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                        Surface(
                                            modifier = Modifier.weight(1.2f),
                                            shape = pillShape,
                                            color = accentBadgeBg
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    Translations.tr("worker_tab_home_wallet", lang),
                                                    fontSize = 10.5.sp,
                                                    color = secondaryTextColor,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    "${String.format(java.util.Locale.US, "%,.0f", netPersonalBal)} $curr",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.5.sp,
                                                    color = if (netPersonalBal >= 0) activeAccent else ActionRed,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = pillShape,
                                            color = accentBadgeBg
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    Translations.tr("worker_tab_vet_tasks", lang),
                                                    fontSize = 10.5.sp,
                                                    color = secondaryTextColor,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    "${personalEntries.count { it.type == TransactionType.EXPENSE }} 🧾",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 13.sp,
                                                    color = activeAccent,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = onOpenWorkerSmartNotebook,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("btn_open_worker_smart_notebook"),
                                        shape = pillShape,
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryButtonAccent)
                                    ) {
                                        Text(
                                            Translations.tr("btn_open_worker_smart_notebook", lang),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.5.sp,
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        // بطاقات العنابر مخفية من الشاشة الخارجية للمشرف وتظهر فقط بعد فتح الدفتر الذكي داخل تبويب 💼 الإدارة
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
                    pendingHubTxForSignature?.let { targetTx ->
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
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { showMyManagementsDialog = true },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .testTag("btn_zero_state_my_managements"),
                                            shape = pillShape,
                                            border = BorderStroke(1.2.dp, activeAccent)
                                        ) {
                                            Text(
                                                Translations.tr("btn_my_managements", lang),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = activeAccent,
                                                maxLines = 1
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { showAddManagementDialog = true },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .testTag("btn_zero_state_add_management"),
                                            shape = pillShape,
                                            border = BorderStroke(1.2.dp, activeAccent.copy(alpha = 0.65f))
                                        ) {
                                            Text(
                                                Translations.tr("btn_add_management", lang),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = primaryTextColor,
                                                maxLines = 1
                                            )
                                        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerSmartNotebookScreen(
    farms: List<FarmModel>,
    currentTheme: AppThemeOption,
    lang: AppLanguage,
    onOpenDailyReport: (FarmModel) -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val appearance = LocalAppAppearanceState.current
    val isDark = currentTheme.isDark
    val primaryTextColor = if (isDark) Color(0xFFFFFFFF) else currentTheme.headingColor
    val secondaryTextColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    val activeAccent = appearance.accentColor
    val primaryButtonAccent = appearance.accentOption.primaryColor
    val accentBadgeBg = appearance.accentContainerColor
    val cardCornerShape = RoundedCornerShape(appearance.cornerRadiusDp.dp)
    val pillShape = RoundedCornerShape((appearance.cornerRadiusDp - 4).coerceAtLeast(8).dp)
    val curr = Translations.tr("currency", lang)

    val workerBarns = if (farms.isNotEmpty()) farms else AgritechMockDb.supervisedVetFarms
    val personalEntries = AgritechMockDb.workerPersonalEntries.filter { it.farmId == "personal_worker_ledger" }
    var selectedWorkerTab by rememberSaveable { mutableStateOf(0) }
    var billCardApproved by rememberSaveable { mutableStateOf(false) }
    var billCardDismissed by rememberSaveable { mutableStateOf(false) }
    var showFreeTxDialog by remember { mutableStateOf(false) }
    var activeTxType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var isAddingInvoiceMode by remember { mutableStateOf(false) }

    val totalIncome = personalEntries.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpenses = personalEntries.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val netBalance = totalIncome - totalExpenses

    val homeSummary = remember(personalEntries, totalIncome, totalExpenses) {
        val expenseItems = personalEntries.filter { it.type == TransactionType.EXPENSE }
        var elecGasSum = 0.0
        var internetSum = 0.0
        var carSum = 0.0
        var groceriesSum = 0.0
        for (item in expenseItems) {
            val t = item.title.lowercase()
            when {
                t.contains("كهرباء") || t.contains("غاز") || t.contains("ماء") || t.contains("سونلغاز") || t.contains("electricity") || t.contains("gas") ->
                    elecGasSum += item.amount
                t.contains("إنترنت") || t.contains("انترنت") || t.contains("هاتف") || t.contains("اتصالات") || t.contains("internet") || t.contains("phone") ->
                    internetSum += item.amount
                t.contains("سيارة") || t.contains("وقود") || t.contains("نقل") || t.contains("تأمين") || t.contains("صيانة") || t.contains("car") || t.contains("fuel") ->
                    carSum += item.amount
                else ->
                    groceriesSum += item.amount
            }
        }
        PersonalAndHouseholdEngine.evaluateMonthlyHouseholdBudget(
            monthlySalaryAndIncome = totalIncome,
            electricityAndGasExpense = elecGasSum,
            internetAndPhoneExpense = internetSum,
            carInsuranceAndMaintenanceExpense = carSum,
            groceriesAndHomeExpense = groceriesSum,
            previousMonthTotalExpense = 36000.0
        )
    }

    if (showFreeTxDialog) {
        WorkerFreeTextTransactionDialog(
            type = activeTxType,
            isInvoiceMode = isAddingInvoiceMode,
            lang = lang,
            curr = curr,
            currentTheme = currentTheme,
            onDismiss = { showFreeTxDialog = false },
            onConfirm = { customName, amount ->
                val todayDate = java.time.LocalDate.now().toString()
                AgritechMockDb.workerPersonalEntries.add(
                    0,
                    WorkerPersonalEntryModel(
                        id = "w_tx_${System.currentTimeMillis()}",
                        farmId = "personal_worker_ledger",
                        title = customName,
                        amount = amount,
                        type = activeTxType,
                        dateLabel = todayDate,
                        isBillOrObligation = activeTxType == TransactionType.EXPENSE
                    )
                )
                LocalFarmPersistence.saveAll(context)
                showFreeTxDialog = false
            }
        )
    }

    val workerTabs = remember(lang) {
        listOf(
            Triple(0, "💼", Translations.tr("worker_tab_barns_daily", lang)),
            Triple(1, "💰", Translations.tr("worker_tab_home_wallet", lang)),
            Triple(2, "📊", Translations.tr("worker_tab_analytics_agent", lang)),
            Triple(3, "🧾", Translations.tr("worker_tab_vet_tasks", lang))
        )
    }

    Scaffold(
        containerColor = currentTheme.bgColor,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_worker_notebook_back")
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = primaryTextColor
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            Translations.tr("worker_smart_notebook_card_title", lang),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = primaryTextColor,
                            maxLines = 1
                        )
                        Text(
                            workerTabs.getOrNull(selectedWorkerTab)?.third ?: "",
                            fontSize = 11.5.sp,
                            color = activeAccent,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = primaryTextColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = currentTheme.bgColor)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = currentTheme.cardColor,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("worker_hub_bottom_nav")
            ) {
                workerTabs.forEach { (idx, emoji, title) ->
                    NavigationBarItem(
                        selected = selectedWorkerTab == idx,
                        onClick = { selectedWorkerTab = idx },
                        modifier = Modifier.testTag("worker_hub_tab_$idx"),
                        icon = {
                            Text(emoji, fontSize = 17.sp)
                        },
                        label = {
                            Text(
                                title.replace(emoji, "").trim(),
                                fontSize = 10.5.sp,
                                fontWeight = if (selectedWorkerTab == idx) FontWeight.ExtraBold else FontWeight.Medium,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = activeAccent,
                            selectedTextColor = activeAccent,
                            indicatorColor = accentBadgeBg,
                            unselectedIconColor = secondaryTextColor,
                            unselectedTextColor = secondaryTextColor
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
                .testTag("worker_smart_notebook_screen")
        ) {
            when (selectedWorkerTab) {
                0 -> {
                    // التبويب الأول: 💼 الإدارة (العنابر المسؤول عنها لتسجيل البيانات اليومية فقط بدون فواتير المزرعة)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(workerBarns, key = { it.id }) { farm ->
                            val cycle = AgritechMockDb.cycles.firstOrNull { it.farmId == farm.id }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = cardCornerShape,
                                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.25f))
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
                                        onClick = { onOpenDailyReport(farm) },
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
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
                1 -> {
                    // التبويب الثاني: 💰 الخزنة (حساب ذكي خاص بالمشرف أو العامل الحر مع إدخال حر للاسم والمبلغ)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .testTag("home_personal_management_hub"),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = cardCornerShape,
                                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                border = BorderStroke(1.2.dp, activeAccent.copy(alpha = 0.35f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        Translations.tr("home_ledger_title", lang),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = primaryTextColor
                                    )
                                    Text(
                                        Translations.tr("home_ledger_subtitle", lang),
                                        fontSize = 12.sp,
                                        color = secondaryTextColor
                                    )
                                    NeumorphicRecessedDisplay(
                                        isDark = currentTheme.isDark,
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CalmFinancialItem(
                                                label = Translations.tr("total_income", lang),
                                                value = "${String.format(java.util.Locale.US, "%,.0f", totalIncome)} $curr",
                                                color = activeAccent,
                                                modifier = Modifier.weight(1f)
                                            )
                                            CalmFinancialItem(
                                                label = Translations.tr("total_expenses", lang),
                                                value = "${String.format(java.util.Locale.US, "%,.0f", totalExpenses)} $curr",
                                                color = ActionRed,
                                                modifier = Modifier.weight(1f)
                                            )
                                            CalmFinancialItem(
                                                label = Translations.tr("net_balance", lang),
                                                value = "${String.format(java.util.Locale.US, "%,.0f", netBalance)} $curr",
                                                color = if (netBalance >= 0) activeAccent else ActionRed,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        PremiumPillButton(
                                            text = Translations.tr("btn_spend", lang),
                                            onClick = {
                                                activeTxType = TransactionType.EXPENSE
                                                isAddingInvoiceMode = false
                                                showFreeTxDialog = true
                                            },
                                            variant = PremiumPillVariant.DELETE_RESET,
                                            fontSize = 12.sp,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .testTag("btn_home_add_expense")
                                        )
                                        PremiumPillButton(
                                            text = Translations.tr("btn_income", lang),
                                            onClick = {
                                                activeTxType = TransactionType.INCOME
                                                isAddingInvoiceMode = false
                                                showFreeTxDialog = true
                                            },
                                            variant = PremiumPillVariant.SALES_INCOME,
                                            fontSize = 12.sp,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .testTag("btn_home_add_income")
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                Translations.tr("worker_bills_ledger_section_title", lang),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.5.sp,
                                color = primaryTextColor
                            )
                        }

                        items(personalEntries, key = { it.id }) { entry ->
                            val isExp = entry.type == TransactionType.EXPENSE
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = cardCornerShape,
                                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.22f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            entry.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = primaryTextColor
                                        )
                                        Text(
                                            entry.dateLabel,
                                            fontSize = 11.sp,
                                            color = secondaryTextColor
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            "${if (isExp) "-" else "+"}${String.format(java.util.Locale.US, "%,.0f", entry.amount)} $curr",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = if (isExp) ActionRed else activeAccent
                                        )
                                        IconButton(
                                            onClick = {
                                                AgritechMockDb.workerPersonalEntries.removeAll { it.id == entry.id }
                                                LocalFarmPersistence.saveAll(context)
                                            },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .testTag("btn_delete_worker_entry_${entry.id}")
                                        ) {
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                contentDescription = "Delete",
                                                tint = secondaryTextColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
                2 -> {
                    // التبويب الثالث: 📊 التحليلات (ملخص التوفير الشهرية وتوزيع المصاريف + بطاقة 🧠 تنبيه ذكي ومقارنة شهرية)
                    val savingsRatePct = homeSummary.customKpis["savingsRatePct"] ?: 0.0
                    val momChangePct = homeSummary.customKpis["monthOverMonthPct"] ?: -12.0
                    val expenseEntries = personalEntries.filter { it.type == TransactionType.EXPENSE }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. بطاقة 📈 ملخص نسبة التوفير الشهرية وتوزيع المصاريف (مع ارتفاع داخلي مريح يمنع انقطاع 33,400 DZD)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = cardCornerShape,
                                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                border = BorderStroke(1.2.dp, activeAccent.copy(alpha = 0.35f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        Translations.tr("worker_savings_summary_title", lang),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.5.sp,
                                        color = primaryTextColor
                                    )
                                    NeumorphicRecessedDisplay(
                                        isDark = currentTheme.isDark,
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CalmFinancialItem(
                                                label = Translations.tr("worker_savings_rate_label", lang),
                                                value = "${String.format(java.util.Locale.US, "%.1f", savingsRatePct)}%",
                                                color = activeAccent,
                                                modifier = Modifier.weight(1f)
                                            )
                                            CalmFinancialItem(
                                                label = Translations.tr("worker_mom_comparison_label", lang),
                                                value = "${String.format(java.util.Locale.US, "%+.1f", momChangePct)}%",
                                                color = if (momChangePct <= 0) activeAccent else ActionRed,
                                                modifier = Modifier.weight(1f)
                                            )
                                            CalmFinancialItem(
                                                label = Translations.tr("net_balance", lang),
                                                value = "${String.format(java.util.Locale.US, "%,.0f", netBalance)} $curr",
                                                color = if (netBalance >= 0) activeAccent else ActionRed,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }

                                    // توزيع المصاريف الشخصية للعامل
                                    expenseEntries.forEach { exp ->
                                        val sharePct = if (totalExpenses > 0) (exp.amount / totalExpenses) * 100.0 else 0.0
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "${exp.title} (${String.format(java.util.Locale.US, "%.0f", sharePct)}%)",
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = primaryTextColor,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                "${String.format(java.util.Locale.US, "%,.0f", exp.amount)} $curr",
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = ActionRed
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 2. بطاقة الوكيل الذكي 🧠 تنبيه ذكي ومقارنة شهرية
                        if (!billCardDismissed) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("home_proactive_ai_bill_card"),
                                    shape = cardCornerShape,
                                    colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                    border = BorderStroke(1.2.dp, activeAccent.copy(alpha = 0.45f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            Translations.tr("worker_ai_smart_alert_title", lang),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = activeAccent
                                        )
                                        Text(
                                            Translations.tr("home_ai_card1_body", lang),
                                            fontSize = 12.5.sp,
                                            color = primaryTextColor,
                                            lineHeight = 18.sp
                                        )
                                        if (billCardApproved) {
                                            Surface(
                                                shape = pillShape,
                                                color = appearance.accentContainerColor,
                                                border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.5f)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    Translations.tr("home_ai_card1_done", lang),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = activeAccent,
                                                    modifier = Modifier.padding(10.dp)
                                                )
                                            }
                                        } else {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        AgritechMockDb.workerPersonalEntries.add(
                                                            0,
                                                            WorkerPersonalEntryModel(
                                                                id = "w_tx_ai_${System.currentTimeMillis()}",
                                                                farmId = "personal_worker_ledger",
                                                                title = Translations.tr("home_cat_electricity_gas", lang),
                                                                amount = 4500.0,
                                                                type = TransactionType.EXPENSE,
                                                                dateLabel = java.time.LocalDate.now().toString(),
                                                                isBillOrObligation = true
                                                            )
                                                        )
                                                        LocalFarmPersistence.saveAll(context)
                                                        billCardApproved = true
                                                    },
                                                    shape = pillShape,
                                                    colors = ButtonDefaults.buttonColors(containerColor = primaryButtonAccent),
                                                    modifier = Modifier
                                                        .weight(1.3f)
                                                        .height(40.dp)
                                                        .testTag("btn_home_ai_approve_bill")
                                                ) {
                                                    Text(
                                                        Translations.tr("home_ai_card1_approve", lang),
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        maxLines = 1
                                                    )
                                                }
                                                OutlinedButton(
                                                    onClick = { billCardDismissed = true },
                                                    shape = pillShape,
                                                    border = BorderStroke(1.dp, secondaryTextColor.copy(alpha = 0.4f)),
                                                    modifier = Modifier
                                                        .weight(0.7f)
                                                        .height(40.dp)
                                                ) {
                                                    Text(
                                                        Translations.tr("ai_agent_card2_ignore", lang),
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = secondaryTextColor
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
                3 -> {
                    // التبويب الرابع: 🧾 الفواتير (سجل الفواتير والمشتريات والالتزامات الدورية الخاصة بالعامل + زر [+ إضافة فاتورة جديدة])
                    val invoiceEntries = personalEntries.filter { it.type == TransactionType.EXPENSE }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    Translations.tr("worker_invoices_tab_section_title", lang),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.5.sp,
                                    color = primaryTextColor,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        activeTxType = TransactionType.EXPENSE
                                        isAddingInvoiceMode = true
                                        showFreeTxDialog = true
                                    },
                                    shape = pillShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryButtonAccent),
                                    modifier = Modifier
                                        .height(40.dp)
                                        .testTag("btn_worker_add_custom_bill")
                                ) {
                                    Text(
                                        Translations.tr("worker_add_new_bill_btn", lang),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        items(invoiceEntries, key = { it.id }) { bill ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = cardCornerShape,
                                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.28f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "🧾 ${bill.title}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = primaryTextColor
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "📅 ${bill.dateLabel}",
                                            fontSize = 11.5.sp,
                                            color = secondaryTextColor
                                        )
                                    }
                                    Text(
                                        "${String.format(java.util.Locale.US, "%,.0f", bill.amount)} $curr",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.5.sp,
                                        color = ActionRed
                                    )
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


data class Ross308BookChapter(
    val chapterNumber: Int,
    val tag: String,
    val title: String,
    val ageRangeLabel: String,
    val summaryPoints: List<String>,
    val fullScientificReference: String,
    val liveBarnComparison: String
)

data class BookSubCardModel(
    val id: String,
    val branchIndex: Int,
    val cardNumberInBranch: Int,
    val totalCardsInBranch: Int = 4,
    val iconEmoji: String,
    val title: String,
    val badge1: String,
    val badge2: String,
    val scientificRulePoints: List<String>,
    val practicalStepsWithCheck: List<String>,
    val liveBarnComparisonText: String,
    val posterSpec: EncyclopediaPosterSpec? = null
)

data class BookBranchModel(
    val branchIndex: Int,
    val tag: String,
    val title: String,
    val shortName: String,
    val subtitle: String,
    val iconEmoji: String,
    val subCards: List<BookSubCardModel>
)

object Ross308BroilerHandbook {

    fun getStandardTempForDay(ageDays: Int): Double {
        return when {
            ageDays <= 3 -> 33.0
            ageDays <= 7 -> 31.0
            ageDays <= 14 -> 29.0
            ageDays <= 21 -> 27.0
            ageDays <= 27 -> 24.0
            else -> 21.0
        }
    }

    fun getStandardMinVentM3PerHourPerBird(ageDays: Int): Double {
        return when {
            ageDays <= 7 -> 0.10
            ageDays <= 14 -> 0.25
            ageDays <= 21 -> 0.55
            ageDays <= 28 -> 0.95
            ageDays <= 34 -> 1.25
            else -> 1.50
        }
    }

    fun getStandardFcrForDay(ageDays: Int): Double {
        return when {
            ageDays <= 7 -> 0.85
            ageDays <= 14 -> 1.08
            ageDays <= 21 -> 1.25
            ageDays <= 28 -> 1.38
            ageDays <= 35 -> 1.50
            else -> 1.65
        }
    }

    fun getStandardFeedPhaseForDay(ageDays: Int): String {
        return when {
            ageDays <= 10 -> "بادئ (Starter — الأيام 1–10: بروتين 23% • طاقة 3000 kcal/kg • محبب ناعم Crumble)"
            ageDays <= 21 -> "نامي (Grower — الأيام 11–21: بروتين 22% • طاقة 3100 kcal/kg)"
            else -> "ناهي (Finisher — من اليوم 22 حتى التسويق: بروتين 20.5% • طاقة 3200 kcal/kg • Pellets)"
        }
    }

    fun buildChaptersForBarn(
        barnName: String,
        ageDays: Int,
        placedBirds: Int,
        liveBirds: Int,
        recordedTempC: Double?,
        totalFeedQuintals: Double,
        remainingFeedQuintals: Double,
        latestDailyFeedKg: Double,
        latestDailyWaterLiters: Double,
        currentWeightKg: Double,
        actualFcr: Double,
        qUnit: String = "ق",
        kgUnit: String = "كغ"
    ): List<Ross308BookChapter> {
        val safeAge = ageDays.coerceAtLeast(1)
        val targetTempC = getStandardTempForDay(safeAge)
        val minVentPerBird = getStandardMinVentM3PerHourPerBird(safeAge)
        val totalMinVentM3h = liveBirds * minVentPerBird
        val targetWeightKg = PoultryCalculators.getRoss308StandardWeightKg(safeAge)
        val targetFcr = getStandardFcrForDay(safeAge)
        val currentFeedPhase = getStandardFeedPhaseForDay(safeAge)
        val waterToFeedRatio = if (latestDailyFeedKg > 0.0 && latestDailyWaterLiters > 0.0) {
            latestDailyWaterLiters / latestDailyFeedKg
        } else 0.0

        val ch1 = Ross308BookChapter(
            chapterNumber = 1,
            tag = "ross308_chapter_1",
            title = "الفصل الأول: استقبال الكتاكيت والتحضين الحرج (الأيام 1–7) 🐣",
            ageRangeLabel = "الأيام 1–7",
            summaryPoints = listOf(
                "التدفئة المسبقة (Pre-heating) قبل 24 ساعة: حرارة الفرشة 28°C – 30°C (بعمق 2–4 سم) وحرارة الهواء 30°C – 32°C ورطوبة نسبية 60% – 70%.",
                "حرارة المجمع (Vent/Cloacal Temp): المعيار الذهبي لراحة الكتكوت 39.4°C – 40.5°C.",
                "فحص امتلاء الحوصلة (Crop Fill): بعد ساعتين 75% • بعد 8 ساعات 85% • بعد 24 ساعة > 95% (مع إضاءة 30–40 Lux).",
                "هدف وزن اليوم السابع: 4.6 – 4.8 أضعاف وزن الفقس (190 – 200 غرام كحد أدنى)."
            ),
            fullScientificReference = "مرجع Aviagen Ross 308 (الفصل 1 — التحضين الحرج):\n" +
                "1) التجهيز الحراري: لا تعتمد على حرارة الهواء وحدها؛ افحص حرارة الفرشة (28°C – 30°C) لأن أقدام الكتكوت تفقد الحرارة بسرعة إذا كانت الأرضية باردة.\n" +
                "2) بروتوكول الحوصلة (Crop Fill): افحص 30–40 كتكوتاً عشوائياً؛ الحوصلة السليمة تكون لينة وممتلئة بمزيج الماء والعلف (> 95% عند 24 ساعة).\n" +
                "3) كل 10 غرامات إضافية فوق وزن 190 غ في اليوم 7 تترجم إلى +50 إلى +70 غراماً إضافية عند عمر التسويق (اليوم 35–42).",
            liveBarnComparison = "🏠 تطبيق الفصل على [$barnName]: العمر الحالي اليوم $safeAge • الكتاكيت المسكنة: ${String.format(java.util.Locale.US, "%,d", placedBirds)} • " +
                if (safeAge <= 7) {
                    "أنت الآن في مرحلة التحضين الحرج! راقب امتلاء الحوصلة (> 95%) واستهدف وزن 190–200 غ في اليوم 7."
                } else {
                    "تجاوز القطيع مرحلة التحضين (اليوم $safeAge) بحيوية ${String.format(java.util.Locale.US, "%,d", liveBirds)} طائر حي."
                }
        )

        val tempComparisonText = if (recordedTempC != null) {
            val diff = recordedTempC - targetTempC
            val statusNote = when {
                diff > 2.0 -> "⚠️ أعلى من معيار Ross 308 بـ ${String.format(java.util.Locale.US, "+%.1f°C", diff)} — ارفع التهوية لتجنب الإجهاد الحراري."
                diff < -2.0 -> "⚠️ أقل من معيار Ross 308 بـ ${String.format(java.util.Locale.US, "%.1f°C", diff)} — اضبط التدفئة مع الحفاظ على التهوية الدنيا."
                else -> "✓ مطابقة تماماً لمعيار Ross 308 لعمر اليوم $safeAge."
            }
            "حرارة عنبرك المسجلة: ${String.format(java.util.Locale.US, "%.1f°C", recordedTempC)} مقابل الهدف القياسي لليوم $safeAge (${String.format(java.util.Locale.US, "%.0f°C", targetTempC)}) — $statusNote"
        } else {
            "حرارة عنبرك غير مسجلة بعد في السجل اليومي — الهدف القياسي لليوم $safeAge هو ${String.format(java.util.Locale.US, "%.0f°C", targetTempC)}."
        }

        val ch2 = Ross308BookChapter(
            chapterNumber = 2,
            tag = "ross308_chapter_2",
            title = "الفصل الثاني: جدول الحرارة والرطوبة والتهوية القياسي (الأيام 1–42) 🌡️💨",
            ageRangeLabel = "الأيام 1–42",
            summaryPoints = listOf(
                "منحنى الحرارة القياسي (رطوبة 60–70%): اليوم 1: 30°C ⬅️ اليوم 7: 28°C ⬅️ اليوم 14: 26°C ⬅️ اليوم 21: 24°C ⬅️ اليوم 27+: 20°C – 21°C.",
                "الحد الأدنى للتهوية (Minimum Ventilation): يبدأ من 0.10 m³/ساعة/طائر في اليوم 1 ويصل إلى 1.50 m³/ساعة/طائر في اليوم 35.",
                "معايير جودة هواء العنبر: الأمونيا NH3 < 10 ppm • ثاني أكسيد الكربون CO2 < 2500 ppm • لا تكتم العنبر أبداً لتوفير الغاز!"
            ),
            fullScientificReference = "مرجع Aviagen Ross 308 (الفصل 2 — المناخ والتهوية):\n" +
                "• جدول التدرج الحراري الكامل: اليوم 1 (30°C) | اليوم 3 (28°C) | اليوم 6 (27°C) | اليوم 9 (26°C) | اليوم 12 (25°C) | اليوم 15 (24°C) | اليوم 18 (23°C) | اليوم 21 (22°C) | اليوم 24 (21°C) | اليوم 27 حتى البيع (20°C).\n" +
                "• التهوية الدنيا ضرورية لطرد الرطوبة والغازات السامة ومنع تبلل الفرشة والتهاب وسادة القدم (Footpad Dermatitis).",
            liveBarnComparison = "🏠 مقارنة مناخ [$barnName] (اليوم $safeAge):\n" +
                "• $tempComparisonText\n" +
                "• التهوية الدنيا المطلوبة حالياً لـ ${String.format(java.util.Locale.US, "%,d", liveBirds)} طائر: ${String.format(java.util.Locale.US, "%,.0f", totalMinVentM3h)} m³/ساعة (${String.format(java.util.Locale.US, "%.2f", minVentPerBird)} m³/ساعة/طائر)."
        )

        val waterRatioComparison = if (waterToFeedRatio > 0.0) {
            "نسبة الماء/العلف المسجلة: ${String.format(java.util.Locale.US, "%.2f", waterToFeedRatio)} لتر/كغ (المعيار الذهبي: 1.6 – 1.8 لتر/كغ عند 21°C)."
        } else {
            "القاعدة الذهبية: 1.6 – 1.8 لتر ماء لكل 1 كغ علف (وترتفع إلى 2.5 – 3.0 أضعاف فوق 30°C)."
        }

        val ch3 = Ross308BookChapter(
            chapterNumber = 3,
            tag = "ross308_chapter_3",
            title = "الفصل الثالث: برنامج التغذية المرحلي وميزان الماء والعلف 🌾💧",
            ageRangeLabel = "3 مراحل تغذية",
            summaryPoints = listOf(
                "بادئ (Starter الأيام 1–10): بروتين 23% وطاقة 3000 kcal/kg (محبب ناعم Crumble — ~250 غ/طائر).",
                "نامي (Grower الأيام 11–21): بروتين 22% وطاقة 3100 kcal/kg (~1.00 كغ/طائر).",
                "ناهي (Finisher من اليوم 22 حتى التسويق): بروتين 20.5% وطاقة 3200 kcal/kg (محبب Pellets 3.5 ملم).",
                "قاعدة الماء الذهبية: 1.6 إلى 1.8 لتر ماء لكل 1 كغ علف في الحرارة المعتدلة (21°C)؛ أي هبوط مفاجئ في شرب الماء هو أول إنذار مبكر للمرض قبل ظهور النافق بـ 24 ساعة."
            ),
            fullScientificReference = "مرجع Aviagen Ross 308 (الفصل 3 — التغذية والماء):\n" +
                "• الانتقال التدريجي بين العلف البادئ والنامي والناهي يمنع صدمة الجهاز الهضمي ورفض العلف.\n" +
                "• اضبط ارتفاع المعالف والمساقي يومياً بحيث تكون حافة المعلف بمستوى ظهر الطائر لتقليل هدر العلف بنسبة تتجاوز 3%.",
            liveBarnComparison = "🏠 برنامج تغذية [$barnName] (اليوم $safeAge):\n" +
                "• المرحلة الموصى بها اليوم: $currentFeedPhase\n" +
                "• العلف المستهلك: ${Translations.formatQuintalNumber(totalFeedQuintals)} $qUnit | المخزون المتبقي: ${Translations.formatQuintalNumber(remainingFeedQuintals)} $qUnit\n" +
                "• $waterRatioComparison"
        )

        val weightDiffGrams = ((currentWeightKg - targetWeightKg) * 1000.0).toInt()
        val fcrComparisonNote = if (actualFcr > 0.0) {
            "معامل التحويل الفعلي: ${String.format(java.util.Locale.US, "%.2f", actualFcr)} مقابل معيار Ross 308 (${String.format(java.util.Locale.US, "%.2f", targetFcr)})"
        } else {
            "معامل التحويل القياسي المستهدف لليوم $safeAge: FCR ~${String.format(java.util.Locale.US, "%.2f", targetFcr)}"
        }

        val ch4 = Ross308BookChapter(
            chapterNumber = 4,
            tag = "ross308_chapter_4",
            title = "الفصل الرابع: منحنى الوزن القياسي ومعامل التحويل الغذائي (FCR) ⚖️📈",
            ageRangeLabel = "جدول الأوزان",
            summaryPoints = listOf(
                "اليوم 7: ~190 غ (FCR ~0.85) • اليوم 14: ~520 غ (FCR ~1.08) • اليوم 21: ~1.00 كغ (FCR ~1.25).",
                "اليوم 28: ~1.58 كغ (FCR ~1.38) • اليوم 35: ~2.25 كغ (FCR ~1.48–1.52) • اليوم 42: ~2.95 كغ (FCR ~1.63–1.68).",
                "معامل التحويل الغذائي (FCR) = إجمالي العلف المستهلك (كغ) ÷ إجمالي الوزن الحي المنتج شاملاً المبيعات الجزئية (كغ)."
            ),
            fullScientificReference = "مرجع Aviagen Ross 308 (الفصل 4 — أهداف الأداء القياسية As-Hatched):\n" +
                "• قم بوزن عينة لا تقل عن 1% من القطيع (أو 100 طائر كحد أدنى) أسبوعياً من 3 نقاط مختلفة داخل العنبر لحساب معامل التجانس (CV% المستهدف < 10%).",
            liveBarnComparison = "🏠 مقارنة أداء [$barnName] (اليوم $safeAge):\n" +
                "• الوزن الحالي: ${String.format(java.util.Locale.US, "%.2f", currentWeightKg)} $kgUnit مقابل معيار Ross 308 (${String.format(java.util.Locale.US, "%.2f", targetWeightKg)} $kgUnit | الفارق: ${String.format(java.util.Locale.US, "%+d", weightDiffGrams)} غ)\n" +
                "• $fcrComparisonNote"
        )

        val lightingRecommendationNow = if (safeAge <= 7) {
            "اليوم $safeAge (أسبوع التحضين): إضاءة 23 ساعة + 1 ساعة ظلام بشدة 30–40 Lux لتحفيز استهلاك العلف والماء."
        } else {
            "اليوم $safeAge (بعد اليوم 7): طبّق 4 إلى 6 ساعات إظلام يومياً بانتظام لمنع الاستسقاء (Ascites) والموت المفاجئ (Flip-over)."
        }

        val ch5 = Ross308BookChapter(
            chapterNumber = 5,
            tag = "ross308_chapter_5",
            title = "الفصل الخامس: برنامج الإضاءة والأمن الحيوي والوقاية من الاستسقاء 🛡️💡",
            ageRangeLabel = "وقاية وإضاءة",
            summaryPoints = listOf(
                "بعد اليوم 7: يُمنع ترك الإضاءة 24 ساعة متواصلة؛ طبّق 4–6 ساعات ظلام يومياً لتقليل الاستسقاء (Ascites) والموت المفاجئ (Flip-over) وتحسين FCR.",
                "الدليل الوقائي: الأسبوع 1 نيوكاسل (Hitchner B1 / Clone 30) + التهاب الشعب المعدي (IB).",
                "الأسبوع 2 (اليوم 12–14): تحصين الجمبورو (IBD) • الأسبوع 3 (اليوم 18–21): تنشيطية نيوكاسل (LaSota / Clone 30) مع فيتامينات AD3E وإلكتروليتات."
            ),
            fullScientificReference = "مرجع Aviagen Ross 308 (الفصل 5 — الإضاءة والأمن الحيوي):\n" +
                "• ساعات الظلام (4–6 ساعات) ضرورية لإفراز هرمون الميلاتونين، تقوية عظام الأرجل، وتخفيف العبء القلبي الرئوي لمنع الاستسقاء.\n" +
                "• قبل أي تحصين في مياه الشرب: ارفع الكلور والمطهرات قبل 48 ساعة، وأضف حليباً خالي الدسم (2 غ/لتر) لحماية اللقاح الحي.",
            liveBarnComparison = "🏠 بروتوكول [$barnName] (اليوم $safeAge):\n" +
                "• الإضاءة: $lightingRecommendationNow\n" +
                "• التحصين القادم/الحالي: ${
                    when {
                        safeAge <= 7 -> "الأسبوع 1: تحصين نيوكاسل (B1/Clone 30) + IB."
                        safeAge <= 14 -> "الأسبوع 2 (اليوم 12–14): تحصين الجمبورو (IBD) في مياه الشرب."
                        safeAge <= 21 -> "الأسبوع 3 (اليوم 18–21): جرعة تنشيطية نيوكاسل (LaSota/Clone 30) + AD3E."
                        else -> "مرحلة التسمين النهائية: دعم فيتامينات AD3E وأملاح معدنية ومراقبة جودة الفرشة والتهوية."
                    }
                }"
        )

        return listOf(ch1, ch2, ch3, ch4, ch5)
    }

    fun buildFourBranchesForBarn(
        barnName: String,
        ageDays: Int,
        placedBirds: Int,
        liveBirds: Int,
        recordedTempC: Double?,
        totalFeedQuintals: Double,
        remainingFeedQuintals: Double,
        latestDailyFeedKg: Double,
        latestDailyWaterLiters: Double,
        currentWeightKg: Double,
        actualFcr: Double,
        qUnit: String = "ق",
        kgUnit: String = "كغ"
    ): List<BookBranchModel> {
        val safeAge = ageDays.coerceAtLeast(1)
        val targetTempC = getStandardTempForDay(safeAge)
        val minVentPerBird = getStandardMinVentM3PerHourPerBird(safeAge)
        val totalMinVentM3h = liveBirds * minVentPerBird
        val targetWeightKg = PoultryCalculators.getRoss308StandardWeightKg(safeAge)
        val targetFcr = getStandardFcrForDay(safeAge)
        val waterToFeedRatio = if (latestDailyFeedKg > 0.0 && latestDailyWaterLiters > 0.0) {
            latestDailyWaterLiters / latestDailyFeedKg
        } else 0.0
        val tempNowStr = if (recordedTempC != null) {
            String.format(java.util.Locale.US, "%.1f°C", recordedTempC)
        } else {
            "غير مسجلة بعد"
        }

        // الفرع 1: 1. برنامج الرعاية 🐣
        val branch0Cards = listOf(
            BookSubCardModel(
                id = "care_1",
                branchIndex = 0,
                cardNumberInBranch = 1,
                iconEmoji = "🔥",
                title = "تطهير العنبر والتدفئة المسبقة (33.5°C هواء / 30°C فرشة)",
                badge1 = "🌡️ 33.5°C هواء / 30°C فرشة",
                badge2 = "🪵 فرشة 5-8 سم",
                scientificRulePoints = listOf(
                    "تطهير العنبر والتدفئة المسبقة قبل 24 ساعة حتى تستقر حرارة الهواء عند 33.5°C والفرشة عند 30°C ورطوبة نسبية 50%–60%.",
                    "الأرضية الباردة تسبب برودة أقدام الصوص واضطراب امتصاص كيس المح والجهاز الهضمي.",
                    "فحص الحوصلة بعد 24 ساعة يجب أن يتجاوز 95% (لينة وممتلئة) للوصول لوزن 190–200 غ في اليوم 7."
                ),
                practicalStepsWithCheck = listOf(
                    "✓ نظّف العنبر وطهّر الأرضية والخطوط ثم ضع فرشة نظيفة وجافة بسمك 5–8 سم شتاءً.",
                    "✓ شغّل التدفئة قبل الوصول بـ 24 ساعة (33.5°C هواء / 30°C فرشة) وافرد ورق التحضين وأطباق العلف.",
                    "✓ قِس حرارة الفرشة وحرارة المجمع (39.4–40.5°C) وافحص امتلاء الحوصلة (>95%) بعد 24 ساعة."
                ),
                liveBarnComparisonText = "🏠 عنبرك الحالي [$barnName] (اليوم $safeAge • الكتاكيت المسكنة: ${String.format(java.util.Locale.US, "%,d", placedBirds)}): " +
                    "حرارة العنبر المسجلة: $tempNowStr (الهدف القياسي لليوم $safeAge: ${String.format(java.util.Locale.US, "%.0f°C", targetTempC)}).",
                posterSpec = Ross308PosterCatalog.forSubCard("care_1")
            ),
            BookSubCardModel(
                id = "care_2",
                branchIndex = 0,
                cardNumberInBranch = 2,
                iconEmoji = "🐣",
                title = "مراقبة سلوك الصيصان في التحضين",
                badge1 = "🌡️ توزيع الحضينة",
                badge2 = "⚠️ راقب السلوك أولاً",
                scientificRulePoints = listOf(
                    "تجمع الصيصان تحت الدفاية يعني برداً، والابتعاد مع اللهاث يعني حرارة زائدة، والتجمع في جهة واحدة يعني تياراً هوائياً.",
                    "التوزيع المتوازن 100% حول المعالف والمساقي مع زقزقة هادئة وأرجل دافئة هو علامة الراحة المثالية.",
                    "لا تعتمد على الترمومتر المعلق فقط بل راقب سلوك الصيصان وتوزعها ودرجة دفء أرجلها دائماً."
                ),
                practicalStepsWithCheck = listOf(
                    "✓ تفقد توزيع الصيصان كل 3 ساعات ليلاً ونهاراً وعدّل الدفايات أو الستائر فوراً.",
                    "✓ أغلق أي شقوق أو فتحات تسبب تياراً هوائياً جانبياً يجمع الصيصان في زاوية واحدة.",
                    "✓ المس أرجل عينة من الصيصان بظهر يدك للتأكد من دفئها وعدم برودة الفرشة."
                ),
                liveBarnComparisonText = "🏠 عنبرك الحالي [$barnName] (اليوم $safeAge • الطيور الحية: ${String.format(java.util.Locale.US, "%,d", liveBirds)}): " +
                    "الوزن الحالي ${String.format(java.util.Locale.US, "%.2f", currentWeightKg)} $kgUnit مقابل معيار Ross 308 (${String.format(java.util.Locale.US, "%.2f", targetWeightKg)} $kgUnit).",
                posterSpec = Ross308PosterCatalog.forSubCard("care_2")
            ),
            BookSubCardModel(
                id = "care_3",
                branchIndex = 0,
                cardNumberInBranch = 3,
                iconEmoji = "💨",
                title = "تهوية العنبر وجودة الهواء",
                badge1 = "💨 تيار علوي للسقف",
                badge2 = "🌬️ أمونيا <10ppm",
                scientificRulePoints = listOf(
                    "جدد الهواء حتى في الطقس البارد عبر الفتحات العلوية المائلة ليختلط بالهواء الدافئ في السقف قبل نزوله للطيور.",
                    "الحد الأقصى المسموح للأمونيا NH3 أقل من 10 ppm وثاني أكسيد الكربون CO2 أقل من 2500 ppm.",
                    "تجنب إغلاق التهوية تماماً في البرد أو توجيه الهواء مباشرة على الطيور أو ترك فرشة مبتلة."
                ),
                practicalStepsWithCheck = listOf(
                    "✓ اضبط الفتحات الجانبية العلوية بحيث تتجه نحو السقف ولا تسقط تياراً بارداً على الطيور.",
                    "✓ شغّل التهوية التوقيتية (Timer) بانتظام لطرد الرطوبة والأمونيا دون هبوط مفاجئ بالحرارة.",
                    "✓ افحص رائحة الأمونيا وجفاف الفرشة يومياً وارفع السحب فوراً عند أي تكثف."
                ),
                liveBarnComparisonText = "🏠 عنبرك الحالي [$barnName] (اليوم $safeAge): " +
                    "التهوية الدنيا المطلوبة لـ ${String.format(java.util.Locale.US, "%,d", liveBirds)} طائر هي ${String.format(java.util.Locale.US, "%,.0f", totalMinVentM3h)} m³/ساعة • الحرارة المسجلة: $tempNowStr.",
                posterSpec = Ross308PosterCatalog.forSubCard("care_3")
            ),
            BookSubCardModel(
                id = "care_4",
                branchIndex = 0,
                cardNumberInBranch = 4,
                iconEmoji = "💡",
                title = "الإضاءة والكثافة وتجهيز ما قبل البيع",
                badge1 = "🌙 إظلام 4-6 ساعات",
                badge2 = "⏱️ تصويم 8-10 ساعات",
                scientificRulePoints = listOf(
                    "تطبيق برنامج الإظلام (4–6 ساعات يومياً بعد اليوم 7) يقوي الهيكل العظمي ويمنع الاستسقاء والموت المفاجئ.",
                    "الكثافة القياسية (10–12 طائر/م² صيفاً و12–14 شتاءً) تحمي التجانس وتقلل أمراض الفرشة.",
                    "تصويم العلف لمدة 8–10 ساعات قبل التحميل مع بقاء الماء يفرغ الأمعاء دون جفاف أو نقص وزن."
                ),
                practicalStepsWithCheck = listOf(
                    "✓ طبّق 4–6 ساعات ظلام منتظمة يومياً بعد اليوم السابع مع تقليصها تدريجياً قبل البيع بـ 48 ساعة.",
                    "✓ التزم بفترة السحب الدوائي (Withdrawal Period) لأي علاج مسجل قبل التحميل.",
                    "✓ ارفع المعالف قبل التحميل بـ 8–10 ساعات واترك الماء متاحاً حتى ساعة الصيد."
                ),
                liveBarnComparisonText = "🏠 عنبرك الحالي [$barnName] (اليوم $safeAge): " +
                    "متوسط الوزن الحالي ${String.format(java.util.Locale.US, "%.2f", currentWeightKg)} $kgUnit • إجمالي الكتلة الحية التقديرية: ${String.format(java.util.Locale.US, "%,.0f", liveBirds * currentWeightKg)} $kgUnit.",
                posterSpec = Ross308PosterCatalog.forSubCard("care_4")
            )
        )

        // الفرع 2: 2. برنامج التحصينات 💉
        val branch1Cards = listOf(
            BookSubCardModel(
                id = "vax_1",
                branchIndex = 1,
                cardNumberInBranch = 1,
                iconEmoji = "💧",
                title = "استقبال الكتاكيت (اليوم 1 - 3): محلول الجفاف وفيتامين AD3E",
                badge1 = "💧 في ماء الشرب",
                badge2 = "⚡ تنشيط المناعة",
                scientificRulePoints = listOf(
                    "تعويض السوائل والأملاح المفقودة أثناء النقل عبر محلول الجفاف (Electrolytes) في الساعات الـ 6 الأولى.",
                    "تقديم فيتامينات AD3E + فيتامين C بجرعة قياسية لدعم الأغشية المخاطية وامتصاص كيس المح.",
                    "حرارة ماء الشرب المثالية للكتاكيت في الأيام الثلاثة الأولى: 18°C – 21°C."
                ),
                practicalStepsWithCheck = listOf(
                    "✓ اغسل خطوط المساقي قبل وصول الكتاكيت وتأكد من تدفق الحلمات بسهولة.",
                    "✓ قدّم محلول الجفاف والفيتامينات في ماء فاتر نظيف خالٍ من الرواسب.",
                    "✓ تجنب المضادات الحيوية العشوائية في اليوم الأول دون تشخيص بيطري."
                ),
                liveBarnComparisonText = "🏠 عنبرك الحالي [$barnName] (اليوم $safeAge • الكتاكيت المسكنة: ${String.format(java.util.Locale.US, "%,d", placedBirds)}): " +
                    if (safeAge <= 3) "أنت الآن في نافذة الاستقبال (اليوم $safeAge) — قدّم محلول الجفاف وAD3E." else "تم اجتياز مرحلة استقبال الأيام 1–3.",
                posterSpec = Ross308PosterCatalog.forSubCard("vax_1")
            ),
            BookSubCardModel(
                id = "vax_2",
                branchIndex = 1,
                cardNumberInBranch = 2,
                iconEmoji = "👁️",
                title = "التحصين الأول (اليوم 6 - 7): نيوكاسل Hitchner B1 + التهاب الشعب IB",
                badge1 = "👁️ تقطير / رش",
                badge2 = "🛡️ وقاية تنفسية",
                scientificRulePoints = listOf(
                    "بناء المناعة الموضعية الأولى في الجهاز التنفسي العلوي ضد النيوكاسل (Hitchner B1 / Clone 30) والتهاب الشعب (IB).",
                    "التقطير العيني أو الرش الخشن (قطرات 100–150 ميكرون) يضمن تغطية مناعية تفوق 95% من القطيع.",
                    "يُمنع تحصين أي قطيع يعاني من إجهاد حراري أو ضعف عام قبل استقرار حالته."
                ),
                practicalStepsWithCheck = listOf(
                    "✓ أوقف الشفاطات وخفّض الإضاءة أثناء الرش ولمدة 20 دقيقة بعده لضمان استنشاق اللقاح.",
                    "✓ استخدم ماءً مقطراً بارداً لإذابة اللقاح واحفظ الأمبولات في ثلج حتى لحظة الاستخدام.",
                    "✓ ادعم القطيع بفيتامين AD3E + C لمدة 24 ساعة بعد التحصين لتخفيف رد الفعل التنفسي."
                ),
                liveBarnComparisonText = "🏠 عنبرك الحالي [$barnName] (اليوم $safeAge): " +
                    when {
                        safeAge < 6 -> "موعد التحصين الأول بعد ${6 - safeAge} أيام (اليوم 6–7)."
                        safeAge in 6..7 -> "⚠️ اليوم ($safeAge) هو الموعد القياسي لتحصين النيوكاسل B1 + IB!"
                        else -> "تم تجاوز موعد التحصين الأول (اليوم 6–7)."
                    },
                posterSpec = Ross308PosterCatalog.forSubCard("vax_2")
            ),
            BookSubCardModel(
                id = "vax_3",
                branchIndex = 1,
                cardNumberInBranch = 3,
                iconEmoji = "🛡️",
                title = "تحصين الجمبورو (اليوم 12 - 14): لقاح IBD + رافع مناعة",
                badge1 = "💧 ماء خالٍ من الكلور",
                badge2 = "⏳ تعطيش ساعة",
                scientificRulePoints = listOf(
                    "تحصين الجمبورو (IBD Intermediate) يحمي غدة فابريشيوس والجهاز المناعي في الفترة الحرجة (اليوم 12–14).",
                    "الكلور والمطهرات تقتل اللقاح الحي فوراً؛ يجب إيقافها قبل 48 ساعة من التحصين.",
                    "كمية ماء التحصين تُحسب ليتم شربها بالكامل خلال 1.5 إلى ساعتين كحد أقصى."
                ),
                practicalStepsWithCheck = listOf(
                    "✓ عطّش الطيور لمدة 60 دقيقة فقط قبل التحصين (أقل في الأجواء الحارة).",
                    "✓ أضف حليباً خالي الدسم (2 غ/لتر) أو مثبت لقاح في الماء قبل فتح الأمبولات تحت سطح الماء.",
                    "✓ امشِ بهدوء داخل العنبر لتحريك الطيور نحو المساقي لضمان شرب 100% من القطيع."
                ),
                liveBarnComparisonText = "🏠 عنبرك الحالي [$barnName] (اليوم $safeAge): " +
                    when {
                        safeAge < 12 -> "موعد تحصين الجمبورو بعد ${12 - safeAge} يوم (اليوم 12–14)."
                        safeAge in 12..14 -> "⚠️ اليوم ($safeAge) هو الموعد القياسي لتحصين الجمبورو (IBD)!"
                        else -> "تم تجاوز نافذة تحصين الجمبورو (اليوم 12–14)."
                    },
                posterSpec = Ross308PosterCatalog.forSubCard("vax_3")
            ),
            BookSubCardModel(
                id = "vax_4",
                branchIndex = 1,
                cardNumberInBranch = 4,
                iconEmoji = "🧪",
                title = "الجرعة المعززة (اليوم 18 - 20): نيوكاسل LaSota / Clone 30 + مضاد سموم",
                badge1 = "💧 شرب صباحي",
                badge2 = "🧪 غسيل كلوي وكبدي",
                scientificRulePoints = listOf(
                    "الجرعة التنشيطية للنيوكاسل (LaSota أو Clone 30) في اليوم 18–20 ترفع المناعة لحماية القطيع حتى البيع.",
                    "مع زيادة استهلاك العلف النامي والناهي، يحتاج الكبد والكلى إلى مضاد سموم فطرية وغسيل كلوي.",
                    "يُجرى التحصين في الصباح الباكر حصراً لتفادي حرارة الظهيرة وإجهاد الطيور."
                ),
                practicalStepsWithCheck = listOf(
                    "✓ قدّم اللقاح في الصباح الباكر في ماء خالٍ من الكلور مع مثبت اللقاح.",
                    "✓ استخدم موسع شعب ومنشط مناعة بعد التحصين بـ 48 ساعة إذا ظهرت عطسة لقاح خفيفة.",
                    "✓ طبّق برنامج مضاد سموم وغسيل كلوي وكبدي لمدة 3 أيام متتالية بعد عمر 20 يوماً."
                ),
                liveBarnComparisonText = "🏠 عنبرك الحالي [$barnName] (اليوم $safeAge): " +
                    when {
                        safeAge < 18 -> "موعد الجرعة المعززة للنيوكاسل بعد ${18 - safeAge} يوم (اليوم 18–20)."
                        safeAge in 18..20 -> "⚠️ اليوم ($safeAge) هو الموعد القياسي للجرعة المعززة (LaSota / Clone 30)!"
                        else -> "مرحلة ما بعد التحصينات (اليوم $safeAge): ركّز على مضاد السموم والغسيل الكلوي والفيتامينات."
                    },
                posterSpec = Ross308PosterCatalog.forSubCard("vax_4")
            )
        )

        // الفرع 3: 3. الأمراض والوقاية 🩺
        val branch2Cards = listOf(
            BookSubCardModel(
                id = "dis_1",
                branchIndex = 2,
                cardNumberInBranch = 1,
                iconEmoji = "🫁",
                title = "مرض البرونشيت المعدية — Infectious Bronchitis (IB)",
                badge1 = "🚨 حشرجة وسعال وتنفس",
                badge2 = "🧬 تشخيص PCR & ELISA",
                scientificRulePoints = listOf(
                    "مرض فيروسي شديد العدوى يصيب الجيوب الأنفية والقصبة والشعب الهوائية والرئتين (نفوق 5–30% وتراجع وزن 10–20%).",
                    "يرفع معامل التحويل (FCR +0.10–0.30) ويمهّد لعدوى E.coli البكتيرية الثانوية وتجبن الشعب.",
                    "مناعة البرونشيت قصيرة الأمد وقد تحدث إصابة بعترات متحورة رغم التحصين."
                ),
                practicalStepsWithCheck = listOf(
                    "✓ قدّم رافع مناعة + AD3E + C وموسع شعب، ومضاداً حيوياً تحت إشراف بيطري للعدوى الثانوية.",
                    "✓ ارفع حرارة العنبر 1–2 درجة مئوية وحسّن التهوية الدنيا لطرد الأمونيا والرطوبة.",
                    "✓ التزم بالتحصين المبكر، الأمن الحيوي، وتطهير العنبر بين الدورات."
                ),
                liveBarnComparisonText = "🏠 فحص عنبرك [$barnName] (اليوم $safeAge): " +
                    "الحرارة المسجلة: $tempNowStr • التهوية الدنيا المطلوبة: ${String.format(java.util.Locale.US, "%,.0f", totalMinVentM3h)} m³/ساعة لحماية القصبة الهوائية.",
                posterSpec = Ross308PosterCatalog.forSubCard("dis_1")
            ),
            BookSubCardModel(
                id = "dis_2",
                branchIndex = 2,
                cardNumberInBranch = 2,
                iconEmoji = "😷",
                title = "الميكوبلازما والأمراض التنفسية المزمنة (CRD)",
                badge1 = "🚨 شخير ليلي وتورم عين",
                badge2 = "💨 أمونيا > 10 ppm",
                scientificRulePoints = listOf(
                    "التهاب مزمن يصيب القصبة والأكياس الهوائية ويسبب عتامة وتجبناً وتغطية القلب والكبد بغشاء فبريني مع E.coli.",
                    "المحفز الرئيسي لانتشار CRD هو ارتفاع غاز الأمونيا (> 10 ppm) والتذبذب الحراري الحاد بين الليل والنهار.",
                    "العلاج الدوائي لا ينجح أبداً بدون إصلاح التهوية وتجفيف الفرشة أولاً."
                ),
                practicalStepsWithCheck = listOf(
                    "✓ ارفع معدل التهوية الدنيا فوراً لطرد الأمونيا والغبار دون إسقاط تيار بارد مباشر.",
                    "✓ أضف موسعات شعب وزيوت عطرية (منثول / أوكالبتوس) مع المضاد النوعي بوصفة بيطرية.",
                    "✓ راقب تفاوت الأوزان الأسبوعي والتزم بفترة السحب الدوائي قبل البيع."
                ),
                liveBarnComparisonText = "🏠 فحص عنبرك [$barnName] (اليوم $safeAge): " +
                    "الحرارة المسجلة: $tempNowStr • التهوية الدنيا المطلوبة: ${String.format(java.util.Locale.US, "%,.0f", totalMinVentM3h)} m³/ساعة لمنع تراكم الأمونيا.",
                posterSpec = Ross308PosterCatalog.forSubCard("dis_2")
            ),
            BookSubCardModel(
                id = "dis_3",
                branchIndex = 2,
                cardNumberInBranch = 3,
                iconEmoji = "🦠",
                title = "الكوكسيديا والسموم الفطرية والإجهاد المعوي",
                badge1 = "🚨 زرق مدمم أو برتقالي",
                badge2 = "🪵 رطوبة الفرشة > 30%",
                scientificRulePoints = listOf(
                    "طفيلي الأيميريا يتكاثر في الفرشة الرطبة الدافئة ويدمر الأمعاء الدقيقة والأعورين مسبباً نزيفاً وضعف امتصاص.",
                    "يرفع معامل التحويل الغذائي (+0.18 إلى +0.35) ويمهد للإصابة بالتهاب الأمعاء النخري (كلوستريديا).",
                    "المطهرات العادية لا تقتل الحويصلات؛ الجفاف التام للفرشة وإصلاح تسريب الحلمات أساس الوقاية."
                ),
                practicalStepsWithCheck = listOf(
                    "✓ أصلح أي تسريب في حلمات المساقي فوراً وأزل البقع الرطبة واستبدلها بنشارة جافة.",
                    "✓ قدّم مضاد الكوكسيديا الموصوف مع فيتامين K3 وA لوقف النزيف المعوي وتجديد البطانة.",
                    "✓ ادعم الأمعاء بالبروبيوتيك ومضاد السموم الفطرية لمنع تكاثر الكلوستريديا."
                ),
                liveBarnComparisonText = "🏠 فحص عنبرك [$barnName] (اليوم $safeAge): " +
                    "معامل التحويل الحالي ${if (actualFcr > 0.0) String.format(java.util.Locale.US, "%.2f", actualFcr) else "قيد الحساب"} (معيار Ross 308: ${String.format(java.util.Locale.US, "%.2f", targetFcr)}) — أي ارتفاع مفاجئ في FCR يستوجب فحص الأمعاء والفرشة.",
                posterSpec = Ross308PosterCatalog.forSubCard("dis_3")
            ),
            BookSubCardModel(
                id = "dis_4",
                branchIndex = 2,
                cardNumberInBranch = 4,
                iconEmoji = "🚨",
                title = "النيوكاسل والجمبورو والإجهاد الحراري (طوارئ القطيع)",
                badge1 = "🚨 نفوق حاد والتواء رقبة",
                badge2 = "❄️ تبريد + حجر صحي",
                scientificRulePoints = listOf(
                    "النيوكاسل يضرب التنفس والأعصاب (التواء الرقبة وإسهال أخضر)، والجمبورو يدمر غدة فابريشيوس والمناعة.",
                    "تجاوز الحرارة 30°C يرفع استهلاك الماء 2.5–3 أضعاف ويسبب احتباساً وسكتة حرارية في الأوزان الكبيرة.",
                    "تصويم العلف وقت الذروة الحارة (11ص–4ع) يخفض الحرارة الأيضية الداخلية للطائر."
                ),
                practicalStepsWithCheck = listOf(
                    "✓ في الحر: ارفع العلف وقت الذروة وشغّل التبريد والشفاطات وقدّم ماءً بارداً + فيتامين C وإلكتروليت.",
                    "✓ في الاشتباه الفيروسي: اعزل العنبر ومنع انتقال العمال واستدعِ البيطري للفحص (PCR/HI).",
                    "✓ التزم بمواعيد التحصين والأمن الحيوي وغلق العنبر أمام الزوار والوسطاء."
                ),
                liveBarnComparisonText = "🏠 فحص عنبرك [$barnName] (اليوم $safeAge): " +
                    "حرارة العنبر المسجلة: $tempNowStr مقابل الهدف القياسي (${String.format(java.util.Locale.US, "%.0f°C", targetTempC)}) • الطيور الحية: ${String.format(java.util.Locale.US, "%,d", liveBirds)}.",
                posterSpec = Ross308PosterCatalog.forSubCard("dis_4")
            )
        )

        // الفرع 4: 4. برنامج العلف والأوزان 🌾
        val branch3Cards = listOf(
            BookSubCardModel(
                id = "feed_1",
                branchIndex = 3,
                cardNumberInBranch = 1,
                iconEmoji = "🌾",
                title = "المرحلة الأولى (اليوم 1 - 10): العلف البادئ Starter",
                badge1 = "🌾 بروتين 23%",
                badge2 = "⚡ طاقة 3000 kcal",
                scientificRulePoints = listOf(
                    "مواصفات العلف البادئ (Starter Days 1–10): بروتين خام 23% • طاقة ممثلة 3000 kcal/kg • قوام محبب ناعم (Crumble).",
                    "الاستهلاك التراكمي المستهدف في الأيام الـ 10 الأولى: ~250 إلى 290 غرام علف لكل كتكوت.",
                    "هدف الوزن في نهاية المرحلة (اليوم 10): ~300 إلى 320 غرام بمعامل تحويل FCR ~0.92."
                ),
                practicalStepsWithCheck = listOf(
                    "✓ وزّع العلف البادئ بكميات قليلة ومتكررة (4–5 مرات يومياً) للحفاظ على طزاجته ورائحته الشهية.",
                    "✓ انخل الناعم المتراكم في أطباق التحضين يومياً وتأكد من خلو العلف من التكتل والرطوبة.",
                    "✓ ابدأ خلط 25% علف نامي مع 75% بادئ في اليوم 9–10 للانتقال التدريجي السلس."
                ),
                liveBarnComparisonText = "🏠 تغذية [$barnName] (اليوم $safeAge): " +
                    "العلف المستهلك: ${Translations.formatQuintalNumber(totalFeedQuintals)} $qUnit • المخزون المتبقي: ${Translations.formatQuintalNumber(remainingFeedQuintals)} $qUnit.",
                posterSpec = Ross308PosterCatalog.forSubCard("feed_1")
            ),
            BookSubCardModel(
                id = "feed_2",
                branchIndex = 3,
                cardNumberInBranch = 2,
                iconEmoji = "🌿",
                title = "المرحلة الثانية (اليوم 11 - 21): العلف النامي Grower",
                badge1 = "🌾 بروتين 22%",
                badge2 = "⚡ طاقة 3100 kcal",
                scientificRulePoints = listOf(
                    "مواصفات العلف النامي (Grower Days 11–21): بروتين خام 21.5%–22% • طاقة ممثلة 3100 kcal/kg.",
                    "الاستهلاك التراكمي المستهدف خلال مرحلة النامي: ~1.00 كغ علف لكل طائر.",
                    "هدف الوزن في اليوم 14: 520 غ (FCR 1.08) • وفي اليوم 21: 1,000 غ / 1.0 كغ (FCR 1.25)."
                ),
                practicalStepsWithCheck = listOf(
                    "✓ انتقل تدريجياً من المحبب الناعم (Crumble) إلى الكبسولات الصغيرة (Mini-pellets 2–2.5 ملم).",
                    "✓ اضبط ارتفاع المعلف بمستوى ظهر الطائر بحيث يغطي العلف ثلث قاع الطبق فقط لمنع الهدر.",
                    "✓ اوزن عينة عشوائية (100 طائر) في اليوم 14 واليوم 21 لمطابقة منحنى Ross 308."
                ),
                liveBarnComparisonText = "🏠 تغذية [$barnName] (اليوم $safeAge): " +
                    "الوزن الحالي ${String.format(java.util.Locale.US, "%.2f", currentWeightKg)} $kgUnit مقابل معيار Ross 308 (${String.format(java.util.Locale.US, "%.2f", targetWeightKg)} $kgUnit).",
                posterSpec = Ross308PosterCatalog.forSubCard("feed_2")
            ),
            BookSubCardModel(
                id = "feed_3",
                branchIndex = 3,
                cardNumberInBranch = 3,
                iconEmoji = "🌽",
                title = "تغيير العلف التدريجي (3 أيام: 75/25 ➔ 50/50 ➔ 25/75) ومضاد السموم",
                badge1 = "🔄 خلط تدريجي 3 أيام",
                badge2 = "🧪 مضاد سموم + قنطار",
                scientificRulePoints = listOf(
                    "قاعدة الخلط التدريجي عند الانتقال بين مراحل العلف (3 أيام): اليوم 1 (75% قديم + 25% جديد) ➔ اليوم 2 (50% + 50%) ➔ اليوم 3 (25% قديم + 75% جديد).",
                    "تمثل مرحلة الناهي (20.5% بروتين • 3200 kcal • 3.5mm) أكثر من 65% من إجمالي تكلفة العلف للدورة.",
                    "إضافة مضاد السموم الفطرية (Mycotoxin Binder) وتخزين الأكياس على طبالي جافة بالقنطار (1 ق = 100 كغ) يحمي الكبد ويضبط FCR."
                ),
                practicalStepsWithCheck = listOf(
                    "✓ طبّق الخلط التدريجي 3 أيام (75/25 ثم 50/50 ثم 25/75) دون تبديل مفاجئ يحمي الأمعاء من الإسهال.",
                    "✓ قدّم كورس مضاد سموم فطرية عند كل نقلة علف وراقب متانة حبة العلف (Pellets) وخلوها من البودرة.",
                    "✓ راقب المخزون المتبقي في العنبر بالقنطار (ق) يومياً لتأمين طلبيات العلف قبل نفادها بـ 3 أيام."
                ),
                liveBarnComparisonText = "🏠 تغذية [$barnName] (اليوم $safeAge): " +
                    "معامل التحويل الحالي ${if (actualFcr > 0.0) String.format(java.util.Locale.US, "%.2f", actualFcr) else "قيد الحساب"} (الهدف: ${String.format(java.util.Locale.US, "%.2f", targetFcr)}) • المخزون المتبقي: ${Translations.formatQuintalNumber(remainingFeedQuintals)} $qUnit.",
                posterSpec = Ross308PosterCatalog.forSubCard("feed_3")
            ),
            BookSubCardModel(
                id = "feed_4",
                branchIndex = 3,
                cardNumberInBranch = 4,
                iconEmoji = "⚖️",
                title = "جدول الأوزان واستهلاك العلف وميزان الماء (1 إلى 45 يوم)",
                badge1 = "⚖️ حتى 2800 غ",
                badge2 = "💧 الماء 1.7x العلف",
                scientificRulePoints = listOf(
                    "المرجع القياسي للأوزان: يوم 7 (190 غ) • يوم 14 (520 غ) • يوم 21 (1000 غ) • يوم 28 (1580 غ) • يوم 35 (2250 غ) • يوم 42–45 (2800–2950 غ).",
                    "قاعدة ميزان الماء والعلف: يستهلك الطائر 1.6 إلى 1.8 لتر ماء لكل 1 كغ علف (بمتوسط 1.7x في حرارة 21°C).",
                    "أي انخفاض مفاجئ في استهلاك الماء اليومي عن 1.6x هو أول إنذار مبكر للمرض قبل ظهور النافق بـ 24 ساعة."
                ),
                practicalStepsWithCheck = listOf(
                    "✓ سجّل استهلاك العلف بالقنطار (1 قنطار = 100 كغ) واستهلاك الماء باللتر يومياً في السجل اليومي.",
                    "✓ قارن وزن قطيعك الفعلي من جدول الميزان السريع مع الوزن القياسي لليوم المقابل.",
                    "✓ افحص عداد المياه وفلاتر الخطوط كل صباح للتأكد من عدم انسداد الحلمات."
                ),
                liveBarnComparisonText = "🏠 ميزان الماء والعلف في [$barnName] (اليوم $safeAge): " +
                    if (waterToFeedRatio > 0.0) {
                        "نسبة الماء/العلف المسجلة: ${String.format(java.util.Locale.US, "%.2f", waterToFeedRatio)} لتر/كغ (المعيار القياسي: 1.6 – 1.8 لتر/كغ)."
                    } else {
                        "الوزن الحالي ${String.format(java.util.Locale.US, "%.2f", currentWeightKg)} $kgUnit • المعيار القياسي للماء 1.7 لتر لكل 1 كغ علف."
                    },
                posterSpec = Ross308PosterCatalog.forSubCard("feed_4")
            )
        )

        return listOf(
            BookBranchModel(
                branchIndex = 0,
                tag = "book_branch_0",
                title = "1. برنامج الرعاية 🐣",
                shortName = "برنامج الرعاية",
                subtitle = "(الحرارة، التهوية، ومراحل النمو)",
                iconEmoji = "🐣",
                subCards = branch0Cards
            ),
            BookBranchModel(
                branchIndex = 1,
                tag = "book_branch_1",
                title = "2. برنامج التحصينات 💉",
                shortName = "برنامج التحصينات",
                subtitle = "(جدول اللقاحات والفيتامينات)",
                iconEmoji = "💉",
                subCards = branch1Cards
            ),
            BookBranchModel(
                branchIndex = 2,
                tag = "book_branch_2",
                title = "3. الأمراض والوقاية 🩺",
                shortName = "الأمراض والوقاية",
                subtitle = "(الأعراض والأمن الحيوي)",
                iconEmoji = "🩺",
                subCards = branch2Cards
            ),
            BookBranchModel(
                branchIndex = 3,
                tag = "book_branch_3",
                title = "4. برنامج العلف والأوزان 🌾",
                shortName = "برنامج العلف والأوزان",
                subtitle = "(مراحل التغذية والتحويل)",
                iconEmoji = "🌾",
                subCards = branch3Cards
            )
        )
    }

    fun findSubCard(
        branches: List<BookBranchModel>,
        subCardId: String?
    ): Pair<BookBranchModel, BookSubCardModel>? {
        if (subCardId.isNullOrBlank()) return null
        for (branch in branches) {
            val found = branch.subCards.firstOrNull { it.id == subCardId }
            if (found != null) return branch to found
        }
        return null
    }

    fun getNextSubCardId(branches: List<BookBranchModel>, currentSubCardId: String): String {
        val pair = findSubCard(branches, currentSubCardId) ?: return currentSubCardId
        val cards = pair.first.subCards
        val idx = cards.indexOfFirst { it.id == currentSubCardId }
        if (idx < 0 || cards.isEmpty()) return currentSubCardId
        return cards[(idx + 1) % cards.size].id
    }

    fun getPreviousSubCardId(branches: List<BookBranchModel>, currentSubCardId: String): String {
        val pair = findSubCard(branches, currentSubCardId) ?: return currentSubCardId
        val cards = pair.first.subCards
        val idx = cards.indexOfFirst { it.id == currentSubCardId }
        if (idx < 0 || cards.isEmpty()) return currentSubCardId
        return cards[(idx - 1 + cards.size) % cards.size].id
    }

    fun buildSixteenBookPages(
        branches: List<BookBranchModel>
    ): List<Pair<BookBranchModel, BookSubCardModel>> {
        return branches.flatMap { branch ->
            branch.subCards.map { subCard -> branch to subCard }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun OwnerGlobalAiVetPlaceholderScreen(
    currentTheme: AppThemeOption,
    lang: AppLanguage = AppLanguage.AR
) {
    val sessionState = LocalAgritechAppState.current
    val appearance = LocalAppAppearanceState.current
    val isDark = currentTheme.isDark
    val aiAccent = appearance.accentColor
    val aiPrimaryBtn = appearance.accentOption.primaryColor
    val aiAccentContainer = appearance.accentContainerColor
    val aiCardShape = RoundedCornerShape(appearance.cornerRadiusDp.dp)
    val pillShape = RoundedCornerShape((appearance.cornerRadiusDp - 4).coerceAtLeast(8).dp)
    val primaryText = if (isDark) Color(0xFFFFFFFF) else currentTheme.headingColor
    val secondaryText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    val glassBorderColor = if (isDark) Color.White.copy(alpha = 0.22f) else aiAccent.copy(alpha = 0.35f)
    val coroutineScope = rememberCoroutineScope()

    var isHomePersonalAgentMode by remember(sessionState.currentRole) {
        mutableStateOf(
            sessionState.currentRole == AppRole.HOME_PERSONAL ||
                sessionState.currentRole == AppRole.WORKER
        )
    }
    var selectedAiSubTab by rememberSaveable { mutableStateOf(0) }
    var card1Approved by remember(isHomePersonalAgentMode) { mutableStateOf(false) }
    var card1Ignored by remember(isHomePersonalAgentMode) { mutableStateOf(false) }
    var card2Approved by remember(isHomePersonalAgentMode) { mutableStateOf(false) }
    var card2Ignored by remember(isHomePersonalAgentMode) { mutableStateOf(false) }
    var selectedGoalBubbleIdx by remember { mutableStateOf(0) }
    var selectedQuickChipIdx by remember { mutableStateOf(-1) }

    val aiSubTabs = remember(lang) {
        listOf(
            0 to Translations.tr("ai_subtab_smart_decisions", lang),
            1 to Translations.tr("ai_subtab_diagnostics", lang),
            2 to Translations.tr("ai_subtab_live_chat", lang)
        )
    }

    val goalBubbles = remember(lang) {
        listOf(
            Translations.tr("ai_agent_bubble_profit", lang),
            Translations.tr("ai_agent_bubble_health", lang),
            Translations.tr("ai_agent_bubble_market", lang),
            Translations.tr("ai_agent_bubble_bills", lang)
        )
    }

    val quickChips = remember(lang) {
        listOf(
            Translations.tr("ai_vet_chip_0", lang),
            Translations.tr("ai_vet_chip_1", lang),
            Translations.tr("ai_vet_chip_2", lang),
            Translations.tr("ai_vet_chip_3", lang)
        )
    }

    var promptInput by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var diagnosisHistory by remember(lang) {
        mutableStateOf(
            listOf(
                Pair(
                    Translations.tr("ai_vet_welcome_title", lang).replace("**", ""),
                    Translations.tr("ai_vet_welcome_body", lang).replace("**", "")
                )
            )
        )
    }

    val curr = Translations.tr("currency", lang)
    val qUnit = Translations.tr("quintal_short", lang)
    val kgUnit = Translations.tr("kg_unit", lang)

    val availableAiBarns = AgritechMockDb.farms.filterNot {
        it.id in AgritechMockDb.deletedFarmIds || AgritechFirestoreRepository.isUnassignedFarm(it.id)
    }
    var selectedAiBarnId by rememberSaveable(sessionState.activeFarmId, availableAiBarns.size) {
        mutableStateOf(
            availableAiBarns.firstOrNull { it.id == sessionState.activeFarmId }?.id
                ?: availableAiBarns.firstOrNull()?.id.orEmpty()
        )
    }
    val selectedAiFarm = availableAiBarns.firstOrNull { it.id == selectedAiBarnId }
        ?: availableAiBarns.firstOrNull()

    fun buildLiveDiagnosticReport(cardTag: String, cardTitle: String): Pair<String, String>? {
        val activeFarm = selectedAiFarm

        val activeCycle = activeFarm?.let { farm ->
            AgritechMockDb.resolveCycleForFarm(farm)
        }
        val farmReports = if (activeFarm != null) {
            AgritechMockDb.resolveReportsForFarmAndCycle(activeFarm.id, activeCycle?.id)
        } else emptyList()

        val farmTxs = if (activeFarm != null) {
            AgritechMockDb.transactions.filter {
                it.farmId == activeFarm.id &&
                    (activeCycle == null || it.cycleId == activeCycle.id) &&
                    it.status.equals("APPROVED", ignoreCase = true) &&
                    (it.currency.isBlank() ||
                        it.currency.equals(activeFarm.currencyCode, ignoreCase = true) ||
                        it.currency == curr)
            }
        } else emptyList()

        val barnDisplayName = if (activeFarm != null) {
            Translations.getLocalizedFarmName(activeFarm.id, lang, activeFarm.name)
        } else {
            Translations.tr("no_active_cycle", lang)
        }
        val maxReportDay = farmReports.maxOfOrNull { it.dayNumber } ?: 1
        val ageDays = if (activeCycle != null) {
            PoultryCalculators.calculateCycleDay(activeCycle.startDate, maxReportDay)
        } else maxReportDay
        val totalMortality = farmReports.sumOf { it.mortalityCount }
        val placedBirds = activeCycle?.totalBirdsPlaced ?: activeFarm?.capacity ?: 0
        val soldBirds = activeCycle?.cumulativeBirdsSold ?: 0
        val liveBirds = (placedBirds - totalMortality - soldBirds).coerceAtLeast(0)
        val totalFeedKg = farmReports.sumOf { it.feedIntakeKg }
        val totalFeedQuintals = totalFeedKg / 100.0
        val latestMeasuredWeightKg = farmReports.lastOrNull { it.actualAvgWeightKg > 0.0 }?.actualAvgWeightKg ?: 0.0
        val rossTargetWeightKg = PoultryCalculators.getRoss308StandardWeightKg(ageDays)
        val currentWeightKg = if (latestMeasuredWeightKg > 0.0) latestMeasuredWeightKg else rossTargetWeightKg

        return when (cardTag) {
            "ai_agent_goal_bubble_0" -> {
                if (isHomePersonalAgentMode) {
                    val entries = AgritechMockDb.workerPersonalEntries
                    val totalInc = entries.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                    val totalExp = entries.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                    val net = totalInc - totalExp
                    val savingsPct = if (totalInc > 0.0) ((net / totalInc) * 100.0).coerceIn(-100.0, 100.0) else 0.0
                    Pair(
                        "📊 ${Translations.tr("ai_vet_diagnosis_prefix", lang)} ($cardTitle)",
                        "• إجمالي الدخل المسجل: ${String.format(java.util.Locale.US, "%,.0f", totalInc)} $curr\n" +
                            "• إجمالي المصاريف والفواتير: ${String.format(java.util.Locale.US, "%,.0f", totalExp)} $curr\n" +
                            "• صافي الرصيد المتبقي: ${String.format(java.util.Locale.US, "%,.0f", net)} $curr\n" +
                            "• نسبة التوفير الفعلية: ${String.format(java.util.Locale.US, "%.1f%%", savingsPct)}\n" +
                            "💡 توصية الموظف الذكي: خصص 15% من الدخل الشهري في صندوق الطوارئ وتجنب المصاريف غير المجدولة."
                    )
                } else {
                    val totalExpenses = farmTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                    val totalIncome = farmTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                    val estBiomassKg = liveBirds * currentWeightKg
                    val costPerBird = if (liveBirds > 0) totalExpenses / liveBirds else 0.0
                    val costPerKg = if (estBiomassKg > 0.0) totalExpenses / estBiomassKg else 0.0
                    val estMarketRevenue = estBiomassKg * 250.0
                    val estNetProfit = (estMarketRevenue + totalIncome) - totalExpenses
                    Pair(
                        "💰 ${Translations.tr("ai_vet_diagnosis_prefix", lang)} ($cardTitle — $barnDisplayName)",
                        "• العنبر النشط: $barnDisplayName (العمر: اليوم $ageDays • الطيور الحية: ${String.format(java.util.Locale.US, "%,d", liveBirds)})\n" +
                            "• إجمالي المصاريف المسجلة بالخزنة: ${String.format(java.util.Locale.US, "%,.0f", totalExpenses)} $curr\n" +
                            "• تكلفة الطائر الحي حتى اليوم: ${String.format(java.util.Locale.US, "%,.1f", costPerBird)} $curr\n" +
                            "• تكلفة إنتاج 1 $kgUnit حي: ${String.format(java.util.Locale.US, "%,.1f", costPerKg)} $curr / $kgUnit\n" +
                            "• صافي الربح التقديري (بسعر 250 $curr/$kgUnit): ${String.format(java.util.Locale.US, "%+,.0f", estNetProfit)} $curr\n" +
                            "💡 التوصية المالية: تكلفة الكيلو (${String.format(java.util.Locale.US, "%.0f", costPerKg)} $curr) ${if (costPerKg <= 215.0) "ممتازة وتمنح هامش ربح آمن" else "مرتفعة نسبياً، راقب هدر العلف لرفع هامش الربح"}."
                    )
                }
            }
            "ai_vet_quick_chip_2" -> {
                val standingBiomassKg = liveBirds * currentWeightKg
                val soldWeightKg = activeCycle?.cumulativeSoldWeightKg ?: 0.0
                val totalProducedWeightKg = standingBiomassKg + soldWeightKg
                val actualFcr = if (totalProducedWeightKg > 0.0 && totalFeedKg > 0.0) {
                    totalFeedKg / totalProducedWeightKg
                } else 0.0
                val standardFcr = when {
                    ageDays <= 14 -> 1.18
                    ageDays <= 28 -> 1.42
                    ageDays <= 35 -> 1.56
                    else -> 1.68
                }
                Pair(
                    "⚖️ ${Translations.tr("ai_vet_diagnosis_prefix", lang)} ($cardTitle — $barnDisplayName)",
                    "• العنبر: $barnDisplayName (اليوم $ageDays • الطيور الحية: ${String.format(java.util.Locale.US, "%,d", liveBirds)})\n" +
                        "• إجمالي العلف المستهلك: ${Translations.formatQuintalNumber(totalFeedQuintals)} $qUnit (${String.format(java.util.Locale.US, "%,.0f", totalFeedKg)} $kgUnit)\n" +
                        "• متوسط الوزن الحالي للطير: ${String.format(java.util.Locale.US, "%.2f", currentWeightKg)} $kgUnit (معيار Ross 308 لليوم $ageDays: ${String.format(java.util.Locale.US, "%.2f", rossTargetWeightKg)} $kgUnit)\n" +
                        "• معامل التحويل المحسوب (FCR): ${String.format(java.util.Locale.US, "%.2f", actualFcr)} (المعيار القياسي: ${String.format(java.util.Locale.US, "%.2f", standardFcr)})\n" +
                        "💡 التوصية الفنية: ${if (actualFcr in 0.1..standardFcr) "معامل التحويل ممتاز وأفضل من المعيار العالمي، حافظ على ضبط ارتفاع المعالف." else "يرجى فحص مستوى المعالف لمنع بعثرة العلف وضبط حرارة العنبر لتحسين التحويل."}"
                )
            }
            "ai_agent_goal_bubble_3" -> {
                if (isHomePersonalAgentMode) {
                    val bills = AgritechMockDb.workerPersonalEntries.filter { it.type == TransactionType.EXPENSE }
                    val totalBills = bills.sumOf { it.amount }
                    val topBill = bills.maxByOrNull { it.amount }
                    val latestThree = bills.take(3).joinToString("\n") {
                        "  - ${it.title}: ${String.format(java.util.Locale.US, "%,.0f", it.amount)} $curr (${it.dateLabel})"
                    }
                    Pair(
                        "📋 ${Translations.tr("ai_vet_diagnosis_prefix", lang)} ($cardTitle)",
                        "• عدد الفواتير والمصاريف المسجلة: ${bills.size} فاتورة/قيد\n" +
                            "• إجمالي المصاريف: ${String.format(java.util.Locale.US, "%,.0f", totalBills)} $curr\n" +
                            "• أكبر بند استهلاكاً للميزانية: ${topBill?.title ?: "-"} (${String.format(java.util.Locale.US, "%,.0f", topBill?.amount ?: 0.0)} $curr)\n" +
                            "• أحدث الفواتير المسجلة:\n$latestThree"
                    )
                } else {
                    val expenseTxs = farmTxs.filter { it.type == TransactionType.EXPENSE }
                    val totalExp = expenseTxs.sumOf { it.amount }
                    val topExp = expenseTxs.maxByOrNull { it.amount }
                    val latestList = expenseTxs.takeLast(3).reversed().joinToString("\n") {
                        "  - ${Translations.getLocalizedTxCategory(it.category, lang)}: ${String.format(java.util.Locale.US, "%,.0f", it.amount)} $curr (${it.date})"
                    }.ifBlank { "  - لا توجد فواتير مسجلة" }
                    Pair(
                        "📋 ${Translations.tr("ai_vet_diagnosis_prefix", lang)} ($cardTitle — $barnDisplayName)",
                        "• إجمالي مصاريف الخزنة ($barnDisplayName): ${String.format(java.util.Locale.US, "%,.0f", totalExp)} $curr\n" +
                            "• أكبر بند استهلاكاً للميزانية: ${topExp?.let { Translations.getLocalizedTxCategory(it.category, lang) } ?: "-"} (${String.format(java.util.Locale.US, "%,.0f", topExp?.amount ?: 0.0)} $curr)\n" +
                            "• آخر الفواتير المسجلة:\n$latestList"
                    )
                }
            }
            "ai_vet_quick_chip_0" -> {
                val latestReport = farmReports.maxByOrNull { it.dayNumber }
                val recordedTempC: Double? = latestReport?.temperature
                val idealTempC = Ross308BroilerHandbook.getStandardTempForDay(ageDays)
                val tempSummaryLine = if (recordedTempC != null) {
                    val tempDiff = recordedTempC - idealTempC
                    val ventStatus = latestReport?.let { Translations.getLocalizedVentilation(it.ventilationStatus, lang) }
                        ?: Translations.tr("vent_good", lang)
                    "• آخر درجة حرارة مسجلة بالسجل: ${String.format(java.util.Locale.US, "%.1f°C", recordedTempC)} (الهدف المثالي لليوم $ageDays: ${String.format(java.util.Locale.US, "%.1f°C", idealTempC)})\n" +
                        "• حالة التهوية المسجلة: $ventStatus (الفارق الحراري: ${String.format(java.util.Locale.US, "%+.1f°C", tempDiff)})\n" +
                        "💡 التوصية البيئية: ${
                            when {
                                tempDiff > 2.0 -> "تنبيه إجهاد حراري (+${String.format(java.util.Locale.US, "%.1f°C", tempDiff)} فوق الهدف): ارفع سرعة الشفاطات تدريجياً وأضف فيتامين C وإلكتروليت في مياه الشرب."
                                tempDiff < -2.0 -> "الحرارة أقل من الهدف المثالي: اضبط التدفئة وتأكد من عدم وجود تيارات هوائية باردة لمنع تجمع الطيور."
                                else -> "درجة الحرارة والتهوية مثالية تماماً لعمر $ageDays يوم وفق دليل Ross 308."
                            }
                        }"
                } else {
                    "• آخر درجة حرارة مسجلة بالسجل: غير مسجلة (لا توجد قراءة بعد — الهدف المثالي لليوم $ageDays: ${String.format(java.util.Locale.US, "%.1f°C", idealTempC)})\n" +
                        "💡 التوصية البيئية: لم يتم إدخال تقرير يومي لهذا العنبر بعد؛ يرجى تسجيل حرارة العنبر في السجل اليومي للحصول على تقييم دقيق."
                }
                Pair(
                    "🌡️ ${Translations.tr("ai_vet_diagnosis_prefix", lang)} ($cardTitle — $barnDisplayName)",
                    "• العنبر: $barnDisplayName (العمر الحالي: اليوم $ageDays)\n$tempSummaryLine"
                )
            }
            else -> null
        }
    }

    fun triggerAiConsultation(queryText: String, switchToChat: Boolean = false, diagnosticTag: String? = null) {
        val cleanQuery = queryText.trim().replace("**", "")
        if (cleanQuery.isEmpty() || isAnalyzing) return
        if (switchToChat) {
            selectedAiSubTab = 2
        }
        val instantReport = diagnosticTag?.let { buildLiveDiagnosticReport(it, cleanQuery) }
        if (instantReport != null) {
            diagnosisHistory = diagnosisHistory + instantReport
            return
        }
        isAnalyzing = true
        diagnosisHistory = diagnosisHistory + Pair(
            "${Translations.tr("ai_vet_user_query_prefix", lang)}: $cleanQuery",
            Translations.tr("ai_vet_analyzing_msg", lang).replace("**", "")
        )
        coroutineScope.launch {
            val activeFarm = selectedAiFarm
            val activeCycle = activeFarm?.let { f ->
                AgritechMockDb.resolveCycleForFarm(f)
            }
            val activeReports = if (activeFarm != null && activeCycle != null) {
                AgritechMockDb.resolveReportsForFarmAndCycle(activeFarm.id, activeCycle.id, emptyList())
            } else {
                emptyList()
            }
            val barnDisplayName: String? = activeFarm?.let { Translations.getLocalizedFarmName(it.id, lang, it.name) }
            val realAgeDays: Int? = if (activeCycle != null) {
                val maxDay = activeReports.maxOfOrNull { it.dayNumber } ?: 1
                PoultryCalculators.calculateCycleDay(activeCycle.startDate, maxDay)
            } else {
                null
            }
            val realTotalMortality = activeReports.sumOf { it.mortalityCount }
            val realLiveBirds: Int? = if (activeCycle != null) {
                PoultryCalculators.calculateLiveBirds(
                    totalBirdsPlaced = activeCycle.totalBirdsPlaced,
                    totalMortality = realTotalMortality,
                    cumulativeBirdsSold = activeCycle.cumulativeBirdsSold
                )
            } else {
                null
            }
            val realMortalityPct: Double? = if (activeCycle != null && activeCycle.totalBirdsPlaced > 0) {
                (realTotalMortality.toDouble() / activeCycle.totalBirdsPlaced) * 100.0
            } else {
                null
            }
            val latestRecordedTempC: Double? = activeReports.maxByOrNull { it.dayNumber }?.temperature
            val remainingFeedKg: Double? = activeCycle?.feedInventoryKg?.takeIf { it > 0.0 || activeReports.isNotEmpty() }

            val barnContext = com.example.utils.BarnAiContext(
                farmName = barnDisplayName,
                cycleAgeDays = realAgeDays,
                liveBirdsCount = realLiveBirds,
                latestTemperatureC = latestRecordedTempC,
                cumulativeMortalityPct = realMortalityPct,
                feedInventoryKg = remainingFeedKg
            )

            val reply = try {
                AiVetGeminiService.generateVetResponse(
                    question = cleanQuery,
                    lang = lang,
                    flockAgeDays = realAgeDays,
                    liveBirds = realLiveBirds,
                    mortalityPct = realMortalityPct,
                    currentTempC = latestRecordedTempC,
                    barnName = barnDisplayName.orEmpty(),
                    barnContext = barnContext
                ).replace("**", "")
            } catch (e: Exception) {
                "${AiVetGeminiService.fallbackNoticeFor(lang)}\n" +
                    Translations.tr("ai_vet_fallback_reply", lang).replace("**", "")
            }
            diagnosisHistory = diagnosisHistory.dropLast(1) + Pair(
                "${Translations.tr("ai_vet_diagnosis_prefix", lang)} ($cleanQuery)",
                reply.replace("**", "")
            )
            isAnalyzing = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .darwinPaperBackground(currentTheme)
            .testTag("global_ai_vet_screen")
    ) {
        // Glassmorphic Radial Light Glow behind AI Agent Cards
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            aiAccent.copy(alpha = if (isDark) 0.20f else 0.14f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // الجراحة 1: شريط علوي موحد ونظيف في جميع الأقسام الثلاثة داخل شاشة 📖 الكتاب:
            // شريحة اختيار العنبر (🏠 العنبر: [اسم العنبر النشط]) + شريط التبويبات الثلاثة (📊 التحليلات | 💡 نصائح | 💬 المحادثة)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .darwinManuscriptBorder(cornerRadius = appearance.cornerRadiusDp.dp, isDark = isDark)
                    .testTag("ai_vet_interactive_header_card"),
                shape = aiCardShape,
                colors = CardDefaults.cardColors(
                    containerColor = currentTheme.cardColor
                ),
                border = BorderStroke(1.2.dp, glassBorderColor),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (appearance.paperStyle == AppPaperStyleOption.FROSTED_GLASS) 0.dp else 3.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // شريحة اختيار العنبر النشط: 🏠 العنبر: [اسم العنبر النشط]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .testTag("ai_vet_selected_barn_bar"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = Translations.tr("book_barn_selector_label", lang),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = secondaryText
                        )
                        if (availableAiBarns.isEmpty()) {
                            Surface(
                                shape = pillShape,
                                color = Color.Transparent,
                                border = BorderStroke(1.dp, glassBorderColor)
                            ) {
                                Text(
                                    text = Translations.tr("no_active_cycle", lang),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = secondaryText,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            availableAiBarns.forEach { barnItem ->
                                val isSelectedBarn = selectedAiFarm?.id == barnItem.id
                                val localizedBarnTitle = Translations.getLocalizedFarmName(barnItem.id, lang, barnItem.name)
                                Surface(
                                    modifier = Modifier
                                        .clip(pillShape)
                                        .clickable {
                                            selectedAiBarnId = barnItem.id
                                            sessionState.activeFarmId = barnItem.id
                                        }
                                        .testTag("ai_vet_select_barn_${barnItem.id}"),
                                    shape = pillShape,
                                    color = if (isSelectedBarn) aiAccentContainer else Color.Transparent,
                                    border = BorderStroke(
                                        width = if (isSelectedBarn) 1.5.dp else 1.dp,
                                        color = if (isSelectedBarn) aiAccent else glassBorderColor
                                    )
                                ) {
                                    Text(
                                        text = localizedBarnTitle,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelectedBarn) aiAccent else secondaryText,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // شريط التبويبات الثلاثة ثلاثي الأبعاد: 📊 التحليلات | 💡 نصائح | 💬 المحادثة
                    NeumorphicRecessedDisplay(
                        isDark = isDark,
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_segmented_subtabs_row"),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            aiSubTabs.forEach { (tabIdx, tabTitle) ->
                                val isSelectedTab = selectedAiSubTab == tabIdx
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(pillShape)
                                        .clickable { selectedAiSubTab = tabIdx }
                                        .testTag("ai_subtab_$tabIdx"),
                                    shape = pillShape,
                                    color = if (isSelectedTab) aiPrimaryBtn else Color.Transparent,
                                    shadowElevation = if (isSelectedTab) 4.dp else 0.dp
                                ) {
                                    Text(
                                        text = tabTitle,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelectedTab) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isSelectedTab) Color.White else secondaryText,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // حساب المؤشرات الحية للعنبر النشط لعرضها في (📊 التحليلات) و(💡 نصائح كتاب Ross 308)
            val activeCycleForBook = selectedAiFarm?.let { AgritechMockDb.resolveCycleForFarm(it) }
            val activeReportsForBook = if (selectedAiFarm != null) {
                AgritechMockDb.resolveReportsForFarmAndCycle(selectedAiFarm.id, activeCycleForBook?.id)
            } else emptyList()
            val activeTxsForBook = if (selectedAiFarm != null) {
                AgritechMockDb.transactions.filter {
                    it.farmId == selectedAiFarm.id &&
                        (activeCycleForBook == null || it.cycleId == activeCycleForBook.id) &&
                        it.status.equals("APPROVED", ignoreCase = true) &&
                        (it.currency.isBlank() ||
                            it.currency.equals(selectedAiFarm.currencyCode, ignoreCase = true) ||
                            it.currency == curr)
                }
            } else emptyList()

            val bookBarnName = if (selectedAiFarm != null) {
                Translations.getLocalizedFarmName(selectedAiFarm.id, lang, selectedAiFarm.name)
            } else {
                Translations.tr("no_active_cycle", lang)
            }
            val bookMaxReportDay = activeReportsForBook.maxOfOrNull { it.dayNumber } ?: 1
            val bookAgeDays = if (activeCycleForBook != null) {
                PoultryCalculators.calculateCycleDay(activeCycleForBook.startDate, bookMaxReportDay)
            } else bookMaxReportDay
            val bookTotalMortality = activeReportsForBook.sumOf { it.mortalityCount }
            val bookPlacedBirds = activeCycleForBook?.totalBirdsPlaced ?: selectedAiFarm?.capacity ?: 0
            val bookSoldBirds = activeCycleForBook?.cumulativeBirdsSold ?: 0
            val bookLiveBirds = (bookPlacedBirds - bookTotalMortality - bookSoldBirds).coerceAtLeast(0)
            val bookMortalityPct = if (bookPlacedBirds > 0) (bookTotalMortality.toDouble() / bookPlacedBirds) * 100.0 else 0.0
            val bookLivabilityPct = (100.0 - bookMortalityPct).coerceIn(0.0, 100.0)
            val bookTotalFeedKg = activeReportsForBook.sumOf { it.feedIntakeKg }
            val bookTotalFeedQuintals = bookTotalFeedKg / 100.0
            val bookRemainingFeedKg = activeCycleForBook?.feedInventoryKg ?: 0.0
            val bookRemainingFeedQuintals = bookRemainingFeedKg / 100.0
            val bookLatestReport = activeReportsForBook.maxByOrNull { it.dayNumber }
            val bookLatestTempC: Double? = bookLatestReport?.temperature
            val bookLatestFeedKg = bookLatestReport?.feedIntakeKg ?: 0.0
            val bookLatestWaterL = bookLatestReport?.waterIntakeLiters ?: 0.0
            val bookLatestWeightKg = activeReportsForBook.lastOrNull { it.actualAvgWeightKg > 0.0 }?.actualAvgWeightKg ?: 0.0
            val bookTargetWeightKg = PoultryCalculators.getRoss308StandardWeightKg(bookAgeDays)
            val bookCurrentWeightKg = if (bookLatestWeightKg > 0.0) bookLatestWeightKg else bookTargetWeightKg
            val bookStandingBiomassKg = bookLiveBirds * bookCurrentWeightKg
            val bookTotalProducedWeightKg = bookStandingBiomassKg + (activeCycleForBook?.cumulativeSoldWeightKg ?: 0.0)
            val bookActualFcr = if (bookTotalProducedWeightKg > 0.0 && bookTotalFeedKg > 0.0) {
                bookTotalFeedKg / bookTotalProducedWeightKg
            } else 0.0
            val bookTotalExpenses = activeTxsForBook.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val bookTotalIncome = activeTxsForBook.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val bookCostPerBird = if (bookLiveBirds > 0) bookTotalExpenses / bookLiveBirds else 0.0
            val bookCostPerKg = if (bookStandingBiomassKg > 0.0) bookTotalExpenses / bookStandingBiomassKg else 0.0

            val bookBranches = remember(
                bookBarnName,
                bookAgeDays,
                bookLiveBirds,
                bookPlacedBirds,
                bookLatestTempC,
                bookTotalFeedQuintals,
                bookRemainingFeedQuintals,
                bookLatestFeedKg,
                bookLatestWaterL,
                bookCurrentWeightKg,
                bookActualFcr,
                lang
            ) {
                Ross308BroilerHandbook.buildFourBranchesForBarn(
                    barnName = bookBarnName,
                    ageDays = bookAgeDays,
                    placedBirds = bookPlacedBirds,
                    liveBirds = bookLiveBirds,
                    recordedTempC = bookLatestTempC,
                    totalFeedQuintals = bookTotalFeedQuintals,
                    remainingFeedQuintals = bookRemainingFeedQuintals,
                    latestDailyFeedKg = bookLatestFeedKg,
                    latestDailyWaterLiters = bookLatestWaterL,
                    currentWeightKg = bookCurrentWeightKg,
                    actualFcr = bookActualFcr,
                    qUnit = qUnit,
                    kgUnit = kgUnit
                )
            }

            // محتوى القسم النشط (📊 التحليلات | 💡 نصائح | 💬 المحادثة)
            when (selectedAiSubTab) {
                0 -> {
                    // القسم الأول: 📊 التحليلات (بطاقات التحليل الحية لعنبر المستخدم الفعلي فقط مترجمة لـ 14 لغة)
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .testTag("ai_smart_decisions_list"),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = Translations.tr("book_analytics_section_title", lang),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = aiAccent
                            )
                        }

                        // 1. بطاقة تحليل المصاريف والتكلفة الفعلية
                        item {
                            val expensesHeader = "${Translations.tr("book_analytics_expenses_header", lang)} — $bookBarnName"
                            val expensesBody = Translations.tr("book_analytics_expenses_body_fmt", lang)
                                .replace("{exp}", String.format(java.util.Locale.US, "%,.0f", bookTotalExpenses))
                                .replace("{inc}", String.format(java.util.Locale.US, "%,.0f", bookTotalIncome))
                                .replace("{cpb}", String.format(java.util.Locale.US, "%,.1f", bookCostPerBird))
                                .replace("{cpkg}", String.format(java.util.Locale.US, "%,.1f", bookCostPerKg))
                                .replace("{curr}", curr)
                                .replace("{kg}", kgUnit)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .tactile3DStyle(
                                        baseColor = aiAccent,
                                        cornerRadius = appearance.cornerRadiusDp.dp,
                                        elevation = 4.dp
                                    )
                                    .testTag("book_analytics_expenses_card"),
                                shape = aiCardShape,
                                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                border = BorderStroke(1.2.dp, glassBorderColor)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = expensesHeader,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.5.sp,
                                        color = aiAccent
                                    )
                                    Text(
                                        text = expensesBody,
                                        fontSize = 12.sp,
                                        color = primaryText,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        // 2. بطاقة تحليل استهلاك العلف والمخزون المتبقي ومعامل التحويل
                        item {
                            val targetFcrNow = Ross308BroilerHandbook.getStandardFcrForDay(bookAgeDays)
                            val fcrDisplay = if (bookActualFcr > 0.0) {
                                String.format(java.util.Locale.US, "%.2f", bookActualFcr)
                            } else {
                                Translations.tr("fcr_not_calculated", lang)
                            }
                            val feedBody = Translations.tr("book_analytics_feed_body_fmt", lang)
                                .replace("{used}", Translations.formatQuintalNumber(bookTotalFeedQuintals))
                                .replace("{rem}", Translations.formatQuintalNumber(bookRemainingFeedQuintals))
                                .replace("{q}", qUnit)
                                .replace("{wt}", String.format(java.util.Locale.US, "%.2f", bookCurrentWeightKg))
                                .replace("{day}", bookAgeDays.toString())
                                .replace("{targetWt}", String.format(java.util.Locale.US, "%.2f", bookTargetWeightKg))
                                .replace("{kg}", kgUnit)
                                .replace("{fcr}", fcrDisplay)
                                .replace("{targetFcr}", String.format(java.util.Locale.US, "%.2f", targetFcrNow))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .tactile3DStyle(
                                        baseColor = aiAccent,
                                        cornerRadius = appearance.cornerRadiusDp.dp,
                                        elevation = 4.dp
                                    )
                                    .testTag("book_analytics_feed_card"),
                                shape = aiCardShape,
                                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                border = BorderStroke(1.2.dp, glassBorderColor)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = Translations.tr("book_analytics_feed_fcr_header", lang),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.5.sp,
                                        color = aiAccent
                                    )
                                    Text(
                                        text = feedBody,
                                        fontSize = 12.sp,
                                        color = primaryText,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        // 3. بطاقة تحليل الحيوية والنفوق والمناخ مقارنة بمعيار Ross 308
                        item {
                            val targetTempNow = Ross308BroilerHandbook.getStandardTempForDay(bookAgeDays)
                            val tempDisplay = if (bookLatestTempC != null) {
                                "${String.format(java.util.Locale.US, "%.1f°C", bookLatestTempC)} (Ross 308: ${String.format(java.util.Locale.US, "%.0f°C", targetTempNow)})"
                            } else {
                                "— (Ross 308: ${String.format(java.util.Locale.US, "%.0f°C", targetTempNow)})"
                            }
                            val vitalityBody = Translations.tr("book_analytics_vitality_body_fmt", lang)
                                .replace("{live}", String.format(java.util.Locale.US, "%,d", bookLiveBirds))
                                .replace("{placed}", String.format(java.util.Locale.US, "%,d", bookPlacedBirds))
                                .replace("{day}", bookAgeDays.toString())
                                .replace("{livPct}", String.format(java.util.Locale.US, "%.1f", bookLivabilityPct))
                                .replace("{mort}", bookTotalMortality.toString())
                                .replace("{mortPct}", String.format(java.util.Locale.US, "%.2f", bookMortalityPct))
                                .replace("{temp}", tempDisplay)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .tactile3DStyle(
                                        baseColor = aiAccent,
                                        cornerRadius = appearance.cornerRadiusDp.dp,
                                        elevation = 4.dp
                                    )
                                    .testTag("book_analytics_livability_card"),
                                shape = aiCardShape,
                                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                border = BorderStroke(1.2.dp, glassBorderColor)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = Translations.tr("book_analytics_vitality_climate_header", lang),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.5.sp,
                                        color = aiAccent
                                    )
                                    Text(
                                        text = vitalityBody,
                                        fontSize = 12.sp,
                                        color = primaryText,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // نظام الخطوات الـ 3 المستقلة لكتب Ross 308 الـ 4 (رف الكتب 2x2 -> فهرس الكتاب -> صفحة الدرس بالسحب فقط)
                    val bookCtx = LocalContext.current
                    val activeBookUid = remember(bookCtx) {
                        AgritechAuthHelper.resolveActiveStorageIdentityKey(
                            AgritechAuthHelper.currentUserUid() ?: SaasPreferencesManager.getSavedUserUid(bookCtx)
                        )
                    }
                    BookAdviceNavigationFlow(
                        branches = bookBranches,
                        activeUserUid = activeBookUid,
                        lang = lang,
                        currentTheme = currentTheme,
                        accentColor = aiAccent,
                        accentPrimaryBtn = aiPrimaryBtn,
                        accentContainer = aiAccentContainer,
                        cornerRadiusDp = appearance.cornerRadiusDp,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .testTag("ai_diagnostics_cards_list")
                    )
                }

                2 -> {
                    AgritechAiVetInteractiveChatSection(
                        selectedFarm = selectedAiFarm,
                        currentTheme = currentTheme,
                        lang = lang,
                        diagnosisHistory = diagnosisHistory,
                        onUpdateDiagnosisHistory = { diagnosisHistory = it },
                        modifier = Modifier.weight(1f)
                    )
                }
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FarmCard(
    farm: FarmModel,
    currentTheme: AppThemeOption,
    lang: AppLanguage,
    firestoreRepo: AgritechFirestoreRepository? = null,
    onOpenNotebook: () -> Unit,
    onStartNewCycle: (FarmModel) -> Unit
) {
    val context = LocalContext.current
    val preferredFeedUnit = LocalFeedUnitPreference.current
    var showTeamQrInviteSheet by remember { mutableStateOf(false) }
    var showDeleteBarnOverlay by remember(farm.id) { mutableStateOf(false) }
    var showDeleteBarnConfirmDialog by remember(farm.id) { mutableStateOf(false) }
    val deleteBarnScope = rememberCoroutineScope()

    if (showTeamQrInviteSheet) {
        TeamQrInviteBottomSheet(
            farm = farm,
            lang = lang,
            firestoreRepo = firestoreRepo,
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
    val isDarwin = appearance.paperStyle == AppPaperStyleOption.DARWIN_PARCHMENT
    val primaryText = currentTheme.headingColor
    val secondaryText = currentTheme.secondaryTextColor
    val accentEmerald = appearance.accentColor
    val pillBgEmerald = appearance.accentContainerColor
    val pillBorderEmerald = accentEmerald.copy(alpha = 0.45f)
    val identityPanelBg = currentTheme.innerStatBoxColor
    val identityPanelBorder = when {
        isDark && isDarwin -> Color(0xFF8B6914)
        isDark -> Color(0xFF334155)
        isDarwin -> Color(0xFF8C6D46)
        else -> accentEmerald.copy(alpha = 0.28f)
    }
    val qrButtonBg = appearance.accentContainerColor
    val cardCornerShape = RoundedCornerShape(appearance.cornerRadiusDp.dp)
    val innerCornerDp = (appearance.cornerRadiusDp - 4).coerceAtLeast(4).dp
    val innerCornerShape = RoundedCornerShape(innerCornerDp)

    if (showDeleteBarnConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteBarnConfirmDialog = false },
            shape = cardCornerShape,
            containerColor = currentTheme.dialogCardColor,
            modifier = Modifier
                .darwinManuscriptBorder(cornerRadius = appearance.cornerRadiusDp.dp, isDark = isDark)
                .testTag("dialog_confirm_delete_farm_${farm.id}"),
            title = {
                Text(
                    Translations.tr("delete_barn_dialog_title", lang),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.5.sp,
                    color = ActionRed
                )
            },
            text = {
                Text(
                    "🏡 ${Translations.getLocalizedFarmName(farm.id, lang, farm.name)}\n\n${Translations.tr("delete_barn_dialog_msg", lang)}",
                    fontSize = 13.sp,
                    color = primaryText,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                val sessionState = LocalAgritechAppState.current
                PremiumPillButton(
                    text = Translations.tr("delete_barn_confirm_btn", lang),
                    onClick = {
                        showDeleteBarnConfirmDialog = false
                        showDeleteBarnOverlay = false
                        AgritechMockDb.deleteFarmCompletely(farm.id)
                        val nextFarmId = AgritechMockDb.farms.firstOrNull {
                            it.id != farm.id && it.id !in AgritechMockDb.deletedFarmIds
                        }?.id.orEmpty()
                        if (sessionState.activeFarmId == farm.id) {
                            sessionState.activeFarmId = nextFarmId
                            SaasPreferencesManager.saveAuthSession(
                                context = context,
                                isLoggedIn = sessionState.isLoggedIn,
                                role = sessionState.currentRole,
                                hasLinkedBarns = sessionState.hasLinkedBarns,
                                activeFarmId = nextFarmId,
                                userUid = sessionState.currentUserUid
                            )
                        }
                        LocalFarmPersistence.saveAll(context)
                        if (firestoreRepo != null) {
                            deleteBarnScope.launch {
                                firestoreRepo.deleteFarmRecord(farm.id)
                            }
                        }
                    },
                    variant = PremiumPillVariant.DELETE_RESET,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .height(42.dp)
                        .testTag("btn_confirm_delete_farm_${farm.id}")
                )
            },
            dismissButton = {
                PremiumPillButton(
                    text = Translations.tr("btn_cancel", lang),
                    onClick = {
                        showDeleteBarnConfirmDialog = false
                        showDeleteBarnOverlay = false
                    },
                    variant = PremiumPillVariant.PRIMARY,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .height(42.dp)
                        .testTag("btn_cancel_delete_farm_${farm.id}")
                )
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardCornerShape)
            .darwinManuscriptBorder(cornerRadius = appearance.cornerRadiusDp.dp, isDark = isDark)
            .combinedClickable(
                onClick = onOpenNotebook,
                onLongClick = { showDeleteBarnOverlay = !showDeleteBarnOverlay }
            )
            .testTag("farm_card_${farm.id}"),
        shape = cardCornerShape,
        colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
        border = if (showDeleteBarnOverlay) BorderStroke(1.8.dp, ActionRed) else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (appearance.paperStyle == AppPaperStyleOption.FROSTED_GLASS) 0.dp else 3.dp
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            if (showDeleteBarnOverlay) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PremiumPillButton(
                        text = Translations.tr("delete_barn_long_press_badge", lang),
                        onClick = { showDeleteBarnConfirmDialog = true },
                        variant = PremiumPillVariant.DELETE_RESET,
                        fontSize = 12.5.sp,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_trigger_delete_farm_${farm.id}")
                    )
                    PremiumTactileCalcButton(
                        label = "✕",
                        onClick = { showDeleteBarnOverlay = false },
                        isDark = isDark,
                        accentColor = ActionRed,
                        modifier = Modifier
                            .width(44.dp)
                            .testTag("btn_dismiss_delete_overlay_${farm.id}")
                    )
                }
            }
            if (activeCycle != null) {
                // Reactive calculations for liveBirds, cycle day, weight, and livability
                val reportsForCycle = AgritechMockDb.resolveReportsForFarmAndCycle(farm.id, activeCycle.id)
                val txsForCycle = AgritechMockDb.transactions.filter {
                    it.farmId == farm.id && (it.cycleId == activeCycle.id || it.cycleId.isBlank())
                }
                val totalMortality = reportsForCycle.sumOf { it.mortalityCount }
                val totalFeed = reportsForCycle.sumOf { it.feedIntakeKg }
                val totalPurchasedFeedKg = txsForCycle.filter {
                    it.type == TransactionType.EXPENSE &&
                        it.status.equals("APPROVED", true) &&
                        (it.quantityQuintals > 0.0 ||
                            it.category.contains("علف") ||
                            it.category.contains("Feed", true) ||
                            it.category.contains("Aliment", true) ||
                            it.category.contains("Pienso", true) ||
                            it.category.contains("Yem", true) ||
                            it.category.contains("Ração", true))
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
                val defaultCardWeightKg = actualCardWeightKg ?: PoultryCalculators.getRoss308StandardWeightKg(calculatedDay)
                var cardInteractiveWeightKg by remember(activeCycle.id, actualCardWeightKg, calculatedDay) {
                    mutableStateOf(defaultCardWeightKg)
                }
                val cumulativeSoldWeightKgForCard = PoultryCalculators.resolveCumulativeSoldWeightKg(activeCycle, txsForCycle)
                var cardInteractiveFcr by remember(activeCycle.id, totalFeed, cardInteractiveWeightKg) {
                    mutableStateOf(
                        if (totalFeed > 0.0) {
                            PoultryCalculators.calculateFcr(
                                totalFeedKg = totalFeed,
                                initialBirds = activeCycle.totalBirdsPlaced,
                                totalMortality = totalMortality,
                                birdsSold = totalSold,
                                ageInDays = calculatedDay,
                                actualAvgWeightKg = cardInteractiveWeightKg,
                                cumulativeSoldWeightKg = cumulativeSoldWeightKgForCard
                            )
                        } else {
                            0.0
                        }
                    )
                }

                val formattedStartDate = PoultryCalculators.formatDateDdMmYyyy(activeCycle.startDate)
                val kgUnit = Translations.tr("kg_unit", lang)
                val curr = Translations.tr("currency", lang)

                var showCardFeedLedger by remember { mutableStateOf(false) }
                var showFarmVitalityAndVaccinesSheet by remember { mutableStateOf(false) }
                var showFarmWeightAndIncomeSummarySheet by remember { mutableStateOf(false) }
                var showCardSalesCullSimulatorDialog by remember { mutableStateOf(false) }
                var cardCullBirdsText by remember(activeCycle.id) { mutableStateOf("0") }
                var cardExpectedPriceText by remember(activeCycle.id) { mutableStateOf("380") }

                if (showCardFeedLedger) {
                    FeedOrdersLedgerBottomSheet(
                        farmName = Translations.getLocalizedFarmName(farm.id, lang, farm.name),
                        cycle = activeCycle.copy(feedInventoryKg = effectiveFeedInventoryKg),
                        transactions = txsForCycle,
                        lang = lang,
                        onDismiss = { showCardFeedLedger = false }
                    )
                }

                // نافذة (ثالثاً): 💉 سجل الحيوية واللقاحات والأدوية المستعملة عند الضغط على مربع الحيوية
                if (showFarmVitalityAndVaccinesSheet) {
                    val cycleVetNotes = AgritechMockDb.vetNotes.filter {
                        it.farmId == farm.id && (it.cycleId == activeCycle.id || it.cycleId.isBlank())
                    }.sortedByDescending { it.dayNumber }
                    val waterMedReports = reportsForCycle.filter {
                        it.medicineGiven || it.medicineSummary.isNotBlank()
                    }.sortedByDescending { it.dayNumber }

                    androidx.compose.ui.window.Dialog(onDismissRequest = { showFarmVitalityAndVaccinesSheet = false }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .darwinManuscriptBorder(cornerRadius = 24.dp, isDark = isDark)
                                .testTag("farm_vitality_vaccines_sheet_${farm.id}"),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = currentTheme.dialogCardColor),
                            border = BorderStroke(1.2.dp, accentEmerald.copy(alpha = 0.4f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    Translations.tr("farm_vitality_vaccines_sheet_title", lang),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.5.sp,
                                    color = primaryText
                                )
                                Text(
                                    Translations.tr("farm_vitality_vaccines_sheet_subtitle", lang),
                                    fontSize = 11.5.sp,
                                    color = secondaryText
                                )

                                // شريط ملخص الحيوية: الطيور الحية | إجمالي النفوق | نسبة الحيوية
                                NeumorphicRecessedDisplay(
                                    isDark = isDark,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CalmFinancialItem(
                                            label = Translations.tr("quick_stat_live_birds", lang),
                                            value = String.format(java.util.Locale.US, "%,d", liveBirds),
                                            color = accentEmerald,
                                            modifier = Modifier.weight(1f)
                                        )
                                        CalmFinancialItem(
                                            label = Translations.tr("sum_mortality", lang),
                                            value = String.format(java.util.Locale.US, "%,d", totalMortality),
                                            color = ActionRed,
                                            modifier = Modifier.weight(1f)
                                        )
                                        CalmFinancialItem(
                                            label = Translations.tr("livability_label", lang),
                                            value = String.format(java.util.Locale.US, "%.1f%%", livabilityPct),
                                            color = accentEmerald,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                if (cycleVetNotes.isEmpty() && waterMedReports.isEmpty()) {
                                    Surface(
                                        shape = innerCornerShape,
                                        color = identityPanelBg,
                                        border = BorderStroke(1.dp, identityPanelBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            Translations.tr("farm_vitality_no_meds_yet", lang),
                                            fontSize = 12.5.sp,
                                            color = secondaryText,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                } else {
                                    cycleVetNotes.forEach { vNote ->
                                        Surface(
                                            shape = innerCornerShape,
                                            color = identityPanelBg,
                                            border = BorderStroke(1.dp, accentEmerald.copy(alpha = 0.3f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        "💉 ${Translations.getLocalizedMedicine(vNote.id, vNote.medicineName, lang)}",
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 13.sp,
                                                        color = primaryText,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(50),
                                                        color = pillBgEmerald
                                                    ) {
                                                        Text(
                                                            "${Translations.tr("day_prefix", lang)} ${vNote.dayNumber}",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = accentEmerald,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    "${Translations.getLocalizedDosage(vNote.id, vNote.dosage, lang)} • ${Translations.getLocalizedVetNote(vNote.id, vNote.note, lang)}",
                                                    fontSize = 11.5.sp,
                                                    color = secondaryText
                                                )
                                                Text(
                                                    Translations.tr("farm_med_status_vet_prescribed", lang),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = accentEmerald
                                                )
                                            }
                                        }
                                    }

                                    waterMedReports.forEach { rep ->
                                        Surface(
                                            shape = innerCornerShape,
                                            color = identityPanelBg,
                                            border = BorderStroke(1.dp, accentEmerald.copy(alpha = 0.22f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        "💊 ${rep.medicineSummary.ifBlank { Translations.tr("med_vitamin", lang) }}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.5.sp,
                                                        color = primaryText
                                                    )
                                                    Text(
                                                        Translations.tr("farm_med_status_water_confirmed", lang),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = accentEmerald
                                                    )
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(50),
                                                    color = pillBgEmerald
                                                ) {
                                                    Text(
                                                        "${Translations.tr("day_prefix", lang)} ${rep.dayNumber}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = accentEmerald,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                PremiumPillButton(
                                    text = Translations.tr("btn_close_check", lang),
                                    onClick = { showFarmVitalityAndVaccinesSheet = false },
                                    variant = PremiumPillVariant.PRIMARY,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("btn_close_vitality_vaccines_sheet_${farm.id}")
                                )
                            }
                        }
                    }
                }

                // نافذة (رابعاً): ⚖️ ملخص الوزن التقديري واستهلاك العلف ومعدل الدخل التقديري عند الضغط على مربع الوزن التقديري
                if (showFarmWeightAndIncomeSummarySheet) {
                    val approvedExpenses = txsForCycle.filter {
                        it.type == TransactionType.EXPENSE && it.status.equals("APPROVED", true)
                    }.sumOf { it.amount }
                    val approvedIncome = txsForCycle.filter {
                        it.type == TransactionType.INCOME && it.status.equals("APPROVED", true)
                    }.sumOf { it.amount }
                    val cardNetTreasury = approvedIncome - approvedExpenses
                    val feedPerBirdKg = if (liveBirds > 0) totalFeed / liveBirds.toDouble() else 0.0
                    val parsedCull = (cardCullBirdsText.toIntOrNull() ?: 0).coerceIn(0, liveBirds)
                    val marketableBirdsCard = (liveBirds - parsedCull).coerceAtLeast(0)
                    val simPriceCard = cardExpectedPriceText.toDoubleOrNull() ?: 380.0
                    val expectedSaleWeightKg = marketableBirdsCard * cardInteractiveWeightKg
                    val expectedSalesRevenue = expectedSaleWeightKg * simPriceCard
                    val estimatedCycleNetProfit = expectedSalesRevenue - approvedExpenses

                    fun persistCardAdjustedWeight(newWeightKg: Double) {
                        val clamped = (kotlin.math.round(newWeightKg * 100.0) / 100.0).coerceIn(0.10, 6.50)
                        cardInteractiveWeightKg = clamped
                        if (totalFeed > 0.0) {
                            cardInteractiveFcr = PoultryCalculators.calculateFcr(
                                totalFeedKg = totalFeed,
                                initialBirds = activeCycle.totalBirdsPlaced,
                                totalMortality = totalMortality,
                                birdsSold = totalSold,
                                ageInDays = calculatedDay,
                                actualAvgWeightKg = clamped,
                                cumulativeSoldWeightKg = cumulativeSoldWeightKgForCard
                            )
                        }
                        val latestRep = reportsForCycle.maxByOrNull { it.dayNumber }
                        if (latestRep != null) {
                            AgritechMockDb.upsertDailyReportWithAudit(
                                latestRep.copy(actualAvgWeightKg = clamped, isEdited = true)
                            )
                        }
                    }

                    fun persistCardAdjustedFcr(newFcr: Double) {
                        val clampedFcr = (kotlin.math.round(newFcr * 100.0) / 100.0).coerceIn(1.00, 3.80)
                        val oldFcr = cardInteractiveFcr.coerceAtLeast(0.1)
                        cardInteractiveFcr = clampedFcr
                        val impliedWeight = if (totalFeed > 0.0 && liveBirds > 0) {
                            val reqBiomass = totalFeed / clampedFcr
                            val liveBio = (reqBiomass - cumulativeSoldWeightKgForCard).coerceAtLeast(liveBirds * 0.1)
                            (liveBio / liveBirds).coerceIn(0.10, 6.50)
                        } else {
                            (cardInteractiveWeightKg * (oldFcr / clampedFcr)).coerceIn(0.10, 6.50)
                        }
                        persistCardAdjustedWeight(impliedWeight)
                    }

                    androidx.compose.ui.window.Dialog(onDismissRequest = { showFarmWeightAndIncomeSummarySheet = false }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .darwinManuscriptBorder(cornerRadius = 24.dp, isDark = isDark)
                                .testTag("farm_weight_income_sheet_${farm.id}"),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = currentTheme.dialogCardColor),
                            border = BorderStroke(1.2.dp, accentEmerald.copy(alpha = 0.4f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    Translations.tr("farm_weight_income_sheet_title", lang),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.5.sp,
                                    color = primaryText
                                )
                                Text(
                                    Translations.tr("farm_weight_income_sheet_subtitle", lang),
                                    fontSize = 11.5.sp,
                                    color = secondaryText
                                )

                                // 1. كم أكل القطيع (إجمالي العلف المستهلك بالقنطار والكيلوغرام + استهلاك الدجاجة الواحدة)
                                Text(
                                    Translations.tr("farm_feed_consumed_section_title", lang),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = accentEmerald
                                )
                                NeumorphicRecessedDisplay(
                                    isDark = isDark,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CalmFinancialItem(
                                            label = Translations.tr("farm_total_feed_consumed_label", lang),
                                            value = Translations.formatBulkFeedPurchaseQuantity(totalFeed, lang, preferredFeedUnit),
                                            color = accentEmerald,
                                            modifier = Modifier.weight(1.2f)
                                        )
                                        CalmFinancialItem(
                                            label = Translations.tr("farm_feed_per_bird_label", lang),
                                            value = "${String.format(java.util.Locale.US, "%.2f", feedPerBirdKg)} $kgUnit",
                                            color = primaryText,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                // 2. الوزن التقديري ومعامل التحويل مع أزرار [-] و[+] ثلاثية الأبعاد
                                NeumorphicRecessedDisplay(
                                    isDark = isDark,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "🐥 ${Translations.tr("est_weight", lang)}",
                                                color = secondaryText,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                PremiumTactileCalcButton(
                                                    label = "-",
                                                    onClick = { persistCardAdjustedWeight(cardInteractiveWeightKg - 0.1) },
                                                    isDark = isDark,
                                                    accentColor = accentEmerald,
                                                    fontSize = 16.sp,
                                                    modifier = Modifier
                                                        .width(42.dp)
                                                        .testTag("btn_card_weight_minus_${farm.id}")
                                                )
                                                Text(
                                                    "≈ ${String.format(java.util.Locale.US, "%.1f", cardInteractiveWeightKg)} $kgUnit",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 14.5.sp,
                                                    color = accentEmerald,
                                                    modifier = Modifier.testTag("txt_card_weight_val_${farm.id}")
                                                )
                                                PremiumTactileCalcButton(
                                                    label = "+",
                                                    onClick = { persistCardAdjustedWeight(cardInteractiveWeightKg + 0.1) },
                                                    isDark = isDark,
                                                    accentColor = accentEmerald,
                                                    fontSize = 16.sp,
                                                    modifier = Modifier
                                                        .width(42.dp)
                                                        .testTag("btn_card_weight_plus_${farm.id}")
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                Translations.tr("fcr", lang),
                                                color = secondaryText,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                PremiumTactileCalcButton(
                                                    label = "-",
                                                    onClick = {
                                                        val baseFcr = if (cardInteractiveFcr <= 0.0) 1.60 else cardInteractiveFcr
                                                        persistCardAdjustedFcr(baseFcr - 0.02)
                                                    },
                                                    isDark = isDark,
                                                    accentColor = accentEmerald,
                                                    fontSize = 16.sp,
                                                    modifier = Modifier
                                                        .width(42.dp)
                                                        .testTag("btn_card_fcr_minus_${farm.id}")
                                                )
                                                val fcrText = if (cardInteractiveFcr <= 0.0) {
                                                    Translations.tr("fcr_not_calculated", lang)
                                                } else {
                                                    "≈ ${String.format(java.util.Locale.US, "%.2f", cardInteractiveFcr)}"
                                                }
                                                Text(
                                                    fcrText,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = if (cardInteractiveFcr <= 0.0) 11.5.sp else 14.5.sp,
                                                    color = accentEmerald,
                                                    modifier = Modifier.testTag("txt_card_fcr_val_${farm.id}")
                                                )
                                                PremiumTactileCalcButton(
                                                    label = "+",
                                                    onClick = {
                                                        val baseFcr = if (cardInteractiveFcr <= 0.0) 1.60 else cardInteractiveFcr
                                                        persistCardAdjustedFcr(baseFcr + 0.02)
                                                    },
                                                    isDark = isDark,
                                                    accentColor = accentEmerald,
                                                    fontSize = 16.sp,
                                                    modifier = Modifier
                                                        .width(42.dp)
                                                        .testTag("btn_card_fcr_plus_${farm.id}")
                                                )
                                            }
                                        }
                                    }
                                }

                                // 3. معدل دخل العنبر وصافي الربح التقديري (3 أسطر أفقية مستقلة بدون أي تداخل للأرقام)
                                Text(
                                    Translations.tr("farm_income_summary_section_title", lang),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = accentEmerald
                                )
                                NeumorphicRecessedDisplay(
                                    isDark = isDark,
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                Translations.tr("quick_stat_net_treasury", lang),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = secondaryText,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                "${String.format(java.util.Locale.US, "%+,.0f", cardNetTreasury)} $curr",
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (cardNetTreasury >= 0) accentEmerald else ActionRed,
                                                maxLines = 1,
                                                softWrap = false,
                                                modifier = Modifier.testTag("txt_card_net_treasury_${farm.id}")
                                            )
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                Translations.tr("sim_total_expected_revenue_label", lang),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = secondaryText,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                "${String.format(java.util.Locale.US, "%,.0f", expectedSalesRevenue)} $curr",
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = accentEmerald,
                                                maxLines = 1,
                                                softWrap = false,
                                                modifier = Modifier.testTag("txt_card_expected_revenue_${farm.id}")
                                            )
                                        }
                                        HorizontalDivider(color = identityPanelBorder)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                Translations.tr("sim_estimated_net_profit_label", lang),
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = primaryText,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                "${String.format(java.util.Locale.US, "%+,.0f", estimatedCycleNetProfit)} $curr",
                                                fontSize = 14.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (estimatedCycleNetProfit >= 0) accentEmerald else ActionRed,
                                                maxLines = 1,
                                                softWrap = false,
                                                modifier = Modifier.testTag("txt_card_estimated_profit_${farm.id}")
                                            )
                                        }
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        showFarmWeightAndIncomeSummarySheet = false
                                        showCardSalesCullSimulatorDialog = true
                                    },
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.3.dp, accentEmerald),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = pillBgEmerald,
                                        contentColor = accentEmerald
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .tactile3DStyle(baseColor = accentEmerald, cornerRadius = 50.dp, elevation = 5.dp)
                                        .testTag("btn_open_card_profit_simulator_${farm.id}")
                                ) {
                                    Text(
                                        Translations.tr("btn_open_sales_cull_simulator", lang),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.5.sp,
                                        color = accentEmerald
                                    )
                                }

                                PremiumPillButton(
                                    text = Translations.tr("btn_close_check", lang),
                                    onClick = { showFarmWeightAndIncomeSummarySheet = false },
                                    variant = PremiumPillVariant.PRIMARY,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("btn_close_weight_income_sheet_${farm.id}")
                                )
                            }
                        }
                    }
                }

                // جدول محاكاة المبيعات والفرزة المفتوح مباشرة من بطاقة العنبر
                if (showCardSalesCullSimulatorDialog) {
                    val approvedExpenses = txsForCycle.filter {
                        it.type == TransactionType.EXPENSE && it.status.equals("APPROVED", true)
                    }.sumOf { it.amount }
                    val parsedCullBirds = (cardCullBirdsText.toIntOrNull() ?: 0).coerceIn(0, liveBirds)
                    val marketableBirds = (liveBirds - parsedCullBirds).coerceAtLeast(0)
                    val simPricePerKg = cardExpectedPriceText.toDoubleOrNull() ?: 380.0
                    val expectedTotalSaleWeightKg = marketableBirds * cardInteractiveWeightKg
                    val expectedTotalSalesRevenue = expectedTotalSaleWeightKg * simPricePerKg
                    val estimatedNetProfit = expectedTotalSalesRevenue - approvedExpenses

                    androidx.compose.ui.window.Dialog(onDismissRequest = { showCardSalesCullSimulatorDialog = false }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .darwinManuscriptBorder(cornerRadius = 24.dp, isDark = isDark)
                                .testTag("card_direct_sales_profit_simulator_${farm.id}"),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = currentTheme.dialogCardColor),
                            border = BorderStroke(1.2.dp, accentEmerald.copy(alpha = 0.35f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    Translations.tr("sales_profit_simulator_title", lang),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.5.sp,
                                    color = primaryText
                                )
                                Text(
                                    Translations.tr("sales_profit_simulator_subtitle", lang),
                                    fontSize = 11.5.sp,
                                    color = secondaryText
                                )
                                NeumorphicRecessedDisplay(
                                    isDark = isDark,
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "🐔 ${Translations.tr("marketable_birds_label", lang)}:",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryText
                                        )
                                        Text(
                                            "${String.format(java.util.Locale.US, "%,d", marketableBirds)} ${Translations.tr("bird_unit", lang)}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = accentEmerald
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    NeumorphicRecessedField(
                                        value = cardCullBirdsText,
                                        onValueChange = { raw -> cardCullBirdsText = raw.filter { it.isDigit() } },
                                        label = Translations.tr("cull_unfit_birds_label", lang),
                                        isDark = isDark,
                                        accentColor = accentEmerald,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                    PremiumTactileCalcButton(
                                        label = "\u200E-10",
                                        onClick = { cardCullBirdsText = (parsedCullBirds - 10).coerceAtLeast(0).toString() },
                                        isDark = isDark,
                                        accentColor = accentEmerald,
                                        fontSize = 11.5.sp,
                                        modifier = Modifier.width(48.dp)
                                    )
                                    PremiumTactileCalcButton(
                                        label = "\u200E+10",
                                        onClick = { cardCullBirdsText = (parsedCullBirds + 10).coerceAtMost(liveBirds).toString() },
                                        isDark = isDark,
                                        accentColor = accentEmerald,
                                        fontSize = 11.5.sp,
                                        modifier = Modifier.width(48.dp)
                                    )
                                }
                                NeumorphicRecessedField(
                                    value = cardExpectedPriceText,
                                    onValueChange = { raw -> cardExpectedPriceText = raw.filter { it.isDigit() || it == '.' } },
                                    label = Translations.tr("sim_expected_price_kg_label", lang),
                                    isDark = isDark,
                                    accentColor = accentEmerald,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                NeumorphicRecessedDisplay(
                                    isDark = isDark,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(Translations.tr("sim_total_expected_weight_label", lang), fontSize = 12.sp, color = secondaryText)
                                            Text("${String.format(java.util.Locale.US, "%,.1f", expectedTotalSaleWeightKg)} $kgUnit", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = primaryText)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(Translations.tr("sim_total_expected_revenue_label", lang), fontSize = 12.sp, color = secondaryText)
                                            Text("${String.format(java.util.Locale.US, "%,.0f", expectedTotalSalesRevenue)} $curr", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = accentEmerald)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(Translations.tr("sim_actual_cycle_expenses_label", lang), fontSize = 12.sp, color = secondaryText)
                                            Text("${String.format(java.util.Locale.US, "%,.0f", approvedExpenses)} $curr", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = ActionRed)
                                        }
                                        HorizontalDivider(color = identityPanelBorder)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(Translations.tr("sim_estimated_net_profit_label", lang), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = primaryText)
                                            Text(
                                                "${String.format(java.util.Locale.US, "%+,.0f", estimatedNetProfit)} $curr",
                                                fontSize = 14.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (estimatedNetProfit >= 0) accentEmerald else ActionRed
                                            )
                                        }
                                    }
                                }
                                PremiumPillButton(
                                    text = Translations.tr("btn_close_check", lang),
                                    onClick = { showCardSalesCullSimulatorDialog = false },
                                    variant = PremiumPillVariant.PRIMARY,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                )
                            }
                        }
                    }
                }

                // a) ID Header Row: 3D Barn Avatar (🏡) on start, full Barn Name + Location subtitle, and 3D Day Badge on opposite side
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
                        Tactile3DIconBadge(
                            emoji = "🏡",
                            tint = accentEmerald,
                            containerColor = pillBgEmerald,
                            boxSize = 46.dp,
                            cornerRadius = 14.dp,
                            emojiFontSize = 21.sp,
                            isDark = isDark
                        )
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

                    // 3D Embossed Day Capsule Badge on opposite side
                    Surface(
                        shape = innerCornerShape,
                        color = pillBgEmerald,
                        border = BorderStroke(1.dp, pillBorderEmerald),
                        modifier = Modifier.tactile3DStyle(baseColor = accentEmerald, cornerRadius = innerCornerDp, elevation = 5.dp)
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

                // b) ID Body: Structured bordered container with Active Live Birds + full-width Start Date + 3 equal-width 3D mini stat boxes
                val isFeedLow = effectiveFeedInventoryKg < 500.0
                val feedInventoryColor = if (isFeedLow) {
                    if (isDark) Color(0xFFFCA5A5) else Color(0xFFD32F2F)
                } else accentEmerald
                val miniBoxBg = currentTheme.innerStatBoxColor
                val miniBoxBorder = if (isDark) Color(0xFF334155) else accentEmerald.copy(alpha = 0.22f)
                val feedBoxBg = if (isFeedLow) {
                    if (appearance.paperStyle == AppPaperStyleOption.FROSTED_GLASS) {
                        Color(0xFFE11D48).copy(alpha = if (isDark) 0.24f else 0.16f)
                    } else if (isDark && isDarwin) {
                        Color(0xFF2A2018)
                    } else if (isDark) {
                        Color(0xFF450A0A)
                    } else {
                        Color(0xFFFFEBEE)
                    }
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

                        // 3 equal-width (Modifier.weight(1f)) 3D clickable mini stat boxes inside the card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1) Estimated Weight (قابل للضغط ويعرض ≈ 2.8 كغ مباشرة)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .darwinManuscriptBorder(cornerRadius = innerCornerDp, isDark = isDark)
                                    .tactile3DStyle(baseColor = accentEmerald, cornerRadius = innerCornerDp, elevation = 5.dp)
                                    .testTag("badge_estimated_weight_${farm.id}")
                                    .clickable { showFarmWeightAndIncomeSummarySheet = true },
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
                                        fontWeight = FontWeight.SemiBold,
                                        color = secondaryText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        "≈ ${String.format(java.util.Locale.US, "%.1f", cardInteractiveWeightKg)} $kgUnit",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = accentEmerald,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // 2) Livability (قابل للضغط لعرض سجل اللقاحات والأدوية)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .darwinManuscriptBorder(cornerRadius = innerCornerDp, isDark = isDark)
                                    .tactile3DStyle(baseColor = accentEmerald, cornerRadius = innerCornerDp, elevation = 5.dp)
                                    .testTag("badge_livability_${farm.id}")
                                    .clickable { showFarmVitalityAndVaccinesSheet = true },
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
                                        fontWeight = FontWeight.SemiBold,
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

                            // 3) Feed Stock (Quintals - 3D Capsule)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .darwinManuscriptBorder(cornerRadius = innerCornerDp, isDark = isDark)
                                    .tactile3DStyle(baseColor = feedInventoryColor, cornerRadius = innerCornerDp, elevation = 5.dp)
                                    .testTag("badge_feed_inventory_${farm.id}")
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
                                        fontWeight = FontWeight.SemiBold,
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

                // c) ID Footer Section: Clean 2-Button 3D Layout ("📖 فتح الدفتر" + "👥+ فريق")
                // ("🔄 إعادة الدورة" moved to Settings ⚙️ and inner barn notebook header)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PremiumPillButton(
                        text = Translations.tr("open_notebook", lang),
                        onClick = onOpenNotebook,
                        variant = PremiumPillVariant.PRIMARY,
                        fontSize = 13.5.sp,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        leadingIcon = {
                            Icon(
                                Icons.Default.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                        },
                        modifier = Modifier
                            .weight(1.45f)
                            .height(48.dp)
                            .testTag("btn_open_notebook_${farm.id}")
                    )

                    OutlinedButton(
                        onClick = { showTeamQrInviteSheet = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .tactile3DStyle(baseColor = accentEmerald, cornerRadius = 50.dp, elevation = 5.dp)
                            .testTag("btn_add_team_${farm.id}"),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.2.dp, accentEmerald.copy(alpha = 0.55f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = qrButtonBg,
                            contentColor = accentEmerald
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            Translations.tr("btn_invite_team_short", lang),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = accentEmerald,
                            maxLines = 1,
                            softWrap = false
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
                            shape = RoundedCornerShape(14.dp),
                            color = pillBgEmerald,
                            border = BorderStroke(1.dp, pillBorderEmerald),
                            modifier = Modifier
                                .size(46.dp)
                                .tactile3DStyle(baseColor = accentEmerald, cornerRadius = 14.dp, elevation = 6.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🏡", fontSize = 21.sp)
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
                        border = BorderStroke(1.dp, pillBorderEmerald),
                        modifier = Modifier.tactile3DStyle(baseColor = accentEmerald, cornerRadius = innerCornerDp, elevation = 5.dp)
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
                    PremiumPillButton(
                        text = Translations.tr("btn_start_new_cycle", lang),
                        onClick = { onStartNewCycle(farm) },
                        variant = PremiumPillVariant.PRIMARY,
                        leadingIcon = {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_start_cycle_${farm.id}")
                    )

                    OutlinedButton(
                        onClick = { showTeamQrInviteSheet = true },
                        modifier = Modifier
                            .height(48.dp)
                            .tactile3DStyle(baseColor = accentEmerald, cornerRadius = 50.dp, elevation = 5.dp)
                            .testTag("btn_add_team_${farm.id}"),
                        shape = RoundedCornerShape(50),
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
    firestoreRepo: AgritechFirestoreRepository? = null,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedInviteRole by remember { mutableStateOf(AppRole.WORKER) }
    var activeInviteId by remember(farm.id, selectedInviteRole) {
        mutableStateOf("inv_local_${farm.id}_${selectedInviteRole.name.lowercase()}")
    }
    LaunchedEffect(farm.id, selectedInviteRole, firestoreRepo) {
        if (firestoreRepo != null && AgritechAuthHelper.isSignedIn()) {
            val res = firestoreRepo.createFarmInvite(farm.id, selectedInviteRole)
            res.getOrNull()?.inviteId?.takeIf { it.isNotBlank() }?.let { cloudInviteId ->
                activeInviteId = cloudInviteId
            }
        }
    }
    val localizedFarmName = Translations.getLocalizedFarmName(farm.id, lang, farm.name)
    val qrPayload = formatAgritechQrInvitePayload(
        farmId = farm.id,
        role = selectedInviteRole,
        inviteId = activeInviteId
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = LocalAppAppearanceState.current.theme.dialogCardColor,
        scrimColor = Color.Black.copy(alpha = 0.65f),
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
                    detectedQrBanner = Translations.tr("qr_detected_banner_fmt", lang)
                        .replace("{farmId}", inviteData.farmId)
                        .replace("{role}", inviteData.role.name)
                    onJoinFarmWithRole?.invoke(inviteData.farmId, inviteData.role)
                    onJoinRole(inviteData.role)
                }
            )

            detectedQrBanner?.let { bannerText ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LightGreenContainer,
                    border = BorderStroke(1.dp, TextForestGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = bannerText,
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
                    Translations.tr("qr_confirm_scan_join_btn", lang),
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
    onJoinFarmWithRole: ((String, AppRole) -> Unit)? = null,
    onJoinInviteData: ((QrFarmInviteData) -> Unit)? = null
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
        AppRole.HOME_PERSONAL -> Translations.tr("role_badge_home_personal", lang)
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
                            onJoinInviteData?.invoke(inviteData)
                            onJoinFarmWithRole?.invoke(inviteData.farmId, inviteData.role)
                            onJoinRole(inviteData.role)
                        }
                    }
                )

                detectedQrBanner?.let { bannerText ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = accentContainer,
                        border = BorderStroke(1.dp, activeAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = bannerText,
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
    val dialogBg = currentTheme.dialogCardColor
    val primaryText = currentTheme.headingColor
    val secondaryText = currentTheme.secondaryTextColor
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

    val reportsForCycle = AgritechMockDb.resolveReportsForFarmAndCycle(
        farmId = farm.id,
        cycleId = activeCycle.id,
        extraReports = reports
    )
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
    var currentDraftSaleOperationId by rememberSaveable(farm.id, activeCycle.id) {
        mutableStateOf(java.util.UUID.randomUUID().toString())
    }
    var salesFlowStep by remember { mutableStateOf(SalesFlowStep.NONE) }
    var buyerNameInput by remember { mutableStateOf("") }
    var truckPlateInput by remember { mutableStateOf("") }
    var pricePerKgInput by remember { mutableStateOf("") }
    var shipmentPriceError by remember { mutableStateOf<String?>(null) }
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
    val pricePerKg = pricePerKgInput.trim().toDoubleOrNull() ?: 0.0
    val totalSalesValue = totalNetKg * pricePerKg

    val totalFeed = reportsForCycle.sumOf { it.feedIntakeKg }
    val totalWater = reportsForCycle.sumOf { it.waterIntakeLiters }
    val totalMortality = reportsForCycle.sumOf { it.mortalityCount }
    val totalPurchasedFeedKg = txsForCycle.filter {
        it.type == TransactionType.EXPENSE &&
            it.status.equals("APPROVED", true) &&
            (it.quantityQuintals > 0.0 ||
                it.category.contains("علف") ||
                it.category.contains("Feed", true) ||
                it.category.contains("Aliment", true) ||
                it.category.contains("Pienso", true) ||
                it.category.contains("Yem", true) ||
                it.category.contains("Ração", true))
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
                    val idempKey = "tx_inc_${farm.id}_${activeCycle.id}_$incomeDialogIdempUuid"
                    if (idempKey != lastSubmittedIdempotencyKey) {
                        lastSubmittedIdempotencyKey = idempKey
                        onAddTransaction(
                            TransactionModel(
                                id = idempKey,
                                farmId = farm.id,
                                cycleId = activeCycle.id,
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

    selectedTransactionForReceipt?.let { txForReceipt ->
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

    selectedInvoiceForView?.let { invoice ->
        ProfessionalSalesInvoiceModal(
            invoice = invoice,
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
                onPricePerKgChange = {
                    pricePerKgInput = it
                    shipmentPriceError = null
                },
                priceError = shipmentPriceError,
                birdsPerCrate = birdsPerCrateInput,
                onBirdsPerCrateChange = { birdsPerCrateInput = it },
                tareMode = tareMode,
                onTareModeChange = { tareMode = it },
                directTareTotal = directTareTotalInput,
                onDirectTareTotalChange = { directTareTotalInput = it },
                onProceedToWeighing = {
                    val enteredPrice = pricePerKgInput.trim().toDoubleOrNull() ?: 0.0
                    if (enteredPrice <= 0.0) {
                        shipmentPriceError = Translations.tr("input_required_error", lang)
                    } else {
                        shipmentPriceError = null
                        salesFlowStep = SalesFlowStep.STAGE_2_WEIGHING_TABLE
                    }
                },
                onBack = {
                    shipmentPriceError = null
                    salesFlowStep = SalesFlowStep.NONE
                }
            )
        }
        SalesFlowStep.STAGE_2_WEIGHING_TABLE -> {
            val parsedPrice = pricePerKgInput.trim().toDoubleOrNull() ?: 0.0
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
                        if (tareWeightsList.isEmpty() && grossWeightsList.isEmpty()) {
                            currentDraftSaleOperationId = java.util.UUID.randomUUID().toString()
                        }
                    }
                },
                onAddGrossWeight = { crates, kg ->
                    grossWeightsList = grossWeightsList + WeighEntry(crates, kg)
                },
                onDeleteGrossWeight = { idx ->
                    if (idx in grossWeightsList.indices) {
                        grossWeightsList = grossWeightsList.filterIndexed { i, _ -> i != idx }
                        if (tareWeightsList.isEmpty() && grossWeightsList.isEmpty()) {
                            currentDraftSaleOperationId = java.util.UUID.randomUUID().toString()
                        }
                    }
                },
                onFinalizeAndMigrate = {
                    if (totalSalesValue > 0.0 && totalNetKg > 0.0 && totalBirdsSold in 1..liveBirds) {
                        val deterministicIdempotencyKey = "sale_${farm.id}_${activeCycle.id}_$currentDraftSaleOperationId"
                        val invoiceNumber = "INV-${farm.id.takeLast(1).uppercase()}-${String.format(java.util.Locale.US, "%04d", transactions.count { it.type == TransactionType.INCOME } + 1)}-${currentDraftSaleOperationId.take(4).uppercase()}"
                        if (deterministicIdempotencyKey != lastSubmittedIdempotencyKey &&
                            AgritechMockDb.transactions.none { it.id == deterministicIdempotencyKey }
                        ) {
                            lastSubmittedIdempotencyKey = deterministicIdempotencyKey
                            val completedInvoice = SalesInvoiceModel(
                                invoiceNumber = invoiceNumber,
                                farmName = Translations.getLocalizedFarmName(farm.id, lang),
                                cycleId = activeCycle.id,
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
                                    id = deterministicIdempotencyKey,
                                    farmId = farm.id,
                                    cycleId = activeCycle.id,
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

                            // 3. تُصفّر حقول الميزان ويُولّد معرّف عملية جديد فقط بعد اكتمال حفظ الفاتورة وتفريغ أوزان المسودة بنجاح
                            buyerNameInput = ""
                            truckPlateInput = ""
                            pricePerKgInput = ""
                            shipmentPriceError = null
                            birdsPerCrateInput = "10"
                            tareMode = TareMode.DETAILED
                            directTareTotalInput = ""
                            tareWeightsList = emptyList()
                            grossWeightsList = emptyList()
                            currentDraftSaleOperationId = java.util.UUID.randomUUID().toString()
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
            var showFcrWeightEditorDialog by remember { mutableStateOf(false) }
            var showProfitEstimatorDialog by remember { mutableStateOf(false) }

            // Shared interactive state for flexible Owner adjustment of Estimated Weight, FCR, and Net Profit Simulator
            val defaultEstWeightKg = (actualMeasuredWeightKg ?: currentStandardWeightKg).coerceAtLeast(0.1)
            val defaultFcr = if (accurateFcr > 0.0) accurateFcr else 0.0
            var interactiveWeightKg by remember(activeCycle.id, ageInDays, actualMeasuredWeightKg) {
                mutableStateOf(defaultEstWeightKg)
            }
            var interactiveFcr by remember(activeCycle.id, ageInDays, actualMeasuredWeightKg, totalFeed) {
                mutableStateOf(defaultFcr)
            }
            var cullUnfitBirdsText by remember(activeCycle.id, liveBirds) {
                mutableStateOf("0")
            }
            var simWeightInputText by remember(activeCycle.id, ageInDays, actualMeasuredWeightKg) {
                mutableStateOf(String.format(java.util.Locale.US, "%.1f", defaultEstWeightKg))
            }
            var expectedPricePerKgText by remember(activeCycle.id) {
                mutableStateOf("250")
            }

            fun updateWeightAndRecalcFcr(newWeight: Double) {
                val clampedW = (kotlin.math.round(newWeight * 100.0) / 100.0).coerceIn(0.10, 6.50)
                interactiveWeightKg = clampedW
                simWeightInputText = String.format(java.util.Locale.US, "%.1f", clampedW)
                if (totalFeed > 0.0) {
                    interactiveFcr = PoultryCalculators.calculateFcr(
                        totalFeedKg = totalFeed,
                        initialBirds = activeCycle.totalBirdsPlaced,
                        totalMortality = totalMortality,
                        birdsSold = cumulativeBirdsSold,
                        ageInDays = ageInDays,
                        actualAvgWeightKg = clampedW,
                        cumulativeSoldWeightKg = cumulativeSoldWeightKg
                    )
                }
            }

            fun updateFcrAndRecalcWeight(newFcr: Double) {
                val clampedFcr = (kotlin.math.round(newFcr * 100.0) / 100.0).coerceIn(1.00, 3.80)
                val oldFcr = interactiveFcr.coerceAtLeast(0.1)
                interactiveFcr = clampedFcr
                val impliedWeight = if (totalFeed > 0.0 && liveBirds > 0) {
                    val requiredTotalBiomass = totalFeed / clampedFcr
                    val liveBiomass = (requiredTotalBiomass - cumulativeSoldWeightKg).coerceAtLeast(liveBirds * 0.1)
                    (liveBiomass / liveBirds).coerceIn(0.10, 6.50)
                } else {
                    (interactiveWeightKg * (oldFcr / clampedFcr)).coerceIn(0.10, 6.50)
                }
                val roundedW = kotlin.math.round(impliedWeight * 100.0) / 100.0
                interactiveWeightKg = roundedW
                simWeightInputText = String.format(java.util.Locale.US, "%.1f", roundedW)
            }

            val dynamicProducedBiomassKg = PoultryCalculators.calculateTotalProducedBiomassKg(
                liveBirds = liveBirds,
                avgLiveWeightKg = interactiveWeightKg,
                cumulativeSoldWeightKg = cumulativeSoldWeightKg
            )
            val dynamicCostPerKg = PoultryCalculators.calculateCostPerKg(
                totalExpenses = totalExpenses,
                totalProducedBiomassKg = dynamicProducedBiomassKg
            )

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
            val typedIndoorReading = remember(farm.id, latestReportForHeader) {
                IndoorReadingState.fromManualReport(farm.id, latestReportForHeader)
            }
            val latestTempC: Double? = typedIndoorReading.temperatureC
            val hasRecordedTemperature = latestTempC != null
            val liveTempC = latestTempC ?: 0.0
            val liveAmmoniaPpm = typedIndoorReading.ammoniaPpm ?: 0.0
            val liveVentStatus = typedIndoorReading.ventilationStatus ?: Translations.tr("indoor_reading_no_data_short", lang)
            val isClimateAlert = (latestTempC != null && (latestTempC > 33.5 || latestTempC < 19.0)) || liveAmmoniaPpm > 18.0
            val isClimateWarning = !isClimateAlert && ((latestTempC != null && latestTempC > 31.5) || liveAmmoniaPpm > 12.0)

            if (showBarnClimateAndStatsDialog) {
                BarnClimateAndStatsDialog(
                    farmName = Translations.getLocalizedFarmName(farm.id, lang, farm.name),
                    barnId = farm.id,
                    accountUid = try { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty() } catch (_: Exception) { "" },
                    indoorReading = typedIndoorReading,
                    cycleDay = calculatedCycleDay,
                    temperatureC = liveTempC,
                    ammoniaPpm = liveAmmoniaPpm,
                    ventilationStatus = liveVentStatus,
                    liveBirds = liveBirds,
                    livabilityPct = liveabilityPct,
                    feedInventoryKg = effectiveFeedInventoryKg,
                    fcr = interactiveFcr,
                    estimatedWeightKg = interactiveWeightKg,
                    netBalance = netBalance,
                    totalExpenses = totalExpenses,
                    currencyCode = curr,
                    lang = lang,
                    currentTheme = currentTheme,
                    onFcrWeightCardClick = { showFcrWeightEditorDialog = true },
                    onNetTreasuryCardClick = { showProfitEstimatorDialog = true },
                    onDismiss = { showBarnClimateAndStatsDialog = false }
                )
            }

            if (showFcrWeightEditorDialog) {
                val dialogAccent = appearance.accentColor
                val dialogSecondaryColor = if (currentTheme.isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                androidx.compose.ui.window.Dialog(onDismissRequest = { showFcrWeightEditorDialog = false }) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_fcr_weight_editor")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                Translations.tr("fcr_weight_editor_dialog_title", lang),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.5.sp,
                                color = if (currentTheme.isDark) Color.White else currentTheme.headingColor
                            )

                            // 🐥 الوزن التقديري الحالي: [-] ≈ 2.8 كغ [+]
                            NeumorphicRecessedDisplay(
                                isDark = currentTheme.isDark,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "🐥 ${Translations.tr("est_weight", lang)}",
                                        color = dialogSecondaryColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        PremiumTactileCalcButton(
                                            label = "-",
                                            onClick = { updateWeightAndRecalcFcr(interactiveWeightKg - 0.1) },
                                            isDark = currentTheme.isDark,
                                            accentColor = dialogAccent,
                                            fontSize = 16.sp,
                                            modifier = Modifier
                                                .width(44.dp)
                                                .testTag("btn_dialog_adjust_weight_minus")
                                        )
                                        Text(
                                            "≈ ${String.format(java.util.Locale.US, "%.1f", interactiveWeightKg)} $kgUnit",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = dialogAccent,
                                            modifier = Modifier.testTag("txt_dialog_est_weight_value")
                                        )
                                        PremiumTactileCalcButton(
                                            label = "+",
                                            onClick = { updateWeightAndRecalcFcr(interactiveWeightKg + 0.1) },
                                            isDark = currentTheme.isDark,
                                            accentColor = dialogAccent,
                                            fontSize = 16.sp,
                                            modifier = Modifier
                                                .width(44.dp)
                                                .testTag("btn_dialog_adjust_weight_plus")
                                        )
                                    }
                                }
                            }

                            // ⚖️ معامل التحويل FCR: [-] ≈ 1.68 [+]
                            NeumorphicRecessedDisplay(
                                isDark = currentTheme.isDark,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        Translations.tr("fcr", lang),
                                        color = dialogSecondaryColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        PremiumTactileCalcButton(
                                            label = "-",
                                            onClick = {
                                                val baseF = if (interactiveFcr <= 0.0) 1.60 else interactiveFcr
                                                updateFcrAndRecalcWeight(baseF - 0.02)
                                            },
                                            isDark = currentTheme.isDark,
                                            accentColor = dialogAccent,
                                            fontSize = 16.sp,
                                            modifier = Modifier
                                                .width(44.dp)
                                                .testTag("btn_dialog_adjust_fcr_minus")
                                        )
                                        val dialogFcrText = if (interactiveFcr <= 0.0) {
                                            Translations.tr("fcr_not_calculated", lang)
                                        } else {
                                            "≈ ${String.format(java.util.Locale.US, "%.2f", interactiveFcr)}"
                                        }
                                        Text(
                                            dialogFcrText,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = if (interactiveFcr <= 0.0) 11.5.sp else 15.sp,
                                            color = dialogAccent,
                                            modifier = Modifier.testTag("txt_dialog_fcr_value")
                                        )
                                        PremiumTactileCalcButton(
                                            label = "+",
                                            onClick = {
                                                val baseF = if (interactiveFcr <= 0.0) 1.60 else interactiveFcr
                                                updateFcrAndRecalcWeight(baseF + 0.02)
                                            },
                                            isDark = currentTheme.isDark,
                                            accentColor = dialogAccent,
                                            fontSize = 16.sp,
                                            modifier = Modifier
                                                .width(44.dp)
                                                .testTag("btn_dialog_adjust_fcr_plus")
                                        )
                                    }
                                }
                            }

                            // 🏷️ تكلفة الكيلو الواحد (تتحدث لحظياً)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    Translations.tr("cost_per_kg", lang),
                                    fontSize = 12.5.sp,
                                    color = dialogSecondaryColor
                                )
                                Text(
                                    if (dynamicProducedBiomassKg > 0) "${String.format(java.util.Locale.US, "%.1f", dynamicCostPerKg)} $curr/$kgUnit" else "0 $curr/$kgUnit",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (currentTheme.isDark) Color.White else currentTheme.headingColor
                                )
                            }

                            PremiumPillButton(
                                text = Translations.tr("btn_close_check", lang),
                                onClick = { showFcrWeightEditorDialog = false },
                                variant = PremiumPillVariant.PRIMARY,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_close_fcr_weight_editor")
                            )
                        }
                    }
                }
            }

            if (showProfitEstimatorDialog) {
                val dialogAccent = appearance.accentColor
                val parsedCullBirds = (cullUnfitBirdsText.toIntOrNull() ?: 0).coerceIn(0, liveBirds)
                val marketableBirds = (liveBirds - parsedCullBirds).coerceAtLeast(0)
                val simPricePerKg = expectedPricePerKgText.toDoubleOrNull() ?: 0.0
                val expectedTotalSaleWeightKg = marketableBirds * interactiveWeightKg
                val expectedTotalSalesRevenue = expectedTotalSaleWeightKg * simPricePerKg
                val estimatedNetProfit = expectedTotalSalesRevenue - totalExpenses
                val isSimulatedProfitPositive = estimatedNetProfit >= 0.0
                val profitHighlightColor = if (isSimulatedProfitPositive) dialogAccent else ActionRed

                androidx.compose.ui.window.Dialog(onDismissRequest = { showProfitEstimatorDialog = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .darwinManuscriptBorder(cornerRadius = appearance.cornerRadiusDp.dp, isDark = currentTheme.isDark)
                            .testTag("card_sales_profit_simulator"),
                        shape = RoundedCornerShape(appearance.cornerRadiusDp.dp),
                        colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                        border = BorderStroke(1.2.dp, dialogAccent.copy(alpha = 0.35f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                Translations.tr("sales_profit_simulator_title", lang),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.5.sp,
                                color = if (currentTheme.isDark) Color.White else currentTheme.headingColor
                            )
                            Text(
                                Translations.tr("sales_profit_simulator_subtitle", lang),
                                fontSize = 11.5.sp,
                                color = if (currentTheme.isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                            )

                            // أ) الطيور الصالحة للبيع + خصم الفرزة (الطيور غير الصالحة)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                NeumorphicRecessedDisplay(
                                    isDark = currentTheme.isDark,
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "🐔 ${Translations.tr("marketable_birds_label", lang)}:",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (currentTheme.isDark) Color.White else currentTheme.headingColor
                                        )
                                        Text(
                                            "${String.format(java.util.Locale.US, "%,d", marketableBirds)} ${Translations.tr("bird_unit", lang)}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = dialogAccent,
                                            modifier = Modifier.testTag("txt_sim_marketable_birds")
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    NeumorphicRecessedField(
                                        value = cullUnfitBirdsText,
                                        onValueChange = { raw ->
                                            cullUnfitBirdsText = raw.filter { it.isDigit() }
                                        },
                                        label = Translations.tr("cull_unfit_birds_label", lang),
                                        isDark = currentTheme.isDark,
                                        accentColor = dialogAccent,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Next
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("input_cull_unfit_birds")
                                    )
                                    PremiumTactileCalcButton(
                                        label = "\u200E-10",
                                        onClick = {
                                            val nextCull = (parsedCullBirds - 10).coerceAtLeast(0)
                                            cullUnfitBirdsText = nextCull.toString()
                                        },
                                        isDark = currentTheme.isDark,
                                        accentColor = dialogAccent,
                                        fontSize = 11.5.sp,
                                        modifier = Modifier
                                            .width(48.dp)
                                            .testTag("btn_cull_minus")
                                    )
                                    PremiumTactileCalcButton(
                                        label = "\u200E+10",
                                        onClick = {
                                            val nextCull = (parsedCullBirds + 10).coerceAtMost(liveBirds)
                                            cullUnfitBirdsText = nextCull.toString()
                                        },
                                        isDark = currentTheme.isDark,
                                        accentColor = dialogAccent,
                                        fontSize = 11.5.sp,
                                        modifier = Modifier
                                            .width(48.dp)
                                            .testTag("btn_cull_plus")
                                    )
                                }

                                // ب) متوسط الوزن التقديري (كغ) + سعر البيع المتوقع للكيلو (دج)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    NeumorphicRecessedField(
                                        value = simWeightInputText,
                                        onValueChange = { raw ->
                                            val filtered = raw.filter { it.isDigit() || it == '.' }
                                            simWeightInputText = filtered
                                            val parsedW = filtered.toDoubleOrNull()
                                            if (parsedW != null && parsedW >= 0.1) {
                                                interactiveWeightKg = parsedW.coerceAtMost(6.5)
                                                if (totalFeed > 0.0) {
                                                    interactiveFcr = PoultryCalculators.calculateFcr(
                                                        totalFeedKg = totalFeed,
                                                        initialBirds = activeCycle.totalBirdsPlaced,
                                                        totalMortality = totalMortality,
                                                        birdsSold = cumulativeBirdsSold,
                                                        ageInDays = ageInDays,
                                                        actualAvgWeightKg = interactiveWeightKg,
                                                        cumulativeSoldWeightKg = cumulativeSoldWeightKg
                                                    )
                                                }
                                            }
                                        },
                                        label = Translations.tr("sim_avg_weight_label", lang),
                                        isDark = currentTheme.isDark,
                                        accentColor = dialogAccent,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Next
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("input_sim_avg_weight")
                                    )

                                    NeumorphicRecessedField(
                                        value = expectedPricePerKgText,
                                        onValueChange = { raw ->
                                            expectedPricePerKgText = raw.filter { it.isDigit() || it == '.' }
                                        },
                                        label = Translations.tr("sim_expected_price_kg_label", lang),
                                        isDark = currentTheme.isDark,
                                        accentColor = dialogAccent,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("input_sim_price_per_kg")
                                    )
                                }
                            }

                            // ج) النتيجة الحسابية المباشرة داخل شاشة رقمية غائرة (Recessed Inset Neumorphic Display)
                            NeumorphicRecessedDisplay(
                                isDark = currentTheme.isDark,
                                contentPadding = PaddingValues(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            Translations.tr("sim_total_expected_weight_label", lang),
                                            fontSize = 12.sp,
                                            color = if (currentTheme.isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            "${String.format(java.util.Locale.US, "%,.1f", expectedTotalSaleWeightKg)} $kgUnit",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (currentTheme.isDark) Color.White else currentTheme.headingColor,
                                            modifier = Modifier.testTag("txt_sim_total_weight")
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            Translations.tr("sim_total_expected_revenue_label", lang),
                                            fontSize = 12.sp,
                                            color = if (currentTheme.isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            "${String.format(java.util.Locale.US, "%,.0f", expectedTotalSalesRevenue)} $curr",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = dialogAccent,
                                            modifier = Modifier.testTag("txt_sim_total_revenue")
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            Translations.tr("sim_actual_cycle_expenses_label", lang),
                                            fontSize = 12.sp,
                                            color = if (currentTheme.isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            "- ${String.format(java.util.Locale.US, "%,.0f", totalExpenses)} $curr",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ActionRed
                                        )
                                    }

                                    HorizontalDivider(color = profitHighlightColor.copy(alpha = 0.25f))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            Translations.tr("sim_estimated_net_profit_label", lang),
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (currentTheme.isDark) Color.White else currentTheme.headingColor,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            "${if (isSimulatedProfitPositive) "+" else "-"} ${String.format(java.util.Locale.US, "%,.0f", kotlin.math.abs(estimatedNetProfit))} $curr",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = profitHighlightColor,
                                            modifier = Modifier.testTag("txt_sim_net_profit")
                                        )
                                    }
                                }
                            }

                            PremiumPillButton(
                                text = Translations.tr("btn_close_check", lang),
                                onClick = { showProfitEstimatorDialog = false },
                                variant = PremiumPillVariant.PRIMARY,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_close_profit_simulator_dialog")
                            )
                        }
                    }
                }
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

            selectedOwnerDayReportForMedicalDetails?.let { selectedReport ->
                val activeReport = reportsForCycle.firstOrNull { it.dayNumber == selectedReport.dayNumber }
                    ?: selectedReport
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

            activeContextualChatChannel?.let { channel ->
                ContextualChatBottomSheet(
                    title = activeContextualChatTitle,
                    subtitle = "${Translations.getLocalizedFarmName(farm.id, lang, farm.name)} • ${activeCycle.id}",
                    channelId = channel,
                    currentTheme = currentTheme,
                    lang = lang,
                    firestoreRepo = firestoreRepo,
                    onOpenVetInvoice = if (channel.contains("vet")) {
                        {
                            activeContextualChatChannel = null
                            showVetMedicineInvoiceDialog = true
                        }
                    } else null,
                    onDismiss = { activeContextualChatChannel = null }
                )
            }

            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .darwinPaperBackground(currentTheme),
                containerColor = Color.Transparent,
                topBar = {
                    val isDarwinNight = currentTheme.isDark && appearance.paperStyle == AppPaperStyleOption.DARWIN_PARCHMENT
                    val isFrostedGlassNotebook = appearance.paperStyle == AppPaperStyleOption.FROSTED_GLASS
                    val climatePillColor = when {
                        isClimateAlert -> if (currentTheme.isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626)
                        isClimateWarning -> if (currentTheme.isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
                        !hasRecordedTemperature -> currentTheme.secondaryTextColor
                        else -> appearance.accentColor
                    }
                    val climatePillBg = when {
                        isDarwinNight || isFrostedGlassNotebook -> currentTheme.cardColor
                        isClimateAlert -> if (currentTheme.isDark) Color(0xFF450A0A) else Color(0xFFFEF2F2)
                        isClimateWarning -> if (currentTheme.isDark) Color(0xFF451A03) else Color(0xFFFFFBEB)
                        !hasRecordedTemperature -> if (currentTheme.isDark) currentTheme.cardColor else Color(0xFFF1F5F9)
                        else -> appearance.accentContainerColor
                    }
                    val headerPillShape = RoundedCornerShape((appearance.cornerRadiusDp - 6).coerceAtLeast(6).dp)

                    Surface(
                        color = if (isFrostedGlassNotebook) currentTheme.cardColor else currentTheme.bgColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .then(
                                if (isFrostedGlassNotebook) {
                                    Modifier.darwinManuscriptBorder(cornerRadius = 0.dp, isDark = currentTheme.isDark)
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 6.dp)
                        ) {
                            var showResetCycleDialog by remember { mutableStateOf(false) }
                            val notebookResetScope = rememberCoroutineScope()
                            if (showResetCycleDialog) {
                                AlertDialog(
                                    onDismissRequest = { showResetCycleDialog = false },
                                    shape = RoundedCornerShape(appearance.cornerRadiusDp.dp),
                                    containerColor = currentTheme.dialogCardColor,
                                    title = {
                                        Text(
                                            Translations.tr("reset_cycle_dialog_title", lang),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.5.sp,
                                            color = ActionRed
                                        )
                                    },
                                    text = {
                                        Text(
                                            Translations.tr("reset_cycle_dialog_msg", lang),
                                            fontSize = 13.sp,
                                            color = if (currentTheme.isDark) Color.White else currentTheme.headingColor,
                                            lineHeight = 19.sp
                                        )
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                AgritechMockDb.resetCycleAndClearBarn(farm.id, activeCycle.id)
                                                tareWeightsList = emptyList()
                                                grossWeightsList = emptyList()
                                                lastSavedInvoice = null
                                                if (firestoreRepo != null) {
                                                    notebookResetScope.launch {
                                                        firestoreRepo.resetCycleAndClearBarn(farm.id, activeCycle.id)
                                                    }
                                                }
                                                salesMigratedMsg = Translations.tr("reset_cycle_success_msg", lang)
                                                showResetCycleDialog = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                            shape = headerPillShape,
                                            modifier = Modifier.testTag("btn_confirm_reset_cycle")
                                        ) {
                                            Text(
                                                Translations.tr("reset_cycle_confirm_btn", lang),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = { showResetCycleDialog = false },
                                            modifier = Modifier.testTag("btn_cancel_reset_cycle")
                                        ) {
                                            Text(
                                                Translations.tr("btn_cancel", lang),
                                                color = if (currentTheme.isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                )
                            }

                            // Row 1: Back Button + Full Barn Name & Day/Live Birds + Reset Cycle Button + Settings Icon
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
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Row 2: Generous 3D Climate Status Pill (`🌡️`) + 3D Feed Stock Pill (`🌾`)
                            val headerPillCornerDp = (appearance.cornerRadiusDp - 6).coerceAtLeast(6).dp
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1. Sleek Top Header 3D Climate Status Pill/Icon Button (`🌡️ 29.0°C • المناخ`)
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .tactile3DStyle(baseColor = climatePillColor, cornerRadius = headerPillCornerDp, elevation = 5.dp)
                                        .testTag("header_climate_status_btn")
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
                                        val climateHeaderBtnText = if (latestTempC != null) {
                                            "🌡️ ${String.format(java.util.Locale.US, "%.1f°C", latestTempC)} • ${Translations.tr("climate_header_btn_label", lang)}"
                                        } else {
                                            "🌡️ ${Translations.tr("climate_header_btn_label", lang)} • ${Translations.tr("indoor_reading_no_data_short", lang)}"
                                        }
                                        Text(
                                            climateHeaderBtnText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = climatePillColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                // 2. Dual Smart Feed Unit Header 3D Inventory Badge: e.g., `مخزون: 40 ق 🌾`
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .tactile3DStyle(baseColor = feedStatusColor, cornerRadius = headerPillCornerDp, elevation = 5.dp)
                                        .testTag("header_feed_inventory_indicator")
                                        .clickable { showFeedLedgerBottomSheet = true },
                                    shape = headerPillShape,
                                    color = when {
                                        isDarwinNight || isFrostedGlassNotebook -> currentTheme.cardColor
                                        isLowFeedInventory -> if (currentTheme.isDark) Color(0xFF450A0A) else Color(0xFFFFEBEE)
                                        else -> appearance.accentContainerColor
                                    },
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
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .tactile3DStyle(baseColor = appearance.accentOption.primaryColor, cornerRadius = appearance.cornerRadiusDp.dp)
                            .testTag("fab_worker_chat")
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
                else -> null
            }
        },
        bottomBar = {
            val items = listOf(
                Triple(0, Translations.tr("tab_ledger", lang), Icons.Default.TableChart),
                Triple(1, Translations.tr("tab_treasury", lang), Icons.Default.ReceiptLong),
                Triple(2, Translations.tr("tab_analytics", lang), Icons.Default.Insights),
                Triple(3, Translations.tr("notebook_tab_inventory", lang), Icons.Default.Inventory2)
            )
            AgritechAnimatedBottomBar(
                items = items.map { (index, label, icon) ->
                    AgritechTabBarItemSpec(
                        id = "notebook_tab_$index",
                        label = label,
                        icon = icon,
                        testTag = "nav_tab_$index"
                    )
                },
                selectedIndex = selectedTab,
                onSelectIndex = { selectedTab = it },
                currentTheme = currentTheme,
                modifier = Modifier.testTag("notebook_bottom_nav")
            )
        }
    ) { padding ->
        val appearance = LocalAppAppearanceState.current
        val notebookAccent = appearance.accentColor
        val notebookButtonAccent = appearance.accentOption.primaryColor
        val notebookShape = RoundedCornerShape(appearance.cornerRadiusDp.dp)
        val notebookBtnCornerDp = (appearance.cornerRadiusDp - 6).coerceAtLeast(4).dp
        val notebookBtnShape = RoundedCornerShape(notebookBtnCornerDp)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .darwinPaperBackground(currentTheme)
                .padding(padding)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
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
                            PremiumPillButton(
                                text = Translations.tr("btn_owner_add_daily_report", lang),
                                onClick = {
                                    editingDailyReport = null
                                    showOwnerDailyReportDialog = true
                                },
                                variant = PremiumPillVariant.PRIMARY,
                                fontSize = 12.sp,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.EditNote,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("btn_owner_add_daily_report")
                            )
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
                                Box(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = Translations.tr("withdrawal_active_banner_title", lang)
                                            .replace("{days}", activeWithdrawalDays.toString()),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.5.sp,
                                        color = if (currentTheme.isDark) Color(0xFFFCA5A5) else ActionRed,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 14.dp)
                                .darwinManuscriptBorder(cornerRadius = appearance.cornerRadiusDp.dp, isDark = currentTheme.isDark),
                            shape = notebookShape,
                            colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (appearance.paperStyle == AppPaperStyleOption.FROSTED_GLASS) 0.dp else 2.dp
                            )
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
                                        .background(currentTheme.innerStatBoxColor, RoundedCornerShape(8.dp))
                                        .darwinManuscriptBorder(cornerRadius = 8.dp, isDark = currentTheme.isDark)
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
                                CalmSummaryItem(label = Translations.tr("sum_mortality", lang), value = "$totalMortality 💀", isAlert = totalMortality > 5, headingColor = mortColor)
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .darwinManuscriptBorder(cornerRadius = appearance.cornerRadiusDp.dp, isDark = currentTheme.isDark)
                                .testTag("card_treasury_summary"),
                            shape = notebookShape,
                            colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    Translations.tr("treasury_title", lang),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = currentTheme.headingColor
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                NeumorphicRecessedDisplay(
                                    isDark = currentTheme.isDark,
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    val positiveGreen = notebookAccent
                                    val labelTextColor = if (currentTheme.isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                                    val dividerColor = if (currentTheme.isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = Translations.tr("total_expenses", lang),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = labelTextColor
                                            )
                                            Text(
                                                text = "${String.format(java.util.Locale.US, "%,.0f", totalExpenses)} $curr",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = ActionRed,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = Translations.tr("total_income", lang),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = labelTextColor
                                            )
                                            Text(
                                                text = "${String.format(java.util.Locale.US, "%,.0f", totalIncome)} $curr",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = positiveGreen,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = Translations.tr("net_balance", lang),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (currentTheme.isDark) Color.White else currentTheme.headingColor
                                            )
                                            Text(
                                                text = "${String.format(java.util.Locale.US, "%,.0f", netBalance)} $curr",
                                                fontSize = 15.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (netBalance >= 0) positiveGreen else ActionRed,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
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
                            PremiumPillButton(
                                text = Translations.tr("btn_spend", lang),
                                onClick = { showExpenseDialog = true },
                                variant = PremiumPillVariant.DELETE_RESET,
                                fontSize = 12.5.sp,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            )

                            PremiumPillButton(
                                text = Translations.tr("btn_income", lang),
                                onClick = { showIncomeDialog = true },
                                variant = PremiumPillVariant.SALES_INCOME,
                                fontSize = 12.5.sp,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            )
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
                                        .darwinManuscriptBorder(cornerRadius = appearance.cornerRadiusDp.dp, isDark = currentTheme.isDark)
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
                                                    color = if (currentTheme.isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
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
                        // 1. بطاقة مؤشرات الأداء والكفاءة (مع تحكم مرن في الوزن التقديري ومعامل التحويل FCR)
                        Text(Translations.tr("analytics_title", lang), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = currentTheme.headingColor)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .darwinManuscriptBorder(cornerRadius = appearance.cornerRadiusDp.dp, isDark = currentTheme.isDark),
                            shape = notebookShape,
                            colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                val positiveGreen = notebookAccent
                                val darkValueColor = if (currentTheme.isDark) Color.White else Color(0xFF212121)
                                val secondaryLabelColor = if (currentTheme.isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
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
                                val costPerKg = dynamicCostPerKg

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

                                // 5. 🐥 الوزن التقديري الحالي (قراءة فقط برقم عشري واحد نظيف — التعديل محصور في نافذة المناخ)
                                val formattedSimpleWeight = String.format(java.util.Locale.US, "%.1f", interactiveWeightKg)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "🐥 ${Translations.tr("est_weight", lang)}",
                                        color = secondaryLabelColor,
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        "≈ $formattedSimpleWeight $kgUnit",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = positiveGreen,
                                        modifier = Modifier.testTag("txt_est_weight_value")
                                    )
                                }
                                HorizontalDivider(color = if (currentTheme.isDark) Color(0xFF2C2C2C) else Color(0xFFF5F5F5))

                                // 6. ⚖️ معامل التحويل FCR (قراءة فقط — التعديل محصور في نافذة المناخ)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        Translations.tr("fcr", lang),
                                        color = secondaryLabelColor,
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        "≈ ${String.format(java.util.Locale.US, "%.2f", interactiveFcr)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = positiveGreen,
                                        modifier = Modifier.testTag("txt_fcr_value")
                                    )
                                }
                                HorizontalDivider(color = if (currentTheme.isDark) Color(0xFF2C2C2C) else Color(0xFFF5F5F5))

                                // 7. 💰 إجمالي مصاريف العنبر (دج)
                                CalmMetricRow(
                                    Translations.tr("total_expenses", lang),
                                    "${String.format(java.util.Locale.US, "%,.0f", totalExpenses)} $curr",
                                    ActionRed
                                )

                                // 8. 🐓 تكلفة الدجاجة الواحدة (دج/طائر)
                                CalmMetricRow(
                                    Translations.tr("cost_per_bird", lang),
                                    "${String.format(java.util.Locale.US, "%,.1f", costPerBird)} $curr/$birdUnit",
                                    darkValueColor
                                )

                                // 9. 🏷️ تكلفة الكيلو الواحد (دج/كغ — يتحدث لحظياً عند تعديل الوزن أو FCR)
                                CalmMetricRow(
                                    Translations.tr("cost_per_kg", lang),
                                    if (dynamicProducedBiomassKg > 0) "${String.format(java.util.Locale.US, "%.1f", costPerKg)} $curr/$kgUnit" else "0 $curr/$kgUnit",
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
                                            color = if (currentTheme.isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                                        )
                                    }
                                }

                                // تفاصيل الشحنة المسجلة في الأعلى بوضوح (مع تنبيه واضح بأنها مسودة غير مرحلة للخزنة حتى يتم الحفظ)
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
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (currentTheme.isDark) Color(0xFF78350F).copy(alpha = 0.45f) else Color(0xFFFEF3C7),
                                                border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("unsaved_weighing_draft_banner")
                                            ) {
                                                Text(
                                                    text = Translations.tr("weigh_unsaved_draft_warning", lang),
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (currentTheme.isDark) Color(0xFFFDE68A) else Color(0xFFB45309),
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                )
                                            }

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
                                            lineHeight = 18.sp,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }

                                // 1. زر البيع الرئيسي المختصر [البيع 🍗] بتصميم كبسولة ثلاثية الأبعاد زمردية (PremiumPillButton)
                                PremiumPillButton(
                                    text = Translations.tr("btn_start_sales_flow", lang),
                                    onClick = {
                                        salesFlowStep = SalesFlowStep.STAGE_1_SHIPMENT_INFO
                                    },
                                    variant = PremiumPillVariant.SALES_INCOME,
                                    fontSize = 14.5.sp,
                                    leadingIcon = {
                                        Icon(Icons.Default.Scale, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("btn_start_sales_flow")
                                )

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
                    // TAB 3: 📦 المخزون (Barn Inventory: Gas, Wood Shavings, Water Tankers, Medications + Vet Records & Consultation)
                    val inventorySyncScope = rememberCoroutineScope()
                    val pendingVetNotes = vetNotes.filter { it.status.equals("PENDING", ignoreCase = true) }
                    val gasCount = AgritechMockDb.getBarnSupplyCount(farm.id, "gas", 0)
                    val shavingsCount = AgritechMockDb.getBarnSupplyCount(farm.id, "shavings", 0)
                    val waterCount = AgritechMockDb.getBarnSupplyCount(farm.id, "water", 0)
                    val medsCount = AgritechMockDb.getBarnSupplyCount(farm.id, "meds", 0)
                    val invPrimaryText = if (currentTheme.isDark) Color.White else currentTheme.headingColor
                    val invSecondaryText = if (currentTheme.isDark) Color(0xFFCBD5E1) else Color(0xFF64748B)

                    val barnSupplyItems = listOf(
                        Triple(
                            "gas",
                            Translations.tr("barn_inv_gas_cylinders_title", lang) to Translations.tr("barn_inv_gas_cylinders_sub", lang),
                            gasCount to Translations.tr("barn_inv_gas_unit", lang)
                        ),
                        Triple(
                            "shavings",
                            Translations.tr("barn_inv_wood_shavings_title", lang) to Translations.tr("barn_inv_wood_shavings_sub", lang),
                            shavingsCount to Translations.tr("barn_inv_shavings_unit", lang)
                        ),
                        Triple(
                            "water",
                            Translations.tr("barn_inv_water_tankers_title", lang) to Translations.tr("barn_inv_water_tankers_sub", lang),
                            waterCount to Translations.tr("barn_inv_water_unit", lang)
                        ),
                        Triple(
                            "meds",
                            Translations.tr("barn_inv_meds_packs_title", lang) to Translations.tr("barn_inv_meds_packs_sub", lang),
                            medsCount to Translations.tr("barn_inv_meds_unit", lang)
                        )
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("barn_inventory_and_vet_tab"),
                        contentPadding = PaddingValues(bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // القسم الأول (أعلى التبويب): 📦 جرد ومخزون العنبر (الغاز، النشارة، الماء، والأدوية) بأزرار [-1] و[+1] و[+5] ثلاثية الأبعاد
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    Translations.tr("barn_inventory_section_title", lang),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = invPrimaryText
                                )
                                Text(
                                    Translations.tr("barn_inventory_section_subtitle", lang),
                                    fontSize = 11.5.sp,
                                    color = invSecondaryText
                                )
                            }
                        }

                        items(barnSupplyItems, key = { it.first }) { (supplyKey, titleSub, countUnit) ->
                            val (itemTitle, itemSubtitle) = titleSub
                            val (currentCount, unitLabel) = countUnit
                            val defaultVal = 0
                            val supplyIcon = when (supplyKey) {
                                "gas" -> Icons.Default.LocalFireDepartment
                                "shavings" -> Icons.Default.Grass
                                "water" -> Icons.Default.WaterDrop
                                else -> Icons.Default.MedicalServices
                            }
                            val supplyIconTint = when (supplyKey) {
                                "gas" -> Color(0xFFEA580C)
                                "shavings" -> Color(0xFFD97706)
                                "water" -> Color(0xFF0284C7)
                                else -> notebookAccent
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("barn_inventory_card_$supplyKey"),
                                shape = notebookShape,
                                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                                border = BorderStroke(1.dp, notebookAccent.copy(alpha = 0.28f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Tactile3DIconBadge(
                                                icon = supplyIcon,
                                                tint = supplyIconTint,
                                                isDark = currentTheme.isDark,
                                                size = 40.dp,
                                                iconSize = 20.dp
                                            )
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    itemTitle,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 14.sp,
                                                    color = invPrimaryText
                                                )
                                                Text(
                                                    itemSubtitle,
                                                    fontSize = 11.5.sp,
                                                    color = invSecondaryText
                                                )
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = appearance.accentContainerColor,
                                            border = BorderStroke(1.dp, notebookAccent.copy(alpha = 0.45f)),
                                            modifier = Modifier
                                                .tactile3DStyle(baseColor = notebookAccent, cornerRadius = 50.dp, elevation = 4.dp)
                                                .testTag("txt_barn_inv_count_$supplyKey")
                                        ) {
                                            Text(
                                                "$currentCount $unitLabel",
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = notebookAccent,
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
                                        PremiumTactileCalcButton(
                                            label = "\u200E-1",
                                            onClick = {
                                                AgritechMockDb.adjustBarnSupplyCount(farm.id, supplyKey, -1, defaultVal)
                                                val nextVal = AgritechMockDb.getBarnSupplyCount(farm.id, supplyKey, defaultVal)
                                                inventorySyncScope.launch { firestoreRepo?.updateBarnInventoryItem(farm.id, supplyKey, nextVal) }
                                            },
                                            isDark = currentTheme.isDark,
                                            accentColor = notebookAccent,
                                            fontSize = 12.5.sp,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                                .tactile3DStyle(baseColor = Color(0xFF64748B), cornerRadius = 14.dp, elevation = 5.dp)
                                                .testTag("btn_dec_barn_inv_$supplyKey")
                                        )
                                        PremiumTactileCalcButton(
                                            label = "\u200E+1",
                                            onClick = {
                                                AgritechMockDb.adjustBarnSupplyCount(farm.id, supplyKey, +1, defaultVal)
                                                val nextVal = AgritechMockDb.getBarnSupplyCount(farm.id, supplyKey, defaultVal)
                                                inventorySyncScope.launch { firestoreRepo?.updateBarnInventoryItem(farm.id, supplyKey, nextVal) }
                                            },
                                            isDark = currentTheme.isDark,
                                            accentColor = notebookAccent,
                                            fontSize = 12.5.sp,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                                .tactile3DStyle(baseColor = Color(0xFF64748B), cornerRadius = 14.dp, elevation = 5.dp)
                                                .testTag("btn_inc1_barn_inv_$supplyKey")
                                        )
                                        PremiumTactileCalcButton(
                                            label = "\u200E+5",
                                            onClick = {
                                                AgritechMockDb.adjustBarnSupplyCount(farm.id, supplyKey, +5, defaultVal)
                                                val nextVal = AgritechMockDb.getBarnSupplyCount(farm.id, supplyKey, defaultVal)
                                                inventorySyncScope.launch { firestoreRepo?.updateBarnInventoryItem(farm.id, supplyKey, nextVal) }
                                            },
                                            isSelected = true,
                                            isDark = currentTheme.isDark,
                                            accentColor = notebookButtonAccent,
                                            fontSize = 12.5.sp,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                                .tactile3DStyle(baseColor = notebookButtonAccent, cornerRadius = 14.dp, elevation = 5.dp)
                                                .testTag("btn_inc5_barn_inv_$supplyKey")
                                        )
                                    }
                                }
                            }
                        }

                        // القسم الثاني (تحت قسم المخزون في نفس التبويب): 💉 السجل والوصفات الطبية البيطرية + زر 💬 استشارة الطبيب البيطري + زر 🧾 إصدار فاتورة دواء
                        item {
                            HorizontalDivider(
                                color = if (currentTheme.isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    "💉 ${Translations.tr("vet_records_title", lang)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = invPrimaryText
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PremiumPillButton(
                                        text = "💬 ${Translations.tr("chat_vet_title", lang)}",
                                        onClick = {
                                            activeContextualChatTitle = Translations.tr("chat_vet_title", lang)
                                            activeContextualChatChannel = "${cycle.id}_vet"
                                        },
                                        variant = PremiumPillVariant.PRIMARY,
                                        fontSize = 11.5.sp,
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                            .testTag("btn_consult_vet_3d")
                                    )
                                    PremiumPillButton(
                                        text = "🧾 ${Translations.tr("vet_invoice_btn", lang)}",
                                        onClick = { showVetMedicineInvoiceDialog = true },
                                        variant = PremiumPillVariant.SALES_INCOME,
                                        fontSize = 11.5.sp,
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                            .testTag("btn_open_vet_invoice_dialog")
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

                        vetInvoiceSuccessBanner?.let { bannerMsg ->
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
                                            bannerMsg,
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

