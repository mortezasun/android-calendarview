package com.develotter.calendarview.calendars.hijri

import kotlin.math.round

internal expect fun webHijriLocalYear(): Int

internal expect fun webHijriLocalMonth(): Int

internal expect fun webHijriLocalDay(): Int

internal expect fun webHijriPartsFromEpochMs(epochMs: Double): String

internal expect fun webHijriFormatMonthYear(epochMs: Double): String

internal expect fun webHijriFormatShortMonth(epochMs: Double): String

internal expect fun webHijriFormatFullDate(canonicalMs: Double, actualMs: Double): String

internal expect fun webHijriFormatShortDayMonthYear(epochMs: Double): String

internal expect fun webHijriFormatDayNumber(value: Int): String

internal expect fun webHijriFormatWeekdayShort(epochMs: Double): String

internal actual object HijriPlatform {
    private const val MILLIS_PER_DAY = 86_400_000.0
    private const val NOON_MILLIS = 43_200_000.0

    actual fun currentEpochDay(): Long = gregorianToEpochDay(
        year = webHijriLocalYear(),
        month = webHijriLocalMonth(),
        day = webHijriLocalDay(),
    )

    actual fun fromEpochDay(epochDay: Long): HijriDate {
        val raw = webHijriPartsFromEpochMs(utcNoonMillis(epochDay))
        val parts = raw.split('-')
        require(parts.size == 3) { "Unexpected Hijri parts for epochDay $epochDay: $raw" }
        return HijriDate(
            year = parts[0].toInt(),
            month = parts[1].toInt(),
            dayOfMonth = parts[2].toInt(),
        )
    }

    actual fun toEpochDay(date: HijriDate): Long {
        var candidate = round(
            (date.year - 1) * 354.36707 + (date.month - 1) * 29.530588 + date.dayOfMonth - 492149.0,
        ).toLong()
        repeat(6) {
            val probe = fromEpochDay(candidate)
            if (probe == date) return candidate
            val step = (date.year - probe.year) * 354L +
                (date.month - probe.month) * 29L +
                (date.dayOfMonth - probe.dayOfMonth).toLong()
            candidate += if (step == 0L) (if (probe < date) 1L else -1L) else step
        }
        var current = fromEpochDay(candidate)
        if (current < date) {
            repeat(35) {
                candidate += 1L
                current = fromEpochDay(candidate)
                if (current == date) return candidate
                if (current > date) error("Invalid HijriDate: $date")
            }
        } else if (current > date) {
            repeat(35) {
                candidate -= 1L
                current = fromEpochDay(candidate)
                if (current == date) return candidate
                if (current < date) error("Invalid HijriDate: $date")
            }
        } else {
            return candidate
        }
        error("Invalid HijriDate: $date")
    }

    actual fun lengthOfMonth(year: Int, month: Int): Int {
        val start = toEpochDay(HijriDate(year, month, 1))
        val next = if (month == 12) HijriDate(year + 1, 1, 1) else HijriDate(year, month + 1, 1)
        return (toEpochDay(next) - start).toInt()
    }

    actual fun formatMonthYear(year: Int, month: Int): String =
        webHijriFormatMonthYear(utcNoonMillis(toEpochDay(HijriDate(year, month, 1))))

    actual fun formatShortMonth(year: Int, month: Int): String =
        webHijriFormatShortMonth(utcNoonMillis(toEpochDay(HijriDate(year, month, 1))))

    actual fun formatFullDate(date: HijriDate, weekdayEpochDay: Long): String =
        webHijriFormatFullDate(utcNoonMillis(toEpochDay(date)), utcNoonMillis(weekdayEpochDay))

    actual fun formatShortDayMonthYear(epochDay: Long): String =
        webHijriFormatShortDayMonthYear(utcNoonMillis(epochDay))

    actual fun formatDayNumber(day: Int): String = webHijriFormatDayNumber(day)

    actual fun weekdayShortLabel(isoWeekday: Int): String =
        webHijriFormatWeekdayShort(utcNoonMillis(isoWeekday.toLong() + 3L))

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
