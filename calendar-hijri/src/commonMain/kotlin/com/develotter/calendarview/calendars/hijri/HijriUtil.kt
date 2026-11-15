package com.develotter.calendarview.calendars.hijri

import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate

fun HijrahDate.day(): Int {

    return this.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
}
fun HijrahDate. year(): Int {
    return this.get(java.time.temporal.ChronoField.YEAR)

}
fun HijrahDate.month(): Int {
    return this.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)

}
fun LocalDate.toHijri(): HijrahDate {

    return  HijrahChronology.INSTANCE.date(this)

}
fun HijrahDate.toLocalDate(): LocalDate {

    return  LocalDate.from(this)

}