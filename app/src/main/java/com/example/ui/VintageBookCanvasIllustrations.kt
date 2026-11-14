package com.example.ui

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.EncyclopediaPosterSpec
import com.example.models.InfographicVisualType
import com.example.utils.AppLanguage
import com.example.utils.BookNanoBananaImageEngine
import com.example.utils.Translations
import kotlin.math.cos
import kotlin.math.sin

val BookParchmentLight = Color(0xFFF5E6C8)
val BookParchmentCard = Color(0xFFFAF0D7)
val BookParchmentMid = Color(0xFFEFE0BE)
val BookParchmentDeep = Color(0xFFE8D0A7)
val BookBurgundy = Color(0xFF5C1D18)
val BookBurgundyDark = Color(0xFF3E110E)
val BookBorderOuter = Color(0xFF6E3B1E)
val BookBorderInner = Color(0xFFA67B5B)
val BookGoldAccent = Color(0xFFD4AF37)
val BookSepiaInk = Color(0xFF5C3A21)

/**
 * Multi-layered classic engraving & warm-shaded poultry silhouette:
 * Supports day-old fluffy chicks, mid-cycle growers, and mature Ross 308 broilers.
 */
private fun DrawScope.drawClassicBirdIllustration(
    cx: Float,
    cy: Float,
    scaleW: Float,
    scaleH: Float,
    bodyFill: Color,
    shadeFill: Color,
    strokeColor: Color,
    isMatureBroiler: Boolean = false,
    facingRight: Boolean = true,
    showSoftGroundShadow: Boolean = true
) {
    val dir = if (facingRight) 1f else -1f

    if (showSoftGroundShadow) {
        drawOval(
            color = BookSepiaInk.copy(alpha = 0.16f),
            topLeft = Offset(cx - scaleW * 0.34f, cy + scaleH * 0.42f),
            size = Size(scaleW * 0.68f, scaleH * 0.10f)
        )
    }

    // Main outer body contour
    val bodyPath = Path().apply {
        // Throat / breast front
        moveTo(cx + dir * scaleW * 0.24f, cy - scaleH * 0.16f)
        // Crown of head
        cubicTo(
            cx + dir * scaleW * 0.30f, cy - scaleH * 0.44f,
            cx + dir * scaleW * 0.06f, cy - scaleH * 0.45f,
            cx + dir * scaleW * 0.04f, cy - scaleH * 0.24f
        )
        // Neck & dorsal back sweep to tail
        cubicTo(
            cx - dir * scaleW * 0.04f, cy - scaleH * 0.12f,
            cx - dir * scaleW * 0.22f, cy - scaleH * 0.22f,
            cx - dir * scaleW * 0.42f, cy - scaleH * 0.32f
        )
        // Feathered tail tips
        lineTo(cx - dir * scaleW * 0.46f, cy - scaleH * 0.16f)
        lineTo(cx - dir * scaleW * 0.41f, cy - scaleH * 0.06f)
        lineTo(cx - dir * scaleW * 0.43f, cy + scaleH * 0.04f)
        // Deep rounded keel & breast
        cubicTo(
            cx - dir * scaleW * 0.34f, cy + scaleH * 0.32f,
            cx + dir * scaleW * 0.08f, cy + scaleH * 0.38f,
            cx + dir * scaleW * 0.26f, cy + scaleH * 0.18f
        )
        // Breast curve up to throat
        cubicTo(
            cx + dir * scaleW * 0.32f, cy + scaleH * 0.06f,
            cx + dir * scaleW * 0.28f, cy - scaleH * 0.06f,
            cx + dir * scaleW * 0.24f, cy - scaleH * 0.16f
        )
        close()
    }
    drawPath(bodyPath, color = bodyFill)

    // Warm lower-belly feather shading crescent (depth without gradients)
    val bellyShadePath = Path().apply {
        moveTo(cx - dir * scaleW * 0.34f, cy + scaleH * 0.06f)
        cubicTo(
            cx - dir * scaleW * 0.24f, cy + scaleH * 0.32f,
            cx + dir * scaleW * 0.10f, cy + scaleH * 0.35f,
            cx + dir * scaleW * 0.24f, cy + scaleH * 0.16f
        )
        cubicTo(
            cx + dir * scaleW * 0.08f, cy + scaleH * 0.24f,
            cx - dir * scaleW * 0.18f, cy + scaleH * 0.22f,
            cx - dir * scaleW * 0.34f, cy + scaleH * 0.06f
        )
        close()
    }
    drawPath(bellyShadePath, color = shadeFill.copy(alpha = 0.42f))

    // Crisp classic ink outline
    drawPath(
        bodyPath,
        color = strokeColor,
        style = Stroke(width = 2.0f, join = StrokeJoin.Round, cap = StrokeCap.Round)
    )

    // Layered Wing with feather strokes
    val wingPath = Path().apply {
        moveTo(cx + dir * scaleW * 0.02f, cy - scaleH * 0.08f)
        cubicTo(
            cx - dir * scaleW * 0.18f, cy - scaleH * 0.12f,
            cx - dir * scaleW * 0.28f, cy + scaleH * 0.06f,
            cx - dir * scaleW * 0.10f, cy + scaleH * 0.16f
        )
        cubicTo(
            cx + dir * scaleW * 0.02f, cy + scaleH * 0.12f,
            cx + dir * scaleW * 0.05f, cy + scaleH * 0.02f,
            cx + dir * scaleW * 0.02f, cy - scaleH * 0.08f
        )
        close()
    }
    drawPath(wingPath, color = shadeFill.copy(alpha = 0.28f))
    drawPath(wingPath, color = strokeColor.copy(alpha = 0.55f), style = Stroke(width = 1.4f, cap = StrokeCap.Round))
    // Inner wing feather lines
    drawLine(
        color = strokeColor.copy(alpha = 0.38f),
        start = Offset(cx - dir * scaleW * 0.04f, cy + scaleH * 0.01f),
        end = Offset(cx - dir * scaleW * 0.18f, cy + scaleH * 0.06f),
        strokeWidth = 1.1f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = strokeColor.copy(alpha = 0.38f),
        start = Offset(cx - dir * scaleW * 0.02f, cy + scaleH * 0.07f),
        end = Offset(cx - dir * scaleW * 0.15f, cy + scaleH * 0.11f),
        strokeWidth = 1.1f,
        cap = StrokeCap.Round
    )

    // Serrated Comb (mature broiler gets vibrant crimson comb & wattle)
    if (isMatureBroiler) {
        val combColor = Color(0xFFC81E1E)
        val combPath = Path().apply {
            moveTo(cx + dir * scaleW * 0.08f, cy - scaleH * 0.34f)
            lineTo(cx + dir * scaleW * 0.10f, cy - scaleH * 0.47f)
            lineTo(cx + dir * scaleW * 0.14f, cy - scaleH * 0.40f)
            lineTo(cx + dir * scaleW * 0.18f, cy - scaleH * 0.49f)
            lineTo(cx + dir * scaleW * 0.21f, cy - scaleH * 0.40f)
            lineTo(cx + dir * scaleW * 0.25f, cy - scaleH * 0.45f)
            lineTo(cx + dir * scaleW * 0.26f, cy - scaleH * 0.33f)
            close()
        }
        drawPath(combPath, color = combColor)
        drawPath(combPath, color = strokeColor.copy(alpha = 0.65f), style = Stroke(width = 1.1f, join = StrokeJoin.Round))

        // Wattle under chin
        drawOval(
            color = combColor,
            topLeft = Offset(
                cx + dir * scaleW * 0.20f - scaleW * 0.04f,
                cy - scaleH * 0.15f
            ),
            size = Size(scaleW * 0.08f, scaleH * 0.11f)
        )
    }

    // Two-part curved Beak
    val beakPath = Path().apply {
        moveTo(cx + dir * scaleW * 0.26f, cy - scaleH * 0.28f)
        lineTo(cx + dir * scaleW * 0.42f, cy - scaleH * 0.22f)
        lineTo(cx + dir * scaleW * 0.26f, cy - scaleH * 0.18f)
        close()
    }
    drawPath(beakPath, color = Color(0xFFD97706))
    drawPath(beakPath, color = strokeColor, style = Stroke(width = 1.2f, join = StrokeJoin.Round))
    drawLine(
        color = strokeColor,
        start = Offset(cx + dir * scaleW * 0.26f, cy - scaleH * 0.23f),
        end = Offset(cx + dir * scaleW * 0.38f, cy - scaleH * 0.22f),
        strokeWidth = 1.0f
    )

    // Expressive Eye with dark pupil and white sparkle
    val eyePos = Offset(cx + dir * scaleW * 0.19f, cy - scaleH * 0.29f)
    drawCircle(color = BookSepiaInk, radius = (scaleH * 0.045f).coerceIn(2.4f, 4.2f), center = eyePos)
    drawCircle(
        color = Color.White,
        radius = (scaleH * 0.016f).coerceIn(0.9f, 1.6f),
        center = Offset(eyePos.x + dir * 0.9f, eyePos.y - 0.9f)
    )

    // Articulated Legs & Three-Toed Feet
    val legColor = Color(0xFFD97706)
    val legTopY = cy + scaleH * 0.29f
    val legBottomY = cy + scaleH * 0.46f
    val leg1X = cx - dir * scaleW * 0.07f
    val leg2X = cx + dir * scaleW * 0.05f

    listOf(leg1X, leg2X).forEach { lx ->
        drawLine(legColor, Offset(lx, legTopY), Offset(lx - dir * 2f, legBottomY), strokeWidth = 3.0f, cap = StrokeCap.Round)
        drawLine(strokeColor, Offset(lx, legTopY), Offset(lx - dir * 2f, legBottomY), strokeWidth = 1.0f, cap = StrokeCap.Round)
        // Toes
        drawLine(legColor, Offset(lx - dir * 2f, legBottomY), Offset(lx + dir * 9f, legBottomY + 1f), strokeWidth = 2.2f, cap = StrokeCap.Round)
        drawLine(legColor, Offset(lx - dir * 2f, legBottomY), Offset(lx + dir * 5f, legBottomY + 3f), strokeWidth = 2.0f, cap = StrokeCap.Round)
        drawLine(legColor, Offset(lx - dir * 2f, legBottomY), Offset(lx - dir * 7f, legBottomY + 1f), strokeWidth = 2.0f, cap = StrokeCap.Round)
    }
}

/**
 * Suspended conical metal hopper + red scalloped feed pan filled with crumble/pellets.
 */
private fun DrawScope.drawSuspendedPanFeeder(
    cx: Float,
    topY: Float,
    bottomY: Float,
    feederW: Float
) {
    val h = bottomY - topY
    // Suspension cable from top
    drawLine(
        color = Color(0xFF475569),
        start = Offset(cx, 0f),
        end = Offset(cx, topY + h * 0.08f),
        strokeWidth = 2.2f
    )
    // Hook ring
    drawCircle(
        color = Color(0xFF475569),
        radius = 3.5f,
        center = Offset(cx, topY + h * 0.08f),
        style = Stroke(width = 1.6f)
    )
    // Metal cone hopper body
    val cylinderPath = Path().apply {
        moveTo(cx - feederW * 0.22f, topY + h * 0.14f)
        lineTo(cx + feederW * 0.22f, topY + h * 0.14f)
        lineTo(cx + feederW * 0.28f, topY + h * 0.72f)
        lineTo(cx - feederW * 0.28f, topY + h * 0.72f)
        close()
    }
    drawPath(cylinderPath, color = Color(0xFFE2E8F0))
    // Right-side shading band on cylinder
    val cylShade = Path().apply {
        moveTo(cx + feederW * 0.06f, topY + h * 0.14f)
        lineTo(cx + feederW * 0.22f, topY + h * 0.14f)
        lineTo(cx + feederW * 0.28f, topY + h * 0.72f)
        lineTo(cx + feederW * 0.08f, topY + h * 0.72f)
        close()
    }
    drawPath(cylShade, color = Color(0xFF94A3B8).copy(alpha = 0.55f))
    drawPath(cylinderPath, color = Color(0xFF334155), style = Stroke(width = 1.6f, join = StrokeJoin.Round))

    // Golden feed visible inside pan rim
    drawOval(
        color = Color(0xFFD97706),
        topLeft = Offset(cx - feederW * 0.42f, topY + h * 0.68f),
        size = Size(feederW * 0.84f, h * 0.16f)
    )
    // Red circular pan base
    val panPath = Path().apply {
        moveTo(cx - feederW * 0.48f, topY + h * 0.74f)
        lineTo(cx + feederW * 0.48f, topY + h * 0.74f)
        lineTo(cx + feederW * 0.40f, bottomY)
        lineTo(cx - feederW * 0.40f, bottomY)
        close()
    }
    drawPath(panPath, color = Color(0xFFB91C1C))
    drawPath(panPath, color = BookBurgundyDark, style = Stroke(width = 1.6f, join = StrokeJoin.Round))
    // Support stone/block beneath pan
    drawRoundRect(
        color = Color(0xFF94A3B8),
        topLeft = Offset(cx - feederW * 0.30f, bottomY),
        size = Size(feederW * 0.60f, h * 0.10f),
        cornerRadius = CornerRadius(2f, 2f)
    )
}

/**
 * Rich textured grain/pellet mound (Starter fine crumble, Grower mini-pellets, Finisher pellets).
 */
private fun DrawScope.drawFeedPileIllustration(
    cx: Float,
    baseY: Float,
    pileW: Float,
    pileH: Float,
    granuleSizeScale: Float
) {
    // Ground scatter shadow
    drawOval(
        color = BookSepiaInk.copy(alpha = 0.18f),
        topLeft = Offset(cx - pileW * 0.52f, baseY - pileH * 0.12f),
        size = Size(pileW * 1.04f, pileH * 0.24f)
    )
    // Main shaded cone mound
    val moundPath = Path().apply {
        moveTo(cx - pileW * 0.48f, baseY)
        cubicTo(
            cx - pileW * 0.22f, baseY - pileH * 0.92f,
            cx + pileW * 0.22f, baseY - pileH * 0.92f,
            cx + pileW * 0.48f, baseY
        )
        close()
    }
    drawPath(moundPath, color = Color(0xFFD4A359))
    val rightMoundShade = Path().apply {
        moveTo(cx + pileW * 0.02f, baseY - pileH * 0.70f)
        cubicTo(
            cx + pileW * 0.24f, baseY - pileH * 0.68f,
            cx + pileW * 0.40f, baseY - pileH * 0.22f,
            cx + pileW * 0.48f, baseY
        )
        lineTo(cx + pileW * 0.04f, baseY)
        close()
    }
    drawPath(rightMoundShade, color = Color(0xFFB07D38).copy(alpha = 0.55f))
    drawPath(moundPath, color = BookSepiaInk, style = Stroke(width = 1.5f, join = StrokeJoin.Round))

    // Textured granules/pellets over the mound
    val cols = if (granuleSizeScale < 1f) 11 else if (granuleSizeScale < 1.5f) 8 else 6
    val rows = if (granuleSizeScale < 1f) 6 else 5
    for (r in 0 until rows) {
        val ratioY = r.toFloat() / rows.toFloat()
        val rowY = baseY - pileH * (0.12f + ratioY * 0.58f)
        val halfSpan = pileW * (0.40f * (1f - ratioY * 0.75f))
        val itemsInRow = (cols * (1f - ratioY * 0.55f)).toInt().coerceAtLeast(2)
        for (c in 0 until itemsInRow) {
            val t = if (itemsInRow == 1) 0.5f else c.toFloat() / (itemsInRow - 1).toFloat()
            val gx = (cx - halfSpan) + t * (halfSpan * 2f) + ((r % 2) * 2.5f - 1.2f)
            val pelletColor = when {
                (r + c) % 3 == 0 -> Color(0xFFF3D08A)
                (r + c) % 3 == 1 -> Color(0xFFC2883A)
                else -> Color(0xFF9C6625)
            }
            if (granuleSizeScale <= 0.9f) {
                // Fine crumble dots
                drawCircle(color = pelletColor, radius = 2.1f, center = Offset(gx, rowY))
                drawCircle(color = BookSepiaInk.copy(alpha = 0.4f), radius = 2.1f, center = Offset(gx, rowY), style = Stroke(0.6f))
            } else {
                // Angled cylindrical grower/finisher pellets
                val pw = 5.5f * granuleSizeScale
                val ph = 2.8f * granuleSizeScale
                val angle = ((c * 37 + r * 23) % 70 - 35).toFloat()
                rotate(degrees = angle, pivot = Offset(gx, rowY)) {
                    drawRoundRect(
                        color = pelletColor,
                        topLeft = Offset(gx - pw / 2f, rowY - ph / 2f),
                        size = Size(pw, ph),
                        cornerRadius = CornerRadius(ph / 2f, ph / 2f)
                    )
                    drawRoundRect(
                        color = BookSepiaInk.copy(alpha = 0.65f),
                        topLeft = Offset(gx - pw / 2f, rowY - ph / 2f),
                        size = Size(pw, ph),
                        cornerRadius = CornerRadius(ph / 2f, ph / 2f),
                        style = Stroke(width = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun SpiralBindingRingsCanvas(
    ringCount: Int = 8,
    onRightEdge: Boolean = false,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .width(22.dp)
            .testTag("vintage_spiral_binding_rings")
    ) {
        val w = size.width
        val h = size.height
        val count = ringCount.coerceAtLeast(7)
        val spacing = h / (count + 1)
        for (i in 1..count) {
            val cy = spacing * i
            val holeX = if (onRightEdge) w * 0.24f else w * 0.76f
            val holeCenter = Offset(holeX, cy)
            drawCircle(color = Color(0xFF2B1810), radius = 5.2f, center = holeCenter)
            drawCircle(color = BookBorderOuter, radius = 6.2f, center = holeCenter, style = Stroke(width = 1.4f))

            val wireStartX = if (onRightEdge) holeX else 1f
            val wireWidth = w * 0.78f
            drawRoundRect(
                color = Color(0xFF334155),
                topLeft = Offset(wireStartX, cy - 6.2f),
                size = Size(wireWidth, 5.2f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = Color(0xFF94A3B8),
                topLeft = Offset(wireStartX + 1f, cy - 5.6f),
                size = Size(wireWidth - 2f, 3.6f),
                cornerRadius = CornerRadius(3f, 3f)
            )
            drawLine(
                color = Color(0xFFF1F5F9),
                start = Offset(wireStartX + 3f, cy - 4.2f),
                end = Offset(wireStartX + wireWidth - 4f, cy - 4.2f),
                strokeWidth = 1.5f,
                cap = StrokeCap.Round
            )
            drawRoundRect(
                color = Color(0xFF475569),
                topLeft = Offset(wireStartX, cy + 1f),
                size = Size(wireWidth, 4.6f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawLine(
                color = Color(0xFFCBD5E1),
                start = Offset(wireStartX + 3f, cy + 2.6f),
                end = Offset(wireStartX + wireWidth - 4f, cy + 2.6f),
                strokeWidth = 1.3f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun TopSpiralBindingRingsCanvas(
    ringCount: Int = 12,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
            .testTag("vintage_top_spiral_binding_rings")
    ) {
        val w = size.width
        val h = size.height
        val count = ringCount.coerceAtLeast(8)
        val spacing = w / (count + 1)
        for (i in 1..count) {
            val cx = spacing * i
            val holeCenter = Offset(cx, h * 0.76f)
            drawCircle(color = Color(0xFF2B1810), radius = 4.8f, center = holeCenter)
            drawCircle(color = BookBorderOuter, radius = 5.6f, center = holeCenter, style = Stroke(width = 1.2f))
            drawRoundRect(
                color = Color(0xFF334155),
                topLeft = Offset(cx - 5f, 1f),
                size = Size(4.6f, h * 0.76f),
                cornerRadius = CornerRadius(3f, 3f)
            )
            drawRoundRect(
                color = Color(0xFF94A3B8),
                topLeft = Offset(cx - 4.5f, 1.5f),
                size = Size(3.2f, h * 0.73f),
                cornerRadius = CornerRadius(2.5f, 2.5f)
            )
            drawLine(
                color = Color(0xFFF1F5F9),
                start = Offset(cx - 3.3f, 3f),
                end = Offset(cx - 3.3f, h * 0.65f),
                strokeWidth = 1.3f,
                cap = StrokeCap.Round
            )
            drawRoundRect(
                color = Color(0xFF475569),
                topLeft = Offset(cx + 1f, 1f),
                size = Size(4.2f, h * 0.74f),
                cornerRadius = CornerRadius(3f, 3f)
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
        drawCircle(color = BookBurgundy, radius = w * 0.48f, center = Offset(w / 2f, h / 2f), style = Stroke(width = 2.2f))
        val bodyPath = Path().apply {
            moveTo(w * 0.30f, h * 0.76f)
            cubicTo(w * 0.28f, h * 0.48f, w * 0.38f, h * 0.30f, w * 0.56f, h * 0.30f)
            cubicTo(w * 0.68f, h * 0.32f, w * 0.72f, h * 0.56f, w * 0.64f, h * 0.76f)
            close()
        }
        drawPath(bodyPath, color = BookBurgundy)
        drawCircle(color = Color(0xFFB91C1C), radius = w * 0.08f, center = Offset(w * 0.46f, h * 0.23f))
        drawCircle(color = Color(0xFFB91C1C), radius = w * 0.085f, center = Offset(w * 0.55f, h * 0.22f))
        drawCircle(color = Color(0xFFB91C1C), radius = w * 0.07f, center = Offset(w * 0.62f, h * 0.26f))
        val beak = Path().apply {
            moveTo(w * 0.58f, h * 0.34f)
            lineTo(w * 0.78f, h * 0.39f)
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
                lineTo(w * 0.08f, h * 0.18f)
                lineTo(w * 0.08f, h * 0.88f)
                lineTo(0f, h * 0.88f)
                lineTo(w * 0.038f, h * 0.53f)
                close()
            }
            drawPath(leftTail, color = BookBorderOuter)
            val rightTail = Path().apply {
                moveTo(w, h * 0.18f)
                lineTo(w * 0.92f, h * 0.18f)
                lineTo(w * 0.92f, h * 0.88f)
                lineTo(w, h * 0.88f)
                lineTo(w * 0.962f, h * 0.53f)
                close()
            }
            drawPath(rightTail, color = BookBorderOuter)
        }
        Surface(
            modifier = Modifier.padding(horizontal = 8.dp),
            shape = RoundedCornerShape(6.dp),
            color = BookParchmentDeep,
            border = BorderStroke(1.2.dp, BookBorderOuter)
        ) {
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BookBurgundyDark,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun VintageScrollIndexBanner(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = BookParchmentMid,
        border = BorderStroke(1.8.dp, BookBorderOuter),
        shadowElevation = 3.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BookBurgundyDark,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 21.sp
            )
        }
    }
}

/**
 * Framed 68.dp circular illustration for each lesson in the Step 2 Book Index screen.
 */
@Composable
fun BookIndexLessonCircleArt(
    lessonId: String,
    visualType: InfographicVisualType,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(68.dp),
        shape = CircleShape,
        color = Color(0xFFFFFDF7),
        border = BorderStroke(1.8.dp, BookBurgundy)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            // Soft parchment disk backdrop
            drawCircle(color = Color(0xFFF6E9CD), radius = w * 0.48f, center = Offset(cx, cy))

            when (visualType) {
                InfographicVisualType.FEED_STARTER_SPEC -> {
                    drawFeedPileIllustration(cx, h * 0.78f, w * 0.80f, h * 0.56f, granuleSizeScale = 0.85f)
                }
                InfographicVisualType.FEED_GROWER_HEIGHT -> {
                    drawRect(Color(0xFFE2CCA4), Offset(0f, h * 0.78f), Size(w, h * 0.22f))
                    drawSuspendedPanFeeder(w * 0.62f, h * 0.08f, h * 0.76f, w * 0.44f)
                    drawClassicBirdIllustration(
                        cx = w * 0.28f,
                        cy = h * 0.58f,
                        scaleW = w * 0.44f,
                        scaleH = h * 0.42f,
                        bodyFill = Color(0xFFFDE68A),
                        shadeFill = Color(0xFFD97706),
                        strokeColor = BookSepiaInk,
                        isMatureBroiler = false
                    )
                }
                InfographicVisualType.FEED_FINISHER_PELLETS -> {
                    drawFeedPileIllustration(cx, h * 0.80f, w * 0.82f, h * 0.42f, granuleSizeScale = 1.25f)
                    // Two hands scooping/mixing grains above pile
                    drawOval(Color(0xFFE0AC69), Offset(w * 0.12f, h * 0.24f), Size(w * 0.30f, h * 0.18f))
                    drawOval(BookSepiaInk, Offset(w * 0.12f, h * 0.24f), Size(w * 0.30f, h * 0.18f), style = Stroke(1.3f))
                    drawOval(Color(0xFFE0AC69), Offset(w * 0.56f, h * 0.22f), Size(w * 0.30f, h * 0.18f))
                    drawOval(BookSepiaInk, Offset(w * 0.56f, h * 0.22f), Size(w * 0.30f, h * 0.18f), style = Stroke(1.3f))
                }
                InfographicVisualType.FEED_WEIGHT_WATER_CURVE -> {
                    // Mini weight table & anatomical chick matching Reference Image 1
                    drawRoundRect(Color.White, Offset(w * 0.08f, h * 0.26f), Size(w * 0.26f, h * 0.46f), CornerRadius(3f, 3f))
                    drawRoundRect(BookSepiaInk, Offset(w * 0.08f, h * 0.26f), Size(w * 0.26f, h * 0.46f), CornerRadius(3f, 3f), style = Stroke(1.2f))
                    drawClassicBirdIllustration(
                        cx = w * 0.62f,
                        cy = h * 0.54f,
                        scaleW = w * 0.58f,
                        scaleH = h * 0.62f,
                        bodyFill = Color(0xFFFDE68A),
                        shadeFill = Color(0xFFD97706),
                        strokeColor = BookSepiaInk,
                        isMatureBroiler = false,
                        facingRight = false
                    )
                    drawCircle(Color(0xFFE11D48).copy(alpha = 0.78f), radius = w * 0.08f, center = Offset(w * 0.60f, h * 0.54f))
                }
                InfographicVisualType.CARE_PREHEAT_PREP -> {
                    val cone = Path().apply {
                        moveTo(cx - w * 0.16f, h * 0.30f)
                        lineTo(cx + w * 0.16f, h * 0.30f)
                        lineTo(cx + w * 0.38f, h * 0.76f)
                        lineTo(cx - w * 0.38f, h * 0.76f)
                        close()
                    }
                    drawPath(cone, color = Color(0xFFF59E0B).copy(alpha = 0.28f))
                    drawRoundRect(BookBurgundy, Offset(cx - w * 0.20f, h * 0.16f), Size(w * 0.40f, h * 0.14f), CornerRadius(4f, 4f))
                    drawClassicBirdIllustration(cx, h * 0.64f, w * 0.44f, h * 0.38f, Color(0xFFFDE68A), Color(0xFFD97706), BookSepiaInk)
                }
                InfographicVisualType.CARE_BROODING_BEHAVIOR -> {
                    drawClassicBirdIllustration(w * 0.36f, h * 0.58f, w * 0.42f, h * 0.38f, Color(0xFFFDE68A), Color(0xFFD97706), BookSepiaInk)
                    drawClassicBirdIllustration(w * 0.68f, h * 0.60f, w * 0.36f, h * 0.34f, Color(0xFFFDE68A), Color(0xFFD97706), BookSepiaInk, facingRight = false)
                }
                InfographicVisualType.CARE_VENTILATION_AIRFLOW -> {
                    val roof = Path().apply {
                        moveTo(w * 0.10f, h * 0.34f)
                        lineTo(cx, h * 0.14f)
                        lineTo(w * 0.90f, h * 0.34f)
                    }
                    drawPath(roof, BookBorderOuter, style = Stroke(2.4f, cap = StrokeCap.Round))
                    val arc = Path().apply {
                        moveTo(w * 0.14f, h * 0.42f)
                        cubicTo(w * 0.38f, h * 0.22f, w * 0.62f, h * 0.24f, w * 0.82f, h * 0.48f)
                    }
                    drawPath(arc, Color(0xFF2563EB), style = Stroke(2.4f, cap = StrokeCap.Round))
                    drawCircle(Color(0xFF334155), radius = w * 0.10f, center = Offset(w * 0.80f, h * 0.62f))
                }
                InfographicVisualType.CARE_LIGHTING_PRESALE -> {
                    drawCircle(Color(0xFF1E293B), radius = w * 0.24f, center = Offset(w * 0.38f, h * 0.44f))
                    drawCircle(Color(0xFFF6E9CD), radius = w * 0.18f, center = Offset(w * 0.46f, h * 0.40f))
                    drawRoundRect(BookBurgundy, Offset(w * 0.46f, h * 0.58f), Size(w * 0.36f, h * 0.20f), CornerRadius(4f, 4f))
                }
                InfographicVisualType.VAX_RECEPTION_WATER,
                InfographicVisualType.VAX_GUMBORO_DRINKING,
                InfographicVisualType.VAX_BOOSTER_DETOX -> {
                    drawRoundRect(Color(0xFFDBEAFE), Offset(w * 0.26f, h * 0.26f), Size(w * 0.24f, h * 0.48f), CornerRadius(5f, 5f))
                    drawRoundRect(Color(0xFF1D4ED8), Offset(w * 0.26f, h * 0.26f), Size(w * 0.24f, h * 0.48f), CornerRadius(5f, 5f), style = Stroke(1.6f))
                    drawRoundRect(Color(0xFF0F766E), Offset(w * 0.54f, h * 0.36f), Size(w * 0.22f, h * 0.38f), CornerRadius(4f, 4f))
                    drawRect(BookGoldAccent, Offset(w * 0.58f, h * 0.26f), Size(w * 0.14f, h * 0.10f))
                }
                InfographicVisualType.VAX_NEWCASTLE_EYE_SPRAY -> {
                    val drop = Path().apply {
                        moveTo(cx, h * 0.18f)
                        cubicTo(w * 0.72f, h * 0.48f, w * 0.70f, h * 0.76f, cx, h * 0.78f)
                        cubicTo(w * 0.30f, h * 0.76f, w * 0.28f, h * 0.48f, cx, h * 0.18f)
                        close()
                    }
                    drawPath(drop, color = Color(0xFF0284C7))
                    drawPath(drop, color = BookSepiaInk, style = Stroke(1.4f))
                }
                else -> {
                    // Diseases: classic broiler + anatomical lesion highlight
                    drawClassicBirdIllustration(
                        cx = cx,
                        cy = cy + h * 0.04f,
                        scaleW = w * 0.74f,
                        scaleH = h * 0.72f,
                        bodyFill = Color(0xFFFFF8E7),
                        shadeFill = Color(0xFFE2CCA4),
                        strokeColor = BookSepiaInk,
                        isMatureBroiler = true
                    )
                    drawCircle(Color(0xFFDC2626).copy(alpha = 0.72f), radius = w * 0.11f, center = Offset(cx - w * 0.02f, cy + h * 0.04f))
                }
            }
        }
    }
}

private data class FarmerGaugeChip(
    val icon: String,
    val metricValue: String
)

private fun farmerHeroGaugesFor(
    spec: EncyclopediaPosterSpec,
    lang: AppLanguage
): List<FarmerGaugeChip> {
    val cmLabel = if (lang == AppLanguage.AR) "سم" else "cm"
    val hrLabel = if (lang == AppLanguage.AR) "ساعة" else "h"
    val kgLabel = Translations.tr("unit_kg", lang)
    return when (spec.visualType) {
        InfographicVisualType.CARE_PREHEAT_PREP -> listOf(
            FarmerGaugeChip("🌡️", "33.5°C"),
            FarmerGaugeChip("💧", "50–60%"),
            FarmerGaugeChip("📏", "5–8 $cmLabel")
        )
        InfographicVisualType.CARE_BROODING_BEHAVIOR -> listOf(
            FarmerGaugeChip("🌡️", "33°C ➔ 30°C"),
            FarmerGaugeChip("🐣", "> 95%"),
            FarmerGaugeChip("⚖️", "190g / 7d")
        )
        InfographicVisualType.CARE_VENTILATION_AIRFLOW -> listOf(
            FarmerGaugeChip("🌬️", "NH₃ < 10 ppm"),
            FarmerGaugeChip("💨", "0.5–1.5 m/s"),
            FarmerGaugeChip("💧", "50–65%")
        )
        InfographicVisualType.CARE_LIGHTING_PRESALE -> listOf(
            FarmerGaugeChip("🌙", "4–6 $hrLabel"),
            FarmerGaugeChip("📐", "12 / m²"),
            FarmerGaugeChip("⏱️", "8–10 $hrLabel")
        )
        InfographicVisualType.VAX_RECEPTION_WATER -> listOf(
            FarmerGaugeChip("💧", "18°C–21°C"),
            FarmerGaugeChip("⏱️", "6 $hrLabel"),
            FarmerGaugeChip("⚡", "AD3E + C")
        )
        InfographicVisualType.VAX_NEWCASTLE_EYE_SPRAY -> listOf(
            FarmerGaugeChip("❄️", "2°C–8°C"),
            FarmerGaugeChip("👁️", "95%+"),
            FarmerGaugeChip("⏱️", "20 min")
        )
        InfographicVisualType.VAX_GUMBORO_DRINKING -> listOf(
            FarmerGaugeChip("❄️", "2°C–8°C"),
            FarmerGaugeChip("💧", "1–2 $hrLabel"),
            FarmerGaugeChip("🛡️", "IBD D12–14")
        )
        InfographicVisualType.VAX_BOOSTER_DETOX -> listOf(
            FarmerGaugeChip("🛡️", "LaSota"),
            FarmerGaugeChip("💧", "AD3E + C"),
            FarmerGaugeChip("⏱️", "2 $hrLabel")
        )
        InfographicVisualType.FEED_STARTER_SPEC -> listOf(
            FarmerGaugeChip("🌾", "23% ➔ 22% ➔ 20.5%"),
            FarmerGaugeChip("⚖️", "2.8 $kgLabel"),
            FarmerGaugeChip("📈", "FCR 1.50")
        )
        InfographicVisualType.FEED_GROWER_HEIGHT -> listOf(
            FarmerGaugeChip("📏", "100%"),
            FarmerGaugeChip("💧", "18°C–21°C"),
            FarmerGaugeChip("🌾", "22%")
        )
        InfographicVisualType.FEED_FINISHER_PELLETS -> listOf(
            FarmerGaugeChip("1️⃣", "75 / 25"),
            FarmerGaugeChip("2️⃣", "50 / 50"),
            FarmerGaugeChip("3️⃣", "25 / 75")
        )
        InfographicVisualType.FEED_WEIGHT_WATER_CURVE -> listOf(
            FarmerGaugeChip("💧", "1.8x"),
            FarmerGaugeChip("⚖️", "2.8 $kgLabel"),
            FarmerGaugeChip("📈", "FCR 1.50")
        )
        else -> {
            val fact1 = spec.quickFacts.getOrNull(0)?.second?.take(18) ?: "1–4 w"
            val fact2 = spec.quickFacts.getOrNull(3)?.second?.take(18) ?: "< 3.5%"
            listOf(
                FarmerGaugeChip("🩺", spec.pathogenCode.ifBlank { "Ross 308" }),
                FarmerGaugeChip("📅", fact1),
                FarmerGaugeChip("🌬️", fact2)
            )
        }
    }
}

private data class StageVisualDisplay(
    val iconEmoji: String,
    val prominentBadge: String
)

private fun stageVisualDisplayFor(
    visualType: InfographicVisualType,
    stepIndex: Int,
    lang: AppLanguage
): StageVisualDisplay {
    val cmLabel = if (lang == AppLanguage.AR) "سم" else "cm"
    val hrLabel = if (lang == AppLanguage.AR) "ساعة" else "h"
    val kgLabel = Translations.tr("unit_kg", lang)

    return when (visualType) {
        InfographicVisualType.FEED_STARTER_SPEC -> when (stepIndex) {
            0 -> StageVisualDisplay("🌾", "23%")
            1 -> StageVisualDisplay("🌾", "22%")
            else -> StageVisualDisplay("🌾", "20.5%")
        }
        InfographicVisualType.FEED_GROWER_HEIGHT -> when (stepIndex) {
            0 -> StageVisualDisplay("🐣", "D1–7")
            1 -> StageVisualDisplay("📏", "100%")
            else -> StageVisualDisplay("💧", "1.8x")
        }
        InfographicVisualType.FEED_FINISHER_PELLETS -> when (stepIndex) {
            0 -> StageVisualDisplay("🥣", "75 / 25")
            1 -> StageVisualDisplay("🥣", "50 / 50")
            else -> StageVisualDisplay("🥣", "25 / 75")
        }
        InfographicVisualType.FEED_WEIGHT_WATER_CURVE -> when (stepIndex) {
            0 -> StageVisualDisplay("💧", "1.8x")
            1 -> StageVisualDisplay("🌾", "FCR 1.5")
            else -> StageVisualDisplay("⚖️", "2.8 $kgLabel")
        }
        InfographicVisualType.CARE_PREHEAT_PREP -> when (stepIndex) {
            0 -> StageVisualDisplay("🧼", "24 $hrLabel")
            1 -> StageVisualDisplay("📏", "5–8 $cmLabel")
            else -> StageVisualDisplay("🌡️", "33.5°C")
        }
        InfographicVisualType.CARE_BROODING_BEHAVIOR -> when (stepIndex) {
            0 -> StageVisualDisplay("🥶", "< 30°C")
            1 -> StageVisualDisplay("☀️", "33.5°C")
            else -> StageVisualDisplay("🥵", "> 36°C")
        }
        InfographicVisualType.CARE_VENTILATION_AIRFLOW -> when (stepIndex) {
            0 -> StageVisualDisplay("🪟", "45°")
            1 -> StageVisualDisplay("🌀", "Timer")
            else -> StageVisualDisplay("🌬️", "<10 ppm")
        }
        InfographicVisualType.CARE_LIGHTING_PRESALE -> when (stepIndex) {
            0 -> StageVisualDisplay("🌙", "4–6 $hrLabel")
            1 -> StageVisualDisplay("📐", "12 / m²")
            else -> StageVisualDisplay("⏱️", "8–10 $hrLabel")
        }
        InfographicVisualType.VAX_RECEPTION_WATER -> when (stepIndex) {
            0 -> StageVisualDisplay("🧼", "0 ppm")
            1 -> StageVisualDisplay("💧", "18–21°C")
            else -> StageVisualDisplay("⚡", "6 $hrLabel")
        }
        InfographicVisualType.VAX_NEWCASTLE_EYE_SPRAY -> when (stepIndex) {
            0 -> StageVisualDisplay("❄️", "2°C–8°C")
            1 -> StageVisualDisplay("👁️", "1 Drop")
            else -> StageVisualDisplay("🛡️", "24 $hrLabel")
        }
        InfographicVisualType.VAX_GUMBORO_DRINKING -> when (stepIndex) {
            0 -> StageVisualDisplay("⏱️", "90 min")
            1 -> StageVisualDisplay("🛡️", "D12–14")
            else -> StageVisualDisplay("💧", "2 $hrLabel")
        }
        InfographicVisualType.VAX_BOOSTER_DETOX -> when (stepIndex) {
            0 -> StageVisualDisplay("❄️", "2°C–8°C")
            1 -> StageVisualDisplay("💉", "D18–21")
            else -> StageVisualDisplay("🛡️", "AD3E")
        }
        else -> when (stepIndex) {
            0 -> StageVisualDisplay("🩺", "D1")
            1 -> StageVisualDisplay("📉", "FCR")
            else -> StageVisualDisplay("💊", "AD3E + C")
        }
    }
}

/**
 * Section 3 of Lesson Page:
 * High-resolution Nano Banana (`gemini-2.5-flash-image`) hero artwork with permanent disk cache
 * in `filesDir/nano_banana_book_cache`, backed immediately by bundled watercolor encyclopedia art
 * and a clear 3-metric Farmer Gauge Strip (🌡️ / 💧 / 🌬️ / 🌾 / ⚖️) with zero baked-in image text.
 */
@Composable
fun NanoBananaLessonHeroPanel(
    lessonId: String,
    lessonTitle: String,
    spec: EncyclopediaPosterSpec,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val heroImageKey = "hero_$lessonId"
    var generatedBitmap by remember(heroImageKey) {
        mutableStateOf<Bitmap?>(BookNanoBananaImageEngine.loadCachedBitmap(context, heroImageKey))
    }

    LaunchedEffect(heroImageKey, spec.visualType) {
        if (generatedBitmap == null) {
            val fetched = BookNanoBananaImageEngine.getOrFetchLessonImage(
                context = context,
                imageKey = heroImageKey,
                prompt = BookNanoBananaImageEngine.buildPromptForHero(lessonId, spec.visualType)
            )
            if (fetched != null) {
                generatedBitmap = fetched
            }
        }
    }

    val gauges = remember(spec.visualType, spec.quickFacts, lang) {
        farmerHeroGaugesFor(spec, lang)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("infographic_canvas_${spec.visualType.name.lowercase()}"),
        shape = RoundedCornerShape(12.dp),
        color = BookParchmentCard,
        border = BorderStroke(1.3.dp, BookBorderInner),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(172.dp)
                    .background(BookParchmentMid)
            ) {
                val bmp = generatedBitmap
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = lessonTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(
                            id = BookNanoBananaImageEngine.bundledFallbackDrawableFor(spec.visualType)
                        ),
                        contentDescription = lessonTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Crisp, ultra-readable farmer telemetry & standards strip below illustration
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BookParchmentMid)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                gauges.forEach { gauge ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = BookParchmentCard,
                        border = BorderStroke(1.dp, BookBorderInner)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = gauge.icon, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = gauge.metricValue,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BookBurgundyDark,
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

/**
 * Farmer-clear illustration box inside each of the 3 stage cards (1 | 2 | 3).
 * Loads cached/fetched Nano Banana step image when available, otherwise shows unmistakable
 * agricultural icon + large metric badge (no vague brown drawRect shapes).
 */
@Composable
fun NanoBananaStageArtBox(
    lessonId: String,
    visualType: InfographicVisualType,
    stepIndex: Int,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val stepKey = "step_${lessonId}_$stepIndex"
    var stepBitmap by remember(stepKey) {
        mutableStateOf<Bitmap?>(BookNanoBananaImageEngine.loadCachedBitmap(context, stepKey))
    }

    LaunchedEffect(stepKey, visualType) {
        if (stepBitmap == null) {
            val fetched = BookNanoBananaImageEngine.getOrFetchLessonImage(
                context = context,
                imageKey = stepKey,
                prompt = BookNanoBananaImageEngine.buildPromptForStep(lessonId, visualType, stepIndex)
            )
            if (fetched != null) {
                stepBitmap = fetched
            }
        }
    }

    val stageVisual = remember(visualType, stepIndex, lang) {
        stageVisualDisplayFor(visualType, stepIndex, lang)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(78.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(BookParchmentMid),
        contentAlignment = Alignment.Center
    ) {
        val bmp = stepBitmap
        if (bmp != null) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = stageVisual.prominentBadge,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stageVisual.iconEmoji,
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = BookParchmentCard,
                    border = BorderStroke(1.dp, BookBurgundy)
                ) {
                    Text(
                        text = stageVisual.prominentBadge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BookBurgundyDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Clean 50% / 50% Farmer Comparison Card ("✅ علامات النجاح — افعل هذا" vs "❌ تجنّب هذا الخطأ — إياك وهذا")
 * replacing the ambiguous internal chick anatomy diagram.
 */
@Composable
fun SuccessMistakeFarmerCard(
    isSuccess: Boolean,
    headerTitle: String,
    bodyText: String,
    textLayoutDirection: LayoutDirection,
    modifier: Modifier = Modifier
) {
    val bannerColor = if (isSuccess) Color(0xFF15803D) else Color(0xFFB91C1C)
    val softTintBg = if (isSuccess) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
    val cardBorder = if (isSuccess) Color(0xFF15803D) else Color(0xFFB91C1C)
    val badgeSymbol = if (isSuccess) "✓" else "✕"

    CompositionLocalProvider(LocalLayoutDirection provides textLayoutDirection) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .testTag(if (isSuccess) "book_card_success_farmer" else "book_card_mistake_farmer"),
            shape = RoundedCornerShape(12.dp),
            color = softTintBg,
            border = BorderStroke(1.5.dp, cardBorder),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bannerColor)
                        .padding(horizontal = 8.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = headerTitle,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        lineHeight = 13.5.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = bannerColor,
                        border = BorderStroke(2.dp, Color.White),
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = badgeSymbol,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    Text(
                        text = bodyText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BookBurgundyDark,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.5.sp,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Bottom Sepia Farm Engraving Footer (barn, silos, trees, flock of hens) + plain text swipe line.
 */
@Composable
fun SepiaFarmEngravingFooterCanvas(
    pageNumber: Int,
    totalPages: Int,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val pageLabel = Translations.tr("book_swipe_page_footer_fmt", lang)
        .replace("{page}", pageNumber.toString())
        .replace("{total}", totalPages.toString())

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vintage_sepia_farm_footer"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
        ) {
            val w = size.width
            val h = size.height
            val groundY = h * 0.84f

            // Ground horizon line
            drawLine(
                color = BookSepiaInk,
                start = Offset(0f, groundY),
                end = Offset(w, groundY),
                strokeWidth = 1.6f
            )

            // Left farm poultry barn with gabled roof
            val barnPath = Path().apply {
                moveTo(w * 0.08f, groundY)
                lineTo(w * 0.08f, h * 0.46f)
                lineTo(w * 0.22f, h * 0.16f)
                lineTo(w * 0.36f, h * 0.46f)
                lineTo(w * 0.36f, groundY)
                close()
            }
            drawPath(barnPath, color = BookSepiaInk.copy(alpha = 0.16f))
            drawPath(barnPath, color = BookSepiaInk, style = Stroke(width = 1.5f, join = StrokeJoin.Round))

            // Twin feed silos beside barn
            listOf(0.42f, 0.465f).forEach { sx ->
                drawRoundRect(
                    color = BookSepiaInk.copy(alpha = 0.20f),
                    topLeft = Offset(w * sx, h * 0.24f),
                    size = Size(w * 0.038f, groundY - h * 0.24f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                drawRoundRect(
                    color = BookSepiaInk,
                    topLeft = Offset(w * sx, h * 0.24f),
                    size = Size(w * 0.038f, groundY - h * 0.24f),
                    cornerRadius = CornerRadius(4f, 4f),
                    style = Stroke(1.2f)
                )
            }

            // Shady trees on right
            listOf(0.76f, 0.85f).forEach { tx ->
                drawLine(BookSepiaInk, Offset(w * tx, h * 0.56f), Offset(w * tx, groundY), strokeWidth = 2.0f)
                drawCircle(BookSepiaInk.copy(alpha = 0.22f), radius = 10f, center = Offset(w * tx, h * 0.46f))
                drawCircle(BookSepiaInk, radius = 10f, center = Offset(w * tx, h * 0.46f), style = Stroke(1.2f))
            }

            // Small grazing hens silhouette flock on lower right
            listOf(0.56f, 0.62f, 0.68f, 0.74f).forEach { hx ->
                drawOval(
                    color = BookSepiaInk.copy(alpha = 0.65f),
                    topLeft = Offset(w * hx, h * 0.68f),
                    size = Size(11f, 7f)
                )
                drawCircle(
                    color = BookSepiaInk.copy(alpha = 0.70f),
                    radius = 2.4f,
                    center = Offset(w * hx + 11f, h * 0.67f)
                )
            }
        }

        // Plain simple text line directly on parchment (NO button box background)
        Text(
            text = pageLabel,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BookBurgundyDark,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .testTag("vintage_book_page_number_badge")
        )
    }
}

@Composable
fun VintageBookShelfCoverEmblem(
    branchIndex: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(52.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        drawCircle(
            color = accentColor.copy(alpha = 0.14f),
            radius = w * 0.46f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = BookGoldAccent.copy(alpha = 0.65f),
            radius = w * 0.44f,
            center = Offset(cx, cy),
            style = Stroke(width = 2.2f)
        )
        when (branchIndex) {
            0 -> {
                val canopy = Path().apply {
                    moveTo(cx - w * 0.26f, h * 0.34f)
                    lineTo(cx + w * 0.26f, h * 0.34f)
                    lineTo(cx + w * 0.12f, h * 0.18f)
                    lineTo(cx - w * 0.12f, h * 0.18f)
                    close()
                }
                drawPath(canopy, color = BookBurgundy)
                drawCircle(color = Color(0xFFF59E0B).copy(alpha = 0.35f), radius = w * 0.22f, center = Offset(cx, h * 0.56f))
                drawClassicBirdIllustration(cx, h * 0.62f, w * 0.38f, h * 0.38f, Color(0xFFFDE047), Color(0xFFD97706), BookSepiaInk, showSoftGroundShadow = false)
            }
            1 -> {
                val shield = Path().apply {
                    moveTo(cx, h * 0.18f)
                    lineTo(cx + w * 0.26f, h * 0.28f)
                    lineTo(cx + w * 0.20f, h * 0.60f)
                    lineTo(cx, h * 0.80f)
                    lineTo(cx - w * 0.20f, h * 0.60f)
                    lineTo(cx - w * 0.26f, h * 0.28f)
                    close()
                }
                drawPath(shield, color = Color(0xFF0F766E).copy(alpha = 0.25f))
                drawPath(shield, color = Color(0xFF0F766E), style = Stroke(width = 2.5f))
                drawLine(Color(0xFF0F766E), Offset(cx, h * 0.34f), Offset(cx, h * 0.62f), strokeWidth = 3.5f)
                drawLine(Color(0xFF0F766E), Offset(cx - w * 0.12f, h * 0.48f), Offset(cx + w * 0.12f, h * 0.48f), strokeWidth = 3.5f)
            }
            2 -> {
                drawLine(BookBurgundy, Offset(cx, h * 0.20f), Offset(cx, h * 0.45f), strokeWidth = 3.5f)
                drawLine(BookBurgundy, Offset(cx, h * 0.45f), Offset(cx - w * 0.14f, h * 0.56f), strokeWidth = 3f)
                drawLine(BookBurgundy, Offset(cx, h * 0.45f), Offset(cx + w * 0.14f, h * 0.56f), strokeWidth = 3f)
                drawOval(color = Color(0xFFE11D48).copy(alpha = 0.35f), topLeft = Offset(cx - w * 0.28f, h * 0.48f), size = Size(w * 0.22f, h * 0.28f))
                drawOval(color = Color(0xFFE11D48).copy(alpha = 0.35f), topLeft = Offset(cx + w * 0.06f, h * 0.48f), size = Size(w * 0.22f, h * 0.28f))
                drawOval(color = BookBurgundy, topLeft = Offset(cx - w * 0.28f, h * 0.48f), size = Size(w * 0.22f, h * 0.28f), style = Stroke(width = 2f))
                drawOval(color = BookBurgundy, topLeft = Offset(cx + w * 0.06f, h * 0.48f), size = Size(w * 0.22f, h * 0.28f), style = Stroke(width = 2f))
            }
            else -> {
                drawFeedPileIllustration(cx, h * 0.72f, w * 0.52f, h * 0.36f, granuleSizeScale = 1.0f)
                val curve = Path().apply {
                    moveTo(cx - w * 0.22f, h * 0.64f)
                    lineTo(cx - w * 0.02f, h * 0.52f)
                    lineTo(cx + w * 0.20f, h * 0.30f)
                }
                drawPath(curve, color = Color(0xFF15803D), style = Stroke(width = 2.8f, cap = StrokeCap.Round))
            }
        }
    }
}
