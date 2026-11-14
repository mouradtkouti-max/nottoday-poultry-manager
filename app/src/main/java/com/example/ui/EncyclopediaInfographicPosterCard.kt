package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.BookSubCardModel
import com.example.models.AppThemeOption
import com.example.models.EncyclopediaPosterSpec
import com.example.models.InfographicTimelineStep
import com.example.models.Ross308PosterCatalog
import com.example.utils.AppLanguage
import com.example.utils.Ross308BookPageTranslations
import com.example.utils.Translations
import kotlin.math.absoluteValue

private data class StageCardUiModel(
    val headerTitle: String,
    val bottomSummary: String
)

private fun splitStageCardHeaderAndSummary(
    spec: EncyclopediaPosterSpec,
    step: InfographicTimelineStep?,
    stepIndex: Int,
    lang: AppLanguage
): StageCardUiModel {
    if (spec.isMedicalSixSectionLayout) {
        return when (stepIndex) {
            0 -> {
                val sym1 = spec.sec2SymptomCards.getOrNull(0)?.title
                    ?: Translations.tr("book_ribbon_main_symptoms", lang)
                val sym2 = spec.sec2SymptomCards.getOrNull(1)?.title
                    ?: spec.quickFacts.firstOrNull()?.second
                    ?: ""
                StageCardUiModel(headerTitle = sym1, bottomSummary = sym2)
            }
            1 -> {
                val metric = spec.sec4PerformanceTable.firstOrNull()
                val header = metric?.metricLabel
                    ?: Translations.tr("book_ribbon_performance_impact", lang)
                val summary = metric?.deltaValue
                    ?: spec.sec5DiagnosisPoints.firstOrNull()?.removePrefix("✅")?.trim()
                    ?: ""
                StageCardUiModel(headerTitle = header, bottomSummary = summary)
            }
            else -> {
                val header = Translations.tr("book_ribbon_treatment", lang)
                    .removePrefix("➕")
                    .removePrefix("🩺")
                    .trim()
                val summary = spec.sec3TreatmentSteps.firstOrNull()
                    ?.removePrefix("✅")
                    ?.trim()
                    ?: spec.sec6PreventionIcons.firstOrNull()?.second
                    ?: ""
                StageCardUiModel(headerTitle = header, bottomSummary = summary)
            }
        }
    }

    if (step == null) {
        return StageCardUiModel("", "")
    }

    if (step.subtitle.isNotBlank()) {
        return StageCardUiModel(
            headerTitle = step.title.trim(),
            bottomSummary = step.subtitle.trim()
        )
    }

    val raw = step.title.trim()
    val delimiters = listOf(" — ", ": ", "：", " ➔ ")
    for (delim in delimiters) {
        val idx = raw.indexOf(delim)
        if (idx in 2 until raw.length - delim.length) {
            val left = raw.substring(0, idx).trim()
            val right = raw.substring(idx + delim.length).trim()
            if (left.isNotEmpty() && right.isNotEmpty()) {
                return StageCardUiModel(headerTitle = left, bottomSummary = right)
            }
        }
    }

    val words = raw.split(" ").filter { it.isNotBlank() }
    return if (words.size >= 4) {
        val mid = (words.size + 1) / 2
        StageCardUiModel(
            headerTitle = words.take(mid).joinToString(" "),
            bottomSummary = words.drop(mid).joinToString(" ")
        )
    } else {
        StageCardUiModel(headerTitle = raw, bottomSummary = "")
    }
}

@Composable
fun VintageSpiralBookPageContainer(
    detailCard: BookSubCardModel,
    spec: EncyclopediaPosterSpec,
    pageNumber: Int = detailCard.cardNumberInBranch.coerceIn(1, 4),
    totalPages: Int = 4,
    branchLessons: List<BookSubCardModel> = listOf(detailCard),
    onSelectPageInBranch: (Int) -> Unit = {},
    specialtyTitle: String? = null,
    onBackToIndex: (() -> Unit)? = null,
    lang: AppLanguage = AppLanguage.AR,
    currentTheme: AppThemeOption = AppThemeOption.NATURE_GREEN,
    accentColor: Color = BookBurgundy,
    accentPrimaryBtn: Color = BookBurgundy,
    accentContainer: Color = BookParchmentDeep,
    cornerRadiusDp: Int = 14,
    externalZoomTrigger: Boolean = false,
    onDismissExternalZoom: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var internalZoomOpen by remember { mutableStateOf(false) }
    val showZoom = internalZoomOpen || externalZoomTrigger
    val (localizedCard, localizedSpec) = remember(detailCard, spec, lang) {
        Ross308BookPageTranslations.localizeSubCardAndSpec(detailCard, spec, lang)
    }
    val pageLayoutDirection = if (lang.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    val lessonNumFormatted = remember(pageNumber, localizedSpec.lessonNumberFormatted) {
        String.format(java.util.Locale.US, "%02d", pageNumber.coerceIn(1, 4))
    }

    if (showZoom) {
        VintageBookPageZoomDialog(
            detailCard = localizedCard,
            spec = localizedSpec,
            pageNumber = pageNumber,
            totalPages = totalPages,
            branchLessons = branchLessons,
            onSelectPageInBranch = onSelectPageInBranch,
            specialtyTitle = specialtyTitle,
            lang = lang,
            onDismiss = {
                internalZoomOpen = false
                onDismissExternalZoom()
            }
        )
    }

    CompositionLocalProvider(LocalLayoutDirection provides pageLayoutDirection) {
        // Outer Book Parchment Sheet matching Reference Image 2 (1794641992685_cdefeea4.jpg)
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .testTag("vintage_spiral_book_page_$lessonNumFormatted")
                .testTag("book_detail_view_${detailCard.id}"),
            shape = RoundedCornerShape(cornerRadiusDp.dp),
            color = BookParchmentDeep,
            border = BorderStroke(2.0.dp, BookBorderOuter),
            shadowElevation = 5.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .background(
                        BookParchmentLight,
                        RoundedCornerShape((cornerRadiusDp - 3).coerceAtLeast(8).dp)
                    )
                    .border(
                        width = 1.2.dp,
                        color = BookBorderInner,
                        shape = RoundedCornerShape((cornerRadiusDp - 3).coerceAtLeast(8).dp)
                    )
            ) {
                // Keep metallic spiral binding cleanly along visual left edge matching Reference Image 2
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SpiralBindingRingsCanvas(
                            ringCount = 9,
                            onRightEdge = false,
                            modifier = Modifier
                                .heightIn(min = 580.dp)
                                .padding(vertical = 8.dp)
                        )

                        CompositionLocalProvider(LocalLayoutDirection provides pageLayoutDirection) {
                            VintageBookPageInnerContent(
                                detailCard = localizedCard,
                                spec = localizedSpec,
                                pageNumber = pageNumber,
                                totalPages = totalPages,
                                lang = lang,
                                specialtyTitle = specialtyTitle,
                                onBackToIndex = onBackToIndex,
                                onOpenZoom = { internalZoomOpen = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 4.dp, end = 10.dp, top = 8.dp, bottom = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VintageBookPageInnerContent(
    detailCard: BookSubCardModel,
    spec: EncyclopediaPosterSpec,
    pageNumber: Int,
    totalPages: Int,
    lang: AppLanguage,
    specialtyTitle: String? = null,
    onBackToIndex: (() -> Unit)? = null,
    onOpenZoom: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val lessonNumFormatted = String.format(java.util.Locale.US, "%02d", pageNumber.coerceIn(1, 4))
    val pageLayoutDirection = if (lang.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    val compactLessonTagText = if (lang == AppLanguage.AR) {
        "الدرس\n$lessonNumFormatted"
    } else {
        Translations.tr("book_lesson_number_fmt", lang).replace("{num}", lessonNumFormatted)
    }
    val activeSpecialtyLabel = spec.specialtyBadge.ifBlank { specialtyTitle.orEmpty() }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // =========================================================================
        // 1. Top Sheet Strip (Back Arrow + Rooster & Studio | Encyclopedia Ribbon | Specialty + Lesson Box)
        // =========================================================================
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("encyclopedia_top_ribbon"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (onBackToIndex != null) {
                    IconButton(
                        onClick = onBackToIndex,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_back_to_book_index")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = Translations.tr("book_btn_back_to_index", lang),
                            tint = BookBurgundyDark
                        )
                    }
                }

                // Studio Logo + Rooster Emblem
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    VintageRoosterLogoCanvas()
                    Column {
                        Text(
                            text = "iQ Notebook",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BookBurgundyDark,
                            maxLines = 1
                        )
                        Text(
                            text = "NOT TODAY Studio",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = BookSepiaInk,
                            maxLines = 1
                        )
                    }
                }

                // Center Parchment Ribbon ("📖 موسوعة تربية دجاج اللحم")
                VintageFoldedRibbonBanner(
                    text = Translations.tr("book_encyclopedia_ribbon_title", lang),
                    modifier = Modifier.weight(1f)
                )

                // Dark Burgundy Specialty Badge + Framed "الدرس 01" Box
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = BookBurgundy,
                        border = BorderStroke(0.8.dp, BookGoldAccent)
                    ) {
                        Text(
                            text = activeSpecialtyLabel,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFF8E7),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            lineHeight = 10.sp,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .widthIn(max = 78.dp)
                                .padding(horizontal = 5.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = BookParchmentLight,
                        border = BorderStroke(1.2.dp, BookBurgundy),
                        modifier = Modifier.testTag("book_lesson_number_badge_$lessonNumFormatted")
                    ) {
                        Text(
                            text = compactLessonTagText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BookBurgundyDark,
                            textAlign = TextAlign.Center,
                            lineHeight = 10.sp,
                            maxLines = 2,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // =========================================================================
        // 2. Main Centered Bold Burgundy Title ONLY (No subtitle, no Polaroid, no medallion)
        // =========================================================================
        Text(
            text = detailCard.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BookBurgundyDark,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
                .testTag("book_lesson_main_title")
        )

        // =========================================================================
        // =========================================================================
        // 3. Nano Banana Lesson Hero Artwork + High-Contrast Farmer Gauge Strip (Tap to zoom)
        // =========================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onOpenZoom != null) {
                        Modifier
                            .clickable(onClick = onOpenZoom)
                            .testTag("btn_zoom_encyclopedia_poster")
                    } else {
                        Modifier
                    }
                )
        ) {
            NanoBananaLessonHeroPanel(
                lessonId = detailCard.id,
                lessonTitle = detailCard.title,
                spec = spec,
                lang = lang,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // =========================================================================
        // 4. Three Vertical Stage Cards Row (1 | 2 | 3) with Nano Banana Art & Farmer Metrics
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .testTag("vintage_book_three_stage_cards_row"),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.Top
        ) {
            for (idx in 0 until 3) {
                val stage = splitStageCardHeaderAndSummary(
                    spec = spec,
                    step = spec.timelineSteps.getOrNull(idx),
                    stepIndex = idx,
                    lang = lang
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 12.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = BookParchmentCard,
                        border = BorderStroke(1.2.dp, BookBorderInner),
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    start = 5.dp,
                                    end = 5.dp,
                                    top = 15.dp,
                                    bottom = 8.dp
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stage.headerTitle,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BookBurgundyDark,
                                textAlign = TextAlign.Center,
                                lineHeight = 14.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )

                            NanoBananaStageArtBox(
                                lessonId = detailCard.id,
                                visualType = spec.visualType,
                                stepIndex = idx,
                                lang = lang,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            if (stage.bottomSummary.isNotBlank()) {
                                Text(
                                    text = stage.bottomSummary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BookBurgundyDark,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 13.5.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Protruding burgundy circular badge centered on top border (1, 2, 3)
                    Surface(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.TopCenter)
                            .zIndex(2f),
                        shape = CircleShape,
                        color = BookBurgundy,
                        border = BorderStroke(1.5.dp, Color(0xFFFFF8E7)),
                        shadowElevation = 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${idx + 1}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 5A. Bottom 50% / 50% Farmer Do-vs-Don't Comparison Row (No Chick Anatomy Diagram)
        //     RTL: Start (Right) = "✅ علامات النجاح — افعل هذا" | End (Left) = "❌ تجنّب هذا الخطأ — إياك وهذا"
        // =========================================================================
        val successBodyText = if (spec.isMedicalSixSectionLayout) {
            spec.sec3TreatmentSteps.firstOrNull()?.removePrefix("✅")?.trim().orEmpty()
        } else {
            spec.comparisonPair?.goodSummaryCaption?.ifBlank {
                spec.comparisonPair.goodPoints.firstOrNull().orEmpty()
            }.orEmpty()
        }

        val mistakeBodyText = if (spec.isMedicalSixSectionLayout) {
            spec.sec6ImportantNote
                .removePrefix("⚠️")
                .removePrefix("ملاحظة مهمة:")
                .trim()
        } else {
            spec.comparisonPair?.badSummaryCaption?.ifBlank {
                spec.comparisonPair.badPoints.firstOrNull().orEmpty()
            }.orEmpty()
        }

        CompositionLocalProvider(LocalLayoutDirection provides pageLayoutDirection) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)
                    .testTag("vintage_book_bottom_comparison_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                SuccessMistakeFarmerCard(
                    isSuccess = true,
                    headerTitle = Translations.tr("book_card_success_do_this", lang),
                    bodyText = successBodyText,
                    textLayoutDirection = pageLayoutDirection,
                    modifier = Modifier.weight(1f)
                )

                SuccessMistakeFarmerCard(
                    isSuccess = false,
                    headerTitle = Translations.tr("book_card_avoid_dont", lang),
                    bodyText = mistakeBodyText,
                    textLayoutDirection = pageLayoutDirection,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // =========================================================================
        // 5B. Sepia Farm Engraving Footer + Plain Swipe Indicator Line
        // =========================================================================
        SepiaFarmEngravingFooterCanvas(
            pageNumber = pageNumber,
            totalPages = totalPages,
            lang = lang
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VintageBookPageZoomDialog(
    detailCard: BookSubCardModel,
    spec: EncyclopediaPosterSpec,
    pageNumber: Int,
    totalPages: Int,
    branchLessons: List<BookSubCardModel>,
    onSelectPageInBranch: (Int) -> Unit,
    specialtyTitle: String? = null,
    lang: AppLanguage,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1.0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val safeLessons = if (branchLessons.isNotEmpty()) branchLessons else listOf(detailCard)
    val initialIdx = (pageNumber - 1).coerceIn(0, (safeLessons.size - 1).coerceAtLeast(0))
    val zoomPagerState = rememberPagerState(
        initialPage = initialIdx,
        pageCount = { safeLessons.size.coerceAtLeast(1) }
    )

    LaunchedEffect(zoomPagerState.currentPage) {
        scale = 1.0f
        offsetX = 0f
        offsetY = 0f
        onSelectPageInBranch(zoomPagerState.currentPage)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(BookParchmentDeep)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .testTag("encyclopedia_zoom_dialog"),
            color = BookParchmentDeep
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Minimal Top Zoom Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BookBurgundy)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${Translations.tr("book_btn_zoom_page", lang)} (${(scale * 100).toInt()}%)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFF8E7),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            scale = if (scale > 1.2f) 1.0f else 1.65f
                            offsetX = 0f
                            offsetY = 0f
                        }) {
                            Text("100% / 165%", fontWeight = FontWeight.ExtraBold, color = BookGoldAccent)
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_close_zoom_poster")
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = Translations.tr("btn_close", lang),
                                tint = Color.White
                            )
                        }
                    }
                }

                // Swipeable HorizontalPager with 3D Page Flip inside Zoom Dialog
                HorizontalPager(
                    state = zoomPagerState,
                    userScrollEnabled = scale <= 1.05f,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("zoom_book_horizontal_pager"),
                    pageSpacing = 12.dp
                ) { pageIdx ->
                    val pageCardItem = safeLessons.getOrElse(pageIdx) { detailCard }
                    val rawSpec = pageCardItem.posterSpec ?: Ross308PosterCatalog.forSubCard(pageCardItem.id) ?: spec
                    val (locCard, locSpec) = remember(pageCardItem, rawSpec, lang) {
                        Ross308BookPageTranslations.localizeSubCardAndSpec(pageCardItem, rawSpec, lang)
                    }
                    val pageOffset = (zoomPagerState.currentPage - pageIdx) + zoomPagerState.currentPageOffsetFraction

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                cameraDistance = 16f * density
                                rotationY = (-pageOffset * 26f).coerceIn(-35f, 35f)
                                val absOff = pageOffset.absoluteValue.coerceIn(0f, 1f)
                                alpha = 1f - (absOff * 0.22f)
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        scale = if (scale > 1.25f) 1.0f else 1.75f
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                )
                            }
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    val nextScale = (scale * zoom).coerceIn(1f, 2.6f)
                                    scale = nextScale
                                    if (nextScale > 1.02f) {
                                        offsetX += pan.x
                                        offsetY += pan.y
                                    } else {
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                }
                            }
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offsetX,
                                    translationY = offsetY
                                )
                        ) {
                            VintageBookPageInnerContent(
                                detailCard = locCard,
                                spec = locSpec,
                                pageNumber = pageIdx + 1,
                                totalPages = safeLessons.size.coerceAtLeast(4),
                                specialtyTitle = specialtyTitle,
                                lang = lang
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EncyclopediaInfographicPosterCard(
    detailCard: BookSubCardModel,
    spec: EncyclopediaPosterSpec,
    currentTheme: AppThemeOption,
    accentColor: Color,
    accentPrimaryBtn: Color,
    accentContainer: Color,
    cornerRadiusDp: Int,
    lang: AppLanguage = AppLanguage.AR,
    modifier: Modifier = Modifier
) {
    VintageSpiralBookPageContainer(
        detailCard = detailCard,
        spec = spec,
        pageNumber = detailCard.cardNumberInBranch.coerceIn(1, 4),
        totalPages = 4,
        lang = lang,
        currentTheme = currentTheme,
        accentColor = accentColor,
        accentPrimaryBtn = accentPrimaryBtn,
        accentContainer = accentContainer,
        cornerRadiusDp = cornerRadiusDp,
        modifier = modifier
    )
}
