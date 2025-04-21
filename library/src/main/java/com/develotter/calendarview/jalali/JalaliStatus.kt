package com.develotter.calendarview.jalali

import com.develotter.calendarview.status.DayStatus
import com.develotter.calendarview.status.MonthStatus
import com.github.eloyzone.jalalicalendar.DateConverter
import com.github.eloyzone.jalalicalendar.JalaliDate
import com.github.eloyzone.jalalicalendar.MonthPersian
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor

open class JalaliStatus() : MonthStatus<JalaliDate, DayStatus>() {
    var jalaliDateRow: JalaliDate

    override fun setLocaleInUse(lc: Locale) {
        lcInUse = lc
    }

    override fun getThisMonthCaption(): String {
        return getMonthName() + " : " + getYearName()
    }

    init {
        val dateConverter = DateConverter()
        val localDate = LocalDate.now()
        jalaliDateRow = dateConverter.gregorianToJalali(localDate.year, localDate.month, localDate.dayOfMonth)
    }

    override fun lengthOfMonth(): Int {
        return when (jalaliDateRow.monthPersian) {
            MonthPersian.FARVARDIN -> 31
            MonthPersian.ORDIBEHESHT -> 31
            MonthPersian.KHORDAD -> 31
            MonthPersian.TIR -> 31
            MonthPersian.MORDAD -> 31
            MonthPersian.SHAHRIVAR -> 31
            MonthPersian.MEHR -> 30
            MonthPersian.ABAN -> 30
            MonthPersian.AZAR -> 30
            MonthPersian.DAY -> 30
            MonthPersian.BAHMAN -> 30
            MonthPersian.ESFAND -> (if (jalaliDateRow.isLeapYear) 30 else 29)
        }
    }

    override fun getMonthName(): String {

        return if (lcInUse.language == "fa") {
            jalaliDateRow.monthPersian.stringInPersian
        } else {
            jalaliDateRow.monthPersian.stringInEnglish
        }
    }

    override fun getYearName(): String {
        return jalaliDateRow.year.toString()
    }

    override fun getNow(): JalaliDate {
        return jalaliDateRow
    }

    override fun atEndOfMonth(): Int {
        return lengthOfMonth()
    }

    override fun atStartOfMonth(): Int {
        return 1
    }

    override fun minusMonths(count: Int): MonthStatus<JalaliDate, DayStatus> {

        return plusMonths(-count)
    }

    override fun plusMonths(count: Int): MonthStatus<JalaliDate, DayStatus> {
        var status = JalaliStatus()

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
        var newYear = jalaliDateRow.year + floors
        var newMonth = jalaliDateRow.monthPersian.value + (rest).toInt()
        if (newMonth > 12) {
            newYear++
            newMonth -= 12
        } else if (newMonth < 1) {
            newYear--
            newMonth += 12
        }


        status.jalaliDateRow = JalaliDate(newYear, newMonth, 1)
        return status
    }

    override fun setOnCreateDayStatus(day: Int): DayStatus {
        val dayStatus = JalaliDayStatus()
        dayStatus.localDate = atDay(day)
        var month = jalaliDateRow.monthPersian.value
        if (month == 0) {
            month = 1
        }
        dayStatus.jalaliDate = JalaliDate(jalaliDateRow.year, month, day)
        return dayStatus
    }

    override fun atDay(day: Int): LocalDate {
        val dateConverter = DateConverter()
        var month = jalaliDateRow.monthPersian.value
        if (month == 0) {
            month = 1
        }
        return dateConverter.jalaliToGregorian(JalaliDate(jalaliDateRow.year, month, day))
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