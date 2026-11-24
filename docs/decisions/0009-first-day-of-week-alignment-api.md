# 0009: Shared first-day-of-week alignment API

## Context

Each calendar module (`:calendar-gregorian`, `:calendar-hijri`, `:calendar-solarHijri-native`,
`:calendar-chinese-compose`) and `CalendarSelection.presets` previously duplicated local arithmetic
to validate `firstDayOfWeek` (`1..7`), compute the leading empty cell offset before a month's
first day or week start, and rotate weekday labels starting from a configured ISO weekday.

Centralizing this pure epoch-day math in `:library` eliminates duplication while keeping every
calendar engine's default start day, label language, RTL layout direction, and ISO week numbering
(`isoWeekNumber`, always Monday-based) strictly independent.

## Decision

Add shared week-start helpers to `:library` (`com.develotter.calendarview.selection` in
`CalendarWeekStart.kt`):

1. **ISO-8601 numbering and validation:**
   - `requireValidFirstDayOfWeek(firstDayOfWeek: Int): Int` enforces `firstDayOfWeek in 1..7`
     (1 = Monday .. 7 = Sunday) and throws `IllegalArgumentException` for invalid values such as
     `0` or `8`.
   - Nullable `firstDayOfWeek` parameters in sample/host wrappers keep `null` as "use the calendar
     engine's default".

2. **Offset and week-start helpers:**
   - `firstDayOffset(epochDay: Long, firstDayOfWeek: Int): Int` validates `firstDayOfWeek` and
     returns `(isoWeekday(epochDay) - firstDayOfWeek).mod(7)` in `0..6`.
   - `weekStartEpochDay(epochDay: Long, firstDayOfWeek: Int): Long` validates `firstDayOfWeek` and
     returns `epochDay - firstDayOffset(epochDay, firstDayOfWeek)`.

3. **Weekday label ordering:**
   - `orderedIsoWeekdays(firstDayOfWeek: Int): List<Int>` validates `firstDayOfWeek` and returns
     the seven ISO weekday integers in grid order starting at `firstDayOfWeek`.
   - `weekdayLabelsStartingAt(firstDayOfWeek: Int, labelForIsoWeekday: (Int) -> String): List<String>`
     builds the seven localized weekday labels in grid order.

4. **Engine defaults and purity:**
   - Existing engine defaults remain unchanged (Gregorian: `GregorianPlatformFormatter.defaultFirstDayOfWeek()`,
     Hijri / Solar Hijri / Solar Hijri Native: Saturday = `6`, Chinese Compose: `ChinesePlatform`).
   - `:calendar-chinese-native` remains completely independent of `:library` and Compose.
   - Public composables and data builders keep their existing signatures and defaults; engines
     delegate internal offset and label-rotation calculations to the shared `:library` helpers.
