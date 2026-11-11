package com.develotter.calendarview.calendars.hijri.compose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.util.Locale

class HijriWeekDataTest {
    @Test
    fun buildsSevenConsecutiveDaysStartingOnSaturday() {
        val week = hijriWeekData(
            date = HijrahDate.from(LocalDate.of(2026, 11, 11)),
            locale = Locale.US,
            firstDayOfWeek = DayOfWeek.SATURDAY,
        )

        assertEquals(7, week.days.size)
        assertEquals(DayOfWeek.SATURDAY, LocalDate.ofEpochDay(week.days.first().epochDay).dayOfWeek)
        assertTrue(week.days.zipWithNext().all { (first, second) ->
            second.epochDay == first.epochDay + 1
        })
        assertEquals(7, week.weekdayLabels.size)
        assertNull(week.weekNumber)
        assertTrue(week.title.isNotBlank())
    }
}
