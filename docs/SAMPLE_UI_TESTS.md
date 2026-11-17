# Sample UI acceptance tests

CalendarSampleUiTest uses createAndroidComposeRule<LauncherActivity> and the actual sample
application. It does not replace the real screen or callbacks with a test fixture.

## Added checks

1. Home showcase renders all five published calendar artifacts plus live multi-calendar today conversions and navigates directly into the Calendars explorer.
2. Calendars explorer pairs any primary calendar dynamically with secondary-calendar labels (`Gregorian`, `Hijri`, `SolarHijri`, `SolarHijriNative`, `Chinese`) and switches cleanly between `Month`, `Week`, and `Agenda` segmented modes.
3. Integrated Heatmaps tab renders `CalendarYearHeatmap` inside the Material 3 navigation shell.
4. Horizontal pager swipe in Playground advances the visible Gregorian month; Next, dialog and mode switch use that month; Previous returns to the expected month.
5. Default single-month and all four list/pager modes in Playground support requested Next/Previous.
6. Selected date and visible month survive ActivityScenario.recreate of the real launcher.
7. Dialog selection updates the main ISO summary, and Clear removes it.
8. All five calendar controls and four locale controls render the matching engine caption.

Tests assume a normal run not crossing a local midnight/month boundary. UI controls start
on the Home showcase; Playground helpers navigate into the `Playground` tab with default English
locale, Gregorian calendar and Single selection mode. Mode names other than Single are unique;
Single also names a selection chip, so tests avoid an ambiguous text selector cleanly.

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

Status: on the current Kotlin `2.5.0-RC` / Platform `37.1` branch, the sample contributes
**31 of the 52** checked-in instrumentation tests (21 in `:library`, 31 in `:calendarviewsample`).
Those 52 tests are compile-verified locally on Linux (`compileAndroidDeviceTest` /
`compileDebugAndroidTestKotlin`), awaiting CI emulator execution. For the tagged `3.0.0` release
commit `a0c686e`, all 26 sample tests then in the suite passed in both emulator workflows alongside
the 18 library tests
([tablet](https://github.com/mortezasun/android-calendarview/actions/runs/43854430879),
[phone](https://github.com/mortezasun/android-calendarview/actions/runs/43854430766)). For
`1a24725` all 26 sample tests passed alongside the then-16 library tests. An earlier tablet run
exposed a missing `ComponentActivity` declaration; the debug variant now attaches
`ui-test-manifest`. Historical 2.1.0 results are recorded in [PROJECT_STATUS.md](PROJECT_STATUS.md).
If a test fails, inspect its assertion/report rather than weakening the intended behavior.

## Remaining broader migration work

The CI workflows now target the KMP/JS/Wasm/Apple/Native tasks, and the local build/test/lint/KLIB/
publication matrix passes on Linux. The instrumentation suite ran on API 29 phone and tablet emulators
for the tagged `3.0.0` release (44/44); live emulator execution of the expanded 52-test suite, native
runtime verification on Apple (`ios*`, `macosArm64`), `linuxArm64`, and `mingwX64` hardware, and any
future stable `2.5.0` / `1.13.0` release decision remain open. The legacy feature inventory, obsolete
view/XML removal, dependency isolation, catalog consolidation, sample localization and the main
documentation/agent-rule migration are complete as of [docs/PROJECT_STATUS.md](PROJECT_STATUS.md).
