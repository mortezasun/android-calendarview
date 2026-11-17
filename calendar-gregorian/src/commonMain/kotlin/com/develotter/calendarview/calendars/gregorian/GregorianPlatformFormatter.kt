package com.develotter.calendarview.calendars.gregorian

internal expect object GregorianPlatformFormatter {
    fun currentEpochDay(): Long
    fun defaultFirstDayOfWeek(): Int
    fun formatMonthYear(year: Int, month: Int): String
    fun formatShortMonth(year: Int, month: Int): String
    fun formatFullDate(epochDay: Long): String
    fun formatShortDayMonth(epochDay: Long): String
    fun formatShortDayMonthYear(epochDay: Long): String
    fun formatDayNumber(day: Int): String
    fun weekdayShortLabel(isoWeekday: Int): String
}
