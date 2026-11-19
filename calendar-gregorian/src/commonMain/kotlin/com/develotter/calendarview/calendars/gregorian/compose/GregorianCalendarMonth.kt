package com.develotter.calendarview.calendars.gregorian.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.develotter.calendarview.calendars.gregorian.GregorianLabels
import com.develotter.calendarview.calendars.gregorian.GregorianPlatformFormatter
import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarDayMarker
import com.develotter.calendarview.compose.CalendarMonthData
import com.develotter.calendarview.compose.CalendarMonthGrid
import com.develotter.calendarview.locale.CalendarLocale
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
 * Localized Gregorian month-year title, for example `April 2026`.
 *
 * [locale] selects the library's own label tables; `null` keeps the platform default locale.
 */
fun gregorianMonthTitle(month: GregorianYearMonth, locale: CalendarLocale? = null): String =
    locale?.let { GregorianLabels.monthYear(it, month.month, month.year) }
        ?: GregorianPlatformFormatter.formatMonthYear(month.year, month.month)

/**
 * Compact Gregorian secondary label for another primary calendar grid: renders the short month
 * name on the 1st of the Gregorian month and the localized day number on other days.
 */
fun gregorianSecondaryLabel(epochDay: Long, locale: CalendarLocale? = null): String {
    val month = GregorianYearMonth.fromEpochDay(epochDay)
    val dayOfMonth = (epochDay - month.atDay(1)).toInt() + 1
    return when {
        dayOfMonth == 1 && locale != null -> GregorianLabels.shortMonthName(locale, month.month)
        dayOfMonth == 1 -> GregorianPlatformFormatter.formatShortMonth(month.year, month.month)
        locale != null -> GregorianLabels.number(locale, dayOfMonth)
        else -> GregorianPlatformFormatter.formatDayNumber(dayOfMonth)
    }
}

/** Localized full Gregorian date description suitable for screen readers and date inspectors. */
fun gregorianAccessibilityLabel(epochDay: Long, locale: CalendarLocale? = null): String =
    locale?.let { GregorianLabels.fullDate(it, epochDay) }
        ?: GregorianPlatformFormatter.formatFullDate(epochDay)

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
    locale: CalendarLocale? = null,
    isDateEnabled: (Long) -> Boolean = { true },
): CalendarDayCell {
    val month = GregorianYearMonth.fromEpochDay(epochDay)
    val dayOfMonth = (epochDay - month.atDay(1)).toInt() + 1
    return CalendarDayCell(
        epochDay = epochDay,
        label = locale?.let { GregorianLabels.number(it, dayOfMonth) }
            ?: GregorianPlatformFormatter.formatDayNumber(dayOfMonth),
        accessibilityLabel = gregorianAccessibilityLabel(epochDay, locale),
        enabled = isDateEnabled(epochDay),
        isToday = epochDay == todayEpochDay,
    )
}

/** Pure month data. Disabled dates reject taps but may lie inside a selected range. */
fun gregorianMonthData(
    month: GregorianYearMonth,
    firstDayOfWeek: Int = GregorianPlatformFormatter.defaultFirstDayOfWeek(),
    todayEpochDay: Long = GregorianPlatformFormatter.currentEpochDay(),
    locale: CalendarLocale? = null,
    isDateEnabled: (Long) -> Boolean = { true },
): GregorianMonthData {
    val firstEpochDay = month.atDay(1)
    return GregorianMonthData(
        title = gregorianMonthTitle(month, locale),
        weekdayLabels = gregorianWeekdayLabels(firstDayOfWeek, locale),
        firstDayOffset = gregorianDayOfWeekOffset(firstEpochDay, firstDayOfWeek),
        days = (1..month.lengthOfMonth()).map { day ->
            gregorianDayCell(
                epochDay = month.atDay(day),
                todayEpochDay = todayEpochDay,
                isDateEnabled = isDateEnabled,
                locale = locale,
            )
        },
    )
}

/** Binary-compatible overload for callers compiled against 3.0.0. */
@Deprecated(
    message = "Binary compatibility overload for callers compiled against 3.0.0.",
    level = DeprecationLevel.HIDDEN,
)
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
    GregorianCalendarMonth(
        month = month,
        selection = selection,
        onDateClick = onDateClick,
        modifier = modifier,
        firstDayOfWeek = firstDayOfWeek,
        showPreviousMonthDays = showPreviousMonthDays,
        showNextMonthDays = showNextMonthDays,
        todayEpochDay = todayEpochDay,
        showHeader = showHeader,
        showWeekdayLabels = showWeekdayLabels,
        fixedSixRows = fixedSixRows,
        todayBorderColor = todayBorderColor,
        todayDescription = todayDescription,
        markers = markers,
        weekNumber = weekNumber,
        isDateEnabled = isDateEnabled,
        secondaryLabel = null,
        secondaryAccessibilityLabel = null,
    )
}

/**
 * Controlled month UI; caller owns selection and navigation. Adjacent visibility is
 * independent on each side, disabled by default. Adjacent clicks do not navigate.
 * Locale does not set layout direction; [firstDayOfWeek] and all labels come from the
 * platform formatter. [secondaryLabel] and [secondaryAccessibilityLabel] display an optional
 * second calendar label below each day cell.
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
    secondaryLabel: ((Long) -> String?)? = null,
    secondaryAccessibilityLabel: ((Long) -> String?)? = null,
    locale: CalendarLocale? = null,
    dayContent: @Composable (CalendarDayCell, Boolean, Boolean) -> Unit = { day, _, _ -> Text(day.label) },
) {
    val data = gregorianMonthData(
        month = month,
        firstDayOfWeek = firstDayOfWeek,
        todayEpochDay = todayEpochDay,
        isDateEnabled = isDateEnabled,
        locale = locale,
    )
    CalendarMonthGrid(
        title = data.title, weekdayLabels = data.weekdayLabels, firstDayOffset = data.firstDayOffset,
        days = data.days, selection = selection, modifier = modifier,
        onDayClick = onDateClick,
        dayContent = dayContent,
        showPreviousMonthDays = showPreviousMonthDays, showNextMonthDays = showNextMonthDays,
        showHeader = showHeader, showWeekdayLabels = showWeekdayLabels, fixedSixRows = fixedSixRows,
        todayBorderColor = todayBorderColor,
        todayDescription = todayDescription,
        markers = markers,
        weekNumber = weekNumber,
        secondaryLabel = secondaryLabel,
        secondaryAccessibilityLabel = secondaryAccessibilityLabel,
        adjacentDayProvider = { adjacentEpochDay ->
            gregorianDayCell(
                epochDay = adjacentEpochDay,
                todayEpochDay = todayEpochDay,
                isDateEnabled = isDateEnabled,
                locale = locale,
            )
        },
    )
}
