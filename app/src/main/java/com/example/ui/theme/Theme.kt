package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.models.AppAccentColorOption
import com.example.models.AppCornerStyleOption

@Composable
fun PoultryTheme(
    isDark: Boolean = false,
    accentColor: AppAccentColorOption = AppAccentColorOption.FOREST_GREEN,
    cornerStyle: AppCornerStyleOption = AppCornerStyleOption.ROUNDED,
    themeBgColor: Color? = null,
    themeCardColor: Color? = null,
    themeHeadingColor: Color? = null,
    content: @Composable () -> Unit,
) {
    val activePrimary = if (isDark) accentColor.darkModeColor else accentColor.primaryColor
    val activeContainer = if (isDark) accentColor.darkContainerColor else accentColor.softContainerColor
    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = activePrimary,
            onPrimary = Color.White,
            primaryContainer = activeContainer,
            onPrimaryContainer = Color.White,
            secondary = activePrimary,
            onSecondary = Color.White,
            background = Color(0xFF0F172A),
            onBackground = Color.White,
            surface = Color(0xFF1E293B),
            onSurface = Color.White,
            surfaceVariant = Color(0xFF1E293B),
            onSurfaceVariant = Color(0xFFCBD5E1)
        )
    } else {
        val resolvedBg = themeBgColor ?: IceBlueBg
        val resolvedCard = themeCardColor ?: WhiteCard
        val resolvedHeading = themeHeadingColor ?: Color(0xFF0F172A)
        lightColorScheme(
            primary = activePrimary,
            onPrimary = Color.White,
            primaryContainer = activeContainer,
            onPrimaryContainer = accentColor.primaryColor,
            secondary = activePrimary,
            onSecondary = Color.White,
            background = resolvedBg,
            onBackground = resolvedHeading,
            surface = resolvedCard,
            onSurface = resolvedHeading,
            surfaceVariant = resolvedBg,
            onSurfaceVariant = Color(0xFF475569)
        )
    }

    val radius = cornerStyle.cornerRadiusDp.dp
    val smallRadius = (cornerStyle.cornerRadiusDp * 0.6f).dp.coerceAtLeast(4.dp)
    val dynamicShapes = Shapes(
        extraSmall = RoundedCornerShape(smallRadius),
        small = RoundedCornerShape(smallRadius),
        medium = RoundedCornerShape(radius),
        large = RoundedCornerShape(radius),
        extraLarge = RoundedCornerShape((cornerStyle.cornerRadiusDp + 4).dp)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = dynamicShapes,
        content = content
    )
}
