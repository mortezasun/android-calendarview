package com.develotter.calendarview.adapter

import androidx.viewbinding.ViewBinding
import com.develotter.calendarview.status.DayStatus
import com.develotter.calendarview.status.MonthStatus
import java.time.DayOfWeek

interface DayCellViewAdapter<Day: ViewBinding,Week: ViewBinding>
     {

    fun onReset()
    fun onBindDayView( dayStatus: DayStatus)   : Day
    fun onBindWeekView( dayStatus: DayOfWeek)   : Week

    fun onBindLastMonthDayView( dayStatus: DayStatus)   : Day
    fun onBindNextMonthDayView( dayStatus: DayStatus)   : Day


    fun onCreateDayView(dayStatus: DayStatus, day:Int): Day
    fun onHandleClickRowDay(dayStatus: DayStatus, viewbinding:Day, isLong: Boolean)
    fun onNoneClickRowDay(dayStatus: DayStatus, viewbinding:Day, isLong: Boolean)
    fun onSingleClickRowDay(dayStatus: DayStatus, viewbinding:Day, isLong: Boolean)
    fun onMultipleClickRowDay(dayStatus: DayStatus, viewbinding:Day, isLong: Boolean)
    fun onRangeClickRowDay(dayStatus: DayStatus, viewbinding:Day, isLong: Boolean)

    fun onDayFocused(status: MonthStatus<*, DayStatus>)
    fun onDayItemClick(dayStatus: DayStatus, viewbinding:Day)



}