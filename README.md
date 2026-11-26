<p align="center">
  <img src="docs/images/banner.svg" alt="CalendarView Compose Banner" width="100%"/>
</p>

<p align="center">
  <a href="https://jitpack.io/#mortezasun/android-calendarview"><img src="https://jitpack.io/v/mortezasun/android-calendarview.svg" alt="JitPack Release"/></a>
  <a href="https://github.com/mortezasun/android-calendarview/actions/workflows/android-verification.yml"><img src="https://github.com/mortezasun/android-calendarview/actions/workflows/android-verification.yml/badge.svg?branch=master" alt="Android verification"/></a>
  <a href="https://github.com/mortezasun/android-calendarview/actions/workflows/android-instrumentation.yml"><img src="https://github.com/mortezasun/android-calendarview/actions/workflows/android-instrumentation.yml/badge.svg?branch=master" alt="Android instrumentation"/></a>
  <img src="https://img.shields.io/badge/Platform-Android%20%7C%20JVM%20%7C%20iOS%20%7C%20macOS%20%7C%20JS%20%7C%20Wasm-green.svg" alt="Platform: Android, JVM, iOS, macOS, JS and Wasm"/>
  <img src="https://img.shields.io/badge/minSdk-27-brightgreen.svg" alt="minSdk 27"/>
  <img src="https://img.shields.io/badge/License-Apache%202.0-orange.svg" alt="License"/>
</p>

A **Kotlin Multiplatform** Jetpack Compose calendar library built around a shared, calendar-independent core (`CalendarMonthGrid`, epoch-day selection engine, month containers, product views, heatmaps) and pluggable calendar engines for **Gregorian**, **Islamic Hijri**, **Solar Hijri (Jalali / Persian)**, and the **Chinese lunisolar calendar** — with full RTL support.

- **Guides:** [Getting Started](docs/GETTING_STARTED.md) · [Usage Guide](docs/USAGE_GUIDE.md) · [Architecture](docs/ARCHITECTURE.md) · [Project Status](docs/PROJECT_STATUS.md)
- **Latest Stable Release (`3.0.0`):** Released via [ADR 0006](docs/decisions/0006-kotlin-multiplatform-calendar-modules.md) ([Release notes](docs/releases/3.0.0.md) · [Changelog](docs/releases/3.0.0-changelog.md)).
- **`3.1.0-beta` Pre-Release Line (Pending Tag):** Expands KMP targets (`JVM`, `macOS`, `JS`, `Wasm`, plus `Linux`/`Windows` on `:calendar-chinese-native`) on Kotlin `2.5.0-RC`, Compose Multiplatform `1.13.0-beta02`, AGP `9.5.1`, Gradle `9.9.1`, Compose BOM `2026.09.00` (see [Project Status](docs/PROJECT_STATUS.md) and [Kotlin 2.5 Upgrade Notes](docs/KOTLIN_2_5_UPGRADE.md) for verified vs. hardware-pending platforms).

---

## ✨ Features

- **100% Jetpack Compose** – Declarative month grids, scrolling containers, heatmaps, and product views with no XML layouts or reflection.
- **Four Calendar Systems (Five Engine Entries)** – Gregorian (`GregorianYearMonth`), Hijri (`HijriDate` with pluggable `HijriDateProvider`), Solar Hijri / Jalali (`SolarHijriDate` via a dependency-free Borkowski engine), and Chinese lunisolar (`ChineseCalendarEngine`, pure Kotlin across `1901..2099` with explicit leap months).
- **Dynamic Alternative Calendar Labels** – Pair any primary calendar with any secondary calendar (`gregorianSecondaryLabel`, `hijriSecondaryLabel`, `solarHijriSecondaryLabel`, `solarHijriNativeSecondaryLabel`, `chineseSecondaryLabel` + matching accessibility & title helpers) in `Month`, `CalendarWeekGrid`, and `CalendarAgenda`.
- **Universal Epoch-Day Selection** – `None`, `Single`, `Multiple`, and `Range` modes (with optional `CalendarRangePolicy`) keyed by `Long` epoch days and persisted with `rememberSaveable`. `CalendarSelection.presets(firstDayOfWeek, todayEpochDay)` derives today, this week (honoring the configured first weekday), this month and the last seven days on the same epoch-day identity, with no platform date access.
- **Range Drag and Day Styling Helpers** – `CalendarRangeDrag` is pure drag state with policy/window validation and `CalendarSelectionState.selectRange(drag, window)`; `calendarDayContent` / `CalendarDayAppearance` style weekends or caller-owned holidays through the existing `dayContent` slot.
- **Day View** – `CalendarDayView` renders a stateless, calendar-independent hour timeline from `CalendarDayEvent(startMinute, endMinute)` values, with automatic side-by-side layout of overlapping events (`layoutDayEvents`).
- **Selectable-Day Window** – `CalendarDateWindow(minEpochDay, maxEpochDay)` limits where days may be selected and plugs into every `isDateEnabled` parameter; `CalendarRangePolicy` still limits the range length.
- **Stateless & Controlled** – Callers own selection state and visible month bounds via saveable engine `*MonthState` holders and `CalendarMonthHeader`.
- **Rich Product Views** – `CalendarWeekGrid`, `CalendarAgenda`, `CalendarMonthList`, `CalendarMonthPager`, `CalendarMonthColumns` (with optional `maxColumns`, so extra months wrap into further rows), event markers, optional week numbers, `LocalCalendarStyle`, custom `dayContent`, `CalendarMonthData` / `CalendarWeekData` overloads, and `toHeatmap()` adapters.
- **Stepped Heatmaps and Legend** – `HeatmapBuckets` snaps the `colorForIntensity` lookup to ascending thresholds (`buckets = HeatmapBuckets(listOf(0.25f, 0.5f, 0.75f))`), and `HeatmapLegend` renders the same steps with caller-supplied labels and descriptions. Accessibility labels and stored intensities keep the real percentages.
- **Stateless Month Picker** – `CalendarMonthPicker` renders one year of selectable month cells with grid semantics, keyboard activation and layout-direction-aware layout; the host keeps the dialog frame, the visible year and all localized labels ([ADR 0008](docs/decisions/0008-calendar-month-picker.md)).
- **Accessibility & RTL** – Localized date/marker descriptions (including merged secondary-calendar descriptions), collection/heading semantics, and automatic RTL mirroring via `LocalLayoutDirection`.
- **Opt-in Keyboard Navigation** – `CalendarMonthGrid(keyboardNavigation = true)` adds arrow-key day/week movement (layout-direction aware), Page Up/Page Down month steps through `onMonthStep`, Enter/Space activation and a visible focus border, without changing the default semantics or any calendar engine. All five engine month composables and `GregorianChineseCalendarMonth` forward the same two parameters, so hosts rarely need the grid directly; see the [usage guide](docs/USAGE_GUIDE.md#keyboard-navigation).
- **Caller-Selected Locale** – `CalendarLocale` (`en`, `de`, `fa`, `ar`, `fr`, `es`, `zh`, or any tag that falls back to English) drives month titles, weekday labels, day numbers and accessibility labels in every engine; `null` keeps the platform default locale, so calendar content can follow an in-app language switch on every target. Chinese labels stay on `ChineseTextStyle`, and layout direction stays a host decision (`CalendarLocale.isRightToLeft` and the opt-in `ProvideCalendarLocaleLayoutDirection` help derive it); see the [language guide](docs/USAGE_GUIDE.md#language-and-locale) and [ADR 0007](docs/decisions/0007-caller-selected-calendar-locale.md).

---

## 🧱 Modules & Platform Targets

| Module | Role | Configured Targets |
|--------|------|--------------------|
| `:library` | Stateless core UI, selection engine, containers, heatmaps, shared contracts | `android`, `jvm`, `iosArm64`, `iosSimulatorArm64`, `macosArm64`, `js`, `wasmJs` |
| `:calendar-gregorian` | Gregorian engine & `GregorianCalendarMonth` | `android`, `jvm`, `iosArm64`, `iosSimulatorArm64`, `macosArm64`, `js`, `wasmJs` |
| `:calendar-hijri` | Hijri engine, `HijriDateProvider` & `HijriCalendarMonth` | `android`, `jvm`, `iosArm64`, `iosSimulatorArm64`, `macosArm64`, `js`, `wasmJs` |
| `:calendar-solarHijri` | Compose facade over `:calendar-solarHijri-native` | `android`, `jvm`, `iosArm64`, `iosSimulatorArm64`, `macosArm64`, `js`, `wasmJs` |
| `:calendar-solarHijri-native` | Dependency-free Solar Hijri engine & `SolarHijriNativeCalendarMonth` | `android`, `jvm`, `iosArm64`, `iosSimulatorArm64`, `macosArm64`, `js`, `wasmJs` |
| `:calendar-chinese-native` | Pure Kotlin lunisolar engine (no Compose/UI dependencies) | `jvm`, `linuxX64`, `linuxArm64`, `mingwX64`, `iosX64`, `iosArm64`, `iosSimulatorArm64`, `macosArm64`, `js`, `wasmJs` |
| `:calendar-chinese-compose` | Compose facade (`ChineseCalendarMonth`, `GregorianChineseCalendarMonth`, heatmaps) | `android`, `jvm`, `iosArm64`, `iosSimulatorArm64`, `macosArm64`, `js`, `wasmJs` |
| `:calendar-bom` | `java-platform` version constraints for the seven published artifacts | – |
| `:benchmarks` | Standalone JVM kotlinx-benchmark smoke suite (unpublished) | `jvm` |
| `:calendarviewsample` | Interactive Android demo app showcasing all calendars & views (unpublished) | `android` |

> [!NOTE]
> Linux hosts cannot execute iOS/macOS (`ios*`, `macosArm64`) or Windows (`mingwX64`) binaries at runtime; those targets are cross-compiled/KLIB-checked on Linux and require Apple/Windows hardware for native execution. See [Project Status](docs/PROJECT_STATUS.md) for the exact verification matrix.

---

## 🚀 Quick Start

Add the BOM and calendar dependencies in `build.gradle.kts` (see [Getting Started](docs/GETTING_STARTED.md) for JitPack repository setup, KMP `commonMain` configuration, and all example projects):

```kotlin
dependencies {
    implementation(platform("com.github.mortezasun.android-calendarview:calendar-bom:3.0.0"))
    implementation("com.github.mortezasun.android-calendarview:calendar-gregorian")
}
```

Render a calendar month with saved selection in Compose:

```kotlin
@Composable
fun GregorianScreen() {
    val state = rememberCalendarSelectionState(TypeSelectDay.Range)

    GregorianCalendarMonth(
        month = GregorianYearMonth.now(),
        selection = state.selection,
        onDateClick = { epochDay -> state.onDayClick(epochDay) },
        fixedSixRows = true,
    )
}
```

Or use the pure Kotlin lunisolar engine without Compose or Android:

```kotlin
val newYear = ChineseDate.of(2024, monthNumber = 1, isLeapMonth = false, dayOfMonth = 1)
val leapMonth = ChineseCalendarEngine.leapMonth(2023) // ChineseMonth(2023, 2, leap = true)
```

For full examples across Hijri, Solar Hijri, Chinese, week/agenda views, month navigation, adaptive columns, styling, containers, heatmaps, and custom grids, read the **[Usage Guide](docs/USAGE_GUIDE.md)**.

---

## 📚 Documentation

| Document | Content |
|----------|---------|
| [Getting Started](docs/GETTING_STARTED.md) | Installation, KMP setup, BOM, checked-in examples, upgrading (`2.4.0` → `3.0.0` / `1.x` → `2.0.0`), and building from source |
| [Usage Guide](docs/USAGE_GUIDE.md) | Complete examples for all 5 calendar engines, selection modes, navigation, week/agenda, markers, adaptive columns, styling, heatmaps, and `CalendarMonthGrid` |
| [Architecture](docs/ARCHITECTURE.md) | Module graph, rendering pipeline, epoch-day state, product views, and distribution layout |
| [Project Status](docs/PROJECT_STATUS.md) | Verified checks, CI/JitPack history, and open platform/hardware gates |
| [Kotlin 2.5 Upgrade](docs/KOTLIN_2_5_UPGRADE.md) | Kotlin `2.5.0-RC` / Compose Multiplatform `1.13.0-beta02` preparation notes and checklist |
| [Setup](docs/SETUP.md) · [Development](docs/DEVELOPMENT.md) | Local SDK toolchain, API/KLIB/purity guards, and contribution conventions |
| [CI Workflows](docs/CI.md) · [Benchmarks](docs/BENCHMARKS.md) | GitHub Actions verification/publishing pipelines and JVM performance baseline |
| [Chinese Calendar Spec](docs/CHINESE_CALENDAR_SPEC.md) · [Quality](docs/CHINESE_CALENDAR_QUALITY.md) | Lunisolar table provenance, API contract, and test matrix |
| [Adaptive Layout](docs/ADAPTIVE_LAYOUT.md) · [Containers](docs/COMPOSE_MONTH_CONTAINERS.md) · [Adjacent Days](docs/ADJACENT_MONTH_DAYS.md) | Responsive columns, lazy/pager contracts, and adjacent-month day rules |
| [Compose Tests](docs/COMPOSE_TESTS.md) · [Sample UI Tests](docs/SAMPLE_UI_TESTS.md) · [Instrumentation Results](docs/INSTRUMENTATION_RESULTS.md) | Compose instrumentation test coverage and emulator results |
| [Sample Demo README](calendarviewsample/README.md) | Running and testing the Android sample application |
| [Decisions (ADRs)](docs/decisions) · [AGENTS.md](AGENTS.md) | Architecture decisions (including [ADR 0006](docs/decisions/0006-kotlin-multiplatform-calendar-modules.md)) and coding agent rules |

---

## 📄 License

```
Copyright (c) mortezasun

Licensed under the Apache License, Version 2.0.
```

See [LICENSE](LICENSE) for the full text.
