# Version 2 implementation progress

Date: 2026-11-09
Branch: update
Architecture: decisions/0002-compose-v2-shared-core.md

## Implemented steps

1. UI-independent CalendarSelection for none/single/multiple/range; 11 JUnit tests.
2. Shared Compose selection state, saveable ownership and native CalendarMonthGrid.
3. Gregorian month builder and GregorianCalendarMonth; five data tests.
4. Hijri month builder and HijriCalendarMonth; five data tests.
5. Solar Hijri month builder, solarHijriEpochDay and SolarHijriCalendarMonth; five data tests.
   The Solar Hijri module enables Compose and explicitly exports both core and JalaliCalendar.

All three calendar modules now contain native Compose entry points depending on the shared
core, not on each other. These are incremental single-month APIs, not feature-complete
replacements for the previous view library. Old views and sample wiring remain unchanged.
The core still exports the Jalali library pending legacy API cleanup; dependency isolation
is therefore not complete. Compose BOM 2026.09.00 remains in the core build file.

Calendar builders provide localized labels and epoch-day identities. Gregorian defaults
to the locale's week start; Hijri/Jalali preserve Saturday-first with explicit overrides.
Locale does not set Compose layout direction. Custom rendering uses each builder with
CalendarMonthGrid slots. Disabled dates reject direct taps but may lie in selected ranges.

Solar Hijri uses the existing conversion extension and JalaliCalendar, with round-trip and
consecutive-day checks that reject conversion inconsistencies. No new conversion algorithm
or wider supported year range is claimed. Persian month names come from JalaliCalendar;
other locales use the existing project's English transliterations, not fully translated
Jalali month names. Mutable consumer predicates receive separate JalaliCalendar instances.
Hijri follows platform chronology data, not local moon-sighting adjustments.

## Solar Hijri usage inside a composable

```kotlin
val state = rememberCalendarSelectionState(TypeSelectDay.Range)
SolarHijriCalendarMonth(
    month = JalaliCalendar(1400, 1, 1),
    selection = state.selection,
    onDateClick = { state.onDayClick(solarHijriEpochDay(it)) },
    locale = Locale.forLanguageTag("fa"),
)
```

Import JalaliCalendar from ir.huri.jcal, Locale from java.util, shared Compose state from
com.develotter.calendarview.compose, TypeSelectDay from the shared enums package and the
Solar Hijri entry points from com.develotter.calendarview.calendars.solarHijri.compose.

Callers own visible month and selection. Saved selection contains explicit epoch-day keys.
Initial dates are initializers, not controlled inputs. Large multiple selections can consume
substantial saved-instance-state space. Restoration has not been tested.

## Verification limits

Source review only. All 26 added JUnit tests are unexecuted. No Gradle compilation, lint,
emulator/device test, restoration test or manual UI/RTL/accessibility check was performed.
The GitHub connector is not a build runner. Compiler/plugin compatibility is unverified.
PROJECT_STATUS.md successful build results concern the pre-migration implementation only.

```bash
bash ./gradlew :calendar-solarHijri:compileDebugKotlin
bash ./gradlew test
bash ./gradlew :calendarviewsample:assembleDebug
bash ./gradlew :library:lintDebug :calendar-gregorian:lintDebug :calendar-hijri:lintDebug :calendar-solarHijri:lintDebug
```

## Remaining work

- Add navigation, list/pager modes, adjacent-month cells and missing customization hooks.
- Add UI/state/restoration tests; run all behavior and conversion tests.
- Migrate heatmap and sample; remove old rendering only after replacement.
- Remove Jalali dependency from shared core after migrating affected legacy APIs.
- Consolidate Compose coordinates in the catalog and verify dependency/toolchain versions.
- Update README, architecture, setup and agent rules; align release metadata to 2.0.0.
- Run builds, lint and manual UI/RTL/accessibility checks before declaring completion.

Official setup references consulted in step 2:

- https://developer.android.com/develop/ui/compose/bom
- https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler
- https://developer.android.com/build/migrate-to-built-in-kotlin

This is an incremental implementation, not a completed migration or published v2 release.
