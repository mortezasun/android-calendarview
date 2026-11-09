package com.develotter.calendarview.selection

import com.develotter.calendarview.enums.TypeSelectDay

/**
 * UI-independent selection value for the v2 calendar API.
 *
 * Keys are epoch days (for example, LocalDate.toEpochDay()), not formatted calendar dates.
 * Each calendar engine must map a displayed day to the same absolute-day timeline.
 * Only explicit selections are stored: range interiors are computed without enumeration.
 * Instances expose no mutable collections; publish the returned value through Compose state.
 * This class does not itself provide observable state or persistence.
 */
class CalendarSelection private constructor(
    val mode: TypeSelectDay,
    private val days: List<Long>,
) {
    /** Sorted explicit selections; for ranges these are zero, one or two endpoints. */
    val selectedDays: List<Long>
        get() = days.toList()

    /** Whether a range has two distinct endpoints. */
    val isRangeComplete: Boolean
        get() = mode == TypeSelectDay.Range && days.size == 2

    /** Includes range interiors, even for days that have never been rendered. */
    operator fun contains(epochDay: Long): Boolean = when {
        isRangeComplete -> epochDay >= days[0] && epochDay <= days[1]
        else -> epochDay in days
    }

    /** Whether this day is an explicit range endpoint, including an incomplete range. */
    fun isRangeEndpoint(epochDay: Long): Boolean =
        mode == TypeSelectDay.Range && epochDay in days

    /** Returns an empty selection of the same mode without mutating this value. */
    fun clear(): CalendarSelection = empty(mode)

    /**
     * Applies the existing adapter's tap semantics without touching views.
     *
     * Single/multiple selections toggle. A repeated lone range endpoint clears the range.
     * Tapping either endpoint of a complete range leaves the other as an incomplete range.
     * Outside taps extend the appropriate endpoint; interior taps replace the end.
     * Input availability/disabled-day rules must be checked by the caller before dispatch.
     */
    fun onDayClick(epochDay: Long): CalendarSelection {
        val next = when (mode) {
            TypeSelectDay.None -> emptyList()
            TypeSelectDay.Single -> if (epochDay in days) emptyList() else listOf(epochDay)
            TypeSelectDay.Multiple -> if (epochDay in days) days - epochDay else days + epochDay
            TypeSelectDay.Range -> when {
                epochDay in days -> days - epochDay
                days.size < 2 -> days + epochDay
                epochDay < days[0] -> listOf(epochDay, days[1])
                else -> listOf(days[0], epochDay)
            }
        }
        return from(mode, next)
    }

    companion object {
        /** Creates an empty value in the requested mode. */
        fun empty(mode: TypeSelectDay): CalendarSelection = CalendarSelection(mode, emptyList())

        /**
         * Restores a selection from absolute-day keys. Copies, deduplicates and sorts input.
         * None accepts no dates, Single at most one, and Range at most two distinct endpoints.
         * Invalid restored values fail immediately rather than silently discarding selections.
         */
        fun from(mode: TypeSelectDay, epochDays: Iterable<Long>): CalendarSelection {
            val days = epochDays.distinct().sorted()
            val maximum = when (mode) {
                TypeSelectDay.None -> 0
                TypeSelectDay.Single -> 1
                TypeSelectDay.Multiple -> Int.MAX_VALUE
                TypeSelectDay.Range -> 2
            }
            require(days.size <= maximum) { "Too many selected days for $mode: ${days.size}" }
            return CalendarSelection(mode, days)
        }
    }
}
