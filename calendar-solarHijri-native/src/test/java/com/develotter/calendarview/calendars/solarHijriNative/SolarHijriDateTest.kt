package com.develotter.calendarview.calendars.solarHijriNative

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SolarHijriDateTest {
    @Test
    fun plusMonthsClampsDayToTargetMonthLength() {
        assertEquals(SolarHijriDate(1404, 7, 30), SolarHijriDate(1404, 1, 31).plusMonths(6))
        assertEquals(SolarHijriDate(1404, 12, 29), SolarHijriDate(1403, 12, 30).plusMonths(12))
        assertEquals(SolarHijriDate(1403, 12, 30), SolarHijriDate(1403, 12, 30).plusMonths(0))
        assertEquals(SolarHijriDate(1404, 1, 1), SolarHijriDate(1403, 12, 30).plusDays(1))
        assertEquals(SolarHijriDate(1403, 12, 30), SolarHijriDate(1404, 1, 1).minusDays(1))
    }

    @Test
    fun withMethodsValidateAndClampAsDocumented() {
        assertEquals(SolarHijriDate(1404, 12, 29), SolarHijriDate(1403, 12, 30).withYear(1404))
        assertEquals(SolarHijriDate(1404, 7, 30), SolarHijriDate(1404, 1, 31).withMonth(7))
        assertThrows(IllegalArgumentException::class.java) {
            SolarHijriDate(1404, 12, 1).withDayOfMonth(30)
        }
    }

    @Test
    fun comparableAndValueEqualityWork() {
        val a = SolarHijriDate(1404, 1, 1)
        val b = SolarHijriDate.of(1404, 1, 1)
        val c = SolarHijriDate(1404, 1, 2)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertTrue(a < c)
        assertTrue(c > a)
        assertEquals(0, a.compareTo(b))
    }

    @Test
    fun nowUsesInjectedClock() {
        val instant = Instant.parse("2025-03-21T12:00:00Z")
        val clock = Clock.fixed(instant, ZoneOffset.UTC)
        assertEquals(SolarHijriDate(1404, 1, 1), SolarHijriDate.now(clock))
        assertEquals(LocalDate.of(2025, 3, 21), SolarHijriDate.now(clock).toLocalDate())
    }

    @Test
    fun localizationSupportsPersianDariAndEnglish() {
        assertEquals("فروردین", SolarHijriLocale.monthName(1, Locale.forLanguageTag("fa")))
        assertEquals("اسفند", SolarHijriLocale.monthName(12, Locale.forLanguageTag("fa")))
        assertEquals("حمل", SolarHijriLocale.monthName(1, Locale.forLanguageTag("fa-AF")))
        assertEquals("حوت", SolarHijriLocale.monthName(12, Locale.forLanguageTag("prs")))
        assertEquals("Farvardin 1404", SolarHijriLocale.formatMonthYear(1404, 1, Locale.US))
        assertFalse(SolarHijriLocale.formatMonthYear(1404, 1, Locale.forLanguageTag("fa")).contains("Farvardin"))
    }
}
