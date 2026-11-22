package com.develotter.calendarview.selection

import com.develotter.calendarview.enums.TypeSelectDay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CalendarSelectionPresetsTest {
    @Test
    fun presetsDerivesExpectedRangesForMondayFirst() {
        val selection = CalendarSelection.empty(TypeSelectDay.Range)
        // 2026-11-21 is a Saturday
        val today = epochDayOf(2026, 11, 21)
        val presets = selection.presets(firstDayOfWeek = 1, todayEpochDay = today)

        assertEquals(today, presets.today)

        // Monday-first week containing Saturday 2026-11-21:
        // Mon 2026-11-16 to Sun 2026-11-22
        val expectedMonday = epochDayOf(2026, 11, 16)
        val expectedSunday = epochDayOf(2026, 11, 22)
        assertEquals(expectedMonday..expectedSunday, presets.thisWeek)
        assertTrue(today in presets.thisWeek)
        assertEquals(7, presets.thisWeek.count())

        // Civil month of November 2026: 2026-11-01 to 2026-11-30
        val monthStart = epochDayOf(2026, 11, 1)
        val monthEnd = epochDayOf(2026, 11, 30)
        assertEquals(monthStart..monthEnd, presets.thisMonth)
        assertTrue(today in presets.thisMonth)

        // Last 7 days: today - 6 .. today
        assertEquals((today - 6)..today, presets.lastSevenDays)
        assertEquals(7, presets.lastSevenDays.count())
    }

    @Test
    fun presetsDerivesExpectedRangesForSundayFirst() {
        val selection = CalendarSelection.empty(TypeSelectDay.Range)
        // 2026-11-21 is a Saturday
        val today = epochDayOf(2026, 11, 21)
        val presets = selection.presets(firstDayOfWeek = 7, todayEpochDay = today)

        assertEquals(today, presets.today)

        // Sunday-first week containing Saturday 2026-11-21:
        // Sun 2026-11-15 to Sat 2026-11-21
        val expectedSunday = epochDayOf(2026, 11, 15)
        val expectedSaturday = epochDayOf(2026, 11, 21)
        assertEquals(expectedSunday..expectedSaturday, presets.thisWeek)
        assertTrue(today in presets.thisWeek)
        assertEquals(7, presets.thisWeek.count())
    }

    @Test
    fun presetsValidatesFirstDayOfWeek() {
        val selection = CalendarSelection.empty(TypeSelectDay.Single)
        val today = epochDayOf(2026, 11, 21)
        assertFailsWith<IllegalArgumentException> { selection.presets(firstDayOfWeek = 0, todayEpochDay = today) }
        assertFailsWith<IllegalArgumentException> { selection.presets(firstDayOfWeek = 8, todayEpochDay = today) }
    }
}
