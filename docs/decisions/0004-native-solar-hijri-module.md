# ADR 0004: Native Solar Hijri module (`:calendar-solarHijri-native`)

Date: 2026-11-12
Status: Accepted for 2.3.0
Target release: 2.3.0
Target branch: update

## Context

[ADR 0003](0003-phase-3-distribution-and-hijri-method.md) deferred replacing `ir.huri:jalalicalendar` inside `:calendar-solarHijri` because `JalaliCalendar` is part of that module's public JVM API (`api(libs.jalali.calendar.huri)`). Removing or replacing it in place would break binary and source compatibility for existing callers.

At the same time, consumers want a dependency-free Solar Hijri engine with an immutable value type, a single consistent leap-year rule across long date ranges, and deterministic `Clock` injection for tests.

## Decision

1. **Coexistence over replacement:** Keep `:calendar-solarHijri` and its public API (`api/calendar-solarHijri.api`) completely unchanged. Introduce `:calendar-solarHijri-native` as a sibling module at the shared root version (`2.3.0`) and constrain it in `:calendar-bom`.
2. **Package and namespace:** Use `com.develotter.calendarview.calendars.solarHijriNative` (matching the camelCase convention of `solarHijri` while avoiding the Java reserved keyword `native` in package segments). Public types and composables use the `SolarHijriNative*` / `solarHijriNative*` prefix (except the core types `SolarHijriEngine`, `SolarHijriDate`, `SolarHijriLocale`) so both Solar Hijri modules can coexist on the same classpath without symbol collisions.
3. **Algorithm and leap-year single source of truth:** Port Kazimierz M. Borkowski's (1996) break-table algorithm as implemented in `jalaali-js` (MIT License; attributed in `SolarHijriEngine.kt` and root `NOTICE`) for years `-61..3177` AP. All leap-year checks (`isLeapYear`), month lengths (`daysInMonth`), and epoch-day conversions (`toEpochDay`, `fromEpochDay`) derive exclusively from the internal `jalCal` break calculation—never mixing a secondary `mod 33` rule.
4. **Epoch-day identity & boundaries:** `SolarHijriEngine.toEpochDay` uses `LocalDate.toEpochDay()` (1970-01-01 = day 0, JDN offset `2440588L`), preserving cross-calendar selection and marker compatibility. Years outside `-61..3177` AP and epoch days outside `MIN_EPOCH_DAY..MAX_EPOCH_DAY` throw `IllegalArgumentException`. `SolarHijriDate.plusMonths` / `withYear` / `withMonth` clamp the day to `daysInMonth`.
5. **UI boundary guard & time source:** `SolarHijriNativeMonthState` clamps navigation to `-61-01..3177-12` in addition to optional caller bounds, and accepts an injectable `java.time.Clock` (`SolarHijriDate.now(clock)`). When rendering adjacent month days at `-61-01` or `3177-12`, out-of-range cells are returned as blank, disabled `CalendarDayCell` instances instead of throwing.

## Consequences

- Existing `:calendar-solarHijri` consumers remain 100% binary compatible.
- Consumers of `:calendar-solarHijri-native` pull only `:library` (plus Compose Material 3 `implementation`) and no `ir.huri:jalalicalendar` transitive dependency.
- `api/calendar-solarHijri-native.api` is tracked by `scripts/api-check.sh` alongside the existing modules, and all CI/publishing/JitPack lists include `:calendar-solarHijri-native`.
