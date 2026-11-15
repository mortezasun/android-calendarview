package com.develotter.calendarview.calendars.hijri

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarIdentifierIslamicUmmAlQura
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterFullStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970
import kotlin.math.floor

internal actual object HijriPlatform {
    private const val SECONDS_PER_DAY = 86_400.0

    private fun ummAlQuraCalendar(): NSCalendar =
        NSCalendar(calendarIdentifier = NSCalendarIdentifierIslamicUmmAlQura).apply {
            locale = NSLocale.currentLocale
        }

    actual fun currentEpochDay(): Long =
        floor(NSDate().timeIntervalSince1970 / SECONDS_PER_DAY).toLong()

    actual fun fromEpochDay(epochDay: Long): HijriDate {
        val date = NSDate.dateWithTimeIntervalSince1970(epochDay * SECONDS_PER_DAY)
        val comps = ummAlQuraCalendar().components(
            NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
            fromDate = date,
        )
        return HijriDate(
            year = comps.year.toInt(),
            month = comps.month.toInt(),
            dayOfMonth = comps.day.toInt(),
        )
    }

    actual fun toEpochDay(date: HijriDate): Long {
        val comps = NSDateComponents().apply {
            year = date.year.toLong()
            month = date.month.toLong()
            day = date.dayOfMonth.toLong()
            hour = 12
        }
        val nsDate = ummAlQuraCalendar().dateFromComponents(comps)
            ?: error("Invalid HijriDate: $date")
        return floor(nsDate.timeIntervalSince1970 / SECONDS_PER_DAY).toLong()
    }

    actual fun lengthOfMonth(year: Int, month: Int): Int {
        val comps = NSDateComponents().apply {
            this.year = year.toLong()
            this.month = month.toLong()
            this.day = 1
            hour = 12
        }
        val cal = ummAlQuraCalendar()
        val nsDate = cal.dateFromComponents(comps) ?: error("Invalid Hijri year/month: $year-$month")
        return cal.rangeOfUnit(NSCalendarUnitDay, inUnit = NSCalendarUnitMonth, forDate = nsDate).length.toInt()
    }

    actual fun formatMonthYear(year: Int, month: Int): String {
        val epochDay = toEpochDay(HijriDate(year, month, 1))
        val formatter = NSDateFormatter().apply {
            calendar = ummAlQuraCalendar()
            locale = NSLocale.currentLocale
            dateFormat = "LLLL yyyy"
        }
        return formatter.stringFromDate(NSDate.dateWithTimeIntervalSince1970(epochDay * SECONDS_PER_DAY))
    }

    actual fun formatFullDate(epochDay: Long): String {
        val formatter = NSDateFormatter().apply {
            calendar = ummAlQuraCalendar()
            locale = NSLocale.currentLocale
            dateStyle = NSDateFormatterFullStyle
            timeStyle = NSDateFormatterNoStyle
        }
        return formatter.stringFromDate(NSDate.dateWithTimeIntervalSince1970(epochDay * SECONDS_PER_DAY))
    }

    actual fun formatDayNumber(day: Int): String {
        val formatter = NSNumberFormatter().apply {
            locale = NSLocale.currentLocale
            usesGroupingSeparator = false
        }
        return formatter.stringFromNumber(NSNumber(day)) ?: day.toString()
    }

    actual fun weekdayShortLabel(isoWeekday: Int): String {
        val symbols = ummAlQuraCalendar().shortStandaloneWeekdaySymbols
        val index = if (isoWeekday == 7) 0 else isoWeekday
        return (symbols.getOrNull(index) as? String) ?: isoWeekday.toString()
    }
}
