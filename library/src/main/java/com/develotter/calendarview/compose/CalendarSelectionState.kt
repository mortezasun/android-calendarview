package com.develotter.calendarview.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import com.develotter.calendarview.enums.TypeSelectDay
import com.develotter.calendarview.selection.CalendarSelection
import com.develotter.calendarview.selection.CalendarSelectionSnapshot

/** Observable owner of the UI-independent selection engine. Mutate on the UI thread. */
@Stable
class CalendarSelectionState(initialSelection: CalendarSelection) {
    var selection by mutableStateOf(initialSelection)
        private set

    /** Applies a tap; callers must reject disabled dates before dispatching. */
    fun onDayClick(epochDay: Long) {
        selection = selection.onDayClick(epochDay)
    }

    /** Clears selected days while retaining the selection mode. */
    fun clear() {
        selection = selection.clear()
    }
}

/**
 * Remembers selection and saves a version, mode and explicit absolute-day keys.
 * An empty selection still has a nonempty snapshot and takes precedence over initial dates.
 * Changing mode during composition resets selection. Restoration separately checks mode;
 * incompatible or invalid snapshots fall back to initialization. Old unversioned snapshots
 * from development builds are not migrated. Initial dates are initializers, not controlled
 * inputs. Saved-state size still grows with explicit multiple selections.
 */
@Composable
fun rememberCalendarSelectionState(
    mode: TypeSelectDay = TypeSelectDay.Single,
    initialEpochDays: List<Long> = emptyList(),
): CalendarSelectionState = rememberSaveable(
    mode,
    saver = listSaver<CalendarSelectionState, String>(
        save = { CalendarSelectionSnapshot.encode(it.selection) },
        restore = { values ->
            CalendarSelectionSnapshot.decode(mode, values)?.let { CalendarSelectionState(it) }
        },
    ),
) {
    CalendarSelectionState(CalendarSelection.from(mode, initialEpochDays))
}
