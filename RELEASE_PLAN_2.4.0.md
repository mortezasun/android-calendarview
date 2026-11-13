# Releaseplan 2.4.0: eigener chinesischer Kalender

Stand: 2026-11-13. Zielbranch: `update`. Status: Umsetzung und Review-Nachbesserungen
lokal verifiziert; Remote-CI fuer den Evidenzbaum `1ab841e` und den Fixstand `e80c00f` gruen.
Die Nachbesserungen auf Basis `6fac725` sind als `5cf640d` lokal verifiziert; der gepushte Baum
`952bbb6` ist mit allen vier Remote-Workflows gruen. Die Review-Findings gegen `54bf23d` sind im
Arbeitsbaum lokal behoben, publiziert und mit isolierten Consumern geprueft. Der Nachbesserungs-
block ist als `09e3a73` auf `update`; alle vier Remote-Workflows sind success. Die
Publishing-URL-Nachbesserung auf Basis `acf3bfc` ist lokal verifiziert. Der Folgeblock ist als
`9fd3b50` auf `update`; alle vier Remote-Workflows sind success. Die Nachbesserung auf Basis
`9fd3b50` ist als `a138306` gepusht und mit allen vier Remote-Workflows gruen. JitPack ist fuer
`2.3.0` sowie den Commit `7a51e24` nachgewiesen; der 2.4.0-Tag-/BOM-Consumer bleibt ohne Tag
blockiert. Benchmarks und der Advisory-Scan sind ausgefuehrt. Die chinesische
Geraete-/Fold-/TalkBack-/Grossschrift-/RTL-Matrix sowie Tag und Release bleiben offen.
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
- [x] Offene JitPack-2.3.0-Pruefung nachholen oder Ursache und Auswirkung auf 2.4.0 dokumentieren. Ein HTTP-500-Probeversuch ist kein bestaetigter Buildfehler und kein Consumer-Nachweis. (2.3.0-Buildlog HTTP 200 mit allen fuenf Modulen und BOM; JitPack-only Android-BOM-Consumer mit `library` kompiliert: `BUILD SUCCESSFUL`; 2.4.0-Tag bleibt separat blockiert, siehe Gate-Nachtrag.)

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

- [ ] Gesamte Matrix in CHINESE_CALENDAR_QUALITY ausfuehren und Findings schliessen. (Funktionale Kernmatrix, Regression und Remote-Suite ausgefuehrt; Benchmarks und Advisory-Scan mit realen Ergebnissen nachgetragen; chinesische Geraete-/Fold-/TalkBack-/Grossschrift-/RTL-Matrix und physische Benchmark-Abnahme offen.)
- [x] README, ARCHITECTURE, DEVELOPMENT, SETUP, CI, PROJECT_STATUS, RELEASE_CHECKLIST, CHANGELOG und AGENTS auf tatsaechlich implementierten Stand bringen. Bestehende historische Nachweise erhalten.
- [x] Fehlenden API-Dump und Consumer-Beispiele ergaenzen; Lizenz-/NOTICE- und Publikationsmetadaten pruefen.
- [x] Bestehende API-Dumps gegen 2.3.0 vergleichen; neue Module duerfen keine versteckten Breaking Changes verursachen. (Fuenf bestehende Dumps `0` Diff; zwei neue Dumps.)

### P6 - Release Candidate und 2.4.0

- [x] Erst nach gruenen Produktgates Versionen synchron auf 2.4.0 vorbereiten; Sample versionCode 20400 nur nach Monotoniepruefung, versionName 2.4.0. (20300 -> 20400.)
- [ ] RC-Nachweise fuer einen exakten Commit sammeln; alle Veroeffentlichungs- und Consumer-Pruefungen aus dem Releaseplan ausfuehren. (Evidenzbaum `1ab841e70b6fb7ab35b16e970715b500b91f95a2`, Review-Fixstand `e80c00f` und Nachbesserungsbaum `952bbb6` auf `origin/update`; isolierte Publikation, zwei Consumer und alle vier Remote-Workflows bestanden. JitPack-Commit-Build `7a51e24` und direkter Engine-Consumer bestanden; der 2.4.0-Tag-/BOM-Consumer ist ohne Tag blockiert, siehe Gate-Nachtrag.)
- [ ] Separate ausdrueckliche Freigabe fuer Merge, Tag und GitHub Release einholen. Kein Tag 2.4.0 mit offenen Pflichtgates.
- [ ] Nach Tagging tatsaechliche Remote-Artefakte/Varianten und unabhaengige Consumer verifizieren. Erst dann erfolgreiche Distribution behaupten.

### Review-Nachbesserungen 2026-11-13 (Basis `6970b92`)

| Finding | Fix (Datei) | Regressionstest | Ausgefuehrte Pruefung |
|---|---|---|---|
| P1 Heute-Navigation | `calendar-chinese-compose/.../ChineseMonthState.kt`: `canGoToday` und `goToToday` pruefen beide `minMonth..maxMonth`; ausserhalb der Bounds bleibt der Monat unveraendert. | `ChineseComposeDataTest.monthStateTodayNavigationHonoursConfiguredBounds`; bestehender Leap-/Bounds-Test korrigiert. | `:calendar-chinese-compose:testDebugUnitTest` und `:calendarviewsample:testDebugUnitTest` gruen. |
| P1 Referenzabweichungen | `tools/chinese-calendar/generate.py`: Referenzdateien, Jahres-/Schaltstatus, vollstaendige Abdeckung und `--verify-reference`; `reference-conflicts.json` mit JDE/TT, Delta-T, UTC, UTC+8, strukturierter NASA/GSFC-Zeit und Toleranz; `manifest.json` neu erzeugt. Keine Tabellenkorrektur. | `tools/chinese-calendar/test_generate.py` (fehlende Dateien/Starts, Jahres-/Leap-Mismatch, Konfliktaudit, Verify-Fehlerstatus, Zeitketten-/Toleranzpruefung); `ChineseCalendarEngineTest.documentedHkoConflictsRemainOnTheUtcPlus8CivilDate`. | `test_generate.py`: 7 Tests, 0 Fehler. Generator `--verify-reference`: 2.458 exakt + 3 auditiert = 2.461, 0 fehlende Referenzen, `complete=True`, Exit 0. Manifest-Output/-Coverage und der SHA-256 der geprueften Kotlin-Daten stimmen mit dem Manifest. |
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
| P2 Zeitketten-/Referenzaudit | `tools/chinese-calendar/generate.py`: strukturierte NASA-Referenz, explizite UTC-/UTC+8-Offsets, Gleichheit beider Instants und dokumentierte 60-s-Toleranz. | `test_conflict_instants_must_agree_and_use_structured_reference_tolerance` (manipulierte Zeiten, fehlende Zeitzonen, Toleranzverletzung). | `test_generate.py`: 7 Tests, 0 Fehler; `--verify-reference`: 2.458 + 3 = 2.461, 0 fehlend, Exit 0. |
| P2 NASA-Wert/HKO-Erklaerung | `reference-conflicts.json`: 1914-11-17 auf 16:02 UT korrigiert; 1916/1920 auf 16:05 UT bestaetigt; die drei HKO-Zeilen als bewusste UTC+8-Produktentscheidung dokumentiert (Pekinger Ortszeit erklaert sie, eine pauschale LMT-Umstellung verschiebt aber auch 1906-04-24). Manifest, Spezifikation, Quality- und Release-Doku synchronisiert. | `test_1914_reference_uses_the_actual_nasa_minute_and_records_hko_difference`. | Generator-/Manifestpruefung und Python-Tests gruen; Remote-CI `43439808516`/`43439808420` success. |
| P2 Today-Callback | Kein Produktionscode noetig; Test tauscht zwei echte `FixedToday`-Callback-Instanzen nach dem Restore aus. | `ChineseComposeRegressionTest.chineseMonthStateSurvivesRestorationAndUsesAReplacedTodayCallback`. | AndroidTest kompiliert; Geraeteausfuehrung lokal nicht moeglich; Remote-Instrumentation `43439808384` success, keine separate chinesische Geraetematrix. |

Diese Nachbesserungen sind lokal getestet. Remote-CI fuer `952bbb6` (enthaelt `5cf640d`):
verification `43439808516`, publishing `43439808420`, instrumentation `43439808384`, tablet
`43439808435` - alle success. Geraete-, Fold-, TalkBack-, Benchmark-, Advisory- und
JitPack-Gates bleiben offen.

### Review-Nachbesserungen 2026-11-13 (Basis `54bf23d`)

| Finding | Fix (Datei) | Regressionstest | Ausgefuehrte Pruefung |
|---|---|---|---|
| P1 Viewport-Recovery | `calendarviewsample/.../CalendarSampleViewport.kt`: ein `LaunchedEffect` setzt bei gueltigem `desired` und fehlgeschlagenem Fenster das Center auf `desired`; ungueltiges `desired` bleibt im Fehlerzweig. | `ChineseComposeRegressionTest.invalidCenterRecoversWhenAValidAnchorArrivesWithoutCompositionRestart` (ungueltiger Center, dann gueltiger Anchor ohne Neustart) und `switchingFromGregorianOverlayToChineseMonthsRebuildsTheWindow`. | `compileDebugKotlin`, `compileDebugAndroidTestKotlin` und `testDebugUnitTest` gruen; UI-Tests lokal nicht ausfuehrbar (kein Emulator), Remote-Instrumentation nach Push offen. |
| P2 README/Consumer | `README.md`: `ChineseDate.of(2024, monthNumber = 1, isLeapMonth = false, dayOfMonth = 1)`. Neuer isolierter JVM-Consumer unter `tools/consumer-check/jvm-consumer` kompiliert und fuehrt genau dieses Beispiel aus. | JVM-Consumer `Main.kt` mit Asserts fuer Epoch 19763, Roundtrip, 13 Monate und `2023-2L`. | `:jvm-consumer:run` mit `/tmp/m2-review-final` als einziger Kalender-Repository-Quelle: `JVM consumer OK`, Exit 0. |
| P2 Publishing-CI | `scripts/verify-publication.sh`: exakte AAR-/JAR-/Klib-/POM-/Module-/Sources-Pfade, ZIP-/Klassen-/Klib-Inhalte, alle `files`-/`available-at`-Referenzen der `.module`-Dateien sowie POM-/BOM-Abhaengigkeiten. Fehlende Target-Binaries und gebrochene Metadatenreferenzen schlagen fehl. Workflow ruft den Check und zwei isolierte Consumer auf. Die spaetere URL-Nachbesserung auf Basis `acf3bfc` loest `files[].url` gegen die `.module`-Datei auf und prueft `name` separat. | Negativtests: geloeschte JVM-JAR, manipulierte Klib-Referenz, fehlende `url` und kaputte `url` bei vorhandener `name`-Datei fuehren jeweils zu Exit 1. Positivtest: vollstaendige lokale Publikation Exit 0. | `scripts/verify-publication.sh /tmp/m2-review-final 2.4.0` passed; Android-Consumer `:android-consumer:compileDebugKotlin` und JVM-Consumer `:jvm-consumer:run` aus `tools/consumer-check` erfolgreich. |
| P2 API-Guard | `scripts/api-check.sh`: Version aus `build.gradle.kts`, exakter Pfad `calendar-chinese-native-jvm-${VERSION}.jar`, genau ein nicht-Sources-JVM-Jar; abweichende/stale Jars sind ein Fehler. | Manueller Negativtest mit zusaetzlichem `-jvm-2.3.0.jar`: Exit 1 mit Liste der gefundenen Jars; danach entfernt. | `bash scripts/api-check.sh` nach Entfernen des stale Jars: `Public API matches the checked-in dumps`, Exit 0. |
| Zusatzfund Publikationsmetadaten | `calendar-chinese-native/build.gradle.kts`: Root-Metadata-JAR, Root-Sources-JAR und linuxX64-Klib werden auf die in Gradle Module Metadata referenzierten Dateinamen normalisiert. Vorher referenzierten die `.module`-Dateien `calendar-chinese-native-metadata-*.jar`/`-kotlin-*-sources.jar` und `...linuxX64Main-*.klib`, die im Repository nicht unter diesen Namen lagen. | `scripts/verify-publication.sh` prueft jede Metadatenreferenz; der Android-/JVM-Consumer loest die Varianten aus dem publizierten Repo auf. | Isolierte Publikation und beide Consumer erfolgreich; der Check schlaegt bei manipulierter Referenz fehl. |

Lokale Regression: `test :calendar-chinese-native:jvmTest :calendar-chinese-native:linuxX64Test`
und `:calendarviewsample:assembleDebug` BUILD SUCCESSFUL; 193 Android-JVM- + 14 KMP-JVM-Tests
sowie 14 linuxX64-Tests ohne Fehler. Die beiden neuen Compose-Tests sind kompiliert, aber ohne
lokalen Emulator nicht ausgefuehrt. Remote-CI fuer `09e3a73`: verification `43447073439`,
publishing `43447073475`, instrumentation `43447073390`, tablet `43447073539` - alle success.

### Review-Nachbesserung 2026-11-13 (Basis `acf3bfc`)

| Finding | Fix (Datei) | Regressionstest | Ausgefuehrte Pruefung |
|---|---|---|---|
| P1 Publishing-Pruefluecke | `scripts/publication_metadata.py` (importierbarer Check) und `scripts/verify-publication.sh`: `files[].url` ist Pflicht, wird per `urlsplit` validiert und relativ gegen die `.module`-Datei aufgeloest; das Ziel muss existieren und nicht leer sein. `files[].name` wird nur separat auf Vorhandensein geprueft und nie als Pfad verwendet. `available-at.url` nutzt denselben Check. Nicht-lokale URLs werden als nicht pruefbar abgelehnt. | `scripts/test_publication_metadata.py`: gueltige relative URL bei abweichendem `name`, fehlende URL, kaputte URL bei vorhandener `name`-Datei und fehlender Name. Zusaetzlich Integrationstests gegen eine kopierte vollstaendige Publikation mit entfernter bzw. gebrochener URL. | `python3 -m unittest discover -s scripts -p 'test_*.py'`: 12 Tests gruen; Verifier gegen `/tmp/m2-review-final`: Exit 0; beide manipulierten Module: Exit 1 mit `missing url` bzw. `missing/empty`. |

Plattformabdeckung dieses Nachweises: Der JVM-Consumer loest die Root-Modul-Metadaten und die JVM-Variante auf und fuehrt `ChineseDate.of` aus. Der Android-Consumer kompiliert die Compose-Fassade und ihre veroeffentlichten AAR-Abhaengigkeiten. Die linuxX64-Klib wird auf Datei-/Archiv- und Metadatenebene geprueft, aber in diesem Lauf **nicht von einem Kotlin/Native-Consumer konsumiert**. Die Pruefung ist kein JitPack- oder Remote-Consumer-Nachweis.

Remote-CI fuer `9fd3b50`: verification `43459739249`, publishing `43459739473`, instrumentation `43459739435`, tablet `43459739230` - alle success.

### Nachbesserung 2026-11-13 (Basis `9fd3b50`)

| Punkt | Umsetzung | Nachweis |
|---|---|---|
| HKO-Datenkonvention | Bewusste UT+8-Produktentscheidung: Die drei HKO-Zeilen sind mit Pekinger Ortszeit vor 1929 vereinbar, eine pauschale LMT-Umstellung verschiebt aber auch 1906-04-24. `unresolved` ist entfernt; Daten und Ausgabe-Hash bleiben unveraendert. | Generator `--verify-reference`: 2.458 exakt + 3 dokumentiert = 2.461, `complete=True`; `ChineseCalendarData.kt` byte-identisch; Manifest-Hash `2636c523...`; Audit der LMT-Variante lokal ausgefuehrt. |
| Default-Monat | `defaultChineseMonth` klemmt den Standard-`initialMonth` auf `MIN_EPOCH_DAY..MAX_EPOCH_DAY`; bei einem Host-Datum ausserhalb des Datenbereichs wird der naechste unterstuetzte Monat angezeigt. | `ChineseComposeDataTest.defaultMonthIsClampedToTheSupportedDataSet` mit `Long.MIN_VALUE`, `Long.MAX_VALUE` und einem Datum im Bereich; Fokustest gruen, XML `failures="0"`. |
| Workflow-Version | `.github/workflows/android-publishing.yml` liest die Version aus `build.gradle.kts`, prueft sie auf leer und verwendet sie fuer Verifier und Native-POM; keine hartkodierte `2.4.0`. | YAML-Parse OK; lokale Versionsermittlung `2.4.0`; `grep` findet keine hartkodierte Version. |
| Daten-Hash-Test | `test_checked_in_kotlin_source_matches_manifest_sha256` vergleicht den SHA-256 von `ChineseCalendarData.kt` mit `manifest.json`. | `python3 -m unittest discover -s tools/chinese-calendar -p 'test_*.py'`: 7 Tests, 0 Fehler. |

Commits: `95c1cc3` (vier Nachbesserungen) und `a138306` (interner Helper aus dem JVM-API-Dump gefiltert). Der erste Verification-Run fuer `95c1cc3` schlug am API-Guard fehl, weil der interne Top-Level-Helper als JVM-Methode sichtbar war; der API-Dump blieb unveraendert. Remote-CI fuer `a138306`: verification `43472900415`, publishing `43472900052`, instrumentation `43472901643`, tablet `43472900502` - alle success.

#### Weiterhin offene Gates

- [ ] JitPack-Build `2.4.0` und JitPack-Only-Consumer ohne `mavenLocal`/`includeBuild`. Teilnachweis: 2.3.0 und Commit `7a51e24` bestanden; der 2.4.0-Tag-/BOM-Consumer ist ohne Tag blockiert (siehe unten).
- [ ] Chinesische Geraete-/Fold-/TalkBack-/Grossschrift-/RTL-Matrix. Die Remote-Instrumentation auf API 29 ist kein Ersatz fuer reale Geraete/Folds; ein lokaler Emulator scheitert an fehlender KVM-Berechtigung.
- [ ] Benchmarks: JVM-Baseline und provisorische Budgets sind erhoben; physische Geraeteabnahme und finale Budgetfreigabe stehen aus.
- [ ] Advisory-/CVE-Scan: Laufzeitgraph ohne Treffer; bekannte Buildscript-Findings benoetigen eine Maintainer-Akzeptanz oder einen AGP-Upgrade.
- [ ] Merge, Tag `2.4.0` und GitHub Release nur nach separater ausdruecklicher Freigabe; bis dahin kein Tag und keine Publikation.

### Release-Gate-Nachtrag 2026-11-13

#### JitPack

| Punkt | Ergebnis |
|---|---|
| JitPack `2.3.0` | Buildlog HTTP 200; listet `library`, `calendar-gregorian`, `calendar-hijri`, `calendar-solarHijri`, `calendar-solarHijri-native` und `calendar-bom`. Ein temporaerer Android-Consumer mit BOM `2.3.0` und `library` kompilierte JitPack-only ohne `mavenLocal`/`includeBuild`: `BUILD SUCCESSFUL in 54s`, `:android-consumer:compileDebugKotlin` Exit 0. |
| JitPack `2.4.0` Tag | Noch nicht vorhanden: der Buildlog-Endpunkt antwortet HTTP 404 mit `Tag or commit not found: 2.4.0`. Ohne separaten Tag-/Release-Auftrag ist der echte 2.4.0-Consumer nicht ausfuehrbar. |
| JitPack Commit `7a51e24` | Commit-Build erfolgreich; Buildlog listet BOM, `calendar-chinese-native` (Metadata, JVM, linuxX64), `calendar-chinese-compose` und die fuenf bestehenden Module. Der direkte JVM-Engine-Consumer gegen `calendar-chinese-native:7a51e24` lief ohne BOM: `JVM consumer OK: newYear=2024-1-1 epochDay=19763 months=13`, Exit 0. |
| JitPack-BOM bei Commit-Hash | **Blockiert:** Das von JitPack veroeffentlichte BOM-POM `7a51e24` hat zwar `<version>7a51e24</version>`, constrainiert die Module aber weiterhin auf `2.4.0`. JitPack schreibt bei Commit-Builds die eigene POM-Version um, nicht die `dependencyManagement`-Werte. Der BOM-Consumer scheitert daher mit `Could not find ...calendar-chinese-native:2.4.0`; das ist kein lokaler Repository- oder Cache-Fehler. Bei einem echten Tag `2.4.0` stimmen Constraint und JitPack-Version ueberein. |

Reproduktion der JitPack-Nachweise:

```bash
curl -fsSL https://jitpack.io/com/github/mortezasun/android-calendarview/2.3.0/build.log
curl -fsSL https://jitpack.io/com/github/mortezasun/android-calendarview/7a51e24/build.log
curl -sS -o /dev/null -w '%{http_code}\n' https://jitpack.io/com/github/mortezasun/android-calendarview/2.4.0/build.log

# 2.3.0: temporaere Kopie von tools/consumer-check, nur google()/mavenCentral()/jitpack.io,
# BOM 2.3.0 + library, dann:
./gradlew -p /tmp/jitpack-230-consumer :android-consumer:compileDebugKotlin --refresh-dependencies

# 2.4.0-Commit: BOM entfernen und direkt calendar-chinese-native:7a51e24 anfordern, dann:
./gradlew -p /tmp/jitpack-240-engine :jvm-consumer:run --refresh-dependencies

# Nach separater Tag-/Release-Freigabe: Tag 2.4.0 erstellen, JitPack-Build abwarten und
# tools/consumer-check mit JitPack-Repository (ohne mavenLocal/includeBuild) ausfuehren.
```

#### Benchmarks

Der Test `ChineseJvmBenchmarkTest.reportJvmPerformanceBaseline` ist Teil der JVM-Tests und gibt
reproduzierbare `BENCHMARK`-Zeilen aus; er hat bewusst keine zeitbasierte Assertion. Gemessen wurde
am 2026-11-13 auf der Freebuff-Cloud-VM (Linux amd64, 8 vCPU, AMD-KVM, JDK 21.0.13 Temurin,
Max-Heap 512 MiB), Median aus fuenf Messungen nach zwei Aufwaermlaeufen. Die JVM-Allokationen
kommen aus dem HotSpot-Thread-Allocation-Counter.

| Operation | Messwert | Allokation | Datensatz pro Messung |
|---|---:|---:|---:|
| Konvertierung Epoch Day -> chinesisch -> Epoch Day | 58,77 ns/Op | 56,01 B/Op | 72.674 Tage |
| Monatsaufbau (`chineseMonthData`) | 16.074,47 ns/Monat | 15.630,53 B/Monat | 2.461 Monate |
| 13-Monats-Heatmap (`chineseYearHeatmap(2023)`) | 355.526 ns/Jahr | 248.328 B/Jahr | 13 Monate |
| Wiederholte Navigation (`next`/`previous`) | 53,79 ns/Schritt | 48,01 B/Schritt | 100.000 Schritte |

Artefaktgroessen des lokalen Release-Builds: Engine-Metadata-JAR 6.989 B, Engine-JVM-JAR 32.436 B,
linuxX64-Klib 41.288 B, Compose-AAR 36.116 B. Provisorische JVM-Budgets mit mindestens 6-fachem Laufzeit- und Allokationspuffer:
Konvertierung <= 1.000 ns und <= 512 B/Op; Monatsaufbau <= 100.000 ns und <= 100 KB/Op;
Heatmap <= 5 ms und <= 1,5 MB/Jahr; Navigation <= 1.000 ns und <= 512 B/Schritt. Diese Budgets
sind noch keine physische Geraeteabnahme; sie muessen vor RC auf der benannten Zielhardware
bestaetigt oder angepasst werden.

```bash
./gradlew :calendar-chinese-compose:testDebugUnitTest --tests '*ChineseJvmBenchmarkTest*'
```

#### Advisory-/CVE-Scan

Gescannt wurden die aufgeloesten Laufzeitgraphen von Engine (`jvmRuntimeClasspath`), Compose
(`releaseRuntimeClasspath`) und Sample (`debugRuntimeClasspath`): 101 eindeutige Maven-Koordinaten.
Die OSV-`querybatch`-Abfrage (POST, `https://api.osv.dev/v1/querybatch`, Maven-Ökosystem) lieferte
**0 betroffene Laufzeitkoordinaten**.

Zusaetzlich wurden 334 Koordinaten aus Laufzeit- plus `buildEnvironment`-Graph geprueft. Die
Treffer liegen ausschliesslich im AGP-9.5.1-Buildscript-Classpath, nicht in den ausgelieferten
AARs/JARs: Guava 33.4.0-jre (`GHSA-xxph-c9ww-hj94`, fixed 33.7.2), commons-lang3 3.16.0
(`GHSA-j288-q9x7-2f5v`, fixed 3.18.0), jose4j 0.9.5 (`GHSA-3677-xxcr-wjqv`, fixed 0.9.6),
bcpkix/bcprov-jdk18on 1.80.2 (`GHSA-wg6q-6289-32hp`, `GHSA-9pwp-9qqc-pr26`,
`GHSA-qp49-qgx5-5m26`, fixed 1.84/1.85) und jdom2 2.0.6 (`GHSA-2363-cqg2-863c`, fixed 2.0.6.1).
Der Laufzeitgraph enthaelt diese Pakete nicht; `com.google.guava:listenablefuture:1.0` ist nur ein
leerer Kompatibilitaetsmarker. Ein `kotlin-stdlib {strictly ...}`-Treffer war ein Parserartefakt
aus der Gradle-Ausgabe, keine reale Koordinate; aufgeloest ist Kotlin 2.4.21, oberhalb der
betroffenen Bereiche. Fuer 2.4.0 wurde kein riskanter Buildscript-Zwangsumstieg vorgenommen;
die Findings benoetigen eine dokumentierte Maintainer-Akzeptanz (Owner/Expiry) oder einen
separaten AGP-Upgrade. Der Laufzeit-Scan ist damit bestanden, der Build-Tooling-Teil bleibt offen.

```bash
./gradlew :calendar-chinese-native:dependencies --configuration jvmRuntimeClasspath \
  :calendar-chinese-compose:dependencies --configuration releaseRuntimeClasspath \
  :calendarviewsample:dependencies --configuration debugRuntimeClasspath --console=plain > /tmp/gradle-deps.log
# Danach die group:artifact:version-Koordinaten per OSV /v1/querybatch (Maven) pruefen.
```

#### Geraete-, Fold-, TalkBack- und Emulator-Nachweis

`adb devices` ist leer und es gibt kein lokales Geraet. Der nachinstallierte API-29-Emulator
konnte nicht booten, weil der Benutzer nicht in der Gruppe `kvm` ist:
`ProbeKVM: This user doesn't have permissions to use KVM (/dev/kvm); The KVM line is kvm:x:994:`.
Ein x86_64-Emulator verlangt Hardwarebeschleunigung; ohne KVM-/Gruppenfreigabe ist der lokale
Lauf nicht moeglich. Die Remote-Instrumentation und -Tablet-Suite auf API 29 sind gruen, ersetzen
aber weder reale Foldables noch TalkBack/Grossschrift/RTL. Die Matrix bleibt daher offen; die
bestehenden UI-Tests sind kompiliert und remote ausgefuehrt, nicht als chinesische Geraetematrix.

```bash
adb devices
# Nach KVM-Freigabe durch den Administrator (z. B. Benutzer in Gruppe kvm, neue Login-Sitzung):
emulator -avd <avd-name> -no-window -no-audio -no-boot-anim -gpu swiftshader_indirect
./gradlew :calendarviewsample:connectedDebugAndroidTest
```

Manuell verbleiben: Telefon/Tablet/Foldable mit Buch-/Tabletop-Posture, Landscape/Split-Screen,
TalkBack mit chinesischen und englischen Labels, grosse Schrift, RTL, deaktivierte Tage, sichtbare
Schaltmonatkennzeichnung und Touch-Target-Messung. Die konkrete Checkliste steht in
`docs/CHINESE_CALENDAR_QUALITY.md`.

#### Ergebnis der lokalen Regressionspruefung

`./gradlew test :calendar-chinese-native:linuxX64Test` war erfolgreich; der fokussierte
Benchmark-Test ebenfalls. `bash scripts/api-check.sh` meldet `Public API matches the checked-in
dumps.`; die Python-Suites liefen mit 7 + 12 Tests ohne Fehler. Kein Merge, Tag oder Release
wurde erstellt.

## Arbeitsregel fuer die Implementierung

Pro Phase: kleinste zusammenhaengende Aenderung, Tests, Diff-Review, Dokumentationsupdate und
klarer Bericht ueber ausgefuehrte/nicht ausgefuehrte Pruefungen. Keine Checkbox ohne
Ergebnisnachweis abhaken. Ein fehlender Runner, eine ungepruefte Lizenz oder ein gebrochener
Remote-Consumer ist ein offener Blocker, keine stillschweigende Ausnahme.

## Nachweise

Details, Kommandos und Ergebnisse: [PROJECT_STATUS.md](docs/PROJECT_STATUS.md#current-development-240-chinese-calendar-unreleased-2026-11-13)
und [releases/2.4.0.md](docs/releases/2.4.0.md#verifikation-2026-11-13-lokaler-arbeitsbaum).
