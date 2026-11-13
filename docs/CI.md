# Android verification workflow

Workflow: .github/workflows/android-verification.yml
Branch: update
Status: active; the `2.4.0` workflow changes passed on `1ab841e` (2026-11-13):
verification Run `43401491446`, publishing Run `43401491433`, instrumentation Run
`43401491452` and tablet Run `43401491675`, all success. The local checks remain listed
separately in [PROJECT_STATUS.md](PROJECT_STATUS.md).

## Triggers and cost

Pushes to update, pull requests targeting update and manual workflow_dispatch start four
independent GitHub-hosted Ubuntu jobs. Each has a 30-minute timeout. New runs on the same
ref cancel older runs. Jobs can consume Actions minutes and store artifacts for seven days,
subject to repository/account policy. No publishing, release signing, deployment, write token
permissions or repository secrets are configured. Fork pull requests do not use privileged
pull_request_target execution. GitHub repository Actions settings may still require approval.

## Jobs

- assemble: builds the sample, all release AARs, the Chinese engine JVM jar and the BOM POM.
- unit-tests: runs Gradle `test` across the project plus `:calendar-chinese-native:jvmTest`.
- native-engine: runs `:calendar-chinese-native:linuxX64Test` on the same commonTest fixtures.
- lint: runs debug lint for core, all calendar modules, the Chinese Compose facade and sample.

The matrix uses fail-fast: false, so failure of one job does not cancel the other checks.
The native-engine job downloads the Kotlin/Native toolchain on first use and can take longer than
the JVM jobs; the 30-minute job timeout still applies.
No continue-on-error, test exclusion or lint suppression is introduced. These are debug
checks, not proof that release variants or published consumer artifacts work.

## Environment

JDK 17 (Temurin), repository Gradle wrapper and documented SDK packages:
platform-tools, platforms;android-37.0 and build-tools;36.0.0. SDK tools are set up by
android-actions/setup-android. Toolchain diagnostics record Java, Gradle and installed SDK
packages. Version/plugin resolution errors remain failures; the workflow does not silently
substitute older plugins, disable Compose, downgrade SDK or skip failing tests.

The SDK package names follow existing SETUP.md; availability on hosted runners has not
been independently established by this commit. Existing Kotlin/AGP/Compose compiler
compatibility is also unverified until compilation succeeds. Action major-version refs
are used; pinning reviewed commit SHAs is a separate supply-chain hardening task.

## Inspect results

In the GitHub repository open Actions, then Android verification, and select the run for
the exact commit under review. Check all three job conclusions, not just artifact presence.
Downloaded artifacts contain available Gradle HTML/XML reports, test results, toolchain
diagnostics and the debug APK when assembly produced it. Upload runs even after failures,
but missing files produce a warning, not a passing check. Artifacts alone prove no success.

The 2.1.0 release runs were inspected through the public GitHub check pages. A workflow
commit alone is not evidence of execution or success; the final status and exact run IDs are
recorded in [PROJECT_STATUS.md](PROJECT_STATUS.md). Automatic push runs remain the primary
trigger for this branch-only setup.

## Publishing verification

Workflow: .github/workflows/android-publishing.yml. It publishes every Android AAR, the KMP
engine root/JVM/linuxX64 publications and the BOM to `$RUNNER_TEMP/calendar-maven`, then runs
`scripts/verify-publication.sh`. That script checks exact versioned paths (not `find`-selected
files), archive contents, every file and `available-at` target referenced by Gradle Module
Metadata, engine POM isolation and BOM constraints. It fails on a missing target binary or a
broken metadata reference.

The same job then builds `tools/consumer-check`, a standalone Gradle build that has no
`project(...)` or `includeBuild(...)` link to this repository. Its JVM module runs the public
`ChineseDate.of` example against the published engine/BOM, and its Android module compiles the
public Compose facade against the published AARs. The consumer receives the isolated repository
through `-PconsumerRepo=...`; it does not use `mavenLocal()` by default. This is local-candidate
verification, not a JitPack consumer test. The exact-binary and consumer checks were validated
locally on 2026-11-13; a remote run for the current commit still has to be recorded.

## Remaining priority-0 checks

This workflow does not start an emulator, install/run the APK, test Android saved-state
restoration, execute Compose UI tests or assess gestures, accessibility, RTL and screenshots.
Add those separately after the compile/unit/lint baseline is working. The 2.1.0 release
runs and the JitPack-only consumer are recorded in [PROJECT_STATUS.md](PROJECT_STATUS.md);
this workflow still does not execute the sample's full gesture/accessibility matrix.

Official action documentation consulted:

- https://github.com/actions/setup-java
- https://github.com/android-actions/setup-android
- https://github.com/actions/upload-artifact
- https://docs.github.com/en/actions/tutorials/store-and-share-data
