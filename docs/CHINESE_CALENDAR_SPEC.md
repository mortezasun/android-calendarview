# Chinesischer Kalender: API- und Datenvertrag fuer 2.4.0

Status: Entwurf zur Implementierung; keine der genannten APIs existiert durch dieses Dokument.

## Kalenderkonvention

Moderner chinesischer Lunisolarkalender mit dokumentierter ziviler UTC+08:00-Referenz. Keine Zusage historischer Ortszeitregeln oder Gleichheit mit vietnamesischen/koreanischen Varianten. Ein Epoch Day bezeichnet wie bisher das ganzzahlige ISO-Datum relativ zu 1970-01-01, nicht einen Instant und nicht eine lokale Mitternacht in Millisekunden. Dieselbe Tagesidentitaet muss in allen Kalendern erhalten bleiben.

`year` bezeichnet das gregorianische Jahr, in dem das betreffende chinesische Neujahr beginnt. Nicht ungeprueft mit ICU YEAR oder EXTENDED_YEAR gleichsetzen. `monthNumber` liegt in 1..12; Schaltmonate werden durch ein eigenes Flag unterschieden, niemals als Monat 13 kodiert.

## Vorgeschlagene Engine-API

Paket: `com.develotter.calendarview.calendars.chineseNative`.

- `ChineseMonth`: year, monthNumber, isLeapMonth.
- `ChineseDate`: ChineseMonth und dayOfMonth.
- Validierende Factorys bzw. Konstruktoren; keine oeffentlichen Pfade zu ungueltigen Werten. Bei Kotlin-data-classes insbesondere copy()-Umgehungen verhindern oder gleichwertig validieren.
- `ChineseCalendarEngine`: minEpochDay/maxEpochDay, fromEpochDay, toEpochDay, monthsInYear, lengthOfMonth, nextMonth und previousMonth.
- Tagesarithmetik ueber Epoch Days mit gepruefter Addition; Monatsarithmetik ueber die tatsaechliche Reihenfolge der 12/13 Monate. Bei gleichem Tag in kuerzerem Zielmonat wird auf dessen letzten Tag geklemmt; ein nicht vorhandener Schaltmonat wird niemals erfunden.
- Monate des Jahres werden chronologisch geliefert, z.B. regulaerer Monat N, Schaltmonat N, regulaerer Monat N+1. Keine fixe Liste mit genau zwoelf Eintraegen.
- Datumsvergleich erfolgt chronologisch; gleiche Monatsnummer mit anderem Schaltflag ist nicht derselbe Monat.

Invalides Datum: IllegalArgumentException oder dokumentierter spezialisierter Untertyp. Nicht unterstuetztes Datum: konsistenter dokumentierter Bereichsfehler. Keine Rueckgabe von Null-/Ersatzdaten, kein stilles Clamping der Datumsumrechnung. Vor Tabellenzugriff Jahr und Grenzen pruefen. Numerische Extremwerte und Long.MIN_VALUE als Rechenoperand explizit abfangen.

## Daten und Grenzen

Ziel-Referenzfenster: gregorianisch 1901-2100. Das HKO stellt Umrechnungstabellen fuer diese Jahre bereit. Exakte nutzbare Grenzen und vollstaendige Randmonate erst nach Datenpruefung festschreiben; fehlende Tage nicht extrapolieren. Teilweise sichtbare Randmonate muessen klar als solche behandelt werden.

Eigener Datensatz: Monatsbeginn als Epoch Day, Monatsnummer, Schaltflag und Laenge, bzw. aequivalente selbst definierte Kompaktkodierung. Invarianten beim Generieren pruefen: streng steigende Starts, lueckenlose Tage, gueltige Laengen, gueltige Monatsfolge und konsistente Jahresstarts. Praefixindizes fuer begrenzte Suche; keine mutable globale Kalenderinstanz.

Pflichtmanifest: Quelle und Abrufdatum, Nutzungsbedingungen/Attribution, Quellen-Hash, Generatorversion, Datensatzversion und -Hash, exakter Bereich, Referenzkonvention, Konfliktliste und Reviewer. Generator und gepruefte Eingabe muessen reproduzierbar sein; Laufzeit und normale Consumer-Builds brauchen keinen Referenzserver.

HKO-Zukunftsdaten koennen bei Ereignissen nahe Mitternacht um einen Tag abweichen. Fuer 2057, 2089 und 2097 sind entsprechende Neumondfaelle dokumentiert. Datensatzversion fixieren, Grenzen der Zusage offenlegen und Aenderungen in spaeteren Releases nachvollziehbar dokumentieren. Ungeklaerte Abweichungen nicht durch willkuerliche Sonderfaelle kaschieren.

## Compose-Vertrag

Paket: `com.develotter.calendarview.calendars.chineseCompose`. Geplante Namen: ChineseCalendarMonth, ChineseMonthData, ChineseWeekData, ChineseMonthState und rememberChineseMonthState. Benennung vor API-Freeze mit existierenden Konventionen abgleichen.

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
