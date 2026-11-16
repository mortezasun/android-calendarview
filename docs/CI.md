# CI workflows

Workflows live in `.github/workflows/`. They run on pushes and pull requests for `master`,
`update` and `kmp` (the tablet workflow only pushes on `update` and `kmp`). Nothing is
published, signed or deployed; every job has read-only repository permissions.

## Android verification

`.github/workflows/android-verification.yml` runs four independent jobs:

- **assemble**: `:calendarviewsample:assembleDebug assembleAndroidMain
  :calendar-chinese-native:jvmJar :calendar-bom:generatePomFileForCalendarBomPublication`
  followed by `bash scripts/api-check.sh`.
- **unit-tests**: `jvmTest testAndroidHostTest :calendarviewsample:testDebugUnitTest` plus the
  Python suites in `scripts/` and `tools/chinese-calendar/`.
- **native-engine**: `:calendar-chinese-native:linuxX64Test` on the shared `commonTest` fixtures.
- **lint**: `lint` for the six KMP modules and the sample.

The KMP library plugin creates no Android test or lint tasks, so the modules apply
`com.android.lint` and opt in to `withHostTest`/`withDeviceTest`; see
[ADR 0006](decisions/0006-kotlin-multiplatform-calendar-modules.md).

## Android instrumentation and tablet

`.github/workflows/android-instrumentation.yml` compiles
`:library:assembleAndroidDeviceTest` and `:calendarviewsample:assembleDebugAndroidTest`, starts an
API 29 `google_apis` x86_64 emulator and runs `scripts/run-connected-tests.sh`. The script runs
`:library:connectedAndroidDeviceTest` and `:calendarviewsample:connectedDebugAndroidTest` and
preserves each exit status.

`.github/workflows/android-tablet.yml` runs the same tests on a 1600×2560 240 dpi tablet
emulator after building the sample, both instrumentation APKs, the JVM/host tests and lint.

Instrumentation runs require KVM on the runner and are not executed on every machine; compiling
the device-test sources (`compileAndroidDeviceTest`) works without an emulator.

## Publishing verification

`.github/workflows/android-publishing.yml` publishes the seven modules and the BOM to an isolated
local Maven repository, runs `scripts/verify-publication.sh`, then builds
`tools/consumer-check` (JVM and Android consumers) against that repository. The verifier checks
the KMP layout (root metadata, `<module>-android` AARs, `<module>-iosarm64` /
`<module>-iossimulatorarm64` klibs), the Chinese engine JVM/linuxX64/iosX64 artifacts and the
BOM constraints. The iOS klibs are cross-compiled files only; they are not linked or run on a
device here.

## Environment

JDK 17 (Temurin), the repository Gradle wrapper and the documented SDK packages
(`platform-tools`, `platforms;android-37.0`, `build-tools;36.0.0`). Diagnostics and reports are
uploaded for seven days. A workflow commit is not evidence of a successful run; check the run
for the exact commit in the Actions tab.
