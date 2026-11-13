package com.develotter.calendarviewsample

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.develotter.calendarview.calendars.chineseCompose.rememberChineseMonthState
import com.develotter.calendarview.calendars.chineseNative.ChineseCalendarEngine
import com.develotter.calendarview.calendars.chineseNative.ChineseMonth
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.calendars.hijri.UmmAlQuraHijriDateProvider
import com.develotter.calendarview.compose.CalendarMonthHeader
import com.develotter.calendarview.compose.rememberCalendarSelectionState
import com.develotter.calendarview.enums.TypeSelectDay
import org.junit.Assert.assertNotSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.util.Locale

private class FixedToday(private val epochDay: Long) : () -> Long {
    override fun invoke(): Long = epochDay
}

/** Targeted regression tests for the reviewed Chinese Compose and sample integration paths. */
@RunWith(AndroidJUnit4::class)
class ChineseComposeRegressionTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun chineseMonthStateSurvivesRestorationAndUsesAReplacedTodayCallback() {
        val firstToday = FixedToday(ChineseMonth.of(2023, 3).startEpochDay())
        val secondToday = FixedToday(ChineseMonth.of(2023, 4).startEpochDay())
        assertNotSame(firstToday, secondToday)
        val today = mutableStateOf<() -> Long>(firstToday)
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            MaterialTheme {
                Column {
                    val state = rememberChineseMonthState(
                        initialMonth = ChineseMonth.of(2023, 2),
                        minMonth = ChineseMonth.of(2023, 1),
                        maxMonth = ChineseMonth.of(2023, 3),
                        today = today.value,
                    )
                    Text("month:${state.title()}")
                    Text("canToday:${state.canGoToday}")
                    Button(onClick = state::next) { Text("next") }
                    Button(onClick = { today.value = secondToday }) { Text("replaceToday") }
                }
            }
        }

        compose.onNodeWithText("canToday:true").assertExists()
        compose.onNodeWithText("next").performClick()
        compose.onNodeWithText("month:2023 Leap 2nd Month").assertExists()

        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("month:2023 Leap 2nd Month").assertExists()

        compose.onNodeWithText("replaceToday").performClick()
        compose.onNodeWithText("canToday:false").assertExists()
        // Replacing the callback instance must not reset the visible navigation state.
        compose.onNodeWithText("month:2023 Leap 2nd Month").assertExists()
    }

    @Test
    fun invalidCenterRecoversWhenAValidAnchorArrivesWithoutCompositionRestart() {
        val invalid = ChineseCalendarEngine.MIN_EPOCH_DAY - 1
        val valid = ChineseCalendarEngine.MIN_EPOCH_DAY
        val anchor = mutableStateOf(invalid)
        val validMonth = moveSampleMonth(SampleCalendar.Chinese, valid, 0)
        compose.setContent {
            MaterialTheme {
                Column {
                    Button(onClick = { anchor.value = valid }) { Text("valid") }
                    CalendarSampleViewport(
                        calendar = SampleCalendar.Chinese,
                        anchor = anchor.value,
                        forcedMode = SampleDisplayMode.Single,
                        viewportHeight = androidx.compose.ui.unit.Dp(200f),
                    ) { monthAnchor ->
                        Text("month:$monthAnchor")
                    }
                }
            }
        }

        // The initial center is invalid, so the window must fail before the valid anchor arrives.
        compose.onNodeWithText("month:$validMonth").assertDoesNotExist()
        compose.onNodeWithText("valid").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("month:$validMonth").assertIsDisplayed()
        compose.onNodeWithText("Updating month window").assertDoesNotExist()
    }

    @Test
    fun switchingFromGregorianOverlayToChineseMonthsRebuildsTheWindow() {
        val anchor = LocalDate.of(2023, 2, 15).toEpochDay()
        val overlay = mutableStateOf(true)
        val gregorianMonth = moveSampleMonth(SampleCalendar.Gregorian, anchor, 0)
        val chineseMonth = moveSampleMonth(SampleCalendar.Chinese, anchor, 0)
        compose.setContent {
            MaterialTheme {
                Column {
                    Button(onClick = { overlay.value = false }) { Text("chinese") }
                    CalendarSampleViewport(
                        calendar = SampleCalendar.Chinese,
                        anchor = anchor,
                        monthCalendar = sampleMonthNavigationCalendar(SampleCalendar.Chinese, overlay.value),
                        forcedMode = SampleDisplayMode.HorizontalPager,
                        viewportHeight = androidx.compose.ui.unit.Dp(200f),
                    ) { monthAnchor ->
                        Text("month:$monthAnchor")
                    }
                }
            }
        }

        compose.onNodeWithText("month:$gregorianMonth").assertIsDisplayed()
        compose.onNodeWithText("chinese").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("month:$chineseMonth").assertIsDisplayed()
        compose.onNodeWithText("Updating month window").assertDoesNotExist()
    }

    @Test
    fun chineseBoundaryWindowRendersAndNavigatesInSingleMode() {
        val anchor = mutableStateOf(ChineseCalendarEngine.MIN_EPOCH_DAY)
        val minimum = moveSampleMonth(SampleCalendar.Chinese, ChineseCalendarEngine.MIN_EPOCH_DAY, 0)
        val maximum = moveSampleMonth(SampleCalendar.Chinese, ChineseCalendarEngine.MAX_EPOCH_DAY, 0)
        compose.setContent {
            MaterialTheme {
                Column {
                    val navigation = rememberSampleMonthNavigation(
                        calendar = SampleCalendar.Chinese,
                        anchor = anchor.value,
                        locale = Locale.ENGLISH,
                    ) { anchor.value = it }
                    CalendarMonthHeader(
                        title = navigation.title,
                        state = navigation.state,
                        previousMonthContentDescription = "Previous month",
                        nextMonthContentDescription = "Next month",
                    )
                    Button(onClick = { anchor.value = ChineseCalendarEngine.MAX_EPOCH_DAY }) { Text("upper") }
                    CalendarSampleViewport(
                        calendar = SampleCalendar.Chinese,
                        anchor = anchor.value,
                        forcedMode = SampleDisplayMode.Single,
                        viewportHeight = androidx.compose.ui.unit.Dp(200f),
                    ) { monthAnchor ->
                        Text("month:$monthAnchor")
                    }
                }
            }
        }

        compose.onNodeWithText("month:$minimum").assertIsDisplayed()
        compose.onNodeWithText("Window month 1 / 13").assertExists()
        compose.onNodeWithText("Updating month window").assertDoesNotExist()
        compose.onNodeWithContentDescription("Next month").performClick()
        compose.onNodeWithText("month:${moveSampleMonth(SampleCalendar.Chinese, minimum, 1)}").assertIsDisplayed()

        compose.onNodeWithText("upper").performClick()
        compose.onNodeWithText("month:$maximum").assertIsDisplayed()
        compose.onNodeWithText("Window month 13 / 13").assertExists()
        compose.onNodeWithText("Updating month window").assertDoesNotExist()
        compose.onNodeWithContentDescription("Previous month").performClick()
        compose.onNodeWithText("month:${moveSampleMonth(SampleCalendar.Chinese, maximum, -1)}").assertIsDisplayed()
    }

    @Test
    fun chineseBoundaryWindowRendersAndNavigatesInPager() {
        val anchor = mutableStateOf(ChineseCalendarEngine.MIN_EPOCH_DAY)
        val minimum = moveSampleMonth(SampleCalendar.Chinese, ChineseCalendarEngine.MIN_EPOCH_DAY, 0)
        val maximum = moveSampleMonth(SampleCalendar.Chinese, ChineseCalendarEngine.MAX_EPOCH_DAY, 0)
        compose.setContent {
            MaterialTheme {
                Column {
                    Button(onClick = { anchor.value = ChineseCalendarEngine.MAX_EPOCH_DAY }) { Text("upper") }
                    CalendarSampleViewport(
                        calendar = SampleCalendar.Chinese,
                        anchor = anchor.value,
                        forcedMode = SampleDisplayMode.HorizontalPager,
                        onVisibleMonthChanged = { anchor.value = it },
                        viewportHeight = androidx.compose.ui.unit.Dp(200f),
                    ) { monthAnchor ->
                        Text("month:$monthAnchor")
                    }
                }
            }
        }

        compose.onNodeWithText("month:$minimum").assertIsDisplayed()
        compose.onNodeWithText("month:$minimum").performTouchInput { swipeLeft() }
        compose.waitForIdle()
        compose.onNodeWithText("month:${moveSampleMonth(SampleCalendar.Chinese, minimum, 1)}").assertIsDisplayed()

        compose.onNodeWithText("upper").performClick()
        compose.onNodeWithText("month:$maximum").assertIsDisplayed()
        compose.onNodeWithText("month:$maximum").performTouchInput { swipeRight() }
        compose.waitForIdle()
        compose.onNodeWithText("month:${moveSampleMonth(SampleCalendar.Chinese, maximum, -1)}").assertIsDisplayed()
    }

    @Test
    fun agendaRebuildsLabelsWhenTheStyleChangesWithoutADateChange() {
        val style = mutableStateOf(ChineseTextStyle.English)
        compose.setContent {
            MaterialTheme {
                val selection = rememberCalendarSelectionState(TypeSelectDay.Single)
                Column {
                    SampleAgenda(
                        calendar = SampleCalendar.Chinese,
                        anchor = LocalDate.of(2023, 3, 22).toEpochDay(),
                        locale = Locale.ENGLISH,
                        state = selection,
                        hijriProvider = UmmAlQuraHijriDateProvider,
                        chineseStyle = style.value,
                    )
                    Button(onClick = { style.value = ChineseTextStyle.Numeric }) { Text("numeric") }
                    Button(onClick = { style.value = ChineseTextStyle.SimplifiedChinese }) { Text("simplified") }
                    Button(onClick = { style.value = ChineseTextStyle.TraditionalChinese }) { Text("traditional") }
                }
            }
        }

        compose.onNodeWithText("Agenda: 2023 Leap 2nd Month").assertExists()
        compose.onNodeWithText("numeric").performClick()
        compose.onNodeWithText("Agenda: 2023 M2L").assertExists()
        compose.onNodeWithText("simplified").performClick()
        compose.onNodeWithText("Agenda: 2023年闰二月").assertExists()
        compose.onNodeWithText("traditional").performClick()
        compose.onNodeWithText("Agenda: 2023年閏二月").assertExists()
    }

    @Test
    fun gregorianOverlayHeaderAndPagerStepGregorianMonths() {
        val anchor = mutableStateOf(LocalDate.of(2023, 2, 15).toEpochDay())
        compose.setContent {
            MaterialTheme {
                Column {
                    var visible by rememberSaveable { mutableStateOf(anchor.value) }
                    val navigation = rememberSampleMonthNavigation(
                        calendar = SampleCalendar.Chinese,
                        anchor = visible,
                        locale = Locale.ENGLISH,
                        chineseGregorianGrid = true,
                    ) { visible = it }
                    CalendarMonthHeader(
                        title = navigation.title,
                        state = navigation.state,
                        previousMonthContentDescription = "Previous month",
                        nextMonthContentDescription = "Next month",
                    )
                    Text("anchor:${LocalDate.ofEpochDay(visible)}")
                    CalendarSampleViewport(
                        calendar = SampleCalendar.Chinese,
                        anchor = visible,
                        monthCalendar = sampleMonthNavigationCalendar(SampleCalendar.Chinese, true),
                        onVisibleMonthChanged = { visible = it },
                        forcedMode = SampleDisplayMode.HorizontalPager,
                        viewportHeight = androidx.compose.ui.unit.Dp(200f),
                    ) { monthAnchor ->
                        Text("grid:${LocalDate.ofEpochDay(monthAnchor)}")
                    }
                }
            }
        }

        compose.onNodeWithText("February 2023").assertExists()
        compose.onNodeWithContentDescription("Next month").performClick()
        compose.onNodeWithText("March 2023").assertExists()
        compose.onNodeWithText("anchor:2023-03-01").assertExists()

        compose.onNodeWithText("grid:2023-03-01").performTouchInput { swipeLeft() }
        compose.waitForIdle()
        compose.onNodeWithText("April 2023").assertExists()
        compose.onNodeWithText("anchor:2023-04-01").assertExists()
    }

    @Test
    fun unsupportedAnchorFallbackKeepsTheSharedSelection() {
        val today = LocalDate.of(2023, 2, 15).toEpochDay()
        compose.setContent {
            MaterialTheme {
                val selection = rememberCalendarSelectionState(TypeSelectDay.Single)
                val ui = rememberCalendarSampleState()
                Column {
                    Text("selection:${selection.selection.selectedDays.joinToString()}")
                    Button(onClick = { selection.onDayClick(today) }) { Text("select") }
                    Button(onClick = {
                        ui.showMonth(ChineseCalendarEngine.MIN_EPOCH_DAY - 1)
                        ui.selectCalendar(SampleCalendar.Chinese)
                    }) {
                        Text("switchLow")
                    }
                    Text("calendar:${ui.calendarName}")
                    Text("anchor:${ui.anchor}")
                    Text("error:${ui.error ?: "none"}")
                }
            }
        }

        compose.onNodeWithText("select").performClick()
        compose.onNodeWithText("selection:$today").assertExists()
        compose.onNodeWithText("switchLow").performClick()
        compose.onNodeWithText("calendar:Chinese").assertExists()
        compose.onNodeWithText("anchor:${ChineseCalendarEngine.MIN_EPOCH_DAY}").assertExists()
        compose.onNodeWithText("selection:$today").assertExists()
        compose.onNodeWithText("error:", substring = true).assertExists()
    }
}
