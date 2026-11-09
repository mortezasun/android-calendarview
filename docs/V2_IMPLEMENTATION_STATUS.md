# Version 2 implementation progress

Date: 2026-11-09
Branch: update
Architecture: decisions/0002-compose-v2-shared-core.md

## Implemented

- Shared selection engine and saveable Compose ownership (11 JUnit tests).
- Native customizable month grid and Gregorian/Hijri/Jalali entry points (15 data tests).
- Compose launcher with calendar/selection/locale/RTL controls, month navigation, shared
  selection dialog and heatmap link (four navigation tests).
- Native quarter/year heatmaps and migrated HeatMapViewActivity (five tests).
- Calendar-independent vertical/horizontal list and pager library APIs.
- CalendarSampleViewport integrates all five display modes into the launcher using a
  bounded 480dp viewport and 25 real calendar months. Scroll states use epoch-day keys
  and reset when calendar/window anchor changes. Heatmap button label is corrected.

See COMPOSE_MONTH_CONTAINERS.md for mode/window/navigation semantics and limitations.
The viewport does not synchronize its swiped month back to the outer anchor. Outer navigation
recenters the window; the dialog shows that anchor month. List and pager positions are
remembered separately. All modes share the same outer selection.

## Verification

Source review only. All 35 added JUnit tests remain unexecuted; no new tests in this step.
No Gradle build, lint, Compose UI, gesture, restoration, device or RTL/accessibility check
was performed. Available GitHub tools do not run builds. Plugin/compiler compatibility is
unverified. Historical PROJECT_STATUS.md successful checks concern pre-migration code only.

```bash
bash ./gradlew :calendarviewsample:assembleDebug
bash ./gradlew test
bash ./gradlew :calendarviewsample:lintDebug :library:lintDebug
```

Manual checks: all calendars/modes/locales, list and pager swipes, RTL, large fonts, rotation,
anchor recentering, cross-month selection and shared dialog selection, heatmap navigation.

## Remaining work

- Run actual builds/tests; add UI/restoration/scroll tests.
- Add adjacent-month cells and remaining legacy customization/features.
- Migrate legacy CalendarViewActivity; remove old views/layouts after replacements.
- Clean dangling CustomCalendarViewActivity manifest entry.
- Remove Jalali dependency from core after migrating legacy APIs requiring it.
- Consolidate Compose coordinates in catalog; verify dependency/toolchain versions.
- Update README, architecture, setup and agent rules; align metadata to 2.0.0.

Core still exports Jalali; legacy ViewBinding remains. BOM 2026.09.00 remains in core build
file and release metadata is unchanged. Disabled dates reject direct taps but can lie inside
selected ranges. Locale and layout direction are independent. Hijri uses platform chronology.
Jalali checks reject inconsistent conversion and non-Persian names are transliterations.
Heatmaps remain Gregorian with synthetic demo data. State restoration is not verified.

This is an incremental migration, not a completed, verified or published v2 release.
