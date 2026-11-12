# Adaptive layout and state contract

Status: **contract only** (Phase 1). No code, public API, default value or dependency is changed by
this document. Later phases implement it; deviations must update this file first.

Verified against commit `00545e1` (`CalendarMonthColumns.kt`, `CalendarMonthGrid.kt`). Items that
cannot be verified from the repository are listed under [Open decisions](#open-decisions).

## Principles

1. **Width, not device.** Layout follows the width actually available to the calendar area. There
   are no `isTablet` checks; a tablet in narrow split-screen gets the compact layout.
2. **Core stays device-agnostic.** `:library` and the calendar engines know nothing about window
   size classes, postures or folds. `CalendarMonthColumns` only measures its own constraints
   (`BoxWithConstraints`).
3. **Sample owns window decisions.** Window size classes, panes, posture and fold handling live in
   `:calendarviewsample` only. Adding a window-size or fold dependency
   (for example `material3-adaptive`, `androidx.window`) needs explicit approval and stays
   sample-only.
4. **One container.** Multi-month layout extends `CalendarMonthColumns`; no second adaptive
   container and no per-engine tablet code.
5. **Controlled state.** The caller owns selection, calendar, mode and anchor. The library renders
   and reports.

## Minimum month width

```
minMonthWidth = 7 × dayWidth + (weekNumbers ? 40.dp : 0.dp) + extra spacing
```

| Desired day width | Without week numbers | With week numbers (40 dp column) |
|-------------------|----------------------|----------------------------------|
| 48 dp             | 336 dp               | 376 dp                           |

Values are before outer padding and exclude any additional `horizontalSpacing` inside the month
content (the grid itself adds none between day cells).

### Current behaviour (not changed)

- `CalendarMonthColumns` default `minMonthWidth = 320.dp`, `horizontalSpacing = 8.dp`.
- 320 dp / 7 ≈ **45.7 dp** per day without week numbers; (320 − 40) / 7 = **40 dp** with them.
- `CalendarMonthGrid` enforces only a **minimum height of 48 dp** per day row (and week-number
  cell). The width of a day is whatever the container gives it. The 320 dp default is therefore
  **not** a 48 dp-width touch-target guarantee.
- The default is not changed silently because it alters visible behaviour of existing callers.
  Hosts that want 48 dp-wide days pass `minMonthWidth = 336.dp` (or `376.dp` with week numbers).
  Any future opt-in preset must be an additive defaulted parameter placed before the trailing
  content lambda.

### Narrow containers (documented fallback)

If the container is narrower than the minimum month width, `CalendarMonthColumns` uses one column
and day cells shrink with the container (below 48 dp width, height stays ≥ 48 dp). The library does
not promise 48 dp-wide targets below `minMonthWidth`; hosts that need them must give the calendar
at least that width (scroll horizontally, change pane layout, or use the pager).

### Maximum columns

On very wide windows the column count grows with width. An optional `maxColumns` limit is **not**
part of this contract yet; it is added only if the sample demonstrates the need, as an additive
defaulted parameter. Until then hosts constrain width with `Modifier.widthIn(max = …)`.

## Active month

With several visible months the **active month is the month at
`LazyGridState.firstVisibleItemIndex`** (first visible item in reading order; top-left in LTR,
top-right in RTL). This is what `onMonthActive` reports today. Months to its right/below are
visible but not active. All visible months share **one** selection (epoch-day keyed); a tap in any
visible month updates the same `CalendarSelection`.

## Switching between one and many columns

- The visible month anchor survives width changes: the caller keeps the anchor month (index or
  month key); after a column-count change the grid scrolls so the anchor month is the first
  visible item again.
- Stable `monthKey` values must be used so items are recognized across column changes.
- Automatic resizes (rotation, fold, split-screen) must not cause a feedback loop between
  `onMonthActive`, the caller anchor and the grid position: the anchor is only updated by user
  scrolling or navigation, not by a layout-induced `firstVisibleItemIndex` change that the caller
  itself caused.
- The sample's `key(calendar.name, center, mode)` in `CalendarSampleViewport` intentionally resets
  the in-month position on **explicit** mode/calendar changes. It must not trigger on automatic
  resize.

## State that must survive rotation, folding and resizing

Calendar (Gregorian/Hijri/Solar Hijri) and Hijri offset, locale/RTL, selection mode and selection,
visible month/anchor, display options (week numbers, adjacent days, markers), heatmap year and
style, open dialogs and agenda day. Mechanism: `rememberSaveable` and the existing
`CalendarSelectionSnapshot` saver; no ViewModel, Flow or `configChanges` workaround.

## Out of scope

Multi-instance windows and presentation on external displays are separate features and are not
covered by this contract.

## Phases

All planned phases 1 through 7 are implemented:
1. Contract (`docs/ADAPTIVE_LAYOUT.md`).
2. Library constraint tests (`CalendarMonthColumnsTest.kt`).
3. Split sample state/controls (`CalendarSampleState.kt`, `CalendarSampleControls.kt`, `CalendarSampleScreen.kt`).
4. Adaptive panes (`CalendarSampleAdaptiveLayout.kt` at 840 dp).
5. Fold-aware layout (`androidx.window:window:1.5.1`, `CalendarSampleFold.kt`, book/tabletop postures).
6. Adaptive dialogs & heatmap demo (`CalendarSampleScreen.kt`, `HeatMapViewActivity.kt`).
7. State restoration & recreation tests (`CalendarSampleStateTest.kt`, `CalendarSampleUiTest.kt`).

## Open decisions

- Whether to add `maxColumns` and/or a 48 dp-day preset to `CalendarMonthColumns`.
- Whether the 320 dp default should ever change (visible behaviour change; major/minor policy).
