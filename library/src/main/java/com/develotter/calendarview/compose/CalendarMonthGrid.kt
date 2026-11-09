package com.develotter.calendarview.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.develotter.calendarview.selection.CalendarSelection

/** Display label plus a calendar-independent absolute-day identity. */
data class CalendarDayCell(
    val epochDay: Long,
    val label: String,
    val accessibilityLabel: String,
    val enabled: Boolean = true,
)

/**
 * Native stateless month renderer; no AndroidView, XML or ViewBinding is used.
 *
 * Supply days in chronological order and weekday labels in the intended first-weekday order.
 * firstDayOffset counts blank cells before day one in that same order. Row placement follows
 * the ambient Compose layout direction, including RTL. The caller owns calendar arithmetic,
 * localization, navigation and selection state. Adjacent-month days are not rendered yet.
 *
 * Day slots receive selected/endpoint flags; click handling and accessibility stay on the
 * outer cell. Custom content should not install a competing click handler.
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
) {
    require(weekdayLabels.size == 7) { "Exactly seven weekday labels are required" }
    require(firstDayOffset in 0..6) { "firstDayOffset must be in 0..6" }
    require(days.size in 1..31) { "A month must contain 1..31 days" }
    require(days.map { it.epochDay }.distinct().size == days.size) { "Day identities must be unique" }

    Column(modifier) {
        header(title)
        Row(Modifier.fillMaxWidth()) {
            weekdayLabels.forEach { label ->
                Box(Modifier.weight(1f).padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                    weekdayContent(label)
                }
            }
        }
        val rowCount = (firstDayOffset + days.size + 6) / 7
        repeat(rowCount) { row ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { column ->
                    val day = days.getOrNull(row * 7 + column - firstDayOffset)
                    val cellModifier = Modifier.weight(1f).heightIn(min = 48.dp)
                    if (day == null) {
                        Box(cellModifier)
                    } else {
                        val selected = day.epochDay in selection
                        Surface(
                            modifier = cellModifier
                                .semantics { contentDescription = day.accessibilityLabel }
                                .selectable(
                                    selected = selected,
                                    enabled = day.enabled,
                                    role = Role.Button,
                                    onClick = { onDayClick(day.epochDay) },
                                ),
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface,
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
