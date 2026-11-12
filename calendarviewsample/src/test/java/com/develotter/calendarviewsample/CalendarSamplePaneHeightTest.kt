package com.develotter.calendarviewsample

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarSamplePaneHeightTest {
    @Test
    fun unhostedViewportKeepsPreferredHeight() {
        assertEquals(480.dp, sampleBoundedViewportHeight(480.dp, Dp.Infinity))
    }

    @Test
    fun largePaneDoesNotGrowBeyondPreferredHeight() {
        assertEquals(480.dp, sampleBoundedViewportHeight(480.dp, 1000.dp))
    }

    @Test
    fun compactPaneReservesProportionalSpace() {
        assertEquals(300.dp, sampleBoundedViewportHeight(480.dp, 400.dp))
    }

    @Test
    fun tabletopPaneCanShrinkBelowFormerMinimums() {
        assertEquals(120.dp, sampleBoundedViewportHeight(480.dp, 160.dp))
    }

    @Test
    fun zeroPaneDoesNotForcePositiveViewport() {
        assertEquals(0.dp, sampleBoundedViewportHeight(480.dp, 0.dp))
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativePaneIsRejected() {
        sampleBoundedViewportHeight(480.dp, (-1).dp)
    }

    @Test(expected = IllegalArgumentException::class)
    fun unspecifiedPaneIsRejected() {
        sampleBoundedViewportHeight(480.dp, Dp.Unspecified)
    }
}
