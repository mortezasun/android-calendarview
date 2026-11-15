package com.develotter.calendarview.calendars.chineseCompose

internal expect object ChinesePlatform {
    fun currentEpochDay(): Long
    fun defaultFirstDayOfWeek(): Int
    fun formatMonthYear(year: Int, month: Int): String
    fun formatFullDate(epochDay: Long): String
    fun formatDayNumber(value: Int): String
    fun weekdayShortLabel(isoWeekday: Int): String
}
