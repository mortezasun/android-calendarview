package com.develotter.calendarview.calendars.solarHijri.compose

import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.locale.CalendarLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SolarHijriDelegateLocaleTest {
    private val farvardin1404 = SolarHijriDate(1404, 1, 1)

    @Test
    fun monthTitlesUseTheLanguageSpellingAndDigits() {
        assertEquals("Farvardin 1404", solarHijriMonthTitle(farvardin1404, CalendarLocale.English))
        assertEquals("Farvardin 1404", solarHijriMonthTitle(farvardin1404, CalendarLocale.German))
        assertEquals("فروردین ۱۴۰۴", solarHijriMonthTitle(farvardin1404, CalendarLocale.Persian))
        assertEquals("فروردين ١٤٠٤", solarHijriMonthTitle(farvardin1404, CalendarLocale.Arabic))
    }

    @Test
    fun everyMonthTitleIsNonBlankForEveryLanguage() {
        for (tag in listOf("en", "de", "fa", "ar", "fr", "es", "zh")) {
            for (month in 1..12) {
                val title = solarHijriMonthTitle(SolarHijriDate(1404, month, 1), CalendarLocale(tag))
                assertTrue(title.isNotBlank(), "blank title for $tag/$month")
                assertTrue(title.none { it == '$' || it == '{' || it == '?' }, title)
            }
        }
    }

    @Test
    fun accessibilityLabelUsesThePersianSeparator() {
        val label = solarHijriAccessibilityLabel(farvardin1404.toEpochDay(), CalendarLocale.Persian)
        assertTrue(label != null && "،" in label && "۱۴۰۴" in label, label)
    }

    @Test
    fun unsupportedLanguageFallsBackToEnglish() {
        assertEquals(
            "Farvardin 1404",
            solarHijriMonthTitle(farvardin1404, CalendarLocale("tr")),
        )
    }

    @Test
    fun newLanguagesFormatTheMonthTitle() {
        assertEquals("Farvardin 1404", solarHijriMonthTitle(farvardin1404, CalendarLocale.French))
        assertEquals("Farvardin de 1404", solarHijriMonthTitle(farvardin1404, CalendarLocale.Spanish))
        assertEquals("1404年法尔瓦丁月", solarHijriMonthTitle(farvardin1404, CalendarLocale.Chinese))
    }
}
