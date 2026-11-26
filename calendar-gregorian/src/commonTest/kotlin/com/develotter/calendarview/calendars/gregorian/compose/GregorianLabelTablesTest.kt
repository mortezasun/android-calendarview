package com.develotter.calendarview.calendars.gregorian.compose

import com.develotter.calendarview.calendars.gregorian.GregorianLabels
import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.locale.CalendarLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GregorianLabelTablesTest {
    private val languages = listOf("en", "de", "fa", "ar", "fr", "es", "zh").map(::CalendarLocale)
    private val sundayEpochDay = GregorianYearMonth(2026, 11).atDay(1)

    @Test
    fun everyLanguageHasCompleteNonPlaceholderTables() {
        for (locale in languages) {
            val labels = buildList {
                for (month in 1..12) {
                    add(GregorianLabels.monthName(locale, month))
                    add(GregorianLabels.shortMonthName(locale, month))
                }
                for (weekday in 1..7) {
                    add(GregorianLabels.weekdayShort(locale, weekday))
                    add(GregorianLabels.weekdayFull(locale, weekday))
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
            val digits = GregorianLabels.number(locale, 1234567890)
            assertEquals(10, digits.length)
            assertEquals(10, digits.toSet().size)
        }
    }

    @Test
    fun digitsMatchTheLanguageScript() {
        assertEquals("1234567890", GregorianLabels.number(CalendarLocale.German, 1234567890))
        assertEquals("۱۲۳۴۵۶۷۸۹۰", GregorianLabels.number(CalendarLocale.Persian, 1234567890))
        assertEquals("١٢٣٤٥٦٧٨٩٠", GregorianLabels.number(CalendarLocale.Arabic, 1234567890))
    }

    @Test
    fun fullDateUsesTheLanguageSeparators() {
        assertEquals(
            "Sunday, 1 November 2026",
            GregorianLabels.fullDate(CalendarLocale.English, sundayEpochDay),
        )
        assertEquals(
            "Sonntag, 1. November 2026",
            GregorianLabels.fullDate(CalendarLocale.German, sundayEpochDay),
        )
        assertEquals(
            "یکشنبه، ۱ نوامبر ۲۰۲۶",
            GregorianLabels.fullDate(CalendarLocale.Persian, sundayEpochDay),
        )
        assertEquals(
            "الأحد، ١ نوفمبر ٢٠٢٦",
            GregorianLabels.fullDate(CalendarLocale.Arabic, sundayEpochDay),
        )
    }

    @Test
    fun unsupportedLanguageFallsBackToEnglishLabelsAndWesternDigits() {
        val turkish = CalendarLocale("tr")
        assertEquals("November", GregorianLabels.monthName(turkish, 11))
        assertEquals("Sun", GregorianLabels.weekdayShort(turkish, 7))
        assertEquals("2026", GregorianLabels.number(turkish, 2026))
        assertEquals(
            "Sunday, 1 November 2026",
            GregorianLabels.fullDate(turkish, sundayEpochDay),
        )
    }

    @Test
    fun frenchSpanishAndChineseFollowTheirDateConventions() {
        val french = CalendarLocale.French
        val spanish = CalendarLocale.Spanish
        val chinese = CalendarLocale.Chinese
        assertEquals("dimanche 1 novembre 2026", GregorianLabels.fullDate(french, sundayEpochDay))
        assertEquals("domingo, 1 de noviembre de 2026", GregorianLabels.fullDate(spanish, sundayEpochDay))
        assertEquals("2026年11月1日星期日", GregorianLabels.fullDate(chinese, sundayEpochDay))
        assertEquals("novembre 2026", GregorianLabels.monthYear(french, 11, 2026))
        assertEquals("noviembre de 2026", GregorianLabels.monthYear(spanish, 11, 2026))
        assertEquals("2026年11月", GregorianLabels.monthYear(chinese, 11, 2026))
        assertEquals("1 nov.", GregorianLabels.shortDayMonth(french, sundayEpochDay))
        assertEquals("1 nov 2026", GregorianLabels.shortDayMonthYear(spanish, sundayEpochDay))
        assertEquals("11月1日", GregorianLabels.shortDayMonth(chinese, sundayEpochDay))
        assertEquals("2026年11月1日", GregorianLabels.shortDayMonthYear(chinese, sundayEpochDay))
    }

    @Test
    fun newLanguagesUseWesternDigits() {
        listOf(CalendarLocale.French, CalendarLocale.Spanish, CalendarLocale.Chinese).forEach {
            assertEquals("1234567890", GregorianLabels.number(it, 1234567890))
        }
    }

    @Test
    fun regionalTagsReuseTheLanguageTable() {
        assertEquals(
            GregorianLabels.fullDate(CalendarLocale.Spanish, sundayEpochDay),
            GregorianLabels.fullDate(CalendarLocale("es-MX"), sundayEpochDay),
        )
        assertEquals(
            GregorianLabels.fullDate(CalendarLocale.Chinese, sundayEpochDay),
            GregorianLabels.fullDate(CalendarLocale("zh-Hant-TW"), sundayEpochDay),
        )
    }
}
