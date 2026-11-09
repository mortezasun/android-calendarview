# Version 2 implementation progress

Date: 2026-11-09
Branch: update
Architecture: decisions/0002-compose-v2-shared-core.md

## Implemented steps

1. UI-independent CalendarSelection; 11 JUnit behavior tests.
2. Compose selection owner with saveable state and native customizable CalendarMonthGrid.
3. Gregorian data builder and single-month Compose entry point; five data tests.
4. Hijri data builder and single-month Compose entry point; five data tests.
5. Solar Hijri data builder, checked epoch conversion and Compose entry point; five tests.
6. Launcher replaced by a Compose sample with calendar switching, previous/next/today
   navigation, all selection modes, clearing, locale selection (en/de/fa/ar), explicit RTL
   switch, ISO selection summary and a native Compose calendar dialog. Navigation logic
   has four unit tests and uses real calendar months rather than fixed day offsets.

## Sample behavior

The existing manifest still launches LauncherActivity, now an AppCompatActivity hosting
a ComposeView with lifecycle-bound composition disposal. No activity-compose dependency
was added. The app enables Compose and directly declares Foundation and Material 3, using
the shared core's transitive BOM constraint. ViewBinding remains for legacy screens.
Release metadata is unchanged; this is not version 2.0.0 or a published release.

Calendar type, selection mode, locale tag, RTL flag, anchor day and dialog visibility use
rememberSaveable. Selection uses the shared saveable owner. Changing calendar preserves
the absolute anchor and selected dates; changing selection mode clears selection during
composition. Each engine displays the month containing the anchor. Previous/next normalize
to the destination month's first day. Navigation range failures leave the current anchor
unchanged and show an error. Today reads the current local date when tapped.

Main screen and dialog intentionally edit the same selection; dismissing the dialog does
not cancel changes. The summary shows ISO dates (range endpoints only), clearly labeled.
Controls remain English demonstration labels; calendar labels follow selected locale. RTL
is controlled independently. System light/dark theme is used, and content applies safe
drawing insets and vertical scrolling. Saved-state restoration has not been verified.

Legacy calendar and heatmap remain accessible through buttons. Their sources and XML
are retained; this step is not a full migration of those demos or the heatmap. The obsolete
CustomCalendarViewActivity manifest declaration remains a documented pre-existing issue.
All three modules depend on the shared core, not one another, but the core still exports
the Jalali dependency pending legacy cleanup. BOM 2026.09.00 remains in the core build file.

## Calendar limitations

Single-month APIs do not yet implement list/pager modes, adjacent-month cells, or all old
adapter features. Disabled dates reject clicks but can lie in range interiors. Hijri uses
platform chronology data, not moon sighting. Jalali conversion checks reject inconsistent
round trips. Persian month names come from JalaliCalendar; other locales use the project's
English transliterations. Calendar conversion errors during rendering are not swallowed;
only navigation actions display range errors.

## Verification

Source review only. All 30 added JUnit tests are unexecuted. No Gradle compilation, lint,
emulator/device run, UI test or saved-state/RTL/accessibility check was performed. The
GitHub connector is not a build runner. Plugin/compiler compatibility is unverified.
PROJECT_STATUS.md successful results apply only to pre-migration code.

```bash
bash ./gradlew :calendarviewsample:assembleDebug
bash ./gradlew test
bash ./gradlew :calendarviewsample:lintDebug :library:lintDebug
```

Manual checks: launch, switch all calendars/locales/modes, navigate year boundaries, select
a cross-month range, clear selection, edit in dialog, dismiss/reopen, rotate/recreate,
check system dark theme and RTL, and open both legacy demos.

## Remaining work

- Run actual builds/tests; add Compose UI and saved-state restoration tests.
- Add library list/pager/navigation APIs, adjacent-month cells and missing customization.
- Migrate heatmap and remaining legacy demo features; then remove old views/layouts.
- Remove Jalali dependency from core after migrating affected legacy APIs.
- Consolidate Compose coordinates and verify dependency/toolchain versions.
- Update README, architecture, setup, agent rules and release metadata to 2.0.0.

This remains an incremental migration, not a completed or verified v2 release.
