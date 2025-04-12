package com.develotter.calendarview.status

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale


abstract class DayStatus<T> {
    var isChecked: Boolean = false

    var selectRangeDayStatus:SelectRangeDayStatus = SelectRangeDayStatus.Nothing
    lateinit var localDate: LocalDate
    lateinit var lcInUse: Locale
    open fun formatDayIntWithLocale(): String {

        return formatNumberToArabicDigits(onGetDayInt())
    }

    abstract fun onGetDayInt(): Int
    abstract fun onGetMonthInt(): Int
    abstract fun onGetYearInt(): Int


    abstract fun setDateSelect(year:Int,month:Int,day:Int)

    fun formatNumberToArabicDigits(number: Int): String {

        val numberFormat = NumberFormat.getInstance(lcInUse)
        if (numberFormat is DecimalFormat && (lcInUse.toLanguageTag()
                .equals("fa") || lcInUse.toLanguageTag().equals("ar"))
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