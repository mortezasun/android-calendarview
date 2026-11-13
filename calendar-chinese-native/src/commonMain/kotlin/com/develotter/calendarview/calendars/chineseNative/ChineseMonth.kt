package com.develotter.calendarview.calendars.chineseNative

/**
 * Immutable Chinese lunisolar month.
 *
 * [year] is the Gregorian year in which the Chinese year begins, [monthNumber] is `1..12`, and
 * [isLeapMonth] distinguishes the repeated month of a 13-month year. A leap month is never
 * encoded as month 13 and never exists on its own: every instance is validated against the
 * supported data set. Because this is not a data class there is no `copy()` path around
 * validation; use [ChineseCalendarEngine.nextMonth], [ChineseCalendarEngine.previousMonth] or
 * [ChineseMonth.of] instead.
 */
class ChineseMonth private constructor(
    val year: Int,
    val monthNumber: Int,
    val isLeapMonth: Boolean,
) : Comparable<ChineseMonth> {
    init {
        ChineseCalendarTable.yearIndex(year)
        require(monthNumber in 1..12) { "monthNumber must be in 1..12 but was $monthNumber" }
        if (isLeapMonth) {
            val yearIndex = ChineseCalendarTable.yearIndex(year)
            require(ChineseCalendarTable.leapMonthNumber(yearIndex) == monthNumber) {
                "year $year has no leap month $monthNumber"
            }
        }
    }

    /** Number of days in this month. */
    val lengthOfMonth: Int
        get() = ChineseCalendarEngine.lengthOfMonth(this)

    /** Epoch day of the first day of this month. */
    fun startEpochDay(): Long = ChineseCalendarEngine.monthStartEpochDay(this)

    /** Last day of this month as a [ChineseDate]. */
    fun lastDate(): ChineseDate = ChineseDate(year, monthNumber, isLeapMonth, lengthOfMonth)

    override fun compareTo(other: ChineseMonth): Int =
        compareValuesBy(this, other, ChineseMonth::year, ChineseMonth::monthNumber, ChineseMonth::isLeapMonth)

    override fun equals(other: Any?): Boolean =
        other is ChineseMonth &&
            year == other.year &&
            monthNumber == other.monthNumber &&
            isLeapMonth == other.isLeapMonth

    override fun hashCode(): Int = (year * 31 + monthNumber) * 31 + if (isLeapMonth) 1 else 0

    override fun toString(): String = "$year-${monthNumber}${if (isLeapMonth) "L" else ""}"

    companion object {
        /** Validates a month against the supported range and the actual leap month of [year]. */
        fun of(year: Int, monthNumber: Int, isLeapMonth: Boolean = false): ChineseMonth =
            ChineseMonth(year, monthNumber, isLeapMonth)

        /** Month containing [epochDay]; see [ChineseCalendarEngine.MIN_EPOCH_DAY]. */
        fun fromEpochDay(epochDay: Long): ChineseMonth =
            ChineseCalendarEngine.monthOf(epochDay)

        /** Weekday index in `0..6` with Monday = 0, derived from the epoch day alone. */
        fun dayOfWeekIndex(epochDay: Long): Int = floorMod7(epochDay + 3L)
    }
}

/** Mathematical floor modulus for a positive divisor; avoids platform-specific remainder rules. */
internal fun floorMod7(value: Long): Int {
    val remainder = value % 7L
    return (if (remainder < 0) remainder + 7L else remainder).toInt()
}
