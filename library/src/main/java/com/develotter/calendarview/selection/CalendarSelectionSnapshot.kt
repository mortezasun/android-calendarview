package com.develotter.calendarview.selection

import com.develotter.calendarview.enums.TypeSelectDay

/**
 * Versioned primitive-only saved-state format. A nonempty header preserves an explicitly
 * empty selection, and the saved mode prevents incompatible restoration into a new owner.
 * This is saved-instance-state encoding, not a public persistence/file format.
 */
internal object CalendarSelectionSnapshot {
    fun encode(selection: CalendarSelection): List<String> =
        listOf("1", selection.mode.name) + selection.selectedDays.map { it.toString() }

    /** Invalid, old or mode-incompatible snapshots return null so initialization can run. */
    fun decode(expectedMode: TypeSelectDay, values: List<String>): CalendarSelection? {
        if (values.size < 2 || values[0] != "1" || values[1] != expectedMode.name) return null
        val days = ArrayList<Long>(values.size - 2)
        for (value in values.drop(2)) {
            days.add(value.toLongOrNull() ?: return null)
        }
        return try {
            CalendarSelection.from(expectedMode, days)
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
