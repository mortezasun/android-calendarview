package com.develotter.calendarview.compose

import kotlin.test.Test
import kotlin.test.assertFailsWith

class CalendarMonthPickerTest {
    @Test
    fun requireMonthPickerSelectionAcceptsValidRanges() {
        requireMonthPickerSelection(2000..2050, 2026, 1)
        requireMonthPickerSelection(2000..2050, 2026, 12)
        requireMonthPickerSelection(2026..2026, 2026, 6)
    }

    @Test
    fun requireMonthPickerSelectionRejectsEmptyRange() {
        assertFailsWith<IllegalArgumentException> {
            requireMonthPickerSelection(2050..2000, 2026, 5)
        }
    }

    @Test
    fun requireMonthPickerSelectionRejectsYearOutOfRange() {
        assertFailsWith<IllegalArgumentException> {
            requireMonthPickerSelection(2000..2025, 2026, 5)
        }
        assertFailsWith<IllegalArgumentException> {
            requireMonthPickerSelection(2027..2030, 2026, 5)
        }
    }

    @Test
    fun requireMonthPickerSelectionRejectsMonthOutOfRange() {
        assertFailsWith<IllegalArgumentException> {
            requireMonthPickerSelection(2000..2050, 2026, 0)
        }
        assertFailsWith<IllegalArgumentException> {
            requireMonthPickerSelection(2000..2050, 2026, 13)
        }
    }
}
