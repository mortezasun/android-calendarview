package com.develotter.calendarview.calendars.solarHijriNative

import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

internal actual object SolarHijriPlatform {
    actual fun currentEpochDay(): Long = LocalDate.now().toEpochDay()

    actual fun defaultFirstDayOfWeek(): Int = WeekFields.of(Locale.getDefault()).firstDayOfWeek.value

    actual fun formatDayNumber(day: Int): String {
        val nf = NumberFormat.getIntegerInstance(Locale.getDefault()).apply { isGroupingUsed = false }
        return nf.format(day)
    }

    actual fun weekdayShortLabel(isoWeekday: Int): String {
        return DayOfWeek.of(isoWeekday).getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault())
    }
}
