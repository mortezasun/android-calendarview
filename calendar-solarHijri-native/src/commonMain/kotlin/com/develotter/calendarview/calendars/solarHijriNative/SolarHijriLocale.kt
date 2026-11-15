package com.develotter.calendarview.calendars.solarHijriNative

/**
 * Month names and number formatting for Solar Hijri captions.
 *
 * Persian (`fa`) uses the Iranian names, Dari (`fa-AF`, `prs`) the Afghan names; every other
 * locale gets English transliterations. Language, country and digits come from the platform's
 * default locale, like the rest of the library.
 */
object SolarHijriLocale {
    private val ENGLISH = listOf(
        "Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar",
        "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand",
    )
    private val PERSIAN = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
    )
    private val DARI = listOf(
        "حمل", "ثور", "جوزا", "سرطان", "اسد", "سنبله",
        "میزان", "عقرب", "قوس", "جدی", "دلو", "حوت",
    )

    /** Name of [month] (`1..12`) for the default locale. */
    fun monthName(month: Int): String {
        require(month in 1..12) { "month must be in 1..12 but was $month" }
        val language = SolarHijriPlatform.localeLanguage()
        val country = SolarHijriPlatform.localeCountry()
        val names = when {
            language == "prs" -> DARI
            language == "fa" && country == "AF" -> DARI
            language == "fa" -> PERSIAN
            else -> ENGLISH
        }
        return names[month - 1]
    }

    /** Formats [value] without digit grouping using the default locale's digits. */
    fun formatNumber(value: Int): String = SolarHijriPlatform.formatDayNumber(value)

    /** Month caption such as `Farvardin 1404`. */
    fun formatMonthYear(year: Int, month: Int): String =
        "${monthName(month)} ${formatNumber(year)}"
}
