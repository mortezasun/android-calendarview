package com.develotter.calendarview.compose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CalendarMonthCellsTest {
    private fun cell(epoch: Long) = CalendarDayCell(epoch, epoch.toString(), epoch.toString())
    private fun month(start: Long, count: Int) = List(count) { cell(start + it) }

    @Test
    fun defaultPaddingIsBlankAndDoesNotCallProvider() {
        val cells = buildCalendarMonthCells(month(100L, 30), 6, adjacentDayProvider = { error("Unexpected provider call") })
        assertEquals(42, cells.size)
        assertTrue(cells.take(6).all { it == null })
        assertTrue(cells.takeLast(6).all { it == null })
        assertEquals(30, cells.filterNotNull().size)
    }

    @Test
    fun previousAndNextVisibilityAreIndependent() {
        val days = month(100L, 30)
        val previous = buildCalendarMonthCells(days, 6, true, false, ::cell)
        val next = buildCalendarMonthCells(days, 6, false, true, ::cell)
        assertEquals((94L..99L).toList(), previous.take(6).map { it!!.epochDay })
        assertTrue(previous.takeLast(6).all { it == null })
        assertTrue(next.take(6).all { it == null })
        assertEquals((130L..135L).toList(), next.takeLast(6).map { it!!.epochDay })
    }

    @Test
    fun yearBoundaryDatesAreConsecutiveAndMarkedAsAdjacent() {
        val first = LocalDate.of(2027, 1, 1).toEpochDay()
        val cells = buildCalendarMonthCells(month(first, 31), 4, true, true, ::cell).filterNotNull()
        assertEquals(LocalDate.of(2026, 12, 28).toEpochDay(), cells.first().epochDay)
        assertEquals(LocalDate.of(2027, 1, 31).toEpochDay(), cells.last().epochDay)
        assertTrue(cells.take(4).all { !it.isCurrentMonth })
        assertTrue(cells.drop(4).all { it.isCurrentMonth })
    }

    @Test
    fun providerEnabledFlagIsPreservedWithoutMutatingCurrentCells() {
        val days = month(100L, 30)
        val cells = buildCalendarMonthCells(days, 6, true, true) { cell(it).copy(enabled = false) }
        assertFalse(cells.first()!!.enabled)
        assertFalse(cells.last()!!.enabled)
        assertTrue(days.all { it.enabled && it.isCurrentMonth })
        assertTrue(cells.drop(6).take(30).all { it!!.isCurrentMonth })
    }

    @Test(expected = IllegalArgumentException::class)
    fun visiblePaddingWithoutProviderIsRejected() {
        buildCalendarMonthCells(month(100L, 30), 6, showPreviousMonthDays = true)
    }

    @Test
    fun invalidOffsetsChronologyAndProviderIdentityAreRejected() {
        val operations = listOf<() -> Unit>(
            { buildCalendarMonthCells(month(100L, 30), 7) },
            { buildCalendarMonthCells(listOf(cell(100L), cell(102L)), 0) },
            { buildCalendarMonthCells(month(100L, 30), 6, true, false) { cell(it + 1) } },
        )
        operations.forEach { operation ->
            var rejected = false
            try { operation() } catch (_: IllegalArgumentException) { rejected = true }
            assertTrue(rejected)
        }
    }
}
