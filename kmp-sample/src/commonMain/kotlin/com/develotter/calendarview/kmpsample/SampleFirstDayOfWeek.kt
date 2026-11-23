package com.develotter.calendarview.kmpsample

/**
 * First-day-of-week choice of the Playground. [iso] uses ISO-8601 numbering (1 = Monday ..
 * 7 = Sunday); `null` keeps the default of each calendar engine.
 */
internal enum class DemoFirstDayOfWeek(val iso: Int?, val label: String) {
    CalendarDefault(null, "Calendar default"),
    Monday(1, "Monday"),
    Tuesday(2, "Tuesday"),
    Wednesday(3, "Wednesday"),
    Thursday(4, "Thursday"),
    Friday(5, "Friday"),
    Saturday(6, "Saturday"),
    Sunday(7, "Sunday"),
}

private val ShortWeekdayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

/**
 * Weekday column order for an explicit [firstDay] (ISO 1..7), or `null` for the calendar default.
 */
internal fun sampleWeekdayOrderPreview(firstDay: Int?): String? {
    if (firstDay == null) return null
    require(firstDay in 1..7) { "firstDay must be in 1..7: $firstDay" }
    return List(7) { offset -> ShortWeekdayNames[(firstDay - 1 + offset) % 7] }.joinToString(" · ")
}
