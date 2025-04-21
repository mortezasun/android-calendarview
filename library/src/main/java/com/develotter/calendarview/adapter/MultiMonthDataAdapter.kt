package com.develotter.calendarview.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout.LayoutParams
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.viewbinding.ViewBinding
import com.develotter.calendarview.MonthView
import com.develotter.calendarview.R
import com.develotter.calendarview.status.DayStatus
import com.develotter.calendarview.status.MonthStatus
@Suppress("UNCHECKED_CAST")
class MultiMonthDataAdapter<D: ViewBinding,W: ViewBinding,M: ViewBinding>(
    private val inDayCellViewAdapterThis : BaseCalendarAdapter<D, W,M>
    ,private val yearMonthListThis: MutableList<MonthStatus<*, DayStatus>>) :
    Adapter<RecyclerView.ViewHolder>() {
    init {
        inDayCellViewAdapterThis.onReset()
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val linearLayout: LinearLayout = LinearLayout(parent.context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, parent.context.resources.getDimensionPixelSize(
                R.dimen.default_height))
            orientation = LinearLayout.VERTICAL
        }
        val monthView = MonthView<D, W,M>(parent.context)
        linearLayout.addView(monthView)

        return MultiMonthViewHolder<D, W,M>(monthView,linearLayout)
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as MultiMonthViewHolder<D, W,M>).monthView.setUp(  inDayCellViewAdapterThis,yearMonthListThis[position] )
    }
    override fun getItemCount() = yearMonthListThis.size

}
class MultiMonthViewHolder<D:ViewBinding,W: ViewBinding,M: ViewBinding>(var monthView:MonthView<D, W,M>,  itemView: View) : RecyclerView.ViewHolder(itemView)