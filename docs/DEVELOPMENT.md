# Development guide

## Where typical changes belong

| Task | Location |
|------|----------|
| Change selection behaviour (single/multiple/range) | `library/.../selection/CalendarSelection.kt` (`onDayClick`); saved state in `compose/CalendarSelectionState.kt` |
| Add a configuration switch | The public composable in `library/.../compose/CalendarMonthGrid.kt` or `CalendarHeatmap.kt` (defaulted parameter, forwarded to the renderer) |
| Add a view mode (list/pager direction) | `library/.../compose/CalendarMonthContainers.kt` (`CalendarMonthList`, `CalendarMonthPager`, `CalendarScrollAxis`) |
| Change month grid geometry | `library/.../compose/CalendarMonthGrid.kt` and `CalendarMonthCells.kt` (`buildCalendarMonthCells`, `fixedSixRows`) |
| Add a calendar system | New Gradle module with a `*MonthData` builder and a `*CalendarMonth` composable, modeled on `:calendar-gregorian`; for a bounded engine use `:calendar-chinese-native` + `:calendar-chinese-compose` as the KMP/engine-facade example |
| Change day formatting | The engine's `*MonthData.kt` / `*DayCell` code in its own module; the grid only renders `CalendarDayCell` |
| Change the Hijri method | `calendar-hijri/.../HijriDateProvider.kt`; pass the provider through `hijriMonthData`, `hijriWeekData`, `HijriCalendarMonth` and the provider-aware `HijriMonthState`/`rememberHijriMonthState` |
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
   `:library`). If the engine must work without Android/Compose, split it: a pure Kotlin
   Multiplatform module plus a Compose facade, as `:calendar-chinese-native` and
   `:calendar-chinese-compose` do.
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

`CalendarMonthGrid`, the engine month composables, the week/agenda views and the heatmaps
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

### New calendar-independent core helper

Logic that needs no Compose and no calendar engine goes into `:library`'s `selection` package
(`EpochDayMath`, `CalendarWeekNumbering`, `CalendarSelectionPresets`). Keep it pure:

1. Take every platform value as a parameter (`todayEpochDay`, `firstDayOfWeek`) instead of reading
   a clock or a system locale; `:library` `commonMain` must stay free of `java.*` and `expect`
   platform date APIs.
2. Keep Gregorian arithmetic internal to `:library` and validate inputs with `require`, so the
   failure is testable in `commonTest` without composition.
3. Engine modules may call the public helpers (`isoWeekNumber`, `isoWeekYear`) and format digits
   with their own label tables; `:library` must never import a calendar engine.
4. Cover month, year and leap boundaries in `commonTest`. Do not add a preset or a helper that
   duplicates a container's layout logic.

### New locale-aware parameter or label table

The locale work repeats a three-part shape, and a new localized option follows it:

1. Add a defaulted `locale: CalendarLocale? = null` before the trailing lambda and forward it
   through the engine wrapper, the `*MonthData`/`*WeekData` builder and `*MonthState`. `null` must
   keep the platform formatter so existing calls and tests stay unchanged, and a new *option* that
   changes interaction behaviour stays opt-in (`= false`).
2. Put the labels in the engine's `commonMain` tables (`GregorianLabels`, `HijriLabels`,
   `SolarHijriLabels`) and let unknown tags fall back to English; never read `java.util.Locale` in
   `commonMain`. Only the platform language lookup is an `internal expect` function with one actual
   per source set (`jvmMain`/`androidMain`, `appleMain`, `jsMain`, `wasmJsMain`).
3. Update every checked-in API surface with `bash scripts/api-check.sh --update` and
   `bash scripts/klib-api-check.sh --update`, then rerun both without `--update` and review
   `git diff api/`.
4. Cover the table in the engine's `commonTest` (completeness, digits, fallback) and keep the
   library-level contract in `CalendarLocaleTest`; `:library` ships no label table, so label and
   digit assertions belong to the engine that owns them.

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
  `HijriDate` has no `plusMonths`.
- Existing KDoc is English; keep new comments in English for consistency.
- Fix a stale comment only when the code proves it wrong; otherwise leave it and note the
  discrepancy.

## Build, test and review workflow

1. Read [ARCHITECTURE.md](ARCHITECTURE.md) and the relevant source file before editing.
2. Make the smallest change; do not reformat untouched lines.
3. Compile the affected module:
   ```bash
   ./gradlew :library:compileAndroidMain
   ```
   For changes that touch several modules or the sample, build the app:
   ```bash
   ./gradlew :calendarviewsample:assembleDebug
   ```
4. Run `./gradlew jvmTest` (169 KMP JVM tests), `./gradlew testAndroidHostTest` (152 tests on the
   Android host runtime), `./gradlew :calendarviewsample:testDebugUnitTest` (62 sample tests),
   `./gradlew jsNodeTest jsBrowserTest :calendar-chinese-native:wasmJsNodeTest wasmJsBrowserTest`
   (Kotlin/JS and Kotlin/Wasm suites in Node.js and headless Chrome), and
   `./gradlew :calendar-chinese-native:linuxX64Test` (16 Kotlin/Native tests). The Compose UI and
   sample acceptance tests live in `src/androidTest` and need an emulator or device;
   `./gradlew :library:compileAndroidDeviceTest :calendarviewsample:compileDebugAndroidTestKotlin`
   only proves all 46 instrumentation tests compile.
5. Run `./gradlew lint` when the change is non-trivial.
6. Inspect `git diff` and `git status`: only intended files, no build output, no unrelated mode
   changes.
7. Update the documents listed in [AGENTS.md](../AGENTS.md#documentation-to-update).

If a change affects rendering, a manual check in the sample app is required; CLI builds do not
verify layout, RTL behaviour or selection visuals.

## Public API guard

The published AARs have a checked-in public JVM API dump in `api/*.api`. After changing a public
declaration, assemble the release AARs and run:

```bash
./gradlew assembleAndroidMain :calendar-chinese-native:jvmJar
bash scripts/api-check.sh          # fails on an unexpected API diff
bash scripts/api-check.sh --update # regenerates api/*.api after an intentional change
```

CI runs the same check in the `assemble` job. The dump is generated with `javap` from the release
AARs: it covers public JVM classes, constructors, methods and fields, but not Kotlin default
values, annotations or source compatibility. Review `git diff api/` together with the public KDoc.

`gregorianOverlayData` is `internal` for the Gregorian-overlay tests. Kotlin mangles internal
*members* but not internal *top-level functions*, so it appears as a public static method on the
file facade class in JVM bytecode. The guard filters that exact name and rejects it if it appears
again; the KLIB dump does not list it at all.

The five platform adapter objects (`HeatmapPlatformFormatter`, `GregorianPlatformFormatter`,
`HijriPlatform`, `SolarHijriPlatform` and `ChinesePlatform`) are `internal` implementation
details. Kotlin `internal` is still public JVM bytecode, and the repository does not apply the
binary-compatibility-validator plugin because it creates no tasks with AGP 9's built-in Kotlin
support. `scripts/api-check.sh` therefore filters those exact classes and rejects a leak; the KLIB
dump already omits them, and `scripts/klib-api-check.sh` rejects a leak as well.

## Kotlin Multiplatform guards

`commonMain` must stay free of JVM, Android, Apple, Web (`kotlinx.browser`/`kotlin.js`), and
removed `ir.huri` imports. The Apple KLIB surface has separate ABI snapshots under `api/klib/*.abi`:

```bash
bash scripts/check-commonmain-purity.sh
./gradlew iosArm64MainKlibrary iosSimulatorArm64MainKlibrary macosArm64MainKlibrary \
  :calendar-chinese-native:linuxArm64MainKlibrary :calendar-chinese-native:mingwX64MainKlibrary \
  :calendar-chinese-native:iosX64MainKlibrary
bash scripts/klib-api-check.sh          # fails on an unexpected KLIB ABI diff
bash scripts/klib-api-check.sh --update # regenerates api/klib/*.abi after an intentional change
```

CI runs both guards across the `assemble`, `native-engine`, and `klib-api` jobs.

### Build conventions

- `-Xexpect-actual-classes` is applied only to the five modules that declare `internal expect
  object` platform adapters (`:library`, `:calendar-gregorian`, `:calendar-hijri`,
  `:calendar-solarHijri-native`, `:calendar-chinese-compose`). Kotlin 2.5.0-RC reports expect/actual
  classes as a Beta feature on both the expect and the actual declaration. Scoping the opt-in to
  the declaring modules keeps the warning active for any new expect/actual class elsewhere;
  removing it from a declaring module brings the warning back.
- Compose modules keep `binaries.executable()` and `useChromeHeadlessNoSandbox()` for JS/Wasm
  browser tests; the root build disables the device-test Compose resource copy task because
  Compose Multiplatform `1.13.0-beta02` leaves its output unconfigured.

## Documentation and benchmarks

Public KDoc is rendered by Dokka `2.3.0` for all seven published modules:

```bash
./gradlew :dokkaGenerate
# build/dokka/html/index.html
```

Run it after changing public KDoc or adding a published module. Unresolved KDoc links are
warnings, not build failures; keep the output free of them when touching those declarations. The
`API documentation` workflow publishes the HTML to Pages when Pages uses workflow builds and
always uploads the `dokka-html` artifact.

The separate `:benchmarks` module uses kotlinx-benchmark `0.5.0` and is intentionally outside
`check`, `build` and CI. Run `./gradlew :benchmarks:mainBenchmark` for the JVM baseline; see
[BENCHMARKS.md](BENCHMARKS.md).

## Toolchain upgrades

The wrapper is on Gradle `9.9.1`, and the uncommitted working tree prepares Kotlin `2.5.0-RC` and
Compose Multiplatform `1.13.0-beta02` against `platforms;android-37.1` (while tagged `3.0.0` used
Kotlin `2.4.21` and Compose Multiplatform `1.12.2`). After any wrapper or plugin upgrade, rerun the
full gate stack: JVM/Android-host/JS/Wasm/Linux tests, `lint`, sample release, instrumentation
compile checks, `api-check.sh`, `klib-api-check.sh`, `check-commonmain-purity.sh`, and 60-module
`verify-publication.sh`. Gradle 9.9.1 still reports upstream deprecation warnings from the active
KMP/Compose plugins; record any new warning and the plugin that owns it rather than suppressing it.

## Release and versioning notes

- The published version lives once in the root `build.gradle.kts` (`allprojects` group and version)
  and is shared by every module. The artifact id is the module name: `library`,
  `calendar-gregorian`, `calendar-hijri`, `calendar-solarHijri`, `calendar-solarHijri-native`,
  `calendar-chinese-native`, `calendar-chinese-compose`.
- All six Compose modules publish root metadata plus `-android` AAR, `-jvm` JAR, and `-iosarm64`,
  `-iossimulatorarm64`, `-macosarm64`, `-js`, and `-wasm-js` KLIB variants (with sources);
  `:calendar-chinese-native` publishes root metadata plus `-jvm`, `-linuxx64`, `-linuxarm64`,
  `-mingwx64`, `-iosx64`, `-iosarm64`, `-iossimulatorarm64`, `-macosarm64`, `-js`, and `-wasm-js`
  variants (60 directories total with `:calendar-bom`). Android consumers use `:calendar-chinese-native`'s
  JVM variant. All share `com.github.mortezasun.android-calendarview`. `:calendarviewsample` is
  not published.
- `:calendar-bom` is a `java-platform` publication whose constraints use the same root version.
  Publish it together with the modules and update `jitpack.yml`/CI if the module set changes.
- The shared POM block in the root build supplies name, description, URL, Apache-2.0 license,
  developer and SCM. Do not duplicate or diverge these fields per module. Maven Central signing
  and upload are separate release operations and are not configured.
- JitPack builds from Git tags and commit hashes; `jitpack.yml` pins the JDK because AGP 9.5 and
  Kotlin `2.5.0-RC` need JDK 17. Verify with `./gradlew publishToMavenLocal` and
  `bash scripts/verify-publication.sh` before tagging.
- The sample declares its own `versionCode`/`versionName` in `calendarviewsample/build.gradle.kts`.
- Do not bump versions or update dependencies as part of an unrelated change.

### New heatmap option

Heatmap options mirror `CalendarMonthGrid`. Add a new option as a defaulted parameter to all
three public heatmaps (`CalendarQuarterHeatmap` and both `CalendarYearHeatmap` overloads),
place it before the trailing `colorForIntensity` / `dayContent` parameters so existing
trailing-lambda calls keep compiling, forward it in the Gregorian year overload and pass it to
the private renderer through `HeatmapRenderConfig`. Cell geometry belongs in `HeatmapStyle`
(with validation in its `init`), per-day state in `HeatmapDayData` with a default value.

A stepped scale is a value type, not a callback: extend `HeatmapBuckets` (with validation in its
`init`) and forward it through `HeatmapRenderConfig` to `HeatmapCell`, where only the color lookup
is derived from the bucket. Never replace `HeatmapDayData.intensity`, selection or accessibility
text with the bucket bound. A legend reuses the same `HeatmapBuckets`, `HeatmapStyle` and
`colorForIntensity`; keep its labels as slots because `:library` ships no UI strings.

Keep the touch-target contract: `HeatmapCell` draws the visible cell from `visualModifier` and the
interactive slot from `slotModifier`, which is sized to at least
`LocalMinimumInteractiveComponentSize`. `WeeksAsColumns` sizes both slot edges; `WeeksAsRows`
sizes only the height and lets `weight` share the row width, because seven columns must fit. Do not
move the click handling onto the small visual box.

### Bounded calendar engines and adjacent-day guards

When a calendar engine supports a finite year range (such as `SolarHijriEngine`'s `-61..3177` AP
or `ChineseCalendarEngine`'s Chinese years `1901..2099`),
the month state must clamp navigation (`canGoPrevious`/`canGoNext`) to the engine bounds even when
caller bounds are `null`, and `adjacentDayProvider` / week builders must return a blank, disabled
`CalendarDayCell` for out-of-range epoch days instead of throwing.
