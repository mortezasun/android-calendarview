# ADR 0002: Compose v2 with a shared core

Date: 2026-11-09
Status: Accepted architecture; implementation pending
Target release: 2.0.0
Target branch: update

## Context

The user requested a complete migration from XML/ViewBinding to native Jetpack Compose, preservation of calendar functionality, updated compatible stable dependencies, documentation, and commits on the update branch only. The user selected option 1: three independently consumable calendar libraries backed by a shared core.

## Decision

Retain the existing Gradle module names:

- :library is the shared core for calendar contracts, selection state, Compose rendering and heatmap UI. It must not depend on concrete calendar modules.
- :calendar-gregorian provides the Gregorian implementation and its public Compose entry point.
- :calendar-hijri provides the Islamic Hijri implementation and its public Compose entry point.
- :calendar-solarHijri provides the Persian/Jalali implementation and its public Compose entry point.
- :calendarviewsample is an unpublished Compose demonstration app.

Each calendar artifact depends on :library, not on another calendar artifact. Consumers can include just one calendar artifact and receive its shared-core dependency transitively. The sample can depend on all three.

Native composables and composable content slots replace XML inflation, ViewBinding adapter callbacks and custom-view rendering. Wrapping the old views with AndroidView does not satisfy this migration. Calendar calculations remain ordinary Kotlin logic, separate from rendering; they are not themselves composables.

The core owns observable selection state with explicit events and clear ownership. No selection state may rely on cached view bindings. Public APIs must support custom day cells, weekday labels, month headers and selection summaries. Preserve none, single, multiple and range selection; supported scrolling/paging modes; calendar navigation; locale and RTL handling; and heatmap functionality.

XML layout and ViewBinding usage are migration targets. AndroidManifest.xml and non-layout resources may remain where required; Compose does not imply deleting all XML files.

## Compatibility and release

This is an explicitly requested breaking public API migration for version 2.0.0. Document replacements for the old adapter and view APIs. Do not present the old API as compatible unless a tested compatibility layer is deliberately provided.

Verify current stable dependency versions and toolchain compatibility against official sources before changing the version catalog. Do not treat versions already written in repository documentation as independent verification of latest releases. Add only dependencies needed for the migration.

## Implementation and verification checklist

- Inventory public APIs, sample features, layouts and calendar calculations before replacement.
- Add behavioral tests for selection transitions and calendar month arithmetic, including year boundaries.
- Implement core state, native Compose rendering, customization slots and navigation.
- Implement and verify each calendar module independently of the other two.
- Migrate heatmap and sample UI; remove obsolete UI implementations and dependencies only when replaced.
- Align release metadata to 2.0.0 without publishing a release or tag as an incidental action.
- Update README, ARCHITECTURE, DEVELOPMENT, SETUP, PROJECT_STATUS and AGENTS.md to match implemented behavior.
- Run applicable compilation, unit tests, lint and sample assembly; separately verify UI, RTL and restoration on a device or emulator. Record commands and actual outcomes.
- Commit only task-related changes on update. Never force-push or merge into master.

## Current status

This ADR records the approved target architecture only. It does not implement the Compose migration, update dependencies, change release metadata or establish passing build/test results for version 2.
