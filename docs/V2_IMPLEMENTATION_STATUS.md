# Version 2 implementation progress

Date: 2026-11-09
Branch: update
Architecture: decisions/0002-compose-v2-shared-core.md

## Current source/configuration

Shared selection engine/versioned snapshots/saveable ownership; native Gregorian, Hijri
and Jalali month APIs; quarter/year heatmaps; shared lists/pagers; five bounded sample
display modes; calendar/selection/locale/RTL controls, navigation and dialogs.
41 new JVM tests and four new Compose instrumented tests have been added.

The missing CustomCalendarViewActivity manifest declaration was removed in commit
31fbc8773ce7c0da5816372d900a01eee7a652b6. See CI_FAILURES.md for the reported lint error.

## Priority 0 verification

User reported run 42630675110 as 'alles ok' after the manifest correction:
https://github.com/mortezasun/android-calendarview/actions/runs/42630675110

Record this as user-reported successful verification, not independently observed results.
The assistant's fetch of that run failed; exact job conclusions/test counts were not
retrieved. Do not claim that the four Compose tests ran in the original JVM-test workflow.

A separate Android instrumentation workflow is now configured to compile the core test
APK then run all core connected tests on an API 29 google_apis x86_64 emulator, using an
explicit ubuntu-24.04 runner. Reports, test APK and diagnostic logs are collected when
available. No emulator startup/test outcome has been observed. See COMPOSE_TESTS.md.

The original android-verification.yml remains unchanged and checks assembly, JVM tests
and all-module lint. Adding this second workflow can trigger additional Actions usage.
Historical PROJECT_STATUS.md results still concern pre-migration code.

## Remaining work

Confirm emulator results and fix actual failures; expand UI/restoration/API-level coverage.
Then continue adjacent-month cells and full legacy parity, synchronized swiped-month
navigation, legacy CalendarViewActivity/views/XML cleanup, Jalali dependency isolation,
version-catalog consolidation and main documentation/agent-rule migration. The missing
manifest declaration is fixed at source level and no longer an outstanding code change.
Sample version remains 1.1.0-alpha1; no 2.0.0 release/tag created.
