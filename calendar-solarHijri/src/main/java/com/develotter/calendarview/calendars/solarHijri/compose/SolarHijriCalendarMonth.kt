package com.develotter.calendarview.calendars.solarHijri.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.develotter.calendarview.calendars.solarHijri.fromSolarHijriToLocalDate
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarMonthGrid
import com.develotter.calendarview.selection.CalendarSelection
import ir.huri.jcal.JalaliCalendar
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** Localized month snapshot; lists must be treated as read-only. */
data class SolarHijriMonthData(
    val title: String,
    val weekdayLabels: List<String>,
    val firstDayOffset: Int,
    val days: List<CalendarDayCell>,
)

/**
 * Maps a Jalali date to the shared absolute-day timeline using the existing conversion.
 * A round-trip check rejects inconsistencies between that conversion and JalaliCalendar.
 * Supported dates follow the existing converter/library; no broader range is promised.
 */
fun solarHijriEpochDay(date: JalaliCalendar): Long {
    val gregorian = date.fromSolarHijriToLocalDate()
    val restored = JalaliCalendar(gregorian)
    require(restored.year == date.year && restored.month == date.month && restored.day == date.day) {
        "Solar Hijri conversion does not round-trip for ${date.year}/${date.month}/${date.day}"
    }
    return gregorian.toEpochDay()
}

/**
 * Builds the month containing [month] without mutating the supplied JalaliCalendar.
 * Saturday-first matches the existing module. Persian month names come from the library;
 * other locales use the existing project's English transliterations. Weekday names and
 * digits follow locale. Disabled dates reject taps, not selection of range interiors.
 */
fun solarHijriMonthData(
    month: JalaliCalendar,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.SATURDAY,
    isDateEnabled: (JalaliCalendar) -> Boolean = { true },
): SolarHijriMonthData {
    val first = JalaliCalendar(month.year, month.month, 1)
    val firstEpochDay = solarHijriEpochDay(first)
    val length = first.monthLength
    val numbers = NumberFormat.getIntegerInstance(locale).apply { isGroupingUsed = false }
    val englishMonths = listOf(
        "Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar",
        "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand",
    )
    val monthName = if (locale.language == "fa") first.monthString else englishMonths[first.month - 1]
    val yearLabel = numbers.format(first.year)
    return SolarHijriMonthData(
        title = "$monthName $yearLabel",
        weekdayLabels = List(7) {
            firstDayOfWeek.plus(it.toLong()).getDisplayName(TextStyle.SHORT_STANDALONE, locale)
        },
        firstDayOffset = Math.floorMod(LocalDate.ofEpochDay(firstEpochDay).dayOfWeek.value - firstDayOfWeek.value, 7),
        days = (1..length).map { day ->
            val date = JalaliCalendar(first.year, first.month, day)
            val epochDay = solarHijriEpochDay(date)
            require(epochDay == firstEpochDay + day - 1) { "Non-consecutive Solar Hijri month conversion" }
            val weekday = LocalDate.ofEpochDay(epochDay).dayOfWeek.getDisplayName(TextStyle.FULL, locale)
            CalendarDayCell(
                epochDay = epochDay,
                label = numbers.format(day),
                accessibilityLabel = "$weekday, ${numbers.format(day)} $monthName $yearLabel",
                // Use a separate mutable library instance for consumer predicates.
                enabled = isDateEnabled(JalaliCalendar(first.year, first.month, day)),
            )
        },
    )
}

/**
 * Controlled native Solar Hijri UI. The caller owns month, selection and navigation.
 * For custom content use solarHijriMonthData with CalendarMonthGrid directly. Locale
 * does not set Compose layout direction. Callers must not mutate input during rendering.
 */
@Composable
fun SolarHijriCalendarMonth(
    month: JalaliCalendar,
    selection: CalendarSelection,
    onDateClick: (JalaliCalendar) -> Unit,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.SATURDAY,
    isDateEnabled: (JalaliCalendar) -> Boolean = { true },
) {
    val data = solarHijriMonthData(month, locale, firstDayOfWeek, isDateEnabled)
    CalendarMonthGrid(
        title = data.title,
        weekdayLabels = data.weekdayLabels,
        firstDayOffset = data.firstDayOffset,
        days = data.days,
        selection = selection,
        onDayClick = { onDateClick(JalaliCalendar(LocalDate.ofEpochDay(it))) },
        modifier = modifier,
    )
}
