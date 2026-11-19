package com.develotter.calendarview.kmpsample

import com.develotter.calendarview.calendars.chineseCompose.chineseMonthData
import com.develotter.calendarview.calendars.chineseCompose.chineseWeekData
import com.develotter.calendarview.calendars.chineseCompose.chineseYearHeatmap
import com.develotter.calendarview.calendars.chineseNative.ChineseCalendarEngine
import com.develotter.calendarview.calendars.chineseNative.ChineseDate
import com.develotter.calendarview.calendars.chineseNative.ChineseMonth
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.calendars.gregorian.compose.gregorianMonthData
import com.develotter.calendarview.calendars.gregorian.compose.gregorianWeekData
import com.develotter.calendarview.calendars.hijri.HijriDate
import com.develotter.calendarview.calendars.hijri.HijriDateProvider
import com.develotter.calendarview.calendars.hijri.OffsetHijriDateProvider
import com.develotter.calendarview.calendars.hijri.compose.hijriMonthData
import com.develotter.calendarview.calendars.hijri.compose.hijriWeekData
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriMonthData
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriWeekData
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.compose.solarHijriNativeMonthData
import com.develotter.calendarview.calendars.solarHijriNative.compose.solarHijriNativeWeekData
import com.develotter.calendarview.compose.CalendarMonthData
import com.develotter.calendarview.compose.CalendarWeekData
import com.develotter.calendarview.compose.HeatmapMonthData
import com.develotter.calendarview.compose.toHeatmap
import com.develotter.calendarview.locale.CalendarLocale

/** Hijri provider mirroring the selected day offset; zero keeps the Umm al-Qura default. */
internal fun sampleHijriProvider(offsetDays: Int): HijriDateProvider = OffsetHijriDateProvider(offsetDays)

/**
 * Maps the demo script choice onto the engine text styles. The engine has no pinyin style, so
 * pinyin falls back to the English labels the same way the library default does.
 */
internal fun DemoChineseScript.textStyle(): ChineseTextStyle = when (this) {
    DemoChineseScript.Simplified -> ChineseTextStyle.SimplifiedChinese
    DemoChineseScript.Traditional -> ChineseTextStyle.TraditionalChinese
    DemoChineseScript.Pinyin -> ChineseTextStyle.English
    DemoChineseScript.Numeric -> ChineseTextStyle.Numeric
}

/** One month snapshot of [calendar] containing [anchorEpochDay]. */
internal fun sampleMonthData(
    calendar: DemoCalendar,
    anchorEpochDay: Long,
    todayEpochDay: Long,
    hijriDateProvider: HijriDateProvider,
    chineseStyle: ChineseTextStyle,
    locale: CalendarLocale? = null,
): CalendarMonthData = when (calendar) {
    DemoCalendar.Gregorian -> gregorianMonthData(
        month = GregorianYearMonth.fromEpochDay(anchorEpochDay),
        todayEpochDay = todayEpochDay,
        locale = locale,
    )

    DemoCalendar.Hijri -> hijriMonthData(
        month = hijriDateProvider.dateForEpochDay(anchorEpochDay),
        todayEpochDay = todayEpochDay,
        dateProvider = hijriDateProvider,
        locale = locale,
    )

    DemoCalendar.SolarHijri -> solarHijriMonthData(
        month = SolarHijriDate.fromEpochDay(anchorEpochDay),
        todayEpochDay = todayEpochDay,
        locale = locale,
    )

    DemoCalendar.SolarHijriNative -> solarHijriNativeMonthData(
        month = SolarHijriDate.fromEpochDay(anchorEpochDay),
        todayEpochDay = todayEpochDay,
        locale = locale,
    )

    DemoCalendar.Chinese -> chineseMonthData(
        month = ChineseMonth.fromEpochDay(anchorEpochDay),
        style = chineseStyle,
        todayEpochDay = todayEpochDay,
    )
}

/** One seven-day snapshot of [calendar] containing [anchorEpochDay]. */
internal fun sampleWeekData(
    calendar: DemoCalendar,
    anchorEpochDay: Long,
    todayEpochDay: Long,
    hijriDateProvider: HijriDateProvider,
    chineseStyle: ChineseTextStyle,
    locale: CalendarLocale? = null,
): CalendarWeekData = when (calendar) {
    DemoCalendar.Gregorian -> gregorianWeekData(
        dateEpochDay = anchorEpochDay,
        todayEpochDay = todayEpochDay,
        locale = locale,
    )

    DemoCalendar.Hijri -> hijriWeekData(
        dateEpochDay = anchorEpochDay,
        todayEpochDay = todayEpochDay,
        dateProvider = hijriDateProvider,
        locale = locale,
    )

    DemoCalendar.SolarHijri -> solarHijriWeekData(
        dateEpochDay = anchorEpochDay,
        todayEpochDay = todayEpochDay,
        locale = locale,
    )

    DemoCalendar.SolarHijriNative -> solarHijriNativeWeekData(
        dateEpochDay = anchorEpochDay,
        todayEpochDay = todayEpochDay,
        locale = locale,
    )

    DemoCalendar.Chinese -> chineseWeekData(
        epochDay = anchorEpochDay,
        style = chineseStyle,
        todayEpochDay = todayEpochDay,
    )
}

/** Calendar year that contains [todayEpochDay] in [calendar]. */
internal fun sampleYear(
    calendar: DemoCalendar,
    todayEpochDay: Long,
    hijriDateProvider: HijriDateProvider,
): Int = when (calendar) {
    DemoCalendar.Gregorian -> GregorianYearMonth.fromEpochDay(todayEpochDay).year
    DemoCalendar.Hijri -> hijriDateProvider.dateForEpochDay(todayEpochDay).year
    DemoCalendar.SolarHijri -> SolarHijriDate.fromEpochDay(todayEpochDay).year
    DemoCalendar.SolarHijriNative -> SolarHijriDate.fromEpochDay(todayEpochDay).year
    DemoCalendar.Chinese -> ChineseDate.fromEpochDay(todayEpochDay).year
}

/**
 * All months of [year] in [calendar] as generic heatmap snapshots. The synthetic intensity is
 * deterministic so the demo renders the same pattern on every platform.
 */
internal fun sampleHeatmapYear(
    calendar: DemoCalendar,
    year: Int,
    todayEpochDay: Long,
    hijriDateProvider: HijriDateProvider,
    chineseStyle: ChineseTextStyle,
    locale: CalendarLocale? = null,
): List<HeatmapMonthData> = when (calendar) {
    DemoCalendar.Gregorian -> (1..12).map { month ->
        gregorianMonthData(
            month = GregorianYearMonth(year, month),
            todayEpochDay = todayEpochDay,
            locale = locale,
        ).toHeatmap(::sampleHeatmapIntensity)
    }

    DemoCalendar.Hijri -> (1..12).map { month ->
        hijriMonthData(
            month = HijriDate(year, month, 1),
            todayEpochDay = todayEpochDay,
            dateProvider = hijriDateProvider,
            locale = locale,
        ).toHeatmap(::sampleHeatmapIntensity)
    }

    DemoCalendar.SolarHijri -> (1..12).map { month ->
        solarHijriMonthData(
            month = SolarHijriDate(year, month, 1),
            todayEpochDay = todayEpochDay,
            locale = locale,
        ).toHeatmap(::sampleHeatmapIntensity)
    }

    DemoCalendar.SolarHijriNative -> (1..12).map { month ->
        solarHijriNativeMonthData(
            month = SolarHijriDate(year, month, 1),
            todayEpochDay = todayEpochDay,
            locale = locale,
        ).toHeatmap(::sampleHeatmapIntensity)
    }

    DemoCalendar.Chinese -> chineseYearHeatmap(
        year = year,
        style = chineseStyle,
        todayEpochDay = todayEpochDay,
        intensity = ::sampleHeatmapIntensity,
    )
}

/** Deterministic demo intensity in `0f..0.909f`. */
internal fun sampleHeatmapIntensity(epochDay: Long): Float =
    ((epochDay % 11L) + 11L).mod(11L).toFloat() / 10f

/**
 * Epoch day of the first day of the month that is [monthOffset] months away from
 * [anchorEpochDay] in [calendar], or null when that month is outside the engine range.
 */
internal fun sampleShiftMonth(
    calendar: DemoCalendar,
    anchorEpochDay: Long,
    monthOffset: Int,
    hijriDateProvider: HijriDateProvider,
): Long? = runCatching {
    when (calendar) {
        DemoCalendar.Gregorian ->
            GregorianYearMonth.fromEpochDay(anchorEpochDay).plusMonths(monthOffset).atDay(1)

        DemoCalendar.Hijri -> {
            val date = hijriDateProvider.dateForEpochDay(anchorEpochDay)
            val monthIndex = date.year * 12 + (date.month - 1) + monthOffset
            hijriDateProvider.epochDayFor(HijriDate(monthIndex / 12, monthIndex % 12 + 1, 1))
        }

        DemoCalendar.SolarHijri, DemoCalendar.SolarHijriNative ->
            SolarHijriDate.fromEpochDay(anchorEpochDay).plusMonths(monthOffset.toLong()).toEpochDay()

        DemoCalendar.Chinese -> {
            var month = ChineseMonth.fromEpochDay(anchorEpochDay)
            val steps = if (monthOffset < 0) -monthOffset else monthOffset
            repeat(steps) {
                month = if (monthOffset > 0) {
                    ChineseCalendarEngine.nextMonth(month)
                } else {
                    ChineseCalendarEngine.previousMonth(month)
                }
            }
            month.startEpochDay()
        }
    }
}.getOrNull()

/** Localized month caption for the month that is [monthOffset] months away from the anchor. */
internal fun sampleMonthTitle(
    calendar: DemoCalendar,
    anchorEpochDay: Long,
    monthOffset: Int,
    todayEpochDay: Long,
    hijriDateProvider: HijriDateProvider,
    chineseStyle: ChineseTextStyle,
    locale: CalendarLocale? = null,
): String? = sampleShiftMonth(calendar, anchorEpochDay, monthOffset, hijriDateProvider)
    ?.let { shifted ->
        runCatching {
            sampleMonthData(
                calendar,
                shifted,
                todayEpochDay,
                hijriDateProvider,
                chineseStyle,
                locale,
            ).title
        }.getOrNull()
    }
