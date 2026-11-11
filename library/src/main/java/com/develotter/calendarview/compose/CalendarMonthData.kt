package com.develotter.calendarview.compose

/**
 * Calendar-independent month snapshot used by every calendar engine.
 *
 * Implementations supply chronological current-month cells, ordered weekday labels and the
 * leading empty-cell offset. The shared month grid and the heatmap adapters only read this
 * contract, so `:library` stays independent of concrete calendar systems.
 */
interface CalendarMonthData {
    /** Localized month caption, for example `November 2026` or `Ramadan 1448 AH`. */
    val title: String

    /** Exactly seven localized weekday labels in grid order. */
    val weekdayLabels: List<String>

    /** Number of empty cells before the first day of the month, in `0..6`. */
    val firstDayOffset: Int

    /** Chronological current-month days; adjacent months are built by the grid. */
    val days: List<CalendarDayCell>
}

/**
 * Converts this month into the calendar-agnostic year-heatmap block format.
 *
 * [intensity] receives the absolute epoch day and returns the cell value; finite values are
 * clamped into `0..1`, NaN and infinities become zero. Missing intensities default to zero.
 * `isToday` and `enabled` are forwarded from [CalendarDayCell], and the accessibility label
 * becomes the heatmap day label. The result always contains whole seven-day rows, padded with
 * `null` outside the month.
 */
fun CalendarMonthData.toHeatmap(intensity: (Long) -> Float = { 0f }): HeatmapMonthData {
    require(weekdayLabels.size == 7) { "Exactly seven weekday labels are required" }
    require(firstDayOffset in 0..6) { "firstDayOffset must be in 0..6" }
    val cells = buildList<HeatmapDayData?> {
        repeat(firstDayOffset) { add(null) }
        days.forEach { day ->
            add(
                HeatmapDayData(
                    epochDay = day.epochDay,
                    label = day.accessibilityLabel,
                    intensity = normalizeHeatmapIntensity(intensity(day.epochDay)),
                    isToday = day.isToday,
                    enabled = day.enabled,
                ),
            )
        }
        while (size % 7 != 0) add(null)
    }
    return HeatmapMonthData(title, weekdayLabels, cells.chunked(7))
}

/** Heatmap adapter for precomputed intensities keyed by absolute epoch day. */
fun CalendarMonthData.toHeatmap(intensities: Map<Long, Float>): HeatmapMonthData =
    toHeatmap { epochDay -> intensities[epochDay] ?: 0f }
