package com.develotter.calendarview.calendars.solarHijriNative

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SolarHijriEngineTest {
    @Test
    fun monthsRoundTripThroughEpochDaysAcrossTheSupportedRange() {
        val years = listOf(
            SolarHijriEngine.MIN_YEAR,
            SolarHijriEngine.MIN_YEAR + 1,
            1399,
            1400,
            1403,
            1404,
            SolarHijriEngine.MAX_YEAR,
        )
        for (year in years) {
            for (month in 1..12) {
                val length = SolarHijriEngine.daysInMonth(year, month)
                assertTrue(length in 29..31)
                val first = SolarHijriEngine.toEpochDay(year, month, 1)
                assertEquals(first + length - 1, SolarHijriEngine.toEpochDay(year, month, length))
                assertEquals(SolarHijriDate(year, month, length), SolarHijriEngine.fromEpochDay(first + length - 1))
            }
        }
    }

    @Test
    fun consecutiveMonthsAreContiguous() {
        val first = SolarHijriEngine.toEpochDay(1404, 1, 1)
        assertEquals(first + 186, SolarHijriEngine.toEpochDay(1404, 7, 1))
        // 1404 is a common year, so its six second-half months total 179 days.
        assertEquals(first + 365, SolarHijriEngine.toEpochDay(1405, 1, 1))
    }

    @Test
    fun leapYearsFollowTheJalaliCycle() {
        assertTrue(SolarHijriEngine.isLeapYear(1403))
        assertFalse(SolarHijriEngine.isLeapYear(1404))
        assertEquals(30, SolarHijriEngine.daysInMonth(1403, 12))
        assertEquals(29, SolarHijriEngine.daysInMonth(1404, 12))
    }

    @Test
    fun supportedEpochRangeIsEnforced() {
        assertFailsWith<IllegalArgumentException> { SolarHijriEngine.fromEpochDay(SolarHijriEngine.MIN_EPOCH_DAY - 1) }
        assertFailsWith<IllegalArgumentException> { SolarHijriEngine.fromEpochDay(SolarHijriEngine.MAX_EPOCH_DAY + 1) }
        assertEquals(SolarHijriEngine.MIN_YEAR, SolarHijriEngine.fromEpochDay(SolarHijriEngine.MIN_EPOCH_DAY).year)
        assertEquals(SolarHijriEngine.MAX_YEAR, SolarHijriEngine.fromEpochDay(SolarHijriEngine.MAX_EPOCH_DAY).year)
    }

    @Test
    fun knownNowruzDateMatchesTheGregorianTimeline() {
        // 1404-01-01 is 2025-03-21 on the shared absolute timeline.
        assertEquals(20168L, SolarHijriEngine.toEpochDay(1404, 1, 1))
        assertEquals(SolarHijriDate(1404, 1, 1), SolarHijriEngine.fromEpochDay(20168L))
    }
}
