package com.develotter.calendarview.calendars.solarHijriNative

/*
 * Kotlin port of the Jalaali calendar algorithm from jalaali-js
 * (https://github.com/jalaali/jalaali-js, MIT License, based on Kazimierz M. Borkowski, 1996).
 * See the NOTICE file in the repository root for the full license text.
 */

/**
 * Dependency-free Solar Hijri (Jalali) calendar arithmetic using the Borkowski break table.
 *
 * Supported years are [MIN_YEAR]..[MAX_YEAR]; everything outside throws
 * [IllegalArgumentException]. The leap-year rule exists exactly once, in the private
 * `jalCal`; [isLeapYear], [daysInMonth], [toEpochDay] and [fromEpochDay] all derive from it,
 * so conversion and month lengths cannot disagree. Day numbers use the same epoch as
 * [java.time.LocalDate.toEpochDay] (1970-01-01 is day 0).
 */
object SolarHijriEngine {
    // Declared first: the epoch-day bounds below run jalCal during object initialization.
    private val BREAKS = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
        1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178,
    )

    /** First supported Solar Hijri year. */
    const val MIN_YEAR: Int = -61

    /** Last supported Solar Hijri year. */
    const val MAX_YEAR: Int = 3177

    /** Epoch day of the first supported date, -61-01-01. */
    val MIN_EPOCH_DAY: Long = jdnOf(MIN_YEAR, 1, 1) - JDN_OF_EPOCH_DAY_ZERO

    /** Epoch day of the last supported date, the last day of Esfand [MAX_YEAR]. */
    val MAX_EPOCH_DAY: Long = jdnOf(MAX_YEAR, 12, if (leapFlag(MAX_YEAR) == 0) 30 else 29) - JDN_OF_EPOCH_DAY_ZERO

    /** Julian Day Number (integer, noon convention) of 1970-01-01. */
    private const val JDN_OF_EPOCH_DAY_ZERO: Long = 2440588L

    private class JalCal(val leap: Int, val gy: Int, val march: Int)

    /** Whether [year] is a leap year (Esfand has 30 days). */
    fun isLeapYear(year: Int): Boolean {
        requireYear(year)
        return leapFlag(year) == 0
    }

    /** Number of days in [month] of [year]: 31 for 1..6, 30 for 7..11, 29 or 30 for Esfand. */
    fun daysInMonth(year: Int, month: Int): Int {
        requireYear(year)
        require(month in 1..12) { "month must be in 1..12 but was $month" }
        return when {
            month <= 6 -> 31
            month <= 11 -> 30
            leapFlag(year) == 0 -> 30
            else -> 29
        }
    }

    /** Epoch day (see [java.time.LocalDate.toEpochDay]) of a valid Solar Hijri date. */
    fun toEpochDay(year: Int, month: Int, day: Int): Long {
        require(day in 1..daysInMonth(year, month)) {
            "day must be in 1..${if (month in 1..12 && year in MIN_YEAR..MAX_YEAR) daysInMonth(year, month) else 0} " +
                "for $year-$month but was $day"
        }
        return jdnOf(year, month, day) - JDN_OF_EPOCH_DAY_ZERO
    }

    /** Solar Hijri date of [epochDay]; must be within [MIN_EPOCH_DAY]..[MAX_EPOCH_DAY]. */
    fun fromEpochDay(epochDay: Long): SolarHijriDate {
        require(epochDay in MIN_EPOCH_DAY..MAX_EPOCH_DAY) {
            "epochDay $epochDay is outside $MIN_EPOCH_DAY..$MAX_EPOCH_DAY"
        }
        val jdn = epochDay + JDN_OF_EPOCH_DAY_ZERO
        val gy = gregorianYearOf(jdn)
        var jy = gy - 621
        val r = jalCal(jy)
        val jdn1f = gregorianToJdn(gy, 3, r.march)
        var k = jdn - jdn1f
        if (k >= 0) {
            if (k <= 185) {
                return SolarHijriDate(jy, 1 + (k / 31).toInt(), (k % 31).toInt() + 1)
            }
            k -= 186
        } else {
            jy -= 1
            k += 179
            if (r.leap == 1) k += 1
        }
        return SolarHijriDate(jy, 7 + (k / 30).toInt(), (k % 30).toInt() + 1)
    }

    private fun requireYear(year: Int) {
        require(year in MIN_YEAR..MAX_YEAR) { "year must be in $MIN_YEAR..$MAX_YEAR but was $year" }
    }

    private fun leapFlag(year: Int): Int = jalCal(year).leap

    private fun jdnOf(jy: Int, jm: Int, jd: Int): Long {
        val r = jalCal(jy)
        return gregorianToJdn(r.gy, 3, r.march) + (jm - 1) * 31L - (jm / 7) * (jm - 7L) + jd - 1
    }

    /** Break-table core: leap position (0 = leap year), Gregorian year and March day of Nowruz. */
    private fun jalCal(jy: Int): JalCal {
        require(jy in BREAKS[0]..BREAKS[BREAKS.size - 1]) {
            "year must be in $MIN_YEAR..$MAX_YEAR but was $jy"
        }
        val gy = jy + 621
        var leapJ = -14
        var jp = BREAKS[0]
        var jump = 0
        for (i in 1 until BREAKS.size) {
            val jm = BREAKS[i]
            jump = jm - jp
            if (jy < jm) break
            leapJ += (jump / 33) * 8 + ((jump % 33) / 4)
            jp = jm
        }
        var n = jy - jp
        leapJ += (n / 33) * 8 + (((n % 33) + 3) / 4)
        if ((jump % 33) == 4 && jump - n == 4) leapJ += 1
        val leapG = (gy / 4) - (((gy / 100) + 1) * 3) / 4 - 150
        val march = 20 + leapJ - leapG
        if (jump - n < 6) n = n - jump + ((jump + 4) / 33) * 33
        var leap = (((n + 1) % 33) - 1) % 4
        if (leap == -1) leap = 4
        return JalCal(leap, gy, march)
    }

    private fun gregorianToJdn(gy: Int, gm: Int, gd: Int): Long {
        val a = (gm - 8) / 6
        var d = ((gy + a + 100100L) * 1461L) / 4L +
            (153 * ((gm + 9) % 12) + 2) / 5 + gd - 34840408L
        d = d - (((gy + 100100L + a) / 100L) * 3L) / 4L + 752L
        return d
    }

    private fun gregorianYearOf(jdn: Long): Int {
        var j = 4L * jdn + 139361631L
        j += (((4L * jdn + 183187720L) / 146097L) * 3L) / 4L * 4L - 3908L
        val i = ((j % 1461L) / 4L) * 5L + 308L
        val gm = (((i / 153L) % 12L) + 1L).toInt()
        return ((j / 1461L) - 100100L + (8L - gm) / 6L).toInt()
    }
}
