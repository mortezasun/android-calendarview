package com.develotter.calendarview.calendars.hijri.compose

import com.develotter.calendarview.calendars.hijri.HijriDate
import com.develotter.calendarview.calendars.hijri.OffsetHijriDateProvider
import com.develotter.calendarview.calendars.hijri.UmmAlQuraHijriDateProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HijriWeekDataTest {
    private fun isoWeekday(epochDay: Long): Int = (((epochDay + 3) % 7 + 7) % 7).toInt() + 1

    @Test
    fun weekContainsSevenConsecutiveDaysStartingOnTheConfiguredWeekday() {
        val epochDay = UmmAlQuraHijriDateProvider.epochDayFor(HijriDate(1447, 9, 10))
        val data = hijriWeekData(epochDay, firstDayOfWeek = 1, todayEpochDay = 0L)
        assertEquals(7, data.days.size)
        assertEquals(7, data.weekdayLabels.size)
        assertTrue(data.days.zipWithNext().all { (a, b) -> b.epochDay == a.epochDay + 1 })
        assertEquals(1, isoWeekday(data.days.first().epochDay))
        assertEquals(epochDay, data.days[isoWeekday(epochDay) - 1].epochDay)
        assertNull(data.weekNumber)
    }

    @Test
    fun providerShiftKeepsTheAbsoluteTimeline() {
        val epochDay = UmmAlQuraHijriDateProvider.epochDayFor(HijriDate(1447, 9, 10))
        val provider = OffsetHijriDateProvider(1)
        val shifted = hijriWeekData(epochDay, firstDayOfWeek = 1, todayEpochDay = 0L, dateProvider = provider)
        assertEquals(7, shifted.days.size)
        assertTrue(shifted.days.zipWithNext().all { (a, b) -> b.epochDay == a.epochDay + 1 })
        assertTrue(shifted.title.isNotBlank())
    }

    @Test
    fun todayMarkerAndEnabledPredicateUseEpochDays() {
        val epochDay = UmmAlQuraHijriDateProvider.epochDayFor(HijriDate(1447, 9, 10))
        val data = hijriWeekData(epochDay, firstDayOfWeek = 1, todayEpochDay = epochDay) { it != epochDay }
        assertEquals(1, data.days.count { it.isToday })
        assertEquals(epochDay, data.days.single { it.isToday }.epochDay)
        assertTrue(data.days.single { it.epochDay == epochDay }.enabled.not())
    }
}
