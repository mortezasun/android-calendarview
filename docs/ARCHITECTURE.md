# Architecture (v2.0.0)

## Overview

`android-calendarview` is a **native Jetpack Compose calendar library** with decoupled calendar engines and an interactive demo app. Version 2.0.0 eliminates all legacy XML layouts, ViewBinding, custom view measurement, and reflection.

The architecture follows standard Compose state and rendering patterns:
- **Stateless rendering:** `CalendarMonthGrid` and `CalendarDayCell` render month layouts declaratively.
- **Calendar abstraction:** Calendar engines supply month data snapshots (`GregorianMonthData`, `HijriMonthData`, `SolarHijriMonthData`) and day cells (`CalendarDayCell`).
- **Unified selection engine:** `CalendarSelection` and `CalendarSelectionState` operate on universal epoch-day identities (`Long`).
- **Containers:** Compose foundational lists and pagers (`HorizontalPager`, `VerticalPager`, `LazyRow`, `LazyColumn`) provide smooth scrolling and month navigation.
- **Heatmaps:** `CalendarQuarterHeatmap` and `CalendarYearHeatmap` render activity intensities as a grid of Compose cells, configurable like the month grid.

---

## Modules

| Module | Responsibility |
|--------|----------------|
| `:library` | Core Compose views (`CalendarMonthGrid`, `CalendarQuarterHeatmap`, `CalendarYearHeatmap`), month containers (`CalendarMonthList`, `CalendarMonthPager`), selection state machine (`CalendarSelection`, `CalendarSelectionState`, `TypeSelectDay`), and cell data models. |
| `:calendar-gregorian` | Gregorian calendar engine backed by `java.time.YearMonth` and `java.time.LocalDate`. Provides `GregorianCalendarMonth`, `gregorianMonthData`, and `gregorianDayCell`. |
| `:calendar-hijri` | Islamic Hijri calendar engine backed by `java.time.chrono.HijrahDate`. Provides `HijriCalendarMonth`, `hijriMonthData`, and `hijriDayCell`. |
| `:calendar-solarHijri` | Solar Hijri / Jalali calendar engine backed by `ir.huri:jalalicalendar`. Provides `SolarHijriCalendarMonth`, `solarHijriMonthData`, and `solarHijriDayCell`. |
| `:calendarviewsample` | Interactive Compose demo app illustrating configuration, selection modes, navigation, dialogs, and heatmaps. |

### Dependency Rules

- The dependency graph is strictly one-way: `calendar-* -> library`, `calendarviewsample -> all`.
- `:library` does not depend on any specific calendar engine or third-party calendar library (such as `jcal`).
- `calendar-solarHijri` is the only module that declares and encapsulates the `ir.huri:jalalicalendar` dependency.

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

## Heatmap Architecture

The heatmap is decoupled from month grid calculations but shares its configuration model:
- `CalendarQuarterHeatmap` (Gregorian, `Map<LocalDate, Float>`) renders one quarter;
  the Gregorian `CalendarYearHeatmap(year, ...)` stacks four quarters.
- The calendar-agnostic `CalendarYearHeatmap(months: List<HeatmapMonthData>, ...)` renders
  month blocks built by the caller with any engine (`gregorianMonthData`, `hijriMonthData`,
  `solarHijriMonthData`), so `:library` stays independent of calendar engines.
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
