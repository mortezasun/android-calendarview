package com.develotter.calendarview.calendars.hijri.compose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.chrono.HijrahDate
import java.util.Locale

class HijriMonthStateTest {
    @Test
    fun navigationStaysOnFirstDays() {
        val state = HijriMonthState(HijrahDate.of(1448, 1, 1))

        state.next()
        assertEquals(HijrahDate.of(1448, 2, 1), state.month)

        state.previous()
        assertEquals(HijrahDate.of(1448, 1, 1), state.month)
    }

    @Test
    fun goToNormalizesTheDayAndRejectsOutOfRangeTargets() {
        val state = HijriMonthState(
            initialMonth = HijrahDate.of(1448, 1, 10),
            minMonth = HijrahDate.of(1448, 1, 1),
            maxMonth = HijrahDate.of(1448, 3, 1),
        )
        state.goTo(HijrahDate.of(1448, 2, 20))
        assertEquals(HijrahDate.of(1448, 2, 1), state.month)

        var rejected = false
        try {
            state.goTo(HijrahDate.of(1448, 4, 1))
        } catch (_: IllegalArgumentException) {
            rejected = true
        }
        assertTrue(rejected)
    }

    @Test
    fun boundsStopNavigation() {
        val state = HijriMonthState(
            initialMonth = HijrahDate.of(1448, 1, 1),
            minMonth = HijrahDate.of(1448, 1, 1),
            maxMonth = HijrahDate.of(1448, 2, 1),
        )
        assertFalse(state.canGoPrevious)
        assertTrue(state.canGoNext)
        state.next()
        assertFalse(state.canGoNext)
    }

    @Test
    fun titleUsesTheHijriChronology() {
        val state = HijriMonthState(HijrahDate.of(1448, 1, 1))
        assertTrue(state.title(Locale.US).isNotBlank())
    }
}
