package com.develotter.calendarviewsample

import com.develotter.calendarview.calendars.hijri.OffsetHijriDateProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.util.Locale

class CalendarSampleMonthDataTest {
    private val locale = Locale.US

    @Test
    fun hijriOffsetChangesAgendaEpochDaysWithoutChangingTheMonthLength() {
        val first = HijrahDate.of(1447, 9, 1)
        val base = sampleMonthData(SampleCalendar.Hijri, first.toEpochDay(), locale)
        val shifted = sampleMonthData(
            calendar = SampleCalendar.Hijri,
            anchorEpochDay = first.toEpochDay(),
            locale = locale,
            hijriDateProvider = OffsetHijriDateProvider(offsetDays = 1),
        )

        assertEquals(base.days.size, shifted.days.size)
        assertEquals(base.days.first().epochDay - 1, shifted.days.first().epochDay)
        assertEquals(base.days.last().epochDay - 1, shifted.days.last().epochDay)
        assertEquals("1", shifted.days.first().label)
        assertTrue(shifted.days.zipWithNext().all { (a, b) -> b.epochDay == a.epochDay + 1 })
    }

    @Test
    fun weekNumberUsesTheLocaleWeekFields() {
        val weekNumber = sampleWeekNumber(LocalDate.of(2026, 1, 5).toEpochDay(), Locale.US)
        assertTrue(weekNumber.isNotBlank())
        assertTrue(weekNumber.all(Char::isDigit))
    }
}
