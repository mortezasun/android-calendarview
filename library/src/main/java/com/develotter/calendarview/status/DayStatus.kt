package com.develotter.calendarview.status

import com.develotter.calendarview.calendarformat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


abstract class DayStatus(val localDate: LocalDate,val lcInUse: Locale) {
    var isChecked: Boolean = false
    var selectRangeDayStatus: SelectRangeDayStatus = SelectRangeDayStatus.Nothing


    open fun formatDayIntWithLocale(): String {

        return formatNumberToArabicDigits(onGetDayInt())
    }

    open fun getDisplayName(txtStyle: TextStyle): String {
        return localDate.calendarformat(txtStyle,lcInUse)

    }

    open fun onGetDayInt(): Int {
        return localDate.dayOfMonth
    }

    open fun onGetMonthInt(): Int {
        return localDate.monthValue
    }

    open fun onGetYearInt(): Int {
        return localDate.year
    }



    open fun checkForArabicAndPersianLanguage(): Boolean {

        return (lcInUse.toLanguageTag().equals("fa") || lcInUse.toLanguageTag().equals("ar"))


    }

    fun formatNumberToArabicDigits(number: Int): String {

        val numberFormat = NumberFormat.getInstance(lcInUse)
        if (numberFormat is DecimalFormat && checkForArabicAndPersianLanguage()
        ) {
            val symbols = numberFormat.decimalFormatSymbols ?: DecimalFormatSymbols(lcInUse)
            symbols.zeroDigit = '٠'
            numberFormat.decimalFormatSymbols = symbols
            return numberFormat.format(number)
        } else {
            return numberFormat.format(number)
        }
    }

    open fun isToday(): Boolean {
        return localDate == LocalDate.now()
    }


}