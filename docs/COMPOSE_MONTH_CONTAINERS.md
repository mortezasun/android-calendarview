# Compose month lists and pagers

The shared core provides CalendarMonthList and CalendarMonthPager. Both are calendar-neutral:
consumers map indices to months and provide rendering. No calendar-specific dependency is
introduced in the shared core by these containers. Both support horizontal and vertical axes.

## Gregorian pager example

This composable shows a fixed 25-month window with the middle month initially visible.
The initial anchor is remembered and saved, preventing rotation from shifting the window.

```kotlin
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.develotter.calendarview.calendars.gregorian.compose.GregorianCalendarMonth
import com.develotter.calendarview.compose.CalendarMonthPager
import com.develotter.calendarview.compose.rememberCalendarSelectionState
import java.time.YearMonth

@Composable
fun GregorianPagerExample() {
    val startText = rememberSaveable { YearMonth.now().minusMonths(12).toString() }
    val start = YearMonth.parse(startText)
    val selection = rememberCalendarSelectionState()
    val pager = rememberPagerState(initialPage = 12, pageCount = { 25 })
    CalendarMonthPager(
        monthCount = 25,
        state = pager,
        modifier = Modifier.height(440.dp),
        monthKey = { start.plusMonths(it.toLong()).toString() },
    ) { index ->
        GregorianCalendarMonth(
            month = start.plusMonths(index.toLong()),
            selection = selection.selection,
            onDateClick = { selection.onDayClick(it.toEpochDay()) },
        )
    }
}
```

Use rememberCoroutineScope and launch { pager.animateScrollToPage(targetIndex) } for
buttons, validating targetIndex against 0 until pager.pageCount. For lists, supply a
rememberLazyListState and use animateScrollToItem instead. First/last window navigation
is caller policy; these finite containers do not automatically append months.

## Contracts

- Positive monthCount; external PagerState.pageCount must match it.
- Vertical list: bounded height. Horizontal list: bounded width, explicit positive finite
  horizontalMonthWidth (default 320.dp). Pager: bounded width and height.
- Do not nest a vertical lazy list in the launcher's unbounded vertical scrolling Column.
  Integrating modes there requires a bounded viewport or restructuring the screen.
- Share selection outside monthContent so selections survive off-screen disposal.
- Stable, unique, saveable month keys identify items. Default keys are indices, suitable
  only for a fixed window. Re-key/reset state when the window's identity changes.
- List callback: first visible index. Pager callback: settled page. Initial callbacks fire.
- Rendering and navigation remain engine-owned; Hijri/Jalali consumers map indices using
  their calendar's month arithmetic, never by adding 30 days.
- These APIs do not add adjacent-month cells or replace legacy adapters automatically.

## Verification

Source-reviewed only. No compilation, Compose UI test, gesture, saved-state or RTL test has
been executed for these APIs. No new passing tests are claimed. The sample still uses its
single-month screen; the containers are library APIs and are not yet wired into its controls.
