package com.develotter.calendarview.calendars.solarHijriNative.compose

import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.locale.CalendarLocale
import kotlin.test.Test
import kotlin.test.assertEquals

class SolarHijriLocaleLabelsTest {
    private val farvardin1404 = SolarHijriDate(1404, 1, 1)

    @Test
    fun explicitLocaleSelectsTheMonthTable() {
        assertEquals(
            "فروردین ۱۴۰۴",
            solarHijriNativeMonthTitle(farvardin1404, CalendarLocale.Persian),
        )
        assertEquals(
            "فروردين ١٤٠٤",
            solarHijriNativeMonthTitle(farvardin1404, CalendarLocale.Arabic),
        )
        assertEquals(
            "Farvardin 1404",
            solarHijriNativeMonthTitle(farvardin1404, CalendarLocale.German),
        )
    }

    @Test
    fun nullLocaleKeepsThePlatformLocale() {
        assertEquals(
            solarHijriNativeMonthTitle(farvardin1404),
            solarHijriNativeMonthTitle(farvardin1404, null),
        )
    }
}
