package com.develotter.calendarviewsample

import com.develotter.calendarview.calendars.chineseNative.ChineseDate
import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.calendars.gregorian.compose.GregorianMonthState
import com.develotter.calendarview.calendars.gregorian.compose.gregorianMonthData
import com.develotter.calendarview.calendars.hijri.HijriDate
import com.develotter.calendarview.calendars.hijri.UmmAlQuraHijriDateProvider
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarSampleNavigationTest {
    private val gregorian = GregorianYearMonth.gregorianToEpochDay(2026, 12, 31)

    @Test
    fun gregorianMovesAcrossYearAndNormalizesToFirstDay() {
        assertEquals(
            GregorianYearMonth.gregorianToEpochDay(2027, 1, 1),
            moveSampleMonth(SampleCalendar.Gregorian, gregorian, 1),
        )
        assertEquals(
            GregorianYearMonth.gregorianToEpochDay(2026, 11, 1),
            moveSampleMonth(SampleCalendar.Gregorian, gregorian, -1),
        )
    }

    @Test
    fun hijriMovesAcrossYearInBothDirections() {
        val provider = UmmAlQuraHijriDateProvider
        val anchor = provider.epochDayFor(HijriDate(1447, 12, 15))
        assertEquals(provider.epochDayFor(HijriDate(1448, 1, 1)), moveSampleMonth(SampleCalendar.Hijri, anchor, 1))
        assertEquals(provider.epochDayFor(HijriDate(1447, 11, 1)), moveSampleMonth(SampleCalendar.Hijri, anchor, -1))
    }

    @Test
    fun jalaliMovesAcrossYearInBothDirections() {
        val anchor = SolarHijriDate(1399, 12, 15).toEpochDay()
        assertEquals(SolarHijriDate(1400, 1, 1).toEpochDay(), moveSampleMonth(SampleCalendar.SolarHijri, anchor, 1))
        assertEquals(SolarHijriDate(1399, 11, 1).toEpochDay(), moveSampleMonth(SampleCalendar.SolarHijri, anchor, -1))
    }

    @Test
    fun jalaliSupportsOffsetsLargerThanOneYearAndZero() {
        val anchor = SolarHijriDate(1400, 1, 15).toEpochDay()
        assertEquals(SolarHijriDate(1398, 12, 1).toEpochDay(), moveSampleMonth(SampleCalendar.SolarHijri, anchor, -13))
        assertEquals(SolarHijriDate(1401, 2, 1).toEpochDay(), moveSampleMonth(SampleCalendar.SolarHijri, anchor, 13))
        assertEquals(SolarHijriDate(1400, 1, 1).toEpochDay(), moveSampleMonth(SampleCalendar.SolarHijri, anchor, 0))
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
        val anchor = GregorianYearMonth.gregorianToEpochDay(2023, 2, 15)
        assertEquals(SampleCalendar.Gregorian, sampleMonthNavigationCalendar(SampleCalendar.Chinese, true))
        assertEquals(SampleCalendar.Chinese, sampleMonthNavigationCalendar(SampleCalendar.Chinese, false))
        assertEquals(SampleCalendar.Gregorian, sampleMonthNavigationCalendar(SampleCalendar.Gregorian, true))

        val overlayCalendar = sampleMonthNavigationCalendar(SampleCalendar.Chinese, true)
        assertEquals(
            GregorianYearMonth.gregorianToEpochDay(2023, 2, 1),
            moveSampleMonth(overlayCalendar, anchor, 0),
        )
        assertEquals(
            GregorianYearMonth.gregorianToEpochDay(2023, 3, 1),
            moveSampleMonth(overlayCalendar, anchor, 1),
        )
        assertEquals(
            GregorianYearMonth.gregorianToEpochDay(2023, 1, 1),
            moveSampleMonth(overlayCalendar, anchor, -1),
        )
        assertEquals(
            listOf(GregorianYearMonth.gregorianToEpochDay(2023, 2, 1)),
            sampleMonthWindow(overlayCalendar, anchor, radius = 0),
        )

        // Header path: the overlay uses the Gregorian month state and Gregorian title.
        val header = GregorianMonthState(GregorianYearMonth(2023, 2))
        header.next()
        assertEquals(GregorianYearMonth(2023, 3), header.month)
        assertEquals(gregorianMonthData(GregorianYearMonth(2023, 3), todayEpochDay = 0L).title, header.title())
        assertTrue(header.title().isNotBlank())
        header.previous()
        assertEquals(GregorianYearMonth(2023, 2), header.month)

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
        assertEquals(
            SolarHijriDate(1401, 2, 1).toEpochDay(),
            moveSampleMonth(SampleCalendar.SolarHijriNative, SolarHijriDate(1400, 1, 15).toEpochDay(), 13),
        )
    }
}
