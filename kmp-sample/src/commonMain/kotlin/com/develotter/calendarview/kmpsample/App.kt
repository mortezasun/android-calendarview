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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.develotter.calendarview.calendars.chinese.ChineseCalendarMonth
import com.develotter.calendarview.calendars.chinese.ChineseMonth
import com.develotter.calendarview.calendars.gregorian.GregorianCalendarMonth
import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.calendars.hijri.HijriCalendarMonth
import com.develotter.calendarview.calendars.hijri.HijriYearMonth
import com.develotter.calendarview.calendars.solarhijri.SolarHijriCalendarMonth
import com.develotter.calendarview.calendars.solarhijri.SolarHijriYearMonth
import com.develotter.calendarview.core.TypeSelectDay
import com.develotter.calendarview.core.rememberCalendarSelectionState

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
    var gregorianMonth by remember { mutableStateOf(GregorianYearMonth.now()) }
    var hijriMonth by remember { mutableStateOf(HijriYearMonth.now()) }
    var solarHijriMonth by remember { mutableStateOf(SolarHijriYearMonth.now()) }
    var chineseMonth by remember { mutableStateOf(ChineseMonth.now()) }

    val selectionState = rememberCalendarSelectionState(selectionMode)

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
                                SampleCalendar.Gregorian -> gregorianMonth = gregorianMonth.minusMonths(1)
                                SampleCalendar.Hijri -> hijriMonth = hijriMonth.minusMonths(1)
                                SampleCalendar.SolarHijri -> solarHijriMonth = solarHijriMonth.minusMonths(1)
                                SampleCalendar.Chinese -> chineseMonth = chineseMonth.minusMonths(1)
                            }
                        },
                    ) { Text("Previous") }

                    OutlinedButton(
                        onClick = {
                            when (selectedCalendar) {
                                SampleCalendar.Gregorian -> gregorianMonth = GregorianYearMonth.now()
                                SampleCalendar.Hijri -> hijriMonth = HijriYearMonth.now()
                                SampleCalendar.SolarHijri -> solarHijriMonth = SolarHijriYearMonth.now()
                                SampleCalendar.Chinese -> chineseMonth = ChineseMonth.now()
                            }
                        },
                    ) { Text("Today") }

                    OutlinedButton(
                        onClick = {
                            when (selectedCalendar) {
                                SampleCalendar.Gregorian -> gregorianMonth = gregorianMonth.plusMonths(1)
                                SampleCalendar.Hijri -> hijriMonth = hijriMonth.plusMonths(1)
                                SampleCalendar.SolarHijri -> solarHijriMonth = solarHijriMonth.plusMonths(1)
                                SampleCalendar.Chinese -> chineseMonth = chineseMonth.plusMonths(1)
                            }
                        },
                    ) { Text("Next") }
                }

                Spacer(modifier = Modifier.height(12.dp))
                when (selectedCalendar) {
                    SampleCalendar.Gregorian -> GregorianCalendarMonth(
                        month = gregorianMonth,
                        selection = selectionState.selection,
                        onDateClick = { epochDay -> selectionState.onDayClick(epochDay) },
                        fixedSixRows = true,
                    )

                    SampleCalendar.Hijri -> HijriCalendarMonth(
                        month = hijriMonth,
                        selection = selectionState.selection,
                        onDateClick = { epochDay -> selectionState.onDayClick(epochDay) },
                        fixedSixRows = true,
                    )

                    SampleCalendar.SolarHijri -> SolarHijriCalendarMonth(
                        month = solarHijriMonth,
                        selection = selectionState.selection,
                        onDateClick = { epochDay -> selectionState.onDayClick(epochDay) },
                        fixedSixRows = true,
                    )

                    SampleCalendar.Chinese -> ChineseCalendarMonth(
                        month = chineseMonth,
                        selection = selectionState.selection,
                        onDateClick = { epochDay -> selectionState.onDayClick(epochDay) },
                        fixedSixRows = true,
                    )
                }
            }
        }
    }
}
