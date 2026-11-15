package com.develotter.calendarview.calendars.solarHijriNative

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

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
        assertFailsWith<IllegalArgumentException> { SolarHijriDate(1404, 12, 1).withDayOfMonth(30) }
        assertFailsWith<IllegalArgumentException> { SolarHijriDate(1404, 13, 1) }
    }

    @Test
    fun invalidDatesAndUnsupportedYearsAreRejected() {
        assertFailsWith<IllegalArgumentException> { SolarHijriDate(1404, 12, 30) } // 1404 is common
        assertFailsWith<IllegalArgumentException> { SolarHijriDate(SolarHijriEngine.MAX_YEAR + 1, 1, 1) }
        assertFailsWith<IllegalArgumentException> { SolarHijriDate(SolarHijriEngine.MIN_YEAR - 1, 1, 1) }
        assertFailsWith<IllegalArgumentException> { SolarHijriDate(1404, 1, 0) }
        assertFailsWith<IllegalArgumentException> {
            SolarHijriDate(SolarHijriEngine.MAX_YEAR, 12, 1).plusMonths(1)
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
        assertEquals("1404-01-01", a.toString())
    }

    @Test
    fun epochDayIdentityAndIsoWeekdayArePlatformIndependent() {
        // 1404-01-01 is 2025-03-21, a Friday (ISO weekday 5).
        assertEquals(20168L, SolarHijriDate(1404, 1, 1).toEpochDay())
        assertEquals(5, SolarHijriDate(1404, 1, 1).isoWeekday())
        assertEquals(SolarHijriDate(1404, 1, 1), SolarHijriDate.fromEpochDay(20168L))
    }

    @Test
    fun nowUsesThePlatformClock() {
        val today = SolarHijriDate.now()
        assertEquals(SolarHijriPlatform.currentEpochDay(), today.toEpochDay())
        assertTrue(today.year in SolarHijriEngine.MIN_YEAR..SolarHijriEngine.MAX_YEAR)
    }

    @Test
    fun monthNamesAndNumbersAreAvailableForEveryMonth() {
        assertTrue((1..12).all { SolarHijriLocale.monthName(it).isNotBlank() })
        assertTrue(SolarHijriLocale.formatMonthYear(1404, 1).isNotBlank())
        assertTrue(SolarHijriLocale.formatNumber(1404).isNotBlank())
        assertFailsWith<IllegalArgumentException> { SolarHijriLocale.monthName(13) }
    }

    @Test
    fun monthLengthAndLeapYearAreExposed() {
        assertEquals(31, SolarHijriDate(1404, 1, 1).lengthOfMonth())
        assertEquals(30, SolarHijriDate(1403, 12, 1).lengthOfMonth())
        assertTrue(SolarHijriDate(1403, 1, 1).isLeapYear())
        assertTrue(!SolarHijriDate(1404, 1, 1).isLeapYear())
    }
}
