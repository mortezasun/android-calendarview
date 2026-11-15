package com.develotter.calendarview.calendars.hijri.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.develotter.calendarview.calendars.hijri.HijriDateProvider
import com.develotter.calendarview.calendars.hijri.UmmAlQuraHijriDateProvider
import com.develotter.calendarview.compose.CalendarMonthNavigationState
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Observable Hijri visible-month state with optional inclusive bounds.
 *
 * Months are normalized to their first day, so the state never carries a day-of-month that
 * could overflow during navigation. Navigation stops at the bounds instead of throwing;
 * [goTo] rejects out-of-range targets. Mutate on the UI thread.
 *
 * The three-parameter constructor uses Umm al-Qura for [goToToday]. Use the provider
 * constructor when the displayed dates come from another [HijriDateProvider], so "today"
 * follows the same absolute-day method as the grid.
 */
@Stable
class HijriMonthState(
    initialMonth: HijrahDate,
    minMonth: HijrahDate? = null,
    maxMonth: HijrahDate? = null,
) : CalendarMonthNavigationState {
    private var dateProvider: HijriDateProvider = UmmAlQuraHijriDateProvider

    /** Creates a state whose today action uses [dateProvider] for the absolute-day mapping. */
    constructor(
        initialMonth: HijrahDate,
        minMonth: HijrahDate? = null,
        maxMonth: HijrahDate? = null,
        dateProvider: HijriDateProvider,
    ) : this(initialMonth, minMonth, maxMonth) {
        this.dateProvider = dateProvider
    }

    val minMonth: HijrahDate? = minMonth?.let(::firstDayOfMonth)
    val maxMonth: HijrahDate? = maxMonth?.let(::firstDayOfMonth)

    /** Currently visible month, normalized to the first day. */
    var month by mutableStateOf(firstDayOfMonth(initialMonth))
        private set

    init {
        val min = this.minMonth
        val max = this.maxMonth
        require(min == null || max == null || min.toEpochDay() <= max.toEpochDay()) {
            "minMonth must not be after maxMonth"
        }
        require(inRange(month)) { "initialMonth must be within minMonth..maxMonth" }
    }

    override val canGoPrevious: Boolean
        get() = minMonth == null || month.toEpochDay() > minMonth.toEpochDay()

    override val canGoNext: Boolean
        get() = maxMonth == null || month.toEpochDay() < maxMonth.toEpochDay()

    override val canGoToday: Boolean
        get() = inRange(todayMonth())

    override fun previous() {
        if (canGoPrevious) month = firstDayOfMonth(month.minus(1, ChronoUnit.MONTHS))
    }

    override fun next() {
        if (canGoNext) month = firstDayOfMonth(month.plus(1, ChronoUnit.MONTHS))
    }

    override fun goToToday() {
        val today = todayMonth()
        if (inRange(today)) month = today
    }

    /** Moves to an exact in-range month; the target is normalized to its first day. */
    fun goTo(target: HijrahDate) {
        val normalized = firstDayOfMonth(target)
        require(inRange(normalized)) { "target must be within minMonth..maxMonth" }
        month = normalized
    }

    /** Localized month caption matching [hijriMonthData]. */
    fun title(locale: Locale = Locale.getDefault()): String =
        month.format(DateTimeFormatter.ofPattern("MMMM y G", locale).withChronology(month.chronology))

    private fun todayMonth(): HijrahDate =
        firstDayOfMonth(dateProvider.dateForEpochDay(LocalDate.now().toEpochDay()))

    private fun inRange(value: HijrahDate): Boolean {
        val min = minMonth
        val max = maxMonth
        return (min == null || value.toEpochDay() >= min.toEpochDay()) &&
            (max == null || value.toEpochDay() <= max.toEpochDay())
    }
}

private fun firstDayOfMonth(date: HijrahDate): HijrahDate =
    date.with(ChronoField.DAY_OF_MONTH, 1)

/**
 * Remembers a saveable [HijriMonthState]. The initial month and bounds are initializers, not
 * controlled inputs: changing them during composition resets the state. The saved value is the
 * Hijri year and month; restoration normalizes to the first day again.
 */
@Composable
fun rememberHijriMonthState(
    initialMonth: HijrahDate = HijrahDate.now(),
    minMonth: HijrahDate? = null,
    maxMonth: HijrahDate? = null,
): HijriMonthState {
    val saver = remember(minMonth, maxMonth) {
        listSaver<HijriMonthState, Int>(
            save = {
                listOf(
                    it.month.get(ChronoField.YEAR),
                    it.month.get(ChronoField.MONTH_OF_YEAR),
                )
            },
            restore = {
                HijriMonthState(HijrahDate.of(it[0], it[1], 1), minMonth, maxMonth)
            },
        )
    }
    return rememberSaveable(initialMonth, minMonth, maxMonth, saver = saver) {
        HijriMonthState(initialMonth, minMonth, maxMonth)
    }
}

/**
 * Remembers a saveable [HijriMonthState] whose today action uses [dateProvider]. The saved
 * value is the Hijri year and month; restoration normalizes to the first day again.
 */
@Composable
fun rememberHijriMonthState(
    initialMonth: HijrahDate,
    minMonth: HijrahDate? = null,
    maxMonth: HijrahDate? = null,
    dateProvider: HijriDateProvider,
): HijriMonthState {
    val saver = remember(minMonth, maxMonth, dateProvider) {
        listSaver<HijriMonthState, Int>(
            save = {
                listOf(
                    it.month.get(ChronoField.YEAR),
                    it.month.get(ChronoField.MONTH_OF_YEAR),
                )
            },
            restore = {
                HijriMonthState(HijrahDate.of(it[0], it[1], 1), minMonth, maxMonth, dateProvider)
            },
        )
    }
    return rememberSaveable(initialMonth, minMonth, maxMonth, dateProvider, saver = saver) {
        HijriMonthState(initialMonth, minMonth, maxMonth, dateProvider)
    }
}
