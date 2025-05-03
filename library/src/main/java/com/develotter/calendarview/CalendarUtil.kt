package com.develotter.calendarview

import com.develotter.calendarview.enums.TypeArtCalender
import com.develotter.calendarview.georgian.GeorgianDayStatus
import com.develotter.calendarview.jalali.JalaliDayStatus
import com.develotter.calendarview.status.DayStatus
import ir.huri.jcal.JalaliCalendar
import java.time.LocalDate
import java.util.Locale

fun LocalDate.getCalendarBase(typeArtCalendar: TypeArtCalender, locale: Locale): DayStatus {
    return when (typeArtCalendar) {
        TypeArtCalender.JALALI -> { JalaliDayStatus(this.toJalali(), locale); }
        else -> { GeorgianDayStatus(this, locale) }
    }
}

fun LocalDate.getTitle(typeArtCalendar: TypeArtCalender, locale: Locale): String {
    var title = when (locale.language) {
        "fa" -> {
            getCalendarBase(typeArtCalendar, locale).formatDayIntWithLocale()
        }

        else -> {
            ""
        }
    }
    return title
}

fun JalaliCalendar.toLocalDate(): LocalDate {

    return toLocalDate(this.year,this.month,this.day)
}

fun LocalDate.toJalali(): JalaliCalendar {

    return JalaliCalendar(this)

}
fun LocalDate.toJalaliArrayInt(): Array<Int> {
    var gy: Int = this.year
    var gm: Int = this.monthValue
    var gd: Int = this.dayOfMonth
    var g_d_m: IntArray = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
    var gy2: Int = if (gm > 2) (gy + 1) else gy
    var days: Int = 355666 + (365 * gy) + ((gy2 + 3) / 4).toInt() - ((gy2 + 99) / 100).toInt() + ((gy2 + 399) / 400).toInt() + gd + g_d_m[gm - 1]
    var jy: Int = -1595 + (33 * (days / 12053).toInt())
    days %= 12053
    jy += 4 * (days / 1461).toInt()
    days %= 1461
    if (days > 365) {
        jy += ((days - 1) / 365).toInt()
        days = (days - 1) % 365
    }
    var jm: Int; var jd: Int;
    if (days < 186) {
        jm = 1 + (days / 31).toInt()
        jd = 1 + (days % 31)
    } else {
        jm = 7 + ((days - 186) / 30).toInt()
        jd = 1 + ((days - 186) % 30)
    }
    return arrayOf(jy, jm, jd)
}


fun toLocalDate( jy:Int, jm:Int, jd:Int): LocalDate {

    var jy1: Int = jy + 1595
    var days: Int = -355668 + (365 * jy1) + ((jy1 / 33).toInt() * 8) + (((jy1 % 33) + 3) / 4).toInt() + jd + (if (jm < 7) ((jm - 1) * 31) else (((jm - 7) * 30) + 186))
    var gy: Int = 400 * (days / 146097).toInt()
    days %= 146097
    if (days > 36524) {
        gy += 100 * (--days / 36524).toInt()
        days %= 36524
        if (days >= 365) days++
    }
    gy += 4 * (days / 1461).toInt()
    days %= 1461
    if (days > 365) {
        gy += ((days - 1) / 365).toInt()
        days = (days - 1) % 365
    }
    var gd: Int = days + 1
    var sal_a: IntArray = intArrayOf(0, 31, if((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    var gm: Int = 0
    while (gm < 13 && gd > sal_a[gm]) gd -= sal_a[gm++]
    return LocalDate.of(gy, gm, gd)
}
