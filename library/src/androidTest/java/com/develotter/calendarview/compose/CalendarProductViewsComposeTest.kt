package com.develotter.calendarview.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.develotter.calendarview.enums.TypeSelectDay
import com.develotter.calendarview.selection.CalendarSelection
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Device/emulator tests for the Phase 2 week and agenda product views. */
class CalendarProductViewsComposeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun weekGridRendersWeekNumberMarkerAndReportsClicks() {
        var clicked: Long? = null
        val days = (0 until 7).map { index ->
            CalendarDayCell(
                epochDay = 100L + index,
                label = "${index + 1}",
                accessibilityLabel = "Day ${index + 1}",
                isToday = index == 0,
            )
        }
        compose.setContent {
            MaterialTheme {
                CalendarWeekGrid(
                    title = "Week 44",
                    weekdayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
                    days = days,
                    selection = CalendarSelection.empty(TypeSelectDay.Single),
                    onDayClick = { clicked = it },
                    todayDescription = "Today",
                    markers = mapOf(100L to listOf(CalendarDayMarker(Color.Red, "Holiday"))),
                    weekNumber = "44",
                )
            }
        }

        compose.onNodeWithText("44").assertExists()
        compose.onNodeWithText("Week 44").assertExists()
        compose.onNodeWithContentDescription("Day 1, Holiday, Today").assertExists()
        compose.onNodeWithText("2").performClick()
        compose.runOnIdle { assertEquals(101L, clicked) }
    }

    @Test
    fun agendaRendersDaysAndReportsClicks() {
        var clicked: Long? = null
        val days = listOf(
            CalendarDayCell(epochDay = 10L, label = "1", accessibilityLabel = "Day 1"),
            CalendarDayCell(epochDay = 11L, label = "2", accessibilityLabel = "Day 2"),
        )
        compose.setContent {
            MaterialTheme {
                CalendarAgenda(
                    days = days,
                    selection = CalendarSelection.empty(TypeSelectDay.Single),
                    onDayClick = { clicked = it },
                    markers = mapOf(10L to listOf(CalendarDayMarker(Color.Red, "Holiday"))),
                )
            }
        }

        compose.onNodeWithText("Day 1").assertExists()
        compose.onNodeWithContentDescription("Day 1, Holiday").assertExists()
        compose.onNodeWithText("Day 2").performClick()
        compose.runOnIdle { assertEquals(11L, clicked) }
    }
}
