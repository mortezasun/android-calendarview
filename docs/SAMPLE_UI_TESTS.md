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

Status: on the current uncommitted Kotlin `2.5.0-RC` / Platform `37.1` working tree, the sample
contributes **28 of the 46** checked-in instrumentation tests (18 in `:library`, 28 in
`:calendarviewsample`, adding two tests in `SolarHijriComposeRegressionTest`). Those 46 tests are
compile-verified locally on Linux (`compileAndroidDeviceTest` / `compileDebugAndroidTestKotlin`),
not yet executed on a live emulator. For the tagged `3.0.0` release commit `a0c686e`, all 26 sample
tests then in the suite passed in both emulator workflows alongside the 18 library tests
([tablet](https://github.com/mortezasun/android-calendarview/actions/runs/43854430879),
[phone](https://github.com/mortezasun/android-calendarview/actions/runs/43854430766)). For
`1a24725` all 26 sample tests passed alongside the then-16 library tests. An earlier tablet run
exposed a missing `ComponentActivity` declaration; the debug variant now attaches
`ui-test-manifest`. Historical 2.1.0 results are recorded in [PROJECT_STATUS.md](PROJECT_STATUS.md).
If a test fails, inspect its assertion/report rather than weakening the intended behavior.

## Remaining broader migration work

The CI workflows now target the KMP/JS/Wasm/Apple/Native tasks, and the local build/test/lint/KLIB/
publication matrix passes on Linux. The instrumentation suite ran on API 29 phone and tablet emulators
for the tagged `3.0.0` release (44/44); live emulator execution of the expanded 46-test suite, native
runtime verification on Apple (`ios*`, `macosArm64`), `linuxArm64`, and `mingwX64` hardware, and any
future stable `2.5.0` / `1.13.0` release decision remain open. The legacy feature inventory, obsolete
view/XML removal, dependency isolation, catalog consolidation, sample localization and the main
documentation/agent-rule migration are complete as of [docs/PROJECT_STATUS.md](PROJECT_STATUS.md).
