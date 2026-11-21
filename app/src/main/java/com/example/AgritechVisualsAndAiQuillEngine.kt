package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.AppIconStyleOption
import com.example.models.AppIconStyleRuntime
import com.example.models.AppPaperStyleOption
import com.example.models.AppPaperStyleRuntime
import com.example.models.AppTabBarMotionStyle
import com.example.models.AppThemeOption
import com.example.models.FarmModel
import com.example.utils.AiVetGeminiService
import com.example.utils.AppLanguage
import com.example.utils.BarnAiContext
import com.example.utils.LocalAppAppearanceState
import com.example.utils.PoultryCalculators
import com.example.utils.Translations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

// ============================================================================
// 1. UNIVERSAL 5 PAPER STYLES ENGINE (BACKGROUNDS & CARD BORDERS — ZERO LINES OVER TEXT!)
// ============================================================================

fun Modifier.universalPaperScreenBackground(
    paperStyle: AppPaperStyleOption,
    isDark: Boolean,
    baseBgColor: Color,
    accentColor: Color
): Modifier = this.drawBehind {
    when (paperStyle) {
        AppPaperStyleOption.DARWIN_PARCHMENT -> {
            val centerBg = if (isDark) Color(0xFF211913) else Color(0xFFF4EBD6)
            val midBg = if (isDark) Color(0xFF1A140F) else Color(0xFFE7D6B5)
            val edgeBg = if (isDark) Color(0xFF120D0A) else Color(0xFFD2B892)
            drawRect(color = centerBg)
            val maxDim = max(size.width, size.height).coerceAtLeast(1f)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, midBg.copy(alpha = 0.55f), edgeBg.copy(alpha = 0.85f)),
                    center = Offset(size.width * 0.5f, size.height * 0.42f),
                    radius = maxDim * 0.78f
                )
            )
            // Subtle aged parchment fiber lines on background only
            val fiberColor = if (isDark) Color(0xFFD7C4A3).copy(alpha = 0.035f) else Color(0xFF6E4720).copy(alpha = 0.045f)
            val stepPx = 30.dp.toPx()
            var y = stepPx
            while (y < size.height) {
                drawLine(
                    color = fiberColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 0.6.dp.toPx()
                )
                y += stepPx
            }
        }

        AppPaperStyleOption.RULED_NOTEBOOK -> {
            val bg = if (isDark) Color(0xFF1A1814) else Color(0xFFF6F0DD)
            drawRect(color = bg)
            val lineSpacing = 28.dp.toPx()
            val ruleColor = Color(0xFF3B82F6).copy(alpha = if (isDark) 0.16f else 0.14f)
            val ruleStroke = 0.7.dp.toPx()
            var y = lineSpacing
            while (y < size.height) {
                drawLine(
                    color = ruleColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = ruleStroke
                )
                y += lineSpacing
            }
            // Vertical red notebook margin line on the background only
            val marginX = size.width - 34.dp.toPx()
            if (marginX > 0f) {
                drawLine(
                    color = Color(0xFFEF4444).copy(alpha = 0.25f),
                    start = Offset(marginX, 0f),
                    end = Offset(marginX, size.height),
                    strokeWidth = 1.2.dp.toPx()
                )
            }
        }

        AppPaperStyleOption.ENGINEERING_GRID -> {
            val bg = if (isDark) Color(0xFF0F171E) else Color(0xFFEBF3F9)
            drawRect(color = bg)
            val minorStep = 8.dp.toPx()
            val minorColor = Color(0xFF0284C7).copy(alpha = if (isDark) 0.07f else 0.06f)
            val majorColor = Color(0xFF0284C7).copy(alpha = if (isDark) 0.15f else 0.13f)
            val minorStroke = 0.5.dp.toPx()
            val majorStroke = 0.9.dp.toPx()

            var x = 0f
            var colIndex = 0
            while (x < size.width) {
                val isMajor = colIndex % 4 == 0
                drawLine(
                    color = if (isMajor) majorColor else minorColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = if (isMajor) majorStroke else minorStroke
                )
                x += minorStep
                colIndex++
            }

            var y = 0f
            var rowIndex = 0
            while (y < size.height) {
                val isMajor = rowIndex % 4 == 0
                drawLine(
                    color = if (isMajor) majorColor else minorColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = if (isMajor) majorStroke else minorStroke
                )
                y += minorStep
                rowIndex++
            }
        }

        AppPaperStyleOption.FROSTED_GLASS -> {
            val maxDim = max(size.width, size.height).coerceAtLeast(1f)
            if (isDark) {
                // Keep existing Smoked Obsidian dark frosted glass untouched
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0E1116), Color(0xFF151921))
                    )
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.28f),
                            accentColor.copy(alpha = 0.09f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.16f),
                        radius = maxDim * 0.62f
                    )
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF38BDF8).copy(alpha = 0.20f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.15f, size.height * 0.80f),
                        radius = maxDim * 0.62f
                    )
                )
                val paperFiberColor = Color.White.copy(alpha = 0.025f)
                val stepPx = 24.dp.toPx()
                var y = stepPx
                while (y < size.height) {
                    drawLine(
                        color = paperFiberColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 0.55.dp.toPx()
                    )
                    y += stepPx
                }
            } else {
                // Ultra Frosted Glass Day Backdrop: Luminous Icy Sky -> Aqua -> Soft Lavender -> Crystal Sky
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE0F2FE),
                            Color(0xFFCCFBF1),
                            Color(0xFFEDE9FE),
                            Color(0xFFBAE6FD)
                        ),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height)
                    )
                )
                // Soft airy ambient glass orbs visible clearly behind translucent 0.58f cards
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF38BDF8).copy(alpha = 0.26f),
                            Color(0xFF2DD4BF).copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.84f, size.height * 0.18f),
                        radius = maxDim * 0.56f
                    )
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFA78BFA).copy(alpha = 0.24f),
                            Color(0xFF7DD3FC).copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.16f, size.height * 0.78f),
                        radius = maxDim * 0.58f
                    )
                )
            }
        }

        AppPaperStyleOption.DEFAULT_PURE -> {
            drawRect(color = baseBgColor)
        }
    }
}

/**
 * Draws ONLY the card's outer frame/border (or translucent layered paper bevel) WITHOUT drawing ANY horizontal or grid lines across text!
 */
fun Modifier.universalPaperCardFrame(
    paperStyle: AppPaperStyleOption,
    isDark: Boolean,
    cornerRadius: Dp,
    accentColor: Color
): Modifier = this.drawWithContent {
    // 1. In FROSTED_GLASS, add a subtle crystal glass sheen behind card content without dulling translucency
    if (paperStyle == AppPaperStyleOption.FROSTED_GLASS) {
        val cornerPx = cornerRadius.toPx()
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = if (isDark) {
                    listOf(
                        Color.White.copy(alpha = 0.13f),
                        Color.White.copy(alpha = 0.03f),
                        accentColor.copy(alpha = 0.07f)
                    )
                } else {
                    listOf(
                        Color.White.copy(alpha = 0.30f),
                        Color.White.copy(alpha = 0.08f),
                        Color.Transparent
                    )
                },
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            ),
            cornerRadius = CornerRadius(cornerPx, cornerPx)
        )
    }

    // 2. Draw all text & card children cleanly without any interfering lines
    drawContent()

    // 3. Draw clean outer border matching the active paper style (NEVER crossing the card interior)
    val cornerPx = cornerRadius.toPx()
    when (paperStyle) {
        AppPaperStyleOption.DARWIN_PARCHMENT -> {
            val outerColor = if (isDark) Color(0xFFC59B27) else Color(0xFF4A321F)
            val innerColor = if (isDark) Color(0xFF8B6914) else Color(0xFF8C6D46)
            val outerStroke = 1.4.dp.toPx()
            val inset = 3.dp.toPx()
            drawRoundRect(
                color = outerColor,
                topLeft = Offset(outerStroke / 2f, outerStroke / 2f),
                size = Size(
                    (size.width - outerStroke).coerceAtLeast(0f),
                    (size.height - outerStroke).coerceAtLeast(0f)
                ),
                cornerRadius = CornerRadius(cornerPx, cornerPx),
                style = Stroke(width = outerStroke)
            )
            if (size.width > inset * 2 && size.height > inset * 2) {
                val innerCorner = (cornerPx - inset).coerceAtLeast(2.dp.toPx())
                drawRoundRect(
                    color = innerColor,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2, size.height - inset * 2),
                    cornerRadius = CornerRadius(innerCorner, innerCorner),
                    style = Stroke(width = 0.7.dp.toPx())
                )
            }
        }

        AppPaperStyleOption.RULED_NOTEBOOK -> {
            val outerColor = if (isDark) Color(0xFF93C5FD).copy(alpha = 0.34f) else Color(0xFF3B82F6).copy(alpha = 0.30f)
            val marginAccent = Color(0xFFEF4444).copy(alpha = if (isDark) 0.28f else 0.22f)
            drawRoundRect(
                color = outerColor,
                cornerRadius = CornerRadius(cornerPx, cornerPx),
                style = Stroke(width = 1.2.dp.toPx())
            )
            val edgeInset = 4.dp.toPx()
            if (size.width > 40.dp.toPx() && size.height > 28.dp.toPx()) {
                drawLine(
                    color = marginAccent,
                    start = Offset(size.width - edgeInset, cornerPx * 0.75f),
                    end = Offset(size.width - edgeInset, size.height - cornerPx * 0.75f),
                    strokeWidth = 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        AppPaperStyleOption.ENGINEERING_GRID -> {
            val outerColor = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.38f) else Color(0xFF0284C7).copy(alpha = 0.34f)
            val innerColor = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.16f) else Color(0xFF0284C7).copy(alpha = 0.15f)
            val inset = 3.dp.toPx()
            drawRoundRect(
                color = outerColor,
                cornerRadius = CornerRadius(cornerPx, cornerPx),
                style = Stroke(width = 1.25.dp.toPx())
            )
            if (size.width > inset * 4 && size.height > inset * 4) {
                val innerCorner = (cornerPx - inset).coerceAtLeast(2f)
                drawRoundRect(
                    color = innerColor,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2, size.height - inset * 2),
                    cornerRadius = CornerRadius(innerCorner, innerCorner),
                    style = Stroke(width = 0.65.dp.toPx())
                )
            }
        }

        AppPaperStyleOption.FROSTED_GLASS -> {
            if (isDark) {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF).copy(alpha = 0.62f),
                            Color(0xFFCBD5E1).copy(alpha = 0.28f)
                        ),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height)
                    ),
                    cornerRadius = CornerRadius(cornerPx, cornerPx),
                    style = Stroke(width = 1.3.dp.toPx())
                )
            } else {
                // Crisp 1.2.dp glossy white frosted-glass frame
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.85f),
                    cornerRadius = CornerRadius(cornerPx, cornerPx),
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }
        }

        AppPaperStyleOption.DEFAULT_PURE -> {
            // Standard clean card border
        }
    }
}

// ============================================================================
// 1B. UNIVERSAL 3D TACTILE ICON BADGE (`Tactile3DIconBadge`)
// ============================================================================

@Composable
fun Tactile3DIconBadge(
    emoji: String? = null,
    icon: ImageVector? = null,
    tint: Color = Color(0xFF10B981),
    containerColor: Color = tint.copy(alpha = 0.14f),
    boxSize: Dp = 44.dp,
    cornerRadius: Dp = 14.dp,
    emojiFontSize: TextUnit = 20.sp,
    iconSize: Dp = 20.dp,
    iconStyle: AppIconStyleOption = AppIconStyleRuntime.activeIconStyle,
    isDark: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isFrosted = AppPaperStyleRuntime.activePaperStyle == AppPaperStyleOption.FROSTED_GLASS
    val effectiveContainer = if (isFrosted) {
        if (isDark) Color(0xFF1E293B).copy(alpha = 0.62f) else Color(0xFFFFFDF9).copy(alpha = 0.72f)
    } else {
        containerColor
    }

    when (iconStyle) {
        AppIconStyleOption.TACTILE_3D -> {
            val shape = RoundedCornerShape(cornerRadius)
            val lipColor = Color(
                red = (tint.red * 0.65f).coerceIn(0f, 1f),
                green = (tint.green * 0.65f).coerceIn(0f, 1f),
                blue = (tint.blue * 0.70f).coerceIn(0f, 1f),
                alpha = if (isFrosted) 0.45f else 0.72f
            )
            Box(
                modifier = modifier
                    .size(boxSize)
                    .then(
                        if (!isFrosted) {
                            Modifier.shadow(
                                elevation = 4.dp,
                                shape = shape,
                                spotColor = lipColor,
                                ambientColor = lipColor
                            )
                        } else {
                            Modifier
                        }
                    )
                    .clip(shape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                if (isDark) effectiveContainer.copy(alpha = 0.92f) else Color.White.copy(alpha = if (isFrosted) 0.78f else 0.90f),
                                effectiveContainer
                            )
                        ),
                        shape = shape
                    )
                    .border(
                        width = 1.2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isDark) 0.45f else 0.88f),
                                tint.copy(alpha = 0.50f)
                            )
                        ),
                        shape = shape
                    )
                    .drawWithContent {
                        drawContent()
                        val lipPx = 2.6.dp.toPx()
                        drawRect(
                            color = lipColor,
                            topLeft = Offset(0f, size.height - lipPx),
                            size = Size(size.width, lipPx)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (emoji != null) {
                    Text(text = emoji, fontSize = emojiFontSize)
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }

        AppIconStyleOption.CLASSIC_INK -> {
            val shape = RoundedCornerShape(cornerRadius)
            val outerInk = if (isDark) Color(0xFFD4AF37) else Color(0xFF4A321F)
            val innerInk = if (isDark) Color(0xFF8B6914) else Color(0xFF8C6D46)
            Box(
                modifier = modifier
                    .size(boxSize)
                    .clip(shape)
                    .background(effectiveContainer, shape)
                    .drawWithContent {
                        drawContent()
                        val rPx = cornerRadius.toPx()
                        drawRoundRect(
                            color = outerInk,
                            cornerRadius = CornerRadius(rPx, rPx),
                            style = Stroke(width = 1.4.dp.toPx())
                        )
                        val inset = 2.8.dp.toPx()
                        val innerR = (rPx - inset).coerceAtLeast(2f)
                        drawRoundRect(
                            color = innerInk.copy(alpha = 0.7f),
                            topLeft = Offset(inset, inset),
                            size = Size(
                                (size.width - inset * 2f).coerceAtLeast(0f),
                                (size.height - inset * 2f).coerceAtLeast(0f)
                            ),
                            cornerRadius = CornerRadius(innerR, innerR),
                            style = Stroke(width = 0.7.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (emoji != null) {
                    Text(text = emoji, fontSize = emojiFontSize)
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }

        AppIconStyleOption.FLAT_MINIMAL -> {
            Surface(
                modifier = modifier.size(boxSize),
                shape = CircleShape,
                color = effectiveContainer,
                border = if (isFrosted) BorderStroke(1.dp, tint.copy(alpha = 0.35f)) else null
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (emoji != null) {
                        Text(text = emoji, fontSize = emojiFontSize)
                    } else if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 2. SIX INTERACTIVE 3D PAPER CAPSULE TAB BAR MOTIONS (Video 2: Tab Bar Part 12)
// ============================================================================

data class AgritechTabBarItemSpec(
    val id: String,
    val label: String,
    val icon: ImageVector? = null,
    val emoji: String? = null,
    val badgeCount: Int = 0,
    val testTag: String
)

/**
 * 3D Tactile Paper Capsule Tab Bar matching Video 2 (`Tab Bar Part 12 — The Six Tab Moves`):
 * - Includes 12.dp top headroom (`clip = false` behavior) above the 3D capsule pill so `02 LIQUID BULGE`,
 *   `03 FLOATING NOTCH`, and `06 BUBBLE FLOAT` never clip at the top edge.
 * - Preserves `0.94f` high-frost opacity in `FROSTED_GLASS` (`📄 مظهر أوراق شفاف`) with a `1.dp #CBD5E1` top divider.
 * - Respects `Modifier.navigationBarsPadding()`.
 */
@Composable
fun AgritechAnimatedBottomBar(
    items: List<AgritechTabBarItemSpec>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    currentTheme: AppThemeOption,
    modifier: Modifier = Modifier
) {
    val appearance = LocalAppAppearanceState.current
    val activeAccent = appearance.accentColor
    val motionStyle = appearance.tabBarMotion
    val paperStyle = appearance.paperStyle
    val isDark = currentTheme.isDark

    // Outer dock surface preserves 0.94f opacity in FROSTED_GLASS so scrolling content never bleeds behind the dock
    val dockSurfaceColor = when (paperStyle) {
        AppPaperStyleOption.DARWIN_PARCHMENT ->
            if (isDark) Color(0xFF211913).copy(alpha = 0.96f) else Color(0xFFF4EBD6).copy(alpha = 0.96f)
        AppPaperStyleOption.RULED_NOTEBOOK ->
            if (isDark) Color(0xFF1A1814).copy(alpha = 0.96f) else Color(0xFFF6F0DD).copy(alpha = 0.96f)
        AppPaperStyleOption.ENGINEERING_GRID ->
            if (isDark) Color(0xFF0F171E).copy(alpha = 0.96f) else Color(0xFFEBF3F9).copy(alpha = 0.96f)
        AppPaperStyleOption.FROSTED_GLASS ->
            if (isDark) Color(0xFF0F172A).copy(alpha = 0.48f) else Color.White.copy(alpha = 0.24f)
        AppPaperStyleOption.DEFAULT_PURE ->
            if (isDark) Color(0xFF121722).copy(alpha = 0.96f) else Color(0xFFF8FAFC).copy(alpha = 0.96f)
    }

    // 3D Tactile Paper Capsule body colors (ultra-sheer crystal glass in FROSTED_GLASS, warm tactile paper in other modes)
    val capsuleTopColor = when (paperStyle) {
        AppPaperStyleOption.DARWIN_PARCHMENT ->
            if (isDark) Color(0xFF30241B) else Color(0xFFFFFAF0)
        AppPaperStyleOption.RULED_NOTEBOOK ->
            if (isDark) Color(0xFF2B2720) else Color(0xFFFFFEF7)
        AppPaperStyleOption.ENGINEERING_GRID ->
            if (isDark) Color(0xFF1E2C3A) else Color(0xFFF8FCFF)
        AppPaperStyleOption.FROSTED_GLASS ->
            if (isDark) Color(0xFF1E293B).copy(alpha = 0.54f) else Color.White.copy(alpha = 0.30f)
        AppPaperStyleOption.DEFAULT_PURE ->
            if (isDark) Color(0xFF1E2534) else Color(0xFFFFFFFF)
    }
    val capsuleBottomColor = when (paperStyle) {
        AppPaperStyleOption.DARWIN_PARCHMENT ->
            if (isDark) Color(0xFF241B14) else Color(0xFFF0E4CE)
        AppPaperStyleOption.RULED_NOTEBOOK ->
            if (isDark) Color(0xFF201D17) else Color(0xFFF1EBD8)
        AppPaperStyleOption.ENGINEERING_GRID ->
            if (isDark) Color(0xFF15202B) else Color(0xFFE4F0F8)
        AppPaperStyleOption.FROSTED_GLASS ->
            if (isDark) Color(0xFF0F172A).copy(alpha = 0.46f) else Color.White.copy(alpha = 0.16f)
        AppPaperStyleOption.DEFAULT_PURE ->
            if (isDark) Color(0xFF161B26) else Color(0xFFF1F5F9)
    }
    val capsuleLipColor = when {
        paperStyle == AppPaperStyleOption.FROSTED_GLASS -> Color.Transparent
        isDark -> Color(0xFF090D14)
        else -> Color(0xFFD5CFC2)
    }
    val capsuleStrokeColor = when {
        paperStyle == AppPaperStyleOption.FROSTED_GLASS ->
            Color.White.copy(alpha = if (isDark) 0.24f else 0.78f)
        isDark -> Color.White.copy(alpha = 0.22f)
        else -> Color.White
    }

    // Soft sage/accent 3D pill & disc fill (like the tactile pale-green paper discs in Video 2)
    val activePaperDiscFill = if (isDark) {
        activeAccent.copy(alpha = 0.28f)
    } else {
        Color(
            red = (activeAccent.red * 0.22f + 0.76f).coerceIn(0f, 1f),
            green = (activeAccent.green * 0.24f + 0.74f).coerceIn(0f, 1f),
            blue = (activeAccent.blue * 0.22f + 0.73f).coerceIn(0f, 1f),
            alpha = 1f
        )
    }
    val activePaperDiscLip = if (isDark) {
        activeAccent.copy(alpha = 0.55f)
    } else {
        activeAccent.copy(alpha = 0.38f)
    }

    val topBorderColor = when (paperStyle) {
        AppPaperStyleOption.DARWIN_PARCHMENT ->
            if (isDark) Color(0xFFD4AF37).copy(alpha = 0.35f) else Color(0xFF8B5A2B).copy(alpha = 0.28f)
        AppPaperStyleOption.RULED_NOTEBOOK ->
            Color(0xFF3B82F6).copy(alpha = 0.22f)
        AppPaperStyleOption.ENGINEERING_GRID ->
            Color(0xFF0284C7).copy(alpha = 0.25f)
        AppPaperStyleOption.FROSTED_GLASS ->
            Color.White.copy(alpha = 0.65f)
        AppPaperStyleOption.DEFAULT_PURE ->
            activeAccent.copy(alpha = 0.16f)
    }

    val safeSelectedIndex = selectedIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))
    val animatedIndex by animateFloatAsState(
        targetValue = safeSelectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.68f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tab_bar_index_spring"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "tab_bar_particles")
    val bubblePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble_phase"
    )

    // Total dock height = 76.dp (12.dp top headroom for unclipped Floating Notch / Liquid Bulge / Paper Bubbles + 56.dp 3D Paper Capsule + 8.dp bottom cushion)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(76.dp),
        color = dockSurfaceColor,
        tonalElevation = 0.dp,
        shadowElevation = if (paperStyle == AppPaperStyleOption.FROSTED_GLASS) 0.dp else 6.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Top separator line (0.8.dp glossy white hairline in FROSTED_GLASS)
                    drawLine(
                        color = topBorderColor,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = if (paperStyle == AppPaperStyleOption.FROSTED_GLASS) 0.8.dp.toPx() else 1.dp.toPx()
                    )

                    val horizPad = 14.dp.toPx()
                    val topHeadroom = 12.dp.toPx()
                    val bottomPad = 8.dp.toPx()
                    val capsuleLeft = horizPad
                    val capsuleRight = (size.width - horizPad).coerceAtLeast(capsuleLeft + 10f)
                    val capsuleWidth = capsuleRight - capsuleLeft
                    val capsuleTop = topHeadroom
                    val capsuleBottom = (size.height - bottomPad).coerceAtLeast(capsuleTop + 20f)
                    val capsuleHeight = capsuleBottom - capsuleTop
                    val capsuleRadius = capsuleHeight / 2f
                    val lipOffset = 3.2.dp.toPx()

                    val tabWidth = if (items.isNotEmpty()) capsuleWidth / items.size.toFloat() else capsuleWidth
                    val effectiveIdx = if (layoutDirection == LayoutDirection.Rtl) {
                        (items.size - 1).toFloat() - animatedIndex
                    } else {
                        animatedIndex
                    }
                    val centerX = capsuleLeft + (effectiveIdx + 0.5f) * tabWidth
                    val capsuleCenterY = capsuleTop + capsuleHeight / 2f

                    // 1. Draw 3D Tactile Paper Capsule Base (with organic roof bulge when motionStyle == LIQUID_BULGE)
                    if (motionStyle == AppTabBarMotionStyle.LIQUID_BULGE && items.isNotEmpty()) {
                        val bulgeHalfW = (tabWidth * 0.62f).coerceAtMost(48.dp.toPx())
                        val bulgePeakH = 10.dp.toPx()
                        val safeBulgeLeft = (centerX - bulgeHalfW).coerceAtLeast(capsuleLeft + capsuleRadius * 0.6f)
                        val safeBulgeRight = (centerX + bulgeHalfW).coerceAtMost(capsuleRight - capsuleRadius * 0.6f)

                        fun buildBulgeCapsulePath(yOffset: Float): Path {
                            val topY = capsuleTop + yOffset
                            val botY = capsuleBottom + yOffset
                            return Path().apply {
                                moveTo(capsuleLeft + capsuleRadius, topY)
                                if (safeBulgeLeft > capsuleLeft + capsuleRadius) {
                                    lineTo(safeBulgeLeft, topY)
                                }
                                cubicTo(
                                    centerX - bulgeHalfW * 0.48f, topY,
                                    centerX - bulgeHalfW * 0.36f, topY - bulgePeakH,
                                    centerX, topY - bulgePeakH
                                )
                                cubicTo(
                                    centerX + bulgeHalfW * 0.36f, topY - bulgePeakH,
                                    centerX + bulgeHalfW * 0.48f, topY,
                                    safeBulgeRight, topY
                                )
                                lineTo(capsuleRight - capsuleRadius, topY)
                                quadraticTo(capsuleRight, topY, capsuleRight, topY + capsuleRadius)
                                quadraticTo(capsuleRight, botY, capsuleRight - capsuleRadius, botY)
                                lineTo(capsuleLeft + capsuleRadius, botY)
                                quadraticTo(capsuleLeft, botY, capsuleLeft, topY + capsuleRadius)
                                quadraticTo(capsuleLeft, topY, capsuleLeft + capsuleRadius, topY)
                                close()
                            }
                        }

                        // 3D Bottom Lip Shadow
                        drawPath(
                            path = buildBulgeCapsulePath(lipOffset),
                            color = capsuleLipColor
                        )
                        // Main Paper Capsule Body with Liquid Bulge
                        val mainBulgePath = buildBulgeCapsulePath(0f)
                        drawPath(
                            path = mainBulgePath,
                            brush = Brush.verticalGradient(
                                colors = listOf(capsuleTopColor, capsuleBottomColor),
                                startY = capsuleTop - bulgePeakH,
                                endY = capsuleBottom
                            )
                        )
                        drawPath(
                            path = mainBulgePath,
                            color = capsuleStrokeColor,
                            style = Stroke(width = 1.3.dp.toPx())
                        )
                    } else {
                        // Standard 3D Tactile Paper Capsule with 3.2.dp bottom lip
                        drawRoundRect(
                            color = capsuleLipColor,
                            topLeft = Offset(capsuleLeft, capsuleTop + lipOffset),
                            size = Size(capsuleWidth, capsuleHeight),
                            cornerRadius = CornerRadius(capsuleRadius, capsuleRadius)
                        )
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(capsuleTopColor, capsuleBottomColor),
                                startY = capsuleTop,
                                endY = capsuleBottom
                            ),
                            topLeft = Offset(capsuleLeft, capsuleTop),
                            size = Size(capsuleWidth, capsuleHeight),
                            cornerRadius = CornerRadius(capsuleRadius, capsuleRadius)
                        )
                        drawRoundRect(
                            color = capsuleStrokeColor,
                            topLeft = Offset(capsuleLeft, capsuleTop),
                            size = Size(capsuleWidth, capsuleHeight),
                            cornerRadius = CornerRadius(capsuleRadius, capsuleRadius),
                            style = Stroke(width = 1.3.dp.toPx())
                        )
                    }

                    // 2. Draw Active Motion Indicator matching Video 2 (Tab Bar Part 12)
                    if (items.isNotEmpty()) {
                        when (motionStyle) {
                            AppTabBarMotionStyle.SLIDING_PILL -> {
                                // 01 — SLIDING PILL: Clean sliding top accent pill without any opaque background rectangle behind the active tab
                                val pillW = (tabWidth * 0.48f).coerceIn(26.dp.toPx(), 44.dp.toPx())
                                val pillH = 3.2.dp.toPx()
                                val leftX = centerX - pillW / 2f
                                drawRoundRect(
                                    color = activeAccent,
                                    topLeft = Offset(leftX, capsuleTop + 1.dp.toPx()),
                                    size = Size(pillW, pillH),
                                    cornerRadius = CornerRadius(pillH, pillH)
                                )
                            }

                            AppTabBarMotionStyle.LIQUID_BULGE -> {
                                // 02 — LIQUID BULGE: Pure organic roof bulge without any extra disc obscuring the icon or label
                                val dotR = 2.6.dp.toPx()
                                drawCircle(
                                    color = activeAccent,
                                    radius = dotR,
                                    center = Offset(centerX, capsuleTop - 2.dp.toPx())
                                )
                            }

                            AppTabBarMotionStyle.FLOATING_NOTCH -> {
                                // 03 — FLOATING NOTCH: 3D paper disc hops up (-8.dp) into the 12.dp top headroom
                                val discR = 19.5.dp.toPx()
                                val discY = capsuleTop + 11.dp.toPx()
                                // Outer paper ring halo
                                drawCircle(
                                    color = capsuleTopColor,
                                    radius = discR + 3.5.dp.toPx(),
                                    center = Offset(centerX, discY)
                                )
                                drawCircle(
                                    color = capsuleLipColor,
                                    radius = discR + 3.5.dp.toPx(),
                                    center = Offset(centerX, discY),
                                    style = Stroke(width = 1.dp.toPx())
                                )
                                // 3D disc lip & body
                                drawCircle(
                                    color = activePaperDiscLip,
                                    radius = discR,
                                    center = Offset(centerX, discY + 2.dp.toPx())
                                )
                                drawCircle(
                                    color = activePaperDiscFill,
                                    radius = discR,
                                    center = Offset(centerX, discY)
                                )
                                drawCircle(
                                    color = activeAccent.copy(alpha = 0.55f),
                                    radius = discR,
                                    center = Offset(centerX, discY),
                                    style = Stroke(width = 1.2.dp.toPx())
                                )
                            }

                            AppTabBarMotionStyle.ICON_FILL -> {
                                // 04 — SPOTLIGHT BEAM FROM TOP: Glowing top-edge bar + soft downward light cone onto the icon
                                val topBarW = (tabWidth * 0.44f).coerceIn(24.dp.toPx(), 40.dp.toPx())
                                val topBarH = 3.2.dp.toPx()
                                val beamTopY = capsuleTop + topBarH * 0.6f
                                val beamBottomY = capsuleBottom - 5.dp.toPx()
                                val beamHalfTop = topBarW * 0.46f
                                val beamHalfBottom = (tabWidth * 0.46f).coerceIn(28.dp.toPx(), 46.dp.toPx())

                                val spotlightPath = Path().apply {
                                    moveTo(centerX - beamHalfTop, beamTopY)
                                    lineTo(centerX + beamHalfTop, beamTopY)
                                    lineTo(centerX + beamHalfBottom, beamBottomY)
                                    lineTo(centerX - beamHalfBottom, beamBottomY)
                                    close()
                                }
                                drawPath(
                                    path = spotlightPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            activeAccent.copy(alpha = if (isDark) 0.36f else 0.28f),
                                            activeAccent.copy(alpha = if (isDark) 0.14f else 0.10f),
                                            Color.Transparent
                                        ),
                                        startY = beamTopY,
                                        endY = beamBottomY
                                    )
                                )
                                // Luminous indicator bar at the top edge of the active tab
                                drawRoundRect(
                                    color = activeAccent,
                                    topLeft = Offset(centerX - topBarW / 2f, capsuleTop),
                                    size = Size(topBarW, topBarH),
                                    cornerRadius = CornerRadius(topBarH, topBarH)
                                )
                            }

                            AppTabBarMotionStyle.EXPANDING_LABEL -> {
                                // 05 — EXPANDING LABEL: Handled by the 3D carved pill Box around the selected item
                            }

                            AppTabBarMotionStyle.BUBBLE_FLOAT -> {
                                // 06 — BUBBLE FLOAT: Clean rising bubbles above the icon with NO obscuring circle over icon/label
                                val offsetsX = listOf(-12f, 11f, -3f, 14f, 3f)
                                val phaseShifts = listOf(0f, 0.22f, 0.45f, 0.67f, 0.85f)
                                for (i in offsetsX.indices) {
                                    val p = (bubblePhase + phaseShifts[i]) % 1f
                                    val bx = centerX + offsetsX[i].dp.toPx()
                                    val by = (capsuleTop + 10.dp.toPx()) - (p * 19.dp.toPx())
                                    val bAlpha = ((1f - p) * 0.90f).coerceIn(0f, 1f)
                                    val br = (4.6f - p * 2.0f).coerceAtLeast(2.0f).dp.toPx()
                                    drawCircle(
                                        color = activePaperDiscFill.copy(alpha = bAlpha * 0.85f),
                                        radius = br,
                                        center = Offset(bx, by)
                                    )
                                    drawCircle(
                                        color = activeAccent.copy(alpha = bAlpha * 0.65f),
                                        radius = br,
                                        center = Offset(bx, by),
                                        style = Stroke(width = 0.85.dp.toPx())
                                    )
                                }
                            }
                        }
                    }
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = index == safeSelectedIndex
                    val iconScale by animateFloatAsState(
                        targetValue = when {
                            !isSelected -> 1f
                            motionStyle == AppTabBarMotionStyle.ICON_FILL -> 1.18f
                            motionStyle == AppTabBarMotionStyle.FLOATING_NOTCH -> 1.12f
                            motionStyle == AppTabBarMotionStyle.LIQUID_BULGE -> 1.10f
                            else -> 1.06f
                        },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "tab_icon_scale_$index"
                    )
                    val verticalOffset by animateDpAsState(
                        targetValue = when {
                            !isSelected -> 0.dp
                            motionStyle == AppTabBarMotionStyle.FLOATING_NOTCH -> (-8).dp
                            motionStyle == AppTabBarMotionStyle.LIQUID_BULGE -> (-3).dp
                            motionStyle == AppTabBarMotionStyle.ICON_FILL -> (-2).dp
                            else -> 0.dp
                        },
                        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
                        label = "tab_notch_offset_$index"
                    )

                    val unselectedTint = if (isDark) Color(0xFF94A3B8) else Color(0xFF6E6A5F)
                    val selectedContentTint = if (isSelected) activeAccent else unselectedTint

                    val isExpandingPillStyle = motionStyle == AppTabBarMotionStyle.EXPANDING_LABEL

                    Box(
                        modifier = Modifier
                            .weight(if (isExpandingPillStyle && isSelected) 1.38f else 1f)
                            .fillMaxHeight()
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                            .background(Color.Transparent)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onSelectIndex(index) }
                            .testTag(item.testTag),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isExpandingPillStyle) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            ) {
                                TabIconWithBadge(
                                    item = item,
                                    tint = selectedContentTint,
                                    scale = iconScale,
                                    accentColor = activeAccent,
                                    isSelected = isSelected
                                )
                                AnimatedVisibility(
                                    visible = isSelected,
                                    enter = fadeIn(tween(180)) + expandHorizontally(tween(220)),
                                    exit = fadeOut(tween(120)) + shrinkHorizontally(tween(180))
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = item.label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = activeAccent,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(modifier = Modifier.offset(y = verticalOffset)) {
                                    TabIconWithBadge(
                                        item = item,
                                        tint = selectedContentTint,
                                        scale = iconScale,
                                        accentColor = activeAccent,
                                        isSelected = isSelected
                                    )
                                }
                                Spacer(modifier = Modifier.height(1.5.dp))
                                Text(
                                    text = item.label,
                                    fontSize = 9.5.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) activeAccent else unselectedTint,
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
}

@Composable
private fun TabIconWithBadge(
    item: AgritechTabBarItemSpec,
    tint: Color,
    scale: Float,
    accentColor: Color,
    isSelected: Boolean
) {
    Box(contentAlignment = Alignment.TopEnd) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .scale(scale),
            contentAlignment = Alignment.Center
        ) {
            if (item.icon != null) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            } else if (item.emoji != null) {
                Text(
                    text = item.emoji,
                    fontSize = if (isSelected) 16.sp else 15.sp
                )
            }
        }
        if (item.badgeCount > 0) {
            Box(
                modifier = Modifier
                    .offset(x = 6.dp, y = (-4).dp)
                    .size(15.dp)
                    .clip(CircleShape)
                    .background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (item.badgeCount > 9) "9+" else item.badgeCount.toString(),
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ============================================================================
// 3. OPEN GEMINI-STYLE AI VET CHAT + MASTER CALLIGRAPHIC FEATHER QUILL (📖 الكتاب ➔ 💬 المحادثة)
// ============================================================================

enum class QuillActivityState {
    IDLE,
    THINKING,
    WRITING
}

private const val PREFS_GEMINI_BYOK = "agritech_gemini_byok_prefs"
private const val KEY_PERSONAL_GEMINI_API_KEY = "personal_gemini_api_key"

object PersonalGeminiKeyStore {
    fun getSavedKey(context: Context): String {
        return context.getSharedPreferences(PREFS_GEMINI_BYOK, Context.MODE_PRIVATE)
            .getString(KEY_PERSONAL_GEMINI_API_KEY, "")
            ?.trim()
            .orEmpty()
    }

    fun saveKey(context: Context, apiKey: String) {
        context.getSharedPreferences(PREFS_GEMINI_BYOK, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PERSONAL_GEMINI_API_KEY, apiKey.trim())
            .apply()
    }

    fun clearKey(context: Context) {
        context.getSharedPreferences(PREFS_GEMINI_BYOK, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_PERSONAL_GEMINI_API_KEY)
            .apply()
    }
}

private data class GeminiLiveCallResult(
    val text: String,
    val modelUsed: String,
    val succeeded: Boolean
)

/**
 * Calls ONLY active modern Gemini models (`gemini-2.5-flash` and `gemini-2.0-flash`).
 * Never calls deprecated `gemini-1.5-flash`, and never leaks raw English technical errors to the UI.
 */
private suspend fun callPersonalGeminiHttpApi(
    apiKey: String,
    systemInstruction: String,
    userPrompt: String
): GeminiLiveCallResult = withContext(Dispatchers.IO) {
    val trimmedKey = apiKey.trim()
    if (!trimmedKey.startsWith("AIza")) {
        return@withContext GeminiLiveCallResult(text = "", modelUsed = "", succeeded = false)
    }

    val candidateModels = listOf(
        "gemini-2.5-flash",
        "gemini-2.0-flash"
    )

    for (modelName in candidateModels) {
        var connection: HttpURLConnection? = null
        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"
            val url = URL(endpoint)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 12000
                readTimeout = 20000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("x-goog-api-key", trimmedKey)
            }

            val payload = JSONObject().apply {
                put(
                    "system_instruction",
                    JSONObject().apply {
                        put(
                            "parts",
                            JSONArray().put(
                                JSONObject().put("text", systemInstruction)
                            )
                        )
                    }
                )
                put(
                    "contents",
                    JSONArray().put(
                        JSONObject().apply {
                            put("role", "user")
                            put(
                                "parts",
                                JSONArray().put(
                                    JSONObject().put("text", userPrompt)
                                )
                            )
                        }
                    )
                )
                put(
                    "generationConfig",
                    JSONObject().apply {
                        put("temperature", 0.35)
                        put("maxOutputTokens", 1024)
                    }
                )
            }

            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            val statusCode = connection.responseCode
            val stream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
            val responseBody = if (stream != null) {
                BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
            } else {
                ""
            }

            if (statusCode in 200..299 && responseBody.isNotBlank()) {
                val rootJson = JSONObject(responseBody)
                val candidates = rootJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.optJSONObject(0)
                    val contentObj = firstCandidate?.optJSONObject("content")
                    val partsArr = contentObj?.optJSONArray("parts")
                    val sb = StringBuilder()
                    if (partsArr != null) {
                        for (i in 0 until partsArr.length()) {
                            val partText = partsArr.optJSONObject(i)?.optString("text").orEmpty()
                            if (partText.isNotBlank()) {
                                sb.append(partText)
                            }
                        }
                    }
                    val finalAnswer = sb.toString().trim()
                    if (finalAnswer.isNotBlank()) {
                        return@withContext GeminiLiveCallResult(
                            text = finalAnswer,
                            modelUsed = modelName,
                            succeeded = true
                        )
                    }
                }
            }
        } catch (_: Exception) {
            // Silent fallback to conversational veterinary engine
        } finally {
            connection?.disconnect()
        }
    }

    GeminiLiveCallResult(
        text = "",
        modelUsed = "",
        succeeded = false
    )
}

/**
 * Intelligent Conversational Barn Analysis Engine:
 * Produces natural, Gemini-style open responses tailored to the active barn (`algrien` / `selectedFarm`)
 * and the exact question asked by the farmer, with zero error banners.
 */
private suspend fun buildConversationalBarnReply(
    question: String,
    lang: AppLanguage,
    barnName: String,
    ageDays: Int,
    placedBirds: Int,
    liveBirds: Int,
    totalMortality: Int,
    livabilityPct: Double,
    totalFeedQuintals: Double,
    remainingFeedQuintals: Double,
    latestTempC: Double?,
    latestWaterLiters: Double,
    medsSummary: String,
    barnContext: BarnAiContext
): String {
    val baseServiceReply = try {
        AiVetGeminiService.generateVetResponse(
            question = question,
            lang = lang,
            flockAgeDays = ageDays,
            liveBirds = liveBirds,
            mortalityPct = (100.0 - livabilityPct).coerceAtLeast(0.0),
            currentTempC = latestTempC,
            barnName = barnName,
            barnContext = barnContext
        ).replace("**", "").trim()
    } catch (_: Exception) {
        ""
    }
    if (lang != AppLanguage.AR && baseServiceReply.isNotBlank()) {
        return baseServiceReply
    }
    val displayBarn = barnName.ifBlank { "العنبر النشط" }
    val targetTemp = when {
        ageDays <= 3 -> 33.0
        ageDays <= 7 -> 31.0
        ageDays <= 14 -> 29.0
        ageDays <= 21 -> 27.0
        ageDays <= 28 -> 24.0
        else -> 21.0
    }
    val targetWeightKg = PoultryCalculators.getRoss308StandardWeightKg(ageDays)
    val standingBiomassKg = liveBirds * targetWeightKg
    val fcrEstimate = if (standingBiomassKg > 0.0 && totalFeedQuintals > 0.0) {
        (totalFeedQuintals * 100.0) / standingBiomassKg
    } else {
        Ross308BroilerHandbook.getStandardFcrForDay(ageDays)
    }
    val qFormatted = Translations.formatQuintalNumber(totalFeedQuintals)
    val remQFormatted = Translations.formatQuintalNumber(remainingFeedQuintals)
    val tempStatus = if (latestTempC != null) {
        val diff = latestTempC - targetTemp
        when {
            diff > 2.0 -> "مرتفعة بـ ${String.format(java.util.Locale.US, "%.1f", diff)}°C عن المثالي (${targetTemp.toInt()}°C)؛ يُنصح برفع التهوية تدريجياً وإضافة فيتامين C أو إلكتروليت في ماء الشرب."
            diff < -2.0 -> "أقل بـ ${String.format(java.util.Locale.US, "%.1f", abs(diff))}°C من المثالي (${targetTemp.toInt()}°C)؛ يُنصح بضبط التدفئة لمنع تجمع الطيور وارتفاع الرطوبة."
            else -> "مثالية ومطابقة لمعيار Ross 308 لعمر $ageDays يوم (${String.format(java.util.Locale.US, "%.1f", latestTempC)}°C مقابل ${targetTemp.toInt()}°C)."
        }
    } else {
        "الحرارة المثالية لعمر $ageDays يوم هي ${targetTemp.toInt()}°C مع رطوبة نسبية 60% وتجديد هواء منتظم."
    }

    val qLower = question.lowercase()
    return when {
        qLower.contains("حرار") || qLower.contains("تهوي") || qLower.contains("مناخ") || qLower.contains("رطوب") -> buildString {
            appendLine("بناءً على بيانات عنبر «$displayBarn» في اليوم $ageDays ($liveBirds طائر حي من أصل $placedBirds):")
            appendLine()
            appendLine("🌡️ تقييم الحرارة والتهوية:")
            appendLine("• $tempStatus")
            appendLine("• في عمر $ageDays يوم، يحتاج القطيع إلى تيار هواء علوي غير مباشر لسحب الأمونيا وثاني أكسيد الكربون دون تعريض ظهر الكتاكيت لتيار بارد.")
            appendLine("• استهلاك الماء المسجل ($latestWaterLiters لتر) يعكس نشاط الشرب؛ تأكد من بقاء حرارة ماء الشرب بين 18°C و21°C.")
        }.trim()

        qLower.contains("علف") || qLower.contains("تحويل") || qLower.contains("fcr") || qLower.contains("وزن") || qLower.contains("قنطار") -> buildString {
            appendLine("تحليل كفاءة العلف والوزن لعنبر «$displayBarn» (اليوم $ageDays):")
            appendLine()
            appendLine("🌾 الميزان العلفي ومعامل التحويل (بالقنطار ق = 100 كغ):")
            appendLine("• إجمالي العلف المستهلك حتى الآن: $qFormatted ق (${(totalFeedQuintals * 100).toInt()} كغ).")
            appendLine("• المخزون المتبقي في العنبر: $remQFormatted ق.")
            appendLine("• الوزن المرجعي المتوقع لعمر $ageDays يوم: ${String.format(java.util.Locale.US, "%.2f", targetWeightKg)} كغ/طائر (الكتلة الحية التقديرية: ${String.format(java.util.Locale.US, "%,.0f", standingBiomassKg)} كغ).")
            appendLine("• معامل التحويل التقديري (FCR): ${String.format(java.util.Locale.US, "%.2f", fcrEstimate)}.")
            appendLine("💡 نصيحة عملية: اضبط ارتفاع المعالف ليكون بمستوى ظهر الطائر لتقليل الفاقد في الفرشة وتحسين معامل التحويل.")
        }.trim()

        qLower.contains("تحصين") || qLower.contains("لقاح") || qLower.contains("دواء") || qLower.contains("نافق") || qLower.contains("موت") || qLower.contains("حيوي") -> buildString {
            appendLine("التقييم الصحي وبرنامج الوقاية لعنبر «$displayBarn» (اليوم $ageDays):")
            appendLine()
            appendLine("💊 الحيوية وسجل الأدوية:")
            appendLine("• نسبة الحيوية الحالية: ${String.format(java.util.Locale.US, "%.1f", livabilityPct)}% ($liveBirds طائر حي | إجمالي النافق: $totalMortality).")
            appendLine("• السجل العلاجي المسجل: $medsSummary.")
            appendLine("• التوصية لعمر $ageDays يوم: حافظ على جفاف الفرشة حول المساقي، وقدّم رافع مناعة (فيتامين AD3E + هـ سيلينيوم) بعد أي تحصين لرفع الاستجابة المناعية وتقليل الإجهاد.")
        }.trim()

        else -> buildString {
            appendLine("قراءة تحليلية شاملة لعنبر «$displayBarn» — اليوم $ageDays:")
            appendLine()
            appendLine("• الطيور الحية والحيوية: $liveBirds طائر حي من أصل $placedBirds (نسبة الحيوية ${String.format(java.util.Locale.US, "%.1f", livabilityPct)}%، إجمالي النافق $totalMortality).")
            appendLine("• العلف والمخزون: استهلك القطيع $qFormatted ق، والمتبقي في المخزن $remQFormatted ق (الوزن القياسي لعمر $ageDays يوم: ${String.format(java.util.Locale.US, "%.2f", targetWeightKg)} كغ).")
            appendLine("• المناخ والتهوية: $tempStatus")
            appendLine("• الحالة الصحية: $medsSummary.")
            if (baseServiceReply.isNotBlank()) {
                appendLine()
                appendLine(baseServiceReply)
            }
        }.trim()
    }
}

/**
 * Master Calligraphic Feather Quill (`🪶 Master Calligraphic Feather Quill`):
 * Custom multi-layered Bezier peacock/ostrich plume with curved rachis shaft, individual golden/bronze
 * feather barbs, and a split metallic calligraphic nib — zero primitive triangles!
 */
@Composable
fun InteractiveQuillCanvas(
    quillState: QuillActivityState,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "master_calligraphic_quill")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (quillState) {
                    QuillActivityState.WRITING -> 560
                    QuillActivityState.THINKING -> 1100
                    QuillActivityState.IDLE -> 2400
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "quill_phase"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val goldBright = Color(0xFFF5D061)
        val goldInk = Color(0xFFD4AF37)
        val warmAmber = Color(0xFFB47B26)
        val deepBronze = Color(0xFF5C3A21)
        val creamHighlight = Color(0xFFFFFBEB)

        val rotationDeg = when (quillState) {
            QuillActivityState.WRITING -> sin(phase) * 11f - 14f
            QuillActivityState.THINKING -> sin(phase) * 6f - 8f
            QuillActivityState.IDLE -> sin(phase) * 2f - 12f
        }
        val shiftX = when (quillState) {
            QuillActivityState.WRITING -> cos(phase * 2f) * (w * 0.07f)
            else -> 0f
        }
        val shiftY = when (quillState) {
            QuillActivityState.WRITING -> abs(sin(phase)) * (-h * 0.06f)
            QuillActivityState.THINKING -> sin(phase) * (h * 0.06f)
            QuillActivityState.IDLE -> sin(phase) * (h * 0.02f)
        }

        val nibX = w * 0.22f + shiftX
        val nibY = h * 0.84f + shiftY

        // Flowing golden ink trail & glowing droplets at the writing point
        if (quillState == QuillActivityState.WRITING || quillState == QuillActivityState.THINKING) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        goldInk.copy(alpha = 0.55f),
                        accentColor.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(nibX, nibY),
                    radius = w * 0.28f
                ),
                radius = w * 0.28f,
                center = Offset(nibX, nibY)
            )
            val wavePath = Path().apply {
                moveTo(w * 0.06f, h * 0.89f)
                quadraticBezierTo(
                    nibX - w * 0.06f,
                    h * 0.82f,
                    nibX,
                    nibY + h * 0.02f
                )
            }
            drawPath(
                path = wavePath,
                color = goldInk.copy(alpha = 0.78f),
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        rotate(degrees = rotationDeg, pivot = Offset(nibX, nibY)) {
            val crownX = nibX + w * 0.62f
            val crownY = nibY - h * 0.70f

            // 1. Upper/Left Lush Plume Vane (with realistic feather barb splits)
            val upperVane = Path().apply {
                moveTo(nibX + w * 0.10f, nibY - h * 0.14f)
                cubicTo(
                    nibX + w * 0.06f, nibY - h * 0.42f,
                    nibX + w * 0.22f, nibY - h * 0.64f,
                    crownX - w * 0.10f, crownY + h * 0.04f
                )
                // Barb notch 1
                lineTo(crownX - w * 0.16f, crownY + h * 0.12f)
                lineTo(crownX - w * 0.04f, crownY + h * 0.01f)
                cubicTo(
                    crownX + w * 0.03f, crownY - h * 0.03f,
                    crownX + w * 0.06f, crownY + h * 0.03f,
                    crownX, crownY + h * 0.08f
                )
                close()
            }
            drawPath(
                path = upperVane,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.92f),
                        goldBright,
                        warmAmber
                    ),
                    start = Offset(crownX, crownY),
                    end = Offset(nibX, nibY)
                )
            )

            // 2. Lower/Right Plush Plume Vane (with layered golden-bronze barbs)
            val lowerVane = Path().apply {
                moveTo(nibX + w * 0.10f, nibY - h * 0.14f)
                cubicTo(
                    nibX + w * 0.34f, nibY - h * 0.12f,
                    nibX + w * 0.54f, nibY - h * 0.30f,
                    crownX + w * 0.02f, crownY + h * 0.16f
                )
                // Delicate barb split on lower edge
                lineTo(crownX - w * 0.08f, crownY + h * 0.20f)
                lineTo(crownX, crownY + h * 0.08f)
                close()
            }
            drawPath(
                path = lowerVane,
                brush = Brush.linearGradient(
                    colors = listOf(
                        goldBright,
                        goldInk,
                        deepBronze
                    ),
                    start = Offset(crownX, crownY),
                    end = Offset(nibX, nibY)
                )
            )

            // 3. Individual Calligraphic Feather Barbs (delicate diagonal hairlines radiating from the rachis)
            for (i in 1..6) {
                val t = i / 7f
                val sx = nibX + (crownX - nibX) * t
                val sy = nibY + (crownY - nibY) * t - (sin(t * PI).toFloat() * h * 0.05f)
                // Upper barb hairline
                drawLine(
                    color = creamHighlight.copy(alpha = 0.55f),
                    start = Offset(sx, sy),
                    end = Offset(sx - w * 0.11f * (1f - t * 0.3f), sy - h * 0.10f * (1f - t * 0.3f)),
                    strokeWidth = 0.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Lower barb hairline
                drawLine(
                    color = deepBronze.copy(alpha = 0.45f),
                    start = Offset(sx, sy),
                    end = Offset(sx + w * 0.12f * (1f - t * 0.35f), sy - h * 0.02f),
                    strokeWidth = 0.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 4. Curved Rachis (Central Feather Spine) & Metallic Calligraphic Nib
            val rachisPath = Path().apply {
                moveTo(nibX, nibY)
                quadraticBezierTo(
                    nibX + w * 0.26f,
                    nibY - h * 0.40f,
                    crownX,
                    crownY + h * 0.04f
                )
            }
            drawPath(
                path = rachisPath,
                color = creamHighlight,
                style = Stroke(width = 1.9.dp.toPx(), cap = StrokeCap.Round)
            )
            drawPath(
                path = rachisPath,
                color = goldInk,
                style = Stroke(width = 1.1.dp.toPx(), cap = StrokeCap.Round)
            )

            // 5. Split Metallic Calligraphic Nib at the tip
            drawCircle(
                color = goldBright,
                radius = 1.8.dp.toPx(),
                center = Offset(nibX, nibY)
            )
        }
    }
}

@Composable
fun AgritechAiVetInteractiveChatSection(
    selectedFarm: FarmModel?,
    currentTheme: AppThemeOption,
    lang: AppLanguage,
    diagnosisHistory: List<Pair<String, String>>,
    onUpdateDiagnosisHistory: (List<Pair<String, String>>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appearance = LocalAppAppearanceState.current
    val isDark = currentTheme.isDark
    val activeAccent = appearance.accentColor
    val primaryButtonColor = appearance.accentOption.primaryColor
    val cornerRadius = appearance.cornerRadiusDp.dp
    val pillShape = RoundedCornerShape((appearance.cornerRadiusDp - 4).coerceAtLeast(8).dp)
    val coroutineScope = rememberCoroutineScope()

    var savedApiKey by remember { mutableStateOf(PersonalGeminiKeyStore.getSavedKey(context)) }
    var draftApiKey by remember(savedApiKey) { mutableStateOf(savedApiKey) }
    var showApiKeySetupDialog by remember { mutableStateOf(false) }
    var showApiKeyChars by remember { mutableStateOf(false) }
    var keyStatusBanner by remember { mutableStateOf<String?>(null) }

    var inputPrompt by remember { mutableStateOf("") }
    var quillState by remember { mutableStateOf(QuillActivityState.IDLE) }
    var activeStreamingQuestion by remember { mutableStateOf("") }
    var activeStreamingAnswer by remember { mutableStateOf("") }

    // Live Barn Metrics Computation from AgritechMockDb
    val activeCycle = remember(selectedFarm?.id, AgritechMockDb.cycles.size) {
        selectedFarm?.let { AgritechMockDb.resolveCycleForFarm(it) }
    }
    val activeReports = remember(selectedFarm?.id, activeCycle?.id, AgritechMockDb.dailyReports.size) {
        if (selectedFarm != null && activeCycle != null) {
            AgritechMockDb.resolveReportsForFarmAndCycle(selectedFarm.id, activeCycle.id, emptyList())
        } else {
            emptyList()
        }
    }

    val barnDisplayName = remember(selectedFarm, lang) {
        selectedFarm?.let { Translations.getLocalizedFarmName(it.id, lang, it.name) }.orEmpty()
    }
    val realAgeDays = remember(activeCycle, activeReports) {
        if (activeCycle != null) {
            val maxDay = activeReports.maxOfOrNull { it.dayNumber } ?: 1
            PoultryCalculators.calculateCycleDay(activeCycle.startDate, maxDay)
        } else {
            1
        }
    }
    val placedBirds = remember(activeCycle, selectedFarm) {
        activeCycle?.totalBirdsPlaced ?: selectedFarm?.capacity ?: 0
    }
    val realTotalMortality = remember(activeReports) { activeReports.sumOf { it.mortalityCount } }
    val realLiveBirds = remember(activeCycle, realTotalMortality, placedBirds) {
        if (activeCycle != null) {
            PoultryCalculators.calculateLiveBirds(
                totalBirdsPlaced = activeCycle.totalBirdsPlaced,
                totalMortality = realTotalMortality,
                cumulativeBirdsSold = activeCycle.cumulativeBirdsSold
            )
        } else {
            placedBirds
        }
    }
    val realMortalityPct = remember(placedBirds, realTotalMortality) {
        if (placedBirds > 0) {
            (realTotalMortality.toDouble() / placedBirds) * 100.0
        } else {
            0.0
        }
    }
    val realLivabilityPct = (100.0 - realMortalityPct).coerceIn(0.0, 100.0)
    val latestRecordedTempC = remember(activeReports) { activeReports.maxByOrNull { it.dayNumber }?.temperature }
    val latestWaterLiters = remember(activeReports) { activeReports.maxByOrNull { it.dayNumber }?.waterIntakeLiters ?: 0.0 }
    val totalFeedConsumedQuintals = remember(activeReports) { activeReports.sumOf { it.feedIntakeKg } / 100.0 }
    val remainingFeedQuintals = remember(activeCycle) { (activeCycle?.feedInventoryKg ?: 0.0) / 100.0 }
    val medsSummaryText = remember(activeReports) {
        activeReports.filter { it.medicineGiven || it.medicineSummary.isNotBlank() }
            .takeLast(4)
            .joinToString(" | ") { "يوم ${it.dayNumber}: ${it.medicineSummary}" }
            .ifBlank { "برنامج وقائي مستقر بدون علاجات طارئة" }
    }

    val barnMetricsSummary = remember(
        barnDisplayName,
        realAgeDays,
        realLiveBirds,
        realTotalMortality,
        realLivabilityPct,
        totalFeedConsumedQuintals,
        remainingFeedQuintals,
        latestRecordedTempC,
        latestWaterLiters,
        medsSummaryText
    ) {
        buildString {
            appendLine("• اسم العنبر: ${barnDisplayName.ifBlank { "العنبر النشط" }}")
            appendLine("• عمر الدورة الحالي: اليوم $realAgeDays")
            appendLine("• عدد الطيور الحية: $realLiveBirds طير (الحيوية: ${String.format(java.util.Locale.US, "%.1f", realLivabilityPct)}% | النافق التراكمي: $realTotalMortality)")
            appendLine("• العلف المستهلك: ${String.format(java.util.Locale.US, "%.2f", totalFeedConsumedQuintals)} ق (المتبقي بالمخزن: ${String.format(java.util.Locale.US, "%.2f", remainingFeedQuintals)} ق)")
            appendLine("• آخر حرارة مسجلة: ${latestRecordedTempC?.let { "${it}°C" } ?: "غير مسجلة"} | استهلاك الماء اليومي: ${latestWaterLiters.toInt()} لتر")
            appendLine("• سجل الأدوية واللقاحات: $medsSummaryText")
        }
    }

    fun sendQuestionWithQuill(rawQuestion: String) {
        val cleanQ = rawQuestion.trim().replace("**", "")
        if (cleanQ.isBlank() || quillState != QuillActivityState.IDLE) return

        inputPrompt = ""
        activeStreamingQuestion = cleanQ
        activeStreamingAnswer = ""
        quillState = QuillActivityState.THINKING

        coroutineScope.launch {
            val currentKey = savedApiKey.trim()
            val systemPrompt = """
                أنت الخبير البيطري والمهندس الزراعي الذكي في تطبيق "IQ Notebook — دفتر المزرعة الذكي" لدواجن التسمين.
                أجب بأسلوب محادثة طبيعي وودود ومنظم مثل Gemini، مع الاستناد إلى بيانات العنبر الحية التالية:
                $barnMetricsSummary
                التزم دائماً بوحدة القنطار (ق = 100 كغ) للعلف، ولا تستخدم كلمة ديون بل المصاريف.
            """.trimIndent()

            val barnContext = BarnAiContext(
                farmName = barnDisplayName.ifBlank { null },
                cycleAgeDays = realAgeDays,
                liveBirdsCount = realLiveBirds,
                latestTemperatureC = latestRecordedTempC,
                cumulativeMortalityPct = realMortalityPct,
                feedInventoryKg = activeCycle?.feedInventoryKg
            )

            val finalAnswerText: String
            if (currentKey.isNotBlank()) {
                val liveResult = callPersonalGeminiHttpApi(
                    apiKey = currentKey,
                    systemInstruction = systemPrompt,
                    userPrompt = cleanQ
                )
                finalAnswerText = if (liveResult.succeeded && liveResult.text.isNotBlank()) {
                    liveResult.text.replace("**", "")
                } else {
                    // Seamless, silent transition to the intelligent conversational barn engine (ZERO technical error banners!)
                    buildConversationalBarnReply(
                        question = cleanQ,
                        lang = lang,
                        barnName = barnDisplayName,
                        ageDays = realAgeDays,
                        placedBirds = placedBirds,
                        liveBirds = realLiveBirds,
                        totalMortality = realTotalMortality,
                        livabilityPct = realLivabilityPct,
                        totalFeedQuintals = totalFeedConsumedQuintals,
                        remainingFeedQuintals = remainingFeedQuintals,
                        latestTempC = latestRecordedTempC,
                        latestWaterLiters = latestWaterLiters,
                        medsSummary = medsSummaryText,
                        barnContext = barnContext
                    )
                }
            } else {
                delay(320)
                finalAnswerText = buildConversationalBarnReply(
                    question = cleanQ,
                    lang = lang,
                    barnName = barnDisplayName,
                    ageDays = realAgeDays,
                    placedBirds = placedBirds,
                    liveBirds = realLiveBirds,
                    totalMortality = realTotalMortality,
                    livabilityPct = realLivabilityPct,
                    totalFeedQuintals = totalFeedConsumedQuintals,
                    remainingFeedQuintals = remainingFeedQuintals,
                    latestTempC = latestRecordedTempC,
                    latestWaterLiters = latestWaterLiters,
                    medsSummary = medsSummaryText,
                    barnContext = barnContext
                )
            }

            // Progressive Calligraphic Feather Quill Writing Animation in open space
            quillState = QuillActivityState.WRITING
            val totalLen = finalAnswerText.length
            val chunkStep = when {
                totalLen > 600 -> 18
                totalLen > 300 -> 12
                else -> 7
            }
            var cursor = 0
            while (cursor < totalLen) {
                cursor = (cursor + chunkStep).coerceAtMost(totalLen)
                activeStreamingAnswer = finalAnswerText.substring(0, cursor)
                delay(18)
            }

            // Persist permanently in diagnosisHistory so the response NEVER disappears!
            onUpdateDiagnosisHistory(diagnosisHistory + Pair(cleanQ, finalAnswerText))
            activeStreamingQuestion = ""
            activeStreamingAnswer = ""
            quillState = QuillActivityState.IDLE
        }
    }

    // Filter out the old static welcome card so the open Gemini chat starts clean and spacious
    val welcomeTitle = Translations.tr("ai_vet_welcome_card_title", lang)
    val userConversationTurns = remember(diagnosisHistory, welcomeTitle) {
        diagnosisHistory.filterNot { (title, _) ->
            title.trim() == welcomeTitle.trim()
        }
    }

    // Optional Modal Dialog for Personal Gemini API Key Setup (opened only when tapping the top capsule button)
    if (showApiKeySetupDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeySetupDialog = false },
            shape = RoundedCornerShape(cornerRadius),
            containerColor = currentTheme.dialogCardColor,
            modifier = Modifier
                .darwinManuscriptBorder(cornerRadius = cornerRadius, isDark = isDark)
                .testTag("dialog_personal_gemini_key"),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Tactile3DIconBadge(
                        emoji = "🔑",
                        tint = activeAccent,
                        boxSize = 38.dp,
                        cornerRadius = 12.dp,
                        emojiFontSize = 18.sp,
                        isDark = isDark
                    )
                    Column {
                        Text(
                            text = Translations.tr("ai_vet_key_setup_badge", lang),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = currentTheme.headingColor
                        )
                        Text(
                            text = if (savedApiKey.isNotBlank()) {
                                "متصل ✓ (${savedApiKey.take(6)}••••)"
                            } else {
                                "Google AI Studio • Gemini 2.5 Flash"
                            },
                            fontSize = 11.sp,
                            color = activeAccent
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "1️⃣ اضغط على الزر أدناه لفتح منصة Google AI Studio الرسمية.\n" +
                            "2️⃣ سجل الدخول بحساب Google واضغط على Create API key.\n" +
                            "3️⃣ انسخ المفتاح والصقه في الحقل بالأسفل.",
                        fontSize = 11.5.sp,
                        color = currentTheme.headingColor,
                        lineHeight = 17.sp
                    )

                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://aistudio.google.com/apikey")
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_open_google_ai_studio_apikey"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.2.dp, activeAccent),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = activeAccent)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🌐 احصل على مفتاح Gemini API المجاني",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    LuminousOutlinedTextField(
                        value = draftApiKey,
                        onValueChange = {
                            draftApiKey = it
                            keyStatusBanner = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_personal_gemini_api_key"),
                        singleLine = true,
                        label = {
                            Text(
                                text = "الصق مفتاح Gemini API هنا",
                                fontSize = 11.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = activeAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showApiKeyChars = !showApiKeyChars }) {
                                Icon(
                                    imageVector = if (showApiKeyChars) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "إظهار أو إخفاء المفتاح",
                                    tint = currentTheme.secondaryTextColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        visualTransformation = if (showApiKeyChars) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        }
                    )

                    if (savedApiKey.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFDC2626).copy(alpha = 0.12f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    PersonalGeminiKeyStore.clearKey(context)
                                    savedApiKey = ""
                                    draftApiKey = ""
                                    keyStatusBanner = "تم مسح المفتاح المحفوظ."
                                }
                                .testTag("btn_clear_personal_gemini_key")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🗑️ مسح المفتاح المحفوظ",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }

                    if (!keyStatusBanner.isNullOrBlank()) {
                        Text(
                            text = keyStatusBanner!!,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeAccent
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = draftApiKey.trim()
                        if (trimmed.isNotBlank()) {
                            PersonalGeminiKeyStore.saveKey(context, trimmed)
                            savedApiKey = trimmed
                            showApiKeySetupDialog = false
                        } else {
                            keyStatusBanner = "يرجى لصق المفتاح أولاً."
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryButtonColor),
                    modifier = Modifier.testTag("btn_save_personal_gemini_key")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "حفظ وتفعيل 🪶",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeySetupDialog = false }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = currentTheme.secondaryTextColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = Translations.tr("btn_cancel", lang),
                        fontSize = 12.sp,
                        color = currentTheme.secondaryTextColor
                    )
                }
            }
        )
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Top Compact Bar: Barn Live Context Badge + Sleek Capsule Button `🔑 مفتاح Gemini API (متصل ✓)`
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                InteractiveQuillCanvas(
                    quillState = quillState,
                    accentColor = activeAccent,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = if (barnDisplayName.isNotBlank()) {
                        "$barnDisplayName • ${Translations.tr("day_prefix", lang)} $realAgeDays ($realLiveBirds 🐥)"
                    } else {
                        Translations.tr("ai_vet_open_chat_hint", lang)
                    },
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = currentTheme.secondaryTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = if (savedApiKey.isNotBlank()) appearance.accentContainerColor else currentTheme.cardColor,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (savedApiKey.isNotBlank()) activeAccent else activeAccent.copy(alpha = 0.45f)
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        draftApiKey = savedApiKey
                        keyStatusBanner = null
                        showApiKeySetupDialog = true
                    }
                    .testTag("btn_open_gemini_key_dialog")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (savedApiKey.isNotBlank()) {
                            Translations.tr("ai_vet_key_connected_badge", lang)
                        } else {
                            Translations.tr("ai_vet_key_setup_badge", lang)
                        },
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (savedApiKey.isNotBlank()) activeAccent else currentTheme.headingColor
                    )
                }
            }
        }

        // 2. Open Gemini-Style Conversation Area (`ai_vet_messages_list`) — Zero cramped nested boxes!
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("ai_vet_messages_list"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (userConversationTurns.isEmpty() && quillState == QuillActivityState.IDLE) {
                // Clean, breathable Gemini-style greeting in open space when no messages have been sent yet
                item(key = "open_gemini_greeting_space") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        InteractiveQuillCanvas(
                            quillState = QuillActivityState.IDLE,
                            accentColor = activeAccent,
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = Translations.tr("ai_vet_open_chat_hint", lang),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = currentTheme.headingColor
                        )
                        Text(
                            text = if (barnDisplayName.isNotBlank()) {
                                "العنبر النشط: $barnDisplayName • اليوم $realAgeDays • الحيوية ${String.format(java.util.Locale.US, "%.1f", realLivabilityPct)}% • المخزون ${Translations.formatQuintalNumber(remainingFeedQuintals)} ق"
                            } else {
                                Translations.tr("ai_vet_chat_disclaimer", lang)
                            },
                            fontSize = 11.5.sp,
                            color = currentTheme.secondaryTextColor
                        )
                    }
                }
            }

            // Render each conversation turn in open Gemini layout:
            // - User question in a sleek bubble on the start/right (`Alignment.End`)
            // - AI Vet response written directly in the open space below it!
            itemsIndexed(userConversationTurns) { turnIdx, (rawTitle, rawBody) ->
                val cleanQuestion = rawTitle
                    .replace("**", "")
                    .removePrefix("🪶 ")
                    .removePrefix("${Translations.tr("ai_vet_diagnosis_prefix", lang)} (")
                    .removePrefix("${Translations.tr("ai_vet_user_query_prefix", lang)}: ")
                    .removeSuffix(")")
                    .trim()
                val cleanAnswer = rawBody.replace("**", "").trim()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .testTag("ai_vet_turn_$turnIdx"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // User Question Bubble (Sleek capsule aligned to End/Right)
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 4.dp,
                                bottomStart = 18.dp,
                                bottomEnd = 18.dp
                            ),
                            color = appearance.accentContainerColor,
                            border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.40f)),
                            modifier = Modifier.widthIn(max = 310.dp)
                        ) {
                            Text(
                                text = cleanQuestion,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentTheme.headingColor,
                                lineHeight = 19.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }

                    // AI Vet Answer Written Directly in Open Space (Gemini-style, zero nested boxes!)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "🪶",
                                fontSize = 14.sp
                            )
                            Text(
                                text = Translations.tr("ai_vet_diagnosis_prefix", lang),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = activeAccent
                            )
                        }
                        val aiAnswerInkColor = if (currentTheme.isDark) Color(0xFFF8FAFC) else Color(0xFF111827)
                        Text(
                            text = cleanAnswer,
                            fontSize = 13.5.sp,
                            color = aiAnswerInkColor,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    HorizontalDivider(
                        color = activeAccent.copy(alpha = 0.14f),
                        thickness = 0.8.dp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Active Streaming Turn: Master Calligraphic Feather Quill gliding at the end of the line in open space!
            if (quillState != QuillActivityState.IDLE) {
                item(key = "active_quill_open_streaming_turn") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // User Question Bubble
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 18.dp,
                                    topEnd = 4.dp,
                                    bottomStart = 18.dp,
                                    bottomEnd = 18.dp
                                ),
                                color = appearance.accentContainerColor,
                                border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.45f)),
                                modifier = Modifier.widthIn(max = 310.dp)
                            ) {
                                Text(
                                    text = activeStreamingQuestion,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = currentTheme.headingColor,
                                    lineHeight = 19.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                )
                            }
                        }

                        // Open Space AI Response + Gliding Calligraphic Feather Quill
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                InteractiveQuillCanvas(
                                    quillState = quillState,
                                    accentColor = activeAccent,
                                    modifier = Modifier.size(34.dp)
                                )
                                Text(
                                    text = if (quillState == QuillActivityState.THINKING) {
                                        "الريشة تراجع مؤشرات $barnDisplayName..."
                                    } else {
                                        "الريشة تخطّ الإجابة البيطرية..."
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = activeAccent
                                )
                            }

                            if (activeStreamingAnswer.isNotBlank()) {
                                val aiAnswerInkColor = if (currentTheme.isDark) Color(0xFFF8FAFC) else Color(0xFF111827)
                                Text(
                                    text = activeStreamingAnswer,
                                    fontSize = 13.5.sp,
                                    lineHeight = 22.sp,
                                    color = aiAnswerInkColor,
                                    fontWeight = FontWeight.Normal,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                // Calligraphic Feather Quill gliding smoothly at the end of the written line
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    InteractiveQuillCanvas(
                                        quillState = QuillActivityState.WRITING,
                                        accentColor = activeAccent,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Horizontal Scrollable Quick Suggestion Strip (`LazyRow`) right above the input field
        // Automatically hides once the conversation starts!
        AnimatedVisibility(visible = userConversationTurns.isEmpty() && quillState == QuillActivityState.IDLE) {
            val quickPrompts = remember(barnDisplayName, realAgeDays) {
                val bLabel = barnDisplayName.ifBlank { "العنبر" }
                listOf(
                    "📊 تحليل شامل لأداء $bLabel اليوم",
                    "🌡️ ضبط الحرارة والتهوية لليوم $realAgeDays",
                    "🌾 تقييم العلف بالقنطار ومعامل التحويل FCR",
                    "💊 برنامج التحصين والوقاية وتقليل النافق"
                )
            }
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(quickPrompts) { idx, chipText ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = currentTheme.cardColor,
                        border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.40f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(enabled = quillState == QuillActivityState.IDLE) {
                                sendQuestionWithQuill(chipText)
                            }
                            .testTag("ai_vet_quick_chip_$idx")
                    ) {
                        Text(
                            text = chipText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.headingColor,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        // 4. Clean Bottom Input Bar (`input_global_ai_vet_prompt` & `btn_send_global_ai_vet`) — Zero triangle inside the field!
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LuminousOutlinedTextField(
                value = inputPrompt,
                onValueChange = { inputPrompt = it },
                placeholder = {
                    Text(
                        text = Translations.tr("ai_vet_input_placeholder", lang),
                        fontSize = 12.sp,
                        color = currentTheme.secondaryTextColor.copy(alpha = 0.75f)
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_global_ai_vet_prompt")
            )

            Button(
                onClick = { sendQuestionWithQuill(inputPrompt) },
                enabled = quillState == QuillActivityState.IDLE && inputPrompt.isNotBlank(),
                shape = pillShape,
                colors = ButtonDefaults.buttonColors(containerColor = primaryButtonColor),
                modifier = Modifier
                    .height(52.dp)
                    .testTag("btn_send_global_ai_vet")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAiVetBottomSheet(
    selectedFarm: FarmModel?,
    currentTheme: AppThemeOption,
    lang: AppLanguage,
    onDismiss: () -> Unit
) {
    val appearance = LocalAppAppearanceState.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetCorner = appearance.cornerRadiusDp.dp
    val isDark = currentTheme.isDark
    val activeAccent = appearance.accentColor
    val primaryText = if (isDark) Color(0xFFF8FAFC) else Color(0xFF111827)
    val secondaryText = currentTheme.secondaryTextColor
    var quickSheetDiagnosisHistory by remember {
        mutableStateOf<List<Pair<String, String>>>(emptyList())
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC),
        scrimColor = Color.Black.copy(alpha = 0.70f),
        shape = RoundedCornerShape(topStart = sheetCorner, topEnd = sheetCorner),
        modifier = Modifier.testTag("quick_ai_vet_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.86f)
                .darwinPaperBackground(currentTheme)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Header: 🤖 المساعد البيطري الذكي السريع (IQ AI)
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
                    Tactile3DIconBadge(
                        emoji = "🤖",
                        tint = activeAccent,
                        boxSize = 40.dp,
                        cornerRadius = 12.dp,
                        isDark = isDark
                    )
                    Column {
                        Text(
                            text = Translations.tr("quick_ai_vet_sheet_title", lang),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.5.sp,
                            color = primaryText
                        )
                        Text(
                            text = Translations.tr("quick_ai_vet_sheet_subtitle", lang),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = secondaryText
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("btn_close_quick_ai_vet_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = Translations.tr("btn_close", lang),
                        tint = primaryText
                    )
                }
            }

            HorizontalDivider(color = activeAccent.copy(alpha = 0.22f))

            AgritechAiVetInteractiveChatSection(
                selectedFarm = selectedFarm,
                currentTheme = currentTheme,
                lang = lang,
                diagnosisHistory = quickSheetDiagnosisHistory,
                onUpdateDiagnosisHistory = { quickSheetDiagnosisHistory = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}


