package com.develotter.calendarview.calendars.hijri

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HijriRoundTripPropertyTest {
    @Test
    fun ummAlQuraRoundTripForRandomEpochDays() {
        val random = Random(20261116)
        repeat(500) {
            val epochDay = random.nextLong(0, 60_000)
            val date = UmmAlQuraHijriDateProvider.dateForEpochDay(epochDay)
            assertEquals(epochDay, UmmAlQuraHijriDateProvider.epochDayFor(date))
            assertTrue(date.month in 1..12)
            assertTrue(date.dayOfMonth in 1..30)
        }
    }

    @Test
    fun offsetProviderRoundTripPreservesEpochDay() {
        val random = Random(11)
        val provider = OffsetHijriDateProvider(offsetDays = 2)
        repeat(300) {
            val epochDay = random.nextLong(0, 60_000)
            val date = provider.dateForEpochDay(epochDay)
            assertEquals(epochDay, provider.epochDayFor(date))
        }
    }
}
