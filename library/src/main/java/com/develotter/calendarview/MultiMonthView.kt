package com.develotter.calendarview


import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.develotter.calendarview.adapter.BaseCalendarAdapter
import com.develotter.calendarview.adapter.MultiMonthDataAdapter
import com.develotter.calendarview.enums.TypeViewCalender
import com.develotter.calendarview.enums.TypeWeekShow
import com.develotter.calendarview.status.DayStatus
import com.develotter.calendarview.status.MonthStatus
import java.util.Locale

/**
 * Scrollable list of months rendered with consumer-supplied [ViewBinding]s.
 *
 * The view is normally declared in XML and configured programmatically:
 * ```
 * multiMonthView.addMonths(GregorianStatus::class.java, object : MonthSampleAdapter<...>(status) {
 *     // inflate and bind day/week/month/controller views
 * })
 * ```
 * [addMonths] creates a window of months around today and reuses the same adapter for every month.
 * Vertical/horizontal lists and paged variants are selected through
 * [com.develotter.calendarview.status.CalendarStatus].
 *
 * @param D binding for a day cell
 * @param W binding for a weekday label
 * @param M binding for a month header
 * @param SelectController binding for the selected-day summary
 */
@Suppress("UNCHECKED_CAST")
open class MultiMonthView<D : ViewBinding, W : ViewBinding, M : ViewBinding, SelectController : ViewBinding> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private lateinit var recyclerViewCalendarList: RecyclerView
    private lateinit var monthViewBinding: M

    private var snapHelper = PagerSnapHelper()
    private lateinit var inDayCellViewAdapter: BaseCalendarAdapter<D, W, M, SelectController>
    private lateinit var multiMonthDataAdapter: MultiMonthDataAdapter<D, W, M, SelectController>
    private lateinit var yearMonthList: MutableList<MonthStatus<*, DayStatus>>
    private var defaultHeightList = context.resources.getDimensionPixelSize(
        R.dimen.default_height
    )
    var inPosition = -1

    init {
        orientation = VERTICAL
    }




    /**
     * Rebuilds the calendar and starts rendering from the current month.
     *
     * All previous children are removed, so this is also the way to apply changes to
     * [com.develotter.calendarview.status.CalendarStatus]. [clazzMonth] must be a [MonthStatus]
     * subclass with a public constructor taking a single [Locale]; it is instantiated twice per
     * month (base and helper) to compute the month window.
     *
     * The window spans `getCountsMonthAfterAndBefore() * 2` months around today.
     */
    fun <T : MonthStatus<*, DayStatus>> addMonths(
        clazzMonth: Class<T>,
        fromInDayCellViewAdapter: BaseCalendarAdapter<D, W, M, SelectController>
    ) {
        removeAllViews()
        inDayCellViewAdapter = fromInDayCellViewAdapter


        yearMonthList = mutableListOf()

        val endMonth = inDayCellViewAdapter.calendarStatus.getCountsMonthAfterAndBefore() * 2
        for (i in 1..endMonth) {
            val base = clazzMonth.getDeclaredConstructor(Locale::class.java)
                .newInstance(inDayCellViewAdapter.calendarStatus.getLocalInUse())
            val helper = clazzMonth.getDeclaredConstructor(Locale::class.java)
                .newInstance(inDayCellViewAdapter.calendarStatus.getLocalInUse())
            base.setInstanceDay(
                helper.minusMonths(inDayCellViewAdapter.calendarStatus.getCountsMonthAfterAndBefore())
                    .plusMonths(i).getInstanceDay()
            )
            yearMonthList.add(base as T)
        }


        if (inDayCellViewAdapter.calendarStatus.getShowRowMonthName()) {
            monthViewBinding =
                inDayCellViewAdapter.onBindMonthView(yearMonthList[getThisMonthPosition()])
            addView(monthViewBinding.root)

        }

        if (inDayCellViewAdapter.calendarStatus.getShowRowWeekName() == TypeWeekShow.Fix) {
            var week = LinearLayout(context)
            week.orientation = HORIZONTAL

            for (day in 1..7) {
                val weeKBind =
                    inDayCellViewAdapter.onBindWeekView(
                        yearMonthList[getThisMonthPosition()].getTrueDayOfWeek(
                            day
                        )
                    )
                val layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f)
                weeKBind.root.layoutParams = layoutParams
                week.addView(weeKBind.root)

            }
            addView(week)
        }




        initRecyclerView()
        addView(recyclerViewCalendarList)



        if (inDayCellViewAdapter.calendarStatus.getShowCalendarController()) {
            addView(inDayCellViewAdapter.onBindControllerCalendar().root)
        }
        if (inDayCellViewAdapter.calendarStatus.getShowSelectedDayController()) {

            addView(inDayCellViewAdapter.select.root)
        }
        setAdapter()
//        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, defaultHeightList+context.resources.getDimensionPixelSize(
//            R.dimen.default_height_row)*2)
        scrollToThisMonth()
    }

    open fun getMonthInPosition(): MonthStatus<*, DayStatus> {
        return yearMonthList[inPosition]
    }

    open fun getThisMonthCaption(): String {
        return yearMonthList[getThisMonthPosition()].getThisMonthCaption()
    }

    open fun scrollToNextMonth() {
        val oldPosition =
            getPosition(recyclerViewCalendarList, recyclerViewCalendarList.scrollState)
        val newPosition = oldPosition + 1
        if (newPosition < yearMonthList.count()) {

            scrollToMonth(newPosition)

        }
    }

    open fun scrollToPreviousMonth() {
        val oldPosition =
            getPosition(recyclerViewCalendarList, recyclerViewCalendarList.scrollState)
        val newPosition = oldPosition - 1
        if (newPosition < yearMonthList.count()) {

            scrollToMonth(newPosition)

        }
    }

    open fun scrollToThisMonth() {
        inPosition = getThisMonthPosition()
        scrollToMonth(inPosition)
    }

    open fun scrollToToday() {
        scrollToThisMonth()
        inDayCellViewAdapter.onDayFocused(yearMonthList[getThisMonthPosition()])
    }

    open fun scrollToMonth(position: Int) {
        recyclerViewCalendarList.scrollToPosition(position)
        callMonthActivating(position)

    }

    open fun callMonthActivating(position: Int) {
        inDayCellViewAdapter.onMonthViewActive(yearMonthList[position])
        if (inDayCellViewAdapter.calendarStatus.getShowRowMonthName()) {
            inDayCellViewAdapter.onUpdateMonthView(yearMonthList[position], monthViewBinding)
        }
    }

    private fun initRecyclerView() {
        multiMonthDataAdapter = MultiMonthDataAdapter(inDayCellViewAdapter, yearMonthList)
        recyclerViewCalendarList = RecyclerView(context)
        recyclerViewCalendarList.setHasFixedSize(true)
        recyclerViewCalendarList.setItemViewCacheSize(inDayCellViewAdapter.calendarStatus.getCountsMonthAfterAndBefore() * 2)
        recyclerViewCalendarList.layoutParams =
            LayoutParams(LayoutParams.MATCH_PARENT, defaultHeightList)

        when (inDayCellViewAdapter.calendarStatus.getViewTypeSelected()) {

            TypeViewCalender.VERTICAL_LIST -> {
                recyclerViewCalendarList.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            }

            TypeViewCalender.HORIZONTAL_LIST -> {
                recyclerViewCalendarList.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }

            TypeViewCalender.VERTICAL_PAGER -> {
                pager(LinearLayoutManager.VERTICAL)
            }

            TypeViewCalender.HORIZONTAL_PAGER -> {
                pager(LinearLayoutManager.HORIZONTAL)
            }
        }


    }

    fun getMultiAdapter(): MultiMonthDataAdapter<D, W, M, SelectController> {
        return multiMonthDataAdapter
    }

    fun getMonthViewAdapter(): BaseCalendarAdapter<D, W, M, SelectController> {
        return inDayCellViewAdapter
    }

    private fun setAdapter() {
        recyclerViewCalendarList.adapter = multiMonthDataAdapter
    }

    open fun getThisMonthPosition(): Int {
        return yearMonthList.count().minus(1).div(2)
    }

    /**
     * Returns the snapped month position, or `-1` while the RecyclerView is not idle.
     *
     * Callers must handle `-1`: navigation invoked during a running animation intentionally does
     * nothing instead of skipping a month.
     */
    fun getPosition(recyclerView: RecyclerView, newState: Int): Int {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val snappedView = snapHelper.findSnapView(layoutManager)
            val position = (snappedView?.let { recyclerView.getChildAdapterPosition(it) } ?: -1)
            Log.i("snappedView", "onScrollStateChanged: $position")
            return position
        }
        return -1
    }

    /**
     * Switches the RecyclerView to paged scrolling and keeps [inPosition] in sync with the snapped
     * page. The [PagerSnapHelper] is attached only in this mode; plain list modes do not use it.
     */
    fun pager(direction: Int) {


        recyclerViewCalendarList.layoutManager = LinearLayoutManager(context, direction, false)

        snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerViewCalendarList)
        recyclerViewCalendarList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                inPosition = getPosition(recyclerView, newState)
                if (inPosition != -1) {
                    callMonthActivating(inPosition)

                }


            }
        })
    }


}

class MyGenericClassSafe<TF>() {


    // Requires a non-null default.  Use only if you *know* a non-null
    // is appropriate and safe.
    constructor(value: TF? = null) : this()

    // 2.b Provide a Class<T>
    constructor(value: TF? = null, clazz: Class<TF>) : this(value ?: createInstance(clazz))

    companion object {
        fun <TF> createInstance(clazz: Class<TF>): TF {
            return clazz.getDeclaredConstructor().newInstance()
        }
    }
}