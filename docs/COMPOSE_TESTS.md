# Compose instrumentation checks

Module: :library
Test class: com.develotter.calendarview.compose.CalendarSelectionComposeTest
Status when introduced: source added, not compiled or executed.

## Scope

Four tests use createComposeRule and a debug host supplied by ui-test-manifest:

1. Clear a nonempty initial selection, simulate saved-instance-state restore and assert
   that the cleared selection does not revert to the initial date.
2. Select range endpoints through UI buttons, simulate restoration and verify endpoints
   plus membership of an interior absolute day.
3. Switch Multiple to Single during composition, check selection reset, restore and select
   a new single date. This does not independently test a different input at initial restore;
   codec mode-mismatch checks remain covered by JVM tests.
4. Verify a disabled grid cell's semantics and selection semantics after an enabled click.

StateRestorationTester simulates Compose saved-state restoration. It does not kill the
process, recreate the real sample activity or verify every Android lifecycle interaction.
No passing tests are claimed until the actual run reports success.

## Dependencies

Compose ui-test-junit4 is androidTestImplementation; ui-test-manifest is debugImplementation.
Versions use the same BOM as the core. These add test/debug dependencies, not release UI
features. The existing AndroidJUnitRunner remains unchanged.

## Commands

Compile/package the test APK without a device:

```bash
bash ./gradlew :library:assembleDebugAndroidTest
```

Run on a connected compatible emulator/device (minSdk 27):

```bash
bash ./gradlew :library:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.develotter.calendarview.compose.CalendarSelectionComposeTest
```

The existing Android verification workflow only runs assembleDebug, JVM test and lint.
It does not run these instrumentation tests or necessarily compile their APK. An emulator
job and artifact collection are still required for automated execution. Do not report this
change as completed emulator coverage.

## Remaining verification

Sample activity recreation/process death; list/pager restoration and gestures; window
identity changes; RTL and font scaling; accessibility/touch target behavior; consumer slots;
calendar conversions on actual Android runtimes; complete disabled-date interaction policy.

Official references:

- https://developer.android.com/develop/ui/compose/testing
- https://developer.android.com/develop/ui/compose/testing/common-patterns
