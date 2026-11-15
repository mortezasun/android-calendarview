package com.develotter.calendarview.compose

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarIdentifierGregorian
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterFullStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterPercentStyle
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970
import kotlin.math.floor

internal actual object HeatmapPlatformFormatter {
    private const val SECONDS_PER_DAY = 86_400.0

    actual fun currentEpochDay(): Long {
        val seconds = NSDate().timeIntervalSince1970
        return floor(seconds / SECONDS_PER_DAY).toLong()
    }

    actual fun formatMonthYear(epochDay: Long): String {
        val formatter = NSDateFormatter().apply {
            locale = NSLocale.currentLocale
            dateFormat = "LLLL yyyy"
        }
        return formatter.stringFromDate(NSDate.dateWithTimeIntervalSince1970(epochDay * SECONDS_PER_DAY))
    }

    actual fun formatFullDate(epochDay: Long): String {
        val formatter = NSDateFormatter().apply {
            locale = NSLocale.currentLocale
            dateStyle = NSDateFormatterFullStyle
            timeStyle = NSDateFormatterNoStyle
        }
        return formatter.stringFromDate(NSDate.dateWithTimeIntervalSince1970(epochDay * SECONDS_PER_DAY))
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
}
