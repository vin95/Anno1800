# FreeGoodsChoice Reward Implementation

## Problem

Der `FreeGoodsChoice` Reward war der einzige Reward-Typ, der eine Benutzereingabe erfordert, da der Spieler/Agent aus mehreren verfügbaren Waren eine auswählen muss. Die ursprüngliche Implementierung hatte folgende Probleme:

1. Das `FreeGoodsChoice` Record hatte nur `options[]`, aber kein Feld für die gewählte Ware
2. `ActivateReward` versuchte auf `r.chosenGood()` zuzugreifen, was nicht existierte
3. Es gab keine Möglichkeit, die Wahl interaktiv zu treffen

## Lösung

### 1. Erweiterte FreeGoodsChoice Reward

```java
record FreeGoodsChoice(Goods[] options, Goods chosenGood) implements Reward {
    // Konstruktor für initiale Erstellung (ohne Wahl)
    public FreeGoodsChoice(Goods[] options) {
        this(options, null);
    }
    
    // Methode um eine Wahl zu treffen
    public FreeGoodsChoice withChoice(Goods chosen) { ... }
    
    // Prüft ob eine Wahl getroffen wurde
    public boolean hasChoice() { ... }
}
```

### 2. Neue ChooseGoods Action

```java
record ChooseGoods(Reward.FreeGoodsChoice reward, Goods chosenGood) implements Action {
}
```

### 3. Workflow für FreeGoodsChoice

1. **Erstellung**: `new Reward.FreeGoodsChoice(options)` - nur mit verfügbaren Optionen
2. **Wahl treffen**: `new Action.ChooseGoods(reward, chosenGood)` - gibt ein neues FreeGoodsChoice mit Wahl zurück
3. **Aktivierung**: `new Action.ActivateReward(chosenReward)` - aktiviert den Reward mit der getroffenen Wahl

## Beispiel-Verwendung

```java
// 1. Erstelle Reward mit Optionen
Goods[] options = {Goods.CACAO, Goods.COFFEE_BEANS, Goods.TOBACCO};
Reward.FreeGoodsChoice originalReward = new Reward.FreeGoodsChoice(options);

// 2. Treffe eine Wahl (durch User/Agent Input)
Action.ChooseGoods chooseAction = new Action.ChooseGoods(originalReward, Goods.COFFEE_BEANS);
ActionResult result = actionHandler.execute(chooseAction, player);

// 3. Aktiviere den Reward mit der getroffenen Wahl
if (result instanceof ActionResult.RewardResult rewardResult) {
    Reward.FreeGoodsChoice chosenReward = (Reward.FreeGoodsChoice) rewardResult.reward();
    Action.ActivateReward activateAction = new Action.ActivateReward(chosenReward);
    actionHandler.execute(activateAction, player);
}
```

## Vorteile der Lösung

1. **Type Safety**: Alle Wahl-Validierung erfolgt zur Compile-Zeit und Runtime
2. **Immutability**: Rewards werden nicht verändert, sondern neue Instanzen erstellt
3. **Clear Separation**: Wahl treffen und Reward aktivieren sind separate Actions
4. **Error Handling**: Klare Fehlermeldungen für ungültige oder doppelte Wahlen
5. **Consistency**: Folgt dem gleichen Pattern wie andere Actions im System

## Agent/Bot Integration

Für Agents/Bots kann die Wahl automatisiert werden:

```java
public Goods makeChoice(Reward.FreeGoodsChoice reward, PlayerBoard playerBoard) {
    // Agent-Logik: Wähle basierend auf aktuellen Bedürfnissen
    Goods[] options = reward.options();
    
    // Beispiel: Wähle die erste verfügbare Option, die nicht im Lager ist
    for (Goods option : options) {
        if (!playerBoard.hasGoodInStorage(option)) {
            return option;
        }
    }
    
    // Fallback: Wähle die erste Option
    return options[0];
}
```

## Testing

Siehe `FreeGoodsChoiceExample.java` für vollständige Beispiele und Fehlerszenarien.