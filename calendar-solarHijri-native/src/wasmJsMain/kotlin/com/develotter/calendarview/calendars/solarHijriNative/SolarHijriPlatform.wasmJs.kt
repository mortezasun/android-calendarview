@file:OptIn(ExperimentalWasmJsInterop::class)

package com.develotter.calendarview.calendars.solarHijriNative

import kotlin.js.ExperimentalWasmJsInterop

internal actual fun webSolarHijriLocalYear(): Int = js("new Date().getFullYear()")

internal actual fun webSolarHijriLocalMonth(): Int = js("new Date().getMonth() + 1")

internal actual fun webSolarHijriLocalDay(): Int = js("new Date().getDate()")

internal actual fun webSolarHijriLocaleLanguage(): String = js(
    "(function() { try { var tag = (typeof navigator !== 'undefined' && navigator.language) || new Intl.DateTimeFormat().resolvedOptions().locale || 'en'; return new Intl.Locale(tag).language || ''; } catch (e) { return 'en'; } })()",
)

internal actual fun webSolarHijriLocaleCountry(): String = js(
    "(function() { try { var tag = (typeof navigator !== 'undefined' && navigator.language) || new Intl.DateTimeFormat().resolvedOptions().locale || ''; return tag ? (new Intl.Locale(tag).region || '') : ''; } catch (e) { return ''; } })()",
)

internal actual fun webSolarHijriFormatDayNumber(value: Int): String = js(
    "new Intl.NumberFormat(undefined, { useGrouping: false }).format(value)",
)

internal actual fun webSolarHijriWeekdayShortLabel(epochMs: Double): String = js(
    "new Intl.DateTimeFormat(undefined, { calendar: 'gregory', weekday: 'short', timeZone: 'UTC' }).format(new Date(epochMs))",
)

internal actual fun webSolarHijriWeekdayFullLabel(epochMs: Double): String = js(
    "new Intl.DateTimeFormat(undefined, { calendar: 'gregory', weekday: 'long', timeZone: 'UTC' }).format(new Date(epochMs))",
)
