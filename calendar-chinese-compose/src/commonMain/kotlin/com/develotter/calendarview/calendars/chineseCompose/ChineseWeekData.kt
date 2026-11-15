package com.develotter.calendarview.calendars.chineseCompose

import androidx.compose.runtime.Immutable
import com.develotter.calendarview.calendars.chineseNative.ChineseCalendarEngine
import com.develotter.calendarview.calendars.chineseNative.ChineseDate
import com.develotter.calendarview.calendars.chineseNative.ChineseDateOutOfRangeException
import com.develotter.calendarview.calendars.chineseNative.ChineseFormat
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarWeekData

/** Chinese seven-day snapshot; lists are read-only and [days] are consecutive epoch days. */
@Immutable
data class ChineseWeekData(
    override val title: String,
    override val weekdayLabels: List<String>,
    override val days: List<CalendarDayCell>,
) : CalendarWeekData

/**
 * Builds the week containing [epochDay], aligned to [firstDayOfWeek] (ISO 1..7, Monday first).
 *
 * Unlike a month, a week cannot be partially rendered at the data-set boundaries: if any of its
 * seven days is outside the supported range this function throws
 * [ChineseDateOutOfRangeException] instead of inventing or clamping dates. The week number is
 * deliberately omitted; the host decides which week-number convention to show.
 */
fun chineseWeekData(
    epochDay: Long,
    style: ChineseTextStyle = ChineseTextStyle.English,
    firstDayOfWeek: Int = ChinesePlatform.defaultFirstDayOfWeek(),
    todayEpochDay: Long = ChinesePlatform.currentEpochDay(),
    isDateEnabled: (Long) -> Boolean = { true },
): ChineseWeekData {
    val start = epochDay - chineseDayOfWeekOffset(epochDay, firstDayOfWeek)
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
    )
}
