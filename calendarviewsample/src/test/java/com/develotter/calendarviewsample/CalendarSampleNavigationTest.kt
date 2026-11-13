package com.develotter.calendarviewsample

import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriEpochDay
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import ir.huri.jcal.JalaliCalendar
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.chrono.HijrahDate

class CalendarSampleNavigationTest {
    @Test
    fun gregorianMovesAcrossYearAndNormalizesToFirstDay() {
        val anchor = LocalDate.of(2026, 12, 31).toEpochDay()
        assertEquals(LocalDate.of(2027, 1, 1).toEpochDay(), moveSampleMonth(SampleCalendar.Gregorian, anchor, 1))
        assertEquals(LocalDate.of(2026, 11, 1).toEpochDay(), moveSampleMonth(SampleCalendar.Gregorian, anchor, -1))
    }

    @Test
    fun hijriMovesAcrossYearInBothDirections() {
        val anchor = HijrahDate.of(1447, 12, 15).toEpochDay()
        assertEquals(HijrahDate.of(1448, 1, 1).toEpochDay(), moveSampleMonth(SampleCalendar.Hijri, anchor, 1))
        assertEquals(HijrahDate.of(1447, 11, 1).toEpochDay(), moveSampleMonth(SampleCalendar.Hijri, anchor, -1))
    }

    @Test
    fun jalaliMovesAcrossYearInBothDirections() {
        val anchor = solarHijriEpochDay(JalaliCalendar(1399, 12, 15))
        assertEquals(solarHijriEpochDay(JalaliCalendar(1400, 1, 1)), moveSampleMonth(SampleCalendar.SolarHijri, anchor, 1))
        assertEquals(solarHijriEpochDay(JalaliCalendar(1399, 11, 1)), moveSampleMonth(SampleCalendar.SolarHijri, anchor, -1))
    }

    @Test
    fun jalaliSupportsOffsetsLargerThanOneYearAndZero() {
        val anchor = solarHijriEpochDay(JalaliCalendar(1400, 1, 15))
        assertEquals(solarHijriEpochDay(JalaliCalendar(1398, 12, 1)), moveSampleMonth(SampleCalendar.SolarHijri, anchor, -13))
        assertEquals(solarHijriEpochDay(JalaliCalendar(1401, 2, 1)), moveSampleMonth(SampleCalendar.SolarHijri, anchor, 13))
        assertEquals(solarHijriEpochDay(JalaliCalendar(1400, 1, 1)), moveSampleMonth(SampleCalendar.SolarHijri, anchor, 0))
    }

    @Test
    fun jalaliNativeMovesAcrossYearAndOffsets() {
        val anchor = SolarHijriDate(1399, 12, 15).toEpochDay()
        assertEquals(SolarHijriDate(1400, 1, 1).toEpochDay(), moveSampleMonth(SampleCalendar.SolarHijriNative, anchor, 1))
        assertEquals(SolarHijriDate(1399, 11, 1).toEpochDay(), moveSampleMonth(SampleCalendar.SolarHijriNative, anchor, -1))
        assertEquals(SolarHijriDate(1401, 2, 1).toEpochDay(), moveSampleMonth(SampleCalendar.SolarHijriNative, SolarHijriDate(1400, 1, 15).toEpochDay(), 13))
    }
}
