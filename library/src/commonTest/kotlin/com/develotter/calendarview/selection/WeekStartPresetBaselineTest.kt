package com.develotter.calendarview.selection

import com.develotter.calendarview.enums.TypeSelectDay
import kotlin.test.Test
import kotlin.test.assertEquals

class WeekStartPresetBaselineTest {
    @Test
    fun allWeekStartsPreserveOtherPresetsBeforeTheEpoch() {
        val selection = CalendarSelection.empty(TypeSelectDay.Range)
        val today = -1L // Wednesday, 1969-12-31.
        val expectedStarts = listOf(-3L, -2L, -1L, -7L, -6L, -5L, -4L)
        for ((index, start) in expectedStarts.withIndex()) {
            val firstDay = index + 1
            val presets = selection.presets(firstDayOfWeek = firstDay, todayEpochDay = today)
            assertEquals(start..(start + 6), presets.thisWeek, "ISO start day $firstDay")
            assertEquals(today, presets.today)
            assertEquals(-31L..-1L, presets.thisMonth)
            assertEquals(-7L..-1L, presets.lastSevenDays)
        }
    }
}
