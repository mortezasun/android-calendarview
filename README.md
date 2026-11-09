![https://jitpack.io/#mortezasun/android-calendarview/1.0.0-alpha](https://jitpack.io/v/mortezasun/calendarview.svg)

# 📅 Custom CalendarView for Android (ViewBinding)

A powerful and flexible calendar widget for Android applications. Supports multiple views, date range selection, custom styling using ViewBinding, and much more.<br>
**You can create any type of calendar you want with this view.**

![image](https://github.com/user-attachments/assets/d1acc817-a80c-423b-824f-58bc39eff198)<br>
![image](https://github.com/user-attachments/assets/16d427bc-1878-4625-852e-08bbe3d1416b)

---

## ✨ Features

- Month, Week, and Day view
- Single, Multiple, and Range date selection
- Fully customizable styles (colors, fonts, icons)
- Localization & custom first day of week
- Click listeners support
- Dark mode compatible


## 📅  Calendar Support

| Calendar        | Support  |  Source  |
|-----------------|-------------|-------------|
| Gregorian (Julian)      | ✅          | [java.time.LocalDate](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/LocalDate.html) <br> [java.time.YearMonth](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/YearMonth.html ) |
|  Hijri (Islamic)| ✅          | [java.time.chrono.HijrahDate](https://docs.oracle.com/en/java/javase/17/docs/api//java.base/java/time/chrono/HijrahDate.html)             |
| Solar Hijri <br> Jalali (Persian)| ✅          | [Jalali Calendar from Mohammad Razeghi](https://github.com/razeghi71/JalaliCalendar)              |


---

## 🧱 Repository layout

| Module | Namespace | Purpose |
|--------|-----------|---------|
| `:library` | `com.develotter.calendarview` | Core views, adapter contracts, status models and heatmap views. |
| `:calendar-gregorian` | `com.develotter.calendarview.calendars.gregorian` | Gregorian month/day implementation. |
| `:calendar-hijri` | `com.develotter.calendarview.calendars.hijri` | Islamic (Hijri) implementation based on `HijrahDate`. |
| `:calendar-solarHijri` | `com.develotter.calendarview.calendars.solarHijri` | Solar Hijri (Jalali) implementation based on `ir.huri:jalalicalendar`. |
| `:calendarviewsample` | `com.develotter.calendarviewsample` | Demo app for configuration, selection modes and the heatmap. |

The calendar modules depend on `:library`; the sample app depends on all of them. There is no
dependency in the other direction.

## 🛠 Tech stack

- Kotlin 2.4.21 (declared in the version catalog; compilation uses AGP 9's built-in Kotlin support)
- Android Gradle Plugin 9.5.1, Gradle 9.8.2
- `compileSdk`/`targetSdk` 37, `minSdk` 27, Java 17
- XML layouts + ViewBinding, RecyclerView, Material Components
- No Jetpack Compose, no ViewModel/Flow, no dependency-injection framework

---

## 📦 Installation

This library is available on **JitPack**. Add it to your project in a few simple steps:

### Step 1: Add JitPack to your root `build.gradle` (or `settings.gradle` for newer Gradle versions)

```gradle
// For old versions
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}

// For Gradle 7.0 and above (settings.gradle.kts)
	dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url = uri("https://jitpack.io") }
		}
	}
```
### Step 2: Add the dependency

```gradle
dependencies {
    implementation 'com.github.mortezasun:calendarview:1.1.0-alpha2'
}
```

### Step 3: Using in Code for Calendar
```xml
<com.develotter.calendarview.MultiMonthView
        android:id="@+id/calendar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        />
```
```Kotlin
 var thisCalendarStatus = CalendarStatus()
 binding.calendar.addMonths(GregorianStatus::class.java,object,object :
            MonthSampleAdapter<RowDayBinding, RowWeekBinding, RowMonthBinding, RowShowSelectedDayBinding>(thisCalendarStatus){}
```

### Optional
## for Activating Day before show Calendar
```Kotlin
  var dayStatusListSelectedBySingleSelect: MutableList<DayStatus> = mutableListOf()
  dayStatusListSelectedBySingleSelect.add(0, object :DayStatus(LocalDate.now(), lcInUse){})
  binding.calendar.addMonths(GregorianStatus::class.java,object,object :
            MonthSampleAdapter<RowCalendarBinding, RowCalendarBinding, RowMonthBinding, RowShowSelectedDayBinding>
                    (thisCalendarStatus,dayStatusListSelectedBySingleSelect=dayStatusListSelectedBySingleSelect ))
```

---

## 🔨 Building from source

Requirements: JDK 17, Android SDK Platform 37 and Build-Tools 36.0.0.

```bash
# Linux/macOS; the repository may be checked out without the executable bit
chmod +x gradlew

# Build the demo app and every library module it depends on
./gradlew :calendarviewsample:assembleDebug

# APK output
# calendarviewsample/build/outputs/apk/debug/calendarviewsample-debug.apk

# JVM unit tests (currently template placeholders, see docs/PROJECT_STATUS.md)
./gradlew test

# Lint for the core library
./gradlew :library:lintDebug
```

Detailed environment notes, known setup problems and the verified toolchain are documented in
[docs/SETUP.md](docs/SETUP.md).

---

## 📚 Documentation

- [docs/SETUP.md](docs/SETUP.md) - development environment, build and test setup
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) - modules, rendering pipeline, state handling
- [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) - where and how to make typical changes
- [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md) - last verified state, known issues, next steps
- [docs/decisions/0001-generic-viewbinding-calendar-adapters.md](docs/decisions/0001-generic-viewbinding-calendar-adapters.md) - why the library is generic over ViewBindings
- [AGENTS.md](AGENTS.md) - working rules for coding agents

---

## 📄 License

Apache License 2.0, see [LICENSE](LICENSE).
