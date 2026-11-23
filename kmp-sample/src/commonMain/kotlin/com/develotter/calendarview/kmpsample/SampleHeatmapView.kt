package com.develotter.calendarview.kmpsample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.calendars.hijri.OffsetHijriDateProvider
import com.develotter.calendarview.compose.CalendarYearHeatmap
import com.develotter.calendarview.compose.HeatmapBuckets
import com.develotter.calendarview.compose.HeatmapLegend
import com.develotter.calendarview.compose.HeatmapOrientation
import com.develotter.calendarview.compose.HeatmapStyle
import com.develotter.calendarview.locale.CalendarLocale
import kotlin.math.roundToInt

/**
 * One heatmap year of the selected calendar. Cells use the deterministic demo intensity and the
 * chosen palette; tapping a cell reports its label and intensity.
 */
@Composable
internal fun SampleHeatmap(
    options: HeatmapOptions,
    onDetails: (String) -> Unit,
    todayEpochDay: Long,
) {
    val hijriProvider = remember { OffsetHijriDateProvider(0) }
    val chineseStyle = ChineseTextStyle.English
    val year = remember(options.calendar, todayEpochDay) {
        sampleYear(options.calendar, todayEpochDay, hijriProvider)
    }
    val months = remember(options.calendar, options.locale, options.firstDayOfWeek, year, todayEpochDay) {
        runCatching {
            sampleHeatmapYear(
                options.calendar,
                year,
                todayEpochDay,
                hijriProvider,
                chineseStyle,
                options.locale,
                options.firstDayOfWeek,
            )
        }.getOrDefault(emptyList())
    }
    if (months.isEmpty()) {
        androidx.compose.material3.Text(
            "The heatmap year is outside the supported range of ${options.calendar.label}.",
        )
        return
    }

    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val accent = options.palette.accentColor()
    val language = options.locale ?: CalendarLocale.English
    // One stepped scale drives both the cells and the legend below them.
    val buckets = remember { HeatmapBuckets(listOf(0.25f, 0.5f, 0.75f)) }
    val orientation = when (options.orientation) {
        DemoHeatmapOrientation.WeeksAsColumns -> HeatmapOrientation.WeeksAsColumns
        DemoHeatmapOrientation.WeeksAsRows -> HeatmapOrientation.WeeksAsRows
    }
    val cellShape: Shape = when (options.cells) {
        DemoHeatmapCells.Square -> RectangleShape
        DemoHeatmapCells.Rounded -> RoundedCornerShape(4.dp)
    }

    val heatmapStyle = HeatmapStyle(
        cellSize = 20.dp,
        cellSpacing = 2.dp,
        cellShape = cellShape,
    )
    val colorForIntensity: (Float) -> Color = { intensity ->
        lerp(baseColor, accent, intensity)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CalendarYearHeatmap(
            months = months,
            onDayClick = { day ->
                onDetails("${day.label} · ${(day.intensity * 100f).roundToInt()}% activity")
            },
            modifier = Modifier.fillMaxWidth(),
            orientation = orientation,
            style = heatmapStyle,
            buckets = buckets,
            colorForIntensity = colorForIntensity,
        )
        HeatmapLegend(
            buckets = buckets,
            style = heatmapStyle,
            colorForIntensity = colorForIntensity,
            label = { level -> Text(SampleStrings.heatmapLevel(language, level + 1)) },
            contentDescription = { level -> SampleStrings.heatmapLevel(language, level + 1) },
        )
    }
}

private fun DemoHeatmapPalette.accentColor(): Color = when (this) {
    DemoHeatmapPalette.Green -> Color(0xFF2E7D32)
    DemoHeatmapPalette.Blue -> Color(0xFF1F6FEB)
    DemoHeatmapPalette.Amber -> Color(0xFFB26A00)
}
