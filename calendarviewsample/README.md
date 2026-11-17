# Android demo app

`:calendarviewsample` is the runnable **Android-only** demo for the calendar library. The
library itself is Kotlin Multiplatform (`Android`, `JVM`, `iOS`, `macOS`, `JS`, `Wasm`, plus
`Linux`/`Windows` targets on the pure `:calendar-chinese-native` engine), but this application uses
the Android application plugin and is not published as an artifact. There is no desktop, iOS, or
browser demo app in this repository.

The app is useful for a manual check of rendering, selection, navigation and accessibility. A CLI
build is not a substitute for looking at the screens.

## Build and install

Requirements: JDK 17+, Android SDK Platform 37.1 (`platforms;android-37.1`), Build-Tools 36.0.0 or
37.0.0, and an Android device or emulator with API 27 or newer.

```bash
./gradlew :calendarviewsample:assembleDebug
adb install -r calendarviewsample/build/outputs/apk/debug/calendarviewsample-debug.apk
```

The launcher activity is `LauncherActivity`. The heatmap screen is `HeatMapViewActivity`; open it
from the **Compose heatmap demo** button in the main screen.

## What the app covers

The launcher activity (`LauncherActivity`) hosts a Material 3 adaptive navigation shell
(`NavigationBar` on compact/medium widths, `NavigationRail` on expanded screens) with four focused
destinations:

- **Home:** Overview of the library, live **"Today Across All Calendars"** epoch-day conversions, quick links into each calendar module, and the full capability catalog.
- **Calendars:** Dedicated explorer for each published module (`Gregorian`, `Hijri`, `SolarHijriNative`, `SolarHijri`, `Chinese`), calendar-specific options (Hijri offset, Chinese scripts & 2023 leap month, Solar Hijri leap-year status), **Alternative Secondary Calendar** pairing (`None` + all 5 calendars), a `SingleChoiceSegmentedButtonRow` (`Month` | `Week` | `Agenda`), and a live Selected Date Inspector.
- **Playground:** Full combined workbench preserving all selection modes, `CalendarRangePolicy`, `CalendarMonthList` / `CalendarMonthPager` / `CalendarMonthColumns`, `CalendarMonthHeader`, custom `CalendarStyle`, RTL switching, dynamic secondary calendars, dialog previews, and `CN Leap`.
- **Heatmaps:** Integrated contribution heatmap explorer (also directly launchable as `HeatMapViewActivity`) supporting Gregorian, Hijri, Solar Hijri, and Chinese datasets with `WeeksAsColumns` / `WeeksAsRows` orientation, color palettes, and cell styles.

The five calendar entries demonstrated across `Calendars` and `Playground`:

| Entry | What it demonstrates |
|---|---|
| `Gregorian` | Gregorian month grid, month header, adjacent days, ISO week numbers, week and agenda views. |
| `Hijri` | Umm al-Qura month view with an offset method (`-1`, default, `+1` days), week and agenda data. |
| `SolarHijriNative` | Dependency-free Borkowski native Solar Hijri engine with astronomical leap-year status. |
| `SolarHijri` | Compatibility facade over `:calendar-solarHijri-native`. |
| `Chinese` | Chinese lunisolar month with leap months (`2023 Leap 2nd Month`), 4 text scripts, Gregorian overlay, and date details. |

## Tests

Unit tests for the sample's state, geometry, secondary-calendar helpers, navigation and heatmap helpers:

```bash
./gradlew :calendarviewsample:testDebugUnitTest
```

Compile the Compose instrumentation tests without a device:

```bash
./gradlew :calendarviewsample:compileDebugAndroidTestKotlin
```

Run the instrumentation suite on a connected device or emulator:

```bash
./gradlew :calendarviewsample:connectedDebugAndroidTest
```

See
[../docs/COMPOSE_TESTS.md](../docs/COMPOSE_TESTS.md),
[../docs/SAMPLE_UI_TESTS.md](../docs/SAMPLE_UI_TESTS.md) and
[../docs/CI.md](../docs/CI.md).

## Limits

- Android only. The shared library modules build for JVM, Apple (`iOS`/`macOS`), and Web
  (`JS`/`Wasm`), but this sample application does not.
- The emulator suites are not a physical-device, foldable or TalkBack acceptance matrix.
- The sample is a development tool; it has no persistence, network or production data layer.
