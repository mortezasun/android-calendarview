package com.develotter.calendarview.kmpsample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

internal enum class DemoCalendar(val label: String) {
    Gregorian("Gregorian"),
    Hijri("Hijri"),
    SolarHijri("Solar Hijri"),
    SolarHijriNative("Solar Hijri Native"),
    Chinese("Chinese"),
}

internal enum class DemoPage(val label: String) {
    Home("Home"),
    Calendars("Calendars"),
    Playground("Playground"),
    Heatmaps("Heatmaps"),
    Settings("Settings"),
}

@Composable
internal fun SampleHomeScreen(
    todayLabels: Map<DemoCalendar, String>,
    onOpenCalendar: (DemoCalendar) -> Unit,
    onNavigate: (DemoPage) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("CalendarView", style = MaterialTheme.typography.headlineMedium)
        Text("Today across all calendars", style = MaterialTheme.typography.titleLarge)

        DemoCalendar.entries.forEach { calendar ->
            val date = todayLabels[calendar]
            OutlinedButton(
                onClick = { onOpenCalendar(calendar) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (date == null) calendar.label else "${calendar.label} — $date")
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Explore the library", style = MaterialTheme.typography.titleMedium)
                Text("Compare calendar systems, try date selection and explore activity heatmaps.")
                DemoPage.entries.filterNot { it == DemoPage.Home }.forEach { page ->
                    OutlinedButton(onClick = { onNavigate(page) }) {
                        Text(page.label)
                    }
                }
            }
        }
    }
}