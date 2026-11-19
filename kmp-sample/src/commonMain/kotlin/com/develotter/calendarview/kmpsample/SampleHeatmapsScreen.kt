package com.develotter.calendarview.kmpsample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

internal enum class DemoHeatmapOrientation { WeeksAsColumns, WeeksAsRows }
internal enum class DemoHeatmapPalette { Green, Blue, Amber }
internal enum class DemoHeatmapCells { Square, Rounded }

internal data class HeatmapOptions(
    val calendar: DemoCalendar,
    val orientation: DemoHeatmapOrientation,
    val palette: DemoHeatmapPalette,
    val cells: DemoHeatmapCells,
)

@Composable
internal fun SampleHeatmapsScreen(
    modifier: Modifier = Modifier,
    renderHeatmap: @Composable (HeatmapOptions, (String) -> Unit) -> Unit,
) {
    val datasets = listOf(
        DemoCalendar.Gregorian,
        DemoCalendar.Hijri,
        DemoCalendar.SolarHijri,
        DemoCalendar.Chinese,
    )
    var calendarName by rememberSaveable { mutableStateOf(DemoCalendar.Gregorian.name) }
    var orientationName by rememberSaveable { mutableStateOf(DemoHeatmapOrientation.WeeksAsColumns.name) }
    var paletteName by rememberSaveable { mutableStateOf(DemoHeatmapPalette.Green.name) }
    var cellsName by rememberSaveable { mutableStateOf(DemoHeatmapCells.Rounded.name) }
    var details by rememberSaveable(calendarName) { mutableStateOf<String?>(null) }

    val options = HeatmapOptions(
        calendar = DemoCalendar.valueOf(calendarName),
        orientation = DemoHeatmapOrientation.valueOf(orientationName),
        palette = DemoHeatmapPalette.valueOf(paletteName),
        cells = DemoHeatmapCells.valueOf(cellsName),
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Heatmaps", style = MaterialTheme.typography.headlineSmall)
        DemoChoices("Dataset", datasets, options.calendar, { it.label }) { calendarName = it.name }
        DemoChoices("Orientation", DemoHeatmapOrientation.entries, options.orientation, { it.name }) {
            orientationName = it.name
        }
        DemoChoices("Palette", DemoHeatmapPalette.entries, options.palette, { it.name }) {
            paletteName = it.name
        }
        DemoChoices("Cells", DemoHeatmapCells.entries, options.cells, { it.name }) {
            cellsName = it.name
        }

        renderHeatmap(options) { details = it }
        Text(details ?: "Select a cell to inspect its activity.")
    }
}