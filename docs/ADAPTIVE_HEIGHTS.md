# Adaptive sample viewport heights

Prepared on 2026-11-12 for branch `update`.

## Main calendar viewport

The adaptive host captures the bounded content-pane height before its vertical scroll containers measure children. Safe-drawing insets are already excluded; 32 dp accounts for the pane's top and bottom padding. In tabletop mode, the calendar uses the top pane ending at the local fold start, not the full window height.

`LocalSamplePaneHeight` supplies this height only to the content slot. `CalendarSampleViewport` defaults to the smaller of 480 dp and 75 percent of that height. This proportional allowance leaves some space for controls but does not guarantee that navigation, selectors, captions, agenda and all options fit simultaneously. Outer scrolling remains intentional; tall month content remains navigable through the existing viewport scrolling modes.

The default reacts to pane-height changes without adding height to the viewport's composition keys. Explicit `viewportHeight` values remain caller-controlled, including the current dialog call. Outside the adaptive host, the preferred 480 dp default is retained. No positive minimum is forced on a zero-height pane.

Added seven JVM tests for the sizing policy. Verification is source review only: no build, test compilation, JVM execution, instrumentation or CI outcome is claimed. Very small and zero-height lazy containers, resizing, large fonts and selection/anchor continuity require runtime checks.

## Remaining height work

- Agenda still explicitly requests 280 dp and needs integration with the pane-height budget.
- Dialog still uses its existing 240/360 dp clamp and needs a separate constraint-respecting fix.
- The 75 percent policy is not exact measurement of controls and may need tuning after device checks.

No dependency, public library API, version, tag or release is changed. This document advances the height item in `ADAPTIVE_REVIEW_FIXES.md` but does not mark it complete.
