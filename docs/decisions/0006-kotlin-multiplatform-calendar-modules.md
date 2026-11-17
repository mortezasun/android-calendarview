# 0006 – Kotlin Multiplatform calendar modules

Status: accepted and released as `3.0.0` (2026-11-16); iOS runtime verification remains open.

## Context

The 2.4.0 tree was Android-only. `:library`, `:calendar-gregorian`, `:calendar-hijri`,
`:calendar-solarHijri` and `:calendar-chinese-compose` used `java.time` (`LocalDate`,
`YearMonth`, `DayOfWeek`, `HijrahDate`, `Clock`) and `java.util.Locale` in the code that the
month views share. `:calendar-solarHijri` additionally depended on `ir.huri:jalalicalendar`,
which is a JVM `java.util.Calendar` implementation. None of that code could be compiled for
iOS or any other Kotlin/Native target, so the calendar UIs were not reusable outside Android.

## Decision

Move the shared calendar code to `commonMain` and replace every JVM date/locale dependency
with two building blocks:

1. **Own immutable value types** with epoch-day identity:
   `GregorianYearMonth` (`:calendar-gregorian`), `HijriDate` (`:calendar-hijri`) and
   `SolarHijriDate` (`:calendar-solarHijri-native`); `ChineseMonth`/`ChineseDate` were already
   platform independent.
2. **Internal platform hooks** (`GregorianPlatformFormatter`, `HijriPlatform`,
   `SolarHijriPlatform`, `ChinesePlatform`, `HeatmapPlatformFormatter`) that supply the
   current epoch day, the locale's first day of week, weekday labels, number digits and the
   localized full date. The Android and JVM actuals use `java.time`/`java.util.Locale`; the
   iOS actuals use `NSCalendar`/`NSDateFormatter`/`NSNumberFormatter`.

Consequences of the shared identity:

- Every public callback and predicate now receives or consumes an epoch day (`Long`):
  `onDateClick: (Long) -> Unit`, `isDateEnabled: (Long) -> Boolean`, `todayEpochDay: Long`,
  `firstDayOfWeek: Int` (ISO 1..7) and `currentEpochDay: () -> Long` for state holders.
- `:calendar-solarHijri` becomes a thin facade over `:calendar-solarHijri-native`, so one
  Jalali implementation serves both modules and `ir.huri:jalalicalendar` is no longer
  declared anywhere.
- The modules use the AGP Kotlin Multiplatform library plugin
  (`com.android.kotlin.multiplatform.library`), which has one Android variant: AARs are built by
  `assembleAndroidMain` as `build/outputs/aar/<module>.aar` and there is no `assembleRelease`.
  The plugin also creates no lint or test tasks, so every module applies `com.android.lint` and
  opts in to `withHostTest`/`withDeviceTest`; `src/androidTest/java` is mapped into
  `androidDeviceTest` and instrumentation tests compile without a device.
- `:library` and the Compose calendar modules publish `iosArm64` and `iosSimulatorArm64`
  targets. `iosX64` is not published because Compose Multiplatform 1.12.2 does not provide
  it for these modules.
- JVM-only conveniences remain available in `jvmMain`/`androidMain`
  (`LocalDate.toSolarHijri()`, `SolarHijriDate.toLocalDate()`, `HijriDate` provider mappings),
  so existing Android hosts can convert at the boundary.

## Breaking changes

- `JalaliCalendar` and `HijrahDate` no longer appear in public signatures; use
  `SolarHijriDate` and `HijriDate`.
- `CalendarDayCell` callbacks reported a formatted day object in some engines; they now report
  the cell's epoch day.
- The `@Deprecated(HIDDEN)` binary-compatibility overloads that took `java.time` types were
  removed because they cannot be expressed in `commonMain`.
- `SolarHijriLocale.monthName`, `formatNumber` and `formatMonthYear` no longer take a
  `Locale`; they use the platform default locale, matching the rest of the library.

## Verification

- `commonTest` suites run on the JVM for all seven library modules and the sample
  (165 library tests, 62 sample tests); 148 of the shared tests also pass through
  `testAndroidHostTest`.
- `compileCommonMainKotlinMetadata`, `compileKotlinJvm`, `compileAndroidMain` and
  `compileIosMainKotlinMetadata` pass for the library and every calendar module.
- `lint` runs for the six KMP modules and the sample (0 errors),
  `compileAndroidDeviceTest` and `compileDebugAndroidTestKotlin` compile the instrumentation
  tests, and `:calendarviewsample:assembleRelease` builds the unsigned release APK.
- `publishToMavenLocal` succeeds for the seven modules and the BOM; `scripts/api-check.sh`,
  `scripts/klib-api-check.sh` and `scripts/check-commonmain-purity.sh` pass.
- iOS simulator tests and an `iosApp` sample were not executed: they require macOS/Xcode.

## Post-release multiplatform expansion (uncommitted)

Subsequent uncommitted work after `3.0.0` (on Kotlin `2.5.0-RC` and Compose Multiplatform
`1.13.0-beta02`, `compileSdk 37.1`):

- Consolidated Apple actuals from `iosMain` into `appleMain` and fixed the previously recorded Apple
  time-zone, civil-day, calendar-identifier and Hijri 12:00 UTC anchoring gaps.
- Added `macosArm64`, `js` and `wasmJs` targets to `:library` and all five Compose calendar modules,
  backed by `webMain` (`jsMain` + `wasmJsMain`) platform formatters using standard JS `Intl`/`Date`
  interop in `webMain` (`commonMain` purity intact).
- Expanded `:calendar-chinese-native` to `jvm`, `linuxX64`, `linuxArm64`, `mingwX64`, `iosX64`,
  `iosArm64`, `iosSimulatorArm64`, `macosArm64`, `js` and `wasmJs`, giving a 60-module/variant
  publication set together with `:calendar-bom`.
- Native execution on Apple (`ios*`, `macosArm64`), `linuxArm64` and `mingwX64` hardware remains
  unverified on Linux; see [PROJECT_STATUS.md](../PROJECT_STATUS.md).
