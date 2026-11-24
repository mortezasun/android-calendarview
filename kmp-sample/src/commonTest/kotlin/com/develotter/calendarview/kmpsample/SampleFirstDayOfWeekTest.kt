package com.develotter.calendarview.kmpsample

import com.develotter.calendarview.calendars.chineseNative.ChineseCalendarEngine
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.locale.CalendarLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

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
    fun explicitStartAlignsWeekSnapshotsForEveryDemoCalendar() {
        val provider = sampleHijriProvider(0)
        for (calendar in DemoCalendar.entries) {
            fun week(firstDay: Int?) = sampleWeekData(
                calendar,
                anchor,
                anchor,
                provider,
                ChineseTextStyle.English,
                CalendarLocale.English,
                firstDay,
            )
            val mondayLabels = week(1).weekdayLabels
            for (firstDay in 1..7) {
                val data = week(firstDay)
                val expectedLabels = mondayLabels.drop(firstDay - 1) + mondayLabels.take(firstDay - 1)
                val epochDays = data.days.map { it.epochDay }
                assertEquals(expectedLabels, data.weekdayLabels, "$calendar labels for $firstDay")
                assertEquals(firstDay, isoWeekday(epochDays.first()), "$calendar week start for $firstDay")
                assertEquals(List(7) { epochDays.first() + it }, epochDays, "$calendar consecutive days for $firstDay")
                assertTrue(anchor in epochDays, "$calendar week contains the anchor for $firstDay")
            }
        }
    }

    @Test
    fun explicitStartAlignsHeatmapMonthsForEveryDemoCalendar() {
        val provider = sampleHijriProvider(0)
        for (calendar in DemoCalendar.entries) {
            val year = sampleYear(calendar, anchor, provider)
            fun months(firstDay: Int?) = sampleHeatmapYear(
                calendar,
                year,
                anchor,
                provider,
                ChineseTextStyle.English,
                CalendarLocale.English,
                firstDay,
            )
            val mondayLabels = months(1).first().weekdayLabels
            for (firstDay in 1..7) {
                val expectedLabels = mondayLabels.drop(firstDay - 1) + mondayLabels.take(firstDay - 1)
                for (month in months(firstDay)) {
                    assertEquals(expectedLabels, month.weekdayLabels, "$calendar labels for $firstDay")
                    val firstWeek = month.weeks.first()
                    val offset = firstWeek.indexOfFirst { it != null }
                    assertEquals(
                        (isoWeekday(firstWeek[offset]!!.epochDay) - firstDay).mod(7),
                        offset,
                        "$calendar heatmap offset for $firstDay",
                    )
                }
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
                "$calendar month default",
            )
            assertEquals(
                sampleWeekData(calendar, anchor, anchor, provider, ChineseTextStyle.English, CalendarLocale.English),
                sampleWeekData(calendar, anchor, anchor, provider, ChineseTextStyle.English, CalendarLocale.English, null),
                "$calendar week default",
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
        for (choice in DemoFirstDayOfWeek.entries) {
            assertEquals(choice, DemoFirstDayOfWeek.valueOf(choice.name))
        }
    }

    @Test
    fun rtlAndNonEnglishLocalesPreserveWeekdayRotationAcrossAllSevenStarts() {
        val provider = sampleHijriProvider(0)
        val locales = listOf(
            CalendarLocale.Arabic,
            CalendarLocale.Persian,
            CalendarLocale.German,
        )
        for (locale in locales) {
            for (calendar in DemoCalendar.entries) {
                val mondayLabels = sampleMonthData(
                    calendar,
                    anchor,
                    anchor,
                    provider,
                    ChineseTextStyle.SimplifiedChinese,
                    locale,
                    1,
                ).weekdayLabels
                for (firstDay in 1..7) {
                    val data = sampleMonthData(
                        calendar,
                        anchor,
                        anchor,
                        provider,
                        ChineseTextStyle.SimplifiedChinese,
                        locale,
                        firstDay,
                    )
                    val expectedLabels = mondayLabels.drop(firstDay - 1) + mondayLabels.take(firstDay - 1)
                    assertEquals(expectedLabels, data.weekdayLabels, "$calendar $locale labels for $firstDay")
                    assertEquals(
                        (isoWeekday(data.days.first().epochDay) - firstDay).mod(7),
                        data.firstDayOffset,
                        "$calendar $locale offset for $firstDay",
                    )
                }
            }
        }
    }

    @Test
    fun chineseBoundariesAndLeapMonthAlignAcrossAllSevenStarts() {
        val provider = sampleHijriProvider(0)
        // Lower boundary month, upper boundary month, and 2023-03-22 (Chinese leap 2nd month).
        val boundaryAndLeapAnchors = listOf(
            ChineseCalendarEngine.MIN_EPOCH_DAY,
            ChineseCalendarEngine.MAX_EPOCH_DAY,
            19_438L,
        )
        for (testAnchor in boundaryAndLeapAnchors) {
            val mondayLabels = sampleMonthData(
                DemoCalendar.Chinese,
                testAnchor,
                testAnchor,
                provider,
                ChineseTextStyle.English,
                CalendarLocale.English,
                1,
            ).weekdayLabels
            for (firstDay in 1..7) {
                val data = sampleMonthData(
                    DemoCalendar.Chinese,
                    testAnchor,
                    testAnchor,
                    provider,
                    ChineseTextStyle.English,
                    CalendarLocale.English,
                    firstDay,
                )
                val expectedLabels = mondayLabels.drop(firstDay - 1) + mondayLabels.take(firstDay - 1)
                assertEquals(expectedLabels, data.weekdayLabels, "Chinese $testAnchor labels for $firstDay")
                assertEquals(
                    (isoWeekday(data.days.first().epochDay) - firstDay).mod(7),
                    data.firstDayOffset,
                    "Chinese $testAnchor offset for $firstDay",
                )
            }
        }
    }
}
