package com.develotter.calendarview.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import com.develotter.calendarview.enums.TypeSelectDay
import com.develotter.calendarview.selection.CalendarSelection

/** Observable owner of the UI-independent selection engine. Mutate on the UI thread. */
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
 * Remembers selection and saves explicit absolute-day keys through saved instance state.
 * Changing mode resets the selection. Initial keys are used only on first creation;
 * restored keys take precedence. Range interiors are never expanded into saved state.
 * Saved-state size still grows with explicit multiple selections.
 */
@Composable
fun rememberCalendarSelectionState(
    mode: TypeSelectDay = TypeSelectDay.Single,
    initialEpochDays: List<Long> = emptyList(),
): CalendarSelectionState = rememberSaveable(
    mode,
    saver = listSaver<CalendarSelectionState, Long>(
        save = { it.selection.selectedDays },
        restore = { CalendarSelectionState(CalendarSelection.from(mode, it)) },
    ),
) {
    CalendarSelectionState(CalendarSelection.from(mode, initialEpochDays))
}
