# 0007: Caller-selected calendar locale

## Context

ADR 0006 moved the shared calendar code to `commonMain` and removed explicit `Locale` parameters:
every engine formatter read the platform default locale (`java.util.Locale`, `NSLocale`,
`Intl`, or `SolarHijriPlatform.localeLanguage()`). That kept the engines dependency free, but it
made the rendered language uncontrollable for hosts: an app that offers its own language switch
cannot change month names, weekday labels or digits, because those come from the operating
system, and the JS/Wasm targets have no global default-locale switch at all. The multiplatform
sample showed the effect: its language setting translated the shell only.

## Decision

Add one public value type, `CalendarLocale(languageTag)` in `:library`
(`com.develotter.calendarview.locale`), with the predefined `English`, `German`, `Persian` and
`Arabic`.

- Every engine accepts `locale: CalendarLocale? = null` on its label-producing functions
  (`*MonthTitle`, `*SecondaryLabel`, `*AccessibilityLabel`, `*DayCell`, `*MonthData`, `*WeekData`,
  `*MonthState.title()` and the month composables). `null` keeps the platform default locale, so
  existing behaviour and the `3.0.0` binary-compatibility overloads are unchanged.
- The languages are implemented as pure Kotlin label tables inside each engine
  (`GregorianLabels`, `HijriLabels`, `SolarHijriLabels` and the existing `SolarHijriLocale`
  month lists), not by passing locale objects to platform formatters. This keeps the engine
  usable without Compose, Android or ICU, and gives JS, Wasm, Native and Apple the same labels.
- Unknown language tags fall back to English. Labels never change layout direction; the caller
  still provides `LayoutDirection`.
- `:calendar-chinese-compose` keeps `ChineseTextStyle` as its language control because Chinese
  month and day labels are script choices, not translations.

## Consequences

- Hosts can localize calendar content independently of the operating system, including on the
  web targets.
- Only the four built-in languages are translated; any other language renders English labels
  until a table is added.
- The library now ships label data for those languages, while the platform formatters remain the
  source for `null` and for anything not covered (for example first day of week, week numbers).
