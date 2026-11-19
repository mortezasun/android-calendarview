package com.develotter.calendarview.kmpsample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

internal enum class DemoCalendarView { Month, Week, Agenda }
internal enum class DemoChineseScript { Simplified, Traditional, Pinyin, Numeric }

internal data class CalendarExplorerOptions(
    val calendar: DemoCalendar,
    val secondaryCalendar: DemoCalendar?,
    val view: DemoCalendarView,
    val selectedEpochDay: Long?,
    val hijriOffset: Int,
    val chineseScript: DemoChineseScript,
    val chineseLeapMonthRequest: Int,
)

@Composable
internal fun SampleCalendarsScreen(
    calendar: DemoCalendar,
    onCalendarChange: (DemoCalendar) -> Unit,
    dateDetails: (Long) -> List<String>,
    modifier: Modifier = Modifier,
    renderCalendar: @Composable (CalendarExplorerOptions, (Long) -> Unit) -> Unit,
) {
    var secondaryName by rememberSaveable { mutableStateOf<String?>(null) }
    var viewName by rememberSaveable { mutableStateOf(DemoCalendarView.Month.name) }
    var selectedDay by rememberSaveable { mutableStateOf<Long?>(null) }
    var hijriOffset by rememberSaveable { mutableStateOf(0) }
    var scriptName by rememberSaveable { mutableStateOf(DemoChineseScript.Simplified.name) }
    var leapMonthRequest by rememberSaveable { mutableStateOf(0) }

    val secondary = secondaryName?.let(DemoCalendar::valueOf)?.takeUnless { it == calendar }
    val options = CalendarExplorerOptions(
        calendar = calendar,
        secondaryCalendar = secondary,
        view = DemoCalendarView.valueOf(viewName),
        selectedEpochDay = selectedDay,
        hijriOffset = hijriOffset,
        chineseScript = DemoChineseScript.valueOf(scriptName),
        chineseLeapMonthRequest = leapMonthRequest,
    )

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Calendars", style = MaterialTheme.typography.headlineSmall)
        DemoChoices("Primary calendar", DemoCalendar.entries, calendar, { it.label }, onCalendarChange)
        DemoChoices(
            title = "Secondary calendar",
            items = listOf(null) + DemoCalendar.entries.filterNot { it == calendar },
            selected = secondary,
            label = { it?.label ?: "None" },
            onSelect = { secondaryName = it?.name },
        )
        DemoChoices("View", DemoCalendarView.entries, options.view, { it.name }) { viewName = it.name }

        if (calendar == DemoCalendar.Hijri) {
            DemoChoices("Hijri day offset", listOf(-1, 0, 1), hijriOffset, Int::toString) {
                hijriOffset = it
            }
        }
        if (calendar == DemoCalendar.Chinese) {
            DemoChoices("Chinese script", DemoChineseScript.entries, options.chineseScript, { it.name }) {
                scriptName = it.name
            }
            OutlinedButton(onClick = { leapMonthRequest++ }) {
                Text("Show leap month 2 of 2023")
            }
        }

        selectedDay?.let { day ->
            Text("Selected date", style = MaterialTheme.typography.titleSmall)
            dateDetails(day).forEach { Text(it) }
        }

        androidx.compose.foundation.layout.Box(Modifier.weight(1f)) {
            renderCalendar(options) { selectedDay = it }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun <T> DemoChoices(
    title: String,
    items: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    Text(title, style = MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            FilterChip(
                selected = item == selected,
                onClick = { onSelect(item) },
                label = { Text(label(item)) },
            )
        }
    }
}