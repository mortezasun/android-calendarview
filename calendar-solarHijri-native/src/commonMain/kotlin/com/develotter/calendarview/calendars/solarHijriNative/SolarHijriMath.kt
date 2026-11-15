package com.develotter.calendarview.calendars.solarHijriNative

/** Floor division for values that may be negative, without `java.lang.Math`. */
internal fun floorDiv(value: Int, divisor: Int): Int {
    val quotient = value / divisor
    return if (value % divisor != 0 && (value xor divisor) < 0) quotient - 1 else quotient
}

/** Floor modulus matching [floorDiv]; the result has the sign of [divisor]. */
internal fun floorMod(value: Int, divisor: Int): Int = value - floorDiv(value, divisor) * divisor

/** Floor division for values that may be negative, without `java.lang.Math`. */
internal fun floorDiv(value: Long, divisor: Long): Long {
    val quotient = value / divisor
    return if (value % divisor != 0L && (value xor divisor) < 0L) quotient - 1 else quotient
}

/** Floor modulus matching [floorDiv]; the result has the sign of [divisor]. */
internal fun floorMod(value: Long, divisor: Long): Long = value - floorDiv(value, divisor) * divisor
