package com.develotter.calendarviewsample

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.develotter.calendarview.calendars.chineseNative.ChineseCalendarEngine
import com.develotter.calendarview.calendars.chineseNative.ChineseDate
import com.develotter.calendarview.enums.TypeSelectDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CalendarSampleStateTest {
    @Test
    fun sampleStateUpdatesAnchorAndClearsError() {
        val initialDate = LocalDate.of(2026, 6, 15)
        val state = CalendarSampleState(
            calendarNameState = mutableStateOf(SampleCalendar.Gregorian.name),
            modeNameState = mutableStateOf(TypeSelectDay.Single.name),
            localeTagState = mutableStateOf("en"),
            rtlState = mutableStateOf(false),
            previousDaysState = mutableStateOf(false),
            nextDaysState = mutableStateOf(false),
            fixedSixRowsState = mutableStateOf(false),
            showWeekState = mutableStateOf(false),
            showAgendaState = mutableStateOf(false),
            showWeekNumbersState = mutableStateOf(false),
            customStyleState = mutableStateOf(false),
            useHeaderApiState = mutableStateOf(false),
            limitRangeState = mutableStateOf(false),
            hijriOffsetState = mutableIntStateOf(0),
            anchorState = mutableStateOf(initialDate.toEpochDay()),
            showDialogState = mutableStateOf(false),
            errorState = mutableStateOf("stale error"),
        )
        assertEquals(SampleCalendar.Gregorian, state.calendar)
        assertEquals(TypeSelectDay.Single, state.mode)
        assertEquals("en", state.locale.language)

        state.move(1)
        assertEquals(LocalDate.of(2026, 7, 1).toEpochDay(), state.anchor)
        assertNull(state.error)

        state.selectCalendar(SampleCalendar.Hijri)
        assertEquals(SampleCalendar.Hijri, state.calendar)
        assertNull(state.error)

        state.goToToday()
        assertEquals(LocalDate.now().toEpochDay(), state.anchor)
        assertNull(state.error)
    }

    @Test
    fun switchingToChineseClampsUnsupportedAnchorsAtBothEnds() {
        val below = sampleState(ChineseCalendarEngine.MIN_EPOCH_DAY - 1)
        below.selectCalendar(SampleCalendar.Chinese)
        assertEquals(SampleCalendar.Chinese, below.calendar)
        assertEquals(ChineseCalendarEngine.MIN_EPOCH_DAY, below.anchor)
        assertNotNull(below.error)
        assertTrue(below.error!!.contains("1901-02-19"))

        val above = sampleState(ChineseCalendarEngine.MAX_EPOCH_DAY + 1)
        above.selectCalendar(SampleCalendar.Chinese)
        assertEquals(SampleCalendar.Chinese, above.calendar)
        assertEquals(ChineseCalendarEngine.MAX_EPOCH_DAY, above.anchor)
        assertNotNull(above.error)
        assertTrue(above.error!!.contains("2100-02-08"))

        // Both inclusive boundaries are supported and switch without a fallback message.
        val lowerBound = sampleState(ChineseCalendarEngine.MIN_EPOCH_DAY)
        lowerBound.selectCalendar(SampleCalendar.Chinese)
        assertEquals(ChineseCalendarEngine.MIN_EPOCH_DAY, lowerBound.anchor)
        assertNull(lowerBound.error)

        val upperBound = sampleState(ChineseCalendarEngine.MAX_EPOCH_DAY)
        upperBound.selectCalendar(SampleCalendar.Chinese)
        assertEquals(ChineseCalendarEngine.MAX_EPOCH_DAY, upperBound.anchor)
        assertNull(upperBound.error)
    }

    @Test
    fun switchingOffChineseOverlayClampsOutOfRangeAnchorsToMonthBoundaries() {
        val firstMonthStart = ChineseCalendarEngine.firstMonthOfYear(ChineseCalendarEngine.MIN_YEAR).startEpochDay()
        val lastMonthStart = ChineseCalendarEngine.lastMonthOfYear(ChineseCalendarEngine.MAX_YEAR).startEpochDay()

        val below = sampleState(
            anchor = ChineseCalendarEngine.MIN_EPOCH_DAY - 1,
            calendar = SampleCalendar.Chinese,
            chineseGregorianGrid = true,
        )
        below.chineseGregorianGrid = false
        assertEquals(firstMonthStart, below.anchor)
        assertNotNull(below.error)
        assertTrue(below.error!!.contains("1901-02-19"))
        below.move(1)
        assertEquals(ChineseCalendarEngine.nextMonth(
            ChineseCalendarEngine.firstMonthOfYear(ChineseCalendarEngine.MIN_YEAR),
        ).startEpochDay(), below.anchor)
        below.move(-1)
        assertEquals(firstMonthStart, below.anchor)

        val above = sampleState(
            anchor = ChineseCalendarEngine.MAX_EPOCH_DAY + 1,
            calendar = SampleCalendar.Chinese,
            chineseGregorianGrid = true,
        )
        above.chineseGregorianGrid = false
        assertEquals(lastMonthStart, above.anchor)
        assertNotNull(above.error)
        assertTrue(above.error!!.contains("2100-02-08"))
        above.move(-1)
        assertEquals(ChineseCalendarEngine.previousMonth(
            ChineseCalendarEngine.lastMonthOfYear(ChineseCalendarEngine.MAX_YEAR),
        ).startEpochDay(), above.anchor)
        above.move(1)
        assertEquals(lastMonthStart, above.anchor)
    }

    @Test
    fun chineseOverlayMovesByGregorianMonths() {
        val state = sampleState(LocalDate.of(2023, 2, 15).toEpochDay())
        state.chineseGregorianGrid = true
        state.selectCalendar(SampleCalendar.Chinese)
        assertNull(state.error)
        state.move(1)
        assertEquals(LocalDate.of(2023, 3, 1).toEpochDay(), state.anchor)
        state.move(-1)
        assertEquals(LocalDate.of(2023, 2, 1).toEpochDay(), state.anchor)

        state.chineseGregorianGrid = false
        state.showMonth(ChineseDate(2023, 2, true, 1).toEpochDay())
        state.move(1)
        assertEquals(ChineseDate(2023, 3, false, 1).toEpochDay(), state.anchor)
    }

    private fun sampleState(
        anchor: Long,
        calendar: SampleCalendar = SampleCalendar.Gregorian,
        chineseGregorianGrid: Boolean = false,
    ): CalendarSampleState = CalendarSampleState(
        calendarNameState = mutableStateOf(calendar.name),
        modeNameState = mutableStateOf(TypeSelectDay.Single.name),
        localeTagState = mutableStateOf("en"),
        rtlState = mutableStateOf(false),
        previousDaysState = mutableStateOf(false),
        nextDaysState = mutableStateOf(false),
        fixedSixRowsState = mutableStateOf(false),
        showWeekState = mutableStateOf(false),
        showAgendaState = mutableStateOf(false),
        showWeekNumbersState = mutableStateOf(false),
        customStyleState = mutableStateOf(false),
        useHeaderApiState = mutableStateOf(false),
        limitRangeState = mutableStateOf(false),
        hijriOffsetState = mutableIntStateOf(0),
        anchorState = mutableStateOf(anchor),
        showDialogState = mutableStateOf(false),
        errorState = mutableStateOf(null),
        chineseGregorianGridState = mutableStateOf(chineseGregorianGrid),
    )

    @Test
    fun sampleStatePreservesDisplaySettings() {
        val state = CalendarSampleState(
            calendarNameState = mutableStateOf(SampleCalendar.Gregorian.name),
            modeNameState = mutableStateOf(TypeSelectDay.Range.name),
            localeTagState = mutableStateOf("de"),
            rtlState = mutableStateOf(true),
            previousDaysState = mutableStateOf(true),
            nextDaysState = mutableStateOf(true),
            fixedSixRowsState = mutableStateOf(true),
            showWeekState = mutableStateOf(true),
            showAgendaState = mutableStateOf(true),
            showWeekNumbersState = mutableStateOf(true),
            customStyleState = mutableStateOf(true),
            useHeaderApiState = mutableStateOf(true),
            limitRangeState = mutableStateOf(true),
            hijriOffsetState = mutableIntStateOf(1),
            anchorState = mutableStateOf(LocalDate.now().toEpochDay()),
            showDialogState = mutableStateOf(true),
            errorState = mutableStateOf(null),
        )
        assertEquals(TypeSelectDay.Range, state.mode)
        assertEquals("de", state.locale.language)
        assertEquals(true, state.rtl)
        assertEquals(true, state.previousDays)
        assertEquals(true, state.nextDays)
        assertEquals(true, state.fixedSixRows)
        assertEquals(true, state.showWeek)
        assertEquals(true, state.showAgenda)
        assertEquals(true, state.showWeekNumbers)
        assertEquals(true, state.customStyle)
        assertEquals(true, state.useHeaderApi)
        assertEquals(true, state.limitRange)
        assertEquals(1, state.hijriOffset)
        assertEquals(true, state.showDialog)
    }

    @Test
    fun moveCapturesExceptionAsError() {
        val state = CalendarSampleState(
            calendarNameState = mutableStateOf(SampleCalendar.Gregorian.name),
            modeNameState = mutableStateOf(TypeSelectDay.Single.name),
            localeTagState = mutableStateOf("en"),
            rtlState = mutableStateOf(false),
            previousDaysState = mutableStateOf(false),
            nextDaysState = mutableStateOf(false),
            fixedSixRowsState = mutableStateOf(false),
            showWeekState = mutableStateOf(false),
            showAgendaState = mutableStateOf(false),
            showWeekNumbersState = mutableStateOf(false),
            customStyleState = mutableStateOf(false),
            useHeaderApiState = mutableStateOf(false),
            limitRangeState = mutableStateOf(false),
            hijriOffsetState = mutableIntStateOf(0),
            anchorState = mutableStateOf(LocalDate.MAX.toEpochDay()),
            showDialogState = mutableStateOf(false),
            errorState = mutableStateOf(null),
        )
        state.move(1)
        org.junit.Assert.assertNotNull(state.error)

        state.showMonth(LocalDate.of(2026, 1, 1).toEpochDay())
        assertNull(state.error)
    }

    @Test
    fun selectCalendarClearsStaleError() {
        val state = CalendarSampleState(
            calendarNameState = mutableStateOf(SampleCalendar.Gregorian.name),
            modeNameState = mutableStateOf(TypeSelectDay.Single.name),
            localeTagState = mutableStateOf("en"),
            rtlState = mutableStateOf(false),
            previousDaysState = mutableStateOf(false),
            nextDaysState = mutableStateOf(false),
            fixedSixRowsState = mutableStateOf(false),
            showWeekState = mutableStateOf(false),
            showAgendaState = mutableStateOf(false),
            showWeekNumbersState = mutableStateOf(false),
            customStyleState = mutableStateOf(false),
            useHeaderApiState = mutableStateOf(false),
            limitRangeState = mutableStateOf(false),
            hijriOffsetState = mutableIntStateOf(0),
            anchorState = mutableStateOf(LocalDate.of(2026, 1, 1).toEpochDay()),
            showDialogState = mutableStateOf(false),
            errorState = mutableStateOf("prior error"),
        )
        state.selectCalendar(SampleCalendar.SolarHijri)
        assertEquals(SampleCalendar.SolarHijri, state.calendar)
        state.selectCalendar(SampleCalendar.SolarHijriNative)
        assertEquals(SampleCalendar.SolarHijriNative, state.calendar)
        assertNull(state.error)
    }
}

