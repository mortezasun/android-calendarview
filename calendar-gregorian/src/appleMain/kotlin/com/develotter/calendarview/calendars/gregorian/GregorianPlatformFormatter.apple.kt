package com.develotter.calendarview.calendars.gregorian

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarIdentifierGregorian
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterFullStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSTimeZone
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.localTimeZone
import platform.Foundation.timeZoneForSecondsFromGMT

internal actual object GregorianPlatformFormatter {
    private const val SECONDS_PER_DAY = 86_400.0
    private const val NOON_SECONDS = 43_200.0

    private fun utcTimeZone(): NSTimeZone = NSTimeZone.timeZoneForSecondsFromGMT(0)

    private fun gregorianUtcCalendar(): NSCalendar =
        NSCalendar(calendarIdentifier = NSCalendarIdentifierGregorian).apply {
            locale = NSLocale.currentLocale
            timeZone = utcTimeZone()
        }

    private fun utcNoonDate(epochDay: Long): NSDate =
        NSDate.dateWithTimeIntervalSince1970(epochDay * SECONDS_PER_DAY + NOON_SECONDS)

    actual fun currentEpochDay(): Long {
        val localCalendar = NSCalendar(calendarIdentifier = NSCalendarIdentifierGregorian).apply {
            timeZone = NSTimeZone.localTimeZone
        }
        val components = localCalendar.components(
            NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
            fromDate = NSDate(),
        )
        return GregorianYearMonth.gregorianToEpochDay(
            year = components.year.toInt(),
            month = components.month.toInt(),
            day = components.day.toInt(),
        )
    }

    actual fun defaultFirstDayOfWeek(): Int {
        val firstWeekday = gregorianUtcCalendar().firstWeekday.toInt()
        // NSCalendar.firstWeekday is 1-indexed starting at Sunday (1 = Sun .. 7 = Sat).
        return if (firstWeekday == 1) 7 else firstWeekday - 1
    }

    actual fun formatMonthYear(year: Int, month: Int): String {
        val epochDay = GregorianYearMonth.gregorianToEpochDay(year, month, 1)
        val formatter = NSDateFormatter().apply {
            calendar = gregorianUtcCalendar()
            locale = NSLocale.currentLocale
            timeZone = utcTimeZone()
            dateFormat = "LLLL yyyy"
        }
        return formatter.stringFromDate(utcNoonDate(epochDay))
    }

    actual fun formatShortMonth(year: Int, month: Int): String {
        val epochDay = GregorianYearMonth.gregorianToEpochDay(year, month, 1)
        val formatter = NSDateFormatter().apply {
            calendar = gregorianUtcCalendar()
            locale = NSLocale.currentLocale
            timeZone = utcTimeZone()
            dateFormat = "LLL"
        }
        return formatter.stringFromDate(utcNoonDate(epochDay))
    }

    actual fun formatFullDate(epochDay: Long): String {
        val formatter = NSDateFormatter().apply {
            calendar = gregorianUtcCalendar()
            locale = NSLocale.currentLocale
            timeZone = utcTimeZone()
            dateStyle = NSDateFormatterFullStyle
            timeStyle = NSDateFormatterNoStyle
        }
        return formatter.stringFromDate(utcNoonDate(epochDay))
    }

    actual fun formatShortDayMonth(epochDay: Long): String {
        val formatter = NSDateFormatter().apply {
            calendar = gregorianUtcCalendar()
            locale = NSLocale.currentLocale
            timeZone = utcTimeZone()
            dateFormat = "d MMM"
        }
        return formatter.stringFromDate(utcNoonDate(epochDay))
    }

    actual fun formatShortDayMonthYear(epochDay: Long): String {
        val formatter = NSDateFormatter().apply {
            calendar = gregorianUtcCalendar()
            locale = NSLocale.currentLocale
            timeZone = utcTimeZone()
            dateFormat = "d MMM y"
        }
        return formatter.stringFromDate(utcNoonDate(epochDay))
    }

    actual fun formatDayNumber(day: Int): String {
        val formatter = NSNumberFormatter().apply {
            locale = NSLocale.currentLocale
            usesGroupingSeparator = false
        }
        return formatter.stringFromNumber(NSNumber(day)) ?: day.toString()
    }

    actual fun weekdayShortLabel(isoWeekday: Int): String {
        val symbols = gregorianUtcCalendar().shortStandaloneWeekdaySymbols
        val index = if (isoWeekday == 7) 0 else isoWeekday
        return (symbols.getOrNull(index) as? String) ?: isoWeekday.toString()
    }
}
