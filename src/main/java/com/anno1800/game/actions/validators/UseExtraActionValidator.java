package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.cards.ObjectiveCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;

/**
 * Validator for the UseExtraAction free action.
 * Checks if the player can use the Extra Action from the ExtraAction ObjectiveCard.
 * 
 * Requirements:
 * - ExtraAction ObjectiveCard must be active in the game
 * - Player has not used this action yet this turn
 * - Player has at least 3 Gold
 * - Player has at least 3 Explorer Chips
 */
public class UseExtraActionValidator {

    /**
     * Checks if the UseExtraAction can be executed.
     * 
     * @param action The UseExtraAction action
     * @param player The player attempting the action
     * @param game The current game state
     * @return true if the action can be executed
     */
    public static boolean canUseExtraAction(Action.UseExtraAction action, Player player, Game game) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        
        // Check if ExtraAction ObjectiveCard is active
        boolean hasCard = game.getActiveObjectiveCards().stream()
            .anyMatch(card -> card instanceof ObjectiveCard.ExtraAction);
        if (!hasCard) {
            return false;
        }
        
        // Check if already used this turn
        if (playerBoard.hasUsedExtraActionThisTurn()) {
            return false;
        }
        
        // Check gold (3 required)
        if (playerBoard.getGold() < 3) {
            return false;
        }
        
        // Check explorer chips (3 required)
        if (playerBoard.getAvailableExplorerChips() < 3) {
            return false;
        }
        
        return true;
    }
}
