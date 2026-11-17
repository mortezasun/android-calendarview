package com.develotter.calendarview.calendars.hijri

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarIdentifierGregorian
import platform.Foundation.NSCalendarIdentifierIslamicUmmAlQura
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterFullStyle
import platform.Foundation.NSDateFormatterLongStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSTimeZone
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.localTimeZone
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.timeZoneForSecondsFromGMT
import kotlin.math.floor

internal actual object HijriPlatform {
    private const val SECONDS_PER_DAY = 86_400.0
    private const val NOON_SECONDS = 43_200.0

    private fun utcTimeZone(): NSTimeZone = NSTimeZone.timeZoneForSecondsFromGMT(0)

    private fun ummAlQuraUtcCalendar(): NSCalendar =
        NSCalendar(calendarIdentifier = NSCalendarIdentifierIslamicUmmAlQura).apply {
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

    actual fun fromEpochDay(epochDay: Long): HijriDate {
        val comps = ummAlQuraUtcCalendar().components(
            NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
            fromDate = utcNoonDate(epochDay),
        )
        return HijriDate(
            year = comps.year.toInt(),
            month = comps.month.toInt(),
            dayOfMonth = comps.day.toInt(),
        )
    }

    actual fun toEpochDay(date: HijriDate): Long {
        val cal = ummAlQuraUtcCalendar()
        val comps = NSDateComponents().apply {
            timeZone = utcTimeZone()
            year = date.year.toLong()
            month = date.month.toLong()
            day = date.dayOfMonth.toLong()
            hour = 12
        }
        val nsDate = cal.dateFromComponents(comps)
            ?: error("Invalid HijriDate: $date")
        return floor(nsDate.timeIntervalSince1970 / SECONDS_PER_DAY).toLong()
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun lengthOfMonth(year: Int, month: Int): Int {
        val cal = ummAlQuraUtcCalendar()
        val comps = NSDateComponents().apply {
            timeZone = utcTimeZone()
            this.year = year.toLong()
            this.month = month.toLong()
            this.day = 1
            hour = 12
        }
        val nsDate = cal.dateFromComponents(comps) ?: error("Invalid Hijri year/month: $year-$month")
        return cal.rangeOfUnit(NSCalendarUnitDay, inUnit = NSCalendarUnitMonth, forDate = nsDate)
            .useContents { length.toInt() }
    }

    actual fun formatMonthYear(year: Int, month: Int): String {
        val epochDay = toEpochDay(HijriDate(year, month, 1))
        val formatter = NSDateFormatter().apply {
            calendar = ummAlQuraUtcCalendar()
            locale = NSLocale.currentLocale
            timeZone = utcTimeZone()
            dateFormat = "LLLL yyyy"
        }
        return formatter.stringFromDate(utcNoonDate(epochDay))
    }

    actual fun formatFullDate(date: HijriDate, weekdayEpochDay: Long): String {
        val canonicalEpoch = toEpochDay(date)
        val cal = ummAlQuraUtcCalendar()
        val utc = utcTimeZone()
        val fullFormatter = NSDateFormatter().apply {
            calendar = cal
            locale = NSLocale.currentLocale
            timeZone = utc
            dateStyle = NSDateFormatterFullStyle
            timeStyle = NSDateFormatterNoStyle
        }
        val canonicalFull = fullFormatter.stringFromDate(utcNoonDate(canonicalEpoch))
        if (canonicalEpoch == weekdayEpochDay) return canonicalFull
        val weekdayFormatter = NSDateFormatter().apply {
            calendar = cal
            locale = NSLocale.currentLocale
            timeZone = utc
            dateFormat = "EEEE"
        }
        val wrongWeekday = weekdayFormatter.stringFromDate(utcNoonDate(canonicalEpoch))
        val rightWeekday = weekdayFormatter.stringFromDate(utcNoonDate(weekdayEpochDay))
        if (wrongWeekday != rightWeekday && canonicalFull.contains(wrongWeekday)) {
            return canonicalFull.replaceFirst(wrongWeekday, rightWeekday)
        }
        val longFormatter = NSDateFormatter().apply {
            calendar = cal
            locale = NSLocale.currentLocale
            timeZone = utc
            dateStyle = NSDateFormatterLongStyle
            timeStyle = NSDateFormatterNoStyle
        }
        return "$rightWeekday, ${longFormatter.stringFromDate(utcNoonDate(canonicalEpoch))}"
    }

    actual fun formatShortDayMonthYear(epochDay: Long): String {
        val formatter = NSDateFormatter().apply {
            calendar = ummAlQuraUtcCalendar()
            locale = NSLocale.currentLocale
            timeZone = utcTimeZone()
            dateFormat = "d MMM y G"
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
        val symbols = ummAlQuraUtcCalendar().shortStandaloneWeekdaySymbols
        val index = if (isoWeekday == 7) 0 else isoWeekday
        return (symbols.getOrNull(index) as? String) ?: isoWeekday.toString()
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
