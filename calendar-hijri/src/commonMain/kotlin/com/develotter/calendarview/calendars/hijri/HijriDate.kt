package com.develotter.calendarview.calendars.hijri

import androidx.compose.runtime.Immutable

/**
 * Platform-independent Hijri date value type for KMP.
 */
@Immutable
data class HijriDate(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int,
) : Comparable<HijriDate> {
    init {
        require(month in 1..12) { "month must be in 1..12, got $month" }
        require(dayOfMonth in 1..30) { "dayOfMonth must be in 1..30, got $dayOfMonth" }
    }

    override fun compareTo(other: HijriDate): Int {
        val y = year.compareTo(other.year)
        if (y != 0) return y
        val m = month.compareTo(other.month)
        return if (m != 0) m else dayOfMonth.compareTo(other.dayOfMonth)
    }
}
