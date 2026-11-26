package com.develotter.calendarview.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.develotter.calendarview.selection.isoWeekday

/**
 * Optional per-day look, for example for weekends or caller-owned holidays.
 *
 * The library bundles no holiday data: the caller decides which days get an appearance. A null
 * color keeps the ambient one. Selected days keep the selection colors, so an appearance never
 * hides the selection.
 */
@Immutable
data class CalendarDayAppearance(
    val containerColor: Color? = null,
    val contentColor: Color? = null,
    val bold: Boolean = false,
)

/**
 * Builds a `dayContent` lambda that styles each day through [appearanceFor].
 *
 * Pass the result as the `dayContent` argument of the month grid and engine month composables.
 * [appearanceFor] returns `null` for days that keep the default look. It runs for every visible
 * cell, so keep it cheap and free of allocation where possible.
 */
fun calendarDayContent(
    appearanceFor: (CalendarDayCell) -> CalendarDayAppearance?,
): @Composable (CalendarDayCell, Boolean, Boolean) -> Unit = { day, selected, _ ->
    val appearance = if (selected) null else appearanceFor(day)
    val container = appearance?.containerColor
    Box(
        modifier = if (container != null) {
            Modifier.clip(CircleShape).background(container).padding(4.dp)
        } else {
            Modifier
        },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = day.label,
            color = appearance?.contentColor ?: Color.Unspecified,
            fontWeight = if (appearance?.bold == true) FontWeight.Bold else null,
        )
    }
}

/**
 * [appearanceFor] helper that applies [appearance] to days whose ISO weekday (1 = Monday ..
 * 7 = Sunday) is in [weekendDays], for use with [calendarDayContent].
 *
 * The weekend is a caller decision because it differs by region; the default is Saturday and
 * Sunday. Weekdays follow the epoch-day timeline, so every calendar engine shares the result.
 */
fun calendarWeekendAppearance(
    appearance: CalendarDayAppearance,
    weekendDays: Set<Int> = setOf(6, 7),
): (CalendarDayCell) -> CalendarDayAppearance? {
    require(weekendDays.all { it in 1..7 }) { "weekendDays must contain ISO weekdays 1..7" }
    return { day -> if (isoWeekday(day.epochDay) in weekendDays) appearance else null }
}
