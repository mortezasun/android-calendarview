@file:OptIn(ExperimentalWasmJsInterop::class)

package com.develotter.calendarview.calendars.hijri

import kotlin.js.ExperimentalWasmJsInterop

internal actual fun webHijriLocalYear(): Int = js("new Date().getFullYear()")

internal actual fun webHijriLocalMonth(): Int = js("new Date().getMonth() + 1")

internal actual fun webHijriLocalDay(): Int = js("new Date().getDate()")

internal actual fun webHijriPartsFromEpochMs(epochMs: Double): String = js(
    "(function(ms) { var parts = new Intl.DateTimeFormat('en-US', { calendar: 'islamic-umalqura', numberingSystem: 'latn', timeZone: 'UTC', year: 'numeric', month: 'numeric', day: 'numeric' }).formatToParts(new Date(ms)); var y = '', m = '', d = ''; for (var i = 0; i < parts.length; i++) { var p = parts[i]; if (p.type === 'year') y = p.value; else if (p.type === 'month') m = p.value; else if (p.type === 'day') d = p.value; } return y + '-' + m + '-' + d; })(epochMs)",
)

internal actual fun webHijriFormatMonthYear(epochMs: Double): String = js(
    "new Intl.DateTimeFormat(undefined, { calendar: 'islamic-umalqura', month: 'long', year: 'numeric', timeZone: 'UTC' }).format(new Date(epochMs))",
)

internal actual fun webHijriFormatFullDate(canonicalMs: Double, actualMs: Double): String = js(
    "(function(cMs, aMs) { var fmt = new Intl.DateTimeFormat(undefined, { calendar: 'islamic-umalqura', dateStyle: 'full', timeZone: 'UTC' }); if (cMs === aMs) return fmt.format(new Date(cMs)); var parts = fmt.formatToParts(new Date(cMs)); var actualParts = fmt.formatToParts(new Date(aMs)); var weekdayValue = null; for (var i = 0; i < actualParts.length; i++) { if (actualParts[i].type === 'weekday') { weekdayValue = actualParts[i].value; break; } } var out = ''; for (var j = 0; j < parts.length; j++) { out += (parts[j].type === 'weekday' && weekdayValue !== null) ? weekdayValue : parts[j].value; } return out; })(canonicalMs, actualMs)",
)

internal actual fun webHijriFormatShortDayMonthYear(epochMs: Double): String = js(
    "new Intl.DateTimeFormat(undefined, { calendar: 'islamic-umalqura', day: 'numeric', month: 'short', year: 'numeric', era: 'short', timeZone: 'UTC' }).format(new Date(epochMs))",
)

internal actual fun webHijriFormatDayNumber(value: Int): String = js(
    "new Intl.NumberFormat(undefined, { useGrouping: false }).format(value)",
)

internal actual fun webHijriFormatWeekdayShort(epochMs: Double): String = js(
    "new Intl.DateTimeFormat(undefined, { calendar: 'gregory', weekday: 'short', timeZone: 'UTC' }).format(new Date(epochMs))",
)
