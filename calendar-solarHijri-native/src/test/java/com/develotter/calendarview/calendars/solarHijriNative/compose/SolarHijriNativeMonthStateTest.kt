package com.develotter.calendarview.calendars.solarHijriNative.compose

import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriEngine
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SolarHijriNativeMonthStateTest {
    @Test
    fun navigationCrossesTheSolarYearBoundary() {
        val state = SolarHijriNativeMonthState(SolarHijriDate(1405, 12, 1))
        state.next()
        assertEquals(1406, state.year)
        assertEquals(1, state.monthNumber)
        assertEquals(1, state.month.day)

        state.previous()
        assertEquals(1405, state.year)
        assertEquals(12, state.monthNumber)
    }

    @Test
    fun goToNormalizesTheTargetToItsFirstDay() {
        val state = SolarHijriNativeMonthState(SolarHijriDate(1405, 1, 1))
        state.goTo(SolarHijriDate(1405, 5, 20))
        assertEquals(1405, state.year)
        assertEquals(5, state.monthNumber)
        assertEquals(1, state.month.day)
    }

    @Test
    fun boundsStopNavigationAndRejectInvalidTargets() {
        val state = SolarHijriNativeMonthState(
            initialMonth = SolarHijriDate(1405, 1, 1),
            minMonth = SolarHijriDate(1405, 1, 10),
            maxMonth = SolarHijriDate(1405, 2, 15),
        )
        assertEquals(SolarHijriDate(1405, 1, 1), state.minMonth)
        assertEquals(SolarHijriDate(1405, 2, 1), state.maxMonth)
        assertFalse(state.canGoPrevious)
        assertTrue(state.canGoNext)
        state.next()
        assertFalse(state.canGoNext)

        assertThrows(IllegalArgumentException::class.java) {
            state.goTo(SolarHijriDate(1405, 3, 1))
        }
        assertThrows(IllegalArgumentException::class.java) {
            SolarHijriNativeMonthState(
                initialMonth = SolarHijriDate(1405, 1, 1),
                minMonth = SolarHijriDate(1405, 2, 1),
                maxMonth = SolarHijriDate(1405, 1, 1),
            )
        }
    }

    @Test
    fun engineBoundsClampUnboundedNavigation() {
        val atMin = SolarHijriNativeMonthState(SolarHijriDate(SolarHijriEngine.MIN_YEAR, 1, 1))
        assertFalse(atMin.canGoPrevious)
        atMin.previous()
        assertEquals(SolarHijriEngine.MIN_YEAR, atMin.year)

        val atMax = SolarHijriNativeMonthState(SolarHijriDate(SolarHijriEngine.MAX_YEAR, 12, 1))
        assertFalse(atMax.canGoNext)
        atMax.next()
        assertEquals(SolarHijriEngine.MAX_YEAR, atMax.year)
    }

    @Test
    fun goToTodayUsesInjectedClock() {
        val clock = Clock.fixed(Instant.parse("2025-03-21T12:00:00Z"), ZoneOffset.UTC)
        val state = SolarHijriNativeMonthState(SolarHijriDate(1400, 1, 1), clock = clock)
        assertTrue(state.canGoToday)
        state.goToToday()
        assertEquals(SolarHijriDate(1404, 1, 1), state.month)
    }

    @Test
    fun titleUsesTheSharedSolarHijriCaptionFormat() {
        val state = SolarHijriNativeMonthState(SolarHijriDate(1405, 1, 1))
        assertEquals("Farvardin 1405", state.title(Locale.US))
        assertTrue(state.title(Locale.forLanguageTag("fa")).isNotBlank())
    }
}
