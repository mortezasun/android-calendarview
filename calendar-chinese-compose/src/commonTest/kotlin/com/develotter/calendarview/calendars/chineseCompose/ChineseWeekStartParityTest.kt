package com.develotter.calendarview.calendars.chineseCompose

import com.develotter.calendarview.calendars.chineseNative.ChineseMonth
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.calendars.gregorian.compose.gregorianMonthData
import com.develotter.calendarview.compose.toHeatmap
import com.develotter.calendarview.locale.CalendarLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ChineseWeekStartParityTest {
    @Test
    fun leapMonthWeekAndHeatmapAgreeForAllStartDays() {
        val month = ChineseMonth.of(2023, 2, true)
        val anchor = month.startEpochDay()
        val style = ChineseTextStyle.TraditionalChinese
        for (firstDay in 1..7) {
            val data = chineseMonthData(month, style, firstDay, anchor)
            val week = chineseWeekData(anchor, style, firstDay, anchor)
            val heatmap = chineseMonthHeatmap(2023, 2, true, style, firstDay, anchor)
            assertEquals(month, data.month)
            assertEquals(data.toHeatmap(), heatmap)
            assertEquals(data.weekdayLabels, week.weekdayLabels)
            assertEquals(data.firstDayOffset, week.days.indexOfFirst { it.epochDay == anchor })
            assertEquals(data.days.first(), week.days[data.firstDayOffset])
        }
    }

    @Test
    fun localizedOverlayUsesTheGregorianPrimaryAlignment() {
        val month = GregorianYearMonth(2023, 3)
        val anchor = month.atDay(1)
        val locale = CalendarLocale.German
        for (firstDay in 1..7) {
            val primary = gregorianMonthData(month, firstDay, anchor, locale)
            val overlay = gregorianOverlayData(month, firstDay, anchor, locale)
            assertEquals(primary.weekdayLabels, overlay.weekdayLabels)
            assertEquals(primary.firstDayOffset, overlay.firstDayOffset)
            assertEquals(primary.days, overlay.days)
        }
    }

    @Test
    fun weekRejectsInvalidIsoStartDays() {
        for (firstDay in listOf(0, 8)) {
            assertFailsWith<IllegalArgumentException> {
                chineseWeekData(20_000L, firstDayOfWeek = firstDay, todayEpochDay = 20_000L)
            }
        }
    }

    @Test
    fun monthHeatmapAndOverlayRejectInvalidIsoStartDays() {
        val month = ChineseMonth.of(2023, 2, true)
        val anchor = month.startEpochDay()
        for (firstDay in listOf(0, 8)) {
            assertFailsWith<IllegalArgumentException> {
                chineseMonthData(month, ChineseTextStyle.English, firstDay, anchor)
            }
            assertFailsWith<IllegalArgumentException> {
                chineseMonthHeatmap(2023, 2, true, ChineseTextStyle.English, firstDay, anchor)
            }
            assertFailsWith<IllegalArgumentException> {
                chineseYearHeatmap(2023, ChineseTextStyle.English, firstDay, anchor)
            }
            assertFailsWith<IllegalArgumentException> {
                gregorianOverlayData(GregorianYearMonth(2023, 3), firstDay, anchor)
            }
        }
    }
}
