package com.develotter.calendarviewsample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Width at which the sample switches to two panes. It follows the Material "expanded" window
 * width class (840 dp) but is measured from the actual available width, not from the device.
 */
internal val SampleExpandedPaneMinWidth: Dp = 840.dp

/**
 * Demo-only adaptive host. Compact widths stack [header], [content] and [actions] in one
 * scrolling column; expanded widths show [header] and [actions] in a controls pane beside the
 * [content] pane. A separating fold (book or tabletop posture) overrides the width rule and
 * places the panes on either side of the hinge. The slots are `movableContentOf`, so their state (display mode, scroll, dialogs)
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
    val fold = rememberSampleFold()
    val contentDirection = LocalLayoutDirection.current
    BoxWithConstraints(Modifier.fillMaxSize()) {
        when (sampleLayoutKind(maxWidth, fold)) {
            SampleLayoutKind.SideBySide -> Row(
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
            SampleLayoutKind.SplitByVerticalFold -> {
                val separator = checkNotNull(fold)
                val gap = separator.size.coerceAtLeast(SampleMinFoldGap)
                val left = separator.start.coerceIn(0.dp, (maxWidth - gap).coerceAtLeast(0.dp))
                // Fold bounds are physical left-origin coordinates, independent of text direction.
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(Modifier.fillMaxSize().safeDrawingPadding()) {
                        CompositionLocalProvider(LocalLayoutDirection provides contentDirection) {
                            Column(
                                Modifier.width(left).fillMaxHeight().verticalScroll(rememberScrollState()).padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                movableHeader()
                                movableActions()
                            }
                        }
                        Spacer(Modifier.width(gap))
                        CompositionLocalProvider(LocalLayoutDirection provides contentDirection) {
                            Column(
                                Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                movableContent()
                            }
                        }
                    }
                }
            }
            SampleLayoutKind.SplitByHorizontalFold -> {
                // Tabletop posture: calendar above the hinge, controls below it.
                val separator = checkNotNull(fold)
                val gap = separator.size.coerceAtLeast(SampleMinFoldGap)
                val top = separator.start.coerceIn(0.dp, (maxHeight - gap).coerceAtLeast(0.dp))
                Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                    Column(
                        Modifier.height(top).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        movableContent()
                    }
                    Spacer(Modifier.height(gap))
                    Column(
                        Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        movableHeader()
                        movableActions()
                    }
                }
            }
            SampleLayoutKind.Stacked -> Column(
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
