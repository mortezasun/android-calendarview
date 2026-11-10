package com.develotter.calendarview.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
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

/** Calendar-agnostic heatmap day; [label] is the caller-formatted, localized date. */
data class HeatmapDayData(
    val epochDay: Long,
    val label: String,
    val intensity: Float,
)

/** Calendar-agnostic month block: seven weekday rows, one column per week. */
data class HeatmapMonthData(
    val title: String,
    val weekdayLabels: List<String>,
    val weeks: List<List<HeatmapDayData?>>,
)

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
    colorForIntensity: (Float) -> Color = { lerp(Color.Red, Color.Green, it) },
    dayContent: @Composable (HeatmapDay) -> Unit = {},
) {
    val data = heatmapQuarterData(year, quarter, intensities, firstDayOfWeek)
    val monthFormat = DateTimeFormatter.ofPattern("LLLL uuuu", locale)
    val dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)
    val percent = NumberFormat.getPercentInstance(locale)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("${data.start.format(monthFormat)} - ${data.end.format(monthFormat)}")
        Row {
            if (showWeekdayLabels) {
                Column {
                    repeat(7) { index ->
                        Box(Modifier.size(width = 64.dp, height = 48.dp), contentAlignment = Alignment.Center) {
                            Text(firstDayOfWeek.plus(index.toLong()).getDisplayName(TextStyle.SHORT_STANDALONE, locale))
                        }
                    }
                }
            }
            Row(Modifier.weight(1f).horizontalScroll(rememberScrollState())) {
                data.weeks.forEach { week ->
                    Column {
                        week.forEach { day ->
                            if (day == null) {
                                Box(Modifier.size(48.dp))
                            } else {
                                val description = "${day.date.format(dateFormat)}: ${percent.format(day.intensity)}"
                                Box(
                                    Modifier.size(48.dp)
                                        .background(colorForIntensity(day.intensity))
                                        .semantics { contentDescription = description }
                                        .clickable(role = Role.Button) { onDayClick(day.date) },
                                    contentAlignment = Alignment.Center,
                                ) { dayContent(day) }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Four native Compose quarters. The caller supplies vertical scrolling or a bounded host.
 * Data changes recompose the existing tree instead of appending duplicate quarter views.
 * This heatmap deliberately uses Gregorian LocalDate, matching the old heatmap API.
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
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        for (quarter in 1..4) {
            CalendarQuarterHeatmap(
                year, quarter, intensities, onDayClick, locale = locale,
                firstDayOfWeek = firstDayOfWeek, showWeekdayLabels = showWeekdayLabels,
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
 */
@Composable
fun CalendarYearHeatmap(
    months: List<HeatmapMonthData>,
    onDayClick: (HeatmapDayData) -> Unit,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.getDefault(),
    showWeekdayLabels: Boolean = true,
    colorForIntensity: (Float) -> Color = { lerp(Color.Red, Color.Green, it) },
    dayContent: @Composable (HeatmapDayData) -> Unit = {},
) {
    require(months.isNotEmpty()) { "At least one heatmap month is required" }
    require(months.all { month ->
        month.weekdayLabels.size == 7 && month.weeks.all { week -> week.size == 7 }
    }) { "Heatmap months require seven weekday labels and seven cells per week" }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        months.forEach { month ->
            CalendarHeatmapMonthBlock(
                month = month,
                onDayClick = onDayClick,
                locale = locale,
                showWeekdayLabels = showWeekdayLabels,
                colorForIntensity = colorForIntensity,
                dayContent = dayContent,
            )
        }
    }
}

@Composable
private fun CalendarHeatmapMonthBlock(
    month: HeatmapMonthData,
    onDayClick: (HeatmapDayData) -> Unit,
    locale: Locale,
    showWeekdayLabels: Boolean,
    colorForIntensity: (Float) -> Color,
    dayContent: @Composable (HeatmapDayData) -> Unit,
) {
    val percent = NumberFormat.getPercentInstance(locale)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(month.title)
        Row {
            if (showWeekdayLabels) {
                Column {
                    month.weekdayLabels.forEach { label ->
                        Box(
                            Modifier.size(width = 64.dp, height = 48.dp),
                            contentAlignment = Alignment.Center,
                        ) { Text(label) }
                    }
                }
            }
            Row(Modifier.weight(1f).horizontalScroll(rememberScrollState())) {
                month.weeks.forEach { week ->
                    Column {
                        week.forEach { day ->
                            if (day == null) {
                                Box(Modifier.size(48.dp))
                            } else {
                                val intensity = normalizeHeatmapIntensity(day.intensity)
                                val description = "${day.label}: ${percent.format(intensity)}"
                                Box(
                                    Modifier.size(48.dp)
                                        .background(colorForIntensity(intensity))
                                        .semantics { contentDescription = description }
                                        .clickable(role = Role.Button) { onDayClick(day) },
                                    contentAlignment = Alignment.Center,
                                ) { dayContent(day) }
                            }
                        }
                    }
                }
            }
        }
    }
}
