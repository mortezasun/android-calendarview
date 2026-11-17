package com.develotter.calendarview.calendars.solarHijriNative.compose

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriEngine
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriLocale
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriPlatform
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarDayMarker
import com.develotter.calendarview.compose.CalendarMonthData
import com.develotter.calendarview.compose.CalendarMonthGrid
import com.develotter.calendarview.selection.CalendarSelection

/** Localized month snapshot; treat lists as read-only. */
@Immutable
data class SolarHijriNativeMonthData(
    override val title: String,
    override val weekdayLabels: List<String>,
    override val firstDayOffset: Int,
    override val days: List<CalendarDayCell>,
) : CalendarMonthData

/** Localized Solar Hijri month-year title, for example `Farvardin 1404`. */
fun solarHijriNativeMonthTitle(month: SolarHijriDate): String =
    SolarHijriLocale.formatMonthYear(month.year, month.month)

/**
 * Compact Solar Hijri secondary label for another primary calendar grid: renders the localized
 * month name on the 1st of the Solar Hijri month and the localized day number on other days, or
 * `null` if [epochDay] is outside [SolarHijriEngine.MIN_EPOCH_DAY]..[SolarHijriEngine.MAX_EPOCH_DAY].
 */
fun solarHijriNativeSecondaryLabel(epochDay: Long): String? {
    if (epochDay !in SolarHijriEngine.MIN_EPOCH_DAY..SolarHijriEngine.MAX_EPOCH_DAY) return null
    val date = SolarHijriDate.fromEpochDay(epochDay)
    return if (date.day == 1) {
        SolarHijriLocale.monthName(date.month)
    } else {
        SolarHijriLocale.formatNumber(date.day)
    }
}

/**
 * Localized full Solar Hijri date description suitable for screen readers and date inspectors, or
 * `null` if [epochDay] is outside [SolarHijriEngine.MIN_EPOCH_DAY]..[SolarHijriEngine.MAX_EPOCH_DAY].
 */
fun solarHijriNativeAccessibilityLabel(epochDay: Long): String? {
    if (epochDay !in SolarHijriEngine.MIN_EPOCH_DAY..SolarHijriEngine.MAX_EPOCH_DAY) return null
    return SolarHijriLocale.formatFullDate(SolarHijriDate.fromEpochDay(epochDay))
}

/** Formats one Solar Hijri date; [isDateEnabled] receives the absolute epoch day. */
fun solarHijriNativeDayCell(
    epochDay: Long,
    todayEpochDay: Long = SolarHijriPlatform.currentEpochDay(),
    isDateEnabled: (Long) -> Boolean = { true },
): CalendarDayCell {
    val date = SolarHijriDate.fromEpochDay(epochDay)
    return CalendarDayCell(
        epochDay = epochDay,
        label = SolarHijriLocale.formatNumber(date.day),
        accessibilityLabel = SolarHijriLocale.formatFullDate(date),
        enabled = isDateEnabled(epochDay),
        isToday = epochDay == todayEpochDay,
    )
}

/**
 * Cell for any epoch day, including days beyond the supported years. The grid asks for adjacent
 * days of the first and last supported month; those get a blank, disabled cell instead of an
 * exception.
 */
internal fun solarHijriNativeDayCellForEpoch(
    epochDay: Long,
    todayEpochDay: Long,
    isDateEnabled: (Long) -> Boolean,
): CalendarDayCell =
    if (epochDay in SolarHijriEngine.MIN_EPOCH_DAY..SolarHijriEngine.MAX_EPOCH_DAY) {
        solarHijriNativeDayCell(epochDay, todayEpochDay, isDateEnabled)
    } else {
        CalendarDayCell(epochDay = epochDay, label = "", accessibilityLabel = "", enabled = false, isToday = false)
    }

/** Saturday-first is configurable; non-Persian month names are English transliterations. */
fun solarHijriNativeMonthData(
    month: SolarHijriDate,
    firstDayOfWeek: Int = SolarHijriNativeDefaultFirstDayOfWeek,
    todayEpochDay: Long = SolarHijriPlatform.currentEpochDay(),
    isDateEnabled: (Long) -> Boolean = { true },
): SolarHijriNativeMonthData {
    val first = month.withDayOfMonth(1)
    val firstEpochDay = first.toEpochDay()
    return SolarHijriNativeMonthData(
        title = solarHijriNativeMonthTitle(first),
        weekdayLabels = solarHijriNativeWeekdayLabels(firstDayOfWeek),
        firstDayOffset = solarHijriNativeDayOfWeekOffset(firstEpochDay, firstDayOfWeek),
        days = (1..first.lengthOfMonth()).map { day ->
            solarHijriNativeDayCell(
                epochDay = first.withDayOfMonth(day).toEpochDay(),
                todayEpochDay = todayEpochDay,
                isDateEnabled = isDateEnabled,
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
fun SolarHijriNativeCalendarMonth(
    month: SolarHijriDate,
    selection: CalendarSelection,
    onDateClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    firstDayOfWeek: Int = SolarHijriNativeDefaultFirstDayOfWeek,
    showPreviousMonthDays: Boolean = false,
    showNextMonthDays: Boolean = false,
    todayEpochDay: Long = SolarHijriPlatform.currentEpochDay(),
    showHeader: Boolean = true,
    showWeekdayLabels: Boolean = true,
    fixedSixRows: Boolean = false,
    todayBorderColor: Color? = null,
    todayDescription: String? = null,
    markers: Map<Long, List<CalendarDayMarker>> = emptyMap(),
    weekNumber: ((Long) -> String?)? = null,
    isDateEnabled: (Long) -> Boolean = { true },
) {
    SolarHijriNativeCalendarMonth(
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
 * Controlled Solar Hijri month with independently visible adjacent dates, blank by default.
 * Adjacent dates use the same enabled predicate; beyond the first and last supported day they
 * are blank and disabled. [secondaryLabel] and [secondaryAccessibilityLabel] display an optional
 * second calendar label below each day cell. Clicking selects/calls back without navigation.
 * Locale does not set layout direction.
 */
@Composable
fun SolarHijriNativeCalendarMonth(
    month: SolarHijriDate,
    selection: CalendarSelection,
    onDateClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    firstDayOfWeek: Int = SolarHijriNativeDefaultFirstDayOfWeek,
    showPreviousMonthDays: Boolean = false,
    showNextMonthDays: Boolean = false,
    todayEpochDay: Long = SolarHijriPlatform.currentEpochDay(),
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
    dayContent: @Composable (CalendarDayCell, Boolean, Boolean) -> Unit = { day, _, _ -> Text(day.label) },
) {
    val data = solarHijriNativeMonthData(
        month = month,
        firstDayOfWeek = firstDayOfWeek,
        todayEpochDay = todayEpochDay,
        isDateEnabled = isDateEnabled,
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
        adjacentDayProvider = { solarHijriNativeDayCellForEpoch(it, todayEpochDay, isDateEnabled) },
    )
}
