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
import com.example.darwinManuscriptBorder
import com.example.darwinPaperBackground
import com.example.models.AppPaperStyleOption
import com.example.models.AppPaperStyleRuntime
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

    val isDark = currentTheme.isDark
    val isDarwin = AppPaperStyleRuntime.activePaperStyle == AppPaperStyleOption.DARWIN_PARCHMENT
    val pageOuterBg = if (isDarwin && !isDark) Color(0xFFF4EBD6) else currentTheme.bgColor
    val pageInnerBg = if (isDarwin && !isDark) Color(0xFFFAF3E4) else currentTheme.cardColor
    val pageBorderOuter = if (isDarwin && !isDark) Color(0xFF4A321F) else accentColor.copy(alpha = 0.55f)
    val pageBorderInner = if (isDarwin && !isDark) Color(0xFF8C6D46) else accentColor.copy(alpha = 0.35f)

    CompositionLocalProvider(LocalLayoutDirection provides pageLayoutDirection) {
        // Darwin Parchment Manuscript Sheet or Active Paper Style Sheet framed with double-line ink border
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .darwinManuscriptBorder(cornerRadius = cornerRadiusDp.dp, isDark = isDark)
                .testTag("vintage_spiral_book_page_$lessonNumFormatted")
                .testTag("book_detail_view_${detailCard.id}"),
            shape = RoundedCornerShape(cornerRadiusDp.dp),
            color = pageOuterBg,
            border = BorderStroke(1.4.dp, pageBorderOuter),
            shadowElevation = 5.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(3.dp)
                    .background(
                        pageInnerBg,
                        RoundedCornerShape((cornerRadiusDp - 3).coerceAtLeast(4).dp)
                    )
                    .border(
                        width = 0.8.dp,
                        color = pageBorderInner,
                        shape = RoundedCornerShape((cornerRadiusDp - 3).coerceAtLeast(4).dp)
                    )
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides pageLayoutDirection) {
                    VintageBookPageInnerContent(
                        detailCard = localizedCard,
                        spec = localizedSpec,
                        pageNumber = pageNumber,
                        totalPages = totalPages,
                        lang = lang,
                        currentTheme = currentTheme,
                        accentColor = accentColor,
                        specialtyTitle = specialtyTitle,
                        onBackToIndex = onBackToIndex,
                        onOpenZoom = { internalZoomOpen = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                    )
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
    currentTheme: AppThemeOption = AppThemeOption.NATURE_GREEN,
    accentColor: Color = BookBurgundy,
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

        val isDark = currentTheme.isDark
        val isDarwin = AppPaperStyleRuntime.activePaperStyle == AppPaperStyleOption.DARWIN_PARCHMENT
        val mainTitleColor = if (isDarwin && !isDark) BookBurgundyDark else currentTheme.headingColor
        val stageCardBg = if (isDarwin && !isDark) Color(0xFFFAF3E4) else currentTheme.innerStatBoxColor
        val stageTextColor = if (isDarwin && !isDark) Color(0xFF26160C) else currentTheme.headingColor
        val stageOuterBorder = if (isDarwin && !isDark) Color(0xFF4A321F) else accentColor.copy(alpha = 0.45f)
        val stageInnerBorder = if (isDarwin && !isDark) Color(0xFF8C6D46) else accentColor.copy(alpha = 0.25f)

        // =========================================================================
        // 2. Main Centered Bold Burgundy Title ONLY (No subtitle, no Polaroid, no medallion)
        // =========================================================================
        Text(
            text = detailCard.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = mainTitleColor,
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
        val tapHintText = Translations.tr("book_tap_for_source_hint", lang)
        var selectedScientificModalItem by remember { mutableStateOf<BookScientificDetailModalSpec?>(null) }

        selectedScientificModalItem?.let { modalSpec ->
            BookScientificDetailModalDialog(
                spec = modalSpec,
                currentTheme = currentTheme,
                accentColor = accentColor,
                lang = lang,
                onDismiss = { selectedScientificModalItem = null }
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

                val romanNumeral = when (idx) {
                    0 -> "I"
                    1 -> "II"
                    else -> "III"
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 13.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selectedScientificModalItem = buildRomanStageScientificModalSpec(
                                    detailCard = detailCard,
                                    spec = spec,
                                    stageIndex = idx,
                                    romanNumeral = romanNumeral,
                                    stageTitle = stage.headerTitle,
                                    stageMetric = stage.bottomSummary,
                                    lang = lang
                                )
                            }
                            .darwinManuscriptBorder(cornerRadius = 10.dp, isDark = isDark)
                            .testTag("book_roman_stage_card_$romanNumeral"),
                        shape = RoundedCornerShape(10.dp),
                        color = stageCardBg,
                        border = BorderStroke(1.3.dp, stageOuterBorder),
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.5.dp)
                                .border(0.7.dp, stageInnerBorder, RoundedCornerShape(8.dp))
                                .padding(
                                    start = 5.dp,
                                    end = 5.dp,
                                    top = 14.dp,
                                    bottom = 6.dp
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stage.headerTitle,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = stageTextColor,
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
                                    color = stageTextColor,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 13.5.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = stageOuterBorder.copy(alpha = 0.12f),
                                border = BorderStroke(0.6.dp, stageOuterBorder.copy(alpha = 0.35f)),
                                modifier = Modifier.padding(top = 3.dp)
                            ) {
                                Text(
                                    text = tapHintText,
                                    fontSize = 7.8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = stageTextColor.copy(alpha = 0.88f),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Protruding Darwin Brass Roman Numeral Seal centered on top border (I, II, III)
                    Surface(
                        modifier = Modifier
                            .size(26.dp)
                            .align(Alignment.TopCenter)
                            .zIndex(2f),
                        shape = CircleShape,
                        color = Color(0xFF6E4720),
                        border = BorderStroke(1.5.dp, Color(0xFFD4AF37)),
                        shadowElevation = 3.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp)
                                .border(0.7.dp, Color(0xFFF4EBD6).copy(alpha = 0.7f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = romanNumeral,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFAF3E4)
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
                    tapHintText = tapHintText,
                    onClick = {
                        selectedScientificModalItem = buildComparisonScientificModalSpec(
                            detailCard = detailCard,
                            spec = spec,
                            isSuccess = true,
                            headerTitle = Translations.tr("book_card_success_do_this", lang),
                            bodyText = successBodyText,
                            lang = lang
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                SuccessMistakeFarmerCard(
                    isSuccess = false,
                    headerTitle = Translations.tr("book_card_avoid_dont", lang),
                    bodyText = mistakeBodyText,
                    textLayoutDirection = pageLayoutDirection,
                    tapHintText = tapHintText,
                    onClick = {
                        selectedScientificModalItem = buildComparisonScientificModalSpec(
                            detailCard = detailCard,
                            spec = spec,
                            isSuccess = false,
                            headerTitle = Translations.tr("book_card_avoid_dont", lang),
                            bodyText = mistakeBodyText,
                            lang = lang
                        )
                    },
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

private data class BookScientificDetailModalSpec(
    val romanOrBadgeLabel: String,
    val itemTitle: String,
    val benchmarkValue: String,
    val practicalExplanationPoints: List<String>,
    val barnApplicationSummary: String
)

private fun buildRomanStageScientificModalSpec(
    detailCard: BookSubCardModel,
    spec: EncyclopediaPosterSpec,
    stageIndex: Int,
    romanNumeral: String,
    stageTitle: String,
    stageMetric: String,
    lang: AppLanguage
): BookScientificDetailModalSpec {
    val badgePrefix = when (lang) {
        AppLanguage.AR -> "البند $romanNumeral"
        AppLanguage.FR -> "Point $romanNumeral"
        else -> "Stage $romanNumeral"
    }
    val rulePoint = detailCard.scientificRulePoints.getOrNull(stageIndex)
        ?: detailCard.scientificRulePoints.firstOrNull().orEmpty()
    val practicalStep = detailCard.practicalStepsWithCheck.getOrNull(stageIndex)
        ?: detailCard.practicalStepsWithCheck.firstOrNull().orEmpty()
    val extraStep = detailCard.practicalStepsWithCheck.getOrNull((stageIndex + 1) % detailCard.practicalStepsWithCheck.size.coerceAtLeast(1)).orEmpty()

    val expandedPoints = buildList {
        if (rulePoint.isNotBlank()) add("🔬 الأساس العلمي والفيزيولوجي: $rulePoint")
        if (practicalStep.isNotBlank()) add("🛠️ التطبيق العملي داخل العنبر (باللتر والحرارة والتوقيت): $practicalStep")
        if (extraStep.isNotBlank() && extraStep != practicalStep) add("📐 خطوة الضبط الميداني المكملة: $extraStep")
        if (spec.isMedicalSixSectionLayout) {
            spec.sec5DiagnosisPoints.getOrNull(stageIndex)?.let { add("🩺 التشخيص والمؤشر الحيوي: $it") }
            spec.sec3TreatmentSteps.getOrNull(stageIndex)?.let { add("💊 البروتوكول العلاجي والوقائي: $it") }
        }
        if (detailCard.liveBarnComparisonText.isNotBlank()) {
            add("📈 الأثر المباشر على الحيوية ومعامل التحويل (FCR): ${detailCard.liveBarnComparisonText}")
        }
    }.ifEmpty {
        listOf(
            "🔬 يضمن تطبيق هذا المعيار استقرار حرارة وحيوية القطيع ومنع الإجهاد الحراري أو التنفسي.",
            "📈 يحافظ على كفاءة التحويل الغذائي (FCR) وتجانس أوزان القطيع وفق منحنى Ross 308 القياسي."
        )
    }

    return BookScientificDetailModalSpec(
        romanOrBadgeLabel = badgePrefix,
        itemTitle = stageTitle.ifBlank { detailCard.title },
        benchmarkValue = stageMetric.ifBlank { detailCard.badge1 },
        practicalExplanationPoints = expandedPoints,
        barnApplicationSummary = detailCard.title
    )
}

private fun buildComparisonScientificModalSpec(
    detailCard: BookSubCardModel,
    spec: EncyclopediaPosterSpec,
    isSuccess: Boolean,
    headerTitle: String,
    bodyText: String,
    lang: AppLanguage
): BookScientificDetailModalSpec {
    val pair = spec.comparisonPair
    val points = buildList {
        if (bodyText.isNotBlank()) {
            add(if (isSuccess) "✅ الإجراء الصحيح المعتمد: $bodyText" else "❌ الخطأ الحرج الواجب تجنبه: $bodyText")
        }
        if (isSuccess) {
            pair?.goodPoints?.forEach { pt ->
                if (pt.isNotBlank() && pt != bodyText) add("✔️ $pt")
            }
            detailCard.practicalStepsWithCheck.forEach { step ->
                if (step.isNotBlank()) add("🛠️ خطوة التطبيق في العنبر: $step")
            }
        } else {
            pair?.badPoints?.forEach { pt ->
                if (pt.isNotBlank() && pt != bodyText) add("⚠️ $pt")
            }
            detailCard.scientificRulePoints.forEach { rule ->
                if (rule.isNotBlank()) add("🔬 التفسير العلمي والوقائي: $rule")
            }
        }
        if (detailCard.liveBarnComparisonText.isNotBlank()) {
            add("📈 الأثر المباشر على الحيوية ومعامل التحويل (FCR): ${detailCard.liveBarnComparisonText}")
        }
    }

    return BookScientificDetailModalSpec(
        romanOrBadgeLabel = if (isSuccess) "✅" else "❌",
        itemTitle = headerTitle,
        benchmarkValue = bodyText,
        practicalExplanationPoints = points,
        barnApplicationSummary = detailCard.title
    )
}

@Composable
private fun BookScientificDetailModalDialog(
    spec: BookScientificDetailModalSpec,
    currentTheme: AppThemeOption,
    accentColor: Color,
    lang: AppLanguage,
    onDismiss: () -> Unit
) {
    val isDark = currentTheme.isDark
    val isDarwin = AppPaperStyleRuntime.activePaperStyle == AppPaperStyleOption.DARWIN_PARCHMENT
    val modalBg = if (isDarwin && !isDark) Color(0xFFFAF3E4) else currentTheme.dialogCardColor
    val innerBoxBg = if (isDarwin && !isDark) Color(0xFFF2E5C9) else currentTheme.innerStatBoxColor
    val primaryInk = if (isDarwin && !isDark) Color(0xFF26160C) else currentTheme.headingColor
    val secondaryInk = if (isDarwin && !isDark) Color(0xFF5C4028) else currentTheme.secondaryTextColor
    val borderAccent = if (isDarwin && !isDark) Color(0xFF6E4720) else accentColor
    val layoutDir = if (lang.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .darwinManuscriptBorder(cornerRadius = 18.dp, isDark = isDark)
                    .testTag("book_scientific_detail_modal"),
                shape = RoundedCornerShape(18.dp),
                color = modalBg,
                border = BorderStroke(1.5.dp, borderAccent.copy(alpha = 0.65f)),
                shadowElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header: 📜 التفصيل العلمي التطبيقي والمصدر المرجعي
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF6E4720),
                            border = BorderStroke(1.5.dp, Color(0xFFD4AF37)),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = spec.romanOrBadgeLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFAF3E4),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Translations.tr("book_scientific_modal_title", lang),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = primaryInk,
                                lineHeight = 18.sp
                            )
                            Text(
                                text = spec.barnApplicationSummary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = secondaryInk,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // 1) Full Item Title & Standard Benchmark Value
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = innerBoxBg,
                        border = BorderStroke(1.dp, borderAccent.copy(alpha = 0.40f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${spec.romanOrBadgeLabel} — ${spec.itemTitle}",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = primaryInk,
                                lineHeight = 20.sp
                            )
                            if (spec.benchmarkValue.isNotBlank() && spec.benchmarkValue != spec.itemTitle) {
                                Text(
                                    text = "🎯 القيمة والمعيار القياسي المعتمد: ${spec.benchmarkValue}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = borderAccent,
                                    lineHeight = 19.sp
                                )
                            }
                        }
                    }

                    // 2) Expanded Practical Barn Explanation for the Farmer
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        spec.practicalExplanationPoints.forEach { point ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = modalBg,
                                border = BorderStroke(0.8.dp, borderAccent.copy(alpha = 0.28f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = point,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = primaryInk,
                                    lineHeight = 19.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }

                    // 3) Documented Scientific Source Card (📚 المصدر المرجعي المعتمد)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = innerBoxBg,
                        border = BorderStroke(1.2.dp, Color(0xFFD4AF37).copy(alpha = 0.65f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = Translations.tr("book_scientific_source_title", lang),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = borderAccent
                            )
                            Text(
                                text = Translations.tr("book_scientific_source_body", lang),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = secondaryInk,
                                lineHeight = 16.5.sp
                            )
                        }
                    }

                    // 4) Sleek Close Button (إغلاق ✓)
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = borderAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_close_scientific_modal")
                    ) {
                        Text(
                            text = "${Translations.tr("btn_close", lang)} ✓",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
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
