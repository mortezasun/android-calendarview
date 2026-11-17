package com.develotter.calendarviewsample

import com.develotter.calendarview.calendars.chineseNative.ChineseCalendarEngine
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.calendars.hijri.OffsetHijriDateProvider
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SampleSecondaryCalendarTest {
    @Test
    fun catalogCoversEverySampleCalendarOnce() {
        assertEquals(SampleCalendar.entries.toSet(), SampleCalendarCatalog.map { it.calendar }.toSet())
        SampleCalendarCatalog.forEach { info ->
            assertTrue(info.artifact.startsWith(":calendar-"))
            assertTrue(info.highlights.isNotEmpty())
        }
    }

    @Test
    fun noneProviderReturnsNullAndActiveProvidersResolveAllSupportedCalendars() {
        assertNull(sampleSecondaryLabelProvider(SampleSecondaryCalendar.None))
        assertNull(sampleSecondaryAccessibilityProvider(SampleSecondaryCalendar.None))

        val epochDay = LocalDate.of(2026, 6, 15).toEpochDay()
        SampleSecondaryCalendar.entries
            .filter { it != SampleSecondaryCalendar.None }
            .forEach { secondary ->
                val label = sampleSecondaryLabelProvider(secondary)?.invoke(epochDay)
                val accessibility = sampleSecondaryAccessibilityProvider(secondary)?.invoke(epochDay)
                assertFalse("Empty label for $secondary", label.isNullOrBlank())
                assertFalse("Empty accessibility label for $secondary", accessibility.isNullOrBlank())
            }
    }

    @Test
    fun providersReturnNullSafelyOutsideLimitedEngineRanges() {
        val outOfRangeChinese = ChineseCalendarEngine.MIN_EPOCH_DAY - 10L
        assertNull(sampleSecondaryLabelProvider(SampleSecondaryCalendar.Chinese)?.invoke(outOfRangeChinese))
        assertNull(sampleSecondaryAccessibilityProvider(SampleSecondaryCalendar.Chinese)?.invoke(outOfRangeChinese))

        val outOfRangeSolar = SolarHijriEngine.MIN_EPOCH_DAY - 10L
        assertNull(sampleSecondaryLabelProvider(SampleSecondaryCalendar.SolarHijriNative)?.invoke(outOfRangeSolar))
        assertNull(sampleSecondaryAccessibilityProvider(SampleSecondaryCalendar.SolarHijriNative)?.invoke(outOfRangeSolar))
    }

    @Test
    fun hijriOffsetProviderAndChineseStylePropagateIntoSecondaryHelpers() {
        val epochDay = LocalDate.of(2026, 6, 15).toEpochDay()
        val defaultHijri = sampleSecondaryAccessibilityProvider(SampleSecondaryCalendar.Hijri)?.invoke(epochDay)
        val shiftedHijri = sampleSecondaryAccessibilityProvider(
            secondary = SampleSecondaryCalendar.Hijri,
            hijriDateProvider = OffsetHijriDateProvider(1),
        )?.invoke(epochDay)
        assertNotNull(defaultHijri)
        assertNotNull(shiftedHijri)
        assertNotEquals(defaultHijri, shiftedHijri)

        val englishChinese = sampleSecondaryLabelProvider(
            secondary = SampleSecondaryCalendar.Chinese,
            chineseStyle = ChineseTextStyle.English,
        )?.invoke(epochDay)
        val numericChinese = sampleSecondaryLabelProvider(
            secondary = SampleSecondaryCalendar.Chinese,
            chineseStyle = ChineseTextStyle.Numeric,
        )?.invoke(epochDay)
        assertNotNull(englishChinese)
        assertNotNull(numericChinese)
    }

    @Test
    fun crossCalendarConversionsCoverAllFourCalendarSystems() {
        val epochDay = LocalDate.of(2026, 6, 15).toEpochDay()
        val conversions = sampleAllCalendarConversions(epochDay)
        assertEquals(4, conversions.size)
        conversions.forEach { entry ->
            assertFalse(entry.formattedDate.isBlank())
        }
    }
}
