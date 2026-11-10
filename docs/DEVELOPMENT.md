# Development guide

## Where typical changes belong

| Task | Location |
|------|----------|
| Change selection behaviour (single/multiple/range) | `library/.../adapter/MonthSampleAdapter.kt` |
| Add a configuration switch | `library/.../status/CalendarStatus.kt`, then read it where the view is built |
| Add a view mode (list/pager direction) | `library/.../enums/TypeViewCalender.kt`, `MultiMonthView.initRecyclerView` |
| Change month grid geometry | `library/.../MonthView.kt` (`numColumns`, `onMeasure`, `onLayout`) |
| Add a calendar system | New Gradle module with `MonthStatus` + `DayStatus` subclasses, modeled on `:calendar-gregorian` |
| Change day formatting | `library/.../CalendarUtil.kt` and the calendar-specific `DayStatus` subclass |
| Change heatmap rendering | `library/.../compose/CalendarHeatmap.kt` (`HeatmapBlock`, `HeatmapCell`) |
| Change heatmap data handling | `heatmapQuarterData` (Gregorian) / `HeatmapMonthData` built in the caller, see `calendarviewsample/.../CalendarSampleHeatmap.kt` |
| Change sample screen wiring | `calendarviewsample/.../CalendarViewActivity.kt` (reference) or `LauncherActivity.kt` |

Keep the dependency direction intact: `:library` must not import classes from the calendar modules
or from the sample. Calendar-specific code stays in its module.

## Patterns for new features

### New calendar engine

1. Create a module like `:calendar-gregorian` (same `build.gradle.kts` shape, dependency on
   `:library`).
2. Implement `MonthStatus<Month, DayStatus>`:
   - month caption and names,
   - length/start/end of month,
   - `minusMonths`/`plusMonths` (return new instances; do not mutate the receiver),
   - `setOnCreateDayStatus`, `atDay`,
   - first/last day of week and their integer indexing.
3. Implement `DayStatus` with the calendar's day/month/year values and text formatting.
4. Add the module to `settings.gradle.kts` and to the consumer's dependencies.
5. Extend the sample's `TypeArtCalender` only if the sample should demonstrate it.

`minusMonths`/`plusMonths` are called for every month in the window, so they must be pure and
must not depend on the day currently selected.

### New adapter rendering hook

Consumers implement the abstract `onBind*` methods of `MonthSampleAdapter`. If a new hook is
needed, add it to `BaseCalendarAdapter`/`DayCellViewAdapter` with a default only where every
existing consumer can use it; otherwise update all four modules and the sample in the same change.
This is a public API change and must be called out in the summary.

### New `CalendarStatus` option

1. Add a private field with a getter and a fluent setter.
2. Read it while building views in `MultiMonthView.addMonths` or `MonthView.setUp`.
3. Because `addMonths` rebuilds everything, document that callers must rebuild after changing it.
4. Add the option to the sample only if it demonstrates the feature.

### Error handling and logging

The library is deliberately small:

- Invalid grid configuration throws `IllegalArgumentException` (`MonthView`, `numColumns`).
- Methods that index selection lists directly have preconditions (`getPeriodByRange` expects two
  entries); guard at the call site instead of swallowing exceptions.
- The only logging is `Log.i("snappedView", ...)` in `MultiMonthView.getPosition`.
- Do not add error-handling frameworks or coroutines for existing synchronous rendering.

### Comments

- Write KDoc for public types and for non-obvious private helpers.
- Comments explain **why** (constraints, invariants, calendar quirks), not **what** the next line
  does. Example: the manual month arithmetic in `HijriStatus`/`SolarHijriStatus` explains why it
  exists, because `HijrahDate` has no `plusMonths`.
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
4. Run `./gradlew test`. The current tests are template placeholders, so passing them does not
   prove calendar behaviour; state that explicitly in the summary.
5. Run `./gradlew :library:lintDebug` when the change is non-trivial.
6. Inspect `git diff` and `git status`: only intended files, no build output, no unrelated mode
   changes.
7. Update the documents listed in [AGENTS.md](../AGENTS.md#documentation-to-update).

If a change affects rendering, a manual check in the sample app is required; CLI builds do not
verify layout, RTL behaviour or selection visuals.

## Release and versioning notes

- The sample declares `versionCode`/`versionName` in `calendarviewsample/build.gradle.kts`.
- Publishing happens through JitPack from Git tags; the repository does not contain a Maven
  publishing configuration.
- Do not bump versions or update dependencies as part of an unrelated change.

### New heatmap option

Heatmap options mirror `CalendarMonthGrid`. Add a new option as a defaulted parameter to all
three public heatmaps (`CalendarQuarterHeatmap` and both `CalendarYearHeatmap` overloads),
place it before the trailing `colorForIntensity` / `dayContent` parameters so existing
trailing-lambda calls keep compiling, forward it in the Gregorian year overload and pass it to
the private renderer through `HeatmapRenderConfig`. Cell geometry belongs in `HeatmapStyle`
(with validation in its `init`), per-day state in `HeatmapDayData` with a default value.
