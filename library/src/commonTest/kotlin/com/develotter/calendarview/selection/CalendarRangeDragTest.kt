package com.develotter.calendarview.selection

import com.develotter.calendarview.compose.CalendarSelectionState
import com.develotter.calendarview.enums.TypeSelectDay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CalendarRangeDragTest {
    @Test
    fun endpointsAreSortedWhateverTheDragDirection() {
        val drag = CalendarRangeDrag(20L).moveTo(15L)
        assertEquals(15L, drag.startEpochDay)
        assertEquals(20L, drag.endEpochDay)
        assertEquals(6L, drag.length)
        assertTrue(17L in drag)
        assertFalse(21L in drag)
    }

    @Test
    fun disabledDayLeavesTheDragUnchanged() {
        val drag = CalendarRangeDrag(10L).moveTo(12L)
        assertEquals(drag, drag.moveTo(13L) { it != 13L })
    }

    @Test
    fun allowedNeedsPolicyAndWindow() {
        val drag = CalendarRangeDrag(10L).moveTo(12L)
        assertTrue(drag.isAllowed())
        assertFalse(drag.isAllowed(CalendarRangePolicy(minRangeLength = 4)))
        assertFalse(drag.isAllowed(window = CalendarDateWindow(minEpochDay = 11L)))
        assertTrue(drag.isAllowed(CalendarRangePolicy(maxRangeLength = 3), CalendarDateWindow(10L, 12L)))
    }

    @Test
    fun singleDayDragFollowsTheMinimumLength() {
        val drag = CalendarRangeDrag(10L)
        assertTrue(drag.isAllowed())
        assertFalse(drag.isAllowed(CalendarRangePolicy(minRangeLength = 2)))
    }

    @Test
    fun previewSelectionHoldsTheSortedEndpoints() {
        val selection = CalendarRangeDrag(20L).moveTo(15L).toSelection()
        assertEquals(listOf(15L, 20L), selection.selectedDays)
        assertTrue(selection.isRangeComplete)
        assertEquals(1, CalendarRangeDrag(15L).toSelection().selectedDays.size)
    }

    @Test
    fun stateCommitsOnlyAllowedDragsInRangeMode() {
        val state = CalendarSelectionState(
            CalendarSelection.empty(TypeSelectDay.Range),
            CalendarRangePolicy(minRangeLength = 2, maxRangeLength = 5),
        )
        assertFalse(state.selectRange(CalendarRangeDrag(10L)))
        assertTrue(state.selection.selectedDays.isEmpty())
        assertFalse(state.selectRange(CalendarRangeDrag(10L).moveTo(20L)))
        assertTrue(state.selectRange(CalendarRangeDrag(12L).moveTo(10L)))
        assertEquals(listOf(10L, 12L), state.selection.selectedDays)
        assertFalse(state.selectRange(CalendarRangeDrag(30L).moveTo(31L), CalendarDateWindow(maxEpochDay = 20L)))
        assertEquals(listOf(10L, 12L), state.selection.selectedDays)
    }

    @Test
    fun otherModesIgnoreTheDrag() {
        val state = CalendarSelectionState(CalendarSelection.empty(TypeSelectDay.Multiple))
        assertFalse(state.selectRange(CalendarRangeDrag(1L).moveTo(3L)))
    }
}
