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

Seven main-height JVM tests already exist; this patch adds seven secondary-height tests. Tests are added, not claimed to have passed. No Gradle build, test compilation, JVM execution, instrumentation or CI outcome was verified in this session.

Before closing the adaptive review:
1. Compile and run all changed JVM and Compose tests; inspect results for the final commit, not historical release runs.
2. Exercise main calendar, agenda and dialog in compact height, large font, RTL, all three engines and each scrolling mode. Zero/tiny lazy viewport behavior must be checked.
3. Verify real selection, visible-month and scroll continuity across column-count, width and fold changes; the counter fixture is not sufficient evidence.
4. Verify fold geometry and actual window resizing on a foldable/tablet emulator or device, including system bars and asymmetric folds.
5. Implement the sample's 336/376 dp month-width configuration with week-number handling and tests; preserve the library's existing default.
6. Reconcile `PROJECT_STATUS.md`, `ADAPTIVE_LAYOUT.md`, release notes and the accumulated review documents. Remove unsupported full-verification claims.
7. Review the complete diff, public API guard and branch status and report remaining limitations. Integration into master is a separate approved operation; do not move published tags.

No dependency, public library API, version, tag or release is changed. The height calculations are implemented, but device acceptance and review closure remain open.
