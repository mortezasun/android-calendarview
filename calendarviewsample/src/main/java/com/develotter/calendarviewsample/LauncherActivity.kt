package com.develotter.calendarviewsample

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.develotter.calendarview.calendars.gregorian.compose.GregorianCalendarMonth
import com.develotter.calendarview.calendars.hijri.compose.HijriCalendarMonth
import com.develotter.calendarview.calendars.solarHijri.compose.SolarHijriCalendarMonth
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriEpochDay
import com.develotter.calendarview.compose.CalendarSelectionState
import com.develotter.calendarview.compose.rememberCalendarSelectionState
import com.develotter.calendarview.enums.TypeSelectDay
import ir.huri.jcal.JalaliCalendar
import java.time.LocalDate
import java.time.YearMonth
import java.time.chrono.HijrahDate
import java.util.Locale

/** Native Compose demo host using ComponentActivity and direct setContent. */
class LauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
                CalendarSample(
                    onHeatmap = { startActivity(Intent(this@LauncherActivity, HeatMapViewActivity::class.java)) },
                )
            }
        }
    }
}

/** Saves UI configuration; selection and the settled visible month are shared with the dialog. */
@Composable
private fun CalendarSample(onHeatmap: () -> Unit) {
    var calendarName by rememberSaveable { mutableStateOf(SampleCalendar.Gregorian.name) }
    var modeName by rememberSaveable { mutableStateOf(TypeSelectDay.Single.name) }
    var localeTag by rememberSaveable { mutableStateOf("en") }
    var rtl by rememberSaveable { mutableStateOf(false) }
    var previousDays by rememberSaveable { mutableStateOf(false) }
    var nextDays by rememberSaveable { mutableStateOf(false) }
    var fixedSixRows by rememberSaveable { mutableStateOf(false) }
    var anchor by rememberSaveable { mutableStateOf(LocalDate.now().toEpochDay()) }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val calendar = SampleCalendar.valueOf(calendarName)
    val state = rememberCalendarSelectionState(TypeSelectDay.valueOf(modeName))
    val locale = Locale.forLanguageTag(localeTag)

    fun move(offset: Int) {
        try {
            anchor = moveSampleMonth(calendar, anchor, offset)
            error = null
        } catch (exception: RuntimeException) {
            error = exception.message ?: "Date outside the supported calendar range"
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        Surface(Modifier.fillMaxSize()) {
            Column(
                Modifier.safeDrawingPadding().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Compose Calendar Demo", style = MaterialTheme.typography.headlineSmall)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SampleCalendar.entries.forEach { item ->
                        FilterChip(selected = calendar == item, onClick = { calendarName = item.name; error = null }, label = { Text(item.name) })
                    }
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TypeSelectDay.entries.forEach { mode ->
                        FilterChip(selected = mode.name == modeName, onClick = { modeName = mode.name }, label = { Text(mode.name) })
                    }
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("en", "de", "fa", "ar").forEach { tag ->
                        FilterChip(selected = localeTag == tag, onClick = { localeTag = tag }, label = { Text(tag) })
                    }
                    Text("RTL")
                    Switch(checked = rtl, onCheckedChange = { rtl = it })
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Previous month days")
                    Switch(checked = previousDays, onCheckedChange = { previousDays = it })
                    Text("Next month days")
                    Switch(checked = nextDays, onCheckedChange = { nextDays = it })
                    Text("Fixed 6 rows")
                    Switch(checked = fixedSixRows, onCheckedChange = { fixedSixRows = it })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { move(-1) }) { Text("Previous") }
                    Button(onClick = { anchor = LocalDate.now().toEpochDay(); error = null }) { Text("Today") }
                    Button(onClick = { move(1) }) { Text("Next") }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                CalendarSampleViewport(
                    calendar = calendar, anchor = anchor,
                    onVisibleMonthChanged = { anchor = it; error = null },
                ) { monthAnchor ->
                    SampleMonth(calendar, monthAnchor, locale, state, previousDays, nextDays, fixedSixRows)
                }
                Text("Selected dates / range endpoints (ISO):")
                Text(state.selection.selectedDays.joinToString { LocalDate.ofEpochDay(it).toString() }.ifEmpty { "None" })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = state::clear) { Text("Clear selection") }
                    TextButton(onClick = { showDialog = true }) { Text("Open dialog") }
                }
                TextButton(onClick = onHeatmap) { Text("Compose heatmap demo") }
            }
        }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = { TextButton(onClick = { showDialog = false }) { Text("Close") } },
                title = { Text("${calendar.name} calendar") },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        SampleMonth(calendar, anchor, locale, state, previousDays, nextDays, fixedSixRows)
                    }
                },
            )
        }
    }
}

@Composable
private fun SampleMonth(
    calendar: SampleCalendar, anchor: Long, locale: Locale, state: CalendarSelectionState,
    previousDays: Boolean, nextDays: Boolean, fixedSixRows: Boolean = false,
) {
    val date = LocalDate.ofEpochDay(anchor)
    when (calendar) {
        SampleCalendar.Gregorian -> GregorianCalendarMonth(
            month = YearMonth.from(date), selection = state.selection,
            onDateClick = { state.onDayClick(it.toEpochDay()) }, locale = locale, modifier = Modifier.fillMaxWidth(),
            showPreviousMonthDays = previousDays, showNextMonthDays = nextDays, fixedSixRows = fixedSixRows,
        )
        SampleCalendar.Hijri -> HijriCalendarMonth(
            month = HijrahDate.from(date), selection = state.selection,
            onDateClick = { state.onDayClick(it.toEpochDay()) }, locale = locale, modifier = Modifier.fillMaxWidth(),
            showPreviousMonthDays = previousDays, showNextMonthDays = nextDays, fixedSixRows = fixedSixRows,
        )
        SampleCalendar.SolarHijri -> SolarHijriCalendarMonth(
            month = JalaliCalendar(date), selection = state.selection,
            onDateClick = { state.onDayClick(solarHijriEpochDay(it)) }, locale = locale, modifier = Modifier.fillMaxWidth(),
            showPreviousMonthDays = previousDays, showNextMonthDays = nextDays, fixedSixRows = fixedSixRows,
        )
    }
}
