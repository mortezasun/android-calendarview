package com.develotter.calendarview.compose

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
import platform.Foundation.NSNumberFormatterPercentStyle
import platform.Foundation.NSTimeZone
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.localTimeZone
import platform.Foundation.timeZoneForSecondsFromGMT

internal actual object HeatmapPlatformFormatter {
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
        return gregorianToEpochDay(
            year = components.year.toInt(),
            month = components.month.toInt(),
            day = components.day.toInt(),
        )
    }

    actual fun formatMonthYear(epochDay: Long): String {
        val formatter = NSDateFormatter().apply {
            calendar = gregorianUtcCalendar()
            locale = NSLocale.currentLocale
            timeZone = utcTimeZone()
            dateFormat = "LLLL yyyy"
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

    actual fun weekdayShortLabel(isoWeekday: Int): String {
        val calendar = NSCalendar(calendarIdentifier = NSCalendarIdentifierGregorian).apply {
            locale = NSLocale.currentLocale
        }
        // NSCalendar shortStandaloneWeekdaySymbols is 0-indexed starting at Sunday (0 = Sun, 1 = Mon, ..., 6 = Sat).
        val symbols = calendar.shortStandaloneWeekdaySymbols
        val index = if (isoWeekday == 7) 0 else isoWeekday
        return (symbols.getOrNull(index) as? String) ?: isoWeekday.toString()
    }

    actual fun formatPercent(value: Float): String {
        val formatter = NSNumberFormatter().apply {
            locale = NSLocale.currentLocale
            numberStyle = NSNumberFormatterPercentStyle
        }
        return formatter.stringFromNumber(NSNumber(value)) ?: "${(value * 100).toInt()}%"
    }

    private fun gregorianToEpochDay(year: Int, month: Int, day: Int): Long {
        val y = year.toLong() - if (month <= 2) 1L else 0L
        val era = if (y >= 0) y / 400 else (y - 399) / 400
        val yoe = y - era * 400
        val m = month.toLong()
        val doy = (153 * (m + if (m > 2) -3 else 9) + 2) / 5 + day - 1
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        return era * 146097 + doe - 719468
    }
}
