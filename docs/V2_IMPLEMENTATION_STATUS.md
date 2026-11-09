# Version 2 implementation progress

Date: 2026-11-09
Branch: update
Architecture: decisions/0002-compose-v2-shared-core.md

## Step 1: UI-independent selection engine

CalendarSelection in :library handles none, single, multiple and range selection using
absolute-day keys, without view caches. Eleven JUnit behavior tests were added.
The implementation and tests were committed in 2cbd21aa3b78908eaf00f297b6778f7fca555fe0.
The tests have not been executed in this environment.

## Step 2: Compose foundation and native month grid

The shared :library module now enables Compose alongside legacy ViewBinding. The existing
kotlin-compose plugin alias is applied; no kotlin-android plugin is added to AGP built-in Kotlin.
Compose runtime, saveable state, UI, Foundation and Material 3 use BOM 2026.09.00.
The BOM is declared in the core build file; moving its coordinates into the version catalog
is deferred. Existing toolchain and non-Compose dependency versions are unchanged.

Official setup sources consulted on 2026-11-09:

- https://developer.android.com/develop/ui/compose/bom
- https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler
- https://developer.android.com/build/migrate-to-built-in-kotlin

CalendarSelectionState publishes each selection change via Compose mutableStateOf.
rememberCalendarSelectionState saves explicit epoch-day keys with a listSaver. The mode is
configuration supplied by the caller; changing it resets state. Initial dates are only
initializers, not controlled inputs. Large multiple selections can consume substantial saved state.

CalendarMonthGrid is a native, stateless Compose renderer for one month. It accepts localized
labels, a first-day offset, chronological day cells, selection and a click callback. Header,
weekday and day content are composable slots. Disabled cells do not dispatch clicks.
The layout follows ambient layout direction; RTL has not been manually verified.

Example wiring inside a composable (month data must come from the calendar engine):

```kotlin
val state = rememberCalendarSelectionState(TypeSelectDay.Range)
CalendarMonthGrid(
    title = title,
    weekdayLabels = weekdayLabels,
    firstDayOffset = firstDayOffset,
    days = days,
    selection = state.selection,
    onDayClick = state::onDayClick,
)
```

## Verification limits

Source review only. No Gradle compilation, JUnit execution, lint, saved-state restoration
test, emulator or device check was run: the available GitHub connector is not a build runner.
Plugin/compiler compatibility with the repository's AGP/Kotlin configuration must be checked
by a real Gradle build. Earlier successful results in PROJECT_STATUS.md are pre-migration
results, not verification of these changes.

```bash
bash ./gradlew :library:compileDebugKotlin
bash ./gradlew :library:testDebugUnitTest
bash ./gradlew :calendarviewsample:assembleDebug
bash ./gradlew :library:lintDebug
```

## Remaining work

- Connect and test Gregorian, Hijri and Jalali engine data with the Compose core.
- Add grid and saved-state restoration tests and run all existing behavior tests.
- Implement list/pager navigation, adjacent-month cells and remaining customization hooks.
- Migrate heatmap and the complete sample; remove old rendering only after replacement.
- Move the Jalali dependency out of the shared core into its calendar module.
- Finish dependency audit, catalog consolidation and actual toolchain compatibility checks.
- Align release metadata to 2.0.0 and update README, architecture, setup and agent rules.
- Run builds, lint and manual UI, accessibility and RTL checks.

This is an incremental implementation, not a completed Compose migration or a v2 release.
