package com.develotter.calendarview.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics

/**
 * Optional event/status marker for one absolute day.
 *
 * Markers are keyed by epoch day in the calendar views. [contentDescription] should be caller
 * localized and is appended to the day's accessibility description; the colored dot itself is
 * decorative. At most four markers are drawn in a cell, while all descriptions remain available
 * to accessibility services.
 */
@Immutable
data class CalendarDayMarker(
    val color: Color,
    val contentDescription: String? = null,
)

/** Appends marker and today descriptions to a day's accessibility label. */
internal fun calendarMarkerDescription(
    markers: List<CalendarDayMarker>,
    isToday: Boolean,
    todayDescription: String?,
): String? = buildList {
    markers.mapNotNullTo(this) { it.contentDescription }
    if (isToday && todayDescription != null) add(todayDescription)
}.joinToString(", ").ifBlank { null }

/** Draws up to four decorative marker dots; the caller supplies the accessible description. */
@Composable
internal fun CalendarMarkerRow(markers: List<CalendarDayMarker>) {
    if (markers.isEmpty()) return
    val style = LocalCalendarStyle.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(style.markerSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        markers.take(4).forEach { marker ->
            Box(
                Modifier
                    .size(style.markerSize)
                    .clip(CircleShape)
                    .background(marker.color)
                    .clearAndSetSemantics {},
            )
        }
    }
}
