# Migration Guide: Version 1.x to Version 2.0.0

This document maps all public types, configurations, layouts, and behaviors from Version 1.x (XML / Custom Views / ViewBinding) to their modern, native Jetpack Compose equivalents in Version 2.0.0.

---

## 1. Architecture Comparison

| Aspect | Version 1.x | Version 2.0.0 |
| :--- | :--- | :--- |
| **UI Framework** | Android Views & XML (`FrameLayout`, `RecyclerView`) | Jetpack Compose |
| **Binding Mechanism** | ViewBinding (`ViewBinding.inflate`, custom generic adapters) | Declarative Compose UI (`@Composable`) |
| **State Management** | Mutable objects (`DayStatus.isChecked`, mutable lists in Adapter) | Unidirectional Data Flow, Immutable State (`CalendarSelection`, `CalendarSelectionState`) |
| **State Preservation** | Manual bundle saving or lost on recreation | Native `rememberSaveable` & `SavedStateHandle` |
| **Date Identity** | Instanced `LocalDate` inside mutable view holders | Epoch Day (`Long`) for fast, calendar-agnostic comparisons |
| **Module Structure** | Coupled monolith with external Jalali dependency in Core | Strict modularization: `library` (Core Compose) + independent calendar artifacts |

---

## 2. Core Class & API Mapping

### Calendar Views & Containers

| Version 1.x Class | Status in 2.0.0 | Version 2.0.0 Equivalent | Notes / Migration Action |
| :--- | :--- | :--- | :--- |
| `MonthView<D, W, M, S>` | **Removed** | `GregorianCalendarMonth`, `HijriCalendarMonth`, `SolarHijriCalendarMonth` or `CalendarMonthGrid` | Replace custom FrameLayout with the corresponding composable function. |
| `MultiMonthView` | **Removed** | `CalendarMonthList` / `CalendarMonthPager` | Use `CalendarMonthList` for scrollable columns/rows or `CalendarMonthPager` for paged swiping. |
| `BaseCalendarAdapter` | **Removed** | Direct composable composition | No adapter subclassing needed. Pass day/month items directly to composables. |
| `MonthSampleAdapter` | **Removed** | Native Compose item builders | Day cells and headers are configured via Composable slots and parameters. |
| `DayCellViewAdapter` | **Removed** | Direct Composable slot (`dayContent`) | Custom cell styling is passed directly to `CalendarMonthGrid`. |

### Selection Engine & Status

| Version 1.x Class | Status in 2.0.0 | Version 2.0.0 Equivalent | Notes / Migration Action |
| :--- | :--- | :--- | :--- |
| `CalendarStatus` | **Removed** | `CalendarSelectionState` + Composable parameters | Replaced by Compose state holder and function parameters (locale, adjacent days). |
| `DayStatus` | **Removed** | `CalendarDayCell` / Epoch day (`Long`) | Day identity is represented by `Long` epoch days; visual cell data is in `CalendarDayCell`. |
| `MonthStatus` | **Removed** | Pure month data functions (`*MonthData`) | Use `gregorianMonthData`, `hijriMonthData`, or `solarHijriMonthData`. |
| `SelectRangeDayStatus` | **Removed** | `CalendarSelection.Range` | Computed automatically by `CalendarSelection.Range.contains` and endpoints. |
| `TypeSelectDay` | **Preserved** | `com.develotter.calendarview.enums.TypeSelectDay` | Unchanged enum (`None`, `Single`, `Multiple`, `Range`). |

### Calendar Modules

| Version 1.x Class | Module | Status in 2.0.0 | Version 2.0.0 Equivalent |
| :--- | :--- | :--- | :--- |
| `GregorianStatus`, `GregorianDayStatus` | `calendar-gregorian` | **Removed** | `GregorianCalendarMonth`, `gregorianMonthData`, `gregorianDayCell` |
| `HijriStatus`, `HijriDayStatus`, `HijriUtil` | `calendar-hijri` | **Removed** | `HijriCalendarMonth`, `hijriMonthData`, `hijriDayCell` |
| `SolarHijriStatus`, `SolarHijriDayStatus`, `SolarHijriUtil` | `calendar-solarHijri` | **Removed** | `SolarHijriCalendarMonth`, `solarHijriMonthData`, `solarHijriDayCell` |

### Heatmaps

| Version 1.x Class | Status in 2.0.0 | Version 2.0.0 Equivalent | Notes |
| :--- | :--- | :--- | :--- |
| `HeatMapCalendarView` | **Removed** | `CalendarYearHeatmap` | Native Compose heatmap without view hierarchy accumulation bugs. |
| `CalendarHeatmapYearSectionedView`| **Removed** | `CalendarYearHeatmap` | Fully declarative; re-renders cleanly on data changes. |
| `QuarterHeatmapView` | **Removed** | `CalendarQuarterHeatmap` | Single quarter heatmap with horizontal scrolling. |
| `DayHeatMapView` | **Removed** | Composable canvas cell | Rendered automatically within the heatmap grid. |

---

## 3. XML Attributes to Compose Properties

All XML layout declarations and styleables (`res/values/attrs.xml`) have been removed:

| XML Attribute (`CustomMonthView`) | Version 2.0.0 Equivalent | Usage Example |
| :--- | :--- | :--- |
| `app:numColumns="7"` | *Obsolete* | Standard calendar grid is always 7 columns. |
| `app:showRowWeekName="true"` | Configured via slot / month composable | Included by default in `*CalendarMonth`; custom header supported via `CalendarMonthGrid`. |
| `app:showRowMonthName="true"` | Title rendered by `*CalendarMonth` | Header bar can be displayed or replaced with a custom title slot. |
| `CalendarStatus.setShowLastMonth(true)` | `showPreviousMonthDays = true` | `GregorianCalendarMonth(..., showPreviousMonthDays = true)` |
| `CalendarStatus.setShowNextMonth(true)` | `showNextMonthDays = true` | `GregorianCalendarMonth(..., showNextMonthDays = true)` |

---

## 4. Code Migration Example

### Before (Version 1.x - XML & ViewBinding)
```xml
<!-- In layout XML -->
<com.develotter.calendarview.MonthView
    android:id="@+id/monthView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:showRowWeekName="true" />
```
```kotlin
// In Activity / Fragment
val calendarStatus = GregorianStatus(this).apply {
    setTypeSelectDay(TypeSelectDay.Range)
    setShowLastMonth(true)
    setShowNextMonth(true)
}
val adapter = object : MonthSampleAdapter<RowDayBinding, RowWeekBinding, RowMonthBinding, RowControllerBinding>(
    calendarStatus, this
) { ... }
binding.monthView.setUp(adapter, calendarStatus)
```

### After (Version 2.0.0 - Native Jetpack Compose)
```kotlin
@Composable
fun MyCalendarScreen() {
    val selectionState = rememberCalendarSelectionState(TypeSelectDay.Range)
    val currentMonth = YearMonth.now()

    GregorianCalendarMonth(
        month = currentMonth,
        selection = selectionState.selection,
        onDateClick = { date -> selectionState.onDayClick(date.toEpochDay()) },
        showPreviousMonthDays = true,
        showNextMonthDays = true,
        modifier = Modifier.fillMaxWidth(),
    )
}
```
