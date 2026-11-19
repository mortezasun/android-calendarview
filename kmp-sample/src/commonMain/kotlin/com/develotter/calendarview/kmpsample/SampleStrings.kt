package com.develotter.calendarview.kmpsample

/**
 * Localized labels of the sample shell. Screen contents stay in English because they are
 * library examples; the shell navigation follows [AppLanguage].
 */
internal object SampleStrings {
    fun appTitle(language: AppLanguage): String = "CalendarView"

    fun menu(language: AppLanguage): String = when (language) {
        AppLanguage.English -> "Menu"
        AppLanguage.German -> "Menü"
    }

    fun close(language: AppLanguage): String = when (language) {
        AppLanguage.English -> "Close"
        AppLanguage.German -> "Schließen"
    }

    fun pageTitle(page: DemoPage, language: AppLanguage): String = when (language) {
        AppLanguage.English -> page.label
        AppLanguage.German -> when (page) {
            DemoPage.Home -> "Start"
            DemoPage.Calendars -> "Kalender"
            DemoPage.Playground -> "Spielwiese"
            DemoPage.Heatmaps -> "Heatmaps"
            DemoPage.Settings -> "Einstellungen"
        }
    }

    fun settingsTitle(language: AppLanguage): String = when (language) {
        AppLanguage.English -> "Settings"
        AppLanguage.German -> "Einstellungen"
    }

    fun languageLabel(language: AppLanguage): String = when (language) {
        AppLanguage.English -> "Language"
        AppLanguage.German -> "Sprache"
    }

    fun languageName(value: AppLanguage, language: AppLanguage): String = when (value) {
        AppLanguage.English -> "English"
        AppLanguage.German -> "Deutsch"
    }

    fun themeLabel(language: AppLanguage): String = when (language) {
        AppLanguage.English -> "Theme"
        AppLanguage.German -> "Design"
    }

    fun themeModeName(mode: AppThemeMode, language: AppLanguage): String = when (mode) {
        AppThemeMode.System -> when (language) {
            AppLanguage.English -> "System"
            AppLanguage.German -> "System"
        }

        AppThemeMode.Light -> when (language) {
            AppLanguage.English -> "Light"
            AppLanguage.German -> "Hell"
        }

        AppThemeMode.Dark -> when (language) {
            AppLanguage.English -> "Dark"
            AppLanguage.German -> "Dunkel"
        }
    }

    fun paletteLabel(language: AppLanguage): String = when (language) {
        AppLanguage.English -> "Accent"
        AppLanguage.German -> "Akzentfarbe"
    }

    fun paletteName(palette: AppThemePalette, language: AppLanguage): String = when (palette) {
        AppThemePalette.Blue -> when (language) {
            AppLanguage.English -> "Blue"
            AppLanguage.German -> "Blau"
        }

        AppThemePalette.Green -> when (language) {
            AppLanguage.English -> "Green"
            AppLanguage.German -> "Grün"
        }

        AppThemePalette.Amber -> when (language) {
            AppLanguage.English -> "Amber"
            AppLanguage.German -> "Bernstein"
        }
    }
}
