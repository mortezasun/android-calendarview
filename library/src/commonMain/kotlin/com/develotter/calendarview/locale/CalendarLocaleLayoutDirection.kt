package com.develotter.calendarview.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/**
 * Opt-in helper that derives the ambient `LayoutDirection` from [locale] for [content]:
 * [CalendarLocale.isRightToLeft] selects `Rtl`, every other language selects `Ltr`.
 *
 * Calendar composables never call this themselves; wrap the calendar (or the whole screen) only
 * if the host wants the language to decide the layout direction. A `null` [locale] leaves the
 * ambient direction untouched, matching the `null` meaning of the `locale` parameters elsewhere.
 */
@Composable
fun ProvideCalendarLocaleLayoutDirection(
    locale: CalendarLocale?,
    content: @Composable () -> Unit,
) {
    if (locale == null) {
        content()
        return
    }
    val direction = if (locale.isRightToLeft) LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(LocalLayoutDirection provides direction, content = content)
}
