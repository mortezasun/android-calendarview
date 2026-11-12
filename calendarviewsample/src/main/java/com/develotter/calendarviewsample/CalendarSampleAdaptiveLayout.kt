package com.develotter.calendarviewsample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

internal val SampleExpandedPaneMinWidth: Dp = 840.dp

/**
 * Demo-only adaptive host with caller-direction content and physical fold geometry.
 * The measured inner origin includes safe-drawing insets and any host offset. Movable slots
 * retain their composition identity when panes change; runtime restoration needs UI tests.
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
    val windowFold = rememberSampleFold()
    val contentDirection = LocalLayoutDirection.current
    val density = LocalDensity.current
    var origin by remember { mutableStateOf<Offset?>(null) }
    // Apply insets once, outside the measured coordinate space used for pane placement.
    Box(Modifier.fillMaxSize().safeDrawingPadding()) {
        BoxWithConstraints(
            Modifier.fillMaxSize().onGloballyPositioned { origin = it.positionInWindow() },
        ) {
            val measuredOrigin = origin
            if (windowFold != null && measuredOrigin == null) return@BoxWithConstraints
            val fold = if (windowFold != null && measuredOrigin != null) {
                val axisOrigin = with(density) {
                    (if (windowFold.vertical) measuredOrigin.x else measuredOrigin.y).toDp()
                }
                sampleLocalFold(windowFold, axisOrigin, if (windowFold.vertical) maxWidth else maxHeight)
            } else null
            // Capture the bounded pane before verticalScroll gives its children infinite height.
            val paneHeight = (
                (if (fold != null && !fold.vertical) fold.start else maxHeight) - 32.dp
            ).coerceAtLeast(0.dp)
            val paneContent: @Composable () -> Unit = {
                CompositionLocalProvider(LocalSamplePaneHeight provides paneHeight) {
                    movableContent()
                }
            }
            when (sampleLayoutKind(maxWidth, fold)) {
                SampleLayoutKind.SideBySide -> Row(
                    Modifier.fillMaxSize().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(
                        Modifier.weight(0.4f).fillMaxHeight().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) { movableHeader(); movableActions() }
                    Column(
                        Modifier.weight(0.6f).fillMaxHeight().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) { paneContent() }
                }
                SampleLayoutKind.SplitByVerticalFold -> {
                    val separator = checkNotNull(fold)
                    // Only geometry is LTR; pane contents keep the caller's direction.
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(Modifier.fillMaxSize()) {
                            CompositionLocalProvider(LocalLayoutDirection provides contentDirection) {
                                Column(
                                    Modifier.width(separator.start).fillMaxHeight().verticalScroll(rememberScrollState()).padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) { movableHeader(); movableActions() }
                            }
                            Spacer(Modifier.width(separator.size))
                            CompositionLocalProvider(LocalLayoutDirection provides contentDirection) {
                                Column(
                                    Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) { paneContent() }
                            }
                        }
                    }
                }
                SampleLayoutKind.SplitByHorizontalFold -> {
                    val separator = checkNotNull(fold)
                    Column(Modifier.fillMaxSize()) {
                        Column(
                            Modifier.height(separator.start).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) { paneContent() }
                        Spacer(Modifier.height(separator.size))
                        Column(
                            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) { movableHeader(); movableActions() }
                    }
                }
                SampleLayoutKind.Stacked -> Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) { movableHeader(); paneContent(); movableActions() }
            }
        }
    }
}
