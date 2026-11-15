package com.develotter.calendarview.calendars.solarHijriNative.compose

import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.isoWeekdayOfEpochDay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SolarHijriNativeWeekDataTest {
    @Test
    fun weekContainsSevenConsecutiveDaysStartingOnSaturday() {
        val epochDay = SolarHijriDate(1404, 1, 15).toEpochDay()
        val data = solarHijriNativeWeekData(epochDay, todayEpochDay = 0L)
        assertEquals(7, data.days.size)
        assertEquals(7, data.weekdayLabels.size)
        assertTrue(data.days.zipWithNext().all { (a, b) -> b.epochDay == a.epochDay + 1 })
        assertEquals(6, isoWeekdayOfEpochDay(data.days.first().epochDay))
        assertEquals(epochDay, data.days.single { it.epochDay == epochDay }.epochDay)
        assertNull(data.weekNumber)
        assertTrue(data.title.isNotBlank())
    }

    @Test
    fun weekStartIsConfigurable() {
        val epochDay = SolarHijriDate(1404, 1, 15).toEpochDay()
        val saturday = solarHijriNativeWeekData(epochDay, firstDayOfWeek = 6, todayEpochDay = 0L)
        val monday = solarHijriNativeWeekData(epochDay, firstDayOfWeek = 1, todayEpochDay = 0L)
        val iso = isoWeekdayOfEpochDay(epochDay)
        assertEquals(epochDay - (iso - 1), monday.days.first().epochDay)
        assertEquals(epochDay - (iso - 6 + 7) % 7, saturday.days.first().epochDay)
        assertEquals(1, isoWeekdayOfEpochDay(monday.days.first().epochDay))
    }

    @Test
    fun todayMarkerAndEnabledPredicateUseEpochDays() {
        val epochDay = SolarHijriDate(1404, 1, 15).toEpochDay()
        val data = solarHijriNativeWeekData(epochDay, todayEpochDay = epochDay) { it != epochDay }
        assertEquals(1, data.days.count { it.isToday })
        assertTrue(!data.days.single { it.epochDay == epochDay }.enabled)
    }
}
