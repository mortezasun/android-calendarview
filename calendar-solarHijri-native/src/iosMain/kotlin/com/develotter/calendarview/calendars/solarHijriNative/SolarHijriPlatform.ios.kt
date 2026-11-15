package com.develotter.calendarview.calendars.solarHijriNative

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarIdentifierPersian
import platform.Foundation.NSDate
import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.currentLocale
import platform.Foundation.timeIntervalSince1970
import kotlin.math.floor

internal actual object SolarHijriPlatform {
    private const val SECONDS_PER_DAY = 86_400.0

    actual fun currentEpochDay(): Long =
        floor(NSDate().timeIntervalSince1970 / SECONDS_PER_DAY).toLong()

    actual fun defaultFirstDayOfWeek(): Int {
        val firstWeekday = NSCalendar(calendarIdentifier = NSCalendarIdentifierPersian).apply {
            locale = NSLocale.currentLocale
        }.firstWeekday.toInt()
        return if (firstWeekday == 1) 7 else firstWeekday - 1
    }

    actual fun formatDayNumber(day: Int): String {
        val formatter = NSNumberFormatter().apply {
            locale = NSLocale.currentLocale
            usesGroupingSeparator = false
        }
        return formatter.stringFromNumber(NSNumber(day)) ?: day.toString()
    }

    actual fun weekdayShortLabel(isoWeekday: Int): String {
        val calendar = NSCalendar(calendarIdentifier = NSCalendarIdentifierPersian).apply {
            locale = NSLocale.currentLocale
        }
        val symbols = calendar.shortStandaloneWeekdaySymbols
        val index = if (isoWeekday == 7) 0 else isoWeekday
        return (symbols.getOrNull(index) as? String) ?: isoWeekday.toString()
    }
}
