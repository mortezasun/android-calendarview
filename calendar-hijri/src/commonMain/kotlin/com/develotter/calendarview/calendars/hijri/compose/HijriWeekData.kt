package com.develotter.calendarview.calendars.hijri.compose

import androidx.compose.runtime.Immutable
import com.develotter.calendarview.calendars.hijri.HijriDate
import com.develotter.calendarview.calendars.hijri.HijriDateProvider
import com.develotter.calendarview.calendars.hijri.HijriPlatform
import com.develotter.calendarview.calendars.hijri.UmmAlQuraHijriDateProvider
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarWeekData

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
 * first) on the absolute timeline before mapping the days back to displayed dates. Local
 * moon-sighting adjustments come from [dateProvider]. No Hijri week-number convention is
 * assumed, so [weekNumber] is null.
 */
fun hijriWeekData(
    dateEpochDay: Long,
    firstDayOfWeek: Int = HijriDefaultFirstDayOfWeek,
    todayEpochDay: Long = HijriPlatform.currentEpochDay(),
    dateProvider: HijriDateProvider = UmmAlQuraHijriDateProvider,
    isDateEnabled: (Long) -> Boolean = { true },
): HijriWeekData {
    val startEpochDay = dateEpochDay - hijriDayOfWeekOffset(dateEpochDay, firstDayOfWeek)
    val dates = List(7) { offset -> dateProvider.dateForEpochDay(startEpochDay + offset) }
    return HijriWeekData(
        title = "${displayedShortDate(dates.first())} – ${displayedShortDate(dates.last())}",
        weekdayLabels = hijriWeekdayLabels(firstDayOfWeek),
        days = dates.mapIndexed { offset, date ->
            hijriDayCellForEpoch(
                date = date,
                epochDay = startEpochDay + offset,
                todayEpochDay = todayEpochDay,
                isDateEnabled = isDateEnabled,
            )
        },
    )
}

/** Seven localized short weekday labels in grid order starting at [firstDayOfWeek]. */
internal fun hijriWeekdayLabels(firstDayOfWeek: Int): List<String> = List(7) { offset ->
    HijriPlatform.weekdayShortLabel((firstDayOfWeek - 1 + offset) % 7 + 1)
}

/** Leading empty cells before [epochDay] for a grid that starts on [firstDayOfWeek]. */
internal fun hijriDayOfWeekOffset(epochDay: Long, firstDayOfWeek: Int): Int =
    (isoWeekday(epochDay) - firstDayOfWeek + 7) % 7

/** ISO weekday (Monday 1 .. Sunday 7) of an absolute epoch day. */
internal fun isoWeekday(epochDay: Long): Int = (((epochDay + 3) % 7 + 7) % 7).toInt() + 1

/** Formats a provider date through its canonical epoch day so offset providers stay correct. */
private fun displayedShortDate(date: HijriDate): String =
    HijriPlatform.formatShortDayMonthYear(HijriPlatform.toEpochDay(date))
