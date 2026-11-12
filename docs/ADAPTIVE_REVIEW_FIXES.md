# Adaptive review fixes

Branch: `update`. Updated on 2026-11-12.

## Step 1a: physical vertical-fold placement in RTL

Committed as `632af9717c1ff3c4c319968a576e7553b1e9d4db`.
The vertical-fold row uses physical LTR geometry; each pane restores the caller's layout direction.

## Step 1b: local fold coordinates and insets

- Safe-drawing insets are applied once outside the measured content container.
- `positionInWindow()` supplies the content origin, including insets and host offsets.
- `sampleLocalFold` subtracts that origin on the relevant axis, clips the reserved interval to the content bounds and ignores offscreen folds.
- Thin folds receive a centered minimum gap, clipped rather than shifted at content edges.
- Pane selection uses the inset-adjusted available width. Physical LTR placement and caller-direction contents are retained.
- Added six JVM geometry tests for vertical/horizontal translation, zero-width gaps, edge clipping, offscreen folds and invalid extent.

## Step 1c: injectable fold override and adaptive UI tests

- Added `LocalSampleFoldOverride` and `SampleFoldOverride`.
- Added `CalendarSampleAdaptiveLayoutTest` covering physical LTR/RTL fold placement, top insets and host offsets, and continuity across layout and fold changes.

## Step 1d: adaptive heights for viewport, agenda and dialog

- `LocalSamplePaneHeight` captures available content pane height.
- `sampleBoundedViewportHeight` bounds main viewport and agenda to 75% of available height.
- `sampleDialogViewportHeight` uses dialog slot constraint clamped to 360 dp without artificial minimums.
- Tested via 14 JVM unit tests in `CalendarSamplePaneHeightTest` and `CalendarSampleSecondaryHeightTest`.

## Step 1e: sample target month width honoring 48 dp day cells

- Added `CalendarSampleMonthWidth.kt` with `sampleTargetMonthWidth(showWeekNumbers)`.
- 336 dp target without week numbers (7 × 48 dp), 376 dp target with week numbers (336 dp + 40 dp).
- Passed down through `CalendarSampleViewport` to `CalendarMonthColumns` and `CalendarMonthList`.
- Tested via `CalendarSampleMonthWidthTest`.

## Step 1f: genuine state restoration and continuous layout verification

- `CalendarSampleStateRestorationTest` verifies genuine SavedInstanceState restoration using Compose's `StateRestorationTester` across all 17 configuration properties, selection range endpoints, display modes, and heatmap settings.
- `CalendarSampleAdaptiveLayoutTest` verifies that real calendar selection and month anchors survive continuous resizing, multi-column switches, and fold postures without recreation or reset.

## Step 1g: dedicated tablet CI verification

- Added `.github/workflows/android-tablet.yml` testing on API 29, 1600x2560 at 240 dpi tablet emulator.
- Verified all 4 CI workflows (verification, publishing, instrumentation, tablet).

