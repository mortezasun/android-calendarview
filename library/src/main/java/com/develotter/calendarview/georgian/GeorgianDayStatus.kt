package com.develotter.calendarview.georgian

import com.develotter.calendarview.status.DayStatus
import java.time.LocalDate

class GeorgianDayStatus : DayStatus<LocalDate>() {
    override fun onGetDayInt(): Int {
        return localDate.dayOfMonth
    }

    override fun onGetMonthInt(): Int {
        return localDate.monthValue
    }

    override fun onGetYearInt(): Int {
        return localDate.year
    }

    override fun setDateSelect(year: Int, month: Int, day: Int) {
        localDate = LocalDate.of(year,month,day)
    }

}