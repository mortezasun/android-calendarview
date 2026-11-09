package com.develotter.calendarview.status

import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

/**
 * Calendar-system independent view of one month.
 *
 * Implementations provide month navigation and day creation for their calendar system.
 * [com.develotter.calendarview.MultiMonthView] builds a sliding window of months by repeatedly
 * calling [minusMonths] and [plusMonths], and [MonthView] asks [setOnCreateDayStatus] for every
 * visible day. Navigation methods return new instances and must not mutate the receiver.
 *
 * @param lcInUse Locale used for month/day names and number formatting.
 */
abstract class MonthStatus<Month,Day>(   val lcInUse: Locale) {




   abstract fun getThisMonthCaption():String
   abstract fun lengthOfMonth(): Int
   abstract fun getMonthName(): String
   abstract fun getYearName(): String
   abstract fun getInstanceDay(): Month
   abstract fun setInstanceDay( monthInstance: Month)
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