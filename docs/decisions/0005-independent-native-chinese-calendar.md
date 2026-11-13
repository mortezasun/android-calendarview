# ADR 0005: selbstgeschriebener chinesischer Kalender

Datum: 2026-11-13. Status: Zielentscheidung fuer die Planung; Umsetzung offen.

## Kontext

2.3.0 enthaelt bereits einen gemeinsamen Compose-Kern und getrennte Kalender-Module. Der neue chinesische Kalender muss selbstgeschrieben, optional einbindbar und von anderen Kalender-Engines unabhaengig sein. Native bedeutet hier eigene plattformunabhaengige Kotlin-Berechnung, nicht lediglich ein Kotlin-Wrapper um Android ICU.

## Entscheidung

Zwei getrennte Artefakte: `:calendar-chinese-native` fuer Berechnung und immutable Datumswerte; `:calendar-chinese-compose` fuer Darstellung und UI-Zustand. Abhaengigkeitsrichtung: Compose -> Engine und bestehender UI-Kern. Engine -> Kotlin-Standardbibliothek. Bestehende Engines und UI-Kern -> keine neue Engine.

Keine Uebernahme oder Portierung von lunar-java, ICU, Internet-Snippets oder fremden Kalenderalgorithmen-Implementierungen. Kalenderdaten sind getrennt vom eigenen Code zu behandeln: Herkunft, Nutzungsrechte und Erzeugungsverfahren muessen nachgewiesen sein. Selbstgeschriebener Code bedeutet nicht selbst beobachtete astronomische Daten.

Die erste Engine benutzt eigene ganzzahlige Umrechnungslogik mit versionierten, kompakten Monatsdaten. Das ist eine native Offline-Implementierung, aber keine astronomische Echtzeitberechnung. Unterstuetzter Bereich und Datensatzversion sind Teil des Vertrags. Keine extrapolierten Ergebnisse ausserhalb des Bereichs.

Bestehende Compose-APIs bleiben unveraendert. KMP-/Kotlin-Native-Unterstuetzung der Engine wird durch echte Builds und Tests belegt. Die Android-Compose-Fassade wird nicht als iOS-faehig bezeichnet, solange ihr gemeinsamer Kern nicht nachweislich multiplatform ist.

## Folgen und Alternativen

Vorteile: Engine ohne UI nutzbar, klare Abhaengigkeitspruefung, identische Berechnung ueber Plattformen, additive Einfuehrung. Kosten: zwei Publikationen, Datensatzpflege und zusaetzliche Native-CI. Ein einzelnes Android-Modul waere einfacher, wuerde aber echte UI-Unabhaengigkeit und Native-Verifikation erschweren. Eine komplette KMP-Migration aller bestehenden Module ist eine separate Architekturaufgabe.

Verbindliche Ausfuehrung: [Releaseplan](../../RELEASE_PLAN_2.4.0.md), [Spezifikation](../CHINESE_CALENDAR_SPEC.md), [Qualitaet](../CHINESE_CALENDAR_QUALITY.md).
