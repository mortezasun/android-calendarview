package com.develotter.calendarviewsample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.develotter.calendarview.calendars.gregorian.compose.rememberGregorianMonthState
import com.develotter.calendarview.calendars.hijri.compose.rememberHijriMonthState
import com.develotter.calendarview.calendars.solarHijri.compose.rememberSolarHijriMonthState
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriEpochDay
import com.develotter.calendarview.compose.CalendarMonthNavigationState
import ir.huri.jcal.JalaliCalendar
import java.time.LocalDate
import java.time.YearMonth
import java.time.chrono.HijrahDate
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
    onMonthChanged: (Long) -> Unit,
): SampleMonthNavigation {
    val date = LocalDate.ofEpochDay(anchor)
    val onChanged by rememberUpdatedState(onMonthChanged)
    return when (calendar) {
        SampleCalendar.Gregorian -> {
            val state = rememberGregorianMonthState(YearMonth.from(date))
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
            val state = rememberHijriMonthState(HijrahDate.from(date))
            LaunchedEffect(state.month) {
                if (state.month.toEpochDay() != anchor) onChanged(state.month.toEpochDay())
            }
            LaunchedEffect(anchor) {
                val target = HijrahDate.from(LocalDate.ofEpochDay(anchor))
                    .with(ChronoField.DAY_OF_MONTH, 1)
                if (state.month != target) state.goTo(target)
            }
            SampleMonthNavigation(title = state.title(locale), state = state)
        }
        SampleCalendar.SolarHijri -> {
            val state = rememberSolarHijriMonthState(JalaliCalendar(date))
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
    }
}
