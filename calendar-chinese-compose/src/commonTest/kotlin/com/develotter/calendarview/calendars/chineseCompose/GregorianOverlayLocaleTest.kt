package com.develotter.calendarview.calendars.chineseCompose

import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.locale.CalendarLocale
import kotlin.test.Test
import kotlin.test.assertEquals

class GregorianOverlayLocaleTest {
    private val november = GregorianYearMonth(2026, 11)

    @Test
    fun explicitLocaleDrivesTitleWeekdaysAndDigits() {
        val data = gregorianOverlayData(november, firstDayOfWeek = 1, todayEpochDay = -1L, locale = CalendarLocale.Persian)
        assertEquals("نوامبر ۲۰۲۶", data.title)
        assertEquals(listOf("د", "س", "چ", "پ", "ج", "ش", "ی"), data.weekdayLabels)
        assertEquals("۱", data.days.first().label)
    }

    @Test
    fun germanLocaleUsesTheGregorianFullDateLabel() {
        val data = gregorianOverlayData(november, firstDayOfWeek = 1, todayEpochDay = -1L, locale = CalendarLocale.German)
        assertEquals("November 2026", data.title)
        assertEquals("Sonntag, 1. November 2026", data.days.first().accessibilityLabel)
        assertEquals(listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"), data.weekdayLabels)
    }

    @Test
    fun nullLocaleKeepsThePlatformFormatter() {
        val data = gregorianOverlayData(november, firstDayOfWeek = 1, todayEpochDay = -1L)
        assertEquals(ChinesePlatform.formatMonthYear(2026, 11), data.title)
        assertEquals(chineseWeekdayLabels(1), data.weekdayLabels)
        assertEquals(ChinesePlatform.formatFullDate(november.atDay(1)), data.days.first().accessibilityLabel)
    }
}
