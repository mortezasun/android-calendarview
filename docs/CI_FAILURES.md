# CI failure investigation

Status: **resolved.** The dangling manifest entry was removed together with the legacy
activities; the current `calendarviewsample/src/main/AndroidManifest.xml` declares only
`LauncherActivity` and `HeatMapViewActivity`. This file is kept as the record of the original
investigation, and the "Correction" section below still describes the 1.x-era state in which
`CalendarViewActivity` existed.

Date: 2026-11-09
Branch: update
Reported run: 42628963758
Reported lint job: 129203160430

## Evidence

The user supplied annotations containing four occurrences of:

Class referenced in the manifest, com.develotter.calendarviewsample.CustomCalendarViewActivity,
was not found in the project or the libraries.

The lint job ended with exit code 1. The current source manifest was inspected and still
declared .CustomCalendarViewActivity; the main sample source directory contains no such
class. These repeated annotations name the same missing component; the full job log was
not accessible, so this note does not claim there are no additional failures.

## Correction

Remove only the missing activity declaration from calendarviewsample/src/main/AndroidManifest.xml.
Keep LauncherActivity, HeatMapViewActivity and CalendarViewActivity unchanged. Do not add
a placeholder activity, suppress lint, disable abort-on-error or weaken CI checks. This
resolves the dangling declaration previously recorded in the migration audit at source level.
Older audit/setup documents describe the historical issue until reconciled.

## Separate runner notices

Three notices describe ubuntu-latest moving to Ubuntu 26.04. They are runner migration
announcements, not the missing-class lint error. No runner version is changed in this
focused fix. A pinned runner image can be considered separately for reproducibility.

## Verification status

No local Gradle command was executed and no successful rerun was observed. Build and JVM
test conclusions are not established by the supplied annotations alone. The approved push
may trigger the existing verification workflow; inspect the run for the new commit.
The separately proposed emulator workflow is not part of this fix or its approval.

```bash
bash ./gradlew :calendarviewsample:lintDebug
```

After that check, verify all-module lint, assembly and tests before closing priority 0.
