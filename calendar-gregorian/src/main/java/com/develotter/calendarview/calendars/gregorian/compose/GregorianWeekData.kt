package com.develotter.calendarview.calendars.gregorian.compose

import androidx.compose.runtime.Immutable
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarWeekData
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

/** Localized Gregorian seven-day snapshot; treat lists as read-only. */
@Immutable
data class GregorianWeekData(
    override val title: String,
    override val weekdayLabels: List<String>,
    override val days: List<CalendarDayCell>,
    override val weekNumber: String? = null,
) : CalendarWeekData

/**
 * Builds a Gregorian week around [date], aligned to [firstDayOfWeek].
 * The week number uses a locale-independent week-based year with one minimal leading day;
 * callers that need ISO or another convention can pass their own week-number callback to the
 * grid instead. Date-range errors propagate.
 */
fun gregorianWeekData(
    date: LocalDate,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = WeekFields.of(locale).firstDayOfWeek,
    today: LocalDate = LocalDate.now(),
    isDateEnabled: (LocalDate) -> Boolean = { true },
): GregorianWeekData {
    val start = date.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
    val end = start.plusDays(6)
    val numbers = NumberFormat.getIntegerInstance(locale).apply { isGroupingUsed = false }
    val weekFields = WeekFields.of(firstDayOfWeek, 1)
    return GregorianWeekData(
        title = "${start.format(DateTimeFormatter.ofPattern("d MMM", locale))} – " +
            end.format(DateTimeFormatter.ofPattern("d MMM y", locale)),
        weekdayLabels = List(7) { offset ->
            firstDayOfWeek.plus(offset.toLong()).getDisplayName(TextStyle.SHORT_STANDALONE, locale)
        },
        days = List(7) { offset ->
            gregorianDayCell(
                date = start.plusDays(offset.toLong()),
                locale = locale,
                today = today,
                isDateEnabled = isDateEnabled,
            )
        },
        weekNumber = numbers.format(start.get(weekFields.weekOfWeekBasedYear())),
    )
}
