package com.develotter.calendarviewsample

import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.calendars.hijri.OffsetHijriDateProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.util.Locale

class CalendarSampleMonthDataTest {
    private val locale = Locale.US

    @Test
    fun hijriOffsetChangesAgendaEpochDaysWithoutChangingTheMonthLength() {
        val first = HijrahDate.of(1447, 9, 1)
        val base = sampleMonthData(SampleCalendar.Hijri, first.toEpochDay(), locale)
        val shifted = sampleMonthData(
            calendar = SampleCalendar.Hijri,
            anchorEpochDay = first.toEpochDay(),
            locale = locale,
            hijriDateProvider = OffsetHijriDateProvider(offsetDays = 1),
        )

        assertEquals(base.days.size, shifted.days.size)
        assertEquals(base.days.first().epochDay - 1, shifted.days.first().epochDay)
        assertEquals(base.days.last().epochDay - 1, shifted.days.last().epochDay)
        assertEquals("1", shifted.days.first().label)
        assertTrue(shifted.days.zipWithNext().all { (a, b) -> b.epochDay == a.epochDay + 1 })
    }

    @Test
    fun hijriMonthStartsAtTheProvidersAbsoluteMonthBoundary() {
        val provider = OffsetHijriDateProvider(offsetDays = 1)
        val first = HijrahDate.of(1448, 1, 1)
        val firstEpochDay = provider.epochDayFor(first)
        val data = sampleMonthData(
            calendar = SampleCalendar.Hijri,
            anchorEpochDay = firstEpochDay,
            locale = locale,
            hijriDateProvider = provider,
        )

        assertEquals(firstEpochDay, data.days.first().epochDay)
        assertEquals(first, provider.dateForEpochDay(data.days.first().epochDay))
    }

    @Test
    fun chineseAgendaLabelsFollowTheSelectedStyleWithoutChangingTheDate() {
        val anchor = LocalDate.of(2023, 3, 22).toEpochDay()
        val titles = ChineseTextStyle.entries.associateWith { style ->
            sampleMonthData(
                calendar = SampleCalendar.Chinese,
                anchorEpochDay = anchor,
                locale = locale,
                chineseStyle = style,
            ).title
        }
        assertEquals(4, titles.values.toSet().size)
        assertTrue(titles.getValue(ChineseTextStyle.English).contains("Leap 2nd Month"))
        assertTrue(titles.getValue(ChineseTextStyle.Numeric).contains("M2L"))
        assertTrue(titles.getValue(ChineseTextStyle.SimplifiedChinese).contains("闰二月"))
        assertTrue(titles.getValue(ChineseTextStyle.TraditionalChinese).contains("閏二月"))
    }

    @Test
    fun weekNumberUsesTheLocaleWeekFields() {
        val weekNumber = sampleWeekNumber(LocalDate.of(2026, 1, 5).toEpochDay(), Locale.US)
        assertTrue(weekNumber.isNotBlank())
        assertTrue(weekNumber.all(Char::isDigit))
    }
}
