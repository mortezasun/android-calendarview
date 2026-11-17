package com.develotter.calendarview.compose

internal expect fun webHeatmapLocalYear(): Int

internal expect fun webHeatmapLocalMonth(): Int

internal expect fun webHeatmapLocalDay(): Int

internal expect fun webHeatmapFormatMonthYear(epochMs: Double): String

internal expect fun webHeatmapFormatFullDate(epochMs: Double): String

internal expect fun webHeatmapFormatWeekdayShort(epochMs: Double): String

internal expect fun webHeatmapFormatPercent(value: Double): String

internal actual object HeatmapPlatformFormatter {
    private const val MILLIS_PER_DAY = 86_400_000.0
    private const val NOON_MILLIS = 43_200_000.0

    actual fun currentEpochDay(): Long = gregorianToEpochDay(
        year = webHeatmapLocalYear(),
        month = webHeatmapLocalMonth(),
        day = webHeatmapLocalDay(),
    )

    actual fun formatMonthYear(epochDay: Long): String =
        webHeatmapFormatMonthYear(utcNoonMillis(epochDay))

    actual fun formatFullDate(epochDay: Long): String =
        webHeatmapFormatFullDate(utcNoonMillis(epochDay))

    actual fun weekdayShortLabel(isoWeekday: Int): String =
        webHeatmapFormatWeekdayShort(utcNoonMillis(isoWeekday.toLong() + 3L))

    actual fun formatPercent(value: Float): String =
        webHeatmapFormatPercent(value.toDouble())

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
