package com.develotter.calendarview.locale

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.localeIdentifier

internal actual fun platformLanguageTag(): String =
    NSLocale.currentLocale.localeIdentifier.replace('_', '-')
