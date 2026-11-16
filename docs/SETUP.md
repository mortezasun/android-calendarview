# Setup

Verified on 2026-11-16 against the `3.0.0` release tree on branch `kmp` (JDK 21.0,
Android SDK Platform 37.0, Build-Tools 37.0.0). The GitHub workflows configure JDK 17; their
`3.0.0` runs are recorded in [PROJECT_STATUS.md](PROJECT_STATUS.md).

## Required tools

| Tool | Version | Notes |
|------|---------|-------|
| JDK | 17 | Minimum and default for AGP 9.5. Temurin 17.0.21+9 was used for verification. |
| Gradle | 9.8.2 | Provided by the wrapper; do not install a separate Gradle. |
| Android Gradle Plugin | 9.5.1 | Resolved from `google()` via `gradle/libs.versions.toml`. |
| Android SDK Platform | API 37 | SDK Manager package `platforms;android-37.0`. Required by `compileSdk = 37`. |
| Android SDK Build-Tools | 36.0.0 | Default/minimum for AGP 9.5; 37.0.0 was also used successfully for the local 3.0.0 verification. |
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

## Build setup

- The KMP modules use `kotlin { android { ... } }`, apply `com.android.lint` and opt in to
  `withHostTest {}`/`withDeviceTest {}`; `src/androidTest/java` is mapped into
  `androidDeviceTest`.
- Compose dependencies use the explicit `org.jetbrains.compose.*` coordinates from
  `gradle/libs.versions.toml`; the deprecated plugin dependency accessors are not used.
- `kotlin.native.ignoreDisabledTargets=true` in `gradle.properties` keeps configuration quiet on
  hosts that cannot build the iOS targets.

## Tests and lint

```bash
./gradlew jvmTest             # KMP JVM tests for the seven library modules
./gradlew testAndroidHostTest # the same commonTest suites on the Android host runtime
./gradlew :calendarviewsample:testDebugUnitTest
./gradlew lint                # Android Lint for the six KMP modules and the sample
./gradlew :library:compileAndroidDeviceTest :calendarviewsample:compileDebugAndroidTestKotlin
```

`commonTest` holds the shared tests: selection engine and saved-state snapshots, month-cell
building, heatmap data and style validation, the Hijri date-provider mapping and one month-data
suite per calendar engine (156 tests). The sample keeps 62 JVM tests in `src/test`.
`testAndroidHostTest` runs 141 of the shared tests on Android; the Chinese JVM benchmark is
`jvmTest`-only. The Chinese engine fixtures also run on Kotlin/Native through
`:calendar-chinese-native:linuxX64Test`.

`src/androidTest` holds the Compose UI tests (`CalendarSelectionComposeTest`,
`CalendarHeatmapComposeTest`, `CalendarMonthHeaderComposeTest`,
`CalendarProductViewsComposeTest`, plus the sample acceptance tests). The KMP modules map that
directory into the `androidDeviceTest` compilation, so `compileAndroidDeviceTest` proves they
compile without an emulator; running them requires a device or emulator.

## Dependencies and repositories

Repositories are defined centrally in `settings.gradle.kts` with
`RepositoriesMode.FAIL_ON_PROJECT_REPOS`:

- `google()` for AGP and AndroidX/Material,
- `mavenCentral()` for Kotlin and JUnit,
- `https://jitpack.io` for JitPack consumption.

No module declares a third-party calendar dependency: the Hijri chronology comes from the
platform, and the Solar Hijri and Chinese engines are self-written.

## Secrets

The project needs no API keys, tokens or signing secrets for debug builds. Release signing is not
configured in the repository. Never commit keystores, `local.properties`, credentials or private
keys.

## Public API guard

The release AARs have a checked-in public JVM API dump in `api/`. After assembling them, run:

```bash
./gradlew assembleAndroidMain :calendar-chinese-native:jvmJar
bash scripts/api-check.sh
```

`bash scripts/api-check.sh --update` regenerates the dumps after an intentional API change. The
script needs a JDK (`javap`, `jar`) and `unzip`; CI runs it in the assemble job. It compares public
JVM signatures only, not Kotlin default values, annotations or source compatibility.

## Publishing

Five Android library modules apply `maven-publish` and publish their `release` variant with a
sources jar as `com.github.mortezasun.android-calendarview:<module>:<version>`.
`:calendar-chinese-native` publishes Kotlin Multiplatform metadata plus `jvm` and `linuxx64`
variants under the same group; its artifacts are a metadata jar, per-target jars/module metadata
and sources, not an AAR. The group and version are
defined once in the root `build.gradle.kts`; the artifact id is the module name.
`:calendar-bom` publishes a `java-platform` POM with constraints for all seven modules.
`calendarviewsample` is not published. `jitpack.yml` pins JDK 17 for the JitPack build and
publishes the BOM together with the modules.

```bash
./gradlew \
  :library:publishToMavenLocal \
  :calendar-gregorian:publishToMavenLocal \
  :calendar-hijri:publishToMavenLocal \
  :calendar-solarHijri:publishToMavenLocal \
  :calendar-solarHijri-native:publishToMavenLocal \
  :calendar-chinese-native:publishToMavenLocal \
  :calendar-chinese-compose:publishToMavenLocal \
  :calendar-bom:publishToMavenLocal
```

Artifacts land in `~/.m2/repository/com/github/mortezasun/android-calendarview/`. Verify the
exact versioned files, archive contents and every Gradle Module Metadata reference, then build
the isolated JVM and Android consumers (pass the same Maven repository used above):

```bash
bash scripts/verify-publication.sh /tmp/m2-review 3.0.0
# KMP layout: the root <module> artifact is metadata, AARs are <module>-android, and iOS
# klibs are <module>-iosarm64 / <module>-iossimulatorarm64. The verifier checks those files
# (iOS is cross-compiled only) plus every Gradle Module Metadata reference.
./gradlew -p tools/consumer-check :jvm-consumer:run :android-consumer:compileDebugKotlin \
  -PconsumerRepo=/tmp/m2-review
```

The consumer build is standalone: it has no `project(...)` or `includeBuild(...)` reference and
does not default to `mavenLocal()`. Check the generated POMs: each Compose `calendar-*` module
must depend on `...:library` in compile scope
(and `calendar-chinese-compose` additionally on `calendar-chinese-native`),
no calendar module may carry a third-party calendar dependency (the Hijri chronology comes from
the platform; the Solar Hijri and Chinese engines are self-written), and the BOM must list all
seven modules in `dependencyManagement` with the root version. The Chinese engine POM must not
reference AndroidX, Compose, Java date libraries or another calendar module. The shared POM fields (name,
description, URL, license, developer, SCM) come from the root build. Maven Central signing and
upload are not configured. JitPack built release `2.1.0` and a JitPack-only consumer resolved the
BOM plus all four modules on 2026-11-11; see [PROJECT_STATUS.md](PROJECT_STATUS.md) and
[RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md).

## Known setup problems

1. **Old SDK command-line tools cannot see API 37.** They fail with
   `Package platforms;android-37 not found` or `Failed to find package 'platforms;android-37'`.
   Update the command-line tools, then install `platforms;android-37.0` - the package carries the
   minor API level in its name.
2. **`./gradlew: Permission denied`.** The repository tracks `gradlew` without the executable bit.
   Run `chmod +x gradlew` (do not commit the mode change unless the task asks for it).
3. **Missing SDK location.** Without `ANDROID_HOME` or `local.properties`, Gradle stops during
   configuration with "SDK location not found".
4. **No JDK installed.** AGP 9.5 requires JDK 17; JDK 21.0 was used for the local 3.0.0 verification.
5. **Kotlin/Native toolchain download.** `:calendar-chinese-native:linuxX64Test` downloads the
   Kotlin/Native compiler and dependencies on first use. This needs network access and disk space;
   an offline run fails before the tests start.
