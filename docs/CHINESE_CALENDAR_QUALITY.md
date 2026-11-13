# 2.4.0: Testplan, Anfaelligkeiten und Sicherheitsgates

Status: Kernmatrix am 2026-11-13 lokal (JDK 21, JVM + linuxX64) und in Remote-CI auf `1ab841e70b6fb7ab35b16e970715b500b91f95a2` sowie auf dem Review-Fixstand `e80c00f` ausgefuehrt. Die Nachbesserungen auf `6fac725` (`5cf640d`) sind lokal geprueft; der gepushte Baum `952bbb6` ist in allen vier Remote-Workflows success. Am 2026-11-13 wurden zusaetzlich eine JVM-Benchmark-Baseline und ein OSV-Advisory-Scan ausgefuehrt. Die chinesische Geraete-/Fold-/TalkBack-/Grossschrift-/RTL-Matrix bleibt offen. Dieser Plan behauptet weder Schwachstellenfreiheit des Build-Toolings noch eine abgeschlossene physische Geraeteabnahme.

## Funktionale Pflichtmatrix

- [x] Jeden unterstuetzten Epoch Day nach chinesisch und zurueck konvertieren; jeden gueltigen chinesischen Tag umgekehrt pruefen. (72.674 Tage, `wholeRangeRoundTrips`.)
- [x] Zusaetzlich unabhaengige Referenzdaten vergleichen. Roundtrips allein beweisen keine richtige Kalenderzuordnung. (Generator: 2.458 exakt + 3 auditierte Abweichungen = 2.461 HKO-Monatsstarts, 0 fehlende Referenzen; die Abweichungen sind mit strukturierter NASA/GSFC-Neumondzeit und 60-s-Toleranz belegt; `--verify-reference` verlangt vollstaendige Datei-/Startabdeckung, Jahres-/Schaltmonatsstatus, identische UTC-/UTC+8-Instants und explizite Zeitzonen. Die HKO-Zeilen sind mit Pekinger Ortszeit vor 1929 vereinbar; die Library bleibt bewusst bei UTC+8 und behandelt sie als dokumentierte historische Referenzunterschiede.)
- [x] Alle Jahres-/Monatswechsel, 29-/30-Tage-Monate, alle Schaltmonate und insbesondere 2033/2034 gegen Referenz pruefen. (2.461 Monate lueckenlos; Leap-Fixtures inkl. 2033/2034.)
- [x] Randjahre, Randmonate, Nebenmonatszellen, min/max +/- 1 und ungueltige Schaltmonate pruefen.
- [x] Gregorianische Jahrhundertgrenzen einschliesslich 1900/2000/2100 soweit unterstuetzt bzw. als ablehnender Grenztest abdecken. (Range endet 2100-02-08; 1900/2100 ausserhalb werden abgelehnt.)
- [x] Grosse positive/negative Operanden, Int-/Long-Grenzen, Overflow und ungueltige Tageszahlen pruefen.
- [x] UTC, UTC+08, Europe/Berlin mit Sommerzeit und weitere Geraetezonen liefern fuer denselben Epoch Day dasselbe Engine-Ergebnis. Keine Millisekunden/86400000-Tagesrechnung. (Engine nutzt ausschliesslich Epoch Days und enthaelt keine java.time-/Zeitzonen-Nutzung; Jar-Scan.)
- [x] Monatsarithmetik mit Schaltmonaten und Tag-Clamping, naechster/vorheriger Monat sowie Anfang/Ende testen.
- [x] Identische Fixtures auf JVM und einem ausgefuehrten Kotlin/Native-Target. Fuer beworbenes iOS: Simulator-Runtime und unabhaengiger Consumer auf macOS. Nur Kompilieren ist kein Runtime-Test. (JVM 14 + linuxX64 14; iOS nicht beworben.)

## Compose- und Regressionsmatrix

- [ ] None/Single/Multiple/Range, deaktivierte Tage und Marker; Epoch-Day-Identitaet beim Wechsel zwischen allen Kalendern erhalten.
- [x] Navigation normaler Monat -> Schaltmonat -> Folgemonat, Jahreswechsel, Grenzen und Heute ausserhalb des Bereichs. (JVM-Bounds-Tests, Boundary-Window-Tests fuer beide Engine-Grenzen sowie Compose-Single-/Pager-Regressionstests ergaenzt; Geraete-Ausfuehrung offen.)
- [x] Saver-Roundtrip und echte StateRestorationTester-/Activity-Recreation-Tests einschliesslich Schaltflag und alter gespeicherter Sample-Zustaende. Neue Enum-Eintraege duerfen alte ordinalbasierte Saves nicht umdeuten. (`ChineseMonthSnapshot`-Decode-Tests und `StateRestorationTester`-Test fuer den Schaltmonat mit Austausch zweier Callback-Instanzen ergaenzt; lokal kompiliert, Remote-Instrumentation der Sample-Suite auf API 29 erfolgreich, aber keine separate chinesische Geraetematrix.)
- [ ] Monat, Woche, Agenda, Pager/Liste und Jahresheatmap mit 12 und 13 Monaten.
- [ ] Telefon/Tablet, schmale Breite, Landscape, Mehrspalten, Split-Screen, simulierte Folds und kontinuierliche Layoutwechsel. Reale Foldable-Tests separat ausweisen.
- [ ] LTR/RTL, chinesische/englische Beschriftung, grosse Schrift, TalkBack, deaktivierte Semantik und sichtbare Schaltmonatkennzeichnung.
- [ ] Bestehende Touch-Target-Regeln und bekannte schmale-Viewport-Einschraenkungen erhalten; keine pauschale 48-dp-Garantie ohne Messung.
- [x] Regression aller vorhandenen Module: Tests, Lint, Sample,   API-Guard und eigenstaendige Consumer. (210 JVM inkl. Benchmark + 14 Native, Lint, Sample-APK, API-Guard, JVM- und Android-Consumer.)

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
| Bekannte verwundbare Abhaengigkeiten | OSV-Scan 2026-11-13: 101 Laufzeitkoordinaten ohne Treffer; 334 Laufzeit-/Buildscript-Koordinaten mit Treffern ausschliesslich im AGP-Buildscript (nicht ausgeliefert). Build-Tooling-Akzeptanz oder AGP-Upgrade bleibt eine Maintainer-Entscheidung; Pruefsummen sind kein CVE-Scan |
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
| JVM | `test :calendar-chinese-native:jvmTest`: 210 JVM-Tests (196 Android JVM + 14 KMP JVM), 0 Fehler, 0 Skip; enthaelt den neuen Benchmark-Reporting-Test |
| Kotlin/Native | `:calendar-chinese-native:linuxX64Test`: 14 Tests, 0 Fehler, 0 Skip |
| Compose-Daten/State | 12 Tests: Monat/Woche/State/Saver/Heatmap/Overlay und Benchmark-Reporting, inkl. 12/13 Monate, Bounds und Decode-Validierung |
| Sample | `assembleDebug`, 60 Unit-Tests und `compileDebugAndroidTestKotlin` erfolgreich; neue Single-/Pager-Boundary-Tests nur kompiliert |
| API/Consumer | bestehende Dumps `0` Diff; Engine-JVM-Consumer; Android-BOM-Consumer `assembleDebug` |
| Python-Referenzpruefung | 7 Tests: fehlende Dateien/Starts, Jahres-/Schaltmonatsstatus, dokumentierter Konflikt, Verify-Fehlerstatus, Instant-Gleichheit/Zeitzonen/Toleranz, NASA-1914-Wert und SHA-256 der Kotlin-Daten gegen das Manifest |
| Remote-CI | Evidenzbaum `1ab841e`: verification `43401491446`, publishing `43401491433`, instrumentation `43401491452`, tablet `43401491675`; Fixstand `e80c00f`: verification `43412948314`, publishing `43412948284`, instrumentation `43412948316`, tablet `43412948286`; Nachbesserungsbaum `952bbb6` (enthaelt `5cf640d`): verification `43439808516`, publishing `43439808420`, instrumentation `43439808384`, tablet `43439808435` - alle success. |
| JitPack | 2.3.0-Buildlog HTTP 200; Android-BOM-Consumer `BUILD SUCCESSFUL`; Commit `7a51e24` mit BOM, Engine (Metadata/JVM/linuxX64), Compose und fuenf bestehenden Modulen; direkter Engine-JVM-Consumer `JVM consumer OK`, Exit 0. 2.4.0-Tag/BOM-Consumer ohne Tag blockiert; Commit-BOM constrainiert weiterhin `2.4.0`. |
| Benchmark | JVM-Baseline auf Freebuff-VM, JDK 21.0.13, 8 vCPU: Konvertierung 58,77 ns/Op, Monatsaufbau 16.074,47 ns/Monat, 13-Monats-Heatmap 355.526 ns/Jahr, Navigation 53,79 ns/Schritt; Allokationen 56,01 B / 15.630,53 B / 248.328 B / 48,01 B pro Operation. Fokustest gruen; physische Geraeteabnahme offen. |
| Advisory | 101 Laufzeitkoordinaten, OSV `querybatch`: 0 Treffer. 334 Laufzeit-/Buildscript-Koordinaten: Treffer nur im AGP-9.5.1-Buildscript (Guava, commons-lang3, jose4j, Bouncy Castle, JDOM2); nicht in AAR/JAR. Maintainer-Akzeptanz/AGP-Upgrade offen. |
| Emulator | API-29-x86_64-Image und Emulator installiert; Boot scheitert an `ProbeKVM: This user doesn't have permissions to use KVM (/dev/kvm)`, Benutzer nicht in Gruppe `kvm`. Keine lokale Geraeteausfuehrung. |
| Gate-Evidenz-Remote-CI | `4ce4d3f`: verification `43479911419`, publishing `43479911387`, instrumentation `43479911196`, tablet `43479911275` - alle success. |
| Offen | chinesische UI-Interaktion auf realen Geraeten/Folds, TalkBack/Grossschrift/RTL, Touch-Target-Messung, physische Benchmark-Abnahme, finale Budgetfreigabe, Buildscript-Advisory-Akzeptanz, Tag/JitPack-2.4.0 |

## Benchmark-Budgets (provisorisch, 2026-11-13)

Die Messung ist eine JVM-Baseline, keine Zielgeraete-Abnahme. Die Budgets sind bewusst mit mindestens 6-fachem Puffer zu den gemessenen Medianen gesetzt und werden erst nach einer Messung auf
der benannten Zielhardware final eingefroren.

| Operation | Messwert (Median) | Provisorisches Budget |
|---|---:|---:|
| Konvertierung Hin/Rueck | 58,77 ns/Op, 56,01 B/Op | <= 1.000 ns/Op, <= 512 B/Op |
| Monatsaufbau | 16.074,47 ns/Monat, 15.630,53 B/Monat | <= 100.000 ns/Monat, <= 100 KB/Monat |
| 13-Monats-Heatmap | 355.526 ns/Jahr, 248.328 B/Jahr | <= 5 ms/Jahr, <= 1,5 MB/Jahr |
| Navigation | 53,79 ns/Schritt, 48,01 B/Schritt | <= 1.000 ns/Schritt, <= 512 B/Schritt |

Der Benchmark ist `ChineseJvmBenchmarkTest`; er gibt die Rohwerte aus und hat keine
zeitbasierte Assertion. Der Testlauf ist Teil von `./gradlew test`, der fokussierte Befehl ist:

```bash
./gradlew :calendar-chinese-compose:testDebugUnitTest --tests '*ChineseJvmBenchmarkTest*'
```

## Manuelle Geraete-/Accessibility-Matrix

Mindestens auf einem Telefon, einem Tablet und einem Foldable (Buch- und Tabletop-Posture)
ausfuehren, jeweils mit chinesischem Monat, Schaltmonat, Jahresheatmap und
`chineseGregorianGrid`:

- [ ] Telefon hoch/quer, Tablet hoch/quer, Split-Screen und kontinuierliche Breitenaenderung; keine abgeschnittenen Tage oder Header.
- [ ] Foldable: Inhalt wechselt an der Falz, Buch-/Tabletop-Posture, keine Bedienelemente unter dem Scharnier; synthetische `sampleLocalFold`-Tests sind kein Ersatz.
- [ ] TalkBack: chinesische und englische Labels, ISO-Datum im Accessibility-Text, Schaltmonat `M2L`/`闰`, deaktivierte Tage, Auswahl und Heute; Fokusreihenfolge im Grid.
- [ ] Grosse Schrift (mind. 200 %) und RTL (Arabisch/Hebraeisch): kein Clipping, korrekte Leserichtung, Touch-Targets messbar (nicht pauschal 48 dp behaupten).
- [ ] Wiederholte Navigation ueber normale Monate, Schaltmonate und Jahresgrenzen; keine Recomposition, die den gesamten Bereich scannt.
- [ ] Activity-Recreation/Rotation mit chinesischem Monat, Auswahl und Gregorian-Overlay; Saver darf den Schaltmonat nicht verlieren.

Fuer eine verfuegbare lokale/remote Geraeteverbindung:

```bash
adb devices
./gradlew :calendarviewsample:connectedDebugAndroidTest
```

Der lokale API-29-Emulator konnte wegen fehlender KVM-Gruppenberechtigung nicht booten; die
Remote-API-29-Suite ist gruen, deckt aber weder reale Foldables noch TalkBack ab.

## Nachweisformat

Jedes Ergebnis: Datum, exakter Commit, Toolchain/Target, Task/Befehl, Runner/Geraet, Ergebnis, Testanzahl/Skipzahl, Berichtspfad bzw. CI-Run und offene Findings. Nicht ausgefuehrte Tests bleiben offen. Historische gruenen Runs belegen keinen spaeter veraenderten Releasebaum.
