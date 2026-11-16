# Android demo app

`:calendarviewsample` is the runnable **Android-only** demo for the calendar library. The
library itself is Kotlin Multiplatform (Android, JVM and iOS), but this application uses the
Android application plugin and is not published as an artifact. There is no JVM or iOS demo app
in this repository.

The app is useful for a manual check of rendering, selection, navigation and accessibility. A CLI
build is not a substitute for looking at the screens.

## Build and install

Requirements: JDK 17+, Android SDK Platform 37, Build-Tools 36.0.0 or 37.0.0, and an Android
device or emulator with API 27 or newer.

```bash
./gradlew :calendarviewsample:assembleDebug
adb install -r calendarviewsample/build/outputs/apk/debug/calendarviewsample-debug.apk
```

The launcher activity is `LauncherActivity`. The heatmap screen is `HeatMapViewActivity`; open it
from the **Compose heatmap demo** button in the main screen.

## What the app covers

The main screen can switch between all five calendar entries:

| Entry | What it demonstrates |
|---|---|
| `Gregorian` | Gregorian month grid, month header, week and agenda views. |
| `Hijri` | Umm al-Qura month view with an offset method (`-1`, default, `+1` days), week and agenda data. |
| `SolarHijri` | The facade over the dependency-free Solar Hijri engine. |
| `SolarHijriNative` | The dependency-free native Solar Hijri engine directly. |
| `Chinese` | Chinese lunisolar month with leap months, Chinese labels, a Gregorian grid with Chinese secondary labels and a details panel. |

Shared controls and views include:

- selection modes `None`, `Single`, `Multiple` and `Range`, including a 2–14 day range policy;
- previous/next month, today, month bounds and the `CalendarMonthHeader` API;
- fixed six-row grids, previous/next-month days, week numbers and event markers;
- locale choices, RTL layout direction, custom style and the Hijri method offset;
- month list and pager containers, a week grid and an agenda;
- adaptive side-by-side panes on wide windows, fold/posture-aware sample geometry and adaptive
  dialog height;
- saved selection, display settings and visible month across activity recreation;
- Gregorian, Hijri, Solar Hijri and Chinese heatmaps with orientation, palette, cell style,
  today border, labels, disabled future days and selection modes.

The Chinese demo badge and the heatmap screen are demo-only wiring in this module; they are not
part of the published API.

## Tests

Unit tests for the sample's state, geometry, navigation and heatmap helpers:

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

The CI emulator workflows run both the library and sample suites (44 tests: 18 library and 26
sample) on API 29 phone and tablet emulators. See
[../docs/COMPOSE_TESTS.md](../docs/COMPOSE_TESTS.md),
[../docs/SAMPLE_UI_TESTS.md](../docs/SAMPLE_UI_TESTS.md) and
[../docs/CI.md](../docs/CI.md).

## Limits

- Android only. The shared library modules build for JVM and iOS, but this sample does not.
- The emulator suites are not a physical-device, foldable or TalkBack acceptance matrix.
- The sample is a development tool; it has no persistence, network or production data layer.
