package com.develotter.calendarview.compose

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarMonthColumnsTest {
    @Test
    fun derivesWholeColumnsAndKeepsAtLeastOne() {
        assertEquals(1, calendarMonthColumns(320.dp, 320.dp))
        assertEquals(1, calendarMonthColumns(360.dp, 320.dp))
        assertEquals(2, calendarMonthColumns(720.dp, 320.dp))
        assertEquals(3, calendarMonthColumns(1200.dp, 320.dp))
    }

    @Test
    fun accountsForSpacingWhenHonoringTheMinimumWidth() {
        assertEquals(1, calendarMonthColumns(640.dp, 320.dp, 8.dp))
        assertEquals(2, calendarMonthColumns(648.dp, 320.dp, 8.dp))
        assertEquals(2, calendarMonthColumns(720.dp, 320.dp, 8.dp))
    }

    @Test
    fun fortyEightDpDayWidthThresholdsMatchTheLayoutContract() {
        // 7 x 48 dp = 336 dp; with the 40 dp week-number column 376 dp (docs/ADAPTIVE_LAYOUT.md).
        assertEquals(1, calendarMonthColumns(335.dp, 336.dp, 8.dp))
        assertEquals(1, calendarMonthColumns(336.dp, 336.dp, 8.dp))
        assertEquals(1, calendarMonthColumns(679.dp, 336.dp, 8.dp))
        assertEquals(2, calendarMonthColumns(680.dp, 336.dp, 8.dp))
        assertEquals(1, calendarMonthColumns(759.dp, 376.dp, 8.dp))
        assertEquals(2, calendarMonthColumns(760.dp, 376.dp, 8.dp))
    }

    @Test
    fun containersNarrowerThanOneMonthFallBackToOneColumn() {
        assertEquals(1, calendarMonthColumns(200.dp, 336.dp, 8.dp))
        assertEquals(1, calendarMonthColumns(1.dp, 320.dp))
    }

    @Test
    fun columnCountIsMonotonicInWidthAndConvergesBackToOne() {
        var previous = 0
        for (width in 100..2000 step 7) {
            val columns = calendarMonthColumns(width.dp, 336.dp, 8.dp)
            assertTrue(columns >= previous)
            previous = columns
        }
        assertEquals(1, calendarMonthColumns(360.dp, 336.dp, 8.dp))
    }

    @Test
    fun defaultMinimumWidthDoesNotGuaranteeFortyEightDpDays() {
        // Documents the known limit: 320 dp / 7 is below 48 dp per day.
        assertTrue(320f / 7f < 48f)
        assertTrue((320f - 40f) / 7f < 48f)
    }

    @Test
    fun invalidDimensionsAreRejected() {
        var rejected = false
        try {
            calendarMonthColumns(0.dp, 320.dp)
        } catch (_: IllegalArgumentException) {
            rejected = true
        }
        assertTrue(rejected)

        rejected = false
        try {
            calendarMonthColumns(320.dp, 0.dp)
        } catch (_: IllegalArgumentException) {
            rejected = true
        }
        assertTrue(rejected)

        rejected = false
        try {
            calendarMonthColumns(320.dp, 320.dp, -1.dp)
        } catch (_: IllegalArgumentException) {
            rejected = true
        }
        assertTrue(rejected)
    }
}
