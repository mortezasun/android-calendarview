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

/** Prepared, localized month data. Treat the lists as read-only snapshots. */
data class GregorianMonthData(
    val title: String,
    val weekdayLabels: List<String>,
    val firstDayOffset: Int,
    val days: List<CalendarDayCell>,
)

/**
 * Builds month data without composition, views or Android framework calls.
 * Absolute-day keys match LocalDate.toEpochDay(), including across year boundaries.
 * The enabled predicate controls taps only; range selection may still include disabled
 * interior dates. Restricting entire ranges is a separate policy, not implemented here.
 */
fun gregorianMonthData(
    month: YearMonth,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = WeekFields.of(locale).firstDayOfWeek,
    isDateEnabled: (LocalDate) -> Boolean = { true },
): GregorianMonthData {
    val numberFormat = NumberFormat.getIntegerInstance(locale).apply { isGroupingUsed = false }
    val dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)
    return GregorianMonthData(
        title = month.format(DateTimeFormatter.ofPattern("LLLL uuuu", locale)),
        weekdayLabels = List(7) { index ->
            firstDayOfWeek.plus(index.toLong()).getDisplayName(TextStyle.SHORT_STANDALONE, locale)
        },
        firstDayOffset = Math.floorMod(month.atDay(1).dayOfWeek.value - firstDayOfWeek.value, 7),
        days = (1..month.lengthOfMonth()).map { day ->
            val date = month.atDay(day)
            CalendarDayCell(
                epochDay = date.toEpochDay(),
                label = numberFormat.format(day),
                accessibilityLabel = date.format(dateFormat),
                enabled = isDateEnabled(date),
            )
        },
    )
}

/**
 * Controlled native Gregorian month UI. The caller owns visible month and selection.
 * Use rememberCalendarSelectionState in the shared core for saveable selection ownership.
 * For custom header/weekday/day slots, feed gregorianMonthData to CalendarMonthGrid directly.
 * Locale and first weekday affect labels/order, not layout direction; supply RTL via
 * the host's Compose layout direction when appropriate. No navigation controls are added.
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
) {
    // Rebuild from current inputs, including changes in the enabled predicate's captured state.
    val data = gregorianMonthData(month, locale, firstDayOfWeek, isDateEnabled)
    CalendarMonthGrid(
        title = data.title,
        weekdayLabels = data.weekdayLabels,
        firstDayOffset = data.firstDayOffset,
        days = data.days,
        selection = selection,
        onDayClick = { onDateClick(LocalDate.ofEpochDay(it)) },
        modifier = modifier,
    )
}
