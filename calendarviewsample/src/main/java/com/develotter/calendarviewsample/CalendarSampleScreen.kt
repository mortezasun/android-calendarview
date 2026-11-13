package com.develotter.calendarviewsample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.develotter.calendarview.calendars.chineseNative.ChineseDate
import com.develotter.calendarview.calendars.chineseNative.ChineseFormat
import com.develotter.calendarview.calendars.chineseNative.ChineseMonth
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.calendars.chineseCompose.ChineseCalendarMonth
import com.develotter.calendarview.calendars.chineseCompose.GregorianChineseCalendarMonth
import com.develotter.calendarview.calendars.chineseCompose.chineseWeekData
import com.develotter.calendarview.calendars.gregorian.compose.GregorianCalendarMonth
import com.develotter.calendarview.calendars.gregorian.compose.gregorianWeekData
import com.develotter.calendarview.calendars.hijri.HijriDateProvider
import com.develotter.calendarview.calendars.hijri.OffsetHijriDateProvider
import com.develotter.calendarview.calendars.hijri.compose.HijriCalendarMonth
import com.develotter.calendarview.calendars.hijri.compose.hijriWeekData
import com.develotter.calendarview.calendars.solarHijri.compose.SolarHijriCalendarMonth
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriEpochDay
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriWeekData
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.compose.SolarHijriNativeCalendarMonth
import com.develotter.calendarview.calendars.solarHijriNative.compose.solarHijriNativeWeekData
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
import com.develotter.calendarview.selection.CalendarSelection
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
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Compose Calendar Demo",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Box(
                            Modifier
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable(onClick = ui::showChineseDemo)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                "CN 2.4.0",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                    CalendarSampleControls(ui)
                },
                content = {
                    if (ui.useHeaderApi) {
                        val navigation = rememberSampleMonthNavigation(
                            calendar = calendar,
                            anchor = ui.anchor,
                            locale = locale,
                            hijriDateProvider = hijriProvider,
                            chineseStyle = ui.chineseStyle,
                            chineseGregorianGrid = ui.chineseGregorianGrid,
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
                        monthCalendar = sampleMonthNavigationCalendar(calendar, ui.chineseGregorianGrid),
                        onVisibleMonthChanged = { ui.showMonth(it) },
                        minMonthWidth = sampleTargetMonthWidth(ui.showWeekNumbers),
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
                            chineseStyle = ui.chineseStyle,
                            chineseGregorianGrid = ui.chineseGregorianGrid,
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
                            chineseStyle = ui.chineseStyle,
                        )
                    }
                    if (ui.showAgenda) {
                        SampleAgenda(
                            calendar = calendar,
                            anchor = ui.anchor,
                            locale = locale,
                            state = state,
                            hijriProvider = hijriProvider,
                            chineseStyle = ui.chineseStyle,
                        )
                    }
                    if (ui.showChineseDetails && calendar == SampleCalendar.Chinese) {
                        ChineseDetails(
                            anchor = ui.anchor,
                            selection = state.selection,
                            style = ui.chineseStyle,
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
                    BoxWithConstraints(Modifier.fillMaxWidth()) {
                        val dialogHeight = sampleDialogViewportHeight(maxHeight)
                        CalendarSampleViewport(
                            calendar = calendar,
                            anchor = ui.anchor,
                            monthCalendar = sampleMonthNavigationCalendar(calendar, ui.chineseGregorianGrid),
                            onVisibleMonthChanged = { ui.showMonth(it) },
                            showModeSelector = false,
                            showWindowLabel = false,
                            forcedMode = SampleDisplayMode.HorizontalPager,
                            viewportHeight = dialogHeight,
                            minMonthWidth = sampleTargetMonthWidth(ui.showWeekNumbers),
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
                                chineseStyle = ui.chineseStyle,
                                chineseGregorianGrid = ui.chineseGregorianGrid,
                            )
                        }
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
    chineseStyle: ChineseTextStyle = ChineseTextStyle.English,
    chineseGregorianGrid: Boolean = false,
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
        SampleCalendar.SolarHijriNative -> SolarHijriNativeCalendarMonth(
            month = SolarHijriDate.fromLocalDate(date), selection = state.selection,
            onDateClick = { state.onDayClick(it.toEpochDay()) }, locale = locale, modifier = Modifier.fillMaxWidth(),
            showPreviousMonthDays = previousDays, showNextMonthDays = nextDays, fixedSixRows = fixedSixRows,
            today = SolarHijriDate.fromLocalDate(LocalDate.now()),
            todayDescription = todayDescription, markers = sampleMarkers(date),
            showHeader = showHeader, weekNumber = weekNumberProvider,
        )
        SampleCalendar.Chinese -> if (chineseGregorianGrid) {
            GregorianChineseCalendarMonth(
                month = YearMonth.from(date), selection = state.selection,
                onDateClick = { state.onDayClick(it.toEpochDay()) }, locale = locale, modifier = Modifier.fillMaxWidth(),
                style = chineseStyle, showPreviousMonthDays = previousDays, showNextMonthDays = nextDays,
                fixedSixRows = fixedSixRows, todayDescription = todayDescription, markers = sampleMarkers(date),
                showHeader = showHeader, weekNumber = weekNumberProvider,
            )
        } else {
            ChineseCalendarMonth(
                month = ChineseMonth.fromEpochDay(anchor), selection = state.selection,
                onDateClick = state::onDayClick, locale = locale, modifier = Modifier.fillMaxWidth(),
                style = chineseStyle, showPreviousMonthDays = previousDays, showNextMonthDays = nextDays,
                fixedSixRows = fixedSixRows, todayDescription = todayDescription, markers = sampleMarkers(date),
                showHeader = showHeader, weekNumber = weekNumberProvider,
            )
        }
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
    chineseStyle: ChineseTextStyle = ChineseTextStyle.English,
) {
    val date = LocalDate.ofEpochDay(anchor)
    val week: CalendarWeekData? = runCatching { when (calendar) {
        SampleCalendar.Gregorian -> gregorianWeekData(date, locale)
        SampleCalendar.Hijri -> hijriWeekData(
            date = hijriProvider.dateForEpochDay(anchor),
            locale = locale,
            today = hijriToday,
            dateProvider = hijriProvider,
        )
        SampleCalendar.SolarHijri -> solarHijriWeekData(JalaliCalendar(date), locale)
        SampleCalendar.SolarHijriNative -> solarHijriNativeWeekData(SolarHijriDate.fromLocalDate(date), locale)
        SampleCalendar.Chinese -> chineseWeekData(
            epochDay = anchor,
            style = chineseStyle,
            locale = locale,
            todayEpochDay = LocalDate.now().toEpochDay(),
        )
    } }.getOrNull()
    if (week == null) {
        Text("Week outside supported range", color = MaterialTheme.colorScheme.error)
        return
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
internal fun SampleAgenda(
    calendar: SampleCalendar,
    anchor: Long,
    locale: Locale,
    state: CalendarSelectionState,
    hijriProvider: HijriDateProvider,
    chineseStyle: ChineseTextStyle = ChineseTextStyle.English,
) {
    val date = LocalDate.ofEpochDay(anchor)
    val data = remember(calendar, anchor, locale, hijriProvider, chineseStyle) {
        runCatching {
            sampleMonthData(
                calendar = calendar,
                anchorEpochDay = anchor,
                locale = locale,
                hijriDateProvider = hijriProvider,
                chineseStyle = chineseStyle,
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
                modifier = Modifier.fillMaxWidth().height(
                    sampleBoundedViewportHeight(280.dp, LocalSamplePaneHeight.current),
                ),
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

/**
 * Demo detail panel for the selected day (or the visible month anchor): shared epoch identity,
 * Gregorian date, formatted Chinese date and explicit leap-month status.
 */
@Composable
private fun ChineseDetails(
    anchor: Long,
    selection: CalendarSelection,
    style: ChineseTextStyle,
) {
    val epochDay = selection.selectedDays.firstOrNull() ?: anchor
    val gregorian = LocalDate.ofEpochDay(epochDay)
    Text("Details", style = MaterialTheme.typography.titleSmall)
    Text("Epoch day: $epochDay")
    Text("Gregorian: $gregorian")
    val date = runCatching { ChineseDate.fromEpochDay(epochDay) }.getOrNull()
    if (date == null) {
        Text(
            "Chinese: outside supported range",
            color = MaterialTheme.colorScheme.error,
        )
    } else {
        Text("Chinese: ${ChineseFormat.dateLabel(date, style)}")
        Text("Chinese year: ${date.year}, month ${date.monthNumber}, day ${date.dayOfMonth}")
        Text("Leap month: ${if (date.isLeapMonth) "yes (month ${date.monthNumber})" else "no"}")
    }
}
