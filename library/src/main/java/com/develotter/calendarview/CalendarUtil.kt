package com.develotter.calendarview


import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


/**
 * Maps a [TextStyle] to the pattern used for day captions.
 *
 * The patterns are fixed per style so every calendar module renders the same shape; weekday and
 * month names are still localized through the [Locale] passed to the formatter.
 */
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

