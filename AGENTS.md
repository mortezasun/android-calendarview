# AGENTS.md

Binding working rules for coding agents in this repository. Read this file and the linked
documents before changing any code.

## Current work: Kotlin Multiplatform `3.1.0-beta` (`2.5.0-RC` multi-target expansion)

`:library` and the calendar modules were released as Kotlin Multiplatform `3.0.0` at commit
`a0c686e`, with `kmp` and `master` both synced to `a116b81` (`3.0.0` plus Dokka/benchmark tooling).
The active `3.1.0-beta` branch line prepares the full multiplatform target expansion (`Android`,
`JVM`, `iOS`, `macOS`, `JS`, `Wasm`, plus `Linux`/`Windows` targets on `:calendar-chinese-native`)
on **Kotlin `2.5.0-RC`** and **Compose Multiplatform `1.13.0-beta02`** against `compileSdk 37.1`.
Read [ADR 0006](docs/decisions/0006-kotlin-multiplatform-calendar-modules.md) for the API design and
[docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md) for verified vs. open items. Apple/Windows/`linuxArm64`
hardware runtime verification, live API-29 emulator execution of the updated 46-test suite, Maven
Central credentials, and real process-death/hardware acceptance remain open; do not report them as
done.

The engine must stay self-written pure Kotlin and usable without Compose, Android or other
calendar engines; the separate Compose facade may reuse `:library`. Do not copy/port external
calendar code or data tables, add an ICU runtime wrapper, or claim unverified platform support.
Do not move the `2.4.0` or `3.0.0` tags or rewrite published history; future releases need
separate approval.

## Read first

1. [README.md](README.md) - overview and links to [Getting Started](docs/GETTING_STARTED.md) and
   [Usage Guide](docs/USAGE_GUIDE.md).
2. [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) - modules, rendering pipeline and state handling.
3. [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) - patterns for typical changes.
4. [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md) - last verified state, known issues, next steps.

Check `git status` and `git branch --show-current` before editing. Preserve uncommitted work that
is not part of your task and never reset, stash or overwrite it without explicit permission.

## Project at a glance

- Multi-module **Kotlin Multiplatform library** project:
  - Compose/UI modules (`:library`, `:calendar-gregorian`, `:calendar-hijri`, `:calendar-solarHijri`,
    `:calendar-solarHijri-native`, `:calendar-chinese-compose`) target `android`, `jvm`, `iosArm64`,
    `iosSimulatorArm64`, `macosArm64`, `js` (`browser` + `nodejs`), and `wasmJs` (`browser`).
  - Pure KMP engine (`:calendar-chinese-native`) targets `jvm`, `linuxX64`, `linuxArm64`, `mingwX64`,
    `iosX64`, `iosArm64`, `iosSimulatorArm64`, `macosArm64`, `js` (`browser` + `nodejs`), and `wasmJs`
    (`browser` + `nodejs`).
  - `:calendar-bom` constrains all 7 published modules (60 published artifact directories total);
    `:calendarviewsample` is the Android demo app; `:benchmarks` is a standalone JVM performance
    module outside the published set.
- Shared code lives in `commonMain`; `jvmMain`/`androidMain` hold platform hooks and conversions,
  `appleMain` holds shared Apple actuals (`iOS`/`macOS`), and `webMain`/`jsMain`/`wasmJsMain` hold
  JS/Wasm browser/Node formatters.
- Public API lives in `com.develotter.calendarview.*`; calendar-specific types live in
  `com.develotter.calendarview.calendars.*`.
- Toolchain: Kotlin `2.5.0-RC`, Compose Multiplatform `1.13.0-beta02`, AGP `9.5.1`, Gradle `9.9.1`,
  Compose BOM `2026.09.00`, `compileSdk 37.1` (`platforms;android-37.1`), `minSdk` 27, Java 17.
- UI is **100% Jetpack Compose**. There are no XML layouts, no ViewBinding, no custom view
  measurement, no ViewModel/Flow, no DI framework, no network and no persistence layer.

## Architecture conventions

- `CalendarMonthGrid` is the stateless rendering core: title, seven weekday labels,
  `firstDayOffset` and `CalendarDayCell` items. The engine composables wrap it; they do not
  reimplement the grid.
- Day identity is an epoch day (`Long`) everywhere. Never key selection or callbacks by formatted
  dates or calendar-specific objects.
- `CalendarSelection` is an immutable value. `rememberCalendarSelectionState` owns it with
  `rememberSaveable` and a `CalendarSelectionSnapshot` saver; range interiors are computed, never
  enumerated. Do not replace this with `StateFlow`, LiveData or a ViewModel.
- `CalendarMonthList` and `CalendarMonthPager` are calendar-independent containers: a month count
  plus a month-content lambda. They read the ambient `LayoutDirection` and `LazyListState` /
  `PagerState` supplied by the caller.
- Heatmaps are built from `heatmapQuarterData` (Gregorian) or caller-built `HeatmapMonthData`.
  All public heatmaps share one private `HeatmapBlock`/`HeatmapCell` renderer and `HeatmapStyle`.
- Public composables are **controlled and stateless**. The caller owns selection, navigation and
  the visible month; the library only renders and reports clicks. Do not add internal mutable
  state for values the caller already owns.
- Every engine month snapshot implements `CalendarMonthData`; `toHeatmap()` is the shared
  month-to-heatmap adapter. Month arithmetic and saveable visible-month state live in the engine
  modules (`*MonthState.kt`); `CalendarMonthNavigationState` and `CalendarMonthHeader` stay
  calendar-independent in `:library`.
- Marker, week, agenda and adaptive-month APIs are calendar-independent product views in
  `:library`. `CalendarWeekData` is implemented by each engine's `*WeekData`; range validation
  belongs in `CalendarRangePolicy`, while drag gestures and picker dialogs stay host concerns.
- The Hijri method hook is engine-local: `HijriDateProvider` maps epoch days to `HijriDate` and
  back. Keep the Umm-al-Qura default and forward a caller-supplied provider through month data,
  week data, `HijriCalendarMonth` and the provider-aware `HijriMonthState`/`rememberHijriMonthState`;
  never put mutable provider state in `:library`.
- Public composable signatures are API. Add options as defaulted parameters before the trailing
  `dayContent` (and, for heatmaps, `colorForIntensity`) lambda so existing calls keep compiling.
  Do not rename or remove parameters as a side effect of another change.
- Heatmap cells keep a touch target of at least `LocalMinimumInteractiveComponentSize` even when
  `HeatmapStyle.cellSize` is smaller: both edges in `WeeksAsColumns`, the height in `WeeksAsRows`,
  which shares the row width between seven columns. Do not move click handling onto the shrunken
  visual cell.

## Verified commands

These commands pass on Linux with OpenJDK `17.0.21`, Android SDK Platform `37.1`
(`platforms;android-37.1`), headless Chrome, Node.js/Yarn, and Gradle `9.9.1`:

```bash
bash ./gradlew :calendarviewsample:assembleDebug       # builds library + all calendar modules
bash ./gradlew jvmTest                                 # 169 KMP JVM tests across all 7 modules
bash ./gradlew testAndroidHostTest                     # 152 shared tests on the Android host
bash ./gradlew :calendarviewsample:testDebugUnitTest   # 62 sample unit tests
bash ./gradlew jsNodeTest jsBrowserTest                # 168 JS tests per runner across all 7 modules
bash ./gradlew :calendar-chinese-native:wasmJsNodeTest # 16 Wasm tests on Node.js
bash ./gradlew wasmJsBrowserTest                       # 168 Wasm tests in headless Chrome across all 7 modules
bash ./gradlew :calendar-chinese-native:linuxX64Test   # 16 Kotlin/Native Linux x64 tests
bash ./gradlew lint                                    # Android Lint, all six KMP modules + sample
bash ./gradlew :library:compileAndroidDeviceTest       # 18 Compose UI tests (compile only, no emulator)
bash ./gradlew :calendarviewsample:compileDebugAndroidTestKotlin # 28 sample instrumentation tests (compile only)
bash ./gradlew :calendarviewsample:assembleRelease     # unsigned release APK
bash ./gradlew assembleAndroidMain :calendar-chinese-native:jvmJar  # AARs + engine JVM jar
bash scripts/check-commonmain-purity.sh                # commonMain purity check
bash scripts/api-check.sh                              # public JVM API diff against api/*.api
bash ./gradlew iosArm64MainKlibrary iosSimulatorArm64MainKlibrary macosArm64MainKlibrary \
  :calendar-chinese-native:linuxArm64MainKlibrary :calendar-chinese-native:mingwX64MainKlibrary \
  :calendar-chinese-native:iosX64MainKlibrary          # cross-compile all Apple/ARM/Windows KLIBs
bash scripts/klib-api-check.sh                         # KLIB ABI diff against api/klib/*.abi
bash ./gradlew :dokkaGenerate                          # HTML docs in build/dokka/html
bash ./gradlew :benchmarks:mainBenchmark               # JVM benchmark; not in check/CI
bash ./gradlew :library:publishToMavenLocal :calendar-gregorian:publishToMavenLocal \
  :calendar-hijri:publishToMavenLocal :calendar-solarHijri:publishToMavenLocal \
  :calendar-solarHijri-native:publishToMavenLocal :calendar-chinese-native:publishToMavenLocal \
  :calendar-chinese-compose:publishToMavenLocal \
  :calendar-bom:publishToMavenLocal -Dmaven.repo.local=/tmp/m2-verify
bash scripts/verify-publication.sh /tmp/m2-verify 3.1.0-beta
bash ./gradlew -p tools/consumer-check :jvm-consumer:run :android-consumer:assembleRelease \
  -PconsumerRepo=/tmp/m2-verify
```

Single-module compile checks, for example `bash ./gradlew :library:compileAndroidMain`, are
acceptable for smaller changes. Native runtime tests on iOS/macOS (`ios*`, `macosArm64`),
`linuxArm64`, and `mingwX64` cannot run on an x86_64 Linux VM. Report any command that could not be
run and why.

### Build conventions

- KMP modules use the `android {}` target block and the explicit `org.jetbrains.compose.*`
  dependency coordinates from the catalog; the plugin's deprecated `androidLibrary`/`compose.*`
  accessors must not come back.
- Android KMP modules apply `com.android.lint`: `com.android.kotlin.multiplatform.library`
  provides no lint tasks of its own.
- They opt in to `withHostTest {}` and `withDeviceTest {}`; `src/androidTest/java` is mapped into
  `androidDeviceTest`. The Compose resource copy task for device tests is disabled centrally in
  the root build because Compose Multiplatform `1.13.0-beta02` leaves its output unconfigured.
- Compose modules declare `useChromeHeadlessNoSandbox()` and `binaries.executable()` for JS/Wasm
  browser tests; `:calendar-chinese-native` declares `useChromeHeadlessNoSandbox()` for both JS and
  Wasm browser tests. Root `build.gradle.kts` clears per-plugin Node/Yarn/Binaryen Ivy `downloadBaseUrl`
  values so `settings.gradle.kts` central repositories respect `FAIL_ON_PROJECT_REPOS`.

## Rules for changes

- **English only:** all code, comments, KDoc, commit messages, and documentation (including
  `README.md`, `RELEASE_PLAN_*.md`, ADRs, specs, quality reports, and release notes) must be
  written exclusively in English.
- **Minimal invasive:** change only the files required by the task. No drive-by refactoring, no
  reformatting of untouched code, no renames.
- **No new dependencies** without explicit approval. Prefer what is already in
  `gradle/libs.versions.toml`.
- **No placeholder code.** Do not add `TODO: implement` stubs or leave code paths incomplete.
- **Do not delete existing code** or public API without explicit permission.
- Keep the existing style: 4-space indentation, KDoc in English, trailing commas where the file
  already uses them, package layout under `src/commonMain/kotlin` or the platform source set.
- Do not edit generated or build output (`build/`, `.gradle/`, `.kotlin/`, generated bindings).
- Do not weaken tests, lint or build settings to make a check pass.
- No secrets, API keys or machine-specific paths in tracked files. `local.properties` stays
  untracked.
- Git: commit only on request and only the files belonging to the task. Never force-push, never
  merge into `master`/`main` without explicit user approval, never rewrite published history.

## When something is unclear

1. Inspect the code and the documents above first.
2. If the behaviour is still ambiguous, ask one focused question with the concrete options.
3. Do not invent product rules or historical rationale. If a reason cannot be verified from the
   repository, say so and document the open question instead of guessing.
4. Bugs discovered while working are reported in [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md)
   and in the final summary; do not fix them silently in an unrelated change.

## Documentation to update

| Change | Update |
|--------|--------|
| Public feature, usage, installation | `README.md` |
| New/removed module, rendering or state-flow change | `docs/ARCHITECTURE.md` |
| Toolchain, SDK, build or test setup | `docs/SETUP.md` |
| New recurring implementation pattern | `docs/DEVELOPMENT.md` |
| Any completed session or verified state change | `docs/PROJECT_STATUS.md` |
| Public API change | `api/*.api` via `bash scripts/api-check.sh --update`, plus README/KDoc |
| Irreversible or structural architecture decision | new file in `docs/decisions/` |

Comments explain local *why*, documents explain the overview. Do not duplicate the same
explanation in both places.

## Definition of done

- The requested behaviour is implemented completely; no placeholder code remains.
- Public API changes have an updated checked-in `api/*.api` dump.
- The smallest relevant build/compile check passes; tests and lint were run when they cover the
  change, or their absence is reported explicitly.
- Documentation and KDoc affected by the change are updated.
- `git diff` contains only intended changes; no formatting noise, no unrelated files.
- The final summary lists: changed files, executed checks with results, and known limitations.
