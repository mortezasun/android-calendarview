package com.develotter.calendarview.jalali

import com.develotter.calendarview.status.DayStatus
import com.develotter.calendarview.toJalali

import com.develotter.calendarview.toLocalDate

import ir.huri.jcal.JalaliCalendar
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * Represents the status of a day in the Jalali (Persian) calendar.  Extends the generic [DayStatus] class for [JalaliCalendar].
 *
 * This class provides information about a specific day, such as its day of the month and whether it is the current day.
 *
 * @property jalaliDate The [JalaliCalendar] object representing the date for which the status is being determined.  Must be initialized before use.
 */
class JalaliDayStatus(val  jalaliDate: JalaliCalendar,lcInUse: Locale) : DayStatus(jalaliDate.toLocalDate(),lcInUse) {



    override fun onGetDayInt(): Int {
        return jalaliDate.day
    }

    override fun onGetMonthInt(): Int {
        return jalaliDate.month
    }

    override fun onGetYearInt(): Int {
        return jalaliDate.year
    }

    fun getJalaliTextStyle(txtStyle: TextStyle, lcLocale: Locale): String {
        return when (txtStyle) {


            TextStyle.SHORT -> {
                jalaliDate.day.toString() + "/" + jalaliDate.month + "/" + jalaliDate.year
            }
            else
                -> {
                getDayOfWeekString(txtStyle,lcLocale) + ", " + jalaliDate.day + " " + getMonthString(lcLocale) + ", " + jalaliDate.year
            }

        }
    }
    fun getDayOfWeekString(txtStyle: TextStyle, lcLocale: Locale): String {
        return when (lcLocale.language) {
            "fa" -> {
                jalaliDate.dayOfWeekString
            }
            else -> {
                jalaliDate.toLocalDate().dayOfWeek.getDisplayName(txtStyle, lcLocale)
            }
        }
    }
    fun getMonthString(lcLocale: Locale): String {
        return when (lcLocale.language) {
            "fa" -> {
                jalaliDate.monthString
            }
            else -> {
                getMonthStringEnglish()
            }
        }
    }
    private fun getMonthStringEnglish(): String {
        return when ( jalaliDate.month) {
            1 -> "Farvardin"
            2 -> "Ordibehesht"
            3 -> "Khordad"
            4 -> "Tir"
            5 -> "Mordad"
            6 -> "Shahrivar"
            7 -> "Mehr"
            8 -> "Aban"
            9 -> "Azar"
            10 -> "Dey"
            11 -> "Bahman"
            12 -> "Esfand"
            else -> "Unknown"
        }
    }

    override fun getDisplayName(txtStyle: TextStyle): String {
        return getJalaliTextStyle(txtStyle,lcInUse)
    }





    override fun isToday(): Boolean {

        val todayInJalali = LocalDate.now().toJalali()


        return jalaliDate.year == todayInJalali.year && jalaliDate.month == todayInJalali.month && jalaliDate.day == todayInJalali.day

    }
}