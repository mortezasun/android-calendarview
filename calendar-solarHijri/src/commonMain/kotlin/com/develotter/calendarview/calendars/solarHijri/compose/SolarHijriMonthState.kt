package com.develotter.calendarview.calendars.solarHijri.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.develotter.calendarview.compose.CalendarMonthNavigationState
import ir.huri.jcal.JalaliCalendar
import java.time.LocalDate
import java.util.Locale

/**
 * Observable Solar Hijri visible-month state with optional inclusive bounds.
 *
 * Internally stores the year and month only, because [JalaliCalendar] is mutable and has no
 * value equality. [month] returns a fresh first-day snapshot, so callers cannot mutate the
 * state through it. Navigation stops at the bounds instead of throwing; [goTo] rejects
 * out-of-range targets. Mutate on the UI thread.
 */
@Stable
class SolarHijriMonthState(
    initialMonth: JalaliCalendar,
    minMonth: JalaliCalendar? = null,
    maxMonth: JalaliCalendar? = null,
) : CalendarMonthNavigationState {
    private val minBound: JalaliCalendar? = minMonth?.let { JalaliCalendar(it.year, it.month, 1) }
    private val maxBound: JalaliCalendar? = maxMonth?.let { JalaliCalendar(it.year, it.month, 1) }

    /** Lower bound as a fresh snapshot; mutating it does not change this state. */
    val minMonth: JalaliCalendar?
        get() = minBound?.let { JalaliCalendar(it.year, it.month, 1) }

    /** Upper bound as a fresh snapshot; mutating it does not change this state. */
    val maxMonth: JalaliCalendar?
        get() = maxBound?.let { JalaliCalendar(it.year, it.month, 1) }

    private var currentYear by mutableIntStateOf(initialMonth.year)
    private var currentMonth by mutableIntStateOf(initialMonth.month)

    init {
        val min = minBound
        val max = maxBound
        require(min == null || max == null || compareMonths(min.year, min.month, max.year, max.month) <= 0) {
            "minMonth must not be after maxMonth"
        }
        require(inRange(currentYear, currentMonth)) { "initialMonth must be within minMonth..maxMonth" }
    }

    /** Visible Solar Hijri year. */
    val year: Int
        get() = currentYear

    /** Visible Solar Hijri month, in `1..12`. */
    val monthNumber: Int
        get() = currentMonth

    /** Visible month as a fresh first-day snapshot. */
    val month: JalaliCalendar
        get() = JalaliCalendar(currentYear, currentMonth, 1)

    override val canGoPrevious: Boolean
        get() = minBound?.let { compareMonths(currentYear, currentMonth, it.year, it.month) > 0 } ?: true

    override val canGoNext: Boolean
        get() = maxBound?.let { compareMonths(currentYear, currentMonth, it.year, it.month) < 0 } ?: true

    override val canGoToday: Boolean
        get() = LocalDate.now().let { today ->
            val jalali = JalaliCalendar(today)
            inRange(jalali.year, jalali.month)
        }

    override fun previous() {
        if (!canGoPrevious) return
        val target = shifted(-1)
        currentYear = target.first
        currentMonth = target.second
    }

    override fun next() {
        if (!canGoNext) return
        val target = shifted(1)
        currentYear = target.first
        currentMonth = target.second
    }

    override fun goToToday() {
        val jalali = JalaliCalendar(LocalDate.now())
        if (inRange(jalali.year, jalali.month)) {
            currentYear = jalali.year
            currentMonth = jalali.month
        }
    }

    /** Moves to an exact in-range month; the target is normalized to its first day. */
    fun goTo(target: JalaliCalendar) {
        require(inRange(target.year, target.month)) { "target must be within minMonth..maxMonth" }
        currentYear = target.year
        currentMonth = target.month
    }

    /** Localized month caption matching [solarHijriMonthData]. */
    fun title(locale: Locale = Locale.getDefault()): String =
        solarHijriMonthTitle(JalaliCalendar(currentYear, currentMonth, 1), locale)

    private fun inRange(year: Int, month: Int): Boolean {
        val min = minBound
        val max = maxBound
        return (min == null || compareMonths(year, month, min.year, min.month) >= 0) &&
            (max == null || compareMonths(year, month, max.year, max.month) <= 0)
    }

    private fun shifted(offset: Int): Pair<Int, Int> {
        val index = currentYear.toLong() * 12 + (currentMonth - 1) + offset
        val year = Math.toIntExact(Math.floorDiv(index, 12L))
        val month = Math.floorMod(index, 12L).toInt() + 1
        return year to month
    }

    private fun compareMonths(year: Int, month: Int, otherYear: Int, otherMonth: Int): Int =
        if (year != otherYear) year.compareTo(otherYear) else month.compareTo(otherMonth)
}

/**
 * Remembers a saveable [SolarHijriMonthState]. The initial month and bounds are initializers,
 * not controlled inputs: changing their year or month during composition resets the state.
 * The saved value is the visible year and month, not the mutable [JalaliCalendar] object.
 */
@Composable
fun rememberSolarHijriMonthState(
    initialMonth: JalaliCalendar = JalaliCalendar(LocalDate.now()),
    minMonth: JalaliCalendar? = null,
    maxMonth: JalaliCalendar? = null,
): SolarHijriMonthState {
    val initialYear = initialMonth.year
    val initialMonthNumber = initialMonth.month
    val minYear = minMonth?.year
    val minMonthNumber = minMonth?.month
    val maxYear = maxMonth?.year
    val maxMonthNumber = maxMonth?.month
    val saver = remember(initialYear, initialMonthNumber, minYear, minMonthNumber, maxYear, maxMonthNumber) {
        listSaver<SolarHijriMonthState, Int>(
            save = { listOf(it.year, it.monthNumber) },
            restore = {
                SolarHijriMonthState(JalaliCalendar(it[0], it[1], 1), minMonth, maxMonth)
            },
        )
    }
    return rememberSaveable(
        initialYear,
        initialMonthNumber,
        minYear,
        minMonthNumber,
        maxYear,
        maxMonthNumber,
        saver = saver,
    ) {
        SolarHijriMonthState(initialMonth, minMonth, maxMonth)
    }
}
