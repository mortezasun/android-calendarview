package com.develotter.calendarview.calendars.gregorian

internal expect fun webGregorianLocalYear(): Int

internal expect fun webGregorianLocalMonth(): Int

internal expect fun webGregorianLocalDay(): Int

internal expect fun webGregorianDefaultFirstDayOfWeek(): Int

internal expect fun webGregorianFormatMonthYear(epochMs: Double): String

internal expect fun webGregorianFormatFullDate(epochMs: Double): String

internal expect fun webGregorianFormatShortDayMonth(epochMs: Double): String

internal expect fun webGregorianFormatShortDayMonthYear(epochMs: Double): String

internal expect fun webGregorianFormatDayNumber(value: Int): String

internal expect fun webGregorianFormatWeekdayShort(epochMs: Double): String

internal actual object GregorianPlatformFormatter {
    private const val MILLIS_PER_DAY = 86_400_000.0
    private const val NOON_MILLIS = 43_200_000.0

    actual fun currentEpochDay(): Long = GregorianYearMonth.gregorianToEpochDay(
        year = webGregorianLocalYear(),
        month = webGregorianLocalMonth(),
        day = webGregorianLocalDay(),
    )

    actual fun defaultFirstDayOfWeek(): Int = webGregorianDefaultFirstDayOfWeek()

    actual fun formatMonthYear(year: Int, month: Int): String =
        webGregorianFormatMonthYear(utcNoonMillis(GregorianYearMonth.gregorianToEpochDay(year, month, 1)))

    actual fun formatFullDate(epochDay: Long): String =
        webGregorianFormatFullDate(utcNoonMillis(epochDay))

    actual fun formatShortDayMonth(epochDay: Long): String =
        webGregorianFormatShortDayMonth(utcNoonMillis(epochDay))

    actual fun formatShortDayMonthYear(epochDay: Long): String =
        webGregorianFormatShortDayMonthYear(utcNoonMillis(epochDay))

    actual fun formatDayNumber(day: Int): String = webGregorianFormatDayNumber(day)

    actual fun weekdayShortLabel(isoWeekday: Int): String =
        webGregorianFormatWeekdayShort(utcNoonMillis(isoWeekday.toLong() + 3L))

    private fun utcNoonMillis(epochDay: Long): Double =
        epochDay.toDouble() * MILLIS_PER_DAY + NOON_MILLIS
}
