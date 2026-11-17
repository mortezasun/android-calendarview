package com.develotter.calendarview.calendars.solarHijriNative

internal actual fun webSolarHijriLocalYear(): Int =
    js("new Date().getFullYear()").unsafeCast<Int>()

internal actual fun webSolarHijriLocalMonth(): Int =
    js("new Date().getMonth() + 1").unsafeCast<Int>()

internal actual fun webSolarHijriLocalDay(): Int =
    js("new Date().getDate()").unsafeCast<Int>()

private val jsSolarHijriLangFn: dynamic = js(
    "(function() { try { var defFmt = new Intl.DateTimeFormat(); var tag = (typeof navigator !== 'undefined' && navigator.language) || defFmt.resolvedOptions().locale || 'en'; var loc = new Intl.Locale(tag); return loc.language || ''; } catch (e) { return 'en'; } })",
)

internal actual fun webSolarHijriLocaleLanguage(): String =
    jsSolarHijriLangFn().unsafeCast<String>()

private val jsSolarHijriCountryFn: dynamic = js(
    "(function() { try { var defFmt = new Intl.DateTimeFormat(); var tag = (typeof navigator !== 'undefined' && navigator.language) || defFmt.resolvedOptions().locale || ''; if (!tag) return ''; var loc = new Intl.Locale(tag); return loc.region || ''; } catch (e) { return ''; } })",
)

internal actual fun webSolarHijriLocaleCountry(): String =
    jsSolarHijriCountryFn().unsafeCast<String>()

private val jsSolarHijriDayNumberFn: dynamic = js(
    "(function(value) { var fmt = new Intl.NumberFormat(undefined, { useGrouping: false }); return fmt.format(value); })",
)

internal actual fun webSolarHijriFormatDayNumber(value: Int): String =
    jsSolarHijriDayNumberFn(value).unsafeCast<String>()

private val jsSolarHijriWeekdayShortFn: dynamic = js(
    "(function(epochMs) { var fmt = new Intl.DateTimeFormat(undefined, { calendar: 'gregory', weekday: 'short', timeZone: 'UTC' }); return fmt.format(new Date(epochMs)); })",
)

internal actual fun webSolarHijriWeekdayShortLabel(epochMs: Double): String =
    jsSolarHijriWeekdayShortFn(epochMs).unsafeCast<String>()

private val jsSolarHijriWeekdayFullFn: dynamic = js(
    "(function(epochMs) { var fmt = new Intl.DateTimeFormat(undefined, { calendar: 'gregory', weekday: 'long', timeZone: 'UTC' }); return fmt.format(new Date(epochMs)); })",
)

internal actual fun webSolarHijriWeekdayFullLabel(epochMs: Double): String =
    jsSolarHijriWeekdayFullFn(epochMs).unsafeCast<String>()
