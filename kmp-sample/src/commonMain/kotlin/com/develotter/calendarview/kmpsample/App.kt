package com.develotter.calendarview.kmpsample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.develotter.calendarview.calendars.gregorian.GregorianCalendarMonth
import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.core.TypeSelectDay
import com.develotter.calendarview.core.rememberCalendarSelectionState

@Composable
fun App() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(16.dp)) {
                val state = rememberCalendarSelectionState(TypeSelectDay.Range)
                GregorianCalendarMonth(
                    month = GregorianYearMonth.now(),
                    selection = state.selection,
                    onDateClick = { epochDay -> state.onDayClick(epochDay) },
                    fixedSixRows = true,
                )
            }
        }
    }
}
