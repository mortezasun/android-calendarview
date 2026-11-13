package com.develotter.calendarviewsample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.develotter.calendarview.calendars.chineseNative.ChineseCalendarEngine
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.enums.TypeSelectDay
import java.time.LocalDate
import java.util.Locale

/**
 * Demo-only UI configuration. Every value is backed by `rememberSaveable`, so it survives
 * rotation, folding and window resizing. Selection itself stays in
 * `rememberCalendarSelectionState`; this class never owns it.
 */
@Stable
internal class CalendarSampleState(
    private val calendarNameState: MutableState<String>,
    private val modeNameState: MutableState<String>,
    private val localeTagState: MutableState<String>,
    private val rtlState: MutableState<Boolean>,
    private val previousDaysState: MutableState<Boolean>,
    private val nextDaysState: MutableState<Boolean>,
    private val fixedSixRowsState: MutableState<Boolean>,
    private val showWeekState: MutableState<Boolean>,
    private val showAgendaState: MutableState<Boolean>,
    private val showWeekNumbersState: MutableState<Boolean>,
    private val customStyleState: MutableState<Boolean>,
    private val useHeaderApiState: MutableState<Boolean>,
    private val limitRangeState: MutableState<Boolean>,
    private val hijriOffsetState: MutableIntState,
    private val anchorState: MutableState<Long>,
    private val showDialogState: MutableState<Boolean>,
    private val errorState: MutableState<String?>,
    private val chineseStyleNameState: MutableState<String> = mutableStateOf(ChineseTextStyle.English.name),
    private val showChineseDetailsState: MutableState<Boolean> = mutableStateOf(false),
    private val chineseGregorianGridState: MutableState<Boolean> = mutableStateOf(false),
) {
    var calendarName: String by calendarNameState
    var modeName: String by modeNameState
    var localeTag: String by localeTagState
    var rtl: Boolean by rtlState
    var previousDays: Boolean by previousDaysState
    var nextDays: Boolean by nextDaysState
    var fixedSixRows: Boolean by fixedSixRowsState
    var showWeek: Boolean by showWeekState
    var showAgenda: Boolean by showAgendaState
    var showWeekNumbers: Boolean by showWeekNumbersState
    var customStyle: Boolean by customStyleState
    var useHeaderApi: Boolean by useHeaderApiState
    var limitRange: Boolean by limitRangeState
    var hijriOffset: Int by hijriOffsetState
    var anchor: Long by anchorState
    var showDialog: Boolean by showDialogState
    var error: String? by errorState
    var chineseStyleName: String by chineseStyleNameState
    var showChineseDetails: Boolean by showChineseDetailsState
    var chineseGregorianGrid: Boolean by chineseGregorianGridState

    val calendar: SampleCalendar get() = SampleCalendar.valueOf(calendarName)
    val mode: TypeSelectDay get() = TypeSelectDay.valueOf(modeName)
    val locale: Locale get() = Locale.forLanguageTag(localeTag)
    val chineseStyle: ChineseTextStyle get() = ChineseTextStyle.valueOf(chineseStyleName)

    /** Switches the calendar engine and clears a stale navigation error. */
    fun selectCalendar(item: SampleCalendar) {
        calendarName = item.name
        // The Chinese engine has a smaller range than the Gregorian date type. Keep the switch
        // usable at an unsupported anchor by showing the nearest supported month and saying so;
        // the shared selection is intentionally untouched. All conversions below stay in range.
        if (item == SampleCalendar.Chinese && !ChineseCalendarEngine.isSupported(anchor)) {
            anchor = anchor.coerceIn(
                ChineseCalendarEngine.MIN_EPOCH_DAY,
                ChineseCalendarEngine.MAX_EPOCH_DAY,
            )
            error = "Chinese calendar supports 1901-02-19..2100-02-08; showing ${LocalDate.ofEpochDay(anchor)}"
        } else {
            error = null
        }
    }

    /** Sets the anchor from a settled visible month and clears a stale navigation error. */
    fun showMonth(epochDay: Long) {
        anchor = epochDay
        error = null
    }

    fun goToToday() = showMonth(LocalDate.now().toEpochDay())

    /** Moves the anchor by whole months; unsupported dates are reported in [error]. */
    fun move(offset: Int) {
        try {
            anchor = moveSampleMonth(sampleMonthNavigationCalendar(calendar, chineseGregorianGrid), anchor, offset)
            error = null
        } catch (exception: RuntimeException) {
            error = exception.message ?: "Date outside the supported calendar range"
        }
    }
}

@Composable
internal fun rememberCalendarSampleState(): CalendarSampleState {
    val calendarName = rememberSaveable { mutableStateOf(SampleCalendar.Gregorian.name) }
    val modeName = rememberSaveable { mutableStateOf(TypeSelectDay.Single.name) }
    val localeTag = rememberSaveable { mutableStateOf("en") }
    val rtl = rememberSaveable { mutableStateOf(false) }
    val previousDays = rememberSaveable { mutableStateOf(false) }
    val nextDays = rememberSaveable { mutableStateOf(false) }
    val fixedSixRows = rememberSaveable { mutableStateOf(false) }
    val showWeek = rememberSaveable { mutableStateOf(false) }
    val showAgenda = rememberSaveable { mutableStateOf(false) }
    val showWeekNumbers = rememberSaveable { mutableStateOf(false) }
    val customStyle = rememberSaveable { mutableStateOf(false) }
    val useHeaderApi = rememberSaveable { mutableStateOf(false) }
    val limitRange = rememberSaveable { mutableStateOf(false) }
    val hijriOffset = rememberSaveable { mutableIntStateOf(0) }
    val anchor = rememberSaveable { mutableStateOf(LocalDate.now().toEpochDay()) }
    val showDialog = rememberSaveable { mutableStateOf(false) }
    val error = rememberSaveable { mutableStateOf<String?>(null) }
    val chineseStyleName = rememberSaveable { mutableStateOf(ChineseTextStyle.English.name) }
    val showChineseDetails = rememberSaveable { mutableStateOf(false) }
    val chineseGregorianGrid = rememberSaveable { mutableStateOf(false) }
    return CalendarSampleState(
        calendarName, modeName, localeTag, rtl, previousDays, nextDays, fixedSixRows, showWeek,
        showAgenda, showWeekNumbers, customStyle, useHeaderApi, limitRange, hijriOffset, anchor,
        showDialog, error, chineseStyleName, showChineseDetails, chineseGregorianGrid,
    )
}
