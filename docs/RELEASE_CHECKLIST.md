# Release acceptance: 2.0.0

Status: GitHub release published; remote Maven distribution acceptance remains OPEN.
The `2.0.0` tag and both branches were observed at
`9e521a67fde6316e4672d239a40797a2a9609518` before this follow-up.
Do not move the published tag to incorporate subsequent fixes.

## Correction to the historical candidate reference

The previous version of this checklist and PROJECT_STATUS.md used the incorrect full
SHA `d552dec886daea64fe23c347b97364b63ee2d770`. The actual commit in the repository
with the short name `d552dec` is `d552decb8f7382e883e705533079664e76ab1158`.
This correction does not independently establish the head SHA of the historical CI runs;
that mapping still needs direct verification. The reference in PROJECT_STATUS.md is
historical and must not be used as an exact verification target without this correction.

## 1. Historical verification evidence

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

## 2. Publishing and JitPack - still open

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
JitPack preflight. The user reports that JitPack is not green. The log and status could
not be retrieved during this follow-up, so a specific JitPack failure cause is not confirmed.

The follow-up changes invoke Gradle through Bash and explicitly publish all four library
modules. This removes reliance on the wrapper executable bit; it does not prove that
permissions were the observed JitPack failure. A separate CI workflow validates the
publication tasks and generated AAR/POM/module/sources files in an isolated Maven repository.
That CI job is not a substitute for a JitPack consumer build.

## 3. Manual acceptance

- [x] API 29 instrumentation results are recorded for selection, navigation, dialogs,
  heatmaps, RTL and activity recreation. Exact coverage is determined by the test reports;
  a passing suite is not exhaustive verification of every locale, font scale or runtime.
- [x] Physical device testing was waived with documented residual risk in PROJECT_STATUS.md.

## 4. Documentation

- [x] Changelog and migration guide exist.
- [x] Historical status files are retained.
- [ ] Correct historical run-to-SHA references after inspecting the run metadata.
- [ ] Record final remote-consumer evidence; do not equate Maven Local success with JitPack success.

## 5. Publication and follow-up release

- [x] Tag `2.0.0` exists.
- [x] GitHub Release `2.0.0` is published.
- [ ] Confirm consumption of the released tag from JitPack, or document that it remains unavailable.
- [ ] If a source/configuration fix is required, verify it on a new commit first and obtain
  separate approval for a patch release such as `2.0.1`. Do not rewrite `2.0.0`.

This follow-up targets `update` only. No change to `master`, release tag or GitHub Release
is authorized by this document. No fresh build, consumer run or device test is claimed here.
