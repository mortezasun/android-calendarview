package com.develotter.calendarview.calendars.chineseCompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.develotter.calendarview.calendars.chineseNative.ChineseCalendarEngine
import com.develotter.calendarview.calendars.chineseNative.ChineseDate
import com.develotter.calendarview.calendars.chineseNative.ChineseFormat
import com.develotter.calendarview.calendars.chineseNative.ChineseMonth
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.compose.CalendarMonthNavigationState
import java.time.LocalDate

/**
 * Observable Chinese visible-month state with optional inclusive bounds.
 *
 * The caller owns one instance and renders [month]. Navigation stops at the bounds (or at the
 * documented engine range) instead of throwing; [goTo] rejects out-of-range targets so
 * configuration errors surface early. Mutate on the UI thread.
 *
 * [today] is injected for deterministic tests and for hosts whose "today" is not the device
 * date. The saved value is the full month identity (year, month number and leap flag), so a leap
 * month cannot be restored as its regular namesake.
 */
@Stable
class ChineseMonthState(
    initialMonth: ChineseMonth,
    val minMonth: ChineseMonth = ChineseCalendarEngine.firstMonthOfYear(ChineseCalendarEngine.MIN_YEAR),
    val maxMonth: ChineseMonth = ChineseCalendarEngine.lastMonthOfYear(ChineseCalendarEngine.MAX_YEAR),
    private val today: () -> Long = { LocalDate.now().toEpochDay() },
) : CalendarMonthNavigationState {
    init {
        require(minMonth <= maxMonth) { "minMonth must not be after maxMonth" }
        require(initialMonth in minMonth..maxMonth) { "initialMonth must be within minMonth..maxMonth" }
    }

    /** Currently visible month. */
    var month by mutableStateOf(initialMonth)
        private set

    override val canGoPrevious: Boolean
        get() = month > minMonth

    override val canGoNext: Boolean
        get() = month < maxMonth

    override val canGoToday: Boolean
        get() {
            val epochDay = today()
            return ChineseCalendarEngine.isSupported(epochDay) &&
                ChineseMonth.fromEpochDay(epochDay) in minMonth..maxMonth
        }

    override fun previous() {
        if (canGoPrevious) month = ChineseCalendarEngine.previousMonth(month)
    }

    override fun next() {
        if (canGoNext) month = ChineseCalendarEngine.nextMonth(month)
    }

    override fun goToToday() {
        val epochDay = today()
        if (ChineseCalendarEngine.isSupported(epochDay) &&
            ChineseMonth.fromEpochDay(epochDay) in minMonth..maxMonth
        ) {
            month = ChineseMonth.fromEpochDay(epochDay)
        }
    }

    /** Moves to an exact in-range month; out-of-range targets fail instead of clamping. */
    fun goTo(target: ChineseMonth) {
        require(target in minMonth..maxMonth) { "target must be within minMonth..maxMonth" }
        month = target
    }

    /** Localized month caption including the leap-month marker. */
    fun title(style: ChineseTextStyle = ChineseTextStyle.English): String =
        ChineseFormat.monthTitle(month, style)
}

/** Encodes the complete month identity for saved-state round-trips. */
internal object ChineseMonthSnapshot {
    fun encode(month: ChineseMonth): List<Int> =
        listOf(month.year, month.monthNumber, if (month.isLeapMonth) 1 else 0)

    /** Returns null for malformed values or a month that is not valid in the current data set. */
    fun decode(values: List<Int>): ChineseMonth? {
        if (values.size != 3) return null
        if (values[2] != 0 && values[2] != 1) return null
        return runCatching { ChineseMonth.of(values[0], values[1], values[2] == 1) }.getOrNull()
    }
}

/**
 * Resolves the default visible month inside the engine's supported epoch-day range.
 * A host clock outside the published data set must not crash composition; the nearest
 * supported month is shown instead.
 */
internal fun defaultChineseMonth(todayEpochDay: Long): ChineseMonth =
    ChineseMonth.fromEpochDay(
        todayEpochDay.coerceIn(
            ChineseCalendarEngine.MIN_EPOCH_DAY,
            ChineseCalendarEngine.MAX_EPOCH_DAY,
        ),
    )

/**
 * Remembers a saveable [ChineseMonthState]. Initial month and bounds are initializers, not
 * controlled inputs: changing them during composition resets the state. Restoration validates
 * the saved month against the current data set and bounds and falls back to the initializer when
 * a saved value is no longer valid.
 */
@Composable
fun rememberChineseMonthState(
    initialMonth: ChineseMonth = defaultChineseMonth(LocalDate.now().toEpochDay()),
    minMonth: ChineseMonth = ChineseCalendarEngine.firstMonthOfYear(ChineseCalendarEngine.MIN_YEAR),
    maxMonth: ChineseMonth = ChineseCalendarEngine.lastMonthOfYear(ChineseCalendarEngine.MAX_YEAR),
    today: () -> Long = { LocalDate.now().toEpochDay() },
): ChineseMonthState {
    // The callback is an input, not an initializer: a recomposition may replace it while the
    // visible month and the restored state must stay untouched. The delegated read keeps the
    // provider current inside the saver too; rememberUpdatedState itself is composition-stable.
    val currentToday by rememberUpdatedState(today)
    val saver = remember(minMonth, maxMonth) {
        listSaver<ChineseMonthState, Int>(
            save = { state -> ChineseMonthSnapshot.encode(state.month) },
            restore = { values ->
                val restored = ChineseMonthSnapshot.decode(values)
                if (restored == null || restored !in minMonth..maxMonth) {
                    null
                } else {
                    ChineseMonthState(restored, minMonth, maxMonth) { currentToday() }
                }
            },
        )
    }
    return rememberSaveable(initialMonth, minMonth, maxMonth, saver = saver) {
        ChineseMonthState(initialMonth, minMonth, maxMonth) { currentToday() }
    }
}
