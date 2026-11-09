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

    @Test
    fun fixedSixRowsProduces42CellsEvenForShorterMonths() {
        val fourWeekMonth = month(100L, 28)
        val dynamicCells28 = buildCalendarMonthCells(fourWeekMonth, 0, fixedSixRows = false)
        val fixedCells28 = buildCalendarMonthCells(fourWeekMonth, 0, fixedSixRows = true)
        assertEquals(28, dynamicCells28.size)
        assertEquals(42, fixedCells28.size)
        assertEquals(28, fixedCells28.filterNotNull().size)
        assertTrue(fixedCells28.takeLast(14).all { it == null })

        val fiveWeekMonth = month(100L, 30)
        val dynamicCells30 = buildCalendarMonthCells(fiveWeekMonth, 1, fixedSixRows = false)
        val fixedCells30 = buildCalendarMonthCells(fiveWeekMonth, 1, fixedSixRows = true)
        assertEquals(35, dynamicCells30.size)
        assertEquals(42, fixedCells30.size)
        assertEquals(30, fixedCells30.filterNotNull().size)
        assertTrue(fixedCells30.takeLast(11).all { it == null })
    }

    @Test
    fun fixedSixRowsWithNextDaysFillsAll42CellsWithConsecutiveDates() {
        val fourWeekMonth = month(100L, 28)
        val cells = buildCalendarMonthCells(
            days = fourWeekMonth,
            firstDayOffset = 0,
            showPreviousMonthDays = false,
            showNextMonthDays = true,
            adjacentDayProvider = ::cell,
            fixedSixRows = true,
        )
        assertEquals(42, cells.size)
        assertEquals(42, cells.filterNotNull().size)
        val expectedEpochs = (100L until 142L).toList()
        assertEquals(expectedEpochs, cells.map { it!!.epochDay })
        assertTrue(cells.take(28).all { it!!.isCurrentMonth })
        assertTrue(cells.drop(28).all { !it!!.isCurrentMonth })
    }

    @Test
    fun dayCellIsTodayDefaultsToFalseAndCanBeSet() {
        val regular = cell(100L)
        assertFalse(regular.isToday)
        val today = regular.copy(isToday = true)
        assertTrue(today.isToday)
    }
}
