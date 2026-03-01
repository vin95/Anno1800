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
Enthält alle geteilten Ressourcen, organisiert in logische Sub-Records:

```java
public record BoardState(
    FactoryState factories,              // Verfügbare Fabriken
    ResidentCardState residentCards,     // 3 Resident-Card-Stacks
    ExpeditionState expeditions,         // Expedition-Cards
    ShipyardState shipyards,            // Werft-Tiles (3 Level)
    ShipState ships,                    // Schiffe (Trade & Explorer, je 3 Level)
    IslandState islands,                // Inseln (Old World & New World)
    ResourcePoolState resources         // Bevölkerung, Gold, Chips
)
```

**Sub-Records:**
- `FactoryState`: Anzahl verfügbarer Fabriken
- `ResidentCardState`: Stack-Größen (Level 1-3) + Gesamtanzahl
- `ExpeditionState`: Expedition-Stack-Größe
- `ShipyardState`: Verfügbare Werft-Tiles nach Level
- `ShipState`: `ShipLevelCounts` für Trade- und Explorer-Schiffe
- `IslandState`: Verfügbare Inseln (Old World, New World)
- `ResourcePoolState`: Farmers, Workers, Artisans, Engineers, Investors, Gold, Trade-Chips, Explorer-Chips

#### PlayerState
Enthält den Zustand eines einzelnen Spielers, organisiert in logische Sub-Records:

```java
public record PlayerState(
    String name,
    int position,
    TileState tiles,                    // Freie Tiles (Land, Coast, Sea)
    BuildingState buildings,            // Fabriken + Werften
    PlayerShipState ships,              // Schiffe mit Counts + Levels
    PlayerResourceState resources,      // Gold + Chips
    ResidentState residents,            // Anzahl + detaillierte Liste
    CardState cards                     // Resident Cards
)
```

**Sub-Records:**
- `TileState`: Freie Land-, Küsten- und See-Tiles
- `BuildingState`: Anzahl Fabriken und Werften
- `PlayerShipState`: `ShipCountWithLevels` für Trade- und Explorer-Schiffe
  - `ShipCountWithLevels`: Gesamtanzahl + Level-Breakdown (1-3)
- `PlayerResourceState`: Gold, Trade-Chips, Explorer-Chips
- `ResidentState`: Gesamtanzahl + Liste mit ResidentSummary
- `CardState`: Anzahl Resident Cards

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
String currentPlayerName = state.players().get(state.currentPlayerIndex()).name();

// Board-Informationen (verschachtelte Sub-Records)
int availableFactories = state.boardState().factories().availableFactories();
int goldPool = state.boardState().resources().gold();
int farmers = state.boardState().resources().farmers();

// Schiffe nach Level
int tradeShipsLevel1 = state.boardState().ships().tradeShips().level1();
int explorerShipsLevel2 = state.boardState().ships().explorerShips().level2();

// Inseln
int oldWorldIslands = state.boardState().islands().oldWorldSize();
int newWorldIslands = state.boardState().islands().newWorldSize();

// Resident Cards
int totalResidentCards = state.boardState().residentCards().totalAvailable();

// Spieler-Informationen (verschachtelte Sub-Records)
for (PlayerState player : state.players()) {
    System.out.println(player.name() + " has " + player.buildings().factoryCount() + " factories");
    System.out.println(player.name() + " has " + player.ships().tradeShips().totalCount() + " trade ships");
    System.out.println(player.name() + " has " + player.resources().gold() + " gold");
    System.out.println(player.name() + " has " + player.tiles().freeLandTiles() + " free land tiles");
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
- GameState Record mit Timestamp, Round, Current Player Index
- BoardState mit 7 logischen Sub-Records:
  - FactoryState, ResidentCardState, ExpeditionState
  - ShipyardState, ShipState, IslandState, ResourcePoolState
- PlayerState mit 6 logischen Sub-Records:
  - TileState, BuildingState, PlayerShipState
  - PlayerResourceState, ResidentState, CardState
- ResidentSummary Record für detaillierte Resident-Informationen

### 🔄 Schritt 2: Erweitert (Optional)
- Detaillierte Card-Informationen (nicht nur Counts)
- Inventory/Goods tracking pro Spieler
- Factory-Details mit Worker-Assignments

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
GameState state = GameState.createSnapshot(board, players, round, playerIndex);
```

### Nested Records (Hierarchische Struktur)
Records verwenden Sub-Records für logische Gruppierung statt langer Parameterlisten:
```java
// BoardState: 7 Sub-Records statt 25 Parameter
state.boardState().ships().tradeShips().level1()

// PlayerState: 6 Sub-Records statt 18 Parameter  
state.players().get(0).resources().gold()
```

**Vorteile der verschachtelten Struktur:**
- ✅ Selbstdokumentierend: `player.tiles().freeLandTiles()` ist klarer als `player.freeLandTiles()`
- ✅ Gruppierung: Zusammengehörige Daten sind gebündelt
- ✅ Wartbarkeit: Änderungen an Schiffen nur in ShipState
- ✅ Wiederverwendbarkeit: `ShipLevelCounts` wird in BoardState und PlayerState verwendet

### Nested Records
Hierarchische Struktur mit Records (logische Gruppierung):
```
GameState
├── BoardState
│   ├── FactoryState
│   ├── ResidentCardState
│   ├── ExpeditionState
│   ├── ShipyardState
│   ├── ShipState
│   │   ├── ShipLevelCounts (tradeShips)
│   │   └── ShipLevelCounts (explorerShips)
│   ├── IslandState
│   └── ResourcePoolState
└── PlayerState[]
    ├── TileState
    ├── BuildingState
    ├── PlayerShipState
    │   ├── ShipCountWithLevels (tradeShips)
    │   │   └── ShipLevelCounts
    │   └── ShipCountWithLevels (explorerShips)
    │       └── ShipLevelCounts
    ├── PlayerResourceState
    ├── ResidentState
    │   └── ResidentSummary[]
    └── CardState
```

**Vorteil:** Statt 25 flache Parameter hat BoardState 7 logisch gruppierte Sub-Records, PlayerState hat 6 Sub-Records statt 18+ flacher Parameter. Das macht den Code lesbarer und wartbarer.

### No Game Logic
GameState enthält nur Daten, keine Spiellogik:
```java
// ✅ Gut: Nur Datenzugriff (verschachtelte Records)
int gold = state.boardState().resources().gold();
int tradeShips = state.boardState().ships().tradeShips().level1();

// ❌ Schlecht: Spiellogik gehört nicht hierher
// state.buyFactory(); // NICHT in GameState!
```

### Record Decomposition
Statt lange Parameterlisten: Logische Sub-Records:
```java
// ✅ Gut: Gruppierte Daten (BoardState)
state.boardState().ships().tradeShips().level1()
state.boardState().resources().farmers()

// ✅ Gut: Gruppierte Daten (PlayerState)
state.players().get(0).ships().tradeShips().totalCount()
state.players().get(0).resources().gold()
state.players().get(0).tiles().freeLandTiles()

// ❌ Vermieden: Flache Struktur
// state.boardState().tradeShipLevel1Size()
// state.players().get(0).tradeShipCount()
// state.players().get(0).gold()
```

## Beispiel: Vollständiger Workflow

```java
// 1. Spiel initialisieren
Board board = Board.initializeBoard(2);
Player[] players = Player.initializePlayers(2);

// 2. Snapshot erstellen
GameState state = GameState.createSnapshot(
    board, players, 1, 0
);

// 3. Informationen extrahieren (verschachtelte Records)
System.out.println("Round: " + state.round());
System.out.println("Current Player: " + state.players().get(state.currentPlayerIndex()).name());

// Board State
System.out.println("Gold Pool: " + state.boardState().resources().gold());
System.out.println("Factories: " + state.boardState().factories().availableFactories());
System.out.println("Trade Ships L1: " + state.boardState().ships().tradeShips().level1());

// Player State
var player = state.players().get(0);
System.out.println("Player Gold: " + player.resources().gold());
System.out.println("Player Factories: " + player.buildings().factoryCount());
System.out.println("Player Trade Ships: " + player.ships().tradeShips().totalCount());
System.out.println("Free Land Tiles: " + player.tiles().freeLandTiles());

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
           return GameState.createSnapshot(board, players, round, currentPlayerIndex);
       }
   }
   ```

4. **Serialisierung**: JSON Save/Load implementieren

## Praktische Beispiele

### Beispiel 1: Spieler-Ressourcen vergleichen
```java
GameState state = game.getState();

for (var player : state.players()) {
    System.out.printf("%s: %d gold, %d factories, %d ships%n",
        player.name(),
        player.resources().gold(),
        player.buildings().factoryCount(),
        player.ships().tradeShips().totalCount()
    );
}
```

### Beispiel 2: Board-Verfügbarkeit prüfen
```java
var board = state.boardState();

boolean canBuildFactory = board.factories().availableFactories() > 0;
boolean canBuyTradeShip = board.ships().tradeShips().level1() > 0 
    && board.resources().gold() >= 5;
boolean canRecruitFarmers = board.resources().farmers() > 0;
```

### Beispiel 3: KI-Agent Entscheidung
```java
public Action selectAction(GameState state) {
    var player = state.players().get(state.currentPlayerIndex());
    
    // Prüfe Ressourcen
    if (player.resources().gold() >= 10 
        && player.tiles().freeLandTiles() > 0
        && state.boardState().factories().availableFactories() > 0) {
        return new BuildFactoryAction(...);
    }
    
    // Alternative Aktionen...
    return new PassAction();
}
```

## Häufige Fragen

**Q: Warum Records statt Classes?**
A: Records sind immutable by design, kompakt und perfekt für DTOs (Data Transfer Objects).

**Q: Warum Sub-Records statt Builder Pattern?**
A: Builder Pattern ist für *optionale* Parameter gedacht. Bei Records mit vielen *required* Parametern ist Record Decomposition idiomatischer: Lange Parameterliste → logische Sub-Records. Das verbessert Lesbarkeit (`ships().tradeShips().level1()`) und Wartbarkeit.

**Q: Wie viele Parameter wurden durch Sub-Records reduziert?**
A: 
- BoardState: 25 Parameter → 7 Sub-Records
- PlayerState: 18 Parameter → 6 Sub-Records + name/position
- Resultat: Hierarchische, selbstdokumentierende Struktur statt flacher Parameterliste

**Q: Wie aktualisiere ich den GameState?**
A: Gar nicht! GameState ist immutable. Erstelle einen neuen Snapshot nach jeder Änderung.

**Q: Wo gehört Spiellogik hin?**
A: In `Game`, `ActionHandler`, oder andere Service-Klassen. GameState ist nur für Daten!

**Q: Wie speichere ich den GameState?**
A: Serialisiere zu JSON mit einer Bibliothek wie Jackson oder Gson.
