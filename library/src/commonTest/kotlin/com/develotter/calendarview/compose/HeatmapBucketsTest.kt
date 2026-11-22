package com.develotter.calendarview.compose

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class HeatmapBucketsTest {
    @Test
    fun defaultBucketsHasOneStep() {
        val buckets = HeatmapBuckets()
        assertEquals(1, buckets.bucketCount)
        assertEquals(0f, buckets.intensityForBucket(0))
        assertEquals(0, buckets.bucketOf(0.5f))
        assertEquals(0f, buckets.colorIntensityFor(0.5f))
    }

    @Test
    fun thresholdsDefineSteppedBuckets() {
        val buckets = HeatmapBuckets(listOf(0.25f, 0.5f, 0.75f))
        assertEquals(4, buckets.bucketCount)
        assertEquals(0f, buckets.intensityForBucket(0))
        assertEquals(0.25f, buckets.intensityForBucket(1))
        assertEquals(0.5f, buckets.intensityForBucket(2))
        assertEquals(0.75f, buckets.intensityForBucket(3))

        assertEquals(0, buckets.bucketOf(0f))
        assertEquals(0, buckets.bucketOf(0.25f)) // closed on upper edge
        assertEquals(1, buckets.bucketOf(0.26f))
        assertEquals(1, buckets.bucketOf(0.5f))
        assertEquals(2, buckets.bucketOf(0.75f))
        assertEquals(3, buckets.bucketOf(0.76f))
        assertEquals(3, buckets.bucketOf(1.0f))

        assertEquals(0f, buckets.colorIntensityFor(0.25f))
        assertEquals(0.25f, buckets.colorIntensityFor(0.5f))
        assertEquals(0.75f, buckets.colorIntensityFor(1.0f))
    }

    @Test
    fun rejectsNonAscendingOrOutOfBoundsThresholds() {
        assertFailsWith<IllegalArgumentException> {
            HeatmapBuckets(listOf(0.5f, 0.25f))
        }
        assertFailsWith<IllegalArgumentException> {
            HeatmapBuckets(listOf(-0.1f, 0.5f))
        }
        assertFailsWith<IllegalArgumentException> {
            HeatmapBuckets(listOf(0.5f, 1.1f))
        }
        assertFailsWith<IllegalArgumentException> {
            HeatmapBuckets(listOf(Float.NaN))
        }
    }
}
