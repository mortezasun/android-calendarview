package com.develotter.calendarview.calendars.hijri

internal actual fun webHijriLocalYear(): Int =
    js("new Date().getFullYear()").unsafeCast<Int>()

internal actual fun webHijriLocalMonth(): Int =
    js("new Date().getMonth() + 1").unsafeCast<Int>()

internal actual fun webHijriLocalDay(): Int =
    js("new Date().getDate()").unsafeCast<Int>()

private val jsHijriPartsFn: dynamic = js(
    "(function(epochMs) { var fmt = new Intl.DateTimeFormat('en-US', { calendar: 'islamic-umalqura', numberingSystem: 'latn', timeZone: 'UTC', year: 'numeric', month: 'numeric', day: 'numeric' }); var parts = fmt.formatToParts(new Date(epochMs)); var y = '', m = '', d = ''; for (var i = 0; i < parts.length; i++) { var p = parts[i]; if (p.type === 'year') y = p.value; else if (p.type === 'month') m = p.value; else if (p.type === 'day') d = p.value; } return y + '-' + m + '-' + d; })",
)

internal actual fun webHijriPartsFromEpochMs(epochMs: Double): String =
    jsHijriPartsFn(epochMs).unsafeCast<String>()

private val jsHijriMonthYearFn: dynamic = js(
    "(function(epochMs) { var fmt = new Intl.DateTimeFormat(undefined, { calendar: 'islamic-umalqura', month: 'long', year: 'numeric', timeZone: 'UTC' }); return fmt.format(new Date(epochMs)); })",
)

internal actual fun webHijriFormatMonthYear(epochMs: Double): String =
    jsHijriMonthYearFn(epochMs).unsafeCast<String>()

private val jsHijriFullDateFn: dynamic = js(
    "(function(canonicalMs, actualMs) { var fmt = new Intl.DateTimeFormat(undefined, { calendar: 'islamic-umalqura', dateStyle: 'full', timeZone: 'UTC' }); if (canonicalMs === actualMs) return fmt.format(new Date(canonicalMs)); var parts = fmt.formatToParts(new Date(canonicalMs)); var actualParts = fmt.formatToParts(new Date(actualMs)); var weekdayValue = null; for (var i = 0; i < actualParts.length; i++) { if (actualParts[i].type === 'weekday') { weekdayValue = actualParts[i].value; break; } } var out = ''; for (var j = 0; j < parts.length; j++) { out += (parts[j].type === 'weekday' && weekdayValue !== null) ? weekdayValue : parts[j].value; } return out; })",
)

internal actual fun webHijriFormatFullDate(canonicalMs: Double, actualMs: Double): String =
    jsHijriFullDateFn(canonicalMs, actualMs).unsafeCast<String>()

private val jsHijriShortDateFn: dynamic = js(
    "(function(epochMs) { var fmt = new Intl.DateTimeFormat(undefined, { calendar: 'islamic-umalqura', day: 'numeric', month: 'short', year: 'numeric', era: 'short', timeZone: 'UTC' }); return fmt.format(new Date(epochMs)); })",
)

internal actual fun webHijriFormatShortDayMonthYear(epochMs: Double): String =
    jsHijriShortDateFn(epochMs).unsafeCast<String>()

private val jsHijriDayNumberFn: dynamic = js(
    "(function(value) { var fmt = new Intl.NumberFormat(undefined, { useGrouping: false }); return fmt.format(value); })",
)

internal actual fun webHijriFormatDayNumber(value: Int): String =
    jsHijriDayNumberFn(value).unsafeCast<String>()

private val jsHijriWeekdayShortFn: dynamic = js(
    "(function(epochMs) { var fmt = new Intl.DateTimeFormat(undefined, { calendar: 'gregory', weekday: 'short', timeZone: 'UTC' }); return fmt.format(new Date(epochMs)); })",
)

internal actual fun webHijriFormatWeekdayShort(epochMs: Double): String =
    jsHijriWeekdayShortFn(epochMs).unsafeCast<String>()
