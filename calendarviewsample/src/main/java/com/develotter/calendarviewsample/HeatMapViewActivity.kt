package com.develotter.calendarviewsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import java.time.LocalDate
import java.time.Year

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
    var year by rememberSaveable { mutableStateOf(LocalDate.now().year.coerceIn(1900, 2100)) }
    var labels by rememberSaveable { mutableStateOf(true) }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val data = remember(year) {
        val first = LocalDate.of(year, 1, 1)
        (0 until Year.of(year).length()).associate { offset ->
            first.plusDays(offset.toLong()) to ((offset % 11) / 10f)
        }
    }
    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.safeDrawingPadding().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Compose heatmap: $year", style = MaterialTheme.typography.headlineSmall)
            Text("Synthetic demo data. Swipe each quarter horizontally to see all weeks.")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(enabled = year > 1900, onClick = { year--; selected = null }) { Text("Previous") }
                Button(enabled = year < 2100, onClick = { year++; selected = null }) { Text("Next") }
            }
            Row {
                Text("Weekday labels")
                Switch(checked = labels, onCheckedChange = { labels = it })
            }
            CalendarYearHeatmap(
                year = year, intensities = data,
                onDayClick = { selected = it.toString() }, showWeekdayLabels = labels,
            )
        }
    }
    selected?.let { value ->
        val date = LocalDate.parse(value)
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(value) },
            text = { Text("Demo intensity: ${data[date] ?: 0f}") },
            confirmButton = { TextButton(onClick = { selected = null }) { Text("Close") } },
        )
    }
}
