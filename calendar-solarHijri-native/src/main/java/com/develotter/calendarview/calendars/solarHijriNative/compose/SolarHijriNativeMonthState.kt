package com.develotter.calendarview.calendars.solarHijriNative.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriEngine
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriLocale
import com.develotter.calendarview.compose.CalendarMonthNavigationState
import java.time.Clock
import java.time.LocalDate
import java.util.Locale

/**
 * Observable Solar Hijri visible-month state with optional inclusive bounds.
 *
 * [SolarHijriDate] is immutable, so bounds and the visible month are exposed directly. Bounds
 * are normalized to the first day of their month. Navigation always stays inside the supported
 * years of [SolarHijriEngine] as well, so it never reaches an unrepresentable month; it stops
 * at the bounds instead of throwing, and [goTo] rejects out-of-range targets. [clock] is the
 * source of "today" and can be fixed in tests. Mutate on the UI thread.
 */
@Stable
class SolarHijriNativeMonthState(
    initialMonth: SolarHijriDate,
    minMonth: SolarHijriDate? = null,
    maxMonth: SolarHijriDate? = null,
    private val clock: Clock = Clock.systemDefaultZone(),
) : CalendarMonthNavigationState {
    /** Lower bound normalized to day 1, or null when unbounded. */
    val minMonth: SolarHijriDate? = minMonth?.withDayOfMonth(1)

    /** Upper bound normalized to day 1, or null when unbounded. */
    val maxMonth: SolarHijriDate? = maxMonth?.withDayOfMonth(1)

    private val minIndex: Int = maxOf(this.minMonth?.let { monthIndex(it) } ?: Int.MIN_VALUE, monthIndex(SolarHijriEngine.MIN_YEAR, 1))
    private val maxIndex: Int = minOf(this.maxMonth?.let { monthIndex(it) } ?: Int.MAX_VALUE, monthIndex(SolarHijriEngine.MAX_YEAR, 12))

    private var currentIndex by mutableIntStateOf(monthIndex(initialMonth))

    init {
        val min = this.minMonth
        val max = this.maxMonth
        require(min == null || max == null || min <= max) { "minMonth must not be after maxMonth" }
        require(currentIndex in minIndex..maxIndex) { "initialMonth must be within minMonth..maxMonth" }
    }

    /** Visible Solar Hijri year. */
    val year: Int
        get() = Math.floorDiv(currentIndex, 12)

    /** Visible Solar Hijri month, in `1..12`. */
    val monthNumber: Int
        get() = Math.floorMod(currentIndex, 12) + 1

    /** Visible month as its first day. */
    val month: SolarHijriDate
        get() = SolarHijriDate(year, monthNumber, 1)

    override val canGoPrevious: Boolean
        get() = currentIndex > minIndex

    override val canGoNext: Boolean
        get() = currentIndex < maxIndex

    override val canGoToday: Boolean
        get() = todayIndex()?.let { it in minIndex..maxIndex } ?: false

    override fun previous() {
        if (canGoPrevious) currentIndex -= 1
    }

    override fun next() {
        if (canGoNext) currentIndex += 1
    }

    override fun goToToday() {
        val index = todayIndex() ?: return
        if (index in minIndex..maxIndex) currentIndex = index
    }

    /** Moves to an exact in-range month; the target is normalized to its first day. */
    fun goTo(target: SolarHijriDate) {
        val index = monthIndex(target)
        require(index in minIndex..maxIndex) { "target must be within minMonth..maxMonth" }
        currentIndex = index
    }

    /** Localized month caption matching [solarHijriNativeMonthData]. */
    fun title(locale: Locale = Locale.getDefault()): String =
        SolarHijriLocale.formatMonthYear(year, monthNumber, locale)

    /** Month index of today, or null when the clock is outside the supported years. */
    private fun todayIndex(): Int? =
        LocalDate.now(clock).toEpochDay()
            .takeIf { it in SolarHijriEngine.MIN_EPOCH_DAY..SolarHijriEngine.MAX_EPOCH_DAY }
            ?.let { monthIndex(SolarHijriDate.fromEpochDay(it)) }

    private companion object {
        fun monthIndex(date: SolarHijriDate): Int = monthIndex(date.year, date.month)

        fun monthIndex(year: Int, month: Int): Int = year * 12 + (month - 1)
    }
}

/**
 * Remembers a saveable [SolarHijriNativeMonthState]. The initial month and bounds are
 * initializers, not controlled inputs: changing them during composition resets the state. The
 * saved value is the visible year and month.
 */
@Composable
fun rememberSolarHijriNativeMonthState(
    initialMonth: SolarHijriDate = SolarHijriDate.now(),
    minMonth: SolarHijriDate? = null,
    maxMonth: SolarHijriDate? = null,
    clock: Clock = Clock.systemDefaultZone(),
): SolarHijriNativeMonthState {
    val saver = remember(initialMonth.year, initialMonth.month, minMonth, maxMonth, clock) {
        listSaver<SolarHijriNativeMonthState, Int>(
            save = { listOf(it.year, it.monthNumber) },
            restore = { SolarHijriNativeMonthState(SolarHijriDate(it[0], it[1], 1), minMonth, maxMonth, clock) },
        )
    }
    return rememberSaveable(initialMonth.year, initialMonth.month, minMonth, maxMonth, saver = saver) {
        SolarHijriNativeMonthState(initialMonth, minMonth, maxMonth, clock)
    }
}
