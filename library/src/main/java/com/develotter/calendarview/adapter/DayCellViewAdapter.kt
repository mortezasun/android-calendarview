package com.develotter.calendarview.adapter

import androidx.viewbinding.ViewBinding
import com.develotter.calendarview.status.DayStatus
import com.develotter.calendarview.status.MonthStatus
import java.time.DayOfWeek

interface DayCellViewAdapter<Date,T:DayStatus<Date>,Day: ViewBinding,Week: ViewBinding>
     {

    fun onReset()
    fun onBindDayView( dayStatus: T)   : Day
    fun onBindWeekView( dayStatus: DayOfWeek)   : Week

    fun onBindLastMonthDayView( dayStatus: T)   : Day
    fun onBindNextMonthDayView( dayStatus: T)   : Day


    fun onCreateDayView(dayStatus: T,day:Int): Day
    fun onHandleClickRowDay(dayStatus: T, viewbinding:Day,isLong: Boolean)
    fun onNoneClickRowDay(dayStatus: T, viewbinding:Day,isLong: Boolean)
    fun onSingleClickRowDay(dayStatus: T, viewbinding:Day,isLong: Boolean)
    fun onMultipleClickRowDay(dayStatus: T, viewbinding:Day,isLong: Boolean)
    fun onRangeClickRowDay(dayStatus: T, viewbinding:Day,isLong: Boolean)

    fun onDayFocused(status: MonthStatus<*, T>)
    fun onDayItemClick(dayStatus: T, viewbinding:Day)



}