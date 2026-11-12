# Adaptive test import correction

Prepared on 2026-11-12 for branch `update`.

Source review after commit `abd58cf` found an invalid package-level import of `androidx.compose.ui.test.assertExists` in `CalendarSampleAdaptiveLayoutTest.kt`. `assertExists` is a member of `SemanticsNodeInteraction`; its existing calls require no such import.

The patch removes only that import from the test file. All three test cases, assertions and production code remain unchanged. This corrects an error introduced in the preceding patch rather than weakening tests.

Evidence: Android API reference, https://developer.android.com/reference/kotlin/androidx/compose/ui/test/SemanticsNodeInteraction

No Gradle compile or instrumentation run was performed. This source correction is not a claim that the full test suite compiles or passes. CI logs were not retrieved in this session.

Adaptive main viewport, agenda and dialog heights remain the next feature work. The patch changes no dependency, published API, version, tag or release.
