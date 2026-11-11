# Development guide

## Where typical changes belong

| Task | Location |
|------|----------|
| Change selection behaviour (single/multiple/range) | `library/.../selection/CalendarSelection.kt` (`onDayClick`); saved state in `compose/CalendarSelectionState.kt` |
| Add a configuration switch | The public composable in `library/.../compose/CalendarMonthGrid.kt` or `CalendarHeatmap.kt` (defaulted parameter, forwarded to the renderer) |
| Add a view mode (list/pager direction) | `library/.../compose/CalendarMonthContainers.kt` (`CalendarMonthList`, `CalendarMonthPager`, `CalendarScrollAxis`) |
| Change month grid geometry | `library/.../compose/CalendarMonthGrid.kt` and `CalendarMonthCells.kt` (`buildCalendarMonthCells`, `fixedSixRows`) |
| Add a calendar system | New Gradle module with a `*MonthData` builder and a `*CalendarMonth` composable, modeled on `:calendar-gregorian` |
| Change day formatting | The engine's `*MonthData.kt` / `*DayCell` code in its own module; the grid only renders `CalendarDayCell` |
| Change the Hijri method | `calendar-hijri/.../HijriDateProvider.kt`; pass the provider through `hijriMonthData`, `hijriWeekData` and `HijriCalendarMonth` |
| Change heatmap rendering | `library/.../compose/CalendarHeatmap.kt` (`HeatmapBlock`, `HeatmapCell`) |
| Change heatmap data handling | `heatmapQuarterData` (Gregorian) / `CalendarMonthData.toHeatmap()`; see `calendarviewsample/.../CalendarSampleHeatmap.kt` |
| Add month navigation | `:library` `compose/CalendarMonthHeader.kt` (state contract and header); the engine's `compose/*MonthState.kt` (month arithmetic and saved state) |
| Add a shared month field | `:library` `compose/CalendarMonthData.kt` and every engine `*MonthData` implementation |
| Add a week or agenda view | `:library` `compose/CalendarWeekGrid.kt` / `CalendarAgenda.kt`; engine `compose/*WeekData.kt` |
| Add a marker or week-number column | `:library` `compose/CalendarMarkers.kt` and `CalendarMonthGrid.kt` |
| Add an adaptive month container | `:library` `compose/CalendarMonthColumns.kt` |
| Change range validation | `:library` `selection/CalendarRangePolicy.kt` and `compose/CalendarSelectionState.kt` |
| Change optional cell colors | `:library` `compose/CalendarStyle.kt` and the grid/agenda fallbacks |
| Change sample screen wiring | `calendarviewsample/.../CalendarSampleWindow.kt`, `CalendarSampleNavigation.kt`, `CalendarSampleViewport.kt`; entry points `LauncherActivity.kt` and `HeatMapViewActivity.kt` |

Keep the dependency direction intact: `:library` must not import classes from the calendar modules
or from the sample. Calendar-specific code stays in its module.

## Patterns for new features

### New calendar engine

1. Create a module like `:calendar-gregorian` (same `build.gradle.kts` shape, dependency on
   `:library`).
2. Implement the month data builder (`*MonthData.kt`), mirroring `gregorianMonthData`:
   - localized month caption and weekday labels,
   - `firstDayOffset` relative to the configured `firstDayOfWeek`,
   - month length and one `CalendarDayCell` per day with epoch day, label, accessibility text,
     `isToday` and `enabled`,
   - implement `CalendarMonthData` so `toHeatmap()` and generic helpers work without adapters.
3. Implement the public composable (`*CalendarMonth.kt`): a thin wrapper around
   `CalendarMonthGrid` that forwards every configuration parameter.
4. Add a saveable `*MonthState` in the same module with the engine's month arithmetic,
   inclusive bounds and `CalendarMonthNavigationState`; the shared header stays in `:library`.
5. Add the module to `settings.gradle.kts` and to the consumer's dependencies.
6. Add `*MonthDataTest` coverage next to the existing engine tests.
7. If the engine has an alternative date method, expose a stateless provider as
   `:calendar-hijri` does with `HijriDateProvider`; do not put method-specific state in
   `:library` or a composition local.

Epoch days are the only identity shared across engines, and the month arithmetic is called for
every month in the window, so it must be pure and must not depend on the day currently selected.

### New composable option

`CalendarMonthGrid`, the three engine month composables, the week/agenda views and the heatmaps
are controlled and stateless. To add an option:

1. Add it as a defaulted parameter on the public composable, before the trailing `dayContent`
   slot (and before `colorForIntensity` for heatmaps), so existing trailing-lambda calls keep
   compiling. For a function with a trailing predicate, keep the predicate last and place the
   new parameter before it.
2. Forward it in the engine wrapper and, for heatmaps, through `HeatmapRenderConfig`; forward
   week/marker options through `CalendarWeekGrid` and `CalendarAgenda` where they apply.
3. Geometry belongs in `HeatmapStyle` (validated in its `init`) or the marker/style properties;
   per-day state belongs in `CalendarDayCell` / `HeatmapDayData` with a default value.
4. Do not add internal mutable state for a value the caller already owns. Range limits belong in
   `CalendarRangePolicy`; a drag gesture remains the host's responsibility and calls
   `CalendarSelectionState.selectRange`.

### Error handling and logging

The library is deliberately small:

- Invalid configuration fails a `require` or throws `IllegalArgumentException`
  (`HeatmapStyle` init, `heatmapQuarterData`, `CalendarSelection.from`).
- `CalendarSelection.from` rejects too many days for the mode instead of truncating silently;
  guard at the call site rather than swallowing the exception.
- The library does not log. Do not add logging, error-handling frameworks or coroutines for
  synchronous rendering.

### Comments

- Write KDoc for public types and for non-obvious private helpers.
- Comments explain **why** (constraints, invariants, calendar quirks), not **what** the next line
  does. Example: the manual month arithmetic in `hijriMonthData` explains why it exists, because
  `HijrahDate` has no `plusMonths`.
- Existing KDoc is English; keep new comments in English for consistency.
- Fix a stale comment only when the code proves it wrong; otherwise leave it and note the
  discrepancy.

## Build, test and review workflow

1. Read [ARCHITECTURE.md](ARCHITECTURE.md) and the relevant source file before editing.
2. Make the smallest change; do not reformat untouched lines.
3. Compile the affected module:
   ```bash
   ./gradlew :library:compileDebugKotlin
   ```
   For changes that touch several modules or the sample, build the app:
   ```bash
   ./gradlew :calendarviewsample:assembleDebug
   ```
4. Run `./gradlew test` (JVM tests for selection, month cells, heatmap data and every engine).
   The Compose UI and sample acceptance tests live in `src/androidTest` and need an emulator or
   device; `./gradlew :library:compileDebugAndroidTestKotlin` only proves they compile.
5. Run `./gradlew :library:lintDebug` when the change is non-trivial.
6. Inspect `git diff` and `git status`: only intended files, no build output, no unrelated mode
   changes.
7. Update the documents listed in [AGENTS.md](../AGENTS.md#documentation-to-update).

If a change affects rendering, a manual check in the sample app is required; CLI builds do not
verify layout, RTL behaviour or selection visuals.

## Public API guard

The published AARs have a checked-in public JVM API dump in `api/*.api`. After changing a public
declaration, assemble the release AARs and run:

```bash
./gradlew :library:assembleRelease :calendar-gregorian:assembleRelease \
  :calendar-hijri:assembleRelease :calendar-solarHijri:assembleRelease
bash scripts/api-check.sh          # fails on an unexpected API diff
bash scripts/api-check.sh --update # regenerates api/*.api after an intentional change
```

CI runs the same check in the `assemble` job. The dump is generated with `javap` from the release
AARs: it covers public JVM classes, constructors, methods and fields, but not Kotlin default
values, annotations or source compatibility. Review `git diff api/` together with the public KDoc.

## Release and versioning notes

- The published version lives once in the root `build.gradle.kts` (`allprojects` group and version)
  and is shared by every module. The artifact id is the module name: `library`,
  `calendar-gregorian`, `calendar-hijri`, `calendar-solarHijri`.
- The four library modules apply `maven-publish` and publish their `release` variant with a sources
  jar under `com.github.mortezasun.android-calendarview`. `:calendarviewsample` is not published.
- `:calendar-bom` is a `java-platform` publication whose constraints use the same root version.
  Publish it together with the modules and update `jitpack.yml`/CI if the module set changes.
- The shared POM block in the root build supplies name, description, URL, Apache-2.0 license,
  developer and SCM. Do not duplicate or diverge these fields per module. Maven Central signing
  and upload are separate release operations and are not configured.
- JitPack builds from Git tags and commit hashes; `jitpack.yml` pins the JDK because AGP 9.5 needs
  JDK 17. Verify with `./gradlew publishToMavenLocal` and inspect the generated POMs before tagging.
- The sample declares its own `versionCode`/`versionName` in `calendarviewsample/build.gradle.kts`.
- Do not bump versions or update dependencies as part of an unrelated change.

### New heatmap option

Heatmap options mirror `CalendarMonthGrid`. Add a new option as a defaulted parameter to all
three public heatmaps (`CalendarQuarterHeatmap` and both `CalendarYearHeatmap` overloads),
place it before the trailing `colorForIntensity` / `dayContent` parameters so existing
trailing-lambda calls keep compiling, forward it in the Gregorian year overload and pass it to
the private renderer through `HeatmapRenderConfig`. Cell geometry belongs in `HeatmapStyle`
(with validation in its `init`), per-day state in `HeatmapDayData` with a default value.

Keep the touch-target contract: `HeatmapCell` draws the visible cell from `visualModifier` and the
interactive slot from `slotModifier`, which is sized to at least
`LocalMinimumInteractiveComponentSize`. `WeeksAsColumns` sizes both slot edges; `WeeksAsRows`
sizes only the height and lets `weight` share the row width, because seven columns must fit. Do not
move the click handling onto the small visual box.
