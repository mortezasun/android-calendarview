package com.develotter.calendarview.kmpsample

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "CalendarView KMP Sample (Desktop)",
        state = rememberWindowState(width = 1200.dp, height = 860.dp),
    ) {
        val todayEpochDay = remember { desktopTodayEpochDay() }
        val todayLabels = remember(todayEpochDay) { sampleTodayLabels(todayEpochDay) }
        App(
            todayLabels = todayLabels,
            dateDetails = ::sampleDateDetails,
            renderCalendar = { options, onSelect ->
                SampleCalendarExplorer(options, onSelect, todayEpochDay)
            },
            renderViewport = { options, onDayClick, isSelected ->
                SamplePlaygroundViewport(options, onDayClick, isSelected, todayEpochDay)
            },
            renderDialogMonth = { calendar -> SampleDialogMonth(calendar, todayEpochDay) },
            rangePolicyAccepts = { day -> sampleRangePolicyAccepts(day, todayEpochDay) },
            renderHeatmap = { options, onDetails -> SampleHeatmap(options, onDetails, todayEpochDay) },
        )
    }
}

/** Desktop anchor day: the first day of the current Gregorian month. */
internal fun desktopTodayEpochDay(): Long = GregorianYearMonth.now().atDay(1)
