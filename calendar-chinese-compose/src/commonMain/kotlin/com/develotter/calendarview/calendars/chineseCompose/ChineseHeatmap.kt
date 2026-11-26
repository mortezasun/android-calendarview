package com.develotter.calendarview.calendars.chineseCompose

import com.develotter.calendarview.calendars.chineseNative.ChineseCalendarEngine
import com.develotter.calendarview.calendars.chineseNative.ChineseMonth
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.compose.HeatmapMonthData
import com.develotter.calendarview.compose.toHeatmap
import com.develotter.calendarview.locale.CalendarLocale

/**
 * Year heatmap blocks for the Chinese calendar, one per month.
 *
 * A Chinese year has 12 or 13 blocks, and the leap month keeps its own title and key. Intensities
 * are read through the shared [toHeatmap] adapter; disabled days and today state come from the
 * month data. Months are ordered chronologically. The caller renders the blocks with the
 * calendar-agnostic `CalendarYearHeatmap(months, ...)` overload. A non-null [locale] localizes the
 * weekday labels only; titles and day labels follow [style].
 */
fun chineseYearHeatmap(
    year: Int,
    style: ChineseTextStyle = ChineseTextStyle.English,
    firstDayOfWeek: Int = ChinesePlatform.defaultFirstDayOfWeek(),
    todayEpochDay: Long = ChinesePlatform.currentEpochDay(),
    locale: CalendarLocale? = null,
    isDateEnabled: (Long) -> Boolean = { true },
    intensity: (Long) -> Float = { 0f },
): List<HeatmapMonthData> = ChineseCalendarEngine.monthsInYear(year).map { month ->
    chineseMonthData(
        month = month,
        style = style,
        firstDayOfWeek = firstDayOfWeek,
        todayEpochDay = todayEpochDay,
        locale = locale,
        isDateEnabled = isDateEnabled,
    ).toHeatmap(intensity)
}

/** Binary-compatible overload for callers compiled against 3.1.0-beta without `locale`. */
@Deprecated(
    message = "Binary compatibility overload without the locale parameter.",
    level = DeprecationLevel.HIDDEN,
)
fun chineseYearHeatmap(
    year: Int,
    style: ChineseTextStyle = ChineseTextStyle.English,
    firstDayOfWeek: Int = ChinesePlatform.defaultFirstDayOfWeek(),
    todayEpochDay: Long = ChinesePlatform.currentEpochDay(),
    isDateEnabled: (Long) -> Boolean = { true },
    intensity: (Long) -> Float = { 0f },
): List<HeatmapMonthData> =
    chineseYearHeatmap(year, style, firstDayOfWeek, todayEpochDay, null, isDateEnabled, intensity)

/**
 * Month heatmap block including its leap-month marker. Use this for a single month outside a
 * full year, for example a detail pane. A non-null [locale] localizes the weekday labels only.
 */
fun chineseMonthHeatmap(
    year: Int,
    monthNumber: Int,
    isLeapMonth: Boolean,
    style: ChineseTextStyle = ChineseTextStyle.English,
    firstDayOfWeek: Int = ChinesePlatform.defaultFirstDayOfWeek(),
    todayEpochDay: Long = ChinesePlatform.currentEpochDay(),
    locale: CalendarLocale? = null,
    isDateEnabled: (Long) -> Boolean = { true },
    intensity: (Long) -> Float = { 0f },
): HeatmapMonthData = chineseMonthData(
    month = ChineseMonth.of(year, monthNumber, isLeapMonth),
    style = style,
    firstDayOfWeek = firstDayOfWeek,
    todayEpochDay = todayEpochDay,
    locale = locale,
    isDateEnabled = isDateEnabled,
).toHeatmap(intensity)

/** Binary-compatible overload for callers compiled against 3.1.0-beta without `locale`. */
@Deprecated(
    message = "Binary compatibility overload without the locale parameter.",
    level = DeprecationLevel.HIDDEN,
)
fun chineseMonthHeatmap(
    year: Int,
    monthNumber: Int,
    isLeapMonth: Boolean,
    style: ChineseTextStyle = ChineseTextStyle.English,
    firstDayOfWeek: Int = ChinesePlatform.defaultFirstDayOfWeek(),
    todayEpochDay: Long = ChinesePlatform.currentEpochDay(),
    isDateEnabled: (Long) -> Boolean = { true },
    intensity: (Long) -> Float = { 0f },
): HeatmapMonthData = chineseMonthHeatmap(
    year, monthNumber, isLeapMonth, style, firstDayOfWeek, todayEpochDay, null, isDateEnabled, intensity,
)
