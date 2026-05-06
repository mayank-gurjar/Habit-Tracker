package com.habittracker.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SportyDarkColorScheme = darkColorScheme(
    primary = SportyPrimary,
    onPrimary = SportyOnPrimary,
    primaryContainer = SportyPrimaryContainer,
    onPrimaryContainer = SportyOnPrimaryContainer,
    secondary = SportySecondary,
    onSecondary = SportyOnSecondary,
    background = SportyBackground,
    onBackground = SportyTextPrimary,
    surface = SportySurface,
    onSurface = SportyTextPrimary,
    surfaceVariant = SportySurfaceVariant,
    onSurfaceVariant = SportyTextSecondary
)

@Composable
fun HabitTrackerTheme(
    content: @Composable () -> Unit
) {
    // We force the Sporty Dark Theme for a premium classic look
    MaterialTheme(
        colorScheme = SportyDarkColorScheme,
        typography = AppTypography,
        content = content
    )
}
