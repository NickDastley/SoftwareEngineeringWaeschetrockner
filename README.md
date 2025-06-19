# SoftwareEngineeringWaeschetrockner

**Projektarbeit KI-4 Software Engineering**  
Technische Hochschule Deggendorf  
Autor: Nico Dilger (22302719)

## Projektbeschreibung

Dieses Projekt simuliert die Steuerung und Bedienung eines Wäschetrockners mit grafischer Benutzeroberfläche. Es können verschiedene Trockenprogramme ausgewählt, der Türstatus gesteuert und Sicherheitsmechanismen wie Türverriegelung und Überhitzungsschutz getestet werden.

## Features

- Auswahl verschiedener Trockenprogramme (Baumwolle, Synthetik, Wolle)
- Simulation von Temperatur- und Feuchtigkeitsverlauf
- Türsteuerung mit Verriegelung und Sicherheitsprüfung
- Ereignis- und Fehlerprotokollierung
- JavaFX-Benutzeroberfläche
- Umfangreiche Unit- und GUI-Tests

## Verwendete Technologien & Bibliotheken

- **Java 21**  
  Hauptprogrammiersprache für die gesamte Logik und GUI.

- **Gradle**  
  Build- und Dependency-Management (siehe [`build.gradle.kts`](build.gradle.kts)).

- **JavaFX**  
  Für die grafische Benutzeroberfläche (GUI).

- **JUnit 5**  
  Für Unit-Tests der Kernlogik.

- **TestFX**  
  Für automatisierte GUI-Tests.

## Projektstruktur

```
src/
  main/
    java/clothdryer/           # Hauptlogik (Simulation, State, Safety, Manager)
    java/clothdryer/scenes/    # JavaFX-GUI-Szenen
  test/
    java/clothdryer/           # Unit-Tests
    java/clothdryer/scenes/    # GUI-Tests
```

## Installation & Ausführung

1. **Voraussetzungen:**  
   - Java 21 oder neuer  
   - Gradle (Wrapper ist enthalten, keine lokale Installation nötig)

2. **Build & Start:**  
   Im Projektverzeichnis ausführen:
   ```sh
   ./gradlew build
   ./gradlew run
   ```

3. **Tests ausführen:**  
   ```sh
   ./gradlew test
   ```

## Bedienung

1. Nach dem Start erscheint die Programmauswahl.
2. Baumwolle, Synthetik oder Wolle auswählen (nur bei geschlossener Tür).
3. Die Tür kann über die UI geöffnet/geschlossen werden, ist während des Programms verriegelt.
4. Nach Programmende kann bei geöffneter Tür neue Wäsche eingelegt werden.
5. Laufende Programme können jederzeit abgebrochen werden.
6. Restlaufzeit, Temperatur und Feuchte werden live angezeigt.

## Lizenz

Dieses Projekt steht unter der [GNU Affero General Public License v3.0](LICENSE).

---

**Kontakt:**  
Für Fragen oder Feedback: [nico.dilger@stud.th-deg.de](mailto:nico.dilger@stud.th-deg.de)