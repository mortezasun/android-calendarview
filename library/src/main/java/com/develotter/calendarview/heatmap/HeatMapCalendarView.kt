package com.develotter.calendarview.heatmap

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * A custom ViewGroup that displays a calendar-based heatmap.  Each day of the year is represented
 * by a square, and the color of the square indicates the intensity of some value associated with that
 * day (e.g., activity level, sales volume).
 *
 *  Key features:
 *  - Displays a heatmap for a single year, starting from January 1st.  The current year is used by default,
 *    but this could be made configurable in the future.
 *  - Weeks start on Monday, and the calendar grid includes any days before the start of the year to
 *    ensure that the first Monday of the year is in the correct column.  Similarly, the grid extends to
 *    the end of the last week of the year.
 *  - Displays weekday labels ("Mon", "Tue", etc.) along the left side and abbreviated month names at the top.
 *  - Uses a [DayHeatMapView] for each day, responsible for rendering the heat intensity color.
 *  - Provides an iterator to traverse the [DayHeatMapView] instances for the days within the year.
 *  - Supports applying heatmap data via the [applyHeatmapData] function, which maps dates to intensity values.
 *
 */
class HeatMapCalendarView(context: Context, attrs: AttributeSet?) : ViewGroup(context, attrs) {

    // Mapping from date to DayView
    private val dayViewMap = mutableMapOf<LocalDate, DayHeatMapView>()
    // Lists for month and weekday labels
    private val monthLabels = mutableListOf<TextView>()
    private val dayLabels = mutableListOf<TextView>()

    // Configuration: The current year is used here; alternatively, the year value can be made parameterizable.
     val year: Int = LocalDate.now().year
    private val startDate = LocalDate.of(year, 1, 1)
    private val endDate = LocalDate.of(year, 12, 31)

    // To calculate the Style grid:
    // We define the start of the week as Monday.
    private val firstGridDate = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    private val lastGridDate = endDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    private val totalDays = ChronoUnit.DAYS.between(firstGridDate, lastGridDate).toInt() + 1
    private val weeks = totalDays / 7

    init {
        // Create a DayView for each date in the year and add it to the ViewGroup.
        var date = startDate
        while (!date.isAfter(endDate)) {
            val dayHeatMapView = DayHeatMapView(context)

            dayViewMap[date] = dayHeatMapView
            addView(dayHeatMapView)
            date = date.plusDays(1)
        }

        // Create weekday labels (first column)
        // We use the short form here (e.g., "Mon", "Tue", ...)
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        for (name in dayNames) {
            val tv = TextView(context).apply {
                text = name
                textSize = 10f
                gravity = Gravity.CENTER
            }
            dayLabels.add(tv)
            addView(tv)
        }

        // Create month labels (for each month in a list)

        for (month in 1..12) {
            val tv = TextView(context).apply {
                text = Month.of(month).getDisplayName(TextStyle.SHORT, Locale("en","US"))
                textSize = 5f
                gravity = Gravity.CENTER
            }
            monthLabels.add(tv)
            addView(tv)
        }
    }

    // Internal iterator that only iterates the DayViews that are in our year range.
    inner class DayViewIterator : Iterator<DayHeatMapView> {
        private val dates = dayViewMap.keys.sorted()
        private var index = 0
        override fun hasNext() = index < dates.size
        override fun next(): DayHeatMapView {
            if (!hasNext()) throw NoSuchElementException()
            return dayViewMap[dates[index++]]!!
        }
    }

    fun iterator(): Iterator<DayHeatMapView> = DayViewIterator()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Total grid:
        // - Columns: weeks (days) + 1 (first column for weekday labels)
        // - Rows: 7 (days of the week) + 1 (first row for month names)
        val totalColumns = weeks + 1
        val totalRows = 7 + 1
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val cellSize = width / totalColumns
        val height = cellSize * totalRows
        setMeasuredDimension(width, height)

        // All children (DayViews and TextViews) get the cell size.
        val cellMeasureSpec = MeasureSpec.makeMeasureSpec(cellSize, MeasureSpec.EXACTLY)
        for (i in 0 until childCount) {
            getChildAt(i).measure(cellMeasureSpec, cellMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val totalColumns = weeks + 1
        val cellSize = (r - l) / totalColumns

        // Layout of the month labels in the top row (row 0)
        // Here we calculate the start and end week index for each month
        for (month in 1..12) {
            val monthStart = LocalDate.of(year, month, 1)
            val diff = ChronoUnit.DAYS.between(firstGridDate, monthStart).toInt()
            val weekCol = diff / 7 + 1 // +1 because column 0 is reserved for tag labels

            // Determine the last day of the month
            val monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth())
            val diffEnd = ChronoUnit.DAYS.between(firstGridDate, monthEnd).toInt()
            val weekColEnd = diffEnd / 7 + 1

            val left = weekCol * cellSize
            val right = (weekColEnd + 1) * cellSize  // +1 to cover the entire month
            val tv = monthLabels[month - 1]
            tv.layout(left, 0, right, cellSize)  // in the top row
        }

        // Layout of the weekday labels in the first column (column 0, rows 1 to 7)
        for (i in 0 until 7) {
            val tv = dayLabels[i]
            val top = (i + 1) * cellSize
            tv.layout(0, top, cellSize, top + cellSize)
        }

        // Layout of the DayViews in the remaining grid (columns 1 to weeks, rows 1 to 7)
        // We iterate over the entire grid. The date is calculated as:
        // cellDate = firstGridDate + ((col-1)*7 + row)
        for (row in 0 until 7) {
            for (col in 1 until totalColumns) {
                val cellDate = firstGridDate.plusDays(((col - 1) * 7 + row).toLong())
                val dayView = dayViewMap[cellDate]
                if (dayView != null) {
                    val left = col * cellSize
                    val top = (row + 1) * cellSize
                    dayView.layout(left, top, left + cellSize, top + cellSize)
                }
            }
        }
    }

    // Transfer the heatmap data: The keys are LocalDate, the values are the intensity (0.0 to 1.0).
    fun applyHeatmapData(heatmapData: Map<LocalDate, Float>) {
        dayViewMap.forEach { (date, dayView) ->
            dayView.heatIntensity = heatmapData[date] ?: 0f
            dayView.invalidate()
        }
    }
}
