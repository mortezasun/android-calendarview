package com.develotter.calendarview.adapter

import androidx.viewbinding.ViewBinding
import com.develotter.calendarview.status.CalendarStatus
import com.develotter.calendarview.status.DayStatus
import com.develotter.calendarview.status.MonthStatus


abstract class BaseCalendarAdapter<Day : ViewBinding, Week : ViewBinding, Month : ViewBinding>(
    var calendarStatus: CalendarStatus,
    var dayStatusListAdapter:  MutableMap<String, DayStatus> = mutableMapOf(),
    var dayStatusListSelectedByMultipleSelect: MutableList<DayStatus>,
    var dayStatusListSelectedBySingleSelect: MutableList<DayStatus>,
    var dayStatusListSelectedRange: MutableList<DayStatus>,
    var dayStatusListSelectedViewBinding: MutableMap<String, Day>
) :
    DayCellViewAdapter< Day, Week > {
    abstract fun onBindControllerCalendar( )   : ViewBinding
    abstract fun onBindMonthView(monthStatus: MonthStatus<*, *>):Month
    abstract fun onMonthViewActive(monthStatus: MonthStatus<*, *>, headerPager: Month)
}