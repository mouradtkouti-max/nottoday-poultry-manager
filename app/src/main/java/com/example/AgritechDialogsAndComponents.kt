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
// AGRITECH DIALOGS, DARWIN PARCHMENT MODIFIERS & SHARED UI COMPONENTS
// ============================================================================

// ============================================================================
// PREMIUM NEUMORPHISM & 3D TACTILE UI DESIGN SYSTEM
// ============================================================================

enum class PremiumPillVariant {
    PRIMARY,
    SALES_INCOME,
    DELETE_RESET,
    LOGIN_GRADIENT
}

/**
 * Multi-layer paper background modifier driven by `AppPaperStyleRuntime.activePaperStyle`:
 * - DARWIN_PARCHMENT (Day): #F4EBD6 center -> #E7D6B5 edges with Soft Sepia Vignette #D2B892
 * - DARWIN_PARCHMENT (Night): #211913 center -> #18120E edges
 * - RULED_LEDGER: subtle horizontal ledger lines
 * - BLUEPRINT_GRID: subtle engineering grid
 * - CLEAN_DEFAULT: clean solid background
 */
fun Modifier.darwinPaperBackground(
    theme: AppThemeOption,
    paperStyle: AppPaperStyleOption = AppPaperStyleRuntime.activePaperStyle
): Modifier = this.drawWithContent {
    val isDark = theme.isDark
    when (paperStyle) {
        AppPaperStyleOption.DARWIN_PARCHMENT -> {
            val radius = maxOf(size.width, size.height) * 0.78f
            val brush = if (isDark) {
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF211913),
                        Color(0xFF1C1510),
                        Color(0xFF18120E)
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.42f),
                    radius = radius.coerceAtLeast(1f)
                )
            } else {
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFF4EBD6),
                        Color(0xFFE7D6B5),
                        Color(0xFFD2B892)
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.42f),
                    radius = radius.coerceAtLeast(1f)
                )
            }
            drawRect(brush = brush)
        }
        AppPaperStyleOption.RULED_LEDGER -> {
            drawRect(color = theme.bgColor)
            val lineColor = if (isDark) Color(0xFF334155).copy(alpha = 0.35f) else Color(0xFFCBD5E1).copy(alpha = 0.55f)
            val stepPx = 28.dp.toPx()
            var y = stepPx
            while (y < size.height) {
                drawLine(
                    color = lineColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
                y += stepPx
            }
        }
        AppPaperStyleOption.BLUEPRINT_GRID -> {
            drawRect(color = theme.bgColor)
            val gridColor = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.14f) else Color(0xFF0284C7).copy(alpha = 0.12f)
            val stepPx = 24.dp.toPx()
            var x = stepPx
            while (x < size.width) {
                drawLine(color = gridColor, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 1.dp.toPx())
                x += stepPx
            }
            var y = stepPx
            while (y < size.height) {
                drawLine(color = gridColor, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1.dp.toPx())
                y += stepPx
            }
        }
        AppPaperStyleOption.CLEAN_DEFAULT -> {
            drawRect(color = theme.bgColor)
        }
    }
    drawContent()
}

fun Modifier.darwinPaperBackground(
    appearance: AppAppearanceState,
    isDark: Boolean = appearance.isDark
): Modifier = this.darwinPaperBackground(
    theme = if (isDark && !appearance.theme.isDark) AppThemeOption.NIGHT_MODE else appearance.theme,
    paperStyle = appearance.paperStyle
)

/**
 * Curved Double-Line Manuscript Border following `appearance.cornerRadiusDp.dp`:
 * - Outer line: 1.4.dp (#4A321F in Day mode, #C59B27 in Night mode)
 * - Inner parallel line: 0.7.dp inset by 3.dp (#8C6D46 in Day mode, #8B6914 in Night mode)
 */
fun Modifier.darwinManuscriptBorder(
    appearance: AppAppearanceState,
    isDark: Boolean = appearance.isDark
): Modifier = this.darwinManuscriptBorder(
    cornerRadius = appearance.cornerRadiusDp.dp,
    isDark = isDark,
    paperStyle = appearance.paperStyle
)

fun Modifier.darwinManuscriptBorder(
    cornerRadius: androidx.compose.ui.unit.Dp,
    isDark: Boolean = false,
    paperStyle: AppPaperStyleOption = AppPaperStyleRuntime.activePaperStyle
): Modifier {
    if (paperStyle != AppPaperStyleOption.DARWIN_PARCHMENT) return this
    return this.drawWithContent {
        drawContent()
        val outerStrokePx = 1.4.dp.toPx()
        val innerStrokePx = 0.7.dp.toPx()
        val insetPx = 3.dp.toPx()
        val outerRadiusPx = cornerRadius.toPx()
        val innerRadiusPx = (outerRadiusPx - insetPx).coerceAtLeast(2.dp.toPx())
        val outerColor = if (isDark) Color(0xFFC59B27) else Color(0xFF4A321F)
        val innerColor = if (isDark) Color(0xFF8B6914) else Color(0xFF8C6D46)

        drawRoundRect(
            color = outerColor,
            topLeft = Offset(outerStrokePx / 2f, outerStrokePx / 2f),
            size = androidx.compose.ui.geometry.Size(
                (size.width - outerStrokePx).coerceAtLeast(0f),
                (size.height - outerStrokePx).coerceAtLeast(0f)
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(outerRadiusPx, outerRadiusPx),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = outerStrokePx)
        )
        if (size.width > insetPx * 2 && size.height > insetPx * 2) {
            drawRoundRect(
                color = innerColor,
                topLeft = Offset(insetPx, insetPx),
                size = androidx.compose.ui.geometry.Size(
                    size.width - insetPx * 2f,
                    size.height - insetPx * 2f
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(innerRadiusPx, innerRadiusPx),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = innerStrokePx)
            )
        }
    }
}

/**
 * Outer Luminous Halo + Inner Ambient Illumination for focused inputs (`isFocused == true`),
 * respecting `appearance.cornerRadiusDp.dp` without any inner orange line or harsh black border.
 */
fun Modifier.luminousFocusGlow(
    isFocused: Boolean,
    cornerRadius: androidx.compose.ui.unit.Dp,
    isDark: Boolean = false
): Modifier = this.drawWithContent {
    val radiusPx = cornerRadius.toPx()
    if (isFocused) {
        val ambientFill = if (isDark) {
            Color(0xFFF59E0B).copy(alpha = 0.14f)
        } else {
            Color(0xFFFDE68A).copy(alpha = 0.32f)
        }
        drawRoundRect(
            color = ambientFill,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusPx, radiusPx)
        )
    }
    drawContent()
    val outerStrokeColor = if (isFocused) {
        if (isDark) Color(0xFFFBBF24).copy(alpha = 0.85f) else Color(0xFFD97706).copy(alpha = 0.75f)
    } else {
        if (AppPaperStyleRuntime.activePaperStyle == AppPaperStyleOption.DARWIN_PARCHMENT) {
            if (isDark) Color(0xFF8B6914).copy(alpha = 0.65f) else Color(0xFF8C6D46).copy(alpha = 0.65f)
        } else {
            if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
        }
    }
    val strokeWidthPx = if (isFocused) 1.8.dp.toPx() else 1.dp.toPx()
    drawRoundRect(
        color = outerStrokeColor,
        topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f),
        size = androidx.compose.ui.geometry.Size(
            (size.width - strokeWidthPx).coerceAtLeast(0f),
            (size.height - strokeWidthPx).coerceAtLeast(0f)
        ),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusPx, radiusPx),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx)
    )
    if (isFocused) {
        val haloInset = 2.2.dp.toPx()
        val haloColor = if (isDark) Color(0xFFFDE68A).copy(alpha = 0.35f) else Color(0xFFF59E0B).copy(alpha = 0.28f)
        drawRoundRect(
            color = haloColor,
            topLeft = Offset(haloInset, haloInset),
            size = androidx.compose.ui.geometry.Size(
                (size.width - haloInset * 2f).coerceAtLeast(0f),
                (size.height - haloInset * 2f).coerceAtLeast(0f)
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                (radiusPx - haloInset).coerceAtLeast(2f),
                (radiusPx - haloInset).coerceAtLeast(2f)
            ),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2.dp.toPx())
        )
    }
}

/**
 * Universal 3D Keycap & Tactile Embossed UI modifier (`Modifier.tactile3DStyle(baseColor, cornerRadius)`):
 * Gives buttons, icons, badges, and fields:
 * - Soft deep 3D shadow (`shadow(elevation = 6.dp)`)
 * - Top specular highlight (`1.dp` white alpha `0.4f` border)
 * - Raised 3D Bottom Bevel Lip (`3.dp` darker bottom edge)
 * - When Darwin Parchment is active, frames with a fine double border (#3A2415 / #D4AF37).
 */
fun Modifier.tactile3DStyle(
    baseColor: Color,
    cornerRadius: androidx.compose.ui.unit.Dp = 14.dp,
    elevation: androidx.compose.ui.unit.Dp = 6.dp
): Modifier {
    val isDarwin = AppPaperStyleRuntime.activePaperStyle == AppPaperStyleOption.DARWIN_PARCHMENT
    val shape = RoundedCornerShape(cornerRadius)
    val darkLipColor = if (isDarwin) {
        Color(0xFF3A2415)
    } else {
        Color(
            red = (baseColor.red * 0.62f).coerceIn(0f, 1f),
            green = (baseColor.green * 0.62f).coerceIn(0f, 1f),
            blue = (baseColor.blue * 0.68f).coerceIn(0f, 1f),
            alpha = 0.95f
        )
    }
    val bevelBorderBrush = if (isDarwin) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFD4AF37).copy(alpha = 0.85f),
                Color(0xFF3A2415)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.45f),
                darkLipColor
            )
        )
    }
    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            spotColor = darkLipColor,
            ambientColor = darkLipColor
        )
        .clip(shape)
        .border(
            width = 1.dp,
            color = if (isDarwin) Color(0xFF3A2415) else Color.White.copy(alpha = 0.4f),
            shape = shape
        )
        .border(width = 1.5.dp, brush = bevelBorderBrush, shape = shape)
        .drawWithContent {
            drawContent()
            val lipPx = 3.dp.toPx()
            drawRect(
                color = darkLipColor.copy(alpha = 0.55f),
                topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - lipPx),
                size = androidx.compose.ui.geometry.Size(size.width, lipPx)
            )
            if (isDarwin) {
                val insetPx = 2.5.dp.toPx()
                val rPx = (cornerRadius.toPx() - insetPx).coerceAtLeast(2f)
                drawRoundRect(
                    color = Color(0xFFD4AF37).copy(alpha = 0.55f),
                    topLeft = Offset(insetPx, insetPx),
                    size = androidx.compose.ui.geometry.Size(
                        (size.width - insetPx * 2f).coerceAtLeast(0f),
                        (size.height - insetPx * 2f).coerceAtLeast(0f)
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(rPx, rPx),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.7.dp.toPx())
                )
            }
        }
}

@Composable
fun PremiumPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: PremiumPillVariant = PremiumPillVariant.PRIMARY,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    fontSize: TextUnit = 13.5.sp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
) {
    val appearance = LocalAppAppearanceState.current
    val isDarwin = appearance.paperStyle == AppPaperStyleOption.DARWIN_PARCHMENT
    val pillCornerDp = appearance.cornerRadiusDp.dp
    val pillShape = RoundedCornerShape(pillCornerDp)
    val basePrimary = appearance.accentOption.primaryColor
    val deepPrimary = Color(
        red = (basePrimary.red * 0.72f).coerceIn(0f, 1f),
        green = (basePrimary.green * 0.72f).coerceIn(0f, 1f),
        blue = (basePrimary.blue * 0.78f).coerceIn(0f, 1f),
        alpha = 1f
    )
    val gradientColors = when (variant) {
        PremiumPillVariant.PRIMARY -> if (isDarwin && appearance.accentOption == AppAccentColorOption.DARWIN_BRONZE) {
            listOf(
                Color(0xFF8B5A2B),
                Color(0xFF6E4720),
                Color(0xFF4A2E12)
            )
        } else {
            listOf(
                appearance.accentOption.darkModeColor,
                basePrimary,
                deepPrimary
            )
        }
        PremiumPillVariant.SALES_INCOME -> if (isDarwin) {
            listOf(
                Color(0xFF3E5E2E),
                Color(0xFF2E4722)
            )
        } else {
            listOf(
                Color(0xFF10B981),
                Color(0xFF059669)
            )
        }
        PremiumPillVariant.DELETE_RESET -> if (isDarwin) {
            listOf(
                Color(0xFF8B2E22),
                Color(0xFF6E2219)
            )
        } else {
            listOf(
                Color(0xFFFF4B72),
                Color(0xFFE11D48)
            )
        }
        PremiumPillVariant.LOGIN_GRADIENT -> if (isDarwin) {
            listOf(
                Color(0xFF8B5A2B),
                Color(0xFF6E4720)
            )
        } else {
            listOf(
                Color(0xFF43C6FF),
                Color(0xFF6B46FF)
            )
        }
    }
    val spotShadowColor = when (variant) {
        PremiumPillVariant.PRIMARY -> basePrimary
        PremiumPillVariant.SALES_INCOME -> if (isDarwin) Color(0xFF2E4722) else Color(0xFF10B981)
        PremiumPillVariant.DELETE_RESET -> if (isDarwin) Color(0xFF6E2219) else Color(0xFFE11D48)
        PremiumPillVariant.LOGIN_GRADIENT -> if (isDarwin) Color(0xFF6E4720) else Color(0xFF6B46FF)
    }
    val activeBrush = if (enabled) {
        Brush.verticalGradient(colors = gradientColors)
    } else {
        Brush.verticalGradient(colors = listOf(Color(0xFF94A3B8), Color(0xFF64748B)))
    }

    Box(
        modifier = modifier
            .tactile3DStyle(
                baseColor = if (enabled) spotShadowColor else Color(0xFF64748B),
                cornerRadius = pillCornerDp,
                elevation = if (enabled) 6.dp else 0.dp
            )
            .background(brush = activeBrush, shape = pillShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = fontSize,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Kinetic "The Bin Eats The Label" button (`AnimatedDeleteOrResetButton`):
 * When tapped, the trash/reset icon rotates (`25f`) and slides horizontally across the button
 * to "swallow" the text label (`animateDpAsState` + `animateFloatAsState`) while invoking `onClick`.
 */
@Composable
fun AnimatedDeleteOrResetButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 11.5.sp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
) {
    var isEating by remember { mutableStateOf(false) }
    val animScope = rememberCoroutineScope()
    val iconSlideOffset by animateDpAsState(
        targetValue = if (isEating) 28.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bin_eat_offset"
    )
    val lidRotation by animateFloatAsState(
        targetValue = if (isEating) 25f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bin_lid_rotation"
    )
    val labelScale by animateFloatAsState(
        targetValue = if (isEating) 0.15f else 1f,
        animationSpec = tween(durationMillis = 260),
        label = "bin_label_scale"
    )

    val pillShape = RoundedCornerShape(50)
    val gradientColors = listOf(Color(0xFFFF4B72), Color(0xFFE11D48))
    val spotShadowColor = Color(0xFFE11D48)
    val innerSpecularHighlight = Brush.verticalGradient(
        colors = listOf(Color.White.copy(alpha = 0.45f), Color.Transparent)
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = pillShape,
                spotColor = spotShadowColor,
                ambientColor = spotShadowColor
            )
            .clip(pillShape)
            .background(brush = Brush.linearGradient(colors = gradientColors), shape = pillShape)
            .border(width = 1.dp, brush = innerSpecularHighlight, shape = pillShape)
            .clickable {
                isEating = true
                onClick()
                animScope.launch {
                    delay(350)
                    isEating = false
                }
            }
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(16.dp)
                    .offset(x = iconSlideOffset)
                    .rotate(lidRotation)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = fontSize,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.scale(labelScale)
            )
        }
    }
}

@Composable
fun Tactile3DIconBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    isDark: Boolean = LocalAppAppearanceState.current.isDark,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 36.dp,
    iconSize: androidx.compose.ui.unit.Dp = 19.dp
) {
    val badgeShape = RoundedCornerShape(12.dp)
    val bevelBorderBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.6f),
            Color(0xFF94A3B8)
        )
    )
    val surfaceBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF283750), Color(0xFF141E30))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0))
        )
    }
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 6.dp,
                shape = badgeShape,
                spotColor = if (isDark) Color.Black else Color(0xFF64748B)
            )
            .clip(badgeShape)
            .background(brush = surfaceBrush, shape = badgeShape)
            .border(width = 1.3.dp, brush = bevelBorderBrush, shape = badgeShape)
            .border(
                width = 0.7.dp,
                color = Color.White.copy(alpha = if (isDark) 0.22f else 0.6f),
                shape = badgeShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun PremiumTactileCalcButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isDark: Boolean = LocalAppAppearanceState.current.isDark,
    accentColor: Color = LocalAppAppearanceState.current.accentOption.primaryColor,
    fontSize: TextUnit = 13.5.sp
) {
    val btnShape = RoundedCornerShape(14.dp)
    val outerBevelBrush = if (isSelected) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.65f),
                Color(0xFF94A3B8)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.6f),
                Color(0xFF94A3B8)
            )
        )
    }

    val appearance = LocalAppAppearanceState.current
    val innerSurfaceBrush = if (isSelected) {
        Brush.verticalGradient(
            colors = listOf(
                appearance.accentOption.darkModeColor,
                accentColor
            )
        )
    } else if (isDark) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF2B3B55), Color(0xFF152033))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0))
        )
    }

    val textColor = when {
        isSelected -> Color.White
        isDark -> Color.White
        else -> Color(0xFF0F172A)
    }

    Box(
        modifier = modifier
            .height(44.dp)
            .tactile3DStyle(
                baseColor = if (isSelected) accentColor else Color(0xFF64748B),
                cornerRadius = 14.dp,
                elevation = 6.dp
            )
            .background(brush = innerSurfaceBrush, shape = btnShape)
            .border(width = 1.4.dp, brush = outerBevelBrush, shape = btnShape)
            .border(
                width = 0.7.dp,
                color = Color.White.copy(alpha = if (isDark) 0.25f else 0.6f),
                shape = btnShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun NeumorphicRecessedDisplay(
    modifier: Modifier = Modifier,
    isDark: Boolean = LocalAppAppearanceState.current.isDark,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val appearance = LocalAppAppearanceState.current
    val isDarwin = appearance.paperStyle == AppPaperStyleOption.DARWIN_PARCHMENT
    val cornerDp = appearance.cornerRadiusDp.dp
    val recessedShape = RoundedCornerShape(cornerDp)
    val recessedBg = when {
        isDark && isDarwin -> Color(0xFF211913)
        isDark -> Color(0xFF0B1324)
        isDarwin -> Color(0xFFF4EBD6)
        else -> Color(0xFFEBF1F8)
    }
    val recessedBorderBrush = when {
        isDark && isDarwin -> Brush.linearGradient(listOf(Color(0xFF8B6914), Color(0xFFC59B27)))
        isDark -> Brush.linearGradient(listOf(Color(0xFF050914), Color(0xFF26354D)))
        isDarwin -> Brush.linearGradient(listOf(Color(0xFF8C6D46), Color(0xFF4A321F)))
        else -> Brush.linearGradient(listOf(Color(0xFFCBD5E1), Color.White))
    }

    Box(
        modifier = modifier
            .clip(recessedShape)
            .background(color = recessedBg, shape = recessedShape)
            .border(width = 1.2.dp, brush = recessedBorderBrush, shape = recessedShape)
            .padding(contentPadding)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
fun NeumorphicRecessedField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isDark: Boolean = LocalAppAppearanceState.current.isDark,
    accentColor: Color = LocalAppAppearanceState.current.accentColor,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val appearance = LocalAppAppearanceState.current
    val isDarwin = appearance.paperStyle == AppPaperStyleOption.DARWIN_PARCHMENT
    val cornerDp = appearance.cornerRadiusDp.dp
    val recessedShape = RoundedCornerShape(cornerDp)
    var isFocused by remember { mutableStateOf(false) }

    val recessedBg = when {
        isDark && isDarwin -> Color(0xFF211913)
        isDark -> Color(0xFF0B1324)
        isDarwin -> Color(0xFFFAF3E4)
        else -> Color(0xFFEBF1F8)
    }
    val primaryText = appearance.theme.headingColor
    val secondaryText = appearance.theme.secondaryTextColor

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                fontSize = 11.sp,
                color = if (isFocused) accentColor else secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        placeholder = if (placeholder.isNotEmpty()) {
            { Text(placeholder, color = secondaryText.copy(alpha = 0.6f), fontSize = 11.sp) }
        } else null,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = recessedShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
            focusedContainerColor = recessedBg,
            unfocusedContainerColor = recessedBg,
            disabledContainerColor = recessedBg,
            errorContainerColor = recessedBg,
            focusedTextColor = primaryText,
            unfocusedTextColor = primaryText,
            focusedLabelColor = accentColor,
            cursorColor = accentColor
        ),
        modifier = modifier
            .luminousFocusGlow(
                isFocused = isFocused,
                cornerRadius = cornerDp,
                isDark = isDark
            )
            .onFocusChanged { isFocused = it.isFocused }
    )
}

@Composable
fun LuminousOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val appearance = LocalAppAppearanceState.current
    val isDark = appearance.isDark
    val isDarwin = appearance.paperStyle == AppPaperStyleOption.DARWIN_PARCHMENT
    val cornerDp = appearance.cornerRadiusDp.dp
    val fieldShape = RoundedCornerShape(cornerDp)
    var isFocused by remember { mutableStateOf(false) }

    val fieldBg = when {
        !enabled -> if (isDark) Color(0xFF18120E) else Color(0xFFF4EBD6)
        isDark && isDarwin -> Color(0xFF211913)
        isDark -> Color(0xFF0F172A)
        isDarwin -> Color(0xFFFAF3E4)
        else -> Color.White
    }
    val primaryText = appearance.theme.headingColor
    val secondaryText = appearance.theme.secondaryTextColor
    val activeAccent = appearance.accentColor

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        enabled = enabled,
        isError = isError,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        supportingText = supportingText,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = fieldShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
            focusedContainerColor = fieldBg,
            unfocusedContainerColor = fieldBg,
            disabledContainerColor = fieldBg,
            errorContainerColor = fieldBg,
            focusedTextColor = primaryText,
            unfocusedTextColor = primaryText,
            disabledTextColor = primaryText,
            disabledLabelColor = secondaryText,
            disabledLeadingIconColor = activeAccent,
            disabledSupportingTextColor = secondaryText.copy(alpha = 0.9f),
            focusedLabelColor = activeAccent,
            cursorColor = activeAccent
        ),
        modifier = modifier
            .luminousFocusGlow(
                isFocused = isFocused && enabled && !readOnly,
                cornerRadius = cornerDp,
                isDark = isDark
            )
            .onFocusChanged { isFocused = it.isFocused }
    )
}

@Composable
fun WorkerFreeTextTransactionDialog(
    type: TransactionType,
    isInvoiceMode: Boolean,
    lang: AppLanguage,
    curr: String,
    currentTheme: AppThemeOption,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    val appearance = LocalAppAppearanceState.current
    val isDark = currentTheme.isDark
    val dialogBg = currentTheme.cardColor
    val primaryText = currentTheme.headingColor
    val secondaryText = currentTheme.secondaryTextColor
    val activeAccent = appearance.accentColor
    val primaryButtonAccent = appearance.accentOption.primaryColor
    val pillShape = RoundedCornerShape((appearance.cornerRadiusDp - 4).coerceAtLeast(8).dp)

    var customTitleInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val quickChips = remember(type, lang) {
        if (type == TransactionType.EXPENSE) {
            listOf(
                Translations.tr("home_cat_groceries", lang),
                Translations.tr("home_cat_electricity_gas", lang),
                Translations.tr("home_cat_internet_phone", lang),
                Translations.tr("home_cat_car_insurance", lang)
            )
        } else {
            listOf(
                Translations.tr("home_cat_salary_income", lang),
                Translations.tr("tx_other_inc", lang)
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .darwinManuscriptBorder(appearance, isDark)
            .testTag("worker_free_tx_dialog"),
        shape = RoundedCornerShape(appearance.cornerRadiusDp.dp),
        containerColor = dialogBg,
        title = {
            Text(
                text = when {
                    isInvoiceMode -> Translations.tr("worker_add_new_bill_btn", lang)
                    type == TransactionType.EXPENSE -> Translations.tr("btn_spend", lang)
                    else -> Translations.tr("btn_income", lang)
                },
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
                LuminousOutlinedTextField(
                    value = customTitleInput,
                    onValueChange = {
                        customTitleInput = it
                        validationError = null
                    },
                    label = {
                        Text(
                            if (type == TransactionType.EXPENSE)
                                Translations.tr("worker_tx_expense_name_label", lang)
                            else
                                Translations.tr("worker_tx_income_name_label", lang),
                            fontSize = 11.5.sp
                        )
                    },
                    placeholder = {
                        Text(
                            if (type == TransactionType.EXPENSE)
                                Translations.tr("worker_tx_expense_name_placeholder", lang)
                            else
                                Translations.tr("worker_tx_income_name_placeholder", lang),
                            fontSize = 11.sp
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_worker_tx_custom_name")
                )

                // اختصارات سريعة اختيارية تملأ الحقل النصي مع بقاء حرية التعديل الكاملة
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickChips.forEach { chipLabel ->
                        Surface(
                            shape = pillShape,
                            color = if (customTitleInput == chipLabel) appearance.accentContainerColor else Color.Transparent,
                            border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.4f)),
                            modifier = Modifier.clickable {
                                customTitleInput = chipLabel
                                validationError = null
                            }
                        ) {
                            Text(
                                text = chipLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (customTitleInput == chipLabel) activeAccent else secondaryText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                LuminousOutlinedTextField(
                    value = amountInput,
                    onValueChange = {
                        amountInput = it.filter { ch -> ch.isDigit() || ch == '.' }
                        validationError = null
                    },
                    label = { Text("${Translations.tr("amount_label", lang)} ($curr)") },
                    placeholder = { Text("1500") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_worker_tx_amount")
                )

                validationError?.let { errMsg ->
                    Text(
                        text = errMsg,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = ActionRed
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountInput.trim().toDoubleOrNull()
                    val resolvedName = customTitleInput.trim()
                    if (resolvedName.isBlank() || amt == null || amt <= 0.0) {
                        validationError = Translations.tr("input_required_error", lang)
                        return@Button
                    }
                    onConfirm(resolvedName, amt)
                },
                shape = pillShape,
                colors = ButtonDefaults.buttonColors(containerColor = primaryButtonAccent),
                modifier = Modifier.testTag("btn_confirm_worker_free_tx")
            ) {
                Text(
                    Translations.tr("confirm_btn", lang),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Translations.tr("cancel_btn", lang), color = secondaryText)
            }
        }
    )
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

        val visibleConversations = conversations.filter {
            it.id !in AgritechMockDb.blockedUserIds &&
                it.userName !in AgritechMockDb.blockedUserIds &&
                it.id !in AgritechMockDb.reportedUgcItemIds
        }
        items(visibleConversations, key = { it.id }) { conv ->
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
    var showUgcReportDialog by remember { mutableStateOf(false) }
    var ugcReportConfirmationBanner by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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

    if (showUgcReportDialog) {
        UgcReportReasonDialog(
            currentTheme = currentTheme,
            lang = lang,
            onDismiss = { showUgcReportDialog = false },
            onConfirmReport = { reason ->
                showUgcReportDialog = false
                AgritechMockDb.reportUgcContent(
                    itemId = conversation.id,
                    targetUid = conversation.id,
                    reason = reason,
                    context = context,
                    firestoreRepo = firestoreRepo
                )
                ugcReportConfirmationBanner = Translations.tr("ugc_report_received_banner", lang)
                scope.launch {
                    firestoreRepo?.submitUgcReportToCloud(
                        itemId = conversation.id,
                        targetUid = conversation.id,
                        reason = reason,
                        contentType = "CHAT"
                    )
                }
            }
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

                    // Google Play UGC Policy: Report & Block User actions inside Chat
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .testTag("btn_report_ugc_content")
                            .clickable { showUgcReportDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFFBEB),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.45f))
                    ) {
                        Text(
                            Translations.tr("ugc_report_short_btn", lang),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFB45309)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .testTag("btn_block_ugc_user")
                            .clickable {
                                AgritechMockDb.blockUser(
                                    targetUid = conversation.id,
                                    context = context,
                                    firestoreRepo = firestoreRepo
                                )
                                scope.launch {
                                    firestoreRepo?.saveBlockedUserToCloud(AgritechMockDb.blockedUserIds.toSet())
                                }
                                onBack()
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.45f))
                    ) {
                        Text(
                            Translations.tr("ugc_block_short_btn", lang),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            }
        }

        ugcReportConfirmationBanner?.let { bannerMsg ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("ugc_report_confirmation_banner"),
                shape = RoundedCornerShape(10.dp),
                color = appearance.accentContainerColor,
                border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.45f))
            ) {
                Text(
                    bannerMsg,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else activeAccent
                )
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
        containerColor = currentTheme.cardColor,
        shape = RoundedCornerShape(appearance.cornerRadiusDp.dp),
        modifier = Modifier
            .darwinManuscriptBorder(appearance, isDark)
            .testTag("add_phone_contact_dialog"),
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
                LuminousOutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text(Translations.tr("contact_name_label", lang), fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_contact_name")
                )
                LuminousOutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it.filter { ch -> ch.isDigit() || ch == '+' || ch == ' ' } },
                    label = { Text(Translations.tr("contact_phone_label", lang), fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
    val dialogBg = currentTheme.cardColor
    val primaryText = currentTheme.headingColor
    val secondaryText = currentTheme.secondaryTextColor
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
        modifier = Modifier
            .darwinManuscriptBorder(appearance, isDark)
            .testTag("universal_financial_request_dialog"),
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

                LuminousOutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("${Translations.tr("fin_req_amount_label", lang)} ($curr)", fontSize = 12.sp) },
                    placeholder = { Text("15000", fontSize = 12.sp, color = secondaryText.copy(alpha = 0.5f)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_fin_req_amount")
                )

                LuminousOutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text(Translations.tr("fin_req_note_label", lang), fontSize = 12.sp) },
                    singleLine = false,
                    maxLines = 2,
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
                        val firestoreCategory = if (selectedCatIdx == 4) "MEDICATION" else chosenCat
                        val finalNote = if (selectedCatIdx == 4) {
                            listOf(chosenCat, noteText.trim()).filter { it.isNotBlank() }.joinToString(" • ")
                        } else {
                            noteText.trim()
                        }
                        val txId = "tx_req_${farm.id}_${cycle.id}_$dialogIdempotencyUuid"
                        val pendingTx = TransactionModel(
                            id = txId,
                            farmId = farm.id,
                            cycleId = cycle.id,
                            type = TransactionType.EXPENSE,
                            category = firestoreCategory,
                            amount = amt,
                            currency = curr,
                            date = "${Translations.tr("day_prefix", lang)} $dayNumber",
                            status = "PENDING",
                            notes = finalNote
                        )
                        val chatMsg = AgritechMockDb.recordUniversalFinancialRequest(
                            tx = pendingTx,
                            noteText = finalNote,
                            senderId = "contact_${channelId}",
                            senderName = senderName,
                            senderRole = senderRole,
                            chatChannelId = channelId,
                            displayCategory = chosenCat
                        )
                        if (firestoreRepo != null) {
                            scope.launch {
                                firestoreRepo.recordTransactionIdempotent(
                                    idempotencyKey = txId,
                                    tx = pendingTx
                                )
                                firestoreRepo.sendChatMessage(chatMsg, farmIdHint = farm.id)
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
                            maxLines = 1,
                            softWrap = false
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
    firestoreRepo: AgritechFirestoreRepository? = null,
    onContactPublisher: (MarketPostModel) -> Unit
) {
    val appearance = LocalAppAppearanceState.current
    var ugcFeedbackBanner by remember { mutableStateOf<String?>(null) }
    val visiblePosts = posts.filter { post ->
        post.id !in AgritechMockDb.reportedUgcItemIds &&
            post.publisherId !in AgritechMockDb.blockedUserIds &&
            post.chatChannelId !in AgritechMockDb.blockedUserIds &&
            post.publisherName !in AgritechMockDb.blockedUserIds
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .testTag("global_market_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ugcFeedbackBanner?.let { msg ->
            item(key = "ugc_market_feedback_banner") {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ugc_report_confirmation_banner"),
                    shape = RoundedCornerShape(12.dp),
                    color = appearance.accentContainerColor,
                    border = BorderStroke(1.dp, appearance.accentColor.copy(alpha = 0.45f))
                ) {
                    Text(
                        msg,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentTheme.isDark) Color.White else appearance.accentColor
                    )
                }
            }
        }

        items(visiblePosts, key = { it.id }) { post ->
            MarketPostCard(
                post = post,
                currentTheme = currentTheme,
                lang = lang,
                firestoreRepo = firestoreRepo,
                onContact = { onContactPublisher(post) },
                onReportedNotice = { notice ->
                    ugcFeedbackBanner = notice
                }
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
    firestoreRepo: AgritechFirestoreRepository? = null,
    onContact: () -> Unit,
    onReportedNotice: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showReportDialog by remember { mutableStateOf(false) }
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
    val targetAuthorId = post.publisherId.ifBlank { post.chatChannelId.ifBlank { post.publisherName } }

    if (showReportDialog) {
        UgcReportReasonDialog(
            currentTheme = currentTheme,
            lang = lang,
            onDismiss = { showReportDialog = false },
            onConfirmReport = { reason ->
                showReportDialog = false
                AgritechMockDb.reportUgcContent(
                    itemId = post.id,
                    targetUid = targetAuthorId,
                    reason = reason,
                    context = context,
                    firestoreRepo = firestoreRepo
                )
                onReportedNotice(Translations.tr("ugc_report_received_banner", lang))
                scope.launch {
                    firestoreRepo?.submitUgcReportToCloud(
                        itemId = post.id,
                        targetUid = targetAuthorId,
                        reason = reason,
                        contentType = "MARKET_POST"
                    )
                }
            }
        )
    }

    val localizedPost = remember(post, lang, Translations.globalCurrencyCode) {
        GlobalLanguageDictionaries.localizeMarketPost(post, lang)
    }

    val loadedBitmap = remember(localizedPost.imageUri) {
        localizedPost.imageUri?.let { uriStr ->
            try {
                val parsed = Uri.parse(uriStr)
                context.contentResolver.openInputStream(parsed)?.use { stream ->
                    val opts = BitmapFactory.Options().apply { inSampleSize = 4 }
                    BitmapFactory.decodeStream(stream, null, opts)?.asImageBitmap()
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("market_post_card_${localizedPost.id}"),
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
                        contentDescription = localizedPost.imageCaption,
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
                            if (localizedPost.imageUri != null) Icons.Default.PhotoCamera else Icons.Default.Image,
                            contentDescription = localizedPost.imageCaption,
                            tint = if (localizedPost.imageUri != null) accentEmerald else metaText,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            localizedPost.imageCaption,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (localizedPost.imageUri != null) accentEmerald else metaText
                        )
                    }
                }
            }

            // b) Post Title & Text Description
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    localizedPost.title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.5.sp,
                    color = primaryText
                )
                Text(
                    localizedPost.description,
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
                    "${String.format(java.util.Locale.US, "%,.0f", localizedPost.priceDzd)} ${localizedPost.priceUnit}",
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
                            localizedPost.publisherName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = primaryText,
                            maxLines = 1
                        )
                        Text(
                            "📍 ${localizedPost.publisherLocation}",
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

            // e) Google Play UGC Moderation Row: Report Content & Block User
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showReportDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_report_ugc_content"),
                    shape = btnShape,
                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        Translations.tr("ugc_report_btn", lang),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = {
                        AgritechMockDb.blockUser(
                            targetUid = targetAuthorId,
                            context = context,
                            firestoreRepo = firestoreRepo
                        )
                        scope.launch {
                            firestoreRepo?.saveBlockedUserToCloud(AgritechMockDb.blockedUserIds.toSet())
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_block_ugc_user"),
                    shape = btnShape,
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        Translations.tr("ugc_block_btn", lang),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
    val appearance = LocalAppAppearanceState.current
    val isDark = currentTheme.isDark
    val dialogBg = currentTheme.cardColor
    val fieldBg = currentTheme.bgColor
    val fieldBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val primaryText = currentTheme.headingColor
    val secondaryText = currentTheme.secondaryTextColor
    val accentEmerald = appearance.accentColor
    val dialogCornerDp = appearance.cornerRadiusDp.dp

    var titleInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Real Android Photo Picker (ActivityResultContracts.PickVisualMedia) with persistable read URI grant
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            selectedImageUri = uri
        }
    }

    val previewBitmap = remember(selectedImageUri) {
        selectedImageUri?.let { uri ->
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val opts = BitmapFactory.Options().apply { inSampleSize = 4 }
                    BitmapFactory.decodeStream(stream, null, opts)?.asImageBitmap()
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(dialogCornerDp),
        containerColor = dialogBg,
        modifier = Modifier.darwinManuscriptBorder(cornerRadius = dialogCornerDp, isDark = isDark),
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
                LuminousOutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text(Translations.tr("field_post_title", lang), fontSize = 12.sp, color = secondaryText) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_market_post_title")
                )

                LuminousOutlinedTextField(
                    value = descInput,
                    onValueChange = { descInput = it },
                    label = { Text(Translations.tr("field_post_desc", lang), fontSize = 12.sp, color = secondaryText) },
                    singleLine = false,
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_market_post_desc")
                )

                LuminousOutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text(Translations.tr("field_post_price", lang), fontSize = 12.sp, color = secondaryText) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_market_post_price")
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
                            appearance.accentContainerColor
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
                        if (selectedImageUri != null) Translations.tr("market_image_attached_success", lang)
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
                        val ownerUid = currentUser?.uid?.takeIf { it.isNotBlank() }.orEmpty()
                        val newPostId = "post_${System.currentTimeMillis()}"
                        val resolvedChatChannel = if (ownerUid.isNotBlank()) "market_${newPostId}_$ownerUid" else "general_admin"
                        val activeFarm = AgritechMockDb.farms.firstOrNull()
                        val resolvedPublisherName = currentUser?.displayName?.takeIf { it.isNotBlank() }
                            ?: activeFarm?.name?.takeIf { it.isNotBlank() }
                            ?: Translations.tr("role_badge_owner_barn", lang)
                        val resolvedPublisherLocation = activeFarm?.location?.takeIf { it.isNotBlank() }
                            ?: Translations.tr("farms_title", lang)
                        onPublishPost(
                            MarketPostModel(
                                id = newPostId,
                                title = cleanTitle,
                                description = descInput.trim().ifEmpty { cleanTitle },
                                priceDzd = parsedPrice,
                                priceUnit = curr,
                                publisherName = resolvedPublisherName,
                                publisherLocation = resolvedPublisherLocation,
                                imageCaption = cleanTitle,
                                chatChannelId = resolvedChatChannel,
                                imageUri = selectedImageUri?.toString(),
                                publisherId = ownerUid
                            )
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LocalAppAppearanceState.current.accentOption.primaryColor),
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
    val dialogBg = currentTheme.cardColor
    val fieldSurfaceBg = currentTheme.bgColor
    val cardBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val primaryText = currentTheme.headingColor
    val secondaryText = currentTheme.secondaryTextColor
    val accentEmerald = appearance.accentColor
    val cornerRadius = appearance.cornerRadiusDp.dp

    val isEditMode = initialReport != null
    val lockedDayNumber = (initialReport?.dayNumber ?: suggestedDayNumber).coerceAtLeast(1)
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
            initialReport?.temperature?.let { String.format(java.util.Locale.US, "%.1f", it) } ?: ""
        )
    }
    var gasAmmoniaInput by remember(initialReport) {
        mutableStateOf(
            if (initialReport != null) String.format(java.util.Locale.US, "%.1f", initialReport.gasUnits)
            else ""
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

    val parsedCurrentDay = lockedDayNumber
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
        shape = RoundedCornerShape(cornerRadius),
        modifier = Modifier
            .darwinManuscriptBorder(cornerRadius = cornerRadius, isDark = isDark)
            .testTag("owner_daily_report_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accentEmerald.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, accentEmerald.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "📋",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEditMode) Translations.tr("dialog_edit_daily_report_title", lang)
                        else Translations.tr("dialog_owner_daily_report_title", lang),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = primaryText,
                        lineHeight = 20.sp
                    )
                    Text(
                        text = Translations.getLocalizedFarmName(farm.id, lang, farm.name),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HorizontalDivider(color = cardBorderColor.copy(alpha = 0.7f))

                // Locked Auto-Increment Cycle Day Field (Full Width, readOnly & non-editable)
                LuminousOutlinedTextField(
                    value = "${Translations.tr("day_prefix", lang)} $lockedDayNumber",
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = accentEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    label = {
                        Text(
                            Translations.tr("field_day_number_mid_cycle", lang),
                            fontSize = 11.5.sp,
                            color = secondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    supportingText = {
                        Text(
                            Translations.tr("field_day_number_mid_cycle_hint", lang),
                            fontSize = 10.5.sp,
                            color = secondaryText.copy(alpha = 0.9f),
                            lineHeight = 14.sp
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_owner_report_day")
                )

                // Row 1: Feed (Quintals) | Water (Liters)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LuminousOutlinedTextField(
                        value = feedQuintalsInput,
                        onValueChange = {
                            feedQuintalsInput = it.filter { ch -> ch.isDigit() || ch == '.' }
                            dailyReportValidationError = null
                        },
                        label = {
                            Text(
                                Translations.tr("th_feed", lang),
                                fontSize = 11.sp,
                                color = secondaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        placeholder = { Text("1.8", fontSize = 11.sp, color = secondaryText.copy(alpha = 0.55f)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_owner_report_feed_quintals")
                    )

                    LuminousOutlinedTextField(
                        value = waterLitersInput,
                        onValueChange = {
                            waterLitersInput = it.filter { ch -> ch.isDigit() || ch == '.' }
                            dailyReportValidationError = null
                        },
                        label = {
                            Text(
                                Translations.tr("th_water", lang),
                                fontSize = 11.sp,
                                color = secondaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        placeholder = { Text("320", fontSize = 11.sp, color = secondaryText.copy(alpha = 0.55f)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_owner_report_water")
                    )
                }

                // Row 2: Mortality | Avg Bird Weight (Grams, optional)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LuminousOutlinedTextField(
                        value = mortalityInput,
                        onValueChange = {
                            mortalityInput = it.filter { ch -> ch.isDigit() }
                            dailyReportValidationError = null
                        },
                        label = {
                            Text(
                                Translations.tr("th_mortality", lang),
                                fontSize = 11.sp,
                                color = secondaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        placeholder = { Text("0", fontSize = 11.sp, color = secondaryText.copy(alpha = 0.55f)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_owner_report_mortality")
                    )

                    LuminousOutlinedTextField(
                        value = actualWeightGramsInput,
                        onValueChange = { actualWeightGramsInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = {
                            Text(
                                Translations.tr("field_actual_bird_weight_grams_optional", lang),
                                fontSize = 11.sp,
                                color = secondaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        placeholder = { Text("450", fontSize = 11.sp, color = secondaryText.copy(alpha = 0.55f)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_owner_report_actual_weight_grams")
                    )
                }

                // Row 3: Temp (°C) | Gas/Ammonia
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LuminousOutlinedTextField(
                        value = tempInput,
                        onValueChange = { tempInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = {
                            Text(
                                Translations.tr("th_temp", lang),
                                fontSize = 11.sp,
                                color = secondaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        placeholder = { Text("29.5", fontSize = 11.sp, color = secondaryText.copy(alpha = 0.55f)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_owner_report_temp")
                    )

                    LuminousOutlinedTextField(
                        value = gasAmmoniaInput,
                        onValueChange = { gasAmmoniaInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = {
                            Text(
                                Translations.tr("th_gas", lang),
                                fontSize = 11.sp,
                                color = secondaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        placeholder = { Text("1.5", fontSize = 11.sp, color = secondaryText.copy(alpha = 0.55f)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_owner_report_gas")
                    )
                }

                // Clean Medicine in Water Confirmation Card
                Surface(
                    shape = RoundedCornerShape(cornerRadius),
                    color = if (medicineGivenToggle) accentEmerald.copy(alpha = if (isDark) 0.16f else 0.08f) else fieldSurfaceBg,
                    border = BorderStroke(
                        1.dp,
                        if (medicineGivenToggle) accentEmerald.copy(alpha = 0.55f) else cardBorderColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = Translations.tr("confirm_medicine_water_today_label", lang),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (medicineGivenToggle) accentEmerald else primaryText,
                                lineHeight = 16.sp,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            Switch(
                                checked = medicineGivenToggle,
                                onCheckedChange = { medicineGivenToggle = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = accentEmerald
                                ),
                                modifier = Modifier.testTag("switch_owner_report_medicine_given")
                            )
                        }
                        AnimatedVisibility(visible = medicineGivenToggle) {
                            LuminousOutlinedTextField(
                                value = medicineSummaryInput,
                                onValueChange = { medicineSummaryInput = it },
                                label = {
                                    Text(
                                        Translations.tr("medicine_water_name_input_label", lang),
                                        fontSize = 11.sp,
                                        color = secondaryText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_owner_report_medicine_name")
                            )
                        }
                    }
                }

                dailyReportValidationError?.let { errMsg ->
                    Text(
                        text = errMsg,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = ActionRed,
                        modifier = Modifier.testTag("daily_report_validation_error")
                    )
                }
            }
        },
        confirmButton = {
            PremiumPillButton(
                text = Translations.tr("btn_save", lang),
                onClick = {
                    val parsedFeedQ = feedQuintalsInput.trim().toDoubleOrNull()
                    val parsedWaterVal = waterLitersInput.trim().toDoubleOrNull()
                    val parsedMortVal = mortalityInput.trim().toIntOrNull()
                    if (parsedFeedQ == null || parsedWaterVal == null || parsedMortVal == null) {
                        dailyReportValidationError = Translations.tr("input_required_error", lang)
                        return@PremiumPillButton
                    }
                    val parsedDay = lockedDayNumber
                    val parsedFeedKg = (parsedFeedQ * 100.0).coerceAtLeast(0.0)
                    val parsedWater = parsedWaterVal.coerceAtLeast(0.0)
                    val parsedMortality = parsedMortVal.coerceAtLeast(0)
                    val parsedTemp: Double? = tempInput.trim().toDoubleOrNull()
                    val parsedGas = (gasAmmoniaInput.trim().toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
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

                    val newVentilation = initialReport?.ventilationStatus ?: "جيدة"
                    val newLitter = initialReport?.litterCondition ?: ""
                    val oldWeight = initialReport?.actualAvgWeightKg?.takeIf { it > 0.0 }
                    val newWeight = resolvedActualWeightKg.takeIf { it > 0.0 }
                    val weightChanged = if (oldWeight == null && newWeight == null) {
                        false
                    } else if (oldWeight == null || newWeight == null) {
                        true
                    } else {
                        kotlin.math.abs(newWeight - oldWeight) > 0.0001
                    }

                    val oldTemp = initialReport?.temperature
                    val tempChanged = if (oldTemp == null && parsedTemp == null) {
                        false
                    } else if (oldTemp == null || parsedTemp == null) {
                        true
                    } else {
                        kotlin.math.abs(parsedTemp - oldTemp) > 0.01
                    }
                    val hasActualValueChange = initialReport != null && (
                        kotlin.math.abs(parsedFeedKg - initialReport.feedIntakeKg) > 0.001 ||
                            kotlin.math.abs(parsedWater - initialReport.waterIntakeLiters) > 0.001 ||
                            parsedMortality != initialReport.mortalityCount ||
                            tempChanged ||
                            kotlin.math.abs(parsedGas - initialReport.gasUnits) > 0.01 ||
                            newVentilation.trim() != initialReport.ventilationStatus.trim() ||
                            newLitter.trim() != initialReport.litterCondition.trim() ||
                            medicineGivenToggle != initialReport.medicineGiven ||
                            resolvedMedName.trim() != initialReport.medicineSummary.trim() ||
                            weightChanged
                    )

                    val reportId = AgritechFirestoreRepository.buildCanonicalDailyReportId(
                        farmId = farm.id,
                        cycleId = cycle.id,
                        dayNumber = parsedDay
                    )
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
                        ventilationStatus = newVentilation,
                        litterCondition = newLitter,
                        actualAvgWeightKg = resolvedActualWeightKg,
                        isEdited = if (initialReport != null) {
                            if (hasActualValueChange) true else initialReport.isEdited
                        } else {
                            false
                        },
                        previousSummary = if (initialReport != null && hasActualValueChange) {
                            initialReport.previousSummary.ifBlank {
                                val oldTempText = initialReport.temperature?.let { "${String.format(java.util.Locale.US, "%.1f", it)}°" } ?: "—"
                                "العلف: ${Translations.formatQuintalNumber(initialReport.feedIntakeKg / 100.0)} ق • الماء: ${initialReport.waterIntakeLiters.toInt()} L • النافق: ${initialReport.mortalityCount} • الحرارة: $oldTempText"
                            }
                        } else {
                            initialReport?.previousSummary ?: ""
                        },
                        medicineGiven = medicineGivenToggle,
                        medicineSummary = resolvedMedName
                    )
                    onSaveReport(reportToSave)
                },
                variant = PremiumPillVariant.PRIMARY,
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .padding(horizontal = 4.dp)
                    .testTag("btn_confirm_owner_daily_report")
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) {
                Text(
                    text = Translations.tr("btn_cancel", lang),
                    color = secondaryText,
                    fontWeight = FontWeight.Bold
                )
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
    val dialogBg = currentTheme.cardColor
    val primaryText = currentTheme.headingColor
    val secondaryText = currentTheme.secondaryTextColor
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
        modifier = Modifier
            .darwinManuscriptBorder(appearance, isDark)
            .testTag("vet_medicine_invoice_dialog"),
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

                LuminousOutlinedTextField(
                    value = medicineName,
                    onValueChange = { medicineName = it },
                    label = { Text(Translations.tr("vet_med_name_label", lang), fontSize = 12.sp, color = secondaryText) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vet_med_name")
                )

                LuminousOutlinedTextField(
                    value = dosageText,
                    onValueChange = { dosageText = it },
                    label = { Text(Translations.tr("vet_dosage_instructions_label", lang), fontSize = 12.sp, color = secondaryText) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vet_dosage")
                )

                LuminousOutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text(Translations.tr("vet_med_total_price_label", lang), fontSize = 12.sp, color = secondaryText) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vet_med_price")
                )

                LuminousOutlinedTextField(
                    value = clinicalNote,
                    onValueChange = { clinicalNote = it },
                    label = { Text(Translations.tr("vet_clinical_note_label", lang), fontSize = 12.sp, color = secondaryText) },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vet_clinical_note")
                )

                LuminousOutlinedTextField(
                    value = withdrawalDaysInput,
                    onValueChange = { withdrawalDaysInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text(Translations.tr("vet_withdrawal_days_label", lang), fontSize = 11.5.sp, color = secondaryText) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(vertical = 10.dp, horizontal = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "$medsCategoryLabel:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentEmerald,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "-${String.format(java.util.Locale.US, "%,.0f", parsedPrice)} $curr",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDark) Color(0xFFFCA5A5) else ActionRed
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = Translations.tr("vet_invoice_auto_ledger_note", lang),
                            fontSize = 11.5.sp,
                            lineHeight = 17.sp,
                            color = primaryText,
                            softWrap = true,
                            overflow = TextOverflow.Visible,
                            modifier = Modifier.fillMaxWidth()
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
                                firestoreRepo.sendChatMessage(chatMsg, farmIdHint = farm.id)
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
    val sessionState = LocalAgritechAppState.current
    val currentRole = sessionState.currentRole ?: AppRole.OWNER
    val messages = AgritechMockDb.streamChatMessages(channelId)
    var messageInput by remember { mutableStateOf("") }
    var pendingChatApprovalMsg by remember { mutableStateOf<ChatMessageModel?>(null) }
    val currentUserId = remember(sessionState.currentUserUid, currentRole) {
        try {
            FirebaseAuth.getInstance().currentUser?.uid
        } catch (_: Exception) {
            null
        } ?: sessionState.currentUserUid ?: when {
            currentRole.isVetRole -> "vet_1"
            currentRole == AppRole.WORKER -> "worker_1"
            else -> "owner_1"
        }
    }
    val resolvedSenderRole = when {
        currentRole.isOwnerRole -> Translations.tr("role_badge_owner_barn", lang)
        currentRole.isVetRole -> Translations.tr("role_badge_vet_clinic", lang)
        currentRole == AppRole.WORKER -> Translations.tr("role_badge_worker", lang)
        currentRole == AppRole.FEED_SUPPLIER -> Translations.tr("role_badge_feed_supplier", lang)
        currentRole == AppRole.CHICK_HATCHERY -> Translations.tr("role_badge_chick_hatchery", lang)
        currentRole == AppRole.WHOLESALE_TRADER -> Translations.tr("role_badge_wholesale_trader", lang)
        currentRole == AppRole.VET_IMPORTER -> Translations.tr("role_badge_vet_importer", lang)
        else -> Translations.tr("role_badge_owner_barn", lang)
    }
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
                val strippedCycleHint = channelId.removeSuffix("_worker").removeSuffix("_vet")
                val targetFarmId = targetTx?.farmId?.takeIf { it.isNotBlank() }
                    ?: AgritechMockDb.cycles.firstOrNull {
                        it.id == channelId || it.id == strippedCycleHint
                    }?.farmId?.takeIf { it.isNotBlank() }
                    ?: sessionState.selectedFarmForOwner?.id?.takeIf { it.isNotBlank() }
                    ?: sessionState.activeFarmId.takeIf {
                        it.isNotBlank() && !AgritechFirestoreRepository.isUnassignedFarm(it)
                    }
                    ?: AgritechMockDb.farms.firstOrNull()?.id.orEmpty()
                if (firestoreRepo != null && targetFarmId.isNotBlank()) {
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
                val isMe = if (currentRole.isOwnerRole) {
                    msg.senderId == currentUserId || msg.senderId == "owner_1"
                } else {
                    msg.senderId == currentUserId
                }

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
                                val preferredFarm = AgritechMockDb.farms.firstOrNull { it.id == sessionState.activeFarmId }
                                    ?: sessionState.selectedFarmForOwner
                                    ?: AgritechMockDb.farms.firstOrNull()
                                val resolvedSenderName = try {
                                    FirebaseAuth.getInstance().currentUser?.displayName?.takeIf { it.isNotBlank() }
                                } catch (_: Exception) {
                                    null
                                } ?: preferredFarm?.name?.takeIf { it.isNotBlank() }
                                    ?: resolvedSenderRole
                                val strippedCycleId = channelId.removeSuffix("_worker").removeSuffix("_vet")
                                val isDirectOrMarket = AgritechFirestoreRepository.isMarketOrPrivateDirectChannelId(channelId)
                                val channelFarmHint = if (isDirectOrMarket) {
                                    null
                                } else {
                                    AgritechMockDb.cycles.firstOrNull {
                                        it.id == channelId || it.id == strippedCycleId
                                    }?.farmId ?: sessionState.activeFarmId.takeIf {
                                        it.isNotBlank() && !AgritechFirestoreRepository.isUnassignedFarm(it)
                                    }
                                }
                                val marketTargetUid = if (channelId.startsWith("market_")) {
                                    AgritechMockDb.marketPosts.firstOrNull {
                                        (it.chatChannelId == channelId || channelId.startsWith("market_${it.id}_")) &&
                                            it.publisherId.isNotBlank()
                                    }?.publisherId ?: channelId.removePrefix("market_").substringAfterLast("_").takeIf {
                                        it.isNotBlank() && it != currentUserId
                                    } ?: channelId.removePrefix("market_").takeIf { it.isNotBlank() }
                                } else {
                                    AgritechMockDb.marketPosts.firstOrNull {
                                        it.chatChannelId == channelId && it.publisherId.isNotBlank()
                                    }?.publisherId
                                }
                                val newMsg = ChatMessageModel(
                                    id = "msg_${System.currentTimeMillis()}",
                                    cycleId = channelId,
                                    senderId = currentUserId,
                                    senderName = resolvedSenderName,
                                    senderRole = resolvedSenderRole,
                                    text = trimmed,
                                    timestamp = nowTime
                                )
                                AgritechMockDb.sendChatMessage(newMsg)
                                if (firestoreRepo != null) {
                                    scope.launch {
                                        firestoreRepo.sendChatMessage(
                                            message = newMsg,
                                            farmIdHint = channelFarmHint,
                                            targetUid = marketTargetUid
                                        )
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
    priceError: String? = null,
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
                            "${Translations.getLocalizedFarmName(farm.id, lang, farm.name)} • ${Translations.tr("active_cycle_label", lang)}",
                            fontSize = 12.sp,
                            color = if (currentTheme.isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
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
                        Tactile3DIconBadge(
                            icon = Icons.Default.LocalShipping,
                            tint = shipAccent,
                            isDark = currentTheme.isDark,
                            size = 44.dp,
                            iconSize = 23.dp
                        )
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
                            Tactile3DIconBadge(
                                icon = Icons.Default.Person,
                                tint = shipAccent,
                                isDark = currentTheme.isDark
                            )
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
                            Tactile3DIconBadge(
                                icon = Icons.Default.LocalShipping,
                                tint = shipAccent,
                                isDark = currentTheme.isDark
                            )
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
                            Tactile3DIconBadge(
                                icon = Icons.Default.CalendarToday,
                                tint = shipAccent,
                                isDark = currentTheme.isDark,
                                size = 40.dp,
                                iconSize = 20.dp
                            )
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

                    // 4. سعر الكيلوغرام الواحد في السوق اليوم (دج/كغ) مع اهتزاز فيزيائي وإطار أحمر مضيء عند الخطأ
                    val priceShakeOffset = remember { Animatable(0f) }
                    LaunchedEffect(priceError) {
                        if (priceError != null) {
                            listOf(-12f, 12f, -8f, 8f, 0f).forEach { target ->
                                priceShakeOffset.animateTo(target, animationSpec = tween(45))
                            }
                        }
                    }
                    OutlinedTextField(
                        value = pricePerKg,
                        onValueChange = onPricePerKgChange,
                        label = { Text(Translations.tr("price_per_kg_label", lang), fontSize = 13.sp) },
                        placeholder = { Text("380") },
                        isError = priceError != null,
                        supportingText = if (priceError != null) {
                            {
                                Text(
                                    text = priceError,
                                    color = ActionRed,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.testTag("sales_stage1_price_error")
                                )
                            }
                        } else null,
                        leadingIcon = {
                            Tactile3DIconBadge(
                                icon = Icons.Default.Payments,
                                tint = if (priceError != null) Color(0xFFEF4444) else shipAccent,
                                isDark = currentTheme.isDark
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(x = priceShakeOffset.value.dp)
                            .testTag("input_sales_price_per_kg"),
                        shape = shipFieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = shipAccent,
                            focusedLabelColor = shipAccent,
                            errorBorderColor = Color(0xFFEF4444),
                            errorLabelColor = Color(0xFFEF4444)
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
                            Tactile3DIconBadge(
                                icon = Icons.Default.Inbox,
                                tint = if (isCrateCountExceedingLiveBirds) ActionRed else shipAccent,
                                isDark = currentTheme.isDark
                            )
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
                                    Tactile3DIconBadge(
                                        icon = Icons.Default.Scale,
                                        tint = shipAccent,
                                        isDark = currentTheme.isDark
                                    )
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

            // زر عريض ثلاثي الأبعاد في الأسفل: [حفظ وبدء تسجيل الوزنات ➡️]
            PremiumPillButton(
                text = Translations.tr("btn_proceed_to_weighing", lang),
                onClick = {
                    if (!isCrateCountExceedingLiveBirds) {
                        onProceedToWeighing()
                    }
                },
                enabled = !isCrateCountExceedingLiveBirds,
                variant = PremiumPillVariant.PRIMARY,
                fontSize = 15.sp,
                leadingIcon = {
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("btn_proceed_to_weighing")
            )
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

    var weighSaveSuccessGlow by remember { mutableStateOf(false) }
    val weighSaveScale by animateFloatAsState(
        targetValue = if (weighSaveSuccessGlow) 1.04f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "weigh_save_spring"
    )

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
                weighSaveSuccessGlow = true
                listState.animateScrollToItem(targetLastIdx)
                delay(280)
                weighSaveSuccessGlow = false
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

                    // لا يتم حساب وإظهار الصافي إلا في الأسفل من المجموع الكلي داخل شاشة عرض رقمية غائرة (NeumorphicRecessedDisplay)
                    NeumorphicRecessedDisplay(
                        isDark = currentTheme.isDark,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val summaryLabelColor = if (currentTheme.isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                            // صافي وزن الدجاج الحي = (مجموع أوزان الممتلئ) - (مجموع أوزان الفارغ)
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.1f)) {
                                Text(Translations.tr("weigh_net_weight_label", lang), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = summaryLabelColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${String.format(java.util.Locale.US, "%,.1f", totalNetKg)} $kgUnit", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = positiveGreen)
                            }
                            // إجمالي عدد الطيور المباعة
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text(Translations.tr("weigh_total_birds_label", lang), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = summaryLabelColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    "${String.format(java.util.Locale.US, "%,d", totalBirdsSold)} ${Translations.tr("birds_unit_short", lang)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = if (isBirdsSoldExceedingLive) ActionRed else darkColor
                                )
                            }
                            // معدل وزن الدجاجة الواحدة = (الصافي الكلي ÷ إجمالي الطيور)
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text(Translations.tr("weigh_avg_bird_label", lang), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = summaryLabelColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${String.format(java.util.Locale.US, "%.3f", avgBirdWeightKg)} $kgUnit", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = positiveGreen)
                            }
                            // إجمالي قيمة المبيعات (دج) = (الصافي الكلي × سعر الكيلو)
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.2f)) {
                                Text(Translations.tr("weigh_total_sales_label", lang), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = summaryLabelColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${String.format(java.util.Locale.US, "%,.0f", totalSalesValue)} $curr", fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp, color = positiveGreen)
                            }
                        }
                    }

                    // زر [حفظ الفاتورة وترحيل للخزنة 💰] في الأسفل داخل SafeArea بتصميم كبسولة ثلاثية الأبعاد زمردية
                    var isSavingInvoice by remember { mutableStateOf(false) }
                    val saveScope = rememberCoroutineScope()
                    PremiumPillButton(
                        text = Translations.tr("btn_save_invoice_and_migrate", lang),
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
                        variant = PremiumPillVariant.SALES_INCOME,
                        fontSize = 14.sp,
                        leadingIcon = {
                            if (isSavingInvoice) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_save_invoice_and_migrate")
                    )
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
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(weighSaveScale),
                shape = weighCardShape,
                colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                border = if (weighSaveSuccessGlow) BorderStroke(1.8.dp, Color(0xFF10B981)) else null,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                "${Translations.tr("weigh_price_per_kg_badge", lang)}: ${String.format(java.util.Locale.US, "%,.0f", pricePerKg)} $curr",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = weighAccent
                            )
                        }
                    }

                    // شريط التحكم الرقمي ثلاثي الأبعاد فقط [ - ]   📦 X صناديق (Y طائر)   [ + ] بدون صف الأزرار [1..5] لتوفير المساحة الرأسية
                    val cratesNum = (cratesPerThisWeigh.toIntOrNull() ?: 5).coerceAtLeast(1)
                    val birdsInThisWeigh = cratesNum * birdsPerCrate
                    val stepperDisplayLabel = Translations.tr("weigh_crates_stepper_fmt", lang)
                        .replace("{crates}", "$cratesNum")
                        .replace("{birds}", "$birdsInThisWeigh")

                    NeumorphicRecessedDisplay(
                        isDark = currentTheme.isDark,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PremiumTactileCalcButton(
                                label = "-",
                                onClick = {
                                    cratesPerThisWeigh = (cratesNum - 1).coerceAtLeast(1).toString()
                                },
                                isDark = currentTheme.isDark,
                                accentColor = weighButtonAccent,
                                fontSize = 17.sp,
                                modifier = Modifier
                                    .width(48.dp)
                                    .testTag("btn_weigh_crates_minus")
                            )
                            Text(
                                text = stepperDisplayLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = weighAccent,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                                    .testTag("txt_weigh_crates_stepper_value")
                            )
                            PremiumTactileCalcButton(
                                label = "+",
                                onClick = {
                                    cratesPerThisWeigh = (cratesNum + 1).toString()
                                },
                                isDark = currentTheme.isDark,
                                accentColor = weighButtonAccent,
                                fontSize = 17.sp,
                                modifier = Modifier
                                    .width(48.dp)
                                    .testTag("btn_weigh_crates_plus")
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // خانة وزن الفارغ الغائرة (NeumorphicRecessedField)
                        NeumorphicRecessedField(
                            value = tareInput,
                            onValueChange = { tareInput = it },
                            label = Translations.tr("weigh_tare_input_label", lang),
                            placeholder = Translations.tr("weigh_tare_placeholder", lang),
                            isDark = currentTheme.isDark,
                            accentColor = Color(0xFF0284C7),
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
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(tareFocusRequester)
                                .testTag("input_weigh_tare")
                        )

                        // خانة وزن الممتلئ الغائرة (NeumorphicRecessedField)
                        NeumorphicRecessedField(
                            value = grossInput,
                            onValueChange = { grossInput = it },
                            label = Translations.tr("weigh_gross_input_label", lang),
                            placeholder = Translations.tr("weigh_gross_placeholder", lang),
                            isDark = currentTheme.isDark,
                            accentColor = weighAccent,
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
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(grossFocusRequester)
                                .testTag("input_weigh_gross")
                        )

                        // زر [حفظ ✓] كبسولة ثلاثية الأبعاد
                        PremiumPillButton(
                            text = Translations.tr("weigh_save_btn", lang),
                            onClick = { submitCurrentWeighRow() },
                            enabled = tareInput.isNotBlank() || grossInput.isNotBlank(),
                            variant = PremiumPillVariant.PRIMARY,
                            fontSize = 13.sp,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            leadingIcon = {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            },
                            modifier = Modifier
                                .height(54.dp)
                                .testTag("btn_save_weigh_entry")
                        )
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
fun CalmFinancialItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val isDark = LocalAppAppearanceState.current.isDark
    val labelColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    Column(modifier = modifier.padding(vertical = 2.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = labelColor,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.5.sp,
            color = color,
            maxLines = 1,
            softWrap = false
        )
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

    // Feed-specific fields when "علف" (Feed) chip is selected in Add Expense Dialog:
    // Never pre-fill with "40" or "8000" so user input never auto-multiplies by 8000 (e.g., 6000 * 8000 = 48,000,000).
    var feedDesignation by remember { mutableStateOf(Translations.tr("feed_designation_default", lang)) }
    var feedQuintalsText by remember { mutableStateOf("") }
    var feedPricePerQuintalText by remember { mutableStateOf("") }
    var customExpenseOrIncomeName by remember { mutableStateOf("") }

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
    val dialogBg = appearance.theme.cardColor
    val primaryText = appearance.theme.headingColor
    val secondaryText = appearance.theme.secondaryTextColor
    val accentEmerald = appearance.accentColor
    val parsedQuintals = (feedQuintalsText.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val parsedUnitPrice = (feedPricePerQuintalText.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val autoCalculatedFeedAmount = parsedQuintals * parsedUnitPrice
    val addedFeedKg = parsedQuintals * 100.0
    val curr = Translations.tr("currency", lang)
    val kgUnit = Translations.tr("kg_unit", lang)
    val quintalUnit = Translations.tr("quintal_unit", lang)

    val txScope = rememberCoroutineScope()
    val txShakeOffset = remember { Animatable(0f) }
    var txValidationError by remember { mutableStateOf(false) }

    AlertDialog(
        containerColor = dialogBg,
        shape = RoundedCornerShape(appearance.cornerRadiusDp.dp),
        modifier = Modifier.darwinManuscriptBorder(cornerRadius = appearance.cornerRadiusDp.dp, isDark = isDark),
        onDismissRequest = onDismiss,
        title = {
            Text(title, fontWeight = FontWeight.Bold, color = if (type == TransactionType.INCOME) accentEmerald else ActionRed)
        },
        text = {
            Column(
                modifier = Modifier.offset(x = txShakeOffset.value.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(Translations.tr("category_label", lang), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = accentEmerald)
                WrapFilters(
                    categories = categories,
                    selected = selectedCategory,
                    onSelect = { newCat ->
                        if (newCat != selectedCategory) {
                            selectedCategory = newCat
                            txValidationError = false
                        }
                    }
                )

                if (isFeedExpense) {
                    NeumorphicRecessedField(
                        value = feedDesignation,
                        onValueChange = { feedDesignation = it },
                        label = Translations.tr("feed_designation_label", lang),
                        isDark = isDark,
                        accentColor = accentEmerald,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_feed_designation")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NeumorphicRecessedField(
                            value = feedQuintalsText,
                            onValueChange = {
                                feedQuintalsText = it.filter { ch -> ch.isDigit() || ch == '.' }
                                txValidationError = false
                            },
                            label = Translations.tr("feed_qty_quintals_label", lang),
                            placeholder = "40",
                            isDark = isDark,
                            accentColor = if (txValidationError && parsedQuintals <= 0.0) Color(0xFFEF4444) else accentEmerald,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_feed_quintals")
                        )

                        NeumorphicRecessedField(
                            value = feedPricePerQuintalText,
                            onValueChange = {
                                feedPricePerQuintalText = it.filter { ch -> ch.isDigit() || ch == '.' }
                                txValidationError = false
                            },
                            label = Translations.tr("feed_price_quintal_label", lang),
                            placeholder = "8000",
                            isDark = isDark,
                            accentColor = if (txValidationError && parsedUnitPrice <= 0.0) Color(0xFFEF4444) else accentEmerald,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_feed_unit_price")
                        )
                    }

                    NeumorphicRecessedDisplay(
                        isDark = isDark,
                        contentPadding = PaddingValues(12.dp)
                    ) {
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
                } else {
                    // Always display a text field above the amount field so the Owner can write what the expense/income is for (e.g. نشارة خشب، صهريج ماء، تصليح، كهرباء...)
                    NeumorphicRecessedField(
                        value = customExpenseOrIncomeName,
                        onValueChange = { customExpenseOrIncomeName = it },
                        label = if (type == TransactionType.EXPENSE) {
                            Translations.tr("expense_custom_name_label", lang)
                        } else {
                            Translations.tr("income_custom_name_label", lang)
                        },
                        placeholder = if (type == TransactionType.EXPENSE) {
                            Translations.tr("expense_custom_name_placeholder", lang)
                        } else "",
                        isDark = isDark,
                        accentColor = accentEmerald,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_tx_custom_designation")
                    )

                    NeumorphicRecessedField(
                        value = amountText,
                        onValueChange = {
                            amountText = it.filter { ch -> ch.isDigit() || ch == '.' }
                            txValidationError = false
                        },
                        label = Translations.tr("amount_label", lang),
                        isDark = isDark,
                        accentColor = if (txValidationError) Color(0xFFEF4444) else accentEmerald,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_tx_amount")
                    )
                }
            }
        },
        confirmButton = {
            PremiumPillButton(
                text = Translations.tr("confirm_btn", lang),
                onClick = {
                    if (isFeedExpense) {
                        if (autoCalculatedFeedAmount > 0.0 && addedFeedKg > 0.0) {
                            val cleanDesig = feedDesignation.trim().ifEmpty { Translations.tr("feed_designation_default", lang) }
                            val qShort = Translations.tr("quintal_short", lang)
                            val formattedCat = "🌾 $cleanDesig (${Translations.formatQuintalNumber(parsedQuintals)} $qShort)"
                            onConfirm(formattedCat, autoCalculatedFeedAmount, addedFeedKg)
                        } else {
                            txValidationError = true
                            txScope.launch {
                                listOf(-12f, 12f, -8f, 8f, 0f).forEach { target ->
                                    txShakeOffset.animateTo(target, animationSpec = tween(45))
                                }
                            }
                        }
                    } else {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            val cleanCustomName = customExpenseOrIncomeName.trim()
                            val finalCategory = if (cleanCustomName.isNotEmpty()) {
                                "$selectedCategory • $cleanCustomName"
                            } else {
                                selectedCategory
                            }
                            onConfirm(finalCategory, amt, 0.0)
                        } else {
                            txValidationError = true
                            txScope.launch {
                                listOf(-12f, 12f, -8f, 8f, 0f).forEach { target ->
                                    txShakeOffset.animateTo(target, animationSpec = tween(45))
                                }
                            }
                        }
                    }
                },
                variant = if (type == TransactionType.INCOME) PremiumPillVariant.SALES_INCOME else PremiumPillVariant.PRIMARY,
                modifier = Modifier
                    .height(44.dp)
                    .testTag("btn_confirm_transaction")
            )
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
                                    "${String.format(java.util.Locale.US, "%,.0f", row.totalAmountDzd)} $curr",
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

internal data class MedicineLedgerRowItem(
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
            sharedWhatsAppPreview?.let { previewText ->
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
                            previewText,
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
                        Text(
                            "🗓️ ${Translations.tr("active_cycle_label", lang)} • $dateText",
                            fontSize = 12.sp,
                            color = if (appearance.isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                        )
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                Translations.getLocalizedFarmName(farm.id, lang, farm.name),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = currentTheme.headingColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )
                            Surface(
                                shape = btnShape,
                                color = appearance.accentContainerColor
                            ) {
                                Text(
                                    "${Translations.tr("liveability", lang)}: 100%",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = appearance.accentColor,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                        Text(
                            Translations.getLocalizedFarmLocation(farm.id, lang, farm.location),
                            fontSize = 12.sp,
                            color = if (currentTheme.isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
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

    val liveReports = AgritechMockDb.resolveReportsForFarmAndCycle(
        farmId = farm.id,
        cycleId = cycle.id,
        extraReports = reports
    )
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

    selectedDayReportForDetails?.let { selectedReport ->
        val activeReport = liveReports.firstOrNull { it.dayNumber == selectedReport.dayNumber }
            ?: selectedReport
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
                            "${Translations.tr("vet_workspace_title", lang)} • ${Translations.tr("active_cycle_label", lang)}",
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
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    // Table Header with balanced weights so "نفوق" and all columns fit on phone screens without clipping
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(currentTheme.bgColor, RoundedCornerShape(8.dp))
                                            .padding(vertical = 10.dp, horizontal = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            Translations.tr("th_day", lang),
                                            modifier = Modifier.weight(1.15f),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = primaryTextColor,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                        Text(
                                            Translations.tr("th_feed", lang),
                                            modifier = Modifier.weight(1f),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = primaryTextColor,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                        Text(
                                            Translations.tr("th_water", lang),
                                            modifier = Modifier.weight(1f),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = primaryTextColor,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                        Text(
                                            Translations.tr("th_mortality", lang),
                                            modifier = Modifier.weight(0.95f),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = primaryTextColor,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                        Text(
                                            Translations.tr("th_temp", lang),
                                            modifier = Modifier.weight(0.9f),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = primaryTextColor,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
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
                                                        .weight(1.15f)
                                                        .clickable { selectedDayReportForDetails = r }
                                                        .padding(horizontal = 2.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        "${Translations.tr("day_prefix", lang)} ${r.dayNumber}",
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 11.5.sp,
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
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = if (isUrgentDay) ActionRed else activeAccent,
                                                                maxLines = 1,
                                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                val cellTextColor = if (currentTheme.isDark) Color.White else Color(0xFF212121)
                                                Text(
                                                    "${Translations.formatQuintalNumber(r.feedIntakeKg / 100.0)} $qShort",
                                                    modifier = Modifier.weight(1f),
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = cellTextColor,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    String.format(java.util.Locale.US, "%.0f", r.waterIntakeLiters),
                                                    modifier = Modifier.weight(1f),
                                                    fontSize = 11.5.sp,
                                                    color = cellTextColor,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    "${r.mortalityCount}",
                                                    modifier = Modifier.weight(0.95f),
                                                    fontSize = 11.5.sp,
                                                    fontWeight = if (r.mortalityCount > 3) FontWeight.ExtraBold else FontWeight.Normal,
                                                    color = if (r.mortalityCount > 3) ActionRed else cellTextColor,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    r.temperature?.let { String.format(java.util.Locale.US, "%.1f°", it) } ?: "—",
                                                    modifier = Modifier.weight(0.9f),
                                                    fontSize = 11.5.sp,
                                                    color = cellTextColor,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1
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
                        it.farmId == farm.id && (
                            it.category.contains("أدوية") ||
                                it.category.contains("لقاح") ||
                                it.category.contains("بيطر") ||
                                it.category.contains("MEDICATION", ignoreCase = true) ||
                                it.category.contains("Vet", ignoreCase = true)
                            )
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
    val dialogBg = currentTheme.cardColor
    val subtleBg = currentTheme.bgColor
    val primaryText = currentTheme.headingColor
    val secondaryText = currentTheme.secondaryTextColor
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
        modifier = Modifier
            .darwinManuscriptBorder(appearance, isDark)
            .testTag("day_medical_details_sheet"),
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
                    "${Translations.getLocalizedFarmName(farm.id, lang, farm.name)} • ${Translations.tr("active_cycle_label", lang)}",
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
                            report.temperature?.let { String.format(java.util.Locale.US, "%.1f°C", it) } ?: "—",
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
                            LuminousOutlinedTextField(
                                value = medNameInput,
                                onValueChange = { medNameInput = it },
                                label = { Text(Translations.tr("vet_med_name_label", lang), fontSize = 11.5.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_day_med_name")
                            )
                            LuminousOutlinedTextField(
                                value = dosageInput,
                                onValueChange = { dosageInput = it },
                                label = { Text(Translations.tr("vet_dosage_instructions_label", lang), fontSize = 11.5.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_day_med_dosage")
                            )
                            LuminousOutlinedTextField(
                                value = clinicalNoteInput,
                                onValueChange = { clinicalNoteInput = it },
                                label = { Text(Translations.tr("vet_clinical_note_label", lang), fontSize = 11.5.sp) },
                                singleLine = false,
                                maxLines = 3,
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
                .darwinPaperBackground(appearance, currentTheme.isDark)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .darwinManuscriptBorder(appearance, currentTheme.isDark),
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
                            Text("${Translations.tr("batch_label", lang)}: ${String.format(java.util.Locale.US, "%,d", cycle.totalBirdsPlaced)} 🐥", fontSize = 12.sp, color = currentTheme.secondaryTextColor)
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .darwinManuscriptBorder(appearance, currentTheme.isDark),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .darwinManuscriptBorder(appearance, currentTheme.isDark),
                        shape = cardShape,
                        colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("${Translations.tr("log_today_prefix", lang)} $currentDay 🗓️", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = currentTheme.headingColor)

                            LuminousOutlinedTextField(
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
                                    .testTag("worker_input_feed_quintals")
                            )
                            LuminousOutlinedTextField(value = waterText, onValueChange = { waterText = it; workerValidationError = null }, label = { Text(Translations.tr("th_water", lang)) }, placeholder = { Text("500") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            LuminousOutlinedTextField(value = mortalityText, onValueChange = { mortalityText = it; workerValidationError = null }, label = { Text(Translations.tr("th_mortality", lang)) }, placeholder = { Text("0") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            LuminousOutlinedTextField(value = tempText, onValueChange = { tempText = it }, label = { Text(Translations.tr("th_temp", lang)) }, placeholder = { Text("30.0") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            LuminousOutlinedTextField(value = gasText, onValueChange = { gasText = it }, label = { Text(Translations.tr("th_gas", lang)) }, placeholder = { Text("2.5") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                            workerValidationError?.let { errMsg ->
                                Text(
                                    text = errMsg,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ActionRed
                                )
                            }

                            // Supervisor Medicine in Water Confirmation Row ("💊 تأكيد إعطاء الدواء في الماء اليوم (شرب الدواء ✓)")
                            Surface(
                                shape = RoundedCornerShape(appearance.cornerRadiusDp.dp),
                                color = if (workerMedicineGiven) appearance.accentContainerColor.copy(alpha = 0.65f)
                                else currentTheme.bgColor,
                                border = BorderStroke(
                                    1.dp,
                                    if (workerMedicineGiven) appearance.accentColor.copy(alpha = 0.55f)
                                    else currentTheme.secondaryTextColor.copy(alpha = 0.25f)
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
                                        LuminousOutlinedTextField(
                                            value = workerMedicineName,
                                            onValueChange = { workerMedicineName = it },
                                            label = { Text(Translations.tr("medicine_water_name_input_label", lang), fontSize = 11.sp) },
                                            singleLine = true,
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
                                        val temp: Double? = tempText.trim().toDoubleOrNull()
                                        val gas = (gasText.trim().toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
                                        val resolvedWorkerMed = if (workerMedicineGiven) {
                                            workerMedicineName.trim().ifBlank { defaultWorkerMedName }
                                        } else {
                                            ""
                                        }

                                        workerScope.launch {
                                            delay(350)
                                            onSubmitReport(
                                                DailyReportModel(
                                                    id = AgritechFirestoreRepository.buildCanonicalDailyReportId(
                                                        farmId = farm.id,
                                                        cycleId = cycle.id,
                                                        dayNumber = currentDay
                                                    ),
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
