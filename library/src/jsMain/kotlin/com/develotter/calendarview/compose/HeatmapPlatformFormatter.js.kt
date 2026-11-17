package com.develotter.calendarview.compose

internal actual fun webHeatmapLocalYear(): Int =
    js("new Date().getFullYear()").unsafeCast<Int>()

internal actual fun webHeatmapLocalMonth(): Int =
    js("new Date().getMonth() + 1").unsafeCast<Int>()

internal actual fun webHeatmapLocalDay(): Int =
    js("new Date().getDate()").unsafeCast<Int>()

private val jsFormatMonthYearFn: dynamic = js(
    "(function(epochMs) { var fmt = new Intl.DateTimeFormat(undefined, { calendar: 'gregory', month: 'long', year: 'numeric', timeZone: 'UTC' }); return fmt.format(new Date(epochMs)); })",
)

internal actual fun webHeatmapFormatMonthYear(epochMs: Double): String =
    jsFormatMonthYearFn(epochMs).unsafeCast<String>()

private val jsFormatFullDateFn: dynamic = js(
    "(function(epochMs) { var fmt = new Intl.DateTimeFormat(undefined, { calendar: 'gregory', dateStyle: 'full', timeZone: 'UTC' }); return fmt.format(new Date(epochMs)); })",
)

internal actual fun webHeatmapFormatFullDate(epochMs: Double): String =
    jsFormatFullDateFn(epochMs).unsafeCast<String>()

private val jsFormatWeekdayShortFn: dynamic = js(
    "(function(epochMs) { var fmt = new Intl.DateTimeFormat(undefined, { calendar: 'gregory', weekday: 'short', timeZone: 'UTC' }); return fmt.format(new Date(epochMs)); })",
)

internal actual fun webHeatmapFormatWeekdayShort(epochMs: Double): String =
    jsFormatWeekdayShortFn(epochMs).unsafeCast<String>()

private val jsFormatPercentFn: dynamic = js(
    "(function(value) { var fmt = new Intl.NumberFormat(undefined, { style: 'percent' }); return fmt.format(value); })",
)

internal actual fun webHeatmapFormatPercent(value: Double): String =
    jsFormatPercentFn(value).unsafeCast<String>()
