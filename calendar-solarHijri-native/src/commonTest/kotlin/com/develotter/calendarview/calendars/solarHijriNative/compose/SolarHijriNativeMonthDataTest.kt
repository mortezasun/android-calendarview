package com.develotter.calendarview.calendars.solarHijriNative.compose

import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SolarHijriNativeMonthDataTest {
    @Test
    fun monthLengthsIncludeLeapAndCommonEsfand() {
        assertEquals(31, solarHijriNativeMonthData(SolarHijriDate(1400, 1, 1), todayEpochDay = 0L).days.size)
        assertEquals(30, solarHijriNativeMonthData(SolarHijriDate(1400, 7, 1), todayEpochDay = 0L).days.size)
        assertEquals(30, solarHijriNativeMonthData(SolarHijriDate(1399, 12, 1), todayEpochDay = 0L).days.size)
        assertEquals(29, solarHijriNativeMonthData(SolarHijriDate(1400, 12, 1), todayEpochDay = 0L).days.size)
    }

    @Test
    fun nowruzHasExpectedIdentityAndRoundTrip() {
        val first = SolarHijriDate(1404, 1, 1)
        assertEquals(20168L, first.toEpochDay())
        assertEquals(first, SolarHijriDate.fromEpochDay(20168L))
        assertEquals(first, solarHijriNativeMonthData(first, todayEpochDay = 0L).let {
            SolarHijriDate.fromEpochDay(it.days.first().epochDay)
        })
    }

    @Test
    fun monthStartsAtRequestedWeekStart() {
        val first = SolarHijriDate(1404, 1, 1)
        val saturdayFirst = solarHijriNativeMonthData(first, firstDayOfWeek = 6, todayEpochDay = 0L)
        val mondayFirst = solarHijriNativeMonthData(first, firstDayOfWeek = 1, todayEpochDay = 0L)
        assertEquals((first.isoWeekday() - 6 + 7) % 7, saturdayFirst.firstDayOffset)
        assertEquals((first.isoWeekday() - 1 + 7) % 7, mondayFirst.firstDayOffset)
        assertEquals(7, mondayFirst.weekdayLabels.size)
        assertTrue(mondayFirst.weekdayLabels.all { it.isNotBlank() })
    }

    @Test
    fun labelsAndDisabledDatesArePopulated() {
        val first = SolarHijriDate(1404, 1, 1)
        val today = first.toEpochDay() + 1
        val data = solarHijriNativeMonthData(first, todayEpochDay = today) { it != today }
        assertTrue(data.title.isNotBlank())
        assertTrue(data.days.all { it.label.isNotBlank() && it.accessibilityLabel.isNotBlank() })
        assertTrue(data.days.first().enabled)
        assertFalse(data.days[1].enabled)
        assertEquals(1, data.days.count { it.isToday })
        assertEquals(today, data.days.single { it.isToday }.epochDay)
    }

    @Test
    fun daysStayConsecutiveAcrossTheYearBoundary() {
        val december = solarHijriNativeMonthData(SolarHijriDate(1399, 12, 1), todayEpochDay = 0L)
        val january = solarHijriNativeMonthData(SolarHijriDate(1400, 1, 1), todayEpochDay = 0L)
        assertEquals(december.days.last().epochDay + 1, january.days.first().epochDay)
        assertTrue(december.days.zipWithNext().all { (a, b) -> b.epochDay == a.epochDay + 1 })
    }

    @Test
    fun outOfRangeAdjacentDaysRenderAsBlankDisabledCells() {
        val cell = solarHijriNativeDayCellForEpoch(SolarHijriEngine.MAX_EPOCH_DAY + 1, 0L) { true }
        assertEquals("", cell.label)
        assertEquals("", cell.accessibilityLabel)
        assertFalse(cell.enabled)
        assertFalse(cell.isToday)
    }
}
