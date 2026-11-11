package com.develotter.calendarview.calendars.solarHijri.compose

import ir.huri.jcal.JalaliCalendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class SolarHijriMonthStateTest {
    @Test
    fun navigationCrossesTheSolarYearBoundary() {
        val state = SolarHijriMonthState(JalaliCalendar(1405, 12, 1))

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
        val state = SolarHijriMonthState(JalaliCalendar(1405, 1, 1))
        state.goTo(JalaliCalendar(1405, 5, 20))
        assertEquals(1405, state.year)
        assertEquals(5, state.monthNumber)
        assertEquals(1, state.month.day)
    }

    @Test
    fun boundsStopNavigation() {
        val state = SolarHijriMonthState(
            initialMonth = JalaliCalendar(1405, 1, 1),
            minMonth = JalaliCalendar(1405, 1, 1),
            maxMonth = JalaliCalendar(1405, 2, 1),
        )
        assertFalse(state.canGoPrevious)
        assertTrue(state.canGoNext)
        state.next()
        assertFalse(state.canGoNext)

        var rejected = false
        try {
            state.goTo(JalaliCalendar(1405, 3, 1))
        } catch (_: IllegalArgumentException) {
            rejected = true
        }
        assertTrue(rejected)
    }

    @Test
    fun titleUsesTheSharedSolarHijriCaptionFormat() {
        val state = SolarHijriMonthState(JalaliCalendar(1405, 1, 1))
        assertTrue(state.title(Locale.US).isNotBlank())
        assertTrue(state.title(Locale.forLanguageTag("fa")).isNotBlank())
    }
}
