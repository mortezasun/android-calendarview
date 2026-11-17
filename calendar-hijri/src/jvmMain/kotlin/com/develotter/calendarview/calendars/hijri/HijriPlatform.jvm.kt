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
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalField
import java.time.temporal.TemporalQueries
import java.time.temporal.TemporalQuery
import java.time.temporal.ValueRange
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

    actual fun formatShortMonth(year: Int, month: Int): String {
        val formatter = DateTimeFormatter.ofPattern("LLL", Locale.getDefault())
            .withChronology(HijrahChronology.INSTANCE)
        return HijrahDate.of(year, month, 1).format(formatter)
    }

    actual fun formatFullDate(date: HijriDate, weekdayEpochDay: Long): String {
        val hijrahDate = HijrahDate.of(date.year, date.month, date.dayOfMonth)
        val actualWeekday = LocalDate.ofEpochDay(weekdayEpochDay).dayOfWeek.value.toLong()
        val accessor = object : TemporalAccessor {
            override fun isSupported(field: TemporalField): Boolean = hijrahDate.isSupported(field)

            override fun range(field: TemporalField): ValueRange = hijrahDate.range(field)

            override fun getLong(field: TemporalField): Long =
                if (field == ChronoField.DAY_OF_WEEK) actualWeekday else hijrahDate.getLong(field)

            @Suppress("UNCHECKED_CAST")
            override fun <R> query(query: TemporalQuery<R>): R =
                when (query) {
                    TemporalQueries.chronology() -> HijrahChronology.INSTANCE as R
                    TemporalQueries.precision() -> ChronoUnit.DAYS as R
                    else -> super.query(query)
                }
        }
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
            .withLocale(Locale.getDefault())
            .withChronology(HijrahChronology.INSTANCE)
        return formatter.format(accessor)
    }

    actual fun formatShortDayMonthYear(epochDay: Long): String {
        val formatter = DateTimeFormatter.ofPattern("d MMM y G", Locale.getDefault())
            .withChronology(HijrahChronology.INSTANCE)
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
