package com.develotter.calendarview.calendars.gregorian.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.develotter.calendarview.calendars.gregorian.GregorianPlatformFormatter
import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarDayMarker
import com.develotter.calendarview.compose.CalendarMonthData
import com.develotter.calendarview.compose.CalendarMonthGrid
import com.develotter.calendarview.selection.CalendarSelection

/** Localized month snapshot; treat lists as read-only. */
@Immutable
data class GregorianMonthData(
    override val title: String,
    override val weekdayLabels: List<String>,
    override val firstDayOffset: Int,
    override val days: List<CalendarDayCell>,
) : CalendarMonthData

/**
 * Formats one Gregorian date for either current or adjacent month rendering.
 *
 * [epochDay] is the absolute day identity, [todayEpochDay] the identity of today and
 * [isDateEnabled] receives the same identity. Formatting is delegated to the platform
 * formatter so the default locale and its digits are used.
 */
fun gregorianDayCell(
    epochDay: Long,
    todayEpochDay: Long = GregorianPlatformFormatter.currentEpochDay(),
    isDateEnabled: (Long) -> Boolean = { true },
): CalendarDayCell {
    val month = GregorianYearMonth.fromEpochDay(epochDay)
    val dayOfMonth = (epochDay - month.atDay(1)).toInt() + 1
    return CalendarDayCell(
        epochDay = epochDay,
        label = GregorianPlatformFormatter.formatDayNumber(dayOfMonth),
        accessibilityLabel = GregorianPlatformFormatter.formatFullDate(epochDay),
        enabled = isDateEnabled(epochDay),
        isToday = epochDay == todayEpochDay,
    )
}

/** Pure month data. Disabled dates reject taps but may lie inside a selected range. */
fun gregorianMonthData(
    month: GregorianYearMonth,
    firstDayOfWeek: Int = GregorianPlatformFormatter.defaultFirstDayOfWeek(),
    todayEpochDay: Long = GregorianPlatformFormatter.currentEpochDay(),
    isDateEnabled: (Long) -> Boolean = { true },
): GregorianMonthData {
    val firstEpochDay = month.atDay(1)
    return GregorianMonthData(
        title = GregorianPlatformFormatter.formatMonthYear(month.year, month.month),
        weekdayLabels = gregorianWeekdayLabels(firstDayOfWeek),
        firstDayOffset = gregorianDayOfWeekOffset(firstEpochDay, firstDayOfWeek),
        days = (1..month.lengthOfMonth()).map { day ->
            gregorianDayCell(
                epochDay = month.atDay(day),
                todayEpochDay = todayEpochDay,
                isDateEnabled = isDateEnabled,
            )
        },
    )
}

/**
 * Controlled month UI; caller owns selection and navigation. Adjacent visibility is
 * independent on each side, disabled by default. Adjacent clicks do not navigate.
 * Locale does not set layout direction; [firstDayOfWeek] and all labels come from the
 * platform formatter. For custom slots use month/day builders with CalendarMonthGrid.
 * Date-range errors propagate rather than displaying incorrect dates.
 */
@Composable
fun GregorianCalendarMonth(
    month: GregorianYearMonth,
    selection: CalendarSelection,
    onDateClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    firstDayOfWeek: Int = GregorianPlatformFormatter.defaultFirstDayOfWeek(),
    showPreviousMonthDays: Boolean = false,
    showNextMonthDays: Boolean = false,
    todayEpochDay: Long = GregorianPlatformFormatter.currentEpochDay(),
    showHeader: Boolean = true,
    showWeekdayLabels: Boolean = true,
    fixedSixRows: Boolean = false,
    todayBorderColor: Color? = null,
    todayDescription: String? = null,
    markers: Map<Long, List<CalendarDayMarker>> = emptyMap(),
    weekNumber: ((Long) -> String?)? = null,
    isDateEnabled: (Long) -> Boolean = { true },
) {
    val data = gregorianMonthData(
        month = month,
        firstDayOfWeek = firstDayOfWeek,
        todayEpochDay = todayEpochDay,
        isDateEnabled = isDateEnabled,
    )
    CalendarMonthGrid(
        title = data.title, weekdayLabels = data.weekdayLabels, firstDayOffset = data.firstDayOffset,
        days = data.days, selection = selection, modifier = modifier,
        onDayClick = onDateClick,
        showPreviousMonthDays = showPreviousMonthDays, showNextMonthDays = showNextMonthDays,
        showHeader = showHeader, showWeekdayLabels = showWeekdayLabels, fixedSixRows = fixedSixRows,
        todayBorderColor = todayBorderColor,
        todayDescription = todayDescription,
        markers = markers,
        weekNumber = weekNumber,
        adjacentDayProvider = { adjacentEpochDay ->
            gregorianDayCell(
                epochDay = adjacentEpochDay,
                todayEpochDay = todayEpochDay,
                isDateEnabled = isDateEnabled,
            )
        },
    )
}
