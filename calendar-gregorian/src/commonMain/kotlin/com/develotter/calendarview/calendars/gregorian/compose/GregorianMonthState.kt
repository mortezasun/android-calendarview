package com.develotter.calendarview.calendars.gregorian.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.develotter.calendarview.calendars.gregorian.GregorianPlatformFormatter
import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.compose.CalendarMonthNavigationState
import com.develotter.calendarview.locale.CalendarLocale

/**
 * Observable Gregorian visible-month state with optional inclusive bounds.
 *
 * The caller owns one instance and renders the month through [GregorianCalendarMonth] using
 * [month]. Navigation stops at the bounds instead of throwing; [goTo] rejects out-of-range
 * targets so configuration errors surface early. Mutate on the UI thread.
 */
@Stable
class GregorianMonthState(
    initialMonth: GregorianYearMonth,
    val minMonth: GregorianYearMonth? = null,
    val maxMonth: GregorianYearMonth? = null,
) : CalendarMonthNavigationState {
    init {
        require(minMonth == null || maxMonth == null || minMonth <= maxMonth) {
            "minMonth must not be after maxMonth"
        }
        require(inRange(initialMonth)) { "initialMonth must be within minMonth..maxMonth" }
    }

    /** Currently visible month. */
    var month by mutableStateOf(initialMonth)
        private set

    override val canGoPrevious: Boolean
        get() = minMonth == null || month > minMonth

    override val canGoNext: Boolean
        get() = maxMonth == null || month < maxMonth

    override val canGoToday: Boolean
        get() = inRange(GregorianYearMonth.now())

    override fun previous() {
        if (canGoPrevious) month = month.minusMonths(1)
    }

    override fun next() {
        if (canGoNext) month = month.plusMonths(1)
    }

    override fun goToToday() {
        val today = GregorianYearMonth.now()
        if (inRange(today)) month = today
    }

    /** Moves to an exact in-range month; out-of-range targets fail instead of clamping. */
    fun goTo(target: GregorianYearMonth) {
        require(inRange(target)) { "target must be within minMonth..maxMonth" }
        month = target
    }

    /** Localized month caption matching [gregorianMonthData]; `null` keeps the platform locale. */
    fun title(locale: CalendarLocale? = null): String = gregorianMonthTitle(month, locale)

    private fun inRange(value: GregorianYearMonth): Boolean =
        (minMonth == null || value >= minMonth) && (maxMonth == null || value <= maxMonth)
}

/**
 * Remembers a saveable [GregorianMonthState]. The initial month and bounds are initializers,
 * not controlled inputs: changing them during composition resets the state. The saved value is
 * the visible year and month, restored through the same bounds.
 */
@Composable
fun rememberGregorianMonthState(
    initialMonth: GregorianYearMonth = GregorianYearMonth.now(),
    minMonth: GregorianYearMonth? = null,
    maxMonth: GregorianYearMonth? = null,
): GregorianMonthState {
    val saver = remember(minMonth, maxMonth) {
        listSaver<GregorianMonthState, Int>(
            save = { listOf(it.month.year, it.month.month) },
            restore = { GregorianMonthState(GregorianYearMonth(it[0], it[1]), minMonth, maxMonth) },
        )
    }
    return rememberSaveable(initialMonth, minMonth, maxMonth, saver = saver) {
        GregorianMonthState(initialMonth, minMonth, maxMonth)
    }
}
