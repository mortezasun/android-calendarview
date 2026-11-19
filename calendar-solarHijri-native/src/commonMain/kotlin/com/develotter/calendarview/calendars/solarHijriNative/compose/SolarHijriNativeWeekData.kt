package com.develotter.calendarview.calendars.solarHijriNative.compose

import androidx.compose.runtime.Immutable
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriEngine
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriLabels
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriLocale
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriPlatform
import com.develotter.calendarview.calendars.solarHijriNative.isoWeekdayOfEpochDay
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarWeekData
import com.develotter.calendarview.locale.CalendarLocale

/** Saturday is the default first day of the Solar Hijri week. */
internal const val SolarHijriNativeDefaultFirstDayOfWeek: Int = 6

/** Localized Solar Hijri seven-day snapshot; treat lists as read-only. */
@Immutable
data class SolarHijriNativeWeekData(
    override val title: String,
    override val weekdayLabels: List<String>,
    override val days: List<CalendarDayCell>,
    override val weekNumber: String? = null,
) : CalendarWeekData

/**
 * Builds a Solar Hijri week around [dateEpochDay], aligned to [firstDayOfWeek] (ISO 1..7,
 * Monday first). No Solar Hijri week-number convention is assumed, so
 * [SolarHijriNativeWeekData.weekNumber] is null. Days beyond the supported years are blank,
 * disabled cells.
 */
fun solarHijriNativeWeekData(
    dateEpochDay: Long,
    firstDayOfWeek: Int = SolarHijriNativeDefaultFirstDayOfWeek,
    todayEpochDay: Long = SolarHijriPlatform.currentEpochDay(),
    locale: CalendarLocale? = null,
    isDateEnabled: (Long) -> Boolean = { true },
): SolarHijriNativeWeekData {
    val start = dateEpochDay - solarHijriNativeDayOfWeekOffset(dateEpochDay, firstDayOfWeek)
    val first = captionDate(start)
    val last = captionDate(start + 6)
    return SolarHijriNativeWeekData(
        title = if (locale == null) {
            "${SolarHijriLocale.formatMonthYear(first.year, first.month)} – " +
                SolarHijriLocale.formatMonthYear(last.year, last.month)
        } else {
            "${SolarHijriLabels.monthYear(locale, first.year, first.month)} – " +
                SolarHijriLabels.monthYear(locale, last.year, last.month)
        },
        weekdayLabels = solarHijriNativeWeekdayLabels(firstDayOfWeek, locale),
        days = List(7) { offset ->
            solarHijriNativeDayCellForEpoch(
                epochDay = start + offset,
                todayEpochDay = todayEpochDay,
                locale = locale,
                isDateEnabled = isDateEnabled,
            )
        },
    )
}

/** Seven localized short weekday labels in grid order starting at [firstDayOfWeek]. */
internal fun solarHijriNativeWeekdayLabels(
    firstDayOfWeek: Int,
    locale: CalendarLocale? = null,
): List<String> = List(7) { offset ->
    val isoWeekday = (firstDayOfWeek - 1 + offset) % 7 + 1
    locale?.let { SolarHijriLabels.weekdayShort(it, isoWeekday) }
        ?: SolarHijriPlatform.weekdayShortLabel(isoWeekday)
}

/** Leading empty cells before [epochDay] for a grid that starts on [firstDayOfWeek]. */
internal fun solarHijriNativeDayOfWeekOffset(epochDay: Long, firstDayOfWeek: Int): Int =
    (isoWeekdayOfEpochDay(epochDay) - firstDayOfWeek + 7) % 7

private fun captionDate(epochDay: Long): SolarHijriDate = SolarHijriDate.fromEpochDay(
    epochDay.coerceIn(SolarHijriEngine.MIN_EPOCH_DAY, SolarHijriEngine.MAX_EPOCH_DAY),
)
