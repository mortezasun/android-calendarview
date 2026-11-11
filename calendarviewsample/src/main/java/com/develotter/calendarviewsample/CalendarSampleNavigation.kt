package com.develotter.calendarviewsample

import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriEpochDay
import ir.huri.jcal.JalaliCalendar
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale

/** Calendar identifiers for this demo only, not a shared-library abstraction. */
enum class SampleCalendar { Gregorian, Hijri, SolarHijri }

/** Host-formatted week number; the library deliberately leaves the convention to the caller. */
fun sampleWeekNumber(epochDay: Long, locale: Locale): String {
    val week = LocalDate.ofEpochDay(epochDay).get(WeekFields.of(locale).weekOfWeekBasedYear())
    return NumberFormat.getIntegerInstance(locale).apply { isGroupingUsed = false }.format(week)
}

/**
 * Returns the first absolute day of the target calendar month.
 * Uses calendar months, not fixed day offsets. Platform/library range errors propagate
 * so the UI can display an error and leave its current month unchanged.
 */
fun moveSampleMonth(calendar: SampleCalendar, anchorEpochDay: Long, offset: Int): Long {
    val date = LocalDate.ofEpochDay(anchorEpochDay)
    return when (calendar) {
        SampleCalendar.Gregorian -> YearMonth.from(date).plusMonths(offset.toLong()).atDay(1).toEpochDay()
        SampleCalendar.Hijri -> HijrahDate.from(date)
            .with(ChronoField.DAY_OF_MONTH, 1)
            .plus(offset.toLong(), ChronoUnit.MONTHS).toEpochDay()
        SampleCalendar.SolarHijri -> {
            val jalali = JalaliCalendar(date)
            val index = jalali.year.toLong() * 12 + jalali.month - 1 + offset.toLong()
            val year = Math.toIntExact(Math.floorDiv(index, 12L))
            val month = Math.floorMod(index, 12L).toInt() + 1
            solarHijriEpochDay(JalaliCalendar(year, month, 1))
        }
    }
}
