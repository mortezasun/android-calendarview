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
