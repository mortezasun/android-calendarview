package com.develotter.calendarview.calendars.solarHijri.compose

import androidx.compose.runtime.Immutable
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarWeekData
import ir.huri.jcal.JalaliCalendar
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/** Localized Solar Hijri seven-day snapshot; treat lists as read-only. */
@Immutable
data class SolarHijriWeekData(
    override val title: String,
    override val weekdayLabels: List<String>,
    override val days: List<CalendarDayCell>,
    override val weekNumber: String? = null,
) : CalendarWeekData

/**
 * Builds a Solar Hijri week around [date], aligned to [firstDayOfWeek].
 * Conversion goes through the checked absolute-day bridge; no Solar Hijri week-number
 * convention is assumed, so [weekNumber] is null.
 */
fun solarHijriWeekData(
    date: JalaliCalendar,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.SATURDAY,
    today: JalaliCalendar = JalaliCalendar(LocalDate.now()),
    isDateEnabled: (JalaliCalendar) -> Boolean = { true },
): SolarHijriWeekData {
    val startLocal = LocalDate.ofEpochDay(solarHijriEpochDay(date))
        .with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
    val start = JalaliCalendar(startLocal)
    val end = JalaliCalendar(startLocal.plusDays(6))
    return SolarHijriWeekData(
        title = "${solarHijriMonthTitle(start, locale)} – ${solarHijriMonthTitle(end, locale)}",
        weekdayLabels = List(7) { offset ->
            firstDayOfWeek.plus(offset.toLong()).getDisplayName(TextStyle.SHORT_STANDALONE, locale)
        },
        days = List(7) { offset ->
            solarHijriDayCell(
                date = JalaliCalendar(startLocal.plusDays(offset.toLong())),
                locale = locale,
                today = today,
                isDateEnabled = isDateEnabled,
            )
        },
    )
}
