package consumer.android

import androidx.compose.runtime.Composable
import com.develotter.calendarview.calendars.chineseCompose.ChineseCalendarMonth
import com.develotter.calendarview.calendars.chineseCompose.rememberChineseMonthState
import com.develotter.calendarview.calendars.chineseNative.ChineseMonth
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.compose.rememberCalendarSelectionState

@Composable
fun ChineseScreen() {
    val selection = rememberCalendarSelectionState()
    val monthState = rememberChineseMonthState(initialMonth = ChineseMonth.of(2024, 1))

    ChineseCalendarMonth(
        month = monthState.month,
        selection = selection.selection,
        onDateClick = { epochDay -> selection.onDayClick(epochDay) },
        style = ChineseTextStyle.SimplifiedChinese,
    )
}
