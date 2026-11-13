package com.develotter.calendarview.calendars.solarHijriNative

import java.text.NumberFormat
import java.util.Locale

/**
 * Month names and number formatting for Solar Hijri captions.
 *
 * Persian (`fa`) uses the Iranian names, Dari (`fa-AF`, `prs`) the Afghan names; every other
 * locale gets English transliterations. Digits follow [NumberFormat] for the locale, like the
 * rest of the library.
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

    /** Name of [month] (`1..12`) for [locale]. */
    fun monthName(month: Int, locale: Locale): String {
        require(month in 1..12) { "month must be in 1..12 but was $month" }
        val names = when {
            locale.language == "prs" -> DARI
            locale.language == "fa" && locale.country == "AF" -> DARI
            locale.language == "fa" -> PERSIAN
            else -> ENGLISH
        }
        return names[month - 1]
    }

    /** Formats [value] without digit grouping using the digits of [locale]. */
    fun formatNumber(value: Int, locale: Locale): String =
        NumberFormat.getIntegerInstance(locale).apply { isGroupingUsed = false }.format(value)

    /** Month caption such as `Farvardin 1404`. */
    fun formatMonthYear(year: Int, month: Int, locale: Locale): String =
        "${monthName(month, locale)} ${formatNumber(year, locale)}"
}
