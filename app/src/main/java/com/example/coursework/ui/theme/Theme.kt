package com.example.coursework.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    background = BgDark,
    surface = BgDark,
    primary = BtnPrimary,
    secondary = BtnPrimaryBlue,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onSurface = TextSecondary
)

@Composable
fun RunTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = AppTypography,
        content = content
    )
}