@file:OptIn(ExperimentalWasmJsInterop::class)

package com.develotter.calendarview.calendars.chineseCompose

import kotlin.js.ExperimentalWasmJsInterop

internal actual fun webChineseLocalYear(): Int = js("new Date().getFullYear()")

internal actual fun webChineseLocalMonth(): Int = js("new Date().getMonth() + 1")

internal actual fun webChineseLocalDay(): Int = js("new Date().getDate()")

internal actual fun webChineseDefaultFirstDayOfWeek(): Int = js(
    "(function() { try { var tag = (typeof navigator !== 'undefined' && navigator.language) || new Intl.DateTimeFormat().resolvedOptions().locale || 'en-US'; var loc = new Intl.Locale(tag); var info = typeof loc.getWeekInfo === 'function' ? loc.getWeekInfo() : loc.weekInfo; var first = info && info.firstDay; return (first >= 1 && first <= 7) ? first : 1; } catch (e) { return 1; } })()",
)

internal actual fun webChineseFormatMonthYear(epochMs: Double): String = js(
    "new Intl.DateTimeFormat(undefined, { calendar: 'gregory', month: 'long', year: 'numeric', timeZone: 'UTC' }).format(new Date(epochMs))",
)

internal actual fun webChineseFormatFullDate(epochMs: Double): String = js(
    "new Intl.DateTimeFormat(undefined, { calendar: 'gregory', dateStyle: 'full', timeZone: 'UTC' }).format(new Date(epochMs))",
)

internal actual fun webChineseFormatDayNumber(value: Int): String = js(
    "new Intl.NumberFormat(undefined, { useGrouping: false }).format(value)",
)

internal actual fun webChineseFormatWeekdayShort(epochMs: Double): String = js(
    "new Intl.DateTimeFormat(undefined, { calendar: 'gregory', weekday: 'short', timeZone: 'UTC' }).format(new Date(epochMs))",
)
