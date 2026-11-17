#!/usr/bin/env bash
# Guards the Kotlin Multiplatform boundary: commonMain must stay free of JVM/Android/Apple/Web
# platform APIs and of the removed jalalicalendar dependency. Compose UI packages are the only
# allowed androidx imports.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

status=0
while IFS= read -r line; do
    [[ -z "$line" ]] && continue
    echo "Forbidden platform import in commonMain: $line" >&2
    status=1
done < <(grep -rnE '^import (java|javax|android|ir\.huri|platform|kotlinx\.browser|kotlin\.js)\.' \
    --include="*.kt" library/src/commonMain calendar-*/src/commonMain 2>/dev/null || true)

while IFS= read -r line; do
    [[ -z "$line" ]] && continue
    echo "Forbidden non-Compose androidx import in commonMain: $line" >&2
    status=1
done < <(grep -rnE '^import androidx\.' \
    --include="*.kt" library/src/commonMain calendar-*/src/commonMain 2>/dev/null \
    | grep -vE '^[^:]+:[0-9]+:import androidx\.compose\.' || true)

while IFS= read -r line; do
    [[ -z "$line" ]] && continue
    echo "Removed jalalicalendar dependency is referenced: $line" >&2
    status=1
done < <(grep -rn "ir\.huri" --include="*.kts" --include="*.toml" . 2>/dev/null \
    | grep -vE '/build/' || true)

if [[ "$status" -ne 0 ]]; then
    echo "commonMain purity check failed." >&2
    exit 1
fi
echo "commonMain purity check passed."
