package com.develotter.calendarviewsample

/** Bounded demo window, with each index calculated using the selected calendar engine. */
fun sampleMonthWindow(calendar: SampleCalendar, center: Long, radius: Int = 12): List<Long> {
    require(radius in 0..120) { "Demo window radius must be in 0..120" }
    return List(radius * 2 + 1) { moveSampleMonth(calendar, center, it - radius) }
}
