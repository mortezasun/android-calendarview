package com.develotter.calendarview.calendars.solarHijri.compose

import androidx.compose.runtime.Immutable
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.compose.solarHijriNativeWeekData
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarWeekData
import com.develotter.calendarview.locale.CalendarLocale

/** Saturday-first is configurable. */
private const val DEFAULT_FIRST_DAY_OF_WEEK = 6

/** Localized Solar Hijri seven-day snapshot; treat lists as read-only. */
@Immutable
data class SolarHijriWeekData(
    override val title: String,
    override val weekdayLabels: List<String>,
    override val days: List<CalendarDayCell>,
    override val weekNumber: String? = null,
) : CalendarWeekData

/**
 * Builds a Solar Hijri week around [dateEpochDay], aligned to [firstDayOfWeek] (ISO 1..7,
 * Monday first). Conversion goes through the checked absolute-day bridge; no Solar Hijri
 * week-number convention is assumed, so `weekNumber` is null.
 */
fun solarHijriWeekData(
    dateEpochDay: Long,
    firstDayOfWeek: Int = DEFAULT_FIRST_DAY_OF_WEEK,
    todayEpochDay: Long = SolarHijriDate.now().toEpochDay(),
    locale: CalendarLocale? = null,
    isDateEnabled: (Long) -> Boolean = { true },
): SolarHijriWeekData {
    val native = solarHijriNativeWeekData(
        dateEpochDay = dateEpochDay,
        firstDayOfWeek = firstDayOfWeek,
        todayEpochDay = todayEpochDay,
        locale = locale,
        isDateEnabled = isDateEnabled,
    )
    return SolarHijriWeekData(
        title = native.title,
        weekdayLabels = native.weekdayLabels,
        days = native.days,
    )
}
