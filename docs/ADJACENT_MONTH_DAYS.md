# Adjacent dates: all calendar modules and sample

CalendarMonthGrid supports independent showPreviousMonthDays/showNextMonthDays flags and
a calendar-specific adjacentDayProvider. Both flags default to false. The shared builder
produces complete seven-column rows, marks isCurrentMonth, preserves enabled flags and
validates absolute-day identity and chronological current-month input.

## Completed engine integration

GregorianCalendarMonth, HijriCalendarMonth and SolarHijriCalendarMonth now expose both
visibility flags and supply providers automatically. New public gregorianDayCell,
hijriDayCell and solarHijriDayCell functions are shared by current-month and adjacent
rendering, avoiding mismatched date labels/availability rules. Existing month-builder
signatures remain unchanged. Composable flags were appended after isDateEnabled: use
named isDateEnabled arguments instead of relying on it being the final trailing lambda.

Gregorian dates use LocalDate, Hijri preserves the input Hijrah chronology, and Jalali
uses checked epoch conversion. The predicate applies to each rendered date regardless
of month. Jalali predicates receive a separate mutable instance. Range interiors can
still include disabled days; this change does not introduce a whole-range blocking policy.

Locale controls labels, not layout direction. Persian month names use JalaliCalendar;
other Jalali month names remain English transliterations. Date-range/conversion errors
propagate rather than substituting Gregorian labels or silently suppressing bad dates.

## Sample controls

Two saved switches, Previous month days and Next month days, control all three calendars
in every display mode and the shared-selection dialog. Defaults remain off. Adjacent
clicks select/call back without automatic navigation. Repeated representations of the
same date use the shared absolute-day selection identity. Existing window/navigation
limitations documented in COMPOSE_MONTH_CONTAINERS.md are unchanged.

## Verification

Four JVM integration tests added in the sample module, which intentionally depends on
all three engines: Gregorian year boundary, Hijri year boundary, Jalali Nowruz and disabled
predicates. This supplements six existing shared-grid tests. The 2.1.0 release-tree API 29
runs passed all 26 instrumentation tests; the earlier report for grid commit
bab4922d1b9f29392d0e379d207bd2d18a95f8aa remains historical.

```bash
bash ./gradlew test :calendarviewsample:assembleDebug
bash ./gradlew :library:connectedDebugAndroidTest
```

Manual checks: each flag independently and together, all calendars/locales/modes, selection
across month/year boundaries, dialog parity, disabled dates, RTL and font scaling.
