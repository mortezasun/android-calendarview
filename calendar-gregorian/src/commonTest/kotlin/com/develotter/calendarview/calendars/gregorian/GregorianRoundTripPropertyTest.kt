package com.develotter.calendarview.calendars.gregorian

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GregorianRoundTripPropertyTest {
    @Test
    fun yearMonthAndDayRoundTripForRandomDates() {
        val random = Random(20261116)
        repeat(500) {
            val year = random.nextInt(1900, 2101)
            val month = random.nextInt(1, 13)
            val yearMonth = GregorianYearMonth(year, month)
            val day = random.nextInt(1, yearMonth.lengthOfMonth() + 1)
            val epochDay = GregorianYearMonth.gregorianToEpochDay(year, month, day)

            assertEquals(yearMonth, GregorianYearMonth.fromEpochDay(epochDay))
            assertEquals(epochDay, yearMonth.atDay(day))
        }
    }

    @Test
    fun adjacentMonthsAreContiguous() {
        val random = Random(7)
        repeat(200) {
            val start = GregorianYearMonth(random.nextInt(1900, 2100), random.nextInt(1, 13))
            val next = start.plusMonths(1)
            assertEquals(start.atDay(start.lengthOfMonth()) + 1, next.atDay(1))
            assertEquals(start, next.minusMonths(1))
        }
    }

    @Test
    fun leapDayOnlyExistsInLeapYears() {
        assertEquals(29, GregorianYearMonth(2024, 2).lengthOfMonth())
        assertEquals(28, GregorianYearMonth(2025, 2).lengthOfMonth())
        assertTrue(GregorianYearMonth.isLeapYear(2000))
        assertTrue(!GregorianYearMonth.isLeapYear(1900))
    }
}
