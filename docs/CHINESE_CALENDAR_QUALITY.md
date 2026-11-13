# 2.4.0: Test plan, vulnerabilities and security gates

Status: the core matrix was executed locally on 2026-11-13 (JDK 21, JVM + linuxX64) and in
remote CI on `1ab841e70b6fb7ab35b16e970715b500b91f95a2`, on review-fix commit `e80c00f`,
and on the verified release source HEAD `4dc484ae8cff91bad0869cd8d24e0b71144ff8e9`. The follow-up fixes
on `6fac725` (`5cf640d`) were verified locally; pushed tree `952bbb6` succeeded in all four
remote workflows. On 2026-11-13 a JVM benchmark baseline and an OSV advisory scan were also
executed. The Chinese device/fold/TalkBack/large-font/RTL matrix remains open. This plan claims
neither that the build tooling is vulnerability-free nor that physical device acceptance is
complete.

## Functional mandatory matrix

- [x] Convert every supported epoch day to Chinese and back; verify every valid Chinese day in
  reverse. (72,674 days, `wholeRangeRoundTrips`.)
- [x] Compare against additional independent reference data. Round trips alone do not prove a
  correct calendar mapping. (Generator: 2,458 exact + 3 audited differences = 2,461 HKO month
  starts, 0 missing references; the differences are documented with structured NASA/GSFC new
  moon times and a 60 s tolerance; `--verify-reference` requires complete file/start coverage,
  year/leap status, identical UTC/UTC+8 instants and explicit time zones. The HKO rows are
  consistent with Beijing local time before 1929; the library deliberately remains on UTC+8 and
  treats them as documented historical reference differences.)
- [x] Verify all year/month transitions, 29/30-day months, every leap month and especially
  2033/2034 against the reference. (2,461 gapless months; leap fixtures including 2033/2034.)
- [x] Check boundary years, boundary months, adjacent-month cells, min/max +/- 1 and invalid
  leap months.
- [x] Cover Gregorian century boundaries including 1900/2000/2100 where supported or as
  rejecting boundary tests. (Range ends 2100-02-08; 1900/2100 are outside and rejected.)
- [x] Check large positive/negative operands, Int/Long boundaries, overflow and invalid day
  counts.
- [x] UTC, UTC+08, Europe/Berlin with daylight saving time and other device zones produce the
  same engine result for the same epoch day. No milliseconds/86400000 day arithmetic. (The
  engine uses only epoch days and contains no java.time or time-zone use; JAR scan.)
- [x] Test month arithmetic with leap months and day clamping, next/previous month, and the
  beginning/end.
- [x] Identical fixtures on JVM and one executed Kotlin/Native target. For advertised iOS:
  simulator runtime and an independent consumer on macOS. Compilation alone is not a runtime
  test. (JVM 14 + linuxX64 14; iOS is not advertised.)

## Compose and regression matrix

- [ ] None/Single/Multiple/Range, disabled days and markers; preserve epoch-day identity when
  switching between all calendars.
- [x] Navigation from a regular month to a leap month to the following month, year transitions,
  bounds and today outside the range. (JVM bounds tests, boundary-window tests for both engine
  bounds and Compose single/pager regression tests added; device execution remains open.)
- [x] Saver round trip and real `StateRestorationTester`/activity recreation tests including the
  leap flag and old saved sample states. New enum entries must not reinterpret old
  ordinal-based saves. (`ChineseMonthSnapshot` decode tests and a `StateRestorationTester` test
  for the leap month with two exchanged callback instances added; compiled locally and the
  remote instrumentation of the sample suite on API 29 succeeded, but there is no separate
  Chinese device matrix.)
- [ ] Month, week, agenda, pager/list and year heatmap with 12 and 13 months.
- [ ] Phone/tablet, narrow width, landscape, multiple columns, split-screen, simulated folds and
  continuous layout changes. Report real foldable tests separately.
- [ ] LTR/RTL, Chinese/English labels, large font, TalkBack, disabled semantics and visible
  leap-month identification.
- [ ] Preserve existing touch-target rules and known narrow-viewport limitations; do not claim a
  blanket 48 dp guarantee without measurement.
- [x] Regression of all existing modules: tests, lint, sample, API guard and independent
  consumers. (211 JVM including benchmark + 14 native, lint, sample APK, API guard, JVM and
  Android consumers.)

## Risk register

| Risk | Mitigation / release gate |
|---|---|
| Incorrect or unclearly licensed data tables | Provenance manifest, legal/attribution review, deterministic generator, second review; blocker until resolved |
| Round trip works despite an incorrect mapping | Independent reference comparison over the entire promised range |
| Leap month is lost in a key/saver/navigation path | Full month identity and explicit leap-month fixtures |
| Future reference differences | Document HKO notes and data-set version; do not promise universal astronomical accuracy |
| Overflow or expensive calls from invalid input | Up-front validation, checked arithmetic, bounded search/iteration and extreme-value tests |
| Network/privacy risk | No network permission, telemetry, dynamic downloads or date logging in the new module |
| Hidden foreign calendar dependency | Inspect dependency graph, POM/Gradle metadata and artifact content; engine consumer without Compose/Android/other engines |
| Supply chain / tampered artifacts | Fixed versions, repository scopes, wrapper checksum, verified Gradle verification metadata; no dynamic release versions |
| Known vulnerable dependencies | OSV scan 2026-11-13: 101 runtime coordinates with no findings; 334 runtime/buildscript coordinates with findings only in the AGP buildscript (not shipped). Build-tooling acceptance or an AGP upgrade remains a maintainer decision; checksums are not a CVE scan |
| CI token or release manipulation | Minimal workflow permissions, no secrets in foreign PR jobs, pin actions to verified commit SHAs, separate release approval |
| API/consumer breakage | Verify the unchanged 2.3.0 baseline, new API dumps, publication and binary-compatibility tests |
| JitPack/KMP variants cannot be consumed | Isolated publication plus a remote consumer for every advertised variant; no mavenLocal/includeBuild substitute |

## Security and performance policy

Release blockers: unexplained calendar differences, broken data integrity, missing license,
broken existing API, or required builds/consumers. Applicable Critical/High security findings
must be fixed or explicitly accepted by the maintainer before release with justification, scope
and expiration date; no silent exceptions. Document Medium/Low findings with an owner and
deadline as well. Do not introduce a new scan dependency without approval.

Gradle dependency verification checks integrity/signatures but does not prevent use of known
vulnerable versions. Automatically generated trust metadata must be independently reviewed;
blind bootstrapping can pin compromised downloads. Reference:
https://docs.gradle.org/current/userguide/dependency_verification.html

Before release, collect benchmarks for conversion, month construction, the 13-month heatmap and
repeated navigation on named hardware. Record data-set size, artifact size, allocations and
runtimes, and freeze justified budgets before RC. UI recomposition must not scan the entire
reference range; no unbounded global caches or range-interior lists.

## Execution 2026-11-13

| Item | Result |
|---|---|
| JVM | `test :calendar-chinese-native:jvmTest`: 211 JVM tests (197 Android JVM + 14 KMP JVM), 0 failures, 0 skipped; includes the new benchmark-reporting test |
| Kotlin/Native | `:calendar-chinese-native:linuxX64Test`: 14 tests, 0 failures, 0 skipped |
| Compose data/state | 12 tests: month/week/state/saver/heatmap/overlay and benchmark reporting, including 12/13 months, bounds and decode validation |
| Sample | `assembleDebug`, 62 unit tests and `compileDebugAndroidTestKotlin` successful; new single/pager boundary tests compiled only |
| API/consumer | Existing dumps `0` diff; engine JVM consumer; Android BOM consumer `assembleDebug` |
| Python reference check | 7 tests: missing files/starts, year/leap status, documented conflict, verify failure status, instant equality/time zones/tolerance, NASA 1914 value and SHA-256 of the Kotlin data against the manifest |
| Remote CI | Evidence tree `1ab841e`: verification `43401491446`, publishing `43401491433`, instrumentation `43401491452`, tablet `43401491675`; fix commit `e80c00f`: verification `43412948314`, publishing `43412948284`, instrumentation `43412948316`, tablet `43412948286`; follow-up tree `952bbb6` (contains `5cf640d`): verification `43439808516`, publishing `43439808420`, instrumentation `43439808384`, tablet `43439808435` - all successful. Verified source `4dc484a`: verification `43528254651`, publishing `43528254527`, instrumentation `43528254722`, tablet `43528254471` - all successful. |
| JitPack | 2.3.0 build log HTTP 200; Android BOM consumer `BUILD SUCCESSFUL`; commit `7a51e24` with BOM, engine (metadata/JVM/linuxX64), Compose and five existing modules; direct engine JVM consumer `JVM consumer OK`, exit 0. 2.4.0 tag/BOM consumer is blocked without a tag; the commit BOM still constrains `2.4.0`. |
| Benchmark | JVM baseline on Freebuff VM, JDK 21.0.13, 8 vCPU: conversion 58.77 ns/op, month construction 16,074.47 ns/month, 13-month heatmap 355,526 ns/year, navigation 53.79 ns/step; allocations 56.01 B / 15,630.53 B / 248,328 B / 48.01 B per operation. Focused test green; physical device acceptance remains open. |
| Advisory | 101 runtime coordinates, OSV `querybatch`: 0 findings. 334 runtime/buildscript coordinates: findings only in the AGP 9.5.1 buildscript (Guava, commons-lang3, jose4j, Bouncy Castle, JDOM2); not in AAR/JAR. Maintainer acceptance/AGP upgrade remains open. |
| Emulator | API 29 x86_64 image; KVM unavailable (`ProbeKVM`). A software-accelerated AVD booted with `-accel off` at 1080x1920/420 dpi (`dev.bootcomplete=1`). The debug sample installed and rendered; the compact `CN 2.4.0` entry showed `2023 Leap 2nd Month` and details `Epoch day: 19445`, `Gregorian: 2023-03-29`, `Chinese: 2023 Leap 2nd Month, day 8`. No app `FATAL EXCEPTION`; System UI/GMS ANRs occurred under software CPU emulation. This is phone smoke evidence, not hardware, fold, TalkBack or performance evidence. |
| Gate evidence remote CI | `4ce4d3f`: verification `43479911419`, publishing `43479911387`, instrumentation `43479911196`, tablet `43479911275` - all successful. |
| Open | Chinese UI interaction on real devices/folds, TalkBack, large font, full RTL, touch-target measurement, physical benchmark acceptance, final budget approval, buildscript advisory acceptance, tag/JitPack 2.4.0 |

## Benchmark budgets (provisional, 2026-11-13)

The measurement is a JVM baseline, not target-device acceptance. Budgets deliberately have at
least a 6x margin over the measured medians and will be frozen only after measurement on the
named target hardware.

| Operation | Measurement (median) | Provisional budget |
|---|---:|---:|
| Convert there/back | 58.77 ns/op, 56.01 B/op | <= 1,000 ns/op, <= 512 B/op |
| Month construction | 16,074.47 ns/month, 15,630.53 B/month | <= 100,000 ns/month, <= 100 KB/month |
| 13-month heatmap | 355,526 ns/year, 248,328 B/year | <= 5 ms/year, <= 1.5 MB/year |
| Navigation | 53.79 ns/step, 48.01 B/step | <= 1,000 ns/step, <= 512 B/step |

The benchmark is `ChineseJvmBenchmarkTest`; it prints raw values and deliberately has no
time-based assertion. The test run is part of `./gradlew test`; the focused command is:

```bash
./gradlew :calendar-chinese-compose:testDebugUnitTest --tests '*ChineseJvmBenchmarkTest*'
```

## Manual device/accessibility matrix

Execute on at least a phone, a tablet and a foldable (book and tabletop postures), each with a
Chinese month, leap month, year heatmap and `chineseGregorianGrid`:

- [ ] Phone portrait/landscape, tablet portrait/landscape, split-screen and continuous width
  changes; no clipped days or headers.
- [ ] Foldable: content switches at the fold, book/tabletop posture, no controls under the
  hinge; synthetic `sampleLocalFold` tests are not a substitute.
- [ ] TalkBack: Chinese and English labels, ISO date in accessibility text, leap month
  `M2L`/`闰`, disabled days, selection and today; focus order in the grid.
- [ ] Large font (at least 200%) and RTL (Arabic/Hebrew): no clipping, correct reading direction,
  measurable touch targets (do not claim 48 dp across the board).
- [ ] Repeated navigation across regular months, leap months and year boundaries; no
  recomposition that scans the entire range.
- [ ] Activity recreation/rotation with Chinese month, selection and Gregorian overlay; the
  saver must not lose the leap month.

For an available local/remote device connection:

```bash
adb devices
./gradlew :calendarviewsample:connectedDebugAndroidTest
```

The software-accelerated local API 29 emulator booted after KVM was unavailable; it provides a
phone smoke check of the sample and leap-month rendering, but covers neither real foldables nor
TalkBack, large font or a complete RTL matrix.

## Evidence format

Every result: date, exact commit, toolchain/target, task/command, runner/device, result, test and
skip counts, report path or CI run, and open findings. Tests not executed remain open.
Historical green runs do not prove a later changed release tree.
