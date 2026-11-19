package com.develotter.calendarview.calendars.gregorian.compose

import com.develotter.calendarview.calendars.gregorian.GregorianLabels
import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.locale.CalendarLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GregorianLabelTablesTest {
    private val languages = listOf("en", "de", "fa", "ar").map(::CalendarLocale)
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
        val spanish = CalendarLocale("es")
        assertEquals("November", GregorianLabels.monthName(spanish, 11))
        assertEquals("Sun", GregorianLabels.weekdayShort(spanish, 7))
        assertEquals("2026", GregorianLabels.number(spanish, 2026))
        assertEquals(
            "Sunday, 1 November 2026",
            GregorianLabels.fullDate(spanish, sundayEpochDay),
        )
    }
}
