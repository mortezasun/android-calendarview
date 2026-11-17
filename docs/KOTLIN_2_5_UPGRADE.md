# Kotlin 2.5.0 upgrade plan and RC validation

Status: active **`3.1.0-beta` pre-release preparation** (`Kotlin 2.5.0-RC` and Compose
Multiplatform `1.13.0-beta02`, compiled against Android SDK Platform `37.1`, Gradle `9.9.1`, AGP
`9.5.1`). The experimental `.kar` `KOTLIN_ARCHIVE` publication format remains disabled. The
`3.1.0-beta` version is configured for CI and manual APK verification; the `3.1.0-beta` release tag
is pending test confirmation, and stable releases await final Kotlin `2.5.0` / Compose Multiplatform
`1.13.0`.

Notice the tagged `3.0.0` release on GitHub used Kotlin `2.4.21` and Compose Multiplatform `1.12.2`;
this document tracks the uncommitted Kotlin `2.5.0-RC` upgrade and cross-target expansion ahead of
eventual Kotlin `2.5.0` stable adoption.

Official references:

- [Kotlin release process and upcoming releases](https://kotlinlang.org/docs/releases.html)
- [What's new in Kotlin 2.5.0-RC](https://kotlinlang.org/docs/whatsnew-eap.html)
- [Gradle compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html)
- [Compose Multiplatform releases](https://github.com/JetBrains/compose-multiplatform/releases)

## Current RC baseline (uncommitted)

Read these values from the repository before changing anything:

| Item | Current value | Source |
|---|---|---|
| Kotlin | `2.5.0-RC` (tagged `3.0.0` used `2.4.21`) | `gradle/libs.versions.toml` |
| Compose Multiplatform | `1.13.0-beta02` (tagged `3.0.0` used `1.12.2`) | `gradle/libs.versions.toml` |
| Compose Material 3 | `1.13.0-beta02` | `gradle/libs.versions.toml` |
| Gradle wrapper | `9.9.1` | `gradle/wrapper/gradle-wrapper.properties` |
| Android Gradle Plugin | `9.5.1` (`compileSdk` `37.1` via `platforms;android-37.1`) | `gradle/libs.versions.toml` |
| Kotlin/JVM target | `17` | module build files and CI |
| CI JDK | `17` | `.github/workflows/*.yml` |
| Local verification JDK | `17.0.21` (OpenJDK 17) | [SETUP.md](SETUP.md) |

## Official changes accounted for

- **Compiler JDK:** the Kotlin 2.5.0-RC notes raise the compiler's minimum runtime JDK to 17. The
  repository already targets JVM 17 and configures JDK 17 in CI and locally.
- **Compose Multiplatform `1.13.0-beta02` AAR metadata & JS tests:** Compose `1.13.0-beta02`
  requires `compileSdk >= 37.1` (`platforms;android-37.1`) for `checkAndroidMainAarMetadata` and
  requires `binaries.executable()` on the JS target of Compose modules so `jsBrowserTest` satisfies
  `checkComposeUiTestConfigurationForJs`. `copyAndroidDeviceTestComposeResourcesToAndroidAssets`
  still leaves `outputDirectory` unconfigured and remains disabled centrally in root
  `build.gradle.kts`.
- **Multiplatform publication format:** Kotlin 2.5.0-RC introduces an experimental format that
  publishes platform artifacts other than JVM and Android as a single `.kar` archive. The
  `KOTLIN_ARCHIVE` opt-in remains disabled so all 60 Maven publication directories continue to expose
  standard AAR, JAR, KLIB, POM, and Gradle Module Metadata files.
- **API and publication tooling:** compiler/metadata changes are guarded continuously by
  `scripts/api-check.sh` and `scripts/klib-api-check.sh`. Both guards pass with zero unintended
  public API/ABI changes and all five internal platform adapters absent.

## Checklist

- [x] Validate `Kotlin 2.5.0-RC` + Compose Multiplatform `1.13.0-beta02` + `platforms;android-37.1`
      on Gradle `9.9.1` and AGP `9.5.1` locally (OpenJDK `17.0.21`).
- [x] Run `jvmTest`, `testAndroidHostTest`, `:calendarviewsample:testDebugUnitTest`,
      `jsNodeTest`, `jsBrowserTest`, `:calendar-chinese-native:wasmJsNodeTest`,
      `wasmJsBrowserTest`, `:calendar-chinese-native:linuxX64Test`, `lint`, and instrumentation
      compilation tasks.
- [x] Assemble Android AARs and JVM JAR, check `scripts/api-check.sh`, and compile all KLIB
      targets (`iosArm64`, `iosSimulatorArm64`, `macosArm64`, `iosX64`, `linuxArm64`, `mingwX64`) +
      run `scripts/klib-api-check.sh` and `scripts/check-commonmain-purity.sh`.
- [x] Keep `KOTLIN_ARCHIVE` off and verify all 60 local Maven publication directories with
      `scripts/verify-publication.sh` plus the isolated JVM and minified Android consumers.
- [ ] Re-verify against final stable Kotlin `2.5.0` and stable Compose Multiplatform `1.13.0`
      (replacing `2.5.0-RC` and `1.13.0-beta02`) before tagging or cutting any release.
- [ ] Run API 29 emulator workflows (`46` instrumentation tests), plus macOS/Xcode (`ios*`,
      `macosArm64`) and Windows (`mingwX64`) / Linux ARM (`linuxArm64`) runtime tests on dedicated
      hardware.
- [ ] Verify whether `kotlinx-benchmark 0.5.0` and Compose UI runtime need any further adjustment
      after moving from RC/beta to stable `2.5.0` / `1.13.0`.

## Open questions

- When will final stable Kotlin `2.5.0` and a matching non-beta Compose Multiplatform `1.13.0`
  release be published for Gradle `9.9.1` and AGP `9.5.1`?
- Should `.kar` (`KOTLIN_ARCHIVE`) stay disabled permanently for JitPack compatibility or be
  evaluated once downstream tooling supports it?
- Do Apple (`ios*`, `macosArm64`), `linuxArm64`, and `mingwX64` hardware test runs pass all runtime
  localization/time-zone assertions without adjustment?
