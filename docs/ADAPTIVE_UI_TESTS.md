# Adaptive sample UI regression tests

Prepared on 2026-11-12 for branch `update` after fold-coordinate commit `080c7aa`.

`CalendarSampleAdaptiveLayoutTest` uses the actual adaptive host and a sample-only `LocalSampleFoldOverride`. An explicit `SampleFoldOverride(null)` disables real fold detection in the fixture. Without a provider, production still observes WindowManager.

Three instrumented tests cover:
- Asymmetric vertical-fold geometry in LTR and RTL, with caller direction retained inside panes.
- Horizontal-fold placement with safe-drawing padding and a nonzero host offset.
- A caller-content counter surviving compact/expanded width changes and vertical/horizontal/no-fold pane transitions through movable content.

The expanded test uses a virtual 1000 dp container. Its offscreen geometry is measured in window coordinates; this is not a claim that a phone emulator displays a tablet viewport. The state test checks composition continuity, not activity recreation, saved-state restoration or process death. It does not prove calendar scroll-anchor or selection restoration.

Verification: source review only. No Gradle build, test compilation or instrumented execution has been performed for this patch. Tests are added, not reported as passing.

Required next checks:
```bash
./gradlew :calendarviewsample:assembleDebug :calendarviewsample:compileDebugAndroidTestKotlin
./gradlew :calendarviewsample:connectedDebugAndroidTest
```

Real foldable posture delivery, actual OS window resizing, recreation, calendar anchor/selection and full-device tablet layouts remain separate acceptance work. No dependency, published library API, version, tag or release is changed.

This document advances item 1 of the remaining-work list in `ADAPTIVE_REVIEW_FIXES.md`; it does not mark all restoration coverage complete. Main/agenda/dialog heights, month-width presets and verification-claim reconciliation remain open.
