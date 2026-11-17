package com.develotter.calendarview.calendars.gregorian

internal actual fun webGregorianLocalYear(): Int =
    js("new Date().getFullYear()").unsafeCast<Int>()

internal actual fun webGregorianLocalMonth(): Int =
    js("new Date().getMonth() + 1").unsafeCast<Int>()

internal actual fun webGregorianLocalDay(): Int =
    js("new Date().getDate()").unsafeCast<Int>()

private val jsGregorianFirstDayFn: dynamic = js(
    "(function() { try { var defFmt = new Intl.DateTimeFormat(); var tag = (typeof navigator !== 'undefined' && navigator.language) || defFmt.resolvedOptions().locale || 'en-US'; var loc = new Intl.Locale(tag); var info = typeof loc.getWeekInfo === 'function' ? loc.getWeekInfo() : loc.weekInfo; var first = info && info.firstDay; return (first >= 1 && first <= 7) ? first : 1; } catch (e) { return 1; } })",
)

internal actual fun webGregorianDefaultFirstDayOfWeek(): Int =
    jsGregorianFirstDayFn().unsafeCast<Int>()

private val jsGregorianMonthYearFn: dynamic = js(
    "(function(epochMs) { var fmt = new Intl.DateTimeFormat(undefined, { calendar: 'gregory', month: 'long', year: 'numeric', timeZone: 'UTC' }); return fmt.format(new Date(epochMs)); })",
)

internal actual fun webGregorianFormatMonthYear(epochMs: Double): String =
    jsGregorianMonthYearFn(epochMs).unsafeCast<String>()

private val jsGregorianFullDateFn: dynamic = js(
    "(function(epochMs) { var fmt = new Intl.DateTimeFormat(undefined, { calendar: 'gregory', dateStyle: 'full', timeZone: 'UTC' }); return fmt.format(new Date(epochMs)); })",
)

internal actual fun webGregorianFormatFullDate(epochMs: Double): String =
    jsGregorianFullDateFn(epochMs).unsafeCast<String>()

private val jsGregorianShortDayMonthFn: dynamic = js(
    "(function(epochMs) { var fmt = new Intl.DateTimeFormat(undefined, { calendar: 'gregory', day: 'numeric', month: 'short', timeZone: 'UTC' }); return fmt.format(new Date(epochMs)); })",
)

internal actual fun webGregorianFormatShortDayMonth(epochMs: Double): String =
    jsGregorianShortDayMonthFn(epochMs).unsafeCast<String>()

private val jsGregorianShortDayMonthYearFn: dynamic = js(
    "(function(epochMs) { var fmt = new Intl.DateTimeFormat(undefined, { calendar: 'gregory', day: 'numeric', month: 'short', year: 'numeric', timeZone: 'UTC' }); return fmt.format(new Date(epochMs)); })",
)

internal actual fun webGregorianFormatShortDayMonthYear(epochMs: Double): String =
    jsGregorianShortDayMonthYearFn(epochMs).unsafeCast<String>()

private val jsGregorianDayNumberFn: dynamic = js(
    "(function(value) { var fmt = new Intl.NumberFormat(undefined, { useGrouping: false }); return fmt.format(value); })",
)

internal actual fun webGregorianFormatDayNumber(value: Int): String =
    jsGregorianDayNumberFn(value).unsafeCast<String>()

private val jsGregorianWeekdayShortFn: dynamic = js(
    "(function(epochMs) { var fmt = new Intl.DateTimeFormat(undefined, { calendar: 'gregory', weekday: 'short', timeZone: 'UTC' }); return fmt.format(new Date(epochMs)); })",
)

internal actual fun webGregorianFormatWeekdayShort(epochMs: Double): String =
    jsGregorianWeekdayShortFn(epochMs).unsafeCast<String>()
