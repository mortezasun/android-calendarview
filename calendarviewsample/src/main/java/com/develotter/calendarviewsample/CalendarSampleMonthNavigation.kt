package com.develotter.calendarviewsample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.develotter.calendarview.calendars.chineseNative.ChineseCalendarEngine
import com.develotter.calendarview.calendars.chineseNative.ChineseMonth
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.calendars.chineseCompose.rememberChineseMonthState
import com.develotter.calendarview.calendars.gregorian.compose.rememberGregorianMonthState
import com.develotter.calendarview.calendars.hijri.HijriDateProvider
import com.develotter.calendarview.calendars.hijri.UmmAlQuraHijriDateProvider
import com.develotter.calendarview.calendars.hijri.compose.rememberHijriMonthState
import com.develotter.calendarview.calendars.solarHijri.compose.rememberSolarHijriMonthState
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriEpochDay
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.compose.rememberSolarHijriNativeMonthState
import com.develotter.calendarview.compose.CalendarMonthNavigationState
import ir.huri.jcal.JalaliCalendar
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoField
import java.util.Locale

/** Title and navigation state exposed by the engine-specific saveable month state. */
internal data class SampleMonthNavigation(
    val title: String,
    val state: CalendarMonthNavigationState,
)

/**
 * Bridges the controlled sample anchor to the library's engine month states. Scrolling the
 * viewport updates the state through [anchor]; header navigation reports the normalized first
 * day back to the sample, which keeps the grid, pager and dialog in sync.
 */
@Composable
internal fun rememberSampleMonthNavigation(
    calendar: SampleCalendar,
    anchor: Long,
    locale: Locale,
    hijriDateProvider: HijriDateProvider = UmmAlQuraHijriDateProvider,
    chineseStyle: ChineseTextStyle = ChineseTextStyle.English,
    chineseGregorianGrid: Boolean = false,
    onMonthChanged: (Long) -> Unit,
): SampleMonthNavigation {
    val date = LocalDate.ofEpochDay(anchor)
    val onChanged by rememberUpdatedState(onMonthChanged)
    return when (calendar) {
        SampleCalendar.Gregorian -> {
            // Keep the state initializer stable; the anchor effect below is the controlled input.
            val initialMonth = remember { YearMonth.from(date) }
            val state = rememberGregorianMonthState(initialMonth)
            LaunchedEffect(state.month) {
                val epochDay = state.month.atDay(1).toEpochDay()
                if (epochDay != anchor) onChanged(epochDay)
            }
            LaunchedEffect(anchor) {
                val target = YearMonth.from(LocalDate.ofEpochDay(anchor))
                if (state.month != target) state.goTo(target)
            }
            SampleMonthNavigation(title = state.title(locale), state = state)
        }
        SampleCalendar.Hijri -> {
            val initialMonth = remember(hijriDateProvider) {
                hijriDateProvider.dateForEpochDay(anchor)
            }
            val state = rememberHijriMonthState(
                initialMonth = initialMonth,
                dateProvider = hijriDateProvider,
            )
            LaunchedEffect(state.month) {
                val epochDay = hijriDateProvider.epochDayFor(state.month)
                if (epochDay != anchor) onChanged(epochDay)
            }
            LaunchedEffect(anchor) {
                val target = hijriDateProvider.dateForEpochDay(anchor)
                    .with(ChronoField.DAY_OF_MONTH, 1)
                if (state.month != target) state.goTo(target)
            }
            SampleMonthNavigation(title = state.title(locale), state = state)
        }
        SampleCalendar.SolarHijri -> {
            val initialMonth = remember { JalaliCalendar(date) }
            val state = rememberSolarHijriMonthState(initialMonth)
            LaunchedEffect(state.year, state.monthNumber) {
                val epochDay = solarHijriEpochDay(state.month)
                if (epochDay != anchor) onChanged(epochDay)
            }
            LaunchedEffect(anchor) {
                val target = JalaliCalendar(LocalDate.ofEpochDay(anchor))
                if (state.year != target.year || state.monthNumber != target.month) {
                    state.goTo(target)
                }
            }
            SampleMonthNavigation(title = state.title(locale), state = state)
        }
        SampleCalendar.Chinese -> if (chineseGregorianGrid) {
            // The overlay is a Gregorian month grid, including its header, previous/next and
            // pager identities. Selection remains the shared epoch-day selection.
            val initialMonth = remember { YearMonth.from(date) }
            val state = rememberGregorianMonthState(initialMonth)
            LaunchedEffect(state.month) {
                val epochDay = state.month.atDay(1).toEpochDay()
                if (epochDay != anchor) onChanged(epochDay)
            }
            LaunchedEffect(anchor) {
                val target = YearMonth.from(LocalDate.ofEpochDay(anchor))
                if (state.month != target) state.goTo(target)
            }
            SampleMonthNavigation(title = state.title(locale), state = state)
        } else {
            // A saved or externally supplied sample anchor may predate the Chinese range.
            // Clamp only this header's input and report the normalized month back; never call
            // ChineseMonth.fromEpochDay with an unsupported day.
            val safeAnchor = anchor.coerceIn(
                ChineseCalendarEngine.MIN_EPOCH_DAY,
                ChineseCalendarEngine.MAX_EPOCH_DAY,
            )
            val initialMonth = remember { ChineseMonth.fromEpochDay(safeAnchor) }
            val state = rememberChineseMonthState(initialMonth)
            LaunchedEffect(state.month) {
                val epochDay = state.month.startEpochDay()
                if (epochDay != anchor) onChanged(epochDay)
            }
            LaunchedEffect(anchor) {
                val epochDay = anchor.coerceIn(
                    ChineseCalendarEngine.MIN_EPOCH_DAY,
                    ChineseCalendarEngine.MAX_EPOCH_DAY,
                )
                val target = ChineseMonth.fromEpochDay(epochDay)
                if (state.month != target) state.goTo(target)
            }
            SampleMonthNavigation(title = state.title(chineseStyle), state = state)
        }
        SampleCalendar.SolarHijriNative -> {
            val initialMonth = remember { SolarHijriDate.fromLocalDate(date) }
            val state = rememberSolarHijriNativeMonthState(initialMonth)
            LaunchedEffect(state.year, state.monthNumber) {
                val epochDay = state.month.toEpochDay()
                if (epochDay != anchor) onChanged(epochDay)
            }
            LaunchedEffect(anchor) {
                val target = SolarHijriDate.fromEpochDay(anchor).withDayOfMonth(1)
                if (state.month != target) state.goTo(target)
            }
            SampleMonthNavigation(title = state.title(locale), state = state)
        }
    }
}
