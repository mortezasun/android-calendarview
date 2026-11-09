package com.develotter.calendarviewsample

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.develotter.calendarview.calendars.gregorian.compose.gregorianMonthData
import com.develotter.calendarview.calendars.hijri.compose.hijriMonthData
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriMonthData
import ir.huri.jcal.JalaliCalendar
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.YearMonth
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** Exercises the real launcher and its production callbacks, not a replacement fixture. */
@RunWith(AndroidJUnit4::class)
class CalendarSampleUiTest {
    @get:Rule
    val compose = createAndroidComposeRule<LauncherActivity>()

    private fun click(label: String) {
        val node = compose.onNodeWithText(label)
        try {
            node.performScrollTo()
        } catch (_: AssertionError) {
            // Node is outside any scrollable container (e.g. dialog action bar)
        }
        node.performClick()
        compose.waitForIdle()
    }

    private fun clickDialog(label: String) {
        compose.onNode(hasText(label) and hasAnyAncestor(isDialog())).performClick()
        compose.waitForIdle()
    }

    private fun caption(month: YearMonth): String = month.format(DateTimeFormatter.ofPattern("LLLL uuuu", Locale.ENGLISH))

    private fun showViewport() {
        compose.onNode(hasText("Window month ", substring = true)).performScrollTo()
        compose.waitForIdle()
    }

    private fun assertMonth(month: YearMonth) {
        showViewport()
        val text = caption(month)
        val node = compose.onNodeWithText(text)
        val bounds = node.fetchSemanticsNode().boundsInWindow
        val rootBounds = compose.onRoot().fetchSemanticsNode().boundsInWindow
        println("TEST_DEBUG: month=$text, bounds=$bounds, rootBounds=$rootBounds")
        node.assertIsDisplayed()
    }

    @Test
    fun swipeThenNextDialogAndModeSwitchUseTheReachedMonth() {
        val initial = YearMonth.now()
        click("HorizontalPager")
        assertMonth(initial)
        compose.onNodeWithText(caption(initial)).performTouchInput { swipeLeft() }
        compose.waitForIdle()
        assertMonth(initial.plusMonths(1))
        click("Next")
        assertMonth(initial.plusMonths(2))
        click("Open dialog")
        compose.onNode(hasText(caption(initial.plusMonths(2))) and hasAnyAncestor(isDialog())).assertIsDisplayed()
        clickDialog("Close")
        click("VerticalList")
        assertMonth(initial.plusMonths(2))
        click("Previous")
        assertMonth(initial.plusMonths(1))
    }

    @Test
    fun everyListAndPagerModeAcceptsProgrammaticNavigation() {
        val initial = YearMonth.now()
        assertMonth(initial)
        for (mode in listOf("VerticalList", "HorizontalList", "HorizontalPager", "VerticalPager")) {
            click(mode)
            assertMonth(initial)
            click("Next")
            assertMonth(initial.plusMonths(1))
            click("Previous")
            assertMonth(initial)
        }
    }

    @Test
    fun selectedDateAndVisibleMonthSurviveActualActivityRecreation() {
        val initial = YearMonth.now()
        val selected = initial.atDay(1)
        val description = selected.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.ENGLISH))
        showViewport()
        compose.onNodeWithContentDescription(description).performClick()
        compose.onNodeWithText(selected.toString()).assertExists()
        click("Next")
        assertMonth(initial.plusMonths(1))
        compose.activityRule.scenario.recreate()
        compose.waitForIdle()
        compose.onNodeWithText(selected.toString()).assertExists()
        assertMonth(initial.plusMonths(1))
    }

    @Test
    fun selectingInDialogUpdatesTheMainSelectionSummary() {
        val selected = YearMonth.now().atDay(1)
        val description = selected.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.ENGLISH))
        click("Open dialog")
        compose.onNode(hasContentDescription(description) and hasAnyAncestor(isDialog())).performClick()
        clickDialog("Close")
        compose.onNodeWithText(selected.toString()).assertExists()
        click("Clear selection")
        compose.onNodeWithText(selected.toString()).assertDoesNotExist()
    }

    @Test
    fun calendarAndLocaleControlsRenderTheMatchingEngineCaption() {
        val today = LocalDate.now()
        for (tag in listOf("en", "de", "fa", "ar")) {
            click(tag)
            val locale = Locale.forLanguageTag(tag)
            for (calendar in SampleCalendar.entries) {
                click(calendar.name)
                showViewport()
                val title = when (calendar) {
                    SampleCalendar.Gregorian -> gregorianMonthData(YearMonth.from(today), locale).title
                    SampleCalendar.Hijri -> hijriMonthData(HijrahDate.from(today), locale).title
                    SampleCalendar.SolarHijri -> solarHijriMonthData(JalaliCalendar(today), locale).title
                }
                compose.onNodeWithText(title).assertIsDisplayed()
            }
        }
    }
}
