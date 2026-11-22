package com.develotter.calendarview.selection

internal data class CivilDate(
    val year: Int,
    val month: Int,
    val day: Int,
)

internal fun isoWeekday(epochDay: Long): Int =
    (((epochDay + 3) % 7 + 7) % 7).toInt() + 1

internal fun civilFromEpochDay(epochDay: Long): CivilDate {
    val z = epochDay + 719468
    val era = (if (z >= 0) z else z - 146096) / 146097
    val doe = z - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val y = yoe + era * 400
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = doy - (153 * mp + 2) / 5 + 1
    val m = mp + if (mp < 10) 3 else -9
    val year = (y + if (m <= 2) 1 else 0).toInt()
    return CivilDate(year, m.toInt(), d.toInt())
}

internal fun epochDayOf(year: Int, month: Int, day: Int): Long {
    require(month in 1..12) { "month must be in 1..12: $month" }
    val maxDays = daysInGregorianMonth(year, month)
    require(day in 1..maxDays) { "day $day out of range 1..$maxDays for $year-$month" }
    val y = year.toLong() - if (month <= 2) 1L else 0L
    val era = if (y >= 0) y / 400 else (y - 399) / 400
    val yoe = y - era * 400
    val m = month.toLong()
    val doy = (153 * (m + if (m > 2) -3 else 9) + 2) / 5 + day - 1
    val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
    return era * 146097 + doe - 719468
}

internal fun daysInGregorianMonth(year: Int, month: Int): Int {
    require(month in 1..12) { "month must be in 1..12: $month" }
    return when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) 29 else 28
        else -> error("unreachable")
    }
}
