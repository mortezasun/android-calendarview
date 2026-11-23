package com.develotter.calendarview.kmpsample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.develotter.calendarview.compose.rememberCalendarSelectionState
import com.develotter.calendarview.enums.TypeSelectDay
import com.develotter.calendarview.locale.CalendarLocale

internal enum class DemoMonthLayout {
    Single, VerticalList, HorizontalList, HorizontalPager, VerticalPager, AdaptiveColumns,
}

internal data class PlaygroundOptions(
    val calendar: DemoCalendar,
    val layout: DemoMonthLayout,
    val selectionMode: TypeSelectDay,
    val showMarkers: Boolean,
    val customStyle: Boolean,
    val limitRange: Boolean,
    val locale: CalendarLocale? = null,
    val firstDayOfWeek: Int? = null,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SamplePlaygroundScreen(
    locale: CalendarLocale? = null,
    modifier: Modifier = Modifier,
    renderViewport: @Composable (PlaygroundOptions, (Long) -> Unit, (Long) -> Boolean) -> Unit,
    renderDialogMonth: @Composable (DemoCalendar) -> Unit,
    rangePolicyAccepts: (Long) -> Boolean,
) {
    var calendarName by rememberSaveable { mutableStateOf(DemoCalendar.Gregorian.name) }
    var layoutName by rememberSaveable { mutableStateOf(DemoMonthLayout.Single.name) }
    var modeName by rememberSaveable { mutableStateOf(TypeSelectDay.Range.name) }
    var firstDayName by rememberSaveable { mutableStateOf(DemoFirstDayOfWeek.CalendarDefault.name) }
    var rtl by rememberSaveable { mutableStateOf(false) }
    var markers by rememberSaveable { mutableStateOf(true) }
    var customStyle by rememberSaveable { mutableStateOf(false) }
    var limitRange by rememberSaveable { mutableStateOf(false) }
    var showDialog by rememberSaveable { mutableStateOf(false) }

    val firstDay = DemoFirstDayOfWeek.valueOf(firstDayName)
    val options = PlaygroundOptions(
        calendar = DemoCalendar.valueOf(calendarName),
        layout = DemoMonthLayout.valueOf(layoutName),
        selectionMode = TypeSelectDay.valueOf(modeName),
        showMarkers = markers,
        customStyle = customStyle,
        limitRange = limitRange,
        locale = locale,
        firstDayOfWeek = firstDay.iso,
    )
    val selection = key(options.selectionMode, limitRange) {
        rememberCalendarSelectionState(mode = options.selectionMode)
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
    ) {
        Column(
            modifier = modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Playground", style = MaterialTheme.typography.headlineSmall)
            DemoChoices("Calendar", DemoCalendar.entries, options.calendar, { it.label }) {
                calendarName = it.name
            }
            DemoChoices("Layout", DemoMonthLayout.entries, options.layout, { it.name }) {
                layoutName = it.name
            }
            DemoChoices("Selection", TypeSelectDay.entries, options.selectionMode, { it.name }) {
                modeName = it.name
            }
            DemoChoices("First day", DemoFirstDayOfWeek.entries, firstDay, { it.label }) {
                firstDayName = it.name
            }
            Text(
                text = sampleWeekdayOrderPreview(firstDay.iso)
                    ?: "Each calendar uses its own default first day",
                style = MaterialTheme.typography.bodySmall,
            )

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(rtl, { rtl = !rtl }, { Text("RTL") })
                FilterChip(markers, { markers = !markers }, { Text("Markers") })
                FilterChip(customStyle, { customStyle = !customStyle }, { Text("Custom style") })
                FilterChip(limitRange, { limitRange = !limitRange }, { Text("Restrict dates") })
                OutlinedButton(onClick = { showDialog = true }) { Text("Preview dialog") }
            }

            Box(Modifier.weight(1f)) {
                renderViewport(
                    options,
                    { day ->
                        if (!limitRange || rangePolicyAccepts(day)) {
                            selection.onDayClick(day)
                        }
                    },
                    { day -> day in selection.selection },
                )
            }
        }
        if (showDialog) {
            CompositionLocalProvider(LocalPlaygroundFirstDayOfWeek provides firstDay.iso) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Calendar preview") },
                    text = { renderDialogMonth(options.calendar) },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false }) { Text("Close") }
                    },
                )
            }
        }
    }
}
