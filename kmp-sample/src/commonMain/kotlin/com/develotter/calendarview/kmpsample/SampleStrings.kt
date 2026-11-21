package com.develotter.calendarview.kmpsample

import com.develotter.calendarview.locale.CalendarLocale

/**
 * Localized labels of the sample shell. Screen contents come from the library engines, which
 * receive the same [CalendarLocale].
 */
internal object SampleStrings {
    fun appTitle(language: CalendarLocale): String = "CalendarView"

    fun menu(language: CalendarLocale): String =
        pick(language, en = "Menu", de = "Menü", fa = "منو", ar = "القائمة")

    fun close(language: CalendarLocale): String =
        pick(language, en = "Close", de = "Schließen", fa = "بستن", ar = "إغلاق")

    fun jumpToMonth(language: CalendarLocale): String =
        pick(language, en = "Jump to month", de = "Zum Monat springen", fa = "پرش به ماه", ar = "الانتقال إلى الشهر")

    fun previousYear(language: CalendarLocale): String =
        pick(language, en = "Previous year", de = "Vorheriges Jahr", fa = "سال قبل", ar = "السنة السابقة")

    fun nextYear(language: CalendarLocale): String =
        pick(language, en = "Next year", de = "Nächstes Jahr", fa = "سال بعد", ar = "السنة التالية")

    fun heatmapLevel(language: CalendarLocale, level: Int): String = pick(
        language,
        en = "Level $level",
        de = "Stufe $level",
        fa = "سطح $level",
        ar = "المستوى $level",
    )

    fun pageTitle(page: DemoPage, language: CalendarLocale): String = when (page) {
        DemoPage.Home -> pick(language, "Home", "Start", "خانه", "الرئيسية")
        DemoPage.Calendars -> pick(language, "Calendars", "Kalender", "تقویم‌ها", "التقاويم")
        DemoPage.Playground -> pick(language, "Playground", "Spielwiese", "زمین بازی", "الملعب")
        DemoPage.Heatmaps -> pick(language, "Heatmaps", "Heatmaps", "نقشه حرارتی", "الخرائط الحرارية")
        DemoPage.Settings -> pick(language, "Settings", "Einstellungen", "تنظیمات", "الإعدادات")
    }

    fun settingsTitle(language: CalendarLocale): String =
        pick(language, en = "Settings", de = "Einstellungen", fa = "تنظیمات", ar = "الإعدادات")

    fun languageLabel(language: CalendarLocale): String =
        pick(language, en = "Language", de = "Sprache", fa = "زبان", ar = "اللغة")

    /** Endonym of [value]; the same in every UI language. */
    fun languageName(value: CalendarLocale): String = when (value.language) {
        "de" -> "Deutsch"
        "fa" -> "فارسی"
        "ar" -> "العربية"
        else -> "English"
    }

    fun themeLabel(language: CalendarLocale): String =
        pick(language, en = "Theme", de = "Design", fa = "پوسته", ar = "المظهر")

    fun themeModeName(mode: AppThemeMode, language: CalendarLocale): String = when (mode) {
        AppThemeMode.System -> pick(language, "System", "System", "سیستم", "النظام")
        AppThemeMode.Light -> pick(language, "Light", "Hell", "روشن", "فاتح")
        AppThemeMode.Dark -> pick(language, "Dark", "Dunkel", "تیره", "داكن")
    }

    fun paletteLabel(language: CalendarLocale): String =
        pick(language, en = "Accent", de = "Akzentfarbe", fa = "رنگ تأکیدی", ar = "لون التمييز")

    fun paletteName(palette: AppThemePalette, language: CalendarLocale): String = when (palette) {
        AppThemePalette.Blue -> pick(language, "Blue", "Blau", "آبی", "أزرق")
        AppThemePalette.Green -> pick(language, "Green", "Grün", "سبز", "أخضر")
        AppThemePalette.Amber -> pick(language, "Amber", "Bernstein", "کهربایی", "كهرماني")
    }

    private fun pick(
        language: CalendarLocale,
        en: String,
        de: String,
        fa: String,
        ar: String,
    ): String = when (language.language) {
        "de" -> de
        "fa" -> fa
        "ar" -> ar
        else -> en
    }
}
