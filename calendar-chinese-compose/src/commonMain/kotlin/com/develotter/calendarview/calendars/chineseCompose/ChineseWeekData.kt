package com.develotter.calendarview.calendars.chineseCompose

import androidx.compose.runtime.Immutable
import com.develotter.calendarview.calendars.chineseNative.ChineseCalendarEngine
import com.develotter.calendarview.calendars.chineseNative.ChineseDate
import com.develotter.calendarview.calendars.chineseNative.ChineseDateOutOfRangeException
import com.develotter.calendarview.calendars.chineseNative.ChineseFormat
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarWeekData
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

/** Chinese seven-day snapshot; lists are read-only and [days] are consecutive epoch days. */
@Immutable
data class ChineseWeekData(
    override val title: String,
    override val weekdayLabels: List<String>,
    override val days: List<CalendarDayCell>,
) : CalendarWeekData

/**
 * Builds the week containing [epochDay], aligned to [firstDayOfWeek].
 *
 * Unlike a month, a week cannot be partially rendered at the data-set boundaries: if any of its
 * seven days is outside the supported range this function throws
 * [ChineseDateOutOfRangeException] instead of inventing or clamping dates. The week number is
 * deliberately omitted; the host decides which week-number convention to show.
 */
fun chineseWeekData(
    epochDay: Long,
    style: ChineseTextStyle = ChineseTextStyle.English,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = WeekFields.of(locale).firstDayOfWeek,
    todayEpochDay: Long = LocalDate.now().toEpochDay(),
    isDateEnabled: (Long) -> Boolean = { true },
): ChineseWeekData {
    val offset = Math.floorMod(epochDay + 4L - firstDayOfWeek.value, 7L).toInt()
    val start = epochDay - offset
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
        weekdayLabels = List(7) { index ->
            firstDayOfWeek.plus(index.toLong()).getDisplayName(TextStyle.SHORT_STANDALONE, locale)
        },
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
