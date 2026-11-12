# Adaptive review fixes

## Step 1a: physical vertical-fold placement in RTL

Branch: `update`. Prepared on 2026-11-12.

- The vertical-fold row uses LTR geometry because `SampleFold.start` is measured from the physical left window edge.
- Each pane restores the caller's layout direction, including RTL calendar rendering and controls.
- No library API, dependency, version, tag or release is changed.

Verification: source review only. No Gradle build, JVM tests, instrumentation or device checks were run for this patch. A successful build and an asymmetric-fold RTL UI test remain required.

Remaining review work, in order:
1. Translate fold window coordinates into local coordinates and account for safe-drawing insets.
2. Inject fold geometry and add adaptive layout, resizing and restoration UI tests.
3. Make main viewport, agenda and dialog heights respect available pane height.
4. Demonstrate 336/376 dp month-width configuration in the sample.
5. Reconcile project-status and release claims with actual verification evidence.

The existing `PROJECT_STATUS.md` statement that adaptive folds are fully verified is not evidence of real foldable runtime coverage. This patch does not resolve the separately reported inset-coordinate issue.
