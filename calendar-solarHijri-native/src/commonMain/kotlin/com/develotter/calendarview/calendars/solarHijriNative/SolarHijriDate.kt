package com.develotter.calendarview.calendars.solarHijriNative

import androidx.compose.runtime.Immutable

/**
 * Immutable Solar Hijri (Jalali) date with value equality, validated by [SolarHijriEngine].
 *
 * Construction throws [IllegalArgumentException] for a year outside
 * [SolarHijriEngine.MIN_YEAR]..[SolarHijriEngine.MAX_YEAR], a month outside `1..12` or a day
 * that does not exist. Month and year arithmetic clamps the day to the target month length;
 * results outside the supported years throw [IllegalArgumentException]. The epoch-day identity
 * is the absolute day number used by every calendar in this library, so selections and markers
 * are interchangeable with other calendars.
 */
@Immutable
data class SolarHijriDate(val year: Int, val month: Int, val day: Int) : Comparable<SolarHijriDate> {
    init {
        require(year in SolarHijriEngine.MIN_YEAR..SolarHijriEngine.MAX_YEAR) {
            "year must be in ${SolarHijriEngine.MIN_YEAR}..${SolarHijriEngine.MAX_YEAR} but was $year"
        }
        require(month in 1..12) { "month must be in 1..12 but was $month" }
        val length = SolarHijriEngine.daysInMonth(year, month)
        require(day in 1..length) { "day must be in 1..$length for $year-$month but was $day" }
    }

    /** Absolute epoch day of this date. */
    fun toEpochDay(): Long = SolarHijriEngine.toEpochDay(year, month, day)

    /** ISO weekday of this date: `1` = Monday .. `7` = Sunday. */
    fun isoWeekday(): Int = isoWeekdayOfEpochDay(toEpochDay())

    /** Number of days in this date's month. */
    fun lengthOfMonth(): Int = SolarHijriEngine.daysInMonth(year, month)

    /** Whether this date's year is a leap year. */
    fun isLeapYear(): Boolean = SolarHijriEngine.isLeapYear(year)

    fun plusDays(days: Long): SolarHijriDate = fromEpochDay(toEpochDay() + days)

    fun minusDays(days: Long): SolarHijriDate = fromEpochDay(toEpochDay() - days)

    /** Adds whole months, clamping the day to the target month (31 Farvardin + 6 = 30 Mehr). */
    fun plusMonths(months: Long): SolarHijriDate {
        val index = year.toLong() * 12 + (month - 1) + months
        val targetYear = floorDiv(index, 12L)
        require(targetYear in SolarHijriEngine.MIN_YEAR.toLong()..SolarHijriEngine.MAX_YEAR.toLong()) {
            "Target year $targetYear is outside ${SolarHijriEngine.MIN_YEAR}..${SolarHijriEngine.MAX_YEAR}"
        }
        return clamped(targetYear.toInt(), floorMod(index, 12L).toInt() + 1)
    }

    fun minusMonths(months: Long): SolarHijriDate = plusMonths(-months)

    /** Replaces the year, clamping the day (30 Esfand of a leap year becomes 29 otherwise). */
    fun withYear(year: Int): SolarHijriDate {
        require(year in SolarHijriEngine.MIN_YEAR..SolarHijriEngine.MAX_YEAR) {
            "year must be in ${SolarHijriEngine.MIN_YEAR}..${SolarHijriEngine.MAX_YEAR} but was $year"
        }
        return clamped(year, month)
    }

    /** Replaces the month, clamping the day to the new month length. */
    fun withMonth(month: Int): SolarHijriDate {
        require(month in 1..12) { "month must be in 1..12 but was $month" }
        return clamped(year, month)
    }

    /** Replaces the day of month; unlike the other `with` functions this never clamps. */
    fun withDayOfMonth(day: Int): SolarHijriDate = SolarHijriDate(year, month, day)

    override fun compareTo(other: SolarHijriDate): Int = compareValuesBy(this, other, { it.year }, { it.month }, { it.day })

    override fun toString(): String = "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"

    private fun clamped(targetYear: Int, targetMonth: Int): SolarHijriDate =
        SolarHijriDate(targetYear, targetMonth, minOf(day, SolarHijriEngine.daysInMonth(targetYear, targetMonth)))

    companion object {
        /** Same as the constructor. */
        fun of(year: Int, month: Int, day: Int): SolarHijriDate = SolarHijriDate(year, month, day)

        /** Date of [epochDay]; see [SolarHijriEngine.fromEpochDay] for the supported range. */
        fun fromEpochDay(epochDay: Long): SolarHijriDate = SolarHijriEngine.fromEpochDay(epochDay)

        /** Today on the platform clock; inject a fixed epoch day for deterministic tests. */
        fun now(): SolarHijriDate = fromEpochDay(SolarHijriPlatform.currentEpochDay())
    }
}

/** ISO weekday (`1` = Monday .. `7` = Sunday) of an absolute epoch day. */
internal fun isoWeekdayOfEpochDay(epochDay: Long): Int = (((epochDay + 3) % 7 + 7) % 7).toInt() + 1
