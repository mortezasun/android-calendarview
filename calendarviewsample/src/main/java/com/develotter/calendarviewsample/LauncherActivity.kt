package com.develotter.calendarviewsample

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.develotter.calendarview.adapter.MonthSampleAdapter
import com.develotter.calendarview.calendars.gregorian.GregorianStatus
import com.develotter.calendarview.enums.TypeSelectDay
import com.develotter.calendarview.calendars.solarHijri.SolarHijriDayStatus
import com.develotter.calendarview.status.CalendarStatus
import com.develotter.calendarview.status.DayStatus
import com.develotter.calendarview.status.MonthStatus
import com.develotter.calendarview.status.SelectRangeDayStatus
import com.develotter.calendarviewsample.databinding.CalendarDialogBinding
import com.develotter.calendarviewsample.databinding.LauncherActivityBinding
import com.develotter.calendarviewsample.databinding.RowCalendarBinding
import com.develotter.calendarviewsample.databinding.RowCalendarControllerBinding
import com.develotter.calendarviewsample.databinding.RowMonthBinding
import com.develotter.calendarviewsample.databinding.RowShowSelectedDayBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import ir.huri.jcal.JalaliCalendar
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale
import java.util.Vector

class LauncherActivity: AppCompatActivity()  {
    private lateinit var binding: LauncherActivityBinding


    private lateinit var thisCalendarStatus: CalendarStatus
    private  var textStyle: TextStyle = TextStyle.SHORT_STANDALONE

    private lateinit var lcInUse: Locale
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lcInUse =getSuperLocale()
        binding = LauncherActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        val adapter = MyAdapter(getData())

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        binding.list.adapter = adapter
        binding.list.layoutManager = layoutManager

    }




    private fun getData(): Vector<MyData> {
        val data = Vector<MyData>()
        data.add(MyData(1,"Calendar View", R.drawable.baseline_calendar_month_24))
        data.add(MyData(2,"Calendar View in Dialog", R.drawable.baseline_calendar_month_24))
        data.add(MyData(4,"Heatmap Calendar", R.drawable.baseline_map_24))


        return data
    }

    data class MyData(val index: Int,val title: String, val imageResource: Int)

    inner class MyAdapter(private val data: Vector<MyData>) :
        RecyclerView.Adapter<MyViewHolder>() {

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): MyViewHolder {
            val itemView = layoutInflater.inflate(R.layout.row_launcher, parent, false)
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.bind(data[position])
            holder.itemView.setOnClickListener {
                when(data[position].index){
                    1->{
                        val intent = Intent(this@LauncherActivity, CalendarViewActivity::class.java)
                        startActivity(intent)
                    }
                    2->{
                        showCalendarDialog()
                    }

                    4->{
                        val intent = Intent(this@LauncherActivity, HeatMapViewActivity::class.java)
                        startActivity(intent)
                    }
                }
            }

        }
        override fun getItemCount() = data.size
    }
    inner class MyViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {

        private val titleTextView: android.widget.TextView = itemView.findViewById(R.id.titleTextView)
        private val imageView: android.widget.ImageView = itemView.findViewById(R.id.imageView)

        fun bind(myData: MyData) {
            titleTextView.text = myData.title
            imageView.setImageResource(myData.imageResource)

        }
    }
    fun showCalendarDialog() {
        val bindingDialog = CalendarDialogBinding.inflate(layoutInflater)

        thisCalendarStatus = CalendarStatus().setLocalInUse(lcInUse).setShowRowMonthName(true)

        setUpCalendar(bindingDialog)
        AlertDialog.Builder(this)

            .setView(bindingDialog.root)


            .show()
    }
   

    fun setUpCalendar(binding: CalendarDialogBinding) {

        var dayStatusListSelectedBySingleSelect: MutableList<DayStatus> = mutableListOf()

        val jal = SolarHijriDayStatus(JalaliCalendar(1404, 2, 7), lcInUse)
        val jal1 = SolarHijriDayStatus(JalaliCalendar(1404, 2, 14), lcInUse)
        val jal2 = SolarHijriDayStatus(JalaliCalendar(1404, 2, 21), lcInUse)
        dayStatusListSelectedBySingleSelect.add(0, jal)
        binding.calendar.addMonths(GregorianStatus::class.java,object :
            MonthSampleAdapter<RowCalendarBinding, RowCalendarBinding, RowMonthBinding, RowShowSelectedDayBinding>
                (
                thisCalendarStatus
            ) {
            override fun onBindWeekView(dayStatus: DayOfWeek): RowCalendarBinding {
                val binding = RowCalendarBinding.inflate(layoutInflater)
                binding.textRow.text =
                    dayStatus.getDisplayName(textStyle, thisCalendarStatus.getLocalInUse())



                return binding
            }

            override val select: RowShowSelectedDayBinding
                get() = RowShowSelectedDayBinding.inflate(layoutInflater)


            override fun onBindDayView(dayStatus: DayStatus): RowCalendarBinding {
                val binding = RowCalendarBinding.inflate(layoutInflater)
                binding.textRow.text = dayStatus.formatDayIntWithLocale()

                animateDaySelection(dayStatus, binding)

                return binding
            }

            override fun onBindControllerCalendar(): ViewBinding {

                val bindings = RowCalendarControllerBinding.inflate(layoutInflater)
                bindings.nextButton.setOnClickListener {
                    binding.calendar.scrollToNextMonth()
                }
                bindings.prevButton.setOnClickListener {
                    binding.calendar.scrollToPreviousMonth()
                }
                return bindings
            }

            override fun onBindMonthView(dayStatus: MonthStatus<*, *>): RowMonthBinding {
                val binding = RowMonthBinding.inflate(layoutInflater)
                binding.textRowMonth.text = dayStatus.getThisMonthCaption()


                return binding
            }


            override fun onBindLastMonthDayView(dayStatus: DayStatus): RowCalendarBinding {
                val binding = RowCalendarBinding.inflate(layoutInflater)

                binding.textRow.text = dayStatus.formatDayIntWithLocale()
                binding.textRow.setTextColor(
                    ContextCompat.getColor(
                        this@LauncherActivity,
                        R.color.gray
                    )
                )
                return binding
            }

            override fun onBindNextMonthDayView(dayStatus: DayStatus): RowCalendarBinding {
                val binding = RowCalendarBinding.inflate(layoutInflater)
                binding.textRow.text = dayStatus.formatDayIntWithLocale()
                binding.textRow.setTextColor(
                    ContextCompat.getColor(
                        this@LauncherActivity,
                        R.color.gray
                    )
                )
                return binding
            }

            override fun onDayFocused(status: MonthStatus<*, DayStatus>) {

            }


            override fun onDayItemClick(
                dayStatus: DayStatus,
                viewbinding: RowCalendarBinding
            ) {
                animateDaySelection(dayStatus, viewbinding)

                var list = getSelectedList()

                val group =
                    binding.calendar.findViewById<ChipGroup>(select.ChipGroupLayout.id) as (ChipGroup)
                if(thisCalendarStatus.getTypeSelectDay()==TypeSelectDay.Range){
                    if(list.isNotEmpty()) {

                        var chip = group.findViewWithTag<Chip>(
                            "-100"
                        )
                        if(chip==null){
                            chip= Chip(this@LauncherActivity)
                            group.addView(chip)
                        }

                        var title = if (list.size == 1) {
                            getString(R.string.From) + "  " + list[0].getDisplayName(textStyle)
                        }else{
                            getString(R.string.From) + "  " + list[0].getDisplayName(textStyle) + " , " + getString(R.string.To)+ " " + list[1].getDisplayName(textStyle)
                        }
                        chip.text =title
                        chip.tag ="-100"

                        if (list.size>1){
                            //binding.toolbar.title = binding.calendar.getMonthInPosition().getThisMonthCaption() + ": (${list[0].localDate.until(list[1].localDate).days+1}) "
                        }

                    }else
                    {
                        group.removeAllViews()
                      //  binding.toolbar.title = binding.calendar.getMonthInPosition().getThisMonthCaption()
                    }

                }else {
                   // binding.toolbar.title = binding.calendar.getMonthInPosition().getThisMonthCaption() + ": (${list.size}) "
                    list.forEach { it ->

                        createChip(group,getKeyStringForViewBinding(it), it.getDisplayName(textStyle))
                    }
                    val oldList = group.children.toList()
                    oldList.forEach {
                        var chip = list.find { d -> getKeyStringForViewBinding(d) == it.tag }
                        if (chip == null) {
                            group.removeView(it)
                        }
                    }
                }

            }

            override fun onUpdateMonthView(   monthStatus: MonthStatus<*, *>,headerPager: RowMonthBinding) {
                headerPager.textRowMonth.text = monthStatus.getThisMonthCaption()
            }

            override fun onMonthViewActive(
                monthStatus: MonthStatus<*, *>
            ) {

              //  binding.toolbar.title = monthStatus.getThisMonthCaption() + ": (${getSelectedList().size}) "
            }
        })

    }
    private fun createChip(group: ChipGroup,key: String,title:String){
        var chip = group.findViewWithTag<Chip>(
            key
        )
        if (chip == null) {
            var ch = Chip(this@LauncherActivity)

            ch.text = title
            ch.tag =key

            group.addView(ch)
        }
    }

    private fun animateDaySelection(dayStatus: DayStatus, viewbinding: RowCalendarBinding) {
        if (dayStatus.isToday()) {
            viewbinding.baseRow.setBackgroundColor(
                ContextCompat.getColor(
                    this@LauncherActivity,
                    R.color.white
                )
            )
            viewbinding.textRow.setTextColor(
                ContextCompat.getColor(
                    this@LauncherActivity,
                    com.develotter.calendarview.R.color.purple_200
                )
            )
        } else {
            viewbinding.baseRow.setBackgroundColor(
                ContextCompat.getColor(
                    this@LauncherActivity,
                    R.color.white
                )
            )
            viewbinding.textRow.setTextColor(
                ContextCompat.getColor(
                    this@LauncherActivity,
                    com.develotter.calendarview.R.color.purple_500
                )
            )
        }
        val startBackgroundColor = ContextCompat.getColor(
            this@LauncherActivity,
            if (dayStatus.isChecked) R.color.white else com.develotter.calendarview.R.color.teal_200
        )
        val endBackgroundColor = ContextCompat.getColor(
            this@LauncherActivity,
            if (dayStatus.isChecked) com.develotter.calendarview.R.color.teal_200 else R.color.white
        )
        val backgroundColorAnimator =
            ValueAnimator.ofObject(ArgbEvaluator(), startBackgroundColor, endBackgroundColor)
        backgroundColorAnimator.duration = 400
        backgroundColorAnimator.addUpdateListener { animator ->
            viewbinding.baseRow.setBackgroundColor(animator.animatedValue as Int)
        }


        backgroundColorAnimator.start()

        when (dayStatus.selectRangeDayStatus) {
            SelectRangeDayStatus.Nothing -> {
                viewbinding.imgRowEndBottom.visibility = View.GONE
                viewbinding.imgRowEndTop.visibility = View.GONE
                viewbinding.imgRowStartTop.visibility = View.GONE
                viewbinding.imgRowStartBottom.visibility = View.GONE
            }

            SelectRangeDayStatus.AsStartOrEnd -> {
                viewbinding.imgRowEndBottom.visibility = View.VISIBLE
                viewbinding.imgRowEndTop.visibility = View.VISIBLE
                viewbinding.imgRowStartTop.visibility = View.VISIBLE
                viewbinding.imgRowStartBottom.visibility = View.VISIBLE
            }

            SelectRangeDayStatus.AsStart -> {
                viewbinding.imgRowEndBottom.visibility = View.GONE
                viewbinding.imgRowEndTop.visibility = View.GONE
                viewbinding.imgRowStartTop.visibility = View.VISIBLE
                viewbinding.imgRowStartBottom.visibility = View.VISIBLE
            }

            SelectRangeDayStatus.AsEnd -> {
                viewbinding.imgRowEndBottom.visibility = View.VISIBLE
                viewbinding.imgRowEndTop.visibility = View.VISIBLE
                viewbinding.imgRowStartTop.visibility = View.GONE
                viewbinding.imgRowStartBottom.visibility = View.GONE
            }
        }


    }

}