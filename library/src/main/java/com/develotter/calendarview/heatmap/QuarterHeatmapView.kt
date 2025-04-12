package com.develotter.calendarview.heatmap

import android.annotation.SuppressLint
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
 * A custom view that displays a heatmap for a given quarter of a year.
 * The heatmap visualizes data intensity for each day within the specified date range.
 *
 * @constructor Creates a new QuarterHeatmapView.
 * @param context The Context the view is running in, through which it can
 *        access the current theme, resources, etc.
 * @param attrs The attributes of the XML tag that is inflating the view.
 * @param startDate The first date of the quarter to be displayed.
 * @param endDate The last date of the quarter to be displayed.
 * @param hideWeekNames When true, the weekday labels in the first column are hidden. Defaults to false.
 * @param monthLabelSpacingDp Additional spacing in dp for month labels. Defaults to 8dp.
 */
@SuppressLint("ViewConstructor")
class QuarterHeatmapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    private val startDate: LocalDate,
    private val endDate: LocalDate,
    // When true, weekday labels (first column) are hidden
     var hideWeekNames: Boolean = false,
    // Optional: additional spacing for month labels (if needed)
    private val monthLabelSpacingDp: Float = 8f

) : ViewGroup(context, attrs) {

    // Mapping from date to its corresponding DayView.
    private val dayHeatMapViewMap = mutableMapOf<LocalDate, DayHeatMapView>()

    // Lists for month and weekday labels.
    private val monthLabels = mutableListOf<TextView>()
    private val weekdayLabels = mutableListOf<TextView>()

    // Listener for day clicks.
    var onDayClick: ((LocalDate) -> Unit)? = null

    // Calculate grid boundaries.
    private val firstGridDate: LocalDate = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    private val lastGridDate: LocalDate = endDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    private val totalDays: Int = ChronoUnit.DAYS.between(firstGridDate, lastGridDate).toInt() + 1
    // Number of weeks in the grid.
    private val weeks: Int = totalDays / 7



    init {
        // Create DayViews for every day in the quarter.
        var date = startDate
        while (!date.isAfter(endDate)) {
            val dayHeatMapView = DayHeatMapView(context)
            // Set click listener for each day.
            dayHeatMapView.date=date
            dayHeatMapView.setOnClickListener {
                onDayClick?.invoke((it as DayHeatMapView).date)
            }
            dayHeatMapViewMap[date] = dayHeatMapView
            addView(dayHeatMapView)
            date = date.plusDays(1)
        }

        // Create month labels for all months included in the quarter.
        val months = mutableSetOf<Int>()
        var tmpDate = startDate
        while (!tmpDate.isAfter(endDate)) {
            months.add(tmpDate.monthValue)
            tmpDate = tmpDate.plusDays(1)
        }
        months.toList().sorted().forEach { month ->
            val tv = TextView(context).apply {
                text = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                textSize = 12f
                gravity = Gravity.CENTER
            }
            monthLabels.add(tv)
            addView(tv)
        }

        // Only create weekday labels if they are not hidden.
        if (!hideWeekNames) {
            val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            dayNames.forEach { name ->
                val tv = TextView(context).apply {
                    text = name
                    textSize = 12f
                    gravity = Gravity.CENTER
                }
                weekdayLabels.add(tv)
                addView(tv)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val availableWidth = MeasureSpec.getSize(widthMeasureSpec)

        // If week names are hidden, the number of columns equals the number of weeks.
        // Otherwise, reserve the first column for the weekday labels.
        val totalColumns = if (hideWeekNames) weeks else weeks + 1

        // Header rows for month labels.
        val headerRows = if (monthLabels.isNotEmpty()) 1 else 0
        // Total rows: header for months + 7 rows for days.
        val totalRows = headerRows + 7

        val cellSize = availableWidth / totalColumns
        val totalHeight = cellSize * totalRows
        setMeasuredDimension(availableWidth, totalHeight)

        // Measure month labels equally across the full width.
        val monthLabelCount = monthLabels.size
        val monthLabelWidthSpec = MeasureSpec.makeMeasureSpec(availableWidth / monthLabelCount, MeasureSpec.EXACTLY)
        val monthLabelHeightSpec = MeasureSpec.makeMeasureSpec(cellSize, MeasureSpec.EXACTLY)
        monthLabels.forEach { it.measure(monthLabelWidthSpec, monthLabelHeightSpec) }

        // Measure weekday labels (if shown) and DayViews using cellSize.
        val cellMeasureSpec = MeasureSpec.makeMeasureSpec(cellSize, MeasureSpec.EXACTLY)
        weekdayLabels.forEach { it.measure(cellMeasureSpec, cellMeasureSpec) }
        dayHeatMapViewMap.values.forEach { it.measure(cellMeasureSpec, cellMeasureSpec) }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val availableWidth = r - l
        // Adjust totalColumns based on hideWeekNames flag.
        val totalColumns = if (hideWeekNames) weeks else weeks + 1
        val headerRows = if (monthLabels.isNotEmpty()) 1 else 0
        val cellSize = availableWidth / totalColumns

        // Layout month labels evenly across the entire width.
        val monthLabelCount = monthLabels.size
        val monthLabelWidth = availableWidth / monthLabelCount
        monthLabels.forEachIndexed { index, tv ->
            val left = index * monthLabelWidth
            tv.layout(left, 0, left + monthLabelWidth, cellSize)
        }

        // If weekday labels are shown, layout them in the first column.
        if (!hideWeekNames) {
            weekdayLabels.forEachIndexed { i, tv ->
                val top = (i + headerRows) * cellSize
                tv.layout(0, top, cellSize, top + cellSize)
            }
        }

        // Layout DayViews.
        // Determine starting column for DayViews:
        // If weekday labels are visible, DayViews start at column 1; otherwise, they start at column 0.
        val startCol = if (hideWeekNames) 0 else 1
        for (row in headerRows until (7 + headerRows)) {
            for (col in startCol until totalColumns) {
                // Calculate date from grid position.
                val dayIndex = ((col - startCol) * 7) + (row - headerRows)
                val cellDate = firstGridDate.plusDays(dayIndex.toLong())
                val dayView = dayHeatMapViewMap[cellDate]
                if (dayView != null) {
                    val left = col * cellSize
                    val top = row * cellSize
                    dayView.layout(left, top, left + cellSize, top + cellSize)
                }
            }
        }
    }

    // Apply heatmap data: keys are LocalDate, values are intensity (0.0 to 1.0).
    fun applyHeatmapData(heatmapData: Map<LocalDate, Float>) {
        dayHeatMapViewMap.forEach { (date, dayView) ->
            dayView.addHeatIntensity(heatmapData[date] ?: 0f)
            dayView.invalidate()
        }
    }
}

