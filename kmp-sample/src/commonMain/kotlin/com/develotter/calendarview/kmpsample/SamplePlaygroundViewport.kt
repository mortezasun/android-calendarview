package com.develotter.calendarview.kmpsample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.develotter.calendarview.calendars.chineseCompose.ChineseCalendarMonth
import com.develotter.calendarview.calendars.chineseNative.ChineseMonth
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.calendars.gregorian.compose.GregorianCalendarMonth
import com.develotter.calendarview.calendars.hijri.HijriDateProvider
import com.develotter.calendarview.calendars.hijri.OffsetHijriDateProvider
import com.develotter.calendarview.calendars.hijri.compose.HijriCalendarMonth
import com.develotter.calendarview.calendars.solarHijri.compose.SolarHijriCalendarMonth
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.compose.SolarHijriNativeCalendarMonth
import com.develotter.calendarview.compose.CalendarDayMarker
import com.develotter.calendarview.compose.CalendarMonthColumns
import com.develotter.calendarview.compose.CalendarMonthHeader
import com.develotter.calendarview.compose.CalendarMonthList
import com.develotter.calendarview.compose.CalendarMonthNavigationState
import com.develotter.calendarview.compose.CalendarMonthPager
import com.develotter.calendarview.compose.CalendarScrollAxis
import com.develotter.calendarview.compose.CalendarStyle
import com.develotter.calendarview.compose.LocalCalendarStyle
import com.develotter.calendarview.compose.rememberCalendarSelectionState
import com.develotter.calendarview.locale.CalendarLocale
import com.develotter.calendarview.selection.CalendarSelection

/** Number of months the list, pager and grid containers expose around the current month. */
private const val SampleMonthWindow = 25

/** Index inside [SampleMonthWindow] that renders the current month. */
private const val SampleMonthCenter = SampleMonthWindow / 2

private val SampleMarkerColor = Color(0xFF1F6FEB)

/**
 * Renders the selected layout for the Playground page.
 *
 * The month grid needs a [CalendarSelection] to highlight days, so this viewport mirrors the host
 * selection locally and forwards every tap through [onDayClick]; the host stays the owner of the
 * selection, the range policy and the [isSelected] predicate it uses for its own inspection.
 */
@Composable
internal fun SamplePlaygroundViewport(
    options: PlaygroundOptions,
    onDayClick: (Long) -> Unit,
    isSelected: (Long) -> Boolean,
    todayEpochDay: Long,
) {
    val hijriProvider = remember { OffsetHijriDateProvider(0) }
    val chineseStyle = ChineseTextStyle.English
    val selectionState = rememberCalendarSelectionState(options.selectionMode)
    val selection = selectionState.selection
    val onDayTap: (Long) -> Unit = { epochDay ->
        selectionState.onDayClick(epochDay)
        onDayClick(epochDay)
    }
    val monthAnchors = remember(options.calendar, todayEpochDay, hijriProvider) {
        (-SampleMonthCenter..SampleMonthCenter).map { offset ->
            sampleShiftMonth(options.calendar, todayEpochDay, offset, hijriProvider)
        }
    }

    val customStyle = options.customStyle
    val selectedContainer = MaterialTheme.colorScheme.tertiaryContainer
    val selectedContent = MaterialTheme.colorScheme.onTertiaryContainer
    val todayBorder = MaterialTheme.colorScheme.primary
    val calendarStyle = remember(customStyle, selectedContainer, selectedContent, todayBorder) {
        if (customStyle) {
            CalendarStyle(
                selectedContainerColor = selectedContainer,
                selectedContentColor = selectedContent,
                todayBorderColor = todayBorder,
                markerSize = 10.dp,
                markerSpacing = 4.dp,
            )
        } else {
            CalendarStyle()
        }
    }

    CompositionLocalProvider(LocalCalendarStyle provides calendarStyle) {
        when (options.layout) {
            DemoMonthLayout.Single -> SampleSingleMonth(
                options = options,
                monthAnchor = monthAnchors.getOrNull(SampleMonthCenter),
                todayEpochDay = todayEpochDay,
                selection = selection,
                onDayTap = onDayTap,
                hijriProvider = hijriProvider,
                chineseStyle = chineseStyle,
            )

            DemoMonthLayout.VerticalList, DemoMonthLayout.HorizontalList -> CalendarMonthList(
                monthCount = SampleMonthWindow,
                modifier = Modifier.fillMaxSize(),
                axis = if (options.layout == DemoMonthLayout.VerticalList) {
                    CalendarScrollAxis.Vertical
                } else {
                    CalendarScrollAxis.Horizontal
                },
                state = rememberLazyListState(initialFirstVisibleItemIndex = SampleMonthCenter),
                horizontalMonthWidth = 320.dp,
                monthKey = { index -> monthAnchors[index] ?: "unsupported-$index" },
            ) { index ->
                SamplePlaygroundMonth(
                    calendar = options.calendar,
                    monthAnchor = monthAnchors[index],
                    todayEpochDay = todayEpochDay,
                    selection = selection,
                    onDayTap = onDayTap,
                    showMarkers = options.showMarkers,
                    hijriProvider = hijriProvider,
                    chineseStyle = chineseStyle,
                    locale = options.locale,
                )
            }

            DemoMonthLayout.HorizontalPager, DemoMonthLayout.VerticalPager -> CalendarMonthPager(
                monthCount = SampleMonthWindow,
                modifier = Modifier.fillMaxSize(),
                axis = if (options.layout == DemoMonthLayout.HorizontalPager) {
                    CalendarScrollAxis.Horizontal
                } else {
                    CalendarScrollAxis.Vertical
                },
                state = rememberPagerState(
                    initialPage = SampleMonthCenter,
                    pageCount = { SampleMonthWindow },
                ),
                monthKey = { index -> monthAnchors[index] ?: "unsupported-$index" },
            ) { index ->
                SamplePlaygroundMonth(
                    calendar = options.calendar,
                    monthAnchor = monthAnchors[index],
                    todayEpochDay = todayEpochDay,
                    selection = selection,
                    onDayTap = onDayTap,
                    showMarkers = options.showMarkers,
                    hijriProvider = hijriProvider,
                    chineseStyle = chineseStyle,
                    locale = options.locale,
                )
            }

            DemoMonthLayout.AdaptiveColumns -> CalendarMonthColumns(
                monthCount = SampleMonthWindow,
                modifier = Modifier.fillMaxSize(),
                minMonthWidth = 320.dp,
                state = rememberLazyGridState(initialFirstVisibleItemIndex = SampleMonthCenter),
                monthKey = { index -> monthAnchors[index] ?: "unsupported-$index" },
            ) { index ->
                SamplePlaygroundMonth(
                    calendar = options.calendar,
                    monthAnchor = monthAnchors[index],
                    todayEpochDay = todayEpochDay,
                    selection = selection,
                    onDayTap = onDayTap,
                    showMarkers = options.showMarkers,
                    hijriProvider = hijriProvider,
                    chineseStyle = chineseStyle,
                    locale = options.locale,
                )
            }
        }
    }
}

@Composable
private fun SampleSingleMonth(
    options: PlaygroundOptions,
    monthAnchor: Long?,
    todayEpochDay: Long,
    selection: CalendarSelection,
    onDayTap: (Long) -> Unit,
    hijriProvider: HijriDateProvider,
    chineseStyle: ChineseTextStyle,
) {
    val monthOffset = remember { mutableIntStateOf(0) }
    val navigation = remember(monthOffset) {
        object : CalendarMonthNavigationState {
            override val canGoPrevious: Boolean get() = true
            override val canGoNext: Boolean get() = true
            override val canGoToday: Boolean get() = true
            override fun previous() { monthOffset.intValue -= 1 }
            override fun next() { monthOffset.intValue += 1 }
            override fun goToToday() { monthOffset.intValue = 0 }
        }
    }
    val title = sampleMonthTitle(
        calendar = options.calendar,
        anchorEpochDay = monthAnchor ?: todayEpochDay,
        monthOffset = monthOffset.intValue,
        todayEpochDay = todayEpochDay,
        hijriDateProvider = hijriProvider,
        chineseStyle = chineseStyle,
        locale = options.locale,
    ) ?: "Unsupported month"

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CalendarMonthHeader(
            title = title,
            state = navigation,
            previousMonthContentDescription = "Previous month",
            nextMonthContentDescription = "Next month",
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            SamplePlaygroundMonth(
                calendar = options.calendar,
                monthAnchor = sampleShiftMonth(
                    calendar = options.calendar,
                    anchorEpochDay = monthAnchor ?: todayEpochDay,
                    monthOffset = monthOffset.intValue,
                    hijriDateProvider = hijriProvider,
                ),
                todayEpochDay = todayEpochDay,
                selection = selection,
                onDayTap = onDayTap,
                showMarkers = options.showMarkers,
                hijriProvider = hijriProvider,
                chineseStyle = chineseStyle,
                locale = options.locale,
            )
        }
    }
}

/** One engine month with optional demo markers. */
@Composable
private fun SamplePlaygroundMonth(
    calendar: DemoCalendar,
    monthAnchor: Long?,
    todayEpochDay: Long,
    selection: CalendarSelection,
    onDayTap: (Long) -> Unit,
    showMarkers: Boolean,
    hijriProvider: HijriDateProvider,
    chineseStyle: ChineseTextStyle,
    locale: CalendarLocale? = null,
) {
    if (monthAnchor == null) {
        Text("Month outside the supported range of ${calendar.label}.")
        return
    }
    val markers = remember(calendar, monthAnchor, showMarkers, todayEpochDay) {
        if (!showMarkers) {
            emptyMap()
        } else {
            runCatching {
                sampleMonthData(
                    calendar,
                    monthAnchor,
                    todayEpochDay,
                    hijriProvider,
                    chineseStyle,
                    locale,
                ).days
                    .filter { day -> day.epochDay % 5L == 0L }
                    .associate { day ->
                        day.epochDay to listOf(CalendarDayMarker(SampleMarkerColor, "Reminder"))
                    }
            }.getOrDefault(emptyMap())
        }
    }

    when (calendar) {
        DemoCalendar.Gregorian -> GregorianCalendarMonth(
            month = GregorianYearMonth.fromEpochDay(monthAnchor),
            selection = selection,
            onDateClick = onDayTap,
            todayEpochDay = todayEpochDay,
            markers = markers,
            locale = locale,
        )

        DemoCalendar.Hijri -> HijriCalendarMonth(
            month = hijriProvider.dateForEpochDay(monthAnchor),
            selection = selection,
            onDateClick = onDayTap,
            todayEpochDay = todayEpochDay,
            markers = markers,
            dateProvider = hijriProvider,
            locale = locale,
        )

        DemoCalendar.SolarHijri -> SolarHijriCalendarMonth(
            month = SolarHijriDate.fromEpochDay(monthAnchor),
            selection = selection,
            onDateClick = onDayTap,
            todayEpochDay = todayEpochDay,
            markers = markers,
            locale = locale,
        )

        DemoCalendar.SolarHijriNative -> SolarHijriNativeCalendarMonth(
            month = SolarHijriDate.fromEpochDay(monthAnchor),
            selection = selection,
            onDateClick = onDayTap,
            todayEpochDay = todayEpochDay,
            markers = markers,
            locale = locale,
        )

        DemoCalendar.Chinese -> ChineseCalendarMonth(
            month = ChineseMonth.fromEpochDay(monthAnchor),
            selection = selection,
            onDateClick = onDayTap,
            style = chineseStyle,
            todayEpochDay = todayEpochDay,
            markers = markers,
        )
    }
}
