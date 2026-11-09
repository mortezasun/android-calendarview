# Instrumentation report investigation

Date: 2026-11-09
Branch: update
Evidence: user-uploaded Gradle report index.html, generated Nov 9, 2026 at 14:14:11.
The summary itself does not identify a commit or Actions run; do not invent that mapping.

## Observed results

| Class | Tests | Failures | Skipped | Duration |
|-------|-------|----------|---------|----------|
| CalendarSelectionComposeTest | 4 | 0 | 0 | 5.134 s |
| ExampleInstrumentedTest | 1 | 1 | 0 | 0.005 s |
| Total | 5 | 1 | 0 | 5.139 s |

The four Compose restoration/interaction tests passed in this supplied report. The only
reported failure is ExampleInstrumentedTest.useAppContext. This is stronger evidence than
the earlier generic Gradle VerificationException, but does not prove the whole sample UI,
other runtime API levels or all remaining migration behavior.

## Source inspection and correction

The inspected ExampleInstrumentedTest had a single assertion expecting targetContext's
package name to equal the library namespace com.develotter.calendarview. A library test
runs in its test APK rather than a separately installed application with that namespace.
The corrected smoke test asserts the default test host package com.develotter.calendarview.test
and verifies that its target context is the same package as the instrumentation context.
The package expectation is not removed, and no test is filtered, skipped or suppressed.

The uploaded index contains no detailed assertion message or expected/actual stack trace;
its links point to class pages not included in the upload. The correction follows the
checked library configuration and self-instrumenting host contract. A rerun must verify
that this host assumption holds for the actual generated APK. If it does not, inspect
the generated test manifest and full failure message rather than weakening the assertion.

## Rerun

The approved push to update triggers android-verification.yml and android-instrumentation.yml
subject to repository Actions policy. No manual rerun API is exposed by the available
connector. No successful post-fix result is claimed until that run is inspected.

Expected verification: all five currently observed core instrumented tests pass, none
skipped, alongside the existing assembly/JVM-test/lint checks. Do not equate this expected
outcome with an observed result. Historical V2_IMPLEMENTATION_STATUS.md statements that
all four new instrumented tests were unobserved are superseded by the supplied report
for this particular execution; post-fix results remain pending.

```bash
bash ./gradlew :library:connectedDebugAndroidTest --no-daemon --stacktrace
```

No application logic, Compose assertions, emulator settings or release metadata changed.
