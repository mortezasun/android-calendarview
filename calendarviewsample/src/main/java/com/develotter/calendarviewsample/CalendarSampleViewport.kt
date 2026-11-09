package com.develotter.calendarviewsample

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.develotter.calendarview.compose.CalendarMonthList
import com.develotter.calendarview.compose.CalendarMonthPager
import com.develotter.calendarview.compose.CalendarScrollAxis

private enum class SampleDisplayMode { Single, VerticalList, HorizontalList, HorizontalPager, VerticalPager }

/**
 * Bounded demo viewport for all native container modes. Selection is owned outside this
 * component. Calendar/anchor changes reset the window; mode changes preserve each native
 * list/pager position separately. No automatic infinite month loading is implied.
 */
@Composable
internal fun CalendarSampleViewport(
    calendar: SampleCalendar,
    anchor: Long,
    monthContent: @Composable (Long) -> Unit,
) {
    var modeName by rememberSaveable { mutableStateOf(SampleDisplayMode.Single.name) }
    val mode = SampleDisplayMode.valueOf(modeName)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SampleDisplayMode.entries.forEach { item ->
                FilterChip(selected = mode == item, onClick = { modeName = item.name }, label = { Text(item.name) })
            }
        }
        key(calendar.name, anchor) {
            val window = remember(calendar, anchor) {
                runCatching { List(25) { moveSampleMonth(calendar, anchor, it - 12) } }
            }
            val months = window.getOrNull()
            if (months == null) {
                Text(
                    window.exceptionOrNull()?.message ?: "Month window exceeds the calendar range",
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                val listState = rememberLazyListState(initialFirstVisibleItemIndex = 12)
                val pagerState = rememberPagerState(initialPage = 12, pageCount = { months.size })
                val activeIndex = when (mode) {
                    SampleDisplayMode.Single -> 12
                    SampleDisplayMode.VerticalList, SampleDisplayMode.HorizontalList -> listState.firstVisibleItemIndex
                    else -> pagerState.settledPage
                }
                Text("Window month ${activeIndex + 1} / ${months.size}. Previous/Today/Next above recenter the window.")
                val viewport = Modifier.fillMaxWidth().height(480.dp)
                when (mode) {
                    SampleDisplayMode.Single -> Box(viewport) {
                        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) { monthContent(months[12]) }
                    }
                    SampleDisplayMode.VerticalList, SampleDisplayMode.HorizontalList -> CalendarMonthList(
                        monthCount = months.size, modifier = viewport, state = listState,
                        axis = if (mode == SampleDisplayMode.VerticalList) CalendarScrollAxis.Vertical else CalendarScrollAxis.Horizontal,
                        monthKey = { months[it] }, horizontalMonthWidth = 320.dp,
                    ) { index ->
                        if (mode == SampleDisplayMode.HorizontalList) {
                            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) { monthContent(months[index]) }
                        } else {
                            monthContent(months[index])
                        }
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
        }
    }
}
