package com.develotter.calendarview.calendars.chineseCompose

import androidx.compose.runtime.Immutable
import com.develotter.calendarview.calendars.chineseNative.ChineseCalendarEngine
import com.develotter.calendarview.calendars.chineseNative.ChineseDate
import com.develotter.calendarview.calendars.chineseNative.ChineseDateOutOfRangeException
import com.develotter.calendarview.calendars.chineseNative.ChineseFormat
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarWeekData
import com.develotter.calendarview.selection.isoWeekNumber
import com.develotter.calendarview.selection.weekStartEpochDay

/** Chinese seven-day snapshot; lists are read-only and [days] are consecutive epoch days. */
@Immutable
data class ChineseWeekData(
    override val title: String,
    override val weekdayLabels: List<String>,
    override val days: List<CalendarDayCell>,
    override val weekNumber: String? = null,
) : CalendarWeekData

/**
 * Builds the week containing [epochDay], aligned to [firstDayOfWeek] (ISO 1..7, Monday first).
 * Values outside that range are rejected.
 *
 * Unlike a month, a week cannot be partially rendered at the data-set boundaries: if any of its
 * seven days is outside the supported range this function throws
 * [ChineseDateOutOfRangeException] instead of inventing or clamping dates. The week number is
 * deliberately omitted unless [useIsoWeekNumber] opts in to the ISO-8601 week of the week start;
 * that number is written with Western digits because the Chinese text style has no digit table.
 */
fun chineseWeekData(
    epochDay: Long,
    style: ChineseTextStyle = ChineseTextStyle.English,
    firstDayOfWeek: Int = ChinesePlatform.defaultFirstDayOfWeek(),
    todayEpochDay: Long = ChinesePlatform.currentEpochDay(),
    useIsoWeekNumber: Boolean = false,
    isDateEnabled: (Long) -> Boolean = { true },
): ChineseWeekData {
    val start = weekStartEpochDay(epochDay, firstDayOfWeek)
    val end = start + 6
    if (!ChineseCalendarEngine.isSupported(start) || !ChineseCalendarEngine.isSupported(end)) {
        throw ChineseDateOutOfRangeException(
            "week $start..$end is outside ${ChineseCalendarEngine.MIN_EPOCH_DAY}.." +
                "${ChineseCalendarEngine.MAX_EPOCH_DAY}",
        )
    }
    val first = ChineseDate.fromEpochDay(start)
    val last = ChineseDate.fromEpochDay(end)
    return ChineseWeekData(
        title = "${ChineseFormat.dateLabel(first, style)} – ${ChineseFormat.dateLabel(last, style)}",
        weekdayLabels = chineseWeekdayLabels(firstDayOfWeek),
        days = List(7) { index ->
            chineseDayCell(
                epochDay = start + index,
                style = style,
                todayEpochDay = todayEpochDay,
                isDateEnabled = isDateEnabled,
            )
        },
        weekNumber = if (useIsoWeekNumber) isoWeekNumber(start).toString() else null,
    )
}
