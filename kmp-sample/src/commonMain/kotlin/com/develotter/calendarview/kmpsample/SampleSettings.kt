package com.develotter.calendarview.kmpsample

import com.develotter.calendarview.locale.CalendarLocale

/** Languages offered by the sample shell and passed straight to the library engines. */
internal val SampleLanguages: List<CalendarLocale> = listOf(
    CalendarLocale.English,
    CalendarLocale.German,
    CalendarLocale.Persian,
    CalendarLocale.Arabic,
)

/** Light/dark selection of the sample shell. */
internal enum class AppThemeMode { System, Light, Dark }

/** Accent palette of the sample shell. */
internal enum class AppThemePalette { Blue, Green, Amber }

/** Values owned by the caller and passed to [SampleTheme] and the settings screen. */
internal data class AppSettings(
    val language: CalendarLocale = CalendarLocale.English,
    val themeMode: AppThemeMode = AppThemeMode.System,
    val palette: AppThemePalette = AppThemePalette.Blue,
)
