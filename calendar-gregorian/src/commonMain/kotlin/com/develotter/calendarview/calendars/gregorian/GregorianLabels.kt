package com.develotter.calendarview.calendars.gregorian

import com.develotter.calendarview.locale.CalendarLocale

/**
 * Gregorian label tables for an explicitly selected [CalendarLocale].
 *
 * Only used when the caller passes a locale; `null` keeps `GregorianPlatformFormatter` and its
 * platform default. Unknown language tags fall back to English.
 */
internal object GregorianLabels {
    private val MONTHS: Map<String, List<String>> = mapOf(
        "en" to listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December",
        ),
        "de" to listOf(
            "Januar", "Februar", "März", "April", "Mai", "Juni",
            "Juli", "August", "September", "Oktober", "November", "Dezember",
        ),
        "fa" to listOf(
            "ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن",
            "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر",
        ),
        "ar" to listOf(
            "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
            "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر",
        ),
    )

    private val SHORT_MONTHS: Map<String, List<String>> = mapOf(
        "en" to listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
        ),
        "de" to listOf(
            "Jan.", "Feb.", "März", "Apr.", "Mai", "Juni",
            "Juli", "Aug.", "Sept.", "Okt.", "Nov.", "Dez.",
        ),
        "fa" to listOf(
            "ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن",
            "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر",
        ),
        "ar" to listOf(
            "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
            "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر",
        ),
    )

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
        return table(MONTHS, locale)[month - 1]
    }

    /** Abbreviated month name of [month] (`1..12`), or the full name when the language has none. */
    fun shortMonthName(locale: CalendarLocale, month: Int): String {
        require(month in 1..12) { "month must be in 1..12 but was $month" }
        return table(SHORT_MONTHS, locale)[month - 1]
    }

    /** Month caption such as `April 2026` or `آوریل ۲۰۲۶`. */
    fun monthYear(locale: CalendarLocale, month: Int, year: Int): String =
        "${monthName(locale, month)} ${number(locale, year)}"

    /** Localized full date label including weekday, day, month name and year. */
    fun fullDate(locale: CalendarLocale, epochDay: Long): String {
        val month = GregorianYearMonth.fromEpochDay(epochDay)
        val dayOfMonth = (epochDay - month.atDay(1)).toInt() + 1
        val weekday = weekdayFull(locale, GregorianYearMonth.isoWeekdayFromEpochDay(epochDay))
        val day = number(locale, dayOfMonth)
        val year = number(locale, month.year)
        val monthName = monthName(locale, month.month)
        return when (languageOf(locale)) {
            "de" -> "$weekday, $day. $monthName $year"
            "fa", "ar" -> "$weekday، $day $monthName $year"
            else -> "$weekday, $day $monthName $year"
        }
    }

    /** Short day and month label such as `20 Apr` or `۲۰ آوریل`. */
    fun shortDayMonth(locale: CalendarLocale, epochDay: Long): String {
        val month = GregorianYearMonth.fromEpochDay(epochDay)
        val dayOfMonth = (epochDay - month.atDay(1)).toInt() + 1
        return "${number(locale, dayOfMonth)} ${shortMonthName(locale, month.month)}"
    }

    /** Short day, month and year label such as `20 Apr 2026`. */
    fun shortDayMonthYear(locale: CalendarLocale, epochDay: Long): String {
        val month = GregorianYearMonth.fromEpochDay(epochDay)
        return "${shortDayMonth(locale, epochDay)} ${number(locale, month.year)}"
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

    private fun languageOf(locale: CalendarLocale): String =
        locale.language.takeIf { it in MONTHS } ?: "en"

    private fun table(tables: Map<String, List<String>>, locale: CalendarLocale): List<String> =
        tables.getValue(languageOf(locale))
}
