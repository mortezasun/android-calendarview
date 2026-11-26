package com.develotter.calendarview.compose

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CalendarDayViewLayoutTest {
    private fun event(id: String, start: Int, end: Int) = CalendarDayEvent(id, start, end, id)

    private fun List<PositionedDayEvent>.of(id: String) = first { it.event.id == id }

    @Test
    fun separateEventsUseTheFullWidth() {
        val result = layoutDayEvents(listOf(event("a", 60, 120), event("b", 180, 240)))
        assertTrue(result.all { it.column == 0 && it.columnCount == 1 })
    }

    @Test
    fun touchingEventsDoNotOverlap() {
        val result = layoutDayEvents(listOf(event("a", 60, 120), event("b", 120, 180)))
        assertTrue(result.all { it.column == 0 && it.columnCount == 1 })
    }

    @Test
    fun overlappingEventsShareColumns() {
        val result = layoutDayEvents(listOf(event("a", 60, 180), event("b", 90, 150)))
        assertEquals(0, result.of("a").column)
        assertEquals(1, result.of("b").column)
        assertTrue(result.all { it.columnCount == 2 })
    }

    @Test
    fun freedColumnIsReusedAndGroupSharesTheMaximumCount() {
        val result = layoutDayEvents(
            listOf(event("a", 0, 120), event("b", 30, 60), event("c", 70, 100)),
        )
        assertEquals(1, result.of("b").column)
        assertEquals(1, result.of("c").column)
        assertTrue(result.all { it.columnCount == 2 })
    }

    @Test
    fun overlapIsTransitiveButGroupsAreIndependent() {
        val result = layoutDayEvents(
            listOf(event("a", 0, 60), event("b", 30, 120), event("c", 100, 150), event("d", 200, 260)),
        )
        assertEquals(2, result.of("a").columnCount)
        assertEquals(2, result.of("c").columnCount)
        assertEquals(1, result.of("d").columnCount)
    }

    @Test
    fun inputOrderIsIrrelevantAndLongerEventComesFirstOnTies() {
        val result = layoutDayEvents(listOf(event("short", 60, 90), event("long", 60, 180)))
        assertEquals(listOf("long", "short"), result.map { it.event.id })
        assertEquals(0, result.of("long").column)
    }

    @Test
    fun emptyInputGivesEmptyLayout() {
        assertTrue(layoutDayEvents(emptyList()).isEmpty())
    }

    @Test
    fun eventRejectsInvalidTimes() {
        assertFailsWith<IllegalArgumentException> { event("x", -1, 10) }
        assertFailsWith<IllegalArgumentException> { event("x", 10, 10) }
        assertFailsWith<IllegalArgumentException> { event("x", 10, 1441) }
        assertFailsWith<IllegalArgumentException> { event("x", 1440, 1441) }
    }
}
