package com.develotter.calendarview.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Calendar-independent month navigation contract implemented by the engine month states.
 *
 * The state owns the visible month and its bounds; the header only renders and dispatches.
 * Navigation methods do nothing when the corresponding capability is false, except [goToToday]
 * which similarly stays on the current month when today is outside the configured bounds.
 * Mutate on the UI thread, like the selection state.
 */
@Stable
interface CalendarMonthNavigationState {
    /** Whether [previous] can move the visible month backwards. */
    val canGoPrevious: Boolean

    /** Whether [next] can move the visible month forwards. */
    val canGoNext: Boolean

    /** Whether today lies inside the configured month bounds. */
    val canGoToday: Boolean

    /** Moves to the previous month when allowed; otherwise does nothing. */
    fun previous()

    /** Moves to the next month when allowed; otherwise does nothing. */
    fun next()

    /** Moves to the month containing today when it is inside the bounds; otherwise does nothing. */
    fun goToToday()
}

/**
 * Stateless header for a calendar month: heading title, optional today action and previous/next
 * buttons. [state] supplies the visible month and the enabled flags; the caller still owns the
 * month value and renders the grid with it.
 *
 * [previousMonthContentDescription] and [nextMonthContentDescription] are required because the
 * library ships no localized UI strings; pass localized values from the host application.
 * [todayLabel] is optional; when null no today button is shown. The default arrow glyphs are
 * decorative and hidden from accessibility services in favour of the button descriptions.
 */
@Composable
fun CalendarMonthHeader(
    title: String,
    state: CalendarMonthNavigationState,
    previousMonthContentDescription: String,
    nextMonthContentDescription: String,
    modifier: Modifier = Modifier,
    todayLabel: String? = null,
    titleContent: @Composable (String) -> Unit = {
        Text(it, style = MaterialTheme.typography.titleMedium)
    },
    previousContent: @Composable () -> Unit = {
        Text("\u2039", style = MaterialTheme.typography.titleLarge, modifier = Modifier.clearAndSetSemantics {})
    },
    nextContent: @Composable () -> Unit = {
        Text("\u203A", style = MaterialTheme.typography.titleLarge, modifier = Modifier.clearAndSetSemantics {})
    },
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(Modifier.weight(1f).semantics(mergeDescendants = true) { heading() }) {
            titleContent(title)
        }
        if (todayLabel != null) {
            TextButton(onClick = state::goToToday, enabled = state.canGoToday) {
                Text(todayLabel)
            }
        }
        IconButton(
            onClick = state::previous,
            enabled = state.canGoPrevious,
            modifier = Modifier.semantics { contentDescription = previousMonthContentDescription },
        ) {
            previousContent()
        }
        IconButton(
            onClick = state::next,
            enabled = state.canGoNext,
            modifier = Modifier.semantics { contentDescription = nextMonthContentDescription },
        ) {
            nextContent()
        }
    }
}
