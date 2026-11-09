# Version 2 implementation progress

Date: 2026-11-09
Branch: update
Architecture: decisions/0002-compose-v2-shared-core.md

## Implemented

1. UI-independent selection engine: 11 JUnit tests.
2. Compose selection ownership, saved selection and customizable native month grid.
3. Gregorian month data and Compose entry point: five tests.
4. Hijri month data and Compose entry point: five tests.
5. Jalali month data, checked epoch conversion and Compose entry point: five tests.
6. Compose launcher with calendar/mode/locale/RTL switching, month navigation, selection
   summary, clearing and shared-selection dialog: four navigation tests.
7. Native CalendarQuarterHeatmap and CalendarYearHeatmap; HeatMapViewActivity migrated
   to Compose with synthetic data, bounded year navigation (1900..2100), weekday visibility
   switch and day-details dialog. Five heatmap data tests added.

## Heatmap API and behavior

heatmapQuarterData builds chronological seven-day week columns with null padding outside
the quarter. Missing intensities become zero; finite values clamp to 0..1, while NaN and
infinities become zero. Input maps are not mutated or retained by the resulting cells.
Out-of-quarter entries are ignored. The heatmap remains Gregorian, matching the old API.

Quarter views use 48dp clickable cells and horizontal scrolling, localized quarter/month
range and weekday labels, localized date/intensity accessibility descriptions and an
optional custom color function/day slot. Default colors preserve the old implementation's
red-at-zero to green-at-one direction. Year view composes four quarters; the caller supplies
vertical scrolling. Recomposition replaces content rather than accumulating child views.
Layout follows ambient direction; actual RTL/accessibility behavior is unverified.

HeatMapViewActivity no longer inflates XML or ViewBinding. Its existing launcher button
still says 'Legacy heatmap demo', but now opens the Compose screen. The old heatmap classes
and layouts remain for compatibility until the public API migration is complete; old bugs
are not claimed fixed in those retained classes. Demo intensities are deterministic synthetic
values, not user data. The demo uses the system locale/theme independently of launcher options.

## Other behavior and limits

Calendar modules depend on shared core, not one another; core still exports Jalali pending
legacy cleanup. BOM 2026.09.00 is in the core build file. Release metadata is unchanged.
Single-month APIs lack library-level list/pager navigation and adjacent-month cells. Calendar
labels follow locale but RTL is a separate host setting. Disabled days reject direct taps
but may lie inside selected ranges. Hijri follows platform chronology, not moon sighting.
Jalali checks reject inconsistent conversions; non-Persian month names are transliterations.

Launcher preserves primitive settings and anchor with rememberSaveable; selection mode
changes clear selection during composition. Switching calendars retains absolute dates.
Main screen and dialog share selection, so dialog dismissal does not cancel edits. Controls
are English demo labels. Old CalendarViewActivity remains reachable and ViewBinding-enabled.
The pre-existing dangling CustomCalendarViewActivity manifest entry remains unresolved.

## Verification

Source review only. All 35 added JUnit tests are unexecuted. No Gradle compilation, lint,
emulator/device run, UI, restoration or RTL/accessibility check was performed. The GitHub
connector is not a build runner. Compiler/plugin compatibility is unverified. Historical
PROJECT_STATUS.md successful checks concern pre-migration code, not the current changes.

```bash
bash ./gradlew :calendarviewsample:assembleDebug
bash ./gradlew test
bash ./gradlew :calendarviewsample:lintDebug :library:lintDebug
```

Manual checks must include all calendars, modes, locales, year boundaries, range selection,
dialogs, rotation/recreation, dark theme, RTL, heatmap scrolling and intensity/day feedback.

## Remaining work

- Run builds/tests and add Compose UI/restoration tests.
- Add list/pager/navigation APIs, adjacent-month cells and remaining customization hooks.
- Migrate remaining legacy calendar demo features, then remove obsolete views/layouts.
- Remove Jalali dependency from shared core after migrating affected APIs.
- Consolidate coordinates in version catalog and verify versions/toolchain compatibility.
- Update README, architecture, setup, agent rules and release metadata for 2.0.0.

This is an incremental migration, not a completed, verified or published v2 release.
