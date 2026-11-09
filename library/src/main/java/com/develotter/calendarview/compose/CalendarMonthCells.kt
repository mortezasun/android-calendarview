package com.develotter.calendarview.compose

/** Display label plus absolute-day identity. Adjacent cells are marked by the grid builder. */
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
    adjacentDayProvider: ((Long) -> CalendarDayCell)? = null,
    fixedSixRows: Boolean = false,
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
