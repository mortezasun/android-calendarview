# Project status

Last updated: 2026-11-11
Version: `2.1.0` (published)
Branch: `update`

## Adaptive layout, Phase 7 (state restoration & recreation tests, branch `update`)

- Added `CalendarSampleStateTest.kt` covering anchor updates, month moves, engine switches and
  display settings preservation.
- Added `calendarEngineAndDisplayOptionsSurviveActivityRecreation` to `CalendarSampleUiTest.kt`,
  verifying that engine switch and state survive activity recreation.

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

## Current release status: 2.1.0 (published)

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

## Previous release: 2.0.1 (2026-11-11)

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

## Verified state

| Item | Value |
|------|-------|
| AGP | 9.5.1 |
| Gradle wrapper | 9.8.2 |
| Kotlin (catalog) | 2.4.21; compilation via AGP 9 built-in Kotlin |
| Compose BOM | 2026.09.00 |
| `compileSdk` / `targetSdk` | 37 / 37 |
| `minSdk` | 27 |
| JDK used for verification | Temurin 17.0.21+9 |
| Android SDK used | Platform `android-37.0`, Build-Tools 36.0.0 |

Executed and verified on 2026-11-09 via GitHub Actions CI. This block is the historical CI row set;
the `test` count below belongs to that run. The current local count is 70 - see the sessions at
the end of this file:

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

