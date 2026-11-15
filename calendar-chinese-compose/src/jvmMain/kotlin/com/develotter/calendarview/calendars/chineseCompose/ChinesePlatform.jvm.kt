package com.develotter.calendarview.calendars.chineseCompose

import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

internal actual object ChinesePlatform {
    actual fun currentEpochDay(): Long = LocalDate.now().toEpochDay()

    actual fun defaultFirstDayOfWeek(): Int =
        WeekFields.of(Locale.getDefault()).firstDayOfWeek.value

    actual fun formatMonthYear(year: Int, month: Int): String =
        YearMonth.of(year, month).format(DateTimeFormatter.ofPattern("LLLL uuuu", Locale.getDefault()))

    actual fun formatFullDate(epochDay: Long): String =
        LocalDate.ofEpochDay(epochDay).format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.getDefault()),
        )

    actual fun formatDayNumber(value: Int): String =
        NumberFormat.getIntegerInstance(Locale.getDefault()).apply { isGroupingUsed = false }.format(value)

    actual fun weekdayShortLabel(isoWeekday: Int): String =
        DayOfWeek.of(isoWeekday).getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault())
}
