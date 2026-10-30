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
            background = Color(0xFF0E121A),
            onBackground = Color.White,
            surface = Color(0xFF161B26),
            onSurface = Color.White,
            surfaceVariant = Color(0xFF1E2534),
            onSurfaceVariant = Color(0xFFCBD5E1)
        )
    } else {
        lightColorScheme(
            primary = activePrimary,
            onPrimary = Color.White,
            primaryContainer = activeContainer,
            onPrimaryContainer = accentColor.primaryColor,
            secondary = activePrimary,
            onSecondary = Color.White,
            background = IceBlueBg,
            onBackground = TextDarkGreen,
            surface = WhiteCard,
            onSurface = TextDarkGreen,
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
