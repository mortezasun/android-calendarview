package com.develotter.calendarview.calendars.solarHijri

import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate

/** Absolute epoch day of [date] on the shared timeline. */
fun solarHijriEpochDay(date: SolarHijriDate): Long = date.toEpochDay()

/** Validated Solar Hijri date from its fields; throws for invalid or unsupported input. */
fun solarHijriToEpochDay(year: Int, month: Int, day: Int): Long =
    SolarHijriDate(year, month, day).toEpochDay()

/** Solar Hijri date of [epochDay] within the engine's supported range. */
fun epochDayToSolarHijri(epochDay: Long): SolarHijriDate = SolarHijriDate.fromEpochDay(epochDay)
