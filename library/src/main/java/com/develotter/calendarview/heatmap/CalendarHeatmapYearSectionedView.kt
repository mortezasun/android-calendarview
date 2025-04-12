package com.develotter.calendarview.heatmap

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import java.time.LocalDate

/**
 * A custom view that displays a year-long calendar heatmap, segmented into four quarters.
 *
 * This view extends [LinearLayout] and arranges four [QuarterHeatmapView] instances vertically,
 * each representing a quarter of the year.  It allows for displaying heatmap data across
 * an entire year and handling user interactions with individual days.
 *
 * @constructor Creates a new CalendarHeatmapYearSectionedView.
 * @param context The Context the view is running in, through which it can
 *        access the current theme, resources, etc.
 * @param attrs The attributes of the XML tag that is inflating the view.  This
 *        is where you supply parameters for initialisation of the view.
 */
class CalendarHeatmapYearSectionedView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    var currentYear = LocalDate.now().year
    init {
        orientation = VERTICAL
    }
    fun applyHeatmapData(year:Int,heatmapData: Map<LocalDate, Float>,listener: (LocalDate) -> Unit) {
        currentYear = year
        val q1View = QuarterHeatmapView(
            context,
            null,
            LocalDate.of(currentYear, 1, 1),
            LocalDate.of(currentYear, 3, 31)
        )
        val q2View = QuarterHeatmapView(
            context,
            null,
            LocalDate.of(currentYear, 4, 1),
            LocalDate.of(currentYear, 6, 30)
        )
        val q3View = QuarterHeatmapView(
            context,
            null,
            LocalDate.of(currentYear, 7, 1),
            LocalDate.of(currentYear, 9, 30)
        )
        val q4View = QuarterHeatmapView(
            context,
            null,
            LocalDate.of(currentYear, 10, 1),
            LocalDate.of(currentYear, 12, 31)
        )
        q1View.applyHeatmapData(heatmapData)
        q2View.applyHeatmapData(heatmapData)
        q3View.applyHeatmapData(heatmapData)
        q4View.applyHeatmapData(heatmapData)
        addView(q1View, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))
        addView(q2View, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))
        addView(q3View, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))
        addView(q4View, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))
        q1View.onDayClick = {date->
            listener(date)
        }
        q2View.onDayClick = {date->
            listener(date)
        }
        q3View.onDayClick = {date->
            listener(date)
        }
        q4View.onDayClick = {date->
            listener(date)
        }
    }
}