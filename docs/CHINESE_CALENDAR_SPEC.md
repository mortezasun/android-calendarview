# Chinesischer Kalender: API- und Datenvertrag fuer 2.4.0

Status: implementiert auf Branch `update` (2.4.0-Releasekandidat); API-Dumps unter `api/`.

## Kalenderkonvention

Moderner chinesischer Lunisolarkalender mit dokumentierter ziviler UTC+08:00-Referenz. Keine Zusage historischer Ortszeitregeln oder Gleichheit mit vietnamesischen/koreanischen Varianten. Ein Epoch Day bezeichnet wie bisher das ganzzahlige ISO-Datum relativ zu 1970-01-01, nicht einen Instant und nicht eine lokale Mitternacht in Millisekunden. Dieselbe Tagesidentitaet muss in allen Kalendern erhalten bleiben.

`year` bezeichnet das gregorianische Jahr, in dem das betreffende chinesische Neujahr beginnt. Nicht ungeprueft mit ICU YEAR oder EXTENDED_YEAR gleichsetzen. `monthNumber` liegt in 1..12; Schaltmonate werden durch ein eigenes Flag unterschieden, niemals als Monat 13 kodiert.

## Implementierte Engine-API

Paket: `com.develotter.calendarview.calendars.chineseNative`.

- `ChineseMonth`: `year`, `monthNumber`, `isLeapMonth`; privater Konstruktor, Factory `of`, `fromEpochDay`.
- `ChineseDate`: `year`, `monthNumber`, `isLeapMonth`, `dayOfMonth`; Factory `of`, `fromEpochDay`.
- Beide sind keine data classes; Validierung im Konstruktor, manuelles `equals`/`hashCode`, kein `copy()`-Pfad.
- `ChineseCalendarEngine`: `MIN_YEAR`/`MAX_YEAR`, `MIN_EPOCH_DAY`/`MAX_EPOCH_DAY`, `isSupported`, `fromEpochDay`, `toEpochDay`, `monthsInYear`, `monthOf`, `lengthOfMonth`, `lengthOfYear`, `monthStartEpochDay`, `firstMonthOfYear`, `lastMonthOfYear`, `nextMonth`, `previousMonth`, `leapMonth`, `hasLeapMonth`.
- `ChineseDateOutOfRangeException : IllegalArgumentException` fuer nicht unterstuetzte Werte; ungueltige Monate/Tage werfen `IllegalArgumentException`.
- `ChineseFormat`/`ChineseTextStyle`: English, SimplifiedChinese, TraditionalChinese, Numeric.
- Tagesarithmetik ueber Epoch Days mit gepruefter Addition; Monatsarithmetik ueber die tatsaechliche Reihenfolge der 12/13 Monate. Bei gleichem Tag in kuerzerem Zielmonat wird auf dessen letzten Tag geklemmt; ein nicht vorhandener Schaltmonat wird niemals erfunden.
- Monate des Jahres werden chronologisch geliefert, z.B. regulaerer Monat N, Schaltmonat N, regulaerer Monat N+1. Keine fixe Liste mit genau zwoelf Eintraegen.
- Datumsvergleich erfolgt chronologisch; gleiche Monatsnummer mit anderem Schaltflag ist nicht derselbe Monat.

Invalides Datum: IllegalArgumentException oder dokumentierter spezialisierter Untertyp. Nicht unterstuetztes Datum: konsistenter dokumentierter Bereichsfehler. Keine Rueckgabe von Null-/Ersatzdaten, kein stilles Clamping der Datumsumrechnung. Vor Tabellenzugriff Jahr und Grenzen pruefen. Numerische Extremwerte und Long.MIN_VALUE als Rechenoperand explizit abfangen.

## Daten und Grenzen

Implementierter Bereich: chinesische Jahre `1901..2099`, also Gregorian `1901-02-19` bis
`2100-02-08` (72.674 Tage, 2.461 Monate, 73 Schaltjahre). Die Tabelle enthaelt vollstaendige
chinesische Jahre; ausserhalb liegende Tage werden abgelehnt, nicht extrapoliert. Teilweise
sichtbare Randmonate werden in der Compose-Fassade als leere, deaktivierte Zellen behandelt.
Der Generator verglich 2.461 Monatsstarts mit HKO-Referenzen (2.458 exakt gleich); die drei
Abweichungen sind unten und im Manifest dokumentiert. Es wird keine HKO-Tabelle ausgeliefert.

Eigener Datensatz: Monatsbeginn als Epoch Day, Monatsnummer, Schaltflag und Laenge, bzw. aequivalente selbst definierte Kompaktkodierung. Invarianten beim Generieren pruefen: streng steigende Starts, lueckenlose Tage, gueltige Laengen, gueltige Monatsfolge und konsistente Jahresstarts. Praefixindizes fuer begrenzte Suche; keine mutable globale Kalenderinstanz.

Pflichtmanifest: Quelle und Abrufdatum, Nutzungsbedingungen/Attribution, Quellen-Hash, Generatorversion, Datensatzversion und -Hash, exakter Bereich, Referenzkonvention, Konfliktliste und Reviewer. Generator und gepruefte Eingabe muessen reproduzierbar sein; Laufzeit und normale Consumer-Builds brauchen keinen Referenzserver.

Ereignisse nahe lokaler Mitternacht koennen je nach Referenz um einen Tag abweichen. Der
Generator dokumentiert drei solche Faelle: 1914-11-18, 1916-02-04 und 1920-11-11. Fuer jeden
Fall nennt `tools/chinese-calendar/reference-conflicts.json` JDE (TT), Delta-T, UTC-Instant,
UTC+8-Tagesdatum, die unabhaengige NASA/GSFC-Neumondzeit und die Einordnung: der Neumond liegt
1m43s, 5m16s bzw. 4m50s nach UTC+8-Mitternacht, waehrend die HKO-Zeile den UT-Kalendertag
(Vortag) traegt. Die Tabelle bleibt auf dem dokumentierten UTC+8-Datum; es gibt keine
undokumentierte Tabellenkorrektur. `--verify-reference` prueft alle erwarteten Dateien und
Monatsstarts sowie Jahr und Schaltstatus und beendet sich bei Abweichungen mit Status 1.
Datensatzversion `2026.11-astronomical-v1`, Ausgabe-SHA256 und VSOP87D-Eingabe-SHA256 stehen im
Manifest. Aenderungen muessen in spaeteren Releases nachvollziehbar versioniert werden; keine
willkuerlichen Sonderfaelle.

## Compose-Vertrag

Paket: `com.develotter.calendarview.calendars.chineseCompose`. Implementiert:
`ChineseCalendarMonth`, `GregorianChineseCalendarMonth`, `ChineseMonthData`, `ChineseWeekData`
mit `chineseWeekData`, `ChineseMonthState` mit `rememberChineseMonthState`, `ChineseMonthSnapshot`,
`chineseMonthData`, `chineseMonthOfOrNull`, `chineseYearHeatmap` und `chineseMonthHeatmap`.

- CalendarMonthData/CalendarWeekData und den vorhandenen Renderer verwenden.
- Auswahl, Marker und Click-Mapping bleiben Epoch-Day-basiert.
- Zustandsspeicherung umfasst Jahr, Monatsnummer und Schaltflag sowie benoetigte Bereichsparameter; keine plattformgebundenen Datumsobjekte serialisieren.
- Deterministische Konvertierung kennt weder Uhr noch Geraetezeitzone. Heute wird der UI als Epoch Day injiziert; ein plattformspezifischer Komfortadapter darf es explizit berechnen. Anzeigezeitzone fuer Heute und feste Kalenderreferenz sind getrennte Entscheidungen.
- Min/Max-Navigation, goTo, Wiederherstellung und Tag-Clamping dokumentieren. An Grenzen keine nicht unterstuetzten Nebenmonatsdaten erzeugen.
- Titel und Screenreader-Text enthalten das Schaltmonatmerkmal. Keys enthalten die vollstaendige Monatsidentitaet.
- Formatierung ist nicht Berechnung: englische und chinesische Labels, vereinfachte/traditionelle Zeichen und numerischer Fallback separat testen. Keine java.util.Locale-Typen im gemeinsamen Engine-Vertrag.
- Stateless Composables, State nur fuer vom Aufrufer gewuenschte Navigation; keine neue ViewModel-/Flow-/DI-Architektur.

## Referenzen fuer die Implementierung

- HKO Umrechnung und Unsicherheiten: https://www.hko.gov.hk/en/gts/time/conversion.htm
- HKO Referenz 2033: https://www.hko.gov.hk/en/gts/time/calendar/text/files/T2033e.txt
- Android ICU Kalenderkonventionen, nur als Entwicklungsreferenz: https://developer.android.com/reference/kotlin/android/icu/util/ChineseCalendar

Referenz bedeutet keine erteilte Lizenz zur Weiterveroeffentlichung. Nutzungsbedingungen separat pruefen.
