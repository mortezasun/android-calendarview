package com.develotter.calendarviewsample

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.develotter.calendarview.calendars.solarHijri.compose.rememberSolarHijriMonthState
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.compose.rememberSolarHijriNativeMonthState
import org.junit.Assert.assertNotSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private class FixedSolarHijriToday(private val epochDay: Long) : () -> Long {
    override fun invoke(): Long = epochDay
}

/** Regression tests for Solar Hijri state restoration and updated today-callback wiring. */
@RunWith(AndroidJUnit4::class)
class SolarHijriComposeRegressionTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun solarHijriNativeMonthStateSurvivesRestorationAndUsesReplacedTodayCallback() {
        val firstToday = FixedSolarHijriToday(SolarHijriDate(1404, 3, 10).toEpochDay())
        val secondToday = FixedSolarHijriToday(SolarHijriDate(1404, 4, 10).toEpochDay())
        assertNotSame(firstToday, secondToday)
        val today = mutableStateOf<() -> Long>(firstToday)
        val restoration = StateRestorationTester(compose)
        val targetTitle = rememberExpectedNativeTitle(1404, 2)
        restoration.setContent {
            MaterialTheme {
                Column {
                    val state = rememberSolarHijriNativeMonthState(
                        initialMonth = SolarHijriDate(1404, 1, 1),
                        minMonth = SolarHijriDate(1404, 1, 1),
                        maxMonth = SolarHijriDate(1404, 3, 1),
                        currentEpochDay = today.value,
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
        compose.onNodeWithText("month:$targetTitle").assertExists()

        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("month:$targetTitle").assertExists()

        compose.onNodeWithText("replaceToday").performClick()
        compose.onNodeWithText("canToday:false").assertExists()
        // Replacing the callback instance must not reset the visible navigation state.
        compose.onNodeWithText("month:$targetTitle").assertExists()
    }

    @Test
    fun solarHijriMonthStateSurvivesRestorationAndUsesReplacedTodayCallback() {
        val firstToday = FixedSolarHijriToday(SolarHijriDate(1404, 3, 10).toEpochDay())
        val secondToday = FixedSolarHijriToday(SolarHijriDate(1404, 4, 10).toEpochDay())
        assertNotSame(firstToday, secondToday)
        val today = mutableStateOf<() -> Long>(firstToday)
        val restoration = StateRestorationTester(compose)
        val targetTitle = rememberExpectedNativeTitle(1404, 2)
        restoration.setContent {
            MaterialTheme {
                Column {
                    val state = rememberSolarHijriMonthState(
                        initialMonth = SolarHijriDate(1404, 1, 1),
                        minMonth = SolarHijriDate(1404, 1, 1),
                        maxMonth = SolarHijriDate(1404, 3, 1),
                        currentEpochDay = today.value,
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
        compose.onNodeWithText("month:$targetTitle").assertExists()

        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("month:$targetTitle").assertExists()

        compose.onNodeWithText("replaceToday").performClick()
        compose.onNodeWithText("canToday:false").assertExists()
        compose.onNodeWithText("month:$targetTitle").assertExists()
    }

    private fun rememberExpectedNativeTitle(year: Int, month: Int): String =
        com.develotter.calendarview.calendars.solarHijriNative.SolarHijriLocale.formatMonthYear(year, month)
}
