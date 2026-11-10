# Project status

Last updated: 2026-11-10
Version: `2.0.0`
Branch: `update`

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
