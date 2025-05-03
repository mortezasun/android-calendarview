![https://jitpack.io/#mortezasun/calendarview/1.0.0-alpha](https://jitpack.io/v/mortezasun/calendarview.svg)

# 📅 Custom CalendarView for Android

A powerful and flexible calendar widget for Android applications. Supports multiple views, date range selection, custom styling using ViewBinding, and much more.


---

## ✨ Features

- Month, Week, and Day view
- Single, Multiple, and Range date selection
- Fully customizable styles (colors, fonts, icons)
- Localization & custom first day of week
- Click listeners support
- Dark mode compatible


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
    implementation 'com.github.mortezasun:calendarview:1.0.0-alpha3'
}
