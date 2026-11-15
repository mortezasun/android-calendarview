package com.develotter.calendarview.calendars.solarHijriNative.compose

import androidx.compose.runtime.Immutable
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriEngine
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriLocale
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarWeekData
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/** Localized Solar Hijri seven-day snapshot; treat lists as read-only. */
@Immutable
data class SolarHijriNativeWeekData(
    override val title: String,
    override val weekdayLabels: List<String>,
    override val days: List<CalendarDayCell>,
    override val weekNumber: String? = null,
) : CalendarWeekData

/**
 * Builds a Solar Hijri week around [date], aligned to [firstDayOfWeek]. No Solar Hijri
 * week-number convention is assumed, so [SolarHijriNativeWeekData.weekNumber] is null. Days
 * beyond the supported years are blank, disabled cells.
 */
fun solarHijriNativeWeekData(
    date: SolarHijriDate,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.SATURDAY,
    today: SolarHijriDate = SolarHijriDate.now(),
    isDateEnabled: (SolarHijriDate) -> Boolean = { true },
): SolarHijriNativeWeekData {
    val start = date.toLocalDate().with(TemporalAdjusters.previousOrSame(firstDayOfWeek)).toEpochDay()
    fun captionDate(epochDay: Long) = SolarHijriDate.fromEpochDay(
        epochDay.coerceIn(SolarHijriEngine.MIN_EPOCH_DAY, SolarHijriEngine.MAX_EPOCH_DAY),
    )
    val first = captionDate(start)
    val last = captionDate(start + 6)
    return SolarHijriNativeWeekData(
        title = "${SolarHijriLocale.formatMonthYear(first.year, first.month, locale)} – " +
            SolarHijriLocale.formatMonthYear(last.year, last.month, locale),
        weekdayLabels = List(7) { offset ->
            firstDayOfWeek.plus(offset.toLong()).getDisplayName(TextStyle.SHORT_STANDALONE, locale)
        },
        days = List(7) { offset ->
            solarHijriNativeDayCellForEpoch(start + offset, locale, today, isDateEnabled)
        },
    )
}
