package com.develotter.calendarview.calendars.solarHijri

import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import java.time.LocalDate

/** The same calendar day as a [LocalDate]. */
fun SolarHijriDate.toLocalDate(): LocalDate = LocalDate.ofEpochDay(toEpochDay())

/** Solar Hijri date of the same calendar day as [this]. */
fun LocalDate.toSolarHijri(): SolarHijriDate = SolarHijriDate.fromEpochDay(toEpochDay())

/** Gregorian date of a validated Solar Hijri date. */
fun solarHijriToLocalDate(year: Int, month: Int, day: Int): LocalDate =
    SolarHijriDate(year, month, day).toLocalDate()

/** Solar Hijri year, month and day of [this]. */
fun LocalDate.toSolarHijriArray(): IntArray = toSolarHijri().let { intArrayOf(it.year, it.month, it.day) }
