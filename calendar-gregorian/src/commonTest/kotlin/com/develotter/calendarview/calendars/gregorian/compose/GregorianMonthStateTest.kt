package com.develotter.calendarview.calendars.gregorian.compose

import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GregorianMonthStateTest {
    @Test
    fun navigationMovesOneMonthAtATime() {
        val state = GregorianMonthState(GregorianYearMonth(2026, 11))
        state.next()
        assertEquals(GregorianYearMonth(2026, 12), state.month)
        state.next()
        assertEquals(GregorianYearMonth(2027, 1), state.month)
        state.previous()
        assertEquals(GregorianYearMonth(2026, 12), state.month)
    }

    @Test
    fun boundsStopNavigationAndRejectOutOfRangeTargets() {
        val min = GregorianYearMonth(2026, 1)
        val max = GregorianYearMonth(2026, 3)
        val state = GregorianMonthState(GregorianYearMonth(2026, 2), min, max)
        assertTrue(state.canGoPrevious)
        assertTrue(state.canGoNext)
        state.previous()
        assertEquals(min, state.month)
        assertFalse(state.canGoPrevious)
        state.previous()
        assertEquals(min, state.month)
        state.next()
        state.next()
        assertEquals(max, state.month)
        assertFalse(state.canGoNext)
        state.goTo(GregorianYearMonth(2026, 2))
        assertEquals(GregorianYearMonth(2026, 2), state.month)
        assertFailsWith<IllegalArgumentException> { state.goTo(GregorianYearMonth(2026, 4)) }
    }

    @Test
    fun initialMonthOutsideBoundsOrInvertedBoundsAreRejected() {
        assertFailsWith<IllegalArgumentException> {
            GregorianMonthState(
                GregorianYearMonth(2026, 4),
                GregorianYearMonth(2026, 1),
                GregorianYearMonth(2026, 3),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            GregorianMonthState(
                GregorianYearMonth(2026, 2),
                GregorianYearMonth(2026, 3),
                GregorianYearMonth(2026, 1),
            )
        }
    }

    @Test
    fun titleMatchesMonthDataCaption() {
        val month = GregorianYearMonth(2026, 11)
        val state = GregorianMonthState(month)
        assertEquals(gregorianMonthData(month, todayEpochDay = 0L).title, state.title())
        assertTrue(state.title().isNotBlank())
    }

    @Test
    fun todayNavigationRespectsBounds() {
        assertTrue(GregorianMonthState(GregorianYearMonth(2026, 11)).canGoToday)
        val bounded = GregorianMonthState(
            GregorianYearMonth(2000, 11),
            GregorianYearMonth(2000, 1),
            GregorianYearMonth(2000, 12),
        )
        assertFalse(bounded.canGoToday)
        bounded.goToToday()
        assertEquals(GregorianYearMonth(2000, 11), bounded.month)
    }
}
