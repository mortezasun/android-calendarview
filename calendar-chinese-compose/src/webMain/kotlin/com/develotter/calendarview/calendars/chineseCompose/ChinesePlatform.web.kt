package com.develotter.calendarview.calendars.chineseCompose

import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth

internal expect fun webChineseLocalYear(): Int

internal expect fun webChineseLocalMonth(): Int

internal expect fun webChineseLocalDay(): Int

internal expect fun webChineseDefaultFirstDayOfWeek(): Int

internal expect fun webChineseFormatMonthYear(epochMs: Double): String

internal expect fun webChineseFormatFullDate(epochMs: Double): String

internal expect fun webChineseFormatDayNumber(value: Int): String

internal expect fun webChineseFormatWeekdayShort(epochMs: Double): String

internal actual object ChinesePlatform {
    private const val MILLIS_PER_DAY = 86_400_000.0
    private const val NOON_MILLIS = 43_200_000.0

    actual fun currentEpochDay(): Long = GregorianYearMonth.gregorianToEpochDay(
        year = webChineseLocalYear(),
        month = webChineseLocalMonth(),
        day = webChineseLocalDay(),
    )

    actual fun defaultFirstDayOfWeek(): Int = webChineseDefaultFirstDayOfWeek()

    actual fun formatMonthYear(year: Int, month: Int): String =
        webChineseFormatMonthYear(utcNoonMillis(GregorianYearMonth.gregorianToEpochDay(year, month, 1)))

    actual fun formatFullDate(epochDay: Long): String =
        webChineseFormatFullDate(utcNoonMillis(epochDay))

    actual fun formatDayNumber(value: Int): String = webChineseFormatDayNumber(value)

    actual fun weekdayShortLabel(isoWeekday: Int): String =
        webChineseFormatWeekdayShort(utcNoonMillis(isoWeekday.toLong() + 3L))

    private fun utcNoonMillis(epochDay: Long): Double =
        epochDay.toDouble() * MILLIS_PER_DAY + NOON_MILLIS
}
