package com.develotter.calendarview.selection

import com.develotter.calendarview.enums.TypeSelectDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CalendarSelectionTest {
    @Test
    fun noneIgnoresClicks() {
        val selection = CalendarSelection.empty(TypeSelectDay.None).onDayClick(10L)
        assertTrue(selection.selectedDays.isEmpty())
        assertFalse(10L in selection)
    }

    @Test
    fun singleReplacesAndTogglesWithoutMutatingPreviousValue() {
        val first = CalendarSelection.empty(TypeSelectDay.Single).onDayClick(10L)
        val second = first.onDayClick(20L)
        assertEquals(listOf(10L), first.selectedDays)
        assertEquals(listOf(20L), second.selectedDays)
        assertTrue(second.onDayClick(20L).selectedDays.isEmpty())
    }

    @Test
    fun multipleSortsAndToggles() {
        val selection = CalendarSelection.empty(TypeSelectDay.Multiple)
            .onDayClick(20L).onDayClick(10L).onDayClick(30L).onDayClick(20L)
        assertEquals(listOf(10L, 30L), selection.selectedDays)
        assertFalse(20L in selection)
    }

    @Test
    fun rangeOrdersReverseClicksAndIncludesUnrenderedInterior() {
        val selection = range(20L, 10L)
        assertEquals(listOf(10L, 20L), selection.selectedDays)
        assertTrue(selection.isRangeComplete)
        assertTrue(15L in selection)
        assertTrue(selection.isRangeEndpoint(10L))
        assertFalse(selection.isRangeEndpoint(15L))
        assertFalse(9L in selection)
        assertFalse(21L in selection)
    }

    @Test
    fun rangeLoneEndpointTogglesOff() {
        val selection = CalendarSelection.empty(TypeSelectDay.Range).onDayClick(10L)
        assertFalse(selection.isRangeComplete)
        assertTrue(10L in selection)
        assertFalse(11L in selection)
        assertTrue(selection.onDayClick(10L).selectedDays.isEmpty())
    }

    @Test
    fun removingEitherRangeEndpointLeavesTheOther() {
        val selection = range(10L, 20L)
        assertEquals(listOf(20L), selection.onDayClick(10L).selectedDays)
        assertEquals(listOf(10L), selection.onDayClick(20L).selectedDays)
        assertFalse(15L in selection.onDayClick(20L))
    }

    @Test
    fun rangeOutsideClicksExtendAndInteriorClickReplacesEnd() {
        val selection = range(10L, 20L)
        assertEquals(listOf(5L, 20L), selection.onDayClick(5L).selectedDays)
        assertEquals(listOf(10L, 25L), selection.onDayClick(25L).selectedDays)
        val shortened = selection.onDayClick(15L)
        assertEquals(listOf(10L, 15L), shortened.selectedDays)
        assertFalse(18L in shortened)
    }

    @Test
    fun restorationCopiesDeduplicatesAndSortsInput() {
        val input = mutableListOf(20L, 10L, 20L)
        val selection = CalendarSelection.from(TypeSelectDay.Range, input)
        input.clear()
        assertEquals(listOf(10L, 20L), selection.selectedDays)
        assertTrue(selection.isRangeComplete)
        val copy = selection.selectedDays.toMutableList()
        copy.clear()
        assertEquals(listOf(10L, 20L), selection.selectedDays)
    }

    @Test
    fun invalidRestorationIsRejected() {
        for ((mode, days) in listOf(
            TypeSelectDay.None to listOf(1L),
            TypeSelectDay.Single to listOf(1L, 2L),
            TypeSelectDay.Range to listOf(1L, 2L, 3L),
        )) {
            var rejected = false
            try {
                CalendarSelection.from(mode, days)
            } catch (_: IllegalArgumentException) {
                rejected = true
            }
            assertTrue("Invalid input must be rejected for $mode", rejected)
        }
    }

    @Test
    fun clearPreservesModeAndDoesNotMutateOriginal() {
        val selection = range(10L, 20L)
        val cleared = selection.clear()
        assertEquals(TypeSelectDay.Range, cleared.mode)
        assertTrue(cleared.selectedDays.isEmpty())
        assertTrue(selection.isRangeComplete)
    }

    @Test
    fun absoluteDaysHandleYearBoundariesAndExtremeKeysWithoutEnumeration() {
        val start = LocalDate.of(2026, 12, 31).toEpochDay()
        val end = LocalDate.of(2027, 1, 2).toEpochDay()
        assertTrue(LocalDate.of(2027, 1, 1).toEpochDay() in range(start, end))
        val extreme = range(Long.MIN_VALUE, Long.MAX_VALUE)
        assertTrue(0L in extreme)
        assertEquals(2, extreme.selectedDays.size)
    }

    private fun range(first: Long, second: Long): CalendarSelection =
        CalendarSelection.empty(TypeSelectDay.Range).onDayClick(first).onDayClick(second)
}
