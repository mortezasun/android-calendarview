# Architecture (v2.0.0)

## Overview

`android-calendarview` is a **native Jetpack Compose calendar library** with decoupled calendar engines and an interactive demo app. Version 2.0.0 eliminates all legacy XML layouts, ViewBinding, custom view measurement, and reflection.

The architecture follows standard Compose state and rendering patterns:
- **Stateless rendering:** `CalendarMonthGrid` and `CalendarDayCell` render month layouts declaratively.
- **Calendar abstraction:** Calendar engines supply month data snapshots (`GregorianMonthData`, `HijriMonthData`, `SolarHijriMonthData`) and day cells (`CalendarDayCell`).
- **Unified selection engine:** `CalendarSelection` and `CalendarSelectionState` operate on universal epoch-day identities (`Long`).
- **Containers:** Compose foundational lists and pagers (`HorizontalPager`, `VerticalPager`, `LazyRow`, `LazyColumn`) provide smooth scrolling and month navigation.
- **Canvas Heatmaps:** `CalendarQuarterHeatmap` and `CalendarYearHeatmap` render activity intensities using Compose graphics.

---

## Modules

| Module | Responsibility |
|--------|----------------|
| `:library` | Core Compose views (`CalendarMonthGrid`, `CalendarQuarterHeatmap`, `CalendarYearHeatmap`), month containers (`CalendarHorizontalPager`, `CalendarVerticalPager`, etc.), selection state machine (`CalendarSelection`, `CalendarSelectionState`, `TypeSelectDay`), and cell data models. |
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

The heatmap component is decoupled from month grid calculations:
- `CalendarQuarterHeatmap` renders 13-week quarter columns using Compose `Canvas`.
- `CalendarYearHeatmap` stacks four quarters vertically or horizontally.
- Intensities are represented as a map `Map<LocalDate, Float>` where values range from `0.0f` to `1.0f`.
- Weekday labels and month headers adapt to ambient light/dark theme color schemes.
