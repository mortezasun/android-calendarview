package com.develotter.calendarview.calendars.solarHijriNative.compose

import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriEngine
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SolarHijriNativeMonthDataTest {
    @Test
    fun monthLengthsIncludeLeapAndCommonEsfand() {
        assertEquals(31, solarHijriNativeMonthData(SolarHijriDate(1400, 1, 1)).days.size)
        assertEquals(30, solarHijriNativeMonthData(SolarHijriDate(1400, 7, 1)).days.size)
        assertEquals(30, solarHijriNativeMonthData(SolarHijriDate(1399, 12, 1)).days.size)
        assertEquals(29, solarHijriNativeMonthData(SolarHijriDate(1400, 12, 1)).days.size)
    }

    @Test
    fun nowruzHasExpectedGregorianIdentity() {
        val date = SolarHijriDate(1404, 1, 1)
        assertEquals(LocalDate.of(2025, 3, 21).toEpochDay(), date.toEpochDay())
        assertEquals(date, SolarHijriDate.fromEpochDay(date.toEpochDay()))
    }

    @Test
    fun yearBoundaryIsConsecutive() {
        val oldMonth = solarHijriNativeMonthData(SolarHijriDate(1399, 12, 1), Locale.US)
        val newMonth = solarHijriNativeMonthData(SolarHijriDate(1400, 1, 1), Locale.US)
        assertEquals(oldMonth.days.last().epochDay + 1, newMonth.days.first().epochDay)
        assertTrue(oldMonth.days.zipWithNext().all { (a, b) -> b.epochDay == a.epochDay + 1 })
    }

    @Test
    fun inputIsNormalizedAndWeekStartIsConfigurable() {
        val input = SolarHijriDate(1400, 1, 15)
        val monday = solarHijriNativeMonthData(input, Locale.US, DayOfWeek.MONDAY)
        assertEquals(6, monday.firstDayOffset)
        assertEquals(7, monday.weekdayLabels.size)
        assertEquals(solarHijriNativeMonthData(SolarHijriDate(1400, 1, 1), Locale.US, DayOfWeek.MONDAY), monday)
        assertEquals(1, solarHijriNativeMonthData(input, Locale.US).firstDayOffset)
    }

    @Test
    fun persianLabelsAndDisabledDatePredicateArePopulated() {
        val data = solarHijriNativeMonthData(
            month = SolarHijriDate(1400, 1, 1),
            locale = Locale.forLanguageTag("fa"),
            today = SolarHijriDate(1400, 1, 5),
        ) { it.day != 2 }
        assertTrue(data.title.isNotBlank())
        assertTrue(data.days.all { it.label.isNotBlank() && it.accessibilityLabel.isNotBlank() })
        assertTrue(data.days.first().enabled)
        assertFalse(data.days[1].enabled)
        assertTrue(data.days[4].isToday)
    }

    @Test
    fun boundaryMonthsProduceDisabledBlankCellsOutsideSupportedRange() {
        val beforeMin = solarHijriNativeDayCellForEpoch(
            epochDay = SolarHijriEngine.MIN_EPOCH_DAY - 1,
            locale = Locale.US,
            today = SolarHijriDate(1404, 1, 1),
            isDateEnabled = { true },
        )
        assertFalse(beforeMin.enabled)
        assertEquals("", beforeMin.label)

        val afterMax = solarHijriNativeDayCellForEpoch(
            epochDay = SolarHijriEngine.MAX_EPOCH_DAY + 1,
            locale = Locale.US,
            today = SolarHijriDate(1404, 1, 1),
            isDateEnabled = { true },
        )
        assertFalse(afterMax.enabled)
        assertEquals("", afterMax.label)

        val firstMonth = solarHijriNativeMonthData(SolarHijriDate(SolarHijriEngine.MIN_YEAR, 1, 1), Locale.US)
        val lastMonth = solarHijriNativeMonthData(SolarHijriDate(SolarHijriEngine.MAX_YEAR, 12, 1), Locale.US)
        assertEquals(SolarHijriEngine.MIN_EPOCH_DAY, firstMonth.days.first().epochDay)
        assertEquals(SolarHijriEngine.MAX_EPOCH_DAY, lastMonth.days.last().epochDay)
    }
}
