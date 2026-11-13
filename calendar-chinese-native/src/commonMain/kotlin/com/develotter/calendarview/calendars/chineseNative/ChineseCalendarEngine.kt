package com.develotter.calendarview.calendars.chineseNative

/**
 * Thrown when a requested day or month lies outside the supported data range.
 *
 * Unlike an invalid month or day, this is not a malformed value but a documented limit of the
 * generated data set; callers should present it as "not supported" rather than "invalid".
 */
class ChineseDateOutOfRangeException(message: String) : IllegalArgumentException(message)

/**
 * Self-contained Chinese lunisolar calendar arithmetic over immutable, generated month data.
 *
 * Supported range: Chinese years [MIN_YEAR]..[MAX_YEAR] (Gregorian 1901..2099), from
 * [MIN_EPOCH_DAY] through [MAX_EPOCH_DAY]. Epoch days are the same integer day identity used by
 * the other calendar modules and by `java.time.LocalDate.toEpochDay`; they are not instants and
 * do not depend on a device time zone. Days are never computed from milliseconds.
 *
 * The engine is pure Kotlin common code: no Android, Compose, `java.time` or other calendar
 * module is referenced. Month starts and lengths come from the versioned data set documented in
 * `tools/chinese-calendar/manifest.json`.
 */
object ChineseCalendarEngine {
    /** First supported Chinese year. */
    const val MIN_YEAR: Int = ChineseCalendarTable.FIRST_YEAR

    /** Last supported Chinese year. */
    const val MAX_YEAR: Int = ChineseCalendarTable.LAST_YEAR

    /** Version of the generated month data. */
    const val DATASET_VERSION: String = ChineseCalendarTable.DATASET_VERSION

    /** Epoch day of the first supported day, the first day of month 1 of [MIN_YEAR]. */
    val MIN_EPOCH_DAY: Long = ChineseCalendarTable.startOfYear(0).toLong()

    /** Epoch day of the last supported day, the last day of [MAX_YEAR]. */
    val MAX_EPOCH_DAY: Long =
        ChineseCalendarTable.startOfYear(ChineseCalendarTable.yearCount - 1).toLong() +
            ChineseCalendarTable.lengthOfYear(ChineseCalendarTable.yearCount - 1) - 1

    /** Whether [epochDay] lies inside the supported range. */
    fun isSupported(epochDay: Long): Boolean = epochDay in MIN_EPOCH_DAY..MAX_EPOCH_DAY

    /** Whether [year] is a supported Chinese year and has a leap month. */
    fun hasLeapMonth(year: Int): Boolean =
        ChineseCalendarTable.leapMonthNumber(ChineseCalendarTable.yearIndex(year)) != 0

    /** Repeated month number of [year], or `null` when the year has twelve months. */
    fun leapMonthNumber(year: Int): Int? =
        ChineseCalendarTable.leapMonthNumber(ChineseCalendarTable.yearIndex(year)).takeIf { it != 0 }

    /** Chinese date of [epochDay]; throws [ChineseDateOutOfRangeException] outside the range. */
    fun fromEpochDay(epochDay: Long): ChineseDate {
        requireSupported(epochDay)
        val yearIndex = ChineseCalendarTable.yearIndexForEpochDay(epochDay)
        var remaining = epochDay - ChineseCalendarTable.startOfYear(yearIndex)
        var position = 0
        val count = ChineseCalendarTable.monthCount(yearIndex)
        while (position < count) {
            val length = ChineseCalendarTable.lengthOfMonth(yearIndex, position)
            if (remaining < length) break
            remaining -= length
            position += 1
        }
        val (monthNumber, isLeapMonth) = ChineseCalendarTable.monthIdentity(yearIndex, position)
        return ChineseDate(
            year = ChineseCalendarTable.FIRST_YEAR + yearIndex,
            monthNumber = monthNumber,
            isLeapMonth = isLeapMonth,
            dayOfMonth = remaining.toInt() + 1,
        )
    }

    /** Epoch day of [date]; the date is validated on construction. */
    fun toEpochDay(date: ChineseDate): Long = monthStartEpochDay(date.month) + date.dayOfMonth - 1

    /** Chinese month containing [epochDay]. */
    fun monthOf(epochDay: Long): ChineseMonth {
        val date = fromEpochDay(epochDay)
        return date.month
    }

    /** Chronological months of [year], including the leap month when present. */
    fun monthsInYear(year: Int): List<ChineseMonth> {
        val yearIndex = ChineseCalendarTable.yearIndex(year)
        return List(ChineseCalendarTable.monthCount(yearIndex)) { position ->
            val (number, leap) = ChineseCalendarTable.monthIdentity(yearIndex, position)
            ChineseMonth.of(year, number, leap)
        }
    }

    /** Number of days in [month]. */
    fun lengthOfMonth(month: ChineseMonth): Int {
        val yearIndex = ChineseCalendarTable.yearIndex(month.year)
        return ChineseCalendarTable.lengthOfMonth(
            yearIndex,
            ChineseCalendarTable.positionOf(yearIndex, month.monthNumber, month.isLeapMonth),
        )
    }

    /** Number of days in [year], 353..355 or 383..385. */
    fun lengthOfYear(year: Int): Int =
        ChineseCalendarTable.lengthOfYear(ChineseCalendarTable.yearIndex(year))

    /** Epoch day of the first day of [month]. */
    fun monthStartEpochDay(month: ChineseMonth): Long {
        val yearIndex = ChineseCalendarTable.yearIndex(month.year)
        val position = ChineseCalendarTable.positionOf(yearIndex, month.monthNumber, month.isLeapMonth)
        return ChineseCalendarTable.startOfYear(yearIndex).toLong() +
            ChineseCalendarTable.monthOffset(yearIndex, position)
    }

    /** First month of [year]. */
    fun firstMonthOfYear(year: Int): ChineseMonth =
        ChineseMonth.of(year, 1, false)

    /** Last month of [year]. */
    fun lastMonthOfYear(year: Int): ChineseMonth {
        val months = monthsInYear(year)
        return months.last()
    }

    /** Month following [month]; throws [ChineseDateOutOfRangeException] after the last month. */
    fun nextMonth(month: ChineseMonth): ChineseMonth = monthAtIndex(indexOf(month) + 1)

    /** Month preceding [month]; throws [ChineseDateOutOfRangeException] before the first month. */
    fun previousMonth(month: ChineseMonth): ChineseMonth = monthAtIndex(indexOf(month) - 1)

    /** Repeated month of [year], or `null` when the year has twelve months. */
    fun leapMonth(year: Int): ChineseMonth? {
        val number = leapMonthNumber(year) ?: return null
        return ChineseMonth.of(year, number, true)
    }

    internal fun indexOf(month: ChineseMonth): Long {
        val yearIndex = ChineseCalendarTable.yearIndex(month.year)
        val position = ChineseCalendarTable.positionOf(yearIndex, month.monthNumber, month.isLeapMonth)
        return ChineseCalendarTable.globalMonthIndex(yearIndex, position).toLong()
    }

    internal fun monthAtIndex(index: Long): ChineseMonth {
        if (index < 0 || index >= ChineseCalendarTable.totalMonths) {
            throw ChineseDateOutOfRangeException(
                "month index $index is outside 0..${ChineseCalendarTable.totalMonths - 1}",
            )
        }
        val yearIndex = ChineseCalendarTable.yearIndexForMonthIndex(index.toInt())
        val position = ChineseCalendarTable.positionInYear(index.toInt())
        val (number, leap) = ChineseCalendarTable.monthIdentity(yearIndex, position)
        return ChineseMonth.of(ChineseCalendarTable.FIRST_YEAR + yearIndex, number, leap)
    }


    /** Epoch day of the first day of [year] in Chinese month 1. */
    fun yearStartEpochDay(year: Int): Long =
        ChineseCalendarTable.startOfYear(ChineseCalendarTable.yearIndex(year)).toLong()

    private fun requireSupported(epochDay: Long) {
        if (epochDay !in MIN_EPOCH_DAY..MAX_EPOCH_DAY) {
            throw ChineseDateOutOfRangeException(
                "epochDay $epochDay is outside $MIN_EPOCH_DAY..$MAX_EPOCH_DAY",
            )
        }
    }
}
