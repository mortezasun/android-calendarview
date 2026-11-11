package com.develotter.calendarview.calendars.hijri.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarDayMarker
import com.develotter.calendarview.compose.CalendarMonthData
import com.develotter.calendarview.compose.CalendarMonthGrid
import com.develotter.calendarview.calendars.hijri.HijriDateProvider
import com.develotter.calendarview.calendars.hijri.UmmAlQuraHijriDateProvider
import com.develotter.calendarview.selection.CalendarSelection
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import java.util.Locale

/** Localized month snapshot; treat lists as read-only. */
@Immutable
data class HijriMonthData(
    override val title: String,
    override val weekdayLabels: List<String>,
    override val firstDayOffset: Int,
    override val days: List<CalendarDayCell>,
) : CalendarMonthData

/** Formats a Hijri day with its own chronology, including dates in adjacent months. */
fun hijriDayCell(
    date: HijrahDate,
    locale: Locale = Locale.getDefault(),
    today: HijrahDate = HijrahDate.now(),
    isDateEnabled: (HijrahDate) -> Boolean = { true },
): CalendarDayCell = hijriDayCellForEpoch(
    date = date,
    epochDay = date.toEpochDay(),
    locale = locale,
    today = today,
    isDateEnabled = isDateEnabled,
)

/** Variant for providers whose displayed date differs from the canonical epoch day. */
internal fun hijriDayCellForEpoch(
    date: HijrahDate,
    epochDay: Long,
    locale: Locale,
    today: HijrahDate,
    isDateEnabled: (HijrahDate) -> Boolean,
): CalendarDayCell {
    val numbers = NumberFormat.getIntegerInstance(locale).apply { isGroupingUsed = false }
    val format = DateTimeFormatter.ofPattern("EEEE d MMMM y G", locale).withChronology(date.chronology)
    return CalendarDayCell(
        epochDay = epochDay, label = numbers.format(date.get(ChronoField.DAY_OF_MONTH)),
        accessibilityLabel = date.format(format), enabled = isDateEnabled(date),
        isToday = date.toEpochDay() == today.toEpochDay(),
    )
}

/**
 * Builds a Hijri month with the platform Umm al-Qura chronology and Saturday-first by
 * default. Use the provider overload for a local date method.
 */
fun hijriMonthData(
    month: HijrahDate,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.SATURDAY,
    today: HijrahDate = HijrahDate.now(),
    isDateEnabled: (HijrahDate) -> Boolean = { true },
): HijriMonthData = hijriMonthData(
    month = month,
    locale = locale,
    firstDayOfWeek = firstDayOfWeek,
    today = today,
    dateProvider = UmmAlQuraHijriDateProvider,
    isDateEnabled = isDateEnabled,
)

/**
 * Builds a Hijri month using [dateProvider]. Pass [today] from the same provider when using
 * a local offset. Saturday-first is configurable.
 */
fun hijriMonthData(
    month: HijrahDate,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.SATURDAY,
    today: HijrahDate = HijrahDate.now(),
    dateProvider: HijriDateProvider,
    isDateEnabled: (HijrahDate) -> Boolean = { true },
): HijriMonthData {
    val first = month.with(ChronoField.DAY_OF_MONTH, 1)
    val firstEpochDay = dateProvider.epochDayFor(first)
    return HijriMonthData(
        title = first.format(DateTimeFormatter.ofPattern("MMMM y G", locale).withChronology(first.chronology)),
        weekdayLabels = List(7) { firstDayOfWeek.plus(it.toLong()).getDisplayName(TextStyle.SHORT_STANDALONE, locale) },
        firstDayOffset = Math.floorMod(first.get(ChronoField.DAY_OF_WEEK) - firstDayOfWeek.value, 7),
        days = (0 until first.lengthOfMonth()).map { offset ->
            hijriDayCellForEpoch(
                date = dateProvider.dateForEpochDay(firstEpochDay + offset),
                epochDay = firstEpochDay + offset,
                locale = locale,
                today = today,
                isDateEnabled = isDateEnabled,
            )
        },
    )
}

/**
 * Controlled Hijri month UI with the platform Umm al-Qura chronology. Adjacent cells retain
 * input chronology and use the same enabled predicate and click callback. Use the provider
 * overload for a local date method.
 */
@Composable
fun HijriCalendarMonth(
    month: HijrahDate,
    selection: CalendarSelection,
    onDateClick: (HijrahDate) -> Unit,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.SATURDAY,
    showPreviousMonthDays: Boolean = false,
    showNextMonthDays: Boolean = false,
    today: HijrahDate = HijrahDate.now(),
    showHeader: Boolean = true,
    showWeekdayLabels: Boolean = true,
    fixedSixRows: Boolean = false,
    todayBorderColor: Color? = null,
    todayDescription: String? = null,
    markers: Map<Long, List<CalendarDayMarker>> = emptyMap(),
    weekNumber: ((Long) -> String?)? = null,
    isDateEnabled: (HijrahDate) -> Boolean = { true },
) {
    HijriCalendarMonth(
        month = month,
        selection = selection,
        onDateClick = onDateClick,
        modifier = modifier,
        locale = locale,
        firstDayOfWeek = firstDayOfWeek,
        showPreviousMonthDays = showPreviousMonthDays,
        showNextMonthDays = showNextMonthDays,
        today = today,
        showHeader = showHeader,
        showWeekdayLabels = showWeekdayLabels,
        fixedSixRows = fixedSixRows,
        todayBorderColor = todayBorderColor,
        todayDescription = todayDescription,
        markers = markers,
        weekNumber = weekNumber,
        dateProvider = UmmAlQuraHijriDateProvider,
        isDateEnabled = isDateEnabled,
    )
}

/**
 * Controlled Hijri month UI using [dateProvider]. Adjacent cells retain input chronology and
 * use the same enabled predicate and click callback. Visibility is independent and defaults
 * to blank padding. Engine date-range errors propagate. Locale does not set layout direction.
 */
@Composable
fun HijriCalendarMonth(
    month: HijrahDate,
    selection: CalendarSelection,
    onDateClick: (HijrahDate) -> Unit,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.getDefault(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.SATURDAY,
    showPreviousMonthDays: Boolean = false,
    showNextMonthDays: Boolean = false,
    today: HijrahDate = HijrahDate.now(),
    showHeader: Boolean = true,
    showWeekdayLabels: Boolean = true,
    fixedSixRows: Boolean = false,
    todayBorderColor: Color? = null,
    todayDescription: String? = null,
    markers: Map<Long, List<CalendarDayMarker>> = emptyMap(),
    weekNumber: ((Long) -> String?)? = null,
    dateProvider: HijriDateProvider,
    isDateEnabled: (HijrahDate) -> Boolean = { true },
) {
    val data = hijriMonthData(
        month = month, locale = locale, firstDayOfWeek = firstDayOfWeek, today = today,
        dateProvider = dateProvider, isDateEnabled = isDateEnabled,
    )
    CalendarMonthGrid(
        title = data.title, weekdayLabels = data.weekdayLabels, firstDayOffset = data.firstDayOffset,
        days = data.days, selection = selection, modifier = modifier,
        onDayClick = { onDateClick(dateProvider.dateForEpochDay(it)) },
        showPreviousMonthDays = showPreviousMonthDays, showNextMonthDays = showNextMonthDays,
        showHeader = showHeader, showWeekdayLabels = showWeekdayLabels, fixedSixRows = fixedSixRows,
        todayBorderColor = todayBorderColor,
        todayDescription = todayDescription,
        markers = markers,
        weekNumber = weekNumber,
        adjacentDayProvider = {
            hijriDayCellForEpoch(
                date = dateProvider.dateForEpochDay(it), epochDay = it, locale = locale,
                today = today, isDateEnabled = isDateEnabled,
            )
        },
    )
}
