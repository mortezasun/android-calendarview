package com.develotter.calendarview.calendars.chineseCompose

import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import kotlin.math.floor
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarIdentifierGregorian
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterFullStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970

internal actual object ChinesePlatform {
    private const val SECONDS_PER_DAY = 86_400.0

    actual fun currentEpochDay(): Long =
        floor(NSDate().timeIntervalSince1970 / SECONDS_PER_DAY).toLong()

    actual fun defaultFirstDayOfWeek(): Int {
        val firstWeekday = NSCalendar(calendarIdentifier = NSCalendarIdentifierGregorian).apply {
            locale = NSLocale.currentLocale
        }.firstWeekday.toInt()
        // NSCalendar.firstWeekday is 1-indexed starting at Sunday (1 = Sun .. 7 = Sat).
        return if (firstWeekday == 1) 7 else firstWeekday - 1
    }

    actual fun formatMonthYear(year: Int, month: Int): String {
        val formatter = NSDateFormatter().apply {
            locale = NSLocale.currentLocale
            dateFormat = "LLLL yyyy"
        }
        return formatter.stringFromDate(NSDate.dateWithTimeIntervalSince1970(epochDayOfFirst(year, month) * SECONDS_PER_DAY))
    }

    actual fun formatFullDate(epochDay: Long): String {
        val formatter = NSDateFormatter().apply {
            locale = NSLocale.currentLocale
            dateStyle = NSDateFormatterFullStyle
            timeStyle = NSDateFormatterNoStyle
        }
        return formatter.stringFromDate(NSDate.dateWithTimeIntervalSince1970(epochDay * SECONDS_PER_DAY))
    }

    actual fun formatDayNumber(value: Int): String {
        val formatter = NSNumberFormatter().apply {
            locale = NSLocale.currentLocale
            usesGroupingSeparator = false
        }
        return formatter.stringFromNumber(NSNumber(value)) ?: value.toString()
    }

    actual fun weekdayShortLabel(isoWeekday: Int): String {
        val calendar = NSCalendar(calendarIdentifier = NSCalendarIdentifierGregorian).apply {
            locale = NSLocale.currentLocale
        }
        val symbols = calendar.shortStandaloneWeekdaySymbols
        val index = if (isoWeekday == 7) 0 else isoWeekday
        return (symbols.getOrNull(index) as? String) ?: isoWeekday.toString()
    }

    /** First epoch day of a Gregorian month, used to format its caption. */
    private fun epochDayOfFirst(year: Int, month: Int): Long =
        GregorianYearMonth.gregorianToEpochDay(year, month, 1)
}
