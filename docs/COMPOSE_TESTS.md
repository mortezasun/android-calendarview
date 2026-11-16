# Compose instrumentation checks

Last verified: 2026-11-16 (post-migration execution for `1a24725`: 42/42 tests passed in both
the tablet and phone workflows with the `junit4.v2` rules)

Historical: the 2.1.0 release-tree runs passed the then 26 tests on API 29 on 2026-11-11; see
[PROJECT_STATUS.md](PROJECT_STATUS.md) for the run IDs.

## Test inventory

The checked-in Android instrumentation suite contains **44 test methods**:
**18 in `:library`** and **26 in `:calendarviewsample`**. The rules come from
`androidx.compose.ui.test.junit4.v2` and therefore use the standard test dispatcher. The source
inventory is:

### `:library`

- `CalendarSelectionComposeTest` (4 tests): cleared-selection restoration, range
  restoration, mode changes and disabled/selected grid-cell semantics.
- `CalendarHeatmapComposeTest` (7 tests): both orientations, click reporting by epoch
  day, RTL weekday mirroring, caller-owned selection, disabled days and the minimum
  touch target for small cells.
- `CalendarMonthHeaderComposeTest` (2 tests): disabled navigation and the optional
  localized today suffix in a day description.
- `CalendarProductViewsComposeTest` (2 tests): week grid and agenda rendering and click
  reporting.
- `CalendarAccessibilityComposeTest` (2 tests): day descriptions plus selected semantics, and
  RTL with a 200% font scale.
- `ExampleInstrumentedTest` (1 test): generated package check.

### `:calendarviewsample`

- `CalendarSampleUiTest` (9 tests): list/pager navigation, dialog selection, dialog
  pager swipe, caption content, caption visibility, activity restoration and the two
  main-viewport selection tests described below.
- `ChineseComposeRegressionTest` (8 tests): Chinese month-state restoration, boundary
  windows in Single/Pager mode, invalid-anchor recovery, Gregorian overlay navigation and
  agenda label rebuilding.
- `CalendarSampleAdaptiveLayoutTest` (4 tests) and `CalendarSampleStateRestorationTest`
  (4 tests): adaptive geometry/fold probes and saved-state restoration.
- `ExampleInstrumentedTest` (1 test): generated package check.

## Main-viewport selection regression

`selectingVisibleDayInMainViewportUpdatesSelection` and
`touchingVisibleDayInMainViewportUpdatesSelection` select Gregorian/Single in the real
launcher, find the first day of the current month in the main viewport, assert that it is
displayed, clickable and not selected, then select it. One test uses the semantics click;
the other uses a real touch click. Each starts from a fresh activity state, so the second
click cannot clear the first selection.

Both tests check the rendered day and the selection summary:

```kotlin
dayNode.assertIsDisplayed()
dayNode.assertHasClickAction()
dayNode.assertIsNotSelected()
// select, then:
dayNode.assertIsSelected()
compose.onNodeWithText(selected.toString()).assertExists()
```

The search uses a marker-tolerant content-description substring. The tests do not open a
dialog and do not set selection state directly.

### Why the first attempt failed

The day was below the small API-29 screen. The main viewport is a 480 dp `Column` inside
the sample's page; `performScrollTo()` on the day scrolls that inner `Column`, not the
outer page. The page therefore still clipped the day. The helper now finds the outer
scroll node by requiring a descendant with the `Window month` label and swipes that page
before the day assertions. This was a test-setup bug, not a production selection bug:
after the outer page scroll, both the semantics click and the touch click update the day
and the summary successfully.

## Dialog restoration remains a separate contract

`selectedDateAndVisibleMonthSurviveActualActivityRecreation` keeps selecting through the
real dialog, checks the shared summary and visible month, calls
`compose.activityRule.scenario.recreate()`, then checks the summary, the visible month
and the rendered `Selected` semantics after navigating back to the selected month.

It proves activity recreation, not process death. The dialog test and the main-viewport
tests intentionally cover different entry paths. The dialog path working does not by
itself prove selection in the main viewport.

## Caption contracts

`everyCalendarAndLocaleRendersTheExpectedCaptionContent` checks the calculated localized
title for all three engines and the `en`, `de`, `fa` and `ar` locales with
`assertExists()`. It does not claim that every pager/list instance displays the title.
`gregorianCaptionIsDisplayedInSingleMainViewport` scrolls to the Single-mode title and
uses `assertIsDisplayed()`; that is the separate visibility contract.

## Emulator workflow

`.github/workflows/android-instrumentation.yml` runs on `master`/`update`/`kmp` pushes, pull
requests targeting those branches and manual dispatch. It uses `ubuntu-24.04`, installs the
documented compile SDK and build tools, compiles both instrumentation APKs, then starts API 29
`google_apis` x86_64 with animations disabled. API 29 is a runtime baseline, not a full
Android matrix.

`scripts/run-connected-tests.sh` runs both `:library:connectedAndroidDeviceTest` and
`:calendarviewsample:connectedDebugAndroidTest`, preserves failure status with
`pipefail`, and dumps Logcat in an EXIT trap. It is the command used by the emulator step.
The workflow has a 45-minute timeout; reports and test APKs are retained for seven days.

The workflow now has a separate summary step that parses the JUnit XML and emits public
check annotations with the exact library and sample test counts. The failure reporter is
limited to a failure of the emulator step itself, so a failure in compilation, artifact
upload or another later step is not mislabelled as a connected-test failure.

## Verification status

The full core and sample instrumentation run **43065898948** for commit
`f7010a58fa59ea6a56836a207f9a27ac92e73efa` completed successfully on API 29. Its
emulator step succeeded, and the two main-viewport tests, the dialog restoration test and
the caption tests were part of the executed suite. That run predates the KMP migration and the
test-count summary step; the checked-in inventory is now 18 library plus 26 sample tests.

After the KMP migration the source inventory is **44 tests (18 library, 26 sample)**. The new
accessibility test (RTL, 200% font scale, selection semantics) is part of the 3.0.0 release gate.
For `1a24725` both emulator workflows passed the then-42 tests with 0 failures, 0 errors and 0
skips:
[tablet](https://github.com/mortezasun/android-calendarview/actions/runs/43836063797) and
[phone](https://github.com/mortezasun/android-calendarview/actions/runs/43836063805). The
preceding `3bb7fdd` runs also passed 42/42. An earlier tablet run
([43824943413](https://github.com/mortezasun/android-calendarview/actions/runs/43824943413))
exposed 12 sample failures caused by a missing `ComponentActivity` declaration; `ui-test-manifest`
now attaches to the sample debug variant and failed cases are published for both workflows.

Remaining limits:

- Activity recreation is covered; real process death is not.
- API 29 on the hosted emulator is not a physical-device acceptance test or a full API,
  locale, form-factor or rotation matrix.
- The main-viewport tests use the actual launcher callbacks and real day nodes, but they
  do not test drag-range selection or a user-opened date picker.
- Pager/list gestures are covered only by the existing navigation and dialog tests.
- The workflow uses action major-version refs, not pinned commit SHAs.

## References

- https://developer.android.com/develop/ui/compose/testing
- https://developer.android.com/develop/ui/compose/testing/common-patterns
- https://github.com/ReactiveCircus/android-emulator-runner
