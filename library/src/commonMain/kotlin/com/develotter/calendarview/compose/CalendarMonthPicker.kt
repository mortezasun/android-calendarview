package com.develotter.calendarview.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Stateless calendar month picker that renders one year of selectable month cells.
 */
@Composable
fun CalendarMonthPicker(
    yearRange: IntRange,
    selectedYear: Int,
    selectedMonth: Int,
    onMonthSelected: (year: Int, month: Int) -> Unit,
    modifier: Modifier = Modifier,
    onYearChange: ((Int) -> Unit)? = null,
    monthLabel: @Composable (Int) -> Unit = { Text(it.toString()) },
    yearLabel: @Composable (Int) -> Unit = { Text(it.toString(), style = MaterialTheme.typography.titleMedium) },
    previousYearContentDescription: String? = null,
    nextYearContentDescription: String? = null,
    previousYearIcon: @Composable () -> Unit = {
        val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        Text(
            if (rtl) "\u203A" else "\u2039",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.clearAndSetSemantics {},
        )
    },
    nextYearIcon: @Composable () -> Unit = {
        val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        Text(
            if (rtl) "\u2039" else "\u203A",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.clearAndSetSemantics {},
        )
    },
) {
    requireMonthPickerSelection(yearRange, selectedYear, selectedMonth)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(
                onClick = { onYearChange?.invoke(selectedYear - 1) },
                enabled = onYearChange != null && selectedYear > yearRange.first,
                modifier = if (previousYearContentDescription != null) {
                    Modifier.semantics { contentDescription = previousYearContentDescription }
                } else {
                    Modifier
                },
            ) {
                previousYearIcon()
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                yearLabel(selectedYear)
            }

            IconButton(
                onClick = { onYearChange?.invoke(selectedYear + 1) },
                enabled = onYearChange != null && selectedYear < yearRange.last,
                modifier = if (nextYearContentDescription != null) {
                    Modifier.semantics { contentDescription = nextYearContentDescription }
                } else {
                    Modifier
                },
            ) {
                nextYearIcon()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    collectionInfo = CollectionInfo(rowCount = 4, columnCount = 3)
                },
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (row in 0 until 4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for (col in 0 until 3) {
                        val month = row * 3 + col + 1
                        val isSelected = month == selectedMonth
                        val cellModifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .semantics {
                                collectionItemInfo = CollectionItemInfo(
                                    rowIndex = row,
                                    rowSpan = 1,
                                    columnIndex = col,
                                    columnSpan = 1,
                                )
                            }
                            .selectable(
                                selected = isSelected,
                                role = Role.RadioButton,
                                onClick = { onMonthSelected(selectedYear, month) },
                            )

                        Surface(
                            modifier = cellModifier,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            contentColor = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Box(
                                modifier = Modifier.heightIn(min = 48.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                monthLabel(month)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Validates the inputs to [CalendarMonthPicker].
 */
internal fun requireMonthPickerSelection(
    yearRange: IntRange,
    selectedYear: Int,
    selectedMonth: Int,
) {
    require(!yearRange.isEmpty()) { "yearRange must not be empty" }
    require(selectedYear in yearRange) { "selectedYear $selectedYear must be within $yearRange" }
    require(selectedMonth in 1..12) { "selectedMonth $selectedMonth must be in 1..12" }
}
