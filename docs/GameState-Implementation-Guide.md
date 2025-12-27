# GameState Implementierung - Vollständige Anleitung

## Übersicht

Der `GameState` ist eine **immutable Datenstruktur**, die den kompletten Spielzustand zu einem bestimmten Zeitpunkt speichert. Er dient als:
- Snapshot für Speichern/Laden
- Basis für KI-Agenten
- Debug-Tool zur Spielzustandsanalyse

## Architektur

### 1. Hauptstruktur: GameState (Record)

```java
public record GameState(
    Instant timestamp,           // Zeitstempel des Snapshots
    int round,                   // Aktuelle Runde
    Phase currentPhase,          // Aktuelle Phase
    int currentPlayerIndex,      // Index des aktiven Spielers
    BoardState boardState,       // Shared Board-Zustand
    List<PlayerState> players    // Alle Spieler-Zustände
)
```

### 2. Nested Records

#### BoardState
Enthält alle geteilten Ressourcen:
- Factory Stacks (Anzahl verfügbarer Fabriken)
- Resident Card Stacks (3 verschiedene Level)
- Expedition Cards
- Shipyards (3 Level)
- Ships (Trade & Explorer, je 3 Level)
- Islands (Old World & New World)
- Shared Pools (Farmers, Workers, Artisans, Engineers, Investors, Gold)

#### PlayerState
Enthält den Zustand eines einzelnen Spielers:
- Name
- Tiles (Land, Coast, Sea)
- Buildings (Factories, Shipyards)
- Ships (Trade, Explorer)
- Residents (Anzahl und Details)

#### ResidentSummary
Kompakte Info über einen Resident:
- Level (1-5)
- Status (FIT, AT_WORK, EXHAUSTED)

## Verwendung

### Schritt 1: GameState erstellen

```java
// Setup
Board board = Board.initializeBoard(numPlayers);
Player[] players = Player.initializePlayers(numPlayers);

// Snapshot erstellen
GameState state = GameState.createSnapshot(
    board,
    players,
    1,                    // Runde
    Phase.PRODUCTION,     // Phase
    0                     // Aktueller Spieler-Index
);
```

### Schritt 2: Daten auslesen

```java
// Basis-Informationen
int round = state.round();
Phase phase = state.currentPhase();
String currentPlayerName = state.players().get(state.currentPlayerIndex()).name();

// Board-Informationen
int availableFactories = state.boardState().availableFactories();
int goldPool = state.boardState().gold();

// Spieler-Informationen
for (PlayerState player : state.players()) {
    System.out.println(player.name() + " has " + player.factoryCount() + " factories");
}
```

### Schritt 3: Ausgeben

```java
GameStatePrinter printer = new GameStatePrinter();

// Detaillierte Ansicht
printer.printDetailed(state);

// Kompakte Zusammenfassung
printer.printSummary(state);

// JSON-Format
printer.printJson(state);
```

## Schrittweise Implementierung

### ✅ Schritt 1: Basis (FERTIG)
- GameState Record mit Timestamp, Round, Phase, Current Player
- Phase Enum
- BoardState Record (mit allen Stacks)
- PlayerState Record (mit Tiles, Buildings, Residents)
- ResidentSummary Record

### 🔄 Schritt 2: Erweitert (Optional)
- ResidentCards im Spielerbesitz
- ExplorationCards im Spielerbesitz
- Inventory/Goods tracking
- Gold pro Spieler

### 🔄 Schritt 3: Serialisierung (Optional)
- JSON Serialisierung
- Speichern/Laden von Spielständen

### 🔄 Schritt 4: Deep Copy (Optional)
- Detaillierte Factory-Informationen
- Worker-Assignments in Factories
- Detaillierte Card-Informationen

## Designprinzipien

### Immutability
Alle Felder sind `final`, alle Records sind unveränderlich.
```java
public record GameState(...) {  // Immutable by design
    // Keine Setter, nur Getter
}
```

### Factory Methods
Statische Factory-Methoden erstellen Snapshots:
```java
GameState state = GameState.createSnapshot(board, players, round, phase, playerIndex);
```

### Nested Records
Hierarchische Struktur mit Records:
```
GameState
├── BoardState
└── PlayerState[]
    └── ResidentSummary[]
```

### No Game Logic
GameState enthält nur Daten, keine Spiellogik:
```java
// ✅ Gut: Nur Datenzugriff
int gold = state.boardState().gold();

// ❌ Schlecht: Spiellogik gehört nicht hierher
// state.buyFactory(); // NICHT in GameState!
```

## Beispiel: Vollständiger Workflow

```java
// 1. Spiel initialisieren
Board board = Board.initializeBoard(2);
Player[] players = Player.initializePlayers(2);

// 2. Snapshot erstellen
GameState state = GameState.createSnapshot(
    board, players, 1, Phase.PRODUCTION, 0
);

// 3. Informationen extrahieren
System.out.println("Round: " + state.round());
System.out.println("Phase: " + state.currentPhase());
System.out.println("Current Player: " + state.players().get(state.currentPlayerIndex()).name());

// 4. Ausgeben
GameStatePrinter printer = new GameStatePrinter();
printer.printDetailed(state);

// 5. Für KI verwenden
// AI-Agent kann state analysieren und Entscheidungen treffen
```

## Nächste Schritte

1. **Testen**: Beispiel ausführen
   ```bash
   ./gradlew run
   ```

2. **Erweitern**: Weitere Informationen hinzufügen
   - Spieler-Gold
   - Inventory/Goods
   - Detaillierte Card-Informationen

3. **Integration**: In Game-Klasse einbinden
   ```java
   public class Game {
       public GameState getState() {
           return GameState.createSnapshot(...);
       }
   }
   ```

4. **Serialisierung**: JSON Save/Load implementieren

## Häufige Fragen

**Q: Warum Records statt Classes?**
A: Records sind immutable by design, kompakt und perfekt für DTOs (Data Transfer Objects).

**Q: Wie aktualisiere ich den GameState?**
A: Gar nicht! GameState ist immutable. Erstelle einen neuen Snapshot nach jeder Änderung.

**Q: Wo gehört Spiellogik hin?**
A: In `Game`, `ActionHandler`, oder andere Service-Klassen. GameState ist nur für Daten!

**Q: Wie speichere ich den GameState?**
A: Serialisiere zu JSON mit einer Bibliothek wie Jackson oder Gson.
