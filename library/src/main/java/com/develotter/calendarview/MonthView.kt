package com.develotter.calendarview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import androidx.viewbinding.ViewBinding
import com.develotter.calendarview.adapter.BaseCalendarAdapter
import com.develotter.calendarview.enums.TypeWeekShow
import com.develotter.calendarview.status.DayStatus
import com.develotter.calendarview.status.MonthStatus
import kotlin.math.abs
import kotlin.math.min


class MonthView<D : ViewBinding, W : ViewBinding, M : ViewBinding,SelectController : ViewBinding> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var numColumns = 7




    private lateinit var inDayCellViewAdapter: BaseCalendarAdapter< D, W, M,SelectController>

    private var dayPreStatusList: MutableList<DayStatus> = mutableListOf()
    private var dayNextStatusList: MutableList<DayStatus> = mutableListOf()
    private var dayStatusList: MutableList<DayStatus> = mutableListOf()


    init {

        if (attrs != null) {
            context.withStyledAttributes(attrs, R.styleable.CustomMonthView) {
                numColumns = getInt(R.styleable.CustomMonthView_numColumns, 7)

            }
        }
        if (numColumns < 1) {
            throw IllegalArgumentException("numColumns must be at least 1")
        }
        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )

    }



    fun setUp(
        thisInDayCellViewAdapter: BaseCalendarAdapter< D, W, M,SelectController>,
        monthStatus: MonthStatus<*, DayStatus>
    ) {

        if (dayStatusList.isEmpty()) {
            inDayCellViewAdapter = thisInDayCellViewAdapter

            dayStatusList = mutableListOf()



            if (inDayCellViewAdapter.calendarStatus.getShowRowWeekName()== TypeWeekShow.First) {
                for (day in 1..7) {
                    addView(inDayCellViewAdapter.onBindWeekView(monthStatus.getTrueDayOfWeek(day)).root)
                }
            }

            for (day in 1..monthStatus.atEndOfMonth()) {

                val dayStatus = monthStatus.setOnCreateDayStatus(day)

                inDayCellViewAdapter.dayStatusListSelectedBySingleSelect.find { it.localDate == dayStatus.localDate }.let {
                    if (it != null) {
                        dayStatus.isChecked = true
                    }
                }


                dayStatusList.add(dayStatus)
                val rows =thisInDayCellViewAdapter.onCreateDayView(dayStatus, day)
                if (day == 1 && dayStatus.localDate.dayOfWeek != monthStatus.getStartDayOfWeek()) {
                    val lastMonth = monthStatus.minusMonths(1)

                    val getTrueIntDayOfWeek = monthStatus.getTrueIntDayOfWeek(dayStatus.localDate.dayOfWeek) - monthStatus.getTrueIntDayOfWeek(monthStatus.getStartDayOfWeek())
                    val atEndOfMonth = lastMonth.atEndOfMonth()
                    val lastWeek = atEndOfMonth - getTrueIntDayOfWeek
                    for (week in lastWeek until atEndOfMonth) {
                        if (thisInDayCellViewAdapter.calendarStatus.getShowLastMonth()) {
                            val dayStatusLastMonth = lastMonth.setOnCreateDayStatus(week)

                            dayPreStatusList.add(dayStatusLastMonth)
                            val rowsPre =
                                inDayCellViewAdapter.onBindLastMonthDayView(dayStatusLastMonth)
                            addView(rowsPre.root.apply {
                                tag = week
                            })
                        } else {
                            addView(View(context))
                        }

                    }
                }
                addView(rows.root)
                if (day == monthStatus.atEndOfMonth() && dayStatus.localDate.dayOfWeek != monthStatus.getEndDayOfWeek()) {
                    val nextMonth = monthStatus.plusMonths(1)

                    val trueIntDayOfWeekThis =
                        monthStatus.getTrueIntDayOfWeek(dayStatus.localDate.dayOfWeek)
                    val trueIntDayOfWeekEnd =
                        monthStatus.getTrueIntDayOfWeek(monthStatus.getEndDayOfWeek())
                    val nextWeek = abs(trueIntDayOfWeekThis - trueIntDayOfWeekEnd)

                    for (week in 1..nextWeek) {
                        if (thisInDayCellViewAdapter.calendarStatus.getShowNextMonth()) {
                            val dayStatusNextMonth = nextMonth.setOnCreateDayStatus(week)

                            dayNextStatusList.add(dayStatusNextMonth)
                            val rowsPre =
                                inDayCellViewAdapter.onBindNextMonthDayView(dayStatusNextMonth)
                            addView(rowsPre.root.apply {
                                tag = week
                            })
                        }else{
                            addView(View(context))
                        }

                    }
                }


            }

        }


    }



    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        // Determine total width
        val totalWidth = if (widthMode == MeasureSpec.EXACTLY) widthSize else widthSize

        // Calculate cell width based on number of columns
        val cellWidth = totalWidth / numColumns

        // Determine number of rows based on child count
        val childCount = childCount
        val numRows = if (childCount > 0) (childCount + numColumns - 1) / numColumns else 0

        // Determine cell height based on height spec
        val cellHeight: Int = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize / numRows
            MeasureSpec.AT_MOST -> if (numRows > 0) heightSize / numRows else 0
            else -> { // UNSPECIFIED
                val defaultHeightDp = 50 // Default height in dp
                (defaultHeightDp * resources.displayMetrics.density).toInt()
            }
        }

        // Measure each child with exact cell dimensions
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val childWidthSpec = MeasureSpec.makeMeasureSpec(cellWidth, MeasureSpec.EXACTLY)
            val childHeightSpec = MeasureSpec.makeMeasureSpec(cellHeight, MeasureSpec.EXACTLY)
            child.measure(childWidthSpec, childHeightSpec)
        }

        // Set measured dimensions for the layout
        val measuredWidth = totalWidth
        val measuredHeight = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            else -> {
                val calculatedHeight = numRows * cellHeight
                if (heightMode == MeasureSpec.AT_MOST) min(
                    calculatedHeight,
                    heightSize
                ) else calculatedHeight
            }
        }
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val childCount = childCount
        val cellWidth = measuredWidth / numColumns
        val cellHeight = measuredHeight / ((childCount + numColumns - 1) / numColumns)

        var row = 0
        var col = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)

            // Calculate the left position of the child
            val childLeft = if (layoutDirection == LAYOUT_DIRECTION_RTL) {
                // In RTL layout, the first column is on the right
                measuredWidth - (col + 1) * cellWidth
            } else {
                // In LTR layout, the first column is on the left
                col * cellWidth
            }

            val childTop = row * cellHeight
            val childRight = childLeft + child.measuredWidth
            val childBottom = childTop + child.measuredHeight

            // Position the child view in its grid cell,
            // with adjustments for RTL layout if necessary

            child.layout(childLeft, childTop, childRight, childBottom)

            // Move to next column, or next row if column limit reached
            col++
            if (col == numColumns) {
                col = 0
                row++
            }
        }
    }


}