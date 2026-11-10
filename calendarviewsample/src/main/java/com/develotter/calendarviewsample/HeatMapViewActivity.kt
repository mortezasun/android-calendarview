package com.develotter.calendarviewsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.develotter.calendarview.compose.CalendarYearHeatmap
import java.util.Locale

/** Heatmap demo rendered entirely in Compose using ComponentActivity and direct setContent. */
class HeatMapViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
                HeatmapSample()
            }
        }
    }
}

/** Deterministic synthetic intensities; no user activity or persistence is implied. */
@Composable
private fun HeatmapSample() {
    var calendarName by rememberSaveable { mutableStateOf(SampleCalendar.Gregorian.name) }
    val calendar = SampleCalendar.valueOf(calendarName)
    var year by rememberSaveable(calendarName) { mutableStateOf(sampleHeatmapCurrentYear(calendar)) }
    var labels by rememberSaveable { mutableStateOf(true) }
    var selected by rememberSaveable { mutableStateOf<Long?>(null) }
    val yearRange = remember(calendar) { sampleHeatmapYearRange(calendar) }
    val months = remember(calendar, year) {
        runCatching { sampleHeatmapYear(calendar, year, Locale.ENGLISH) }
    }
    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.safeDrawingPadding().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("${calendar.name} heatmap: $year", style = MaterialTheme.typography.headlineSmall)
            Text("Synthetic demo data. Swipe each month horizontally to see all weeks.")
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SampleCalendar.entries.forEach { item ->
                    FilterChip(
                        selected = calendar == item,
                        onClick = { calendarName = item.name; selected = null },
                        label = { Text(item.name) },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    enabled = year > yearRange.first,
                    onClick = { year--; selected = null },
                ) { Text("Previous") }
                Button(
                    enabled = year < yearRange.last,
                    onClick = { year++; selected = null },
                ) { Text("Next") }
            }
            Row {
                Text("Weekday labels")
                Switch(checked = labels, onCheckedChange = { labels = it })
            }
            months.fold(
                onSuccess = { monthData ->
                    CalendarYearHeatmap(
                        months = monthData,
                        onDayClick = { selected = it.epochDay },
                        showWeekdayLabels = labels,
                    )
                },
                onFailure = { error ->
                    Text(
                        error.message ?: "Unsupported calendar year",
                        color = MaterialTheme.colorScheme.error,
                    )
                },
            )
        }
    }
    selected?.let { epochDay ->
        val day = months.getOrNull()
            ?.asSequence()
            ?.flatMap { month -> month.weeks.asSequence().flatten() }
            ?.filterNotNull()
            ?.firstOrNull { it.epochDay == epochDay }
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(day?.label ?: "") },
            text = { Text("Demo intensity: ${day?.intensity ?: 0f}") },
            confirmButton = { TextButton(onClick = { selected = null }) { Text("Close") } },
        )
    }
}
