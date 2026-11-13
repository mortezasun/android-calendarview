package com.develotter.calendarviewsample

import com.develotter.calendarview.calendars.chineseNative.ChineseDateOutOfRangeException

/**
 * Bounded demo window, with each index calculated using the selected calendar engine.
 *
 * A finite engine range truncates the window instead of failing it: at a boundary the
 * unsupported side is omitted, while the requested month remains the first or last entry.
 * Invalid centers still propagate so the viewport can present the engine's error.
 */
fun sampleMonthWindow(calendar: SampleCalendar, center: Long, radius: Int = 12): List<Long> {
    require(radius in 0..120) { "Demo window radius must be in 0..120" }
    // Validate the center even if every neighbor is outside the range (e.g. an invalid anchor).
    moveSampleMonth(calendar, center, 0)
    return buildList {
        for (offset in -radius..radius) {
            val month = try {
                moveSampleMonth(calendar, center, offset)
            } catch (_: ChineseDateOutOfRangeException) {
                continue
            }
            if (lastOrNull() != month) add(month)
        }
    }
}
