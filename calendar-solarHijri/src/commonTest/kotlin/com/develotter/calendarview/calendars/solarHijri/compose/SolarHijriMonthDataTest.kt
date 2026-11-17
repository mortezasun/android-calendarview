package com.develotter.calendarview.calendars.solarHijri.compose

import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijri.epochDayToSolarHijri
import com.develotter.calendarview.calendars.solarHijri.solarHijriEpochDay
import com.develotter.calendarview.calendars.solarHijri.solarHijriToEpochDay
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SolarHijriMonthDataTest {
    @Test
    fun monthLengthsIncludeLeapAndCommonEsfand() {
        assertEquals(31, solarHijriMonthData(SolarHijriDate(1400, 1, 1), todayEpochDay = 0L).days.size)
        assertEquals(30, solarHijriMonthData(SolarHijriDate(1400, 7, 1), todayEpochDay = 0L).days.size)
        assertEquals(30, solarHijriMonthData(SolarHijriDate(1399, 12, 1), todayEpochDay = 0L).days.size)
        assertEquals(29, solarHijriMonthData(SolarHijriDate(1400, 12, 1), todayEpochDay = 0L).days.size)
    }

    @Test
    fun nowruzKeepsItsGregorianIdentity() {
        val first = SolarHijriDate(1404, 1, 1)
        val data = solarHijriMonthData(first, todayEpochDay = 0L)
        assertEquals(20168L, first.toEpochDay())
        assertEquals(20168L, data.days.first().epochDay)
        assertTrue(solarHijriEpochDay(first) == first.toEpochDay())
        assertEquals(first, epochDayToSolarHijri(20168L))
        assertEquals(20168L, solarHijriToEpochDay(1404, 1, 1))
    }

    @Test
    fun yearBoundaryIsConsecutive() {
        val oldMonth = solarHijriMonthData(SolarHijriDate(1399, 12, 1), todayEpochDay = 0L)
        val newMonth = solarHijriMonthData(SolarHijriDate(1400, 1, 1), todayEpochDay = 0L)
        assertEquals(oldMonth.days.last().epochDay + 1, newMonth.days.first().epochDay)
        assertTrue(oldMonth.days.zipWithNext().all { (a, b) -> b.epochDay == a.epochDay + 1 })
    }

    @Test
    fun titleAndLabelsAreLocalizedAndWeekStartIsConfigurable() {
        val first = SolarHijriDate(1400, 1, 1)
        val monday = solarHijriMonthData(first, firstDayOfWeek = 1, todayEpochDay = 0L)
        assertEquals(SolarHijriLocale.formatMonthYear(1400, 1), monday.title)
        assertEquals((first.isoWeekday() - 1 + 7) % 7, monday.firstDayOffset)
        assertEquals(7, monday.weekdayLabels.size)
        assertTrue(monday.weekdayLabels.all { it.isNotBlank() })
        assertEquals(monday, solarHijriMonthData(first, firstDayOfWeek = 1, todayEpochDay = 0L))
    }

    @Test
    fun disabledDatesAndTodayMarkerArePopulated() {
        val first = SolarHijriDate(1400, 1, 1)
        val today = first.toEpochDay() + 1
        val data = solarHijriMonthData(first, todayEpochDay = today) { it != today }
        assertTrue(data.days.all { it.label.isNotBlank() && it.accessibilityLabel.isNotBlank() })
        assertTrue(data.days.first().enabled)
        assertFalse(data.days[1].enabled)
        assertEquals(1, data.days.count { it.isToday })
        assertEquals(today, data.days.single { it.isToday }.epochDay)
    }

    @Test
    fun titleAndSecondaryHelpersDelegateAndReturnNullOutOfRange() {
        val first = SolarHijriDate(1404, 1, 1)
        val firstEpochDay = first.toEpochDay()
        assertEquals(SolarHijriLocale.formatMonthYear(1404, 1), solarHijriMonthTitle(first))
        assertTrue(!solarHijriSecondaryLabel(firstEpochDay).isNullOrBlank())
        assertTrue(!solarHijriAccessibilityLabel(firstEpochDay).isNullOrBlank())
        assertEquals(null, solarHijriSecondaryLabel(-1_000_000L))
        assertEquals(null, solarHijriAccessibilityLabel(-1_000_000L))
    }
}
