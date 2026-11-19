package com.develotter.calendarview.locale

/**
 * UI language for the labels the library formats itself: month names, weekday labels and digits.
 *
 * Every engine accepts `null` instead of an instance, which keeps the platform default locale and
 * therefore the behaviour of earlier versions. The engines ship label tables for the predefined
 * languages; any other [languageTag] falls back to English labels. A locale never changes layout
 * direction by itself: the host provides `LayoutDirection`, optionally derived from the language
 * through [isRightToLeft] or [ProvideCalendarLocaleLayoutDirection].
 */
data class CalendarLocale(val languageTag: String) {
    /** Lowercase ISO 639 language subtag, for example `fa` for `fa-AF`. */
    val language: String get() = languageTag.substringBefore('-').lowercase()

    /**
     * Whether the language is written right to left, decided by a fixed list of language subtags
     * (`ar`, `fa`, `prs`, `he`, `iw`, `ur`, `ps`, `sd`, `ug`, `yi`, `dv`). It does not inspect the
     * script subtag, so a right-to-left language written in Latin script still reports `true`.
     */
    val isRightToLeft: Boolean get() = language in RIGHT_TO_LEFT_LANGUAGES

    companion object {
        private val RIGHT_TO_LEFT_LANGUAGES = setOf(
            "ar", "fa", "prs", "he", "iw", "ur", "ps", "sd", "ug", "yi", "dv",
        )

        val English = CalendarLocale("en")
        val German = CalendarLocale("de")
        val Persian = CalendarLocale("fa")
        val Arabic = CalendarLocale("ar")

        /**
         * Language subtags with their own label tables in every engine, in the order `en`, `de`,
         * `fa`, `ar`. Any other language renders English labels and Western digits.
         */
        val supportedLanguages: List<String> = listOf("en", "de", "fa", "ar")

        /**
         * The platform's current language as a [CalendarLocale]. The value is read on each call;
         * it is not observed. A language outside [supportedLanguages] still renders English labels,
         * so pass `null` instead to keep the platform formatters.
         */
        fun systemDefault(): CalendarLocale = CalendarLocale(platformLanguageTag())
    }
}
