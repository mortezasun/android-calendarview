package com.develotter.calendarviewsample

import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.calendars.gregorian.compose.gregorianDayCell
import com.develotter.calendarview.calendars.gregorian.compose.gregorianMonthData
import com.develotter.calendarview.calendars.hijri.HijriDate
import com.develotter.calendarview.calendars.hijri.UmmAlQuraHijriDateProvider
import com.develotter.calendarview.calendars.hijri.compose.hijriDayCell
import com.develotter.calendarview.calendars.hijri.compose.hijriMonthData
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriDayCell
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriMonthData
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.compose.solarHijriNativeDayCell
import com.develotter.calendarview.calendars.solarHijriNative.compose.solarHijriNativeMonthData
import com.develotter.calendarview.compose.buildCalendarMonthCells
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdjacentCalendarDaysTest {
    @Test
    fun gregorianYearBoundaryUsesGregorianLabels() {
        val data = gregorianMonthData(GregorianYearMonth(2027, 1), firstDayOfWeek = 1, todayEpochDay = 0L)
        val cells = buildCalendarMonthCells(data.days, data.firstDayOffset, true, true) {
            gregorianDayCell(it, todayEpochDay = 0L)
        }.filterNotNull()
        assertEquals(20815L, cells.first().epochDay) // 2026-12-28
        assertEquals("28", cells.first().label)
        assertFalse(cells.first().isCurrentMonth)
    }

    @Test
    fun hijriYearBoundaryPreservesChronologyAndDayNumbers() {
        val first = HijriDate(1448, 1, 1)
        val firstEpochDay = UmmAlQuraHijriDateProvider.epochDayFor(first)
        val data = hijriMonthData(first, firstDayOfWeek = 1, todayEpochDay = 0L)
        val cells = buildCalendarMonthCells(data.days, data.firstDayOffset, true, true) {
            hijriDayCell(it, todayEpochDay = 0L)
        }.filterNotNull()
        val previous = UmmAlQuraHijriDateProvider.dateForEpochDay(firstEpochDay - 1)
        assertEquals(1447, previous.year)
        assertEquals(firstEpochDay - 1, cells.first().epochDay)
        assertEquals(previous.dayOfMonth.toString(), cells.first().label)
        assertTrue(cells.zipWithNext().all { (a, b) -> b.epochDay == a.epochDay + 1 })
    }

    @Test
    fun jalaliNowruzUsesPreviousYearsLastDay() {
        val data = solarHijriMonthData(SolarHijriDate(1400, 1, 1), todayEpochDay = 0L)
        val cells = buildCalendarMonthCells(data.days, data.firstDayOffset, true, true) {
            solarHijriDayCell(it, todayEpochDay = 0L)
        }.filterNotNull()
        assertEquals(18706L, cells.first().epochDay) // 2021-03-20
        assertEquals("30", cells.first().label)
        assertFalse(cells.first().isCurrentMonth)
        assertTrue(cells.zipWithNext().all { (a, b) -> b.epochDay == a.epochDay + 1 })
    }

    @Test
    fun dayFactoriesRespectDisabledPredicatesAcrossCalendars() {
        assertFalse(gregorianDayCell(20819L, todayEpochDay = 0L) { false }.enabled)
        assertFalse(hijriDayCell(UmmAlQuraHijriDateProvider.epochDayFor(HijriDate(1448, 1, 1)), todayEpochDay = 0L) { false }.enabled)
        assertFalse(solarHijriDayCell(SolarHijriDate(1400, 1, 1).toEpochDay(), todayEpochDay = 0L) { false }.enabled)
        assertFalse(solarHijriNativeDayCell(SolarHijriDate(1400, 1, 1).toEpochDay(), todayEpochDay = 0L) { false }.enabled)
    }

    @Test
    fun jalaliNativeNowruzUsesPreviousYearsLastDay() {
        val data = solarHijriNativeMonthData(SolarHijriDate(1400, 1, 1), todayEpochDay = 0L)
        val cells = buildCalendarMonthCells(data.days, data.firstDayOffset, true, true) {
            solarHijriNativeDayCell(it, todayEpochDay = 0L)
        }.filterNotNull()
        assertEquals(18706L, cells.first().epochDay) // 2021-03-20
        assertEquals("30", cells.first().label)
        assertFalse(cells.first().isCurrentMonth)
        assertTrue(cells.zipWithNext().all { (a, b) -> b.epochDay == a.epochDay + 1 })
    }
}
