package com.develotter.calendarview.calendars.solarHijri.compose

import ir.huri.jcal.JalaliCalendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

class SolarHijriMonthDataTest {
    @Test
    fun monthLengthsIncludeLeapAndCommonEsfand() {
        assertEquals(31, solarHijriMonthData(JalaliCalendar(1400, 1, 1)).days.size)
        assertEquals(30, solarHijriMonthData(JalaliCalendar(1400, 7, 1)).days.size)
        assertEquals(30, solarHijriMonthData(JalaliCalendar(1399, 12, 1)).days.size)
        assertEquals(29, solarHijriMonthData(JalaliCalendar(1400, 12, 1)).days.size)
    }

    @Test
    fun nowruzHasExpectedGregorianIdentityAndRoundTrip() {
        val jalali = JalaliCalendar(1400, 1, 1)
        val epoch = solarHijriEpochDay(jalali)
        assertEquals(LocalDate.of(2021, 3, 21).toEpochDay(), epoch)
        val restored = JalaliCalendar(LocalDate.ofEpochDay(epoch))
        assertEquals(jalali.year, restored.year)
        assertEquals(jalali.month, restored.month)
        assertEquals(jalali.day, restored.day)
    }

    @Test
    fun yearBoundaryIsConsecutive() {
        val oldMonth = solarHijriMonthData(JalaliCalendar(1399, 12, 1), Locale.US)
        val newMonth = solarHijriMonthData(JalaliCalendar(1400, 1, 1), Locale.US)
        assertEquals(oldMonth.days.last().epochDay + 1, newMonth.days.first().epochDay)
        assertTrue(oldMonth.days.zipWithNext().all { (a, b) -> b.epochDay == a.epochDay + 1 })
    }

    @Test
    fun inputIsNormalizedWithoutMutationAndWeekStartIsConfigurable() {
        val input = JalaliCalendar(1400, 1, 15)
        val monday = solarHijriMonthData(input, Locale.US, DayOfWeek.MONDAY)
        assertEquals(15, input.day)
        assertEquals(6, monday.firstDayOffset)
        assertEquals(7, monday.weekdayLabels.size)
        assertEquals(solarHijriMonthData(JalaliCalendar(1400, 1, 1), Locale.US, DayOfWeek.MONDAY), monday)
        assertEquals(1, solarHijriMonthData(input, Locale.US).firstDayOffset)
    }

    @Test
    fun persianLabelsAndDisabledDatePredicateArePopulated() {
        val data = solarHijriMonthData(JalaliCalendar(1400, 1, 1), Locale.forLanguageTag("fa")) { it.day != 2 }
        assertTrue(data.title.isNotBlank())
        assertTrue(data.days.all { it.label.isNotBlank() && it.accessibilityLabel.isNotBlank() })
        assertTrue(data.days.first().enabled)
        assertFalse(data.days[1].enabled)
    }
}
