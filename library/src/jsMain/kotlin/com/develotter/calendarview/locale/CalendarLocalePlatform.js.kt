package com.develotter.calendarview.locale

internal actual fun platformLanguageTag(): String =
    js("Intl.DateTimeFormat().resolvedOptions().locale").unsafeCast<String>()
