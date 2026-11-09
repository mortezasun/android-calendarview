# Version 2 implementation progress

Date: 2026-11-09
Branch: update
Architecture: decisions/0002-compose-v2-shared-core.md

## Implemented steps

1. CalendarSelection: UI-independent none/single/multiple/range selection and 11 JUnit tests.
2. Shared Compose core: CalendarSelectionState, rememberCalendarSelectionState and native
   CalendarMonthGrid with customizable header, weekday and day slots. Compose BOM 2026.09.00
   is declared in the core build file. Legacy ViewBinding remains during migration.
3. Gregorian Compose integration: gregorianMonthData builds localized labels, locale-based
   or explicit week starts, absolute-day keys and enabled flags. GregorianCalendarMonth
   connects that data to the shared native grid. Five data tests were added. The Gregorian
   module enables Compose and exports the shared core through api(project(":library")).

The Gregorian entry point is controlled: callers own the displayed YearMonth and selection.
Custom rendering can use gregorianMonthData with CalendarMonthGrid directly.
Locale controls formatting and week defaults, not the ambient Compose layout direction.
Disabled cells reject clicks but are not excluded from the interior of a selected range.
The old GregorianStatus and views remain unchanged. The sample is not yet wired to Compose.

## Usage inside a composable

```kotlin
val state = rememberCalendarSelectionState(TypeSelectDay.Range)
GregorianCalendarMonth(
    month = YearMonth.of(2026, 11),
    selection = state.selection,
    onDateClick = { state.onDayClick(it.toEpochDay()) },
    locale = Locale.GERMANY,
)
```

Imports come from java.time, java.util.Locale, the shared core's compose package,
the shared TypeSelectDay enum and calendars.gregorian.compose.GregorianCalendarMonth.
Selection is saved as explicit epoch-day keys; changing mode resets remembered selection
during composition. Initial selections are initializers, not controlled inputs. Large
multiple selections may consume substantial saved-instance-state space.

## Verification limits

Source review only. All 16 new JUnit tests are unexecuted. No Gradle compilation, lint,
emulator/device check, saved-state restoration or manual RTL/accessibility check was run.
The GitHub connector offers file operations, not a build runner. Plugin/compiler compatibility
with the existing AGP/Kotlin configuration is not established by these commits. Earlier
PROJECT_STATUS.md build results concern the pre-migration implementation only.

```bash
bash ./gradlew :calendar-gregorian:compileDebugKotlin
bash ./gradlew :library:testDebugUnitTest :calendar-gregorian:testDebugUnitTest
bash ./gradlew :calendarviewsample:assembleDebug
bash ./gradlew :library:lintDebug :calendar-gregorian:lintDebug
```

## Remaining work

- Implement Hijri and Jalali Compose entry points and verify absolute-day conversions.
- Add navigation, list/pager modes, adjacent-month cells and remaining customization hooks.
- Add grid, selection-state and saved-state restoration tests; execute behavior tests.
- Migrate heatmap and sample; remove legacy rendering after feature parity.
- Move Jalali dependency from core to its calendar module.
- Consolidate Compose dependencies in the catalog and finish version/toolchain verification.
- Update README, architecture, setup and agent rules; align release metadata to 2.0.0.
- Run builds, lint and manual UI/RTL/accessibility checks before declaring completion.

Official setup references consulted during step 2:

- https://developer.android.com/develop/ui/compose/bom
- https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler
- https://developer.android.com/build/migrate-to-built-in-kotlin

This is an incremental implementation, not a completed migration or published v2 release.
