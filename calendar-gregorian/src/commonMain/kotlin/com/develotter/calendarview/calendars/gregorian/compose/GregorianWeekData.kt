package com.develotter.calendarview.calendars.gregorian.compose

import androidx.compose.runtime.Immutable
import com.develotter.calendarview.calendars.gregorian.GregorianLabels
import com.develotter.calendarview.calendars.gregorian.GregorianPlatformFormatter
import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarWeekData
import com.develotter.calendarview.locale.CalendarLocale
import com.develotter.calendarview.selection.isoWeekNumber

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
 * Monday first). Values outside that range are rejected. The week number uses a locale-independent
 * week-based year with one minimal leading day; callers that need ISO or another convention can
 * pass their own week-number callback to the grid instead. Date-range errors propagate.
 *
 * [useIsoWeekNumber] switches the snapshot's `weekNumber` to the ISO-8601 week of the same week
 * start, still formatted with the selected language's digits. The default keeps the previous
 * week-based-year convention, so existing screens and tests are unchanged.
 */
fun gregorianWeekData(
    dateEpochDay: Long,
    firstDayOfWeek: Int = GregorianPlatformFormatter.defaultFirstDayOfWeek(),
    todayEpochDay: Long = GregorianPlatformFormatter.currentEpochDay(),
    locale: CalendarLocale? = null,
    useIsoWeekNumber: Boolean = false,
    isDateEnabled: (Long) -> Boolean = { true },
): GregorianWeekData {
    val layout = GregorianWeekLayout(firstDayOfWeek)
    val start = layout.startOfWeek(dateEpochDay)
    val end = start + 6
    val weekNumber = if (useIsoWeekNumber) {
        isoWeekNumber(start)
    } else {
        weekOfWeekBasedYear(start, layout)
    }
    return GregorianWeekData(
        title = if (locale == null) {
            "${GregorianPlatformFormatter.formatShortDayMonth(start)} – " +
                GregorianPlatformFormatter.formatShortDayMonthYear(end)
        } else {
            "${GregorianLabels.shortDayMonth(locale, start)} – " +
                GregorianLabels.shortDayMonthYear(locale, end)
        },
        weekdayLabels = layout.weekdayLabels(locale),
        days = List(7) { offset ->
            gregorianDayCell(
                epochDay = start + offset,
                todayEpochDay = todayEpochDay,
                isDateEnabled = isDateEnabled,
                locale = locale,
            )
        },
        weekNumber = locale?.let { GregorianLabels.number(it, weekNumber) }
            ?: GregorianPlatformFormatter.formatDayNumber(weekNumber),
    )
}

/** Seven localized short weekday labels in grid order starting at [firstDayOfWeek]. */
internal fun gregorianWeekdayLabels(
    firstDayOfWeek: Int,
    locale: CalendarLocale? = null,
): List<String> = GregorianWeekLayout(firstDayOfWeek).weekdayLabels(locale)

/** Leading empty cells before [epochDay] for a grid that starts on [firstDayOfWeek]. */
internal fun gregorianDayOfWeekOffset(epochDay: Long, firstDayOfWeek: Int): Int =
    GregorianWeekLayout(firstDayOfWeek).offsetFor(epochDay)

private class GregorianWeekLayout(private val firstDayOfWeek: Int) {
    init {
        require(firstDayOfWeek in 1..7) { "firstDayOfWeek must be in 1..7: $firstDayOfWeek" }
    }

    fun offsetFor(epochDay: Long): Int =
        (GregorianYearMonth.isoWeekdayFromEpochDay(epochDay) - firstDayOfWeek).mod(7)

    fun startOfWeek(epochDay: Long): Long = epochDay - offsetFor(epochDay)

    fun weekdayLabels(locale: CalendarLocale?): List<String> = List(7) { offset ->
        val isoWeekday = (firstDayOfWeek - 1 + offset).mod(7) + 1
        locale?.let { GregorianLabels.weekdayShort(it, isoWeekday) }
            ?: GregorianPlatformFormatter.weekdayShortLabel(isoWeekday)
    }
}

/**
 * Week of the week-based year with one minimal leading day, matching the previous
 * `WeekFields.of(firstDayOfWeek, 1)` behaviour without java.time.
 */
private fun weekOfWeekBasedYear(epochDay: Long, layout: GregorianWeekLayout): Int {
    val year = GregorianYearMonth.fromEpochDay(epochDay).year
    val firstWeekStart = layout.startOfWeek(GregorianYearMonth.gregorianToEpochDay(year, 1, 1))
    return if (epochDay >= firstWeekStart) {
        ((epochDay - firstWeekStart) / 7).toInt() + 1
    } else {
        val previousWeekStart = layout.startOfWeek(GregorianYearMonth.gregorianToEpochDay(year - 1, 1, 1))
        ((epochDay - previousWeekStart) / 7).toInt() + 1
    }
}
