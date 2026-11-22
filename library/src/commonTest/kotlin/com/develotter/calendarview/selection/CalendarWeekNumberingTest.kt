package com.develotter.calendarview.selection

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CalendarWeekNumberingTest {
    @Test
    fun isoWeekdayReturnsOneForMondayThroughSevenForSunday() {
        // 1970-01-01 was Thursday = 4
        assertEquals(4, isoWeekday(0L))
        assertEquals(5, isoWeekday(1L))  // Friday
        assertEquals(6, isoWeekday(2L))  // Saturday
        assertEquals(7, isoWeekday(3L))  // Sunday
        assertEquals(1, isoWeekday(4L))  // Monday
        assertEquals(2, isoWeekday(5L))  // Tuesday
        assertEquals(3, isoWeekday(6L))  // Wednesday
        // Negative epoch days
        assertEquals(3, isoWeekday(-1L)) // 1969-12-31 was Wednesday
    }

    @Test
    fun civilDateAndEpochDayRoundTrip() {
        for (year in listOf(1970, 2000, 2024, 2026, 2100)) {
            for (month in 1..12) {
                val maxDay = daysInGregorianMonth(year, month)
                for (day in listOf(1, 15, maxDay)) {
                    val epoch = epochDayOf(year, month, day)
                    val civil = civilFromEpochDay(epoch)
                    assertEquals(CivilDate(year, month, day), civil)
                }
            }
        }
    }

    @Test
    fun leapYearCalculationInFebruary() {
        assertEquals(29, daysInGregorianMonth(2000, 2))
        assertEquals(29, daysInGregorianMonth(2024, 2))
        assertEquals(28, daysInGregorianMonth(1900, 2))
        assertEquals(28, daysInGregorianMonth(2026, 2))
        assertEquals(28, daysInGregorianMonth(2100, 2))
    }

    @Test
    fun epochDayOfValidatesInputs() {
        assertFailsWith<IllegalArgumentException> { epochDayOf(2026, 0, 1) }
        assertFailsWith<IllegalArgumentException> { epochDayOf(2026, 13, 1) }
        assertFailsWith<IllegalArgumentException> { epochDayOf(2026, 1, 0) }
        assertFailsWith<IllegalArgumentException> { epochDayOf(2026, 1, 32) }
        assertFailsWith<IllegalArgumentException> { epochDayOf(2026, 2, 29) } // 2026 is not a leap year
    }

    @Test
    fun isoWeekNumberAndYearFollowIsoRules() {
        // 1970-01-01 (Thu) is in week 1 of 1970
        assertEquals(1, isoWeekNumber(0L))
        assertEquals(1970, isoWeekYear(0L))

        // 2024-12-30 (Mon) belongs to week 1 of 2025
        val dec30_2024 = epochDayOf(2024, 12, 30)
        assertEquals(1, isoWeekNumber(dec30_2024))
        assertEquals(2025, isoWeekYear(dec30_2024))

        // 2021-01-01 (Fri) belongs to week 53 of 2020
        val jan1_2021 = epochDayOf(2021, 1, 1)
        assertEquals(53, isoWeekNumber(jan1_2021))
        assertEquals(2020, isoWeekYear(jan1_2021))

        // 2021-01-04 (Mon) belongs to week 1 of 2021
        val jan4_2021 = epochDayOf(2021, 1, 4)
        assertEquals(1, isoWeekNumber(jan4_2021))
        assertEquals(2021, isoWeekYear(jan4_2021))

        // 2026-11-21 (Sat) is in week 47 of 2026
        val nov21_2026 = epochDayOf(2026, 11, 21)
        assertEquals(47, isoWeekNumber(nov21_2026))
        assertEquals(2026, isoWeekYear(nov21_2026))
    }
}
