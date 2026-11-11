# Sample UI acceptance tests

CalendarSampleUiTest uses createAndroidComposeRule<LauncherActivity> and the actual sample
application. It does not replace the real screen or callbacks with a test fixture.

## Added checks

1. Horizontal pager swipe advances the visible Gregorian month; Next, dialog and mode
   switch use that month; Previous returns to the expected month.
2. Default single-month and all four list/pager modes support requested Next/Previous.
3. Selected date and visible month survive ActivityScenario.recreate of the real launcher.
4. Dialog selection updates the main ISO summary, and Clear removes it.
5. All three calendar controls and four locale controls render the matching engine caption.

Tests assume a normal run not crossing a local midnight/month boundary. UI controls start
with the sample's default English locale, Gregorian calendar and Single selection mode.
Mode names other than Single are unique; Single also names a selection chip, so tests
avoid an ambiguous text selector by checking the default single-month screen first.

These are initial acceptance checks, not complete gesture coverage. Only the horizontal
pager is swiped here; list/vertical pager modes use programmatic navigation assertions.
Calendar captions intentionally use the production data builders: the check validates
UI wiring, not independent correctness of calendar algorithms. Activity recreation is
not process death. Adjacent-day switch semantics, RTL/font scaling/accessibility and
rapid concurrent scroll/navigation cases still need dedicated UI tests.

## CI and failures

The emulator workflow first compiles both core and sample test APKs, then runs all connected
instrumented tests in both modules, including retained templates. Both runtime suites are
attempted even if one fails; any failure leaves the script/job failed. Reports and separate
Gradle logs for both modules plus Logcat are uploaded. The 45-minute timeout is unchanged.
No test is filtered, skipped or marked continue-on-error.

```bash
bash ./gradlew :calendarviewsample:assembleDebugAndroidTest
# Dedicated emulator/device required:
bash ./gradlew :calendarviewsample:connectedDebugAndroidTest
```

Status: these sample tests are included in the 26-test API 29 instrumentation suite. The two
2.1.0 release-tree runs passed on 2026-11-11; exact run IDs are in
[PROJECT_STATUS.md](PROJECT_STATUS.md). If a test fails, inspect its assertion/report rather
than weakening the intended behavior.

## Remaining broader migration work

Release and independent-consumer checks, a fresh CI run for the current commit and the final
module build/test/lint matrix remain open. The legacy feature inventory, obsolete view/XML
removal, dependency isolation, catalog consolidation, sample localization and the main
documentation/agent-rule migration are complete as of
[docs/PROJECT_STATUS.md](PROJECT_STATUS.md).
