package com.develotter.calendarview.selection

import kotlin.test.Test
import kotlin.test.assertEquals

class EpochDayWeekdayTest {
    @Test
    fun isoWeekdayCyclesAcrossTheEpoch() {
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), (-3L..3L).map(::isoWeekday))
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), (-10L..-4L).map(::isoWeekday))
    }

    @Test
    fun isoWeekdayRemainsPeriodicAtLongLimits() {
        assertEquals(4, isoWeekday(Long.MAX_VALUE))
        assertEquals(3, isoWeekday(Long.MIN_VALUE))
        assertEquals(isoWeekday(Long.MAX_VALUE - 7), isoWeekday(Long.MAX_VALUE))
        assertEquals(isoWeekday(Long.MIN_VALUE + 7), isoWeekday(Long.MIN_VALUE))
    }
}
