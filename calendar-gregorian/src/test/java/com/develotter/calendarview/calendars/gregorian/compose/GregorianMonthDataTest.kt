package com.develotter.calendarview.calendars.gregorian.compose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class GregorianMonthDataTest {
    @Test
    fun februaryRespectsGregorianLeapYearRules() {
        assertEquals(29, gregorianMonthData(YearMonth.of(2024, 2)).days.size)
        assertEquals(28, gregorianMonthData(YearMonth.of(2025, 2)).days.size)
        assertEquals(28, gregorianMonthData(YearMonth.of(2100, 2)).days.size)
        assertEquals(29, gregorianMonthData(YearMonth.of(2000, 2)).days.size)
    }

    @Test
    fun firstWeekdayControlsOffsetAndLabelOrder() {
        val month = YearMonth.of(2026, 11)
        val monday = gregorianMonthData(month, Locale.US, DayOfWeek.MONDAY)
        val sunday = gregorianMonthData(month, Locale.US, DayOfWeek.SUNDAY)
        assertEquals(6, monday.firstDayOffset)
        assertEquals(0, sunday.firstDayOffset)
        assertEquals(DayOfWeek.MONDAY.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.US), monday.weekdayLabels.first())
        assertEquals(7, sunday.weekdayLabels.size)
    }

    @Test
    fun localeDefaultWeekStartsAreRespected() {
        assertEquals(0, gregorianMonthData(YearMonth.of(2026, 11), Locale.US).firstDayOffset)
        assertEquals(6, gregorianMonthData(YearMonth.of(2026, 11), Locale.GERMANY).firstDayOffset)
    }

    @Test
    fun epochKeysRemainConsecutiveAcrossYearBoundary() {
        val december = gregorianMonthData(YearMonth.of(2026, 12), Locale.US)
        val january = gregorianMonthData(YearMonth.of(2027, 1), Locale.US)
        assertEquals(LocalDate.of(2026, 12, 31).toEpochDay(), december.days.last().epochDay)
        assertEquals(december.days.last().epochDay + 1, january.days.first().epochDay)
        assertTrue(december.days.zipWithNext().all { (a, b) -> b.epochDay == a.epochDay + 1 })
        assertTrue(december.days.all { it.label.isNotBlank() && it.accessibilityLabel.isNotBlank() })
    }

    @Test
    fun enabledPredicateIsEvaluatedForEachDate() {
        val data = gregorianMonthData(YearMonth.of(2026, 11), Locale.US) { it.dayOfMonth != 2 }
        assertTrue(data.days[0].enabled)
        assertFalse(data.days[1].enabled)
        assertEquals(30, data.days.size)
    }
}
