# Changelog

## 2.0.1 - Unreleased

Publishing patch for the Compose 2.0 release. No calendar rendering, selection API or
minimum Android version changes are introduced by this patch.

### Fixed

- Invoke the Gradle wrapper through Bash on JitPack instead of relying on its executable bit.
- Explicitly publish the core and all three calendar engines in the JitPack install step.
- Correct release verification documentation and the historical candidate SHA reference.

### Added

- Publishing verification CI with an isolated Maven repository and checks for nonempty
  AAR, POM, Gradle module metadata and sources artifacts for all four modules.

### Verification scope

All five PR checks for publishing commit `4b570f2` succeeded before merge into master
(PR #1, merge commit `d265b7f`). The owner reports that JitPack also built that commit
successfully. This is not a claim that `2.0.1` has already been tagged, built on JitPack
or consumed by an independent project. The new candidate needs its own CI results.

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
