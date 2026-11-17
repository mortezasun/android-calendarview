package com.develotter.calendarviewsample

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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate

private data class SampleFeatureItem(
    val title: String,
    val module: String,
    val description: String,
)

private val SampleLibraryFeatures: List<SampleFeatureItem> = listOf(
    SampleFeatureItem(
        title = "Dynamic Secondary Calendar Labels",
        module = ":library + all 5 engines",
        description = "Overlay any calendar engine (Gregorian, Hijri, SolarHijri, SolarHijriNative, Chinese) as a second label and screen-reader description inside Month, Week, and Agenda views.",
    ),
    SampleFeatureItem(
        title = "Saveable Selection & Range Policy",
        module = ":library",
        description = "Single, Multiple, and Range selections keyed strictly by epoch day, backed by rememberSaveable with configurable min/max CalendarRangePolicy rules.",
    ),
    SampleFeatureItem(
        title = "Scalable Month Containers",
        module = ":library",
        description = "Vertical and horizontal CalendarMonthList, CalendarMonthPager, adaptive CalendarMonthColumns, and calendar-independent CalendarMonthHeader.",
    ),
    SampleFeatureItem(
        title = "Week Grid, Agenda & Markers",
        module = ":library",
        description = "CalendarWeekGrid and CalendarAgenda share the CalendarWeekData / CalendarMonthData contracts, today rings, disabled predicates, and multi-dot CalendarDayMarker support.",
    ),
    SampleFeatureItem(
        title = "Contribution Heatmaps",
        module = ":library + engines",
        description = "CalendarYearHeatmap with WeeksAsColumns or WeeksAsRows orientation, customizable HeatmapStyle geometry, interactive sizing, and intensity palettes.",
    ),
    SampleFeatureItem(
        title = "Adaptive Layout, Foldables & Accessibility",
        module = "Sample + :library",
        description = "Material 3 navigation rail/bar adaptation, physical hinge awareness via WindowInfoTracker, full RTL layout support, and merged semantics.",
    ),
)

/**
 * Landing showcase listing the project's capabilities, live multi-calendar date conversions for
 * today, and direct entry cards for each published calendar module.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SampleHomeScreen(
    ui: CalendarSampleState,
    modifier: Modifier = Modifier,
) {
    val todayEpochDay = remember { LocalDate.now().toEpochDay() }
    val todayConversions = remember(todayEpochDay, ui.chineseStyleName, ui.hijriOffset) {
        sampleAllCalendarConversions(
            epochDay = todayEpochDay,
            chineseStyle = ui.chineseStyle,
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Text(
                        text = "KMP 3.1.0-beta · Compose Multiplatform",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
                Text(
                    text = "Multi-Calendar Compose Toolkit",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Stateless, epoch-day driven calendars for Gregorian, Hijri, Solar Hijri, and Chinese Lunar systems with dynamic cross-calendar secondary labels.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(onClick = { ui.navigateTo(SampleDestination.Calendars) }) {
                        Icon(SampleIcons.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Explore Calendars")
                    }
                    FilledTonalButton(onClick = { ui.navigateTo(SampleDestination.Playground) }) {
                        Icon(SampleIcons.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Open Playground")
                    }
                    OutlinedButton(onClick = { ui.navigateTo(SampleDestination.Heatmaps) }) {
                        Icon(SampleIcons.GridView, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("View Heatmaps")
                    }
                }
            }
        }

        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Today Across All Calendars",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Epoch $todayEpochDay",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                HorizontalDivider()
                todayConversions.forEach { conversion ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = conversion.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = conversion.formattedDate,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        conversion.detail?.let { detail ->
                            Text(
                                text = detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AssistChip(
                        onClick = {
                            ui.openCalendar(SampleCalendar.Gregorian, SampleSecondaryCalendar.Hijri)
                        },
                        label = { Text("Try Gregorian + Hijri") },
                    )
                    AssistChip(
                        onClick = {
                            ui.openCalendar(SampleCalendar.Gregorian, SampleSecondaryCalendar.Chinese)
                        },
                        label = { Text("Try Gregorian + Chinese") },
                    )
                    AssistChip(
                        onClick = {
                            ui.openCalendar(SampleCalendar.SolarHijriNative, SampleSecondaryCalendar.Gregorian)
                        },
                        label = { Text("Try SolarHijri + Gregorian") },
                    )
                }
            }
        }

        Text(
            text = "Calendar Modules",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        SampleCalendarCatalog.forEach { info ->
            val suggestedSecondary = when (info.calendar) {
                SampleCalendar.Gregorian -> SampleSecondaryCalendar.Hijri
                else -> SampleSecondaryCalendar.Gregorian
            }
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = info.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ) {
                            Text(
                                text = info.artifact,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }
                    Text(
                        text = info.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    info.highlights.forEach { bullet ->
                        Text(
                            text = "• $bullet",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(onClick = { ui.openCalendar(info.calendar) }) {
                            Text("Explore ${info.calendar.name}")
                            Spacer(Modifier.width(6.dp))
                            Icon(SampleIcons.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        OutlinedButton(
                            onClick = { ui.openCalendar(info.calendar, suggestedSecondary) },
                        ) {
                            Text("+ ${suggestedSecondary.label}")
                        }
                    }
                }
            }
        }

        Text(
            text = "Library Capabilities",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        SampleLibraryFeatures.forEach { feature ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = feature.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = feature.module,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = feature.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
