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
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.develotter.calendarview.calendars.gregorian.compose.GregorianCalendarMonth
import com.develotter.calendarview.calendars.gregorian.compose.gregorianWeekData
import com.develotter.calendarview.calendars.hijri.HijriDateProvider
import com.develotter.calendarview.calendars.hijri.OffsetHijriDateProvider
import com.develotter.calendarview.calendars.hijri.compose.HijriCalendarMonth
import com.develotter.calendarview.calendars.hijri.compose.hijriWeekData
import com.develotter.calendarview.calendars.solarHijri.compose.SolarHijriCalendarMonth
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriEpochDay
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriWeekData
import com.develotter.calendarview.compose.CalendarAgenda
import com.develotter.calendarview.compose.CalendarDayMarker
import com.develotter.calendarview.compose.CalendarMonthHeader
import com.develotter.calendarview.compose.CalendarSelectionState
import com.develotter.calendarview.compose.CalendarStyle
import com.develotter.calendarview.compose.CalendarWeekData
import com.develotter.calendarview.compose.CalendarWeekGrid
import com.develotter.calendarview.compose.LocalCalendarStyle
import com.develotter.calendarview.compose.rememberCalendarSelectionState
import com.develotter.calendarview.enums.TypeSelectDay
import com.develotter.calendarview.selection.CalendarRangePolicy
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
    var showWeek by rememberSaveable { mutableStateOf(false) }
    var showAgenda by rememberSaveable { mutableStateOf(false) }
    var showWeekNumbers by rememberSaveable { mutableStateOf(false) }
    var customStyle by rememberSaveable { mutableStateOf(false) }
    var useHeaderApi by rememberSaveable { mutableStateOf(false) }
    var limitRange by rememberSaveable { mutableStateOf(false) }
    var hijriOffset by rememberSaveable { mutableIntStateOf(0) }
    var anchor by rememberSaveable { mutableStateOf(LocalDate.now().toEpochDay()) }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val calendar = SampleCalendar.valueOf(calendarName)
    val locale = Locale.forLanguageTag(localeTag)
    val hijriProvider: HijriDateProvider = remember(hijriOffset) { OffsetHijriDateProvider(hijriOffset) }
    val hijriToday = remember(hijriOffset) {
        hijriProvider.dateForEpochDay(LocalDate.now().toEpochDay())
    }
    val rangePolicy = remember(limitRange) {
        if (limitRange) CalendarRangePolicy(minRangeLength = 2, maxRangeLength = 14) else CalendarRangePolicy()
    }
    val state = rememberCalendarSelectionState(
        mode = TypeSelectDay.valueOf(modeName),
        rangePolicy = rangePolicy,
    )
    val calendarStyle = if (customStyle) {
        CalendarStyle(
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            todayBorderColor = MaterialTheme.colorScheme.tertiary,
            markerSize = 8.dp,
        )
    } else {
        CalendarStyle()
    }

    fun move(offset: Int) {
        try {
            anchor = moveSampleMonth(calendar, anchor, offset)
            error = null
        } catch (exception: RuntimeException) {
            error = exception.message ?: "Date outside the supported calendar range"
        }
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
        LocalCalendarStyle provides calendarStyle,
    ) {
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
                    Text("Week view")
                    Switch(checked = showWeek, onCheckedChange = { showWeek = it })
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Agenda")
                    Switch(checked = showAgenda, onCheckedChange = { showAgenda = it })
                    Text("Week numbers")
                    Switch(checked = showWeekNumbers, onCheckedChange = { showWeekNumbers = it })
                    Text("Custom style")
                    Switch(checked = customStyle, onCheckedChange = { customStyle = it })
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Library month header")
                    Switch(checked = useHeaderApi, onCheckedChange = { useHeaderApi = it })
                    Text("Range 2–14 days")
                    Switch(checked = limitRange, onCheckedChange = { limitRange = it })
                }
                if (calendar == SampleCalendar.Hijri) {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Hijri method")
                        listOf(-1, 0, 1).forEach { offset ->
                            FilterChip(
                                selected = hijriOffset == offset,
                                onClick = { hijriOffset = offset },
                                label = {
                                    Text(
                                        when (offset) {
                                            -1 -> "−1 day"
                                            0 -> "Umm al-Qura"
                                            else -> "+1 day"
                                        },
                                    )
                                },
                            )
                        }
                    }
                }
                if (useHeaderApi) {
                    val navigation = rememberSampleMonthNavigation(
                        calendar = calendar,
                        anchor = anchor,
                        locale = locale,
                        hijriDateProvider = hijriProvider,
                    ) { visible ->
                        anchor = visible
                        error = null
                    }
                    CalendarMonthHeader(
                        title = navigation.title,
                        state = navigation.state,
                        previousMonthContentDescription = "Previous month",
                        nextMonthContentDescription = "Next month",
                        todayLabel = sampleTodayDescription(locale),
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { move(-1) }) { Text("Previous") }
                        Button(onClick = { anchor = LocalDate.now().toEpochDay(); error = null }) { Text("Today") }
                        Button(onClick = { move(1) }) { Text("Next") }
                    }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                CalendarSampleViewport(
                    calendar = calendar,
                    anchor = anchor,
                    onVisibleMonthChanged = { anchor = it; error = null },
                ) { monthAnchor ->
                    SampleMonth(
                        calendar = calendar,
                        anchor = monthAnchor,
                        locale = locale,
                        state = state,
                        previousDays = previousDays,
                        nextDays = nextDays,
                        fixedSixRows = fixedSixRows,
                        hijriProvider = hijriProvider,
                        hijriToday = hijriToday,
                        showHeader = !useHeaderApi,
                        showWeekNumbers = showWeekNumbers,
                    )
                }
                if (showWeek) {
                    SampleWeek(
                        calendar = calendar,
                        anchor = anchor,
                        locale = locale,
                        state = state,
                        hijriProvider = hijriProvider,
                        hijriToday = hijriToday,
                        showWeekNumbers = showWeekNumbers,
                    )
                }
                if (showAgenda) {
                    SampleAgenda(
                        calendar = calendar,
                        anchor = anchor,
                        locale = locale,
                        state = state,
                        hijriProvider = hijriProvider,
                    )
                }
                Text("Selected dates / range endpoints (ISO):")
                Text(state.selection.selectedDays.joinToString { LocalDate.ofEpochDay(it).toString() }.ifEmpty { "None" })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = state::clear) { Text("Clear selection") }
                    if (state.selection.mode == TypeSelectDay.Range) {
                        TextButton(onClick = { state.selectRange(anchor, anchor + 4) }) {
                            Text("Select 5-day range")
                        }
                    }
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
                    CalendarSampleViewport(
                        calendar = calendar,
                        anchor = anchor,
                        onVisibleMonthChanged = { anchor = it; error = null },
                        showModeSelector = false,
                        showWindowLabel = false,
                        forcedMode = SampleDisplayMode.HorizontalPager,
                        viewportHeight = 340.dp,
                    ) { monthAnchor ->
                        SampleMonth(
                            calendar = calendar,
                            anchor = monthAnchor,
                            locale = locale,
                            state = state,
                            previousDays = previousDays,
                            nextDays = nextDays,
                            fixedSixRows = fixedSixRows,
                            hijriProvider = hijriProvider,
                            hijriToday = hijriToday,
                            showWeekNumbers = showWeekNumbers,
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun SampleMonth(
    calendar: SampleCalendar,
    anchor: Long,
    locale: Locale,
    state: CalendarSelectionState,
    previousDays: Boolean,
    nextDays: Boolean,
    fixedSixRows: Boolean,
    hijriProvider: HijriDateProvider,
    hijriToday: HijrahDate,
    showHeader: Boolean = true,
    showWeekNumbers: Boolean = false,
) {
    val date = LocalDate.ofEpochDay(anchor)
    val todayDescription = sampleTodayDescription(locale)
    val weekNumberProvider: ((Long) -> String?)? = if (showWeekNumbers) {
        { epochDay -> sampleWeekNumber(epochDay, locale) }
    } else {
        null
    }
    when (calendar) {
        SampleCalendar.Gregorian -> GregorianCalendarMonth(
            month = YearMonth.from(date), selection = state.selection,
            onDateClick = { state.onDayClick(it.toEpochDay()) }, locale = locale, modifier = Modifier.fillMaxWidth(),
            showPreviousMonthDays = previousDays, showNextMonthDays = nextDays, fixedSixRows = fixedSixRows,
            todayDescription = todayDescription, markers = sampleMarkers(date),
            showHeader = showHeader, weekNumber = weekNumberProvider,
        )
        SampleCalendar.Hijri -> HijriCalendarMonth(
            month = hijriProvider.dateForEpochDay(anchor), selection = state.selection,
            onDateClick = { state.onDayClick(hijriProvider.epochDayFor(it)) },
            locale = locale, modifier = Modifier.fillMaxWidth(),
            showPreviousMonthDays = previousDays, showNextMonthDays = nextDays, fixedSixRows = fixedSixRows,
            today = hijriToday, todayDescription = todayDescription, markers = sampleMarkers(date),
            showHeader = showHeader, weekNumber = weekNumberProvider, dateProvider = hijriProvider,
        )
        SampleCalendar.SolarHijri -> SolarHijriCalendarMonth(
            month = JalaliCalendar(date), selection = state.selection,
            onDateClick = { state.onDayClick(solarHijriEpochDay(it)) }, locale = locale, modifier = Modifier.fillMaxWidth(),
            showPreviousMonthDays = previousDays, showNextMonthDays = nextDays, fixedSixRows = fixedSixRows,
            todayDescription = todayDescription, markers = sampleMarkers(date),
            showHeader = showHeader, weekNumber = weekNumberProvider,
        )
    }
}

/** Demo-only weekly view sharing the month selection and event markers. */
@Composable
private fun SampleWeek(
    calendar: SampleCalendar,
    anchor: Long,
    locale: Locale,
    state: CalendarSelectionState,
    hijriProvider: HijriDateProvider,
    hijriToday: HijrahDate,
    showWeekNumbers: Boolean,
) {
    val date = LocalDate.ofEpochDay(anchor)
    val week: CalendarWeekData = when (calendar) {
        SampleCalendar.Gregorian -> gregorianWeekData(date, locale)
        SampleCalendar.Hijri -> hijriWeekData(
            date = hijriProvider.dateForEpochDay(anchor),
            locale = locale,
            today = hijriToday,
            dateProvider = hijriProvider,
        )
        SampleCalendar.SolarHijri -> solarHijriWeekData(JalaliCalendar(date), locale)
    }
    val weekNumber = if (showWeekNumbers) {
        week.weekNumber ?: sampleWeekNumber(week.days.first().epochDay, locale)
    } else {
        null
    }
    CalendarWeekGrid(
        title = week.title, weekdayLabels = week.weekdayLabels, days = week.days,
        selection = state.selection, onDayClick = { state.onDayClick(it) },
        weekNumber = weekNumber, todayDescription = sampleTodayDescription(locale),
        markers = sampleMarkers(date),
    )
}

/** Demo-only agenda for the current month, built from the shared CalendarMonthData contract. */
@Composable
private fun SampleAgenda(
    calendar: SampleCalendar,
    anchor: Long,
    locale: Locale,
    state: CalendarSelectionState,
    hijriProvider: HijriDateProvider,
) {
    val date = LocalDate.ofEpochDay(anchor)
    val data = remember(calendar, anchor, locale, hijriProvider) {
        runCatching {
            sampleMonthData(
                calendar = calendar,
                anchorEpochDay = anchor,
                locale = locale,
                hijriDateProvider = hijriProvider,
            )
        }
    }
    Text("Agenda: ${data.getOrNull()?.title ?: "current month"}")
    data.fold(
        onSuccess = { monthData ->
            CalendarAgenda(
                days = monthData.days,
                selection = state.selection,
                onDayClick = { state.onDayClick(it) },
                modifier = Modifier.fillMaxWidth().height(280.dp),
                markers = sampleMarkers(date),
                todayDescription = sampleTodayDescription(locale),
            )
        },
        onFailure = { exception ->
            Text(
                exception.message ?: "Unsupported agenda month",
                color = MaterialTheme.colorScheme.error,
            )
        },
    )
}

/** Demo-only marker every week; the library leaves event semantics to the host. */
@Composable
private fun sampleMarkers(date: LocalDate): Map<Long, List<CalendarDayMarker>> {
    val color = MaterialTheme.colorScheme.secondary
    return remember(date.toEpochDay(), color) {
        val first = date.withDayOfMonth(1)
        (0 until date.lengthOfMonth() step 7).associate { offset ->
            first.plusDays(offset.toLong()).toEpochDay() to
                listOf(CalendarDayMarker(color, "Sample event"))
        }
    }
}

/** Demo-only localized "today" description; the library deliberately ships no UI strings. */
private fun sampleTodayDescription(locale: Locale): String = when (locale.language) {
    "de" -> "Heute"
    "fa" -> "امروز"
    "ar" -> "اليوم"
    else -> "Today"
}
