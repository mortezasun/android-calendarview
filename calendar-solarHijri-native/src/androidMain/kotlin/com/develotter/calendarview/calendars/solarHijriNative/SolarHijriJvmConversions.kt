package com.develotter.calendarview.calendars.solarHijriNative

import java.time.LocalDate

/** Platform conversion retained for Android consumers of the pre-KMP API. */
fun SolarHijriDate.toLocalDate(): LocalDate = LocalDate.ofEpochDay(toEpochDay())

/** Platform conversion retained for Android consumers of the pre-KMP API. */
fun LocalDate.toSolarHijri(): SolarHijriDate = SolarHijriDate.fromEpochDay(toEpochDay())
