package com.develotter.calendarview.compose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarMonthDataTest {
    @Test
    fun heatmapAdapterPadsToWholeWeeksAndCarriesDayState() {
        val month = monthData(firstDayOffset = 2, dayCount = 4)
        val heatmap = month.toHeatmap { epochDay -> (epochDay - 100L) / 10f }

        assertEquals(month.title, heatmap.title)
        assertEquals(month.weekdayLabels, heatmap.weekdayLabels)
        assertEquals(1, heatmap.weeks.size)
        assertNull(heatmap.weeks[0][0])
        assertNull(heatmap.weeks[0][1])
        val firstDay = heatmap.weeks[0][2]!!
        assertEquals(100L, firstDay.epochDay)
        assertEquals(0f, firstDay.intensity, 0f)
        assertTrue(heatmap.weeks[0][3]?.isToday == true)
        assertFalse(heatmap.weeks[0][4]?.enabled == true)
        assertNull(heatmap.weeks[0][6])
    }

    @Test
    fun heatmapAdapterNormalizesInvalidIntensitiesAndUsesZeroForMissingValues() {
        val month = monthData(firstDayOffset = 0, dayCount = 2)

        val clamped = month.toHeatmap { 5f }
        assertEquals(1f, clamped.weeks[0][0]?.intensity)

        val invalid = month.toHeatmap { Float.NaN }
        assertEquals(0f, invalid.weeks[0][0]?.intensity)

        val fromMap = month.toHeatmap(mapOf(100L to 0.25f))
        assertEquals(0.25f, fromMap.weeks[0][0]?.intensity)
        assertEquals(0f, fromMap.weeks[0][1]?.intensity)
    }

    @Test
    fun heatmapAdapterPadsLongMonthsToSixRows() {
        val heatmap = monthData(firstDayOffset = 6, dayCount = 31).toHeatmap()
        assertEquals(6, heatmap.weeks.size)
        assertTrue(heatmap.weeks.all { it.size == 7 })
    }

    @Test
    fun heatmapAdapterRejectsIncompleteWeekdayLabels() {
        val invalid = object : CalendarMonthData {
            override val title = "Invalid"
            override val weekdayLabels = List(6) { "D$it" }
            override val firstDayOffset = 0
            override val days = monthData(firstDayOffset = 0, dayCount = 1).days
        }
        var rejected = false
        try {
            invalid.toHeatmap()
        } catch (_: IllegalArgumentException) {
            rejected = true
        }
        assertTrue(rejected)
    }

    private fun monthData(firstDayOffset: Int, dayCount: Int): CalendarMonthData = object : CalendarMonthData {
        override val title = "Month"
        override val weekdayLabels = List(7) { "D$it" }
        override val firstDayOffset = firstDayOffset
        override val days = (0 until dayCount).map { index ->
            CalendarDayCell(
                epochDay = 100L + index,
                label = "${index + 1}",
                accessibilityLabel = "Day ${index + 1}",
                enabled = index != 2,
                isToday = index == 1,
            )
        }
    }
}
