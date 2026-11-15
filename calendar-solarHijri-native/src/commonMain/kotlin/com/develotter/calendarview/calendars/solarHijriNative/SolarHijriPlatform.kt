package com.develotter.calendarview.calendars.solarHijriNative

/**
 * Platform hooks for the Solar Hijri engine: the current day, the default locale's language and
 * country, and localized digits and weekday names. Kept internal so the public engine stays
 * dependency free.
 */
internal expect object SolarHijriPlatform {
    /** Absolute epoch day of today in the platform's default time zone. */
    fun currentEpochDay(): Long

    /** ISO 639 language code of the default locale, for example `fa` or `prs`. */
    fun localeLanguage(): String

    /** ISO 3166 country code of the default locale, for example `AF`, or an empty string. */
    fun localeCountry(): String

    /** Formats [value] without digit grouping using the default locale's digits. */
    fun formatDayNumber(value: Int): String

    /** Short standalone weekday name for an ISO weekday (`1` = Monday .. `7` = Sunday). */
    fun weekdayShortLabel(isoWeekday: Int): String

    /** Full weekday name for an ISO weekday (`1` = Monday .. `7` = Sunday). */
    fun weekdayFullLabel(isoWeekday: Int): String
}
