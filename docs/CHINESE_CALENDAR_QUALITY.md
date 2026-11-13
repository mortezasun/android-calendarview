# 2.4.0: Testplan, Anfaelligkeiten und Sicherheitsgates

Status: Kernmatrix am 2026-11-13 lokal (JDK 21, JVM + linuxX64) und in Remote-CI auf `1ab841e70b6fb7ab35b16e970715b500b91f95a2` ausgefuehrt; Review-Nachbesserungen
auf `6970b92` sind lokal getestet, aber noch nicht remote. Geraete-, UI-, Benchmark- und
Advisory-Pruefungen sind offen. Dieser Plan behauptet weder Schwachstellenfreiheit noch einen
aktuellen CVE-Scan.

## Funktionale Pflichtmatrix

- [x] Jeden unterstuetzten Epoch Day nach chinesisch und zurueck konvertieren; jeden gueltigen chinesischen Tag umgekehrt pruefen. (72.674 Tage, `wholeRangeRoundTrips`.)
- [x] Zusaetzlich unabhaengige Referenzdaten vergleichen. Roundtrips allein beweisen keine richtige Kalenderzuordnung. (Generator: 2.458/2.461 HKO-Monatsstarts exakt, drei auditierte Abweichungen mit NASA/GSFC-Neumondzeit; `--verify-reference` verlangt vollstaendige Datei-/Startabdeckung und Jahres-/Schaltmonatsstatus.)
- [x] Alle Jahres-/Monatswechsel, 29-/30-Tage-Monate, alle Schaltmonate und insbesondere 2033/2034 gegen Referenz pruefen. (2.461 Monate lueckenlos; Leap-Fixtures inkl. 2033/2034.)
- [x] Randjahre, Randmonate, Nebenmonatszellen, min/max +/- 1 und ungueltige Schaltmonate pruefen.
- [x] Gregorianische Jahrhundertgrenzen einschliesslich 1900/2000/2100 soweit unterstuetzt bzw. als ablehnender Grenztest abdecken. (Range endet 2100-02-08; 1900/2100 ausserhalb werden abgelehnt.)
- [x] Grosse positive/negative Operanden, Int-/Long-Grenzen, Overflow und ungueltige Tageszahlen pruefen.
- [x] UTC, UTC+08, Europe/Berlin mit Sommerzeit und weitere Geraetezonen liefern fuer denselben Epoch Day dasselbe Engine-Ergebnis. Keine Millisekunden/86400000-Tagesrechnung. (Engine nutzt ausschliesslich Epoch Days und enthaelt keine java.time-/Zeitzonen-Nutzung; Jar-Scan.)
- [x] Monatsarithmetik mit Schaltmonaten und Tag-Clamping, naechster/vorheriger Monat sowie Anfang/Ende testen.
- [x] Identische Fixtures auf JVM und einem ausgefuehrten Kotlin/Native-Target. Fuer beworbenes iOS: Simulator-Runtime und unabhaengiger Consumer auf macOS. Nur Kompilieren ist kein Runtime-Test. (JVM 13 + linuxX64 13; iOS nicht beworben.)

## Compose- und Regressionsmatrix

- [ ] None/Single/Multiple/Range, deaktivierte Tage und Marker; Epoch-Day-Identitaet beim Wechsel zwischen allen Kalendern erhalten.
- [x] Navigation normaler Monat -> Schaltmonat -> Folgemonat, Jahreswechsel, Grenzen und Heute ausserhalb des Bereichs. (JVM-Bounds-Tests und Compose-Regressionstest fuer Callback/Grenzen ergaenzt; Geraete-Ausfuehrung offen.)
- [x] Saver-Roundtrip und echte StateRestorationTester-/Activity-Recreation-Tests einschliesslich Schaltflag und alter gespeicherter Sample-Zustaende. Neue Enum-Eintraege duerfen alte ordinalbasierte Saves nicht umdeuten. (`ChineseMonthSnapshot`-Decode-Tests und `StateRestorationTester`-Test fuer den Schaltmonat ergaenzt; der Test ist kompiliert, nicht auf einem Geraet ausgefuehrt.)
- [ ] Monat, Woche, Agenda, Pager/Liste und Jahresheatmap mit 12 und 13 Monaten.
- [ ] Telefon/Tablet, schmale Breite, Landscape, Mehrspalten, Split-Screen, simulierte Folds und kontinuierliche Layoutwechsel. Reale Foldable-Tests separat ausweisen.
- [ ] LTR/RTL, chinesische/englische Beschriftung, grosse Schrift, TalkBack, deaktivierte Semantik und sichtbare Schaltmonatkennzeichnung.
- [ ] Bestehende Touch-Target-Regeln und bekannte schmale-Viewport-Einschraenkungen erhalten; keine pauschale 48-dp-Garantie ohne Messung.
- [x] Regression aller vorhandenen Module: Tests, Lint, Sample,   API-Guard und eigenstaendige Consumer. (205 JVM + 14 Native, Lint, Sample-APK, API-Guard, JVM- und Android-Consumer.)

## Risikoregister

| Risiko | Gegenmassnahme / Freigabegate |
|---|---|
| Falsche oder ungeklart lizenzierte Datentabellen | Herkunftsmanifest, Rechts-/Attributionspruefung, deterministischer Generator, zweiter Review; Blocker bis geklaert |
| Rundlauf funktioniert trotz falscher Zuordnung | Unabhaengiger Referenzvergleich ueber den gesamten zugesagten Bereich |
| Schaltmonat geht in Key/Saver/Navigation verloren | Vollstaendige Monatsidentitaet und explizite Schaltmonat-Fixtures |
| Zukuenftige Referenzabweichungen | HKO-Hinweise und Datensatzversion dokumentieren; keine universelle astronomische Genauigkeit versprechen |
| Overflow oder teure Aufrufe durch ungueltige Eingaben | Vorabvalidierung, checked arithmetic, begrenzte Suche/Iteration und Extremwerttests |
| Netzwerk-/Datenschutzrisiko | Keine Netzwerkberechtigung, Telemetrie, dynamischen Downloads oder Datumsprotokolle im neuen Modul |
| Verdeckte fremde Kalenderabhaengigkeit | Dependency-Graph, POM/Gradle-Metadata und Artefaktinhalt pruefen; Engine-Consumer ohne Compose/Android/andere Engines |
| Supply-Chain / manipulierte Artefakte | Feste Versionen, Repository-Scopes, Wrapper-Pruefsumme, gepruefte Gradle-Verifikationsmetadaten; keine dynamischen Releaseversionen |
| Bekannte verwundbare Abhaengigkeiten | Aktuellen Advisory-/CVE-Scan fuer aufgeloeste direkte/transitive Build- und Laufzeitabhaengigkeiten dokumentieren; Pruefsummen sind kein CVE-Scan |
| CI-Token oder Release-Manipulation | Minimale Workflow-Permissions, keine Secrets in fremden PR-Jobs, Actions auf gepruefte Commit-SHAs pinnen, Releasefreigabe trennen |
| API-/Consumer-Bruch | 2.3.0-Baseline unveraendert pruefen, neue API-Dumps, Publikations- und Binarkompatibilitaetstests |
| JitPack/KMP-Varianten nicht konsumierbar | Isolierte Publikation plus Remote-Consumer fuer jede beworbene Variante; kein mavenLocal/includeBuild als Ersatz |

## Sicherheits- und Leistungspolitik

Release-Blocker: unerklaerte Kalenderabweichung, kaputte Datenintegritaet, fehlende Lizenz, gebrochene bestehende API oder erforderliche Builds/Consumer. Anwendbare Critical-/High-Sicherheitsfindings muessen behoben oder vor Freigabe vom Maintainer mit Begruendung, Reichweite und Ablaufdatum ausdruecklich akzeptiert werden; keine stillen Ausnahmen. Medium/Low ebenfalls mit Owner und Frist dokumentieren. Keine neue Scan-Abhaengigkeit ohne Genehmigung einfuehren.

Gradle dependency verification prueft Integritaet/Signaturen, verhindert aber nicht die Nutzung bekannter verwundbarer Versionen. Automatisch erzeugte Vertrauensmetadaten muessen unabhaengig geprueft werden; blindes Bootstrapping kann kompromittierte Downloads festschreiben. Referenz: https://docs.gradle.org/current/userguide/dependency_verification.html

Vor Freigabe Benchmarks fuer Konvertierung, Monatsaufbau, 13-Monats-Heatmap und wiederholte Navigation auf benannter Hardware sammeln. Datensatzgroesse, Artefaktgroesse, Allokationen und Laufzeiten aufzeichnen und begruendete Budgets vor RC einfrieren. UI-Recomposition darf nicht den ganzen Referenzbereich scannen; keine unbeschraenkten globalen Caches oder Range-Interior-Listen.

## Ausfuehrung 2026-11-13

| Punkt | Ergebnis |
|---|---|
| JVM | `test :calendar-chinese-native:jvmTest`: 205 JVM-Tests, 0 Fehler, 0 Skip |
| Kotlin/Native | `:calendar-chinese-native:linuxX64Test`: 14 Tests, 0 Fehler, 0 Skip |
| Compose-Daten/State | 10 Tests: Monat/Woche/State/Saver/Heatmap/Overlay, inkl. 12/13 Monate, Bounds und Decode-Validierung |
| Sample | `assembleDebug`, Unit-Tests und `compileDebugAndroidTestKotlin` erfolgreich |
| API/Consumer | bestehende Dumps `0` Diff; Engine-JVM-Consumer; Android-BOM-Consumer `assembleDebug` |
| Python-Referenzpruefung | 4 Tests: fehlende Dateien/Starts, Jahres-/Schaltmonatsstatus, dokumentierter Konflikt, Verify-Fehlerstatus |
| Remote-CI | verification `43401491446`, publishing `43401491433`, instrumentation `43401491452`, tablet `43401491675`: alle success (Evidenzbaum `1ab841e`; Fixstand noch nicht remote) |
| Offen | chinesische UI-Interaktion auf realen Geraeten/Folds, TalkBack/Grossschrift/RTL, Touch-Target-Messung, Benchmarks, Advisory-/CVE-Scan, JitPack |

## Nachweisformat

Jedes Ergebnis: Datum, exakter Commit, Toolchain/Target, Task/Befehl, Runner/Geraet, Ergebnis, Testanzahl/Skipzahl, Berichtspfad bzw. CI-Run und offene Findings. Nicht ausgefuehrte Tests bleiben offen. Historische gruenen Runs belegen keinen spaeter veraenderten Releasebaum.
