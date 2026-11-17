package com.develotter.calendarview.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
 * Binary-compatible overload for callers compiled against 3.0.0.
 */
@Deprecated(
    message = "Binary compatibility overload for callers compiled against 3.0.0.",
    level = DeprecationLevel.HIDDEN,
)
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
    CalendarWeekGrid(
        title = title,
        weekdayLabels = weekdayLabels,
        days = days,
        selection = selection,
        onDayClick = onDayClick,
        modifier = modifier,
        showHeader = showHeader,
        showWeekdayLabels = showWeekdayLabels,
        todayBorderColor = todayBorderColor,
        todayDescription = todayDescription,
        markers = markers,
        weekNumber = weekNumber,
        secondaryLabel = null,
        secondaryAccessibilityLabel = null,
    )
}

/**
 * Convenience overload rendering a shared [CalendarWeekData] snapshot directly.
 */
@Composable
fun CalendarWeekGrid(
    data: CalendarWeekData,
    selection: CalendarSelection,
    onDayClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    showHeader: Boolean = true,
    showWeekdayLabels: Boolean = true,
    todayBorderColor: Color? = null,
    todayDescription: String? = null,
    markers: Map<Long, List<CalendarDayMarker>> = emptyMap(),
    weekNumber: String? = data.weekNumber,
    header: @Composable (String) -> Unit = { Text(it, style = MaterialTheme.typography.titleMedium) },
    weekdayContent: @Composable (String) -> Unit = { Text(it) },
    weekNumberContent: @Composable (String) -> Unit = { Text(it) },
    secondaryLabel: ((Long) -> String?)? = null,
    secondaryAccessibilityLabel: ((Long) -> String?)? = null,
    dayContent: @Composable (CalendarDayCell, Boolean, Boolean) -> Unit = { day, _, _ -> Text(day.label) },
) {
    CalendarWeekGrid(
        title = data.title,
        weekdayLabels = data.weekdayLabels,
        days = data.days,
        selection = selection,
        onDayClick = onDayClick,
        modifier = modifier,
        showHeader = showHeader,
        showWeekdayLabels = showWeekdayLabels,
        todayBorderColor = todayBorderColor,
        todayDescription = todayDescription,
        markers = markers,
        weekNumber = weekNumber,
        header = header,
        weekdayContent = weekdayContent,
        weekNumberContent = weekNumberContent,
        secondaryLabel = secondaryLabel,
        secondaryAccessibilityLabel = secondaryAccessibilityLabel,
        dayContent = dayContent,
    )
}

/**
 * Stateless seven-day week view built on [CalendarMonthGrid].
 *
 * The caller owns selection and navigation. [weekNumber] is caller formatted and appears in a
 * leading column; the remaining layout, marker, today, secondary-label and accessibility behavior
 * matches the month grid. Use the engine `*WeekData` builders to obtain the seven cells.
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
    header: @Composable (String) -> Unit = { Text(it, style = MaterialTheme.typography.titleMedium) },
    weekdayContent: @Composable (String) -> Unit = { Text(it) },
    weekNumberContent: @Composable (String) -> Unit = { Text(it) },
    secondaryLabel: ((Long) -> String?)? = null,
    secondaryAccessibilityLabel: ((Long) -> String?)? = null,
    dayContent: @Composable (CalendarDayCell, Boolean, Boolean) -> Unit = { day, _, _ -> Text(day.label) },
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
        header = header,
        weekdayContent = weekdayContent,
        dayContent = dayContent,
        showHeader = showHeader,
        showWeekdayLabels = showWeekdayLabels,
        todayBorderColor = todayBorderColor,
        todayDescription = todayDescription,
        markers = markers,
        weekNumber = weekNumberProvider,
        weekNumberContent = weekNumberContent,
        secondaryLabel = secondaryLabel,
        secondaryAccessibilityLabel = secondaryAccessibilityLabel,
    )
}
