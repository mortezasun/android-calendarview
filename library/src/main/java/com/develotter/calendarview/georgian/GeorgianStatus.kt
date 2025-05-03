package com.develotter.calendarview.georgian

import com.develotter.calendarview.status.DayStatus
import com.develotter.calendarview.status.MonthStatus
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

open class GeorgianStatus(localInUse: Locale) : MonthStatus<YearMonth, DayStatus>(localInUse) {
    var yearMonth: YearMonth = getNow()


    override fun getThisMonthCaption(): String {
        return getMonthName() + " : " + getYearName()
    }

    init {
        yearMonth = YearMonth.now()
    }

    override fun lengthOfMonth(): Int {
        return yearMonth.lengthOfMonth()
    }

    override fun getMonthName(): String {
        return yearMonth.month.getDisplayName(TextStyle.SHORT, lcInUse)
    }

    override fun getYearName(): String {
        return yearMonth.year.toString()
    }

    override fun getNow(): YearMonth {
        return yearMonth
    }

    override fun atEndOfMonth(): Int {
        return yearMonth.atEndOfMonth().dayOfMonth
    }

    override fun atStartOfMonth(): Int {
        return yearMonth.atDay(1).dayOfMonth
    }

    override fun minusMonths(count: Int): MonthStatus<YearMonth, DayStatus> {
        var georgianStatus = GeorgianStatus(lcInUse)
        georgianStatus.yearMonth = yearMonth.minusMonths(count.toLong())
        return georgianStatus
    }

    override fun plusMonths(count: Int): MonthStatus<YearMonth, DayStatus> {
        var georgianStatus = GeorgianStatus(lcInUse)
        georgianStatus.yearMonth = yearMonth.plusMonths(count.toLong())
        return georgianStatus
    }

    override fun atDay(day: Int): LocalDate {
        return yearMonth.atDay(day)
    }

    override fun setOnCreateDayStatus(day: Int): DayStatus {
        return GeorgianDayStatus(LocalDate.of(yearMonth.year, yearMonth.month, day),lcInUse)
    }

    override fun getStartDayOfWeek(): DayOfWeek {
        return DayOfWeek.MONDAY
    }

    override fun getEndDayOfWeek(): DayOfWeek {
        return DayOfWeek.SUNDAY
    }

    override fun getTrueDayOfWeek(day: Int): DayOfWeek {
        return when (day) {
            1 -> return DayOfWeek.MONDAY
            2 -> return DayOfWeek.TUESDAY
            3 -> return DayOfWeek.WEDNESDAY
            4 -> return DayOfWeek.THURSDAY
            5 -> return DayOfWeek.FRIDAY
            6 -> return DayOfWeek.SATURDAY
            else -> {
                return DayOfWeek.SUNDAY
            }
        }
    }

    override fun getTrueIntDayOfWeek(dayOfWeek: DayOfWeek): Int {
        return dayOfWeek.value.toInt()
    }
}