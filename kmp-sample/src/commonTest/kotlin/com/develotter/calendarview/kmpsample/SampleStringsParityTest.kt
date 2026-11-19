package com.develotter.calendarview.kmpsample

import com.develotter.calendarview.locale.CalendarLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SampleStringsParityTest {
    private val english = CalendarLocale.English

    /** Every shell string for [language], keyed by a stable name. */
    private fun allStrings(language: CalendarLocale): Map<String, String> = buildMap {
        put("appTitle", SampleStrings.appTitle(language))
        put("menu", SampleStrings.menu(language))
        put("close", SampleStrings.close(language))
        put("settingsTitle", SampleStrings.settingsTitle(language))
        put("languageLabel", SampleStrings.languageLabel(language))
        put("themeLabel", SampleStrings.themeLabel(language))
        put("paletteLabel", SampleStrings.paletteLabel(language))
        DemoPage.entries.forEach { put("page.${it.name}", SampleStrings.pageTitle(it, language)) }
        AppThemeMode.entries.forEach { put("mode.${it.name}", SampleStrings.themeModeName(it, language)) }
        AppThemePalette.entries.forEach { put("palette.${it.name}", SampleStrings.paletteName(it, language)) }
        CalendarLocale.supportedLanguages.forEach {
            put("languageName.$it", SampleStrings.languageName(CalendarLocale(it)))
        }
    }

    @Test
    fun everySupportedLanguageHasTheSameNonBlankKeys() {
        val reference = allStrings(english).keys
        for (tag in CalendarLocale.supportedLanguages) {
            val strings = allStrings(CalendarLocale(tag))
            assertEquals(reference, strings.keys, "keys for $tag")
            strings.forEach { (key, value) ->
                assertTrue(value.isNotBlank(), "blank string $key for $tag")
            }
        }
    }

    @Test
    fun nonEnglishLanguagesTranslateTheShellCopy() {
        // Words that are spelled the same in German and English.
        val sameInGerman = setOf("appTitle", "page.Heatmaps", "mode.System")
        val englishStrings = allStrings(english)
        for (tag in listOf("de", "fa", "ar")) {
            allStrings(CalendarLocale(tag)).forEach { (key, value) ->
                if (key.startsWith("languageName.")) return@forEach
                if (tag == "de" && key in sameInGerman) return@forEach
                if (tag != "de" && key == "appTitle") return@forEach
                assertNotEquals(englishStrings.getValue(key), value, "$key is untranslated for $tag")
            }
        }
    }
}
