package com.develotter.calendarview.calendars.gregorian.compose

import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GregorianMonthDataTest {
    @Test
    fun februaryRespectsGregorianLeapYearRules() {
        assertEquals(29, gregorianMonthData(GregorianYearMonth(2024, 2), todayEpochDay = 0L).days.size)
        assertEquals(28, gregorianMonthData(GregorianYearMonth(2025, 2), todayEpochDay = 0L).days.size)
        assertEquals(28, gregorianMonthData(GregorianYearMonth(2100, 2), todayEpochDay = 0L).days.size)
        assertEquals(29, gregorianMonthData(GregorianYearMonth(2000, 2), todayEpochDay = 0L).days.size)
    }

    @Test
    fun firstWeekdayControlsOffsetAndLabelOrder() {
        // 2026-11-01 is a Sunday.
        val month = GregorianYearMonth(2026, 11)
        val mondayFirst = gregorianMonthData(month, firstDayOfWeek = 1, todayEpochDay = 0L)
        val sundayFirst = gregorianMonthData(month, firstDayOfWeek = 7, todayEpochDay = 0L)
        assertEquals(6, mondayFirst.firstDayOffset)
        assertEquals(0, sundayFirst.firstDayOffset)
        assertEquals(7, mondayFirst.weekdayLabels.size)
        assertEquals(7, sundayFirst.weekdayLabels.size)
        assertTrue(mondayFirst.weekdayLabels.all { it.isNotBlank() })
    }

    @Test
    fun epochKeysRemainConsecutiveAcrossYearBoundary() {
        val december = gregorianMonthData(GregorianYearMonth(2026, 12), todayEpochDay = 0L)
        val january = gregorianMonthData(GregorianYearMonth(2027, 1), todayEpochDay = 0L)
        assertEquals(20818L, december.days.last().epochDay) // 2026-12-31
        assertEquals(december.days.last().epochDay + 1, january.days.first().epochDay)
        assertTrue(december.days.zipWithNext().all { (a, b) -> b.epochDay == a.epochDay + 1 })
        assertTrue(december.days.all { it.label.isNotBlank() && it.accessibilityLabel.isNotBlank() })
    }

    @Test
    fun enabledPredicateIsEvaluatedForEachDate() {
        val firstEpochDay = GregorianYearMonth(2026, 11).atDay(1)
        val disabled = firstEpochDay + 1
        val data = gregorianMonthData(GregorianYearMonth(2026, 11), todayEpochDay = 0L) { it != disabled }
        assertTrue(data.days[0].enabled)
        assertFalse(data.days[1].enabled)
        assertEquals(30, data.days.size)
    }

    @Test
    fun todayMarkerUsesInjectedEpochDay() {
        val month = GregorianYearMonth(2026, 11)
        val today = month.atDay(15)
        val data = gregorianMonthData(month, todayEpochDay = today)
        assertEquals(1, data.days.count { it.isToday })
        assertEquals(today, data.days.single { it.isToday }.epochDay)
        assertEquals(month, GregorianYearMonth.fromEpochDay(today))
    }

    @Test
    fun titleAndSecondaryHelpersFormatFirstAndSubsequentDays() {
        val month = GregorianYearMonth(2026, 4)
        val firstDay = month.atDay(1)
        val secondDay = month.atDay(2)
        assertEquals(gregorianMonthData(month, todayEpochDay = 0L).title, gregorianMonthTitle(month))
        val firstSecondary = gregorianSecondaryLabel(firstDay)
        val secondSecondary = gregorianSecondaryLabel(secondDay)
        assertTrue(firstSecondary.isNotBlank())
        assertTrue(secondSecondary.isNotBlank())
        assertTrue(firstSecondary != secondSecondary)
        assertTrue(gregorianAccessibilityLabel(firstDay).isNotBlank())
    }
}
