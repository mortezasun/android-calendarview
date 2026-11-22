package com.develotter.calendarview.selection

/**
 * Returns the ISO-8601 week number (1..53) for the week containing [epochDay].
 *
 * Weeks begin on Monday and week 1 of a year is the week containing the first Thursday of that year.
 */
fun isoWeekNumber(epochDay: Long): Int {
    val weekday = isoWeekday(epochDay)
    val thursday = epochDay + (4 - weekday)
    val isoYear = civilFromEpochDay(thursday).year
    val jan4 = epochDayOf(isoYear, 1, 4)
    val firstThursday = jan4 + (4 - isoWeekday(jan4))
    return 1 + ((thursday - firstThursday) / 7).toInt()
}

/**
 * Returns the ISO-8601 week-based year for the week containing [epochDay].
 */
fun isoWeekYear(epochDay: Long): Int {
    val weekday = isoWeekday(epochDay)
    val thursday = epochDay + (4 - weekday)
    return civilFromEpochDay(thursday).year
}
