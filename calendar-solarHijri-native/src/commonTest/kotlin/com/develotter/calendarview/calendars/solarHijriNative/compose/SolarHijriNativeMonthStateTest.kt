package com.develotter.calendarview.calendars.solarHijriNative.compose

import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SolarHijriNativeMonthStateTest {
    @Test
    fun navigationMovesOneMonthAtATimeAndNormalizesTheFirstDay() {
        val state = SolarHijriNativeMonthState(SolarHijriDate(1404, 1, 15), currentEpochDay = { 0L })
        assertEquals(SolarHijriDate(1404, 1, 1), state.month)
        state.next()
        assertEquals(SolarHijriDate(1404, 2, 1), state.month)
        state.previous()
        state.previous()
        assertEquals(SolarHijriDate(1403, 12, 1), state.month)
        assertEquals(1403, state.year)
        assertEquals(12, state.monthNumber)
    }

    @Test
    fun boundsStopNavigationAndRejectOutOfRangeTargets() {
        val min = SolarHijriDate(1404, 1, 1)
        val max = SolarHijriDate(1404, 3, 1)
        val state = SolarHijriNativeMonthState(SolarHijriDate(1404, 2, 10), min, max) { 0L }
        assertTrue(state.canGoPrevious)
        assertTrue(state.canGoNext)
        state.previous()
        assertEquals(min, state.month)
        assertFalse(state.canGoPrevious)
        state.next()
        state.next()
        assertEquals(max, state.month)
        assertFalse(state.canGoNext)
        state.goTo(SolarHijriDate(1404, 2, 20))
        assertEquals(SolarHijriDate(1404, 2, 1), state.month)
        assertFailsWith<IllegalArgumentException> { state.goTo(SolarHijriDate(1404, 4, 1)) }
    }

    @Test
    fun initialMonthOutsideBoundsOrInvertedBoundsAreRejected() {
        assertFailsWith<IllegalArgumentException> {
            SolarHijriNativeMonthState(
                SolarHijriDate(1404, 4, 1),
                SolarHijriDate(1404, 1, 1),
                SolarHijriDate(1404, 3, 1),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            SolarHijriNativeMonthState(
                SolarHijriDate(1404, 2, 1),
                SolarHijriDate(1404, 3, 1),
                SolarHijriDate(1404, 1, 1),
            )
        }
    }

    @Test
    fun titleMatchesMonthDataCaption() {
        val month = SolarHijriDate(1404, 1, 1)
        val state = SolarHijriNativeMonthState(month, currentEpochDay = { 0L })
        assertEquals(solarHijriNativeMonthData(month, todayEpochDay = 0L).title, state.title())
        assertTrue(state.title().isNotBlank())
    }

    @Test
    fun todayFollowsInjectedClock() {
        val today = SolarHijriDate(1404, 5, 10).toEpochDay()
        val state = SolarHijriNativeMonthState(SolarHijriDate(1404, 1, 1)) { today }
        assertTrue(state.canGoToday)
        state.goToToday()
        assertEquals(SolarHijriDate(1404, 5, 1), state.month)
    }

    @Test
    fun todayOutsideBoundsIsNotReachable() {
        val state = SolarHijriNativeMonthState(
            SolarHijriDate(1380, 6, 1),
            SolarHijriDate(1380, 1, 1),
            SolarHijriDate(1380, 12, 1),
        ) { 0L }
        assertFalse(state.canGoToday)
        state.goToToday()
        assertEquals(SolarHijriDate(1380, 6, 1), state.month)
    }
}
