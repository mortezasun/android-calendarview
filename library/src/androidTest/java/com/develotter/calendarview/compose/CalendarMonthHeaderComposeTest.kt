package com.develotter.calendarview.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.develotter.calendarview.enums.TypeSelectDay
import com.develotter.calendarview.selection.CalendarSelection
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Device/emulator tests for the shared month header and grid accessibility semantics. */
class CalendarMonthHeaderComposeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun headerDispatchesNavigationAndDisablesUnavailableDirections() {
        val state = FakeMonthNavigationState(canGoPrevious = false)
        compose.setContent {
            MaterialTheme {
                CalendarMonthHeader(
                    title = "November 2026",
                    state = state,
                    previousMonthContentDescription = "Previous month",
                    nextMonthContentDescription = "Next month",
                    todayLabel = "Today",
                )
            }
        }

        compose.onNodeWithText("November 2026").assertExists()
        compose.onNodeWithContentDescription("Previous month").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Next month").performClick()
        compose.onNodeWithText("Today").performClick()

        compose.runOnIdle {
            assertEquals(0, state.previousClicks)
            assertEquals(1, state.nextClicks)
            assertEquals(1, state.todayClicks)
        }
    }

    @Test
    fun gridAppendsTheLocalizedTodayDescriptionToTheDayLabel() {
        compose.setContent {
            MaterialTheme {
                CalendarMonthGrid(
                    title = "Test month",
                    weekdayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
                    firstDayOffset = 0,
                    days = listOf(
                        CalendarDayCell(
                            epochDay = 10L,
                            label = "1",
                            accessibilityLabel = "1 November 2026",
                            isToday = true,
                        ),
                    ),
                    selection = CalendarSelection.empty(TypeSelectDay.Single),
                    onDayClick = {},
                    showHeader = false,
                    showWeekdayLabels = false,
                    todayDescription = "Today",
                )
            }
        }

        compose.onNodeWithContentDescription("1 November 2026, Today").assertExists()
    }
}

private class FakeMonthNavigationState(
    override var canGoPrevious: Boolean = true,
    override var canGoNext: Boolean = true,
    override var canGoToday: Boolean = true,
) : CalendarMonthNavigationState {
    var previousClicks = 0
    var nextClicks = 0
    var todayClicks = 0

    override fun previous() {
        previousClicks++
    }

    override fun next() {
        nextClicks++
    }

    override fun goToToday() {
        todayClicks++
    }
}
