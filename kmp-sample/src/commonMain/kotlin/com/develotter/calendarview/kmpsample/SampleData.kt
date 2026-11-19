package com.develotter.calendarview.kmpsample

import com.develotter.calendarview.calendars.chineseCompose.chineseAccessibilityLabel
import com.develotter.calendarview.calendars.gregorian.compose.gregorianAccessibilityLabel
import com.develotter.calendarview.calendars.hijri.compose.hijriAccessibilityLabel
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriAccessibilityLabel
import com.develotter.calendarview.calendars.solarHijriNative.compose.solarHijriNativeAccessibilityLabel
import com.develotter.calendarview.locale.CalendarLocale
import com.develotter.calendarview.selection.CalendarRangePolicy

/** Demo range limit: a day may be selected within one month around today. */
private val SampleRangePolicy = CalendarRangePolicy(minRangeLength = 1, maxRangeLength = 31)

/** Localized date of [todayEpochDay] in every supported calendar. */
internal fun sampleTodayLabels(
    todayEpochDay: Long,
    locale: CalendarLocale? = null,
): Map<DemoCalendar, String> =
    DemoCalendar.entries.associateWith { calendar ->
        sampleCalendarLabel(calendar, todayEpochDay, locale)
    }

/** Date inspector lines for [epochDay], one per calendar that supports the day. */
internal fun sampleDateDetails(
    epochDay: Long,
    locale: CalendarLocale? = null,
): List<String> = buildList {
    add("Gregorian: ${gregorianAccessibilityLabel(epochDay, locale)}")
    hijriAccessibilityLabel(epochDay, locale = locale)?.let { add("Hijri: $it") }
    solarHijriAccessibilityLabel(epochDay, locale)?.let { add("Solar Hijri: $it") }
    chineseAccessibilityLabel(epochDay)?.let { add("Chinese: $it") }
}

/**
 * Whether the demo range policy accepts [epochDay] relative to [todayEpochDay]. The Playground
 * uses it as the enabled predicate for the restricted-date mode.
 */
internal fun sampleRangePolicyAccepts(epochDay: Long, todayEpochDay: Long): Boolean =
    SampleRangePolicy.allows(epochDay, todayEpochDay)

private fun sampleCalendarLabel(
    calendar: DemoCalendar,
    epochDay: Long,
    locale: CalendarLocale?,
): String = when (calendar) {
    DemoCalendar.Gregorian -> gregorianAccessibilityLabel(epochDay, locale)
    DemoCalendar.Hijri -> hijriAccessibilityLabel(epochDay, locale = locale) ?: "Out of range"
    DemoCalendar.SolarHijri -> solarHijriAccessibilityLabel(epochDay, locale) ?: "Out of range"
    DemoCalendar.SolarHijriNative ->
        solarHijriNativeAccessibilityLabel(epochDay, locale) ?: "Out of range"

    DemoCalendar.Chinese -> chineseAccessibilityLabel(epochDay) ?: "Out of range"
}
