# Version 2 implementation progress

Date: 2026-11-09
Branch: update
Architecture: decisions/0002-compose-v2-shared-core.md

## Current implementation

Shared selection/versioned snapshots/saveable ownership; three native calendar month
APIs/builders; heatmaps; shared lists/pagers and five sample modes; CI and emulator checks.

Adjacent days are now wired into Gregorian/Hijri/Jalali entry points using calendar-specific
day factories. Two saved sample switches apply to all modes and the dialog. Both visibility
flags default off. The same enabled predicate, selection identity and click behavior apply
to current and adjacent dates. See ADJACENT_MONTH_DAYS.md for contracts and compatibility.

## Verification evidence

User reported both workflows green for prior grid commit bab4922d1b9f29392d0e379d207bd2d18a95f8aa.
Earlier screenshot confirmed the KVM-correction instrumentation run green. These establish
previous baselines only; this integration has no observed build/test/lint/emulator outcome.

51 new JVM tests and four new Compose instrumented tests exist after this change. Four
JVM engine-integration tests were added here. Device behavior and CI results must be
checked again for the new commit. No full activity/process recreation, font-scaling, RTL
or accessibility acceptance is implied by the current focused instrumented tests.

## Remaining work

Synchronize swiped-month navigation/dialog/mode changes, complete legacy settings/custom
rendering/API parity, migrate legacy CalendarViewActivity and remove obsolete views/XML
after replacement, isolate Jalali dependency, consolidate version catalog, update main
documentation/agent rules and align release metadata. Expand UI/restoration/device coverage.
Version remains 1.1.0-alpha1; migration is incomplete and no 2.0.0 release/tag is published.
