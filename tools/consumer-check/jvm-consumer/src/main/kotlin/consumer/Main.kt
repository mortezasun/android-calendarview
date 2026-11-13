package consumer

import com.develotter.calendarview.calendars.chineseNative.ChineseCalendarEngine
import com.develotter.calendarview.calendars.chineseNative.ChineseDate
import com.develotter.calendarview.calendars.chineseNative.ChineseMonth

fun main() {
    val newYear = ChineseDate.of(2024, monthNumber = 1, isLeapMonth = false, dayOfMonth = 1)
    val epochDay = newYear.toEpochDay()
    check(epochDay == 19763L) { "Expected 2024-02-10 at epoch 19763, got $epochDay" }
    check(ChineseDate.fromEpochDay(epochDay) == newYear) { "Chinese date round-trip failed" }

    val months = ChineseCalendarEngine.monthsInYear(2023)
    check(months.size == 13) { "2023 must expose 13 Chinese months, got ${months.size}" }
    check(ChineseCalendarEngine.leapMonth(2023) == ChineseMonth.of(2023, 2, isLeapMonth = true)) {
        "2023 leap month must be month 2L"
    }
    check(ChineseMonth.of(2023, 2, isLeapMonth = true).startEpochDay() > 0) {
        "Leap month must expose a published start epoch day"
    }

    println("JVM consumer OK: newYear=$newYear epochDay=$epochDay months=${months.size}")
}
