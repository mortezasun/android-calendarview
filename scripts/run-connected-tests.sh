#!/usr/bin/env bash
set -euo pipefail

mkdir -p .ci-diagnostics
trap 'status=$?; adb logcat -d > .ci-diagnostics/instrumentation-logcat.txt 2>&1 || true; exit "$status"' EXIT

adb shell getprop ro.build.version.sdk | tee .ci-diagnostics/emulator-api.txt
bash ./gradlew :library:connectedDebugAndroidTest --no-daemon --stacktrace --console=plain 2>&1 | tee .ci-diagnostics/instrumentation-gradle.txt
