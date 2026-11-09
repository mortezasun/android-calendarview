# Compose month lists, pagers and synchronized sample navigation

Shared CalendarMonthList/Pager remain calendar-neutral and expose native scroll state,
unique/saveable keys and active-month callbacks. Lists report first-visible indices;
pagers report settled pages. The shared APIs are unchanged in this sample synchronization.

## Controlled visible month

The sample now separates its saved window center from the controlled visible anchor.
A 25-month window uses real calendar arithmetic; it does not rebuild on every swipe.
Settled scrolling updates the outer anchor (first visible month for lists, settled page
for pagers). Previous/Next calculate from that month, and the dialog shows it too.
Mode switches initialize at the controlled month instead of an unrelated old list/pager
position. Intra-month offsets are intentionally reset on mode changes.

Programmatic requests scroll the current native container. Requests already reported by
user scrolling are acknowledged without scrolling back to the start of a partially visible
month. Observation is gated while applying external requests to prevent feedback loops.
Requests outside the current window recenter it. This is not infinite loading while
swiping; the finite edges remain. Complete-window calendar range failures display errors.

Calendar changes keep the absolute anchor and open the corresponding month in the new
engine. The selection and adjacent-day switches remain shared. Today requests today's
local date; the viewport normalizes it to the current engine's month. A settled month,
not an intermediate drag position, is the authoritative anchor.

## Layout and state

Viewport remains 480dp high and width-bounded; horizontal items are 320dp wide. Pager and
horizontal-list contents can scroll vertically for overflow. Window center and mode are
saveable, as is the outer anchor. Native container positions are reconciled against it
after restoration. Old within-month offsets need not survive mode switches.

## Verification

Four JVM window tests cover ordering/identity in all engines, lookup without recentering,
zero radius and invalid radius. They do not validate coroutine timing or UI gestures.
No new UI/scroll/restoration tests were run for this change. The current emulator suite
checks core selection/grid behavior, not the sample's synchronization logic.

Required manual checks: swipe then Next/Previous/dialog; switch all five modes; Today
inside/outside window; rapid requests during scrolling; rotation; calendar/locale/RTL
changes; cross-month range selection and large fonts. Automated sample UI coverage remains
open even if compilation, JVM tests and core instrumentation pass.
