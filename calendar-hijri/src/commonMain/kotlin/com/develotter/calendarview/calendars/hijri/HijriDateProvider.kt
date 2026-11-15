package com.develotter.calendarview.calendars.hijri

import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate

/**
 * Maps absolute epoch days to Hijri dates and back.
 *
 * Implement this to model a local moon-sighting convention or another Hijri method. The
 * provider must round-trip its own dates: [dateForEpochDay] and [epochDayFor] must agree.
 * Calendar arithmetic stays on [HijrahDate], so month lengths and year boundaries follow the
 * supplied chronology.
 */
interface HijriDateProvider {
    /** Returns the Hijri date displayed for [epochDay]. */
    fun dateForEpochDay(epochDay: Long): HijrahDate

    /** Returns the absolute epoch day on which [date] starts. */
    fun epochDayFor(date: HijrahDate): Long
}

/**
 * The platform `HijrahDate` chronology (Umm al-Qura on Android).
 *
 * This is a tabular calendar, not local moon sighting. Use [OffsetHijriDateProvider] or a
 * custom provider when the host community follows a different method.
 */
object UmmAlQuraHijriDateProvider : HijriDateProvider {
    override fun dateForEpochDay(epochDay: Long): HijrahDate =
        HijrahDate.from(HijrahChronology.INSTANCE.dateEpochDay(epochDay))

    override fun epochDayFor(date: HijrahDate): Long = date.toEpochDay()
}

/**
 * Shifts every displayed Hijri date by [offsetDays] relative to [UmmAlQuraHijriDateProvider].
 *
 * A positive offset means the local community is ahead of Umm al-Qura: epoch day `d` shows
 * the Umm al-Qura date for `d + offsetDays`. A negative offset means it is behind. The shift
 * is uniform, so it models a declared monthly adjustment, not astronomical moon sighting.
 */
class OffsetHijriDateProvider(val offsetDays: Int = 0) : HijriDateProvider {
    override fun dateForEpochDay(epochDay: Long): HijrahDate =
        UmmAlQuraHijriDateProvider.dateForEpochDay(epochDay + offsetDays)

    override fun epochDayFor(date: HijrahDate): Long = date.toEpochDay() - offsetDays
}
