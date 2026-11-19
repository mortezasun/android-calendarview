package com.develotter.calendarview.calendars.hijri.compose

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.develotter.calendarview.calendars.hijri.HijriDate
import com.develotter.calendarview.calendars.hijri.HijriDateProvider
import com.develotter.calendarview.calendars.hijri.HijriLabels
import com.develotter.calendarview.calendars.hijri.HijriPlatform
import com.develotter.calendarview.calendars.hijri.UmmAlQuraHijriDateProvider
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarDayMarker
import com.develotter.calendarview.compose.CalendarMonthData
import com.develotter.calendarview.compose.CalendarMonthGrid
import com.develotter.calendarview.locale.CalendarLocale
import com.develotter.calendarview.selection.CalendarSelection

/** Localized month snapshot; treat lists as read-only. */
@Immutable
data class HijriMonthData(
    override val title: String,
    override val weekdayLabels: List<String>,
    override val firstDayOffset: Int,
    override val days: List<CalendarDayCell>,
) : CalendarMonthData

/**
 * Localized Hijri month-year title, for example `Shawwal 1447`.
 *
 * [locale] selects the library's own label tables; `null` keeps the platform default locale.
 */
fun hijriMonthTitle(month: HijriDate, locale: CalendarLocale? = null): String =
    locale?.let { HijriLabels.monthYear(it, month.month, month.year) }
        ?: HijriPlatform.formatMonthYear(month.year, month.month)

/**
 * Compact Hijri secondary label for another primary calendar grid: renders the short month name
 * on the 1st of the Hijri month and the localized day number on other days, or `null` if
 * [epochDay] falls outside the platform's supported Hijri range.
 */
fun hijriSecondaryLabel(
    epochDay: Long,
    dateProvider: HijriDateProvider = UmmAlQuraHijriDateProvider,
    locale: CalendarLocale? = null,
): String? = runCatching {
    val date = dateProvider.dateForEpochDay(epochDay)
    when {
        date.dayOfMonth == 1 && locale != null -> HijriLabels.shortMonthName(locale, date.month)
        date.dayOfMonth == 1 -> HijriPlatform.formatShortMonth(date.year, date.month)
        locale != null -> HijriLabels.number(locale, date.dayOfMonth)
        else -> HijriPlatform.formatDayNumber(date.dayOfMonth)
    }
}.getOrNull()

/**
 * Localized full Hijri date description suitable for screen readers and date inspectors, or `null`
 * if [epochDay] falls outside the platform's supported Hijri range.
 */
fun hijriAccessibilityLabel(
    epochDay: Long,
    dateProvider: HijriDateProvider = UmmAlQuraHijriDateProvider,
    locale: CalendarLocale? = null,
): String? = runCatching {
    val date = dateProvider.dateForEpochDay(epochDay)
    locale?.let { HijriLabels.fullDate(it, date, epochDay) }
        ?: HijriPlatform.formatFullDate(date, epochDay)
}.getOrNull()

/** Formats a Hijri day with its own chronology, including dates in adjacent months. */
fun hijriDayCell(
    epochDay: Long,
    dateProvider: HijriDateProvider = UmmAlQuraHijriDateProvider,
    todayEpochDay: Long = HijriPlatform.currentEpochDay(),
    locale: CalendarLocale? = null,
    isDateEnabled: (Long) -> Boolean = { true },
): CalendarDayCell = hijriDayCellForEpoch(
    date = dateProvider.dateForEpochDay(epochDay),
    epochDay = epochDay,
    todayEpochDay = todayEpochDay,
    isDateEnabled = isDateEnabled,
    locale = locale,
)

/** Variant for providers whose displayed date differs from the canonical epoch day. */
internal fun hijriDayCellForEpoch(
    date: HijriDate,
    epochDay: Long,
    todayEpochDay: Long,
    locale: CalendarLocale? = null,
    isDateEnabled: (Long) -> Boolean,
): CalendarDayCell = CalendarDayCell(
    epochDay = epochDay,
    label = locale?.let { HijriLabels.number(it, date.dayOfMonth) }
        ?: HijriPlatform.formatDayNumber(date.dayOfMonth),
    accessibilityLabel = locale?.let { HijriLabels.fullDate(it, date, epochDay) }
        ?: HijriPlatform.formatFullDate(date, epochDay),
    enabled = isDateEnabled(epochDay),
    isToday = epochDay == todayEpochDay,
)

/**
 * Builds a Hijri month with the platform Umm al-Qura chronology and Saturday-first by
 * default. Pass another [dateProvider] for a local date method; the month length and the
 * displayed day labels follow that provider's uniform shift.
 */
fun hijriMonthData(
    month: HijriDate,
    firstDayOfWeek: Int = HijriDefaultFirstDayOfWeek,
    todayEpochDay: Long = HijriPlatform.currentEpochDay(),
    dateProvider: HijriDateProvider = UmmAlQuraHijriDateProvider,
    locale: CalendarLocale? = null,
    isDateEnabled: (Long) -> Boolean = { true },
): HijriMonthData {
    val first = month.copy(dayOfMonth = 1)
    val firstEpochDay = dateProvider.epochDayFor(first)
    return HijriMonthData(
        title = hijriMonthTitle(first, locale),
        weekdayLabels = hijriWeekdayLabels(firstDayOfWeek, locale),
        // The displayed Hijri date may be offset from its absolute timeline weekday.
        firstDayOffset = hijriDayOfWeekOffset(firstEpochDay, firstDayOfWeek),
        days = List(HijriPlatform.lengthOfMonth(first.year, first.month)) { offset ->
            hijriDayCellForEpoch(
                date = dateProvider.dateForEpochDay(firstEpochDay + offset),
                epochDay = firstEpochDay + offset,
                todayEpochDay = todayEpochDay,
                isDateEnabled = isDateEnabled,
                locale = locale,
            )
        },
    )
}

/** Binary-compatible overload for callers compiled against 3.0.0. */
@Deprecated(
    message = "Binary compatibility overload for callers compiled against 3.0.0.",
    level = DeprecationLevel.HIDDEN,
)
@Composable
fun HijriCalendarMonth(
    month: HijriDate,
    selection: CalendarSelection,
    onDateClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    firstDayOfWeek: Int = HijriDefaultFirstDayOfWeek,
    showPreviousMonthDays: Boolean = false,
    showNextMonthDays: Boolean = false,
    todayEpochDay: Long = HijriPlatform.currentEpochDay(),
    showHeader: Boolean = true,
    showWeekdayLabels: Boolean = true,
    fixedSixRows: Boolean = false,
    todayBorderColor: Color? = null,
    todayDescription: String? = null,
    markers: Map<Long, List<CalendarDayMarker>> = emptyMap(),
    weekNumber: ((Long) -> String?)? = null,
    dateProvider: HijriDateProvider = UmmAlQuraHijriDateProvider,
    isDateEnabled: (Long) -> Boolean = { true },
) {
    HijriCalendarMonth(
        month = month,
        selection = selection,
        onDateClick = onDateClick,
        modifier = modifier,
        firstDayOfWeek = firstDayOfWeek,
        showPreviousMonthDays = showPreviousMonthDays,
        showNextMonthDays = showNextMonthDays,
        todayEpochDay = todayEpochDay,
        showHeader = showHeader,
        showWeekdayLabels = showWeekdayLabels,
        fixedSixRows = fixedSixRows,
        todayBorderColor = todayBorderColor,
        todayDescription = todayDescription,
        markers = markers,
        weekNumber = weekNumber,
        dateProvider = dateProvider,
        isDateEnabled = isDateEnabled,
        secondaryLabel = null,
        secondaryAccessibilityLabel = null,
    )
}

/**
 * Controlled Hijri month UI using [dateProvider]. Adjacent cells retain the provider's
 * chronology and use the same enabled predicate and click callback. [onDateClick] receives the
 * absolute epoch day of the clicked cell. Visibility is independent and defaults to blank
 * padding. [secondaryLabel] and [secondaryAccessibilityLabel] display an optional second calendar
 * label below each day cell. Engine date-range errors propagate. Locale does not set layout direction.
 */
@Composable
fun HijriCalendarMonth(
    month: HijriDate,
    selection: CalendarSelection,
    onDateClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    firstDayOfWeek: Int = HijriDefaultFirstDayOfWeek,
    showPreviousMonthDays: Boolean = false,
    showNextMonthDays: Boolean = false,
    todayEpochDay: Long = HijriPlatform.currentEpochDay(),
    showHeader: Boolean = true,
    showWeekdayLabels: Boolean = true,
    fixedSixRows: Boolean = false,
    todayBorderColor: Color? = null,
    todayDescription: String? = null,
    markers: Map<Long, List<CalendarDayMarker>> = emptyMap(),
    weekNumber: ((Long) -> String?)? = null,
    dateProvider: HijriDateProvider = UmmAlQuraHijriDateProvider,
    isDateEnabled: (Long) -> Boolean = { true },
    secondaryLabel: ((Long) -> String?)? = null,
    secondaryAccessibilityLabel: ((Long) -> String?)? = null,
    locale: CalendarLocale? = null,
    dayContent: @Composable (CalendarDayCell, Boolean, Boolean) -> Unit = { day, _, _ -> Text(day.label) },
) {
    val data = hijriMonthData(
        month = month,
        firstDayOfWeek = firstDayOfWeek,
        todayEpochDay = todayEpochDay,
        dateProvider = dateProvider,
        isDateEnabled = isDateEnabled,
        locale = locale,
    )
    CalendarMonthGrid(
        title = data.title, weekdayLabels = data.weekdayLabels, firstDayOffset = data.firstDayOffset,
        days = data.days, selection = selection, modifier = modifier,
        onDayClick = onDateClick,
        dayContent = dayContent,
        showPreviousMonthDays = showPreviousMonthDays, showNextMonthDays = showNextMonthDays,
        showHeader = showHeader, showWeekdayLabels = showWeekdayLabels, fixedSixRows = fixedSixRows,
        todayBorderColor = todayBorderColor,
        todayDescription = todayDescription,
        markers = markers,
        weekNumber = weekNumber,
        secondaryLabel = secondaryLabel,
        secondaryAccessibilityLabel = secondaryAccessibilityLabel,
        adjacentDayProvider = { adjacentEpochDay ->
            hijriDayCellForEpoch(
                date = dateProvider.dateForEpochDay(adjacentEpochDay),
                epochDay = adjacentEpochDay,
                todayEpochDay = todayEpochDay,
                isDateEnabled = isDateEnabled,
                locale = locale,
            )
        },
    )
}
