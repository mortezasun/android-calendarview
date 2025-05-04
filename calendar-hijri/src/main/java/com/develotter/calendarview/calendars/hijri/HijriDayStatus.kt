package com.develotter.calendarview.calendars.hijri

import com.develotter.calendarview.getStringFormatBase
import com.develotter.calendarview.status.DayStatus
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class HijriDayStatus(val hijriDate: HijrahDate, lcInUse: Locale): DayStatus(hijriDate.toLocalDate(), lcInUse) {

    override fun onGetDayInt(): Int {
        return hijriDate.day()
    }

    override fun onGetMonthInt(): Int {
        return hijriDate.month()
    }

    override fun onGetYearInt(): Int {
        return hijriDate.year()
    }

    override fun getDisplayName(txtStyle: TextStyle): String {
        return hijriDate.format(DateTimeFormatter.ofPattern(txtStyle.getStringFormatBase(), lcInUse))
    }
    fun getMonthName():String{

       return hijriDate.format(DateTimeFormatter.ofPattern("MMMM", lcInUse))
    }
    override fun isToday(): Boolean {

        val todayIntoHijri = LocalDate.now().toHijri()


        return hijriDate.year() == todayIntoHijri.year() && hijriDate.month() == todayIntoHijri.month() && hijriDate.day() == todayIntoHijri.day()

    }
}