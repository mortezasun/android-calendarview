package com.develotter.calendarview.adapter

import androidx.viewbinding.ViewBinding
import com.develotter.calendarview.status.CalendarStatus
import com.develotter.calendarview.status.DayStatus
import com.develotter.calendarview.status.MonthStatus


/**
 * Connects the calendar model to the views supplied by the consumer.
 *
 * Each type parameter is the [ViewBinding] used for one part of the calendar:
 * [Day] for a day cell, [Week] for a weekday label, [Month] for a month header and
 * [SelectController] for the selected-day summary. The selection state itself is shared by all
 * months and is stored in the lists below.
 */
abstract class BaseCalendarAdapter<Day : ViewBinding, Week : ViewBinding, Month : ViewBinding,SelectController: ViewBinding>(
    var calendarStatus: CalendarStatus,
    var dayStatusListAdapter:  MutableMap<String, DayStatus> = mutableMapOf(),
    var dayStatusListSelectedByMultipleSelect: MutableList<DayStatus>,
    var dayStatusListSelectedBySingleSelect: MutableList<DayStatus>,
    var dayStatusListSelectedRange: MutableList<DayStatus>,
    var dayStatusListSelectedViewBinding: MutableMap<String, Day>
) :
    DayCellViewAdapter< Day, Week,SelectController > {

    abstract fun onBindControllerCalendar( )   : ViewBinding

    abstract fun onBindMonthView(monthStatus: MonthStatus<*, *>):Month
    abstract fun onUpdateMonthView(monthStatus: MonthStatus<*, *>,headerPager: Month)
    abstract fun onMonthViewActive(monthStatus: MonthStatus<*, *>)

}