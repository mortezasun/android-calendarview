package com.develotter.calendarviewsample

import com.develotter.calendarview.calendars.gregorian.compose.gregorianMonthData
import com.develotter.calendarview.calendars.hijri.HijriDateProvider
import com.develotter.calendarview.calendars.hijri.UmmAlQuraHijriDateProvider
import com.develotter.calendarview.calendars.hijri.compose.hijriMonthData
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriMonthData
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.compose.solarHijriNativeMonthData
import com.develotter.calendarview.compose.CalendarMonthData
import ir.huri.jcal.JalaliCalendar
import java.time.LocalDate
import java.time.YearMonth
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
): CalendarMonthData {
    val date = LocalDate.ofEpochDay(anchorEpochDay)
    return when (calendar) {
        SampleCalendar.Gregorian -> gregorianMonthData(YearMonth.from(date), locale, today = today)
        SampleCalendar.Hijri -> hijriMonthData(
            month = hijriDateProvider.dateForEpochDay(anchorEpochDay),
            locale = locale,
            today = hijriDateProvider.dateForEpochDay(today.toEpochDay()),
            dateProvider = hijriDateProvider,
        )
        SampleCalendar.SolarHijri -> solarHijriMonthData(
            month = JalaliCalendar(date),
            locale = locale,
            today = JalaliCalendar(today),
        )
        SampleCalendar.SolarHijriNative -> solarHijriNativeMonthData(
            month = SolarHijriDate.fromLocalDate(date),
            locale = locale,
            today = SolarHijriDate.fromLocalDate(today),
        )
    }
}
