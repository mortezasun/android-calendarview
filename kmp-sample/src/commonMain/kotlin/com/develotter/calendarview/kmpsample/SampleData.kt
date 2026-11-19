package com.develotter.calendarview.kmpsample

import com.develotter.calendarview.calendars.chineseCompose.chineseAccessibilityLabel
import com.develotter.calendarview.calendars.gregorian.compose.gregorianAccessibilityLabel
import com.develotter.calendarview.calendars.hijri.compose.hijriAccessibilityLabel
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriAccessibilityLabel
import com.develotter.calendarview.calendars.solarHijriNative.compose.solarHijriNativeAccessibilityLabel
import com.develotter.calendarview.selection.CalendarRangePolicy

/** Demo range limit: a day may be selected within one month around today. */
private val SampleRangePolicy = CalendarRangePolicy(minRangeLength = 1, maxRangeLength = 31)

/** Localized date of [todayEpochDay] in every supported calendar. */
internal fun sampleTodayLabels(todayEpochDay: Long): Map<DemoCalendar, String> =
    DemoCalendar.entries.associateWith { calendar -> sampleCalendarLabel(calendar, todayEpochDay) }

/** Date inspector lines for [epochDay], one per calendar that supports the day. */
internal fun sampleDateDetails(epochDay: Long): List<String> = buildList {
    add("Gregorian: ${gregorianAccessibilityLabel(epochDay)}")
    hijriAccessibilityLabel(epochDay)?.let { add("Hijri: $it") }
    solarHijriAccessibilityLabel(epochDay)?.let { add("Solar Hijri: $it") }
    chineseAccessibilityLabel(epochDay)?.let { add("Chinese: $it") }
}

/**
 * Whether the demo range policy accepts [epochDay] relative to [todayEpochDay]. The Playground
 * uses it as the enabled predicate for the restricted-date mode.
 */
internal fun sampleRangePolicyAccepts(epochDay: Long, todayEpochDay: Long): Boolean =
    SampleRangePolicy.allows(epochDay, todayEpochDay)

private fun sampleCalendarLabel(calendar: DemoCalendar, epochDay: Long): String = when (calendar) {
    DemoCalendar.Gregorian -> gregorianAccessibilityLabel(epochDay)
    DemoCalendar.Hijri -> hijriAccessibilityLabel(epochDay) ?: "Out of range"
    DemoCalendar.SolarHijri -> solarHijriAccessibilityLabel(epochDay) ?: "Out of range"
    DemoCalendar.SolarHijriNative -> solarHijriNativeAccessibilityLabel(epochDay) ?: "Out of range"
    DemoCalendar.Chinese -> chineseAccessibilityLabel(epochDay) ?: "Out of range"
}
