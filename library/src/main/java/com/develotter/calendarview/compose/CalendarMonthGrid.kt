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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
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
 * with the primary selection highlight.
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
            header(title)
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
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    val cellModifier = Modifier.weight(1f).heightIn(min = 48.dp)
                    if (day == null) {
                        Box(cellModifier)
                    } else {
                        val selected = day.epochDay in selection
                        Surface(
                            modifier = cellModifier
                                .semantics { contentDescription = day.accessibilityLabel }
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
