# Project status

Last updated: 2026-11-09
Branch: `update`
Last functional commit: `9a71c52` - "Update Gradle, AGP, Kotlin and AndroidX versions"
This file documents the state after the documentation/comment session that follows that commit.

## Verified state

| Item | Value |
|------|-------|
| AGP | 9.5.1 |
| Gradle wrapper | 9.8.2 |
| Kotlin (catalog) | 2.4.21; compilation via AGP 9 built-in Kotlin |
| `compileSdk` / `targetSdk` | 37 / 37 |
| `minSdk` | 27 |
| JDK used for verification | Temurin 17.0.21+9 |
| Android SDK used | Platform `android-37.0`, Build-Tools 36.0.0 |

Executed on 2026-11-09 with `bash ./gradlew ... --no-daemon`:

| Check | Command | Result |
|-------|---------|--------|
| Full build (library + all calendars + sample) | `:calendarviewsample:assembleDebug` | **BUILD SUCCESSFUL** (47 s, 167 tasks) |
| JVM unit tests | `test` | **5 tests, 0 failures, 0 errors, 0 skipped** |
| Android Lint (core library) | `:library:lintDebug` | **BUILD SUCCESSFUL**; 2 informational findings, no errors |

The APK is produced at
`calendarviewsample/build/outputs/apk/debug/calendarviewsample-debug.apk`.

The 5 passing tests are the generated `ExampleUnitTest` templates (one per module). They assert
`2 + 2 == 4` and do **not** cover calendar rendering, month arithmetic or selection behaviour.
Instrumented tests were not run (no emulator/device). The UI was not manually inspected in this
session; the checks above are compile/test/lint only.

Lint findings (informational, not fixed):

1. `CustomViewStyleable`: the custom view `MonthView` and the styleable `CustomMonthView` follow
   different naming conventions.
2. `AndroidGradlePluginVersion`: lint reports Gradle 9.9.0 as available. This was not verified
   independently and versions were intentionally not changed in this task.

## Known issues and limitations

1. **Dangling activity in the sample manifest.**
   `calendarviewsample/src/main/AndroidManifest.xml` declares `.CustomCalendarViewActivity`, but no
   such source file exists. The build and the launcher are unaffected; starting that activity would
   fail. No fix was applied because the intended screen is unclear.
2. **No meaningful test coverage.** Selection logic (`MonthSampleAdapter`), month arithmetic in the
   Hijri/Jalali modules and the manual grid layout are untested.
3. **Heatmap year view accumulates children.** `CalendarHeatmapYearSectionedView.applyHeatmapData`
   adds four new quarter views on every call and never removes previous ones. Repeated calls stack
   duplicate views. Documented, not fixed.
4. **Version references are inconsistent.** The README JitPack snippet uses `1.1.0-alpha2`, the
   sample declares `versionName = "1.1.0-alpha1"`, and the latest dependency commit is titled
   `1.1.0-alpha3`. Not changed because the actually published artifact version is unknown.
5. **Heatmap labels are not localized.** `HeatMapCalendarView` hardcodes English weekday names and
   formats months with `Locale.getDefault()`; `QuarterHeatmapView` uses `Locale.ENGLISH`.
6. **Heatmap intensity is not validated.** Values outside `0.0..1.0` are passed to
   `DayHeatMapView.getHeatColor` without clamping.
7. **`QuarterHeatmapView.monthLabelSpacingDp` is unused.** It is accepted by the constructor but not
   applied by `onMeasure`/`onLayout`.
8. **Configuration changes require a rebuild.** Mutating `CalendarStatus` after `addMonths` does not
   update existing views; callers must call `addMonths` again.

## Open tasks / sensible next steps

- Decide what should happen with `CustomCalendarViewActivity`: implement the screen, remove the
  manifest entry, or leave it documented. Requires a product decision.
- Add JVM tests for `MonthStatus.plusMonths`/`minusMonths` (especially Hijri/Jalali year wrapping)
  and for the range state machine. No new dependency is needed; plain JUnit is already present.
- Align the published version references (README, sample, tags) once the target release is known.
- Fix the heatmap child accumulation if repeated `applyHeatmapData` calls are supported.
- Consider localizing heatmap labels.
- Optionally introduce an ADR before changing the public adapter contract.

## Notes for the next session

- Read [ARCHITECTURE.md](ARCHITECTURE.md) and [DEVELOPMENT.md](DEVELOPMENT.md) before editing.
- The repository's `gradlew` may be checked out without the executable bit; use `bash ./gradlew` or
  `chmod +x gradlew` (do not commit the mode change unless requested).
- `local.properties` is not tracked; configure `ANDROID_HOME` or create it locally.
- No secrets are required for debug builds.
