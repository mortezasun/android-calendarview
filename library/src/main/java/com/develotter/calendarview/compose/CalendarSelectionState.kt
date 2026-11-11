package com.develotter.calendarview.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import com.develotter.calendarview.enums.TypeSelectDay
import com.develotter.calendarview.selection.CalendarRangePolicy
import com.develotter.calendarview.selection.CalendarSelection
import com.develotter.calendarview.selection.CalendarSelectionSnapshot

/**
 * Observable owner of the UI-independent selection engine. Mutate on the UI thread.
 *
 * [rangePolicy] limits complete range selections; an incomplete first endpoint is always
 * accepted. It is a validation policy, not a gesture implementation. The one-argument
 * constructor keeps the pre-policy API and uses [CalendarRangePolicy] defaults.
 */
@Stable
class CalendarSelectionState(
    initialSelection: CalendarSelection,
    val rangePolicy: CalendarRangePolicy,
) {
    constructor(initialSelection: CalendarSelection) : this(initialSelection, CalendarRangePolicy())

    init {
        val days = initialSelection.selectedDays
        require(
            !initialSelection.isRangeComplete || rangePolicy.allows(days[0], days[1]),
        ) { "initialSelection must satisfy rangePolicy" }
    }

    var selection by mutableStateOf(initialSelection)
        private set

    /** Applies a tap; callers must reject disabled dates before dispatching. */
    fun onDayClick(epochDay: Long) {
        val next = selection.onDayClick(epochDay)
        if (selection.mode == TypeSelectDay.Range && next.isRangeComplete &&
            !rangePolicy.allows(next.selectedDays[0], next.selectedDays[1])
        ) {
            return
        }
        selection = next
    }

    /**
     * Applies a complete range from an external gesture layer, subject to [rangePolicy].
     * Does nothing outside Range mode or when the endpoints violate the policy.
     */
    fun selectRange(startEpochDay: Long, endEpochDay: Long) {
        if (selection.mode != TypeSelectDay.Range) return
        val next = CalendarSelection.from(TypeSelectDay.Range, listOf(startEpochDay, endEpochDay))
        val days = next.selectedDays
        if (days.size == 2 && rangePolicy.allows(days[0], days[1])) selection = next
    }

    /** Clears selected days while retaining the selection mode. */
    fun clear() {
        selection = selection.clear()
    }
}

/**
 * Remembers selection and saves a version, mode and explicit absolute-day keys.
 * An empty selection still has a nonempty snapshot and takes precedence over initial dates.
 * Changing mode during composition resets selection. Restoration separately checks mode and
 * complete range policy; incompatible or invalid snapshots fall back to initialization. Old
 * unversioned snapshots from development builds are not migrated. Initial dates are
 * initializers, not controlled inputs. Saved-state size still grows with explicit multiple
 * selections.
 */
@Composable
fun rememberCalendarSelectionState(
    mode: TypeSelectDay = TypeSelectDay.Single,
    initialEpochDays: List<Long> = emptyList(),
    rangePolicy: CalendarRangePolicy = CalendarRangePolicy(),
): CalendarSelectionState = rememberSaveable(
    mode,
    rangePolicy,
    saver = listSaver<CalendarSelectionState, String>(
        save = { CalendarSelectionSnapshot.encode(it.selection) },
        restore = { values ->
            CalendarSelectionSnapshot.decode(mode, values)?.let { restored ->
                val days = restored.selectedDays
                if (restored.isRangeComplete && !rangePolicy.allows(days[0], days[1])) {
                    null
                } else {
                    CalendarSelectionState(restored, rangePolicy)
                }
            }
        },
    ),
) {
    CalendarSelectionState(CalendarSelection.from(mode, initialEpochDays), rangePolicy)
}
