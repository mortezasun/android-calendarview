# Architecture

## Overview

`android-calendarview` is a **Kotlin Multiplatform Compose calendar library** targeting Android, JVM, Apple (`iosArm64`, `iosSimulatorArm64`, `macosArm64`), and Web (`js`, `wasmJs`) — plus `linuxX64`, `linuxArm64`, `mingwX64`, and `iosX64` on the pure `:calendar-chinese-native` engine — with an interactive Android demo app. `3.0.0` (at commit `a0c686e`) is the first Kotlin Multiplatform release; `2.4.0` remains the previous Android-only release, and the uncommitted working tree expands the matrix on Kotlin `2.5.0-RC` / Compose Multiplatform `1.13.0-beta02`. The migration is documented in [ADR 0006](decisions/0006-kotlin-multiplatform-calendar-modules.md). Version 2.0.0 eliminated all legacy XML layouts, ViewBinding, custom view measurement, and reflection.

The architecture follows standard Compose state and rendering patterns:
- **Stateless rendering:** `CalendarMonthGrid` and `CalendarDayCell` render month layouts declaratively.
- **Calendar abstraction:** Calendar engines supply month data snapshots (`GregorianMonthData`, `HijriMonthData`, `SolarHijriMonthData`, `SolarHijriNativeMonthData`, `ChineseMonthData`) and day cells (`CalendarDayCell`).
- **Unified selection engine:** `CalendarSelection` and `CalendarSelectionState` operate on universal epoch-day identities (`Long`).
- **Containers:** Compose foundational lists and pagers (`HorizontalPager`, `VerticalPager`, `LazyRow`, `LazyColumn`) provide smooth scrolling and month navigation.
- **Month data:** every engine month snapshot implements `CalendarMonthData`; `toHeatmap()` converts any month into the calendar-independent heatmap block format.
- **Month navigation:** `CalendarMonthNavigationState` and `CalendarMonthHeader` live in `:library`; each engine owns a saveable `*MonthState` with its month arithmetic and inclusive bounds.
- **Heatmaps:** `CalendarQuarterHeatmap` and `CalendarYearHeatmap` render activity intensities as a grid of Compose cells, configurable like the month grid.

---

## Modules

| Module | Responsibility |
|--------|----------------|
| `:library` | Core Compose views (`CalendarMonthGrid`, `CalendarWeekGrid`, `CalendarAgenda`, `CalendarQuarterHeatmap`, `CalendarYearHeatmap`), month containers (`CalendarMonthList`, `CalendarMonthPager`, `CalendarMonthColumns`), selection state machine (`CalendarSelection`, `CalendarSelectionState`, `CalendarRangePolicy`, `TypeSelectDay`), shared month/week data, markers, styling, month navigation, and cell data models (`android`, `jvm`, `iosArm64`, `iosSimulatorArm64`, `macosArm64`, `js`, `wasmJs`). |
| `:calendar-gregorian` | Gregorian calendar engine backed by the immutable `GregorianYearMonth` value type. Provides `GregorianCalendarMonth`, month/week data builders and day cells (`android`, `jvm`, `iosArm64`, `iosSimulatorArm64`, `macosArm64`, `js`, `wasmJs`). |
| `:calendar-hijri` | Islamic Hijri calendar engine backed by the immutable `HijriDate` value type and platform Umm al-Qura chronology (`java.time` on Android/JVM, Foundation `NSCalendar` in `appleMain`, `Intl.DateTimeFormat` in `webMain`). Provides `HijriCalendarMonth`, `HijriDateProvider`, month/week data builders and day cells (`android`, `jvm`, `iosArm64`, `iosSimulatorArm64`, `macosArm64`, `js`, `wasmJs`). |
| `:calendar-solarHijri` | Solar Hijri / Jalali facade over `:calendar-solarHijri-native`. Provides `SolarHijriCalendarMonth`, month/week data builders and day cells (`android`, `jvm`, `iosArm64`, `iosSimulatorArm64`, `macosArm64`, `js`, `wasmJs`); no `ir.huri:jalalicalendar` dependency. |
| `:calendar-solarHijri-native` | Dependency-free Solar Hijri engine (`SolarHijriEngine`, immutable `SolarHijriDate`, `SolarHijriLocale`, `SolarHijriNativeCalendarMonth`) using the Borkowski (1996) break-table algorithm (`-61..3177` AP); `android`, `jvm`, `iosArm64`, `iosSimulatorArm64`, `macosArm64`, `js`, `wasmJs`. |
| `:calendar-chinese-native` | Self-written Kotlin Multiplatform Chinese lunisolar engine (`ChineseCalendarEngine`, immutable `ChineseMonth`/`ChineseDate`, `ChineseFormat`, versioned month table). Targets `jvm`, `linuxX64`, `linuxArm64`, `mingwX64`, `iosX64`, `iosArm64`, `iosSimulatorArm64`, `macosArm64`, `js` and `wasmJs`; no Android target, Compose, Java date or third-party calendar dependency. |
| `:calendar-chinese-compose` | Compose facade (`ChineseCalendarMonth`, `GregorianChineseCalendarMonth`, `ChineseMonthData`, `ChineseWeekData`, `ChineseMonthState`, `chineseYearHeatmap`) depending on `:calendar-chinese-native`, `:calendar-gregorian` and `:library` (`android`, `jvm`, `iosArm64`, `iosSimulatorArm64`, `macosArm64`, `js`, `wasmJs`). |
| `:calendar-bom` | `java-platform` constraints for the seven published library artifacts; no runtime classes. |
| `:benchmarks` | Standalone Kotlin/JVM kotlinx-benchmark module for Gregorian/Hijri month calculations and the shared heatmap adapters. Not published and not part of `check`/`build`. |
| `:calendarviewsample` | Interactive Material 3 Compose Android demo app (`Home`, `Calendars`, `Playground`, `Heatmaps`) illustrating dynamic secondary calendars, configuration, selection modes, navigation, dialogs, and heatmaps. |

### Dependency Rules

- The dependency graph is strictly one-way: `calendar-* -> library` (except the standalone engine), `calendar-chinese-compose -> calendar-chinese-native + library`, `calendarviewsample -> all`.
- `:library` does not depend on any specific calendar engine or third-party calendar library.
- `:calendar-chinese-native` depends only on the Kotlin standard library; no existing module depends on either Chinese module.
- `calendar-solarHijri` reuses the dependency-free `:calendar-solarHijri-native` engine through `api`, so it declares no third-party calendar dependency.
- `:calendar-bom` constrains published versions but is not a dependency of the modules.
- `:benchmarks` depends on `:library`, `:calendar-gregorian` and `:calendar-hijri`; it is not published and no library module depends on it.
- Root POM metadata is shared by all `maven-publish` publications; module publication blocks only choose the component and artifact.

---

## Core Rendering Pipeline

1. **Month Data Snapshot:**
   The caller supplies a month descriptor (e.g. `GregorianYearMonth`, `HijriDate`, `SolarHijriDate` or `ChineseMonth`). The calendar engine computes the `firstDayOffset` (relative to the configured `firstDayOfWeek`) and formats localized `CalendarDayCell` items.
2. **Cell Grid Layout:**
   `buildCalendarMonthCells` creates complete 7-column rows. When `fixedSixRows = true`, the grid always produces 42 cells. Padding cells for previous and next months are either left blank (`null`) or populated with real adjacent day identities via `adjacentDayProvider`.
3. **Compose Month Grid:**
   `CalendarMonthGrid` lays out the month title (optional), weekday header row (optional), and the 7-column cell grid using Compose `Column`, `Row`, and `Surface`. It also accepts a `CalendarMonthData` snapshot directly alongside optional `secondaryLabel` and `secondaryAccessibilityLabel` callbacks for cross-calendar day annotations.
4. **Day Interaction & Focus:**
   Day cells display their localized labels. Current days have a primary-colored border focus indicator (`day.isToday`). Clicks pass the cell's `epochDay` to `onDayClick`. Disabled dates reject interactions.
5. **RTL Support:**
   Compose layout automatically reflects the ambient `LocalLayoutDirection`. In RTL layouts (e.g., Persian or Arabic), columns are positioned from right to left without custom arithmetic.
6. **Keyboard Focus (opt-in):**
   `keyboardNavigation = true` adds a focus position and key handling to the day cells: arrow keys move focus by one day or one week (horizontal direction follows the ambient layout direction) and report the new day through `onDayClick`, Page Up/Down delegate month changes to the caller's `onMonthStep` because the grid does not know the month identity, and Enter/Space report the focused day through `onDayClick`. The grid still owns no selection and no month, and the focus border disappears when focus leaves the grid.

---

## Locale and label data flow

`CalendarLocale` is a value type in `:library` that only normalizes the language subtag and exposes
`supportedLanguages`, `isRightToLeft` and `systemDefault()`; it holds no labels.

- **`null` means the platform.** Every `locale` parameter defaults to `null`, which keeps the
  platform formatter (`java.time` on JVM/Android, `NSDateFormatter` on Apple, `Intl` on the web)
  and therefore the pre-locale behaviour, including the `3.0.0` binary-compatibility overloads.
- **A non-null locale means the engine tables.** Each engine owns pure Kotlin tables
  (`GregorianLabels`, `HijriLabels`, `SolarHijriLabels`, the `SolarHijriLocale` month lists) and
  formats month titles, weekday labels, day numbers and accessibility labels from them. Unknown
  tags fall back to English and Western digits; there is no fallback chain beyond the language
  subtag, so `de-AT` behaves like `de`.
- **Chinese stays a style.** `:calendar-chinese-compose` keeps `ChineseTextStyle` because its
  labels are script choices, not translations. `GregorianChineseCalendarMonth` localizes only its
  Gregorian primary layer and keeps the Chinese secondary labels on the style.
- **Direction is not a locale side effect.** Calendar composables never change `LayoutDirection`.
  `isRightToLeft` and `ProvideCalendarLocaleLayoutDirection` are an opt-in helper for hosts that
  want the language to decide; `null` leaves the ambient direction untouched.
- **Platform glue stays in platform source sets.** `platformLanguageTag()` is an `internal expect`
  function with `jvmMain`/`androidMain`, `appleMain`, `jsMain` and `wasmJsMain` actuals, so
  `commonMain` never imports `java.util.Locale`; `asCalendarLocale()` is a JVM/Android extension in
  those two source sets.

---

## State and Selection Engine

State is managed declaratively and cleanly separated from UI rendering:

- **Epoch-Day Identities:** Dates across the four calendar systems (five engine entries, because Solar Hijri has facade and native engines) are mapped to standard epoch-day integers (`Long`), enabling seamless cross-calendar selections and comparisons.
- **Selection Modes (`TypeSelectDay`):**
  - `None`: Read-only calendar.
  - `Single`: Exactly zero or one selected day.
  - `Multiple`: Arbitrary set of selected days.
  - `Range`: An ordered contiguous range bounded by start and end epoch days. Intermediate days are computed immutably.
- **State Preservation:**
  `rememberCalendarSelectionState` uses `rememberSaveable` with `CalendarSelectionSnapshot` saver, ensuring selections survive activity recreation and configuration changes (e.g., screen rotation, dark/light theme toggle).
- **Picker and legend in the samples:** `:kmp-sample` wires `CalendarMonthPicker` to its Gregorian month state (jump-to-month, year stepping) and renders `HeatmapLegend` from the same `HeatmapBuckets` its heatmap uses, so the shipped demos exercise both APIs.
- **Derived presets & week alignment:** `CalendarSelection.presets(firstDayOfWeek, todayEpochDay)` returns today, the aligned seven-day week, the civil Gregorian month and the last seven days as epoch-day ranges. Shared week-start math (`requireValidFirstDayOfWeek`, `firstDayOffset`, `weekStartEpochDay`, `orderedIsoWeekdays`, `weekdayLabelsStartingAt`) lives in `:library` ([ADR 0009](decisions/0009-first-day-of-week-alignment-api.md)) and is delegated to by all Compose calendar modules without altering engine defaults or `:calendar-chinese-native` independence.

---

## Month Data and Navigation

- **Shared month contract:** every engine month snapshot (`GregorianMonthData`, `HijriMonthData`, `SolarHijriMonthData`, `SolarHijriNativeMonthData`, `ChineseMonthData`) implements `CalendarMonthData`. The contract carries the localized title, seven weekday labels, the leading offset and the current-month `CalendarDayCell` list. `toHeatmap()` builds a `HeatmapMonthData` block from it, including full-week padding, `isToday`/`enabled` forwarding and intensity normalization, so the README no longer needs a hand-written adapter.
- **Hijri method:** `HijriDateProvider` maps an absolute epoch day to the displayed `HijriDate` and back. `UmmAlQuraHijriDateProvider` is the default; `OffsetHijriDateProvider` shifts a local method by whole days. Month/week builders derive the cell epoch from the provider's reverse mapping, so labels and selection identity stay consistent. The provider-aware `HijriMonthState` constructor and `rememberHijriMonthState` overload use the same mapping for today; the three-argument API keeps the Umm al-Qura default.
- **View model states:** `GregorianMonthState`, `HijriMonthState`, `SolarHijriMonthState`, `SolarHijriNativeMonthState` and `ChineseMonthState` are observable, saveable state holders that keep the visible month, normalize it to the first day, enforce inclusive `minMonth`/`maxMonth` bounds and expose `previous()`, `next()`, `goToToday()` and an engine-specific `goTo(...)`. They live in the calendar modules because month arithmetic is calendar-specific.
- **Header:** `CalendarMonthHeader` is calendar-independent and stateless. It renders the title as a heading, exposes optional today/previous/next actions, and takes localized content descriptions from the caller because the library ships no UI strings.
- **Picker:** `CalendarMonthPicker` is the one month-grid container the library owns. It renders twelve `selectable` cells for a caller-supplied year, exposes grid semantics, keeps no internal state and takes all labels as slots; the host keeps the dialog frame and the year state ([ADR 0008](decisions/0008-calendar-month-picker.md)).
- **Semantics:** month titles are headings; the week container exposes one `CollectionInfo` and each day or optional week-number cell exposes `CollectionItemInfo`, so screen readers can announce one grid position. `todayDescription` is an optional caller-localized suffix for today's accessibility label; the default keeps the date-only label.

---

## Distribution

- `:calendar-bom` is a `java-platform` publication. Its POM has `dependencyManagement` constraints for `library`, `calendar-gregorian`, `calendar-hijri`, `calendar-solarHijri`, `calendar-solarHijri-native`, `calendar-chinese-native` and `calendar-chinese-compose` at the root version.
- Root `build.gradle.kts` owns group, version and the shared POM block (name, description, URL, Apache-2.0 license, developer, SCM). Module POMs inherit it.
- JitPack and the publishing CI publish the BOM together with all six Compose KMP modules (root metadata plus `-android` AAR, `-jvm` JAR, and `-iosarm64`, `-iossimulatorarm64`, `-macosarm64`, `-js`, `-wasm-js` KLIBs) and the standalone `:calendar-chinese-native` engine (root metadata plus `-jvm` JAR and `-linuxx64`, `-linuxarm64`, `-mingwx64`, `-iosx64`, `-iosarm64`, `-iossimulatorarm64`, `-macosarm64`, `-js`, `-wasm-js` KLIBs) — 60 publication directories in total. Apple, Windows, and `linuxArm64` binaries are cross-compiled and metadata/archive-verified on Linux, not executed on native hardware. Maven Central signing, namespace verification and upload are not configured; the metadata is a prerequisite only.
- Release `2.1.0` published the four-module BOM on JitPack and a JitPack-only consumer resolved it on 2026-11-11. Release `3.0.0` (`a0c686e`) built on JitPack with all seven modules and the BOM, and JVM plus minified Android consumers resolved it from JitPack on 2026-11-16. See [PROJECT_STATUS.md](PROJECT_STATUS.md).

## Product views, markers and styling

- **Week view:** `CalendarWeekData` is the seven-day contract; each engine has a `*WeekData` builder that aligns to the configured first day and returns consecutive cells. `CalendarWeekGrid` accepts explicit fields or a `CalendarWeekData` snapshot directly and reuses the month grid's styling, markers, week numbers, `secondaryLabel` / `secondaryAccessibilityLabel`, and accessibility rules.
- **Agenda:** `CalendarAgenda` is a bounded `LazyColumn` over `CalendarDayCell` values. It keeps selection and markers caller-owned, supports optional `secondaryLabel` / `secondaryAccessibilityLabel` subtitles, and appends marker/today/secondary descriptions to the accessible day label.
- **Markers:** `CalendarDayMarker` is keyed by epoch day in a caller-owned map. The renderer draws at most four decorative dots; content descriptions are merged into the day label, so markers add information without creating noisy focus targets.
- **Week numbers:** an optional callback on `CalendarMonthGrid` receives the first epoch day of each week. The library ships ISO-8601 arithmetic (`isoWeekNumber`, `isoWeekYear`) but selects no convention by default: each `*WeekData` builder exposes `useIsoWeekNumber = false`, and the Gregorian builder keeps its week-based-year number. Digits come from the engine's label table.
- **Adaptive months:** `CalendarMonthColumns` derives one or more columns from the available width and inter-column spacing, so each month keeps its minimum width, and exposes `LazyGridState`. The optional `maxColumns` caps that derived count and wraps the remaining months into further rows (the default `Int.MAX_VALUE` keeps the unbounded layout), with the same rule available as the pure `calendarMonthColumns` helper. It is a layout container, not a new calendar engine. Width rules, active-month definition and state contract: [ADAPTIVE_LAYOUT.md](ADAPTIVE_LAYOUT.md).
- **Styling:** `LocalCalendarStyle` carries optional selected/surface/content/today colors and marker geometry. Null colors fall back to MaterialTheme; explicit `todayBorderColor` arguments still take precedence.
- **Range policy:** `CalendarRangePolicy` validates complete ranges by inclusive length. `CalendarSelectionState` applies it to taps, external ranges, construction and saved-state restoration; it exposes `selectRange` for a caller-owned drag layer and does not install a gesture or enumerate range interiors.
- **Hijri provider:** `HijriDateProvider`, `UmmAlQuraHijriDateProvider` and `OffsetHijriDateProvider` live in `:calendar-hijri` (not `:library`), because they map the platform Hijri chronology to the shared epoch-day identity. The view, month data and week data take the provider as a defaulted parameter; no provider state is hidden in a composition local.

---

## Heatmap Architecture

The heatmap is decoupled from month grid calculations but shares its configuration model:
- `CalendarQuarterHeatmap` (Gregorian, `Map<Long, Float>` keyed by epoch day) renders one quarter;
  the Gregorian `CalendarYearHeatmap(year, ...)` stacks four quarters.
- The calendar-agnostic `CalendarYearHeatmap(months: List<HeatmapMonthData>, ...)` renders
  month blocks built by the caller with any engine (`gregorianMonthData`, `hijriMonthData`,
  `solarHijriMonthData`, `chineseYearHeatmap`) through `CalendarMonthData.toHeatmap()`, so `:library` stays independent
  of calendar engines.
  `HeatmapDayData.isToday` / `enabled` carry the engine's `CalendarDayCell` state.
- All three composables share one private renderer (`HeatmapBlock` / `HeatmapCell`) and the
  same options as `CalendarMonthGrid`: `showHeader`, `showWeekdayLabels`, `header` and
  `weekdayContent` slots, today border (`todayBorderColor`), disabled dates (dimmed, not
  clickable) and an optional `CalendarSelection` that is rendered but owned by the caller.
- Heatmap-specific layout: `HeatmapOrientation.WeeksAsColumns` (activity chart, horizontal
  scroll, default) or `WeeksAsRows` (month-grid layout, cells share the width), and
  `HeatmapStyle` for cell size, spacing, shape, label width, border widths and disabled alpha.
  `HeatmapStyle()` reproduces the original 48dp rectangular cells without gaps. `cellSize`
  controls only the drawn cell: the layout slot is at least the platform minimum interactive size
  (48dp) in height, and on both edges in `WeeksAsColumns`, so smaller cells keep a full-size touch
  target. `WeeksAsRows` has to fit seven columns and shares the row width instead, so below roughly
  `7 * 48dp + spacing` of viewport width its columns drop under 48dp.
- `HeatmapBuckets` is an immutable, validated value type (finite, ascending thresholds inside
  `0..1`). It maps a normalized intensity to a step and hands the step's lower bound to
  `colorForIntensity`, so a stepped palette needs no branching in the caller. The stored
  `HeatmapDayData` intensity, selection and accessibility descriptions keep the exact value.
- `HeatmapLegend` renders one swatch per step with the same `HeatmapBuckets`,
  `colorForIntensity` and `HeatmapStyle` as the grid, so cells and legend cannot diverge. Labels
  and content descriptions are slots, and the legend adds no touch targets.
- Colors: `colorForIntensity` maps the normalized intensity (0..1) to the cell fill; selected
  cells get a `selectedBorderColor` border, which takes precedence over the today border.
- Layout follows the ambient `LayoutDirection`, so RTL works without extra parameters.
