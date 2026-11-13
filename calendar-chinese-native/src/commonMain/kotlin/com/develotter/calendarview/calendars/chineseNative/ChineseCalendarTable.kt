package com.develotter.calendarview.calendars.chineseNative

/**
 * Decoder for the generated, immutable month table.
 *
 * Each year occupies two `Int` entries in [ChineseCalendarData.YEARS]: the epoch day of its
 * first month and a packed value with the repeated (leap) month number in bits 16..19 and one
 * bit per chronological month, set when that month has 30 days. All arithmetic stays integral.
 */
internal object ChineseCalendarTable {
    private val years: IntArray = ChineseCalendarData.YEARS

    const val FIRST_YEAR: Int = ChineseCalendarData.FIRST_YEAR
    const val LAST_YEAR: Int = ChineseCalendarData.LAST_YEAR
    const val DATASET_VERSION: String = ChineseCalendarData.DATASET_VERSION

    val yearCount: Int = years.size / 2

    private val monthPrefix: IntArray = IntArray(yearCount + 1).also { prefix ->
        for (index in 0 until yearCount) {
            prefix[index + 1] = prefix[index] + monthCount(index)
        }
    }

    val totalMonths: Int = monthPrefix[yearCount]

    fun yearIndex(year: Int): Int {
        require(year in FIRST_YEAR..LAST_YEAR) {
            "year must be in $FIRST_YEAR..$LAST_YEAR but was $year"
        }
        return year - FIRST_YEAR
    }

    fun startOfYear(yearIndex: Int): Int = years[yearIndex * 2]

    private fun packed(yearIndex: Int): Int = years[yearIndex * 2 + 1]

    /** Repeated month number of [yearIndex], or `0` when the year has twelve months. */
    fun leapMonthNumber(yearIndex: Int): Int = (packed(yearIndex) ushr 16) and 0xF

    fun monthCount(yearIndex: Int): Int = if (leapMonthNumber(yearIndex) == 0) 12 else 13

    fun lengthOfMonth(yearIndex: Int, position: Int): Int {
        require(position in 0 until monthCount(yearIndex)) { "position out of range" }
        return if ((packed(yearIndex) ushr position) and 1 == 1) 30 else 29
    }

    fun lengthOfYear(yearIndex: Int): Int {
        var total = 0
        for (position in 0 until monthCount(yearIndex)) total += lengthOfMonth(yearIndex, position)
        return total
    }

    fun monthOffset(yearIndex: Int, position: Int): Int {
        var offset = 0
        for (index in 0 until position) offset += lengthOfMonth(yearIndex, index)
        return offset
    }

    fun globalMonthIndex(yearIndex: Int, position: Int): Int = monthPrefix[yearIndex] + position

    fun yearIndexForMonthIndex(monthIndex: Int): Int {
        require(monthIndex in 0 until totalMonths) { "month index $monthIndex is out of range" }
        var low = 0
        var high = yearCount - 1
        var result = 0
        while (low <= high) {
            val middle = (low + high) ushr 1
            if (monthPrefix[middle] <= monthIndex) {
                result = middle
                low = middle + 1
            } else {
                high = middle - 1
            }
        }
        return result
    }

    fun positionInYear(monthIndex: Int): Int = monthIndex - monthPrefix[yearIndexForMonthIndex(monthIndex)]

    fun yearIndexForEpochDay(epochDay: Long): Int {
        var low = 0
        var high = yearCount - 1
        var result = 0
        while (low <= high) {
            val middle = (low + high) ushr 1
            if (startOfYear(middle).toLong() <= epochDay) {
                result = middle
                low = middle + 1
            } else {
                high = middle - 1
            }
        }
        return result
    }

    /**
     * Number and leap flag of the chronological [position] inside a year.
     * A repeated month follows the regular month with the same number.
     */
    fun monthIdentity(yearIndex: Int, position: Int): Pair<Int, Boolean> {
        val leap = leapMonthNumber(yearIndex)
        if (leap == 0) return (position + 1) to false
        return when {
            position < leap - 1 -> (position + 1) to false
            position == leap - 1 -> leap to false
            position == leap -> leap to true
            else -> position to false
        }
    }

    fun positionOf(yearIndex: Int, monthNumber: Int, isLeapMonth: Boolean): Int {
        require(monthNumber in 1..12) { "monthNumber must be in 1..12 but was $monthNumber" }
        val leap = leapMonthNumber(yearIndex)
        if (isLeapMonth) {
            require(leap == monthNumber) {
                "year ${FIRST_YEAR + yearIndex} has no leap month $monthNumber"
            }
            return leap
        }
        return if (leap == 0 || monthNumber <= leap) monthNumber - 1 else monthNumber
    }

    fun requireYearIndex(yearIndex: Int) {
        require(yearIndex in 0 until yearCount) { "year index $yearIndex is out of range" }
    }
}
