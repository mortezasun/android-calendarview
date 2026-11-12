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

Verification: source review only. No Gradle build, JVM tests, instrumentation or device checks were run for these patches. The new tests are added, not claimed to have passed. Real foldable and asymmetric-fold RTL UI checks remain required.

No library API, dependency, version, tag or release is changed.

Remaining work, in order:
1. Inject fold geometry and add adaptive layout, resizing and restoration UI tests.
2. Make main viewport, agenda and dialog heights respect available pane height.
3. Demonstrate 336/376 dp month-width configuration in the sample.
4. Reconcile project-status and release claims with actual verification evidence.

The existing `PROJECT_STATUS.md` statement that adaptive folds are fully verified is not evidence of real foldable runtime coverage. This session is recorded here without rewriting the historical status log; its overall verification claims still need correction.
