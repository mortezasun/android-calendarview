package com.develotter.calendarview.locale

import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class CalendarLocaleJavaTest {
    @Test
    fun javaLocaleConvertsThroughItsLanguageTag() {
        assertEquals("de-DE", Locale.GERMANY.asCalendarLocale().languageTag)
        assertEquals("de", Locale.GERMANY.asCalendarLocale().language)
        assertEquals("fa-AF", Locale.forLanguageTag("fa-AF").asCalendarLocale().languageTag)
    }

    @Test
    fun systemDefaultFollowsTheJvmDefault() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("ar"))
            assertEquals(CalendarLocale.Arabic, CalendarLocale.systemDefault())
        } finally {
            Locale.setDefault(previous)
        }
    }
}
