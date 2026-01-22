package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.residents.Resident;
import com.anno1800.game.residents.ResidentCosts;

/**
 * Validates resident upgrade actions.
 */
public class UpgradeResidentValidator {

    /**
     * Validates UpgradeResident action.
     * Requirements:
     * - At least 1 and at most 3 residents
     * - Each resident must belong to player (on PlayerBoard)
     * - Each resident level must be < 5 (can't upgrade level 5)
     * - Higher level resident must be available on GameBoard for each
     * - Required goods must be obtainable (production/trade/import) for all upgrades
     */
    public static boolean canUpgradeResident(Action.UpgradeResident action, Player player, Game game) {
        Resident[] residents = action.residents();
        PlayerBoard playerBoard = player.getPlayerBoard();
        
        // Check array size (1-3 residents)
        if (residents == null || residents.length == 0 || residents.length > 3) {
            return false;
        }
        
        // Validate each resident and accumulate goods costs
        for (Resident resident : residents) {
            // Check if resident belongs to player
            if (!playerBoard.getResidents().contains(resident)) {
                playerBoard.clearStoredGoods();
                return false;
            }
            
            // Check if resident can be upgraded (level < 5)
            int currentLevel = resident.getPopulationLevel();
            if (currentLevel >= 5) {
                playerBoard.clearStoredGoods();
                return false;
            }
            
            int targetLevel = currentLevel + 1;
            
            // Check if higher level resident is available on GameBoard
            if (!hasResidentAvailable(game, targetLevel)) {
                playerBoard.clearStoredGoods();
                return false;
            }
            
            // PLANNING PHASE: Check if player can obtain required goods for this upgrade
            ResidentCosts.Cost cost = ResidentCosts.getUpgradeCost(targetLevel);
            if (!playerBoard.canObtainGoods(cost.goods())) {
                playerBoard.clearStoredGoods();
                return false;
            }
        }
        
        // Clear storedGoods after validation (rollback)
        playerBoard.clearStoredGoods();
        
        return true;
    }
    
    /**
     * Checks if a resident of the specified level is available on the game board.
     */
    private static boolean hasResidentAvailable(Game game, int level) {
        return switch (level) {
            case 1 -> game.getBoard().getFarmers() > 0;
            case 2 -> game.getBoard().getWorkers() > 0;
            case 3 -> game.getBoard().getArtisans() > 0;
            case 4 -> game.getBoard().getEngineers() > 0;
            case 5 -> game.getBoard().getInvestors() > 0;
            default -> false;
        };
    }
}
