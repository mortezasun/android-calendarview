package com.develotter.calendarview.kmpsample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.develotter.calendarview.calendars.chineseCompose.ChineseCalendarMonth
import com.develotter.calendarview.calendars.chineseCompose.chineseSecondaryLabel
import com.develotter.calendarview.calendars.chineseCompose.rememberChineseMonthState
import com.develotter.calendarview.calendars.chineseNative.ChineseCalendarEngine
import com.develotter.calendarview.calendars.chineseNative.ChineseMonth
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.calendars.gregorian.compose.GregorianCalendarMonth
import com.develotter.calendarview.calendars.gregorian.compose.gregorianMonthTitle
import com.develotter.calendarview.calendars.gregorian.compose.gregorianSecondaryLabel
import com.develotter.calendarview.calendars.gregorian.compose.rememberGregorianMonthState
import com.develotter.calendarview.calendars.hijri.HijriDateProvider
import com.develotter.calendarview.calendars.hijri.UmmAlQuraHijriDateProvider
import com.develotter.calendarview.calendars.hijri.compose.HijriCalendarMonth
import com.develotter.calendarview.calendars.hijri.compose.hijriSecondaryLabel
import com.develotter.calendarview.calendars.hijri.compose.rememberHijriMonthState
import com.develotter.calendarview.calendars.solarHijri.compose.SolarHijriCalendarMonth
import com.develotter.calendarview.calendars.solarHijri.compose.rememberSolarHijriMonthState
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriSecondaryLabel
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.compose.SolarHijriNativeCalendarMonth
import com.develotter.calendarview.calendars.solarHijriNative.compose.rememberSolarHijriNativeMonthState
import com.develotter.calendarview.calendars.solarHijriNative.compose.solarHijriNativeSecondaryLabel
import com.develotter.calendarview.compose.CalendarAgenda
import com.develotter.calendarview.compose.CalendarMonthHeader
import com.develotter.calendarview.compose.CalendarMonthNavigationState
import com.develotter.calendarview.compose.CalendarMonthPicker
import com.develotter.calendarview.compose.CalendarWeekGrid
import com.develotter.calendarview.enums.TypeSelectDay
import com.develotter.calendarview.locale.CalendarLocale
import com.develotter.calendarview.selection.CalendarSelection

/**
 * Engine-specific month, week or agenda view for the Calendars page. The caller owns the visible
 * day through [CalendarExplorerOptions.selectedEpochDay]; this composable only reports clicks.
 */
@Composable
internal fun SampleCalendarExplorer(
    options: CalendarExplorerOptions,
    onSelect: (Long) -> Unit,
    todayEpochDay: Long,
) {
    val hijriProvider = remember(options.hijriOffset) { sampleHijriProvider(options.hijriOffset) }
    val chineseStyle = options.chineseScript.textStyle()
    val selection = remember(options.selectedEpochDay) {
        CalendarSelection.from(TypeSelectDay.Single, listOfNotNull(options.selectedEpochDay))
    }
    val secondaryLabel = remember(options.secondaryCalendar, options.locale, hijriProvider, chineseStyle) {
        sampleSecondaryLabel(options.secondaryCalendar, hijriProvider, chineseStyle, options.locale)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when (options.view) {
            DemoCalendarView.Month -> SampleMonthSection(
                options = options,
                selection = selection,
                onSelect = onSelect,
                todayEpochDay = todayEpochDay,
                hijriProvider = hijriProvider,
                chineseStyle = chineseStyle,
                secondaryLabel = secondaryLabel,
            )

            DemoCalendarView.Week -> SampleWeekSection(
                options = options,
                selection = selection,
                onSelect = onSelect,
                todayEpochDay = todayEpochDay,
                hijriProvider = hijriProvider,
                chineseStyle = chineseStyle,
                secondaryLabel = secondaryLabel,
            )

            DemoCalendarView.Agenda -> SampleAgendaSection(
                options = options,
                selection = selection,
                onSelect = onSelect,
                todayEpochDay = todayEpochDay,
                hijriProvider = hijriProvider,
                chineseStyle = chineseStyle,
            )
        }
    }
}

/** Compact secondary label of [calendar], or null when no second calendar is selected. */
internal fun sampleSecondaryLabel(
    calendar: DemoCalendar?,
    hijriDateProvider: HijriDateProvider,
    chineseStyle: ChineseTextStyle,
    locale: CalendarLocale? = null,
): ((Long) -> String?)? = when (calendar) {
    null -> null
    DemoCalendar.Gregorian -> { epochDay -> gregorianSecondaryLabel(epochDay, locale) }
    DemoCalendar.Hijri -> { epochDay -> hijriSecondaryLabel(epochDay, hijriDateProvider, locale) }
    DemoCalendar.SolarHijri -> { epochDay -> solarHijriSecondaryLabel(epochDay, locale) }
    DemoCalendar.SolarHijriNative -> { epochDay -> solarHijriNativeSecondaryLabel(epochDay, locale) }
    DemoCalendar.Chinese -> { epochDay -> chineseSecondaryLabel(epochDay, chineseStyle) }
}

@Composable
private fun SampleMonthSection(
    options: CalendarExplorerOptions,
    selection: CalendarSelection,
    onSelect: (Long) -> Unit,
    todayEpochDay: Long,
    hijriProvider: HijriDateProvider,
    chineseStyle: ChineseTextStyle,
    secondaryLabel: ((Long) -> String?)?,
) {
    val firstDay = options.firstDayOfWeek
    when (options.calendar) {
        DemoCalendar.Gregorian -> {
            val language = options.locale ?: CalendarLocale.English
            val state = rememberGregorianMonthState(
                initialMonth = GregorianYearMonth.fromEpochDay(todayEpochDay),
            )
            var showPicker by remember { mutableStateOf(false) }
            Column {
                TextButton(onClick = { showPicker = !showPicker }) {
                    Text(SampleStrings.jumpToMonth(language))
                }
                if (showPicker) {
                    CalendarMonthPicker(
                        yearRange = (state.month.year - 50)..(state.month.year + 50),
                        selectedYear = state.month.year,
                        selectedMonth = state.month.month,
                        onMonthSelected = { year, month ->
                            state.goTo(GregorianYearMonth(year, month))
                            showPicker = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        onYearChange = { year ->
                            state.goTo(GregorianYearMonth(year, state.month.month))
                        },
                        monthLabel = { month ->
                            Text(
                                gregorianMonthTitle(
                                    GregorianYearMonth(state.month.year, month),
                                    language,
                                ),
                            )
                        },
                        previousYearContentDescription = SampleStrings.previousYear(language),
                        nextYearContentDescription = SampleStrings.nextYear(language),
                    )
                }
                SampleMonthFrame(title = state.title(options.locale), state = state) {
                    if (firstDay == null) {
                        GregorianCalendarMonth(
                            month = state.month,
                            selection = selection,
                            onDateClick = onSelect,
                            todayEpochDay = todayEpochDay,
                            showHeader = false,
                            secondaryLabel = secondaryLabel,
                            locale = options.locale,
                            keyboardNavigation = true,
                            onMonthStep = { step -> state.goTo(state.month.plusMonths(step)) },
                        )
                    } else {
                        GregorianCalendarMonth(
                            month = state.month,
                            selection = selection,
                            onDateClick = onSelect,
                            firstDayOfWeek = firstDay,
                            todayEpochDay = todayEpochDay,
                            showHeader = false,
                            secondaryLabel = secondaryLabel,
                            locale = options.locale,
                            keyboardNavigation = true,
                            onMonthStep = { step -> state.goTo(state.month.plusMonths(step)) },
                        )
                    }
                }
            }
        }

        DemoCalendar.Hijri -> {
            val state = rememberHijriMonthState(
                initialMonth = hijriProvider.dateForEpochDay(todayEpochDay),
                dateProvider = hijriProvider,
            )
            SampleMonthFrame(title = state.title(options.locale), state = state) {
                if (firstDay == null) {
                    HijriCalendarMonth(
                        month = state.month,
                        selection = selection,
                        onDateClick = onSelect,
                        todayEpochDay = todayEpochDay,
                        showHeader = false,
                        dateProvider = hijriProvider,
                        secondaryLabel = secondaryLabel,
                        locale = options.locale,
                    )
                } else {
                    HijriCalendarMonth(
                        month = state.month,
                        selection = selection,
                        onDateClick = onSelect,
                        firstDayOfWeek = firstDay,
                        todayEpochDay = todayEpochDay,
                        showHeader = false,
                        dateProvider = hijriProvider,
                        secondaryLabel = secondaryLabel,
                        locale = options.locale,
                    )
                }
            }
        }

        DemoCalendar.SolarHijri -> {
            val state = rememberSolarHijriMonthState(
                initialMonth = SolarHijriDate.fromEpochDay(todayEpochDay),
                currentEpochDay = { todayEpochDay },
            )
            SampleMonthFrame(title = state.title(options.locale), state = state) {
                if (firstDay == null) {
                    SolarHijriCalendarMonth(
                        month = state.month,
                        selection = selection,
                        onDateClick = onSelect,
                        todayEpochDay = todayEpochDay,
                        showHeader = false,
                        secondaryLabel = secondaryLabel,
                        locale = options.locale,
                    )
                } else {
                    SolarHijriCalendarMonth(
                        month = state.month,
                        selection = selection,
                        onDateClick = onSelect,
                        firstDayOfWeek = firstDay,
                        todayEpochDay = todayEpochDay,
                        showHeader = false,
                        secondaryLabel = secondaryLabel,
                        locale = options.locale,
                    )
                }
            }
        }

        DemoCalendar.SolarHijriNative -> {
            val state = rememberSolarHijriNativeMonthState(
                initialMonth = SolarHijriDate.fromEpochDay(todayEpochDay),
                currentEpochDay = { todayEpochDay },
            )
            SampleMonthFrame(title = state.title(options.locale), state = state) {
                if (firstDay == null) {
                    SolarHijriNativeCalendarMonth(
                        month = state.month,
                        selection = selection,
                        onDateClick = onSelect,
                        todayEpochDay = todayEpochDay,
                        showHeader = false,
                        secondaryLabel = secondaryLabel,
                        locale = options.locale,
                    )
                } else {
                    SolarHijriNativeCalendarMonth(
                        month = state.month,
                        selection = selection,
                        onDateClick = onSelect,
                        firstDayOfWeek = firstDay,
                        todayEpochDay = todayEpochDay,
                        showHeader = false,
                        secondaryLabel = secondaryLabel,
                        locale = options.locale,
                    )
                }
            }
        }

        DemoCalendar.Chinese -> {
            val leapMonth = options.chineseLeapMonthRequest
                .takeIf { it > 0 }
                ?.let { runCatching { ChineseCalendarEngine.leapMonth(2023) }.getOrNull() }
            val initialMonth = leapMonth ?: ChineseMonth.fromEpochDay(todayEpochDay)
            val state = rememberChineseMonthState(
                initialMonth = initialMonth,
                today = { todayEpochDay },
            )
            SampleMonthFrame(title = state.title(chineseStyle), state = state) {
                if (firstDay == null) {
                    ChineseCalendarMonth(
                        month = state.month,
                        selection = selection,
                        onDateClick = onSelect,
                        style = chineseStyle,
                        todayEpochDay = todayEpochDay,
                        showHeader = false,
                        secondaryLabel = secondaryLabel,
                    )
                } else {
                    ChineseCalendarMonth(
                        month = state.month,
                        selection = selection,
                        onDateClick = onSelect,
                        style = chineseStyle,
                        firstDayOfWeek = firstDay,
                        todayEpochDay = todayEpochDay,
                        showHeader = false,
                        secondaryLabel = secondaryLabel,
                    )
                }
            }
        }
    }
}

@Composable
private fun SampleWeekSection(
    options: CalendarExplorerOptions,
    selection: CalendarSelection,
    onSelect: (Long) -> Unit,
    todayEpochDay: Long,
    hijriProvider: HijriDateProvider,
    chineseStyle: ChineseTextStyle,
    secondaryLabel: ((Long) -> String?)?,
) {
    val anchorEpochDay = options.selectedEpochDay ?: todayEpochDay
    val week = remember(
        options.calendar,
        options.locale,
        options.firstDayOfWeek,
        anchorEpochDay,
        todayEpochDay,
        hijriProvider,
        chineseStyle,
    ) {
        runCatching {
            sampleWeekData(
                options.calendar,
                anchorEpochDay,
                todayEpochDay,
                hijriProvider,
                chineseStyle,
                options.locale,
                options.firstDayOfWeek,
            )
        }
    }
    val data = week.getOrNull()
    if (data == null) {
        Text("This week is outside the supported range of ${options.calendar.label}.")
        return
    }
    CalendarWeekGrid(
        data = data,
        selection = selection,
        onDayClick = onSelect,
        modifier = Modifier.fillMaxSize(),
        secondaryLabel = secondaryLabel,
    )
}

@Composable
private fun SampleAgendaSection(
    options: CalendarExplorerOptions,
    selection: CalendarSelection,
    onSelect: (Long) -> Unit,
    todayEpochDay: Long,
    hijriProvider: HijriDateProvider,
    chineseStyle: ChineseTextStyle,
) {
    val anchorEpochDay = options.selectedEpochDay ?: todayEpochDay
    val month = remember(
        options.calendar,
        options.locale,
        anchorEpochDay,
        todayEpochDay,
        hijriProvider,
        chineseStyle,
    ) {
        runCatching {
            sampleMonthData(
                options.calendar,
                anchorEpochDay,
                todayEpochDay,
                hijriProvider,
                chineseStyle,
                options.locale,
            )
        }
    }
    val data = month.getOrNull()
    if (data == null) {
        Text("This month is outside the supported range of ${options.calendar.label}.")
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        CalendarAgenda(days = data.days, selection = selection, onDayClick = onSelect)
    }
}

/** Month title with previous/next navigation followed by the caller's month content. */
@Composable
private fun SampleMonthFrame(
    title: String,
    state: CalendarMonthNavigationState,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CalendarMonthHeader(
            title = title,
            state = state,
            previousMonthContentDescription = "Previous month",
            nextMonthContentDescription = "Next month",
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            content()
        }
    }
}

/**
 * Single-month preview used inside the Playground dialog, without selection. The month follows
 * [LocalPlaygroundFirstDayOfWeek]; `null` keeps the default of each calendar engine.
 */
@Composable
internal fun SampleDialogMonth(
    calendar: DemoCalendar,
    todayEpochDay: Long,
    locale: CalendarLocale? = null,
) {
    val firstDayOfWeek = LocalPlaygroundFirstDayOfWeek.current
    val selection = remember { CalendarSelection.from(TypeSelectDay.None, emptyList()) }
    val onDateClick: (Long) -> Unit = {}

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Box(Modifier.fillMaxWidth()) {
            when (calendar) {
                DemoCalendar.Gregorian -> {
                    val month = GregorianYearMonth.fromEpochDay(todayEpochDay)
                    if (firstDayOfWeek == null) {
                        GregorianCalendarMonth(
                            month = month,
                            selection = selection,
                            onDateClick = onDateClick,
                            todayEpochDay = todayEpochDay,
                            locale = locale,
                        )
                    } else {
                        GregorianCalendarMonth(
                            month = month,
                            selection = selection,
                            onDateClick = onDateClick,
                            firstDayOfWeek = firstDayOfWeek,
                            todayEpochDay = todayEpochDay,
                            locale = locale,
                        )
                    }
                }

                DemoCalendar.Hijri -> {
                    val month = UmmAlQuraHijriDateProvider.dateForEpochDay(todayEpochDay)
                    if (firstDayOfWeek == null) {
                        HijriCalendarMonth(
                            month = month,
                            selection = selection,
                            onDateClick = onDateClick,
                            todayEpochDay = todayEpochDay,
                            locale = locale,
                        )
                    } else {
                        HijriCalendarMonth(
                            month = month,
                            selection = selection,
                            onDateClick = onDateClick,
                            firstDayOfWeek = firstDayOfWeek,
                            todayEpochDay = todayEpochDay,
                            locale = locale,
                        )
                    }
                }

                DemoCalendar.SolarHijri -> {
                    val month = SolarHijriDate.fromEpochDay(todayEpochDay)
                    if (firstDayOfWeek == null) {
                        SolarHijriCalendarMonth(
                            month = month,
                            selection = selection,
                            onDateClick = onDateClick,
                            todayEpochDay = todayEpochDay,
                            locale = locale,
                        )
                    } else {
                        SolarHijriCalendarMonth(
                            month = month,
                            selection = selection,
                            onDateClick = onDateClick,
                            firstDayOfWeek = firstDayOfWeek,
                            todayEpochDay = todayEpochDay,
                            locale = locale,
                        )
                    }
                }

                DemoCalendar.SolarHijriNative -> {
                    val month = SolarHijriDate.fromEpochDay(todayEpochDay)
                    if (firstDayOfWeek == null) {
                        SolarHijriNativeCalendarMonth(
                            month = month,
                            selection = selection,
                            onDateClick = onDateClick,
                            todayEpochDay = todayEpochDay,
                            locale = locale,
                        )
                    } else {
                        SolarHijriNativeCalendarMonth(
                            month = month,
                            selection = selection,
                            onDateClick = onDateClick,
                            firstDayOfWeek = firstDayOfWeek,
                            todayEpochDay = todayEpochDay,
                            locale = locale,
                        )
                    }
                }

                DemoCalendar.Chinese -> {
                    val month = ChineseMonth.fromEpochDay(todayEpochDay)
                    if (firstDayOfWeek == null) {
                        ChineseCalendarMonth(
                            month = month,
                            selection = selection,
                            onDateClick = onDateClick,
                            todayEpochDay = todayEpochDay,
                        )
                    } else {
                        ChineseCalendarMonth(
                            month = month,
                            selection = selection,
                            onDateClick = onDateClick,
                            firstDayOfWeek = firstDayOfWeek,
                            todayEpochDay = todayEpochDay,
                        )
                    }
                }
            }
        }
    }
}
