package com.develotter.calendarview.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
 * Vertical day agenda with caller-owned selection and markers.
 *
 * [days] are rendered in the supplied order and need unique epoch identities. The host must give
 * the list a bounded height. [dayContent] receives the cell, selected state and range-endpoint
 * state; the default shows the full localized accessibility label. [todayDescription] is only
 * appended to today's accessible description. Marker descriptions are appended for every day.
 */
@Composable
fun CalendarAgenda(
    days: List<CalendarDayCell>,
    selection: CalendarSelection,
    onDayClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    markers: Map<Long, List<CalendarDayMarker>> = emptyMap(),
    todayDescription: String? = null,
    todayBorderColor: Color? = null,
    dayContent: @Composable (CalendarDayCell, Boolean, Boolean) -> Unit = { day, _, _ ->
        Text(day.accessibilityLabel)
    },
) {
    require(days.isNotEmpty()) { "An agenda requires at least one day" }
    require(days.map { it.epochDay }.toSet().size == days.size) {
        "Agenda days must have unique epoch identities"
    }
    val style = LocalCalendarStyle.current
    val effectiveTodayBorderColor = todayBorderColor
        ?: style.todayBorderColor
        ?: MaterialTheme.colorScheme.tertiary
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(items = days, key = { it.epochDay }) { day ->
            val selected = day.epochDay in selection
            val dayMarkers = markers[day.epochDay].orEmpty()
            val suffix = calendarMarkerDescription(dayMarkers, day.isToday, todayDescription)
            val description = if (suffix == null) {
                day.accessibilityLabel
            } else {
                "${day.accessibilityLabel}, $suffix"
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = description }
                    .selectable(
                        selected = selected,
                        enabled = day.enabled,
                        role = Role.Button,
                        onClick = { onDayClick(day.epochDay) },
                    ),
                color = when {
                    selected -> style.selectedContainerColor ?: MaterialTheme.colorScheme.primaryContainer
                    else -> style.surfaceColor ?: MaterialTheme.colorScheme.surface
                },
                contentColor = when {
                    selected -> style.selectedContentColor ?: MaterialTheme.colorScheme.onPrimaryContainer
                    !day.isCurrentMonth -> style.adjacentContentColor ?: MaterialTheme.colorScheme.onSurfaceVariant
                    else -> style.contentColor ?: MaterialTheme.colorScheme.onSurface
                },
                border = if (day.isToday) BorderStroke(1.5.dp, effectiveTodayBorderColor) else null,
                shape = RoundedCornerShape(8.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(Modifier.weight(1f)) {
                        dayContent(day, selected, selection.isRangeEndpoint(day.epochDay))
                    }
                    if (dayMarkers.isNotEmpty()) {
                        CalendarMarkerRow(dayMarkers)
                    }
                }
            }
        }
    }
}
