package com.develotter.calendarview.compose

import com.develotter.calendarview.enums.TypeSelectDay
import com.develotter.calendarview.selection.CalendarRangePolicy
import com.develotter.calendarview.selection.CalendarSelection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarSelectionStateTest {
    @Test
    fun rejectsCompleteRangesOutsideThePolicy() {
        val state = CalendarSelectionState(
            CalendarSelection.empty(TypeSelectDay.Range),
            CalendarRangePolicy(minRangeLength = 2, maxRangeLength = 3),
        )
        state.onDayClick(1L)
        assertEquals(listOf(1L), state.selection.selectedDays)
        state.onDayClick(2L)
        assertEquals(listOf(1L, 2L), state.selection.selectedDays)
        state.onDayClick(5L)
        assertEquals(listOf(1L, 2L), state.selection.selectedDays)

        state.selectRange(10L, 12L)
        assertEquals(listOf(10L, 12L), state.selection.selectedDays)
        state.selectRange(10L, 13L)
        assertEquals(listOf(10L, 12L), state.selection.selectedDays)
    }

    @Test
    fun incompleteEndpointsAndNonRangeModesAreUnaffected() {
        val range = CalendarSelectionState(
            CalendarSelection.empty(TypeSelectDay.Range),
            CalendarRangePolicy(minRangeLength = 3),
        )
        range.onDayClick(1L)
        assertEquals(listOf(1L), range.selection.selectedDays)

        val single = CalendarSelectionState(
            CalendarSelection.empty(TypeSelectDay.Single),
            CalendarRangePolicy(minRangeLength = 3),
        )
        single.selectRange(1L, 2L)
        assertTrue(single.selection.selectedDays.isEmpty())
        single.onDayClick(7L)
        assertEquals(listOf(7L), single.selection.selectedDays)
    }
}
