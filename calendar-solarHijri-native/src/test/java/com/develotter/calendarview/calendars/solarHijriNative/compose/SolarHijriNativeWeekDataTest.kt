package com.develotter.calendarview.calendars.solarHijriNative.compose

import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriEngine
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SolarHijriNativeWeekDataTest {
    @Test
    fun buildsSevenConsecutiveDaysStartingOnSaturday() {
        val week = solarHijriNativeWeekData(
            date = SolarHijriDate.fromLocalDate(LocalDate.of(2026, 11, 11)),
            locale = Locale.US,
            firstDayOfWeek = DayOfWeek.SATURDAY,
        )
        assertEquals(7, week.days.size)
        assertEquals(DayOfWeek.SATURDAY, LocalDate.ofEpochDay(week.days.first().epochDay).dayOfWeek)
        assertTrue(week.days.zipWithNext().all { (a, b) -> b.epochDay == a.epochDay + 1 })
        assertEquals(7, week.weekdayLabels.size)
        assertNull(week.weekNumber)
        assertTrue(week.title.isNotBlank())
    }

    @Test
    fun boundaryWeeksDoNotThrow() {
        val first = solarHijriNativeWeekData(SolarHijriDate(SolarHijriEngine.MIN_YEAR, 1, 1), Locale.US)
        val last = solarHijriNativeWeekData(SolarHijriDate(SolarHijriEngine.MAX_YEAR, 12, 29), Locale.US)
        assertEquals(7, first.days.size)
        assertEquals(7, last.days.size)
    }
}
