package com.develotter.calendarview.calendars.hijri.compose

import com.develotter.calendarview.calendars.hijri.HijriDate
import com.develotter.calendarview.calendars.hijri.HijriPlatform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HijriMonthDataTest {
    private fun isoWeekday(epochDay: Long): Int = (((epochDay + 3) % 7 + 7) % 7).toInt() + 1

    @Test
    fun monthLengthsFollowTheChronology() {
        for (month in 1..12) {
            val first = HijriDate(1447, month, 1)
            val data = hijriMonthData(first, todayEpochDay = 0L)
            assertEquals(HijriPlatform.lengthOfMonth(1447, month), data.days.size)
            assertTrue(data.days.size in 29..30)
            assertTrue(data.days.zipWithNext().all { (a, b) -> b.epochDay == a.epochDay + 1 })
        }
    }

    @Test
    fun anyDayInTheMonthProducesTheSameData() {
        val first = HijriDate(1447, 9, 1)
        val later = HijriPlatform.fromEpochDay(HijriPlatform.toEpochDay(first) + 12)
        assertEquals(hijriMonthData(first, todayEpochDay = 0L), hijriMonthData(later, todayEpochDay = 0L))
    }

    @Test
    fun yearBoundaryStaysOnOneTimeline() {
        val last = hijriMonthData(HijriDate(1447, 12, 1), todayEpochDay = 0L)
        val next = hijriMonthData(HijriDate(1448, 1, 1), todayEpochDay = 0L)
        assertEquals(last.days.last().epochDay + 1, next.days.first().epochDay)
        assertEquals(HijriPlatform.toEpochDay(HijriDate(1447, 12, 1)), last.days.first().epochDay)
    }

    @Test
    fun firstDayOfWeekControlsOffsetAndLabels() {
        val first = HijriDate(1447, 1, 1)
        val firstEpochDay = HijriPlatform.toEpochDay(first)
        val saturdayFirst = hijriMonthData(first, todayEpochDay = 0L)
        val mondayFirst = hijriMonthData(first, firstDayOfWeek = 1, todayEpochDay = 0L)
        assertEquals((isoWeekday(firstEpochDay) - 6 + 7) % 7, saturdayFirst.firstDayOffset)
        assertEquals((isoWeekday(firstEpochDay) - 1 + 7) % 7, mondayFirst.firstDayOffset)
        assertEquals(7, mondayFirst.weekdayLabels.size)
        assertTrue(mondayFirst.weekdayLabels.all { it.isNotBlank() })
    }

    @Test
    fun labelsAndDisabledDatesArePopulated() {
        val first = HijriDate(1447, 9, 1)
        val firstEpochDay = HijriPlatform.toEpochDay(first)
        val data = hijriMonthData(first, todayEpochDay = firstEpochDay + 1) { it != firstEpochDay + 1 }
        assertTrue(data.title.isNotBlank())
        assertTrue(data.days.all { it.label.isNotBlank() && it.accessibilityLabel.isNotBlank() })
        assertTrue(data.days.first().enabled)
        assertFalse(data.days[1].enabled)
        assertEquals(1, data.days.count { it.isToday })
        assertEquals(firstEpochDay + 1, data.days.single { it.isToday }.epochDay)
    }

    @Test
    fun titleAndSecondaryHelpersFormatAndGuardOutOfRangeEpochDays() {
        val first = HijriDate(1447, 9, 1)
        val firstEpochDay = HijriPlatform.toEpochDay(first)
        assertEquals(hijriMonthData(first, todayEpochDay = 0L).title, hijriMonthTitle(first))
        val firstLabel = hijriSecondaryLabel(firstEpochDay)
        val secondLabel = hijriSecondaryLabel(firstEpochDay + 1)
        assertTrue(!firstLabel.isNullOrBlank())
        assertTrue(!secondLabel.isNullOrBlank())
        assertTrue(firstLabel != secondLabel)
        assertTrue(!hijriAccessibilityLabel(firstEpochDay).isNullOrBlank())
        // Extremely far epoch days outside Umm-al-Qura range must return null rather than throwing.
        assertEquals(null, hijriSecondaryLabel(-1_000_000L))
        assertEquals(null, hijriAccessibilityLabel(-1_000_000L))
    }
}
