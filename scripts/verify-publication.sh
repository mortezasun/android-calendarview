#!/usr/bin/env bash
# Verify an isolated Maven publication, including the real JVM/Native binaries and every
# file referenced by Gradle Module Metadata. A stale or missing target binary must fail here;
# resolving "any jar below the module" is not sufficient for a release gate.
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

ANDROID_MODULES=(
    library
    calendar-gregorian
    calendar-hijri
    calendar-solarHijri
    calendar-solarHijri-native
    calendar-chinese-compose
)
for module in "${ANDROID_MODULES[@]}"; do
    dir="$BASE/$module/$VERSION"
    for suffix in .aar .pom .module -sources.jar; do
        require_file "$dir/${module}-${VERSION}${suffix}"
    done
done

ENGINE_BASE="$BASE/calendar-chinese-native/$VERSION"
JVM_BASE="$BASE/calendar-chinese-native-jvm/$VERSION"
NATIVE_BASE="$BASE/calendar-chinese-native-linuxx64/$VERSION"
for file in \
    "$ENGINE_BASE/calendar-chinese-native-${VERSION}.jar" \
    "$ENGINE_BASE/calendar-chinese-native-${VERSION}.pom" \
    "$ENGINE_BASE/calendar-chinese-native-${VERSION}.module" \
    "$ENGINE_BASE/calendar-chinese-native-${VERSION}-sources.jar" \
    "$ENGINE_BASE/calendar-chinese-native-${VERSION}-kotlin-tooling-metadata.json" \
    "$JVM_BASE/calendar-chinese-native-jvm-${VERSION}.jar" \
    "$JVM_BASE/calendar-chinese-native-jvm-${VERSION}.pom" \
    "$JVM_BASE/calendar-chinese-native-jvm-${VERSION}.module" \
    "$JVM_BASE/calendar-chinese-native-jvm-${VERSION}-sources.jar" \
    "$NATIVE_BASE/calendar-chinese-native-linuxx64-${VERSION}.klib" \
    "$NATIVE_BASE/calendar-chinese-native-linuxx64-${VERSION}.pom" \
    "$NATIVE_BASE/calendar-chinese-native-linuxx64-${VERSION}.module" \
    "$NATIVE_BASE/calendar-chinese-native-linuxx64-${VERSION}-sources.jar"; do
    require_file "$file"
done

BOM_BASE="$BASE/calendar-bom/$VERSION"
for suffix in .pom .module; do
    require_file "$BOM_BASE/calendar-bom-${VERSION}${suffix}"
done

# Gradle Module Metadata is the consumer-facing contract. Every file it names must exist
# under the same coordinates, otherwise a platform variant is silently unresolvable.
python3 - "$BASE" "$VERSION" <<'PY'
import json
import pathlib
import sys
import xml.etree.ElementTree as ET
import zipfile

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


def check_module(path):
    if not path.is_file() or path.stat().st_size == 0:
        fail(f"missing module metadata: {path}")
        return
    try:
        metadata = json.loads(path.read_text())
    except json.JSONDecodeError as error:
        fail(f"invalid module metadata {path}: {error}")
        return
    for variant in metadata.get("variants", []):
        for artifact in variant.get("files", []):
            name = artifact.get("name")
            if not name:
                fail(f"{path}: variant {variant.get('name')} has a file without a name")
                continue
            referenced = path.parent / name
            if not referenced.is_file() or referenced.stat().st_size == 0:
                fail(
                    f"{path}: variant {variant.get('name')} references missing/empty "
                    f"{referenced}"
                )
        available_at = variant.get("available-at")
        if available_at and available_at.get("url"):
            referenced = (path.parent / available_at["url"]).resolve()
            if not referenced.is_file() or referenced.stat().st_size == 0:
                fail(f"{path}: available-at target is missing/empty: {referenced}")


engine_dir = base / "calendar-chinese-native" / version
jvm_dir = base / "calendar-chinese-native-jvm" / version
native_dir = base / "calendar-chinese-native-linuxx64" / version
check_module(engine_dir / f"calendar-chinese-native-{version}.module")
check_module(jvm_dir / f"calendar-chinese-native-jvm-{version}.module")
check_module(native_dir / f"calendar-chinese-native-linuxx64-{version}.module")

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
check_zip(
    native_dir / f"calendar-chinese-native-linuxx64-{version}.klib",
    ["default/manifest", "default/linkdata/"],
    "engine Linux klib",
)

for module in [
    "library",
    "calendar-gregorian",
    "calendar-hijri",
    "calendar-solarHijri",
    "calendar-solarHijri-native",
    "calendar-chinese-compose",
]:
    aar = base / module / version / f"{module}-{version}.aar"
    check_zip(aar, ["classes.jar"], f"{module} AAR")
    if aar.is_file():
        with zipfile.ZipFile(aar) as archive:
            classes = archive.read("classes.jar")
        if len(classes) < 100:
            fail(f"{module} AAR contains an empty classes.jar")


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


for engine_pom in [
    engine_dir / f"calendar-chinese-native-{version}.pom",
    jvm_dir / f"calendar-chinese-native-jvm-{version}.pom",
    native_dir / f"calendar-chinese-native-linuxx64-{version}.pom",
]:
    if engine_pom.is_file():
        text = engine_pom.read_text()
        for forbidden in ("calendar-gregorian", "calendar-hijri", "calendar-solarHijri", "androidx", "compose"):
            if forbidden in text:
                fail(f"{engine_pom} must not depend on {forbidden}")

compose_pom = base / "calendar-chinese-compose" / version / f"calendar-chinese-compose-{version}.pom"
if compose_pom.is_file():
    dependencies = pom_dependencies(compose_pom)
    for required in ("library", "calendar-chinese-native"):
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
print("Validated all publication binaries and Gradle Module Metadata references.")
PY

echo "Publication verification passed for $BASE at $VERSION."
