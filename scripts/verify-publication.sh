#!/usr/bin/env bash
# Verify an isolated Maven publication, including the real JVM/Native/JS/Wasm binaries and every
# file referenced by Gradle Module Metadata. A stale or missing target binary must fail here;
# resolving "any jar below the module" is not sufficient for a release gate.
#
# Kotlin Multiplatform layout:
# - Compose modules publish root metadata plus -android, -jvm, -iosarm64, -iossimulatorarm64,
#   -macosarm64, -js, and -wasm-js variants.
# - :calendar-chinese-native publishes root metadata plus -jvm, -linuxx64, -linuxarm64, -mingwx64,
#   -iosx64, -iosarm64, -iossimulatorarm64, -macosarm64, -js, and -wasm-js variants.
# Apple and Windows binaries are cross-compiled metadata/KLIBs; this gate verifies their files
# and Gradle Module Metadata contracts, not runtime execution on Apple/Windows hardware.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
REPO="${1:-$HOME/.m2/repository}"
VERSION="${2:-}"
BASE="$REPO/com/github/mortezasun/android-calendarview"

if [[ -z "$VERSION" ]]; then
    VERSION="$(sed -nE 's/^[[:space:]]*version = "([^"]+)"$/\1/p' "$ROOT/build.gradle.kts" | head -n 1)"
fi
if [[ -z "$VERSION" ]]; then
    echo "Could not determine the published version." >&2
    exit 2
fi

fail() {
    echo "Publication verification failed: $*" >&2
    exit 1
}

require_file() {
    local file="$1"
    [[ -f "$file" ]] || fail "missing $file"
    [[ -s "$file" ]] || fail "empty $file"
    echo "Verified $file"
}

COMPOSE_MODULES=(
    library
    calendar-gregorian
    calendar-hijri
    calendar-solarHijri
    calendar-solarHijri-native
    calendar-chinese-compose
)
COMPOSE_KLIB_TARGETS=(iosarm64 iossimulatorarm64 macosarm64 js wasm-js)

for module in "${COMPOSE_MODULES[@]}"; do
    dir="$BASE/$module/$VERSION"
    for suffix in .jar .pom .module -sources.jar -kotlin-tooling-metadata.json; do
        require_file "$dir/${module}-${VERSION}${suffix}"
    done

    android_dir="$BASE/${module}-android/$VERSION"
    for suffix in .aar .pom .module -sources.jar; do
        require_file "$android_dir/${module}-android-${VERSION}${suffix}"
    done

    jvm_dir="$BASE/${module}-jvm/$VERSION"
    for suffix in .jar .pom .module -sources.jar; do
        require_file "$jvm_dir/${module}-jvm-${VERSION}${suffix}"
    done

    for target in "${COMPOSE_KLIB_TARGETS[@]}"; do
        target_dir="$BASE/${module}-${target}/$VERSION"
        for suffix in .klib .pom .module -sources.jar; do
            require_file "$target_dir/${module}-${target}-${VERSION}${suffix}"
        done
    done
done

ENGINE_BASE="$BASE/calendar-chinese-native/$VERSION"
for suffix in .jar .pom .module -sources.jar -kotlin-tooling-metadata.json; do
    require_file "$ENGINE_BASE/calendar-chinese-native-${VERSION}${suffix}"
done

ENGINE_JVM_BASE="$BASE/calendar-chinese-native-jvm/$VERSION"
for suffix in .jar .pom .module -sources.jar; do
    require_file "$ENGINE_JVM_BASE/calendar-chinese-native-jvm-${VERSION}${suffix}"
done

ENGINE_KLIB_TARGETS=(
    linuxx64
    linuxarm64
    mingwx64
    iosx64
    iosarm64
    iossimulatorarm64
    macosarm64
    js
    wasm-js
)
for target in "${ENGINE_KLIB_TARGETS[@]}"; do
    target_dir="$BASE/calendar-chinese-native-${target}/$VERSION"
    for suffix in .klib .pom .module -sources.jar; do
        require_file "$target_dir/calendar-chinese-native-${target}-${VERSION}${suffix}"
    done
done

BOM_BASE="$BASE/calendar-bom/$VERSION"
for suffix in .pom .module; do
    require_file "$BOM_BASE/calendar-bom-${VERSION}${suffix}"
done

# Gradle Module Metadata is the consumer-facing contract. Every artifact URL must resolve
# to a non-empty file; the display name is validated separately and is never used as a path.
PYTHONPATH="$ROOT/scripts${PYTHONPATH:+:$PYTHONPATH}" python3 - "$BASE" "$VERSION" <<'PY'
import pathlib
import sys
import xml.etree.ElementTree as ET
import zipfile

from publication_metadata import check_module

base = pathlib.Path(sys.argv[1])
version = sys.argv[2]
group = "com.github.mortezasun.android-calendarview"
failures = []


def fail(message):
    failures.append(message)


def check_zip(path, required_prefixes, label):
    if not path.is_file() or path.stat().st_size == 0:
        fail(f"{label} is missing or empty: {path}")
        return
    try:
        with zipfile.ZipFile(path) as archive:
            names = archive.namelist()
    except zipfile.BadZipFile as error:
        fail(f"{label} is not a valid archive: {path}: {error}")
        return
    for prefix in required_prefixes:
        if not any(name.startswith(prefix) for name in names):
            fail(f"{label} has no archive entry {prefix!r}: {path}")


compose_modules = [
    "library",
    "calendar-gregorian",
    "calendar-hijri",
    "calendar-solarHijri",
    "calendar-solarHijri-native",
    "calendar-chinese-compose",
]
compose_klib_targets = [
    "iosarm64",
    "iossimulatorarm64",
    "macosarm64",
    "js",
    "wasm-js",
]
engine_klib_targets = [
    "linuxx64",
    "linuxarm64",
    "mingwx64",
    "iosx64",
    "iosarm64",
    "iossimulatorarm64",
    "macosarm64",
    "js",
    "wasm-js",
]

module_files = [
    base / "calendar-chinese-native" / version / f"calendar-chinese-native-{version}.module",
    base / "calendar-chinese-native-jvm" / version / f"calendar-chinese-native-jvm-{version}.module",
    base / "calendar-bom" / version / f"calendar-bom-{version}.module",
    *(
        base / f"calendar-chinese-native-{target}" / version / f"calendar-chinese-native-{target}-{version}.module"
        for target in engine_klib_targets
    ),
]
for module in compose_modules:
    module_files.append(base / module / version / f"{module}-{version}.module")
    module_files.append(base / f"{module}-android" / version / f"{module}-android-{version}.module")
    module_files.append(base / f"{module}-jvm" / version / f"{module}-jvm-{version}.module")
    for target in compose_klib_targets:
        module_files.append(base / f"{module}-{target}" / version / f"{module}-{target}-{version}.module")

for module_file in module_files:
    failures.extend(check_module(module_file))

engine_dir = base / "calendar-chinese-native" / version
jvm_dir = base / "calendar-chinese-native-jvm" / version
check_zip(
    engine_dir / f"calendar-chinese-native-{version}.jar",
    ["META-INF/kotlin-project-structure-metadata.json"],
    "engine metadata jar",
)
check_zip(
    jvm_dir / f"calendar-chinese-native-jvm-{version}.jar",
    ["com/develotter/calendarview/calendars/chineseNative/"],
    "engine JVM jar",
)
for target in engine_klib_targets:
    klib = base / f"calendar-chinese-native-{target}" / version / f"calendar-chinese-native-{target}-{version}.klib"
    check_zip(klib, ["default/manifest", "default/linkdata/"], f"engine {target} klib")

for module in compose_modules:
    check_zip(
        base / module / version / f"{module}-{version}.jar",
        ["META-INF/kotlin-project-structure-metadata.json"],
        f"{module} metadata jar",
    )
    aar = base / f"{module}-android" / version / f"{module}-android-{version}.aar"
    check_zip(aar, ["classes.jar"], f"{module} AAR")
    if aar.is_file():
        with zipfile.ZipFile(aar) as archive:
            classes = archive.read("classes.jar")
        if len(classes) < 100:
            fail(f"{module} AAR contains an empty classes.jar")
    jvm_jar = base / f"{module}-jvm" / version / f"{module}-jvm-{version}.jar"
    check_zip(jvm_jar, ["com/develotter/calendarview/"], f"{module} JVM jar")
    for target in compose_klib_targets:
        klib = base / f"{module}-{target}" / version / f"{module}-{target}-{version}.klib"
        check_zip(klib, ["default/manifest", "default/linkdata/"], f"{module} {target} klib")


def pom_dependencies(path):
    tree = ET.parse(path)
    root = tree.getroot()
    namespace = {"m": root.tag.split("}")[0].lstrip("{")} if "}" in root.tag else {}
    dependencies = []
    for dependency in root.findall(".//m:dependency", namespace):
        dependency_group = dependency.findtext("m:groupId", default="", namespaces=namespace)
        dependency_artifact = dependency.findtext("m:artifactId", default="", namespaces=namespace)
        dependencies.append((dependency_group, dependency_artifact))
    return dependencies


engine_poms = [
    engine_dir / f"calendar-chinese-native-{version}.pom",
    jvm_dir / f"calendar-chinese-native-jvm-{version}.pom",
    *(
        base / f"calendar-chinese-native-{target}" / version / f"calendar-chinese-native-{target}-{version}.pom"
        for target in engine_klib_targets
    ),
]
for engine_pom in engine_poms:
    if engine_pom.is_file():
        text = engine_pom.read_text()
        for forbidden in ("calendar-gregorian", "calendar-hijri", "calendar-solarHijri", "androidx", "compose"):
            if forbidden in text:
                fail(f"{engine_pom} must not depend on {forbidden}")

for module in (
    "calendar-gregorian",
    "calendar-hijri",
    "calendar-solarHijri",
    "calendar-solarHijri-native",
    "calendar-chinese-compose",
):
    module_pom = base / module / version / f"{module}-{version}.pom"
    if module_pom.is_file():
        dependencies = pom_dependencies(module_pom)
        if (group, "library") not in dependencies:
            fail(f"{module_pom} is missing dependency library")

solar_facade_pom = base / "calendar-solarHijri" / version / f"calendar-solarHijri-{version}.pom"
if solar_facade_pom.is_file():
    if (group, "calendar-solarHijri-native") not in pom_dependencies(solar_facade_pom):
        fail(f"{solar_facade_pom} is missing dependency calendar-solarHijri-native")

compose_pom = base / "calendar-chinese-compose" / version / f"calendar-chinese-compose-{version}.pom"
if compose_pom.is_file():
    dependencies = pom_dependencies(compose_pom)
    for required in ("library", "calendar-chinese-native", "calendar-gregorian"):
        if (group, required) not in dependencies:
            fail(f"{compose_pom} is missing dependency {required}")

bom_pom = base / "calendar-bom" / version / f"calendar-bom-{version}.pom"
if bom_pom.is_file():
    dependencies = pom_dependencies(bom_pom)
    expected = [
        "library",
        "calendar-gregorian",
        "calendar-hijri",
        "calendar-solarHijri",
        "calendar-solarHijri-native",
        "calendar-chinese-native",
        "calendar-chinese-compose",
    ]
    for artifact in expected:
        if (group, artifact) not in dependencies:
            fail(f"{bom_pom} does not constrain {artifact}")

if failures:
    for message in failures:
        print(f"ERROR: {message}", file=sys.stderr)
    sys.exit(1)
print(f"Validated {len(module_files)} publication modules, binaries, POMs and Gradle Module Metadata references.")
PY

echo "Publication verification passed for $BASE at $VERSION."
