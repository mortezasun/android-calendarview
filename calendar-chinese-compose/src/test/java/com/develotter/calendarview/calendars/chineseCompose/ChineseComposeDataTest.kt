package com.develotter.calendarview.calendars.chineseCompose

import com.develotter.calendarview.calendars.chineseNative.ChineseCalendarEngine
import com.develotter.calendarview.calendars.chineseNative.ChineseDate
import com.develotter.calendarview.calendars.chineseNative.ChineseDateOutOfRangeException
import com.develotter.calendarview.calendars.chineseNative.ChineseMonth
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class ChineseComposeDataTest {
    private val monday = DayOfWeek.MONDAY

    @Test
    fun leapMonthDataKeepsItsIdentityAndMarker() {
        val month = ChineseMonth.of(2023, 2, isLeapMonth = true)
        val data = chineseMonthData(
            month = month,
            style = ChineseTextStyle.English,
            locale = java.util.Locale.ENGLISH,
            firstDayOfWeek = monday,
            todayEpochDay = LocalDate.of(2023, 3, 22).toEpochDay(),
        )
        assertEquals("2023 Leap 2nd Month", data.title)
        assertEquals(month, data.month)
        assertEquals(29, data.days.size)
        assertEquals(2, data.firstDayOffset)
        assertEquals(7, data.weekdayLabels.size)
        assertTrue(data.days.first().isToday)
        assertEquals("1", data.days.first().label)
        assertEquals(
            ChineseDate(2023, 2, true, 1).toEpochDay(),
            data.days.first().epochDay,
        )
        assertTrue(data.days.first().accessibilityLabel.contains("Leap 2nd Month"))
        assertTrue(data.days.first().accessibilityLabel.contains("2023-03-22"))
    }

    @Test
    fun regularMonthHasTwelveMonthYearAndNoLeapMarker() {
        val month = ChineseMonth.of(2024, 1)
        val data = chineseMonthData(
            month = month,
            style = ChineseTextStyle.SimplifiedChinese,
            locale = java.util.Locale.ENGLISH,
            firstDayOfWeek = monday,
            todayEpochDay = 0L,
        )
        assertEquals("2024年正月", data.title)
        assertEquals(29, data.days.size)
        assertFalse(data.title.contains("闰"))
        assertEquals(5, data.firstDayOffset)
    }

    @Test
    fun dayCellsUseStyleAndEnabledPredicate() {
        val leapDay = ChineseDate(2023, 2, true, 1).toEpochDay()
        val cell = chineseDayCell(leapDay, ChineseTextStyle.SimplifiedChinese, todayEpochDay = leapDay) { day ->
            day != leapDay
        }
        assertEquals("初一", cell.label)
        assertEquals(false, cell.enabled)
        assertTrue(cell.isToday)
        assertTrue(cell.accessibilityLabel.contains("闰二月"))
        assertEquals(leapDay, cell.epochDay)

        val numeric = chineseDayCell(leapDay, ChineseTextStyle.Numeric, 0L)
        assertEquals("1", numeric.label)
        assertTrue(numeric.accessibilityLabel.contains("M2L"))
    }

    @Test
    fun weekIsConsecutiveAndRejectsPartialBoundaryWeeks() {
        val epochDay = LocalDate.of(2023, 3, 22).toEpochDay()
        val week = chineseWeekData(
            epochDay = epochDay,
            style = ChineseTextStyle.English,
            locale = java.util.Locale.ENGLISH,
            firstDayOfWeek = monday,
            todayEpochDay = epochDay,
        )
        assertEquals(7, week.days.size)
        assertEquals(7, week.days.map { it.epochDay }.toSet().size)
        week.days.zipWithNext().forEach { (a, b) -> assertEquals(a.epochDay + 1, b.epochDay) }
        assertEquals(epochDay, week.days[2].epochDay)
        assertTrue(week.title.contains("2023"))

        try {
            chineseWeekData(
                epochDay = ChineseCalendarEngine.MIN_EPOCH_DAY,
                locale = java.util.Locale.ENGLISH,
                firstDayOfWeek = monday,
            )
            fail("a week across the range start must be rejected")
        } catch (expected: ChineseDateOutOfRangeException) {
            assertTrue(expected.message!!.contains("outside"))
        }
    }

    @Test
    fun monthStateNavigatesLeapMonthsAndHonoursBounds() {
        val state = ChineseMonthState(
            initialMonth = ChineseMonth.of(2023, 2),
            minMonth = ChineseMonth.of(2023, 1),
            maxMonth = ChineseMonth.of(2023, 3),
            today = { ChineseCalendarEngine.MIN_EPOCH_DAY },
        )
        assertEquals("2023 2nd Month", state.title())
        assertTrue(state.canGoNext)
        state.next()
        assertEquals("2023 Leap 2nd Month", state.title())
        assertTrue(state.month.isLeapMonth)
        state.next()
        assertEquals(ChineseMonth.of(2023, 3), state.month)
        assertFalse(state.canGoNext)
        state.next()
        assertEquals(ChineseMonth.of(2023, 3), state.month)
        state.previous()
        assertTrue(state.month.isLeapMonth)
        try {
            state.goTo(ChineseMonth.of(2024, 1))
            fail("out-of-bounds month must be rejected")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message!!.contains("minMonth"))
        }
        assertTrue(state.canGoToday)
    }

    @Test
    fun snapshotRoundTripsLeapIdentityAndRejectsInvalidValues() {
        val leap = ChineseMonth.of(2033, 11, isLeapMonth = true)
        val encoded = ChineseMonthSnapshot.encode(leap)
        assertEquals(listOf(2033, 11, 1), encoded)
        assertEquals(leap, ChineseMonthSnapshot.decode(encoded))
        assertEquals(ChineseMonth.of(2024, 2), ChineseMonthSnapshot.decode(listOf(2024, 2, 0)))
        assertNull(ChineseMonthSnapshot.decode(listOf(2024, 2, 1)))
        assertNull(ChineseMonthSnapshot.decode(listOf(2024)))
        assertNull(ChineseMonthSnapshot.decode(listOf(1800, 1, 0)))
    }

    @Test
    fun yearHeatmapHasTwelveOrThirteenBlocksAndKeepsLeapTitle() {
        val thirteen = chineseYearHeatmap(
            year = 2023,
            style = ChineseTextStyle.English,
            locale = java.util.Locale.ENGLISH,
            firstDayOfWeek = monday,
            todayEpochDay = 0L,
        )
        assertEquals(13, thirteen.size)
        assertEquals("2023 Leap 2nd Month", thirteen[2].title)
        assertTrue(thirteen.all { it.weekdayLabels.size == 7 && it.weeks.all { week -> week.size == 7 } })

        val twelve = chineseYearHeatmap(
            year = 2024,
            style = ChineseTextStyle.English,
            locale = java.util.Locale.ENGLISH,
            firstDayOfWeek = monday,
            todayEpochDay = 0L,
        )
        assertEquals(12, twelve.size)
        assertEquals("2024 1st Month", twelve.first().title)

        val singles = chineseMonthHeatmap(
            year = 2023,
            monthNumber = 2,
            isLeapMonth = true,
            style = ChineseTextStyle.English,
            locale = java.util.Locale.ENGLISH,
            firstDayOfWeek = monday,
        )
        assertEquals("2023 Leap 2nd Month", singles.title)
        assertEquals(29, singles.weeks.sumOf { week -> week.count { it != null } })
    }

    @Test
    fun gregorianOverlayResolvesChineseMonthsOnlyInsideTheRange() {
        assertEquals(ChineseMonth.of(2023, 12), chineseMonthOfOrNull(YearMonth.of(2024, 2)))
        assertNotNull(chineseMonthOfOrNull(YearMonth.of(1901, 3)))
        assertNull(chineseMonthOfOrNull(YearMonth.of(1901, 1)))
        assertNull(chineseMonthOfOrNull(YearMonth.of(2100, 6)))
    }
}
