package com.develotter.calendarview.status

import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

abstract class MonthStatus<Month,Day> {

   lateinit var lcInUse: Locale

   abstract fun setLocaleInUse(lc: Locale)
   abstract fun getThisMonthCaption():String
   abstract fun lengthOfMonth(): Int
   abstract fun getMonthName(): String
   abstract fun getYearName(): String
   abstract fun getNow(): Month
   abstract fun atEndOfMonth():Int
   abstract fun atStartOfMonth():Int
   abstract fun minusMonths(count:Int): MonthStatus<Month,Day>
   abstract fun plusMonths(count:Int): MonthStatus<Month,Day>
   abstract fun atDay(day:Int): LocalDate
   abstract fun setOnCreateDayStatus(day:Int): Day
   abstract fun getStartDayOfWeek(): DayOfWeek
   abstract fun getEndDayOfWeek(): DayOfWeek
   abstract fun getTrueDayOfWeek(day: Int): DayOfWeek
   abstract fun getTrueIntDayOfWeek(dayOfWeek: DayOfWeek): Int
}