package com.develotter.calendarviewsample

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.click as touchClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
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
        compose.onNodeWithText(caption(month)).assertIsDisplayed()
    }

    private fun selectSingleGregorian() {
        click("Gregorian")
        // "Single" appears once for the selection mode and once for the display mode.
        compose.onAllNodesWithText("Single")[0].performClick()
        compose.onAllNodesWithText("Single")[1].performClick()
        compose.waitForIdle()
    }

    private fun scrollMainViewportUp() {
        val page = compose.onNode(
            hasScrollAction() and hasAnyDescendant(hasText("Window month ", substring = true)),
        )
        page.performTouchInput {
            swipe(
                start = Offset(center.x, 300f),
                end = Offset(center.x, 0f),
                durationMillis = 300,
            )
        }
        compose.waitForIdle()
    }

    private fun assertDisplayed(node: SemanticsNodeInteraction, label: String) {
        try {
            node.assertIsDisplayed()
        } catch (error: AssertionError) {
            val bounds = node.fetchSemanticsNode().boundsInRoot
            throw AssertionError("$label is not displayed; boundsInRoot=$bounds", error)
        }
    }

    private fun assertDisplayedInMainViewport(node: SemanticsNodeInteraction) {
        try {
            node.assertIsDisplayed()
            return
        } catch (_: AssertionError) {
            scrollMainViewportUp()
        }
        node.performScrollTo()
        compose.waitForIdle()
        assertDisplayed(node, "Main calendar day")
    }

    private fun visibleMainDay(description: String, selected: Boolean): SemanticsNodeInteraction {
        compose.onAllNodes(isDialog()).assertCountEquals(0)
        val day = compose.onNodeWithContentDescription(description, substring = true)
        assertDisplayedInMainViewport(day)
        day.assertHasClickAction()
        if (selected) day.assertIsSelected() else day.assertIsNotSelected()
        compose.onAllNodes(isDialog()).assertCountEquals(0)
        return day
    }

    private fun englishDescription(date: LocalDate): String =
        date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.ENGLISH))

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
    fun selectingVisibleDayInMainViewportUpdatesSelection() {
        val selected = YearMonth.now().atDay(1)
        val description = englishDescription(selected)
        selectSingleGregorian()
        showViewport()
        val day = visibleMainDay(description, selected = false)
        day.performClick()
        compose.waitForIdle()
        compose.onNodeWithContentDescription(description, substring = true).assertIsSelected()
        compose.onNodeWithText(selected.toString()).assertExists()
    }

    @Test
    fun touchingVisibleDayInMainViewportUpdatesSelection() {
        val selected = YearMonth.now().atDay(1)
        val description = englishDescription(selected)
        selectSingleGregorian()
        showViewport()
        val day = visibleMainDay(description, selected = false)
        day.performTouchInput { touchClick() }
        compose.waitForIdle()
        compose.onNodeWithContentDescription(description, substring = true).assertIsSelected()
        compose.onNodeWithText(selected.toString()).assertExists()
    }

    @Test
    fun selectedDateAndVisibleMonthSurviveActualActivityRecreation() {
        val initial = YearMonth.now()
        val selected = initial.atDay(1)
        val description = englishDescription(selected)
        showViewport()
        // This checks dialog selection and restoration; main-viewport selection is
        // covered separately by the two tests above.
        click("Open dialog")
        compose.onNode(
            hasContentDescription(description, substring = true) and hasAnyAncestor(isDialog()),
        ).performClick()
        clickDialog("Close")
        compose.onNodeWithText(selected.toString()).assertExists()
        visibleMainDay(description, selected = true)
        click("Next")
        assertMonth(initial.plusMonths(1))
        compose.activityRule.scenario.recreate()
        compose.waitForIdle()
        assertMonth(initial.plusMonths(1))
        compose.onNodeWithText(selected.toString()).assertExists()
        click("Previous")
        assertMonth(initial)
        visibleMainDay(description, selected = true)
    }

    @Test
    fun selectingInDialogUpdatesTheMainSelectionSummary() {
        val selected = YearMonth.now().atDay(1)
        val description = selected.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.ENGLISH))
        click("Open dialog")
        compose.onNode(hasContentDescription(description, substring = true) and hasAnyAncestor(isDialog())).performClick()
        clickDialog("Close")
        compose.onNodeWithText(selected.toString()).assertExists()
        click("Clear selection")
        compose.onNodeWithText(selected.toString()).assertDoesNotExist()
    }

    @Test
    fun dialogPagerSwipesToAdjacentMonth() {
        val initial = YearMonth.now()
        click("Open dialog")
        compose.onNode(hasText(caption(initial)) and hasAnyAncestor(isDialog())).assertIsDisplayed()
        compose.onNode(hasText(caption(initial)) and hasAnyAncestor(isDialog()))
            .performTouchInput { swipeLeft() }
        compose.waitForIdle()
        compose.onNode(hasText(caption(initial.plusMonths(1))) and hasAnyAncestor(isDialog())).assertIsDisplayed()
        clickDialog("Close")
    }

    @Test
    fun everyCalendarAndLocaleRendersTheExpectedCaptionContent() {
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
                compose.onNodeWithText(title).assertExists()
            }
        }
    }

    @Test
    fun gregorianCaptionIsDisplayedInSingleMainViewport() {
        val today = LocalDate.now()
        click("en")
        click("Gregorian")
        compose.onAllNodesWithText("Single")[1].performClick()
        compose.waitForIdle()
        val title = gregorianMonthData(YearMonth.from(today), Locale.ENGLISH).title
        showViewport()
        assertDisplayedInMainViewport(compose.onNodeWithText(title))
    }
}
