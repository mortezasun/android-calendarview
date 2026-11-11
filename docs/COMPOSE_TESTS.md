# Compose instrumentation checks

Module: :library
Test classes:

- com.develotter.calendarview.compose.CalendarSelectionComposeTest
- com.develotter.calendarview.compose.CalendarHeatmapComposeTest
- com.develotter.calendarview.compose.CalendarMonthHeaderComposeTest

Five selection tests cover cleared-selection restoration, range restoration, mode changes
and disabled/selected grid-cell semantics. StateRestorationTester simulates Compose
saved state; it does not kill the process or recreate the real sample activity.

Six heatmap tests cover both orientations, click reporting by epoch day, RTL weekday
mirroring, caller-owned selection semantics, disabled days and the minimum touch target
when `HeatmapStyle.cellSize` is below 48 dp. They render the composables directly, not a
heatmap screen inside the sample app.

Two header tests cover navigation dispatch with disabled bounds and the optional localized
today suffix appended to a day cell's accessibility description. They render the composables
directly and were compiled but not executed in the 2026-11-11 session.

## Emulator workflow

.github/workflows/android-instrumentation.yml runs on update pushes, PRs targeting update
and manual dispatch. It is separate from the existing build/JVM-test/lint workflow.
It uses ubuntu-24.04 explicitly rather than following changes to ubuntu-latest.

The job installs the repository's documented compile SDK/build tools and compiles
:library:assembleDebugAndroidTest before starting an emulator. Compilation failure stops
emulator setup. KVM permissions are configured only on the disposable hosted runner.
reactivecircus/android-emulator-runner@v2 starts API 29 google_apis x86_64 with animations
disabled. API 29 is a first runtime baseline, not a full minimum/latest Android matrix.

scripts/run-connected-tests.sh runs all core connectedDebugAndroidTest tests, including
existing template tests. It preserves failure status through pipefail and collects Logcat
in an EXIT trap before emulator cleanup. Diagnostics are best-effort, not a success signal.
Reports/test APK/logs are retained seven days; missing artifacts only warn. The job has
a 45-minute timeout and may consume Actions minutes and storage. No secrets, write-token
permissions, publishing, deployment or privileged pull_request_target execution are added.
Action refs use major versions, not pinned SHAs.

```bash
bash ./gradlew :library:assembleDebugAndroidTest
# With a dedicated running emulator/device and adb on PATH:
bash scripts/run-connected-tests.sh
```

Do not collect logs from a personal device containing private app data. The script dumps
Logcat from the connected test device. No device logs were collected in this session.

## Verification status

User reported verification run 42630675110 as successful. That report concerns the
existing verification workflow, not this new emulator workflow. The assistant could not
retrieve the linked run and has not independently checked job conclusions or test counts.
The instrumented tests and the emulator setup remain unverified until execution.

Still open: actual emulator results for the selection, heatmap and header classes, real
activity/process recreation, list/pager gestures and restoration, window identity, font
scaling, additional runtime API levels, Android calendar conversions and end-to-end sample
checks.

References:

- https://developer.android.com/develop/ui/compose/testing
- https://developer.android.com/develop/ui/compose/testing/common-patterns
- https://github.com/ReactiveCircus/android-emulator-runner
