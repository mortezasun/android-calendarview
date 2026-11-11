package com.develotter.calendarview.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
 * The header is exposed as a heading and every week/cell carries collection semantics so
 * screen readers can announce the grid position.
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
    todayBorderColor: Color = MaterialTheme.colorScheme.tertiary,
    todayDescription: String? = null,
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
    Column(modifier) {
        if (showHeader) {
            Box(Modifier.semantics(mergeDescendants = true) { heading() }) {
                header(title)
            }
        }
        if (showWeekdayLabels) {
            Row(Modifier.fillMaxWidth()) {
                weekdayLabels.forEach { label ->
                    Box(Modifier.weight(1f).padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        weekdayContent(label)
                    }
                }
            }
        }
        val weeks = cells.chunked(7)
        weeks.forEachIndexed { rowIndex, week ->
            Row(
                Modifier.fillMaxWidth().semantics {
                    collectionInfo = CollectionInfo(rowCount = weeks.size, columnCount = 7)
                },
            ) {
                week.forEachIndexed { columnIndex, day ->
                    val cellModifier = Modifier.weight(1f).heightIn(min = 48.dp)
                    if (day == null) {
                        Box(cellModifier)
                    } else {
                        val selected = day.epochDay in selection
                        val description = if (day.isToday && todayDescription != null) {
                            "${day.accessibilityLabel}, $todayDescription"
                        } else {
                            day.accessibilityLabel
                        }
                        Surface(
                            modifier = cellModifier
                                .semantics {
                                    contentDescription = description
                                    collectionItemInfo = CollectionItemInfo(
                                        rowIndex = rowIndex,
                                        rowSpan = 1,
                                        columnIndex = columnIndex,
                                        columnSpan = 1,
                                    )
                                }
                                .selectable(
                                    selected = selected, enabled = day.enabled, role = Role.Button,
                                    onClick = { onDayClick(day.epochDay) },
                                ),
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            contentColor = when {
                                selected -> MaterialTheme.colorScheme.onPrimaryContainer
                                !day.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            border = if (day.isToday) BorderStroke(1.5.dp, todayBorderColor) else null,
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Box(Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                                dayContent(day, selected, selection.isRangeEndpoint(day.epochDay))
                            }
                        }
                    }
                }
            }
        }
    }
}
