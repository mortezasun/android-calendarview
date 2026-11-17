# Setup

Verified on 2026-11-16 (UTC 2026-11-17) against the uncommitted Kotlin `2.5.0-RC` / Compose
Multiplatform `1.13.0-beta02` tree (`master` and `kmp` both at `a116b81`, using OpenJDK
`17.0.21`, Android SDK Platform `37.1`, Build-Tools `37.0.0`, Gradle `9.9.1`). The GitHub
workflows configure Temurin JDK 17; their historical `3.0.0` runs and the post-release checks are
recorded in [PROJECT_STATUS.md](PROJECT_STATUS.md).

## Required tools

| Tool | Version | Notes |
|------|---------|-------|
| JDK | 17 | Minimum and default for AGP 9.5 and Kotlin `2.5.0-RC` compiler; local & CI verification use JDK 17 (`17.0.21`). |
| Gradle | 9.9.1 | Provided by the wrapper; do not install a separate Gradle. |
| Android Gradle Plugin | 9.5.1 | Resolved from `google()` via `gradle/libs.versions.toml`. |
| Android SDK Platform | API 37.1 | SDK Manager package `platforms;android-37.1`. Required by `compileSdk` `37` (`minorApiLevel = 1`) for Compose Multiplatform `1.13.0-beta02`. |
| Android SDK Build-Tools | 36.0.0 / 37.0.0 | Minimum 36.0.0 for AGP 9.5; 37.0.0 is installed for local verification. |
| Android SDK Platform-Tools | current | Only needed to install/run the APK on a device. |
| Node.js / Yarn / Chrome | managed + system Chrome | Gradle fetches Node/Yarn/Binaryen via central Ivy repos; headless Chrome is needed for `jsBrowserTest` and `wasmJsBrowserTest`. |

`targetSdk` is 37 and `minSdk` is 27. Java/Kotlin target is JVM 17. Kotlin compilation goes through
AGP 9's built-in Kotlin support; the `kotlin-android` plugin is intentionally not applied in the
modules, and the Kotlin version in the catalog matches Kotlin Multiplatform (`2.5.0-RC`).

## Android SDK configuration

The repository does not contain `local.properties`; it is gitignored. Either

- set `ANDROID_HOME` (or `ANDROID_SDK_ROOT`) to the SDK directory, or
- create `local.properties` locally with `sdk.dir=/absolute/path/to/Android/Sdk`.

Do not commit that file.

Install the required packages:

```bash
sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-37.1" "build-tools;36.0.0"
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
./gradlew :library:assembleAndroidMain
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
./gradlew jvmTest                                 # 169 KMP JVM tests across all 7 modules
./gradlew testAndroidHostTest                     # 152 shared commonTest suites on Android host
./gradlew :calendarviewsample:testDebugUnitTest   # 62 sample JVM tests
./gradlew jsNodeTest jsBrowserTest                # 168 JS tests per runner in Node & Chrome
./gradlew :calendar-chinese-native:wasmJsNodeTest # 16 Wasm engine tests on Node.js
./gradlew wasmJsBrowserTest                       # 168 Wasm tests across all 7 modules in Chrome
./gradlew :calendar-chinese-native:linuxX64Test   # 16 Kotlin/Native Linux x64 tests
./gradlew lint                                    # Android Lint for the 6 KMP modules and sample
./gradlew :library:compileAndroidDeviceTest :calendarviewsample:compileDebugAndroidTestKotlin
```

`commonTest` holds 168 shared tests across the seven library modules (`library` 57, `calendar-gregorian`
19, `calendar-hijri` 22, `calendar-solarHijri` 14, `calendar-solarHijri-native` 30,
`calendar-chinese-native` 16, `calendar-chinese-compose` 10): selection engine and saved-state
snapshots, month-cell building, heatmap data and style validation, the Hijri date-provider mapping,
and engine month/week builders. `jvmTest` runs 169 tests (`commonTest` 168 + 1 `ChineseJvmBenchmarkTest`).
`testAndroidHostTest` runs 152 tests across the six Android KMP modules. All seven modules execute
their `commonTest` suites on `jsNodeTest`, `jsBrowserTest`, and `wasmJsBrowserTest` (168 each);
`:calendar-chinese-native` additionally runs 16 engine fixtures on `wasmJsNodeTest` and `linuxX64Test`.
The sample keeps 62 JVM tests in `src/test`.

Dokka and performance checks are available without joining the normal test gate:

```bash
./gradlew :dokkaGenerate            # HTML API reference in build/dokka/html
./gradlew :benchmarks:mainBenchmark # JVM performance smoke run; not part of check/build
```

Dokka `2.3.0` documents all seven published modules. The `:benchmarks` module uses
kotlinx-benchmark `0.5.0`; see [BENCHMARKS.md](BENCHMARKS.md).

`src/androidTest` holds the 46 Compose instrumentation tests (18 in `:library` covering selection,
heatmaps, month header, week/agenda views, accessibility and template check, plus 28 in
`:calendarviewsample` covering sample acceptance, Chinese/Solar Hijri regression tests, adaptive
layout and state restoration). The KMP modules map that directory into `androidDeviceTest`, so
`compileAndroidDeviceTest` and `compileDebugAndroidTestKotlin` prove they compile without an
emulator; executing them requires a device or emulator.

## Dependencies and repositories

Repositories are defined centrally in `settings.gradle.kts` with
`RepositoriesMode.FAIL_ON_PROJECT_REPOS`:

- `google()` for AGP and AndroidX/Material,
- `mavenCentral()` for Kotlin, Compose Multiplatform, and JUnit,
- `https://jitpack.io` for JitPack consumption,
- Ivy endpoints for Node.js, Yarn, and Binaryen toolchains (so Kotlin/JS and Kotlin/Wasm stay
  compliant with `FAIL_ON_PROJECT_REPOS` after clearing per-plugin URLs in `build.gradle.kts`).

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
Kotlin/Native ABI is guarded separately by `bash scripts/klib-api-check.sh` against
`api/klib/*.abi`; `bash scripts/check-commonmain-purity.sh` verifies that `commonMain` stays free
of platform APIs.

## Publishing

All six Compose KMP modules apply `maven-publish` under
`com.github.mortezasun.android-calendarview:<module>:<version>` (`library`, `calendar-gregorian`,
`calendar-hijri`, `calendar-solarHijri`, `calendar-solarHijri-native`, `calendar-chinese-compose`)
and publish root metadata plus `-android` AAR, `-jvm` JAR, and `-iosarm64`, `-iossimulatorarm64`,
`-macosarm64`, `-js`, and `-wasm-js` KLIB variants (8 artifact directories each, including sources
jars).
`:calendar-chinese-native` publishes Kotlin Multiplatform metadata plus `-jvm`, `-linuxx64`,
`-linuxarm64`, `-mingwx64`, `-iosx64`, `-iosarm64`, `-iossimulatorarm64`, `-macosarm64`, `-js`, and
`-wasm-js` variants under the same group (11 artifact directories; Android consumers use its JVM
variant). The group and version are defined once in the root `build.gradle.kts`; the artifact id is
the module name. `:calendar-bom` publishes a `java-platform` POM with constraints for all seven
modules (`60` published artifact directories in total). `calendarviewsample` is not published.
`jitpack.yml` pins JDK 17 for the JitPack build and publishes the BOM together with the modules.

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

Artifacts land in `~/.m2/repository/com/github/mortezasun/android-calendarview/`. Verify all 60
publication directories, archive contents, and every Gradle Module Metadata reference, then run the
isolated JVM and minified Android release consumers (pass the same Maven repository used above):

```bash
bash scripts/verify-publication.sh /tmp/m2-review 3.1.0-beta
./gradlew -p tools/consumer-check :jvm-consumer:run :android-consumer:assembleRelease \
  -PconsumerRepo=/tmp/m2-review
```

The consumer build is standalone: it has no `project(...)` or `includeBuild(...)` reference and
does not default to `mavenLocal()`. Check the generated POMs: each Compose `calendar-*` module
must depend on `...:library` in compile scope (and `calendar-solarHijri` on
`calendar-solarHijri-native`; `calendar-chinese-compose` on `calendar-chinese-native` and
`calendar-gregorian`), no calendar module may carry a third-party calendar dependency (the Hijri
chronology comes from the platform; the Solar Hijri and Chinese engines are self-written), and the
BOM must list all seven modules in `dependencyManagement` with the root version. The Chinese
engine POM must not reference AndroidX, Compose, Java date libraries or another calendar module.
The shared POM fields (name, description, URL, license, developer, SCM) come from the root build.
Maven Central signing and upload are not configured. JitPack built release `2.1.0` and `3.0.0`
(`a0c686e`) cleanly; see [PROJECT_STATUS.md](PROJECT_STATUS.md) and
[RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md).

## Known setup problems

1. **Old SDK command-line tools cannot see API 37.1.** They fail with
   `Package platforms;android-37 not found` or `Failed to find package 'platforms;android-37.1'`.
   Update the command-line tools, then install `platforms;android-37.1` — Compose Multiplatform
   `1.13.0-beta02` requires `minorApiLevel = 1` in its AAR metadata.
2. **`./gradlew: Permission denied`.** The repository tracks `gradlew` without the executable bit.
   Run `chmod +x gradlew` or `bash ./gradlew` (do not commit mode changes unless requested).
3. **Missing SDK location.** Without `ANDROID_HOME` or `local.properties`, Gradle stops during
   configuration with "SDK location not found".
4. **No JDK installed.** AGP 9.5 and Kotlin `2.5.0-RC` require JDK 17 (`17.0.21` used locally).
5. **Kotlin/Native, JS and Wasm toolchains.** `:calendar-chinese-native:linuxX64Test`, Apple/MinGW
   KLIB tasks, and `jsBrowserTest`/`wasmJsBrowserTest` download the Kotlin/Native bundle, Node.js,
   Yarn, and Binaryen on first use; browser tests additionally require Chrome/Chromium and
   generate `kotlin-js-store/` lockfiles locally (`kotlin-js-store/` is ignored in `.gitignore`).
6. **`kotlinStoreYarnLock` mismatch after JS build changes.** If `kotlin-js-store/` exists locally
   and JS/Wasm targets change, run `./gradlew kotlinUpgradeYarnLock`.
