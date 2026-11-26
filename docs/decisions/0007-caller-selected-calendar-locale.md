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
- Unknown language tags fall back to English. A locale value never changes layout direction by
  itself, and no calendar composable reads it for that purpose; the caller still provides
  `LayoutDirection`. (Amended below by the opt-in RTL helpers.)
- `:calendar-chinese-compose` keeps `ChineseTextStyle` as its language control because Chinese
  month and day labels are script choices, not translations.
- `GregorianChineseCalendarMonth` also accepts `locale`. The overlay's primary grid is a plain
  Gregorian month, so its title, weekday labels, day numbers and accessibility labels come from the
  Gregorian label tables; the Chinese secondary labels stay on `ChineseTextStyle` because a
  language tag cannot choose between Chinese scripts. `null` keeps the platform formatter that the
  overlay used before.
- Fallback semantics are deliberately simple: every unsupported tag (for example `es`, `sv` or an
  empty tag) renders English labels and Western digits. There is no fallback chain and no regional
  matching beyond the language subtag (`de-AT` is `de`); the only regional exception is Solar Hijri
  month names, where `fa-AF` and `prs` select the Dari list.

### Amendment: opt-in right-to-left derivation

The original statement "labels never change layout direction" was too absolute for hosts that
switch language at runtime: they had to maintain their own language-to-direction map. `:library`
now offers `CalendarLocale.isRightToLeft` (a pure Kotlin check over a fixed language list) and the
composable `ProvideCalendarLocaleLayoutDirection(locale) { ... }`, which overrides the ambient
`LayoutDirection` for its content only. The rule that changes is *who may decide*: the calendar
composables still never touch the direction, and the default is unchanged, but a host can opt in to
deriving the direction from the language instead of duplicating the mapping. `null` leaves the
ambient direction alone. The list ignores script subtags, so it is a language-level default, not a
full BCP 47 script resolution; a host with other needs keeps providing `LayoutDirection` itself.

### Amendment: parameter placement and the `3.0.0` overload

`GregorianChineseCalendarMonth` takes `locale` directly before `weekNumber`. `weekNumber` must
stay ahead of the trailing `weekNumberContent` composable slot, and the repository rule places new
defaulted options before trailing lambdas so existing trailing-lambda calls keep compiling;
appending `locale` after the slot would invert that order. Because the insertion changes the JVM
signature that `3.0.0` callers were compiled against, the previous parameter list stays available
as a `@Deprecated(HIDDEN)` overload that forwards with `locale = null`. Kotlin callers resolve to
the current function whether they pass arguments positionally or by name, and
`api/calendar-chinese-compose.api` lists both JVM signatures; the hidden overload exists only for
binary compatibility and is removed in `4.0`.

### Amendment: host-provided layout direction

A host that needs a different mapping than [isRightToLeft] simply keeps providing
`LayoutDirection` around the calendar itself (as both samples did before the helper existed) and
does not call `ProvideCalendarLocaleLayoutDirection`; the two mechanisms compose, and the inner
call wins. The helper is a language-level default, not a script resolver: it deliberately ignores
the script subtag, so `ckb` (Sorani), `az-Arab` or `pa-Arab` report `false` unless the host adds
its own direction.

### Amendment: convenience API

`CalendarLocale.supportedLanguages` documents the languages that have tables,
`CalendarLocale.systemDefault()` wraps the platform language (`Locale`, `NSLocale`, `Intl`), and
`java.util.Locale.asCalendarLocale()` is a JVM/Android extension, so hosts no longer carry their own
bridge. These add public API only; existing signatures are unchanged.

## Consequences

- Hosts can localize calendar content independently of the operating system, including on the
  web targets.
- Only the four built-in languages are translated; any other language renders English labels
  until a table is added.
- The library now ships label data for those languages, while the platform formatters remain the
  source for `null` and for anything not covered (for example first day of week, week numbers).

## Amendment: `fr`, `es` and `zh` tables

`CalendarLocale.supportedLanguages` now also lists `fr`, `es` and `zh`, with `French`, `Spanish` and
`Chinese` constants. Each engine keeps its own self-written table; nothing was copied from an
external source.

- **Date order:** `fr` writes `dimanche 1 novembre 2026`; `es` writes `domingo, 1 de noviembre de
  2026` and month captions as `noviembre de 2026`; `zh` writes `2026年11月1日星期日` and `2026年11月`.
  Digits stay Western in all three.
- **Hijri and Solar Hijri month names:** `fr` and `es` use Latin transliterations in each
  language's usual spelling; `zh` uses Chinese transliterations. Solar Hijri keeps the shared
  Latin transliteration (`Farvardin`, ...) for `fr` and `es`.
- **Review needed:** the Hijri and Solar Hijri names for `fr`, `es` and `zh` could not be verified
  against an authoritative source in this repository; a native speaker should review them before
  the next release.
- **Samples:** `:calendarviewsample` and `:kmp-sample` still ship their four translated languages
  through their own language lists; the library supports more than the samples translate.
- **Unsupported-tag tests:** the fallback tests now use `tr`, which remains outside the list.
