package com.develotter.calendarview.locale

/**
 * UI language for the labels the library formats itself: month names, weekday labels and digits.
 *
 * Every engine accepts `null` instead of an instance, which keeps the platform default locale and
 * therefore the behaviour of earlier versions. The engines ship label tables for the predefined
 * languages; any other [languageTag] falls back to English labels. Labels never change layout
 * direction, the caller still provides `LayoutDirection`.
 */
data class CalendarLocale(val languageTag: String) {
    /** Lowercase ISO 639 language subtag, for example `fa` for `fa-AF`. */
    val language: String get() = languageTag.substringBefore('-').lowercase()

    companion object {
        val English = CalendarLocale("en")
        val German = CalendarLocale("de")
        val Persian = CalendarLocale("fa")
        val Arabic = CalendarLocale("ar")
    }
}
