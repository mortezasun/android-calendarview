package com.develotter.calendarview.selection

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarRangePolicyTest {
    @Test
    fun countsBothEndpointsAndAcceptsEitherOrder() {
        val policy = CalendarRangePolicy(minRangeLength = 2, maxRangeLength = 4)
        assertFalse(policy.allows(1L, 1L))
        assertTrue(policy.allows(1L, 2L))
        assertTrue(policy.allows(4L, 1L))
        assertTrue(policy.allows(1L, 4L))
        assertFalse(policy.allows(1L, 5L))
    }

    @Test
    fun defaultPolicyAcceptsEveryRepresentableRange() {
        val policy = CalendarRangePolicy()
        assertTrue(policy.allows(1L, 1L))
        assertFalse(policy.allows(Long.MIN_VALUE, Long.MAX_VALUE))
    }

    @Test
    fun invalidPolicyIsRejected() {
        for (policy in listOf(
            { CalendarRangePolicy(minRangeLength = 0) },
            { CalendarRangePolicy(minRangeLength = 3, maxRangeLength = 2) },
        )) {
            var rejected = false
            try {
                policy()
            } catch (_: IllegalArgumentException) {
                rejected = true
            }
            assertTrue(rejected)
        }
    }
}
