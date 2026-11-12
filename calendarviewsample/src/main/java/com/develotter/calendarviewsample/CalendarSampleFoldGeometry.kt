package com.develotter.calendarviewsample

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Converts window-axis fold bounds into the measured inset-content coordinate space.
 * Offscreen folds are ignored. A narrow fold gets a centered minimum gap, clipped at the
 * content edges without moving the physical hinge or exceeding the available extent.
 */
internal fun sampleLocalFold(fold: SampleFold, origin: Dp, extent: Dp): SampleFold? {
    require(origin.value.isFinite()) { "origin must be finite" }
    require(extent.value.isFinite() && extent >= 0.dp) { "extent must be finite and non-negative" }
    require(fold.start.value.isFinite()) { "fold start must be finite" }
    require(fold.size.value.isFinite() && fold.size >= 0.dp) { "fold size must be finite and non-negative" }
    if (extent == 0.dp) return null
    val start = fold.start - origin
    val end = start + fold.size
    if (fold.size == 0.dp) {
        if (start <= 0.dp || start >= extent) return null
    } else if (end <= 0.dp || start >= extent) {
        return null
    }
    val extra = (SampleMinFoldGap - fold.size).coerceAtLeast(0.dp) / 2f
    val localStart = (start - extra).coerceIn(0.dp, extent)
    val localEnd = (end + extra).coerceIn(0.dp, extent)
    return SampleFold(fold.vertical, localStart, localEnd - localStart)
}
