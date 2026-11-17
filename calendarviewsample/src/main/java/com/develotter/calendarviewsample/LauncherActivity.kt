package com.develotter.calendarviewsample

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

/** Native Compose demo host using ComponentActivity, edge-to-edge and Material 3 dynamic theme. */
class LauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SampleTheme {
                CalendarSample(
                    onHeatmap = { startActivity(Intent(this@LauncherActivity, HeatMapViewActivity::class.java)) },
                )
            }
        }
    }
}
