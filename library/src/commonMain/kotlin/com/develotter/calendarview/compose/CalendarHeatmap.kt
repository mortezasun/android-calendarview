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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.develotter.calendarview.selection.CalendarSelection

/** Heatmap cell snapshot with intensity normalized into 0..1. */
@Immutable
data class HeatmapDay(val epochDay: Long, val intensity: Float)

/** Weeks are chronological columns; null entries are padding outside the quarter. */
@Immutable
data class HeatmapQuarterData(
    val startEpochDay: Long,
    val endEpochDay: Long,
    val weeks: List<List<HeatmapDay?>>,
)

/**
 * Calendar-agnostic heatmap day; [label] is the caller-formatted, localized date.
 * [isToday] draws the today border and [enabled] = false renders the cell dimmed and
 * not clickable, mirroring [CalendarDayCell] of the month grid.
 */
@Immutable
data class HeatmapDayData(
    val epochDay: Long,
    val label: String,
    val intensity: Float,
    val isToday: Boolean = false,
    val enabled: Boolean = true,
)

/** Calendar-agnostic month block: seven weekday rows, one column per week. */
@Immutable
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
 * Stepped intensity scale for heatmaps: [thresholds] are ascending upper bounds inside `0..1`
 * (for example `listOf(0.25f, 0.5f, 0.75f)` for four steps). Each intensity is snapped down to the
 * lower bound of its bucket before it reaches `colorForIntensity`, so a stepped palette needs no
 * per-call branching, and [HeatmapLegend] can render exactly the same steps.
 *
 * The value does not change intensities stored in the data or the accessibility labels; it only
 * changes the color lookup and the legend.
 */
@Immutable
data class HeatmapBuckets(val thresholds: List<Float> = emptyList()) {
    init {
        require(thresholds.all { it.isFinite() && it in 0f..1f }) {
            "Heatmap thresholds must be finite and inside 0..1"
        }
        require(thresholds.zipWithNext().all { (left, right) -> left < right }) {
            "Heatmap thresholds must be strictly ascending"
        }
    }

    /** Number of steps: one more than the number of thresholds. */
    val bucketCount: Int get() = thresholds.size + 1

    /** Lower bound of the step that contains [intensity]. */
    fun intensityForBucket(bucket: Int): Float {
        require(bucket in 0 until bucketCount) { "bucket must be in 0 until $bucketCount" }
        return if (bucket == 0) 0f else thresholds[bucket - 1]
    }

    /**
     * Zero-based step index of a normalized intensity. A value equal to a threshold stays in the
     * step that threshold bounds, so the steps are closed on their upper edge; NaN and infinities
     * are normalized to zero first.
     */
    fun bucketOf(intensity: Float): Int {
        val normalized = normalizeHeatmapIntensity(intensity)
        return thresholds.indexOfFirst { normalized <= it }.let { if (it < 0) thresholds.size else it }
    }

    /** Intensity passed to `colorForIntensity` for a point on the normalized scale. */
    fun colorIntensityFor(intensity: Float): Float = intensityForBucket(bucketOf(intensity))
}

/**
 * Horizontal legend for a stepped heatmap scale.
 *
 * One swatch per bucket, coloured with the same `colorForIntensity` the heatmap uses, so legend and
 * cells cannot drift apart. The host supplies [label] and [contentDescription] because `:library`
 * ships no UI strings; without a label the swatches still render for a purely visual legend.
 * The legend is not interactive and adds no touch targets.
 */
@Composable
fun HeatmapLegend(
    buckets: HeatmapBuckets,
    modifier: Modifier = Modifier,
    style: HeatmapStyle = HeatmapStyle(),
    colorForIntensity: (Float) -> Color = { lerp(Color.Red, Color.Green, it) },
    label: @Composable (Int) -> Unit = {},
    contentDescription: ((Int) -> String)? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(style.cellSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (bucket in 0 until buckets.bucketCount) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(style.cellSize)
                        .clip(style.cellShape)
                        .background(colorForIntensity(buckets.intensityForBucket(bucket)))
                        .then(
                            if (contentDescription == null) Modifier else Modifier.semantics {
                                this.contentDescription = contentDescription(bucket)
                            },
                        ),
                )
                label(bucket)
            }
        }
    }
}

/**
 * Gregorian quarter data independent of composition and platform date APIs.
 * Missing intensities become zero; out-of-quarter keys are ignored. Inputs are not mutated.
 * [firstDayOfWeek] uses ISO-8601 numbering (1 = Monday .. 7 = Sunday).
 */
fun heatmapQuarterData(
    year: Int,
    quarter: Int,
    intensities: Map<Long, Float> = emptyMap(),
    firstDayOfWeek: Int = 1,
): HeatmapQuarterData {
    require(quarter in 1..4) { "quarter must be in 1..4" }
    require(firstDayOfWeek in 1..7) { "firstDayOfWeek must be in 1..7" }
    val startMonth = (quarter - 1) * 3 + 1
    val endMonth = startMonth + 2
    val start = gregorianToEpochDay(year, startMonth, 1)
    val end = gregorianToEpochDay(year, endMonth, gregorianDaysInMonth(year, endMonth))
    val startIsoWeekday = isoWeekdayFromEpochDay(start)
    val offset = ((startIsoWeekday - firstDayOfWeek) % 7 + 7) % 7
    val gridStart = start - offset
    val count = (offset + (end - start).toInt() + 1 + 6) / 7
    return HeatmapQuarterData(start, end, List(count) { week ->
        List(7) { row ->
            val epochDay = gridStart + (week * 7 + row).toLong()
            if (epochDay < start || epochDay > end) null
            else HeatmapDay(epochDay, normalizeHeatmapIntensity(intensities[epochDay] ?: 0f))
        }
    })
}

/**
 * Native Gregorian quarter heatmap with 48dp click targets and horizontal scrolling.
 * The ambient layout direction controls column placement.
 * [firstDayOfWeek] uses ISO-8601 numbering (1 = Monday .. 7 = Sunday).
 */
@Composable
fun CalendarQuarterHeatmap(
    year: Int,
    quarter: Int,
    intensities: Map<Long, Float>,
    onDayClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    firstDayOfWeek: Int = 1,
    showWeekdayLabels: Boolean = true,
    showHeader: Boolean = true,
    orientation: HeatmapOrientation = HeatmapOrientation.WeeksAsColumns,
    style: HeatmapStyle = HeatmapStyle(),
    todayEpochDay: Long? = HeatmapPlatformFormatter.currentEpochDay(),
    isDateEnabled: (Long) -> Boolean = { true },
    selection: CalendarSelection? = null,
    todayBorderColor: Color = MaterialTheme.colorScheme.tertiary,
    selectedBorderColor: Color = MaterialTheme.colorScheme.primary,
    header: @Composable (String) -> Unit = { Text(it) },
    weekdayContent: @Composable (String) -> Unit = { Text(it) },
    buckets: HeatmapBuckets? = null,
    colorForIntensity: (Float) -> Color = { lerp(Color.Red, Color.Green, it) },
    dayContent: @Composable (HeatmapDay) -> Unit = {},
) {
    val data = heatmapQuarterData(year, quarter, intensities, firstDayOfWeek)
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
        buckets = buckets,
        colorForIntensity = colorForIntensity,
    )
    HeatmapBlock(
        title = "${HeatmapPlatformFormatter.formatMonthYear(data.startEpochDay)} - ${HeatmapPlatformFormatter.formatMonthYear(data.endEpochDay)}",
        weekdayLabels = List(7) {
            val isoDay = ((firstDayOfWeek - 1 + it) % 7) + 1
            HeatmapPlatformFormatter.weekdayShortLabel(isoDay)
        },
        weeks = data.weeks,
        config = config,
        modifier = modifier,
        cellInfo = { day ->
            HeatmapCellInfo(
                epochDay = day.epochDay,
                intensity = day.intensity,
                description = "${HeatmapPlatformFormatter.formatFullDate(day.epochDay)}: ${HeatmapPlatformFormatter.formatPercent(day.intensity)}",
                isToday = day.epochDay == todayEpochDay,
                enabled = isDateEnabled(day.epochDay),
            )
        },
        onClick = { onDayClick(it.epochDay) },
        dayContent = dayContent,
    )
}

/**
 * Four native Compose quarters. The caller supplies vertical scrolling or a bounded host.
 * Data changes recompose the existing tree instead of appending duplicate quarter views.
 * All configuration parameters are forwarded to every [CalendarQuarterHeatmap].
 */
@Composable
fun CalendarYearHeatmap(
    year: Int,
    intensities: Map<Long, Float>,
    onDayClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    firstDayOfWeek: Int = 1,
    showWeekdayLabels: Boolean = true,
    showHeader: Boolean = true,
    orientation: HeatmapOrientation = HeatmapOrientation.WeeksAsColumns,
    style: HeatmapStyle = HeatmapStyle(),
    todayEpochDay: Long? = HeatmapPlatformFormatter.currentEpochDay(),
    isDateEnabled: (Long) -> Boolean = { true },
    selection: CalendarSelection? = null,
    todayBorderColor: Color = MaterialTheme.colorScheme.tertiary,
    selectedBorderColor: Color = MaterialTheme.colorScheme.primary,
    header: @Composable (String) -> Unit = { Text(it) },
    weekdayContent: @Composable (String) -> Unit = { Text(it) },
    buckets: HeatmapBuckets? = null,
    colorForIntensity: (Float) -> Color = { lerp(Color.Red, Color.Green, it) },
    dayContent: @Composable (HeatmapDay) -> Unit = {},
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        for (quarter in 1..4) {
            CalendarQuarterHeatmap(
                year = year,
                quarter = quarter,
                intensities = intensities,
                onDayClick = onDayClick,
                firstDayOfWeek = firstDayOfWeek,
                showWeekdayLabels = showWeekdayLabels,
                showHeader = showHeader,
                orientation = orientation,
                style = style,
                todayEpochDay = todayEpochDay,
                isDateEnabled = isDateEnabled,
                selection = selection,
                todayBorderColor = todayBorderColor,
                selectedBorderColor = selectedBorderColor,
                header = header,
                weekdayContent = weekdayContent,
                buckets = buckets,
                colorForIntensity = colorForIntensity,
                dayContent = dayContent,
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
    showWeekdayLabels: Boolean = true,
    showHeader: Boolean = true,
    orientation: HeatmapOrientation = HeatmapOrientation.WeeksAsColumns,
    style: HeatmapStyle = HeatmapStyle(),
    selection: CalendarSelection? = null,
    todayBorderColor: Color = MaterialTheme.colorScheme.tertiary,
    selectedBorderColor: Color = MaterialTheme.colorScheme.primary,
    header: @Composable (String) -> Unit = { Text(it) },
    weekdayContent: @Composable (String) -> Unit = { Text(it) },
    buckets: HeatmapBuckets? = null,
    colorForIntensity: (Float) -> Color = { lerp(Color.Red, Color.Green, it) },
    dayContent: @Composable (HeatmapDayData) -> Unit = {},
) {
    require(months.isNotEmpty()) { "At least one heatmap month is required" }
    require(months.all { month ->
        month.weekdayLabels.size == 7 && month.weeks.all { week -> week.size == 7 }
    }) { "Heatmap months require seven weekday labels and seven cells per week" }
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
        buckets = buckets,
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
                        description = "${day.label}: ${HeatmapPlatformFormatter.formatPercent(intensity)}",
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
    val buckets: HeatmapBuckets?,
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
            Box(Modifier.semantics(mergeDescendants = true) { heading() }) {
                config.header(title)
            }
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

// The slot and the drawn cell carry separate modifiers by design, so the single-`modifier`
// convention does not apply here.
@Suppress("ModifierParameter")
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
                .background(
                    config.colorForIntensity(
                        config.buckets?.colorIntensityFor(info.intensity) ?: info.intensity,
                    ),
                )
                .then(border),
            contentAlignment = Alignment.Center,
        ) { dayContent(day) }
    }
}

private fun gregorianDaysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) 29 else 28
    else -> throw IllegalArgumentException("Invalid month: $month")
}

private fun gregorianToEpochDay(year: Int, month: Int, day: Int): Long {
    val y = year.toLong() - if (month <= 2) 1L else 0L
    val era = if (y >= 0) y / 400 else (y - 399) / 400
    val yoe = y - era * 400
    val m = month.toLong()
    val doy = (153 * (m + if (m > 2) -3 else 9) + 2) / 5 + day - 1
    val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
    return era * 146097 + doe - 719468
}

private fun isoWeekdayFromEpochDay(epochDay: Long): Int {
    // 1970-01-01 was Thursday (ISO 4).
    return (((epochDay + 3) % 7 + 7) % 7).toInt() + 1
}
