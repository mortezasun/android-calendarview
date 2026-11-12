package com.develotter.calendarviewsample

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * A fold or hinge that separates the window into two areas. [vertical] means the fold line is
 * vertical (panes side by side); otherwise it is horizontal (tabletop: panes stacked).
 * [start] is the distance from the window's left (vertical) or top (horizontal) edge, [size] the
 * thickness of the hinge along the same axis.
 */
internal data class SampleFold(val vertical: Boolean, val start: Dp, val size: Dp)

internal enum class SampleLayoutKind { Stacked, SideBySide, SplitByVerticalFold, SplitByHorizontalFold }

/**
 * Pure layout decision: a separating fold wins over the width rule, because content must not be
 * drawn across a hinge. Without a fold the actual available [width] decides, never the device.
 */
internal fun sampleLayoutKind(width: Dp, fold: SampleFold?): SampleLayoutKind = when {
    fold != null && fold.vertical -> SampleLayoutKind.SplitByVerticalFold
    fold != null -> SampleLayoutKind.SplitByHorizontalFold
    width >= SampleExpandedPaneMinWidth -> SampleLayoutKind.SideBySide
    else -> SampleLayoutKind.Stacked
}

/** Minimum gap kept between the two panes of a fold-aware layout, even for a zero-width fold. */
internal val SampleMinFoldGap: Dp = 16.dp

/**
 * Observes the window layout and returns the first separating [FoldingFeature], or null on a
 * normal window. Positions are in the window's coordinate space; the sample fills the window.
 */
@Composable
internal fun rememberSampleFold(): SampleFold? {
    val activity = LocalActivity.current
    val density = LocalDensity.current
    val layoutFlow: Flow<WindowLayoutInfo?> = remember(activity) {
        activity?.let { WindowInfoTracker.getOrCreate(it).windowLayoutInfo(it) } ?: emptyFlow()
    }
    val layoutInfo by layoutFlow.collectAsState(initial = null)
    val feature = layoutInfo?.displayFeatures
        ?.filterIsInstance<FoldingFeature>()
        ?.firstOrNull { it.isSeparating }
        ?: return null
    val bounds = feature.bounds
    return with(density) {
        if (feature.orientation == FoldingFeature.Orientation.VERTICAL) {
            SampleFold(vertical = true, start = bounds.left.toDp(), size = bounds.width().toDp())
        } else {
            SampleFold(vertical = false, start = bounds.top.toDp(), size = bounds.height().toDp())
        }
    }
}
