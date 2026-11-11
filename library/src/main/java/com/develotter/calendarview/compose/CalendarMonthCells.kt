package com.develotter.calendarview.compose

import androidx.compose.runtime.Immutable

/** Display label plus absolute-day identity. Adjacent cells are marked by the grid builder. */
@Immutable
data class CalendarDayCell(
    val epochDay: Long,
    val label: String,
    val accessibilityLabel: String,
    val enabled: Boolean = true,
    val isCurrentMonth: Boolean = true,
    val isToday: Boolean = false,
)

/**
 * Creates complete seven-column rows from chronological current-month cells.
 * Previous/next padding can independently remain null or contain real adjacent dates.
 * When [fixedSixRows] is true, the grid always produces 42 cells (6 rows).
 * The provider must format/validate dates using the chosen calendar engine, return the
 * requested epoch identity, and supply enabled flags. Calendar-range/overflow errors
 * propagate rather than silently substituting a different date or calendar system.
 * Inputs are not mutated. Layout direction is deliberately a rendering concern.
 */
fun buildCalendarMonthCells(
    days: List<CalendarDayCell>,
    firstDayOffset: Int,
    showPreviousMonthDays: Boolean = false,
    showNextMonthDays: Boolean = false,
    fixedSixRows: Boolean = false,
    adjacentDayProvider: ((Long) -> CalendarDayCell)? = null,
): List<CalendarDayCell?> {
    require(days.size in 1..31) { "A month must contain 1..31 days" }
    require(firstDayOffset in 0..6) { "firstDayOffset must be in 0..6" }
    require(days.zipWithNext().all { (a, b) -> a.epochDay != Long.MAX_VALUE && b.epochDay == a.epochDay + 1 }) {
        "Current-month days must have consecutive chronological identities"
    }
    val cellCount = if (fixedSixRows) 42 else ((firstDayOffset + days.size + 6) / 7) * 7
    return List(cellCount) { index ->
        val relative = index - firstDayOffset
        when {
            relative in days.indices -> days[relative].copy(isCurrentMonth = true)
            relative < 0 && !showPreviousMonthDays -> null
            relative >= days.size && !showNextMonthDays -> null
            else -> {
                val provider = requireNotNull(adjacentDayProvider) {
                    "An adjacentDayProvider is required for visible adjacent dates"
                }
                val epochDay = Math.addExact(days.first().epochDay, relative.toLong())
                val cell = provider(epochDay)
                require(cell.epochDay == epochDay) { "Adjacent provider returned a different day identity" }
                cell.copy(isCurrentMonth = false)
            }
        }
    }
}

/** Overload retaining positional compatibility when [adjacentDayProvider] is passed as the 5th parameter. */
fun buildCalendarMonthCells(
    days: List<CalendarDayCell>,
    firstDayOffset: Int,
    showPreviousMonthDays: Boolean,
    showNextMonthDays: Boolean,
    adjacentDayProvider: ((Long) -> CalendarDayCell)?,
): List<CalendarDayCell?> = buildCalendarMonthCells(
    days = days,
    firstDayOffset = firstDayOffset,
    showPreviousMonthDays = showPreviousMonthDays,
    showNextMonthDays = showNextMonthDays,
    fixedSixRows = false,
    adjacentDayProvider = adjacentDayProvider,
)
