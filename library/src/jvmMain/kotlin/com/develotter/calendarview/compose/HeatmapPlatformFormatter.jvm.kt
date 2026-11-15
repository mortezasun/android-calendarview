package com.develotter.calendarview.compose

import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

internal actual object HeatmapPlatformFormatter {
    actual fun currentEpochDay(): Long = LocalDate.now().toEpochDay()

    actual fun formatMonthYear(epochDay: Long): String {
        val formatter = DateTimeFormatter.ofPattern("LLLL uuuu", Locale.getDefault())
        return LocalDate.ofEpochDay(epochDay).format(formatter)
    }

    actual fun formatFullDate(epochDay: Long): String {
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.getDefault())
        return LocalDate.ofEpochDay(epochDay).format(formatter)
    }

    actual fun weekdayShortLabel(isoWeekday: Int): String {
        return DayOfWeek.of(isoWeekday).getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault())
    }

    actual fun formatPercent(value: Float): String {
        return NumberFormat.getPercentInstance(Locale.getDefault()).format(value)
    }
}
