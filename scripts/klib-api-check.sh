#!/usr/bin/env bash
# Compares the public ABI of the iosArm64 klibs with checked-in snapshots. The JVM API guard does
# not see native-only declarations, so this is the KMP counterpart for the iOS surface.
#
# Usage: bash scripts/klib-api-check.sh [--update]
# The klibs are produced by e.g. ./gradlew iosArm64MainKlibrary.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

UPDATE=0
if [[ "${1:-}" == "--update" ]]; then
    UPDATE=1
fi

VERSION="$(sed -nE 's/^[[:space:]]*version = "([^"]+)"$/\1/p' build.gradle.kts | head -n 1)"
if [[ -z "$VERSION" ]]; then
    echo "Could not determine the project version." >&2
    exit 2
fi

KLIB="$(find "${KONAN_DATA_DIR:-$HOME/.konan}" -type f -path '*/bin/klib' 2>/dev/null | head -n 1)"
if [[ -z "$KLIB" ]]; then
    echo "Kotlin/Native klib tool not found; run a compileKotlinIosArm64 task first." >&2
    exit 2
fi

MODULES=(
    library
    calendar-gregorian
    calendar-hijri
    calendar-solarHijri
    calendar-solarHijri-native
    calendar-chinese-native
    calendar-chinese-compose
)

status=0
for module in "${MODULES[@]}"; do
    klib="$ROOT/$module/build/classes/kotlin/iosArm64/main/klib/$module"
    if [[ ! -d "$klib" ]]; then
        echo "Missing $module klib directory; run ./gradlew iosArm64MainKlibrary." >&2
        status=1
        continue
    fi

    snapshot="$ROOT/api/klib/${module}.abi"
    actual="$(mktemp)"
    "$KLIB" dump-abi "$klib" > "$actual"

    if [[ "$UPDATE" -eq 1 ]]; then
        mkdir -p "$ROOT/api/klib"
        cp "$actual" "$snapshot"
        echo "Updated $snapshot"
    elif [[ ! -f "$snapshot" ]]; then
        echo "Missing KLIB API snapshot $snapshot; run with --update." >&2
        status=1
    elif ! diff -u "$snapshot" "$actual"; then
        echo "KLIB API mismatch for $module; review the diff and update with --update." >&2
        status=1
    else
        echo "KLIB API matches for $module."
    fi
    rm -f "$actual"
done

if [[ "$status" -ne 0 ]]; then
    exit 1
fi
echo "KLIB API check passed for $VERSION."
