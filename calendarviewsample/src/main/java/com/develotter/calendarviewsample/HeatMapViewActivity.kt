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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.develotter.calendarview.compose.CalendarYearHeatmap
import com.develotter.calendarview.compose.HeatmapOrientation
import com.develotter.calendarview.compose.HeatmapStyle
import com.develotter.calendarview.compose.rememberCalendarSelectionState
import com.develotter.calendarview.enums.TypeSelectDay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
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

/** Demo color scales for [CalendarYearHeatmap]'s colorForIntensity. */
private enum class SampleHeatmapPalette { RedGreen, Green, Theme }

/** Demo cell geometries for [HeatmapStyle]. */
private enum class SampleHeatmapCells { Default, Compact, Rounded }

/** Demo today-border colors, mirroring todayBorderColor of the month views. */
private enum class SampleTodayColor { Theme, Blue, Magenta }

private val firstDayOptions = listOf<DayOfWeek?>(null, DayOfWeek.MONDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

/**
 * Deterministic synthetic intensities; no user activity or persistence is implied.
 * Offers the same configuration as the month demo (locale, RTL, first weekday, disabled
 * dates, selection modes) plus heatmap-specific orientation, palette and cell style.
 */
@Composable
private fun HeatmapSample() {
    var calendarName by rememberSaveable { mutableStateOf(SampleCalendar.Gregorian.name) }
    val calendar = SampleCalendar.valueOf(calendarName)
    var year by rememberSaveable(calendarName) { mutableStateOf(sampleHeatmapCurrentYear(calendar)) }
    var labels by rememberSaveable { mutableStateOf(true) }
    var header by rememberSaveable { mutableStateOf(true) }
    var localeTag by rememberSaveable { mutableStateOf("en") }
    var rtl by rememberSaveable { mutableStateOf(false) }
    var firstDayIndex by rememberSaveable { mutableIntStateOf(0) }
    var disableFuture by rememberSaveable { mutableStateOf(false) }
    var orientationName by rememberSaveable { mutableStateOf(HeatmapOrientation.WeeksAsColumns.name) }
    var paletteName by rememberSaveable { mutableStateOf(SampleHeatmapPalette.RedGreen.name) }
    var cellsName by rememberSaveable { mutableStateOf(SampleHeatmapCells.Default.name) }
    var todayColorName by rememberSaveable { mutableStateOf(SampleTodayColor.Theme.name) }
    var modeName by rememberSaveable { mutableStateOf(TypeSelectDay.None.name) }
    var selected by rememberSaveable { mutableStateOf<Long?>(null) }
    val mode = TypeSelectDay.valueOf(modeName)
    val state = rememberCalendarSelectionState(mode)
    val locale = Locale.forLanguageTag(localeTag)
    val firstDay = firstDayOptions[firstDayIndex]
    val yearRange = remember(calendar) { sampleHeatmapYearRange(calendar) }
    val months = remember(calendar, year, localeTag, firstDay, disableFuture) {
        val today = LocalDate.now()
        runCatching {
            sampleHeatmapYear(
                calendar, year, locale, today = today, firstDayOfWeek = firstDay,
                isDateEnabled = { !disableFuture || !it.isAfter(today) },
            )
        }
    }
    val scheme = MaterialTheme.colorScheme
    val colorForIntensity: (Float) -> Color = when (SampleHeatmapPalette.valueOf(paletteName)) {
        SampleHeatmapPalette.RedGreen -> { value -> lerp(Color.Red, Color.Green, value) }
        SampleHeatmapPalette.Green -> { value -> lerp(Color(0xFFEBEDF0), Color(0xFF216E39), value) }
        SampleHeatmapPalette.Theme -> { value -> lerp(scheme.surfaceVariant, scheme.primary, value) }
    }
    val style = when (SampleHeatmapCells.valueOf(cellsName)) {
        SampleHeatmapCells.Default -> HeatmapStyle()
        SampleHeatmapCells.Compact -> HeatmapStyle(cellSize = 28.dp, cellSpacing = 2.dp, weekdayLabelWidth = 40.dp)
        SampleHeatmapCells.Rounded -> HeatmapStyle(
            cellSize = 40.dp, cellSpacing = 4.dp, cellShape = RoundedCornerShape(8.dp), weekdayLabelWidth = 48.dp,
        )
    }
    val todayBorderColor = when (SampleTodayColor.valueOf(todayColorName)) {
        SampleTodayColor.Theme -> scheme.tertiary
        SampleTodayColor.Blue -> Color(0xFF1565C0)
        SampleTodayColor.Magenta -> Color(0xFFC2185B)
    }

    CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        Surface(Modifier.fillMaxSize()) {
            Column(
                Modifier.safeDrawingPadding().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("${calendar.name} heatmap: $year", style = MaterialTheme.typography.headlineSmall)
                val orientation = HeatmapOrientation.valueOf(orientationName)
                Text(
                    if (orientation == HeatmapOrientation.WeeksAsColumns) {
                        "Synthetic demo data. Swipe each month horizontally to see all weeks."
                    } else {
                        "Synthetic demo data. Each row is one week."
                    },
                )
                ChipRow {
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
                ChipRow {
                    TypeSelectDay.entries.forEach { item ->
                        FilterChip(selected = mode == item, onClick = { modeName = item.name }, label = { Text(item.name) })
                    }
                }
                ChipRow {
                    listOf("en", "de", "fa", "ar").forEach { tag ->
                        FilterChip(selected = localeTag == tag, onClick = { localeTag = tag }, label = { Text(tag) })
                    }
                    Text("RTL")
                    Switch(checked = rtl, onCheckedChange = { rtl = it })
                }
                ChipRow {
                    Text("First day")
                    firstDayOptions.forEachIndexed { index, day ->
                        FilterChip(
                            selected = firstDayIndex == index,
                            onClick = { firstDayIndex = index },
                            label = { Text(day?.getDisplayName(TextStyle.SHORT, Locale.ENGLISH) ?: "Default") },
                        )
                    }
                }
                ChipRow {
                    HeatmapOrientation.entries.forEach { item ->
                        FilterChip(
                            selected = orientationName == item.name,
                            onClick = { orientationName = item.name },
                            label = { Text(if (item == HeatmapOrientation.WeeksAsColumns) "Weeks as columns" else "Weeks as rows") },
                        )
                    }
                }
                ChipRow {
                    Text("Colors")
                    SampleHeatmapPalette.entries.forEach { item ->
                        FilterChip(selected = paletteName == item.name, onClick = { paletteName = item.name }, label = { Text(item.name) })
                    }
                }
                ChipRow {
                    Text("Cells")
                    SampleHeatmapCells.entries.forEach { item ->
                        FilterChip(selected = cellsName == item.name, onClick = { cellsName = item.name }, label = { Text(item.name) })
                    }
                }
                ChipRow {
                    Text("Today border")
                    SampleTodayColor.entries.forEach { item ->
                        FilterChip(selected = todayColorName == item.name, onClick = { todayColorName = item.name }, label = { Text(item.name) })
                    }
                }
                ChipRow {
                    Text("Header")
                    Switch(checked = header, onCheckedChange = { header = it })
                    Text("Weekday labels")
                    Switch(checked = labels, onCheckedChange = { labels = it })
                    Text("Disable future days")
                    Switch(checked = disableFuture, onCheckedChange = { disableFuture = it })
                }
                months.fold(
                    onSuccess = { monthData ->
                        CalendarYearHeatmap(
                            months = monthData,
                            onDayClick = { day ->
                                if (mode == TypeSelectDay.None) selected = day.epochDay else state.onDayClick(day.epochDay)
                            },
                            locale = locale,
                            showWeekdayLabels = labels,
                            showHeader = header,
                            orientation = orientation,
                            style = style,
                            selection = if (mode == TypeSelectDay.None) null else state.selection,
                            todayBorderColor = todayBorderColor,
                            header = { Text(it, style = MaterialTheme.typography.titleMedium) },
                            weekdayContent = { Text(it, style = MaterialTheme.typography.labelSmall) },
                            colorForIntensity = colorForIntensity,
                        )
                    },
                    onFailure = { error ->
                        Text(
                            error.message ?: "Unsupported calendar year",
                            color = MaterialTheme.colorScheme.error,
                        )
                    },
                )
                if (mode != TypeSelectDay.None) {
                    Text("Selected dates / range endpoints (ISO):")
                    Text(state.selection.selectedDays.joinToString { LocalDate.ofEpochDay(it).toString() }.ifEmpty { "None" })
                    TextButton(onClick = state::clear) { Text("Clear selection") }
                }
            }
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

@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) { content() }
}
