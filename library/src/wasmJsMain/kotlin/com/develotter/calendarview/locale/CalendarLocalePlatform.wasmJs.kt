@file:OptIn(ExperimentalWasmJsInterop::class)

package com.develotter.calendarview.locale

import kotlin.js.ExperimentalWasmJsInterop

internal actual fun platformLanguageTag(): String =
    js("Intl.DateTimeFormat().resolvedOptions().locale")
