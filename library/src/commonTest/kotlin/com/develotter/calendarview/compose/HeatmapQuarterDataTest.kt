package com.develotter.calendarview.compose

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class HeatmapQuarterDataTest {
    @Test
    fun allQuartersCoverEveryDayExactlyOnceIncludingLeapDay() {
        val expectedDays = mapOf(2024 to 366, 2025 to 365)
        val firstEpochDay = mapOf(2024 to 19723L, 2025 to 20089L)
        val lastEpochDay = mapOf(2024 to 20088L, 2025 to 20453L)
        for (year in listOf(2024, 2025)) {
            val days = (1..4).flatMap { quarter ->
                heatmapQuarterData(year, quarter).weeks.flatten().filterNotNull().map { it.epochDay }
            }
            assertEquals(expectedDays.getValue(year), days.size)
            assertEquals(days.size, days.distinct().size)
            assertEquals(firstEpochDay.getValue(year), days.first())
            assertEquals(lastEpochDay.getValue(year), days.last())
            assertTrue(days.zipWithNext().all { (a, b) -> b == a + 1 })
        }
    }

    @Test
    fun cellsUseSevenDayWeeksAndRespectConfiguredWeekStart() {
        val sunday = heatmapQuarterData(2026, 4, firstDayOfWeek = 7)
        val monday = heatmapQuarterData(2026, 4, firstDayOfWeek = 1)
        assertTrue(sunday.weeks.all { it.size == 7 })
        assertEquals(20727L, sunday.weeks.first()[4]?.epochDay) // 2026-10-01
        assertEquals(20727L, monday.weeks.first()[3]?.epochDay) // 2026-10-01
        assertTrue(sunday.weeks.first().take(4).all { it == null })
        assertTrue(sunday.weeks.last().drop(5).all { it == null })
    }

    @Test
    fun invalidIntensitiesAreNormalized() {
        assertEquals(0f, normalizeHeatmapIntensity(-1f))
        assertEquals(1f, normalizeHeatmapIntensity(2f))
        assertEquals(0.5f, normalizeHeatmapIntensity(0.5f))
        for (invalid in listOf(Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)) {
            assertEquals(0f, normalizeHeatmapIntensity(invalid))
        }
    }

    @Test
    fun dataIsCopiedAndOutsideQuarterValuesAreIgnored() {
        val firstEpochDay = heatmapQuarterData(2026, 1).weeks.flatten().first { it != null }!!.epochDay
        val input = mutableMapOf(firstEpochDay to 0.5f, firstEpochDay - 1 to 1f)
        val data = heatmapQuarterData(2026, 1, input)
        input.clear()
        val cells = data.weeks.flatten().filterNotNull()
        assertEquals(0.5f, cells.first().intensity)
        assertTrue(cells.drop(1).all { it.intensity == 0f })
        assertTrue(cells.all { it.epochDay >= firstEpochDay })
    }

    @Test
    fun invalidQuarterIsRejected() {
        assertFailsWith<IllegalArgumentException> { heatmapQuarterData(2026, 0) }
    }
}
