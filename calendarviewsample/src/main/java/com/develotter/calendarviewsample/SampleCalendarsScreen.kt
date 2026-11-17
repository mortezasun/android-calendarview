package com.develotter.calendarviewsample

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.calendars.hijri.HijriDate
import com.develotter.calendarview.calendars.hijri.HijriDateProvider
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.compose.CalendarMonthHeader
import com.develotter.calendarview.compose.CalendarSelectionState
import com.develotter.calendarview.enums.TypeSelectDay
import java.time.LocalDate

/**
 * Focused single-calendar explorer for each published module (`Gregorian`, `Hijri`,
 * `SolarHijriNative`, `SolarHijri`, `Chinese`). Highlights engine-specific features, the dynamic
 * secondary-label selector, Month / Week / Agenda modes, and a live cross-calendar date inspector.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SampleCalendarsScreen(
    ui: CalendarSampleState,
    state: CalendarSelectionState,
    hijriProvider: HijriDateProvider,
    hijriToday: HijriDate,
    modifier: Modifier = Modifier,
) {
    val calendar = ui.calendar
    val catalogInfo = remember(calendar) {
        SampleCalendarCatalog.first { it.calendar == calendar }
    }
    val secondaryCalendar = if (calendar == SampleCalendar.Chinese && ui.chineseGregorianGrid) {
        SampleSecondaryCalendar.Chinese
    } else {
        ui.secondaryCalendar
    }
    val secondaryLabelProvider = remember(secondaryCalendar, hijriProvider, ui.chineseStyleName) {
        sampleSecondaryLabelProvider(
            secondary = secondaryCalendar,
            hijriDateProvider = hijriProvider,
            chineseStyle = ui.chineseStyle,
        )
    }
    val secondaryAccessibilityProvider = remember(secondaryCalendar, hijriProvider, ui.chineseStyleName) {
        sampleSecondaryAccessibilityProvider(
            secondary = secondaryCalendar,
            hijriDateProvider = hijriProvider,
            chineseStyle = ui.chineseStyle,
        )
    }

    val inspectedEpochDay = state.selection.selectedDays.firstOrNull() ?: ui.anchor
    val conversions = remember(inspectedEpochDay, hijriProvider, ui.chineseStyleName) {
        sampleAllCalendarConversions(
            epochDay = inspectedEpochDay,
            hijriDateProvider = hijriProvider,
            chineseStyle = ui.chineseStyle,
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SampleCalendar.entries.forEach { item ->
                FilterChip(
                    selected = calendar == item,
                    onClick = { ui.selectCalendar(item) },
                    label = { Text(item.name) },
                )
            }
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${catalogInfo.title} Module",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = catalogInfo.artifact,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = catalogInfo.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Alternative Secondary Calendar",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Dynamically render a second calendar label inside every day cell.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    val swappableTarget = ui.secondaryCalendar.toSampleCalendarOrNull()
                    if (swappableTarget != null && swappableTarget != calendar) {
                        IconButton(
                            onClick = {
                                val previousPrimary = calendar.toSecondaryCalendar()
                                ui.selectCalendar(swappableTarget)
                                ui.secondaryCalendarName = previousPrimary.name
                            },
                        ) {
                            Icon(
                                SampleIcons.SwapCalendar,
                                contentDescription = "Swap primary and secondary calendars",
                            )
                        }
                    }
                }
                SecondaryCalendarSelector(
                    selected = ui.secondaryCalendar,
                    onSelect = { ui.secondaryCalendarName = it.name },
                )
            }
        }

        CalendarSpecificControlsCard(ui = ui, calendar = calendar)

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            val modes = SampleCalendarViewMode.entries
            modes.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = ui.calendarViewMode == mode,
                    onClick = { ui.calendarViewModeName = mode.name },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                    label = { Text(mode.label) },
                )
            }
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val navigation = rememberSampleMonthNavigation(
                    calendar = calendar,
                    anchor = ui.anchor,
                    locale = ui.locale,
                    hijriDateProvider = hijriProvider,
                    chineseStyle = ui.chineseStyle,
                    chineseGregorianGrid = ui.chineseGregorianGrid,
                ) { visible -> ui.showMonth(visible) }
                CalendarMonthHeader(
                    title = navigation.title,
                    state = navigation.state,
                    previousMonthContentDescription = "Previous month",
                    nextMonthContentDescription = "Next month",
                    todayLabel = sampleTodayDescription(ui.locale),
                )
                ui.error?.let { errorText ->
                    Text(
                        text = errorText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                when (ui.calendarViewMode) {
                    SampleCalendarViewMode.Month -> SampleMonth(
                        calendar = calendar,
                        anchor = ui.anchor,
                        locale = ui.locale,
                        state = state,
                        previousDays = ui.previousDays,
                        nextDays = ui.nextDays,
                        fixedSixRows = ui.fixedSixRows,
                        hijriProvider = hijriProvider,
                        hijriToday = hijriToday,
                        showHeader = false,
                        showWeekNumbers = ui.showWeekNumbers,
                        chineseStyle = ui.chineseStyle,
                        chineseGregorianGrid = ui.chineseGregorianGrid,
                        secondaryLabel = secondaryLabelProvider,
                        secondaryAccessibilityLabel = secondaryAccessibilityProvider,
                    )
                    SampleCalendarViewMode.Week -> SampleWeek(
                        calendar = calendar,
                        anchor = ui.anchor,
                        locale = ui.locale,
                        state = state,
                        hijriProvider = hijriProvider,
                        hijriToday = hijriToday,
                        showWeekNumbers = ui.showWeekNumbers,
                        chineseStyle = ui.chineseStyle,
                        secondaryLabel = secondaryLabelProvider,
                        secondaryAccessibilityLabel = secondaryAccessibilityProvider,
                    )
                    SampleCalendarViewMode.Agenda -> SampleAgenda(
                        calendar = calendar,
                        anchor = ui.anchor,
                        locale = ui.locale,
                        state = state,
                        hijriProvider = hijriProvider,
                        chineseStyle = ui.chineseStyle,
                        secondaryLabel = secondaryLabelProvider,
                        secondaryAccessibilityLabel = secondaryAccessibilityProvider,
                    )
                }
                HorizontalDivider()
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    listOf(
                        TypeSelectDay.Single,
                        TypeSelectDay.Multiple,
                        TypeSelectDay.Range,
                    ).forEach { mode ->
                        FilterChip(
                            selected = ui.mode == mode,
                            onClick = { ui.modeName = mode.name },
                            label = { Text(mode.name) },
                        )
                    }
                    if (state.selection.selectedDays.isNotEmpty()) {
                        TextButton(onClick = state::clear) { Text("Clear selection") }
                    }
                }
            }
        }

        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (state.selection.selectedDays.isNotEmpty()) {
                            "Selected Date Inspector"
                        } else {
                            "Month Anchor Date Inspector"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${LocalDate.ofEpochDay(inspectedEpochDay)} (#$inspectedEpochDay)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                HorizontalDivider()
                conversions.forEach { conversion ->
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = conversion.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = conversion.formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        conversion.detail?.let { detail ->
                            Text(
                                text = detail,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CalendarSpecificControlsCard(
    ui: CalendarSampleState,
    calendar: SampleCalendar,
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Module Features & Grid Options",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            when (calendar) {
                SampleCalendar.Hijri -> {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Method offset", style = MaterialTheme.typography.labelMedium)
                        listOf(-1, 0, 1).forEach { offset ->
                            FilterChip(
                                selected = ui.hijriOffset == offset,
                                onClick = { ui.hijriOffset = offset },
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
                SampleCalendar.Chinese -> {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Script", style = MaterialTheme.typography.labelMedium)
                        ChineseTextStyle.entries.forEach { style ->
                            FilterChip(
                                selected = ui.chineseStyle == style,
                                onClick = { ui.chineseStyleName = style.name },
                                label = { Text(chineseStyleChipLabel(style)) },
                            )
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        FilledTonalButton(onClick = ui::showChineseDemo) {
                            Text("2023 Leap 2nd Month")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("Gregorian grid + Chinese labels", style = MaterialTheme.typography.bodySmall)
                            Switch(
                                checked = ui.chineseGregorianGrid,
                                onCheckedChange = { ui.chineseGregorianGrid = it },
                            )
                        }
                    }
                }
                SampleCalendar.SolarHijriNative -> {
                    val solarDate = runCatching { SolarHijriDate.fromEpochDay(ui.anchor) }.getOrNull()
                    if (solarDate != null) {
                        val leapLabel = if (solarDate.isLeapYear()) {
                            "Leap year (${solarDate.year} SH · Esfand has 30 days)"
                        } else {
                            "Common year (${solarDate.year} SH · Esfand has 29 days)"
                        }
                        Text(
                            text = "Borkowski Astronomical Engine: $leapLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                SampleCalendar.SolarHijri -> {
                    Text(
                        text = "Bridge Module: Delegates arithmetic and localization cleanly to :calendar-solarHijri-native.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                SampleCalendar.Gregorian -> {
                    Text(
                        text = "ISO Chronology: Full range support with adjacent month cells, 6-row lock, and ISO week numbers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("Adjacent days", style = MaterialTheme.typography.bodySmall)
                    Switch(
                        checked = ui.previousDays && ui.nextDays,
                        onCheckedChange = { enabled ->
                            ui.previousDays = enabled
                            ui.nextDays = enabled
                        },
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("Fixed 6 rows", style = MaterialTheme.typography.bodySmall)
                    Switch(checked = ui.fixedSixRows, onCheckedChange = { ui.fixedSixRows = it })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("Week numbers", style = MaterialTheme.typography.bodySmall)
                    Switch(checked = ui.showWeekNumbers, onCheckedChange = { ui.showWeekNumbers = it })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("RTL", style = MaterialTheme.typography.bodySmall)
                    Switch(checked = ui.rtl, onCheckedChange = { ui.rtl = it })
                }
            }
        }
    }
}

private fun SampleSecondaryCalendar.toSampleCalendarOrNull(): SampleCalendar? = when (this) {
    SampleSecondaryCalendar.None -> null
    SampleSecondaryCalendar.Gregorian -> SampleCalendar.Gregorian
    SampleSecondaryCalendar.Hijri -> SampleCalendar.Hijri
    SampleSecondaryCalendar.SolarHijri -> SampleCalendar.SolarHijri
    SampleSecondaryCalendar.SolarHijriNative -> SampleCalendar.SolarHijriNative
    SampleSecondaryCalendar.Chinese -> SampleCalendar.Chinese
}

private fun SampleCalendar.toSecondaryCalendar(): SampleSecondaryCalendar = when (this) {
    SampleCalendar.Gregorian -> SampleSecondaryCalendar.Gregorian
    SampleCalendar.Hijri -> SampleSecondaryCalendar.Hijri
    SampleCalendar.SolarHijri -> SampleSecondaryCalendar.SolarHijri
    SampleCalendar.SolarHijriNative -> SampleSecondaryCalendar.SolarHijriNative
    SampleCalendar.Chinese -> SampleSecondaryCalendar.Chinese
}
