# Version 2 implementation progress

Date: 2026-11-09
Branch: update
Architecture: decisions/0002-compose-v2-shared-core.md

## Current implementation

- Shared selection engine, versioned snapshot codec and saveable Compose owner.
- Native Gregorian/Hijri/Jalali month UI and data builders.
- Native quarter/year heatmaps and migrated heatmap demo.
- Shared lists/pagers and five bounded sample display modes.
- Sample selection/calendar/locale/RTL controls, navigation and shared dialog.
- Build/JVM-test/all-module-lint Actions workflow configured; results unobserved.
- Four Compose instrumented tests added for saved-state restoration, mode changes and
  grid interaction semantics, with required test/debug dependencies. See COMPOSE_TESTS.md.

## Priority 0 verification

41 added JVM tests and four added instrumented Compose tests are unexecuted from this
session's perspective. Test source is not proof of compilation or success. No observed
Gradle, lint, emulator/device, UI or restoration result exists for this migration.
The public Actions page fetch failed; the connector cannot inspect Actions runs/logs.
Existing CI does not execute or necessarily compile the newly added instrumentation tests.
An emulator CI job remains open. See CI.md for current workflow scope and V2_AUDIT.md for
the priority checklist. Historical PROJECT_STATUS.md results concern pre-migration code.

```bash
bash ./gradlew :calendarviewsample:assembleDebug :library:assembleDebugAndroidTest
bash ./gradlew test
bash ./gradlew :library:connectedDebugAndroidTest
```

The final command needs a compatible running emulator/device. StateRestorationTester
simulates Compose restoration; it does not replace real activity/process-death checks.
Compiler/plugin and SDK-package availability remain unverified until builds run.

## Remaining migration/release work

Adjacent-month cells, full old API parity, synchronized navigation from swiped months,
legacy CalendarViewActivity/views/layout cleanup, dangling manifest entry, Jalali dependency
isolation, catalog consolidation, dependency/toolchain verification and main documentation
updates remain open. Version stays 1.1.0-alpha1; no 2.0.0 release/tag was created.
