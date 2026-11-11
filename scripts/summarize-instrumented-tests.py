#!/usr/bin/env python3
"""Publish exact connected-test counts as public GitHub Actions annotations."""

from __future__ import annotations

import xml.etree.ElementTree as ET
from pathlib import Path

MODULE_REPORT_DIRECTORIES = {
    "library": (
        "library/build/outputs/androidTest-results",
        "library/build/reports/androidTests",
    ),
    "sample": (
        "calendarviewsample/build/outputs/androidTest-results",
        "calendarviewsample/build/reports/androidTests",
    ),
}


def collect_module(root: Path, directories: tuple[str, ...]) -> dict[str, int]:
    seen_cases: set[tuple[str, str]] = set()
    classes: set[str] = set()
    report_count = 0
    tests = failures = errors = skipped = 0

    for relative_directory in directories:
        directory = root / relative_directory
        if not directory.is_dir():
            continue
        for report in sorted(directory.rglob("TEST-*.xml")):
            try:
                tree = ET.parse(report)
            except ET.ParseError:
                continue
            report_count += 1
            for case in tree.iter("testcase"):
                class_name = case.get("classname") or "unknown class"
                test_name = case.get("name") or "unknown test"
                identity = (class_name, test_name)
                if identity in seen_cases:
                    continue
                seen_cases.add(identity)
                classes.add(class_name)
                tests += 1
                if case.find("failure") is not None:
                    failures += 1
                if case.find("error") is not None:
                    errors += 1
                if case.find("skipped") is not None:
                    skipped += 1

    return {
        "reports": report_count,
        "classes": len(classes),
        "tests": tests,
        "failures": failures,
        "errors": errors,
        "skipped": skipped,
    }


def main() -> int:
    root = Path(__file__).resolve().parents[1]
    totals = {"reports": 0, "classes": 0, "tests": 0, "failures": 0, "errors": 0, "skipped": 0}
    for module, directories in MODULE_REPORT_DIRECTORIES.items():
        counts = collect_module(root, directories)
        for key in ("reports", "tests", "failures", "errors", "skipped"):
            totals[key] += counts[key]
        totals["classes"] += counts["classes"]
        print(
            f"::notice title={module.capitalize()} instrumented tests::"
            f"tests={counts['tests']} failures={counts['failures']} "
            f"errors={counts['errors']} skipped={counts['skipped']} "
            f"classes={counts['classes']} reports={counts['reports']}"
        )
        # print() also keeps the counts in the step log for artifact-free reading.
        print(f"{module}: {counts}")

    print(
        "::notice title=Instrumented test summary::"
        f"tests={totals['tests']} failures={totals['failures']} "
        f"errors={totals['errors']} skipped={totals['skipped']} "
        f"classes={totals['classes']}"
    )
    print(f"total: {totals}")
    if totals["tests"] == 0:
        print(
            "::warning title=Instrumented tests::"
            "No connected-test JUnit XML reports were found; inspect the uploaded "
            "android-instrumentation artifact."
        )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
