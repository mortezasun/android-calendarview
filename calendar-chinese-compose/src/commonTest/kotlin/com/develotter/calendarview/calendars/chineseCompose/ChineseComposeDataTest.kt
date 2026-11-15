package com.develotter.calendarview.calendars.chineseCompose

import com.develotter.calendarview.calendars.chineseNative.ChineseCalendarEngine
import com.develotter.calendarview.calendars.chineseNative.ChineseDate
import com.develotter.calendarview.calendars.chineseNative.ChineseDateOutOfRangeException
import com.develotter.calendarview.calendars.chineseNative.ChineseFormat
import com.develotter.calendarview.calendars.chineseNative.ChineseMonth
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChineseComposeDataTest {
    private val monday = 1

    @Test
    fun leapMonthDataKeepsItsIdentityAndMarker() {
        val month = ChineseMonth.of(2023, 2, isLeapMonth = true)
        val leapDay = ChineseDate(2023, 2, true, 1).toEpochDay()
        val data = chineseMonthData(month, ChineseTextStyle.English, monday, todayEpochDay = leapDay)
        assertEquals("2023 Leap 2nd Month", data.title)
        assertEquals(month, data.month)
        assertEquals(29, data.days.size)
        assertEquals(2, data.firstDayOffset)
        assertEquals(7, data.weekdayLabels.size)
        assertEquals(leapDay, data.days.first().epochDay)
        assertTrue(data.days.first().isToday)
        assertEquals("1", data.days.first().label)
        assertTrue(data.days.first().accessibilityLabel.contains("Leap 2nd Month"))
        assertTrue(data.days.first().accessibilityLabel.contains("2023-03-22"))
    }

    @Test
    fun regularMonthHasTwelveMonthYearAndNoLeapMarker() {
        val month = ChineseMonth.of(2024, 1)
        val data = chineseMonthData(month, ChineseTextStyle.SimplifiedChinese, monday, todayEpochDay = 0L)
        assertEquals("2024年正月", data.title)
        assertEquals(29, data.days.size)
        assertFalse(data.title.contains("闰"))
        assertEquals(5, data.firstDayOffset)
    }

    @Test
    fun dayCellsUseStyleAndEnabledPredicate() {
        val leapDay = ChineseDate(2023, 2, true, 1).toEpochDay()
        val cell = chineseDayCell(leapDay, ChineseTextStyle.SimplifiedChinese, todayEpochDay = leapDay) { it != leapDay }
        assertEquals("初一", cell.label)
        assertFalse(cell.enabled)
        assertTrue(cell.isToday)
        assertTrue(cell.accessibilityLabel.contains("闰二月"))
        assertEquals(leapDay, cell.epochDay)

        val numeric = chineseDayCell(leapDay, ChineseTextStyle.Numeric, 0L)
        assertEquals("1", numeric.label)
        assertTrue(numeric.accessibilityLabel.contains("M2L"))
    }

    @Test
    fun weekContainsSevenConsecutiveDaysStartingOnTheConfiguredWeekday() {
        val epochDay = ChineseDate(2024, 1, false, 10).toEpochDay()
        val data = chineseWeekData(epochDay, ChineseTextStyle.English, monday, todayEpochDay = 0L)
        assertEquals(7, data.days.size)
        assertEquals(7, data.weekdayLabels.size)
        assertTrue(data.days.zipWithNext().all { (a, b) -> b.epochDay == a.epochDay + 1 })
        assertEquals(1, GregorianYearMonth.isoWeekdayFromEpochDay(data.days.first().epochDay))
        assertTrue(data.title.isNotBlank())
    }

    @Test
    fun weekOutsideTheSupportedRangeIsRejected() {
        assertFailsWith<ChineseDateOutOfRangeException> {
            chineseWeekData(ChineseCalendarEngine.MIN_EPOCH_DAY - 1, ChineseTextStyle.English, monday, todayEpochDay = 0L)
        }
    }

    @Test
    fun heatmapsCoverEveryMonthIncludingTheLeapMonth() {
        val blocks = chineseYearHeatmap(2023, ChineseTextStyle.English, monday, todayEpochDay = 0L)
        assertEquals(13, blocks.size)
        assertEquals(13, blocks.map { it.title }.distinct().size)
        assertTrue(blocks.all { it.weeks.isNotEmpty() })

        val single = chineseMonthHeatmap(
            year = 2023,
            monthNumber = 2,
            isLeapMonth = true,
            style = ChineseTextStyle.English,
            firstDayOfWeek = monday,
            todayEpochDay = 0L,
        )
        assertTrue(single.title.contains("Leap 2nd Month"))
    }

    @Test
    fun monthLookupForGregorianMonths() {
        val month = chineseMonthOfOrNull(GregorianYearMonth(2024, 2))
        assertEquals(ChineseMonth.of(2023, 12), month)
    }

    @Test
    fun monthStateNavigatesAndKeepsTheLeapFlag() {
        val initial = ChineseMonth.of(2024, 1)
        val state = ChineseMonthState(initial, today = { initial.startEpochDay() })
        state.next()
        assertEquals(ChineseMonth.of(2024, 2), state.month)
        state.previous()
        assertEquals(initial, state.month)
        assertTrue(state.canGoToday)
        state.goToToday()
        assertEquals(initial, state.month)
        assertTrue(state.title().isNotBlank())
    }

    @Test
    fun savedMonthIdentityRoundTrips() {
        val leapMonth = ChineseMonth.of(2023, 2, isLeapMonth = true)
        assertEquals("2023 Leap 2nd Month", ChineseFormat.monthTitle(leapMonth, ChineseTextStyle.English))
        assertEquals(leapMonth, ChineseMonthSnapshot.decode(ChineseMonthSnapshot.encode(leapMonth)))
        assertNull(ChineseMonthSnapshot.decode(listOf(2023, 2)))
        assertNull(ChineseMonthSnapshot.decode(listOf(2023, 2, 2)))
        assertNull(ChineseMonthSnapshot.decode(listOf(2023, 13, 0)))
    }

}
