package com.develotter.calendarview.calendars.solarHijri.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.compose.SolarHijriNativeMonthState
import com.develotter.calendarview.compose.CalendarMonthNavigationState

/**
 * Observable Solar Hijri visible-month state with optional inclusive bounds.
 *
 * The immutable [SolarHijriDate] is the visible month, so bounds and the current month cannot
 * be mutated from outside. Bounds are normalized to the first day of their month. Navigation
 * stays inside the engine's supported years, stops at the bounds instead of throwing, and
 * [goTo] rejects out-of-range targets. [currentEpochDay] is the source of "today" and can be
 * fixed in tests. Mutate on the UI thread.
 */
@Stable
class SolarHijriMonthState(
    initialMonth: SolarHijriDate,
    minMonth: SolarHijriDate? = null,
    maxMonth: SolarHijriDate? = null,
    currentEpochDay: () -> Long = { SolarHijriDate.now().toEpochDay() },
) : CalendarMonthNavigationState {
    private val delegate = SolarHijriNativeMonthState(initialMonth, minMonth, maxMonth, currentEpochDay)

    /** Lower bound normalized to day 1, or null when unbounded. */
    val minMonth: SolarHijriDate?
        get() = delegate.minMonth

    /** Upper bound normalized to day 1, or null when unbounded. */
    val maxMonth: SolarHijriDate?
        get() = delegate.maxMonth

    /** Visible Solar Hijri year. */
    val year: Int
        get() = delegate.year

    /** Visible Solar Hijri month, in `1..12`. */
    val monthNumber: Int
        get() = delegate.monthNumber

    /** Visible month as its first day. */
    val month: SolarHijriDate
        get() = delegate.month

    override val canGoPrevious: Boolean
        get() = delegate.canGoPrevious

    override val canGoNext: Boolean
        get() = delegate.canGoNext

    override val canGoToday: Boolean
        get() = delegate.canGoToday

    override fun previous() = delegate.previous()

    override fun next() = delegate.next()

    override fun goToToday() = delegate.goToToday()

    /** Moves to an exact in-range month; the target is normalized to its first day. */
    fun goTo(target: SolarHijriDate) = delegate.goTo(target)

    /** Localized month caption matching [solarHijriMonthData]. */
    fun title(): String = delegate.title()
}

/**
 * Remembers a saveable [SolarHijriMonthState]. The initial month and bounds are initializers,
 * not controlled inputs: changing their year or month during composition resets the state.
 * The saved value is the visible year and month.
 */
@Composable
fun rememberSolarHijriMonthState(
    initialMonth: SolarHijriDate = SolarHijriDate.now(),
    minMonth: SolarHijriDate? = null,
    maxMonth: SolarHijriDate? = null,
    currentEpochDay: () -> Long = { SolarHijriDate.now().toEpochDay() },
): SolarHijriMonthState {
    val latestCurrentEpochDay by rememberUpdatedState(currentEpochDay)
    val saver = remember(initialMonth.year, initialMonth.month, minMonth, maxMonth) {
        listSaver<SolarHijriMonthState, Int>(
            save = { listOf(it.year, it.monthNumber) },
            restore = {
                SolarHijriMonthState(
                    SolarHijriDate(it[0], it[1], 1),
                    minMonth,
                    maxMonth,
                ) { latestCurrentEpochDay() }
            },
        )
    }
    return rememberSaveable(initialMonth.year, initialMonth.month, minMonth, maxMonth, saver = saver) {
        SolarHijriMonthState(initialMonth, minMonth, maxMonth) { latestCurrentEpochDay() }
    }
}
