# NewResidents und UpgradeResidents Reward Implementierung

## Übersicht

Diese Implementierung vervollständigt die Aktivierung der `NewResidents` und `UpgradeResidents` Rewards in der [ActivateReward.java](ActivateReward.java) Klasse.

## NewResidents Reward

### Funktionalität
Der `NewResidents` Reward fügt neue Residents zum PlayerBoard hinzu, die direkt vom GameBoard entnommen werden.

### Parameter
- `amount`: Anzahl der hinzuzufügenden Residents
- `populationLevel`: Bevölkerungslevel der Residents (1-5)

### Implementierung
```java
case Reward.NewResidents r -> {
    // Nimm r.amount() Residents vom GameBoard und füge sie dem PlayerBoard mit Status FIT hinzu
    for (int i = 0; i < r.amount(); i++) {
        Resident resident = game.getBoard().takeResident(r.populationLevel());
        resident.setStatus(FIT);
        player.getPlayerBoard().getResidents().add(resident);
    }
}
```

### Ablauf
1. Für jede gewünschte Anzahl (`amount`)
2. Nimm einen Resident vom GameBoard (`takeResident`)
3. Setze Status auf `FIT`
4. Füge Resident zum PlayerBoard hinzu

## UpgradeResidents Reward

### Funktionalität
Der `UpgradeResidents` Reward ersetzt bestehende Residents durch höhere Bevölkerungslevels, während Status und Factory-Zuordnungen erhalten bleiben.

### Parameter
- `amount`: Anzahl der zu upgradenden Residents
- `populationLevel1`: Aktuelles Level der zu upgradenden Residents
- `populationLevel2`: Ziel-Level nach dem Upgrade

### Implementierung
Die Implementierung ist komplex, da sie verschiedene Zustände handhaben muss:

1. **Resident finden**: Suche einen Resident mit dem entsprechenden Level
2. **Status speichern**: Merke dir den aktuellen Status (FIT, AT_WORK, EXHAUSTED)
3. **Factory-Info speichern**: Falls der Resident in einer Factory arbeitet, merke dir welche Factory und welcher Slot
4. **Factory-Zuordnung entfernen**: Entferne den Resident aus der Factory
5. **Resident austauschen**: Entferne vom PlayerBoard, gib zum GameBoard zurück, nimm neuen vom GameBoard
6. **Status wiederherstellen**: Setze den gleichen Status beim neuen Resident
7. **Factory-Zuordnung wiederherstellen**: Falls der Resident in einer Factory war, setze den neuen in den gleichen Slot

### Ablauf (detailliert)
```java
for (int i = 0; i < r.amount(); i++) {
    // 1. Finde Resident mit populationLevel1
    Resident oldResident = ...;
    
    if (oldResident != null) {
        // 2. Speichere Status und Factory-Info
        ResidentStatus oldStatus = oldResident.getStatus();
        Factory factoryWithResident = null;
        int slotNumber = 0;
        
        // 3. Suche Factory-Zuordnung
        for (Factory factory : playerBoard.getFactories()) {
            if (factory.getSlot1() == oldResident) {
                factoryWithResident = factory;
                slotNumber = 1;
                break;
            } else if (factory.getSlot2() == oldResident) {
                factoryWithResident = factory;
                slotNumber = 2;
                break;
            }
        }
        
        // 4. Entferne Factory-Zuordnung
        if (factoryWithResident != null) {
            factoryWithResident.setSlot1/2(null);
        }
        
        // 5. Resident austauschen
        playerBoard.getResidents().remove(oldResident);
        game.getBoard().returnResident(populationLevel1, oldResident);
        Resident newResident = game.getBoard().takeResident(populationLevel2);
        
        // 6. Status wiederherstellen
        newResident.setStatus(oldStatus);
        playerBoard.getResidents().add(newResident);
        
        // 7. Factory-Zuordnung wiederherstellen
        if (factoryWithResident != null) {
            factoryWithResident.setSlot1/2(newResident);
        }
    }
}
```

## Besondere Eigenschaften

### Status-Erhaltung
- **FIT**: Resident kann normal arbeiten
- **AT_WORK**: Resident arbeitet gerade in einer Factory
- **EXHAUSTED**: Resident ist erschöpft und kann nicht arbeiten

Der neue Resident erhält exakt den gleichen Status wie der ersetzte Resident.

### Factory-Zuordnung-Erhaltung
Falls ein Resident in einer Factory arbeitet:
- Der Slot wird zwischenzeitlich geleert
- Der neue Resident wird in den exakt gleichen Slot gesetzt
- Der Status bleibt AT_WORK

### Fehlerbehandlung
- Falls kein Resident mit dem gewünschten Level vorhanden ist, wird dieser Upgrade-Versuch übersprungen
- Falls keine Residents auf dem GameBoard verfügbar sind, wirft `takeResident` eine Exception

## Verwendungsbeispiele

### Beispiel 1: Neue Farmer hinzufügen
```java
Reward.NewResidents reward = new Reward.NewResidents(2, 1); // 2 Farmer
// Fügt 2 Farmer mit Status FIT zum PlayerBoard hinzu
```

### Beispiel 2: Worker zu Artisans upgraden
```java
Reward.UpgradeResidents reward = new Reward.UpgradeResidents(3, 2, 3); // 3 Worker -> Artisans
// Ersetzt bis zu 3 Worker durch Artisans, erhält Status und Factory-Zuordnungen
```

### Beispiel 3: Arbeitenden Resident upgraden
```java
// Resident arbeitet in Factory Slot 1 mit Status AT_WORK
// Nach Upgrade: Neuer Resident arbeitet weiter in Factory Slot 1 mit Status AT_WORK
```

## Tests

Die Implementierung kann mit der [ResidentRewardsExample.java](ResidentRewardsExample.java) Klasse getestet werden, die verschiedene Szenarien demonstriert.

## Abhängigkeiten

Die Implementierung nutzt:
- `GameBoard.takeResident()` - Nimmt Resident vom Board
- `GameBoard.returnResident()` - Gibt Resident zum Board zurück  
- `Resident.setStatus()` - Setzt Resident-Status
- `Factory.getSlot1/2()` und `Factory.setSlot1/2()` - Factory-Zuordnung
- `PlayerBoard.getResidents()` - Zugriff auf Player-Residents