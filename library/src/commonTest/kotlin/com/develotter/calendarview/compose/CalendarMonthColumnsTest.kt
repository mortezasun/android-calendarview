package com.develotter.calendarview.compose

import androidx.compose.ui.unit.dp
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.Test

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
    fun maxColumnsCapsTheDerivedColumnsWithoutAddingAny() {
        assertEquals(4, calendarMonthColumns(1600.dp, 320.dp, 8.dp))
        assertEquals(2, calendarMonthColumns(1600.dp, 320.dp, 8.dp, maxColumns = 2))
        assertEquals(1, calendarMonthColumns(1600.dp, 320.dp, 8.dp, maxColumns = 1))
        // A generous cap never widens the grid beyond what fits.
        assertEquals(4, calendarMonthColumns(1600.dp, 320.dp, 8.dp, maxColumns = 10))
        assertEquals(1, calendarMonthColumns(360.dp, 336.dp, 8.dp, maxColumns = 6))
    }

    @Test
    fun defaultMaxColumnsKeepsTheUnboundedLayout() {
        for (width in 320..1600 step 40) {
            assertEquals(
                calendarMonthColumns(width.dp, 336.dp, 8.dp),
                calendarMonthColumns(width.dp, 336.dp, 8.dp, Int.MAX_VALUE),
            )
        }
        assertEquals(3, calendarMonthColumns(1200.dp, 320.dp))
    }

    @Test
    fun monthsWrapIntoFurtherRowsWhenTheColumnsAreCapped() {
        // A Gregorian year folds into three rows of four months or four rows of three.
        assertEquals(3, calendarMonthRows(monthCount = 12, columns = 4))
        assertEquals(4, calendarMonthRows(monthCount = 12, columns = 3))
        assertEquals(1, calendarMonthRows(monthCount = 12, columns = 12))
        // The fold depends on the month count, not on the 28, 29, 30 or 31 days inside a month.
        val commonYearDays = listOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val leapYearDays = listOf(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        assertEquals(3, calendarMonthRows(commonYearDays.size, 4))
        assertEquals(3, calendarMonthRows(leapYearDays.size, 4))
        // A window that ends mid-row starts a new row instead of a new column.
        assertEquals(4, calendarMonthRows(monthCount = 13, columns = 4))
        assertEquals(2, calendarMonthRows(monthCount = 5, columns = 4))
        assertEquals(5, calendarMonthRows(monthCount = 25, columns = 6))
    }

    @Test
    fun nonPositiveMaxColumnsIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            calendarMonthColumns(1200.dp, 320.dp, 8.dp, maxColumns = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            calendarMonthColumns(1200.dp, 320.dp, 8.dp, maxColumns = -1)
        }
    }

    @Test
    fun rowHelperRejectsNonPositiveInputs() {
        assertFailsWith<IllegalArgumentException> { calendarMonthRows(monthCount = 0, columns = 3) }
        assertFailsWith<IllegalArgumentException> { calendarMonthRows(monthCount = 12, columns = 0) }
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
