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

## Pending execution on a Gradle-capable checkout

1. Check out the reference revision without disturbing any uncommitted work and record `git rev-parse HEAD` and `git status --short`.
2. Run `bash ./gradlew :kmp-sample:tasks --all` and `bash ./gradlew jvmTest testAndroidHostTest wasmJsBrowserTest --dry-run`. Compare the selected tasks with the sample's Android host, named `desktop` JVM and Wasm browser targets. Do not assume a task name or infer execution from the dry run alone.
3. Run the unchanged verification suites and inspect `:kmp-sample` test XML for `SampleStringsParityTest` on each available runner. Record actual tasks, counts and outcomes, including infrastructure failures; add only demonstrably missing sample tasks in the later CI phase.
4. Before refactoring, add characterization tests for defaults, explicit overrides, label ordering, month-to-heatmap offsets and week starts. Extend them later to all seven start days, RTL, custom labels, date boundaries and state preservation in the sample.

Execution status: Gradle task discovery, dry run, test execution and CI report verification were not performed in this source audit. No build or test pass is claimed for the reference revision.
