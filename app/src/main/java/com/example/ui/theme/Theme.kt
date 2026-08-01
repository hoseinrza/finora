package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldGreen,
    onPrimary = Color.White,
    primaryContainer = EmeraldGreenDark,
    onPrimaryContainer = TextPrimaryDark,
    secondary = CyanAccent,
    onSecondary = Color.White,
    secondaryContainer = NavyCardBgElevated,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = PurpleAccent,
    onTertiary = Color.White,
    background = NavyDarkBg,
    onBackground = TextPrimaryDark,
    surface = NavyCardBg,
    onSurface = TextPrimaryDark,
    surfaceVariant = NavyCardBorder,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainer = NavyCardBgElevated,
    surfaceContainerHigh = NavyCardBgElevated,
    surfaceContainerLow = NavyDarkBg,
    outline = NavyCardBorder,
    outlineVariant = NavyCardBorder,
    error = RoseRed,
    onError = Color.White,
    errorContainer = RoseRedDark,
    onErrorContainer = RoseRedLight,
)

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = LightSurfaceElevated,
    onPrimaryContainer = TextPrimaryLight,
    secondary = EmeraldGreen,
    onSecondary = Color.White,
    secondaryContainer = EmeraldGreenLight,
    onSecondaryContainer = EmeraldGreenDark,
    tertiary = CyanAccent,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceBorder,
    onSurfaceVariant = TextSecondaryLight,
    surfaceContainer = LightSurfaceElevated,
    surfaceContainerHigh = LightSurfaceElevated,
    surfaceContainerLow = LightBackground,
    outline = LightSurfaceBorder,
    outlineVariant = LightSurfaceBorder,
    error = RoseRed,
    onError = Color.White,
    errorContainer = RoseRedLight,
    onErrorContainer = RoseRedDark,
)

@Composable
fun FinoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
