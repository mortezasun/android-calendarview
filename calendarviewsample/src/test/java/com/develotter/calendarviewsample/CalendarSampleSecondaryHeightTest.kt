package com.develotter.calendarviewsample

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarSampleSecondaryHeightTest {
    @Test
    fun agendaShrinksWithPaneAndKeepsPreferredCap() {
        assertEquals(120.dp, sampleBoundedViewportHeight(280.dp, 160.dp))
        assertEquals(280.dp, sampleBoundedViewportHeight(280.dp, 1000.dp))
        assertEquals(280.dp, sampleBoundedViewportHeight(280.dp, Dp.Infinity))
    }

    @Test
    fun dialogDoesNotForceFormerMinimum() {
        assertEquals(120.dp, sampleDialogViewportHeight(120.dp))
        assertEquals(239.dp, sampleDialogViewportHeight(239.dp))
    }

    @Test
    fun dialogRespectsZeroHeight() {
        assertEquals(0.dp, sampleDialogViewportHeight(0.dp))
    }

    @Test
    fun dialogCapsLargeFiniteSlot() {
        assertEquals(360.dp, sampleDialogViewportHeight(1000.dp))
    }

    @Test
    fun unboundedDialogUsesFiniteFallback() {
        assertEquals(340.dp, sampleDialogViewportHeight(Dp.Infinity))
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeDialogHeightIsRejected() {
        sampleDialogViewportHeight((-1).dp)
    }

    @Test(expected = IllegalArgumentException::class)
    fun unspecifiedDialogHeightIsRejected() {
        sampleDialogViewportHeight(Dp.Unspecified)
    }
}
