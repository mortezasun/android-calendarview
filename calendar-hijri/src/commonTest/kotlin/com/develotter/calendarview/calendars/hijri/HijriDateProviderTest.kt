package com.develotter.calendarview.calendars.hijri

import com.develotter.calendarview.calendars.hijri.compose.hijriDayCell
import com.develotter.calendarview.calendars.hijri.compose.hijriMonthData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class HijriDateProviderTest {
    @Test
    fun ummAlQuraRoundTripsEpochDays() {
        for (epochDay in listOf(0L, 19500L, 20000L, 20500L, 20819L, 21000L, 22000L)) {
            val date = UmmAlQuraHijriDateProvider.dateForEpochDay(epochDay)
            assertTrue(date.month in 1..12)
            assertTrue(date.dayOfMonth in 1..30)
            assertEquals(epochDay, UmmAlQuraHijriDateProvider.epochDayFor(date))
        }
    }

    @Test
    fun monthLengthsAgreeWithConsecutiveMonthStarts() {
        for (year in 1445..1449) {
            for (month in 1..12) {
                val length = HijriPlatform.lengthOfMonth(year, month)
                assertTrue(length in 29..30, "Unexpected month length $length for $year-$month")
                val start = UmmAlQuraHijriDateProvider.epochDayFor(HijriDate(year, month, 1))
                val nextMonth = if (month == 12) HijriDate(year + 1, 1, 1) else HijriDate(year, month + 1, 1)
                val nextStart = UmmAlQuraHijriDateProvider.epochDayFor(nextMonth)
                assertEquals(length.toLong(), nextStart - start)
            }
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
    fun offsetProviderKeepsTimelineWeekdayInAccessibilityLabel() {
        val epochDay = 20819L
        val shiftedDate = UmmAlQuraHijriDateProvider.dateForEpochDay(epochDay + 1)
        val cell = hijriDayCell(
            epochDay = epochDay,
            dateProvider = OffsetHijriDateProvider(1),
            todayEpochDay = 0L,
        )
        assertEquals(HijriPlatform.formatDayNumber(shiftedDate.dayOfMonth), cell.label)
        assertEquals(HijriPlatform.formatFullDate(shiftedDate, epochDay), cell.accessibilityLabel)
        assertNotEquals(HijriPlatform.formatFullDate(shiftedDate, epochDay + 1), cell.accessibilityLabel)
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
