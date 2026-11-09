# Compose month lists and pagers

CalendarMonthList and CalendarMonthPager in the shared core accept monthCount, native
LazyListState/PagerState, a monthKey function, an active-month callback and composable content
by index. Both support vertical/horizontal axes. They do not know concrete calendar systems.

## Sample integration

LauncherActivity now embeds CalendarSampleViewport with five modes: Single, VerticalList,
HorizontalList, HorizontalPager and VerticalPager. Every mode shares the outer selection.
A fixed 25-month window is built using moveSampleMonth(calendar, anchor, index - 12), so
month index 12 contains the anchor and each engine uses its own calendar arithmetic.

The viewport is 480dp high with bounded width. Horizontal list items are 320dp wide. Pager
content and single/horizontal-list content have vertical overflow scrolling. The vertical
lazy list is explicitly bounded before being placed inside the outer scrolling Column.
Nested scrolling, font scaling and gestures still require actual device verification.

Previous/Today/Next above the viewport recenter the entire window on the chosen anchor;
they do not advance from the month most recently reached by swiping. Swipes scroll inside
the fixed window. Lists share one saved list position across axes; pagers share a separate
saved pager position. Switching between list and pager can therefore show different indices.
Both initialize at 12. Calendar or anchor changes re-key/reset these positions.

The active-index label shows first-visible list index or settled pager page. The calendar
dialog intentionally shows the anchor month, not the swiped page. It edits shared selection.
If any month in the full window exceeds an engine's range, the viewport displays an error
instead of constructing a partial window. No infinite loading is implemented.

## Library contracts

- monthCount must be positive; external PagerState.pageCount must match.
- Vertical list requires bounded height, horizontal list bounded width. Pager needs both.
- Keys must be unique and saveable. Defaults use indices and suit fixed windows only.
- Re-key native state when window identity changes; share selection outside item content.
- Use state.animateScrollToItem / animateScrollToPage from a caller coroutine for buttons.
- Callbacks report first-visible item for lists and settled page for pagers, including initial
  composition. They do not calculate the most-visible month or synchronize the host anchor.
- Adjacent-month cells and legacy adapter parity remain separate migration work.

No compilation, UI, gesture, state-restoration or RTL tests have been executed in this session.
