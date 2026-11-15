package com.develotter.calendarview.calendars.hijri

import com.develotter.calendarview.calendars.hijri.compose.hijriMonthData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HijriDateProviderTest {
    @Test
    fun ummAlQuraRoundTripsEpochDays() {
        for (epochDay in listOf(20000L, 20500L, 20819L, 21000L)) {
            val date = UmmAlQuraHijriDateProvider.dateForEpochDay(epochDay)
            assertTrue(date.month in 1..12)
            assertTrue(date.dayOfMonth in 1..30)
            assertEquals(epochDay, UmmAlQuraHijriDateProvider.epochDayFor(date))
        }
    }

    @Test
    fun offsetProviderShiftsDisplayedDatesByWholeDays() {
        val ahead = OffsetHijriDateProvider(1)
        for (epochDay in listOf(20000L, 20819L)) {
            assertEquals(
                UmmAlQuraHijriDateProvider.dateForEpochDay(epochDay + 1),
                ahead.dateForEpochDay(epochDay),
            )
            assertEquals(epochDay, ahead.epochDayFor(ahead.dateForEpochDay(epochDay)))
        }
        val behind = OffsetHijriDateProvider(-2)
        assertEquals(
            UmmAlQuraHijriDateProvider.dateForEpochDay(20819L - 2),
            behind.dateForEpochDay(20819L),
        )
        assertEquals(20819L, behind.epochDayFor(behind.dateForEpochDay(20819L)))
    }

    @Test
    fun monthDataFollowsTheProviderShift() {
        val date = HijriDate(1447, 9, 1)
        val canonical = UmmAlQuraHijriDateProvider.epochDayFor(date)
        val shifted = hijriMonthData(date, todayEpochDay = 0L, dateProvider = OffsetHijriDateProvider(1))
        assertEquals(canonical - 1, shifted.days.first().epochDay)
        assertEquals(canonical + shifted.days.size - 2, shifted.days.last().epochDay)
    }

    @Test
    fun zeroOffsetMatchesUmmAlQura() {
        val provider = OffsetHijriDateProvider(0)
        val date = HijriDate(1447, 1, 1)
        assertEquals(UmmAlQuraHijriDateProvider.epochDayFor(date), provider.epochDayFor(date))
        assertEquals(
            UmmAlQuraHijriDateProvider.dateForEpochDay(20600L),
            provider.dateForEpochDay(20600L),
        )
    }
}
