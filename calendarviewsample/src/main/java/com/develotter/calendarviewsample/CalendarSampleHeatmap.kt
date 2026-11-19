package com.develotter.calendarviewsample

import com.develotter.calendarview.calendars.chineseNative.ChineseDate
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.calendars.chineseCompose.chineseYearHeatmap
import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.calendars.gregorian.compose.gregorianMonthData
import com.develotter.calendarview.calendars.hijri.HijriDate
import com.develotter.calendarview.calendars.hijri.HijriDateProvider
import com.develotter.calendarview.calendars.hijri.UmmAlQuraHijriDateProvider
import com.develotter.calendarview.calendars.hijri.compose.hijriMonthData
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriMonthData
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.compose.solarHijriNativeMonthData
import com.develotter.calendarview.compose.HeatmapMonthData
import com.develotter.calendarview.compose.toHeatmap
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * Builds one heatmap year for the selected calendar. Calendar engines stay in their own
 * modules; :library only receives the generic [HeatmapMonthData] snapshots.
 * A null [firstDayOfWeek] keeps each engine's default (locale-based for Gregorian,
 * Saturday for Hijri and Solar Hijri). [isDateEnabled] is evaluated on the shared
 * absolute timeline and becomes `HeatmapDayData.enabled`, like the month views.
 */
fun sampleHeatmapYear(
    calendar: SampleCalendar,
    year: Int,
    locale: Locale,
    today: LocalDate = LocalDate.now(),
    firstDayOfWeek: DayOfWeek? = null,
    hijriDateProvider: HijriDateProvider = UmmAlQuraHijriDateProvider,
    isDateEnabled: (LocalDate) -> Boolean = { true },
): List<HeatmapMonthData> {
    val todayEpochDay = today.toEpochDay()
    val enabled = { epochDay: Long -> isDateEnabled(LocalDate.ofEpochDay(epochDay)) }
    val calendarLocale = locale.asCalendarLocale()
    val localeFirstDay = (firstDayOfWeek ?: WeekFields.of(locale).firstDayOfWeek).value
    return when (calendar) {
        SampleCalendar.Gregorian -> (1..12).map { month ->
            gregorianMonthData(
                month = GregorianYearMonth(year, month),
                firstDayOfWeek = localeFirstDay,
                todayEpochDay = todayEpochDay,
                locale = calendarLocale,
                isDateEnabled = enabled,
            ).toHeatmap { sampleHeatmapIntensity(it) }
        }
        SampleCalendar.Hijri -> (1..12).map { month ->
            hijriMonthData(
                month = HijriDate(year, month, 1),
                firstDayOfWeek = (firstDayOfWeek ?: DayOfWeek.SATURDAY).value,
                todayEpochDay = todayEpochDay,
                dateProvider = hijriDateProvider,
                locale = calendarLocale,
                isDateEnabled = enabled,
            ).toHeatmap { sampleHeatmapIntensity(it) }
        }
        SampleCalendar.SolarHijri -> (1..12).map { month ->
            solarHijriMonthData(
                month = SolarHijriDate(year, month, 1),
                firstDayOfWeek = (firstDayOfWeek ?: DayOfWeek.SATURDAY).value,
                todayEpochDay = todayEpochDay,
                locale = calendarLocale,
                isDateEnabled = enabled,
            ).toHeatmap { sampleHeatmapIntensity(it) }
        }
        SampleCalendar.Chinese -> chineseYearHeatmap(
            year = year,
            style = ChineseTextStyle.English,
            firstDayOfWeek = localeFirstDay,
            todayEpochDay = todayEpochDay,
            isDateEnabled = enabled,
            intensity = { sampleHeatmapIntensity(it) },
        )
        SampleCalendar.SolarHijriNative -> (1..12).map { month ->
            solarHijriNativeMonthData(
                month = SolarHijriDate(year, month, 1),
                firstDayOfWeek = (firstDayOfWeek ?: DayOfWeek.SATURDAY).value,
                todayEpochDay = todayEpochDay,
                locale = calendarLocale,
                isDateEnabled = enabled,
            ).toHeatmap { sampleHeatmapIntensity(it) }
        }
    }
}

/** Current calendar year, used to reset navigation when the calendar changes. */
fun sampleHeatmapCurrentYear(
    calendar: SampleCalendar,
    today: LocalDate = LocalDate.now(),
    hijriDateProvider: HijriDateProvider = UmmAlQuraHijriDateProvider,
): Int {
    val todayEpochDay = today.toEpochDay()
    return when (calendar) {
        SampleCalendar.Gregorian -> GregorianYearMonth.fromEpochDay(todayEpochDay).year
        SampleCalendar.Hijri -> hijriDateProvider.dateForEpochDay(todayEpochDay).year
        SampleCalendar.SolarHijri -> SolarHijriDate.fromEpochDay(todayEpochDay).year
        SampleCalendar.SolarHijriNative -> SolarHijriDate.fromEpochDay(todayEpochDay).year
        SampleCalendar.Chinese -> ChineseDate.fromEpochDay(todayEpochDay).year
    }
}

/** Bounded demo ranges that the platform chronologies and engines support. */
fun sampleHeatmapYearRange(calendar: SampleCalendar): IntRange = when (calendar) {
    SampleCalendar.Gregorian -> 1900..2100
    SampleCalendar.Hijri -> 1300..1600
    SampleCalendar.SolarHijri -> 1200..1600
    SampleCalendar.SolarHijriNative -> 1200..1600
    SampleCalendar.Chinese -> 1901..2099
}

/** Deterministic synthetic values in 0..0.9, matching the previous demo's rhythm. */
private fun sampleHeatmapIntensity(epochDay: Long): Float =
    (epochDay % 11L + 11L).mod(11L).toFloat() / 10f
