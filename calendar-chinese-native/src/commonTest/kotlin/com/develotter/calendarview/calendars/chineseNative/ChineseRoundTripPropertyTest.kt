package com.develotter.calendarview.calendars.chineseNative

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChineseRoundTripPropertyTest {
    @Test
    fun randomEpochDaysRoundTrip() {
        val random = Random(20261116)
        repeat(500) {
            val epochDay = random.nextLong(
                ChineseCalendarEngine.MIN_EPOCH_DAY,
                ChineseCalendarEngine.MAX_EPOCH_DAY + 1,
            )
            val date = ChineseCalendarEngine.fromEpochDay(epochDay)

            assertEquals(epochDay, date.toEpochDay())
            assertTrue(date.monthNumber in 1..12)
            assertTrue(date.dayOfMonth in 1..ChineseCalendarEngine.lengthOfMonth(date.month))
        }
    }

    @Test
    fun monthLengthsMatchTheYearTotal() {
        val random = Random(5)
        repeat(100) {
            val year = random.nextInt(ChineseCalendarEngine.MIN_YEAR, ChineseCalendarEngine.MAX_YEAR + 1)
            val months = ChineseCalendarEngine.monthsInYear(year)
            assertEquals(ChineseCalendarEngine.lengthOfYear(year), months.sumOf { ChineseCalendarEngine.lengthOfMonth(it) })
            assertEquals(12 + if (ChineseCalendarEngine.hasLeapMonth(year)) 1 else 0, months.size)
        }
    }
}
