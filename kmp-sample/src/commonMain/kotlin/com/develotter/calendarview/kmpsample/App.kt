package com.develotter.calendarview.kmpsample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.develotter.calendarview.calendars.chineseCompose.ChineseCalendarMonth
import com.develotter.calendarview.calendars.chineseCompose.rememberChineseMonthState
import com.develotter.calendarview.calendars.gregorian.compose.GregorianCalendarMonth
import com.develotter.calendarview.calendars.gregorian.compose.rememberGregorianMonthState
import com.develotter.calendarview.calendars.hijri.compose.HijriCalendarMonth
import com.develotter.calendarview.calendars.hijri.compose.rememberHijriMonthState
import com.develotter.calendarview.calendars.solarHijri.compose.SolarHijriCalendarMonth
import com.develotter.calendarview.calendars.solarHijri.compose.rememberSolarHijriMonthState
import com.develotter.calendarview.compose.rememberCalendarSelectionState
import com.develotter.calendarview.enums.TypeSelectDay

private enum class SampleCalendar(val label: String) {
    Gregorian("Gregorian"),
    Hijri("Hijri"),
    SolarHijri("Solar Hijri"),
    Chinese("Chinese"),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun App() {
    var selectedCalendar by remember { mutableStateOf(SampleCalendar.Gregorian) }
    var selectionMode by remember { mutableStateOf(TypeSelectDay.Range) }
    val gregorianState = rememberGregorianMonthState()
    val hijriState = rememberHijriMonthState()
    val solarHijriState = rememberSolarHijriMonthState()
    val chineseState = rememberChineseMonthState()

    val selectionState = key(selectionMode) { rememberCalendarSelectionState(mode = selectionMode) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                Text(
                    text = "CalendarView KMP Sample",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Calendar System", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SampleCalendar.entries.forEach { calendar ->
                        FilterChip(
                            selected = selectedCalendar == calendar,
                            onClick = { selectedCalendar = calendar },
                            label = { Text(calendar.label) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Selection Mode", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TypeSelectDay.entries.forEach { mode ->
                        FilterChip(
                            selected = selectionMode == mode,
                            onClick = { selectionMode = mode },
                            label = { Text(mode.name) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    OutlinedButton(
                        onClick = {
                            when (selectedCalendar) {
                                SampleCalendar.Gregorian -> gregorianState.previous()
                                SampleCalendar.Hijri -> hijriState.previous()
                                SampleCalendar.SolarHijri -> solarHijriState.previous()
                                SampleCalendar.Chinese -> chineseState.previous()
                            }
                        },
                    ) { Text("Previous") }

                    OutlinedButton(
                        onClick = {
                            when (selectedCalendar) {
                                SampleCalendar.Gregorian -> gregorianState.goToToday()
                                SampleCalendar.Hijri -> hijriState.goToToday()
                                SampleCalendar.SolarHijri -> solarHijriState.goToToday()
                                SampleCalendar.Chinese -> chineseState.goToToday()
                            }
                        },
                    ) { Text("Today") }

                    OutlinedButton(
                        onClick = {
                            when (selectedCalendar) {
                                SampleCalendar.Gregorian -> gregorianState.next()
                                SampleCalendar.Hijri -> hijriState.next()
                                SampleCalendar.SolarHijri -> solarHijriState.next()
                                SampleCalendar.Chinese -> chineseState.next()
                            }
                        },
                    ) { Text("Next") }
                }

                Spacer(modifier = Modifier.height(12.dp))
                when (selectedCalendar) {
                    SampleCalendar.Gregorian -> GregorianCalendarMonth(
                        month = gregorianState.month,
                        selection = selectionState.selection,
                        onDateClick = { epochDay -> selectionState.onDayClick(epochDay) },
                        fixedSixRows = true,
                    )

                    SampleCalendar.Hijri -> HijriCalendarMonth(
                        month = hijriState.month,
                        selection = selectionState.selection,
                        onDateClick = { epochDay -> selectionState.onDayClick(epochDay) },
                        fixedSixRows = true,
                    )

                    SampleCalendar.SolarHijri -> SolarHijriCalendarMonth(
                        month = solarHijriState.month,
                        selection = selectionState.selection,
                        onDateClick = { epochDay -> selectionState.onDayClick(epochDay) },
                        fixedSixRows = true,
                    )

                    SampleCalendar.Chinese -> ChineseCalendarMonth(
                        month = chineseState.month,
                        selection = selectionState.selection,
                        onDateClick = { epochDay -> selectionState.onDayClick(epochDay) },
                        fixedSixRows = true,
                    )
                }
            }
        }
    }
}
