# Release plan 2.4.0: native Chinese calendar

Date: 2026-11-13. Target branch: `update`. Status: implementation and review follow-ups are
locally verified; remote CI for the evidence tree `1ab841e` and the fix commit `e80c00f` is
green. The follow-ups based on `6fac725` are locally verified as `5cf640d`; pushed tree
`952bbb6` is green in all four remote workflows. The review findings against `54bf23d` are
fixed locally, published and checked with isolated consumers. The follow-up block is
`09e3a73` on `update`; all four remote workflows succeeded. The publishing URL follow-up based
on `acf3bfc` is locally verified. The next block is `9fd3b50` on `update`; all four remote
workflows succeeded. The follow-up based on `9fd3b50` is pushed as `a138306` and green in all
four remote workflows. The verified release source HEAD is
`4dc484ae8cff91bad0869cd8d24e0b71144ff8e9`; verification `43528254651`, publishing
`43528254527`, instrumentation `43528254722` and tablet `43528254471` all succeeded. JitPack is
verified for `2.3.0` and commit `7a51e24`; tag `2.4.0` now exists, and the JitPack tag/BOM
consumer verification is pending. Benchmarks and the advisory scan were executed. A software-emulated API 29 phone smoke test
reached the sample's leap-month details; the real-device/fold/TalkBack/large-font/RTL matrix
remains open. The maintainer approved the 2.4.0 release on 2026-11-14; tag and GitHub Release are
created by the automated workflow with these gates documented as open.

Verified branch base: `a9642f13d596fd35137945bc234dbf2827e6e398`. Last GitHub API-confirmed
release: `2.3.0`, tag commit `8b7bea42e888785752fff199b2d53c04ab9888e4`, published on
2026-11-13. Do not adopt differing historical dates without verification.

## Binding assignment

Build a self-written Chinese calendar system as an additional, optionally includable library
family. No foreign calendar engine, no ICU wrapper and no porting of foreign calendar code.
Calculation in pure Kotlin; presentation in Compose without XML, WebView or JNI. Independent of
the Gregorian, Hijri and Solar Hijri modules. The existing shared Compose core may be used for
presentation; the engine must be usable without that core.

Existing code and releases remain unchanged. Checkboxes are checked only with actual evidence;
open points remain explicitly open.

## Documents and reading order

1. This plan: scope, order and work assignments.
2. [ADR 0005](docs/decisions/0005-independent-native-chinese-calendar.md): independence and
   module boundaries.
3. [API and calendar contract](docs/CHINESE_CALENDAR_SPEC.md): data model, time convention,
   bounds.
4. [Tests and risks](docs/CHINESE_CALENDAR_QUALITY.md): test matrix, vulnerabilities and
   security gates.
5. [Release 2.4.0](docs/releases/2.4.0.md): integration inventory, approval and evidence.
6. Existing `docs/RELEASE_CHECKLIST.md`, `docs/PROJECT_STATUS.md` and the 2.3.0 release report
   as historical baseline, not as test evidence for 2.4.0.

## Architecture and deliverables

- `:calendar-chinese-native`: separately publishable Kotlin Multiplatform engine; Kotlin standard
  library only, with no Compose, Android, Java date or foreign calendar dependencies in common
  code.
- `:calendar-chinese-compose`: separately publishable Compose facade; depends only on the new
  engine, `:library` and already approved Compose/platform building blocks. The existing Android
  API and shared renderer are reused.
- Existing calendars must not gain a dependency on either new module.
- KMP/Kotlin-Native evidence applies to the engine in 2.4.0. The Compose facade remains Android
  while `:library` is Android-specific. Do not advertise an iOS Compose library without actual
  migration and consumer tests. A complete migration of existing modules is not part of this
  additive release.
- Target scope: bidirectional date conversion, leap months, validated date values, day and month
  navigation, month/week data, Compose month view, saveable state, agenda/pager/heatmap
  integration and a playground.
- Not included: BaZi, horoscopes, official workdays/compensatory days, complete long-range
  astronomical calculation and exact solar-term times.

## Implementation: complete one phase at a time

### P0 - Baseline and build feasibility

- [x] Check working tree and branch; preserve foreign local changes. No force pushes or automatic
  merges. (Branch `update`; independent `artifacts/` files remain untracked.)
- [x] Read README, AGENTS, architecture, development rules and current status fully; inventory
  build scripts and publication workflows.
- [x] Run existing tests, lint, API guard and sample build as baseline; document existing failures
  separately. (Baseline `test` green; no existing failures found.)
- [x] Determine exact Kotlin/AGP/Gradle/Compose versions from the repository; verify a compatible
  KMP plugin/Android target configuration in a small build prototype. Do not blindly upgrade to
  supposedly latest versions. (Kotlin 2.4.21, AGP 9.5.1, Gradle 9.8.2, Compose BOM 2026.09.00;
  KMP plugin without its own version because AGP already provides Kotlin.)
- [x] Engine targets: Android-compatible artifact and at least one executed Kotlin/Native target;
  verify iOS device/simulator artifacts and a simulator consumer on macOS before promising iOS
  support. Document exact targets and test tasks after the build prototype. (`jvm()` consumed by
  `:calendar-chinese-compose`; `linuxX64Test` 14 tests. iOS not declared.)
- [x] Complete the open JitPack 2.3.0 check or document cause and impact on 2.4.0. An HTTP 500
  probe is not a confirmed build failure and not a consumer proof. (2.3.0 build log HTTP 200 with
  all five modules and BOM; JitPack-only Android BOM consumer with `library` compiles:
  `BUILD SUCCESSFUL`; 2.4.0 tag remains separately blocked, see gate addendum.)

### P1 - Data contract and provenance

- [x] Finalize the CHINESE_CALENDAR_SPEC contract and review the API design.
- [x] Clarify reference data, reuse rights and required notices. Do not copy foreign bitmask
  tables or source text. (HKO IP note: no data incorporated; generator uses published
  astronomical series, VSOP87D input not redistributed.)
- [x] Create deterministic import/generation tools and a data manifest. Reference retrieval is a
  development process, never a runtime library function. (`tools/chinese-calendar/generate.py`,
  `manifest.json`, output SHA-256 documented.)
- [x] Determine the exact supported epoch-day range and required boundary months from checked
  data. Target is Gregorian 1901-2100; no unproven promise of complete Chinese years at the
  bounds. (Chinese years 1901..2099 = 1901-02-19..2100-02-08, 72,674 days.)
- [x] Gate: no engine release with unresolved data license, unknown provenance or unexplained
  reference differences. (2,458/2,461 HKO month starts exact; three differences documented with
  JDE/TT, Delta-T, UTC/UTC+8, NASA reference and reproducible audit in
  `reference-conflicts.json`; `--verify-reference` checks complete coverage.)

### P2 - Independent Kotlin engine

- [x] Create a new engine module with real implementations, no TODO stubs.
- [x] Implement validated immutable values, conversion, month lists, leap months, arithmetic and
  explicit range errors.
- [x] Keep tables and indexes immutable; day calculation uses integer epoch days only.
- [x] Pass complete range tests and independent reference comparison; run the same fixtures on JVM
  and Kotlin/Native. (72,674-day round trip, 2,461 gapless months, generator reference
  comparison; `jvmTest` and `linuxX64Test` 14 tests each.)
- [x] Gate: engine is usable without Android SDK, Compose or other calendar modules as an
  independent consumer. (JVM consumer with `javac`, only engine jar + `kotlin-stdlib`; no
  AndroidX/Compose/Android classes in the jar.)

### P3 - Compose integration

- [x] Implement the Compose module, MonthData, WeekData, state/saver and calendar composable.
- [x] Reuse the existing renderer, selection and epoch-day markers; no second selection engine.
- [x] Distinguish regular and leap months in navigation, titles, keys, saver and heatmap year
  lists.
- [x] Treat adjacent-month cells at data-range bounds as empty/disabled; no unverified conversion
  outside the range.
- [x] Deliberately align all existing display options; document exceptions instead of claiming
  parity without proof. (Deliberate differences: `todayEpochDay` instead of `LocalDate`,
  `ChineseTextStyle` instead of `dateProvider`; header/weekday/adjacent/fixed rows/markers/week
  number present.)

### P4 - Sample and playground

- [x] Integrate the Chinese calendar as its own selection. Offer a Chinese month and a Gregorian
  grid with Chinese secondary labels.
- [x] Group controls: calendar, date/navigation, selection, presentation, bounds and details.
  Details show epoch day, Gregorian date, Chinese date and leap-month status.
- [x] Provide a deterministic leap-month showcase entry that opens the native engine/Compose
  facade with Chinese details visible. (`showChineseDemo`; JVM regression test and debug
  sample build.)
- [ ] Test phone, tablet, multiple columns, split-screen and fold transitions with the existing
  adaptive architecture. (Local API 29 software emulator renders the sample and the Chinese
  leap-month details; tablet/split-screen/fold transitions were not executed locally, and the
  remote instrumentation/tablet suites are not a real device/fold matrix.)
- [ ] Preserve selection and visible month through rotation, state restoration and calendar
  switching. A bounded display month must not silently alter the global selection. (Saver/navigation
  logic covered by unit tests and the existing restoration suite; real activity recreation with
  Chinese was not executed separately.)

### P5 - Hardening and documentation synchronization

- [ ] Execute the complete CHINESE_CALENDAR_QUALITY matrix and close findings. (Functional core
  matrix, regression and remote suite executed; benchmarks and advisory scan added with real
  results; Chinese device/fold/TalkBack/large-font/RTL matrix and physical benchmark acceptance
  remain open.)
- [x] Synchronize README, ARCHITECTURE, DEVELOPMENT, SETUP, CI, PROJECT_STATUS, RELEASE_CHECKLIST,
  CHANGELOG and AGENTS with the actually implemented state. Preserve existing historical
  evidence.
- [x] Add the missing API dump and consumer examples; check license/NOTICE and publication
  metadata.
- [x] Compare existing API dumps against 2.3.0; new modules must not introduce hidden breaking
  changes. (Five existing dumps `0` diff; two new dumps.)

### P6 - Release candidate and 2.4.0

- [x] Prepare versions synchronously as 2.4.0 only after green product gates; sample versionCode
  20400 only after the monotonicity check, versionName 2.4.0. (20300 -> 20400.)
- [ ] Collect RC evidence for an exact commit; execute all publication and consumer checks from
  the release plan. (Evidence tree `1ab841e70b6fb7ab35b16e970715b500b91f95a2`, review-fix state
  `e80c00f` and follow-up tree `952bbb6` on `origin/update`; isolated publication, two consumers
  and all four remote workflows passed. JitPack commit build `7a51e24` and direct engine
  consumer passed; tag `2.4.0` was created on 2026-11-14, and the JitPack tag/BOM consumer still
  needs verification, see gate addendum.
  Verified release source HEAD `4dc484a` is green in all four workflows: verification `43528254651`, publishing
  `43528254527`, instrumentation `43528254722`, tablet `43528254471`.)
- [x] Obtain separate explicit approval for merge, tag and GitHub Release. Approved 2026-11-14
  with the open device/benchmark/advisory gates explicitly documented; this does not mark those
  gates complete.
- [x] Tag `2.4.0` and GitHub Release created by release workflow `43537597202` at
  `291275b9e01da11a9b009f9ad77f05a5ef89a05e` on 2026-11-14.
- [ ] Verify actual remote JitPack artifacts/variants and independent consumers. Only then claim
  successful distribution.

### Review follow-ups 2026-11-13 (base `6970b92`)

| Finding | Fix (file) | Regression test | Executed check |
|---|---|---|---|
| P1 today navigation | `calendar-chinese-compose/.../ChineseMonthState.kt`: `canGoToday` and `goToToday` both check `minMonth..maxMonth`; outside the bounds the month remains unchanged. | `ChineseComposeDataTest.monthStateTodayNavigationHonoursConfiguredBounds`; existing leap/bounds test corrected. | `:calendar-chinese-compose:testDebugUnitTest` and `:calendarviewsample:testDebugUnitTest` green. |
| P1 reference differences | `tools/chinese-calendar/generate.py`: reference files, year/leap status, complete coverage and `--verify-reference`; `reference-conflicts.json` with JDE/TT, Delta-T, UTC, UTC+8, structured NASA/GSFC time and tolerance; `manifest.json` regenerated. No table correction. | `tools/chinese-calendar/test_generate.py` (missing files/starts, year/leap mismatch, conflict audit, verify failure status, time-chain/tolerance check); `ChineseCalendarEngineTest.documentedHkoConflictsRemainOnTheUtcPlus8CivilDate`. | `test_generate.py`: 7 tests, 0 failures. Generator `--verify-reference`: 2,458 exact + 3 audited = 2,461, 0 missing references, `complete=True`, exit 0. Manifest output/coverage and the SHA-256 of the checked Kotlin data match the manifest. |
| P2 overlay navigation | `CalendarSampleMonthNavigation.kt`, `CalendarSampleViewport.kt`, `CalendarSampleScreen.kt`, `CalendarSampleNavigation.kt`: Gregorian month anchors, header and pager/list identities for `chineseGregorianGrid`. | `CalendarSampleNavigationTest.chineseOverlayUsesGregorianMonthStepsAndIdentities`; `ChineseComposeRegressionTest.gregorianOverlayHeaderAndPagerStepGregorianMonths`. | Sample JVM tests green; `compileDebugAndroidTestKotlin` green; remote instrumentation of the core/sample suite on API 29 successful. No separate Chinese device matrix. |
| P2 agenda | `CalendarSampleScreen.kt`: `chineseStyle` is a `remember` key. | `CalendarSampleMonthDataTest.chineseAgendaLabelsFollowTheSelectedStyleWithoutChangingTheDate`; `ChineseComposeRegressionTest.agendaRebuildsLabelsWhenTheStyleChangesWithoutADateChange`. | Sample JVM tests and AndroidTest compilation green; remote suite successful. |
| P2 today callback | `ChineseMonthState.kt`: `rememberUpdatedState` for state and saver; `ChineseMonthSnapshot.decode` rejects flags outside `0/1`. | `ChineseComposeDataTest.monthStateRejectsNonBinaryLeapFlagsWhenRestoring`; `ChineseComposeRegressionTest.chineseMonthStateSurvivesRestorationAndUsesAReplacedTodayCallback`. | Compose JVM tests green; AndroidTest compilation green; remote suite successful. |
| P2 calendar switch | `CalendarSampleState.kt`, `CalendarSampleMonthNavigation.kt`: anchor fallback at both bounds with a message; selection unchanged; header clamps invalid anchors. | `CalendarSampleStateTest.switchingToChineseClampsUnsupportedAnchorsAtBothEnds`; `ChineseComposeRegressionTest.unsupportedAnchorFallbackKeepsTheSharedSelection`. | Sample JVM tests green; AndroidTest compilation green; remote suite successful. |

- [x] Release documentation: the earlier contradiction between a missing remote run and successful
  runs is removed; the named runs `1ab841e` and `e80c00f` belong to actually pushed commits. The
  documentation completion `2491c09` is also on `update`; its four remote workflows succeeded
  (verification `43414411458`, publishing `43414411492`, instrumentation `43414411441`, tablet
  `43414411446`).
- [x] Remote CI for fix commit `e80c00f` on `update`: verification `43412948314`, publishing
  `43412948284`, instrumentation `43412948316`, tablet `43412948286` - all successful.

### Review follow-ups 2026-11-13 (base `6fac725`)

| Finding | Fix (file) | Regression test | Executed check |
|---|---|---|---|
| P1 window at engine bounds | `calendarviewsample/.../CalendarSampleWindow.kt`: center validated; at a finite bound only the invalid window side is omitted, the boundary month remains the first/last entry. | `CalendarSampleWindowTest.chineseWindowIsTruncatedAtBothEngineBounds`; `ChineseComposeRegressionTest.chineseBoundaryWindowRendersAndNavigatesInSingleMode` and `...InPager`. | Sample JVM 60 tests, 0 failures; AndroidTest compiled; new UI tests not executed locally (no emulator); remote instrumentation `43439808384` successful, no separate Chinese device matrix. |
| P2 time-chain/reference audit | `tools/chinese-calendar/generate.py`: structured NASA reference, explicit UTC/UTC+8 offsets, equality of both instants and documented 60 s tolerance. | `test_conflict_instants_must_agree_and_use_structured_reference_tolerance` (manipulated times, missing time zones, tolerance violation). | `test_generate.py`: 7 tests, 0 failures; `--verify-reference`: 2,458 + 3 = 2,461, 0 missing, exit 0. |
| P2 NASA value/HKO explanation | `reference-conflicts.json`: 1914-11-17 corrected to 16:02 UT; 1916/1920 confirmed at 16:05 UT; the three HKO rows documented as a deliberate UTC+8 product decision (Beijing local time explains them, but a blanket LMT switch also moves 1906-04-24). Manifest, specification, quality and release documentation synchronized. | `test_1914_reference_uses_the_actual_nasa_minute_and_records_hko_difference`. | Generator/manifest check and Python tests green; remote CI `43439808516`/`43439808420` successful. |
| P2 today callback | No production code needed; the test exchanges two real `FixedToday` callback instances after restore. | `ChineseComposeRegressionTest.chineseMonthStateSurvivesRestorationAndUsesAReplacedTodayCallback`. | AndroidTest compiled; device execution not possible locally; remote instrumentation `43439808384` successful, no separate Chinese device matrix. |

These follow-ups are locally tested. Remote CI for `952bbb6` (contains `5cf640d`): verification
`43439808516`, publishing `43439808420`, instrumentation `43439808384`, tablet `43439808435` -
all successful. Device, fold, TalkBack, benchmark, advisory and JitPack gates remain open.

### Review follow-ups 2026-11-13 (base `54bf23d`)

| Finding | Fix (file) | Regression test | Executed check |
|---|---|---|---|
| P1 viewport recovery | `calendarviewsample/.../CalendarSampleViewport.kt`: a `LaunchedEffect` resets the center to `desired` when the window failed and `desired` is valid; invalid `desired` remains in the error branch. | `ChineseComposeRegressionTest.invalidCenterRecoversWhenAValidAnchorArrivesWithoutCompositionRestart` (invalid center, then valid anchor without a composition restart) and `switchingFromGregorianOverlayToChineseMonthsRebuildsTheWindow`. | `compileDebugKotlin`, `compileDebugAndroidTestKotlin` and `testDebugUnitTest` green; UI tests not locally executable (no emulator); remote instrumentation open after push. |
| P2 README/consumer | `README.md`: `ChineseDate.of(2024, monthNumber = 1, isLeapMonth = false, dayOfMonth = 1)`. New isolated JVM consumer under `tools/consumer-check/jvm-consumer` compiles and runs exactly this example. | JVM consumer `Main.kt` with assertions for epoch 19763, round trip, 13 months and `2023-2L`. | `:jvm-consumer:run` with `/tmp/m2-review-final` as the only calendar repository source: `JVM consumer OK`, exit 0. |
| P2 publishing CI | `scripts/verify-publication.sh`: exact AAR/JAR/Klib/POM/module/sources paths, ZIP/class/Klib contents, all `files`/`available-at` references of `.module` files and POM/BOM dependencies. Missing target binaries and broken metadata references fail. Workflow also runs the check and two isolated consumers. The later URL follow-up based on `acf3bfc` resolves `files[].url` against the `.module` file and checks `name` separately. | Negative tests: deleted JVM jar, manipulated Klib reference, missing `url` and broken `url` with an existing `name` file each exit 1. Positive test: complete local publication exits 0. | `scripts/verify-publication.sh /tmp/m2-review-final 2.4.0` passed; Android consumer `:android-consumer:compileDebugKotlin` and JVM consumer `:jvm-consumer:run` from `tools/consumer-check` successful. |
| P2 API guard | `scripts/api-check.sh`: version from `build.gradle.kts`, exact path `calendar-chinese-native-jvm-${VERSION}.jar`, exactly one non-sources JVM jar; differing/stale jars are an error. | Manual negative test with an additional `-jvm-2.3.0.jar`: exit 1 with the list of found jars; removed afterwards. | `bash scripts/api-check.sh` after removing the stale jar: `Public API matches the checked-in dumps`, exit 0. |
| Additional publication metadata finding | `calendar-chinese-native/build.gradle.kts`: root metadata jar, root sources jar and linuxX64 Klib are normalized to the file names referenced in Gradle module metadata. Previously the `.module` files referenced `calendar-chinese-native-metadata-*.jar`/`-kotlin-*-sources.jar` and `...linuxX64Main-*.klib`, which did not exist under those names in the repository. | `scripts/verify-publication.sh` checks every metadata reference; the Android/JVM consumer resolves the variants from the published repository. | Isolated publication and both consumers successful; the check fails with a manipulated reference. |

Local regression: `test :calendar-chinese-native:jvmTest :calendar-chinese-native:linuxX64Test`
and `:calendarviewsample:assembleDebug` BUILD SUCCESSFUL; 193 Android JVM + 14 KMP JVM tests and
14 linuxX64 tests without failures. The two new Compose tests are compiled but not executed
without a local emulator. Remote CI for `09e3a73`: verification `43447073439`, publishing
`43447073475`, instrumentation `43447073390`, tablet `43447073539` - all successful.

### Review follow-up 2026-11-13 (base `acf3bfc`)

| Finding | Fix (file) | Regression test | Executed check |
|---|---|---|---|
| P1 publishing verification gap | `scripts/publication_metadata.py` (importable check) and `scripts/verify-publication.sh`: `files[].url` is mandatory, validated with `urlsplit` and resolved relatively against the `.module` file; the target must exist and be non-empty. `files[].name` is checked separately and never used as a path. `available-at.url` uses the same check. Non-local URLs are rejected as unverifiable. | `scripts/test_publication_metadata.py`: valid relative URL with a differing `name`, missing URL, broken URL with an existing `name` file and missing name. Additional integration tests against a copied complete publication with removed/broken URL. | `python3 -m unittest discover -s scripts -p 'test_*.py'`: 12 tests green; verifier against `/tmp/m2-review-final`: exit 0; both manipulated modules: exit 1 with `missing url` or `missing/empty`. |

Platform coverage of this evidence: the JVM consumer resolves the root module metadata and the JVM
variant and executes `ChineseDate.of`. The Android consumer compiles the Compose facade and its
published AAR dependencies. The linuxX64 Klib is checked at file/archive and metadata level but is
**not consumed by a Kotlin/Native consumer** in this run. This check is not JitPack or remote
consumer evidence.

Remote CI for `9fd3b50`: verification `43459739249`, publishing `43459739473`, instrumentation
`43459739435`, tablet `43459739230` - all successful.

### Follow-up 2026-11-13 (base `9fd3b50`)

| Item | Implementation | Evidence |
|---|---|---|
| HKO data convention | Deliberate UT+8 product decision: the three HKO rows are consistent with Beijing local time before 1929, but a blanket LMT switch also moves 1906-04-24. `unresolved` removed; data and output hash unchanged. | Generator `--verify-reference`: 2,458 exact + 3 documented = 2,461, `complete=True`; `ChineseCalendarData.kt` byte-identical; manifest hash `2636c523...`; LMT variant audit executed locally. |
| Default month | `defaultChineseMonth` clamps the default `initialMonth` to `MIN_EPOCH_DAY..MAX_EPOCH_DAY`; a host date outside the data range shows the next supported month. | `ChineseComposeDataTest.defaultMonthIsClampedToTheSupportedDataSet` with `Long.MIN_VALUE`, `Long.MAX_VALUE` and an in-range date; focused test green, XML `failures="0"`. |
| Workflow version | `.github/workflows/android-publishing.yml` reads the version from `build.gradle.kts`, checks for empty and uses it for the verifier and native POM; no hard-coded `2.4.0`. | YAML parse OK; local version resolution `2.4.0`; `grep` finds no hard-coded version. |
| Data hash test | `test_checked_in_kotlin_source_matches_manifest_sha256` compares the SHA-256 of `ChineseCalendarData.kt` with `manifest.json`. | `python3 -m unittest discover -s tools/chinese-calendar -p 'test_*.py'`: 7 tests, 0 failures. |

Commits: `95c1cc3` (four follow-ups) and `a138306` (internal helper filtered from the JVM API
dump). The first verification run for `95c1cc3` failed at the API guard because the internal
top-level helper was visible as a JVM method; the API dump remained unchanged. Remote CI for
`a138306`: verification `43472900415`, publishing `43472900052`, instrumentation `43472901643`,
tablet `43472900502` - all successful.

#### Remaining open gates

- [ ] JitPack build `2.4.0` and JitPack-only consumer without `mavenLocal`/`includeBuild`. Partial
  evidence: 2.3.0 and commit `7a51e24` passed; tag `2.4.0` now exists, but the JitPack build and
  tag/BOM consumer verification remain pending (see below).
- [ ] Chinese device/fold/TalkBack/large-font/RTL matrix. Remote instrumentation on API 29 is not
  a substitute for real devices/folds. A software-accelerated local API 29 phone booted and the
  sample reached the leap-month details; fold, TalkBack, large-font and full RTL checks remain
  unexecuted.
- [ ] Benchmarks: JVM baseline and provisional budgets collected; physical device acceptance and
  final budget approval remain open.
- [ ] Advisory/CVE scan: runtime graph clean; known buildscript findings need maintainer
  acceptance or an AGP upgrade.
- [ ] Merge, tag `2.4.0` and GitHub Release only after separate explicit approval; no tag or
  publication until then.

### Release gate addendum 2026-11-13

#### JitPack

| Item | Result |
|---|---|
| JitPack `2.3.0` | Build log HTTP 200; lists `library`, `calendar-gregorian`, `calendar-hijri`, `calendar-solarHijri`, `calendar-solarHijri-native` and `calendar-bom`. A temporary Android consumer with BOM `2.3.0` and `library` compiled JitPack-only without `mavenLocal`/`includeBuild`: `BUILD SUCCESSFUL in 54s`, `:android-consumer:compileDebugKotlin` exit 0. |
| JitPack `2.4.0` tag | Tag `2.4.0` exists at `291275b9e01da11a9b009f9ad77f05a5ef89a05e` (published 2026-11-14). Immediately after publication the JitPack build-log endpoint returned HTTP 404 with `Tag or commit not found: 2.4.0`; the asynchronous build and BOM consumer verification remain pending. |
| JitPack commit `7a51e24` | Commit build successful; build log lists BOM, `calendar-chinese-native` (metadata, JVM, linuxX64), `calendar-chinese-compose` and the five existing modules. Direct JVM engine consumer against `calendar-chinese-native:7a51e24` ran without BOM: `JVM consumer OK: newYear=2024-1-1 epochDay=19763 months=13`, exit 0. |
| JitPack BOM at commit hash | **Blocked:** the JitPack-published BOM POM `7a51e24` has `<version>7a51e24</version>` but still constrains the modules to `2.4.0`. JitPack rewrites its own POM version for commit builds, not the `dependencyManagement` values. The BOM consumer therefore fails with `Could not find ...calendar-chinese-native:2.4.0`; this is not a local repository or cache error. With a real `2.4.0` tag, constraint and JitPack version match. |

Reproducing the JitPack evidence:

```bash
curl -fsSL https://jitpack.io/com/github/mortezasun/android-calendarview/2.3.0/build.log
curl -fsSL https://jitpack.io/com/github/mortezasun/android-calendarview/7a51e24/build.log
curl -sS -o /dev/null -w '%{http_code}\n' https://jitpack.io/com/github/mortezasun/android-calendarview/2.4.0/build.log

# 2.3.0: temporary copy of tools/consumer-check, only google()/mavenCentral()/jitpack.io,
# BOM 2.3.0 + library, then:
./gradlew -p /tmp/jitpack-230-consumer :android-consumer:compileDebugKotlin --refresh-dependencies

# 2.4.0 commit: remove the BOM and request calendar-chinese-native:7a51e24 directly, then:
./gradlew -p /tmp/jitpack-240-engine :jvm-consumer:run --refresh-dependencies

# After separate tag/release approval: create tag 2.4.0, wait for the JitPack build and run
# tools/consumer-check against the JitPack repository (without mavenLocal/includeBuild).
```

#### Benchmarks

The test `ChineseJvmBenchmarkTest.reportJvmPerformanceBaseline` is part of the JVM tests and
prints reproducible `BENCHMARK` lines; it deliberately has no time-based assertion. Measurements
were taken on 2026-11-13 on the Freebuff cloud VM (Linux amd64, 8 vCPU, AMD-KVM, JDK 21.0.13
Temurin, max heap 512 MiB), median of five measurements after two warm-up runs. JVM allocations
come from the HotSpot thread-allocation counter.

| Operation | Measurement | Allocation | Data set per measurement |
|---|---:|---:|---:|
| Conversion epoch day -> Chinese -> epoch day | 58.77 ns/op | 56.01 B/op | 72,674 days |
| Month construction (`chineseMonthData`) | 16,074.47 ns/month | 15,630.53 B/month | 2,461 months |
| 13-month heatmap (`chineseYearHeatmap(2023)`) | 355,526 ns/year | 248,328 B/year | 13 months |
| Repeated navigation (`next`/`previous`) | 53.79 ns/step | 48.01 B/step | 100,000 steps |

Artifact sizes of the local release build: engine metadata jar 6,989 B, engine JVM jar 32,436 B,
linuxX64 Klib 41,288 B, Compose AAR 36,116 B. Provisional JVM budgets with at least 6x runtime
and allocation headroom: conversion <= 1,000 ns and <= 512 B/op; month construction <= 100,000 ns
and <= 100 KB/op; heatmap <= 5 ms and <= 1.5 MB/year; navigation <= 1,000 ns and <= 512 B/step.
These budgets are not physical device acceptance; they must be confirmed or adjusted on the named
target hardware before RC.

```bash
./gradlew :calendar-chinese-compose:testDebugUnitTest --tests '*ChineseJvmBenchmarkTest*'
```

#### Advisory/CVE scan

Scanned the resolved runtime graphs of engine (`jvmRuntimeClasspath`), Compose
(`releaseRuntimeClasspath`) and sample (`debugRuntimeClasspath`): 101 unique Maven coordinates.
The OSV `querybatch` query (POST, `https://api.osv.dev/v1/querybatch`, Maven ecosystem) returned
**0 affected runtime coordinates**.

Additionally, 334 coordinates from the runtime plus `buildEnvironment` graph were checked. Findings
are exclusively in the AGP 9.5.1 buildscript classpath, not in the shipped AARs/JARs: Guava
33.4.0-jre (`GHSA-xxph-c9ww-hj94`, fixed 33.7.2), commons-lang3 3.16.0
(`GHSA-j288-q9x7-2f5v`, fixed 3.18.0), jose4j 0.9.5 (`GHSA-3677-xxcr-wjqv`, fixed 0.9.6),
bcpkix/bcprov-jdk18on 1.80.2 (`GHSA-wg6q-6289-32hp`, `GHSA-9pwp-9qqc-pr26`,
`GHSA-qp49-qgx5-5m26`, fixed 1.84/1.85) and jdom2 2.0.6 (`GHSA-2363-cqg2-863c`, fixed 2.0.6.1).
The runtime graph does not contain these packages; `com.google.guava:listenablefuture:1.0` is only
an empty compatibility marker. A `kotlin-stdlib {strictly ...}` finding was a parser artifact
from Gradle output, not a real coordinate; resolved Kotlin is 2.4.21, above the affected ranges.
No risky forced buildscript upgrade was made for 2.4.0; the findings need documented maintainer
acceptance (owner/expiry) or a separate AGP upgrade. The runtime scan therefore passes; the
build-tooling part remains open.

```bash
./gradlew :calendar-chinese-native:dependencies --configuration jvmRuntimeClasspath \
  :calendar-chinese-compose:dependencies --configuration releaseRuntimeClasspath \
  :calendarviewsample:dependencies --configuration debugRuntimeClasspath --console=plain > /tmp/gradle-deps.log
# Then check the group:artifact:version coordinates with OSV /v1/querybatch (Maven).
```

#### Device, fold, TalkBack and emulator evidence

There is no physical device and KVM is unavailable:
`ProbeKVM: This user doesn't have permissions to use KVM (/dev/kvm); The KVM line is kvm:x:994:`.
A software-accelerated API 29 x86_64 AVD (`codebuff_api29`, 1080x1920, 420 dpi) was nevertheless
booted with `-accel off -no-window -no-audio -no-boot-anim -gpu swiftshader_indirect`. It reached
`dev.bootcomplete=1` and exposed `emulator-5554`; the debug sample installed, `LauncherActivity`
started, and the main screen rendered. Tapping the compact `CN 2.4.0` entry showed
`2023 Leap 2nd Month`; after scrolling, `Details` showed epoch day 19445, Gregorian `2023-03-29`
and `Chinese: 2023 Leap 2nd Month, day 8`. No app `FATAL EXCEPTION` appeared. Under software CPU
emulation System UI and Google Play services raised ANRs, and `am start -W` timed out at 21.9 s
although the app process stayed alive; this is not hardware or performance evidence. Screenshots
and logcat are kept locally under `artifacts/screenshots/` and `artifacts/logcat/` (untracked).

Remote instrumentation and tablet suites on API 29 are green, but replace neither real devices or
foldables nor TalkBack/large-font/RTL. The device matrix therefore remains open; existing UI tests
are compiled and executed remotely, not as a Chinese device/accessibility matrix.

```bash
adb devices
# Without KVM (slow, software CPU):
emulator -avd codebuff_api29 -accel off -no-window -no-audio -no-boot-anim \
  -gpu swiftshader_indirect -no-snapshot
adb install -r calendarviewsample/build/outputs/apk/debug/calendarviewsample-debug.apk
adb shell am start -n com.develotter.calendarviewsample/.LauncherActivity
# Instrumentation was not run on this software-emulated AVD in this session:
./gradlew :calendarviewsample:connectedDebugAndroidTest
```

Manual work remains: phone/tablet/foldable with book/tabletop posture, landscape/split-screen,
TalkBack with Chinese and English labels, large font, RTL, disabled days, visible leap-month
marker and touch-target measurement. The concrete checklist is in
`docs/CHINESE_CALENDAR_QUALITY.md`.

#### Local regression result

`./gradlew test :calendar-chinese-native:linuxX64Test` was successful; the focused benchmark test
as well. `bash scripts/api-check.sh` reports `Public API matches the checked-in dumps.`; the Python
suites ran 7 + 12 tests without failures. Evidence commit `4ce4d3f` (code and gate documentation)
is on `origin/update`; all four remote workflows succeeded: verification `43479911419`, publishing
`43479911387`, instrumentation `43479911196`, tablet `43479911275`. Verified release source HEAD `4dc484a` is
green: verification `43528254651`, publishing `43528254527`, instrumentation `43528254722`,
tablet `43528254471`. At that source-verification stage no merge, tag or release existed; the
maintainer approved the 2.4.0 release on 2026-11-14 with the open gates documented.

## Implementation work rule

Per phase: smallest coherent change, tests, diff review, documentation update and a clear report
of executed/not executed checks. Do not check a box without evidence. A missing runner, unreviewed
license or broken remote consumer is an open blocker, not a silent exception.

## Evidence

Details, commands and results:
[PROJECT_STATUS.md](docs/PROJECT_STATUS.md#current-development-240-chinese-calendar-unreleased-2026-11-13)
and [releases/2.4.0.md](docs/releases/2.4.0.md#verification-2026-11-13-local-working-tree).
