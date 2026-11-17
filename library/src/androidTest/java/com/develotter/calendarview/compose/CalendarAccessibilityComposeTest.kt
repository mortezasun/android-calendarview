package com.develotter.calendarview.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.develotter.calendarview.enums.TypeSelectDay
import com.develotter.calendarview.selection.CalendarSelection
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Accessibility smoke tests: semantics, RTL and a 200% font scale. */
class CalendarAccessibilityComposeTest {
    @get:Rule
    val compose = createComposeRule()

    private val days = (1..31).map { day ->
        CalendarDayCell(
            epochDay = 20_000L + day,
            label = "$day",
            accessibilityLabel = "Day $day",
        )
    }

    @Test
    fun dayCellsExposeDescriptionsAndSelection() {
        var clicked: Long? = null
        compose.setContent {
            MaterialTheme {
                CalendarMonthGrid(
                    title = "Month",
                    weekdayLabels = listOf("M", "T", "W", "T", "F", "S", "S"),
                    firstDayOffset = 0,
                    days = days,
                    selection = CalendarSelection.from(TypeSelectDay.Single, listOf(20_001L)),
                    onDayClick = { clicked = it },
                )
            }
        }

        compose.onNodeWithContentDescription("Day 1")
            .assertIsDisplayed()
            .assertIsSelected()
            .performClick()
        compose.runOnIdle { assertEquals(20_001L, clicked) }
    }

    @Test
    fun rtlAndLargeFontKeepTheGridUsable() {
        compose.setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(density = 1f, fontScale = 2f),
                LocalLayoutDirection provides LayoutDirection.Rtl,
            ) {
                MaterialTheme {
                    CalendarMonthGrid(
                        title = "Month",
                        weekdayLabels = listOf("M", "T", "W", "T", "F", "S", "S"),
                        firstDayOffset = 0,
                        days = days,
                        selection = CalendarSelection.empty(TypeSelectDay.Multiple),
                        onDayClick = {},
                    )
                }
            }
        }

        compose.onNodeWithContentDescription("Day 1").assertExists().assertIsDisplayed()
        compose.onNodeWithContentDescription("Day 31").assertExists()
    }

    @Test
    fun monthGridDataOverloadRendersSecondaryLabelAndSkipsBlankCells() {
        val monthData = object : CalendarMonthData {
            override val title: String = "April 2026"
            override val weekdayLabels: List<String> = listOf("M", "T", "W", "T", "F", "S", "S")
            override val firstDayOffset: Int = 0
            override val days: List<CalendarDayCell> = listOf(
                CalendarDayCell(epochDay = 20_100L, label = "1", accessibilityLabel = "Day 1"),
                CalendarDayCell(epochDay = 20_101L, label = "", accessibilityLabel = "", enabled = false),
            )
        }
        compose.setContent {
            MaterialTheme {
                CalendarMonthGrid(
                    data = monthData,
                    selection = CalendarSelection.empty(TypeSelectDay.Single),
                    onDayClick = {},
                    secondaryLabel = { epochDay -> "Sec $epochDay" },
                    secondaryAccessibilityLabel = { epochDay -> "Second $epochDay" },
                )
            }
        }

        compose.onNodeWithContentDescription("Day 1, Second 20100").assertExists().assertIsDisplayed()
        compose.onNodeWithContentDescription(", Second 20101").assertDoesNotExist()
    }
}
