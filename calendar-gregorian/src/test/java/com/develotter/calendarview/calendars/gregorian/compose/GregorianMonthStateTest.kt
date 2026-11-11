package com.develotter.calendarview.calendars.gregorian.compose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth
import java.util.Locale

class GregorianMonthStateTest {
    @Test
    fun navigationStopsAtBoundsWithoutThrowing() {
        val state = GregorianMonthState(
            initialMonth = YearMonth.of(2026, 1),
            minMonth = YearMonth.of(2025, 12),
            maxMonth = YearMonth.of(2026, 2),
        )

        assertTrue(state.canGoPrevious)
        state.previous()
        assertEquals(YearMonth.of(2025, 12), state.month)
        assertFalse(state.canGoPrevious)
        state.previous()
        assertEquals(YearMonth.of(2025, 12), state.month)

        state.next()
        state.next()
        assertEquals(YearMonth.of(2026, 2), state.month)
        assertFalse(state.canGoNext)
        state.next()
        assertEquals(YearMonth.of(2026, 2), state.month)
    }

    @Test
    fun goToRejectsTargetsOutsideTheBounds() {
        val state = GregorianMonthState(
            initialMonth = YearMonth.of(2026, 1),
            minMonth = YearMonth.of(2026, 1),
            maxMonth = YearMonth.of(2026, 3),
        )
        var rejected = false
        try {
            state.goTo(YearMonth.of(2026, 4))
        } catch (_: IllegalArgumentException) {
            rejected = true
        }
        assertTrue(rejected)
        assertEquals(YearMonth.of(2026, 1), state.month)
    }

    @Test
    fun initialMonthMustLieInsideTheBounds() {
        var rejected = false
        try {
            GregorianMonthState(
                initialMonth = YearMonth.of(2025, 1),
                minMonth = YearMonth.of(2026, 1),
            )
        } catch (_: IllegalArgumentException) {
            rejected = true
        }
        assertTrue(rejected)
    }

    @Test
    fun titleUsesTheSharedGregorianCaptionFormat() {
        val state = GregorianMonthState(YearMonth.of(2026, 11))
        assertEquals("November 2026", state.title(Locale.US))
    }
}
