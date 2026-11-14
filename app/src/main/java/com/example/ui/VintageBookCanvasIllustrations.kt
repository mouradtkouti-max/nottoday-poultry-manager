package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.EncyclopediaPosterSpec
import com.example.models.InfographicVisualType
import com.example.utils.AppLanguage
import com.example.utils.Translations
import kotlin.math.cos
import kotlin.math.sin

val BookParchmentLight = Color(0xFFF5E6C8)
val BookParchmentMid = Color(0xFFEFE0BE)
val BookParchmentDeep = Color(0xFFE8D0A7)
val BookBurgundy = Color(0xFF5C1D18)
val BookBurgundyDark = Color(0xFF3E110E)
val BookBorderOuter = Color(0xFF6E3B1E)
val BookBorderInner = Color(0xFFA67B5B)
val BookGoldAccent = Color(0xFFD4AF37)
val BookSepiaInk = Color(0xFF5C3A21)

/**
 * Helper to draw a full-featured side-profile chick or broiler chicken with head, serrated comb,
 * beak, eye, neck, chest, wing feather lines, tail, and legs.
 */
private fun DrawScope.drawDetailedBirdSilhouette(
    cx: Float,
    cy: Float,
    scaleW: Float,
    scaleH: Float,
    bodyFill: Color,
    strokeColor: Color,
    isMatureBroiler: Boolean = false
) {
    // Full anatomical side-profile path (facing left: head on left, tail on right)
    val bodyPath = Path().apply {
        // Start at throat under beak
        moveTo(cx - scaleW * 0.32f, cy - scaleH * 0.16f)
        // Crown of head
        cubicTo(
            cx - scaleW * 0.36f, cy - scaleH * 0.42f,
            cx - scaleW * 0.16f, cy - scaleH * 0.44f,
            cx - scaleW * 0.12f, cy - scaleH * 0.26f
        )
        // Back of neck sweeping to dorsal back & tail
        cubicTo(
            cx - scaleW * 0.04f, cy - scaleH * 0.14f,
            cx + scaleW * 0.18f, cy - scaleH * 0.22f,
            cx + scaleW * 0.38f, cy - scaleH * 0.30f
        )
        // Tail feathers tip
        lineTo(cx + scaleW * 0.44f, cy - scaleH * 0.12f)
        lineTo(cx + scaleW * 0.38f, cy + scaleH * 0.04f)
        // Vent & rounded lower abdomen + deep broiler breast
        cubicTo(
            cx + scaleW * 0.34f, cy + scaleH * 0.32f,
            cx - scaleW * 0.08f, cy + scaleH * 0.38f,
            cx - scaleW * 0.24f, cy + scaleH * 0.18f
        )
        // Breast up to throat
        cubicTo(
            cx - scaleW * 0.32f, cy + scaleH * 0.06f,
            cx - scaleW * 0.30f, cy - scaleH * 0.06f,
            cx - scaleW * 0.32f, cy - scaleH * 0.16f
        )
        close()
    }
    drawPath(bodyPath, color = bodyFill)
    drawPath(bodyPath, color = strokeColor, style = Stroke(width = 2.2f, join = StrokeJoin.Round))

    // Serrated Comb (العرف المسنن)
    val combColor = if (isMatureBroiler) Color(0xFFDC2626) else Color(0xFFEA580C)
    val combR = if (isMatureBroiler) scaleH * 0.065f else scaleH * 0.048f
    drawCircle(combColor, radius = combR, center = Offset(cx - scaleW * 0.28f, cy - scaleH * 0.38f))
    drawCircle(combColor, radius = combR * 1.1f, center = Offset(cx - scaleW * 0.22f, cy - scaleH * 0.40f))
    drawCircle(combColor, radius = combR * 0.9f, center = Offset(cx - scaleW * 0.16f, cy - scaleH * 0.37f))

    // Pointed Beak (المنقار)
    val beakPath = Path().apply {
        moveTo(cx - scaleW * 0.32f, cy - scaleH * 0.26f)
        lineTo(cx - scaleW * 0.45f, cy - scaleH * 0.21f)
        lineTo(cx - scaleW * 0.32f, cy - scaleH * 0.16f)
        close()
    }
    drawPath(beakPath, color = Color(0xFFD97706))
    drawPath(beakPath, color = strokeColor, style = Stroke(width = 1.2f))

    // Wattle under beak
    if (isMatureBroiler) {
        drawCircle(
            color = Color(0xFFDC2626),
            radius = scaleH * 0.045f,
            center = Offset(cx - scaleW * 0.31f, cy - scaleH * 0.12f)
        )
    }

    // Eye with white highlight
    val eyeCenter = Offset(cx - scaleW * 0.25f, cy - scaleH * 0.26f)
    drawCircle(color = BookBurgundyDark, radius = 3.0f, center = eyeCenter)
    drawCircle(color = Color.White, radius = 1.1f, center = Offset(eyeCenter.x - 0.8f, eyeCenter.y - 0.8f))

    // Subtle Wing outline (الجناح)
    val wingPath = Path().apply {
        moveTo(cx - scaleW * 0.02f, cy - scaleH * 0.08f)
        cubicTo(
            cx + scaleW * 0.16f, cy - scaleH * 0.10f,
            cx + scaleW * 0.24f, cy + scaleH * 0.06f,
            cx + scaleW * 0.08f, cy + scaleH * 0.14f
        )
    }
    drawPath(wingPath, color = strokeColor.copy(alpha = 0.28f), style = Stroke(width = 1.5f, cap = StrokeCap.Round))

    // Two anatomical legs & claws (الساقان والأصابع)
    val legColor = Color(0xFFD97706)
    val leg1X = cx - scaleW * 0.02f
    val leg2X = cx + scaleW * 0.10f
    val legTopY = cy + scaleH * 0.29f
    val legBottomY = cy + scaleH * 0.46f
    drawLine(legColor, Offset(leg1X, legTopY), Offset(leg1X - 3f, legBottomY), strokeWidth = 2.8f, cap = StrokeCap.Round)
    drawLine(legColor, Offset(leg1X - 3f, legBottomY), Offset(leg1X - 10f, legBottomY), strokeWidth = 2.2f, cap = StrokeCap.Round)
    drawLine(legColor, Offset(leg1X - 3f, legBottomY), Offset(leg1X + 3f, legBottomY), strokeWidth = 2.2f, cap = StrokeCap.Round)

    drawLine(legColor, Offset(leg2X, legTopY), Offset(leg2X - 2f, legBottomY), strokeWidth = 2.8f, cap = StrokeCap.Round)
    drawLine(legColor, Offset(leg2X - 2f, legBottomY), Offset(leg2X - 9f, legBottomY), strokeWidth = 2.2f, cap = StrokeCap.Round)
    drawLine(legColor, Offset(leg2X - 2f, legBottomY), Offset(leg2X + 4f, legBottomY), strokeWidth = 2.2f, cap = StrokeCap.Round)
}

@Composable
fun SpiralBindingRingsCanvas(
    ringCount: Int = 7,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .width(24.dp)
            .testTag("vintage_spiral_binding_rings")
    ) {
        val w = size.width
        val h = size.height
        val count = ringCount.coerceAtLeast(7)
        val spacing = h / (count + 1)
        for (i in 1..count) {
            val cy = spacing * i
            val holeCenter = Offset(w * 0.76f, cy)
            drawCircle(
                color = Color(0xFF2B1810),
                radius = 5.5f,
                center = holeCenter
            )
            drawCircle(
                color = BookBorderOuter,
                radius = 6.5f,
                center = holeCenter,
                style = Stroke(width = 1.5f)
            )
            drawRoundRect(
                color = Color(0xFF334155),
                topLeft = Offset(1f, cy - 6.5f),
                size = Size(w * 0.78f, 5.5f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = Color(0xFF94A3B8),
                topLeft = Offset(1.5f, cy - 6f),
                size = Size(w * 0.75f, 3.8f),
                cornerRadius = CornerRadius(3f, 3f)
            )
            drawLine(
                color = Color(0xFFF1F5F9),
                start = Offset(3f, cy - 4.5f),
                end = Offset(w * 0.68f, cy - 4.5f),
                strokeWidth = 1.6f,
                cap = StrokeCap.Round
            )
            drawRoundRect(
                color = Color(0xFF475569),
                topLeft = Offset(1f, cy + 1f),
                size = Size(w * 0.78f, 5f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawLine(
                color = Color(0xFFCBD5E1),
                start = Offset(3f, cy + 2.8f),
                end = Offset(w * 0.68f, cy + 2.8f),
                strokeWidth = 1.4f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun VintageRoosterLogoCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(28.dp)) {
        val w = size.width
        val h = size.height
        drawCircle(color = BookParchmentDeep, radius = w * 0.48f, center = Offset(w / 2f, h / 2f))
        drawCircle(color = BookBurgundy, radius = w * 0.48f, center = Offset(w / 2f, h / 2f), style = Stroke(width = 2.5f))
        val bodyPath = Path().apply {
            moveTo(w * 0.32f, h * 0.74f)
            cubicTo(w * 0.30f, h * 0.48f, w * 0.38f, h * 0.30f, w * 0.54f, h * 0.30f)
            cubicTo(w * 0.66f, h * 0.32f, w * 0.70f, h * 0.54f, w * 0.64f, h * 0.74f)
            close()
        }
        drawPath(bodyPath, color = BookBurgundy)
        drawCircle(color = Color(0xFFB91C1C), radius = w * 0.08f, center = Offset(w * 0.46f, h * 0.24f))
        drawCircle(color = Color(0xFFB91C1C), radius = w * 0.085f, center = Offset(w * 0.54f, h * 0.23f))
        drawCircle(color = Color(0xFFB91C1C), radius = w * 0.07f, center = Offset(w * 0.61f, h * 0.26f))
        val beak = Path().apply {
            moveTo(w * 0.58f, h * 0.34f)
            lineTo(w * 0.76f, h * 0.39f)
            lineTo(w * 0.58f, h * 0.44f)
            close()
        }
        drawPath(beak, color = BookGoldAccent)
        drawCircle(color = Color.White, radius = 1.6f, center = Offset(w * 0.52f, h * 0.37f))
    }
}

@Composable
fun VintageFoldedRibbonBanner(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val leftTail = Path().apply {
                moveTo(0f, h * 0.18f)
                lineTo(w * 0.07f, h * 0.18f)
                lineTo(w * 0.07f, h * 0.88f)
                lineTo(0f, h * 0.88f)
                lineTo(w * 0.035f, h * 0.53f)
                close()
            }
            drawPath(leftTail, color = BookBurgundyDark)
            val rightTail = Path().apply {
                moveTo(w, h * 0.18f)
                lineTo(w * 0.93f, h * 0.18f)
                lineTo(w * 0.93f, h * 0.88f)
                lineTo(w, h * 0.88f)
                lineTo(w * 0.965f, h * 0.53f)
                close()
            }
            drawPath(rightTail, color = BookBurgundyDark)
        }
        Surface(
            modifier = Modifier.padding(horizontal = 8.dp),
            shape = RoundedCornerShape(6.dp),
            color = BookBurgundy,
            border = BorderStroke(1.2.dp, BookGoldAccent)
        ) {
            Text(
                text = text,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFFF8E7),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun BurgundySectionRibbon(
    title: String,
    modifier: Modifier = Modifier,
    ribbonColor: Color = BookBurgundy
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = ribbonColor,
        border = BorderStroke(1.dp, BookGoldAccent)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFFF8E7),
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun PolaroidLessonFrame(
    caption: String,
    emoji: String,
    visualType: InfographicVisualType,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(86.dp)
            .graphicsLayer(rotationZ = -4f)
            .testTag("vintage_polaroid_frame"),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            modifier = Modifier
                .padding(top = 5.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            color = Color(0xFFFFFDF7),
            border = BorderStroke(1.dp, BookBorderInner),
            shadowElevation = 3.dp
        ) {
            Column(
                modifier = Modifier.padding(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(Color(0xFFFAF0D7), RoundedCornerShape(3.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        drawRect(
                            color = Color(0xFFE6B86A),
                            topLeft = Offset(0f, h * 0.68f),
                            size = Size(w, h * 0.32f)
                        )
                        drawCircle(
                            color = Color(0xFFEA580C).copy(alpha = 0.25f),
                            radius = h * 0.42f,
                            center = Offset(w * 0.5f, h * 0.38f)
                        )
                        drawDetailedBirdSilhouette(
                            cx = w * 0.52f,
                            cy = h * 0.54f,
                            scaleW = w * 0.48f,
                            scaleH = h * 0.62f,
                            bodyFill = Color(0xFFFDE68A),
                            strokeColor = BookBorderOuter,
                            isMatureBroiler = false
                        )
                    }
                }
                Text(
                    text = caption,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BookBurgundyDark,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Box(
            modifier = Modifier
                .width(34.dp)
                .height(10.dp)
                .graphicsLayer(rotationZ = 3f)
                .background(Color(0xFFE5CFA0).copy(alpha = 0.9f), RoundedCornerShape(2.dp))
        )
    }
}

@Composable
fun BroilerHeadMedallionCanvas(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .size(58.dp)
            .testTag("vintage_broiler_head_medallion"),
        shape = CircleShape,
        color = BookParchmentMid,
        border = BorderStroke(1.4.dp, BookBorderOuter)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            val w = size.width
            val h = size.height
            val headPath = Path().apply {
                moveTo(w * 0.24f, h * 0.90f)
                cubicTo(w * 0.20f, h * 0.54f, w * 0.28f, h * 0.24f, w * 0.54f, h * 0.24f)
                cubicTo(w * 0.74f, h * 0.24f, w * 0.80f, h * 0.48f, w * 0.74f, h * 0.90f)
                close()
            }
            drawPath(headPath, color = Color(0xFFFFF8E7))
            drawPath(headPath, color = BookSepiaInk, style = Stroke(width = 2.4f))
            // Serrated 4-point comb
            drawCircle(color = BookBurgundy, radius = w * 0.085f, center = Offset(w * 0.42f, h * 0.20f))
            drawCircle(color = BookBurgundy, radius = w * 0.10f, center = Offset(w * 0.52f, h * 0.17f))
            drawCircle(color = BookBurgundy, radius = w * 0.09f, center = Offset(w * 0.62f, h * 0.19f))
            drawCircle(color = BookBurgundy, radius = w * 0.075f, center = Offset(w * 0.70f, h * 0.24f))
            // Upper & lower beak
            val beak = Path().apply {
                moveTo(w * 0.32f, h * 0.38f)
                lineTo(w * 0.08f, h * 0.46f)
                lineTo(w * 0.32f, h * 0.53f)
                close()
            }
            drawPath(beak, color = Color(0xFFD97706))
            drawPath(beak, color = BookSepiaInk, style = Stroke(width = 1.4f))
            // Red wattle & ear lobe
            drawCircle(color = BookBurgundy, radius = w * 0.085f, center = Offset(w * 0.32f, h * 0.61f))
            // Detailed eye with iris & pupil
            drawCircle(color = Color(0xFFD97706), radius = 4.2f, center = Offset(w * 0.45f, h * 0.41f))
            drawCircle(color = BookBurgundyDark, radius = 2.4f, center = Offset(w * 0.45f, h * 0.41f))
            drawCircle(color = Color.White, radius = 1.0f, center = Offset(w * 0.43f, h * 0.39f))
        }
    }
}

@Composable
fun LessonHeroSceneCanvas(
    spec: EncyclopediaPosterSpec,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("infographic_canvas_${spec.visualType.name.lowercase()}"),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFAF0D7),
        border = BorderStroke(1.4.dp, BookBorderOuter)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(138.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    // Barn interior background
                    drawRect(
                        color = Color(0xFFF3DFB9),
                        topLeft = Offset(0f, 0f),
                        size = Size(w, h * 0.66f)
                    )
                    // Thick golden wood shavings litter bed (bottom 34%)
                    drawRoundRect(
                        color = Color(0xFFD99B43),
                        topLeft = Offset(0f, h * 0.66f),
                        size = Size(w, h * 0.34f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    for (i in 0..16) {
                        val fx = w * (0.04f + (i * 0.056f))
                        val fy = h * (0.72f + (i % 3) * 0.08f)
                        drawLine(
                            color = BookBorderOuter,
                            start = Offset(fx, fy),
                            end = Offset(fx + 9f, fy + 2f),
                            strokeWidth = 2f,
                            cap = StrokeCap.Round
                        )
                    }

                    if (spec.visualType == InfographicVisualType.CARE_VENTILATION_AIRFLOW) {
                        // Barn cross-section: pitched roof, side upper inlet, exhaust fan, and warm/cold airflow arrows
                        val roofPath = Path().apply {
                            moveTo(w * 0.04f, h * 0.24f)
                            lineTo(w * 0.50f, h * 0.05f)
                            lineTo(w * 0.96f, h * 0.24f)
                        }
                        drawPath(roofPath, color = BookBorderOuter, style = Stroke(width = 3.2f, cap = StrokeCap.Round))
                        // Left upper inlet baffle angled toward ceiling
                        drawLine(
                            color = Color(0xFF1D4ED8),
                            start = Offset(w * 0.06f, h * 0.32f),
                            end = Offset(w * 0.16f, h * 0.22f),
                            strokeWidth = 4f,
                            cap = StrokeCap.Round
                        )
                        // Cold air arrows entering high & mixing with warm air at ceiling
                        val coldArc = Path().apply {
                            moveTo(w * 0.08f, h * 0.28f)
                            cubicTo(w * 0.25f, h * 0.12f, w * 0.45f, h * 0.12f, w * 0.62f, h * 0.26f)
                        }
                        drawPath(coldArc, color = Color(0xFF2563EB), style = Stroke(width = 3f, cap = StrokeCap.Round))
                        // Warm mixed air circulation arrows
                        val warmArc = Path().apply {
                            moveTo(w * 0.35f, h * 0.22f)
                            cubicTo(w * 0.52f, h * 0.20f, w * 0.68f, h * 0.35f, w * 0.84f, h * 0.42f)
                        }
                        drawPath(warmArc, color = Color(0xFFEA580C), style = Stroke(width = 3f, cap = StrokeCap.Round))
                        // Right exhaust fan housing
                        drawRoundRect(
                            color = Color(0xFF334155),
                            topLeft = Offset(w * 0.86f, h * 0.28f),
                            size = Size(w * 0.10f, h * 0.32f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                        drawCircle(color = Color(0xFF94A3B8), radius = 9f, center = Offset(w * 0.91f, h * 0.44f))
                    } else {
                        // Suspended metallic brooder canopy with radiant heat cone
                        val conePath = Path().apply {
                            moveTo(w * 0.36f, h * 0.22f)
                            lineTo(w * 0.60f, h * 0.22f)
                            lineTo(w * 0.80f, h * 0.66f)
                            lineTo(w * 0.16f, h * 0.66f)
                            close()
                        }
                        drawPath(conePath, color = Color(0xFFF59E0B).copy(alpha = 0.22f))
                        // Suspension chain
                        drawLine(
                            color = Color(0xFF475569),
                            start = Offset(w * 0.48f, 0f),
                            end = Offset(w * 0.48f, h * 0.08f),
                            strokeWidth = 2.5f
                        )
                        // Metallic brooder reflector dome
                        val hoodPath = Path().apply {
                            moveTo(w * 0.35f, h * 0.08f)
                            lineTo(w * 0.61f, h * 0.08f)
                            lineTo(w * 0.72f, h * 0.23f)
                            lineTo(w * 0.24f, h * 0.23f)
                            close()
                        }
                        drawPath(hoodPath, color = BookBurgundy)
                        drawPath(hoodPath, color = BookGoldAccent, style = Stroke(width = 2f))
                        // Glowing burner core
                        drawCircle(
                            color = Color(0xFFEA580C),
                            radius = 10f,
                            center = Offset(w * 0.48f, h * 0.24f)
                        )
                        drawCircle(
                            color = Color(0xFFFDE047),
                            radius = 5.5f,
                            center = Offset(w * 0.48f, h * 0.24f)
                        )
                        // Radiant heat wave lines
                        for (ray in -2..2) {
                            val rx = w * 0.48f + ray * (w * 0.08f)
                            drawLine(
                                color = Color(0xFFEA580C).copy(alpha = 0.55f),
                                start = Offset(w * 0.48f, h * 0.27f),
                                end = Offset(rx, h * 0.62f),
                                strokeWidth = 2.2f,
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // Detailed chicks with full features (head, comb, beak, eye, wing, legs) spread on litter
                    val chickCenters = listOf(0.20f, 0.38f, 0.56f, 0.74f)
                    chickCenters.forEachIndexed { idx, cxRatio ->
                        val cyPos = h * (if (idx % 2 == 0) 0.63f else 0.67f)
                        drawDetailedBirdSilhouette(
                            cx = w * cxRatio,
                            cy = cyPos,
                            scaleW = 34f,
                            scaleH = 28f,
                            bodyFill = Color(0xFFFBBF24),
                            strokeColor = BookBorderOuter,
                            isMatureBroiler = spec.visualType == InfographicVisualType.FEED_FINISHER_PELLETS ||
                                spec.visualType == InfographicVisualType.FEED_WEIGHT_WATER_CURVE
                        )
                    }

                    // Detailed mercury thermometer with graduation tick marks on the right
                    val tx = w * 0.91f
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(tx - 7f, h * 0.10f),
                        size = Size(14f, h * 0.52f),
                        cornerRadius = CornerRadius(7f, 7f)
                    )
                    drawRoundRect(
                        color = BookBorderOuter,
                        topLeft = Offset(tx - 7f, h * 0.10f),
                        size = Size(14f, h * 0.52f),
                        cornerRadius = CornerRadius(7f, 7f),
                        style = Stroke(width = 1.8f)
                    )
                    drawRoundRect(
                        color = Color(0xFFDC2626),
                        topLeft = Offset(tx - 3.2f, h * 0.22f),
                        size = Size(6.4f, h * 0.40f),
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                    drawCircle(
                        color = Color(0xFFDC2626),
                        radius = 9f,
                        center = Offset(tx, h * 0.63f)
                    )
                    drawCircle(
                        color = BookBorderOuter,
                        radius = 9f,
                        center = Offset(tx, h * 0.63f),
                        style = Stroke(width = 1.5f)
                    )
                    for (t in 0..4) {
                        val ty = h * (0.16f + t * 0.08f)
                        drawLine(
                            color = BookBurgundyDark,
                            start = Offset(tx - 6f, ty),
                            end = Offset(tx - 2f, ty),
                            strokeWidth = 1.4f
                        )
                    }
                }

                // Callout Badges
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = BookBurgundy,
                        border = BorderStroke(0.8.dp, BookGoldAccent)
                    ) {
                        Text(
                            text = "🔥 ${spec.heroCalloutTop} ➔",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFF8E7),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp),
                    shape = RoundedCornerShape(5.dp),
                    color = BookParchmentLight,
                    border = BorderStroke(1.dp, BookBorderOuter)
                ) {
                    Text(
                        text = "🪵 ${spec.heroCalloutBottom} ➔",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BookBurgundyDark,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HorizontalStepIllustrationCanvas(
    stepIndex: Int,
    emoji: String,
    visualType: InfographicVisualType = InfographicVisualType.CARE_PREHEAT_PREP,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFFFAF0D7), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Ground floor strip
            drawRect(Color(0xFFE2CCA4), Offset(0f, h * 0.74f), Size(w, h * 0.26f))

            when {
                // Vaccination lessons: vaccine vial, eye dropper, drinking bell drinker
                visualType.name.startsWith("VAX_") && stepIndex == 0 -> {
                    // Cold-chain vaccine vial + ice cube
                    drawRoundRect(
                        color = Color(0xFFDBEAFE),
                        topLeft = Offset(w * 0.22f, h * 0.34f),
                        size = Size(w * 0.22f, h * 0.38f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    drawRoundRect(
                        color = Color(0xFF1D4ED8),
                        topLeft = Offset(w * 0.50f, h * 0.22f),
                        size = Size(w * 0.22f, h * 0.50f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    drawRect(Color(0xFF93C5FD), Offset(w * 0.54f, h * 0.14f), Size(w * 0.14f, h * 0.08f))
                }
                visualType.name.startsWith("VAX_") && stepIndex == 1 -> {
                    // Eye dropper / coarse spray droplet arc
                    val dropPath = Path().apply {
                        moveTo(w * 0.50f, h * 0.16f)
                        cubicTo(w * 0.65f, h * 0.42f, w * 0.62f, h * 0.66f, w * 0.50f, h * 0.68f)
                        cubicTo(w * 0.38f, h * 0.66f, w * 0.35f, h * 0.42f, w * 0.50f, h * 0.16f)
                        close()
                    }
                    drawPath(dropPath, color = Color(0xFF0284C7))
                }
                // Feed & Weight lessons: feeder aligned to bird back, quintal sack, weighing scale
                visualType.name.startsWith("FEED_") && stepIndex == 0 -> {
                    // Red pan feeder + golden crumble feed
                    drawLine(BookBorderOuter, Offset(w * 0.5f, 0f), Offset(w * 0.5f, h * 0.52f), strokeWidth = 2.5f)
                    drawRoundRect(
                        color = Color(0xFFDC2626),
                        topLeft = Offset(w * 0.26f, h * 0.50f),
                        size = Size(w * 0.48f, h * 0.24f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    drawOval(Color(0xFFF59E0B), Offset(w * 0.32f, h * 0.44f), Size(w * 0.36f, h * 0.14f))
                }
                visualType.name.startsWith("FEED_") && stepIndex == 2 -> {
                    // Weighing scale dial & platform
                    drawCircle(Color.White, radius = h * 0.26f, center = Offset(w * 0.50f, h * 0.38f))
                    drawCircle(BookBurgundy, radius = h * 0.26f, center = Offset(w * 0.50f, h * 0.38f), style = Stroke(width = 2.2f))
                    drawLine(Color(0xFFDC2626), Offset(w * 0.50f, h * 0.38f), Offset(w * 0.60f, h * 0.26f), strokeWidth = 2.2f)
                    drawRoundRect(BookBorderOuter, Offset(w * 0.28f, h * 0.68f), Size(w * 0.44f, h * 0.12f), CornerRadius(3f, 3f))
                }
                stepIndex == 0 -> {
                    // Step 1: Broom sweeping floor & water spray sparkles
                    drawLine(
                        color = BookBorderOuter,
                        start = Offset(w * 0.28f, h * 0.10f),
                        end = Offset(w * 0.54f, h * 0.58f),
                        strokeWidth = 3.8f,
                        cap = StrokeCap.Round
                    )
                    val bristles = Path().apply {
                        moveTo(w * 0.44f, h * 0.55f)
                        lineTo(w * 0.68f, h * 0.52f)
                        lineTo(w * 0.74f, h * 0.74f)
                        lineTo(w * 0.40f, h * 0.74f)
                        close()
                    }
                    drawPath(bristles, color = Color(0xFFD97706))
                    drawPath(bristles, color = BookBorderOuter, style = Stroke(width = 1.4f))
                    drawCircle(Color(0xFF38BDF8), radius = 3f, center = Offset(w * 0.24f, h * 0.56f))
                    drawCircle(Color(0xFF38BDF8), radius = 2.2f, center = Offset(w * 0.32f, h * 0.44f))
                }
                stepIndex == 1 -> {
                    // Step 2: Tilted shavings sack pouring golden wood flakes onto 5-8 cm bed
                    drawRoundRect(
                        color = Color(0xFFD99B43),
                        topLeft = Offset(w * 0.10f, h * 0.58f),
                        size = Size(w * 0.80f, h * 0.24f),
                        cornerRadius = CornerRadius(5f, 5f)
                    )
                    drawRoundRect(
                        color = Color(0xFF92400E),
                        topLeft = Offset(w * 0.22f, h * 0.14f),
                        size = Size(w * 0.32f, h * 0.32f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    // Falling golden wood flakes
                    drawCircle(Color(0xFFF59E0B), radius = 3.2f, center = Offset(w * 0.58f, h * 0.36f))
                    drawCircle(Color(0xFFF59E0B), radius = 2.8f, center = Offset(w * 0.64f, h * 0.48f))
                    drawCircle(Color(0xFFF59E0B), radius = 3.0f, center = Offset(w * 0.52f, h * 0.50f))
                }
                else -> {
                    // Step 3: Radiant brooder heater + warm cone + 33°C thermometer
                    val cone = Path().apply {
                        moveTo(w * 0.34f, h * 0.28f)
                        lineTo(w * 0.66f, h * 0.28f)
                        lineTo(w * 0.82f, h * 0.74f)
                        lineTo(w * 0.18f, h * 0.74f)
                        close()
                    }
                    drawPath(cone, color = Color(0xFFEA580C).copy(alpha = 0.24f))
                    drawRoundRect(
                        color = BookBurgundy,
                        topLeft = Offset(w * 0.30f, h * 0.12f),
                        size = Size(w * 0.40f, h * 0.18f),
                        cornerRadius = CornerRadius(5f, 5f)
                    )
                    drawCircle(Color(0xFFF59E0B), radius = 6f, center = Offset(w * 0.50f, h * 0.31f))
                }
            }
        }
        Text(
            text = emoji,
            fontSize = 15.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(3.dp)
        )
    }
}

@Composable
fun ChickInternalAnatomyCanvas(
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val tracheaLabel = Translations.tr("book_chick_trachea", lang)
    val lungsLabel = Translations.tr("book_chick_lungs", lang)
    val digestiveLabel = Translations.tr("book_chick_digestive", lang)
    val cropYolkLabel = Translations.tr("book_chick_crop_yolk", lang)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vintage_chick_anatomy_center"),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFAF0D7),
        border = BorderStroke(1.2.dp, BookBorderOuter)
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            BurgundySectionRibbon(
                title = Translations.tr("book_ribbon_chick_anatomy", lang),
                modifier = Modifier.fillMaxWidth()
            )
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
            ) {
                val w = size.width
                val h = size.height
                val cx = w * 0.52f
                val cy = h * 0.52f

                // 1. Complete Anatomical Cutaway Chick Silhouette (Head, Comb, Beak, Eye, Wing, Tail, Legs)
                drawDetailedBirdSilhouette(
                    cx = cx,
                    cy = cy,
                    scaleW = w * 0.72f,
                    scaleH = h * 0.86f,
                    bodyFill = Color(0xFFFFF3C4),
                    strokeColor = BookBorderOuter,
                    isMatureBroiler = false
                )

                // 2. Cartilaginous Trachea (القصبة الهوائية بحلقات غضروفية) branching into Bronchi
                val tracheaStart = Offset(cx - w * 0.20f, cy - h * 0.16f)
                val tracheaFork = Offset(cx - w * 0.04f, cy - h * 0.05f)
                drawLine(
                    color = Color(0xFF2563EB),
                    start = tracheaStart,
                    end = tracheaFork,
                    strokeWidth = 4.5f,
                    cap = StrokeCap.Round
                )
                // Cartilaginous rings along trachea
                for (r in 1..4) {
                    val t = r / 5f
                    val rx = tracheaStart.x + (tracheaFork.x - tracheaStart.x) * t
                    val ry = tracheaStart.y + (tracheaFork.y - tracheaStart.y) * t
                    drawCircle(color = Color(0xFF93C5FD), radius = 2.0f, center = Offset(rx, ry))
                }

                // 3. Detailed Dual Lungs & Air Sacs (الرئتان والأكياس الهوائية)
                drawOval(
                    color = Color(0xFFE11D48),
                    topLeft = Offset(cx - w * 0.05f, cy - h * 0.18f),
                    size = Size(w * 0.19f, h * 0.17f)
                )
                drawOval(
                    color = Color(0xFFFDA4AF).copy(alpha = 0.55f),
                    topLeft = Offset(cx + w * 0.08f, cy - h * 0.12f),
                    size = Size(w * 0.14f, h * 0.14f)
                )

                // 4. Crop (الحوصلة), Gizzard (القانصة), Intestines (الأمعاء) & Yolk Sac (كيس المح)
                // Crop at base of neck
                val cropCenter = Offset(cx - w * 0.14f, cy + h * 0.04f)
                drawCircle(color = Color(0xFFF59E0B), radius = h * 0.075f, center = cropCenter)
                // Gizzard
                val gizzardCenter = Offset(cx - w * 0.01f, cy + h * 0.08f)
                drawOval(
                    color = Color(0xFFB45309),
                    topLeft = Offset(gizzardCenter.x - 8f, gizzardCenter.y - 6f),
                    size = Size(16f, 12f)
                )
                // Coiled Intestines & Yolk Sac
                val gutCenter = Offset(cx + w * 0.12f, cy + h * 0.10f)
                drawOval(
                    color = Color(0xFF059669),
                    topLeft = Offset(gutCenter.x - 10f, gutCenter.y - 8f),
                    size = Size(22f, 16f)
                )
                drawCircle(
                    color = Color(0xFFEAB308),
                    radius = h * 0.065f,
                    center = Offset(cx + w * 0.06f, cy + h * 0.20f)
                )

                // 5. Pointer callout lines & colored dots
                // Trachea pointer
                drawLine(Color(0xFF1E3A8A), Offset(tracheaStart.x, tracheaStart.y), Offset(w * 0.10f, h * 0.16f), strokeWidth = 1.3f)
                drawCircle(Color(0xFF1E3A8A), radius = 3f, center = Offset(w * 0.10f, h * 0.16f))
                // Lungs pointer
                drawLine(BookBurgundy, Offset(cx + w * 0.04f, cy - h * 0.12f), Offset(w * 0.88f, h * 0.16f), strokeWidth = 1.3f)
                drawCircle(BookBurgundy, radius = 3f, center = Offset(w * 0.88f, h * 0.16f))
                // Crop & Digestive pointer
                drawLine(Color(0xFF065F46), gutCenter, Offset(w * 0.88f, h * 0.82f), strokeWidth = 1.3f)
                drawCircle(Color(0xFF065F46), radius = 3f, center = Offset(w * 0.88f, h * 0.82f))
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("🔹 $tracheaLabel", fontSize = 7.8.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E3A8A))
                Text("🔺 $lungsLabel", fontSize = 7.8.sp, fontWeight = FontWeight.ExtraBold, color = BookBurgundy)
                Text("🟠 $cropYolkLabel", fontSize = 7.8.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFB45309))
                Text("🟢 $digestiveLabel", fontSize = 7.8.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF065F46))
            }
        }
    }
}

@Composable
fun GoodBadSceneFrameCanvas(
    isGood: Boolean,
    ribbonTitle: String,
    caption: String,
    modifier: Modifier = Modifier
) {
    val frameBorder = if (isGood) Color(0xFF15803D) else Color(0xFFB91C1C)
    val ribbonBg = if (isGood) Color(0xFF166534) else BookBurgundy
    val badgeSymbol = if (isGood) "✅" else "❌"

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFAF0D7),
        border = BorderStroke(1.2.dp, frameBorder)
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BurgundySectionRibbon(
                title = ribbonTitle,
                modifier = Modifier.fillMaxWidth(),
                ribbonColor = ribbonBg
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(
                        if (isGood) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                        RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    if (isGood) {
                        // Dry golden litter + evenly spread healthy chicks
                        drawRect(Color(0xFFD99B43), Offset(0f, h * 0.66f), Size(w, h * 0.34f))
                        listOf(0.24f, 0.52f, 0.78f).forEach { cxRatio ->
                            drawDetailedBirdSilhouette(
                                cx = w * cxRatio,
                                cy = h * 0.56f,
                                scaleW = 24f,
                                scaleH = 20f,
                                bodyFill = Color(0xFFFBBF24),
                                strokeColor = BookBorderOuter,
                                isMatureBroiler = false
                            )
                        }
                    } else {
                        // Cold wet dark litter puddles + huddled chilled chicks
                        drawRect(Color(0xFF64748B), Offset(0f, h * 0.66f), Size(w, h * 0.34f))
                        drawOval(Color(0xFF3B82F6).copy(alpha = 0.45f), Offset(w * 0.16f, h * 0.70f), Size(w * 0.68f, h * 0.22f))
                        listOf(0.44f, 0.56f).forEach { cxRatio ->
                            drawDetailedBirdSilhouette(
                                cx = w * cxRatio,
                                cy = h * 0.58f,
                                scaleW = 22f,
                                scaleH = 19f,
                                bodyFill = Color(0xFFCBD5E1),
                                strokeColor = Color(0xFF334155),
                                isMatureBroiler = false
                            )
                        }
                    }
                }
                Text(
                    text = badgeSymbol,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(3.dp)
                )
            }
            Text(
                text = caption,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                color = BookBurgundyDark,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
fun ChickenDiseaseAnatomyZoomCanvas(
    spec: EncyclopediaPosterSpec,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("infographic_canvas_${spec.visualType.name.lowercase()}"),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFAF0D7),
        border = BorderStroke(1.3.dp, BookBorderOuter)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📍 ${Translations.tr("book_ribbon_infection_sites", lang)}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BookBurgundy
                )
                Surface(
                    shape = RoundedCornerShape(5.dp),
                    color = BookBurgundy,
                    border = BorderStroke(0.8.dp, BookGoldAccent)
                ) {
                    Text(
                        text = "🦠 ${spec.pathogenCode}",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val birdCenterX = w * 0.64f
                    val birdCenterY = h * 0.52f

                    // 1. Full anatomical cutaway broiler hen silhouette (Head, Serrated Comb, Beak, Eye, Neck, Breast, Wing, Tail, Legs)
                    drawDetailedBirdSilhouette(
                        cx = birdCenterX,
                        cy = birdCenterY,
                        scaleW = w * 0.62f,
                        scaleH = h * 0.84f,
                        bodyFill = Color(0xFFFFF8E7),
                        strokeColor = BookBorderOuter,
                        isMatureBroiler = true
                    )

                    // 2. Cartilaginous Trachea (القصبة الهوائية) & Bronchial Bifurcation (الشعبتان)
                    val trStart = Offset(birdCenterX - w * 0.16f, birdCenterY - h * 0.18f)
                    val trFork = Offset(birdCenterX - w * 0.04f, birdCenterY - h * 0.02f)
                    drawLine(
                        color = Color(0xFFDC2626),
                        start = trStart,
                        end = trFork,
                        strokeWidth = 5.5f,
                        cap = StrokeCap.Round
                    )
                    for (ring in 1..5) {
                        val t = ring / 6f
                        drawCircle(
                            color = Color(0xFFFECACA),
                            radius = 2.1f,
                            center = Offset(
                                trStart.x + (trFork.x - trStart.x) * t,
                                trStart.y + (trFork.y - trStart.y) * t
                            )
                        )
                    }
                    // Bronchi fork into Lungs
                    drawLine(
                        color = Color(0xFFEA580C),
                        start = trFork,
                        end = Offset(birdCenterX + w * 0.05f, birdCenterY - h * 0.08f),
                        strokeWidth = 4.5f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFFEA580C),
                        start = trFork,
                        end = Offset(birdCenterX + w * 0.04f, birdCenterY + h * 0.06f),
                        strokeWidth = 4.5f,
                        cap = StrokeCap.Round
                    )

                    // 3. Detailed Lungs & Air Sacs (الرئتان والأكياس الهوائية)
                    drawOval(
                        color = Color(0xFFE11D48).copy(alpha = 0.85f),
                        topLeft = Offset(birdCenterX - w * 0.01f, birdCenterY - h * 0.16f),
                        size = Size(w * 0.16f, h * 0.22f)
                    )
                    drawOval(
                        color = Color(0xFFFDA4AF).copy(alpha = 0.55f),
                        topLeft = Offset(birdCenterX + w * 0.08f, birdCenterY - h * 0.06f),
                        size = Size(w * 0.12f, h * 0.18f)
                    )

                    // 4. Crop, Gizzard & Intestinal Tract / Ceca (الأمعاء والأعوران)
                    drawCircle(
                        color = Color(0xFFD97706),
                        radius = h * 0.065f,
                        center = Offset(birdCenterX - w * 0.11f, birdCenterY + h * 0.06f)
                    )
                    drawOval(
                        color = if (spec.visualType == InfographicVisualType.DIS_COCCIDIOSIS_GUT) Color(0xFFB91C1C) else Color(0xFF059669),
                        topLeft = Offset(birdCenterX + w * 0.02f, birdCenterY + h * 0.08f),
                        size = Size(w * 0.16f, h * 0.16f)
                    )

                    // 5. Circular Magnified Zoom Inset on the left (عدسة تكبير التهاب الشعب/الأمعاء)
                    val zoomCenter = Offset(w * 0.21f, h * 0.48f)
                    val zoomRadius = h * 0.35f
                    val targetOrganOffset = if (spec.visualType == InfographicVisualType.DIS_COCCIDIOSIS_GUT) {
                        Offset(birdCenterX + w * 0.06f, birdCenterY + h * 0.14f)
                    } else {
                        trFork
                    }
                    drawLine(
                        color = BookBurgundy,
                        start = targetOrganOffset,
                        end = Offset(zoomCenter.x + zoomRadius, zoomCenter.y),
                        strokeWidth = 2f
                    )
                    drawCircle(color = Color(0xFFFEF2F2), radius = zoomRadius, center = zoomCenter)
                    drawCircle(color = BookBurgundy, radius = zoomRadius, center = zoomCenter, style = Stroke(width = 3f))
                    // Inflamed bronchial/mucosal ring & yellow mucus plug inside Zoom Inset
                    drawCircle(color = Color(0xFFDC2626), radius = zoomRadius * 0.66f, center = zoomCenter, style = Stroke(width = 5.5f))
                    drawCircle(color = Color(0xFFFBBF24), radius = zoomRadius * 0.38f, center = zoomCenter)
                    for (deg in 0 until 360 step 45) {
                        val rad = deg * Math.PI / 180.0
                        drawLine(
                            color = BookBurgundyDark,
                            start = zoomCenter,
                            end = Offset(
                                (zoomCenter.x + cos(rad) * zoomRadius * 0.52f).toFloat(),
                                (zoomCenter.y + sin(rad) * zoomRadius * 0.52f).toFloat()
                            ),
                            strokeWidth = 2f
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(2.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = BookBurgundyDark
                ) {
                    Text(
                        text = "🔍 ${spec.zoomInsetTitle}",
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFF8E7),
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            // Anatomical callouts strip
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                spec.anatomyLabels.take(4).chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        rowItems.forEach { item ->
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(5.dp),
                                color = BookParchmentLight,
                                border = BorderStroke(0.8.dp, BookBorderInner)
                            ) {
                                Column(modifier = Modifier.padding(4.dp)) {
                                    Text(
                                        text = "• ${item.partName}",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BookBurgundy
                                    )
                                    Text(
                                        text = item.lesionNote,
                                        fontSize = 7.5.sp,
                                        color = BookBurgundyDark,
                                        lineHeight = 10.sp
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

@Composable
fun PinnedImportantNoteCard(
    title: String,
    noteText: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vintage_pinned_important_note"),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            modifier = Modifier
                .padding(top = 6.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFFFF8DC),
            border = BorderStroke(1.2.dp, BookBurgundy)
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BookBurgundy
                )
                Text(
                    text = noteText.removePrefix("⚠️ ملاحظة مهمة:").removePrefix("⚠️ Important Note:").trim(),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BookBurgundyDark,
                    lineHeight = 12.sp
                )
            }
        }
        Canvas(modifier = Modifier.size(14.dp)) {
            drawCircle(color = Color(0xFFDC2626), radius = 6f, center = Offset(size.width / 2f, 6f))
            drawCircle(color = Color(0xFFFCA5A5), radius = 2.2f, center = Offset(size.width / 2f - 1.5f, 4.5f))
            drawLine(
                color = Color(0xFF475569),
                start = Offset(size.width / 2f, 11f),
                end = Offset(size.width / 2f, 14f),
                strokeWidth = 2f
            )
        }
    }
}

@Composable
fun SepiaFarmEngravingFooterCanvas(
    pageNumber: Int,
    totalPages: Int,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val pageLabel = Translations.tr("book_page_footer_fmt", lang)
        .replace("{page}", pageNumber.toString())
        .replace("{total}", totalPages.toString())

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag("vintage_sepia_farm_footer"),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawLine(
                color = BookSepiaInk,
                start = Offset(0f, h * 0.78f),
                end = Offset(w, h * 0.78f),
                strokeWidth = 2f
            )
            val barnPath = Path().apply {
                moveTo(w * 0.06f, h * 0.78f)
                lineTo(w * 0.06f, h * 0.45f)
                lineTo(w * 0.14f, h * 0.22f)
                lineTo(w * 0.22f, h * 0.45f)
                lineTo(w * 0.22f, h * 0.78f)
                close()
            }
            drawPath(barnPath, color = BookSepiaInk.copy(alpha = 0.22f))
            drawPath(barnPath, color = BookSepiaInk, style = Stroke(width = 1.6f))
            drawRoundRect(
                color = BookSepiaInk.copy(alpha = 0.28f),
                topLeft = Offset(w * 0.24f, h * 0.26f),
                size = Size(w * 0.05f, h * 0.52f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            val rightBarn = Path().apply {
                moveTo(w * 0.76f, h * 0.78f)
                lineTo(w * 0.76f, h * 0.48f)
                lineTo(w * 0.85f, h * 0.26f)
                lineTo(w * 0.94f, h * 0.48f)
                lineTo(w * 0.94f, h * 0.78f)
                close()
            }
            drawPath(rightBarn, color = BookSepiaInk.copy(alpha = 0.22f))
            drawPath(rightBarn, color = BookSepiaInk, style = Stroke(width = 1.6f))
            drawCircle(color = BookSepiaInk.copy(alpha = 0.35f), radius = 8f, center = Offset(w * 0.71f, h * 0.50f))
            drawLine(BookSepiaInk, Offset(w * 0.71f, h * 0.58f), Offset(w * 0.71f, h * 0.78f), strokeWidth = 2f)
        }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = BookBurgundy,
            border = BorderStroke(1.dp, BookGoldAccent),
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Text(
                text = "📖 $pageLabel",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFFF8E7),
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 3.dp)
                    .testTag("vintage_book_page_number_badge")
            )
        }
    }
}
