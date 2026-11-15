package com.develotter.calendarview.calendars.solarHijriNative

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarIdentifierGregorian
import platform.Foundation.NSDate
import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.countryCode
import platform.Foundation.timeIntervalSince1970
import kotlin.math.floor

internal actual object SolarHijriPlatform {
    private const val SECONDS_PER_DAY = 86_400.0

    actual fun currentEpochDay(): Long =
        floor(NSDate().timeIntervalSince1970 / SECONDS_PER_DAY).toLong()

    actual fun localeLanguage(): String = NSLocale.currentLocale.languageCode ?: ""

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
        }
        val symbols = if (full) calendar.weekdaySymbols else calendar.shortStandaloneWeekdaySymbols
        val index = if (isoWeekday == 7) 0 else isoWeekday
        return (symbols.getOrNull(index) as? String) ?: isoWeekday.toString()
    }
}
