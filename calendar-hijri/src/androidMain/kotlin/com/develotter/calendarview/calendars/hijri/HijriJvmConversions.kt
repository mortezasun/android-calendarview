package com.develotter.calendarview.calendars.hijri

import java.time.LocalDate

/** Platform conversion retained for Android consumers of the pre-KMP API. */
fun LocalDate.toHijri(): HijriDate = HijriPlatform.fromEpochDay(toEpochDay())

/** Platform conversion retained for Android consumers of the pre-KMP API. */
fun HijriDate.toLocalDate(): LocalDate = LocalDate.ofEpochDay(HijriPlatform.toEpochDay(this))
