package com.develotter.calendarview.compose

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class CalendarDayAppearanceTest {
    private val weekend = CalendarDayAppearance(contentColor = Color.Red, bold = true)

    private fun cell(epochDay: Long) = CalendarDayCell(epochDay, epochDay.toString(), epochDay.toString())

    @Test
    fun defaultWeekendIsSaturdayAndSunday() {
        val appearanceFor = calendarWeekendAppearance(weekend)
        // Epoch day 0 is Thursday 1970-01-01.
        assertNull(appearanceFor(cell(0L)))
        assertEquals(weekend, appearanceFor(cell(2L)))
        assertEquals(weekend, appearanceFor(cell(3L)))
        assertNull(appearanceFor(cell(4L)))
    }

    @Test
    fun weekendIsCallerDefined() {
        val appearanceFor = calendarWeekendAppearance(weekend, weekendDays = setOf(5, 6))
        assertEquals(weekend, appearanceFor(cell(1L)))
        assertEquals(weekend, appearanceFor(cell(2L)))
        assertNull(appearanceFor(cell(3L)))
    }

    @Test
    fun rejectsNonIsoWeekdays() {
        assertFailsWith<IllegalArgumentException> { calendarWeekendAppearance(weekend, setOf(0)) }
        assertFailsWith<IllegalArgumentException> { calendarWeekendAppearance(weekend, setOf(8)) }
    }
}
