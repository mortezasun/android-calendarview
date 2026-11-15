package com.develotter.calendarview.calendars.hijri.compose

import androidx.compose.runtime.Immutable
import com.develotter.calendarview.calendars.hijri.HijriDateProvider
import com.develotter.calendarview.calendars.hijri.UmmAlQuraHijriDateProvider
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarWeekData
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/** Localized Hijri seven-day snapshot; treat lists as read-only. */
@Immutable
data class HijriWeekData(
    override val title: String,
    override val weekdayLabels: List<String>,
    override val days: List<CalendarDayCell>,
    override val weekNumber: String? = null,
) : CalendarWeekData

/**
 * Builds a Hijri week with the platform Umm al-Qura chronology. Use the provider overload
 * for a local date method. No Hijri week-number convention is assumed, so [weekNumber] is null.
 */
fun hijriWeekData(
    date: HijrahDate,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.SATURDAY,
    today: HijrahDate = HijrahDate.now(),
    isDateEnabled: (HijrahDate) -> Boolean = { true },
): HijriWeekData = hijriWeekData(
    date = date,
    locale = locale,
    firstDayOfWeek = firstDayOfWeek,
    today = today,
    dateProvider = UmmAlQuraHijriDateProvider,
    isDateEnabled = isDateEnabled,
)

/**
 * Builds a Hijri week around [date], aligned to [firstDayOfWeek], using [dateProvider].
 * Local moon-sighting adjustments come from an [HijriDateProvider]. No Hijri week-number
 * convention is assumed, so [weekNumber] is null.
 */
fun hijriWeekData(
    date: HijrahDate,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.SATURDAY,
    today: HijrahDate = HijrahDate.now(),
    dateProvider: HijriDateProvider,
    isDateEnabled: (HijrahDate) -> Boolean = { true },
): HijriWeekData {
    // Align the week on the absolute timeline before mapping it back to displayed dates.
    val dateEpochDay = dateProvider.epochDayFor(date)
    val startEpochDay = LocalDate.ofEpochDay(dateEpochDay)
        .with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
        .toEpochDay()
    val todayEpochDay = dateProvider.epochDayFor(today)
    val dates = List(7) { offset ->
        dateProvider.dateForEpochDay(startEpochDay + offset)
    }
    val days = dates.mapIndexed { offset, date ->
        hijriDayCellForEpoch(
            date = date,
            epochDay = startEpochDay + offset,
            locale = locale,
            todayEpochDay = todayEpochDay,
            isDateEnabled = isDateEnabled,
        )
    }
    val titleFormat = DateTimeFormatter.ofPattern("d MMM y G", locale)
        .withChronology(date.chronology)
    return HijriWeekData(
        title = "${dates.first().format(titleFormat)} – ${dates.last().format(titleFormat)}",
        weekdayLabels = List(7) { offset ->
            firstDayOfWeek.plus(offset.toLong()).getDisplayName(TextStyle.SHORT_STANDALONE, locale)
        },
        days = days,
    )
}
