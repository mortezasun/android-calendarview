# Version 2 implementation progress (superseded)

Status: **superseded.** This file recorded the migration progress on 2026-11-09. It is kept for
history only; its "Still open" list has since been completed (legacy view/XML removal, Jalali
isolation, version catalog, localization, documentation migration, migration guide and the 2.0.0
version metadata).

**[docs/PROJECT_STATUS.md](PROJECT_STATUS.md) is the single source of truth for the current
verification state, test counts and remaining work.** Use it instead of this document.

## What was true on 2026-11-09

Date: 2026-11-09
Branch: update
Architecture: decisions/0002-compose-v2-shared-core.md

Shared Compose selection/snapshots/grid; Gregorian/Hijri/Jalali APIs with adjacent-day
providers; heatmaps; list/pager APIs and five sample modes; synchronized visible-month
navigation/dialog; sample locale/RTL/selection/adjacent-day controls; verification CI.

At that time 55 added JVM tests and nine added instrumented tests existed (four core, five
sample), excluding the pre-existing templates, and the sample still declared
`1.1.0-alpha1`. Current numbers are in PROJECT_STATUS.md; no v2 release tag has been created.
