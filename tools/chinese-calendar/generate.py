#!/usr/bin/env python3
"""Development-time generator for the native Chinese calendar month table.

This tool is NOT part of the published library. It computes the Chinese lunisolar
calendar for a fixed Gregorian window from first principles:

* Earth's heliocentric position from the VSOP87D theory (Bretagnon & Francou, 1988),
  truncated to the terms that matter below one arcsecond.
* New moon instants from the Meeus "Astronomical Algorithms" chapter 49 series.
* Solar terms by solving the apparent solar longitude.
* Delta T from the Espenak & Meeus polynomial expressions.

The Chinese calendar rules applied here are the documented civil rules: months begin
on the day (UTC+08:00) of the astronomical new moon, month 11 contains the winter
solstice, and in a year with 13 months the first month without a principal term is
the leap month.

HKO conversion tables are used only as an independent development-time reference
(``--hko-dir``); no HKO data is embedded in the generated table or the library.
"""

import argparse
import bisect
import datetime
import hashlib
import json
import math
import os
import re
import subprocess
import sys

EPOCH_JD = 2440587.5  # 1970-01-01 00:00 UT
EPOCH_ORDINAL = datetime.date(1970, 1, 1).toordinal()


# --------------------------------------------------------------------------- VSOP87
def parse_vsop(path):
    series = {1: {}, 2: {}, 3: {}}  # variable -> power -> [(A, B, C)]
    variable = power = None
    with open(path, encoding="latin-1") as handle:
        for line in handle:
            if "VSOP87" in line and "VARIABLE" in line:
                match = re.search(r"VARIABLE (\d) \(LBR\)\s+\*T\*\*(\d)", line)
                if match:
                    variable, power = int(match.group(1)), int(match.group(2))
                continue
            if variable is None:
                continue
            parts = line.split()
            if len(parts) < 3:
                continue
            try:
                amplitude, phase, frequency = (float(parts[-3]), float(parts[-2]), float(parts[-1]))
            except ValueError:
                continue
            series[variable].setdefault(power, []).append((amplitude, phase, frequency))
    return series


def vsop_eval(series, variable, t, threshold):
    total = 0.0
    for power, terms in series[variable].items():
        partial = 0.0
        for amplitude, phase, frequency in terms:
            if abs(amplitude) < threshold:
                continue
            partial += amplitude * math.cos(phase + frequency * t)
        total += partial * t**power
    return total


def norm360(value):
    return value % 360.0


def nutation_longitude(centuries):
    """Meeus 22.2, accuracy about 0.5 arcsecond. Returns degrees."""
    omega = norm360(125.04452 - 1934.136261 * centuries + 0.0020708 * centuries**2 + centuries**3 / 450000.0)
    sun_mean = norm360(280.4665 + 36000.7698 * centuries)
    moon_mean = norm360(218.3165 + 481267.8813 * centuries)
    delta = (
        -17.20 * math.sin(math.radians(omega))
        - 1.32 * math.sin(math.radians(2 * sun_mean))
        - 0.23 * math.sin(math.radians(2 * moon_mean))
        + 0.21 * math.sin(math.radians(2 * omega))
    )
    return delta / 3600.0


def sun_apparent_longitude(series, jde):
    millennia = (jde - 2451545.0) / 365250.0
    lon = math.degrees(vsop_eval(series, 1, millennia, 1e-7)) + 180.0
    radius = vsop_eval(series, 3, millennia, 1e-6)
    centuries = (jde - 2451545.0) / 36525.0
    return norm360(lon + nutation_longitude(centuries) - (20.4898 / 3600.0) / radius)


def solve_solar_longitude(series, target, guess_jde):
    jde = guess_jde
    for _ in range(40):
        diff = ((target - sun_apparent_longitude(series, jde) + 180.0) % 360.0) - 180.0
        step = diff / 0.98564736
        jde += step
        if abs(step) < 1e-7:
            break
    return jde


# --------------------------------------------------------------------------- Delta T
def delta_t_days(year):
    if 1900 <= year < 1920:
        t = year - 1900
        seconds = -2.79 + 1.494119 * t - 0.0598939 * t**2 + 0.0061966 * t**3 - 0.000197 * t**4
    elif 1920 <= year < 1941:
        t = year - 1920
        seconds = 21.20 + 0.84493 * t - 0.076100 * t**2 + 0.0020936 * t**3
    elif 1941 <= year < 1961:
        t = year - 1950
        seconds = 29.07 + 0.407 * t - t**2 / 233.0 + t**3 / 2547.0
    elif 1961 <= year < 1986:
        t = year - 1975
        seconds = 45.45 + 1.067 * t - t**2 / 260.0 - t**3 / 718.0
    elif 1986 <= year < 2005:
        t = year - 2000
        seconds = (63.86 + 0.3345 * t - 0.060374 * t**2 + 0.0017275 * t**3
                   + 0.000651814 * t**4 + 0.00002373599 * t**5)
    elif 2005 <= year < 2050:
        t = year - 2000
        seconds = 62.92 + 0.32217 * t + 0.005589 * t**2
    elif 2050 <= year <= 2150:
        seconds = -20 + 32 * ((year - 1820) / 100.0) ** 2 - 0.5628 * (2150 - year)
    else:
        raise ValueError("delta T not modelled for %d" % year)
    return seconds / 86400.0


# --------------------------------------------------------------------------- new moon
def new_moon_jde(k):
    """Meeus chapter 49; result is in terrestrial time (JDE)."""
    centuries = k / 1236.85
    jde = (2451550.09766 + 29.530588861 * k + 0.00015437 * centuries**2
           - 0.000000150 * centuries**3 + 0.00000000073 * centuries**4)
    factor = 1 - 0.002516 * centuries - 0.0000074 * centuries**2
    sun_anomaly = norm360(2.5534 + 29.10535670 * k - 0.0000014 * centuries**2 - 0.00000011 * centuries**3)
    moon_anomaly = norm360(201.5643 + 385.81693528 * k + 0.0107582 * centuries**2
                           + 0.00001238 * centuries**3 - 0.000000058 * centuries**4)
    argument_latitude = norm360(160.7108 + 390.67050284 * k - 0.0016118 * centuries**2
                                - 0.00000227 * centuries**3 + 0.000000011 * centuries**4)
    node = norm360(124.7746 - 1.56375588 * k + 0.0020672 * centuries**2 + 0.00000215 * centuries**3)
    sin = lambda degrees: math.sin(math.radians(degrees))
    correction = (
        -0.40720 * sin(moon_anomaly)
        + 0.17241 * factor * sin(sun_anomaly)
        + 0.01608 * sin(2 * moon_anomaly)
        + 0.01039 * sin(2 * argument_latitude)
        + 0.00739 * factor * sin(moon_anomaly - sun_anomaly)
        - 0.00514 * factor * sin(moon_anomaly + sun_anomaly)
        + 0.00208 * factor * factor * sin(2 * sun_anomaly)
        - 0.00111 * sin(moon_anomaly - 2 * argument_latitude)
        - 0.00057 * sin(moon_anomaly + 2 * argument_latitude)
        + 0.00056 * factor * sin(2 * moon_anomaly + sun_anomaly)
        - 0.00042 * sin(3 * moon_anomaly)
        + 0.00042 * factor * sin(sun_anomaly + 2 * argument_latitude)
        + 0.00038 * factor * sin(sun_anomaly - 2 * argument_latitude)
        - 0.00024 * factor * sin(2 * moon_anomaly - sun_anomaly)
        - 0.00017 * sin(node)
        - 0.00007 * sin(moon_anomaly + 2 * sun_anomaly)
        + 0.00004 * sin(2 * moon_anomaly - 2 * argument_latitude)
        + 0.00004 * sin(3 * sun_anomaly)
        + 0.00003 * sin(moon_anomaly + sun_anomaly - 2 * argument_latitude)
        + 0.00003 * sin(2 * moon_anomaly + 2 * argument_latitude)
        - 0.00003 * sin(moon_anomaly + sun_anomaly + 2 * argument_latitude)
        + 0.00003 * sin(moon_anomaly - sun_anomaly + 2 * argument_latitude)
        - 0.00002 * sin(moon_anomaly - sun_anomaly - 2 * argument_latitude)
        - 0.00002 * sin(3 * moon_anomaly + sun_anomaly)
        + 0.00002 * sin(4 * moon_anomaly)
    )
    planetary_args = [
        299.77 + 0.107408 * k - 0.009173 * centuries**2,
        251.88 + 0.016321 * k,
        251.83 + 26.651886 * k,
        349.42 + 36.412478 * k,
        84.66 + 18.206239 * k,
        141.74 + 53.303771 * k,
        207.14 + 2.453732 * k,
        154.84 + 7.306860 * k,
        34.52 + 27.261239 * k,
        207.19 + 0.121824 * k,
        291.34 + 1.844379 * k,
        161.72 + 24.198154 * k,
        239.56 + 25.513099 * k,
        331.55 + 3.592518 * k,
    ]
    planetary_amplitudes = [0.000325, 0.000165, 0.000164, 0.000126, 0.000110, 0.000062, 0.000060,
                            0.000056, 0.000047, 0.000042, 0.000040, 0.000037, 0.000035, 0.000023]
    correction += sum(a * sin(arg) for a, arg in zip(planetary_amplitudes, planetary_args))
    return jde + correction


# --------------------------------------------------------------------------- months
def local_epoch_day(jde, year):
    return math.floor(jde - delta_t_days(year) + 8.0 / 24.0 - EPOCH_JD + 1e-9)


def civil_date(epoch_day):
    return datetime.date.fromordinal(EPOCH_ORDINAL + int(epoch_day))


def build_table(series, first_year, last_year):
    """Return per Chinese year: start epoch day, leap month number (0 = none), month lengths."""
    solstices = {}
    for year in range(first_year - 1, last_year + 2):
        guess = 2451545.0 + (year - 2000) * 365.2422 + 355.0
        jde = solve_solar_longitude(series, 270.0, guess)
        while civil_date(local_epoch_day(jde, year)).year < year:
            jde = solve_solar_longitude(series, 270.0, jde + 360)
        while civil_date(local_epoch_day(jde, year)).year > year:
            jde = solve_solar_longitude(series, 270.0, jde - 360)
        solstices[year] = jde

    principal_terms = []
    for year in range(first_year - 1, last_year + 3):
        for target in range(0, 360, 30):
            guess = 2451545.0 + (year - 2000) * 365.2422 + (target - 280.0) / 0.98565
            jde = solve_solar_longitude(series, float(target), guess)
            while civil_date(local_epoch_day(jde, year)).year < year:
                jde = solve_solar_longitude(series, float(target), jde + 365.2422)
            while civil_date(local_epoch_day(jde, year)).year > year:
                jde = solve_solar_longitude(series, float(target), jde - 365.2422)
            principal_terms.append(local_epoch_day(jde, year))
    principal_terms.sort()

    first_k = int(math.floor((first_year - 0.2 - 2000) * 12.3685))
    last_k = int(math.ceil((last_year + 2.2 - 2000) * 12.3685))
    moons = []
    for k in range(first_k, last_k + 1):
        jde = new_moon_jde(k)
        moons.append((jde, local_epoch_day(jde, 2000 + k / 12.3685)))
    start_days = [moon[1] for moon in moons]

    month11_index = {}
    for year, solstice in solstices.items():
        solstice_day = local_epoch_day(solstice, year)
        index = bisect.bisect_right(start_days, solstice_day) - 1
        month11_index[year] = index
        if not (start_days[index] <= solstice_day < start_days[index + 1]):
            raise AssertionError("winter solstice not inside month 11 for %d" % year)

    months = []  # (jde, epoch_day, gregorian_year_of_sui, number, leap)
    for year in range(first_year, last_year + 2):
        start_index = month11_index[year - 1]
        end_index = month11_index[year]
        count = end_index - start_index
        if count not in (12, 13):
            raise AssertionError("sui %d has %d months" % (year, count))
        leap_index = None
        if count == 13:
            for index in range(start_index, end_index):
                month_start, next_start = moons[index][1], moons[index + 1][1]
                if not any(month_start <= term < next_start for term in principal_terms):
                    leap_index = index
                    break
            if leap_index is None:
                raise AssertionError("13-month sui %d has no month without principal term" % year)
        number = 11
        for index in range(start_index, end_index):
            if index == start_index:
                month_number, leap = 11, False
            elif leap_index is not None and index == leap_index:
                month_number, leap = number, True
            else:
                number = number % 12 + 1
                month_number, leap = number, False
            months.append((moons[index][0], moons[index][1], year, month_number, leap))

    years = {}
    current = None
    for month in months:
        if month[3] == 1 and not month[4]:
            current = civil_date(month[1]).year
            years[current] = []
        if current is not None:
            years[current].append(month)

    table = []
    for year in range(first_year, last_year + 1):
        months_of_year = years[year]
        if not months_of_year:
            raise AssertionError("missing year %d" % year)
        if months_of_year[0][3] != 1 or months_of_year[0][4]:
            raise AssertionError("year %d does not start with month 1" % year)
        leap_numbers = [m[3] for m in months_of_year if m[4]]
        if len(leap_numbers) > 1:
            raise AssertionError("year %d has several leap months" % year)
        lengths = []
        for position, month in enumerate(months_of_year):
            next_start = (months_of_year[position + 1][1]
                          if position + 1 < len(months_of_year)
                          else years[year + 1][0][1])
            length = next_start - month[1]
            if length not in (29, 30):
                raise AssertionError("year %d month %d has %d days" % (year, month[3], length))
            lengths.append(length)
        table.append({
            "year": year,
            "startEpochDay": months_of_year[0][1],
            "leapMonth": leap_numbers[0] if leap_numbers else 0,
            "monthLengths": lengths,
        })
    return table


# --------------------------------------------------------------------------- HKO
def load_hko(path):
    """Reference month starts from an HKO English conversion table."""
    starts = {}
    with open(path, encoding="latin-1") as handle:
        for line in handle:
            if not re.search(r"Lunar [Mm]onth", line):
                continue
            parts = line.split()
            year, month, day = map(int, parts[0].split("/"))
            number = int(re.match(r"(\d+)", parts[1]).group(1))
            starts[datetime.date(year, month, day)] = number
    return starts


def compare_with_hko(table, hko_dir):
    compared = 0
    mismatches = []
    cache = {}

    def reference_for(year):
        if year not in cache:
            path = os.path.join(hko_dir, "T%de.txt" % year)
            cache[year] = load_hko(path) if os.path.exists(path) else {}
        return cache[year]

    for entry in table:
        for index, length in enumerate(entry["monthLengths"]):
            epoch_day = entry["startEpochDay"] + sum(entry["monthLengths"][:index])
            current = civil_date(epoch_day)
            number = index + 1 if entry["leapMonth"] == 0 or index < entry["leapMonth"] else index
            # month number in chronological order
            number = month_number_at(entry, index)
            reference = reference_for(current.year)
            compared += 1
            if current not in reference:
                nearby = sorted(
                    (day, value) for day, value in reference.items()
                    if abs((day - current).days) <= 3
                )
                mismatches.append({
                    "date": current.isoformat(),
                    "ours": number,
                    "hko": None,
                    "nearestHko": [
                        {"date": day.isoformat(), "month": value} for day, value in nearby
                    ],
                })
            elif reference[current] != number:
                mismatches.append({"date": current.isoformat(), "ours": number, "hko": reference[current]})
            _ = length
    return compared, mismatches


def month_number_at(entry, index):
    """Chronological month number for the month at [index] inside a year."""
    leap = entry["leapMonth"]
    if leap == 0:
        return (index % 12) + 1
    # months before and including the repeated month, then the following numbers
    if index <= leap:
        return min(index + 1, leap)
    return index


# --------------------------------------------------------------------------- output
def kotlin_source(table, dataset_version):
    rows = []
    for entry in table:
        mask = 0
        for index, length in enumerate(entry["monthLengths"]):
            if length == 30:
                mask |= 1 << index
        packed = (entry["leapMonth"] << 16) | mask
        rows.append((entry["startEpochDay"], packed))
    lines = []
    lines.append("package com.develotter.calendarview.calendars.chineseNative")
    lines.append("")
    lines.append("// Generated by tools/chinese-calendar/generate.py. Do not edit by hand.")
    lines.append("// Dataset %s; see tools/chinese-calendar/manifest.json." % dataset_version)
    lines.append("internal object ChineseCalendarData {")
    lines.append("    const val FIRST_YEAR: Int = %d" % table[0]["year"])
    lines.append("    const val LAST_YEAR: Int = %d" % table[-1]["year"])
    lines.append("    const val DATASET_VERSION: String = \"%s\"" % dataset_version)
    lines.append("")
    lines.append("    // Two ints per year: start epoch day of month 1, then")
    lines.append("    // (leap month number << 16) | bitmask of 30-day months in chronological order.")
    lines.append("    val YEARS: IntArray = intArrayOf(")
    for index in range(0, len(rows), 4):
        chunk = rows[index:index + 4]
        lines.append("        " + ", ".join("%d, %d" % pair for pair in chunk) + ",")
    lines.append("    )")
    lines.append("}")
    return "\n".join(lines) + "\n"


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--vsop", default=os.environ.get("VSOP87D_EAR", ""))
    parser.add_argument("--hko-dir", default=os.environ.get("HKO_DIR", ""))
    parser.add_argument("--first-year", type=int, default=1901)
    parser.add_argument("--last-year", type=int, default=2099)
    parser.add_argument("--output", default="")
    parser.add_argument("--manifest", default="")
    parser.add_argument("--dataset-version", default="2026.11-astronomical-v1")
    args = parser.parse_args()
    if not args.vsop or not os.path.exists(args.vsop):
        sys.exit("VSOP87D.ear missing; pass --vsop or set VSOP87D_EAR")

    with open(args.vsop, "rb") as handle:
        vsop_hash = hashlib.sha256(handle.read()).hexdigest()
    series = parse_vsop(args.vsop)
    table = build_table(series, args.first_year, args.last_year)

    compared = 0
    mismatches = []
    if args.hko_dir:
        compared, mismatches = compare_with_hko(table, args.hko_dir)

    source = kotlin_source(table, args.dataset_version)
    source_hash = hashlib.sha256(source.encode()).hexdigest()
    if args.output:
        with open(args.output, "w", encoding="utf-8") as handle:
            handle.write(source)

    manifest = {
        "datasetVersion": args.dataset_version,
        "generator": "tools/chinese-calendar/generate.py",
        "generatorVersion": 1,
        "range": {"firstYear": args.first_year, "lastYear": args.last_year},
        "sources": {
            "vsop87d": {
                "url": "https://ftp.imcce.fr/pub/ephem/planets/vsop87/VSOP87D.ear",
                "sha256": vsop_hash,
                "usage": "Development-time astronomy only; the file is not redistributed.",
            },
            "algorithms": [
                "VSOP87D Earth series (Bretagnon & Francou 1988), truncated below 1e-7 rad.",
                "Meeus, Astronomical Algorithms, 2nd ed., chapter 25 (apparent solar longitude), "
                "chapter 49 (new moon).",
                "Espenak & Meeus Delta T polynomial expressions.",
            ],
            "reference": {
                "name": "Hong Kong Observatory Gregorian-Lunar conversion tables",
                "url": "https://www.hko.gov.hk/en/gts/time/conversion.htm",
                "usage": "Development-time comparison only; no HKO content is embedded.",
                "terms": "https://www.hko.gov.hk/en/readme/readme.htm",
                "monthsCompared": compared,
                "mismatches": mismatches,
            },
        },
        "output": {
            "kotlinFile": os.path.basename(args.output) if args.output else None,
            "sha256": source_hash,
            "years": len(table),
            "months": sum(len(entry["monthLengths"]) for entry in table),
            "leapYears": sum(1 for entry in table if entry["leapMonth"]),
        },
    }
    if args.manifest:
        with open(args.manifest, "w", encoding="utf-8") as handle:
            json.dump(manifest, handle, indent=2, sort_keys=True)
            handle.write("\n")
    print(json.dumps({k: manifest["output"][k] for k in ("years", "months", "leapYears", "sha256")}, indent=2))
    print("reference compared:", compared, "mismatches:", len(mismatches))
    for item in mismatches[:20]:
        print("  ", item)


if __name__ == "__main__":
    main()
