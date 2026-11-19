package com.develotter.calendarview.kmpsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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
}
