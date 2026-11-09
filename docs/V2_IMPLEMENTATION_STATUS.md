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
- Saved selection now has version/mode headers, preserves empty values and rejects
  incompatible restoration. Six regression tests added for the snapshot codec.

See V2_AUDIT.md for the prioritized review of all recorded open areas. That checklist
separates implemented source from unverified behavior and incomplete migration/release work.
See COMPOSE_MONTH_CONTAINERS.md for current sample window/navigation limitations.

## Verification

All 41 added JUnit tests remain unexecuted. Source review only: no Gradle build, lint,
Compose UI, scroll/gesture, lifecycle restoration, device or RTL/accessibility test was run.
Available GitHub tools do not execute builds. Compiler/plugin compatibility is unverified.
Historical PROJECT_STATUS.md results describe pre-migration code, not the present branch.

```bash
bash ./gradlew :calendarviewsample:assembleDebug
bash ./gradlew test
bash ./gradlew :calendarviewsample:lintDebug :library:lintDebug
```

Snapshot tests validate encoding only, not Compose SavedStateRegistry integration.
Old unversioned development snapshots are intentionally not migrated.

## Release status

Not complete and not release-verified. Legacy views, adapters, CalendarViewActivity and
ViewBinding remain; core still exports Jalali. Adjacent-month cells and full API parity
remain open. Main documentation/agent rules need migration. BOM coordinates remain in
core build file; sample version is unchanged at 1.1.0-alpha1. No v2 tag/release was created.
