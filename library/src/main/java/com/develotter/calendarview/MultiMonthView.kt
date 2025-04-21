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
import com.develotter.calendarview.enums.TypeArtCalender
import com.develotter.calendarview.enums.TypeViewCalender
import com.develotter.calendarview.enums.TypeWeekShow
import com.develotter.calendarview.georgian.GeorgianStatus
import com.develotter.calendarview.jalali.JalaliStatus
import com.develotter.calendarview.status.DayStatus
import com.develotter.calendarview.status.MonthStatus
import java.time.YearMonth

@Suppress("UNCHECKED_CAST")
open class MultiMonthView<Date,T: DayStatus<Date>,D:ViewBinding,W: ViewBinding,M: ViewBinding> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private lateinit var  recyclerViewCalendarList: RecyclerView
    private lateinit var  monthViewBinding: M

    private var snapHelper = PagerSnapHelper()
    private lateinit var inDayCellViewAdapter: BaseCalendarAdapter<Date,T,D, W,M>
    private lateinit var multiMonthDataAdapter: MultiMonthDataAdapter<Date,T,D, W,M>
    private lateinit var yearMonthList :MutableList<MonthStatus<*,T>>
    private var defaultHeightList= context.resources.getDimensionPixelSize(
    R.dimen.default_height)

    init {
        orientation =VERTICAL
    }
    fun  addMonths( fromInDayCellViewAdapter: BaseCalendarAdapter<Date,T,D, W,M>) {
        removeAllViews()
        inDayCellViewAdapter = fromInDayCellViewAdapter


        yearMonthList = mutableListOf()

        if(!   inDayCellViewAdapter. calendarStatus.getCustomCalendar() ) {


            val startMonth = when (   inDayCellViewAdapter. calendarStatus.getArtSelected()) {
                TypeArtCalender.Georgian -> {
                    YearMonth.now().minusMonths(   inDayCellViewAdapter. calendarStatus.getCountsMonthAfterAndBefore().toLong())
                }

                TypeArtCalender.JALALI -> {
                    JalaliStatus().minusMonths(   inDayCellViewAdapter. calendarStatus.getCountsMonthAfterAndBefore())
                }
            }

            val endMonth =    inDayCellViewAdapter. calendarStatus.getCountsMonthAfterAndBefore() * 2
            for (i in 1..endMonth) {
                val ys = when (   inDayCellViewAdapter. calendarStatus.getArtSelected()) {
                    TypeArtCalender.Georgian -> {
                        val ys = GeorgianStatus()
                        ys.setLocaleInUse(   inDayCellViewAdapter. calendarStatus.getLocalInUse())
                        if (startMonth is YearMonth)
                            ys.yearMonth = startMonth.plusMonths(i.toLong())
                        ys
                        //    yearMonthList.add(ys as MonthStatus<*, T>)
                    }

                    TypeArtCalender.JALALI -> {
                        val ys = JalaliStatus()
                        ys.setLocaleInUse(   inDayCellViewAdapter. calendarStatus.getLocalInUse())
                        if (startMonth is JalaliStatus)
                            ys.jalaliDateRow = startMonth.plusMonths(i).getNow()
                        ys
                        //    yearMonthList.add(ys as MonthStatus<*, T>)
                    }
                }
                yearMonthList.add(ys as MonthStatus<*, T>)


            }
        }else{
            yearMonthList =    inDayCellViewAdapter. calendarStatus.getListCustomCalendar()
        }

        if (inDayCellViewAdapter.calendarStatus.getShowRowMonthName()) {
            monthViewBinding= inDayCellViewAdapter.onBindMonthView(yearMonthList[getThisMonthPosition()])
            addView(monthViewBinding.root)

        }

        if (inDayCellViewAdapter.calendarStatus.getShowRowWeekName()== TypeWeekShow.Fix) {
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



        if(inDayCellViewAdapter.calendarStatus.getShowCalendarController()){
            addView(inDayCellViewAdapter.onBindControllerCalendar().root)
        }

//        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, defaultHeightList+context.resources.getDimensionPixelSize(
//            R.dimen.default_height_row)*2)
        scrollToThisMonth()
    }

    open  fun getThisMonthCaption():String {
        return  yearMonthList[getThisMonthPosition()].getThisMonthCaption()
    }

    open  fun scrollToNextMonth(){
        val oldPosition = getPosition(recyclerViewCalendarList,recyclerViewCalendarList.scrollState)
        val newPosition =oldPosition+1
        if (newPosition<yearMonthList.count()) {

            scrollToMonth(newPosition)

        }
    }
    open  fun scrollToPreviousMonth(){
        val oldPosition = getPosition(recyclerViewCalendarList,recyclerViewCalendarList.scrollState)
        val newPosition =oldPosition-1
        if (newPosition<yearMonthList.count()) {

            scrollToMonth(newPosition)

        }
    }
    open  fun scrollToThisMonth(){
        scrollToMonth(getThisMonthPosition())
    }
    open  fun scrollToToday(){
        scrollToThisMonth()
        inDayCellViewAdapter.onDayFocused(yearMonthList[getThisMonthPosition()])
    }
    open  fun scrollToMonth(position:Int){
        recyclerViewCalendarList.scrollToPosition(position)
        callMonthActivating(position)

    }
    open fun callMonthActivating(position:Int){
        if (inDayCellViewAdapter.calendarStatus.getShowRowMonthName()) {
            inDayCellViewAdapter.onMonthViewActive(yearMonthList[position],monthViewBinding)
        }
    }
    private fun  initRecyclerView(){
        multiMonthDataAdapter = MultiMonthDataAdapter(inDayCellViewAdapter, yearMonthList )
        recyclerViewCalendarList = RecyclerView(context)
        recyclerViewCalendarList.setHasFixedSize(true)
        recyclerViewCalendarList.setItemViewCacheSize(   inDayCellViewAdapter. calendarStatus.getCountsMonthAfterAndBefore()*2)
        recyclerViewCalendarList.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, defaultHeightList)

        when(   inDayCellViewAdapter. calendarStatus.getViewTypeSelected()){

            TypeViewCalender.VERTICAL_LIST ->{
                recyclerViewCalendarList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            }
            TypeViewCalender.HORIZONTAL_LIST ->{
                recyclerViewCalendarList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
            TypeViewCalender.VERTICAL_PAGER ->{
                pager( LinearLayoutManager.VERTICAL)
            }

            TypeViewCalender.HORIZONTAL_PAGER -> {
                pager( LinearLayoutManager.HORIZONTAL)
            }
        }



        recyclerViewCalendarList.adapter =multiMonthDataAdapter


    }
    open  fun getThisMonthPosition():Int{
        return yearMonthList.count().minus(1).div(2)
    }
    fun  getPosition(recyclerView: RecyclerView, newState: Int):Int{
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val snappedView = snapHelper.findSnapView(layoutManager)
            val position = (snappedView?.let { recyclerView.getChildAdapterPosition(it) } ?: -1)
            Log.i("snappedView", "onScrollStateChanged: $position")
            return position
        }
        return -1
    }
    fun  pager(direction: Int){





        recyclerViewCalendarList.layoutManager =  LinearLayoutManager(context, direction, false)

        snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerViewCalendarList)
        recyclerViewCalendarList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val inPosition =getPosition(recyclerView,newState)
                if ( inPosition!=-1){
                    callMonthActivating(inPosition)

                }


            }
        })
    }


}