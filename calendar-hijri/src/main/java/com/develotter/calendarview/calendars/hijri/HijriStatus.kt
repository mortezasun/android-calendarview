package com.develotter.calendarview.calendars.hijri

import com.develotter.calendarview.status.DayStatus
import com.develotter.calendarview.status.MonthStatus
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor

class HijriStatus(localInUse: Locale) : MonthStatus<HijriDayStatus, DayStatus>(localInUse) {
    var hijriDateRow: HijriDayStatus



    override fun getThisMonthCaption(): String {
        return getMonthName() + " : " + getYearName()
    }

    init {
        hijriDateRow = HijriDayStatus(LocalDate.now().toHijri(),lcInUse)
    }

    override fun lengthOfMonth(): Int {
        return hijriDateRow.hijriDate.lengthOfMonth()

    }

    override fun getMonthName(): String {

        return  hijriDateRow.getMonthName()
    }

    override fun getYearName(): String {
        return hijriDateRow.hijriDate.year().toString()
    }

    override fun getInstanceDay(): HijriDayStatus {
        return hijriDateRow
    }

    override fun setInstanceDay(monthInstance: HijriDayStatus) {
        hijriDateRow = monthInstance
    }
    override fun atEndOfMonth(): Int {
        return lengthOfMonth()
    }

    override fun atStartOfMonth(): Int {
        return 1
    }

    override fun minusMonths(count: Int): MonthStatus<HijriDayStatus, DayStatus> {

        return plusMonths(-count)
    }

    override fun plusMonths(count: Int): MonthStatus<HijriDayStatus, DayStatus> {
        var status = HijriStatus(lcInUse)

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
        var newYear = hijriDateRow.hijriDate.year() + floors
        var newMonth = hijriDateRow.hijriDate.month() + (rest).toInt()
        if (newMonth > 12) {
            newYear++
            newMonth -= 12
        } else if (newMonth < 1) {
            newYear--
            newMonth += 12
        }


        status.hijriDateRow = HijriDayStatus(HijrahDate.of(newYear, newMonth, 1),lcInUse)
        return status
    }

    override fun setOnCreateDayStatus(day: Int): DayStatus {
        var month = hijriDateRow.hijriDate.month()
        if (month == 0) {
            month = 1
        }
        return HijriDayStatus(HijrahDate.of(hijriDateRow.hijriDate.year(), month, day),lcInUse)
    }

    override fun atDay(day: Int): LocalDate {

        var month = hijriDateRow.hijriDate.month()
        if (month == 0) {
            month = 1
        }
        return HijrahDate.of(hijriDateRow.hijriDate.year(), month, day).toLocalDate()
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