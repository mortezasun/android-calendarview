# 0001 - Generic ViewBinding adapters for multiple calendar systems

- Status: accepted (inferred from the existing code; rationale not recorded in the repository)
- Date: 2026-11-09

## Context

The library has to render Gregorian, Hijri and Solar Hijri (Jalali) months while letting the
consumer style every cell, weekday label, month header and selection summary with its own layout.
The calendar-system-specific code must not leak into the core module.

## Decision

The core views are generic over four `ViewBinding` types:

```kotlin
MultiMonthView<D : ViewBinding, W : ViewBinding, M : ViewBinding, SelectController : ViewBinding>
```

`MonthStatus<Month, Day>` and `DayStatus` abstract the calendar system, and
`BaseCalendarAdapter`/`MonthSampleAdapter` expose abstract `onBind*` hooks that return the
consumer's bindings. The calendar class is passed to `MultiMonthView.addMonths` as a `Class<T>`.

## Alternatives considered

The repository does not document alternatives. Possible options such as a single concrete binding,
Compose slots, or a non-generic `View`-based adapter contract are **not** recorded as considered;
no decision record from the original author was found.

## Consequences

Positive:

- The consumer keeps full control over layout and styling without the library knowing resource IDs.
- Calendar systems are isolated in separate modules; `:library` only depends on the abstractions.
- `ViewBinding` types are checked at compile time inside the consumer's adapter implementation.

Negative:

- Consumers must declare four binding types and implement several hooks even for simple use cases.
- The type parameters are erased at runtime. `addMonths` reflects on a `Locale` constructor and the
  code uses `@Suppress("UNCHECKED_CAST")` at the boundaries.
- The adapter contract is wide; adding a rendering hook is a breaking public API change.
- No Compose support and no shared view holder between different binding types.

## Open questions

- Why four separate bindings instead of one binding with optional sections was not recorded.
- Whether the reflection-based month creation was intended to support plugin-style calendar classes
  or simply to avoid a factory interface cannot be verified from the code. It is documented here as
  a constraint, not as a confirmed design goal.
