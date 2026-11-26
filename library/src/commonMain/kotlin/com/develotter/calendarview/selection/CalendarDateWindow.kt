package com.develotter.calendarview.selection

import androidx.compose.runtime.Immutable

/**
 * Inclusive epoch-day window of days a caller allows to be selected.
 *
 * Either bound may be `null` for an open end; the default window accepts every day. A window
 * complements [CalendarRangePolicy], which limits the range length: the window limits where a
 * range may lie. It is a plain value, so it adds no state to the stateless composables. Pass
 * [isDateEnabled] as the `isDateEnabled` argument of any month, week, agenda or heatmap
 * composable, and use [allows] to validate an externally built range.
 */
@Immutable
data class CalendarDateWindow(
    val minEpochDay: Long? = null,
    val maxEpochDay: Long? = null,
) {
    init {
        require(minEpochDay == null || maxEpochDay == null || minEpochDay <= maxEpochDay) {
            "minEpochDay must be null or not after maxEpochDay"
        }
    }

    /** Whether [epochDay] lies inside the window (both bounds inclusive). */
    operator fun contains(epochDay: Long): Boolean =
        (minEpochDay == null || epochDay >= minEpochDay) &&
            (maxEpochDay == null || epochDay <= maxEpochDay)

    /** Whether both endpoints lie inside the window. Endpoint order is irrelevant. */
    fun allows(startEpochDay: Long, endEpochDay: Long): Boolean =
        startEpochDay in this && endEpochDay in this

    /** `isDateEnabled` adapter for the calendar composables and data builders. */
    val isDateEnabled: (Long) -> Boolean
        get() = { epochDay -> epochDay in this }
}
