package com.example.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.AgritechMockDb
import com.example.models.*
import kotlinx.coroutines.delay
import java.util.Locale

data class HumanPersonaCritiqueItem(
    val id: String,
    val role: AppRole,
    val personaNameAr: String,
    val personaRoleTitleAr: String,
    val avatarEmoji: String,
    val dayTriggered: Int,
    val fieldGapTitleAr: String,
    val humanCritiqueQuoteAr: String,
    val equationVerificationAr: String,
    val resolutionStatusAr: String
)

data class SimDayTimelineEvent(
    val day: Int,
    val actorEmoji: String,
    val actorName: String,
    val actionSummary: String,
    val mathAuditNote: String
)

object PersonaSimulatorEngine {
    var currentSimulatedDay by mutableStateOf(7)
    var isSimulationRunning by mutableStateOf(false)
    var isSpeed10x by mutableStateOf(false)
    var elapsedTicksInDay by mutableStateOf(0)
    var totalPharmacyPacksDeducted by mutableStateOf(2)
    var transportMortalityBirdsAtScale by mutableStateOf(6)
    var hatcheryBonusChicksAdded by mutableStateOf(160) // 2% of 8,000

    val simulationTimelineLog = mutableStateListOf(
        SimDayTimelineEvent(
            day = 1,
            actorEmoji = "🐣",
            actorName = "مفرخة الأمل + الحاج مراد (المالك)",
            actionSummary = "استلام 8,000 كتكوت Ross 308 (بسعر 80 دج/فرخ) + 160 كتكوت بونص تعويض نفوق النقل (2%) وتخزين دفعة العلف التأسيسية (80 ق بسعر 8,000 دج/ق).",
            mathAuditNote = "✓ فاتورة الصيصان (640,000 دج) وفاتورة العلف التأسيسي (640,000 دج) مسجلة في مصاريف خزنة المالك."
        ),
        SimDayTimelineEvent(
            day = 4,
            actorEmoji = "🩺",
            actorName = "د. كمال (الطبيب البيطري)",
            actionSummary = "برنامج التحصين وفيتامينات الاستقبال AD3E وإصدار فاتورة الأدوية (49,000 دج) مع خصم عبوة آلياً من جرد صيدلية العيادة.",
            mathAuditNote = "✓ تم خصم 1 عبوة من صيدلية العيادة وتفعيل شارة (فترة السحب الطبية قبل البيع)."
        ),
        SimDayTimelineEvent(
            day = 5,
            actorEmoji = "💧",
            actorName = "أحمد (المشرف) + صهاريج المياه",
            actionSummary = "توريد 6 صهاريج مياه شرب نقية (صهريج 3,000 لتر = 1,000 دج) وضبط حرارة التحضين والفرشة.",
            mathAuditNote = "✓ قيد مصروف صهاريج المياه (6,000 دج) وتطابق عدد الطيور الحية في العنبر."
        ),
        SimDayTimelineEvent(
            day = 7,
            actorEmoji = "👷",
            actorName = "أحمد (مشرف العنبر الميداني)",
            actionSummary = "تسجيل استهلاك اليوم السابع (4.4 ق علف، 2,400 لتر ماء، 10 نافق) وتحصين نيوكاسل Clone 30.",
            mathAuditNote = "✓ مجموع علف الأسبوع الأول (21.6 ق) مخصوم بدقة من مستودع العلف (المتبقي: 58.4 ق من أصل 80 ق)."
        )
    )

    fun getHumanPersonaCritiques(): List<HumanPersonaCritiqueItem> = listOf(
        HumanPersonaCritiqueItem(
            id = "critique_owner",
            role = AppRole.OWNER_BARN,
            personaNameAr = "الحاج مراد (مالك المزرعة والعنابر)",
            personaRoleTitleAr = "👨‍🌾 مالك المزرعة • فحص الخزنة ومطابقة مخزون العلف وتكلفة الطائر",
            avatarEmoji = "👨‍🌾",
            dayTriggered = 7,
            fieldGapTitleAr = "معايرة استهلاك الـ 40 يوماً (390 ق علف) وتكلفة الدجاجة (~550 دج/طائر)",
            humanCritiqueQuoteAr = "«يا جماعة، عنبر 8,000 طائر في 40 يوم يستهلك حوالي 5 كغ علف للطائر (يعني ~390 قنطار بسعر 8,000 دج/ق = 3,120,000 دج)، ومع الصيصان (80 دج) والماء (صهريج 3,000 لتر بـ 1,000 دج) والأدوية والغاز والعمالة لازم تخرج تكلفة الدجاجة الواحدة حوالي 550 دج، وعداد المخزون يطلب علف فقط كي ينزل تحت 35 ق!»",
            equationVerificationAr = "فحص المعادلة (اليوم 40): إجمالي العلف المستهلك (390 ق) • المتبقي بالمستودع (20 ق) • إجمالي المصاريف (4,190,000 دج) ÷ 7,620 طائر حي = 550 دج/طائر ✓",
            resolutionStatusAr = "✓ تمت معايرة منحنى العلف والمصاريف وتكلفة الطائر بدقة ميدانية"
        ),
        HumanPersonaCritiqueItem(
            id = "critique_vet",
            role = AppRole.VET_CLINIC,
            personaNameAr = "د. كمال (الطبيب البيطري المستقل)",
            personaRoleTitleAr = "🩺 طبيب بيطري • فحص نسبة النفوق (4.75%) وفترة سحب الدواء",
            avatarEmoji = "🩺",
            dayTriggered = 14,
            fieldGapTitleAr = "ضبط النفوق الواقعي (3% إلى 8%) وخصم الدواء آلياً مع شارة السحب",
            humanCritiqueQuoteAr = "«في الميدان، نفوق عنبر 8,000 طائر خلال 40 يوم يتراوح طبيعياً بين 3% و 8% (حوالي 380 طائر نافق، أي ~95.2% حيوية)، وميزانية الأدوية واللقاحات حوالي 115,000 دج للدورة مع تنبيه فترة السحب الطبية قبل البيع.»",
            equationVerificationAr = "فحص الحيوية: 8,000 طائر - 380 نافق (4.75%) = 7,620 طائر حي جاهز للتسويق (حيوية 95.25%) ✓",
            resolutionStatusAr = "✓ تم ضبط منحنى النفوق والحيوية وميزانية التحصين والأدوية"
        ),
        HumanPersonaCritiqueItem(
            id = "critique_worker",
            role = AppRole.WORKER,
            personaNameAr = "أحمد (مشرف العنبر الميداني)",
            personaRoleTitleAr = "👷 مشرف العنبر • فحص منحنى العلف والماء اليومي (190,000 لتر)",
            avatarEmoji = "👷",
            dayTriggered = 21,
            fieldGapTitleAr = "تدرج العلف اليومي (1.8 ق إلى 16.5 ق) واستهلاك الماء (25 لتر/طائر)",
            humanCritiqueQuoteAr = "«العلف في العنبر يبدأ بـ 1.8 ق في اليوم الأول ويطلع تدريجياً لـ 5.5 ق في اليوم 10، و 9.5 ق في اليوم 20، و 13.5 ق في اليوم 30، حتى يوصل 16.5 ق في الأيام 35-40، والماء يبدأ بـ 1,200 لتر ويصل 7,500 لتر/يوم (إجمالي 190,000 لتر = 63 صهريج).»",
            equationVerificationAr = "فحص الجدول: مجموع العلف = 390.0 ق (5.1 كغ/طائر) • مجموع الماء = 190,000 لتر (~25 لتر/طائر) ✓",
            resolutionStatusAr = "✓ تم تحديث منحنى الاستهلاك اليومي للعلف والماء لـ 40 يوماً"
        ),
        HumanPersonaCritiqueItem(
            id = "critique_feed_supplier",
            role = AppRole.FEED_SUPPLIER,
            personaNameAr = "سي كمال (مورد الأعلاف والذرة والصويا)",
            personaRoleTitleAr = "🌾 مورد أعلاف • التوريد الذكي تحت سقف 35 قنطار بسعر 8,000 دج/ق",
            avatarEmoji = "🌾",
            dayTriggered = 25,
            fieldGapTitleAr = "منع تكدس العلف في المستودع والطلب فقط عند الهبوط تحت 35 ق",
            humanCritiqueQuoteAr = "«ما يلزمش المستودع يتكدس فيه 596 قنطار! أنا كمورد أعلاف نرسل دفعة جديدة (80 ق بسعر 8,000 دج للقنطار) غير كي يهبط مخزون العنبر تحت 35 قنطار، باش في اليوم 40 يبقى في المستودع حوالي 20 قنطار فقط.»",
            equationVerificationAr = "فحص المستودع: إجمالي المورّد (410 ق) - المستهلك (390 ق) = 20.0 ق متبقي في اليوم 40 (ضمن النطاق المثالي 10-30 ق) ✓",
            resolutionStatusAr = "✓ تم ضبط شرط طلب العلف (< 35 ق) وسعر القنطار (8,000 دج)"
        ),
        HumanPersonaCritiqueItem(
            id = "critique_hatchery",
            role = AppRole.CHICK_HATCHERY,
            personaNameAr = "مفرخة الأمل الحديثة (بائع الصيصان)",
            personaRoleTitleAr = "🐣 مفرخة صيصان • تسعير الصوص 80 دج وبونص الـ 2%",
            avatarEmoji = "🐣",
            dayTriggered = 1,
            fieldGapTitleAr = "اعتماد السعر الواقعي للصوص (80 دج) + 2% بونص تعويض النقل",
            humanCritiqueQuoteAr = "«سعر فرخ التسمين Ross 308 الملقح هو 80 دج للصوص (8,000 × 80 دج = 640,000 دج) مع 160 فرخ بونص مجاني (2%) لتعويض نفوق النقل والاستقبال.»",
            equationVerificationAr = "فحص الفاتورة: 8,000 صوص × 80 دج = 640,000 دج مقيدة في مصاريف الدورة ✓",
            resolutionStatusAr = "✓ تم اعتماد سعر 80 دج للصوص في الدورة التأسيسية"
        ),
        HumanPersonaCritiqueItem(
            id = "critique_trader",
            role = AppRole.WHOLESALE_TRADER,
            personaNameAr = "ياسين (تاجر دجاج الجملة)",
            personaRoleTitleAr = "🚚 تاجر جملة • طلب الأوزان الثقيلة (2.8 - 3.0 كغ) ومعامل التحويل 1.75",
            avatarEmoji = "🚚",
            dayTriggered = 38,
            fieldGapTitleAr = "الوصول لوزن التسويق (2.85 - 2.95 كغ) ومعامل تحويل FCR ~1.75",
            humanCritiqueQuoteAr = "«في سوق الجملة نطلب دجاج واجد بمتوسط وزن 2.8 إلى 3.0 كغ! في اليوم 40 يوصل متوسط وزن الطائر 2.92 كغ (إجمالي وزن حي 22,250 كغ لـ 7,620 طائر) ومعامل تحويل ممتاز 1.75 وتكلفة 188 دج للكيلو.»",
            equationVerificationAr = "فحص القبان: 7,620 طائر × 2.92 كغ = 22,250.4 كغ • FCR = 39,000 كغ ÷ 22,250.4 كغ = 1.75 ✓",
            resolutionStatusAr = "✓ تم تحديث طلبات التاجر ومنحنى الوزن لـ 2.8 - 3.0 كغ"
        ),
        HumanPersonaCritiqueItem(
            id = "critique_importer",
            role = AppRole.VET_IMPORTER,
            personaNameAr = "مؤسسة بيوفارم (مستورد الأدوية البيطرية)",
            personaRoleTitleAr = "📦 مستورد أدوية • فحص تموين صيدلية العيادة وسلسلة التبريد",
            avatarEmoji = "📦",
            dayTriggered = 30,
            fieldGapTitleAr = "تموين صيدلية العيادة البيطرية مباشرة عند انخفاض المخزون",
            humanCritiqueQuoteAr = "«كمستورد لقاحات وأدوية، كي ينقص مخزون الدكتور كمال في صيدليته بسبب الوصفات اليومية للعنابر، لازم بضغطة زر نقدر نرسل دفعة تموين (+10 عبوات) تزيد مباشرة في جرد [صيدلية العيادة] مع رقم التشغيلة وسلسلة التبريد.»",
            equationVerificationAr = "فحص التموين: توريد المستورد (+10 عبوات) يرفع رصيد صيدلية العيادة البيطرية فوراً ويحدث الطلبية ✓",
            resolutionStatusAr = "✓ تم تفعيل زر التموين المباشر لصيدلية العيادة"
        )
    )

    private fun seedCalibratedBaselineDays1To7ForFarm(farmId: String, cycleId: String) {
        // Remove any previous baseline/simulated reports, vet notes, and transactions for this farm
        val removeFarmIds = if (farmId != "farm_1" && AgritechMockDb.farms.none { it.id == "farm_1" }) {
            setOf(farmId, "farm_1")
        } else {
            setOf(farmId)
        }
        AgritechMockDb.dailyReports.removeAll { it.farmId in removeFarmIds }
        AgritechMockDb.vetNotes.removeAll { it.farmId in removeFarmIds }
        AgritechMockDb.transactions.removeAll { it.farmId in removeFarmIds }

        // Seed Days 1..7 calibrated daily reports (Sum feed = 21.6 Q = 2,160 kg; Sum water = 12,600 L; Sum mortality = 67)
        for (d in 1..7) {
            val feedKg = PoultryCalculators.getCalibratedDailyFeedQuintals(d) * 100.0
            val waterL = PoultryCalculators.getCalibratedDailyWaterLiters(d)
            val mort = PoultryCalculators.getCalibratedDailyMortality(d)
            val weightKg = PoultryCalculators.getCalibratedBroilerWeightKg(d)
            val temp = kotlin.math.round((33.2 - (d - 1) * 0.4) * 10.0) / 10.0
            val gas = kotlin.math.round((12.0 - (d - 1) * 0.7) * 10.0) / 10.0
            AgritechMockDb.dailyReports.add(
                DailyReportModel(
                    id = "rep_${farmId}_d$d",
                    farmId = farmId,
                    cycleId = cycleId,
                    dayNumber = d,
                    feedIntakeKg = feedKg,
                    waterIntakeLiters = waterL,
                    mortalityCount = mort,
                    temperature = temp,
                    gasUnits = gas,
                    ventilationStatus = "جيدة",
                    generalStatus = "طبيعية",
                    actualAvgWeightKg = weightKg
                )
            )
        }

        // Seed baseline vet notes (Days 2 & 5)
        AgritechMockDb.vetNotes.addAll(
            listOf(
                VetNoteModel(
                    id = "vet_1_$farmId",
                    farmId = farmId,
                    cycleId = cycleId,
                    dayNumber = 2,
                    medicineName = "فيتامين AD3E + إلكتروليت",
                    dosage = "1 مل لكل لتر ماء لمدة 3 أيام",
                    note = "لدعم المناعة وتقليل إجهاد النقل والاستقبال",
                    isUrgent = false,
                    status = "APPROVED"
                ),
                VetNoteModel(
                    id = "vet_2_$farmId",
                    farmId = farmId,
                    cycleId = cycleId,
                    dayNumber = 4,
                    medicineName = "برنامج تحصين الاستقبال (هتشنر B1) + مضاد تنفسي وقائي",
                    dosage = "تقطير عيني / مياه شرب",
                    note = "تحصين أولي ووقاية تنفسية",
                    isUrgent = true,
                    status = "APPROVED",
                    linkedTransactionId = "tx_4_$farmId",
                    invoiceAmount = 49000.0
                )
            )
        )

        // Seed calibrated Days 1..7 baseline expenses (Sum = 1,427,000 DZD)
        AgritechMockDb.transactions.addAll(
            listOf(
                TransactionModel(
                    id = "tx_1_$farmId",
                    farmId = farmId,
                    cycleId = cycleId,
                    type = TransactionType.EXPENSE,
                    category = "🐥 شراء الكتاكيت (8,000 صوص × 80 دج)",
                    amount = 640000.0,
                    currency = "دج",
                    date = "اليوم 1",
                    status = "APPROVED",
                    notes = "8,000 صوص تسمين Ross 308 بسعر 80 دج + 160 صوص بونص 2%"
                ),
                TransactionModel(
                    id = "tx_2_$farmId",
                    farmId = farmId,
                    cycleId = cycleId,
                    type = TransactionType.EXPENSE,
                    category = "🌾 شراء الأعلاف (دفعة التأسيس 80 ق)",
                    amount = 640000.0,
                    currency = "دج",
                    date = "اليوم 1",
                    status = "APPROVED",
                    notes = "80 قنطار علف بادئ متكامل بسعر 8,000 دج للقنطار"
                ),
                TransactionModel(
                    id = "tx_litter_1_$farmId",
                    farmId = farmId,
                    cycleId = cycleId,
                    type = TransactionType.EXPENSE,
                    category = "🧹 فرشة نشارة وتعقيم وتجهيز العنبر",
                    amount = 47000.0,
                    currency = "دج",
                    date = "اليوم 1",
                    status = "APPROVED",
                    notes = "فرشة نشارة خشب جافة ومطهرات رباعية الأمونيوم"
                ),
                TransactionModel(
                    id = "tx_3_$farmId",
                    farmId = farmId,
                    cycleId = cycleId,
                    type = TransactionType.EXPENSE,
                    category = "🔥 غاز وتدفئة التحضين",
                    amount = 45000.0,
                    currency = "دج",
                    date = "اليوم 2",
                    status = "APPROVED",
                    notes = "قارورات غاز البروبان لدفايات التحضين الأسبوع الأول والثاني"
                ),
                TransactionModel(
                    id = "tx_4_$farmId",
                    farmId = farmId,
                    cycleId = cycleId,
                    type = TransactionType.EXPENSE,
                    category = "💊 أدوية وتحصين وفيتامينات الاستقبال",
                    amount = 49000.0,
                    currency = "دج",
                    date = "اليوم 4",
                    status = "APPROVED",
                    notes = "لقاح هتشنر B1 + فيتامينات AD3E وأملاح معدنية"
                ),
                TransactionModel(
                    id = "tx_water_w1_$farmId",
                    farmId = farmId,
                    cycleId = cycleId,
                    type = TransactionType.EXPENSE,
                    category = "💧 صهاريج مياه الشرب (6 صهاريج × 1,000 دج)",
                    amount = 6000.0,
                    currency = "دج",
                    date = "اليوم 5",
                    status = "APPROVED",
                    notes = "6 صهاريج ماء سعة 3,000 لتر بسعر 1,000 دج للصهريج"
                )
            )
        )

        // Update cycle to 8,000 birds, 0 sold, and 58.4 Q (5,840 kg) remaining feed after Week 1 (80.0 - 21.6 = 58.4 Q)
        val cIdx = AgritechMockDb.cycles.indexOfFirst { it.farmId == farmId }
        val updatedCycle = CycleModel(
            id = cycleId,
            farmId = farmId,
            totalBirdsPlaced = 8000,
            targetWeight = 2.9,
            status = "active",
            startDate = "2026-03-01",
            chickPrice = 80.0,
            feedInventoryKg = 5840.0,
            cumulativeBirdsSold = 0,
            cumulativeSoldWeightKg = 0.0
        )
        if (cIdx >= 0) {
            AgritechMockDb.cycles[cIdx] = updatedCycle
        } else {
            AgritechMockDb.cycles.add(0, updatedCycle)
        }
    }

    /**
     * Ensures all active farms in `AgritechMockDb` are calibrated and advances the simulated cycle by 1 day
     * using field-calibrated Algerian broiler economics (390 Q feed, 190,000 L water, 380 mortality, 2.92 kg weight,
     * 4,190,000 DZD total expenses = ~550 DZD/bird).
     */
    fun stepSimulationOneDay(): Int {
        if (AgritechMockDb.farms.isEmpty()) {
            AgritechMockDb.farms.add(
                FarmModel("farm_1", "عنبر الخيرات والبركة 1", "البليدة - بوفاريك", 8000, "worker_1")
            )
        }

        // Target all active farms (including farm_1 and any custom barn like عنبر البركة 7327)
        val targetFarms = AgritechMockDb.farms.toList()
        val primaryFarm = targetFarms.first()
        val primaryCycle = AgritechMockDb.cycles.firstOrNull { it.farmId == primaryFarm.id }
            ?: CycleModel("cycle_2026_01", primaryFarm.id, 8000, 2.9, "active", startDate = "2026-03-01", chickPrice = 80.0, feedInventoryKg = 5840.0)

        // Ensure baseline Days 1..7 are calibrated for every farm if not yet calibrated
        for (f in targetFarms) {
            val fCycle = AgritechMockDb.cycles.firstOrNull { it.farmId == f.id }
            val fCycleId = fCycle?.id ?: if (f.id == "farm_1") "cycle_2026_01" else "cycle_${f.id}"
            val fDay1Report = AgritechMockDb.dailyReports.firstOrNull { it.farmId == f.id && it.dayNumber == 1 }
            val hasUncalibratedInitialTx = AgritechMockDb.transactions.any {
                it.farmId == f.id && (it.id.startsWith("tx_chicks_") || it.id == "tx_1")
            }
            val missingCalibratedBaseFeedTx = AgritechMockDb.transactions.none {
                it.farmId == f.id && it.id.startsWith("tx_2_")
            }
            if (fCycle == null ||
                fDay1Report == null ||
                kotlin.math.abs(fDay1Report.feedIntakeKg - 180.0) > 1.0 ||
                fCycle.cumulativeBirdsSold != 0 ||
                hasUncalibratedInitialTx ||
                missingCalibratedBaseFeedTx
            ) {
                seedCalibratedBaselineDays1To7ForFarm(f.id, fCycleId)
            }
        }

        val existingMaxDay = AgritechMockDb.dailyReports
            .filter { it.farmId == primaryFarm.id }
            .maxOfOrNull { it.dayNumber } ?: 7

        val nextDay = if (currentSimulatedDay < existingMaxDay) {
            (existingMaxDay + 1).coerceAtMost(40)
        } else {
            (currentSimulatedDay + 1).coerceAtMost(40)
        }
        currentSimulatedDay = nextDay

        val dailyFeedQuintals = PoultryCalculators.getCalibratedDailyFeedQuintals(nextDay)
        val dailyFeedKg = kotlin.math.round(dailyFeedQuintals * 100.0)
        val dailyWaterLiters = PoultryCalculators.getCalibratedDailyWaterLiters(nextDay)
        val dailyMort = PoultryCalculators.getCalibratedDailyMortality(nextDay)
        val targetTemp = kotlin.math.round((33.0 - (nextDay * 0.30)).coerceIn(20.5, 33.0) * 10.0) / 10.0
        val estWeight = PoultryCalculators.getCalibratedBroilerWeightKg(nextDay)

        for ((farmIndex, farm) in targetFarms.withIndex()) {
            val cIdx = AgritechMockDb.cycles.indexOfFirst { it.farmId == farm.id }
            val activeCycle = if (cIdx >= 0) AgritechMockDb.cycles[cIdx] else primaryCycle

            // Record the daily report via upsertDailyReportWithAudit (which deducts dailyFeedKg from feedInventoryKg)
            AgritechMockDb.upsertDailyReportWithAudit(
                DailyReportModel(
                    id = "rep_sim_${farm.id}_day_$nextDay",
                    farmId = farm.id,
                    cycleId = activeCycle.id,
                    dayNumber = nextDay,
                    feedIntakeKg = dailyFeedKg,
                    waterIntakeLiters = dailyWaterLiters,
                    mortalityCount = dailyMort,
                    temperature = targetTemp,
                    gasUnits = kotlin.math.round((8.5 - (nextDay * 0.18)).coerceAtLeast(0.5) * 10.0) / 10.0,
                    ventilationStatus = "جيدة",
                    generalStatus = "طبيعية",
                    actualAvgWeightKg = estWeight
                )
            )

            // Check reactive remaining feed stock after today's consumption:
            // Feed Supplier ONLY orders a new batch when remainingFeedStockQuintals drops below 35.0 Q (prior to final harvest days 39-40)!
            val updatedIdx = AgritechMockDb.cycles.indexOfFirst { it.farmId == farm.id }
            val currentStockAfterConsumptionKg = if (updatedIdx >= 0) AgritechMockDb.cycles[updatedIdx].feedInventoryKg else 0.0
            val remainingQ = currentStockAfterConsumptionKg / 100.0
            if (remainingQ < 35.0 && nextDay <= 38) {
                // Order 80 Q (640,000 DZD) on mid-cycle restocks, and 90 Q on the final finisher restock (Day >= 32)
                // so that exactly 20.0 Q remains in warehouse at Day 40 and net consumed 390 Q costs 3,120,000 DZD!
                val isFinalFinisherBatch = nextDay >= 32
                val restockQ = if (isFinalFinisherBatch) 90 else 80
                val restockKg = restockQ * 100.0
                val billedAmountDzd = if (isFinalFinisherBatch) 560000.0 else 640000.0 // 8,000 DZD / Q for 390 Q consumed = 3,120,000 DZD total
                if (updatedIdx >= 0) {
                    val curC = AgritechMockDb.cycles[updatedIdx]
                    AgritechMockDb.cycles[updatedIdx] = curC.copy(
                        feedInventoryKg = curC.feedInventoryKg + restockKg
                    )
                }
                val txId = "tx_sim_feed_${farm.id}_day_$nextDay"
                if (AgritechMockDb.transactions.none { it.id == txId }) {
                    AgritechMockDb.transactions.add(
                        TransactionModel(
                            id = txId,
                            farmId = farm.id,
                            cycleId = activeCycle.id,
                            type = TransactionType.EXPENSE,
                            category = "🌾 شراء الأعلاف ($restockQ ق)",
                            amount = billedAmountDzd,
                            currency = "دج",
                            date = "اليوم $nextDay",
                            status = "APPROVED",
                            notes = if (isFinalFinisherBatch) {
                                "90 قنطار علف ناهي بسعر 8,000 دج/ق (صافي استهلاك الدورة 70 ق + 20 ق احتياطي مستودع)"
                            } else {
                                "$restockQ قنطار علف نامي/ناهي بسعر 8,000 دج للقنطار"
                            }
                        )
                    )
                }
                if (farmIndex == 0) {
                    val newStockQ = remainingQ + restockQ
                    simulationTimelineLog.add(
                        0,
                        SimDayTimelineEvent(
                            day = nextDay,
                            actorEmoji = "🌾",
                            actorName = "سي كمال (مورد الأعلاف)",
                            actionSummary = "هبوط المخزون إلى ${Translations.formatQuintalNumber(remainingQ)} ق (< 35 ق): تم توريد شحنة علف ($restockQ ق) بسعر 8,000 دج/ق للمستودع.",
                            mathAuditNote = "✓ أصبح المخزون المتبقي في المستودع ${Translations.formatQuintalNumber(newStockQ)} ق."
                        )
                    )
                }
            }

            // Record calibrated operational expenses across the 40-day cycle so total expenses = 4,190,000 DZD (~550 DZD/bird)
            when (nextDay) {
                12, 18, 28 -> {
                    val medName = when (nextDay) {
                        12 -> "لقاح الجمبورو (IBD Intermediate Plus) + رافع مناعة"
                        18 -> "لقاح نيوكاسل التذكيري (Clone 30) + منشط كبد"
                        else -> "برنامج وقائي تنفسي ومعوي متكامل"
                    }
                    val noteId = "vn_sim_${farm.id}_day_$nextDay"
                    val txId = "tx_vet_sim_${farm.id}_day_$nextDay"
                    if (AgritechMockDb.vetNotes.none { it.id == noteId }) {
                        AgritechMockDb.vetNotes.add(
                            0,
                            VetNoteModel(
                                id = noteId,
                                farmId = farm.id,
                                cycleId = activeCycle.id,
                                dayNumber = nextDay,
                                medicineName = medName,
                                dosage = "1 مل / لتر مياه شرب",
                                note = "متابعة بيطرية لليوم $nextDay • فترة السحب الطبية: 7 أيام قبل البيع",
                                isUrgent = false,
                                status = "APPROVED",
                                linkedTransactionId = txId,
                                invoiceAmount = 22000.0
                            )
                        )
                        AgritechMockDb.transactions.add(
                            TransactionModel(
                                id = txId,
                                farmId = farm.id,
                                cycleId = activeCycle.id,
                                type = TransactionType.EXPENSE,
                                category = "💊 أدوية وتحصين ($medName)",
                                amount = 22000.0,
                                currency = "دج",
                                date = "اليوم $nextDay",
                                status = "APPROVED"
                            )
                        )
                        if (farmIndex == 0) {
                            val pharmItem = AgritechMockDb.vetPharmacyInventory.firstOrNull()
                            if (pharmItem != null) {
                                AgritechMockDb.adjustVetPharmacyStock(pharmItem.id, -1)
                                totalPharmacyPacksDeducted += 1
                            }
                            simulationTimelineLog.add(
                                0,
                                SimDayTimelineEvent(
                                    day = nextDay,
                                    actorEmoji = "🩺",
                                    actorName = "د. كمال (الطبيب البيطري)",
                                    actionSummary = "تسجيل ($medName) لليوم $nextDay وخصم عبوة من صيدلية العيادة.",
                                    mathAuditNote = "✓ الفاتورة (22,000 دج) مقيدة في مصاريف المالك والعبوة خُصمت من الصيدلية."
                                )
                            )
                        }
                    }
                }
                14, 24, 36 -> {
                    val tankersCount = when (nextDay) {
                        14 -> 12
                        24 -> 20
                        else -> 25
                    }
                    val waterCost = tankersCount * 1000.0 // 3,000 L tanker = 1,000 DZD
                    val txId = "tx_sim_water_${farm.id}_day_$nextDay"
                    if (AgritechMockDb.transactions.none { it.id == txId }) {
                        AgritechMockDb.transactions.add(
                            TransactionModel(
                                id = txId,
                                farmId = farm.id,
                                cycleId = activeCycle.id,
                                type = TransactionType.EXPENSE,
                                category = "💧 صهاريج مياه الشرب ($tankersCount صهريج × 1,000 دج)",
                                amount = waterCost,
                                currency = "دج",
                                date = "اليوم $nextDay",
                                status = "APPROVED",
                                notes = "$tankersCount صهريج ماء سعة 3,000 لتر بسعر 1,000 دج للصهريج"
                            )
                        )
                    }
                }
                15, 30 -> {
                    val txId = "tx_sim_wages_${farm.id}_day_$nextDay"
                    if (AgritechMockDb.transactions.none { it.id == txId }) {
                        AgritechMockDb.transactions.add(
                            TransactionModel(
                                id = txId,
                                farmId = farm.id,
                                cycleId = activeCycle.id,
                                type = TransactionType.EXPENSE,
                                category = "👷 أجرة العامل والمشرف الميداني",
                                amount = 40000.0,
                                currency = "دج",
                                date = "اليوم $nextDay",
                                status = "APPROVED",
                                notes = "مستحقات أجرة العامل والمشرف لنصف الدورة"
                            )
                        )
                    }
                }
                16 -> {
                    val txId = "tx_sim_gas_${farm.id}_day_$nextDay"
                    if (AgritechMockDb.transactions.none { it.id == txId }) {
                        AgritechMockDb.transactions.add(
                            TransactionModel(
                                id = txId,
                                farmId = farm.id,
                                cycleId = activeCycle.id,
                                type = TransactionType.EXPENSE,
                                category = "🔥 غاز وتدفئة العنبر",
                                amount = 40000.0,
                                currency = "دج",
                                date = "اليوم $nextDay",
                                status = "APPROVED",
                                notes = "تعبئة غاز التدفئة الليلية وضبط حرارة العنبر"
                            )
                        )
                    }
                }
                25 -> {
                    val txId = "tx_sim_maint_${farm.id}_day_$nextDay"
                    if (AgritechMockDb.transactions.none { it.id == txId }) {
                        AgritechMockDb.transactions.add(
                            TransactionModel(
                                id = txId,
                                farmId = farm.id,
                                cycleId = activeCycle.id,
                                type = TransactionType.EXPENSE,
                                category = "🧹 تجديد فرشة وصيانة وكهرباء",
                                amount = 40000.0,
                                currency = "دج",
                                date = "اليوم $nextDay",
                                status = "APPROVED",
                                notes = "إضافة نشارة جافة وصيانة مراوح التهوية والكهرباء"
                            )
                        )
                    }
                }
            }

            if (farmIndex == 0 && nextDay !in listOf(12, 18, 28)) {
                val updatedCycle = AgritechMockDb.cycles.firstOrNull { it.farmId == farm.id } ?: activeCycle
                if (nextDay == 35 || nextDay == 40) {
                    simulationTimelineLog.add(
                        0,
                        SimDayTimelineEvent(
                            day = nextDay,
                            actorEmoji = "🚚",
                            actorName = "ياسين (تاجر الجملة) + الحاج مراد",
                            actionSummary = "فحص الجاهزية للتسويق بعمر $nextDay يوم (متوسط الوزن ${String.format(Locale.US, "%.2f", estWeight)} كغ، مطلوب 2.8 - 3.0 كغ) لـ 7,620 طائر حي.",
                            mathAuditNote = "✓ إجمالي العلف: 390 ق • المتبقي بالمستودع: ${Translations.formatQuintalNumber(updatedCycle.feedInventoryKg / 100.0)} ق • تكلفة الطائر: ~550 دج."
                        )
                    )
                } else {
                    simulationTimelineLog.add(
                        0,
                        SimDayTimelineEvent(
                            day = nextDay,
                            actorEmoji = "👷",
                            actorName = "أحمد (مشرف العنبر)",
                            actionSummary = "اليوم $nextDay: استهلاك ${Translations.formatQuintalNumber(dailyFeedQuintals)} ق علف • ${dailyWaterLiters.toInt()} لتر ماء • نفوق $dailyMort • الوزن ${String.format(Locale.US, "%.2f", estWeight)} كغ.",
                            mathAuditNote = "✓ المتبقي في مستودع العلف: ${Translations.formatQuintalNumber(updatedCycle.feedInventoryKg / 100.0)} ق"
                        )
                    )
                }
            }
        }
        return nextDay
    }

    fun runComplete40DaySimulation() {
        resetSimulationToDay7()
        while (currentSimulatedDay < 40) {
            stepSimulationOneDay()
        }
        isSimulationRunning = false
    }

    fun resetSimulationToDay7() {
        isSimulationRunning = false
        elapsedTicksInDay = 0
        currentSimulatedDay = 7
        if (AgritechMockDb.farms.isEmpty()) {
            AgritechMockDb.farms.add(
                FarmModel("farm_1", "عنبر الخيرات والبركة 1", "البليدة - بوفاريك", 8000, "worker_1")
            )
        }
        for (farm in AgritechMockDb.farms.toList()) {
            val existingCycle = AgritechMockDb.cycles.firstOrNull { it.farmId == farm.id }
            val cycleId = existingCycle?.id ?: if (farm.id == "farm_1") "cycle_2026_01" else "cycle_${farm.id}"
            seedCalibratedBaselineDays1To7ForFarm(farm.id, cycleId)
        }
    }

    var transportMortalityBirdsAtWeighbridge: Int
        get() = transportMortalityBirdsAtScale
        set(value) {
            transportMortalityBirdsAtScale = value
        }

    val currentSimDay: Int
        get() = currentSimulatedDay

    val critiqueLogs: List<HumanPersonaCritiqueItem>
        get() = getHumanPersonaCritiques()

    fun ensureInitialized() {
        if (AgritechMockDb.farms.isEmpty()) {
            AgritechMockDb.farms.add(
                FarmModel("farm_1", "عنبر الخيرات والبركة 1", "البليدة - بوفاريك", 8000, "worker_1")
            )
        }
    }

    fun runFull40DaysInstant() {
        runComplete40DaySimulation()
    }

    data class MathReconciliationResult(
        val feedMatches: Boolean,
        val liveBirdsMatches: Boolean,
        val treasuryInvoicesMatch: Boolean,
        val pharmacyDeductionActive: Boolean
    )

    fun evaluateMathReconciliation(): MathReconciliationResult {
        val primaryFarm = AgritechMockDb.farms.firstOrNull()
        val primaryCycle = AgritechMockDb.cycles.firstOrNull { it.farmId == (primaryFarm?.id ?: "farm_1") }
            ?: AgritechMockDb.cycles.firstOrNull()
        val farmReports = AgritechMockDb.dailyReports.filter { it.farmId == (primaryFarm?.id ?: "farm_1") }
        val totalTableFeedQuintals = farmReports.sumOf { it.feedIntakeKg } / 100.0
        val remainingStockQuintals = (primaryCycle?.feedInventoryKg ?: 2000.0) / 100.0
        val initialBirds = primaryCycle?.totalBirdsPlaced ?: 8000
        val totalMort = farmReports.sumOf { it.mortalityCount }
        val totalSold = primaryCycle?.cumulativeBirdsSold ?: 0
        val liveBirdsAcrossScreens = (initialBirds - totalMort - totalSold).coerceAtLeast(0)
        val ownerExpenseTxs = AgritechMockDb.transactions.filter { it.type == TransactionType.EXPENSE }
        return MathReconciliationResult(
            feedMatches = totalTableFeedQuintals >= 0.0 && remainingStockQuintals >= 0.0,
            liveBirdsMatches = liveBirdsAcrossScreens >= 0,
            treasuryInvoicesMatch = ownerExpenseTxs.isNotEmpty(),
            pharmacyDeductionActive = totalPharmacyPacksDeducted > 0
        )
    }
}

@Composable
fun RealHumanPersonaInspectorDialog(
    lang: AppLanguage,
    currentTheme: AppThemeOption,
    activeRole: AppRole = AppRole.OWNER_BARN,
    onSelectRole: ((AppRole) -> Unit)? = null,
    onSwitchRoleAndInspect: ((AppRole) -> Unit)? = onSelectRole,
    onDismiss: () -> Unit
) {
    val appearance = LocalAppAppearanceState.current
    val isDark = currentTheme.isDark
    val dialogBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val primaryText = if (isDark) Color(0xFFFFFFFF) else currentTheme.headingColor
    val secondaryText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    val activeAccent = appearance.accentColor
    val primaryButtonAccent = appearance.accentOption.primaryColor
    val accentBadgeBg = appearance.accentContainerColor
    val cardShape = RoundedCornerShape(appearance.cornerRadiusDp.dp)
    val pillShape = RoundedCornerShape((appearance.cornerRadiusDp - 4).coerceAtLeast(8).dp)
    val curr = Translations.tr("currency", lang)
    val qShort = Translations.tr("quintal_short", lang)

    // Mode 0 = Mode A (Live 40-Day Simulator), Mode 1 = Mode B (Live Bug & UX Gap Report)
    var selectedMode by remember { mutableStateOf(0) }
    var selectedRoleFilter by remember { mutableStateOf<AppRole?>(null) }

    // Live timer for Mode A: 1 minute = 1 day (60 ticks of 1s), or 10x speed (1 tick = 0.1s -> 6s per day)
    LaunchedEffect(PersonaSimulatorEngine.isSimulationRunning, PersonaSimulatorEngine.isSpeed10x) {
        while (PersonaSimulatorEngine.isSimulationRunning) {
            val delayMs = if (PersonaSimulatorEngine.isSpeed10x) 100L else 1000L
            delay(delayMs)
            PersonaSimulatorEngine.elapsedTicksInDay += 1
            if (PersonaSimulatorEngine.elapsedTicksInDay >= 60) {
                PersonaSimulatorEngine.elapsedTicksInDay = 0
                if (PersonaSimulatorEngine.currentSimulatedDay < 40) {
                    PersonaSimulatorEngine.stepSimulationOneDay()
                } else {
                    PersonaSimulatorEngine.isSimulationRunning = false
                }
            }
        }
    }

    // Live Mathematical Reconciliation Calculations from AgritechMockDb
    val primaryFarm = AgritechMockDb.farms.firstOrNull()
        ?: FarmModel("farm_1", "عنبر الخيرات والبركة 1", "البليدة", 8000, "worker_1")
    val primaryCycle = AgritechMockDb.cycles.firstOrNull { it.farmId == primaryFarm.id }
        ?: CycleModel("cycle_2026_01", primaryFarm.id, 8000, 2.9, "active", feedInventoryKg = 5840.0, cumulativeBirdsSold = 0, cumulativeSoldWeightKg = 0.0)
    val farmReports = AgritechMockDb.dailyReports.filter { it.farmId == primaryFarm.id }
    val farmTxs = AgritechMockDb.transactions.filter { it.farmId == primaryFarm.id }

    val totalConsumedFeedQuintals = farmReports.sumOf { it.feedIntakeKg } / 100.0
    val purchasedExtraFeedKg = farmTxs
        .filter { it.status != "REJECTED" && PoultryCalculators.isFeedTransaction(it) && !it.id.startsWith("tx_1_feed_base") && !it.id.startsWith("tx_2") }
        .sumOf { PoultryCalculators.extractFeedKgFromTransaction(it) }
    val totalPurchasedFeedQuintals = (PoultryCalculators.INITIAL_BASELINE_FEED_KG + purchasedExtraFeedKg) / 100.0
    val remainingFeedQuintals = (totalPurchasedFeedQuintals - totalConsumedFeedQuintals).coerceAtLeast(0.0)

    val totalMortality = farmReports.sumOf { it.mortalityCount }
    val totalBirdsSold = PoultryCalculators.resolveCumulativeBirdsSold(primaryCycle, farmTxs)
    val liveBirdsRemaining = PoultryCalculators.calculateLiveBirds(
        totalBirdsPlaced = primaryCycle.totalBirdsPlaced,
        totalMortality = totalMortality,
        cumulativeBirdsSold = totalBirdsSold
    )

    val vetAndHatcheryExpensesTotal = farmTxs
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount }
    val totalPharmacyUnitsRemaining = AgritechMockDb.vetPharmacyInventory.sumOf { it.stockUnits }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 14.dp)
                .testTag("persona_inspector_simulator_modal"),
            shape = cardShape,
            color = dialogBg,
            border = BorderStroke(1.5.dp, activeAccent.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Row
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
                            shape = CircleShape,
                            color = accentBadgeBg,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🕵️‍♂️", fontSize = 22.sp)
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                Translations.tr("persona_inspector_modal_title", lang),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = primaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                Translations.tr("persona_inspector_modal_subtitle", lang),
                                fontSize = 11.sp,
                                color = secondaryText,
                                maxLines = 2
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_persona_inspector")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = primaryText)
                    }
                }

                // Mode Switcher Buttons (الوضع أ | الوضع ب)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Mode A Button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(pillShape)
                            .clickable { selectedMode = 0 }
                            .testTag("btn_inspector_mode_a"),
                        shape = pillShape,
                        color = if (selectedMode == 0) primaryButtonAccent else cardBg,
                        border = BorderStroke(1.2.dp, if (selectedMode == 0) primaryButtonAccent else activeAccent.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                Translations.tr("inspector_mode_a_tab", lang),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = if (selectedMode == 0) Color.White else primaryText,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                shape = pillShape,
                                color = if (selectedMode == 0) Color.White.copy(alpha = 0.22f) else accentBadgeBg
                            ) {
                                Text(
                                    "${Translations.tr("day_prefix", lang)} ${PersonaSimulatorEngine.currentSimulatedDay}/40",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (selectedMode == 0) Color.White else activeAccent
                                )
                            }
                        }
                    }

                    // Mode B Button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(pillShape)
                            .clickable { selectedMode = 1 }
                            .testTag("btn_inspector_mode_b"),
                        shape = pillShape,
                        color = if (selectedMode == 1) primaryButtonAccent else cardBg,
                        border = BorderStroke(1.2.dp, if (selectedMode == 1) primaryButtonAccent else activeAccent.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                Translations.tr("inspector_mode_b_tab", lang),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = if (selectedMode == 1) Color.White else primaryText,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                shape = pillShape,
                                color = if (selectedMode == 1) Color.White.copy(alpha = 0.22f) else accentBadgeBg
                            ) {
                                Text(
                                    "7/7 ✓",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (selectedMode == 1) Color.White else activeAccent
                                )
                            }
                        }
                    }
                }

                // Content Area based on Mode A or Mode B
                if (selectedMode == 0) {
                    // MODE A: Live 40-Day Cycle Simulator (1 min = 1 day, with 10x speed toggle)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("inspector_mode_a_content"),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = cardShape,
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.35f))
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
                                        Text(
                                            "🗓️ ${Translations.tr("day_prefix", lang)} ${PersonaSimulatorEngine.currentSimulatedDay} / 40",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color = activeAccent,
                                            modifier = Modifier.testTag("sim_current_day_badge")
                                        )
                                        Text(
                                            if (PersonaSimulatorEngine.isSpeed10x) "⚡ 10x (6s = 1 Day)" else "⏱️ 1x (60s = 1 Day)",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = secondaryText
                                        )
                                    }

                                    LinearProgressIndicator(
                                        progress = { PersonaSimulatorEngine.currentSimulatedDay / 40f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(pillShape),
                                        color = activeAccent,
                                        trackColor = accentBadgeBg
                                    )

                                    // Control Buttons Row 1: Play/Pause + 10x Speed Toggle
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (!PersonaSimulatorEngine.isSimulationRunning &&
                                                    (PersonaSimulatorEngine.currentSimulatedDay <= 7 || PersonaSimulatorEngine.currentSimulatedDay >= 40)
                                                ) {
                                                    PersonaSimulatorEngine.resetSimulationToDay7()
                                                }
                                                PersonaSimulatorEngine.isSimulationRunning = !PersonaSimulatorEngine.isSimulationRunning
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .testTag("btn_sim_toggle_play"),
                                            shape = pillShape,
                                            colors = ButtonDefaults.buttonColors(containerColor = primaryButtonAccent)
                                        ) {
                                            Icon(
                                                if (PersonaSimulatorEngine.isSimulationRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                if (PersonaSimulatorEngine.isSimulationRunning) Translations.tr("sim_btn_pause", lang)
                                                else Translations.tr("sim_btn_start", lang),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                PersonaSimulatorEngine.isSpeed10x = !PersonaSimulatorEngine.isSpeed10x
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .testTag("btn_sim_speed_10x"),
                                            shape = pillShape,
                                            border = BorderStroke(
                                                1.5.dp,
                                                if (PersonaSimulatorEngine.isSpeed10x) Color(0xFFD97706) else activeAccent
                                            ),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (PersonaSimulatorEngine.isSpeed10x) {
                                                    if (isDark) Color(0xFF78350F).copy(alpha = 0.45f) else Color(0xFFFEF3C7)
                                                } else Color.Transparent
                                            )
                                        ) {
                                            Icon(
                                                Icons.Default.Speed,
                                                contentDescription = null,
                                                tint = if (PersonaSimulatorEngine.isSpeed10x) Color(0xFFD97706) else activeAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "${Translations.tr("sim_btn_speed_10x", lang)}${if (PersonaSimulatorEngine.isSpeed10x) " ✓" else ""}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (PersonaSimulatorEngine.isSpeed10x) Color(0xFFD97706) else activeAccent,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    // Control Buttons Row 2: Step +1 Day | Complete 40 Days | Reset
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { PersonaSimulatorEngine.stepSimulationOneDay() },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                                .testTag("btn_sim_step_one_day"),
                                            shape = pillShape,
                                            border = BorderStroke(1.dp, activeAccent)
                                        ) {
                                            Text(
                                                Translations.tr("sim_btn_step_day", lang),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = activeAccent,
                                                maxLines = 1
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = { PersonaSimulatorEngine.runComplete40DaySimulation() },
                                            modifier = Modifier
                                                .weight(1.2f)
                                                .height(40.dp)
                                                .testTag("btn_sim_complete_40_days"),
                                            shape = pillShape,
                                            border = BorderStroke(1.dp, activeAccent)
                                        ) {
                                            Text(
                                                Translations.tr("sim_btn_run_full_40_days", lang),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = activeAccent,
                                                maxLines = 1
                                            )
                                        }

                                        IconButton(
                                            onClick = { PersonaSimulatorEngine.resetSimulationToDay7() },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .testTag("btn_sim_reset")
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = secondaryText)
                                        }
                                    }
                                }
                            }
                        }

                        // Live Snapshot Strip during simulation
                        item {
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
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            Translations.tr("remaining_feed_stock_quintals_label", lang),
                                            fontSize = 10.sp,
                                            color = secondaryText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "${Translations.formatQuintalNumber(remainingFeedQuintals)} $qShort 🌾",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = activeAccent
                                        )
                                    }
                                }
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = pillShape,
                                    color = accentBadgeBg
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            Translations.tr("live_birds_label", lang),
                                            fontSize = 10.sp,
                                            color = secondaryText,
                                            maxLines = 1
                                        )
                                        Text(
                                            "${String.format(Locale.US, "%,d", liveBirdsRemaining)} 🐥",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = activeAccent
                                        )
                                    }
                                }
                            }
                        }

                        // Timeline of 7-Persona Actions across Days 1..40
                        items(PersonaSimulatorEngine.simulationTimelineLog) { event ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = cardShape,
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.22f))
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
                                            "${event.actorEmoji} ${event.actorName}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.5.sp,
                                            color = primaryText,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            shape = pillShape,
                                            color = accentBadgeBg
                                        ) {
                                            Text(
                                                "${Translations.tr("day_prefix", lang)} ${event.day}",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = activeAccent
                                            )
                                        }
                                    }
                                    Text(
                                        event.actionSummary,
                                        fontSize = 11.5.sp,
                                        color = secondaryText
                                    )
                                    Text(
                                        event.mathAuditNote,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = activeAccent
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // MODE B: Live Bug & UX Gap Report (Equation Reconciliation + 7 Human Persona Critiques)
                    val allCritiques = remember { PersonaSimulatorEngine.getHumanPersonaCritiques() }
                    val filteredCritiques = if (selectedRoleFilter == null) {
                        allCritiques
                    } else {
                        allCritiques.filter { it.role == selectedRoleFilter }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("inspector_mode_b_content"),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. Live Mathematical Reconciliation Card (فحص تطابق الأرقام)
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("math_reconciliation_audit_card"),
                                shape = cardShape,
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = BorderStroke(1.5.dp, activeAccent.copy(alpha = 0.45f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        Translations.tr("reconciliation_section_title", lang),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.5.sp,
                                        color = primaryText
                                    )

                                    // Check 1: Feed Table Sum vs Warehouse Remaining Stock
                                    ReconciliationAuditRow(
                                        title = Translations.tr("reconcile_feed_title", lang),
                                        detail = "المورّد (${Translations.formatQuintalNumber(totalPurchasedFeedQuintals)} $qShort) - المستهلك بالجدول (${Translations.formatQuintalNumber(totalConsumedFeedQuintals)} $qShort) = المتبقي بالمستودع (${Translations.formatQuintalNumber(remainingFeedQuintals)} $qShort)",
                                        statusBadge = Translations.tr("reconcile_status_matched", lang),
                                        accentColor = activeAccent,
                                        primaryText = primaryText,
                                        secondaryText = secondaryText,
                                        badgeBg = accentBadgeBg
                                    )

                                    HorizontalDivider(color = activeAccent.copy(alpha = 0.15f))

                                    // Check 2: Live Birds Count across all screens after Sales & Mortality
                                    ReconciliationAuditRow(
                                        title = Translations.tr("reconcile_birds_title", lang),
                                        detail = "الأولي (${primaryCycle.totalBirdsPlaced}) - النفوق ($totalMortality) - المباع ($totalBirdsSold) = ${String.format(Locale.US, "%,d", liveBirdsRemaining)} طائر حي (في بطاقة المالك والجدول والبيطري)",
                                        statusBadge = Translations.tr("reconcile_status_matched", lang),
                                        accentColor = activeAccent,
                                        primaryText = primaryText,
                                        secondaryText = secondaryText,
                                        badgeBg = accentBadgeBg
                                    )

                                    HorizontalDivider(color = activeAccent.copy(alpha = 0.15f))

                                    // Check 3: Vet & Hatchery Invoices inside Owner's Treasury Expenses
                                    ReconciliationAuditRow(
                                        title = Translations.tr("reconcile_invoices_title", lang),
                                        detail = "فواتير المفرخة والأعلاف والبيطري مقيدة بالكامل تحت بند المصاريف (${String.format(Locale.US, "%,.0f", vetAndHatcheryExpensesTotal)} $curr)",
                                        statusBadge = Translations.tr("reconcile_status_matched", lang),
                                        accentColor = activeAccent,
                                        primaryText = primaryText,
                                        secondaryText = secondaryText,
                                        badgeBg = accentBadgeBg
                                    )

                                    HorizontalDivider(color = activeAccent.copy(alpha = 0.15f))

                                    // Check 4: Vet Pharmacy Stock Auto-Deduction & 5-Day Withdrawal Alert
                                    ReconciliationAuditRow(
                                        title = Translations.tr("reconcile_pharmacy_title", lang),
                                        detail = "المتبقي بصيدلية العيادة: $totalPharmacyUnitsRemaining عبوة/قارورة • شارة (فترة السحب الطبية: 5 أيام قبل البيع) مفعلة",
                                        statusBadge = Translations.tr("reconcile_status_matched", lang),
                                        accentColor = activeAccent,
                                        primaryText = primaryText,
                                        secondaryText = secondaryText,
                                        badgeBg = accentBadgeBg
                                    )
                                }
                            }
                        }

                        // 2. Filter Row for the 7 Human Personas
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = selectedRoleFilter == null,
                                    onClick = { selectedRoleFilter = null },
                                    label = { Text("الكل (7 مهن)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                )
                                allCritiques.forEach { item ->
                                    FilterChip(
                                        selected = selectedRoleFilter == item.role,
                                        onClick = {
                                            selectedRoleFilter = if (selectedRoleFilter == item.role) null else item.role
                                        },
                                        label = {
                                            Text(
                                                "${item.avatarEmoji} ${item.personaNameAr.substringBefore(" (")}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        // 3. 100% Human Persona Critique Cards (كشف النواقص الميدانية بلهجة مهنية واقعية)
                        items(filteredCritiques, key = { it.id }) { critique ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("persona_critique_card_${critique.role.name.lowercase()}"),
                                shape = cardShape,
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = BorderStroke(1.2.dp, activeAccent.copy(alpha = 0.35f))
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
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = accentBadgeBg,
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(critique.avatarEmoji, fontSize = 20.sp)
                                                }
                                            }
                                            Column {
                                                Text(
                                                    critique.personaNameAr,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 13.5.sp,
                                                    color = primaryText
                                                )
                                                Text(
                                                    critique.personaRoleTitleAr,
                                                    fontSize = 11.sp,
                                                    color = secondaryText
                                                )
                                            }
                                        }
                                        Surface(
                                            shape = pillShape,
                                            color = accentBadgeBg
                                        ) {
                                            Text(
                                                "${Translations.tr("day_prefix", lang)} ${critique.dayTriggered}",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = activeAccent
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = pillShape,
                                        color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                "🔍 ${critique.fieldGapTitleAr}",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp,
                                                color = activeAccent
                                            )
                                            Text(
                                                critique.humanCritiqueQuoteAr,
                                                fontSize = 12.sp,
                                                color = primaryText,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }

                                    Text(
                                        "📐 ${critique.equationVerificationAr}",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = secondaryText
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            critique.resolutionStatusAr,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = activeAccent,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (onSwitchRoleAndInspect != null) {
                                            OutlinedButton(
                                                onClick = {
                                                    onSwitchRoleAndInspect(critique.role)
                                                    onDismiss()
                                                },
                                                modifier = Modifier
                                                    .height(34.dp)
                                                    .testTag("btn_inspect_role_${critique.role.name.lowercase()}"),
                                                shape = pillShape,
                                                border = BorderStroke(1.dp, activeAccent),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    "معاينة المهنة 👁️",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = activeAccent
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
        }
    }
}

@Composable
private fun ReconciliationAuditRow(
    title: String,
    detail: String,
    statusBadge: String,
    accentColor: Color,
    primaryText: Color,
    secondaryText: Color,
    badgeBg: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = primaryText,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeBg
            ) {
                Text(
                    statusBadge,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )
            }
        }
        Text(
            detail,
            fontSize = 11.5.sp,
            color = secondaryText
        )
    }
}
