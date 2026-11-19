package com.develotter.calendarview.calendars.hijri.compose

import com.develotter.calendarview.calendars.hijri.HijriDate
import com.develotter.calendarview.calendars.hijri.HijriLabels
import com.develotter.calendarview.locale.CalendarLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HijriLocaleLabelsTest {
    private val languages = listOf("en", "de", "fa", "ar").map(::CalendarLocale)
    private val rabiAlAwwal = HijriDate(1448, 3, 20)
    private val sundayEpochDay = 20758L // 2026-11-01

    @Test
    fun everyLanguageHasCompleteNonPlaceholderTables() {
        for (locale in languages) {
            val labels = buildList {
                for (month in 1..12) {
                    add(HijriLabels.monthName(locale, month))
                    add(HijriLabels.shortMonthName(locale, month))
                }
                for (weekday in 1..7) {
                    add(HijriLabels.weekdayShort(locale, weekday))
                    add(HijriLabels.weekdayFull(locale, weekday))
                }
            }
            assertEquals(12 * 2 + 7 * 2, labels.size)
            labels.forEach { label ->
                assertTrue(label.isNotBlank(), "blank label for ${locale.languageTag}")
                assertTrue(
                    label.none { it == '$' || it == '{' || it == '?' },
                    "placeholder in '$label' for ${locale.languageTag}",
                )
            }
            val digits = HijriLabels.number(locale, 1234567890)
            assertEquals(10, digits.length)
            assertEquals(10, digits.toSet().size)
        }
    }

    @Test
    fun monthTitlesUseTheLanguageSpellingAndDigits() {
        val month = HijriDate(1448, 3, 1)
        assertEquals("Rabi al-Awwal 1448", hijriMonthTitle(month, CalendarLocale.English))
        assertEquals("Rabi al-Awwal 1448", hijriMonthTitle(month, CalendarLocale.German))
        assertEquals("ربیع\u200cالاول ۱۴۴۸", hijriMonthTitle(month, CalendarLocale.Persian))
        assertEquals("ربيع الأول ١٤٤٨", hijriMonthTitle(month, CalendarLocale.Arabic))
    }

    @Test
    fun arabicRamadanAndHijjahSpellings() {
        assertEquals("رمضان", HijriLabels.monthName(CalendarLocale.Arabic, 9))
        assertEquals("ذو القعدة", HijriLabels.monthName(CalendarLocale.Arabic, 11))
        assertEquals("ذو الحجة", HijriLabels.monthName(CalendarLocale.Arabic, 12))
        assertEquals("ذی\u200cالحجه", HijriLabels.monthName(CalendarLocale.Persian, 12))
    }

    @Test
    fun fullDateUsesTheLanguageSeparators() {
        assertEquals(
            "Sunday, 20 Rabi al-Awwal 1448",
            HijriLabels.fullDate(CalendarLocale.English, rabiAlAwwal, sundayEpochDay),
        )
        assertEquals(
            "Sonntag, 20. Rabi al-Awwal 1448",
            HijriLabels.fullDate(CalendarLocale.German, rabiAlAwwal, sundayEpochDay),
        )
        assertEquals(
            "یکشنبه، ۲۰ ربیع\u200cالاول ۱۴۴۸",
            HijriLabels.fullDate(CalendarLocale.Persian, rabiAlAwwal, sundayEpochDay),
        )
        assertEquals(
            "الأحد، ٢٠ ربيع الأول ١٤٤٨",
            HijriLabels.fullDate(CalendarLocale.Arabic, rabiAlAwwal, sundayEpochDay),
        )
    }

    @Test
    fun unsupportedLanguageFallsBackToEnglishLabelsAndWesternDigits() {
        val spanish = CalendarLocale("es")
        assertEquals("Ramadan", HijriLabels.monthName(spanish, 9))
        assertEquals("Sun", HijriLabels.weekdayShort(spanish, 7))
        assertEquals("1448", HijriLabels.number(spanish, 1448))
    }
}
