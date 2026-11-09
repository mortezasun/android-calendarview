# Adjacent days in the shared Compose grid

CalendarMonthGrid now accepts showPreviousMonthDays and showNextMonthDays independently.
Both default to false to preserve current entry-point behavior. When visible padding is
needed, adjacentDayProvider must return a fully formatted CalendarDayCell for the requested
absolute epoch day. A provider is not required if the requested side has no padding cells.

The pure buildCalendarMonthCells function constructs seven-column rows, checks chronological
current-month identities, marks current cells true and adjacent cells false through
isCurrentMonth, and preserves provider-supplied enabled flags. It rejects a provider returning
a different epoch identity. Integer overflow and engine range errors propagate.

Rendering uses the same selection/click path for current and adjacent dates. Selected cells
use selection colors; unselected adjacent dates inherit onSurfaceVariant. Custom slots can
inspect isCurrentMonth. Clicking an adjacent date selects/calls back but does not navigate.
Range selection keeps the same absolute-day identity across repeated rendered occurrences.

## Calendar-specific integration

This commit implements the shared renderer contract and six data tests, not the complete
engine/sample integration. Existing GregorianCalendarMonth, HijriCalendarMonth and
SolarHijriCalendarMonth still call the grid with default blank padding. Their entry points
need provider wiring and UI controls in a subsequent change. The new contract does not
incorrectly assume that Gregorian day numbers represent Hijri or Jalali dates.

Providers must use the relevant calendar conversion and locale. Gregorian callers can map
LocalDate.ofEpochDay(key), Hijri callers their Hijrah chronology, and Jalali callers the
existing checked conversion. They must decide date-range and disabled-date policy.

## Verification

Six JVM tests added for blank padding, independent visibility, year boundary alignment,
flags/input preservation and invalid inputs. New tests have not been run in this session.
The previous KVM/instrumentation workflow is green in the supplied screenshot; it does not
verify the changes in this commit. Updated builds, JVM tests and emulator checks must run.
