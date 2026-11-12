package com.develotter.calendarviewsample

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Target minimum month width honoring 48 dp per day cell:
 * - Without week numbers: 7 × 48 dp = 336 dp.
 * - With week numbers (40 dp column): 336 dp + 40 dp = 376 dp.
 * Documented in docs/ADAPTIVE_LAYOUT.md.
 *
 * This policy is sample-specific; the underlying library defaults remain 320 dp.
 */
internal fun sampleTargetMonthWidth(showWeekNumbers: Boolean): Dp =
    if (showWeekNumbers) 376.dp else 336.dp
