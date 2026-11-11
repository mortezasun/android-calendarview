package com.develotter.calendarview.compose

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalendarMarkersTest {
    @Test
    fun appendsMarkerAndTodayDescriptionsInOrder() {
        val markers = listOf(
            CalendarDayMarker(Color.Red, "Holiday"),
            CalendarDayMarker(Color.Blue, "Event"),
            CalendarDayMarker(Color.Green),
        )
        assertEquals("Holiday, Event, Today", calendarMarkerDescription(markers, true, "Today"))
        assertEquals("Holiday, Event", calendarMarkerDescription(markers, false, "Today"))
    }

    @Test
    fun returnsNullWhenThereIsNothingToAppend() {
        assertNull(calendarMarkerDescription(emptyList(), false, "Today"))
        assertNull(calendarMarkerDescription(listOf(CalendarDayMarker(Color.Red)), true, null))
    }
}
