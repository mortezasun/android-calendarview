package com.develotter.calendarview.compose

import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.Test

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
            assertFailsWith<IllegalArgumentException>("case $index must be rejected") {
                create()
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
