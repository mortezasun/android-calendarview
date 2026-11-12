package com.develotter.calendarviewsample

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Available content-pane height after insets and pane padding, before scroll measurement. */
internal val LocalSamplePaneHeight = compositionLocalOf { Dp.Infinity }

/**
 * Caps a preferred viewport at 75 percent of the bounded pane, leaving a proportional allowance
 * for controls. This is a sizing policy, not a guarantee that all controls fit without scrolling.
 * Unhosted callers retain their preferred height. No minimum can exceed a tiny or zero pane.
 */
internal fun sampleBoundedViewportHeight(preferred: Dp, available: Dp): Dp {
    require(preferred.value.isFinite() && preferred >= 0.dp) { "preferred must be finite and non-negative" }
    require(!available.value.isNaN() && available >= 0.dp) { "available must be non-negative" }
    return if (available == Dp.Infinity) preferred else minOf(preferred, available * 0.75f)
}
