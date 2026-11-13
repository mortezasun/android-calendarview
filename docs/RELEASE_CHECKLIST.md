# Release acceptance

Status: `2.4.0` is a locally verified release candidate, not tagged or published. `2.3.0` is the latest published release. `2.2.1` remains the previous published release and published on GitHub Releases. `2.2.0` remains the previous published release.

## 0. Current release candidate: 2.4.0 (unreleased, 2026-11-13)

- [x] `:calendar-chinese-native` and `:calendar-chinese-compose` implemented, integrated and documented.
- [x] Root version `2.4.0`; sample `versionCode 20400` (monotonic after `20300`) and `versionName "2.4.0"`.
- [x] BOM, `jitpack.yml`, API guard, verification/publishing workflows and sample updated for seven artifacts.
- [x] `api/calendar-chinese-native.api` and `api/calendar-chinese-compose.api` generated; existing five dumps unchanged (`0` diff).
- [x] Local JVM/Native tests, sample build, lint, release AARs, isolated publishing, POM checks, standalone JVM consumer and independent Android BOM consumer passed.
- [x] Evidence tree committed as `d5416403d2f9a77a60b30eb2bf93f6302959059b`; push to `update` in progress.
- [ ] Remote CI (verification, publishing verification, instrumentation, tablet) green for that commit.
- [ ] Remote JitPack `2.4.0` build and JitPack-only consumer without `mavenLocal`/`includeBuild`.
- [ ] Instrumentation and device/fold/TalkBack/benchmark checks listed as open in `CHINESE_CALENDAR_QUALITY.md`.
- [ ] Separate explicit approval for merge, tag `2.4.0` and GitHub Release.

## 1. Previous release: 2.3.0 (2026-11-12)

- [x] Root version set to `2.3.0` in root `build.gradle.kts`, sample `versionCode` `20300` / `versionName` `"2.3.0"`.
- [x] New `:calendar-solarHijri-native` module implemented (`SolarHijriEngine`, `SolarHijriDate`, `SolarHijriLocale`, Compose parity, boundary guards, `Clock` injection).
- [x] `:calendar-solarHijri-native` added to `settings.gradle.kts`, `calendar-bom`, `jitpack.yml`, `scripts/api-check.sh`, CI workflows, and `calendarviewsample`.
- [x] `api/calendar-solarHijri-native.api` generated; existing `api/*.api` dumps unchanged (`0` diff).
- [x] All required CI checks passed for the release tree (`8b7bea42e888785752fff199b2d53c04ab9888e4`):
  - `Android verification`: Run 43375002038 (success)
  - `Android publishing verification`: Run 43375002125 (success)
  - `Android instrumentation`: Run 43375002077 (success)
  - `Android tablet verification`: Run 43375002074 (success)
- [x] Release commit `8b7bea4` (`release: 2.3.0`) is tagged `2.3.0`; GitHub Release `2.3.0` is published via `publish-2-3-0.yml` (Run 43375002033).
- [ ] JitPack `2.3.0` build verified (all 5 modules + BOM) and consumer smoke test passed without `ir.huri:jalalicalendar` (initial JitPack probe returned HTTP 500; verify on `jitpack.io/#mortezasun/android-calendarview/2.3.0`).

## 1. Current release: 2.2.1 (2026-11-12)

- [x] Root version set to `2.2.1` in root `build.gradle.kts`.
- [x] Sample `versionCode` set to `20201` and `versionName` set to `2.2.1`.
- [x] Release commit `dd20e49` is tagged `2.2.1`; GitHub Release `2.2.1` is published via `publish-2-2-1.yml` (run 43268408129).
- [x] Minimum month width touch target enforcement (336 dp without week numbers, 376 dp with 40 dp week numbers) implemented and tested.
- [x] Adaptive pane and secondary viewport height bounds implemented and tested.
- [x] RTL and insets-aware fold geometry handling implemented and tested.
- [x] Continuous layout & fold transition tests passing.
- [x] Genuine `SavedInstanceState` restoration suite (`StateRestorationTester`) passing.
- [x] Tablet CI emulator workflow (`android-tablet.yml`, 1600x2560 240 dpi) operational and passing.
- [x] All required CI checks passed for the release tree:
  - `Android verification`: Run 43268408115 (success)
  - `Android publishing verification`: Run 43268408103 (success)
  - `Android instrumentation`: Run 43268408390 (success)
  - `Android tablet verification`: Run 43268408198 (success)
- [x] Documentation, CHANGELOG, ARCHITECTURE, README and release notes (`docs/releases/2.2.1.md`) updated.
- [x] 100% binary compatible with `2.2.0`, `2.1.0` and `2.0.1`.

## 2. Previous release: 2.2.0 (2026-11-12)

- [x] Root version set to `2.2.0` in root `build.gradle.kts`.
- [x] Sample `versionCode` set to `20200` and `versionName` set to `2.2.0`.
- [x] Release commit `f5dd967` is tagged `2.2.0`; GitHub Release `2.2.0` is published.
- [x] Adaptive multi-month columns contract and constraint tests implemented.
- [x] Two-pane adaptive layout (`CalendarSampleAdaptiveLayout`) implemented for wide screens (>= 840 dp).
- [x] Fold and posture awareness implemented via `androidx.window:window:1.5.1`.
- [x] Adaptive dialog viewport heights and adaptive heatmap demo implemented.
- [x] State preservation and recreation verified with unit and UI tests.
- [x] All required CI checks passed for the release tree:
  - `Android verification`: Run 43176664468 (success)
  - `Android publishing verification`: Run 43176664370 (success)
  - `Android instrumentation`: Run 43176664365 (success)
- [x] Documentation, CHANGELOG, ARCHITECTURE, README and release notes (`docs/releases/2.2.0.md`) updated.
- [x] 100% binary compatible with `2.1.0`.

## 2. Previous release: 2.1.0 (2026-11-11)

- [x] Root version and sample version set to `2.1.0`.
- [x] Release commit `bd046a188d9a661e657affe580c7a7fa1a3e98ba` is tagged `2.1.0`;
  the published `2.0.1` tag was not moved.
- [x] GitHub Release `2.1.0` is published.
- [x] Published `2.0.1` AARs compared with the release; all 248 public/protected JVM
  members retain identical descriptors.
- [x] Hidden delegation overloads preserve the changed selection state, month grid and
  three engine month composable signatures.
- [x] Remote verification, JVM tests, lint, publishing verification and public API guard
  passed for the release commit.
- [x] Two complete API 29 instrumentation runs passed on the identical release tree:
  `2eb0fdbc5c1ec9d5cfbeae1570eddd7e9bf37a41` (run 43081287870) and
  `bd046a188d9a661e657affe580c7a7fa1a3e98ba` (run 43082385555).
- [x] JitPack built tag `2.1.0`; its log lists AAR, POM, module metadata and sources for
  all four modules, plus the BOM POM/module metadata.
- [x] JitPack-only consumer resolved the BOM and all four constrained modules at `2.1.0`;
  `mavenLocal()` is absent from the consumer build.
- [x] README installation coordinates, CHANGELOG, PROJECT_STATUS, ARCHITECTURE, SETUP and
  [release notes](releases/2.1.0.md) updated for `2.1.0`.
- [x] GitHub Release body updated from the source release notes by the release-notes
  workflow (run `43086799069`).

## 2. Previous release: 2.0.1 (2026-11-11)

- [x] Release commit `068c57ff90bd8a7e503788a7d951dca65f8f3953` is tagged `2.0.1`.
  The published tag was not moved.
- [x] GitHub Release `2.0.1` is published.
- [x] JitPack build for `2.0.1` succeeded. Build log:
  `https://jitpack.io/com/github/mortezasun/android-calendarview/2.0.1/build.log`
  (`BUILD SUCCESSFUL in 1m`; AAR, POM, module metadata and sources listed for all four
  modules).
- [x] Consumer: Gregorian only built from JitPack.
- [x] Consumer: Hijri only built from JitPack.
- [x] Consumer: Solar Hijri only built from JitPack; `library` and
  `ir.huri:jalalicalendar:2.0.0` resolved transitively.
- [x] Consumer: all three engines built together; no dependency conflict and one shared
  `library:2.0.1`.
- [x] `mavenLocal()` excluded and `~/.m2` absent for all four builds; JitPack download
  URLs and resolved artifacts recorded.
- [x] Runtime smoke test on an API 29 emulator: all three calendars rendered, one day
  selected in each, `OK (1 test)`.
- [x] README installation coordinates, CHANGELOG and PROJECT_STATUS updated for `2.0.1`.

Residual limitation: the runtime test used a software-emulated x86_64 image and covered
one API level, default locale and one selection per calendar. It does not replace the
full manual acceptance matrix.

## 3. Historical 2.0.0 acceptance record

Checkbox states in this section are the states recorded at the end of the 2.0.0 session.
The 2.1.0 section above is the current status; the 2.0.1 section is the previous release.

### 3.1 Candidate and CI evidence

The previous version of this checklist and PROJECT_STATUS.md used the incorrect full
SHA `d552dec886daea64fe23c347b97364b63ee2d770`. The actual commit in the repository
with the short name `d552dec` is `d552decb8f7382e883e705533079664e76ab1158`.
This correction does not independently establish the head SHA of the historical CI runs;
that mapping still needs direct verification. The reference in PROJECT_STATUS.md is
historical and must not be used as an exact verification target without this correction.

PROJECT_STATUS.md records the following results for candidate `d552dec`:

- [x] Verification Run 42911012589 recorded as successful.
- [x] Instrumentation Run 42911012594 recorded as successful on API 29.
- [x] Recorded reports: 19 instrumented tests, 12 in the core and 7 in the sample.
- [x] Recorded release AAR builds for all four modules and sample debug APK.
- [x] Recorded JVM tests: 70 passed; lint: 0 errors across five modules.
- [ ] Independently verify those run head SHAs against the corrected full candidate SHA.
- [ ] Confirm verification and instrumentation outcomes for the actual release SHA
  `9e521a67fde6316e4672d239a40797a2a9609518`.

The release SHA includes a publishing configuration change after the recorded candidate.
Historical success is not automatically proof for this later configuration.

### 3.2 Publishing and JitPack (2.0.0 session)

- [x] Local publishing and independent consumer builds in four configurations are recorded
  in PROJECT_STATUS.md (Maven Local baseline only).
- [ ] Obtain the failing JitPack build log and identify the first actionable error.
- [ ] Confirm the new Android publishing verification workflow succeeds for the fix commit.
- [ ] Confirm a JitPack build succeeds for a fixed commit containing the fix.
- [ ] Build Gregorian-only, Hijri-only, Solar-Hijri-only and all-three consumer configurations
  against JitPack with Maven Local disabled and no project/includeBuild substitutions.
- [ ] Inspect resolved dependencies and the four artifacts and publication metadata.
- [ ] Record build version, commit SHA, log and consumer results.

The earlier Cloudflare/HTTP failure was a failed availability check, not a passed
JitPack preflight. At that time the user reported that JitPack was not green. The log and
status could not be retrieved during that follow-up, so a specific JitPack failure cause
is not confirmed.

The follow-up changes invoke Gradle through Bash and explicitly publish all four library
modules. This removes reliance on the wrapper executable bit; it does not prove that
permissions were the observed JitPack failure. A separate CI workflow validates the
publication tasks and generated AAR/POM/module/sources files in an isolated Maven repository.
That CI job is not a substitute for a JitPack consumer build.

The items in this subsection were closed by the 2.0.1 release section above: the tag
build log was retrieved, and all four consumer configurations were built from JitPack.

### 3.3 Manual acceptance (2.0.0)

- [x] API 29 instrumentation results are recorded for selection, navigation, dialogs,
  heatmaps, RTL and activity recreation. Exact coverage is determined by the test reports;
  a passing suite is not exhaustive verification of every locale, font scale or runtime.
- [x] Physical device testing was waived with documented residual risk in PROJECT_STATUS.md.

### 3.4 Documentation (2.0.0 session)

- [x] Changelog and migration guide exist.
- [x] Historical status files are retained.
- [ ] Correct historical run-to-SHA references after inspecting the run metadata.
- [ ] Record final remote-consumer evidence; do not equate Maven Local success with JitPack success.

### 3.5 Publication (2.0.0)

- [x] Tag `2.0.0` exists.
- [x] GitHub Release `2.0.0` is published.
- [ ] Confirm consumption of the released tag from JitPack, or document that it remains unavailable.
- [ ] If a source/configuration fix is required, verify it on a new commit first and obtain
  separate approval for a patch release such as `2.0.1`. Do not rewrite `2.0.0`.

The 2.0.0 follow-up targeted `update` only. No change to `master`, release tag or GitHub
Release was authorized by that document. No fresh build, consumer run or device test is
claimed there.
