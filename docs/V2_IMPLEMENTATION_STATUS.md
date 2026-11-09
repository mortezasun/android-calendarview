# Version 2 implementation progress

Date: 2026-11-09
Branch: update
Architecture: decisions/0002-compose-v2-shared-core.md

## Current implementation

- UI-independent selection engine and observable/saveable Compose owner.
- Native month grid, Gregorian/Hijri/Jalali builders and entry points.
- Native quarter/year heatmaps and migrated heatmap activity.
- Shared vertical/horizontal lists and pagers, integrated in five sample display modes.
- Sample calendar/selection/locale/RTL controls, bounded 25-month window, navigation and dialog.
- Versioned saved-selection codec with mode validation and preservation of empty selections.
- Android verification workflow configured for update pushes/PRs, with independent assemble,
  JVM test and all-module lint jobs plus available report/APK artifacts. See CI.md.

V2_AUDIT.md remains the prioritized checklist. Its statement that no workflow existed
describes the earlier audit snapshot; a workflow is now configured, but actual execution
and results remain pending. COMPOSE_MONTH_CONTAINERS.md documents current navigation limits.

## Verification

All 41 added JUnit tests remain unexecuted from this session's perspective. No Gradle build,
lint, Compose UI, gesture, lifecycle restoration, device or RTL/accessibility result has
been observed. Available GitHub connector tools cannot execute builds or inspect Actions
runs. CI configuration is not proof of a passing build. Compiler/plugin compatibility
remains unverified. Historical PROJECT_STATUS.md results concern pre-migration code only.

The workflow can run automatically after the approved push, subject to repository settings.
It uses the documented JDK/SDK configuration without bypassing failures. Verify conclusions
for the exact commit in GitHub Actions; record actual results before closing priority 0.

Local equivalents:

```bash
bash ./gradlew :calendarviewsample:assembleDebug
bash ./gradlew test
bash ./gradlew :library:lintDebug :calendar-gregorian:lintDebug :calendar-hijri:lintDebug :calendar-solarHijri:lintDebug :calendarviewsample:lintDebug
```

## Release status

Not complete or release-verified. Legacy views/adapters, CalendarViewActivity and ViewBinding
remain; core still exports Jalali. Adjacent-month cells and full API parity remain open.
Documentation/agent rules need migration. BOM is in core build file; sample version remains
1.1.0-alpha1. No v2 tag/release created. Emulator/UI/restoration tests are not part of CI yet.
