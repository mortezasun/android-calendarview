package com.develotter.calendarview.calendars.hijri.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.develotter.calendarview.calendars.hijri.HijriDate
import com.develotter.calendarview.calendars.hijri.HijriDateProvider
import com.develotter.calendarview.calendars.hijri.HijriPlatform
import com.develotter.calendarview.calendars.hijri.UmmAlQuraHijriDateProvider
import com.develotter.calendarview.compose.CalendarMonthNavigationState
import com.develotter.calendarview.locale.CalendarLocale

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
    initialMonth: HijriDate,
    minMonth: HijriDate? = null,
    maxMonth: HijriDate? = null,
) : CalendarMonthNavigationState {
    private var dateProvider: HijriDateProvider = UmmAlQuraHijriDateProvider

    /** Creates a state whose today action uses [dateProvider] for the absolute-day mapping. */
    constructor(
        initialMonth: HijriDate,
        minMonth: HijriDate? = null,
        maxMonth: HijriDate? = null,
        dateProvider: HijriDateProvider,
    ) : this(initialMonth, minMonth, maxMonth) {
        this.dateProvider = dateProvider
    }

    val minMonth: HijriDate? = minMonth?.let(::firstDayOfMonth)
    val maxMonth: HijriDate? = maxMonth?.let(::firstDayOfMonth)

    /** Currently visible month, normalized to the first day. */
    var month by mutableStateOf(firstDayOfMonth(initialMonth))
        private set

    init {
        val min = this.minMonth
        val max = this.maxMonth
        require(min == null || max == null || min <= max) {
            "minMonth must not be after maxMonth"
        }
        require(inRange(month)) { "initialMonth must be within minMonth..maxMonth" }
    }

    override val canGoPrevious: Boolean
        get() = minMonth == null || month > minMonth

    override val canGoNext: Boolean
        get() = maxMonth == null || month < maxMonth

    override val canGoToday: Boolean
        get() = inRange(todayMonth())

    override fun previous() {
        if (canGoPrevious) month = month.previousMonth()
    }

    override fun next() {
        if (canGoNext) month = month.nextMonth()
    }

    override fun goToToday() {
        val today = todayMonth()
        if (inRange(today)) month = today
    }

    /** Moves to an exact in-range month; the target is normalized to its first day. */
    fun goTo(target: HijriDate) {
        val normalized = firstDayOfMonth(target)
        require(inRange(normalized)) { "target must be within minMonth..maxMonth" }
        month = normalized
    }

    /** Localized month caption matching [hijriMonthData]. */
    fun title(locale: CalendarLocale? = null): String = hijriMonthTitle(month, locale)

    private fun todayMonth(): HijriDate =
        firstDayOfMonth(dateProvider.dateForEpochDay(HijriPlatform.currentEpochDay()))

    private fun inRange(value: HijriDate): Boolean {
        val min = minMonth
        val max = maxMonth
        return (min == null || value >= min) && (max == null || value <= max)
    }
}

private fun firstDayOfMonth(date: HijriDate): HijriDate = date.copy(dayOfMonth = 1)

private fun HijriDate.previousMonth(): HijriDate =
    if (month == 1) copy(year = year - 1, month = 12) else copy(month = month - 1)

private fun HijriDate.nextMonth(): HijriDate =
    if (month == 12) copy(year = year + 1, month = 1) else copy(month = month + 1)

/**
 * Remembers a saveable [HijriMonthState]. The initial month and bounds are initializers, not
 * controlled inputs: changing them during composition resets the state. The saved value is the
 * Hijri year and month; restoration normalizes to the first day again.
 */
@Composable
fun rememberHijriMonthState(
    initialMonth: HijriDate = UmmAlQuraHijriDateProvider.dateForEpochDay(
        HijriPlatform.currentEpochDay(),
    ),
    minMonth: HijriDate? = null,
    maxMonth: HijriDate? = null,
): HijriMonthState {
    val saver = remember(minMonth, maxMonth) {
        listSaver<HijriMonthState, Int>(
            save = { listOf(it.month.year, it.month.month) },
            restore = { HijriMonthState(HijriDate(it[0], it[1], 1), minMonth, maxMonth) },
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
    initialMonth: HijriDate,
    minMonth: HijriDate? = null,
    maxMonth: HijriDate? = null,
    dateProvider: HijriDateProvider,
): HijriMonthState {
    val saver = remember(minMonth, maxMonth, dateProvider) {
        listSaver<HijriMonthState, Int>(
            save = { listOf(it.month.year, it.month.month) },
            restore = {
                HijriMonthState(HijriDate(it[0], it[1], 1), minMonth, maxMonth, dateProvider)
            },
        )
    }
    return rememberSaveable(initialMonth, minMonth, maxMonth, dateProvider, saver = saver) {
        HijriMonthState(initialMonth, minMonth, maxMonth, dateProvider)
    }
}
