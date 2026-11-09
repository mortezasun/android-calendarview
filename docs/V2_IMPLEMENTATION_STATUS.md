# Version 2 implementation progress

Date: 2026-11-09
Branch: update
Architecture: decisions/0002-compose-v2-shared-core.md

## Step 1: UI-independent selection engine

Added CalendarSelection in the shared :library module and 11 JUnit behavior tests.
The engine uses the existing TypeSelectDay enum and introduces no dependencies.
It stores copied, sorted absolute-day keys, with only endpoints retained for a range.
The existing MonthSampleAdapter was inspected to preserve its tap transitions.
Selection membership no longer needs a rendered view or cached binding.

The engine is an immutable-by-API value holder, not observable Compose state.
A future Compose owner must publish each returned instance through observable state.
Calendar modules must convert their dates to epoch days before using this API.
The new engine is not yet wired into existing views or the sample.

## Checks

Source review only. No Gradle build, JUnit execution, lint, emulator or device check was run
in this session: the available GitHub connector provides file operations, not a build runner.
The tests are added but unverified. Prior successful results in PROJECT_STATUS.md describe
the pre-migration code and must not be treated as verification of this change.

Suggested verification in an Android development checkout:

```bash
bash ./gradlew :library:testDebugUnitTest
bash ./gradlew :calendarviewsample:assembleDebug
bash ./gradlew :library:lintDebug
```

## Remaining work

- Audit calendar contracts, conversions and all public customization hooks.
- Verify stable Compose/AndroidX versions and compatible Kotlin/AGP plugin configuration.
- Add observable Compose state ownership and restoration.
- Implement native calendar grid, list/pager navigation and composable customization slots.
- Connect the three independently consumable calendar modules to the shared Compose core.
- Migrate heatmap and the complete sample application.
- Replace obsolete XML/ViewBinding rendering only after feature parity is established.
- Align version metadata to 2.0.0 and update README, architecture, setup and agent rules.
- Run build, behavior tests, lint and manual UI/RTL checks before declaring v2 complete.

This step is implementation groundwork, not a completed Compose migration or a v2 release.
