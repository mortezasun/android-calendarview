package com.develotter.calendarview.kmpsample

/** UI languages offered by the sample shell. */
internal enum class AppLanguage { English, German }

/** Light/dark selection of the sample shell. */
internal enum class AppThemeMode { System, Light, Dark }

/** Accent palette of the sample shell. */
internal enum class AppThemePalette { Blue, Green, Amber }

/** Values owned by the caller and passed to [SampleTheme] and the settings screen. */
internal data class AppSettings(
    val language: AppLanguage = AppLanguage.English,
    val themeMode: AppThemeMode = AppThemeMode.System,
    val palette: AppThemePalette = AppThemePalette.Blue,
)
