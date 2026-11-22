package com.develotter.calendarview.calendars.hijri.compose

import com.develotter.calendarview.calendars.hijri.OffsetHijriDateProvider
import com.develotter.calendarview.calendars.hijri.UmmAlQuraHijriDateProvider
import com.develotter.calendarview.locale.CalendarLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class HijriWeekStartParityTest {
    @Test
    fun weekStartAlignsAbsoluteDaysIndependentlyOfProvider() {
        val anchor = 20_000L // Friday, 2024-10-04.
        val locale = CalendarLocale.Arabic
        val offsetProvider = OffsetHijriDateProvider(1)
        val mondayLabels = hijriWeekdayLabels(1, locale)
        for ((index, offset) in listOf(4, 3, 2, 1, 0, 6, 5).withIndex()) {
            val firstDay = index + 1
            val start = anchor - offset
            val expectedDays = (start..(start + 6)).toList()
            assertEquals(offset, hijriDayOfWeekOffset(anchor, firstDay))
            for (provider in listOf(UmmAlQuraHijriDateProvider, offsetProvider)) {
                val week = hijriWeekData(anchor, firstDay, anchor, provider, locale)
                assertEquals(expectedDays, week.days.map { it.epochDay })
                assertEquals(mondayLabels.drop(index) + mondayLabels.take(index), week.weekdayLabels)
                assertEquals(anchor, week.days.single { it.isToday }.epochDay)
            }
        }
        assertEquals(
            hijriWeekData(anchor, 6, anchor, offsetProvider, locale),
            hijriWeekData(anchor, todayEpochDay = anchor, dateProvider = offsetProvider, locale = locale),
        )
    }

    @Test
    fun invalidIsoStartsAreRejectedByBothAlignmentHelpers() {
        for (firstDay in listOf(0, 8)) {
            assertFailsWith<IllegalArgumentException> { hijriDayOfWeekOffset(20_000L, firstDay) }
            assertFailsWith<IllegalArgumentException> { hijriWeekdayLabels(firstDay) }
        }
    }
}
