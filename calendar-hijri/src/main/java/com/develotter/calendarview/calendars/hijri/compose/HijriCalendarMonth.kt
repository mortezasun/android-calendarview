package com.develotter.calendarview.calendars.hijri.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarMonthGrid
import com.develotter.calendarview.selection.CalendarSelection
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.Locale

/** Localized month snapshot; callers must treat lists as read-only. */
data class HijriMonthData(
    val title: String,
    val weekdayLabels: List<String>,
    val firstDayOffset: Int,
    val days: List<CalendarDayCell>,
)

/**
 * Builds the month containing [month], preserving its Hijrah chronology.
 * Saturday-first matches the existing HijriStatus default. Absolute-day keys are
 * compatible with the shared selection engine and Gregorian LocalDate epoch days.
 * Supported years and conversion results follow the platform's Hijrah chronology data;
 * this is not a moon-sighting calendar. Disabled dates only reject direct taps.
 */
fun hijriMonthData(
    month: HijrahDate,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.SATURDAY,
    isDateEnabled: (HijrahDate) -> Boolean = { true },
): HijriMonthData {
    val first = month.with(ChronoField.DAY_OF_MONTH, 1)
    val numbers = NumberFormat.getIntegerInstance(locale).apply { isGroupingUsed = false }
    val titleFormat = DateTimeFormatter.ofPattern("MMMM y G", locale).withChronology(first.chronology)
    val fullFormat = DateTimeFormatter.ofPattern("EEEE d MMMM y G", locale).withChronology(first.chronology)
    return HijriMonthData(
        title = first.format(titleFormat),
        weekdayLabels = List(7) {
            firstDayOfWeek.plus(it.toLong()).getDisplayName(TextStyle.SHORT_STANDALONE, locale)
        },
        firstDayOffset = Math.floorMod(first.get(ChronoField.DAY_OF_WEEK) - firstDayOfWeek.value, 7),
        days = (1..first.lengthOfMonth()).map { day ->
            val date = first.plus((day - 1).toLong(), ChronoUnit.DAYS)
            CalendarDayCell(
                epochDay = date.toEpochDay(),
                label = numbers.format(day),
                accessibilityLabel = date.format(fullFormat),
                enabled = isDateEnabled(date),
            )
        },
    )
}

/**
 * Controlled native Hijri month UI: the caller owns the month and selection.
 * Any day in the target month is accepted. Click callbacks retain its chronology.
 * For custom content slots, supply hijriMonthData to CalendarMonthGrid directly.
 * Locale controls labels, not layout direction; RTL is supplied by the Compose host.
 */
@Composable
fun HijriCalendarMonth(
    month: HijrahDate,
    selection: CalendarSelection,
    onDateClick: (HijrahDate) -> Unit,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.SATURDAY,
    isDateEnabled: (HijrahDate) -> Boolean = { true },
) {
    val data = hijriMonthData(month, locale, firstDayOfWeek, isDateEnabled)
    CalendarMonthGrid(
        title = data.title,
        weekdayLabels = data.weekdayLabels,
        firstDayOffset = data.firstDayOffset,
        days = data.days,
        selection = selection,
        onDayClick = { onDateClick(month.chronology.dateEpochDay(it)) },
        modifier = modifier,
    )
}
