# Setup

Verified on 2026-11-09 against branch `update` (commit `9a71c52` plus the documentation commit
based on it).

## Required tools

| Tool | Version | Notes |
|------|---------|-------|
| JDK | 17 | Minimum and default for AGP 9.5. Temurin 17.0.21+9 was used for verification. |
| Gradle | 9.8.2 | Provided by the wrapper; do not install a separate Gradle. |
| Android Gradle Plugin | 9.5.1 | Resolved from `google()` via `gradle/libs.versions.toml`. |
| Android SDK Platform | API 37 | SDK Manager package `platforms;android-37.0`. Required by `compileSdk = 37`. |
| Android SDK Build-Tools | 36.0.0 | Default/minimum for AGP 9.5. |
| Android SDK Platform-Tools | current | Only needed to install/run the APK on a device. |

`targetSdk` is 37 and `minSdk` is 27. Java/Kotlin target is JVM 17. Kotlin compilation goes through
AGP 9's built-in Kotlin support; the `kotlin-android` plugin is intentionally not applied in the
modules, and the Kotlin version in the catalog is kept for consistency with the plugin aliases that
are declared `apply false`.

## Android SDK configuration

The repository does not contain `local.properties`; it is gitignored. Either

- set `ANDROID_HOME` (or `ANDROID_SDK_ROOT`) to the SDK directory, or
- create `local.properties` locally with `sdk.dir=/absolute/path/to/Android/Sdk`.

Do not commit that file.

Install the required packages:

```bash
sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-37.0" "build-tools;36.0.0"
```

## Build and run

```bash
# The wrapper is tracked without the executable bit in some clones
chmod +x gradlew

# Build the demo app; this compiles :library and all calendar modules
./gradlew :calendarviewsample:assembleDebug

# APK
# calendarviewsample/build/outputs/apk/debug/calendarviewsample-debug.apk

# Install on a connected device/emulator
adb install -r calendarviewsample/build/outputs/apk/debug/calendarviewsample-debug.apk
```

The demo app has two activities: `LauncherActivity` (configurable calendar screen, including the
synchronized calendar inside an `AlertDialog`) and `HeatMapViewActivity` (heatmap screen).
`LauncherActivity` is the launcher entry point.

Building only the core library:

```bash
./gradlew :library:assembleDebug
```

## Tests and lint

```bash
./gradlew test               # JVM unit tests for all modules
./gradlew :library:lintDebug # Android Lint for the core library
```

`src/test` holds the JVM tests: selection engine and saved-state snapshots, month-cell building,
heatmap data and style validation, the sample helpers and one month-data suite per calendar
engine. `src/androidTest` holds the Compose UI tests (`CalendarSelectionComposeTest`,
`CalendarHeatmapComposeTest`, plus the sample acceptance tests). Only the `ExampleUnitTest` /
`ExampleInstrumentedTest` files are leftover templates. Instrumented tests require an emulator or
device and were not part of the CLI verification.

## Dependencies and repositories

Repositories are defined centrally in `settings.gradle.kts` with
`RepositoriesMode.FAIL_ON_PROJECT_REPOS`:

- `google()` for AGP and AndroidX/Material,
- `mavenCentral()` for Kotlin/JUnit and `ir.huri:jalalicalendar`,
- `https://jitpack.io` for JitPack consumption.

`:calendar-solarHijri` is the only module that declares `ir.huri:jalalicalendar`, and it exposes it
with `api`, so consumers of that engine get the library transitively while `:library` stays free
of it.

## Secrets

The project needs no API keys, tokens or signing secrets for debug builds. Release signing is not
configured in the repository. Never commit keystores, `local.properties`, credentials or private
keys.

## Known setup problems

1. **Old SDK command-line tools cannot see API 37.** They fail with
   `Package platforms;android-37 not found` or `Failed to find package 'platforms;android-37'`.
   Update the command-line tools, then install `platforms;android-37.0` - the package carries the
   minor API level in its name.
2. **`./gradlew: Permission denied`.** The repository tracks `gradlew` without the executable bit.
   Run `chmod +x gradlew` (do not commit the mode change unless the task asks for it).
3. **Missing SDK location.** Without `ANDROID_HOME` or `local.properties`, Gradle stops during
   configuration with "SDK location not found".
4. **No JDK installed.** AGP 9.5 requires JDK 17; a newer JDK may work but was not verified here.
