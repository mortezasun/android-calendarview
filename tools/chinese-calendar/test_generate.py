#!/usr/bin/env python3
"""Regression tests for the development-time HKO comparison, run without VSOP87D."""

import copy
import datetime
import importlib.util
import json
import os
import tempfile
import unittest

HERE = os.path.dirname(os.path.abspath(__file__))
SPEC = importlib.util.spec_from_file_location("generate", os.path.join(HERE, "generate.py"))
GENERATE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(GENERATE)

EPOCH = datetime.date(1970, 1, 1)


def epoch_day(value):
    return value.toordinal() - EPOCH.toordinal()


def write_hko(directory, year, days):
    with open(os.path.join(directory, "T%de.txt" % year), "w", encoding="latin-1") as handle:
        handle.write("Gregorian-Lunar Calendar Conversion Table\n")
        for day, label in days:
            handle.write("%s     %s Lunar month     Monday\n" % (day.strftime("%Y/%m/%d"), label))


def month(year, start, lengths, leap=0):
    return {
        "year": year,
        "startEpochDay": epoch_day(start),
        "leapMonth": leap,
        "monthLengths": lengths,
    }


class HkoComparisonTest(unittest.TestCase):
    def test_missing_reference_file_is_an_explicit_coverage_gap(self):
        with tempfile.TemporaryDirectory() as directory:
            report = GENERATE.compare_with_hko([month(2024, datetime.date(2024, 2, 10), [29])], directory)
        self.assertIn("T2024e.txt", report["missingReferenceFiles"])
        self.assertEqual(["2024-02-10"], report["missingReferenceDates"])
        self.assertFalse(report["complete"])
        errors = GENERATE.verify_reference(report)
        self.assertTrue(any("missing reference files" in error for error in errors))
        self.assertTrue(any("missing reference dates" in error for error in errors))

    def test_year_and_leap_status_are_checked_in_addition_to_the_number(self):
        start = datetime.date(2024, 2, 10)
        leap_start = datetime.date(2024, 3, 10)
        with tempfile.TemporaryDirectory() as directory:
            write_hko(directory, 2024, [
                (datetime.date(2024, 1, 1), "12th"),
                (start, "1st"),
                (leap_start, "1st"),
            ])
            matching = month(2024, start, [29, 29], leap=1)
            report = GENERATE.compare_with_hko([matching], directory)
            self.assertEqual(2, report["monthsExactMatch"])
            self.assertTrue(report["complete"])

            wrong_number = month(2024, start, [29, 29], leap=0)
            report = GENERATE.compare_with_hko([wrong_number], directory)
            self.assertFalse(report["complete"])
            self.assertTrue(any(
                "leap" in detail
                for mismatch in report["mismatches"]
                for detail in mismatch.get("details", [])
            ))

        with tempfile.TemporaryDirectory() as directory:
            year_start = datetime.date(2025, 2, 10)
            write_hko(directory, 2025, [
                (year_start, "1st"),
                (datetime.date(2025, 3, 10), "1st"),
            ])
            wrong_year = month(2024, year_start, [29, 29], leap=1)
            report = GENERATE.compare_with_hko([wrong_year], directory)
            self.assertTrue(any(
                "chineseYear" in detail
                for mismatch in report["mismatches"]
                for detail in mismatch.get("details", [])
            ))

    def test_documented_conflict_requires_reproducible_astronomy(self):
        with open(os.path.join(HERE, "reference-conflicts.json"), encoding="utf-8") as handle:
            conflict = json.load(handle)["conflicts"][1]
        start = datetime.date.fromisoformat(conflict["generatedDate"])
        hko_date = datetime.date.fromisoformat(conflict["hkoDate"])
        entry = month(conflict["chineseYear"], start, [29])
        with tempfile.TemporaryDirectory() as directory:
            write_hko(directory, 1916, [
                (hko_date, "1st"),
            ])
            report = GENERATE.compare_with_hko([entry], directory, [conflict])
            self.assertEqual(0, report["monthsExactMatch"])
            self.assertEqual(1, report["monthsDocumentedConflict"])
            self.assertEqual([], report["mismatches"])
            self.assertTrue(report["complete"])

            incomplete = dict(conflict)
            del incomplete["deltaTSeconds"]
            report = GENERATE.compare_with_hko([entry], directory, [incomplete])
            self.assertFalse(report["complete"])
            self.assertTrue(any(
                mismatch["kind"] == "invalidDocumentedConflict"
                for mismatch in report["mismatches"]
            ))
            self.assertTrue(GENERATE.verify_reference(report))

    def test_conflict_instants_must_agree_and_use_structured_reference_tolerance(self):
        with open(os.path.join(HERE, "reference-conflicts.json"), encoding="utf-8") as handle:
            conflict = json.load(handle)["conflicts"][0]
        start = datetime.date.fromisoformat(conflict["generatedDate"])

        def check(value):
            return GENERATE.verify_conflict_astronomy(
                value,
                epoch_day(start),
                value["monthNumber"],
                value["chineseYear"],
                value["isLeapMonth"],
            )

        self.assertEqual([], check(conflict))

        different_instant = copy.deepcopy(conflict)
        different_instant["newMoonUtcPlus8"] = "1914-11-18T00:01:43.986+08:00"
        self.assertTrue(any(
            "same instant" in error for error in check(different_instant)
        ))

        missing_local_zone = copy.deepcopy(conflict)
        missing_local_zone["newMoonUtcPlus8"] = "1914-11-18T00:01:42.986"
        self.assertTrue(any(
            "timezone offset" in error for error in check(missing_local_zone)
        ))

        missing_utc_zone = copy.deepcopy(conflict)
        missing_utc_zone["newMoonUtc"] = "1914-11-17T16:01:42.986"
        self.assertTrue(any(
            "timezone offset" in error for error in check(missing_utc_zone)
        ))

        outside_tolerance = copy.deepcopy(conflict)
        outside_tolerance["independentReference"]["newMoonUtc"] = "1914-11-17T16:04:00Z"
        self.assertTrue(any(
            "tolerance 60.0s" in error for error in check(outside_tolerance)
        ))

        too_tight = copy.deepcopy(conflict)
        too_tight["independentReference"]["toleranceSeconds"] = 5
        self.assertTrue(any(
            "tolerance 5.0s" in error for error in check(too_tight)
        ))

    def test_1914_reference_uses_the_actual_nasa_minute_and_marks_hko_reason_unresolved(self):
        with open(os.path.join(HERE, "reference-conflicts.json"), encoding="utf-8") as handle:
            conflict = json.load(handle)["conflicts"][0]
        self.assertEqual("1914-11-17T16:02:00Z", conflict["independentReference"]["newMoonUtc"])
        self.assertEqual(60, conflict["independentReference"]["toleranceSeconds"])
        self.assertIn("unresolved", conflict["classification"])

    def test_unallowed_field_mismatch_fails_verification(self):
        start = datetime.date(2024, 2, 10)
        with tempfile.TemporaryDirectory() as directory:
            write_hko(directory, 2024, [(start, "2nd")])
            report = GENERATE.compare_with_hko([month(2024, start, [29])], directory)
        self.assertFalse(report["complete"])
        self.assertTrue(GENERATE.verify_reference(report))


if __name__ == "__main__":
    unittest.main()
