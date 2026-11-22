package com.develotter.calendarview.calendars.solarHijri.compose

import com.develotter.calendarview.calendars.solarHijriNative.compose.solarHijriNativeWeekData
import com.develotter.calendarview.locale.CalendarLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SolarHijriWeekStartParityTest {
    @Test
    fun facadePreservesNativeAlignmentAndSaturdayDefault() {
        val anchor = 20_000L // Friday, 2024-10-04.
        val locale = CalendarLocale.Persian
        for ((index, offset) in listOf(4, 3, 2, 1, 0, 6, 5).withIndex()) {
            val firstDay = index + 1
            val native = solarHijriNativeWeekData(anchor, firstDay, anchor, locale, useIsoWeekNumber = true)
            val facade = solarHijriWeekData(anchor, firstDay, anchor, locale, useIsoWeekNumber = true)
            val start = anchor - offset
            assertEquals((start..(start + 6)).toList(), facade.days.map { it.epochDay })
            assertEquals(native.days, facade.days)
            assertEquals(native.weekdayLabels, facade.weekdayLabels)
            assertEquals(native.title, facade.title)
            assertEquals(native.weekNumber, facade.weekNumber)
        }
        assertEquals(
            solarHijriWeekData(anchor, 6, anchor, locale),
            solarHijriWeekData(anchor, todayEpochDay = anchor, locale = locale),
        )
        assertEquals(
            solarHijriNativeWeekData(anchor, 6, anchor, locale),
            solarHijriNativeWeekData(anchor, todayEpochDay = anchor, locale = locale),
        )
    }

    @Test
    fun nativeValidationAlsoAppliesToTheFacade() {
        for (firstDay in listOf(0, 8)) {
            assertFailsWith<IllegalArgumentException> { solarHijriNativeWeekData(20_000L, firstDay, 20_000L) }
            assertFailsWith<IllegalArgumentException> { solarHijriWeekData(20_000L, firstDay, 20_000L) }
        }
    }
}
