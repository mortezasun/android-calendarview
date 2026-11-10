# Release acceptance: 2.0.0

Status: ACCEPTED. Final candidate: `d552dec886daea64fe23c347b97364b63ee2d770` on
`update`.

## Evidence recorded

[PROJECT_STATUS.md](PROJECT_STATUS.md) records successful verification and API 29 instrumentation
workflows for candidate `d552dec886daea64fe23c347b97364b63ee2d770`.

## 1. Final candidate and CI

- [x] Record the full final candidate SHA (`d552dec886daea64fe23c347b97364b63ee2d770`).
- [x] Confirm `Android verification` succeeds for that exact `head_sha` (Run 42911012589).
- [x] Confirm `Android instrumentation` succeeds for that exact `head_sha` (Run 42911012594).
- [x] Record run IDs and inspect reports: 19 instrumented tests passed (12 in `:library`, 7 in `:calendarviewsample`).
- [x] Confirm release builds of all four library modules and debug assembly of the sample (all AARs and debug APK captured).
- [x] Confirm JVM tests (70 tests across all modules) and lint (0 errors) cover all modules.

## 2. JitPack preflight before the final tag

- [x] Local publishing and independent consumer build verified in four configurations (Maven Local baseline).
- [x] JitPack service availability evaluated (external Cloudflare worker 500 error logged; artifacts verified locally and in CI).

## 3. Manual acceptance

- [x] Automated instrumentation suite on API 29 covers:
  - Three calendars: month/year boundaries, month lengths and today markers.
  - Selection: None, Single, Multiple, Range and clearing.
  - Navigation: buttons, lists and pagers on both axes.
  - Dialog: synchronized month, selection and dismissal.
  - Heatmaps: both orientations, disabled dates, selection and small visual cells.
  - Localization and RTL: English, German, Persian and Arabic.
  - Appearance: light/dark, larger fonts and narrow windows.
  - State restoration: activity recreation with retained selection and visible month.
- [x] Physical device testing waived with documented residual risk per explicit owner directive.

## 4. Documentation freeze

- [x] Review CHANGELOG.md, README.md and V2_MIGRATION_MAPPING.md together.
- [x] Preserve historical instrumentation results and mark their commit/run scope.
- [x] Keep V2_IMPLEMENTATION_STATUS.md clearly superseded.
- [x] Remove the README's unreleased notice for the 2.0.0 release.
- [x] Record all final evidence and accepted limitations in PROJECT_STATUS.md.

## 5. Tag and publication

- [x] All required gates above are passed or explicitly waived by the owner.
- [x] Confirm the final SHA, clean working tree and absence of an existing `2.0.0` tag.
- [x] Create and push the annotated `2.0.0` tag.
- [x] Publish GitHub Release notes from the approved changelog content.

No merge into `master` is included in this checklist.
