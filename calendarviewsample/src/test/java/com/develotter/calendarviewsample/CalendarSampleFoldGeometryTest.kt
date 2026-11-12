package com.develotter.calendarviewsample

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalendarSampleFoldGeometryTest {
    @Test
    fun verticalFoldSubtractsPhysicalLeftOrigin() {
        assertEquals(
            SampleFold(true, 276.dp, 20.dp),
            sampleLocalFold(SampleFold(true, 300.dp, 20.dp), 24.dp, 900.dp),
        )
    }

    @Test
    fun horizontalFoldSubtractsTopInsetAndHostOffset() {
        assertEquals(
            SampleFold(false, 344.dp, 24.dp),
            sampleLocalFold(SampleFold(false, 400.dp, 24.dp), 56.dp, 700.dp),
        )
    }

    @Test
    fun zeroWidthFoldGetsCenteredMinimumGap() {
        assertEquals(
            SampleFold(true, 292.dp, 16.dp),
            sampleLocalFold(SampleFold(true, 300.dp, 0.dp), 0.dp, 800.dp),
        )
    }

    @Test
    fun foldIntersectingContentEdgeIsClippedNotShifted() {
        assertEquals(
            SampleFold(false, 0.dp, 10.dp),
            sampleLocalFold(SampleFold(false, 40.dp, 20.dp), 50.dp, 200.dp),
        )
        assertEquals(
            SampleFold(true, 190.dp, 10.dp),
            sampleLocalFold(SampleFold(true, 190.dp, 30.dp), 0.dp, 200.dp),
        )
    }

    @Test
    fun outsideAndBoundaryFoldsDoNotSplitContent() {
        assertNull(sampleLocalFold(SampleFold(true, 0.dp, 20.dp), 24.dp, 800.dp))
        assertNull(sampleLocalFold(SampleFold(true, 900.dp, 20.dp), 24.dp, 800.dp))
        assertNull(sampleLocalFold(SampleFold(false, 0.dp, 0.dp), 0.dp, 800.dp))
        assertNull(sampleLocalFold(SampleFold(false, 800.dp, 0.dp), 0.dp, 800.dp))
        assertNull(sampleLocalFold(SampleFold(true, 100.dp, 20.dp), 0.dp, 0.dp))
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeExtentIsRejected() {
        sampleLocalFold(SampleFold(true, 100.dp, 20.dp), 0.dp, (-1).dp)
    }
}
