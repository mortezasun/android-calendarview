package com.develotter.calendarview.selection

/**
 * Validates that [firstDayOfWeek] is a valid ISO-8601 weekday (1 = Monday .. 7 = Sunday)
 * and returns it. Throws [IllegalArgumentException] for any value outside `1..7`.
 */
fun requireValidFirstDayOfWeek(firstDayOfWeek: Int): Int {
    require(firstDayOfWeek in 1..7) { "firstDayOfWeek must be in 1..7: $firstDayOfWeek" }
    return firstDayOfWeek
}

/**
 * Returns the leading empty-cell offset (`0..6`) before [epochDay] in a seven-column grid whose
 * first column is [firstDayOfWeek] (1 = Monday .. 7 = Sunday).
 */
fun firstDayOffset(epochDay: Long, firstDayOfWeek: Int): Int {
    requireValidFirstDayOfWeek(firstDayOfWeek)
    return (isoWeekday(epochDay) - firstDayOfWeek).mod(7)
}

/**
 * Returns the first epoch day of the seven-day week containing [epochDay] when weeks start on
 * [firstDayOfWeek] (1 = Monday .. 7 = Sunday).
 */
fun weekStartEpochDay(epochDay: Long, firstDayOfWeek: Int): Long =
    epochDay - firstDayOffset(epochDay, firstDayOfWeek)

/**
 * Returns the seven ISO-8601 weekday numbers (`1..7`) in grid order starting at [firstDayOfWeek].
 */
fun orderedIsoWeekdays(firstDayOfWeek: Int): List<Int> {
    requireValidFirstDayOfWeek(firstDayOfWeek)
    return List(7) { offset -> (firstDayOfWeek - 1 + offset).mod(7) + 1 }
}

/**
 * Builds seven weekday labels in grid order starting at [firstDayOfWeek] (1 = Monday .. 7 = Sunday)
 * by invoking [labelForIsoWeekday] for each ISO weekday in sequence.
 */
inline fun weekdayLabelsStartingAt(
    firstDayOfWeek: Int,
    labelForIsoWeekday: (Int) -> String,
): List<String> {
    requireValidFirstDayOfWeek(firstDayOfWeek)
    return List(7) { offset ->
        val isoWeekday = (firstDayOfWeek - 1 + offset).mod(7) + 1
        labelForIsoWeekday(isoWeekday)
    }
}
