package consumer

import com.develotter.calendarview.calendars.chineseNative.ChineseCalendarEngine
import com.develotter.calendarview.calendars.chineseNative.ChineseDate
import com.develotter.calendarview.calendars.chineseNative.ChineseFormat
import com.develotter.calendarview.calendars.chineseNative.ChineseMonth
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle

fun main() {
    val newYear = ChineseDate.of(2024, monthNumber = 1, isLeapMonth = false, dayOfMonth = 1)
    val epochDay = newYear.toEpochDay()
    check(epochDay == 19763L) { "Expected 2024-02-10 at epoch 19763, got $epochDay" }
    check(ChineseDate.fromEpochDay(epochDay) == newYear) { "Chinese date round-trip failed" }

    val months = ChineseCalendarEngine.monthsInYear(2023)
    check(months.size == 13) { "2023 must expose 13 Chinese months, got ${months.size}" }
    val leapMonth = ChineseCalendarEngine.leapMonth(2023)
        ?: error("2023 must expose leap month 2L")
    check(leapMonth == ChineseMonth.of(2023, 2, isLeapMonth = true)) {
        "2023 leap month must be month 2L"
    }
    check(leapMonth.startEpochDay() > 0) {
        "Leap month must expose a published start epoch day"
    }

    val monthStart = leapMonth.startEpochDay()
    val firstDay = ChineseDate.fromEpochDay(monthStart)
    check(firstDay.dayOfMonth == 1) {
        "The first day of the leap month must round-trip through its start epoch day"
    }
    val dayLabels = (1..leapMonth.lengthOfMonth).joinToString(", ") {
        ChineseFormat.dayLabel(it, ChineseTextStyle.English)
    }

    println(
        "JVM engine OK: dataset=${ChineseCalendarEngine.DATASET_VERSION} " +
            "range=${ChineseCalendarEngine.MIN_YEAR}..${ChineseCalendarEngine.MAX_YEAR}",
    )
    println("newYear=$newYear epochDay=$epochDay roundTrip=true")
    println(
        "year2023 months=${months.size} yearDays=${ChineseCalendarEngine.lengthOfYear(2023)} " +
            "leapMonth=${ChineseFormat.monthTitle(leapMonth, ChineseTextStyle.English)}",
    )
    println(
        "month=${ChineseFormat.monthTitle(leapMonth, ChineseTextStyle.English)} " +
            "days=${leapMonth.lengthOfMonth} startEpochDay=$monthStart " +
            "firstDay=${ChineseFormat.dateLabel(firstDay, ChineseTextStyle.English)} " +
            "weekdayOffsetMonday=${ChineseMonth.dayOfWeekIndex(monthStart)}",
    )
    println("dayLabels=$dayLabels")
}
