# Compose v2 audit and completion checklist

Date: 2026-11-09
Branch: update
Scope: source/document review of the migration work and recorded open points, not an
exhaustive repository-wide correctness proof. No build or runtime verification occurred.

## Priority 0: verification and correctness

- OPEN: compile every module and assemble the sample. No migration commit has a verified
  Gradle result in this session; compiler/plugin compatibility is not established.
- OPEN: execute JVM tests, lint, Compose UI tests, emulator/device checks and restoration
  tests. Earlier PROJECT_STATUS results refer to pre-migration code. No CI workflow is
  present in the root listing; a dedicated workflow has not been added in this step.
- IMPLEMENTED, UNVERIFIED: saved selection now includes format version and selection mode.
  Empty selections have nonempty payloads; incompatible/corrupt snapshots reinitialize.
  Six pure-code regression tests cover encoding, not Android saved-state lifecycle.
- OPEN: StateRestorationTester checks for clear-after-initial-selection, mode changes,
  process/activity recreation, window identity and list/pager positions.
- OPEN: platform JDK/Android differences in Hijrah locale/chronology data and Jalali
  conversion/library support ranges must be tested, not inferred from source review.

## Priority 1: remaining functional parity

- IMPLEMENTED, UNVERIFIED: native single-month entry points for three calendars, shared
  selection, quarter/year heatmaps, lists/pagers and sample mode switching.
- OPEN: adjacent-month cells and independent previous/next visibility/configuration.
- OPEN: inventory all old CalendarStatus settings, adapters, month/week/day modes, click
  and long-click callbacks, selected-day controller hooks, today/focus styling and public
  navigation helpers; map each to a Compose replacement before deleting legacy code.
- OPEN: navigation from the actually swiped month, synchronized dialog month and mode
  switching at the same visible month. Current outer buttons intentionally recenter from
  the anchor; list and pager positions remain separate.
- OPEN: window growth/range-boundary UX. The sample requires the complete 25-month window.
- OPEN: device checks for bounded/nested scrolling, vertical-pager gestures, large fonts,
  dark theme, RTL, accessibility semantics, touch targets and custom slot interactions.
- OPEN: fully localized demo controls and non-Persian Jalali month translations.

## Priority 2: architecture and dependency cleanup

- OPEN: migrate legacy CalendarViewActivity before removing its bindings/layouts.
- OPEN: remove obsolete public views/adapters only with documented v2 replacements.
- OPEN: remove dangling CustomCalendarViewActivity manifest entry.
- OPEN: remove shared-core Jalali export after inspecting/migrating every affected API.
  CalendarUtil.kt and DayStatus.kt inspected in this review do not import Jalali, but this
  does not prove the whole core is free of it. No unsafe dependency removal was performed.
- OPEN: consolidate Compose coordinates/BOM in libs.versions.toml, remove unused XML UI
  dependencies after migration, and verify current stable versions against official sources.
- OPEN: confirm the actual Kotlin compiler used by AGP built-in Kotlin and compatibility
  with the configured Compose compiler plugin; catalog declarations alone are not proof.
- OPEN: define/test consumer dependency and publishing metadata for each of the three
  separately consumable calendar artifacts plus transitive shared core.

## Priority 3: documentation and release

- OPEN: update README, ARCHITECTURE, DEVELOPMENT, SETUP and AGENTS.md. The checked AGENTS.md
  still says there is no Compose and describes only old adapter/state rules. It is stale
  for v2; ADR 0002 and migration status describe the explicitly requested new direction.
- OPEN: update PROJECT_STATUS without presenting historical build results as current.
- OPEN: complete public KDoc and migration guide, including removed XML attributes and
  adapter methods, behavior changes, disabled-range policy and saved-state limits.
- OPEN: align version metadata to 2.0.0 only with an honest implementation status. The
  sample still declares 1.1.0-alpha1; no v2 release/tag has been published here.
- OPEN: final module build/test/lint matrix and manual acceptance checks before release.

## Snapshot compatibility

Development snapshots that stored only Long keys are intentionally not migrated. The new
format is a nonempty List<String>: version, mode name, then signed epoch-day strings.
Malformed/unknown/mode-incompatible snapshots return null to let the initializer run.
A valid empty snapshot restores an empty selection rather than falling back to initial
dates. Range interiors are never enumerated. Large multiple selections still grow saved
state; this change is not durable storage or a Bundle-size solution.

Official/API references consulted:

- https://developer.android.com/develop/ui/compose/state-saving
- https://composables.com/jetpack-compose/androidx.compose.runtime/runtime-saveable/composable-functions/rememberSaveable/api

No checklist item marked OPEN is claimed implemented by this audit.
