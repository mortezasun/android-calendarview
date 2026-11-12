# Adaptive sample viewport heights

Updated on 2026-11-12 for branch `update`.

## Main calendar viewport

Implemented in commit `6aa975e`. The adaptive host captures the bounded content-pane height before vertical scroll measurement. Safe-drawing insets are already excluded; 32 dp accounts for vertical pane padding. In tabletop mode the calendar uses the top pane ending at the local fold start, not the full window height.

`LocalSamplePaneHeight` supplies this height only to the content slot. `CalendarSampleViewport` defaults to the smaller of 480 dp and 75 percent of that height. The allowance is a sizing policy, not exact measurement of navigation, selectors or captions. Outer scrolling remains intentional. Explicit heights remain caller-controlled.

## Agenda

The agenda now uses the smaller of 280 dp and 75 percent of the same content-pane height. Without a bounded host it retains 280 dp. Main calendar and agenda are individually capped, not allocated a shared simultaneous-fit budget; their combined content can require outer scrolling.

## Dialog

The dialog reads its own text-slot constraints rather than the background calendar pane. Finite heights are capped at 360 dp without the former forced 240 dp minimum; zero remains zero. An unbounded slot retains a finite 340 dp fallback. This does not claim that a complete AlertDialog with title/actions fits every tiny window; fullscreen fallback and real compact-height checks remain acceptance work.

## Verification and review completion

Seven main-height JVM tests and seven secondary-height tests cover the bounded height calculations.
Sample month width dynamic configuration (336/376 dp) honoring the 48 dp touch target rule has been
implemented and wired into `CalendarSampleScreen` and `CalendarSampleViewport`.
Continuous layout, fold, and state survival are verified via `CalendarSampleAdaptiveLayoutTest` and
`CalendarSampleStateRestorationTest`.

No dependency, public library API, version, tag or release is changed.

