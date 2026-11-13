package com.develotter.calendarview.calendars.chineseNative

/**
 * Presentation style for Chinese calendar labels.
 *
 * Formatting is deliberately separated from calculation and never uses `java.util.Locale` in the
 * engine; Android callers pick a style explicitly or map a locale onto one.
 */
enum class ChineseTextStyle {
    /** English labels such as `Leap 2nd Month`. */
    English,

    /** Simplified Chinese labels such as `闰二月`. */
    SimplifiedChinese,

    /** Traditional Chinese labels such as `閏二月`. */
    TraditionalChinese,

    /** Locale-neutral numeric fallback such as `M2L`. */
    Numeric,
}

/**
 * Pure-Kotlin formatting for Chinese months and days.
 *
 * Every style marks a leap month explicitly, so titles, content descriptions and saved-state
 * debugging output cannot silently lose the distinction.
 */
object ChineseFormat {
    private val englishMonths = listOf(
        "1st Month", "2nd Month", "3rd Month", "4th Month", "5th Month", "6th Month",
        "7th Month", "8th Month", "9th Month", "10th Month", "11th Month", "12th Month",
    )

    private val simplifiedMonths = listOf(
        "正月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "十一月", "十二月",
    )

    private val traditionalMonths = listOf(
        "正月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "十一月", "十二月",
    )

    private val simplifiedDigits = listOf("一", "二", "三", "四", "五", "六", "七", "八", "九", "十")
    private val traditionalDigits = simplifiedDigits

    /** Month name including the leap marker for the requested style. */
    fun monthName(month: ChineseMonth, style: ChineseTextStyle): String {
        require(month.monthNumber in 1..12)
        val base = when (style) {
            ChineseTextStyle.English -> englishMonths[month.monthNumber - 1]
            ChineseTextStyle.SimplifiedChinese -> simplifiedMonths[month.monthNumber - 1]
            ChineseTextStyle.TraditionalChinese -> traditionalMonths[month.monthNumber - 1]
            ChineseTextStyle.Numeric -> "M${month.monthNumber}"
        }
        if (!month.isLeapMonth) return base
        return when (style) {
            ChineseTextStyle.English -> "Leap $base"
            ChineseTextStyle.SimplifiedChinese -> "闰$base"
            ChineseTextStyle.TraditionalChinese -> "閏$base"
            ChineseTextStyle.Numeric -> "${base}L"
        }
    }

    /** Localized month caption used by the month grid. */
    fun monthTitle(month: ChineseMonth, style: ChineseTextStyle): String = when (style) {
        ChineseTextStyle.SimplifiedChinese, ChineseTextStyle.TraditionalChinese ->
            "${month.year}年${monthName(month, style)}"
        else -> "${month.year} ${monthName(month, style)}"
    }

    /** Day-of-month label; Chinese styles use the traditional day numerals. */
    fun dayLabel(dayOfMonth: Int, style: ChineseTextStyle): String {
        require(dayOfMonth in 1..30) { "dayOfMonth must be in 1..30 but was $dayOfMonth" }
        return when (style) {
            ChineseTextStyle.SimplifiedChinese -> chineseDay(dayOfMonth, simplifiedDigits)
            ChineseTextStyle.TraditionalChinese -> chineseDay(dayOfMonth, traditionalDigits)
            else -> dayOfMonth.toString()
        }
    }

    /** Full date label including the leap marker. */
    fun dateLabel(date: ChineseDate, style: ChineseTextStyle): String = when (style) {
        ChineseTextStyle.SimplifiedChinese, ChineseTextStyle.TraditionalChinese ->
            "${date.year}年${monthName(date.month, style)}${dayLabel(date.dayOfMonth, style)}"
        ChineseTextStyle.English ->
            "${date.year} ${monthName(date.month, style)}, day ${date.dayOfMonth}"
        ChineseTextStyle.Numeric ->
            "${date.year}-${monthName(date.month, style)}-${date.dayOfMonth}"
    }

    private fun chineseDay(day: Int, digits: List<String>): String = when (day) {
        in 1..10 -> "初${digits[day - 1]}"
        20 -> "二十"
        30 -> "三十"
        in 11..19 -> "十${digits[day - 11]}"
        in 21..29 -> "廿${digits[day - 21]}"
        else -> error("unreachable day $day")
    }
}
