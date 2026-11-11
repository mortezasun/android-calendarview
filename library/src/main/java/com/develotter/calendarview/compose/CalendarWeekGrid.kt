package com.develotter.calendarview.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.develotter.calendarview.selection.CalendarSelection

/**
 * Calendar-independent week snapshot.
 *
 * [days] must contain exactly seven consecutive absolute days; [weekdayLabels] must contain
 * seven labels in the same order. Engines build this from their own calendar arithmetic.
 */
interface CalendarWeekData {
    /** Localized week caption, for example a date range. */
    val title: String

    /** Exactly seven localized weekday labels in grid order. */
    val weekdayLabels: List<String>

    /** Exactly seven consecutive days covering the week. */
    val days: List<CalendarDayCell>

    /** Optional localized week number; null hides the leading week-number column. */
    val weekNumber: String?
        get() = null
}

/**
 * Stateless seven-day week view built on [CalendarMonthGrid].
 *
 * The caller owns selection and navigation. [weekNumber] is caller formatted and appears in a
 * leading column; the remaining layout, marker, today and accessibility behavior matches the
 * month grid. Use the engine `*WeekData` builders to obtain the seven cells.
 */
@Composable
fun CalendarWeekGrid(
    title: String,
    weekdayLabels: List<String>,
    days: List<CalendarDayCell>,
    selection: CalendarSelection,
    onDayClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    showHeader: Boolean = true,
    showWeekdayLabels: Boolean = true,
    todayBorderColor: Color? = null,
    todayDescription: String? = null,
    markers: Map<Long, List<CalendarDayMarker>> = emptyMap(),
    weekNumber: String? = null,
) {
    require(days.size == 7) { "A week must contain exactly seven days" }
    val weekNumberProvider: ((Long) -> String?)? =
        weekNumber?.let { label -> { _: Long -> label } }
    CalendarMonthGrid(
        title = title,
        weekdayLabels = weekdayLabels,
        firstDayOffset = 0,
        days = days,
        selection = selection,
        onDayClick = onDayClick,
        modifier = modifier,
        showHeader = showHeader,
        showWeekdayLabels = showWeekdayLabels,
        todayBorderColor = todayBorderColor,
        todayDescription = todayDescription,
        markers = markers,
        weekNumber = weekNumberProvider,
    )
}
