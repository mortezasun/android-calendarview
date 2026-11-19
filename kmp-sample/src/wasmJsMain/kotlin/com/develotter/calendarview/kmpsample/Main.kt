package com.develotter.calendarview.kmpsample

import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        val todayEpochDay = remember { GregorianYearMonth.now().atDay(1) }
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
