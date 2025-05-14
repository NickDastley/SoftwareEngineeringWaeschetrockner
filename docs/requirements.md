# Anforderungen – Trockenprogrammsteuerung

Dieses Dokument beschreibt die funktionalen und nicht-funktionalen Anforderungen an die Software zur Steuerung von Trockenprogrammen in einem Wäschetrockner.

---

## Übersicht

| ID       | Titel                    | Typ            | Priorität | Status     |
|----------|--------------------------|----------------|-----------|------------|
| REQ-001  | Programmauswahl         | Funktional     | Muss      | Offen      |
| REQ-002  | Überhitzungsschutz      | Nicht-Funktional | Muss    | Offen      |
| REQ-003  | Anzeige Restlaufzeit    | Funktional     | Soll      | Offen      |

---

## Anforderungen im Detail

### REQ-001: Programmauswahl
**Typ:** Funktional  
**Priorität:** Muss  
**Beschreibung:** Das System muss dem Benutzer ermöglichen, zwischen den Trockenprogrammen "Baumwolle", "Synthetik" und "Wolle" auszuwählen.  
**Akzeptanzkriterium:** Der Benutzer kann über die UI das gewünschte Programm auswählen und starten. Die Auswahl ist im Display ersichtlich.

---

### REQ-002: Überhitzungsschutz
**Typ:** Nicht-Funktional  
**Priorität:** Muss  
**Beschreibung:** Das System muss sich bei einer gemessenen Temperatur > 85 °C automatisch abschalten.  
**Akzeptanzkriterium:** Bei Tests mit erhöhter Temperatur schaltet das System korrekt ab und zeigt einen Fehlercode an.

---

### REQ-003: Anzeige Restlaufzeit
**Typ:** Funktional  
**Priorität:** Soll  
**Beschreibung:** Während des Trockenvorgangs soll die verbleibende Laufzeit auf dem Display angezeigt werden.  
**Akzeptanzkriterium:** Anzeige ist korrekt synchronisiert mit der tatsächlichen Programmlaufzeit und wird regelmäßig aktualisiert.

---

## Änderungs- und Versionsverfolgung
Alle Änderungen an diesem Dokument sind über Git und Pull Requests nachvollziehbar. Änderungen an Requirements sollen mit nachvollziehbaren Commits dokumentiert werden.

