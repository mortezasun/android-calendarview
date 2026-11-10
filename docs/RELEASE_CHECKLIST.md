# Release acceptance: 2.0.0

Status: OPEN. Preparation baseline: `eb422da96b12129d53591a4566c4399b8f85efe3` on
`update`. Any subsequent change creates a new candidate that needs its relevant checks.
This document does not authorize tagging, publishing a GitHub Release, merging branches
or waiving manual acceptance.

## Evidence already recorded

[PROJECT_STATUS.md](PROJECT_STATUS.md) records successful local publishing for all four
library modules, an independent Maven Local consumer build in four configurations, and
successful verification and API 29 instrumentation workflows for `ece9864`. These are
recorded historical results, not fresh verification of `eb422da` or later commits.

## 1. Final candidate and CI

- [ ] Record the full final candidate SHA.
- [ ] Confirm `Android verification` succeeds for that exact `head_sha`.
- [ ] Confirm `Android instrumentation` succeeds for that exact `head_sha`.
- [ ] Record run IDs and inspect reports for the expected core, heatmap and sample tests.
- [ ] Confirm release builds of all four library modules and debug assembly of the sample.
- [ ] Confirm JVM tests and lint cover all modules; record any accepted warnings.

Do not interpret a rate-limit or unavailable report as a failed run or a successful run.

## 2. JitPack preflight before the final tag

- [ ] Use the independent consumer project, without `project(...)` or `includeBuild(...)`.
- [ ] Replace Maven Local with `maven { url = uri("https://jitpack.io") }`; retain Google
  and Maven Central for other dependencies. Ensure Maven Local cannot mask missing artifacts.
- [ ] Request the fixed candidate version, initially `eb422da`, rather than `update-SNAPSHOT`.
- [ ] Build Gregorian-only, Hijri-only, Solar-Hijri-only and all-three configurations.
- [ ] Inspect resolved dependencies for the shared core and the isolated Jalali dependency.
- [ ] Check JitPack logs and the four artifacts/POMs; record the resolved version and outcome.

For the existing untracked consumer project, use `-PcalendarVersion=eb422da` only if its
build actually reads that property. The project is not checked into this repository;
its commands and results must not be assumed or invented. A first artifact request can
trigger a remote JitPack build.

## 3. Manual acceptance

- [ ] Three calendars: month/year boundaries, month lengths and today markers.
- [ ] Selection: None, Single, Multiple, Range and clearing.
- [ ] Navigation: buttons, lists and pagers on both axes.
- [ ] Dialog: synchronized month, selection and dismissal.
- [ ] Heatmaps: both orientations, disabled dates, selection and small visual cells.
- [ ] Localization and RTL: English, German, Persian and Arabic.
- [ ] Appearance: light/dark, larger fonts and narrow windows.
- [ ] State restoration: record the lifecycle scenario actually tested.

Record device, Android version, APK source SHA, results and known issues in
PROJECT_STATUS.md. If no device is available, this gate remains open. An explicit owner
decision may waive it with documented residual risk; CI is not silently substituted.

## 4. Documentation freeze

- [ ] Review CHANGELOG.md, README.md and V2_MIGRATION_MAPPING.md together.
- [ ] Preserve historical instrumentation results and mark their commit/run scope.
- [ ] Keep V2_IMPLEMENTATION_STATUS.md clearly superseded; do not delete history merely
  to remove old open-item wording.
- [ ] Keep the README's unreleased notice until the tag exists.
- [ ] Do not claim remote artifact availability before the corresponding consumer test.
- [ ] Record all final evidence and accepted limitations in PROJECT_STATUS.md.

## 5. Tag and publication - separate explicit approval

- [ ] All required gates above are passed or explicitly waived by the owner.
- [ ] Confirm the final SHA, clean working tree and absence of an existing `2.0.0` tag.
- [ ] Obtain explicit approval for the exact tag target and GitHub Release content.
- [ ] Create and push the annotated `2.0.0` tag on the approved SHA.
- [ ] Confirm JitPack builds that tag; run the external consumer against `2.0.0`.
- [ ] Remove the obsolete README snapshot notice and date the changelog only when true.
- [ ] Publish GitHub Release notes from the approved changelog content.

Do not move a published tag to hide a correction. Use a subsequent patch release when needed.
No merge into `master` is included in this checklist.
