package com.develotter.calendarview.calendars.solarHijri

import com.develotter.calendarview.status.DayStatus
import com.develotter.calendarview.toJalali

import com.develotter.calendarview.toLocalDate

import ir.huri.jcal.JalaliCalendar
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale


/**
 * Represents the status of a specific day in the Solar Hijri calendar.
 *
 * This class extends [DayStatus] and provides methods to retrieve information about a day
 * in the Solar Hijri (Jalali) calendar, including day, month, and year, as well as formatted
 * representations of the date and day of the week. It also handles localization for Persian (fa)
 * and other locales.
 *
 * @property solarHijriDate The [JalaliCalendar] instance representing the date.
 * @property lcInUse The [Locale] currently in use for text formatting.
 * @constructor Creates a [SolarHijriDayStatus] instance.
 */
class SolarHijriDayStatus(val  solarHijriDate: JalaliCalendar, lcInUse: Locale) : DayStatus(solarHijriDate.toLocalDate(),lcInUse) {



    override fun onGetDayInt(): Int {
        return solarHijriDate.day
    }

    override fun onGetMonthInt(): Int {
        return solarHijriDate.month
    }

    override fun onGetYearInt(): Int {
        return solarHijriDate.year
    }

    fun getJalaliTextStyle(txtStyle: TextStyle, lcLocale: Locale): String {
        return when (txtStyle) {


            TextStyle.SHORT -> {
                solarHijriDate.day.toString() + "/" + solarHijriDate.month + "/" + solarHijriDate.year
            }
            else
                -> {
                getDayOfWeekString(txtStyle,lcLocale) + ", " + solarHijriDate.day + " " + getMonthString(lcLocale) + ", " + solarHijriDate.year
            }

        }
    }
    fun getDayOfWeekString(txtStyle: TextStyle, lcLocale: Locale): String {
        return when (lcLocale.language) {
            "fa" -> {
                solarHijriDate.dayOfWeekString
            }
            else -> {
                solarHijriDate.toLocalDate().dayOfWeek.getDisplayName(txtStyle, lcLocale)
            }
        }
    }
    fun getMonthString(lcLocale: Locale): String {
        return when (lcLocale.language) {
            "fa" -> {
                solarHijriDate.monthString
            }
            else -> {
                getMonthStringEnglish()
            }
        }
    }
    private fun getMonthStringEnglish(): String {
        return when ( solarHijriDate.month) {
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


        return solarHijriDate.year == todayInJalali.year && solarHijriDate.month == todayInJalali.month && solarHijriDate.day == todayInJalali.day

    }
}