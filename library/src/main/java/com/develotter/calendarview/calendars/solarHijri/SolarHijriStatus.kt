package com.develotter.calendarview.calendars.solarHijri

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

/**
 * Represents the status of a month in the Solar Hijri calendar system.
 *
 * This class extends [MonthStatus] and provides specific implementations for handling
 * Solar Hijri dates, including calculating the length of the month, getting the month
 * and year names, navigating between months, and determining the day of the week.
 *
 * @property solarHijriDateRow The [SolarHijriDayStatus] representing the current date.
 * @constructor Creates a new [SolarHijriStatus] instance.
 * @param localInUse The [Locale] to use for formatting and displaying date-related information.
 */
open class SolarHijriStatus(localInUse: Locale) : MonthStatus<SolarHijriDayStatus, DayStatus>(localInUse) {
    var solarHijriDateRow: SolarHijriDayStatus



    override fun getThisMonthCaption(): String {
        return getMonthName() + " : " + getYearName()
    }

    init {
        solarHijriDateRow = SolarHijriDayStatus(LocalDate.now().toJalali(),lcInUse)
    }

    override fun lengthOfMonth(): Int {
        return solarHijriDateRow.solarHijriDate.monthLength

    }

    override fun getMonthName(): String {

        return  solarHijriDateRow.getMonthString(lcInUse)
    }

    override fun getYearName(): String {
        return solarHijriDateRow.solarHijriDate.year.toString()
    }

    override fun getNow(): SolarHijriDayStatus {
        return solarHijriDateRow
    }

    override fun atEndOfMonth(): Int {
        return lengthOfMonth()
    }

    override fun atStartOfMonth(): Int {
        return 1
    }

    override fun minusMonths(count: Int): MonthStatus<SolarHijriDayStatus, DayStatus> {

        return plusMonths(-count)
    }

    override fun plusMonths(count: Int): MonthStatus<SolarHijriDayStatus, DayStatus> {
        var status = SolarHijriStatus(lcInUse)

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
        var newYear = solarHijriDateRow.solarHijriDate.year + floors
        var newMonth = solarHijriDateRow.solarHijriDate.month + (rest).toInt()
        if (newMonth > 12) {
            newYear++
            newMonth -= 12
        } else if (newMonth < 1) {
            newYear--
            newMonth += 12
        }


        status.solarHijriDateRow = SolarHijriDayStatus(JalaliCalendar(newYear, newMonth, 1),lcInUse)
        return status
    }

    override fun setOnCreateDayStatus(day: Int): DayStatus {
        var month = solarHijriDateRow.solarHijriDate.month
        if (month == 0) {
            month = 1
        }
        return SolarHijriDayStatus(JalaliCalendar(solarHijriDateRow.solarHijriDate.year, month, day),lcInUse)
    }

    override fun atDay(day: Int): LocalDate {

        var month = solarHijriDateRow.solarHijriDate.month
        if (month == 0) {
            month = 1
        }
        return JalaliCalendar(solarHijriDateRow.solarHijriDate.year, month, day).toLocalDate()
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