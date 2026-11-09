# Project status

Last updated: 2026-11-09
Branch: `update`
Last functional commit: `09b4f59` - "fix(sample): remove redundant horizontalScroll on navigation buttons so vertical performScrollTo scrolls outer container"

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

Executed and verified on 2026-11-09 via GitHub Actions CI:

| Check | Scope / Command | Result |
|-------|-----------------|--------|
| Full build | `:calendarviewsample:assembleDebug` | **BUILD SUCCESSFUL** |
| JVM unit tests | `test` across all modules | **5 tests, 0 failures, 0 errors, 0 skipped** |
| Android Lint | `:library:lintDebug`, `:calendar-*:lintDebug`, `:calendarviewsample:lintDebug` | **BUILD SUCCESSFUL**; 0 errors |
| Core Compose instrumentation (API 29) | `:library:connectedDebugAndroidTest` | **5 tests, 0 failures, 0 errors** (all Compose selection tests pass) |
| Sample UI instrumentation (API 29) | `:calendarviewsample:connectedDebugAndroidTest` | **6 tests, 0 failures, 0 errors** (all 5 acceptance tests + context pass) |

Artifacts and test reports are generated and uploaded in CI:
- `android-assemble-42676869805-1`
- `android-unit-tests-42676869805-1`
- `android-lint-42676869805-1`
- `android-instrumentation-42676870113-1`

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
