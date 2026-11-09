package com.develotter.calendarview.calendars.gregorian.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarMonthGrid
import com.develotter.calendarview.selection.CalendarSelection
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

/** Localized month snapshot; treat lists as read-only. */
data class GregorianMonthData(val title: String, val weekdayLabels: List<String>, val firstDayOffset: Int, val days: List<CalendarDayCell>)

/** Formats one Gregorian date for either current or adjacent month rendering. */
fun gregorianDayCell(
    date: LocalDate,
    locale: Locale = Locale.getDefault(),
    isDateEnabled: (LocalDate) -> Boolean = { true },
): CalendarDayCell {
    val numbers = NumberFormat.getIntegerInstance(locale).apply { isGroupingUsed = false }
    return CalendarDayCell(
        epochDay = date.toEpochDay(), label = numbers.format(date.dayOfMonth),
        accessibilityLabel = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)),
        enabled = isDateEnabled(date),
    )
}

/** Pure month data. Disabled dates reject taps but may lie inside a selected range. */
fun gregorianMonthData(
    month: YearMonth,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = WeekFields.of(locale).firstDayOfWeek,
    isDateEnabled: (LocalDate) -> Boolean = { true },
): GregorianMonthData = GregorianMonthData(
    title = month.format(DateTimeFormatter.ofPattern("LLLL uuuu", locale)),
    weekdayLabels = List(7) { firstDayOfWeek.plus(it.toLong()).getDisplayName(TextStyle.SHORT_STANDALONE, locale) },
    firstDayOffset = Math.floorMod(month.atDay(1).dayOfWeek.value - firstDayOfWeek.value, 7),
    days = (1..month.lengthOfMonth()).map { gregorianDayCell(month.atDay(it), locale, isDateEnabled) },
)

/**
 * Controlled month UI; caller owns selection and navigation. Adjacent visibility is
 * independent on each side, disabled by default. Adjacent clicks do not navigate.
 * Locale does not set layout direction. For custom slots use month/day builders with
 * CalendarMonthGrid. Date-range errors propagate rather than displaying incorrect dates.
 */
@Composable
fun GregorianCalendarMonth(
    month: YearMonth,
    selection: CalendarSelection,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = WeekFields.of(locale).firstDayOfWeek,
    isDateEnabled: (LocalDate) -> Boolean = { true },
    showPreviousMonthDays: Boolean = false,
    showNextMonthDays: Boolean = false,
) {
    val data = gregorianMonthData(month, locale, firstDayOfWeek, isDateEnabled)
    CalendarMonthGrid(
        title = data.title, weekdayLabels = data.weekdayLabels, firstDayOffset = data.firstDayOffset,
        days = data.days, selection = selection, modifier = modifier,
        onDayClick = { onDateClick(LocalDate.ofEpochDay(it)) },
        showPreviousMonthDays = showPreviousMonthDays, showNextMonthDays = showNextMonthDays,
        adjacentDayProvider = { gregorianDayCell(LocalDate.ofEpochDay(it), locale, isDateEnabled) },
    )
}
