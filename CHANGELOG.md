# Changelog

## Unreleased (`3.1.0-beta` pre-release preparation; tag pending CI and device verification)

Post-`3.0.0` tooling (`a116b81`) plus preparation for **`3.1.0-beta`** on **Kotlin `2.5.0-RC`**,
**Compose Multiplatform `1.13.0-beta02`**, Android SDK Platform `37.1`, and full multiplatform target
expansion; tag `3.1.0-beta` awaits CI and manual debug-APK verification before release.

### Added

- `CalendarDayView`, `CalendarDayEvent`, `PositionedDayEvent` and `layoutDayEvents`: stateless hour
  timeline in `:library` with minute-based events, overlap columns, a visible hour window, an
  optional "now" line and caller-supplied hour labels.
- `CalendarDateWindow(minEpochDay, maxEpochDay)`: immutable inclusive selectable-day window with
  `contains`, `allows(start, end)` and an `isDateEnabled` adapter, complementing the length-only
  `CalendarRangePolicy` (which is unchanged).
- `ChineseCalendarMonth` data builders and heatmaps accept `locale: CalendarLocale?` for weekday
  labels (`chineseMonthData`, `chineseYearHeatmap`, `chineseMonthHeatmap`); titles and day labels
  still follow `ChineseTextStyle`.
- Keyboard navigation reaches the engine facades: `keyboardNavigation` (default `false`) and
  `onMonthStep` are parameters of all five engine month composables and
  `GregorianChineseCalendarMonth`, forwarded to `CalendarMonthGrid`.
- Selection presets: `CalendarSelection.presets(firstDayOfWeek, todayEpochDay)` returns
  `CalendarSelectionPresets` (`today`, `thisWeek`, `thisMonth`, `lastSevenDays`) on the shared
  epoch-day identity. `thisWeek` honours `firstDayOfWeek`, `thisMonth` is the civil Gregorian
  month and `lastSevenDays` ends on `today`; no platform clock is read and `CalendarRangePolicy`
  is unchanged.
- ISO-8601 week numbers: `isoWeekNumber` / `isoWeekYear` in `:library`, plus an opt-in
  `useIsoWeekNumber` (default `false`, placed before `isDateEnabled`) on every `*WeekData`
  builder. Digits follow the selected language; the Gregorian default keeps its week-based-year
  number and the other engines keep `null` unless the option is enabled.
- Heatmap buckets and legend: `HeatmapBuckets(thresholds)` snaps `colorForIntensity` lookups to
  ascending steps, a `buckets` parameter is available on all three heatmaps, and
  `HeatmapLegend` draws the same steps with caller-supplied labels. Intensities, selection and
  accessibility descriptions keep the exact value.
- `CalendarMonthPicker`: stateless, keyboard-operable and RTL-aware month grid with optional year
  stepping and caller-supplied labels (ADR 0008).
- `CalendarMonthColumns(maxColumns = Int.MAX_VALUE)`: caps the width-derived column count and wraps
  the remaining months into further rows, so a wide window shows a readable number of columns
  instead of stretching the grid. The pure `calendarMonthColumns(width, minMonthWidth, spacing,
  maxColumns)` overload exposes the same rule; the default keeps the previous unbounded layout.
- `:kmp-sample` demonstrates the new APIs: the Calendars page opens a `CalendarMonthPicker` that
  drives its Gregorian month state, and the Heatmaps page renders a `HeatmapLegend` from the same
  `HeatmapBuckets` its heatmap uses. Its Playground forwards keyboard navigation to every engine
  month.
- Caller-selected label language (ADR 0007): `CalendarLocale` in `:library` with `English`, `German`,
  `Persian` and `Arabic`, and an optional `locale: CalendarLocale? = null` on the month titles,
  secondary and accessibility labels, day cells, month and week data, `*MonthState.title()` and the
  month composables of every engine. `null` keeps the platform formatter, so earlier behaviour and
  the `3.0.0` binary-compatibility overloads are unchanged. Labels come from pure Kotlin tables; other
  languages render English labels and Western digits.
- `GregorianChineseCalendarMonth` accepts `locale` for its Gregorian primary layer; the Chinese
  secondary labels stay on `ChineseTextStyle`. The previous signature remains as a hidden
  binary-compatibility overload.
- `CalendarLocale.supportedLanguages`, `CalendarLocale.systemDefault()`, `CalendarLocale.isRightToLeft`,
  the opt-in `ProvideCalendarLocaleLayoutDirection` helper and the JVM/Android extension
  `java.util.Locale.asCalendarLocale()`. Layout direction stays a host decision unless the helper is used.
- Golden and completeness tests for the Gregorian, Hijri, Solar Hijri and Solar Hijri native label
  tables, English-fallback tests, and a string-parity test for `:kmp-sample`.
- Opt-in keyboard navigation on `CalendarMonthGrid`: `keyboardNavigation` (default `false`) and
  `onMonthStep`. Arrow keys move one day or one week in the ambient layout direction, Page Up/Down
  request a month step, Enter/Space activate the focused day, and the focused cell draws a border.
  The grid keeps only a focus position; selection, semantics and the default behaviour are unchanged.
- `:kmp-sample` selects its language from the library locales and passes it to every calendar;
  `:calendarviewsample` shows a language switch in its top bar and drives all engines from the same
  selection, with instrumentation tests whose expectations come from the library tables.
- Shared `appleMain` and `webMain` (`jsMain` / `wasmJsMain`) platform formatters and `currentEpochDay`
  actuals across `:library`, `:calendar-gregorian`, `:calendar-hijri`, `:calendar-solarHijri-native`,
  and `:calendar-chinese-compose`.
- Additional published KMP targets: `jvm()`, `macosArm64()`, `js()` (`browser` + `nodejs`), and
  `wasmJs()` (`browser`) on all six Compose modules; plus `linuxArm64()`, `mingwX64()`,
  `macosArm64()`, `js()`, and `wasmJs()` on `:calendar-chinese-native` (60 artifact directories total).
- `SolarHijriComposeRegressionTest` verifying saved-instance restoration and updated
  `currentEpochDay` wiring for `rememberSolarHijriNativeMonthState` and `rememberSolarHijriMonthState`.
- Dokka `2.3.0` API documentation for all seven published KMP modules, generated by
  `:dokkaGenerate` into `build/dokka/html` and published by the `API documentation` workflow.
- A standalone `:benchmarks` module using kotlinx-benchmark `0.5.0` (JMH `1.37`) for Gregorian and
  Hijri month calculations and their shared heatmap adapters. Benchmarks are not part of `check`,
  `build` or CI.

### Changed

- `:calendarviewsample` uses the library's `Locale.asCalendarLocale()` instead of its own copy.
- `-Xexpect-actual-classes` is scoped to the five modules that declare `internal expect object`
  platform adapters instead of every KMP module, so a new expect/actual class elsewhere still
  reports the Beta warning.
- `:kmp-sample` derives the layout direction from the selected language through
  `ProvideCalendarLocaleLayoutDirection`; the Playground RTL switch remains a narrower override.
- The `:calendarviewsample` top-bar language chips scroll inside a width cap, so a 320 dp app bar
  keeps its title and no chip is clipped.
- ADR 0007 now allows hosts to derive `LayoutDirection` from the language through the opt-in helpers;
  the calendar composables themselves still never change the direction.
- Version prepared as `3.1.0-beta` across `build.gradle.kts`, `:calendarviewsample` (`versionCode`
  `30100`, `versionName` `"3.1.0-beta"`), and `tools/consumer-check` BOM references.
- Updated `:calendarviewsample` header (`KMP 3.1.0-beta · All calendar modules` subtitle and
  `CN Leap` showcase badge replacing the legacy `CN 2.4.0` label).
- Ignored generated `kotlin-js-store/` Yarn lockfiles in `.gitignore`.
- Toolchain raised to Kotlin `2.5.0-RC`, Compose Multiplatform `1.13.0-beta02` (with experimental
  `KOTLIN_ARCHIVE` `.kar` format kept disabled), `compileSdk 37.1` (`platforms;android-37.1`), and
  Gradle wrapper `9.9.1`.
- Configured `binaries.executable()` alongside `useChromeHeadlessNoSandbox()` for JS/Wasm browser tests
  so Compose JS browser suites satisfy `checkComposeUiTestConfigurationForJs`, and declared central
  Node/Yarn/Binaryen Ivy repositories in `settings.gradle.kts` under `FAIL_ON_PROJECT_REPOS`.
- Hardened `scripts/verify-publication.sh`, `scripts/check-commonmain-purity.sh`, and CI workflows
  to validate all 60 Maven publication directories, JS/Wasm Node/Chrome tests, and Apple/ARM/MinGW KLIBs.
- Split [README.md](README.md) into a concise landing overview plus [docs/GETTING_STARTED.md](docs/GETTING_STARTED.md)
  and [docs/USAGE_GUIDE.md](docs/USAGE_GUIDE.md).
- KDoc links that Dokka could not resolve were replaced with inline code; Dokka generation reports
  no unresolved KDoc link warnings.

### Fixed

- `appleMain` actuals now derive `currentEpochDay()` from the local civil day, set explicit local time
  zones and Gregorian/Umm-al-Qura calendar IDs, and anchor `HijriPlatform` consistently at 12:00 UTC.
- `HijriMonthData` / `HijriWeekData` (`HijriPlatform.formatFullDate`) now keep the timeline weekday
  consistent with `weekdayEpochDay` when `OffsetHijriDateProvider` shifts Hijri dates.
- `rememberSolarHijriMonthState` and `rememberSolarHijriNativeMonthState` now capture `currentEpochDay`
  via `rememberUpdatedState` so replacing the provider does not reset visible month navigation.
- Filtered internal platform adapter bytecode names cleanly in `scripts/api-check.sh` and aligned
  platform test assertions and container KDoc.
- `scripts/api-check.sh` also filters and rejects the internal top-level `gregorianOverlayData`
  function, so the JVM dump no longer lists a declaration that is not public API.
- `SampleStringsParityTest` documents each non-English exception (product name, German homographs)
  instead of skipping it without a reason.

### Verification

- `169` `jvmTest` (`168` `commonTest` + `1` JVM benchmark), `152` `testAndroidHostTest`, `62` sample
  unit tests, `168` `jsNodeTest`, `168` `jsBrowserTest`, `16` `:calendar-chinese-native:wasmJsNodeTest`,
  `168` `wasmJsBrowserTest`, `16` `linuxX64Test`, and Python test suites in `scripts/` & `tools/chinese-calendar/`.
- `lint`, sample release APK, Android AARs + engine JVM JAR, `46` compiled instrumentation tests
  (not executed on a live emulator), `api-check`, `klib-api-check` (all Apple/ARM/MinGW KLIBs),
  `commonMain` purity check, 60-module isolated publication verification + `:jvm-consumer:run` +
  `:android-consumer:assembleRelease`, `:dokkaGenerate`, and `:benchmarks:mainBenchmark` (results in
  [docs/BENCHMARKS.md](docs/BENCHMARKS.md)).
- Locale-review and keyboard-navigation session on `kmp`: `208` `jvmTest` and `188`
  `testAndroidHostTest` tests across the seven modules (including the new locale fallback and JVM
  bridge cases), `68` sample unit tests, `2` `:kmp-sample` tests per host runner, `lint` without
  errors, `api-check` and `klib-api-check` after regenerating both surfaces, and
  `check-commonmain-purity.sh`. The new Compose device tests (`CalendarMonthGridKeyboardTest`,
  `CalendarLocaleLayoutDirectionComposeTest`, `SampleLanguageSwitchTest`) are compiled only.
- Library-extension batch (T1-T6) on `kmp`: `245` `jvmTest` and `227` `testAndroidHostTest` tests
  across the seven modules (selection presets, ISO weeks and localized digits, heatmap buckets,
  picker contract and one week-numbering class per engine), `68` sample unit tests, `2`
  `:kmp-sample` host tests, `lint` without errors, both API surfaces regenerated and re-checked,
  and `check-commonmain-purity.sh`. The new Compose device tests (`CalendarMonthPickerComposeTest`,
  `HeatmapLegendComposeTest`) are compiled only because no emulator is available on this host.

## 3.0.0 - 2026-11-16

Kotlin Multiplatform release for Android, the JVM and iOS.

### Breaking changes

- Public callbacks and predicates use epoch days (`Long`), ISO weekday numbers (`Int`) and
  injected `currentEpochDay` providers instead of `java.time` types and `Locale`.
- `YearMonth`, `HijrahDate` and `JalaliCalendar` are replaced by `GregorianYearMonth`,
  `HijriDate` and `SolarHijriDate`; the hidden `java.time` overloads are removed.
- `:calendar-solarHijri` is a facade over `:calendar-solarHijri-native`;
  `ir.huri:jalalicalendar` is no longer used or published.

### Added

- Kotlin Multiplatform source sets, iOS platform actuals, iOS klib publication and a KLIB ABI
  snapshot guard.
- Property tests for Gregorian, Hijri, Solar Hijri and Chinese date round-trips and boundaries.
- Accessibility coverage for day descriptions, RTL and a 200% font scale.
- `commonMain` purity guard and a minified Android published-artifact consumer.

### Verification

- 165 JVM, 148 Android-host, 62 sample and 16 Kotlin/Native tests passed; 44 instrumented tests
  on phone and tablet API 29 emulators gate the release.
- Lint, JVM API guard, KLIB ABI guard, isolated publication verification and JVM/minified
  Android consumers passed. See [3.0.0 release notes](docs/releases/3.0.0.md).

### Known open gates

- iOS runtime/simulator tests and an `iosApp` sample need macOS/Xcode.
- No physical-device, foldable, TalkBack or full locale/form-factor acceptance matrix.
- JitPack is the only configured remote repository; Maven Central upload is not configured.

## 2.4.0 - 2026-11-14

Self-written Chinese lunisolar calendar: a dependency-free Kotlin Multiplatform engine and an
optional Compose facade.

### Added

- `:calendar-chinese-native`: validating `ChineseMonth`/`ChineseDate` values with explicit leap
  months, epoch-day conversion, leap-aware navigation, generated `1901..2099` data, and
  `jvm()`/`linuxX64()` targets with no foreign calendar or Android dependency.
- `:calendar-chinese-compose`: `ChineseCalendarMonth`, `GregorianChineseCalendarMonth`,
  month/week data, saveable state, heatmaps and shared selection/renderer integration.
- Sample playground, BOM/JitPack entries, API dumps and CI verification for both modules.

### Compatibility

- Additive release: existing public API dumps for `library`, `calendar-gregorian`,
  `calendar-hijri`, `calendar-solarHijri` and `calendar-solarHijri-native` are unchanged.
- No existing module depends on the new Chinese modules.

### Verification

- 211 JVM tests (197 Android + 14 KMP), 14 linuxX64 tests, sample build, API guard and
  publication/consumer checks passed; all four workflows were green on `4dc484a`.

### Known open gates

- Real-device/fold/TalkBack/large-font/full-RTL matrix and target-hardware benchmark acceptance
  remain open.
- The AGP 9.5.1 buildscript advisories have no owner/expiry acceptance; the runtime OSV scan
  reported no findings. See `docs/CHINESE_CALENDAR_QUALITY.md`.

## 2.3.0 - 2026-11-12

New dependency-free `:calendar-solarHijri-native` module (Borkowski engine, immutable `SolarHijriDate`, injectable `Clock`).

### Added

- New `:calendar-solarHijri-native` module (`com.develotter.calendarview.calendars.solarHijriNative`) with zero external calendar library dependencies:
  - `SolarHijriEngine`: pure-Kotlin port of the Borkowski (1996) break-table algorithm (`-61..3177` AP, MIT-licensed `jalaali-js` logic, attributed in `NOTICE`), using a single leap-year source (`jalCal`).
  - `SolarHijriDate`: `@Immutable` `Comparable` value type with `plusDays`/`minusDays`/`plusMonths`/`minusMonths` (clamping), `withYear`/`withMonth`/`withDayOfMonth`, `toEpochDay()` matching `LocalDate.toEpochDay()`, and `now(clock: Clock)`.
  - `SolarHijriLocale`: Persian (`fa`), Dari (`fa-AF` / `prs`), and English month names with locale-aware digits.
  - Compose parity: `SolarHijriNativeCalendarMonth`, `SolarHijriNativeMonthData`, `SolarHijriNativeWeekData`, `SolarHijriNativeMonthState` (`rememberSolarHijriNativeMonthState` with `Clock` support and engine-bound clamping), and blank/disabled adjacent-cell guards at `-61-01` and `3177-12`.
- Added `:calendar-solarHijri-native` to `:calendar-bom`, `jitpack.yml`, `scripts/api-check.sh`, CI verification/publishing workflows, and the `calendarviewsample` demo app (`SampleCalendar.SolarHijriNative`).

### Compatibility

- 100% binary-compatible with `2.2.1`, `2.2.0`, `2.1.0`, and `2.0.1`. `:calendar-solarHijri` and its public API (`api/calendar-solarHijri.api`) remain completely unchanged.

## 2.2.1 - 2026-11-12

Adaptive layout hardening, minimum touch targets, fold geometry, state restoration suite and tablet CI.

### Added and Improved

- Month width enforcement (`CalendarSampleMonthWidth.kt`) guaranteeing full 48 dp day cell touch targets (336 dp without week numbers, 376 dp with 40 dp week numbers), verified in `CalendarSampleMonthWidthTest`.
- Adaptive viewport, agenda, and dialog heights (`CalendarSamplePaneHeights.kt`) clamped to prevent clipping in landscape or compact heights, verified in `CalendarSamplePaneHeightTest` and `CalendarSampleSecondaryHeightTest`.
- RTL and insets-aware fold geometry handling (`CalendarSampleFoldGeometry.kt`), verified in `CalendarSampleFoldGeometryTest`.
- Continuous layout and fold transition tests (`CalendarSampleAdaptiveLayoutTest`) validating state continuity across compact, wide, vertical fold, horizontal fold, no fold, week toggle, and calendar engine switches.
- Genuine `SavedInstanceState` restoration tests (`CalendarSampleStateRestorationTest.kt`) using Compose's `StateRestorationTester`, asserting survival of all 17 sample properties and selection range endpoints and interior.
- Automated Android Tablet CI workflow (`android-tablet.yml`) verifying all instrumentation tests on a 1600x2560 240 dpi API 29 tablet emulator.
- Consolidated adaptive documentation (`docs/ADAPTIVE_LAYOUT.md`, `docs/ADAPTIVE_HEIGHTS.md`, `docs/ADAPTIVE_REVIEW_FIXES.md`, `docs/ADAPTIVE_TABLET_CI.md`).

### Compatibility

- 100% binary-compatible with `2.2.0`, `2.1.0` and `2.0.1`. All public/protected JVM APIs across `:library` and all three calendar engines remain unchanged.
- `:library` and the calendar engines remain completely dependency-free of window/fold packages.

## 2.2.0 - 2026-11-12

Adaptive layout, multi-window and foldable release.

### Added

- Adaptive layout and state contract (`docs/ADAPTIVE_LAYOUT.md`) defining month width thresholds
  (336 dp without week numbers, 376 dp with 40 dp week numbers), narrow-container fallbacks,
  and active-month rules.
- Width constraint and monotonicity tests in `CalendarMonthColumnsTest`.
- Adaptive two-pane layout (`CalendarSampleAdaptiveLayout`) switching at 840 dp available width
  (Material expanded window width), placing controls beside calendar content.
- Foldable and hinge-aware posture support via `androidx.window:window:1.5.1` (sample-only),
  supporting vertical book posture and horizontal tabletop posture with separation gaps.
- Adaptive dialog viewport height calculation clamped between 240 dp and 360 dp to prevent
  vertical overflow in landscape and compact height containers.
- Adaptive two-pane layout for `HeatMapViewActivity`.
- Modularized sample architecture: `CalendarSampleState`, `CalendarSampleControls`, `CalendarSampleScreen`.
- State restoration tests (`CalendarSampleStateTest`, `CalendarSampleUiTest`) verifying state
  survival across activity recreation and configuration changes.

### Compatibility

- 100% binary-compatible with `2.1.0`. All public/protected JVM APIs across `:library` and all
  three calendar engines remain unchanged.
- `:library` and the calendar engines remain completely dependency-free of window/fold packages.

## 2.1.0 - 2026-11-11

Released. Tag `2.1.0` points to `bd046a188d9a661e657affe580c7a7fa1a3e98ba`;
the GitHub Release and JitPack tag build are published.

### Added

- `CalendarMonthData`, the calendar-independent month contract implemented by all three engines.
- `CalendarMonthData.toHeatmap()` and the map-based overload, replacing hand-written adapters.
- `CalendarMonthHeader` and the engine month states `GregorianMonthState`, `HijriMonthState`
  and `SolarHijriMonthState`, including saveable visible-month state, inclusive bounds,
  today navigation and per-engine titles.
- `todayDescription` on the month grid and all engine month composables, appending a caller-localized
  *today* label to the accessibility description.
- Heading and grid-collection semantics for month titles and day cells.
- `CalendarWeekGrid`, `CalendarWeekData` and engine `*WeekData` builders for seven-day views.
- `CalendarAgenda` for a bounded vertical day list with selection and markers.
- `CalendarDayMarker`, marker rendering and accessible marker descriptions; optional week-number columns on month and week grids.
- `CalendarMonthColumns` for width-adaptive multi-month layouts.
- `CalendarStyle` / `LocalCalendarStyle` color overrides with MaterialTheme fallbacks.
- `CalendarRangePolicy` and `CalendarSelectionState.selectRange()` for inclusive min/max range limits.
- Dependency-free public JVM API guard: `scripts/api-check.sh`, `api/*.api` and a CI check.

### Compatibility

- Compared the published `2.0.1` JitPack AARs with the release: all 248 public/protected
  JVM members are present with identical descriptors.
- Added hidden delegation overloads for `rememberCalendarSelectionState`,
  `CalendarMonthGrid` and the three engine month composables. They retain the pre-BOM
  parameter lists and default-argument shapes while new source resolves the current APIs.
- The standalone consumer compiles the same `2.0.1`-compatible screen against published
  `2.0.1` and the JitPack `2.1.0` release, and additionally compiles the Hijri provider and
  range-policy APIs.
- The JitPack build publishes the BOM plus `library` and all three engines. A JitPack-only
  consumer resolved all four constrained modules at `2.1.0` without `mavenLocal()`.

### Changed

- `CalendarSelection` has structural equality and is marked `@Immutable`; `CalendarSelectionState`
  is `@Stable` and month/heatmap data classes are `@Immutable`, improving Compose skipping.
- `CalendarSelection.contains` uses an internal set for membership on multiple selections.
- `todayBorderColor` is optional; month, week and agenda views fall back through `LocalCalendarStyle` to MaterialTheme.
- Seven-column month grids now document the 48 dp width caveat for very narrow windows.

### Release verification

- Two complete API 29 instrumentation runs passed on the identical release tree:
  `2eb0fdbc5c1ec9d5cfbeae1570eddd7e9bf37a41` (run 43081287870) and
  `bd046a188d9a661e657affe580c7a7fa1a3e98ba` (run 43082385555).
- Remote verification, JVM tests, lint, publishing verification and the public API guard
  were green for the release commit.
- Tag `2.1.0` and the GitHub Release are published. The JitPack build log reports
  `BUILD SUCCESSFUL` and lists AAR, POM, module metadata and sources for all four modules,
  plus the BOM POM/module metadata. The 2.0.1 tag remains unchanged.
  See [the 2.1.0 release notes](docs/releases/2.1.0.md) for the published summary.

## 2.0.1 - 2026-11-11

Publishing patch for the Compose 2.0 release. No calendar rendering, selection API or
minimum Android version changes are introduced by this patch.

### Fixed

- Invoke the Gradle wrapper through Bash on JitPack instead of relying on its executable bit.
- Explicitly publish the core and all three calendar engines in the JitPack install step.
- Correct release verification documentation and the historical candidate SHA reference.

### Added

- Publishing verification CI with an isolated Maven repository and checks for nonempty
  AAR, POM, Gradle module metadata and sources artifacts for all four modules.

### Release verification

- Release commit `068c57ff90bd8a7e503788a7d951dca65f8f3953` is tagged `2.0.1`; the
  GitHub Release `2.0.1` is published.
- The JitPack build log for the tag
  (`https://jitpack.io/com/github/mortezasun/android-calendarview/2.0.1/build.log`)
  reports `BUILD SUCCESSFUL in 1m` and lists AAR, POM, Gradle module metadata and
  sources artifacts for all four modules.
- An independent consumer project (separate Gradle build, no `mavenLocal()`, no
  `project(...)` or `includeBuild(...)`) built all four configurations from JitPack:
  Gregorian only, Hijri only, Solar Hijri only and all three engines. The shared
  `library:2.0.1` resolved transitively, and the Solar Hijri configuration also resolved
  `ir.huri:jalalicalendar:2.0.0`.
- A runtime smoke test on an API 29 emulator rendered all three calendars and selected
  one day in each (`OK (1 test)`).

The smoke test used a software-emulated x86_64 image. Physical-device acceptance remains
waived; see [the release checklist](docs/RELEASE_CHECKLIST.md).

## 2.0.0 - 2026-11-10

Native Jetpack Compose release of `android-calendarview`. See [release checklist](docs/RELEASE_CHECKLIST.md)
and [project status](docs/PROJECT_STATUS.md) for verification evidence.

### Breaking changes

- Replaced the View/XML/ViewBinding UI with native Jetpack Compose. This is not a
  drop-in upgrade for applications using the 1.x View API.
- Removed `MonthView`, `MultiMonthView`, the legacy adapters, `CalendarStatus`,
  `MonthStatus`, `DayStatus`, the legacy calendar status classes and View-based heatmaps.
- Replaced adapter rendering hooks and XML attributes with composable parameters and
  `header`, `weekdayContent` and `dayContent` slots.
- Date identity and selection use absolute epoch days (`Long`) across calendar systems.
- Calendar engines are separately consumable artifacts with a shared Compose core.
  The Jalali dependency belongs only to the Solar Hijri engine.

See the [1.x to 2.0.0 migration matrix](docs/V2_MIGRATION_MAPPING.md) for replacements
and a migration example.

### Added

- Native `GregorianCalendarMonth`, `HijriCalendarMonth` and `SolarHijriCalendarMonth`.
- Shared stateless `CalendarMonthGrid` and immutable `CalendarSelection`, with
  None, Single, Multiple and Range modes.
- `rememberCalendarSelectionState` with saved-state snapshots for selection restoration.
- Calendar-independent `CalendarMonthList` and `CalendarMonthPager`, supporting both axes.
- Adjacent-month days, fixed six-row grids, configurable headers and weekday labels,
  disabled dates and customizable today-border colors.
- Gregorian quarter/year heatmaps and calendar-independent year heatmaps, with
  orientation, cell style, color scale, slots, disabled dates and caller-owned selection.
- Compose sample screens for navigation, dialogs, calendar switching and heatmap options.
- JVM tests, Compose instrumentation tests and GitHub Actions verification workflows.
- Maven publishing for all four library modules, sources jars and JitPack JDK configuration.

### Artifacts

The intended coordinates for the final tag are:

```text
com.github.mortezasun.android-calendarview:library:2.0.0
com.github.mortezasun.android-calendarview:calendar-gregorian:2.0.0
com.github.mortezasun.android-calendarview:calendar-hijri:2.0.0
com.github.mortezasun.android-calendarview:calendar-solarHijri:2.0.0
```

Each engine includes the shared core transitively. The sample app is not a library artifact.
These coordinates must pass the JitPack release smoke test before availability is claimed.

### Build baseline

The repository verification baseline is JDK 17, Android SDK Platform 37, Build-Tools
36.0.0, AGP 9.5.1, Gradle 9.8.2, Kotlin catalog 2.4.21 with AGP built-in Kotlin, and
Compose BOM 2026.09.00. The modules use `minSdk` 27 and `compileSdk` 37. This baseline
is not a claim that every consumer must use identical plugin versions.

### Behavior changes and known limitations

- Gregorian heatmaps highlight today by default. Pass `today = null` for no today border.
- Heatmap visual cells and interactive slots are separate. With the default minimum
  interactive size, `WeeksAsColumns` reserves at least 48 dp on both axes, subject to
  host constraints. `WeeksAsRows` reserves the minimum height but divides the available
  width between seven columns. Its column width is `(viewport - 6 * cellSpacing) / 7`;
  a 48 dp width requires at least `7 * 48 dp + 6 * cellSpacing`.
- Narrow month-grid layouts do not force horizontal scrolling to guarantee 48 dp columns.
- Saved selection is saved-instance state, not durable storage. Do not infer exhaustive
  real-process-death coverage from activity or Compose restoration tests.
- Manual device acceptance and independent JitPack consumption remain release gates;
  historical or local test results do not prove an untested final commit.
