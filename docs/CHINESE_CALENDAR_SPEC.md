# Chinese calendar: API and data contract for 2.4.0

Status: implemented on branch `update` (2.4.0 release candidate); public API dumps are under
`api/`. Current verified remote HEAD is `dc3abe22f1d67ad2dd78fe43b99515f49ee97f27`.

## Calendar convention

Modern Chinese lunisolar calendar with a documented civil UTC+08:00 reference. It makes no
promise about historical local-time rules or equality with Vietnamese/Korean variants. An epoch
day, as before, is the integer ISO date relative to 1970-01-01, not an instant and not local
midnight expressed in milliseconds. The same day identity must be preserved across all
calendars.

`year` is the Gregorian year in which the relevant Chinese New Year begins. Do not equate it
with ICU `YEAR` or `EXTENDED_YEAR` without verification. `monthNumber` is in 1..12; leap months
are distinguished by a separate flag and are never encoded as month 13.

## Implemented engine API

Package: `com.develotter.calendarview.calendars.chineseNative`.

- `ChineseMonth`: `year`, `monthNumber`, `isLeapMonth`; private constructor, factories `of` and
  `fromEpochDay`.
- `ChineseDate`: `year`, `monthNumber`, `isLeapMonth`, `dayOfMonth`; factories `of` and
  `fromEpochDay`.
- Neither type is a data class; validation happens in the constructor, equality/hash code are
  manual, and there is no `copy()` path.
- `ChineseCalendarEngine`: `MIN_YEAR`/`MAX_YEAR`, `MIN_EPOCH_DAY`/`MAX_EPOCH_DAY`,
  `isSupported`, `fromEpochDay`, `toEpochDay`, `monthsInYear`, `monthOf`, `lengthOfMonth`,
  `lengthOfYear`, `monthStartEpochDay`, `firstMonthOfYear`, `lastMonthOfYear`, `nextMonth`,
  `previousMonth`, `leapMonth`, and `hasLeapMonth`.
- `ChineseDateOutOfRangeException : IllegalArgumentException` for unsupported values; invalid
  months/days throw `IllegalArgumentException`.
- `ChineseFormat`/`ChineseTextStyle`: English, SimplifiedChinese, TraditionalChinese, Numeric.
- Day arithmetic uses epoch days with checked addition; month arithmetic follows the actual
  order of the 12/13 months. When the day does not exist in a shorter target month, it is clamped
  to that month's last day; a missing leap month is never invented.
- Months of a year are returned chronologically, for example regular month N, leap month N,
  regular month N+1. There is no fixed list containing exactly twelve entries.
- Date comparison is chronological; the same month number with a different leap flag is not the
  same month.

Invalid date: `IllegalArgumentException` or a documented specialized subtype. Unsupported date:
a consistent documented range error. Never return null/substitute data and never silently clamp
date conversion. Check the year and bounds before accessing tables. Explicitly guard numeric
extremes and `Long.MIN_VALUE` as arithmetic operands.

## Data and bounds

Implemented range: Chinese years `1901..2099`, corresponding to Gregorian `1901-02-19` through
`2100-02-08` (72,674 days, 2,461 months, 73 leap years). The table contains complete Chinese
years; days outside the range are rejected rather than extrapolated. Partially visible boundary
months are represented as empty, disabled cells in the Compose facade. The generator compared
**2,461 month starts** with HKO references: **2,458 exact + 3 audited differences = 2,461,
0 missing references/files**. The three differences are documented below and in the manifest.
No HKO table is shipped.

Own data set: month start as an epoch day, month number, leap flag and length, or an equivalent
self-defined compact encoding. Validate invariants during generation: strictly increasing
starts, gapless days, valid lengths, valid month order and consistent year starts. Use prefix
indexes for bounded search; do not use a mutable global calendar instance.

Required manifest: source and retrieval date, terms of use/attribution, source hash, generator
version, data-set version and hash, exact range, reference convention, conflict list and
reviewer. The generator and checked input must be reproducible; runtime and normal consumer
builds must not require a reference server.

Events near local midnight can differ by one day depending on the reference. The generator
documents three such cases: 1914-11-18, 1916-02-04 and 1920-11-11. For each case,
`tools/chinese-calendar/reference-conflicts.json` records JDE (TT), Delta-T, the UTC instant,
the UTC+8 calendar date, a structured independent NASA/GSFC reference (UTC time, source, URL
and documented 60 s tolerance) and the classification: the new moon occurs 1m43s, 5m16s and
4m50s after UTC+8 midnight respectively. The NASA table gives 1914-11-17 16:02 UT,
1916-02-03 16:05 UT and 1920-11-10 16:05 UT. `--verify-reference` checks that `newMoonUtc` and
`newMoonUtcPlus8` denote the same instant, that both carry explicit offsets, and that the NASA
time is within the documented tolerance of the newly calculated UTC value. The HKO row carries
the preceding UT calendar day; this is consistent with Beijing local time (UT+7:45:40) before
1929. The library makes a deliberate product decision: it applies the modern UTC+8 convention
across the entire range and does not adopt a historical local-time rule. A blanket switch to
local time would, according to the audit performed, also move other month starts (for example
1906-04-24); it is therefore not part of this decision. The three HKO rows are documented
historical reference differences, not an open data question. The table remains on the
documented UTC+8 date; there is no undocumented table correction. `--verify-reference` checks
all expected files and month starts as well as year and leap status, exiting with status 1 on
differences. Data-set version `2026.11-astronomical-v1`, output SHA-256 and VSOP87D input
SHA-256 are recorded in the manifest. Changes must be traceably versioned in later releases; no
arbitrary special cases.

## Compose contract

Package: `com.develotter.calendarview.calendars.chineseCompose`. Implemented:
`ChineseCalendarMonth`, `GregorianChineseCalendarMonth`, `ChineseMonthData`, `ChineseWeekData`
with `chineseWeekData`, `ChineseMonthState` with `rememberChineseMonthState`,
`ChineseMonthSnapshot`, `chineseMonthData`, `chineseMonthOfOrNull`, `chineseYearHeatmap` and
`chineseMonthHeatmap`.

- Use `CalendarMonthData`/`CalendarWeekData` and the existing renderer.
- Selection, markers and click mapping remain epoch-day based.
- State saving includes year, month number and leap flag as well as required range parameters;
  do not serialize platform-bound date objects.
- Deterministic conversion knows neither the clock nor the device time zone. Today is injected
  into the UI as an epoch day; a platform-specific convenience adapter may calculate it
  explicitly. The display time zone for today and the fixed calendar reference are separate
  decisions.
- Document min/max navigation, `goTo`, restoration and day clamping. Do not create unsupported
  adjacent-month data at the bounds.
- Titles and screen-reader text include the leap-month marker. Keys include the full month
  identity.
- Formatting is not calculation: test English and Chinese labels, simplified/traditional
  characters and the numeric fallback separately. Do not put `java.util.Locale` types in the
  shared engine contract.
- Stateless composables; state only for navigation requested by the caller; no new
  ViewModel/Flow/DI architecture.

## Implementation references

- HKO conversion and uncertainties: https://www.hko.gov.hk/en/gts/time/conversion.htm
- HKO reference 2033: https://www.hko.gov.hk/en/gts/time/calendar/text/files/T2033e.txt
- Android ICU calendar conventions, development reference only:
  https://developer.android.com/reference/kotlin/android/icu/util/ChineseCalendar

A reference does not grant permission to redistribute. Check terms of use separately.
