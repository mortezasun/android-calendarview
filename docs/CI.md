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
- **unit-tests**: `jvmTest testAndroidHostTest :calendarviewsample:testDebugUnitTest jsNodeTest
  jsBrowserTest :calendar-chinese-native:wasmJsNodeTest wasmJsBrowserTest` plus the Python suites
  in `scripts/` and `tools/chinese-calendar/`.
- **native-engine**: `:calendar-chinese-native:linuxX64Test`, plus cross-compilation of
  `:calendar-chinese-native:linuxArm64MainKlibrary` and
  `:calendar-chinese-native:mingwX64MainKlibrary`.
- **lint**: `lint` for the six KMP modules and the sample.
- **klib-api**: cross-compiles Apple KLIBs (`iosArm64MainKlibrary`,
  `iosSimulatorArm64MainKlibrary`, `macosArm64MainKlibrary`, `:calendar-chinese-native:iosX64MainKlibrary`)
  and runs `bash scripts/klib-api-check.sh` against the checked-in `api/klib/*.abi` snapshots.

The KMP library plugin creates no Android test or lint tasks, so the modules apply
`com.android.lint` and opt in to `withHostTest`/`withDeviceTest`; see
[ADR 0006](decisions/0006-kotlin-multiplatform-calendar-modules.md).

`.github/workflows/docs.yml` runs on pushes to `master` and `kmp` and via `workflow_dispatch`. It
executes `:dokkaGenerate`, always uploads the `dokka-html` artifact, and deploys it with
`actions/deploy-pages` only when the repository Pages source reports `build_type: workflow`.
With any other Pages state the artifact is the documented fallback. The job needs `pages: write`
and `id-token: write`, but no library publication credentials. For the post-release tooling run
`43863485535` the build job succeeded and uploaded `dokka-html`; the Pages deploy job was skipped
because the repository Pages source was not `build_type: workflow` and the Pages site returned
HTTP 404. The artifact is therefore the current documentation delivery.

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
`tools/consumer-check` (`:jvm-consumer:run :android-consumer:assembleRelease`) against that
repository. The verifier checks all 60 published artifact directories (root metadata,
`<module>-android` AARs, `<module>-jvm` JARs, `-iosarm64`/`-iossimulatorarm64`/`-macosarm64`/`-js`/
`-wasm-js` KLIBs, plus the Chinese engine's `-linuxx64`/`-linuxarm64`/`-mingwx64`/`-iosx64` KLIBs,
POM dependencies, `.module` `files[]`/`available-at` references, and BOM constraints). The Apple,
Windows, and Linux ARM binaries are cross-compiled only; they are not executed on native hardware
by Linux CI.

## Environment

JDK 17 (Temurin), the repository Gradle wrapper and the documented SDK packages
(`platform-tools`, `platforms;android-37.1`, `build-tools;36.0.0`). Diagnostics and reports are
uploaded for seven days. A workflow commit is not evidence of a successful run; check the run
for the exact commit in the Actions tab.

## Approved release

`.github/workflows/publish-3-0-0.yml` runs only for a push to `kmp` whose head commit message
contains `release: 3.0.0`. It waits for the named verification, instrumentation, publishing and
KLIB jobs on that exact commit, refuses to move an existing `3.0.0` tag, and creates the GitHub
release and tag with `contents: write`. The workflow must already exist on the branch before the
release commit is pushed.
