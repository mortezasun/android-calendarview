# Version 2 implementation progress

Date: 2026-11-09
Branch: update
Architecture: decisions/0002-compose-v2-shared-core.md

## Implemented

1. UI-independent selection engine (11 JUnit tests).
2. Compose selection owner, saveable selection and native customizable month grid.
3. Gregorian month builder/entry point (five tests).
4. Hijri month builder/entry point (five tests).
5. Jalali month builder, checked epoch conversion and entry point (five tests).
6. Compose launcher with selection/calendar/locale/RTL controls, navigation and dialog
   (four navigation tests). Legacy calendar demo remains reachable.
7. Compose quarter/year heatmaps and migrated HeatMapViewActivity (five data tests).
8. CalendarMonthList and CalendarMonthPager: both horizontal/vertical, native hoisted scroll
   state, saveable item keys, programmatic navigation through native state, active-index
   callbacks. Foundation is now an api dependency because public signatures expose its types.

See COMPOSE_MONTH_CONTAINERS.md for examples, viewport requirements and callback semantics.
Containers are not yet wired into sample controls. No new tests were added in step 8.

## Verification

All 35 added JUnit tests remain unexecuted. No Gradle compilation, lint, emulator/device run,
Compose UI, gesture, restoration or RTL/accessibility check has been performed. Available
GitHub tools do not execute builds. Compiler/plugin compatibility is unverified. Historical
PROJECT_STATUS.md successful results concern pre-migration code only.

```bash
bash ./gradlew :calendarviewsample:assembleDebug
bash ./gradlew test
bash ./gradlew :calendarviewsample:lintDebug :library:lintDebug
```

## Limits and remaining work

- Run builds/tests and implement Compose UI/restoration/scroll-state tests.
- Integrate list/pager modes into the sample with bounded viewports.
- Add adjacent-month cells and remaining legacy customization/features.
- Migrate legacy CalendarViewActivity; remove old views/layouts after replacements.
- Remove the dangling CustomCalendarViewActivity manifest entry as part of sample cleanup.
- Remove Jalali dependency from core after migrating legacy APIs that require it.
- Consolidate Compose coordinates in catalog; verify versions/toolchain compatibility.
- Update README, architecture, setup and agent rules; align release metadata to 2.0.0.

Calendar modules depend on core, not each other, but core still exports Jalali. BOM
2026.09.00 is in the core build file. Old views and ViewBinding remain. Release metadata
is unchanged. Disabled dates reject direct taps but may lie inside ranges. Locale and
layout direction are independent. Jalali checks reject inconsistent conversion; non-Persian
month names are transliterations. Hijri uses platform chronology, not moon sighting.

Heatmaps remain Gregorian with normalized intensities and synthetic demo data. The launcher
button still says 'Legacy heatmap demo' although its target is now Compose. Main calendar
dialog shares selection; dismissing does not cancel edits. Saved-state restoration remains
unverified. Finite month windows do not automatically grow and require stable identities.

This is incremental migration, not a completed, verified or published v2 release.
