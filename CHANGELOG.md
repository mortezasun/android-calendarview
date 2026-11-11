# Changelog

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
