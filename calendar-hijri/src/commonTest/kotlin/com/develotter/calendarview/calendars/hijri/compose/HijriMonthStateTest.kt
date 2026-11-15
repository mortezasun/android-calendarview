package com.develotter.calendarview.calendars.hijri.compose

import com.develotter.calendarview.calendars.hijri.HijriDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HijriMonthStateTest {
    @Test
    fun navigationMovesOneMonthAtATimeAndNormalizesTheFirstDay() {
        val state = HijriMonthState(HijriDate(1447, 9, 15))
        assertEquals(HijriDate(1447, 9, 1), state.month)
        state.next()
        assertEquals(HijriDate(1447, 10, 1), state.month)
        state.next()
        state.next()
        state.next()
        assertEquals(HijriDate(1448, 1, 1), state.month)
        state.previous()
        assertEquals(HijriDate(1447, 12, 1), state.month)
    }

    @Test
    fun boundsStopNavigationAndRejectOutOfRangeTargets() {
        val min = HijriDate(1447, 1, 1)
        val max = HijriDate(1447, 3, 1)
        val state = HijriMonthState(HijriDate(1447, 2, 10), min, max)
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
        state.goTo(HijriDate(1447, 2, 20))
        assertEquals(HijriDate(1447, 2, 1), state.month)
        assertFailsWith<IllegalArgumentException> { state.goTo(HijriDate(1447, 4, 1)) }
    }

    @Test
    fun initialMonthOutsideBoundsOrInvertedBoundsAreRejected() {
        assertFailsWith<IllegalArgumentException> {
            HijriMonthState(HijriDate(1447, 4, 1), HijriDate(1447, 1, 1), HijriDate(1447, 3, 1))
        }
        assertFailsWith<IllegalArgumentException> {
            HijriMonthState(HijriDate(1447, 2, 1), HijriDate(1447, 3, 1), HijriDate(1447, 1, 1))
        }
    }

    @Test
    fun titleMatchesMonthDataCaption() {
        val month = HijriDate(1447, 9, 1)
        val state = HijriMonthState(month)
        assertEquals(hijriMonthData(month, todayEpochDay = 0L).title, state.title())
        assertTrue(state.title().isNotBlank())
    }

    @Test
    fun todayNavigationRespectsBounds() {
        assertTrue(HijriMonthState(HijriDate(1447, 9, 1)).canGoToday)
        val bounded = HijriMonthState(
            HijriDate(1440, 6, 1),
            HijriDate(1440, 1, 1),
            HijriDate(1440, 12, 1),
        )
        assertFalse(bounded.canGoToday)
        bounded.goToToday()
        assertEquals(HijriDate(1440, 6, 1), bounded.month)
    }
}
