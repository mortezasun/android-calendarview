package com.develotter.calendarview.adapter

import androidx.viewbinding.ViewBinding
import com.develotter.calendarview.status.CalendarStatus
import com.develotter.calendarview.status.DayStatus
import com.develotter.calendarview.status.MonthStatus


abstract class BaseCalendarAdapter<Date, T : DayStatus<Date>, Day : ViewBinding, Week : ViewBinding, Month : ViewBinding>(
    var calendarStatus: CalendarStatus,
    var dayStatusListAdapter:  MutableMap<String, T> = mutableMapOf() ,
    var dayStatusListSelectedByMultipleSelect: MutableList<T> ,
    var dayStatusListSelectedBySingleSelect: MutableList<T> ,
    var dayStatusListSelectedRange: MutableList<T> ,
    var dayStatusListSelectedViewBinding: MutableMap<String, Day>
) :
    DayCellViewAdapter<Date, T, Day, Week > {
    abstract fun onBindControllerCalendar( )   : ViewBinding
    abstract fun onBindMonthView(monthStatus: MonthStatus<*, *>):Month
    abstract fun onMonthViewActive(monthStatus: MonthStatus<*, *>, headerPager: Month)
}