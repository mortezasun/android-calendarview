package com.develotter.calendarview.selection

import androidx.compose.runtime.Immutable
import com.develotter.calendarview.enums.TypeSelectDay

/**
 * Pure state of a range drag: the day where the gesture started and the day it currently covers.
 *
 * This is logic only. The host owns the pointer gesture and the hit test that turns a pointer
 * position into an epoch day, calls [moveTo] while dragging and checks [isAllowed] before it
 * commits the result, for example through `CalendarSelectionState.selectRange`. The value is
 * immutable, so every update returns a new instance. The anchor and the current day may lie in
 * either order; [startEpochDay] and [endEpochDay] are always sorted.
 */
@Immutable
data class CalendarRangeDrag(
    val anchorEpochDay: Long,
    val currentEpochDay: Long = anchorEpochDay,
) {
    /** First day of the dragged range. */
    val startEpochDay: Long get() = minOf(anchorEpochDay, currentEpochDay)

    /** Last day of the dragged range. */
    val endEpochDay: Long get() = maxOf(anchorEpochDay, currentEpochDay)

    /** Number of days covered, counting both endpoints. */
    val length: Long get() = endEpochDay - startEpochDay + 1

    /** Whether [epochDay] lies inside the dragged range. */
    operator fun contains(epochDay: Long): Boolean = epochDay in startEpochDay..endEpochDay

    /**
     * Moves the dragged end to [epochDay]. A day rejected by [isDateEnabled] leaves the drag
     * unchanged, so the range never ends on a disabled day.
     */
    fun moveTo(epochDay: Long, isDateEnabled: (Long) -> Boolean = { true }): CalendarRangeDrag =
        if (isDateEnabled(epochDay)) copy(currentEpochDay = epochDay) else this

    /**
     * Whether the dragged range may be committed: both endpoints must lie in [window] and the
     * length must satisfy [policy]. A single-day drag is accepted only when the policy accepts
     * length 1.
     */
    fun isAllowed(
        policy: CalendarRangePolicy = CalendarRangePolicy(),
        window: CalendarDateWindow = CalendarDateWindow(),
    ): Boolean = window.allows(startEpochDay, endEpochDay) && policy.allows(startEpochDay, endEpochDay)

    /** Range selection that previews the drag, for rendering the highlight while it is active. */
    fun toSelection(): CalendarSelection =
        CalendarSelection.from(TypeSelectDay.Range, listOf(startEpochDay, endEpochDay))
}
