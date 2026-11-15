package com.develotter.calendarview.compose

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Optional visual overrides for the shared calendar views.
 *
 * A null color keeps the MaterialTheme default, so existing callers need no style. Provide an
 * instance through [LocalCalendarStyle] to theme month, week and agenda cells without copying the
 * composables. [selectedContainerColor] / [selectedContentColor] apply to selected cells;
 * [surfaceColor] / [contentColor] apply to ordinary cells and [adjacentContentColor] to visible
 * previous/next-month cells.
 */
@Immutable
data class CalendarStyle(
    val selectedContainerColor: Color? = null,
    val selectedContentColor: Color? = null,
    val surfaceColor: Color? = null,
    val contentColor: Color? = null,
    val adjacentContentColor: Color? = null,
    val todayBorderColor: Color? = null,
    val markerSize: Dp = 6.dp,
    val markerSpacing: Dp = 3.dp,
) {
    init {
        require(markerSize.value.isFinite() && markerSize.value > 0f) {
            "markerSize must be finite and positive"
        }
        require(markerSpacing.value.isFinite() && markerSpacing.value >= 0f) {
            "markerSpacing must be finite and non-negative"
        }
    }
}

/** Ambient [CalendarStyle] used by the month, week and agenda views. */
val LocalCalendarStyle = staticCompositionLocalOf { CalendarStyle() }
