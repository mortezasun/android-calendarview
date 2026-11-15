package com.develotter.calendarview.calendars.gregorian

import androidx.compose.runtime.Immutable

/**
 * Platform-independent Gregorian year-month value type for KMP.
 */
@Immutable
data class GregorianYearMonth(
    val year: Int,
    val month: Int,
) : Comparable<GregorianYearMonth> {
    init {
        require(month in 1..12) { "month must be in 1..12, got $month" }
    }

    override fun compareTo(other: GregorianYearMonth): Int {
        val yearCmp = year.compareTo(other.year)
        return if (yearCmp != 0) yearCmp else month.compareTo(other.month)
    }

    fun plusMonths(monthsToAdd: Int): GregorianYearMonth {
        val totalMonths = year * 12L + (month - 1) + monthsToAdd.toLong()
        val newYear = totalMonths.floorDiv(12).toInt()
        val newMonth = totalMonths.mod(12) + 1
        return GregorianYearMonth(newYear, newMonth)
    }

    fun minusMonths(monthsToSubtract: Int): GregorianYearMonth = plusMonths(-monthsToSubtract)

    fun lengthOfMonth(): Int = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> error("Invalid month: $month")
    }

    fun atDay(dayOfMonth: Int): Long {
        require(dayOfMonth in 1..lengthOfMonth()) {
            "dayOfMonth $dayOfMonth out of range 1..${lengthOfMonth()} for $year-$month"
        }
        return gregorianToEpochDay(year, month, dayOfMonth)
    }

    companion object {
        fun now(): GregorianYearMonth {
            val todayEpochDay = GregorianPlatformFormatter.currentEpochDay()
            return fromEpochDay(todayEpochDay)
        }

        fun fromEpochDay(epochDay: Long): GregorianYearMonth {
            val z = epochDay + 719468
            val era = (if (z >= 0) z else z - 146096) / 146097
            val doe = z - era * 146097
            val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
            val y = yoe + era * 400
            val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
            val mp = (5 * doy + 2) / 153
            val m = mp + if (mp < 10) 3 else -9
            val year = (y + if (m <= 2) 1 else 0).toInt()
            return GregorianYearMonth(year, m.toInt())
        }

        fun isLeapYear(year: Int): Boolean =
            (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

        fun gregorianToEpochDay(year: Int, month: Int, day: Int): Long {
            val y = year.toLong() - if (month <= 2) 1L else 0L
            val era = if (y >= 0) y / 400 else (y - 399) / 400
            val yoe = y - era * 400
            val m = month.toLong()
            val doy = (153 * (m + if (m > 2) -3 else 9) + 2) / 5 + day - 1
            val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
            return era * 146097 + doe - 719468
        }

        fun isoWeekdayFromEpochDay(epochDay: Long): Int =
            (((epochDay + 3) % 7 + 7) % 7).toInt() + 1
    }
}
