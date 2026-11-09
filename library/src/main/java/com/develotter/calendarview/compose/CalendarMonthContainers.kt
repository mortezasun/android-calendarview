package com.develotter.calendarview.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Scroll axis for native month containers. */
enum class CalendarScrollAxis { Vertical, Horizontal }

/**
 * Lazy calendar-independent month list. The caller maps each index to a calendar month
 * and supplies its composable, normally one of the three calendar entry points.
 * Selection is caller-owned and can be shared across all items.
 *
 * Vertical lists require bounded height; horizontal lists require bounded width and use
 * [horizontalMonthWidth] per item. Do not place a vertical list inside an unbounded vertical
 * scroll container. Keys must be unique and saveable, such as an epoch day or month string.
 * Changing the window identity requires the caller to reset/re-key the list state.
 *
 * [onMonthActive] reports the first visible item, initially and whenever that index changes;
 * it is not a most-visible-item calculation. Programmatic navigation uses [state].
 */
@Composable
fun CalendarMonthList(
    monthCount: Int,
    modifier: Modifier = Modifier,
    axis: CalendarScrollAxis = CalendarScrollAxis.Vertical,
    state: LazyListState = rememberLazyListState(),
    horizontalMonthWidth: Dp = 320.dp,
    monthKey: (Int) -> Any = { it },
    onMonthActive: (Int) -> Unit = {},
    monthContent: @Composable (Int) -> Unit,
) {
    require(monthCount > 0) { "monthCount must be positive" }
    require(horizontalMonthWidth.value.isFinite() && horizontalMonthWidth.value > 0f) {
        "horizontalMonthWidth must be finite and positive"
    }
    val activeIndex by remember(state) { derivedStateOf { state.firstVisibleItemIndex } }
    val callback by rememberUpdatedState(onMonthActive)
    LaunchedEffect(state, activeIndex, monthCount) {
        if (activeIndex in 0 until monthCount) callback(activeIndex)
    }
    when (axis) {
        CalendarScrollAxis.Vertical -> LazyColumn(modifier = modifier, state = state) {
            items(count = monthCount, key = monthKey) { index ->
                Box(Modifier.fillMaxWidth()) { monthContent(index) }
            }
        }
        CalendarScrollAxis.Horizontal -> LazyRow(modifier = modifier, state = state) {
            items(count = monthCount, key = monthKey) { index ->
                Box(Modifier.width(horizontalMonthWidth)) { monthContent(index) }
            }
        }
    }
}

/**
 * Calendar-independent month pager with a caller-accessible native [PagerState].
 * The state pageCount must match [monthCount]. Both axes need a bounded page viewport;
 * month content fills it and can manage its own overflow if needed. Keys must be unique
 * and saveable. Use a new/re-keyed state when changing the identity of the month window.
 *
 * [onMonthActive] reports the settled page, not each intermediate animation position.
 * Use state.animateScrollToPage or state.scrollToPage from a caller coroutine to navigate.
 * The default remembered native pager state supports saved-instance-state restoration.
 * Gesture, restoration and RTL behavior require device verification.
 */
@Composable
fun CalendarMonthPager(
    monthCount: Int,
    modifier: Modifier = Modifier,
    axis: CalendarScrollAxis = CalendarScrollAxis.Horizontal,
    state: PagerState = rememberPagerState(pageCount = { monthCount }),
    monthKey: (Int) -> Any = { it },
    onMonthActive: (Int) -> Unit = {},
    monthContent: @Composable (Int) -> Unit,
) {
    require(monthCount > 0) { "monthCount must be positive" }
    require(state.pageCount == monthCount) { "PagerState.pageCount must equal monthCount" }
    val activeIndex by remember(state) { derivedStateOf { state.settledPage } }
    val callback by rememberUpdatedState(onMonthActive)
    LaunchedEffect(state, activeIndex, monthCount) {
        if (activeIndex in 0 until monthCount) callback(activeIndex)
    }
    when (axis) {
        CalendarScrollAxis.Horizontal -> HorizontalPager(state = state, modifier = modifier, key = monthKey) { index ->
            Box(Modifier.fillMaxSize()) { monthContent(index) }
        }
        CalendarScrollAxis.Vertical -> VerticalPager(state = state, modifier = modifier, key = monthKey) { index ->
            Box(Modifier.fillMaxSize()) { monthContent(index) }
        }
    }
}
