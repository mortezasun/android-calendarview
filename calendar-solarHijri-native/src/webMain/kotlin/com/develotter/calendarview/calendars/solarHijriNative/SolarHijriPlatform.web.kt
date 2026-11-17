package com.develotter.calendarview.calendars.solarHijriNative

internal expect fun webSolarHijriLocalYear(): Int

internal expect fun webSolarHijriLocalMonth(): Int

internal expect fun webSolarHijriLocalDay(): Int

internal expect fun webSolarHijriLocaleLanguage(): String

internal expect fun webSolarHijriLocaleCountry(): String

internal expect fun webSolarHijriFormatDayNumber(value: Int): String

internal expect fun webSolarHijriWeekdayShortLabel(epochMs: Double): String

internal expect fun webSolarHijriWeekdayFullLabel(epochMs: Double): String

internal actual object SolarHijriPlatform {
    private const val MILLIS_PER_DAY = 86_400_000.0
    private const val NOON_MILLIS = 43_200_000.0

    actual fun currentEpochDay(): Long = gregorianToEpochDay(
        year = webSolarHijriLocalYear(),
        month = webSolarHijriLocalMonth(),
        day = webSolarHijriLocalDay(),
    )

    actual fun localeLanguage(): String = webSolarHijriLocaleLanguage()

    actual fun localeCountry(): String = webSolarHijriLocaleCountry()

    actual fun formatDayNumber(value: Int): String = webSolarHijriFormatDayNumber(value)

    actual fun weekdayShortLabel(isoWeekday: Int): String =
        webSolarHijriWeekdayShortLabel(utcNoonMillis(isoWeekday.toLong() + 3L))

    actual fun weekdayFullLabel(isoWeekday: Int): String =
        webSolarHijriWeekdayFullLabel(utcNoonMillis(isoWeekday.toLong() + 3L))

    private fun utcNoonMillis(epochDay: Long): Double =
        epochDay.toDouble() * MILLIS_PER_DAY + NOON_MILLIS

    private fun gregorianToEpochDay(year: Int, month: Int, day: Int): Long {
        val y = year.toLong() - if (month <= 2) 1L else 0L
        val era = if (y >= 0) y / 400 else (y - 399) / 400
        val yoe = y - era * 400
        val m = month.toLong()
        val doy = (153 * (m + if (m > 2) -3 else 9) + 2) / 5 + day - 1
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        return era * 146097 + doe - 719468
    }
}
