package com.example.fitnesapp.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import com.example.fitnesapp.domain.model.AppThemeMode

private val StandardColors = darkColorScheme(
    primary = StandardPrimary,
    onPrimary = TextPrimary,
    primaryContainer = StandardPrimaryDark,
    secondary = StandardSecondary,
    background = StandardBackground,
    surface = StandardSurface,
    surfaceVariant = StandardSurfaceVariant,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = DangerRed
)

private val SportColors = darkColorScheme(
    primary = SportPrimary,
    onPrimary = TextPrimary,
    primaryContainer = SportPrimaryContainer,
    secondary = SportSecondary,
    background = SportBackground,
    surface = SportSurface,
    surfaceVariant = SportSurfaceVariant,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = DangerRed
)

data class FitnesThemeExtras(
    val glowColor: androidx.compose.ui.graphics.Color,
    val isSport: Boolean
)

val LocalFitnesThemeExtras = staticCompositionLocalOf {
    FitnesThemeExtras(glowColor = AccentOrange.copy(alpha = 0.25f), isSport = false)
}

@Composable
fun FitnesAppTheme(themeMode: AppThemeMode, content: @Composable () -> Unit) {
    val colors = when (themeMode) {
        AppThemeMode.STANDARD -> StandardColors
        AppThemeMode.ORANGE_SPORT -> SportColors
    }
    val extras = when (themeMode) {
        AppThemeMode.STANDARD -> FitnesThemeExtras(glowColor = StandardSecondary.copy(alpha = 0.22f), isSport = false)
        AppThemeMode.ORANGE_SPORT -> FitnesThemeExtras(glowColor = SportGlow, isSport = true)
    }
    CompositionLocalProvider(LocalFitnesThemeExtras provides extras) {
        MaterialTheme(
            colorScheme = colors,
            typography = AppTypography,
            content = content
        )
    }
}
