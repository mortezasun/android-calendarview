package com.develotter.calendarviewsample

import com.develotter.calendarview.calendars.chineseNative.ChineseMonth
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.calendars.chineseCompose.chineseMonthData
import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.calendars.gregorian.compose.gregorianMonthData
import com.develotter.calendarview.calendars.hijri.HijriDateProvider
import com.develotter.calendarview.calendars.hijri.UmmAlQuraHijriDateProvider
import com.develotter.calendarview.calendars.hijri.compose.hijriMonthData
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriMonthData
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.compose.solarHijriNativeMonthData
import com.develotter.calendarview.compose.CalendarMonthData
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * Builds the same engine month snapshot used by the month view. The sample uses it for the
 * agenda and to exercise the shared [CalendarMonthData] contract; the library itself only sees
 * the generic snapshot.
 */
fun sampleMonthData(
    calendar: SampleCalendar,
    anchorEpochDay: Long,
    locale: Locale,
    today: LocalDate = LocalDate.now(),
    hijriDateProvider: HijriDateProvider = UmmAlQuraHijriDateProvider,
    chineseStyle: ChineseTextStyle = ChineseTextStyle.English,
): CalendarMonthData {
    val todayEpochDay = today.toEpochDay()
    val calendarLocale = locale.asCalendarLocale()
    val localeFirstDay = WeekFields.of(locale).firstDayOfWeek.value
    return when (calendar) {
        SampleCalendar.Gregorian -> gregorianMonthData(
            month = GregorianYearMonth.fromEpochDay(anchorEpochDay),
            firstDayOfWeek = localeFirstDay,
            todayEpochDay = todayEpochDay,
            locale = calendarLocale,
        )
        SampleCalendar.Hijri -> hijriMonthData(
            month = hijriDateProvider.dateForEpochDay(anchorEpochDay),
            todayEpochDay = todayEpochDay,
            dateProvider = hijriDateProvider,
            locale = calendarLocale,
        )
        SampleCalendar.SolarHijri -> solarHijriMonthData(
            month = SolarHijriDate.fromEpochDay(anchorEpochDay),
            todayEpochDay = todayEpochDay,
            locale = calendarLocale,
        )
        SampleCalendar.SolarHijriNative -> solarHijriNativeMonthData(
            month = SolarHijriDate.fromEpochDay(anchorEpochDay),
            todayEpochDay = todayEpochDay,
            locale = calendarLocale,
        )
        SampleCalendar.Chinese -> chineseMonthData(
            month = ChineseMonth.fromEpochDay(anchorEpochDay),
            style = chineseStyle,
            firstDayOfWeek = localeFirstDay,
            todayEpochDay = todayEpochDay,
        )
    }
}
