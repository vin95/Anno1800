package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;

/**
 * Validator for FulfillNeeds actions.
 */
public class FulfillNeedsValidator {
    /**
     * Checks if a player can fulfill the needs of a resident card.
     * Uses planning phase to check if all required goods can be obtained.
     * 
     * @param action The FulfillNeeds action
     * @param residentCard The resident card whose needs to check
     * @param player The player to check
     * @param game The current game state
     * @return true if all needs can be fulfilled
     */
    public static boolean canFulfillNeeds(Action.FulfillNeeds action, ResidentCard residentCard, Player player, Game game) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        
        // Check if card belongs to player
        if (!playerBoard.getResidentCards().contains(residentCard)) {
            return false;
        }
        
        // PLANNING PHASE: Check if player can obtain all required goods
        // Pass game context to enable ExplorerTrader logic (2 explorer chips = 1 trade chip)
        boolean canObtain = playerBoard.canObtainGoods(residentCard.needs(), game);
        
        // Clear storedGoods after validation (rollback)
        playerBoard.clearStoredGoods();
        
        return canObtain;
    }
}