# Android verification workflow

Workflow: .github/workflows/android-verification.yml
Branch: update
Status when introduced: configured source only; no execution result observed.

## Triggers and cost

Pushes to update, pull requests targeting update and manual workflow_dispatch start three
independent GitHub-hosted Ubuntu jobs. Each has a 30-minute timeout. New runs on the same
ref cancel older runs. Jobs can consume Actions minutes and store artifacts for seven days,
subject to repository/account policy. No publishing, release signing, deployment, write token
permissions or repository secrets are configured. Fork pull requests do not use privileged
pull_request_target execution. GitHub repository Actions settings may still require approval.

## Jobs

- assemble: builds the sample and its library dependencies.
- unit-tests: runs Gradle test across the project; generated template tests may also run.
- lint: runs debug lint for core, all three calendar modules and sample.

The matrix uses fail-fast: false, so failure of one job does not cancel the other checks.
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

The connector used in this session cannot list Actions runs or download their logs. A
workflow commit is not evidence of execution or success. If results cannot be inspected,
keep verification status pending. A manual run must select update; workflow_dispatch may
not appear in the UI until GitHub discovers the workflow on the default branch. Automatic
push runs are therefore the primary trigger for this branch-only setup.

## Remaining priority-0 checks

This workflow does not start an emulator, install/run the APK, test Android saved-state
restoration, execute Compose UI tests or assess gestures, accessibility, RTL and screenshots.
Add those separately after the compile/unit/lint baseline is working. Also test release
variants and independent consumers before calling version 2 release-ready.

Official action documentation consulted:

- https://github.com/actions/setup-java
- https://github.com/android-actions/setup-android
- https://github.com/actions/upload-artifact
- https://docs.github.com/en/actions/tutorials/store-and-share-data
