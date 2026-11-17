@file:OptIn(ExperimentalWasmJsInterop::class)

package com.develotter.calendarview.calendars.gregorian

import kotlin.js.ExperimentalWasmJsInterop

internal actual fun webGregorianLocalYear(): Int = js("new Date().getFullYear()")

internal actual fun webGregorianLocalMonth(): Int = js("new Date().getMonth() + 1")

internal actual fun webGregorianLocalDay(): Int = js("new Date().getDate()")

internal actual fun webGregorianDefaultFirstDayOfWeek(): Int = js(
    "(function() { try { var tag = (typeof navigator !== 'undefined' && navigator.language) || new Intl.DateTimeFormat().resolvedOptions().locale || 'en-US'; var loc = new Intl.Locale(tag); var info = typeof loc.getWeekInfo === 'function' ? loc.getWeekInfo() : loc.weekInfo; var first = info && info.firstDay; return (first >= 1 && first <= 7) ? first : 1; } catch (e) { return 1; } })()",
)

internal actual fun webGregorianFormatMonthYear(epochMs: Double): String = js(
    "new Intl.DateTimeFormat(undefined, { calendar: 'gregory', month: 'long', year: 'numeric', timeZone: 'UTC' }).format(new Date(epochMs))",
)

internal actual fun webGregorianFormatFullDate(epochMs: Double): String = js(
    "new Intl.DateTimeFormat(undefined, { calendar: 'gregory', dateStyle: 'full', timeZone: 'UTC' }).format(new Date(epochMs))",
)

internal actual fun webGregorianFormatShortDayMonth(epochMs: Double): String = js(
    "new Intl.DateTimeFormat(undefined, { calendar: 'gregory', day: 'numeric', month: 'short', timeZone: 'UTC' }).format(new Date(epochMs))",
)

internal actual fun webGregorianFormatShortDayMonthYear(epochMs: Double): String = js(
    "new Intl.DateTimeFormat(undefined, { calendar: 'gregory', day: 'numeric', month: 'short', year: 'numeric', timeZone: 'UTC' }).format(new Date(epochMs))",
)

internal actual fun webGregorianFormatDayNumber(value: Int): String = js(
    "new Intl.NumberFormat(undefined, { useGrouping: false }).format(value)",
)

internal actual fun webGregorianFormatWeekdayShort(epochMs: Double): String = js(
    "new Intl.DateTimeFormat(undefined, { calendar: 'gregory', weekday: 'short', timeZone: 'UTC' }).format(new Date(epochMs))",
)
