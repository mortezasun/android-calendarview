"""Tests for scripts/report-instrumented-failures.py."""

from __future__ import annotations

import contextlib
import importlib.util
import io
import tempfile
import unittest
from pathlib import Path

SCRIPT = Path(__file__).with_name("report-instrumented-failures.py")


def load_reporter():
    spec = importlib.util.spec_from_file_location("report_instrumented_failures", SCRIPT)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(module)
    return module


class ReportInstrumentedFailuresTest(unittest.TestCase):
    def setUp(self) -> None:
        self.reporter = load_reporter()
        self.original_directories = self.reporter.MODULE_REPORT_DIRECTORIES
        # Keep reports under the repository root so relative annotation paths work.
        self.temp = tempfile.TemporaryDirectory(dir=SCRIPT.parents[1])
        self.addCleanup(self.temp.cleanup)
        self.reports = Path(self.temp.name) / "reports"
        self.reports.mkdir()
        self.reporter.MODULE_REPORT_DIRECTORIES = (str(self.reports),)

    def tearDown(self) -> None:
        self.reporter.MODULE_REPORT_DIRECTORIES = self.original_directories

    def run_reporter(self) -> str:
        output = io.StringIO()
        with contextlib.redirect_stdout(output):
            self.reporter.main()
        return output.getvalue()

    def write_report(self, name: str, body: str) -> None:
        (self.reports / name).write_text(body, encoding="utf-8")

    @staticmethod
    def junit_case(inner: str, classname: str = "ExampleTest", testname: str = "fails") -> str:
        return (
            '<testsuite tests="1">'
            f'<testcase classname="{classname}" name="{testname}">{inner}</testcase>'
            "</testsuite>"
        )

    def test_failure_is_reported_with_test_name_and_message(self) -> None:
        self.write_report(
            "TEST-sample.xml",
            self.junit_case('<failure message="Expected true">stack trace</failure>'),
        )
        output = self.run_reporter()
        self.assertIn("ExampleTest.fails failed", output)
        self.assertIn("Expected true", output)
        self.assertIn("1 failed case(s)", output)

    def test_error_element_is_reported(self) -> None:
        self.write_report(
            "TEST-sample.xml",
            self.junit_case('<error message="Could not launch">stack trace</error>'),
        )
        output = self.run_reporter()
        self.assertIn("ExampleTest.fails failed", output)
        self.assertIn("Could not launch", output)

    def test_duplicate_reports_produce_one_annotation(self) -> None:
        body = self.junit_case('<failure message="Same failure" />')
        self.write_report("TEST-one.xml", body)
        self.write_report("TEST-two.xml", body)
        output = self.run_reporter()
        self.assertEqual(output.count("::error file="), 1)
        self.assertIn("1 failed case(s)", output)

    def test_invalid_xml_emits_a_warning(self) -> None:
        self.write_report("TEST-broken.xml", "<testsuite")
        output = self.run_reporter()
        self.assertIn("::warning file=", output)
        self.assertIn("Could not parse connected-test report", output)

    def test_missing_reports_emits_infrastructure_error(self) -> None:
        output = self.run_reporter()
        self.assertIn("No connected-test JUnit XML report was produced", output)
        self.assertNotIn("failed case(s)", output)

    def test_successful_cases_do_not_claim_test_failures(self) -> None:
        self.write_report("TEST-success.xml", self.junit_case(""))
        output = self.run_reporter()
        self.assertNotIn("failed case(s)", output)
        self.assertIn("failed without a failed testcase", output)

    def test_percent_and_whitespace_are_escaped_and_normalized(self) -> None:
        self.write_report(
            "TEST-escape.xml",
            self.junit_case('<failure message="100%\nsecond line" />'),
        )
        output = self.run_reporter()
        self.assertIn("100%25 second line", output)
        self.assertNotIn("100%\n", output)

    def test_long_message_is_limited(self) -> None:
        self.write_report(
            "TEST-long.xml",
            self.junit_case(f'<failure message="{"x" * 2000}TAIL" />'),
        )
        output = self.run_reporter()
        self.assertNotIn("TAIL", output)
        self.assertIn("x" * self.reporter.MAX_MESSAGE_LENGTH, output)


if __name__ == "__main__":
    unittest.main()
