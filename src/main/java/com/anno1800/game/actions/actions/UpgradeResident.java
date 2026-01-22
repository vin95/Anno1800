package com.anno1800.game.actions.actions;

import com.anno1800.game.actions.Action;
import com.anno1800.game.board.Board;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.residents.Resident;
import com.anno1800.game.residents.ResidentCosts;

/**
 * Upgrade a resident to the next population level.
 * 
 * PRECONDITION: ActionValidator has verified all requirements.
 * Goods are obtained and consumed during this action.
 */
public class UpgradeResident {
    
    /**
     * Upgrades up to 3 residents to the next level.
     * For each resident: removes from PlayerBoard, obtains required goods,
     * and adds a new higher-level resident from GameBoard.
     * 
     * @param player The player upgrading the residents
     * @param game The current game state
     * @param action The upgrade resident action (with 1-3 residents)
     */
    public static void upgradeResident(Player player, Game game, Action.UpgradeResident action) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        Board gameBoard = game.getBoard();
        Resident[] residents = action.residents();
        
        // Validate array
        if (residents == null || residents.length == 0 || residents.length > 3) {
            throw new IllegalArgumentException("Must upgrade 1-3 residents, got: " + 
                (residents == null ? "null" : residents.length));
        }
        
        System.out.println("Upgrading " + residents.length + " resident(s)");
        
        // Upgrade each resident
        for (Resident oldResident : residents) {
            int currentLevel = oldResident.getPopulationLevel();
            int targetLevel = currentLevel + 1;
            
            // Validate
            if (currentLevel >= 5) {
                throw new IllegalStateException("Cannot upgrade level 5 resident");
            }
            
            if (!playerBoard.getResidents().contains(oldResident)) {
                throw new IllegalStateException("Resident does not belong to player");
            }
            
            // Get required goods
            ResidentCosts.Cost cost = ResidentCosts.getUpgradeCost(targetLevel);
            
            if (cost.goods() != null && cost.goods().length > 0) {
                System.out.println("  Upgrading resident from level " + currentLevel + " to " + targetLevel);
                System.out.println("    Requires: " + java.util.Arrays.toString(cost.goods()));
                
                // PLANNING PHASE: Determine how to obtain goods
                if (!playerBoard.canObtainGoods(cost.goods())) {
                    throw new IllegalStateException("Cannot obtain required goods for upgrade");
                }
                
                // EXECUTION PHASE: Actually obtain and consume goods
                playerBoard.consumeGoods(cost.goods());
            }
            
            // Remove old resident from PlayerBoard
            playerBoard.getResidents().remove(oldResident);
            System.out.println("    Removed resident level " + currentLevel);
            
            // Take new resident from GameBoard
            Resident newResident = gameBoard.takeResident(targetLevel);
            
            // Copy status from old resident (if they were AT_WORK, new one should be too)
            newResident.setStatus(oldResident.getStatus());
            
            // Add new resident to PlayerBoard
            playerBoard.getResidents().add(newResident);
            System.out.println("    Added resident level " + targetLevel + " with status " + newResident.getStatus());
            
            System.out.println("  Successfully upgraded resident from level " + currentLevel + " to " + targetLevel);
        }
        
        System.out.println("All " + residents.length + " resident(s) upgraded successfully");
    }
}
