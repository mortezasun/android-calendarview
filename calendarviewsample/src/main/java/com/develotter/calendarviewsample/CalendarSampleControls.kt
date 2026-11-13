package com.develotter.calendarviewsample

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.enums.TypeSelectDay

/** Demo-only configuration controls: calendar, mode, locale and display options. */
@Composable
internal fun CalendarSampleControls(state: CalendarSampleState) {
    val calendar = state.calendar
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SampleCalendar.entries.forEach { item ->
            FilterChip(selected = calendar == item, onClick = { state.selectCalendar(item) }, label = { Text(item.name) })
        }
    }
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TypeSelectDay.entries.forEach { mode ->
            FilterChip(selected = mode.name == state.modeName, onClick = { state.modeName = mode.name }, label = { Text(mode.name) })
        }
    }
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("en", "de", "fa", "ar").forEach { tag ->
            FilterChip(selected = state.localeTag == tag, onClick = { state.localeTag = tag }, label = { Text(tag) })
        }
        Text("RTL")
        Switch(checked = state.rtl, onCheckedChange = { state.rtl = it })
    }
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Previous month days")
        Switch(checked = state.previousDays, onCheckedChange = { state.previousDays = it })
        Text("Next month days")
        Switch(checked = state.nextDays, onCheckedChange = { state.nextDays = it })
        Text("Fixed 6 rows")
        Switch(checked = state.fixedSixRows, onCheckedChange = { state.fixedSixRows = it })
        Text("Week view")
        Switch(checked = state.showWeek, onCheckedChange = { state.showWeek = it })
    }
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Agenda")
        Switch(checked = state.showAgenda, onCheckedChange = { state.showAgenda = it })
        Text("Week numbers")
        Switch(checked = state.showWeekNumbers, onCheckedChange = { state.showWeekNumbers = it })
        Text("Custom style")
        Switch(checked = state.customStyle, onCheckedChange = { state.customStyle = it })
    }
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Library month header")
        Switch(checked = state.useHeaderApi, onCheckedChange = { state.useHeaderApi = it })
        Text("Range 2–14 days")
        Switch(checked = state.limitRange, onCheckedChange = { state.limitRange = it })
    }
    if (calendar == SampleCalendar.Chinese) {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Chinese labels")
            ChineseTextStyle.entries.forEach { style ->
                FilterChip(
                    selected = state.chineseStyle == style,
                    onClick = { state.chineseStyleName = style.name },
                    label = {
                        Text(
                            when (style) {
                                ChineseTextStyle.English -> "EN"
                                ChineseTextStyle.SimplifiedChinese -> "简体"
                                ChineseTextStyle.TraditionalChinese -> "繁體"
                                ChineseTextStyle.Numeric -> "1/2"
                            },
                        )
                    },
                )
            }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Gregorian grid + Chinese labels")
            Switch(checked = state.chineseGregorianGrid, onCheckedChange = { state.chineseGregorianGrid = it })
            Text("Details")
            Switch(checked = state.showChineseDetails, onCheckedChange = { state.showChineseDetails = it })
        }
    }
    if (calendar == SampleCalendar.Hijri) {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Hijri method")
            listOf(-1, 0, 1).forEach { offset ->
                FilterChip(
                    selected = state.hijriOffset == offset,
                    onClick = { state.hijriOffset = offset },
                    label = {
                        Text(
                            when (offset) {
                                -1 -> "−1 day"
                                0 -> "Umm al-Qura"
                                else -> "+1 day"
                            },
                        )
                    },
                )
            }
        }
    }
}
