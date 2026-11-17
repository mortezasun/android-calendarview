@file:OptIn(ExperimentalWasmJsInterop::class)

package com.develotter.calendarview.compose

import kotlin.js.ExperimentalWasmJsInterop

internal actual fun webHeatmapLocalYear(): Int = js("new Date().getFullYear()")

internal actual fun webHeatmapLocalMonth(): Int = js("new Date().getMonth() + 1")

internal actual fun webHeatmapLocalDay(): Int = js("new Date().getDate()")

internal actual fun webHeatmapFormatMonthYear(epochMs: Double): String = js(
    "new Intl.DateTimeFormat(undefined, { calendar: 'gregory', month: 'long', year: 'numeric', timeZone: 'UTC' }).format(new Date(epochMs))",
)

internal actual fun webHeatmapFormatFullDate(epochMs: Double): String = js(
    "new Intl.DateTimeFormat(undefined, { calendar: 'gregory', dateStyle: 'full', timeZone: 'UTC' }).format(new Date(epochMs))",
)

internal actual fun webHeatmapFormatWeekdayShort(epochMs: Double): String = js(
    "new Intl.DateTimeFormat(undefined, { calendar: 'gregory', weekday: 'short', timeZone: 'UTC' }).format(new Date(epochMs))",
)

internal actual fun webHeatmapFormatPercent(value: Double): String = js(
    "new Intl.NumberFormat(undefined, { style: 'percent' }).format(value)",
)
