package com.develotter.calendarviewsample

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.develotter.calendarviewsample.databinding.DialogMultiBinding
import com.develotter.calendarviewsample.databinding.RowHeatBinding
import java.time.LocalDate

class HeatMapViewActivity: AppCompatActivity() {

    private lateinit var binding: RowHeatBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = RowHeatBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = getString(R.string.HeatMapCalendar)
        val heatmapData = mutableMapOf<LocalDate, Float>()
        heatmapData[LocalDate.of(2025, 1, 15)] = 0.3f
        heatmapData[LocalDate.of(2025, 3, 10)] = 0.8f
        heatmapData[LocalDate.of(2025, 7, 4)] = 1.0f
        heatmapData[LocalDate.of(2025, 10, 31)] = 0.5f
        heatmapData[LocalDate.of(2025, 12, 25)] = 0.7f


        binding.heatView.applyHeatmapData(2025,heatmapData){date ->
            val bindingDialog = DialogMultiBinding.inflate(layoutInflater)


            AlertDialog.Builder(this)
                .setTitle(  " ${date.dayOfMonth}  / ${date.month} / ${date.year}")
                .setView(bindingDialog.root)
                .show()
        }
    }
}