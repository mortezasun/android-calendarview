package com.develotter.calendarview.locale

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CalendarLocaleTest {
    @Test
    fun languageIsTheLowercasePrimarySubtag() {
        assertEquals("de", CalendarLocale("de-DE").language)
        assertEquals("fa", CalendarLocale("fa-AF").language)
        assertEquals("zh", CalendarLocale("zh-Hant-TW").language)
        assertEquals("ar", CalendarLocale("AR").language)
        assertEquals("de", CalendarLocale("DE-at").language)
        assertEquals("und", CalendarLocale("und").language)
    }

    @Test
    fun emptyTagHasAnEmptyLanguage() {
        assertEquals("", CalendarLocale("").language)
    }

    @Test
    fun predefinedLocalesMatchTheSupportedLanguages() {
        assertEquals(
            CalendarLocale.supportedLanguages,
            listOf(
                CalendarLocale.English,
                CalendarLocale.German,
                CalendarLocale.Persian,
                CalendarLocale.Arabic,
                CalendarLocale.French,
                CalendarLocale.Spanish,
                CalendarLocale.Chinese,
            ).map { it.language },
        )
    }

    /**
     * The label tables live in the engine modules, so this is the only fallback statement `:library`
     * itself can make: an unsupported tag is outside [CalendarLocale.supportedLanguages] and every
     * engine therefore renders English labels and Western digits for it.
     */
    @Test
    fun unsupportedTagIsOutsideTheSupportedLanguages() {
        val turkish = CalendarLocale("tr")
        assertEquals("tr", turkish.language)
        assertFalse(turkish.language in CalendarLocale.supportedLanguages)
        assertFalse(turkish.isRightToLeft)
        assertEquals("tr", CalendarLocale("tr-TR").language)
    }

    @Test
    fun systemDefaultReturnsAPlatformTag() {
        assertTrue(CalendarLocale.systemDefault().languageTag.isNotBlank())
    }

    @Test
    fun rightToLeftFollowsTheLanguageList() {
        assertTrue(CalendarLocale.Persian.isRightToLeft)
        assertTrue(CalendarLocale.Arabic.isRightToLeft)
        assertTrue(CalendarLocale("fa-AF").isRightToLeft)
        assertTrue(CalendarLocale("he").isRightToLeft)
        assertFalse(CalendarLocale.English.isRightToLeft)
        assertFalse(CalendarLocale.German.isRightToLeft)
        assertFalse(CalendarLocale("").isRightToLeft)
        assertFalse(CalendarLocale("zh-Hant-TW").isRightToLeft)
    }
}
