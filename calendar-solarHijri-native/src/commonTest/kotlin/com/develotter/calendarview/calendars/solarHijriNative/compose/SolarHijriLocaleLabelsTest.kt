package com.develotter.calendarview.calendars.solarHijriNative.compose

import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriLabels
import com.develotter.calendarview.locale.CalendarLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

    @Test
    fun everyLanguageHasCompleteNonPlaceholderTables() {
        for (tag in listOf("en", "de", "fa", "ar", "fr", "es", "zh")) {
            val locale = CalendarLocale(tag)
            val labels = buildList {
                for (month in 1..12) add(SolarHijriLabels.monthName(locale, month))
                for (weekday in 1..7) {
                    add(SolarHijriLabels.weekdayShort(locale, weekday))
                    add(SolarHijriLabels.weekdayFull(locale, weekday))
                }
            }
            labels.forEach { label ->
                assertTrue(label.isNotBlank(), "blank label for $tag")
                assertTrue(label.none { it == '$' || it == '{' || it == '?' }, label)
            }
            val digits = SolarHijriLabels.number(locale, 1234567890)
            assertEquals(10, digits.toSet().size, "digits for $tag")
        }
    }

    @Test
    fun dariTagSelectsAfghanMonthNames() {
        assertEquals("حمل ۱۴۰۴", solarHijriNativeMonthTitle(farvardin1404, CalendarLocale("fa-AF")))
    }

    @Test
    fun unsupportedLanguageFallsBackToEnglishLabelsAndWesternDigits() {
        val turkish = CalendarLocale("tr")
        assertEquals("Farvardin 1404", solarHijriNativeMonthTitle(farvardin1404, turkish))
        assertEquals("Sun", SolarHijriLabels.weekdayShort(turkish, 7))
        assertEquals("1404", SolarHijriLabels.number(turkish, 1404))
    }

    @Test
    fun frenchSpanishAndChineseFollowTheirDateConventions() {
        // 1 Farvardin 1404 is Friday, 2025-03-21.
        assertEquals("Farvardin 1404", solarHijriNativeMonthTitle(farvardin1404, CalendarLocale.French))
        assertEquals("Farvardin de 1404", solarHijriNativeMonthTitle(farvardin1404, CalendarLocale.Spanish))
        assertEquals("1404年法尔瓦丁月", solarHijriNativeMonthTitle(farvardin1404, CalendarLocale.Chinese))
        assertEquals("vendredi 1 Farvardin 1404", SolarHijriLabels.fullDate(CalendarLocale.French, farvardin1404))
        assertEquals(
            "viernes, 1 de Farvardin de 1404",
            SolarHijriLabels.fullDate(CalendarLocale.Spanish, farvardin1404),
        )
        assertEquals("1404年法尔瓦丁月1日星期五", SolarHijriLabels.fullDate(CalendarLocale.Chinese, farvardin1404))
    }
}
