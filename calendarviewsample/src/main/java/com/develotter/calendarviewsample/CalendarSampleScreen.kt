package com.develotter.calendarviewsample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
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

/** Saves UI configuration; selection and the settled visible month are shared with the dialog. */
@Composable
internal fun CalendarSample(onHeatmap: () -> Unit) {
    val ui = rememberCalendarSampleState()
    val calendar = ui.calendar
    val locale = ui.locale
    val hijriProvider: HijriDateProvider = remember(ui.hijriOffset) { OffsetHijriDateProvider(ui.hijriOffset) }
    val hijriToday = remember(ui.hijriOffset) {
        hijriProvider.dateForEpochDay(LocalDate.now().toEpochDay())
    }
    val rangePolicy = remember(ui.limitRange) {
        if (ui.limitRange) CalendarRangePolicy(minRangeLength = 2, maxRangeLength = 14) else CalendarRangePolicy()
    }
    val state = rememberCalendarSelectionState(
        mode = ui.mode,
        rangePolicy = rangePolicy,
    )
    val calendarStyle = if (ui.customStyle) {
        CalendarStyle(
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            todayBorderColor = MaterialTheme.colorScheme.tertiary,
            markerSize = 8.dp,
        )
    } else {
        CalendarStyle()
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides if (ui.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
        LocalCalendarStyle provides calendarStyle,
    ) {
        Surface(Modifier.fillMaxSize()) {
            CalendarSampleAdaptiveLayout(
                header = {
                    Text("Compose Calendar Demo", style = MaterialTheme.typography.headlineSmall)
                    CalendarSampleControls(ui)
                },
                content = {
                    if (ui.useHeaderApi) {
                        val navigation = rememberSampleMonthNavigation(
                            calendar = calendar,
                            anchor = ui.anchor,
                            locale = locale,
                            hijriDateProvider = hijriProvider,
                        ) { visible -> ui.showMonth(visible) }
                        CalendarMonthHeader(
                            title = navigation.title,
                            state = navigation.state,
                            previousMonthContentDescription = "Previous month",
                            nextMonthContentDescription = "Next month",
                            todayLabel = sampleTodayDescription(locale),
                        )
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { ui.move(-1) }) { Text("Previous") }
                            Button(onClick = { ui.goToToday() }) { Text("Today") }
                            Button(onClick = { ui.move(1) }) { Text("Next") }
                        }
                    }
                    ui.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    CalendarSampleViewport(
                        calendar = calendar,
                        anchor = ui.anchor,
                        onVisibleMonthChanged = { ui.showMonth(it) },
                    ) { monthAnchor ->
                        SampleMonth(
                            calendar = calendar,
                            anchor = monthAnchor,
                            locale = locale,
                            state = state,
                            previousDays = ui.previousDays,
                            nextDays = ui.nextDays,
                            fixedSixRows = ui.fixedSixRows,
                            hijriProvider = hijriProvider,
                            hijriToday = hijriToday,
                            showHeader = !ui.useHeaderApi,
                            showWeekNumbers = ui.showWeekNumbers,
                        )
                    }
                    if (ui.showWeek) {
                        SampleWeek(
                            calendar = calendar,
                            anchor = ui.anchor,
                            locale = locale,
                            state = state,
                            hijriProvider = hijriProvider,
                            hijriToday = hijriToday,
                            showWeekNumbers = ui.showWeekNumbers,
                        )
                    }
                    if (ui.showAgenda) {
                        SampleAgenda(
                            calendar = calendar,
                            anchor = ui.anchor,
                            locale = locale,
                            state = state,
                            hijriProvider = hijriProvider,
                        )
                    }
                },
                actions = {
                    Text("Selected dates / range endpoints (ISO):")
                    Text(state.selection.selectedDays.joinToString { LocalDate.ofEpochDay(it).toString() }.ifEmpty { "None" })
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = state::clear) { Text("Clear selection") }
                        if (state.selection.mode == TypeSelectDay.Range) {
                            TextButton(onClick = { state.selectRange(ui.anchor, ui.anchor + 4) }) {
                                Text("Select 5-day range")
                            }
                        }
                        TextButton(onClick = { ui.showDialog = true }) { Text("Open dialog") }
                    }
                    TextButton(onClick = onHeatmap) { Text("Compose heatmap demo") }
                },
            )
        }
        if (ui.showDialog) {
            AlertDialog(
                onDismissRequest = { ui.showDialog = false },
                confirmButton = { TextButton(onClick = { ui.showDialog = false }) { Text("Close") } },
                title = { Text("${calendar.name} calendar") },
                text = {
                    CalendarSampleViewport(
                        calendar = calendar,
                        anchor = ui.anchor,
                        onVisibleMonthChanged = { ui.showMonth(it) },
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
                            previousDays = ui.previousDays,
                            nextDays = ui.nextDays,
                            fixedSixRows = ui.fixedSixRows,
                            hijriProvider = hijriProvider,
                            hijriToday = hijriToday,
                            showWeekNumbers = ui.showWeekNumbers,
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
