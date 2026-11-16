# Adjacent dates: all calendar modules and sample

`CalendarMonthGrid` supports independent `showPreviousMonthDays`/`showNextMonthDays` flags and a
calendar-specific `adjacentDayProvider`. Both flags default to `false`. The shared builder
produces complete seven-column rows, marks `isCurrentMonth`, preserves enabled flags and
validates absolute-day identity and chronological current-month input.

## Engine integration

All five engine month composables expose both visibility flags and supply providers
automatically: Gregorian, Hijri, both Solar Hijri entries and Chinese. Public
`gregorianDayCell`, `hijriDayCell`, `solarHijriDayCell`, `solarHijriNativeDayCell` and
`chineseDayCell` helpers are shared by current-month and adjacent rendering, avoiding mismatched
date labels or availability rules.

Dates use immutable, calendar-specific value types (`GregorianYearMonth`, `HijriDate`,
`SolarHijriDate`, `ChineseDate`, `ChineseMonth`) and absolute epoch-day identities. There are no
`java.time` types, no `JalaliCalendar` dependency and no platform-specific date arithmetic in
`commonMain`. The predicate applies to each rendered date regardless of month. The range policy
still allows interiors containing disabled days; this change does not introduce a whole-range
blocking policy.

Locale controls labels, not layout direction. Persian month names use the Solar Hijri engine's
`SolarHijriLocale`; other Jalali labels remain English transliterations. Date-range and
conversion errors propagate rather than substituting Gregorian labels or silently suppressing
bad dates.

## Sample controls

Two saved switches, Previous month days and Next month days, control every calendar mode in
every display mode and the shared-selection dialog. Defaults remain off. Adjacent clicks
select/call back without automatic navigation. Repeated representations of the same date use the
shared absolute-day selection identity. Existing window/navigation limitations documented in
`COMPOSE_MONTH_CONTAINERS.md` are unchanged.

## Verification

The shared-grid and engine boundary suites cover Gregorian and Hijri year boundaries, Solar Hijri
Nowruz, Chinese month boundaries and disabled predicates. The historical 2.1.0 emulator evidence
and its 26 instrumentation tests are recorded in [PROJECT_STATUS.md](PROJECT_STATUS.md).

```bash
./gradlew jvmTest :calendarviewsample:assembleDebug
./gradlew :library:compileAndroidDeviceTest :calendarviewsample:compileDebugAndroidTestKotlin
```

Manual checks: each flag independently and together, all calendar modes/locales, selection
across month/year boundaries, dialog parity, disabled dates, RTL and font scaling.
