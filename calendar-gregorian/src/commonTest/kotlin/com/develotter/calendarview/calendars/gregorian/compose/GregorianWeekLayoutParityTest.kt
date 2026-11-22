package com.develotter.calendarview.calendars.gregorian.compose

import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.compose.heatmapQuarterData
import com.develotter.calendarview.compose.toHeatmap
import com.develotter.calendarview.locale.CalendarLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GregorianWeekLayoutParityTest {
    @Test
    fun quarterHeatmapMatchesMonthAndWeekForEveryExplicitStartDay() {
        val month = GregorianYearMonth(2019, 1)
        val anchor = month.atDay(1)
        for (firstDay in 1..7) {
            val monthData = gregorianMonthData(month, firstDay, anchor, CalendarLocale.German)
            val weekData = gregorianWeekData(anchor, firstDay, anchor, CalendarLocale.German)
            val quarter = heatmapQuarterData(2019, 1, firstDayOfWeek = firstDay)
            val quarterFirstWeek = quarter.weeks.first().map { it?.epochDay }
            assertEquals(quarterFirstWeek, monthData.toHeatmap().weeks.first().map { it?.epochDay })
            assertEquals(quarterFirstWeek, weekData.days.map { it.epochDay.takeIf { day -> day >= anchor } })
            assertEquals(monthData.firstDayOffset, quarterFirstWeek.indexOf(anchor))
        }
    }

    @Test
    fun gregorianSnapshotsRejectValuesOutsideTheIsoWeekdayRange() {
        val month = GregorianYearMonth(2019, 1)
        val anchor = month.atDay(1)
        for (firstDay in listOf(0, 8)) {
            assertFailsWith<IllegalArgumentException> {
                gregorianMonthData(month, firstDay, anchor, CalendarLocale.German)
            }
            assertFailsWith<IllegalArgumentException> {
                gregorianWeekData(anchor, firstDay, anchor, CalendarLocale.German)
            }
        }
    }
}
