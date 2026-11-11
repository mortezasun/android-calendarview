# Release acceptance

Status: `2.0.1` is released, built successfully by JitPack and consumed from JitPack by
an independent project. A `2.1.0` release candidate is prepared locally but is not tagged
or published. The 2.0.0 material at the end remains a historical record.

## 0. Release candidate: 2.1.0 (not released)

- [x] Root version and sample version set to `2.1.0`.
- [x] Published `2.0.1` AARs compared with the candidate; all 248 public/protected JVM
  members retain identical descriptors.
- [x] Hidden delegation overloads added for the changed selection state, month grid and
  three engine month composables.
- [x] Standalone consumer built against published `2.0.1` and the local `2.1.0` candidate,
  including the provider/range-policy source and the BOM path, without `mavenLocal()`.
- [x] Local publication contains the BOM and all four constrained artifacts; the BOM
  resolves the four modules at `2.1.0`.
- [ ] Commit and push, then run the full remote verification, instrumentation and
  publishing checks.
- [ ] Obtain explicit approval, create tag `2.1.0` without moving `2.0.1`, publish the
  GitHub Release, and verify the JitPack build and an independent JitPack consumer.

## 1. Current release: 2.0.1 (2026-11-11)

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

## 2. Historical 2.0.0 acceptance record

Checkbox states in this section are the states recorded at the end of the 2.0.0 session.
The 2.0.1 section above is the current status.

### 2.1 Candidate and CI evidence

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

### 2.2 Publishing and JitPack (2.0.0 session)

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

### 2.3 Manual acceptance (2.0.0)

- [x] API 29 instrumentation results are recorded for selection, navigation, dialogs,
  heatmaps, RTL and activity recreation. Exact coverage is determined by the test reports;
  a passing suite is not exhaustive verification of every locale, font scale or runtime.
- [x] Physical device testing was waived with documented residual risk in PROJECT_STATUS.md.

### 2.4 Documentation (2.0.0 session)

- [x] Changelog and migration guide exist.
- [x] Historical status files are retained.
- [ ] Correct historical run-to-SHA references after inspecting the run metadata.
- [ ] Record final remote-consumer evidence; do not equate Maven Local success with JitPack success.

### 2.5 Publication (2.0.0)

- [x] Tag `2.0.0` exists.
- [x] GitHub Release `2.0.0` is published.
- [ ] Confirm consumption of the released tag from JitPack, or document that it remains unavailable.
- [ ] If a source/configuration fix is required, verify it on a new commit first and obtain
  separate approval for a patch release such as `2.0.1`. Do not rewrite `2.0.0`.

The 2.0.0 follow-up targeted `update` only. No change to `master`, release tag or GitHub
Release was authorized by that document. No fresh build, consumer run or device test is
claimed there.
