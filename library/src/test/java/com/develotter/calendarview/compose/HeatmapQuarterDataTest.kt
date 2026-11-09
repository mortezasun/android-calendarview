package com.develotter.calendarview.compose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Year

class HeatmapQuarterDataTest {
    @Test
    fun allQuartersCoverEveryDayExactlyOnceIncludingLeapDay() {
        for (year in listOf(2024, 2025)) {
            val dates = (1..4).flatMap { heatmapQuarterData(year, it).weeks.flatten().filterNotNull().map { cell -> cell.date } }
            assertEquals(Year.of(year).length(), dates.size)
            assertEquals(dates.size, dates.distinct().size)
            assertEquals(LocalDate.of(year, 1, 1), dates.first())
            assertEquals(LocalDate.of(year, 12, 31), dates.last())
        }
    }

    @Test
    fun cellsUseSevenDayWeeksAndRespectConfiguredWeekStart() {
        val sunday = heatmapQuarterData(2026, 4, firstDayOfWeek = DayOfWeek.SUNDAY)
        val monday = heatmapQuarterData(2026, 4, firstDayOfWeek = DayOfWeek.MONDAY)
        assertTrue(sunday.weeks.all { it.size == 7 })
        assertEquals(LocalDate.of(2026, 10, 1), sunday.weeks.first()[4]?.date)
        assertEquals(LocalDate.of(2026, 10, 1), monday.weeks.first()[3]?.date)
        assertTrue(sunday.weeks.first().take(4).all { it == null })
        assertTrue(sunday.weeks.last().drop(5).all { it == null })
    }

    @Test
    fun invalidIntensitiesAreNormalized() {
        assertEquals(0f, normalizeHeatmapIntensity(-1f), 0f)
        assertEquals(1f, normalizeHeatmapIntensity(2f), 0f)
        assertEquals(0.5f, normalizeHeatmapIntensity(0.5f), 0f)
        for (invalid in listOf(Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)) {
            assertEquals(0f, normalizeHeatmapIntensity(invalid), 0f)
        }
    }

    @Test
    fun dataIsCopiedAndOutsideQuarterValuesAreIgnored() {
        val first = LocalDate.of(2026, 1, 1)
        val input = mutableMapOf(first to 0.5f, LocalDate.of(2025, 12, 31) to 1f)
        val data = heatmapQuarterData(2026, 1, input)
        input.clear()
        val cells = data.weeks.flatten().filterNotNull()
        assertEquals(0.5f, cells.first().intensity, 0f)
        assertTrue(cells.drop(1).all { it.intensity == 0f })
        assertTrue(cells.all { it.date.year == 2026 })
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidQuarterIsRejected() {
        heatmapQuarterData(2026, 0)
    }
}
