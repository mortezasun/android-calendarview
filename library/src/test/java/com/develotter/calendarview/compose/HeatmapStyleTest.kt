package com.develotter.calendarview.compose

import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HeatmapStyleTest {
    @Test
    fun defaultsMatchTheOriginalHeatmapGeometry() {
        val style = HeatmapStyle()
        assertEquals(48.dp, style.cellSize)
        assertEquals(0.dp, style.cellSpacing)
        assertEquals(RectangleShape, style.cellShape)
        assertEquals(64.dp, style.weekdayLabelWidth)
    }

    @Test
    fun rejectsInvalidGeometry() {
        val invalid = listOf(
            { HeatmapStyle(cellSize = 0.dp) },
            { HeatmapStyle(cellSize = (-1).dp) },
            { HeatmapStyle(cellSpacing = (-1).dp) },
            { HeatmapStyle(weekdayLabelWidth = (-1).dp) },
            { HeatmapStyle(todayBorderWidth = (-1).dp) },
            { HeatmapStyle(selectedBorderWidth = (-1).dp) },
            { HeatmapStyle(disabledAlpha = 1.5f) },
            { HeatmapStyle(disabledAlpha = -0.1f) },
        )
        invalid.forEachIndexed { index, create ->
            try {
                create()
                throw AssertionError("case $index must be rejected")
            } catch (expected: IllegalArgumentException) {
                // Expected: invalid geometry is rejected at construction.
            }
        }
    }

    @Test
    fun heatmapDayDataDefaultsToEnabledAndNotToday() {
        val day = HeatmapDayData(epochDay = 1L, label = "label", intensity = 0.5f)
        assertFalse(day.isToday)
        assertTrue(day.enabled)
    }
}
