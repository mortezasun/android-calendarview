package com.develotter.calendarview.calendars.gregorian.compose

import com.develotter.calendarview.calendars.gregorian.GregorianPlatformFormatter
import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.compose.toHeatmap
import com.develotter.calendarview.locale.CalendarLocale
import kotlin.test.Test
import kotlin.test.assertEquals

class GregorianWeekStartBaselineTest {
    @Test
    fun allWeekStartsAlignMonthWeekAndHeatmapAcrossTheYearBoundary() {
        val month = GregorianYearMonth(2019, 1)
        val anchor = month.atDay(1) // Tuesday.
        val locale = CalendarLocale.German
        val mondayFirst = gregorianMonthData(month, 1, anchor, locale)
        val expectedOffsets = listOf(1, 0, 6, 5, 4, 3, 2)
        for ((index, expectedOffset) in expectedOffsets.withIndex()) {
            val firstDay = index + 1
            val monthData = gregorianMonthData(month, firstDay, anchor, locale)
            val weekData = gregorianWeekData(anchor, firstDay, anchor, locale)
            val expectedLabels = mondayFirst.weekdayLabels.drop(index) + mondayFirst.weekdayLabels.take(index)
            val expectedStart = anchor - expectedOffset
            assertEquals(expectedOffset, monthData.firstDayOffset, "ISO start day $firstDay")
            assertEquals(expectedLabels, monthData.weekdayLabels)
            assertEquals(expectedLabels, weekData.weekdayLabels)
            assertEquals(expectedLabels, monthData.toHeatmap().weekdayLabels)
            assertEquals((expectedStart..(expectedStart + 6)).toList(), weekData.days.map { it.epochDay })
            assertEquals(mondayFirst.days, monthData.days)
        }
    }

    @Test
    fun explicitLabelLocaleDoesNotReplaceThePlatformWeekStart() {
        val month = GregorianYearMonth(2019, 1)
        val anchor = month.atDay(1)
        val locale = CalendarLocale.German
        val platformStart = GregorianPlatformFormatter.defaultFirstDayOfWeek()
        assertEquals(
            gregorianMonthData(month, platformStart, anchor, locale),
            gregorianMonthData(month, todayEpochDay = anchor, locale = locale),
        )
        assertEquals(
            gregorianWeekData(anchor, platformStart, anchor, locale),
            gregorianWeekData(anchor, todayEpochDay = anchor, locale = locale),
        )
    }
}
