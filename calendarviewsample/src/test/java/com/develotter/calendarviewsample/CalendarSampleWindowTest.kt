package com.develotter.calendarviewsample

import com.develotter.calendarview.calendars.chineseNative.ChineseCalendarEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CalendarSampleWindowTest {
    private val anchor = LocalDate.of(2026, 11, 9).toEpochDay()

    @Test
    fun everyEngineBuildsOrderedUniqueWindowAroundItsOwnMonth() {
        SampleCalendar.entries.forEach { calendar ->
            val months = sampleMonthWindow(calendar, anchor)
            assertEquals(25, months.size)
            assertEquals(25, months.distinct().size)
            assertEquals(moveSampleMonth(calendar, anchor, 0), months[12])
            assertTrue(months.zipWithNext().all { (a, b) -> b > a })
        }
    }

    @Test
    fun aRequestedNeighborIsFoundWithoutRecenteringTheWindow() {
        SampleCalendar.entries.forEach { calendar ->
            val months = sampleMonthWindow(calendar, anchor)
            assertEquals(15, months.indexOf(moveSampleMonth(calendar, anchor, 3)))
            assertEquals(9, months.indexOf(moveSampleMonth(calendar, anchor, -3)))
            assertEquals(-1, months.indexOf(moveSampleMonth(calendar, anchor, 13)))
        }
    }

    @Test
    fun zeroRadiusNormalizesToOneMonth() {
        SampleCalendar.entries.forEach { calendar ->
            assertEquals(listOf(moveSampleMonth(calendar, anchor, 0)), sampleMonthWindow(calendar, anchor, 0))
        }
    }

    @Test
    fun chineseWindowIsTruncatedAtBothEngineBounds() {
        val lower = sampleMonthWindow(SampleCalendar.Chinese, ChineseCalendarEngine.MIN_EPOCH_DAY)
        assertEquals(13, lower.size)
        assertEquals(moveSampleMonth(SampleCalendar.Chinese, ChineseCalendarEngine.MIN_EPOCH_DAY, 0), lower.first())
        assertEquals(moveSampleMonth(SampleCalendar.Chinese, ChineseCalendarEngine.MIN_EPOCH_DAY, 12), lower.last())
        assertEquals(1, lower.indexOf(moveSampleMonth(SampleCalendar.Chinese, ChineseCalendarEngine.MIN_EPOCH_DAY, 1)))
        assertTrue(lower.zipWithNext().all { (a, b) -> b > a })

        val upper = sampleMonthWindow(SampleCalendar.Chinese, ChineseCalendarEngine.MAX_EPOCH_DAY)
        assertEquals(13, upper.size)
        assertEquals(moveSampleMonth(SampleCalendar.Chinese, ChineseCalendarEngine.MAX_EPOCH_DAY, -12), upper.first())
        assertEquals(moveSampleMonth(SampleCalendar.Chinese, ChineseCalendarEngine.MAX_EPOCH_DAY, 0), upper.last())
        assertEquals(upper.lastIndex - 1, upper.indexOf(moveSampleMonth(SampleCalendar.Chinese, ChineseCalendarEngine.MAX_EPOCH_DAY, -1)))
        assertTrue(upper.zipWithNext().all { (a, b) -> b > a })
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidRadiusIsRejected() {
        sampleMonthWindow(SampleCalendar.Gregorian, anchor, -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidChineseCenterStillFailsInsteadOfReturningAnEmptyWindow() {
        sampleMonthWindow(SampleCalendar.Chinese, ChineseCalendarEngine.MIN_EPOCH_DAY - 1)
    }
}
