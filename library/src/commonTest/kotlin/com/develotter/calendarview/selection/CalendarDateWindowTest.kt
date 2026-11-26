package com.develotter.calendarview.selection

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CalendarDateWindowTest {
    @Test
    fun boundsAreInclusive() {
        val window = CalendarDateWindow(minEpochDay = 10L, maxEpochDay = 20L)
        assertTrue(10L in window)
        assertTrue(20L in window)
        assertFalse(9L in window)
        assertFalse(21L in window)
    }

    @Test
    fun openEndsAcceptEverythingOnThatSide() {
        assertTrue(Long.MIN_VALUE in CalendarDateWindow(maxEpochDay = 5L))
        assertFalse(6L in CalendarDateWindow(maxEpochDay = 5L))
        assertTrue(Long.MAX_VALUE in CalendarDateWindow(minEpochDay = 5L))
        assertFalse(4L in CalendarDateWindow(minEpochDay = 5L))
        assertTrue(0L in CalendarDateWindow())
    }

    @Test
    fun rangeNeedsBothEndpointsInsideInEitherOrder() {
        val window = CalendarDateWindow(10L, 20L)
        assertTrue(window.allows(12L, 18L))
        assertTrue(window.allows(18L, 12L))
        assertFalse(window.allows(9L, 12L))
        assertFalse(window.allows(12L, 21L))
    }

    @Test
    fun adapterMatchesContains() {
        val window = CalendarDateWindow(10L, 20L)
        val enabled = window.isDateEnabled
        assertTrue(enabled(15L))
        assertFalse(enabled(25L))
    }

    @Test
    fun rejectsInvertedBounds() {
        assertFailsWith<IllegalArgumentException> { CalendarDateWindow(5L, 4L) }
    }
}
