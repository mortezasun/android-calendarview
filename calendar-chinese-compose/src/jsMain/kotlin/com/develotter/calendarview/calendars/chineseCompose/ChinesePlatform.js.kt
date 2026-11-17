package com.develotter.calendarview.calendars.chineseCompose

internal actual fun webChineseLocalYear(): Int =
    js("new Date().getFullYear()").unsafeCast<Int>()

internal actual fun webChineseLocalMonth(): Int =
    js("new Date().getMonth() + 1").unsafeCast<Int>()

internal actual fun webChineseLocalDay(): Int =
    js("new Date().getDate()").unsafeCast<Int>()

private val jsChineseFirstDayFn: dynamic = js(
    "(function() { try { var defFmt = new Intl.DateTimeFormat(); var tag = (typeof navigator !== 'undefined' && navigator.language) || defFmt.resolvedOptions().locale || 'en-US'; var loc = new Intl.Locale(tag); var info = typeof loc.getWeekInfo === 'function' ? loc.getWeekInfo() : loc.weekInfo; var first = info && info.firstDay; return (first >= 1 && first <= 7) ? first : 1; } catch (e) { return 1; } })",
)

internal actual fun webChineseDefaultFirstDayOfWeek(): Int =
    jsChineseFirstDayFn().unsafeCast<Int>()

private val jsChineseMonthYearFn: dynamic = js(
    "(function(epochMs) { var fmt = new Intl.DateTimeFormat(undefined, { calendar: 'gregory', month: 'long', year: 'numeric', timeZone: 'UTC' }); return fmt.format(new Date(epochMs)); })",
)

internal actual fun webChineseFormatMonthYear(epochMs: Double): String =
    jsChineseMonthYearFn(epochMs).unsafeCast<String>()

private val jsChineseFullDateFn: dynamic = js(
    "(function(epochMs) { var fmt = new Intl.DateTimeFormat(undefined, { calendar: 'gregory', dateStyle: 'full', timeZone: 'UTC' }); return fmt.format(new Date(epochMs)); })",
)

internal actual fun webChineseFormatFullDate(epochMs: Double): String =
    jsChineseFullDateFn(epochMs).unsafeCast<String>()

private val jsChineseDayNumberFn: dynamic = js(
    "(function(value) { var fmt = new Intl.NumberFormat(undefined, { useGrouping: false }); return fmt.format(value); })",
)

internal actual fun webChineseFormatDayNumber(value: Int): String =
    jsChineseDayNumberFn(value).unsafeCast<String>()

private val jsChineseWeekdayShortFn: dynamic = js(
    "(function(epochMs) { var fmt = new Intl.DateTimeFormat(undefined, { calendar: 'gregory', weekday: 'short', timeZone: 'UTC' }); return fmt.format(new Date(epochMs)); })",
)

internal actual fun webChineseFormatWeekdayShort(epochMs: Double): String =
    jsChineseWeekdayShortFn(epochMs).unsafeCast<String>()
