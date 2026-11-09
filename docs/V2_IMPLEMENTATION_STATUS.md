# Version 2 implementation progress

Date: 2026-11-09
Branch: update
Architecture: decisions/0002-compose-v2-shared-core.md

## Current source

Shared selection/snapshot/saveable state; three calendar month APIs/builders; native
heatmaps; list/pager APIs and five sample display modes; sample controls/navigation/dialogs;
build/test/lint and emulator workflows; corrected manifest, test-host assertion and KVM setup.

The shared month grid now supports independently visible previous/next dates via a
calendar-specific provider. isCurrentMonth is exposed to slots. Six pure grid tests added.
Existing calendar entry points/sample still use blank padding until providers are wired.
See ADJACENT_MONTH_DAYS.md for that precise scope and the remaining integration.

## Evidence and verification

The supplied screenshot shows Android instrumentation #3 for the KVM correction green.
It does not expose per-test counts. Earlier uploaded HTML showed four Compose tests passing
and the old smoke test failing; the smoke test and KVM setup were subsequently corrected.
Build/JVM-test/lint success was previously reported by the user. These results establish
a baseline for earlier code, not proof of the newly prepared grid changes.

47 new JVM tests and four new instrumented tests exist after this change; the six new JVM
tests have no observed result yet. Re-run both CI workflows for the new commit. Actual
activity/process recreation, RTL, font scaling, accessibility, gesture/state coverage and
additional runtime API levels remain open even when current CI is green.

## Remaining work

Wire adjacent-date providers into all calendar entry points and sample controls; complete
legacy API parity; synchronize swiped-month navigation/dialog/mode switching; migrate
legacy CalendarViewActivity; remove old views/XML and core Jalali dependency after replacement;
consolidate catalog; update main documentation/agent rules and release metadata.
Version remains 1.1.0-alpha1; no 2.0.0 release/tag created. Migration is not complete.
