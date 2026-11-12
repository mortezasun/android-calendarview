package com.develotter.calendarviewsample

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.develotter.calendarview.compose.rememberCalendarSelectionState
import com.develotter.calendarview.enums.TypeSelectDay

/** Exercises the real adaptive host with deterministic folds, not a replacement layout. */
@RunWith(AndroidJUnit4::class)
class CalendarSampleAdaptiveLayoutTest {
    @get:Rule
    val compose = createAndroidComposeRule<LauncherActivity>()

    private class Fixture {
        var width by mutableStateOf(360.dp)
        var direction by mutableStateOf(LayoutDirection.Ltr)
        var vertical by mutableStateOf<Boolean?>(null)
        private var origin by mutableStateOf<Offset?>(null)
        val bounds = mutableMapOf<String, Rect>()
        var hingeStart = 0f
        var hingeEnd = 0f

        private fun probe(name: String) = Modifier.onGloballyPositioned {
            val position = it.positionInWindow()
            bounds[name] = Rect(
                position.x, position.y,
                position.x + it.size.width, position.y + it.size.height,
            )
        }

        @Composable
        fun Render() {
            // A virtual wide container exercises expanded layouts even on a phone emulator.
            // Window-coordinate probes, not visibility claims, verify its offscreen geometry.
            CompositionLocalProvider(
                LocalDensity provides Density(1f),
                LocalLayoutDirection provides direction,
            ) {
                Box(Modifier.requiredSize(width, 400.dp).padding(start = 24.dp, top = 32.dp)) {
                    Box(
                        Modifier.fillMaxSize().safeDrawingPadding().onGloballyPositioned {
                            origin = it.positionInWindow()
                        },
                    ) {
                        val position = origin
                        val axis = vertical
                        val fold = if (position != null && axis != null) {
                            val start = (if (axis) position.x else position.y) + 120f
                            hingeStart = start
                            hingeEnd = start + 20f
                            SampleFold(axis, start.dp, 20.dp)
                        } else null
                        CompositionLocalProvider(LocalSampleFoldOverride provides SampleFoldOverride(fold)) {
                            CalendarSampleAdaptiveLayout(
                                header = {
                                    Text(
                                        "Header ${LocalLayoutDirection.current}",
                                        Modifier.fillMaxWidth().height(32.dp).then(probe("header")),
                                    )
                                },
                                content = {
                                    var count by rememberSaveable { mutableStateOf(0) }
                                    Column(Modifier.fillMaxWidth().then(probe("content"))) {
                                        Text("Content ${LocalLayoutDirection.current}")
                                        Button(onClick = { count++ }) { Text("Count $count") }
                                    }
                                },
                                actions = { Text("Actions") },
                            )
                        }
                    }
                }
            }
        }
    }

    private fun install(fixture: Fixture) {
        compose.runOnUiThread {
            compose.activity.setContent { MaterialTheme { fixture.Render() } }
        }
        compose.waitForIdle()
    }

    @Test
    fun verticalFoldKeepsPhysicalPlacementAndCallerDirection() {
        val fixture = Fixture().apply { vertical = true }
        install(fixture)
        for (direction in listOf(LayoutDirection.Ltr, LayoutDirection.Rtl)) {
            compose.runOnIdle { fixture.direction = direction }
            compose.waitForIdle()
            compose.onNodeWithText("Header $direction").assertExists()
            compose.onNodeWithText("Content $direction").assertExists()
            compose.runOnIdle {
                assertTrue(fixture.bounds.getValue("header").right <= fixture.hingeStart + 1f)
                assertTrue(fixture.bounds.getValue("content").left >= fixture.hingeEnd - 1f)
            }
        }
    }

    @Test
    fun horizontalFoldAccountsForTopInsetAndHostOffset() {
        val fixture = Fixture().apply { vertical = false }
        install(fixture)
        compose.runOnIdle {
            assertTrue(fixture.bounds.getValue("content").bottom <= fixture.hingeStart + 1f)
            assertTrue(fixture.bounds.getValue("header").top >= fixture.hingeEnd - 1f)
        }
    }

    @Test
    fun contentStateSurvivesResizeAndFoldPaneChanges() {
        val fixture = Fixture()
        install(fixture)
        compose.onNodeWithText("Count 0").performClick()
        compose.onNodeWithText("Count 1").assertExists()
        compose.runOnIdle { fixture.width = 1000.dp }
        compose.waitForIdle()
        compose.runOnIdle {
            assertTrue(fixture.bounds.getValue("header").right < fixture.bounds.getValue("content").left)
        }
        compose.onNodeWithText("Count 1").assertExists()
        compose.runOnIdle { fixture.width = 360.dp; fixture.vertical = true }
        compose.waitForIdle()
        compose.onNodeWithText("Count 1").assertExists()
        compose.runOnIdle { fixture.vertical = false }
        compose.waitForIdle()
        compose.onNodeWithText("Count 1").assertExists()
        compose.runOnIdle { fixture.vertical = null }
        compose.waitForIdle()
        compose.onNodeWithText("Count 1").assertExists()
        compose.runOnIdle {
            assertTrue(fixture.bounds.getValue("header").bottom < fixture.bounds.getValue("content").top)
        }
    }

    private class CalendarHostFixture {
        var width by mutableStateOf(360.dp)
        var vertical by mutableStateOf<Boolean?>(null)
        var direction by mutableStateOf(LayoutDirection.Ltr)

        @Composable
        fun Render() {
            CompositionLocalProvider(
                LocalDensity provides Density(1f),
                LocalLayoutDirection provides direction,
            ) {
                Box(Modifier.requiredSize(width, 500.dp).padding(start = 24.dp, top = 32.dp)) {
                    val fold = vertical?.let { isVertical ->
                        SampleFold(isVertical, 160.dp, 20.dp)
                    }
                    CompositionLocalProvider(
                        LocalSampleFoldOverride provides SampleFoldOverride(fold),
                    ) {
                        val ui = rememberCalendarSampleState()
                        val state = rememberCalendarSelectionState(TypeSelectDay.Range)
                        CalendarSampleAdaptiveLayout(
                            header = {
                                Column {
                                    Text("Selected:${state.selection.selectedDays.joinToString(",").ifEmpty { "none" }}")
                                    Text("Calendar:${ui.calendar.name}")
                                    Text("Weeks:${ui.showWeekNumbers}")
                                    Button(onClick = { state.selectRange(20000L, 20004L) }) { Text("SelectRange") }
                                    Button(onClick = { ui.move(1) }) { Text("Next") }
                                    Button(onClick = { ui.showWeekNumbers = !ui.showWeekNumbers }) { Text("ToggleWeeks") }
                                    Button(onClick = { ui.selectCalendar(SampleCalendar.Hijri) }) { Text("SwitchHijri") }
                                }
                            },
                            content = {
                                CalendarSampleViewport(
                                    calendar = ui.calendar,
                                    anchor = ui.anchor,
                                    onVisibleMonthChanged = { ui.showMonth(it) },
                                    minMonthWidth = sampleTargetMonthWidth(ui.showWeekNumbers),
                                ) { monthAnchor ->
                                    Text("ViewportMonth:$monthAnchor")
                                }
                            },
                            actions = { Text("Actions") },
                        )
                    }
                }
            }
        }
    }

    @Test
    fun calendarSelectionAndMonthSurviveContinuousLayoutAndFoldTransitions() {
        val fixture = CalendarHostFixture()
        compose.runOnUiThread {
            compose.activity.setContent { MaterialTheme { fixture.Render() } }
        }
        compose.waitForIdle()

        compose.onNodeWithText("Selected:none").assertExists()
        compose.onNodeWithText("Calendar:Gregorian").assertExists()

        compose.onNodeWithText("SelectRange").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Selected:20000,20004").assertExists()

        compose.onNodeWithText("Next").performClick()
        compose.waitForIdle()

        compose.runOnIdle { fixture.width = 1000.dp }
        compose.waitForIdle()
        compose.onNodeWithText("Selected:20000,20004").assertExists()
        compose.onNodeWithText("Next").assertExists()

        compose.runOnIdle { fixture.vertical = true }
        compose.waitForIdle()
        compose.onNodeWithText("Selected:20000,20004").assertExists()
        compose.onNodeWithText("Next").assertExists()

        compose.runOnIdle { fixture.vertical = false }
        compose.waitForIdle()
        compose.onNodeWithText("Selected:20000,20004").assertExists()
        compose.onNodeWithText("Next").assertExists()

        compose.runOnIdle { fixture.vertical = null }
        compose.waitForIdle()
        compose.onNodeWithText("Selected:20000,20004").assertExists()

        compose.onNodeWithText("ToggleWeeks").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Weeks:true").assertExists()
        compose.onNodeWithText("Selected:20000,20004").assertExists()

        compose.onNodeWithText("SwitchHijri").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Calendar:Hijri").assertExists()
        compose.onNodeWithText("Selected:20000,20004").assertExists()

        compose.runOnIdle { fixture.width = 360.dp }
        compose.waitForIdle()
        compose.onNodeWithText("Selected:20000,20004").assertExists()
        compose.onNodeWithText("Calendar:Hijri").assertExists()
    }
}

