# Project status

Last updated: 2026-11-24 (UTC)
Version: `3.1.0-beta` pre-release preparation (latest tagged release: `3.0.0` at `a0c686e`; `master`/`kmp` base `a116b81`)
Branches: `master` and `kmp` synced at `c8b1287` (following `e5e7c73`); `3.1.0-beta` tag and GitHub Pre-Release await manual debug APK verification and explicit release approval.

## First-day-of-week unification & KMP sample configurability (`kmp`, 2026-11-24)

- **Shared `:library` API ([ADR 0009](decisions/0009-first-day-of-week-alignment-api.md)):** Added `requireValidFirstDayOfWeek`, `firstDayOffset`, `weekStartEpochDay`, `orderedIsoWeekdays`, and `weekdayLabelsStartingAt` in `com.develotter.calendarview.selection` (`CalendarWeekStart.kt`).
- **Engine delegation:** `CalendarSelectionPresets`, `:calendar-gregorian`, `:calendar-hijri`, `:calendar-solarHijri-native` (and `:calendar-solarHijri` facade), and `:calendar-chinese-compose` delegate week-start, offset, and weekday-label rotation to `:library`, preserving all existing engine defaults and keeping `:calendar-chinese-native` pure and independent.
- **KMP sample (`:kmp-sample`):** First day of week (`Calendar default` + ISO `Monday`..`Sunday`) is selectable across all 5 demo calendars (`Gregorian`, `Hijri`, `SolarHijri`, `SolarHijriNative`, `Chinese`) on Playground, Preview Dialog, Calendars, and Heatmaps, with `rememberSaveable` state persistence.
- **Verification:** `jvmTest`, `:kmp-sample:desktopTest`, `testAndroidHostTest`, `:calendarviewsample:testDebugUnitTest`, `jsNodeTest`, `jsBrowserTest`, `:calendar-chinese-native:wasmJsNodeTest`, `wasmJsBrowserTest`, `:calendar-chinese-native:linuxX64Test`, `lint`, `:calendarviewsample:compileDebugAndroidTestKotlin`, `check-commonmain-purity.sh`, `api-check.sh --update`, and `klib-api-check.sh --update` all passed locally on Linux.

## Library extension batch (T1-T6, `kmp`, 2026-11-20)

Status: implemented and verified locally on Linux; not committed, tagged, merged or run in CI.
Step 0 of the plan (commit the locale review and the F1 work, then wait for CI) is still open
because this host has no repository credentials and GitHub Actions logs are not readable
anonymously; the features below therefore sit on top of an uncommitted working tree.

- T1 keyboard forwarding: `keyboardNavigation` and `onMonthStep` are now parameters of
  `GregorianCalendarMonth`, `HijriCalendarMonth`, `SolarHijriCalendarMonth`,
  `SolarHijriNativeCalendarMonth`, `ChineseCalendarMonth` and `GregorianChineseCalendarMonth`,
  placed before the trailing `dayContent` slot. The hidden 3.0.0 overloads are untouched.
- T2 selection presets: `CalendarSelection.presets(firstDayOfWeek, todayEpochDay)` and
  `CalendarSelectionPresets` (`today`, `thisWeek`, `thisMonth`, `lastSevenDays`). `thisWeek`
  honours `firstDayOfWeek`, `thisMonth` is the civil Gregorian month, `lastSevenDays` ends on
  `today`. No platform date access and no change to `CalendarRangePolicy`.
- T3 ISO week numbers: `isoWeekNumber` and `isoWeekYear` in `:library` `selection`; every
  `*WeekData` builder takes `useIsoWeekNumber` (default `false`) before `isDateEnabled`, so
  trailing-lambda calls keep compiling. Digits come from the engine label tables and the Chinese
  builder writes Western digits. The Solar Hijri facade now forwards the native `weekNumber` it
  previously dropped.
- T4 heatmap buckets and legend: `HeatmapBuckets`, a `buckets` parameter on all three heatmaps
  (before `colorForIntensity`) and `HeatmapLegend`. Drawn colors use the bucket's lower bound;
  intensities, selection and accessibility text keep the exact value.
- T5 month picker: `CalendarMonthPicker` and ADR 0008. One year of `selectable` month cells with
  `CollectionInfo`/`CollectionItemInfo`, optional year stepping, no internal state and
  caller-supplied labels.
- T6 adaptive column cap: `CalendarMonthColumns(maxColumns = Int.MAX_VALUE)` caps the derived
  column count and wraps the remaining months into further rows; `calendarMonthColumns(width,
  minMonthWidth, spacing, maxColumns)` exposes the same rule as a pure 4-argument overload, and
  the internal `calendarMonthRows` helper is covered in `commonTest`. The default keeps the
  previous unbounded layout, so existing `LazyGridState` positions are unchanged. No new
  dependency and no foldable-window API: only the column cap.
- Samples: `:kmp-sample` drives its Gregorian month from `CalendarMonthPicker` (jump-to-month
  button, year stepping, localized month titles) and renders `HeatmapLegend` from the same
  `HeatmapBuckets` its heatmap uses; the new labels go through `SampleStrings` and its parity
  test. The Playground forwards `keyboardNavigation`/`onMonthStep` to every engine month.
- Tests: `CalendarSelectionPresetsTest`, `CalendarWeekNumberingTest`, `HeatmapBucketsTest`,
  `CalendarMonthPickerContractTest`, extended `CalendarMonthColumnsTest` (cap, wrap rows across
  month counts and differing day counts, unchanged default, invalid caps), one week-numbering
  test class per engine module, and `CalendarMonthPickerComposeTest` / `HeatmapLegendComposeTest`
  (compiled only).
- Verification (one pass, `-Duser.language=en -Duser.country=US`): `245` `jvmTest`, `227`
  `testAndroidHostTest`, `68` sample unit tests, `2` `:kmp-sample` host tests, `lint`,
  `api-check` and `klib-api-check` without `--update`, `check-commonmain-purity.sh`,
  `:library:compileAndroidDeviceTest`, `:calendarviewsample:compileDebugAndroidTestKotlin`,
  `:calendarviewsample:assembleDebug`, `:kmp-sample:compileKotlinDesktop` and
  `:kmp-sample:compileAndroidMain`.
- Open: step 0 (commit + CI) remains with the user. Foldable-window/windowing integration is not
  implemented: it would need a new dependency and an explicit product decision, so only the
  `maxColumns` cap from step 6 is in place.

## Locale review acceptance and keyboard navigation (`kmp`, 2026-11-19)

Status: review points R1-R10 addressed and the next feature (opt-in keyboard navigation in
`CalendarMonthGrid`) implemented as an additive `3.1` API. Verified locally on Linux; the CI run for
the new commit is still pending because this host has no repository credentials.

### R1 - CI failure on `ddb482c`

- `ddb482c` contains only `.github/workflows/*` renames (10 files, no content change), so its build
  inputs are identical to its parent `cc5087c`.
- [Run 44625861323](https://github.com/mortezasun/android-calendarview/actions/runs/44625861323)
  failed only in two of the five jobs (`assemble`, `native-engine`); `unit-tests`, `lint` and
  `klib-api` succeeded on the same commit.
- Each failing job stopped 16 seconds into its Gradle step (22:09:52-22:10:08 and
  22:09:54-22:10:10) and uploaded no build output. Configuring this multi-module build takes longer
  even with a warm cache, and a Kotlin or Kotlin/Native compilation error cannot occur that early,
  so the failure happened before the project was configured.
- The public job page exposes only the annotation `Process completed with exit code 1`. Raw logs sit
  behind GitHub sign-in and this host has no token, so the runner message itself could not be read;
  the classification is inferred from timings, missing artifacts and the unchanged build inputs.
- HEAD `3254379` has a complete green run set: `Android verification`
  [44632297629](https://github.com/mortezasun/android-calendarview/actions/runs/44632297629),
  `Android instrumentation`
  [44632285522](https://github.com/mortezasun/android-calendarview/actions/runs/44632285522),
  `Android tablet verification`
  [44632285173](https://github.com/mortezasun/android-calendarview/actions/runs/44632285173),
  `Android publishing verification`
  [44632287875](https://github.com/mortezasun/android-calendarview/actions/runs/44632287875) and
  `API documentation`
  [44632285179](https://github.com/mortezasun/android-calendarview/actions/runs/44632285179); the
  release workflow was skipped by its trigger conditions. Contrary to the session hand-off, `3254379`
  did get a CI run.
- Conclusion: runner/infrastructure failure on `ddb482c`, no code involvement, superseded by the
  all-green `3254379` run. Open: a `workflow_dispatch` re-run of `ddb482c` was not possible without
  repository credentials.
- Workflow defect found while diagnosing and not changed: the `Upload reports and available APK`
  step lists `.ci-diagnostics/toolchain.txt`, but `upload-artifact` v4+ skips hidden paths by
  default, so the diagnostics file is never uploaded. Adding `include-hidden-files: true` would make
  that file available for future triage.

### R2 - unreported commit `3254379`

- Reproduced: without the flag, `:library:compileKotlinJvm` warns `'expect'/'actual' classes ... are
  in Beta` on both `HeatmapPlatformFormatter` and its JVM actual. The opt-in is genuinely required
  by the five `internal expect object` declarations under Kotlin `2.5.0-RC`.
- It was applied to every KMP module through `subprojects {}`. It is now scoped to the five modules
  that declare those adapters (`:library`, `:calendar-gregorian`, `:calendar-hijri`,
  `:calendar-solarHijri-native`, `:calendar-chinese-compose`), which keeps the warning active for a
  new expect/actual class in any other module. Documented in
  [DEVELOPMENT.md](DEVELOPMENT.md#build-conventions).
- `withHostTest {}` on `:kmp-sample` matches the other modules and lets the parity test run on the
  Android host. The `desktopMain` accessor rewrite is behaviour-neutral. No other content in the
  commit.

### R3 - RTL helper without usage or test

- `:kmp-sample` now wraps its theme in `ProvideCalendarLocaleLayoutDirection(settings.language)`, so
  the language switch drives the reading direction; the Playground RTL switch stays a narrower inner
  override. `:calendarviewsample` keeps its own explicit switch.
- New device test `CalendarLocaleLayoutDirectionComposeTest` in `:library` covers `fa`/`ar` (Rtl),
  `de`/`en` (Ltr) and `null` (ambient direction unchanged, both directions).

### R4 - internal declaration in the JVM dump

- `gregorianOverlayData` is `internal` but appeared in `api/calendar-chinese-compose.api` because
  Kotlin does not mangle internal top-level function names. `scripts/api-check.sh` now filters that
  exact name and rejects it if it reappears; the KLIB dump never listed it. Documented in
  [DEVELOPMENT.md](DEVELOPMENT.md#public-api-guard).

### R5 - hidden compatibility overload for `GregorianChineseCalendarMonth`

- `locale` was inserted before `weekNumber` because `weekNumber` has to stay directly ahead of the
  trailing `weekNumberContent` slot; appending `locale` would invert that order. The old parameter
  list keeps its own JVM signature as a `@Deprecated(HIDDEN)` overload forwarding with `locale =
  null`. Rationale is in the overload KDoc and the [ADR 0007](decisions/0007-caller-selected-calendar-locale.md)
  amendment; the API dump lists both signatures.

### R6 - test gaps

- `:library` gained `unsupportedTagIsOutsideTheSupportedLanguages`. The label/digit half stays in
  `GregorianLabelTablesTest.unsupportedLanguageFallsBackToEnglishLabelsAndWesternDigits`, because
  `:library` ships no label table; a library-level label assertion would duplicate an engine table or
  invert the dependency direction. This split is intentional and documented here.
- `CalendarLocaleJavaTest` now covers `fa-AF` (tag, language, RTL), `de-AT`, `Locale.ROOT` (both
  report `und`) and `systemDefault()` against `Locale.GERMANY` and `Locale.ROOT` in addition to `ar`.

### R7 - parity-test exceptions

- `SampleStringsParityTest` names both exceptions with a reason: `appTitle` is the product name and
  stays untranslated in every language; `page.Heatmaps` and `mode.System` are German homographs of
  the English words and are translated rather than skipped.

### R8 - language chips at 320 dp

- The `:calendarviewsample` top-bar switch is now an internal `SampleLanguageSwitch(localeTag,
  onLocaleTagChange)` with a horizontally scrolling row inside a `widthIn(max = 224.dp)` cap, so the
  title keeps room and chips stay reachable instead of being clipped.
- New device test `SampleLanguageSwitchTest` composes the switch at exactly 320 dp, scrolls to every
  supported chip, asserts it is displayed and checks the click callback.

### R9 - right-to-left list

- Documentation only: ADR 0007 now states how a host overrides the mapping (own `LayoutDirection`
  around the calendar) and that the list ignores script subtags, so `ckb`, `az-Arab` and `pa-Arab`
  report `false` unless the host decides otherwise.

### R10 - documentation matrix

- `ARCHITECTURE.md` gained the locale data flow (`null` = platform formatter, non-null = engine
  tables, Chinese = style, direction opt-in, platform `expect` glue) and the opt-in keyboard focus
  step. `DEVELOPMENT.md` gained the recurring locale-parameter pattern, the scoped build flag and the
  internal-top-level-function guard note. README, USAGE_GUIDE, CHANGELOG and ADR 0007 were extended
  for the new keyboard feature and the review fixes.

### Next feature - keyboard navigation (F1)

- `CalendarMonthGrid` accepts `keyboardNavigation: Boolean = false` and `onMonthStep: ((Int) -> Unit)?
  = null` on both its data and cell overloads. Arrow keys move one day or one week (horizontal
  direction follows `LocalLayoutDirection`), Page Up/Down call `onMonthStep`, Enter/Space report the
  focused day, and the focused cell draws a border. Selection, `CollectionInfo` semantics and the
  default behaviour are unchanged.
- A key that leaves the rendered cells requests the matching month step, because the grid does not
  know the month identity.
- Device tests in `:library` (`CalendarMonthGridKeyboardTest`) cover day/week movement, month steps,
  Enter and RTL. They compile here and run in the CI emulator jobs.
- Tests, API dumps and docs were updated together. Local results: `208` `jvmTest` and `188`
  `testAndroidHostTest` across the seven modules, `68` sample unit tests, `2` `:kmp-sample` tests per
  host runner, `lint` green, `api-check`/`klib-api-check` green after regeneration, `commonMain`
  purity green, `:calendarviewsample:compileDebugAndroidTestKotlin` and
  `:library:compileAndroidDeviceTest` green (compile only).
- Deliberate-break check: emptying `RIGHT_TO_LEFT_LANGUAGES` makes
  `CalendarLocaleTest.rightToLeftFollowsTheLanguageList` fail; restoring it makes the suite pass
  again. The device tests could not be executed here, so their failure-on-break is not verified.

### Open points from this session

- CI for the new commit is not started (no repository credentials on this host).
- The `ddb482c` `workflow_dispatch` re-run and its log text remain unavailable.
- `upload-artifact` hidden-file exclusion for `.ci-diagnostics` is diagnosed but not changed.
- `:library` cannot assert engine labels/digits itself; that assertion lives in `:calendar-gregorian`.
- Found and not fixed (outside this task): `CalendarSampleHeatmapTest.firstDayOfWeekOverrideChangesTheWeekdayOrder`
  fails on a JVM whose default locale is German (`de_DE.UTF-8`), because the Chinese heatmap weekday
  labels come from the platform formatter and ignore `CalendarLocale` (`Sun` becomes `So`). The test
  passes with `-Duser.language=en`, which the suites above used.

### Fix: Chinese heatmap weekday labels and CI diagnostics upload

- `chineseMonthData`, `chineseYearHeatmap` and `chineseMonthHeatmap` gained a defaulted
  `locale: CalendarLocale? = null` before the trailing lambdas. A non-null locale takes the weekday
  labels from the Gregorian tables; titles and day labels still follow `ChineseTextStyle`. Hidden
  deprecated overloads keep the previous signatures; `api/calendar-chinese-compose.api` and the KLIB
  dump were regenerated. `:calendarviewsample` forwards its locale, which fixes
  `CalendarSampleHeatmapTest.firstDayOfWeekOverrideChangesTheWeekdayOrder` on a German default locale.
- The four workflows that upload `.ci-diagnostics` now set `include-hidden-files: true`.
- Checks (Linux, `-Duser.language=de`): `:calendar-chinese-compose:jvmTest` (new locale test, 12/12 in
  `ChineseComposeDataTest`), `:calendarviewsample:testDebugUnitTest`,
  `:calendar-chinese-compose:testAndroidHostTest`, `:kmp-sample:compileKotlinDesktop`, `api-check`,
  `klib-api-check` (modules with a built KLIB), `commonMain` purity. Not run: the CI change itself
  (needs a GitHub run), Apple/Windows runtime.

### Feature: `CalendarDateWindow` and "Build your own calendar view" guide

- New immutable `CalendarDateWindow(minEpochDay, maxEpochDay)` in `:library` (`contains`, `allows`,
  `isDateEnabled`). `CalendarRangePolicy` is deliberately unchanged: adding constructor parameters to
  that data class would break its binary `copy`/`componentN` signatures.
- `docs/USAGE_GUIDE.md` gained the window section and a five-step tutorial; README and CHANGELOG
  updated; `api/library.api` and `api/klib/library.abi` regenerated.
- Checks: `:library:jvmTest`, `:library:testAndroidHostTest` (new `CalendarDateWindowTest`, 5 tests),
  `api-check`, `klib-api-check`, `commonMain` purity. Not run: emulator and Apple/Windows runtime.

### Feature: `CalendarDayView`

- New `CalendarDayView` in `:library` with public pure `layoutDayEvents` (overlap columns). Compose
  rendering is covered only by compilation; the layout logic has 8 `commonTest` tests (all green on
  `jvmTest`). No Compose UI test was added, so rendering, scrolling and click behaviour are not
  device-verified.
- `:library:compileAndroidDeviceTest` could not be resolved in this session because Maven Central
  downloads failed (`hamcrest-library`, `error_prone_annotations`); `jvmTest`, `testAndroidHostTest`,
  Apple KLIB builds, `api-check`, `klib-api-check` and the `commonMain` purity check passed.
- Still open from the backlog: D (range-drag helper, logic only), E (day-style helper).

### Features: `CalendarRangeDrag` and per-day appearance

- `CalendarRangeDrag` (logic only) and `CalendarSelectionState.selectRange(drag, window)` in
  `:library`; `calendarDayContent`, `CalendarDayAppearance` and `calendarWeekendAppearance` as a
  helper over the existing `dayContent` slot, so no composable signature changed. This honours the
  AGENTS.md rule that drag gestures stay host concerns.
- Tests: 7 drag and 3 appearance `commonTest` cases, green on `jvmTest`. The `calendarDayContent`
  rendering is compile-verified only, with no Compose UI test, so it is not device-verified.
- Checks: `jvmTest`, `testAndroidHostTest`, `api-check`, `klib-api-check`, `commonMain` purity.
  `compileAndroidDeviceTest` was not re-run (Maven Central downloads failed earlier in the session).
- Backlog now left: I (more locales), G (screenshot tests, needs a dependency decision) and H
  (hardware verification).

### Feature: `fr`, `es` and `zh` locales

- Tables and date formats for French, Spanish and Chinese in `:calendar-gregorian`, `:calendar-hijri`
  and `:calendar-solarHijri-native`; `CalendarLocale` gained three constants and the supported list
  now has seven entries. Sample apps use their own four-language lists so the strings parity test is
  unchanged. Details and amendment: ADR 0007.
- Tests: label-table, date-convention, regional-tag and fallback tests (fallback now uses `tr`).
  `jvmTest`, `testAndroidHostTest` and the sample unit tests: 612 tests, 0 failures, with the JVM
  default locale set to German.
- Open: the non-Gregorian month names for the three languages are unverified and need native-speaker
  review; no sample translations exist for them; no device test covers them.

 (`Kotlin 2.5.0-RC` & multiplatform target expansion)

Status: implemented, pushed identically to `master` and `kmp`, and verified both locally on Linux and across all five GitHub Actions workflows (including API 29 phone and tablet emulators) against **Kotlin `2.5.0-RC`**, **Compose Multiplatform `1.13.0-beta02`**, Android SDK Platform `37.1`, OpenJDK `17.0.21`, and Gradle `9.9.1`; ready for manual debug APK device testing prior to tagging `3.1.0-beta`.

- **Release prep, sample app, and repo hygiene:**
  - Set publication `version = "3.1.0-beta"`, aligned `tools/consumer-check` BOM references to
    `3.1.0-beta`, and updated `:calendarviewsample` (`versionCode = 30100`, `versionName = "3.1.0-beta"`).
  - Added generic cross-calendar pairing support directly into `:library` and all five Compose
    calendar engines: `CalendarMonthGrid`, `CalendarWeekGrid`, `CalendarAgenda`, and all five
    `*CalendarMonth` composables accept `secondaryLabel` and `secondaryAccessibilityLabel`
    alongside `CalendarMonthData` / `CalendarWeekData` convenience overloads, trailing `dayContent`,
    and `@Deprecated(..., HIDDEN)` 3.0.0 binary-compatibility overloads. Each engine exposes
    `*MonthTitle`, `*SecondaryLabel`, and `*AccessibilityLabel` helpers (including range-safe
    fallbacks and platform `formatShortMonth` / `SolarHijriLocale.formatFullDate`).
  - Restructured `:calendarviewsample` with a Material 3 edge-to-edge navigation shell
    (`NavigationBar` / `NavigationRail` + `SampleTheme` dynamic colors): **Home** showcase
    with live multi-calendar "Today Across All Calendars" conversions and 5 module entry cards;
    **Calendars** explorer with dynamic **Alternative Secondary Calendar** pairing, per-engine
    feature cards, `SingleChoiceSegmentedButtonRow` (`Month` | `Week` | `Agenda`), and a live
    Selected Date Inspector; full interactive **Playground** workbench; and integrated **Heatmaps**
    tab (`HeatMapViewActivity`).
  - Follow-up fix (`c8b1287`): updated `rememberSampleMonthNavigation` to compare against the
    current anchor's month instead of overwriting intra-month anchors with day 1 (preserving the
    selected/today week when switching to Week view), replaced nested `horizontalScroll` chip strips
    with wrapping `FlowRow` layouts, and hardened `CalendarSampleUiTest` viewport assertions.
  - Added `kotlin-js-store/` to `.gitignore` so local JS/Wasm Yarn lockfiles remain untracked.
- **Caller-selected locale (ADR 0007):**
  - `:library` gained `CalendarLocale(languageTag)` with `English`, `German`, `Persian` and
    `Arabic`; every engine accepts an optional `locale` on `*MonthTitle`, `*SecondaryLabel`,
    `*AccessibilityLabel`, `*DayCell`, `*MonthData`, `*WeekData`, `*MonthState.title()` and the
    month composables. `null` keeps the platform formatter, so earlier behaviour and the 3.0.0
    binary-compatibility overloads are unchanged.
  - Labels come from pure Kotlin tables (`GregorianLabels`, `HijriLabels`, `SolarHijriLabels`,
    `SolarHijriLocale` month lists), which gives JS, Wasm, Native and Apple the same month names,
    weekday labels and digits without ICU or platform locale objects. Unknown tags fall back to
    English; `:calendar-chinese-compose` keeps `ChineseTextStyle` as its language control.
  - `:kmp-sample` now selects its language from `SampleLanguages` (the library locales) and passes
    it to every calendar, so the Settings switch changes calendar content instead of only the
    shell; shell copy exists for `en`, `de`, `fa` and `ar`.
  - `:calendarviewsample` bridges its existing `Locale` selection to the library locale, so the
    Android demo's language chips change month titles, weekday labels, day numbers, secondary
    labels and accessibility descriptions too; the Chinese module keeps `ChineseTextStyle`.
  - Locale follow-up (verified on the Linux host by `jvmTest`, compile checks and the API/KLIB
    guards only; no emulator or device run):
    - `GregorianChineseCalendarMonth` accepts `locale` for its Gregorian layer (hidden overload keeps
      the 3.0.0 signature); Chinese secondary labels remain `ChineseTextStyle`.
    - `CalendarLocale.supportedLanguages`, `systemDefault()`, `isRightToLeft`, the opt-in
      `ProvideCalendarLocaleLayoutDirection` and `java.util.Locale.asCalendarLocale()` (JVM/Android) were
      added; ADR 0007 was amended (layout direction is still never changed by calendar composables).
      `SampleLocale.kt` was removed in favour of the library extension.
    - New golden/completeness/fallback tests for all four label tables, `CalendarLocale` normalization
      tests, and `SampleStringsParityTest` in `:kmp-sample`.
    - `:calendarviewsample` has a top-bar language switch (`language-<tag>` test tags) and two new
      instrumentation tests in `CalendarSampleUiTest`; they are compiled here and have not run on an
      emulator yet (CI-Instrumentation pending).
    - Open observations (not fixed): `CalendarLocale.language` splits on `-` only, so an underscore tag
      such as `de_DE` yields `de_de` and falls back to English; the Hijri `de` table reuses the English
      transliterations; `GregorianChineseCalendarMonth` still has no `isDateEnabled`/`dayContent`
      parameters unlike the other month composables.
- **Multiplatform target expansion & shared actuals:**
  - All six Compose modules (`:library`, `:calendar-gregorian`, `:calendar-hijri`,
    `:calendar-solarHijri`, `:calendar-solarHijri-native`, `:calendar-chinese-compose`) now target
    `android`, `jvm`, `iosArm64`, `iosSimulatorArm64`, `macosArm64`, `js` (`browser` + `nodejs`), and
    `wasmJs` (`browser`). `:calendar-chinese-native` targets `jvm`, `linuxX64`, `linuxArm64`,
    `mingwX64`, `iosX64`, `iosArm64`, `iosSimulatorArm64`, `macosArm64`, `js`, and `wasmJs`.
  - Replaced single-target `iosMain` adapters with shared `appleMain` actuals (`iOS` + `macOS`)
    and added `webMain` (`jsMain` + `wasmJsMain`) formatters and `currentEpochDay` implementations.
  - Resolved the `3.0.0` Apple source implementation gaps noted below: `appleMain` `currentEpochDay()`
    now reads local civil year/month/day, formatters explicitly set `localTimeZone` / explicit
    Gregorian or Umm-al-Qura calendar IDs, and `HijriPlatform.apple.kt` anchors dates uniformly at
    12:00 UTC. Apple runtime execution itself still requires macOS/Xcode hardware.
- **Review fixes & regression coverage:**
  - Fixed `HijriPlatform.formatFullDate` and `OffsetHijriDateProvider` wiring so accessibility labels
    keep the actual timeline weekday (`weekdayEpochDay`) when dates are shifted by whole days.
  - Fixed `rememberSolarHijriMonthState` and `rememberSolarHijriNativeMonthState` to capture
    `currentEpochDay` via `rememberUpdatedState`, keeping visible navigation intact when callers
    replace the callback; added `SolarHijriComposeRegressionTest` (2 tests).
  - Configured `binaries.executable()` alongside `useChromeHeadlessNoSandbox()` for JS/Wasm
    browser tests on all Compose modules and declared Node/Yarn/Binaryen Ivy repositories in
    `settings.gradle.kts` under `FAIL_ON_PROJECT_REPOS`.
  - Hardened `scripts/verify-publication.sh` to validate all 60 publication artifact directories
    (root metadata, `-android`, `-jvm`, Apple/ARM/MinGW/JS/Wasm KLIBs, POMs, `.module` references,
    and BOM constraints) and updated `.github/workflows/*.yml`.
  - Split `README.md` into a concise overview linking [GETTING_STARTED.md](GETTING_STARTED.md) and
    [USAGE_GUIDE.md](USAGE_GUIDE.md).
- **Verified locally on Linux & on GitHub Actions (`c8b1287`):**
  - Unit & multiplatform tests: `174` `jvmTest`, `157` `testAndroidHostTest`, `68` sample unit
    tests, `173` `jsNodeTest`, `173` `jsBrowserTest`, `16` `:calendar-chinese-native:wasmJsNodeTest`,
    `173` `wasmJsBrowserTest`, `16` `linuxX64Test`, plus Python tests in `scripts/` &
    `tools/chinese-calendar/` (`kotlin-js-store/` is ignored in `.gitignore`).
  - Builds & static guards: `:calendarviewsample:assembleDebug` / `assembleRelease`,
    `assembleAndroidMain`, `:calendar-chinese-native:jvmJar`, `lint`,
    `scripts/check-commonmain-purity.sh`, updated `scripts/api-check.sh` and
    `scripts/klib-api-check.sh`, all Apple/ARM/MinGW KLIB compilations, 60-module isolated
    publication verification (`3.1.0-beta`) + `:jvm-consumer:run` +
    `:android-consumer:assembleRelease`, and `:dokkaGenerate`.
  - GitHub Actions (`master` & `kmp` at `c8b1287`): all 5 active workflows succeeded —
    `Android verification` (`44064747948` / `44064748169`), `Android instrumentation`
    (`44064748139` / `44064748054`, API 29 phone emulator: 52/52 tests passed = 21 library + 31
    sample), `Android tablet verification` (`44064747953` / `44064747967`, API 29 tablet emulator:
    52/52 tests passed = 21 library + 31 sample), `Android publishing verification` (`44064747918` /
    `44064748231`), and `API documentation` (`44064747960` / `44064747993`).
- **Remaining open gates (unverified on this Linux host):**
  - User manual verification of `:calendarviewsample` (`calendarviewsample-debug.apk`) on device and
    explicit release approval before tagging `3.1.0-beta` pre-release.
  - Native runtime execution on Apple (`ios*`, `macosArm64`), `linuxArm64`, and Windows (`mingwX64`)
    hardware (cross-compiled and KLIB-ABI-checked only on Linux).
  - Replacing pre-release `Kotlin 2.5.0-RC` and `Compose Multiplatform 1.13.0-beta02` with final
    stable releases before cutting any stable (`3.1.0`) release tag.
  - Physical device / fold / TalkBack acceptance, Maven Central signing/credentials, GitHub Pages
    `build_type: workflow` setting, and JitPack consumption of the pushed `3.1.0-beta` tree.

## Post-3.0.0 tooling (branch `kmp` / `master` at `a116b81`)

Status: committed tooling on `a116b81` (`3.0.0` at `a0c686e` plus Dokka, benchmark, and API-guard
commits). No new tag or GitHub Release is part of this tooling change.

- **Dokka:** Dokka `2.3.0` is applied to all seven published KMP modules and aggregates them with
  the root `:dokkaGenerate` task. HTML is written to `build/dokka/html`; the `API documentation`
  workflow uploads `dokka-html` and deploys to GitHub Pages when Pages uses workflow builds.
  Dokka reports no unresolved KDoc links. Run
  [43863485535](https://github.com/mortezasun/android-calendarview/actions/runs/43863485535)
  completed successfully and uploaded the `dokka-html` artifact. Its Pages steps were skipped
  because the repository's Pages source is not configured as `build_type: workflow` (the Pages
  site currently returns HTTP 404), so the artifact is the documented fallback. Enabling Pages
  with GitHub Actions as the source remains a maintainer repository setting.
- **Benchmarks:** the standalone `:benchmarks` module uses kotlinx-benchmark `0.5.0` with JMH
  `1.37`. `./gradlew :benchmarks:mainBenchmark` runs Gregorian and Hijri month calculations plus
  both shared heatmap adapters. It is not wired into `check`, `build` or CI. Results are in
  [BENCHMARKS.md](BENCHMARKS.md).
- **JVM engine console example:** the tracked `tools/consumer-check/jvm-consumer` uses only the
  published BOM and `calendar-chinese-native` JVM artifact, with no Compose, Android UI or added
  dependency. It prints the dataset range, epoch-day mapping, 2023 month/year lengths, leap-month
  title, start epoch day, weekday offset and day labels. `./gradlew -p tools/consumer-check
  :jvm-consumer:run` completed with exit 0 and `JVM engine OK`; `:calendar-chinese-native:jvmTest`
  was also green. This is a JVM engine example only, not a UI or Compose Desktop example.
- **API guard scope:** the five platform adapter objects are internal implementation details. The
  JVM dump filters their exact JVM bytecode names because Kotlin `internal` is public bytecode and
  the binary-compatibility-validator plugin creates no tasks with AGP 9's built-in Kotlin support;
  the KLIB guard rejects any leak. The `api/*.api` dumps contain none of the five objects, and the
  KLIB snapshots contain none. See [DEVELOPMENT.md](DEVELOPMENT.md#public-api-guard).
- **Gradle:** wrapper raised from `9.8.2` to `9.9.1`.
- **Deprecations:** Gradle 9.9.1 still reports upstream deprecation warnings during build execution;
  record plugin-owned deprecations rather than suppressing them. Configuration-cache storage works
  for `help`.

Open blockers: Apple/Windows/`linuxArm64` hardware runtime verification, live API 29 emulator run of
the expanded 46-test suite, Maven Central signing/credentials, and real process-death/hardware
acceptance remain open.

## Kotlin 2.5.0 upgrade status

While tagged `3.0.0` used Kotlin `2.4.21` and Compose Multiplatform `1.12.2`, the uncommitted local
working tree actively tests Kotlin `2.5.0-RC` and Compose Multiplatform `1.13.0-beta02` against
Android SDK Platform `37.1`, Gradle `9.9.1`, and AGP `9.5.1` (keeping the experimental `.kar`
`KOTLIN_ARCHIVE` publication format disabled). The checklist, tested scope, and remaining stable-release
promotion questions are tracked in [KOTLIN_2_5_UPGRADE.md](KOTLIN_2_5_UPGRADE.md).

## Kotlin Multiplatform release 3.0.0

Status: implemented, locally verified and released from branch `kmp` at `a0c686e7da9e5ea6950477b088c11e66486c172b`.
Tag `3.0.0` and the GitHub Release were published on 2026-11-16 by release workflow
[43854431009](https://github.com/mortezasun/android-calendarview/actions/runs/43854431009).
iOS runtime/simulator verification is still not possible on Linux and is not claimed. The
calendar modules no longer use `java.time` or `ir.huri:jalalicalendar` in shared code. Known iOS
implementation gaps before claiming runtime support: `currentEpochDay` derives the day from UTC
instead of the local civil day; the formatters do not set an explicit local time zone and the
Gregorian formatters do not force the Gregorian calendar; `HijriPlatform.fromEpochDay` and
`toEpochDay` use different noon anchoring; and the platform formatter objects still appear in
`api/*.api`. These require a macOS/Xcode fix-and-test pass.

- `:library`, `:calendar-gregorian`, `:calendar-hijri`, `:calendar-solarHijri` (facade),
  `:calendar-solarHijri-native` and `:calendar-chinese-compose` moved their shared sources to
  `commonMain` and use immutable value types plus internal platform formatter hooks.
- Public callbacks and predicates use epoch days (`Long`), ISO weekday numbers (`Int`) and
  injected `currentEpochDay` providers instead of `LocalDate`, `DayOfWeek`, `Clock` and
  `Locale`; see ADR 0006 for the breaking changes.
- `:calendar-solarHijri` is now a facade over the dependency-free `:calendar-solarHijri-native`
  engine; the `ir.huri:jalalicalendar` dependency was removed.
- Verification at the 3.0.0 release: 165 `jvmTest` cases in the seven library modules, 148 of
  them again through `testAndroidHostTest` (`calendar-chinese-native` is native-only; one
  Chinese-Compose test is JVM-only), 62 sample unit tests and 16 Kotlin/Native tests, `lint`
  for the six KMP modules and the sample (0 errors), `:library:compileAndroidDeviceTest` and
  `:calendarviewsample:compileDebugAndroidTestKotlin`, `:calendarviewsample:assembleDebug`/
  `assembleRelease`, `assembleAndroidMain`, `publishToMavenLocal` for all seven modules plus the
  BOM, `scripts/api-check.sh`, `scripts/klib-api-check.sh` and
  `scripts/check-commonmain-purity.sh`. The emulator executions are recorded below.
- The tagged `3.0.0` release and the current branch both contain the same 16 Kotlin/Native
  tests. See [Post-3.0.0 tooling](#post-300-tooling-branch-kmp-unreleased).
- Build setup: the KMP modules apply `com.android.lint` because
  `com.android.kotlin.multiplatform.library` creates no lint tasks, and opt in to
  `withHostTest`/`withDeviceTest`; `src/androidTest/java` is mapped into `androidDeviceTest`.
  Compose Multiplatform 1.12.2 leaves the device-test resource copy task unconfigured, so the
  root build disables it.
- Build and CI hardening (2026-11-16): the deprecated `androidLibrary` block and `compose.*`
  dependency accessors were replaced by `android` and explicit `org.jetbrains.compose.*`
  coordinates, unused sample resources were removed (sample lint now reports no issues), and the
  four workflows plus `scripts/run-connected-tests.sh` were ported to the KMP tasks and the
  `kmp` branch. `scripts/verify-publication.sh` now follows the KMP layout (root metadata,
  `<module>-android` AARs, iOS klibs), checks all root module metadata plus the iOS klib archives,
  and the isolated JVM/Android consumers resolve the published coordinates. The former Gradle
  9.9.1 availability hint is resolved by the post-release wrapper update above.
- Not run locally: iOS simulator tests and the `iosApp` sample (need macOS with Xcode);
  instrumentation tests only compile in the Linux VM. The CI emulator runs are recorded below.

### CI results (2026-11-16)

The first run for `64b521a` passed Android verification and publishing, but the tablet workflow
failed in the connected-test step: all 16 library tests passed and 12 of 26 sample tests failed
with `Unable to resolve activity ... androidx.activity.ComponentActivity`. The sample's debug
variant now attaches `ui-test-manifest`, and both emulator workflows publish the complete failed
case list (the summary is emitted before GitHub's ten-annotation limit).

Rerun for `1a24725` (pre-3.0.0 head, with the Compose test rules on `junit4.v2`):

| Workflow | Run | Result |
|---|---|---|
| Android verification | [43836063861](https://github.com/mortezasun/android-calendarview/actions/runs/43836063861) | success |
| Android publishing verification | [43836063794](https://github.com/mortezasun/android-calendarview/actions/runs/43836063794) | success |
| Android tablet verification | [43836063797](https://github.com/mortezasun/android-calendarview/actions/runs/43836063797) | success: 42/42 tests (16 library, 26 sample), 0 failures |
| Android instrumentation | [43836063805](https://github.com/mortezasun/android-calendarview/actions/runs/43836063805) | success: 42/42 tests (16 library, 26 sample), 0 failures |

The preceding `3bb7fdd` runs also passed 42/42 and verified the `ui-test-manifest` fix before the
test-rule migration. The earlier tablet failure
([43824943413](https://github.com/mortezasun/android-calendarview/actions/runs/43824943413)) was
diagnosed from its published annotations; the phone run for `64b521a` was superseded before it
completed and is not part of the final result.

### 3.0.0 release commit `a0c686e` (2026-11-16)

| Workflow | Run | Result |
|---|---|---|
| Android verification (including `klib-api`) | [43854430900](https://github.com/mortezasun/android-calendarview/actions/runs/43854430900) | success |
| Android publishing verification | [43854430869](https://github.com/mortezasun/android-calendarview/actions/runs/43854430869) | success: isolated publication, minified Android consumer |
| Android tablet verification | [43854430879](https://github.com/mortezasun/android-calendarview/actions/runs/43854430879) | success: 44-test release gate (18 library, 26 sample) |
| Android instrumentation | [43854430766](https://github.com/mortezasun/android-calendarview/actions/runs/43854430766) | success: 44-test release gate (18 library, 26 sample) |
| Publish approved 3.0.0 release | [43854431009](https://github.com/mortezasun/android-calendarview/actions/runs/43854431009) | success: tag `3.0.0` and GitHub Release created |

The release workflow only creates the tag after every listed check succeeds on the release commit.
JitPack built `3.0.0` with all seven modules and the BOM (`BUILD SUCCESSFUL`, exit 0, 4m 9s;
[build log](https://jitpack.io/com/github/mortezasun/android-calendarview/3.0.0/build.log)), and
JVM plus minified Android consumers resolved the BOM and artifacts only from JitPack on
2026-11-16. The BOM and library POMs return HTTP 200.

## Previous release: 2.4.0 Chinese calendar (2026-11-14)

Status: implemented, committed, locally verified and remotely CI-green on branch `update` at
`4dc484ae8cff91bad0869cd8d24e0b71144ff8e9`. The review fixes on top of
`6970b924c855bb02792dee2bf818f63fe1b66500` are locally tested and CI-green at
`e80c00fd1748a2d0e0c35db10abe8053d1176b66`. The further review fixes on base `6fac725` are
committed as `5cf640d`; remote CI for the pushed tree `952bbb6` (containing `5cf640d`) passed all
four workflows. The 2.4.0 release was approved on 2026-11-14 with the documented external gates
still open; tag `2.4.0` and the GitHub Release were published on 2026-11-14 by release workflow
`43537597202` at tag commit `291275b9e01da11a9b009f9ad77f05a5ef89a05e`. The release history was
fast-forwarded to `master` on 2026-11-14. JitPack is confirmed
for `2.3.0` and commit `7a51e24`; the `2.4.0` tag/BOM consumer still has to be verified. No claim
is made for real-device, foldable or TalkBack coverage.

- Added `:calendar-chinese-native`, a Kotlin Multiplatform engine with `jvm()` and `linuxX64()`
  targets: immutable `ChineseMonth`/`ChineseDate`, `ChineseCalendarEngine`, `ChineseFormat`,
  `ChineseTextStyle`, explicit `ChineseDateOutOfRangeException`, and a versioned month table for
  Chinese years `1901..2099` (Gregorian `1901-02-19..2100-02-08`, 72,674 days, 2,461 months,
  73 leap years).
- Data provenance: `tools/chinese-calendar/generate.py` derives month starts at development time
  from VSOP87D Earth series, Meeus chapters 25/49 and Espenak–Meeus ΔT. HKO tables were used only
  as a development-time comparison: 2,458/2,461 month starts match exactly. The three cases
  1914-11-18, 1916-02-04 and 1920-11-11 are audited in
  `tools/chinese-calendar/reference-conflicts.json` with JDE/TT, ΔT, UTC, UTC+8 and structured
  NASA/GSFC new-moon times (1914-11-17 16:02 UT, 1916-02-03 16:05 UT, 1920-11-10 16:05 UT) with a
  documented 60-second tolerance. `--verify-reference` checks complete file/start coverage, year
  and leap status, identical UTC/UTC+8 instants, explicit time zones and the independent time
  within tolerance. HKO lists the previous UT date, consistent with pre-1929 Beijing local mean
  time (UT+7:45:40). The dataset deliberately keeps the modern UTC+8 convention across its full
  range; these are documented historical-reference differences, not an open data question. A
  blanket local-time switch would also move other starts (the audit includes 1906-04-24), so it is
  not adopted. No HKO data, ICU, lunar-java or other calendar code is embedded or redistributed.
- Review fixes (base `6970b92`): today navigation honours configured month bounds; the reference
  comparison rejects missing files/starts and checks year/leap status; the Gregorian Chinese
  overlay uses Gregorian header/pager identities; agenda keys include the text style; the today
  callback is kept current without resetting navigation; snapshot decoding rejects non-binary
  leap flags; switching to Chinese clamps an unsupported anchor with a visible message while the
  shared selection stays unchanged. Targeted JVM and compiled Compose regression tests were added.
- Further review fixes (base `6fac725`): the sample month window is truncated at finite engine
  bounds so both boundary months render in Single and Pager modes; the conflict audit requires
  both recorded instants to agree and compares the structured NASA time within its documented
  tolerance; the 1914 NASA value is corrected and the HKO rows are recorded as a deliberate UTC+8
  product convention; the
  today-callback test exchanges two distinct callback instances after restoration. Sample JVM
  tests and the Python audit pass; the new Compose boundary/callback tests compile but were not
  run locally on a device.
- Review fixes against `54bf23d`: `CalendarSampleViewport` rebuilds a failed window when a valid
  requested month arrives (invalid center -> valid anchor and Gregorian overlay -> Chinese mode
  are covered by new compiled Compose regression tests); the README `ChineseDate.of` example now
  passes `isLeapMonth = false` and is executed by a standalone JVM consumer; the publishing
  workflow checks exact target binaries and every Gradle Module Metadata reference and builds an
  isolated JVM + Android consumer; the API guard requires the exact versioned engine JAR and
  rejects ambiguous/stale JVM jars. A real publication defect was also fixed: Kotlin 2.4 wrote
  the metadata/sources/native files under different names than the `.module` files referenced,
  so the engine build now normalizes those names. Local consumers resolve the BOM, JVM jar and
  native klib from the isolated repository. This block is `09e3a73` on `update`; all four remote
  workflows passed (verification `43447073439`, publishing `43447073475`, instrumentation
  `43447073390`, tablet `43447073539`). A docs-only follow-up records these results.
- Publishing URL fix (base `acf3bfcb484886a73c4a03474971d0b289219023`): the verifier now requires
  `files[].url`, validates it and resolves relative URLs against the `.module` file; the target must
  exist and be non-empty. `files[].name` is checked separately and is never used as the download
  path. `available-at.url` uses the same validation. A copied complete publication with a missing
  URL or a broken URL (while the `name` file still exists) now fails the verifier; 12 script unit
  tests pass. The JVM and Android consumers were re-run after the change; the linuxX64 klib is
  file/metadata-checked but was not consumed by a Kotlin/Native consumer. This block is `9fd3b50`
  on `update`; verification `43459739249`, publishing `43459739473`, instrumentation
  `43459739435` and tablet `43459739230` all passed.
- Added `:calendar-chinese-compose`: `ChineseCalendarMonth`, `GregorianChineseCalendarMonth`,
  `chineseMonthData`, `ChineseWeekData`, `ChineseMonthState`/`rememberChineseMonthState` with
  leap-month identity in title, keys, saver and heatmap titles, plus `chineseYearHeatmap` /
  `chineseMonthHeatmap`. Adjacent cells outside the engine range are blank and disabled.
- Integrated `SampleCalendar.Chinese` into the sample with a separate month view, Gregorian grid
  with Chinese secondary labels, style chips, leap-month details, week/agenda views, heatmaps,
  navigation bounds and saved-state fields. The enum entry is appended last and the sample saves
  the calendar by name, so existing saved states are not reinterpreted.
- Wired `settings.gradle.kts`, root version `2.4.0`, sample `versionCode 20400`/`versionName
  "2.4.0"`, BOM (seven constraints), `jitpack.yml`, `scripts/api-check.sh` (six AARs plus the KMP
  JVM jar), `android-verification.yml` (new native-engine job) and `android-publishing.yml`.

### Verification (local, JDK 21.0, Android SDK Platform 37.0, Build-Tools 37.0.0)

| Check | Result |
|---|---|
| `./gradlew test :calendar-chinese-native:jvmTest` | BUILD SUCCESSFUL; 211 JVM tests (197 Android JVM + 14 KMP JVM, incl. benchmark reporting), 0 failed, 0 skipped |
| `./gradlew :calendar-chinese-native:linuxX64Test` | BUILD SUCCESSFUL; 14 tests, 0 failed, 0 skipped |
| `./gradlew :calendarviewsample:assembleDebug` | BUILD SUCCESSFUL |
| `./gradlew ... lintDebug` for core, all five calendar modules and sample | BUILD SUCCESSFUL |
| `./gradlew :calendarviewsample:compileDebugAndroidTestKotlin` | BUILD SUCCESSFUL (compile only; not executed) |
| `python3 -m unittest tools/chinese-calendar/test_generate.py` | 7 tests, 0 failed; missing reference files/starts, year/leap checks, documented-conflict audit, instant/timezone/tolerance rejection, the corrected NASA-1914 value and the Kotlin-data SHA-256 against the manifest |
| `bash scripts/api-check.sh` | passed; existing five dumps `0` diff, two new dumps added |
| isolated `publishToMavenLocal` to `/tmp/m2test` | engine root/jvm/linuxx64, compose and BOM published; POMs inspected: engine only `kotlin-stdlib`, compose `library` + engine + material3 runtime, BOM lists seven modules |
| isolated publication verification on `/tmp/m2-review-final` | `scripts/verify-publication.sh` passed: exact AAR/JAR/KLIB/POM/MODULE/sources files, archive contents, all `.module` `files`/`available-at` references, engine POM isolation and BOM constraints |
| standalone JVM consumer (`javac` + engine jar) | 2024-02-10 → epoch 19763, round-trip OK, 2023 has 13 months and leap month `2023-2L`; jar contains no AndroidX/Compose/Android classes |
| tracked isolated JVM consumer (`tools/consumer-check`) | `:jvm-consumer:run` against `/tmp/m2-review-final`: `JVM consumer OK`, Exit 0; resolves BOM + engine from the publication only |
| tracked isolated Android consumer (`tools/consumer-check`) | `:android-consumer:compileDebugKotlin` against `/tmp/m2-review-final`: BUILD SUCCESSFUL; compiles the public Compose facade, not installed/run |
| negative publication/API checks | missing target JVM jar, broken native `.module` reference, missing `files[].url` and broken `files[].url` (with the `name` file still present) each fail the verifier; a second stale `-jvm-2.3.0.jar` fails `scripts/api-check.sh` |
| `python3 -m unittest discover -s scripts -p 'test_*.py'` | 12 tests, 0 failed; 4 new tests cover URL resolution and missing-name handling |
| independent Android consumer (separate `/tmp` project, BOM + file Maven repo, `assembleDebug`) | BUILD SUCCESSFUL; APK produced, not installed or run |

### Release-gate addendum (2026-11-13)

- JitPack `2.3.0` is confirmed: the build log returned HTTP 200 with five modules and the BOM, and a temporary Android BOM consumer (`library`) compiled JitPack-only without `mavenLocal`/`includeBuild` (`BUILD SUCCESSFUL`, exit 0).
- JitPack commit `7a51e24` is confirmed: the build log succeeded with the BOM, engine (metadata/JVM/linuxX64), Compose and five existing modules, and the direct engine JVM consumer ran (`JVM consumer OK`, epoch 19763, 13 months, exit 0). The `2.4.0` tag exists, but the JitPack-only tag/BOM consumer remains unverified because the build endpoint returned HTTP 404 at that time; the commit BOM still constrains modules to `2.4.0`.
- Benchmark baseline (Freebuff VM, JDK 21.0.13, 8 vCPU): conversion 58.77 ns/op; month construction 16,074.47 ns/month; 13-month heatmap 355,526 ns/year; navigation 53.79 ns/step. Allocations were 56.01 B / 15,630.53 B / 248,328 B / 48.01 B per operation. The focused test was green; physical target hardware and final budgets remain open.
- Advisory scan: 101 runtime coordinates had no OSV findings; 334 runtime/buildscript coordinates had findings only in the AGP 9.5.1 buildscript (Guava, commons-lang3, jose4j, Bouncy Castle, JDOM2). These packages are not in the shipped AAR/JAR files; maintainer acceptance or an AGP upgrade remains open.
- A local API-29 emulator was installed, but boot failed because the KVM group permission for `/dev/kvm` was unavailable. The remote API-29 suite is green, but there is no Chinese device, fold or TalkBack matrix.
- `./gradlew test :calendar-chinese-native:linuxX64Test`, the focused benchmark, `scripts/api-check.sh` and the Python suites succeeded. The gate-evidence commit `4ce4d3f` is on `origin/update`; remote CI was verification `43479911419`, publishing `43479911387`, instrumentation `43479911196` and tablet `43479911275`, all successful. No merge, tag or release was created.

### Open items

- Evidence tree and CI fix pushed to `origin/update`; remote workflows passed for `1ab841e70b6fb7ab35b16e970715b500b91f95a2`:
  - `Android verification`: Run 43401491446 (success)
  - `Android publishing verification`: Run 43401491433 (success)
  - `Android instrumentation`: Run 43401491452 (success)
  - `Android tablet verification`: Run 43401491675 (success).
- Review-fix commit `e80c00fd1748a2d0e0c35db10abe8053d1176b66` pushed to `origin/update`; all four workflows passed:
  - `Android verification`: Run 43412948314 (success)
  - `Android publishing verification`: Run 43412948284 (success)
  - `Android instrumentation`: Run 43412948316 (success)
  - `Android tablet verification`: Run 43412948286 (success).
- Remote GitHub Actions are green for `1ab841e70b6fb7ab35b16e970715b500b91f95a2` and `e80c00f`, including the new `native-engine` job and
  the Chinese reference-comparison Python step. These runs execute the existing
  instrumentation/tablet suites; they are not a Chinese-specific device, fold, TalkBack or
  benchmark matrix.
- Remote-CI for the pushed tree `952bbb6` (containing `5cf640d`) passed all four workflows:
  - `Android verification`: Run 43439808516 (success)
  - `Android publishing verification`: Run 43439808420 (success)
  - `Android instrumentation`: Run 43439808384 (success)
  - `Android tablet verification`: Run 43439808435 (success)
- Remote JitPack `2.3.0` and commit `7a51e24` are confirmed (build + consumers). The tagged `2.4.0` BOM consumer remains blocked until a separately approved tag; the commit-hash BOM still constrains modules to `2.4.0`.
- Remote CI for the review-fix commit `09e3a73` against `54bf23d` is success: verification `43447073439`, publishing `43447073475`, instrumentation `43447073390`, tablet `43447073539`. The documentation follow-up is a separate commit; local publication, both isolated consumers, the API guard and the native/JVM regression are green.
- Remote CI for the URL-based publication follow-up `9fd3b50` on base `acf3bfc` is success: verification `43459739249`, publishing `43459739473`, instrumentation `43459739435`, tablet `43459739230`. The verifier, 12 script tests and both isolated consumers are locally green; the linuxX64 variant remains file-checked only.
- Instrumentation/Compose UI tests and real phone/tablet/fold transitions, TalkBack/large-font/RTL
  checks were not executed locally (no usable device; KVM permission denied). JVM benchmarks and
  the OSV advisory scan were executed; physical benchmark acceptance and buildscript finding
  acceptance remain open. See [CHINESE_CALENDAR_QUALITY.md](CHINESE_CALENDAR_QUALITY.md).
- No merge, tag, GitHub Release or publication was performed and none is authorized by this status.

## Historical release status: 2.3.0 (published, branch `update`, 2026-11-12)

- Added dependency-free `:calendar-solarHijri-native` module (`com.develotter.calendarview.calendars.solarHijriNative`) per [ADR 0004](decisions/0004-native-solar-hijri-module.md), keeping `:calendar-solarHijri` and `api/calendar-solarHijri.api` completely unchanged.
- Implemented `SolarHijriEngine` (Borkowski 1996 break-table port from `jalaali-js`, MIT, attributed in `NOTICE`, years `-61..3177` AP), immutable `SolarHijriDate`, `SolarHijriLocale` (`fa`, `fa-AF`/`prs`, `en`), and Compose parity (`SolarHijriNativeCalendarMonth`, `SolarHijriNativeMonthData`, `SolarHijriNativeWeekData`, `SolarHijriNativeMonthState` / `rememberSolarHijriNativeMonthState` with injectable `Clock` and boundary guards).
- Integrated `:calendar-solarHijri-native` into `settings.gradle.kts`, `calendar-bom`, `jitpack.yml`, `scripts/api-check.sh`, CI workflows (`android-verification.yml`, `android-publishing.yml`, `android-tablet.yml`), `calendarviewsample` (`SampleCalendar.SolarHijriNative`), and `2.3.0` release preparation (`build.gradle.kts`, `CHANGELOG.md`, `docs/releases/2.3.0.md`, `publish-2-3-0.yml`, `update-2-3-0-release-notes.yml`).
- Verified locally on `update` (JDK 17.0.21, Android SDK Platform 37.0, untracked `local.properties`):
  - `./gradlew test`: 176 JVM unit tests passed (0 failed, 0 skipped; 27 in `:calendar-solarHijri-native`, 56 `:library`, 53 `:calendarviewsample`, 18 `:calendar-hijri`, 11 `:calendar-gregorian`, 11 `:calendar-solarHijri`), including exhaustive bijection across all days of `-61..3177` AP and `1280..1450` AP cross-check against `ir.huri:jalalicalendar` (note: `ir.huri` diverges by 1 day around 1900 CE / 1278 AP).
  - `./gradlew :calendarviewsample:assembleDebug`, `:library:lintDebug`, `:calendar-solarHijri-native:lintDebug`, `:calendarviewsample:lintDebug`, `:library:compileDebugAndroidTestKotlin`, `:calendarviewsample:compileDebugAndroidTestKotlin`: passed.
  - Release AARs for all 5 library modules + `bash scripts/api-check.sh`: passed (`api/calendar-solarHijri-native.api` added; existing 4 `.api` dumps have 0 diff).
  - `publishToMavenLocal` to `/tmp/m2`: verified `calendar-solarHijri-native-2.3.0.pom` has no `jalalicalendar` dependency and `calendar-bom-2.3.0.pom` constrains all 5 library modules.
  - `python3 -m unittest discover -s scripts -p 'test_*.py'`: 8 tests passed.
- Published `2.3.0` on GitHub Releases (`https://github.com/mortezasun/android-calendarview/releases/tag/2.3.0`) from commit `8b7bea42e888785752fff199b2d53c04ab9888e4` via `publish-2-3-0.yml` (Run `43375002033`).
- Verified remote CI runs on `8b7bea4`:
  - `Android verification`: Run `43375002038` (success)
  - `Android publishing verification`: Run `43375002125` (success)
  - `Android instrumentation`: Run `43375002077` (success)
  - `Android tablet verification`: Run `43375002074` (success)
- JitPack status (2026-11-13): `2.3.0` build and JitPack-only BOM consumer passed; commit `7a51e24` also builds and its direct engine consumer passed. The `2.4.0` tag/BOM consumer remains open until a separately approved tag exists.

## Post-2.2.0 Adaptive Review and Hardening (branch `update`, work packages A–I)

Following the published `2.2.0` release, a comprehensive independent code review identified and resolved
remaining edge cases in adaptive geometry, viewport heights, month width touch targets, and test coverage:

- **Work package A (Build & CI Stabilization):** Dedicated workflows, test sync, and CI stabilization.
- **Work package B (Month Widths & Touch Targets):** Implemented `sampleTargetMonthWidth(showWeekNumbers)`
  (336 dp without week numbers, 376 dp with week numbers) to guarantee 48 dp day cells in the sample,
  preserving the library default (320 dp). Tested via `CalendarSampleMonthWidthTest`.
- **Work package C (Continuous Calendar State Continuity):** Added `CalendarSampleAdaptiveLayoutTest`
  exercising real calendar selection, navigation, multi-column switches, and fold changes continuously
  without activity restart or state loss.
- **Work package D (Genuine State Restoration):** Added `CalendarSampleStateRestorationTest` testing
  `rememberCalendarSampleState` (all 17 properties), `CalendarSelectionState` (range endpoints and interior),
  display modes, and heatmap settings using Compose's `StateRestorationTester`.
- **Work package E (Adaptive Viewport Heights):** Implemented `sampleBoundedViewportHeight` and
  `sampleDialogViewportHeight` in `CalendarSamplePaneHeight.kt`, removing artificial minimums and capping
  heights to 75% of available pane. Tested via 14 JVM unit tests.
- **Work package F (Fold Geometry & Insets):** Implemented `sampleLocalFold` in `CalendarSampleFoldGeometry.kt`,
  properly handling safe-drawing insets, local coordinates, offscreen folds, and physical LTR placement for RTL.
- **Work package G (Heatmap & Accessibility):** Preserved touch targets, TalkBack semantics, and adaptive
  two-pane layouts for the heatmap demo.
- **Work package H (Documentation Consolidation):** Synchronized all status, architecture, and review docs.
- **Work package I (Independent Review & CI Verification):** Full diff verification, API-guard check,
  and green CI across all four workflows on commit `90ad35b`:
  - `Android verification`: Run 43223950693 (success across assemble, unit-tests, lint)
  - `Android publishing verification`: Run 43223950670 (success)
  - `Android instrumentation`: Run 43223950664 (success on API 29 across all 34 instrumented tests)
  - `Android tablet verification`: Run 43223950572 (success on 1600x2560 240 dpi tablet across all 34 instrumented tests)

## Historical release status: 2.2.1 (published)

- Release commit `dd20e49` is tagged `2.2.1` and the GitHub Release `2.2.1` is published via `publish-2-2-1.yml` (run 43268408129).
- Includes the full work packages A–I adaptive hardening suite, 48 dp touch target enforcement, fold geometry, adaptive pane heights, continuous transition tests, genuine state restoration tests, and tablet CI.
- All four CI workflows verified on release tree `dd20e49`:
  - `Android verification`: Run 43268408115 (success across assemble, unit-tests, lint)
  - `Android publishing verification`: Run 43268408103 (success)
  - `Android instrumentation`: Run 43268408390 (success on API 29 across all 34 instrumented tests)
  - `Android tablet verification`: Run 43268408198 (success on 1600x2560 240 dpi tablet across all 34 instrumented tests)
- 100% binary compatible with `2.2.0`, `2.1.0` and `2.0.1`.

## Historical release status: 2.2.0 (published)

- Release commit `f5dd967` is tagged `2.2.0` and the GitHub Release `2.2.0` is published.
- All release verification jobs passed on the release tree:
  - `Android verification`: Run 43176664468 (success across assemble, unit-tests, lint)
  - `Android publishing verification`: Run 43176664370 (success)
  - `Android instrumentation`: Run 43176664365 (success on API 29 across all 26 instrumented tests)
- 100% binary compatible with `2.1.0`. All public and protected JVM APIs in `:library` and all calendar engines are identical.
- Adaptive multi-month columns, fold postures, and two-pane adaptive sample layouts are fully implemented and verified.


## Adaptive layout, Phase 7 (state restoration & recreation tests, branch `update`)

- Added `CalendarSampleStateTest.kt` covering anchor updates, month moves, engine switches and
  display settings preservation.
- Verified state survival via `CalendarSampleStateTest.kt` (JVM unit tests) and the existing
  `selectedDateAndVisibleMonthSurviveActualActivityRecreation` in `CalendarSampleUiTest.kt`.

## Adaptive layout, Phase 6 (adaptive dialogs & heatmap demo, branch `update`)

- `HeatMapViewActivity.kt`: wrapped `HeatmapSample` in `CalendarSampleAdaptiveLayout`, providing
  two-pane layout on wide screens (>= 840 dp) and separating folds (book/tabletop posture).
- `CalendarSampleScreen.kt`: dialog month viewport height is now adaptive using `BoxWithConstraints`
  clamped between 240 dp and 360 dp to prevent clipping in landscape/compact height.

## Adaptive layout, Phase 5 (fold-aware sample, branch `update`)

- Added `androidx.window:window:1.5.1` (`libs.androidx.window`) to `:calendarviewsample` only,
  approved by the maintainer for this phase. `:library` and the engines stay dependency-free.
- `CalendarSampleFold.kt` reads the first separating `FoldingFeature`; `sampleLayoutKind` chooses
  Stacked / SideBySide / split by vertical fold (book) / split by horizontal fold (tabletop).
  A separating fold overrides the 840 dp width rule. JVM test: `SampleLayoutKindTest`.
- Compilation fix: typed `layoutFlow` explicitly to satisfy `collectAsState` (commit `a401f82`).
- Verified on CI: `Android verification` (run 43171433070, success), `Android publishing verification`
  (run 43171433056, success), and `Android instrumentation` (run 43171433218, success).
- Limitation: fold bounds are window coordinates, while the layout applies safe-drawing padding;
  offsets of a few dp near system bars are possible. Behaviour on a real foldable or foldable
  emulator is **not verified**; CI emulators have no fold, so fold branches are untested at runtime.

## Adaptive layout, Phase 4 (adaptive sample panes, branch `update`)

- Added `CalendarSampleAdaptiveLayout.kt`: below 840 dp available width the sample keeps the single
  scrolling column; from 840 dp it shows a controls pane (title, options, selection actions) beside
  a calendar pane. The decision uses `BoxWithConstraints`, so no new dependency was needed.
- Slots use `movableContentOf`, so display mode, scroll and viewport state should survive a layout
  switch. This is unverified at runtime; Phase 7 must test it.
- Not changed: viewport fixed heights (Phase 6), fold/posture handling (Phase 5, needs approval).
- Not compiled locally (Gradle unavailable); verification is via CI only.

## Adaptive layout, Phase 3 (sample refactor, branch `update`)

- Split `LauncherActivity.kt` without behaviour change: `CalendarSampleState.kt` (saveable UI
  state, `rememberCalendarSampleState`), `CalendarSampleControls.kt` (configuration rows) and
  `CalendarSampleScreen.kt` (screen plus demo month/week/agenda). The activity only hosts it.
- Prepares the adaptive panes of Phase 4. No library or API change.
- Not compiled locally (Gradle unavailable in this VM); verification is via CI only.

## Adaptive layout, Phase 2 (library constraint tests, branch `update`)

- Added JVM tests to `CalendarMonthColumnsTest` for the 336/376 dp thresholds, the narrow-container
  fallback and monotonic column counts. No public API change, so `api/*.api` is unchanged.
- Not run locally (Gradle unavailable in this VM); verification is via CI only.

## Adaptive layout, Phase 1 (docs only, branch `update`)

- Added [ADAPTIVE_LAYOUT.md](ADAPTIVE_LAYOUT.md): width formula (336/376 dp at 48 dp days),
  narrow-container fallback, active-month definition, 1↔N column anchor rule, state to preserve.
- Finding: the `CalendarMonthColumns` default `minMonthWidth = 320.dp` yields about 45.7 dp
  (no week numbers) or 40 dp (with week numbers) per day; only the height is enforced at 48 dp.
  Default intentionally unchanged.
- No code, API, dependency or build changes; nothing was built or tested locally.
- Open: phases 2–7, window-size/fold dependency approval, optional `maxColumns`/48 dp preset.

## Historical release status: 2.1.0 (published)

- Release commit `bd046a188d9a661e657affe580c7a7fa1a3e98ba` is tagged `2.1.0` and the
  GitHub Release `2.1.0` is published.
- Two complete API 29 instrumentation runs passed on the identical release tree:
  `2eb0fdbc5c1ec9d5cfbeae1570eddd7e9bf37a41` (run 43081287870) and
  `bd046a188d9a661e657affe580c7a7fa1a3e98ba` (run 43082385555). Remote verification,
  JVM tests, lint, publishing verification and the public API guard were green.
- JitPack built tag `2.1.0` successfully. Its build log reports `BUILD SUCCESSFUL` and
  lists AAR, POM, module metadata and sources for `library`, `calendar-gregorian`,
  `calendar-hijri` and `calendar-solarHijri`, plus the BOM POM/module metadata.
- A JitPack-only consumer resolved all four constrained modules at `2.1.0`; the BOM
  constraints match `2.1.0`, and `mavenLocal()` is absent from the consumer build.
- The `2.0.1` tag remains at `068c57ff90bd8a7e503788a7d951dca65f8f3953` and was not moved.

## Historical release: 2.0.1 (2026-11-11)

- Release commit `068c57ff90bd8a7e503788a7d951dca65f8f3953` is tagged `2.0.1` and the
  GitHub Release `2.0.1` is published.
- JitPack built the tagged commit successfully. The retrieved build log
  (`https://jitpack.io/com/github/mortezasun/android-calendarview/2.0.1/build.log`)
  reports `BUILD SUCCESSFUL in 1m` and lists AAR, POM, Gradle module metadata and sources
  artifacts for `library`, `calendar-gregorian`, `calendar-hijri` and
  `calendar-solarHijri` at `2.0.1`.
- Physical device acceptance remains waived as recorded below. No new physical-device
  testing is claimed.

## 2.1.0 API compatibility evidence

- The published `2.0.1` JitPack AARs were compared with the release: all 248
  public/protected JVM members from those AARs are present with identical descriptors.
  Hidden delegation overloads preserve the old `rememberCalendarSelectionState`,
  `CalendarMonthGrid`, Gregorian, Hijri and Solar Hijri month-composable signatures.
- The independent consumer compiles the same `2.0.1`-compatible screen against published
  `2.0.1` and against JitPack `2.1.0`; candidate-only source also compiles the Hijri
  provider and range-policy APIs.

### 2.1.0 JitPack consumer verification

The candidate repository was removed before this build. The consumer used
`--refresh-dependencies`, resolved every calendar artifact from JitPack and has no
`mavenLocal()` repository. The BOM supplied all four calendar versions; the consumer compiled
both the 2.0.1-compatible screen and the candidate-only provider/range-policy source.

| Consumer | Declared dependencies | Resolved from JitPack | Result |
|---|---|---|---|
| BOM + all three engines | `calendar-bom:2.1.0`; versionless `library` and engines | BOM constraints at `2.1.0`; AAR, POM, module and sources for all four modules; `ir.huri:jalalicalendar:2.0.0` | BUILD SUCCESSFUL |

### 2.0.1 consumer verification (historical)

The consumer is a standalone Gradle build (not part of this repository's module graph).
Its repositories are `google()`, `mavenCentral()` and `https://jitpack.io`; `mavenLocal()`
is absent, and it has no `project(...)` or `includeBuild(...)` reference to this
repository. `~/.m2` did not exist during the runs. Every configuration was built with
`--refresh-dependencies`, and the `--info` logs record the JitPack download URLs for the
`2.0.1` artifacts.

| Consumer configuration | Declared calendar dependency | Resolved published artifacts | Result |
|---|---|---|---|
| Gregorian only | `calendar-gregorian:2.0.1` | `calendar-gregorian:2.0.1`, `library:2.0.1` | BUILD SUCCESSFUL |
| Hijri only | `calendar-hijri:2.0.1` | `calendar-hijri:2.0.1`, `library:2.0.1` | BUILD SUCCESSFUL |
| Solar Hijri only | `calendar-solarHijri:2.0.1` | `calendar-solarHijri:2.0.1`, `library:2.0.1`, `ir.huri:jalalicalendar:2.0.0` | BUILD SUCCESSFUL |
| All three engines | all three `2.0.1` engines | `calendar-gregorian:2.0.1`, `calendar-hijri:2.0.1`, `calendar-solarHijri:2.0.1`, one `library:2.0.1` | BUILD SUCCESSFUL |

Each app module compiled a Compose screen that calls its engine's public composable; the
all-three screen renders `GregorianCalendarMonth`, `HijriCalendarMonth` and
`SolarHijriCalendarMonth` together.

### Runtime smoke test

- Device: API 29 AOSP x86_64 emulator with 4 GB RAM, running under software emulation
  (`-accel off`; KVM was not accessible to this runner).
- Test: `AllCalendarsSmokeTest.allThreeCalendarsRenderAndAcceptSelection` in the
  all-three consumer app. It launches the app, verifies the three calendar test tags,
  clicks the first clickable day in each calendar and asserts that each selection summary
  changes.
- Result: `OK (1 test)` in 119.056 s. The Gradle `connectedDebugAndroidTest` task skipped
  the emulator because ddmlib's device-property query timed out on the slow software
  emulator; the same test APK was executed directly through
  `adb shell am instrument`.
- Not covered: physical devices, additional API levels, locales, RTL, rotation or process
  death.

## Historical evidence

### 2.0.1 preparation session (historical)

The following describes the pre-release preparation state; the items listed as open were
completed by the release recorded above.

- PR #1 merged publishing commit `4b570f2af666fcc9ace532523a42326176215e52` into
  master as `d265b7fd314ee55285a1970ad5bbfe1ce4df1ed3`.
- The GitHub check-runs API confirmed all five new PR checks succeeded:
  - Verification Run 42980672262: assemble, unit-tests and lint.
  - Instrumentation Run 42980672194: Core and sample instrumentation.
  - Publishing Run 42980672274: isolated Maven publication and artifact checks.
- The owner reported on 2026-11-11 that JitPack for `4b570f2` was green. That was
  owner-reported evidence at the time; the `2.0.1` tag now has a retrieved build log.
- At the end of that session the candidate CI, tag/release, JitPack build and independent
  consumer verification were still open.

The sessions below retain their original verification scope. Earlier statements that tests
were unexecuted or a tag was missing describe those sessions, not the current overall state.
The old full candidate SHA was corrected to the actual `d552dec` commit in the final historical
section; the mapping of the older run IDs to that SHA still needs direct API confirmation.

## Historical verified state (2026-11-09)

This table is the historical 2026-11-09 CI row set, not the current branch state. The current
wrapper is Gradle `9.9.1` and the current test counts are in the post-3.0.0 tooling section above.

| Item | Value |
|------|-------|
| AGP | 9.5.1 |
| Gradle wrapper | 9.8.2 (historical; current branch `9.9.1`) |
| Kotlin (catalog) | 2.4.21; compilation via AGP 9 built-in Kotlin |
| Compose BOM | 2026.09.00 |
| `compileSdk` / `targetSdk` | 37 / 37 |
| `minSdk` | 27 |
| JDK used for verification | Temurin 17.0.21+9 |
| Android SDK used | Platform `android-37.0`, Build-Tools 36.0.0 |

Executed and verified on 2026-11-09 via GitHub Actions CI. The `test` count below belongs to that
historical run; it is not the current local count:

| Check | Scope / Command | Result |
|-------|-----------------|--------|
| Full build | `:calendarviewsample:assembleDebug` | **BUILD SUCCESSFUL** |
| JVM unit tests | `test` across all modules | **8 tests, 0 failures, 0 errors, 0 skipped** (all green) |
| Android Lint | `:library:lintDebug`, `:calendar-*:lintDebug`, `:calendarviewsample:lintDebug` | **BUILD SUCCESSFUL**; 0 errors |
| Core Compose instrumentation (API 29) | `:library:connectedDebugAndroidTest` | **5 tests, 0 failures, 0 errors** (all Compose selection tests pass) |
| Sample UI instrumentation (API 29) | `:calendarviewsample:connectedDebugAndroidTest` | **6 tests, 0 failures, 0 errors** (all 5 acceptance tests + context pass) |

Artifacts and test reports are generated and uploaded in CI:
- `android-assemble-42686121567-1`
- `android-unit-tests-42686121567-1`
- `android-lint-42686121567-1`
- `android-instrumentation-42686121582-1`

---

## Migration and Modernization (v2.0.0)

All 8 phases of the 2.0.0 modernization plan are complete:

1. **CI and Test Stability:**
   - Both `Android verification` and `Android instrumentation` workflows run on GitHub Actions with 100% pass rate.
   - Fixed Compose test scroll issues with inner scrollable containers and out-of-viewport touch target handling.
2. **Legacy API Audit & Mapping:**
   - Documented complete 1.x -> 2.0.0 mapping in [docs/V2_MIGRATION_MAPPING.md](V2_MIGRATION_MAPPING.md).
3. **Compose Feature Parity:**
   - Added `fixedSixRows` (42-cell uniform grid) for month transitions.
   - Added `showHeader` and `showWeekdayLabels` visibility controls.
   - Added today focus indicator with primary border highlight.
   - Verified selection states (None, Single, Multiple, Range) with consecutive epoch identities.
4. **Legacy Sample UI Migration:**
   - Removed `CalendarViewActivity`, legacy XML layouts, and dangling activity entries.
   - `LauncherActivity` and `HeatMapViewActivity` are 100% native Compose.
5. **Legacy View and ViewBinding Elimination:**
   - Deleted all legacy custom views (`MultiMonthView`, `MonthView`, `DayHeatMapView`, etc.).
   - Removed `viewBinding = true` and unused legacy dependencies across `:library` and calendar engines.
6. **Artifact Isolation:**
   - Decoupled `ir.huri:jalalicalendar` from `:library`; now strictly encapsulated in `:calendar-solarHijri`.
   - All modules compile and test independently.
7. **Edge Cases & Localization:**
   - Verified locales (`en`, `de`, `fa`, `ar`), RTL layout direction, and activity recreation state preservation.
8. **Documentation and Versioning:**
   - Aligned versions to `2.0.0`.
   - Updated `README.md`, `ARCHITECTURE.md`, and `V2_MIGRATION_MAPPING.md`.

---

## Session 2026-11-10: Heatmap configuration parity

Heatmaps now offer the month grid's configuration surface. All changes are additive; no public
name or parameter was removed or renamed.

- `:library` (`CalendarHeatmap.kt`):
  - New `HeatmapOrientation` (`WeeksAsColumns` default, `WeeksAsRows` month-grid layout) and
    `HeatmapStyle` (cell size, spacing, shape, weekday label width, border widths, disabled alpha;
    validated in `init`, defaults equal the previous geometry).
  - `HeatmapDayData` gained `isToday = false` and `enabled = true`.
  - `CalendarQuarterHeatmap` and both `CalendarYearHeatmap` overloads gained `showHeader`,
    `orientation`, `style`, `selection`, `todayBorderColor`, `selectedBorderColor`, `header` and
    `weekdayContent`; the Gregorian ones also `today` and `isDateEnabled`. The Gregorian
    `CalendarYearHeatmap` now also accepts `colorForIntensity` and `dayContent`.
  - New parameters sit before the trailing `colorForIntensity` / `dayContent`, so existing
    trailing-lambda calls still compile. All three composables share one private renderer.
- Sample: the heatmap screen adds selection modes, locale, RTL, first weekday, orientation,
  color palette, cell style, today-border color, header toggle and "disable future days".
  Mode `None` keeps the previous detail dialog. `sampleHeatmapYear` gained `firstDayOfWeek`
  and `isDateEnabled` and forwards `isToday` / `enabled` from the engines.

Verified on 2026-11-10 locally (JDK 17.0.21, Platform `android-37.0`, Build-Tools 36.0.0):

| Check | Result |
|-------|--------|
| `:calendarviewsample:assembleDebug` | BUILD SUCCESSFUL |
| `test` (all modules) | 70 tests, 0 failures, 0 errors, 0 skipped |
| `:library:lintDebug`, `:calendarviewsample:lintDebug` | BUILD SUCCESSFUL, 0 errors; no new warnings from this change |
| `:library:compileDebugAndroidTestKotlin`, `:calendarviewsample:compileDebugAndroidTestKotlin` | BUILD SUCCESSFUL (compiled only, not executed) |

Known limitations / open points:
- Instrumentation tests were not run (no emulator available); the new heatmap layouts, RTL and
  selection highlight are not device-verified and have no Compose UI test yet.
- Visible default change: the Gregorian heatmaps now draw a today border (default
  `today = LocalDate.now()`), matching the month views. Pass `today = null` for the old look.
- `HeatmapStyle.cellSize` below 48dp shrunk the touch target below the 48dp guideline. Resolved in
  the follow-up session below.
- Stale documentation: `AGENTS.md`, `docs/DEVELOPMENT.md`, `docs/ARCHITECTURE.md` and other status
  files still described the 1.x XML/ViewBinding architecture and APIs that no longer exist.
  Resolved in the follow-up session below.

---

## Session 2026-11-10 (follow-up): documentation, heatmap touch target, heatmap UI tests

Closes the open points of the previous session. No public composable, parameter or type was renamed
or removed; the only behaviour change is the heatmap touch target described below.

- `:library` (`compose/CalendarHeatmap.kt`):
  - `HeatmapCell` now separates the interactive slot (`slotModifier`) from the drawn cell
    (`visualModifier`). The slot is
    `max(HeatmapStyle.cellSize, LocalMinimumInteractiveComponentSize)`, so a cell smaller than 48dp
    keeps a full-size touch target while the visible cell stays at `cellSize`.
  - Default geometry is unchanged: `HeatmapStyle()` still produces 48dp cells on a 48dp pitch.
  - The weekday label column and the `null` padding cells use the same slot size, so labels and
    grid stay aligned.
- `:library` `androidTest`: new `CalendarHeatmapComposeTest` (6 tests) covers `WeeksAsRows` label
  rendering and click reporting by epoch day, `WeeksAsColumns` scrolling plus click reporting, RTL
  weekday mirroring, caller-owned selection semantics, disabled days and the minimum touch target
  at `cellSize = 24.dp`.
- Documentation aligned to the Compose v2 state: `AGENTS.md` (architecture rules and verified
  commands), `docs/DEVELOPMENT.md` (real file paths, engine and option patterns, actual test
  state), `docs/ARCHITECTURE.md` (container names, heatmap slot behaviour), `docs/SETUP.md` (test
  state, `ir.huri:jalalicalendar` ownership, removed manifest problem).
  `docs/V2_IMPLEMENTATION_STATUS.md` is now a superseded pointer at this file.
  `docs/COMPOSE_TESTS.md`, `docs/SAMPLE_UI_TESTS.md`, `docs/CI_FAILURES.md` and `docs/V2_AUDIT.md`
  were corrected for the same consistency.

Executed on 2026-11-10 locally (Temurin 17.0.21+9, Platform `android-37.0`, Build-Tools 36.0.0):

| Check | Result |
|-------|--------|
| `test` (all modules) | 70 tests, 0 failures, 0 errors |
| `:library:compileDebugAndroidTestKotlin` | BUILD SUCCESSFUL (compiled, not executed) |
| `:library:lintDebug` | BUILD SUCCESSFUL, no lint errors |
| `:calendarviewsample:assembleDebug` | BUILD SUCCESSFUL |

Remaining limitations:
- No emulator or device was available. The six new heatmap UI tests and the five existing selection
  UI tests are compiled but not executed, so their runtime behaviour is unverified.
- `CalendarHeatmapComposeTest` uses `createComposeRule`, which now reports a deprecation warning in
  favour of the `junit4.v2` variant. It matches the existing `CalendarSelectionComposeTest`; the
  migration was not part of this change.
- No release tag was created and no version was bumped. That stays a separate, deliberate step per
  `docs/decisions/0002-compose-v2-shared-core.md`.
- No independent JitPack consumer build was performed here.

CI for commit `5e736f6` completed successfully:

| Workflow | Run | Conclusion |
|----------|-----|------------|
| `Android verification` | 42890377741 | success |
| `Android instrumentation` (API 29) | 42890377813 | success |

These conclusions were read from the GitHub Actions API for that exact `head_sha`. The job logs and
per-test counts were not downloaded, so this records the workflow outcome, not a per-test review.

---

### Correction: `WeeksAsRows` width is not a touch-target guarantee

A review found that the paragraph above over-states the guarantee for
`HeatmapOrientation.WeeksAsRows`. That orientation shares the row width between seven columns
(`Modifier.weight(1f)`), so the slot width is `(viewport - 6 * cellSpacing) / 7` and falls below
48dp on viewports narrower than roughly `7 * 48dp + 6 * cellSpacing` (for example a 320dp
split-screen window). Only the slot height carries the minimum there.

- Behaviour is unchanged: `WeeksAsColumns` already sizes both slot edges with
  `Modifier.size(slotSize)`.
- `HeatmapStyle` KDoc, `README.md`, `docs/ARCHITECTURE.md`, `AGENTS.md` and `docs/DEVELOPMENT.md`
  now scope the guarantee per orientation instead of claiming it for every cell.
- `CalendarHeatmapComposeTest` checks both edges in `WeeksAsColumns`, and for `WeeksAsRows` at a
  300dp viewport the height minimum plus the equal column share.

Enforcing a 48dp width in `WeeksAsRows` would need horizontal scrolling for a seven-column month
grid or dropping the equal-width fill; both change the documented layout, and `CalendarMonthGrid`
shares the same seven-column constraint. That stays an open product decision rather than a silent
fix.

---

## Session 2026-11-10: Maven publishing for 2.0.0

The repository had no publishing configuration, so the installation coordinates documented in
`README.md` could not be resolved by any consumer and JitPack had no artifact to build.

- All four library modules now apply `maven-publish`, publish the `release` variant and ship a
  sources jar. `:calendarviewsample` stays excluded.
- `group` and `version` are defined once in the root `build.gradle.kts`
  (`com.github.mortezasun.android-calendarview`, `2.0.0`); each artifact id is the module name.
- New `jitpack.yml` pins `openjdk17`, which AGP 9.5 requires.

Verified locally on 2026-11-10:

| Check | Result |
|-------|--------|
| `publishToMavenLocal` for all four modules | BUILD SUCCESSFUL |
| `test` (all modules) | 70 tests, 0 failures, 0 errors |
| `:calendarviewsample:assembleDebug` | BUILD SUCCESSFUL |

POM inspection:

- `...:library:2.0.0` imports `androidx.compose:compose-bom` through `dependencyManagement` and
  lists `runtime`, `runtime-saveable`, `ui` and `foundation` in compile scope.
- `...:calendar-gregorian:2.0.0` and the other engines depend on `...:library:2.0.0` (compile).
- `...:calendar-solarHijri:2.0.0` additionally carries `ir.huri:jalalicalendar:2.0.0`.
- Every module publishes `<module>-2.0.0.aar`, `.pom`, `.module` and `-sources.jar`.

Still open at that point: an independent consumer project and the `2.0.0` tag. The `mavenLocal`
part is recorded in the next section; the JitPack preflight is still open.

---

## Session 2026-11-10: consumer test and CI status

- An independent consumer project (`artifacts/consumer-test`, untracked) builds against the
  artifacts from `publishToMavenLocal`, with no `project(...)` and no `includeBuild(...)`.
  All four configurations build: Gregorian only, Hijri only, Solar Hijri only, and all three
  engines sharing one selection.
- The resolved compile classpath confirms the published metadata:
  `calendar-gregorian -> library -> androidx.compose:compose-bom`, `calendar-hijri -> library`,
  `calendar-solarHijri -> library` plus `ir.huri:jalalicalendar`. A consumer needs no own BOM
  import; the versions arrive through the library metadata.
- CI for `ece9864`: `Android verification` success, `Android instrumentation` (API 29) success.
- Correction: the instrumentation workflow had failed for `46e66605` and `c894246`. The failing
  assertion was the exact dp comparison of two weighted column widths in
  `weeksAsRowsKeepsMinimumHeightWhenTheViewportIsNarrow`; weighted columns split the row into
  whole pixels, so exact equality is not a contract. The test now asserts the documented 48dp
  slot height plus click reporting. The failure message itself was never available - the job logs
  require authentication and no KVM was available for a local emulator - so this is a strong
  indication, not a log-confirmed diagnosis.

Still open: the JitPack preflight with a commit hash, the release notes, the manual acceptance
pass and the `2.0.0` tag.

---

## Session 2026-11-10: Release verification and 2.0.0 release

Verification of candidate `d552decb8f7382e883e705533079664e76ab1158` (corrected repository SHA;
the historical run-to-SHA mapping still needs direct confirmation):
- CI workflow `android-verification.yml` was extended to assemble release AARs for all 4 library modules (`:library:assembleRelease`, `:calendar-gregorian:assembleRelease`, `:calendar-hijri:assembleRelease`, `:calendar-solarHijri:assembleRelease`) in addition to `:calendarviewsample:assembleDebug`, and to capture the release AAR artifacts.
- CI outcomes for `d552dec`:
  - `Android verification` (Run 42911012589): SUCCESS across all jobs (assemble, unit-tests, lint).
    - Assembled artifacts: `library-release.aar`, `calendar-gregorian-release.aar`, `calendar-hijri-release.aar`, `calendar-solarHijri-release.aar`, and `calendarviewsample-debug.apk`.
    - Unit tests: 70 tests passed, 0 failures, 0 skipped.
    - Lint: 0 errors across all 5 modules.
  - `Android instrumentation` (Run 42911012594): SUCCESS on API 29.
    - 19 automated instrumented tests (12 in `:library`, 7 in `:calendarviewsample`) covering all selection modes, navigation, modal dialogs, heatmaps in both orientations with touch targets, RTL, and activity recreation.
- Acceptance decision:
  - Automated instrumentation suite passed on API 29.
  - Physical device manual acceptance waived with documented residual risk per explicit owner directive.
- Tag and release:
  - Version `2.0.0` tagged on the release commit.
  - GitHub Release published.

---

## Session 2026-11-11: Phase 1 - shared month data, navigation and SDK guard

Additive public work; no existing public name or parameter was removed or renamed.

- `:library`:
  - `CalendarMonthData` contract and `toHeatmap()` adapters (intensity lambda and map overload).
  - `CalendarMonthNavigationState` and `CalendarMonthHeader` with heading semantics,
    caller-localized content descriptions, an optional today action and bounds-aware enabled states.
  - `CalendarSelection` has structural `equals`/`hashCode`/`toString` and is `@Immutable`;
    membership uses an internal set. `CalendarSelectionState` is `@Stable`; month and heatmap data
    classes are `@Immutable`.
  - `CalendarMonthGrid` adds optional `todayDescription`, heading semantics, one
    `CollectionInfo` on the week container and `CollectionItemInfo` per day/week-number cell.
- Engines:
  - `GregorianMonthData`, `HijriMonthData` and `SolarHijriMonthData` implement
    `CalendarMonthData` and are `@Immutable`.
  - `GregorianMonthState`, `HijriMonthState` and `SolarHijriMonthState` plus saveable
    `remember*MonthState`; inclusive bounds, first-day normalization, titles and today navigation.
  - Engine month composables forward `todayDescription`; the sample supplies localized demo text.
- API guard: `scripts/api-check.sh` compares `javap` dumps of the four release AARs against
  `api/*.api`; CI runs it in the assemble job. The binary-compatibility-validator plugin was
  evaluated but creates no tasks with AGP 9 built-in Kotlin, so it is not used.

Executed on 2026-11-11 locally (Temurin 17.0.21+9, Platform `android-37.0`, Build-Tools 36.0.0):

| Check | Result |
|-------|--------|
| `test` (all modules) | 87 tests, 0 failures, 0 errors |
| `:calendarviewsample:assembleDebug` | BUILD SUCCESSFUL |
| `:library:lintDebug`, `:calendar-gregorian:lintDebug`, `:calendar-hijri:lintDebug`, `:calendar-solarHijri:lintDebug` | BUILD SUCCESSFUL, 0 errors |
| `:library:compileDebugAndroidTestKotlin` | BUILD SUCCESSFUL (compiled only, not executed) |
| Release AARs for all four library modules | BUILD SUCCESSFUL |
| `bash scripts/api-check.sh` | Public API matches the checked-in dumps |

Remaining limitations:
- No emulator or device was available. The new `CalendarMonthHeaderComposeTest` (2 tests) and the
  existing instrumentation suites were compiled but not executed.
- The API guard covers public JVM signatures only, not Kotlin default values, annotations or
  source compatibility. `CalendarSelectionSnapshot` and `solarHijriMonthTitle` are intentionally
  excluded as Kotlin-internal.
- No version bump, tag, JitPack consumer build or physical-device acceptance was performed.

---

## Session 2026-11-11: Phase 2 - week, agenda, markers and adaptive product surface

Additive public work; existing names, parameters and source calls remain valid.

- `:library`:
  - `CalendarWeekData` / `CalendarWeekGrid`: seven-day view with week-number and marker support.
  - `CalendarAgenda`: bounded vertical day list with selection, markers, today styling and
    accessible day descriptions.
  - `CalendarDayMarker` plus a `markers` map on the month, week and agenda views. Descriptions
    join the day's accessibility label; at most four dots are drawn per cell.
  - Optional `weekNumber` callback on the month grid and a caller-formatted week number for the
    week grid. No week-number convention is imposed by the library.
  - `CalendarMonthColumns`: width-adaptive multi-month layout with a caller-owned `LazyGridState`.
  - `CalendarStyle` / `LocalCalendarStyle`: optional selected/surface/content/today colors and
    marker geometry, falling back to MaterialTheme. `todayBorderColor` is now nullable.
  - `CalendarRangePolicy` and `CalendarSelectionState.selectRange()`: inclusive min/max range
    validation shared by taps and a future/caller-owned drag layer.
- Engines:
  - `GregorianWeekData` / `gregorianWeekData`, `HijriWeekData` / `hijriWeekData` and
    `SolarHijriWeekData` / `solarHijriWeekData`; all return seven consecutive epoch days.
  - Engine month wrappers forward `markers` and `weekNumber` and accept nullable today colors.
- Sample: month views include event markers; a switch adds the matching engine week view.
- Tests: range-policy/state, marker descriptions, adaptive column calculation and all three week
  builders; one compile-only Compose class covers week/agenda rendering and clicks.

Executed locally on 2026-11-11 (Temurin 17.0.21+9, Platform `android-37.0`, Build-Tools 36.0.0):

| Check | Result |
|-------|--------|
| `test` (all modules) | 99 tests, 0 failures, 0 errors |
| `:calendarviewsample:assembleDebug` | BUILD SUCCESSFUL |
| `:library:compileDebugAndroidTestKotlin` | BUILD SUCCESSFUL (compiled only, not executed) |
| `:library:lintDebug`, `:calendar-gregorian:lintDebug`, `:calendar-hijri:lintDebug`, `:calendar-solarHijri:lintDebug`, `:calendarviewsample:lintDebug` | BUILD SUCCESSFUL, 0 errors |
| Release AARs for all four library modules | BUILD SUCCESSFUL |
| `bash scripts/api-check.sh` | Public API matches the checked-in dumps |

Remaining limitations:
- No emulator or device was available. `CalendarProductViewsComposeTest` and the existing
  instrumentation suites were compiled but not executed.
- Drag-range gestures, date/month picker dialogs, an infinite pager, a separate test artifact,
  screenshot tests and Baseline Profiles are not part of this slice. They need new dependencies
  and/or an emulator and remain future Phase 2 work.
- The week-number callback is intentionally caller-owned; Gregorian data supplies a deterministic
  week number, while Hijri and Solar Hijri leave it null because no shared convention exists.

---

## Session 2026-11-11: Phase 3 - BOM, POM metadata and Hijri method hook

Additive public work. Existing Hijri function signatures are retained as overloads; the new
provider variants add declarations rather than removing or renaming existing calls.

- Distribution:
  - New `:calendar-bom` `java-platform` module with constraints for `library`,
    `calendar-gregorian`, `calendar-hijri` and `calendar-solarHijri` at the root version.
  - Root `build.gradle.kts` now supplies the shared Maven POM fields (name, description, URL,
    Apache-2.0 license, developer, SCM) to every `maven-publish` publication.
  - `settings.gradle.kts`, `jitpack.yml`, the publishing workflow and the verification workflow
    include the BOM. Publishing CI verifies its `.pom` and `.module` artifacts separately from
    the AAR modules.
- Hijri method hook:
  - `HijriDateProvider` maps absolute epoch days to `HijrahDate` and back.
    `UmmAlQuraHijriDateProvider` is the default; `OffsetHijriDateProvider` applies a uniform
    local-method offset.
  - `hijriMonthData`, `hijriWeekData` and `HijriCalendarMonth` have provider overloads. The
    original signatures remain as delegating overloads, so previously compiled callers keep
    their JVM entry points.
  - Provider-backed cells keep the absolute epoch-day identity separate from the displayed
    Hijri date, so selection and click callbacks remain consistent under an offset.
  - Tests cover round-trip mapping, positive and negative offsets, month/week generation and
    default-provider equivalence.
- Documentation: README, ARCHITECTURE, DEVELOPMENT, SETUP, AGENTS and ADR 0003 describe the BOM,
  the provider and the deferred strategic decisions.

Executed on 2026-11-11 locally (Temurin 17.0.21+9, Platform `android-37.0`, Build-Tools 36.0.0):

| Check | Result |
|-------|--------|
| `test` (all modules) | 104 tests, 0 failures, 0 errors, 0 skipped |
| `:calendarviewsample:assembleDebug` | BUILD SUCCESSFUL |
| `:library:lintDebug`, `:calendar-gregorian:lintDebug`, `:calendar-hijri:lintDebug`, `:calendar-solarHijri:lintDebug`, `:calendarviewsample:lintDebug` | BUILD SUCCESSFUL, 0 errors |
| Release AARs for all four library modules | BUILD SUCCESSFUL |
| BOM `publishToMavenLocal` | BUILD SUCCESSFUL; `.pom` and `.module` present |
| `bash scripts/api-check.sh` after `--update` | Public API matches the checked-in dumps |

Remaining limitations:
- No emulator or device was available, so the Compose instrumentation tests were not executed.
- Maven Central signing, namespace verification and upload are not configured. The POM metadata
  is present, but no Maven Central release was performed.
- Kotlin Multiplatform `:core`, replacement of `ir.huri:jalalicalendar`, a Play demo and a hosted
  documentation site were deferred pending their architectural/product decisions (ADR 0003).
- The existing `2.0.1` release tag predates the BOM; it becomes consumable from JitPack only from a
  newer tag or commit build.
- No release tag or version bump was performed; the Phase 3 changes are committed and pushed to
  `update`.

### Next step: release and consumer-verify the BOM

The immediate next step is a versioned release containing `:calendar-bom`, followed by an
independent consumer build. The existing `2.0.1` tag cannot prove the BOM or the provider API.
The proposed version is `2.1.0` because the change is additive but adds a public Hijri provider
API and a new published artifact.

Acceptance criteria:

1. Decide and apply the version (`2.1.0` proposed), then create the annotated tag.
2. Let JitPack build the tag and confirm the BOM POM, module POM and all AAR artifacts.
3. Build a standalone consumer that imports the BOM without explicit calendar versions and uses
   the Hijri provider overloads; verify against JitPack with no `mavenLocal()`.
4. Only after that, add Maven Central signing, namespace verification and release credentials.

KMP extraction, the Jalali replacement, the Play demo and the hosted documentation site remain
later decisions; they are not part of the BOM release gate.

---

## Session 2026-11-11: sample app showcase for the v2 APIs

Sample-only changes, no library API or dependency changes. The demo now exercises the recent
additive APIs in the running app instead of documenting them only in the library:

- `LauncherActivity` adds switches for agenda, week numbers, a custom `CalendarStyle`, the
  library `CalendarMonthHeader`, a 2-14 day `CalendarRangePolicy` and Hijri method offsets.
  The selection row can call `selectRange` directly.
- The display-mode selector includes `CalendarMonthColumns` for adaptive month columns.
- `sampleMonthData` exposes one `CalendarMonthData` snapshot for all three engines; the agenda
  consumes it. `sampleWeekNumber` supplies host-formatted week numbers because the library
  deliberately leaves that convention to the caller.
- `rememberSampleMonthNavigation` bridges the controlled sample anchor to the engine-specific
  saveable month states and the library month header.
- The heatmap demo now maps through `CalendarMonthData.toHeatmap { ... }` and exposes the Hijri
  provider offset there as well.
- New JVM tests cover provider-backed agenda month data, host week numbers and the Hijri
  heatmap offset.

Executed on 2026-11-11 locally (Temurin 17.0.21+9, Platform `android-37.0`, Build-Tools 36.0.0):

| Check | Result |
|-------|--------|
| `test` (all modules) | 107 tests, 0 failures, 0 errors, 0 skipped |
| `:calendarviewsample:assembleDebug` | BUILD SUCCESSFUL |
| `:calendarviewsample:compileDebugAndroidTestKotlin` | BUILD SUCCESSFUL; one deprecation warning for the v1 Compose test rule |
| `:calendarviewsample:lintDebug` | BUILD SUCCESSFUL, 0 errors |

Remaining limitations:
- No emulator or device was available. The new Compose controls were not visually exercised,
  and the instrumentation tests were only compiled, not executed.

---

## Session 2026-11-11: Review fixes for Hijri identity, invariants and accessibility

Follow-up to the four-commit review. The changes are additive at the public JVM level;
old constructors and helper overloads remain present.

- Hijri identity:
  - Month offsets and week starts are derived from the absolute epoch-day weekday, not from the
    provider-displayed HijrahDate. Month and week cells now line up with their weekday labels
    under `-1`, `0` and `+1` methods.
  - `HijriCalendarMonth` documents that `onDateClick` receives the displayed date; the sample
    and README map it back with `HijriDateProvider.epochDayFor()` before storing selection.
  - `HijriMonthState` and `rememberHijriMonthState` gained provider-aware overloads. The sample
    navigation, month/week data, agenda data, heatmap year and disabled-date predicate all use
    the same provider. The three-argument state API remains Umm al-Qura.
- Selection invariants:
  - `CalendarSelectionState` validates complete initial ranges and complete restored ranges.
    Invalid restoration falls back to initialization; incomplete first endpoints remain valid.
- Layout and accessibility:
  - `calendarMonthColumns` accounts for `horizontalSpacing`; the existing two-argument helper
    remains as a delegating overload.
  - The month grid exposes one `CollectionInfo` on its week container and item info on day and
    optional week-number cells. Default header arrows mirror in RTL.
- Distribution docs: the README marks the `2.1.0` BOM example as unreleased; the existing
  `2.0.1` tag still has no BOM. API dumps contain only the three additive signatures.
- Tests: provider-backed month/week alignment, provider state today, sample month boundary and
  range-constructor rejection were added.

Executed on 2026-11-11 locally (Temurin 17.0.21+9, Platform `android-37.0`, Build-Tools 36.0.0):

| Check | Result |
|-------|--------|
| `test` (all modules) | 113 tests, 0 failures, 0 errors, 0 skipped |
| `:calendarviewsample:assembleDebug` | BUILD SUCCESSFUL |
| `lintDebug` for library, Gregorian, Hijri, Solar Hijri and sample | BUILD SUCCESSFUL, 0 errors |
| AndroidTest compilation for library, Hijri and sample | BUILD SUCCESSFUL (compiled only, not executed) |
| Release AARs for all four library modules | BUILD SUCCESSFUL |
| `publishToMavenLocal` for all modules and BOM | BUILD SUCCESSFUL |
| BOM POM constraints | library, Gregorian, Hijri and Solar Hijri at `2.0.1` |
| `bash scripts/api-check.sh` after `--update` | Public API matches the checked-in dumps |

Remaining limitations:
- No emulator or device was available. Instrumented tests, visual RTL arrows, semantics and the
  new sample interactions were not executed.
- The API guard is a static JVM-signature check. No standalone consumer binary compiled against
  the published `2.0.1` artifacts was built.
- No version bump, tag, JitPack consumer build, Maven Central upload or release was performed. The
  `2.1.0` BOM example remains explicitly unreleased.

---

## Session 2026-11-11: verified main-viewport selection and instrumentation diagnostics

Branch `update`; no release version, tag or publication was changed in this session.

- `b71ed14600281e86bfbc9bcb459fad476c44732c` (baseline): Android instrumentation run
  `43057537150` succeeded, but that run predates the public test-count summary. The previous
  dialog-only test did not exercise the failing main-viewport selection path.
- `1267142` added the separate semantics and touch main-viewport tests, plus the split caption
  content/visibility contracts. Its first CI run `43064116534` failed because the day was below
  the small emulator screen; the inner calendar scrolled, not the outer sample page.
- `f7010a5` fixed the test setup by targeting the outer scroll container explicitly. Android
  instrumentation run `43065898948` succeeded on the full core and sample suite. Both
  main-viewport tests, the dialog restoration test and the caption tests were green.
- `7320fe2` scoped the failure reporter to `steps.instrumentation.outcome == 'failure'`, added a
  repeatable Python test suite for the reporter and added public JUnit count annotations.
  Android verification `43068767081` and Android publishing verification `43068767401` succeeded
  for this commit. The subsequent final instrumentation run `43070445246` confirmed the count:
  16 library and 10 sample tests, 0 failures, 0 errors and 0 skips (7 classes).
- The checked-in instrumentation inventory is 26 tests (16 library, 10 sample). The sample suite
  keeps the dialog path and the main-viewport path as separate contracts; the main-viewport tests
  use the real `onDayClick` callback and check both `Selected` semantics and the selection
  summary. Activity recreation is covered; process death is not.

Local checks for `7320fe2` (Temurin 17.0.21+9, Platform `android-37.0`, Build-Tools 36.0.0):

| Check | Result |
|-------|--------|
| `test` (all modules) | 113 tests, 0 failures, 0 errors, 0 skipped |
| `:calendarviewsample:assembleDebug` | BUILD SUCCESSFUL |
| `:library:assembleDebugAndroidTest` and `:calendarviewsample:assembleDebugAndroidTest` | BUILD SUCCESSFUL |
| `lintDebug` for library, Gregorian, Hijri, Solar Hijri and sample | BUILD SUCCESSFUL, 0 errors |
| Python reporter suite (`scripts/test_report_instrumented_failures.py`) | 8 tests, OK |
| Instrumentation on API 29 | run `43065898948` (f7010a5) successful; final run `43070445246` (d8524da): 26 tests, 0 failures, 0 errors, 0 skipped (16 library, 10 sample) |

Remaining limits: no local emulator or physical device was available; API 29 is not a full device
matrix; the main-viewport tests use the current month's first day and do not cover drag-range
selection.

