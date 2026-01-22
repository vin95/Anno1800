# Goods System - Planning & Execution Architecture

## Übersicht

Das Goods-System verwendet ein **Plan-Execute-Pattern** mit Rollback-Mechanismus:

1. **Planning Phase** - Simuliert Warenproduktion/-handel ohne tatsächliche Ausführung
2. **Execution Phase** - Führt die tatsächliche Produktion/Handel durch und konsumiert Waren
3. **Rollback** - `storedGoods` wird geleert nach Aktion oder bei Fehlschlag

## Datenstruktur: ProducedGood

```java
record ProducedGood(Goods good, GoodSource source)

sealed interface GoodSource {
    record Produced(Factory factory, Resident resident)
    record Traded(int fromPlayer, int tradeChip)
    record Imported(int explorerChip)
    record FromReward()
    record Other(String description)
}
```

Speichert **Ware + Herkunft + verbrauchte Ressourcen**

## Workflow für Validators

### Beispiel: BuildFactory Validator

```java
public static boolean canBuildFactory(Action.BuildFactory action, Player player, Game game) {
    Factory factory = action.factory();
    
    // 1. Check basic requirements (tiles, availability)
    if (!game.getBoard().hasFactory(factory.getType())) return false;
    if (board.getFreeLandTiles() <= 0) return false;
    
    // 2. PLANNING PHASE: Can player obtain required goods?
    boolean canObtain = board.canObtainGoods(factory.costs());
    
    // 3. ROLLBACK: Clear storedGoods (nothing was actually produced)
    board.clearStoredGoods();
    
    return canObtain;
}
```

### Was passiert in `canObtainGoods()`?

```java
public boolean canObtainGoods(Goods[] required) {
    for (Goods good : required) {
        if (!tryObtainGood(good)) {
            clearStoredGoods();  // Rollback bei Fehlschlag
            return false;
        }
    }
    return true;  // Alle Waren beschaffbar
}
```

### Was macht `tryObtainGood()`?

Versucht Ware zu beschaffen durch:

1. **Produktion** - Factory mit freiem Slot + FIT Resident?
2. **Trade** - Trade Chips verfügbar?
3. **Import** - Explorer Chips + New World Good?

Fügt ProducedGood mit Quelle zu `storedGoods` hinzu.

## Workflow für Actions

### Beispiel: BuildFactory Action

```java
public static void buildFactory(Player player, Factory factory, Game game) {
    PlayerBoard board = player.getPlayerBoard();
    Goods[] costs = factory.costs();
    
    if (costs != null && costs.length > 0) {
        // PLANNING PHASE: Determine how to obtain goods
        if (!board.canObtainGoods(costs)) {
            throw new IllegalStateException("Cannot obtain goods");
        }
        
        // EXECUTION PHASE: Actually obtain and consume goods
        board.consumeGoods(costs);
    }
    
    // Build factory
    Factory factoryFromBoard = game.getBoard().takeFactory(factory.getType());
    board.buildFactory(factoryFromBoard);
}
```

### Was macht `consumeGoods()`?

```java
public void consumeGoods(Goods[] required) {
    for (Goods good : required) {
        // Find in storedGoods
        ProducedGood producedGood = findInStoredGoods(good);
        
        // Execute actual production/trade based on source
        executeGoodSource(producedGood);
        
        // Remove from storedGoods
        storedGoods.remove(producedGood);
    }
    
    clearStoredGoods();  // Cleanup
}
```

### Was macht `executeGoodSource()`?

Führt die **tatsächliche** Aktion aus:

- **Produced**: Weist Resident Factory zu, setzt Status auf AT_WORK
- **Traded**: Verbraucht Trade Chip, entfernt Ware von anderem Spieler
- **Imported**: Verbraucht Explorer Chip

## Aktionen die Waren benötigen

Diese Actions nutzen das System:

1. **BuildFactory** - Benötigt Baumaterialien
2. **BuildShipyard** - Benötigt Baumaterialien
3. **BuildShips** - Benötigt Schiffsbaumaterialien
4. **FulfillNeeds** - Benötigt ResidentCard needs
5. **SettleResident** - Benötigt Siedlungskosten
6. **UpgradeResident** - Benötigt Upgrade-Kosten

## Vorteile des Systems

✅ **Agent kann planen** - "Habe ich genug Ressourcen?" ohne etwas zu produzieren
✅ **Rollback** - Bei Fehlschlag wird nichts verändert
✅ **Transparenz** - Agent sieht genau woher jede Ware kommt
✅ **Flexibilität** - Verschiedene Varianten (2 produziert + 1 gehandelt vs. 3 produziert)
✅ **Atomare Actions** - Eine Action = Planning + Execution + Cleanup

## Beispiel-Ablauf

### Agent will BuildFactory(SteelWorks) ausführen

1. **ActionGenerator** generiert `BuildFactory(SteelWorks)` Action
2. **ActionValidator** prüft:
   ```
   - Factory verfügbar? ✓
   - Freie Tiles? ✓
   - canObtainGoods([COAL, BRICKS])? 
     → tryObtainGood(COAL): Factory + Resident? ✓ → add to storedGoods
     → tryObtainGood(BRICKS): Factory + Resident? ✓ → add to storedGoods
   - clearStoredGoods() → Rollback
   - Return: true
   ```
3. **Agent** wählt Action
4. **ActionExecutor** führt aus:
   ```
   - canObtainGoods([COAL, BRICKS]) → plant wieder
   - consumeGoods([COAL, BRICKS]):
     → executeGoodSource(COAL): Assign Resident to Coal Mine
     → executeGoodSource(BRICKS): Assign Resident to Brick Factory
     → remove from storedGoods
   - clearStoredGoods()
   - Build factory
   ```

## Wichtige Methoden

### PlayerBoard

| Methode | Phase | Beschreibung |
|---------|-------|--------------|
| `canObtainGoods(Goods[])` | Planning | Prüft ob Waren beschaffbar, füllt storedGoods |
| `consumeGoods(Goods[])` | Execution | Führt tatsächliche Produktion/Trade aus |
| `clearStoredGoods()` | Cleanup/Rollback | Leert storedGoods |
| `addProducedGood(ProducedGood)` | Planning | Fügt geplante Ware hinzu |
| `getStoredGoods()` | Info | Gibt aktuelle storedGoods zurück |

## Zukünftige Erweiterungen

1. **Optimierung** - Wähle günstigste Variante (weniger Trade Chips)
2. **Lookahead** - Berücksichtige zukünftige Aktionen
3. **Constraints** - "Produziere nicht mit letztem Resident Level 5"
4. **Caching** - Cache Planning-Ergebnisse für Performance
