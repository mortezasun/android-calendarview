package com.develotter.calendarview.calendars.gregorian.compose

import androidx.compose.runtime.Immutable
import com.develotter.calendarview.calendars.gregorian.GregorianPlatformFormatter
import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarWeekData

/** Localized Gregorian seven-day snapshot; treat lists as read-only. */
@Immutable
data class GregorianWeekData(
    override val title: String,
    override val weekdayLabels: List<String>,
    override val days: List<CalendarDayCell>,
    override val weekNumber: String? = null,
) : CalendarWeekData

/**
 * Builds a Gregorian week around [dateEpochDay], aligned to [firstDayOfWeek] (ISO 1..7,
 * Monday first). The week number uses a locale-independent week-based year with one minimal
 * leading day; callers that need ISO or another convention can pass their own week-number
 * callback to the grid instead. Date-range errors propagate.
 */
fun gregorianWeekData(
    dateEpochDay: Long,
    firstDayOfWeek: Int = GregorianPlatformFormatter.defaultFirstDayOfWeek(),
    todayEpochDay: Long = GregorianPlatformFormatter.currentEpochDay(),
    isDateEnabled: (Long) -> Boolean = { true },
): GregorianWeekData {
    val start = startOfWeek(dateEpochDay, firstDayOfWeek)
    val end = start + 6
    return GregorianWeekData(
        title = "${GregorianPlatformFormatter.formatShortDayMonth(start)} – " +
            GregorianPlatformFormatter.formatShortDayMonthYear(end),
        weekdayLabels = gregorianWeekdayLabels(firstDayOfWeek),
        days = List(7) { offset ->
            gregorianDayCell(
                epochDay = start + offset,
                todayEpochDay = todayEpochDay,
                isDateEnabled = isDateEnabled,
            )
        },
        weekNumber = GregorianPlatformFormatter.formatDayNumber(
            weekOfWeekBasedYear(start, firstDayOfWeek),
        ),
    )
}

/** Seven localized short weekday labels in grid order starting at [firstDayOfWeek]. */
internal fun gregorianWeekdayLabels(firstDayOfWeek: Int): List<String> = List(7) { offset ->
    GregorianPlatformFormatter.weekdayShortLabel((firstDayOfWeek - 1 + offset) % 7 + 1)
}

/** Leading empty cells before [epochDay] for a grid that starts on [firstDayOfWeek]. */
internal fun gregorianDayOfWeekOffset(epochDay: Long, firstDayOfWeek: Int): Int =
    (GregorianYearMonth.isoWeekdayFromEpochDay(epochDay) - firstDayOfWeek + 7) % 7

private fun startOfWeek(epochDay: Long, firstDayOfWeek: Int): Long =
    epochDay - gregorianDayOfWeekOffset(epochDay, firstDayOfWeek)

/**
 * Week of the week-based year with one minimal leading day, matching the previous
 * `WeekFields.of(firstDayOfWeek, 1)` behaviour without java.time.
 */
private fun weekOfWeekBasedYear(epochDay: Long, firstDayOfWeek: Int): Int {
    val year = GregorianYearMonth.fromEpochDay(epochDay).year
    val firstWeekStart = startOfWeek(GregorianYearMonth.gregorianToEpochDay(year, 1, 1), firstDayOfWeek)
    return if (epochDay >= firstWeekStart) {
        ((epochDay - firstWeekStart) / 7).toInt() + 1
    } else {
        val previousWeekStart = startOfWeek(
            GregorianYearMonth.gregorianToEpochDay(year - 1, 1, 1),
            firstDayOfWeek,
        )
        ((epochDay - previousWeekStart) / 7).toInt() + 1
    }
}
