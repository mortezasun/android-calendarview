package com.develotter.calendarview.kmpsample

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "CalendarView KMP Sample (Linux)",
    ) {
        App()
    }
}
