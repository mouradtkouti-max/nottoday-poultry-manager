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
import com.example.BookSubCardModel
import com.example.models.AppThemeOption
import com.example.models.EncyclopediaPosterSpec
import com.example.models.Ross308PosterCatalog
import com.example.utils.AppLanguage
import com.example.utils.Ross308BookPageTranslations
import com.example.utils.Translations
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun VintageSpiralBookPageContainer(
    detailCard: BookSubCardModel,
    spec: EncyclopediaPosterSpec,
    pageNumber: Int = detailCard.cardNumberInBranch.coerceIn(1, 4),
    totalPages: Int = 4,
    branchLessons: List<BookSubCardModel> = listOf(detailCard),
    onSelectPageInBranch: (Int) -> Unit = {},
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
            lang = lang,
            onDismiss = {
                internalZoomOpen = false
                onDismissExternalZoom()
            }
        )
    }

    CompositionLocalProvider(LocalLayoutDirection provides pageLayoutDirection) {
        // Outer Book Cover Shadow & Parchment Sheet Container
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .testTag("vintage_spiral_book_page_$lessonNumFormatted")
                .testTag("book_detail_view_${detailCard.id}"),
            shape = RoundedCornerShape(cornerRadiusDp.dp),
            color = BookParchmentDeep,
            border = BorderStroke(2.2.dp, BookBorderOuter),
            shadowElevation = 6.dp
        ) {
            // Double Inner Classic Border (#6E3B1E outer -> #A67B5B inner) on Warm Parchment (#F5E6C8)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .background(BookParchmentLight, RoundedCornerShape((cornerRadiusDp - 3).coerceAtLeast(8).dp))
                    .border(
                        width = 1.3.dp,
                        color = BookBorderInner,
                        shape = RoundedCornerShape((cornerRadiusDp - 3).coerceAtLeast(8).dp)
                    )
            ) {
                // Force LTR row for physical spiral binding on the left edge + book page body on the right
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Edge: 7 Metallic 3D Spiral Binding Rings & Punched Holes
                        SpiralBindingRingsCanvas(
                            ringCount = 7,
                            modifier = Modifier
                                .heightIn(min = 560.dp)
                                .padding(vertical = 8.dp)
                        )

                        // Book Page Content (respects active language RTL/LTR)
                        CompositionLocalProvider(LocalLayoutDirection provides pageLayoutDirection) {
                            VintageBookPageInnerContent(
                                detailCard = localizedCard,
                                spec = localizedSpec,
                                pageNumber = pageNumber,
                                totalPages = totalPages,
                                lang = lang,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 6.dp, end = 10.dp, top = 10.dp, bottom = 8.dp)
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
    modifier: Modifier = Modifier
) {
    val lessonNumFormatted = String.format(java.util.Locale.US, "%02d", pageNumber.coerceIn(1, 4))
    val lessonTagText = Translations.tr("book_lesson_number_fmt", lang)
        .replace("{num}", lessonNumFormatted)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Top Book Page Header (Left: Rooster + iQ Notebook / NOT TODAY Studio | Center: Folded Ribbon | Right: Chapter Badge + Lesson Number 01..04)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("encyclopedia_top_ribbon"),
            shape = RoundedCornerShape(8.dp),
            color = BookParchmentMid,
            border = BorderStroke(1.2.dp, BookBorderOuter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Top Brand & Chapter Strip (Fixed Left-Center-Right layout matching physical book header)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Left: Burgundy Rooster Icon + iQ Notebook / NOT TODAY Studio
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            VintageRoosterLogoCanvas()
                            Column {
                                Text(
                                    text = "iQ Notebook",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BookBurgundy
                                )
                                Text(
                                    text = "NOT TODAY Studio",
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BookSepiaInk
                                )
                            }
                        }

                        // Center: Vintage Folded Ribbon Banner ("📖 موسوعة تربية دجاج اللحم")
                        VintageFoldedRibbonBanner(
                            text = Translations.tr("book_encyclopedia_ribbon_title", lang),
                            modifier = Modifier.weight(1f)
                        )

                        // Right: Dark Burgundy Specialty Badge + Framed "رقم الدرس 01..04" Box
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
                                    text = spec.specialtyBadge,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFFF8E7),
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = BookParchmentLight,
                                border = BorderStroke(1.2.dp, BookBurgundy),
                                modifier = Modifier.testTag("book_lesson_number_badge_$lessonNumFormatted")
                            ) {
                                Text(
                                    text = lessonTagText,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BookBurgundyDark,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                // Main Lesson Title Row: Left Polaroid Frame + Center Burgundy Title & Subtitle + Right Classic Bird Head Illustration
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PolaroidLessonFrame(
                            caption = spec.polaroidCaption,
                            emoji = detailCard.iconEmoji,
                            visualType = spec.visualType
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = detailCard.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BookBurgundyDark,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Text(
                                text = spec.englishSubtitleOrRule,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = BookSepiaInk,
                                textAlign = TextAlign.Center,
                                lineHeight = 13.sp
                            )
                        }

                        BroilerHeadMedallionCanvas()
                    }
                }
            }
        }

        // 2. Multi-Column Illustrated Book Body (Template 1 vs Template 2 — NO duplicate text boxes below)
        if (spec.isMedicalSixSectionLayout) {
            DiseaseMedicalBookPageLayout(spec = spec, lang = lang)
        } else {
            CareVaxFeedBookPageLayout(spec = spec, lang = lang)
        }

        // 3. Bottom Sepia Farm Engraving Footer ("صفحة X من 4")
        SepiaFarmEngravingFooterCanvas(
            pageNumber = pageNumber,
            totalPages = totalPages,
            lang = lang
        )
    }
}

@Composable
private fun CareVaxFeedBookPageLayout(
    spec: EncyclopediaPosterSpec,
    lang: AppLanguage
) {
    // SECTION 1: Top Dual 2-Column Hero Row (Left: Illustrated Scene with Thermometer & Callouts | Right: Core Idea + Why It Is Important)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("vintage_book_hero_2col_row"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Left Column: Visual Scene Panel
            Box(modifier = Modifier.weight(1.05f)) {
                LessonHeroSceneCanvas(spec = spec)
            }

            // Right Column: Core Idea + Why It Is Important (4 green check points)
            CompositionLocalProvider(
                LocalLayoutDirection provides if (lang.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
                Column(
                    modifier = Modifier.weight(1.1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BurgundySectionRibbon(
                        title = Translations.tr("book_ribbon_core_idea", lang),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFFFDF7),
                        border = BorderStroke(1.dp, BookBorderInner),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = spec.coreIdea,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BookBurgundyDark,
                            lineHeight = 13.5.sp,
                            modifier = Modifier.padding(7.dp)
                        )
                    }

                    BurgundySectionRibbon(
                        title = Translations.tr("book_ribbon_why_important", lang),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFFFDF7),
                        border = BorderStroke(1.dp, BookBorderInner),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(7.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            spec.whyImportantPoints.take(4).forEach { pt ->
                                val cleanPt = if (pt.startsWith("✅")) pt else "✅ $pt"
                                Text(
                                    text = cleanPt,
                                    fontSize = 8.8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BookBurgundyDark,
                                    lineHeight = 12.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // SECTION 2: Middle Horizontal 3-Step Strip ("⚙️ كيف أجهّز العنبر؟" — 3 Illustrated Cards in One Row: 1 -> 2 -> 3)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vintage_book_horizontal_steps_row"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        BurgundySectionRibbon(
            title = spec.timelineStepsTitle.ifBlank { Translations.tr("book_ribbon_how_to_prepare", lang) },
            modifier = Modifier.fillMaxWidth(0.68f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val steps = spec.timelineSteps.take(3)
            steps.forEachIndexed { idx, step ->
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFFDF7),
                    border = BorderStroke(1.2.dp, BookBorderOuter)
                ) {
                    Column(
                        modifier = Modifier.padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            HorizontalStepIllustrationCanvas(
                                stepIndex = idx,
                                emoji = step.emoji,
                                visualType = spec.visualType
                            )
                            Surface(
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.TopStart),
                                shape = CircleShape,
                                color = BookBurgundy,
                                border = BorderStroke(1.dp, BookGoldAccent)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${idx + 1}",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        Text(
                            text = step.title,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BookBurgundyDark,
                            textAlign = TextAlign.Center,
                            lineHeight = 11.5.sp
                        )
                    }
                }

                if (idx < steps.lastIndex) {
                    Text(
                        text = if (lang.isRtl) "⬅" else "➔",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BookBurgundy
                    )
                }
            }
        }
    }

    // SECTION 3: Bottom Triple Row ("❌ تجنّب هذا الخطأ" | "🐣 رسم تشريحي للصوص" | "✅ علامات الجاهزية")
    val cmp = spec.comparisonPair
    val badCaption = cmp?.badSummaryCaption?.ifBlank { cmp.badPoints.firstOrNull().orEmpty() }
        ?: "لا تستقبل الصيصان في عنبر بارد أو فرشة رطبة أو متسخة"
    val goodCaption = cmp?.goodSummaryCaption?.ifBlank { cmp.goodPoints.firstOrNull().orEmpty() }
        ?: "عنبر دافئ، فرشة جافة، وصيصان منتشرة بهدوء"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vintage_book_bottom_triple_row"),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top
    ) {
        GoodBadSceneFrameCanvas(
            isGood = false,
            ribbonTitle = Translations.tr("book_ribbon_avoid_mistake", lang),
            caption = badCaption,
            modifier = Modifier.weight(1f)
        )

        ChickInternalAnatomyCanvas(
            lang = lang,
            modifier = Modifier.weight(1.12f)
        )

        GoodBadSceneFrameCanvas(
            isGood = true,
            ribbonTitle = Translations.tr("book_ribbon_readiness_signs", lang),
            caption = goodCaption,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DiseaseMedicalBookPageLayout(
    spec: EncyclopediaPosterSpec,
    lang: AppLanguage
) {
    // SECTION 1: Disease Definition + Internal Anatomy with Zoom Inset + Quick Facts Box
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("encyclopedia_sec1_definition"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        BurgundySectionRibbon(
            title = Translations.tr("book_ribbon_disease_def", lang),
            modifier = Modifier.fillMaxWidth(0.52f)
        )
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFFFFDF7),
            border = BorderStroke(1.dp, BookBorderInner),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = spec.sec1Definition,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = BookBurgundyDark,
                lineHeight = 13.5.sp,
                modifier = Modifier.padding(7.dp)
            )
        }

        // Row: Right Framed "معلومات سريعة" Box + Left/Center High-Precision Anatomy with Zoom Inset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.weight(0.92f),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFFDF7),
                border = BorderStroke(1.3.dp, BookBurgundy)
            ) {
                Column(
                    modifier = Modifier.padding(7.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    BurgundySectionRibbon(
                        title = "⚡ ${Translations.tr("book_ribbon_quick_facts", lang)}",
                        modifier = Modifier.fillMaxWidth()
                    )
                    spec.quickFacts.forEach { (label, value) ->
                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = BookParchmentLight,
                            border = BorderStroke(0.7.dp, BookBorderInner),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(5.dp)) {
                                Text(
                                    text = label,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BookBurgundy
                                )
                                Text(
                                    text = value,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BookBurgundyDark
                                )
                            }
                        }
                    }
                }
            }

            ChickenDiseaseAnatomyZoomCanvas(
                spec = spec,
                lang = lang,
                modifier = Modifier.weight(1.35f)
            )
        }
    }

    // SECTION 2: Horizontal Main Symptoms Strip ("🩺 الأعراض الرئيسية" — 5 Illustrated Boxes in One Row)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("encyclopedia_symptoms_row"),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        BurgundySectionRibbon(
            title = Translations.tr("book_ribbon_main_symptoms", lang),
            modifier = Modifier.fillMaxWidth(0.52f)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            spec.sec2SymptomCards.take(5).forEach { sym ->
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(7.dp),
                    color = Color(0xFFFFFDF7),
                    border = BorderStroke(1.dp, BookBorderOuter)
                ) {
                    Column(
                        modifier = Modifier.padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(34.dp)
                                .background(BookParchmentMid, RoundedCornerShape(5.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = sym.emoji, fontSize = 16.sp)
                        }
                        Text(
                            text = sym.title,
                            fontSize = 7.8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BookBurgundyDark,
                            textAlign = TextAlign.Center,
                            lineHeight = 10.5.sp
                        )
                    }
                }
            }
        }
    }

    // SECTION 3A (Requirement 4): Full-Width "📊 التأثير على الأداء" Table Card across the entire width of the page (no vertical squishing)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("encyclopedia_performance_table"),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFFFDF7),
        border = BorderStroke(1.3.dp, BookBurgundy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(7.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            BurgundySectionRibbon(
                title = Translations.tr("book_ribbon_performance_impact", lang),
                modifier = Modifier.fillMaxWidth()
            )
            // Render the 5 performance metrics in an organized wide 2-row grid (3 metrics top + 2 metrics bottom) so labels and red arrows never wrap vertically
            val metrics = spec.sec4PerformanceTable.take(5)
            val topMetrics = metrics.take(3)
            val bottomMetrics = metrics.drop(3)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                topMetrics.forEach { metric ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(6.dp),
                        color = BookParchmentLight,
                        border = BorderStroke(0.9.dp, BookBorderInner)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = metric.metricLabel,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BookBurgundyDark,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = metric.deltaValue,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFB91C1C),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (bottomMetrics.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    bottomMetrics.forEach { metric ->
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            color = BookParchmentLight,
                            border = BorderStroke(0.9.dp, BookBorderInner)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = metric.metricLabel,
                                    fontSize = 8.8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BookBurgundyDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = metric.deltaValue,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFB91C1C),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // SECTION 3B (Requirement 4): Balanced 50% / 50% Equal-Height Row for "➕ العلاج والتعامل" and "🔬 التشخيص"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Column 1 (50%): ➕ العلاج والتعامل
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .testTag("encyclopedia_sec3_treatment"),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFFFFDF7),
            border = BorderStroke(1.1.dp, BookBorderOuter)
        ) {
            Column(
                modifier = Modifier.padding(7.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                BurgundySectionRibbon(
                    title = Translations.tr("book_ribbon_treatment", lang),
                    modifier = Modifier.fillMaxWidth()
                )
                spec.sec3TreatmentSteps.take(5).forEach { step ->
                    val formatted = if (step.startsWith("✅")) step else "✅ $step"
                    Text(
                        text = formatted,
                        fontSize = 8.4.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BookBurgundyDark,
                        lineHeight = 12.sp
                    )
                }
            }
        }

        // Column 2 (50%): 🔬 التشخيص
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .testTag("encyclopedia_sec5_diagnosis"),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFFFFDF7),
            border = BorderStroke(1.1.dp, BookBorderOuter)
        ) {
            Column(
                modifier = Modifier.padding(7.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                BurgundySectionRibbon(
                    title = Translations.tr("book_ribbon_diagnosis", lang),
                    modifier = Modifier.fillMaxWidth()
                )
                spec.sec5DiagnosisPoints.forEach { pt ->
                    val formatted = if (pt.startsWith("✅")) pt else "✅ $pt"
                    Text(
                        text = formatted,
                        fontSize = 8.4.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BookBurgundyDark,
                        lineHeight = 12.sp
                    )
                }
            }
        }
    }

    // SECTION 4: Prevention (5 Vertical Icon Boxes) + Pinned Important Note Card
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("encyclopedia_sec6_prevention"),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1.6f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            BurgundySectionRibbon(
                title = Translations.tr("book_ribbon_prevention", lang),
                modifier = Modifier.fillMaxWidth(0.55f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                spec.sec6PreventionIcons.take(5).forEach { (icon, label) ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFFFDF7),
                        border = BorderStroke(0.9.dp, BookBorderOuter)
                    ) {
                        Column(
                            modifier = Modifier.padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(text = icon, fontSize = 14.sp)
                            Text(
                                text = label,
                                fontSize = 7.2.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BookBurgundyDark,
                                textAlign = TextAlign.Center,
                                lineHeight = 9.5.sp
                            )
                        }
                    }
                }
            }
        }

        PinnedImportantNoteCard(
            title = Translations.tr("book_ribbon_important_note", lang),
            noteText = spec.sec6ImportantNote,
            modifier = Modifier.weight(1f)
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
    lang: AppLanguage,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1.0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()
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
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
                .testTag("encyclopedia_zoom_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = BookParchmentDeep,
            border = BorderStroke(2.dp, BookBurgundy)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Zoom Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BookBurgundy)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${Translations.tr("book_btn_zoom_page", lang)} (${(scale * 100).toInt()}%)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFF8E7),
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
                            modifier = Modifier.testTag("btn_close_zoom_poster")
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
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
                                lang = lang
                            )
                        }
                    }
                }

                // Bottom Page Flip Navigation Bar inside Zoom Dialog ("⬅ الدرس السابق" | "صفحة X من 4" | "الدرس التالي ➔")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BookParchmentMid)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .testTag("zoom_dialog_bottom_nav_bar"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val totalInBranch = safeLessons.size.coerceAtLeast(1)
                    val currentPageOneBased = zoomPagerState.currentPage + 1
                    val pageIndicatorText = Translations.tr("book_page_footer_fmt", lang)
                        .replace("{page}", currentPageOneBased.toString())
                        .replace("{total}", totalInBranch.toString())

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val prev = (zoomPagerState.currentPage - 1 + totalInBranch) % totalInBranch
                                coroutineScope.launch { zoomPagerState.animateScrollToPage(prev) }
                            }
                            .testTag("btn_zoom_prev_lesson"),
                        shape = RoundedCornerShape(8.dp),
                        color = BookParchmentLight,
                        border = BorderStroke(1.2.dp, BookBorderOuter)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
                            Text(
                                text = Translations.tr("book_btn_prev_lesson", lang),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BookBurgundyDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BookBurgundyDark,
                        border = BorderStroke(1.dp, BookGoldAccent)
                    ) {
                        Text(
                            text = "📖 $pageIndicatorText",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFF8E7),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val next = (zoomPagerState.currentPage + 1) % totalInBranch
                                coroutineScope.launch { zoomPagerState.animateScrollToPage(next) }
                            }
                            .testTag("btn_zoom_next_lesson"),
                        shape = RoundedCornerShape(8.dp),
                        color = BookBurgundy,
                        border = BorderStroke(1.2.dp, BookGoldAccent)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
                            Text(
                                text = Translations.tr("book_btn_next_lesson", lang),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFF8E7),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
