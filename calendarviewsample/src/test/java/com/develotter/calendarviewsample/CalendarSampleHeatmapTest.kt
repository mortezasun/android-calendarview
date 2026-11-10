package com.develotter.calendarviewsample

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class CalendarSampleHeatmapTest {
    private val locale = Locale.ENGLISH
    private val today = LocalDate.of(2026, 11, 10)

    @Test
    fun buildsTwelveCompleteMonthsForEveryCalendar() {
        for (calendar in SampleCalendar.entries) {
            val year = sampleHeatmapCurrentYear(calendar, today)
            val months = sampleHeatmapYear(calendar, year, locale, today)
            assertEquals("$calendar month count", 12, months.size)
            months.forEach { month ->
                assertEquals("$calendar weekday labels", 7, month.weekdayLabels.size)
                assertTrue("$calendar title", month.title.isNotBlank())
                assertTrue("$calendar weeks", month.weeks.isNotEmpty())
                month.weeks.forEach { week -> assertEquals("$calendar week size", 7, week.size) }
                val days = month.weeks.flatten().filterNotNull()
                assertTrue("$calendar days", days.isNotEmpty())
                days.zipWithNext().forEach { (first, second) ->
                    assertEquals("$calendar consecutive days", first.epochDay + 1, second.epochDay)
                }
            }
        }
    }

    @Test
    fun currentYearsAreInsideTheSupportedRanges() {
        for (calendar in SampleCalendar.entries) {
            val year = sampleHeatmapCurrentYear(calendar, today)
            assertTrue("$calendar $year", year in sampleHeatmapYearRange(calendar))
        }
    }
}
