package com.develotter.calendarview.calendars.chineseCompose

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.develotter.calendarview.calendars.chineseNative.ChineseCalendarEngine
import com.develotter.calendarview.calendars.chineseNative.ChineseDate
import com.develotter.calendarview.calendars.chineseNative.ChineseFormat
import com.develotter.calendarview.calendars.chineseNative.ChineseMonth
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarDayMarker
import com.develotter.calendarview.compose.CalendarMonthData
import com.develotter.calendarview.compose.CalendarMonthGrid
import com.develotter.calendarview.selection.CalendarSelection

/** Localized Chinese month snapshot; lists are read-only. */
@Immutable
data class ChineseMonthData(
    val month: ChineseMonth,
    override val title: String,
    override val weekdayLabels: List<String>,
    override val firstDayOffset: Int,
    override val days: List<CalendarDayCell>,
) : CalendarMonthData

/** Localized Chinese month title, for example `2023 Leap 2nd Month`. */
fun chineseMonthTitle(
    month: ChineseMonth,
    style: ChineseTextStyle = ChineseTextStyle.English,
): String = ChineseFormat.monthTitle(month, style)

/** Empty cell used for adjacent positions outside the supported data range. */
private fun unsupportedAdjacentCell(epochDay: Long): CalendarDayCell = CalendarDayCell(
    epochDay = epochDay,
    label = "",
    accessibilityLabel = "",
    enabled = false,
)

/**
 * One Chinese calendar day. [epochDay] is the shared absolute identity; the label comes from an
 * explicit [style] and the accessibility label always includes the ISO date so screen readers
 * keep both calendars distinguishable.
 */
fun chineseDayCell(
    epochDay: Long,
    style: ChineseTextStyle,
    todayEpochDay: Long,
    isDateEnabled: (Long) -> Boolean = { true },
): CalendarDayCell {
    val date = ChineseDate.fromEpochDay(epochDay)
    return CalendarDayCell(
        epochDay = epochDay,
        label = ChineseFormat.dayLabel(date.dayOfMonth, style),
        accessibilityLabel = "${ChineseFormat.dateLabel(date, style)} (${chineseIsoDate(epochDay)})",
        enabled = isDateEnabled(epochDay),
        isToday = epochDay == todayEpochDay,
    )
}

/**
 * Pure Chinese month data. Disabled days still render but reject taps; callers must not use
 * [ChineseDate.fromEpochDay] on adjacent positions outside the engine range.
 */
fun chineseMonthData(
    month: ChineseMonth,
    style: ChineseTextStyle = ChineseTextStyle.English,
    firstDayOfWeek: Int = ChinesePlatform.defaultFirstDayOfWeek(),
    todayEpochDay: Long = ChinesePlatform.currentEpochDay(),
    isDateEnabled: (Long) -> Boolean = { true },
): ChineseMonthData {
    val firstEpochDay = month.startEpochDay()
    return ChineseMonthData(
        month = month,
        title = chineseMonthTitle(month, style),
        weekdayLabels = chineseWeekdayLabels(firstDayOfWeek),
        firstDayOffset = chineseDayOfWeekOffset(firstEpochDay, firstDayOfWeek),
        days = List(month.lengthOfMonth) { index ->
            val epochDay = firstEpochDay + index
            chineseDayCell(
                epochDay = epochDay,
                style = style,
                todayEpochDay = todayEpochDay,
                isDateEnabled = isDateEnabled,
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
fun ChineseCalendarMonth(
    month: ChineseMonth,
    selection: CalendarSelection,
    onDateClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    style: ChineseTextStyle = ChineseTextStyle.English,
    firstDayOfWeek: Int = ChinesePlatform.defaultFirstDayOfWeek(),
    showPreviousMonthDays: Boolean = false,
    showNextMonthDays: Boolean = false,
    todayEpochDay: Long = ChinesePlatform.currentEpochDay(),
    showHeader: Boolean = true,
    showWeekdayLabels: Boolean = true,
    fixedSixRows: Boolean = false,
    todayBorderColor: Color? = null,
    todayDescription: String? = null,
    markers: Map<Long, List<CalendarDayMarker>> = emptyMap(),
    weekNumber: ((Long) -> String?)? = null,
    isDateEnabled: (Long) -> Boolean = { true },
) {
    ChineseCalendarMonth(
        month = month,
        selection = selection,
        onDateClick = onDateClick,
        modifier = modifier,
        style = style,
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
        isDateEnabled = isDateEnabled,
        secondaryLabel = null,
        secondaryAccessibilityLabel = null,
    )
}

/**
 * Controlled Chinese month view built on the shared [CalendarMonthGrid].
 *
 * Selection, navigation and the visible month stay with the caller; clicks report the absolute
 * epoch day. The title and screen-reader labels always include the leap-month marker. Adjacent
 * days are rendered only where they exist in the supported range; boundary cells stay empty and
 * disabled instead of being converted speculatively. [secondaryLabel] and
 * [secondaryAccessibilityLabel] display an optional second calendar label below each day cell.
 * Weekday labels and the default first day of the week come from the platform locale and do not
 * change the calculation or the layout direction.
 */
@Composable
fun ChineseCalendarMonth(
    month: ChineseMonth,
    selection: CalendarSelection,
    onDateClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    style: ChineseTextStyle = ChineseTextStyle.English,
    firstDayOfWeek: Int = ChinesePlatform.defaultFirstDayOfWeek(),
    showPreviousMonthDays: Boolean = false,
    showNextMonthDays: Boolean = false,
    todayEpochDay: Long = ChinesePlatform.currentEpochDay(),
    showHeader: Boolean = true,
    showWeekdayLabels: Boolean = true,
    fixedSixRows: Boolean = false,
    todayBorderColor: Color? = null,
    todayDescription: String? = null,
    markers: Map<Long, List<CalendarDayMarker>> = emptyMap(),
    weekNumber: ((Long) -> String?)? = null,
    isDateEnabled: (Long) -> Boolean = { true },
    secondaryLabel: ((Long) -> String?)? = null,
    secondaryAccessibilityLabel: ((Long) -> String?)? = null,
    dayContent: @Composable (CalendarDayCell, Boolean, Boolean) -> Unit = { day, _, _ -> Text(day.label) },
) {
    val data = chineseMonthData(
        month = month,
        style = style,
        firstDayOfWeek = firstDayOfWeek,
        todayEpochDay = todayEpochDay,
        isDateEnabled = isDateEnabled,
    )
    CalendarMonthGrid(
        title = data.title,
        weekdayLabels = data.weekdayLabels,
        firstDayOffset = data.firstDayOffset,
        days = data.days,
        selection = selection,
        onDayClick = onDateClick,
        modifier = modifier,
        dayContent = dayContent,
        showPreviousMonthDays = showPreviousMonthDays,
        showNextMonthDays = showNextMonthDays,
        showHeader = showHeader,
        showWeekdayLabels = showWeekdayLabels,
        fixedSixRows = fixedSixRows,
        todayBorderColor = todayBorderColor,
        todayDescription = todayDescription,
        markers = markers,
        weekNumber = weekNumber,
        secondaryLabel = secondaryLabel,
        secondaryAccessibilityLabel = secondaryAccessibilityLabel,
        adjacentDayProvider = { epochDay ->
            if (ChineseCalendarEngine.isSupported(epochDay)) {
                chineseDayCell(epochDay, style, todayEpochDay, isDateEnabled)
            } else {
                unsupportedAdjacentCell(epochDay)
            }
        },
    )
}

/**
 * Gregorian grid with the Chinese calendar as a secondary label per day.
 *
 * The grid, weekday alignment, selection and markers follow the Gregorian month; day one of a
 * Chinese month additionally shows the Chinese month name, so leap months remain visible. Days
 * outside the Chinese engine range simply omit the secondary label.
 */
@Composable
fun GregorianChineseCalendarMonth(
    month: GregorianYearMonth,
    selection: CalendarSelection,
    onDateClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    style: ChineseTextStyle = ChineseTextStyle.English,
    firstDayOfWeek: Int = ChinesePlatform.defaultFirstDayOfWeek(),
    showPreviousMonthDays: Boolean = false,
    showNextMonthDays: Boolean = false,
    todayEpochDay: Long = ChinesePlatform.currentEpochDay(),
    showHeader: Boolean = true,
    showWeekdayLabels: Boolean = true,
    fixedSixRows: Boolean = false,
    todayBorderColor: Color? = null,
    todayDescription: String? = null,
    markers: Map<Long, List<CalendarDayMarker>> = emptyMap(),
    weekNumber: ((Long) -> String?)? = null,
) {
    val data = gregorianOverlayData(month, firstDayOfWeek, todayEpochDay)
    CalendarMonthGrid(
        title = data.title,
        weekdayLabels = data.weekdayLabels,
        firstDayOffset = data.firstDayOffset,
        days = data.days,
        selection = selection,
        onDayClick = onDateClick,
        modifier = modifier,
        showPreviousMonthDays = showPreviousMonthDays,
        showNextMonthDays = showNextMonthDays,
        showHeader = showHeader,
        showWeekdayLabels = showWeekdayLabels,
        fixedSixRows = fixedSixRows,
        todayBorderColor = todayBorderColor,
        todayDescription = todayDescription,
        markers = markers,
        weekNumber = weekNumber,
        adjacentDayProvider = { epochDay -> gregorianDayCell(epochDay, todayEpochDay) },
        secondaryLabel = { epochDay -> chineseSecondaryLabel(epochDay, style) },
        secondaryAccessibilityLabel = { epochDay -> chineseAccessibilityLabel(epochDay, style) },
    )
}

private class GregorianOverlayData(
    val title: String,
    val weekdayLabels: List<String>,
    val firstDayOffset: Int,
    val days: List<CalendarDayCell>,
)

private fun gregorianOverlayData(
    month: GregorianYearMonth,
    firstDayOfWeek: Int,
    todayEpochDay: Long,
): GregorianOverlayData = GregorianOverlayData(
    title = ChinesePlatform.formatMonthYear(month.year, month.month),
    weekdayLabels = chineseWeekdayLabels(firstDayOfWeek),
    firstDayOffset = chineseDayOfWeekOffset(month.atDay(1), firstDayOfWeek),
    days = (1..month.lengthOfMonth()).map { day ->
        gregorianDayCell(month.atDay(day), todayEpochDay)
    },
)

private fun gregorianDayCell(
    epochDay: Long,
    todayEpochDay: Long,
): CalendarDayCell {
    val dayOfMonth = (epochDay - GregorianYearMonth.fromEpochDay(epochDay).atDay(1)).toInt() + 1
    return CalendarDayCell(
        epochDay = epochDay,
        label = ChinesePlatform.formatDayNumber(dayOfMonth),
        accessibilityLabel = ChinesePlatform.formatFullDate(epochDay),
        isToday = epochDay == todayEpochDay,
    )
}

/**
 * Compact Chinese secondary label for another primary calendar grid: renders the Chinese month
 * name on the 1st of the Chinese month (preserving leap-month names) and the day label otherwise,
 * or `null` when [epochDay] falls outside the Chinese engine range.
 */
fun chineseSecondaryLabel(
    epochDay: Long,
    style: ChineseTextStyle = ChineseTextStyle.English,
): String? {
    if (!ChineseCalendarEngine.isSupported(epochDay)) return null
    val date = ChineseDate.fromEpochDay(epochDay)
    return if (date.dayOfMonth == 1) {
        ChineseFormat.monthName(date.month, style)
    } else {
        ChineseFormat.dayLabel(date.dayOfMonth, style)
    }
}

/**
 * Full Chinese date for secondary screen-reader labels and date inspectors, including leap-month
 * identity, or `null` when [epochDay] falls outside the Chinese engine range.
 */
fun chineseAccessibilityLabel(
    epochDay: Long,
    style: ChineseTextStyle = ChineseTextStyle.English,
): String? {
    if (!ChineseCalendarEngine.isSupported(epochDay)) return null
    return ChineseFormat.dateLabel(ChineseDate.fromEpochDay(epochDay), style)
}

/** Chinese month of [month]'s first day, or null when the Gregorian month is outside the range. */
fun chineseMonthOfOrNull(month: GregorianYearMonth): ChineseMonth? {
    val epochDay = month.atDay(1)
    return if (ChineseCalendarEngine.isSupported(epochDay)) ChineseMonth.fromEpochDay(epochDay) else null
}

/** Seven localized short weekday labels in grid order starting at [firstDayOfWeek] (ISO 1..7). */
internal fun chineseWeekdayLabels(firstDayOfWeek: Int): List<String> = List(7) { offset ->
    ChinesePlatform.weekdayShortLabel((firstDayOfWeek - 1 + offset) % 7 + 1)
}

/** Leading empty cells before [epochDay] for a grid that starts on [firstDayOfWeek]. */
internal fun chineseDayOfWeekOffset(epochDay: Long, firstDayOfWeek: Int): Int =
    (GregorianYearMonth.isoWeekdayFromEpochDay(epochDay) - firstDayOfWeek + 7) % 7

/** ISO `yyyy-MM-dd` rendering of an epoch day for screen-reader labels. */
internal fun chineseIsoDate(epochDay: Long): String {
    val yearMonth = GregorianYearMonth.fromEpochDay(epochDay)
    val day = (epochDay - yearMonth.atDay(1)).toInt() + 1
    return "${yearMonth.year}-${yearMonth.month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
}
