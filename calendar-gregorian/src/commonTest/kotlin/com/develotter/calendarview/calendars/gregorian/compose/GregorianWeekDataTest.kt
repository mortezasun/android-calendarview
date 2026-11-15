package com.develotter.calendarview.calendars.gregorian.compose

import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GregorianWeekDataTest {
    @Test
    fun weekContainsSevenConsecutiveDaysStartingOnTheConfiguredWeekday() {
        val date = GregorianYearMonth(2026, 11).atDay(4)
        val data = gregorianWeekData(date, firstDayOfWeek = 1, todayEpochDay = 0L)
        assertEquals(7, data.days.size)
        assertEquals(7, data.weekdayLabels.size)
        assertTrue(data.days.zipWithNext().all { (a, b) -> b.epochDay == a.epochDay + 1 })
        assertEquals(1, GregorianYearMonth.isoWeekdayFromEpochDay(data.days.first().epochDay))
        assertTrue(data.days.all { it.label.isNotBlank() && it.accessibilityLabel.isNotBlank() })
    }

    @Test
    fun weekStartChangesTheFirstDayOnly() {
        val date = GregorianYearMonth(2026, 11).atDay(4)
        val mondayFirst = gregorianWeekData(date, firstDayOfWeek = 1, todayEpochDay = 0L)
        val sundayFirst = gregorianWeekData(date, firstDayOfWeek = 7, todayEpochDay = 0L)
        assertEquals(sundayFirst.days.first().epochDay + 1, mondayFirst.days.first().epochDay)
        assertEquals(7, sundayFirst.weekdayLabels.size)
        assertTrue(mondayFirst.title.isNotBlank())
    }

    @Test
    fun weekNumberIsProvided() {
        val data = gregorianWeekData(GregorianYearMonth(2026, 1).atDay(1), todayEpochDay = 0L)
        assertTrue(data.weekNumber?.isNotBlank() == true)
    }

    @Test
    fun todayMarkerUsesInjectedEpochDay() {
        val date = GregorianYearMonth(2026, 11).atDay(4)
        val data = gregorianWeekData(date, todayEpochDay = date)
        assertEquals(1, data.days.count { it.isToday })
        assertEquals(date, data.days.single { it.isToday }.epochDay)
    }

    @Test
    fun enabledPredicateReceivesEpochDays() {
        val date = GregorianYearMonth(2026, 11).atDay(4)
        val data = gregorianWeekData(date, firstDayOfWeek = 1, todayEpochDay = 0L) { it % 2L == 0L }
        assertTrue(data.days.all { it.enabled == (it.epochDay % 2L == 0L) })
    }
}
