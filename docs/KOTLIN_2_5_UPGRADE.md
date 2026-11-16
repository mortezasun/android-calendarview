# Kotlin 2.5.0 upgrade plan

Status: planning only. This branch still uses Kotlin `2.4.21` and has not enabled any Kotlin 2.5
preview or publication-format option. No version bump, tag or release is part of this plan.

As of 2026-11-16, the official Kotlin release page lists Kotlin `2.5.0` as planned for December
2026, and the `2.5.0-RC` notes are available. Check the stable release notes before starting; an
RC statement is not a compatibility guarantee for this repository.

Official references:

- [Kotlin release process and upcoming releases](https://kotlinlang.org/docs/releases.html)
- [What's new in Kotlin 2.5.0-RC](https://kotlinlang.org/docs/whatsnew-eap.html)
- [Gradle compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html)
- [Compose Multiplatform releases](https://github.com/JetBrains/compose-multiplatform/releases)

## Current baseline

Read these values from the repository before changing anything:

| Item | Current value | Source |
|---|---|---|
| Kotlin | `2.4.21` | `gradle/libs.versions.toml` |
| Compose Multiplatform | `1.12.2` | `gradle/libs.versions.toml` |
| Compose Material 3 | `1.9.0` | `gradle/libs.versions.toml` |
| Gradle wrapper | `9.9.1` | `gradle/wrapper/gradle-wrapper.properties` |
| Android Gradle Plugin | `9.5.1` | `gradle/libs.versions.toml` |
| Kotlin/JVM target | `17` | module build files and CI |
| CI JDK | `17` | `.github/workflows/*.yml` |
| Local verification JDK | `21.0` | [SETUP.md](SETUP.md) |

## Official changes to account for

- **Compiler JDK:** the Kotlin 2.5.0-RC notes raise the compiler's minimum runtime JDK to 17. The
  repository already targets JVM 17 and configures JDK 17 in CI, but the JDK used by every local
  and CI Gradle invocation must be checked before the upgrade.
- **Multiplatform publication format:** Kotlin 2.5.0-RC introduces an experimental format that
  publishes platform artifacts other than JVM and Android as a single `.kar` archive. The official
  opt-in is `kotlin.publicationFormat=KOTLIN_ARCHIVE` in `gradle.properties`, or the module-level
  `kotlin { publishing { publicationFormat.set(KotlinPublicationFormat.KOTLIN_ARCHIVE) } }`.
  This plan does not enable it by default.
- **Compilation scheme:** the RC introduces an experimental KMP compilation scheme. This plan does
  not enable it. Evaluate it separately if the standard scheme has a concrete problem.
- **Compatibility:** Kotlin 2.5.0 stable compatibility with the repository's Gradle `9.9.1`, AGP
  `9.5.1`, Compose Multiplatform `1.12.2`, Compose compiler plugin and `kotlinx-benchmark 0.5.0` is
  **not established by the repository**. Verify each against the official compatibility documents
  for the stable release; do not infer support from an RC.
- **API and publication tooling:** compiler/metadata changes can alter JVM and KLIB ABI dumps even
  when source compatibility is unchanged. `scripts/api-check.sh` and `scripts/klib-api-check.sh`
  must be run before deciding whether a dump change is an intentional API change.

## Checklist

- [ ] Read the final Kotlin 2.5.0 release notes, compatibility guide and Compose Multiplatform
      release notes; record the supported Kotlin/Gradle/AGP/Compose combination.
- [ ] Decide whether the project upgrades to stable `2.5.0` or validates an RC first; keep this
      decision and the evidence in the upgrade PR.
- [ ] Update `kotlin` in `gradle/libs.versions.toml`; update the Compose Multiplatform version only
      to a version explicitly documented as compatible with that Kotlin version.
- [ ] Check whether the Compose compiler plugin, Gradle, AGP and `kotlinx-benchmark` need coordinated
      versions. Do not bump them speculatively in the same change.
- [ ] Run `./gradlew help` and the module configuration checks with JDK 17 and JDK 21.
- [ ] Run `jvmTest`, `testAndroidHostTest`, `:calendarviewsample:testDebugUnitTest`,
      `:calendar-chinese-native:linuxX64Test`, lint and the device-test compilation tasks.
- [ ] Build the six Android AARs with `assembleAndroidMain` and run `scripts/api-check.sh`; review
      every dump change. Confirm the five internal platform adapters remain absent from
      `api/*.api`.
- [ ] Build all seven `iosArm64MainKlibrary` outputs and run `scripts/klib-api-check.sh`; review
      every KLIB ABI change and confirm the platform adapters remain absent.
- [ ] Decide explicitly whether to opt in to `KOTLIN_ARCHIVE`. Keep it off unless the team accepts
      its experimental status and updates publication verification.
- [ ] If `KOTLIN_ARCHIVE` is enabled, update `scripts/verify-publication.sh` for `.kar` files and
      rerun the isolated JVM and Android consumers plus the JitPack consumer. JVM and Android
      artifacts remain standard Maven artifacts.
- [ ] Run `publishToMavenLocal` for all seven modules and the BOM, then
      `scripts/verify-publication.sh`.
- [ ] Run `scripts/check-commonmain-purity.sh` and `./gradlew :dokkaGenerate`.
- [ ] Update `docs/PROJECT_STATUS.md`, `docs/SETUP.md`, `docs/DEVELOPMENT.md`, CI workflow comments
      and the dependency rationale with the verified toolchain.
- [ ] Do not create a tag or change the published `3.0.0` tag until a separate release decision is
      approved.

## Open questions

- Which Compose Multiplatform release officially supports Kotlin 2.5.0 stable?
- Does the stable Kotlin 2.5.0 release support Gradle `9.9.1` and AGP `9.5.1` without unsupported
  combinations?
- Does `kotlinx-benchmark 0.5.0` support the Kotlin 2.5 compiler and the current Gradle/AGP stack?
- Is the `.kar` publication format still experimental at stable, and does JitPack/our publication
  verifier support it for this seven-module layout?
- Are any language/compiler behavior changes in Kotlin 2.5 observable in the public API dumps or
  in `commonMain` source compatibility?
