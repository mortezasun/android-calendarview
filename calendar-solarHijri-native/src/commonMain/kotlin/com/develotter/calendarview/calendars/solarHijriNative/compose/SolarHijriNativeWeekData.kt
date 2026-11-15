package com.develotter.calendarview.calendars.solarHijriNative.compose

import androidx.compose.runtime.Immutable
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriEngine
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriLocale
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriPlatform
import com.develotter.calendarview.calendars.solarHijriNative.isoWeekdayOfEpochDay
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarWeekData

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
    isDateEnabled: (Long) -> Boolean = { true },
): SolarHijriNativeWeekData {
    val start = dateEpochDay - solarHijriNativeDayOfWeekOffset(dateEpochDay, firstDayOfWeek)
    val first = captionDate(start)
    val last = captionDate(start + 6)
    return SolarHijriNativeWeekData(
        title = "${SolarHijriLocale.formatMonthYear(first.year, first.month)} – " +
            SolarHijriLocale.formatMonthYear(last.year, last.month),
        weekdayLabels = solarHijriNativeWeekdayLabels(firstDayOfWeek),
        days = List(7) { offset ->
            solarHijriNativeDayCellForEpoch(start + offset, todayEpochDay, isDateEnabled)
        },
    )
}

/** Seven localized short weekday labels in grid order starting at [firstDayOfWeek]. */
internal fun solarHijriNativeWeekdayLabels(firstDayOfWeek: Int): List<String> = List(7) { offset ->
    SolarHijriPlatform.weekdayShortLabel((firstDayOfWeek - 1 + offset) % 7 + 1)
}

/** Leading empty cells before [epochDay] for a grid that starts on [firstDayOfWeek]. */
internal fun solarHijriNativeDayOfWeekOffset(epochDay: Long, firstDayOfWeek: Int): Int =
    (isoWeekdayOfEpochDay(epochDay) - firstDayOfWeek + 7) % 7

private fun captionDate(epochDay: Long): SolarHijriDate = SolarHijriDate.fromEpochDay(
    epochDay.coerceIn(SolarHijriEngine.MIN_EPOCH_DAY, SolarHijriEngine.MAX_EPOCH_DAY),
)
