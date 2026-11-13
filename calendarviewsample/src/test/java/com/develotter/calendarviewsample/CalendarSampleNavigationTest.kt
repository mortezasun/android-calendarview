package com.develotter.calendarviewsample

import com.develotter.calendarview.calendars.chineseNative.ChineseDate
import com.develotter.calendarview.calendars.gregorian.compose.GregorianMonthState
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriEpochDay
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import ir.huri.jcal.JalaliCalendar
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.time.chrono.HijrahDate
import java.util.Locale

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
    fun chineseTraversesTheLeapMonthAndNormalizesToDayOne() {
        val anchor = ChineseDate(2023, 2, true, 18).toEpochDay()
        assertEquals(ChineseDate(2023, 3, false, 1).toEpochDay(), moveSampleMonth(SampleCalendar.Chinese, anchor, 1))
        assertEquals(ChineseDate(2023, 2, false, 1).toEpochDay(), moveSampleMonth(SampleCalendar.Chinese, anchor, -1))
        assertEquals(ChineseDate(2023, 2, true, 1).toEpochDay(), moveSampleMonth(SampleCalendar.Chinese, anchor, 0))
        val newYear = ChineseDate(2024, 1, false, 20).toEpochDay()
        assertEquals(ChineseDate(2024, 2, false, 1).toEpochDay(), moveSampleMonth(SampleCalendar.Chinese, newYear, 1))
        assertEquals(ChineseDate(2023, 12, false, 1).toEpochDay(), moveSampleMonth(SampleCalendar.Chinese, newYear, -1))
    }

    @Test
    fun chineseOverlayUsesGregorianMonthStepsAndIdentities() {
        val anchor = LocalDate.of(2023, 2, 15).toEpochDay()
        assertEquals(SampleCalendar.Gregorian, sampleMonthNavigationCalendar(SampleCalendar.Chinese, true))
        assertEquals(SampleCalendar.Chinese, sampleMonthNavigationCalendar(SampleCalendar.Chinese, false))
        assertEquals(SampleCalendar.Gregorian, sampleMonthNavigationCalendar(SampleCalendar.Gregorian, true))

        val overlayCalendar = sampleMonthNavigationCalendar(SampleCalendar.Chinese, true)
        assertEquals(LocalDate.of(2023, 2, 1).toEpochDay(), moveSampleMonth(overlayCalendar, anchor, 0))
        assertEquals(LocalDate.of(2023, 3, 1).toEpochDay(), moveSampleMonth(overlayCalendar, anchor, 1))
        assertEquals(LocalDate.of(2023, 1, 1).toEpochDay(), moveSampleMonth(overlayCalendar, anchor, -1))
        assertEquals(
            listOf(LocalDate.of(2023, 2, 1).toEpochDay()),
            sampleMonthWindow(overlayCalendar, anchor, radius = 0),
        )

        // Header path: the overlay uses the Gregorian month state and Gregorian title.
        val header = GregorianMonthState(YearMonth.of(2023, 2))
        header.next()
        assertEquals(YearMonth.of(2023, 3), header.month)
        assertEquals("March 2023", header.title(Locale.ENGLISH))
        header.previous()
        assertEquals(YearMonth.of(2023, 2), header.month)

        // The real Chinese mode still traverses the leap month.
        val chinese = sampleMonthNavigationCalendar(SampleCalendar.Chinese, false)
        val leap = ChineseDate(2023, 2, true, 1).toEpochDay()
        assertEquals(ChineseDate(2023, 3, false, 1).toEpochDay(), moveSampleMonth(chinese, leap, 1))
        assertEquals(ChineseDate(2023, 2, false, 1).toEpochDay(), moveSampleMonth(chinese, leap, -1))
    }

    @Test
    fun jalaliNativeMovesAcrossYearAndOffsets() {
        val anchor = SolarHijriDate(1399, 12, 15).toEpochDay()
        assertEquals(SolarHijriDate(1400, 1, 1).toEpochDay(), moveSampleMonth(SampleCalendar.SolarHijriNative, anchor, 1))
        assertEquals(SolarHijriDate(1399, 11, 1).toEpochDay(), moveSampleMonth(SampleCalendar.SolarHijriNative, anchor, -1))
        assertEquals(SolarHijriDate(1401, 2, 1).toEpochDay(), moveSampleMonth(SampleCalendar.SolarHijriNative, SolarHijriDate(1400, 1, 15).toEpochDay(), 13))
    }
}
