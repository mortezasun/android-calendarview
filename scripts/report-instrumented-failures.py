#!/usr/bin/env python3
"""Publish failed connected-test cases as GitHub Actions annotations.

The Actions log viewer requires a signed-in account, while check-run annotations
are public. Parsing the JUnit XML keeps the failing class and method visible
even when the instrumentation artifact cannot be downloaded.
"""

from __future__ import annotations

import xml.etree.ElementTree as ET
from pathlib import Path

MODULE_REPORT_DIRECTORIES = (
    "library/build/outputs/androidTest-results",
    "library/build/reports/androidTests",
    "calendarviewsample/build/outputs/androidTest-results",
    "calendarviewsample/build/reports/androidTests",
)
MAX_MESSAGE_LENGTH = 1200


def escape_workflow_command(value: str) -> str:
    """Escape a value that is printed after a GitHub Actions command separator."""
    return value.replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")


def case_failure_message(case: ET.Element) -> str | None:
    failure = case.find("failure")
    if failure is None:
        failure = case.find("error")
    if failure is None:
        return None

    message = failure.get("message") or failure.text or "failed"
    normalized = " ".join(message.split())
    return normalized[:MAX_MESSAGE_LENGTH]


def main() -> int:
    root = Path(__file__).resolve().parents[1]
    report_count = 0
    failures: list[tuple[Path, str, str]] = []
    seen: set[tuple[str, str]] = set()

    for relative_directory in MODULE_REPORT_DIRECTORIES:
        directory = root / relative_directory
        if not directory.is_dir():
            continue
        for report in sorted(directory.rglob("TEST-*.xml")):
            report_count += 1
            try:
                tree = ET.parse(report)
            except ET.ParseError as error:
                print(
                    f"::warning file={report.relative_to(root)}::"
                    f"Could not parse connected-test report: {escape_workflow_command(str(error))}"
                )
                continue
            for case in tree.iter("testcase"):
                message = case_failure_message(case)
                if message is None:
                    continue
                class_name = case.get("classname") or "unknown class"
                test_name = case.get("name") or "unknown test"
                identity = f"{class_name}.{test_name}"
                if (identity, message) in seen:
                    continue
                seen.add((identity, message))
                failures.append((report, identity, message))

    for report, identity, message in failures:
        print(
            f"::error file={report.relative_to(root)}::"
            f"{escape_workflow_command(identity)} failed: {escape_workflow_command(message)}"
        )

    if failures:
        summary = "; ".join(f"{identity}: {message}" for _, identity, message in failures)
        print(
            "::error title=Instrumented test failures::"
            f"{len(failures)} failed case(s): {escape_workflow_command(summary[:6000])}"
        )
    elif report_count == 0:
        print(
            "::error title=Instrumented tests::"
            "No connected-test JUnit XML report was produced; inspect the uploaded "
            "android-instrumentation artifact for the Gradle and Logcat diagnostics."
        )
    else:
        print(
            "::error title=Instrumented tests::"
            "The connected-test task failed without a failed testcase; inspect the uploaded "
            "android-instrumentation artifact for the Gradle and Logcat diagnostics."
        )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
