package com.develotter.calendarview.calendars.solarHijri.compose

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriEngine
import com.develotter.calendarview.calendars.solarHijriNative.compose.SolarHijriNativeCalendarMonth
import com.develotter.calendarview.calendars.solarHijriNative.compose.solarHijriNativeAccessibilityLabel
import com.develotter.calendarview.calendars.solarHijriNative.compose.solarHijriNativeDayCell
import com.develotter.calendarview.calendars.solarHijriNative.compose.solarHijriNativeMonthData
import com.develotter.calendarview.calendars.solarHijriNative.compose.solarHijriNativeMonthTitle
import com.develotter.calendarview.calendars.solarHijriNative.compose.solarHijriNativeSecondaryLabel
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarDayMarker
import com.develotter.calendarview.compose.CalendarMonthData
import com.develotter.calendarview.locale.CalendarLocale
import com.develotter.calendarview.selection.CalendarSelection

/** Saturday-first is configurable. */
private const val DEFAULT_FIRST_DAY_OF_WEEK = 6

/** Localized month snapshot; treat lists as read-only. */
@Immutable
data class SolarHijriMonthData(
    override val title: String,
    override val weekdayLabels: List<String>,
    override val firstDayOffset: Int,
    override val days: List<CalendarDayCell>,
) : CalendarMonthData

/**
 * Localized Solar Hijri month-year title, for example `Farvardin 1404`.
 *
 * [locale] selects the library's own label tables; `null` keeps the platform default locale.
 */
fun solarHijriMonthTitle(month: SolarHijriDate, locale: CalendarLocale? = null): String =
    solarHijriNativeMonthTitle(month, locale)

/**
 * Compact Solar Hijri secondary label for another primary calendar grid, or `null` if [epochDay]
 * falls outside the supported Solar Hijri range.
 */
fun solarHijriSecondaryLabel(epochDay: Long, locale: CalendarLocale? = null): String? =
    solarHijriNativeSecondaryLabel(epochDay, locale)

/**
 * Localized full Solar Hijri date description suitable for screen readers and date inspectors, or
 * `null` if [epochDay] falls outside the supported Solar Hijri range.
 */
fun solarHijriAccessibilityLabel(epochDay: Long, locale: CalendarLocale? = null): String? =
    solarHijriNativeAccessibilityLabel(epochDay, locale)

/** Formats one Solar Hijri date; [isDateEnabled] receives the absolute epoch day. */
fun solarHijriDayCell(
    epochDay: Long,
    todayEpochDay: Long = SolarHijriDate.now().toEpochDay(),
    locale: CalendarLocale? = null,
    isDateEnabled: (Long) -> Boolean = { true },
): CalendarDayCell =
    if (epochDay in SolarHijriEngine.MIN_EPOCH_DAY..SolarHijriEngine.MAX_EPOCH_DAY) {
        solarHijriNativeDayCell(
            epochDay = epochDay,
            todayEpochDay = todayEpochDay,
            locale = locale,
            isDateEnabled = isDateEnabled,
        )
    } else {
        CalendarDayCell(epochDay = epochDay, label = "", accessibilityLabel = "", enabled = false, isToday = false)
    }

/** Saturday-first is configurable; non-Persian month names remain English transliterations. */
fun solarHijriMonthData(
    month: SolarHijriDate,
    firstDayOfWeek: Int = DEFAULT_FIRST_DAY_OF_WEEK,
    todayEpochDay: Long = SolarHijriDate.now().toEpochDay(),
    locale: CalendarLocale? = null,
    isDateEnabled: (Long) -> Boolean = { true },
): SolarHijriMonthData {
    val native = solarHijriNativeMonthData(
        month = month,
        firstDayOfWeek = firstDayOfWeek,
        todayEpochDay = todayEpochDay,
        locale = locale,
        isDateEnabled = isDateEnabled,
    )
    return SolarHijriMonthData(
        title = native.title,
        weekdayLabels = native.weekdayLabels,
        firstDayOffset = native.firstDayOffset,
        days = native.days,
    )
}

/** Binary-compatible overload for callers compiled against 3.0.0. */
@Deprecated(
    message = "Binary compatibility overload for callers compiled against 3.0.0.",
    level = DeprecationLevel.HIDDEN,
)
@Composable
fun SolarHijriCalendarMonth(
    month: SolarHijriDate,
    selection: CalendarSelection,
    onDateClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    firstDayOfWeek: Int = DEFAULT_FIRST_DAY_OF_WEEK,
    showPreviousMonthDays: Boolean = false,
    showNextMonthDays: Boolean = false,
    todayEpochDay: Long = SolarHijriDate.now().toEpochDay(),
    showHeader: Boolean = true,
    showWeekdayLabels: Boolean = true,
    fixedSixRows: Boolean = false,
    todayBorderColor: Color? = null,
    todayDescription: String? = null,
    markers: Map<Long, List<CalendarDayMarker>> = emptyMap(),
    weekNumber: ((Long) -> String?)? = null,
    isDateEnabled: (Long) -> Boolean = { true },
) {
    SolarHijriCalendarMonth(
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
 * Controlled month UI with independently visible adjacent dates, blank by default.
 * Adjacent dates use checked Solar Hijri conversion and the same enabled predicate. [secondaryLabel]
 * and [secondaryAccessibilityLabel] display an optional second calendar label below each day cell.
 * Clicking selects/calls back without navigation. Locale does not set layout direction.
 */
@Composable
fun SolarHijriCalendarMonth(
    month: SolarHijriDate,
    selection: CalendarSelection,
    onDateClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    firstDayOfWeek: Int = DEFAULT_FIRST_DAY_OF_WEEK,
    showPreviousMonthDays: Boolean = false,
    showNextMonthDays: Boolean = false,
    todayEpochDay: Long = SolarHijriDate.now().toEpochDay(),
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
        secondaryLabel = secondaryLabel,
        secondaryAccessibilityLabel = secondaryAccessibilityLabel,
        locale = locale,
        dayContent = dayContent,
    )
}
