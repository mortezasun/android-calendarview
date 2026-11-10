package com.develotter.calendarview.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.develotter.calendarview.selection.CalendarSelection
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

/** Heatmap cell snapshot with intensity normalized into 0..1. */
data class HeatmapDay(val date: LocalDate, val intensity: Float)

/** Weeks are chronological columns; null entries are padding outside the quarter. */
data class HeatmapQuarterData(
    val start: LocalDate,
    val end: LocalDate,
    val weeks: List<List<HeatmapDay?>>,
)

/**
 * Calendar-agnostic heatmap day; [label] is the caller-formatted, localized date.
 * [isToday] draws the today border and [enabled] = false renders the cell dimmed and
 * not clickable, mirroring [CalendarDayCell] of the month grid.
 */
data class HeatmapDayData(
    val epochDay: Long,
    val label: String,
    val intensity: Float,
    val isToday: Boolean = false,
    val enabled: Boolean = true,
)

/** Calendar-agnostic month block: seven weekday rows, one column per week. */
data class HeatmapMonthData(
    val title: String,
    val weekdayLabels: List<String>,
    val weeks: List<List<HeatmapDayData?>>,
)

/** Placement of the seven weekdays and the week sequence inside a heatmap block. */
enum class HeatmapOrientation {
    /** Activity-chart layout: weekdays are rows, each week is a column; scrolls horizontally. */
    WeeksAsColumns,

    /** Month-grid layout: weekdays are columns, each week is a row; cells share the width. */
    WeeksAsRows,
}

/**
 * Geometry of heatmap cells. The defaults reproduce the original heatmap look:
 * 48dp square cells without gaps, rectangular shape and a 64dp weekday label column.
 * In [HeatmapOrientation.WeeksAsRows] cells take an equal share of the width and
 * [cellSize] is used as the row height; [weekdayLabelWidth] is ignored there.
 *
 * [cellSize] only controls the drawn cell, not the touch target. Every cell gets a layout slot of
 * at least the platform minimum interactive size ([LocalMinimumInteractiveComponentSize], 48dp by
 * default): in [HeatmapOrientation.WeeksAsColumns] both slot edges, in
 * [HeatmapOrientation.WeeksAsRows] the slot height. Cells drawn below that size therefore stay
 * easy to tap.
 *
 * [HeatmapOrientation.WeeksAsRows] gives each cell an equal share of the row width instead of a
 * fixed minimum, because seven weekday columns have to fit the viewport. Below roughly
 * `7 * 48dp + 6 * cellSpacing` of available width (for example a 320dp split-screen window) a
 * column is narrower than 48dp; use [HeatmapOrientation.WeeksAsColumns] when every cell needs a
 * full-size target.
 */
@Immutable
data class HeatmapStyle(
    val cellSize: Dp = 48.dp,
    val cellSpacing: Dp = 0.dp,
    val cellShape: Shape = RectangleShape,
    val weekdayLabelWidth: Dp = 64.dp,
    val todayBorderWidth: Dp = 1.5.dp,
    val selectedBorderWidth: Dp = 3.dp,
    val disabledAlpha: Float = 0.38f,
) {
    init {
        require(cellSize.value.isFinite() && cellSize.value > 0f) { "cellSize must be finite and positive" }
        require(cellSpacing.value.isFinite() && cellSpacing.value >= 0f) { "cellSpacing must be finite and non-negative" }
        require(weekdayLabelWidth.value.isFinite() && weekdayLabelWidth.value >= 0f) {
            "weekdayLabelWidth must be finite and non-negative"
        }
        require(todayBorderWidth.value.isFinite() && todayBorderWidth.value >= 0f) {
            "todayBorderWidth must be finite and non-negative"
        }
        require(selectedBorderWidth.value.isFinite() && selectedBorderWidth.value >= 0f) {
            "selectedBorderWidth must be finite and non-negative"
        }
        require(disabledAlpha in 0f..1f) { "disabledAlpha must be in 0..1" }
    }
}

/** Finite values are clamped; NaN and infinities become zero. */
fun normalizeHeatmapIntensity(value: Float): Float = if (value.isFinite()) value.coerceIn(0f, 1f) else 0f

/**
 * Gregorian quarter data independent of composition and Android views.
 * Missing intensities become zero; out-of-quarter keys are ignored. Inputs are not mutated.
 * Date arithmetic errors at the extreme LocalDate bounds propagate to the caller.
 */
fun heatmapQuarterData(
    year: Int,
    quarter: Int,
    intensities: Map<LocalDate, Float> = emptyMap(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
): HeatmapQuarterData {
    require(quarter in 1..4) { "quarter must be in 1..4" }
    val start = LocalDate.of(year, (quarter - 1) * 3 + 1, 1)
    val end = YearMonth.from(start).plusMonths(2).atEndOfMonth()
    val offset = Math.floorMod(start.dayOfWeek.value - firstDayOfWeek.value, 7)
    val gridStart = start.minusDays(offset.toLong())
    val count = (offset + ChronoUnit.DAYS.between(start, end).toInt() + 1 + 6) / 7
    return HeatmapQuarterData(start, end, List(count) { week ->
        List(7) { row ->
            val date = gridStart.plusDays((week * 7 + row).toLong())
            if (date < start || date > end) null
            else HeatmapDay(date, normalizeHeatmapIntensity(intensities[date] ?: 0f))
        }
    })
}

/**
 * Native Gregorian quarter heatmap with 48dp click targets and horizontal scrolling.
 * The ambient layout direction controls column placement. Labels follow [locale].
 * The default red-to-green scale preserves the legacy view's actual color direction;
 * callers can replace the color function and the composable cell content.
 *
 * Layout, cell geometry, header/weekday slots, today border, disabled dates and selection
 * highlight behave like [CalendarMonthGrid]: [selection] only renders state, the caller
 * still owns it and updates it from [onDayClick]. A selected cell shows
 * [selectedBorderColor] instead of the today border because the fill is the intensity color.
 */
@Composable
fun CalendarQuarterHeatmap(
    year: Int,
    quarter: Int,
    intensities: Map<LocalDate, Float>,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    showWeekdayLabels: Boolean = true,
    showHeader: Boolean = true,
    orientation: HeatmapOrientation = HeatmapOrientation.WeeksAsColumns,
    style: HeatmapStyle = HeatmapStyle(),
    today: LocalDate? = LocalDate.now(),
    isDateEnabled: (LocalDate) -> Boolean = { true },
    selection: CalendarSelection? = null,
    todayBorderColor: Color = MaterialTheme.colorScheme.tertiary,
    selectedBorderColor: Color = MaterialTheme.colorScheme.primary,
    header: @Composable (String) -> Unit = { Text(it) },
    weekdayContent: @Composable (String) -> Unit = { Text(it) },
    colorForIntensity: (Float) -> Color = { lerp(Color.Red, Color.Green, it) },
    dayContent: @Composable (HeatmapDay) -> Unit = {},
) {
    val data = heatmapQuarterData(year, quarter, intensities, firstDayOfWeek)
    val monthFormat = DateTimeFormatter.ofPattern("LLLL uuuu", locale)
    val dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)
    val percent = NumberFormat.getPercentInstance(locale)
    val config = HeatmapRenderConfig(
        showHeader = showHeader,
        showWeekdayLabels = showWeekdayLabels,
        orientation = orientation,
        style = style,
        selection = selection,
        todayBorderColor = todayBorderColor,
        selectedBorderColor = selectedBorderColor,
        header = header,
        weekdayContent = weekdayContent,
        colorForIntensity = colorForIntensity,
    )
    HeatmapBlock(
        title = "${data.start.format(monthFormat)} - ${data.end.format(monthFormat)}",
        weekdayLabels = List(7) { firstDayOfWeek.plus(it.toLong()).getDisplayName(TextStyle.SHORT_STANDALONE, locale) },
        weeks = data.weeks,
        config = config,
        modifier = modifier,
        cellInfo = { day ->
            HeatmapCellInfo(
                epochDay = day.date.toEpochDay(),
                intensity = day.intensity,
                description = "${day.date.format(dateFormat)}: ${percent.format(day.intensity)}",
                isToday = day.date == today,
                enabled = isDateEnabled(day.date),
            )
        },
        onClick = { onDayClick(it.date) },
        dayContent = dayContent,
    )
}

/**
 * Four native Compose quarters. The caller supplies vertical scrolling or a bounded host.
 * Data changes recompose the existing tree instead of appending duplicate quarter views.
 * This heatmap deliberately uses Gregorian LocalDate, matching the old heatmap API.
 * All configuration parameters are forwarded to every [CalendarQuarterHeatmap].
 */
@Composable
fun CalendarYearHeatmap(
    year: Int,
    intensities: Map<LocalDate, Float>,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    showWeekdayLabels: Boolean = true,
    showHeader: Boolean = true,
    orientation: HeatmapOrientation = HeatmapOrientation.WeeksAsColumns,
    style: HeatmapStyle = HeatmapStyle(),
    today: LocalDate? = LocalDate.now(),
    isDateEnabled: (LocalDate) -> Boolean = { true },
    selection: CalendarSelection? = null,
    todayBorderColor: Color = MaterialTheme.colorScheme.tertiary,
    selectedBorderColor: Color = MaterialTheme.colorScheme.primary,
    header: @Composable (String) -> Unit = { Text(it) },
    weekdayContent: @Composable (String) -> Unit = { Text(it) },
    colorForIntensity: (Float) -> Color = { lerp(Color.Red, Color.Green, it) },
    dayContent: @Composable (HeatmapDay) -> Unit = {},
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        for (quarter in 1..4) {
            CalendarQuarterHeatmap(
                year, quarter, intensities, onDayClick, locale = locale,
                firstDayOfWeek = firstDayOfWeek, showWeekdayLabels = showWeekdayLabels,
                showHeader = showHeader, orientation = orientation, style = style,
                today = today, isDateEnabled = isDateEnabled, selection = selection,
                todayBorderColor = todayBorderColor, selectedBorderColor = selectedBorderColor,
                header = header, weekdayContent = weekdayContent,
                colorForIntensity = colorForIntensity, dayContent = dayContent,
            )
        }
    }
}

/**
 * Calendar-agnostic year heatmap built from caller-supplied month blocks.
 *
 * Use this for Hijri, Solar Hijri or any other calendar engine: :library stays independent
 * of concrete calendars. Every month needs seven weekday labels and week columns with seven
 * entries each (null marks padding outside the month). Intensities are normalized to 0..1.
 * The caller owns year navigation and builds [months] with its calendar engine.
 * Today and disabled state come from [HeatmapDayData]; the remaining configuration
 * matches [CalendarQuarterHeatmap].
 */
@Composable
fun CalendarYearHeatmap(
    months: List<HeatmapMonthData>,
    onDayClick: (HeatmapDayData) -> Unit,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.getDefault(),
    showWeekdayLabels: Boolean = true,
    showHeader: Boolean = true,
    orientation: HeatmapOrientation = HeatmapOrientation.WeeksAsColumns,
    style: HeatmapStyle = HeatmapStyle(),
    selection: CalendarSelection? = null,
    todayBorderColor: Color = MaterialTheme.colorScheme.tertiary,
    selectedBorderColor: Color = MaterialTheme.colorScheme.primary,
    header: @Composable (String) -> Unit = { Text(it) },
    weekdayContent: @Composable (String) -> Unit = { Text(it) },
    colorForIntensity: (Float) -> Color = { lerp(Color.Red, Color.Green, it) },
    dayContent: @Composable (HeatmapDayData) -> Unit = {},
) {
    require(months.isNotEmpty()) { "At least one heatmap month is required" }
    require(months.all { month ->
        month.weekdayLabels.size == 7 && month.weeks.all { week -> week.size == 7 }
    }) { "Heatmap months require seven weekday labels and seven cells per week" }
    val percent = NumberFormat.getPercentInstance(locale)
    val config = HeatmapRenderConfig(
        showHeader = showHeader,
        showWeekdayLabels = showWeekdayLabels,
        orientation = orientation,
        style = style,
        selection = selection,
        todayBorderColor = todayBorderColor,
        selectedBorderColor = selectedBorderColor,
        header = header,
        weekdayContent = weekdayContent,
        colorForIntensity = colorForIntensity,
    )
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        months.forEach { month ->
            HeatmapBlock(
                title = month.title,
                weekdayLabels = month.weekdayLabels,
                weeks = month.weeks,
                config = config,
                modifier = Modifier,
                cellInfo = { day ->
                    val intensity = normalizeHeatmapIntensity(day.intensity)
                    HeatmapCellInfo(
                        epochDay = day.epochDay,
                        intensity = intensity,
                        description = "${day.label}: ${percent.format(intensity)}",
                        isToday = day.isToday,
                        enabled = day.enabled,
                    )
                },
                onClick = onDayClick,
                dayContent = dayContent,
            )
        }
    }
}

/** Render options shared by all public heatmaps, bundled to keep the private renderer small. */
private class HeatmapRenderConfig(
    val showHeader: Boolean,
    val showWeekdayLabels: Boolean,
    val orientation: HeatmapOrientation,
    val style: HeatmapStyle,
    val selection: CalendarSelection?,
    val todayBorderColor: Color,
    val selectedBorderColor: Color,
    val header: @Composable (String) -> Unit,
    val weekdayContent: @Composable (String) -> Unit,
    val colorForIntensity: (Float) -> Color,
)

/** Calendar-independent view of one cell; [intensity] is already normalized. */
private class HeatmapCellInfo(
    val epochDay: Long,
    val intensity: Float,
    val description: String,
    val isToday: Boolean,
    val enabled: Boolean,
)

@Composable
private fun <T : Any> HeatmapBlock(
    title: String,
    weekdayLabels: List<String>,
    weeks: List<List<T?>>,
    config: HeatmapRenderConfig,
    modifier: Modifier,
    cellInfo: (T) -> HeatmapCellInfo,
    onClick: (T) -> Unit,
    dayContent: @Composable (T) -> Unit,
) {
    val style = config.style
    val spacing = Arrangement.spacedBy(style.cellSpacing)
    // Visual cell size and touch target are independent: a cell smaller than the platform
    // minimum still occupies a full-size slot, so hit areas never shrink below the guideline.
    val slotSize = maxOf(style.cellSize, LocalMinimumInteractiveComponentSize.current)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (config.showHeader) {
            config.header(title)
        }
        when (config.orientation) {
            HeatmapOrientation.WeeksAsColumns -> Row {
                if (config.showWeekdayLabels) {
                    Column(verticalArrangement = spacing) {
                        weekdayLabels.forEach { label ->
                            Box(
                                Modifier.size(width = style.weekdayLabelWidth, height = slotSize),
                                contentAlignment = Alignment.Center,
                            ) { config.weekdayContent(label) }
                        }
                    }
                }
                Row(Modifier.weight(1f).horizontalScroll(rememberScrollState()), horizontalArrangement = spacing) {
                    weeks.forEach { week ->
                        Column(verticalArrangement = spacing) {
                            week.forEach { day ->
                                HeatmapCell(
                                    day,
                                    slotModifier = Modifier.size(slotSize),
                                    visualModifier = Modifier.size(style.cellSize),
                                    config = config,
                                    cellInfo = cellInfo,
                                    onClick = onClick,
                                    dayContent = dayContent,
                                )
                            }
                        }
                    }
                }
            }
            HeatmapOrientation.WeeksAsRows -> Column(verticalArrangement = spacing) {
                if (config.showWeekdayLabels) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = spacing) {
                        weekdayLabels.forEach { label ->
                            Box(Modifier.weight(1f).padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                config.weekdayContent(label)
                            }
                        }
                    }
                }
                weeks.forEach { week ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = spacing) {
                        week.forEach { day ->
                            HeatmapCell(
                                day,
                                slotModifier = Modifier.weight(1f).height(slotSize),
                                visualModifier = Modifier.fillMaxWidth().height(style.cellSize),
                                config = config,
                                cellInfo = cellInfo,
                                onClick = onClick,
                                dayContent = dayContent,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun <T : Any> HeatmapCell(
    day: T?,
    slotModifier: Modifier,
    visualModifier: Modifier,
    config: HeatmapRenderConfig,
    cellInfo: (T) -> HeatmapCellInfo,
    onClick: (T) -> Unit,
    dayContent: @Composable (T) -> Unit,
) {
    if (day == null) {
        // Padding cells keep the slot so the grid stays aligned with the real cells.
        Box(slotModifier)
        return
    }
    val info = cellInfo(day)
    val style = config.style
    val selection = config.selection
    val selected = selection != null && info.epochDay in selection
    val border = when {
        selected && style.selectedBorderWidth > 0.dp ->
            Modifier.border(style.selectedBorderWidth, config.selectedBorderColor, style.cellShape)
        info.isToday && style.todayBorderWidth > 0.dp ->
            Modifier.border(style.todayBorderWidth, config.todayBorderColor, style.cellShape)
        else -> Modifier
    }
    // Only expose selected/not-selected semantics when the caller actually uses selection.
    val interaction = if (selection != null) {
        Modifier.selectable(selected = selected, enabled = info.enabled, role = Role.Button) { onClick(day) }
    } else {
        Modifier.clickable(enabled = info.enabled, role = Role.Button) { onClick(day) }
    }
    Box(
        slotModifier
            .semantics { contentDescription = info.description }
            .then(interaction),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            visualModifier
                .alpha(if (info.enabled) 1f else style.disabledAlpha)
                .clip(style.cellShape)
                .background(config.colorForIntensity(info.intensity))
                .then(border),
            contentAlignment = Alignment.Center,
        ) { dayContent(day) }
    }
}
