package com.develotter.calendarviewsample

import com.develotter.calendarview.calendars.chineseNative.ChineseDate
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.calendars.chineseCompose.chineseYearHeatmap
import com.develotter.calendarview.calendars.gregorian.compose.gregorianMonthData
import com.develotter.calendarview.calendars.hijri.HijriDateProvider
import com.develotter.calendarview.calendars.hijri.UmmAlQuraHijriDateProvider
import com.develotter.calendarview.calendars.hijri.compose.hijriMonthData
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriEpochDay
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriMonthData
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.compose.solarHijriNativeMonthData
import com.develotter.calendarview.compose.HeatmapMonthData
import com.develotter.calendarview.compose.toHeatmap
import ir.huri.jcal.JalaliCalendar
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * Builds one heatmap year for the selected calendar. Calendar engines stay in their own
 * modules; :library only receives the generic [HeatmapMonthData] snapshots.
 * A null [firstDayOfWeek] keeps each engine's default (locale-based for Gregorian,
 * Saturday for Hijri and Solar Hijri). [isDateEnabled] is evaluated on the shared
 * Gregorian timeline and becomes [HeatmapDayData.enabled], like the month views.
 */
fun sampleHeatmapYear(
    calendar: SampleCalendar,
    year: Int,
    locale: Locale,
    today: LocalDate = LocalDate.now(),
    firstDayOfWeek: DayOfWeek? = null,
    hijriDateProvider: HijriDateProvider = UmmAlQuraHijriDateProvider,
    isDateEnabled: (LocalDate) -> Boolean = { true },
): List<HeatmapMonthData> = when (calendar) {
    SampleCalendar.Gregorian -> (1..12).map { month ->
        val data = gregorianMonthData(
            YearMonth.of(year, month), locale,
            firstDayOfWeek = firstDayOfWeek ?: WeekFields.of(locale).firstDayOfWeek,
            today = today, isDateEnabled = isDateEnabled,
        )
        data.toHeatmap { sampleHeatmapIntensity(it) }
    }
    SampleCalendar.Hijri -> (1..12).map { month ->
        val data = hijriMonthData(
            HijrahDate.of(year, month, 1), locale,
            firstDayOfWeek = firstDayOfWeek ?: DayOfWeek.SATURDAY,
            today = hijriDateProvider.dateForEpochDay(today.toEpochDay()),
            dateProvider = hijriDateProvider,
            isDateEnabled = { isDateEnabled(LocalDate.ofEpochDay(hijriDateProvider.epochDayFor(it))) },
        )
        data.toHeatmap { sampleHeatmapIntensity(it) }
    }
    SampleCalendar.SolarHijri -> (1..12).map { month ->
        val data = solarHijriMonthData(
            JalaliCalendar(year, month, 1), locale,
            firstDayOfWeek = firstDayOfWeek ?: DayOfWeek.SATURDAY,
            today = JalaliCalendar(today),
            isDateEnabled = { isDateEnabled(LocalDate.ofEpochDay(solarHijriEpochDay(it))) },
        )
        data.toHeatmap { sampleHeatmapIntensity(it) }
    }
    SampleCalendar.Chinese -> chineseYearHeatmap(
        year = year,
        style = ChineseTextStyle.English,
        locale = locale,
        firstDayOfWeek = firstDayOfWeek ?: WeekFields.of(locale).firstDayOfWeek,
        todayEpochDay = today.toEpochDay(),
        isDateEnabled = { isDateEnabled(LocalDate.ofEpochDay(it)) },
        intensity = { sampleHeatmapIntensity(it) },
    )
    SampleCalendar.SolarHijriNative -> (1..12).map { month ->
        val data = solarHijriNativeMonthData(
            SolarHijriDate(year, month, 1), locale,
            firstDayOfWeek = firstDayOfWeek ?: DayOfWeek.SATURDAY,
            today = SolarHijriDate.fromLocalDate(today),
            isDateEnabled = { isDateEnabled(it.toLocalDate()) },
        )
        data.toHeatmap { sampleHeatmapIntensity(it) }
    }
}

/** Current calendar year, used to reset navigation when the calendar changes. */
fun sampleHeatmapCurrentYear(
    calendar: SampleCalendar,
    today: LocalDate = LocalDate.now(),
    hijriDateProvider: HijriDateProvider = UmmAlQuraHijriDateProvider,
): Int = when (calendar) {
    SampleCalendar.Gregorian -> today.year
    SampleCalendar.Hijri -> hijriDateProvider.dateForEpochDay(today.toEpochDay()).get(ChronoField.YEAR)
    SampleCalendar.SolarHijri -> JalaliCalendar(today).year
    SampleCalendar.SolarHijriNative -> SolarHijriDate.fromLocalDate(today).year
    SampleCalendar.Chinese -> ChineseDate.fromEpochDay(today.toEpochDay()).year
}

/** Bounded demo ranges that the platform chronologies and JalaliCalendar support. */
fun sampleHeatmapYearRange(calendar: SampleCalendar): IntRange = when (calendar) {
    SampleCalendar.Gregorian -> 1900..2100
    SampleCalendar.Hijri -> 1300..1600
    SampleCalendar.SolarHijri -> 1200..1600
    SampleCalendar.SolarHijriNative -> 1200..1600
    SampleCalendar.Chinese -> 1901..2099
}

/** Deterministic synthetic values in 0..0.9, matching the previous demo's rhythm. */
private fun sampleHeatmapIntensity(epochDay: Long): Float =
    Math.floorMod(epochDay, 11L).toFloat() / 10f
