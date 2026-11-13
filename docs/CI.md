# Android verification workflow

Workflow: .github/workflows/android-verification.yml
Branch: update
Status: active; the `2.4.0` workflow changes have not run on GitHub yet, so this document describes
the configured jobs, not a passed remote run. The last inspected remote runs were the `2.3.0`
release-tree jobs on 2026-11-12.

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
