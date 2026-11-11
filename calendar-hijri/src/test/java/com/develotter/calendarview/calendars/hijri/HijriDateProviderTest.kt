package com.develotter.calendarview.calendars.hijri

import com.develotter.calendarview.calendars.hijri.compose.hijriMonthData
import com.develotter.calendarview.calendars.hijri.compose.hijriWeekData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoUnit
import java.util.Locale

class HijriDateProviderTest {
    @Test
    fun ummAlQuraProviderRoundTripsEpochDays() {
        val date = HijrahDate.of(1447, 9, 15)
        assertEquals(date, UmmAlQuraHijriDateProvider.dateForEpochDay(date.toEpochDay()))
        assertEquals(date.toEpochDay(), UmmAlQuraHijriDateProvider.epochDayFor(date))
    }

    @Test
    fun offsetProviderMovesDatesAndEpochDaysConsistently() {
        val date = HijrahDate.of(1447, 9, 15)
        val provider = OffsetHijriDateProvider(offsetDays = 1)

        assertEquals(date.plus(1, ChronoUnit.DAYS), provider.dateForEpochDay(date.toEpochDay()))
        assertEquals(date.toEpochDay() - 1, provider.epochDayFor(date))
        assertEquals(date, provider.dateForEpochDay(provider.epochDayFor(date)))
    }

    @Test
    fun monthDataUsesTheProviderForEveryDay() {
        val first = HijrahDate.of(1447, 9, 1)
        val provider = OffsetHijriDateProvider(offsetDays = -1)
        val data = hijriMonthData(first, Locale.US, dateProvider = provider, today = first)

        assertEquals(first.lengthOfMonth(), data.days.size)
        assertEquals(first.toEpochDay() + 1, data.days.first().epochDay)
        assertTrue(data.days.zipWithNext().all { (a, b) -> b.epochDay == a.epochDay + 1 })
        assertEquals(first, provider.dateForEpochDay(data.days.first().epochDay))
        assertEquals(first.plus((first.lengthOfMonth() - 1).toLong(), ChronoUnit.DAYS), provider.dateForEpochDay(data.days.last().epochDay))
        assertTrue(data.days.first().isToday)
    }

    @Test
    fun weekDataUsesTheProviderAndKeepsSevenConsecutiveDays() {
        val date = HijrahDate.of(1447, 9, 15)
        val provider = OffsetHijriDateProvider(offsetDays = 1)
        val week = hijriWeekData(
            date = date,
            locale = Locale.US,
            firstDayOfWeek = DayOfWeek.SATURDAY,
            dateProvider = provider,
        )

        assertEquals(7, week.days.size)
        assertEquals(date.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY)).toEpochDay() - 1, week.days.first().epochDay)
        assertTrue(week.days.zipWithNext().all { (a, b) -> b.epochDay == a.epochDay + 1 })
        assertTrue(week.title.isNotBlank())
    }

    @Test
    fun defaultProviderMatchesThePlatformChronology() {
        val date = HijrahDate.of(1447, 9, 1)
        assertEquals(
            hijriMonthData(date, Locale.US),
            hijriMonthData(date, Locale.US, dateProvider = UmmAlQuraHijriDateProvider),
        )
    }
}
