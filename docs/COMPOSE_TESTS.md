# Compose instrumentation checks

Module: :library
Test class: com.develotter.calendarview.compose.CalendarSelectionComposeTest
Four added tests cover cleared-selection restoration, range restoration, mode changes
and disabled/selected grid-cell semantics. StateRestorationTester simulates Compose
saved state; it does not kill the process or recreate the real sample activity.

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
The four instrumented tests and new emulator setup remain unverified until execution.

Still open: actual emulator results, real activity/process recreation, list/pager gestures
and restoration, window identity, RTL/font scaling/accessibility, additional runtime API
levels, Android calendar conversions and end-to-end sample checks.

References:

- https://developer.android.com/develop/ui/compose/testing
- https://developer.android.com/develop/ui/compose/testing/common-patterns
- https://github.com/ReactiveCircus/android-emulator-runner
