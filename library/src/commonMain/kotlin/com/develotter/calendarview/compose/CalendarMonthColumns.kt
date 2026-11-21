package com.develotter.calendarview.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Number of whole month columns that fit into [availableWidth].
 * At least one column is returned; invalid dimensions fail instead of silently collapsing.
 */
fun calendarMonthColumns(availableWidth: Dp, minMonthWidth: Dp): Int =
    calendarMonthColumns(availableWidth, minMonthWidth, 0.dp)

/**
 * Number of whole month columns that fit into [availableWidth] with [horizontalSpacing]
 * between columns, so every month still reaches [minMonthWidth]. At least one column is
 * returned; invalid dimensions fail instead of silently collapsing.
 */
fun calendarMonthColumns(
    availableWidth: Dp,
    minMonthWidth: Dp,
    horizontalSpacing: Dp,
): Int = calendarMonthColumns(availableWidth, minMonthWidth, horizontalSpacing, Int.MAX_VALUE)

/**
 * Number of whole month columns that fit into [availableWidth], capped at [maxColumns].
 *
 * The cap never adds columns beyond what fits, and the months that no longer have a column wrap
 * into further rows, so a fold-aware host can keep a readable column count on wide screens
 * instead of stretching a calendar year across the full width. `Int.MAX_VALUE` (the default of
 * [CalendarMonthColumns]) keeps the unbounded behaviour. Invalid dimensions and a non-positive
 * cap fail instead of silently collapsing.
 */
fun calendarMonthColumns(
    availableWidth: Dp,
    minMonthWidth: Dp,
    horizontalSpacing: Dp,
    maxColumns: Int,
): Int {
    require(availableWidth.value.isFinite() && availableWidth.value > 0f) {
        "availableWidth must be finite and positive"
    }
    require(minMonthWidth.value.isFinite() && minMonthWidth.value > 0f) {
        "minMonthWidth must be finite and positive"
    }
    require(horizontalSpacing.value.isFinite() && horizontalSpacing.value >= 0f) {
        "horizontalSpacing must be finite and non-negative"
    }
    require(maxColumns >= 1) { "maxColumns must be positive" }
    val fittingColumns = maxOf(
        1,
        ((availableWidth.value + horizontalSpacing.value) /
            (minMonthWidth.value + horizontalSpacing.value)).toInt(),
    )
    return minOf(fittingColumns, maxColumns)
}

/**
 * Number of rows that [monthCount] months need when a grid has [columns] columns.
 * Months wrap at every column edge, independent of how many days a month has. The grid wraps by
 * construction; this pure helper keeps the rule testable in `commonTest`.
 */
internal fun calendarMonthRows(monthCount: Int, columns: Int): Int {
    require(monthCount > 0) { "monthCount must be positive" }
    require(columns > 0) { "columns must be positive" }
    return (monthCount + columns - 1) / columns
}

/**
 * Adaptive multi-month grid with a caller-accessible native [LazyGridState].
 *
 * The column count is derived from the available width and [minMonthWidth], so tablets and
 * resized/foldable windows show multiple months without a separate layout API. The host must
 * give the grid a bounded height. [onMonthActive] reports the first visible item, initially and
 * whenever that index changes. Use a new/re-keyed state when changing the month-window identity.
 *
 * [maxColumns] caps the derived column count: months beyond that many columns wrap into further
 * rows instead of widening the grid. `Int.MAX_VALUE` keeps the unbounded behaviour, so existing
 * screens and `LazyGridState` positions are unchanged.
 */
@Composable
fun CalendarMonthColumns(
    monthCount: Int,
    modifier: Modifier = Modifier,
    minMonthWidth: Dp = 320.dp,
    state: LazyGridState = rememberLazyGridState(),
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    monthKey: (Int) -> Any = { it },
    onMonthActive: (Int) -> Unit = {},
    maxColumns: Int = Int.MAX_VALUE,
    monthContent: @Composable (Int) -> Unit,
) {
    require(monthCount > 0) { "monthCount must be positive" }
    require(minMonthWidth.value.isFinite() && minMonthWidth.value > 0f) {
        "minMonthWidth must be finite and positive"
    }
    require(horizontalSpacing.value.isFinite() && horizontalSpacing.value >= 0f) {
        "horizontalSpacing must be finite and non-negative"
    }
    require(verticalSpacing.value.isFinite() && verticalSpacing.value >= 0f) {
        "verticalSpacing must be finite and non-negative"
    }
    require(maxColumns >= 1) { "maxColumns must be positive" }
    val activeIndex by remember(state) { derivedStateOf { state.firstVisibleItemIndex } }
    val callback by rememberUpdatedState(onMonthActive)
    LaunchedEffect(state, activeIndex, monthCount) {
        if (activeIndex in 0 until monthCount) callback(activeIndex)
    }
    BoxWithConstraints(modifier) {
        val columns = calendarMonthColumns(maxWidth, minMonthWidth, horizontalSpacing, maxColumns)
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            state = state,
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        ) {
            items(count = monthCount, key = monthKey) { index ->
                Box(Modifier.fillMaxWidth()) { monthContent(index) }
            }
        }
    }
}
