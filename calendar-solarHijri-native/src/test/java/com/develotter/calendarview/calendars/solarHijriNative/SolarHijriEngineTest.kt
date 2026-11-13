package com.develotter.calendarview.calendars.solarHijriNative

import ir.huri.jcal.JalaliCalendar
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SolarHijriEngineTest {
    private val breakYears = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
        1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178,
    )

    @Test
    fun referenceDatesMatchLocalDate() {
        assertEquals(LocalDate.of(1970, 1, 1).toEpochDay(), SolarHijriEngine.toEpochDay(1348, 10, 11))
        assertEquals(LocalDate.of(2025, 3, 21).toEpochDay(), SolarHijriEngine.toEpochDay(1404, 1, 1))
        assertEquals(SolarHijriDate(1348, 10, 11), SolarHijriEngine.fromEpochDay(0))
        assertEquals(LocalDate.of(2025, 3, 21).toEpochDay(), SolarHijriDate(1404, 1, 1).toEpochDay())
        assertEquals(LocalDate.of(2024, 3, 20), SolarHijriDate(1403, 1, 1).toLocalDate())
    }

    @Test
    fun recentLeapYears() {
        for (year in intArrayOf(1399, 1403, 1408)) assertTrue("$year", SolarHijriEngine.isLeapYear(year))
        for (year in intArrayOf(1400, 1401, 1402, 1404, 1405)) assertFalse("$year", SolarHijriEngine.isLeapYear(year))
    }

    @Test
    fun everyDayRoundTripsAndIsConsecutive() {
        var expected = SolarHijriEngine.MIN_EPOCH_DAY
        var days = 0L
        for (year in SolarHijriEngine.MIN_YEAR..SolarHijriEngine.MAX_YEAR) {
            for (month in 1..12) {
                for (day in 1..SolarHijriEngine.daysInMonth(year, month)) {
                    val epoch = SolarHijriEngine.toEpochDay(year, month, day)
                    assertEquals("$year-$month-$day", expected, epoch)
                    assertEquals(SolarHijriDate(year, month, day), SolarHijriEngine.fromEpochDay(epoch))
                    expected++
                    days++
                }
            }
        }
        assertEquals(SolarHijriEngine.MAX_EPOCH_DAY, expected - 1)
        assertEquals(SolarHijriEngine.MAX_EPOCH_DAY - SolarHijriEngine.MIN_EPOCH_DAY + 1, days)
    }

    @Test
    fun yearLengthsAreThe365Or366() {
        for (year in SolarHijriEngine.MIN_YEAR until SolarHijriEngine.MAX_YEAR) {
            val length = SolarHijriEngine.toEpochDay(year + 1, 1, 1) - SolarHijriEngine.toEpochDay(year, 1, 1)
            assertEquals("$year", if (SolarHijriEngine.isLeapYear(year)) 366L else 365L, length)
        }
    }

    @Test
    fun breakYearsAreConsistentOnBothSides() {
        for (breakYear in breakYears) {
            for (year in breakYear - 1..breakYear + 1) {
                if (year !in SolarHijriEngine.MIN_YEAR..SolarHijriEngine.MAX_YEAR) continue
                val last = SolarHijriEngine.daysInMonth(year, 12)
                assertEquals(if (SolarHijriEngine.isLeapYear(year)) 30 else 29, last)
                val first = SolarHijriEngine.toEpochDay(year, 1, 1)
                assertEquals(SolarHijriDate(year, 1, 1), SolarHijriEngine.fromEpochDay(first))
                assertEquals(SolarHijriDate(year, 12, last), SolarHijriEngine.fromEpochDay(first + 364 + if (last == 30) 1 else 0))
            }
        }
    }

    @Test
    fun agreesWithJalaliLibraryWhereBothModelsCoincide() {
        // Starting at 1280 AP (1901 CE): ir.huri:jalalicalendar diverges by 1 day around 1900 CE (1278 AP).
        val start = SolarHijriEngine.toEpochDay(1280, 1, 1)
        val end = SolarHijriEngine.toEpochDay(1450, 12, 29)
        for (epoch in start..end) {
            val oracle = JalaliCalendar(LocalDate.ofEpochDay(epoch))
            val ours = SolarHijriEngine.fromEpochDay(epoch)
            assertEquals("epoch $epoch", Triple(oracle.year, oracle.month, oracle.day), Triple(ours.year, ours.month, ours.day))
        }
    }

    @Test
    fun rangeIsRejectedOutsideTheBounds() {
        assertThrows(IllegalArgumentException::class.java) { SolarHijriEngine.isLeapYear(-62) }
        assertThrows(IllegalArgumentException::class.java) { SolarHijriEngine.isLeapYear(3178) }
        assertThrows(IllegalArgumentException::class.java) { SolarHijriEngine.daysInMonth(1400, 0) }
        assertThrows(IllegalArgumentException::class.java) { SolarHijriEngine.daysInMonth(1400, 13) }
        assertThrows(IllegalArgumentException::class.java) { SolarHijriEngine.toEpochDay(1404, 12, 30) }
        assertThrows(IllegalArgumentException::class.java) { SolarHijriEngine.toEpochDay(1400, 1, 32) }
        assertThrows(IllegalArgumentException::class.java) { SolarHijriEngine.toEpochDay(-62, 12, 29) }
        assertThrows(IllegalArgumentException::class.java) { SolarHijriEngine.toEpochDay(3178, 1, 1) }
        assertThrows(IllegalArgumentException::class.java) { SolarHijriEngine.fromEpochDay(SolarHijriEngine.MIN_EPOCH_DAY - 1) }
        assertThrows(IllegalArgumentException::class.java) { SolarHijriEngine.fromEpochDay(SolarHijriEngine.MAX_EPOCH_DAY + 1) }
        assertTrue(SolarHijriEngine.isLeapYear(1403))
        assertEquals(30, SolarHijriEngine.daysInMonth(1403, 12))
    }

    @Test
    fun boundsAreTheFirstAndLastSupportedDays() {
        assertEquals(SolarHijriDate(-61, 1, 1), SolarHijriEngine.fromEpochDay(SolarHijriEngine.MIN_EPOCH_DAY))
        val last = SolarHijriEngine.fromEpochDay(SolarHijriEngine.MAX_EPOCH_DAY)
        assertEquals(3177, last.year)
        assertEquals(12, last.month)
        assertEquals(SolarHijriEngine.daysInMonth(3177, 12), last.day)
    }
}
