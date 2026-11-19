package com.develotter.calendarview.calendars.solarHijriNative

import com.develotter.calendarview.locale.CalendarLocale

/**
 * Solar Hijri label tables for an explicitly selected [CalendarLocale].
 *
 * Only used when the caller passes a locale; `null` keeps [SolarHijriLocale] and its platform
 * default. Month names are the Iranian set for `fa`, the Afghan set for `fa-AF`/`prs`, Arabic
 * script names for `ar`, and English transliterations for `de` and every other tag.
 */
internal object SolarHijriLabels {
    private val WEEKDAYS_SHORT: Map<String, List<String>> = mapOf(
        "en" to listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
        "de" to listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"),
        "fa" to listOf("د", "س", "چ", "پ", "ج", "ش", "ی"),
        "ar" to listOf("إث", "ثل", "أر", "خم", "جم", "سب", "أح"),
    )

    private val WEEKDAYS_FULL: Map<String, List<String>> = mapOf(
        "en" to listOf(
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday",
        ),
        "de" to listOf(
            "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag",
        ),
        "fa" to listOf(
            "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه", "یکشنبه",
        ),
        "ar" to listOf(
            "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت", "الأحد",
        ),
    )

    private val DIGITS: Map<String, String> = mapOf(
        "en" to "0123456789",
        "de" to "0123456789",
        "fa" to "۰۱۲۳۴۵۶۷۸۹",
        "ar" to "٠١٢٣٤٥٦٧٨٩",
    )

    /** Full month name of [month] (`1..12`). */
    fun monthName(locale: CalendarLocale, month: Int): String {
        require(month in 1..12) { "month must be in 1..12 but was $month" }
        return months(locale)[month - 1]
    }

    /** Month caption such as `Farvardin 1404`. */
    fun monthYear(locale: CalendarLocale, year: Int, month: Int): String =
        "${monthName(locale, month)} ${number(locale, year)}"

    /** Localized full date label including weekday, day, month name and year. */
    fun fullDate(locale: CalendarLocale, date: SolarHijriDate): String {
        val weekday = weekdayFull(locale, isoWeekdayOfEpochDay(date.toEpochDay()))
        val day = number(locale, date.day)
        val monthName = monthName(locale, date.month)
        val year = number(locale, date.year)
        return when (languageOf(locale)) {
            "de" -> "$weekday, $day. $monthName $year"
            "fa", "ar" -> "$weekday، $day $monthName $year"
            else -> "$weekday, $day $monthName $year"
        }
    }

    /** Short standalone weekday name for an ISO weekday (`1` = Monday .. `7` = Sunday). */
    fun weekdayShort(locale: CalendarLocale, isoWeekday: Int): String {
        require(isoWeekday in 1..7) { "isoWeekday must be in 1..7 but was $isoWeekday" }
        return table(WEEKDAYS_SHORT, locale)[isoWeekday - 1]
    }

    /** Full weekday name for an ISO weekday (`1` = Monday .. `7` = Sunday). */
    fun weekdayFull(locale: CalendarLocale, isoWeekday: Int): String {
        require(isoWeekday in 1..7) { "isoWeekday must be in 1..7 but was $isoWeekday" }
        return table(WEEKDAYS_FULL, locale)[isoWeekday - 1]
    }

    /** [value] without digit grouping using the language's digits. */
    fun number(locale: CalendarLocale, value: Int): String {
        val digits = DIGITS.getValue(languageOf(locale))
        return value.toString().map { char -> if (char in '0'..'9') digits[char - '0'] else char }
            .joinToString("")
    }

    private fun months(locale: CalendarLocale): List<String> = when {
        locale.languageTag.equals("fa-AF", ignoreCase = true) || locale.language == "prs" ->
            SolarHijriLocale.DARI

        locale.language == "fa" -> SolarHijriLocale.PERSIAN
        locale.language == "ar" -> SolarHijriLocale.ARABIC
        else -> SolarHijriLocale.ENGLISH
    }

    private fun languageOf(locale: CalendarLocale): String =
        if (locale.languageTag.equals("fa-AF", ignoreCase = true) || locale.language == "prs") {
            "fa"
        } else {
            locale.language.takeIf { it in WEEKDAYS_FULL } ?: "en"
        }

    private fun table(tables: Map<String, List<String>>, locale: CalendarLocale): List<String> =
        tables.getValue(languageOf(locale))
}
