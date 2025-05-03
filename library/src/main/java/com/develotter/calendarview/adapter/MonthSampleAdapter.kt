package com.develotter.calendarview.adapter

import androidx.core.view.children
import androidx.viewbinding.ViewBinding
import com.develotter.calendarview.enums.TypeSelectDay
import com.develotter.calendarview.status.CalendarStatus
import com.develotter.calendarview.status.DayStatus
import com.develotter.calendarview.status.SelectRangeDayStatus
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.time.format.TextStyle

abstract class MonthSampleAdapter<Day : ViewBinding, Week : ViewBinding, Month : ViewBinding, SelectController : ViewBinding>(
    calendarStatus: CalendarStatus,
    dayStatusListAdapter: MutableMap<String, DayStatus> = mutableMapOf(),
    dayStatusListSelectedByMultipleSelect: MutableList<DayStatus> = mutableListOf(),
    dayStatusListSelectedBySingleSelect: MutableList<DayStatus> = mutableListOf(),
    dayStatusListSelectedRange: MutableList<DayStatus> = mutableListOf(),
    dayStatusListSelectedViewBinding: MutableMap<String, Day> = mutableMapOf()
) :
    BaseCalendarAdapter<Day, Week, Month, SelectController>(
        calendarStatus,
        dayStatusListAdapter,
        dayStatusListSelectedByMultipleSelect,
        dayStatusListSelectedBySingleSelect,
        dayStatusListSelectedRange,
        dayStatusListSelectedViewBinding
    ) {

    override fun onReset() {}

    override fun onCreateDayView(dayStatus: DayStatus, day: Int): Day {

        dayStatusListAdapter[getKeyStringForViewBinding(dayStatus)] = (dayStatus)
        val rows = onBindDayView(dayStatus)

        dayStatusListSelectedViewBinding.put(getKeyStringForViewBinding(dayStatus), rows)
        rows.root.setOnClickListener {
            onHandleClickRowDay(dayStatus, rows, false)
        }
        rows.root.setOnLongClickListener {
            onHandleClickRowDay(dayStatus, rows, true)
            false
        }
        return rows
    }

    open fun getKeyStringForViewBinding(dayStatus: DayStatus): String {
        var monthString = ""
        var dayString = ""
        var yearString = dayStatus.onGetYearInt()
        var monthInt = dayStatus.onGetMonthInt()
        var dayInt = dayStatus.onGetDayInt()
        monthString = if (monthInt < 10) {
            "0$monthInt"
        } else {
            "$monthInt"
        }
        dayString = if (dayInt < 10) {
            "0$dayInt"
        } else {
            "$dayInt"
        }
        return "${yearString}${monthString}${dayString}"
    }

    override fun onNoneClickRowDay(dayStatus: DayStatus, viewbinding: Day, isLong: Boolean) {

    }

    override fun onSingleClickRowDay(dayStatus: DayStatus, viewbinding: Day, isLong: Boolean) {
        if (dayStatusListSelectedBySingleSelect.count() > 0) {
            var another = getKeyStringForViewBinding(dayStatusListSelectedBySingleSelect[0])
            var now = getKeyStringForViewBinding(dayStatus)
            if (another != now) {
                var preSelected = dayStatusListSelectedBySingleSelect[0]
                clickSingleDay(dayStatusListSelectedBySingleSelect, preSelected)
                dayStatusListSelectedViewBinding[another]?.let {
                    onDayItemClick(
                        preSelected,
                        it
                    )
                }
            }

        }
        clickSingleDay(dayStatusListSelectedBySingleSelect, dayStatus)
        onDayItemClick(dayStatus, viewbinding)
    }

    override fun onMultipleClickRowDay(dayStatus: DayStatus, viewbinding: Day, isLong: Boolean) {

        clickSingleDay(dayStatusListSelectedByMultipleSelect, dayStatus)
        onDayItemClick(dayStatus, viewbinding)
    }

    fun sortRangeListBase() {
        dayStatusListSelectedRange.sortWith(compareBy { getKeyStringForViewBinding(it).toInt() })
    }

    override fun onRangeClickRowDay(dayStatus: DayStatus, viewbinding: Day, isLong: Boolean) {
        sortRangeListBase()

        if (dayStatusListSelectedRange.count() == 0) {
            dayStatus.isChecked = true
            dayStatus.selectRangeDayStatus = SelectRangeDayStatus.AsStartOrEnd
            dayStatusListSelectedRange.add(dayStatus)
            onDayItemClick(dayStatus, viewbinding)


        } else if (dayStatusListSelectedRange.contains(dayStatus)) {
            if (dayStatusListSelectedRange.count() == 1) {
                dayStatus.isChecked = false
                dayStatus.selectRangeDayStatus = SelectRangeDayStatus.Nothing
                dayStatusListSelectedRange.remove(dayStatus)
                onDayItemClick(dayStatus, viewbinding)
                dayStatusListSelectedRange.clear()
                dayStatusListSelectedByMultipleSelect.clear()

            } else if (dayStatusListSelectedRange.count() == 2) {
                dayStatus.isChecked = false
                dayStatus.selectRangeDayStatus = SelectRangeDayStatus.Nothing
                onDayItemClick(dayStatus, viewbinding)
                dayStatusListSelectedRange.remove(dayStatus)
                dayStatusListSelectedRange[0].isChecked = true
                dayStatusListSelectedRange[0].selectRangeDayStatus =
                    SelectRangeDayStatus.AsStartOrEnd
                dayStatusListAdapter[getKeyStringForViewBinding(dayStatusListSelectedRange[0])] =
                    dayStatusListSelectedRange[0]
                dayStatusListSelectedViewBinding[getKeyStringForViewBinding(
                    dayStatusListSelectedRange[0]
                )]
                    ?.let { viewbinding ->
                        onDayItemClick(
                            dayStatusListSelectedRange[0],
                            viewbinding
                        )
                    }
                if (dayStatusListSelectedByMultipleSelect.isNotEmpty()) {
                    val oldList = dayStatusListSelectedByMultipleSelect.toMutableList()
                    for (preDay in oldList) {
                        var fe: Day? =
                            dayStatusListSelectedViewBinding[getKeyStringForViewBinding(preDay)]
                        fe?.let { viewbinding ->
                            if (preDay.selectRangeDayStatus != SelectRangeDayStatus.AsStartOrEnd) {

                                preDay.selectRangeDayStatus = SelectRangeDayStatus.Nothing
                                preDay.isChecked = false
                                dayStatusListSelectedByMultipleSelect.remove(preDay)
                                onDayItemClick(
                                    preDay,
                                    viewbinding
                                )
                            }
                        }
                    }
                    dayStatusListSelectedByMultipleSelect.clear()

                }
            }


        } else if (!dayStatusListSelectedRange.contains(dayStatus)) {
            if (dayStatusListSelectedRange.count() == 1) {
                dayStatus.isChecked = true
                dayStatusListSelectedRange.add(dayStatus)

                getPeriodByRange { startDay, endDay ->
                    var startDayKey = getKeyStringForViewBinding(startDay).toInt()
                    var endDayKey = getKeyStringForViewBinding(endDay).toInt()
                    for (preDay in dayStatusListAdapter.keys) {
                        var preDayKey = (preDay).toInt()
                        if (endDayKey > preDayKey && startDayKey < preDayKey) {

                            dayStatusListAdapter[preDay]?.let {
                                it.selectRangeDayStatus = SelectRangeDayStatus.Nothing
                                it.isChecked = true
                                dayStatusListSelectedByMultipleSelect.add(it)
                            }

                        }
                    }
                    activeRangeView {
                        var preDayKey = (it).toInt()
                        endDayKey >= preDayKey && startDayKey <= preDayKey
                    }
                }


            } else if (dayStatusListSelectedRange.count() == 2) {
                var startDate = dayStatusListSelectedRange[0]
                var endDate = dayStatusListSelectedRange[1]


                val thirdDate = getKeyStringForViewBinding(dayStatus).toInt()
                if (thirdDate < getKeyStringForViewBinding(startDate).toInt()) {

                    dayStatusListSelectedRange[0].isChecked = true
                    dayStatusListSelectedRange[0].selectRangeDayStatus =
                        SelectRangeDayStatus.Nothing
                    dayStatusListAdapter[getKeyStringForViewBinding(dayStatusListSelectedRange[0])] =
                        dayStatusListSelectedRange[0]

                    dayStatus.isChecked = true
                    dayStatus.selectRangeDayStatus = SelectRangeDayStatus.AsStart
                    dayStatusListSelectedRange[0] = (dayStatus)

                    var startDayKey = getKeyStringForViewBinding(startDate).toInt()
                    var thirdDateKey = getKeyStringForViewBinding(dayStatus).toInt()
                    for (preDay in dayStatusListAdapter.keys) {

                        var preDayKey = (preDay).toInt()
                        if (startDayKey >= preDayKey && thirdDateKey < preDayKey) {
                            dayStatusListAdapter[preDay]?.let {
                                it.selectRangeDayStatus = SelectRangeDayStatus.Nothing
                                it.isChecked = true
                                dayStatusListSelectedByMultipleSelect.add(it)
                            }
                        }
                    }


                } else if (thirdDate > getKeyStringForViewBinding(endDate).toInt()) {

                    dayStatusListSelectedRange[1].isChecked = true
                    dayStatusListSelectedRange[1].selectRangeDayStatus =
                        SelectRangeDayStatus.Nothing
                    dayStatusListAdapter[getKeyStringForViewBinding(dayStatusListSelectedRange[1])] =
                        dayStatusListSelectedRange[1]

                    dayStatus.isChecked = true
                    dayStatus.selectRangeDayStatus = SelectRangeDayStatus.AsEnd
                    dayStatusListSelectedRange[1] = (dayStatus)


                    var endDayKey = getKeyStringForViewBinding(endDate).toInt()
                    var thirdDateKey = getKeyStringForViewBinding(dayStatus).toInt()
                    for (preDay in dayStatusListAdapter.keys) {
                        var preDayKey = (preDay).toInt()
                        if (endDayKey <= preDayKey && thirdDateKey > preDayKey) {
                            dayStatusListAdapter[preDay]?.let {
                                it.selectRangeDayStatus = SelectRangeDayStatus.Nothing
                                it.isChecked = true
                                dayStatusListSelectedByMultipleSelect.add(it)
                            }
                        }
                    }


                } else if (thirdDate < getKeyStringForViewBinding(endDate).toInt() && thirdDate > getKeyStringForViewBinding(
                        startDate
                    ).toInt()
                ) {


                    dayStatusListSelectedRange[1].isChecked = false
                    dayStatusListSelectedRange[1].selectRangeDayStatus =
                        SelectRangeDayStatus.Nothing
                    dayStatusListAdapter[getKeyStringForViewBinding(dayStatusListSelectedRange[1])] =
                        dayStatusListSelectedRange[1]

                    dayStatus.isChecked = true
                    dayStatus.selectRangeDayStatus = SelectRangeDayStatus.AsEnd
                    dayStatusListSelectedRange[1] = (dayStatus)
                    var endDayKey = getKeyStringForViewBinding(endDate).toInt()
                    var thirdDateKey = getKeyStringForViewBinding(dayStatus).toInt()
                    for (preDay in dayStatusListAdapter.keys) {
                        var preDayKey = (preDay).toInt()
                        if (endDayKey > preDayKey && thirdDateKey < preDayKey) {
                            dayStatusListAdapter[preDay]?.let {
                                it.selectRangeDayStatus = SelectRangeDayStatus.Nothing
                                it.isChecked = false
                                dayStatusListSelectedByMultipleSelect.remove(it)
                            }
                        }
                    }

                    activeRangeView {
                        var preDayKey = (it).toInt()
                        endDayKey >= preDayKey && thirdDateKey < preDayKey
                    }


                }

                getPeriodByRange { startDay, endDay ->
                    var endDayKey = getKeyStringForViewBinding(endDay).toInt()
                    var startDayKey = getKeyStringForViewBinding(startDay).toInt()
                    activeRangeView {
                        var preDayKey = (it).toInt()
                        endDayKey >= preDayKey && startDayKey <= preDayKey


                    }

                }


            }

        }

    }

    open fun getSelectedList(): MutableList<DayStatus> {
        return when (calendarStatus.getTypeSelectDay()) {
            TypeSelectDay.None -> {
                mutableListOf()
            }

            TypeSelectDay.Single -> {
                dayStatusListSelectedBySingleSelect

            }

            TypeSelectDay.Multiple -> {
                dayStatusListSelectedByMultipleSelect

            }

            TypeSelectDay.Range -> {
                dayStatusListSelectedRange
            }
        }
    }

    override fun onHandleClickRowDay(dayStatus: DayStatus, viewbinding: Day, isLong: Boolean) {
        when (calendarStatus.getTypeSelectDay()) {
            TypeSelectDay.None -> {
                onNoneClickRowDay(dayStatus, viewbinding, isLong)
            }

            TypeSelectDay.Single -> {
                onSingleClickRowDay(dayStatus, viewbinding, isLong)

            }

            TypeSelectDay.Multiple -> {
                onMultipleClickRowDay(dayStatus, viewbinding, isLong)

            }

            TypeSelectDay.Range -> {
                onRangeClickRowDay(dayStatus, viewbinding, isLong)
            }
        }

    }

    fun getPeriodByRange(action: (startDay: DayStatus, endDay: DayStatus) -> Unit) {
        sortRangeListBase()
        dayStatusListSelectedRange[0].selectRangeDayStatus = SelectRangeDayStatus.AsStart
        dayStatusListSelectedRange[1].selectRangeDayStatus = SelectRangeDayStatus.AsEnd



        action(
            dayStatusListSelectedRange[0],
            dayStatusListSelectedRange[1],

            )
    }

    private fun activeRangeView(action: (preDay: String) -> Boolean) {
        for (preDay in dayStatusListSelectedViewBinding.keys) {
            if (action(preDay)) {
                var fe: Day? = dayStatusListSelectedViewBinding[preDay]
                fe?.let { viewbinding ->
                    //   clickDay(dayStatusListSelectedRange, dayStatusList[preDay - 1])
                    dayStatusListAdapter[preDay]?.let {
                        onDayItemClick(
                            it,
                            viewbinding
                        )
                    }
                }
            }
        }
    }

    private fun clickSingleDay(dayStatusListInner: MutableList<DayStatus>, dayStatus: DayStatus) {
        if (dayStatusListInner.contains(dayStatus)) {
            dayStatus.selectRangeDayStatus = SelectRangeDayStatus.Nothing
            dayStatus.isChecked = false
            dayStatusListInner.remove(dayStatus)

        } else {
            dayStatus.isChecked = true
            dayStatus.selectRangeDayStatus = SelectRangeDayStatus.Nothing
            dayStatusListInner.add(dayStatus)

        }
    }



}