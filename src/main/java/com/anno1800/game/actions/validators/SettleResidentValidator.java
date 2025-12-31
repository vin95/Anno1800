package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;

/**
 * Validates resident-related actions (settling, upgrading, fulfilling needs, swapping cards).
 */
public class SettleResidentValidator {

    /**
     * Validates SettleResident action.
     * Requirements:
     * - Must have a resident of the specified population level available on the board
     * - Must have a resident card in the corresponding stack on the game board OR
     * - Must have enough gold to pay:
     *   - Population level 1,2: 1 Gold
     *   - Population level 3,4,5: 3 Gold
     */
    public static boolean canSettleResident(Action.SettleResident action, Player player, Game game) {
        int populationLevel = action.level();
        
        // Check if resident is available on the board
        boolean residentAvailable = switch (populationLevel) {
            case 1 -> game.getBoard().getFarmers() > 0;
            case 2 -> game.getBoard().getWorkers() > 0;
            case 3 -> game.getBoard().getArtisans() > 0;
            case 4 -> game.getBoard().getEngineers() > 0;
            case 5 -> game.getBoard().getInvestors() > 0;
            default -> false;
        };
        
        if (!residentAvailable) {
            return false;
        }
        
        // Check if resident card is available in the corresponding stack
        boolean hasResidentCard = switch (populationLevel) {
            case 1, 2 -> !game.getBoard().getResidentStack1().isEmpty();
            case 3, 4, 5 -> !game.getBoard().getResidentStack2().isEmpty();
            default -> false;
        };
        
        // If no resident card available, check if player has enough gold
        if (!hasResidentCard) {
            int goldCost = (populationLevel <= 2) ? 1 : 3;
            return player.getPlayerBoard().getGold() >= goldCost;
        }
        
        return true;
    }
}
