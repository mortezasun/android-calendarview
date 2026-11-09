# Version 2 implementation progress

Date: 2026-11-09
Branch: update
Architecture: decisions/0002-compose-v2-shared-core.md

## Implemented steps

1. CalendarSelection: UI-independent none/single/multiple/range selection; 11 JUnit tests.
2. Shared Compose core: CalendarSelectionState, rememberCalendarSelectionState and native
   CalendarMonthGrid with header/weekday/day slots. BOM 2026.09.00 is in the core build file.
3. Gregorian: gregorianMonthData and GregorianCalendarMonth; five data tests.
4. Hijri: hijriMonthData and HijriCalendarMonth; five data tests. The module enables Compose
   and exports :library via api. Saturday-first preserves the old module default and can
   be overridden. Input dates are normalized to the start of their month without changing
   their Hijrah chronology; click callbacks preserve that chronology too.

Both calendar entry points are controlled: callers own month and selection. Directly use
the data builders with CalendarMonthGrid for custom content slots. Locale controls labels,
not ambient layout direction. Disabled days reject taps but may lie inside selected ranges.
No list/pager/navigation controls or adjacent-month cells have been implemented yet.

Hijri dates follow the platform Hijrah chronology and its supported date range; this does
not implement local moon-sighting adjustments. Gregorian and Hijri selection identities
share absolute epoch days. The old calendar classes and sample views remain unchanged.

## Usage inside a composable

```kotlin
val state = rememberCalendarSelectionState(TypeSelectDay.Range)
HijriCalendarMonth(
    month = HijrahDate.now(),
    selection = state.selection,
    onDateClick = { state.onDayClick(it.toEpochDay()) },
    locale = Locale.forLanguageTag("ar"),
)
```

Import HijrahDate from java.time.chrono, Locale from java.util, shared Compose state
from com.develotter.calendarview.compose, TypeSelectDay from the shared enums package,
and HijriCalendarMonth from com.develotter.calendarview.calendars.hijri.compose.

Selection saves explicit epoch-day keys. Initial selections are initializers, not controlled
inputs. Mode changes reset remembered selection during composition. Large multiple selections
can consume substantial saved-instance-state space. Restoration has not been tested.

## Verification limits

Source review only. All 21 added JUnit tests are unexecuted. No Gradle compilation, lint,
emulator/device test, restoration test or manual UI/RTL/accessibility check was performed.
The available GitHub connector is not a build runner. Plugin/compiler compatibility with
the existing AGP/Kotlin configuration is unverified. PROJECT_STATUS.md results describe
the pre-migration implementation, not these changes.

```bash
bash ./gradlew :calendar-hijri:compileDebugKotlin
bash ./gradlew :library:testDebugUnitTest :calendar-gregorian:testDebugUnitTest :calendar-hijri:testDebugUnitTest
bash ./gradlew :calendarviewsample:assembleDebug
bash ./gradlew :library:lintDebug :calendar-gregorian:lintDebug :calendar-hijri:lintDebug
```

## Remaining work

- Implement Jalali Compose entry point and verify absolute-day conversion.
- Add navigation, list/pager modes, adjacent-month cells and remaining customization hooks.
- Add UI/state/restoration tests and execute existing behavior tests.
- Migrate heatmap and sample; remove old rendering after replacement.
- Move Jalali dependency out of the core into its module.
- Consolidate Compose coordinates in the version catalog and verify dependency/toolchain versions.
- Update README, architecture, setup and agent rules; align release metadata to 2.0.0.
- Run builds, lint and manual UI/RTL/accessibility checks before declaring completion.

Official setup references consulted in step 2:

- https://developer.android.com/develop/ui/compose/bom
- https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler
- https://developer.android.com/build/migrate-to-built-in-kotlin

This is an incremental implementation, not a completed migration or published v2 release.
