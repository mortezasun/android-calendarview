# Releaseplan 2.4.0: eigener chinesischer Kalender

Stand: 2026-11-13. Zielbranch: `update`. Status: Umsetzung und Review-Nachbesserungen
lokal verifiziert; Remote-CI fuer den Evidenzbaum `1ab841e` und den Fixstand `e80c00f` gruen.
Die Nachbesserungen auf Basis `6fac725` sind als `5cf640d` lokal verifiziert; der gepushte Baum
`952bbb6` ist mit allen vier Remote-Workflows gruen. JitPack, Geraetegates, Tag und Release offen.
Gepruefte Branchbasis: `a9642f13d596fd35137945bc234dbf2827e6e398`. Letzter per GitHub API
bestaetigter Release: `2.3.0`, Tag-Commit `8b7bea42e888785752fff199b2d53c04ab9888e4`,
veroeffentlicht am 2026-11-13. Abweichende historische Datumsangaben nicht ungeprueft
uebernehmen.

## Verbindlicher Auftrag

Ein selbstgeschriebenes chinesisches Kalendersystem als zusaetzliche, optional einbindbare
Library-Familie. Keine fremde Kalender-Engine, kein Wrapper um ICU und keine Portierung fremden
Kalendercodes. Berechnung in reinem Kotlin; Darstellung in Compose ohne XML, WebView oder JNI.
Unabhaengig von Gregorian-, Hijri- und Solar-Hijri-Modulen. Der existierende gemeinsame
Compose-Kern darf fuer die Darstellung verwendet werden; die Engine muss ohne diesen Kern
nutzbar sein.

Bestehender Code und bestehende Releases bleiben unveraendert. Die Checkboxen werden nur mit
tatsaechlichem Nachweis abgehakt; offene Punkte stehen ausdruecklich als offen.

## Dokumente und Lesereihenfolge

1. Dieser Plan: Umfang, Reihenfolge und Arbeitsauftraege.
2. [ADR 0005](docs/decisions/0005-independent-native-chinese-calendar.md): Unabhaengigkeit und Modulgrenzen.
3. [API und Kalendervertrag](docs/CHINESE_CALENDAR_SPEC.md): Datenmodell, Zeitkonvention, Grenzen.
4. [Tests und Risiken](docs/CHINESE_CALENDAR_QUALITY.md): Testmatrix, Anfaelligkeiten und Sicherheitsgates.
5. [Release 2.4.0](docs/releases/2.4.0.md): Integrationsinventar, Freigabe und Nachweise.
6. Bestehende `docs/RELEASE_CHECKLIST.md`, `docs/PROJECT_STATUS.md` und Releasebericht 2.3.0 als historische Baseline, nicht als Testnachweis fuer 2.4.0.

## Architektur und Lieferumfang

- `:calendar-chinese-native`: separat veroeffentlichbare Kotlin-Multiplatform-Engine; Kotlin-Standardbibliothek, keine Compose-, Android-, Java-Datums- oder fremden Kalender-Abhaengigkeiten im gemeinsamen Code.
- `:calendar-chinese-compose`: separat veroeffentlichbare Compose-Fassade; abhaengig nur von der neuen Engine, `:library` und bereits genehmigten Compose-/Plattform-Bausteinen. Die bestehende Android-API und der gemeinsame Renderer werden wiederverwendet.
- Bestehende Kalender duerfen keine Abhaengigkeit auf eines der neuen Module erhalten.
- KMP-/Kotlin-Native-Nachweise gelten in 2.4.0 fuer die Engine. Die Compose-Fassade bleibt Android, solange `:library` Android-spezifisch ist. Keine Bewerbung als iOS-Compose-Library ohne tatsaechliche Migration und Consumer-Tests. Eine komplette Migration bestehender Module ist nicht Teil dieses additiven Releases.
- Zielumfang: bidirektionale Datumsumrechnung, Schaltmonate, validierte Datumswerte, Tages- und Monatsnavigation, Monats-/Wochendaten, Compose-Monatsansicht, Saveable-Zustand, Agenda-/Pager-/Heatmap-Anbindung und Playground.
- Nicht enthalten: BaZi, Horoskope, amtliche Arbeits-/Ausgleichstage, vollstaendige astronomische Langzeitberechnung und exakte Sonnenabschnittszeiten.

## Umsetzung: immer nur eine Phase abschliessen

### P0 - Baseline und Build-Machbarkeit

- [x] Arbeitsbaum und Branch pruefen; fremde lokale Aenderungen erhalten. Keine Force-Pushes oder automatischen Merges. (Branch `update`; unabhaengige `artifacts/`-Datei bleibt untracked.)
- [x] README, AGENTS, Architektur, Entwicklungsregeln und aktuellen Status vollstaendig lesen; Buildskripte und Veroeffentlichungsworkflows inventarisieren.
- [x] Bestehende Tests, Lint, API-Guard und Sample-Build als Baseline ausfuehren; bestehende Fehler getrennt dokumentieren. (Baseline `test` gruen; keine bestehenden Fehler gefunden.)
- [x] Exakte Kotlin-/AGP-/Gradle-/Compose-Versionen aus dem Repository ermitteln; kompatible KMP-Plugin-/Android-Target-Konfiguration in einem kleinen Build-Prototyp pruefen. Kein blindes Upgrade auf angeblich letzte Versionen. (Kotlin 2.4.21, AGP 9.5.1, Gradle 9.8.2, Compose BOM 2026.09.00; KMP-Plugin ohne eigene Version, da AGP Kotlin bereits bereitstellt.)
- [x] Engine-Targets: Android-kompatibles Artefakt sowie mindestens ein ausgefuehrtes Kotlin/Native-Ziel; iOS-Geraete-/Simulator-Artefakte und Simulator-Consumer auf macOS verifizieren, bevor iOS-Support zugesagt wird. Exakte Targets und Testtasks nach Build-Prototyp dokumentieren. (`jvm()` von `:calendar-chinese-compose` konsumiert; `linuxX64Test` 14 Tests. iOS nicht deklariert.)
- [ ] Offene JitPack-2.3.0-Pruefung nachholen oder Ursache und Auswirkung auf 2.4.0 dokumentieren. Ein HTTP-500-Probeversuch ist kein bestaetigter Buildfehler und kein Consumer-Nachweis. (Weiterhin offen: Remote-JitPack fuer 2.3.0 ungeprueft; 2.4.0-Remote-Gate bleibt blockierend.)

### P1 - Datenvertrag und Datenherkunft

- [x] Vertrag aus CHINESE_CALENDAR_SPEC final pruefen und API-Entwurf reviewen.
- [x] Referenzdaten, Weiterverwendungsrechte und erforderliche Hinweise klaeren. Keine fremden Bitmaskentabellen oder Quelltexte kopieren. (HKO-IP-Hinweis: keine Daten uebernommen; Generator nutzt veroeffentlichte astronomische Reihen, VSOP87D-Eingabe nicht redistribuiert.)
- [x] Eigene deterministische Import-/Generierungswerkzeuge und ein Datenmanifest erstellen. Referenzabruf ist ein Entwicklungsprozess, nie Laufzeitfunktion der Library. (`tools/chinese-calendar/generate.py`, `manifest.json`, Ausgabe-SHA256 dokumentiert.)
- [x] Exakten unterstuetzten Epoch-Day-Bereich und benoetigte Randmonate aus den geprueften Daten bestimmen. Ziel ist der gregorianische Zeitraum 1901-2100; keine unbewiesene Zusage vollstaendiger chinesischer Jahre an den Grenzen. (Chinesische Jahre 1901..2099 = 1901-02-19..2100-02-08, 72.674 Tage.)
- [x] Gate: keine Engine-Freigabe mit ungeklaerter Datenlizenz, unbekannter Herkunft oder nicht erklaerten Referenzabweichungen. (2.458/2.461 HKO-Monatsstarts exakt; drei Abweichungen mit JDE/TT, Delta-T, UTC/UTC+8, NASA-Referenz und reproduzierbarem Audit in `reference-conflicts.json`; `--verify-reference` prueft vollstaendige Abdeckung.)

### P2 - Eigenstaendige Kotlin-Engine

- [x] Neues Engine-Modul mit echten Implementierungen anlegen, keine TODO-Stubs.
- [x] Validierte immutable Werte, Umrechnung, Monatslisten, Schaltmonate, Arithmetik und explizite Bereichsfehler implementieren.
- [x] Tabellen und Indizes immutable halten; Tagesrechnung ausschliesslich ganzzahlig auf Epoch Days.
- [x] Vollstaendige Bereichstests und unabhaengigen Referenzabgleich bestehen; gleiche Fixtures auf JVM und Kotlin/Native ausfuehren. (72.674-Tage-Roundtrip, 2.461 Monate lueckenlos, Generator-Referenzabgleich; `jvmTest` und `linuxX64Test` je 14 Tests.)
- [x] Gate: Engine ist ohne Android SDK, Compose und andere Kalender-Module als eigenstaendiger Consumer nutzbar. (JVM-Consumer mit `javac`, nur Engine-Jar + `kotlin-stdlib`; keine AndroidX/Compose/Android-Klassen im Jar.)

### P3 - Compose-Integration

- [x] Compose-Modul, MonthData, WeekData, State/Saver und Kalender-Composable implementieren.
- [x] Vorhandenen Renderer, Auswahl und Epoch-Day-Marker wiederverwenden; keine zweite Selection-Engine.
- [x] Normale und Schaltmonate in Navigation, Titeln, Keys, Saver und Heatmap-Jahreslisten unterscheiden.
- [x] Nebenmonatszellen an Datenbereichsgrenzen leer/deaktiviert behandeln; keine ungepruefte Konvertierung ausserhalb des Bereichs.
- [x] Alle bestehenden Darstellungsoptionen bewusst abgleichen; Ausnahmen dokumentieren statt Paritaet ungeprueft zu behaupten. (Bewusste Abweichungen: `todayEpochDay` statt `LocalDate`, `ChineseTextStyle` statt `dateProvider`; Header/Weekday/Adjacent/FixedRows/Markers/WeekNumber vorhanden.)

### P4 - Sample und Playground

- [x] Chinesischen Kalender als eigene Auswahl integrieren. Chinesischen Monat und gregorianisches Gitter mit chinesischer Zusatzbeschriftung anbieten.
- [x] Steuerung gruppieren: Kalender, Datum/Navigation, Auswahl, Darstellung, Grenzen und Details. Details zeigen Epoch Day, gregorianisches Datum, chinesisches Datum und Schaltmonatstatus.
- [ ] Telefon, Tablet, Mehrspalten, Split-Screen und Fold-Transitions mit vorhandener adaptiver Architektur testen. (Lokaler Emulator fehlt; Remote-Instrumentation und -Tablet sind fuer die bestehende Suite gruen, eine chinesische Geraete-/Fold-Matrix wurde nicht separat ausgefuehrt.)
- [ ] Auswahl und sichtbaren Monat bei Rotation, Zustandswiederherstellung und Kalenderwechsel erhalten. Ein begrenzter Anzeigemonat darf nicht stillschweigend die globale Auswahl veraendern. (Saver-/Navigationslogik per Unit-Test und bestehender Restoration-Suite abgedeckt; echte Activity-Recreation mit Chinese nicht separat ausgefuehrt.)

### P5 - Haertung und Dokumentationssynchronisation

- [ ] Gesamte Matrix in CHINESE_CALENDAR_QUALITY ausfuehren und Findings schliessen. (Funktionale Kernmatrix, Regression und Remote-Suite ausgefuehrt; chinesische Geraete-/Fold-/TalkBack-Matrix, Benchmarks und Advisory-Scan offen.)
- [x] README, ARCHITECTURE, DEVELOPMENT, SETUP, CI, PROJECT_STATUS, RELEASE_CHECKLIST, CHANGELOG und AGENTS auf tatsaechlich implementierten Stand bringen. Bestehende historische Nachweise erhalten.
- [x] Fehlenden API-Dump und Consumer-Beispiele ergaenzen; Lizenz-/NOTICE- und Publikationsmetadaten pruefen.
- [x] Bestehende API-Dumps gegen 2.3.0 vergleichen; neue Module duerfen keine versteckten Breaking Changes verursachen. (Fuenf bestehende Dumps `0` Diff; zwei neue Dumps.)

### P6 - Release Candidate und 2.4.0

- [x] Erst nach gruenen Produktgates Versionen synchron auf 2.4.0 vorbereiten; Sample versionCode 20400 nur nach Monotoniepruefung, versionName 2.4.0. (20300 -> 20400.)
- [ ] RC-Nachweise fuer einen exakten Commit sammeln; alle Veroeffentlichungs- und Consumer-Pruefungen aus dem Releaseplan ausfuehren. (Evidenzbaum `1ab841e70b6fb7ab35b16e970715b500b91f95a2`, Review-Fixstand `e80c00f` und Nachbesserungsbaum `952bbb6` auf `origin/update`; isolierte Publikation, zwei Consumer und alle vier Remote-Workflows bestanden. Remote-JitPack und die unabhaengige JitPack-Consumer-Pruefung weiterhin offen.)
- [ ] Separate ausdrueckliche Freigabe fuer Merge, Tag und GitHub Release einholen. Kein Tag 2.4.0 mit offenen Pflichtgates.
- [ ] Nach Tagging tatsaechliche Remote-Artefakte/Varianten und unabhaengige Consumer verifizieren. Erst dann erfolgreiche Distribution behaupten.

### Review-Nachbesserungen 2026-11-13 (Basis `6970b92`)

| Finding | Fix (Datei) | Regressionstest | Ausgefuehrte Pruefung |
|---|---|---|---|
| P1 Heute-Navigation | `calendar-chinese-compose/.../ChineseMonthState.kt`: `canGoToday` und `goToToday` pruefen beide `minMonth..maxMonth`; ausserhalb der Bounds bleibt der Monat unveraendert. | `ChineseComposeDataTest.monthStateTodayNavigationHonoursConfiguredBounds`; bestehender Leap-/Bounds-Test korrigiert. | `:calendar-chinese-compose:testDebugUnitTest` und `:calendarviewsample:testDebugUnitTest` gruen. |
| P1 Referenzabweichungen | `tools/chinese-calendar/generate.py`: Referenzdateien, Jahres-/Schaltstatus, vollstaendige Abdeckung und `--verify-reference`; `reference-conflicts.json` mit JDE/TT, Delta-T, UTC, UTC+8, strukturierter NASA/GSFC-Zeit und Toleranz; `manifest.json` neu erzeugt. Keine Tabellenkorrektur. | `tools/chinese-calendar/test_generate.py` (fehlende Dateien/Starts, Jahres-/Leap-Mismatch, Konfliktaudit, Verify-Fehlerstatus, Zeitketten-/Toleranzpruefung); `ChineseCalendarEngineTest.documentedHkoConflictsRemainOnTheUtcPlus8CivilDate`. | `test_generate.py`: 6 Tests, 0 Fehler. Generator `--verify-reference`: 2.458 exakt + 3 auditiert = 2.461, 0 fehlende Referenzen, `complete=True`, Exit 0. Manifest-Output/-Coverage stimmt mit dem gepushten Manifest. |
| P2 Overlay-Navigation | `CalendarSampleMonthNavigation.kt`, `CalendarSampleViewport.kt`, `CalendarSampleScreen.kt`, `CalendarSampleNavigation.kt`: gregorianische Monatsanker, Header und Pager-/Listenidentitaeten fuer `chineseGregorianGrid`. | `CalendarSampleNavigationTest.chineseOverlayUsesGregorianMonthStepsAndIdentities`; `ChineseComposeRegressionTest.gregorianOverlayHeaderAndPagerStepGregorianMonths`. | Sample-JVM-Tests gruen; `compileDebugAndroidTestKotlin` gruen; Remote-Instrumentation der Core-/Sample-Suite auf API 29 success. Keine separate chinesische Geraetematrix. |
| P2 Agenda | `CalendarSampleScreen.kt`: `chineseStyle` ist `remember`-Key. | `CalendarSampleMonthDataTest.chineseAgendaLabelsFollowTheSelectedStyleWithoutChangingTheDate`; `ChineseComposeRegressionTest.agendaRebuildsLabelsWhenTheStyleChangesWithoutADateChange`. | Sample-JVM-Tests und AndroidTest-Kompilierung gruen; Remote-Suite success. |
| P2 Today-Callback | `ChineseMonthState.kt`: `rememberUpdatedState` fuer Zustand und Saver; `ChineseMonthSnapshot.decode` lehnt Flags ausserhalb `0/1` ab. | `ChineseComposeDataTest.monthStateRejectsNonBinaryLeapFlagsWhenRestoring`; `ChineseComposeRegressionTest.chineseMonthStateSurvivesRestorationAndUsesAReplacedTodayCallback`. | Compose-JVM-Tests gruen; AndroidTest-Kompilierung gruen; Remote-Suite success. |
| P2 Kalenderwechsel | `CalendarSampleState.kt`, `CalendarSampleMonthNavigation.kt`: Anchor-Fallback an beiden Grenzen mit Meldung; Auswahl unveraendert; Header clamp't ungueltige Anchors. | `CalendarSampleStateTest.switchingToChineseClampsUnsupportedAnchorsAtBothEnds`; `ChineseComposeRegressionTest.unsupportedAnchorFallbackKeepsTheSharedSelection`. | Sample-JVM-Tests gruen; AndroidTest-Kompilierung gruen; Remote-Suite success. |

- [x] Release-Dokumentation: Der fruehere Widerspruch zwischen fehlendem Remote-Run und erfolgreichen Runs ist entfernt; die genannten Runs `1ab841e` und `e80c00f` gehoeren zu tatsaechlich gepushten Commits. Der Doku-Abschluss `2491c09` ist ebenfalls auf `update`; die vier zugehoerigen Remote-Workflows waren success (verification `43414411458`, publishing `43414411492`, instrumentation `43414411441`, tablet `43414411446`).
- [x] Remote-CI fuer den Fix-Commit `e80c00f` auf `update`: verification `43412948314`, publishing `43412948284`, instrumentation `43412948316`, tablet `43412948286` - alle success.

### Review-Nachbesserungen 2026-11-13 (Basis `6fac725`)

| Finding | Fix (Datei) | Regressionstest | Ausgefuehrte Pruefung |
|---|---|---|---|
| P1 Fenster an Engine-Grenzen | `calendarviewsample/.../CalendarSampleWindow.kt`: Center wird validiert; an einer endlichen Grenze wird die ungueltige Fensterseite weggelassen, der Grenzmonat bleibt erster/letzter Eintrag. | `CalendarSampleWindowTest.chineseWindowIsTruncatedAtBothEngineBounds`; `ChineseComposeRegressionTest.chineseBoundaryWindowRendersAndNavigatesInSingleMode` und `...InPager`. | Sample-JVM 60 Tests, 0 Fehler; AndroidTest kompiliert; neue UI-Tests lokal nicht ausgefuehrt (kein Emulator); Remote-Instrumentation `43439808384` success, keine separate chinesische Geraetematrix. |
| P2 Zeitketten-/Referenzaudit | `tools/chinese-calendar/generate.py`: strukturierte NASA-Referenz, explizite UTC-/UTC+8-Offsets, Gleichheit beider Instants und dokumentierte 60-s-Toleranz. | `test_conflict_instants_must_agree_and_use_structured_reference_tolerance` (manipulierte Zeiten, fehlende Zeitzonen, Toleranzverletzung). | `test_generate.py`: 6 Tests, 0 Fehler; `--verify-reference`: 2.458 + 3 = 2.461, 0 fehlend, Exit 0. |
| P2 NASA-Wert/HKO-Erklaerung | `reference-conflicts.json`: 1914-11-17 auf 16:02 UT korrigiert; 1916/1920 auf 16:05 UT bestaetigt; HKO-UT-Erklaerung als ungeklaert markiert. Manifest, Spezifikation, Quality- und Release-Doku synchronisiert. | `test_1914_reference_uses_the_actual_nasa_minute_and_marks_hko_reason_unresolved`. | Generator-/Manifestpruefung und Python-Tests gruen; Remote-CI `43439808516`/`43439808420` success. |
| P2 Today-Callback | Kein Produktionscode noetig; Test tauscht zwei echte `FixedToday`-Callback-Instanzen nach dem Restore aus. | `ChineseComposeRegressionTest.chineseMonthStateSurvivesRestorationAndUsesAReplacedTodayCallback`. | AndroidTest kompiliert; Geraeteausfuehrung lokal nicht moeglich; Remote-Instrumentation `43439808384` success, keine separate chinesische Geraetematrix. |

Diese Nachbesserungen sind lokal getestet. Remote-CI fuer `952bbb6` (enthaelt `5cf640d`):
verification `43439808516`, publishing `43439808420`, instrumentation `43439808384`, tablet
`43439808435` - alle success. Geraete-, Fold-, TalkBack-, Benchmark-, Advisory- und
JitPack-Gates bleiben offen.

#### Weiterhin offene Gates

- [ ] JitPack-Build `2.4.0` und JitPack-Only-Consumer ohne `mavenLocal`/`includeBuild`; die JitPack-2.3.0-Pruefung bleibt ebenfalls offen.
- [ ] Chinesische Geraete-/Fold-/TalkBack-/Grossschrift-/RTL-Matrix und Benchmarks. Die Remote-Instrumentation auf API 29 ist kein Ersatz fuer reale Geraete/Folds.
- [ ] Advisory-/CVE-Scan fuer die aufgeloesten direkten und transitiven Abhaengigkeiten.
- [x] Remote-CI fuer den Nachbesserungsbaum `952bbb6` (enthaelt `5cf640d`) auf `update`: verification `43439808516`, publishing `43439808420`, instrumentation `43439808384`, tablet `43439808435` - alle success.
- [ ] Merge, Tag `2.4.0` und GitHub Release nur nach separater ausdruecklicher Freigabe; bis dahin kein Tag und keine Publikation.

## Arbeitsregel fuer die Implementierung

Pro Phase: kleinste zusammenhaengende Aenderung, Tests, Diff-Review, Dokumentationsupdate und
klarer Bericht ueber ausgefuehrte/nicht ausgefuehrte Pruefungen. Keine Checkbox ohne
Ergebnisnachweis abhaken. Ein fehlender Runner, eine ungepruefte Lizenz oder ein gebrochener
Remote-Consumer ist ein offener Blocker, keine stillschweigende Ausnahme.

## Nachweise

Details, Kommandos und Ergebnisse: [PROJECT_STATUS.md](docs/PROJECT_STATUS.md#current-development-240-chinese-calendar-unreleased-2026-11-13)
und [releases/2.4.0.md](docs/releases/2.4.0.md#verifikation-2026-11-13-lokaler-arbeitsbaum).
