package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private fun buildLightColorScheme(preset: AppThemePreset) = lightColorScheme(
    primary = preset.lightPrimary,
    onPrimary = Color.White,
    primaryContainer = preset.lightPrimaryContainer,
    onPrimaryContainer = preset.lightOnPrimaryContainer,
    secondary = preset.lightSecondary,
    onSecondary = Color.White,
    secondaryContainer = WarmAmberContainer,
    onSecondaryContainer = OnWarmAmberContainer,
    background = WarmIvoryBackground,
    onBackground = InkCharcoal,
    surface = WarmPaperSurface,
    onSurface = InkCharcoal,
    surfaceVariant = WarmSandContainer,
    onSurfaceVariant = InkMuted,
    surfaceContainerLow = WarmSandContainer,
    surfaceContainer = WarmSandContainer,
    surfaceContainerHigh = WarmSandHigh,
    outline = OutlineWarm,
    outlineVariant = Color(0xFFE6DFD4),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

private fun buildDarkColorScheme(preset: AppThemePreset) = darkColorScheme(
    primary = preset.darkPrimary,
    onPrimary = Color(0xFF042622),
    primaryContainer = preset.darkPrimaryContainer,
    onPrimaryContainer = preset.darkOnPrimaryContainer,
    secondary = Color(0xFFFBBF24),
    onSecondary = Color(0xFF332000),
    secondaryContainer = Color(0xFF523400),
    onSecondaryContainer = Color(0xFFFDE68A),
    background = DarkSlateBackground,
    onBackground = WarmSandLight,
    surface = DarkSlateSurface,
    onSurface = WarmSandLight,
    surfaceVariant = DarkSlateContainer,
    onSurfaceVariant = Color(0xFFA7B3AF),
    surfaceContainerLow = DarkSlateSurface,
    surfaceContainer = DarkSlateContainer,
    surfaceContainerHigh = DarkSlateHigh,
    outline = DarkOutline,
    outlineVariant = Color(0xFF2A3532),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val CloudNoteShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun CloudNoteTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    themePreset: AppThemePreset = AppThemePreset.EMERALD,
    backgroundIsDark: Boolean? = null,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val baseScheme = if (useDark) {
        buildDarkColorScheme(themePreset)
    } else {
        buildLightColorScheme(themePreset)
    }

    val colorScheme = when (backgroundIsDark) {
        true -> baseScheme.copy(onBackground = WarmSandLight)
        false -> baseScheme.copy(onBackground = InkCharcoal)
        null -> baseScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = CloudNoteShapes,
        content = content
    )
}
