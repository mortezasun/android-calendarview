package com.develotter.calendarviewsample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Width at which the sample switches to two panes. It follows the Material "expanded" window
 * width class (840 dp) but is measured from the actual available width, not from the device.
 */
internal val SampleExpandedPaneMinWidth: Dp = 840.dp

/**
 * Demo-only adaptive host. Compact widths stack [header], [content] and [actions] in one
 * scrolling column; expanded widths show [header] and [actions] in a controls pane beside the
 * [content] pane. The slots are `movableContentOf`, so their state (display mode, scroll, dialogs)
 * survives a switch between both layouts caused by rotation, folding or a window resize.
 */
@Composable
internal fun CalendarSampleAdaptiveLayout(
    header: @Composable () -> Unit,
    content: @Composable () -> Unit,
    actions: @Composable () -> Unit,
) {
    val currentHeader by rememberUpdatedState(header)
    val currentContent by rememberUpdatedState(content)
    val currentActions by rememberUpdatedState(actions)
    val movableHeader = remember { movableContentOf { currentHeader() } }
    val movableContent = remember { movableContentOf { currentContent() } }
    val movableActions = remember { movableContentOf { currentActions() } }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth >= SampleExpandedPaneMinWidth) {
            Row(
                Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    Modifier.weight(0.4f).fillMaxHeight().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    movableHeader()
                    movableActions()
                }
                Column(
                    Modifier.weight(0.6f).fillMaxHeight().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    movableContent()
                }
            }
        } else {
            Column(
                Modifier.safeDrawingPadding().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                movableHeader()
                movableContent()
                movableActions()
            }
        }
    }
}
