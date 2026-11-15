package com.develotter.calendarview.calendars.solarHijri.compose

import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SolarHijriWeekDataTest {
    private fun isoWeekday(epochDay: Long): Int = (((epochDay + 3) % 7 + 7) % 7).toInt() + 1

    @Test
    fun weekContainsSevenConsecutiveDaysStartingOnSaturday() {
        val epochDay = SolarHijriDate(1404, 1, 15).toEpochDay()
        val data = solarHijriWeekData(epochDay, todayEpochDay = 0L)
        assertEquals(7, data.days.size)
        assertEquals(7, data.weekdayLabels.size)
        assertTrue(data.days.zipWithNext().all { (a, b) -> b.epochDay == a.epochDay + 1 })
        assertEquals(6, isoWeekday(data.days.first().epochDay))
        assertNull(data.weekNumber)
        assertTrue(data.title.isNotBlank())
    }

    @Test
    fun weekStartIsConfigurable() {
        val epochDay = SolarHijriDate(1404, 1, 15).toEpochDay()
        val monday = solarHijriWeekData(epochDay, firstDayOfWeek = 1, todayEpochDay = 0L)
        assertEquals(epochDay - (isoWeekday(epochDay) - 1), monday.days.first().epochDay)
        assertEquals(1, isoWeekday(monday.days.first().epochDay))
    }

    @Test
    fun todayMarkerAndEnabledPredicateUseEpochDays() {
        val epochDay = SolarHijriDate(1404, 1, 15).toEpochDay()
        val data = solarHijriWeekData(epochDay, todayEpochDay = epochDay) { it != epochDay }
        assertEquals(1, data.days.count { it.isToday })
        assertTrue(!data.days.single { it.epochDay == epochDay }.enabled)
    }
}
