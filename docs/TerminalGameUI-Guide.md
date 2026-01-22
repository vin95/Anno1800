# Terminal Game UI - Anno 1800

## Übersicht

Eine terminalbasierte Spieloberfläche für Anno 1800, die es 2 menschlichen Spielern ermöglicht, das Spiel über die Kommandozeile zu spielen.

## Features

✅ **Vollständige Spielsteuerung**
- Zeigt aktuellen Spieler und verfügbare Aktionen an
- Aktionsauswahl über Terminaleingabe (Nummern 1-9)
- Automatische Validierung aller Aktionen

✅ **Gamestate Management**
- Nach jeder Aktion wird ein neuer vollständiger Gamestate gespeichert
- Jederzeit Anzeige des aktuellen Gamestates (`state`)
- Jederzeit Anzeige der Differenzen zum vorherigen Gamestate (`diff`)

✅ **Benutzerfreundlich**
- Klare Anzeige des Spielfortschritts (Runde, aktueller Spieler)
- Hilfe-Funktion (`help`)
- Formatierte Ausgabe aller Aktionen
- Detaillierte Spielende-Anzeige mit Punktestand

## Verwendung

### Spiel starten

```bash
# Mit Gradle
./gradlew run --console=plain --args="terminal"

# Oder direkt die Klasse ausführen
java -cp build/classes/java/main com.anno1800.ui.TerminalGameUI
```

### Verfügbare Befehle während des Spiels

| Befehl | Beschreibung |
|--------|--------------|
| `1-9` | Wähle eine Aktion nach Nummer aus |
| `state` | Zeige den vollständigen aktuellen Gamestate an |
| `diff` | Zeige Unterschiede zum vorherigen Gamestate |
| `help` | Zeige Hilfe-Information |
| `quit` | Beende das Spiel |

### Spielablauf

1. **Initialisierung**: Das Spiel zeigt den Startspieler an
2. **Rundenzyklus**: Jeder Spieler ist abwechselnd an der Reihe
3. **Aktionsauswahl**: 
   - Verfügbare Aktionen werden nummeriert angezeigt
   - Spieler gibt Nummer ein (1-9)
   - Oder verwendet einen der Spezialbefehle
4. **Ausführung**: 
   - Aktion wird validiert und ausführt
   - Neuer Gamestate wird gespeichert
   - Nächster Spieler ist an der Reihe
5. **Spielende**: Nach MAX_ROUNDS oder wenn die End-Phase ausgelöst wurde

## Beispiel-Session

```
================================================================================
                         ANNO 1800 - BOARD GAME
                         Terminal Edition
================================================================================

Welcome to Anno 1800!
This is a 2-player terminal-based game.

Type 'help' at any time to see available commands.

--------------------------------------------------------------------------------
Starting Player: Player 1 (Position 1)
--------------------------------------------------------------------------------

================================================================================
Round 1 - Player 1's Turn (Position 1)
================================================================================

Available Actions:
--------------------------------------------------------------------------------
[1] Build Factory: SAWMILL_BLUE (costs: WOOD, GOLD)
[2] Build Factory: GRAIN_FARM_BLUE (costs: GOLD)
[3] Settle Resident (Level 1)
[4] Assign Worker to SAWMILL_GREEN

Special Commands:
  'state' - Show current game state
  'diff'  - Show differences from previous state
  'help'  - Show help
  'quit'  - Exit game

Your choice: 1

Executing: Build Factory: SAWMILL_BLUE (costs: WOOD, GOLD)
✓ Action executed successfully!

[Game state saved - 2 states in history]

================================================================================
Round 1 - Player 2's Turn (Position 2)
================================================================================
...
```

## Architektur

### Hauptkomponenten

- **TerminalGameUI**: Hauptklasse für die Benutzeroberfläche
  - Verwaltet den Spielloop
  - Zeigt Aktionen an
  - Verarbeitet Benutzereingaben
  - Speichert Gamestate-Historie

- **ActionGenerator**: Generiert alle möglichen Aktionen für einen Spieler
  - Wird erweitert werden müssen für alle 24 Aktionstypen

- **GameStatePrinter**: Formatierte Ausgabe von Gamestates
  - Detaillierte Ansicht
  - Zusammenfassung
  - JSON-Format

### Gamestate-Tracking

Jeder Gamestate wird als unveränderliches `GameState`-Objekt gespeichert:

```java
private GameState previousState;  // Zustand vor der letzten Aktion
private GameState currentState;   // Aktueller Zustand
private List<GameState> stateHistory; // Komplette Historie
```

Die `diff`-Funktion vergleicht `previousState` mit `currentState` und zeigt:
- Änderungen bei jedem Spieler (Gold, Factories, Residents, etc.)
- Änderungen auf dem Board (Ressourcenpools, verfügbare Karten)

## TODOs / Erweiterungen

⚠️ **ActionGenerator erweitern**
- Aktuell werden nur `BuildFactory` Aktionen generiert
- Alle 24 Aktionstypen müssen implementiert werden

📝 **Mögliche Verbesserungen**
- Gamestate in Dateien speichern (JSON/TXT)
- Undo-Funktion (zurück zu vorherigem State)
- Replay-Funktion (gespeicherte Spiele abspielen)
- Farbige Terminal-Ausgabe
- Bessere Formatierung der Aktionslisten (Gruppierung nach Typ)

## Klassenstruktur

```
ui/
  ├── TerminalGameUI.java         # Haupt-UI-Klasse
  └── output/
      └── GameStatePrinter.java   # Gamestate-Formatierung

game/
  ├── engine/
  │   └── Game.java               # Spielcontroller
  ├── actions/
  │   ├── ActionGenerator.java    # Generiert mögliche Aktionen
  │   └── ActionValidator.java    # Validiert Aktionen
  └── state/
      └── GameState.java          # Unveränderlicher State-Snapshot
```

## Abhängigkeiten

- Java 21 (sealed interfaces, records, pattern matching)
- Keine externen Bibliotheken erforderlich (pure Java)
- Scanner für Terminaleingabe
- Instant für Timestamps

## Bekannte Einschränkungen

1. **ActionGenerator unvollständig**: Generiert aktuell nur BuildFactory-Aktionen
2. **Keine Persistierung**: Gamestates werden nur im Speicher gehalten
3. **Nur 2 Spieler**: Hardcoded für 2 menschliche Spieler
4. **Keine Input-Validierung**: Angenommen werden korrekte Eingaben

## Kontakt & Entwicklung

Diese UI ist Teil des Anno 1800 Board Game Projekts.
Siehe Hauptprojekt-README für weitere Informationen.
