# Version 2 implementation progress

Date: 2026-11-09
Branch: update
Architecture: decisions/0002-compose-v2-shared-core.md

## Current implementation

Shared Compose selection/snapshots/grid; Gregorian/Hijri/Jalali APIs with adjacent-day
providers; heatmaps; list/pager APIs and five sample modes; synchronized visible-month
navigation/dialog; sample locale/RTL/selection/adjacent-day controls; verification CI.

Five new instrumented tests target the real LauncherActivity for swipe/navigation/dialog/
mode behavior, selection after ActivityScenario recreation, dialog selection/clearing and
calendar/locale wiring. See SAMPLE_UI_TESTS.md. Emulator CI now compiles both test APKs and
runs both core and sample suites, preserving overall failure if either fails.

## Verification status

User reported both workflows green for navigation commit 41992996f0ea2df0d9b2115853fdd8d9d57bb320.
This is a user-reported baseline, not independent log retrieval or proof for this change.
55 added JVM tests and nine added instrumented tests exist (four core, five sample),
excluding pre-existing templates. The five sample tests and expanded CI have no observed
result yet. Prior green runs did not exercise these new sample acceptance checks.

Coverage gaps: real process death, all-axis gestures, rapid request/scroll interaction,
window-edge behavior, adjacent-day switches, multiple Android runtimes, RTL/font scaling,
accessibility and release/independent-consumer variants.

## Still open

Full legacy feature/API inventory and parity; legacy CalendarViewActivity migration; old
view/adapter/XML removal after replacement; core Jalali dependency isolation; centralized
version catalog and stable-version audit; complete localization; README/architecture/
setup/development/project-status/agent-rule migration; API migration guide; final acceptance
and version 2.0.0 metadata. Current sample version is 1.1.0-alpha1; no v2 release/tag created.
