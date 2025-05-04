![https://jitpack.io/#mortezasun/calendarview/1.0.0-alpha](https://jitpack.io/v/mortezasun/calendarview.svg)

# 📅 Custom CalendarView for Android (ViewBinding)

A powerful and flexible calendar widget for Android applications. Supports multiple views, date range selection, custom styling using ViewBinding, and much more.

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
| Gregorian        | ✅          | [java.time.LocalDate](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/LocalDate.html) <br> [java.time.YearMonth](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/YearMonth.html ) |
| Jalali (Persian)| ✅          | [Jalali Calendar from Mohammad Razeghi](https://github.com/razeghi71/JalaliCalendar)              | 


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
    implementation 'com.github.mortezasun:calendarview:1.0.0-alpha7'
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
 binding.calendar.addMonths(object :
            MonthSampleAdapter<RowDayBinding, RowWeekBinding, RowMonthBinding, RowShowSelectedDayBinding>(thisCalendarStatus){}
```

### Optional
## for Activating Day before show Calendar
```Kotlin
  var dayStatusListSelectedBySingleSelect: MutableList<DayStatus> = mutableListOf()
  dayStatusListSelectedBySingleSelect.add(0, object :DayStatus(LocalDate.now(), lcInUse){})
  binding.calendar.addMonths(object :
            MonthSampleAdapter<RowCalendarBinding, RowCalendarBinding, RowMonthBinding, RowShowSelectedDayBinding>
                    (thisCalendarStatus,dayStatusListSelectedBySingleSelect=dayStatusListSelectedBySingleSelect ))
```

