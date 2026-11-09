# Version 2 implementation progress

Date: 2026-11-09
Branch: update
Architecture: decisions/0002-compose-v2-shared-core.md

## Implemented source

Shared selection/saveable snapshots, native grid and three calendars, adjacent-day
providers/sample switches, heatmaps, list/pager APIs and five sample modes, build/test/lint
and emulator workflows, plus corrections for manifest/test-host/KVM issues.

Sample navigation now uses the settled visible month. Previous/Next/dialog/mode switches
share that anchor; the 25-month window has a separate saved center and only recenters
when a requested month lies outside it. Adjacent-day switches and selection remain shared.
See COMPOSE_MONTH_CONTAINERS.md for synchronization contracts and unverified edge cases.

## Verification

55 added JVM tests and four added core Compose instrumented tests exist after this change.
Four window tests were added here; they are not gesture/coroutine/lifecycle tests.
No build/test/lint/emulator outcome is observed for the new navigation code. Previous
green workflow reports concern earlier commits, not this new state. Sample UI automation,
rapid gesture/request handling and real activity/process restoration remain open.

## Remaining work

Execute CI and sample navigation checks; add sample UI/restoration coverage; complete
legacy settings/customization/API parity; migrate CalendarViewActivity and remove obsolete
views/XML after replacement; isolate Jalali dependency; consolidate catalog; update main
documentation/agent rules and release metadata. Version stays 1.1.0-alpha1; migration
is incomplete and no 2.0.0 release/tag has been published.
