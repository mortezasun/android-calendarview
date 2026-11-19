package com.develotter.calendarview.calendars.gregorian.compose

import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.locale.CalendarLocale
import kotlin.test.Test
import kotlin.test.assertEquals

class GregorianLocaleTest {
    @Test
    fun explicitLocaleUsesItsOwnMonthNamesAndDigits() {
        val march = GregorianYearMonth(2026, 3)
        assertEquals("März 2026", gregorianMonthTitle(march, CalendarLocale.German))
        assertEquals("مارس ۲۰۲۶", gregorianMonthTitle(march, CalendarLocale.Persian))
        assertEquals("مارس ٢٠٢٦", gregorianMonthTitle(march, CalendarLocale.Arabic))
    }

    @Test
    fun explicitLocaleDrivesWeekdayLabelsAndDayNumbers() {
        val data = gregorianMonthData(
            month = GregorianYearMonth(2026, 3),
            firstDayOfWeek = 1,
            todayEpochDay = -1L,
            locale = CalendarLocale.Persian,
        )
        assertEquals(listOf("د", "س", "چ", "پ", "ج", "ش", "ی"), data.weekdayLabels)
        assertEquals("۱", data.days.first().label)
    }

    @Test
    fun unknownLanguageFallsBackToEnglish() {
        assertEquals(
            "March 2026",
            gregorianMonthTitle(GregorianYearMonth(2026, 3), CalendarLocale("sv")),
        )
    }

    @Test
    fun nullLocaleKeepsThePlatformFormatter() {
        val march = GregorianYearMonth(2026, 3)
        assertEquals(gregorianMonthTitle(march), gregorianMonthTitle(march, null))
    }
}
