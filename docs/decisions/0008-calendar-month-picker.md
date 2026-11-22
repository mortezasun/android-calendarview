# 0008: Calendar month picker

## Context

Hosts often require a month-selection dialog or popup that lets users quickly jump to a different
month or year without swiping through dozens of individual month pages. Previously, the library
offered month containers (`CalendarMonthList`, `CalendarMonthPager`) and the month grid
(`CalendarMonthGrid`), but no dedicated year/month selector. Hosts had to construct their own
grid of months or rely on platform-specific date pickers that do not align with the library's
calendar systems or design tokens.

## Decision

Add `CalendarMonthPicker` to `:library` (`com.develotter.calendarview.compose`).

1. **Controlled and stateless:**
   The host owns the selected year, selected month, and visible year state. The component renders
   one year (12 months) of selectable cells and reports changes via `onMonthSelected(year, month)`
   and optional `onYearChange(year)`. It keeps no internal mutable state.

2. **Calendar-agnostic:**
   Month numbers are represented as `1..12`. The component ships no hardcoded UI strings; the caller
   supplies `monthLabel: @Composable (Int) -> Unit`, `yearLabel: @Composable (Int) -> Unit`, and
   content descriptions for navigation controls. This allows the same picker to serve Gregorian,
   Hijri, Solar Hijri, or Chinese calendars simply by supplying the corresponding month labels.

3. **Accessibility and interaction:**
   - Month cells use `Modifier.selectable` with `Role.RadioButton` (or `Role.Button`), ensuring full
     keyboard focus, Enter/Space activation, and screen reader support.
   - The grid exposes `collectionInfo = CollectionInfo(rowCount = 4, columnCount = 3)` and each cell
     exposes `collectionItemInfo = CollectionItemInfo(rowIndex, 1, columnIndex, 1)`.
   - Each cell maintains a minimum interactive touch target of at least 48 dp.
   - Year navigation buttons disable automatically at the boundaries of `yearRange`.
   - Layout mirrors automatically according to ambient `LocalLayoutDirection`.

4. **Validation:**
   Inputs are validated using `requireMonthPickerSelection(yearRange, selectedYear, selectedMonth)`,
   ensuring `yearRange` is non-empty, `selectedYear in yearRange`, and `selectedMonth in 1..12`.
