package com.develotter.calendarview.locale

import java.util.Locale

/** Converts this [Locale] into the [CalendarLocale] that selects the library's label tables. */
fun Locale.asCalendarLocale(): CalendarLocale = CalendarLocale(toLanguageTag())
