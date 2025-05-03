package com.develotter.calendarview.jalali

import com.develotter.calendarview.status.DayStatus
import com.develotter.calendarview.status.MonthStatus

import com.develotter.calendarview.toJalali
import com.develotter.calendarview.toLocalDate
import ir.huri.jcal.JalaliCalendar

import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor

open class JalaliStatus(localInUse: Locale) : MonthStatus<JalaliDayStatus, DayStatus>(localInUse) {
    var jalaliDateRow: JalaliDayStatus



    override fun getThisMonthCaption(): String {
        return getMonthName() + " : " + getYearName()
    }

    init {
        jalaliDateRow = JalaliDayStatus(LocalDate.now().toJalali(),lcInUse)
    }

    override fun lengthOfMonth(): Int {
        return jalaliDateRow.jalaliDate.monthLength

    }

    override fun getMonthName(): String {

        return  jalaliDateRow.getMonthString(lcInUse)
    }

    override fun getYearName(): String {
        return jalaliDateRow.jalaliDate.year.toString()
    }

    override fun getNow(): JalaliDayStatus {
        return jalaliDateRow
    }

    override fun atEndOfMonth(): Int {
        return lengthOfMonth()
    }

    override fun atStartOfMonth(): Int {
        return 1
    }

    override fun minusMonths(count: Int): MonthStatus<JalaliDayStatus, DayStatus> {

        return plusMonths(-count)
    }

    override fun plusMonths(count: Int): MonthStatus<JalaliDayStatus, DayStatus> {
        var status = JalaliStatus(lcInUse)

        val floors = if (count > 0) {
            floor(count / 12.0).toInt()
        } else {
            ceil(count / 12.0).toInt()
        }
        val rest = if (count > 0) {
            ceil(count % 12.0).toInt()
        } else {
            floor((count % 12.0))
        }
        var newYear = jalaliDateRow.jalaliDate.year + floors
        var newMonth = jalaliDateRow.jalaliDate.month + (rest).toInt()
        if (newMonth > 12) {
            newYear++
            newMonth -= 12
        } else if (newMonth < 1) {
            newYear--
            newMonth += 12
        }


        status.jalaliDateRow = JalaliDayStatus(JalaliCalendar(newYear, newMonth, 1),lcInUse)
        return status
    }

    override fun setOnCreateDayStatus(day: Int): DayStatus {
        var month = jalaliDateRow.jalaliDate.month
        if (month == 0) {
            month = 1
        }
        return JalaliDayStatus(JalaliCalendar(jalaliDateRow.jalaliDate.year, month, day),lcInUse)
    }

    override fun atDay(day: Int): LocalDate {

        var month = jalaliDateRow.jalaliDate.month
        if (month == 0) {
            month = 1
        }
        return JalaliCalendar(jalaliDateRow.jalaliDate.year, month, day).toLocalDate()
    }


    override fun getStartDayOfWeek(): DayOfWeek {
        return DayOfWeek.SATURDAY
    }

    override fun getEndDayOfWeek(): DayOfWeek {
        return DayOfWeek.FRIDAY
    }

    override fun getTrueDayOfWeek(day: Int): DayOfWeek {
        return when (day) {
            1 -> return DayOfWeek.SATURDAY
            2 -> return DayOfWeek.SUNDAY
            3 -> return DayOfWeek.MONDAY
            4 -> return DayOfWeek.TUESDAY
            5 -> return DayOfWeek.WEDNESDAY
            6 -> return DayOfWeek.THURSDAY
            else -> {
                return DayOfWeek.FRIDAY
            }
        }
    }

    override fun getTrueIntDayOfWeek(dayOfWeek: DayOfWeek): Int {
        return when (dayOfWeek) {
            DayOfWeek.SATURDAY -> 1
            DayOfWeek.SUNDAY -> 2
            DayOfWeek.MONDAY -> 3
            DayOfWeek.TUESDAY -> 4
            DayOfWeek.WEDNESDAY -> 5
            DayOfWeek.THURSDAY -> 6
            DayOfWeek.FRIDAY -> 7

        }
    }
}