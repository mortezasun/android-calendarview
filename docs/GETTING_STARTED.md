# Getting Started

This guide covers installation, platform coordinates, example projects, upgrading from earlier
releases, and building from source.

For Compose usage examples across all calendar engines, selection modes, heatmaps, and containers,
see the [Usage Guide](USAGE_GUIDE.md).

---

## Installation

Add JitPack to `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### Android or JVM dependencies

Add the engines you need to your module's `build.gradle.kts`:

```kotlin
dependencies {
    // Core only (needed if you build your own calendar on top of CalendarMonthGrid)
    implementation("com.github.mortezasun.android-calendarview:library:3.0.0")

    // Compose calendar engines (each one already exposes :library via api)
    implementation("com.github.mortezasun.android-calendarview:calendar-gregorian:3.0.0")
    implementation("com.github.mortezasun.android-calendarview:calendar-hijri:3.0.0")
    implementation("com.github.mortezasun.android-calendarview:calendar-solarHijri:3.0.0")
    // Dependency-free native Solar Hijri engine + Compose UI:
    implementation("com.github.mortezasun.android-calendarview:calendar-solarHijri-native:3.0.0")

    // Chinese lunisolar pure KMP engine and optional Compose facade:
    implementation("com.github.mortezasun.android-calendarview:calendar-chinese-native:3.0.0")
    implementation("com.github.mortezasun.android-calendarview:calendar-chinese-compose:3.0.0")
}
```

### Kotlin Multiplatform `commonMain` dependencies

In a Kotlin Multiplatform module, add the same coordinates inside `commonMain.dependencies` (or in
specific intermediate source sets such as `nonAndroidMain` / `webMain`):

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.github.mortezasun.android-calendarview:calendar-gregorian:3.0.0")
            implementation("com.github.mortezasun.android-calendarview:calendar-hijri:3.0.0")
            implementation("com.github.mortezasun.android-calendarview:calendar-solarHijri-native:3.0.0")
            implementation("com.github.mortezasun.android-calendarview:calendar-chinese-compose:3.0.0")
        }
    }
}
```

### Bill of Materials (BOM)

The BOM constrains all seven published modules at the same version (`3.0.0` for the latest stable
JitPack release; `3.1.0-beta` on the Kotlin `2.5.0-RC` multi-target pre-release line once tagged):

```kotlin
dependencies {
    implementation(platform("com.github.mortezasun.android-calendarview:calendar-bom:3.0.0"))
    implementation("com.github.mortezasun.android-calendarview:calendar-gregorian")
    implementation("com.github.mortezasun.android-calendarview:calendar-hijri")
    implementation("com.github.mortezasun.android-calendarview:calendar-solarHijri")
    implementation("com.github.mortezasun.android-calendarview:calendar-solarHijri-native")
    implementation("com.github.mortezasun.android-calendarview:calendar-chinese-native")
    implementation("com.github.mortezasun.android-calendarview:calendar-chinese-compose")
}
```

The BOM is part of the same publication set as the library modules and is included by `jitpack.yml`
and the publishing workflow. If you only need one engine, keep its explicit version and omit the
BOM. Note that Maven Central signing, namespace verification, and upload are not configured;
JitPack (or `publishToMavenLocal` for pre-tag/local testing of `3.1.0-beta`) is the active
distribution path.

---

## Checked-in Examples

The repository contains three runnable/compilable example paths:

1. **Android Demo App ([`calendarviewsample`](../calendarviewsample/README.md)):**
   Covers all five calendar modes (`Gregorian`, `Hijri`, `SolarHijri`, `SolarHijriNative`,
   `Chinese`), month/week/agenda views, heatmaps, adaptive panes, event markers, week numbers,
   range policy, custom styles, and saved state. It is an **Android application only**; this
   repository does not include a separate iOS, desktop, or browser sample app.

   ```bash
   ./gradlew :calendarviewsample:assembleDebug
   adb install -r calendarviewsample/build/outputs/apk/debug/calendarviewsample-debug.apk
   ```

2. **Pure Kotlin/JVM Engine Console ([`tools/consumer-check/jvm-consumer`](../tools/consumer-check/jvm-consumer/src/main/kotlin/consumer/Main.kt)):**
   A dependency-free console example consuming only `calendar-bom` and
   `calendar-chinese-native`. It demonstrates dataset bounds, epoch-day round-trips, month/year
   lengths, leap-month titles, weekday offsets, and day formatting without Compose or Android.

   ```bash
   ./gradlew :calendar-chinese-native:publishToMavenLocal :calendar-bom:publishToMavenLocal
   ./gradlew -p tools/consumer-check :jvm-consumer:run
   ```

3. **Minimal Minified Android Consumer ([`tools/consumer-check/android-consumer`](../tools/consumer-check/android-consumer)):**
   Standalone Android consumer compiling `calendar-chinese-compose` through the published BOM
   with R8/shrinking enabled in release mode.

---

## Upgrading & Migration

### Upgrading from 2.4.0 to 3.0.0

`3.0.0` is a breaking Kotlin Multiplatform release. Update direct versions or the BOM platform,
then migrate public date, callback, and locale types ([ADR 0006](decisions/0006-kotlin-multiplatform-calendar-modules.md)):

- Every callback and predicate takes an epoch day (`Long`); `firstDayOfWeek` is an ISO number
  (`1..7`) and `todayEpochDay` replaces `today`.
- `YearMonth` becomes `GregorianYearMonth`, `HijrahDate` becomes `HijriDate`, and
  `JalaliCalendar` becomes `SolarHijriDate`. Explicit `Locale` parameters are removed; localized
  labels come from each target's platform formatter.
- `ir.huri:jalalicalendar` is no longer used: `:calendar-solarHijri` is a facade over
  `:calendar-solarHijri-native`.
- The `@Deprecated(HIDDEN)` `java.time` overloads were removed. Android and JVM hosts can convert
  at the boundary with `SolarHijriDate.toLocalDate()`, `LocalDate.toSolarHijri()`, and
  `HijriDate` conversion helpers.
- The Hijri engine's `HijriDateProvider` parameter is optional (`UmmAlQuraHijriDateProvider` by
  default). Pass a custom or offset provider to the state holder and composable when using local
  adjustments (see the [Usage Guide](USAGE_GUIDE.md#hijri-islamic)).

### Migrating from 1.x

Version `2.0.0` removed all View-based components (`MultiMonthView`, `MonthView`, `DayHeatMapView`,
XML layouts, ViewBinding) in favor of Jetpack Compose. See
[V2_MIGRATION_MAPPING.md](V2_MIGRATION_MAPPING.md) for the complete 1.x → 2.0.0 mapping.

---

## Building from Source

Requirements:
- JDK 17+ (CI uses Temurin 17; local verification uses OpenJDK 17.0.21)
- Android SDK Platform 37.1 (`platforms;android-37.1`), Build-Tools 36.0.0 or 37.0.0 (configured via
  `ANDROID_HOME` or gitignored `local.properties`)
- Node.js / Yarn toolchain (downloaded automatically by Gradle when testing JS/Wasm) and
  Chromium/Chrome for headless JS and Compose Wasm browser tests (`jsBrowserTest`, `wasmJsBrowserTest`)

Toolchain (`3.1.0-beta` multi-target pre-release line): Kotlin `2.5.0-RC`,
Compose Multiplatform `1.13.0-beta02`, AGP `9.5.1`, Gradle `9.9.1`, Compose BOM `2026.09.00`,
`compileSdk` 37.1, `minSdk` 27. See [SETUP.md](SETUP.md) for detailed environment notes.

```bash
git clone https://github.com/mortezasun/android-calendarview.git
cd android-calendarview

# Sample APKs
./gradlew :calendarviewsample:assembleDebug
./gradlew :calendarviewsample:assembleRelease

# Multiplatform unit tests
./gradlew jvmTest                                 # KMP JVM tests across all 7 modules
./gradlew testAndroidHostTest                     # Shared commonTest suites on Android host
./gradlew :calendarviewsample:testDebugUnitTest   # Sample JVM unit tests
./gradlew jsNodeTest jsBrowserTest                # Kotlin/JS Node.js and headless Chrome suites
./gradlew :calendar-chinese-native:linuxX64Test   # Kotlin/Native engine fixtures
./gradlew :calendar-chinese-native:wasmJsNodeTest # Pure Kotlin/Wasm engine fixtures
./gradlew wasmJsBrowserTest                       # Compose/Wasm headless Chrome test suites

# Android Lint & Compose instrumentation compile check
./gradlew lint
./gradlew :library:compileAndroidDeviceTest :calendarviewsample:compileDebugAndroidTestKotlin

# Public API & KLIB ABI guards + Dokka + Benchmarks
./gradlew assembleAndroidMain :calendar-chinese-native:jvmJar
bash scripts/check-commonmain-purity.sh
bash scripts/api-check.sh
./gradlew iosArm64MainKlibrary && bash scripts/klib-api-check.sh
./gradlew :dokkaGenerate                          # Output: build/dokka/html/index.html
./gradlew :benchmarks:mainBenchmark               # Standalone JVM benchmark baseline

# Publish all 7 library modules + BOM to ~/.m2
./gradlew :library:publishToMavenLocal :calendar-gregorian:publishToMavenLocal \
  :calendar-hijri:publishToMavenLocal :calendar-solarHijri:publishToMavenLocal \
  :calendar-solarHijri-native:publishToMavenLocal :calendar-chinese-native:publishToMavenLocal \
  :calendar-chinese-compose:publishToMavenLocal \
  :calendar-bom:publishToMavenLocal
```

See [CI.md](CI.md) for GitHub Actions workflows and [DEVELOPMENT.md](DEVELOPMENT.md) for
contribution conventions.
