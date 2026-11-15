package com.develotter.calendarview.calendars.hijri.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.develotter.calendarview.calendars.hijri.HijriDate
import com.develotter.calendarview.calendars.hijri.HijriDateProvider
import com.develotter.calendarview.calendars.hijri.HijriPlatform
import com.develotter.calendarview.calendars.hijri.UmmAlQuraHijriDateProvider
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarDayMarker
import com.develotter.calendarview.compose.CalendarMonthData
import com.develotter.calendarview.compose.CalendarMonthGrid
import com.develotter.calendarview.selection.CalendarSelection

/** Localized month snapshot; treat lists as read-only. */
@Immutable
data class HijriMonthData(
    override val title: String,
    override val weekdayLabels: List<String>,
    override val firstDayOffset: Int,
    override val days: List<CalendarDayCell>,
) : CalendarMonthData

/** Formats a Hijri day with its own chronology, including dates in adjacent months. */
fun hijriDayCell(
    epochDay: Long,
    dateProvider: HijriDateProvider = UmmAlQuraHijriDateProvider,
    todayEpochDay: Long = HijriPlatform.currentEpochDay(),
    isDateEnabled: (Long) -> Boolean = { true },
): CalendarDayCell = hijriDayCellForEpoch(
    date = dateProvider.dateForEpochDay(epochDay),
    epochDay = epochDay,
    todayEpochDay = todayEpochDay,
    isDateEnabled = isDateEnabled,
)

/** Variant for providers whose displayed date differs from the canonical epoch day. */
internal fun hijriDayCellForEpoch(
    date: HijriDate,
    epochDay: Long,
    todayEpochDay: Long,
    isDateEnabled: (Long) -> Boolean,
): CalendarDayCell = CalendarDayCell(
    epochDay = epochDay,
    label = HijriPlatform.formatDayNumber(date.dayOfMonth),
    accessibilityLabel = HijriPlatform.formatFullDate(HijriPlatform.toEpochDay(date)),
    enabled = isDateEnabled(epochDay),
    isToday = epochDay == todayEpochDay,
)

/**
 * Builds a Hijri month with the platform Umm al-Qura chronology and Saturday-first by
 * default. Pass another [dateProvider] for a local date method; the month length and the
 * displayed day labels follow that provider's uniform shift.
 */
fun hijriMonthData(
    month: HijriDate,
    firstDayOfWeek: Int = HijriDefaultFirstDayOfWeek,
    todayEpochDay: Long = HijriPlatform.currentEpochDay(),
    dateProvider: HijriDateProvider = UmmAlQuraHijriDateProvider,
    isDateEnabled: (Long) -> Boolean = { true },
): HijriMonthData {
    val first = month.copy(dayOfMonth = 1)
    val firstEpochDay = dateProvider.epochDayFor(first)
    return HijriMonthData(
        title = HijriPlatform.formatMonthYear(first.year, first.month),
        weekdayLabels = hijriWeekdayLabels(firstDayOfWeek),
        // The displayed Hijri date may be offset from its absolute timeline weekday.
        firstDayOffset = hijriDayOfWeekOffset(firstEpochDay, firstDayOfWeek),
        days = List(HijriPlatform.lengthOfMonth(first.year, first.month)) { offset ->
            hijriDayCellForEpoch(
                date = dateProvider.dateForEpochDay(firstEpochDay + offset),
                epochDay = firstEpochDay + offset,
                todayEpochDay = todayEpochDay,
                isDateEnabled = isDateEnabled,
            )
        },
    )
}

/**
 * Controlled Hijri month UI using [dateProvider]. Adjacent cells retain the provider's
 * chronology and use the same enabled predicate and click callback. [onDateClick] receives the
 * absolute epoch day of the clicked cell. Visibility is independent and defaults to blank
 * padding. Engine date-range errors propagate. Locale does not set layout direction.
 */
@Composable
fun HijriCalendarMonth(
    month: HijriDate,
    selection: CalendarSelection,
    onDateClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    firstDayOfWeek: Int = HijriDefaultFirstDayOfWeek,
    showPreviousMonthDays: Boolean = false,
    showNextMonthDays: Boolean = false,
    todayEpochDay: Long = HijriPlatform.currentEpochDay(),
    showHeader: Boolean = true,
    showWeekdayLabels: Boolean = true,
    fixedSixRows: Boolean = false,
    todayBorderColor: Color? = null,
    todayDescription: String? = null,
    markers: Map<Long, List<CalendarDayMarker>> = emptyMap(),
    weekNumber: ((Long) -> String?)? = null,
    dateProvider: HijriDateProvider = UmmAlQuraHijriDateProvider,
    isDateEnabled: (Long) -> Boolean = { true },
) {
    val data = hijriMonthData(
        month = month,
        firstDayOfWeek = firstDayOfWeek,
        todayEpochDay = todayEpochDay,
        dateProvider = dateProvider,
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
            hijriDayCellForEpoch(
                date = dateProvider.dateForEpochDay(adjacentEpochDay),
                epochDay = adjacentEpochDay,
                todayEpochDay = todayEpochDay,
                isDateEnabled = isDateEnabled,
            )
        },
    )
}
