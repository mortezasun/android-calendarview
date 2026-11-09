package com.develotter.calendarview.calendars.hijri.compose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.Locale

class HijriMonthDataTest {
    @Test
    fun anyDayInMonthProducesTheSameCalendarData() {
        val first = HijrahDate.of(1447, 9, 1)
        val data = hijriMonthData(first.plus(12, ChronoUnit.DAYS), Locale.US)
        assertEquals(first.lengthOfMonth(), data.days.size)
        assertTrue(data.days.size in 29..30)
        assertEquals(first.toEpochDay(), data.days.first().epochDay)
        assertEquals(hijriMonthData(first, Locale.US), data)
    }

    @Test
    fun allMonthsUseChronologyLengthsAndConsecutiveDays() {
        for (month in 1..12) {
            val first = HijrahDate.of(1447, month, 1)
            val data = hijriMonthData(first, Locale.US)
            assertEquals(first.lengthOfMonth(), data.days.size)
            assertTrue(data.days.zipWithNext().all { (a, b) -> b.epochDay == a.epochDay + 1 })
            assertEquals(first.plus((first.lengthOfMonth() - 1).toLong(), ChronoUnit.DAYS).toEpochDay(), data.days.last().epochDay)
        }
    }

    @Test
    fun yearBoundaryAndGregorianConversionShareAbsoluteTimeline() {
        val lastMonth = HijrahDate.of(1447, 12, 1)
        val nextMonth = lastMonth.plus(1, ChronoUnit.MONTHS)
        val oldData = hijriMonthData(lastMonth, Locale.US)
        val newData = hijriMonthData(nextMonth, Locale.US)
        assertEquals(1448, nextMonth.get(ChronoField.YEAR))
        assertEquals(oldData.days.last().epochDay + 1, newData.days.first().epochDay)
        val gregorian = LocalDate.from(lastMonth)
        assertEquals(gregorian.toEpochDay(), oldData.days.first().epochDay)
        assertEquals(lastMonth, lastMonth.chronology.dateEpochDay(gregorian.toEpochDay()))
    }

    @Test
    fun configurableWeekStartControlsOffsetAndLabels() {
        val date = HijrahDate.of(1447, 1, 1)
        val saturday = hijriMonthData(date, Locale.US)
        val monday = hijriMonthData(date, Locale.US, DayOfWeek.MONDAY)
        assertEquals(DayOfWeek.SATURDAY.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.US), saturday.weekdayLabels.first())
        assertEquals(Math.floorMod(date.get(ChronoField.DAY_OF_WEEK) - DayOfWeek.MONDAY.value, 7), monday.firstDayOffset)
        assertEquals(7, monday.weekdayLabels.size)
    }

    @Test
    fun localizedLabelsAndDisabledDatesArePopulated() {
        val data = hijriMonthData(HijrahDate.of(1447, 9, 1), Locale.forLanguageTag("ar")) {
            it.get(ChronoField.DAY_OF_MONTH) != 2
        }
        assertTrue(data.title.isNotBlank())
        assertTrue(data.days.all { it.label.isNotBlank() && it.accessibilityLabel.isNotBlank() })
        assertTrue(data.days.first().enabled)
        assertFalse(data.days[1].enabled)
    }
}
