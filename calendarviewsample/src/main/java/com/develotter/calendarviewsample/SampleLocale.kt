package com.develotter.calendarviewsample

import com.develotter.calendarview.locale.CalendarLocale
import java.util.Locale

/**
 * Bridges the sample's locale selection onto the library label locale.
 *
 * The sample keeps a [Locale] for its own platform helpers (week fields, "today" text), while the
 * calendar engines format month names, weekday labels and digits from [CalendarLocale].
 */
internal fun Locale.asCalendarLocale(): CalendarLocale = CalendarLocale(toLanguageTag())
