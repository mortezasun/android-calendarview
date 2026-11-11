package com.develotter.calendarview.selection

import androidx.compose.runtime.Immutable

/**
 * Inclusive length limits for [com.develotter.calendarview.enums.TypeSelectDay.Range].
 *
 * The length counts both endpoints, so a start and end on the same day have length 1. Limits
 * apply when a range becomes complete; an incomplete first endpoint is never rejected. The
 * default policy accepts every range. This is a validation policy, not a UI gesture: callers
 * can use it for tap selection or for an externally implemented drag layer.
 */
@Immutable
data class CalendarRangePolicy(
    val minRangeLength: Int = 1,
    val maxRangeLength: Int? = null,
) {
    init {
        require(minRangeLength >= 1) { "minRangeLength must be at least 1" }
        require(maxRangeLength == null || maxRangeLength >= minRangeLength) {
            "maxRangeLength must be null or at least minRangeLength"
        }
    }

    /** Whether the two absolute days form an allowed inclusive range. Order is irrelevant. */
    fun allows(startEpochDay: Long, endEpochDay: Long): Boolean {
        val start = minOf(startEpochDay, endEpochDay)
        val end = maxOf(startEpochDay, endEpochDay)
        val distance = end - start
        if (distance < 0L) return false
        val length = distance + 1
        if (length <= 0L) return false
        return length >= minRangeLength.toLong() &&
            (maxRangeLength == null || length <= maxRangeLength.toLong())
    }
}
