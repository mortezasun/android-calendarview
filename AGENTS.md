# AGENTS.md

Binding working rules for coding agents in this repository. Read this file and the linked
documents before changing any code.

## Read first

1. [README.md](README.md) - public usage and build entry point.
2. [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) - modules, rendering pipeline and state handling.
3. [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) - patterns for typical changes.
4. [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md) - last verified state, known issues, next steps.

Check `git status` and `git branch --show-current` before editing. Preserve uncommitted work that
is not part of your task and never reset, stash or overwrite it without explicit permission.

## Project at a glance

- Multi-module Android **library** project, not an app-first project.
- `:library` contains the public views, adapter contracts, status models and heatmap views.
- `:calendar-gregorian`, `:calendar-hijri`, `:calendar-solarHijri` are calendar engines that depend
  on `:library`. `:calendarviewsample` is the demo app and depends on all modules.
- Public API lives in `com.develotter.calendarview.*`; calendar-specific types live in
  `com.develotter.calendarview.calendars.*`.
- Toolchain: Kotlin 2.4.21, AGP 9.5.1, Gradle 9.8.2, `compileSdk` 37, `minSdk` 27, Java 17.
- UI is **XML + ViewBinding**. There is **no Compose**, no ViewModel/Flow, no DI framework, no
  network and no persistence layer.

## Architecture conventions

- The core is a generic custom-view library. `MultiMonthView<D, W, M, SelectController>` and
  `MonthView` are parameterized by four `ViewBinding` types (day, week, month header, selection
  controller). Preserve these type parameters and the consumer-facing adapter hooks.
- `MonthStatus<Month, Day>` and `DayStatus` are the calendar-system abstraction. A new calendar
  system is added as a new module with subclasses of both, not by branching inside `:library`.
- Selection behaviour (none/single/multiple/range) lives in `MonthSampleAdapter`. Consumers only
  implement rendering callbacks such as `onBindDayView` and `onDayItemClick`.
- State is plain mutable objects: `CalendarStatus`, `DayStatus.isChecked`,
  `DayStatus.selectRangeDayStatus` and the selection lists in the adapter. Do not introduce
  `StateFlow`, LiveData or ViewModels for existing features.
- Calendar changes are applied by calling `MultiMonthView.addMonths(...)` again; the method clears
  and rebuilds all children. Keep that contract unless the task explicitly changes it.
- Public names, signatures and XML attributes are API. Do not rename or remove them as a side
  effect of another change.

## Verified commands

These commands were executed successfully on 2026-11-09 with JDK 17 and Android SDK Platform 37
(package `platforms;android-37.0`):

```bash
chmod +x gradlew                                  # if the checkout lost the executable bit
./gradlew :calendarviewsample:assembleDebug       # builds library + all calendar modules
./gradlew test                                    # JVM unit tests
./gradlew :library:lintDebug                      # lint for the core library
```

Single-module compile checks, for example `./gradlew :library:compileDebugKotlin`, are acceptable
for smaller changes. Report any command that could not be run and why.

## Rules for changes

- **Minimal invasive:** change only the files required by the task. No drive-by refactoring, no
  reformatting of untouched code, no renames.
- **No new dependencies** without explicit approval. Prefer what is already in
  `gradle/libs.versions.toml`.
- **No placeholder code.** Do not add `TODO: implement` stubs or leave code paths incomplete.
- **Do not delete existing code** or public API without explicit permission.
- Keep the existing style: 4-space indentation, KDoc in English, trailing commas where the file
  already uses them, package layout under `src/main/java`.
- Do not edit generated or build output (`build/`, `.gradle/`, `.kotlin/`, generated bindings).
- Do not weaken tests, lint or build settings to make a check pass.
- No secrets, API keys or machine-specific paths in tracked files. `local.properties` stays
  untracked.
- Git: commit only on request and only the files belonging to the task. Never force-push, never
  merge into `master`/`main`, never rewrite published history.

## When something is unclear

1. Inspect the code and the documents above first.
2. If the behaviour is still ambiguous, ask one focused question with the concrete options.
3. Do not invent product rules or historical rationale. If a reason cannot be verified from the
   repository, say so and document the open question instead of guessing.
4. Bugs discovered while working are reported in [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md)
   and in the final summary; do not fix them silently in an unrelated change.

## Documentation to update

| Change | Update |
|--------|--------|
| Public feature, usage, installation | `README.md` |
| New/removed module, rendering or state-flow change | `docs/ARCHITECTURE.md` |
| Toolchain, SDK, build or test setup | `docs/SETUP.md` |
| New recurring implementation pattern | `docs/DEVELOPMENT.md` |
| Any completed session or verified state change | `docs/PROJECT_STATUS.md` |
| Irreversible or structural architecture decision | new file in `docs/decisions/` |

Comments explain local *why*, documents explain the overview. Do not duplicate the same
explanation in both places.

## Definition of done

- The requested behaviour is implemented completely; no placeholder code remains.
- The smallest relevant build/compile check passes; tests and lint were run when they cover the
  change, or their absence is reported explicitly.
- Documentation and KDoc affected by the change are updated.
- `git diff` contains only intended changes; no formatting noise, no unrelated files.
- The final summary lists: changed files, executed checks with results, and known limitations.
