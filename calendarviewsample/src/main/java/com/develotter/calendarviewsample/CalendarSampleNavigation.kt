package com.develotter.calendarviewsample

import com.develotter.calendarview.calendars.chineseNative.ChineseCalendarEngine
import com.develotter.calendarview.calendars.chineseNative.ChineseDate
import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.calendars.hijri.HijriDate
import com.develotter.calendarview.calendars.hijri.UmmAlQuraHijriDateProvider
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import java.text.NumberFormat
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

/** Calendar identifiers for this demo only, not a shared-library abstraction. */
enum class SampleCalendar { Gregorian, Hijri, SolarHijri, SolarHijriNative, Chinese }

/**
 * Month stepping / pager identity for the current rendering mode. The Gregorian overlay of the
 * Chinese calendar is a Gregorian month grid, so it must not step through Chinese lunar months.
 */
internal fun sampleMonthNavigationCalendar(
    calendar: SampleCalendar,
    chineseGregorianGrid: Boolean,
): SampleCalendar = if (calendar == SampleCalendar.Chinese && chineseGregorianGrid) {
    SampleCalendar.Gregorian
} else {
    calendar
}

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
fun moveSampleMonth(calendar: SampleCalendar, anchorEpochDay: Long, offset: Int): Long = when (calendar) {
    SampleCalendar.Gregorian -> GregorianYearMonth.fromEpochDay(anchorEpochDay)
        .plusMonths(offset)
        .atDay(1)
    SampleCalendar.Hijri -> {
        val provider = UmmAlQuraHijriDateProvider
        val date = provider.dateForEpochDay(anchorEpochDay)
        val index = date.year.toLong() * 12 + (date.month - 1) + offset
        val year = floorDiv(index, 12L).toInt()
        val month = floorMod(index, 12L).toInt() + 1
        provider.epochDayFor(HijriDate(year, month, 1))
    }
    SampleCalendar.SolarHijri, SampleCalendar.SolarHijriNative ->
        SolarHijriDate.fromEpochDay(anchorEpochDay)
            .withDayOfMonth(1)
            .plusMonths(offset.toLong())
            .toEpochDay()
    SampleCalendar.Chinese -> ChineseDate.fromEpochDay(anchorEpochDay)
        .withDayOfMonth(1)
        .plusMonths(offset.toLong())
        .toEpochDay()
}

/** Month index arithmetic shared by the Hijri month stepping above. */
private fun floorDiv(value: Long, divisor: Long): Long {
    val quotient = value / divisor
    return if (value % divisor != 0L && (value xor divisor) < 0) quotient - 1 else quotient
}

private fun floorMod(value: Long, divisor: Long): Long = value - floorDiv(value, divisor) * divisor
