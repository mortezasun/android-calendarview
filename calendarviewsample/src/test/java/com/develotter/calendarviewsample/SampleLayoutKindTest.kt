package com.develotter.calendarviewsample

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class SampleLayoutKindTest {
    @Test
    fun widthDecidesWithoutFold() {
        assertEquals(SampleLayoutKind.Stacked, sampleLayoutKind(839.dp, null))
        assertEquals(SampleLayoutKind.SideBySide, sampleLayoutKind(840.dp, null))
        assertEquals(SampleLayoutKind.Stacked, sampleLayoutKind(320.dp, null))
    }

    @Test
    fun separatingFoldOverridesWidthRule() {
        val vertical = SampleFold(vertical = true, start = 300.dp, size = 0.dp)
        val horizontal = SampleFold(vertical = false, start = 400.dp, size = 8.dp)
        assertEquals(SampleLayoutKind.SplitByVerticalFold, sampleLayoutKind(600.dp, vertical))
        assertEquals(SampleLayoutKind.SplitByVerticalFold, sampleLayoutKind(1200.dp, vertical))
        assertEquals(SampleLayoutKind.SplitByHorizontalFold, sampleLayoutKind(700.dp, horizontal))
    }
}
