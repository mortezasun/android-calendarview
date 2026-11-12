package com.develotter.calendarviewsample

import androidx.compose.ui.unit.dp
import com.develotter.calendarview.compose.calendarMonthColumns
import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarSampleMonthWidthTest {
    @Test
    fun targetMonthWidthReflectsWeekNumberColumn() {
        assertEquals(336.dp, sampleTargetMonthWidth(showWeekNumbers = false))
        assertEquals(376.dp, sampleTargetMonthWidth(showWeekNumbers = true))
    }

    @Test
    fun columnTransitionsWithoutWeekNumbersAt48DpDays() {
        val minWidth = sampleTargetMonthWidth(showWeekNumbers = false) // 336.dp
        val spacing = 8.dp
        assertEquals(1, calendarMonthColumns(336.dp, minWidth, spacing))
        assertEquals(1, calendarMonthColumns(679.dp, minWidth, spacing))
        assertEquals(2, calendarMonthColumns(680.dp, minWidth, spacing))
        assertEquals(2, calendarMonthColumns(1023.dp, minWidth, spacing))
        assertEquals(3, calendarMonthColumns(1024.dp, minWidth, spacing))
    }

    @Test
    fun columnTransitionsWithWeekNumbersAt48DpDays() {
        val minWidth = sampleTargetMonthWidth(showWeekNumbers = true) // 376.dp
        val spacing = 8.dp
        assertEquals(1, calendarMonthColumns(376.dp, minWidth, spacing))
        assertEquals(1, calendarMonthColumns(759.dp, minWidth, spacing))
        assertEquals(2, calendarMonthColumns(760.dp, minWidth, spacing))
        assertEquals(2, calendarMonthColumns(1143.dp, minWidth, spacing))
        assertEquals(3, calendarMonthColumns(1144.dp, minWidth, spacing))
    }

    @Test
    fun narrowContainersFallBackToOneColumn() {
        val withoutWeeks = sampleTargetMonthWidth(showWeekNumbers = false)
        val withWeeks = sampleTargetMonthWidth(showWeekNumbers = true)
        val spacing = 8.dp

        assertEquals(1, calendarMonthColumns(320.dp, withoutWeeks, spacing))
        assertEquals(1, calendarMonthColumns(320.dp, withWeeks, spacing))
        assertEquals(1, calendarMonthColumns(240.dp, withoutWeeks, spacing))
        assertEquals(1, calendarMonthColumns(100.dp, withWeeks, spacing))
    }
}
