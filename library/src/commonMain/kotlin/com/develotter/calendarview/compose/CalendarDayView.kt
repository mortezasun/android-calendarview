package com.develotter.calendarview.compose

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val MinutesPerHour = 60
private const val MinutesPerDay = 24 * MinutesPerHour

/**
 * One timed entry of a [CalendarDayView].
 *
 * Times are minutes since midnight of the displayed day, so the view never depends on a time zone
 * or a calendar system; the caller maps its own instants to minutes. [id] identifies the event in
 * click callbacks. A null [color] uses the theme's primary container. [contentDescription] should
 * be caller localized and defaults to [title] for accessibility services.
 */
@Immutable
data class CalendarDayEvent(
    val id: String,
    val startMinute: Int,
    val endMinute: Int,
    val title: String,
    val color: Color? = null,
    val contentDescription: String? = null,
) {
    init {
        require(startMinute in 0 until MinutesPerDay) { "startMinute must be in 0..1439" }
        require(endMinute in (startMinute + 1)..MinutesPerDay) {
            "endMinute must be after startMinute and at most 1440"
        }
    }
}

/**
 * An event with its column inside an overlap group: [column] is zero-based and [columnCount] is
 * the number of columns its group needs, so the event spans `1 / columnCount` of the width.
 */
@Immutable
data class PositionedDayEvent(
    val event: CalendarDayEvent,
    val column: Int,
    val columnCount: Int,
)

/**
 * Assigns side-by-side columns to overlapping events.
 *
 * Events that touch (one ends exactly when the next starts) do not overlap. Overlap is
 * transitive inside a group, and every event of a group shares the same [columnCount]. Events are
 * returned ordered by start minute, longer events first on ties; the input order is irrelevant.
 */
fun layoutDayEvents(events: List<CalendarDayEvent>): List<PositionedDayEvent> {
    val sorted = events.sortedWith(
        compareBy<CalendarDayEvent> { it.startMinute }.thenByDescending { it.endMinute },
    )
    val result = ArrayList<PositionedDayEvent>(sorted.size)
    var group = ArrayList<Pair<CalendarDayEvent, Int>>()
    val columnEnds = ArrayList<Int>()
    var groupEnd = 0

    fun flush() {
        val count = columnEnds.size
        group.forEach { (event, column) -> result += PositionedDayEvent(event, column, count) }
        group = ArrayList()
        columnEnds.clear()
    }

    for (event in sorted) {
        if (group.isNotEmpty() && event.startMinute >= groupEnd) flush()
        val free = columnEnds.indexOfFirst { it <= event.startMinute }
        val column = if (free >= 0) free else columnEnds.size
        if (free >= 0) columnEnds[free] = event.endMinute else columnEnds += event.endMinute
        group += event to column
        groupEnd = if (group.size == 1) event.endMinute else maxOf(groupEnd, event.endMinute)
    }
    if (group.isNotEmpty()) flush()
    return result
}

/**
 * Stateless day timeline with hour slots, a calendar-independent sibling of the month, week and
 * agenda views.
 *
 * The visible window runs from [firstHour] to [lastHour] (0..24, exclusive end); events outside it
 * are skipped and events crossing its edge are clipped. Overlapping events share the width through
 * [layoutDayEvents]. [hourLabel] supplies the localized label of each hour because the library
 * ships no UI strings. The caller owns scrolling through [scrollState], reacts to taps in
 * [onEventClick] (events are not clickable when it is `null`) and may draw a "now" line by giving
 * [nowMinute] in minutes since midnight. [eventContent] replaces the default title text.
 */
@Composable
fun CalendarDayView(
    events: List<CalendarDayEvent>,
    hourLabel: (Int) -> String,
    modifier: Modifier = Modifier,
    firstHour: Int = 0,
    lastHour: Int = 24,
    hourHeight: Dp = 56.dp,
    labelWidth: Dp = 56.dp,
    nowMinute: Int? = null,
    nowIndicatorColor: Color? = null,
    scrollState: ScrollState = rememberScrollState(),
    onEventClick: ((CalendarDayEvent) -> Unit)? = null,
    eventContent: @Composable (CalendarDayEvent) -> Unit = { Text(it.title, style = MaterialTheme.typography.labelMedium) },
) {
    require(firstHour in 0 until 24 && lastHour in (firstHour + 1)..24) {
        "hour window must satisfy 0 <= firstHour < lastHour <= 24"
    }
    require(hourHeight.value.isFinite() && hourHeight.value > 0f) { "hourHeight must be finite and positive" }
    val windowStart = firstHour * MinutesPerHour
    val windowEnd = lastHour * MinutesPerHour
    val positioned = remember(events, windowStart, windowEnd) {
        layoutDayEvents(events.filter { it.endMinute > windowStart && it.startMinute < windowEnd })
    }
    val minuteHeight = hourHeight / MinutesPerHour
    val totalHeight = hourHeight * (lastHour - firstHour)
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    val nowColor = nowIndicatorColor ?: MaterialTheme.colorScheme.error

    Row(modifier.verticalScroll(scrollState)) {
        Box(Modifier.width(labelWidth).height(totalHeight)) {
            for (hour in firstHour until lastHour) {
                Text(
                    text = hourLabel(hour),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .offset(y = hourHeight * (hour - firstHour))
                        .padding(end = 4.dp),
                )
            }
        }
        BoxWithConstraints(Modifier.weight(1f).height(totalHeight)) {
            for (hour in 0 until lastHour - firstHour) {
                HorizontalDivider(Modifier.offset(y = hourHeight * hour).fillMaxWidth(), color = lineColor)
            }
            positioned.forEach { item ->
                val event = item.event
                val top = minuteHeight * (maxOf(event.startMinute, windowStart) - windowStart)
                val height = (minuteHeight * (minOf(event.endMinute, windowEnd) - maxOf(event.startMinute, windowStart)))
                    .coerceAtLeast(20.dp)
                val slot = maxWidth / item.columnCount
                val container = event.color ?: MaterialTheme.colorScheme.primaryContainer
                val clickModifier = if (onEventClick != null) {
                    Modifier.clickable(role = Role.Button) { onEventClick(event) }
                } else {
                    Modifier
                }
                Box(
                    Modifier
                        .offset(x = slot * item.column, y = top)
                        .size(width = slot, height = height)
                        .padding(1.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(container)
                        .then(clickModifier)
                        .semantics(mergeDescendants = true) {
                            contentDescription = event.contentDescription ?: event.title
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    contentAlignment = Alignment.TopStart,
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides contentColorFor(container),
                    ) { eventContent(event) }
                }
            }
            if (nowMinute != null && nowMinute in windowStart until windowEnd) {
                Box(
                    Modifier
                        .offset(y = minuteHeight * (nowMinute - windowStart))
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(nowColor),
                )
            }
        }
    }
}
