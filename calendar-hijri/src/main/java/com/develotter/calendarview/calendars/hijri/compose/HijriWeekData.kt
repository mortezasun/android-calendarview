package com.develotter.calendarview.calendars.hijri.compose

import androidx.compose.runtime.Immutable
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarWeekData
import java.time.DayOfWeek
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
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
 * Builds a Hijri week around [date], aligned to [firstDayOfWeek].
 * The platform Hijrah chronology is Umm al-Qura; local moon-sighting adjustments are the
 * caller's responsibility. No Hijri week-number convention is assumed, so [weekNumber] is null.
 */
fun hijriWeekData(
    date: HijrahDate,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.SATURDAY,
    today: HijrahDate = HijrahDate.now(),
    isDateEnabled: (HijrahDate) -> Boolean = { true },
): HijriWeekData {
    val start = date.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
    val end = start.plus(6, ChronoUnit.DAYS)
    val titleFormat = DateTimeFormatter.ofPattern("d MMM y G", locale)
        .withChronology(date.chronology)
    return HijriWeekData(
        title = "${start.format(titleFormat)} – ${end.format(titleFormat)}",
        weekdayLabels = List(7) { offset ->
            firstDayOfWeek.plus(offset.toLong()).getDisplayName(TextStyle.SHORT_STANDALONE, locale)
        },
        days = List(7) { offset ->
            hijriDayCell(
                date = start.plus(offset.toLong(), ChronoUnit.DAYS),
                locale = locale,
                today = today,
                isDateEnabled = isDateEnabled,
            )
        },
    )
}
