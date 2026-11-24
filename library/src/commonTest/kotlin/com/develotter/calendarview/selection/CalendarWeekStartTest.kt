package com.develotter.calendarview.selection

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CalendarWeekStartTest {
    @Test
    fun requireValidFirstDayOfWeekAccepts1Through7AndRejectsOthers() {
        for (day in 1..7) {
            assertEquals(day, requireValidFirstDayOfWeek(day))
        }
        assertFailsWith<IllegalArgumentException> { requireValidFirstDayOfWeek(0) }
        assertFailsWith<IllegalArgumentException> { requireValidFirstDayOfWeek(8) }
        assertFailsWith<IllegalArgumentException> { requireValidFirstDayOfWeek(-1) }
    }

    @Test
    fun firstDayOffsetMatchesAllSevenStartsAcrossWeek() {
        // 1970-01-01 (epochDay = 0) was Thursday (ISO 4).
        val thursday = 0L
        assertEquals(3, firstDayOffset(thursday, firstDayOfWeek = 1)) // Mon -> Thu is 3
        assertEquals(2, firstDayOffset(thursday, firstDayOfWeek = 2)) // Tue -> Thu is 2
        assertEquals(1, firstDayOffset(thursday, firstDayOfWeek = 3)) // Wed -> Thu is 1
        assertEquals(0, firstDayOffset(thursday, firstDayOfWeek = 4)) // Thu -> Thu is 0
        assertEquals(6, firstDayOffset(thursday, firstDayOfWeek = 5)) // Fri -> Thu is 6
        assertEquals(5, firstDayOffset(thursday, firstDayOfWeek = 6)) // Sat -> Thu is 5
        assertEquals(4, firstDayOffset(thursday, firstDayOfWeek = 7)) // Sun -> Thu is 4
    }

    @Test
    fun weekStartEpochDayAlignsAndRejectsInvalidStarts() {
        // 2024-10-04 is epochDay 20_000 (Friday, ISO 5).
        val friday = 20_000L
        assertEquals(19_996L, weekStartEpochDay(friday, firstDayOfWeek = 1)) // Monday
        assertEquals(19_999L, weekStartEpochDay(friday, firstDayOfWeek = 4)) // Thursday
        assertEquals(20_000L, weekStartEpochDay(friday, firstDayOfWeek = 5)) // Friday
        assertEquals(19_994L, weekStartEpochDay(friday, firstDayOfWeek = 6)) // Saturday
        assertEquals(19_995L, weekStartEpochDay(friday, firstDayOfWeek = 7)) // Sunday

        assertFailsWith<IllegalArgumentException> { weekStartEpochDay(friday, 0) }
        assertFailsWith<IllegalArgumentException> { weekStartEpochDay(friday, 8) }
    }

    @Test
    fun orderedIsoWeekdaysAndLabelsRotateCorrectly() {
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), orderedIsoWeekdays(1))
        assertEquals(listOf(6, 7, 1, 2, 3, 4, 5), orderedIsoWeekdays(6))
        assertEquals(listOf(7, 1, 2, 3, 4, 5, 6), orderedIsoWeekdays(7))

        val labels = weekdayLabelsStartingAt(6) { iso -> "D$iso" }
        assertEquals(listOf("D6", "D7", "D1", "D2", "D3", "D4", "D5"), labels)

        assertFailsWith<IllegalArgumentException> { orderedIsoWeekdays(0) }
        assertFailsWith<IllegalArgumentException> { weekdayLabelsStartingAt(8) { it.toString() } }
    }
}
