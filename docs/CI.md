# CI workflows

Workflows live in `.github/workflows/`. They run on pushes and pull requests for `master`,
`update` and `kmp` (the tablet workflow only pushes on `update` and `kmp`). Library artifacts are
not published, signed or deployed; the documentation workflow can deploy HTML to GitHub Pages.
Verification and publishing jobs use read-only repository permissions.

## Android verification

`.github/workflows/android-verification.yml` runs five independent jobs:

- **assemble**: `:calendarviewsample:assembleDebug assembleAndroidMain
  :calendar-chinese-native:jvmJar :calendar-bom:generatePomFileForCalendarBomPublication`
  followed by `bash scripts/check-commonmain-purity.sh` and `bash scripts/api-check.sh`.
- **unit-tests**: `jvmTest testAndroidHostTest :calendarviewsample:testDebugUnitTest` plus the
  Python suites in `scripts/` and `tools/chinese-calendar/`.
- **native-engine**: `:calendar-chinese-native:linuxX64Test` on the shared `commonTest` fixtures.
- **lint**: `lint` for the six KMP modules and the sample.
- **klib-api**: compiles every iOS arm64 klib and runs `bash scripts/klib-api-check.sh` against
  the checked-in `api/klib/*.abi` snapshots.

The KMP library plugin creates no Android test or lint tasks, so the modules apply
`com.android.lint` and opt in to `withHostTest`/`withDeviceTest`; see
[ADR 0006](decisions/0006-kotlin-multiplatform-calendar-modules.md).

`.github/workflows/docs.yml` runs on pushes to `kmp` and via `workflow_dispatch`. It executes
`:dokkaGenerate`, always uploads the `dokka-html` artifact, and deploys it with
`actions/deploy-pages` only when the repository Pages source reports `build_type: workflow`.
With any other Pages state the artifact is the documented fallback. The job needs `pages: write`
and `id-token: write`, but no library publication credentials.

The `:benchmarks` module is intentionally not part of any CI workflow. Run
`:benchmarks:mainBenchmark` locally for the JVM baseline; its configuration and measured result
are in [BENCHMARKS.md](BENCHMARKS.md).

## Android instrumentation and tablet

`.github/workflows/android-instrumentation.yml` compiles
`:library:assembleAndroidDeviceTest` and `:calendarviewsample:assembleDebugAndroidTest`, starts an
API 29 `google_apis` x86_64 emulator and runs `scripts/run-connected-tests.sh`. The script runs
`:library:connectedAndroidDeviceTest` and `:calendarviewsample:connectedDebugAndroidTest` and
preserves each exit status.

`.github/workflows/android-tablet.yml` runs the same tests on a 1600×2560 240 dpi tablet
emulator after building the sample, both instrumentation APKs, the JVM/host tests and lint.

Both emulator workflows publish failed test cases as public check-run annotations through
`scripts/report-instrumented-failures.py`; the raw logs and uploaded artifacts still require a
signed-in GitHub account.

Instrumentation runs require KVM on the runner and are not executed on every machine; compiling
the device-test sources (`compileAndroidDeviceTest`) works without an emulator.

## Publishing verification

`.github/workflows/android-publishing.yml` publishes the seven modules and the BOM to an isolated
local Maven repository, runs `scripts/verify-publication.sh`, then builds
`tools/consumer-check` (JVM and minified Android release consumers) against that repository. The verifier checks
the KMP layout (root metadata, `<module>-android` AARs, `<module>-iosarm64` /
`<module>-iossimulatorarm64` klibs), the Chinese engine JVM/linuxX64/iosX64 artifacts and the
BOM constraints. The iOS klibs are cross-compiled files only; they are not linked or run on a
device here.

## Environment

JDK 17 (Temurin), the repository Gradle wrapper and the documented SDK packages
(`platform-tools`, `platforms;android-37.0`, `build-tools;36.0.0`). Diagnostics and reports are
uploaded for seven days. A workflow commit is not evidence of a successful run; check the run
for the exact commit in the Actions tab.

## Approved release

`.github/workflows/publish-3-0-0.yml` runs only for a push to `kmp` whose head commit message
contains `release: 3.0.0`. It waits for the named verification, instrumentation, publishing and
KLIB jobs on that exact commit, refuses to move an existing `3.0.0` tag, and creates the GitHub
release and tag with `contents: write`. The workflow must already exist on the branch before the
release commit is pushed.
