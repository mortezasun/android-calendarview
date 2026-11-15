package com.develotter.calendarview.calendars.hijri

import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import java.util.Locale

internal actual object HijriPlatform {
    actual fun currentEpochDay(): Long = LocalDate.now().toEpochDay()

    actual fun fromEpochDay(epochDay: Long): HijriDate {
        val h = HijrahChronology.INSTANCE.dateEpochDay(epochDay)
        return HijriDate(
            year = h.get(ChronoField.YEAR_OF_ERA),
            month = h.get(ChronoField.MONTH_OF_YEAR),
            dayOfMonth = h.get(ChronoField.DAY_OF_MONTH),
        )
    }

    actual fun toEpochDay(date: HijriDate): Long {
        return HijrahDate.of(date.year, date.month, date.dayOfMonth).toEpochDay()
    }

    actual fun lengthOfMonth(year: Int, month: Int): Int {
        return HijrahDate.of(year, month, 1).lengthOfMonth()
    }

    actual fun formatMonthYear(year: Int, month: Int): String {
        val formatter = DateTimeFormatter.ofPattern("LLLL uuuu", Locale.getDefault())
            .withChronology(HijrahChronology.INSTANCE)
        return HijrahDate.of(year, month, 1).format(formatter)
    }

    actual fun formatFullDate(epochDay: Long): String {
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
            .withLocale(Locale.getDefault())
            .withChronology(HijrahChronology.INSTANCE)
        return HijrahChronology.INSTANCE.dateEpochDay(epochDay).format(formatter)
    }

    actual fun formatShortDayMonthYear(epochDay: Long): String {
        val formatter = DateTimeFormatter.ofPattern("d MMM y G", Locale.getDefault())
        return HijrahChronology.INSTANCE.dateEpochDay(epochDay).format(formatter)
    }

    actual fun formatDayNumber(day: Int): String {
        val nf = NumberFormat.getIntegerInstance(Locale.getDefault()).apply { isGroupingUsed = false }
        return nf.format(day)
    }

    actual fun weekdayShortLabel(isoWeekday: Int): String {
        return DayOfWeek.of(isoWeekday).getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault())
    }
}
