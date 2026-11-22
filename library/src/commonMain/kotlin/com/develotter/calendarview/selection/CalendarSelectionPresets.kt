package com.develotter.calendarview.selection

import androidx.compose.runtime.Immutable

/**
 * Common quick-selection ranges derived from a reference [today] epoch day.
 *
 * @property today The explicit today epoch day.
 * @property thisWeek The seven-day window starting on the configured first day of week that contains [today].
 * @property thisMonth The civil Gregorian month range containing [today].
 * @property lastSevenDays The seven-day range ending on [today] (`today - 6 .. today`).
 */
@Immutable
data class CalendarSelectionPresets(
    val today: Long,
    val thisWeek: LongRange,
    val thisMonth: LongRange,
    val lastSevenDays: LongRange,
)

/**
 * Derives common selection presets for the given [todayEpochDay] and [firstDayOfWeek] (1 = Monday .. 7 = Sunday).
 */
fun CalendarSelection.presets(
    firstDayOfWeek: Int,
    todayEpochDay: Long,
): CalendarSelectionPresets {
    val weekStart = weekStartFor(todayEpochDay, firstDayOfWeek)
    val thisWeek = weekStart..(weekStart + 6)

    val civil = civilFromEpochDay(todayEpochDay)
    val monthStart = epochDayOf(civil.year, civil.month, 1)
    val monthEnd = epochDayOf(civil.year, civil.month, daysInGregorianMonth(civil.year, civil.month))
    val thisMonth = monthStart..monthEnd

    val lastSevenDays = (todayEpochDay - 6)..todayEpochDay

    return CalendarSelectionPresets(
        today = todayEpochDay,
        thisWeek = thisWeek,
        thisMonth = thisMonth,
        lastSevenDays = lastSevenDays,
    )
}

/** Returns the first epoch day of the week containing [epochDay] for an explicit ISO weekday. */
private fun weekStartFor(epochDay: Long, firstDayOfWeek: Int): Long {
    require(firstDayOfWeek in 1..7) { "firstDayOfWeek must be in 1..7: $firstDayOfWeek" }
    return epochDay - (isoWeekday(epochDay) - firstDayOfWeek).mod(7)
}
