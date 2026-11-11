# Architecture (v2.0.0)

## Overview

`android-calendarview` is a **native Jetpack Compose calendar library** with decoupled calendar engines and an interactive demo app. Version 2.0.0 eliminates all legacy XML layouts, ViewBinding, custom view measurement, and reflection.

The architecture follows standard Compose state and rendering patterns:
- **Stateless rendering:** `CalendarMonthGrid` and `CalendarDayCell` render month layouts declaratively.
- **Calendar abstraction:** Calendar engines supply month data snapshots (`GregorianMonthData`, `HijriMonthData`, `SolarHijriMonthData`) and day cells (`CalendarDayCell`).
- **Unified selection engine:** `CalendarSelection` and `CalendarSelectionState` operate on universal epoch-day identities (`Long`).
- **Containers:** Compose foundational lists and pagers (`HorizontalPager`, `VerticalPager`, `LazyRow`, `LazyColumn`) provide smooth scrolling and month navigation.
- **Month data:** every engine month snapshot implements `CalendarMonthData`; `toHeatmap()` converts any month into the calendar-independent heatmap block format.
- **Month navigation:** `CalendarMonthNavigationState` and `CalendarMonthHeader` live in `:library`; each engine owns a saveable `*MonthState` with its month arithmetic and inclusive bounds.
- **Heatmaps:** `CalendarQuarterHeatmap` and `CalendarYearHeatmap` render activity intensities as a grid of Compose cells, configurable like the month grid.

---

## Modules

| Module | Responsibility |
|--------|----------------|
| `:library` | Core Compose views (`CalendarMonthGrid`, `CalendarWeekGrid`, `CalendarAgenda`, `CalendarQuarterHeatmap`, `CalendarYearHeatmap`), month containers (`CalendarMonthList`, `CalendarMonthPager`, `CalendarMonthColumns`), selection state machine (`CalendarSelection`, `CalendarSelectionState`, `CalendarRangePolicy`, `TypeSelectDay`), shared month/week data, markers, styling, month navigation, and cell data models. |
| `:calendar-gregorian` | Gregorian calendar engine backed by `java.time.YearMonth` and `java.time.LocalDate`. Provides `GregorianCalendarMonth`, month/week data builders and day cells. |
| `:calendar-hijri` | Islamic Hijri calendar engine backed by `java.time.chrono.HijrahDate`. Provides `HijriCalendarMonth`, month/week data builders and day cells. |
| `:calendar-solarHijri` | Solar Hijri / Jalali calendar engine backed by `ir.huri:jalalicalendar`. Provides `SolarHijriCalendarMonth`, month/week data builders and day cells. |
| `:calendar-bom` | `java-platform` constraints for the four published library artifacts; no runtime classes. |
| `:calendarviewsample` | Interactive Compose demo app illustrating configuration, selection modes, navigation, dialogs, and heatmaps. |

### Dependency Rules

- The dependency graph is strictly one-way: `calendar-* -> library`, `calendarviewsample -> all`.
- `:library` does not depend on any specific calendar engine or third-party calendar library (such as `jcal`).
- `calendar-solarHijri` is the only module that declares and encapsulates the `ir.huri:jalalicalendar` dependency.
- `:calendar-bom` constrains published versions but is not a dependency of the modules.
- Root POM metadata is shared by all `maven-publish` publications; module publication blocks only choose the component and artifact.

---

## Core Rendering Pipeline

1. **Month Data Snapshot:**
   The caller supplies a month descriptor (e.g. `YearMonth`, `HijrahDate`, or `JalaliCalendar`). The calendar engine computes the `firstDayOffset` (relative to the configured `firstDayOfWeek`) and formats localized `CalendarDayCell` items.
2. **Cell Grid Layout:**
   `buildCalendarMonthCells` creates complete 7-column rows. When `fixedSixRows = true`, the grid always produces 42 cells. Padding cells for previous and next months are either left blank (`null`) or populated with real adjacent day identities via `adjacentDayProvider`.
3. **Compose Month Grid:**
   `CalendarMonthGrid` lays out the month title (optional), weekday header row (optional), and the 7-column cell grid using Compose `Column`, `Row`, and `Surface`.
4. **Day Interaction & Focus:**
   Day cells display their localized labels. Current days have a primary-colored border focus indicator (`day.isToday`). Clicks pass the cell's `epochDay` to `onDayClick`. Disabled dates reject interactions.
5. **RTL Support:**
   Compose layout automatically reflects the ambient `LocalLayoutDirection`. In RTL layouts (e.g., Persian or Arabic), columns are positioned from right to left without custom arithmetic.

---

## State and Selection Engine

State is managed declaratively and cleanly separated from UI rendering:

- **Epoch-Day Identities:** Dates across all three calendar systems are mapped to standard epoch-day integers (`Long`), enabling seamless cross-calendar selections and comparisons.
- **Selection Modes (`TypeSelectDay`):**
  - `None`: Read-only calendar.
  - `Single`: Exactly zero or one selected day.
  - `Multiple`: Arbitrary set of selected days.
  - `Range`: An ordered contiguous range bounded by start and end epoch days. Intermediate days are computed immutably.
- **State Preservation:**
  `rememberCalendarSelectionState` uses `rememberSaveable` with `CalendarSelectionSnapshot` saver, ensuring selections survive activity recreation and configuration changes (e.g., screen rotation, dark/light theme toggle).

---

## Month Data and Navigation

- **Shared month contract:** every engine month snapshot (`GregorianMonthData`, `HijriMonthData`, `SolarHijriMonthData`) implements `CalendarMonthData`. The contract carries the localized title, seven weekday labels, the leading offset and the current-month `CalendarDayCell` list. `toHeatmap()` builds a `HeatmapMonthData` block from it, including full-week padding, `isToday`/`enabled` forwarding and intensity normalization, so the README no longer needs a hand-written adapter.
- **Hijri method:** `HijriDateProvider` maps an absolute epoch day to the displayed `HijrahDate` and back. `UmmAlQuraHijriDateProvider` is the default; `OffsetHijriDateProvider` shifts a local method by whole days. Month/week builders derive the cell epoch from the provider's reverse mapping, so labels and selection identity stay consistent. The month state's `goToToday()` still uses the platform `HijrahDate.now()`; callers using an offset supply the provider-adjusted today to the view and state initializer.
- **View model states:** `GregorianMonthState`, `HijriMonthState` and `SolarHijriMonthState` are observable, saveable state holders that keep the visible month, normalize it to the first day, enforce inclusive `minMonth`/`maxMonth` bounds and expose `previous()`, `next()`, `goToToday()` and an engine-specific `goTo(...)`. They live in the calendar modules because month arithmetic is calendar-specific.
- **Header:** `CalendarMonthHeader` is calendar-independent and stateless. It renders the title as a heading, exposes optional today/previous/next actions, and takes localized content descriptions from the caller because the library ships no UI strings.
- **Semantics:** month titles are headings; each week exposes `CollectionInfo` and each day `CollectionItemInfo`, so screen readers can announce the grid position. `todayDescription` is an optional caller-localized suffix for today's accessibility label; the default keeps the date-only label.

---

## Distribution

- `:calendar-bom` is a `java-platform` publication. Its POM has `dependencyManagement` constraints for `library`, `calendar-gregorian`, `calendar-hijri` and `calendar-solarHijri` at the root version.
- Root `build.gradle.kts` owns group, version and the shared POM block (name, description, URL, Apache-2.0 license, developer, SCM). Module POMs inherit it.
- JitPack and the publishing CI publish the BOM together with the four AAR modules. Maven Central signing, namespace verification and upload are not configured; the metadata is a prerequisite only.

## Product views, markers and styling

- **Week view:** `CalendarWeekData` is the seven-day contract; each engine has a `*WeekData` builder that aligns to the configured first day and returns consecutive cells. `CalendarWeekGrid` reuses the month grid renderer, so selection, markers, week numbers and accessibility stay identical.
- **Agenda:** `CalendarAgenda` is a bounded `LazyColumn` over `CalendarDayCell` values. It keeps selection and markers caller-owned and appends marker/today descriptions to the accessible day label.
- **Markers:** `CalendarDayMarker` is keyed by epoch day in a caller-owned map. The renderer draws at most four decorative dots; content descriptions are merged into the day label, so markers add information without creating noisy focus targets.
- **Week numbers:** an optional callback on `CalendarMonthGrid` receives the first epoch day of each week. The library deliberately does not choose a week-numbering convention; engines and hosts localize it.
- **Adaptive months:** `CalendarMonthColumns` derives one or more columns from the available width and exposes `LazyGridState`. It is a layout container, not a new calendar engine.
- **Styling:** `LocalCalendarStyle` carries optional selected/surface/content/today colors and marker geometry. Null colors fall back to MaterialTheme; explicit `todayBorderColor` arguments still take precedence.
- **Range policy:** `CalendarRangePolicy` validates complete ranges by inclusive length. `CalendarSelectionState` applies it to taps and exposes `selectRange` for a caller-owned drag layer; it does not install a gesture or enumerate range interiors.
- **Hijri provider:** `HijriDateProvider`, `UmmAlQuraHijriDateProvider` and `OffsetHijriDateProvider` live in `:calendar-hijri` (not `:library`), because they depend on `HijrahDate`. The view, month data and week data take the provider as a defaulted parameter; no provider state is hidden in a composition local.

---

## Heatmap Architecture

The heatmap is decoupled from month grid calculations but shares its configuration model:
- `CalendarQuarterHeatmap` (Gregorian, `Map<LocalDate, Float>`) renders one quarter;
  the Gregorian `CalendarYearHeatmap(year, ...)` stacks four quarters.
- The calendar-agnostic `CalendarYearHeatmap(months: List<HeatmapMonthData>, ...)` renders
  month blocks built by the caller with any engine (`gregorianMonthData`, `hijriMonthData`,
  `solarHijriMonthData`) through `CalendarMonthData.toHeatmap()`, so `:library` stays independent
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
- Colors: `colorForIntensity` maps the normalized intensity (0..1) to the cell fill; selected
  cells get a `selectedBorderColor` border, which takes precedence over the today border.
- Layout follows the ambient `LayoutDirection`, so RTL works without extra parameters.
