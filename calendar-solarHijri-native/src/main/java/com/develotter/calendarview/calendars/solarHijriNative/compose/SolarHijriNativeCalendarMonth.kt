package com.develotter.calendarview.calendars.solarHijriNative.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriEngine
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriLocale
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarDayMarker
import com.develotter.calendarview.compose.CalendarMonthData
import com.develotter.calendarview.compose.CalendarMonthGrid
import com.develotter.calendarview.selection.CalendarSelection
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** Localized month snapshot; treat lists as read-only. */
@Immutable
data class SolarHijriNativeMonthData(
    override val title: String,
    override val weekdayLabels: List<String>,
    override val firstDayOffset: Int,
    override val days: List<CalendarDayCell>,
) : CalendarMonthData

/** Formats one Solar Hijri date; [isDateEnabled] receives the immutable date itself. */
fun solarHijriNativeDayCell(
    date: SolarHijriDate,
    locale: Locale = Locale.getDefault(),
    today: SolarHijriDate = SolarHijriDate.now(),
    isDateEnabled: (SolarHijriDate) -> Boolean = { true },
): CalendarDayCell {
    val weekday = date.toLocalDate().dayOfWeek.getDisplayName(TextStyle.FULL, locale)
    val day = SolarHijriLocale.formatNumber(date.day, locale)
    val monthName = SolarHijriLocale.monthName(date.month, locale)
    return CalendarDayCell(
        epochDay = date.toEpochDay(),
        label = day,
        accessibilityLabel = "$weekday, $day $monthName ${SolarHijriLocale.formatNumber(date.year, locale)}",
        enabled = isDateEnabled(date),
        isToday = date == today,
    )
}

/**
 * Cell for any epoch day, including days beyond the supported years. The grid asks for adjacent
 * days of the first and last supported month; those get a blank, disabled cell instead of an
 * exception.
 */
internal fun solarHijriNativeDayCellForEpoch(
    epochDay: Long,
    locale: Locale,
    today: SolarHijriDate,
    isDateEnabled: (SolarHijriDate) -> Boolean,
): CalendarDayCell =
    if (epochDay in SolarHijriEngine.MIN_EPOCH_DAY..SolarHijriEngine.MAX_EPOCH_DAY) {
        solarHijriNativeDayCell(SolarHijriDate.fromEpochDay(epochDay), locale, today, isDateEnabled)
    } else {
        CalendarDayCell(epochDay = epochDay, label = "", accessibilityLabel = "", enabled = false, isToday = false)
    }

/** Saturday-first is configurable; non-Persian month names are English transliterations. */
fun solarHijriNativeMonthData(
    month: SolarHijriDate,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.SATURDAY,
    today: SolarHijriDate = SolarHijriDate.now(),
    isDateEnabled: (SolarHijriDate) -> Boolean = { true },
): SolarHijriNativeMonthData {
    val first = month.withDayOfMonth(1)
    val epoch = first.toEpochDay()
    return SolarHijriNativeMonthData(
        title = SolarHijriLocale.formatMonthYear(first.year, first.month, locale),
        weekdayLabels = List(7) { firstDayOfWeek.plus(it.toLong()).getDisplayName(TextStyle.SHORT_STANDALONE, locale) },
        firstDayOffset = Math.floorMod(LocalDate.ofEpochDay(epoch).dayOfWeek.value - firstDayOfWeek.value, 7),
        days = (1..first.lengthOfMonth()).map { day ->
            solarHijriNativeDayCell(first.withDayOfMonth(day), locale, today, isDateEnabled)
        },
    )
}

/**
 * Controlled Solar Hijri month with independently visible adjacent dates, blank by default.
 * Adjacent dates use the same enabled predicate; beyond the first and last supported day they
 * are blank and disabled. Clicking selects/calls back without navigation. Locale does not set
 * layout direction.
 */
@Composable
fun SolarHijriNativeCalendarMonth(
    month: SolarHijriDate,
    selection: CalendarSelection,
    onDateClick: (SolarHijriDate) -> Unit,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.SATURDAY,
    showPreviousMonthDays: Boolean = false,
    showNextMonthDays: Boolean = false,
    today: SolarHijriDate = SolarHijriDate.now(),
    showHeader: Boolean = true,
    showWeekdayLabels: Boolean = true,
    fixedSixRows: Boolean = false,
    todayBorderColor: Color? = null,
    todayDescription: String? = null,
    markers: Map<Long, List<CalendarDayMarker>> = emptyMap(),
    weekNumber: ((Long) -> String?)? = null,
    isDateEnabled: (SolarHijriDate) -> Boolean = { true },
) {
    val data = solarHijriNativeMonthData(
        month = month, locale = locale, firstDayOfWeek = firstDayOfWeek, today = today, isDateEnabled = isDateEnabled,
    )
    CalendarMonthGrid(
        title = data.title, weekdayLabels = data.weekdayLabels, firstDayOffset = data.firstDayOffset,
        days = data.days, selection = selection, modifier = modifier,
        onDayClick = {
            if (it in SolarHijriEngine.MIN_EPOCH_DAY..SolarHijriEngine.MAX_EPOCH_DAY) {
                onDateClick(SolarHijriDate.fromEpochDay(it))
            }
        },
        showPreviousMonthDays = showPreviousMonthDays, showNextMonthDays = showNextMonthDays,
        showHeader = showHeader, showWeekdayLabels = showWeekdayLabels, fixedSixRows = fixedSixRows,
        todayBorderColor = todayBorderColor,
        todayDescription = todayDescription,
        markers = markers,
        weekNumber = weekNumber,
        adjacentDayProvider = { solarHijriNativeDayCellForEpoch(it, locale, today, isDateEnabled) },
    )
}
