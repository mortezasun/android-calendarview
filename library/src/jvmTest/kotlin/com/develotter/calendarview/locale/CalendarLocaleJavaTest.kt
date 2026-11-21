package com.develotter.calendarview.locale

import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CalendarLocaleJavaTest {
    @Test
    fun javaLocaleConvertsThroughItsLanguageTag() {
        assertEquals("de-DE", Locale.GERMANY.asCalendarLocale().languageTag)
        assertEquals("de", Locale.GERMANY.asCalendarLocale().language)
        val afghanPersian = Locale.forLanguageTag("fa-AF").asCalendarLocale()
        assertEquals("fa-AF", afghanPersian.languageTag)
        assertEquals("fa", afghanPersian.language)
        assertTrue(afghanPersian.isRightToLeft)
        val austrianGerman = Locale.forLanguageTag("de-AT").asCalendarLocale()
        assertEquals("de-AT", austrianGerman.languageTag)
        assertEquals("de", austrianGerman.language)
    }

    /** A root locale has no language subtag, so java.util.Locale reports it as `und`. */
    @Test
    fun rootLocaleHasNoLanguage() {
        val root = Locale.ROOT.asCalendarLocale()
        assertEquals("und", root.languageTag)
        assertEquals("und", root.language)
    }

    @Test
    fun systemDefaultFollowsTheJvmDefault() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("ar"))
            assertEquals(CalendarLocale.Arabic, CalendarLocale.systemDefault())
            Locale.setDefault(Locale.GERMANY)
            assertEquals("de", CalendarLocale.systemDefault().language)
            Locale.setDefault(Locale.ROOT)
            assertEquals("und", CalendarLocale.systemDefault().language)
        } finally {
            Locale.setDefault(previous)
        }
    }
}
