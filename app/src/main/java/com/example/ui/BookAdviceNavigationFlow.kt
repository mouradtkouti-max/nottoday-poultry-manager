package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.ui.zIndex
import com.example.BookBranchModel
import com.example.BookSubCardModel
import com.example.models.AppThemeOption
import com.example.models.InfographicVisualType
import com.example.models.Ross308PosterCatalog
import com.example.utils.AppLanguage
import com.example.utils.Ross308BookPageTranslations
import com.example.utils.SaasPreferencesManager
import com.example.utils.Translations
import kotlin.math.absoluteValue

enum class AdviceScreenStep {
    STEP_1_BOOKS_SHELF,
    STEP_2_BOOK_INDEX,
    STEP_3_LESSON_PAGE
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookAdviceNavigationFlow(
    branches: List<BookBranchModel>,
    activeUserUid: String,
    lang: AppLanguage,
    currentTheme: AppThemeOption,
    accentColor: Color,
    accentPrimaryBtn: Color,
    accentContainer: Color,
    cornerRadiusDp: Int,
    modifier: Modifier = Modifier
) {
    if (branches.isEmpty()) return

    val context = LocalContext.current
    val isDark = currentTheme.isDark
    val primaryText = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val secondaryText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    val shelfCardBg = if (isDark) Color(0xFF1E293B) else BookParchmentLight
    val shelfCardBorder = if (isDark) accentColor.copy(alpha = 0.55f) else BookBorderOuter
    val safeCorner = cornerRadiusDp.coerceIn(8, 24)

    var currentStep by rememberSaveable { mutableStateOf(AdviceScreenStep.STEP_1_BOOKS_SHELF) }
    var selectedBranchIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedLessonIndex by rememberSaveable { mutableIntStateOf(0) }

    val safeBranchIdx = selectedBranchIndex.coerceIn(0, (branches.size - 1).coerceAtLeast(0))
    val activeBranch = branches.getOrElse(safeBranchIdx) { branches.first() }
    val activeBranchLessons = activeBranch.subCards

    var lastReadLessonId by remember {
        mutableStateOf(
            SaasPreferencesManager.getLastReadLessonId(context, safeBranchIdx, activeUserUid)
        )
    }

    LaunchedEffect(safeBranchIdx, activeUserUid, currentStep) {
        lastReadLessonId = SaasPreferencesManager.getLastReadLessonId(context, safeBranchIdx, activeUserUid)
    }

    fun resolveBranchTitle(idx: Int): String {
        val key = if (idx == 3) "book_branch_3_title_display" else "book_branch_${idx}_title"
        return Translations.tr(key, lang)
    }

    val localizedBranchTitle = resolveBranchTitle(safeBranchIdx)
    val layoutDir = if (lang.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
        // الخطوة 1: شاشة الكتب الأربعة فقط (2x2 Grid) تظهر دائماً داخل التبويب دون أن يظهر تحتها فهرس أو درس
        Column(
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 4.dp)
                .testTag("book_step1_shelf_screen"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("book_branches_2x2_grid"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                branches.chunked(2).forEach { rowBranches ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowBranches.forEach { branch ->
                            val idx = branch.branchIndex
                            val cardTitle = resolveBranchTitle(idx)
                            val cardDesc = Translations.tr("book_branch_${idx}_shelf_desc", lang)
                            val lessonsCountBadge = Translations.tr("book_4_lessons_badge_fmt", lang)
                                .replace("{count}", branch.subCards.size.toString())

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .defaultMinSize(minHeight = 168.dp)
                                    .clip(RoundedCornerShape(safeCorner.dp))
                                    .clickable {
                                        selectedBranchIndex = idx
                                        currentStep = AdviceScreenStep.STEP_2_BOOK_INDEX
                                    }
                                    .testTag(branch.tag),
                                shape = RoundedCornerShape(safeCorner.dp),
                                color = shelfCardBg,
                                border = BorderStroke(1.5.dp, shelfCardBorder),
                                shadowElevation = 4.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(accentColor)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    VintageBookShelfCoverEmblem(
                                        branchIndex = idx,
                                        accentColor = accentColor
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = cardTitle,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isDark) primaryText else BookBurgundyDark,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 17.sp
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = cardDesc,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = secondaryText,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 14.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isDark) accentColor.copy(alpha = 0.22f) else BookParchmentMid,
                                        border = BorderStroke(1.dp, if (isDark) accentColor else BookGoldAccent)
                                    ) {
                                        Text(
                                            text = "📖 $lessonsCountBadge",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isDark) primaryText else BookBurgundy,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // الخطوة 2 والخطوة 3: نافذة غامرة بملء الشاشة الكاملة (Full-Screen Immersive Overlay)
        // تخفي ترويسة التطبيق، شريط اختيار العنبر، التبويبات الفرعية، وشريط التنقل السفلي 100%
        if (currentStep != AdviceScreenStep.STEP_1_BOOKS_SHELF) {
            Dialog(
                onDismissRequest = {
                    if (currentStep == AdviceScreenStep.STEP_3_LESSON_PAGE) {
                        currentStep = AdviceScreenStep.STEP_2_BOOK_INDEX
                    } else {
                        currentStep = AdviceScreenStep.STEP_1_BOOKS_SHELF
                    }
                },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BookParchmentDeep)
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                ) {
                    when (currentStep) {
                        AdviceScreenStep.STEP_2_BOOK_INDEX -> {
                            BackHandler {
                                currentStep = AdviceScreenStep.STEP_1_BOOKS_SHELF
                            }

                            val shortBookTitle = Translations.tr("book_index_short_$safeBranchIdx", lang)
                            val indexHeaderText = Translations.tr("book_index_header_fmt", lang)
                                .replace("{book}", shortBookTitle)
                                .replace("{title}", shortBookTitle)
                                .replace("{count}", activeBranchLessons.size.toString())

                            CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
                                BookIndexImmersiveSheet(
                                    indexHeaderText = indexHeaderText,
                                    activeBranchLessons = activeBranchLessons,
                                    lastReadLessonId = lastReadLessonId,
                                    lang = lang,
                                    onBackToBooks = {
                                        currentStep = AdviceScreenStep.STEP_1_BOOKS_SHELF
                                    },
                                    onOpenLesson = { idx, lesson ->
                                        selectedLessonIndex = idx
                                        SaasPreferencesManager.saveLastReadLessonId(
                                            context = context,
                                            branchIndex = safeBranchIdx,
                                            lessonId = lesson.id,
                                            userUid = activeUserUid
                                        )
                                        lastReadLessonId = lesson.id
                                        currentStep = AdviceScreenStep.STEP_3_LESSON_PAGE
                                    }
                                )
                            }
                        }

                        AdviceScreenStep.STEP_3_LESSON_PAGE -> {
                            BackHandler {
                                currentStep = AdviceScreenStep.STEP_2_BOOK_INDEX
                            }

                            val safeInitialPage = selectedLessonIndex.coerceIn(
                                0,
                                (activeBranchLessons.size - 1).coerceAtLeast(0)
                            )
                            val pagerState = rememberPagerState(
                                initialPage = safeInitialPage,
                                pageCount = { activeBranchLessons.size.coerceAtLeast(1) }
                            )

                            LaunchedEffect(pagerState.currentPage, safeBranchIdx, activeUserUid) {
                                val currentLesson = activeBranchLessons.getOrNull(pagerState.currentPage)
                                if (currentLesson != null) {
                                    selectedLessonIndex = pagerState.currentPage
                                    SaasPreferencesManager.saveLastReadLessonId(
                                        context = context,
                                        branchIndex = safeBranchIdx,
                                        lessonId = currentLesson.id,
                                        userUid = activeUserUid
                                    )
                                    lastReadLessonId = currentLesson.id
                                }
                            }

                            CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(BookParchmentDeep)
                                        .padding(6.dp)
                                        .testTag("book_step3_lesson_screen")
                                ) {
                                    HorizontalPager(
                                        state = pagerState,
                                        userScrollEnabled = true,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .testTag("book_pages_horizontal_pager"),
                                        pageSpacing = 12.dp
                                    ) { pageIdx ->
                                        val pageCard: BookSubCardModel =
                                            activeBranchLessons.getOrElse(pageIdx) { activeBranchLessons.first() }
                                        val resolvedSpec =
                                            pageCard.posterSpec ?: Ross308PosterCatalog.forSubCard(pageCard.id)
                                        val pageScrollState = rememberScrollState()
                                        val pageOffset =
                                            (pagerState.currentPage - pageIdx) + pagerState.currentPageOffsetFraction

                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer {
                                                    cameraDistance = 16f * density
                                                    rotationY = (-pageOffset * 28f).coerceIn(-38f, 38f)
                                                    val absOffset = pageOffset.absoluteValue.coerceIn(0f, 1f)
                                                    val scaleFactor = 1f - (absOffset * 0.06f)
                                                    scaleX = scaleFactor
                                                    scaleY = scaleFactor
                                                    alpha = 1f - (absOffset * 0.22f)
                                                }
                                                .verticalScroll(pageScrollState)
                                        ) {
                                            if (resolvedSpec != null) {
                                                VintageSpiralBookPageContainer(
                                                    detailCard = pageCard,
                                                    spec = resolvedSpec,
                                                    pageNumber = pageIdx + 1,
                                                    totalPages = activeBranchLessons.size.coerceAtLeast(4),
                                                    branchLessons = activeBranchLessons,
                                                    onSelectPageInBranch = { /* Synced automatically via swipe */ },
                                                    specialtyTitle = localizedBranchTitle,
                                                    onBackToIndex = {
                                                        currentStep = AdviceScreenStep.STEP_2_BOOK_INDEX
                                                    },
                                                    lang = lang,
                                                    currentTheme = currentTheme,
                                                    accentColor = accentColor,
                                                    accentPrimaryBtn = accentPrimaryBtn,
                                                    accentContainer = accentContainer,
                                                    cornerRadiusDp = safeCorner
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        AdviceScreenStep.STEP_1_BOOKS_SHELF -> Unit
                    }
                }
            }
        }
    }
}

/**
 * Step 2: Full-Screen Vintage Notebook Index matching Reference Image 1 (1794636566946_c55a1f82.jpg).
 * - Metallic spiral rings along top edge and binding side edge
 * - Clean minimal top back arrow (48dp)
 * - Ornate scroll header banner ("فهرس: التغذية والأوزان (4 دروس)")
 * - 4 clean horizontal lesson cards: Circle Art (+ "🔖 توقفت هنا" gold bookmark ribbon) | Bold 2-line title | Inline "إبدأ القراءة 📖 <" CTA
 */
@Composable
private fun BookIndexImmersiveSheet(
    indexHeaderText: String,
    activeBranchLessons: List<BookSubCardModel>,
    lastReadLessonId: String?,
    lang: AppLanguage,
    onBackToBooks: () -> Unit,
    onOpenLesson: (Int, BookSubCardModel) -> Unit
) {
    val pageDir = if (lang.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    val ctaArrow = if (lang.isRtl) "‹" else "›"

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp)
            .testTag("book_step2_index_screen"),
        shape = RoundedCornerShape(14.dp),
        color = BookParchmentDeep,
        border = BorderStroke(2.dp, BookBorderOuter),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .background(BookParchmentLight, RoundedCornerShape(10.dp))
                .border(1.2.dp, BookBorderInner, RoundedCornerShape(10.dp))
        ) {
            // Top Metallic Spiral Binding Rings across the top of the notebook index sheet
            TopSpiralBindingRingsCanvas(
                ringCount = 12,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )

            // Spine rings on binding edge (Right for RTL Arabic matching Reference Image 1; Left for LTR)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (!lang.isRtl) {
                        SpiralBindingRingsCanvas(
                            ringCount = 9,
                            onRightEdge = false,
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(vertical = 8.dp)
                        )
                    }

                    CompositionLocalProvider(LocalLayoutDirection provides pageDir) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Top row: Clean back arrow button ONLY (no giant pill clutter)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                IconButton(
                                    onClick = onBackToBooks,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("btn_back_to_four_books")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = Translations.tr("book_btn_back_to_4_books", lang),
                                        tint = BookBurgundyDark
                                    )
                                }
                            }

                            // Ornate Scroll Header Banner ("فهرس: التغذية والأوزان (4 دروس)")
                            VintageScrollIndexBanner(
                                text = indexHeaderText,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp)
                                    .testTag("book_index_scroll_banner")
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            // 4 Sleek Single-Row Lesson Cards (NO coreIdea paragraph, NO giant orange button below)
                            activeBranchLessons.forEachIndexed { idx, lesson ->
                                val rawSpec = lesson.posterSpec ?: Ross308PosterCatalog.forSubCard(lesson.id)
                                val (localizedLesson, localizedSpec) = remember(lesson, rawSpec, lang) {
                                    if (rawSpec != null) {
                                        Ross308BookPageTranslations.localizeSubCardAndSpec(lesson, rawSpec, lang)
                                    } else {
                                        lesson to rawSpec
                                    }
                                }
                                val isStoppedHere = lastReadLessonId == lesson.id
                                val lessonNumberStr = String.format(java.util.Locale.US, "%02d", idx + 1)
                                val lessonHeading = if (lang == AppLanguage.AR) {
                                    "الدرس $lessonNumberStr: ${localizedLesson.title}"
                                } else {
                                    "${Translations.tr("book_lesson_number_fmt", lang).replace("{num}", lessonNumberStr)}: ${localizedLesson.title}"
                                }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 92.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { onOpenLesson(idx, lesson) }
                                        .testTag("book_index_lesson_${lesson.id}"),
                                    shape = RoundedCornerShape(14.dp),
                                    color = BookParchmentCard,
                                    border = BorderStroke(
                                        width = if (isStoppedHere) 1.8.dp else 1.3.dp,
                                        color = if (isStoppedHere) BookGoldAccent else BookBorderInner
                                    ),
                                    shadowElevation = 3.dp
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Start (Right in RTL): Framed Circle Art + optional overlapping "🔖 توقفت هنا" gold ribbon
                                        Box(
                                            contentAlignment = Alignment.BottomCenter,
                                            modifier = Modifier.padding(bottom = if (isStoppedHere) 6.dp else 0.dp)
                                        ) {
                                            BookIndexLessonCircleArt(
                                                lessonId = lesson.id,
                                                visualType = localizedSpec?.visualType ?: InfographicVisualType.FEED_STARTER_SPEC
                                            )

                                            if (isStoppedHere) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = BookGoldAccent,
                                                    border = BorderStroke(1.dp, BookBurgundyDark),
                                                    shadowElevation = 2.dp,
                                                    modifier = Modifier
                                                        .offset(y = 8.dp)
                                                        .zIndex(2f)
                                                        .testTag("badge_last_read_${lesson.id}")
                                                ) {
                                                    Text(
                                                        text = Translations.tr("book_badge_stopped_here", lang),
                                                        fontSize = 8.5.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = BookBurgundyDark,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // Center: Bold 2-line Lesson Title ("الدرس 01: ...")
                                        Text(
                                            text = lessonHeading,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = BookBurgundyDark,
                                            lineHeight = 18.sp,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )

                                        // End (Left in RTL): Inline burgundy "إبدأ القراءة 📖 ‹" text CTA
                                        Text(
                                            text = "${Translations.tr("book_btn_start_reading", lang)} $ctaArrow",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = BookBurgundy,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .padding(horizontal = 2.dp)
                                                .testTag("btn_start_reading_${lesson.id}")
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Ornate Sepia Flourish Divider at the bottom of the notebook index sheet
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth(0.75f)
                                    .height(18.dp)
                            ) {
                                val w = size.width
                                val cy = size.height / 2f
                                drawLine(
                                    color = BookSepiaInk.copy(alpha = 0.7f),
                                    start = Offset(0f, cy),
                                    end = Offset(w * 0.38f, cy),
                                    strokeWidth = 1.6f,
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    color = BookSepiaInk.copy(alpha = 0.7f),
                                    start = Offset(w * 0.62f, cy),
                                    end = Offset(w, cy),
                                    strokeWidth = 1.6f,
                                    cap = StrokeCap.Round
                                )
                                val swirl = Path().apply {
                                    moveTo(w * 0.40f, cy)
                                    cubicTo(w * 0.44f, cy - 8f, w * 0.50f, cy + 8f, w * 0.50f, cy)
                                    cubicTo(w * 0.50f, cy - 8f, w * 0.56f, cy + 8f, w * 0.60f, cy)
                                }
                                drawPath(
                                    path = swirl,
                                    color = BookBurgundy,
                                    style = Stroke(width = 1.8f, cap = StrokeCap.Round)
                                )
                                drawCircle(color = BookBurgundy, radius = 3.2f, center = Offset(w * 0.50f, cy))
                            }
                        }
                    }

                    if (lang.isRtl) {
                        SpiralBindingRingsCanvas(
                            ringCount = 9,
                            onRightEdge = true,
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
