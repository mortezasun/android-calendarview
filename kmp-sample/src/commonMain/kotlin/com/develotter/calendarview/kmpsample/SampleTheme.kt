package com.develotter.calendarview.kmpsample

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Applies the selected palette and light/dark mode around the sample shell. */
@Composable
internal fun SampleTheme(
    settings: AppSettings,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (settings.themeMode) {
        AppThemeMode.System -> isSystemInDarkTheme()
        AppThemeMode.Light -> false
        AppThemeMode.Dark -> true
    }
    val accent = settings.palette.accentColor()
    MaterialTheme(
        colorScheme = if (darkTheme) {
            darkColorScheme(primary = accent)
        } else {
            lightColorScheme(primary = accent)
        },
        content = content,
    )
}

private fun AppThemePalette.accentColor(): Color = when (this) {
    AppThemePalette.Blue -> Color(0xFF1F6FEB)
    AppThemePalette.Green -> Color(0xFF2E7D32)
    AppThemePalette.Amber -> Color(0xFFB26A00)
}
