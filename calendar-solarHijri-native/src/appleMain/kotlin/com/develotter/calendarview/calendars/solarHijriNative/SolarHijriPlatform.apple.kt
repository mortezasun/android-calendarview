package com.develotter.calendarview.calendars.solarHijriNative

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarIdentifierGregorian
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSTimeZone
import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.localTimeZone
import platform.Foundation.timeZoneForSecondsFromGMT

internal actual object SolarHijriPlatform {
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

    actual fun localeLanguage(): String = NSLocale.currentLocale.languageCode

    actual fun localeCountry(): String = NSLocale.currentLocale.countryCode ?: ""

    actual fun formatDayNumber(value: Int): String {
        val formatter = NSNumberFormatter().apply {
            locale = NSLocale.currentLocale
            usesGroupingSeparator = false
        }
        return formatter.stringFromNumber(NSNumber(value)) ?: value.toString()
    }

    actual fun weekdayShortLabel(isoWeekday: Int): String = weekdayLabel(isoWeekday, full = false)

    actual fun weekdayFullLabel(isoWeekday: Int): String = weekdayLabel(isoWeekday, full = true)

    private fun weekdayLabel(isoWeekday: Int, full: Boolean): String {
        val calendar = NSCalendar(calendarIdentifier = NSCalendarIdentifierGregorian).apply {
            locale = NSLocale.currentLocale
            timeZone = NSTimeZone.timeZoneForSecondsFromGMT(0)
        }
        val symbols = if (full) calendar.weekdaySymbols else calendar.shortStandaloneWeekdaySymbols
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
