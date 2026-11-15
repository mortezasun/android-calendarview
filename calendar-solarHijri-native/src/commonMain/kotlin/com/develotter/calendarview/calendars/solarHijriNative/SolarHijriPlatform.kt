package com.develotter.calendarview.calendars.solarHijriNative

internal expect object SolarHijriPlatform {
    fun currentEpochDay(): Long
    fun defaultFirstDayOfWeek(): Int
    fun formatDayNumber(day: Int): String
    fun weekdayShortLabel(isoWeekday: Int): String
}
