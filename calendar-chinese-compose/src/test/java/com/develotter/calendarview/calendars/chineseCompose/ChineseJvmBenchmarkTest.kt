package com.develotter.calendarview.calendars.chineseCompose

import com.develotter.calendarview.calendars.chineseNative.ChineseCalendarEngine
import com.develotter.calendarview.calendars.chineseNative.ChineseDate
import com.develotter.calendarview.calendars.chineseNative.ChineseMonth
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import org.junit.Test
import java.lang.management.ManagementFactory
import java.time.DayOfWeek
import java.util.Locale

/**
 * Reports a reproducible JVM performance baseline for the four release-plan operations.
 *
 * This is a reporting harness, not a hardware acceptance test: it deliberately has no timing
 * assertion, because CI and developer machines have different CPUs and JIT behaviour. The
 * printed `BENCHMARK` lines are the evidence recorded in the release documentation. Timings are
 * medians of five measured samples after two warm-up samples; allocation counts use the
 * HotSpot thread allocation counter when available and are reported as `-1` otherwise.
 */
class ChineseJvmBenchmarkTest {
    private val locale = Locale.ENGLISH
    private val monday = DayOfWeek.MONDAY

    @Test
    fun reportJvmPerformanceBaseline() {
        println(
            "BENCHMARK environment os=${System.getProperty("os.name")} " +
                "arch=${System.getProperty("os.arch")} " +
                "jdk=${System.getProperty("java.version")} " +
                "vm=${System.getProperty("java.vm.name")} " +
                "cpus=${Runtime.getRuntime().availableProcessors()} " +
                "maxHeapBytes=${Runtime.getRuntime().maxMemory()}",
        )

        val days = ChineseCalendarEngine.MIN_EPOCH_DAY..ChineseCalendarEngine.MAX_EPOCH_DAY
        val months = (ChineseCalendarEngine.MIN_YEAR..ChineseCalendarEngine.MAX_YEAR)
            .flatMap(ChineseCalendarEngine::monthsInYear)
        val heatmapYear = 2023
        check(ChineseCalendarEngine.monthsInYear(heatmapYear).size == 13)

        report(
            name = "conversion_roundtrip",
            operations = days.count().toLong(),
            warmups = 2,
            samples = 5,
        ) {
            for (epochDay in days) {
                val date = ChineseDate.fromEpochDay(epochDay)
                check(date.toEpochDay() == epochDay)
            }
        }

        report(
            name = "month_data",
            operations = months.size.toLong(),
            warmups = 2,
            samples = 5,
        ) {
            for (month in months) {
                chineseMonthData(
                    month = month,
                    style = ChineseTextStyle.English,
                    locale = locale,
                    firstDayOfWeek = monday,
                    todayEpochDay = ChineseCalendarEngine.MIN_EPOCH_DAY,
                )
            }
        }

        report(
            name = "heatmap_13_months",
            operations = 1L,
            warmups = 2,
            samples = 5,
        ) {
            chineseYearHeatmap(
                year = heatmapYear,
                locale = locale,
                firstDayOfWeek = monday,
                todayEpochDay = ChineseCalendarEngine.MIN_EPOCH_DAY,
            )
        }

        val navigation = ChineseMonthState(
            initialMonth = ChineseMonth.of(2000, 1),
            minMonth = ChineseCalendarEngine.firstMonthOfYear(ChineseCalendarEngine.MIN_YEAR),
            maxMonth = ChineseCalendarEngine.lastMonthOfYear(ChineseCalendarEngine.MAX_YEAR),
            today = { ChineseCalendarEngine.MIN_EPOCH_DAY },
        )
        val navigationSteps = 100_000L
        report(
            name = "navigation",
            operations = navigationSteps,
            warmups = 2,
            samples = 5,
        ) {
            repeat((navigationSteps / 2).toInt()) {
                navigation.next()
                navigation.previous()
            }
        }

        println(
            "BENCHMARK dataset days=${days.count()} months=${months.size} " +
                "heatmapYear=$heatmapYear heatmapMonths=13 navigationSteps=$navigationSteps",
        )
    }

    private fun report(
        name: String,
        operations: Long,
        warmups: Int,
        samples: Int,
        block: () -> Unit,
    ) {
        repeat(warmups) { block() }
        val timings = mutableListOf<Long>()
        val allocations = mutableListOf<Long>()
        repeat(samples) {
            val allocationBefore = allocatedBytes()
            val start = System.nanoTime()
            block()
            val elapsed = System.nanoTime() - start
            val allocationAfter = allocatedBytes()
            timings += elapsed
            allocations += if (allocationBefore >= 0 && allocationAfter >= 0) {
                allocationAfter - allocationBefore
            } else {
                -1L
            }
        }
        val medianNanos = timings.sorted()[timings.size / 2]
        val medianAllocation = allocations.sorted()[allocations.size / 2]
        println(
            "BENCHMARK $name operations=$operations " +
                "nsPerOperation=${medianNanos.toDouble() / operations} " +
                "medianTotalNs=$medianNanos allocatedBytesPerOperation=" +
                (if (medianAllocation < 0) "-1" else (medianAllocation.toDouble() / operations).toString()) +
                " samplesNs=${timings.joinToString(",")}",
        )
    }

    private fun allocatedBytes(): Long {
        val bean = ManagementFactory.getThreadMXBean()
        return if (bean is com.sun.management.ThreadMXBean && bean.isThreadAllocatedMemorySupported) {
            bean.getCurrentThreadAllocatedBytes()
        } else {
            -1L
        }
    }
}
