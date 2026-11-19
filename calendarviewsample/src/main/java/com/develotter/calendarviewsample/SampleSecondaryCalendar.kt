package com.develotter.calendarviewsample

import com.develotter.calendarview.calendars.chineseCompose.chineseAccessibilityLabel
import com.develotter.calendarview.calendars.chineseCompose.chineseSecondaryLabel
import com.develotter.calendarview.calendars.chineseNative.ChineseDate
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.calendars.gregorian.compose.gregorianAccessibilityLabel
import com.develotter.calendarview.calendars.gregorian.compose.gregorianSecondaryLabel
import com.develotter.calendarview.calendars.hijri.HijriDateProvider
import com.develotter.calendarview.calendars.hijri.UmmAlQuraHijriDateProvider
import com.develotter.calendarview.calendars.hijri.compose.hijriAccessibilityLabel
import com.develotter.calendarview.calendars.hijri.compose.hijriSecondaryLabel
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriAccessibilityLabel
import com.develotter.calendarview.calendars.solarHijri.compose.solarHijriSecondaryLabel
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.compose.solarHijriNativeAccessibilityLabel
import com.develotter.calendarview.calendars.solarHijriNative.compose.solarHijriNativeSecondaryLabel
import com.develotter.calendarview.locale.asCalendarLocale
import java.util.Locale

/**
 * Dynamic alternative calendar choice for secondary cell labels and accessibility descriptions.
 * Each option delegates directly to the corresponding calendar module helper without sample-only tables.
 */
internal enum class SampleSecondaryCalendar(val label: String) {
    None("None"),
    Gregorian("Gregorian"),
    Hijri("Hijri"),
    SolarHijri("SolarHijri"),
    SolarHijriNative("SolarHijriNative"),
    Chinese("Chinese"),
}

/** Formatted date representation of one epoch day in one calendar module. */
internal data class SampleCalendarConversion(
    val calendar: SampleCalendar,
    val title: String,
    val formattedDate: String,
    val detail: String? = null,
)

/** Static showcase metadata describing each published calendar module on Home and Calendars. */
internal data class SampleCalendarInfo(
    val calendar: SampleCalendar,
    val title: String,
    val artifact: String,
    val summary: String,
    val highlights: List<String>,
)

internal val SampleCalendarCatalog: List<SampleCalendarInfo> = listOf(
    SampleCalendarInfo(
        calendar = SampleCalendar.Gregorian,
        title = "Gregorian",
        artifact = ":calendar-gregorian",
        summary = "ISO-based Gregorian calendar with locale week-start, adjacent days, and ISO week numbers.",
        highlights = listOf(
            "Locale-aware month titles, weekday headers, and digit formatting",
            "Optional previous/next month cells and fixed 6-row layout",
            "Pair dynamically with any secondary calendar label",
        ),
    ),
    SampleCalendarInfo(
        calendar = SampleCalendar.Hijri,
        title = "Hijri",
        artifact = ":calendar-hijri",
        summary = "Umm al-Qura chronology with an engine-local HijriDateProvider hook for −1 / 0 / +1 day offsets.",
        highlights = listOf(
            "Saturday-first default or custom first day of week",
            "Caller-supplied OffsetHijriDateProvider forwarded cleanly through grid, week, and header state",
            "Range-safe secondary label and accessibility helpers",
        ),
    ),
    SampleCalendarInfo(
        calendar = SampleCalendar.SolarHijriNative,
        title = "Solar Hijri (Native)",
        artifact = ":calendar-solarHijri-native",
        summary = "Pure KMP Borkowski arithmetic for Iranian/Afghan Solar Hijri years with Persian, Dari, and English scripts.",
        highlights = listOf(
            "Self-written astronomical leap-year algorithm (1200–1600 SH)",
            "Zero Java/ThreeTen dependency across Android, JVM, Apple, JS, and Wasm",
            "Built-in month names, full-date descriptions, and secondary labels",
        ),
    ),
    SampleCalendarInfo(
        calendar = SampleCalendar.SolarHijri,
        title = "Solar Hijri (Bridge)",
        artifact = ":calendar-solarHijri",
        summary = "Compatibility facade delegating to SolarHijriNative with legacy helper entry points and week snapshots.",
        highlights = listOf(
            "Drop-in Compose compatibility layer built on SolarHijriNative",
            "Full secondary-label, dayContent, week grid, and agenda support",
            "Mirrors pure KMP conversion helpers cleanly",
        ),
    ),
    SampleCalendarInfo(
        calendar = SampleCalendar.Chinese,
        title = "Chinese Lunar",
        artifact = ":calendar-chinese-compose",
        summary = "Pure KMP lunisolar engine (1901–2100) with explicit leap-month identity and 4 display scripts.",
        highlights = listOf(
            "Supports leap months (e.g. 2023 Leap 2nd Month) across grids, weeks, and heatmaps",
            "English, Simplified, Traditional, and Numeric text styles",
            "Both standalone lunar grids and Gregorian + secondary-calendar pairing",
        ),
    ),
)

internal fun sampleSecondaryLabelProvider(
    secondary: SampleSecondaryCalendar,
    hijriDateProvider: HijriDateProvider = UmmAlQuraHijriDateProvider,
    chineseStyle: ChineseTextStyle = ChineseTextStyle.English,
    locale: Locale = Locale.getDefault(),
): ((Long) -> String?)? {
    val calendarLocale = locale.asCalendarLocale()
    return when (secondary) {
        SampleSecondaryCalendar.None -> null
        SampleSecondaryCalendar.Gregorian -> { epochDay -> gregorianSecondaryLabel(epochDay, calendarLocale) }
        SampleSecondaryCalendar.Hijri -> { epochDay -> hijriSecondaryLabel(epochDay, hijriDateProvider, calendarLocale) }
        SampleSecondaryCalendar.SolarHijri -> { epochDay -> solarHijriSecondaryLabel(epochDay, calendarLocale) }
        SampleSecondaryCalendar.SolarHijriNative -> { epochDay -> solarHijriNativeSecondaryLabel(epochDay, calendarLocale) }
        SampleSecondaryCalendar.Chinese -> { epochDay -> chineseSecondaryLabel(epochDay, chineseStyle) }
    }
}

internal fun sampleSecondaryAccessibilityProvider(
    secondary: SampleSecondaryCalendar,
    hijriDateProvider: HijriDateProvider = UmmAlQuraHijriDateProvider,
    chineseStyle: ChineseTextStyle = ChineseTextStyle.English,
    locale: Locale = Locale.getDefault(),
): ((Long) -> String?)? {
    val calendarLocale = locale.asCalendarLocale()
    return when (secondary) {
        SampleSecondaryCalendar.None -> null
        SampleSecondaryCalendar.Gregorian -> { epochDay -> gregorianAccessibilityLabel(epochDay, calendarLocale) }
        SampleSecondaryCalendar.Hijri -> { epochDay -> hijriAccessibilityLabel(epochDay, hijriDateProvider, calendarLocale) }
        SampleSecondaryCalendar.SolarHijri -> { epochDay -> solarHijriAccessibilityLabel(epochDay, calendarLocale) }
        SampleSecondaryCalendar.SolarHijriNative -> { epochDay -> solarHijriNativeAccessibilityLabel(epochDay, calendarLocale) }
        SampleSecondaryCalendar.Chinese -> { epochDay -> chineseAccessibilityLabel(epochDay, chineseStyle) }
    }
}

/** Converts a single absolute epoch day into readable representations across all engines. */
internal fun sampleAllCalendarConversions(
    epochDay: Long,
    hijriDateProvider: HijriDateProvider = UmmAlQuraHijriDateProvider,
    chineseStyle: ChineseTextStyle = ChineseTextStyle.English,
    locale: Locale = Locale.getDefault(),
): List<SampleCalendarConversion> {
    val calendarLocale = locale.asCalendarLocale()
    val gregorianText = gregorianAccessibilityLabel(epochDay, calendarLocale)
    val hijriText = hijriAccessibilityLabel(epochDay, hijriDateProvider, calendarLocale)
        ?: "Outside supported range"
    val solarNativeText = solarHijriNativeAccessibilityLabel(epochDay, calendarLocale)
    val solarLeapDetail = runCatching {
        val date = SolarHijriDate.fromEpochDay(epochDay)
        if (date.isLeapYear()) "Leap year (${date.year} SH)" else "Common year (${date.year} SH)"
    }.getOrNull()
    val chineseText = chineseAccessibilityLabel(epochDay, chineseStyle) ?: "Outside supported range (1901–2100)"
    val chineseDetail = runCatching {
        val date = ChineseDate.fromEpochDay(epochDay)
        if (date.isLeapMonth) {
            "Leap month ${date.monthNumber} · year ${date.year}"
        } else {
            "Year ${date.year}, month ${date.monthNumber}, day ${date.dayOfMonth}"
        }
    }.getOrNull()

    return listOf(
        SampleCalendarConversion(
            calendar = SampleCalendar.Gregorian,
            title = "Gregorian",
            formattedDate = gregorianText,
        ),
        SampleCalendarConversion(
            calendar = SampleCalendar.Hijri,
            title = "Hijri",
            formattedDate = hijriText,
        ),
        SampleCalendarConversion(
            calendar = SampleCalendar.SolarHijriNative,
            title = "Solar Hijri (Native)",
            formattedDate = solarNativeText ?: "Outside supported range (1200–1600 SH)",
            detail = solarLeapDetail,
        ),
        SampleCalendarConversion(
            calendar = SampleCalendar.Chinese,
            title = "Chinese Lunar",
            formattedDate = chineseText,
            detail = chineseDetail,
        ),
    )
}
