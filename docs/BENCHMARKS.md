# Benchmarks

The `:benchmarks` module uses kotlinx-benchmark `0.5.0` on JMH `1.37` with Kotlin/JVM 17. It is a
development tool, not a published artifact. Its benchmark tasks are deliberately **not** wired
into `check`, `build` or CI, so the normal gate keeps its current runtime.

## Run

```bash
./gradlew :benchmarks:mainBenchmark
# Optional: build the generated JMH harness without running it
./gradlew :benchmarks:assembleBenchmarks
```

`mainBenchmark` is the JVM benchmark task for the `main` target. The module is configured with
three warmups, five measurement iterations and 250 ms per iteration to keep a local run short;
that is a smoke baseline, not a release budget.

## Covered paths

- Gregorian month calculation: 12 months of `GregorianYearMonth` day/epoch conversion.
- Gregorian heatmap adapter: `gregorianMonthData(...).toHeatmap(...)`.
- Hijri month calculation: 354 days through `UmmAlQuraHijriDateProvider`.
- Hijri heatmap adapter: `hijriMonthData(...).toHeatmap(...)`.

The two adapters exercise the shared `CalendarMonthData.toHeatmap` conversion, which every engine
uses for year/quarter heatmaps.

## Measured run

Environment: Linux VM container, 2 vCPU, OpenJDK 17.0.21, Gradle 9.9.1, Kotlin `2.5.0-RC`,
kotlinx-benchmark 0.5.0, JMH 1.37, 2026-11-16 (UTC 2026-11-17).

```text
Benchmark                                     Mode  Cnt   Score    Error  Units
CalendarBenchmarks.gregorianHeatmapAdapter    avgt    5   5.345 ± 17.577  us/op
CalendarBenchmarks.gregorianMonthCalculation  avgt    5   0.454 ±  0.023  us/op
CalendarBenchmarks.hijriHeatmapAdapter        avgt    5   0.786 ±  0.185  us/op
CalendarBenchmarks.hijriMonthCalculation      avgt    5  12.009 ±  5.985  us/op
```

For reference, the earlier post-`3.0.0` smoke run on an 8 vCPU Linux container (JDK 21.0.13,
Kotlin `2.4.21`, 2026-11-16) measured `0.975 ± 0.425` (`gregorianHeatmapAdapter`),
`0.450 ± 0.153` (`gregorianMonthCalculation`), `0.771 ± 0.100` (`hijriHeatmapAdapter`), and
`10.786 ± 1.816` us/op (`hijriMonthCalculation`).

The numbers are useful for regression direction only. The 2 vCPU VM container is shared and fast
allocation-sensitive operations (`gregorianHeatmapAdapter`, min `0.971` / max `11.172` us/op) show
high variance; rerun on the target device class before making a performance budget or release claim.
