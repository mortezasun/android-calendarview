package com.develotter.calendarview.status

import com.develotter.calendarview.calendarFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale


/**
 * Selection and display state for a single calendar day.
 *
 * Calendar modules subclass this to provide their own day/month/year values. The selection flags
 * ([isChecked], [selectRangeDayStatus]) are maintained by
 * [com.develotter.calendarview.adapter.MonthSampleAdapter] and are independent of how the cell is
 * rendered.
 *
 * @param localDate Gregorian date used as the common time axis across all calendar systems.
 * @param lcInUse Locale used for formatting and digit conversion.
 */
abstract class DayStatus(val localDate: LocalDate,val lcInUse: Locale) {
    var isChecked: Boolean = false
    var selectRangeDayStatus: SelectRangeDayStatus = SelectRangeDayStatus.Nothing


    open fun formatDayIntWithLocale(): String {

        return formatNumberToArabicDigits(onGetDayInt())
    }

    open fun getDisplayName(txtStyle: TextStyle): String {
        return localDate.calendarFormat(txtStyle,lcInUse)

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

    /**
     * Formats [number] using [lcInUse]. For Persian/Arabic language tags the digits are forced to
     * Arabic-Indic zero so day numbers match the localized calendar text.
     *
     * Note: the check compares the full language tag, so regional variants such as `fa-IR` do not
     * take this branch.
     */
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