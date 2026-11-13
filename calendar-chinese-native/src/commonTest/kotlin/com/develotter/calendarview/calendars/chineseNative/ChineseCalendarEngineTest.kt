package com.develotter.calendarview.calendars.chineseNative

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChineseCalendarEngineTest {
    @Test
    fun knownNewYearDates() {
        val newYears = listOf(
            -25153L to 1901,
            -7258L to 1950,
            10992L to 2000,
            16101L to 2014,
            17194L to 2017,
            18286L to 2020,
            19379L to 2023,
            19763L to 2024,
            20117L to 2025,
            23041L to 2033,
            23425L to 2034,
            47137L to 2099,
        )
        newYears.forEach { (epochDay, year) ->
            val date = ChineseCalendarEngine.fromEpochDay(epochDay)
            assertEquals(year, date.year)
            assertEquals(1, date.monthNumber)
            assertFalse(date.isLeapMonth)
            assertEquals(1, date.dayOfMonth)
            assertEquals(epochDay, date.toEpochDay())
            assertEquals(epochDay, ChineseCalendarEngine.yearStartEpochDay(year))
        }
    }

    @Test
    fun knownLeapMonths() {
        val leaps = listOf(
            2014 to (9 to 16101L + 266),
            2017 to (6 to 17194L + 176),
            2020 to (4 to 18286L + 119),
            2023 to (2 to 19379L + 59),
            2025 to (6 to 20117L + 177),
            2033 to (11 to 23041L + 325),
        )
        leaps.forEach { (year, expectation) ->
            val (number, start) = expectation
            val leap = ChineseCalendarEngine.leapMonth(year)
            assertEquals(number, leap?.monthNumber)
            assertTrue(leap!!.isLeapMonth)
            assertEquals(start, leap.startEpochDay())
            assertEquals(13, ChineseCalendarEngine.monthsInYear(year).size)
        }
        assertNull(ChineseCalendarEngine.leapMonth(2024))
        assertEquals(12, ChineseCalendarEngine.monthsInYear(2024).size)
        assertNull(ChineseCalendarEngine.leapMonth(2034))
    }

    @Test
    fun leapMonthFollowsItsRegularMonth() {
        val months = ChineseCalendarEngine.monthsInYear(2023)
        assertEquals(
            listOf(
                1 to false, 2 to false, 2 to true, 3 to false, 4 to false, 5 to false, 6 to false,
                7 to false, 8 to false, 9 to false, 10 to false, 11 to false, 12 to false,
            ),
            months.map { it.monthNumber to it.isLeapMonth },
        )
        assertEquals("2023-2", months[1].toString())
        assertEquals("2023-2L", months[2].toString())
    }

    @Test
    fun documentedHkoConflictsRemainOnTheUtcPlus8CivilDate() {
        // The HKO reference labels the UT date for these three legacy cases. The audited
        // new-moon instants (JDE - Delta T) fall a few minutes after UTC+8 midnight, so the
        // generated table deliberately keeps the following civil date. See
        // tools/chinese-calendar/reference-conflicts.json for the independent NASA times.
        val cases = listOf(
            -20133L to Triple(1914, 10, false),
            -19690L to Triple(1916, 1, false),
            -17948L to Triple(1920, 10, false),
        )
        cases.forEach { (epochDay, expected) ->
            val (year, monthNumber, leap) = expected
            val date = ChineseCalendarEngine.fromEpochDay(epochDay)
            assertEquals(year, date.year)
            assertEquals(monthNumber, date.monthNumber)
            assertEquals(leap, date.isLeapMonth)
            assertEquals(1, date.dayOfMonth)
            assertEquals(epochDay, date.toEpochDay())
            assertEquals(epochDay, ChineseCalendarEngine.monthStartEpochDay(date.month))
            assertTrue(ChineseCalendarEngine.fromEpochDay(epochDay - 1).dayOfMonth > 1)
        }
        assertEquals(-19690L, ChineseCalendarEngine.yearStartEpochDay(1916))
    }

    @Test
    fun wholeRangeRoundTrips() {
        var epochDay = ChineseCalendarEngine.MIN_EPOCH_DAY
        var days = 0
        while (epochDay <= ChineseCalendarEngine.MAX_EPOCH_DAY) {
            val date = ChineseCalendarEngine.fromEpochDay(epochDay)
            assertEquals(epochDay, ChineseCalendarEngine.toEpochDay(date))
            epochDay += 1
            days += 1
        }
        assertEquals(72_674, days)
    }

    @Test
    fun monthSequenceIsChronologicalAndGapless() {
        var month = ChineseCalendarEngine.firstMonthOfYear(ChineseCalendarEngine.MIN_YEAR)
        var totalMonths = 0
        var previousStart = Long.MIN_VALUE
        while (true) {
            val start = ChineseCalendarEngine.monthStartEpochDay(month)
            if (totalMonths > 0) {
                assertEquals(previousStart + ChineseCalendarEngine.lengthOfMonth(ChineseCalendarEngine.monthOf(previousStart)), start)
            }
            previousStart = start
            totalMonths += 1
            if (month.year == ChineseCalendarEngine.MAX_YEAR && month.monthNumber == 12 && !month.isLeapMonth) break
            month = ChineseCalendarEngine.nextMonth(month)
        }
        assertEquals(2461, totalMonths)
        assertEquals(
            ChineseCalendarEngine.MAX_EPOCH_DAY,
            ChineseCalendarEngine.monthStartEpochDay(month) + ChineseCalendarEngine.lengthOfMonth(month) - 1,
        )
    }

    @Test
    fun monthAndYearLengthsStayInDocumentedRanges() {
        var years = 0
        for (year in ChineseCalendarEngine.MIN_YEAR..ChineseCalendarEngine.MAX_YEAR) {
            years += 1
            val hasLeap = ChineseCalendarEngine.hasLeapMonth(year)
            val expectedMonths = if (hasLeap) 13 else 12
            assertEquals(expectedMonths, ChineseCalendarEngine.monthsInYear(year).size)
            val length = ChineseCalendarEngine.lengthOfYear(year)
            assertTrue(if (hasLeap) length in 383..385 else length in 353..355, "year $year length $length")
            ChineseCalendarEngine.monthsInYear(year).forEach { month ->
                assertTrue(ChineseCalendarEngine.lengthOfMonth(month) in 29..30)
                assertEquals(month.startEpochDay(), ChineseCalendarEngine.monthStartEpochDay(month))
            }
        }
        assertEquals(199, years)
    }

    @Test
    fun monthArithmeticTraversesLeapMonthAndClampsDay() {
        val month = ChineseMonth.of(2023, 2)
        val leap = ChineseCalendarEngine.nextMonth(month)
        assertEquals(2, leap.monthNumber)
        assertTrue(leap.isLeapMonth)
        assertEquals(ChineseMonth.of(2023, 3), ChineseCalendarEngine.nextMonth(leap))

        val lastDay = ChineseDate(2023, 2, false, 30)
        val clamped = lastDay.plusMonths(1)
        assertEquals(ChineseDate(2023, 2, true, 29), clamped)
        assertEquals(lastDay, lastDay.plusMonths(0))
    }

    @Test
    fun dayAndYearArithmeticClampOrReject() {
        val date = ChineseDate(2024, 2, false, 30)
        assertEquals(ChineseDate(2024, 3, false, 29), date.plusMonths(1))
        assertEquals(30, date.withYear(2023).dayOfMonth)
        assertEquals(ChineseDate(2023, 2, true, 29), date.plusMonths(-12))
        assertEquals(ChineseDate(2023, 2, false, 30), date.plusMonths(-13))
        assertEquals(ChineseDate(2024, 1, false, 29), date.withMonth(1))
        assertFailsWith<IllegalArgumentException> { date.withMonth(2, isLeapMonth = true) }
    }

    @Test
    fun invalidValuesAreRejected() {
        assertFailsWith<IllegalArgumentException> { ChineseMonth.of(2024, 0) }
        assertFailsWith<IllegalArgumentException> { ChineseMonth.of(2024, 13) }
        assertFailsWith<IllegalArgumentException> { ChineseMonth.of(2024, 2, isLeapMonth = true) }
        assertFailsWith<IllegalArgumentException> { ChineseDate(2024, 1, false, 0) }
        assertFailsWith<IllegalArgumentException> { ChineseDate(2024, 1, false, 31) }
        assertFailsWith<IllegalArgumentException> { ChineseDate(2023, 3, false, 30) }
        assertFailsWith<IllegalArgumentException> { ChineseDate(1899, 1, false, 1) }
    }

    @Test
    fun rangeAndExtremeOperandsFailConsistently() {
        assertFailsWith<ChineseDateOutOfRangeException> {
            ChineseCalendarEngine.fromEpochDay(ChineseCalendarEngine.MIN_EPOCH_DAY - 1)
        }
        assertFailsWith<ChineseDateOutOfRangeException> {
            ChineseCalendarEngine.fromEpochDay(ChineseCalendarEngine.MAX_EPOCH_DAY + 1)
        }
        assertFailsWith<ChineseDateOutOfRangeException> { ChineseCalendarEngine.fromEpochDay(Long.MIN_VALUE) }
        assertFailsWith<ChineseDateOutOfRangeException> { ChineseCalendarEngine.fromEpochDay(Long.MAX_VALUE) }
        val first = ChineseCalendarEngine.fromEpochDay(ChineseCalendarEngine.MIN_EPOCH_DAY)
        assertFailsWith<ChineseDateOutOfRangeException> { first.minusDays(1) }
        assertFailsWith<ChineseDateOutOfRangeException> { first.minusMonths(1) }
        val last = ChineseCalendarEngine.fromEpochDay(ChineseCalendarEngine.MAX_EPOCH_DAY)
        assertFailsWith<ChineseDateOutOfRangeException> { last.plusDays(1) }
        assertFailsWith<ChineseDateOutOfRangeException> { last.plusMonths(1) }
        assertFailsWith<ChineseDateOutOfRangeException> { first.plusDays(Long.MAX_VALUE) }
        assertFailsWith<ChineseDateOutOfRangeException> { first.minusDays(Long.MIN_VALUE) }
        assertFailsWith<ChineseDateOutOfRangeException> { first.plusMonths(Long.MAX_VALUE) }
        assertFailsWith<ChineseDateOutOfRangeException> { last.minusMonths(Long.MIN_VALUE) }
        assertFailsWith<ChineseDateOutOfRangeException> {
            ChineseCalendarEngine.nextMonth(ChineseCalendarEngine.lastMonthOfYear(ChineseCalendarEngine.MAX_YEAR))
        }
        assertFailsWith<ChineseDateOutOfRangeException> {
            ChineseCalendarEngine.previousMonth(ChineseCalendarEngine.firstMonthOfYear(ChineseCalendarEngine.MIN_YEAR))
        }
    }

    @Test
    fun dayOfWeekMatchesEpochDayIdentity() {
        // 1970-01-01 is a Thursday, index 3 with Monday = 0.
        assertEquals(3, ChineseMonth.dayOfWeekIndex(0))
        assertEquals(0, ChineseMonth.dayOfWeekIndex(-3))
        assertEquals(2, ChineseMonth.dayOfWeekIndex(-1))
        assertEquals(5, ChineseMonth.dayOfWeekIndex(19763))
        val date = ChineseDate(2024, 1, false, 1)
        assertEquals(ChineseMonth.dayOfWeekIndex(date.toEpochDay()), date.dayOfWeekIndex())
    }

    @Test
    fun formattingMarksLeapMonthsInEveryStyle() {
        val leap = ChineseMonth.of(2023, 2, isLeapMonth = true)
        assertEquals("2023 Leap 2nd Month", ChineseFormat.monthTitle(leap, ChineseTextStyle.English))
        assertEquals("2023年闰二月", ChineseFormat.monthTitle(leap, ChineseTextStyle.SimplifiedChinese))
        assertEquals("2023年閏二月", ChineseFormat.monthTitle(leap, ChineseTextStyle.TraditionalChinese))
        assertEquals("2023 M2L", ChineseFormat.monthTitle(leap, ChineseTextStyle.Numeric))
        assertEquals("初十", ChineseFormat.dayLabel(10, ChineseTextStyle.SimplifiedChinese))
        assertEquals("廿九", ChineseFormat.dayLabel(29, ChineseTextStyle.SimplifiedChinese))
        assertEquals("三十", ChineseFormat.dayLabel(30, ChineseTextStyle.TraditionalChinese))
        assertEquals("29", ChineseFormat.dayLabel(29, ChineseTextStyle.English))
        assertEquals(
            "2023年闰二月廿九",
            ChineseFormat.dateLabel(ChineseDate(2023, 2, true, 29), ChineseTextStyle.SimplifiedChinese),
        )
    }

    @Test
    fun datasetVersionIsExposed() {
        assertEquals("2026.11-astronomical-v1", ChineseCalendarEngine.DATASET_VERSION)
        assertEquals(ChineseCalendarEngine.MIN_EPOCH_DAY, ChineseDate.fromEpochDay(ChineseCalendarEngine.MIN_EPOCH_DAY).toEpochDay())
    }
}
