package com.develotter.calendarview.jalali

import com.develotter.calendarview.status.DayStatus
import com.github.eloyzone.jalalicalendar.DateConverter
import com.github.eloyzone.jalalicalendar.JalaliDate
import java.time.LocalDate

/**
 * Represents the status of a day in the Jalali (Persian) calendar.  Extends the generic [DayStatus] class for [JalaliDate].
 *
 * This class provides information about a specific day, such as its day of the month and whether it is the current day.
 *
 * @property jalaliDate The [JalaliDate] object representing the date for which the status is being determined.  Must be initialized before use.
 */
class JalaliDayStatus : DayStatus() {
    lateinit var jalaliDate: JalaliDate


    override fun onGetDayInt(): Int {
        return jalaliDate.day
    }

    override fun onGetMonthInt(): Int {
        return jalaliDate.monthPersian.value
    }

    override fun onGetYearInt(): Int {
        return jalaliDate.year
    }

    override fun setDateSelect(year: Int, month: Int, day: Int) {

        jalaliDate = JalaliDate(year, month, day)
        localDate = atDayLocalDate(year, month, day)
    }

    fun atDayLocalDate(year: Int, month: Int, day: Int): LocalDate {
        val dateConverter = DateConverter()
        return dateConverter.jalaliToGregorian(JalaliDate(year, month, day))
    }
    override fun isToday(): Boolean {
        val dateConverter = DateConverter()
        val today = LocalDate.now()
        val todayInJalali =
            dateConverter.gregorianToJalali(today.year, today.month, today.dayOfMonth)

        return jalaliDate.year == todayInJalali.year && jalaliDate.monthPersian.value == todayInJalali.monthPersian.value && jalaliDate.day == todayInJalali.day

    }
}