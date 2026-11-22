package com.develotter.calendarview.calendars.hijri.compose

import androidx.compose.runtime.Immutable
import com.develotter.calendarview.calendars.hijri.HijriDate
import com.develotter.calendarview.calendars.hijri.HijriDateProvider
import com.develotter.calendarview.calendars.hijri.HijriLabels
import com.develotter.calendarview.calendars.hijri.HijriPlatform
import com.develotter.calendarview.calendars.hijri.UmmAlQuraHijriDateProvider
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarWeekData
import com.develotter.calendarview.locale.CalendarLocale
import com.develotter.calendarview.selection.isoWeekNumber

/** Saturday is the default first day of the Hijri week. */
internal const val HijriDefaultFirstDayOfWeek: Int = 6

/** Localized Hijri seven-day snapshot; treat lists as read-only. */
@Immutable
data class HijriWeekData(
    override val title: String,
    override val weekdayLabels: List<String>,
    override val days: List<CalendarDayCell>,
    override val weekNumber: String? = null,
) : CalendarWeekData

/**
 * Builds a Hijri week around [dateEpochDay], aligned to [firstDayOfWeek] (ISO 1..7, Monday
 * first) on the absolute timeline before mapping the days back to displayed dates. Values outside
 * that range are rejected. Local moon-sighting adjustments come from [dateProvider]. No Hijri
 * week-number convention is assumed, so `weekNumber` is null unless [useIsoWeekNumber] opts in
 * to the ISO-8601 week number of the week start, formatted with the selected language's digits.
 */
fun hijriWeekData(
    dateEpochDay: Long,
    firstDayOfWeek: Int = HijriDefaultFirstDayOfWeek,
    todayEpochDay: Long = HijriPlatform.currentEpochDay(),
    dateProvider: HijriDateProvider = UmmAlQuraHijriDateProvider,
    locale: CalendarLocale? = null,
    useIsoWeekNumber: Boolean = false,
    isDateEnabled: (Long) -> Boolean = { true },
): HijriWeekData {
    val startEpochDay = dateEpochDay - hijriDayOfWeekOffset(dateEpochDay, firstDayOfWeek)
    val dates = List(7) { offset -> dateProvider.dateForEpochDay(startEpochDay + offset) }
    val weekNumber = if (useIsoWeekNumber) {
        val iso = isoWeekNumber(startEpochDay)
        locale?.let { HijriLabels.number(it, iso) } ?: iso.toString()
    } else {
        null
    }
    return HijriWeekData(
        title = "${displayedShortDate(dates.first(), locale)} – " +
            displayedShortDate(dates.last(), locale),
        weekdayLabels = hijriWeekdayLabels(firstDayOfWeek, locale),
        days = dates.mapIndexed { offset, date ->
            hijriDayCellForEpoch(
                date = date,
                epochDay = startEpochDay + offset,
                todayEpochDay = todayEpochDay,
                isDateEnabled = isDateEnabled,
                locale = locale,
            )
        },
        weekNumber = weekNumber,
    )
}

/** Seven localized short weekday labels in grid order starting at [firstDayOfWeek]. */
internal fun hijriWeekdayLabels(
    firstDayOfWeek: Int,
    locale: CalendarLocale? = null,
): List<String> {
    requireValidFirstDayOfWeek(firstDayOfWeek)
    return List(7) { offset ->
        val isoWeekday = (firstDayOfWeek - 1 + offset).mod(7) + 1
        locale?.let { HijriLabels.weekdayShort(it, isoWeekday) }
            ?: HijriPlatform.weekdayShortLabel(isoWeekday)
    }
}

/** Leading empty cells before [epochDay] for a grid that starts on [firstDayOfWeek]. */
internal fun hijriDayOfWeekOffset(epochDay: Long, firstDayOfWeek: Int): Int {
    requireValidFirstDayOfWeek(firstDayOfWeek)
    return (isoWeekday(epochDay) - firstDayOfWeek).mod(7)
}

private fun requireValidFirstDayOfWeek(firstDayOfWeek: Int) {
    require(firstDayOfWeek in 1..7) { "firstDayOfWeek must be in 1..7: $firstDayOfWeek" }
}

/** ISO weekday (Monday 1 .. Sunday 7) of an absolute epoch day. */
internal fun isoWeekday(epochDay: Long): Int = (epochDay.mod(7L).toInt() + 3).mod(7) + 1

/** Formats a provider date through its canonical epoch day so offset providers stay correct. */
private fun displayedShortDate(date: HijriDate, locale: CalendarLocale?): String =
    locale?.let { HijriLabels.shortDayMonthYear(it, date) }
        ?: HijriPlatform.formatShortDayMonthYear(HijriPlatform.toEpochDay(date))
