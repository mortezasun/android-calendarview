package com.develotter.calendarview.calendars.gregorian.compose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

class GregorianWeekDataTest {
    @Test
    fun buildsSevenConsecutiveDaysAlignedToTheConfiguredWeekStart() {
        val week = gregorianWeekData(
            date = LocalDate.of(2026, 10, 29),
            locale = Locale.US,
            firstDayOfWeek = DayOfWeek.MONDAY,
        )

        assertEquals(7, week.days.size)
        assertEquals(LocalDate.of(2026, 10, 26).toEpochDay(), week.days.first().epochDay)
        assertEquals(LocalDate.of(2026, 11, 1).toEpochDay(), week.days.last().epochDay)
        assertTrue(week.days.zipWithNext().all { (first, second) ->
            second.epochDay == first.epochDay + 1
        })
        assertEquals(7, week.weekdayLabels.size)
        assertEquals("44", week.weekNumber)
        assertNotNull(week.title)
    }
}
