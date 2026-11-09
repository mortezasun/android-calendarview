![https://jitpack.io/#mortezasun/android-calendarview/2.0.0](https://jitpack.io/v/mortezasun/calendarview.svg)

# 📅 Native Jetpack Compose CalendarView for Android (v2.0.0)

A powerful, native Jetpack Compose calendar library for Android applications. Supports multi-calendar engines (Gregorian, Hijri, Solar Hijri / Jalali), unified selection models, configurable month grids, heatmap components, and complete RTL layout direction support.

---

## ✨ Features

- **100% Native Jetpack Compose:** Clean declarative API without XML, ViewBinding, or reflection.
- **Multi-Calendar Support:**
  - Gregorian (`java.time.YearMonth`, `java.time.LocalDate`)
  - Islamic Hijri (`java.time.chrono.HijrahDate`)
  - Persian Solar Hijri / Jalali (`ir.huri.jcal.JalaliCalendar`)
- **Flexible Selection Engine:** None, Single, Multiple, and Range selection with persistent state across process recreations (`rememberSaveable`).
- **Configurable Month Grids:**
  - Dynamic rows or fixed 6-row (42 cells) display
  - Independently toggleable adjacent previous/next month days
  - Header and weekday visibility controls
  - Built-in focus indicators for today
- **Heatmap Components:** `CalendarQuarterHeatmap` and `CalendarYearHeatmap` for activity visualizations.
- **Navigation & Containers:** `HorizontalPager`, `VerticalPager`, `HorizontalList`, and `VerticalList` synchronizable viewports.
- **Fully RTL-Compatible:** Naturally inherits ambient `LayoutDirection`.

---

## 🧱 Repository layout

| Module | Namespace | Purpose |
|--------|-----------|---------|
| `:library` | `com.develotter.calendarview` | Core Compose grids, selection engine, heatmap components, and containers. |
| `:calendar-gregorian` | `com.develotter.calendarview.calendars.gregorian` | Gregorian calendar engine backed by `java.time`. |
| `:calendar-hijri` | `com.develotter.calendarview.calendars.hijri` | Islamic Hijri calendar engine backed by `java.time.chrono.HijrahDate`. |
| `:calendar-solarHijri` | `com.develotter.calendarview.calendars.solarHijri` | Solar Hijri / Jalali engine backed by `ir.huri:jalalicalendar`. |
| `:calendarviewsample` | `com.develotter.calendarviewsample` | Interactive Compose demo application. |

---

## 🛠 Tech stack

- Kotlin 2.4.21, Jetpack Compose (BOM 2026.09.00), Compose Material 3
- Android Gradle Plugin 9.5.1, Gradle 9.8.2
- `compileSdk`/`targetSdk` 37, `minSdk` 27, Java 17

---

## 📦 Installation

Add JitPack to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add dependencies to your module's `build.gradle.kts`:

```kotlin
dependencies {
    // Core selection engine and Compose month grid
    implementation("com.github.mortezasun.android-calendarview:library:2.0.0")

    // Choose the calendar engines you need:
    implementation("com.github.mortezasun.android-calendarview:calendar-gregorian:2.0.0")
    implementation("com.github.mortezasun.android-calendarview:calendar-hijri:2.0.0")
    implementation("com.github.mortezasun.android-calendarview:calendar-solarHijri:2.0.0")
}
```

---

## 🚀 Quick Start (Compose)

### 1. Gregorian Calendar Month

```kotlin
@Composable
fun MyCalendarScreen() {
    val selectionState = rememberCalendarSelectionState(TypeSelectDay.Range)

    GregorianCalendarMonth(
        month = YearMonth.now(),
        selection = selectionState.selection,
        onDateClick = { date -> selectionState.onDayClick(date.toEpochDay()) },
        showPreviousMonthDays = true,
        showNextMonthDays = true,
        fixedSixRows = true,
    )
}
```

### 2. Hijri (Islamic) Calendar Month

```kotlin
@Composable
fun MyHijriScreen() {
    val selectionState = rememberCalendarSelectionState(TypeSelectDay.Single)

    HijriCalendarMonth(
        month = HijrahDate.now(),
        selection = selectionState.selection,
        onDateClick = { hijrahDate -> selectionState.onDayClick(hijrahDate.toEpochDay()) },
    )
}
```

### 3. Solar Hijri / Jalali (Persian) Calendar Month

```kotlin
@Composable
fun MySolarHijriScreen() {
    val selectionState = rememberCalendarSelectionState(TypeSelectDay.Multiple)

    SolarHijriCalendarMonth(
        month = JalaliCalendar(),
        selection = selectionState.selection,
        onDateClick = { jalaliCalendar ->
            selectionState.onDayClick(solarHijriEpochDay(jalaliCalendar))
        },
    )
}
```

### 4. Year Heatmap

```kotlin
@Composable
fun MyHeatmapScreen(activityIntensities: Map<LocalDate, Float>) {
    CalendarYearHeatmap(
        year = 2026,
        intensities = activityIntensities,
        onDayClick = { date -> println("Clicked $date") },
    )
}
```

---

## 🔨 Building from source

Requirements: JDK 17, Android SDK Platform 37 and Build-Tools 36.0.0.

```bash
chmod +x gradlew
./gradlew :calendarviewsample:assembleDebug
./gradlew test
./gradlew :library:lintDebug
```

---

## 📚 Documentation

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) - Compose architecture, rendering, and state model
- [docs/V2_MIGRATION_MAPPING.md](docs/V2_MIGRATION_MAPPING.md) - 1.x to 2.0.0 migration matrix
- [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md) - Verified status, test coverage, and release notes
- [AGENTS.md](AGENTS.md) - Working rules for coding agents

---

## 📄 License

Apache License 2.0, see [LICENSE](LICENSE).
