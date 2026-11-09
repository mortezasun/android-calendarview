# Architecture

## Overview

This repository is a **calendar widget library** plus a demo app. It does not use MVVM, MVI or
Clean Architecture in the usual app sense. The central pattern is a **generic custom-view library
with template-method adapter hooks**:

- `MultiMonthView` owns the scrolling list of months.
- `MonthView` renders one month as a manually measured grid.
- `MonthStatus` / `DayStatus` abstract the calendar system.
- `BaseCalendarAdapter` / `MonthSampleAdapter` let the consumer supply `ViewBinding` instances and
  receive callbacks.

## Modules

| Module | Responsibility |
|--------|----------------|
| `:library` | Public views (`MultiMonthView`, `MonthView`), heatmap views, enums, status base classes, selection logic. |
| `:calendar-gregorian` | `GregorianStatus` / `GregorianDayStatus` backed by `YearMonth` / `LocalDate`. |
| `:calendar-hijri` | `HijriStatus` / `HijriDayStatus` backed by `HijrahDate`, plus conversion helpers. |
| `:calendar-solarHijri` | `SolarHijriStatus` / `SolarHijriDayStatus` backed by `JalaliCalendar`, plus conversion helpers. |
| `:calendarviewsample` | Demo activities and layout bindings; not published. |

Dependency direction is one-way: `calendar-* -> library`, `calendarviewsample -> all`. `library`
has no knowledge of the concrete calendar modules; the calendar class is passed in by the consumer.

## Entry points

- `com.develotter.calendarview.MultiMonthView` - XML-inflatable view; configured with
  `addMonths(clazzMonth, adapter)`.
- `com.develotter.calendarview.status.CalendarStatus` - configuration object (view mode, locale,
  selection type, visible labels, month window size).
- `com.develotter.calendarview.adapter.MonthSampleAdapter` - the class consumers subclass.
- `com.develotter.calendarview.heatmap.CalendarHeatmapYearSectionedView` - heatmap entry point with
  `applyHeatmapData(year, data, onDayClick)`.

## Generic ViewBinding contract

`MultiMonthView<D, W, M, SelectController>` and `MonthView` are parameterized by four
`ViewBinding` types:

| Type | Used for | Binding hook |
|------|----------|--------------|
| `D` | Day cell | `onBindDayView`, `onBindLastMonthDayView`, `onBindNextMonthDayView` |
| `W` | Weekday label | `onBindWeekView` |
| `M` | Month header | `onBindMonthView`, `onUpdateMonthView` |
| `SelectController` | Selected-day summary | `select` property and `onBindControllerCalendar` |

`MonthView` is instantiated programmatically by `MultiMonthDataAdapter`; `MultiMonthView` is
usually declared in XML and inflated. The type parameters are erased at runtime, so
`MultiMonthView.addMonths` reflects on the supplied `MonthStatus` class and the code contains
`@Suppress("UNCHECKED_CAST")` at the view/adapter boundaries.

## Rendering pipeline

1. `MultiMonthView.addMonths` removes all children and builds `yearMonthList`.
2. The list contains `getCountsMonthAfterAndBefore() * 2` months. Each entry is created through
   reflection: a helper instance is moved by `minusMonths(N).plusMonths(i)`, then the new base
   instance receives that month via `setInstanceDay`.
3. Optional headers are added: one month caption row (`onBindMonthView`) and, for
   `TypeWeekShow.Fix`, one fixed row of seven weekday labels (`onBindWeekView`).
4. A `RecyclerView` with `MultiMonthDataAdapter` is added. Its layout manager depends on
   `TypeViewCalender`: vertical/horizontal list or pager. Pager modes attach a `PagerSnapHelper`
   and report the snapped position through `onMonthViewActive`.
5. For each bound item, `MonthView.setUp` creates a `DayStatus` per day through
   `MonthStatus.setOnCreateDayStatus`. Leading/trailing grid cells are filled from the previous and
   next month when `CalendarStatus` allows it.
6. `MonthView` measures and lays out the grid itself: equal cell width/height, 7 columns by
   default, RTL-aware column placement.
7. `MonthSampleAdapter.onCreateDayView` installs click/long-click listeners and caches the binding
   in `dayStatusListSelectedViewBinding`, keyed by a zero-padded `yyyyMMdd` string.

`MultiMonthView` also exposes navigation helpers (`scrollToNextMonth`, `scrollToPreviousMonth`,
`scrollToThisMonth`, `scrollToToday`, `scrollToMonth`). Position reads are only valid while the
RecyclerView is idle; `getPosition` returns `-1` otherwise.

## State and selection

There is no ViewModel and no reactive stream. State is held in mutable objects:

- `CalendarStatus` - configuration; changing it requires rebuilding via `addMonths`.
- `DayStatus.isChecked` and `DayStatus.selectRangeDayStatus` - per-day selection flags.
- `BaseCalendarAdapter` - selection lists for none/single/multiple/range.
- `MultiMonthDataAdapter` / `MonthView` - transient view state.

When a day is clicked, `MonthSampleAdapter.onHandleClickRowDay` dispatches by
`CalendarStatus.getTypeSelectDay()`. The adapter updates the flags and lists, then calls
`onDayItemClick(dayStatus, binding)` so the consumer repaints that cell. Range selection additionally
recomputes intermediate days and repaints all cached bindings that fall inside the range.

Because only existing view bindings can be repainted immediately, off-screen days keep their state
flags but are not re-rendered until the consumer decides how to restore them. There is no
persistence: selections live in memory and disappear when the owning activity is recreated.

## Heatmap

The heatmap is independent of the month/calendar abstraction:

- `CalendarHeatmapYearSectionedView` stacks four `QuarterHeatmapView`s.
- `QuarterHeatmapView` builds a Monday-first week grid for a date range and hosts one
  `DayHeatMapView` per day plus month/weekday labels.
- `HeatMapCalendarView` is an alternative single-year view with the same `DayHeatMapView` cells.
- `DayHeatMapView` draws a colored rectangle from `heatIntensity` (`0.0..1.0`, not clamped) and
  forwards clicks through `onDayClick`.

The heatmap uses `java.time.LocalDate` only and does not support the Hijri/Jalali engines.

## Layers that do not exist

- **Dependency injection:** none (no Hilt/Koin/Dagger). Objects are constructed manually.
- **Navigation:** the sample uses plain `Activity` intents. Navigation libraries are declared in
  the sample but no navigation graph is used.
- **Network, database, file storage:** none.
- **Background work:** none (no coroutines, workers or services).
- **Compose:** none. The Compose plugin alias is declared at the root with `apply false` only.

## Important files

| File | Role |
|------|------|
| `library/src/main/java/com/develotter/calendarview/MultiMonthView.kt` | Month window and RecyclerView host. |
| `library/src/main/java/com/develotter/calendarview/MonthView.kt` | Manual month grid. |
| `library/src/main/java/com/develotter/calendarview/adapter/MonthSampleAdapter.kt` | Selection behaviour. |
| `library/src/main/java/com/develotter/calendarview/status/CalendarStatus.kt` | Configuration surface. |
| `library/src/main/java/com/develotter/calendarview/status/MonthStatus.kt` | Calendar-system contract. |
| `calendar-gregorian/.../GregorianStatus.kt` | Reference implementation of `MonthStatus`. |
| `calendar-hijri/.../HijriStatus.kt` | Manual Hijri month arithmetic. |
| `calendar-solarHijri/.../SolarHijriStatus.kt` | Manual Jalali month arithmetic. |
| `library/src/main/java/com/develotter/calendarview/heatmap/` | Heatmap views. |
| `calendarviewsample/src/main/java/com/develotter/calendarviewsample/CalendarViewActivity.kt` | Full adapter wiring example. |
