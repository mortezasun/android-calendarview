package com.develotter.calendarview.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.develotter.calendarview.selection.CalendarSelection

private val WeekNumberWidth = 40.dp

/**
 * Native stateless calendar grid with optional independently visible adjacent-month days.
 * Supply chronological current-month cells, ordered weekday labels and a matching offset.
 * Adjacent formatting/availability comes from the caller's calendar-specific provider.
 * All dates share selection and click handling; clicks do not automatically navigate.
 * Slots receive cells with isCurrentMonth plus selection/endpoint flags. Do not install
 * competing click handlers in slots. Layout follows ambient direction, including RTL.
 * Today indicator uses [todayBorderColor] (defaults to tertiary) so it never clashes
 * with the primary selection highlight. Pass a localized [todayDescription] to append it to
 * the accessibility description of today's cell; the default null keeps the date-only label.
 * The header is exposed as a heading, the week container owns one collection description and
 * every day and optional week-number cell carries its collection position.
 *
 * [markers] are keyed by epoch day and render up to four dots below the day content. Their
 * localized descriptions are appended to the day's accessibility label. [weekNumber] adds a
 * leading, caller-formatted week column when non-null; it receives the first day of the week.
 * [LocalCalendarStyle] supplies optional colors; null colors fall back to MaterialTheme.
 */
@Composable
fun CalendarMonthGrid(
    title: String,
    weekdayLabels: List<String>,
    firstDayOffset: Int,
    days: List<CalendarDayCell>,
    selection: CalendarSelection,
    onDayClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    header: @Composable (String) -> Unit = { Text(it, style = MaterialTheme.typography.titleMedium) },
    weekdayContent: @Composable (String) -> Unit = { Text(it) },
    dayContent: @Composable (CalendarDayCell, Boolean, Boolean) -> Unit = { day, _, _ -> Text(day.label) },
    showPreviousMonthDays: Boolean = false,
    showNextMonthDays: Boolean = false,
    adjacentDayProvider: ((Long) -> CalendarDayCell)? = null,
    showHeader: Boolean = true,
    showWeekdayLabels: Boolean = true,
    fixedSixRows: Boolean = false,
    todayBorderColor: Color? = null,
    todayDescription: String? = null,
    markers: Map<Long, List<CalendarDayMarker>> = emptyMap(),
    weekNumber: ((Long) -> String?)? = null,
    weekNumberContent: @Composable (String) -> Unit = { Text(it) },
) {
    require(weekdayLabels.size == 7) { "Exactly seven weekday labels are required" }
    val cells = buildCalendarMonthCells(
        days = days,
        firstDayOffset = firstDayOffset,
        showPreviousMonthDays = showPreviousMonthDays,
        showNextMonthDays = showNextMonthDays,
        fixedSixRows = fixedSixRows,
        adjacentDayProvider = adjacentDayProvider,
    )
    val style = LocalCalendarStyle.current
    val effectiveTodayBorderColor = todayBorderColor
        ?: style.todayBorderColor
        ?: MaterialTheme.colorScheme.tertiary
    val showWeekNumbers = weekNumber != null
    Column(modifier) {
        if (showHeader) {
            Box(Modifier.semantics(mergeDescendants = true) { heading() }) {
                header(title)
            }
        }
        if (showWeekdayLabels) {
            Row(Modifier.fillMaxWidth()) {
                if (showWeekNumbers) {
                    Spacer(Modifier.width(WeekNumberWidth))
                }
                weekdayLabels.forEach { label ->
                    Box(Modifier.weight(1f).padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        weekdayContent(label)
                    }
                }
            }
        }
        val weeks = cells.chunked(7)
        val columnCount = if (showWeekNumbers) 8 else 7
        Column(
            Modifier.fillMaxWidth().semantics {
                collectionInfo = CollectionInfo(rowCount = weeks.size, columnCount = columnCount)
            },
        ) {
            weeks.forEachIndexed { rowIndex, week ->
                val weekNumberLabel = if (showWeekNumbers) {
                    week.firstNotNullOfOrNull { it }?.let { day -> weekNumber.invoke(day.epochDay) }
                } else {
                    null
                }
                Row(Modifier.fillMaxWidth()) {
                    if (showWeekNumbers) {
                        Box(
                            Modifier.width(WeekNumberWidth).heightIn(min = 48.dp).semantics {
                                collectionItemInfo = CollectionItemInfo(
                                    rowIndex = rowIndex,
                                    rowSpan = 1,
                                    columnIndex = 0,
                                    columnSpan = 1,
                                )
                            },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (weekNumberLabel != null) weekNumberContent(weekNumberLabel)
                        }
                    }
                    week.forEachIndexed { columnIndex, day ->
                        val cellModifier = Modifier.weight(1f).heightIn(min = 48.dp)
                        if (day == null) {
                            Box(cellModifier)
                        } else {
                            val selected = day.epochDay in selection
                            val dayMarkers = markers[day.epochDay].orEmpty()
                            val suffix = calendarMarkerDescription(dayMarkers, day.isToday, todayDescription)
                            val description = if (suffix == null) {
                                day.accessibilityLabel
                            } else {
                                "${day.accessibilityLabel}, $suffix"
                            }
                            Surface(
                                modifier = cellModifier
                                    .semantics {
                                        contentDescription = description
                                        collectionItemInfo = CollectionItemInfo(
                                            rowIndex = rowIndex,
                                            rowSpan = 1,
                                            columnIndex = columnIndex + if (showWeekNumbers) 1 else 0,
                                            columnSpan = 1,
                                        )
                                    }
                                    .selectable(
                                        selected = selected, enabled = day.enabled, role = Role.Button,
                                        onClick = { onDayClick(day.epochDay) },
                                    ),
                                color = when {
                                    selected -> style.selectedContainerColor
                                        ?: MaterialTheme.colorScheme.primaryContainer
                                    else -> style.surfaceColor ?: MaterialTheme.colorScheme.surface
                                },
                                contentColor = when {
                                    selected -> style.selectedContentColor
                                        ?: MaterialTheme.colorScheme.onPrimaryContainer
                                    !day.isCurrentMonth -> style.adjacentContentColor
                                        ?: MaterialTheme.colorScheme.onSurfaceVariant
                                    else -> style.contentColor ?: MaterialTheme.colorScheme.onSurface
                                },
                                border = if (day.isToday) {
                                    BorderStroke(1.5.dp, effectiveTodayBorderColor)
                                } else {
                                    null
                                },
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Column(
                                    Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    dayContent(day, selected, selection.isRangeEndpoint(day.epochDay))
                                    if (dayMarkers.isNotEmpty()) {
                                        Spacer(Modifier.height(2.dp))
                                        CalendarMarkerRow(dayMarkers)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
