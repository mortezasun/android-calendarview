package com.develotter.calendarview.locale

internal actual fun platformLanguageTag(): String = java.util.Locale.getDefault().toLanguageTag()
