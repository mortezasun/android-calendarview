# First day of week: Phase 0 baseline

Scope: `kmp`, reference revision `3e043d1c0a8b6440fdc841fb8d92cd11ce9eba66`. This is a source and CI configuration audit, not a new test run. No runtime, API, Gradle, workflow, or release changes are included.

## Entry points and compatibility

| Area | Current behavior to preserve |
| --- | --- |
| Gregorian | `gregorianWeekData` uses `GregorianPlatformFormatter.defaultFirstDayOfWeek()` unless an ISO weekday integer is supplied. Its label order and week start use that same value. Check `gregorianMonthData`, `GregorianCalendarMonth`, and the Gregorian heatmap builders together when replacing the arithmetic. |
| Hijri | `hijriWeekData` defaults to Saturday (ISO 6). It aligns on the epoch-day timeline before converting each day through `HijriDateProvider`. Check `hijriMonthData` and `HijriCalendarMonth` with the default and offset providers. |
| Solar Hijri and Solar Hijri Native | Both week builders default to Saturday (ISO 6); the facade forwards the override and week number to and from the Native implementation. Check both month builders and composables so the facade and Native paths stay aligned. |
| Chinese Compose and Gregorian overlay | `chineseMonthData`, `chineseWeekData`, and both Chinese heatmap builders accept a start day and default through `ChinesePlatform`. Heatmap helpers forward it to month data. `GregorianChineseCalendarMonth` aligns the primary Gregorian grid while `ChineseTextStyle` controls the secondary text. Preserve leap-month identity and range-boundary behavior. Keep the pure Chinese engine independent of Compose and `:library`. |
| Shared views | `CalendarMonthGrid` consumes ordered labels and a leading offset. `CalendarMonthData.toHeatmap()` uses both without choosing a new start day. Audit `CalendarWeekGrid`, `CalendarAgenda`, `CalendarMonthPicker` and adaptive containers for any independent grouping before adding parameters. `CalendarSelection.presets(firstDayOfWeek, todayEpochDay)` already uses the start day for `thisWeek`; include it in the shared arithmetic audit. |
| KMP sample | `SampleCalendarData.kt` feeds snapshot-based month, week and heatmap paths. `SamplePlaygroundViewport.kt` invokes the engine composables directly for single month, lists, pagers and adaptive columns. Both paths need forwarding; the sample currently does not set a shared start day. |

Existing defaults must not change when no override is supplied. Treat label language, custom weekday text, RTL placement and week numbering as separate concerns. Existing APIs use Monday = 1 through Sunday = 7. Compare the old default and explicit overrides before replacing module-local arithmetic. Test bounds and compare by epoch day, not formatted text or grid position.

## Test inventory and CI

- Preserve the existing engine week-numbering tests, `CalendarWeekNumberingTest`, `CalendarSelectionPresetsTest`, the locale and keyboard UI tests, and `SampleStringsParityTest` under `kmp-sample/src/commonTest`. See [project status](PROJECT_STATUS.md) for earlier local results; they are not results for this audit revision. It also records a locale-sensitive Chinese heatmap assertion in the Android demo; if reproduced, record it separately from any new regression.
- `android-verification.yml` requests JVM, Android host, JS and Wasm suites, Android demo unit tests, lint, native engine verification and JVM/KLIB API guards. A root task name does not by itself prove that the corresponding KMP sample tests ran.
- `android-instrumentation.yml` and `android-tablet.yml` run `:library` and `:calendarviewsample` instrumentation on API 29. They do not directly exercise the separate `:kmp-sample` UI. An Apple KLIB compile and ABI check is not an Apple runtime test.
- `android-publishing.yml` verifies artifacts in an isolated Maven Local repository and runs consumer checks. Keep it unchanged; this is not external publishing. Do not invoke the release workflow or use its release commit marker.

## Phase 1 execution results and CI verification

1. Reference revision: `414d42b` on branch `kmp`.
2. Gradle task discovery (`bash ./gradlew :kmp-sample:tasks --all`):
   - Targets in `:kmp-sample`:
     - `android` (Host tests run via `testAndroidHostTest`)
     - `wasmJs` (Browser tests run via `wasmJsBrowserTest`)
     - `jvm("desktop")` (Desktop JVM tests run via `:kmp-sample:desktopTest`)
   - The root lifecycle task `jvmTest` does not trigger `:kmp-sample:desktopTest` because the target name is `desktop`. Therefore, `:kmp-sample:desktopTest` was added explicitly to `unit-tests` in `.github/workflows/android-verification.yml`.
3. Verified test counts and XML reports:
   - `testAndroidHostTest`:
     - `com.develotter.calendarview.kmpsample.SampleFirstDayOfWeekTest`: 6 tests, 0 failures, 0 errors, 0 skipped
     - `com.develotter.calendarview.kmpsample.SampleStringsParityTest`: 2 tests, 0 failures, 0 errors, 0 skipped
     - Confirmed via CI artifact `android-unit-tests-45172289588-1` (Run 45172289588) and local verification.
   - `wasmJsBrowserTest`:
     - `wasmJsBrowserTest.com.develotter.calendarview.kmpsample.SampleFirstDayOfWeekTest`: 6 tests, 0 failures, 0 errors, 0 skipped
     - `wasmJsBrowserTest.com.develotter.calendarview.kmpsample.SampleStringsParityTest`: 2 tests, 0 failures, 0 errors, 0 skipped
     - Confirmed via CI artifact `android-unit-tests-45172289588-1` (Run 45172289588) and local verification.
   - `desktopTest`:
     - `com.develotter.calendarview.kmpsample.SampleFirstDayOfWeekTest`: 6 tests, 0 failures, 0 errors, 0 skipped
     - `com.develotter.calendarview.kmpsample.SampleStringsParityTest`: 2 tests, 0 failures, 0 errors, 0 skipped
     - Verified locally via `bash ./gradlew :kmp-sample:desktopTest` (8 tests total in 22s).
4. CI status:
   - Instrumentation Run 45172289734 rerun as Job 138380226973 passed (54 tests, 0 failures, 6m 19s).
   - Tablet verification Run 45172289818 passed (54 tests, 0 failures, 9m 17s).
   - Android verification Run 45172289588 passed (10m 03s).
   - All 5 workflows on `kmp` at `414d42b` are green.
   - Commit `e854e1b` confirmed all 5 workflows green, including `:kmp-sample:desktopTest` in CI.

## Phase 2: Audit of views and week arithmetic

| File / Component | Current behavior regarding week start / offsets | Action required / Impact |
| --- | --- | --- |
| `library/src/commonMain/kotlin/com/develotter/calendarview/compose/CalendarWeekGrid.kt` | Renders 7 consecutive `CalendarDayCell` items with `firstDayOffset = 0` via `CalendarMonthGrid`. Requires `days.size == 7`. Does not compute week bounds or offsets itself. | None. Delegates layout to `CalendarMonthGrid` with zero offset; caller/engine provides the pre-computed week cells. |
| `library/src/commonMain/kotlin/com/develotter/calendarview/compose/CalendarAgenda.kt` | Vertical `LazyColumn` of `CalendarDayCell` items. No columns, weekday labels, or week offsets. | None. Pure day list without weekday grid alignment. |
| `library/src/commonMain/kotlin/com/develotter/calendarview/compose/CalendarMonthPicker.kt` | 4x3 month selector grid (months 1..12) for a given year. No day cells, weekday labels, or week offsets. | None. Pure month-level selector. |
| `library/src/commonMain/kotlin/com/develotter/calendarview/compose/CalendarMonthGrid.kt` / `CalendarMonthCells.kt` | Stateless month grid consuming caller-supplied `weekdayLabels: List<String>` (size 7) and `firstDayOffset: Int` (0..6). Does not calculate offsets or reorder labels independently. | None in rendering. Offsets and label ordering remain supplied by the calendar engine month snapshots (`CalendarMonthData`). |
| `library/src/commonMain/kotlin/com/develotter/calendarview/compose/CalendarMonthColumns.kt` | Adaptive grid container (`calendarMonthColumns`) deriving column count from available width and `minMonthWidth`. Layout operates on whole month items. | None. Pure container layout on month level. |
| `library/src/commonMain/kotlin/com/develotter/calendarview/selection/CalendarSelectionPresets.kt` | Derives `thisWeek` via private helper `weekStartFor(todayEpochDay, firstDayOfWeek) = epochDay - (isoWeekday(epochDay) - firstDayOfWeek).mod(7)` with `require(firstDayOfWeek in 1..7)`. | Needs shared API. Will replace private `weekStartFor` with the centralized `:library` week alignment helper in Phase 3. |
| `library/src/commonMain/kotlin/com/develotter/calendarview/selection/CalendarWeekNumbering.kt` | Computes ISO-8601 week numbers and week years (`isoWeekNumber`, `isoWeekYear`). By ISO-8601 specification, week 1 contains the first Thursday and weeks strictly start on Monday. | None. ISO-8601 standard strictly defines Monday start; independent of user-configurable visual week starts. |
| `library/src/commonMain/kotlin/com/develotter/calendarview/selection/EpochDayMath.kt` | Computes `isoWeekday(epochDay): Int` (1..7, Monday=1..Sunday=7) overflow-safely. | Serves as the mathematical foundation in `:library` for the shared public week start and offset helpers in Phase 3. |
| Engine week & month data (`calendar-gregorian`, `calendar-hijri`, `calendar-solarHijri`, `calendar-solarHijri-native`, `calendar-chinese-compose`) | Each module currently duplicates local copies of `dayOfWeekOffset(epochDay, firstDayOfWeek)` and `(firstDayOfWeek - 1 + offset).mod(7) + 1` label rotation. | Will be unified in Phase 3 to delegate to the shared public `:library` helpers once published in `:library`. |

