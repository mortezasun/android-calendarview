package com.develotter.calendarview.kmpsample

import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.locale.CalendarLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SampleFirstDayOfWeekTest {
    private val anchor = 20_000L // Friday, 2024-10-04.

    private fun isoWeekday(epochDay: Long): Int = (epochDay + 3).mod(7) + 1

    @Test
    fun explicitStartRotatesLabelsAndOffsetForEveryDemoCalendar() {
        val provider = sampleHijriProvider(0)
        for (calendar in DemoCalendar.entries) {
            fun month(firstDay: Int?) = sampleMonthData(
                calendar,
                anchor,
                anchor,
                provider,
                ChineseTextStyle.English,
                CalendarLocale.English,
                firstDay,
            )
            val mondayLabels = month(1).weekdayLabels
            for (firstDay in 1..7) {
                val data = month(firstDay)
                val expectedLabels = mondayLabels.drop(firstDay - 1) + mondayLabels.take(firstDay - 1)
                assertEquals(expectedLabels, data.weekdayLabels, "$calendar labels for $firstDay")
                assertEquals(
                    (isoWeekday(data.days.first().epochDay) - firstDay).mod(7),
                    data.firstDayOffset,
                    "$calendar offset for $firstDay",
                )
            }
        }
    }

    @Test
    fun omittedAndNullStartKeepTheCalendarDefault() {
        val provider = sampleHijriProvider(0)
        for (calendar in DemoCalendar.entries) {
            assertEquals(
                sampleMonthData(calendar, anchor, anchor, provider, ChineseTextStyle.English, CalendarLocale.English),
                sampleMonthData(calendar, anchor, anchor, provider, ChineseTextStyle.English, CalendarLocale.English, null),
                "$calendar default",
            )
        }
    }

    @Test
    fun previewListsTheSevenColumnsInOrder() {
        assertEquals("Mon · Tue · Wed · Thu · Fri · Sat · Sun", sampleWeekdayOrderPreview(1))
        assertEquals("Sat · Sun · Mon · Tue · Wed · Thu · Fri", sampleWeekdayOrderPreview(6))
        assertEquals("Sun · Mon · Tue · Wed · Thu · Fri · Sat", sampleWeekdayOrderPreview(7))
        assertEquals(null, sampleWeekdayOrderPreview(null))
        for (invalid in listOf(0, 8)) {
            assertFailsWith<IllegalArgumentException> { sampleWeekdayOrderPreview(invalid) }
        }
    }

    @Test
    fun choicesCoverTheCalendarDefaultAndAllSevenIsoDays() {
        assertEquals(
            listOf<Int?>(null, 1, 2, 3, 4, 5, 6, 7),
            DemoFirstDayOfWeek.entries.map { it.iso },
        )
    }
}
