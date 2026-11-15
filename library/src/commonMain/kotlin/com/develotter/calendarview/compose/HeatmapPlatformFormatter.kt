package com.develotter.calendarview.compose

internal expect object HeatmapPlatformFormatter {
    fun currentEpochDay(): Long
    fun formatMonthYear(epochDay: Long): String
    fun formatFullDate(epochDay: Long): String
    fun weekdayShortLabel(isoWeekday: Int): String
    fun formatPercent(value: Float): String
}
