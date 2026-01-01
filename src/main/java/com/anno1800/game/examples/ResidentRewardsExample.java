package com.anno1800.game.examples;

import com.anno1800.game.actions.Action;
import com.anno1800.game.actions.ActionHandler;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.residents.Resident;
import com.anno1800.game.residents.ResidentStatus;
import com.anno1800.game.rewards.Reward;
import com.anno1800.game.tiles.Factory;

/**
 * Beispiele für die NewResidents und UpgradeResidents Reward-Implementierungen.
 */
public class ResidentRewardsExample {

    /**
     * Demonstriert die NewResidents Reward-Funktionalität
     */
    public static void demonstrateNewResidents(Player player, Game game) {
        System.out.println("=== NewResidents Reward Example ===");
        
        // Zeige Anfangszustand
        int initialPlayerResidents = player.getPlayerBoard().getResidents().size();
        int initialBoardResidents = getBoardResidentCount(game, 1); // Level 1 Farmers
        
        System.out.println("Vor Reward: Player hat " + initialPlayerResidents + " Residents");
        System.out.println("Vor Reward: Board hat " + initialBoardResidents + " Level 1 Residents");
        
        // Erstelle NewResidents Reward (2 Farmers)
        Reward.NewResidents newResidentsReward = new Reward.NewResidents(2, 1);
        
        // Aktiviere den Reward
        Action.ActivateReward activateAction = new Action.ActivateReward(newResidentsReward);
        ActionHandler actionHandler = new ActionHandler(game);
        actionHandler.execute(activateAction, player);
        
        // Zeige Endergebnis
        int finalPlayerResidents = player.getPlayerBoard().getResidents().size();
        int finalBoardResidents = getBoardResidentCount(game, 1);
        
        System.out.println("Nach Reward: Player hat " + finalPlayerResidents + " Residents");
        System.out.println("Nach Reward: Board hat " + finalBoardResidents + " Level 1 Residents");
        System.out.println("Residents hinzugefügt: " + (finalPlayerResidents - initialPlayerResidents));
        
        // Prüfe Status der neuen Residents
        System.out.println("Status der Player-Residents:");
        for (Resident resident : player.getPlayerBoard().getResidents()) {
            System.out.println("  Level " + resident.getPopulationLevel() + 
                             ", Status: " + resident.getStatus().getDisplayName());
        }
        
        System.out.println("=== Ende NewResidents Example ===\\n");
    }
    
    /**
     * Demonstriert die UpgradeResidents Reward-Funktionalität
     */
    public static void demonstrateUpgradeResidents(Player player, Game game) {
        System.out.println("=== UpgradeResidents Reward Example ===");
        
        // Stelle sicher, dass der Player einige Level 1 Residents hat
        ensurePlayerHasResidents(player, game, 1, 3);
        
        // Zeige Anfangszustand
        int initialLevel1 = countPlayerResidentsByLevel(player, 1);
        int initialLevel2 = countPlayerResidentsByLevel(player, 2);
        
        System.out.println("Vor Upgrade: Player hat " + initialLevel1 + " Level 1 Residents");
        System.out.println("Vor Upgrade: Player hat " + initialLevel2 + " Level 2 Residents");
        
        // Erstelle UpgradeResidents Reward (2 Residents von Level 1 zu Level 2)
        Reward.UpgradeResidents upgradeReward = new Reward.UpgradeResidents(2, 1, 2);
        
        // Aktiviere den Reward
        Action.ActivateReward activateAction = new Action.ActivateReward(upgradeReward);
        ActionHandler actionHandler = new ActionHandler(game);
        actionHandler.execute(activateAction, player);
        
        // Zeige Endergebnis
        int finalLevel1 = countPlayerResidentsByLevel(player, 1);
        int finalLevel2 = countPlayerResidentsByLevel(player, 2);
        
        System.out.println("Nach Upgrade: Player hat " + finalLevel1 + " Level 1 Residents");
        System.out.println("Nach Upgrade: Player hat " + finalLevel2 + " Level 2 Residents");
        System.out.println("Level 1 entfernt: " + (initialLevel1 - finalLevel1));
        System.out.println("Level 2 hinzugefügt: " + (finalLevel2 - initialLevel2));
        
        System.out.println("=== Ende UpgradeResidents Example ===\\n");
    }
    
    /**
     * Demonstriert Upgrade eines Residents der in einer Factory arbeitet
     */
    public static void demonstrateUpgradeWorkerInFactory(Player player, Game game, Factory factory) {
        System.out.println("=== UpgradeResidents mit Factory Worker Example ===");
        
        // Stelle sicher, dass ein Level 2 Resident in der Factory arbeitet
        Resident workerResident = null;
        for (Resident resident : player.getPlayerBoard().getResidents()) {
            if (resident.getPopulationLevel() == 2 && 
                resident.getStatus() == ResidentStatus.FIT) {
                workerResident = resident;
                break;
            }
        }
        
        if (workerResident != null) {
            // Setze Resident in Factory Slot 1
            factory.setSlot1(workerResident);
            workerResident.setStatus(ResidentStatus.AT_WORK);
            
            System.out.println("Vor Upgrade: Level 2 Resident arbeitet in Factory (Slot 1)");
            System.out.println("Resident Status: " + workerResident.getStatus().getDisplayName());
            
            // Upgrade von Level 2 zu Level 3
            Reward.UpgradeResidents upgradeReward = new Reward.UpgradeResidents(1, 2, 3);
            Action.ActivateReward activateAction = new Action.ActivateReward(upgradeReward);
            ActionHandler actionHandler = new ActionHandler(game);
            actionHandler.execute(activateAction, player);
            
            // Prüfe Ergebnis
            Resident newWorker = factory.getSlot1();
            if (newWorker != null) {
                System.out.println("Nach Upgrade: Level " + newWorker.getPopulationLevel() + 
                                 " Resident arbeitet in Factory (Slot 1)");
                System.out.println("Neuer Resident Status: " + newWorker.getStatus().getDisplayName());
                System.out.println("Factory-Zuordnung beibehalten: " + 
                                 (newWorker.getStatus() == ResidentStatus.AT_WORK ? "Ja" : "Nein"));
            } else {
                System.out.println("FEHLER: Kein Resident in Factory Slot nach Upgrade!");
            }
        }
        
        System.out.println("=== Ende Factory Worker Upgrade Example ===\\n");
    }
    
    // Helper-Methoden
    
    private static int getBoardResidentCount(Game game, int populationLevel) {
        return switch (populationLevel) {
            case 1 -> game.getBoard().getFarmers();
            case 2 -> game.getBoard().getWorkers();
            case 3 -> game.getBoard().getArtisans();
            case 4 -> game.getBoard().getEngineers();
            case 5 -> game.getBoard().getInvestors();
            default -> 0;
        };
    }
    
    private static int countPlayerResidentsByLevel(Player player, int level) {
        int count = 0;
        for (Resident resident : player.getPlayerBoard().getResidents()) {
            if (resident.getPopulationLevel() == level) {
                count++;
            }
        }
        return count;
    }
    
    private static void ensurePlayerHasResidents(Player player, Game game, int level, int count) {
        int currentCount = countPlayerResidentsByLevel(player, level);
        int needed = count - currentCount;
        
        for (int i = 0; i < needed; i++) {
            try {
                Resident resident = game.getBoard().takeResident(level);
                resident.setStatus(ResidentStatus.FIT);
                player.getPlayerBoard().getResidents().add(resident);
            } catch (Exception e) {
                System.out.println("Konnte keine weiteren Level " + level + " Residents hinzufügen: " + e.getMessage());
                break;
            }
        }
    }
}