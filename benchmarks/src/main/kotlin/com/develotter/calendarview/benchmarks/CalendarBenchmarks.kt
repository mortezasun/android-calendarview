package com.develotter.calendarview.benchmarks

import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.calendars.gregorian.compose.gregorianMonthData
import com.develotter.calendarview.calendars.hijri.HijriDate
import com.develotter.calendarview.calendars.hijri.UmmAlQuraHijriDateProvider
import com.develotter.calendarview.calendars.hijri.compose.hijriMonthData
import com.develotter.calendarview.compose.toHeatmap
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State
import java.util.concurrent.TimeUnit

/**
 * Core calendar paths that are called for every visible month.
 *
 * The fixture data is built once per benchmark state; the measured methods only exercise the
 * calculation or adapter path. They are not part of the normal check/build gate.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
open class CalendarBenchmarks {
    private val gregorianMonth = GregorianYearMonth(year = 2026, month = 11)
    private val gregorianData = gregorianMonthData(
        month = gregorianMonth,
        firstDayOfWeek = 1,
        todayEpochDay = 0L,
    )
    private val hijriData = hijriMonthData(
        month = HijriDate(year = 1448, month = 5, dayOfMonth = 1),
        firstDayOfWeek = 1,
        todayEpochDay = 0L,
    )

    @Benchmark
    fun gregorianMonthCalculation(): Long {
        var epochDaySum = 0L
        for (monthNumber in 1..12) {
            val month = GregorianYearMonth(year = 2026, month = monthNumber)
            for (day in 1..month.lengthOfMonth()) {
                epochDaySum += month.atDay(day)
            }
        }
        return epochDaySum
    }

    @Benchmark
    fun gregorianHeatmapAdapter(): Int =
        gregorianData.toHeatmap { epochDay -> (epochDay % 30).toFloat() / 30f }.weeks.size

    @Benchmark
    fun hijriMonthCalculation(): Long {
        val provider = UmmAlQuraHijriDateProvider
        val firstEpochDay = provider.epochDayFor(HijriDate(year = 1448, month = 1, dayOfMonth = 1))
        var daySum = 0L
        repeat(354) { offset ->
            daySum += provider.dateForEpochDay(firstEpochDay + offset).dayOfMonth
        }
        return daySum
    }

    @Benchmark
    fun hijriHeatmapAdapter(): Int =
        hijriData.toHeatmap { epochDay -> (epochDay % 30).toFloat() / 30f }.weeks.size
}
