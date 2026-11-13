package com.develotter.calendarview.calendars.chineseNative

/**
 * Immutable Chinese lunisolar date, validated against [ChineseCalendarEngine].
 *
 * Construction throws [IllegalArgumentException] for a malformed month/day or an unsupported
 * year, and [ChineseDateOutOfRangeException] when the year has no such leap month or the date is
 * outside the documented data range. Day arithmetic clamps to the target month length; a leap
 * month that does not exist in the target year is rejected instead of being replaced silently.
 */
class ChineseDate(
    val year: Int,
    val monthNumber: Int,
    val isLeapMonth: Boolean,
    val dayOfMonth: Int,
) : Comparable<ChineseDate> {
    init {
        val month = ChineseMonth.of(year, monthNumber, isLeapMonth)
        val length = ChineseCalendarEngine.lengthOfMonth(month)
        require(dayOfMonth in 1..length) {
            "dayOfMonth must be in 1..$length for $month but was $dayOfMonth"
        }
    }

    /** Month of this date. */
    val month: ChineseMonth
        get() = ChineseMonth.of(year, monthNumber, isLeapMonth)

    /** Epoch day identity, identical to `java.time.LocalDate.toEpochDay` of the same day. */
    fun toEpochDay(): Long = ChineseCalendarEngine.monthStartEpochDay(month) + dayOfMonth - 1

    /** Day of week, Monday = 0 through Sunday = 6. */
    fun dayOfWeekIndex(): Int = ChineseMonth.dayOfWeekIndex(toEpochDay())

    /** Adds whole days; results outside the supported range throw [ChineseDateOutOfRangeException]. */
    fun plusDays(days: Long): ChineseDate {
        val base = toEpochDay()
        if (days > 0 && base > Long.MAX_VALUE - days) {
            throw ChineseDateOutOfRangeException("day addition overflows Long")
        }
        if (days < 0 && base < Long.MIN_VALUE - days) {
            throw ChineseDateOutOfRangeException("day subtraction overflows Long")
        }
        return ChineseCalendarEngine.fromEpochDay(base + days)
    }

    fun minusDays(days: Long): ChineseDate {
        if (days == Long.MIN_VALUE) throw ChineseDateOutOfRangeException("day subtraction overflows Long")
        return plusDays(-days)
    }

    /**
     * Adds whole months in the actual 12/13-month sequence. The day is clamped to the target
     * month length; a result outside the supported months throws [ChineseDateOutOfRangeException].
     */
    fun plusMonths(months: Long): ChineseDate {
        val current = ChineseCalendarEngine.indexOf(month)
        if (months > 0 && current > Long.MAX_VALUE - months) {
            throw ChineseDateOutOfRangeException("month addition overflows Long")
        }
        if (months < 0 && current < Long.MIN_VALUE - months) {
            throw ChineseDateOutOfRangeException("month subtraction overflows Long")
        }
        val targetIndex = current + months
        val target = ChineseCalendarEngine.monthAtIndex(targetIndex)
        return ChineseDate(
            year = target.year,
            monthNumber = target.monthNumber,
            isLeapMonth = target.isLeapMonth,
            dayOfMonth = minOf(dayOfMonth, target.lengthOfMonth),
        )
    }

    fun minusMonths(months: Long): ChineseDate {
        if (months == Long.MIN_VALUE) throw ChineseDateOutOfRangeException("month subtraction overflows Long")
        return plusMonths(-months)
    }

    /** Replaces the year, keeping the month identity and clamping the day. */
    fun withYear(year: Int): ChineseDate {
        val target = ChineseMonth.of(year, monthNumber, isLeapMonth)
        return ChineseDate(year, monthNumber, isLeapMonth, minOf(dayOfMonth, target.lengthOfMonth))
    }

    /** Replaces the month; a leap month must exist in this year. */
    fun withMonth(monthNumber: Int, isLeapMonth: Boolean = false): ChineseDate {
        val target = ChineseMonth.of(year, monthNumber, isLeapMonth)
        return ChineseDate(year, monthNumber, isLeapMonth, minOf(dayOfMonth, target.lengthOfMonth))
    }

    /** Replaces the day without clamping. */
    fun withDayOfMonth(dayOfMonth: Int): ChineseDate =
        ChineseDate(year, monthNumber, isLeapMonth, dayOfMonth)

    override fun compareTo(other: ChineseDate): Int = toEpochDay().compareTo(other.toEpochDay())

    override fun equals(other: Any?): Boolean =
        other is ChineseDate &&
            year == other.year &&
            monthNumber == other.monthNumber &&
            isLeapMonth == other.isLeapMonth &&
            dayOfMonth == other.dayOfMonth

    override fun hashCode(): Int =
        (((year * 31 + monthNumber) * 31 + if (isLeapMonth) 1 else 0) * 31) + dayOfMonth

    /** Compact debugging form such as `2023-2L-01`; not a localized display label. */
    override fun toString(): String = "$year-$monthNumber${if (isLeapMonth) "L" else ""}-$dayOfMonth"

    companion object {
        /** Same as the constructor. */
        fun of(year: Int, monthNumber: Int, isLeapMonth: Boolean, dayOfMonth: Int): ChineseDate =
            ChineseDate(year, monthNumber, isLeapMonth, dayOfMonth)

        /** Chinese date of [epochDay]; see [ChineseCalendarEngine.MIN_EPOCH_DAY]. */
        fun fromEpochDay(epochDay: Long): ChineseDate = ChineseCalendarEngine.fromEpochDay(epochDay)
    }
}
