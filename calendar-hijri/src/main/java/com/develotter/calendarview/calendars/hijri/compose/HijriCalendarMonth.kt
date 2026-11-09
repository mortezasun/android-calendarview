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

/** Localized month snapshot; treat lists as read-only. */
data class HijriMonthData(val title: String, val weekdayLabels: List<String>, val firstDayOffset: Int, val days: List<CalendarDayCell>)

/** Formats a Hijri day with its own chronology, including dates in adjacent months. */
fun hijriDayCell(
    date: HijrahDate,
    locale: Locale = Locale.getDefault(),
    today: HijrahDate = HijrahDate.now(),
    isDateEnabled: (HijrahDate) -> Boolean = { true },
): CalendarDayCell {
    val numbers = NumberFormat.getIntegerInstance(locale).apply { isGroupingUsed = false }
    val format = DateTimeFormatter.ofPattern("EEEE d MMMM y G", locale).withChronology(date.chronology)
    return CalendarDayCell(
        epochDay = date.toEpochDay(), label = numbers.format(date.get(ChronoField.DAY_OF_MONTH)),
        accessibilityLabel = date.format(format), enabled = isDateEnabled(date),
        isToday = date.toEpochDay() == today.toEpochDay(),
    )
}

/** Platform Hijrah chronology data, not local moon sighting. Saturday-first is configurable. */
fun hijriMonthData(
    month: HijrahDate,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.SATURDAY,
    today: HijrahDate = HijrahDate.now(),
    isDateEnabled: (HijrahDate) -> Boolean = { true },
): HijriMonthData {
    val first = month.with(ChronoField.DAY_OF_MONTH, 1)
    return HijriMonthData(
        title = first.format(DateTimeFormatter.ofPattern("MMMM y G", locale).withChronology(first.chronology)),
        weekdayLabels = List(7) { firstDayOfWeek.plus(it.toLong()).getDisplayName(TextStyle.SHORT_STANDALONE, locale) },
        firstDayOffset = Math.floorMod(first.get(ChronoField.DAY_OF_WEEK) - firstDayOfWeek.value, 7),
        days = (0 until first.lengthOfMonth()).map { hijriDayCell(date = first.plus(it.toLong(), ChronoUnit.DAYS), locale = locale, today = today, isDateEnabled = isDateEnabled) },
    )
}

/**
 * Controlled Hijri month UI. Adjacent cells retain input chronology and use the same
 * enabled predicate and click callback. Visibility is independent and defaults to blank
 * padding. Engine date-range errors propagate. Locale does not set layout direction.
 */
@Composable
fun HijriCalendarMonth(
    month: HijrahDate,
    selection: CalendarSelection,
    onDateClick: (HijrahDate) -> Unit,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.SATURDAY,
    showPreviousMonthDays: Boolean = false,
    showNextMonthDays: Boolean = false,
    today: HijrahDate = HijrahDate.now(),
    showHeader: Boolean = true,
    showWeekdayLabels: Boolean = true,
    fixedSixRows: Boolean = false,
    isDateEnabled: (HijrahDate) -> Boolean = { true },
) {
    val data = hijriMonthData(
        month = month, locale = locale, firstDayOfWeek = firstDayOfWeek, today = today, isDateEnabled = isDateEnabled,
    )
    CalendarMonthGrid(
        title = data.title, weekdayLabels = data.weekdayLabels, firstDayOffset = data.firstDayOffset,
        days = data.days, selection = selection, modifier = modifier,
        onDayClick = { onDateClick(month.chronology.dateEpochDay(it)) },
        showPreviousMonthDays = showPreviousMonthDays, showNextMonthDays = showNextMonthDays,
        showHeader = showHeader, showWeekdayLabels = showWeekdayLabels, fixedSixRows = fixedSixRows,
        adjacentDayProvider = { hijriDayCell(date = month.chronology.dateEpochDay(it), locale = locale, today = today, isDateEnabled = isDateEnabled) },
    )
}
