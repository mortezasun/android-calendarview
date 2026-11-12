package com.develotter.calendarviewsample

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.develotter.calendarview.compose.rememberCalendarSelectionState
import com.develotter.calendarview.enums.TypeSelectDay
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies that all configuration, selection and display states survive genuine saved-state
 * restoration using Compose's StateRestorationTester (distinct from simple property mutation
 * or process restart assertions).
 */
@RunWith(AndroidJUnit4::class)
class CalendarSampleStateRestorationTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun sampleUiConfigurationSurvivesSavedStateRestoration() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            MaterialTheme {
                val ui = rememberCalendarSampleState()
                Column {
                    Text("calendar:${ui.calendarName}")
                    Text("mode:${ui.modeName}")
                    Text("locale:${ui.localeTag}")
                    Text("rtl:${ui.rtl}")
                    Text("previousDays:${ui.previousDays}")
                    Text("nextDays:${ui.nextDays}")
                    Text("fixedSixRows:${ui.fixedSixRows}")
                    Text("showWeek:${ui.showWeek}")
                    Text("showAgenda:${ui.showAgenda}")
                    Text("showWeekNumbers:${ui.showWeekNumbers}")
                    Text("customStyle:${ui.customStyle}")
                    Text("useHeaderApi:${ui.useHeaderApi}")
                    Text("limitRange:${ui.limitRange}")
                    Text("hijriOffset:${ui.hijriOffset}")
                    Text("anchor:${ui.anchor}")
                    Text("showDialog:${ui.showDialog}")
                    Text("error:${ui.error ?: "none"}")

                    Button(onClick = {
                        ui.selectCalendar(SampleCalendar.Hijri)
                        ui.modeName = TypeSelectDay.Range.name
                        ui.localeTag = "de"
                        ui.rtl = true
                        ui.previousDays = true
                        ui.nextDays = true
                        ui.fixedSixRows = true
                        ui.showWeek = true
                        ui.showAgenda = true
                        ui.showWeekNumbers = true
                        ui.customStyle = true
                        ui.useHeaderApi = true
                        ui.limitRange = true
                        ui.hijriOffset = 2
                        ui.showMonth(12345L)
                        ui.showDialog = true
                    }) {
                        Text("modify")
                    }
                }
            }
        }

        compose.onNodeWithText("calendar:Gregorian").assertExists()
        compose.onNodeWithText("modify").performClick()
        compose.onNodeWithText("calendar:Hijri").assertExists()
        compose.onNodeWithText("mode:Range").assertExists()
        compose.onNodeWithText("locale:de").assertExists()
        compose.onNodeWithText("rtl:true").assertExists()
        compose.onNodeWithText("previousDays:true").assertExists()
        compose.onNodeWithText("nextDays:true").assertExists()
        compose.onNodeWithText("fixedSixRows:true").assertExists()
        compose.onNodeWithText("showWeek:true").assertExists()
        compose.onNodeWithText("showAgenda:true").assertExists()
        compose.onNodeWithText("showWeekNumbers:true").assertExists()
        compose.onNodeWithText("customStyle:true").assertExists()
        compose.onNodeWithText("useHeaderApi:true").assertExists()
        compose.onNodeWithText("limitRange:true").assertExists()
        compose.onNodeWithText("hijriOffset:2").assertExists()
        compose.onNodeWithText("anchor:12345").assertExists()
        compose.onNodeWithText("showDialog:true").assertExists()

        restoration.emulateSavedInstanceStateRestore()

        compose.onNodeWithText("calendar:Hijri").assertTextEquals("calendar:Hijri")
        compose.onNodeWithText("mode:Range").assertTextEquals("mode:Range")
        compose.onNodeWithText("locale:de").assertTextEquals("locale:de")
        compose.onNodeWithText("rtl:true").assertTextEquals("rtl:true")
        compose.onNodeWithText("previousDays:true").assertTextEquals("previousDays:true")
        compose.onNodeWithText("nextDays:true").assertTextEquals("nextDays:true")
        compose.onNodeWithText("fixedSixRows:true").assertTextEquals("fixedSixRows:true")
        compose.onNodeWithText("showWeek:true").assertTextEquals("showWeek:true")
        compose.onNodeWithText("showAgenda:true").assertTextEquals("showAgenda:true")
        compose.onNodeWithText("showWeekNumbers:true").assertTextEquals("showWeekNumbers:true")
        compose.onNodeWithText("customStyle:true").assertTextEquals("customStyle:true")
        compose.onNodeWithText("useHeaderApi:true").assertTextEquals("useHeaderApi:true")
        compose.onNodeWithText("limitRange:true").assertTextEquals("limitRange:true")
        compose.onNodeWithText("hijriOffset:2").assertTextEquals("hijriOffset:2")
        compose.onNodeWithText("anchor:12345").assertTextEquals("anchor:12345")
        compose.onNodeWithText("showDialog:true").assertTextEquals("showDialog:true")
    }

    @Test
    fun selectionStateWithRangeSurvivesSavedStateRestoration() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            MaterialTheme {
                val state = rememberCalendarSelectionState(TypeSelectDay.Range)
                Column {
                    Text("selection:${state.selection.selectedDays.joinToString(",")}")
                    Text("contains_20002:${20002L in state.selection}")
                    Button(onClick = {
                        state.selectRange(20000L, 20004L)
                    }) {
                        Text("selectRange")
                    }
                }
            }
        }
        compose.onNodeWithText("selection:").assertExists()
        compose.onNodeWithText("selectRange").performClick()
        compose.onNodeWithText("selection:20000,20004").assertExists()
        compose.onNodeWithText("contains_20002:true").assertExists()

        restoration.emulateSavedInstanceStateRestore()

        compose.onNodeWithText("selection:20000,20004").assertTextEquals("selection:20000,20004")
        compose.onNodeWithText("contains_20002:true").assertTextEquals("contains_20002:true")
    }

    @Test
    fun displayModeSurvivesSavedStateRestoration() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            MaterialTheme {
                var modeName by rememberSaveable { mutableStateOf(SampleDisplayMode.Single.name) }
                Column {
                    Text("mode:$modeName")
                    Button(onClick = { modeName = SampleDisplayMode.AdaptiveColumns.name }) {
                        Text("switchMode")
                    }
                }
            }
        }
        compose.onNodeWithText("mode:Single").assertExists()
        compose.onNodeWithText("switchMode").performClick()
        compose.onNodeWithText("mode:AdaptiveColumns").assertExists()

        restoration.emulateSavedInstanceStateRestore()

        compose.onNodeWithText("mode:AdaptiveColumns").assertTextEquals("mode:AdaptiveColumns")
    }

    @Test
    fun heatmapConfigurationSurvivesSavedStateRestoration() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            MaterialTheme {
                var year by rememberSaveable { mutableIntStateOf(2026) }
                var orientation by rememberSaveable { mutableStateOf("WeeksAsColumns") }
                var palette by rememberSaveable { mutableStateOf("RedGreen") }
                Column {
                    Text("year:$year")
                    Text("orientation:$orientation")
                    Text("palette:$palette")
                    Button(onClick = {
                        year = 2027
                        orientation = "WeeksAsRows"
                        palette = "Green"
                    }) {
                        Text("changeHeatmap")
                    }
                }
            }
        }
        compose.onNodeWithText("year:2026").assertExists()
        compose.onNodeWithText("changeHeatmap").performClick()
        compose.onNodeWithText("year:2027").assertExists()
        compose.onNodeWithText("orientation:WeeksAsRows").assertExists()
        compose.onNodeWithText("palette:Green").assertExists()

        restoration.emulateSavedInstanceStateRestore()

        compose.onNodeWithText("year:2027").assertTextEquals("year:2027")
        compose.onNodeWithText("orientation:WeeksAsRows").assertTextEquals("orientation:WeeksAsRows")
        compose.onNodeWithText("palette:Green").assertTextEquals("palette:Green")
    }
}
