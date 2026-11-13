package com.develotter.calendarviewsample

import com.develotter.calendarview.calendars.gregorian.compose.gregorianDayCell
import com.develotter.calendarview.calendars.gregorian.compose.gregorianMonthData
import com.develotter.calendarview.calendars.hijri.compose.hijriDayCell
import com.develotter.calendarview.calendars.hijri.compose.hijriMonthData
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriDayCell
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriMonthData
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.compose.solarHijriNativeDayCell
import com.develotter.calendarview.calendars.solarHijriNative.compose.solarHijriNativeMonthData
import com.develotter.calendarview.compose.buildCalendarMonthCells
import ir.huri.jcal.JalaliCalendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.util.Locale

class AdjacentCalendarDaysTest {
    @Test
    fun gregorianYearBoundaryUsesGregorianLabels() {
        val data = gregorianMonthData(YearMonth.of(2027, 1), Locale.US, DayOfWeek.MONDAY)
        val cells = buildCalendarMonthCells(data.days, data.firstDayOffset, true, true) { gregorianDayCell(LocalDate.ofEpochDay(it), Locale.US) }.filterNotNull()
        assertEquals(LocalDate.of(2026, 12, 28).toEpochDay(), cells.first().epochDay)
        assertEquals("28", cells.first().label)
        assertFalse(cells.first().isCurrentMonth)
    }

    @Test
    fun hijriYearBoundaryPreservesChronologyAndDayNumbers() {
        val first = HijrahDate.of(1448, 1, 1)
        val weekday = DayOfWeek.of(first.get(ChronoField.DAY_OF_WEEK)).minus(1)
        val data = hijriMonthData(first, Locale.US, weekday)
        val cells = buildCalendarMonthCells(data.days, data.firstDayOffset, true, true) { hijriDayCell(first.chronology.dateEpochDay(it), Locale.US) }.filterNotNull()
        val previous = first.chronology.dateEpochDay(first.toEpochDay() - 1)
        assertEquals(1447, previous.get(ChronoField.YEAR))
        assertEquals(previous.toEpochDay(), cells.first().epochDay)
        assertEquals(previous.get(ChronoField.DAY_OF_MONTH).toString(), cells.first().label)
        assertTrue(cells.zipWithNext().all { (a, b) -> b.epochDay == a.epochDay + 1 })
    }

    @Test
    fun jalaliNowruzUsesPreviousYearsLastDay() {
        val data = solarHijriMonthData(JalaliCalendar(1400, 1, 1), Locale.US)
        val cells = buildCalendarMonthCells(data.days, data.firstDayOffset, true, true) { solarHijriDayCell(JalaliCalendar(LocalDate.ofEpochDay(it)), Locale.US) }.filterNotNull()
        assertEquals(LocalDate.of(2021, 3, 20).toEpochDay(), cells.first().epochDay)
        assertEquals("30", cells.first().label)
        assertFalse(cells.first().isCurrentMonth)
        assertTrue(cells.zipWithNext().all { (a, b) -> b.epochDay == a.epochDay + 1 })
    }

    @Test
    fun dayFactoriesRespectDisabledPredicatesAcrossCalendars() {
        assertFalse(gregorianDayCell(LocalDate.of(2027, 1, 1), Locale.US) { false }.enabled)
        assertFalse(hijriDayCell(HijrahDate.of(1448, 1, 1), Locale.US) { false }.enabled)
        val original = JalaliCalendar(1400, 1, 1)
        assertFalse(solarHijriDayCell(original, Locale.US) { false }.enabled)
        assertEquals(1, original.day)
        assertFalse(solarHijriNativeDayCell(SolarHijriDate(1400, 1, 1), Locale.US) { false }.enabled)
    }

    @Test
    fun jalaliNativeNowruzUsesPreviousYearsLastDay() {
        val data = solarHijriNativeMonthData(SolarHijriDate(1400, 1, 1), Locale.US)
        val cells = buildCalendarMonthCells(data.days, data.firstDayOffset, true, true) {
            solarHijriNativeDayCell(SolarHijriDate.fromEpochDay(it), Locale.US)
        }.filterNotNull()
        assertEquals(LocalDate.of(2021, 3, 20).toEpochDay(), cells.first().epochDay)
        assertEquals("30", cells.first().label)
        assertFalse(cells.first().isCurrentMonth)
        assertTrue(cells.zipWithNext().all { (a, b) -> b.epochDay == a.epochDay + 1 })
    }
}
