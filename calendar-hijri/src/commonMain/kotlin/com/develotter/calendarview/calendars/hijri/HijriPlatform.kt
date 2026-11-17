package com.develotter.calendarview.calendars.hijri

internal expect object HijriPlatform {
    fun currentEpochDay(): Long
    fun fromEpochDay(epochDay: Long): HijriDate
    fun toEpochDay(date: HijriDate): Long
    fun lengthOfMonth(year: Int, month: Int): Int
    fun formatMonthYear(year: Int, month: Int): String
    fun formatShortMonth(year: Int, month: Int): String
    fun formatFullDate(date: HijriDate, weekdayEpochDay: Long): String
    fun formatShortDayMonthYear(epochDay: Long): String
    fun formatDayNumber(day: Int): String
    fun weekdayShortLabel(isoWeekday: Int): String
}
