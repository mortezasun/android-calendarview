# Usage Guide

This guide shows usage patterns and API examples for all calendar engines, selection modes,
navigation, product views, customization, scrolling containers, heatmaps, and custom grids.

> [!TIP]
> All engines share the same epoch-day timeline (`Long`), so a single `CalendarSelectionState`
> works simultaneously across Gregorian, Hijri, Solar Hijri, and Chinese views.

---

## Calendar Engines

### Gregorian

```kotlin
import androidx.compose.runtime.Composable
import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.calendars.gregorian.compose.GregorianCalendarMonth
import com.develotter.calendarview.compose.rememberCalendarSelectionState
import com.develotter.calendarview.enums.TypeSelectDay

@Composable
fun GregorianScreen() {
    val state = rememberCalendarSelectionState(TypeSelectDay.Range)

    GregorianCalendarMonth(
        month = GregorianYearMonth.now(),
        selection = state.selection,
        onDateClick = { epochDay -> state.onDayClick(epochDay) },
        showPreviousMonthDays = true,
        showNextMonthDays = true,
        fixedSixRows = true,
    )
}
```

### Hijri (Islamic)

Umm al-Qura is the default platform chronology:

```kotlin
import androidx.compose.runtime.Composable
import com.develotter.calendarview.calendars.hijri.UmmAlQuraHijriDateProvider
import com.develotter.calendarview.calendars.hijri.compose.HijriCalendarMonth
import com.develotter.calendarview.compose.rememberCalendarSelectionState
import com.develotter.calendarview.enums.TypeSelectDay

@Composable
fun HijriScreen() {
    val todayEpochDay = 20_393L // Supply your current epoch day
    val state = rememberCalendarSelectionState(TypeSelectDay.Single)

    HijriCalendarMonth(
        month = UmmAlQuraHijriDateProvider.dateForEpochDay(todayEpochDay),
        selection = state.selection,
        onDateClick = { epochDay -> state.onDayClick(epochDay) },
    )
}
```

To apply a declared local day offset (or a custom `HijriDateProvider`), pass the same provider to
`rememberHijriMonthState` and `HijriCalendarMonth` alongside matching `todayEpochDay`:

```kotlin
import com.develotter.calendarview.calendars.hijri.OffsetHijriDateProvider
import com.develotter.calendarview.calendars.hijri.compose.HijriCalendarMonth
import com.develotter.calendarview.calendars.hijri.compose.rememberHijriMonthState
import com.develotter.calendarview.compose.rememberCalendarSelectionState
import com.develotter.calendarview.enums.TypeSelectDay

@Composable
fun HijriOffsetScreen(todayEpochDay: Long) {
    val selectionState = rememberCalendarSelectionState(TypeSelectDay.Single)
    val hijriMethod = OffsetHijriDateProvider(offsetDays = 1)
    val monthState = rememberHijriMonthState(
        initialMonth = hijriMethod.dateForEpochDay(todayEpochDay),
        dateProvider = hijriMethod,
    )

    HijriCalendarMonth(
        month = monthState.month,
        selection = selectionState.selection,
        onDateClick = { epochDay -> selectionState.onDayClick(epochDay) },
        todayEpochDay = todayEpochDay,
        dateProvider = hijriMethod,
    )
}
```

`hijriMonthData` and `hijriWeekData` accept the same `dateProvider`. The provider shifts labels and
month boundaries while selection callbacks and accessibility weekdays stay aligned with the
absolute timeline.

### Solar Hijri / Jalali (Persian)

Both `:calendar-solarHijri-native` and `:calendar-solarHijri` share the same dependency-free
Borkowski engine (`-61..3177` AP) and immutable `SolarHijriDate`:

```kotlin
import androidx.compose.runtime.Composable
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.calendars.solarHijriNative.compose.SolarHijriNativeCalendarMonth
import com.develotter.calendarview.compose.rememberCalendarSelectionState

@Composable
fun SolarHijriNativeScreen() {
    val state = rememberCalendarSelectionState()
    SolarHijriNativeCalendarMonth(
        month = SolarHijriDate.now(),
        selection = state.selection,
        onDateClick = { epochDay -> state.onDayClick(epochDay) },
    )
}
```

Using the `:calendar-solarHijri` facade:

```kotlin
import com.develotter.calendarview.calendars.solarHijri.compose.SolarHijriCalendarMonth
import com.develotter.calendarview.calendars.solarHijriNative.SolarHijriDate
import com.develotter.calendarview.compose.rememberCalendarSelectionState
import com.develotter.calendarview.enums.TypeSelectDay

@Composable
fun SolarHijriScreen() {
    val state = rememberCalendarSelectionState(TypeSelectDay.Multiple)

    SolarHijriCalendarMonth(
        month = SolarHijriDate.now(),
        selection = state.selection,
        onDateClick = { epochDay -> state.onDayClick(epochDay) },
        firstDayOfWeek = 6, // Saturday
    )
}
```

### Chinese Lunisolar

`:calendar-chinese-native` runs pure Kotlin offline across Chinese years `1901..2099`
(Gregorian `1901-02-19..2100-02-08`) with explicit leap months (never encoded as month 13):

```kotlin
import com.develotter.calendarview.calendars.chineseNative.ChineseCalendarEngine
import com.develotter.calendarview.calendars.chineseNative.ChineseDate
import com.develotter.calendarview.calendars.chineseNative.ChineseMonth

// Pure Kotlin: works without Compose, Android, or other calendar modules
val newYear = ChineseDate.of(2024, monthNumber = 1, isLeapMonth = false, dayOfMonth = 1)
val epochDay = newYear.toEpochDay()
val sameDay = ChineseDate.fromEpochDay(epochDay)

val leapSecondMonth = ChineseCalendarEngine.leapMonth(2023) // ChineseMonth(2023, 2, leap = true)
val months = ChineseCalendarEngine.monthsInYear(2023)       // 13 entries; leap month after month 2
```

Compose UI via `:calendar-chinese-compose`:

```kotlin
import androidx.compose.runtime.Composable
import com.develotter.calendarview.calendars.chineseCompose.ChineseCalendarMonth
import com.develotter.calendarview.calendars.chineseCompose.rememberChineseMonthState
import com.develotter.calendarview.calendars.chineseNative.ChineseMonth
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.compose.rememberCalendarSelectionState

@Composable
fun ChineseScreen() {
    val selection = rememberCalendarSelectionState()
    val monthState = rememberChineseMonthState(
        initialMonth = ChineseMonth.of(2024, 3),
    )

    ChineseCalendarMonth(
        month = monthState.month,
        selection = selection.selection,
        onDateClick = { epochDay -> selection.onDayClick(epochDay) },
        style = ChineseTextStyle.SimplifiedChinese,
    )
}
```

Use `GregorianChineseCalendarMonth` for a Gregorian grid annotated with secondary Chinese labels,
or `chineseYearHeatmap` / `chineseMonthHeatmap` for 12- or 13-month year heatmaps:

```kotlin
import com.develotter.calendarview.calendars.chineseCompose.GregorianChineseCalendarMonth
import com.develotter.calendarview.calendars.chineseNative.ChineseTextStyle
import com.develotter.calendarview.calendars.gregorian.GregorianYearMonth
import com.develotter.calendarview.compose.rememberCalendarSelectionState
import com.develotter.calendarview.enums.TypeSelectDay

@Composable
fun GregorianChineseScreen() {
    val state = rememberCalendarSelectionState(TypeSelectDay.Single)

    GregorianChineseCalendarMonth(
        month = GregorianYearMonth.now(),
        selection = state.selection,
        onDateClick = { epochDay -> state.onDayClick(epochDay) },
        style = ChineseTextStyle.SimplifiedChinese,
    )
}
```

`GregorianChineseCalendarMonth` follows Gregorian month boundaries and does not accept
`isDateEnabled`; use `ChineseCalendarMonth` when day filtering or Chinese-first grid pagination is
required.

---

## Selection Modes

| `TypeSelectDay` | Behavior |
|-----------------|----------|
| `None` | Clicks are reported; nothing is stored in selection. |
| `Single` | Zero or one selected day; tapping the selected day clears it, tapping another replaces it. |
| `Multiple` | Toggles individual days in the sorted selection list. |
| `Range` | First tap sets a single endpoint. When a complete range exists, tapping either endpoint removes it (leaving an incomplete one-day range); tapping outside the range extends start/end; tapping strictly inside keeps start and moves end. |

```kotlin
val state = rememberCalendarSelectionState(
    mode = TypeSelectDay.Multiple,
    initialEpochDays = listOf(todayEpochDay),
)

state.selection.selectedDays      // List<Long> (sorted epoch days)
state.selection.isRangeComplete   // Range mode only
todayEpochDay in state.selection
state.clear()
```

`CalendarSelection` is a plain, immutable value class that can also be held in an external state
holder and updated directly with `selection.onDayClick(epochDay)`.

### Selection presets

`presets(...)` derives the usual quick ranges without touching the platform clock or the selection
itself. Both inputs are explicit, so the same code runs unchanged on every target and in tests:

```kotlin
val presets = state.selection.presets(
    firstDayOfWeek = 1,          // ISO Monday; use 7 for Sunday-first screens
    todayEpochDay = todayEpochDay,
)

presets.today           // the passed epoch day
presets.thisWeek        // the seven days from firstDayOfWeek that contain today
presets.thisMonth       // civil Gregorian month of today
presets.lastSevenDays   // today - 6 .. today
```

`thisWeek` is a seven-day window aligned to `firstDayOfWeek` and may cross a month or year
boundary. `thisMonth` is the civil Gregorian month because epoch days are on the ISO timeline;
a non-Gregorian month range stays the engine's job (`*MonthState` plus the month data days).
`lastSevenDays` is relative to `today`, not to the configured first weekday. `CalendarRangePolicy`
is unaffected, so apply it yourself when a preset must be rejected for a screen's limits.

For custom calendar integrations or host wrappers that need the same ISO `1..7` week-start math,
`:library` also exposes `requireValidFirstDayOfWeek(firstDayOfWeek)`,
`firstDayOffset(epochDay, firstDayOfWeek)`, `weekStartEpochDay(epochDay, firstDayOfWeek)`,
`orderedIsoWeekdays(firstDayOfWeek)`, and `weekdayLabelsStartingAt(firstDayOfWeek) { iso -> ... }`
in `com.develotter.calendarview.selection`.

---

## Month Navigation

Engine month states own the visible month, inclusive bounds, and month arithmetic;
`CalendarMonthHeader` stays calendar-independent and renders the title plus navigation buttons.
All five saveable state holders (`rememberGregorianMonthState`, `rememberHijriMonthState`,
`rememberSolarHijriMonthState`, `rememberSolarHijriNativeMonthState`, `rememberChineseMonthState`)
expose `month`, `title()`, `canGoPrevious`, `canGoNext`, `canGoToday`, `previous()`, `next()`,
`goToToday()`, and `goTo(...)`.

```kotlin
val monthState = rememberGregorianMonthState(
    initialMonth = GregorianYearMonth.now(),
    minMonth = GregorianYearMonth(2026, 1),
    maxMonth = GregorianYearMonth(2026, 12),
)

CalendarMonthHeader(
    title = monthState.title(),
    state = monthState,
    previousMonthContentDescription = "Previous month",
    nextMonthContentDescription = "Next month",
    todayLabel = "Today", // Omit to hide the Today button
)

GregorianCalendarMonth(
    month = monthState.month,
    selection = state.selection,
    onDateClick = { state.onDayClick(it) },
)
```

Content descriptions and `todayLabel` are caller-supplied because the library ships no localized UI
strings. Tapping previous/next at a bound is a no-op; `goTo(...)` rejects out-of-range targets.

---

## Language and locale

Calendar labels follow the platform default locale until the caller selects one. Every engine
accepts a `CalendarLocale` from `:library`; the same value drives the month title, weekday
labels, day numbers and accessibility labels:

```kotlin
val data = gregorianMonthData(
    month = month,
    locale = CalendarLocale.German, // null keeps the platform default locale
)

GregorianCalendarMonth(
    month = month,
    selection = state.selection,
    onDateClick = { state.onDayClick(it) },
    locale = CalendarLocale.German,
)
```

The predefined languages are `English`, `German`, `Persian` and `Arabic`; any other language tag
renders English labels. Each engine ships its own tables (`gregorianMonthTitle`,
`hijriMonthData`, `solarHijriNativeMonthData`, `solarHijriMonthData`), so the labels do not depend
on the operating system and work on every target. The Chinese engine keeps `ChineseTextStyle`
because its labels are script choices rather than translations. A locale never changes layout
direction by itself: pass `LayoutDirection` from the host, for example through
`CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)`, or opt in to
deriving it from the language:

```kotlin
val locale = CalendarLocale.Persian
ProvideCalendarLocaleLayoutDirection(locale) { // Rtl for fa/ar/..., Ltr otherwise; null = unchanged
    GregorianCalendarMonth(month = month, selection = selection, onDateClick = onClick, locale = locale)
}
```

`CalendarLocale.isRightToLeft` exposes the same decision as a plain boolean,
`CalendarLocale.supportedLanguages` lists the languages with tables (`en`, `de`, `fa`, `ar`) and
`CalendarLocale.systemDefault()` wraps the platform language. On the JVM and Android,
`java.util.Locale.asCalendarLocale()` converts a `Locale`.

Unsupported tags (for example `es`) render English labels and Western digits; there is no fallback
chain, and only the language subtag is used (`de-AT` behaves like `de`).

### Which argument controls what

| Argument | Controls | Does not control |
|----------|----------|------------------|
| `CalendarLocale` | Gregorian, Hijri and Solar Hijri month titles, weekday labels, day numbers, accessibility labels; the Gregorian primary layer of `GregorianChineseCalendarMonth` | Chinese labels, layout direction, first day of week |
| `ChineseTextStyle` | Chinese month and day labels, including Chinese secondary labels on other grids | Gregorian, Hijri or Solar Hijri labels |
| `LayoutDirection` (host, or opt-in `ProvideCalendarLocaleLayoutDirection`) | Mirroring of the grid and header | Any label text |
| `firstDayOfWeek` (host) | Weekday order and offset | Label language |

So a Chinese calendar is not German when the locale is `de`: choose `ChineseTextStyle` for it.

---

## Week and Agenda

`CalendarWeekGrid` renders a seven-day row backed by engine `*WeekData` builders
(`gregorianWeekData`, `hijriWeekData`, `solarHijriNativeWeekData`, `solarHijriWeekData`,
`chineseWeekData`). Each builder takes `useIsoWeekNumber = true` (default `false`) to fill
`weekNumber` with the ISO-8601 week of the configured week start, written with the selected
language's digits:

```kotlin
val week = gregorianWeekData(todayEpochDay, firstDayOfWeek = 1)

CalendarWeekGrid(
    title = week.title,
    weekdayLabels = week.weekdayLabels,
    days = week.days,
    selection = state.selection,
    onDayClick = { state.onDayClick(it) },
    weekNumber = week.weekNumber,
)
```

`CalendarAgenda` renders a bounded vertical list sharing markers, selection, today styling, and
accessibility labels:

```kotlin
CalendarAgenda(
    days = gregorianMonthData(GregorianYearMonth.now()).days,
    selection = state.selection,
    onDayClick = { state.onDayClick(it) },
    markers = markers,
    modifier = Modifier.height(420.dp), // Requires a bounded height
)
```

---

## Markers and Week Numbers

Markers are keyed by absolute epoch day (`Map<Long, List<CalendarDayMarker>>`), so one map works
across every calendar engine. At most four dots render per cell; descriptions are appended to the
cell's accessibility label.

```kotlin
val markers = mapOf(
    GregorianYearMonth(2026, 11).atDay(11) to listOf(
        CalendarDayMarker(Color(0xFFD32F2F), "Holiday"),
        CalendarDayMarker(Color(0xFF1565C0), "Team event"),
    ),
)

GregorianCalendarMonth(
    month = GregorianYearMonth.now(),
    selection = state.selection,
    onDateClick = { state.onDayClick(it) },
    markers = markers,
    weekNumber = { weekStartEpochDay -> formatWeekLabel(weekStartEpochDay) },
)
```

`weekNumber` receives the first epoch day of each rendered week; return `null` to hide a row label.
`solarHijriWeekData(..., useIsoWeekNumber = true)` and its engine siblings fill the same slot from
`:library`'s ISO arithmetic instead of a host callback, so the label matches the week start the
grid actually renders. The Chinese builder writes Western digits because its text style has no
digit table. The default keeps the previous behaviour: Gregorian keeps its week-based-year
convention, the other engines keep `null`.

---

## Adaptive Months

`CalendarMonthColumns` computes the column count from available width (`minMonthWidth`, default
`320.dp`) and exposes `LazyGridState`:

```kotlin
CalendarMonthColumns(
    monthCount = months.size,
    minMonthWidth = 336.dp, // Guarantees >= 48 dp cells per column without week numbers
    modifier = Modifier.height(520.dp),
    monthKey = { months[it].toString() },
) { index ->
    GregorianCalendarMonth(
        month = months[index],
        selection = state.selection,
        onDateClick = { state.onDayClick(it) },
    )
}
```

`maxColumns` caps how many months share a row; further months wrap into additional rows instead of
stretching the grid across a wide window. The default `Int.MAX_VALUE` keeps the previous unbounded
layout, and the pure `calendarMonthColumns(width, minMonthWidth, spacing, maxColumns)` helper
exposes the same rule for hosts that compute their own layout:

```kotlin
CalendarMonthColumns(
    monthCount = 12,
    minMonthWidth = 336.dp,
    maxColumns = 3,          // three columns, four rows for a year
    modifier = Modifier.height(720.dp),
) { index -> GregorianCalendarMonth(month = GregorianYearMonth(2026, index + 1), ...) }
```

See [ADAPTIVE_LAYOUT.md](ADAPTIVE_LAYOUT.md) for responsive pane and fold notes.

---

### Dynamic Secondary Calendar Labels

Every engine exposes compact secondary-label, accessibility-label, and month-title helpers keyed
by absolute epoch day (`Long`) or month descriptor:

| Module | Helpers |
|--------|---------|
| `:calendar-gregorian` | `gregorianMonthTitle`, `gregorianSecondaryLabel`, `gregorianAccessibilityLabel` |
| `:calendar-hijri` | `hijriMonthTitle`, `hijriSecondaryLabel`, `hijriAccessibilityLabel` |
| `:calendar-solarHijri-native` | `solarHijriNativeMonthTitle`, `solarHijriNativeSecondaryLabel`, `solarHijriNativeAccessibilityLabel` |
| `:calendar-solarHijri` | `solarHijriMonthTitle`, `solarHijriSecondaryLabel`, `solarHijriAccessibilityLabel` |
| `:calendar-chinese-compose` | `chineseMonthTitle`, `chineseSecondaryLabel`, `chineseAccessibilityLabel` |

Pass any `secondaryLabel` and `secondaryAccessibilityLabel` pair into any month composable,
`CalendarMonthGrid`, `CalendarWeekGrid`, or `CalendarAgenda` to pair two calendar systems dynamically:

```kotlin
GregorianCalendarMonth(
    month = GregorianYearMonth.now(),
    selection = state.selection,
    onDateClick = { epochDay -> state.onDayClick(epochDay) },
    secondaryLabel = { epochDay -> hijriSecondaryLabel(epochDay) },
    secondaryAccessibilityLabel = { epochDay -> hijriAccessibilityLabel(epochDay) },
)
```

`CalendarMonthGrid(data = ...)` and `CalendarWeekGrid(data = ...)` also accept an engine
`CalendarMonthData` or `CalendarWeekData` snapshot directly alongside `secondaryLabel` and
custom trailing `dayContent`.

---

## Customization & Styling

The six month composables (`GregorianCalendarMonth`, `HijriCalendarMonth`, `SolarHijriCalendarMonth`,
`SolarHijriNativeCalendarMonth`, `ChineseCalendarMonth`, and `GregorianChineseCalendarMonth`) share
these common optional parameters (`ChineseCalendarMonth` and `GregorianChineseCalendarMonth` also add
`style: ChineseTextStyle`; `GregorianChineseCalendarMonth` omits `isDateEnabled`,
`secondaryLabel`, and `secondaryAccessibilityLabel`):

| Parameter | Default | Description |
|-----------|---------|-------------|
| `firstDayOfWeek` | platform default (Gregorian, Chinese) / `6` (Hijri, Solar Hijri) | ISO weekday `1..7` for the first grid column. |
| `showPreviousMonthDays` / `showNextMonthDays` | `false` | Show adjacent-month days in leading/trailing cells. |
| `fixedSixRows` | `false` | Always render 42 cells (6 weeks) for uniform height. |
| `showHeader` / `showWeekdayLabels` | `true` | Toggle month title and weekday row visibility. |
| `todayEpochDay` | platform clock | Epoch day highlighted as *today*. |
| `todayBorderColor` | `null` → `LocalCalendarStyle`, then `MaterialTheme.colorScheme.tertiary` | Optional border color of the *today* indicator. |
| `todayDescription` | `null` | Caller-localized text appended to today's accessibility label (for example `"Today"`). |
| `dateProvider` | `UmmAlQuraHijriDateProvider` | Hijri only: custom or shifted date provider. |
| `markers` | `emptyMap()` | Event markers keyed by epoch day. |
| `weekNumber` | `null` | Optional caller-formatted week-number column. |
| `isDateEnabled` | `{ true }` | Predicate controlling interactive day state. |
| `secondaryLabel` | `null` | Optional compact secondary calendar line rendered inside each day cell. |
| `secondaryAccessibilityLabel` | `null` | Optional screen-reader description appended for the secondary calendar date. |
| `dayContent` | `Text(day.label)` | Trailing composable slot customizing the primary cell content. |

### `LocalCalendarStyle` & `CalendarRangePolicy`

```kotlin
CompositionLocalProvider(
    LocalCalendarStyle provides CalendarStyle(
        selectedContainerColor = Color(0xFFE3F2FD),
        markerSize = 8.dp,
    ),
) {
    GregorianCalendarMonth(/* ... */)
}

val rangedSelection = rememberCalendarSelectionState(
    mode = TypeSelectDay.Range,
    rangePolicy = CalendarRangePolicy(minRangeLength = 2, maxRangeLength = 14),
)
// For custom drag-to-select overlays:
rangedSelection.selectRange(startEpochDay, endEpochDay)
```

### `CalendarDateWindow`

`CalendarRangePolicy` limits the *length* of a range; `CalendarDateWindow` limits *where* days may
lie, with optional inclusive `minEpochDay` / `maxEpochDay` bounds. Pass its `isDateEnabled` adapter
to any composable or data builder; disabled days still render but reject taps.

```kotlin
val window = CalendarDateWindow(minEpochDay = todayEpochDay, maxEpochDay = todayEpochDay + 90)

GregorianCalendarMonth(
    month = month,
    selection = state.selection,
    onDateClick = { state.onDayClick(it) },
    isDateEnabled = window.isDateEnabled,
)

// For externally built ranges, check the window first; selectRange applies the length policy:
if (window.allows(start, end)) state.selectRange(start, end)
```

---

## Scrolling Containers

`CalendarMonthList` (`LazyColumn`/`LazyRow`) and `CalendarMonthPager`
(`HorizontalPager`/`VerticalPager`) require a bounded viewport:

```kotlin
@Composable
fun MonthPagerScreen() {
    val state = rememberCalendarSelectionState(TypeSelectDay.Single)
    val months = remember { (-12..12).map { GregorianYearMonth.now().plusMonths(it) } }
    val pagerState = rememberPagerState(initialPage = 12, pageCount = { months.size })

    CalendarMonthPager(
        monthCount = months.size,
        state = pagerState,
        axis = CalendarScrollAxis.Horizontal,
        monthKey = { months[it].toString() },
        onMonthActive = { index -> /* settled page index */ },
        modifier = Modifier.height(420.dp),
    ) { index ->
        GregorianCalendarMonth(
            month = months[index],
            selection = state.selection,
            onDateClick = { state.onDayClick(it) },
            fixedSixRows = true,
        )
    }
}
```

See [COMPOSE_MONTH_CONTAINERS.md](COMPOSE_MONTH_CONTAINERS.md) for container details.

---

## Month Picker

`CalendarMonthPicker` renders one year of selectable month cells. It is stateless: the host owns
the visible year and the dialog frame, and the picker only reports the chosen month.

```kotlin
var year by rememberSaveable { mutableIntStateOf(2026) }
var month by rememberSaveable { mutableIntStateOf(11) }

CalendarMonthPicker(
    yearRange = 2000..2100,
    selectedYear = year,
    selectedMonth = month,
    onMonthSelected = { selectedYear, selectedMonth ->
        year = selectedYear
        month = selectedMonth
        monthState.goTo(GregorianYearMonth(selectedYear, selectedMonth))
    },
    onYearChange = { year = it },          // optional year-step buttons
    monthLabel = { Text(monthNames[it - 1]) },   // host-localized names
    yearLabel = { Text(it.toString()) },
)
```

Cells are `selectable`, so they are focusable and activate with Enter/Space, and they expose
`CollectionInfo`/`CollectionItemInfo` for screen readers. Year buttons disable at the range edges,
and everything mirrors with `LocalLayoutDirection` like the month grid. `:library` ships no UI
strings: month names, year labels and the button descriptions come from the caller. Month numbers
are `1..12`, so the same picker works for a non-Gregorian calendar by passing that calendar's month
names. See [ADR 0008](decisions/0008-calendar-month-picker.md).

---

## Heatmaps

Use `CalendarYearHeatmap` / `CalendarQuarterHeatmap` for Gregorian heatmaps, or pass
engine-built `HeatmapMonthData` blocks (`toHeatmap()`, or `chineseYearHeatmap` /
`chineseMonthHeatmap`) to the calendar-agnostic `CalendarYearHeatmap` overload:

```kotlin
val months = (1..12).map { month ->
    solarHijriNativeMonthData(SolarHijriDate(1405, month, 1)).toHeatmap { epochDay ->
        intensities[epochDay] ?: 0f
    }
}

val state = rememberCalendarSelectionState(TypeSelectDay.Range)

CalendarYearHeatmap(
    months = months,
    onDayClick = { day -> state.onDayClick(day.epochDay) },
    orientation = HeatmapOrientation.WeeksAsRows,
    style = HeatmapStyle(cellSize = 36.dp, cellSpacing = 4.dp, cellShape = RoundedCornerShape(8.dp)),
    selection = state.selection,
    todayBorderColor = Color(0xFF1565C0),
    colorForIntensity = { lerp(Color(0xFFEBEDF0), Color(0xFF216E39), it) },
    header = { Text(it, style = MaterialTheme.typography.titleMedium) },
)
```

`HeatmapStyle.cellSize` controls only the drawn cell; layout touch slots stay at least `48.dp` in
`WeeksAsColumns` (both axes) and at least `48.dp` tall in `WeeksAsRows`.

For a stepped scale, pass `HeatmapBuckets` and reuse the same object for the legend:

```kotlin
val buckets = HeatmapBuckets(listOf(0.25f, 0.5f, 0.75f))

CalendarYearHeatmap(
    months = months,
    buckets = buckets,
    colorForIntensity = { lerp(Color(0xFFEBEDF0), Color(0xFF216E39), it) },
    onDayClick = { day -> state.onDayClick(day.epochDay) },
)

HeatmapLegend(
    buckets = buckets,
    colorForIntensity = { lerp(Color(0xFFEBEDF0), Color(0xFF216E39), it) },
    label = { bucket -> Text("$bucket") },
    contentDescription = { bucket -> "Level ${bucket + 1}" },
)
```

Thresholds must be finite, inside `0..1` and strictly ascending. An intensity equal to a threshold
stays in the lower step; the drawn color uses that step's lower bound, while `HeatmapDayData`,
selection and accessibility descriptions keep the real value. `buckets = null` (the default) keeps
the continuous `colorForIntensity` lookup.

---

## Build Your Own Calendar View

The pieces compose in five steps. Each step uses only public API and keeps the caller in charge of
state, so the same recipe works for every engine.

1. **Pick the engine month.** `GregorianYearMonth.now()` (or `HijriDate`, `SolarHijriDate`,
   `ChineseMonth`) is the visible month. Hold it in the engine's saveable `*MonthState`, for example
   `rememberGregorianMonthState()`, so rotation and process death keep it.
2. **Own the selection.** `rememberCalendarSelectionState(TypeSelectDay.Range)` stores epoch days
   only; never key selection by formatted dates.
3. **Render the month.** Call the engine composable (`GregorianCalendarMonth`) with the selection and
   `onDateClick`. Add `markers`, `weekNumber`, `locale` and `isDateEnabled` as needed.
4. **Add navigation.** Use `CalendarMonthHeader` for previous/next/today, `CalendarMonthPager` or
   `CalendarMonthList` for swiping and scrolling, and `CalendarMonthPicker` inside your own dialog.
5. **Limit and polish.** `CalendarRangePolicy` limits range length, `CalendarDateWindow` limits the
   selectable days, `LocalCalendarStyle` themes the cells and `keyboardNavigation = true` enables
   arrow-key movement.

```kotlin
@Composable
fun BookingCalendar(todayEpochDay: Long) {
    val monthState = rememberGregorianMonthState()
    val selection = rememberCalendarSelectionState(
        mode = TypeSelectDay.Range,
        rangePolicy = CalendarRangePolicy(minRangeLength = 2, maxRangeLength = 14),
    )
    val window = CalendarDateWindow(minEpochDay = todayEpochDay)

    Column {
        CalendarMonthHeader(
            title = monthState.title(),
            state = monthState,
            previousMonthContentDescription = "Previous month",
            nextMonthContentDescription = "Next month",
            todayLabel = "Today",
        )
        GregorianCalendarMonth(
            month = monthState.month,
            selection = selection.selection,
            onDateClick = { selection.onDayClick(it) },
            isDateEnabled = window.isDateEnabled,
            keyboardNavigation = true,
        )
    }
}
```

> [!NOTE]
> Keep date arithmetic in epoch days and convert to a calendar date only at the edge. Day cells then
> never depend on a time zone or daylight-saving change.

---

## Custom Calendars (`CalendarMonthGrid`)

Build any custom calendar directly on top of the stateless `CalendarMonthGrid`:

```kotlin
import androidx.compose.runtime.Composable
import com.develotter.calendarview.compose.CalendarDayCell
import com.develotter.calendarview.compose.CalendarMonthGrid
import com.develotter.calendarview.compose.rememberCalendarSelectionState
import com.develotter.calendarview.enums.TypeSelectDay

@Composable
fun MiniMonthGrid() {
    val state = rememberCalendarSelectionState(TypeSelectDay.Single)
    val firstEpochDay = 20_000L
    val days = List(30) { index ->
        CalendarDayCell(
            epochDay = firstEpochDay + index,
            label = (index + 1).toString(),
            accessibilityLabel = "Day ${index + 1}",
            isToday = index == 0,
        )
    }

    CalendarMonthGrid(
        title = "Mini month",
        weekdayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
        firstDayOffset = 0,
        days = days,
        selection = state.selection,
        onDayClick = { epochDay -> state.onDayClick(epochDay) },
    )
}
```

### Keyboard navigation

Desktop (JVM), web and keyboard-driven Android hosts can opt in to grid keyboard handling. It is
off by default, so focus and accessibility semantics of existing screens stay unchanged:

```kotlin
CalendarMonthGrid(
    title = data.title,
    weekdayLabels = data.weekdayLabels,
    firstDayOffset = data.firstDayOffset,
    days = data.days,
    selection = state.selection,
    onDayClick = { state.onDayClick(it) },
    keyboardNavigation = true,
    onMonthStep = { monthDelta ->
        // The grid does not know the month identity, so the host moves the visible month.
        if (monthDelta > 0) monthState.next() else monthState.previous()
    },
)
```

- Arrow keys move the focused day by one day (horizontal direction follows
  `LocalLayoutDirection`) or one week, and report the new day through `onDayClick`; the caller still
  owns the selection.
- Page Up / Page Down call `onMonthStep` with `-1` or `1` because only the host knows the month.
  Without `onMonthStep` the keys are ignored, and an arrow key that leaves the rendered cells also
  requests the matching month step.
- Enter and Space report the focused day through `onDayClick`. The focused cell draws a border; a
  today cell keeps its today border.
- The grid keeps a focus position as view state only. It adds no `StateFlow`, no internal selection
  and no collection semantics change.
