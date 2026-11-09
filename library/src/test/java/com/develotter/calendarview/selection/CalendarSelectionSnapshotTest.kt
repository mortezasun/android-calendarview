package com.develotter.calendarview.selection

import com.develotter.calendarview.enums.TypeSelectDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarSelectionSnapshotTest {
    @Test
    fun emptySelectionsHaveNonemptySnapshotsInEveryMode() {
        for (mode in TypeSelectDay.entries) {
            val encoded = CalendarSelectionSnapshot.encode(CalendarSelection.empty(mode))
            assertEquals(listOf("1", mode.name), encoded)
            val decoded = CalendarSelectionSnapshot.decode(mode, encoded)
            assertNotNull(decoded)
            assertTrue(decoded!!.selectedDays.isEmpty())
        }
    }

    @Test
    fun allModesRoundTripTheirExplicitKeys() {
        val cases = listOf(
            TypeSelectDay.None to emptyList<Long>(),
            TypeSelectDay.Single to listOf(10L),
            TypeSelectDay.Multiple to listOf(-1L, 10L, 50L),
            TypeSelectDay.Range to listOf(10L, 50L),
        )
        for ((mode, days) in cases) {
            val restored = CalendarSelectionSnapshot.decode(mode, CalendarSelectionSnapshot.encode(CalendarSelection.from(mode, days)))
            assertNotNull(restored)
            assertEquals(mode, restored!!.mode)
            assertEquals(days, restored.selectedDays)
        }
    }

    @Test
    fun longKeysRoundTripWithoutPrecisionLoss() {
        val days = listOf(Long.MIN_VALUE, Long.MAX_VALUE)
        val value = CalendarSelection.from(TypeSelectDay.Multiple, days)
        val restored = CalendarSelectionSnapshot.decode(value.mode, CalendarSelectionSnapshot.encode(value))
        assertEquals(days, restored!!.selectedDays)
    }

    @Test
    fun differentModeIsRejectedInsteadOfReinterpretingDates() {
        val encoded = CalendarSelectionSnapshot.encode(CalendarSelection.from(TypeSelectDay.Multiple, listOf(1L, 2L)))
        assertNull(CalendarSelectionSnapshot.decode(TypeSelectDay.Single, encoded))
        assertNull(CalendarSelectionSnapshot.decode(TypeSelectDay.Range, encoded))
    }

    @Test
    fun corruptedUnknownAndOldFormatsAreRejected() {
        for (values in listOf(
            emptyList(), listOf("10"), listOf("2", "Single"),
            listOf("1", "Unknown"), listOf("1", "Single", "NaN"),
            listOf("1", "Single", "9223372036854775808"),
            listOf("1", "Single", "1", "2"),
        )) {
            assertNull(CalendarSelectionSnapshot.decode(TypeSelectDay.Single, values))
        }
    }

    @Test
    fun clearedInitialSelectionRestoresAsEmptyNotAsInitialDate() {
        val initial = CalendarSelection.from(TypeSelectDay.Single, listOf(10L))
        val encoded = CalendarSelectionSnapshot.encode(initial.clear())
        val restored = CalendarSelectionSnapshot.decode(initial.mode, encoded) ?: initial
        assertTrue(restored.selectedDays.isEmpty())
        assertEquals(listOf(10L), initial.selectedDays)
    }
}
