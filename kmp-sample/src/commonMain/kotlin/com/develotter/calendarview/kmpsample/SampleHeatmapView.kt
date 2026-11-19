package com.develotter.calendarview.kmpsample

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import com.develotter.calendarview.compose.HeatmapOrientation
import com.develotter.calendarview.compose.HeatmapStyle
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
    val months = remember(options.calendar, options.locale, year, todayEpochDay) {
        runCatching {
            sampleHeatmapYear(
                options.calendar,
                year,
                todayEpochDay,
                hijriProvider,
                chineseStyle,
                options.locale,
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
    val orientation = when (options.orientation) {
        DemoHeatmapOrientation.WeeksAsColumns -> HeatmapOrientation.WeeksAsColumns
        DemoHeatmapOrientation.WeeksAsRows -> HeatmapOrientation.WeeksAsRows
    }
    val cellShape: Shape = when (options.cells) {
        DemoHeatmapCells.Square -> RectangleShape
        DemoHeatmapCells.Rounded -> RoundedCornerShape(4.dp)
    }

    CalendarYearHeatmap(
        months = months,
        onDayClick = { day ->
            onDetails("${day.label} · ${(day.intensity * 100f).roundToInt()}% activity")
        },
        modifier = Modifier.fillMaxWidth(),
        orientation = orientation,
        style = HeatmapStyle(
            cellSize = 20.dp,
            cellSpacing = 2.dp,
            cellShape = cellShape,
        ),
        colorForIntensity = { intensity -> lerp(baseColor, accent, intensity) },
    )
}

private fun DemoHeatmapPalette.accentColor(): Color = when (this) {
    DemoHeatmapPalette.Green -> Color(0xFF2E7D32)
    DemoHeatmapPalette.Blue -> Color(0xFF1F6FEB)
    DemoHeatmapPalette.Amber -> Color(0xFFB26A00)
}
