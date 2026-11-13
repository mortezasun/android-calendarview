package com.develotter.calendarviewsample

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.develotter.calendarview.compose.CalendarMonthColumns
import com.develotter.calendarview.compose.CalendarMonthList
import com.develotter.calendarview.compose.CalendarMonthPager
import com.develotter.calendarview.compose.CalendarScrollAxis
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first

internal enum class SampleDisplayMode { Single, VerticalList, HorizontalList, HorizontalPager, VerticalPager, AdaptiveColumns }

/**
 * Controlled visible month with an independently saved finite window center.
 * Settled user scrolling reports its month; external requests scroll without feedback.
 * Mode changes start at the controlled month and reset intra-month offsets.
 * The default height follows the host pane; explicit heights remain caller-controlled.
 */
@Composable
internal fun CalendarSampleViewport(
    calendar: SampleCalendar,
    anchor: Long,
    monthCalendar: SampleCalendar = calendar,
    onVisibleMonthChanged: (Long) -> Unit = {},
    showModeSelector: Boolean = true,
    showWindowLabel: Boolean = true,
    forcedMode: SampleDisplayMode? = null,
    viewportHeight: Dp = sampleBoundedViewportHeight(480.dp, LocalSamplePaneHeight.current),
    minMonthWidth: Dp = sampleTargetMonthWidth(showWeekNumbers = false),
    monthContent: @Composable (Long) -> Unit,
) {
    var modeName by rememberSaveable { mutableStateOf(SampleDisplayMode.Single.name) }
    var center by rememberSaveable(calendar.name, monthCalendar.name) { mutableStateOf(anchor) }
    val mode = forcedMode ?: SampleDisplayMode.valueOf(modeName)
    val request = remember(monthCalendar, anchor) { runCatching { moveSampleMonth(monthCalendar, anchor, 0) } }
    val window = remember(monthCalendar, center) { runCatching { sampleMonthWindow(monthCalendar, center) } }
    val desired = request.getOrNull()
    val months = window.getOrNull()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (showModeSelector) {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SampleDisplayMode.entries.forEach { item ->
                    FilterChip(selected = mode == item, onClick = { modeName = item.name }, label = { Text(item.name) })
                }
            }
        }
        when {
            desired == null || months == null -> Text(
                request.exceptionOrNull()?.message ?: window.exceptionOrNull()?.message ?: "Unsupported calendar window",
                color = MaterialTheme.colorScheme.error,
            )
            desired !in months -> {
                LaunchedEffect(monthCalendar, desired) { center = desired }
                Text("Updating month window")
            }
            else -> key(calendar.name, monthCalendar.name, center, mode) {
                SynchronizedMonthViewport(
                    mode = mode,
                    months = months,
                    desired = desired,
                    onVisibleMonthChanged = onVisibleMonthChanged,
                    showWindowLabel = showWindowLabel,
                    viewportHeight = viewportHeight,
                    minMonthWidth = minMonthWidth,
                    monthContent = monthContent,
                )
            }
        }
    }
}

@Composable
private fun SynchronizedMonthViewport(
    mode: SampleDisplayMode,
    months: List<Long>,
    desired: Long,
    onVisibleMonthChanged: (Long) -> Unit,
    showWindowLabel: Boolean,
    viewportHeight: Dp,
    minMonthWidth: Dp,
    monthContent: @Composable (Long) -> Unit,
) {
    val target = months.indexOf(desired)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = target)
    val pagerState = rememberPagerState(initialPage = target, pageCount = { months.size })
    val gridState = rememberLazyGridState(initialFirstVisibleItemIndex = target)
    val isList = mode == SampleDisplayMode.VerticalList || mode == SampleDisplayMode.HorizontalList
    val isAdaptive = mode == SampleDisplayMode.AdaptiveColumns
    var ready by remember { mutableStateOf(false) }
    var appliedRequest by remember { mutableStateOf<Long?>(null) }
    var publishedMonth by remember { mutableStateOf(desired) }
    val currentRequest by rememberUpdatedState(desired)
    val callback by rememberUpdatedState(onVisibleMonthChanged)

    LaunchedEffect(desired) {
        if (appliedRequest == null || desired != publishedMonth) {
            ready = false
            when {
                isList -> listState.scrollToItem(months.indexOf(desired))
                isAdaptive -> gridState.scrollToItem(months.indexOf(desired))
                mode != SampleDisplayMode.Single -> pagerState.scrollToPage(months.indexOf(desired))
            }
            publishedMonth = desired
        }
        appliedRequest = desired
        if (mode != SampleDisplayMode.Single && !isAdaptive) {
            val targetIndex = months.indexOf(desired)
            snapshotFlow {
                if (isList) listState.firstVisibleItemIndex else pagerState.settledPage
            }.first { it == targetIndex }
        }
        ready = true
    }
    LaunchedEffect(mode, listState, pagerState, gridState, months) {
        snapshotFlow {
            val moving = when {
                isList -> listState.isScrollInProgress
                isAdaptive -> gridState.isScrollInProgress
                else -> pagerState.isScrollInProgress
            }
            if (!ready || appliedRequest != currentRequest || moving || mode == SampleDisplayMode.Single) null
            else when {
                isList -> listState.firstVisibleItemIndex
                isAdaptive -> gridState.firstVisibleItemIndex
                else -> pagerState.settledPage
            }
        }.collect { index ->
            if (index != null && index in months.indices) {
                val visible = months[index]
                if (visible != publishedMonth) {
                    publishedMonth = visible
                    callback(visible)
                }
            }
        }
    }

    val active = when {
        mode == SampleDisplayMode.Single -> target
        isList -> listState.firstVisibleItemIndex
        isAdaptive -> gridState.firstVisibleItemIndex
        else -> pagerState.settledPage
    }
    if (showWindowLabel) {
        Text("Window month ${active + 1} / ${months.size}")
    }
    val viewport = Modifier.fillMaxWidth().height(viewportHeight)
    when (mode) {
        SampleDisplayMode.Single -> Box(viewport) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) { monthContent(desired) }
        }
        SampleDisplayMode.AdaptiveColumns -> CalendarMonthColumns(
            monthCount = months.size, modifier = viewport, state = gridState,
            minMonthWidth = minMonthWidth, monthKey = { months[it] },
        ) { index ->
            monthContent(months[index])
        }
        SampleDisplayMode.VerticalList, SampleDisplayMode.HorizontalList -> CalendarMonthList(
            monthCount = months.size, modifier = viewport, state = listState,
            axis = if (mode == SampleDisplayMode.VerticalList) CalendarScrollAxis.Vertical else CalendarScrollAxis.Horizontal,
            monthKey = { months[it] }, horizontalMonthWidth = minMonthWidth,
        ) { index ->
            if (mode == SampleDisplayMode.HorizontalList) {
                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) { monthContent(months[index]) }
            } else monthContent(months[index])
        }
        SampleDisplayMode.HorizontalPager, SampleDisplayMode.VerticalPager -> CalendarMonthPager(
            monthCount = months.size, modifier = viewport, state = pagerState,
            axis = if (mode == SampleDisplayMode.HorizontalPager) CalendarScrollAxis.Horizontal else CalendarScrollAxis.Vertical,
            monthKey = { months[it] },
        ) { index ->
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) { monthContent(months[index]) }
        }
    }
}
