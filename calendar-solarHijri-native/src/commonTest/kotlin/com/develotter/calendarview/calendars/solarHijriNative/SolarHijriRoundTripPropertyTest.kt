package com.develotter.calendarview.calendars.solarHijriNative

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class SolarHijriRoundTripPropertyTest {
    @Test
    fun randomDatesRoundTripThroughEpochDays() {
        val random = Random(20261116)
        repeat(500) {
            val year = random.nextInt(SolarHijriEngine.MIN_YEAR, SolarHijriEngine.MAX_YEAR + 1)
            val month = random.nextInt(1, 13)
            val day = random.nextInt(1, SolarHijriEngine.daysInMonth(year, month) + 1)
            val date = SolarHijriDate.of(year, month, day)

            assertEquals(date, SolarHijriDate.fromEpochDay(date.toEpochDay()))
            assertEquals(year, date.year)
            assertEquals(month, date.month)
            assertEquals(day, date.day)
        }
    }

    @Test
    fun yearBoundariesRoundTrip() {
        listOf(SolarHijriEngine.MIN_YEAR, 1, 1400, SolarHijriEngine.MAX_YEAR).forEach { year ->
            val first = SolarHijriDate.of(year, 1, 1)
            assertEquals(first, SolarHijriDate.fromEpochDay(first.toEpochDay()))
            val lastDay = SolarHijriEngine.daysInMonth(year, 12)
            val last = SolarHijriDate.of(year, 12, lastDay)
            assertEquals(last, SolarHijriDate.fromEpochDay(last.toEpochDay()))
        }
    }
}
