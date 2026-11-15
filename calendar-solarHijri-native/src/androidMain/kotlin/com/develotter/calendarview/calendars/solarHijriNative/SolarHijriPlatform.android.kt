package com.develotter.calendarview.calendars.solarHijriNative

import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

internal actual object SolarHijriPlatform {
    actual fun currentEpochDay(): Long = LocalDate.now().toEpochDay()

    actual fun localeLanguage(): String = Locale.getDefault().language

    actual fun localeCountry(): String = Locale.getDefault().country

    actual fun formatDayNumber(value: Int): String =
        NumberFormat.getIntegerInstance(Locale.getDefault()).apply { isGroupingUsed = false }.format(value)

    actual fun weekdayShortLabel(isoWeekday: Int): String =
        DayOfWeek.of(isoWeekday).getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault())

    actual fun weekdayFullLabel(isoWeekday: Int): String =
        DayOfWeek.of(isoWeekday).getDisplayName(TextStyle.FULL, Locale.getDefault())
}
