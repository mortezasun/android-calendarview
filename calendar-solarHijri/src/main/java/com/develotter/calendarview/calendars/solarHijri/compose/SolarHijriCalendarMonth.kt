package com.develotter.calendarview.calendars.solarHijri.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.develotter.calendarview.calendars.solarHijri.fromSolarHijriToLocalDate
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarDayMarker
import com.develotter.calendarview.compose.CalendarMonthData
import com.develotter.calendarview.compose.CalendarMonthGrid
import com.develotter.calendarview.selection.CalendarSelection
import ir.huri.jcal.JalaliCalendar
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** Localized month snapshot; treat lists as read-only. */
@Immutable
data class SolarHijriMonthData(
    override val title: String,
    override val weekdayLabels: List<String>,
    override val firstDayOffset: Int,
    override val days: List<CalendarDayCell>,
) : CalendarMonthData

/** Checked existing conversion to the shared timeline; no broader year range is promised. */
fun solarHijriEpochDay(date: JalaliCalendar): Long {
    val gregorian = date.fromSolarHijriToLocalDate()
    val restored = JalaliCalendar(gregorian)
    require(restored.year == date.year && restored.month == date.month && restored.day == date.day) {
        "Solar Hijri conversion does not round-trip for ${date.year}/${date.month}/${date.day}"
    }
    return gregorian.toEpochDay()
}

private fun solarMonthName(date: JalaliCalendar, locale: Locale): String =
    if (locale.language == "fa") date.monthString else listOf(
        "Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar",
        "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand",
    )[date.month - 1]

/** Shared month caption; the state holder and the month data use the same formatting. */
internal fun solarHijriMonthTitle(date: JalaliCalendar, locale: Locale): String {
    val numbers = NumberFormat.getIntegerInstance(locale).apply { isGroupingUsed = false }
    return "${solarMonthName(date, locale)} ${numbers.format(date.year)}"
}

/** Formats one checked Jalali date; consumer predicates receive a separate mutable instance. */
fun solarHijriDayCell(
    date: JalaliCalendar,
    locale: Locale = Locale.getDefault(),
    today: JalaliCalendar = JalaliCalendar(LocalDate.now()),
    isDateEnabled: (JalaliCalendar) -> Boolean = { true },
): CalendarDayCell {
    val epoch = solarHijriEpochDay(date)
    val numbers = NumberFormat.getIntegerInstance(locale).apply { isGroupingUsed = false }
    val weekday = LocalDate.ofEpochDay(epoch).dayOfWeek.getDisplayName(TextStyle.FULL, locale)
    val isToday = date.year == today.year && date.month == today.month && date.day == today.day
    return CalendarDayCell(
        epochDay = epoch, label = numbers.format(date.day),
        accessibilityLabel = "$weekday, ${numbers.format(date.day)} ${solarMonthName(date, locale)} ${numbers.format(date.year)}",
        enabled = isDateEnabled(JalaliCalendar(date.year, date.month, date.day)),
        isToday = isToday,
    )
}

/** Saturday-first is configurable; non-Persian month names remain English transliterations. */
fun solarHijriMonthData(
    month: JalaliCalendar,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.SATURDAY,
    today: JalaliCalendar = JalaliCalendar(LocalDate.now()),
    isDateEnabled: (JalaliCalendar) -> Boolean = { true },
): SolarHijriMonthData {
    val first = JalaliCalendar(month.year, month.month, 1)
    val epoch = solarHijriEpochDay(first)
    return SolarHijriMonthData(
        title = solarHijriMonthTitle(first, locale),
        weekdayLabels = List(7) { firstDayOfWeek.plus(it.toLong()).getDisplayName(TextStyle.SHORT_STANDALONE, locale) },
        firstDayOffset = Math.floorMod(LocalDate.ofEpochDay(epoch).dayOfWeek.value - firstDayOfWeek.value, 7),
        days = (1..first.monthLength).map { day ->
            val cell = solarHijriDayCell(date = JalaliCalendar(first.year, first.month, day), locale = locale, today = today, isDateEnabled = isDateEnabled)
            require(cell.epochDay == epoch + day - 1) { "Non-consecutive Solar Hijri month conversion" }
            cell
        },
    )
}

/**
 * Controlled month UI with independently visible adjacent dates, blank by default.
 * Adjacent dates use checked Jalali conversion and the same enabled predicate. Clicking
 * selects/calls back without navigation. Locale does not set layout direction.
 */
/** Binary-compatible overload for callers compiled against 2.0.1. */
@Deprecated(
    message = "Binary compatibility overload for callers compiled against 2.0.1.",
    level = DeprecationLevel.HIDDEN,
)
@Composable
fun SolarHijriCalendarMonth(
    month: JalaliCalendar,
    selection: CalendarSelection,
    onDateClick: (JalaliCalendar) -> Unit,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.SATURDAY,
    showPreviousMonthDays: Boolean = false,
    showNextMonthDays: Boolean = false,
    today: JalaliCalendar = JalaliCalendar(LocalDate.now()),
    showHeader: Boolean = true,
    showWeekdayLabels: Boolean = true,
    fixedSixRows: Boolean = false,
    todayBorderColor: Color = MaterialTheme.colorScheme.tertiary,
    isDateEnabled: (JalaliCalendar) -> Boolean = { true },
) {
    SolarHijriCalendarMonth(
        month = month,
        selection = selection,
        onDateClick = onDateClick,
        modifier = modifier,
        locale = locale,
        firstDayOfWeek = firstDayOfWeek,
        showPreviousMonthDays = showPreviousMonthDays,
        showNextMonthDays = showNextMonthDays,
        today = today,
        showHeader = showHeader,
        showWeekdayLabels = showWeekdayLabels,
        fixedSixRows = fixedSixRows,
        todayBorderColor = todayBorderColor,
        todayDescription = null,
        markers = emptyMap(),
        weekNumber = null,
        isDateEnabled = isDateEnabled,
    )
}

@Composable
fun SolarHijriCalendarMonth(
    month: JalaliCalendar,
    selection: CalendarSelection,
    onDateClick: (JalaliCalendar) -> Unit,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.SATURDAY,
    showPreviousMonthDays: Boolean = false,
    showNextMonthDays: Boolean = false,
    today: JalaliCalendar = JalaliCalendar(LocalDate.now()),
    showHeader: Boolean = true,
    showWeekdayLabels: Boolean = true,
    fixedSixRows: Boolean = false,
    todayBorderColor: Color? = null,
    todayDescription: String? = null,
    markers: Map<Long, List<CalendarDayMarker>> = emptyMap(),
    weekNumber: ((Long) -> String?)? = null,
    isDateEnabled: (JalaliCalendar) -> Boolean = { true },
) {
    val data = solarHijriMonthData(
        month = month, locale = locale, firstDayOfWeek = firstDayOfWeek, today = today, isDateEnabled = isDateEnabled,
    )
    CalendarMonthGrid(
        title = data.title, weekdayLabels = data.weekdayLabels, firstDayOffset = data.firstDayOffset,
        days = data.days, selection = selection, modifier = modifier,
        onDayClick = { onDateClick(JalaliCalendar(LocalDate.ofEpochDay(it))) },
        showPreviousMonthDays = showPreviousMonthDays, showNextMonthDays = showNextMonthDays,
        showHeader = showHeader, showWeekdayLabels = showWeekdayLabels, fixedSixRows = fixedSixRows,
        todayBorderColor = todayBorderColor,
        todayDescription = todayDescription,
        markers = markers,
        weekNumber = weekNumber,
        adjacentDayProvider = { solarHijriDayCell(date = JalaliCalendar(LocalDate.ofEpochDay(it)), locale = locale, today = today, isDateEnabled = isDateEnabled) },
    )
}
