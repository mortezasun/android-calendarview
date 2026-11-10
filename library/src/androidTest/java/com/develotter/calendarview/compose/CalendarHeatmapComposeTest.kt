package com.develotter.calendarview.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.develotter.calendarview.enums.TypeSelectDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.Locale

/**
 * Device/emulator tests for the heatmap composables. They cover layout direction, the
 * selectable/disabled semantics and the minimum touch target; they are not JVM unit tests.
 * `locale = Locale.US` keeps the generated content descriptions independent of the device.
 */
class CalendarHeatmapComposeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun weeksAsRowsRendersLabelsAndReportsClickedEpochDay() {
        var clicked: Long? = null
        compose.setContent {
            MaterialTheme {
                CalendarYearHeatmap(
                    months = listOf(heatmapMonth(listOf(day(10L), day(11L)))),
                    onDayClick = { clicked = it.epochDay },
                    orientation = HeatmapOrientation.WeeksAsRows,
                    locale = Locale.US,
                )
            }
        }
        compose.onNodeWithText("W1").assertExists()
        compose.onNodeWithText("W7").assertExists()
        compose.onNodeWithContentDescription("Day 10: 50%").performClick()
        compose.runOnIdle { assertEquals(10L, clicked) }
    }

    @Test
    fun weeksAsColumnsScrollsToLateWeekAndReportsClickedEpochDay() {
        var clicked: Long? = null
        val days = (0 until 20).flatMap { week -> (0 until 7).map { day(week * 7L + it) } }
        compose.setContent {
            MaterialTheme {
                CalendarYearHeatmap(
                    months = listOf(heatmapMonth(days)),
                    onDayClick = { clicked = it.epochDay },
                    style = HeatmapStyle(cellSize = 32.dp),
                    locale = Locale.US,
                )
            }
        }
        compose.onNodeWithContentDescription("Day 130: 50%").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(130L, clicked) }
    }

    @Test
    fun rtlMirrorsWeekdayColumns() {
        val days = (0 until 7).map { day(it.toLong()) }
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MaterialTheme {
                    CalendarYearHeatmap(
                        months = listOf(heatmapMonth(days)),
                        onDayClick = {},
                        orientation = HeatmapOrientation.WeeksAsRows,
                        locale = Locale.US,
                    )
                }
            }
        }
        val first = compose.onNodeWithText("W1").getUnclippedBoundsInRoot()
        val last = compose.onNodeWithText("W7").getUnclippedBoundsInRoot()
        assertTrue("W1 left=${first.left} should be right of W7 left=${last.left}", first.left > last.left)
    }

    @Test
    fun selectionIsExposedAndUpdatedByCaller() {
        compose.setContent {
            MaterialTheme {
                val state = rememberCalendarSelectionState(TypeSelectDay.Single, listOf(11L))
                CalendarYearHeatmap(
                    months = listOf(heatmapMonth(listOf(day(10L), day(11L)))),
                    onDayClick = { state.onDayClick(it.epochDay) },
                    selection = state.selection,
                    locale = Locale.US,
                )
            }
        }
        compose.onNodeWithContentDescription("Day 11: 50%").assertIsSelected()
        compose.onNodeWithContentDescription("Day 10: 50%").performClick()
        compose.onNodeWithContentDescription("Day 10: 50%").assertIsSelected()
    }

    @Test
    fun disabledDayIsNotEnabledAndReportsNoClick() {
        var clicked: Long? = null
        compose.setContent {
            MaterialTheme {
                CalendarYearHeatmap(
                    months = listOf(heatmapMonth(listOf(day(10L, enabled = false), day(11L)))),
                    onDayClick = { clicked = it.epochDay },
                    locale = Locale.US,
                )
            }
        }
        compose.onNodeWithContentDescription("Day 10: 50%").assertIsNotEnabled().performClick()
        compose.runOnIdle { assertNull(clicked) }
    }

    @Test
    fun smallCellSizeKeepsMinimumTouchTarget() {
        compose.setContent {
            MaterialTheme {
                CalendarYearHeatmap(
                    months = listOf(heatmapMonth(listOf(day(10L), day(11L)))),
                    onDayClick = {},
                    style = HeatmapStyle(cellSize = 24.dp),
                    locale = Locale.US,
                )
            }
        }
        val bounds = compose.onNodeWithContentDescription("Day 10: 50%").getUnclippedBoundsInRoot()
        val height = bounds.bottom - bounds.top
        val width = bounds.right - bounds.left
        assertTrue("touch target height was $height, expected at least 48dp", height >= 48.dp)
        assertTrue("touch target width was $width, expected at least 48dp", width >= 48.dp)
    }

    @Test
    fun weeksAsRowsKeepsMinimumHeightWhenTheViewportIsNarrow() {
        // A 300dp viewport leaves each of the seven columns well below 48dp: this orientation
        // shares the row width instead of forcing a minimum, so only the slot height is guaranteed.
        compose.setContent {
            MaterialTheme {
                Box(Modifier.width(300.dp)) {
                    CalendarYearHeatmap(
                        months = listOf(heatmapMonth(listOf(day(10L), day(11L)))),
                        onDayClick = {},
                        orientation = HeatmapOrientation.WeeksAsRows,
                        style = HeatmapStyle(cellSize = 24.dp),
                        locale = Locale.US,
                    )
                }
            }
        }
        val first = compose.onNodeWithContentDescription("Day 10: 50%").getUnclippedBoundsInRoot()
        val second = compose.onNodeWithContentDescription("Day 11: 50%").getUnclippedBoundsInRoot()
        assertTrue(
            "slot height was ${first.bottom - first.top}, expected at least 48dp",
            first.bottom - first.top >= 48.dp,
        )
        assertEquals(
            "columns share the row width equally",
            (first.right - first.left).value,
            (second.right - second.left).value,
            0.5f,
        )
    }
}

private val weekdayLabels = listOf("W1", "W2", "W3", "W4", "W5", "W6", "W7")

private fun day(epochDay: Long, enabled: Boolean = true) =
    HeatmapDayData(epochDay = epochDay, label = "Day $epochDay", intensity = 0.5f, enabled = enabled)

/** Pads the cells to whole weeks so the year heatmap's seven-cells-per-week contract holds. */
private fun heatmapMonth(days: List<HeatmapDayData?>): HeatmapMonthData {
    val padded = days + List((7 - days.size % 7) % 7) { null }
    return HeatmapMonthData(title = "Test month", weekdayLabels = weekdayLabels, weeks = padded.chunked(7))
}
