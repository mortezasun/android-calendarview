# ADR 0005: Self-written native Chinese calendar

Date: 2026-11-13. Status: implemented on branch `update` (2.4.0 release candidate); tag/release open.

## Context

2.3.0 already includes a shared Compose core and separate calendar modules. The new Chinese calendar must be self-written, optionally includable, and independent of other calendar engines. Native here means our own platform-independent Kotlin calculation, not merely a Kotlin wrapper around Android ICU.

## Decision

Two separate artifacts: `:calendar-chinese-native` for calculation and immutable date values; `:calendar-chinese-compose` for rendering and UI state. Dependency direction: Compose -> engine and existing UI core. Engine -> Kotlin standard library. Existing engines and UI core -> no new engine.

No adoption or porting of lunar-java, ICU, internet snippets, or third-party calendar algorithm implementations. Calendar data must be treated separately from our own code: provenance, usage rights, and generation process must be verified. Self-written code does not mean self-observed astronomical data.

The first engine uses our own integer conversion logic with versioned, compact month data
(Chinese years `1901..2099`, generated at development time from VSOP87D/Meeus; no HKO or ICU data embedded). This is a native offline implementation, not real-time astronomical calculation. Supported range and dataset version are part of the contract. No extrapolated results outside the range.

Existing Compose APIs remain unchanged. KMP/Kotlin-Native support of the engine is proven by real builds and tests. The Android Compose facade is not advertised as iOS-capable as long as its shared core is not demonstrably multiplatform.

## Consequences and alternatives

Benefits: engine usable without UI, clear dependency verification, identical calculation across platforms, additive introduction. Costs: two publications, dataset maintenance, and additional Native CI. A single Android module would be simpler, but would make true UI independence and Native verification harder. A complete KMP migration of all existing modules is a separate architectural task.

Binding execution: [Release plan](../../RELEASE_PLAN_2.4.0.md), [Specification](../CHINESE_CALENDAR_SPEC.md), [Quality](../CHINESE_CALENDAR_QUALITY.md).
