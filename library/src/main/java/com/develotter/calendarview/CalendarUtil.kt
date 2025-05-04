package com.develotter.calendarview


import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


fun TextStyle.getStringFormatBase():String{
   return when(this){
        TextStyle.SHORT,
        TextStyle.SHORT_STANDALONE ->
        {
            "EEE, MMM dd, yyyy"
        }
        TextStyle.FULL,
        TextStyle.FULL_STANDALONE ->
        {
            "EEEE, MMMM dd, yyyy"
        }
        TextStyle.NARROW,
        TextStyle.NARROW_STANDALONE ->
        {
            "dd/MM/yyyy"
        }
    }
}
fun LocalDate.calendarFormat(textStyle: TextStyle, lcInUse: Locale): String {

    return this.baseFormat(textStyle.getStringFormatBase(), lcInUse)
}
fun LocalDate.baseFormat(formatter: String,lcInUse: Locale): String {

    return this.format(DateTimeFormatter.ofPattern(formatter, lcInUse))
}

