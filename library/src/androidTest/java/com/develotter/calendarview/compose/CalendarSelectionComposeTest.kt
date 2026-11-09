package com.develotter.calendarview.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.develotter.calendarview.enums.TypeSelectDay
import org.junit.Rule
import org.junit.Test

/** Device/emulator tests; these are not JVM unit tests or full process-death tests. */
class CalendarSelectionComposeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun clearedInitialSelectionSurvivesSavedStateRestoration() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { SelectionProbe(TypeSelectDay.Single, listOf(10L)) }
        compose.onNodeWithText("selected:10").assertTextEquals("selected:10")
        compose.onNodeWithText("clear").performClick()
        compose.onNodeWithText("selected:empty").assertTextEquals("selected:empty")
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("selected:empty").assertTextEquals("selected:empty")
    }

    @Test
    fun rangeEndpointsAndInteriorMembershipSurviveRestoration() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { SelectionProbe(TypeSelectDay.Range) }
        compose.onNodeWithText("add10").performClick()
        compose.onNodeWithText("add20").performClick()
        compose.onNodeWithText("selected:10,20").assertTextEquals("selected:10,20")
        compose.onNodeWithText("contains15:true").assertTextEquals("contains15:true")
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("selected:10,20").assertTextEquals("selected:10,20")
        compose.onNodeWithText("contains15:true").assertTextEquals("contains15:true")
    }

    @Test
    fun changingModeClearsSelectionAndNewModeRestoresWithoutOldKeys() {
        val mode = mutableStateOf(TypeSelectDay.Multiple)
        val restoration = StateRestorationTester(compose)
        restoration.setContent { SelectionProbe(mode.value) }
        compose.onNodeWithText("add10").performClick()
        compose.onNodeWithText("add20").performClick()
        compose.onNodeWithText("selected:10,20").assertTextEquals("selected:10,20")
        compose.runOnIdle { mode.value = TypeSelectDay.Single }
        compose.onNodeWithText("selected:empty").assertTextEquals("selected:empty")
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("selected:empty").assertTextEquals("selected:empty")
        compose.onNodeWithText("add20").performClick()
        compose.onNodeWithText("selected:20").assertTextEquals("selected:20")
    }

    @Test
    fun gridExposesDisabledDatesAndUpdatesSelectedSemanticsAfterClick() {
        compose.setContent {
            MaterialTheme {
                val state = rememberCalendarSelectionState(TypeSelectDay.Single)
                CalendarMonthGrid(
                    title = "Test month",
                    weekdayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
                    firstDayOffset = 0,
                    days = listOf(
                        CalendarDayCell(10L, "1", "disabled day", enabled = false),
                        CalendarDayCell(11L, "2", "enabled day"),
                    ),
                    selection = state.selection,
                    onDayClick = state::onDayClick,
                )
            }
        }
        compose.onNodeWithContentDescription("disabled day").assertIsNotEnabled()
        compose.onNodeWithContentDescription("enabled day").performClick()
        compose.onNodeWithContentDescription("enabled day").assertIsSelected()
    }
}

@Composable
private fun SelectionProbe(mode: TypeSelectDay, initial: List<Long> = emptyList()) {
    MaterialTheme {
        val state = rememberCalendarSelectionState(mode, initial)
        Column {
            Text("selected:${state.selection.selectedDays.joinToString(",").ifEmpty { "empty" }}")
            Text("contains15:${15L in state.selection}")
            Button(onClick = { state.onDayClick(10L) }) { Text("add10") }
            Button(onClick = { state.onDayClick(20L) }) { Text("add20") }
            Button(onClick = state::clear) { Text("clear") }
        }
    }
}
