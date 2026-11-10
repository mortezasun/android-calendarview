package com.develotter.calendarviewsample

import com.develotter.calendarview.calendars.gregorian.compose.gregorianMonthData
import com.develotter.calendarview.calendars.hijri.compose.hijriMonthData
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriEpochDay
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriMonthData
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.HeatmapDayData
import com.develotter.calendarview.compose.HeatmapMonthData
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
    isDateEnabled: (LocalDate) -> Boolean = { true },
): List<HeatmapMonthData> = when (calendar) {
    SampleCalendar.Gregorian -> (1..12).map { month ->
        val data = gregorianMonthData(
            YearMonth.of(year, month), locale,
            firstDayOfWeek = firstDayOfWeek ?: WeekFields.of(locale).firstDayOfWeek,
            today = today, isDateEnabled = isDateEnabled,
        )
        data.toHeatmapMonth()
    }
    SampleCalendar.Hijri -> (1..12).map { month ->
        val data = hijriMonthData(
            HijrahDate.of(year, month, 1), locale,
            firstDayOfWeek = firstDayOfWeek ?: DayOfWeek.SATURDAY,
            today = HijrahDate.from(today), isDateEnabled = { isDateEnabled(LocalDate.from(it)) },
        )
        data.toHeatmapMonth()
    }
    SampleCalendar.SolarHijri -> (1..12).map { month ->
        val data = solarHijriMonthData(
            JalaliCalendar(year, month, 1), locale,
            firstDayOfWeek = firstDayOfWeek ?: DayOfWeek.SATURDAY,
            today = JalaliCalendar(today),
            isDateEnabled = { isDateEnabled(LocalDate.ofEpochDay(solarHijriEpochDay(it))) },
        )
        data.toHeatmapMonth()
    }
}

/** Current calendar year, used to reset navigation when the calendar changes. */
fun sampleHeatmapCurrentYear(calendar: SampleCalendar, today: LocalDate = LocalDate.now()): Int = when (calendar) {
    SampleCalendar.Gregorian -> today.year
    SampleCalendar.Hijri -> HijrahDate.from(today).get(ChronoField.YEAR)
    SampleCalendar.SolarHijri -> JalaliCalendar(today).year
}

/** Bounded demo ranges that the platform chronologies and JalaliCalendar support. */
fun sampleHeatmapYearRange(calendar: SampleCalendar): IntRange = when (calendar) {
    SampleCalendar.Gregorian -> 1900..2100
    SampleCalendar.Hijri -> 1300..1600
    SampleCalendar.SolarHijri -> 1200..1600
}

private fun com.develotter.calendarview.calendars.gregorian.compose.GregorianMonthData.toHeatmapMonth() =
    heatmapMonth(title, weekdayLabels, firstDayOffset, days)

private fun com.develotter.calendarview.calendars.hijri.compose.HijriMonthData.toHeatmapMonth() =
    heatmapMonth(title, weekdayLabels, firstDayOffset, days)

private fun com.develotter.calendarview.calendars.solarHijri.compose.SolarHijriMonthData.toHeatmapMonth() =
    heatmapMonth(title, weekdayLabels, firstDayOffset, days)

private fun heatmapMonth(
    title: String,
    weekdayLabels: List<String>,
    firstDayOffset: Int,
    days: List<CalendarDayCell>,
): HeatmapMonthData {
    val cells = buildList<HeatmapDayData?> {
        repeat(firstDayOffset) { add(null) }
        days.forEach { day ->
            add(
                HeatmapDayData(
                    epochDay = day.epochDay,
                    label = day.accessibilityLabel,
                    intensity = sampleHeatmapIntensity(day.epochDay),
                    isToday = day.isToday,
                    enabled = day.enabled,
                ),
            )
        }
        while (size % 7 != 0) add(null)
    }
    return HeatmapMonthData(title, weekdayLabels, cells.chunked(7))
}

/** Deterministic synthetic values in 0..0.9, matching the previous demo's rhythm. */
private fun sampleHeatmapIntensity(epochDay: Long): Float =
    Math.floorMod(epochDay, 11L).toFloat() / 10f
